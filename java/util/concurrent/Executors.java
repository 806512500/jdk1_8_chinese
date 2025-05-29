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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据以下网址解释的方式发布到公共领域：
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.security.AccessControlException;
import sun.security.util.SecurityConstants;

/**
 * 本包中定义的 {@link Executor}, {@link
 * ExecutorService}, {@link ScheduledExecutorService}, {@link
 * ThreadFactory}, 和 {@link Callable} 类的工厂和工具方法。此类支持以下几种方法：
 *
 * <ul>
 *   <li> 创建并返回一个具有常用配置设置的 {@link ExecutorService} 的方法。
 *   <li> 创建并返回一个具有常用配置设置的 {@link ScheduledExecutorService} 的方法。
 *   <li> 创建并返回一个“包装的”ExecutorService，通过使实现特定的方法不可访问来禁用重新配置的方法。
 *   <li> 创建并返回一个设置新创建线程为已知状态的 {@link ThreadFactory} 的方法。
 *   <li> 从其他闭包形式创建并返回一个 {@link Callable} 的方法，以便它们可以在需要 {@code Callable} 的执行方法中使用。
 * </ul>
 *
 * @since 1.5
 * @author Doug Lea
 */
public class Executors {

    /**
     * 创建一个线程池，该线程池重用固定数量的线程，这些线程从共享的无界队列中获取任务。在任何时候，最多
     * {@code nThreads} 个线程将处于活动状态处理任务。
     * 如果在所有线程都处于活动状态时提交了额外的任务，它们将在队列中等待，直到有线程可用。
     * 如果任何线程在执行过程中由于故障而终止，在关闭前，如果需要执行后续任务，将有一个新的线程取代它。
     * 线程池中的线程将一直存在，直到显式地调用 {@link ExecutorService#shutdown shutdown}。
     *
     * @param nThreads 线程池中的线程数
     * @return 新创建的线程池
     * @throws IllegalArgumentException 如果 {@code nThreads <= 0}
     */
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }

    /**
     * 创建一个线程池，该线程池维护足够的线程以支持给定的并行级别，并可能使用多个队列来减少争用。并行级别对应于
     * 处于活动状态或可用于任务处理的线程的最大数量。实际的线程数可能会根据需要动态增长和减少。工作窃取池不保证
     * 提交的任务的执行顺序。
     *
     * @param parallelism 目标并行级别
     * @return 新创建的线程池
     * @throws IllegalArgumentException 如果 {@code parallelism <= 0}
     * @since 1.8
     */
    public static ExecutorService newWorkStealingPool(int parallelism) {
        return new ForkJoinPool
            (parallelism,
             ForkJoinPool.defaultForkJoinWorkerThreadFactory,
             null, true);
    }

    /**
     * 创建一个工作窃取线程池，使用所有
     * {@link Runtime#availableProcessors 可用处理器}
     * 作为其目标并行级别。
     * @return 新创建的线程池
     * @see #newWorkStealingPool(int)
     * @since 1.8
     */
    public static ExecutorService newWorkStealingPool() {
        return new ForkJoinPool
            (Runtime.getRuntime().availableProcessors(),
             ForkJoinPool.defaultForkJoinWorkerThreadFactory,
             null, true);
    }

    /**
     * 创建一个线程池，该线程池重用固定数量的线程，这些线程从共享的无界队列中获取任务，使用提供的
     * ThreadFactory 在需要时创建新线程。在任何时候，最多
     * {@code nThreads} 个线程将处于活动状态处理任务。
     * 如果在所有线程都处于活动状态时提交了额外的任务，它们将在队列中等待，直到有线程可用。
     * 如果任何线程在执行过程中由于故障而终止，在关闭前，如果需要执行后续任务，将有一个新的线程取代它。
     * 线程池中的线程将一直存在，直到显式地调用 {@link ExecutorService#shutdown
     * shutdown}。
     *
     * @param nThreads 线程池中的线程数
     * @param threadFactory 创建新线程时使用的工厂
     * @return 新创建的线程池
     * @throws NullPointerException 如果 threadFactory 为 null
     * @throws IllegalArgumentException 如果 {@code nThreads <= 0}
     */
    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>(),
                                      threadFactory);
    }

    /**
     * 创建一个使用单个工作线程从无界队列中获取任务的 Executor。（但是请注意，如果此单个线程在执行过程中由于故障而终止，在关闭前，如果需要执行后续任务，将有一个新的线程取代它。）任务保证顺序执行，任何时候最多只有一个任务处于活动状态。与等效的
     * {@code newFixedThreadPool(1)} 不同，返回的执行器保证不会被重新配置为使用额外的线程。
     *
     * @return 新创建的单线程 Executor
     */
    public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }

    /**
     * 创建一个使用单个工作线程的Executor，该线程从一个无界队列中操作，并使用提供的ThreadFactory在需要时创建新线程。与等效的
     * {@code newFixedThreadPool(1, threadFactory)}不同，返回的执行器保证不会被重新配置为使用额外的线程。
     *
     * @param threadFactory 创建新线程时使用的工厂
     *
     * @return 新创建的单线程Executor
     * @throws NullPointerException 如果threadFactory为null
     */
    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>(),
                                    threadFactory));
    }

    /**
     * 创建一个线程池，根据需要创建新线程，但在有可用的先前构造的线程时会重用这些线程。这些线程池通常可以提高执行许多短生命周期异步任务的程序的性能。
     * 调用{@code execute}时，如果有可用的现有线程，将重用这些线程。如果没有现有的线程可用，将创建一个新线程并添加到池中。未使用超过六十秒的线程将被终止并从缓存中移除。
     * 因此，长时间闲置的池不会消耗任何资源。请注意，可以使用{@link ThreadPoolExecutor}构造函数创建具有类似属性但不同细节（例如超时参数）的池。
     *
     * @return 新创建的线程池
     */
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }

    /**
     * 创建一个线程池，根据需要创建新线程，但在有可用的先前构造的线程时会重用这些线程，并使用提供的ThreadFactory在需要时创建新线程。
     * @param threadFactory 创建新线程时使用的工厂
     * @return 新创建的线程池
     * @throws NullPointerException 如果threadFactory为null
     */
    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>(),
                                      threadFactory);
    }

    /**
     * 创建一个可以安排命令在给定延迟后运行或定期执行的单线程执行器。（但是请注意，如果此单线程在执行过程中因故障而终止，在关闭之前，如果需要执行后续任务，将会有新的线程取代它。）
     * 任务保证顺序执行，并且在任何给定时间最多只有一个任务处于活动状态。与等效的{@code newScheduledThreadPool(1)}不同，返回的执行器保证不会被重新配置为使用额外的线程。
     * @return 新创建的计划执行器
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return new DelegatedScheduledExecutorService
            (new ScheduledThreadPoolExecutor(1));
    }

    /**
     * 创建一个可以安排命令在给定延迟后运行或定期执行的单线程执行器。（但是请注意，如果此单线程在执行过程中因故障而终止，在关闭之前，如果需要执行后续任务，将会有新的线程取代它。）
     * 任务保证顺序执行，并且在任何给定时间最多只有一个任务处于活动状态。与等效的{@code newScheduledThreadPool(1, threadFactory)}不同，返回的执行器保证不会被重新配置为使用额外的线程。
     * @param threadFactory 创建新线程时使用的工厂
     * @return 新创建的计划执行器
     * @throws NullPointerException 如果threadFactory为null
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return new DelegatedScheduledExecutorService
            (new ScheduledThreadPoolExecutor(1, threadFactory));
    }

    /**
     * 创建一个可以安排命令在给定延迟后运行或定期执行的线程池。
     * @param corePoolSize 即使它们空闲也要保留在池中的线程数
     * @return 新创建的计划线程池
     * @throws IllegalArgumentException 如果 {@code corePoolSize < 0}
     */
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }

    /**
     * 创建一个可以安排命令在给定延迟后运行或定期执行的线程池。
     * @param corePoolSize 即使它们空闲也要保留在池中的线程数
     * @param threadFactory 执行器创建新线程时使用的工厂
     * @return 新创建的计划线程池
     * @throws IllegalArgumentException 如果 {@code corePoolSize < 0}
     * @throws NullPointerException 如果threadFactory为null
     */
    public static ScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, ThreadFactory threadFactory) {
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }

    /**
     * 返回一个对象，该对象将所有定义的{@link ExecutorService}方法委托给给定的执行器，但不委托可能通过类型转换访问的任何其他方法。这提供了一种安全地“冻结”配置并禁止调整给定具体实现的方法。
     * @param executor 底层实现
     * @return 一个{@code ExecutorService}实例
     * @throws NullPointerException 如果executor为null
     */
    public static ExecutorService unconfigurableExecutorService(ExecutorService executor) {
        if (executor == null)
            throw new NullPointerException();
        return new DelegatedExecutorService(executor);
    }


    /**
     * 返回一个对象，该对象将所有定义的 {@link
     * ScheduledExecutorService} 方法委托给给定的执行器，但不包括可能通过类型转换访问的其他方法。这提供了一种安全地“冻结”配置并禁止调整给定具体实现的方法。
     * @param executor 底层实现
     * @return 一个 {@code ScheduledExecutorService} 实例
     * @throws NullPointerException 如果执行器为 null
     */
    public static ScheduledExecutorService unconfigurableScheduledExecutorService(ScheduledExecutorService executor) {
        if (executor == null)
            throw new NullPointerException();
        return new DelegatedScheduledExecutorService(executor);
    }

    /**
     * 返回一个默认的线程工厂，用于创建新线程。
     * 该工厂创建的所有新线程都属于同一个 {@link ThreadGroup}。如果有 {@link
     * java.lang.SecurityManager}，则使用 {@link
     * System#getSecurityManager} 的组，否则使用调用此 {@code defaultThreadFactory} 方法的线程的组。每个新线程都被创建为非守护线程，优先级设置为
     * {@code Thread.NORM_PRIORITY} 和线程组允许的最大优先级中的较小值。新线程的名称可以通过 {@link Thread#getName} 访问，格式为
     * <em>pool-N-thread-M</em>，其中 <em>N</em> 是此工厂的序列号，<em>M</em> 是此工厂创建的线程的序列号。
     * @return 一个线程工厂
     */
    public static ThreadFactory defaultThreadFactory() {
        return new DefaultThreadFactory();
    }

    /**
     * 返回一个线程工厂，用于创建具有与当前线程相同权限的新线程。
     * 该工厂创建的线程具有与 {@link
     * Executors#defaultThreadFactory} 相同的设置，并且还设置新线程的 AccessControlContext 和 contextClassLoader 与调用此
     * {@code privilegedThreadFactory} 方法的线程相同。可以在 {@link AccessController#doPrivileged AccessController.doPrivileged}
     * 操作中创建一个新的 {@code privilegedThreadFactory}，将当前线程的访问控制上下文设置为在该操作中创建具有选定权限设置的线程。
     *
     * <p>注意，虽然在这些线程中运行的任务将具有与当前线程相同的访问控制和类加载器设置，但它们不必具有相同的 {@link
     * java.lang.ThreadLocal} 或 {@link
     * java.lang.InheritableThreadLocal} 值。如果需要，在 {@link ThreadPoolExecutor} 子类中可以使用
     * {@link ThreadPoolExecutor#beforeExecute(Thread, Runnable)} 在任何任务运行之前设置或重置特定的线程局部值。此外，如果需要初始化工作线程以具有与某些其他指定线程相同的 InheritableThreadLocal 设置，可以创建一个自定义的 ThreadFactory，在该线程中等待并处理创建其他线程的请求，这些线程将继承其值。
     *
     * @return 一个线程工厂
     * @throws AccessControlException 如果当前访问控制上下文没有同时获取和设置上下文类加载器的权限
     */
    public static ThreadFactory privilegedThreadFactory() {
        return new PrivilegedThreadFactory();
    }

    /**
     * 返回一个 {@link Callable} 对象，当调用时，运行给定的任务并返回给定的结果。这在将需要 {@code Callable} 的方法应用于没有结果的操作时非常有用。
     * @param task 要运行的任务
     * @param result 要返回的结果
     * @param <T> 结果的类型
     * @return 一个 callable 对象
     * @throws NullPointerException 如果任务为 null
     */
    public static <T> Callable<T> callable(Runnable task, T result) {
        if (task == null)
            throw new NullPointerException();
        return new RunnableAdapter<T>(task, result);
    }

    /**
     * 返回一个 {@link Callable} 对象，当调用时，运行给定的任务并返回 {@code null}。
     * @param task 要运行的任务
     * @return 一个 callable 对象
     * @throws NullPointerException 如果任务为 null
     */
    public static Callable<Object> callable(Runnable task) {
        if (task == null)
            throw new NullPointerException();
        return new RunnableAdapter<Object>(task, null);
    }

    /**
     * 返回一个 {@link Callable} 对象，当调用时，运行给定的特权操作并返回其结果。
     * @param action 要运行的特权操作
     * @return 一个 callable 对象
     * @throws NullPointerException 如果操作为 null
     */
    public static Callable<Object> callable(final PrivilegedAction<?> action) {
        if (action == null)
            throw new NullPointerException();
        return new Callable<Object>() {
            public Object call() { return action.run(); }};
    }

    /**
     * 返回一个 {@link Callable} 对象，当调用时，运行给定的特权异常操作并返回其结果。
     * @param action 要运行的特权异常操作
     * @return 一个 callable 对象
     * @throws NullPointerException 如果操作为 null
     */
    public static Callable<Object> callable(final PrivilegedExceptionAction<?> action) {
        if (action == null)
            throw new NullPointerException();
        return new Callable<Object>() {
            public Object call() throws Exception { return action.run(); }};
    }

    /**
     * 返回一个 {@link Callable} 对象，当调用时，将在当前访问控制上下文中执行给定的 {@code callable}。此方法通常应在 {@link AccessController#doPrivileged AccessController.doPrivileged}
     * 操作中调用，以创建可能在该操作中持有的选定权限设置下执行的 callable；如果不可能，则抛出相关的 {@link
     * AccessControlException}。
     * @param callable 底层任务
     * @param <T> callable 结果的类型
     * @return 一个 callable 对象
     * @throws NullPointerException 如果 callable 为 null
     */
    public static <T> Callable<T> privilegedCallable(Callable<T> callable) {
        if (callable == null)
            throw new NullPointerException();
        return new PrivilegedCallable<T>(callable);
    }


    /**
     * 返回一个 {@link Callable} 对象，当被调用时，
     * 该对象将在当前访问控制上下文中执行给定的 {@code callable}，
     * 并将当前上下文类加载器作为上下文类加载器。此方法通常应在
     * {@link AccessController#doPrivileged AccessController.doPrivileged}
     * 操作中调用，以创建尽可能在该操作中选定的权限设置下执行的可调用对象；
     * 如果不可能，则抛出相关的 {@link AccessControlException}。
     *
     * @param callable 基础任务
     * @param <T> 可调用结果的类型
     * @return 一个可调用对象
     * @throws NullPointerException 如果 callable 为 null
     * @throws AccessControlException 如果当前访问控制上下文没有权限同时设置和获取上下文类加载器
     */
    public static <T> Callable<T> privilegedCallableUsingCurrentClassLoader(Callable<T> callable) {
        if (callable == null)
            throw new NullPointerException();
        return new PrivilegedCallableUsingCurrentClassLoader<T>(callable);
    }

    // 支持公共方法的非公共类

    /**
     * 运行给定任务并返回给定结果的可调用对象
     */
    static final class RunnableAdapter<T> implements Callable<T> {
        final Runnable task;
        final T result;
        RunnableAdapter(Runnable task, T result) {
            this.task = task;
            this.result = result;
        }
        public T call() {
            task.run();
            return result;
        }
    }

    /**
     * 在已建立的访问控制设置下运行的可调用对象
     */
    static final class PrivilegedCallable<T> implements Callable<T> {
        private final Callable<T> task;
        private final AccessControlContext acc;

        PrivilegedCallable(Callable<T> task) {
            this.task = task;
            this.acc = AccessController.getContext();
        }

        public T call() throws Exception {
            try {
                return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<T>() {
                        public T run() throws Exception {
                            return task.call();
                        }
                    }, acc);
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        }
    }

    /**
     * 在已建立的访问控制设置和当前类加载器下运行的可调用对象
     */
    static final class PrivilegedCallableUsingCurrentClassLoader<T> implements Callable<T> {
        private final Callable<T> task;
        private final AccessControlContext acc;
        private final ClassLoader ccl;

        PrivilegedCallableUsingCurrentClassLoader(Callable<T> task) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                // 从这个类调用 getContextClassLoader 永远不会触发安全检查，但我们仍然检查调用者是否有此权限。
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);

                // 无论 setContextClassLoader 是否必要，如果权限不可用，我们都会快速失败。
                sm.checkPermission(new RuntimePermission("setContextClassLoader"));
            }
            this.task = task;
            this.acc = AccessController.getContext();
            this.ccl = Thread.currentThread().getContextClassLoader();
        }

        public T call() throws Exception {
            try {
                return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<T>() {
                        public T run() throws Exception {
                            Thread t = Thread.currentThread();
                            ClassLoader cl = t.getContextClassLoader();
                            if (ccl == cl) {
                                return task.call();
                            } else {
                                t.setContextClassLoader(ccl);
                                try {
                                    return task.call();
                                } finally {
                                    t.setContextClassLoader(cl);
                                }
                            }
                        }
                    }, acc);
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        }
    }

    /**
     * 默认的线程工厂
     */
    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                                  Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                          poolNumber.getAndIncrement() +
                         "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    /**
     * 捕获访问控制上下文和类加载器的线程工厂
     */
    static class PrivilegedThreadFactory extends DefaultThreadFactory {
        private final AccessControlContext acc;
        private final ClassLoader ccl;

        PrivilegedThreadFactory() {
            super();
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                // 从这个类调用 getContextClassLoader 永远不会触发安全检查，但我们仍然检查调用者是否有此权限。
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);


                // 快速失败
                sm.checkPermission(new RuntimePermission("setContextClassLoader"));
            }
            this.acc = AccessController.getContext();
            this.ccl = Thread.currentThread().getContextClassLoader();
        }

        public Thread newThread(final Runnable r) {
            return super.newThread(new Runnable() {
                public void run() {
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        public Void run() {
                            Thread.currentThread().setContextClassLoader(ccl);
                            r.run();
                            return null;
                        }
                    }, acc);
                }
            });
        }
    }

    /**
     * 一个包装类，仅暴露ExecutorService实现的ExecutorService方法。
     */
    static class DelegatedExecutorService extends AbstractExecutorService {
        private final ExecutorService e;
        DelegatedExecutorService(ExecutorService executor) { e = executor; }
        public void execute(Runnable command) { e.execute(command); }
        public void shutdown() { e.shutdown(); }
        public List<Runnable> shutdownNow() { return e.shutdownNow(); }
        public boolean isShutdown() { return e.isShutdown(); }
        public boolean isTerminated() { return e.isTerminated(); }
        public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
            return e.awaitTermination(timeout, unit);
        }
        public Future<?> submit(Runnable task) {
            return e.submit(task);
        }
        public <T> Future<T> submit(Callable<T> task) {
            return e.submit(task);
        }
        public <T> Future<T> submit(Runnable task, T result) {
            return e.submit(task, result);
        }
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
            return e.invokeAll(tasks);
        }
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                             long timeout, TimeUnit unit)
            throws InterruptedException {
            return e.invokeAll(tasks, timeout, unit);
        }
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
            return e.invokeAny(tasks);
        }
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                               long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
            return e.invokeAny(tasks, timeout, unit);
        }
    }

    static class FinalizableDelegatedExecutorService
        extends DelegatedExecutorService {
        FinalizableDelegatedExecutorService(ExecutorService executor) {
            super(executor);
        }
        protected void finalize() {
            super.shutdown();
        }
    }

    /**
     * 一个包装类，仅暴露ScheduledExecutorService实现的ScheduledExecutorService方法。
     */
    static class DelegatedScheduledExecutorService
            extends DelegatedExecutorService
            implements ScheduledExecutorService {
        private final ScheduledExecutorService e;
        DelegatedScheduledExecutorService(ScheduledExecutorService executor) {
            super(executor);
            e = executor;
        }
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            return e.schedule(command, delay, unit);
        }
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            return e.schedule(callable, delay, unit);
        }
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            return e.scheduleAtFixedRate(command, initialDelay, period, unit);
        }
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            return e.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        }
    }

    /** 不能实例化。 */
    private Executors() {}
}
