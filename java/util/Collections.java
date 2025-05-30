
/*
 * Copyright (c) 1997, 2019, Oracle and/or its affiliates. All rights reserved.
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
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import sun.misc.SharedSecrets;

/**
 * 本类仅包含静态方法，这些方法操作或返回集合。它包含操作集合的多态算法、“包装器”，这些包装器返回由指定集合支持的新集合，以及一些其他杂项。
 *
 * <p>本类中的方法如果提供的集合或类对象为 null，则都会抛出 <tt>NullPointerException</tt>。
 *
 * <p>本类中包含的多态算法的文档通常包括对 <i>实现</i> 的简要描述。此类描述应被视为 <i>实现说明</i>，而不是 <i>规范</i> 的一部分。实现者应自由地替换其他算法，只要遵循规范本身即可。（例如，<tt>sort</tt> 使用的算法不必是归并排序，但必须是 <i>稳定的</i>。）
 *
 * <p>本类中包含的“破坏性”算法，即修改其操作的集合的算法，如果集合不支持适当的变异原语（如 <tt>set</tt> 方法），则指定抛出 <tt>UnsupportedOperationException</tt>。这些算法可以，但不要求，如果调用对集合没有影响，则抛出此异常。例如，对已排序的不可修改列表调用 <tt>sort</tt> 方法可能会或可能不会抛出 <tt>UnsupportedOperationException</tt>。
 *
 * <p>本类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Collection
 * @see     Set
 * @see     List
 * @see     Map
 * @since   1.2
 */

public class Collections {
    // 抑制默认构造函数，确保不可实例化。
    private Collections() {
    }

    // 算法

    /*
     * 算法的调优参数 - 许多 List 算法有两种实现，一种适用于 RandomAccess 列表，另一种适用于“顺序”列表。通常，随机访问变体在小顺序访问列表上也能提供更好的性能。以下调优参数确定了每个算法中构成“小”顺序访问列表的阈值。这些值是通过经验确定的，适用于 LinkedList。希望对本代码进行性能工作的人员应定期验证这些参数值。
     * （每个调优参数名称的第一个词是它适用的算法。）
     */
    private static final int BINARYSEARCH_THRESHOLD   = 5000;
    private static final int REVERSE_THRESHOLD        =   18;
    private static final int SHUFFLE_THRESHOLD        =    5;
    private static final int FILL_THRESHOLD           =   25;
    private static final int ROTATE_THRESHOLD         =  100;
    private static final int COPY_THRESHOLD           =   10;
    private static final int REPLACEALL_THRESHOLD     =   11;
    private static final int INDEXOFSUBLIST_THRESHOLD =   35;

    /**
     * 按照元素的 {@linkplain Comparable 自然顺序} 对指定列表进行升序排序。
     * 列表中的所有元素都必须实现 {@link Comparable} 接口。此外，列表中的所有元素都必须是 <i>相互可比较的</i>（即，对于列表中的任何元素 {@code e1} 和 {@code e2}，{@code e1.compareTo(e2)} 不应抛出 {@code ClassCastException}）。
     *
     * <p>此排序保证是 <i>稳定的</i>：相等的元素不会因为排序而重新排序。
     *
     * <p>指定的列表必须是可修改的，但不必是可调整大小的。
     *
     * @implNote
     * 此实现使用指定的列表和一个 {@code null} 比较器调用 {@link List#sort(Comparator)} 方法。
     *
     * @param  <T> 列表中对象的类
     * @param  list 要排序的列表。
     * @throws ClassCastException 如果列表包含不是 <i>相互可比较的</i> 元素（例如，字符串和整数）。
     * @throws UnsupportedOperationException 如果指定列表的列表迭代器不支持 {@code set} 操作。
     * @throws IllegalArgumentException (可选) 如果实现检测到列表元素的自然顺序违反了 {@link Comparable} 合约
     * @see List#sort(Comparator)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> void sort(List<T> list) {
        list.sort(null);
    }

    /**
     * 按照指定比较器确定的顺序对指定列表进行排序。列表中的所有元素都必须是 <i>相互可比较的</i> 使用指定的比较器（即，对于列表中的任何元素 {@code e1} 和 {@code e2}，{@code c.compare(e1, e2)} 不应抛出 {@code ClassCastException}）。
     *
     * <p>此排序保证是 <i>稳定的</i>：相等的元素不会因为排序而重新排序。
     *
     * <p>指定的列表必须是可修改的，但不必是可调整大小的。
     *
     * @implNote
     * 此实现使用指定的列表和比较器调用 {@link List#sort(Comparator)} 方法。
     *
     * @param  <T> 列表中对象的类
     * @param  list 要排序的列表。
     * @param  c 确定列表顺序的比较器。{@code null} 值表示应使用元素的 <i>自然顺序</i>。
     * @throws ClassCastException 如果列表包含不是 <i>相互可比较的</i> 使用指定的比较器的元素。
     * @throws UnsupportedOperationException 如果指定列表的列表迭代器不支持 {@code set} 操作。
     * @throws IllegalArgumentException (可选) 如果比较器违反了 {@link Comparator} 合约
     * @see List#sort(Comparator)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> void sort(List<T> list, Comparator<? super T> c) {
        list.sort(c);
    }


    /**
     * 使用二分查找算法在指定列表中搜索指定对象。列表必须按照其元素的 {@linkplain Comparable 自然顺序} 升序排序（如通过 {@link #sort(List)} 方法）才能调用此方法。如果列表未排序，结果是未定义的。如果列表包含多个与指定对象相等的元素，则不保证找到哪一个。
     *
     * <p>对于“随机访问”列表（提供接近常数时间的位置访问），此方法的运行时间为 log(n)。如果指定列表未实现 {@link RandomAccess} 接口且较大，此方法将执行基于迭代器的二分查找，执行 O(n) 链接遍历和 O(log n) 元素比较。
     *
     * @param  <T> 列表中对象的类
     * @param  list 要搜索的列表。
     * @param  key 要搜索的键。
     * @return 如果键包含在列表中，则返回键的索引；否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。插入点定义为键将被插入到列表中的位置：第一个大于键的元素的索引，或者如果列表中所有元素都小于指定键，则为 <tt>list.size()</tt>。请注意，这保证了如果找到键，返回值将为 &gt;= 0。
     * @throws ClassCastException 如果列表包含不是 <i>相互可比较的</i> 元素（例如，字符串和整数），或者搜索键与列表中的元素不是相互可比较的。
     */
    public static <T>
    int binarySearch(List<? extends Comparable<? super T>> list, T key) {
        if (list instanceof RandomAccess || list.size()<BINARYSEARCH_THRESHOLD)
            return Collections.indexedBinarySearch(list, key);
        else
            return Collections.iteratorBinarySearch(list, key);
    }

