
/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * 一个用于列表的迭代器，允许程序员双向遍历列表、在迭代过程中修改列表，并获取迭代器在列表中的当前位置。一个 {@code ListIterator}
 * 没有当前元素；其 <I>光标位置</I> 始终位于调用 {@code previous()} 会返回的元素和调用 {@code next()} 会返回的元素之间。
 * 长度为 {@code n} 的列表的迭代器有 {@code n+1} 个可能的光标位置，如下图所示的尖括号 ({@code ^})：
 * <PRE>
 *                      元素(0)   元素(1)   元素(2)   ... 元素(n-1)
 * 光标位置:  ^            ^            ^            ^                  ^
 * </PRE>
 * 注意，{@link #remove} 和 {@link #set(Object)} 方法并不是根据光标位置定义的；它们被定义为操作调用 {@link #next} 或
 * {@link #previous()} 返回的最后一个元素。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
 *
 * @author  Josh Bloch
 * @see Collection
 * @see List
 * @see Iterator
 * @see Enumeration
 * @see List#listIterator()
 * @since   1.2
 */
public interface ListIterator<E> extends Iterator<E> {
    // 查询操作

    /**
     * 如果此列表迭代器在正向遍历列表时有更多元素，则返回 {@code true}。（换句话说，如果 {@link #next} 会返回一个元素而不是抛出异常，则返回 {@code true}。）
     *
     * @return 如果列表迭代器在正向遍历列表时有更多元素，则返回 {@code true}
     */
    boolean hasNext();

    /**
     * 返回列表中的下一个元素并向前移动光标位置。此方法可以重复调用以遍历列表，或与 {@link #previous} 调用混合使用以来回移动。
     * （注意，交替调用 {@code next} 和 {@code previous} 会重复返回同一个元素。）
     *
     * @return 列表中的下一个元素
     * @throws NoSuchElementException 如果迭代没有下一个元素
     */
    E next();

    /**
     * 如果此列表迭代器在反向遍历列表时有更多元素，则返回 {@code true}。（换句话说，如果 {@link #previous} 会返回一个元素而不是抛出异常，则返回 {@code true}。）
     *
     * @return 如果列表迭代器在反向遍历列表时有更多元素，则返回 {@code true}
     */
    boolean hasPrevious();

    /**
     * 返回列表中的前一个元素并向后移动光标位置。此方法可以重复调用以反向遍历列表，或与 {@link #next} 调用混合使用以来回移动。
     * （注意，交替调用 {@code next} 和 {@code previous} 会重复返回同一个元素。）
     *
     * @return 列表中的前一个元素
     * @throws NoSuchElementException 如果迭代没有前一个元素
     */
    E previous();

    /**
     * 返回后续调用 {@link #next} 会返回的元素的索引。（如果列表迭代器位于列表末尾，则返回列表大小。）
     *
     * @return 后续调用 {@code next} 会返回的元素的索引，或如果列表迭代器位于列表末尾，则返回列表大小
     */
    int nextIndex();

    /**
     * 返回后续调用 {@link #previous} 会返回的元素的索引。（如果列表迭代器位于列表开头，则返回 -1。）
     *
     * @return 后续调用 {@code previous} 会返回的元素的索引，或如果列表迭代器位于列表开头，则返回 -1
     */
    int previousIndex();

    // 修改操作

    /**
     * 从列表中移除由 {@link #next} 或 {@link #previous} 返回的最后一个元素（可选操作）。此调用只能在每次调用 {@code next} 或 {@code previous} 后调用一次。
     * 只有在调用 {@code next} 或 {@code previous} 后未调用 {@link #add} 时，才能调用此方法。
     *
     * @throws UnsupportedOperationException 如果此列表迭代器不支持 {@code remove} 操作
     * @throws IllegalStateException 如果未调用 {@code next} 或 {@code previous}，或在调用 {@code next} 或 {@code previous} 后调用了 {@code remove} 或 {@code add}
     */
    void remove();


                /**
     * 用指定的元素替换由 {@link #next} 或 {@link #previous} 返回的最后一个元素（可选操作）。
     * 仅当在上次调用 {@code next} 或 {@code previous} 之后，没有调用 {@link #remove} 或 {@link #add} 方法时，才能调用此方法。
     *
     * @param e 用于替换 {@code next} 或 {@code previous} 返回的最后一个元素的元素
     * @throws UnsupportedOperationException 如果此列表迭代器不支持 {@code set} 操作
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此列表中
     * @throws IllegalArgumentException 如果指定元素的某些方面阻止其被添加到此列表中
     * @throws IllegalStateException 如果没有调用 {@code next} 或 {@code previous}，或者在上次调用 {@code next} 或 {@code previous} 之后调用了 {@code remove} 或 {@code add}
     */
    void set(E e);

    /**
     * 将指定的元素插入列表中（可选操作）。
     * 该元素立即插入到 {@link #next} 将返回的元素之前（如果有），并且在 {@link #previous} 将返回的元素之后（如果有）。
     * （如果列表中没有元素，新元素将成为列表中唯一的元素。）新元素在隐式光标之前插入：后续调用 {@code next} 不会受到影响，而后续调用 {@code previous} 将返回新元素。
     * （此调用将使 {@code nextIndex} 或 {@code previousIndex} 返回的值增加一。）
     *
     * @param e 要插入的元素
     * @throws UnsupportedOperationException 如果此列表迭代器不支持 {@code add} 方法
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此列表中
     * @throws IllegalArgumentException 如果此元素的某些方面阻止其被添加到此列表中
     */
    void add(E e);
}
