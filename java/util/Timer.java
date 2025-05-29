
/*
 * 版权所有 (c) 1999, 2008, Oracle 和/或其附属公司。保留所有权利。
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

package java.util;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一个线程设施，用于在未来在后台线程中调度任务执行。任务可以被安排一次性执行，也可以定期重复执行。
 *
 * <p>每个 <tt>Timer</tt> 对象都有一个单独的后台线程，用于顺序执行该计时器的所有任务。
 * 计时器任务应快速完成。如果一个计时器任务花费过多时间完成，它会“占用”计时器的任务执行线程。
 * 这可能会延迟后续任务的执行，当（如果）该任务最终完成时，这些任务可能会“堆积”并快速连续执行。
 *
 * <p>在最后一个对 <tt>Timer</tt> 对象的活动引用消失 <i>并且</i> 所有未完成的任务都已执行完毕后，
 * 计时器的任务执行线程会优雅地终止（并成为垃圾回收的对象）。然而，这可能需要任意长的时间发生。
 * 默认情况下，任务执行线程不作为 <i>守护线程</i> 运行，因此它可以防止应用程序终止。
 * 如果调用者希望快速终止计时器的任务执行线程，调用者应调用计时器的 <tt>cancel</tt> 方法。
 *
 * <p>如果计时器的任务执行线程意外终止，例如，因为调用了它的 <tt>stop</tt> 方法，
 * 任何进一步尝试在计时器上调度任务都将导致 <tt>IllegalStateException</tt>，
 * 就好像调用了计时器的 <tt>cancel</tt> 方法一样。
 *
 * <p>这个类是线程安全的：多个线程可以共享一个单独的 <tt>Timer</tt> 对象，而无需外部同步。
 *
 * <p>这个类 <i>不</i> 提供实时保证：它使用 <tt>Object.wait(long)</tt> 方法调度任务。
 *
 * <p>Java 5.0 引入了 {@code java.util.concurrent} 包，其中一个并发工具是
 * {@link java.util.concurrent.ScheduledThreadPoolExecutor ScheduledThreadPoolExecutor}，
 * 它是一个线程池，用于以给定的速率或延迟重复执行任务。它实际上是 {@code Timer}/{@code TimerTask}
 * 组合的更灵活的替代品，因为它允许多个服务线程，接受各种时间单位，并且不需要继承 {@code TimerTask}
 * （只需实现 {@code Runnable}）。将 {@code ScheduledThreadPoolExecutor} 配置为一个线程时，
 * 它等同于 {@code Timer}。
 *
 * <p>实现说明：此类可以扩展到大量并发调度的任务（数千个应该不成问题）。内部，
 * 它使用二叉堆表示其任务队列，因此调度任务的成本是 O(log n)，其中 n 是并发调度的任务数。
 *
 * <p>实现说明：所有构造函数都会启动一个计时器线程。
 *
 * @author  Josh Bloch
 * @see     TimerTask
 * @see     Object#wait(long)
 * @since   1.3
 */

public class Timer {
    /**
     * 计时器任务队列。此数据结构与计时器线程共享。计时器通过其各种调度调用生成任务，
     * 而计时器线程消费，根据需要执行计时器任务，并在任务过期时将其从队列中移除。
     */
    private final TaskQueue queue = new TaskQueue();

    /**
     * 计时器线程。
     */
    private final TimerThread thread = new TimerThread(queue);

    /**
     * 当没有对计时器对象的活动引用且计时器队列中没有任务时，此对象会使计时器的任务执行线程优雅地退出。
     * 它优先于计时器上的终结器，因为这样的终结器可能被子类的终结器忘记调用。
     */
    private final Object threadReaper = new Object() {
        protected void finalize() throws Throwable {
            synchronized(queue) {
                thread.newTasksMayBeScheduled = false;
                queue.notify(); // 如果队列为空，则通知。
            }
        }
    };

    /**
     * 用于生成线程名称的 ID。
     */
    private final static AtomicInteger nextSerialNumber = new AtomicInteger(0);
    private static int serialNumber() {
        return nextSerialNumber.getAndIncrement();
    }