    private static <T>
    int indexedBinarySearch(List<? extends Comparable<? super T>> list, T key) {
        int low = 0;
        int high = list.size()-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Comparable<? super T> midVal = list.get(mid);
            int cmp = midVal.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // 键已找到
        }
        return -(low + 1);  // 键未找到
    }

    private static <T>
    int iteratorBinarySearch(List<? extends Comparable<? super T>> list, T key)
    {
        int low = 0;
        int high = list.size()-1;
        ListIterator<? extends Comparable<? super T>> i = list.listIterator();

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Comparable<? super T> midVal = get(i, mid);
            int cmp = midVal.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // 键已找到
        }
        return -(low + 1);  // 键未找到
    }

    /**
     * 通过重新定位指定的列表列表迭代器从给定列表中获取第 i 个元素。
     */
    private static <T> T get(ListIterator<? extends T> i, int index) {
        T obj = null;
        int pos = i.nextIndex();
        if (pos <= index) {
            do {
                obj = i.next();
            } while (pos++ < index);
        } else {
            do {
                obj = i.previous();
            } while (--pos > index);
        }
        return obj;
    }

    /**
     * 使用二分查找算法在指定列表中搜索指定对象。列表必须按照指定的比较器升序排序（如通过 {@link #sort(List, Comparator) sort(List, Comparator)} 方法）才能调用此方法。如果列表未排序，结果是未定义的。如果列表包含多个与指定对象相等的元素，则不保证找到哪一个。
     *
     * <p>对于“随机访问”列表（提供接近常数时间的位置访问），此方法的运行时间为 log(n)。如果指定列表未实现 {@link RandomAccess} 接口且较大，此方法将执行基于迭代器的二分查找，执行 O(n) 链接遍历和 O(log n) 元素比较。
     *
     * @param  <T> 列表中对象的类
     * @param  list 要搜索的列表。
     * @param  key 要搜索的键。
     * @param  c 确定列表顺序的比较器。{@code null} 值表示应使用元素的 {@linkplain Comparable 自然顺序}。
     * @return 如果键包含在列表中，则返回键的索引；否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。插入点定义为键将被插入到列表中的位置：第一个大于键的元素的索引，或者如果列表中所有元素都小于指定键，则为 <tt>list.size()</tt>。请注意，这保证了如果找到键，返回值将为 &gt;= 0。
     * @throws ClassCastException 如果列表包含不是 <i>相互可比较的</i> 使用指定比较器的元素，或者搜索键与列表中的元素使用此比较器不是相互可比较的。
     */
    @SuppressWarnings("unchecked")
    public static <T> int binarySearch(List<? extends T> list, T key, Comparator<? super T> c) {
        if (c==null)
            return binarySearch((List<? extends Comparable<? super T>>) list, key);

        if (list instanceof RandomAccess || list.size()<BINARYSEARCH_THRESHOLD)
            return Collections.indexedBinarySearch(list, key, c);
        else
            return Collections.iteratorBinarySearch(list, key, c);
    }

    private static <T> int indexedBinarySearch(List<? extends T> l, T key, Comparator<? super T> c) {
        int low = 0;
        int high = l.size()-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            T midVal = l.get(mid);
            int cmp = c.compare(midVal, key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // 键已找到
        }
        return -(low + 1);  // 键未找到
    }


                private static <T> int iteratorBinarySearch(List<? extends T> l, T key, Comparator<? super T> c) {
        int low = 0;
        int high = l.size()-1;
        ListIterator<? extends T> i = l.listIterator();

        while (low <= high) {
            int mid = (low + high) >>> 1;
            T midVal = get(i, mid);
            int cmp = c.compare(midVal, key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // 找到键
        }
        return -(low + 1);  // 未找到键
    }

    /**
     * 反转指定列表中的元素顺序。<p>
     *
     * 此方法运行时间为线性时间。
     *
     * @param  list 要反转的列表。
     * @throws UnsupportedOperationException 如果指定的列表或其列表迭代器不支持 <tt>set</tt> 操作。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void reverse(List<?> list) {
        int size = list.size();
        if (size < REVERSE_THRESHOLD || list instanceof RandomAccess) {
            for (int i=0, mid=size>>1, j=size-1; i<mid; i++, j--)
                swap(list, i, j);
        } else {
            // 为了避免使用原始类型，可以捕获通配符，但这需要调用一个辅助的私有方法
            ListIterator fwd = list.listIterator();
            ListIterator rev = list.listIterator(size);
            for (int i=0, mid=list.size()>>1; i<mid; i++) {
                Object tmp = fwd.next();
                fwd.set(rev.previous());
                rev.set(tmp);
            }
        }
    }

    /**
     * 使用默认的随机源随机排列指定的列表。所有排列出现的概率大致相等。
     *
     * <p>在上述描述中使用了“大致”这个词，因为默认的随机源只是一个大约无偏的独立选择位的源。如果它是一个完美的随机位源，那么算法会选择完美的均匀排列。
     *
     * <p>此实现从列表的最后一个元素遍历到第二个元素，反复将一个随机选择的元素交换到“当前位置”。元素是从列表的第一个元素到当前位置（包括当前位置）的范围内随机选择的。
     *
     * <p>此方法运行时间为线性时间。如果指定的列表没有实现 {@link RandomAccess} 接口且列表很大，此实现会先将指定的列表转储到数组中，然后对其进行随机排列，再将随机排列的数组转储回列表。这避免了在原地随机排列“顺序访问”列表时可能出现的二次行为。
     *
     * @param  list 要随机排列的列表。
     * @throws UnsupportedOperationException 如果指定的列表或其列表迭代器不支持 <tt>set</tt> 操作。
     */
    public static void shuffle(List<?> list) {
        Random rnd = r;
        if (rnd == null)
            r = rnd = new Random(); // 无害的竞态条件。
        shuffle(list, rnd);
    }

    private static Random r;

    /**
     * 使用指定的随机源随机排列指定的列表。假设随机源是公平的，所有排列出现的概率相等。<p>
     *
     * 此实现从列表的最后一个元素遍历到第二个元素，反复将一个随机选择的元素交换到“当前位置”。元素是从列表的第一个元素到当前位置（包括当前位置）的范围内随机选择的。<p>
     *
     * 此方法运行时间为线性时间。如果指定的列表没有实现 {@link RandomAccess} 接口且列表很大，此实现会先将指定的列表转储到数组中，然后对其进行随机排列，再将随机排列的数组转储回列表。这避免了在原地随机排列“顺序访问”列表时可能出现的二次行为。
     *
     * @param  list 要随机排列的列表。
     * @param  rnd 用于随机排列列表的随机源。
     * @throws UnsupportedOperationException 如果指定的列表或其列表迭代器不支持 <tt>set</tt> 操作。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void shuffle(List<?> list, Random rnd) {
        int size = list.size();
        if (size < SHUFFLE_THRESHOLD || list instanceof RandomAccess) {
            for (int i=size; i>1; i--)
                swap(list, i-1, rnd.nextInt(i));
        } else {
            Object[] arr = list.toArray();

            // 随机排列数组
            for (int i=size; i>1; i--)
                swap(arr, i-1, rnd.nextInt(i));

            // 将数组转储回列表
            // 为了避免使用原始类型，可以捕获通配符，但这需要调用一个辅助的私有方法
            ListIterator it = list.listIterator();
            for (int i=0; i<arr.length; i++) {
                it.next();
                it.set(arr[i]);
            }
        }
    }

    /**
     * 交换指定列表中指定位置的元素。（如果指定的位置相同，调用此方法不会改变列表。）
     *
     * @param list 要交换元素的列表。
     * @param i 要交换的一个元素的索引。
     * @param j 要交换的另一个元素的索引。
     * @throws IndexOutOfBoundsException 如果 <tt>i</tt> 或 <tt>j</tt> 超出范围 (i &lt; 0 || i &gt;= list.size()
     *         || j &lt; 0 || j &gt;= list.size())。
     * @since 1.4
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void swap(List<?> list, int i, int j) {
        // 为了避免使用原始类型，可以捕获通配符，但这需要调用一个辅助的私有方法
        final List l = list;
        l.set(i, l.set(j, l.get(i)));
    }

    /**
     * 交换指定数组中指定位置的两个元素。
     */
    private static void swap(Object[] arr, int i, int j) {
        Object tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    /**
     * 用指定的元素替换指定列表中的所有元素。<p>
     *
     * 此方法运行时间为线性时间。
     *
     * @param  <T> 列表中对象的类
     * @param  list 要用指定元素填充的列表。
     * @param  obj 用于填充指定列表的元素。
     * @throws UnsupportedOperationException 如果指定的列表或其列表迭代器不支持 <tt>set</tt> 操作。
     */
    public static <T> void fill(List<? super T> list, T obj) {
        int size = list.size();

        if (size < FILL_THRESHOLD || list instanceof RandomAccess) {
            for (int i=0; i<size; i++)
                list.set(i, obj);
        } else {
            ListIterator<? super T> itr = list.listIterator();
            for (int i=0; i<size; i++) {
                itr.next();
                itr.set(obj);
            }
        }
    }

    /**
     * 将一个列表中的所有元素复制到另一个列表中。操作后，目标列表中每个复制的元素的索引将与其在源列表中的索引相同。目标列表必须至少与源列表一样长。如果目标列表更长，剩余的元素不受影响。<p>
     *
     * 此方法运行时间为线性时间。
     *
     * @param  <T> 列表中对象的类
     * @param  dest 目标列表。
     * @param  src 源列表。
     * @throws IndexOutOfBoundsException 如果目标列表太小，无法包含整个源列表。
     * @throws UnsupportedOperationException 如果目标列表的列表迭代器不支持 <tt>set</tt> 操作。
     */
    public static <T> void copy(List<? super T> dest, List<? extends T> src) {
        int srcSize = src.size();
        if (srcSize > dest.size())
            throw new IndexOutOfBoundsException("源列表无法放入目标列表");

        if (srcSize < COPY_THRESHOLD ||
            (src instanceof RandomAccess && dest instanceof RandomAccess)) {
            for (int i=0; i<srcSize; i++)
                dest.set(i, src.get(i));
        } else {
            ListIterator<? super T> di=dest.listIterator();
            ListIterator<? extends T> si=src.listIterator();
            for (int i=0; i<srcSize; i++) {
                di.next();
                di.set(si.next());
            }
        }
    }

    /**
     * 返回给定集合中的最小元素，根据其元素的<i>自然顺序</i>。集合中的所有元素都必须实现 <tt>Comparable</tt> 接口。
     * 此外，集合中的所有元素必须是<i>相互可比较的</i>（即，<tt>e1.compareTo(e2)</tt> 不应为任何元素 <tt>e1</tt> 和 <tt>e2</tt> 抛出 <tt>ClassCastException</tt>）。<p>
     *
     * 此方法遍历整个集合，因此所需时间与集合的大小成正比。
     *
     * @param  <T> 集合中对象的类
     * @param  coll 要确定最小元素的集合。
     * @return 根据其元素的<i>自然顺序</i>，给定集合中的最小元素。
     * @throws ClassCastException 如果集合包含<i>相互不可比较的</i>元素（例如，字符串和整数）。
     * @throws NoSuchElementException 如果集合为空。
     * @see Comparable
     */
    public static <T extends Object & Comparable<? super T>> T min(Collection<? extends T> coll) {
        Iterator<? extends T> i = coll.iterator();
        T candidate = i.next();

        while (i.hasNext()) {
            T next = i.next();
            if (next.compareTo(candidate) < 0)
                candidate = next;
        }
        return candidate;
    }

    /**
     * 返回给定集合中的最小元素，根据指定比较器确定的顺序。集合中的所有元素必须是<i>相互可比较的</i>，由指定的比较器确定（即，<tt>comp.compare(e1, e2)</tt> 不应为任何元素 <tt>e1</tt> 和 <tt>e2</tt> 抛出 <tt>ClassCastException</tt>）。<p>
     *
     * 此方法遍历整个集合，因此所需时间与集合的大小成正比。
     *
     * @param  <T> 集合中对象的类
     * @param  coll 要确定最小元素的集合。
     * @param  comp 用于确定最小元素的比较器。一个 <tt>null</tt> 值表示应使用元素的<i>自然顺序</i>。
     * @return 根据指定比较器，给定集合中的最小元素。
     * @throws ClassCastException 如果集合包含<i>相互不可比较的</i>元素，使用指定的比较器。
     * @throws NoSuchElementException 如果集合为空。
     * @see Comparable
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T min(Collection<? extends T> coll, Comparator<? super T> comp) {
        if (comp==null)
            return (T)min((Collection) coll);

        Iterator<? extends T> i = coll.iterator();
        T candidate = i.next();

        while (i.hasNext()) {
            T next = i.next();
            if (comp.compare(next, candidate) < 0)
                candidate = next;
        }
        return candidate;
    }

    /**
     * 返回给定集合中的最大元素，根据其元素的<i>自然顺序</i>。集合中的所有元素都必须实现 <tt>Comparable</tt> 接口。
     * 此外，集合中的所有元素必须是<i>相互可比较的</i>（即，<tt>e1.compareTo(e2)</tt> 不应为任何元素 <tt>e1</tt> 和 <tt>e2</tt> 抛出 <tt>ClassCastException</tt>）。<p>
     *
     * 此方法遍历整个集合，因此所需时间与集合的大小成正比。
     *
     * @param  <T> 集合中对象的类
     * @param  coll 要确定最大元素的集合。
     * @return 根据其元素的<i>自然顺序</i>，给定集合中的最大元素。
     * @throws ClassCastException 如果集合包含<i>相互不可比较的</i>元素（例如，字符串和整数）。
     * @throws NoSuchElementException 如果集合为空。
     * @see Comparable
     */
    public static <T extends Object & Comparable<? super T>> T max(Collection<? extends T> coll) {
        Iterator<? extends T> i = coll.iterator();
        T candidate = i.next();

        while (i.hasNext()) {
            T next = i.next();
            if (next.compareTo(candidate) > 0)
                candidate = next;
        }
        return candidate;
    }

    /**
     * 返回给定集合中的最大元素，根据指定比较器确定的顺序。集合中的所有元素必须是<i>相互可比较的</i>，由指定的比较器确定（即，<tt>comp.compare(e1, e2)</tt> 不应为任何元素 <tt>e1</tt> 和 <tt>e2</tt> 抛出 <tt>ClassCastException</tt>）。<p>
     *
     * 此方法遍历整个集合，因此所需时间与集合的大小成正比。
     *
     * @param  <T> 集合中对象的类
     * @param  coll 要确定最大元素的集合。
     * @param  comp 用于确定最大元素的比较器。一个 <tt>null</tt> 值表示应使用元素的<i>自然顺序</i>。
     * @return 根据指定比较器，给定集合中的最大元素。
     * @throws ClassCastException 如果集合包含<i>相互不可比较的</i>元素，使用指定的比较器。
     * @throws NoSuchElementException 如果集合为空。
     * @see Comparable
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T max(Collection<? extends T> coll, Comparator<? super T> comp) {
        if (comp==null)
            return (T)max((Collection) coll);


    /**
     * 将指定列表中的元素按指定距离旋转。调用此方法后，索引 <tt>i</tt> 处的元素将是之前索引 <tt>(i - distance)</tt> mod
     * <tt>list.size()</tt> 处的元素，对于 <tt>0</tt> 和 <tt>list.size()-1</tt> 之间的所有 <tt>i</tt> 值，包括两端。 （此方法对列表的大小没有影响。）
     *
     * <p>例如，假设 <tt>list</tt> 包含 <tt>[t, a, n, k, s]</tt>。调用 <tt>Collections.rotate(list, 1)</tt>（或
     * <tt>Collections.rotate(list, -4)</tt>）后，<tt>list</tt> 将包含 <tt>[s, t, a, n, k]</tt>。
     *
     * <p>请注意，此方法可以应用于子列表，以在列表中移动一个或多个元素，同时保留其余元素的顺序。例如，以下惯用法将索引 <tt>j</tt> 处的元素向前移动到位置
     * <tt>k</tt>（必须大于或等于 <tt>j</tt>）：
     * <pre>
     *     Collections.rotate(list.subList(j, k+1), -1);
     * </pre>
     * 为了具体说明，假设 <tt>list</tt> 包含 <tt>[a, b, c, d, e]</tt>。要将索引 <tt>1</tt>
     * （<tt>b</tt>）处的元素向前移动两个位置，执行以下调用：
     * <pre>
     *     Collections.rotate(l.subList(1, 4), -1);
     * </pre>
     * 结果列表是 <tt>[a, c, d, b, e]</tt>。
     *
     * <p>要向前移动多个元素，增加旋转距离的绝对值。要向后移动元素，使用正的移动距离。
     *
     * <p>如果指定的列表很小或实现了 {@link
     * RandomAccess} 接口，此实现将第一个元素交换到它应该去的位置，然后反复交换被置换的元素到它应该去的位置，直到被置换的元素被交换到第一个元素。如果必要，该过程将在第二个和后续元素上重复，直到旋转完成。如果指定的列表很大且不实现
     * <tt>RandomAccess</tt> 接口，此实现将列表在索引 <tt>-distance mod size</tt> 处分成两个子列表视图。然后在每个子列表视图上调用
     * {@link #reverse(List)} 方法，最后在整个列表上调用该方法。关于这两种算法的更完整描述，请参阅 Jon Bentley 的
     * <i>Programming Pearls</i>（Addison-Wesley, 1986）第 2.3 节。
     *
     * @param list 要旋转的列表。
     * @param distance 旋转列表的距离。此值没有限制，可以是零、负数或大于 <tt>list.size()</tt> 的值。
     * @throws UnsupportedOperationException 如果指定的列表或其列表迭代器不支持 <tt>set</tt> 操作。
     * @since 1.4
     */
    public static void rotate(List<?> list, int distance) {
        if (list instanceof RandomAccess || list.size() < ROTATE_THRESHOLD)
            rotate1(list, distance);
        else
            rotate2(list, distance);
    }

    private static <T> void rotate1(List<T> list, int distance) {
        int size = list.size();
        if (size == 0)
            return;
        distance = distance % size;
        if (distance < 0)
            distance += size;
        if (distance == 0)
            return;

        for (int cycleStart = 0, nMoved = 0; nMoved != size; cycleStart++) {
            T displaced = list.get(cycleStart);
            int i = cycleStart;
            do {
                i += distance;
                if (i >= size)
                    i -= size;
                displaced = list.set(i, displaced);
                nMoved ++;
            } while (i != cycleStart);
        }
    }

    private static void rotate2(List<?> list, int distance) {
        int size = list.size();
        if (size == 0)
            return;
        int mid =  -distance % size;
        if (mid < 0)
            mid += size;
        if (mid == 0)
            return;

        reverse(list.subList(0, mid));
        reverse(list.subList(mid, size));
        reverse(list);
    }

    /**
     * 将列表中所有指定值的出现替换为另一个值。更正式地说，将 <tt>list</tt> 中每个等于 <tt>oldVal</tt> 的元素 <tt>e</tt>
     * 替换为 <tt>newVal</tt>。 （此方法对列表的大小没有影响。）
     *
     * @param  <T> 列表中对象的类
     * @param list 要发生替换的列表。
     * @param oldVal 要被替换的旧值。
     * @param newVal 用于替换 <tt>oldVal</tt> 的新值。
     * @return <tt>true</tt> 如果 <tt>list</tt> 包含一个或多个元素 <tt>e</tt>，使得
     *         <tt>(oldVal==null ?  e==null : oldVal.equals(e))</tt>。
     * @throws UnsupportedOperationException 如果指定的列表或其列表迭代器不支持 <tt>set</tt> 操作。
     * @since  1.4
     */
    public static <T> boolean replaceAll(List<T> list, T oldVal, T newVal) {
        boolean result = false;
        int size = list.size();
        if (size < REPLACEALL_THRESHOLD || list instanceof RandomAccess) {
            if (oldVal==null) {
                for (int i=0; i<size; i++) {
                    if (list.get(i)==null) {
                        list.set(i, newVal);
                        result = true;
                    }
                }
            } else {
                for (int i=0; i<size; i++) {
                    if (oldVal.equals(list.get(i))) {
                        list.set(i, newVal);
                        result = true;
                    }
                }
            }
        } else {
            ListIterator<T> itr=list.listIterator();
            if (oldVal==null) {
                for (int i=0; i<size; i++) {
                    if (itr.next()==null) {
                        itr.set(newVal);
                        result = true;
                    }
                }
            } else {
                for (int i=0; i<size; i++) {
                    if (oldVal.equals(itr.next())) {
                        itr.set(newVal);
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 返回指定目标列表在指定源列表中的第一次出现的起始位置，如果没有这样的出现，则返回 -1。更正式地说，返回最低索引 <tt>i</tt>
     * 使得 {@code source.subList(i, i+target.size()).equals(target)}，如果没有这样的索引，则返回 -1。 （如果
     * {@code target.size() > source.size()}，则返回 -1）
     *
     * <p>此实现使用“暴力”技术扫描源列表，依次查找与目标匹配的位置。
     *
     * @param source 要在其中搜索 <tt>target</tt> 第一次出现的列表。
     * @param target 要作为 <tt>source</tt> 的子列表搜索的列表。
     * @return 指定目标列表在指定源列表中的第一次出现的起始位置，如果没有这样的出现，则返回 -1。
     * @since  1.4
     */
    public static int indexOfSubList(List<?> source, List<?> target) {
        int sourceSize = source.size();
        int targetSize = target.size();
        int maxCandidate = sourceSize - targetSize;

        if (sourceSize < INDEXOFSUBLIST_THRESHOLD ||
            (source instanceof RandomAccess&&target instanceof RandomAccess)) {
        nextCand:
            for (int candidate = 0; candidate <= maxCandidate; candidate++) {
                for (int i=0, j=candidate; i<targetSize; i++, j++)
                    if (!eq(target.get(i), source.get(j)))
                        continue nextCand;  // 元素不匹配，尝试下一个候选
                return candidate;  // 候选的所有元素都匹配目标
            }
        } else {  // 上面算法的迭代器版本
            ListIterator<?> si = source.listIterator();
        nextCand:
            for (int candidate = 0; candidate <= maxCandidate; candidate++) {
                ListIterator<?> ti = target.listIterator();
                for (int i=0; i<targetSize; i++) {
                    if (!eq(ti.next(), si.next())) {
                        // 将源迭代器回退到下一个候选
                        for (int j=0; j<i; j++)
                            si.previous();
                        continue nextCand;
                    }
                }
                return candidate;
            }
        }
        return -1;  // 没有候选匹配目标
    }

    /**
     * 返回指定目标列表在指定源列表中的最后一次出现的起始位置，如果没有这样的出现，则返回 -1。更正式地说，返回最高索引 <tt>i</tt>
     * 使得 {@code source.subList(i, i+target.size()).equals(target)}，如果没有这样的索引，则返回 -1。 （如果
     * {@code target.size() > source.size()}，则返回 -1）
     *
     * <p>此实现使用“暴力”技术遍历源列表，依次查找与目标匹配的位置。
     *
     * @param source 要在其中搜索 <tt>target</tt> 最后一次出现的列表。
     * @param target 要作为 <tt>source</tt> 的子列表搜索的列表。
     * @return 指定目标列表在指定源列表中的最后一次出现的起始位置，如果没有这样的出现，则返回 -1。
     * @since  1.4
     */
    public static int lastIndexOfSubList(List<?> source, List<?> target) {
        int sourceSize = source.size();
        int targetSize = target.size();
        int maxCandidate = sourceSize - targetSize;

        if (sourceSize < INDEXOFSUBLIST_THRESHOLD ||
            source instanceof RandomAccess) {   // 索引访问版本
        nextCand:
            for (int candidate = maxCandidate; candidate >= 0; candidate--) {
                for (int i=0, j=candidate; i<targetSize; i++, j++)
                    if (!eq(target.get(i), source.get(j)))
                        continue nextCand;  // 元素不匹配，尝试下一个候选
                return candidate;  // 候选的所有元素都匹配目标
            }
        } else {  // 上面算法的迭代器版本
            if (maxCandidate < 0)
                return -1;
            ListIterator<?> si = source.listIterator(maxCandidate);
        nextCand:
            for (int candidate = maxCandidate; candidate >= 0; candidate--) {
                ListIterator<?> ti = target.listIterator();
                for (int i=0; i<targetSize; i++) {
                    if (!eq(ti.next(), si.next())) {
                        if (candidate != 0) {
                            // 将源迭代器回退到下一个候选
                            for (int j=0; j<=i+1; j++)
                                si.previous();
                        }
                        continue nextCand;
                    }
                }
                return candidate;
            }
        }
        return -1;  // 没有候选匹配目标
    }


    // 不可修改的包装器

    /**
     * 返回指定集合的不可修改视图。此方法允许模块向用户提供“只读”访问内部集合。返回的集合上的查询操作“读取”指定的集合，尝试修改返回的集合，无论是直接还是通过其迭代器，都会导致
     * <tt>UnsupportedOperationException</tt>。<p>
     *
     * 返回的集合不将 <tt>hashCode</tt> 和 <tt>equals</tt> 操作传递给后备集合，而是依赖于
     * <tt>Object</tt> 的 <tt>equals</tt> 和 <tt>hashCode</tt> 方法。这是为了在后备集合是集合或列表的情况下保留这些操作的合同。<p>
     *
     * 如果指定的集合是可序列化的，则返回的集合将是可序列化的。
     *
     * @param  <T> 集合中对象的类
     * @param  c 要返回不可修改视图的集合。
     * @return 指定集合的不可修改视图。
     */
    public static <T> Collection<T> unmodifiableCollection(Collection<? extends T> c) {
        return new UnmodifiableCollection<>(c);
    }

    /**
     * @serial include
     */
    static class UnmodifiableCollection<E> implements Collection<E>, Serializable {
        private static final long serialVersionUID = 1820017752578914078L;

        final Collection<? extends E> c;

        UnmodifiableCollection(Collection<? extends E> c) {
            if (c==null)
                throw new NullPointerException();
            this.c = c;
        }

        public int size()                   {return c.size();}
        public boolean isEmpty()            {return c.isEmpty();}
        public boolean contains(Object o)   {return c.contains(o);}
        public Object[] toArray()           {return c.toArray();}
        public <T> T[] toArray(T[] a)       {return c.toArray(a);}
        public String toString()            {return c.toString();}

        public Iterator<E> iterator() {
            return new Iterator<E>() {
                private final Iterator<? extends E> i = c.iterator();

                public boolean hasNext() {return i.hasNext();}
                public E next()          {return i.next();}
                public void remove() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void forEachRemaining(Consumer<? super E> action) {
                    // 使用后备集合版本
                    i.forEachRemaining(action);
                }
            };
        }

        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection<?> coll) {
            return c.containsAll(coll);
        }
        public boolean addAll(Collection<? extends E> coll) {
            throw new UnsupportedOperationException();
        }
        public boolean removeAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }
        public boolean retainAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }


                    // 覆盖 Collection 中的默认方法
        @Override
        public void forEach(Consumer<? super E> action) {
            c.forEach(action);
        }
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            throw new UnsupportedOperationException();
        }
        @SuppressWarnings("unchecked")
        @Override
        public Spliterator<E> spliterator() {
            return (Spliterator<E>)c.spliterator();
        }
        @SuppressWarnings("unchecked")
        @Override
        public Stream<E> stream() {
            return (Stream<E>)c.stream();
        }
        @SuppressWarnings("unchecked")
        @Override
        public Stream<E> parallelStream() {
            return (Stream<E>)c.parallelStream();
        }
    }

    /**
     * 返回指定集合的不可修改视图。此方法允许模块向用户提供对内部集合的“只读”访问。
     * 返回的集合上的查询操作“穿透”到指定的集合，尝试修改返回的集合，无论是直接还是通过其迭代器，
     * 都会导致 <tt>UnsupportedOperationException</tt>。<p>
     *
     * 如果指定的集合是可序列化的，则返回的集合也将是可序列化的。
     *
     * @param  <T> 集合中对象的类
     * @param  s 要返回不可修改视图的集合。
     * @return 指定集合的不可修改视图。
     */
    public static <T> Set<T> unmodifiableSet(Set<? extends T> s) {
        return new UnmodifiableSet<>(s);
    }

    /**
     * @serial include
     */
    static class UnmodifiableSet<E> extends UnmodifiableCollection<E>
                                 implements Set<E>, Serializable {
        private static final long serialVersionUID = -9215047833775013803L;

        UnmodifiableSet(Set<? extends E> s)     {super(s);}
        public boolean equals(Object o) {return o == this || c.equals(o);}
        public int hashCode()           {return c.hashCode();}
    }

    /**
     * 返回指定排序集合的不可修改视图。此方法允许模块向用户提供对内部排序集合的“只读”访问。
     * 返回的排序集合上的查询操作“穿透”到指定的排序集合。尝试修改返回的排序集合，无论是直接、
     * 通过其迭代器，还是通过其 <tt>subSet</tt>、<tt>headSet</tt> 或 <tt>tailSet</tt> 视图，
     * 都会导致 <tt>UnsupportedOperationException</tt>。<p>
     *
     * 如果指定的排序集合是可序列化的，则返回的排序集合也将是可序列化的。
     *
     * @param  <T> 集合中对象的类
     * @param s 要返回不可修改视图的排序集合。
     * @return 指定排序集合的不可修改视图。
     */
    public static <T> SortedSet<T> unmodifiableSortedSet(SortedSet<T> s) {
        return new UnmodifiableSortedSet<>(s);
    }

    /**
     * @serial include
     */
    static class UnmodifiableSortedSet<E>
                             extends UnmodifiableSet<E>
                             implements SortedSet<E>, Serializable {
        private static final long serialVersionUID = -4929149591599911165L;
        private final SortedSet<E> ss;

        UnmodifiableSortedSet(SortedSet<E> s) {super(s); ss = s;}

        public Comparator<? super E> comparator() {return ss.comparator();}

        public SortedSet<E> subSet(E fromElement, E toElement) {
            return new UnmodifiableSortedSet<>(ss.subSet(fromElement,toElement));
        }
        public SortedSet<E> headSet(E toElement) {
            return new UnmodifiableSortedSet<>(ss.headSet(toElement));
        }
        public SortedSet<E> tailSet(E fromElement) {
            return new UnmodifiableSortedSet<>(ss.tailSet(fromElement));
        }

        public E first()                   {return ss.first();}
        public E last()                    {return ss.last();}
    }

    /**
     * 返回指定可导航集合的不可修改视图。此方法允许模块向用户提供对内部可导航集合的“只读”访问。
     * 返回的可导航集合上的查询操作“穿透”到指定的可导航集合。尝试修改返回的可导航集合，无论是直接、
     * 通过其迭代器，还是通过其 {@code subSet}、{@code headSet} 或 {@code tailSet} 视图，
     * 都会导致 <tt>UnsupportedOperationException</tt>。<p>
     *
     * 如果指定的可导航集合是可序列化的，则返回的可导航集合也将是可序列化的。
     *
     * @param  <T> 集合中对象的类
     * @param s 要返回不可修改视图的可导航集合
     * @return 指定可导航集合的不可修改视图
     * @since 1.8
     */
    public static <T> NavigableSet<T> unmodifiableNavigableSet(NavigableSet<T> s) {
        return new UnmodifiableNavigableSet<>(s);
    }

    /**
     * 包装一个可导航集合并禁用所有修改操作。
     *
     * @param <E> 元素类型
     * @serial include
     */
    static class UnmodifiableNavigableSet<E>
                             extends UnmodifiableSortedSet<E>
                             implements NavigableSet<E>, Serializable {

        private static final long serialVersionUID = -6027448201786391929L;

        /**
         * 用于 {@link #emptyNavigableSet()} 的空不可修改可导航集合的单例实例。
         *
         * @param <E> 元素类型，如果有，以及边界
         */
        private static class EmptyNavigableSet<E> extends UnmodifiableNavigableSet<E>
            implements Serializable {
            private static final long serialVersionUID = -6291252904449939134L;

            public EmptyNavigableSet() {
                super(new TreeSet<E>());
            }

            private Object readResolve()        { return EMPTY_NAVIGABLE_SET; }
        }

        @SuppressWarnings("rawtypes")
        private static final NavigableSet<?> EMPTY_NAVIGABLE_SET =
                new EmptyNavigableSet<>();

        /**
         * 我们保护的实例。
         */
        private final NavigableSet<E> ns;

        UnmodifiableNavigableSet(NavigableSet<E> s)         {super(s); ns = s;}

        public E lower(E e)                             { return ns.lower(e); }
        public E floor(E e)                             { return ns.floor(e); }
        public E ceiling(E e)                         { return ns.ceiling(e); }
        public E higher(E e)                           { return ns.higher(e); }
        public E pollFirst()     { throw new UnsupportedOperationException(); }
        public E pollLast()      { throw new UnsupportedOperationException(); }
        public NavigableSet<E> descendingSet()
                 { return new UnmodifiableNavigableSet<>(ns.descendingSet()); }
        public Iterator<E> descendingIterator()
                                         { return descendingSet().iterator(); }

        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            return new UnmodifiableNavigableSet<>(
                ns.subSet(fromElement, fromInclusive, toElement, toInclusive));
        }

        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return new UnmodifiableNavigableSet<>(
                ns.headSet(toElement, inclusive));
        }

        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return new UnmodifiableNavigableSet<>(
                ns.tailSet(fromElement, inclusive));
        }
    }

    /**
     * 返回指定列表的不可修改视图。此方法允许模块向用户提供对内部列表的“只读”访问。
     * 返回的列表上的查询操作“穿透”到指定的列表，尝试修改返回的列表，无论是直接还是通过其迭代器，
     * 都会导致 <tt>UnsupportedOperationException</tt>。<p>
     *
     * 如果指定的列表是可序列化的，则返回的列表也将是可序列化的。同样，如果指定的列表实现了
     * {@link RandomAccess}，则返回的列表也将实现它。
     *
     * @param  <T> 列表中对象的类
     * @param  list 要返回不可修改视图的列表。
     * @return 指定列表的不可修改视图。
     */
    public static <T> List<T> unmodifiableList(List<? extends T> list) {
        return (list instanceof RandomAccess ?
                new UnmodifiableRandomAccessList<>(list) :
                new UnmodifiableList<>(list));
    }

    /**
     * @serial include
     */
    static class UnmodifiableList<E> extends UnmodifiableCollection<E>
                                  implements List<E> {
        private static final long serialVersionUID = -283967356065247728L;

        final List<? extends E> list;

        UnmodifiableList(List<? extends E> list) {
            super(list);
            this.list = list;
        }

        public boolean equals(Object o) {return o == this || list.equals(o);}
        public int hashCode()           {return list.hashCode();}

        public E get(int index) {return list.get(index);}
        public E set(int index, E element) {
            throw new UnsupportedOperationException();
        }
        public void add(int index, E element) {
            throw new UnsupportedOperationException();
        }
        public E remove(int index) {
            throw new UnsupportedOperationException();
        }
        public int indexOf(Object o)            {return list.indexOf(o);}
        public int lastIndexOf(Object o)        {return list.lastIndexOf(o);}
        public boolean addAll(int index, Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void sort(Comparator<? super E> c) {
            throw new UnsupportedOperationException();
        }

        public ListIterator<E> listIterator()   {return listIterator(0);}

        public ListIterator<E> listIterator(final int index) {
            return new ListIterator<E>() {
                private final ListIterator<? extends E> i
                    = list.listIterator(index);

                public boolean hasNext()     {return i.hasNext();}
                public E next()              {return i.next();}
                public boolean hasPrevious() {return i.hasPrevious();}
                public E previous()          {return i.previous();}
                public int nextIndex()       {return i.nextIndex();}
                public int previousIndex()   {return i.previousIndex();}

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
                    i.forEachRemaining(action);
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            return new UnmodifiableList<>(list.subList(fromIndex, toIndex));
        }

        /**
         * UnmodifiableRandomAccessList 实例被序列化为 UnmodifiableList 实例，以便在预 1.4 JRE 中反序列化
         * （这些 JRE 没有 UnmodifiableRandomAccessList）。此方法反转此转换。作为有益的副作用，
         * 它还将 RandomAccess 标记移植到在预 1.4 JRE 中序列化的 UnmodifiableList 实例。
         *
         * 注意：不幸的是，在 1.4.1 中序列化并在 1.4 中反序列化的 UnmodifiableRandomAccessList 实例
         * 将成为 UnmodifiableList 实例，因为此方法在 1.4 中缺失。
         */
        private Object readResolve() {
            return (list instanceof RandomAccess
                    ? new UnmodifiableRandomAccessList<>(list)
                    : this);
        }
    }

    /**
     * @serial include
     */
    static class UnmodifiableRandomAccessList<E> extends UnmodifiableList<E>
                                              implements RandomAccess
    {
        UnmodifiableRandomAccessList(List<? extends E> list) {
            super(list);
        }

        public List<E> subList(int fromIndex, int toIndex) {
            return new UnmodifiableRandomAccessList<>(
                list.subList(fromIndex, toIndex));
        }

        private static final long serialVersionUID = -2542308836966382001L;

        /**
         * 允许实例在预 1.4 JRE 中反序列化（这些 JRE 没有 UnmodifiableRandomAccessList）。
         * UnmodifiableList 有一个 readResolve 方法，可以在反序列化时反转此转换。
         */
        private Object writeReplace() {
            return new UnmodifiableList<>(list);
        }
    }

    /**
     * 返回指定映射的不可修改视图。此方法允许模块向用户提供对内部映射的“只读”访问。
     * 返回的映射上的查询操作“穿透”到指定的映射，尝试修改返回的映射，无论是直接还是通过其集合视图，
     * 都会导致 <tt>UnsupportedOperationException</tt>。<p>
     *
     * 如果指定的映射是可序列化的，则返回的映射也将是可序列化的。
     *
     * @param <K> 映射键的类
     * @param <V> 映射值的类
     * @param  m 要返回不可修改视图的映射。
     * @return 指定映射的不可修改视图。
     */
    public static <K,V> Map<K,V> unmodifiableMap(Map<? extends K, ? extends V> m) {
        return new UnmodifiableMap<>(m);
    }

    /**
     * @serial include
     */
    private static class UnmodifiableMap<K,V> implements Map<K,V>, Serializable {
        private static final long serialVersionUID = -1034234728574286014L;

        private final Map<? extends K, ? extends V> m;

        UnmodifiableMap(Map<? extends K, ? extends V> m) {
            if (m==null)
                throw new NullPointerException();
            this.m = m;
        }

        public int size()                        {return m.size();}
        public boolean isEmpty()                 {return m.isEmpty();}
        public boolean containsKey(Object key)   {return m.containsKey(key);}
        public boolean containsValue(Object val) {return m.containsValue(val);}
        public V get(Object key)                 {return m.get(key);}

        public V put(K key, V value) {
            throw new UnsupportedOperationException();
        }
        public V remove(Object key) {
            throw new UnsupportedOperationException();
        }
        public void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException();
        }
        public void clear() {
            throw new UnsupportedOperationException();
        }


                    private transient Set<K> keySet;
        private transient Set<Map.Entry<K,V>> entrySet;
        private transient Collection<V> values;

        public Set<K> keySet() {
            if (keySet==null)
                keySet = unmodifiableSet(m.keySet());
            return keySet;
        }

        public Set<Map.Entry<K,V>> entrySet() {
            if (entrySet==null)
                entrySet = new UnmodifiableEntrySet<>(m.entrySet());
            return entrySet;
        }

        public Collection<V> values() {
            if (values==null)
                values = unmodifiableCollection(m.values());
            return values;
        }

        public boolean equals(Object o) {return o == this || m.equals(o);}
        public int hashCode()           {return m.hashCode();}
        public String toString()        {return m.toString();}

        // 覆盖 Map 中的默认方法
        @Override
        @SuppressWarnings("unchecked")
        public V getOrDefault(Object k, V defaultValue) {
            // 安全转换，因为我们不更改值
            return ((Map<K, V>)m).getOrDefault(k, defaultValue);
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            m.forEach(action);
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V putIfAbsent(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V replace(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfPresent(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V compute(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V merge(K key, V value,
                BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        /**
         * 我们需要这个类，除了 UnmodifiableSet 之外，因为 Map.Entries 本身允许通过它们的 setValue 操作修改底层 Map。
         * 这个类很微妙：有许多可能的攻击必须被阻止。
         *
         * @serial include
         */
        static class UnmodifiableEntrySet<K,V>
            extends UnmodifiableSet<Map.Entry<K,V>> {
            private static final long serialVersionUID = 7854390611657943733L;

            @SuppressWarnings({"unchecked", "rawtypes"})
            UnmodifiableEntrySet(Set<? extends Map.Entry<? extends K, ? extends V>> s) {
                // 需要转换为原始类型，以克服类型系统的限制
                super((Set)s);
            }

            static <K, V> Consumer<Map.Entry<K, V>> entryConsumer(Consumer<? super Entry<K, V>> action) {
                return e -> action.accept(new UnmodifiableEntry<>(e));
            }

            public void forEach(Consumer<? super Entry<K, V>> action) {
                Objects.requireNonNull(action);
                c.forEach(entryConsumer(action));
            }

            static final class UnmodifiableEntrySetSpliterator<K, V>
                    implements Spliterator<Entry<K,V>> {
                final Spliterator<Map.Entry<K, V>> s;

                UnmodifiableEntrySetSpliterator(Spliterator<Entry<K, V>> s) {
                    this.s = s;
                }

                @Override
                public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
                    Objects.requireNonNull(action);
                    return s.tryAdvance(entryConsumer(action));
                }

                @Override
                public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
                    Objects.requireNonNull(action);
                    s.forEachRemaining(entryConsumer(action));
                }

                @Override
                public Spliterator<Entry<K, V>> trySplit() {
                    Spliterator<Entry<K, V>> split = s.trySplit();
                    return split == null
                           ? null
                           : new UnmodifiableEntrySetSpliterator<>(split);
                }

                @Override
                public long estimateSize() {
                    return s.estimateSize();
                }

                @Override
                public long getExactSizeIfKnown() {
                    return s.getExactSizeIfKnown();
                }

                @Override
                public int characteristics() {
                    return s.characteristics();
                }

                @Override
                public boolean hasCharacteristics(int characteristics) {
                    return s.hasCharacteristics(characteristics);
                }

                @Override
                public Comparator<? super Entry<K, V>> getComparator() {
                    return s.getComparator();
                }
            }

            @SuppressWarnings("unchecked")
            public Spliterator<Entry<K,V>> spliterator() {
                return new UnmodifiableEntrySetSpliterator<>(
                        (Spliterator<Map.Entry<K, V>>) c.spliterator());
            }

            @Override
            public Stream<Entry<K,V>> stream() {
                return StreamSupport.stream(spliterator(), false);
            }

            @Override
            public Stream<Entry<K,V>> parallelStream() {
                return StreamSupport.stream(spliterator(), true);
            }

            public Iterator<Map.Entry<K,V>> iterator() {
                return new Iterator<Map.Entry<K,V>>() {
                    private final Iterator<? extends Map.Entry<? extends K, ? extends V>> i = c.iterator();

                    public boolean hasNext() {
                        return i.hasNext();
                    }
                    public Map.Entry<K,V> next() {
                        return new UnmodifiableEntry<>(i.next());
                    }
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @SuppressWarnings("unchecked")
            public Object[] toArray() {
                Object[] a = c.toArray();
                for (int i=0; i<a.length; i++)
                    a[i] = new UnmodifiableEntry<>((Map.Entry<? extends K, ? extends V>)a[i]);
                return a;
            }

            @SuppressWarnings("unchecked")
            public <T> T[] toArray(T[] a) {
                // 我们不将 a 传递给 c.toArray，以避免一个多线程的恶意客户端在 c 中获取原始（未包装）的 Entries。
                Object[] arr = c.toArray(a.length==0 ? a : Arrays.copyOf(a, 0));

                for (int i=0; i<arr.length; i++)
                    arr[i] = new UnmodifiableEntry<>((Map.Entry<? extends K, ? extends V>)arr[i]);

                if (arr.length > a.length)
                    return (T[])arr;

                System.arraycopy(arr, 0, a, 0, arr.length);
                if (a.length > arr.length)
                    a[arr.length] = null;
                return a;
            }

            /**
             * 重写此方法以保护底层集合免受具有恶意 equals 函数的对象的攻击，该函数检测到等价候选是 Map.Entry 并调用其 setValue 方法。
             */
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry))
                    return false;
                return c.contains(
                    new UnmodifiableEntry<>((Map.Entry<?,?>) o));
            }

            /**
             * 以下两个方法被重写以防止一个恶意的 List，其 contains(Object o) 方法检测到 o 是 Map.Entry，并调用 o.setValue。
             */
            public boolean containsAll(Collection<?> coll) {
                for (Object e : coll) {
                    if (!contains(e)) // 调用上面的安全 contains()
                        return false;
                }
                return true;
            }
            public boolean equals(Object o) {
                if (o == this)
                    return true;

                if (!(o instanceof Set))
                    return false;
                Set<?> s = (Set<?>) o;
                if (s.size() != c.size())
                    return false;
                return containsAll(s); // 调用上面的安全 containsAll()
            }

            /**
             * 这个“包装类”有两个目的：它通过短路 setValue 方法防止客户端修改底层 Map，并保护底层 Map 免受在执行等价检查时尝试修改另一个 Map Entry 的恶意 Map.Entry。
             */
            private static class UnmodifiableEntry<K,V> implements Map.Entry<K,V> {
                private Map.Entry<? extends K, ? extends V> e;

                UnmodifiableEntry(Map.Entry<? extends K, ? extends V> e)
                        {this.e = Objects.requireNonNull(e);}

                public K getKey()        {return e.getKey();}
                public V getValue()      {return e.getValue();}
                public V setValue(V value) {
                    throw new UnsupportedOperationException();
                }
                public int hashCode()    {return e.hashCode();}
                public boolean equals(Object o) {
                    if (this == o)
                        return true;
                    if (!(o instanceof Map.Entry))
                        return false;
                    Map.Entry<?,?> t = (Map.Entry<?,?>)o;
                    return eq(e.getKey(),   t.getKey()) &&
                           eq(e.getValue(), t.getValue());
                }
                public String toString() {return e.toString();}
            }
        }
    }

    /**
     * 返回指定排序映射的不可修改视图。此方法允许模块向用户提供“只读”访问内部排序映射。返回的排序映射上的查询操作“穿透”到指定的排序映射。尝试修改返回的排序映射，无论是直接修改，通过其集合视图，还是通过其 <tt>subMap</tt>、<tt>headMap</tt> 或 <tt>tailMap</tt> 视图，都会导致 <tt>UnsupportedOperationException</tt>。<p>
     *
     * 如果指定的排序映射是可序列化的，则返回的排序映射也将是可序列化的。
     *
     * @param <K> 映射键的类
     * @param <V> 映射值的类
     * @param m 要返回不可修改视图的排序映射。
     * @return 指定排序映射的不可修改视图。
     */
    public static <K,V> SortedMap<K,V> unmodifiableSortedMap(SortedMap<K, ? extends V> m) {
        return new UnmodifiableSortedMap<>(m);
    }

    /**
     * @serial include
     */
    static class UnmodifiableSortedMap<K,V>
          extends UnmodifiableMap<K,V>
          implements SortedMap<K,V>, Serializable {
        private static final long serialVersionUID = -8806743815996713206L;

        private final SortedMap<K, ? extends V> sm;

        UnmodifiableSortedMap(SortedMap<K, ? extends V> m) {super(m); sm = m; }
        public Comparator<? super K> comparator()   { return sm.comparator(); }
        public SortedMap<K,V> subMap(K fromKey, K toKey)
             { return new UnmodifiableSortedMap<>(sm.subMap(fromKey, toKey)); }
        public SortedMap<K,V> headMap(K toKey)
                     { return new UnmodifiableSortedMap<>(sm.headMap(toKey)); }
        public SortedMap<K,V> tailMap(K fromKey)
                   { return new UnmodifiableSortedMap<>(sm.tailMap(fromKey)); }
        public K firstKey()                           { return sm.firstKey(); }
        public K lastKey()                             { return sm.lastKey(); }
    }

    /**
     * 返回指定导航映射的不可修改视图。此方法允许模块向用户提供“只读”访问内部导航映射。返回的导航映射上的查询操作“穿透”到指定的导航映射。尝试修改返回的导航映射，无论是直接修改，通过其集合视图，还是通过其 {@code subMap}、{@code headMap} 或 {@code tailMap} 视图，都会导致 {@code UnsupportedOperationException}。<p>
     *
     * 如果指定的导航映射是可序列化的，则返回的导航映射也将是可序列化的。
     *
     * @param <K> 映射键的类
     * @param <V> 映射值的类
     * @param m 要返回不可修改视图的导航映射
     * @return 指定导航映射的不可修改视图
     * @since 1.8
     */
    public static <K,V> NavigableMap<K,V> unmodifiableNavigableMap(NavigableMap<K, ? extends V> m) {
        return new UnmodifiableNavigableMap<>(m);
    }

    /**
     * @serial include
     */
    static class UnmodifiableNavigableMap<K,V>
          extends UnmodifiableSortedMap<K,V>
          implements NavigableMap<K,V>, Serializable {
        private static final long serialVersionUID = -4858195264774772197L;

        /**
         * 一个用于 {@link EMPTY_NAVIGABLE_MAP} 的类，需要 readResolve 以保持单例属性。
         *
         * @param <K> 键的类型，如果有，以及边界类型
         * @param <V> 值的类型，如果有
         */
        private static class EmptyNavigableMap<K,V> extends UnmodifiableNavigableMap<K,V>
            implements Serializable {

            private static final long serialVersionUID = -2239321462712562324L;

            EmptyNavigableMap()                       { super(new TreeMap<K,V>()); }

            @Override
            public NavigableSet<K> navigableKeySet()
                                                { return emptyNavigableSet(); }

            private Object readResolve()        { return EMPTY_NAVIGABLE_MAP; }
        }

        /**
         * 单例，用于 {@link emptyNavigableMap()}，同时也是不可变的。
         */
        private static final EmptyNavigableMap<?,?> EMPTY_NAVIGABLE_MAP =
            new EmptyNavigableMap<>();


                    /**
         * 我们包装和保护的实例。
         */
        private final NavigableMap<K, ? extends V> nm;

        UnmodifiableNavigableMap(NavigableMap<K, ? extends V> m)
                                                            {super(m); nm = m;}

        public K lowerKey(K key)                   { return nm.lowerKey(key); }
        public K floorKey(K key)                   { return nm.floorKey(key); }
        public K ceilingKey(K key)               { return nm.ceilingKey(key); }
        public K higherKey(K key)                 { return nm.higherKey(key); }

        @SuppressWarnings("unchecked")
        public Entry<K, V> lowerEntry(K key) {
            Entry<K,V> lower = (Entry<K, V>) nm.lowerEntry(key);
            return (null != lower)
                ? new UnmodifiableEntrySet.UnmodifiableEntry<>(lower)
                : null;
        }

        @SuppressWarnings("unchecked")
        public Entry<K, V> floorEntry(K key) {
            Entry<K,V> floor = (Entry<K, V>) nm.floorEntry(key);
            return (null != floor)
                ? new UnmodifiableEntrySet.UnmodifiableEntry<>(floor)
                : null;
        }

        @SuppressWarnings("unchecked")
        public Entry<K, V> ceilingEntry(K key) {
            Entry<K,V> ceiling = (Entry<K, V>) nm.ceilingEntry(key);
            return (null != ceiling)
                ? new UnmodifiableEntrySet.UnmodifiableEntry<>(ceiling)
                : null;
        }


        @SuppressWarnings("unchecked")
        public Entry<K, V> higherEntry(K key) {
            Entry<K,V> higher = (Entry<K, V>) nm.higherEntry(key);
            return (null != higher)
                ? new UnmodifiableEntrySet.UnmodifiableEntry<>(higher)
                : null;
        }

        @SuppressWarnings("unchecked")
        public Entry<K, V> firstEntry() {
            Entry<K,V> first = (Entry<K, V>) nm.firstEntry();
            return (null != first)
                ? new UnmodifiableEntrySet.UnmodifiableEntry<>(first)
                : null;
        }

        @SuppressWarnings("unchecked")
        public Entry<K, V> lastEntry() {
            Entry<K,V> last = (Entry<K, V>) nm.lastEntry();
            return (null != last)
                ? new UnmodifiableEntrySet.UnmodifiableEntry<>(last)
                : null;
        }

        public Entry<K, V> pollFirstEntry()
                                 { throw new UnsupportedOperationException(); }
        public Entry<K, V> pollLastEntry()
                                 { throw new UnsupportedOperationException(); }
        public NavigableMap<K, V> descendingMap()
                       { return unmodifiableNavigableMap(nm.descendingMap()); }
        public NavigableSet<K> navigableKeySet()
                     { return unmodifiableNavigableSet(nm.navigableKeySet()); }
        public NavigableSet<K> descendingKeySet()
                    { return unmodifiableNavigableSet(nm.descendingKeySet()); }

        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            return unmodifiableNavigableMap(
                nm.subMap(fromKey, fromInclusive, toKey, toInclusive));
        }

        public NavigableMap<K, V> headMap(K toKey, boolean inclusive)
             { return unmodifiableNavigableMap(nm.headMap(toKey, inclusive)); }
        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive)
           { return unmodifiableNavigableMap(nm.tailMap(fromKey, inclusive)); }
    }

    // 同步包装器

    /**
     * 返回一个由指定集合支持的同步（线程安全）集合。为了保证串行访问，至关重要的是
     * <strong>所有</strong> 对支持集合的访问都必须通过返回的集合完成。<p>
     *
     * 用户必须手动同步返回的集合，当通过 {@link Iterator}、{@link Spliterator}
     * 或 {@link Stream} 遍历它时：
     * <pre>
     *  Collection c = Collections.synchronizedCollection(myCollection);
     *     ...
     *  synchronized (c) {
     *      Iterator i = c.iterator(); // 必须在同步块中
     *      while (i.hasNext())
     *         foo(i.next());
     *  }
     * </pre>
     * 不遵循此建议可能导致不确定的行为。
     *
     * <p>返回的集合不会将 {@code hashCode} 和 {@code equals} 操作传递给支持集合，
     * 而是依赖于 {@code Object} 的 equals 和 hashCode 方法。这是为了在支持集合是
     * 集合或列表时保留这些操作的合同。<p>
     *
     * 如果指定的集合是可序列化的，则返回的集合也将是可序列化的。
     *
     * @param  <T> 集合中对象的类
     * @param  c 要“包装”在同步集合中的集合。
     * @return 指定集合的同步视图。
     */
    public static <T> Collection<T> synchronizedCollection(Collection<T> c) {
        return new SynchronizedCollection<>(c);
    }

    static <T> Collection<T> synchronizedCollection(Collection<T> c, Object mutex) {
        return new SynchronizedCollection<>(c, mutex);
    }

    /**
     * @serial include
     */
    static class SynchronizedCollection<E> implements Collection<E>, Serializable {
        private static final long serialVersionUID = 3053995032091335093L;

        final Collection<E> c;  // 支持集合
        final Object mutex;     // 要同步的对象

        SynchronizedCollection(Collection<E> c) {
            this.c = Objects.requireNonNull(c);
            mutex = this;
        }

        SynchronizedCollection(Collection<E> c, Object mutex) {
            this.c = Objects.requireNonNull(c);
            this.mutex = Objects.requireNonNull(mutex);
        }

        public int size() {
            synchronized (mutex) {return c.size();}
        }
        public boolean isEmpty() {
            synchronized (mutex) {return c.isEmpty();}
        }
        public boolean contains(Object o) {
            synchronized (mutex) {return c.contains(o);}
        }
        public Object[] toArray() {
            synchronized (mutex) {return c.toArray();}
        }
        public <T> T[] toArray(T[] a) {
            synchronized (mutex) {return c.toArray(a);}
        }

        public Iterator<E> iterator() {
            return c.iterator(); // 必须由用户手动同步！
        }

        public boolean add(E e) {
            synchronized (mutex) {return c.add(e);}
        }
        public boolean remove(Object o) {
            synchronized (mutex) {return c.remove(o);}
        }

        public boolean containsAll(Collection<?> coll) {
            synchronized (mutex) {return c.containsAll(coll);}
        }
        public boolean addAll(Collection<? extends E> coll) {
            synchronized (mutex) {return c.addAll(coll);}
        }
        public boolean removeAll(Collection<?> coll) {
            synchronized (mutex) {return c.removeAll(coll);}
        }
        public boolean retainAll(Collection<?> coll) {
            synchronized (mutex) {return c.retainAll(coll);}
        }
        public void clear() {
            synchronized (mutex) {c.clear();}
        }
        public String toString() {
            synchronized (mutex) {return c.toString();}
        }
        // 覆盖 Collection 中的默认方法
        @Override
        public void forEach(Consumer<? super E> consumer) {
            synchronized (mutex) {c.forEach(consumer);}
        }
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            synchronized (mutex) {return c.removeIf(filter);}
        }
        @Override
        public Spliterator<E> spliterator() {
            return c.spliterator(); // 必须由用户手动同步！
        }
        @Override
        public Stream<E> stream() {
            return c.stream(); // 必须由用户手动同步！
        }
        @Override
        public Stream<E> parallelStream() {
            return c.parallelStream(); // 必须由用户手动同步！
        }
        private void writeObject(ObjectOutputStream s) throws IOException {
            synchronized (mutex) {s.defaultWriteObject();}
        }
    }

    /**
     * 返回一个由指定集合支持的同步（线程安全）集合。为了保证串行访问，至关重要的是
     * <strong>所有</strong> 对支持集合的访问都必须通过返回的集合完成。<p>
     *
     * 用户必须手动同步返回的集合，当通过 {@link Iterator}、{@link Spliterator}
     * 或 {@link Stream} 遍历它时：
     * <pre>
     *  Set s = Collections.synchronizedSet(new HashSet());
     *      ...
     *  synchronized (s) {
     *      Iterator i = s.iterator(); // 必须在同步块中
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * 不遵循此建议可能导致不确定的行为。
     *
     * <p>如果指定的集合是可序列化的，则返回的集合也将是可序列化的。
     *
     * @param  <T> 集合中对象的类
     * @param  s 要“包装”在同步集合中的集合。
     * @return 指定集合的同步视图。
     */
    public static <T> Set<T> synchronizedSet(Set<T> s) {
        return new SynchronizedSet<>(s);
    }

    static <T> Set<T> synchronizedSet(Set<T> s, Object mutex) {
        return new SynchronizedSet<>(s, mutex);
    }

    /**
     * @serial include
     */
    static class SynchronizedSet<E>
          extends SynchronizedCollection<E>
          implements Set<E> {
        private static final long serialVersionUID = 487447009682186044L;

        SynchronizedSet(Set<E> s) {
            super(s);
        }
        SynchronizedSet(Set<E> s, Object mutex) {
            super(s, mutex);
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            synchronized (mutex) {return c.equals(o);}
        }
        public int hashCode() {
            synchronized (mutex) {return c.hashCode();}
        }
    }

    /**
     * 返回一个由指定排序集合支持的同步（线程安全）排序集合。为了保证串行访问，至关重要的是
     * <strong>所有</strong> 对支持排序集合的访问都必须通过返回的排序集合（或其视图）完成。<p>
     *
     * 用户必须手动同步返回的排序集合，当遍历它或其任何 <tt>subSet</tt>、
     * <tt>headSet</tt> 或 <tt>tailSet</tt> 视图时。
     * <pre>
     *  SortedSet s = Collections.synchronizedSortedSet(new TreeSet());
     *      ...
     *  synchronized (s) {
     *      Iterator i = s.iterator(); // 必须在同步块中
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * 或：
     * <pre>
     *  SortedSet s = Collections.synchronizedSortedSet(new TreeSet());
     *  SortedSet s2 = s.headSet(foo);
     *      ...
     *  synchronized (s) {  // 注意：s，而不是 s2!!!
     *      Iterator i = s2.iterator(); // 必须在同步块中
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * 不遵循此建议可能导致不确定的行为。
     *
     * <p>如果指定的排序集合是可序列化的，则返回的排序集合也将是可序列化的。
     *
     * @param  <T> 集合中对象的类
     * @param  s 要“包装”在同步排序集合中的排序集合。
     * @return 指定排序集合的同步视图。
     */
    public static <T> SortedSet<T> synchronizedSortedSet(SortedSet<T> s) {
        return new SynchronizedSortedSet<>(s);
    }

    /**
     * @serial include
     */
    static class SynchronizedSortedSet<E>
        extends SynchronizedSet<E>
        implements SortedSet<E>
    {
        private static final long serialVersionUID = 8695801310862127406L;

        private final SortedSet<E> ss;

        SynchronizedSortedSet(SortedSet<E> s) {
            super(s);
            ss = s;
        }
        SynchronizedSortedSet(SortedSet<E> s, Object mutex) {
            super(s, mutex);
            ss = s;
        }

        public Comparator<? super E> comparator() {
            synchronized (mutex) {return ss.comparator();}
        }

        public SortedSet<E> subSet(E fromElement, E toElement) {
            synchronized (mutex) {
                return new SynchronizedSortedSet<>(
                    ss.subSet(fromElement, toElement), mutex);
            }
        }
        public SortedSet<E> headSet(E toElement) {
            synchronized (mutex) {
                return new SynchronizedSortedSet<>(ss.headSet(toElement), mutex);
            }
        }
        public SortedSet<E> tailSet(E fromElement) {
            synchronized (mutex) {
               return new SynchronizedSortedSet<>(ss.tailSet(fromElement),mutex);
            }
        }

        public E first() {
            synchronized (mutex) {return ss.first();}
        }
        public E last() {
            synchronized (mutex) {return ss.last();}
        }
    }

    /**
     * 返回一个由指定可导航集合支持的同步（线程安全）可导航集合。为了保证串行访问，至关重要的是
     * <strong>所有</strong> 对支持可导航集合的访问都必须通过返回的可导航集合（或其视图）完成。<p>
     *
     * 用户必须手动同步返回的可导航集合，当遍历它或其任何 {@code subSet}、
     * {@code headSet} 或 {@code tailSet} 视图时。
     * <pre>
     *  NavigableSet s = Collections.synchronizedNavigableSet(new TreeSet());
     *      ...
     *  synchronized (s) {
     *      Iterator i = s.iterator(); // 必须在同步块中
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * 或：
     * <pre>
     *  NavigableSet s = Collections.synchronizedNavigableSet(new TreeSet());
     *  NavigableSet s2 = s.headSet(foo, true);
     *      ...
     *  synchronized (s) {  // 注意：s，而不是 s2!!!
     *      Iterator i = s2.iterator(); // 必须在同步块中
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * 不遵循此建议可能导致不确定的行为。
     *
     * <p>如果指定的可导航集合是可序列化的，则返回的可导航集合也将是可序列化的。
     *
     * @param  <T> 集合中对象的类
     * @param  s 要“包装”在同步可导航集合中的可导航集合。
     * @return 指定可导航集合的同步视图
     * @since 1.8
     */
    public static <T> NavigableSet<T> synchronizedNavigableSet(NavigableSet<T> s) {
        return new SynchronizedNavigableSet<>(s);
    }

    /**
     * @serial include
     */
    static class SynchronizedNavigableSet<E>
        extends SynchronizedSortedSet<E>
        implements NavigableSet<E>
    {
        private static final long serialVersionUID = -5505529816273629798L;


                    private final NavigableSet<E> ns;

        SynchronizedNavigableSet(NavigableSet<E> s) {
            super(s);
            ns = s;
        }

        SynchronizedNavigableSet(NavigableSet<E> s, Object mutex) {
            super(s, mutex);
            ns = s;
        }
        public E lower(E e)      { synchronized (mutex) {return ns.lower(e);} }
        public E floor(E e)      { synchronized (mutex) {return ns.floor(e);} }
        public E ceiling(E e)  { synchronized (mutex) {return ns.ceiling(e);} }
        public E higher(E e)    { synchronized (mutex) {return ns.higher(e);} }
        public E pollFirst()  { synchronized (mutex) {return ns.pollFirst();} }
        public E pollLast()    { synchronized (mutex) {return ns.pollLast();} }

        public NavigableSet<E> descendingSet() {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.descendingSet(), mutex);
            }
        }

        public Iterator<E> descendingIterator()
                 { synchronized (mutex) { return descendingSet().iterator(); } }

        public NavigableSet<E> subSet(E fromElement, E toElement) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.subSet(fromElement, true, toElement, false), mutex);
            }
        }
        public NavigableSet<E> headSet(E toElement) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.headSet(toElement, false), mutex);
            }
        }
        public NavigableSet<E> tailSet(E fromElement) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.tailSet(fromElement, true), mutex);
            }
        }

        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.subSet(fromElement, fromInclusive, toElement, toInclusive), mutex);
            }
        }

        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.headSet(toElement, inclusive), mutex);
            }
        }

        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.tailSet(fromElement, inclusive), mutex);
            }
        }
    }

    /**
     * 返回一个由指定列表支持的同步（线程安全）列表。为了保证串行访问，至关重要的是，对支持列表的所有访问都必须通过返回的列表完成。<p>
     *
     * 用户必须手动同步返回的列表，当遍历它时：
     * <pre>
     *  List list = Collections.synchronizedList(new ArrayList());
     *      ...
     *  synchronized (list) {
     *      Iterator i = list.iterator(); // 必须在同步块内
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * 不遵循此建议可能导致不确定的行为。
     *
     * <p>如果指定的列表是可序列化的，则返回的列表也将是可序列化的。
     *
     * @param  <T> 列表中对象的类
     * @param  list 要“包装”在同步列表中的列表。
     * @return 指定列表的同步视图。
     */
    public static <T> List<T> synchronizedList(List<T> list) {
        return (list instanceof RandomAccess ?
                new SynchronizedRandomAccessList<>(list) :
                new SynchronizedList<>(list));
    }

    static <T> List<T> synchronizedList(List<T> list, Object mutex) {
        return (list instanceof RandomAccess ?
                new SynchronizedRandomAccessList<>(list, mutex) :
                new SynchronizedList<>(list, mutex));
    }

    /**
     * @serial include
     */
    static class SynchronizedList<E>
        extends SynchronizedCollection<E>
        implements List<E> {
        private static final long serialVersionUID = -7754090372962971524L;

        final List<E> list;

        SynchronizedList(List<E> list) {
            super(list);
            this.list = list;
        }
        SynchronizedList(List<E> list, Object mutex) {
            super(list, mutex);
            this.list = list;
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            synchronized (mutex) {return list.equals(o);}
        }
        public int hashCode() {
            synchronized (mutex) {return list.hashCode();}
        }

        public E get(int index) {
            synchronized (mutex) {return list.get(index);}
        }
        public E set(int index, E element) {
            synchronized (mutex) {return list.set(index, element);}
        }
        public void add(int index, E element) {
            synchronized (mutex) {list.add(index, element);}
        }
        public E remove(int index) {
            synchronized (mutex) {return list.remove(index);}
        }

        public int indexOf(Object o) {
            synchronized (mutex) {return list.indexOf(o);}
        }
        public int lastIndexOf(Object o) {
            synchronized (mutex) {return list.lastIndexOf(o);}
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            synchronized (mutex) {return list.addAll(index, c);}
        }

        public ListIterator<E> listIterator() {
            return list.listIterator(); // 必须由用户手动同步
        }

        public ListIterator<E> listIterator(int index) {
            return list.listIterator(index); // 必须由用户手动同步
        }

        public List<E> subList(int fromIndex, int toIndex) {
            synchronized (mutex) {
                return new SynchronizedList<>(list.subList(fromIndex, toIndex),
                                            mutex);
            }
        }

        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            synchronized (mutex) {list.replaceAll(operator);}
        }
        @Override
        public void sort(Comparator<? super E> c) {
            synchronized (mutex) {list.sort(c);}
        }

        /**
         * SynchronizedRandomAccessList 实例被序列化为 SynchronizedList 实例，以便它们可以在预 1.4 JRE（不支持 SynchronizedRandomAccessList）中反序列化。
         * 此方法反转了转换。作为有益的副作用，它还将 RandomAccess 标记接口嫁接到在预 1.4 JRE 中序列化的 SynchronizedList 实例上。
         *
         * 注意：不幸的是，在 1.4.1 中序列化的 SynchronizedRandomAccessList 实例并在 1.4 中反序列化的实例将变为 SynchronizedList 实例，因为此方法在 1.4 中缺失。
         */
        private Object readResolve() {
            return (list instanceof RandomAccess
                    ? new SynchronizedRandomAccessList<>(list)
                    : this);
        }
    }

    /**
     * @serial include
     */
    static class SynchronizedRandomAccessList<E>
        extends SynchronizedList<E>
        implements RandomAccess {

        SynchronizedRandomAccessList(List<E> list) {
            super(list);
        }

        SynchronizedRandomAccessList(List<E> list, Object mutex) {
            super(list, mutex);
        }

        public List<E> subList(int fromIndex, int toIndex) {
            synchronized (mutex) {
                return new SynchronizedRandomAccessList<>(
                    list.subList(fromIndex, toIndex), mutex);
            }
        }

        private static final long serialVersionUID = 1530674583602358482L;

        /**
         * 允许实例在预 1.4 JRE（不支持 SynchronizedRandomAccessList）中被序列化。SynchronizedList 有一个 readResolve 方法，在反序列化时会反转此转换。
         */
        private Object writeReplace() {
            return new SynchronizedList<>(list);
        }
    }

    /**
     * 返回一个由指定映射支持的同步（线程安全）映射。为了保证串行访问，至关重要的是，对支持映射的所有访问都必须通过返回的映射完成。<p>
     *
     * 用户必须手动同步返回的映射，当遍历其任何集合视图时：
     * <pre>
     *  Map m = Collections.synchronizedMap(new HashMap());
     *      ...
     *  Set s = m.keySet();  // 无需在同步块内
     *      ...
     *  synchronized (m) {  // 同步在 m 上，而不是 s 上！
     *      Iterator i = s.iterator(); // 必须在同步块内
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * 不遵循此建议可能导致不确定的行为。
     *
     * <p>如果指定的映射是可序列化的，则返回的映射也将是可序列化的。
     *
     * @param <K> 映射键的类
     * @param <V> 映射值的类
     * @param  m 要“包装”在同步映射中的映射。
     * @return 指定映射的同步视图。
     */
    public static <K,V> Map<K,V> synchronizedMap(Map<K,V> m) {
        return new SynchronizedMap<>(m);
    }

    /**
     * @serial include
     */
    private static class SynchronizedMap<K,V>
        implements Map<K,V>, Serializable {
        private static final long serialVersionUID = 1978198479659022715L;

        private final Map<K,V> m;     // 支持的映射
        final Object      mutex;        // 要同步的对象

        SynchronizedMap(Map<K,V> m) {
            this.m = Objects.requireNonNull(m);
            mutex = this;
        }

        SynchronizedMap(Map<K,V> m, Object mutex) {
            this.m = m;
            this.mutex = mutex;
        }

        public int size() {
            synchronized (mutex) {return m.size();}
        }
        public boolean isEmpty() {
            synchronized (mutex) {return m.isEmpty();}
        }
        public boolean containsKey(Object key) {
            synchronized (mutex) {return m.containsKey(key);}
        }
        public boolean containsValue(Object value) {
            synchronized (mutex) {return m.containsValue(value);}
        }
        public V get(Object key) {
            synchronized (mutex) {return m.get(key);}
        }

        public V put(K key, V value) {
            synchronized (mutex) {return m.put(key, value);}
        }
        public V remove(Object key) {
            synchronized (mutex) {return m.remove(key);}
        }
        public void putAll(Map<? extends K, ? extends V> map) {
            synchronized (mutex) {m.putAll(map);}
        }
        public void clear() {
            synchronized (mutex) {m.clear();}
        }

        private transient Set<K> keySet;
        private transient Set<Map.Entry<K,V>> entrySet;
        private transient Collection<V> values;

        public Set<K> keySet() {
            synchronized (mutex) {
                if (keySet==null)
                    keySet = new SynchronizedSet<>(m.keySet(), mutex);
                return keySet;
            }
        }

        public Set<Map.Entry<K,V>> entrySet() {
            synchronized (mutex) {
                if (entrySet==null)
                    entrySet = new SynchronizedSet<>(m.entrySet(), mutex);
                return entrySet;
            }
        }

        public Collection<V> values() {
            synchronized (mutex) {
                if (values==null)
                    values = new SynchronizedCollection<>(m.values(), mutex);
                return values;
            }
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            synchronized (mutex) {return m.equals(o);}
        }
        public int hashCode() {
            synchronized (mutex) {return m.hashCode();}
        }
        public String toString() {
            synchronized (mutex) {return m.toString();}
        }

        // 重写 Map 中的默认方法
        @Override
        public V getOrDefault(Object k, V defaultValue) {
            synchronized (mutex) {return m.getOrDefault(k, defaultValue);}
        }
        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            synchronized (mutex) {m.forEach(action);}
        }
        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            synchronized (mutex) {m.replaceAll(function);}
        }
        @Override
        public V putIfAbsent(K key, V value) {
            synchronized (mutex) {return m.putIfAbsent(key, value);}
        }
        @Override
        public boolean remove(Object key, Object value) {
            synchronized (mutex) {return m.remove(key, value);}
        }
        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            synchronized (mutex) {return m.replace(key, oldValue, newValue);}
        }
        @Override
        public V replace(K key, V value) {
            synchronized (mutex) {return m.replace(key, value);}
        }
        @Override
        public V computeIfAbsent(K key,
                Function<? super K, ? extends V> mappingFunction) {
            synchronized (mutex) {return m.computeIfAbsent(key, mappingFunction);}
        }
        @Override
        public V computeIfPresent(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            synchronized (mutex) {return m.computeIfPresent(key, remappingFunction);}
        }
        @Override
        public V compute(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            synchronized (mutex) {return m.compute(key, remappingFunction);}
        }
        @Override
        public V merge(K key, V value,
                BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            synchronized (mutex) {return m.merge(key, value, remappingFunction);}
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            synchronized (mutex) {s.defaultWriteObject();}
        }
    }

    /**
     * 返回一个由指定排序映射支持的同步（线程安全）排序映射。为了保证串行访问，至关重要的是，对支持排序映射的所有访问都必须通过返回的排序映射（或其视图）完成。<p>
     *
     * 用户必须手动同步返回的排序映射，当遍历其任何集合视图，或其 <tt>subMap</tt>、<tt>headMap</tt> 或 <tt>tailMap</tt> 视图的集合视图时。
     * <pre>
     *  SortedMap m = Collections.synchronizedSortedMap(new TreeMap());
     *      ...
     *  Set s = m.keySet();  // 无需在同步块内
     *      ...
     *  synchronized (m) {  // 同步在 m 上，而不是 s 上！
     *      Iterator i = s.iterator(); // 必须在同步块内
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * 或者：
     * <pre>
     *  SortedMap m = Collections.synchronizedSortedMap(new TreeMap());
     *  SortedMap m2 = m.subMap(foo, bar);
     *      ...
     *  Set s2 = m2.keySet();  // 无需在同步块内
     *      ...
     *  synchronized (m) {  // 同步在 m 上，而不是 m2 或 s2 上！
     *      Iterator i = s.iterator(); // 必须在同步块内
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * 不遵循此建议可能导致不确定的行为。
     *
     * <p>如果指定的排序映射是可序列化的，则返回的排序映射也将是可序列化的。
     *
     * @param <K> 映射键的类
     * @param <V> 映射值的类
     * @param  m 要“包装”在同步排序映射中的排序映射。
     * @return 指定排序映射的同步视图。
     */
    public static <K,V> SortedMap<K,V> synchronizedSortedMap(SortedMap<K,V> m) {
        return new SynchronizedSortedMap<>(m);
    }


                /**
     * @serial include
     */
    static class SynchronizedSortedMap<K,V>
        extends SynchronizedMap<K,V>
        implements SortedMap<K,V>
    {
        private static final long serialVersionUID = -8798146769416483793L;

        private final SortedMap<K,V> sm;

        SynchronizedSortedMap(SortedMap<K,V> m) {
            super(m);
            sm = m;
        }
        SynchronizedSortedMap(SortedMap<K,V> m, Object mutex) {
            super(m, mutex);
            sm = m;
        }

        public Comparator<? super K> comparator() {
            synchronized (mutex) {return sm.comparator();}
        }

        public SortedMap<K,V> subMap(K fromKey, K toKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap<>(
                    sm.subMap(fromKey, toKey), mutex);
            }
        }
        public SortedMap<K,V> headMap(K toKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap<>(sm.headMap(toKey), mutex);
            }
        }
        public SortedMap<K,V> tailMap(K fromKey) {
            synchronized (mutex) {
               return new SynchronizedSortedMap<>(sm.tailMap(fromKey),mutex);
            }
        }

        public K firstKey() {
            synchronized (mutex) {return sm.firstKey();}
        }
        public K lastKey() {
            synchronized (mutex) {return sm.lastKey();}
        }
    }

    /**
     * 返回一个由指定的可导航映射支持的同步（线程安全）可导航映射。为了保证串行访问，至关重要的是，对支持的可导航映射的所有访问都必须通过返回的可导航映射（或其视图）完成。<p>
     *
     * 用户必须手动同步返回的可导航映射，当迭代其任何集合视图，或其 {@code subMap}、{@code headMap} 或 {@code tailMap} 视图的集合视图时。
     * <pre>
     *  NavigableMap m = Collections.synchronizedNavigableMap(new TreeMap());
     *      ...
     *  Set s = m.keySet();  // 无需在同步块中
     *      ...
     *  synchronized (m) {  // 同步在 m 上，而不是 s 上！
     *      Iterator i = s.iterator(); // 必须在同步块中
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * 或：
     * <pre>
     *  NavigableMap m = Collections.synchronizedNavigableMap(new TreeMap());
     *  NavigableMap m2 = m.subMap(foo, true, bar, false);
     *      ...
     *  Set s2 = m2.keySet();  // 无需在同步块中
     *      ...
     *  synchronized (m) {  // 同步在 m 上，而不是 m2 或 s2 上！
     *      Iterator i = s.iterator(); // 必须在同步块中
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * 不遵循此建议可能导致非确定性行为。
     *
     * <p>如果指定的可导航映射是可序列化的，则返回的可导航映射也将是可序列化的。
     *
     * @param <K> 映射键的类
     * @param <V> 映射值的类
     * @param  m 要“包装”在同步可导航映射中的可导航映射
     * @return 指定可导航映射的同步视图。
     * @since 1.8
     */
    public static <K,V> NavigableMap<K,V> synchronizedNavigableMap(NavigableMap<K,V> m) {
        return new SynchronizedNavigableMap<>(m);
    }

    /**
     * 同步的可导航映射。
     *
     * @serial include
     */
    static class SynchronizedNavigableMap<K,V>
        extends SynchronizedSortedMap<K,V>
        implements NavigableMap<K,V>
    {
        private static final long serialVersionUID = 699392247599746807L;

        private final NavigableMap<K,V> nm;

        SynchronizedNavigableMap(NavigableMap<K,V> m) {
            super(m);
            nm = m;
        }
        SynchronizedNavigableMap(NavigableMap<K,V> m, Object mutex) {
            super(m, mutex);
            nm = m;
        }

        public Entry<K, V> lowerEntry(K key)
                        { synchronized (mutex) { return nm.lowerEntry(key); } }
        public K lowerKey(K key)
                          { synchronized (mutex) { return nm.lowerKey(key); } }
        public Entry<K, V> floorEntry(K key)
                        { synchronized (mutex) { return nm.floorEntry(key); } }
        public K floorKey(K key)
                          { synchronized (mutex) { return nm.floorKey(key); } }
        public Entry<K, V> ceilingEntry(K key)
                      { synchronized (mutex) { return nm.ceilingEntry(key); } }
        public K ceilingKey(K key)
                        { synchronized (mutex) { return nm.ceilingKey(key); } }
        public Entry<K, V> higherEntry(K key)
                       { synchronized (mutex) { return nm.higherEntry(key); } }
        public K higherKey(K key)
                         { synchronized (mutex) { return nm.higherKey(key); } }
        public Entry<K, V> firstEntry()
                           { synchronized (mutex) { return nm.firstEntry(); } }
        public Entry<K, V> lastEntry()
                            { synchronized (mutex) { return nm.lastEntry(); } }
        public Entry<K, V> pollFirstEntry()
                       { synchronized (mutex) { return nm.pollFirstEntry(); } }
        public Entry<K, V> pollLastEntry()
                        { synchronized (mutex) { return nm.pollLastEntry(); } }

        public NavigableMap<K, V> descendingMap() {
            synchronized (mutex) {
                return
                    new SynchronizedNavigableMap<>(nm.descendingMap(), mutex);
            }
        }

        public NavigableSet<K> keySet() {
            return navigableKeySet();
        }

        public NavigableSet<K> navigableKeySet() {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(nm.navigableKeySet(), mutex);
            }
        }

        public NavigableSet<K> descendingKeySet() {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(nm.descendingKeySet(), mutex);
            }
        }


        public SortedMap<K,V> subMap(K fromKey, K toKey) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(
                    nm.subMap(fromKey, true, toKey, false), mutex);
            }
        }
        public SortedMap<K,V> headMap(K toKey) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(nm.headMap(toKey, false), mutex);
            }
        }
        public SortedMap<K,V> tailMap(K fromKey) {
            synchronized (mutex) {
        return new SynchronizedNavigableMap<>(nm.tailMap(fromKey, true),mutex);
            }
        }

        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(
                    nm.subMap(fromKey, fromInclusive, toKey, toInclusive), mutex);
            }
        }

        public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(
                        nm.headMap(toKey, inclusive), mutex);
            }
        }

        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(
                    nm.tailMap(fromKey, inclusive), mutex);
            }
        }
    }

    // 动态类型安全的集合包装器

    /**
     * 返回指定集合的动态类型安全视图。任何尝试插入错误类型的元素都将立即导致 {@link ClassCastException}。假设在生成动态类型安全视图之前集合中没有错误类型的元素，并且所有后续对集合的访问都通过视图进行，则可以 <i>保证</i> 集合中不会包含错误类型的元素。
     *
     * <p>语言中的泛型机制提供了编译时（静态）类型检查，但可以通过未检查的类型转换来绕过此机制。通常这不是问题，因为编译器会在所有此类未检查操作上发出警告。然而，有时仅静态类型检查是不够的。例如，假设将集合传递给第三方库，并且必须确保库代码不会通过插入错误类型的元素来破坏集合。
     *
     * <p>动态类型安全视图的另一个用途是调试。假设程序因 {@code ClassCastException} 而失败，表明已将错误类型的元素放入参数化集合中。不幸的是，异常可以在错误元素插入后任何时候发生，因此通常提供很少或没有关于问题真正来源的信息。如果问题可以重现，可以通过临时修改程序以使用动态类型安全视图来快速确定问题的来源。例如，此声明：
     *  <pre> {@code
     *     Collection<String> c = new HashSet<>();
     * }</pre>
     * 可以临时替换为：
     *  <pre> {@code
     *     Collection<String> c = Collections.checkedCollection(
     *         new HashSet<>(), String.class);
     * }</pre>
     * 再次运行程序将导致它在尝试将错误类型的元素插入集合时失败，从而明确地识别问题的来源。一旦问题得到解决，可以将修改后的声明恢复为原始声明。
     *
     * <p>返回的集合不将 {@code hashCode} 和 {@code equals} 操作传递给支持的集合，而是依赖于 {@code Object} 的 {@code equals} 和 {@code hashCode} 方法。这是为了在支持的集合是集合或列表时保持这些操作的契约。
     *
     * <p>如果指定的集合是可序列化的，则返回的集合也将是可序列化的。
     *
     * <p>由于 {@code null} 被认为是任何引用类型的值，因此如果支持的集合允许插入 null 元素，则返回的集合也允许插入 null 元素。
     *
     * @param <E> 集合中对象的类
     * @param c 要返回动态类型安全视图的集合
     * @param type {@code c} 允许持有的元素类型
     * @return 指定集合的动态类型安全视图
     * @since 1.5
     */
    public static <E> Collection<E> checkedCollection(Collection<E> c,
                                                      Class<E> type) {
        return new CheckedCollection<>(c, type);
    }

    @SuppressWarnings("unchecked")
    static <T> T[] zeroLengthArray(Class<T> type) {
        return (T[]) Array.newInstance(type, 0);
    }

    /**
     * @serial include
     */
    static class CheckedCollection<E> implements Collection<E>, Serializable {
        private static final long serialVersionUID = 1578914078182001775L;

        final Collection<E> c;
        final Class<E> type;

        @SuppressWarnings("unchecked")
        E typeCheck(Object o) {
            if (o != null && !type.isInstance(o))
                throw new ClassCastException(badElementMsg(o));
            return (E) o;
        }

        private String badElementMsg(Object o) {
            return "尝试将 " + o.getClass() +
                " 元素插入元素类型为 " + type + " 的集合中";
        }

        CheckedCollection(Collection<E> c, Class<E> type) {
            this.c = Objects.requireNonNull(c, "c");
            this.type = Objects.requireNonNull(type, "type");
        }

        public int size()                 { return c.size(); }
        public boolean isEmpty()          { return c.isEmpty(); }
        public boolean contains(Object o) { return c.contains(o); }
        public Object[] toArray()         { return c.toArray(); }
        public <T> T[] toArray(T[] a)     { return c.toArray(a); }
        public String toString()          { return c.toString(); }
        public boolean remove(Object o)   { return c.remove(o); }
        public void clear()               {        c.clear(); }

        public boolean containsAll(Collection<?> coll) {
            return c.containsAll(coll);
        }
        public boolean removeAll(Collection<?> coll) {
            return c.removeAll(coll);
        }
        public boolean retainAll(Collection<?> coll) {
            return c.retainAll(coll);
        }

        public Iterator<E> iterator() {
            // JDK-6363904 - 未包装的迭代器可以被类型转换为
            // ListIterator 并使用不安全的 set()
            final Iterator<E> it = c.iterator();
            return new Iterator<E>() {
                public boolean hasNext() { return it.hasNext(); }
                public E next()          { return it.next(); }
                public void remove()     {        it.remove(); }};
        }

        public boolean add(E e)          { return c.add(typeCheck(e)); }

        private E[] zeroLengthElementArray; // 懒初始化

        private E[] zeroLengthElementArray() {
            return zeroLengthElementArray != null ? zeroLengthElementArray :
                (zeroLengthElementArray = zeroLengthArray(type));
        }

        @SuppressWarnings("unchecked")
        Collection<E> checkedCopyOf(Collection<? extends E> coll) {
            Object[] a;
            try {
                E[] z = zeroLengthElementArray();
                a = coll.toArray(z);
                // 防御 coll 违反 toArray 合约
                if (a.getClass() != z.getClass())
                    a = Arrays.copyOf(a, a.length, z.getClass());
            } catch (ArrayStoreException ignore) {
                // 为了获得更好的和一致的诊断，
                // 我们显式地对每个元素调用 typeCheck。
                // 我们调用 clone() 以防止 coll 保留对返回数组的引用
                // 并在类型检查后存储一个错误的元素。
                a = coll.toArray().clone();
                for (Object o : a)
                    typeCheck(o);
            }
            // 对类型系统的轻微滥用，但在这里是安全的。
            return (Collection<E>) Arrays.asList(a);
        }

        public boolean addAll(Collection<? extends E> coll) {
            // 以这种方式做事可以保护我们免受 coll 内容的并发更改
            // 并提供全部或无的语义（如果我们逐个添加元素并进行类型检查，我们不会得到这种语义）
            return c.addAll(checkedCopyOf(coll));
        }

        // 重写 Collection 中的默认方法
        @Override
        public void forEach(Consumer<? super E> action) {c.forEach(action);}
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return c.removeIf(filter);
        }
        @Override
        public Spliterator<E> spliterator() {return c.spliterator();}
        @Override
        public Stream<E> stream()           {return c.stream();}
        @Override
        public Stream<E> parallelStream()   {return c.parallelStream();}
    }


