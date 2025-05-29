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
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的条款发布到公共领域。
 */

package java.util.concurrent;

/**
 * 当线程尝试等待一个处于损坏状态的屏障，或者在线程等待过程中屏障进入损坏状态时抛出的异常。
 *
 * @see CyclicBarrier
 *
 * @since 1.5
 * @author Doug Lea
 */
public class BrokenBarrierException extends Exception {
    private static final long serialVersionUID = 7117394618823254244L;

    /**
     * 构造一个没有指定详细消息的 {@code BrokenBarrierException}。
     */
    public BrokenBarrierException() {}

    /**
     * 使用指定的详细消息构造一个 {@code BrokenBarrierException}。
     *
     * @param message 详细消息
     */
    public BrokenBarrierException(String message) {
        super(message);
    }
}