    /**
     * 创建一个新的计时器。关联的线程 <i>不</i>
     * {@linkplain Thread#setDaemon 作为守护线程运行}。
     */
    public Timer() {
        this("Timer-" + serialNumber());
    }

    /**
     * 创建一个新的计时器，其关联的线程可以指定为
     * {@linkplain Thread#setDaemon 作为守护线程运行}。
     * 如果计时器将用于调度重复的“维护活动”，则建议使用守护线程，这些活动必须在应用程序运行期间执行，
     * 但不应延长应用程序的生命周期。
     *
     * @param isDaemon 如果关联的线程应作为守护线程运行，则为 true。
     */
    public Timer(boolean isDaemon) {
        this("Timer-" + serialNumber(), isDaemon);
    }

    /**
     * 创建一个新的计时器，其关联的线程具有指定的名称。关联的线程 <i>不</i>
     * {@linkplain Thread#setDaemon 作为守护线程运行}。
     *
     * @param name 关联线程的名称
     * @throws NullPointerException 如果 {@code name} 为 null
     * @since 1.5
     */
    public Timer(String name) {
        thread.setName(name);
        thread.start();
    }

    /**
     * 创建一个新的计时器，其关联的线程具有指定的名称，并且可以指定为
     * {@linkplain Thread#setDaemon 作为守护线程运行}。
     *
     * @param name 关联线程的名称
     * @param isDaemon 如果关联的线程应作为守护线程运行，则为 true
     * @throws NullPointerException 如果 {@code name} 为 null
     * @since 1.5
     */
    public Timer(String name, boolean isDaemon) {
        thread.setName(name);
        thread.setDaemon(isDaemon);
        thread.start();
    }

                /**
     * 安排指定的任务在指定的延迟后执行。
     *
     * @param task  要安排的任务。
     * @param delay 任务执行前的延迟时间，以毫秒为单位。
     * @throws IllegalArgumentException 如果 <tt>delay</tt> 为负数，或者
     *         <tt>delay + System.currentTimeMillis()</tt> 为负数。
     * @throws IllegalStateException 如果任务已被安排或取消，计时器已被取消，或计时器线程已终止。
     * @throws NullPointerException 如果 {@code task} 为 null
     */
    public void schedule(TimerTask task, long delay) {
        if (delay < 0)
            throw new IllegalArgumentException("Negative delay.");
        sched(task, System.currentTimeMillis()+delay, 0);
    }

    /**
     * 安排指定的任务在指定的时间执行。如果时间在过去，任务将立即执行。
     *
     * @param task 要安排的任务。
     * @param time 任务执行的时间。
     * @throws IllegalArgumentException 如果 <tt>time.getTime()</tt> 为负数。
     * @throws IllegalStateException 如果任务已被安排或取消，计时器已被取消，或计时器线程已终止。
     * @throws NullPointerException 如果 {@code task} 或 {@code time} 为 null
     */
    public void schedule(TimerTask task, Date time) {
        sched(task, time.getTime(), 0);
    }

    /**
     * 安排指定的任务以固定的延迟间隔重复执行，从指定的延迟后开始。后续的执行将在大约相等的时间间隔内进行，间隔时间为指定的周期。
     *
     * <p>在固定延迟执行中，每次执行都是相对于前一次执行的实际执行时间来安排的。如果某次执行因任何原因（如垃圾回收或其他后台活动）而延迟，后续的执行也会被延迟。从长远来看，执行的频率通常会略低于指定周期的倒数（假设 <tt>Object.wait(long)</tt> 下的系统时钟是准确的）。
     *
     * <p>固定延迟执行适用于需要“平滑性”的重复活动。换句话说，它适用于在短期内保持频率准确比在长期内保持频率准确更重要的活动。这包括大多数动画任务，如以固定间隔闪烁光标。还包括响应人类输入定期执行活动的任务，如在按键被持续按住时自动重复字符。
     *
     * @param task   要安排的任务。
     * @param delay  任务执行前的延迟时间，以毫秒为单位。
     * @param period 相继任务执行之间的时间，以毫秒为单位。
     * @throws IllegalArgumentException 如果 {@code delay < 0}，或
     *         {@code delay + System.currentTimeMillis() < 0}，或
     *         {@code period <= 0}
     * @throws IllegalStateException 如果任务已被安排或取消，计时器已被取消，或计时器线程已终止。
     * @throws NullPointerException 如果 {@code task} 为 null
     */
    public void schedule(TimerTask task, long delay, long period) {
        if (delay < 0)
            throw new IllegalArgumentException("Negative delay.");
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        sched(task, System.currentTimeMillis()+delay, -period);
    }