/**
 * 返回指定队列的动态类型安全视图。
 * 任何尝试插入错误类型的元素都将立即导致 {@link ClassCastException}。假设队列在生成动态类型安全视图之前不包含任何错误类型的元素，
 * 并且所有后续对队列的访问都通过视图进行，则可以 <i>保证</i> 队列不会包含错误类型的元素。
 *
 * <p>有关动态类型安全视图的使用讨论，请参阅 {@link #checkedCollection
 * checkedCollection} 方法的文档。
 *
 * <p>如果指定的队列是可序列化的，则返回的队列也将是可序列化的。
 *
 * <p>由于 {@code null} 被认为是任何引用类型的值，因此返回的队列允许在支持插入 {@code null} 元素的队列中插入 {@code null} 元素。
 *
 * @param <E> 队列中对象的类
 * @param queue 要返回动态类型安全视图的队列
 * @param type {@code queue} 允许持有的元素类型
 * @return 指定队列的动态类型安全视图
 * @since 1.8
 */
public static <E> Queue<E> checkedQueue(Queue<E> queue, Class<E> type) {
    return new CheckedQueue<>(queue, type);
}

/**
 * @serial include
 */
static class CheckedQueue<E>
    extends CheckedCollection<E>
    implements Queue<E>, Serializable
{
    private static final long serialVersionUID = 1433151992604707767L;
    final Queue<E> queue;

    CheckedQueue(Queue<E> queue, Class<E> elementType) {
        super(queue, elementType);
        this.queue = queue;
    }

    public E element()              {return queue.element();}
    public boolean equals(Object o) {return o == this || c.equals(o);}
    public int hashCode()           {return c.hashCode();}
    public E peek()                 {return queue.peek();}
    public E poll()                 {return queue.poll();}
    public E remove()               {return queue.remove();}
    public boolean offer(E e)       {return queue.offer(typeCheck(e));}
}

