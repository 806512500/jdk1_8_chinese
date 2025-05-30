
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
 * Written by Doug Lea, Bill Scherer, and Michael Scott with
 * assistance from members of JCP JSR-166 Expert Group and released to
 * the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 一个同步点，线程可以在此配对并交换元素。
 * 每个线程在进入 {@link #exchange exchange} 方法时呈现某个对象，与伙伴线程匹配，
 * 并在返回时接收其伙伴线程的对象。Exchanger 可以视为双向形式的 {@link SynchronousQueue}。
 * Exchanger 可能在遗传算法和管道设计等应用中非常有用。
 *
 * <p><b>示例用法：</b>
 * 以下是一个使用 {@code Exchanger} 的类的要点，该类在两个线程之间交换缓冲区，以便填充缓冲区的线程在需要时获得一个新清空的缓冲区，
 * 并将已填充的缓冲区交给清空缓冲区的线程。
 *  <pre> {@code
 * class FillAndEmpty {
 *   Exchanger<DataBuffer> exchanger = new Exchanger<DataBuffer>();
 *   DataBuffer initialEmptyBuffer = ... 一个虚构的类型
 *   DataBuffer initialFullBuffer = ...
 *
 *   class FillingLoop implements Runnable {
 *     public void run() {
 *       DataBuffer currentBuffer = initialEmptyBuffer;
 *       try {
 *         while (currentBuffer != null) {
 *           addToBuffer(currentBuffer);
 *           if (currentBuffer.isFull())
 *             currentBuffer = exchanger.exchange(currentBuffer);
 *         }
 *       } catch (InterruptedException ex) { ... 处理 ... }
 *     }
 *   }
 *
 *   class EmptyingLoop implements Runnable {
 *     public void run() {
 *       DataBuffer currentBuffer = initialFullBuffer;
 *       try {
 *         while (currentBuffer != null) {
 *           takeFromBuffer(currentBuffer);
 *           if (currentBuffer.isEmpty())
 *             currentBuffer = exchanger.exchange(currentBuffer);
 *         }
 *       } catch (InterruptedException ex) { ... 处理 ...}
 *     }
 *   }
 *
 *   void start() {
 *     new Thread(new FillingLoop()).start();
 *     new Thread(new EmptyingLoop()).start();
 *   }
 * }}</pre>
 *
 * <p>内存一致性效果：对于通过 {@code Exchanger} 成功交换对象的每对线程，
 * 每个线程中的 {@code exchange()} 之前的动作
 * <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 另一个线程从相应的 {@code exchange()} 返回后的动作。
 *
 * @since 1.5
 * @author Doug Lea 和 Bill Scherer 和 Michael Scott
 * @param <V> 可以交换的对象类型
 */
public class Exchanger<V> {

    /*
     * 概述：核心算法是，对于一个交换“槽”和一个带有项目的参与者（调用者）：
     *
     * for (;;) {
     *   if (槽为空) {                       // 提供
     *     将项目放入一个 Node；
     *     如果可以将槽从空 CAS 到节点 {
     *       等待释放；
     *       返回节点中的匹配项目；
     *     }
     *   }
     *   else if (可以将槽从节点 CAS 到空) { // 释放
     *     获取节点中的项目；
     *     在节点中设置匹配项目；
     *     释放等待的线程；
     *   }
     *   // 否则在 CAS 失败时重试
     * }
     *
     * 这是“双重数据结构”最简单的形式之一——参见 Scott 和 Scherer 的 DISC 04 论文和
     * http://www.cs.rochester.edu/research/synchronization/pseudocode/duals.html
     *
     * 从理论上讲，这非常好。但在实践中，像许多以单个位置的原子更新为中心的算法一样，当有多个参与者使用同一个 Exchanger 时，它的扩展性非常差。
     * 因此，实现使用了一种消除竞技场的形式，通过安排一些线程通常使用不同的槽来分散这种竞争，同时仍然确保最终任何两个参与者都能够交换项目。
     * 也就是说，我们不能完全在各个线程之间进行分区，而是给线程分配竞技场索引，这些索引在竞争时平均增长，在没有竞争时缩小。
     * 我们通过将无论如何都需要的节点定义为 ThreadLocals，并在其中包含每个线程的索引和相关簿记状态来实现这一点。
     * （我们可以在槽在节点和 null 之间交替的情况下安全地重用每个线程的节点，因此不会遇到 ABA 问题。但是，我们在重用之间确实需要一些重置。）
     *
     * 实现有效的竞技场需要分配大量空间，因此我们只在检测到竞争时才这样做（除了在单处理器上，它们不会有所帮助，因此不使用）。
     * 否则，交换使用单槽的 slotExchange 方法。在竞争时，不仅槽必须位于不同的位置，而且这些位置不能因位于同一缓存行（或更一般地，同一一致性单元）上而遇到内存竞争。
     * 由于目前无法确定缓存行大小，我们定义了一个对于常见平台足够的值。此外，还采取了额外的措施以避免其他错误/意外共享并增强局部性，
     * 包括向节点添加填充（通过 sun.misc.Contended），将“bound”嵌入为 Exchanger 字段，并重新设计一些 park/unpark 机制（与 LockSupport 版本相比）。
     *
     * 竞技场最初只有一个已使用的槽。我们通过跟踪碰撞来扩展有效的竞技场大小；即，在尝试交换时失败的 CAS。
     * 由于上述算法的性质，唯一可靠表明竞争的碰撞类型是两个尝试释放时的碰撞——两个尝试提供的 CAS 之一可以合法失败而不会表明有多个其他线程的竞争。
     * （注意：可以通过在 CAS 失败后读取槽值来更精确地检测竞争，但这不值得。）当线程在当前竞技场边界内的每个槽上发生碰撞时，
     * 它会尝试将竞技场大小扩展一个。我们通过在“bound”字段上使用版本（序列）号来跟踪边界内的碰撞，并在参与者注意到边界已更新（无论方向）时保守地重置碰撞计数。
     *
     * 当竞技场大小大于一个槽时，通过在一段时间后放弃等待并尝试减少竞技场大小来减少有效的竞技场大小。这段时间的值是一个经验问题。
     * 我们通过利用在合理等待性能中必不可少的 spin->yield->block 来实现这一点——在繁忙的交换器中，提供通常几乎立即释放，
     * 在这种情况下，多处理器上的上下文切换非常慢/浪费。竞技场等待只是省略了阻塞部分，而是取消。spin 计数是经验选择的，
     * 以避免在最大持续交换率下 99% 的时间阻塞。spin 和 yield 包含一些有限的随机性（使用廉价的 xorshift）以避免定期模式，
     * 这些模式可能会导致无生产力的扩展/缩小周期。（使用伪随机还有助于通过使分支不可预测来规范化 spin 周期的持续时间。）
     * 此外，在提供期间，等待者可以“知道”当其槽发生变化时它将被释放，但还不能继续，直到匹配设置。在此期间，它不能取消提供，因此会 spin/yield。
     * 注意：通过将线性化点更改为 match 字段的 CAS（如 Scott & Scherer DISC 论文中的一个案例所示），可以避免这种二次检查，
     * 这也增加了一些异步性，但以较差的碰撞检测和无法始终重用每个线程的节点为代价。因此，当前方案通常是更好的权衡。
     *
     * 在发生碰撞时，索引在竞技场中以逆序循环遍历，当边界变化时从最大索引（这将是最稀疏的）重新开始。
     * （在过期时，索引反而会减半直到达到 0。）可以（并且已经尝试过）使用随机化、质数值步进或双重哈希风格的遍历来减少堆积。
     * 但经验表明，无论这些方法可能带来的好处都无法克服其额外的开销：我们管理的是在没有持续竞争的情况下非常快速的操作，
     * 因此更简单/更快的控制策略比更准确但更慢的策略更好。
     *
     * 由于我们使用过期来控制竞技场大小，因此在竞技场大小缩小到零（或竞技场未启用）之前，我们不能在公共交换方法的定时版本中抛出 TimeoutExceptions。
     * 这可能会延迟对超时的响应，但仍在规范范围内。
     *
     * 几乎所有的实现都在 slotExchange 和 arenaExchange 方法中。这些方法具有相似的整体结构，但在太多细节上不同，无法合并。
     * slotExchange 方法使用单个 Exchanger 字段“slot”而不是竞技场数组元素。然而，它仍然需要最小的碰撞检测来触发竞技场构建。
     * （最混乱的部分是在过渡期间确保中断状态和 InterruptedExceptions 的正确处理，当两个方法都可能被调用时。这是通过使用 null 返回作为哨兵来重新检查中断状态来实现的。）
     *
     * 由于大多数逻辑依赖于作为局部变量维护的字段的读取，因此方法是单体的，这使得它们不能很好地分解——主要是，这里，笨重的 spin->yield->block/cancel 代码，
     * 并且严重依赖于内联嵌入的 CAS 和相关内存访问操作的内在函数（当这些操作隐藏在其他方法后面时，动态编译器不太可能将它们内联，从而更好地命名和封装预期效果）。
     * 这包括在重用每个线程的节点之间使用 putOrderedX 清除字段。注意，字段 Node.item 虽然由释放线程读取，但未声明为 volatile，
     * 因为它们仅在 CAS 操作之后读取，这些操作必须先于访问，所有者线程的所有其他使用都通过其他操作正确排序。
     * （因为实际的原子点是槽 CAS，所以在释放中对 Node.match 的写入可以比完全 volatile 写入更弱。然而，这没有这样做，因为它可能会进一步推迟写入，从而延迟进度。）
     */

    /**
     * 竞技场中任何两个已使用槽之间的字节距离（作为移位值）。1 << ASHIFT 应至少为缓存行大小。
     */
    private static final int ASHIFT = 7;

    /**
     * 支持的最大竞技场索引。最大可分配的竞技场大小为 MMASK + 1。必须是 2 的幂减一，小于 (1<<(31-ASHIFT))。
     * 255 (0xff) 的上限足以满足主要算法的预期扩展限制。
     */
    private static final int MMASK = 0xff;

    /**
     * bound 字段的序列/版本位的单位。每次成功更改 bound 时也会添加 SEQ。
     */
    private static final int SEQ = MMASK + 1;

    /** CPU 数量，用于大小和 spin 控制 */
    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * 竞技场的最大槽索引：在没有竞争的情况下可以容纳所有线程的槽数，或最多为最大可索引值。
     */
    static final int FULL = (NCPU >= (MMASK << 1)) ? MMASK : NCPU >>> 1;

    /**
     * 在等待匹配时的 spin 次数上限。实际迭代次数平均约为此值的两倍，由于随机化。注意：当 NCPU==1 时，spin 被禁用。
     */
    private static final int SPINS = 1 << 10;

    /**
     * 表示公共方法的 null 参数/返回值。需要因为 API 最初没有禁止 null 参数，而应该禁止。
     */
    private static final Object NULL_ITEM = new Object();

    /**
     * 内部交换方法在超时时返回的哨兵值，以避免需要这些方法的单独定时版本。
     */
    private static final Object TIMED_OUT = new Object();

    /**
     * Nodes 持有部分交换的数据，以及其他每个线程的簿记信息。通过 @sun.misc.Contended 填充以减少内存竞争。
     */
    @sun.misc.Contended static final class Node {
        int index;              // 竞技场索引
        int bound;              // 最后记录的 Exchanger.bound 值
        int collides;           // 当前边界处的 CAS 失败次数
        int hash;               // 用于 spin 的伪随机数
        Object item;            // 该线程的当前项目
        volatile Object match;  // 释放线程提供的项目
        volatile Thread parked; // 停顿时设置为该线程，否则为 null
    }

    /** 对应的线程本地类 */
    static final class Participant extends ThreadLocal<Node> {
        public Node initialValue() { return new Node(); }
    }


                /**
     * 每线程状态
     */
    private final Participant participant;

    /**
     * 淘汰数组；在启用（在 slotExchange 内）之前为 null。
     * 元素访问使用 volatile 获取和 CAS 的仿真。
     */
    private volatile Node[] arena;

    /**
     * 在检测到争用之前使用的槽。
     */
    private volatile Node slot;

    /**
     * 最大有效 arena 位置的索引，与高位的 SEQ
     * 数字进行 OR 操作，在每次更新时递增。初始
     * 从 0 到 SEQ 的更新用于确保 arena 数组仅
     * 构造一次。
     */
    private volatile int bound;

    /**
     * 当 arenas 启用时的交换函数。请参见上方的解释。
     *
     * @param item 要交换的（非空）项
     * @param timed 如果等待是定时的，则为 true
     * @param ns 如果定时，则为最大等待时间，否则为 0L
     * @return 其他线程的项；如果被中断，则为 null；如果定时且超时，则为 TIMED_OUT
     */
    private final Object arenaExchange(Object item, boolean timed, long ns) {
        Node[] a = arena;
        Node p = participant.get();
        for (int i = p.index;;) {                      // 访问索引 i 处的槽
            int b, m, c; long j;                       // j 是原始数组偏移量
            Node q = (Node)U.getObjectVolatile(a, j = (i << ASHIFT) + ABASE);
            if (q != null && U.compareAndSwapObject(a, j, q, null)) {
                Object v = q.item;                     // 释放
                q.match = item;
                Thread w = q.parked;
                if (w != null)
                    U.unpark(w);
                return v;
            }
            else if (i <= (m = (b = bound) & MMASK) && q == null) {
                p.item = item;                         // 提供
                if (U.compareAndSwapObject(a, j, null, p)) {
                    long end = (timed && m == 0) ? System.nanoTime() + ns : 0L;
                    Thread t = Thread.currentThread(); // 等待
                    for (int h = p.hash, spins = SPINS;;) {
                        Object v = p.match;
                        if (v != null) {
                            U.putOrderedObject(p, MATCH, null);
                            p.item = null;             // 为下次使用清除
                            p.hash = h;
                            return v;
                        }
                        else if (spins > 0) {
                            h ^= h << 1; h ^= h >>> 3; h ^= h << 10; // xorshift
                            if (h == 0)                // 初始化哈希
                                h = SPINS | (int)t.getId();
                            else if (h < 0 &&          // 大约 50% 为真
                                     (--spins & ((SPINS >>> 1) - 1)) == 0)
                                Thread.yield();        // 每次等待两次 yield
                        }
                        else if (U.getObjectVolatile(a, j) != p)
                            spins = SPINS;       // 释放者尚未设置 match
                        else if (!t.isInterrupted() && m == 0 &&
                                 (!timed ||
                                  (ns = end - System.nanoTime()) > 0L)) {
                            U.putObject(t, BLOCKER, this); // 模拟 LockSupport
                            p.parked = t;              // 最小化窗口
                            if (U.getObjectVolatile(a, j) == p)
                                U.park(false, ns);
                            p.parked = null;
                            U.putObject(t, BLOCKER, null);
                        }
                        else if (U.getObjectVolatile(a, j) == p &&
                                 U.compareAndSwapObject(a, j, p, null)) {
                            if (m != 0)                // 尝试缩小
                                U.compareAndSwapInt(this, BOUND, b, b + SEQ - 1);
                            p.item = null;
                            p.hash = h;
                            i = p.index >>>= 1;        // 下降
                            if (Thread.interrupted())
                                return null;
                            if (timed && m == 0 && ns <= 0L)
                                return TIMED_OUT;
                            break;                     // 过期；重新开始
                        }
                    }
                }
                else
                    p.item = null;                     // 清除提供
            }
            else {
                if (p.bound != b) {                    // 过时；重置
                    p.bound = b;
                    p.collides = 0;
                    i = (i != m || m == 0) ? m : m - 1;
                }
                else if ((c = p.collides) < m || m == FULL ||
                         !U.compareAndSwapInt(this, BOUND, b, b + SEQ + 1)) {
                    p.collides = c + 1;
                    i = (i == 0) ? m : i - 1;          // 循环遍历
                }
                else
                    i = m + 1;                         // 增长
                p.index = i;
            }
        }
    }

    /**
     * 在 arenas 启用之前使用的交换函数。请参见上方的解释。
     *
     * @param item 要交换的项
     * @param timed 如果等待是定时的，则为 true
     * @param ns 如果定时，则为最大等待时间，否则为 0L
     * @return 其他线程的项；如果 arena 被启用或线程在完成前被中断，则为 null；如果定时且超时，则为 TIMED_OUT
     */
    private final Object slotExchange(Object item, boolean timed, long ns) {
        Node p = participant.get();
        Thread t = Thread.currentThread();
        if (t.isInterrupted()) // 保留中断状态，以便调用者可以重新检查
            return null;

        for (Node q;;) {
            if ((q = slot) != null) {
                if (U.compareAndSwapObject(this, SLOT, q, null)) {
                    Object v = q.item;
                    q.match = item;
                    Thread w = q.parked;
                    if (w != null)
                        U.unpark(w);
                    return v;
                }
                // 在争用时创建 arena，但继续直到 slot 为 null
                if (NCPU > 1 && bound == 0 &&
                    U.compareAndSwapInt(this, BOUND, 0, SEQ))
                    arena = new Node[(FULL + 2) << ASHIFT];
            }
            else if (arena != null)
                return null; // 调用者必须重新路由到 arenaExchange
            else {
                p.item = item;
                if (U.compareAndSwapObject(this, SLOT, null, p))
                    break;
                p.item = null;
            }
        }

        // 等待释放
        int h = p.hash;
        long end = timed ? System.nanoTime() + ns : 0L;
        int spins = (NCPU > 1) ? SPINS : 1;
        Object v;
        while ((v = p.match) == null) {
            if (spins > 0) {
                h ^= h << 1; h ^= h >>> 3; h ^= h << 10;
                if (h == 0)
                    h = SPINS | (int)t.getId();
                else if (h < 0 && (--spins & ((SPINS >>> 1) - 1)) == 0)
                    Thread.yield();
            }
            else if (slot != p)
                spins = SPINS;
            else if (!t.isInterrupted() && arena == null &&
                     (!timed || (ns = end - System.nanoTime()) > 0L)) {
                U.putObject(t, BLOCKER, this);
                p.parked = t;
                if (slot == p)
                    U.park(false, ns);
                p.parked = null;
                U.putObject(t, BLOCKER, null);
            }
            else if (U.compareAndSwapObject(this, SLOT, p, null)) {
                v = timed && ns <= 0L && !t.isInterrupted() ? TIMED_OUT : null;
                break;
            }
        }
        U.putOrderedObject(p, MATCH, null);
        p.item = null;
        p.hash = h;
        return v;
    }

    /**
     * 创建一个新的 Exchanger。
     */
    public Exchanger() {
        participant = new Participant();
    }

    /**
     * 等待另一个线程到达此交换点（除非当前线程被 {@linkplain Thread#interrupt 中断}），
     * 然后将给定对象传递给它，接收其对象作为交换。
     *
     * <p>如果另一个线程已经在交换点等待，则该线程将被恢复以进行线程调度，并接收当前线程传递的对象。
     * 当前线程立即返回，接收该其他线程传递给交换的对象。
     *
     * <p>如果没有其他线程已经在交换点等待，则当前线程将被禁用以进行线程调度，并处于休眠状态，直到发生以下两种情况之一：
     * <ul>
     * <li>有其他线程进入交换；或
     * <li>有其他线程 {@linkplain Thread#interrupt 中断} 当前线程。
     * </ul>
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时已设置中断状态；或
     * <li>在等待交换时被 {@linkplain Thread#interrupt 中断}，
     * </ul>
     * 则抛出 {@link InterruptedException} 并清除当前线程的中断状态。
     *
     * @param x 要交换的对象
     * @return 其他线程提供的对象
     * @throws InterruptedException 如果当前线程在等待时被中断
     */
    @SuppressWarnings("unchecked")
    public V exchange(V x) throws InterruptedException {
        Object v;
        Object item = (x == null) ? NULL_ITEM : x; // 转换 null 参数
        if ((arena != null ||
             (v = slotExchange(item, false, 0L)) == null) &&
            ((Thread.interrupted() || // 解析 null 返回
              (v = arenaExchange(item, false, 0L)) == null)))
            throw new InterruptedException();
        return (v == NULL_ITEM) ? null : (V)v;
    }

    /**
     * 等待另一个线程到达此交换点（除非当前线程被 {@linkplain Thread#interrupt 中断} 或指定的等待时间过期），
     * 然后将给定对象传递给它，接收其对象作为交换。
     *
     * <p>如果另一个线程已经在交换点等待，则该线程将被恢复以进行线程调度，并接收当前线程传递的对象。
     * 当前线程立即返回，接收该其他线程传递给交换的对象。
     *
     * <p>如果没有其他线程已经在交换点等待，则当前线程将被禁用以进行线程调度，并处于休眠状态，直到发生以下三种情况之一：
     * <ul>
     * <li>有其他线程进入交换；或
     * <li>有其他线程 {@linkplain Thread#interrupt 中断} 当前线程；或
     * <li>指定的等待时间过期。
     * </ul>
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时已设置中断状态；或
     * <li>在等待交换时被 {@linkplain Thread#interrupt 中断}，
     * </ul>
     * 则抛出 {@link InterruptedException} 并清除当前线程的中断状态。
     *
     * <p>如果指定的等待时间过期，则抛出 {@link
     * TimeoutException}。如果时间小于或等于零，该方法将不等待。
     *
     * @param x 要交换的对象
     * @param timeout 最大等待时间
     * @param unit {@code timeout} 参数的时间单位
     * @return 其他线程提供的对象
     * @throws InterruptedException 如果当前线程在等待时被中断
     * @throws TimeoutException 如果指定的等待时间过期且没有其他线程进入交换
     */
    @SuppressWarnings("unchecked")
    public V exchange(V x, long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException {
        Object v;
        Object item = (x == null) ? NULL_ITEM : x;
        long ns = unit.toNanos(timeout);
        if ((arena != null ||
             (v = slotExchange(item, true, ns)) == null) &&
            ((Thread.interrupted() ||
              (v = arenaExchange(item, true, ns)) == null)))
            throw new InterruptedException();
        if (v == TIMED_OUT)
            throw new TimeoutException();
        return (v == NULL_ITEM) ? null : (V)v;
    }

    // Unsafe 机制
    private static final sun.misc.Unsafe U;
    private static final long BOUND;
    private static final long SLOT;
    private static final long MATCH;
    private static final long BLOCKER;
    private static final int ABASE;
    static {
        int s;
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> ek = Exchanger.class;
            Class<?> nk = Node.class;
            Class<?> ak = Node[].class;
            Class<?> tk = Thread.class;
            BOUND = U.objectFieldOffset
                (ek.getDeclaredField("bound"));
            SLOT = U.objectFieldOffset
                (ek.getDeclaredField("slot"));
            MATCH = U.objectFieldOffset
                (nk.getDeclaredField("match"));
            BLOCKER = U.objectFieldOffset
                (tk.getDeclaredField("parkBlocker"));
            s = U.arrayIndexScale(ak);
            // ABASE 吸收元素 0 前的填充
            ABASE = U.arrayBaseOffset(ak) + (1 << ASHIFT);

        } catch (Exception e) {
            throw new Error(e);
        }
        if ((s & (s-1)) != 0 || s > (1 << ASHIFT))
            throw new Error("Unsupported array scale");
    }

}
