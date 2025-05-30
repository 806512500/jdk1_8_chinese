/*
 * Copyright (c) 1999, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.reflect;

/**
 * 当代理实例上的方法调用时，如果其调用处理器的 {@link InvocationHandler#invoke invoke} 方法抛出一个
 * 检查异常（一个不是 {@code RuntimeException} 或 {@code Error} 的 {@code Throwable}），
 * 并且该异常不能分配给代理实例上调用的方法的 {@code throws} 子句中声明的任何异常类型，则抛出此异常。
 *
 * <p>{@code UndeclaredThrowableException} 实例包含由调用处理器抛出的未声明的检查异常，可以通过
 * {@code getUndeclaredThrowable()} 方法获取。
 * {@code UndeclaredThrowableException} 继承自 {@code RuntimeException}，因此它是一个未检查的异常，
 * 包装了一个检查异常。
 *
 * <p>从 1.4 版本开始，此异常已被改造以符合通用的异常链接机制。在构造时提供的“由调用处理器抛出的未声明的检查异常”
 * 可以通过 {@link #getUndeclaredThrowable()} 方法访问，现在被称为 <i>原因</i>，也可以通过 {@link
 * Throwable#getCause()} 方法以及上述“遗留方法”访问。
 *
 * @author      Peter Jones
 * @see         InvocationHandler
 * @since       1.3
 */
public class UndeclaredThrowableException extends RuntimeException {
    static final long serialVersionUID = 330127114055056639L;

    /**
     * 被抛出的未声明的检查异常
     * @serial
     */
    private Throwable undeclaredThrowable;

    /**
     * 使用指定的 {@code Throwable} 构造一个 {@code UndeclaredThrowableException}。
     *
     * @param   undeclaredThrowable 被抛出的未声明的检查异常
     */
    public UndeclaredThrowableException(Throwable undeclaredThrowable) {
        super((Throwable) null);  // 禁用 initCause
        this.undeclaredThrowable = undeclaredThrowable;
    }

    /**
     * 使用指定的 {@code Throwable} 和详细消息构造一个 {@code UndeclaredThrowableException}。
     *
     * @param   undeclaredThrowable 被抛出的未声明的检查异常
     * @param   s 详细消息
     */
    public UndeclaredThrowableException(Throwable undeclaredThrowable,
                                        String s)
    {
        super(s, null);  // 禁用 initCause
        this.undeclaredThrowable = undeclaredThrowable;
    }

    /**
     * 返回包装在 {@code UndeclaredThrowableException} 中的 {@code Throwable} 实例，可能为 {@code null}。
     *
     * <p>此方法早于通用的异常链接机制。现在推荐使用 {@link Throwable#getCause()} 方法获取此信息。
     *
     * @return 被抛出的未声明的检查异常
     */
    public Throwable getUndeclaredThrowable() {
        return undeclaredThrowable;
    }

    /**
     * 返回此异常的原因（包装在 {@code UndeclaredThrowableException} 中的 {@code Throwable} 实例，可能为 {@code null}）。
     *
     * @return  此异常的原因。
     * @since   1.4
     */
    public Throwable getCause() {
        return undeclaredThrowable;
    }
}
