/*
 * Copyright (c) 1995, 2004, Oracle and/or its affiliates. All rights reserved.
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
 * 当应用程序尝试通过其字符串名称加载类时，如果找不到指定名称的类定义，则抛出此异常。使用以下方法之一：
 * <ul>
 * <li>类 <code>Class</code> 中的 <code>forName</code> 方法。
 * <li>类 <code>ClassLoader</code> 中的 <code>findSystemClass</code> 方法。
 * <li>类 <code>ClassLoader</code> 中的 <code>loadClass</code> 方法。
 * </ul>
 * <p>
 * 从 1.4 版本开始，此异常已调整为符合通用的异常链机制。构造时可以提供的“在加载类时引发的可选异常”现在称为<i>原因</i>，可以通过 {@link #getException()} 方法访问，也可以通过上述“遗留方法”访问。
 *
 * @author  未署名
 * @see     java.lang.Class#forName(java.lang.String)
 * @see     java.lang.ClassLoader#findSystemClass(java.lang.String)
 * @see     java.lang.ClassLoader#loadClass(java.lang.String, boolean)
 * @since   JDK1.0
 */
public class ClassNotFoundException extends ReflectiveOperationException {
    /**
     * 为了互操作性，使用 JDK 1.1.X 的 serialVersionUID
     */
     private static final long serialVersionUID = 9176873029745254542L;

    /**
     * 如果使用了 <code>ClassNotFoundException(String s, Throwable ex)</code> 构造函数实例化对象，此字段将保存异常 ex
     * @serial
     * @since 1.2
     */
    private Throwable ex;

    /**
     * 构造一个没有详细消息的 <code>ClassNotFoundException</code>。
     */
    public ClassNotFoundException() {
        super((Throwable)null);  // 禁用 initCause
    }

    /**
     * 使用指定的详细消息构造一个 <code>ClassNotFoundException</code>。
     *
     * @param   s   详细消息。
     */
    public ClassNotFoundException(String s) {
        super(s, null);  // 禁用 initCause
    }

    /**
     * 使用指定的详细消息和在加载类时引发的可选异常构造一个 <code>ClassNotFoundException</code>。
     *
     * @param s 详细消息
     * @param ex 在加载类时引发的异常
     * @since 1.2
     */
    public ClassNotFoundException(String s, Throwable ex) {
        super(s, null);  // 禁用 initCause
        this.ex = ex;
    }

    /**
     * 如果在尝试加载类时发生错误，返回引发的异常。否则，返回 <tt>null</tt>。
     *
     * <p>此方法早于通用的异常链机制。现在推荐使用 {@link Throwable#getCause()} 方法获取此信息。
     *
     * @return 尝试加载类时引发的 <code>Exception</code>
     * @since 1.2
     */
    public Throwable getException() {
        return ex;
    }

    /**
     * 返回此异常的原因（在尝试加载类时引发的异常；否则 <tt>null</tt>）。
     *
     * @return  此异常的原因。
     * @since   1.4
     */
    public Throwable getCause() {
        return ex;
    }
}
