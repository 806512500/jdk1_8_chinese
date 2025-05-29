/*
 * Copyright (c) 1994, 2005, Oracle and/or its affiliates. All rights reserved.
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
 * 实现 Enumeration 接口的对象生成一系列元素，一次一个。对 <code>nextElement</code> 方法的连续调用返回该系列的连续元素。
 * <p>
 * 例如，打印 <tt>Vector&lt;E&gt;</tt> <i>v</i> 的所有元素：
 * <pre>
 *   for (Enumeration&lt;E&gt; e = v.elements(); e.hasMoreElements();)
 *       System.out.println(e.nextElement());</pre>
 * <p>
 * 提供了枚举向量的元素、哈希表的键和哈希表中的值的方法。枚举还用于指定 <code>SequenceInputStream</code> 的输入流。
 * <p>
 * 注意：此接口的功能已被 Iterator 接口复制。此外，Iterator 添加了一个可选的 remove 操作，并且方法名更短。新实现应考虑优先使用 Iterator 而不是 Enumeration。
 *
 * @see     java.util.Iterator
 * @see     java.io.SequenceInputStream
 * @see     java.util.Enumeration#nextElement()
 * @see     java.util.Hashtable
 * @see     java.util.Hashtable#elements()
 * @see     java.util.Hashtable#keys()
 * @see     java.util.Vector
 * @see     java.util.Vector#elements()
 *
 * @author  Lee Boynton
 * @since   JDK1.0
 */
public interface Enumeration<E> {
    /**
     * 测试此枚举是否包含更多元素。
     *
     * @return  如果此枚举对象至少包含一个更多元素，则返回 <code>true</code>；
     *          否则返回 <code>false</code>。
     */
    boolean hasMoreElements();

    /**
     * 如果此枚举对象至少包含一个更多元素，则返回此枚举的下一个元素。
     *
     * @return     此枚举的下一个元素。
     * @exception  NoSuchElementException  如果没有更多元素存在。
     */
    E nextElement();
}
