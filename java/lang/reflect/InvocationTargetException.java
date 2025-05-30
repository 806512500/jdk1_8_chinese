/*
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
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
 * InvocationTargetException 是一个检查异常，用于包装由调用的方法或构造函数抛出的异常。
 *
 * <p>从 1.4 版本开始，此异常已被改造以符合通用的异常链机制。在构造时提供的“目标异常”以及通过
 * {@link #getTargetException()} 方法访问的异常现在被称为 <i>原因</i>，也可以通过
 * {@link Throwable#getCause()} 方法访问，以及上述“遗留方法”。
 *
 * @see Method
 * @see Constructor
 */
public class InvocationTargetException extends ReflectiveOperationException {
    /**
     * 使用 JDK 1.1.X 的 serialVersionUID 以确保互操作性
     */
    private static final long serialVersionUID = 4085088731926701167L;

     /**
     * 如果使用了 InvocationTargetException(Throwable target) 构造函数实例化对象，
     * 则此字段保存目标异常。
     *
     * @serial
     *
     */
    private Throwable target;

    /**
     * 构造一个 {@code InvocationTargetException}，目标异常为 {@code null}。
     */
    protected InvocationTargetException() {
        super((Throwable)null);  // 禁用 initCause
    }

    /**
     * 使用目标异常构造一个 InvocationTargetException。
     *
     * @param target 目标异常
     */
    public InvocationTargetException(Throwable target) {
        super((Throwable)null);  // 禁用 initCause
        this.target = target;
    }

    /**
     * 使用目标异常和详细消息构造一个 InvocationTargetException。
     *
     * @param target 目标异常
     * @param s      详细消息
     */
    public InvocationTargetException(Throwable target, String s) {
        super(s, null);  // 禁用 initCause
        this.target = target;
    }

    /**
     * 获取抛出的目标异常。
     *
     * <p>此方法早于通用异常链机制。现在推荐使用 {@link Throwable#getCause()} 方法
     * 获取此信息。
     *
     * @return 抛出的目标异常（此异常的原因）。
     */
    public Throwable getTargetException() {
        return target;
    }

    /**
     * 返回此异常的原因（抛出的目标异常，可能为 {@code null}）。
     *
     * @return  此异常的原因。
     * @since   1.4
     */
    public Throwable getCause() {
        return target;
    }
}
