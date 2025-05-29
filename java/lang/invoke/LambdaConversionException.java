/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang.invoke;

/**
 * LambdaConversionException
 */
public class LambdaConversionException extends Exception {
    private static final long serialVersionUID = 292L + 8L;

    /**
     * 构造一个 {@code LambdaConversionException}。
     */
    public LambdaConversionException() {
    }

    /**
     * 使用消息构造一个 {@code LambdaConversionException}。
     * @param message 详细消息
     */
    public LambdaConversionException(String message) {
        super(message);
    }

    /**
     * 使用消息和原因构造一个 {@code LambdaConversionException}。
     * @param message 详细消息
     * @param cause 原因
     */
    public LambdaConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用原因构造一个 {@code LambdaConversionException}。
     * @param cause 原因
     */
    public LambdaConversionException(Throwable cause) {
        super(cause);
    }

    /**
     * 使用消息、原因和其他设置构造一个 {@code LambdaConversionException}。
     * @param message 详细消息
     * @param cause 原因
     * @param enableSuppression 是否启用抑制异常
     * @param writableStackTrace 是否允许写入堆栈跟踪
     */
    public LambdaConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
