/*
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.ref;


/**
 * 弱引用对象，它们不会阻止其引用对象被声明为可终结、终结和回收。弱引用最常用于实现规范化映射。
 *
 * <p> 假设垃圾收集器在某个时间点确定一个对象是<a href="package-summary.html#reachability">弱可到达的</a>。在那个时间点，它将原子地清除所有对该对象的弱引用以及所有其他弱可到达对象的弱引用，这些对象通过强引用和软引用链从该对象可达。同时，它将所有先前的弱可到达对象声明为可终结。同时或稍后，它将那些新清除的弱引用（如果注册了引用队列）入队。
 *
 * @author   Mark Reinhold
 * @since    1.2
 */

public class WeakReference<T> extends Reference<T> {

    /**
     * 创建一个新的弱引用，该引用指向给定的对象。新的引用未注册到任何队列。
     *
     * @param referent 新的弱引用将引用的对象
     */
    public WeakReference(T referent) {
        super(referent);
    }

    /**
     * 创建一个新的弱引用，该引用指向给定的对象并注册到给定的队列。
     *
     * @param referent 新的弱引用将引用的对象
     * @param q 引用要注册的队列，或 <tt>null</tt> 如果不需要注册
     */
    public WeakReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
    }

}
