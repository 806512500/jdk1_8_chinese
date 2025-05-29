
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
 * 由 Doug Lea 与 JCP JSR-166 专家组成员合作编写，并发布到公共领域，如在
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的。
 */

package java.util.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * 基于链接节点的无界 {@link TransferQueue}。
 * 该队列按任何给定生产者的先进先出（FIFO）顺序排列元素。队列的<em>头部</em>是
 * 某些生产者在队列中停留时间最长的元素。队列的<em>尾部</em>是
 * 某些生产者在队列中停留时间最短的元素。
 *
 * <p>请注意，与大多数集合不同，{@code size} 方法
 * <em>不是</em> 常量时间操作。由于这些队列的异步性质，确定当前元素数量需要遍历元素，
 * 因此如果在遍历时修改此集合，可能会报告不准确的结果。
 * 此外，批量操作 {@code addAll}，
 * {@code removeAll}，{@code retainAll}，{@code containsAll}，
 * {@code equals} 和 {@code toArray} <em>不是</em> 保证原子执行的。例如，
 * 一个迭代器与一个 {@code addAll} 操作并发运行时，可能会只看到部分添加的元素。
 *
 * <p>此类及其迭代器实现了 {@link Collection} 和 {@link
 * Iterator} 接口的所有 <em>可选</em> 方法。
 *
 * <p>内存一致性效果：与其他并发集合一样，一个线程在将对象放入 {@code LinkedTransferQueue}
 * <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 另一个线程访问或移除此元素后的动作。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.7
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public class LinkedTransferQueue<E> extends AbstractQueue<E>
    implements TransferQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -3223113410248163686L;

    /*
     * *** 松散双队列概述 ***
     *
     * 双队列，由 Scherer 和 Scott 引入
     * (http://www.cs.rice.edu/~wns1/papers/2004-DISC-DDS.pdf) 是
     * (链接) 队列，其中节点可以表示数据或请求。当一个线程尝试将数据节点入队，但遇到请求节点时，
     * 它会“匹配”并移除它；反之亦然，对于入队请求。阻塞双队列安排入队未匹配请求的线程
 * 直到其他线程提供匹配时才会阻塞。双同步队列（参见
 * Scherer, Lea, & Scott
 * http://www.cs.rochester.edu/u/scott/papers/2009_Scherer_CACM_SSQ.pdf）
 * 还安排入队未匹配数据的线程也阻塞。双传输队列支持所有这些模式，由调用者决定。
 *
 * 可以使用 Michael & Scott (M&S) 无锁队列算法的变体实现 FIFO 双队列
 * (http://www.cs.rochester.edu/u/scott/papers/1996_PODC_queues.pdf)。
 * 它维护两个指针字段，“head”，指向一个（已匹配的）节点，该节点又指向第一个实际的
 * （未匹配的）队列节点（或为空）；以及“tail”，指向队列中的最后一个节点（或再次为空）。
 * 例如，这里是一个可能包含四个数据元素的队列：
 *
 *  head                tail
 *    |                   |
 *    v                   v
 *    M -> U -> U -> U -> U
 *
 * M&S 队列算法已知在通过 CAS 维护这些头和尾指针时存在可扩展性和开销限制。这导致了
 * 降低竞争的变体的开发，例如消除数组（参见 Moir 等
 * http://portal.acm.org/citation.cfm?id=1074013）和乐观回指针（参见 Ladan-Mozes & Shavit
 * http://people.csail.mit.edu/edya/publications/OptimisticFIFOQueue-journal.pdf）。
 * 然而，双队列的性质使在需要双性时改进 M&S 风格实现的策略更加简单。
 *
 * 在双队列中，每个节点必须原子地维护其匹配状态。虽然有其他可能的变体，但这里我们实现为：
 * 对于数据模式节点，匹配涉及将“item”字段从非空数据值 CAS 到 null 以匹配，反之亦然，
 * 对于请求节点，从 null CAS 到数据值。 （请注意，这种风格队列的线性化属性很容易验证——
 * 元素通过链接变得可用，并通过匹配变得不可用。）与普通的 M&S 队列相比，双队列的这一属性
 * 每个入队/出队对需要一个额外的成功的原子操作。但它也使队列维护机制的成本更低。
 * （即使对于支持删除内部元素的非双队列，如 j.u.c.ConcurrentLinkedQueue，这一思想的变体也适用。）
 *
 * 一旦节点匹配，其匹配状态就再也不会改变。因此，我们可以安排它们的链接列表包含
 * 零个或多个匹配节点的前缀，后跟零个或多个未匹配节点的后缀。（请注意，我们允许
 * 前缀和后缀都为零长度，这意味着我们不使用虚拟头节点。）如果我们不关心
 * 时间或空间效率，我们可以通过从指向初始节点的指针遍历来正确执行入队和出队操作；
 * 在匹配时 CAS 未匹配节点的 item，以及在追加时 CAS 尾部节点的 next 字段。
 * （加上一些特殊情况处理，例如最初为空时。）虽然这本身是一个糟糕的主意，但它确实有
 * 不需要对头/尾字段进行任何原子更新的好处。
 *
 * 我们在这里介绍了一种介于从不更新队列（头和尾）指针和总是更新之间的方法。
 * 这在有时需要额外的遍历步骤来定位第一个和/或最后一个未匹配节点，与减少
 * 队列指针更新的开销和竞争之间提供了权衡。例如，一个可能的队列快照是：
 *
 *  head           tail
 *    |              |
 *    v              v
 *    M -> M -> U -> U -> U -> U
 *
 * 这个“松弛”（头值与第一个未匹配节点之间的目标最大距离，以及尾部类似）的最佳值
 * 是一个经验问题。我们发现，在一系列平台上，使用 1-3 范围内的非常小的常数效果最好。
 * 较大的值会增加缓存未命中的成本和长遍历链的风险，而较小的值会增加 CAS 竞争和开销。
 *
 * 松弛双队列与普通 M&S 双队列不同之处在于，它们在匹配、追加或甚至遍历节点时
 * 有时会更新头或尾指针，以保持目标松弛。实现“有时”的方法有几种。最简单的是
 * 使用每次遍历步骤递增的操作计数器，并在计数超过阈值时尝试（通过 CAS）更新
 * 相关的队列指针。另一种需要更多开销的方法是使用随机数生成器
 * 以给定的概率在每次遍历步骤中更新。
 *
 * 在这些策略中的任何一种中，由于更新字段的 CAS 可能会失败，实际松弛可能超过目标松弛。
 * 然而，它们可以在任何时候重试以保持目标。即使使用非常小的松弛值，这种方法也适用于双队列，
 * 因为它允许所有操作在匹配或追加项目之前（因此可能允许另一个线程取得进展）是只读的，
 * 从而不引入任何进一步的竞争。如下所述，我们通过在这些点之后执行松弛维护重试来实现这一点。
 *
 * 作为这些技术的补充，可以进一步减少遍历开销，而不会增加头指针更新的竞争：
 * 线程有时可以将从当前“头”节点到更接近当前已知第一个未匹配节点的“下一个”链接路径缩短，
 * 类似地，对于尾部也是如此。同样，这可以使用阈值或随机化触发。
 *
 * 这些想法必须进一步扩展以避免由从旧遗忘头节点开始的节点的“下一个”链接序列
 * 造成的大量难以回收的垃圾：正如 Boehm 首次详细描述的那样
 * (http://portal.acm.org/citation.cfm?doid=503272.503282)，如果 GC 延迟注意到
 * 任何任意旧节点已变成垃圾，所有更新的死节点也将未被回收。（在非 GC 环境中也会出现类似问题。）
 * 为了在我们的实现中应对这一点，在 CAS 以推进头指针时，我们将前一个头的“下一个”链接
 * 设置为仅指向自身；从而限制连接的死列表的长度。
 * （我们还采取类似的措施清除可能保留垃圾的其他 Node 字段中的值。）然而，这样做
 * 增加了遍历的复杂性：如果任何“下一个”指针链接到自身，这表明当前线程
 * 落后于头更新，因此遍历必须从“头”继续。尝试从“尾”开始查找当前尾部的遍历
 * 也可能遇到自链接，在这种情况下，它们也从“头”继续。
 *
 * 在基于松弛的方案中，即使不使用 CAS 进行更新（类似于 Ladan-Mozes & Shavit）也很诱人。
 * 然而，这不能用于头更新，因为在上述链接遗忘机制下，更新可能会将头留在一个分离的节点上。
 * 虽然可以直接写入尾部更新，但这会增加长重遍历的风险，从而增加长垃圾链，
 * 这比值得考虑的成本要高得多，考虑到执行 CAS 与写入的成本差异较小
 * 当它们不是每次操作触发时（特别是考虑到写入和 CAS 同样需要额外的 GC 会计（“写屏障”），
 * 有时比写入本身更昂贵，因为竞争。）
 *
 * *** 实现概述 ***
 *
 * 我们使用基于阈值的方法进行更新，松弛阈值为两个——也就是说，当当前指针
 * 似乎与第一个/最后一个节点相距两个或更多步骤时，我们更新头/尾。松弛值是硬编码的：
 * 路径大于一自然通过检查遍历指针的等价性来实现，除非列表中只有一个元素，
 * 在这种情况下我们保持松弛阈值为一个。避免跨方法调用跟踪显式计数
 * 稍微简化了一个已经很复杂的实现。如果有一个低质量但便宜的每线程随机数生成器可用，
 * 使用随机化可能会更好，但即使是 ThreadLocalRandom 对这些目的来说也太重了。
 *
 * 使用如此小的松弛阈值，除了取消/删除（见下文）之外，增加路径捷径（即取消内部节点的链接）并不值得。
 *
 * 在任何节点入队之前，我们允许头和尾字段都为空；在第一次追加时初始化。
 * 这简化了其他一些逻辑，以及提供了更有效的显式控制路径，而不是让 JVM 在它们为空时插入
 * 隐式 NullPointerException。虽然目前没有完全实现，我们也保留了在为空时重新设置这些字段为空的可能性
 * （这很复杂，但几乎没有好处。）
 *
 * 所有入队/出队操作都由单个方法“xfer”处理，参数指示是否以某种形式的
 * offer, put, poll, take 或 transfer（每个可能带有超时）的形式执行。使用一个单一的大型方法
 * 的相对复杂性超过了为每个情况使用单独方法的代码体积和维护问题。
 *
 * 操作包括最多三个阶段。第一阶段在方法 xfer 内实现，第二阶段在 tryAppend 中，
 * 第三阶段在方法 awaitMatch 中。
 *
 * 1. 尝试匹配现有节点
 *
 *    从头开始，跳过已匹配的节点，直到找到一个相反模式的未匹配节点，如果存在，
 *    则匹配它并返回，必要时更新头指针，使其指向匹配节点之后的一个节点
 *    （或如果列表中没有其他未匹配节点，则指向该节点本身）。如果 CAS 失败，则
 *    一个循环通过两次推进头指针重试，直到成功或松弛最多为两个。通过要求每次尝试
 *    推进头指针两次（如果适用），我们确保松弛不会无限制增长。遍历还检查
 *    初始头是否已脱离列表，在这种情况下，它们从新的头开始。
 *
 *    如果没有找到候选节点且调用是未定时的 poll/offer（参数“how”为 NOW），则返回。
 *
 * 2. 尝试追加新节点（方法 tryAppend）
 *
 *    从当前尾指针开始，找到实际的最后一个节点并尝试追加一个新节点（或如果头为空，则建立第一个节点）。
 *    节点只能在其前驱节点已匹配或模式相同的情况下追加。如果我们检测到其他情况，则必须
 *    重新开始第一阶段。遍历和更新步骤与第一阶段类似：在 CAS 失败时重试并检查陈旧性。
 *    特别是，如果遇到自链接，则可以通过从当前头继续遍历来安全地跳到列表上的节点。
 *
 *    成功追加后，如果调用是 ASYNC，则返回。
 *
 * 3. 等待匹配或取消（方法 awaitMatch）
 *
 *    等待另一个线程匹配节点；如果当前线程被中断或等待超时，则取消。在多处理器上，
 *    我们使用队列前端自旋：如果节点似乎是队列中的第一个未匹配节点，则在阻塞前自旋一段时间。
 *    无论哪种情况，在阻塞前都会尝试取消当前“头”和第一个未匹配节点之间的任何节点的链接。
 *
 *    队列前端自旋极大地提高了高度竞争队列的性能。只要自旋相对较短且“安静”，
 *    自旋对竞争较少的队列的性能影响不大。在自旋期间，线程会检查其中断状态并生成一个线程本地随机数
 *    以决定偶尔执行 Thread.yield。虽然 yield 的规格未定义，但我们假设它可能会有所帮助，
 *    并且不会对忙碌系统上的自旋影响造成损害。我们还使用较小（1/2）的自旋
 *    对于未知是否为前端但其前驱尚未阻塞的节点——这些“链接”自旋避免了
 *    队列前端规则导致的节点自旋与阻塞交替出现的现象。此外，代表阶段变化（从数据节点到请求节点或反之亦然）
 *    的前端线程相对于其前驱会收到额外的链接自旋，反映了在阶段变化期间通常需要更长的路径来解除线程阻塞。
 *
 *
 * ** 取消内部节点的链接 **
 *
 * 除了通过自链接最小化垃圾保留外，我们还取消了已删除的内部节点的链接。这些节点可能由于
 * 超时或中断的等待，或调用 remove(x) 或 Iterator.remove 而产生。通常，给定一个节点
 * 一度已知是某个要删除的节点 s 的前驱，我们可以通过 CAS 更新其前驱的 next 字段来取消 s 的链接
 * 如果它仍然指向 s（否则 s 必须已经被删除或现在不在列表中）。但有两种情况
 * 无法保证以这种方式使节点 s 不可达：(1) 如果 s 是列表的尾节点（即，next 为空），
 * 则它作为追加的目标节点被固定，因此只能在其他节点追加后稍后删除。(2) 我们不能保证取消 s 的链接
 * 给定一个已匹配（包括已取消）的前驱节点：前驱可能已经被取消链接，这种情况下
 * 一些先前可到达的节点可能仍然指向 s。（有关进一步解释，请参见 Herlihy & Shavit
 * “The Art of Multiprocessor Programming” 第 9 章）。尽管如此，如果 s 或其前驱
 * （或可以被设置为）在列表头部，我们可以排除进一步行动的需要。
 *
 * 如果不考虑这些情况，可能会导致大量已删除的节点仍然可达。导致此类累积的情况不常见，
 * 但实践中确实可能发生；例如，当一系列短时调用 poll 反复超时但从未因
 * 队列前端的未定时调用 take 而脱离列表时。
 *
 * 当这些情况出现时，而不是总是重新遍历整个列表以找到实际的前驱来取消链接（这在情况 (1) 中无论如何也不会有帮助），
 * 我们记录了一个可能的取消链接失败的保守估计（在“sweepVotes”中）。当估计值超过阈值
 * （“SWEEP_THRESHOLD”）时，表示在扫除之前可容忍的最大估计移除失败次数，我们触发一次全面扫除，
 * 取消初始移除时未取消链接的已取消节点的链接。我们由触发阈值的线程执行扫除（而不是后台线程或
 * 将工作分配给其他线程），因为在移除发生的主上下文中，调用者已经超时、取消或执行
 * 潜在的 O(n) 操作（例如 remove(x)），这些操作都不够时间关键，不足以证明替代方案
 * 对其他线程造成的开销是合理的。
 *
 * 由于扫除投票估计是保守的，而且节点在脱离列表头部时会“自然”取消链接，
 * 以及我们允许在扫除进行时累积投票，通常这样的节点数量比估计的要少得多。
 * 阈值的选择平衡了浪费努力和竞争的可能性，与提供静默队列中内部节点保留的最坏情况界限。
 * 下面定义的值是根据各种超时情况的经验选择的，以平衡这些因素。
 *
 * 请注意，我们不能在扫除期间自链接已取消链接的内部节点。然而，相关的垃圾链
 * 当某些后继最终脱离列表头部并自链接时终止。
 */


                    /** 如果是多处理器环境则为真 */
    private static final boolean MP =
        Runtime.getRuntime().availableProcessors() > 1;

    /**
     * 在多处理器环境下，当节点显然是队列中的第一个等待者时，在阻塞前自旋的次数（带有随机插入的Thread.yield调用）。参见上述解释。必须是2的幂。该值是通过经验得出的——在各种处理器、CPU数量和操作系统上表现良好。
     */
    private static final int FRONT_SPINS   = 1 << 7;

    /**
     * 当节点前有一个显然正在自旋的节点时，在阻塞前自旋的次数。也作为阶段变化时FRONT_SPINS的增量，以及自旋期间调用yield的基平均频率。必须是2的幂。
     */
    private static final int CHAINED_SPINS = FRONT_SPINS >>> 1;

    /**
     * 在遍历队列解除未在初始移除时解除的取消节点之前，可以容忍的估计移除失败次数（sweepVotes）。参见上述解释。该值至少为2，以避免在移除尾节点时进行无用的遍历。
     */
    static final int SWEEP_THRESHOLD = 32;

    /**
     * 队列节点。使用Object而不是E作为项，以允许在使用后忘记它们。大量依赖于Unsafe机制以最小化不必要的顺序约束：内在顺序的写入相对于其他访问或CAS使用简单的放松形式。
     */
    static final class Node {
        final boolean isData;   // 如果这是请求节点，则为false
        volatile Object item;   // 如果是isData，则最初非空；CAS匹配
        volatile Node next;
        volatile Thread waiter; // 等待前为null

        // 字段的CAS方法
        final boolean casNext(Node cmp, Node val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
        }

        final boolean casItem(Object cmp, Object val) {
            // assert cmp == null || cmp.getClass() != Node.class;
            return UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
        }

        /**
         * 构造一个新节点。使用放松写入，因为item只能在通过casNext发布后被看到。
         */
        Node(Object item, boolean isData) {
            UNSAFE.putObject(this, itemOffset, item); // 放松写入
            this.isData = isData;
        }

        /**
         * 将节点链接到自身以避免垃圾保留。仅在CAS头字段后调用，因此使用放松写入。
         */
        final void forgetNext() {
            UNSAFE.putObject(this, nextOffset, this);
        }

        /**
         * 将item设置为自身并将waiter设置为null，以避免匹配或取消后保留垃圾。使用放松写入，因为顺序在唯一的调用上下文中已经受到约束：item仅在提取项的volatile/原子机制后被忘记。同样，清除waiter跟随CAS或从park返回（如果曾经park；否则我们不在乎）。
         */
        final void forgetContents() {
            UNSAFE.putObject(this, itemOffset, this);
            UNSAFE.putObject(this, waiterOffset, null);
        }

        /**
         * 如果此节点已被匹配，包括由于取消导致的人工匹配，则返回true。
         */
        final boolean isMatched() {
            Object x = item;
            return (x == this) || ((x == null) == isData);
        }

        /**
         * 如果这是未匹配的请求节点，则返回true。
         */
        final boolean isUnmatchedRequest() {
            return !isData && item == null;
        }

        /**
         * 如果给定模式的节点不能附加到此节点，因为此节点未匹配且具有相反的数据模式，则返回true。
         */
        final boolean cannotPrecede(boolean haveData) {
            boolean d = isData;
            Object x;
            return d != haveData && (x = item) != this && (x != null) == d;
        }

        /**
         * 尝试人工匹配数据节点——由remove使用。
         */
        final boolean tryMatchData() {
            // assert isData;
            Object x = item;
            if (x != null && x != this && casItem(x, null)) {
                LockSupport.unpark(waiter);
                return true;
            }
            return false;
        }

        private static final long serialVersionUID = -3375979862319811754L;

        // Unsafe机制
        private static final sun.misc.Unsafe UNSAFE;
        private static final long itemOffset;
        private static final long nextOffset;
        private static final long waiterOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Node.class;
                itemOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("item"));
                nextOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("next"));
                waiterOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("waiter"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /** 队列的头；直到第一次入队前为null */
    transient volatile Node head;

    /** 队列的尾；直到第一次追加前为null */
    private transient volatile Node tail;

    /** 明显失败解除已移除节点的次数 */
    private transient volatile int sweepVotes;

    // 字段的CAS方法
    private boolean casTail(Node cmp, Node val) {
        return UNSAFE.compareAndSwapObject(this, tailOffset, cmp, val);
    }

    private boolean casHead(Node cmp, Node val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }


private boolean casSweepVotes(int cmp, int val) {
    return UNSAFE.compareAndSwapInt(this, sweepVotesOffset, cmp, val);
}

/*
 * Possible values for "how" argument in xfer method.
 */
private static final int NOW   = 0; // for untimed poll, tryTransfer
private static final int ASYNC = 1; // for offer, put, add
private static final int SYNC  = 2; // for transfer, take
private static final int TIMED = 3; // for timed poll, tryTransfer

@SuppressWarnings("unchecked")
static <E> E cast(Object item) {
    // assert item == null || item.getClass() != Node.class;
    return (E) item;
}

/**
 * Implements all queuing methods. See above for explanation.
 *
 * @param e the item or null for take
 * @param haveData true if this is a put, else a take
 * @param how NOW, ASYNC, SYNC, or TIMED
 * @param nanos timeout in nanosecs, used only if mode is TIMED
 * @return an item if matched, else e
 * @throws NullPointerException if haveData mode but e is null
 */
private E xfer(E e, boolean haveData, int how, long nanos) {
    if (haveData && (e == null))
        throw new NullPointerException();
    Node s = null;                        // the node to append, if needed

    retry:
    for (;;) {                            // restart on append race

        for (Node h = head, p = h; p != null;) { // find & match first node
            boolean isData = p.isData;
            Object item = p.item;
            if (item != p && (item != null) == isData) { // unmatched
                if (isData == haveData)   // can't match
                    break;
                if (p.casItem(item, e)) { // match
                    for (Node q = p; q != h;) {
                        Node n = q.next;  // update by 2 unless singleton
                        if (head == h && casHead(h, n == null ? q : n)) {
                            h.forgetNext();
                            break;
                        }                 // advance and retry
                        if ((h = head)   == null ||
                            (q = h.next) == null || !q.isMatched())
                            break;        // unless slack < 2
                    }
                    LockSupport.unpark(p.waiter);
                    return LinkedTransferQueue.<E>cast(item);
                }
            }
            Node n = p.next;
            p = (p != n) ? n : (h = head); // Use head if p offlist
        }

        if (how != NOW) {                 // No matches available
            if (s == null)
                s = new Node(e, haveData);
            Node pred = tryAppend(s, haveData);
            if (pred == null)
                continue retry;           // lost race vs opposite mode
            if (how != ASYNC)
                return awaitMatch(s, pred, e, (how == TIMED), nanos);
        }
        return e; // not waiting
    }
}

/**
 * Tries to append node s as tail.
 *
 * @param s the node to append
 * @param haveData true if appending in data mode
 * @return null on failure due to losing race with append in
 * different mode, else s's predecessor, or s itself if no
 * predecessor
 */
private Node tryAppend(Node s, boolean haveData) {
    for (Node t = tail, p = t;;) {        // move p to last node and append
        Node n, u;                        // temps for reads of next & tail
        if (p == null && (p = head) == null) {
            if (casHead(null, s))
                return s;                 // initialize
        }
        else if (p.cannotPrecede(haveData))
            return null;                  // lost race vs opposite mode
        else if ((n = p.next) != null)    // not last; keep traversing
            p = p != t && t != (u = tail) ? (t = u) : // stale tail
                (p != n) ? n : null;      // restart if off list
        else if (!p.casNext(null, s))
            p = p.next;                   // re-read on CAS failure
        else {
            if (p != t) {                 // update if slack now >= 2
                while ((tail != t || !casTail(t, s)) &&
                       (t = tail)   != null &&
                       (s = t.next) != null && // advance and retry
                       (s = s.next) != null && s != t);
            }
            return p;
        }
    }
}

/**
 * Spins/yields/blocks until node s is matched or caller gives up.
 *
 * @param s the waiting node
 * @param pred the predecessor of s, or s itself if it has no
 * predecessor, or null if unknown (the null case does not occur
 * in any current calls but may in possible future extensions)
 * @param e the comparison value for checking match
 * @param timed if true, wait only until timeout elapses
 * @param nanos timeout in nanosecs, used only if timed is true
 * @return matched item, or e if unmatched on interrupt or timeout
 */
private E awaitMatch(Node s, Node pred, E e, boolean timed, long nanos) {
    final long deadline = timed ? System.nanoTime() + nanos : 0L;
    Thread w = Thread.currentThread();
    int spins = -1; // initialized after first item and cancel checks
    ThreadLocalRandom randomYields = null; // bound if needed

    for (;;) {
        Object item = s.item;
        if (item != e) {                  // matched
            // assert item != s;
            s.forgetContents();           // avoid garbage
            return LinkedTransferQueue.<E>cast(item);
        }
        if ((w.isInterrupted() || (timed && nanos <= 0)) &&
                s.casItem(e, s)) {        // cancel
            unsplice(pred, s);
            return e;
        }

        if (spins < 0) {                  // establish spins at/near front
            if ((spins = spinsFor(pred, s.isData)) > 0)
                randomYields = ThreadLocalRandom.current();
        }
        else if (spins > 0) {             // spin
            --spins;
            if (randomYields.nextInt(CHAINED_SPINS) == 0)
                Thread.yield();           // occasionally yield
        }
        else if (s.waiter == null) {
            s.waiter = w;                 // request unpark then recheck
        }
        else if (timed) {
            nanos = deadline - System.nanoTime();
            if (nanos > 0L)
                LockSupport.parkNanos(this, nanos);
        }
        else {
            LockSupport.park(this);
        }
    }
}


                    /**
     * 返回给定前驱节点和数据模式的节点的spin/yield值。参见上述解释。
     */
    private static int spinsFor(Node pred, boolean haveData) {
        if (MP && pred != null) {
            if (pred.isData != haveData)      // 阶段变化
                return FRONT_SPINS + CHAINED_SPINS;
            if (pred.isMatched())             // 可能处于队首
                return FRONT_SPINS;
            if (pred.waiter == null)          // 前驱节点显然在自旋
                return CHAINED_SPINS;
        }
        return 0;
    }

    /* -------------- 遍历方法 -------------- */

    /**
     * 返回p的后继节点，如果p.next已被链接到自身，则返回头节点。这只有在使用已过时的指针遍历时才会为真，该指针现在已不在列表中。
     */
    final Node succ(Node p) {
        Node next = p.next;
        return (p == next) ? head : next;
    }

    /**
     * 返回给定模式的第一个未匹配节点，如果没有则返回null。用于方法isEmpty和hasWaitingConsumer。
     */
    private Node firstOfMode(boolean isData) {
        for (Node p = head; p != null; p = succ(p)) {
            if (!p.isMatched())
                return (p.isData == isData) ? p : null;
        }
        return null;
    }

    /**
     * 由Spliterator使用的firstOfMode版本。调用者在使用返回的节点之前，必须重新检查该节点的item字段是否为null或自链接。
     */
    final Node firstDataNode() {
        for (Node p = head; p != null;) {
            Object item = p.item;
            if (p.isData) {
                if (item != null && item != p)
                    return p;
            }
            else if (item == null)
                break;
            if (p == (p = p.next))
                p = head;
        }
        return null;
    }

    /**
     * 返回第一个未匹配且isData为true的节点中的项，如果没有则返回null。用于peek方法。
     */
    private E firstDataItem() {
        for (Node p = head; p != null; p = succ(p)) {
            Object item = p.item;
            if (p.isData) {
                if (item != null && item != p)
                    return LinkedTransferQueue.<E>cast(item);
            }
            else if (item == null)
                return null;
        }
        return null;
    }

    /**
     * 遍历并计算给定模式的未匹配节点数。用于方法size和getWaitingConsumerCount。
     */
    private int countOfMode(boolean data) {
        int count = 0;
        for (Node p = head; p != null; ) {
            if (!p.isMatched()) {
                if (p.isData != data)
                    return 0;
                if (++count == Integer.MAX_VALUE) // 达到上限
                    break;
            }
            Node n = p.next;
            if (n != p)
                p = n;
            else {
                count = 0;
                p = head;
            }
        }
        return count;
    }

    final class Itr implements Iterator<E> {
        private Node nextNode;   // 下一个要返回项的节点
        private E nextItem;      // 对应的项
        private Node lastRet;    // 最后返回的节点，用于支持remove
        private Node lastPred;   // 用于解除链接lastRet的前驱节点

        /**
         * 移动到prev之后的下一个节点，如果prev为null，则移动到第一个节点。
         */
        private void advance(Node prev) {
            /*
             * 为了跟踪并避免在调用Queue.remove和Itr.remove时删除节点的累积，我们必须在每次前进时包括unsplice和sweep的变体：在Itr.remove时，我们可能需要从lastPred赶上链接，而在其他删除时，我们可能需要跳过过时的节点并解除找到的已删除节点的链接。
             */

            Node r, b; // 在可能删除lastRet时重置lastPred
            if ((r = lastRet) != null && !r.isMatched())
                lastPred = r;    // 下一个lastPred是旧的lastRet
            else if ((b = lastPred) == null || b.isMatched())
                lastPred = null; // 在列表的开始处
            else {
                Node s, n;       // 帮助删除lastPred.next
                while ((s = b.next) != null &&
                       s != b && s.isMatched() &&
                       (n = s.next) != null && n != s)
                    b.casNext(s, n);
            }

            this.lastRet = prev;

            for (Node p = prev, s, n;;) {
                s = (p == null) ? head : p.next;
                if (s == null)
                    break;
                else if (s == p) {
                    p = null;
                    continue;
                }
                Object item = s.item;
                if (s.isData) {
                    if (item != null && item != s) {
                        nextItem = LinkedTransferQueue.<E>cast(item);
                        nextNode = s;
                        return;
                    }
                }
                else if (item == null)
                    break;
                // assert s.isMatched();
                if (p == null)
                    p = s;
                else if ((n = s.next) == null)
                    break;
                else if (s == n)
                    p = null;
                else
                    p.casNext(s, n);
            }
            nextNode = null;
            nextItem = null;
        }

        Itr() {
            advance(null);
        }

        public final boolean hasNext() {
            return nextNode != null;
        }

        public final E next() {
            Node p = nextNode;
            if (p == null) throw new NoSuchElementException();
            E e = nextItem;
            advance(p);
            return e;
        }

        public final void remove() {
            final Node lastRet = this.lastRet;
            if (lastRet == null)
                throw new IllegalStateException();
            this.lastRet = null;
            if (lastRet.tryMatchData())
                unsplice(lastPred, lastRet);
        }
    }


                    /** 自定义的 Spliterators.IteratorSpliterator 变体 */
    static final class LTQSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 1 << 25;  // 批处理数组的最大大小
        final LinkedTransferQueue<E> queue;
        Node current;    // 当前节点；初始化前为 null
        int batch;          // 分割的批处理大小
        boolean exhausted;  // 当没有更多节点时为 true
        LTQSpliterator(LinkedTransferQueue<E> queue) {
            this.queue = queue;
        }

        public Spliterator<E> trySplit() {
            Node p;
            final LinkedTransferQueue<E> q = this.queue;
            int b = batch;
            int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
            if (!exhausted &&
                ((p = current) != null || (p = q.firstDataNode()) != null) &&
                p.next != null) {
                Object[] a = new Object[n];
                int i = 0;
                do {
                    Object e = p.item;
                    if (e != p && (a[i] = e) != null)
                        ++i;
                    if (p == (p = p.next))
                        p = q.firstDataNode();
                } while (p != null && i < n && p.isData);
                if ((current = p) == null)
                    exhausted = true;
                if (i > 0) {
                    batch = i;
                    return Spliterators.spliterator
                        (a, 0, i, Spliterator.ORDERED | Spliterator.NONNULL |
                         Spliterator.CONCURRENT);
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action) {
            Node p;
            if (action == null) throw new NullPointerException();
            final LinkedTransferQueue<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.firstDataNode()) != null)) {
                exhausted = true;
                do {
                    Object e = p.item;
                    if (e != null && e != p)
                        action.accept((E)e);
                    if (p == (p = p.next))
                        p = q.firstDataNode();
                } while (p != null && p.isData);
            }
        }

        @SuppressWarnings("unchecked")
        public boolean tryAdvance(Consumer<? super E> action) {
            Node p;
            if (action == null) throw new NullPointerException();
            final LinkedTransferQueue<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.firstDataNode()) != null)) {
                Object e;
                do {
                    if ((e = p.item) == p)
                        e = null;
                    if (p == (p = p.next))
                        p = q.firstDataNode();
                } while (e == null && p != null && p.isData);
                if ((current = p) == null)
                    exhausted = true;
                if (e != null) {
                    action.accept((E)e);
                    return true;
                }
            }
            return false;
        }

        public long estimateSize() { return Long.MAX_VALUE; }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.NONNULL |
                Spliterator.CONCURRENT;
        }
    }

    /**
     * 返回一个遍历此队列元素的 {@link Spliterator}。
     *
     * <p>返回的 spliterator 是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     *
     * <p>该 {@code Spliterator} 报告 {@link Spliterator#CONCURRENT}，
     * {@link Spliterator#ORDERED} 和 {@link Spliterator#NONNULL}。
     *
     * @implNote
     * 该 {@code Spliterator} 实现了 {@code trySplit} 以允许有限的并行性。
     *
     * @return 一个遍历此队列元素的 {@code Spliterator}
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new LTQSpliterator<E>(this);
    }

    /* -------------- 移除方法 -------------- */

    /**
     * 立即或稍后解除给定已删除/已取消节点与给定前驱节点的链接。
     *
     * @param pred 一个节点，曾被认为是 s 的前驱，或为 null 或 s 本身，如果 s 是/曾是头部
     * @param s 要解除链接的节点
     */
    final void unsplice(Node pred, Node s) {
        s.forgetContents(); // 忘记不需要的字段
        /*
         * 请参见上述解释。简而言之：如果 pred 仍然指向 s，尝试解除 s 的链接。
         * 如果 s 不能被解除链接，因为它是一个尾节点或 pred 可能被解除链接，
         * 并且 pred 和 s 都不是头部或已从列表中移除，增加 sweepVotes，
         * 并且如果累积了足够的票数，进行 sweep。
         */
        if (pred != null && pred != s && pred.next == s) {
            Node n = s.next;
            if (n == null ||
                (n != s && pred.casNext(s, n) && pred.isMatched())) {
                for (;;) {               // 检查是否在头部或可能在头部
                    Node h = head;
                    if (h == pred || h == s || h == null)
                        return;          // 在头部或列表为空
                    if (!h.isMatched())
                        break;
                    Node hn = h.next;
                    if (hn == null)
                        return;          // 现在为空
                    if (hn != h && casHead(h, hn))
                        h.forgetNext();  // 前进头部
                }
                if (pred.next != pred && s.next != s) { // 重新检查是否已从列表中移除
                    for (;;) {           // 如果有足够的票数，立即进行 sweep
                        int v = sweepVotes;
                        if (v < SWEEP_THRESHOLD) {
                            if (casSweepVotes(v, v + 1))
                                break;
                        }
                        else if (casSweepVotes(v, 0)) {
                            sweep();
                            break;
                        }
                    }
                }
            }
        }
    }

                    /**
     * 从头开始遍历，解除匹配（通常是已取消的）节点的链接。
     */
    private void sweep() {
        for (Node p = head, s, n; p != null && (s = p.next) != null; ) {
            if (!s.isMatched())
                // 未匹配的节点永远不会自链接
                p = s;
            else if ((n = s.next) == null) // 尾节点被固定
                break;
            else if (s == n)    // 过时
                // 无需检查 p == s，因为这隐含 s == n
                p = head;
            else
                p.casNext(s, n);
        }
    }

    /**
     * remove(Object) 的主要实现。
     */
    private boolean findAndRemove(Object e) {
        if (e != null) {
            for (Node pred = null, p = head; p != null; ) {
                Object item = p.item;
                if (p.isData) {
                    if (item != null && item != p && e.equals(item) &&
                        p.tryMatchData()) {
                        unsplice(pred, p);
                        return true;
                    }
                }
                else if (item == null)
                    break;
                pred = p;
                if ((p = p.next) == pred) { // 过时
                    pred = null;
                    p = head;
                }
            }
        }
        return false;
    }

    /**
     * 创建一个初始为空的 {@code LinkedTransferQueue}。
     */
    public LinkedTransferQueue() {
    }

    /**
     * 创建一个初始包含给定集合元素的 {@code LinkedTransferQueue}，
     * 元素按集合迭代器的遍历顺序添加。
     *
     * @param c 初始包含的元素集合
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    public LinkedTransferQueue(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    /**
     * 在此队列的尾部插入指定的元素。
     * 由于队列是无界的，此方法永远不会阻塞。
     *
     * @throws NullPointerException 如果指定的元素为 null
     */
    public void put(E e) {
        xfer(e, true, ASYNC, 0);
    }

    /**
     * 在此队列的尾部插入指定的元素。
     * 由于队列是无界的，此方法永远不会阻塞或返回 {@code false}。
     *
     * @return {@code true}（如 {@link java.util.concurrent.BlockingQueue#offer(Object,long,TimeUnit)
     *  BlockingQueue.offer} 所指定）
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offer(E e, long timeout, TimeUnit unit) {
        xfer(e, true, ASYNC, 0);
        return true;
    }

    /**
     * 在此队列的尾部插入指定的元素。
     * 由于队列是无界的，此方法永远不会返回 {@code false}。
     *
     * @return {@code true}（如 {@link Queue#offer} 所指定）
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offer(E e) {
        xfer(e, true, ASYNC, 0);
        return true;
    }

    /**
     * 在此队列的尾部插入指定的元素。
     * 由于队列是无界的，此方法永远不会抛出
     * {@link IllegalStateException} 或返回 {@code false}。
     *
     * @return {@code true}（如 {@link Collection#add} 所指定）
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean add(E e) {
        xfer(e, true, ASYNC, 0);
        return true;
    }

    /**
     * 如果可能，立即将元素传递给等待的消费者。
     *
     * <p>更准确地说，如果存在一个消费者已经在等待接收它（在
     * {@link #take} 或定时的 {@link #poll(long,TimeUnit) poll} 中），
     * 则立即传递指定的元素，否则不将元素入队并返回 {@code false}。
     *
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean tryTransfer(E e) {
        return xfer(e, true, NOW, 0) == null;
    }

    /**
     * 等待必要的时间将元素传递给消费者。
     *
     * <p>更准确地说，如果存在一个消费者已经在等待接收它（在
     * {@link #take} 或定时的 {@link #poll(long,TimeUnit) poll} 中），
     * 则立即传递指定的元素，否则将指定的元素插入此队列的尾部
     * 并等待直到元素被消费者接收。
     *
     * @throws NullPointerException 如果指定的元素为 null
     */
    public void transfer(E e) throws InterruptedException {
        if (xfer(e, true, SYNC, 0) != null) {
            Thread.interrupted(); // 只有在中断时才可能失败
            throw new InterruptedException();
        }
    }

    /**
     * 如果在超时前可以传递元素，则将元素传递给消费者。
     *
     * <p>更准确地说，如果存在一个消费者已经在等待接收它（在
     * {@link #take} 或定时的 {@link #poll(long,TimeUnit) poll} 中），
     * 则立即传递指定的元素，否则将指定的元素插入此队列的尾部
     * 并等待直到元素被消费者接收，如果指定的等待时间在元素可以传递之前耗尽，
     * 则返回 {@code false}。
     *
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean tryTransfer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (xfer(e, true, TIMED, unit.toNanos(timeout)) == null)
            return true;
        if (!Thread.interrupted())
            return false;
        throw new InterruptedException();
    }

    public E take() throws InterruptedException {
        E e = xfer(null, false, SYNC, 0);
        if (e != null)
            return e;
        Thread.interrupted();
        throw new InterruptedException();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E e = xfer(null, false, TIMED, unit.toNanos(timeout));
        if (e != null || !Thread.interrupted())
            return e;
        throw new InterruptedException();
    }


public E poll() {
    return xfer(null, false, NOW, 0);
}

/**
 * @throws NullPointerException     {@inheritDoc}
 * @throws IllegalArgumentException {@inheritDoc}
 */
