
/*
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
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
 * EnumSet 的私有实现类，用于“常规大小”的枚举类型
 * （即，具有 64 个或更少枚举常量的类型）。
 *
 * @author Josh Bloch
 * @since 1.5
 * @serial exclude
 */
class RegularEnumSet<E extends Enum<E>> extends EnumSet<E> {
    private static final long serialVersionUID = 3411599620347842686L;
    /**
     * 该集合的位向量表示。2^k 位表示 universe[k] 是否存在于此集合中。
     */
    private long elements = 0L;

    RegularEnumSet(Class<E> elementType, Enum<?>[] universe) {
        super(elementType, universe);
    }

    void addRange(E from, E to) {
        elements = (-1L >>>  (from.ordinal() - to.ordinal() - 1)) << from.ordinal();
    }

    void addAll() {
        if (universe.length != 0)
            elements = -1L >>> -universe.length;
    }

    void complement() {
        if (universe.length != 0) {
            elements = ~elements;
            elements &= -1L >>> -universe.length;  // 掩码未使用的位
        }
    }

    /**
     * 返回一个迭代器，遍历此集合中的元素。迭代器按元素的<i>自然顺序</i>（即枚举常量声明的顺序）遍历。
     * 返回的迭代器是一个“快照”迭代器，永远不会抛出 {@link
     * ConcurrentModificationException}；元素将按照调用此方法时的状态进行遍历。
     *
     * @return 一个迭代器，遍历此集合中的元素
     */
    public Iterator<E> iterator() {
        return new EnumSetIterator<>();
    }

    private class EnumSetIterator<E extends Enum<E>> implements Iterator<E> {
        /**
         * 一个位向量，表示此迭代器尚未返回的集合中的元素。
         */
        long unseen;

        /**
         * 表示此迭代器已返回但未移除的最后一个元素的位，如果不存在这样的元素，则为零。
         */
        long lastReturned = 0;

        EnumSetIterator() {
            unseen = elements;
        }

        public boolean hasNext() {
            return unseen != 0;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (unseen == 0)
                throw new NoSuchElementException();
            lastReturned = unseen & -unseen;
            unseen -= lastReturned;
            return (E) universe[Long.numberOfTrailingZeros(lastReturned)];
        }

        public void remove() {
            if (lastReturned == 0)
                throw new IllegalStateException();
            elements &= ~lastReturned;
            lastReturned = 0;
        }
    }

    /**
     * 返回此集合中的元素数量。
     *
     * @return 此集合中的元素数量
     */
    public int size() {
        return Long.bitCount(elements);
    }

    /**
     * 如果此集合不包含任何元素，则返回 <tt>true</tt>。
     *
     * @return 如果此集合不包含任何元素，则返回 <tt>true</tt>
     */
    public boolean isEmpty() {
        return elements == 0;
    }

    /**
     * 如果此集合包含指定的元素，则返回 <tt>true</tt>。
     *
     * @param e 要检查是否包含在此集合中的元素
     * @return 如果此集合包含指定的元素，则返回 <tt>true</tt>
     */
    public boolean contains(Object e) {
        if (e == null)
            return false;
        Class<?> eClass = e.getClass();
        if (eClass != elementType && eClass.getSuperclass() != elementType)
            return false;

        return (elements & (1L << ((Enum<?>)e).ordinal())) != 0;
    }

    // 修改操作

    /**
     * 如果指定的元素尚未存在，则将其添加到此集合中。
     *
     * @param e 要添加到此集合中的元素
     * @return 如果调用此方法后集合发生更改，则返回 <tt>true</tt>
     *
     * @throws NullPointerException 如果 <tt>e</tt> 为 null
     */
    public boolean add(E e) {
        typeCheck(e);

        long oldElements = elements;
        elements |= (1L << ((Enum<?>)e).ordinal());
        return elements != oldElements;
    }

