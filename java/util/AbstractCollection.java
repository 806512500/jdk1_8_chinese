
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 该类提供了 <tt>Collection</tt> 接口的骨架实现，以最小化实现此接口所需的努力。 <p>
 *
 * 要实现一个不可修改的集合，程序员只需要扩展此类并提供 <tt>iterator</tt> 和 <tt>size</tt> 方法的实现。 (由 <tt>iterator</tt> 方法返回的迭代器必须实现 <tt>hasNext</tt> 和 <tt>next</tt>。)<p>
 *
 * 要实现一个可修改的集合，程序员还必须覆盖此类的 <tt>add</tt> 方法（否则会抛出 <tt>UnsupportedOperationException</tt>），并且由 <tt>iterator</tt> 方法返回的迭代器还必须实现其 <tt>remove</tt> 方法。<p>
 *
 * 程序员通常应提供一个无参数和一个 <tt>Collection</tt> 构造函数，如 <tt>Collection</tt> 接口规范中所述。<p>
 *
 * 本类中每个非抽象方法的文档详细描述了其实现。 如果被实现的集合允许更高效的实现，可以覆盖这些方法。<p>
 *
 * 该类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see Collection
 * @since 1.2
 */

public abstract class AbstractCollection<E> implements Collection<E> {
    /**
     * 唯一的构造函数。 (通常由子类构造函数隐式调用。)
     */
    protected AbstractCollection() {
    }

    // 查询操作

    /**
     * 返回一个迭代器，用于遍历此集合中的元素。
     *
     * @return 一个迭代器，用于遍历此集合中的元素
     */
    public abstract Iterator<E> iterator();

    public abstract int size();

