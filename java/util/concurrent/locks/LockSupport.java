
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

package java.util.concurrent.locks;
import sun.misc.Unsafe;

/**
 * 用于创建锁和其他同步类的基本线程阻塞原语。
 *
 * <p>此类为每个使用它的线程关联一个许可（在 {@link java.util.concurrent.Semaphore
 * Semaphore} 类的意义上）。调用 {@code park} 如果许可可用，将立即返回，同时消耗该许可；否则它 <em>可能</em> 阻塞。调用 {@code unpark} 使许可可用，如果它尚未可用。 （与 Semaphore 不同，许可不会累积。最多只有一个。）
 *
 * <p>方法 {@code park} 和 {@code unpark} 提供了高效的线程阻塞和解除阻塞手段，不会遇到已弃用的方法 {@code Thread.suspend}
 * 和 {@code Thread.resume} 用于此类目的时遇到的问题：在其中一个线程调用 {@code park} 和另一个线程尝试 {@code unpark} 之间发生的竞争条件将由于许可的存在而保持活跃。此外，如果调用者的线程被中断，{@code park} 将返回，并支持超时版本。 {@code park} 方法也可能在任何时候，出于“没有原因”而返回，因此通常必须在返回时重新检查条件的循环中调用。在这种意义上，{@code park} 作为“忙等待”的优化，不会浪费太多时间自旋，但必须与 {@code unpark} 配对才能有效。
 *
 * <p>{@code park} 的三种形式还支持一个 {@code blocker} 对象参数。此对象在线程阻塞时记录，以便监控和诊断工具可以识别线程阻塞的原因。（这些工具可以使用方法 {@link #getBlocker(Thread)} 访问阻塞器。）强烈建议使用这些形式而不是没有此参数的原始形式。在锁实现中，作为 {@code blocker} 提供的正常参数是 {@code this}。
 *
 * <p>这些方法设计用于创建更高层次的同步工具，本身对于大多数并发控制应用程序没有直接用处。 {@code park} 方法设计仅用于以下形式的构造：
 *
 *  <pre> {@code
 * while (!canProceed()) { ... LockSupport.park(this); }}</pre>
 *
 * 其中 {@code canProceed} 或调用 {@code park} 之前的任何其他操作都不涉及锁定或阻塞。因为每个线程只关联一个许可，任何中间使用 {@code park} 都可能干扰其预期效果。
 *
 * <p><b>示例用法。</b> 以下是一个先进先出非可重入锁类的草图：
 *  <pre> {@code
 * class FIFOMutex {
 *   private final AtomicBoolean locked = new AtomicBoolean(false);
 *   private final Queue<Thread> waiters
 *     = new ConcurrentLinkedQueue<Thread>();
 *
 *   public void lock() {
 *     boolean wasInterrupted = false;
 *     Thread current = Thread.currentThread();
 *     waiters.add(current);
 *
 *     // 在队列中不是第一个或无法获取锁时阻塞
 *     while (waiters.peek() != current ||
 *            !locked.compareAndSet(false, true)) {
 *       LockSupport.park(this);
 *       if (Thread.interrupted()) // 忽略等待时的中断
 *         wasInterrupted = true;
 *     }
 *
 *     waiters.remove();
 *     if (wasInterrupted)          // 在退出时重新设置中断状态
 *       current.interrupt();
 *   }
 *
 *   public void unlock() {
 *     locked.set(false);
 *     LockSupport.unpark(waiters.peek());
 *   }
 * }}</pre>
 */
public class LockSupport {
    private LockSupport() {} // 不能实例化。

    private static void setBlocker(Thread t, Object arg) {
        // 即使是 volatile，HotSpot 也不需要在这里写屏障。
        UNSAFE.putObject(t, parkBlockerOffset, arg);
    }

