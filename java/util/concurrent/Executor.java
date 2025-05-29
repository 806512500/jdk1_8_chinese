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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的那样。
 */

package java.util.concurrent;

/**
 * 执行提交的 {@link Runnable} 任务的对象。此接口提供了一种将任务提交与每个任务如何运行的机制（包括线程使用、调度等细节）解耦的方法。通常使用 {@code Executor} 而不是显式创建线程。例如，对于一组任务，与其调用 {@code new Thread(new(RunnableTask())).start()}，不如使用：
 *
 * <pre>
 * Executor executor = <em>anExecutor</em>;
 * executor.execute(new RunnableTask1());
 * executor.execute(new RunnableTask2());
 * ...
 * </pre>
 *
 * 然而，{@code Executor} 接口并不要求执行必须是异步的。在最简单的情况下，执行器可以在调用者的线程中立即运行提交的任务：
 *
 *  <pre> {@code
 * class DirectExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     r.run();
 *   }
 * }}</pre>
 *
 * 更典型的情况是，任务在调用者线程之外的某个线程中执行。下面的执行器为每个任务生成一个新线程。
 *
 *  <pre> {@code
 * class ThreadPerTaskExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     new Thread(r).start();
 *   }
 * }}</pre>
 *
 * 许多 {@code Executor} 实现对任务的调度方式和时间施加某种限制。下面的执行器将任务的提交序列化到第二个执行器，说明了一个复合执行器。
 *
 *  <pre> {@code
 * class SerialExecutor implements Executor {
 *   final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
 *   final Executor executor;
 *   Runnable active;
 *
 *   SerialExecutor(Executor executor) {
 *     this.executor = executor;
 *   }
 *
 *   public synchronized void execute(final Runnable r) {
 *     tasks.offer(new Runnable() {
 *       public void run() {
 *         try {
 *           r.run();
 *         } finally {
 *           scheduleNext();
 *         }
 *       }
 *     });
 *     if (active == null) {
 *       scheduleNext();
 *     }
 *   }
 *
 *   protected synchronized void scheduleNext() {
 *     if ((active = tasks.poll()) != null) {
 *       executor.execute(active);
 *     }
 *   }
 * }}</pre>
 *
 * 本包提供的 {@code Executor} 实现实现了 {@link ExecutorService}，这是一个更广泛的接口。{@link ThreadPoolExecutor} 类提供了可扩展的线程池实现。{@link Executors} 类提供了这些执行器的便捷工厂方法。
 *
 * <p>内存一致性效果：在将 {@code Runnable} 对象提交给 {@code Executor} 之前线程中的操作 <a href="package-summary.html#MemoryVisibility"><i>先于</i></a> 其执行开始，可能是在另一个线程中。
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface Executor {

    /**
     * 在未来的某个时间执行给定的命令。命令可能在新线程、线程池中的线程或调用线程中执行，具体由 {@code Executor} 实现决定。
     *
     * @param command 可运行的任务
     * @throws RejectedExecutionException 如果此任务不能被接受执行
     * @throws NullPointerException 如果命令为 null
     */
    void execute(Runnable command);
}