/*
 * ORACLE 专有/机密。使用受许可条款约束。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * 由 Doug Lea 编写，并在 JCP JSR-166 专家小组成员的帮助下发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的。
 */

package java.util.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.security.AccessControlContext;
import java.security.ProtectionDomain;
import java.security.Permissions;

/**
 * 用于运行 {@link ForkJoinTask} 的 {@link ExecutorService}。
 * {@code ForkJoinPool} 为非 {@code ForkJoinTask} 客户端提供了提交入口，以及管理和
 * 监控操作。
 *
 * <p>{@code ForkJoinPool} 与其他类型的 {@link
 * ExecutorService} 的主要区别在于它采用了
 * <em>工作窃取</em>：池中的所有线程都试图找到并执行提交给池的任务和/或由其他活跃任务
 * 创建的任务（如果不存在任务，最终会阻塞等待工作）。这使得在大多数任务生成其他子任务
 * （如大多数 {@code ForkJoinTask}）时，以及当许多小任务从外部客户端提交到池中时，能够
 * 高效处理。特别是在构造函数中将 <em>asyncMode</em> 设置为 true 时，{@code
 * ForkJoinPool} 也适用于那些通常不会被 join 的事件风格的任务。
 *
 * <p>提供了一个静态的 {@link #commonPool()}，适用于大多数应用程序。公共池用于任何
 * 未明确提交到指定池的 ForkJoinTask。使用公共池通常可以减少资源使用（在非使用期间，其线程会慢慢被回收，并在随后的使用中重新启动）。
 *
 * <p>对于需要单独或自定义池的应用程序，可以通过给定的目标并行度级别来构造一个 {@code
 * ForkJoinPool}；默认情况下，等于可用处理器的数量。池试图通过动态添加、暂停或恢复内部工作线程来维持足够的活跃（或可用）线程，
 * 即使某些任务因等待加入其他任务而停滞。然而，面对阻塞的 I/O 或其他未管理的同步时，不会做出这样的调整。嵌套的 {@link
 * ManagedBlocker} 接口允许扩展所支持的同步类型。
 *
 * <p>除了执行和生命周期控制方法外，此类还提供了状态检查方法（例如
 * {@link #getStealCount}），旨在帮助开发、调整和监控 fork/join 应用程序。此外，方法
 * {@link #toString} 以方便的形式返回池状态的指示，以便非正式监控。
 *
 * <p>与其他 ExecutorServices 一样，有三种主要的任务执行方法，总结在下表中。
 * 这些方法主要是为那些尚未在当前池中参与 fork/join 计算的客户端设计的。这些方法的主要形式接受
 * {@code ForkJoinTask} 的实例，但重载形式也允许混合执行普通的 {@code
 * Runnable}- 或 {@code Callable}- 基础活动。然而，已经在池中执行的任务通常应使用表中列出的
 * 计算内形式，除非使用通常不会被 join 的异步事件风格任务，在这种情况下，选择哪种方法差异不大。
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>任务执行方法概览</caption>
 *  <tr>
 *    <td></td>
 *    <td ALIGN=CENTER> <b>从非 fork/join 客户端调用</b></td>
 *    <td ALIGN=CENTER> <b>从 fork/join 计算内部调用</b></td>
 *  </tr>
 *  <tr>
 *    <td> <b>安排异步执行</b></td>
 *    <td> {@link #execute(ForkJoinTask)}</td>
 *    <td> {@link ForkJoinTask#fork}</td>
 *  </tr>
 *  <tr>
 *    <td> <b>等待并获取结果</b></td>
 *    <td> {@link #invoke(ForkJoinTask)}</td>
 *    <td> {@link ForkJoinTask#invoke}</td>
 *  </tr>
 *  <tr>
 *    <td> <b>安排执行并获取 Future</b></td>
 *    <td> {@link #submit(ForkJoinTask)}</td>
 *    <td> {@link ForkJoinTask#fork} (ForkJoinTasks <em>是</em> Futures)</td>
 *  </tr>
 * </table>
 *
 * <p>默认情况下，公共池使用默认参数构建，但可以通过设置三个
 * {@linkplain System#getProperty 系统属性} 来控制这些参数：
 * <ul>
 * <li>{@code java.util.concurrent.ForkJoinPool.common.parallelism}
 * - 并行度级别，一个非负整数
 * <li>{@code java.util.concurrent.ForkJoinPool.common.threadFactory}
 * - {@link ForkJoinWorkerThreadFactory} 的类名
 * <li>{@code java.util.concurrent.ForkJoinPool.common.exceptionHandler}
 * - {@link UncaughtExceptionHandler} 的类名
 * </ul>
 * 如果存在 {@link SecurityManager} 且未指定工厂，则默认池使用一个提供
 * 没有启用任何 {@link Permissions} 的线程的工厂。
 * 使用系统类加载器来加载这些类。
 * 在建立这些设置时发生任何错误，将使用默认参数。
 * 通过将并行度属性设置为零和/或使用可能返回 {@code null} 的工厂，可以禁用或限制公共池中线程的使用。
 * 但是这样做可能会导致未 join 的任务永远不会被执行。
 *
 * <p><b>实现说明</b>：此实现将运行线程的最大数量限制为 32767。尝试创建具有大于最大数量的池将导致
 * {@code IllegalArgumentException}。
 *
 * <p>此实现仅在池已关闭或内部资源已耗尽时拒绝提交的任务（即，通过抛出
 * {@link RejectedExecutionException}）。
 *
 * @since 1.7
 * @author Doug Lea
 */
@sun.misc.Contended
public class ForkJoinPool extends AbstractExecutorService {

