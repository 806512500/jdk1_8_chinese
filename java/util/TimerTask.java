/*
 * 版权所有 (c) 1999, 2013, Oracle 和/或其附属公司。保留所有权利。
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

/**
 * 一个可以由 Timer 安排一次性或重复执行的任务。
 *
 * @author  Josh Bloch
 * @see     Timer
 * @since   1.3
 */

public abstract class TimerTask implements Runnable {
    /**
     * 此对象用于控制对 TimerTask 内部的访问。
     */
    final Object lock = new Object();

    /**
     * 此任务的状态，从以下常量中选择。
     */
    int state = VIRGIN;

    /**
     * 该任务尚未被安排。
     */
    static final int VIRGIN = 0;

    /**
     * 该任务已被安排执行。如果它是一个非重复任务，则尚未执行。
     */
    static final int SCHEDULED   = 1;

    /**
     * 该非重复任务已执行（或正在执行）且未被取消。
     */
    static final int EXECUTED    = 2;

    /**
     * 该任务已被取消（通过调用 TimerTask.cancel）。
     */
    static final int CANCELLED   = 3;

    /**
     * 该任务的下一次执行时间，格式为 System.currentTimeMillis() 返回的格式，假设该任务已被安排执行。
     * 对于重复任务，此字段在每次任务执行前更新。
     */
    long nextExecutionTime;

    /**
     * 重复任务的周期（毫秒）。正值表示固定速率执行。负值表示固定延迟执行。值为 0 表示非重复任务。
     */
    long period = 0;

    /**
     * 创建一个新的计时器任务。
     */
    protected TimerTask() {
    }

    /**
     * 此计时器任务要执行的操作。
     */
    public abstract void run();

    /**
     * 取消此计时器任务。如果任务已被安排一次性执行且尚未运行，或尚未被安排，它将永远不会运行。如果任务已被安排重复执行，它将永远不会再次运行。（如果任务在调用此方法时正在运行，任务将运行到完成，但将永远不会再次运行。）
     *
     * <p>注意，从重复计时器任务的 <tt>run</tt> 方法内部调用此方法绝对可以保证计时器任务不会再次运行。
     *
     * <p>此方法可以多次调用；第二次及后续调用没有效果。
     *
     * @return 如果此任务已被安排一次性执行且尚未运行，或此任务已被安排重复执行，则返回 true。如果任务已被安排一次性执行且已运行，或任务从未被安排，或任务已被取消，则返回 false。（粗略地说，如果此方法阻止了一个或多个已安排的执行，则返回 <tt>true</tt>。）
     */
    public boolean cancel() {
        synchronized(lock) {
            boolean result = (state == SCHEDULED);
            state = CANCELLED;
            return result;
        }
    }

    /**
     * 返回此任务最近一次实际执行的预定执行时间。（如果在任务执行过程中调用此方法，返回值是正在进行的任务执行的预定执行时间。）
     *
     * <p>此方法通常在任务的 run 方法内部调用，以确定当前任务执行是否足够及时以执行预定活动：
     * <pre>{@code
     *   public void run() {
     *       if (System.currentTimeMillis() - scheduledExecutionTime() >=
     *           MAX_TARDINESS)
     *               return;  // 太晚了；跳过此次执行。
     *       // 执行任务
     *   }
     * }</pre>
     * 此方法通常不与 <i>固定延迟执行</i> 的重复任务一起使用，因为它们的预定执行时间允许随时间漂移，因此不太重要。
     *
     * @return 最近一次执行此任务时预定发生的时间，格式为 Date.getTime() 返回的格式。如果任务尚未开始其首次执行，则返回值未定义。
     * @see Date#getTime()
     */
    public long scheduledExecutionTime() {
        synchronized(lock) {
            return (period < 0 ? nextExecutionTime + period
                               : nextExecutionTime - period);
        }
    }
}