
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
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * 一个 {@linkplain BlockingQueue 阻塞队列}，其中每个插入操作都必须等待另一个线程的相应移除操作，反之亦然。同步队列没有内部容量，甚至没有容量为一的容量。你不能 {@code peek} 同步队列，因为只有在你尝试移除时元素才存在；你不能插入元素（使用任何方法）除非另一个线程正在尝试移除它；你不能迭代，因为没有可以迭代的内容。队列的 <em>头</em> 是第一个排队的插入线程正在尝试添加到队列中的元素；如果没有这样的排队线程，则没有元素可供移除，{@code poll()} 将返回 {@code null}。为了其他 {@code Collection} 方法（例如 {@code contains}），{@code SynchronousQueue} 作为空集合处理。此队列不允许 {@code null} 元素。
 *
 * <p>同步队列类似于 CSP 和 Ada 中使用的会合通道。它们非常适合于传递设计，其中一个对象在一个线程中运行，必须与另一个线程中运行的对象同步，以便传递某些信息、事件或任务。
 *
 * <p>此类支持可选的公平策略来排序等待的生产者和消费者线程。默认情况下，此顺序不保证。但是，使用公平性设置为 {@code true} 构造的队列将按 FIFO 顺序授予线程访问权限。
 *
 * <p>此类及其迭代器实现了 {@link Collection} 和 {@link Iterator} 接口的所有 <em>可选</em> 方法。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.5
 * @author Doug Lea 和 Bill Scherer 和 Michael Scott
 * @param <E> 此集合中持有的元素类型
 */
