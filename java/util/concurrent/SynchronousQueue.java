/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
 * 由 Doug Lea, Bill Scherer 和 Michael Scott 编写，并在
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的公共领域中发布。
 */

package java.util.concurrent;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * 一个 {@linkplain BlockingQueue 阻塞队列}，其中每个插入操作必须等待另一个线程的相应移除操作，反之亦然。同步队列没有任何内部容量，甚至没有一个容量。你不能
 * {@code peek} 同步队列，因为只有在尝试移除时元素才存在；你不能插入一个元素（使用任何方法）除非另一个线程正在尝试移除它；
 * 你不能迭代，因为没有东西可以迭代。队列的 <em>头部</em> 是第一个排队的插入线程试图添加到队列中的元素；如果没有这样的排队线程，则没有元素可用于移除，
 * {@code poll()} 将返回 {@code null}。为了其他 {@code Collection} 方法（例如 {@code contains}），一个
 * {@code SynchronousQueue} 作为一个空集合。此队列不允许 {@code null} 元素。
 *
 * <p>同步队列类似于 CSP 和 Ada 中使用的会合通道。它们非常适合于传递设计，在这种设计中，一个线程中的对象必须与另一个线程中的对象同步，以传递某些信息、事件或任务。
 *
 * <p>此类支持等待生产者和消费者线程的可选公平性策略。默认情况下，此顺序不保证。但是，如果将公平性设置为
 * {@code true}，则按 FIFO 顺序授予线程访问权限。
 *
 * <p>此类及其迭代器实现了 {@link Collection} 和 {@link Iterator} 接口的所有 <em>可选</em> 方法。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @since 1.5
 * @author Doug Lea, Bill Scherer 和 Michael Scott
 * @param <E> 此集合中持有的元素类型
 */