                /*
     * 实现概述
     *
     * 该类及其嵌套类提供了工作线程集的主要
     * 功能和控制：非FJ线程的提交进入提交队列。
     * 工作者从这些任务中获取任务，并通常将它们拆分为子任务
     * 这些子任务可能被其他工作者窃取。优先规则给予
     * 首要优先级是处理来自它们自己的队列的任务（LIFO
     * 或FIFO，取决于模式），然后是随机FIFO窃取
     * 其他队列中的任务。这个框架最初是为了
     * 使用工作窃取支持树形并行性。随着时间的推移，其
     * 可扩展性优势导致了扩展和
     * 更好地支持更多样化的使用场景。因为
     * 大多数内部方法和嵌套类是相互关联的，
     * 它们的主要理由和描述都在这里；
     * 个别方法和嵌套类只包含关于细节的简短
     * 注释。
     *
     * 工作队列
     * ==========
     *
     * 大多数操作都在工作窃取队列中进行（在嵌套
     * 类WorkQueue中）。这些是特殊形式的Deque，只支持
     * 四个端操作中的三个 -- push、pop和poll（即窃取），并且
     * 进一步限制为push和pop只能由拥有线程调用（或，如这里扩展的，加锁调用），而poll可以从
     * 其他线程调用。（如果你不熟悉这些，你可能
     * 想在继续之前阅读Herlihy和Shavit的书“多处理器编程的艺术”，第16章
     * 更详细地描述了这些。）主要的工作窃取队列
     * 设计大致类似于Chase和Lev在SPAA 2005的论文“动态循环工作窃取双端队列”
     * (http://research.sun.com/scalable/pubs/index.html) 和
     * Michael, Saraswat, 和Vechev在PPoPP 2009的论文“幂等工作窃取”
     * (http://portal.acm.org/citation.cfm?id=1504186) 中描述的设计。
     * 主要区别最终源于GC要求，即我们尽可能快地将已取走的槽位设为null，以保持尽可能小的
     * 脚印，即使在生成大量任务的程序中也是如此。为了实现这一点，我们将CAS
     * 用于pop与poll（窃取）的仲裁从索引（“base”和“top”）转移到了槽位本身。
     *
     * 添加任务的形式为经典的数组push(task)：
     *    q.array[q.top] = task; ++q.top;
     *
     * （实际代码需要检查数组是否为null和大小检查，
     * 正确地设置访问的内存屏障，并可能信号等待
     * 的工作者开始扫描 -- 请参见下文。）成功的pop和poll主要涉及
     * 槽位从非null到null的CAS。
     *
     * pop操作（始终由所有者执行）是：
     *   if ((base != top) and
     *        (顶部槽位的任务不为null) and
     *        (CAS槽位为null))
     *           top递减并返回任务；
     *
     * 而poll操作（通常由窃取者执行）是
     *    if ((base != top) and
     *        (底部槽位的任务不为null) and
     *        (base未改变) and
     *        (CAS槽位为null))
     *           base递增并返回任务；
     *
     * 由于我们依赖于引用的CAS，我们不需要在base或top上使用标签位。它们是简单的int，如任何循环数组队列中使用的（参见ArrayDeque）。索引的更新保证了top == base意味着队列为空，但其他情况下可能会误判队列非空，当push、pop或poll未完全提交时。（方法isEmpty()检查部分完成的最后一个元素移除的情况。）因此，单独考虑，poll操作不是无等待的。一个窃取者不能成功继续，直到另一个正在进行的操作（或，如果之前为空，则push）完成。然而，总体上，我们确保至少有概率性的非阻塞性。如果尝试窃取失败，窃取者总是选择一个不同的随机目标尝试下一个。因此，为了使一个窃取者能够继续，任何正在进行的poll或任何空队列上的新push完成就足够了。（这就是为什么我们通常使用方法pollAt及其变体，这些方法在明显的base索引处尝试一次，否则考虑其他操作，而不是使用方法poll，后者会重试。）
     *
     * 这种方法还支持用户模式，其中本地任务处理是FIFO，而不是LIFO顺序，只需使用poll而不是pop。这在任务从不合并的消息传递框架中可能有用。然而，无论哪种模式都不考虑亲和性、负载、缓存局部性等因素，因此很少提供给定机器上的最佳性能，但通过平均这些因素可移植地提供良好的吞吐量。此外，即使我们尝试使用这些信息，我们通常也没有利用它的基础。例如，一些任务集受益于缓存亲和性，但其他任务集则因缓存污染效应而受损。此外，即使需要扫描，长期来看，随机选择通常比定向选择策略提供更好的吞吐量，因此在适用时使用廉价的高质量随机化。各种Marsaglia XorShifts（一些具有不同的移位常数）在使用点内联。
     *
     * 工作队列也以类似的方式用于提交给池的任务。我们不能将这些任务与工作者使用的队列混合。相反，我们随机将提交队列与提交线程关联，使用一种哈希形式。ThreadLocalRandom探针值用作选择现有队列的哈希码，如果与其他提交者发生冲突，可能会随机重新定位。本质上，提交者的行为类似于工作者，但它们仅限于执行它们提交的本地任务（或在CountedCompleters的情况下，具有相同根任务的其他任务）。共享模式下的任务插入需要加锁（主要是为了保护在调整大小时的情况），但我们只使用一个简单的自旋锁（使用字段qlock），因为遇到忙碌队列的提交者会移动到尝试或创建其他队列 -- 它们仅在创建和注册新队列时阻塞。此外，“qlock”在关闭时饱和到一个不可解锁的值（-1）。解锁在成功情况下仍然可以并且确实通过更便宜的有序写入“qlock”来执行，但在不成功的情况下使用CAS。
     *
     * 管理
     * ==========
     *
     * 工作窃取的主要吞吐量优势源于分散控制 -- 工作者主要从自己或彼此获取任务，速度可以超过每秒十亿次。池本身创建、激活（启用扫描和运行任务）、停用、阻塞和终止线程，所有这些都具有最少的中心信息。我们只能全局跟踪或维护少数几个属性，因此我们将它们打包到少数几个变量中，通常在不阻塞或加锁的情况下保持原子性。几乎所有本质上原子的控制状态都保存在两个易失性变量中，这些变量大多数时候被读取（而不是写入）作为状态和一致性检查。（此外，字段“config”保存不变的配置状态。）
     *
     * 字段“ctl”包含64位信息，用于原子地决定添加、停用、入队（到事件队列）、出队和/或重新激活工作者。为了实现这种打包，我们将最大并行度限制为(1<<15)-1（远远超过正常操作范围），以便id、计数及其否定（用于阈值）可以适应16位子字段。
     *
     * 字段“runState”保存可锁定的状态位（STARTED, STOP等），也保护对workQueues数组的更新。当用作锁时，它通常只保持几个指令（唯一的例外是一次性数组初始化和不常见的调整大小），因此几乎总是可以在短暂自旋后可用。但为了更加谨慎，自旋后，方法awaitRunStateLock（仅在初始CAS失败时调用）使用内置监视器的等待/通知机制在需要时阻塞。对于高度竞争的锁，这将是一个糟糕的主意，但大多数池在自旋限制后运行时锁从未竞争，因此这作为一个更保守的替代方案工作得很好。因为我们没有其他内部对象可以用作监视器，所以当可用时使用“stealCounter”（一个AtomicLong）（它也必须惰性初始化；参见externalSubmit）。
     *
     * “runState”与“ctl”的使用仅在一个案例中交互：
     * 决定添加一个工作线程（参见tryAddWorker），在这种情况下，ctl CAS在持有锁时执行。
     *
     * 记录工作队列。工作队列记录在
     * “workQueues”数组中。数组在首次使用时创建（参见
     * externalSubmit），并在必要时扩展。记录新工作者和取消记录终止工作者时对数组的更新由runState锁保护，但数组在其他情况下是并发可读的，并直接访问。我们还确保对数组引用本身的读取永远不会太陈旧。为了简化基于索引的操作，数组大小始终是2的幂，所有读取者必须容忍null槽位。工作者队列位于奇数索引。共享（提交）队列位于偶数索引，最多64个槽位，以限制即使数组需要扩展以添加更多工作者时的增长。将它们以这种方式分组在一起简化并加速了任务扫描。
     *
     * 所有工作线程的创建都是按需触发的，由任务提交、替换终止的工作者和/或补偿阻塞的工作者触发。然而，所有其他支持代码都设置为与其他策略一起工作。为了确保我们不会持有会阻止GC的工作者引用，所有对workQueues的访问都是通过workQueues数组中的索引进行的（这是这里一些混乱代码构造的一个来源）。本质上，workQueues数组充当弱引用机制。因此，例如，ctl的堆顶子字段存储索引，而不是引用。
     *
     * 队列空闲工作者。与HPC工作窃取框架不同，我们不能让工作者在找不到任务时无限期地扫描，也不能在没有任务可用时启动/恢复工作者。另一方面，我们必须在提交新任务或生成任务时迅速促使它们行动。在许多使用情况下，激活工作者的加速时间是整体性能的主要限制因素，这在程序启动时由于JIT编译和分配而变得更加严重。因此，我们尽可能简化这一点。
     *
     * “ctl”字段原子地维护活动和总工作者计数以及一个队列，用于放置等待线程，以便可以找到它们进行信号。活动计数还充当静默指示器，因此当工作者认为没有更多任务可执行时会递减。队列实际上是Treiber堆栈的形式。堆栈非常适合按最近使用顺序激活线程。这提高了性能和局部性，超过了容易发生竞争和无法释放堆栈顶部以外工作者的缺点。当工作者找不到工作时，我们会在将它们推入空闲工作者堆栈（由ctl的低32位子字段表示）后将它们停放/唤醒。堆栈顶部状态保存工作者的“scanState”字段：其索引和状态，加上一个版本计数器，除了计数子字段（也用作版本戳）外，还提供对Treiber堆栈ABA效应的保护。
     *
     * 字段scanState由工作者和池使用来管理和跟踪工作者是否处于INACTIVE（可能正在等待信号）或SCANNING（既不持有也不忙于运行任务）。当工作者被停用时，其scanState字段被设置，并被阻止执行任务，即使它必须扫描一次以避免排队竞争。注意，scanState更新滞后于队列CAS释放，因此使用时需要小心。当排队时，scanState的低16位必须保存其池索引。因此我们在初始化时（参见registerWorker）将索引放在那里，并在必要时保持或恢复它。
     *
     * 内存排序。参见Le, Pop, Cohen, 和 Nardelli在PPoPP 2013的论文“Correct and Efficient Work-Stealing for Weak Memory Models” (http://www.di.ens.fr/~zappa/readings/ppopp13.pdf) 对类似于此处使用的窃取算法的内存排序要求的分析。我们通常需要比最小排序更强的排序，因为我们必须有时信号工作者，需要Dekker风格的全内存屏障以避免信号丢失。在不使用昂贵的过度内存屏障的情况下安排足够的排序需要在支持表达访问约束的手段之间进行权衡。最核心的操作，从队列中取任务和更新ctl状态，需要全内存屏障CAS。数组槽位使用Unsafe提供的易失性仿真读取。其他线程访问WorkQueue的base、top和array时需要对这些字段中的第一个进行易失性加载。我们使用声明“base”索引为易失性的约定，并在读取其他字段之前始终读取它。所有者线程必须确保有序更新，因此写入使用有序内联，除非它们可以搭其他写入的便车。类似的约定和理由适用于其他WorkQueue字段（如“currentSteal”），这些字段仅由所有者写入但被其他线程观察。
     *
     * 创建工作者。为了创建一个工作者，我们预先递增总计数（作为预留），并通过其工厂尝试构造一个ForkJoinWorkerThread。在构造后，新线程调用registerWorker，在那里它构造一个WorkQueue并被分配一个workQueues数组中的索引（必要时扩展数组）。然后启动线程。在这些步骤中的任何异常或工厂返回null时，deregisterWorker调整计数并相应记录。如果返回null，池将继续运行，但工作者数量少于目标数量。如果发生异常，异常将传播，通常到某些外部调用者。工作者索引分配避免了如果从workQueues数组的前端开始顺序打包时在扫描中出现的偏差。我们将数组视为一个简单的2的幂哈希表，必要时进行扩展。seedIndex递增确保在需要调整大小或工作者注销并替换之前没有冲突，之后保持低概率的冲突。我们不能使用ThreadLocalRandom.getProbe()来实现类似的目的，因为线程尚未启动，但为现有外部线程创建提交队列时使用。
     *
     * 停用和等待。排队遇到几个内在的竞争；最明显的是，一个生成任务的线程可能会错过看到（并信号）另一个线程，该线程放弃了寻找工作但尚未进入等待队列。当工作者无法窃取任务时，它会停用并入队。通常，缺乏任务是由于GC或OS调度造成的暂时现象。为了减少错误警报的停用，扫描器在扫描期间计算队列状态的校验和。（这里和别处使用的稳定性检查是概率性的快照技术变体 -- 参见Herlihy & Shavit。）只有在校验和在扫描中稳定时，工作者才会放弃并尝试停用。此外，为了避免错过信号，它们在成功入队后重复此扫描过程，直到再次稳定。在这种状态下，工作者不能取/运行它看到的任务，直到它从队列中释放，因此工作者最终尝试释放自己或任何继任者（参见tryRelease）。否则，在空扫描后，停用的工作者使用自适应本地自旋构造（参见awaitWork）然后阻塞（通过park）。注意围绕停放和其他阻塞的Thread.interrupts的不寻常约定：因为中断仅用于提醒线程检查终止，而这无论如何在阻塞时都会检查，我们在任何调用park之前清除状态（使用Thread.interrupted），以便park不会因状态通过用户代码中的其他无关调用中断而立即返回。
     *
     * 信号和激活。只有当有至少一个任务它们可能能够找到并执行时，才会创建或激活工作者。当向先前（可能）为空的队列push（无论是由工作者还是外部提交）时，如果工作者空闲，则向其发送信号，或如果存在的工作者少于给定的并行度，则创建新的工作者。这些主要信号通过其他线程从队列中移除任务并注意到队列中还有其他任务时发出的其他信号得到加强。在大多数平台上，信号（unpark）开销时间明显较长，从信号线程到它实际取得进展的时间可能非常长，因此尽可能将这些延迟从关键路径中卸载是值得的。此外，因为不活跃的工作者通常在重新扫描或自旋而不是阻塞，我们设置和清除WorkQueues的“parker”字段以减少不必要的unpark调用。（这需要二次检查以避免错过信号。）
     *
     * 裁剪工作者。为了在长时间不使用后释放资源，当池处于静默状态时开始等待的工作者将在池静默IDLE_TIMEOUT后超时并终止（参见awaitWork），随着线程数量的减少增加超时时间，最终移除所有工作者。此外，当存在超过两个空闲线程时，多余的线程在下一个静默点立即终止。（通过两个空闲线程的填充避免了滞后。）
     *
     * 关闭和终止。调用shutdownNow会调用tryTerminate以原子地设置runState位。调用线程以及之后终止的每个其他工作者通过设置它们的（qlock）状态、取消它们未处理的任务并唤醒它们来帮助终止，直到稳定（但循环次数以工作者数量为界）。调用非紧急shutdown()会先检查是否应开始终止。这主要依赖于“ctl”的活动计数位保持一致 -- 当池静默时，tryTerminate会从awaitWork调用。然而，外部提交者不参与这种一致性。因此，tryTerminate遍历队列（直到稳定）以确保没有正在飞行的提交和即将处理它们的工作者，然后触发终止的“STOP”阶段。（注意：如果在启用关闭时调用helpQuiescePool，存在内在冲突。两者都在等待静默，但tryTerminate偏向于在helpQuiescePool完成之前不触发。）
     *
     *
     * 合并任务
     * =============
     *
     * 当一个工作者等待加入被另一个工作者窃取（或始终持有的）任务时，可以采取几种行动。因为我们正在将许多任务多路复用到一个工作者池中，我们不能让它们阻塞（如Thread.join）。我们也不能简单地重新分配加入者的运行时堆栈并稍后替换它，这将是一种“延续”，即使可能也不一定是好主意，因为我们可能需要一个未阻塞的任务及其延续来进展。相反，我们结合了两种策略：
     *
     *   帮助：安排加入者执行如果窃取没有发生它将运行的任务。
     *
     *   补偿：除非已经有足够的活跃线程，方法tryCompensate()可能会创建或重新激活一个备用线程，以补偿阻塞的加入者，直到它们解除阻塞。
     *
     * 第三种形式（在tryRemoveAndExec中实现）相当于帮助一个假设的补偿者：如果我们能够轻易判断一个补偿者的可能行动是窃取并执行正在加入的任务，加入线程可以直接这样做，而不需要补偿线程（尽管以更大的运行时堆栈为代价，但通常值得权衡）。
     *
     * ManagedBlocker扩展API不能使用帮助，因此在方法awaitBlocker中仅依赖补偿。
     *
     * helpStealer算法涉及一种“线性帮助”的形式。每个工作者记录（在字段currentSteal中）它最近从其他工作者（或提交）窃取的最接近的任务。它还记录（在字段currentJoin中）它当前正在积极加入的任务。方法helpStealer使用这些标记尝试找到一个工作者来帮助（即，窃取并执行一个任务）以加快正在积极加入的任务的完成。因此，加入者执行一个任务，该任务如果被加入的任务没有被窃取，将位于它自己的本地deque上。这是一种保守的变体，描述在Wagner & Calder的“Leapfrogging: a portable technique for implementing efficient futures” SIGPLAN Notices, 1993 (http://portal.acm.org/citation.cfm?id=155354) 中的方法。它不同之处在于：（1）我们只在窃取时维护跨工作者的依赖链接，而不是使用每个任务的簿记。这有时需要线性扫描workQueues数组来定位窃取者，但通常不需要，因为窃取者留下了（可能变得陈旧/错误的）定位它们的提示。这将成本隔离到需要时，而不是增加每个任务的开销。（2）它是“浅层的”，忽略了嵌套和潜在的循环相互窃取。（3）它有意是竞争的：字段currentJoin仅在积极加入时更新，这意味着我们在长时间任务、GC停顿等情况下会错过链中的链接（这没关系，因为在这种情况下阻塞通常是个好主意）。（4）我们使用校验和限制寻找工作的尝试次数，并在必要时暂停工作者并用另一个替换。
     *
     * CountedCompleters的帮助操作不需要跟踪currentJoins：方法helpComplete获取并执行与正在等待的任务具有相同根的任何任务（优先考虑本地pop而不是非本地poll）。然而，这仍然涉及遍历completer链，因此使用CountedCompleters时没有显式join的效率较低。
     *
     * 补偿不旨在在任何给定时间保持恰好目标并行度数量的未阻塞线程运行。一些早期版本的此类在任何阻塞join时都立即进行补偿。然而，实际上，绝大多数阻塞都是GC和其他JVM或OS活动的暂时副产品，这些活动因替换而变得更糟。目前，只有在验证所有据称活跃的线程都在处理任务后，通过检查字段WorkQueue.scanState，才会尝试补偿，这消除了大多数误报。此外，在最常见的情况下，补偿被跳过（容忍更少的线程）：当一个队列为空（因此没有延续任务）的工作者在join上阻塞，而仍然有足够的线程确保活跃性时。
     *
     * 补偿机制可能受到限制。对于commonPool（参见commonMaxSpares），限制更好地使JVM能够在资源耗尽前应对编程错误和滥用。在其他情况下，用户可以提供限制线程创建的工厂。这种池（如所有其他池）中的限制效果是不精确的。当线程注销时，总工作者计数递减，而不是当线程退出且资源被JVM和OS回收时。因此，同时活跃的线程数量可能会暂时超过限制。
     *
     * 公共池
     * ===========
     *
     * 静态公共池在静态初始化后始终存在。由于它（或任何其他创建的池）可能永远不会被使用，我们最小化初始构造开销和占用空间，仅设置大约十几个字段，没有嵌套分配。大多数引导在方法externalSubmit中进行，这是第一次向池提交时。
     *
     * 当外部线程向公共池提交时，它们可以在join时执行子任务处理（参见externalHelpComplete及相关方法）。这种调用者帮助策略使将公共池并行度级别设置为比可用核心总数少一个（或更多），甚至为零以进行纯调用者运行变得合理。我们不需要记录外部提交是否为公共池 -- 如果不是，外部帮助方法将快速返回。这些提交者否则将被阻塞等待完成，因此额外的努力（带有大量任务状态检查）在不适用的情况下相当于一种有限的自旋等待，然后在ForkJoinTask.join中阻塞。
     *
     * 作为更合适的默认设置，在管理环境中，除非被系统属性覆盖，当存在SecurityManager时，我们使用InnocuousForkJoinWorkerThread子类的工作者。这些工作者没有任何权限设置，不属于任何用户定义的ThreadGroup，并在执行任何顶级任务后擦除所有ThreadLocals（参见WorkQueue.runTask）。相关机制（主要在ForkJoinWorkerThread中）可能是JVM依赖的，必须访问特定的Thread类字段以实现这种效果。
     *
     * 风格注释
     * ===========
     *
     * 内存排序主要依赖于携带进一步责任的Unsafe内联，即显式执行JVM通常隐式执行的null检查和边界检查。这可能很笨拙和丑陋，但也反映了在非常竞争的代码中出现的非常少的不变量的不寻常情况下控制结果的需要。因此，这些显式检查无论如何都会以某种形式存在。所有字段在使用前都读取到局部变量中，如果是引用则进行null检查。这通常以“C”风格完成，即在方法或块的头部列出声明，并在首次遇到时进行内联赋值。数组边界检查通常通过与array.length-1进行掩码来完成，这依赖于这些数组创建时长度为正的不变量，这本身也是偏执地检查的。几乎所有显式检查都会导致绕过/返回，而不是抛出异常，因为它们可能由于取消/撤销在关闭期间合法地出现。
     *
     * ForkJoinPool、ForkJoinWorkerThread和ForkJoinTask类之间存在大量表示级别的耦合。WorkQueue的字段维护由ForkJoinPool管理的数据结构，因此直接访问。尝试减少这一点没有意义，因为任何相关的未来表示变化都需要伴随算法变化。一些方法本质上会扩展，因为它们必须累积对保存在局部变量中的字段的一致性读取。还有一些其他编码怪癖（包括一些看起来不必要的空检查）有助于一些方法在解释（而不是编译）时表现合理。
     *
     * 本文件中声明的顺序（有少数例外）为：
     * (1) 静态实用函数
     * (2) 嵌套（静态）类
     * (3) 静态字段
     * (4) 字段，以及用于解包其中一些字段的常量
     * (5) 内部控制方法
     * (6) ForkJoinTask方法的回调和其他支持
     * (7) 导出方法
     * (8) 静态块，以最小依赖顺序初始化静态变量
     */

                // 静态工具

    /**
     * 如果存在安全管理器，确保调用者有权限修改线程。
     */
    private static void checkPermission() {
        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkPermission(modifyThreadPermission);
    }

    // 嵌套类

    /**
     * 创建新的 {@link ForkJoinWorkerThread} 的工厂。
     * 必须定义和使用 {@code ForkJoinWorkerThreadFactory}，以便为扩展基本功能或以不同上下文初始化线程的 {@code ForkJoinWorkerThread} 子类提供支持。
     */
    public static interface ForkJoinWorkerThreadFactory {
        /**
         * 返回在给定池中操作的新工作线程。
         *
         * @param pool 此线程工作的池
         * @return 新的工作线程
         * @throws NullPointerException 如果池为 null
         */
        public ForkJoinWorkerThread newThread(ForkJoinPool pool);
    }

