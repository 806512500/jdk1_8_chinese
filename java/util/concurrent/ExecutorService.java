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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的那样发布到公共领域。
 */

package java.util.concurrent;
import java.util.List;
import java.util.Collection;

/**
 * 一个提供终止管理和可以生成 {@link Future} 以跟踪一个或多个异步任务进度的方法的 {@link Executor}。
 *
 * <p>一个 {@code ExecutorService} 可以被关闭，这将导致它拒绝新的任务。为关闭 {@code ExecutorService} 提供了两种不同的方法。{@link #shutdown}
 * 方法将允许之前提交的任务执行完毕后终止，而 {@link #shutdownNow} 方法会阻止等待中的任务开始执行，并尝试停止当前正在执行的任务。
 * 终止后，执行器没有正在执行的任务，没有等待执行的任务，且不能提交新的任务。一个未使用的 {@code ExecutorService} 应该被关闭以允许其资源的回收。
 *
 * <p>方法 {@code submit} 扩展了基础方法 {@link
 * Executor#execute(Runnable)}，通过创建并返回一个可以用于取消执行和/或等待完成的 {@link Future}。
 * 方法 {@code invokeAny} 和 {@code invokeAll} 执行最常用的批量执行形式，执行任务集合并等待至少一个或全部任务完成。
 * （可以使用 {@link ExecutorCompletionService} 类编写这些方法的自定义变体。）
 *
 * <p>{@link Executors} 类提供了此包中提供的执行器服务的工厂方法。
 *
 * <h3>使用示例</h3>
 *
 * 以下是一个网络服务的草图，在该服务中，线程池中的线程处理传入的请求。它使用预配置的 {@link
 * Executors#newFixedThreadPool} 工厂方法：
 *
 *  <pre> {@code
 * class NetworkService implements Runnable {
 *   private final ServerSocket serverSocket;
 *   private final ExecutorService pool;
 *
 *   public NetworkService(int port, int poolSize)
 *       throws IOException {
 *     serverSocket = new ServerSocket(port);
 *     pool = Executors.newFixedThreadPool(poolSize);
 *   }
 *
 *   public void run() { // 运行服务
 *     try {
 *       for (;;) {
 *         pool.execute(new Handler(serverSocket.accept()));
 *       }
 *     } catch (IOException ex) {
 *       pool.shutdown();
 *     }
 *   }
 * }
 *
 * class Handler implements Runnable {
 *   private final Socket socket;
 *   Handler(Socket socket) { this.socket = socket; }
 *   public void run() {
 *     // 读取并处理 socket 上的请求
 *   }
 * }}</pre>
 *
 * 以下方法分两个阶段关闭一个 {@code ExecutorService}，首先调用 {@code shutdown} 拒绝新的任务，然后如果必要，调用 {@code shutdownNow} 取消任何剩余的任务：
 *
 *  <pre> {@code
 * void shutdownAndAwaitTermination(ExecutorService pool) {
 *   pool.shutdown(); // 禁止提交新任务
 *   try {
 *     // 等待现有任务终止
 *     if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
 *       pool.shutdownNow(); // 取消当前正在执行的任务
 *       // 等待任务响应取消
 *       if (!pool.awaitTermination(60, TimeUnit.SECONDS))
 *           System.err.println("池未终止");
 *     }
 *   } catch (InterruptedException ie) {
 *     // 如果当前线程也被中断，则重新取消
 *     pool.shutdownNow();
 *     // 保留中断状态
 *     Thread.currentThread().interrupt();
 *   }
 * }}</pre>
 *
 * <p>内存一致性效果：线程在将 {@code Runnable} 或 {@code Callable} 任务提交给 {@code ExecutorService} 之前的操作
 * <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 该任务执行的任何操作，而这些操作又 <i>先于</i> 通过 {@code Future.get()} 检索结果的操作。
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface ExecutorService extends Executor {

    /**
     * 开始有序关闭，其中先前提交的任务将被执行，但不会接受新的任务。
     * 如果已经关闭，则调用此方法不会产生额外的效果。
     *
     * <p>此方法不会等待先前提交的任务完成执行。使用 {@link #awaitTermination awaitTermination}
     * 来实现这一点。
     *
     * @throws SecurityException 如果存在安全经理，并且关闭此 ExecutorService 可能会操作
     *         调用者无权修改的线程，因为调用者没有持有 {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")}，
     *         或安全经理的 {@code checkAccess} 方法拒绝访问。
     */
    void shutdown();

    /**
     * 尝试停止所有正在积极执行的任务，停止等待中的任务处理，并返回一个从未开始执行的任务列表。
     *
     * <p>此方法不会等待正在积极执行的任务终止。使用 {@link #awaitTermination awaitTermination}
     * 来实现这一点。
     *
     * <p>除了尽力尝试停止正在积极执行的任务外，没有其他保证。例如，典型的实现将通过 {@link Thread#interrupt} 取消任务，
     * 因此任何不响应中断的任务可能永远不会终止。
     *
     * @return 从未开始执行的任务列表
     * @throws SecurityException 如果存在安全经理，并且关闭此 ExecutorService 可能会操作
     *         调用者无权修改的线程，因为调用者没有持有 {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")}，
     *         或安全经理的 {@code checkAccess} 方法拒绝访问。
     */
    List<Runnable> shutdownNow();


    /**
     * 如果此执行器已关闭，则返回 {@code true}。
     *
     * @return 如果此执行器已关闭，则返回 {@code true}
     */
    boolean isShutdown();

    /**
     * 如果所有任务在关闭后已完成，则返回 {@code true}。
     * 注意，除非首先调用了 {@code shutdown} 或 {@code shutdownNow}，否则 {@code isTerminated} 永远不会为 {@code true}。
     *
     * @return 如果所有任务在关闭后已完成，则返回 {@code true}
     */
    boolean isTerminated();

    /**
     * 阻塞直到所有任务在关闭请求后完成执行，或者超时发生，或者当前线程被中断，以最先发生的情况为准。
     *
     * @param timeout 要等待的最大时间
     * @param unit 超时参数的时间单位
     * @return 如果此执行器已终止，则返回 {@code true}；如果在终止前超时，则返回 {@code false}
     * @throws InterruptedException 如果在等待过程中被中断
     */
    boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 提交一个返回值的任务以执行，并返回一个表示任务待处理结果的 Future。Future 的 {@code get} 方法将在任务成功完成后返回任务的结果。
     *
     * <p>
     * 如果您希望立即阻塞等待任务完成，可以使用类似 {@code result = exec.submit(aCallable).get();} 的构造。
     *
     * <p>注意：{@link Executors} 类包括一组方法，可以将一些其他常见的闭包类对象（例如，{@link java.security.PrivilegedAction}）转换为 {@link Callable} 形式，以便提交。
     *
     * @param task 要提交的任务
     * @param <T> 任务结果的类型
     * @return 一个表示任务待处理完成的 Future
     * @throws RejectedExecutionException 如果任务无法被调度执行
     * @throws NullPointerException 如果任务为 null
     */
    <T> Future<T> submit(Callable<T> task);

    /**
     * 提交一个 Runnable 任务以执行，并返回一个表示该任务的 Future。Future 的 {@code get} 方法将在任务成功完成后返回给定的结果。
     *
     * @param task 要提交的任务
     * @param result 要返回的结果
     * @param <T> 结果的类型
     * @return 一个表示任务待处理完成的 Future
     * @throws RejectedExecutionException 如果任务无法被调度执行
     * @throws NullPointerException 如果任务为 null
     */
    <T> Future<T> submit(Runnable task, T result);

    /**
     * 提交一个 Runnable 任务以执行，并返回一个表示该任务的 Future。Future 的 {@code get} 方法将在任务成功完成后返回 {@code null}。
     *
     * @param task 要提交的任务
     * @return 一个表示任务待处理完成的 Future
     * @throws RejectedExecutionException 如果任务无法被调度执行
     * @throws NullPointerException 如果任务为 null
     */
    Future<?> submit(Runnable task);

    /**
     * 执行给定的任务，返回一个包含它们状态和结果的 Future 列表，当所有任务完成时。
     * 返回列表中的每个元素的 {@link Future#isDone} 都为 {@code true}。
     * 注意，一个 <em>已完成</em> 的任务可能正常终止或因抛出异常而终止。
     * 如果在操作进行过程中修改了给定的集合，则此方法的结果是未定义的。
     *
     * @param tasks 任务集合
     * @param <T> 任务返回值的类型
     * @return 一个表示任务的 Future 列表，顺序与给定任务列表的迭代器生成的顺序相同，每个任务都已完成
     * @throws InterruptedException 如果在等待过程中被中断，未完成的任务将被取消
     * @throws NullPointerException 如果任务或其任何元素为 {@code null}
     * @throws RejectedExecutionException 如果任何任务无法被调度执行
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException;

    /**
     * 执行给定的任务，返回一个包含它们状态和结果的 Future 列表，当所有任务完成或超时到期时，以最先发生的情况为准。
     * 返回列表中的每个元素的 {@link Future#isDone} 都为 {@code true}。
     * 返回时，未完成的任务将被取消。
     * 注意，一个 <em>已完成</em> 的任务可能正常终止或因抛出异常而终止。
     * 如果在操作进行过程中修改了给定的集合，则此方法的结果是未定义的。
     *
     * @param tasks 任务集合
     * @param timeout 要等待的最大时间
     * @param unit 超时参数的时间单位
     * @param <T> 任务返回值的类型
     * @return 一个表示任务的 Future 列表，顺序与给定任务列表的迭代器生成的顺序相同。如果操作未超时，每个任务都将完成。如果超时，一些任务可能未完成。
     * @throws InterruptedException 如果在等待过程中被中断，未完成的任务将被取消
     * @throws NullPointerException 如果任务、其任何元素或单位为 {@code null}
     * @throws RejectedExecutionException 如果任何任务无法被调度执行
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                  long timeout, TimeUnit unit)
        throws InterruptedException;


    /**
     * 执行给定的任务，返回其中一个成功完成（即，没有抛出异常）的任务的结果，如果有这样的任务。无论是在正常返回还是异常返回时，
     * 未完成的任务将被取消。如果在操作进行过程中修改了给定的集合，此方法的结果是不确定的。
     *
     * @param tasks 任务的集合
     * @param <T> 任务返回值的类型
     * @return 由其中一个任务返回的结果
     * @throws InterruptedException 如果在等待时被中断
     * @throws NullPointerException 如果任务集合或任何待执行的任务元素为 {@code null}
     * @throws IllegalArgumentException 如果任务集合为空
     * @throws ExecutionException 如果没有任务成功完成
     * @throws RejectedExecutionException 如果任务无法被安排执行
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException;

    /**
     * 执行给定的任务，返回其中一个在给定超时时间内成功完成（即，没有抛出异常）的任务的结果，如果有这样的任务。无论是在正常返回还是异常返回时，
     * 未完成的任务将被取消。如果在操作进行过程中修改了给定的集合，此方法的结果是不确定的。
     *
     * @param tasks 任务的集合
     * @param timeout 最大等待时间
     * @param unit 超时参数的时间单位
     * @param <T> 任务返回值的类型
     * @return 由其中一个任务返回的结果
     * @throws InterruptedException 如果在等待时被中断
     * @throws NullPointerException 如果任务集合、时间单位或任何待执行的任务元素为 {@code null}
     * @throws TimeoutException 如果在任何任务成功完成之前给定的超时时间已过期
     * @throws ExecutionException 如果没有任务成功完成
     * @throws RejectedExecutionException 如果任务无法被安排执行
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                    long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