    /**
     * 安排指定的任务以固定的延迟间隔重复执行，从指定的时间开始。后续的执行将在大约相等的时间间隔内进行，间隔时间为指定的周期。
     *
     * <p>在固定延迟执行中，每次执行都是相对于前一次执行的实际执行时间来安排的。如果某次执行因任何原因（如垃圾回收或其他后台活动）而延迟，后续的执行也会被延迟。从长远来看，执行的频率通常会略低于指定周期的倒数（假设 <tt>Object.wait(long)</tt> 下的系统时钟是准确的）。因此，如果安排的首次执行时间在过去，它将被立即执行。
     *
     * <p>固定延迟执行适用于需要“平滑性”的重复活动。换句话说，它适用于在短期内保持频率准确比在长期内保持频率准确更重要的活动。这包括大多数动画任务，如以固定间隔闪烁光标。还包括响应人类输入定期执行活动的任务，如在按键被持续按住时自动重复字符。
     *
     * @param task   要安排的任务。
     * @param firstTime 首次执行任务的时间。
     * @param period 相继任务执行之间的时间，以毫秒为单位。
     * @throws IllegalArgumentException 如果 {@code firstTime.getTime() < 0}，或
     *         {@code period <= 0}
     * @throws IllegalStateException 如果任务已被安排或取消，计时器已被取消，或计时器线程已终止。
     * @throws NullPointerException 如果 {@code task} 或 {@code firstTime} 为 null
     */
    public void schedule(TimerTask task, Date firstTime, long period) {
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        sched(task, firstTime.getTime(), -period);
    }

