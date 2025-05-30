/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * <p> 此接口表示一个保护器，该保护器用于保护对另一个对象的访问。
 *
 * <p>此接口包含一个方法 {@code checkGuard}，该方法具有一个 {@code object} 参数。{@code checkGuard}
 * 由 GuardedObject 的 {@code getObject} 方法调用，以确定是否允许访问该对象。
 *
 * @see GuardedObject
 *
 * @author Roland Schemers
 * @author Li Gong
 */

public interface Guard {

    /**
     * 确定是否允许访问受保护的对象 {@code object}。如果允许访问，则静默返回。
     * 否则，抛出 SecurityException。
     *
     * @param object 由保护器保护的对象。
     *
     * @exception SecurityException 如果访问被拒绝。
     *
     */
    void checkGuard(Object object) throws SecurityException;
}
