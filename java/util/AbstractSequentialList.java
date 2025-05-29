
/*
 * 版权所有 (c) 1997, 2006, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

package java.util;

/**
 * 该类为 <tt>List</tt> 接口提供了一个骨架实现，以最小化实现此接口所需的 effort，特别是当数据存储为“顺序访问”（如链表）时。对于随机访问数据（如数组），应优先使用 <tt>AbstractList</tt> 而不是此类。<p>
 *
 * 从某种意义上说，这个类与 <tt>AbstractList</tt> 类相反，因为它在列表的列表迭代器之上实现了“随机访问”方法（<tt>get(int index)</tt>，<tt>set(int index, E element)</tt>，<tt>add(int index, E element)</tt> 和 <tt>remove(int index)</tt>），而不是反过来。<p>
 *
 * 要实现一个列表，程序员只需扩展此类并提供 <tt>listIterator</tt> 和 <tt>size</tt> 方法的实现。对于不可修改的列表，程序员只需实现列表迭代器的 <tt>hasNext</tt>，<tt>next</tt>，<tt>hasPrevious</tt>，<tt>previous</tt> 和 <tt>index</tt> 方法。<p>
 *
 * 对于可修改的列表，程序员还应实现列表迭代器的 <tt>set</tt> 方法。对于可变大小的列表，程序员还应实现列表迭代器的 <tt>remove</tt> 和 <tt>add</tt> 方法。<p>
 *
 * 程序员通常应提供一个无参数构造函数和一个集合构造函数，如 <tt>Collection</tt> 接口规范中所建议的。<p>
 *
 * 该类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a> 的成员。
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
     * 唯一的构造函数。 （通常由子类构造函数隐式调用。）
     */
    protected AbstractSequentialList() {
    }

    /**
     * 返回此列表中指定位置的元素。
     *
     * <p>此实现首先使用 <tt>listIterator(index)</tt> 获取指向索引元素的列表迭代器。然后，它使用 <tt>ListIterator.next</tt> 获取元素并返回它。
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
     * 在此列表的指定位置插入指定的元素（可选操作）。将当前位置的元素（如果有）和任何后续元素向右移动（增加它们的索引）。
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
     * 从此列表中移除指定位置的元素（可选操作）。将任何后续元素向左移动（减少它们的索引）。返回从列表中移除的元素。
     *
     * <p>此实现首先使用 <tt>listIterator(index)</tt> 获取指向索引元素的列表迭代器。然后，它使用 <tt>ListIterator.remove</tt> 移除元素。
     *
     * <p>请注意，如果列表迭代器未实现 <tt>remove</tt> 操作，此实现将抛出 <tt>UnsupportedOperationException</tt>。
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
     * 将指定集合中的所有元素插入到此列表的指定位置（可选操作）。将当前位置的元素（如果有）和后续元素向右移动（增加它们的索引）。新元素将按照指定集合的迭代器返回的顺序出现在此列表中。如果在操作进行过程中修改了指定的集合，则此操作的行为是未定义的。（注意，如果指定的集合是此列表，并且它不是空的，这种情况会发生。）
     *
     * <p>此实现获取指定集合的迭代器和指向索引元素的列表迭代器（使用<tt>listIterator(index)</tt>）。然后，它遍历指定的集合，使用<tt>ListIterator.add</tt>和<tt>ListIterator.next</tt>（跳过添加的元素）将从迭代器中获取的元素逐个插入到此列表中。
     *
     * <p>请注意，如果<tt>listIterator</tt>方法返回的列表迭代器未实现<tt>add</tt>操作，则此实现将抛出<tt>UnsupportedOperationException</tt>。
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
     * 返回一个按适当顺序遍历此列表元素的迭代器。<p>
     *
     * 此实现只是返回一个列表迭代器。
     *
     * @return 按适当顺序遍历此列表元素的迭代器
     */
    public Iterator<E> iterator() {
        return listIterator();
    }

    /**
     * 返回一个按适当顺序遍历此列表元素的列表迭代器。
     *
     * @param  index 列表迭代器返回的第一个元素的索引（通过调用<code>next</code>方法）
     * @return 按适当顺序遍历此列表元素的列表迭代器
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public abstract ListIterator<E> listIterator(int index);
}
