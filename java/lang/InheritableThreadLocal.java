/*
 * Copyright (c) 1998, 2012, Oracle and/or its affiliates. All rights reserved.
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
import java.lang.ref.*;

/**
 * 此类扩展了 <tt>ThreadLocal</tt> 以提供从父线程到子线程的值继承：当创建子线程时，
 * 子线程会接收所有继承线程局部变量的初始值，这些变量在父线程中具有值。通常情况下，
 * 子线程的值与父线程的值相同；但是，可以通过在此类中覆盖 <tt>childValue</tt> 方法
 * 将子线程的值设置为父线程值的任意函数。
 *
 * <p>当需要自动将线程的某些属性（例如用户ID、事务ID）传输到创建的任何子线程时，
 * 通常使用继承线程局部变量，而不是普通线程局部变量。
 *
 * @author  Josh Bloch 和 Doug Lea
 * @see     ThreadLocal
 * @since   1.2
 */

public class InheritableThreadLocal<T> extends ThreadLocal<T> {
    /**
     * 计算继承线程局部变量在子线程创建时的初始值，作为父线程值的函数。此方法在子线程启动前
     * 由父线程调用。
     * <p>
     * 此方法仅返回其输入参数，如果需要不同的行为，应覆盖此方法。
     *
     * @param parentValue 父线程的值
     * @return 子线程的初始值
     */
    protected T childValue(T parentValue) {
        return parentValue;
    }

    /**
     * 获取与 ThreadLocal 关联的映射。
     *
     * @param t 当前线程
     */
    ThreadLocalMap getMap(Thread t) {
       return t.inheritableThreadLocals;
    }

    /**
     * 创建与 ThreadLocal 关联的映射。
     *
     * @param t 当前线程
     * @param firstValue 表的初始条目的值。
     */
    void createMap(Thread t, T firstValue) {
        t.inheritableThreadLocals = new ThreadLocalMap(this, firstValue);
    }
}
