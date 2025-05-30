/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

/**
 * 当 {@code doPrivileged(PrivilegedExceptionAction)} 和
 * {@code doPrivileged(PrivilegedExceptionAction,
 * AccessControlContext context)} 执行的操作抛出一个检查异常时，会抛出此异常。
 * 通过调用 {@code getException} 方法可以获取操作抛出的异常。实际上，
 * {@code PrivilegedActionException} 是一个“包装器”，用于包装特权操作抛出的异常。
 *
 * <p>自 1.4 版本起，此异常已被改造以符合通用的异常链接机制。构造时提供的“特权计算抛出的异常”
 * 以及通过 {@link #getException()} 方法访问的异常现在被称为<i>原因</i>，
 * 也可以通过 {@link Throwable#getCause()} 方法以及上述“遗留方法”访问。
 *
 * @see PrivilegedExceptionAction
 * @see AccessController#doPrivileged(PrivilegedExceptionAction)
 * @see AccessController#doPrivileged(PrivilegedExceptionAction,AccessControlContext)
 */
public class PrivilegedActionException extends Exception {
    // 为了互操作性，使用 JDK 1.2.2 的 serialVersionUID
    private static final long serialVersionUID = 4724086851538908602L;

    /**
     * @serial
     */
    private Exception exception;

    /**
     * 构造一个新的 PrivilegedActionException，用于“包装”特定的异常。
     *
     * @param exception 抛出的异常
     */
    public PrivilegedActionException(Exception exception) {
        super((Throwable)null);  // 禁止调用 initCause
        this.exception = exception;
    }

    /**
     * 返回导致此 {@code PrivilegedActionException} 的特权计算抛出的异常。
     *
     * <p>此方法早于通用的异常链接机制。现在推荐使用 {@link Throwable#getCause()} 方法
     * 获取此信息。
     *
     * @return 导致此 {@code PrivilegedActionException} 的特权计算抛出的异常。
     * @see PrivilegedExceptionAction
     * @see AccessController#doPrivileged(PrivilegedExceptionAction)
     * @see AccessController#doPrivileged(PrivilegedExceptionAction,
     *                                            AccessControlContext)
     */
    public Exception getException() {
        return exception;
    }

    /**
     * 返回此异常的原因（导致此 {@code PrivilegedActionException} 的特权计算抛出的异常）。
     *
     * @return  此异常的原因。
     * @since   1.4
     */
    public Throwable getCause() {
        return exception;
    }

    public String toString() {
        String s = getClass().getName();
        return (exception != null) ? (s + ": " + exception.toString()) : s;
    }
}
