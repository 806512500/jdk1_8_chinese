
/*
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
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
 * 该类提供了 {@link List} 接口的骨架实现，以最小化实现此接口所需的 effort，特别是当数据存储支持“随机访问”（如数组）时。对于顺序访问的数据（如链表），应优先使用 {@link AbstractSequentialList}。
 *
 * <p>为了实现一个不可修改的列表，程序员只需要扩展此类并提供 {@link #get(int)} 和 {@link List#size() size()} 方法的实现。
 *
 * <p>为了实现一个可修改的列表，程序员必须额外覆盖 {@link #set(int, Object) set(int, E)} 方法（否则会抛出一个 {@code UnsupportedOperationException}）。如果列表是可变大小的，程序员还必须覆盖 {@link #add(int, Object) add(int, E)} 和 {@link #remove(int)} 方法。
 *
 * <p>程序员通常应提供一个无参数构造函数和一个集合构造函数，以符合 {@link Collection} 接口规范中的建议。
 *
 * <p>与其它抽象集合实现不同，程序员不需要提供迭代器实现；迭代器和列表迭代器由此类实现，基于“随机访问”方法：
 * {@link #get(int)},
 * {@link #set(int, Object) set(int, E)},
 * {@link #add(int, Object) add(int, E)} 和
 * {@link #remove(int)}。
 *
 * <p>此类中每个非抽象方法的文档详细描述了其实现。如果实现的集合允许更高效的实现，可以覆盖这些方法。
 *
 * <p>该类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @since 1.2
 */

