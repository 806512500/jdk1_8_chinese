
/*
 * Copyright (c) 1994, 2017, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * {@code Vector} 类实现了一个可增长的对象数组。像数组一样，它可以使用整数索引访问组件。但是，{@code Vector} 的大小可以随着添加和删除项而增长或缩小。
 *
 * <p>每个向量通过维护一个 {@code capacity} 和一个 {@code capacityIncrement} 来优化存储管理。{@code capacity} 始终至少与向量大小相同；它通常更大，因为当组件被添加到向量时，向量的存储会以 {@code capacityIncrement} 的大小增加。应用程序可以在插入大量组件之前增加向量的容量，以减少增量重新分配。
 *
 * <p><a name="fail-fast">
 * 该类的 {@link #iterator() iterator} 和 {@link #listIterator(int) listIterator} 方法返回的迭代器是 <em>快速失败</em> 的：</a> 如果在创建迭代器后，向量在任何时间以任何方式被结构修改，除了通过迭代器自己的 {@link ListIterator#remove() remove} 或 {@link ListIterator#add(Object) add} 方法，迭代器将抛出 {@link ConcurrentModificationException}。因此，在并发修改的情况下，迭代器会快速且干净地失败，而不是在未来的某个不确定时间点表现出任意的、不确定的行为。由 {@link #elements() elements} 方法返回的 {@link Enumeration Enumerations} <em>不是</em> 快速失败的。
 *
 * <p>请注意，迭代器的快速失败行为不能保证，因为在未同步的并发修改的情况下，通常不可能做出任何硬性保证。快速失败的迭代器在最佳努力的基础上抛出 {@code ConcurrentModificationException}。因此，编写依赖于此异常正确性的程序是错误的： <i>迭代器的快速失败行为应仅用于检测错误。</i>
 *
 * <p>自 Java 2 平台 v1.2 起，该类被改造以实现 {@link List} 接口，使其成为 <a href="{@docRoot}/../technotes/guides/collections/index.html">Java Collections Framework</a> 的成员。与新的集合实现不同，{@code Vector} 是同步的。如果不需要线程安全的实现，建议使用 {@link ArrayList} 代替 {@code Vector}。
 *
 * @author  Lee Boynton
 * @author  Jonathan Payne
 * @see Collection
 * @see LinkedList
 * @since   JDK1.0
 */