    /**
     * 使给定线程的许可可用，如果它尚未可用。如果线程在 {@code park} 上阻塞，则它将解除阻塞。否则，它下次调用 {@code park} 时保证不会阻塞。如果给定线程尚未启动，此操作可能没有任何效果。
     *
     * @param thread 要解除阻塞的线程，或 {@code null}，在这种情况下此操作没有效果
     */
    public static void unpark(Thread thread) {
        if (thread != null)
            UNSAFE.unpark(thread);
    }

    /**
     * 如果许可可用，则禁用当前线程以进行线程调度。
     *
     * <p>如果许可可用，则消耗该许可并立即返回；否则
     * 当前线程将被禁用以进行线程调度
     * 并处于休眠状态，直到发生以下三种情况之一：
     *
     * <ul>
     * <li>其他线程调用 {@link #unpark unpark} 以当前线程为目标；或
     *
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 当前线程；或
     *
     * <li>调用无故（即，没有任何原因）返回。
     * </ul>
     *
     * <p>此方法不报告导致方法返回的原因。调用者应重新检查导致线程阻塞的条件。调用者还可以确定，例如，线程的中断状态或返回时的情况。
     *
     * @param blocker 负责此线程阻塞的同步对象
     * @since 1.6
     */
    public static void park(Object blocker) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        UNSAFE.park(false, 0L);
        setBlocker(t, null);
    }

    /**
     * 如果许可可用，则禁用当前线程以进行线程调度，最长等待指定的等待时间。
     *
     * <p>如果许可可用，则消耗该许可并立即返回；否则当前线程将被禁用以进行线程调度
     * 并处于休眠状态，直到发生以下四种情况之一：
     *
     * <ul>
     * <li>其他线程调用 {@link #unpark unpark} 以当前线程为目标；或
     *
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 当前线程；或
     *
     * <li>指定的等待时间已过；或
     *
     * <li>调用无故（即，没有任何原因）返回。
     * </ul>
     *
     * <p>此方法不报告导致方法返回的原因。调用者应重新检查导致线程阻塞的条件。调用者还可以确定，例如，线程的中断状态或返回时的经过时间。
     *
     * @param blocker 负责此线程阻塞的同步对象
     * @param nanos 最长等待的纳秒数
     * @since 1.6
     */
    public static void parkNanos(Object blocker, long nanos) {
        if (nanos > 0) {
            Thread t = Thread.currentThread();
            setBlocker(t, blocker);
            UNSAFE.park(false, nanos);
            setBlocker(t, null);
        }
    }

    /**
     * 如果许可可用，则禁用当前线程以进行线程调度，直到指定的截止时间。
     *
     * <p>如果许可可用，则消耗该许可并立即返回；否则当前线程将被禁用以进行线程调度
     * 并处于休眠状态，直到发生以下四种情况之一：
     *
     * <ul>
     * <li>其他线程调用 {@link #unpark unpark} 以当前线程为目标；或
     *
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 当前线程；或
     *
     * <li>指定的截止时间已过；或
     *
     * <li>调用无故（即，没有任何原因）返回。
     * </ul>
     *
     * <p>此方法不报告导致方法返回的原因。调用者应重新检查导致线程阻塞的条件。调用者还可以确定，例如，线程的中断状态或返回时的当前时间。
     *
     * @param blocker 负责此线程阻塞的同步对象
     * @param deadline 从纪元开始的毫秒数，等待到此截止时间
     * @since 1.6
     */
    public static void parkUntil(Object blocker, long deadline) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        UNSAFE.park(true, deadline);
        setBlocker(t, null);
    }

    /**
     * 返回最近一次尚未解除阻塞的 park 方法调用中提供的阻塞对象，或如果未阻塞则返回 null。返回的值只是一个瞬间快照——线程可能已经解除阻塞或在不同的阻塞对象上阻塞。
     *
     * @param t 线程
     * @return 阻塞对象
     * @throws NullPointerException 如果参数为 null
     * @since 1.6
     */
    public static Object getBlocker(Thread t) {
        if (t == null)
            throw new NullPointerException();
        return UNSAFE.getObjectVolatile(t, parkBlockerOffset);
    }

    /**
     * 如果许可可用，则禁用当前线程以进行线程调度。
     *
     * <p>如果许可可用，则消耗该许可并立即返回；否则当前线程将被禁用以进行线程调度
     * 并处于休眠状态，直到发生以下三种情况之一：
     *
     * <ul>
     *
     * <li>其他线程调用 {@link #unpark unpark} 以当前线程为目标；或
     *
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 当前线程；或
     *
     * <li>调用无故（即，没有任何原因）返回。
     * </ul>
     *
     * <p>此方法不报告导致方法返回的原因。调用者应重新检查导致线程阻塞的条件。调用者还可以确定，例如，线程的中断状态或返回时的情况。
     */
    public static void park() {
        UNSAFE.park(false, 0L);
    }

    /**
     * 如果许可可用，则禁用当前线程以进行线程调度，最长等待指定的等待时间。
     *
     * <p>如果许可可用，则消耗该许可并立即返回；否则当前线程将被禁用以进行线程调度
     * 并处于休眠状态，直到发生以下四种情况之一：
     *
     * <ul>
     * <li>其他线程调用 {@link #unpark unpark} 以当前线程为目标；或
     *
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 当前线程；或
     *
     * <li>指定的等待时间已过；或
     *
     * <li>调用无故（即，没有任何原因）返回。
     * </ul>
     *
     * <p>此方法不报告导致方法返回的原因。调用者应重新检查导致线程阻塞的条件。调用者还可以确定，例如，线程的中断状态或返回时的经过时间。
     *
     * @param nanos 最长等待的纳秒数
     */
    public static void parkNanos(long nanos) {
        if (nanos > 0)
            UNSAFE.park(false, nanos);
    }

    /**
     * 如果许可可用，则禁用当前线程以进行线程调度，直到指定的截止时间。
     *
     * <p>如果许可可用，则消耗该许可并立即返回；否则当前线程将被禁用以进行线程调度
     * 并处于休眠状态，直到发生以下四种情况之一：
     *
     * <ul>
     * <li>其他线程调用 {@link #unpark unpark} 以当前线程为目标；或
     *
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 当前线程；或
     *
     * <li>指定的截止时间已过；或
     *
     * <li>调用无故（即，没有任何原因）返回。
     * </ul>
     *
     * <p>此方法不报告导致方法返回的原因。调用者应重新检查导致线程阻塞的条件。调用者还可以确定，例如，线程的中断状态或返回时的当前时间。
     *
     * @param deadline 从纪元开始的毫秒数，等待到此截止时间
     */
    public static void parkUntil(long deadline) {
        UNSAFE.park(true, deadline);
    }

    /**
     * 返回伪随机初始化或更新的次级种子。
     * 由于包访问限制，从 ThreadLocalRandom 复制。
     */
    static final int nextSecondarySeed() {
        int r;
        Thread t = Thread.currentThread();
        if ((r = UNSAFE.getInt(t, SECONDARY)) != 0) {
            r ^= r << 13;   // xorshift
            r ^= r >>> 17;
            r ^= r << 5;
        }
        else if ((r = java.util.concurrent.ThreadLocalRandom.current().nextInt()) == 0)
            r = 1; // 避免零
        UNSAFE.putInt(t, SECONDARY, r);
        return r;
    }


                // 通过内在函数API实现热点
    private static final sun.misc.Unsafe UNSAFE;
    private static final long parkBlockerOffset;
    private static final long SEED;
    private static final long PROBE;
    private static final long SECONDARY;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> tk = Thread.class;
            parkBlockerOffset = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("parkBlocker"));
            SEED = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomSeed"));
            PROBE = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomProbe"));
            SECONDARY = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomSecondarySeed"));
        } catch (Exception ex) { throw new Error(ex); }
    }

}
