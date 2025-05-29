/*
 * Copyright (c) 2004, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

package java.lang.annotation;

/**
 * 当注释解析器尝试从类文件中读取注释并确定注释格式不正确时抛出此异常。
 * 此错误可以通过 {@linkplain
 * java.lang.reflect.AnnotatedElement 用于反射读取注释的API} 抛出。
 *
 * @author  Josh Bloch
 * @see     java.lang.reflect.AnnotatedElement
 * @since   1.5
 */
public class AnnotationFormatError extends Error {
    private static final long serialVersionUID = -4256701562333669892L;

    /**
     * 使用指定的详细消息构造一个新的 <tt>AnnotationFormatError</tt>。
     *
     * @param   message   详细消息。
     */
    public AnnotationFormatError(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因构造一个新的 <tt>AnnotationFormatError</tt>。请注意，与 <code>cause</code> 关联的详细消息
     * <i>不会</i> 自动包含在此错误的详细消息中。
     *
     * @param  message 详细消息
     * @param  cause 原因（允许 <tt>null</tt> 值，表示原因不存在或未知。）
     */
    public AnnotationFormatError(String message, Throwable cause) {
        super(message, cause);
    }


    /**
     * 使用指定的原因和详细消息 <tt>(cause == null ? null : cause.toString())</tt> 构造一个新的 <tt>AnnotationFormatError</tt>
     * （通常包含 <tt>cause</tt> 的类和详细消息）。
     *
     * @param  cause 原因（允许 <tt>null</tt> 值，表示原因不存在或未知。）
     */
    public AnnotationFormatError(Throwable cause) {
        super(cause);
    }
}