public class Vector<E>
    extends AbstractList<E>
    implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    /**
     * 存储向量组件的数组缓冲区。向量的容量是此数组缓冲区的长度，至少足够大以包含向量的所有元素。
     *
     * <p>向量最后一个元素之后的任何数组元素都是 null。
     *
     * @serial
     */
    protected Object[] elementData;

    /**
     * 此 {@code Vector} 对象中的有效组件数量。组件 {@code elementData[0]} 到 {@code elementData[elementCount-1]} 是实际项。
     *
     * @serial
     */
    protected int elementCount;

    /**
     * 向量的容量在大小超过其容量时自动增加的量。如果容量增量小于或等于零，则向量的容量每次需要增长时都会翻倍。
     *
     * @serial
     */
    protected int capacityIncrement;

    /** 为了互操作性使用 JDK 1.0.2 的 serialVersionUID */
    private static final long serialVersionUID = -2767605614048989439L;

    /**
     * 构造一个具有指定初始容量和容量增量的空向量。
     *
     * @param   initialCapacity     向量的初始容量
     * @param   capacityIncrement   向量溢出时容量增加的量
     * @throws IllegalArgumentException 如果指定的初始容量为负数
     */
    public Vector(int initialCapacity, int capacityIncrement) {
        super();
        if (initialCapacity < 0)
            throw new IllegalArgumentException("非法容量: " +
                                               initialCapacity);
        this.elementData = new Object[initialCapacity];
        this.capacityIncrement = capacityIncrement;
    }

    /**
     * 构造一个具有指定初始容量且容量增量为零的空向量。
     *
     * @param   initialCapacity   向量的初始容量
     * @throws IllegalArgumentException 如果指定的初始容量为负数
     */
    public Vector(int initialCapacity) {
        this(initialCapacity, 0);
    }

    /**
     * 构造一个空向量，使其内部数据数组的大小为 {@code 10}，其标准容量增量为零。
     */
    public Vector() {
        this(10);
    }

    /**
     * 构造一个包含指定集合元素的向量，顺序与集合的迭代器返回的顺序相同。
     *
     * @param c 要放置到此向量中的集合的元素
     * @throws NullPointerException 如果指定的集合为 null
     * @since   1.2
     */
    public Vector(Collection<? extends E> c) {
        Object[] a = c.toArray();
        elementCount = a.length;
        if (c.getClass() == ArrayList.class) {
            elementData = a;
        } else {
            elementData = Arrays.copyOf(a, elementCount, Object[].class);
        }
    }

    /**
     * 将此向量的组件复制到指定的数组中。向量索引为 {@code k} 的项被复制到数组的 {@code k} 位置。
     *
     * @param  anArray 要复制到的数组
     * @throws NullPointerException 如果给定的数组为 null
     * @throws IndexOutOfBoundsException 如果指定的数组不足以容纳此向量的所有组件
     * @throws ArrayStoreException 如果此向量的组件不是可以在指定数组中存储的运行时类型
     * @see #toArray(Object[])
     */
    public synchronized void copyInto(Object[] anArray) {
        System.arraycopy(elementData, 0, anArray, 0, elementCount);
    }

    /**
     * 将此向量的容量调整为其当前大小。如果此向量的容量大于其当前大小，则通过用较小的数组替换其内部数据数组（存储在字段 {@code elementData} 中）来将容量更改为等于大小。应用程序可以使用此操作来最小化向量的存储。
     */
    public synchronized void trimToSize() {
        modCount++;
        int oldCapacity = elementData.length;
        if (elementCount < oldCapacity) {
            elementData = Arrays.copyOf(elementData, elementCount);
        }
    }

    /**
     * 必要时增加此向量的容量，以确保它可以至少容纳由最小容量参数指定的组件数。
     *
     * <p>如果此向量的当前容量小于 {@code minCapacity}，则通过用更大的数组替换其内部数据数组（存储在字段 {@code elementData} 中）来增加其容量。新数据数组的大小将是旧大小加上 {@code capacityIncrement}，除非 {@code capacityIncrement} 小于或等于零，在这种情况下，新容量将是旧容量的两倍；但如果这个新大小仍然小于 {@code minCapacity}，则新容量将是 {@code minCapacity}。
     *
     * @param minCapacity 所需的最小容量
     */
    public synchronized void ensureCapacity(int minCapacity) {
        if (minCapacity > 0) {
            modCount++;
            ensureCapacityHelper(minCapacity);
        }
    }

    /**
     * 实现 ensureCapacity 的非同步语义。此类中的同步方法可以内部调用此方法来确保容量，而不会产生额外的同步开销。
     *
     * @see #ensureCapacity(int)
     */
    private void ensureCapacityHelper(int minCapacity) {
        // 防止溢出的代码
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

    /**
     * 可分配的最大数组大小。某些虚拟机在数组中保留一些头字。尝试分配更大的数组可能会导致
     * OutOfMemoryError: 请求的数组大小超过 VM 限制
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private void grow(int minCapacity) {
        // 防止溢出的代码
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + ((capacityIncrement > 0) ?
                                         capacityIncrement : oldCapacity);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
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
     * 设置此向量的大小。如果新大小大于当前大小，则在向量末尾添加新的 {@code null} 项。如果新大小小于当前大小，则所有索引为 {@code newSize} 及更大的组件都将被丢弃。
     *
     * @param  newSize   此向量的新大小
     * @throws ArrayIndexOutOfBoundsException 如果新大小为负数
     */
    public synchronized void setSize(int newSize) {
        modCount++;
        if (newSize > elementCount) {
            ensureCapacityHelper(newSize);
        } else {
            for (int i = newSize ; i < elementCount ; i++) {
                elementData[i] = null;
            }
        }
        elementCount = newSize;
    }

    /**
     * 返回此向量的当前容量。
     *
     * @return  当前容量（其内部数据数组的长度，存储在字段 {@code elementData} 中）
     */
    public synchronized int capacity() {
        return elementData.length;
    }

    /**
     * 返回此向量中的组件数量。
     *
     * @return  此向量中的组件数量
     */
    public synchronized int size() {
        return elementCount;
    }

    /**
     * 测试此向量是否没有组件。
     *
     * @return  如果且仅如果此向量没有组件，即其大小为零，则返回 {@code true}；否则返回 {@code false}。
     */
    public synchronized boolean isEmpty() {
        return elementCount == 0;
    }

    /**
     * 返回此向量组件的枚举。返回的 {@code Enumeration} 对象将生成此向量中的所有项。首先生成索引为 {@code 0} 的项，然后是索引为 {@code 1} 的项，依此类推。
     *
     * @return  此向量组件的枚举
     * @see     Iterator
     */
    public Enumeration<E> elements() {
        return new Enumeration<E>() {
            int count = 0;

            public boolean hasMoreElements() {
                return count < elementCount;
            }

            public E nextElement() {
                synchronized (Vector.this) {
                    if (count < elementCount) {
                        return elementData(count++);
                    }
                }
                throw new NoSuchElementException("Vector Enumeration");
            }
        };
    }

    /**
     * 如果此向量包含指定的元素，则返回 {@code true}。更正式地说，如果且仅如果此向量包含至少一个元素 {@code e} 使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>，则返回 {@code true}。
     *
     * @param o 要测试其是否存在于此向量中的元素
     * @return 如果此向量包含指定的元素，则返回 {@code true}
     */
    public boolean contains(Object o) {
        return indexOf(o, 0) >= 0;
    }

    /**
     * 返回此向量中指定元素第一次出现的索引，如果此向量不包含该元素，则返回 -1。更正式地说，返回最低的索引 {@code i} 使得
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>，或 -1 如果没有这样的索引。
     *
     * @param o 要搜索的元素
     * @return 此向量中指定元素第一次出现的索引，如果此向量不包含该元素，则返回 -1
     */
    public int indexOf(Object o) {
        return indexOf(o, 0);
    }

    /**
     * 从 {@code index} 开始向前搜索，返回此向量中指定元素第一次出现的索引，如果未找到该元素，则返回 -1。更正式地说，返回最低的索引 {@code i} 使得
     * <tt>(i&nbsp;&gt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i))))</tt>，或 -1 如果没有这样的索引。
     *
     * @param o 要搜索的元素
     * @param index 开始搜索的位置
     * @return 从位置 {@code index} 开始，此向量中指定元素第一次出现的索引；如果未找到该元素，则返回 -1。
     * @throws IndexOutOfBoundsException 如果指定的索引为负数
     * @see     Object#equals(Object)
     */
    public synchronized int indexOf(Object o, int index) {
        if (o == null) {
            for (int i = index ; i < elementCount ; i++)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = index ; i < elementCount ; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }


                /**
     * 返回指定元素在此向量中最后一次出现的索引，如果此向量不包含该元素，则返回 -1。
     * 更正式地说，返回最高的索引 {@code i}，使得
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>，
     * 或者如果没有这样的索引，则返回 -1。
     *
     * @param o 要搜索的元素
     * @return 指定元素在此向量中最后一次出现的索引，
     *         如果此向量不包含该元素，则返回 -1
     */
    public synchronized int lastIndexOf(Object o) {
        return lastIndexOf(o, elementCount-1);
    }

    /**
     * 返回指定元素在此向量中最后一次出现的索引，从 {@code index} 位置向后搜索，如果未找到该元素，则返回 -1。
     * 更正式地说，返回最高的索引 {@code i}，使得
     * <tt>(i&nbsp;&lt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i))))</tt>，
     * 或者如果没有这样的索引，则返回 -1。
     *
     * @param o 要搜索的元素
     * @param index 开始向后搜索的位置
     * @return 位置小于或等于 {@code index} 的指定元素最后一次出现的索引；
     *         如果未找到该元素，则返回 -1。
     * @throws IndexOutOfBoundsException 如果指定的索引大于或等于此向量的当前大小
     */
    public synchronized int lastIndexOf(Object o, int index) {
        if (index >= elementCount)
            throw new IndexOutOfBoundsException(index + " >= "+ elementCount);

        if (o == null) {
            for (int i = index; i >= 0; i--)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = index; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * 返回指定索引处的组件。
     *
     * <p>此方法在功能上与 {@link #get(int)}
     * 方法（它是 {@link List} 接口的一部分）相同。
     *
     * @param      index   此向量中的一个索引
     * @return     指定索引处的组件
     * @throws ArrayIndexOutOfBoundsException 如果索引超出范围
     *         ({@code index < 0 || index >= size()})
     */
    public synchronized E elementAt(int index) {
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
        }

        return elementData(index);
    }

    /**
     * 返回此向量的第一个组件（索引为 {@code 0} 的项）。
     *
     * @return     此向量的第一个组件
     * @throws NoSuchElementException 如果此向量没有组件
     */
    public synchronized E firstElement() {
        if (elementCount == 0) {
            throw new NoSuchElementException();
        }
        return elementData(0);
    }

    /**
     * 返回此向量的最后一个组件。
     *
     * @return  向量的最后一个组件，即索引为
     *          <code>size()&nbsp;-&nbsp;1</code> 的组件。
     * @throws NoSuchElementException 如果此向量为空
     */
    public synchronized E lastElement() {
        if (elementCount == 0) {
            throw new NoSuchElementException();
        }
        return elementData(elementCount - 1);
    }

    /**
     * 将此向量中指定的 {@code index} 位置的组件设置为指定的对象。该位置的前一个组件将被丢弃。
     *
     * <p>索引必须是一个大于或等于 {@code 0}
     * 且小于当前向量大小的值。
     *
     * <p>此方法在功能上与
     * {@link #set(int, Object) set(int, E)}
     * 方法（它是 {@link List} 接口的一部分）相同。请注意，{@code set} 方法反转了参数的顺序，以更接近数组的使用。请注意，{@code set} 方法返回指定位置存储的旧值。
     *
     * @param      obj     组件要设置为的值
     * @param      index   指定的索引
     * @throws ArrayIndexOutOfBoundsException 如果索引超出范围
     *         ({@code index < 0 || index >= size()})
     */
    public synchronized void setElementAt(E obj, int index) {
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " +
                                                     elementCount);
        }
        elementData[index] = obj;
    }

    /**
     * 删除指定索引处的组件。此向量中索引大于或等于指定
     * {@code index} 的每个组件都会向下移动，使其索引比之前小一个。此向量的大小减少 {@code 1}。
     *
     * <p>索引必须是一个大于或等于 {@code 0}
     * 且小于当前向量大小的值。
     *
     * <p>此方法在功能上与 {@link #remove(int)}
     * 方法（它是 {@link List} 接口的一部分）相同。请注意，{@code remove} 方法返回指定位置存储的旧值。
     *
     * @param      index   要删除的对象的索引
     * @throws ArrayIndexOutOfBoundsException 如果索引超出范围
     *         ({@code index < 0 || index >= size()})
     */
    public synchronized void removeElementAt(int index) {
        modCount++;
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " +
                                                     elementCount);
        }
        else if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        int j = elementCount - index - 1;
        if (j > 0) {
            System.arraycopy(elementData, index + 1, elementData, index, j);
        }
        elementCount--;
        elementData[elementCount] = null; /* to let gc do its work */
    }

    /**
     * 将指定对象作为组件插入到此向量的指定 {@code index} 位置。此向量中索引大于或等于指定
     * {@code index} 的每个组件都会向上移动，使其索引比之前大一个。
     *
     * <p>索引必须是一个大于或等于 {@code 0}
     * 且小于或等于当前向量大小的值。（如果索引等于当前向量的大小，则新元素将被追加到向量中。）
     *
     * <p>此方法在功能上与
     * {@link #add(int, Object) add(int, E)}
     * 方法（它是 {@link List} 接口的一部分）相同。请注意，{@code add} 方法反转了参数的顺序，以更接近数组的使用。
     *
     * @param      obj     要插入的组件
     * @param      index   要插入新组件的位置
     * @throws ArrayIndexOutOfBoundsException 如果索引超出范围
     *         ({@code index < 0 || index > size()})
     */
    public synchronized void insertElementAt(E obj, int index) {
        modCount++;
        if (index > elementCount) {
            throw new ArrayIndexOutOfBoundsException(index
                                                     + " > " + elementCount);
        }
        ensureCapacityHelper(elementCount + 1);
        System.arraycopy(elementData, index, elementData, index + 1, elementCount - index);
        elementData[index] = obj;
        elementCount++;
    }

    /**
     * 将指定的组件添加到此向量的末尾，使其大小增加一个。如果此向量的大小超过其容量，则增加其容量。
     *
     * <p>此方法在功能上与
     * {@link #add(Object) add(E)}
     * 方法（它是 {@link List} 接口的一部分）相同。
     *
     * @param   obj   要添加的组件
     */
    public synchronized void addElement(E obj) {
        modCount++;
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = obj;
    }

    /**
     * 从此向量中删除参数的第一个（最低索引）出现。如果在此向量中找到该对象，则向量中索引大于或等于
     * 该对象索引的每个组件都会向下移动，使其索引比之前小一个。
     *
     * <p>此方法在功能上与
     * {@link #remove(Object)} 方法（它是 {@link List} 接口的一部分）相同。
     *
     * @param   obj   要删除的组件
     * @return  如果参数是此向量的组件，则返回 {@code true}；否则返回 {@code false}。
     */
    public synchronized boolean removeElement(Object obj) {
        modCount++;
        int i = indexOf(obj);
        if (i >= 0) {
            removeElementAt(i);
            return true;
        }
        return false;
    }

    /**
     * 从此向量中删除所有组件，并将其大小设置为零。
     *
     * <p>此方法在功能上与 {@link #clear}
     * 方法（它是 {@link List} 接口的一部分）相同。
     */
    public synchronized void removeAllElements() {
        modCount++;
        // 让 gc 完成其工作
        for (int i = 0; i < elementCount; i++)
            elementData[i] = null;

        elementCount = 0;
    }

    /**
     * 返回此向量的克隆。副本将包含内部数据数组的克隆引用，而不是此 {@code Vector} 对象的原始内部数据数组的引用。
     *
     * @return  此向量的克隆
     */
    public synchronized Object clone() {
        try {
            @SuppressWarnings("unchecked")
                Vector<E> v = (Vector<E>) super.clone();
            v.elementData = Arrays.copyOf(elementData, elementCount);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们是可克隆的
            throw new InternalError(e);
        }
    }

    /**
     * 返回包含此 Vector 中所有元素的数组，顺序正确。
     *
     * @since 1.2
     */
    public synchronized Object[] toArray() {
        return Arrays.copyOf(elementData, elementCount);
    }

    /**
     * 返回包含此 Vector 中所有元素的数组，顺序正确；返回数组的运行时类型是指定数组的运行时类型。如果 Vector 能够放入指定数组，则返回该数组。否则，将分配一个运行时类型为指定数组且大小为此 Vector 的新数组。
     *
     * <p>如果 Vector 能够放入指定数组且有剩余空间
     * （即，数组的元素多于 Vector），
     * 则数组中紧接 Vector 末尾的元素将被设置为 null。这在调用者知道 Vector
     * 不包含任何 null 元素时，有助于确定 Vector 的长度。
     *
     * @param a 要存储 Vector 元素的数组，如果它足够大；否则，将为此目的分配一个具有相同运行时类型的数组。
     * @return 包含 Vector 元素的数组
     * @throws ArrayStoreException 如果 a 的运行时类型不是此 Vector 中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果给定数组为 null
     * @since 1.2
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T[] toArray(T[] a) {
        if (a.length < elementCount)
            return (T[]) Arrays.copyOf(elementData, elementCount, a.getClass());

        System.arraycopy(elementData, 0, a, 0, elementCount);

        if (a.length > elementCount)
            a[elementCount] = null;

        return a;
    }

    // 位置访问操作

    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    /**
     * 返回此 Vector 中指定位置的元素。
     *
     * @param index 要返回的元素的索引
     * @return 指定索引处的对象
     * @throws ArrayIndexOutOfBoundsException 如果索引超出范围
     *            ({@code index < 0 || index >= size()})
     * @since 1.2
     */
    public synchronized E get(int index) {
        if (index >= elementCount)
            throw new ArrayIndexOutOfBoundsException(index);

        return elementData(index);
    }

    /**
     * 将此 Vector 中指定位置的元素替换为指定的元素。
     *
     * @param index 要替换的元素的索引
     * @param element 要存储在指定位置的元素
     * @return 指定位置的前一个元素
     * @throws ArrayIndexOutOfBoundsException 如果索引超出范围
     *         ({@code index < 0 || index >= size()})
     * @since 1.2
     */
    public synchronized E set(int index, E element) {
        if (index >= elementCount)
            throw new ArrayIndexOutOfBoundsException(index);

        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }

    /**
     * 将指定的元素追加到此 Vector 的末尾。
     *
     * @param e 要追加到此 Vector 的元素
     * @return {@code true}（由 {@link Collection#add} 指定）
     * @since 1.2
     */
    public synchronized boolean add(E e) {
        modCount++;
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = e;
        return true;
    }

    /**
     * 从此 Vector 中删除指定元素的第一个出现。如果 Vector 不包含该元素，则保持不变。更
     * 正式地说，删除索引 i 最低的元素，使得
     * {@code (o==null ? get(i)==null : o.equals(get(i)))}（如果存在这样的元素）。
     *
     * @param o 要从此 Vector 中删除的元素，如果存在
     * @return 如果 Vector 包含指定的元素，则返回 true
     * @since 1.2
     */
    public boolean remove(Object o) {
        return removeElement(o);
    }

    /**
     * 在此 Vector 的指定位置插入指定的元素。将当前位于该位置（如果有）的元素及其后续元素向右移动（将其索引加一）。
     *
     * @param index 要插入指定元素的位置
     * @param element 要插入的元素
     * @throws ArrayIndexOutOfBoundsException 如果索引超出范围
     *         ({@code index < 0 || index > size()})
     * @since 1.2
     */
    public void add(int index, E element) {
        insertElementAt(element, index);
    }


                /**
     * 从此 Vector 中移除指定位置的元素。
     * 将后续元素向左移动（从它们的索引中减去一）。返回从 Vector 中移除的元素。
     *
     * @throws ArrayIndexOutOfBoundsException 如果索引超出范围
     *         ({@code index < 0 || index >= size()})
     * @param index 要移除的元素的索引
     * @return 被移除的元素
     * @since 1.2
     */
    public synchronized E remove(int index) {
        modCount++;
        if (index >= elementCount)
            throw new ArrayIndexOutOfBoundsException(index);
        E oldValue = elementData(index);

        int numMoved = elementCount - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--elementCount] = null; // 让垃圾回收器工作

        return oldValue;
    }

    /**
     * 从此 Vector 中移除所有元素。调用此方法返回后，Vector 将为空（除非抛出异常）。
     *
     * @since 1.2
     */
    public void clear() {
        removeAllElements();
    }

    // 批量操作

    /**
     * 如果此 Vector 包含指定 Collection 中的所有元素，则返回 true。
     *
     * @param   c 一个包含要测试的元素的 Collection
     * @return 如果此 Vector 包含指定 Collection 中的所有元素，则返回 true
     * @throws NullPointerException 如果指定的 Collection 为 null
     */
    public synchronized boolean containsAll(Collection<?> c) {
        return super.containsAll(c);
    }

    /**
     * 将指定 Collection 中的所有元素按指定 Collection 的 Iterator 返回的顺序追加到此 Vector 的末尾。
     * 如果在操作进行过程中修改了指定的 Collection，则此操作的行为是未定义的。
     * （这意味着如果指定的 Collection 是此 Vector，并且此 Vector 非空，则此调用的行为是未定义的。）
     *
     * @param c 要插入到此 Vector 中的元素
     * @return 如果此 Vector 因此调用而发生更改，则返回 {@code true}
     * @throws NullPointerException 如果指定的 Collection 为 null
     * @since 1.2
     */
    public synchronized boolean addAll(Collection<? extends E> c) {
        modCount++;
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityHelper(elementCount + numNew);
        System.arraycopy(a, 0, elementData, elementCount, numNew);
        elementCount += numNew;
        return numNew != 0;
    }

    /**
     * 从此 Vector 中移除所有包含在指定 Collection 中的元素。
     *
     * @param c 要从 Vector 中移除的元素的 Collection
     * @return 如果此 Vector 因此调用而发生更改，则返回 true
     * @throws ClassCastException 如果此 Vector 中的一个或多个元素的类型与指定 Collection 不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果此 Vector 包含一个或多个 null 元素，而指定 Collection 不支持 null 元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)，
     *         或者如果指定的 Collection 为 null
     * @since 1.2
     */
    public synchronized boolean removeAll(Collection<?> c) {
        return super.removeAll(c);
    }

    /**
     * 仅保留此 Vector 中包含在指定 Collection 中的元素。换句话说，从此 Vector 中移除所有不包含在指定 Collection 中的元素。
     *
     * @param c 要保留在此 Vector 中的元素的 Collection
     *          （所有其他元素将被移除）
     * @return 如果此 Vector 因此调用而发生更改，则返回 true
     * @throws ClassCastException 如果此 Vector 中的一个或多个元素的类型与指定 Collection 不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果此 Vector 包含一个或多个 null 元素，而指定 Collection 不支持 null 元素
     *         (<a href="Collection.html#optional-restrictions">可选</a>),
     *         或者如果指定的 Collection 为 null
     * @since 1.2
     */
    public synchronized boolean retainAll(Collection<?> c) {
        return super.retainAll(c);
    }

    /**
     * 将指定 Collection 中的所有元素插入到此 Vector 的指定位置。将当前位于该位置（如果有）及其后续元素向右移动
     * （增加它们的索引）。新元素将按指定 Collection 的 Iterator 返回的顺序出现在 Vector 中。
     *
     * @param index 要插入指定 Collection 中第一个元素的位置
     * @param c 要插入到此 Vector 中的元素
     * @return 如果此 Vector 因此调用而发生更改，则返回 {@code true}
     * @throws ArrayIndexOutOfBoundsException 如果索引超出范围
     *         ({@code index < 0 || index > size()})
     * @throws NullPointerException 如果指定的 Collection 为 null
     * @since 1.2
     */
    public synchronized boolean addAll(int index, Collection<? extends E> c) {
        modCount++;
        if (index < 0 || index > elementCount)
            throw new ArrayIndexOutOfBoundsException(index);

        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityHelper(elementCount + numNew);

        int numMoved = elementCount - index;
        if (numMoved > 0)
            System.arraycopy(elementData, index, elementData, index + numNew,
                             numMoved);

        System.arraycopy(a, 0, elementData, index, numNew);
        elementCount += numNew;
        return numNew != 0;
    }

    /**
     * 将指定对象与此 Vector 进行相等性比较。只有当指定对象也是一个 List，并且两个 List 的大小相同，且所有对应的元素对在两个 List 中都 <em>相等</em> 时，返回 true。
     * （两个元素 {@code e1} 和 {@code e2} <em>相等</em> 如果 {@code (e1==null ? e2==null : e1.equals(e2))}。）
     * 换句话说，两个 List 被定义为相等，如果它们包含相同的元素且顺序相同。
     *
     * @param o 要与此 Vector 进行相等性比较的对象
     * @return 如果指定对象与此 Vector 相等，则返回 true
     */
    public synchronized boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * 返回此 Vector 的哈希码值。
     */
    public synchronized int hashCode() {
        return super.hashCode();
    }

    /**
     * 返回此 Vector 的字符串表示形式，包含每个元素的字符串表示形式。
     */
    public synchronized String toString() {
        return super.toString();
    }

    /**
     * 返回此 List 从 fromIndex（包含）到 toIndex（不包含）之间的部分视图。（如果 fromIndex 和 toIndex 相等，则返回的 List 为空。）
     * 返回的 List 由此 List 支持，因此返回的 List 中的更改会反映在此 List 中，反之亦然。返回的 List 支持此 List 支持的所有可选 List 操作。
     *
     * <p>此方法消除了显式范围操作的需要（通常数组中存在此类操作）。任何期望 List 的操作都可以作为范围操作通过操作子列表视图来实现，而不是整个 List。
     * 例如，以下惯用法从 List 中移除一个范围的元素：
     * <pre>
     *      list.subList(from, to).clear();
     * </pre>
     * 类似的惯用法可以用于 indexOf 和 lastIndexOf，Collections 类中的所有算法都可以应用于子列表。
     *
     * <p>如果以任何方式结构性地修改了支持的 List（即，此 List），则返回的 List 的语义变得未定义。
     * （结构性修改是指改变 List 的大小，或者以其他方式扰乱 List，使得正在进行的迭代可能产生不正确的结果。）
     *
     * @param fromIndex 子列表的低端点（包含）
     * @param toIndex 子列表的高端点（不包含）
     * @return 此 List 中指定范围的视图
     * @throws IndexOutOfBoundsException 如果端点索引值超出范围
     *         {@code (fromIndex < 0 || toIndex > size)}
     * @throws IllegalArgumentException 如果端点索引顺序错误
     *         {@code (fromIndex > toIndex)}
     */
    public synchronized List<E> subList(int fromIndex, int toIndex) {
        return Collections.synchronizedList(super.subList(fromIndex, toIndex),
                                            this);
    }

    /**
     * 从此列表中移除索引在 {@code fromIndex}（包含）和 {@code toIndex}（不包含）之间的所有元素。
     * 将后续元素向左移动（减少它们的索引）。此调用将列表缩短 {@code (toIndex - fromIndex)} 个元素。
     * （如果 {@code toIndex==fromIndex}，此操作没有效果。）
     */
    protected synchronized void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = elementCount - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                         numMoved);

        // 让垃圾回收器工作
        int newElementCount = elementCount - (toIndex-fromIndex);
        while (elementCount != newElementCount)
            elementData[--elementCount] = null;
    }

    /**
     * 从流中加载一个 {@code Vector} 实例（即，反序列化它）。
     * 此方法执行检查以确保字段的一致性。
     *
     * @param in 流
     * @throws java.io.IOException 如果发生 I/O 错误
     * @throws ClassNotFoundException 如果流包含不存在类的数据
     */
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gfields = in.readFields();
        int count = gfields.get("elementCount", 0);
        Object[] data = (Object[])gfields.get("elementData", null);
        if (count < 0 || data == null || count > data.length) {
            throw new StreamCorruptedException("Inconsistent vector internals");
        }
        elementCount = count;
        elementData = data.clone();
    }

    /**
     * 将 {@code Vector} 实例的状态保存到流中（即，序列化它）。
     * 此方法执行同步以确保序列化数据的一致性。
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        final java.io.ObjectOutputStream.PutField fields = s.putFields();
        final Object[] data;
        synchronized (this) {
            fields.put("capacityIncrement", capacityIncrement);
            fields.put("elementCount", elementCount);
            data = elementData.clone();
        }
        fields.put("elementData", data);
        s.writeFields();
    }

    /**
     * 返回一个列表迭代器，按正确顺序遍历此列表中的元素，从列表中的指定位置开始。
     * 指定的索引表示初始调用 {@link ListIterator#next next} 时将返回的第一个元素。
     * 初始调用 {@link ListIterator#previous previous} 将返回指定索引减一的元素。
     *
     * <p>返回的列表迭代器是 <a href="#fail-fast"><i>快速失败</i></a> 的。
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public synchronized ListIterator<E> listIterator(int index) {
        if (index < 0 || index > elementCount)
            throw new IndexOutOfBoundsException("Index: "+index);
        return new ListItr(index);
    }

    /**
     * 返回一个列表迭代器，按正确顺序遍历此列表中的元素。
     *
     * <p>返回的列表迭代器是 <a href="#fail-fast"><i>快速失败</i></a> 的。
     *
     * @see #listIterator(int)
     */
    public synchronized ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    /**
     * 返回一个迭代器，按正确顺序遍历此列表中的元素。
     *
     * <p>返回的迭代器是 <a href="#fail-fast"><i>快速失败</i></a> 的。
     *
     * @return 按正确顺序遍历此列表中元素的迭代器
     */
    public synchronized Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * AbstractList.Itr 的优化版本
     */
    private class Itr implements Iterator<E> {
        int cursor;       // 下一个要返回的元素的索引
        int lastRet = -1; // 最后返回的元素的索引；如果没有这样的元素，则为 -1
        int expectedModCount = modCount;

        public boolean hasNext() {
            // 虽然存在竞争条件，但在 next/previous 调用中或之后进行同步检查时仍在规范范围内
            return cursor != elementCount;
        }

        public E next() {
            synchronized (Vector.this) {
                checkForComodification();
                int i = cursor;
                if (i >= elementCount)
                    throw new NoSuchElementException();
                cursor = i + 1;
                return elementData(lastRet = i);
            }
        }

        public void remove() {
            if (lastRet == -1)
                throw new IllegalStateException();
            synchronized (Vector.this) {
                checkForComodification();
                Vector.this.remove(lastRet);
                expectedModCount = modCount;
            }
            cursor = lastRet;
            lastRet = -1;
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            synchronized (Vector.this) {
                final int size = elementCount;
                int i = cursor;
                if (i >= size) {
                    return;
                }
        @SuppressWarnings("unchecked")
                final E[] elementData = (E[]) Vector.this.elementData;
                if (i >= elementData.length) {
                    throw new ConcurrentModificationException();
                }
                while (i != size && modCount == expectedModCount) {
                    action.accept(elementData[i++]);
                }
                // 在迭代结束时更新一次，以减少堆写入流量
                cursor = i;
                lastRet = i - 1;
                checkForComodification();
            }
        }


                    final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * AbstractList.ListItr 的优化版本。
     */
    final class ListItr extends Itr implements ListIterator<E> {
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

        public E previous() {
            synchronized (Vector.this) {
                checkForComodification();
                int i = cursor - 1;
                if (i < 0)
                    throw new NoSuchElementException();
                cursor = i;
                return elementData(lastRet = i);
            }
        }

        public void set(E e) {
            if (lastRet == -1)
                throw new IllegalStateException();
            synchronized (Vector.this) {
                checkForComodification();
                Vector.this.set(lastRet, e);
            }
        }

        public void add(E e) {
            int i = cursor;
            synchronized (Vector.this) {
                checkForComodification();
                Vector.this.add(i, e);
                expectedModCount = modCount;
            }
            cursor = i + 1;
            lastRet = -1;
        }
    }

    @Override
    public synchronized void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData;
        final int elementCount = this.elementCount;
        for (int i=0; modCount == expectedModCount && i < elementCount; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        // 确定要删除的元素
        // 在此阶段，从过滤器谓词抛出的任何异常
        // 都将使集合保持不变
        int removeCount = 0;
        final int size = elementCount;
        final BitSet removeSet = new BitSet(size);
        final int expectedModCount = modCount;
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

        // 将幸存的元素向左移动，覆盖被删除元素留下的空位
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
            elementCount = newSize;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++;
        }

        return anyToRemove;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount;
        final int size = elementCount;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            elementData[i] = operator.apply((E) elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount;
        Arrays.sort((E[]) elementData, 0, elementCount, c);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    /**
     * 创建一个 <em><a href="Spliterator.html#binding">延迟绑定</a></em>
     * 和 <em>快速失败</em> 的 {@link Spliterator}，用于遍历此列表中的元素。
     *
     * <p>{@code Spliterator} 报告 {@link Spliterator#SIZED}，
     * {@link Spliterator#SUBSIZED} 和 {@link Spliterator#ORDERED}。
     * 覆盖实现应记录额外的特性值。
     *
     * @return 一个 {@code Spliterator}，用于遍历此列表中的元素
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new VectorSpliterator<>(this, null, 0, -1, 0);
    }

    /** 类似于 ArrayList 的 Spliterator */
    static final class VectorSpliterator<E> implements Spliterator<E> {
        private final Vector<E> list;
        private Object[] array;
        private int index; // 当前索引，前进/拆分时修改
        private int fence; // -1 直到使用；然后是最后一个索引的后一个位置
        private int expectedModCount; // 在设置 fence 时初始化

        /** 创建新的 spliterator，覆盖给定范围 */
        VectorSpliterator(Vector<E> list, Object[] array, int origin, int fence,
                          int expectedModCount) {
            this.list = list;
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // 在首次使用时初始化
            int hi;
            if ((hi = fence) < 0) {
                synchronized(list) {
                    array = list.elementData;
                    expectedModCount = list.modCount;
                    hi = fence = list.elementCount;
                }
            }
            return hi;
        }

        public Spliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                new VectorSpliterator<E>(list, array, lo, index = mid,
                                         expectedModCount);
        }

        @SuppressWarnings("unchecked")
        public boolean tryAdvance(Consumer<? super E> action) {
            int i;
            if (action == null)
                throw new NullPointerException();
            if (getFence() > (i = index)) {
                index = i + 1;
                action.accept((E)array[i]);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi; // 提取访问和检查，避免循环中重复
            Vector<E> lst; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null) {
                if ((hi = fence) < 0) {
                    synchronized(lst) {
                        expectedModCount = lst.modCount;
                        a = array = lst.elementData;
                        hi = fence = lst.elementCount;
                    }
                }
                else
                    a = array;
                if (a != null && (i = index) >= 0 && (index = hi) <= a.length) {
                    while (i < hi)
                        action.accept((E) a[i++]);
                    if (lst.modCount == expectedModCount)
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
}