public class SynchronousQueue<E> extends AbstractQueue<E>
    implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -3223113410248163686L;

    /*
     * 本类实现了“非阻塞并发对象与条件同步”中描述的双栈和双队列算法的扩展，由 W. N. Scherer III 和 M. L. Scott 编写。第 18 届分布式计算年度会议，
     * 2004 年 10 月（另见
     * http://www.cs.rochester.edu/u/scott/synchronization/pseudocode/duals.html）。
     * (Lifo) 栈用于非公平模式，(Fifo) 队列用于公平模式。两者的性能通常相似。Fifo 通常在争用下支持更高的吞吐量，而 Lifo 在常见应用中保持更高的线程局部性。
     *
     * 双队列（和双栈类似）在任何时候都可能持有“数据”——由 put 操作提供的项目，或者“请求”——代表 take 操作的槽，或者为空。调用“fulfill”（即，从持有数据的队列请求项目或反之亦然的调用）会取消队列中的互补节点。
     * 这些队列最有趣的特性是任何操作都可以确定队列处于哪种模式，并相应地采取行动而不需要锁。
     *
     * 队列和栈扩展了定义单个方法 transfer 的抽象类 Transferer，该方法执行 put 或 take 操作。这些操作在双数据结构中是对称的，因此几乎所有代码都可以组合在一起。结果的 transfer 方法较长，但比拆分为几乎重复的部分更容易理解。
     *
     * 队列和栈数据结构在概念上有很多相似之处，但在具体细节上却很少相同。为了简单起见，它们被保持独立，以便它们可以独立地发展。
     *
     * 本文中的算法与上述论文中的版本不同，扩展了它们以用于同步队列，以及处理取消。主要区别包括：
     *
     *  1. 原始算法使用位标记指针，但这里的算法使用节点中的模式位，导致了一系列进一步的调整。
     *  2. SynchronousQueues 必须阻塞等待被满足的线程。
     *  3. 支持通过超时和中断取消，包括从列表中清理已取消的节点/线程以避免垃圾保留和内存耗尽。
     *
     * 阻塞主要通过使用 LockSupport park/unpark 实现，除了那些看起来是下一个将被满足的节点首先在多处理器上自旋一段时间。在非常繁忙的同步队列中，自旋可以显著提高吞吐量。而在不太繁忙的队列中，自旋的数量小到不会引起注意。
     *
     * 清理在队列和栈中以不同的方式进行。对于队列，我们几乎总是在 O(1) 时间内（模除重试以进行一致性检查）立即移除一个被取消的节点，除非它可能是当前的尾部，那么它必须等待直到某些后续的取消。对于栈，我们需要一个可能的 O(n) 遍历来确保我们可以移除节点，但这可以与访问栈的其他线程并发运行。
     *
     * 尽管垃圾收集处理了大多数节点回收问题，这些问题通常会使非阻塞算法复杂化，但还是注意“忘记”可能被阻塞线程长期持有的数据、其他节点和线程的引用。在设置为 null 会与主要算法冲突的情况下，这是通过将节点的链接更改为指向节点本身来完成的。这在 Stack 节点中不常出现（因为阻塞线程不会长时间持有旧的头部指针），但在 Queue 节点中必须积极地“忘记”引用，以避免任何节点自到达以来曾经引用过的所有内容的可达性。
     */


                    /**
     * 双栈和队列共享的内部API。
     */
    abstract static class Transferer<E> {
        /**
         * 执行一个put或take操作。
         *
         * @param e 如果非空，则是要交给消费者的项目；
         *          如果为空，则请求transfer返回生产者提供的项目。
         * @param timed 如果此操作应超时
         * @param nanos 超时时间，以纳秒为单位
         * @return 如果非空，则提供或接收的项目；如果为空，
         *         操作因超时或中断而失败——调用者可以通过检查Thread.interrupted来区分这两种情况。
         */
        abstract E transfer(E e, boolean timed, long nanos);
    }

    /** CPU的数量，用于自旋控制 */
    static final int NCPUS = Runtime.getRuntime().availableProcessors();

    /**
     * 在阻塞前自旋的次数，用于计时等待。
     * 该值是经验得出的——在各种处理器和操作系统上都能很好地工作。经验上，最佳值
     * 似乎不会随着CPU数量（超过2个）的变化而变化，因此只是一个常量。
     */
    static final int maxTimedSpins = (NCPUS < 2) ? 0 : 32;

    /**
     * 在阻塞前自旋的次数，用于非计时等待。
     * 这个值大于计时值，因为非计时等待自旋更快，因为它们在每次自旋时不需要检查时间。
     */
    static final int maxUntimedSpins = maxTimedSpins * 16;

    /**
     * 以纳秒为单位，自旋比使用计时park更快的时间。
     * 粗略估计就足够了。
     */
    static final long spinForTimeoutThreshold = 1000L;

    /** 双栈 */
    static final class TransferStack<E> extends Transferer<E> {
        /*
         * 这扩展了Scherer-Scott双栈算法，不同之处在于使用“覆盖”节点而不是
         * 位标记指针：满足操作在标记节点（在模式中设置FULFILLING位）上推送，以保留一个位置
         * 与等待节点匹配。
         */

        /* SNodes的模式，与节点字段一起使用 */
        /** 节点表示未满足的消费者 */
        static final int REQUEST    = 0;
        /** 节点表示未满足的生产者 */
        static final int DATA       = 1;
        /** 节点正在满足另一个未满足的DATA或REQUEST */
        static final int FULFILLING = 2;

        /** 如果m设置了满足位，则返回true。 */
        static boolean isFulfilling(int m) { return (m & FULFILLING) != 0; }

        /** TransferStacks的节点类。 */
        static final class SNode {
            volatile SNode next;        // 栈中的下一个节点
            volatile SNode match;       // 与此节点匹配的节点
            volatile Thread waiter;     // 用于控制park/unpark
            Object item;                // 数据；或对于REQUESTs为null
            int mode;
            // 注意：item和mode字段不需要是volatile的
            // 因为它们总是在其他volatile/原子操作之前写入，之后读取。

            SNode(Object item) {
                this.item = item;
            }

            boolean casNext(SNode cmp, SNode val) {
                return cmp == next &&
                    UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
            }

            /**
             * 尝试将节点s与此节点匹配，如果成功，则唤醒线程。
             * 满足者调用tryMatch来识别它们的等待者。
             * 等待者在被匹配之前会阻塞。
             *
             * @param s 要匹配的节点
             * @return 如果成功匹配到s，则返回true
             */
            boolean tryMatch(SNode s) {
                if (match == null &&
                    UNSAFE.compareAndSwapObject(this, matchOffset, null, s)) {
                    Thread w = waiter;
                    if (w != null) {    // 等待者最多需要一次unpark
                        waiter = null;
                        LockSupport.unpark(w);
                    }
                    return true;
                }
                return match == s;
            }

            /**
             * 通过将节点匹配到自身来尝试取消等待。
             */
            void tryCancel() {
                UNSAFE.compareAndSwapObject(this, matchOffset, null, this);
            }

            boolean isCancelled() {
                return match == this;
            }

            // Unsafe机制
            private static final sun.misc.Unsafe UNSAFE;
            private static final long matchOffset;
            private static final long nextOffset;

            static {
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class<?> k = SNode.class;
                    matchOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("match"));
                    nextOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("next"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }

        /** 栈的头部（顶部） */
        volatile SNode head;

        boolean casHead(SNode h, SNode nh) {
            return h == head &&
                UNSAFE.compareAndSwapObject(this, headOffset, h, nh);
        }

        /**
         * 创建或重置节点的字段。仅从transfer调用
         * 在栈上推送的节点是懒惰创建的，并且在可能的情况下重用，以帮助减少读取
         * 和CAS头部的时间间隔，并避免在由于竞争而推送节点的CAS失败时产生垃圾。
         */
        static SNode snode(SNode s, Object e, SNode next, int mode) {
            if (s == null) s = new SNode(e);
            s.mode = mode;
            s.next = next;
            return s;
        }

        /**
         * 放入或取出一个项目。
         */
        @SuppressWarnings("unchecked")
        E transfer(E e, boolean timed, long nanos) {
            /*
             * 基本算法是循环尝试以下三种操作之一：
             *
             * 1. 如果显然为空或已经包含相同模式的节点，尝试将节点推送到栈上并等待匹配，
             *    返回它，或者如果已取消则返回null。
             *
             * 2. 如果显然包含互补模式的节点，尝试将一个满足节点推送到栈上，与相应的等待节点匹配，
             *    从栈中弹出两者，并返回匹配的项目。匹配或取消链接实际上可能不是必要的，因为其他线程执行操作3：
             *
             * 3. 如果栈顶已经持有另一个满足节点，帮助它完成匹配和/或弹出操作，
             *    然后继续。帮助的代码基本上与满足的代码相同，只是它不返回项目。
             */


                            SNode s = null; // 根据需要构建或重用
            int mode = (e == null) ? REQUEST : DATA;

            for (;;) {
                SNode h = head;
                if (h == null || h.mode == mode) {  // 空或相同模式
                    if (timed && nanos <= 0) {      // 无法等待
                        if (h != null && h.isCancelled())
                            casHead(h, h.next);     // 弹出已取消的节点
                        else
                            return null;
                    } else if (casHead(h, s = snode(s, e, h, mode))) {
                        SNode m = awaitFulfill(s, timed, nanos);
                        if (m == s) {               // 等待被取消
                            clean(s);
                            return null;
                        }
                        if ((h = head) != null && h.next == s)
                            casHead(h, s.next);     // 帮助 s 的完成者
                        return (E) ((mode == REQUEST) ? m.item : s.item);
                    }
                } else if (!isFulfilling(h.mode)) { // 尝试完成
                    if (h.isCancelled())            // 已经取消
                        casHead(h, h.next);         // 弹出并重试
                    else if (casHead(h, s=snode(s, e, h, FULFILLING|mode))) {
                        for (;;) { // 循环直到匹配或等待者消失
                            SNode m = s.next;       // m 是 s 的匹配
                            if (m == null) {        // 所有等待者都消失了
                                casHead(s, null);   // 弹出完成节点
                                s = null;           // 下次使用新节点
                                break;              // 重新开始主循环
                            }
                            SNode mn = m.next;
                            if (m.tryMatch(s)) {
                                casHead(s, mn);     // 弹出 s 和 m
                                return (E) ((mode == REQUEST) ? m.item : s.item);
                            } else                  // 失配
                                s.casNext(m, mn);   // 帮助解链
                        }
                    }
                } else {                            // 帮助完成者
                    SNode m = h.next;               // m 是 h 的匹配
                    if (m == null)                  // 等待者消失了
                        casHead(h, null);           // 弹出完成节点
                    else {
                        SNode mn = m.next;
                        if (m.tryMatch(h))          // 帮助匹配
                            casHead(h, mn);         // 弹出 h 和 m
                        else                        // 失配
                            h.casNext(m, mn);       // 帮助解链
                    }
                }
            }
        }

        /**
         * 自旋/阻塞直到节点 s 被完成操作匹配。
         *
         * @param s 等待的节点
         * @param timed 是否为定时等待
         * @param nanos 超时值
         * @return 匹配的节点，或 s 如果被取消
         */
        SNode awaitFulfill(SNode s, boolean timed, long nanos) {
            /*
             * 当一个节点/线程即将阻塞时，它会设置其 waiter
             * 字段，然后在实际挂起之前至少再检查一次状态，从而覆盖
             * 完成者注意到 waiter 不为 null 应该唤醒的情况。
             *
             * 当调用时节点似乎位于堆栈头部时，挂起调用前会进行自旋以避免
             * 在生产者和消费者几乎同时到达时阻塞。这在多处理器上才会频繁发生。
             *
             * 主循环中检查返回的顺序反映了中断优先于
             * 正常返回，而正常返回又优先于
             * 超时。因此，在超时后，放弃前会进行最后一次匹配检查。除了
             * 从非定时 SynchronousQueue.{poll/offer} 调用的情况，它们不检查中断
             * 也不等待，因此被困在 transfer 方法中而不是调用 awaitFulfill。
             */
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            Thread w = Thread.currentThread();
            int spins = (shouldSpin(s) ?
                         (timed ? maxTimedSpins : maxUntimedSpins) : 0);
            for (;;) {
                if (w.isInterrupted())
                    s.tryCancel();
                SNode m = s.match;
                if (m != null)
                    return m;
                if (timed) {
                    nanos = deadline - System.nanoTime();
                    if (nanos <= 0L) {
                        s.tryCancel();
                        continue;
                    }
                }
                if (spins > 0)
                    spins = shouldSpin(s) ? (spins-1) : 0;
                else if (s.waiter == null)
                    s.waiter = w; // 建立 waiter 以便下次迭代挂起
                else if (!timed)
                    LockSupport.park(this);
                else if (nanos > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanos);
            }
        }

        /**
         * 如果节点 s 在头部或存在活跃的完成者，则返回 true。
         */
        boolean shouldSpin(SNode s) {
            SNode h = head;
            return (h == s || h == null || isFulfilling(h.mode));
        }

        /**
         * 从堆栈中解链 s。
         */
        void clean(SNode s) {
            s.item = null;   // 忘记项
            s.waiter = null; // 忘记线程

            /*
             * 最坏情况下，我们可能需要遍历整个堆栈以解链 s。如果存在多个并发调用 clean，我们
             * 可能看不到 s，因为另一线程可能已经移除了它。但我们可以在看到任何已知
             * 跟随 s 的节点时停止。我们使用 s.next，除非它也被取消，在这种情况下我们尝试
             * 一个节点之后的节点。我们不再检查任何进一步的节点，因为我们不想为了找到哨兵节点
             * 而双重遍历。
             */


                            SNode past = s.next;
            if (past != null && past.isCancelled())
                past = past.next;

            // 吸收头部的已取消节点
            SNode p;
            while ((p = head) != null && p != past && p.isCancelled())
                casHead(p, p.next);

            // 解除嵌入节点
            while (p != null && p != past) {
                SNode n = p.next;
                if (n != null && n.isCancelled())
                    p.casNext(n, n.next);
                else
                    p = n;
            }
        }

        // Unsafe 机制
        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = TransferStack.class;
                headOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("head"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /** 双队列 */
    static final class TransferQueue<E> extends Transferer<E> {
        /*
         * 这个类扩展了 Scherer-Scott 双队列算法，不同之处在于使用节点中的模式而不是标记指针。
         * 该算法比栈的算法简单一些，因为完成者不需要显式的节点，匹配是通过 CAS 操作 QNode.item 字段
         * 从非空到空（对于 put 操作）或反之（对于 take 操作）来完成的。
         */

        /** TransferQueue 的节点类。 */
        static final class QNode {
            volatile QNode next;          // 队列中的下一个节点
            volatile Object item;         // CAS 操作的目标，从非空到空或反之
            volatile Thread waiter;       // 用于控制 park/unpark
            final boolean isData;

            QNode(Object item, boolean isData) {
                this.item = item;
                this.isData = isData;
            }

            boolean casNext(QNode cmp, QNode val) {
                return next == cmp &&
                    UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
            }

            boolean casItem(Object cmp, Object val) {
                return item == cmp &&
                    UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
            }

            /**
             * 尝试通过 CAS 操作将 this 作为 item 来取消。
             */
            void tryCancel(Object cmp) {
                UNSAFE.compareAndSwapObject(this, itemOffset, cmp, this);
            }

            boolean isCancelled() {
                return item == this;
            }

            /**
             * 如果此节点已知不在队列中，因为其 next 指针由于 advanceHead 操作而被遗忘，则返回 true。
             */
            boolean isOffList() {
                return next == this;
            }

            // Unsafe 机制
            private static final sun.misc.Unsafe UNSAFE;
            private static final long itemOffset;
            private static final long nextOffset;

            static {
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class<?> k = QNode.class;
                    itemOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("item"));
                    nextOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("next"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }

        /** 队列的头部 */
        transient volatile QNode head;
        /** 队列的尾部 */
        transient volatile QNode tail;
        /**
         * 可能尚未从队列中解除链接的已取消节点的引用，因为它是取消时最后一个插入的节点。
         */
        transient volatile QNode cleanMe;

        TransferQueue() {
            QNode h = new QNode(null, false); // 初始化为虚拟节点。
            head = h;
            tail = h;
        }

        /**
         * 尝试将 nh 作为新的头部；如果成功，解除旧头部的下一个节点以避免垃圾保留。
         */
        void advanceHead(QNode h, QNode nh) {
            if (h == head &&
                UNSAFE.compareAndSwapObject(this, headOffset, h, nh))
                h.next = h; // 忘记旧的下一个节点
        }

        /**
         * 尝试将 nt 作为新的尾部。
         */
        void advanceTail(QNode t, QNode nt) {
            if (tail == t)
                UNSAFE.compareAndSwapObject(this, tailOffset, t, nt);
        }

        /**
         * 尝试 CAS cleanMe 插槽。
         */
        boolean casCleanMe(QNode cmp, QNode val) {
            return cleanMe == cmp &&
                UNSAFE.compareAndSwapObject(this, cleanMeOffset, cmp, val);
        }

        /**
         * 放入或取出一个项目。
         */
        @SuppressWarnings("unchecked")
        E transfer(E e, boolean timed, long nanos) {
            /* 基本算法是循环尝试执行以下两个操作之一：
             *
             * 1. 如果队列显然为空或包含相同模式的节点，尝试将节点添加到等待者队列中，
             *    等待被完成（或取消），然后返回匹配的项目。
             *
             * 2. 如果队列显然包含等待的项目，并且此调用是互补模式，尝试通过 CAS 操作
             *    等待节点的 item 字段并将其出队，然后返回匹配的项目。
             *
             * 在每种情况下，途中检查并尝试帮助其他停滞/缓慢的线程推进头部和尾部。
             *
             * 循环从一个空检查开始，以防止看到未初始化的 head 或 tail 值。这在当前的 SynchronousQueue 中
             * 从未发生过，但如果调用者持有非 volatile/final 的 transferer 引用，则可能发生。
             * 无论如何，这里有一个空检查，因为它将空检查放在循环的顶部，这通常比隐式地分散在各处要快。
             */


                            QNode s = null; // 构造/重用，按需
            boolean isData = (e != null);

            for (;;) {
                QNode t = tail;
                QNode h = head;
                if (t == null || h == null)         // 发现未初始化的值
                    continue;                       // 自旋

                if (h == t || t.isData == isData) { // 空或相同模式
                    QNode tn = t.next;
                    if (t != tail)                  // 不一致的读取
                        continue;
                    if (tn != null) {               // 滞后的尾部
                        advanceTail(t, tn);
                        continue;
                    }
                    if (timed && nanos <= 0)        // 不能等待
                        return null;
                    if (s == null)
                        s = new QNode(e, isData);
                    if (!t.casNext(null, s))        // 链接失败
                        continue;

                    advanceTail(t, s);              // 移动尾部并等待
                    Object x = awaitFulfill(s, e, timed, nanos);
                    if (x == s) {                   // 等待被取消
                        clean(t, s);
                        return null;
                    }

                    if (!s.isOffList()) {           // 未被取消链接
                        advanceHead(t, s);          // 如果是头部则取消链接
                        if (x != null)              // 并忘记字段
                            s.item = s;
                        s.waiter = null;
                    }
                    return (x != null) ? (E)x : e;

                } else {                            // 互补模式
                    QNode m = h.next;               // 要满足的节点
                    if (t != tail || m == null || h != head)
                        continue;                   // 不一致的读取

                    Object x = m.item;
                    if (isData == (x != null) ||    // m 已经被满足
                        x == m ||                   // m 被取消
                        !m.casItem(x, e)) {         // CAS 失败
                        advanceHead(h, m);          // 取消队列并重试
                        continue;
                    }

                    advanceHead(h, m);              // 成功满足
                    LockSupport.unpark(m.waiter);
                    return (x != null) ? (E)x : e;
                }
            }
        }

        /**
         * 自旋/阻塞直到节点 s 被满足。
         *
         * @param s 等待的节点
         * @param e 用于检查匹配的比较值
         * @param timed 如果是定时等待则为 true
         * @param nanos 超时值
         * @return 匹配的项，如果被取消则返回 s
         */
        Object awaitFulfill(QNode s, E e, boolean timed, long nanos) {
            /* 与 TransferStack.awaitFulfill 相同的思路 */
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            Thread w = Thread.currentThread();
            int spins = ((head.next == s) ?
                         (timed ? maxTimedSpins : maxUntimedSpins) : 0);
            for (;;) {
                if (w.isInterrupted())
                    s.tryCancel(e);
                Object x = s.item;
                if (x != e)
                    return x;
                if (timed) {
                    nanos = deadline - System.nanoTime();
                    if (nanos <= 0L) {
                        s.tryCancel(e);
                        continue;
                    }
                }
                if (spins > 0)
                    --spins;
                else if (s.waiter == null)
                    s.waiter = w;
                else if (!timed)
                    LockSupport.park(this);
                else if (nanos > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanos);
            }
        }

        /**
         * 删除带有原始前驱节点 pred 的已取消节点 s。
         */
        void clean(QNode pred, QNode s) {
            s.waiter = null; // 忘记线程
            /*
             * 在任何给定时间，列表中恰好有一个节点不能被删除 —— 最后插入的节点。为了适应这一点，
             * 如果我们不能删除 s，我们将它的前驱保存为 "cleanMe"，首先删除之前保存的版本。
             * 至少 s 节点或之前保存的节点之一总是可以被删除，因此这总是会终止。
             */
            while (pred.next == s) { // 如果已经取消链接则提前返回
                QNode h = head;
                QNode hn = h.next;   // 将取消的第一个节点吸收为头部
                if (hn != null && hn.isCancelled()) {
                    advanceHead(h, hn);
                    continue;
                }
                QNode t = tail;      // 确保尾部的一致性读取
                if (t == h)
                    return;
                QNode tn = t.next;
                if (t != tail)
                    continue;
                if (tn != null) {
                    advanceTail(t, tn);
                    continue;
                }
                if (s != t) {        // 如果不是尾部，尝试取消链接
                    QNode sn = s.next;
                    if (sn == s || pred.casNext(s, sn))
                        return;
                }
                QNode dp = cleanMe;
                if (dp != null) {    // 尝试取消链接前一个已取消的节点
                    QNode d = dp.next;
                    QNode dn;
                    if (d == null ||               // d 不存在或
                        d == dp ||                 // d 已经不在列表中或
                        !d.isCancelled() ||        // d 未被取消或
                        (d != t &&                 // d 不是尾部且
                         (dn = d.next) != null &&  //   有后继
                         dn != d &&                //   该后继在列表中
                         dp.casNext(d, dn)))       // d 被取消链接
                        casCleanMe(dp, null);
                    if (dp == pred)
                        return;      // s 已经是保存的节点
                } else if (casCleanMe(null, pred))
                    return;          // 推迟清理 s
            }
        }


                        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset;
        private static final long tailOffset;
        private static final long cleanMeOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = TransferQueue.class;
                headOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("head"));
                tailOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("tail"));
                cleanMeOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("cleanMe"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /**
     * 传输器。仅在构造函数中设置，但不能声明为 final，否则会进一步复杂化序列化。由于这在每个公共方法中最多访问一次，
     * 因此在这里使用 volatile 而不是 final 并没有明显的性能损失。
     */
    private transient volatile Transferer<E> transferer;

    /**
     * 创建一个具有非公平访问策略的 {@code SynchronousQueue}。
     */
    public SynchronousQueue() {
        this(false);
    }

    /**
     * 创建一个具有指定公平性策略的 {@code SynchronousQueue}。
     *
     * @param fair 如果为 true，等待线程以 FIFO 顺序争夺访问权；否则顺序未指定。
     */
    public SynchronousQueue(boolean fair) {
        transferer = fair ? new TransferQueue<E>() : new TransferStack<E>();
    }

    /**
     * 将指定的元素添加到此队列中，必要时等待另一个线程接收它。
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public void put(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        if (transferer.transfer(e, false, 0) == null) {
            Thread.interrupted();
            throw new InterruptedException();
        }
    }

    /**
     * 将指定的元素插入到此队列中，必要时等待指定的等待时间，直到另一个线程接收它。
     *
     * @return 如果成功则返回 {@code true}，如果指定的等待时间在消费者出现之前到期，则返回 {@code false}
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (e == null) throw new NullPointerException();
        if (transferer.transfer(e, true, unit.toNanos(timeout)) != null)
            return true;
        if (!Thread.interrupted())
            return false;
        throw new InterruptedException();
    }

    /**
     * 如果另一个线程正在等待接收它，则将指定的元素插入到此队列中。
     *
     * @param e 要添加的元素
     * @return 如果元素被添加到此队列中，则返回 {@code true}，否则返回 {@code false}
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        return transferer.transfer(e, true, 0) != null;
    }

    /**
     * 检索并移除此队列的头部，必要时等待另一个线程插入它。
     *
     * @return 此队列的头部
     * @throws InterruptedException {@inheritDoc}
     */
    public E take() throws InterruptedException {
        E e = transferer.transfer(null, false, 0);
        if (e != null)
            return e;
        Thread.interrupted();
        throw new InterruptedException();
    }

    /**
     * 检索并移除此队列的头部，必要时等待指定的等待时间，直到另一个线程插入它。
     *
     * @return 此队列的头部，如果指定的等待时间在元素出现之前到期，则返回 {@code null}
     * @throws InterruptedException {@inheritDoc}
     */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E e = transferer.transfer(null, true, unit.toNanos(timeout));
        if (e != null || !Thread.interrupted())
            return e;
        throw new InterruptedException();
    }

    /**
     * 如果另一个线程当前正在提供一个元素，则检索并移除此队列的头部。
     *
     * @return 此队列的头部，如果没有元素可用，则返回 {@code null}
     */
    public E poll() {
        return transferer.transfer(null, true, 0);
    }

    /**
     * 始终返回 {@code true}。
     * {@code SynchronousQueue} 没有内部容量。
     *
     * @return {@code true}
     */
    public boolean isEmpty() {
        return true;
    }

    /**
     * 始终返回零。
     * {@code SynchronousQueue} 没有内部容量。
     *
     * @return 零
     */
    public int size() {
        return 0;
    }

    /**
     * 始终返回零。
     * {@code SynchronousQueue} 没有内部容量。
     *
     * @return 零
     */
    public int remainingCapacity() {
        return 0;
    }

    /**
     * 什么也不做。
     * {@code SynchronousQueue} 没有内部容量。
     */
    public void clear() {
    }

    /**
     * 始终返回 {@code false}。
     * {@code SynchronousQueue} 没有内部容量。
     *
     * @param o 元素
     * @return {@code false}
     */
    public boolean contains(Object o) {
        return false;
    }

    /**
     * 始终返回 {@code false}。
     * {@code SynchronousQueue} 没有内部容量。
     *
     * @param o 要移除的元素
     * @return {@code false}
     */
    public boolean remove(Object o) {
        return false;
    }

    /**
     * 如果给定的集合为空，则返回 {@code false}。
     * {@code SynchronousQueue} 没有内部容量。
     *
     * @param c 集合
     * @return 如果给定的集合为空，则返回 {@code false}
     */
    public boolean containsAll(Collection<?> c) {
        return c.isEmpty();
    }

                    /**
     * 始终返回 {@code false}。
     * 一个 {@code SynchronousQueue} 没有内部容量。
     *
     * @param c 集合
     * @return {@code false}
     */
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    /**
     * 始终返回 {@code false}。
     * 一个 {@code SynchronousQueue} 没有内部容量。
     *
     * @param c 集合
     * @return {@code false}
     */
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    /**
     * 始终返回 {@code null}。
     * 一个 {@code SynchronousQueue} 不会返回元素
     * 除非主动等待。
     *
     * @return {@code null}
     */
    public E peek() {
        return null;
    }

    /**
     * 返回一个空的迭代器，其中 {@code hasNext} 始终返回
     * {@code false}。
     *
     * @return 一个空的迭代器
     */
    public Iterator<E> iterator() {
        return Collections.emptyIterator();
    }

    /**
     * 返回一个空的拆分迭代器，其中调用
     * {@link java.util.Spliterator#trySplit()} 始终返回 {@code null}。
     *
     * @return 一个空的拆分迭代器
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return Spliterators.emptySpliterator();
    }

    /**
     * 返回一个零长度的数组。
     * @return 一个零长度的数组
     */
    public Object[] toArray() {
        return new Object[0];
    }

    /**
     * 将指定数组的第零个元素设置为 {@code null}
     * （如果数组长度非零）并返回它。
     *
     * @param a 数组
     * @return 指定的数组
     * @throws NullPointerException 如果指定的数组为 null
     */
    public <T> T[] toArray(T[] a) {
        if (a.length > 0)
            a[0] = null;
        return a;
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
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
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
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

    /*
     * 为了应对 1.5 版本的 SynchronousQueue 的序列化策略，我们声明了一些仅用于启用跨版本序列化的类和字段。
     * 这些字段从未使用，因此只有在对象被序列化或反序列化时才会初始化。
     */

    @SuppressWarnings("serial")
    static class WaitQueue implements java.io.Serializable { }
    static class LifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3633113410248163686L;
    }
    static class FifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3623113410248163686L;
    }
    private ReentrantLock qlock;
    private WaitQueue waitingProducers;
    private WaitQueue waitingConsumers;

    /**
     * 将此队列保存到流中（即，序列化它）。
     * @param s 流
     * @throws java.io.IOException 如果发生 I/O 错误
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        boolean fair = transferer instanceof TransferQueue;
        if (fair) {
            qlock = new ReentrantLock(true);
            waitingProducers = new FifoWaitQueue();
            waitingConsumers = new FifoWaitQueue();
        }
        else {
            qlock = new ReentrantLock();
            waitingProducers = new LifoWaitQueue();
            waitingConsumers = new LifoWaitQueue();
        }
        s.defaultWriteObject();
    }

    /**
     * 从流中重新构造此队列（即，反序列化它）。
     * @param s 流
     * @throws ClassNotFoundException 如果无法找到序列化对象的类
     * @throws java.io.IOException 如果发生 I/O 错误
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (waitingProducers instanceof FifoWaitQueue)
            transferer = new TransferQueue<E>();
        else
            transferer = new TransferStack<E>();
    }

    // Unsafe 机制
    static long objectFieldOffset(sun.misc.Unsafe UNSAFE,
                                  String field, Class<?> klazz) {
        try {
            return UNSAFE.objectFieldOffset(klazz.getDeclaredField(field));
        } catch (NoSuchFieldException e) {
            // 将异常转换为相应的错误
            NoSuchFieldError error = new NoSuchFieldError(field);
            error.initCause(e);
            throw error;
        }
    }

}
