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
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的那样发布到公共领域。
 */

package java.util.concurrent;

/**
 * 表示值生成任务（如 {@link FutureTask}）的结果无法检索，因为任务已被取消的异常。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class CancellationException extends IllegalStateException {
    private static final long serialVersionUID = -9202173006928992231L;

    /**
     * 构造一个没有详细消息的 {@code CancellationException}。
     */
    public CancellationException() {}

    /**
     * 使用指定的详细消息构造一个 {@code CancellationException}。
     *
     * @param message 详细消息
     */
    public CancellationException(String message) {
        super(message);
    }
}