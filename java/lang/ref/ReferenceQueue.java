/*
 * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.ref;

import java.util.function.Consumer;

/**
 * 引用队列，注册的引用对象在垃圾收集器检测到适当的可达性变化后会被追加到此队列。
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
            // 检查在获取锁之后，此引用是否已经入队（甚至被移除）
            ReferenceQueue<?> queue = r.queue;
            if ((queue == NULL) || (queue == ENQUEUED)) {
                return false;
            }
            assert queue == this;
            r.queue = ENQUEUED;
            r.next = (head == null) ? r : head;
            head = r;
            queueLength++;
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
            @SuppressWarnings("unchecked")
            Reference<? extends T> rn = r.next;
            head = (rn == r) ? null : rn;
            r.queue = NULL;
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
     * 从队列中移除下一个引用对象，阻塞直到有引用对象可用或给定的超时时间到期。
     *
     * <p> 此方法不提供实时保证：它像调用 {@link Object#wait(long)} 方法一样调度超时。
     *
     * @param  timeout  如果为正数，阻塞最多 <code>timeout</code> 毫秒，等待引用对象被添加到此队列。
     *                  如果为零，无限期阻塞。
     *
     * @return  如果在指定的超时时间内有可用的引用对象，则返回一个引用对象，否则返回 <code>null</code>
     *
     * @throws  IllegalArgumentException
     *          如果超时参数的值为负数
     *
     * @throws  InterruptedException
     *          如果超时等待被中断
     */
    public Reference<? extends T> remove(long timeout)
        throws IllegalArgumentException, InterruptedException
    {
        if (timeout < 0) {
            throw new IllegalArgumentException("负的超时值");
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
     * 从队列中移除下一个引用对象，阻塞直到有引用对象可用。
     *
     * @return 阻塞直到有引用对象可用
     * @throws  InterruptedException  如果等待被中断
     */
    public Reference<? extends T> remove() throws InterruptedException {
        return remove(0);
    }

    /**
     * 遍历队列并使用给定的操作调用每个引用。
     * 适用于诊断目的。
     * 警告：使用此方法时应确保不会保留迭代引用的引用对象（特别是 FinalReference(s)），以避免其生命周期不必要的延长。
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
                    // 已经出队：r.queue == NULL; ->
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
