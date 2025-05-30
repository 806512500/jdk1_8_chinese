
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
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
 * 该队列按 FIFO（先进先出）顺序排列任何给定生产者的数据。队列的 <em>头部</em> 是某个生产者在队列中停留时间最长的元素。
 * 队列的 <em>尾部</em> 是某个生产者在队列中停留时间最短的元素。
 *
 * <p>请注意，与大多数集合不同，{@code size} 方法 <em>不是</em> 常量时间操作。由于这些队列的异步性质，
 * 确定当前元素数量需要遍历元素，因此如果在遍历过程中修改了此集合，可能会报告不准确的结果。
 * 此外，批量操作 {@code addAll}、{@code removeAll}、{@code retainAll}、{@code containsAll}、
 * {@code equals} 和 {@code toArray} <em>不保证</em> 原子性。例如，一个在并发执行 {@code addAll} 操作时的迭代器
 * 可能只能看到部分添加的元素。
 *
 * <p>此类及其迭代器实现了 {@link Collection} 和 {@link Iterator} 接口中所有 <em>可选</em> 的方法。
 *
 * <p>内存一致性效果：与其他并发集合一样，线程在将对象放入 {@code LinkedTransferQueue} 之前的操作
 * <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 在其他线程中访问或移除该元素后的操作。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.7
 * @author Doug Lea
 * @param <E> 该集合中元素的类型
 */
