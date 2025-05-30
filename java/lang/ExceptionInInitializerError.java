/*
 * Copyright (c) 1996, 2000, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

/**
 * 表示在静态初始化器中发生了一个意外的异常。
 * <code>ExceptionInInitializerError</code> 用于指示在静态初始化器或静态变量的初始化过程中发生了异常。
 *
 * <p>从 1.4 版本开始，此异常已进行了调整以符合通用的异常链机制。在构造时可以提供的“保存的可抛出对象”现在被称为 <i>原因</i>，
 * 可以通过 {@link Throwable#getCause()} 方法访问，也可以通过上述“遗留方法”访问。
 *
 * @author  Frank Yellin
 * @since   JDK1.1
 */
public class ExceptionInInitializerError extends LinkageError {
    /**
     * 用于互操作性的 JDK 1.1.X 的 serialVersionUID
     */
    private static final long serialVersionUID = 1521711792217232256L;

    /**
     * 如果使用了 ExceptionInInitializerError(Throwable thrown) 构造函数来实例化对象，则此字段保存异常。
     *
     * @serial
     *
     */
    private Throwable exception;

    /**
     * 构造一个 <code>ExceptionInInitializerError</code>，其详细消息字符串为 <code>null</code>，并且没有保存的可抛出对象。
     * 详细消息是一个描述此特定异常的字符串。
     */
    public ExceptionInInitializerError() {
        initCause(null);  // 禁止后续的 initCause
    }

    /**
     * 通过保存一个 <code>Throwable</code> 对象的引用来构造一个新的 <code>ExceptionInInitializerError</code> 类，
     * 该引用可以通过 {@link #getException()} 方法稍后检索。详细消息字符串设置为 <code>null</code>。
     *
     * @param thrown 抛出的异常
     */
    public ExceptionInInitializerError(Throwable thrown) {
        initCause(null);  // 禁止后续的 initCause
        this.exception = thrown;
    }

    /**
     * 使用指定的详细消息字符串构造一个 ExceptionInInitializerError。详细消息是一个描述此特定异常的字符串。
     * 详细消息字符串将保存以供稍后通过 {@link Throwable#getMessage()} 方法检索。没有保存的可抛出对象。
     *
     *
     * @param s 详细消息
     */
    public ExceptionInInitializerError(String s) {
        super(s);
        initCause(null);  // 禁止后续的 initCause
    }

    /**
     * 返回在静态初始化过程中导致此错误创建的异常。
     *
     * <p>此方法早于通用异常链机制。现在 {@link Throwable#getCause()} 方法是获取此信息的首选方法。
     *
     * @return 此 <code>ExceptionInInitializerError</code> 的保存的可抛出对象，或 <code>null</code>
     *         如果此 <code>ExceptionInInitializerError</code> 没有保存的可抛出对象。
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * 返回此错误的原因（在静态初始化过程中导致此错误创建的异常）。
     *
     * @return  此错误的原因或 <code>null</code>，如果原因不存在或未知。
     * @since   1.4
     */
    public Throwable getCause() {
        return exception;
    }
}
