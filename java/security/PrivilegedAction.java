/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security;


/**
 * 一个在启用特权的情况下执行的计算。计算是通过调用 {@code AccessController.doPrivileged} 方法并传递
 * {@code PrivilegedAction} 对象来完成的。此接口仅用于不抛出受检异常的计算；抛出受检异常的计算必须使用
 * {@code PrivilegedExceptionAction}。
 *
 * @see AccessController
 * @see AccessController#doPrivileged(PrivilegedAction)
 * @see PrivilegedExceptionAction
 */

public interface PrivilegedAction<T> {
    /**
     * 执行计算。此方法将在启用特权后由 {@code AccessController.doPrivileged} 调用。
     *
     * @return 一个可能表示计算结果的类依赖值。每个实现 {@code PrivilegedAction} 的类都应文档化此值（如果有的话）表示的内容。
     * @see AccessController#doPrivileged(PrivilegedAction)
     * @see AccessController#doPrivileged(PrivilegedAction,
     *                                     AccessControlContext)
     */
    T run();
}