    /**
     * 安排指定的任务以固定的速率重复执行，从指定的延迟后开始。后续的执行将在大约相等的时间间隔内进行，间隔时间为指定的周期。
     *
     * <p>在固定速率执行中，每次执行都是相对于初始执行的计划执行时间来安排的。如果某次执行因任何原因（如垃圾回收或其他后台活动）而延迟，将会有两次或更多次的执行快速连续发生以“赶上”。从长远来看，执行的频率将精确地等于指定周期的倒数（假设 <tt>Object.wait(long)</tt> 下的系统时钟是准确的）。
     *
     * <p>固定速率执行适用于对<em>绝对</em>时间敏感的重复活动，如每小时整点敲响钟声，或每天在特定时间进行计划维护。它也适用于执行固定次数的重复活动，其中总时间很重要，如每秒一次的倒计时计时器，持续十秒。最后，固定速率执行适用于必须相对于彼此保持同步的多个重复计时器任务的调度。
     *
     * @param task   要安排的任务。
     * @param delay  任务执行前的延迟时间，以毫秒为单位。
     * @param period 相继任务执行之间的时间，以毫秒为单位。
     * @throws IllegalArgumentException 如果 {@code delay < 0}，或
     *         {@code delay + System.currentTimeMillis() < 0}，或
     *         {@code period <= 0}
     * @throws IllegalStateException 如果任务已被安排或取消，计时器已被取消，或计时器线程已终止。
     * @throws NullPointerException 如果 {@code task} 为 null
     */
    public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        if (delay < 0)
            throw new IllegalArgumentException("Negative delay.");
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        sched(task, System.currentTimeMillis()+delay, period);
    }

                /**
     * 安排指定的任务以固定的速率重复执行，
     * 从指定的时间开始。后续执行将在大约固定的时间间隔内进行，
     * 间隔由指定的周期决定。
     *
     * <p>在固定速率执行中，每次执行都是相对于初始执行的计划执行时间进行调度的。如果由于任何原因（如垃圾回收或其他后台活动）导致执行延迟，
     * 为了“赶上”，可能会连续发生两次或更多次执行。从长远来看，执行的频率将是指定周期的倒数（假设支持<tt>Object.wait(long)</tt>的系统时钟是准确的）。
     * 因此，如果计划的第一次执行时间在过去，那么任何“错过”的执行将被立即安排为“赶上”执行。
     *
     * <p>固定速率执行适用于对<em>绝对</em>时间敏感的定期活动，例如每小时整点敲响钟声，或每天特定时间运行计划维护。
     * 它也适用于执行固定次数的定期活动，其中总执行时间很重要，例如每秒滴答一次的倒计时计时器，持续十秒。
     * 最后，固定速率执行适用于必须相互保持同步的多个重复计时器任务的调度。
     *
     * @param task   要调度的任务。
     * @param firstTime 任务首次执行的时间。
     * @param period 相继任务执行之间的时间间隔，以毫秒为单位。
     * @throws IllegalArgumentException 如果 {@code firstTime.getTime() < 0} 或
     *         {@code period <= 0}
     * @throws IllegalStateException 如果任务已被调度或取消，计时器已被取消，或计时器线程已终止。
     * @throws NullPointerException 如果 {@code task} 或 {@code firstTime} 为 null
     */
    public void scheduleAtFixedRate(TimerTask task, Date firstTime,
                                    long period) {
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        sched(task, firstTime.getTime(), period);
    }

    /**
     * 调度指定的计时器任务在指定的时间以指定的周期（以毫秒为单位）执行。如果周期为正数，任务将被安排为重复执行；
     * 如果周期为零，任务将被安排为一次性执行。时间以 Date.getTime() 格式指定。此方法检查计时器状态、任务状态和初始执行时间，但不检查周期。
     *
     * @throws IllegalArgumentException 如果 <tt>time</tt> 为负数。
     * @throws IllegalStateException 如果任务已被调度或取消，计时器已被取消，或计时器线程已终止。
     * @throws NullPointerException 如果 {@code task} 为 null
     */
    private void sched(TimerTask task, long time, long period) {
        if (time < 0)
            throw new IllegalArgumentException("Illegal execution time.");

        // 限制周期值，以防止数值溢出，同时保持实际上无限大的值。
        if (Math.abs(period) > (Long.MAX_VALUE >> 1))
            period >>= 1;

        synchronized(queue) {
            if (!thread.newTasksMayBeScheduled)
                throw new IllegalStateException("Timer already cancelled.");

            synchronized(task.lock) {
                if (task.state != TimerTask.VIRGIN)
                    throw new IllegalStateException(
                        "Task already scheduled or cancelled");
                task.nextExecutionTime = time;
                task.period = period;
                task.state = TimerTask.SCHEDULED;
            }

            queue.add(task);
            if (queue.getMin() == task)
                queue.notify();
        }
    }

    /**
     * 终止此计时器，丢弃所有当前已调度的任务。
     * 不干扰当前正在执行的任务（如果存在）。一旦计时器终止，其执行线程将优雅地终止，
     * 且不能再在此计时器上调度更多任务。
     *
     * <p>请注意，从由该计时器调用的计时器任务的 run 方法内部调用此方法绝对可以保证
     * 正在进行的任务执行是此计时器将执行的最后一个任务执行。
     *
     * <p>可以多次调用此方法；第二次及后续调用无效。
     */
    public void cancel() {
        synchronized(queue) {
            thread.newTasksMayBeScheduled = false;
            queue.clear();
            queue.notify();  // 如果队列已为空，则通知。
        }
    }

    /**
     * 从该计时器的任务队列中移除所有已取消的任务。<i>调用此方法不会影响计时器的行为</i>，但
     * 会从队列中消除对已取消任务的引用。如果没有这些任务的外部引用，它们将有资格进行垃圾回收。
     *
     * <p>大多数程序不需要调用此方法。
     * 它是为罕见的应用设计的，这些应用取消了大量任务。调用此方法以时间换空间：方法的运行时间可能与 n + c log n 成正比，其中 n
     * 是队列中的任务数，c 是已取消的任务数。
     *
     * <p>请注意，可以从在此计时器上调度的任务内部调用此方法。
     *
     * @return 从队列中移除的任务数。
     * @since 1.5
     */
     public int purge() {
         int result = 0;

         synchronized(queue) {
             for (int i = queue.size(); i > 0; i--) {
                 if (queue.get(i).state == TimerTask.CANCELLED) {
                     queue.quickRemove(i);
                     result++;
                 }
             }


                         if (result != 0)
                 queue.heapify();
         }

         return result;
     }
}