public int drainTo(Collection<? super E> c) {
    if (c == null)
        throw new NullPointerException();
    if (c == this)
        throw new IllegalArgumentException();
    int n = 0;
    for (E e; (e = poll()) != null;) {
        c.add(e);
        ++n;
    }
    return n;
}

/**
 * @throws NullPointerException     {@inheritDoc}
 * @throws IllegalArgumentException {@inheritDoc}
 */
public int drainTo(Collection<? super E> c, int maxElements) {
    if (c == null)
        throw new NullPointerException();
    if (c == this)
        throw new IllegalArgumentException();
    int n = 0;
    for (E e; n < maxElements && (e = poll()) != null;) {
        c.add(e);
        ++n;
    }
    return n;
}

/**
 * 返回一个按适当顺序遍历此队列中元素的迭代器。
 * 元素将按从第一个（头）到最后一个（尾）的顺序返回。
 *
 * <p>返回的迭代器是
 * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>的。
 *
 * @return 一个按适当顺序遍历此队列中元素的迭代器
 */
public Iterator<E> iterator() {
    return new Itr();
}

public E peek() {
    return firstDataItem();
}

/**
 * 如果此队列不包含任何元素，则返回 {@code true}。
 *
 * @return 如果此队列不包含任何元素，则返回 {@code true}
 */
