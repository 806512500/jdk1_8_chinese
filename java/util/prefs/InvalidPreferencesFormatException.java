/*
 * 版权所有 (c) 2000, 2003, Oracle 和/或其附属公司。保留所有权利。
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

package java.util.prefs;

import java.io.NotSerializableException;

/**
 * 抛出此异常表示操作无法完成，因为输入不符合 {@link Preferences}
 * 规范中定义的首选项集合的适当 XML 文档类型。
 *
 * @author  Josh Bloch
 * @see     Preferences
 * @since   1.4
 */
public class InvalidPreferencesFormatException extends Exception {
    /**
     * 使用指定的原因构造一个 InvalidPreferencesFormatException。
     *
     * @param  cause 原因（稍后通过 {@link Throwable#getCause()} 方法检索）。
     */
    public InvalidPreferencesFormatException(Throwable cause) {
        super(cause);
    }

   /**
    * 使用指定的详细消息构造一个 InvalidPreferencesFormatException。
    *
    * @param   message   详细消息。详细消息稍后通过 {@link Throwable#getMessage()} 方法检索。
    */
    public InvalidPreferencesFormatException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因构造一个 InvalidPreferencesFormatException。
     *
     * @param  message   详细消息。详细消息稍后通过 {@link Throwable#getMessage()} 方法检索。
     * @param  cause 原因（稍后通过 {@link Throwable#getCause()} 方法检索）。
     */
    public InvalidPreferencesFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = -791715184232119669L;
}