/**
 * 返回指定集合的动态类型安全视图。
 * 任何尝试插入错误类型的元素都将立即导致 {@link ClassCastException}。假设集合在生成动态类型安全视图之前不包含任何错误类型的元素，
 * 并且所有后续对集合的访问都通过视图进行，则可以 <i>保证</i> 集合不会包含错误类型的元素。
 *
 * <p>有关动态类型安全视图的使用讨论，请参阅 {@link #checkedCollection
 * checkedCollection} 方法的文档。
 *
 * <p>如果指定的集合是可序列化的，则返回的集合也将是可序列化的。
 *
 * <p>由于 {@code null} 被认为是任何引用类型的值，因此返回的集合允许在支持插入 {@code null} 元素的集合中插入 {@code null} 元素。
 *
 * @param <E> 集合中对象的类
 * @param s 要返回动态类型安全视图的集合
 * @param type {@code s} 允许持有的元素类型
 * @return 指定集合的动态类型安全视图
 * @since 1.5
 */
public static <E> Set<E> checkedSet(Set<E> s, Class<E> type) {
    return new CheckedSet<>(s, type);
}

/**
 * @serial include
 */
static class CheckedSet<E> extends CheckedCollection<E>
                             implements Set<E>, Serializable
{
    private static final long serialVersionUID = 4694047833775013803L;

    CheckedSet(Set<E> s, Class<E> elementType) { super(s, elementType); }

    public boolean equals(Object o) { return o == this || c.equals(o); }
    public int hashCode()           { return c.hashCode(); }
}

