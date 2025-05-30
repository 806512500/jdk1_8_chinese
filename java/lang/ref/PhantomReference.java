/*
 * Copyright (c) 1997, 2015, Oracle and/or its affiliates. All rights reserved.
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
 * 幽灵引用对象，在收集器确定其引用对象可能被回收后入队。幽灵引用通常用于安排对象的后处理清理操作。
 *
 * <p> 假设垃圾收集器在某个时间点确定一个对象是<a href="package-summary.html#reachability">
 * 幽灵可达</a>。在那个时间点，它将原子地清除对该对象的所有幽灵引用以及从该对象可达的任何其他幽灵可达对象的所有幽灵引用。同时或稍后，它将把那些新清除的幽灵引用入队，这些引用已注册到引用队列。
 *
 * <p> 为了确保可回收对象保持可回收状态，幽灵引用的引用对象不能被检索：幽灵引用的<code>get</code>方法始终返回<code>null</code>。
 *
 * @author   Mark Reinhold
 * @since    1.2
 */

public class PhantomReference<T> extends Reference<T> {

    /**
     * 返回此引用对象的引用对象。由于幽灵引用的引用对象总是不可访问的，此方法始终返回<code>null</code>。
     *
     * @return  <code>null</code>
     */
    public T get() {
        return null;
    }

    /**
     * 创建一个新的幽灵引用，该引用引用给定对象并注册到给定队列。
     *
     * <p> 可以创建一个队列为<tt>null</tt>的幽灵引用，但这样的引用完全无用：它的<tt>get</tt>方法始终返回{@code null}，并且由于它没有队列，它永远不会被入队。
     *
     * @param referent 新的幽灵引用将引用的对象
     * @param q 引用要注册的队列，或<tt>null</tt>表示不需要注册
     */
    public PhantomReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
    }

}
