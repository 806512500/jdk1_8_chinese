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
 * 一个在启用特权的情况下执行的计算，可能抛出一个或多个受检异常。通过调用
 * {@code AccessController.doPrivileged} 方法并传入
 * {@code PrivilegedExceptionAction} 对象来执行计算。此接口仅用于
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
     * @return 一个依赖于类的值，可能表示计算的结果。每个实现
     *         {@code PrivilegedExceptionAction} 的类都应记录此值表示的内容。
     * @throws Exception 发生异常情况。每个实现 {@code PrivilegedExceptionAction} 的类
     *         都应记录其 run 方法可能抛出的异常。
     * @see AccessController#doPrivileged(PrivilegedExceptionAction)
     * @see AccessController#doPrivileged(PrivilegedExceptionAction,AccessControlContext)
     */

    T run() throws Exception;
}
