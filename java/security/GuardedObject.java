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
 * GuardedObject 是一个用于保护对另一个对象访问的对象。
 *
 * <p>GuardedObject 封装了一个目标对象和一个 Guard 对象，使得对目标对象的访问
 * 只有在 Guard 对象允许的情况下才可能进行。
 * 一旦一个对象被 GuardedObject 封装，对该对象的访问就由 {@code getObject}
 * 方法控制，该方法会调用
 * 保护访问的 Guard 对象的
 * {@code checkGuard} 方法。如果访问不被允许，
 * 则会抛出异常。
 *
 * @see Guard
 * @see Permission
 *
 * @author Roland Schemers
 * @author Li Gong
 */

public class GuardedObject implements java.io.Serializable {

    private static final long serialVersionUID = -5240450096227834308L;

    private Object object; // 我们保护的对象
    private Guard guard;   // 保护对象

    /**
     * 使用指定的对象和保护对象构造一个 GuardedObject。
     * 如果 Guard 对象为 null，则不会对谁可以访问该对象施加任何限制。
     *
     * @param object 要保护的对象。
     *
     * @param guard 保护访问对象的 Guard 对象。
     */

    public GuardedObject(Object object, Guard guard)
    {
        this.guard = guard;
        this.object = object;
    }

    /**
     * 检索受保护的对象，如果保护对象拒绝访问，则抛出异常。
     *
     * @return 受保护的对象。
     *
     * @exception SecurityException 如果访问受保护的对象被拒绝。
     */
    public Object getObject()
        throws SecurityException
    {
        if (guard != null)
            guard.checkGuard(object);

        return object;
    }

    /**
     * 将此对象写入流（即序列化它）。
     * 如果有保护对象，我们检查保护对象。
     */
    private void writeObject(java.io.ObjectOutputStream oos)
        throws java.io.IOException
    {
        if (guard != null)
            guard.checkGuard(object);

        oos.defaultWriteObject();
    }
}