/**
 * 返回指定排序集合的动态类型安全视图。
 * 任何尝试插入错误类型的元素都将立即导致 {@link ClassCastException}。假设排序集合在生成动态类型安全视图之前不包含任何错误类型的元素，
 * 并且所有后续对排序集合的访问都通过视图进行，则可以 <i>保证</i> 排序集合不会包含错误类型的元素。
 *
 * <p>有关动态类型安全视图的使用讨论，请参阅 {@link #checkedCollection
 * checkedCollection} 方法的文档。
 *
 * <p>如果指定的排序集合是可序列化的，则返回的排序集合也将是可序列化的。
 *
 * <p>由于 {@code null} 被认为是任何引用类型的值，因此返回的排序集合允许在支持插入 {@code null} 元素的排序集合中插入 {@code null} 元素。
 *
 * @param <E> 集合中对象的类
 * @param s 要返回动态类型安全视图的排序集合
 * @param type {@code s} 允许持有的元素类型
 * @return 指定排序集合的动态类型安全视图
 * @since 1.5
 */
public static <E> SortedSet<E> checkedSortedSet(SortedSet<E> s,
                                                Class<E> type) {
    return new CheckedSortedSet<>(s, type);
}

/**
 * @serial include
 */
static class CheckedSortedSet<E> extends CheckedSet<E>
    implements SortedSet<E>, Serializable
{
    private static final long serialVersionUID = 1599911165492914959L;

    private final SortedSet<E> ss;

    CheckedSortedSet(SortedSet<E> s, Class<E> type) {
        super(s, type);
        ss = s;
    }

    public Comparator<? super E> comparator() { return ss.comparator(); }
    public E first()                   { return ss.first(); }
    public E last()                    { return ss.last(); }

    public SortedSet<E> subSet(E fromElement, E toElement) {
        return checkedSortedSet(ss.subSet(fromElement,toElement), type);
    }
    public SortedSet<E> headSet(E toElement) {
        return checkedSortedSet(ss.headSet(toElement), type);
    }
    public SortedSet<E> tailSet(E fromElement) {
        return checkedSortedSet(ss.tailSet(fromElement), type);
    }
}

/**
 * 返回指定可导航集合的动态类型安全视图。
 * 任何尝试插入错误类型的元素都将立即导致 {@link ClassCastException}。假设可导航集合在生成动态类型安全视图之前不包含任何错误类型的元素，
 * 并且所有后续对可导航集合的访问都通过视图进行，则可以 <em>保证</em> 可导航集合不会包含错误类型的元素。
 *
 * <p>有关动态类型安全视图的使用讨论，请参阅 {@link #checkedCollection
 * checkedCollection} 方法的文档。
 *
 * <p>如果指定的可导航集合是可序列化的，则返回的可导航集合也将是可序列化的。
 *
 * <p>由于 {@code null} 被认为是任何引用类型的值，因此返回的可导航集合允许在支持插入 {@code null} 元素的可导航集合中插入 {@code null} 元素。
 *
 * @param <E> 集合中对象的类
 * @param s 要返回动态类型安全视图的可导航集合
 * @param type {@code s} 允许持有的元素类型
 * @return 指定可导航集合的动态类型安全视图
 * @since 1.8
 */
public static <E> NavigableSet<E> checkedNavigableSet(NavigableSet<E> s,
                                                    Class<E> type) {
    return new CheckedNavigableSet<>(s, type);
}

/**
 * @serial include
 */
static class CheckedNavigableSet<E> extends CheckedSortedSet<E>
    implements NavigableSet<E>, Serializable
{
    private static final long serialVersionUID = -5429120189805438922L;

    private final NavigableSet<E> ns;

    CheckedNavigableSet(NavigableSet<E> s, Class<E> type) {
        super(s, type);
        ns = s;
    }

    public E lower(E e)                             { return ns.lower(e); }
    public E floor(E e)                             { return ns.floor(e); }
    public E ceiling(E e)                         { return ns.ceiling(e); }
    public E higher(E e)                           { return ns.higher(e); }
    public E pollFirst()                         { return ns.pollFirst(); }
    public E pollLast()                            {return ns.pollLast(); }
    public NavigableSet<E> descendingSet()
                      { return checkedNavigableSet(ns.descendingSet(), type); }
    public Iterator<E> descendingIterator()
        {return checkedNavigableSet(ns.descendingSet(), type).iterator(); }

    public NavigableSet<E> subSet(E fromElement, E toElement) {
        return checkedNavigableSet(ns.subSet(fromElement, true, toElement, false), type);
    }
    public NavigableSet<E> headSet(E toElement) {
        return checkedNavigableSet(ns.headSet(toElement, false), type);
    }
    public NavigableSet<E> tailSet(E fromElement) {
        return checkedNavigableSet(ns.tailSet(fromElement, true), type);
    }

    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return checkedNavigableSet(ns.subSet(fromElement, fromInclusive, toElement, toInclusive), type);
    }

    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return checkedNavigableSet(ns.headSet(toElement, inclusive), type);
    }

    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return checkedNavigableSet(ns.tailSet(fromElement, inclusive), type);
    }
}

/**
 * 返回指定列表的动态类型安全视图。
 * 任何尝试插入错误类型的元素都将立即导致 {@link ClassCastException}。假设列表在生成动态类型安全视图之前不包含任何错误类型的元素，
 * 并且所有后续对列表的访问都通过视图进行，则可以 <i>保证</i> 列表不会包含错误类型的元素。
 *
 * <p>有关动态类型安全视图的使用讨论，请参阅 {@link #checkedCollection
 * checkedCollection} 方法的文档。
 *
 * <p>如果指定的列表是可序列化的，则返回的列表也将是可序列化的。
 *
 * <p>由于 {@code null} 被认为是任何引用类型的值，因此返回的列表允许在支持插入 {@code null} 元素的列表中插入 {@code null} 元素。
 *
 * @param <E> 列表中对象的类
 * @param list 要返回动态类型安全视图的列表
 * @param type {@code list} 允许持有的元素类型
 * @return 指定列表的动态类型安全视图
 * @since 1.5
 */