    /**
     * 如果指定的元素存在，则从此集合中移除。
     *
     * @param e 要从此集合中移除的元素（如果存在）
     * @return 如果集合包含指定的元素，则返回 <tt>true</tt>
     */
    public boolean remove(Object e) {
        if (e == null)
            return false;
        Class<?> eClass = e.getClass();
        if (eClass != elementType && eClass.getSuperclass() != elementType)
            return false;

        long oldElements = elements;
        elements &= ~(1L << ((Enum<?>)e).ordinal());
        return elements != oldElements;
    }

    // 批量操作

    /**
     * 如果此集合包含指定集合中的所有元素，则返回 <tt>true</tt>。
     *
     * @param c 要检查是否包含在此集合中的集合
     * @return 如果此集合包含指定集合中的所有元素，则返回 <tt>true</tt>
     * @throws NullPointerException 如果指定的集合为 null
     */
    public boolean containsAll(Collection<?> c) {
        if (!(c instanceof RegularEnumSet))
            return super.containsAll(c);


                    RegularEnumSet<?> es = (RegularEnumSet<?>)c;
        if (es.elementType != elementType)
            return es.isEmpty();

        return (es.elements & ~elements) == 0;
    }

    /**
     * 将指定集合中的所有元素添加到此集合中。
     *
     * @param c 要添加到此集合中的集合
     * @return 如果此集合因调用而发生更改，则返回 <tt>true</tt>
     * @throws NullPointerException 如果指定的集合或其任何元素为 null
     */
    public boolean addAll(Collection<? extends E> c) {
        if (!(c instanceof RegularEnumSet))
            return super.addAll(c);

        RegularEnumSet<?> es = (RegularEnumSet<?>)c;
        if (es.elementType != elementType) {
            if (es.isEmpty())
                return false;
            else
                throw new ClassCastException(
                    es.elementType + " != " + elementType);
        }

        long oldElements = elements;
        elements |= es.elements;
        return elements != oldElements;
    }

    /**
     * 从此集合中删除包含在指定集合中的所有元素。
     *
     * @param c 要从此集合中删除的元素
     * @return 如果此集合因调用而发生更改，则返回 <tt>true</tt>
     * @throws NullPointerException 如果指定的集合为 null
     */
    public boolean removeAll(Collection<?> c) {
        if (!(c instanceof RegularEnumSet))
            return super.removeAll(c);

        RegularEnumSet<?> es = (RegularEnumSet<?>)c;
        if (es.elementType != elementType)
            return false;

        long oldElements = elements;
        elements &= ~es.elements;
        return elements != oldElements;
    }

    /**
     * 仅保留此集合中包含在指定集合中的元素。
     *
     * @param c 要在此集合中保留的元素
     * @return 如果此集合因调用而发生更改，则返回 <tt>true</tt>
     * @throws NullPointerException 如果指定的集合为 null
     */
    public boolean retainAll(Collection<?> c) {
        if (!(c instanceof RegularEnumSet))
            return super.retainAll(c);

        RegularEnumSet<?> es = (RegularEnumSet<?>)c;
        if (es.elementType != elementType) {
            boolean changed = (elements != 0);
            elements = 0;
            return changed;
        }

        long oldElements = elements;
        elements &= es.elements;
        return elements != oldElements;
    }

    /**
     * 从此集合中删除所有元素。
     */
    public void clear() {
        elements = 0;
    }

    /**
     * 将指定对象与此集合进行相等性比较。如果给定对象也是一个集合，两个集合的大小相同，并且给定集合的每个成员都包含在此集合中，则返回 <tt>true</tt>。
     *
     * @param o 要与此集合进行相等性比较的对象
     * @return 如果指定对象等于此集合，则返回 <tt>true</tt>
     */
    public boolean equals(Object o) {
        if (!(o instanceof RegularEnumSet))
            return super.equals(o);

        RegularEnumSet<?> es = (RegularEnumSet<?>)o;
        if (es.elementType != elementType)
            return elements == 0 && es.elements == 0;
        return es.elements == elements;
    }
}