public class SynchronousQueue<E> extends AbstractQueue<E>
    implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -3223113410248163686L;

    /*
     * 此类实现了 "Nonblocking Concurrent Objects with Condition Synchronization" 中描述的双栈和双队列算法的扩展，由 W. N. Scherer III 和 M. L. Scott。18th Annual Conf. on Distributed Computing, Oct. 2004（参见
     * http://www.cs.rochester.edu/u/scott/synchronization/pseudocode/duals.html）。
     * （Lifo）栈用于非公平模式，（Fifo）队列用于公平模式。两者的性能通常相似。Fifo 通常在争用下支持更高的吞吐量，但 Lifo 在常见应用中保持更高的线程局部性。
     *
     * 双队列（和类似地栈）在任何给定时间要么持有“数据”——由 put 操作提供的项目，要么持有“请求”——代表 take 操作的槽，或者为空。调用“fulfill”（即，从持有数据的队列请求项目或反之亦然的调用）会取消排队一个互补节点。这些队列最有趣的特性是任何操作都可以确定队列处于哪种模式，并相应地采取行动而不需要锁。
     *
     * 队列和栈扩展了定义单个方法 transfer 的抽象类 Transferer，该方法执行 put 或 take。这些方法被统一为一个方法，因为在双数据结构中，put 和 take 操作是对称的，因此几乎所有代码都可以合并。结果的 transfer 方法较长，但比分成几乎重复的部分更容易理解。
     *
     * 队列和栈在概念上有很多相似之处，但在具体细节上几乎没有共同点。为了简单起见，它们保持独立，以便以后可以分别发展。
     *
     * 这里的算法与上述论文中的版本不同，扩展了它们以用于同步队列，以及处理取消。主要区别包括：
     *
     * 1. 原始算法使用位标记指针，但这里的算法使用节点中的模式位，导致许多进一步的适应。
     * 2. SynchronousQueues 必须阻塞等待被满足的线程。
     * 3. 支持通过超时和中断取消，包括清理取消的节点/线程以避免垃圾保留和内存耗尽。
     *
     * 阻塞主要通过 LockSupport park/unpark 实现，除非节点似乎是下一个将被满足的节点（仅在多处理器上）。在非常繁忙的同步队列上，自旋可以显著提高吞吐量。而在不太繁忙的队列上，自旋的数量小到不会引起注意。
     *
     * 队列和栈的清理方式不同。对于队列，我们几乎总能在 O(1) 时间内（考虑到重试以进行一致性检查）立即移除一个取消的节点，但如果是当前尾部的节点，则必须等待某些后续取消。对于栈，我们需要一个可能的 O(n) 遍历来确保可以移除节点，但这可以与访问栈的其他线程并发运行。
     *
     * 尽管垃圾收集处理了大多数节点回收问题，否则会复杂化非阻塞算法，但还是注意“忘记”可能被阻塞线程长期持有的数据、其他节点和线程的引用。在设置为 null 会与主算法冲突的情况下，这是通过将节点的链接更改为指向节点本身来完成的。这在 Stack 节点中不常见（因为阻塞线程不会挂起旧的头指针），但 Queue 节点中的引用必须积极地“忘记”，以避免到达任何节点自到达以来曾经引用过的一切。
     */

    /**
     * 双栈和队列的共享内部 API。
     */
    abstract static class Transferer<E> {
        /**
         * 执行 put 或 take。
         *
         * @param e 如果非空，则是要传递给消费者的项目；如果为空，则请求 transfer 返回生产者提供的项目。
         * @param timed 如果此操作应超时
         * @param nanos 超时时间，以纳秒为单位
         * @return 如果非空，则提供或接收的项目；如果为空，则操作因超时或中断失败——调用者可以通过检查 Thread.interrupted 来区分这两种情况。
         */
        abstract E transfer(E e, boolean timed, long nanos);
    }

    /** CPU 数量，用于自旋控制 */
    static final int NCPUS = Runtime.getRuntime().availableProcessors();

    /**
     * 在定时等待前自旋的次数。
     * 该值是经验得出的——在各种处理器和操作系统上表现良好。经验表明，最佳值不会随 CPU 数量（超过 2 个）变化，因此只是一个常量。
     */
    static final int maxTimedSpins = (NCPUS < 2) ? 0 : 32;

    /**
     * 在非定时等待前自旋的次数。
     * 这个值大于定时值，因为非定时等待自旋更快，因为它们不需要在每次自旋时检查时间。
     */
    static final int maxUntimedSpins = maxTimedSpins * 16;

    /**
     * 自旋比使用定时 park 更快的纳秒数。一个粗略的估计就足够了。
     */
    static final long spinForTimeoutThreshold = 1000L;

    /** 双栈 */
    static final class TransferStack<E> extends Transferer<E> {
        /*
         * 这扩展了 Scherer-Scott 双栈算法，不同之处在于使用“覆盖”节点而不是位标记指针：满足操作在标记节点（模式中设置 FULFILLING 位）上推送，以预留一个匹配等待节点的空位。
         */

        /* SNode 的模式，与节点字段 OR 一起 */
        /** 节点表示未满足的消费者 */
        static final int REQUEST    = 0;
        /** 节点表示未满足的生产者 */
        static final int DATA       = 1;
        /** 节点正在满足另一个未满足的 DATA 或 REQUEST */
        static final int FULFILLING = 2;

        /** 如果 m 设置了满足位，则返回 true。 */
        static boolean isFulfilling(int m) { return (m & FULFILLING) != 0; }

        /** TransferStacks 的节点类。 */
        static final class SNode {
            volatile SNode next;        // 栈中的下一个节点
            volatile SNode match;       // 与此节点匹配的节点
            volatile Thread waiter;     // 用于控制 park/unpark 的线程
            Object item;                // 数据；或 REQUESTs 为 null
            int mode;
            // 注意：item 和 mode 字段不需要是 volatile
            // 因为它们总是在其他 volatile/atomic 操作之前写入，之后读取。

            SNode(Object item) {
                this.item = item;
            }

            boolean casNext(SNode cmp, SNode val) {
                return cmp == next &&
                    UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
            }

            /**
             * 尝试将节点 s 匹配到此节点，如果成功，则唤醒线程。
             * 满足者调用 tryMatch 以识别其等待者。
             * 等待者在被匹配后阻塞。
             *
             * @param s 要匹配的节点
             * @return 如果成功匹配到 s，则返回 true
             */
            boolean tryMatch(SNode s) {
                if (match == null &&
                    UNSAFE.compareAndSwapObject(this, matchOffset, null, s)) {
                    Thread w = waiter;
                    if (w != null) {    // 等待者最多需要一次 unpark
                        waiter = null;
                        LockSupport.unpark(w);
                    }
                    return true;
                }
                return match == s;
            }

            /**
             * 尝试通过将节点匹配到自身来取消等待。
             */
            void tryCancel() {
                UNSAFE.compareAndSwapObject(this, matchOffset, null, this);
            }

            boolean isCancelled() {
                return match == this;
            }

            // Unsafe 机制
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

        /** 栈的头（顶部） */
        volatile SNode head;

        boolean casHead(SNode h, SNode nh) {
            return h == head &&
                UNSAFE.compareAndSwapObject(this, headOffset, h, nh);
        }

        /**
         * 创建或重置节点的字段。仅从 transfer 调用，其中要推入栈的节点是懒惰创建的，并在可能的情况下重用，以帮助减少读取和 CAS 头之间的间隔，并避免由于争用导致的节点推入 CAS 失败时的垃圾激增。
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
             * 1. 如果显然为空或已经包含相同模式的节点，尝试将节点推入栈并等待匹配，返回它，或如果取消则返回 null。
             *
             * 2. 如果显然包含互补模式的节点，尝试将一个满足节点推入栈，与相应的等待节点匹配，从栈中弹出两者，并返回匹配的项目。匹配或取消链接可能实际上并不需要，因为其他线程执行了操作 3：
             *
             * 3. 如果栈顶已经持有另一个满足节点，帮助它完成匹配和/或弹出操作，然后继续。帮助的代码基本上与满足的代码相同，只是它不返回项目。
             */


                        SNode s = null; // 构建/重用，按需
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
         * 旋转/阻塞直到节点 s 被完成操作匹配。
         *
         * @param s 等待的节点
         * @param timed 如果是定时等待
         * @param nanos 超时值
         * @return 匹配的节点，或 s 如果被取消
         */
        SNode awaitFulfill(SNode s, boolean timed, long nanos) {
            /*
             * 当一个节点/线程即将阻塞时，它设置其 waiter
             * 字段，然后在实际停放之前至少再检查一次状态，从而覆盖
             * 完成者注意到 waiter 不为空所以应该唤醒的竞态。
             *
             * 当调用点出现的节点在调用时似乎位于堆栈头部时，park 调用
             * 之前会进行自旋以避免在生产者和消费者几乎同时到达时阻塞。
             * 这种情况在多处理器上才会频繁发生。
             *
             * 主循环中检查返回的顺序反映了中断优先于
             * 正常返回，而正常返回优先于超时。因此，在超时后，会再进行一次
             * 匹配检查，然后才放弃。除了 SynchronousQueue.{poll/offer}
             * 的未定时调用不检查中断也不等待，因此被截获在 transfer
             * 方法中而不是调用 awaitFulfill。
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
                    s.waiter = w; // 建立 waiter 以便下次迭代停放
                else if (!timed)
                    LockSupport.park(this);
                else if (nanos > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanos);
            }
        }

        /**
         * 如果节点 s 在头部或有活动的完成者，则返回 true。
         */
        boolean shouldSpin(SNode s) {
            SNode h = head;
            return (h == s || h == null || isFulfilling(h.mode));
        }

        /**
         * 从堆栈中解链 s。
         */
        void clean(SNode s) {
            s.item = null;   // 忘记 item
            s.waiter = null; // 忘记线程

            /*
             * 最坏情况下，我们可能需要遍历整个堆栈来解链 s。如果有多次并发调用 clean，我们
             * 可能看不到 s，因为另一个线程已经移除了它。但我们可以在看到任何已知
             * 跟随 s 的节点时停止。我们使用 s.next，除非它也被取消，在这种情况下我们尝试
             * 一个节点之后。我们不再进一步检查，因为我们不想双重遍历只是为了找到哨兵。
             */

            SNode past = s.next;
            if (past != null && past.isCancelled())
                past = past.next;

            // 吸收头部的已取消节点
            SNode p;
            while ((p = head) != null && p != past && p.isCancelled())
                casHead(p, p.next);

            // 解链嵌入的节点
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
         * 这扩展了 Scherer-Scott 双队列算法，不同之处在于使用节点中的模式而不是
         * 标记指针。该算法比堆栈的算法稍微简单一些，因为完成者不需要显式节点，
         * 匹配是通过 CAS QNode.item 字段从非空到空（对于 put）或反之（对于 take）来完成的。
         */

        /** TransferQueue 的节点类。 */
        static final class QNode {
            volatile QNode next;          // 队列中的下一个节点
            volatile Object item;         // CAS 为或从 null
            volatile Thread waiter;       // 用于控制停放/唤醒
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
             * 尝试通过 CAS 将 this 作为 item 取消。
             */
            void tryCancel(Object cmp) {
                UNSAFE.compareAndSwapObject(this, itemOffset, cmp, this);
            }

            boolean isCancelled() {
                return item == this;
            }

            /**
             * 如果此节点已知不在队列中，因为其 next 指针已因
             * advanceHead 操作而被忘记，则返回 true。
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
         * 可能尚未从队列中解链的已取消节点的引用，因为它是
         * 取消时最后一个插入的节点。
         */
        transient volatile QNode cleanMe;

        TransferQueue() {
            QNode h = new QNode(null, false); // 初始化为虚拟节点。
            head = h;
            tail = h;
        }

        /**
         * 尝试将 nh 作为新的头部；如果成功，解链
         * 旧头部的下一个节点以避免垃圾保留。
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
            /* 基本算法是循环尝试执行以下两种操作之一：
             *
             * 1. 如果队列显然为空或包含相同模式的节点，
             *    尝试将节点添加到等待者队列中，等待被
             *    完成（或取消），然后返回匹配的项目。
             *
             * 2. 如果队列显然包含等待的项目，且此调用是
             *    互补模式，尝试通过 CAS 等待节点的 item 字段并
             *    取消排队，然后返回匹配的项目。
             *
             * 在每种情况下，途中检查并尝试帮助
             * 代表其他停滞/缓慢的线程推进头部和尾部。
             *
             * 循环从一个空检查开始，防止看到未初始化的 head 或 tail 值。
             * 这在当前的 SynchronousQueue 中不会发生，但如果调用者持有
             * transferer 的非易失性/非 final 引用，就可能发生。无论如何，检查在这里
             * 是因为将空检查放在循环顶部通常比隐式地分散在循环中更快。
             */

            QNode s = null; // 构建/重用，按需
            boolean isData = (e != null);

            for (;;) {
                QNode t = tail;
                QNode h = head;
                if (t == null || h == null)         // 看到了未初始化的值
                    continue;                       // 自旋

                if (h == t || t.isData == isData) { // 空或相同模式
                    QNode tn = t.next;
                    if (t != tail)                  // 不一致的读取
                        continue;
                    if (tn != null) {               // 滞后的尾部
                        advanceTail(t, tn);
                        continue;
                    }
                    if (timed && nanos <= 0)        // 无法等待
                        return null;
                    if (s == null)
                        s = new QNode(e, isData);
                    if (!t.casNext(null, s))        // 链接失败
                        continue;

                    advanceTail(t, s);              // 转移尾部并等待
                    Object x = awaitFulfill(s, e, timed, nanos);
                    if (x == s) {                   // 等待被取消
                        clean(t, s);
                        return null;
                    }

                    if (!s.isOffList()) {           // 未被解链
                        advanceHead(t, s);          // 如果是头部则解链
                        if (x != null)              // 并忘记字段
                            s.item = s;
                        s.waiter = null;
                    }
                    return (x != null) ? (E)x : e;

                } else {                            // 互补模式
                    QNode m = h.next;               // 要完成的节点
                    if (t != tail || m == null || h != head)
                        continue;                   // 不一致的读取

                    Object x = m.item;
                    if (isData == (x != null) ||    // m 已经完成
                        x == m ||                   // m 取消
                        !m.casItem(x, e)) {         // CAS 失败
                        advanceHead(h, m);          // 取消排队并重试
                        continue;
                    }


                                advanceHead(h, m);              // 成功完成
                    LockSupport.unpark(m.waiter);
                    return (x != null) ? (E)x : e;
                }
            }
        }

        /**
         * 旋转/阻塞直到节点 s 被完成。
         *
         * @param s 等待的节点
         * @param e 用于检查匹配的比较值
         * @param timed 如果是定时等待则为 true
         * @param nanos 超时值
         * @return 匹配的项，或如果已取消则返回 s
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
             * 至少 s 节点或之前保存的节点之一总是可以被删除的，所以这总是会终止。
             */
            while (pred.next == s) { // 如果已经解除链接则提前返回
                QNode h = head;
                QNode hn = h.next;   // 将已取消的第一个节点作为头节点吸收
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
                if (s != t) {        // 如果不是尾部，尝试解链
                    QNode sn = s.next;
                    if (sn == s || pred.casNext(s, sn))
                        return;
                }
                QNode dp = cleanMe;
                if (dp != null) {    // 尝试解除先前取消的节点
                    QNode d = dp.next;
                    QNode dn;
                    if (d == null ||               // d 已消失或
                        d == dp ||                 // d 已不在列表中或
                        !d.isCancelled() ||        // d 未取消或
                        (d != t &&                 // d 不是尾部且
                         (dn = d.next) != null &&  //   有后继
                         dn != d &&                //   该后继在列表中
                         dp.casNext(d, dn)))       // d 已解除链接
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
     * 转移器。仅在构造函数中设置，但不能声明为 final 以避免进一步复杂化序列化。由于这在每个公共方法中最多访问一次，
     * 使用 volatile 代替 final 并没有明显的性能损失。
     */
    private transient volatile Transferer<E> transferer;

    /**
     * 创建具有非公平访问策略的 {@code SynchronousQueue}。
     */
    public SynchronousQueue() {
        this(false);
    }

    /**
     * 创建具有指定公平策略的 {@code SynchronousQueue}。
     *
     * @param fair 如果为 true，等待的线程以 FIFO 顺序争夺访问权；否则顺序未指定。
     */
    public SynchronousQueue(boolean fair) {
        transferer = fair ? new TransferQueue<E>() : new TransferStack<E>();
    }

    /**
     * 将指定的元素添加到此队列中，必要时等待其他线程接收它。
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
     * 将指定的元素插入到此队列中，必要时等待指定的等待时间，直到其他线程接收它。
     *
     * @return 如果成功则返回 {@code true}，或如果指定的等待时间到期前没有消费者出现则返回 {@code false}
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
     * 将指定的元素插入到此队列中，如果其他线程正在等待接收它。
     *
     * @param e 要添加的元素
     * @return 如果元素被添加到此队列则返回 {@code true}，否则返回 {@code false}
     * @throws NullPointerException 如果指定的元素为 null
     */
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        return transferer.transfer(e, true, 0) != null;
    }

    /**
     * 检索并移除此队列的头部，必要时等待其他线程插入它。
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
     * 检索并移除此队列的头部，必要时等待指定的等待时间，直到其他线程插入它。
     *
     * @return 此队列的头部，或如果指定的等待时间到期前没有元素出现则返回 {@code null}
     * @throws InterruptedException {@inheritDoc}
     */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E e = transferer.transfer(null, true, unit.toNanos(timeout));
        if (e != null || !Thread.interrupted())
            return e;
        throw new InterruptedException();
    }

    /**
     * 检索并移除此队列的头部，如果其他线程当前正在提供一个元素。
     *
     * @return 此队列的头部，或如果没有元素可用则返回 {@code null}
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
     * 如果给定的集合为空则返回 {@code true}，否则返回 {@code false}。
     * {@code SynchronousQueue} 没有内部容量。
     *
     * @param c 集合
     * @return 如果给定的集合为空则返回 {@code true}，否则返回 {@code false}
     */
    public boolean containsAll(Collection<?> c) {
        return c.isEmpty();
    }

    /**
     * 始终返回 {@code false}。
     * {@code SynchronousQueue} 没有内部容量。
     *
     * @param c 集合
     * @return {@code false}
     */
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    /**
     * 始终返回 {@code false}。
     * {@code SynchronousQueue} 没有内部容量。
     *
     * @param c 集合
     * @return {@code false}
     */
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    /**
     * 始终返回 {@code null}。
     * {@code SynchronousQueue} 除非主动等待，否则不会返回元素。
     *
     * @return {@code null}
     */
    public E peek() {
        return null;
    }

    /**
     * 返回一个空迭代器，其中 {@code hasNext} 始终返回 {@code false}。
     *
     * @return 一个空迭代器
     */
    public Iterator<E> iterator() {
        return Collections.emptyIterator();
    }

    /**
     * 返回一个空的 spliterator，其中调用 {@link java.util.Spliterator#trySplit()} 始终返回 {@code null}。
     *
     * @return 一个空的 spliterator
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
     * 将指定数组的零索引元素设置为 {@code null}（如果数组长度非零）并返回它。
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
     * 为了应对 1.5 版本的 SynchronousQueue 的序列化策略，我们声明了一些从未使用的类和字段，
     * 这些字段仅存在于启用跨版本序列化的情况下。这些字段从未使用，因此仅在对象序列化或反序列化时初始化。
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
     * 将此队列保存到流中（即序列化它）。
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
     * 从流中重新构造此队列（即反序列化它）。
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
