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
 * 一个在启用特权的情况下执行的计算，可能抛出一个或多个受检异常。通过调用
 * {@code AccessController.doPrivileged} 方法并传入
 * {@code PrivilegedExceptionAction} 对象来执行此计算。此接口仅用于
 * 抛出受检异常的计算；不抛出受检异常的计算应使用 {@code PrivilegedAction}。
 *
 * @see AccessController
 * @see AccessController#doPrivileged(PrivilegedExceptionAction)
 * @see AccessController#doPrivileged(PrivilegedExceptionAction,
 *                                              AccessControlContext)
 * @see PrivilegedAction
 */

public interface PrivilegedExceptionAction<T> {
    /**
     * 执行计算。此方法将在启用特权后由
     * {@code AccessController.doPrivileged} 调用。
     *
     * @return 一个类依赖的值，可能表示计算的结果。每个实现
     *         {@code PrivilegedExceptionAction} 的类都应记录此值表示的内容。
     * @throws Exception 发生了异常情况。每个实现 {@code PrivilegedExceptionAction} 的类
     *         都应记录其 run 方法可能抛出的异常。
     * @see AccessController#doPrivileged(PrivilegedExceptionAction)
     * @see AccessController#doPrivileged(PrivilegedExceptionAction,AccessControlContext)
     */

    T run() throws Exception;
}
