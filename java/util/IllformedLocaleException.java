/*
 *******************************************************************************
 * Copyright (C) 2009-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package java.util;

/**
 * 由 {@link Locale} 和 {@link Locale.Builder} 中的方法抛出，表示参数不是格式良好的 BCP 47 标签。
 *
 * @see Locale
 * @since 1.7
 */
public class IllformedLocaleException extends RuntimeException {

    private static final long serialVersionUID = -5245986824925681401L;

    private int _errIdx = -1;

    /**
     * 构造一个新的 <code>IllformedLocaleException</code>，没有详细消息，错误索引为 -1。
     */
    public IllformedLocaleException() {
        super();
    }

    /**
     * 使用给定的消息构造一个新的 <code>IllformedLocaleException</code>，错误索引为 -1。
     *
     * @param message 消息
     */
    public IllformedLocaleException(String message) {
        super(message);
    }

    /**
     * 使用给定的消息和错误索引构造一个新的 <code>IllformedLocaleException</code>。错误索引是从不合法值的开始到首次检测到错误点的近似偏移量。负的错误索引值表示错误索引不适用或未知。
     *
     * @param message 消息
     * @param errorIndex 索引
     */
    public IllformedLocaleException(String message, int errorIndex) {
        super(message + ((errorIndex < 0) ? "" : " [at index " + errorIndex + "]"));
        _errIdx = errorIndex;
    }

    /**
     * 返回错误被发现的索引。负值表示错误索引不适用或未知。
     *
     * @return 错误索引
     */
    public int getErrorIndex() {
        return _errIdx;
    }
}
