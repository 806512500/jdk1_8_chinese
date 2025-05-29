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
 * 由 Doug Lea 在 JCP JSR-166 专家组成员的帮助下编写，并发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的那样。
 */

package java.util.concurrent;

/**
 * 当阻塞操作超时时抛出的异常。对于指定了超时时间的阻塞操作，需要一种方法来
 * 表示超时已发生。对于许多这样的操作，可以返回一个表示超时的值；当这不可行或不理想时，
 * 应声明并抛出 {@code TimeoutException}。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class TimeoutException extends Exception {
    private static final long serialVersionUID = 1900926677490660714L;

    /**
     * 构造一个没有指定详细消息的 {@code TimeoutException}。
     */
    public TimeoutException() {}

    /**
     * 使用指定的详细消息构造一个 {@code TimeoutException}。
     *
     * @param message 详细消息
     */
    public TimeoutException(String message) {
        super(message);
    }
}