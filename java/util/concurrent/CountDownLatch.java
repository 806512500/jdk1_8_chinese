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
 * 由 Doug Lea 在 JCP JSR-166 专家组成员的帮助下编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的条款发布到公共领域。
 */

package java.util.concurrent;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 一个同步辅助工具，允许一个或多个线程等待其他线程中执行的一组操作完成。
 *
 * <p>{@code CountDownLatch} 初始化时给定一个 <em>计数</em>。
 * {@link #await await} 方法会阻塞，直到当前计数因调用 {@link #countDown} 方法而达到零，
 * 之后所有等待的线程将被释放，任何后续的 {@link #await await} 调用将立即返回。
 * 这是一个一次性现象——计数不能重置。如果你需要一个可以重置计数的版本，可以考虑使用 {@link CyclicBarrier}。
 *
 * <p>{@code CountDownLatch} 是一个多功能的同步工具，可以用于多种目的。
 * 用计数为一的 {@code CountDownLatch} 作为简单的开/关闩锁或门：所有调用 {@link #await await} 的线程
 * 都会在门打开之前等待，直到被调用 {@link #countDown} 的线程打开门。
 * 用计数为 <em>N</em> 的 {@code CountDownLatch} 可以使一个线程等待直到 <em>N</em> 个线程完成某个操作，
 * 或者某个操作被完成 <em>N</em> 次。
 *
 * <p>{@code CountDownLatch} 的一个有用特性是，它不要求调用 {@code countDown} 的线程在计数达到零之前等待，
 * 它只是阻止任何线程在所有线程都能通过之前通过 {@link #await await}。
 *
 * <p><b>示例用法：</b> 以下是一对类，其中一组工作线程使用两个倒计时闩锁：
 * <ul>
 * <li>第一个是开始信号，防止任何工作线程在驱动程序准备好之前继续；
 * <li>第二个是完成信号，允许驱动程序等待所有工作线程完成。
 * </ul>
 *
 *  <pre> {@code
 * class Driver { // ...
 *   void main() throws InterruptedException {
 *     CountDownLatch startSignal = new CountDownLatch(1);
 *     CountDownLatch doneSignal = new CountDownLatch(N);
 *
 *     for (int i = 0; i < N; ++i) // 创建并启动线程
 *       new Thread(new Worker(startSignal, doneSignal)).start();
 *
 *     doSomethingElse();            // 不让运行
 *     startSignal.countDown();      // 让所有线程继续
 *     doSomethingElse();
 *     doneSignal.await();           // 等待所有完成
 *   }
 * }
 *
 * class Worker implements Runnable {
 *   private final CountDownLatch startSignal;
 *   private final CountDownLatch doneSignal;
 *   Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
 *     this.startSignal = startSignal;
 *     this.doneSignal = doneSignal;
 *   }
 *   public void run() {
 *     try {
 *       startSignal.await();
 *       doWork();
 *       doneSignal.countDown();
 *     } catch (InterruptedException ex) {} // 返回;
 *   }
 *
 *   void doWork() { ... }
 * }}</pre>
 *
 * <p>另一种典型的用法是将问题分为 N 个部分，每个部分用一个 Runnable 描述，该 Runnable 执行该部分并减少闩锁的计数，
 * 并将所有 Runnable 排队到一个 Executor。当所有子部分完成时，协调线程将能够通过 await。（当线程必须以这种方式反复减少计数时，改用 {@link CyclicBarrier}。）
 *
 *  <pre> {@code
 * class Driver2 { // ...
 *   void main() throws InterruptedException {
 *     CountDownLatch doneSignal = new CountDownLatch(N);
 *     Executor e = ...
 *
 *     for (int i = 0; i < N; ++i) // 创建并启动线程
 *       e.execute(new WorkerRunnable(doneSignal, i));
 *
 *     doneSignal.await();           // 等待所有完成
 *   }
 * }
 *
 * class WorkerRunnable implements Runnable {
 *   private final CountDownLatch doneSignal;
 *   private final int i;
 *   WorkerRunnable(CountDownLatch doneSignal, int i) {
 *     this.doneSignal = doneSignal;
 *     this.i = i;
 *   }
 *   public void run() {
 *     try {
 *       doWork(i);
 *       doneSignal.countDown();
 *     } catch (InterruptedException ex) {} // 返回;
 *   }
 *
 *   void doWork() { ... }
 * }}</pre>
 *
 * <p>内存一致性效果：直到计数达到零之前，线程中调用
 * {@code countDown()}
 * <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 另一个线程中从对应的
 * {@code await()} 成功返回后的动作。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class CountDownLatch {
    /**
     * CountDownLatch 的同步控制。
     * 使用 AQS 状态表示计数。
     */
    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            // 减少计数；在转换为零时发出信号
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c-1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

    private final Sync sync;

    /**
     * 构造一个初始化为给定计数的 {@code CountDownLatch}。
     *
     * @param count 必须调用 {@link #countDown} 的次数，线程才能通过 {@link #await}
     * @throws IllegalArgumentException 如果 {@code count} 为负数
     */
    public CountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    /**
     * 使当前线程等待，直到计数器减至零，除非线程被 {@linkplain Thread#interrupt 中断}。
     *
     * <p>如果当前计数为零，则此方法立即返回。
     *
     * <p>如果当前计数大于零，则当前线程将被禁用以进行线程调度，并处于休眠状态，直到以下两种情况之一发生：
     * <ul>
     * <li>由于调用了 {@link #countDown} 方法，计数达到零；或
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 了当前线程。
     * </ul>
     *
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时已设置中断状态；或
     * <li>在等待时被 {@linkplain Thread#interrupt 中断}，
     * </ul>
     * 则抛出 {@link InterruptedException}，并且当前线程的中断状态被清除。
     *
     * @throws InterruptedException 如果当前线程在等待时被中断
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * 使当前线程等待，直到计数器减至零，除非线程被 {@linkplain Thread#interrupt 中断}，
     * 或指定的等待时间已过。
     *
     * <p>如果当前计数为零，则此方法立即返回 {@code true}。
     *
     * <p>如果当前计数大于零，则当前线程将被禁用以进行线程调度，并处于休眠状态，直到以下三种情况之一发生：
     * <ul>
     * <li>由于调用了 {@link #countDown} 方法，计数达到零；或
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 了当前线程；或
     * <li>指定的等待时间已过。
     * </ul>
     *
     * <p>如果计数达到零，则方法返回 {@code true}。
     *
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时已设置中断状态；或
     * <li>在等待时被 {@linkplain Thread#interrupt 中断}，
     * </ul>
     * 则抛出 {@link InterruptedException}，并且当前线程的中断状态被清除。
     *
     * <p>如果指定的等待时间已过，则返回值为 {@code false}。如果时间小于或等于零，方法将不会等待。
     *
     * @param timeout 最大等待时间
     * @param unit {@code timeout} 参数的时间单位
     * @return 如果计数达到零则返回 {@code true}，如果等待时间已过但计数未达到零则返回 {@code false}
     * @throws InterruptedException 如果当前线程在等待时被中断
     */
    public boolean await(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * 减少锁存器的计数，如果计数达到零，则释放所有等待的线程。
     *
     * <p>如果当前计数大于零，则计数递减。如果新的计数为零，则所有等待的线程将被重新启用以进行线程调度。
     *
     * <p>如果当前计数为零，则不发生任何操作。
     */
    public void countDown() {
        sync.releaseShared(1);
    }

    /**
     * 返回当前计数。
     *
     * <p>此方法通常用于调试和测试目的。
     *
     * @return 当前计数
     */
    public long getCount() {
        return sync.getCount();
    }

    /**
     * 返回一个标识此锁存器及其状态的字符串。
     * 状态，用括号表示，包括字符串 {@code "Count ="} 后跟当前计数。
     *
     * @return 一个标识此锁存器及其状态的字符串
     */
    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }
}