    /**
     * 默认的 ForkJoinWorkerThreadFactory 实现；创建一个新的 ForkJoinWorkerThread。
     */
    static final class DefaultForkJoinWorkerThreadFactory
        implements ForkJoinWorkerThreadFactory {
        public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new ForkJoinWorkerThread(pool);
        }
    }

    /**
     * 公共池 ForkJoinWorkerThreadFactory 实现；创建一个新的 ForkJoinWorkerThread。
     */
    static final class CommonPoolForkJoinWorkerThreadFactory
            implements ForkJoinWorkerThreadFactory {
        public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new ForkJoinWorkerThread(pool, null);
        }
    }

    /**
     * 用于在 WorkQueue.tryRemoveAndExec 中从内部队列槽移除目标时替换目标的人工任务类。
     * 我们不需要代理实际执行任何操作，除了具有唯一的标识。
     */
    static final class EmptyTask extends ForkJoinTask<Void> {
        private static final long serialVersionUID = -7721805057305804111L;
        EmptyTask() { status = ForkJoinTask.NORMAL; } // 强制完成
        public final Void getRawResult() { return null; }
        public final void setRawResult(Void x) {}
        public final boolean exec() { return true; }
    }

    // 跨 ForkJoinPool 和 WorkQueue 共享的常量

    // 边界
    static final int SMASK        = 0xffff;        // short 位 == 最大索引
    static final int MAX_CAP      = 0x7fff;        // 最大 #workers - 1
    static final int EVENMASK     = 0xfffe;        // 偶数 short 位
    static final int SQMASK       = 0x007e;        // 最大 64 (偶数) 槽

    // WorkQueue.scanState 和 ctl sp 子字段的掩码和单位
    static final int SCANNING     = 1;             // 运行任务时为 false
    static final int INACTIVE     = 1 << 31;       // 必须为负数
    static final int SS_SEQ       = 1 << 16;       // 版本计数

    // ForkJoinPool.config 和 WorkQueue.config 的模式位
    static final int MODE_MASK    = 0xffff << 16;  // int 的上半部分
    static final int LIFO_QUEUE   = 0;
    static final int FIFO_QUEUE   = 1 << 16;
    static final int SHARED_QUEUE = 1 << 31;       // 必须为负数

    /**
     * 支持工作窃取以及外部任务提交的队列。请参阅上面的描述和算法。
     * 在大多数平台上，WorkQueues 实例及其数组的放置对性能非常敏感——我们绝对不希望多个 WorkQueue 实例或多个队列数组共享缓存行。@Contended 注解提醒 JVM 尝试保持实例之间的分离。
     */
    @sun.misc.Contended
    static final class WorkQueue {

        /**
         * 工作窃取队列数组在初始化时的容量。
         * 必须是 2 的幂；至少为 4，但应更大以减少或消除队列之间的缓存行共享。
         * 当前，它的值要大得多，部分是为了应对 JVM 通常将数组放置在共享 GC 记账（尤其是卡标记）的位置，使得每次写入访问都会遇到严重的内存争用。
         */
        static final int INITIAL_QUEUE_CAPACITY = 1 << 13;

        /**
         * 队列数组的最大大小。必须是 2 的幂，小于或等于 1 << (31 - 数组条目宽度)，以确保索引计算不会溢出，但定义为略小于此值，以帮助用户在系统饱和之前捕获失控程序。
         */
        static final int MAXIMUM_QUEUE_CAPACITY = 1 << 26; // 64M

        // 实例字段
        volatile int scanState;    // 版本化，<0: 不活跃；奇数: 扫描
        int stackPred;             // 池堆栈（ctl）前驱
        int nsteals;               // 窃取次数
        int hint;                  // 随机化和窃取者索引提示
        int config;                // 池索引和模式
        volatile int qlock;        // 1: 锁定，< 0: 终止；否则为 0
        volatile int base;         // 下一个 poll 槽的索引
        int top;                   // 下一个 push 槽的索引
        ForkJoinTask<?>[] array;   // 元素（最初未分配）
        final ForkJoinPool pool;   // 包含的池（可能是 null）
        final ForkJoinWorkerThread owner; // 拥有线程或共享时为 null
        volatile Thread parker;    // 在调用 park 期间 == owner；否则为 null
        volatile ForkJoinTask<?> currentJoin;  // 在 awaitJoin 中连接的任务
        volatile ForkJoinTask<?> currentSteal; // 主要用于 helpStealer

        WorkQueue(ForkJoinPool pool, ForkJoinWorkerThread owner) {
            this.pool = pool;
            this.owner = owner;
            // 将索引放置在数组的中心（该数组尚未分配）
            base = top = INITIAL_QUEUE_CAPACITY >>> 1;
        }


                    /**
         * 返回一个可导出的索引（由 ForkJoinWorkerThread 使用）。
         */
        final int getPoolIndex() {
            return (config & 0xffff) >>> 1; // 忽略奇偶标签位
        }

        /**
         * 返回队列中任务的大致数量。
         */
        final int queueSize() {
            int n = base - top;       // 非所有者调用者必须先读取 base
            return (n >= 0) ? 0 : -n; // 忽略瞬时负值
        }

        /**
         * 提供比 queueSize 更准确的估计，检查队列是否
         * 有任何任务，通过检查几乎为空的队列是否至少有一个未声明的任务。
         */
        final boolean isEmpty() {
            ForkJoinTask<?>[] a; int n, m, s;
            return ((n = base - (s = top)) >= 0 ||
                    (n == -1 &&           // 可能有一个任务
                     ((a = array) == null || (m = a.length - 1) < 0 ||
                      U.getObject
                      (a, (long)((m & (s - 1)) << ASHIFT) + ABASE) == null)));
        }

        /**
         * 推送一个任务。仅由所有者在非共享队列中调用。 (共享队列版本嵌入在方法 externalPush 中。)
         *
         * @param task 任务。调用者必须确保非空。
         * @throws RejectedExecutionException 如果数组无法调整大小
         */
        final void push(ForkJoinTask<?> task) {
            ForkJoinTask<?>[] a; ForkJoinPool p;
            int b = base, s = top, n;
            if ((a = array) != null) {    // 忽略如果队列被移除
                int m = a.length - 1;     // 为任务可见性进行围栏写入
                U.putOrderedObject(a, ((m & s) << ASHIFT) + ABASE, task);
                U.putOrderedInt(this, QTOP, s + 1);
                if ((n = s - b) <= 1) {
                    if ((p = pool) != null)
                        p.signalWork(p.workQueues, this);
                }
                else if (n >= m)
                    growArray();
            }
        }

        /**
         * 初始化或加倍数组的容量。由所有者调用或持有锁时调用 -- 在调整大小期间，允许 base 移动，但不允许 top 移动。
         */
        final ForkJoinTask<?>[] growArray() {
            ForkJoinTask<?>[] oldA = array;
            int size = oldA != null ? oldA.length << 1 : INITIAL_QUEUE_CAPACITY;
            if (size > MAXIMUM_QUEUE_CAPACITY)
                throw new RejectedExecutionException("队列容量超出");
            int oldMask, t, b;
            ForkJoinTask<?>[] a = array = new ForkJoinTask<?>[size];
            if (oldA != null && (oldMask = oldA.length - 1) >= 0 &&
                (t = top) - (b = base) > 0) {
                int mask = size - 1;
                do { // 模拟从旧数组中弹出，推送到新数组
                    ForkJoinTask<?> x;
                    int oldj = ((b & oldMask) << ASHIFT) + ABASE;
                    int j    = ((b &    mask) << ASHIFT) + ABASE;
                    x = (ForkJoinTask<?>)U.getObjectVolatile(oldA, oldj);
                    if (x != null &&
                        U.compareAndSwapObject(oldA, oldj, x, null))
                        U.putObjectVolatile(a, j, x);
                } while (++b != t);
            }
            return a;
        }

        /**
         * 以 LIFO 顺序取下一个任务（如果存在）。仅由所有者在非共享队列中调用。
         */
        final ForkJoinTask<?> pop() {
            ForkJoinTask<?>[] a; ForkJoinTask<?> t; int m;
            if ((a = array) != null && (m = a.length - 1) >= 0) {
                for (int s; (s = top - 1) - base >= 0;) {
                    long j = ((m & s) << ASHIFT) + ABASE;
                    if ((t = (ForkJoinTask<?>)U.getObject(a, j)) == null)
                        break;
                    if (U.compareAndSwapObject(a, j, t, null)) {
                        U.putOrderedInt(this, QTOP, s);
                        return t;
                    }
                }
            }
            return null;
        }

        /**
         * 如果 b 是队列的基点且任务可以在无竞争的情况下声明，则以 FIFO 顺序取一个任务。ForkJoinPool 方法 scan 和 helpStealer 中有专门的版本。
         */
        final ForkJoinTask<?> pollAt(int b) {
            ForkJoinTask<?> t; ForkJoinTask<?>[] a;
            if ((a = array) != null) {
                int j = (((a.length - 1) & b) << ASHIFT) + ABASE;
                if ((t = (ForkJoinTask<?>)U.getObjectVolatile(a, j)) != null &&
                    base == b && U.compareAndSwapObject(a, j, t, null)) {
                    base = b + 1;
                    return t;
                }
            }
            return null;
        }

        /**
         * 以 FIFO 顺序取下一个任务（如果存在）。
         */
        final ForkJoinTask<?> poll() {
            ForkJoinTask<?>[] a; int b; ForkJoinTask<?> t;
            while ((b = base) - top < 0 && (a = array) != null) {
                int j = (((a.length - 1) & b) << ASHIFT) + ABASE;
                t = (ForkJoinTask<?>)U.getObjectVolatile(a, j);
                if (base == b) {
                    if (t != null) {
                        if (U.compareAndSwapObject(a, j, t, null)) {
                            base = b + 1;
                            return t;
                        }
                    }
                    else if (b + 1 == top) // 现在为空
                        break;
                }
            }
            return null;
        }

        /**
         * 以 mode 指定的顺序取下一个任务（如果存在）。
         */
        final ForkJoinTask<?> nextLocalTask() {
            return (config & FIFO_QUEUE) == 0 ? pop() : poll();
        }

        /**
         * 以 mode 指定的顺序返回下一个任务（如果存在）。
         */
        final ForkJoinTask<?> peek() {
            ForkJoinTask<?>[] a = array; int m;
            if (a == null || (m = a.length - 1) < 0)
                return null;
            int i = (config & FIFO_QUEUE) == 0 ? top - 1 : base;
            int j = ((i & m) << ASHIFT) + ABASE;
            return (ForkJoinTask<?>)U.getObjectVolatile(a, j);
        }


                    /**
         * 仅当给定任务位于当前顶部时，将其弹出。
         * （共享版本仅通过 FJP.tryExternalUnpush 可用）
        */
        final boolean tryUnpush(ForkJoinTask<?> t) {
            ForkJoinTask<?>[] a; int s;
            if ((a = array) != null && (s = top) != base &&
                U.compareAndSwapObject
                (a, (((a.length - 1) & --s) << ASHIFT) + ABASE, t, null)) {
                U.putOrderedInt(this, QTOP, s);
                return true;
            }
            return false;
        }

        /**
         * 移除并取消所有已知任务，忽略任何异常。
         */
        final void cancelAll() {
            ForkJoinTask<?> t;
            if ((t = currentJoin) != null) {
                currentJoin = null;
                ForkJoinTask.cancelIgnoringExceptions(t);
            }
            if ((t = currentSteal) != null) {
                currentSteal = null;
                ForkJoinTask.cancelIgnoringExceptions(t);
            }
            while ((t = poll()) != null)
                ForkJoinTask.cancelIgnoringExceptions(t);
        }

        // 专用执行方法

        /**
         * 轮询并运行任务，直到队列为空。
         */
        final void pollAndExecAll() {
            for (ForkJoinTask<?> t; (t = poll()) != null;)
                t.doExec();
        }

        /**
         * 移除并执行所有本地任务。如果使用 LIFO，调用 pollAndExecAll。否则实现一个专用的弹出循环，直到队列为空。
         */
        final void execLocalTasks() {
            int b = base, m, s;
            ForkJoinTask<?>[] a = array;
            if (b - (s = top - 1) <= 0 && a != null &&
                (m = a.length - 1) >= 0) {
                if ((config & FIFO_QUEUE) == 0) {
                    for (ForkJoinTask<?> t;;) {
                        if ((t = (ForkJoinTask<?>)U.getAndSetObject
                             (a, ((m & s) << ASHIFT) + ABASE, null)) == null)
                            break;
                        U.putOrderedInt(this, QTOP, s);
                        t.doExec();
                        if (base - (s = top - 1) > 0)
                            break;
                    }
                }
                else
                    pollAndExecAll();
            }
        }

        /**
         * 执行给定任务和任何剩余的本地任务。
         */
        final void runTask(ForkJoinTask<?> task) {
            if (task != null) {
                scanState &= ~SCANNING; // 标记为忙碌
                (currentSteal = task).doExec();
                U.putOrderedObject(this, QCURRENTSTEAL, null); // 释放以供垃圾回收
                execLocalTasks();
                ForkJoinWorkerThread thread = owner;
                if (++nsteals < 0)      // 在溢出时收集
                    transferStealCount(pool);
                scanState |= SCANNING;
                if (thread != null)
                    thread.afterTopLevelExec();
            }
        }

        /**
         * 如果存在，将窃取计数添加到池的 stealCounter（如果存在），并重置。
         */
        final void transferStealCount(ForkJoinPool p) {
            AtomicLong sc;
            if (p != null && (sc = p.stealCounter) != null) {
                int s = nsteals;
                nsteals = 0;            // 如果为负数，修正溢出
                sc.getAndAdd((long)(s < 0 ? Integer.MAX_VALUE : s));
            }
        }

        /**
         * 如果存在，从队列中移除并执行给定任务，或任何其他已取消的任务。仅由 awaitJoin 使用。
         *
         * @return 如果队列为空且任务未完成，则返回 true
         */
        final boolean tryRemoveAndExec(ForkJoinTask<?> task) {
            ForkJoinTask<?>[] a; int m, s, b, n;
            if ((a = array) != null && (m = a.length - 1) >= 0 &&
                task != null) {
                while ((n = (s = top) - (b = base)) > 0) {
                    for (ForkJoinTask<?> t;;) {      // 从 s 到 b 遍历
                        long j = ((--s & m) << ASHIFT) + ABASE;
                        if ((t = (ForkJoinTask<?>)U.getObject(a, j)) == null)
                            return s + 1 == top;     // 比预期的短
                        else if (t == task) {
                            boolean removed = false;
                            if (s + 1 == top) {      // 弹出
                                if (U.compareAndSwapObject(a, j, task, null)) {
                                    U.putOrderedInt(this, QTOP, s);
                                    removed = true;
                                }
                            }
                            else if (base == b)      // 用代理替换
                                removed = U.compareAndSwapObject(
                                    a, j, task, new EmptyTask());
                            if (removed)
                                task.doExec();
                            break;
                        }
                        else if (t.status < 0 && s + 1 == top) {
                            if (U.compareAndSwapObject(a, j, t, null))
                                U.putOrderedInt(this, QTOP, s);
                            break;                  // 已取消
                        }
                        if (--n == 0)
                            return false;
                    }
                    if (task.status < 0)
                        return false;
                }
            }
            return true;
        }

        /**
         * 如果任务在给定任务的相同 CC 计算中，无论是共享模式还是所有者模式，都将其弹出。仅由 helpComplete 使用。
         */
        final CountedCompleter<?> popCC(CountedCompleter<?> task, int mode) {
            int s; ForkJoinTask<?>[] a; Object o;
            if (base - (s = top) < 0 && (a = array) != null) {
                long j = (((a.length - 1) & (s - 1)) << ASHIFT) + ABASE;
                if ((o = U.getObjectVolatile(a, j)) != null &&
                    (o instanceof CountedCompleter)) {
                    CountedCompleter<?> t = (CountedCompleter<?>)o;
                    for (CountedCompleter<?> r = t;;) {
                        if (r == task) {
                            if (mode < 0) { // 必须锁定
                                if (U.compareAndSwapInt(this, QLOCK, 0, 1)) {
                                    if (top == s && array == a &&
                                        U.compareAndSwapObject(a, j, t, null)) {
                                        U.putOrderedInt(this, QTOP, s - 1);
                                        U.putOrderedInt(this, QLOCK, 0);
                                        return t;
                                    }
                                    U.compareAndSwapInt(this, QLOCK, 1, 0);
                                }
                            }
                            else if (U.compareAndSwapObject(a, j, t, null)) {
                                U.putOrderedInt(this, QTOP, s - 1);
                                return t;
                            }
                            break;
                        }
                        else if ((r = r.completer) == null) // 尝试父任务
                            break;
                    }
                }
            }
            return null;
        }


                    /**
         * 在给定任务的同一CC计算中窃取并运行一个任务，如果存在并且可以无竞争地获取。
         * 否则返回一个校验和/控制值，供方法helpComplete使用。
         *
         * @return 如果成功返回1，如果可重试（被其他窃取者夺走）返回2，如果非空但未找到匹配任务返回-1，
         * 否则返回基础索引，强制为负数。
         */
        final int pollAndExecCC(CountedCompleter<?> task) {
            int b, h; ForkJoinTask<?>[] a; Object o;
            if ((b = base) - top >= 0 || (a = array) == null)
                h = b | Integer.MIN_VALUE;  // 用于重新轮询时检测移动
            else {
                long j = (((a.length - 1) & b) << ASHIFT) + ABASE;
                if ((o = U.getObjectVolatile(a, j)) == null)
                    h = 2;                  // 可重试
                else if (!(o instanceof CountedCompleter))
                    h = -1;                 // 不匹配
                else {
                    CountedCompleter<?> t = (CountedCompleter<?>)o;
                    for (CountedCompleter<?> r = t;;) {
                        if (r == task) {
                            if (base == b &&
                                U.compareAndSwapObject(a, j, t, null)) {
                                base = b + 1;
                                t.doExec();
                                h = 1;      // 成功
                            }
                            else
                                h = 2;      // CAS失败
                            break;
                        }
                        else if ((r = r.completer) == null) {
                            h = -1;         // 不匹配
                            break;
                        }
                    }
                }
            }
            return h;
        }

        /**
         * 如果被拥有且已知未被阻塞，则返回true。
         */
        final boolean isApparentlyUnblocked() {
            Thread wt; Thread.State s;
            return (scanState >= 0 &&
                    (wt = owner) != null &&
                    (s = wt.getState()) != Thread.State.BLOCKED &&
                    s != Thread.State.WAITING &&
                    s != Thread.State.TIMED_WAITING);
        }

        // Unsafe机制。注意其中一些（并且必须）与FJP中的相同
        private static final sun.misc.Unsafe U;
        private static final int  ABASE;
        private static final int  ASHIFT;
        private static final long QTOP;
        private static final long QLOCK;
        private static final long QCURRENTSTEAL;
        static {
            try {
                U = sun.misc.Unsafe.getUnsafe();
                Class<?> wk = WorkQueue.class;
                Class<?> ak = ForkJoinTask[].class;
                QTOP = U.objectFieldOffset
                    (wk.getDeclaredField("top"));
                QLOCK = U.objectFieldOffset
                    (wk.getDeclaredField("qlock"));
                QCURRENTSTEAL = U.objectFieldOffset
                    (wk.getDeclaredField("currentSteal"));
                ABASE = U.arrayBaseOffset(ak);
                int scale = U.arrayIndexScale(ak);
                if ((scale & (scale - 1)) != 0)
                    throw new Error("数据类型的比例不是2的幂");
                ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    // 静态字段（在下面的静态初始化器中初始化）

    /**
     * 创建一个新的ForkJoinWorkerThread。除非在ForkJoinPool构造函数中覆盖，否则使用此工厂。
     */
    public static final ForkJoinWorkerThreadFactory
        defaultForkJoinWorkerThreadFactory;

    /**
     * 调用可能启动或终止线程的方法所需的权限。
     */
    private static final RuntimePermission modifyThreadPermission;

    /**
     * 公共（静态）池。除非静态构造异常，否则对于公共使用非空，但内部使用时会进行空检查，
     * 以避免潜在的初始化循环，并简化生成的代码。
     */
    static final ForkJoinPool common;

    /**
     * 公共池并行度。为了在禁用公共池线程时简化使用和管理，允许底层的common.parallelism字段为零，
     * 但在这种情况下，仍报告并行度为1，以反映结果的调用者运行机制。
     */
    static final int commonParallelism;

    /**
     * 在tryCompensate中构建备用线程的限制。
     */
    private static int commonMaxSpares;

    /**
     * 用于创建workerNamePrefix的序列号。
     */
    private static int poolNumberSequence;

    /**
     * 返回下一个序列号。我们不期望这会竞争，因此使用简单的内置同步。
     */
    private static final synchronized int nextPoolId() {
        return ++poolNumberSequence;
    }

    // 静态配置常量

    /**
     * 触发静止状态的线程等待新工作的初始超时值（以纳秒为单位）。超时后，
     * 线程将尝试减少工作线程的数量。该值应足够大，以避免在大多数瞬时停滞（长时间GC等）期间过度积极地减少。
     */
    private static final long IDLE_TIMEOUT = 2000L * 1000L * 1000L; // 2秒

    /**
     * 空闲超时的容差，以应对计时器下溢。
     */
    private static final long TIMEOUT_SLOP = 20L * 1000L * 1000L;  // 20毫秒

    /**
     * 在静态初始化期间commonMaxSpares的初始值。该值远超正常需求，但远低于MAX_CAP和典型
     * 操作系统线程限制，因此允许JVM在资源耗尽之前捕获误用/滥用。
     */
    private static final int DEFAULT_COMMON_MAX_SPARES = 256;


                /**
     * 在阻塞之前自旋等待的次数。当前在 awaitRunStateLock 和 awaitWork 中使用的自旋是随机化的。
     * 当前设置为零以减少 CPU 使用。
     *
     * 如果大于零，SPINS 的值必须是 2 的幂，至少为 4。2048 的值会导致自旋占用典型上下文切换时间的一小部分。
     *
     * 如果/当 MWAIT 类似的内联函数可用时，它们可能允许更安静的自旋。
     */
    private static final int SPINS  = 0;

    /**
     * 生成器种子的增量。参见 ThreadLocal 类的解释。
     */
    private static final int SEED_INCREMENT = 0x9e3779b9;

    /*
     * 字段 ctl 中的位和掩码，包含 4 个 16 位子字段：
     * AC: 活跃运行的工作线程数减去目标并行度
     * TC: 总工作线程数减去目标并行度
     * SS: 顶部等待线程的版本计数和状态
     * ID: 等待者 Treiber 栈顶部的 poolIndex
     *
     * 当方便时，我们可以提取较低 32 位栈顶位（包括版本位）作为 sp=(int)ctl。通过目标并行度对计数进行偏移以及字段的位置使得可以通过字段的符号测试执行最常见的检查：当 ac 为负时，活跃工作线程不足；当 tc 为负时，总工作线程不足。当 sp 非零时，有等待的工作线程。为了处理可能为负的字段，我们在 "short" 和/或有符号移位之间使用类型转换以保持符号性。
     *
     * 由于它占据最高位，我们可以在从阻塞的 join 返回时使用 getAndAddLong of AC_UNIT 增加一个活跃计数，而不是使用 CAS。其他更新涉及多个子字段和掩码，需要 CAS。
     */

    // 较低和较高字掩码
    private static final long SP_MASK    = 0xffffffffL;
    private static final long UC_MASK    = ~SP_MASK;

    // 活跃计数
    private static final int  AC_SHIFT   = 48;
    private static final long AC_UNIT    = 0x0001L << AC_SHIFT;
    private static final long AC_MASK    = 0xffffL << AC_SHIFT;

    // 总计数
    private static final int  TC_SHIFT   = 32;
    private static final long TC_UNIT    = 0x0001L << TC_SHIFT;
    private static final long TC_MASK    = 0xffffL << TC_SHIFT;
    private static final long ADD_WORKER = 0x0001L << (TC_SHIFT + 15); // 符号

    // runState 位：SHUTDOWN 必须为负，其他为任意的 2 的幂
    private static final int  RSLOCK     = 1;
    private static final int  RSIGNAL    = 1 << 1;
    private static final int  STARTED    = 1 << 2;
    private static final int  STOP       = 1 << 29;
    private static final int  TERMINATED = 1 << 30;
    private static final int  SHUTDOWN   = 1 << 31;

    // 实例字段
    volatile long ctl;                   // 主池控制
    volatile int runState;               // 可锁定状态
    final int config;                    // 并行度，模式
    int indexSeed;                       // 生成工作线程索引
    volatile WorkQueue[] workQueues;     // 主注册表
    final ForkJoinWorkerThreadFactory factory;
    final UncaughtExceptionHandler ueh;  // 每个工作线程的 UEH
    final String workerNamePrefix;       // 创建工作线程名称字符串
    volatile AtomicLong stealCounter;    // 也用作同步监视器

    /**
     * 获取 runState 锁；返回当前（锁定的）runState。
     */
    private int lockRunState() {
        int rs;
        return ((((rs = runState) & RSLOCK) != 0 ||
                 !U.compareAndSwapInt(this, RUNSTATE, rs, rs |= RSLOCK)) ?
                awaitRunStateLock() : rs);
    }

    /**
     * 自旋和/或阻塞直到 runstate 锁可用。参见上述解释。
     */
    private int awaitRunStateLock() {
        Object lock;
        boolean wasInterrupted = false;
        for (int spins = SPINS, r = 0, rs, ns;;) {
            if (((rs = runState) & RSLOCK) == 0) {
                if (U.compareAndSwapInt(this, RUNSTATE, rs, ns = rs | RSLOCK)) {
                    if (wasInterrupted) {
                        try {
                            Thread.currentThread().interrupt();
                        } catch (SecurityException ignore) {
                        }
                    }
                    return ns;
                }
            }
            else if (r == 0)
                r = ThreadLocalRandom.nextSecondarySeed();
            else if (spins > 0) {
                r ^= r << 6; r ^= r >>> 21; r ^= r << 7; // xorshift
                if (r >= 0)
                    --spins;
            }
            else if ((rs & STARTED) == 0 || (lock = stealCounter) == null)
                Thread.yield();   // 初始化竞争
            else if (U.compareAndSwapInt(this, RUNSTATE, rs, rs | RSIGNAL)) {
                synchronized (lock) {
                    if ((runState & RSIGNAL) != 0) {
                        try {
                            lock.wait();
                        } catch (InterruptedException ie) {
                            if (!(Thread.currentThread() instanceof
                                  ForkJoinWorkerThread))
                                wasInterrupted = true;
                        }
                    }
                    else
                        lock.notifyAll();
                }
            }
        }
    }

    /**
     * 解锁并将 runState 设置为 newRunState。
     *
     * @param oldRunState 从 lockRunState 返回的值
     * @param newRunState 下一个值（必须清除锁位）。
     */
    private void unlockRunState(int oldRunState, int newRunState) {
        if (!U.compareAndSwapInt(this, RUNSTATE, oldRunState, newRunState)) {
            Object lock = stealCounter;
            runState = newRunState;              // 清除 RSIGNAL 位
            if (lock != null)
                synchronized (lock) { lock.notifyAll(); }
        }
    }

    // 创建、注册和注销工作线程


                /**
     * 尝试构建并启动一个工作线程。假设总数已经作为预留而增加。在任何失败时调用deregisterWorker。
     *
     * @return 如果成功则返回true
     */
    private boolean createWorker() {
        ForkJoinWorkerThreadFactory fac = factory;
        Throwable ex = null;
        ForkJoinWorkerThread wt = null;
        try {
            if (fac != null && (wt = fac.newThread(this)) != null) {
                wt.start();
                return true;
            }
        } catch (Throwable rex) {
            ex = rex;
        }
        deregisterWorker(wt, ex);
        return false;
    }

    /**
     * 尝试添加一个工作线程，在这样做之前增加ctl计数，依赖于createWorker在失败时回滚。
     *
     * @param c 进来的ctl值，总数为负且没有空闲的工作线程。在CAS失败时，如果满足条件则刷新c并重试（否则，不需要新的工作线程）。
     */
    private void tryAddWorker(long c) {
        boolean add = false;
        do {
            long nc = ((AC_MASK & (c + AC_UNIT)) |
                       (TC_MASK & (c + TC_UNIT)));
            if (ctl == c) {
                int rs, stop;                 // 检查是否终止
                if ((stop = (rs = lockRunState()) & STOP) == 0)
                    add = U.compareAndSwapLong(this, CTL, c, nc);
                unlockRunState(rs, rs & ~RSLOCK);
                if (stop != 0)
                    break;
                if (add) {
                    createWorker();
                    break;
                }
            }
        } while (((c = ctl) & ADD_WORKER) != 0L && (int)c == 0);
    }

    /**
     * ForkJoinWorkerThread构造器的回调，用于建立并记录其WorkQueue。
     *
     * @param wt 工作线程
     * @return 工作者的队列
     */
    final WorkQueue registerWorker(ForkJoinWorkerThread wt) {
        UncaughtExceptionHandler handler;
        wt.setDaemon(true);                           // 配置线程
        if ((handler = ueh) != null)
            wt.setUncaughtExceptionHandler(handler);
        WorkQueue w = new WorkQueue(this, wt);
        int i = 0;                                    // 分配一个池索引
        int mode = config & MODE_MASK;
        int rs = lockRunState();
        try {
            WorkQueue[] ws; int n;                    // 如果没有数组则跳过
            if ((ws = workQueues) != null && (n = ws.length) > 0) {
                int s = indexSeed += SEED_INCREMENT;  // 不太可能冲突
                int m = n - 1;
                i = ((s << 1) | 1) & m;               // 奇数索引
                if (ws[i] != null) {                  // 冲突
                    int probes = 0;                   // 每次大约移动n的一半
                    int step = (n <= 4) ? 2 : ((n >>> 1) & EVENMASK) + 2;
                    while (ws[i = (i + step) & m] != null) {
                        if (++probes >= n) {
                            workQueues = ws = Arrays.copyOf(ws, n <<= 1);
                            m = n - 1;
                            probes = 0;
                        }
                    }
                }
                w.hint = s;                           // 用作随机种子
                w.config = i | mode;
                w.scanState = i;                      // 发布栅栏
                ws[i] = w;
            }
        } finally {
            unlockRunState(rs, rs & ~RSLOCK);
        }
        wt.setName(workerNamePrefix.concat(Integer.toString(i >>> 1)));
        return w;
    }

    /**
     * 从终止的工作线程的最终回调，以及在构建或启动工作线程失败时的回调。从数组中移除工作线程的记录，并调整计数。如果池正在关闭，尝试完成终止。
     *
     * @param wt 工作线程，如果构建失败则为null
     * @param ex 导致失败的异常，如果没有则为null
     */
    final void deregisterWorker(ForkJoinWorkerThread wt, Throwable ex) {
        WorkQueue w = null;
        if (wt != null && (w = wt.workQueue) != null) {
            WorkQueue[] ws;                           // 从数组中移除索引
            int idx = w.config & SMASK;
            int rs = lockRunState();
            if ((ws = workQueues) != null && ws.length > idx && ws[idx] == w)
                ws[idx] = null;
            unlockRunState(rs, rs & ~RSLOCK);
        }
        long c;                                       // 减少计数
        do {} while (!U.compareAndSwapLong
                     (this, CTL, c = ctl, ((AC_MASK & (c - AC_UNIT)) |
                                           (TC_MASK & (c - TC_UNIT)) |
                                           (SP_MASK & c))));
        if (w != null) {
            w.qlock = -1;                             // 确保设置
            w.transferStealCount(this);
            w.cancelAll();                            // 取消剩余任务
        }
        for (;;) {                                    // 可能需要替换
            WorkQueue[] ws; int m, sp;
            if (tryTerminate(false, false) || w == null || w.array == null ||
                (runState & STOP) != 0 || (ws = workQueues) == null ||
                (m = ws.length - 1) < 0)              // 已经在终止
                break;
            if ((sp = (int)(c = ctl)) != 0) {         // 唤醒替换
                if (tryRelease(c, ws[sp & m], AC_UNIT))
                    break;
            }
            else if (ex != null && (c & ADD_WORKER) != 0L) {
                tryAddWorker(c);                      // 创建替换
                break;
            }
            else                                      // 不需要替换
                break;
        }
        if (ex == null)                               // 退出时帮助清理
            ForkJoinTask.helpExpungeStaleExceptions();
        else                                          // 重新抛出
            ForkJoinTask.rethrow(ex);
    }


                // 信号

    /**
     * 尝试创建或激活一个工作线程，如果活跃的工作线程太少。
     *
     * @param ws 要用于查找信号接收者的工作线程数组
     * @param q 一个 WorkQueue -- 如果非空，则不再重试
     */
    final void signalWork(WorkQueue[] ws, WorkQueue q) {
        long c; int sp, i; WorkQueue v; Thread p;
        while ((c = ctl) < 0L) {                       // 活跃线程太少
            if ((sp = (int)c) == 0) {                  // 没有空闲的工作线程
                if ((c & ADD_WORKER) != 0L)            // 工作线程太少
                    tryAddWorker(c);
                break;
            }
            if (ws == null)                            // 未启动/已终止
                break;
            if (ws.length <= (i = sp & SMASK))         // 已终止
                break;
            if ((v = ws[i]) == null)                   // 正在终止
                break;
            int vs = (sp + SS_SEQ) & ~INACTIVE;        // 下一个扫描状态
            int d = sp - v.scanState;                  // 筛选 CAS
            long nc = (UC_MASK & (c + AC_UNIT)) | (SP_MASK & v.stackPred);
            if (d == 0 && U.compareAndSwapLong(this, CTL, c, nc)) {
                v.scanState = vs;                      // 激活 v
                if ((p = v.parker) != null)
                    U.unpark(p);
                break;
            }
            if (q != null && q.base == q.top)          // 没有更多工作
                break;
        }
    }

    /**
     * 如果工作线程 v 在空闲工作线程堆栈的顶部，则发出信号并释放工作线程 v。这仅在至少有一个空闲工作线程时执行 signalWork 的单次版本。
     *
     * @param c 输入的 ctl 值
     * @param v 如果非空，则是一个工作线程
     * @param inc 活跃计数的增量（补偿时为零）
     * @return 如果成功则返回 true
     */
    private boolean tryRelease(long c, WorkQueue v, long inc) {
        int sp = (int)c, vs = (sp + SS_SEQ) & ~INACTIVE; Thread p;
        if (v != null && v.scanState == sp) {          // v 在堆栈顶部
            long nc = (UC_MASK & (c + inc)) | (SP_MASK & v.stackPred);
            if (U.compareAndSwapLong(this, CTL, c, nc)) {
                v.scanState = vs;
                if ((p = v.parker) != null)
                    U.unpark(p);
                return true;
            }
        }
        return false;
    }

    // 任务扫描

    /**
     * 工作线程的顶级运行循环，由 ForkJoinWorkerThread.run 调用。
     */
    final void runWorker(WorkQueue w) {
        w.growArray();                   // 分配队列
        int seed = w.hint;               // 初始时持有随机化提示
        int r = (seed == 0) ? 1 : seed;  // 避免 0 用于 xorShift
        for (ForkJoinTask<?> t;;) {
            if ((t = scan(w, r)) != null)
                w.runTask(t);
            else if (!awaitWork(w, r))
                break;
            r ^= r << 13; r ^= r >>> 17; r ^= r << 5; // xorshift
        }
    }

    /**
     * 扫描并尝试窃取顶级任务。扫描从随机位置开始，遇到明显竞争时随机移动，否则线性继续，直到经过所有队列两次连续的空扫描且校验和相同（每个队列的 base 索引在每次窃取时移动），此时工作线程尝试使自己不活跃，然后重新扫描，如果找到任务则尝试重新激活（自己或其他工作线程）；否则返回 null 以等待工作。扫描过程中尽量减少内存访问，以减少对其他扫描线程的干扰。
     *
     * @param w 工作线程（通过其 WorkQueue）
     * @param r 随机种子
     * @return 一个任务，如果没有找到则返回 null
     */
    private ForkJoinTask<?> scan(WorkQueue w, int r) {
        WorkQueue[] ws; int m;
        if ((ws = workQueues) != null && (m = ws.length - 1) > 0 && w != null) {
            int ss = w.scanState;                     // 初始为非负
            for (int origin = r & m, k = origin, oldSum = 0, checkSum = 0;;) {
                WorkQueue q; ForkJoinTask<?>[] a; ForkJoinTask<?> t;
                int b, n; long c;
                if ((q = ws[k]) != null) {
                    if ((n = (b = q.base) - q.top) < 0 &&
                        (a = q.array) != null) {      // 非空
                        long i = (((a.length - 1) & b) << ASHIFT) + ABASE;
                        if ((t = ((ForkJoinTask<?>)
                                  U.getObjectVolatile(a, i))) != null &&
                            q.base == b) {
                            if (ss >= 0) {
                                if (U.compareAndSwapObject(a, i, t, null)) {
                                    q.base = b + 1;
                                    if (n < -1)       // 通知其他线程
                                        signalWork(ws, q);
                                    return t;
                                }
                            }
                            else if (oldSum == 0 &&   // 尝试激活
                                     w.scanState < 0)
                                tryRelease(c = ctl, ws[m & (int)c], AC_UNIT);
                        }
                        if (ss < 0)                   // 刷新
                            ss = w.scanState;
                        r ^= r << 1; r ^= r >>> 3; r ^= r << 10;
                        origin = k = r & m;           // 移动并重新扫描
                        oldSum = checkSum = 0;
                        continue;
                    }
                    checkSum += b;
                }
                if ((k = (k + 1) & m) == origin) {    // 继续直到稳定
                    if ((ss >= 0 || (ss == (ss = w.scanState))) &&
                        oldSum == (oldSum = checkSum)) {
                        if (ss < 0 || w.qlock < 0)    // 已经不活跃
                            break;
                        int ns = ss | INACTIVE;       // 尝试使不活跃
                        long nc = ((SP_MASK & ns) |
                                   (UC_MASK & ((c = ctl) - AC_UNIT)));
                        w.stackPred = (int)c;         // 保持前一个堆栈顶部
                        U.putInt(w, QSCANSTATE, ns);
                        if (U.compareAndSwapLong(this, CTL, c, nc))
                            ss = ns;
                        else
                            w.scanState = ss;         // 撤销
                    }
                    checkSum = 0;
                }
            }
        }
        return null;
    }


                /**
     * 可能会阻塞工作线程 w 等待任务的窃取，或者
     * 如果工作线程应该终止，则返回 false。如果使 w 失活
     * 导致池变得空闲，检查池的终止情况，并且，只要这不是唯一的工作线程，等待
     * 最多给定的持续时间。超时后，如果 ctl 没有
     * 改变，终止工作线程，这将反过来唤醒
     * 另一个工作线程来可能重复这个过程。
     *
     * @param w 调用的工作线程
     * @param r 随机种子（用于自旋）
     * @return 如果工作线程应该终止，则返回 false
     */
    private boolean awaitWork(WorkQueue w, int r) {
        if (w == null || w.qlock < 0)                 // w 正在终止
            return false;
        for (int pred = w.stackPred, spins = SPINS, ss;;) {
            if ((ss = w.scanState) >= 0)
                break;
            else if (spins > 0) {
                r ^= r << 6; r ^= r >>> 21; r ^= r << 7;
                if (r >= 0 && --spins == 0) {         // 随机化自旋
                    WorkQueue v; WorkQueue[] ws; int s, j; AtomicLong sc;
                    if (pred != 0 && (ws = workQueues) != null &&
                        (j = pred & SMASK) < ws.length &&
                        (v = ws[j]) != null &&        // 查看是否前驱正在停放
                        (v.parker == null || v.scanState >= 0))
                        spins = SPINS;                // 继续自旋
                }
            }
            else if (w.qlock < 0)                     // 自旋后重新检查
                return false;
            else if (!Thread.interrupted()) {
                long c, prevctl, parkTime, deadline;
                int ac = (int)((c = ctl) >> AC_SHIFT) + (config & SMASK);
                if ((ac <= 0 && tryTerminate(false, false)) ||
                    (runState & STOP) != 0)           // 池正在终止
                    return false;
                if (ac <= 0 && ss == (int)c) {        // 是最后一个等待者
                    prevctl = (UC_MASK & (c + AC_UNIT)) | (SP_MASK & pred);
                    int t = (short)(c >>> TC_SHIFT);  // 缩小多余的空闲
                    if (t > 2 && U.compareAndSwapLong(this, CTL, c, prevctl))
                        return false;                 // 否则使用定时等待
                    parkTime = IDLE_TIMEOUT * ((t >= 0) ? 1 : 1 - t);
                    deadline = System.nanoTime() + parkTime - TIMEOUT_SLOP;
                }
                else
                    prevctl = parkTime = deadline = 0L;
                Thread wt = Thread.currentThread();
                U.putObject(wt, PARKBLOCKER, this);   // 模拟 LockSupport
                w.parker = wt;
                if (w.scanState < 0 && ctl == c)      // 停放前重新检查
                    U.park(false, parkTime);
                U.putOrderedObject(w, QPARKER, null);
                U.putObject(wt, PARKBLOCKER, null);
                if (w.scanState >= 0)
                    break;
                if (parkTime != 0L && ctl == c &&
                    deadline - System.nanoTime() <= 0L &&
                    U.compareAndSwapLong(this, CTL, c, prevctl))
                    return false;                     // 缩小池
            }
        }
        return true;
    }

    // 加入任务

    /**
     * 尝试窃取并运行目标计算中的任务。
     * 使用顶级算法的变体，限制为具有给定任务为祖先的任务：它优先取并运行
     * 从工作线程自己的队列中弹出的符合条件的任务（通过
     * popCC）。否则，它扫描其他队列，遇到竞争或执行时随机移动，
     * 根据校验和（通过 pollAndExecCC 的返回代码）决定是否放弃。maxTasks
     * 参数支持外部使用；内部调用使用零，
     * 允许无界步骤（外部调用捕获非正值）。
     *
     * @param w 调用者
     * @param maxTasks 如果非零，则为要运行的其他任务的最大数量
     * @return 退出时的任务状态
     */
    final int helpComplete(WorkQueue w, CountedCompleter<?> task,
                           int maxTasks) {
        WorkQueue[] ws; int s = 0, m;
        if ((ws = workQueues) != null && (m = ws.length - 1) >= 0 &&
            task != null && w != null) {
            int mode = w.config;                 // 用于 popCC
            int r = w.hint ^ w.top;              // 任意种子用于起点
            int origin = r & m;                  // 第一个要扫描的队列
            int h = 1;                           // 1:已运行, >1:竞争, <0:哈希
            for (int k = origin, oldSum = 0, checkSum = 0;;) {
                CountedCompleter<?> p; WorkQueue q;
                if ((s = task.status) < 0)
                    break;
                if (h == 1 && (p = w.popCC(task, mode)) != null) {
                    p.doExec();                  // 运行本地任务
                    if (maxTasks != 0 && --maxTasks == 0)
                        break;
                    origin = k;                  // 重置
                    oldSum = checkSum = 0;
                }
                else {                           // 轮询其他队列
                    if ((q = ws[k]) == null)
                        h = 0;
                    else if ((h = q.pollAndExecCC(task)) < 0)
                        checkSum += h;
                    if (h > 0) {
                        if (h == 1 && maxTasks != 0 && --maxTasks == 0)
                            break;
                        r ^= r << 13; r ^= r >>> 17; r ^= r << 5; // xorshift
                        origin = k = r & m;      // 移动并重新开始
                        oldSum = checkSum = 0;
                    }
                    else if ((k = (k + 1) & m) == origin) {
                        if (oldSum == (oldSum = checkSum))
                            break;
                        checkSum = 0;
                    }
                }
            }
        }
        return s;
    }

    /**
     * 尝试定位并执行给定任务的窃取者，或者反过来，其窃取者之一的任务。追踪 currentSteal ->
     * currentJoin 链，寻找正在处理给定任务的后代的工作线程，并且具有一个非空队列以窃取并
     * 执行任务。首次调用此方法时，等待的连接通常涉及扫描/搜索，（这是可以的
     * 因为连接者没有更好的事情可做），但此方法
     * 在工作线程中留下提示以加速后续调用。
     *
     * @param w 调用者
     * @param task 要加入的任务
     */
    private void helpStealer(WorkQueue w, ForkJoinTask<?> task) {
        WorkQueue[] ws = workQueues;
        int oldSum = 0, checkSum, m;
        if (ws != null && (m = ws.length - 1) >= 0 && w != null &&
            task != null) {
            do {                                       // 重启点
                checkSum = 0;                          // 用于稳定性检查
                ForkJoinTask<?> subtask;
                WorkQueue j = w, v;                    // v 是子任务窃取者
                descent: for (subtask = task; subtask.status >= 0; ) {
                    for (int h = j.hint | 1, k = 0, i; ; k += 2) {
                        if (k > m)                     // 无法找到窃取者
                            break descent;
                        if ((v = ws[i = (h + k) & m]) != null) {
                            if (v.currentSteal == subtask) {
                                j.hint = i;
                                break;
                            }
                            checkSum += v.base;
                        }
                    }
                    for (;;) {                         // 帮助 v 或下降
                        ForkJoinTask<?>[] a; int b;
                        checkSum += (b = v.base);
                        ForkJoinTask<?> next = v.currentJoin;
                        if (subtask.status < 0 || j.currentJoin != subtask ||
                            v.currentSteal != subtask) // 过时
                            break descent;
                        if (b - v.top >= 0 || (a = v.array) == null) {
                            if ((subtask = next) == null)
                                break descent;
                            j = v;
                            break;
                        }
                        int i = (((a.length - 1) & b) << ASHIFT) + ABASE;
                        ForkJoinTask<?> t = ((ForkJoinTask<?>)
                                             U.getObjectVolatile(a, i));
                        if (v.base == b) {
                            if (t == null)             // 过时
                                break descent;
                            if (U.compareAndSwapObject(a, i, t, null)) {
                                v.base = b + 1;
                                ForkJoinTask<?> ps = w.currentSteal;
                                int top = w.top;
                                do {
                                    U.putOrderedObject(w, QCURRENTSTEAL, t);
                                    t.doExec();        // 清除本地任务
                                } while (task.status >= 0 &&
                                         w.top != top &&
                                         (t = w.pop()) != null);
                                U.putOrderedObject(w, QCURRENTSTEAL, ps);
                                if (w.base != w.top)
                                    return;            // 无法进一步帮助
                            }
                        }
                    }
                }
            } while (task.status >= 0 && oldSum != (oldSum = checkSum));
        }
    }


                /**
     * 尝试减少活动计数（有时是隐式的），并可能释放或创建一个补偿工作者以准备阻塞。
     * 如果在竞争、检测到陈旧性、不稳定或终止时，返回 false（调用者可重试）。
     *
     * @param w 调用者
     */
    private boolean tryCompensate(WorkQueue w) {
        boolean canBlock;
        WorkQueue[] ws; long c; int m, pc, sp;
        if (w == null || w.qlock < 0 ||           // 调用者终止
            (ws = workQueues) == null || (m = ws.length - 1) <= 0 ||
            (pc = config & SMASK) == 0)           // 并行性禁用
            canBlock = false;
        else if ((sp = (int)(c = ctl)) != 0)      // 释放空闲工作者
            canBlock = tryRelease(c, ws[sp & m], 0L);
        else {
            int ac = (int)(c >> AC_SHIFT) + pc;
            int tc = (short)(c >> TC_SHIFT) + pc;
            int nbusy = 0;                        // 验证饱和度
            for (int i = 0; i <= m; ++i) {        // 奇数索引的两次遍历
                WorkQueue v;
                if ((v = ws[((i << 1) | 1) & m]) != null) {
                    if ((v.scanState & SCANNING) != 0)
                        break;
                    ++nbusy;
                }
            }
            if (nbusy != (tc << 1) || ctl != c)
                canBlock = false;                 // 不稳定或陈旧
            else if (tc >= pc && ac > 1 && w.isEmpty()) {
                long nc = ((AC_MASK & (c - AC_UNIT)) |
                           (~AC_MASK & c));       // 未补偿
                canBlock = U.compareAndSwapLong(this, CTL, c, nc);
            }
            else if (tc >= MAX_CAP ||
                     (this == common && tc >= pc + commonMaxSpares))
                throw new RejectedExecutionException(
                    "线程限制超过替换阻塞工作者");
            else {                                // 类似于 tryAddWorker
                boolean add = false; int rs;      // 在锁内进行 CAS
                long nc = ((AC_MASK & c) |
                           (TC_MASK & (c + TC_UNIT)));
                if (((rs = lockRunState()) & STOP) == 0)
                    add = U.compareAndSwapLong(this, CTL, c, nc);
                unlockRunState(rs, rs & ~RSLOCK);
                canBlock = add && createWorker(); // 在异常时抛出
            }
        }
        return canBlock;
    }

    /**
     * 帮助和/或阻塞，直到给定任务完成或超时。
     *
     * @param w 调用者
     * @param task 任务
     * @param deadline 对于定时等待，如果非零
     * @return 退出时的任务状态
     */
    final int awaitJoin(WorkQueue w, ForkJoinTask<?> task, long deadline) {
        int s = 0;
        if (task != null && w != null) {
            ForkJoinTask<?> prevJoin = w.currentJoin;
            U.putOrderedObject(w, QCURRENTJOIN, task);
            CountedCompleter<?> cc = (task instanceof CountedCompleter) ?
                (CountedCompleter<?>)task : null;
            for (;;) {
                if ((s = task.status) < 0)
                    break;
                if (cc != null)
                    helpComplete(w, cc, 0);
                else if (w.base == w.top || w.tryRemoveAndExec(task))
                    helpStealer(w, task);
                if ((s = task.status) < 0)
                    break;
                long ms, ns;
                if (deadline == 0L)
                    ms = 0L;
                else if ((ns = deadline - System.nanoTime()) <= 0L)
                    break;
                else if ((ms = TimeUnit.NANOSECONDS.toMillis(ns)) <= 0L)
                    ms = 1L;
                if (tryCompensate(w)) {
                    task.internalWait(ms);
                    U.getAndAddLong(this, CTL, AC_UNIT);
                }
            }
            U.putOrderedObject(w, QCURRENTJOIN, prevJoin);
        }
        return s;
    }

    // 专门的扫描

    /**
     * 如果在扫描期间找到一个（可能）非空的窃取队列，则返回该队列，否则返回 null。
     * 如果调用者在尝试使用队列时发现队列为空，必须重试此方法。
     */
    private WorkQueue findNonEmptyStealQueue() {
        WorkQueue[] ws; int m;  // 一次性扫描循环版本
        int r = ThreadLocalRandom.nextSecondarySeed();
        if ((ws = workQueues) != null && (m = ws.length - 1) >= 0) {
            for (int origin = r & m, k = origin, oldSum = 0, checkSum = 0;;) {
                WorkQueue q; int b;
                if ((q = ws[k]) != null) {
                    if ((b = q.base) - q.top < 0)
                        return q;
                    checkSum += b;
                }
                if ((k = (k + 1) & m) == origin) {
                    if (oldSum == (oldSum = checkSum))
                        break;
                    checkSum = 0;
                }
            }
        }
        return null;
    }

    /**
     * 运行任务直到 {@code isQuiescent()}。我们利用活动计数 ctl 维护，但当找不到任务时，我们不会阻塞，
     * 而是重新扫描，直到所有其他任务也无法找到任务。
     */
    final void helpQuiescePool(WorkQueue w) {
        ForkJoinTask<?> ps = w.currentSteal; // 保存上下文
        for (boolean active = true;;) {
            long c; WorkQueue q; ForkJoinTask<?> t; int b;
            w.execLocalTasks();     // 每次扫描前运行本地任务
            if ((q = findNonEmptyStealQueue()) != null) {
                if (!active) {      // 重新建立活动计数
                    active = true;
                    U.getAndAddLong(this, CTL, AC_UNIT);
                }
                if ((b = q.base) - q.top < 0 && (t = q.pollAt(b)) != null) {
                    U.putOrderedObject(w, QCURRENTSTEAL, t);
                    t.doExec();
                    if (++w.nsteals < 0)
                        w.transferStealCount(this);
                }
            }
            else if (active) {      // 在不排队的情况下减少活动计数
                long nc = (AC_MASK & ((c = ctl) - AC_UNIT)) | (~AC_MASK & c);
                if ((int)(nc >> AC_SHIFT) + (config & SMASK) <= 0)
                    break;          // 绕过减少-然后增加
                if (U.compareAndSwapLong(this, CTL, c, nc))
                    active = false;
            }
            else if ((int)((c = ctl) >> AC_SHIFT) + (config & SMASK) <= 0 &&
                     U.compareAndSwapLong(this, CTL, c, c + AC_UNIT))
                break;
        }
        U.putOrderedObject(w, QCURRENTSTEAL, ps);
    }


                /**
     * 获取并移除给定工作线程的本地或被窃取的任务。
     *
     * @return 如果有可用的任务，则返回该任务
     */
    final ForkJoinTask<?> nextTaskFor(WorkQueue w) {
        for (ForkJoinTask<?> t;;) {
            WorkQueue q; int b;
            if ((t = w.nextLocalTask()) != null)
                return t;
            if ((q = findNonEmptyStealQueue()) == null)
                return null;
            if ((b = q.base) - q.top < 0 && (t = q.pollAt(b)) != null)
                return t;
        }
    }

    /**
     * 返回一个廉价的启发式指南，用于任务划分，当程序员、框架、工具或语言对任务粒度知之甚少时。
     * 本质上，通过提供此方法，我们要求用户只关注开销与预期吞吐量及其变化之间的权衡，而不是任务划分的精细程度。
     *
     * 在稳定状态的严格（树形结构）计算中，每个线程都会提供足够的任务供其他线程保持活跃。归纳地，如果所有线程都遵循相同的规则，每个线程应该只提供常数数量的任务。
     *
     * 最有用的最小常数是1。但使用1作为值将要求每次窃取后立即补充任务以保持足够的任务数量，这是不可行的。此外，提供的任务的划分/粒度应最小化窃取率，通常意味着计算树顶部的线程应生成比底部更多的任务。在完美的稳定状态下，每个线程大约处于相同的计算树级别。然而，生成额外的任务可以摊销进度和扩散假设的不确定性。
     *
     * 因此，用户希望使用大于1（但不会大很多）的值来平滑瞬时短缺并防范不均匀的进度；权衡额外任务开销的成本。我们让用户选择一个阈值，与本方法调用的结果进行比较以指导决策，但建议使用如3这样的值。
     *
     * 当所有线程都活跃时，平均来说，可以严格地局部估计剩余量。在稳定状态下，如果一个线程保持2个剩余任务，那么其他线程也是如此。因此，我们可以只使用估计的队列长度。然而，仅此策略在某些非稳定状态条件（启动、关闭、其他停滞）下会导致严重的估计错误。我们可以通过进一步考虑已知队列任务为零的“空闲”线程的数量，通过（#空闲/#活跃）线程的比例进行补偿。
     */
    static int getSurplusQueuedTaskCount() {
        Thread t; ForkJoinWorkerThread wt; ForkJoinPool pool; WorkQueue q;
        if (((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)) {
            int p = (pool = (wt = (ForkJoinWorkerThread)t).pool).
                config & SMASK;
            int n = (q = wt.workQueue).top - q.base;
            int a = (int)(pool.ctl >> AC_SHIFT) + p;
            return n - (a > (p >>>= 1) ? 0 :
                        a > (p >>>= 1) ? 1 :
                        a > (p >>>= 1) ? 2 :
                        a > (p >>>= 1) ? 4 :
                        8);
        }
        return 0;
    }

    //  终止

    /**
     * 可能启动和/或完成终止。
     *
     * @param now 如果为true，则无条件终止，否则仅在没有工作且没有活跃工作线程时终止
     * @param enable 如果为true，则在下次可能时启用关闭
     * @return 如果当前正在终止或已终止，则返回true
     */
    private boolean tryTerminate(boolean now, boolean enable) {
        int rs;
        if (this == common)                       // 不能关闭
            return false;
        if ((rs = runState) >= 0) {
            if (!enable)
                return false;
            rs = lockRunState();                  // 进入SHUTDOWN阶段
            unlockRunState(rs, (rs & ~RSLOCK) | SHUTDOWN);
        }

        if ((rs & STOP) == 0) {
            if (!now) {                           // 检查空闲
                for (long oldSum = 0L;;) {        // 重复直到稳定
                    WorkQueue[] ws; WorkQueue w; int m, b; long c;
                    long checkSum = ctl;
                    if ((int)(checkSum >> AC_SHIFT) + (config & SMASK) > 0)
                        return false;             // 仍有活跃的工作线程
                    if ((ws = workQueues) == null || (m = ws.length - 1) <= 0)
                        break;                    // 检查队列
                    for (int i = 0; i <= m; ++i) {
                        if ((w = ws[i]) != null) {
                            if ((b = w.base) != w.top || w.scanState >= 0 ||
                                w.currentSteal != null) {
                                tryRelease(c = ctl, ws[m & (int)c], AC_UNIT);
                                return false;     // 安排重新检查
                            }
                            checkSum += b;
                            if ((i & 1) == 0)
                                w.qlock = -1;     // 尝试禁用外部
                        }
                    }
                    if (oldSum == (oldSum = checkSum))
                        break;
                }
            }
            if ((runState & STOP) == 0) {
                rs = lockRunState();              // 进入STOP阶段
                unlockRunState(rs, (rs & ~RSLOCK) | STOP);
            }
        }

        int pass = 0;                             // 3次尝试以帮助终止
        for (long oldSum = 0L;;) {                // 或直到完成或稳定
            WorkQueue[] ws; WorkQueue w; ForkJoinWorkerThread wt; int m;
            long checkSum = ctl;
            if ((short)(checkSum >>> TC_SHIFT) + (config & SMASK) <= 0 ||
                (ws = workQueues) == null || (m = ws.length - 1) <= 0) {
                if ((runState & TERMINATED) == 0) {
                    rs = lockRunState();          // 完成
                    unlockRunState(rs, (rs & ~RSLOCK) | TERMINATED);
                    synchronized (this) { notifyAll(); } // 用于awaitTermination
                }
                break;
            }
            for (int i = 0; i <= m; ++i) {
                if ((w = ws[i]) != null) {
                    checkSum += w.base;
                    w.qlock = -1;                 // 尝试禁用
                    if (pass > 0) {
                        w.cancelAll();            // 清除队列
                        if (pass > 1 && (wt = w.owner) != null) {
                            if (!wt.isInterrupted()) {
                                try {             // 解阻塞join
                                    wt.interrupt();
                                } catch (Throwable ignore) {
                                }
                            }
                            if (w.scanState < 0)
                                U.unpark(wt);     // 唤醒
                        }
                    }
                }
            }
            if (checkSum != oldSum) {             // 不稳定
                oldSum = checkSum;
                pass = 0;
            }
            else if (pass > 3 && pass > m)        // 无法进一步帮助
                break;
            else if (++pass > 1) {                // 尝试出队
                long c; int j = 0, sp;            // 限制尝试次数
                while (j++ <= m && (sp = (int)(c = ctl)) != 0)
                    tryRelease(c, ws[sp & m], AC_UNIT);
            }
        }
        return true;
    }

                // 外部操作

    /**
     * externalPush 的完整版本，处理不常见的情况，
     * 以及在第一次向池提交第一个任务时执行二次初始化。它还检测
     * 由外部线程首次提交的情况，并在索引处的队列为空或有争用时创建一个新的共享队列。
     *
     * @param task 任务。调用者必须确保非空。
     */
    private void externalSubmit(ForkJoinTask<?> task) {
        int r;                                    // 初始化调用者的探针
        if ((r = ThreadLocalRandom.getProbe()) == 0) {
            ThreadLocalRandom.localInit();
            r = ThreadLocalRandom.getProbe();
        }
        for (;;) {
            WorkQueue[] ws; WorkQueue q; int rs, m, k;
            boolean move = false;
            if ((rs = runState) < 0) {
                tryTerminate(false, false);     // 帮助终止
                throw new RejectedExecutionException();
            }
            else if ((rs & STARTED) == 0 ||     // 初始化
                     ((ws = workQueues) == null || (m = ws.length - 1) < 0)) {
                int ns = 0;
                rs = lockRunState();
                try {
                    if ((rs & STARTED) == 0) {
                        U.compareAndSwapObject(this, STEALCOUNTER, null,
                                               new AtomicLong());
                        // 创建大小为2的幂的工作队列数组
                        int p = config & SMASK; // 确保至少有2个槽位
                        int n = (p > 1) ? p - 1 : 1;
                        n |= n >>> 1; n |= n >>> 2;  n |= n >>> 4;
                        n |= n >>> 8; n |= n >>> 16; n = (n + 1) << 1;
                        workQueues = new WorkQueue[n];
                        ns = STARTED;
                    }
                } finally {
                    unlockRunState(rs, (rs & ~RSLOCK) | ns);
                }
            }
            else if ((q = ws[k = r & m & SQMASK]) != null) {
                if (q.qlock == 0 && U.compareAndSwapInt(q, QLOCK, 0, 1)) {
                    ForkJoinTask<?>[] a = q.array;
                    int s = q.top;
                    boolean submitted = false; // 初始提交或调整大小
                    try {                      // 锁定版本的推送
                        if ((a != null && a.length > s + 1 - q.base) ||
                            (a = q.growArray()) != null) {
                            int j = (((a.length - 1) & s) << ASHIFT) + ABASE;
                            U.putOrderedObject(a, j, task);
                            U.putOrderedInt(q, QTOP, s + 1);
                            submitted = true;
                        }
                    } finally {
                        U.compareAndSwapInt(q, QLOCK, 1, 0);
                    }
                    if (submitted) {
                        signalWork(ws, q);
                        return;
                    }
                }
                move = true;                   // 失败时移动
            }
            else if (((rs = runState) & RSLOCK) == 0) { // 创建新队列
                q = new WorkQueue(this, null);
                q.hint = r;
                q.config = k | SHARED_QUEUE;
                q.scanState = INACTIVE;
                rs = lockRunState();           // 发布索引
                if (rs > 0 &&  (ws = workQueues) != null &&
                    k < ws.length && ws[k] == null)
                    ws[k] = q;                 // 否则终止
                unlockRunState(rs, rs & ~RSLOCK);
            }
            else
                move = true;                   // 忙时移动
            if (move)
                r = ThreadLocalRandom.advanceProbe(r);
        }
    }

    /**
     * 尝试将给定任务添加到提交者当前队列的提交队列中。此方法仅直接处理（非常）最常见的路径，
     * 同时筛选是否需要调用 externalSubmit。
     *
     * @param task 任务。调用者必须确保非空。
     */
    final void externalPush(ForkJoinTask<?> task) {
        WorkQueue[] ws; WorkQueue q; int m;
        int r = ThreadLocalRandom.getProbe();
        int rs = runState;
        if ((ws = workQueues) != null && (m = (ws.length - 1)) >= 0 &&
            (q = ws[m & r & SQMASK]) != null && r != 0 && rs > 0 &&
            U.compareAndSwapInt(q, QLOCK, 0, 1)) {
            ForkJoinTask<?>[] a; int am, n, s;
            if ((a = q.array) != null &&
                (am = a.length - 1) > (n = (s = q.top) - q.base)) {
                int j = ((am & s) << ASHIFT) + ABASE;
                U.putOrderedObject(a, j, task);
                U.putOrderedInt(q, QTOP, s + 1);
                U.putIntVolatile(q, QLOCK, 0);
                if (n <= 1)
                    signalWork(ws, q);
                return;
            }
            U.compareAndSwapInt(q, QLOCK, 1, 0);
        }
        externalSubmit(task);
    }

    /**
     * 返回外部线程的公共池队列。
     */
    static WorkQueue commonSubmitterQueue() {
        ForkJoinPool p = common;
        int r = ThreadLocalRandom.getProbe();
        WorkQueue[] ws; int m;
        return (p != null && (ws = p.workQueues) != null &&
                (m = ws.length - 1) >= 0) ?
            ws[m & r & SQMASK] : null;
    }

    /**
     * 为外部提交者执行 tryUnpush：查找队列，如果显然非空则锁定，锁定后验证，并调整顶部。每次检查都可能失败，但很少发生。
     */
    final boolean tryExternalUnpush(ForkJoinTask<?> task) {
        WorkQueue[] ws; WorkQueue w; ForkJoinTask<?>[] a; int m, s;
        int r = ThreadLocalRandom.getProbe();
        if ((ws = workQueues) != null && (m = ws.length - 1) >= 0 &&
            (w = ws[m & r & SQMASK]) != null &&
            (a = w.array) != null && (s = w.top) != w.base) {
            long j = (((a.length - 1) & (s - 1)) << ASHIFT) + ABASE;
            if (U.compareAndSwapInt(w, QLOCK, 0, 1)) {
                if (w.top == s && w.array == a &&
                    U.getObject(a, j) == task &&
                    U.compareAndSwapObject(a, j, task, null)) {
                    U.putOrderedInt(w, QTOP, s - 1);
                    U.putOrderedInt(w, QLOCK, 0);
                    return true;
                }
                U.compareAndSwapInt(w, QLOCK, 1, 0);
            }
        }
        return false;
    }


                /**
     * 为外部提交者执行 helpComplete。
     */
    final int externalHelpComplete(CountedCompleter<?> task, int maxTasks) {
        WorkQueue[] ws; int n;
        int r = ThreadLocalRandom.getProbe();
        return ((ws = workQueues) == null || (n = ws.length) == 0) ? 0 :
            helpComplete(ws[(n - 1) & r & SQMASK], task, maxTasks);
    }

    // 导出的方法

    // 构造函数

    /**
     * 创建一个并行度等于 {@link
     * java.lang.Runtime#availableProcessors} 的 {@code ForkJoinPool}，使用 {@linkplain
     * #defaultForkJoinWorkerThreadFactory 默认线程工厂}，
     * 没有未捕获异常处理器，且处于非异步 LIFO 处理模式。
     *
     * @throws SecurityException 如果存在安全经理，并且调用者没有权限修改线程
     *         因为它没有持有 {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")}
     */
    public ForkJoinPool() {
        this(Math.min(MAX_CAP, Runtime.getRuntime().availableProcessors()),
             defaultForkJoinWorkerThreadFactory, null, false);
    }

    /**
     * 创建一个具有指定并行度的 {@code ForkJoinPool}，使用 {@linkplain
     * #defaultForkJoinWorkerThreadFactory 默认线程工厂}，
     * 没有未捕获异常处理器，且处于非异步 LIFO 处理模式。
     *
     * @param parallelism 并行度
     * @throws IllegalArgumentException 如果并行度小于或等于零，或大于实现限制
     * @throws SecurityException 如果存在安全经理，并且调用者没有权限修改线程
     *         因为它没有持有 {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")}
     */
    public ForkJoinPool(int parallelism) {
        this(parallelism, defaultForkJoinWorkerThreadFactory, null, false);
    }

    /**
     * 使用给定参数创建一个 {@code ForkJoinPool}。
     *
     * @param parallelism 并行度。对于默认值，使用 {@link java.lang.Runtime#availableProcessors}。
     * @param factory 创建新线程的工厂。对于默认值，使用 {@link #defaultForkJoinWorkerThreadFactory}。
     * @param handler 处理因执行任务时遇到无法恢复的错误而终止的内部工作线程的处理器。对于默认值，使用 {@code null}。
     * @param asyncMode 如果为 true，则为从未被 join 的 forked 任务建立本地先进先出调度模式。这种模式在应用程序中可能比默认的本地堆栈模式更合适，
     *         其中工作线程仅处理事件样式的异步任务。对于默认值，使用 {@code false}。
     * @throws IllegalArgumentException 如果并行度小于或等于零，或大于实现限制
     * @throws NullPointerException 如果工厂为 null
     * @throws SecurityException 如果存在安全经理，并且调用者没有权限修改线程
     *         因为它没有持有 {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")}
     */
    public ForkJoinPool(int parallelism,
                        ForkJoinWorkerThreadFactory factory,
                        UncaughtExceptionHandler handler,
                        boolean asyncMode) {
        this(checkParallelism(parallelism),
             checkFactory(factory),
             handler,
             asyncMode ? FIFO_QUEUE : LIFO_QUEUE,
             "ForkJoinPool-" + nextPoolId() + "-worker-");
        checkPermission();
    }

    private static int checkParallelism(int parallelism) {
        if (parallelism <= 0 || parallelism > MAX_CAP)
            throw new IllegalArgumentException();
        return parallelism;
    }

    private static ForkJoinWorkerThreadFactory checkFactory
        (ForkJoinWorkerThreadFactory factory) {
        if (factory == null)
            throw new NullPointerException();
        return factory;
    }

    /**
     * 使用给定参数创建一个 {@code ForkJoinPool}，不进行任何安全检查或参数验证。由 makeCommonPool 直接调用。
     */
    private ForkJoinPool(int parallelism,
                         ForkJoinWorkerThreadFactory factory,
                         UncaughtExceptionHandler handler,
                         int mode,
                         String workerNamePrefix) {
        this.workerNamePrefix = workerNamePrefix;
        this.factory = factory;
        this.ueh = handler;
        this.config = (parallelism & SMASK) | mode;
        long np = (long)(-parallelism); // 偏移 ctl 计数
        this.ctl = ((np << AC_SHIFT) & AC_MASK) | ((np << TC_SHIFT) & TC_MASK);
    }

    /**
     * 返回公共池实例。此池是静态构造的；其运行状态不受 {@link
     * #shutdown} 或 {@link #shutdownNow} 尝试的影响。但是，此池和任何正在进行的处理在程序
     * {@link System#exit} 时会自动终止。任何依赖于异步任务处理在程序终止前完成的程序都应该在退出前调用
     * {@code commonPool().}{@link #awaitQuiescence awaitQuiescence}。
     *
     * @return 公共池实例
     * @since 1.8
     */
    public static ForkJoinPool commonPool() {
        // assert common != null : "static init error";
        return common;
    }

    // 执行方法

    /**
     * 执行给定的任务，返回其完成时的结果。如果计算过程中遇到未检查的异常或错误，
     * 它将作为此调用的结果重新抛出。重新抛出的异常的行为与常规异常相同，但
     * 在可能的情况下，包含当前线程以及实际遇到异常的线程的堆栈跟踪（例如使用 {@code ex.printStackTrace()} 显示）；
     * 最小情况下仅包含后者。
     *
     * @param task 任务
     * @param <T> 任务结果的类型
     * @return 任务的结果
     * @throws NullPointerException 如果任务为 null
     * @throws RejectedExecutionException 如果任务无法被调度执行
     */
    public <T> T invoke(ForkJoinTask<T> task) {
        if (task == null)
            throw new NullPointerException();
        externalPush(task);
        return task.join();
    }


                /**
     * 安排给定任务的（异步）执行。
     *
     * @param task 任务
     * @throws NullPointerException 如果任务为 null
     * @throws RejectedExecutionException 如果任务无法被调度执行
     */
    public void execute(ForkJoinTask<?> task) {
        if (task == null)
            throw new NullPointerException();
        externalPush(task);
    }

    // AbstractExecutorService 方法

    /**
     * @throws NullPointerException 如果任务为 null
     * @throws RejectedExecutionException 如果任务无法被调度执行
     */
    public void execute(Runnable task) {
        if (task == null)
            throw new NullPointerException();
        ForkJoinTask<?> job;
        if (task instanceof ForkJoinTask<?>) // 避免重新包装
            job = (ForkJoinTask<?>) task;
        else
            job = new ForkJoinTask.RunnableExecuteAction(task);
        externalPush(job);
    }

    /**
     * 提交 ForkJoinTask 以执行。
     *
     * @param task 要提交的任务
     * @param <T> 任务结果的类型
     * @return 任务
     * @throws NullPointerException 如果任务为 null
     * @throws RejectedExecutionException 如果任务无法被调度执行
     */
    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        if (task == null)
            throw new NullPointerException();
        externalPush(task);
        return task;
    }

    /**
     * @throws NullPointerException 如果任务为 null
     * @throws RejectedExecutionException 如果任务无法被调度执行
     */
    public <T> ForkJoinTask<T> submit(Callable<T> task) {
        ForkJoinTask<T> job = new ForkJoinTask.AdaptedCallable<T>(task);
        externalPush(job);
        return job;
    }

    /**
     * @throws NullPointerException 如果任务为 null
     * @throws RejectedExecutionException 如果任务无法被调度执行
     */
    public <T> ForkJoinTask<T> submit(Runnable task, T result) {
        ForkJoinTask<T> job = new ForkJoinTask.AdaptedRunnable<T>(task, result);
        externalPush(job);
        return job;
    }

    /**
     * @throws NullPointerException 如果任务为 null
     * @throws RejectedExecutionException 如果任务无法被调度执行
     */
    public ForkJoinTask<?> submit(Runnable task) {
        if (task == null)
            throw new NullPointerException();
        ForkJoinTask<?> job;
        if (task instanceof ForkJoinTask<?>) // 避免重新包装
            job = (ForkJoinTask<?>) task;
        else
            job = new ForkJoinTask.AdaptedRunnableAction(task);
        externalPush(job);
        return job;
    }

    /**
     * @throws NullPointerException       {@inheritDoc}
     * @throws RejectedExecutionException {@inheritDoc}
     */
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        // 在此方法的早期版本中，它构建了一个运行 ForkJoinTask.invokeAll 的任务，但现在外部调用多个任务至少同样高效。
        ArrayList<Future<T>> futures = new ArrayList<>(tasks.size());

        boolean done = false;
        try {
            for (Callable<T> t : tasks) {
                ForkJoinTask<T> f = new ForkJoinTask.AdaptedCallable<T>(t);
                futures.add(f);
                externalPush(f);
            }
            for (int i = 0, size = futures.size(); i < size; i++)
                ((ForkJoinTask<?>)futures.get(i)).quietlyJoin();
            done = true;
            return futures;
        } finally {
            if (!done)
                for (int i = 0, size = futures.size(); i < size; i++)
                    futures.get(i).cancel(false);
        }
    }

    /**
     * 返回用于构建新工作线程的工厂。
     *
     * @return 用于构建新工作线程的工厂
     */
    public ForkJoinWorkerThreadFactory getFactory() {
        return factory;
    }

    /**
     * 返回因执行任务时遇到无法恢复的错误而终止的内部工作线程的处理器。
     *
     * @return 处理器，或如果不存在则返回 {@code null}
     */
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return ueh;
    }

    /**
     * 返回此池的目标并行级别。
     *
     * @return 此池的目标并行级别
     */
    public int getParallelism() {
        int par;
        return ((par = config & SMASK) > 0) ? par : 1;
    }

    /**
     * 返回公共池的目标并行级别。
     *
     * @return 公共池的目标并行级别
     * @since 1.8
     */
    public static int getCommonPoolParallelism() {
        return commonParallelism;
    }

    /**
     * 返回已启动但尚未终止的工作线程的数量。此方法返回的结果可能与 {@link #getParallelism} 不同，当其他线程因合作阻塞而创建线程以保持并行性时。
     *
     * @return 工作线程的数量
     */
    public int getPoolSize() {
        return (config & SMASK) + (short)(ctl >>> TC_SHIFT);
    }

    /**
     * 如果此池使用本地先进先出调度模式来处理从未被加入的任务，则返回 {@code true}。
     *
     * @return 如果此池使用异步模式，则返回 {@code true}
     */
    public boolean getAsyncMode() {
        return (config & FIFO_QUEUE) != 0;
    }

    /**
     * 返回一个估计值，表示未被阻塞等待加入任务或其他管理同步的工作线程的数量。此方法可能高估运行线程的数量。
     *
     * @return 工作线程的数量
     */
    public int getRunningThreadCount() {
        int rc = 0;
        WorkQueue[] ws; WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 1; i < ws.length; i += 2) {
                if ((w = ws[i]) != null && w.isApparentlyUnblocked())
                    ++rc;
            }
        }
        return rc;
    }

                /**
     * 返回当前正在窃取或执行任务的线程数的估计值。此方法可能会高估活动线程的数量。
     *
     * @return 活动线程的数量
     */
    public int getActiveThreadCount() {
        int r = (config & SMASK) + (int)(ctl >> AC_SHIFT);
        return (r <= 0) ? 0 : r; // 抑制暂时的负值
    }

    /**
     * 如果所有工作线程当前都处于空闲状态，则返回 {@code true}。
     * 空闲的工作线程是指无法获取任务执行，因为没有可从其他线程窃取的任务，
     * 并且池中没有待处理的提交任务。此方法是保守的；当所有线程都空闲时，
     * 它可能不会立即返回 {@code true}，但如果线程保持不活跃，最终会变为 true。
     *
     * @return 如果所有线程当前都处于空闲状态，则返回 {@code true}
     */
    public boolean isQuiescent() {
        return (config & SMASK) + (int)(ctl >> AC_SHIFT) <= 0;
    }

    /**
     * 返回一个线程的工作队列中被另一个线程窃取的任务总数的估计值。
     * 当池不处于空闲状态时，报告的值会低估实际的窃取总数。
     * 此值对于监控和调整 fork/join 程序可能有用：通常，窃取次数应该足够高以保持线程忙碌，
     * 但又足够低以避免线程间的开销和争用。
     *
     * @return 窃取的次数
     */
    public long getStealCount() {
        AtomicLong sc = stealCounter;
        long count = (sc == null) ? 0L : sc.get();
        WorkQueue[] ws; WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 1; i < ws.length; i += 2) {
                if ((w = ws[i]) != null)
                    count += w.nsteals;
            }
        }
        return count;
    }

    /**
     * 返回当前由工作线程队列持有的任务总数的估计值（但不包括已提交给池但尚未开始执行的任务）。
     * 此值只是一个近似值，通过遍历池中的所有线程获得。此方法可能对调整任务粒度有用。
     *
     * @return 队列中的任务数量
     */
    public long getQueuedTaskCount() {
        long count = 0;
        WorkQueue[] ws; WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 1; i < ws.length; i += 2) {
                if ((w = ws[i]) != null)
                    count += w.queueSize();
            }
        }
        return count;
    }

    /**
     * 返回已提交给此池但尚未开始执行的任务总数的估计值。此方法可能需要与提交数量成比例的时间。
     *
     * @return 队列中的提交数量
     */
    public int getQueuedSubmissionCount() {
        int count = 0;
        WorkQueue[] ws; WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; i += 2) {
                if ((w = ws[i]) != null)
                    count += w.queueSize();
            }
        }
        return count;
    }

    /**
     * 如果有已提交给此池但尚未开始执行的任务，则返回 {@code true}。
     *
     * @return 如果有排队的提交，则返回 {@code true}
     */
    public boolean hasQueuedSubmissions() {
        WorkQueue[] ws; WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; i += 2) {
                if ((w = ws[i]) != null && !w.isEmpty())
                    return true;
            }
        }
        return false;
    }

    /**
     * 如果有可用的未执行提交，则移除并返回下一个未执行的提交。
     * 此方法在扩展此类时可能有用，用于在具有多个池的系统中重新分配工作。
     *
     * @return 下一个提交，如果没有则返回 {@code null}
     */
    protected ForkJoinTask<?> pollSubmission() {
        WorkQueue[] ws; WorkQueue w; ForkJoinTask<?> t;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; i += 2) {
                if ((w = ws[i]) != null && (t = w.poll()) != null)
                    return t;
            }
        }
        return null;
    }

    /**
     * 从调度队列中移除所有可用的未执行提交和分叉任务，并将它们添加到给定的集合中，而不改变它们的执行状态。
     * 这些可能包括人工生成或包装的任务。此方法设计为仅在已知池处于空闲状态时调用。
     * 在其他时间调用可能不会移除所有任务。尝试将元素添加到集合 {@code c} 时遇到的失败可能导致元素既不在原始集合也不在目标集合中，
     * 或者在其中一个或两个集合中。如果在操作进行过程中修改了指定的集合，则此操作的行为是未定义的。
     *
     * @param c 要转移元素的集合
     * @return 转移的元素数量
     */
    protected int drainTasksTo(Collection<? super ForkJoinTask<?>> c) {
        int count = 0;
        WorkQueue[] ws; WorkQueue w; ForkJoinTask<?> t;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; ++i) {
                if ((w = ws[i]) != null) {
                    while ((t = w.poll()) != null) {
                        c.add(t);
                        ++count;
                    }
                }
            }
        }
        return count;
    }

    /**
     * 返回一个标识此池及其状态的字符串，包括运行状态、并行度级别和工作线程及任务的数量。
     *
     * @return 一个标识此池及其状态的字符串
     */
    public String toString() {
        // 通过遍历 workQueues 收集计数
        long qt = 0L, qs = 0L; int rc = 0;
        AtomicLong sc = stealCounter;
        long st = (sc == null) ? 0L : sc.get();
        long c = ctl;
        WorkQueue[] ws; WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; ++i) {
                if ((w = ws[i]) != null) {
                    int size = w.queueSize();
                    if ((i & 1) == 0)
                        qs += size;
                    else {
                        qt += size;
                        st += w.nsteals;
                        if (w.isApparentlyUnblocked())
                            ++rc;
                    }
                }
            }
        }
        int pc = (config & SMASK);
        int tc = pc + (short)(c >>> TC_SHIFT);
        int ac = pc + (int)(c >> AC_SHIFT);
        if (ac < 0) // 忽略暂时的负值
            ac = 0;
        int rs = runState;
        String level = ((rs & TERMINATED) != 0 ? "Terminated" :
                        (rs & STOP)       != 0 ? "Terminating" :
                        (rs & SHUTDOWN)   != 0 ? "Shutting down" :
                        "Running");
        return super.toString() +
            "[" + level +
            ", parallelism = " + pc +
            ", size = " + tc +
            ", active = " + ac +
            ", running = " + rc +
            ", steals = " + st +
            ", tasks = " + qt +
            ", submissions = " + qs +
            "]";
    }


                /**
     * 可能会启动一个有序的关闭过程，其中之前提交的任务将被执行，但不再接受新的任务。如果这是
     * {@link #commonPool()}，调用不会影响执行状态，如果已经关闭，则没有额外的效果。在本方法执行过程中
     * 并发提交的任务可能会或可能不会被拒绝。
     *
     * @throws SecurityException 如果存在安全管理器，并且调用者没有权限修改线程
     *         因为它没有持有 {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")}
     */
    public void shutdown() {
        checkPermission();
        tryTerminate(false, true);
    }

    /**
     * 可能会尝试取消和/或停止所有任务，并拒绝所有随后提交的任务。如果这是
     * {@link #commonPool()}，调用不会影响执行状态，如果已经关闭，则没有额外的效果。否则，在本方法执行过程中
     * 并发提交或执行的任务可能会或可能不会被拒绝。此方法取消现有和未执行的任务，以允许在任务依赖关系存在的情况下终止。因此，该方法总是返回一个空列表
     * （与其他一些执行器的情况不同）。
     *
     * @return 一个空列表
     * @throws SecurityException 如果存在安全管理器，并且调用者没有权限修改线程
     *         因为它没有持有 {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")}
     */
    public List<Runnable> shutdownNow() {
        checkPermission();
        tryTerminate(true, true);
        return Collections.emptyList();
    }

    /**
     * 如果所有任务在关闭后已完成，则返回 {@code true}。
     *
     * @return 如果所有任务在关闭后已完成，则返回 {@code true}
     */
    public boolean isTerminated() {
        return (runState & TERMINATED) != 0;
    }

    /**
     * 如果终止过程已经开始但尚未完成，则返回 {@code true}。此方法对于调试可能有用。在关闭后足够长的时间
     * 返回 {@code true} 可能表明提交的任务忽略了或抑制了中断，或者正在等待 I/O，导致此执行器无法正确终止。（参见
     * {@link ForkJoinTask} 类的建议，指出任务通常不应涉及阻塞操作。但如果它们确实涉及，它们必须在中断时中止。）
     *
     * @return 如果正在终止但尚未终止，则返回 {@code true}
     */
    public boolean isTerminating() {
        int rs = runState;
        return (rs & STOP) != 0 && (rs & TERMINATED) == 0;
    }

    /**
     * 如果此池已被关闭，则返回 {@code true}。
     *
     * @return 如果此池已被关闭，则返回 {@code true}
     */
    public boolean isShutdown() {
        return (runState & SHUTDOWN) != 0;
    }

    /**
     * 阻塞直到所有任务在关闭请求后完成执行，或者超时发生，或者当前线程被中断，以最先发生的情况为准。因为
     * {@link #commonPool()} 在程序关闭前永远不会终止，当应用于公共池时，此方法等同于 {@link
     * #awaitQuiescence(long, TimeUnit)} 但总是返回 {@code false}。
     *
     * @param timeout 最大等待时间
     * @param unit 超时参数的时间单位
     * @return 如果此执行器已终止，则返回 {@code true}；
     *         如果超时前未终止，则返回 {@code false}
     * @throws InterruptedException 如果等待时被中断
     */
    public boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (this == common) {
            awaitQuiescence(timeout, unit);
            return false;
        }
        long nanos = unit.toNanos(timeout);
        if (isTerminated())
            return true;
        if (nanos <= 0L)
            return false;
        long deadline = System.nanoTime() + nanos;
        synchronized (this) {
            for (;;) {
                if (isTerminated())
                    return true;
                if (nanos <= 0L)
                    return false;
                long millis = TimeUnit.NANOSECONDS.toMillis(nanos);
                wait(millis > 0L ? millis : 1L);
                nanos = deadline - System.nanoTime();
            }
        }
    }

    /**
     * 如果由在此池中运行的 ForkJoinTask 调用，则效果等同于 {@link ForkJoinTask#helpQuiesce}。否则，
     * 等待和/或尝试执行任务，直到此池 {@link #isQuiescent} 或指定的超时时间过去。
     *
     * @param timeout 最大等待时间
     * @param unit 超时参数的时间单位
     * @return 如果池处于静止状态，则返回 {@code true}；如果超时时间过去，则返回 {@code false}
     */
    public boolean awaitQuiescence(long timeout, TimeUnit unit) {
        long nanos = unit.toNanos(timeout);
        ForkJoinWorkerThread wt;
        Thread thread = Thread.currentThread();
        if ((thread instanceof ForkJoinWorkerThread) &&
            (wt = (ForkJoinWorkerThread)thread).pool == this) {
            helpQuiescePool(wt.workQueue);
            return true;
        }
        long startTime = System.nanoTime();
        WorkQueue[] ws;
        int r = 0, m;
        boolean found = true;
        while (!isQuiescent() && (ws = workQueues) != null &&
               (m = ws.length - 1) >= 0) {
            if (!found) {
                if ((System.nanoTime() - startTime) > nanos)
                    return false;
                Thread.yield(); // 不能阻塞
            }
            found = false;
            for (int j = (m + 1) << 2; j >= 0; --j) {
                ForkJoinTask<?> t; WorkQueue q; int b, k;
                if ((k = r++ & m) <= m && k >= 0 && (q = ws[k]) != null &&
                    (b = q.base) - q.top < 0) {
                    found = true;
                    if ((t = q.pollAt(b)) != null)
                        t.doExec();
                    break;
                }
            }
        }
        return true;
    }