/**
 * 此“辅助类”实现了计时器的任务执行线程，该线程在计时器队列上等待任务，
 * 当任务触发时执行它们，重新安排重复任务，并从队列中移除已取消的任务和已执行的非重复任务。
 */
class TimerThread extends Thread {
    /**
     * 此标志由收割者设置为false，以告知我们没有更多对我们的Timer对象的活动引用。
     * 一旦此标志为true且队列中没有更多任务，就没有工作可做，因此我们优雅地终止。
     * 请注意，此字段受队列监视器的保护！
     */
    boolean newTasksMayBeScheduled = true;

    /**
     * 我们的计时器队列。我们存储此引用，而不是计时器的引用，以保持引用图的无环。
     * 否则，计时器永远不会被垃圾回收，这个线程也不会消失。
     */
    private TaskQueue queue;

    TimerThread(TaskQueue queue) {
        this.queue = queue;
    }

    public void run() {
        try {
            mainLoop();
        } finally {
            // 有人终止了这个线程，表现得好像计时器被取消了一样
            synchronized(queue) {
                newTasksMayBeScheduled = false;
                queue.clear();  // 消除过时的引用
            }
        }
    }

    /**
     * 主计时器循环。 (参见类注释。)
     */
    private void mainLoop() {
        while (true) {
            try {
                TimerTask task;
                boolean taskFired;
                synchronized(queue) {
                    // 等待队列变为非空
                    while (queue.isEmpty() && newTasksMayBeScheduled)
                        queue.wait();
                    if (queue.isEmpty())
                        break; // 队列为空且将永远保持；终止

                    // 队列非空；查看第一个事件并采取正确操作
                    long currentTime, executionTime;
                    task = queue.getMin();
                    synchronized(task.lock) {
                        if (task.state == TimerTask.CANCELLED) {
                            queue.removeMin();
                            continue;  // 无需操作，再次轮询队列
                        }
                        currentTime = System.currentTimeMillis();
                        executionTime = task.nextExecutionTime;
                        if (taskFired = (executionTime<=currentTime)) {
                            if (task.period == 0) { // 非重复任务，移除
                                queue.removeMin();
                                task.state = TimerTask.EXECUTED;
                            } else { // 重复任务，重新安排
                                queue.rescheduleMin(
                                  task.period<0 ? currentTime   - task.period
                                                : executionTime + task.period);
                            }
                        }
                    }
                    if (!taskFired) // 任务尚未触发；等待
                        queue.wait(executionTime - currentTime);
                }
                if (taskFired)  // 任务触发；运行它，不持有任何锁
                    task.run();
            } catch(InterruptedException e) {
            }
        }
    }
}

/**
 * 此类表示一个计时器任务队列：一个按nextExecutionTime排序的TimerTasks优先队列。
 * 每个计时器对象都有一个这样的队列，它与计时器线程共享。内部使用堆，提供对add、removeMin和rescheduleMin
 * 操作的log(n)性能，以及对getMin操作的常数时间性能。
 */
class TaskQueue {
    /**
     * 以平衡二叉堆表示的优先队列：queue[n]的两个子节点是queue[2*n]和queue[2*n+1]。
     * 优先队列按nextExecutionTime字段排序：nextExecutionTime最低的TimerTask在queue[1]中（假设队列非空）。
     * 对于堆中的每个节点n及其每个后代d，n.nextExecutionTime <= d.nextExecutionTime。
     */
    private TimerTask[] queue = new TimerTask[128];

