
/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group.  Adapted and released, under explicit permission,
 * from JDK ArrayList.java which carries the following copyright:
 *
 * Copyright 1997 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

package java.util.concurrent;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import sun.misc.SharedSecrets;

/**
 * {@link java.util.ArrayList} 的线程安全变体，其中所有可变操作（如 {@code add}、{@code set} 等）都是通过制作底层数组的新副本实现的。
 *
 * <p>通常这种做法成本较高，但在遍历操作远远多于修改操作时，可能比其他替代方案更高效。当您不能或不想同步遍历操作，但需要防止并发线程之间的干扰时，这种做法也很有用。"快照"风格的迭代器方法使用创建迭代器时数组状态的引用。此数组在迭代器的生命周期内不会改变，因此不可能发生干扰，迭代器也不会抛出 {@code ConcurrentModificationException}。迭代器不会反映自创建以来对列表的添加、删除或更改。迭代器本身（如 {@code remove}、{@code set} 和 {@code add}）的元素更改操作不受支持，这些方法会抛出 {@code UnsupportedOperationException}。
 *
 * <p>允许所有元素，包括 {@code null}。
 *
 * <p>内存一致性效果：与其他并发集合一样，一个线程在将对象放入 {@code CopyOnWriteArrayList} 之前的操作 <a href="package-summary.html#MemoryVisibility"><i>先于</i></a> 另一个线程访问或移除此元素之后的操作。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public class CopyOnWriteArrayList<E>
    implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
    private static final long serialVersionUID = 8673264195747942595L;

    /** 保护所有可变操作的锁 */
    final transient ReentrantLock lock = new ReentrantLock();

    /** 仅通过 getArray/setArray 访问的数组。 */
    private transient volatile Object[] array;

    /**
     * 获取数组。非私有以便也能从 CopyOnWriteArraySet 类访问。
     */
    final Object[] getArray() {
        return array;
    }

    /**
     * 设置数组。
     */
    final void setArray(Object[] a) {
        array = a;
    }

    /**
     * 创建一个空列表。
     */
    public CopyOnWriteArrayList() {
        setArray(new Object[0]);
    }

    /**
     * 创建一个包含指定集合元素的列表，顺序为集合迭代器返回的顺序。
     *
     * @param c 初始持有的元素集合
     * @throws NullPointerException 如果指定的集合为 null
     */
    public CopyOnWriteArrayList(Collection<? extends E> c) {
        Object[] elements;
        if (c.getClass() == CopyOnWriteArrayList.class)
            elements = ((CopyOnWriteArrayList<?>)c).getArray();
        else {
            elements = c.toArray();
            if (c.getClass() != ArrayList.class)
                elements = Arrays.copyOf(elements, elements.length, Object[].class);
        }
        setArray(elements);
    }

    /**
     * 创建一个持有给定数组副本的列表。
     *
     * @param toCopyIn 要复制的数组（此数组的副本用作内部数组）
     * @throws NullPointerException 如果指定的数组为 null
     */
    public CopyOnWriteArrayList(E[] toCopyIn) {
        setArray(Arrays.copyOf(toCopyIn, toCopyIn.length, Object[].class));
    }

    /**
     * 返回此列表中的元素数量。
     *
     * @return 此列表中的元素数量
     */
    public int size() {
        return getArray().length;
    }

    /**
     * 如果此列表不包含任何元素，则返回 {@code true}。
     *
     * @return 如果此列表不包含任何元素，则返回 {@code true}
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * 测试相等性，处理 null。
     */
    private static boolean eq(Object o1, Object o2) {
        return (o1 == null) ? o2 == null : o1.equals(o2);
    }

    /**
     * indexOf 的静态版本，允许在不需每次重新获取数组的情况下重复调用。
     * @param o 要搜索的元素
     * @param elements 数组
     * @param index 要搜索的第一个索引
     * @param fence 要搜索的最后一个索引之后的一个位置
     * @return 元素的索引，如果不存在则返回 -1
     */
    private static int indexOf(Object o, Object[] elements,
                               int index, int fence) {
        if (o == null) {
            for (int i = index; i < fence; i++)
                if (elements[i] == null)
                    return i;
        } else {
            for (int i = index; i < fence; i++)
                if (o.equals(elements[i]))
                    return i;
        }
        return -1;
    }


                    /**
     * 静态版本的 lastIndexOf 方法。
     * @param o 要搜索的元素
     * @param elements 数组
     * @param index 开始搜索的第一个索引
     * @return 元素的索引，如果不存在则返回 -1
     */
    private static int lastIndexOf(Object o, Object[] elements, int index) {
        if (o == null) {
            for (int i = index; i >= 0; i--)
                if (elements[i] == null)
                    return i;
        } else {
            for (int i = index; i >= 0; i--)
                if (o.equals(elements[i]))
                    return i;
        }
        return -1;
    }

    /**
     * 如果此列表包含指定的元素，则返回 {@code true}。
     * 更正式地说，当且仅当此列表包含至少一个元素 {@code e} 使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt> 时，返回 {@code true}。
     *
     * @param o 要测试其在列表中是否存在性的元素
     * @return 如果此列表包含指定的元素，则返回 {@code true}
     */
    public boolean contains(Object o) {
        Object[] elements = getArray();
        return indexOf(o, elements, 0, elements.length) >= 0;
    }

    /**
     * {@inheritDoc}
     */
    public int indexOf(Object o) {
        Object[] elements = getArray();
        return indexOf(o, elements, 0, elements.length);
    }

    /**
     * 返回此列表中指定元素第一次出现的索引，从 {@code index} 开始向前搜索，如果未找到该元素，则返回 -1。
     * 更正式地说，返回最低索引 {@code i} 使得
     * <tt>(i&nbsp;&gt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(e==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;e.equals(get(i))))</tt>，
     * 或者如果没有这样的索引，则返回 -1。
     *
     * @param e 要搜索的元素
     * @param index 开始搜索的索引
     * @return 从位置 {@code index} 或其后的列表中第一次出现的元素的索引；
     *         如果未找到该元素，则返回 {@code -1}。
     * @throws IndexOutOfBoundsException 如果指定的索引为负数
     */
    public int indexOf(E e, int index) {
        Object[] elements = getArray();
        return indexOf(e, elements, index, elements.length);
    }

    /**
     * {@inheritDoc}
     */
    public int lastIndexOf(Object o) {
        Object[] elements = getArray();
        return lastIndexOf(o, elements, elements.length - 1);
    }

    /**
     * 返回此列表中指定元素最后一次出现的索引，从 {@code index} 开始向后搜索，如果未找到该元素，则返回 -1。
     * 更正式地说，返回最高索引 {@code i} 使得
     * <tt>(i&nbsp;&lt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(e==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;e.equals(get(i))))</tt>，
     * 或者如果没有这样的索引，则返回 -1。
     *
     * @param e 要搜索的元素
     * @param index 开始向后搜索的索引
     * @return 从位置小于或等于 {@code index} 的列表中最后一次出现的元素的索引；
     *         如果未找到该元素，则返回 -1。
     * @throws IndexOutOfBoundsException 如果指定的索引大于或等于此列表的当前大小
     */
    public int lastIndexOf(E e, int index) {
        Object[] elements = getArray();
        return lastIndexOf(e, elements, index);
    }

    /**
     * 返回此列表的浅拷贝。（元素本身不被复制。）
     *
     * @return 此列表的克隆
     */
    public Object clone() {
        try {
            @SuppressWarnings("unchecked")
            CopyOnWriteArrayList<E> clone =
                (CopyOnWriteArrayList<E>) super.clone();
            clone.resetLock();
            return clone;
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们实现了 Cloneable 接口
            throw new InternalError();
        }
    }

    /**
     * 返回一个包含此列表中所有元素的数组（从第一个元素到最后一个元素）。
     *
     * <p>返回的数组是“安全的”，即此列表不维护对其的任何引用。（换句话说，此方法必须分配一个新数组）。调用者因此可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组和基于集合的 API 之间的桥梁。
     *
     * @return 包含此列表中所有元素的数组
     */
    public Object[] toArray() {
        Object[] elements = getArray();
        return Arrays.copyOf(elements, elements.length);
    }

    /**
     * 返回一个包含此列表中所有元素的数组（从第一个元素到最后一个元素）；返回数组的运行时类型是指定数组的运行时类型。如果列表适合指定的数组，则返回该数组。否则，分配一个运行时类型为指定数组且大小为此列表大小的新数组。
     *
     * <p>如果此列表适合指定的数组且有剩余空间（即，数组的元素多于此列表的元素），则数组中紧接列表末尾的元素被设置为
     * {@code null}。（这仅在调用者知道此列表不包含任何 null 元素时，用于确定此列表的长度。）
     *
     * <p>像 {@link #toArray()} 方法一样，此方法充当基于数组和基于集合的 API 之间的桥梁。此外，此方法允许对输出数组的运行时类型进行精确控制，并且在某些情况下，可以用于节省分配成本。
     *
     * <p>假设 {@code x} 是一个已知仅包含字符串的列表。可以使用以下代码将列表转储到新分配的 {@code String} 数组中：
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * 注意，{@code toArray(new Object[0])} 在功能上等同于 {@code toArray()}。
     *
     * @param a 如果足够大，则将列表的元素存储在此数组中；否则，为此目的分配一个相同运行时类型的新数组。
     * @return 包含此列表中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此列表中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T a[]) {
        Object[] elements = getArray();
        int len = elements.length;
        if (a.length < len)
            return (T[]) Arrays.copyOf(elements, len, a.getClass());
        else {
            System.arraycopy(elements, 0, a, 0, len);
            if (a.length > len)
                a[len] = null;
            return a;
        }
    }

                    // 位置访问操作

    @SuppressWarnings("unchecked")
    private E get(Object[] a, int index) {
        return (E) a[index];
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        return get(getArray(), index);
    }

    /**
     * 用指定的元素替换此列表中指定位置的元素。
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E set(int index, E element) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            E oldValue = get(elements, index);

            if (oldValue != element) {
                int len = elements.length;
                Object[] newElements = Arrays.copyOf(elements, len);
                newElements[index] = element;
                setArray(newElements);
            } else {
                // 不完全是空操作；确保了 volatile 写语义
                setArray(elements);
            }
            return oldValue;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将指定的元素添加到此列表的末尾。
     *
     * @param e 要添加到此列表的元素
     * @return {@code true}（如 {@link Collection#add} 所指定）
     */
    public boolean add(E e) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            Object[] newElements = Arrays.copyOf(elements, len + 1);
            newElements[len] = e;
            setArray(newElements);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 在此列表的指定位置插入指定的元素。将当前位置的元素（如果有）和任何后续元素向右移动（增加它们的索引）。
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, E element) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            if (index > len || index < 0)
                throw new IndexOutOfBoundsException("Index: "+index+
                                                    ", Size: "+len);
            Object[] newElements;
            int numMoved = len - index;
            if (numMoved == 0)
                newElements = Arrays.copyOf(elements, len + 1);
            else {
                newElements = new Object[len + 1];
                System.arraycopy(elements, 0, newElements, 0, index);
                System.arraycopy(elements, index, newElements, index + 1,
                                 numMoved);
            }
            newElements[index] = element;
            setArray(newElements);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 从此列表中删除指定位置的元素。将任何后续元素向左移动（减少它们的索引）。返回从列表中删除的元素。
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E remove(int index) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            E oldValue = get(elements, index);
            int numMoved = len - index - 1;
            if (numMoved == 0)
                setArray(Arrays.copyOf(elements, len - 1));
            else {
                Object[] newElements = new Object[len - 1];
                System.arraycopy(elements, 0, newElements, 0, index);
                System.arraycopy(elements, index + 1, newElements, index,
                                 numMoved);
                setArray(newElements);
            }
            return oldValue;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 如果此列表包含指定的元素，则移除其第一次出现。如果此列表不包含该元素，则保持不变。更正式地说，移除索引最低的
     * {@code i} 使得
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * （如果存在这样的元素）。如果此列表包含指定的元素（或等效地，如果此列表因调用而改变），则返回 {@code true}。
     *
     * @param o 要从此列表中移除的元素，如果存在
     * @return 如果此列表包含指定的元素，则返回 {@code true}
     */
    public boolean remove(Object o) {
        Object[] snapshot = getArray();
        int index = indexOf(o, snapshot, 0, snapshot.length);
        return (index < 0) ? false : remove(o, snapshot, index);
    }

    /**
     * 使用给定的最近快照包含 o 在给定索引的强烈提示的 remove(Object) 版本。
     */
    private boolean remove(Object o, Object[] snapshot, int index) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] current = getArray();
            int len = current.length;
            if (snapshot != current) findIndex: {
                int prefix = Math.min(index, len);
                for (int i = 0; i < prefix; i++) {
                    if (current[i] != snapshot[i] && eq(o, current[i])) {
                        index = i;
                        break findIndex;
                    }
                }
                if (index >= len)
                    return false;
                if (current[index] == o)
                    break findIndex;
                index = indexOf(o, current, index, len);
                if (index < 0)
                    return false;
            }
            Object[] newElements = new Object[len - 1];
            System.arraycopy(current, 0, newElements, 0, index);
            System.arraycopy(current, index + 1,
                             newElements, index,
                             len - index - 1);
            setArray(newElements);
            return true;
        } finally {
            lock.unlock();
        }
    }


                    /**
     * 从该列表中移除所有索引介于 {@code fromIndex}（包含）和 {@code toIndex}（不包含）之间的元素。
     * 将后续的元素向左移动（减少它们的索引）。
     * 此调用将列表缩短 {@code (toIndex - fromIndex)} 个元素。
     * （如果 {@code toIndex==fromIndex}，此操作没有效果。）
     *
     * @param fromIndex 要移除的第一个元素的索引
     * @param toIndex 要移除的最后一个元素之后的索引
     * @throws IndexOutOfBoundsException 如果 fromIndex 或 toIndex 超出范围
     *         ({@code fromIndex < 0 || toIndex > size() || toIndex < fromIndex})
     */
    void removeRange(int fromIndex, int toIndex) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;

            if (fromIndex < 0 || toIndex > len || toIndex < fromIndex)
                throw new IndexOutOfBoundsException();
            int newlen = len - (toIndex - fromIndex);
            int numMoved = len - toIndex;
            if (numMoved == 0)
                setArray(Arrays.copyOf(elements, newlen));
            else {
                Object[] newElements = new Object[newlen];
                System.arraycopy(elements, 0, newElements, 0, fromIndex);
                System.arraycopy(elements, toIndex, newElements,
                                 fromIndex, numMoved);
                setArray(newElements);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 如果元素不在列表中，则追加该元素。
     *
     * @param e 如果不存在，则要添加到此列表的元素
     * @return 如果元素被添加，则返回 {@code true}
     */
    public boolean addIfAbsent(E e) {
        Object[] snapshot = getArray();
        return indexOf(e, snapshot, 0, snapshot.length) >= 0 ? false :
            addIfAbsent(e, snapshot);
    }

    /**
     * 使用强提示的 addIfAbsent 版本，即给定的最近快照不包含 e。
     */
    private boolean addIfAbsent(E e, Object[] snapshot) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] current = getArray();
            int len = current.length;
            if (snapshot != current) {
                // 优化以应对与另一个 addXXX 操作的竞争
                int common = Math.min(snapshot.length, len);
                for (int i = 0; i < common; i++)
                    if (current[i] != snapshot[i] && eq(e, current[i]))
                        return false;
                if (indexOf(e, current, common, len) >= 0)
                        return false;
            }
            Object[] newElements = Arrays.copyOf(current, len + 1);
            newElements[len] = e;
            setArray(newElements);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 如果此列表包含指定集合中的所有元素，则返回 {@code true}。
     *
     * @param c 要检查是否包含在此列表中的集合
     * @return 如果此列表包含指定集合中的所有元素，则返回 {@code true}
     * @throws NullPointerException 如果指定的集合为 null
     * @see #contains(Object)
     */
    public boolean containsAll(Collection<?> c) {
        Object[] elements = getArray();
        int len = elements.length;
        for (Object e : c) {
            if (indexOf(e, elements, 0, len) < 0)
                return false;
        }
        return true;
    }

    /**
     * 从该列表中移除包含在指定集合中的所有元素。由于需要内部临时数组，此操作在此类中特别昂贵。
     *
     * @param c 包含要从此列表中移除的元素的集合
     * @return 如果此列表因调用而改变，则返回 {@code true}
     * @throws ClassCastException 如果此列表中元素的类与指定集合不兼容
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果此列表包含 null 元素而指定集合不允许 null 元素
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)，
     *         或者指定的集合为 null
     * @see #remove(Object)
     */
    public boolean removeAll(Collection<?> c) {
        if (c == null) throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            if (len != 0) {
                // 临时数组保存我们知道要保留的元素
                int newlen = 0;
                Object[] temp = new Object[len];
                for (int i = 0; i < len; ++i) {
                    Object element = elements[i];
                    if (!c.contains(element))
                        temp[newlen++] = element;
                }
                if (newlen != len) {
                    setArray(Arrays.copyOf(temp, newlen));
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 仅保留此列表中包含在指定集合中的元素。换句话说，从此列表中移除所有不包含在指定集合中的元素。
     *
     * @param c 包含要在此列表中保留的元素的集合
     * @return 如果此列表因调用而改变，则返回 {@code true}
     * @throws ClassCastException 如果此列表中元素的类与指定集合不兼容
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果此列表包含 null 元素而指定集合不允许 null 元素
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)，
     *         或者指定的集合为 null
     * @see #remove(Object)
     */
    public boolean retainAll(Collection<?> c) {
        if (c == null) throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            if (len != 0) {
                // 临时数组保存我们知道要保留的元素
                int newlen = 0;
                Object[] temp = new Object[len];
                for (int i = 0; i < len; ++i) {
                    Object element = elements[i];
                    if (c.contains(element))
                        temp[newlen++] = element;
                }
                if (newlen != len) {
                    setArray(Arrays.copyOf(temp, newlen));
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

                    /**
     * 将指定集合中未包含在此列表中的所有元素，按照指定集合的迭代器返回的顺序，追加到此列表的末尾。
     *
     * @param c 包含要添加到此列表的元素的集合
     * @return 添加的元素数量
     * @throws NullPointerException 如果指定的集合为 null
     * @see #addIfAbsent(Object)
     */
    public int addAllAbsent(Collection<? extends E> c) {
        Object[] cs = c.toArray();
        if (c.getClass() != ArrayList.class) {
            cs = cs.clone();
        }
        if (cs.length == 0)
            return 0;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            int added = 0;
            // 在 cs 中去重并压缩元素
            for (int i = 0; i < cs.length; ++i) {
                Object e = cs[i];
                if (indexOf(e, elements, 0, len) < 0 &&
                    indexOf(e, cs, 0, added) < 0)
                    cs[added++] = e;
            }
            if (added > 0) {
                Object[] newElements = Arrays.copyOf(elements, len + added);
                System.arraycopy(cs, 0, newElements, len, added);
                setArray(newElements);
            }
            return added;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 从此列表中移除所有元素。
     * 该方法调用后，列表将为空。
     */
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            setArray(new Object[0]);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将指定集合中的所有元素按照指定集合的迭代器返回的顺序追加到此列表的末尾。
     *
     * @param c 包含要添加到此列表的元素的集合
     * @return 如果此列表因调用而发生更改，则返回 {@code true}
     * @throws NullPointerException 如果指定的集合为 null
     * @see #add(Object)
     */
    public boolean addAll(Collection<? extends E> c) {
        Object[] cs = (c.getClass() == CopyOnWriteArrayList.class) ?
            ((CopyOnWriteArrayList<?>)c).getArray() : c.toArray();
        if (cs.length == 0)
            return false;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            if (len == 0 && (c.getClass() == CopyOnWriteArrayList.class ||
                             c.getClass() == ArrayList.class)) {
                setArray(cs);
            } else {
                Object[] newElements = Arrays.copyOf(elements, len + cs.length);
                System.arraycopy(cs, 0, newElements, len, cs.length);
                setArray(newElements);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 从指定位置开始，将指定集合中的所有元素插入到此列表中。将当前位置的元素（如果有）及其后续元素向右移动（增加它们的索引）。新元素将按照指定集合的迭代器返回的顺序出现在此列表中。
     *
     * @param index 指定集合中第一个元素要插入的位置
     * @param c 包含要添加到此列表的元素的集合
     * @return 如果此列表因调用而发生更改，则返回 {@code true}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException 如果指定的集合为 null
     * @see #add(int,Object)
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        Object[] cs = c.toArray();
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            if (index > len || index < 0)
                throw new IndexOutOfBoundsException("Index: "+index+
                                                    ", Size: "+len);
            if (cs.length == 0)
                return false;
            int numMoved = len - index;
            Object[] newElements;
            if (numMoved == 0)
                newElements = Arrays.copyOf(elements, len + cs.length);
            else {
                newElements = new Object[len + cs.length];
                System.arraycopy(elements, 0, newElements, 0, index);
                System.arraycopy(elements, index,
                                 newElements, index + cs.length,
                                 numMoved);
            }
            System.arraycopy(cs, 0, newElements, index, cs.length);
            setArray(newElements);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void forEach(Consumer<? super E> action) {
        if (action == null) throw new NullPointerException();
        Object[] elements = getArray();
        int len = elements.length;
        for (int i = 0; i < len; ++i) {
            @SuppressWarnings("unchecked") E e = (E) elements[i];
            action.accept(e);
        }
    }

    public boolean removeIf(Predicate<? super E> filter) {
        if (filter == null) throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            if (len != 0) {
                int newlen = 0;
                Object[] temp = new Object[len];
                for (int i = 0; i < len; ++i) {
                    @SuppressWarnings("unchecked") E e = (E) elements[i];
                    if (!filter.test(e))
                        temp[newlen++] = e;
                }
                if (newlen != len) {
                    setArray(Arrays.copyOf(temp, newlen));
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }


public void replaceAll(UnaryOperator<E> operator) {
    if (operator == null) throw new NullPointerException();
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        Object[] elements = getArray();
        int len = elements.length;
        Object[] newElements = Arrays.copyOf(elements, len);
        for (int i = 0; i < len; ++i) {
            @SuppressWarnings("unchecked") E e = (E) elements[i];
            newElements[i] = operator.apply(e);
        }
        setArray(newElements);
    } finally {
        lock.unlock();
    }
}

public void sort(Comparator<? super E> c) {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        Object[] elements = getArray();
        Object[] newElements = Arrays.copyOf(elements, elements.length);
        @SuppressWarnings("unchecked") E[] es = (E[])newElements;
        Arrays.sort(es, c);
        setArray(newElements);
    } finally {
        lock.unlock();
    }
}

/**
 * 将此列表保存到流中（即序列化它）。
 *
 * @param s 流
 * @throws java.io.IOException 如果发生 I/O 错误
 * @serialData 支持列表的数组的长度被发出
 *               （int），然后是所有元素（每个都是 Object）
 *               按正确顺序。
 */
private void writeObject(java.io.ObjectOutputStream s)
    throws java.io.IOException {

    s.defaultWriteObject();

    Object[] elements = getArray();
    // 写出数组长度
    s.writeInt(elements.length);

    // 按正确顺序写出所有元素。
    for (Object element : elements)
        s.writeObject(element);
}

/**
 * 从流中重建此列表（即反序列化它）。
 * @param s 流
 * @throws ClassNotFoundException 如果找不到序列化对象的类
 * @throws java.io.IOException 如果发生 I/O 错误
 */
private void readObject(java.io.ObjectInputStream s)
    throws java.io.IOException, ClassNotFoundException {

    s.defaultReadObject();

    // 绑定到新锁
    resetLock();

    // 读取数组长度并分配数组
    int len = s.readInt();
    SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, len);
    Object[] elements = new Object[len];

    // 按正确顺序读取所有元素。
    for (int i = 0; i < len; i++)
        elements[i] = s.readObject();
    setArray(elements);
}

/**
 * 返回此列表的字符串表示形式。字符串
 * 表示形式由列表元素的字符串表示形式组成，按其迭代器返回的顺序排列，
 * 用方括号 ({@code "[]"}) 包围。相邻元素由
 * 字符 {@code ", "}（逗号和空格）分隔。元素
 * 转换为字符串，如同 {@link String#valueOf(Object)}。
 *
 * @return 此列表的字符串表示形式
 */
public String toString() {
    return Arrays.toString(getArray());
}

/**
 * 将指定对象与此列表进行相等性比较。
 * 如果指定对象与此对象相同，或者它也是一个 {@link List} 并且
 * 指定列表的 {@linkplain List#iterator() 迭代器} 返回的元素序列
 * 与此列表的迭代器返回的序列相同，则返回 {@code true}。如果两个序列
 * 长度相同且序列中相应位置的元素 <em>相等</em>，则认为这两个序列
 * 是相同的。如果 {@code (e1==null ? e2==null : e1.equals(e2))}，则认为
 * 两个元素 {@code e1} 和 {@code e2} <em>相等</em>。
 *
 * @param o 要与此列表进行相等性比较的对象
 * @return 如果指定对象等于此列表，则返回 {@code true}
 */
public boolean equals(Object o) {
    if (o == this)
        return true;
    if (!(o instanceof List))
        return false;

    List<?> list = (List<?>)(o);
    Iterator<?> it = list.iterator();
    Object[] elements = getArray();
    int len = elements.length;
    for (int i = 0; i < len; ++i)
        if (!it.hasNext() || !eq(elements[i], it.next()))
            return false;
    if (it.hasNext())
        return false;
    return true;
}

/**
 * 返回此列表的哈希码值。
 *
 * <p>此实现使用 {@link List#hashCode} 中的定义。
 *
 * @return 此列表的哈希码值
 */
public int hashCode() {
    int hashCode = 1;
    Object[] elements = getArray();
    int len = elements.length;
    for (int i = 0; i < len; ++i) {
        Object obj = elements[i];
        hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
    }
    return hashCode;
}

/**
 * 返回一个按正确顺序遍历此列表元素的迭代器。
 *
 * <p>返回的迭代器提供了迭代器构造时列表状态的快照。遍历迭代器时不需要同步。迭代器
 * <em>不支持</em> {@code remove} 方法。
 *
 * @return 按正确顺序遍历此列表元素的迭代器
 */
public Iterator<E> iterator() {
    return new COWIterator<E>(getArray(), 0);
}

/**
 * {@inheritDoc}
 *
 * <p>返回的迭代器提供了迭代器构造时列表状态的快照。遍历迭代器时不需要同步。迭代器
 * <em>不支持</em> {@code remove}、{@code set} 或 {@code add} 方法。
 */
public ListIterator<E> listIterator() {
    return new COWIterator<E>(getArray(), 0);
}

/**
 * {@inheritDoc}
 *
 * <p>返回的迭代器提供了迭代器构造时列表状态的快照。遍历迭代器时不需要同步。迭代器
 * <em>不支持</em> {@code remove}、{@code set} 或 {@code add} 方法。
 *
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public ListIterator<E> listIterator(int index) {
    Object[] elements = getArray();
    int len = elements.length;
    if (index < 0 || index > len)
        throw new IndexOutOfBoundsException("Index: "+index);


                        return new COWIterator<E>(elements, index);
    }

    /**
     * 返回此列表中的元素的 {@link Spliterator}。
     *
     * <p>此 {@code Spliterator} 报告 {@link Spliterator#IMMUTABLE}，
     * {@link Spliterator#ORDERED}，{@link Spliterator#SIZED} 和
     * {@link Spliterator#SUBSIZED}。
     *
     * <p>此 spliterator 提供了在 spliterator 构造时列表状态的快照。
     * 在操作 spliterator 时不需要同步。
     *
     * @return 一个覆盖此列表中元素的 {@code Spliterator}
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator
            (getArray(), Spliterator.IMMUTABLE | Spliterator.ORDERED);
    }

    static final class COWIterator<E> implements ListIterator<E> {
        /** 数组的快照 */
        private final Object[] snapshot;
        /** 由后续调用 next 返回的元素的索引。 */
        private int cursor;

        private COWIterator(Object[] elements, int initialCursor) {
            cursor = initialCursor;
            snapshot = elements;
        }

        public boolean hasNext() {
            return cursor < snapshot.length;
        }

        public boolean hasPrevious() {
            return cursor > 0;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (! hasNext())
                throw new NoSuchElementException();
            return (E) snapshot[cursor++];
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            if (! hasPrevious())
                throw new NoSuchElementException();
            return (E) snapshot[--cursor];
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor-1;
        }

        /**
         * 不支持。始终抛出 UnsupportedOperationException。
         * @throws UnsupportedOperationException 始终抛出；此迭代器不支持 {@code remove}。
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * 不支持。始终抛出 UnsupportedOperationException。
         * @throws UnsupportedOperationException 始终抛出；此迭代器不支持 {@code set}。
         */
        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        /**
         * 不支持。始终抛出 UnsupportedOperationException。
         * @throws UnsupportedOperationException 始终抛出；此迭代器不支持 {@code add}。
         */
        public void add(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            Object[] elements = snapshot;
            final int size = elements.length;
            for (int i = cursor; i < size; i++) {
                @SuppressWarnings("unchecked") E e = (E) elements[i];
                action.accept(e);
            }
            cursor = size;
        }
    }

    /**
     * 返回此列表中从 {@code fromIndex}（包含）到 {@code toIndex}（不包含）之间的部分视图。
     * 返回的列表由此列表支持，因此返回的列表中的更改会反映在此列表中。
     *
     * <p>如果以任何方式修改了支持列表（即此列表），除了通过返回的列表，
     * 则返回列表的语义变得未定义。
     *
     * @param fromIndex 子列表的低端点（包含）
     * @param toIndex 子列表的高端点（不包含）
     * @return 此列表中指定范围的视图
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public List<E> subList(int fromIndex, int toIndex) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            if (fromIndex < 0 || toIndex > len || fromIndex > toIndex)
                throw new IndexOutOfBoundsException();
            return new COWSubList<E>(this, fromIndex, toIndex);
        } finally {
            lock.unlock();
        }
    }

    /**
     * CopyOnWriteArrayList 的子列表。
     * 此类仅为了方便扩展 AbstractList，以避免定义 addAll 等方法。这不会造成伤害，但有些浪费。
     * 此类不需要或不使用 AbstractList 中的 modCount 机制，但需要使用类似的机制检查并发修改。
     * 在每次操作中，我们都会检查并更新预期的支持列表使用的数组。
     * 由于我们在 AbstractList 定义的所有基础操作中都这样做，所以一切正常。
     * 尽管效率不高，但不值得改进。从 AbstractList 继承的列表操作在 COW 子列表上已经非常慢，
     * 增加一点空间/时间开销似乎甚至不明显。
     */
    private static class COWSubList<E>
        extends AbstractList<E>
        implements RandomAccess
    {
        private final CopyOnWriteArrayList<E> l;
        private final int offset;
        private int size;
        private Object[] expectedArray;

        // 仅在持有 l 的锁时调用
        COWSubList(CopyOnWriteArrayList<E> list,
                   int fromIndex, int toIndex) {
            l = list;
            expectedArray = l.getArray();
            offset = fromIndex;
            size = toIndex - fromIndex;
        }

        // 仅在持有 l 的锁时调用
        private void checkForComodification() {
            if (l.getArray() != expectedArray)
                throw new ConcurrentModificationException();
        }

        // 仅在持有 l 的锁时调用
        private void rangeCheck(int index) {
            if (index < 0 || index >= size)
                throw new IndexOutOfBoundsException("Index: "+index+
                                                    ",Size: "+size);
        }


                        public E set(int index, E element) {
            final ReentrantLock lock = l.lock;
            lock.lock();
            try {
                // 检查索引是否越界
                rangeCheck(index);
                // 检查是否有并发修改
                checkForComodification();
                // 设置元素并返回旧值
                E x = l.set(index + offset, element);
                // 更新预期数组
                expectedArray = l.getArray();
                return x;
            } finally {
                // 释放锁
                lock.unlock();
            }
        }

        public E get(int index) {
            final ReentrantLock lock = l.lock;
            lock.lock();
            try {
                // 检查索引是否越界
                rangeCheck(index);
                // 检查是否有并发修改
                checkForComodification();
                // 获取元素
                return l.get(index + offset);
            } finally {
                // 释放锁
                lock.unlock();
            }
        }

        public int size() {
            final ReentrantLock lock = l.lock;
            lock.lock();
            try {
                // 检查是否有并发修改
                checkForComodification();
                // 返回列表大小
                return size;
            } finally {
                // 释放锁
                lock.unlock();
            }
        }

        public void add(int index, E element) {
            final ReentrantLock lock = l.lock;
            lock.lock();
            try {
                // 检查是否有并发修改
                checkForComodification();
                // 检查索引是否越界
                if (index < 0 || index > size)
                    throw new IndexOutOfBoundsException();
                // 添加元素
                l.add(index + offset, element);
                // 更新预期数组
                expectedArray = l.getArray();
                // 增加列表大小
                size++;
            } finally {
                // 释放锁
                lock.unlock();
            }
        }

        public void clear() {
            final ReentrantLock lock = l.lock;
            lock.lock();
            try {
                // 检查是否有并发修改
                checkForComodification();
                // 移除指定范围内的所有元素
                l.removeRange(offset, offset + size);
                // 更新预期数组
                expectedArray = l.getArray();
                // 重置列表大小
                size = 0;
            } finally {
                // 释放锁
                lock.unlock();
            }
        }

        public E remove(int index) {
            final ReentrantLock lock = l.lock;
            lock.lock();
            try {
                // 检查索引是否越界
                rangeCheck(index);
                // 检查是否有并发修改
                checkForComodification();
                // 移除元素并返回
                E result = l.remove(index + offset);
                // 更新预期数组
                expectedArray = l.getArray();
                // 减少列表大小
                size--;
                return result;
            } finally {
                // 释放锁
                lock.unlock();
            }
        }

        public boolean remove(Object o) {
            // 查找对象的索引
            int index = indexOf(o);
            if (index == -1)
                return false;
            // 移除元素
            remove(index);
            return true;
        }

        public Iterator<E> iterator() {
            final ReentrantLock lock = l.lock;
            lock.lock();
            try {
                // 检查是否有并发修改
                checkForComodification();
                // 返回迭代器
                return new COWSubListIterator<E>(l, 0, offset, size);
            } finally {
                // 释放锁
                lock.unlock();
            }
        }

        public ListIterator<E> listIterator(int index) {
            final ReentrantLock lock = l.lock;
            lock.lock();
            try {
                // 检查是否有并发修改
                checkForComodification();
                // 检查索引是否越界
                if (index < 0 || index > size)
                    throw new IndexOutOfBoundsException("Index: " + index +
                                                        ", Size: " + size);
                // 返回列表迭代器
                return new COWSubListIterator<E>(l, index, offset, size);
            } finally {
                // 释放锁
                lock.unlock();
            }
        }

        public List<E> subList(int fromIndex, int toIndex) {
            final ReentrantLock lock = l.lock;
            lock.lock();
            try {
                // 检查是否有并发修改
                checkForComodification();
                // 检查索引是否越界
                if (fromIndex < 0 || toIndex > size || fromIndex > toIndex)
                    throw new IndexOutOfBoundsException();
                // 返回子列表
                return new COWSubList<E>(l, fromIndex + offset,
                                         toIndex + offset);
            } finally {
                // 释放锁
                lock.unlock();
            }
        }

        public void forEach(Consumer<? super E> action) {
            if (action == null) throw new NullPointerException();
            int lo = offset;
            int hi = offset + size;
            Object[] a = expectedArray;
            if (l.getArray() != a)
                throw new ConcurrentModificationException();
            if (lo < 0 || hi > a.length)
                throw new IndexOutOfBoundsException();
            // 遍历元素并执行操作
            for (int i = lo; i < hi; ++i) {
                @SuppressWarnings("unchecked") E e = (E) a[i];
                action.accept(e);
            }
        }

        public void replaceAll(UnaryOperator<E> operator) {
            if (operator == null) throw new NullPointerException();
            final ReentrantLock lock = l.lock;
            lock.lock();
            try {
                int lo = offset;
                int hi = offset + size;
                Object[] elements = expectedArray;
                if (l.getArray() != elements)
                    throw new ConcurrentModificationException();
                int len = elements.length;
                if (lo < 0 || hi > len)
                    throw new IndexOutOfBoundsException();
                Object[] newElements = Arrays.copyOf(elements, len);
                // 替换所有元素
                for (int i = lo; i < hi; ++i) {
                    @SuppressWarnings("unchecked") E e = (E) elements[i];
                    newElements[i] = operator.apply(e);
                }
                l.setArray(expectedArray = newElements);
            } finally {
                // 释放锁
                lock.unlock();
            }
        }

        public void sort(Comparator<? super E> c) {
            final ReentrantLock lock = l.lock;
            lock.lock();
            try {
                int lo = offset;
                int hi = offset + size;
                Object[] elements = expectedArray;
                if (l.getArray() != elements)
                    throw new ConcurrentModificationException();
                int len = elements.length;
                if (lo < 0 || hi > len)
                    throw new IndexOutOfBoundsException();
                Object[] newElements = Arrays.copyOf(elements, len);
                @SuppressWarnings("unchecked") E[] es = (E[]) newElements;
                // 排序元素
                Arrays.sort(es, lo, hi, c);
                l.setArray(expectedArray = newElements);
            } finally {
                // 释放锁
                lock.unlock();
            }
        }


public boolean removeAll(Collection<?> c) {
    if (c == null) throw new NullPointerException(); // 如果集合 c 为 null，则抛出 NullPointerException
    boolean removed = false; // 初始化 removed 为 false，表示没有元素被移除
    final ReentrantLock lock = l.lock; // 获取锁
    lock.lock(); // 锁定
    try {
        int n = size; // 获取当前列表的大小
        if (n > 0) { // 如果列表不为空
            int lo = offset; // 获取列表的起始索引
            int hi = offset + n; // 获取列表的结束索引
            Object[] elements = expectedArray; // 获取当前列表的元素数组
            if (l.getArray() != elements) // 如果当前列表的数组与预期数组不一致
                throw new ConcurrentModificationException(); // 抛出并发修改异常
            int len = elements.length; // 获取元素数组的长度
            if (lo < 0 || hi > len) // 如果起始索引或结束索引超出数组范围
                throw new IndexOutOfBoundsException(); // 抛出索引越界异常
            int newSize = 0; // 初始化新列表的大小为 0
            Object[] temp = new Object[n]; // 创建一个临时数组用于存储未被移除的元素
            for (int i = lo; i < hi; ++i) { // 遍历当前列表的元素
                Object element = elements[i]; // 获取当前元素
                if (!c.contains(element)) // 如果集合 c 不包含当前元素
                    temp[newSize++] = element; // 将当前元素添加到临时数组中
            }
            if (newSize != n) { // 如果新列表的大小与原列表的大小不同
                Object[] newElements = new Object[len - n + newSize]; // 创建新的元素数组
                System.arraycopy(elements, 0, newElements, 0, lo); // 复制起始部分的元素
                System.arraycopy(temp, 0, newElements, lo, newSize); // 复制临时数组中的元素
                System.arraycopy(elements, hi, newElements, // 复制结束部分的元素
                                 lo + newSize, len - hi);
                size = newSize; // 更新列表的大小
                removed = true; // 设置 removed 为 true，表示有元素被移除
                l.setArray(expectedArray = newElements); // 更新列表的元素数组
            }
        }
    } finally {
        lock.unlock(); // 释放锁
    }
    return removed; // 返回是否有元素被移除
}

public boolean retainAll(Collection<?> c) {
    if (c == null) throw new NullPointerException(); // 如果集合 c 为 null，则抛出 NullPointerException
    boolean removed = false; // 初始化 removed 为 false，表示没有元素被移除
    final ReentrantLock lock = l.lock; // 获取锁
    lock.lock(); // 锁定
    try {
        int n = size; // 获取当前列表的大小
        if (n > 0) { // 如果列表不为空
            int lo = offset; // 获取列表的起始索引
            int hi = offset + n; // 获取列表的结束索引
            Object[] elements = expectedArray; // 获取当前列表的元素数组
            if (l.getArray() != elements) // 如果当前列表的数组与预期数组不一致
                throw new ConcurrentModificationException(); // 抛出并发修改异常
            int len = elements.length; // 获取元素数组的长度
            if (lo < 0 || hi > len) // 如果起始索引或结束索引超出数组范围
                throw new IndexOutOfBoundsException(); // 抛出索引越界异常
            int newSize = 0; // 初始化新列表的大小为 0
            Object[] temp = new Object[n]; // 创建一个临时数组用于存储保留的元素
            for (int i = lo; i < hi; ++i) { // 遍历当前列表的元素
                Object element = elements[i]; // 获取当前元素
                if (c.contains(element)) // 如果集合 c 包含当前元素
                    temp[newSize++] = element; // 将当前元素添加到临时数组中
            }
            if (newSize != n) { // 如果新列表的大小与原列表的大小不同
                Object[] newElements = new Object[len - n + newSize]; // 创建新的元素数组
                System.arraycopy(elements, 0, newElements, 0, lo); // 复制起始部分的元素
                System.arraycopy(temp, 0, newElements, lo, newSize); // 复制临时数组中的元素
                System.arraycopy(elements, hi, newElements, // 复制结束部分的元素
                                 lo + newSize, len - hi);
                size = newSize; // 更新列表的大小
                removed = true; // 设置 removed 为 true，表示有元素被移除
                l.setArray(expectedArray = newElements); // 更新列表的元素数组
            }
        }
    } finally {
        lock.unlock(); // 释放锁
    }
    return removed; // 返回是否有元素被移除
}

public boolean removeIf(Predicate<? super E> filter) {
    if (filter == null) throw new NullPointerException(); // 如果过滤器为 null，则抛出 NullPointerException
    boolean removed = false; // 初始化 removed 为 false，表示没有元素被移除
    final ReentrantLock lock = l.lock; // 获取锁
    lock.lock(); // 锁定
    try {
        int n = size; // 获取当前列表的大小
        if (n > 0) { // 如果列表不为空
            int lo = offset; // 获取列表的起始索引
            int hi = offset + n; // 获取列表的结束索引
            Object[] elements = expectedArray; // 获取当前列表的元素数组
            if (l.getArray() != elements) // 如果当前列表的数组与预期数组不一致
                throw new ConcurrentModificationException(); // 抛出并发修改异常
            int len = elements.length; // 获取元素数组的长度
            if (lo < 0 || hi > len) // 如果起始索引或结束索引超出数组范围
                throw new IndexOutOfBoundsException(); // 抛出索引越界异常
            int newSize = 0; // 初始化新列表的大小为 0
            Object[] temp = new Object[n]; // 创建一个临时数组用于存储未被移除的元素
            for (int i = lo; i < hi; ++i) { // 遍历当前列表的元素
                @SuppressWarnings("unchecked") E e = (E) elements[i]; // 获取当前元素
                if (!filter.test(e)) // 如果过滤器测试不通过
                    temp[newSize++] = e; // 将当前元素添加到临时数组中
            }
            if (newSize != n) { // 如果新列表的大小与原列表的大小不同
                Object[] newElements = new Object[len - n + newSize]; // 创建新的元素数组
                System.arraycopy(elements, 0, newElements, 0, lo); // 复制起始部分的元素
                System.arraycopy(temp, 0, newElements, lo, newSize); // 复制临时数组中的元素
                System.arraycopy(elements, hi, newElements, // 复制结束部分的元素
                                 lo + newSize, len - hi);
                size = newSize; // 更新列表的大小
                removed = true; // 设置 removed 为 true，表示有元素被移除
                l.setArray(expectedArray = newElements); // 更新列表的元素数组
            }
        }
    } finally {
        lock.unlock(); // 释放锁
    }
    return removed; // 返回是否有元素被移除
}

public Spliterator<E> spliterator() {
    int lo = offset; // 获取列表的起始索引
    int hi = offset + size; // 获取列表的结束索引
    Object[] a = expectedArray; // 获取当前列表的元素数组
    if (l.getArray() != a) // 如果当前列表的数组与预期数组不一致
        throw new ConcurrentModificationException(); // 抛出并发修改异常
    if (lo < 0 || hi > a.length) // 如果起始索引或结束索引超出数组范围
        throw new IndexOutOfBoundsException(); // 抛出索引越界异常
    return Spliterators.spliterator // 返回一个新的 Spliterator
        (a, lo, hi, Spliterator.IMMUTABLE | Spliterator.ORDERED); // 创建 Spliterator 并设置其特性
}

}

private static class COWSubListIterator<E> implements ListIterator<E> {
    private final ListIterator<E> it; // 存储底层列表的迭代器
    private final int offset; // 存储子列表的起始偏移量
    private final int size; // 存储子列表的大小

    COWSubListIterator(List<E> l, int index, int offset, int size) {
        this.offset = offset; // 初始化子列表的起始偏移量
        this.size = size; // 初始化子列表的大小
        it = l.listIterator(index + offset); // 初始化底层列表的迭代器
    }

    public boolean hasNext() {
        return nextIndex() < size; // 如果下一个索引小于子列表的大小，返回 true
    }

    public E next() {
        if (hasNext()) // 如果有下一个元素
            return it.next(); // 返回下一个元素
        else
            throw new NoSuchElementException(); // 否则抛出 NoSuchElementException
    }


                        public boolean hasPrevious() {
            return previousIndex() >= 0;
        }

        public E previous() {
            if (hasPrevious())
                return it.previous();
            else
                throw new NoSuchElementException();
        }

        public int nextIndex() {
            return it.nextIndex() - offset;
        }

        public int previousIndex() {
            return it.previousIndex() - offset;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        public void add(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            int s = size;
            ListIterator<E> i = it;
            while (nextIndex() < s) {
                action.accept(i.next());
            }
        }
    }

    // 支持在反序列化时重置锁
    private void resetLock() {
        UNSAFE.putObjectVolatile(this, lockOffset, new ReentrantLock());
    }
    private static final sun.misc.Unsafe UNSAFE;
    private static final long lockOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = CopyOnWriteArrayList.class;
            lockOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("lock"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