/**
 * 无限期等待和/或尝试协助执行任务
 * 直到 {@link #commonPool()} {@link #isQuiescent}。
 */
static void quiesceCommonPool() {
    common.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
}

/**
 * 用于扩展在 {@link ForkJoinPool} 中运行的任务的管理并行性的接口。
 *
 * <p>{@code ManagedBlocker} 提供了两个方法。方法
 * {@link #isReleasable} 必须在不需要阻塞时返回 {@code true}。方法 {@link #block}
 * 在必要时（可能在实际阻塞之前内部调用 {@code isReleasable}）阻塞当前线程。这些操作由调用
 * {@link ForkJoinPool#managedBlock(ManagedBlocker)} 的任何线程执行。此 API 中的特殊方法适应可能但通常不会长时间阻塞的同步器。
 * 同样，它们允许更有效地处理可能但通常不需要额外工作线程以确保足够并行性的情况。为此，方法 {@code isReleasable} 的实现必须能够重复调用。
 *
 * <p>例如，基于 ReentrantLock 的 ManagedBlocker：
 *  <pre> {@code
 * class ManagedLocker implements ManagedBlocker {
 *   final ReentrantLock lock;
 *   boolean hasLock = false;
 *   ManagedLocker(ReentrantLock lock) { this.lock = lock; }
 *   public boolean block() {
 *     if (!hasLock)
 *       lock.lock();
 *     return true;
 *   }
 *   public boolean isReleasable() {
 *     return hasLock || (hasLock = lock.tryLock());
 *   }
 * }}</pre>
 *
 * <p>这是一个可能阻塞等待给定队列上的项目的类：
 *  <pre> {@code
 * class QueueTaker<E> implements ManagedBlocker {
 *   final BlockingQueue<E> queue;
 *   volatile E item = null;
 *   QueueTaker(BlockingQueue<E> q) { this.queue = q; }
 *   public boolean block() throws InterruptedException {
 *     if (item == null)
 *       item = queue.take();
 *     return true;
 *   }
 *   public boolean isReleasable() {
 *     return item != null || (item = queue.poll()) != null;
 *   }
 *   public E getItem() { // 在 pool.managedBlock 完成后调用
 *     return item;
 *   }
 * }}</pre>
 */
