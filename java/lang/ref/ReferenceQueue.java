
/*
 * 版权所有 (c) 1997, 2017, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang.ref;

import java.util.function.Consumer;

/**
 * 引用队列，注册的引用对象在检测到适当的可达性变化后由垃圾收集器追加到此队列。
 *
 * @author   Mark Reinhold
 * @since    1.2
 */

public class ReferenceQueue<T> {

    /**
     * 构造一个新的引用对象队列。
     */
    public ReferenceQueue() { }

    private static class Null<S> extends ReferenceQueue<S> {
        boolean enqueue(Reference<? extends S> r) {
            return false;
        }
    }

    static ReferenceQueue<Object> NULL = new Null<>();
    static ReferenceQueue<Object> ENQUEUED = new Null<>();

    static private class Lock { };
    private Lock lock = new Lock();
    private volatile Reference<? extends T> head = null;
    private long queueLength = 0;

    boolean enqueue(Reference<? extends T> r) { /* 仅由 Reference 类调用 */
        synchronized (lock) {
            // 检查自获取锁以来此引用是否已被入队（甚至已被移除）
            ReferenceQueue<?> queue = r.queue;
            if ((queue == NULL) || (queue == ENQUEUED)) {
                return false;
            }
            assert queue == this;
            r.next = (head == null) ? r : head;
            head = r;
            queueLength++;
            // 在添加到列表后更新 r.queue，以避免与并发入队检查和快速路径 poll() 的竞争
            // 自旋变量确保顺序。
            r.queue = ENQUEUED;
            if (r instanceof FinalReference) {
                sun.misc.VM.addFinalRefCount(1);
            }
            lock.notifyAll();
            return true;
        }
    }

    private Reference<? extends T> reallyPoll() {       /* 必须持有锁 */
        Reference<? extends T> r = head;
        if (r != null) {
            r.queue = NULL;
            // 在从列表中移除之前更新 r.queue，以避免与并发入队检查和快速路径
            // poll() 的竞争。自旋变量确保顺序。
            @SuppressWarnings("unchecked")
            Reference<? extends T> rn = r.next;
            head = (rn == r) ? null : rn;
            r.next = r;
            queueLength--;
            if (r instanceof FinalReference) {
                sun.misc.VM.addFinalRefCount(-1);
            }
            return r;
        }
        return null;
    }

    /**
     * 检查此队列中是否有可用的引用对象。如果有可用的引用对象，则立即将其从队列中移除并返回。
     * 否则，此方法立即返回 <tt>null</tt>。
     *
     * @return  如果立即可用，则返回一个引用对象，否则返回 <code>null</code>
     */
    public Reference<? extends T> poll() {
        if (head == null)
            return null;
        synchronized (lock) {
            return reallyPoll();
        }
    }

    /**
     * 从队列中移除下一个引用对象，阻塞直到一个可用或给定的超时时间到期。
     *
     * <p> 此方法不提供实时保证：它像调用 {@link Object#wait(long)} 方法一样调度超时。
     *
     * @param  timeout  如果为正数，阻塞最多 <code>timeout</code>
     *                  毫秒，等待引用被添加到此队列。如果为零，无限期阻塞。
     *
     * @return  如果在指定的超时期间内有可用的引用对象，则返回一个引用对象，否则返回 <code>null</code>
     *
     * @throws  IllegalArgumentException
     *          如果超时参数的值为负
     *
     * @throws  InterruptedException
     *          如果超时等待被中断
     */
    public Reference<? extends T> remove(long timeout)
        throws IllegalArgumentException, InterruptedException
    {
        if (timeout < 0) {
            throw new IllegalArgumentException("Negative timeout value");
        }
        synchronized (lock) {
            Reference<? extends T> r = reallyPoll();
            if (r != null) return r;
            long start = (timeout == 0) ? 0 : System.nanoTime();
            for (;;) {
                lock.wait(timeout);
                r = reallyPoll();
                if (r != null) return r;
                if (timeout != 0) {
                    long end = System.nanoTime();
                    timeout -= (end - start) / 1000_000;
                    if (timeout <= 0) return null;
                    start = end;
                }
            }
        }
    }

    /**
     * 从队列中移除下一个引用对象，阻塞直到一个可用。
     *
     * @return  阻塞直到一个可用的引用对象
     * @throws  InterruptedException  如果等待被中断
     */
    public Reference<? extends T> remove() throws InterruptedException {
        return remove(0);
    }

    /**
     * 遍历队列并使用每个引用调用给定的操作。
     * 适用于诊断目的。
     * 警告：此方法的任何使用都应确保不保留迭代引用的引用对象（在 FinalReference(s) 的情况下）
     * 以避免其生命周期不必要的延长。
     */
    void forEach(Consumer<? super Reference<? extends T>> action) {
        for (Reference<? extends T> r = head; r != null;) {
            action.accept(r);
            @SuppressWarnings("unchecked")
            Reference<? extends T> rn = r.next;
            if (rn == r) {
                if (r.queue == ENQUEUED) {
                    // 仍在队列中 -> 我们到达了链的末尾
                    r = null;
                } else {
                    // 已从队列中移除: r.queue == NULL; ->
                    // 当被队列轮询者超越时，从头开始重新启动
                    r = head;
                }
            } else {
                // 链中的下一个
                r = rn;
            }
        }
    }
}
