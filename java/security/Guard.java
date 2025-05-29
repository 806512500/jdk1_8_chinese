/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * <p>此接口表示一个守护者，这是一个用于保护对另一个对象访问的对象。
 *
 * <p>此接口包含一个方法，{@code checkGuard}，该方法有一个 {@code object} 参数。{@code checkGuard}
 * 由 GuardedObject 的 {@code getObject} 方法调用，以确定是否允许访问该对象。
 *
 * @see GuardedObject
 *
 * @author Roland Schemers
 * @author Li Gong
 */

public interface Guard {

    /**
     * 确定是否允许访问由守护者保护的对象 {@code object}。如果允许访问，则静默返回。
     * 否则，抛出一个 SecurityException。
     *
     * @param object 由守护者保护的对象。
     *
     * @exception SecurityException 如果访问被拒绝。
     *
     */
    void checkGuard(Object object) throws SecurityException;
}
