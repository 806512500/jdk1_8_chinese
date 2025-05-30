
/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util;

/**
 * 该类提供了某些 {@link Queue} 操作的骨架实现。这些实现适用于基础实现不允许可空元素的情况。
 * 方法 {@link #add add}、{@link #remove remove} 和 {@link #element element} 分别基于
 * {@link #offer offer}、{@link #poll poll} 和 {@link #peek peek}，但通过抛出异常而不是通过
 * <tt>false</tt> 或 <tt>null</tt> 返回值来表示失败。
 *
 * <p>扩展此类的 <tt>Queue</tt> 实现必须至少定义一个不允许插入可空元素的方法 {@link Queue#offer}，
 * 以及方法 {@link Queue#peek}、{@link Queue#poll}、{@link Collection#size} 和
 * {@link Collection#iterator}。通常，还会覆盖其他方法。如果无法满足这些要求，请考虑扩展
 * {@link AbstractCollection}。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public abstract class AbstractQueue<E>
    extends AbstractCollection<E>
    implements Queue<E> {

    /**
     * 供子类使用的构造函数。
     */
    protected AbstractQueue() {
    }

    /**
     * 如果可以在不违反容量限制的情况下立即插入指定元素，则将其插入此队列，成功时返回 <tt>true</tt>，
     * 如果当前没有可用空间，则抛出 <tt>IllegalStateException</tt>。
     *
     * <p>此实现如果 <tt>offer</tt> 成功，则返回 <tt>true</tt>，否则抛出 <tt>IllegalStateException</tt>。
     *
     * @param e 要添加的元素
     * @return <tt>true</tt>（如 {@link Collection#add} 所指定）
     * @throws IllegalStateException 如果由于容量限制无法在此时添加元素
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此队列
     * @throws NullPointerException 如果指定元素为 null 且此队列不允许 null 元素
     * @throws IllegalArgumentException 如果此元素的某些属性阻止其被添加到此队列
     */
    public boolean add(E e) {
        if (offer(e))
            return true;
        else
            throw new IllegalStateException("队列已满");
    }

    /**
     * 检索并移除此队列的头部。此方法与 {@link #poll poll} 的区别在于，如果此队列为空，则抛出异常。
     *
     * <p>此实现返回 <tt>poll</tt> 的结果，除非队列为空。
     *
     * @return 此队列的头部
     * @throws NoSuchElementException 如果此队列为空
     */
    public E remove() {
        E x = poll();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException();
    }

    /**
     * 检索但不移除此队列的头部。此方法与 {@link #peek peek} 的区别在于，如果此队列为空，则抛出异常。
     *
     * <p>此实现返回 <tt>peek</tt> 的结果，除非队列为空。
     *
     * @return 此队列的头部
     * @throws NoSuchElementException 如果此队列为空
     */
    public E element() {
        E x = peek();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException();
    }

    /**
     * 从此队列中移除所有元素。此方法返回后，队列将为空。
     *
     * <p>此实现反复调用 {@link #poll poll}，直到其返回 <tt>null</tt>。
     */
    public void clear() {
        while (poll() != null)
            ;
    }

    /**
     * 将指定集合中的所有元素添加到此队列。尝试将队列添加到自身会导致 <tt>IllegalArgumentException</tt>。
     * 此外，如果在操作进行过程中修改了指定的集合，则此操作的行为是未定义的。
     *
     * <p>此实现遍历指定的集合，并将迭代器返回的每个元素依次添加到此队列。在尝试添加元素时（特别是
     * <tt>null</tt> 元素）遇到的运行时异常可能会导致在抛出相关异常时只有部分元素成功添加。
     *
     * @param c 包含要添加到此队列的元素的集合
     * @return 如果此队列因调用而发生更改，则返回 <tt>true</tt>
     * @throws ClassCastException 如果指定集合中元素的类阻止其被添加到此队列
     * @throws NullPointerException 如果指定集合包含 null 元素且此队列不允许 null 元素，或者指定集合为 null
     * @throws IllegalArgumentException 如果指定集合中元素的某些属性阻止其被添加到此队列，或者指定集合是此队列
     * @throws IllegalStateException 如果由于插入限制无法在此时添加所有元素
     * @see #add(Object)
     */
    public boolean addAll(Collection<? extends E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        boolean modified = false;
        for (E e : c)
            if (add(e))
                modified = true;
        return modified;
    }

}