public class LinkedTransferQueue<E> extends AbstractQueue<E>
    implements TransferQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -3223113410248163686L;

    /*
     * *** 双队列与松弛 ***
     *
     * 双队列由 Scherer 和 Scott 引入
     * (http://www.cs.rice.edu/~wns1/papers/2004-DISC-DDS.pdf)，是其中节点可以表示数据或请求的（链接）队列。
     * 当一个线程尝试将数据节点入队，但遇到请求节点时，它会“匹配”并移除该请求节点；反之亦然。阻塞双队列安排
 * 使入队未匹配请求的线程在其他线程提供匹配之前阻塞。双同步队列（参见 Scherer, Lea, & Scott
 * http://www.cs.rochester.edu/u/scott/papers/2009_Scherer_CACM_SSQ.pdf）进一步安排使入队未匹配数据的线程也阻塞。
 * 双传输队列支持所有这些模式，由调用者决定。
 *
 * 一个 FIFO 双队列可以使用 Michael & Scott (M&S) 无锁队列算法的变体实现
 * (http://www.cs.rochester.edu/u/scott/papers/1996_PODC_queues.pdf)。它维护两个指针字段，“head”，指向一个
 * （已匹配的）节点，该节点又指向第一个实际（未匹配的）队列节点（或为空）；以及“tail”，指向队列的最后一个节点（或再次为空）。
 * 例如，以下是一个可能的包含四个数据元素的队列：
 *
 *  head                tail
 *    |                   |
 *    v                   v
 *    M -> U -> U -> U -> U
 *
 * M&S 队列算法已知在维护（通过 CAS）这些头和尾指针时存在可扩展性和开销限制。这导致开发了减少竞争的变体，如消除数组
 * （参见 Moir 等 http://portal.acm.org/citation.cfm?id=1074013）和乐观后指针
 * （参见 Ladan-Mozes & Shavit http://people.csail.mit.edu/edya/publications/OptimisticFIFOQueue-journal.pdf）。
 * 然而，双队列的性质使在需要双性时改进 M&S 风格实现的策略更为简单。
 *
 * 在双队列中，每个节点必须原子地维护其匹配状态。虽然有其他可能的变体，但这里我们实现为：对于数据模式节点，匹配涉及
 * 通过 CAS 将“item”字段从非空数据值更改为 null 以匹配，反之亦然，对于请求节点，通过 CAS 从 null 更改为数据值。
 * （请注意，这种风格队列的线性化属性很容易验证——元素通过链接变得可用，通过匹配变得不可用。）与普通的 M&S 队列相比，
 * 这种双队列的性质要求每次入队/出队对需要一个额外的成功的原子操作。但它也使队列维护机制的低成本变体成为可能。
 * （即使对于支持删除内部元素的非双队列，如 j.u.c.ConcurrentLinkedQueue，这种想法的变体也适用。）
 *
 * 一旦节点匹配，其匹配状态将永远不会再次改变。因此，我们可以安排链接列表包含零个或多个匹配节点的前缀，
 * 后跟零个或多个未匹配节点的后缀。（请注意，我们允许前缀和后缀都为零长度，这意味着我们不使用虚拟头节点。）
 * 如果我们不关心时间和空间效率，我们可以通过从指向初始节点的指针遍历来正确执行入队和出队操作；
 * 在匹配时 CAS 更改第一个未匹配节点的 item 字段，在追加时 CAS 更改尾节点的 next 字段。（加上一些特殊情况处理，如初始为空。）
 * 虽然这本身是一个糟糕的主意，但它有一个好处，即不需要对头/尾字段进行任何原子更新。
 *
 * 我们在这里引入了一种介于从不更新队列（头和尾）指针与总是更新之间的方法。这在有时需要额外的遍历步骤来定位第一个和/或最后一个未匹配节点
 * 与减少队列指针更新的开销和竞争之间提供了权衡。例如，一个可能的队列快照是：
 *
 *  head           tail
 *    |              |
 *    v              v
 *    M -> M -> U -> U -> U -> U
 *
 * “松弛”（头值与第一个未匹配节点之间的目标最大距离，以及尾值与最后一个未匹配节点之间的目标最大距离）的最佳值是一个经验问题。
 * 我们发现，在各种平台上，使用 1-3 之间的非常小的常数效果最好。更大的值会增加缓存未命中和长遍历链的风险，而更小的值会增加 CAS 竞争和开销。
 *
 * 带有松弛的双队列与普通的 M&S 双队列不同，因为它们在匹配、追加或甚至遍历节点时有时会更新头或尾指针，以维持目标松弛。
 * “有时”这个概念可以通过几种方式实现。最简单的是使用每次遍历步骤递增的操作计数器，并在计数超过阈值时尝试（通过 CAS）更新相关队列指针。
 * 另一种需要更多开销的方法是使用随机数生成器，以给定的概率在每次遍历步骤中更新。
 *
 * 在这些策略中的任何一种中，由于更新字段的 CAS 可能会失败，实际松弛可能会超过目标松弛。然而，它们可以在任何时候重试以维持目标。
 * 即使使用非常小的松弛值，这种方法也适用于双队列，因为它允许所有操作在匹配或追加项目之前（从而可能允许另一个线程取得进展）都是只读的，
 * 从而不引入进一步的竞争。如下所述，我们通过在这些点之后执行松弛维护重试来实现这一点。
 *
 * 作为这些技术的补充，可以通过减少遍历开销而不增加头指针更新的竞争：线程有时可以将从当前“头”节点到当前已知第一个未匹配节点的“next”链接路径
 * 缩短，同样适用于尾部。同样，这可以通过使用阈值或随机化来触发。
 *
 * 这些想法必须进一步扩展以避免由从旧被遗忘的头节点开始的节点的顺序“next”链接引起的大量难以回收的垃圾：
 * 如 Boehm 首次详细描述的那样（http://portal.acm.org/citation.cfm?doid=503272.503282），如果 GC 延迟注意到任意旧节点已变为垃圾，
 * 所有更新的死节点也将未被回收。（在非 GC 环境中也会出现类似的问题。）为了在我们的实现中应对这一点，在 CAS 以推进头指针时，
 * 我们将前一个头的“next”链接设置为仅指向自身；从而限制了连接的死列表的长度。（我们还采取类似的措施来清除可能保留垃圾的其他 Node 字段的值。）
 * 然而，这样做增加了遍历的复杂性：如果任何“next”指针链接到自身，这表明当前线程落后于头更新，因此遍历必须从“头”继续。
 * 从“尾”开始尝试找到当前尾的遍历也可能遇到自链接，在这种情况下它们也从“头”继续。
 *
 * 在基于松弛的方案中，诱使人们在更新时甚至不使用 CAS（类似于 Ladan-Mozes & Shavit）。然而，这不能用于头更新，
 * 因为在上述链接遗忘机制下，更新可能会将头留在一个已分离的节点上。虽然可以直接写入尾更新，但会增加长重遍历的风险，
 * 从而增加长垃圾链，这可能比值得的代价更高，考虑到执行 CAS 与写入的成本差异较小，尤其是在它们不是每次操作都触发时
 * （特别是考虑到写入和 CAS 同样需要额外的 GC 会计（“写屏障”），这有时比写入本身更昂贵，因为竞争。）
 *
 * *** 实现概述 ***
 *
 * 我们使用基于阈值的方法进行更新，松弛阈值为两个——也就是说，当当前指针看起来与第一个/最后一个节点相距两个或更多步骤时，我们更新头/尾。
 * 松弛值是硬编码的：路径大于一是通过检查遍历指针的等价性自然实现的，除非列表只有一个元素，此时我们保持松弛阈值为一。
 * 避免在方法调用之间跟踪显式计数稍微简化了一个已经复杂的实现。如果有一个低质量且廉价的每线程随机数生成器可用，使用随机化可能会更好，
 * 但即使是 ThreadLocalRandom 对于这些目的来说也太重了。
 *
 * 使用如此小的松弛阈值，除了在取消/删除（见下文）的情况下，不值得通过路径短路（即取消内部节点的链接）来增强这一点。
 *
 * 在任何节点入队之前，我们允许头和尾字段都为 null；在第一次追加时初始化。这简化了其他一些逻辑，以及提供了更高效的显式控制路径，
 * 而不是让 JVM 在它们为 null 时插入隐式 NullPointerException。虽然目前没有完全实现，我们还保留了在为空时重新设置这些字段为 null 的可能性
 * （这很复杂，但几乎没有好处。）
 *
 * 所有入队/出队操作都由单个方法“xfer”处理，参数指示是否以某种形式的 offer、put、poll、take 或 transfer（每个可能带有超时）进行操作。
 * 使用一个单一的大型方法的相对复杂性超过了使用每个情况的单独方法的代码体积和维护问题。
 *
 * 操作由多达三个阶段组成。第一阶段在方法 xfer 内实现，第二阶段在 tryAppend 方法中实现，第三阶段在 awaitMatch 方法中实现。
 *
 * 1. 尝试匹配现有节点
 *
 *    从头开始，跳过已匹配的节点，直到找到一个相反模式的未匹配节点（如果存在），在这种情况下匹配它并返回，
 *    如果需要，更新头以指向匹配节点之后的一个节点（或如果列表没有其他未匹配节点，则指向该节点本身）。
 *    如果 CAS 失败，则循环重试，将头推进两个步骤，直到成功或松弛最多为两个。通过要求每次尝试推进头两个步骤（如果适用），
 *    我们确保松弛不会无限制地增长。遍历还检查初始头是否已脱离列表，在这种情况下它们从新头开始。
 *
 *    如果没有找到候选节点且调用是未定时的 poll/offer（参数“how”为 NOW），则返回。
 *
 * 2. 尝试追加新节点（方法 tryAppend）
 *
 *    从当前尾指针开始，找到实际的最后一个节点并尝试追加一个新节点（或如果头为 null，则建立第一个节点）。
 *    节点只能在其前驱节点已匹配或模式相同的情况下追加。如果我们在遍历过程中检测到其他情况，则必须在阶段 1 重新开始。
 *    遍历和更新步骤与阶段 1 类似：在 CAS 失败时重试并检查陈旧性。特别是，如果遇到自链接，则可以通过从当前头继续遍历来安全地跳到列表上的节点。
 *
 *    在成功追加后，如果调用是 ASYNC，则返回。
 *
 * 3. 等待匹配或取消（方法 awaitMatch）
 *
 *    等待另一个线程匹配节点；如果当前线程被中断或等待超时，则取消。在多处理器上，我们使用队列前端自旋：
 *    如果节点看起来是队列中的第一个未匹配节点，它会在阻塞前自旋一段时间。在任何情况下，在阻塞前，它会尝试取消当前“头”和第一个未匹配节点之间的任何节点的链接。
 *
 *    队列前端自旋极大地提高了高度竞争队列的性能。只要自旋相对较短且“安静”，自旋对不太竞争的队列的性能影响不大。
 *    在自旋期间，线程会检查其中断状态并生成一个线程本地随机数，以决定偶尔执行 Thread.yield。虽然 yield 的规格不明确，
 *    但我们假设它可能有帮助，而且不会对忙碌系统上的自旋影响造成损害。我们还为不是已知前端但其前驱节点尚未阻塞的节点使用较小（1/2）的自旋——
 *    这些“链接”自旋避免了前端规则导致的节点交替自旋与阻塞。此外，表示阶段变化（从数据节点到请求节点或反之亦然）的前端线程
 *    相对于其前驱节点会收到额外的链接自旋，反映了在阶段变化期间解除线程阻塞通常需要的更长路径。
 *
 *
 * ** 取消内部节点的链接 **
 *
 * 除了通过自链接最小化垃圾保留（如上所述），我们还取消已删除内部节点的链接。这些节点可能由于超时或中断的等待、
 * 或调用 remove(x) 或 Iterator.remove 而产生。通常，给定一个节点，该节点曾是某个要删除的节点 s 的前驱节点，我们可以通过 CAS
 * 更改其前驱节点的 next 字段（如果它仍然指向 s）来取消 s 的链接（否则 s 必须已经被删除或已脱离列表）。但有以下两种情况我们不能保证以这种方式使节点 s 不可达：
 * (1) 如果 s 是列表的尾节点（即，next 为 null），则它作为追加的目标节点被固定，因此只能在其他节点追加后稍后删除。
 * (2) 我们不能保证取消已匹配（包括已取消）的前驱节点的链接：前驱节点可能已经被取消链接，因此某些先前可达的节点可能仍然指向 s。
 * （进一步解释参见 Herlihy & Shavit "The Art of Multiprocessor Programming" 第 9 章）。然而，在这两种情况下，如果 s 或其前驱节点
 * （或可以使其）在列表头部或从列表头部脱离，我们可以排除进一步操作的需要。
 *
 * 如果不考虑这些情况，可能会导致大量已删除节点仍然可达。导致此类累积的情况虽然不常见，但在实践中可能会发生；
 * 例如，当一系列短超时调用 poll 重复超时但从未因队列前端的未定时调用 take 而脱离列表时。
 *
 * 当这些情况出现时，而不是总是重新遍历整个列表以找到实际的前驱节点来取消链接（这在情况 (1) 中无论如何也不会有帮助），
 * 我们记录可能的取消链接失败的保守估计（在“sweepVotes”中）。当估计值超过阈值（“SWEEP_THRESHOLD”）时，我们触发一次完整扫描，
 * 取消在初始删除时未取消链接的已取消节点的链接。我们由触发阈值的线程执行扫描（而不是后台线程或分配给其他线程），
 * 因为在删除发生的主上下文中，调用者已经超时、取消或执行可能的 O(n) 操作（例如 remove(x)），这些操作都不够时间关键，
 * 无法承担替代方案对其他线程造成的开销。
 *
 * 由于扫描投票估计是保守的，节点会随着它们从列表头部脱离而“自然”取消链接，以及我们在扫描进行时允许投票累积，
 * 通常这样的节点比估计的要少得多。阈值的选择平衡了浪费努力和竞争的可能性，以及在静止队列中保留内部节点的最坏情况界限。
 * 下面定义的值是通过各种超时场景的经验选择的，以平衡这些因素。
 *
 * 请注意，我们不能在扫描期间取消内部节点的链接。然而，相关的垃圾链在某个后继节点最终从列表头部脱离并自链接时终止。
 */


                /** True if on multiprocessor */
    private static final boolean MP =
        Runtime.getRuntime().availableProcessors() > 1;

    /**
     * 在多处理器上，节点在队列中显然是第一个等待者时，在阻塞前旋转（带有随机插入的调用
     * Thread.yield）的次数。请参见上述解释。该值必须是2的幂。该值是通过经验得出的——
     * 它在各种处理器、CPU数量和操作系统上表现良好。
     */
    private static final int FRONT_SPINS   = 1 << 7;

    /**
     * 当节点在队列中被另一个显然是在旋转的节点所前置时，在阻塞前旋转的次数。
     * 也作为FRONT_SPINS在阶段变化时的增量，以及在旋转期间调用yield的基平均频率。
     * 必须是2的幂。
     */
    private static final int CHAINED_SPINS = FRONT_SPINS >>> 1;

    /**
     * 在队列中扫描并取消未在初始移除时取消链接的已取消节点之前，可以容忍的估计移除失败次数（sweepVotes）。
     * 请参见上述解释。该值必须至少为2，以避免在移除尾节点时进行无用的扫描。
     */
    static final int SWEEP_THRESHOLD = 32;

    /**
     * 队列节点。使用Object而不是E作为项，以便在使用后忘记它们。
     * 重度依赖Unsafe机制以最小化不必要的顺序约束：写入操作在与其他访问或CAS操作内在有序时使用简单的放松形式。
     */
    static final class Node {
        final boolean isData;   // 如果这是请求节点，则为false
        volatile Object item;   // 如果是isData，则最初非空；CAS到匹配
        volatile Node next;
        volatile Thread waiter; // 直到等待前为null

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
         * 将item设置为self并将waiter设置为null，以避免在匹配或取消后保留垃圾。
         * 使用放松写入，因为顺序在唯一调用上下文中已经受约束：item在提取项的volatile/原子机制之后被忘记。
         * 同样，清除waiter跟随CAS或从park返回（如果曾经parked；否则我们不在乎）。
         */
        final void forgetContents() {
            UNSAFE.putObject(this, itemOffset, this);
            UNSAFE.putObject(this, waiterOffset, null);
        }

        /**
         * 如果此节点已被匹配，包括由于取消而进行的人工匹配，则返回true。
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
         * 尝试人为匹配数据节点——用于移除。
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

    /** 显然失败的未解除链接的节点的数量 */
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
     * xfer方法中“how”参数的可能值。
     */
    private static final int NOW   = 0; // 用于非定时的poll，tryTransfer
    private static final int ASYNC = 1; // 用于offer，put，add
    private static final int SYNC  = 2; // 用于transfer，take
    private static final int TIMED = 3; // 用于定时的poll，tryTransfer

    @SuppressWarnings("unchecked")
    static <E> E cast(Object item) {
        // assert item == null || item.getClass() != Node.class;
        return (E) item;
    }

    /**
     * 实现所有排队方法。请参见上述解释。
     *
     * @param e 项或null（用于take）
     * @param haveData 如果这是put，则为true，否则为take
     * @param how NOW, ASYNC, SYNC, 或 TIMED
     * @param nanos 超时时间（纳秒），仅在模式为TIMED时使用
     * @return 如果匹配则返回项，否则返回e
     * @throws NullPointerException 如果是haveData模式但e为null
     */
    private E xfer(E e, boolean haveData, int how, long nanos) {
        if (haveData && (e == null))
            throw new NullPointerException();
        Node s = null;                        // 如果需要，要追加的节点

        retry:
        for (;;) {                            // 在追加竞争时重新开始

            for (Node h = head, p = h; p != null;) { // 查找并匹配第一个节点
                boolean isData = p.isData;
                Object item = p.item;
                if (item != p && (item != null) == isData) { // 未匹配
                    if (isData == haveData)   // 无法匹配
                        break;
                    if (p.casItem(item, e)) { // 匹配
                        for (Node q = p; q != h;) {
                            Node n = q.next;  // 如果不是单例，则更新2个
                            if (head == h && casHead(h, n == null ? q : n)) {
                                h.forgetNext();
                                break;
                            }                 // 前进并重试
                            if ((h = head)   == null ||
                                (q = h.next) == null || !q.isMatched())
                                break;        // 除非松弛度 < 2
                        }
                        LockSupport.unpark(p.waiter);
                        return LinkedTransferQueue.<E>cast(item);
                    }
                }
                Node n = p.next;
                p = (p != n) ? n : (h = head); // 如果p不在列表中，使用head
            }

            if (how != NOW) {                 // 没有可用的匹配
                if (s == null)
                    s = new Node(e, haveData);
                Node pred = tryAppend(s, haveData);
                if (pred == null)
                    continue retry;           // 输给了不同模式的追加竞争
                if (how != ASYNC)
                    return awaitMatch(s, pred, e, (how == TIMED), nanos);
            }
            return e; // 不等待
        }
    }

    /**
     * 尝试将节点s追加为尾部。
     *
     * @param s 要追加的节点
     * @param haveData 如果以数据模式追加，则为true
     * @return 如果由于输给了不同模式的追加竞争而失败，则返回null，否则返回s的前驱，或s本身如果没有前驱
     */
    private Node tryAppend(Node s, boolean haveData) {
        for (Node t = tail, p = t;;) {        // 移动p到最后一个节点并追加
            Node n, u;                        // 用于读取next和tail的临时变量
            if (p == null && (p = head) == null) {
                if (casHead(null, s))
                    return s;                 // 初始化
            }
            else if (p.cannotPrecede(haveData))
                return null;                  // 输给了不同模式的追加竞争
            else if ((n = p.next) != null)    // 不是最后一个；继续遍历
                p = p != t && t != (u = tail) ? (t = u) : // 过时的tail
                    (p != n) ? n : null;      // 重新开始如果不在列表中
            else if (!p.casNext(null, s))
                p = p.next;                   // 在CAS失败时重新读取
            else {
                if (p != t) {                 // 如果松弛度现在 >= 2，则更新
                    while ((tail != t || !casTail(t, s)) &&
                           (t = tail)   != null &&
                           (s = t.next) != null && // 前进并重试
                           (s = s.next) != null && s != t);
                }
                return p;
            }
        }
    }

    /**
     * 旋转/让出/阻塞，直到节点s匹配或调用者放弃。
     *
     * @param s 等待的节点
     * @param pred s的前驱，或s本身如果它没有前驱，或null如果未知（当前调用中不会出现null情况，但可能在未来的扩展中出现）
     * @param e 用于检查匹配的比较值
     * @param timed 如果为true，仅等待直到超时
     * @param nanos 超时时间（纳秒），仅在timed为true时使用
     * @return 匹配的项，或在中断或超时时未匹配的e
     */
    private E awaitMatch(Node s, Node pred, E e, boolean timed, long nanos) {
        final long deadline = timed ? System.nanoTime() + nanos : 0L;
        Thread w = Thread.currentThread();
        int spins = -1; // 在第一次项和取消检查后初始化
        ThreadLocalRandom randomYields = null; // 如果需要则绑定

        for (;;) {
            Object item = s.item;
            if (item != e) {                  // 匹配
                // assert item != s;
                s.forgetContents();           // 避免垃圾
                return LinkedTransferQueue.<E>cast(item);
            }
            if ((w.isInterrupted() || (timed && nanos <= 0)) &&
                    s.casItem(e, s)) {        // 取消
                unsplice(pred, s);
                return e;
            }

            if (spins < 0) {                  // 在前端附近建立旋转
                if ((spins = spinsFor(pred, s.isData)) > 0)
                    randomYields = ThreadLocalRandom.current();
            }
            else if (spins > 0) {             // 旋转
                --spins;
                if (randomYields.nextInt(CHAINED_SPINS) == 0)
                    Thread.yield();           // 偶尔让出
            }
            else if (s.waiter == null) {
                s.waiter = w;                 // 请求unpark然后重新检查
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
     * 返回给定前驱和数据模式的节点的旋转/让出值。请参见上述解释。
     */
    private static int spinsFor(Node pred, boolean haveData) {
        if (MP && pred != null) {
            if (pred.isData != haveData)      // 阶段变化
                return FRONT_SPINS + CHAINED_SPINS;
            if (pred.isMatched())             // 可能在前端
                return FRONT_SPINS;
            if (pred.waiter == null)          // 前驱显然在旋转
                return CHAINED_SPINS;
        }
        return 0;
    }

    /* -------------- 遍历方法 -------------- */

    /**
     * 返回p的后继，或如果p.next已链接到自身，则返回头节点，这只会当使用过时的指针遍历时为true，该指针现在不在列表中。
     */
    final Node succ(Node p) {
        Node next = p.next;
        return (p == next) ? head : next;
    }

    /**
     * 返回给定模式的第一个未匹配节点，或如果不存在则返回null。用于方法isEmpty，hasWaitingConsumer。
     */
    private Node firstOfMode(boolean isData) {
        for (Node p = head; p != null; p = succ(p)) {
            if (!p.isMatched())
                return (p.isData == isData) ? p : null;
        }
        return null;
    }

    /**
     * 用于Spliterator的firstOfMode版本。调用者在使用前必须重新检查返回节点的item字段是否为null或自链接。
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
     * 返回isData的第一个未匹配节点中的项，或如果不存在则返回null。用于peek。
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
     * 遍历并计数给定模式的未匹配节点。
     * 由 size 和 getWaitingConsumerCount 方法使用。
     */
    private int countOfMode(boolean data) {
        int count = 0;
        for (Node p = head; p != null; ) {
            if (!p.isMatched()) {
                if (p.isData != data)
                    return 0;
                if (++count == Integer.MAX_VALUE) // 饱和
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
        private Node nextNode;   // 下一个要返回的节点
        private E nextItem;      // 对应的项
        private Node lastRet;    // 最后返回的节点，用于支持 remove
        private Node lastPred;   // 用于取消链接 lastRet 的前驱

        /**
         * 移动到 prev 之后的下一个节点，如果 prev 为 null，则移动到第一个节点。
         */
        private void advance(Node prev) {
            /*
             * 为了跟踪并避免在调用 Queue.remove 和 Itr.remove 时删除节点的累积，
             * 我们必须在每次 advance 时包含 unsplice 和 sweep 的变体：在 Itr.remove 时，
             * 我们可能需要从 lastPred 捕获链接，而在其他删除时，我们可能需要跳过陈旧节点并取消链接在前进时发现的已删除节点。
             */

            Node r, b; // 在可能删除 lastRet 时重置 lastPred
            if ((r = lastRet) != null && !r.isMatched())
                lastPred = r;    // 下一个 lastPred 是旧的 lastRet
            else if ((b = lastPred) == null || b.isMatched())
                lastPred = null; // 在列表的开头
            else {
                Node s, n;       // 帮助删除 lastPred.next
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
        static final int MAX_BATCH = 1 << 25;  // 最大批量数组大小
        final LinkedTransferQueue<E> queue;
        Node current;    // 当前节点；初始化前为 null
        int batch;          // 分割的批量大小
        boolean exhausted;  // 为 true 时没有更多节点
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
     * <p>{@code Spliterator} 报告 {@link Spliterator#CONCURRENT}，
     * {@link Spliterator#ORDERED} 和 {@link Spliterator#NONNULL}。
     *
     * @implNote
     * {@code Spliterator} 实现 {@code trySplit} 以允许有限的并行性。
     *
     * @return 一个遍历此队列元素的 {@code Spliterator}
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new LTQSpliterator<E>(this);
    }

    /* -------------- 删除方法 -------------- */

    /**
     * 取消链接（现在或稍后）给定的已删除/已取消的节点与给定的前驱节点。
     *
     * @param pred 一个节点，曾被认为是 s 的前驱，或者为 null 或 s 本身，如果 s 是/曾经是头节点
     * @param s 要取消链接的节点
     */
    final void unsplice(Node pred, Node s) {
        s.forgetContents(); // 忘记不需要的字段
        /*
         * 参见上述理由。简而言之：如果 pred 仍然指向 s，尝试取消链接 s。如果 s 不能被取消链接，因为它是尾节点或 pred 可能被取消链接，
         * 并且 pred 和 s 都不是头节点或不在列表中，增加 sweepVotes，并且如果累积了足够的票数，进行 sweep。
         */
        if (pred != null && pred != s && pred.next == s) {
            Node n = s.next;
            if (n == null ||
                (n != s && pred.casNext(s, n) && pred.isMatched())) {
                for (;;) {               // 检查是否在头节点或可能在头节点
                    Node h = head;
                    if (h == pred || h == s || h == null)
                        return;          // 在头节点或列表为空
                    if (!h.isMatched())
                        break;
                    Node hn = h.next;
                    if (hn == null)
                        return;          // 现在为空
                    if (hn != h && casHead(h, hn))
                        h.forgetNext();  // 前进头节点
                }
                if (pred.next != pred && s.next != s) { // 重新检查是否在列表中
                    for (;;) {           // 如果有足够的票数，现在进行 sweep
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
     * 从头节点遍历并取消链接匹配（通常是已取消）的节点。
     */
    private void sweep() {
        for (Node p = head, s, n; p != null && (s = p.next) != null; ) {
            if (!s.isMatched())
                // 未匹配的节点永远不会自链接
                p = s;
            else if ((n = s.next) == null) // 尾节点被固定
                break;
            else if (s == n)    // 陈旧
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
                if ((p = p.next) == pred) { // 陈旧
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
     * @param c 要初始包含的元素集合
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    public LinkedTransferQueue(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    /**
     * 在此队列的尾部插入指定的元素。
     * 由于队列是无界的，此方法将永远不会阻塞。
     *
     * @throws NullPointerException 如果指定的元素为 null
     */
    public void put(E e) {
        xfer(e, true, ASYNC, 0);
    }

    /**
     * 在此队列的尾部插入指定的元素。
     * 由于队列是无界的，此方法将永远不会阻塞或返回 {@code false}。
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
     * 由于队列是无界的，此方法将永远不会返回 {@code false}。
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
     * 由于队列是无界的，此方法将永远不会抛出 {@link IllegalStateException} 或返回 {@code false}。
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
     * {@link #take} 或定时 {@link #poll(long,TimeUnit) poll} 中），
     * 则立即将指定的元素传递给该消费者，否则不将元素入队并返回 {@code false}。
     *
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean tryTransfer(E e) {
        return xfer(e, true, NOW, 0) == null;
    }

    /**
     * 如果需要，等待将元素传递给消费者。
     *
     * <p>更准确地说，如果存在一个消费者已经在等待接收它（在
     * {@link #take} 或定时 {@link #poll(long,TimeUnit) poll} 中），
     * 则立即将指定的元素传递给该消费者，否则将指定的元素插入此队列的尾部并等待直到元素被消费者接收。
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
     * 如果在超时前可以将元素传递给消费者，则将元素传递给消费者。
     *
     * <p>更准确地说，如果存在一个消费者已经在等待接收它（在
     * {@link #take} 或定时 {@link #poll(long,TimeUnit) poll} 中），
     * 则立即将指定的元素传递给该消费者，否则将指定的元素插入此队列的尾部并等待直到元素被消费者接收，
     * 如果指定的等待时间在元素可以传递之前耗尽，则返回 {@code false}。
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
     * 返回一个按顺序遍历此队列元素的迭代器。
     * 元素将按从头到尾的顺序返回。
     *
     * <p>返回的迭代器是
     * <a href="package-summary.html#Weakly"><i>弱一致性</i></a>。
     *
     * @return 一个按顺序遍历此队列元素的迭代器
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
     * 如果此队列包含指定元素的一个实例，则移除该实例。更正式地说，移除一个元素 {@code e}，使得
     * {@code o.equals(e)}，如果此队列包含一个或多个这样的元素。
     * 如果此队列包含指定的元素（或等效地，如果此队列因调用而改变），则返回 {@code true}。
     *
     * @param o 要从此队列中移除的元素（如果存在）
     * @return 如果此队列因调用而改变，则返回 {@code true}
     */
    public boolean remove(Object o) {
        return findAndRemove(o);
    }

    /**
     * 如果此队列包含指定的元素，则返回 {@code true}。更正式地说，如果且仅当此队列包含
     * 至少一个元素 {@code e} 使得 {@code o.equals(e)}，则返回 {@code true}。
     *
     * @param o 要检查是否包含在此队列中的对象
     * @return 如果此队列包含指定的元素，则返回 {@code true}
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
     * 总是返回 {@code Integer.MAX_VALUE}，因为 {@code LinkedTransferQueue} 没有容量限制。
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
     * @serialData 所有元素（每个都是 {@code E}）按正确顺序，后面跟一个 null
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        s.defaultWriteObject();
        for (E e : this)
            s.writeObject(e);
        // 使用尾部 null 作为哨兵
        s.writeObject(null);
    }

    /**
     * 从流中恢复此队列（即反序列化它）。
     * @param s 流
     * @throws ClassNotFoundException 如果无法找到已序列化对象的类
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
}