public static interface ManagedBlocker {
    /**
     * 可能阻塞当前线程，例如等待锁或条件。
     *
     * @return 如果不需要进一步阻塞（即，如果 isReleasable 会返回 true），则返回 {@code true}
     * @throws InterruptedException 如果在等待过程中被中断（该方法不要求这样做，但允许这样做）
     */
    boolean block() throws InterruptedException;

    /**
     * 如果不需要阻塞，则返回 {@code true}。
     * @return 如果不需要阻塞，则返回 {@code true}
     */
    boolean isReleasable();
}

/**
 * 运行给定的可能阻塞的任务。当 {@linkplain
 * ForkJoinTask#inForkJoinPool() 在 ForkJoinPool 中运行} 时，此方法可能安排激活一个空闲线程，以确保当前线程在
 * {@link ManagedBlocker#block blocker.block()} 中被阻塞时有足够的并行性。
 *
 * <p>此方法重复调用 {@code blocker.isReleasable()} 和 {@code blocker.block()}，直到任一方法返回 {@code true}。
 * 每次调用 {@code blocker.block()} 之前都会调用返回 {@code false} 的 {@code blocker.isReleasable()}。
 *
 * <p>如果不在 ForkJoinPool 中运行，此方法的行为等同于
 *  <pre> {@code
 * while (!blocker.isReleasable())
 *   if (blocker.block())
 *     break;}</pre>
 *
 * 如果在 ForkJoinPool 中运行，池可能首先扩展以确保在调用
 * {@code blocker.block()} 期间有足够的并行性。
 *
 * @param blocker 阻塞任务
 * @throws InterruptedException 如果 {@code blocker.block()} 抛出此异常
 */