public boolean isEmpty() {
    for (Node p = head; p != null; p = succ(p)) {
        if (!p.isMatched())
            return !p.isData;
    }
    return true;
}

public boolean hasWaitingConsumer() {
    return firstOfMode(false) != null;
}

/**
 * 返回此队列中的元素数量。如果此队列包含的元素多于 {@code Integer.MAX_VALUE}，则返回
 * {@code Integer.MAX_VALUE}。
 *
 * <p>请注意，与大多数集合不同，此方法
 * <em>不是</em> 常数时间操作。由于这些队列的异步性质，确定当前元素数量需要 O(n) 遍历。
 *
 * @return 此队列中的元素数量
 */
public int size() {
    return countOfMode(true);
}

public int getWaitingConsumerCount() {
    return countOfMode(false);
}

/**
 * 如果此队列包含指定元素的一个实例，则从队列中移除该实例。
 * 更正式地说，移除一个元素 {@code e}，使得 {@code o.equals(e)}，如果此队列包含一个或多个这样的元素。
 * 如果此队列包含指定元素（或等效地，如果此队列因调用而改变），则返回 {@code true}。
 *
 * @param o 要从队列中移除的元素（如果存在）
 * @return 如果此队列因调用而改变，则返回 {@code true}
 */
public boolean remove(Object o) {
    return findAndRemove(o);
}

