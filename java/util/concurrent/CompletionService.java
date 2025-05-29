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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据以下网址的解释发布到公共领域：
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

/**
 * 一种服务，将新异步任务的生成与已完成任务结果的消费解耦。生产者 {@code submit} 任务以执行。消费者 {@code take} 已完成的任务并按完成顺序处理其结果。例如，可以使用 {@code CompletionService} 来管理异步 I/O，其中执行读取的的任务在一个程序或系统的某部分提交，然后在程序的另一部分在读取完成后进行处理，可能与请求的顺序不同。
 *
 * <p>通常，{@code CompletionService} 依赖于单独的 {@link Executor} 实际执行任务，在这种情况下，{@code CompletionService} 仅管理内部完成队列。{@link ExecutorCompletionService} 类提供了这种方法的实现。
 *
 * <p>内存一致性效果：在将任务提交给 {@code CompletionService} 之前线程中的操作 <a href="package-summary.html#MemoryVisibility"><i>先于</i></a> 该任务执行的操作，而后者又 <i>先于</i> 从相应的 {@code take()} 成功返回后的操作。
 */
public interface CompletionService<V> {
    /**
     * 提交一个返回值的任务以执行，并返回一个表示任务待处理结果的 Future。任务完成后，可以获取或轮询此任务。
     *
     * @param task 要提交的任务
     * @return 一个表示任务待处理完成的 Future
     * @throws RejectedExecutionException 如果任务无法安排执行
     * @throws NullPointerException 如果任务为 null
     */
    Future<V> submit(Callable<V> task);

    /**
     * 提交一个 Runnable 任务以执行，并返回一个表示该任务的 Future。任务完成后，可以获取或轮询此任务。
     *
     * @param task 要提交的任务
     * @param result 成功完成时返回的结果
     * @return 一个表示任务待处理完成的 Future，其 {@code get()} 方法在完成时将返回给定的结果值
     * @throws RejectedExecutionException 如果任务无法安排执行
     * @throws NullPointerException 如果任务为 null
     */
    Future<V> submit(Runnable task, V result);

    /**
     * 检索并移除表示下一个已完成任务的 Future，如果没有任务完成则等待。
     *
     * @return 表示下一个已完成任务的 Future
     * @throws InterruptedException 如果在等待时被中断
     */
    Future<V> take() throws InterruptedException;

    /**
     * 检索并移除表示下一个已完成任务的 Future，如果没有任务完成则返回 {@code null}。
     *
     * @return 表示下一个已完成任务的 Future，如果没有任务完成则返回 {@code null}
     */
    Future<V> poll();

    /**
     * 检索并移除表示下一个已完成任务的 Future，如果没有任务完成则必要时等待指定的等待时间。
     *
     * @param timeout 在放弃前等待的时间，以 {@code unit} 为单位
     * @param unit 一个 {@code TimeUnit}，用于解释 {@code timeout} 参数
     * @return 表示下一个已完成任务的 Future，如果指定的等待时间过去后没有任务完成则返回 {@code null}
     * @throws InterruptedException 如果在等待时被中断
     */
    Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException;
}