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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的公共领域发布。
 */

package java.util.concurrent;

/**
 * 一个 {@code Future} 表示异步计算的结果。提供了检查计算是否完成、
 * 等待其完成以及检索计算结果的方法。只有当计算完成时，才能使用方法
 * {@code get} 检索结果，必要时会阻塞直到结果可用。取消操作由
 * {@code cancel} 方法执行。还提供了其他方法来确定任务是正常完成还是被取消。
 * 一旦计算完成，计算就不能被取消。如果希望为了可取消性而使用 {@code Future}，
 * 但不提供可用的结果，可以声明形式为 {@code Future<?>} 的类型，并
 * 将底层任务的结果返回为 {@code null}。
 *
 * <p>
 * <b>示例用法</b>（注意以下类都是虚构的。）
 * <pre> {@code
 * interface ArchiveSearcher { String search(String target); }
 * class App {
 *   ExecutorService executor = ...
 *   ArchiveSearcher searcher = ...
 *   void showSearch(final String target)
 *       throws InterruptedException {
 *     Future<String> future
 *       = executor.submit(new Callable<String>() {
 *         public String call() {
 *             return searcher.search(target);
 *         }});
 *     displayOtherThings(); // 在搜索时执行其他操作
 *     try {
 *       displayText(future.get()); // 使用 future
 *     } catch (ExecutionException ex) { cleanup(); return; }
 *   }
 * }}</pre>
 *
 * {@link FutureTask} 类是 {@code Future} 的一个实现，它实现了 {@code Runnable}，
 * 因此可以由 {@code Executor} 执行。例如，上述使用 {@code submit} 的构造可以替换为：
 *  <pre> {@code
 * FutureTask<String> future =
 *   new FutureTask<String>(new Callable<String>() {
 *     public String call() {
 *       return searcher.search(target);
 *   }});
 * executor.execute(future);}</pre>
 *
 * <p>内存一致性效果：异步计算采取的动作
 * <a href="package-summary.html#MemoryVisibility"> <i>先于</i></a>
 * 另一线程中相应的 {@code Future.get()} 之后的动作。
 *
 * @see FutureTask
 * @see Executor
 * @since 1.5
 * @author Doug Lea
 * @param <V> 此 Future 的 {@code get} 方法返回的结果类型
 */
public interface Future<V> {

    /**
     * 尝试取消此任务的执行。如果任务已经完成、已经被取消，或者由于其他原因无法取消，
     * 则此尝试将失败。如果成功，并且在调用 {@code cancel} 时此任务尚未开始，
     * 则此任务不应运行。如果任务已经开始，则 {@code mayInterruptIfRunning} 参数确定
     * 是否应中断执行此任务的线程以尝试停止任务。
     *
     * <p>此方法返回后，后续对 {@link #isDone} 的调用将始终返回 {@code true}。
     * 如果此方法返回 {@code true}，则后续对 {@link #isCancelled} 的调用将始终返回 {@code true}。
     *
     * @param mayInterruptIfRunning 如果应中断执行此任务的线程，则为 {@code true}；
     * 否则，允许进行中的任务完成
     * @return 如果任务无法取消，通常是因为它已经正常完成，则返回 {@code false}；
     * 否则返回 {@code true}
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * 如果此任务在正常完成前被取消，则返回 {@code true}。
     *
     * @return 如果此任务在正常完成前被取消，则返回 {@code true}
     */
    boolean isCancelled();

    /**
     * 如果此任务已完成，则返回 {@code true}。
     *
     * 完成可能是由于正常终止、异常或取消——在所有这些情况下，此方法将返回
     * {@code true}。
     *
     * @return 如果此任务已完成，则返回 {@code true}
     */
    boolean isDone();

    /**
     * 必要时等待计算完成，然后检索其结果。
     *
     * @return 计算结果
     * @throws CancellationException 如果计算被取消
     * @throws ExecutionException 如果计算抛出异常
     * @throws InterruptedException 如果当前线程在等待时被中断
     */
    V get() throws InterruptedException, ExecutionException;

    /**
     * 必要时最多等待给定时间以完成计算，然后如果可用则检索其结果。
     *
     * @param timeout 最大等待时间
     * @param unit 超时参数的时间单位
     * @return 计算结果
     * @throws CancellationException 如果计算被取消
     * @throws ExecutionException 如果计算抛出异常
     * @throws InterruptedException 如果当前线程在等待时被中断
     * @throws TimeoutException 如果等待超时
     */
    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}