/**
 * 如果此队列包含指定元素，则返回 {@code true}。
 * 更正式地说，如果且仅当此队列包含至少一个元素 {@code e} 使得 {@code o.equals(e)}，则返回 {@code true}。
 *
 * @param o 要检查是否包含在此队列中的对象
 * @return 如果此队列包含指定元素，则返回 {@code true}
 */
public boolean contains(Object o) {
    if (o == null) return false;
    for (Node p = head; p != null; p = succ(p)) {
        Object item = p.item;
        if (p.isData) {
            if (item != null && item != p && o.equals(item))
                return true;
        }
        else if (item == null)
            break;
    }
    return false;
}

/**
 * 始终返回 {@code Integer.MAX_VALUE}，因为 {@code LinkedTransferQueue} 没有容量限制。
 *
 * @return {@code Integer.MAX_VALUE}（如 {@link java.util.concurrent.BlockingQueue#remainingCapacity()
 *         BlockingQueue.remainingCapacity} 所指定）
 */
public int remainingCapacity() {
    return Integer.MAX_VALUE;
}

/**
 * 将此队列保存到流中（即序列化它）。
 *
 * @param s 流
 * @throws java.io.IOException 如果发生 I/O 错误
 * @serialData 所有元素（每个都是 {@code E}）按适当顺序，最后是一个 null
 */
