/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * <code>Runnable</code> 接口应由任何希望其实例由线程执行的类实现。该类必须定义一个无参数的方法，称为 <code>run</code>。
 * <p>
 * 该接口旨在为希望在活动时执行代码的对象提供一个通用协议。例如，<code>Runnable</code> 由类 <code>Thread</code> 实现。
 * 活动仅意味着线程已启动且尚未停止。
 * <p>
 * 此外，<code>Runnable</code> 提供了一种类在不继承 <code>Thread</code> 的情况下保持活动的方法。实现 <code>Runnable</code> 的类
 * 可以通过实例化一个 <code>Thread</code> 实例并将其自身作为目标传递来运行。在大多数情况下，如果仅计划覆盖 <code>run()</code>
 * 方法而不覆盖其他 <code>Thread</code> 方法，则应使用 <code>Runnable</code> 接口。这一点很重要，因为除非程序员打算修改或增强类的
 * 核心行为，否则不应继承类。
 *
 * @author  Arthur van Hoff
 * @see     java.lang.Thread
 * @see     java.util.concurrent.Callable
 * @since   JDK1.0
 */
@FunctionalInterface
public interface Runnable {
    /**
     * 当实现 <code>Runnable</code> 接口的对象用于创建线程时，启动该线程会导致该对象的 <code>run</code> 方法在单独执行的线程中被调用。
     * <p>
     * <code>run</code> 方法的一般契约是它可以采取任何行动。
     *
     * @see     java.lang.Thread#run()
     */
    public abstract void run();
}