public static <E> List<E> checkedList(List<E> list, Class<E> type) {
    return (list instanceof RandomAccess ?
            new CheckedRandomAccessList<>(list, type) :
            new CheckedList<>(list, type));
}

/**
 * @serial include
 */
static class CheckedList<E>
    extends CheckedCollection<E>
    implements List<E>
{
    private static final long serialVersionUID = 65247728283967356L;
    final List<E> list;

    CheckedList(List<E> list, Class<E> type) {
        super(list, type);
        this.list = list;
    }

    public boolean equals(Object o)  { return o == this || list.equals(o); }
    public int hashCode()            { return list.hashCode(); }
    public E get(int index)          { return list.get(index); }
    public E remove(int index)       { return list.remove(index); }
    public int indexOf(Object o)     { return list.indexOf(o); }
    public int lastIndexOf(Object o) { return list.lastIndexOf(o); }

    public E set(int index, E element) {
        return list.set(index, typeCheck(element));
    }

    public void add(int index, E element) {
        list.add(index, typeCheck(element));
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        return list.addAll(index, checkedCopyOf(c));
    }
    public ListIterator<E> listIterator()   { return listIterator(0); }

    public ListIterator<E> listIterator(final int index) {
        final ListIterator<E> i = list.listIterator(index);

        return new ListIterator<E>() {
            public boolean hasNext()     { return i.hasNext(); }
            public E next()              { return i.next(); }
            public boolean hasPrevious() { return i.hasPrevious(); }
            public E previous()          { return i.previous(); }
            public int nextIndex()       { return i.nextIndex(); }
            public int previousIndex()   { return i.previousIndex(); }
            public void remove()         {        i.remove(); }

            public void set(E e) {
                i.set(typeCheck(e));
            }

            public void add(E e) {
                i.add(typeCheck(e));
            }

            @Override
            public void forEachRemaining(Consumer<? super E> action) {
                i.forEachRemaining(action);
            }
        };
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return new CheckedList<>(list.subList(fromIndex, toIndex), type);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ClassCastException 如果操作返回的元素的类阻止其被添加到此集合中。异常可能在列表中的某些元素已经被替换后抛出。
     */
    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        list.replaceAll(e -> typeCheck(operator.apply(e)));
    }

    @Override
    public void sort(Comparator<? super E> c) {
        list.sort(c);
    }
}

/**
 * @serial include
 */