private void writeObject(java.io.ObjectOutputStream s)
    throws java.io.IOException {
    s.defaultWriteObject();
    for (E e : this)
        s.writeObject(e);
    // 使用尾部的 null 作为哨兵
    s.writeObject(null);
}

/**
 * 从流中恢复此队列（即反序列化它）。
 * @param s 流
 * @throws ClassNotFoundException 如果无法找到序列化对象的类
 * @throws java.io.IOException 如果发生 I/O 错误
 */
private void readObject(java.io.ObjectInputStream s)
    throws java.io.IOException, ClassNotFoundException {
    s.defaultReadObject();
    for (;;) {
        @SuppressWarnings("unchecked")
        E item = (E) s.readObject();
        if (item == null)
            break;
        else
            offer(item);
    }
}

// Unsafe 机制

private static final sun.misc.Unsafe UNSAFE;
private static final long headOffset;
private static final long tailOffset;
private static final long sweepVotesOffset;
static {
    try {
        UNSAFE = sun.misc.Unsafe.getUnsafe();
        Class<?> k = LinkedTransferQueue.class;
        headOffset = UNSAFE.objectFieldOffset
            (k.getDeclaredField("head"));
        tailOffset = UNSAFE.objectFieldOffset
            (k.getDeclaredField("tail"));
        sweepVotesOffset = UNSAFE.objectFieldOffset
            (k.getDeclaredField("sweepVotes"));
    } catch (Exception e) {
        throw new Error(e);
    }
}
