
/*
 * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
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

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import sun.misc.SharedSecrets;

/**
 * <tt>List</tt> 接口的可调整数组实现。实现了所有可选的列表操作，并允许所有元素，包括 <tt>null</tt>。除了实现 <tt>List</tt> 接口外，此类还提供了用于操作内部存储列表的数组大小的方法。（此类大致相当于 <tt>Vector</tt>，但不同之处在于它是不同步的。）
 *
 * <p><tt>size</tt>、<tt>isEmpty</tt>、<tt>get</tt>、<tt>set</tt>、<tt>iterator</tt> 和 <tt>listIterator</tt> 操作在常数时间内运行。<tt>add</tt> 操作以 <i>摊销常数时间</i> 运行，即添加 n 个元素需要 O(n) 时间。所有其他操作在（大致）线性时间内运行。与 <tt>LinkedList</tt> 实现相比，常数因子较低。
 *
 * <p>每个 <tt>ArrayList</tt> 实例都有一个 <i>容量</i>。容量是用于存储列表中元素的数组的大小。它总是至少与列表大小一样大。随着元素被添加到 <tt>ArrayList</tt> 中，其容量会自动增长。增长策略的细节未指定，但添加一个元素的摊销常数时间成本是确定的。
 *
 * <p>应用程序可以在添加大量元素之前使用 <tt>ensureCapacity</tt> 操作增加 <tt>ArrayList</tt> 实例的容量，这可以减少增量重新分配的数量。
 *
 * <p><strong>请注意，此实现不同步。</strong> 如果多个线程同时访问一个 <tt>ArrayList</tt> 实例，并且至少有一个线程修改了列表的结构，则必须从外部进行同步。（结构修改是指任何添加或删除一个或多个元素的操作，或显式调整支持数组的大小；仅设置元素的值不是结构修改。）这通常通过在自然封装列表的某个对象上进行同步来实现。
 *
 * 如果没有这样的对象，应使用 {@link Collections#synchronizedList Collections.synchronizedList} 方法将列表“包装”起来。最好在创建时进行此操作，以防止意外的未同步访问：<pre>
 *   List list = Collections.synchronizedList(new ArrayList(...));</pre>
 *
 * <p><a name="fail-fast">
 * 由此类的 {@link #iterator() iterator} 和 {@link #listIterator(int) listIterator} 方法返回的迭代器是 <em>快速失败</em>：</a>
 * 如果在创建迭代器后以任何方式对列表进行结构修改，除非通过迭代器自身的
 * {@link ListIterator#remove() remove} 或
 * {@link ListIterator#add(Object) add} 方法，迭代器将抛出一个
 * {@link ConcurrentModificationException}。因此，在并发修改的情况下，迭代器会快速且干净地失败，而不是在未来的某个不确定时间点出现任意的、不确定的行为。
 *
 * <p>请注意，迭代器的快速失败行为不能保证，因为在存在未同步的并发修改的情况下，通常不可能做出任何硬性保证。快速失败迭代器在尽最大努力的基础上抛出 {@code ConcurrentModificationException}。
 * 因此，编写依赖此异常正确性的程序是错误的：<i>迭代器的快速失败行为仅应用于检测错误。</i>
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Collection
 * @see     List
 * @see     LinkedList
 * @see     Vector
 * @since   1.2
 */