    /**
     * 优先队列中的任务数量。（任务存储在queue[1]到queue[size]中。）
     */
    private int size = 0;

    /**
     * 返回当前队列中的任务数量。
     */
    int size() {
        return size;
    }

    /**
     * 向优先队列中添加一个新任务。
     */
    void add(TimerTask task) {
        // 必要时扩展后备存储
        if (size + 1 == queue.length)
            queue = Arrays.copyOf(queue, 2*queue.length);

        queue[++size] = task;
        fixUp(size);
    }

    /**
     * 返回优先队列的“头任务”。（头任务是nextExecutionTime最低的任务。）
     */
    TimerTask getMin() {
        return queue[1];
    }

    /**
     * 返回优先队列中的第i个任务，其中i的范围从1（头任务，由getMin返回）到队列中的任务数量，包括在内。
     */
    TimerTask get(int i) {
        return queue[i];
    }

    /**
     * 从优先队列中移除头任务。
     */
    void removeMin() {
        queue[1] = queue[size];
        queue[size--] = null;  // 丢弃额外的引用以防止内存泄漏
        fixDown(1);
    }

    /**
     * 从队列中移除第i个元素，不考虑维护堆不变性。回想一下，队列是一基的，所以
     * 1 <= i <= size。
     */
    void quickRemove(int i) {
        assert i <= size;

        queue[i] = queue[size];
        queue[size--] = null;  // 丢弃额外的引用以防止内存泄漏
    }

    /**
     * 将头任务的nextExecutionTime设置为指定值，并相应地调整优先队列。
     */
    void rescheduleMin(long newTime) {
        queue[1].nextExecutionTime = newTime;
        fixDown(1);
    }

    /**
     * 如果优先队列中不包含任何元素，则返回 true。
     */
    boolean isEmpty() {
        return size==0;
    }

    /**
     * 从优先队列中移除所有元素。
     */
    void clear() {
        // 将任务引用设置为 null 以防止内存泄漏
        for (int i=1; i<=size; i++)
            queue[i] = null;

        size = 0;
    }

    /**
     * 建立堆不变性（如上所述），假设堆除了可能的索引为 k 的叶节点外满足不变性
     * （该节点的 nextExecutionTime 可能小于其父节点的）。
     *
     * 该方法通过将 queue[k] 在层次结构中“提升”（通过与父节点交换）来实现
     * 直到 queue[k] 的 nextExecutionTime 大于或等于其父节点的 nextExecutionTime。
     */
    private void fixUp(int k) {
        while (k > 1) {
            int j = k >> 1;
            if (queue[j].nextExecutionTime <= queue[k].nextExecutionTime)
                break;
            TimerTask tmp = queue[j];  queue[j] = queue[k]; queue[k] = tmp;
            k = j;
        }
    }

    /**
     * 在以 k 为根的子树中建立堆不变性（如上所述），假设除了可能的节点 k 本身外
     * 子树满足堆不变性（该节点的 nextExecutionTime 可能大于其子节点的）。
     *
     * 该方法通过将 queue[k] 在层次结构中“降级”（通过与较小的子节点交换）来实现
     * 直到 queue[k] 的 nextExecutionTime 小于或等于其子节点的 nextExecutionTime。
     */
    private void fixDown(int k) {
        int j;
        while ((j = k << 1) <= size && j > 0) {
            if (j < size &&
                queue[j].nextExecutionTime > queue[j+1].nextExecutionTime)
                j++; // j 索引最小的子节点
            if (queue[k].nextExecutionTime <= queue[j].nextExecutionTime)
                break;
            TimerTask tmp = queue[j];  queue[j] = queue[k]; queue[k] = tmp;
            k = j;
        }
    }

    /**
     * 在整个树中建立堆不变性（如上所述），假设调用前元素的顺序没有任何保证。
     */
    void heapify() {
        for (int i = size/2; i >= 1; i--)
            fixDown(i);
    }
}
