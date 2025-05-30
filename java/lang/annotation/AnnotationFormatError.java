/*
 * Copyright (c) 2004, 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang.annotation;

/**
 * 当注解解析器尝试从类文件中读取注解并确定注解格式错误时抛出此异常。
 * 此错误可以通过 {@linkplain
 * java.lang.reflect.AnnotatedElement 用于反射读取注解的API} 抛出。
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
     * 使用指定的详细消息和原因构造一个新的 <tt>AnnotationFormatError</tt>。
     * 请注意，与 <code>cause</code> 关联的详细消息 <i>不会</i> 自动包含在此错误的详细消息中。
     *
     * @param  message 详细消息
     * @param  cause 原因（允许 <tt>null</tt> 值，表示原因不存在或未知。）
     */
    public AnnotationFormatError(String message, Throwable cause) {
        super(message, cause);
    }


    /**
     * 使用指定的原因和详细消息 <tt>(cause == null ? null : cause.toString())</tt> 构造一个新的 <tt>AnnotationFormatError</tt>。
     * （通常包含 <tt>cause</tt> 的类和详细消息）。
     *
     * @param  cause 原因（允许 <tt>null</tt> 值，表示原因不存在或未知。）
     */
    public AnnotationFormatError(Throwable cause) {
        super(cause);
    }
}