static class CheckedRandomAccessList<E> extends CheckedList<E>
                                        implements RandomAccess
{
    private static final long serialVersionUID = 1638200125423088369L;


                    CheckedRandomAccessList(List<E> list, Class<E> type) {
            super(list, type);
        }

        public List<E> subList(int fromIndex, int toIndex) {
            return new CheckedRandomAccessList<>(
                    list.subList(fromIndex, toIndex), type);
        }
    }

    /**
     * 返回指定映射的动态类型安全视图。
     * 任何尝试插入键或值类型不正确的映射将立即导致 {@link ClassCastException}。
     * 同样，任何尝试修改当前与键关联的值也将立即导致 {@link ClassCastException}，
     * 无论修改是直接通过映射本身进行，还是通过从映射的 {@link Map#entrySet() entry set} 视图中获取的 {@link Map.Entry} 实例进行。
     *
     * <p>假设在生成动态类型安全视图之前映射中没有不正确类型的键或值，
     * 并且所有后续访问映射都是通过视图（或其集合视图）进行的，
     * 则 <i>保证</i> 映射中不能包含不正确类型的键或值。
     *
     * <p>关于动态类型安全视图的使用讨论可以在 {@link #checkedCollection
     * checkedCollection} 方法的文档中找到。
     *
     * <p>如果指定的映射是可序列化的，则返回的映射也将是可序列化的。
     *
     * <p>由于 {@code null} 被认为是任何引用类型的值，因此如果支持映射允许插入 null 键或值，
     * 返回的映射也允许插入 null 键或值。
     *
     * @param <K> 映射键的类
     * @param <V> 映射值的类
     * @param m 要返回动态类型安全视图的映射
     * @param keyType {@code m} 允许持有的键类型
     * @param valueType {@code m} 允许持有的值类型
     * @return 指定映射的动态类型安全视图
     * @since 1.5
     */
    public static <K, V> Map<K, V> checkedMap(Map<K, V> m,
                                              Class<K> keyType,
                                              Class<V> valueType) {
        return new CheckedMap<>(m, keyType, valueType);
    }


    /**
     * @serial include
     */
    private static class CheckedMap<K,V>
        implements Map<K,V>, Serializable
    {
        private static final long serialVersionUID = 5742860141034234728L;

        private final Map<K, V> m;
        final Class<K> keyType;
        final Class<V> valueType;

        private void typeCheck(Object key, Object value) {
            if (key != null && !keyType.isInstance(key))
                throw new ClassCastException(badKeyMsg(key));

            if (value != null && !valueType.isInstance(value))
                throw new ClassCastException(badValueMsg(value));
        }

        private BiFunction<? super K, ? super V, ? extends V> typeCheck(
                BiFunction<? super K, ? super V, ? extends V> func) {
            Objects.requireNonNull(func);
            return (k, v) -> {
                V newValue = func.apply(k, v);
                typeCheck(k, newValue);
                return newValue;
            };
        }

        private String badKeyMsg(Object key) {
            return "尝试将 " + key.getClass() +
                    " 键插入键类型为 " + keyType 的映射中";
        }

        private String badValueMsg(Object value) {
            return "尝试将 " + value.getClass() +
                    " 值插入值类型为 " + valueType 的映射中";
        }

        CheckedMap(Map<K, V> m, Class<K> keyType, Class<V> valueType) {
            this.m = Objects.requireNonNull(m);
            this.keyType = Objects.requireNonNull(keyType);
            this.valueType = Objects.requireNonNull(valueType);
        }

        public int size()                      { return m.size(); }
        public boolean isEmpty()               { return m.isEmpty(); }
        public boolean containsKey(Object key) { return m.containsKey(key); }
        public boolean containsValue(Object v) { return m.containsValue(v); }
        public V get(Object key)               { return m.get(key); }
        public V remove(Object key)            { return m.remove(key); }
        public void clear()                    { m.clear(); }
        public Set<K> keySet()                 { return m.keySet(); }
        public Collection<V> values()          { return m.values(); }
        public boolean equals(Object o)        { return o == this || m.equals(o); }
        public int hashCode()                  { return m.hashCode(); }
        public String toString()               { return m.toString(); }

        public V put(K key, V value) {
            typeCheck(key, value);
            return m.put(key, value);
        }

        @SuppressWarnings("unchecked")
        public void putAll(Map<? extends K, ? extends V> t) {
            // 满足以下目标：
            // - 类型不匹配时提供良好的诊断信息
            // - 全有或全无的语义
            // - 防止恶意的 t
            // - 如果 t 是并发映射，则正确处理
            Object[] entries = t.entrySet().toArray();
            List<Map.Entry<K,V>> checked = new ArrayList<>(entries.length);
            for (Object o : entries) {
                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
                Object k = e.getKey();
                Object v = e.getValue();
                typeCheck(k, v);
                checked.add(
                        new AbstractMap.SimpleImmutableEntry<>((K)k, (V)v));
            }
            for (Map.Entry<K,V> e : checked)
                m.put(e.getKey(), e.getValue());
        }

        private transient Set<Map.Entry<K,V>> entrySet;

        public Set<Map.Entry<K,V>> entrySet() {
            if (entrySet==null)
                entrySet = new CheckedEntrySet<>(m.entrySet(), valueType);
            return entrySet;
        }

        // 覆盖 Map 中的默认方法
        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            m.forEach(action);
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            m.replaceAll(typeCheck(function));
        }

        @Override
        public V putIfAbsent(K key, V value) {
            typeCheck(key, value);
            return m.putIfAbsent(key, value);
        }

        @Override
        public boolean remove(Object key, Object value) {
            return m.remove(key, value);
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            typeCheck(key, newValue);
            return m.replace(key, oldValue, newValue);
        }

        @Override
        public V replace(K key, V value) {
            typeCheck(key, value);
            return m.replace(key, value);
        }

        @Override
        public V computeIfAbsent(K key,
                Function<? super K, ? extends V> mappingFunction) {
            Objects.requireNonNull(mappingFunction);
            return m.computeIfAbsent(key, k -> {
                V value = mappingFunction.apply(k);
                typeCheck(k, value);
                return value;
            });
        }

        @Override
        public V computeIfPresent(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            return m.computeIfPresent(key, typeCheck(remappingFunction));
        }

        @Override
        public V compute(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            return m.compute(key, typeCheck(remappingFunction));
        }

        @Override
        public V merge(K key, V value,
                BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            Objects.requireNonNull(remappingFunction);
            return m.merge(key, value, (v1, v2) -> {
                V newValue = remappingFunction.apply(v1, v2);
                typeCheck(null, newValue);
                return newValue;
            });
        }

        /**
         * 我们需要这个类，除了 CheckedSet 之外，因为 Map.Entry 允许通过 setValue 操作修改支持映射。这个类很微妙：必须防止许多可能的攻击。
         *
         * @serial exclude
         */
        static class CheckedEntrySet<K,V> implements Set<Map.Entry<K,V>> {
            private final Set<Map.Entry<K,V>> s;
            private final Class<V> valueType;

            CheckedEntrySet(Set<Map.Entry<K, V>> s, Class<V> valueType) {
                this.s = s;
                this.valueType = valueType;
            }

            public int size()        { return s.size(); }
            public boolean isEmpty() { return s.isEmpty(); }
            public String toString() { return s.toString(); }
            public int hashCode()    { return s.hashCode(); }
            public void clear()      {        s.clear(); }

            public boolean add(Map.Entry<K, V> e) {
                throw new UnsupportedOperationException();
            }
            public boolean addAll(Collection<? extends Map.Entry<K, V>> coll) {
                throw new UnsupportedOperationException();
            }

            public Iterator<Map.Entry<K,V>> iterator() {
                final Iterator<Map.Entry<K, V>> i = s.iterator();
                final Class<V> valueType = this.valueType;

                return new Iterator<Map.Entry<K,V>>() {
                    public boolean hasNext() { return i.hasNext(); }
                    public void remove()     { i.remove(); }

                    public Map.Entry<K,V> next() {
                        return checkedEntry(i.next(), valueType);
                    }
                };
            }

            @SuppressWarnings("unchecked")
            public Object[] toArray() {
                Object[] source = s.toArray();

                /*
                 * 确保我们不会因 s.toArray 返回其他类型的 Object 数组而收到 ArrayStoreException
                 */
                Object[] dest = (CheckedEntry.class.isInstance(
                    source.getClass().getComponentType()) ? source :
                                 new Object[source.length]);

                for (int i = 0; i < source.length; i++)
                    dest[i] = checkedEntry((Map.Entry<K,V>)source[i],
                                           valueType);
                return dest;
            }

            @SuppressWarnings("unchecked")
            public <T> T[] toArray(T[] a) {
                // 我们不将 a 传递给 s.toArray，以避免不诚实的多线程客户端获取 s 中的原始（未包装）Entries
                T[] arr = s.toArray(a.length==0 ? a : Arrays.copyOf(a, 0));

                for (int i=0; i<arr.length; i++)
                    arr[i] = (T) checkedEntry((Map.Entry<K,V>)arr[i],
                                              valueType);
                if (arr.length > a.length)
                    return arr;

                System.arraycopy(arr, 0, a, 0, arr.length);
                if (a.length > arr.length)
                    a[arr.length] = null;
                return a;
            }

            /**
             * 重写此方法以保护支持集合免受具有恶意 equals 函数的对象的影响，该函数在检测到等价候选对象是 Map.Entry 时调用其 setValue 方法。
             */
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry))
                    return false;
                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
                return s.contains(
                    (e instanceof CheckedEntry) ? e : checkedEntry(e, valueType));
            }

            /**
             * 批量集合方法被重写以防止不诚实的集合，其 contains(Object o) 方法在检测到 o 是 Map.Entry 时调用 o.setValue。
             */
            public boolean containsAll(Collection<?> c) {
                for (Object o : c)
                    if (!contains(o)) // 调用上面的安全 contains()
                        return false;
                return true;
            }

            public boolean remove(Object o) {
                if (!(o instanceof Map.Entry))
                    return false;
                return s.remove(new AbstractMap.SimpleImmutableEntry
                                <>((Map.Entry<?,?>)o));
            }

            public boolean removeAll(Collection<?> c) {
                return batchRemove(c, false);
            }
            public boolean retainAll(Collection<?> c) {
                return batchRemove(c, true);
            }
            private boolean batchRemove(Collection<?> c, boolean complement) {
                Objects.requireNonNull(c);
                boolean modified = false;
                Iterator<Map.Entry<K,V>> it = iterator();
                while (it.hasNext()) {
                    if (c.contains(it.next()) != complement) {
                        it.remove();
                        modified = true;
                    }
                }
                return modified;
            }

            public boolean equals(Object o) {
                if (o == this)
                    return true;
                if (!(o instanceof Set))
                    return false;
                Set<?> that = (Set<?>) o;
                return that.size() == s.size()
                    && containsAll(that); // 调用上面的安全 containsAll()
            }

            static <K,V,T> CheckedEntry<K,V,T> checkedEntry(Map.Entry<K,V> e,
                                                            Class<T> valueType) {
                return new CheckedEntry<>(e, valueType);
            }

            /**
             * 这个“包装类”有两个目的：它通过短路 setValue 方法防止客户端修改支持映射，
             * 并保护支持映射免受在执行相等性检查时尝试修改另一个 Map.Entry 的恶意 Map.Entry。
             */
            private static class CheckedEntry<K,V,T> implements Map.Entry<K,V> {
                private final Map.Entry<K, V> e;
                private final Class<T> valueType;

                CheckedEntry(Map.Entry<K, V> e, Class<T> valueType) {
                    this.e = Objects.requireNonNull(e);
                    this.valueType = Objects.requireNonNull(valueType);
                }

                public K getKey()        { return e.getKey(); }
                public V getValue()      { return e.getValue(); }
                public int hashCode()    { return e.hashCode(); }
                public String toString() { return e.toString(); }


                            public V setValue(V value) {
                    if (value != null && !valueType.isInstance(value))
                        throw new ClassCastException("尝试插入 " + value.getClass() +
                        " 类型的值到值类型为 " + valueType + " 的映射中");
                    return e.setValue(value);
                }

                private String badValueMsg(Object value) {
                    return "尝试插入 " + value.getClass() +
                        " 类型的值到值类型为 " + valueType + " 的映射中";
                }

                public boolean equals(Object o) {
                    if (o == this)
                        return true;
                    if (!(o instanceof Map.Entry))
                        return false;
                    return e.equals(new AbstractMap.SimpleImmutableEntry
                                    <>((Map.Entry<?,?>)o));
                }
            }
        }
    }

    /**
     * 返回指定排序映射的动态类型安全视图。
     * 任何尝试插入键或值类型错误的映射都会立即导致 {@link ClassCastException}。
     * 同样，任何尝试修改与键关联的当前值都会立即导致 {@link ClassCastException}，
     * 无论修改是通过映射本身直接尝试，还是通过从映射的 {@link Map#entrySet() entry set} 视图中获得的 {@link Map.Entry} 实例尝试。
     *
     * <p>假设映射在生成动态类型安全视图之前不包含任何类型错误的键或值，
     * 并且所有后续对映射的访问都通过视图（或其集合视图之一）进行，
     * 则 <i>保证</i> 映射不会包含任何类型错误的键或值。
     *
     * <p>关于动态类型安全视图的使用讨论可以在 {@link #checkedCollection checkedCollection} 方法的文档中找到。
     *
     * <p>如果指定的映射是可序列化的，则返回的映射也将是可序列化的。
     *
     * <p>由于 {@code null} 被认为是任何引用类型的值，因此返回的映射允许插入 null 键或值，
     * 只要底层映射允许。
     *
     * @param <K> 映射键的类
     * @param <V> 映射值的类
     * @param m 要返回动态类型安全视图的映射
     * @param keyType {@code m} 允许持有的键类型
     * @param valueType {@code m} 允许持有的值类型
     * @return 指定映射的动态类型安全视图
     * @since 1.5
     */
    public static <K,V> SortedMap<K,V> checkedSortedMap(SortedMap<K, V> m,
                                                        Class<K> keyType,
                                                        Class<V> valueType) {
        return new CheckedSortedMap<>(m, keyType, valueType);
    }

    /**
     * @serial include
     */
    static class CheckedSortedMap<K,V> extends CheckedMap<K,V>
        implements SortedMap<K,V>, Serializable
    {
        private static final long serialVersionUID = 1599671320688067438L;

        private final SortedMap<K, V> sm;

        CheckedSortedMap(SortedMap<K, V> m,
                         Class<K> keyType, Class<V> valueType) {
            super(m, keyType, valueType);
            sm = m;
        }

        public Comparator<? super K> comparator() { return sm.comparator(); }
        public K firstKey()                       { return sm.firstKey(); }
        public K lastKey()                        { return sm.lastKey(); }

        public SortedMap<K,V> subMap(K fromKey, K toKey) {
            return checkedSortedMap(sm.subMap(fromKey, toKey),
                                    keyType, valueType);
        }
        public SortedMap<K,V> headMap(K toKey) {
            return checkedSortedMap(sm.headMap(toKey), keyType, valueType);
        }
        public SortedMap<K,V> tailMap(K fromKey) {
            return checkedSortedMap(sm.tailMap(fromKey), keyType, valueType);
        }
    }

    /**
     * 返回指定可导航映射的动态类型安全视图。
     * 任何尝试插入键或值类型错误的映射都会立即导致 {@link ClassCastException}。
     * 同样，任何尝试修改与键关联的当前值都会立即导致 {@link ClassCastException}，
     * 无论修改是通过映射本身直接尝试，还是通过从映射的 {@link Map#entrySet() entry set} 视图中获得的 {@link Map.Entry} 实例尝试。
     *
     * <p>假设映射在生成动态类型安全视图之前不包含任何类型错误的键或值，
     * 并且所有后续对映射的访问都通过视图（或其集合视图之一）进行，
     * 则 <em>保证</em> 映射不会包含任何类型错误的键或值。
     *
     * <p>关于动态类型安全视图的使用讨论可以在 {@link #checkedCollection checkedCollection} 方法的文档中找到。
     *
     * <p>如果指定的映射是可序列化的，则返回的映射也将是可序列化的。
     *
     * <p>由于 {@code null} 被认为是任何引用类型的值，因此返回的映射允许插入 null 键或值，
     * 只要底层映射允许。
     *
     * @param <K> 映射键的类型
     * @param <V> 映射值的类型
     * @param m 要返回动态类型安全视图的映射
     * @param keyType {@code m} 允许持有的键类型
     * @param valueType {@code m} 允许持有的值类型
     * @return 指定映射的动态类型安全视图
     * @since 1.8
     */
    public static <K,V> NavigableMap<K,V> checkedNavigableMap(NavigableMap<K, V> m,
                                                        Class<K> keyType,
                                                        Class<V> valueType) {
        return new CheckedNavigableMap<>(m, keyType, valueType);
    }

    /**
     * @serial include
     */
    static class CheckedNavigableMap<K,V> extends CheckedSortedMap<K,V>
        implements NavigableMap<K,V>, Serializable
    {
        private static final long serialVersionUID = -4852462692372534096L;

        private final NavigableMap<K, V> nm;

        CheckedNavigableMap(NavigableMap<K, V> m,
                         Class<K> keyType, Class<V> valueType) {
            super(m, keyType, valueType);
            nm = m;
        }

        public Comparator<? super K> comparator()   { return nm.comparator(); }
        public K firstKey()                           { return nm.firstKey(); }
        public K lastKey()                             { return nm.lastKey(); }

        public Entry<K, V> lowerEntry(K key) {
            Entry<K,V> lower = nm.lowerEntry(key);
            return (null != lower)
                ? new CheckedMap.CheckedEntrySet.CheckedEntry<>(lower, valueType)
                : null;
        }

        public K lowerKey(K key)                   { return nm.lowerKey(key); }

        public Entry<K, V> floorEntry(K key) {
            Entry<K,V> floor = nm.floorEntry(key);
            return (null != floor)
                ? new CheckedMap.CheckedEntrySet.CheckedEntry<>(floor, valueType)
                : null;
        }

        public K floorKey(K key)                   { return nm.floorKey(key); }

        public Entry<K, V> ceilingEntry(K key) {
            Entry<K,V> ceiling = nm.ceilingEntry(key);
            return (null != ceiling)
                ? new CheckedMap.CheckedEntrySet.CheckedEntry<>(ceiling, valueType)
                : null;
        }

        public K ceilingKey(K key)               { return nm.ceilingKey(key); }

        public Entry<K, V> higherEntry(K key) {
            Entry<K,V> higher = nm.higherEntry(key);
            return (null != higher)
                ? new CheckedMap.CheckedEntrySet.CheckedEntry<>(higher, valueType)
                : null;
        }

        public K higherKey(K key)                 { return nm.higherKey(key); }

        public Entry<K, V> firstEntry() {
            Entry<K,V> first = nm.firstEntry();
            return (null != first)
                ? new CheckedMap.CheckedEntrySet.CheckedEntry<>(first, valueType)
                : null;
        }

        public Entry<K, V> lastEntry() {
            Entry<K,V> last = nm.lastEntry();
            return (null != last)
                ? new CheckedMap.CheckedEntrySet.CheckedEntry<>(last, valueType)
                : null;
        }

        public Entry<K, V> pollFirstEntry() {
            Entry<K,V> entry = nm.pollFirstEntry();
            return (null == entry)
                ? null
                : new CheckedMap.CheckedEntrySet.CheckedEntry<>(entry, valueType);
        }

        public Entry<K, V> pollLastEntry() {
            Entry<K,V> entry = nm.pollLastEntry();
            return (null == entry)
                ? null
                : new CheckedMap.CheckedEntrySet.CheckedEntry<>(entry, valueType);
        }

        public NavigableMap<K, V> descendingMap() {
            return checkedNavigableMap(nm.descendingMap(), keyType, valueType);
        }

        public NavigableSet<K> keySet() {
            return navigableKeySet();
        }

        public NavigableSet<K> navigableKeySet() {
            return checkedNavigableSet(nm.navigableKeySet(), keyType);
        }

        public NavigableSet<K> descendingKeySet() {
            return checkedNavigableSet(nm.descendingKeySet(), keyType);
        }

        @Override
        public NavigableMap<K,V> subMap(K fromKey, K toKey) {
            return checkedNavigableMap(nm.subMap(fromKey, true, toKey, false),
                                    keyType, valueType);
        }

        @Override
        public NavigableMap<K,V> headMap(K toKey) {
            return checkedNavigableMap(nm.headMap(toKey, false), keyType, valueType);
        }

        @Override
        public NavigableMap<K,V> tailMap(K fromKey) {
            return checkedNavigableMap(nm.tailMap(fromKey, true), keyType, valueType);
        }

        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            return checkedNavigableMap(nm.subMap(fromKey, fromInclusive, toKey, toInclusive), keyType, valueType);
        }

        public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
            return checkedNavigableMap(nm.headMap(toKey, inclusive), keyType, valueType);
        }

        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
            return checkedNavigableMap(nm.tailMap(fromKey, inclusive), keyType, valueType);
        }
    }

    // 空集合

    /**
     * 返回一个没有元素的迭代器。更具体地说，
     *
     * <ul>
     * <li>{@link Iterator#hasNext hasNext} 始终返回 {@code
     * false}。</li>
     * <li>{@link Iterator#next next} 始终抛出 {@link
     * NoSuchElementException}。</li>
     * <li>{@link Iterator#remove remove} 始终抛出 {@link
     * IllegalStateException}。</li>
     * </ul>
     *
     * <p>此方法的实现允许但不要求从多次调用中返回同一个对象。
     *
     * @param <T> 如果有元素，迭代器中的元素类型
     * @return 一个空迭代器
     * @since 1.7
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> emptyIterator() {
        return (Iterator<T>) EmptyIterator.EMPTY_ITERATOR;
    }

    private static class EmptyIterator<E> implements Iterator<E> {
        static final EmptyIterator<Object> EMPTY_ITERATOR
            = new EmptyIterator<>();

        public boolean hasNext() { return false; }
        public E next() { throw new NoSuchElementException(); }
        public void remove() { throw new IllegalStateException(); }
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
        }
    }

    /**
     * 返回一个没有元素的列表迭代器。更具体地说，
     *
     * <ul>
     * <li>{@link Iterator#hasNext hasNext} 和 {@link
     * ListIterator#hasPrevious hasPrevious} 始终返回 {@code
     * false}。</li>
     * <li>{@link Iterator#next next} 和 {@link ListIterator#previous
     * previous} 始终抛出 {@link NoSuchElementException}。</li>
     * <li>{@link Iterator#remove remove} 和 {@link ListIterator#set
     * set} 始终抛出 {@link IllegalStateException}。</li>
     * <li>{@link ListIterator#add add} 始终抛出 {@link
     * UnsupportedOperationException}。</li>
     * <li>{@link ListIterator#nextIndex nextIndex} 始终返回
     * {@code 0}。</li>
     * <li>{@link ListIterator#previousIndex previousIndex} 始终返回
     * {@code -1}。</li>
     * </ul>
     *
     * <p>此方法的实现允许但不要求从多次调用中返回同一个对象。
     *
     * @param <T> 如果有元素，迭代器中的元素类型
     * @return 一个空列表迭代器
     * @since 1.7
     */
    @SuppressWarnings("unchecked")
    public static <T> ListIterator<T> emptyListIterator() {
        return (ListIterator<T>) EmptyListIterator.EMPTY_ITERATOR;
    }

    private static class EmptyListIterator<E>
        extends EmptyIterator<E>
        implements ListIterator<E>
    {
        static final EmptyListIterator<Object> EMPTY_ITERATOR
            = new EmptyListIterator<>();

        public boolean hasPrevious() { return false; }
        public E previous() { throw new NoSuchElementException(); }
        public int nextIndex()     { return 0; }
        public int previousIndex() { return -1; }
        public void set(E e) { throw new IllegalStateException(); }
        public void add(E e) { throw new UnsupportedOperationException(); }
    }

    /**
     * 返回一个没有元素的枚举。更具体地说，
     *
     * <ul>
     * <li>{@link Enumeration#hasMoreElements hasMoreElements} 始终返回 {@code false}。</li>
     * <li> {@link Enumeration#nextElement nextElement} 始终抛出 {@link NoSuchElementException}。</li>
     * </ul>
     *
     * <p>此方法的实现允许但不要求从多次调用中返回同一个对象。
     *
     * @param  <T> 枚举中的对象类
     * @return 一个空枚举
     * @since 1.7
     */
    @SuppressWarnings("unchecked")
    public static <T> Enumeration<T> emptyEnumeration() {
        return (Enumeration<T>) EmptyEnumeration.EMPTY_ENUMERATION;
    }

    private static class EmptyEnumeration<E> implements Enumeration<E> {
        static final EmptyEnumeration<Object> EMPTY_ENUMERATION
            = new EmptyEnumeration<>();

        public boolean hasMoreElements() { return false; }
        public E nextElement() { throw new NoSuchElementException(); }
    }

    /**
     * 空集合（不可变）。此集合是可序列化的。
     *
     * @see #emptySet()
     */
    @SuppressWarnings("rawtypes")
    public static final Set EMPTY_SET = new EmptySet<>();


                /**
     * 返回一个空集（不可变）。此集是可序列化的。
     * 与同名字段不同，此方法是参数化的。
     *
     * <p>此示例说明了获取空集的类型安全方法：
     * <pre>
     *     Set&lt;String&gt; s = Collections.emptySet();
     * </pre>
     * @implNote 此方法的实现不必为每次调用创建一个单独的
     * {@code Set} 对象。使用此方法的成本可能与使用同名字段的成本相当。 （与该字段不同，该方法不提供类型安全性。）
     *
     * @param  <T> 集合中对象的类
     * @return 空集
     *
     * @see #EMPTY_SET
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public static final <T> Set<T> emptySet() {
        return (Set<T>) EMPTY_SET;
    }

    /**
     * @serial include
     */
    private static class EmptySet<E>
        extends AbstractSet<E>
        implements Serializable
    {
        private static final long serialVersionUID = 1582296315990362920L;

        public Iterator<E> iterator() { return emptyIterator(); }

        public int size() {return 0;}
        public boolean isEmpty() {return true;}

        public boolean contains(Object obj) {return false;}
        public boolean containsAll(Collection<?> c) { return c.isEmpty(); }

        public Object[] toArray() { return new Object[0]; }

        public <T> T[] toArray(T[] a) {
            if (a.length > 0)
                a[0] = null;
            return a;
        }

        // 覆盖 Collection 中的默认方法
        @Override
        public void forEach(Consumer<? super E> action) {
            Objects.requireNonNull(action);
        }
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            Objects.requireNonNull(filter);
            return false;
        }
        @Override
        public Spliterator<E> spliterator() { return Spliterators.emptySpliterator(); }

        // 保留单例属性
        private Object readResolve() {
            return EMPTY_SET;
        }
    }

    /**
     * 返回一个空的有序集（不可变）。此集是可序列化的。
     *
     * <p>此示例说明了获取空的有序集的类型安全方法：
     * <pre> {@code
     *     SortedSet<String> s = Collections.emptySortedSet();
     * }</pre>
     *
     * @implNote 此方法的实现不必为每次调用创建一个单独的
     * {@code SortedSet} 对象。
     *
     * @param <E> 集合中元素的类，如果有任何元素的话
     * @return 空的有序集
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <E> SortedSet<E> emptySortedSet() {
        return (SortedSet<E>) UnmodifiableNavigableSet.EMPTY_NAVIGABLE_SET;
    }

    /**
     * 返回一个空的可导航集（不可变）。此集是可序列化的。
     *
     * <p>此示例说明了获取空的可导航集的类型安全方法：
     * <pre> {@code
     *     NavigableSet<String> s = Collections.emptyNavigableSet();
     * }</pre>
     *
     * @implNote 此方法的实现不必为每次调用创建一个单独的
     * {@code NavigableSet} 对象。
     *
     * @param <E> 集合中元素的类，如果有任何元素的话
     * @return 空的可导航集
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <E> NavigableSet<E> emptyNavigableSet() {
        return (NavigableSet<E>) UnmodifiableNavigableSet.EMPTY_NAVIGABLE_SET;
    }

    /**
     * 空列表（不可变）。此列表是可序列化的。
     *
     * @see #emptyList()
     */
    @SuppressWarnings("rawtypes")
    public static final List EMPTY_LIST = new EmptyList<>();

    /**
     * 返回一个空列表（不可变）。此列表是可序列化的。
     *
     * <p>此示例说明了获取空列表的类型安全方法：
     * <pre>
     *     List&lt;String&gt; s = Collections.emptyList();
     * </pre>
     *
     * @implNote
     * 此方法的实现不必为每次调用创建一个单独的 <tt>List</tt>
     * 对象。使用此方法的成本可能与使用同名字段的成本相当。 （与该字段不同，该方法不提供类型安全性。）
     *
     * @param <T> 列表中元素的类，如果有任何元素的话
     * @return 一个空的不可变列表
     *
     * @see #EMPTY_LIST
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public static final <T> List<T> emptyList() {
        return (List<T>) EMPTY_LIST;
    }

    /**
     * @serial include
     */
    private static class EmptyList<E>
        extends AbstractList<E>
        implements RandomAccess, Serializable {
        private static final long serialVersionUID = 8842843931221139166L;

        public Iterator<E> iterator() {
            return emptyIterator();
        }
        public ListIterator<E> listIterator() {
            return emptyListIterator();
        }

        public int size() {return 0;}
        public boolean isEmpty() {return true;}

        public boolean contains(Object obj) {return false;}
        public boolean containsAll(Collection<?> c) { return c.isEmpty(); }

        public Object[] toArray() { return new Object[0]; }

        public <T> T[] toArray(T[] a) {
            if (a.length > 0)
                a[0] = null;
            return a;
        }

        public E get(int index) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }

        public boolean equals(Object o) {
            return (o instanceof List) && ((List<?>)o).isEmpty();
        }

        public int hashCode() { return 1; }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            Objects.requireNonNull(filter);
            return false;
        }
        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            Objects.requireNonNull(operator);
        }
        @Override
        public void sort(Comparator<? super E> c) {
        }

        // 覆盖 Collection 中的默认方法
        @Override
        public void forEach(Consumer<? super E> action) {
            Objects.requireNonNull(action);
        }

        @Override
        public Spliterator<E> spliterator() { return Spliterators.emptySpliterator(); }

        // 保留单例属性
        private Object readResolve() {
            return EMPTY_LIST;
        }
    }

    /**
     * 空映射（不可变）。此映射是可序列化的。
     *
     * @see #emptyMap()
     * @since 1.3
     */
    @SuppressWarnings("rawtypes")
    public static final Map EMPTY_MAP = new EmptyMap<>();

    /**
     * 返回一个空映射（不可变）。此映射是可序列化的。
     *
     * <p>此示例说明了获取空映射的类型安全方法：
     * <pre>
     *     Map&lt;String, Date&gt; s = Collections.emptyMap();
     * </pre>
     * @implNote 此方法的实现不必为每次调用创建一个单独的
     * {@code Map} 对象。使用此方法的成本可能与使用同名字段的成本相当。 （与该字段不同，该方法不提供类型安全性。）
     *
     * @param <K> 映射键的类
     * @param <V> 映射值的类
     * @return 一个空映射
     * @see #EMPTY_MAP
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public static final <K,V> Map<K,V> emptyMap() {
        return (Map<K,V>) EMPTY_MAP;
    }

    /**
     * 返回一个空的有序映射（不可变）。此映射是可序列化的。
     *
     * <p>此示例说明了获取空映射的类型安全方法：
     * <pre> {@code
     *     SortedMap<String, Date> s = Collections.emptySortedMap();
     * }</pre>
     *
     * @implNote 此方法的实现不必为每次调用创建一个单独的
     * {@code SortedMap} 对象。
     *
     * @param <K> 映射键的类
     * @param <V> 映射值的类
     * @return 一个空的有序映射
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static final <K,V> SortedMap<K,V> emptySortedMap() {
        return (SortedMap<K,V>) UnmodifiableNavigableMap.EMPTY_NAVIGABLE_MAP;
    }

    /**
     * 返回一个空的可导航映射（不可变）。此映射是可序列化的。
     *
     * <p>此示例说明了获取空映射的类型安全方法：
     * <pre> {@code
     *     NavigableMap<String, Date> s = Collections.emptyNavigableMap();
     * }</pre>
     *
     * @implNote 此方法的实现不必为每次调用创建一个单独的
     * {@code NavigableMap} 对象。
     *
     * @param <K> 映射键的类
     * @param <V> 映射值的类
     * @return 一个空的可导航映射
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static final <K,V> NavigableMap<K,V> emptyNavigableMap() {
        return (NavigableMap<K,V>) UnmodifiableNavigableMap.EMPTY_NAVIGABLE_MAP;
    }

    /**
     * @serial include
     */
    private static class EmptyMap<K,V>
        extends AbstractMap<K,V>
        implements Serializable
    {
        private static final long serialVersionUID = 6428348081105594320L;

        public int size()                          {return 0;}
        public boolean isEmpty()                   {return true;}
        public boolean containsKey(Object key)     {return false;}
        public boolean containsValue(Object value) {return false;}
        public V get(Object key)                   {return null;}
        public Set<K> keySet()                     {return emptySet();}
        public Collection<V> values()              {return emptySet();}
        public Set<Map.Entry<K,V>> entrySet()      {return emptySet();}

        public boolean equals(Object o) {
            return (o instanceof Map) && ((Map<?,?>)o).isEmpty();
        }

        public int hashCode()                      {return 0;}

        // 覆盖 Map 中的默认方法
        @Override
        @SuppressWarnings("unchecked")
        public V getOrDefault(Object k, V defaultValue) {
            return defaultValue;
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            Objects.requireNonNull(action);
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            Objects.requireNonNull(function);
        }

        @Override
        public V putIfAbsent(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V replace(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfAbsent(K key,
                Function<? super K, ? extends V> mappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfPresent(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V compute(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V merge(K key, V value,
                BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        // 保留单例属性
        private Object readResolve() {
            return EMPTY_MAP;
        }
    }

    // 单例集合

    /**
     * 返回一个只包含指定对象的不可变集。返回的集是可序列化的。
     *
     * @param  <T> 集合中对象的类
     * @param o 要存储在返回集中的唯一对象。
     * @return 一个只包含指定对象的不可变集。
     */
    public static <T> Set<T> singleton(T o) {
        return new SingletonSet<>(o);
    }

    static <E> Iterator<E> singletonIterator(final E e) {
        return new Iterator<E>() {
            private boolean hasNext = true;
            public boolean hasNext() {
                return hasNext;
            }
            public E next() {
                if (hasNext) {
                    hasNext = false;
                    return e;
                }
                throw new NoSuchElementException();
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void forEachRemaining(Consumer<? super E> action) {
                Objects.requireNonNull(action);
                if (hasNext) {
                    action.accept(e);
                    hasNext = false;
                }
            }
        };
    }

    /**
     * 创建一个只包含指定元素的 {@code Spliterator}
     *
     * @param <T> 元素的类型
     * @return 一个单例 {@code Spliterator}
     */
    static <T> Spliterator<T> singletonSpliterator(final T element) {
        return new Spliterator<T>() {
            long est = 1;

            @Override
            public Spliterator<T> trySplit() {
                return null;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> consumer) {
                Objects.requireNonNull(consumer);
                if (est > 0) {
                    est--;
                    consumer.accept(element);
                    return true;
                }
                return false;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> consumer) {
                tryAdvance(consumer);
            }

            @Override
            public long estimateSize() {
                return est;
            }

            @Override
            public int characteristics() {
                int value = (element != null) ? Spliterator.NONNULL : 0;

                return value | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE |
                       Spliterator.DISTINCT | Spliterator.ORDERED;
            }
        };
    }

    /**
     * @serial include
     */
    private static class SingletonSet<E>
        extends AbstractSet<E>
        implements Serializable
    {
        private static final long serialVersionUID = 3193687207550431679L;

        private final E element;

        SingletonSet(E e) {element = e;}

        public Iterator<E> iterator() {
            return singletonIterator(element);
        }

        public int size() {return 1;}

        public boolean contains(Object o) {return eq(o, element);}

        // 覆盖 Collection 中的默认方法
        @Override
        public void forEach(Consumer<? super E> action) {
            action.accept(element);
        }
        @Override
        public Spliterator<E> spliterator() {
            return singletonSpliterator(element);
        }
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            throw new UnsupportedOperationException();
        }
    }


                /**
     * 返回一个只包含指定对象的不可变列表。返回的列表是可序列化的。
     *
     * @param  <T> 列表中对象的类
     * @param o 要存储在返回列表中的唯一对象。
     * @return 一个只包含指定对象的不可变列表。
     * @since 1.3
     */
    public static <T> List<T> singletonList(T o) {
        return new SingletonList<>(o);
    }

    /**
     * @serial include
     */
    private static class SingletonList<E>
        extends AbstractList<E>
        implements RandomAccess, Serializable {

        private static final long serialVersionUID = 3093736618740652951L;

        private final E element;

        SingletonList(E obj)                {element = obj;}

        public Iterator<E> iterator() {
            return singletonIterator(element);
        }

        public int size()                   {return 1;}

        public boolean contains(Object obj) {return eq(obj, element);}

        public E get(int index) {
            if (index != 0)
              throw new IndexOutOfBoundsException("Index: "+index+", Size: 1");
            return element;
        }

        // 覆盖 Collection 的默认方法
        @Override
        public void forEach(Consumer<? super E> action) {
            action.accept(element);
        }
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void sort(Comparator<? super E> c) {
        }
        @Override
        public Spliterator<E> spliterator() {
            return singletonSpliterator(element);
        }
    }

    /**
     * 返回一个只映射指定键到指定值的不可变映射。返回的映射是可序列化的。
     *
     * @param <K> 映射键的类
     * @param <V> 映射值的类
     * @param key 要存储在返回映射中的唯一键。
     * @param value 返回映射映射的 <tt>key</tt> 的值。
     * @return 一个只包含指定键值对的不可变映射。
     * @since 1.3
     */
    public static <K,V> Map<K,V> singletonMap(K key, V value) {
        return new SingletonMap<>(key, value);
    }

    /**
     * @serial include
     */
    private static class SingletonMap<K,V>
          extends AbstractMap<K,V>
          implements Serializable {
        private static final long serialVersionUID = -6979724477215052911L;

        private final K k;
        private final V v;

        SingletonMap(K key, V value) {
            k = key;
            v = value;
        }

        public int size()                                           {return 1;}
        public boolean isEmpty()                                {return false;}
        public boolean containsKey(Object key)             {return eq(key, k);}
        public boolean containsValue(Object value)       {return eq(value, v);}
        public V get(Object key)              {return (eq(key, k) ? v : null);}

        private transient Set<K> keySet;
        private transient Set<Map.Entry<K,V>> entrySet;
        private transient Collection<V> values;

        public Set<K> keySet() {
            if (keySet==null)
                keySet = singleton(k);
            return keySet;
        }

        public Set<Map.Entry<K,V>> entrySet() {
            if (entrySet==null)
                entrySet = Collections.<Map.Entry<K,V>>singleton(
                    new SimpleImmutableEntry<>(k, v));
            return entrySet;
        }

        public Collection<V> values() {
            if (values==null)
                values = singleton(v);
            return values;
        }

        // 覆盖 Map 的默认方法
        @Override
        public V getOrDefault(Object key, V defaultValue) {
            return eq(key, k) ? v : defaultValue;
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            action.accept(k, v);
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V putIfAbsent(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V replace(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfAbsent(K key,
                Function<? super K, ? extends V> mappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfPresent(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V compute(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V merge(K key, V value,
                BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException();
        }
    }

    // 其他方法

    /**
     * 返回一个包含 <tt>n</tt> 个指定对象副本的不可变列表。新分配的数据对象很小（它包含一个对数据对象的引用）。此方法与 <tt>List.addAll</tt> 方法结合使用时，可用于扩展列表。返回的列表是可序列化的。
     *
     * @param  <T> 要复制的对象和返回列表中对象的类。
     * @param  n 返回列表中的元素数量。
     * @param  o 要在返回列表中重复出现的元素。
     * @return 一个包含 <tt>n</tt> 个指定对象副本的不可变列表。
     * @throws IllegalArgumentException 如果 {@code n < 0}
     * @see    List#addAll(Collection)
     * @see    List#addAll(int, Collection)
     */
    public static <T> List<T> nCopies(int n, T o) {
        if (n < 0)
            throw new IllegalArgumentException("List length = " + n);
        return new CopiesList<>(n, o);
    }

    /**
     * @serial include
     */
    private static class CopiesList<E>
        extends AbstractList<E>
        implements RandomAccess, Serializable
    {
        private static final long serialVersionUID = 2739099268398711800L;

        final int n;
        final E element;

        CopiesList(int n, E e) {
            assert n >= 0;
            this.n = n;
            element = e;
        }

        public int size() {
            return n;
        }

        public boolean contains(Object obj) {
            return n != 0 && eq(obj, element);
        }

        public int indexOf(Object o) {
            return contains(o) ? 0 : -1;
        }

        public int lastIndexOf(Object o) {
            return contains(o) ? n - 1 : -1;
        }

        public E get(int index) {
            if (index < 0 || index >= n)
                throw new IndexOutOfBoundsException("Index: "+index+
                                                    ", Size: "+n);
            return element;
        }

        public Object[] toArray() {
            final Object[] a = new Object[n];
            if (element != null)
                Arrays.fill(a, 0, n, element);
            return a;
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            final int n = this.n;
            if (a.length < n) {
                a = (T[])java.lang.reflect.Array
                    .newInstance(a.getClass().getComponentType(), n);
                if (element != null)
                    Arrays.fill(a, 0, n, element);
            } else {
                Arrays.fill(a, 0, n, element);
                if (a.length > n)
                    a[n] = null;
            }
            return a;
        }

        public List<E> subList(int fromIndex, int toIndex) {
            if (fromIndex < 0)
                throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
            if (toIndex > n)
                throw new IndexOutOfBoundsException("toIndex = " + toIndex);
            if (fromIndex > toIndex)
                throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                                   ") > toIndex(" + toIndex + ")");
            return new CopiesList<>(toIndex - fromIndex, element);
        }

        @Override
        public int hashCode() {
            if (n == 0) return 1;
            // n 个重复元素的 hashCode 是 31^n + elementHash * Sum(31^k, k = 0..n-1)
            // 该实现通过利用 31^(2*n) = (31^n)^2 和 Sum(31^k, k = 0..(2*n-1)) = Sum(31^k, k = 0..n-1) * (31^n + 1) 在 O(log(n)) 步骤内完成
            int pow = 31;
            int sum = 1;
            for (int i = Integer.numberOfLeadingZeros(n) + 1; i < Integer.SIZE; i++) {
                sum *= pow + 1;
                pow *= pow;
                if ((n << i) < 0) {
                    pow *= 31;
                    sum = sum * 31 + 1;
                }
            }
            return pow + sum * (element == null ? 0 : element.hashCode());
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof CopiesList) {
                CopiesList<?> other = (CopiesList<?>) o;
                return n == other.n && (n == 0 || eq(element, other.element));
            }
            if (!(o instanceof List))
                return false;

            int remaining = n;
            E e = element;
            Iterator<?> itr = ((List<?>) o).iterator();
            if (e == null) {
                while (itr.hasNext() && remaining-- > 0) {
                    if (itr.next() != null)
                        return false;
                }
            } else {
                while (itr.hasNext() && remaining-- > 0) {
                    if (!e.equals(itr.next()))
                        return false;
                }
            }
            return remaining == 0 && !itr.hasNext();
        }

        // 覆盖 Collection 的默认方法
        @Override
        public Stream<E> stream() {
            return IntStream.range(0, n).mapToObj(i -> element);
        }

        @Override
        public Stream<E> parallelStream() {
            return IntStream.range(0, n).parallel().mapToObj(i -> element);
        }

        @Override
        public Spliterator<E> spliterator() {
            return stream().spliterator();
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            SharedSecrets.getJavaOISAccess().checkArray(ois, Object[].class, n);
        }
    }

    /**
     * 返回一个比较器，该比较器对实现 {@code Comparable} 接口的对象集合施加自然顺序的反向。自然顺序是由对象自身的 {@code compareTo} 方法施加的顺序。这使得可以简单地对（或维护）实现 {@code Comparable} 接口的对象集合（或数组）按反向自然顺序进行排序。例如，假设 {@code a} 是一个字符串数组。那么： <pre>
     *          Arrays.sort(a, Collections.reverseOrder());
     * </pre> 按反向字典（字母）顺序对数组进行排序。<p>
     *
     * 返回的比较器是可序列化的。
     *
     * @param  <T> 比较器比较的对象的类
     * @return 一个比较器，该比较器对实现 <tt>Comparable</tt> 接口的对象集合施加自然顺序的反向。
     * @see Comparable
     */
    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> reverseOrder() {
        return (Comparator<T>) ReverseComparator.REVERSE_ORDER;
    }

    /**
     * @serial include
     */
    private static class ReverseComparator
        implements Comparator<Comparable<Object>>, Serializable {

        private static final long serialVersionUID = 7207038068494060240L;

        static final ReverseComparator REVERSE_ORDER
            = new ReverseComparator();

        public int compare(Comparable<Object> c1, Comparable<Object> c2) {
            return c2.compareTo(c1);
        }

        private Object readResolve() { return Collections.reverseOrder(); }

        @Override
        public Comparator<Comparable<Object>> reversed() {
            return Comparator.naturalOrder();
        }
    }

    /**
     * 返回一个比较器，该比较器对指定比较器施加的顺序进行反向。如果指定的比较器为 {@code null}，此方法等同于 {@link #reverseOrder()}（换句话说，它返回一个比较器，该比较器对实现 Comparable 接口的对象集合施加自然顺序的反向）。
     *
     * <p>返回的比较器是可序列化的（假设指定的比较器也是可序列化的或 {@code null}）。
     *
     * @param <T> 比较器比较的对象的类
     * @param cmp 要由返回的比较器反向排序的比较器，或 {@code null}
     * @return 一个比较器，该比较器对指定的比较器施加的顺序进行反向。
     * @since 1.5
     */
    public static <T> Comparator<T> reverseOrder(Comparator<T> cmp) {
        if (cmp == null)
            return reverseOrder();

        if (cmp instanceof ReverseComparator2)
            return ((ReverseComparator2<T>)cmp).cmp;

        return new ReverseComparator2<>(cmp);
    }

    /**
     * @serial include
     */
    private static class ReverseComparator2<T> implements Comparator<T>,
        Serializable
    {
        private static final long serialVersionUID = 4374092139857L;

        /**
         * 静态工厂中指定的比较器。这永远不会为 null，因为静态工厂在其参数为 null 时返回一个 ReverseComparator 实例。
         *
         * @serial
         */
        final Comparator<T> cmp;

        ReverseComparator2(Comparator<T> cmp) {
            assert cmp != null;
            this.cmp = cmp;
        }

        public int compare(T t1, T t2) {
            return cmp.compare(t2, t1);
        }

        public boolean equals(Object o) {
            return (o == this) ||
                (o instanceof ReverseComparator2 &&
                 cmp.equals(((ReverseComparator2)o).cmp));
        }


                    public int hashCode() {
            return cmp.hashCode() ^ Integer.MIN_VALUE;
        }

        @Override
        public Comparator<T> reversed() {
            return cmp;
        }
    }

    /**
     * 返回指定集合的枚举。这提供了与需要枚举作为输入的旧 API 的互操作性。
     *
     * @param  <T> 集合中对象的类
     * @param c 要返回枚举的集合。
     * @return 指定集合的枚举。
     * @see Enumeration
     */
    public static <T> Enumeration<T> enumeration(final Collection<T> c) {
        return new Enumeration<T>() {
            private final Iterator<T> i = c.iterator();

            public boolean hasMoreElements() {
                return i.hasNext();
            }

            public T nextElement() {
                return i.next();
            }
        };
    }

    /**
     * 返回由指定枚举返回的元素组成的数组列表，顺序与枚举返回的顺序相同。此方法提供了旧 API 返回枚举与新 API 需要集合之间的互操作性。
     *
     * @param <T> 枚举返回的对象的类
     * @param e 提供返回的数组列表元素的枚举
     * @return 由指定枚举返回的元素组成的数组列表。
     * @since 1.4
     * @see Enumeration
     * @see ArrayList
     */
    public static <T> ArrayList<T> list(Enumeration<T> e) {
        ArrayList<T> l = new ArrayList<>();
        while (e.hasMoreElements())
            l.add(e.nextElement());
        return l;
    }

    /**
     * 如果指定的参数相等或都为 null，则返回 true。
     *
     * 注意：在 JDK-8015417 解决之前，不要用 Object.equals 替换。
     */
    static boolean eq(Object o1, Object o2) {
        return o1==null ? o2==null : o1.equals(o2);
    }

    /**
     * 返回指定集合中等于指定对象的元素数量。更正式地说，返回集合中满足
     * <tt>(o == null ? e == null : o.equals(e))</tt> 的元素数量。
     *
     * @param c 要确定 <tt>o</tt> 频率的集合
     * @param o 要确定其频率的对象
     * @return 等于 {@code o} 的元素数量
     * @throws NullPointerException 如果 <tt>c</tt> 为 null
     * @since 1.5
     */
    public static int frequency(Collection<?> c, Object o) {
        int result = 0;
        if (o == null) {
            for (Object e : c)
                if (e == null)
                    result++;
        } else {
            for (Object e : c)
                if (o.equals(e))
                    result++;
        }
        return result;
    }

    /**
     * 如果两个指定的集合没有共同的元素，则返回 {@code true}。
     *
     * <p>如果此方法用于不符合 {@code Collection} 通用合同的集合，则必须谨慎。实现可以选择迭代任一集合并测试是否包含在另一个集合中（或执行任何等效的计算）。如果任一集合使用非标准的相等测试（如 {@link SortedSet} 的排序与 <em>equals 不兼容</em>，或 {@link IdentityHashMap} 的键集），则两个集合都必须使用相同的非标准相等测试，否则此方法的结果是未定义的。
     *
     * <p>当使用对元素有特定限制的集合时也必须谨慎。集合实现允许对它们认为不合格的元素抛出异常。为了绝对安全，指定的集合应仅包含对两个集合都合格的元素。
     *
     * <p>允许将相同的集合作为两个参数传递，此时如果集合为空，则方法将返回 {@code true}。
     *
     * @param c1 一个集合
     * @param c2 一个集合
     * @return 如果两个指定的集合没有共同的元素，则返回 {@code true}。
     * @throws NullPointerException 如果任一集合为 {@code null}。
     * @throws NullPointerException 如果一个集合包含 {@code null} 元素，而 {@code null} 不是另一个集合的合格元素。
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws ClassCastException 如果一个集合包含对另一个集合不合格的元素。
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @since 1.5
     */
    public static boolean disjoint(Collection<?> c1, Collection<?> c2) {
        // 用于 contains() 的集合。优先选择 contains() 复杂度较低的集合。
        Collection<?> contains = c2;
        // 要迭代的集合。如果集合的 contains() 实现复杂度不同，则使用 contains() 较慢的集合进行迭代。如果集合的 contains() 复杂度相同，则最佳性能是迭代较小的集合。
        Collection<?> iterate = c1;

        // 性能优化情况。启发式：
        //   1. 通常迭代 c1。
        //   2. 如果 c1 是 Set，则迭代 c2。
        //   3. 如果任一集合为空，则结果始终为 true。
        //   4. 迭代较小的集合。
        if (c1 instanceof Set) {
            // 使用 c1 进行 contains，因为 Set 的 contains() 预期性能优于 O(N/2)
            iterate = c2;
            contains = c1;
        } else if (!(c2 instanceof Set)) {
            // 两者都是普通的集合。迭代较小的集合。
            // 示例：如果 c1 包含 3 个元素，c2 包含 50 个元素，并假设 contains() 需要 ceiling(N/2) 次比较，则检查 c1 中的所有元素是否在 c2 中需要 75 次比较 (3 * ceiling(50/2))，而检查 c2 中的所有元素是否在 c1 中需要 100 次比较 (50 * ceiling(3/2))。
            int c1size = c1.size();
            int c2size = c2.size();
            if (c1size == 0 || c2size == 0) {
                // 至少有一个集合为空。没有元素匹配。
                return true;
            }

            if (c1size > c2size) {
                iterate = c2;
                contains = c1;
            }
        }

        for (Object e : iterate) {
            if (contains.contains(e)) {
               // 找到一个共同的元素。集合不是不相交的。
                return false;
            }
        }

        // 没有找到共同的元素。
        return true;
    }

    /**
     * 将指定的所有元素添加到指定的集合中。要添加的元素可以单独指定或作为数组指定。此便捷方法的行为与 <tt>c.addAll(Arrays.asList(elements))</tt> 相同，但此方法在大多数实现中可能会显著更快。
     *
     * <p>当元素单独指定时，此方法提供了一种方便的方式，将少量元素添加到现有集合中：
     * <pre>
     *     Collections.addAll(flavors, "Peaches 'n Plutonium", "Rocky Racoon");
     * </pre>
     *
     * @param  <T> 要添加的元素和集合的类
     * @param c 要插入 <tt>elements</tt> 的集合
     * @param elements 要插入到 <tt>c</tt> 中的元素
     * @return 如果调用后集合发生变化，则返回 <tt>true</tt>
     * @throws UnsupportedOperationException 如果 <tt>c</tt> 不支持 <tt>add</tt> 操作
     * @throws NullPointerException 如果 <tt>elements</tt> 包含一个或多个 null 值且 <tt>c</tt> 不允许 null 元素，或 <tt>c</tt> 或 <tt>elements</tt> 为 <tt>null</tt>
     * @throws IllegalArgumentException 如果 <tt>elements</tt> 中某个值的属性阻止其被添加到 <tt>c</tt>
     * @see Collection#addAll(Collection)
     * @since 1.5
     */
    @SafeVarargs
    public static <T> boolean addAll(Collection<? super T> c, T... elements) {
        boolean result = false;
        for (T element : elements)
            result |= c.add(element);
        return result;
    }

    /**
     * 返回由指定映射支持的集合。返回的集合显示与支持映射相同的顺序、并发性和性能特性。本质上，此工厂方法为任何映射实现提供了一个 {@link Set} 实现。对于已经有一个对应的 {@link Set} 实现的 {@link Map} 实现（如 {@link HashMap} 或 {@link TreeMap}），无需使用此方法。
     *
     * <p>对通过此方法返回的集合的每次方法调用都会导致对支持映射或其 <tt>keySet</tt> 视图的恰好一次方法调用，有一个例外。{@link Collection#addAll addAll} 方法是通过在支持映射上执行一系列 <tt>put</tt> 调用来实现的。
     *
     * <p>调用此方法时，指定的映射必须为空，并且在方法返回后不应直接访问该映射。这些条件可以通过创建一个空的映射，直接传递给此方法，并且不保留对映射的引用来确保，如以下代码片段所示：
     * <pre>
     *    Set&lt;Object&gt; weakHashSet = Collections.newSetFromMap(
     *        new WeakHashMap&lt;Object, Boolean&gt;());
     * </pre>
     *
     * @param <E> 映射键和返回集合中对象的类
     * @param map 支持的映射
     * @return 由映射支持的集合
     * @throws IllegalArgumentException 如果 <tt>map</tt> 不为空
     * @since 1.6
     */
    public static <E> Set<E> newSetFromMap(Map<E, Boolean> map) {
        return new SetFromMap<>(map);
    }

    /**
     * @serial include
     */
    private static class SetFromMap<E> extends AbstractSet<E>
        implements Set<E>, Serializable
    {
        private final Map<E, Boolean> m;  // 支持的映射
        private transient Set<E> s;       // 其 keySet

        SetFromMap(Map<E, Boolean> map) {
            if (!map.isEmpty())
                throw new IllegalArgumentException("Map is non-empty");
            m = map;
            s = map.keySet();
        }

        public void clear()               {        m.clear(); }
        public int size()                 { return m.size(); }
        public boolean isEmpty()          { return m.isEmpty(); }
        public boolean contains(Object o) { return m.containsKey(o); }
        public boolean remove(Object o)   { return m.remove(o) != null; }
        public boolean add(E e) { return m.put(e, Boolean.TRUE) == null; }
        public Iterator<E> iterator()     { return s.iterator(); }
        public Object[] toArray()         { return s.toArray(); }
        public <T> T[] toArray(T[] a)     { return s.toArray(a); }
        public String toString()          { return s.toString(); }
        public int hashCode()             { return s.hashCode(); }
        public boolean equals(Object o)   { return o == this || s.equals(o); }
        public boolean containsAll(Collection<?> c) {return s.containsAll(c);}
        public boolean removeAll(Collection<?> c)   {return s.removeAll(c);}
        public boolean retainAll(Collection<?> c)   {return s.retainAll(c);}
        // addAll 是唯一继承的实现

        // 覆盖 Collection 中的默认方法
        @Override
        public void forEach(Consumer<? super E> action) {
            s.forEach(action);
        }
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return s.removeIf(filter);
        }

        @Override
        public Spliterator<E> spliterator() {return s.spliterator();}
        @Override
        public Stream<E> stream()           {return s.stream();}
        @Override
        public Stream<E> parallelStream()   {return s.parallelStream();}

        private static final long serialVersionUID = 2454657854757543876L;

        private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException
        {
            stream.defaultReadObject();
            s = m.keySet();
        }
    }

    /**
     * 返回 {@link Deque} 作为后进先出 (Lifo) {@link Queue} 的视图。方法 <tt>add</tt> 映射到 <tt>push</tt>，<tt>remove</tt> 映射到 <tt>pop</tt> 等等。当您需要 Lifo 顺序但需要使用需要 <tt>Queue</tt> 的方法时，此视图可以很有用。
     *
     * <p>对通过此方法返回的队列的每次方法调用都会导致对支持双端队列的恰好一次方法调用，有一个例外。{@link Queue#addAll addAll} 方法是通过在支持双端队列上执行一系列 {@link Deque#addFirst addFirst} 调用来实现的。
     *
     * @param  <T> 双端队列中对象的类
     * @param deque 双端队列
     * @return 队列
     * @since  1.6
     */
    public static <T> Queue<T> asLifoQueue(Deque<T> deque) {
        return new AsLIFOQueue<>(deque);
    }

    /**
     * @serial include
     */
    static class AsLIFOQueue<E> extends AbstractQueue<E>
        implements Queue<E>, Serializable {
        private static final long serialVersionUID = 1802017725587941708L;
        private final Deque<E> q;
        AsLIFOQueue(Deque<E> q)           { this.q = q; }
        public boolean add(E e)           { q.addFirst(e); return true; }
        public boolean offer(E e)         { return q.offerFirst(e); }
        public E poll()                   { return q.pollFirst(); }
        public E remove()                 { return q.removeFirst(); }
        public E peek()                   { return q.peekFirst(); }
        public E element()                { return q.getFirst(); }
        public void clear()               {        q.clear(); }
        public int size()                 { return q.size(); }
        public boolean isEmpty()          { return q.isEmpty(); }
        public boolean contains(Object o) { return q.contains(o); }
        public boolean remove(Object o)   { return q.remove(o); }
        public Iterator<E> iterator()     { return q.iterator(); }
        public Object[] toArray()         { return q.toArray(); }
        public <T> T[] toArray(T[] a)     { return q.toArray(a); }
        public String toString()          { return q.toString(); }
        public boolean containsAll(Collection<?> c) {return q.containsAll(c);}
        public boolean removeAll(Collection<?> c)   {return q.removeAll(c);}
        public boolean retainAll(Collection<?> c)   {return q.retainAll(c);}
        // 我们使用继承的 addAll；转发 addAll 会出错


                    // 重写 Collection 中的默认方法
        @Override
        public void forEach(Consumer<? super E> action) {q.forEach(action);}
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return q.removeIf(filter);
        }
        @Override
        public Spliterator<E> spliterator() {return q.spliterator();}
        @Override
        public Stream<E> stream()           {return q.stream();}
        @Override
        public Stream<E> parallelStream()   {return q.parallelStream();}
    }
}
