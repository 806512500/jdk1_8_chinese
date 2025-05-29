/*
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

/**
 * 标记接口，用于指示 <tt>List</tt> 实现支持快速（通常为常数时间）随机访问。此接口的主要目的是让通用算法能够改变其行为，以在应用于随机访问列表或顺序访问列表时提供良好的性能。
 *
 * <p>用于操作随机访问列表（如 <tt>ArrayList</tt>）的最佳算法在应用于顺序访问列表（如 <tt>LinkedList</tt>）时可能会产生二次行为。通用列表算法在应用可能导致性能低下的算法之前，应检查给定列表是否是此接口的 <tt>instanceof</tt>，并在必要时改变其行为以保证可接受的性能。
 *
 * <p>认识到随机访问和顺序访问之间的区别通常是模糊的。例如，某些 <tt>List</tt> 实现如果变得非常大，可能会提供渐近线性访问时间，但在实践中提供常数访问时间。这样的 <tt>List</tt> 实现通常应实现此接口。作为一个经验法则，如果对于该类的典型实例，此循环：
 * <pre>
 *     for (int i=0, n=list.size(); i &lt; n; i++)
 *         list.get(i);
 * </pre>
 * 运行速度比此循环快：
 * <pre>
 *     for (Iterator i=list.iterator(); i.hasNext(); )
 *         i.next();
 * </pre>
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.4
 */
public interface RandomAccess {
}
