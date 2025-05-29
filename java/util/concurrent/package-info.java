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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并按照
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的那样发布到公共领域。
 */

/**
 * 并发编程中常用的工具类。此包包括一些小型标准化可扩展框架，以及一些提供有用功能的类，这些类通常难以或繁琐实现。以下是主要组件的简要描述。另请参阅
 * {@link java.util.concurrent.locks} 和
 * {@link java.util.concurrent.atomic} 包。
 *
 * <h2>执行器</h2>
 *
 * <b>接口。</b>
 *
 * {@link java.util.concurrent.Executor} 是一个简单的标准化接口，用于定义自定义线程类子系统，包括线程池、异步 I/O 和轻量级任务框架。
 * 根据使用的具体 Executor 类，任务可能在新创建的线程、现有的任务执行线程或调用 {@link java.util.concurrent.Executor#execute
 * execute} 的线程中执行，并可能顺序或并发执行。
 *
 * {@link java.util.concurrent.ExecutorService} 提供了一个更完整的异步任务执行框架。ExecutorService 管理任务的排队和调度，并允许受控关闭。
 *
 * 子接口 {@link java.util.concurrent.ScheduledExecutorService} 及其相关接口增加了对延迟和周期性任务执行的支持。ExecutorServices
 * 提供了安排任何表示为 {@link java.util.concurrent.Callable} 的函数的异步执行的方法，这是 {@link java.lang.Runnable} 的结果承载类比。
 *
 * {@link java.util.concurrent.Future} 返回函数的结果，允许确定执行是否完成，并提供取消执行的方法。
 *
 * {@link java.util.concurrent.RunnableFuture} 是一个具有 {@code run} 方法的 {@code Future}，该方法在执行时设置其结果。
 *
 * <p>
 *
 * <b>实现。</b>
 *
 * 类 {@link java.util.concurrent.ThreadPoolExecutor} 和
 * {@link java.util.concurrent.ScheduledThreadPoolExecutor} 提供了可调且灵活的线程池。
 *
 * {@link java.util.concurrent.Executors} 类提供了最常见种类和配置的 Executors 的工厂方法，以及一些使用它们的实用方法。基于 {@code Executors} 的其他实用工具包括
 * 提供 Futures 的通用可扩展实现的具体类 {@link java.util.concurrent.FutureTask}，以及
 * 协助处理异步任务组的 {@link java.util.concurrent.ExecutorCompletionService}。
 *
 * <p>类 {@link java.util.concurrent.ForkJoinPool} 提供了一个主要设计用于处理 {@link
 * java.util.concurrent.ForkJoinTask} 及其子类实例的 Executor。这些类采用了一种工作窃取调度器，对于符合限制的任务，可以实现高吞吐量，这些限制通常在计算密集型并行处理中成立。
 *
 * <h2>队列</h2>
 *
 * 类 {@link java.util.concurrent.ConcurrentLinkedQueue} 提供了一个高效可扩展的线程安全非阻塞 FIFO 队列。
 * 类 {@link java.util.concurrent.ConcurrentLinkedDeque} 类似，但还支持 {@link java.util.Deque} 接口。
 *
 * <p>在 {@code java.util.concurrent} 中的五个实现支持扩展的 {@link java.util.concurrent.BlockingQueue}
 * 接口，该接口定义了 put 和 take 的阻塞版本：
 * {@link java.util.concurrent.LinkedBlockingQueue}，
 * {@link java.util.concurrent.ArrayBlockingQueue}，
 * {@link java.util.concurrent.SynchronousQueue}，
 * {@link java.util.concurrent.PriorityBlockingQueue}，和
 * {@link java.util.concurrent.DelayQueue}。
 * 不同的类涵盖了生产者-消费者、消息传递、并行任务处理等并发设计中最常见的使用上下文。
 *
 * <p>扩展接口 {@link java.util.concurrent.TransferQueue}，
 * 和实现 {@link java.util.concurrent.LinkedTransferQueue}
 * 引入了一个同步的 {@code transfer} 方法（以及相关的功能），生产者可以选择阻塞等待其消费者。
 *
 * <p>接口 {@link java.util.concurrent.BlockingDeque}
 * 扩展了 {@code BlockingQueue} 以支持 FIFO 和 LIFO（基于栈）操作。
 * 类 {@link java.util.concurrent.LinkedBlockingDeque} 提供了实现。
 *
 * <h2>定时</h2>
 *
 * 类 {@link java.util.concurrent.TimeUnit} 提供了多种粒度（包括纳秒）来指定和控制基于超时的操作。包中的大多数类都包含基于超时的操作，除了无限期等待。在所有使用超时的情况下，
 * 超时指定了方法在指示超时之前应等待的最短时间。实现会尽力在超时发生后尽快检测到超时。然而，在检测到超时和线程实际再次执行之间可能经过无限的时间。所有接受超时参数的方法都将小于或等于零的值视为不等待。要“永远”等待，可以使用值 {@code Long.MAX_VALUE}。
 *
 * <h2>同步器</h2>
 *
 * 五个类帮助常见的特殊用途同步模式。
 * <ul>
 *
 * <li>{@link java.util.concurrent.Semaphore} 是一个经典的并发工具。
 *
 * <li>{@link java.util.concurrent.CountDownLatch} 是一个非常简单但非常常见的工具，用于在给定数量的信号、事件或条件满足之前阻塞。
 *
 * <li>{@link java.util.concurrent.CyclicBarrier} 是一个可重置的多路同步点，在某些并行编程风格中非常有用。
 *
 * <li>{@link java.util.concurrent.Phaser} 提供了一种更灵活的屏障形式，可用于控制多个线程之间的分阶段计算。
 *
 * <li>{@link java.util.concurrent.Exchanger} 允许两个线程在会合点交换对象，在某些管道设计中非常有用。
 *
 * </ul>
 *
 * <h2>并发集合</h2>
 *
 * 除了队列，此包还提供了设计用于多线程上下文的集合实现：
 * {@link java.util.concurrent.ConcurrentHashMap}，
 * {@link java.util.concurrent.ConcurrentSkipListMap}，
 * {@link java.util.concurrent.ConcurrentSkipListSet}，
 * {@link java.util.concurrent.CopyOnWriteArrayList}，和
 * {@link java.util.concurrent.CopyOnWriteArraySet}。
 * 当预期许多线程将访问给定集合时，通常 {@code ConcurrentHashMap} 比同步的 {@code HashMap} 更可取，而 {@code ConcurrentSkipListMap} 比同步的 {@code TreeMap} 更可取。
 * 当预期读取和遍历次数远远多于列表更新次数时，{@code CopyOnWriteArrayList} 比同步的 {@code ArrayList} 更可取。
 *
 * <p>此包中一些类使用的“并发”前缀是与类似“同步”类区别的简写。例如，{@code java.util.Hashtable} 和
 * {@code Collections.synchronizedMap(new HashMap())} 是同步的。但 {@link
 * java.util.concurrent.ConcurrentHashMap} 是“并发”的。并发集合是线程安全的，但不受单一排除锁的管理。在 {@code ConcurrentHashMap} 的特定情况下，它安全地允许任何数量的并发读取以及可调数量的并发写入。“同步”类在需要通过单个锁防止对集合的所有访问时非常有用，但以较差的可扩展性为代价。在预期多个线程将访问公共集合的情况下，“并发”版本通常更可取。而当集合未共享或仅在持有其他锁时可访问时，未同步的集合更可取。
 *
 * <p id="Weakly">大多数并发集合实现（包括大多数队列）也与通常的 {@code java.util}
 * 约定不同，因为它们的 {@linkplain java.util.Iterator 迭代器} 和 {@linkplain java.util.Spliterator 分割器} 提供了
 * <em>弱一致性</em> 而不是快速失败遍历：
 * <ul>
 * <li>它们可以与其他操作并行进行
 * <li>它们永远不会抛出 {@link java.util.ConcurrentModificationException
 * ConcurrentModificationException}
 * <li>它们保证遍历构造时存在的元素一次，且可能（但不保证）反映构造后的任何修改。
 * </ul>
 *
 * <h2 id="MemoryVisibility">内存一致性属性</h2>
 *
 * <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.4.5">
 * Java 语言规范第 17 章</a> 定义了读取和写入共享变量等内存操作的 <i>先发生</i> 关系。一个线程的写操作结果只有在写操作 <i>先发生</i> 读操作时，才能保证对另一个线程的读操作可见。{@code synchronized} 和 {@code volatile} 构造，以及 {@code Thread.start()} 和 {@code Thread.join()} 方法，可以形成 <i>先发生</i> 关系。特别是：
 *
 * <ul>
 *   <li>线程中的每个动作 <i>先发生</i> 该线程中程序顺序中随后的所有动作。
 *
 *   <li>对监视器的解锁 ({@code synchronized} 块或方法退出) <i>先发生</i> 该监视器的任何后续锁定 ({@code synchronized} 块或方法进入)。由于 <i>先发生</i> 关系是传递的，因此线程在解锁前的所有动作 <i>先发生</i> 任何线程在锁定该监视器后的所有动作。
 *
 *   <li>对 {@code volatile} 字段的写入 <i>先发生</i> 该字段的任何后续读取。对 {@code volatile} 字段的写入和读取具有与进入和退出监视器相似的内存一致性效果，但 <em>不</em> 包括互斥锁。
 *
 *   <li>对线程的 {@code start} 调用 <i>先发生</i> 该线程的任何动作。
 *
 *   <li>线程中的所有动作 <i>先发生</i> 其他线程成功从该线程的 {@code join} 返回。
 *
 * </ul>
 *
 *
 * {@code java.util.concurrent} 及其子包中所有类的方法将这些保证扩展到更高层次的同步。特别是：
 *
 * <ul>
 *
 *   <li>将对象放入任何并发集合之前的线程中的动作 <i>先发生</i> 另一线程访问或移除该元素后的动作。
 *
 *   <li>将 {@code Runnable} 提交给 {@code Executor} 之前的线程中的动作 <i>先发生</i> 其执行开始。同样适用于提交给 {@code ExecutorService} 的 {@code Callables}。
 *
 *   <li>由 {@code Future} 表示的异步计算的动作用 <i>先发生</i> 通过 {@code Future.get()} 在另一线程中检索结果后的动作。
 *
 *   <li>在“释放”同步器方法（如 {@code Lock.unlock}、{@code Semaphore.release} 和
 *   {@code CountDownLatch.countDown}）之前的动作用 <i>先发生</i> 在另一线程中成功“获取”方法（如
 *   {@code Lock.lock}、{@code Semaphore.acquire}、
 *   {@code Condition.await} 和 {@code CountDownLatch.await}）后的动作，这些方法作用于同一同步器对象。
 *
 *   <li>对于通过 {@code Exchanger} 成功交换对象的每对线程，每个线程中的 {@code exchange()}
 *   之前的动作用 <i>先发生</i> 另一线程中对应的 {@code exchange()} 后的动作用。
 *
 *   <li>调用 {@code CyclicBarrier.await} 和
 *   {@code Phaser.awaitAdvance}（及其变体）之前的动作用 <i>先发生</i> 由屏障动作执行的动作，而屏障动作执行的动作用 <i>先发生</i> 其他线程成功从相应的 {@code await} 返回后的动作。
 *
 * </ul>
 *
 * @since 1.5
 */
package java.util.concurrent;