    /**
     * {@inheritDoc}
     *
     * <p>此实现返回 <tt>size() == 0</tt>。
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现遍历集合中的元素，依次检查每个元素是否与指定元素相等。
     *
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean contains(Object o) {
        Iterator<E> it = iterator();
        if (o==null) {
            while (it.hasNext())
                if (it.next()==null)
                    return true;
        } else {
            while (it.hasNext())
                if (o.equals(it.next()))
                    return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现返回一个数组，其中包含此集合的迭代器返回的所有元素，顺序相同，存储在数组的连续元素中，从索引 {@code 0} 开始。
     * 返回的数组的长度等于迭代器返回的元素数量，即使在迭代过程中集合的大小发生变化，也可能会发生这种情况，如果集合允许在迭代过程中进行并发修改。 {@code size} 方法仅作为优化提示调用；即使迭代器返回的元素数量不同，也会返回正确的结果。
     *
     * <p>此方法等效于：
     *
     *  <pre> {@code
     * List<E> list = new ArrayList<E>(size());
     * for (E e : this)
     *     list.add(e);
     * return list.toArray();
     * }</pre>
     */
    public Object[] toArray() {
        // 估计数组的大小；准备好看到更多或更少的元素
        Object[] r = new Object[size()];
        Iterator<E> it = iterator();
        for (int i = 0; i < r.length; i++) {
            if (! it.hasNext()) // 元素数量少于预期
                return Arrays.copyOf(r, i);
            r[i] = it.next();
        }
        return it.hasNext() ? finishToArray(r, it) : r;
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现返回一个数组，其中包含此集合的迭代器返回的所有元素，顺序相同，存储在数组的连续元素中，从索引 {@code 0} 开始。
     * 如果迭代器返回的元素数量太大，无法放入指定的数组中，则返回一个新分配的数组，其长度等于迭代器返回的元素数量，即使在迭代过程中集合的大小发生变化，也可能会发生这种情况，如果集合允许在迭代过程中进行并发修改。 {@code size} 方法仅作为优化提示调用；即使迭代器返回的元素数量不同，也会返回正确的结果。
     *
     * <p>此方法等效于：
     *
     *  <pre> {@code
     * List<E> list = new ArrayList<E>(size());
     * for (E e : this)
     *     list.add(e);
     * return list.toArray(a);
     * }</pre>
     *
     * @throws ArrayStoreException  {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        // 估计数组的大小；准备好看到更多或更少的元素
        int size = size();
        T[] r = a.length >= size ? a :
                  (T[])java.lang.reflect.Array
                  .newInstance(a.getClass().getComponentType(), size);
        Iterator<E> it = iterator();


                    for (int i = 0; i < r.length; i++) {
            if (! it.hasNext()) { // fewer elements than expected
                if (a == r) {
                    r[i] = null; // null-终止
                } else if (a.length < i) {
                    return Arrays.copyOf(r, i);
                } else {
                    System.arraycopy(r, 0, a, 0, i);
                    if (a.length > i) {
                        a[i] = null;
                    }
                }
                return a;
            }
            r[i] = (T)it.next();
        }
        // more elements than expected
        return it.hasNext() ? finishToArray(r, it) : r;
    }

    /**
     * 可分配数组的最大大小。
     * 一些虚拟机在数组中保留一些头部字。
     * 尝试分配更大的数组可能会导致
     * OutOfMemoryError: 请求的数组大小超过虚拟机限制
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 当迭代器返回的元素比预期的多时，重新分配在 toArray 中使用的数组，并从迭代器中完成填充。
     *
     * @param r 包含先前存储元素的数组
     * @param it 迭代此集合的进行中的迭代器
     * @return 包含给定数组中的元素，加上迭代器返回的任何进一步元素，调整到合适大小的数组
     */
    @SuppressWarnings("unchecked")
    private static <T> T[] finishToArray(T[] r, Iterator<?> it) {
        int i = r.length;
        while (it.hasNext()) {
            int cap = r.length;
            if (i == cap) {
                int newCap = cap + (cap >> 1) + 1;
                // 防止溢出的代码
                if (newCap - MAX_ARRAY_SIZE > 0)
                    newCap = hugeCapacity(cap + 1);
                r = Arrays.copyOf(r, newCap);
            }
            r[i++] = (T)it.next();
        }
        // 如果分配过多则裁剪
        return (i == r.length) ? r : Arrays.copyOf(r, i);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // 溢出
            throw new OutOfMemoryError
                ("所需数组大小过大");
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

    // 修改操作

    /**
     * {@inheritDoc}
     *
     * <p>此实现总是抛出一个
     * <tt>UnsupportedOperationException</tt>。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IllegalStateException         {@inheritDoc}
     */
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现遍历集合，查找指定的元素。如果找到该元素，则使用迭代器的 remove 方法从集合中删除该元素。
     *
     * <p>请注意，如果此集合的迭代器方法返回的迭代器未实现 <tt>remove</tt> 方法且此集合包含指定对象，则此实现将抛出
     * <tt>UnsupportedOperationException</tt>。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     */
    public boolean remove(Object o) {
        Iterator<E> it = iterator();
        if (o==null) {
            while (it.hasNext()) {
                if (it.next()==null) {
                    it.remove();
                    return true;
                }
            }
        } else {
            while (it.hasNext()) {
                if (o.equals(it.next())) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }


    // 批量操作

    /**
     * {@inheritDoc}
     *
     * <p>此实现遍历指定的集合，检查迭代器返回的每个元素是否包含在此集合中。如果所有元素都包含，则返回 <tt>true</tt>，否则返回 <tt>false</tt>。
     *
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @see #contains(Object)
     */
    public boolean containsAll(Collection<?> c) {
        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现遍历指定的集合，并将迭代器返回的每个对象依次添加到此集合中。
     *
     * <p>请注意，如果 <tt>add</tt> 未被重写（假设指定的集合非空），此实现将抛出
     * <tt>UnsupportedOperationException</tt>。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IllegalStateException         {@inheritDoc}
     *
     * @see #add(Object)
     */
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E e : c)
            if (add(e))
                modified = true;
        return modified;
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现遍历此集合，检查迭代器返回的每个元素是否包含在指定的集合中。如果包含，则使用迭代器的 <tt>remove</tt> 方法从集合中删除该元素。
     *
     * <p>请注意，如果 <tt>iterator</tt> 方法返回的迭代器未实现 <tt>remove</tt> 方法且此集合与指定集合有一个或多个元素相同，则此实现将抛出
     * <tt>UnsupportedOperationException</tt>。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }


                /**
     * {@inheritDoc}
     *
     * <p>此实现遍历此集合，检查迭代器返回的每个元素是否包含在指定的集合中。如果某个元素不包含在指定的集合中，则使用迭代器的 <tt>remove</tt> 方法将其从集合中移除。
     *
     * <p>请注意，如果此集合包含一个或多个不在指定集合中的元素，并且 <tt>iterator</tt> 方法返回的迭代器未实现 <tt>remove</tt> 方法，则此实现将抛出 <tt>UnsupportedOperationException</tt>。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * {@inheritDoc}
     *
     * <p>此实现遍历此集合，使用 <tt>Iterator.remove</tt> 操作移除每个元素。大多数实现可能会选择为此方法提供更高效的实现。
     *
     * <p>请注意，如果此集合的 <tt>iterator</tt> 方法返回的迭代器未实现 <tt>remove</tt> 方法且此集合非空，则此实现将抛出 <tt>UnsupportedOperationException</tt>。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     */
    public void clear() {
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }


    //  字符串转换

    /**
     * 返回此集合的字符串表示形式。字符串表示形式由方括号 (<tt>"[]"</tt>) 包围的集合元素列表组成，元素的顺序由其迭代器返回。相邻元素之间用字符 <tt>", "</tt>（逗号和空格）分隔。元素转换为字符串的方式与 {@link String#valueOf(Object)} 相同。
     *
     * @return 此集合的字符串表示形式
     */
    public String toString() {
        Iterator<E> it = iterator();
        if (! it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            E e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (! it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

}