public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {
    /**
     * 唯一的构造函数。通常由子类构造函数隐式调用。
     */
    protected AbstractList() {
    }

    /**
     * 将指定的元素添加到此列表的末尾（可选操作）。
     *
     * <p>支持此操作的列表可能对可以添加到此列表的元素施加限制。特别是，某些列表将拒绝添加 null 元素，而其他列表将对可以添加的元素类型施加限制。列表类应在文档中明确指定对可以添加的元素的任何限制。
     *
     * <p>此实现调用 {@code add(size(), e)}。
     *
     * <p>请注意，除非覆盖了 {@link #add(int, Object) add(int, E)}，否则此实现将抛出一个 {@code UnsupportedOperationException}。
     *
     * @param e 要添加到此列表的元素
     * @return {@code true}（由 {@link Collection#add} 指定）
     * @throws UnsupportedOperationException 如果此列表不支持 {@code add} 操作
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此列表
     * @throws NullPointerException 如果指定的元素为 null 且此列表不允许 null 元素
     * @throws IllegalArgumentException 如果此元素的某些属性阻止其被添加到此列表
     */
    public boolean add(E e) {
        add(size(), e);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    abstract public E get(int index);

    /**
     * {@inheritDoc}
     *
     * <p>此实现总是抛出一个 {@code UnsupportedOperationException}。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现总是抛出一个 {@code UnsupportedOperationException}。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现总是抛出一个 {@code UnsupportedOperationException}。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }


    // 搜索操作

    /**
     * {@inheritDoc}
     *
     * <p>此实现首先获取一个列表迭代器（使用 {@code listIterator()}）。然后，它遍历列表，直到找到指定的元素或到达列表的末尾。
     *
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public int indexOf(Object o) {
        ListIterator<E> it = listIterator();
        if (o==null) {
            while (it.hasNext())
                if (it.next()==null)
                    return it.previousIndex();
        } else {
            while (it.hasNext())
                if (o.equals(it.next()))
                    return it.previousIndex();
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现首先获取一个指向列表末尾的列表迭代器（使用 {@code listIterator(size())}）。然后，它从后向前遍历列表，直到找到指定的元素或到达列表的开头。
     *
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public int lastIndexOf(Object o) {
        ListIterator<E> it = listIterator(size());
        if (o==null) {
            while (it.hasPrevious())
                if (it.previous()==null)
                    return it.nextIndex();
        } else {
            while (it.hasPrevious())
                if (o.equals(it.previous()))
                    return it.nextIndex();
        }
        return -1;
    }


    // 批量操作

    /**
     * 从此列表中移除所有元素（可选操作）。调用此方法后，列表将为空。
     *
     * <p>此实现调用 {@code removeRange(0, size())}。
     *
     * <p>请注意，除非覆盖了 {@code remove(int index)} 或 {@code removeRange(int fromIndex, int toIndex)}，此实现将抛出 {@code UnsupportedOperationException}。
     *
     * @throws UnsupportedOperationException 如果此列表不支持 {@code clear} 操作
     */
    public void clear() {
        removeRange(0, size());
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现获取指定集合的迭代器，并遍历该迭代器，使用 {@code add(int, E)} 将从迭代器中获得的元素逐个插入到此列表的适当位置。许多实现会为了效率而覆盖此方法。
     *
     * <p>请注意，除非覆盖了 {@link #add(int, Object) add(int, E)}，此实现将抛出 {@code UnsupportedOperationException}。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        boolean modified = false;
        for (E e : c) {
            add(index++, e);
            modified = true;
        }
        return modified;
    }


    // 迭代器

    /**
     * 返回一个按适当顺序遍历此列表元素的迭代器。
     *
     * <p>此实现返回一个简单的迭代器接口实现，依赖于支持列表的 {@code size()}、{@code get(int)} 和 {@code remove(int)} 方法。
     *
     * <p>请注意，除非覆盖了列表的 {@code remove(int)} 方法，此实现返回的迭代器在其 {@code remove} 方法中将抛出 {@link UnsupportedOperationException}。
     *
     * <p>此实现可以在发生并发修改时抛出运行时异常，如 (protected) {@link #modCount} 字段的说明中所述。
     *
     * @return 按适当顺序遍历此列表元素的迭代器
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现返回 {@code listIterator(0)}。
     *
     * @see #listIterator(int)
     */
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现返回一个简单的 {@code ListIterator} 接口实现，该实现扩展了 {@code iterator()} 方法返回的 {@code Iterator} 接口实现。此 {@code ListIterator} 实现依赖于支持列表的 {@code get(int)}、{@code set(int, E)}、{@code add(int, E)} 和 {@code remove(int)} 方法。
     *
     * <p>请注意，除非覆盖了列表的 {@code remove(int)}、{@code set(int, E)} 和 {@code add(int, E)} 方法，此实现返回的列表迭代器在其 {@code remove}、{@code set} 和 {@code add} 方法中将抛出 {@link UnsupportedOperationException}。
     *
     * <p>此实现可以在发生并发修改时抛出运行时异常，如 (protected) {@link #modCount} 字段的说明中所述。
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public ListIterator<E> listIterator(final int index) {
        rangeCheckForAdd(index);

        return new ListItr(index);
    }

    private class Itr implements Iterator<E> {
        /**
         * 下一次调用 next 时要返回的元素的索引。
         */
        int cursor = 0;

        /**
         * 最近一次调用 next 或 previous 返回的元素的索引。如果通过调用 remove 删除了此元素，则重置为 -1。
         */
        int lastRet = -1;

        /**
         * 迭代器认为支持的 List 应该具有的 modCount 值。如果这个期望被违反，迭代器检测到了并发修改。
         */
        int expectedModCount = modCount;


                    public boolean hasNext() {
            return cursor != size();
        }

        public E next() {
            checkForComodification();
            try {
                int i = cursor;
                E next = get(i);
                lastRet = i;
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                AbstractList.this.remove(lastRet);
                if (lastRet < cursor)
                    cursor--;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public E previous() {
            checkForComodification();
            try {
                int i = cursor - 1;
                E previous = get(i);
                lastRet = cursor = i;
                return previous;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                AbstractList.this.set(lastRet, e);
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                AbstractList.this.add(i, e);
                lastRet = -1;
                cursor = i + 1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现返回一个子类化 {@code AbstractList} 的列表。子类在私有字段中存储子列表在支持列表中的偏移量、子列表的大小（可以在其生命周期内更改）以及支持列表的预期 {@code modCount} 值。子类有两个变体，其中一个实现 {@code RandomAccess}。如果此列表实现了 {@code RandomAccess}，则返回的列表将是实现 {@code RandomAccess} 的子类的实例。
     *
     * <p>子类的 {@code set(int, E)}、{@code get(int)}、{@code add(int, E)}、{@code remove(int)}、{@code addAll(int, Collection)} 和 {@code removeRange(int, int)} 方法都委托给支持抽象列表的相应方法，在进行边界检查并调整偏移量后。{@code addAll(Collection c)} 方法仅返回 {@code addAll(size, c)}。
     *
     * <p>{@code listIterator(int)} 方法返回一个“包装对象”，该对象覆盖支持列表上的列表迭代器，该迭代器是使用支持列表上的相应方法创建的。{@code iterator} 方法仅返回 {@code listIterator()}，而 {@code size} 方法仅返回子类的 {@code size} 字段。
     *
     * <p>所有方法首先检查支持列表的实际 {@code modCount} 是否等于其预期值，如果不是，则抛出 {@code ConcurrentModificationException}。
     *
     * @throws IndexOutOfBoundsException 如果端点索引值超出范围 {@code (fromIndex < 0 || toIndex > size)}
     * @throws IllegalArgumentException 如果端点索引顺序错误 {@code (fromIndex > toIndex)}
     */
    public List<E> subList(int fromIndex, int toIndex) {
        return (this instanceof RandomAccess ?
                new RandomAccessSubList<>(this, fromIndex, toIndex) :
                new SubList<>(this, fromIndex, toIndex));
    }

    // 比较和哈希

    /**
     * 将指定对象与此列表进行相等性比较。仅当指定对象也是一个列表，两个列表具有相同的大小，并且两个列表中所有对应的元素对都是 <i>相等的</i> 时，才返回 {@code true}。 (两个元素 {@code e1} 和 {@code e2} 是 <i>相等的</i>，如果 {@code (e1==null ? e2==null : e1.equals(e2))}。) 换句话说，如果两个列表包含相同顺序的相同元素，则定义它们相等。<p>
     *
     * 此实现首先检查指定对象是否是此列表。如果是，返回 {@code true}；如果不是，检查指定对象是否是列表。如果不是，返回 {@code false}；如果是，迭代两个列表，比较对应的元素对。如果任何比较返回 {@code false}，此方法返回 {@code false}。如果任何一个迭代器在另一个之前耗尽元素，则返回 {@code false}（因为列表长度不等）；否则，当迭代完成时返回 {@code true}。
     *
     * @param o 要与此列表进行相等性比较的对象
     * @return 如果指定对象等于此列表，则返回 {@code true}
     */
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof List))
            return false;


                    ListIterator<E> e1 = listIterator();
        ListIterator<?> e2 = ((List<?>) o).listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            E o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1==null ? o2==null : o1.equals(o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    /**
     * 返回此列表的哈希码值。
     *
     * <p>此实现使用了在 {@link List#hashCode} 方法文档中定义的列表哈希函数的代码。
     *
     * @return 此列表的哈希码值
     */
    public int hashCode() {
        int hashCode = 1;
        for (E e : this)
            hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
        return hashCode;
    }

    /**
     * 从此列表中删除索引在 {@code fromIndex}（包括）和 {@code toIndex}（不包括）之间的所有元素。
     * 将后续元素向左移动（减少它们的索引）。
     * 此调用将列表缩短 {@code (toIndex - fromIndex)} 个元素。
     * （如果 {@code toIndex==fromIndex}，此操作没有效果。）
     *
     * <p>此方法由此列表及其子列表的 {@code clear} 操作调用。
     * 重写此方法以利用列表实现的内部结构可以 <i>显著</i> 提高此列表及其子列表的 {@code clear} 操作的性能。
     *
     * <p>此实现获取一个定位在 {@code fromIndex} 之前的列表迭代器，并反复调用 {@code ListIterator.next}
     * 然后调用 {@code ListIterator.remove}，直到整个范围都被删除。<b>注意：如果 {@code ListIterator.remove}
     * 需要线性时间，此实现需要二次时间。</b>
     *
     * @param fromIndex 要删除的第一个元素的索引
     * @param toIndex 要删除的最后一个元素之后的索引
     */
    protected void removeRange(int fromIndex, int toIndex) {
        ListIterator<E> it = listIterator(fromIndex);
        for (int i=0, n=toIndex-fromIndex; i<n; i++) {
            it.next();
            it.remove();
        }
    }

    /**
     * 此列表被 <i>结构修改</i> 的次数。
     * 结构修改是指改变列表大小或以其他方式扰动列表，使得正在进行的迭代可能产生不正确的结果。
     *
     * <p>此字段由 {@code iterator} 和 {@code listIterator} 方法返回的迭代器和列表迭代器实现使用。
     * 如果此字段的值意外改变，迭代器（或列表迭代器）将在 {@code next}、{@code remove}、
     * {@code previous}、{@code set} 或 {@code add} 操作中抛出 {@code ConcurrentModificationException}。
     * 这提供了 <i>快速失败</i> 行为，而不是在迭代过程中遇到并发修改时的不确定行为。
     *
     * <p><b>子类使用此字段是可选的。</b> 如果子类希望提供快速失败的迭代器（和列表迭代器），则只需在
     * 其 {@code add(int, E)} 和 {@code remove(int)} 方法（以及任何其他重写的方法，这些方法会导致列表的结构修改）中增加此字段即可。
     * 单次调用 {@code add(int, E)} 或 {@code remove(int)} 必须将此字段增加不超过 1，否则迭代器（和列表迭代器）将抛出错误的
     * {@code ConcurrentModificationExceptions}。如果实现不希望提供快速失败的迭代器，可以忽略此字段。
     */
    protected transient int modCount = 0;

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size())
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size();
    }
}

class SubList<E> extends AbstractList<E> {
    private final AbstractList<E> l;
    private final int offset;
    private int size;

    SubList(AbstractList<E> list, int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > list.size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
        l = list;
        offset = fromIndex;
        size = toIndex - fromIndex;
        this.modCount = l.modCount;
    }

    public E set(int index, E element) {
        rangeCheck(index);
        checkForComodification();
        return l.set(index+offset, element);
    }

    public E get(int index) {
        rangeCheck(index);
        checkForComodification();
        return l.get(index+offset);
    }

    public int size() {
        checkForComodification();
        return size;
    }

    public void add(int index, E element) {
        rangeCheckForAdd(index);
        checkForComodification();
        l.add(index+offset, element);
        this.modCount = l.modCount;
        size++;
    }

    public E remove(int index) {
        rangeCheck(index);
        checkForComodification();
        E result = l.remove(index+offset);
        this.modCount = l.modCount;
        size--;
        return result;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        checkForComodification();
        l.removeRange(fromIndex+offset, toIndex+offset);
        this.modCount = l.modCount;
        size -= (toIndex-fromIndex);
    }

    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        int cSize = c.size();
        if (cSize==0)
            return false;


                    checkForComodification();
        l.addAll(offset+index, c);
        this.modCount = l.modCount;
        size += cSize;
        return true;
    }

    public Iterator<E> iterator() {
        return listIterator();
    }

    public ListIterator<E> listIterator(final int index) {
        checkForComodification();
        rangeCheckForAdd(index);

        return new ListIterator<E>() {
            private final ListIterator<E> i = l.listIterator(index+offset);

            public boolean hasNext() {
                return nextIndex() < size;
            }

            public E next() {
                if (hasNext())
                    return i.next();
                else
                    throw new NoSuchElementException();
            }

            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }

            public E previous() {
                if (hasPrevious())
                    return i.previous();
                else
                    throw new NoSuchElementException();
            }

            public int nextIndex() {
                return i.nextIndex() - offset;
            }

            public int previousIndex() {
                return i.previousIndex() - offset;
            }

            public void remove() {
                i.remove();
                SubList.this.modCount = l.modCount;
                size--;
            }

            public void set(E e) {
                i.set(e);
            }

            public void add(E e) {
                i.add(e);
                SubList.this.modCount = l.modCount;
                size++;
            }
        };
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return new SubList<>(this, fromIndex, toIndex);
    }

    private void rangeCheck(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    private void checkForComodification() {
        if (this.modCount != l.modCount)
            throw new ConcurrentModificationException();
    }
}

class RandomAccessSubList<E> extends SubList<E> implements RandomAccess {
    RandomAccessSubList(AbstractList<E> list, int fromIndex, int toIndex) {
        super(list, fromIndex, toIndex);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return new RandomAccessSubList<>(this, fromIndex, toIndex);
    }
}