public static void managedBlock(ManagedBlocker blocker)
    throws InterruptedException {
    ForkJoinPool p;
    ForkJoinWorkerThread wt;
    Thread t = Thread.currentThread();
    if ((t instanceof ForkJoinWorkerThread) &&
        (p = (wt = (ForkJoinWorkerThread)t).pool) != null) {
        WorkQueue w = wt.workQueue;
        while (!blocker.isReleasable()) {
            if (p.tryCompensate(w)) {
                try {
                    do {} while (!blocker.isReleasable() &&
                                 !blocker.block());
                } finally {
                    U.getAndAddLong(p, CTL, AC_UNIT);
                }
                break;
            }
        }
    }
    else {
        do {} while (!blocker.isReleasable() &&
                     !blocker.block());
    }
}

// AbstractExecutorService 覆盖。这些依赖于未记录的事实，即 ForkJoinTask.adapt 返回的 ForkJoinTasks 也实现了 RunnableFuture。

protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
    return new ForkJoinTask.AdaptedRunnable<T>(runnable, value);
}

protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
    return new ForkJoinTask.AdaptedCallable<T>(callable);
}

// Unsafe 机制
private static final sun.misc.Unsafe U;
private static final int  ABASE;
private static final int  ASHIFT;
private static final long CTL;
private static final long RUNSTATE;
private static final long STEALCOUNTER;
private static final long PARKBLOCKER;
private static final long QTOP;
private static final long QLOCK;
private static final long QSCANSTATE;
private static final long QPARKER;
private static final long QCURRENTSTEAL;
private static final long QCURRENTJOIN;


                static {
        // 初始化用于 CAS 等操作的字段偏移量
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ForkJoinPool.class;
            CTL = U.objectFieldOffset
                (k.getDeclaredField("ctl"));
            RUNSTATE = U.objectFieldOffset
                (k.getDeclaredField("runState"));
            STEALCOUNTER = U.objectFieldOffset
                (k.getDeclaredField("stealCounter"));
            Class<?> tk = Thread.class;
            PARKBLOCKER = U.objectFieldOffset
                (tk.getDeclaredField("parkBlocker"));
            Class<?> wk = WorkQueue.class;
            QTOP = U.objectFieldOffset
                (wk.getDeclaredField("top"));
            QLOCK = U.objectFieldOffset
                (wk.getDeclaredField("qlock"));
            QSCANSTATE = U.objectFieldOffset
                (wk.getDeclaredField("scanState"));
            QPARKER = U.objectFieldOffset
                (wk.getDeclaredField("parker"));
            QCURRENTSTEAL = U.objectFieldOffset
                (wk.getDeclaredField("currentSteal"));
            QCURRENTJOIN = U.objectFieldOffset
                (wk.getDeclaredField("currentJoin"));
            Class<?> ak = ForkJoinTask[].class;
            ABASE = U.arrayBaseOffset(ak);
            int scale = U.arrayIndexScale(ak);
            if ((scale & (scale - 1)) != 0)
                throw new Error("数据类型比例不是2的幂");
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new Error(e);
        }

        commonMaxSpares = DEFAULT_COMMON_MAX_SPARES;
        defaultForkJoinWorkerThreadFactory =
            new DefaultForkJoinWorkerThreadFactory();
        modifyThreadPermission = new RuntimePermission("modifyThread");

        common = java.security.AccessController.doPrivileged
            (new java.security.PrivilegedAction<ForkJoinPool>() {
                public ForkJoinPool run() { return makeCommonPool(); }});
        int par = common.config & SMASK; // 即使线程被禁用，也报告1
        commonParallelism = par > 0 ? par : 1;
    }

    /**
     * 创建并返回公共池，尊重用户通过系统属性指定的设置。
     */
    private static ForkJoinPool makeCommonPool() {

        final ForkJoinWorkerThreadFactory commonPoolForkJoinWorkerThreadFactory =
                new CommonPoolForkJoinWorkerThreadFactory();
        int parallelism = -1;
        ForkJoinWorkerThreadFactory factory = null;
        UncaughtExceptionHandler handler = null;
        try {  // 忽略访问/解析属性时的异常
            String pp = System.getProperty
                ("java.util.concurrent.ForkJoinPool.common.parallelism");
            String fp = System.getProperty
                ("java.util.concurrent.ForkJoinPool.common.threadFactory");
            String hp = System.getProperty
                ("java.util.concurrent.ForkJoinPool.common.exceptionHandler");
            if (pp != null)
                parallelism = Integer.parseInt(pp);
            if (fp != null)
                factory = ((ForkJoinWorkerThreadFactory)ClassLoader.
                           getSystemClassLoader().loadClass(fp).newInstance());
            if (hp != null)
                handler = ((UncaughtExceptionHandler)ClassLoader.
                           getSystemClassLoader().loadClass(hp).newInstance());
        } catch (Exception ignore) {
        }
        if (factory == null) {
            if (System.getSecurityManager() == null)
                factory = commonPoolForkJoinWorkerThreadFactory;
            else // 使用安全管理的默认值
                factory = new InnocuousForkJoinWorkerThreadFactory();
        }
        if (parallelism < 0 && // 默认值比核心数少1
            (parallelism = Runtime.getRuntime().availableProcessors() - 1) <= 0)
            parallelism = 1;
        if (parallelism > MAX_CAP)
            parallelism = MAX_CAP;
        return new ForkJoinPool(parallelism, factory, handler, LIFO_QUEUE,
                                "ForkJoinPool.commonPool-worker-");
    }

    /**
     * 无害工作线程的工厂
     */
    static final class InnocuousForkJoinWorkerThreadFactory
        implements ForkJoinWorkerThreadFactory {

        /**
         * 用于限制工厂本身的权限的 ACC。
         * 构建的工作线程没有任何权限设置。
         */
        private static final AccessControlContext innocuousAcc;
        static {
            Permissions innocuousPerms = new Permissions();
            innocuousPerms.add(modifyThreadPermission);
            innocuousPerms.add(new RuntimePermission(
                                   "enableContextClassLoaderOverride"));
            innocuousPerms.add(new RuntimePermission(
                                   "modifyThreadGroup"));
            innocuousAcc = new AccessControlContext(new ProtectionDomain[] {
                    new ProtectionDomain(null, innocuousPerms)
                });
        }

        public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return (ForkJoinWorkerThread.InnocuousForkJoinWorkerThread)
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<ForkJoinWorkerThread>() {
                    public ForkJoinWorkerThread run() {
                        return new ForkJoinWorkerThread.
                            InnocuousForkJoinWorkerThread(pool);
                    }}, innocuousAcc);
        }
    }

}
