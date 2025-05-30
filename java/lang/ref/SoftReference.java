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
 * 软引用对象，这些对象在内存需求时由垃圾收集器自行决定清除。软引用通常用于实现内存敏感的缓存。
 *
 * <p> 假设垃圾收集器在某个时间点确定一个对象是<a href="package-summary.html#reachability">软可达的</a>。
 * 在那个时间点，它可以选择原子地清除所有指向该对象的软引用以及通过强引用链从该对象可达的其他软可达对象的所有软引用。
 * 同时或稍后，它将把那些新清除的软引用（如果注册了引用队列）放入队列中。
 *
 * <p> 在虚拟机抛出<code>OutOfMemoryError</code>之前，所有指向软可达对象的软引用都保证已被清除。
 * 否则，对一组指向不同对象的软引用的清除时间或顺序没有约束。然而，虚拟机实现应鼓励不清理最近创建或最近使用的软引用。
 *
 * <p> 该类的直接实例可用于实现简单的缓存；该类或派生子类也可用于更大数据结构中实现更复杂的缓存。
 * 只要软引用的引用对象是强可达的，即实际在使用中，软引用就不会被清除。因此，复杂的缓存可以通过保持对最近使用的条目的强引用，
 * 防止这些条目被丢弃，而让剩余的条目由垃圾收集器自行决定丢弃。
 *
 * @author   Mark Reinhold
 * @since    1.2
 */

public class SoftReference<T> extends Reference<T> {

    /**
     * 时间戳时钟，由垃圾收集器更新
     */
    static private long clock;

    /**
     * 每次调用get方法时更新的时间戳。虚拟机可以选择使用此字段来选择要清除的软引用，但不是必须的。
     */
    private long timestamp;

    /**
     * 创建一个新的软引用，该引用指向给定的对象。新引用未注册到任何队列。
     *
     * @param referent 新软引用将指向的对象
     */
    public SoftReference(T referent) {
        super(referent);
        this.timestamp = clock;
    }

    /**
     * 创建一个新的软引用，该引用指向给定的对象并注册到给定的队列。
     *
     * @param referent 新软引用将指向的对象
     * @param q 要注册引用的队列，或<tt>null</tt>表示不需要注册
     *
     */
    public SoftReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
        this.timestamp = clock;
    }

    /**
     * 返回此引用对象的引用对象。如果此引用对象已被程序或垃圾收集器清除，则此方法返回<code>null</code>。
     *
     * @return   此引用指向的对象，或<code>null</code>表示此引用对象已被清除
     */
    public T get() {
        T o = super.get();
        if (o != null && this.timestamp != clock)
            this.timestamp = clock;
        return o;
    }

}
