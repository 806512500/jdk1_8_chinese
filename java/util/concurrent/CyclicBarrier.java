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
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的方式发布到公共领域。
 */

package java.util.concurrent;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 一个同步辅助工具，允许一组线程互相等待，直到它们都到达一个共同的屏障点。循环屏障在涉及一组固定线程的程序中非常有用，这些线程必须偶尔互相等待。该屏障被称为
 * <em>循环</em>，因为它在等待的线程被释放后可以重新使用。
 *
 * <p>{@code CyclicBarrier} 支持一个可选的 {@link Runnable} 命令，
 * 该命令在每个屏障点执行一次，在最后一个线程到达后，但在任何线程被释放之前执行。
 * 这个 <em>屏障动作</em> 用于在任何一方继续之前更新共享状态。
 *
 * <p><b>示例用法：</b> 以下是一个在并行分解设计中使用屏障的示例：
 *
 *  <pre> {@code
 * class Solver {
 *   final int N;
 *   final float[][] data;
 *   final CyclicBarrier barrier;
 *
 *   class Worker implements Runnable {
 *     int myRow;
 *     Worker(int row) { myRow = row; }
 *     public void run() {
 *       while (!done()) {
 *         processRow(myRow);
 *
 *         try {
 *           barrier.await();
 *         } catch (InterruptedException ex) {
 *           return;
 *         } catch (BrokenBarrierException ex) {
 *           return;
 *         }
 *       }
 *     }
 *   }
 *
 *   public Solver(float[][] matrix) {
 *     data = matrix;
 *     N = matrix.length;
 *     Runnable barrierAction =
 *       new Runnable() { public void run() { mergeRows(...); }};
 *     barrier = new CyclicBarrier(N, barrierAction);
 *
 *     List<Thread> threads = new ArrayList<Thread>(N);
 *     for (int i = 0; i < N; i++) {
 *       Thread thread = new Thread(new Worker(i));
 *       threads.add(thread);
 *       thread.start();
 *     }
 *
 *     // 等待完成
 *     for (Thread thread : threads)
 *       thread.join();
 *   }
 * }}</pre>
 *
 * 在这里，每个工作线程处理矩阵的一行，然后在所有行都被处理之前在屏障处等待。当所有行都被处理后，
 * 提供的 {@link Runnable} 屏障动作将执行并合并行。如果合并确定找到了解决方案，则 {@code done()} 将返回
 * {@code true}，每个工作线程将终止。
 *
 * <p>如果屏障动作的执行不依赖于各线程被挂起，则当线程被释放时，任何参与的线程都可以执行该动作。为此，每次调用
 * {@link #await} 都会返回该线程到达屏障的索引。然后可以选择哪个线程执行屏障动作，例如：
 *  <pre> {@code
 * if (barrier.await() == 0) {
 *   // 记录本次迭代的完成
 * }}</pre>
 *
 * <p>{@code CyclicBarrier} 使用一种失败同步尝试的全有或全无的中断模型：如果一个线程因为中断、失败或超时而提前离开屏障点，所有在该屏障点等待的其他线程也将通过 {@link BrokenBarrierException}（或
 * {@link InterruptedException} 如果它们也在大约同一时间被中断）异常方式离开。
 *
 * <p>内存一致性效果：线程在调用 {@code await()}
 * 之前的操作 <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 屏障动作中的操作，后者又 <i>先于</i> 其他线程从相应的 {@code await()} 成功返回后的操作。
 *
 * @since 1.5
 * @see CountDownLatch
 *
 * @author Doug Lea
 */
public class CyclicBarrier {
    /**
     * 每次使用屏障都表示为一个生成实例。
     * 生成实例在屏障被触发或重置时更改。由于锁可能以非确定性方式分配给等待的线程，
     * 可能会有多个与使用屏障的线程相关的生成实例，但其中只有一个可以处于活动状态（即 {@code count} 适用的那个）
     * 其余的要么已中断，要么已触发。
     * 如果已中断但没有后续重置，则可能没有活动的生成实例。
     */
    private static class Generation {
        boolean broken = false;
    }

    /** 用于保护屏障入口的锁 */
    private final ReentrantLock lock = new ReentrantLock();
    /** 等待直到触发的条件 */
    private final Condition trip = lock.newCondition();
    /** 参与者的数量 */
    private final int parties;
    /* 触发时执行的命令 */
    private final Runnable barrierCommand;
    /** 当前的生成实例 */
    private Generation generation = new Generation();

    /**
     * 仍在等待的参与者数量。在每个生成实例中从 parties 计数到 0。
     * 在每个新的生成实例或中断时重置为 parties。
     */
    private int count;

    /**
     * 在屏障触发时更新状态并唤醒所有人。
     * 仅在持有锁时调用。
     */
    private void nextGeneration() {
        // 信号完成最后一个生成实例
        trip.signalAll();
        // 设置下一个生成实例
        count = parties;
        generation = new Generation();
    }

    /**
     * 将当前屏障生成实例设置为中断并唤醒所有人。
     * 仅在持有锁时调用。
     */
    private void breakBarrier() {
        generation.broken = true;
        count = parties;
        trip.signalAll();
    }

    /**
     * 主屏障代码，涵盖各种策略。
     */
    private int dowait(boolean timed, long nanos)
        throws InterruptedException, BrokenBarrierException,
               TimeoutException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final Generation g = generation;


            if (g.broken)
                throw new BrokenBarrierException();

            if (Thread.interrupted()) {
                breakBarrier();
                throw new InterruptedException();
            }

            int index = --count;
            if (index == 0) {  // tripped
                boolean ranAction = false;
                try {
                    final Runnable command = barrierCommand;
                    if (command != null)
                        command.run();
                    ranAction = true;
                    nextGeneration();
                    return 0;
                } finally {
                    if (!ranAction)
                        breakBarrier();
                }
            }

            // 循环直到触发、中断、超时或损坏
            for (;;) {
                try {
                    if (!timed)
                        trip.await();
                    else if (nanos > 0L)
                        nanos = trip.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    if (g == generation && ! g.broken) {
                        breakBarrier();
                        throw ie;
                    } else {
                        // 我们即将完成等待，即使没有被中断，因此这个中断被认为属于后续执行。
                        Thread.currentThread().interrupt();
                    }
                }

                if (g.broken)
                    throw new BrokenBarrierException();

                if (g != generation)
                    return index;

                if (timed && nanos <= 0L) {
                    breakBarrier();
                    throw new TimeoutException();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 创建一个新的 {@code CyclicBarrier}，当给定数量的线程等待时触发，并在触发时执行给定的屏障操作，由最后一个进入屏障的线程执行。
     *
     * @param parties 必须调用 {@link #await} 以触发屏障的线程数量
     * @param barrierAction 当屏障触发时执行的命令，如果无操作则为 {@code null}
     * @throws IllegalArgumentException 如果 {@code parties} 小于 1
     */
    public CyclicBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException();
        this.parties = parties;
        this.count = parties;
        this.barrierCommand = barrierAction;
    }

    /**
     * 创建一个新的 {@code CyclicBarrier}，当给定数量的线程等待时触发，不执行预定义的操作。
     *
     * @param parties 必须调用 {@link #await} 以触发屏障的线程数量
     * @throws IllegalArgumentException 如果 {@code parties} 小于 1
     */
    public CyclicBarrier(int parties) {
        this(parties, null);
    }

    /**
     * 返回触发此屏障所需的线程数量。
     *
     * @return 触发此屏障所需的线程数量
     */
    public int getParties() {
        return parties;
    }

    /**
     * 等待所有 {@linkplain #getParties 线程} 调用此屏障的 {@code await}。
     *
     * <p>如果当前线程不是最后一个到达的线程，则它将被禁用以进行线程调度，并处于休眠状态，直到以下情况之一发生：
     * <ul>
     * <li>最后一个线程到达；或
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 当前线程；或
     * <li>其他线程 {@linkplain Thread#interrupt 中断} 其他等待线程之一；或
     * <li>其他线程在等待屏障时超时；或
     * <li>其他线程调用此屏障的 {@link #reset}。
     * </ul>
     *
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时其中断状态已设置；或
     * <li>在等待时被 {@linkplain Thread#interrupt 中断}
     * </ul>
     * 则抛出 {@link InterruptedException} 并清除当前线程的中断状态。
     *
     * <p>如果在任何线程等待时屏障被 {@link #reset 重置}，或者在调用 {@code await} 时屏障 {@linkplain #isBroken 已损坏}，或者在任何线程等待时屏障已损坏，则抛出 {@link BrokenBarrierException}。
     *
     * <p>如果在等待时任何线程被 {@linkplain Thread#interrupt 中断}，则所有其他等待线程将抛出 {@link BrokenBarrierException}，并且屏障将进入损坏状态。
     *
     * <p>如果当前线程是最后一个到达的线程，并且在构造函数中提供了非空的屏障操作，则当前线程将在允许其他线程继续之前运行该操作。
     * 如果在屏障操作中发生异常，则该异常将在当前线程中传播，并且屏障将进入损坏状态。
     *
     * @return 当前线程的到达索引，其中索引 {@code getParties() - 1} 表示第一个到达的线程，0 表示最后一个到达的线程
     * @throws InterruptedException 如果当前线程在等待时被中断
     * @throws BrokenBarrierException 如果 <em>其他</em> 线程在当前线程等待时被中断或超时，或者屏障被重置，或者在调用 {@code await} 时屏障已损坏，或者屏障操作（如果存在）由于异常而失败
     */
    public int await() throws InterruptedException, BrokenBarrierException {
        try {
            return dowait(false, 0L);
        } catch (TimeoutException toe) {
            throw new Error(toe); // 不可能发生
        }
    }


    /**
     * 等待所有 {@linkplain #getParties 参与者} 调用此屏障上的
     * {@code await} 方法，或者指定的等待时间到期。
     *
     * <p>如果当前线程不是最后一个到达的线程，则它将被禁用以进行线程调度，并处于休眠状态，直到以下情况之一发生：
     * <ul>
     * <li>最后一个线程到达；或者
     * <li>指定的超时时间到期；或者
     * <li>其他某个线程 {@linkplain Thread#interrupt 中断} 当前线程；或者
     * <li>其他某个线程 {@linkplain Thread#interrupt 中断} 其他等待线程之一；或者
     * <li>其他某个线程在等待屏障时超时；或者
     * <li>其他某个线程调用 {@link #reset} 重置此屏障。
     * </ul>
     *
     * <p>如果当前线程：
     * <ul>
     * <li>在进入此方法时其中断状态已设置；或者
     * <li>在等待时被 {@linkplain Thread#interrupt 中断}
     * </ul>
     * 则抛出 {@link InterruptedException}，并且清除当前线程的中断状态。
     *
     * <p>如果指定的等待时间到期，则抛出 {@link TimeoutException}。如果时间小于或等于零，该方法将不会等待。
     *
     * <p>如果在任何线程等待时重置了屏障，或者在调用 {@code await} 时屏障已 {@linkplain #isBroken 损坏}，
     * 或者在任何线程等待时屏障损坏，则抛出 {@link BrokenBarrierException}。
     *
     * <p>如果在等待时任何线程被 {@linkplain Thread#interrupt 中断}，则所有其他等待线程将抛出 {@link
     * BrokenBarrierException}，并且屏障将处于损坏状态。
     *
     * <p>如果当前线程是最后一个到达的线程，并且在构造函数中提供了非空的屏障动作，则当前线程将在允许其他线程继续之前运行该动作。
     * 如果在屏障动作中发生异常，则该异常将在当前线程中传播，并且屏障将处于损坏状态。
     *
     * @param timeout 等待屏障的时间
     * @param unit 超时参数的时间单位
     * @return 当前线程的到达索引，其中索引
     *         {@code getParties() - 1} 表示第一个到达的线程，0 表示最后一个到达的线程
     * @throws InterruptedException 如果当前线程在等待时被中断
     * @throws TimeoutException 如果指定的超时时间到期。在这种情况下，屏障将被损坏。
     * @throws BrokenBarrierException 如果 <em>其他</em> 线程在当前线程等待时被中断或超时，
     *         或者屏障被重置，或者在调用 {@code await} 时屏障已损坏，或者屏障动作（如果存在）由于异常而失败
     */
    public int await(long timeout, TimeUnit unit)
        throws InterruptedException,
               BrokenBarrierException,
               TimeoutException {
        return dowait(true, unit.toNanos(timeout));
    }

    /**
     * 查询此屏障是否处于损坏状态。
     *
     * @return 如果一个或多个参与者由于中断或超时而从该屏障中跳出，或者由于异常导致屏障动作失败，
     *         则返回 {@code true}；否则返回 {@code false}。
     */
    public boolean isBroken() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return generation.broken;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将屏障重置为其初始状态。如果任何参与者当前正在屏障处等待，它们将返回带有
     * {@link BrokenBarrierException}。注意，如果由于其他原因已经发生损坏，重置可能会很复杂；
     * 线程需要以某种方式重新同步，并选择一个线程来执行重置。创建一个新的屏障以供后续使用可能更可取。
     */
    public void reset() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            breakBarrier();   // 打破当前代
            nextGeneration(); // 开始新一代
        } finally {
            lock.unlock();
        }
    }

    /**
     * 返回当前在屏障处等待的参与者数量。此方法主要用于调试和断言。
     *
     * @return 当前在 {@link #await} 中阻塞的参与者数量
     */
    public int getNumberWaiting() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return parties - count;
        } finally {
            lock.unlock();
        }
    }
}