public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * 默认初始容量。
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 用于空实例的共享空数组实例。
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * 用于默认大小空实例的共享空数组实例。我们将其与 EMPTY_ELEMENTDATA 区分开来，以便在添加第一个元素时知道要扩展多少。
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * 存储 <tt>ArrayList</tt> 元素的数组缓冲区。<tt>ArrayList</tt> 的容量是此数组缓冲区的长度。任何 elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA 的空 <tt>ArrayList</tt> 在添加第一个元素时将扩展到 DEFAULT_CAPACITY。
     */
    transient Object[] elementData; // non-private to simplify nested class access

    /**
     * <tt>ArrayList</tt> 的大小（它包含的元素数量）。
     *
     * @serial
     */
    private int size;

    /**
     * 构造一个具有指定初始容量的空列表。
     *
     * @param  initialCapacity  列表的初始容量
     * @throws IllegalArgumentException 如果指定的初始容量为负数
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }

    /**
     * 构造一个初始容量为十的空列表。
     */
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    /**
     * 构造一个包含指定集合元素的列表，顺序与集合的迭代器返回的顺序相同。
     *
     * @param c 要放置到此列表中的集合
     * @throws NullPointerException 如果指定的集合为 null
     */
    public ArrayList(Collection<? extends E> c) {
        Object[] a = c.toArray();
        if ((size = a.length) != 0) {
            if (c.getClass() == ArrayList.class) {
                elementData = a;
            } else {
                elementData = Arrays.copyOf(a, size, Object[].class);
            }
        } else {
            // 替换为空数组。
            elementData = EMPTY_ELEMENTDATA;
        }
    }

    /**
     * 将此 <tt>ArrayList</tt> 实例的容量调整为列表的当前大小。应用程序可以使用此操作来最小化 <tt>ArrayList</tt> 实例的存储。
     */
    public void trimToSize() {
        modCount++;
        if (size < elementData.length) {
            elementData = (size == 0)
              ? EMPTY_ELEMENTDATA
              : Arrays.copyOf(elementData, size);
        }
    }

    /**
     * 必要时增加此 <tt>ArrayList</tt> 实例的容量，以确保它可以至少容纳由最小容量参数指定的元素数量。
     *
     * @param   minCapacity   所需的最小容量
     */
    public void ensureCapacity(int minCapacity) {
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
            // 如果不是默认元素表，则为任何大小
            ? 0
            // 对于默认空表，大于默认值。默认情况下，它已经应该是默认大小。
            : DEFAULT_CAPACITY;

        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }

    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }

    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // 溢出意识代码
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

    /**
     * 可分配的最大数组大小。
     * 一些虚拟机在数组中保留一些头字。
     * 尝试分配更大的数组可能导致
     * OutOfMemoryError: 请求的数组大小超过 VM 限制
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 增加容量以确保它可以至少容纳由最小容量参数指定的元素数量。
     *
     * @param minCapacity 所需的最小容量
     */
    private void grow(int minCapacity) {
        // 溢出意识代码
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity 通常接近 size，所以这是一个胜利：
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // 溢出
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

    /**
     * 返回此列表中的元素数量。
     *
     * @return 此列表中的元素数量
     */
    public int size() {
        return size;
    }

    /**
     * 如果此列表不包含任何元素，则返回 <tt>true</tt>。
     *
     * @return 如果此列表不包含任何元素，则返回 <tt>true</tt>
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 如果此列表包含指定的元素，则返回 <tt>true</tt>。更正式地说，如果此列表至少包含一个元素 <tt>e</tt> 使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>，则返回 <tt>true</tt>。
     *
     * @param o 要测试其在此列表中是否存在性的元素
     * @return 如果此列表包含指定的元素，则返回 <tt>true</tt>
     */
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * 返回此列表中指定元素第一次出现的索引，如果此列表不包含该元素，则返回 -1。更正式地说，返回最低索引 <tt>i</tt> 使得
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>，如果不存在这样的索引，则返回 -1。
     */
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * 返回此列表中指定元素最后一次出现的索引，如果此列表不包含该元素，则返回 -1。更正式地说，返回最高索引 <tt>i</tt> 使得
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>，如果不存在这样的索引，则返回 -1。
     */
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size-1; i >= 0; i--)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = size-1; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * 返回此 <tt>ArrayList</tt> 实例的浅拷贝。（元素本身不会被复制。）
     *
     * @return 此 <tt>ArrayList</tt> 实例的克隆
     */
    public Object clone() {
        try {
            ArrayList<?> v = (ArrayList<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们是 Cloneable
            throw new InternalError(e);
        }
    }

    /**
     * 返回一个包含此列表中所有元素的数组（从第一个到最后一个元素）。
     *
     * <p>返回的数组将是“安全的”，因为此列表中没有任何引用。换句话说，此方法必须分配一个新数组。调用者因此可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组和基于集合的 API 之间的桥梁。
     *
     * @return 一个包含此列表中所有元素的数组，按正确顺序排列
     */
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    /**
     * 返回一个包含此列表中所有元素的数组（从第一个到最后一个元素）；返回数组的运行时类型是指定数组的运行时类型。如果列表适合指定的数组，则返回该数组。否则，分配一个运行时类型为指定数组且大小为此列表大小的新数组。
     *
     * <p>如果列表适合指定的数组且有剩余空间（即，数组的元素多于列表），则数组中紧接在集合末尾之后的元素被设置为 <tt>null</tt>。（这仅在调用者知道列表不包含任何 null 元素时有用。）
     *
     * @param a 要存储列表元素的数组，如果它足够大；否则，为此目的分配一个相同运行时类型的数组。
     * @return 包含列表元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此列表中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            // 分配一个运行时类型为 a 的新数组，但内容为我的：
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }


                // 位置访问操作

    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    /**
     * 返回此列表中指定位置的元素。
     *
     * @param  index 要返回的元素的索引
     * @return 此列表中指定位置的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        rangeCheck(index);

        return elementData(index);
    }

    /**
     * 用指定的元素替换此列表中指定位置的元素。
     *
     * @param index 要替换的元素的索引
     * @param element 要存储在指定位置的元素
     * @return 之前位于指定位置的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E set(int index, E element) {
        rangeCheck(index);

        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }

    /**
     * 将指定的元素添加到此列表的末尾。
     *
     * @param e 要添加到此列表的元素
     * @return <tt>true</tt>（如 {@link Collection#add} 所指定）
     */
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // 增加 modCount!!
        elementData[size++] = e;
        return true;
    }

    /**
     * 将指定的元素插入到此列表的指定位置。将当前位置（如果有）及其后续元素向右移动（增加它们的索引）。
     *
     * @param index 要插入指定元素的位置
     * @param element 要插入的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, E element) {
        rangeCheckForAdd(index);

        ensureCapacityInternal(size + 1);  // 增加 modCount!!
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        elementData[index] = element;
        size++;
    }

    /**
     * 从此列表中移除指定位置的元素。将任何后续元素向左移动（减少它们的索引）。
     *
     * @param index 要移除的元素的索引
     * @return 从列表中移除的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E remove(int index) {
        rangeCheck(index);

        modCount++;
        E oldValue = elementData(index);

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // 清除以让 GC 完成其工作

        return oldValue;
    }

    /**
     * 如果此列表包含指定的元素，则移除其第一次出现。如果列表不包含该元素，则保持不变。更正式地说，移除具有最低索引
     * <tt>i</tt> 的元素，使得
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * （如果存在这样的元素）。如果此列表包含指定的元素（或等效地，如果此列表因调用而改变），则返回 <tt>true</tt>。
     *
     * @param o 要从此列表中移除的元素（如果存在）
     * @return <tt>true</tt> 如果此列表包含指定的元素
     */
    public boolean remove(Object o) {
        if (o == null) {
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }

    /*
     * 私有移除方法，跳过边界检查并且不返回移除的值。
     */
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // 清除以让 GC 完成其工作
    }

    /**
     * 从此列表中移除所有元素。调用返回后，列表将为空。
     */
    public void clear() {
        modCount++;

        // 清除以让 GC 完成其工作
        for (int i = 0; i < size; i++)
            elementData[i] = null;

        size = 0;
    }

    /**
     * 将指定集合中的所有元素按指定集合的迭代器返回的顺序添加到此列表的末尾。如果在操作进行过程中指定的集合被修改，此操作的行为是未定义的。这意味着，如果指定的集合是此列表，并且此列表非空，则此调用的行为是未定义的。
     *
     * @param c 包含要添加到此列表的元素的集合
     * @return <tt>true</tt> 如果此列表因调用而改变
     * @throws NullPointerException 如果指定的集合为 null
     */
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // 增加 modCount
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        return numNew != 0;
    }

    /**
     * 将指定集合中的所有元素插入到此列表中，从指定的位置开始。将当前位置（如果有）及其后续元素向右移动（增加它们的索引）。新元素将按指定集合的迭代器返回的顺序出现在列表中。
     *
     * @param index 要插入指定集合中第一个元素的位置
     * @param c 包含要添加到此列表的元素的集合
     * @return <tt>true</tt> 如果此列表因调用而改变
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException 如果指定的集合为 null
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);

        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // 增加 modCount

        int numMoved = size - index;
        if (numMoved > 0)
            System.arraycopy(elementData, index, elementData, index + numNew,
                             numMoved);

        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew;
        return numNew != 0;
    }

    /**
     * 从此列表中移除索引在 {@code fromIndex}（包含）和 {@code toIndex}（不包含）之间的所有元素。将任何后续元素向左移动（减少它们的索引）。此调用将列表缩短 {@code (toIndex - fromIndex)} 个元素。
     * （如果 {@code toIndex==fromIndex}，此操作没有效果。）
     *
     * @throws IndexOutOfBoundsException 如果 {@code fromIndex} 或
     *         {@code toIndex} 超出范围
     *         ({@code fromIndex < 0 ||
     *          fromIndex >= size() ||
     *          toIndex > size() ||
     *          toIndex < fromIndex})
     */
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                         numMoved);

        // 清除以让 GC 完成其工作
        int newSize = size - (toIndex-fromIndex);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        size = newSize;
    }

    /**
     * 检查给定的索引是否在范围内。如果不在范围内，则抛出适当的运行时异常。此方法不检查索引是否为负数：它总是在数组访问之前立即使用，如果索引为负数，数组访问将抛出 ArrayIndexOutOfBoundsException。
     */
    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 用于 add 和 addAll 的 rangeCheck 版本。
     */
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 构造一个 IndexOutOfBoundsException 详细消息。在错误处理代码的许多可能重构中，这种“外联”在服务器和客户端 VM 中的性能最佳。
     */
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    /**
     * 从此列表中移除包含在指定集合中的所有元素。
     *
     * @param c 包含要从此列表中移除的元素的集合
     * @return {@code true} 如果此列表因调用而改变
     * @throws ClassCastException 如果此列表中元素的类与指定集合不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果此列表包含 null 元素且指定集合不允许 null 元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)，
     *         或者指定的集合为 null
     * @see Collection#contains(Object)
     */
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, false);
    }

    /**
     * 仅保留此列表中包含在指定集合中的元素。换句话说，从此列表中移除所有不包含在指定集合中的元素。
     *
     * @param c 包含要保留在此列表中的元素的集合
     * @return {@code true} 如果此列表因调用而改变
     * @throws ClassCastException 如果此列表中元素的类与指定集合不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果此列表包含 null 元素且指定集合不允许 null 元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)，
     *         或者指定的集合为 null
     * @see Collection#contains(Object)
     */
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, true);
    }

    private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData;
        int r = 0, w = 0;
        boolean modified = false;
        try {
            for (; r < size; r++)
                if (c.contains(elementData[r]) == complement)
                    elementData[w++] = elementData[r];
        } finally {
            // 保持与 AbstractCollection 的行为兼容性，即使 c.contains() 抛出异常
            if (r != size) {
                System.arraycopy(elementData, r,
                                 elementData, w,
                                 size - r);
                w += size - r;
            }
            if (w != size) {
                // 清除以让 GC 完成其工作
                for (int i = w; i < size; i++)
                    elementData[i] = null;
                modCount += size - w;
                size = w;
                modified = true;
            }
        }
        return modified;
    }

    /**
     * 将 <tt>ArrayList</tt> 实例的状态保存到流中（即，序列化它）。
     *
     * @serialData 序列化 <tt>ArrayList</tt> 实例时，先发出支持 <tt>ArrayList</tt> 实例的数组的长度（int），然后按正确的顺序发出所有元素（每个都是 <tt>Object</tt>）。
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
        // 发出元素计数，以及任何隐藏的内容
        int expectedModCount = modCount;
        s.defaultWriteObject();

        // 发出大小作为容量，以保持与 clone() 的行为兼容
        s.writeInt(size);

        // 按正确的顺序发出所有元素。
        for (int i=0; i<size; i++) {
            s.writeObject(elementData[i]);
        }

        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * 从流中重新构建 <tt>ArrayList</tt> 实例（即，反序列化它）。
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        elementData = EMPTY_ELEMENTDATA;

        // 读取大小，以及任何隐藏的内容
        s.defaultReadObject();

        // 读取容量
        s.readInt(); // 忽略

        if (size > 0) {
            // 类似于 clone()，根据大小而不是容量分配数组
            int capacity = calculateCapacity(elementData, size);
            SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);
            ensureCapacityInternal(size);

            Object[] a = elementData;
            // 按正确的顺序读取所有元素。
            for (int i=0; i<size; i++) {
                a[i] = s.readObject();
            }
        }
    }

    /**
     * 返回一个从列表的指定位置开始的列表迭代器（按正确的顺序）。指定的索引表示初始调用 {@link ListIterator#next next} 时返回的第一个元素。初始调用 {@link ListIterator#previous previous} 将返回指定索引减一的元素。
     *
     * <p>返回的列表迭代器是 <a href="#fail-fast"><i>快速失败</i></a> 的。
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: "+index);
        return new ListItr(index);
    }

    /**
     * 返回一个按正确顺序遍历此列表元素的列表迭代器。
     *
     * <p>返回的列表迭代器是 <a href="#fail-fast"><i>快速失败</i></a> 的。
     *
     * @see #listIterator(int)
     */
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    /**
     * 返回一个按正确顺序遍历此列表元素的迭代器。
     *
     * <p>返回的迭代器是 <a href="#fail-fast"><i>快速失败</i></a> 的。
     *
     * @return 一个按正确顺序遍历此列表元素的迭代器
     */
    public Iterator<E> iterator() {
        return new Itr();
    }


                /**
     * An optimized version of AbstractList.Itr
     */
    private class Itr implements Iterator<E> {
        int cursor;       // 下一个要返回的元素的索引
        int lastRet = -1; // 最后返回的元素的索引；如果没有这样的元素则为 -1
        int expectedModCount = modCount;

        Itr() {}

        public boolean hasNext() {
            return cursor != size;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = ArrayList.this.size;
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) {
                consumer.accept((E) elementData[i++]);
            }
            // 在迭代结束时一次性更新以减少堆写流量
            cursor = i;
            lastRet = i - 1;
            checkForComodification();
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * An optimized version of AbstractList.ListItr
     */
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i;
            return (E) elementData[lastRet = i];
        }

        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                ArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * 返回此列表指定的 {@code fromIndex}（包含）和 {@code toIndex}（不包含）之间的部分视图。
     * （如果 {@code fromIndex} 和 {@code toIndex} 相等，则返回的列表为空。）
     * 返回的列表由此列表支持，因此返回的列表中的非结构更改将反映在此列表中，反之亦然。
     * 返回的列表支持所有可选的列表操作。
     *
     * <p>此方法消除了显式范围操作的需要（通常数组中存在此类操作）。
     * 任何期望列表的操作都可以通过传递子列表视图而不是整个列表来用作范围操作。
     * 例如，以下惯用法从列表中删除一个范围的元素：
     * <pre>
     *      list.subList(from, to).clear();
     * </pre>
     * 类似的惯用法可以构建用于 {@link #indexOf(Object)} 和 {@link #lastIndexOf(Object)}，
     * 并且 {@link Collections} 类中的所有算法都可以应用于子列表。
     *
     * <p>如果以任何方式通过返回的列表之外的方式对支持列表（即此列表）进行 <i>结构修改</i>，
     * 则返回列表的语义将变得未定义。
     * （结构修改是指改变此列表的大小，或以其他方式干扰此列表，使得正在进行的迭代可能产生不正确的结果。）
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        return new SubList(this, 0, fromIndex, toIndex);
    }

    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
    }

    private class SubList extends AbstractList<E> implements RandomAccess {
        private final AbstractList<E> parent;
        private final int parentOffset;
        private final int offset;
        int size;

        SubList(AbstractList<E> parent,
                int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = ArrayList.this.modCount;
        }

        public E set(int index, E e) {
            rangeCheck(index);
            checkForComodification();
            E oldValue = ArrayList.this.elementData(offset + index);
            ArrayList.this.elementData[offset + index] = e;
            return oldValue;
        }

        public E get(int index) {
            rangeCheck(index);
            checkForComodification();
            return ArrayList.this.elementData(offset + index);
        }

        public int size() {
            checkForComodification();
            return this.size;
        }

        public void add(int index, E e) {
            rangeCheckForAdd(index);
            checkForComodification();
            parent.add(parentOffset + index, e);
            this.modCount = parent.modCount;
            this.size++;
        }

        public E remove(int index) {
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index);
            this.modCount = parent.modCount;
            this.size--;
            return result;
        }

        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            parent.removeRange(parentOffset + fromIndex,
                               parentOffset + toIndex);
            this.modCount = parent.modCount;
            this.size -= toIndex - fromIndex;
        }

        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize==0)
                return false;

            checkForComodification();
            parent.addAll(parentOffset + index, c);
            this.modCount = parent.modCount;
            this.size += cSize;
            return true;
        }

        public Iterator<E> iterator() {
            return listIterator();
        }

        public ListIterator<E> listIterator(final int index) {
            checkForComodification();
            rangeCheckForAdd(index);
            final int offset = this.offset;

            return new ListIterator<E>() {
                int cursor = index;
                int lastRet = -1;
                int expectedModCount = ArrayList.this.modCount;

                public boolean hasNext() {
                    return cursor != SubList.this.size;
                }

                @SuppressWarnings("unchecked")
                public E next() {
                    checkForComodification();
                    int i = cursor;
                    if (i >= SubList.this.size)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i + 1;
                    return (E) elementData[offset + (lastRet = i)];
                }

                public boolean hasPrevious() {
                    return cursor != 0;
                }

                @SuppressWarnings("unchecked")
                public E previous() {
                    checkForComodification();
                    int i = cursor - 1;
                    if (i < 0)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i;
                    return (E) elementData[offset + (lastRet = i)];
                }

                @SuppressWarnings("unchecked")
                public void forEachRemaining(Consumer<? super E> consumer) {
                    Objects.requireNonNull(consumer);
                    final int size = SubList.this.size;
                    int i = cursor;
                    if (i >= size) {
                        return;
                    }
                    final Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length) {
                        throw new ConcurrentModificationException();
                    }
                    while (i != size && modCount == expectedModCount) {
                        consumer.accept((E) elementData[offset + (i++)]);
                    }
                    // 在迭代结束时一次性更新以减少堆写流量
                    lastRet = cursor = i;
                    checkForComodification();
                }

                public int nextIndex() {
                    return cursor;
                }

                public int previousIndex() {
                    return cursor - 1;
                }

                public void remove() {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        SubList.this.remove(lastRet);
                        cursor = lastRet;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void set(E e) {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        ArrayList.this.set(offset + lastRet, e);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void add(E e) {
                    checkForComodification();

                    try {
                        int i = cursor;
                        SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                final void checkForComodification() {
                    if (expectedModCount != ArrayList.this.modCount)
                        throw new ConcurrentModificationException();
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new SubList(this, offset, fromIndex, toIndex);
        }

        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private String outOfBoundsMsg(int index) {
            return "Index: "+index+", Size: "+this.size;
        }

        private void checkForComodification() {
            if (ArrayList.this.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }

        public Spliterator<E> spliterator() {
            checkForComodification();
            return new ArrayListSpliterator<E>(ArrayList.this, offset,
                                               offset + this.size, this.modCount);
        }
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * 创建一个 <em><a href="Spliterator.html#binding">延迟绑定</a></em>
     * 和 <em>快速失败</em> 的 {@link Spliterator}，遍历此列表中的元素。
     *
     * <p>{@code Spliterator} 报告 {@link Spliterator#SIZED}、
     * {@link Spliterator#SUBSIZED} 和 {@link Spliterator#ORDERED}。
     * 覆盖实现应记录额外的特性值。
     *
     * @return 一个遍历此列表中元素的 {@code Spliterator}
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new ArrayListSpliterator<>(this, 0, -1, 0);
    }


                /** Index-based split-by-two, lazily initialized Spliterator */
    static final class ArrayListSpliterator<E> implements Spliterator<E> {

        /*
         * 如果 ArrayList 是不可变的，或者结构上不可变（没有添加、删除等操作），我们可以使用 Arrays.spliterator 实现它们的 Spliterator。
         * 但是，我们尽量在遍历时检测实际的干扰，而不牺牲太多性能。我们主要依赖 modCounts。这些计数不能保证检测到并发违规，并且有时对线程内的干扰过于保守，
         * 但在实践中检测到的问题足够多，值得这样做。为此，我们（1）延迟初始化 fence 和 expectedModCount，直到我们需要提交要检查的状态的最晚时间；
         * 从而提高精度。（这不适用于创建带有当前非延迟值的 spliterator 的 SubLists）。（2）我们在 forEach（性能最敏感的方法）结束时只进行一次
         * ConcurrentModificationException 检查。当使用 forEach（而不是迭代器）时，我们通常只能在操作后检测到干扰，而不是在操作前。
         * 进一步触发 CME 检查适用于所有其他可能的假设违规，例如给定其 size() 的 elementData 数组为 null 或太小，这只能由于干扰而发生。
         * 这允许 forEach 的内部循环在没有任何进一步检查的情况下运行，并简化 lambda 解析。虽然这涉及许多检查，但请注意，在 list.stream().forEach(a) 的常见情况下，
         * 除了在 forEach 内部之外，任何地方都不会发生检查或其他计算。其他较少使用的其他方法不能利用这些优化。
         */

        private final ArrayList<E> list;
        private int index; // 当前索引，修改于 advance/split
        private int fence; // -1 直到使用；然后是最后一个索引的下一个
        private int expectedModCount; // 在设置 fence 时初始化

        /** 创建覆盖给定范围的新 spliterator */
        ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                             int expectedModCount) {
            this.list = list; // 如果不遍历，可以为 null
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // 在首次使用时初始化 fence 为 size
            int hi; // （在方法 forEach 中有一个专门的变体）
            ArrayList<E> lst;
            if ((hi = fence) < 0) {
                if ((lst = list) == null)
                    hi = fence = 0;
                else {
                    expectedModCount = lst.modCount;
                    hi = fence = lst.size;
                }
            }
            return hi;
        }

        public ArrayListSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : // 除非太小，否则将范围分成两半
                new ArrayListSpliterator<E>(list, lo, index = mid,
                                            expectedModCount);
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), i = index;
            if (i < hi) {
                index = i + 1;
                @SuppressWarnings("unchecked") E e = (E)list.elementData[i];
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // 从循环中提升访问和检查
            ArrayList<E> lst; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.elementData) != null) {
                if ((hi = fence) < 0) {
                    mc = lst.modCount;
                    hi = lst.size;
                }
                else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        action.accept(e);
                    }
                    if (lst.modCount == mc)
                        return;
                }
            }
            throw new ConcurrentModificationException();
        }

        public long estimateSize() {
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        // 确定要删除的元素
        // 在此阶段从过滤器谓词抛出的任何异常
        // 都将使集合保持不变
        int removeCount = 0;
        final BitSet removeSet = new BitSet(size);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i];
            if (filter.test(element)) {
                removeSet.set(i);
                removeCount++;
            }
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }

        // 将存活的元素左移，覆盖被删除元素留下的空位
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) {
            final int newSize = size - removeCount;
            for (int i=0, j=0; (i < size) && (j < newSize); i++, j++) {
                i = removeSet.nextClearBit(i);
                elementData[j] = elementData[i];
            }
            for (int k=newSize; k < size; k++) {
                elementData[k] = null;  // 让垃圾回收器工作
            }
            this.size = newSize;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++;
        }

        return anyToRemove;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            elementData[i] = operator.apply((E) elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount;
        Arrays.sort((E[]) elementData, 0, size, c);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
}
