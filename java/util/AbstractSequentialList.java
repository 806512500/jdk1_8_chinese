
/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
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
 * 该类为 <tt>List</tt> 接口提供了一个骨架实现，以最小化实现该接口所需的努力，特别是当数据存储是“顺序访问”（如链表）时。对于随机访问数据（如数组），应优先使用 <tt>AbstractList</tt> 类。<p>
 *
 * 该类与 <tt>AbstractList</tt> 类相反，它在列表迭代器的基础上实现了“随机访问”方法（<tt>get(int index)</tt>、<tt>set(int index, E element)</tt>、<tt>add(int index, E element)</tt> 和 <tt>remove(int index)</tt>），而不是反过来。<p>
 *
 * 要实现一个列表，程序员只需扩展此类并提供 <tt>listIterator</tt> 和 <tt>size</tt> 方法的实现。对于不可修改的列表，程序员只需实现列表迭代器的 <tt>hasNext</tt>、<tt>next</tt>、<tt>hasPrevious</tt>、<tt>previous</tt> 和 <tt>index</tt> 方法。<p>
 *
 * 对于可修改的列表，程序员还应实现列表迭代器的 <tt>set</tt> 方法。对于可变大小的列表，程序员还应实现列表迭代器的 <tt>remove</tt> 和 <tt>add</tt> 方法。<p>
 *
 * 程序员通常应提供一个无参构造函数和一个集合构造函数，以符合 <tt>Collection</tt> 接口规范中的建议。<p>
 *
 * 该类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see Collection
 * @see List
 * @see AbstractList
 * @see AbstractCollection
 * @since 1.2
 */

public abstract class AbstractSequentialList<E> extends AbstractList<E> {
    /**
     * 唯一的构造函数。通常由子类构造函数隐式调用。
     */
    protected AbstractSequentialList() {
    }

    /**
     * 返回此列表中指定位置的元素。
     *
     * <p>此实现首先使用 <tt>listIterator(index)</tt> 获取指向索引元素的列表迭代器。然后，它使用 <tt>ListIterator.next</tt> 获取元素并返回。
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        try {
            return listIterator(index).next();
        } catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
    }

    /**
     * 用指定的元素替换此列表中指定位置的元素（可选操作）。
     *
     * <p>此实现首先使用 <tt>listIterator(index)</tt> 获取指向索引元素的列表迭代器。然后，它使用 <tt>ListIterator.next</tt> 获取当前元素，并使用 <tt>ListIterator.set</tt> 替换它。
     *
     * <p>请注意，如果列表迭代器未实现 <tt>set</tt> 操作，此实现将抛出 <tt>UnsupportedOperationException</tt>。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public E set(int index, E element) {
        try {
            ListIterator<E> e = listIterator(index);
            E oldVal = e.next();
            e.set(element);
            return oldVal;
        } catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
    }

    /**
     * 在此列表的指定位置插入指定的元素（可选操作）。将当前位于该位置的元素（如果有）及其后续元素向右移动（索引加一）。
     *
     * <p>此实现首先使用 <tt>listIterator(index)</tt> 获取指向索引元素的列表迭代器。然后，它使用 <tt>ListIterator.add</tt> 插入指定的元素。
     *
     * <p>请注意，如果列表迭代器未实现 <tt>add</tt> 操作，此实现将抛出 <tt>UnsupportedOperationException</tt>。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public void add(int index, E element) {
        try {
            listIterator(index).add(element);
        } catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
    }


                /**
     * 从此列表中移除指定位置的元素（可选操作）。将任何后续元素向左移动（减一其索引）。返回从列表中移除的元素。
     *
     * <p>此实现首先获取一个指向索引元素的列表迭代器（使用<tt>listIterator(index)</tt>）。然后，使用<tt>ListIterator.remove</tt>移除该元素。
     *
     * <p>请注意，如果列表迭代器未实现<tt>remove</tt>操作，此实现将抛出<tt>UnsupportedOperationException</tt>。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public E remove(int index) {
        try {
            ListIterator<E> e = listIterator(index);
            E outCast = e.next();
            e.remove();
            return outCast;
        } catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
    }


    // 批量操作

    /**
     * 将指定集合中的所有元素插入到此列表的指定位置（可选操作）。将当前位于该位置的元素（如果有）和任何后续元素向右移动（增加其索引）。新元素将按照指定集合的迭代器返回的顺序出现在此列表中。如果在操作进行过程中修改了指定的集合，此操作的行为是未定义的。（请注意，如果指定的集合是此列表，并且它非空，则会发生这种情况。）
     *
     * <p>此实现获取指定集合的迭代器和此列表的指向索引元素的列表迭代器（使用<tt>listIterator(index)</tt>）。然后，它遍历指定的集合，使用<tt>ListIterator.add</tt>和<tt>ListIterator.next</tt>（跳过添加的元素）将从迭代器中获取的元素逐个插入到此列表中。
     *
     * <p>请注意，如果<tt>listIterator</tt>方法返回的列表迭代器未实现<tt>add</tt>操作，此实现将抛出<tt>UnsupportedOperationException</tt>。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        try {
            boolean modified = false;
            ListIterator<E> e1 = listIterator(index);
            Iterator<? extends E> e2 = c.iterator();
            while (e2.hasNext()) {
                e1.add(e2.next());
                modified = true;
            }
            return modified;
        } catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
    }


    // 迭代器

    /**
     * 返回一个迭代器，遍历此列表中的元素（按正确顺序）。<p>
     *
     * 此实现仅返回一个列表迭代器。
     *
     * @return 一个迭代器，遍历此列表中的元素（按正确顺序）
     */
    public Iterator<E> iterator() {
        return listIterator();
    }

    /**
     * 返回一个列表迭代器，遍历此列表中的元素（按正确顺序）。
     *
     * @param  index 列表迭代器返回的第一个元素的索引（通过调用<code>next</code>方法）
     * @return 一个列表迭代器，遍历此列表中的元素（按正确顺序）
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public abstract ListIterator<E> listIterator(int index);
}
