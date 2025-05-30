
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

package java.util.concurrent;
import java.util.Collection;
import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.function.Consumer;

/**
 * 一个使用内部 {@link CopyOnWriteArrayList} 进行所有操作的 {@link java.util.Set}。因此，它具有相同的基属性：
 * <ul>
 *  <li>它最适合于集合大小通常保持较小、只读操作远远多于变更操作，并且需要防止线程在遍历时相互干扰的应用。
 *  <li>它是线程安全的。
 *  <li>变更操作（如 {@code add}、{@code set}、{@code remove} 等）非常昂贵，因为它们通常涉及复制整个底层数组。
 *  <li>迭代器不支持变更的 {@code remove} 操作。
 *  <li>通过迭代器遍历速度快且不会遇到其他线程的干扰。迭代器依赖于在迭代器构建时数组的不变快照。
 * </ul>
 *
 * <p><b>示例用法。</b> 以下代码草图使用一个复制写集合来维护一组在状态更新时执行某些操作的 Handler 对象。
 *
 *  <pre> {@code
 * class Handler { void handle(); ... }
 *
 * class X {
 *   private final CopyOnWriteArraySet<Handler> handlers
 *     = new CopyOnWriteArraySet<Handler>();
 *   public void addHandler(Handler h) { handlers.add(h); }
 *
 *   private long internalState;
 *   private synchronized void changeState() { internalState = ...; }
 *
 *   public void update() {
 *     changeState();
 *     for (Handler handler : handlers)
 *       handler.handle();
 *   }
 * }}</pre>
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @see CopyOnWriteArrayList
 * @since 1.5
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public class CopyOnWriteArraySet<E> extends AbstractSet<E>
        implements java.io.Serializable {
    private static final long serialVersionUID = 5457747651344034263L;

    private final CopyOnWriteArrayList<E> al;

    /**
     * 创建一个空集。
     */
    public CopyOnWriteArraySet() {
        al = new CopyOnWriteArrayList<E>();
    }

    /**
     * 创建一个包含指定集合中所有元素的集。
     *
     * @param c 要初始包含的元素集合
     * @throws NullPointerException 如果指定的集合为 null
     */
    public CopyOnWriteArraySet(Collection<? extends E> c) {
        if (c.getClass() == CopyOnWriteArraySet.class) {
            @SuppressWarnings("unchecked") CopyOnWriteArraySet<E> cc =
                (CopyOnWriteArraySet<E>)c;
            al = new CopyOnWriteArrayList<E>(cc.al);
        }
        else {
            al = new CopyOnWriteArrayList<E>();
            al.addAllAbsent(c);
        }
    }

    /**
     * 返回此集中的元素数量。
     *
     * @return 此集中的元素数量
     */
    public int size() {
        return al.size();
    }

    /**
     * 如果此集不包含任何元素，则返回 {@code true}。
     *
     * @return 如果此集不包含任何元素，则返回 {@code true}
     */
    public boolean isEmpty() {
        return al.isEmpty();
    }

    /**
     * 如果此集包含指定的元素，则返回 {@code true}。更正式地说，如果且仅当此集包含一个元素 {@code e} 使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>，则返回 {@code true}。
     *
     * @param o 要测试其是否存在于此集中的元素
     * @return 如果此集包含指定的元素，则返回 {@code true}
     */
    public boolean contains(Object o) {
        return al.contains(o);
    }

    /**
     * 返回一个包含此集中所有元素的数组。如果此集对其迭代器返回的元素顺序有保证，则此方法必须以相同的顺序返回元素。
     *
     * <p>返回的数组是“安全的”，即此集不维护对它的任何引用。（换句话说，此方法必须分配一个新数组，即使此集由数组支持也是如此）。调用者因此可以自由地修改返回的数组。
     *
     * <p>此方法充当基于数组和基于集合的 API 之间的桥梁。
     *
     * @return 一个包含此集中所有元素的数组
     */
    public Object[] toArray() {
        return al.toArray();
    }

    /**
     * 返回一个包含此集中所有元素的数组；返回数组的运行时类型是指定数组的运行时类型。如果此集可以放入指定数组中，则返回该数组。否则，分配一个具有指定数组的运行时类型和此集大小的新数组。
     *
     * <p>如果此集可以放入指定数组中且有剩余空间（即，数组的元素多于此集的元素），则数组中紧接此集末尾的元素被设置为
     * {@code null}。（这仅在调用者知道此集不包含任何 null 元素时有用。）
     *
     * <p>如果此集对其迭代器返回的元素顺序有保证，则此方法必须以相同的顺序返回元素。
     *
     * <p>类似于 {@link #toArray()} 方法，此方法充当基于数组和基于集合的 API 之间的桥梁。此外，此方法允许精确控制输出数组的运行时类型，并且在某些情况下，可以用来节省分配成本。
     *
     * <p>假设 {@code x} 是一个已知只包含字符串的集。以下代码可以用来将集转储到一个新分配的 {@code String} 数组中：
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * 注意 {@code toArray(new Object[0])} 与 {@code toArray()} 功能相同。
     *
     * @param a 如果足够大，则将此集中的元素存储到其中的数组；否则，为此目的分配一个具有相同运行时类型的数组。
     * @return 一个包含此集中所有元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此集中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    public <T> T[] toArray(T[] a) {
        return al.toArray(a);
    }

    /**
     * 从此集中移除所有元素。调用此方法后，集将为空。
     */
    public void clear() {
        al.clear();
    }

    /**
     * 如果此集包含指定的元素，则将其移除。更正式地说，移除一个元素 {@code e} 使得
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>，如果此集包含这样的元素。如果此集包含该元素（或等效地，如果此集因调用而改变），则返回 {@code true}。（此集在调用返回时将不再包含该元素。）
     *
     * @param o 要从此集中移除的对象，如果存在
     * @return 如果此集包含指定的元素，则返回 {@code true}
     */
    public boolean remove(Object o) {
        return al.remove(o);
    }

    /**
     * 如果此集中尚未包含指定的元素，则将其添加到此集中。更正式地说，如果此集不包含元素 {@code e2} 使得
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>，则将指定的元素 {@code e} 添加到此集中。如果此集已经包含该元素，则调用使集保持不变并返回 {@code false}。
     *
     * @param e 要添加到此集中的元素
     * @return 如果此集之前不包含指定的元素，则返回 {@code true}
     */
    public boolean add(E e) {
        return al.addIfAbsent(e);
    }

    /**
     * 如果此集包含指定集合中的所有元素，则返回 {@code true}。如果指定的集合也是一个集，则此方法返回 {@code true}，如果它是此集的 <i>子集</i>。
     *
     * @param  c 要检查是否包含在此集中的集合
     * @return 如果此集包含指定集合中的所有元素，则返回 {@code true}
     * @throws NullPointerException 如果指定的集合为 null
     * @see #contains(Object)
     */
    public boolean containsAll(Collection<?> c) {
        return al.containsAll(c);
    }

    /**
     * 如果指定集合中的元素尚未存在，则将它们添加到此集中。如果指定的集合也是一个集，则 {@code addAll} 操作实际上会修改此集，使其值为两个集的 <i>并集</i>。如果在操作进行过程中指定的集合被修改，则此操作的行为是未定义的。
     *
     * @param  c 要添加到此集中的元素集合
     * @return 如果此集因调用而改变，则返回 {@code true}
     * @throws NullPointerException 如果指定的集合为 null
     * @see #add(Object)
     */
    public boolean addAll(Collection<? extends E> c) {
        return al.addAllAbsent(c) > 0;
    }

    /**
     * 从此集中移除包含在指定集合中的所有元素。如果指定的集合也是一个集，则此操作实际上会修改此集，使其值为两个集的 <i>非对称集差</i>。
     *
     * @param  c 要从此集中移除的元素集合
     * @return 如果此集因调用而改变，则返回 {@code true}
     * @throws ClassCastException 如果此集中元素的类与指定集合不兼容（可选）
     * @throws NullPointerException 如果此集包含 null 元素且指定集合不允许 null 元素（可选），或者指定的集合为 null
     * @see #remove(Object)
     */
    public boolean removeAll(Collection<?> c) {
        return al.removeAll(c);
    }

    /**
     * 仅保留此集中包含在指定集合中的元素。换句话说，从此集中移除所有不包含在指定集合中的元素。如果指定的集合也是一个集，则此操作实际上会修改此集，使其值为两个集的 <i>交集</i>。
     *
     * @param  c 要保留在此集中的元素集合
     * @return 如果此集因调用而改变，则返回 {@code true}
     * @throws ClassCastException 如果此集中元素的类与指定集合不兼容（可选）
     * @throws NullPointerException 如果此集包含 null 元素且指定集合不允许 null 元素（可选），或者指定的集合为 null
     * @see #remove(Object)
     */
    public boolean retainAll(Collection<?> c) {
        return al.retainAll(c);
    }

    /**
     * 返回一个迭代器，按元素添加的顺序迭代此集中的元素。
     *
     * <p>返回的迭代器提供了一个此集在迭代器构建时的状态快照。遍历迭代器时不需要同步。迭代器不支持 {@code remove} 方法。
     *
     * @return 一个迭代此集中元素的迭代器
     */
    public Iterator<E> iterator() {
        return al.iterator();
    }

    /**
     * 将指定的对象与此集进行比较以确定相等性。如果指定的对象与此对象是同一个对象，或者它也是一个 {@link Set} 且通过 {@linkplain Set#iterator() 迭代器} 返回的指定集的元素与通过迭代器返回的此集的元素相同，则返回 {@code true}。更正式地说，如果两个迭代器返回相同数量的元素，并且对于指定集的迭代器返回的每个元素 {@code e1}，此集的迭代器返回一个元素 {@code e2} 使得
     * {@code (e1==null ? e2==null : e1.equals(e2))}，则认为两个迭代器返回相同的元素。
     *
     * @param o 要与此集比较相等性的对象
     * @return 如果指定的对象等于此集，则返回 {@code true}
     */
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Set))
            return false;
        Set<?> set = (Set<?>)(o);
        Iterator<?> it = set.iterator();

        // 使用 O(n^2) 算法，仅适用于小集，CopyOnWriteArraySets 应该是小集。

        // 使用底层数组的单个快照
        Object[] elements = al.getArray();
        int len = elements.length;
        // 标记匹配的元素以避免重新检查
        boolean[] matched = new boolean[len];
        int k = 0;
        outer: while (it.hasNext()) {
            if (++k > len)
                return false;
            Object x = it.next();
            for (int i = 0; i < len; ++i) {
                if (!matched[i] && eq(x, elements[i])) {
                    matched[i] = true;
                    continue outer;
                }
            }
            return false;
        }
        return k == len;
    }


                public boolean removeIf(Predicate<? super E> filter) {
        return al.removeIf(filter);
    }

    public void forEach(Consumer<? super E> action) {
        al.forEach(action);
    }

    /**
     * 返回一个 {@link Spliterator}，该迭代器按元素添加的顺序遍历此集合中的元素。
     *
     * <p>{@code Spliterator} 报告 {@link Spliterator#IMMUTABLE}，
     * {@link Spliterator#DISTINCT}，{@link Spliterator#SIZED} 和
     * {@link Spliterator#SUBSIZED}。
     *
     * <p>该迭代器提供了一个在迭代器构建时集合状态的快照。
     * 在操作迭代器时不需要同步。
     *
     * @return 一个遍历此集合元素的 {@code Spliterator}
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator
            (al.getArray(), Spliterator.IMMUTABLE | Spliterator.DISTINCT);
    }

    /**
     * 测试相等性，处理空值。
     */
    private static boolean eq(Object o1, Object o2) {
        return (o1 == null) ? o2 == null : o1.equals(o2);
    }
}
