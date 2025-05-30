
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
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 一个可扩展的并发 {@link ConcurrentNavigableMap} 实现。
 * 该映射根据其键的 {@linkplain Comparable 自然排序} 或在创建映射时提供的 {@link Comparator} 进行排序，具体取决于使用哪个构造函数。
 *
 * <p>此类实现了 <a
 * href="http://en.wikipedia.org/wiki/Skip_list" target="_top">SkipLists</a> 的并发变体，
 * 提供了 {@code containsKey}、{@code get}、{@code put} 和
 * {@code remove} 操作及其变体的预期平均 <i>log(n)</i> 时间成本。插入、删除、更新和访问操作可以由多个线程安全地并发执行。
 *
 * <p>迭代器和分割迭代器是
 * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
 *
 * <p>升序键排序视图及其迭代器比降序的更快。
 *
 * <p>本类及其视图和迭代器返回的所有 {@code Map.Entry} 对都代表生成时的映射快照。它们 <em>不</em> 支持 {@code Entry.setValue}
 * 方法。（但是，可以使用 {@code put}、{@code putIfAbsent} 或
 * {@code replace} 更改关联映射中的映射，具体取决于您需要的效果。）
 *
 * <p>请注意，与大多数集合不同，{@code size}
 * 方法 <em>不是</em> 常量时间操作。由于这些映射的异步性质，确定当前元素数量需要遍历元素，因此如果在此集合遍历时进行修改，可能会报告不准确的结果。
 * 此外，批量操作 {@code putAll}、{@code equals}、{@code toArray}、{@code containsValue} 和 {@code clear}
 * <em>不是</em> 保证原子执行的。例如，与 {@code putAll} 操作并发运行的迭代器可能只能看到部分添加的元素。
 *
 * <p>此类及其视图和迭代器实现了 {@link Map} 和 {@link Iterator}
 * 接口的所有 <em>可选</em> 方法。像大多数其他并发集合一样，此类 <em>不</em> 允许使用 {@code null} 键或值，因为某些 null 返回值不能可靠地区分于元素的缺失。
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @author Doug Lea
 * @param <K> 由该映射维护的键的类型
 * @param <V> 映射值的类型
 * @since 1.6
 */
public class ConcurrentSkipListMap<K,V> extends AbstractMap<K,V>
    implements ConcurrentNavigableMap<K,V>, Cloneable, Serializable {
    /*
     * 该类实现了一个树状二维链接跳表，其中索引级别与保存数据的基本节点分开表示。采用这种方法而不是通常的基于数组的结构有两个原因：
     * 1) 基于数组的实现似乎遇到更多的复杂性和开销
     * 2) 我们可以为频繁遍历的索引列表使用更便宜的算法，而这些算法不能用于基本列表。以下是具有 2 级索引的可能列表的一些基本示意图：
     *
     * 头节点          索引节点
     * +-+    right        +-+                      +-+
     * |2|---------------->| |--------------------->| |->null
     * +-+                 +-+                      +-+
     *  | down              |                        |
     *  v                   v                        v
     * +-+            +-+  +-+       +-+            +-+       +-+
     * |1|----------->| |->| |------>| |----------->| |------>| |->null
     * +-+            +-+  +-+       +-+            +-+       +-+
     *  v              |    |         |              |         |
     * 节点  next     v    v         v              v         v
     * +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+
     * | |->|A|->|B|->|C|->|D|->|E|->|F|->|G|->|H|->|I|->|J|->|K|->null
     * +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+  +-+
     *
     * 基本列表使用 HM 链接有序集算法的变体。参见 Tim Harris, "A pragmatic implementation of
     * non-blocking linked lists"
     * http://www.cl.cam.ac.uk/~tlh20/publications.html 和 Maged
     * Michael "High Performance Dynamic Lock-Free Hash Tables and
     * List-Based Sets"
     * http://www.research.ibm.com/people/m/michael/pubs.htm。这些列表的基本思想是在删除时标记已删除节点的“next”指针以避免与并发插入冲突，并在遍历时跟踪三元组
     * (predecessor, node, successor) 以检测何时以及如何取消这些已删除节点的链接。
     *
     * 而不是使用标记位来标记列表删除（这在使用 AtomicMarkedReference 时可能较慢且占用更多空间），节点使用直接 CAS'able next 指针。在删除时，不是标记指针，而是插入另一个可以认为是标记指针的节点（通过使用其他不可能的字段值来指示）。使用普通节点的行为类似于“装箱”实现的标记指针，但仅在节点删除时使用新节点，而不是每个链接都使用。这需要更少的空间并支持更快的遍历。即使 JVM 更好地支持标记引用，使用此技术进行遍历可能仍然更快，因为任何搜索只需要读取一个额外的节点（以检查尾部标记），而不需要取消掩码标记位或类似操作。
     *
     * 这种方法保持了 HM 算法所需的更改已删除节点的 next 指针以使任何其他 CAS 失败的基本属性，但通过将指针更改为指向不同的节点来实现，而不是标记它。虽然可以通过定义标记节点没有 key/value 字段来进一步压缩空间，但这不值得额外的类型测试开销。删除标记很少在遍历时遇到，并且通常会很快被垃圾回收。（请注意，此技术在没有垃圾回收的系统中工作不佳。）
     *
     * 除了使用删除标记外，列表还使用值字段的 null 性来表示删除，风格类似于典型的懒删除方案。如果节点的值为 null，则认为该节点逻辑上已删除并忽略，即使它仍然可到达。这保持了并发替换与删除操作的正确控制——尝试替换必须在删除操作将其字段置为 null 之前失败，而删除操作必须返回字段中最后的非 null 值。（注意：这里使用 null 而不是其他特殊标记值，因为这恰好符合 Map API 要求方法 get 在没有映射时返回 null，这允许节点在删除后仍可并发读取。使用其他标记值在这里将是最糟糕的。）
     *
     * 以下是节点 n 的删除序列，其前驱为 b，后继为 f，初始状态如下：
     *
     *        +------+       +------+      +------+
     *   ...  |   b  |------>|   n  |----->|   f  | ...
     *        +------+       +------+      +------+
     *
     * 1. CAS n 的值字段从非 null 到 null。
     *    从这一点开始，遇到该节点的任何公共操作都认为此映射不存在。但是，其他正在进行的插入和删除操作可能会修改 n 的 next 指针。
     *
     * 2. CAS n 的 next 指针指向一个新的标记节点。
     *    从这一点开始，不再有其他节点可以附加到 n，这避免了基于 CAS 的链接列表中的删除错误。
     *
     *        +------+       +------+      +------+       +------+
     *   ...  |   b  |------>|   n  |----->|marker|------>|   f  | ...
     *        +------+       +------+      +------+       +------+
     *
     * 3. CAS b 的 next 指针越过 n 及其标记。
     *    从这一点开始，新的遍历将不再遇到 n，它可以最终被垃圾回收。
     *        +------+                                    +------+
     *   ...  |   b  |----------------------------------->|   f  | ...
     *        +------+                                    +------+
     *
     * 第 1 步失败会导致简单的重试，因为与另一个操作的竞争失败。第 2-3 步可能失败，因为其他线程在遍历时注意到一个值为 null 的节点并帮助标记和/或取消链接。这种帮助确保没有线程会因删除线程的进展而停滞。使用标记节点稍微复杂化了帮助代码，因为遍历必须跟踪最多四个节点的一致读取 (b, n, marker, f)，而不仅仅是 (b, n, f)，尽管标记的 next 字段是不可变的，一旦 next 字段 CAS 到指向一个标记，它就不再改变，这需要较少的注意。
     *
     * 跳表在此方案中添加了索引，因此基本级别的遍历从接近要查找、插入或删除的位置开始——通常基本级别的遍历只遍历几个节点。这不会改变基本算法，除了需要确保基本遍历从未（结构上）删除的前驱节点 (b) 开始，否则在处理删除后重新尝试。
     *
     * 索引级别维护为具有易失性 next 字段的列表，使用 CAS 链接和取消链接。允许索引列表操作中的竞争，这可能导致（很少）无法链接新索引节点或删除一个。但是，即使发生这种情况，索引列表仍然保持排序，因此正确地充当索引。这可能会影响性能，但由于跳表是概率性的，实际结果是在竞争下，有效的 "p" 值可能低于其名义值。并且竞争窗口保持足够小，即使在大量竞争下，这些失败也很罕见。
     *
     * 由于索引使重试（对于基本列表和索引列表）相对便宜，允许一些重试逻辑的简化。大多数“帮助”CAS 之后都会重新启动遍历。这不总是严格必要，但隐式的后退通常有助于减少其他下游失败的 CAS 足以抵消重新启动的成本。这恶化了最坏情况，但似乎改善了即使高度竞争的情况。
     *
     * 与大多数跳表实现不同，这里的索引插入和删除需要在基本级别操作之后进行单独的遍历，以添加或删除索引节点。这增加了单线程开销，但通过缩小干扰窗口改善了多线程竞争性能，并允许删除确保所有索引节点在从公共 remove 操作返回时变得不可达，从而避免不必要的垃圾保留。这在这里比在某些其他数据结构中更重要，因为我们不能将引用用户键的节点字段置为 null，因为它们可能仍被其他正在进行的遍历读取。
     *
     * 索引使用保持良好搜索性能同时使用比通常更稀疏的索引的跳表参数：硬编码参数 k=1, p=0.5（参见方法 doPut）意味着大约四分之一的节点有索引。其中，一半有一级，四分之一有两级，依此类推（参见 Pugh 的 Skip List Cookbook，第 3.4 节）。该映射的预期总空间需求略低于当前 java.util.TreeMap 实现。
     *
     * 更改索引级别（即，树状结构的高度）也使用 CAS。头索引的初始级别/高度为一。创建高度大于当前级别的索引通过 CAS 在新的最高头节点上添加一个级别。为了在大量删除后保持良好性能，删除方法启发式地尝试减少高度，如果最顶层看起来为空。这可能会遇到竞争，可能（但很少）在即将包含索引时减少并“丢失”一个级别。这不会造成结构上的损害，实际上在实践中似乎是一个比允许级别无限制增长更好的选择。
     *
     * 所有这些代码比你希望的更冗长。大多数操作涉及定位一个元素（或插入元素的位置）。执行此操作的代码不能很好地分解，因为后续使用需要一个前驱和/或后继和/或值字段的快照，这些不能一次返回，至少不创建另一个对象来保存它们——创建这样的小对象对于基本内部搜索操作来说尤其糟糕，因为它增加了垃圾回收开销。（这是我少数几次希望 Java 有宏的情况之一。）相反，一些遍历代码在插入和删除操作中交织。处理所有重试条件的控制逻辑有时会很复杂。大多数搜索分为两部分。findPredecessor() 仅搜索索引节点，返回键的基级别前驱。findNode() 完成基级别搜索。即使有这种分解，也有相当多的代码变体。
     *
     * 为了在不同线程之间不干扰地生成随机值，我们使用 JDK 内部的线程本地随机支持（通过“次级种子”，以避免与用户级别的 ThreadLocalRandom 干扰。）
     *
     * 早期版本的此类通过使用其比较器包装不可比较的键来在使用比较器与 Comparable 时模拟 Comparable。但是，JVM 现在似乎更好地处理将比较器与 Comparable 选择融入搜索循环。静态方法 cpr(comparator, x, y) 用于所有比较，只要比较器参数在循环外部设置（因此有时作为内部方法的参数传递）以避免字段重读。
     *
     * 有关与本算法至少具有两个共同特征的算法的解释，请参阅 Mikhail Fomitchev 的论文
     * (http://www.cs.yorku.ca/~mikhail/)，Keir Fraser 的论文
     * (http://www.cl.cam.ac.uk/users/kaf24/) 和 Hakan Sundell 的论文
     * (http://www.cs.chalmers.se/~phs/)。
     *
     * 鉴于使用树状索引节点，你可能会好奇为什么这里不使用某种搜索树，这将支持稍快的搜索操作。原因是目前没有已知的高效无锁插入和删除算法适用于搜索树。索引节点的“down”链接的不可变性（与真实树中的可变“left”字段相对）使得仅使用 CAS 操作即可实现这一点。
     *
     * 本地变量命名指南
     * 节点：         b, n, f    前驱，节点，后继
     * 索引：        q, r, d    索引节点，右，下。
     *               t          另一个索引节点
     * 头：         h
     * 级别：       j
     * 键：         k, key
     * 值：         v, value
     * 比较：       c
     */


                private static final long serialVersionUID = -8627078645895051609L;

    /**
     * 用于标识基础级别头的特殊值。
     */
    private static final Object BASE_HEADER = new Object();

    /**
     * 跳表的最顶端头索引。
     */
    private transient volatile HeadIndex<K,V> head;

    /**
     * 用于维护此映射中顺序的比较器，如果使用自然排序则为 null。 （非私有以简化嵌套类中的访问。）
     * @serial
     */
    final Comparator<? super K> comparator;

    /** 懒初始化键集 */
    private transient KeySet<K> keySet;
    /** 懒初始化条目集 */
    private transient EntrySet<K,V> entrySet;
    /** 懒初始化值集合 */
    private transient Values<V> values;
    /** 懒初始化降序键集 */
    private transient ConcurrentNavigableMap<K,V> descendingMap;

    /**
     * 初始化或重置状态。由构造函数、克隆、清除、readObject 和 ConcurrentSkipListSet.clone 需要。
     * （注意比较器必须单独初始化。）
     */
    private void initialize() {
        keySet = null;
        entrySet = null;
        values = null;
        descendingMap = null;
        head = new HeadIndex<K,V>(new Node<K,V>(null, BASE_HEADER, null),
                                  null, null, 1);
    }

    /**
     * 比较并设置头节点。
     */
    private boolean casHead(HeadIndex<K,V> cmp, HeadIndex<K,V> val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }

    /* ---------------- 节点 -------------- */

    /**
     * 节点持有键和值，并按排序顺序单链。可能在某些标记节点之间插入。列表由可访问的头节点 head.node 开头。值字段仅声明为 Object，因为标记和头节点具有特殊的非-V 值。
     */
    static final class Node<K,V> {
        final K key;
        volatile Object value;
        volatile Node<K,V> next;

        /**
         * 创建一个新的普通节点。
         */
        Node(K key, Object value, Node<K,V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        /**
         * 创建一个新的标记节点。标记通过其值字段指向自身来区分。标记节点也有 null 键，这一点在某些地方被利用，但这并不能区分标记和基础级别头节点（head.node），后者也有 null 键。
         */
        Node(Node<K,V> next) {
            this.key = null;
            this.value = this;
            this.next = next;
        }

        /**
         * 比较并设置值字段。
         */
        boolean casValue(Object cmp, Object val) {
            return UNSAFE.compareAndSwapObject(this, valueOffset, cmp, val);
        }

        /**
         * 比较并设置下一个字段。
         */
        boolean casNext(Node<K,V> cmp, Node<K,V> val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
        }

        /**
         * 如果此节点是标记节点，则返回 true。此方法实际上在当前代码中检查标记时不会被调用，因为调用者已经读取了值字段并需要使用该读取（而不是在这里再做一次读取），因此直接测试值是否指向节点。
         *
         * @return 如果此节点是标记节点，则返回 true
         */
        boolean isMarker() {
            return value == this;
        }

        /**
         * 如果此节点是基础级别列表的头节点，则返回 true。
         * @return 如果此节点是头节点，则返回 true
         */
        boolean isBaseHeader() {
            return value == BASE_HEADER;
        }

        /**
         * 尝试将删除标记附加到此节点。
         * @param f 此节点的当前后继节点
         * @return 如果成功，则返回 true
         */
        boolean appendMarker(Node<K,V> f) {
            return casNext(f, new Node<K,V>(f));
        }

        /**
         * 通过附加标记或从前驱节点中取消链接来帮助删除。当遍历时发现值字段为 null 时调用此方法。
         * @param b 前驱节点
         * @param f 后继节点
         */
        void helpDelete(Node<K,V> b, Node<K,V> f) {
            /*
             * 重新检查链接，然后每次调用只执行一个帮助阶段，以尽量减少帮助线程之间的 CAS 干扰。
             */
            if (f == next && this == b.next) {
                if (f == null || f.value != f) // 未标记
                    casNext(f, new Node<K,V>(f));
                else
                    b.casNext(this, f.next);
            }
        }

        /**
         * 如果此节点包含有效的键值对，则返回值，否则返回 null。
         * @return 如果此节点不是标记或头节点或已被删除，则返回此节点的值，否则返回 null
         */
        V getValidValue() {
            Object v = value;
            if (v == this || v == BASE_HEADER)
                return null;
            @SuppressWarnings("unchecked") V vv = (V)v;
            return vv;
        }

        /**
         * 如果此节点包含有效值，则创建并返回一个新的 SimpleImmutableEntry，否则返回 null。
         * @return 新的条目或 null
         */
        AbstractMap.SimpleImmutableEntry<K,V> createSnapshot() {
            Object v = value;
            if (v == null || v == this || v == BASE_HEADER)
                return null;
            @SuppressWarnings("unchecked") V vv = (V)v;
            return new AbstractMap.SimpleImmutableEntry<K,V>(key, vv);
        }

        // UNSAFE 机制

        private static final sun.misc.Unsafe UNSAFE;
        private static final long valueOffset;
        private static final long nextOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Node.class;
                valueOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("value"));
                nextOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /* ---------------- 索引 -------------- */

    /**
     * 索引节点表示跳表的级别。注意，尽管节点和索引都有前向指针字段，但它们有不同的类型和不同的处理方式，这些无法通过将字段放在共享抽象类中来很好地捕获。
     */
    static class Index<K,V> {
        final Node<K,V> node;
        final Index<K,V> down;
        volatile Index<K,V> right;

        /**
         * 使用给定值创建索引节点。
         */
        Index(Node<K,V> node, Index<K,V> down, Index<K,V> right) {
            this.node = node;
            this.down = down;
            this.right = right;
        }

        /**
         * 比较并设置右字段。
         */
        final boolean casRight(Index<K,V> cmp, Index<K,V> val) {
            return UNSAFE.compareAndSwapObject(this, rightOffset, cmp, val);
        }

        /**
         * 如果此索引的节点已被删除，则返回 true。
         * @return 如果索引的节点已知被删除，则返回 true
         */
        final boolean indexesDeletedNode() {
            return node.value == null;
        }

        /**
         * 尝试 CAS 新后继节点。为了尽量减少与取消链接的竞争，如果被索引的节点已知被删除，则不会尝试链接。
         * @param succ 当前后继节点
         * @param newSucc 新后继节点
         * @return 如果成功，则返回 true
         */
        final boolean link(Index<K,V> succ, Index<K,V> newSucc) {
            Node<K,V> n = node;
            newSucc.right = succ;
            return n.value != null && casRight(succ, newSucc);
        }

        /**
         * 尝试 CAS 右字段以跳过当前后继节点。如果此节点已知被删除，则失败（迫使调用者重新遍历）。
         * @param succ 当前后继节点
         * @return 如果成功，则返回 true
         */
        final boolean unlink(Index<K,V> succ) {
            return node.value != null && casRight(succ, succ.right);
        }

        // Unsafe 机制
        private static final sun.misc.Unsafe UNSAFE;
        private static final long rightOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Index.class;
                rightOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("right"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /* ---------------- 头节点 -------------- */

    /**
     * 每个级别的头节点跟踪它们的级别。
     */
    static final class HeadIndex<K,V> extends Index<K,V> {
        final int level;
        HeadIndex(Node<K,V> node, Index<K,V> down, Index<K,V> right, int level) {
            super(node, down, right);
            this.level = level;
        }
    }

    /* ---------------- 比较工具 -------------- */

    /**
     * 使用比较器或自然排序（如果为 null）。仅由已执行所需类型检查的方法调用。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static final int cpr(Comparator c, Object x, Object y) {
        return (c != null) ? c.compare(x, y) : ((Comparable)x).compareTo(y);
    }

    /* ---------------- 遍历 -------------- */

    /**
     * 返回键严格小于给定键的基础级别节点，如果没有这样的节点，则返回基础级别头节点。同时清除沿路遇到的已删除节点的索引。调用者依赖于清除已删除节点索引的副作用。
     * @param key 键
     * @return 键的前驱节点
     */
    private Node<K,V> findPredecessor(Object key, Comparator<? super K> cmp) {
        if (key == null)
            throw new NullPointerException(); // 不推迟错误
        for (;;) {
            for (Index<K,V> q = head, r = q.right, d;;) {
                if (r != null) {
                    Node<K,V> n = r.node;
                    K k = n.key;
                    if (n.value == null) {
                        if (!q.unlink(r))
                            break;           // 重新开始
                        r = q.right;         // 重新读取 r
                        continue;
                    }
                    if (cpr(cmp, key, k) > 0) {
                        q = r;
                        r = r.right;
                        continue;
                    }
                }
                if ((d = q.down) == null)
                    return q.node;
                q = d;
                r = d.right;
            }
        }
    }

    /**
     * 返回持有键的节点或如果没有这样的节点则返回 null，同时清除沿路遇到的任何已删除节点。调用者依赖于清除已删除节点的副作用。
     *
     * 重新启动发生在以节点 n 为中心的遍历步骤中，如果：
     *
     *   (1) 在读取 n 的下一个字段后，n 不再是假设的前驱 b 的当前后继，这意味着我们没有一致的 3 节点快照，因此无法取消后续遇到的已删除节点的链接。
     *
     *   (2) n 的值字段为 null，表示 n 已被删除，在这种情况下，我们帮助正在进行的结构删除，然后再重试。即使有某些情况下这样的取消链接不需要重新启动，但在这里不进行区分，因为这样做通常不会超过重新启动的成本。
     *
     *   (3) n 是标记或 n 的前驱的值字段为 null，表示（以及其他可能性）findPredecessor 返回了一个已删除的节点。我们不能取消该节点的链接，因为我们不知道它的前驱，所以依赖于另一个调用 findPredecessor 以注意到并返回某个更早的前驱，它会这样做。此检查仅在循环开始时严格需要（并且 b.value 检查根本不需要），但每次迭代都进行以帮助避免其他线程的争用，因为调用者将无法改变链接，因此无论如何都会重试。
     *
     * doPut、doRemove 和 findNear 中的遍历循环都包括这三种类型的检查。并且 findFirst 和 findLast 及其变体中也出现了专门的版本。它们不能轻松共享代码，因为每个版本使用的是以本地变量持有的字段读取，这些读取按执行顺序进行。
     *
     * @param key 键
     * @return 持有键的节点，或如果没有这样的节点则返回 null
     */
    private Node<K,V> findNode(Object key) {
        if (key == null)
            throw new NullPointerException(); // 不推迟错误
        Comparator<? super K> cmp = comparator;
        outer: for (;;) {
            for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
                Object v; int c;
                if (n == null)
                    break outer;
                Node<K,V> f = n.next;
                if (n != b.next)                // 不一致的读取
                    break;
                if ((v = n.value) == null) {    // n 已被删除
                    n.helpDelete(b, f);
                    break;
                }
                if (b.value == null || v == n)  // b 已被删除
                    break;
                if ((c = cpr(cmp, key, n.key)) == 0)
                    return n;
                if (c < 0)
                    break outer;
                b = n;
                n = f;
            }
        }
        return null;
    }

    /**
     * 获取键的值。几乎与 findNode 相同，但返回找到的值（以避免在重新读取时重试）。
     *
     * @param key 键
     * @return 值，或如果不存在则返回 null
     */
    private V doGet(Object key) {
        if (key == null)
            throw new NullPointerException();
        Comparator<? super K> cmp = comparator;
        outer: for (;;) {
            for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
                Object v; int c;
                if (n == null)
                    break outer;
                Node<K,V> f = n.next;
                if (n != b.next)                // 不一致的读取
                    break;
                if ((v = n.value) == null) {    // n 已被删除
                    n.helpDelete(b, f);
                    break;
                }
                if (b.value == null || v == n)  // b 已被删除
                    break;
                if ((c = cpr(cmp, key, n.key)) == 0) {
                    @SuppressWarnings("unchecked") V vv = (V)v;
                    return vv;
                }
                if (c < 0)
                    break outer;
                b = n;
                n = f;
            }
        }
        return null;
    }


                /* ---------------- Insertion -------------- */

    /**
     * 主要插入方法。如果元素不存在则添加，或者如果存在且 onlyIfAbsent 为 false 则替换值。
     * @param key 键
     * @param value 必须与键关联的值
     * @param onlyIfAbsent 如果已存在则不应插入
     * @return 旧值，如果新插入则为 null
     */
    private V doPut(K key, V value, boolean onlyIfAbsent) {
        Node<K,V> z;             // 添加的节点
        if (key == null)
            throw new NullPointerException();
        Comparator<? super K> cmp = comparator;
        outer: for (;;) {
            for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
                if (n != null) {
                    Object v; int c;
                    Node<K,V> f = n.next;
                    if (n != b.next)               // 读取不一致
                        break;
                    if ((v = n.value) == null) {   // n 被删除
                        n.helpDelete(b, f);
                        break;
                    }
                    if (b.value == null || v == n) // b 被删除
                        break;
                    if ((c = cpr(cmp, key, n.key)) > 0) {
                        b = n;
                        n = f;
                        continue;
                    }
                    if (c == 0) {
                        if (onlyIfAbsent || n.casValue(v, value)) {
                            @SuppressWarnings("unchecked") V vv = (V)v;
                            return vv;
                        }
                        break; // 如果在替换值的竞争中失败则重新开始
                    }
                    // 否则 c < 0; 继续
                }

                z = new Node<K,V>(key, value, n);
                if (!b.casNext(n, z))
                    break;         // 如果在附加到 b 的竞争中失败则重新开始
                break outer;
            }
        }

        int rnd = ThreadLocalRandom.nextSecondarySeed();
        if ((rnd & 0x80000001) == 0) { // 测试最高位和最低位
            int level = 1, max;
            while (((rnd >>>= 1) & 1) != 0)
                ++level;
            Index<K,V> idx = null;
            HeadIndex<K,V> h = head;
            if (level <= (max = h.level)) {
                for (int i = 1; i <= level; ++i)
                    idx = new Index<K,V>(z, idx, null);
            }
            else { // 尝试增加一层
                level = max + 1; // 持有在数组中并稍后选择要使用的
                @SuppressWarnings("unchecked")Index<K,V>[] idxs =
                    (Index<K,V>[])new Index<?,?>[level+1];
                for (int i = 1; i <= level; ++i)
                    idxs[i] = idx = new Index<K,V>(z, idx, null);
                for (;;) {
                    h = head;
                    int oldLevel = h.level;
                    if (level <= oldLevel) // 在增加层级的竞争中失败
                        break;
                    HeadIndex<K,V> newh = h;
                    Node<K,V> oldbase = h.node;
                    for (int j = oldLevel+1; j <= level; ++j)
                        newh = new HeadIndex<K,V>(oldbase, newh, idxs[j], j);
                    if (casHead(h, newh)) {
                        h = newh;
                        idx = idxs[level = oldLevel];
                        break;
                    }
                }
            }
            // 查找插入点并插入
            splice: for (int insertionLevel = level;;) {
                int j = h.level;
                for (Index<K,V> q = h, r = q.right, t = idx;;) {
                    if (q == null || t == null)
                        break splice;
                    if (r != null) {
                        Node<K,V> n = r.node;
                        // 在删除检查之前进行比较以避免需要重新检查
                        int c = cpr(cmp, key, n.key);
                        if (n.value == null) {
                            if (!q.unlink(r))
                                break;
                            r = q.right;
                            continue;
                        }
                        if (c > 0) {
                            q = r;
                            r = r.right;
                            continue;
                        }
                    }

                    if (j == insertionLevel) {
                        if (!q.link(r, t))
                            break; // 重新开始
                        if (t.node.value == null) {
                            findNode(key);
                            break splice;
                        }
                        if (--insertionLevel == 0)
                            break splice;
                    }

                    if (--j >= insertionLevel && j < level)
                        t = t.down;
                    q = q.down;
                    r = q.right;
                }
            }
        }
        return null;
    }

    /* ---------------- Deletion -------------- */

    /**
     * 主要删除方法。定位节点，将值置为 null，附加删除标记，解除前驱链接，移除关联的索引节点，并可能减少头索引层级。
     *
     * 索引节点通过调用 findPredecessor 简单地清除。这会解除路径上找到的已删除节点的索引链接，包括此节点的索引。这是无条件进行的。我们不能事先检查是否有索引节点，因为可能在初始搜索此节点时，一些或所有索引节点尚未插入，我们希望确保没有垃圾保留，所以必须调用以确保。
     *
     * @param key 键
     * @param value 如果非 null，则必须与键关联的值
     * @return 节点，如果未找到则为 null
     */
    final V doRemove(Object key, Object value) {
        if (key == null)
            throw new NullPointerException();
        Comparator<? super K> cmp = comparator;
        outer: for (;;) {
            for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
                Object v; int c;
                if (n == null)
                    break outer;
                Node<K,V> f = n.next;
                if (n != b.next)                    // 读取不一致
                    break;
                if ((v = n.value) == null) {        // n 被删除
                    n.helpDelete(b, f);
                    break;
                }
                if (b.value == null || v == n)      // b 被删除
                    break;
                if ((c = cpr(cmp, key, n.key)) < 0)
                    break outer;
                if (c > 0) {
                    b = n;
                    n = f;
                    continue;
                }
                if (value != null && !value.equals(v))
                    break outer;
                if (!n.casValue(v, null))
                    break;
                if (!n.appendMarker(f) || !b.casNext(n, f))
                    findNode(key);                  // 通过 findNode 重试
                else {
                    findPredecessor(key, cmp);      // 清除索引
                    if (head.right == null)
                        tryReduceLevel();
                }
                @SuppressWarnings("unchecked") V vv = (V)v;
                return vv;
            }
        }
        return null;
    }

    /**
     * 如果没有节点，则可能减少头层级。此方法（很少）可能会出错，即即使它们即将包含索引节点，层级也可能消失。这影响性能，但不影响正确性。为了最小化错误以及减少滞后，只有在最顶层的三个层级看起来为空时，层级才会减少一个。此外，如果移除的层级在 CAS 后看起来非空，我们会尝试快速更改回来，以免其他人注意到我们的错误！（这个技巧工作得很好，因为除非当前线程在第一次 CAS 之前立即停滞，否则此方法实际上永远不会出错，而在之后立即停滞的可能性非常低，所以会恢复。）
     *
     * 我们容忍这一切而不是任由层级增长，因为否则，即使是一个经历了大量插入和删除的小映射也会有很多层级，这会比偶尔不必要的减少更严重地减慢访问速度。
     */
    private void tryReduceLevel() {
        HeadIndex<K,V> h = head;
        HeadIndex<K,V> d;
        HeadIndex<K,V> e;
        if (h.level > 3 &&
            (d = (HeadIndex<K,V>)h.down) != null &&
            (e = (HeadIndex<K,V>)d.down) != null &&
            e.right == null &&
            d.right == null &&
            h.right == null &&
            casHead(h, d) && // 尝试设置
            h.right != null) // 重新检查
            casHead(d, h);   // 尝试撤回
    }

    /* ---------------- Finding and removing first element -------------- */

    /**
     * 获取第一个有效节点的 findNode 的专门变体。
     * @return 第一个节点，如果为空则为 null
     */
    final Node<K,V> findFirst() {
        for (Node<K,V> b, n;;) {
            if ((n = (b = head.node).next) == null)
                return null;
            if (n.value != null)
                return n;
            n.helpDelete(b, n.next);
        }
    }

    /**
     * 删除第一个条目；返回其快照。
     * @return 如果为空则为 null，否则为第一个条目的快照
     */
    private Map.Entry<K,V> doRemoveFirstEntry() {
        for (Node<K,V> b, n;;) {
            if ((n = (b = head.node).next) == null)
                return null;
            Node<K,V> f = n.next;
            if (n != b.next)
                continue;
            Object v = n.value;
            if (v == null) {
                n.helpDelete(b, f);
                continue;
            }
            if (!n.casValue(v, null))
                continue;
            if (!n.appendMarker(f) || !b.casNext(n, f))
                findFirst(); // 重试
            clearIndexToFirst();
            @SuppressWarnings("unchecked") V vv = (V)v;
            return new AbstractMap.SimpleImmutableEntry<K,V>(n.key, vv);
        }
    }

    /**
     * 清除与删除的第一个条目关联的索引节点。
     */
    private void clearIndexToFirst() {
        for (;;) {
            for (Index<K,V> q = head;;) {
                Index<K,V> r = q.right;
                if (r != null && r.indexesDeletedNode() && !q.unlink(r))
                    break;
                if ((q = q.down) == null) {
                    if (head.right == null)
                        tryReduceLevel();
                    return;
                }
            }
        }
    }

    /**
     * 删除最后一个条目；返回其快照。
     * doRemove 的专门变体。
     * @return 如果为空则为 null，否则为最后一个条目的快照
     */
    private Map.Entry<K,V> doRemoveLastEntry() {
        for (;;) {
            Node<K,V> b = findPredecessorOfLast();
            Node<K,V> n = b.next;
            if (n == null) {
                if (b.isBaseHeader())               // 为空
                    return null;
                else
                    continue; // b 的所有后继节点都被删除；重试
            }
            for (;;) {
                Node<K,V> f = n.next;
                if (n != b.next)                    // 读取不一致
                    break;
                Object v = n.value;
                if (v == null) {                    // n 被删除
                    n.helpDelete(b, f);
                    break;
                }
                if (b.value == null || v == n)      // b 被删除
                    break;
                if (f != null) {
                    b = n;
                    n = f;
                    continue;
                }
                if (!n.casValue(v, null))
                    break;
                K key = n.key;
                if (!n.appendMarker(f) || !b.casNext(n, f))
                    findNode(key);                  // 通过 findNode 重试
                else {                              // 清除索引
                    findPredecessor(key, comparator);
                    if (head.right == null)
                        tryReduceLevel();
                }
                @SuppressWarnings("unchecked") V vv = (V)v;
                return new AbstractMap.SimpleImmutableEntry<K,V>(key, vv);
            }
        }
    }

    /* ---------------- Finding and removing last element -------------- */

    /**
     * 获取最后一个有效节点的 find 的专门版本。
     * @return 最后一个节点，如果为空则为 null
     */
    final Node<K,V> findLast() {
        /*
         * 不能使用 findPredecessor 来遍历索引层级，因为这不使用比较。因此，两个层级的遍历合并在一起。
         */
        Index<K,V> q = head;
        for (;;) {
            Index<K,V> d, r;
            if ((r = q.right) != null) {
                if (r.indexesDeletedNode()) {
                    q.unlink(r);
                    q = head; // 重新开始
                }
                else
                    q = r;
            } else if ((d = q.down) != null) {
                q = d;
            } else {
                for (Node<K,V> b = q.node, n = b.next;;) {
                    if (n == null)
                        return b.isBaseHeader() ? null : b;
                    Node<K,V> f = n.next;            // 读取不一致
                    if (n != b.next)
                        break;
                    Object v = n.value;
                    if (v == null) {                 // n 被删除
                        n.helpDelete(b, f);
                        break;
                    }
                    if (b.value == null || v == n)      // b 被删除
                        break;
                    b = n;
                    n = f;
                }
                q = head; // 重新开始
            }
        }
    }

    /**
     * 获取最后一个有效节点的前驱的专门变体。在删除最后一个条目时需要。返回的节点的所有后继节点可能在返回时已被删除，这种情况下可以重试此方法。
     * @return 最后一个节点的可能前驱
     */
    private Node<K,V> findPredecessorOfLast() {
        for (;;) {
            for (Index<K,V> q = head;;) {
                Index<K,V> d, r;
                if ((r = q.right) != null) {
                    if (r.indexesDeletedNode()) {
                        q.unlink(r);
                        break;    // 必须重新开始
                    }
                    // 尽可能地向右前进而不越过
                    if (r.node.next != null) {
                        q = r;
                        continue;
                    }
                }
                if ((d = q.down) != null)
                    q = d;
                else
                    return q.node;
            }
        }
    }


                /* ---------------- Relational operations -------------- */

    // 控制值，作为 findNear 的参数 OR'ed

    private static final int EQ = 1;
    private static final int LT = 2;
    private static final int GT = 0; // 实际上检查为 !LT

    /**
     * 用于 ceiling, floor, lower, higher 方法的工具方法。
     * @param key 键
     * @param rel 关系 -- EQ, LT, GT 的 OR'ed 组合
     * @return 符合关系的最近节点，如果没有这样的节点则返回 null
     */
    final Node<K,V> findNear(K key, int rel, Comparator<? super K> cmp) {
        if (key == null)
            throw new NullPointerException();
        for (;;) {
            for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
                Object v;
                if (n == null)
                    return ((rel & LT) == 0 || b.isBaseHeader()) ? null : b;
                Node<K,V> f = n.next;
                if (n != b.next)                  // 不一致的读取
                    break;
                if ((v = n.value) == null) {      // n 被删除
                    n.helpDelete(b, f);
                    break;
                }
                if (b.value == null || v == n)      // b 被删除
                    break;
                int c = cpr(cmp, key, n.key);
                if ((c == 0 && (rel & EQ) != 0) ||
                    (c <  0 && (rel & LT) == 0))
                    return n;
                if ( c <= 0 && (rel & LT) != 0)
                    return b.isBaseHeader() ? null : b;
                b = n;
                n = f;
            }
        }
    }

    /**
     * 返回 findNear 结果的 SimpleImmutableEntry。
     * @param key 键
     * @param rel 关系 -- EQ, LT, GT 的 OR'ed 组合
     * @return 符合关系的 Entry，如果没有这样的 Entry 则返回 null
     */
    final AbstractMap.SimpleImmutableEntry<K,V> getNear(K key, int rel) {
        Comparator<? super K> cmp = comparator;
        for (;;) {
            Node<K,V> n = findNear(key, rel, cmp);
            if (n == null)
                return null;
            AbstractMap.SimpleImmutableEntry<K,V> e = n.createSnapshot();
            if (e != null)
                return e;
        }
    }

    /* ---------------- Constructors -------------- */

    /**
     * 构造一个新的、空的映射，根据键的 {@linkplain Comparable 自然顺序} 进行排序。
     */
    public ConcurrentSkipListMap() {
        this.comparator = null;
        initialize();
    }

    /**
     * 构造一个新的、空的映射，根据指定的比较器进行排序。
     *
     * @param comparator 将用于对映射进行排序的比较器。
     *        如果为 {@code null}，则使用键的 {@linkplain Comparable 自然顺序}。
     */
    public ConcurrentSkipListMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
        initialize();
    }

    /**
     * 构造一个新的映射，包含给定映射中的相同映射，根据键的 {@linkplain Comparable 自然顺序} 进行排序。
     *
     * @param  m 要放置在该映射中的映射
     * @throws ClassCastException 如果 m 中的键不是 {@link Comparable}，或者不是相互可比较的
     * @throws NullPointerException 如果指定的映射或其任何键或值为 null
     */
    public ConcurrentSkipListMap(Map<? extends K, ? extends V> m) {
        this.comparator = null;
        initialize();
        putAll(m);
    }

    /**
     * 构造一个新的映射，包含指定排序映射中的相同映射和相同的排序。
     *
     * @param m 要放置在该映射中的排序映射，以及用于对映射进行排序的比较器
     * @throws NullPointerException 如果指定的排序映射或其任何键或值为 null
     */
    public ConcurrentSkipListMap(SortedMap<K, ? extends V> m) {
        this.comparator = m.comparator();
        initialize();
        buildFromSorted(m);
    }

    /**
     * 返回此 {@code ConcurrentSkipListMap} 实例的浅拷贝。（键和值本身不会被克隆。）
     *
     * @return 该映射的浅拷贝
     */
    public ConcurrentSkipListMap<K,V> clone() {
        try {
            @SuppressWarnings("unchecked")
            ConcurrentSkipListMap<K,V> clone =
                (ConcurrentSkipListMap<K,V>) super.clone();
            clone.initialize();
            clone.buildFromSorted(this);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * 从给定排序映射的元素中初始化的简化批量插入。仅在构造函数或克隆方法中调用。
     */
    private void buildFromSorted(SortedMap<K, ? extends V> map) {
        if (map == null)
            throw new NullPointerException();

        HeadIndex<K,V> h = head;
        Node<K,V> basepred = h.node;

        // 跟踪每个级别的最右侧节点。使用 ArrayList 以避免承诺初始或最大级别。
        ArrayList<Index<K,V>> preds = new ArrayList<Index<K,V>>();

        // 初始化
        for (int i = 0; i <= h.level; ++i)
            preds.add(null);
        Index<K,V> q = h;
        for (int i = h.level; i > 0; --i) {
            preds.set(i, q);
            q = q.down;
        }

        Iterator<? extends Map.Entry<? extends K, ? extends V>> it =
            map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<? extends K, ? extends V> e = it.next();
            int rnd = ThreadLocalRandom.current().nextInt();
            int j = 0;
            if ((rnd & 0x80000001) == 0) {
                do {
                    ++j;
                } while (((rnd >>>= 1) & 1) != 0);
                if (j > h.level) j = h.level + 1;
            }
            K k = e.getKey();
            V v = e.getValue();
            if (k == null || v == null)
                throw new NullPointerException();
            Node<K,V> z = new Node<K,V>(k, v, null);
            basepred.next = z;
            basepred = z;
            if (j > 0) {
                Index<K,V> idx = null;
                for (int i = 1; i <= j; ++i) {
                    idx = new Index<K,V>(z, idx, null);
                    if (i > h.level)
                        h = new HeadIndex<K,V>(h.node, h, idx, i);

                    if (i < preds.size()) {
                        preds.get(i).right = idx;
                        preds.set(i, idx);
                    } else
                        preds.add(idx);
                }
            }
        }
        head = h;
    }

    /* ---------------- Serialization -------------- */

    /**
     * 将此映射保存到流中（即序列化）。
     *
     * @param s 流
     * @throws java.io.IOException 如果发生 I/O 错误
     * @serialData 键（Object）和值（Object）表示映射中的每个键值对，后跟
     * {@code null}。键值对按键的顺序（由比较器确定，或如果未提供比较器，则按键的自然顺序）发出。
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // 写出比较器和任何隐藏的内容
        s.defaultWriteObject();

        // 写出键和值（交替）
        for (Node<K,V> n = findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null) {
                s.writeObject(n.key);
                s.writeObject(v);
            }
        }
        s.writeObject(null);
    }

    /**
     * 从流中恢复此映射（即反序列化）。
     * @param s 流
     * @throws ClassNotFoundException 如果无法找到序列化对象的类
     * @throws java.io.IOException 如果发生 I/O 错误
     */
    @SuppressWarnings("unchecked")
    private void readObject(final java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // 读取比较器和任何隐藏的内容
        s.defaultReadObject();
        // 重置瞬态变量
        initialize();

        /*
         * 这几乎与 buildFromSorted 相同，但它是独立的，因为 readObject 调用不能很好地适应
         * buildFromSorted 所需的迭代器。可以这样做，但需要类型欺骗和/或创建适配器类。更简单的是直接适应代码。
         */

        HeadIndex<K,V> h = head;
        Node<K,V> basepred = h.node;
        ArrayList<Index<K,V>> preds = new ArrayList<Index<K,V>>();
        for (int i = 0; i <= h.level; ++i)
            preds.add(null);
        Index<K,V> q = h;
        for (int i = h.level; i > 0; --i) {
            preds.set(i, q);
            q = q.down;
        }

        for (;;) {
            Object k = s.readObject();
            if (k == null)
                break;
            Object v = s.readObject();
            if (v == null)
                throw new NullPointerException();
            K key = (K) k;
            V val = (V) v;
            int rnd = ThreadLocalRandom.current().nextInt();
            int j = 0;
            if ((rnd & 0x80000001) == 0) {
                do {
                    ++j;
                } while (((rnd >>>= 1) & 1) != 0);
                if (j > h.level) j = h.level + 1;
            }
            Node<K,V> z = new Node<K,V>(key, val, null);
            basepred.next = z;
            basepred = z;
            if (j > 0) {
                Index<K,V> idx = null;
                for (int i = 1; i <= j; ++i) {
                    idx = new Index<K,V>(z, idx, null);
                    if (i > h.level)
                        h = new HeadIndex<K,V>(h.node, h, idx, i);

                    if (i < preds.size()) {
                        preds.get(i).right = idx;
                        preds.set(i, idx);
                    } else
                        preds.add(idx);
                }
            }
        }
        head = h;
    }

    /* ------ Map API methods ------ */

    /**
     * 如果此映射包含指定键的映射，则返回 {@code true}。
     *
     * @param key 要测试其是否存在于映射中的键
     * @return 如果此映射包含指定键的映射，则返回 {@code true}
     * @throws ClassCastException 如果指定的键不能与映射中当前的键进行比较
     * @throws NullPointerException 如果指定的键为 null
     */
    public boolean containsKey(Object key) {
        return doGet(key) != null;
    }

    /**
     * 返回指定键所映射的值，如果此映射不包含该键的映射，则返回 {@code null}。
     *
     * <p>更正式地说，如果此映射包含从键 {@code k} 到值 {@code v} 的映射，使得 {@code key} 根据映射的排序与 {@code k} 比较相等，则此方法返回 {@code v}；否则返回 {@code null}。
     * （最多只能有一个这样的映射。）
     *
     * @throws ClassCastException 如果指定的键不能与映射中当前的键进行比较
     * @throws NullPointerException 如果指定的键为 null
     */
    public V get(Object key) {
        return doGet(key);
    }

    /**
     * 返回指定键所映射的值，如果此映射不包含该键的映射，则返回给定的 defaultValue。
     *
     * @param key 键
     * @param defaultValue 如果此映射不包含给定键的映射，则返回的值
     * @return 如果存在该键的映射，则返回该映射；否则返回 defaultValue
     * @throws NullPointerException 如果指定的键为 null
     * @since 1.8
     */
    public V getOrDefault(Object key, V defaultValue) {
        V v;
        return (v = doGet(key)) == null ? defaultValue : v;
    }

    /**
     * 将指定的值与指定的键关联到此映射中。如果映射之前包含该键的映射，则替换旧值。
     *
     * @param key 要与指定值关联的键
     * @param value 要与指定键关联的值
     * @return 之前与指定键关联的值，如果没有该键的映射，则返回 {@code null}
     * @throws ClassCastException 如果指定的键不能与映射中当前的键进行比较
     * @throws NullPointerException 如果指定的键或值为 null
     */
    public V put(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        return doPut(key, value, false);
    }

    /**
     * 如果存在，则从映射中移除指定键的映射。
     *
     * @param  key 要移除映射的键
     * @return 之前与指定键关联的值，如果没有该键的映射，则返回 {@code null}
     * @throws ClassCastException 如果指定的键不能与映射中当前的键进行比较
     * @throws NullPointerException 如果指定的键为 null
     */
    public V remove(Object key) {
        return doRemove(key, null);
    }

    /**
     * 如果此映射将一个或多个键映射到指定的值，则返回 {@code true}。此操作需要时间与映射大小成线性关系。此外，映射可能在执行此方法时发生变化，因此返回的结果可能不准确。
     *
     * @param value 要测试其是否存在于映射中的值
     * @return 如果存在映射到 {@code value} 的键，则返回 {@code true}；否则返回 {@code false}
     * @throws NullPointerException 如果指定的值为 null
     */
    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        for (Node<K,V> n = findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null && value.equals(v))
                return true;
        }
        return false;
    }

    /**
     * 返回此映射中的键值映射数量。如果此映射包含的元素多于 {@code Integer.MAX_VALUE}，则返回 {@code Integer.MAX_VALUE}。
     *
     * <p>请注意，与大多数集合不同，此方法 <em>不是</em> 常数时间操作。由于这些映射的异步性质，确定当前元素数量需要遍历所有元素以进行计数。此外，映射可能在执行此方法时发生变化，因此返回的结果可能不准确。因此，此方法在并发应用程序中通常不是很有用。
     *
     * @return 此映射中的元素数量
     */
    public int size() {
        long count = 0;
        for (Node<K,V> n = findFirst(); n != null; n = n.next) {
            if (n.getValidValue() != null)
                ++count;
        }
        return (count >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) count;
    }


                /**
     * 如果此映射不包含键值映射，则返回 {@code true}。
     * @return 如果此映射不包含键值映射，则返回 {@code true}
     */
    public boolean isEmpty() {
        return findFirst() == null;
    }

    /**
     * 从此映射中移除所有映射。
     */
    public void clear() {
        for (;;) {
            Node<K,V> b, n;
            HeadIndex<K,V> h = head, d = (HeadIndex<K,V>)h.down;
            if (d != null)
                casHead(h, d);            // 移除层级
            else if ((b = h.node) != null && (n = b.next) != null) {
                Node<K,V> f = n.next;     // 移除值
                if (n == b.next) {
                    Object v = n.value;
                    if (v == null)
                        n.helpDelete(b, f);
                    else if (n.casValue(v, null) && n.appendMarker(f))
                        b.casNext(n, f);
                }
            }
            else
                break;
        }
    }

    /**
     * 如果指定的键尚未与值关联，则尝试使用给定的映射函数计算其值
     * 并将其输入此映射，除非计算值为 {@code null}。函数
     * <em>不</em>保证仅在值不存在时原子地应用一次。
     *
     * @param key 与指定值关联的键
     * @param mappingFunction 计算值的函数
     * @return 与指定键关联的当前（现有或计算）值，或如果计算值为 null，则返回 null
     * @throws NullPointerException 如果指定的键为 null
     *         或 mappingFunction 为 null
     * @since 1.8
     */
    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        if (key == null || mappingFunction == null)
            throw new NullPointerException();
        V v, p, r;
        if ((v = doGet(key)) == null &&
            (r = mappingFunction.apply(key)) != null)
            v = (p = doPut(key, r, true)) == null ? r : p;
        return v;
    }

    /**
     * 如果指定了键的值存在，则尝试根据键及其当前映射值
     * 计算新的映射。函数<em>不</em>保证仅原子地应用一次。
     *
     * @param key 可能与值关联的键
     * @param remappingFunction 计算值的函数
     * @return 与指定键关联的新值，或无值时返回 null
     * @throws NullPointerException 如果指定的键为 null
     *         或 remappingFunction 为 null
     * @since 1.8
     */
    public V computeIfPresent(K key,
                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (key == null || remappingFunction == null)
            throw new NullPointerException();
        Node<K,V> n; Object v;
        while ((n = findNode(key)) != null) {
            if ((v = n.value) != null) {
                @SuppressWarnings("unchecked") V vv = (V) v;
                V r = remappingFunction.apply(key, vv);
                if (r != null) {
                    if (n.casValue(vv, r))
                        return r;
                }
                else if (doRemove(key, vv) != null)
                    break;
            }
        }
        return null;
    }

    /**
     * 尝试为指定的键及其当前映射值（或如果当前没有映射值，则为 {@code null}）
     * 计算映射。函数<em>不</em>保证仅原子地应用一次。
     *
     * @param key 与指定值关联的键
     * @param remappingFunction 计算值的函数
     * @return 与指定键关联的新值，或无值时返回 null
     * @throws NullPointerException 如果指定的键为 null
     *         或 remappingFunction 为 null
     * @since 1.8
     */
    public V compute(K key,
                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (key == null || remappingFunction == null)
            throw new NullPointerException();
        for (;;) {
            Node<K,V> n; Object v; V r;
            if ((n = findNode(key)) == null) {
                if ((r = remappingFunction.apply(key, null)) == null)
                    break;
                if (doPut(key, r, true) == null)
                    return r;
            }
            else if ((v = n.value) != null) {
                @SuppressWarnings("unchecked") V vv = (V) v;
                if ((r = remappingFunction.apply(key, vv)) != null) {
                    if (n.casValue(vv, r))
                        return r;
                }
                else if (doRemove(key, vv) != null)
                    break;
            }
        }
        return null;
    }

    /**
     * 如果指定的键尚未与值关联，则将其与给定值关联。否则，用给定的重新映射函数的结果
     * 替换值，或如果结果为 {@code null} 则移除。函数<em>不</em>
     * 保证仅原子地应用一次。
     *
     * @param key 与指定值关联的键
     * @param value 缺失时使用的值
     * @param remappingFunction 存在时重新计算值的函数
     * @return 与指定键关联的新值，或无值时返回 null
     * @throws NullPointerException 如果指定的键或值为 null
     *         或 remappingFunction 为 null
     * @since 1.8
     */
    public V merge(K key, V value,
                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (key == null || value == null || remappingFunction == null)
            throw new NullPointerException();
        for (;;) {
            Node<K,V> n; Object v; V r;
            if ((n = findNode(key)) == null) {
                if (doPut(key, value, true) == null)
                    return value;
            }
            else if ((v = n.value) != null) {
                @SuppressWarnings("unchecked") V vv = (V) v;
                if ((r = remappingFunction.apply(vv, value)) != null) {
                    if (n.casValue(vv, r))
                        return r;
                }
                else if (doRemove(key, vv) != null)
                    return null;
            }
        }
    }

    /* ---------------- View methods -------------- */

    /*
     * 注意：视图的懒初始化工作，因为视图类是无状态/不可变的，所以即使创建了多个视图
     * （这很少发生）也不会影响正确性。即使如此，以下惯用法保守地确保如果方法创建了视图，
     * 则返回该方法创建的视图，而不是其他竞争线程创建的视图。
     */

    /**
     * 返回此映射中包含的键的 {@link NavigableSet} 视图。
     *
     * <p>该集合的迭代器按升序返回键。该集合的拆分器还报告 {@link Spliterator#CONCURRENT}，
     * {@link Spliterator#NONNULL}，{@link Spliterator#SORTED} 和 {@link Spliterator#ORDERED}，
     * 遇到顺序为升序键顺序。拆分器的比较器（参见
     * {@link java.util.Spliterator#getComparator()}) 如果映射的比较器（参见 {@link #comparator()}）
     * 为 {@code null}，则为 {@code null}。否则，拆分器的比较器与映射的比较器相同或施加相同的总顺序。
     *
     * <p>该集合由映射支持，因此映射中的更改会反映在集合中，反之亦然。该集合支持元素移除，这会从映射中移除
     * 相应的映射，通过 {@code Iterator.remove}，{@code Set.remove}，
     * {@code removeAll}，{@code retainAll} 和 {@code clear} 操作。它不支持 {@code add} 或 {@code addAll}
     * 操作。
     *
     * <p>该视图的迭代器和拆分器是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     *
     * <p>此方法等同于方法 {@code navigableKeySet}。
     *
     * @return 此映射中键的可导航集合视图
     */
    public NavigableSet<K> keySet() {
        KeySet<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet<K>(this));
    }

    public NavigableSet<K> navigableKeySet() {
        KeySet<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet<K>(this));
    }

    /**
     * 返回此映射中包含的值的 {@link Collection} 视图。
     * <p>该集合的迭代器按相应键的升序返回值。该集合的拆分器还报告 {@link Spliterator#CONCURRENT}，
     * {@link Spliterator#NONNULL} 和 {@link Spliterator#ORDERED}，遇到顺序为相应键的升序。
     *
     * <p>该集合由映射支持，因此映射中的更改会反映在集合中，反之亦然。该集合支持元素移除，这会从映射中移除
     * 相应的映射，通过 {@code Iterator.remove}，{@code Collection.remove}，
     * {@code removeAll}，{@code retainAll} 和 {@code clear} 操作。它不支持 {@code add} 或 {@code addAll}
     * 操作。
     *
     * <p>该视图的迭代器和拆分器是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     */
    public Collection<V> values() {
        Values<V> vs = values;
        return (vs != null) ? vs : (values = new Values<V>(this));
    }

    /**
     * 返回此映射中包含的映射的 {@link Set} 视图。
     *
     * <p>该集合的迭代器按升序键顺序返回条目。该集合的拆分器还报告 {@link Spliterator#CONCURRENT}，
     * {@link Spliterator#NONNULL}，{@link Spliterator#SORTED} 和 {@link Spliterator#ORDERED}，
     * 遇到顺序为升序键顺序。
     *
     * <p>该集合由映射支持，因此映射中的更改会反映在集合中，反之亦然。该集合支持元素移除，这会从映射中移除
     * 相应的映射，通过 {@code Iterator.remove}，{@code Set.remove}，
     * {@code removeAll}，{@code retainAll} 和 {@code clear} 操作。它不支持 {@code add} 或
     * {@code addAll} 操作。
     *
     * <p>该视图的迭代器和拆分器是
     * <a href="package-summary.html#Weakly"><i>弱一致的</i></a>。
     *
     * <p>由 {@code iterator} 或 {@code spliterator} 遍历的 {@code Map.Entry} 元素
     * <em>不</em>支持 {@code setValue} 操作。
     *
     * @return 按升序键顺序排序的此映射中包含的映射的集合视图
     */
    public Set<Map.Entry<K,V>> entrySet() {
        EntrySet<K,V> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet<K,V>(this));
    }

    public ConcurrentNavigableMap<K,V> descendingMap() {
        ConcurrentNavigableMap<K,V> dm = descendingMap;
        return (dm != null) ? dm : (descendingMap = new SubMap<K,V>
                                    (this, null, false, null, false, true));
    }

    public NavigableSet<K> descendingKeySet() {
        return descendingMap().navigableKeySet();
    }

    /* ---------------- AbstractMap Overrides -------------- */

    /**
     * 将指定对象与此映射进行相等性比较。如果给定对象也是映射且两个映射表示相同的映射，则返回 {@code true}。
     * 更正式地说，两个映射 {@code m1} 和 {@code m2} 表示相同的映射，如果
     * {@code m1.entrySet().equals(m2.entrySet())}。如果在执行此方法时任一映射被并发修改，
     * 则此操作可能返回误导性的结果。
     *
     * @param o 要与此映射进行相等性比较的对象
     * @return 如果指定对象等于此映射，则返回 {@code true}
     */
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Map))
            return false;
        Map<?,?> m = (Map<?,?>) o;
        try {
            for (Map.Entry<K,V> e : this.entrySet())
                if (! e.getValue().equals(m.get(e.getKey())))
                    return false;
            for (Map.Entry<?,?> e : m.entrySet()) {
                Object k = e.getKey();
                Object v = e.getValue();
                if (k == null || v == null || !v.equals(get(k)))
                    return false;
            }
            return true;
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    /* ------ ConcurrentMap API methods ------ */

    /**
     * {@inheritDoc}
     *
     * @return 与指定键关联的前一个值，或如果键没有映射值，则返回 {@code null}
     * @throws ClassCastException 如果指定的键不能与映射中的键进行比较
     * @throws NullPointerException 如果指定的键或值为 null
     */
    public V putIfAbsent(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        return doPut(key, value, true);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ClassCastException 如果指定的键不能与映射中的键进行比较
     * @throws NullPointerException 如果指定的键为 null
     */
    public boolean remove(Object key, Object value) {
        if (key == null)
            throw new NullPointerException();
        return value != null && doRemove(key, value) != null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ClassCastException 如果指定的键不能与映射中的键进行比较
     * @throws NullPointerException 如果任何参数为 null
     */
    public boolean replace(K key, V oldValue, V newValue) {
        if (key == null || oldValue == null || newValue == null)
            throw new NullPointerException();
        for (;;) {
            Node<K,V> n; Object v;
            if ((n = findNode(key)) == null)
                return false;
            if ((v = n.value) != null) {
                if (!oldValue.equals(v))
                    return false;
                if (n.casValue(v, newValue))
                    return true;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return 与指定键关联的前一个值，或如果键没有映射值，则返回 {@code null}
     * @throws ClassCastException 如果指定的键不能与映射中的键进行比较
     * @throws NullPointerException 如果指定的键或值为 null
     */
    public V replace(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException();
        for (;;) {
            Node<K,V> n; Object v;
            if ((n = findNode(key)) == null)
                return null;
            if ((v = n.value) != null && n.casValue(v, value)) {
                @SuppressWarnings("unchecked") V vv = (V)v;
                return vv;
            }
        }
    }


                /* ------ SortedMap API methods ------ */

    public Comparator<? super K> comparator() {
        return comparator;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public K firstKey() {
        Node<K,V> n = findFirst();
        if (n == null)
            throw new NoSuchElementException();
        return n.key;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public K lastKey() {
        Node<K,V> n = findLast();
        if (n == null)
            throw new NoSuchElementException();
        return n.key;
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code fromKey} 或 {@code toKey} 为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public ConcurrentNavigableMap<K,V> subMap(K fromKey,
                                              boolean fromInclusive,
                                              K toKey,
                                              boolean toInclusive) {
        if (fromKey == null || toKey == null)
            throw new NullPointerException();
        return new SubMap<K,V>
            (this, fromKey, fromInclusive, toKey, toInclusive, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code toKey} 为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public ConcurrentNavigableMap<K,V> headMap(K toKey,
                                               boolean inclusive) {
        if (toKey == null)
            throw new NullPointerException();
        return new SubMap<K,V>
            (this, null, false, toKey, inclusive, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code fromKey} 为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public ConcurrentNavigableMap<K,V> tailMap(K fromKey,
                                               boolean inclusive) {
        if (fromKey == null)
            throw new NullPointerException();
        return new SubMap<K,V>
            (this, fromKey, inclusive, null, false, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code fromKey} 或 {@code toKey} 为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public ConcurrentNavigableMap<K,V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code toKey} 为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public ConcurrentNavigableMap<K,V> headMap(K toKey) {
        return headMap(toKey, false);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果 {@code fromKey} 为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public ConcurrentNavigableMap<K,V> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

    /* ---------------- Relational operations -------------- */

    /**
     * 返回与给定键严格小于的最大键相关联的键值映射，如果没有这样的键，则返回 {@code null}。返回的条目不支持 {@code Entry.setValue} 方法。
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的键为 null
     */
    public Map.Entry<K,V> lowerEntry(K key) {
        return getNear(key, LT);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的键为 null
     */
    public K lowerKey(K key) {
        Node<K,V> n = findNear(key, LT, comparator);
        return (n == null) ? null : n.key;
    }

    /**
     * 返回与给定键小于或等于的最大键相关联的键值映射，如果没有这样的键，则返回 {@code null}。返回的条目不支持 {@code Entry.setValue} 方法。
     *
     * @param key the key
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的键为 null
     */
    public Map.Entry<K,V> floorEntry(K key) {
        return getNear(key, LT|EQ);
    }

    /**
     * @param key the key
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的键为 null
     */
    public K floorKey(K key) {
        Node<K,V> n = findNear(key, LT|EQ, comparator);
        return (n == null) ? null : n.key;
    }

    /**
     * 返回与给定键大于或等于的最小键相关联的键值映射，如果没有这样的条目，则返回 {@code null}。返回的条目不支持 {@code Entry.setValue} 方法。
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的键为 null
     */
    public Map.Entry<K,V> ceilingEntry(K key) {
        return getNear(key, GT|EQ);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的键为 null
     */
    public K ceilingKey(K key) {
        Node<K,V> n = findNear(key, GT|EQ, comparator);
        return (n == null) ? null : n.key;
    }

    /**
     * 返回与给定键严格大于的最小键相关联的键值映射，如果没有这样的键，则返回 {@code null}。返回的条目不支持 {@code Entry.setValue} 方法。
     *
     * @param key the key
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的键为 null
     */
    public Map.Entry<K,V> higherEntry(K key) {
        return getNear(key, GT);
    }

    /**
     * @param key the key
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的键为 null
     */
    public K higherKey(K key) {
        Node<K,V> n = findNear(key, GT, comparator);
        return (n == null) ? null : n.key;
    }

    /**
     * 返回与此映射中的最小键相关联的键值映射，如果映射为空，则返回 {@code null}。返回的条目不支持 {@code Entry.setValue} 方法。
     */
    public Map.Entry<K,V> firstEntry() {
        for (;;) {
            Node<K,V> n = findFirst();
            if (n == null)
                return null;
            AbstractMap.SimpleImmutableEntry<K,V> e = n.createSnapshot();
            if (e != null)
                return e;
        }
    }

    /**
     * 返回与此映射中的最大键相关联的键值映射，如果映射为空，则返回 {@code null}。返回的条目不支持 {@code Entry.setValue} 方法。
     */
    public Map.Entry<K,V> lastEntry() {
        for (;;) {
            Node<K,V> n = findLast();
            if (n == null)
                return null;
            AbstractMap.SimpleImmutableEntry<K,V> e = n.createSnapshot();
            if (e != null)
                return e;
        }
    }

    /**
     * 移除并返回与此映射中的最小键相关联的键值映射，如果映射为空，则返回 {@code null}。返回的条目不支持 {@code Entry.setValue} 方法。
     */
    public Map.Entry<K,V> pollFirstEntry() {
        return doRemoveFirstEntry();
    }

    /**
     * 移除并返回与此映射中的最大键相关联的键值映射，如果映射为空，则返回 {@code null}。返回的条目不支持 {@code Entry.setValue} 方法。
     */
    public Map.Entry<K,V> pollLastEntry() {
        return doRemoveLastEntry();
    }


    /* ---------------- Iterators -------------- */

    /**
     * 迭代器类的基础：
     */
    abstract class Iter<T> implements Iterator<T> {
        /** 由 next() 返回的最后一个节点 */
        Node<K,V> lastReturned;
        /** 从 next() 返回的下一个节点； */
        Node<K,V> next;
        /** 缓存下一个值字段以保持弱一致性 */
        V nextValue;

        /** 初始化整个范围的升序迭代器。 */
        Iter() {
            while ((next = findFirst()) != null) {
                Object x = next.value;
                if (x != null && x != next) {
                    @SuppressWarnings("unchecked") V vv = (V)x;
                    nextValue = vv;
                    break;
                }
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        /** 将 next 推进到更高的条目。 */
        final void advance() {
            if (next == null)
                throw new NoSuchElementException();
            lastReturned = next;
            while ((next = next.next) != null) {
                Object x = next.value;
                if (x != null && x != next) {
                    @SuppressWarnings("unchecked") V vv = (V)x;
                    nextValue = vv;
                    break;
                }
            }
        }

        public void remove() {
            Node<K,V> l = lastReturned;
            if (l == null)
                throw new IllegalStateException();
            // 从这里直接取消链接并不值得所有开销。使用 remove 足够快。
            ConcurrentSkipListMap.this.remove(l.key);
            lastReturned = null;
        }

    }

    final class ValueIterator extends Iter<V> {
        public V next() {
            V v = nextValue;
            advance();
            return v;
        }
    }

    final class KeyIterator extends Iter<K> {
        public K next() {
            Node<K,V> n = next;
            advance();
            return n.key;
        }
    }

    final class EntryIterator extends Iter<Map.Entry<K,V>> {
        public Map.Entry<K,V> next() {
            Node<K,V> n = next;
            V v = nextValue;
            advance();
            return new AbstractMap.SimpleImmutableEntry<K,V>(n.key, v);
        }
    }

    // 由 ConcurrentSkipListSet 等需要的迭代器工厂方法

    Iterator<K> keyIterator() {
        return new KeyIterator();
    }

    Iterator<V> valueIterator() {
        return new ValueIterator();
    }

    Iterator<Map.Entry<K,V>> entryIterator() {
        return new EntryIterator();
    }

    /* ---------------- View Classes -------------- */

    /*
     * 视图类是静态的，委托给 ConcurrentNavigableMap
     * 以允许 SubMaps 使用，这比需要为 Iterator 方法进行类型测试的丑陋要好。
     */

    static final <E> List<E> toList(Collection<E> c) {
        // 在这里使用 size() 会是一个次优化。
        ArrayList<E> list = new ArrayList<E>();
        for (E e : c)
            list.add(e);
        return list;
    }

    static final class KeySet<E>
            extends AbstractSet<E> implements NavigableSet<E> {
        final ConcurrentNavigableMap<E,?> m;
        KeySet(ConcurrentNavigableMap<E,?> map) { m = map; }
        public int size() { return m.size(); }
        public boolean isEmpty() { return m.isEmpty(); }
        public boolean contains(Object o) { return m.containsKey(o); }
        public boolean remove(Object o) { return m.remove(o) != null; }
        public void clear() { m.clear(); }
        public E lower(E e) { return m.lowerKey(e); }
        public E floor(E e) { return m.floorKey(e); }
        public E ceiling(E e) { return m.ceilingKey(e); }
        public E higher(E e) { return m.higherKey(e); }
        public Comparator<? super E> comparator() { return m.comparator(); }
        public E first() { return m.firstKey(); }
        public E last() { return m.lastKey(); }
        public E pollFirst() {
            Map.Entry<E,?> e = m.pollFirstEntry();
            return (e == null) ? null : e.getKey();
        }
        public E pollLast() {
            Map.Entry<E,?> e = m.pollLastEntry();
            return (e == null) ? null : e.getKey();
        }
        @SuppressWarnings("unchecked")
        public Iterator<E> iterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<E,Object>)m).keyIterator();
            else
                return ((ConcurrentSkipListMap.SubMap<E,Object>)m).keyIterator();
        }
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Set))
                return false;
            Collection<?> c = (Collection<?>) o;
            try {
                return containsAll(c) && c.containsAll(this);
            } catch (ClassCastException unused) {
                return false;
            } catch (NullPointerException unused) {
                return false;
            }
        }
        public Object[] toArray()     { return toList(this).toArray();  }
        public <T> T[] toArray(T[] a) { return toList(this).toArray(a); }
        public Iterator<E> descendingIterator() {
            return descendingSet().iterator();
        }
        public NavigableSet<E> subSet(E fromElement,
                                      boolean fromInclusive,
                                      E toElement,
                                      boolean toInclusive) {
            return new KeySet<E>(m.subMap(fromElement, fromInclusive,
                                          toElement,   toInclusive));
        }
        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return new KeySet<E>(m.headMap(toElement, inclusive));
        }
        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return new KeySet<E>(m.tailMap(fromElement, inclusive));
        }
        public NavigableSet<E> subSet(E fromElement, E toElement) {
            return subSet(fromElement, true, toElement, false);
        }
        public NavigableSet<E> headSet(E toElement) {
            return headSet(toElement, false);
        }
        public NavigableSet<E> tailSet(E fromElement) {
            return tailSet(fromElement, true);
        }
        public NavigableSet<E> descendingSet() {
            return new KeySet<E>(m.descendingMap());
        }
        @SuppressWarnings("unchecked")
        public Spliterator<E> spliterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<E,?>)m).keySpliterator();
            else
                return (Spliterator<E>)((SubMap<E,?>)m).keyIterator();
        }
    }

    static final class Values<E> extends AbstractCollection<E> {
        final ConcurrentNavigableMap<?, E> m;
        Values(ConcurrentNavigableMap<?, E> map) {
            m = map;
        }
        @SuppressWarnings("unchecked")
        public Iterator<E> iterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<?,E>)m).valueIterator();
            else
                return ((SubMap<?,E>)m).valueIterator();
        }
        public boolean isEmpty() {
            return m.isEmpty();
        }
        public int size() {
            return m.size();
        }
        public boolean contains(Object o) {
            return m.containsValue(o);
        }
        public void clear() {
            m.clear();
        }
        public Object[] toArray()     { return toList(this).toArray();  }
        public <T> T[] toArray(T[] a) { return toList(this).toArray(a); }
        @SuppressWarnings("unchecked")
        public Spliterator<E> spliterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<?,E>)m).valueSpliterator();
            else
                return (Spliterator<E>)((SubMap<?,E>)m).valueIterator();
        }
    }


                static final class EntrySet<K1,V1> extends AbstractSet<Map.Entry<K1,V1>> {
        final ConcurrentNavigableMap<K1, V1> m;
        EntrySet(ConcurrentNavigableMap<K1, V1> map) {
            m = map;
        }
        @SuppressWarnings("unchecked")
        public Iterator<Map.Entry<K1,V1>> iterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<K1,V1>)m).entryIterator();
            else
                return ((SubMap<K1,V1>)m).entryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            V1 v = m.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            return m.remove(e.getKey(),
                            e.getValue());
        }
        public boolean isEmpty() {
            return m.isEmpty();
        }
        public int size() {
            return m.size();
        }
        public void clear() {
            m.clear();
        }
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Set))
                return false;
            Collection<?> c = (Collection<?>) o;
            try {
                return containsAll(c) && c.containsAll(this);
            } catch (ClassCastException unused) {
                return false;
            } catch (NullPointerException unused) {
                return false;
            }
        }
        public Object[] toArray()     { return toList(this).toArray();  }
        public <T> T[] toArray(T[] a) { return toList(this).toArray(a); }
        @SuppressWarnings("unchecked")
        public Spliterator<Map.Entry<K1,V1>> spliterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<K1,V1>)m).entrySpliterator();
            else
                return (Spliterator<Map.Entry<K1,V1>>)
                    ((SubMap<K1,V1>)m).entryIterator();
        }
    }

    /**
     * 由 {@link ConcurrentSkipListMap} 子映射操作返回的子映射表示其底层映射的子范围。
     * 该类的实例支持其底层映射的所有方法，不同之处在于范围之外的映射被忽略，尝试添加范围之外的映射会导致 {@link IllegalArgumentException}。
     * 该类的实例仅使用其底层映射的 {@code subMap}、{@code headMap} 和 {@code tailMap} 方法构造。
     *
     * @serial include
     */
    static final class SubMap<K,V> extends AbstractMap<K,V>
        implements ConcurrentNavigableMap<K,V>, Cloneable, Serializable {
        private static final long serialVersionUID = -7647078645895051609L;

        /** 底层映射 */
        private final ConcurrentSkipListMap<K,V> m;
        /** 下限键，或从开始时为 null */
        private final K lo;
        /** 上限键，或到结束时为 null */
        private final K hi;
        /** lo 的包含标志 */
        private final boolean loInclusive;
        /** hi 的包含标志 */
        private final boolean hiInclusive;
        /** 方向 */
        private final boolean isDescending;

        // 惰性初始化视图持有者
        private transient KeySet<K> keySetView;
        private transient Set<Map.Entry<K,V>> entrySetView;
        private transient Collection<V> valuesView;

        /**
         * 创建一个新的子映射，初始化所有字段。
         */
        SubMap(ConcurrentSkipListMap<K,V> map,
               K fromKey, boolean fromInclusive,
               K toKey, boolean toInclusive,
               boolean isDescending) {
            Comparator<? super K> cmp = map.comparator;
            if (fromKey != null && toKey != null &&
                cpr(cmp, fromKey, toKey) > 0)
                throw new IllegalArgumentException("不一致的范围");
            this.m = map;
            this.lo = fromKey;
            this.hi = toKey;
            this.loInclusive = fromInclusive;
            this.hiInclusive = toInclusive;
            this.isDescending = isDescending;
        }

        /* ----------------  工具方法 -------------- */

        boolean tooLow(Object key, Comparator<? super K> cmp) {
            int c;
            return (lo != null && ((c = cpr(cmp, key, lo)) < 0 ||
                                   (c == 0 && !loInclusive)));
        }

        boolean tooHigh(Object key, Comparator<? super K> cmp) {
            int c;
            return (hi != null && ((c = cpr(cmp, key, hi)) > 0 ||
                                   (c == 0 && !hiInclusive)));
        }

        boolean inBounds(Object key, Comparator<? super K> cmp) {
            return !tooLow(key, cmp) && !tooHigh(key, cmp);
        }

        void checkKeyBounds(K key, Comparator<? super K> cmp) {
            if (key == null)
                throw new NullPointerException();
            if (!inBounds(key, cmp))
                throw new IllegalArgumentException("键超出范围");
        }

        /**
         * 如果节点键小于范围的上限，则返回 true。
         */
        boolean isBeforeEnd(ConcurrentSkipListMap.Node<K,V> n,
                            Comparator<? super K> cmp) {
            if (n == null)
                return false;
            if (hi == null)
                return true;
            K k = n.key;
            if (k == null) // 跳过标记和头节点
                return true;
            int c = cpr(cmp, k, hi);
            if (c > 0 || (c == 0 && !hiInclusive))
                return false;
            return true;
        }

        /**
         * 返回最低节点。此节点可能不在范围内，因此大多数使用需要检查边界。
         */
        ConcurrentSkipListMap.Node<K,V> loNode(Comparator<? super K> cmp) {
            if (lo == null)
                return m.findFirst();
            else if (loInclusive)
                return m.findNear(lo, GT|EQ, cmp);
            else
                return m.findNear(lo, GT, cmp);
        }

        /**
         * 返回最高节点。此节点可能不在范围内，因此大多数使用需要检查边界。
         */
        ConcurrentSkipListMap.Node<K,V> hiNode(Comparator<? super K> cmp) {
            if (hi == null)
                return m.findLast();
            else if (hiInclusive)
                return m.findNear(hi, LT|EQ, cmp);
            else
                return m.findNear(hi, LT, cmp);
        }

        /**
         * 返回最低绝对键（忽略方向性）。
         */
        K lowestKey() {
            Comparator<? super K> cmp = m.comparator;
            ConcurrentSkipListMap.Node<K,V> n = loNode(cmp);
            if (isBeforeEnd(n, cmp))
                return n.key;
            else
                throw new NoSuchElementException();
        }

        /**
         * 返回最高绝对键（忽略方向性）。
         */
        K highestKey() {
            Comparator<? super K> cmp = m.comparator;
            ConcurrentSkipListMap.Node<K,V> n = hiNode(cmp);
            if (n != null) {
                K last = n.key;
                if (inBounds(last, cmp))
                    return last;
            }
            throw new NoSuchElementException();
        }

        Map.Entry<K,V> lowestEntry() {
            Comparator<? super K> cmp = m.comparator;
            for (;;) {
                ConcurrentSkipListMap.Node<K,V> n = loNode(cmp);
                if (!isBeforeEnd(n, cmp))
                    return null;
                Map.Entry<K,V> e = n.createSnapshot();
                if (e != null)
                    return e;
            }
        }

        Map.Entry<K,V> highestEntry() {
            Comparator<? super K> cmp = m.comparator;
            for (;;) {
                ConcurrentSkipListMap.Node<K,V> n = hiNode(cmp);
                if (n == null || !inBounds(n.key, cmp))
                    return null;
                Map.Entry<K,V> e = n.createSnapshot();
                if (e != null)
                    return e;
            }
        }

        Map.Entry<K,V> removeLowest() {
            Comparator<? super K> cmp = m.comparator;
            for (;;) {
                Node<K,V> n = loNode(cmp);
                if (n == null)
                    return null;
                K k = n.key;
                if (!inBounds(k, cmp))
                    return null;
                V v = m.doRemove(k, null);
                if (v != null)
                    return new AbstractMap.SimpleImmutableEntry<K,V>(k, v);
            }
        }

        Map.Entry<K,V> removeHighest() {
            Comparator<? super K> cmp = m.comparator;
            for (;;) {
                Node<K,V> n = hiNode(cmp);
                if (n == null)
                    return null;
                K k = n.key;
                if (!inBounds(k, cmp))
                    return null;
                V v = m.doRemove(k, null);
                if (v != null)
                    return new AbstractMap.SimpleImmutableEntry<K,V>(k, v);
            }
        }

        /**
         * ConcurrentSkipListMap.getNearEntry 的子映射版本
         */
        Map.Entry<K,V> getNearEntry(K key, int rel) {
            Comparator<? super K> cmp = m.comparator;
            if (isDescending) { // 调整方向的关系
                if ((rel & LT) == 0)
                    rel |= LT;
                else
                    rel &= ~LT;
            }
            if (tooLow(key, cmp))
                return ((rel & LT) != 0) ? null : lowestEntry();
            if (tooHigh(key, cmp))
                return ((rel & LT) != 0) ? highestEntry() : null;
            for (;;) {
                Node<K,V> n = m.findNear(key, rel, cmp);
                if (n == null || !inBounds(n.key, cmp))
                    return null;
                K k = n.key;
                V v = n.getValidValue();
                if (v != null)
                    return new AbstractMap.SimpleImmutableEntry<K,V>(k, v);
            }
        }

        // 几乎与 getNearEntry 相同，但针对键
        K getNearKey(K key, int rel) {
            Comparator<? super K> cmp = m.comparator;
            if (isDescending) { // 调整方向的关系
                if ((rel & LT) == 0)
                    rel |= LT;
                else
                    rel &= ~LT;
            }
            if (tooLow(key, cmp)) {
                if ((rel & LT) == 0) {
                    ConcurrentSkipListMap.Node<K,V> n = loNode(cmp);
                    if (isBeforeEnd(n, cmp))
                        return n.key;
                }
                return null;
            }
            if (tooHigh(key, cmp)) {
                if ((rel & LT) != 0) {
                    ConcurrentSkipListMap.Node<K,V> n = hiNode(cmp);
                    if (n != null) {
                        K last = n.key;
                        if (inBounds(last, cmp))
                            return last;
                    }
                }
                return null;
            }
            for (;;) {
                Node<K,V> n = m.findNear(key, rel, cmp);
                if (n == null || !inBounds(n.key, cmp))
                    return null;
                K k = n.key;
                V v = n.getValidValue();
                if (v != null)
                    return k;
            }
        }

        /* ----------------  Map API 方法 -------------- */

        public boolean containsKey(Object key) {
            if (key == null) throw new NullPointerException();
            return inBounds(key, m.comparator) && m.containsKey(key);
        }

        public V get(Object key) {
            if (key == null) throw new NullPointerException();
            return (!inBounds(key, m.comparator)) ? null : m.get(key);
        }

        public V put(K key, V value) {
            checkKeyBounds(key, m.comparator);
            return m.put(key, value);
        }

        public V remove(Object key) {
            return (!inBounds(key, m.comparator)) ? null : m.remove(key);
        }

        public int size() {
            Comparator<? super K> cmp = m.comparator;
            long count = 0;
            for (ConcurrentSkipListMap.Node<K,V> n = loNode(cmp);
                 isBeforeEnd(n, cmp);
                 n = n.next) {
                if (n.getValidValue() != null)
                    ++count;
            }
            return count >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)count;
        }

        public boolean isEmpty() {
            Comparator<? super K> cmp = m.comparator;
            return !isBeforeEnd(loNode(cmp), cmp);
        }

        public boolean containsValue(Object value) {
            if (value == null)
                throw new NullPointerException();
            Comparator<? super K> cmp = m.comparator;
            for (ConcurrentSkipListMap.Node<K,V> n = loNode(cmp);
                 isBeforeEnd(n, cmp);
                 n = n.next) {
                V v = n.getValidValue();
                if (v != null && value.equals(v))
                    return true;
            }
            return false;
        }

        public void clear() {
            Comparator<? super K> cmp = m.comparator;
            for (ConcurrentSkipListMap.Node<K,V> n = loNode(cmp);
                 isBeforeEnd(n, cmp);
                 n = n.next) {
                if (n.getValidValue() != null)
                    m.remove(n.key);
            }
        }

        /* ----------------  ConcurrentMap API 方法 -------------- */

        public V putIfAbsent(K key, V value) {
            checkKeyBounds(key, m.comparator);
            return m.putIfAbsent(key, value);
        }

        public boolean remove(Object key, Object value) {
            return inBounds(key, m.comparator) && m.remove(key, value);
        }

        public boolean replace(K key, V oldValue, V newValue) {
            checkKeyBounds(key, m.comparator);
            return m.replace(key, oldValue, newValue);
        }

        public V replace(K key, V value) {
            checkKeyBounds(key, m.comparator);
            return m.replace(key, value);
        }

        /* ----------------  SortedMap API 方法 -------------- */

        public Comparator<? super K> comparator() {
            Comparator<? super K> cmp = m.comparator();
            if (isDescending)
                return Collections.reverseOrder(cmp);
            else
                return cmp;
        }

        /**
         * 创建子映射的工具方法，其中给定的边界覆盖无界的（null）边界和/或检查有界的边界。
         */
        SubMap<K,V> newSubMap(K fromKey, boolean fromInclusive,
                              K toKey, boolean toInclusive) {
            Comparator<? super K> cmp = m.comparator;
            if (isDescending) { // 翻转方向
                K tk = fromKey;
                fromKey = toKey;
                toKey = tk;
                boolean ti = fromInclusive;
                fromInclusive = toInclusive;
                toInclusive = ti;
            }
            if (lo != null) {
                if (fromKey == null) {
                    fromKey = lo;
                    fromInclusive = loInclusive;
                }
                else {
                    int c = cpr(cmp, fromKey, lo);
                    if (c < 0 || (c == 0 && !loInclusive && fromInclusive))
                        throw new IllegalArgumentException("键超出范围");
                }
            }
            if (hi != null) {
                if (toKey == null) {
                    toKey = hi;
                    toInclusive = hiInclusive;
                }
                else {
                    int c = cpr(cmp, toKey, hi);
                    if (c > 0 || (c == 0 && !hiInclusive && toInclusive))
                        throw new IllegalArgumentException("键超出范围");
                }
            }
            return new SubMap<K,V>(m, fromKey, fromInclusive,
                                   toKey, toInclusive, isDescending);
        }


        /* 
            Copyright (c) 1996, 1999, ...
         */
        public SubMap<K,V> subMap(K fromKey, boolean fromInclusive,
                                  K toKey, boolean toInclusive) {
            if (fromKey == null || toKey == null)
                throw new NullPointerException();
            return newSubMap(fromKey, fromInclusive, toKey, toInclusive);
        }

        public SubMap<K,V> headMap(K toKey, boolean inclusive) {
            if (toKey == null)
                throw new NullPointerException();
            return newSubMap(null, false, toKey, inclusive);
        }

        public SubMap<K,V> tailMap(K fromKey, boolean inclusive) {
            if (fromKey == null)
                throw new NullPointerException();
            return newSubMap(fromKey, inclusive, null, false);
        }

        public SubMap<K,V> subMap(K fromKey, K toKey) {
            return subMap(fromKey, true, toKey, false);
        }

        public SubMap<K,V> headMap(K toKey) {
            return headMap(toKey, false);
        }

        public SubMap<K,V> tailMap(K fromKey) {
            return tailMap(fromKey, true);
        }

        public SubMap<K,V> descendingMap() {
            return new SubMap<K,V>(m, lo, loInclusive,
                                   hi, hiInclusive, !isDescending);
        }

        /* ----------------  关系方法 -------------- */

        public Map.Entry<K,V> ceilingEntry(K key) {
            return getNearEntry(key, GT|EQ);
        }

        public K ceilingKey(K key) {
            return getNearKey(key, GT|EQ);
        }

        public Map.Entry<K,V> lowerEntry(K key) {
            return getNearEntry(key, LT);
        }

        public K lowerKey(K key) {
            return getNearKey(key, LT);
        }

        public Map.Entry<K,V> floorEntry(K key) {
            return getNearEntry(key, LT|EQ);
        }

        public K floorKey(K key) {
            return getNearKey(key, LT|EQ);
        }

        public Map.Entry<K,V> higherEntry(K key) {
            return getNearEntry(key, GT);
        }

        public K higherKey(K key) {
            return getNearKey(key, GT);
        }

        public K firstKey() {
            return isDescending ? highestKey() : lowestKey();
        }

        public K lastKey() {
            return isDescending ? lowestKey() : highestKey();
        }

        public Map.Entry<K,V> firstEntry() {
            return isDescending ? highestEntry() : lowestEntry();
        }

        public Map.Entry<K,V> lastEntry() {
            return isDescending ? lowestEntry() : highestEntry();
        }

        public Map.Entry<K,V> pollFirstEntry() {
            return isDescending ? removeHighest() : removeLowest();
        }

        public Map.Entry<K,V> pollLastEntry() {
            return isDescending ? removeLowest() : removeHighest();
        }

        /* ---------------- 子映射视图 -------------- */

        public NavigableSet<K> keySet() {
            KeySet<K> ks = keySetView;
            return (ks != null) ? ks : (keySetView = new KeySet<K>(this));
        }

        public NavigableSet<K> navigableKeySet() {
            KeySet<K> ks = keySetView;
            return (ks != null) ? ks : (keySetView = new KeySet<K>(this));
        }

        public Collection<V> values() {
            Collection<V> vs = valuesView;
            return (vs != null) ? vs : (valuesView = new Values<V>(this));
        }

        public Set<Map.Entry<K,V>> entrySet() {
            Set<Map.Entry<K,V>> es = entrySetView;
            return (es != null) ? es : (entrySetView = new EntrySet<K,V>(this));
        }

        public NavigableSet<K> descendingKeySet() {
            return descendingMap().navigableKeySet();
        }

        Iterator<K> keyIterator() {
            return new SubMapKeyIterator();
        }

        Iterator<V> valueIterator() {
            return new SubMapValueIterator();
        }

        Iterator<Map.Entry<K,V>> entryIterator() {
            return new SubMapEntryIterator();
        }

        /**
         * 主要迭代类的变体，用于遍历子映射。
         * 也作为视图的备用 Spliterator
         */
        abstract class SubMapIter<T> implements Iterator<T>, Spliterator<T> {
            /** 由 next() 返回的最后一个节点 */
            Node<K,V> lastReturned;
            /** 从 next() 返回的下一个节点； */
            Node<K,V> next;
            /** 缓存 next 值字段以保持弱一致性 */
            V nextValue;

            SubMapIter() {
                Comparator<? super K> cmp = m.comparator;
                for (;;) {
                    next = isDescending ? hiNode(cmp) : loNode(cmp);
                    if (next == null)
                        break;
                    Object x = next.value;
                    if (x != null && x != next) {
                        if (! inBounds(next.key, cmp))
                            next = null;
                        else {
                            @SuppressWarnings("unchecked") V vv = (V)x;
                            nextValue = vv;
                        }
                        break;
                    }
                }
            }

            public final boolean hasNext() {
                return next != null;
            }

            final void advance() {
                if (next == null)
                    throw new NoSuchElementException();
                lastReturned = next;
                if (isDescending)
                    descend();
                else
                    ascend();
            }

            private void ascend() {
                Comparator<? super K> cmp = m.comparator;
                for (;;) {
                    next = next.next;
                    if (next == null)
                        break;
                    Object x = next.value;
                    if (x != null && x != next) {
                        if (tooHigh(next.key, cmp))
                            next = null;
                        else {
                            @SuppressWarnings("unchecked") V vv = (V)x;
                            nextValue = vv;
                        }
                        break;
                    }
                }
            }

            private void descend() {
                Comparator<? super K> cmp = m.comparator;
                for (;;) {
                    next = m.findNear(lastReturned.key, LT, cmp);
                    if (next == null)
                        break;
                    Object x = next.value;
                    if (x != null && x != next) {
                        if (tooLow(next.key, cmp))
                            next = null;
                        else {
                            @SuppressWarnings("unchecked") V vv = (V)x;
                            nextValue = vv;
                        }
                        break;
                    }
                }
            }

            public void remove() {
                Node<K,V> l = lastReturned;
                if (l == null)
                    throw new IllegalStateException();
                m.remove(l.key);
                lastReturned = null;
            }

            public Spliterator<T> trySplit() {
                return null;
            }

            public boolean tryAdvance(Consumer<? super T> action) {
                if (hasNext()) {
                    action.accept(next());
                    return true;
                }
                return false;
            }

            public void forEachRemaining(Consumer<? super T> action) {
                while (hasNext())
                    action.accept(next());
            }

            public long estimateSize() {
                return Long.MAX_VALUE;
            }

        }

        final class SubMapValueIterator extends SubMapIter<V> {
            public V next() {
                V v = nextValue;
                advance();
                return v;
            }
            public int characteristics() {
                return 0;
            }
        }

        final class SubMapKeyIterator extends SubMapIter<K> {
            public K next() {
                Node<K,V> n = next;
                advance();
                return n.key;
            }
            public int characteristics() {
                return Spliterator.DISTINCT | Spliterator.ORDERED |
                    Spliterator.SORTED;
            }
            public final Comparator<? super K> getComparator() {
                return SubMap.this.comparator();
            }
        }

        final class SubMapEntryIterator extends SubMapIter<Map.Entry<K,V>> {
            public Map.Entry<K,V> next() {
                Node<K,V> n = next;
                V v = nextValue;
                advance();
                return new AbstractMap.SimpleImmutableEntry<K,V>(n.key, v);
            }
            public int characteristics() {
                return Spliterator.DISTINCT;
            }
        }
    }

    // 默认 Map 方法覆盖

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null) throw new NullPointerException();
        V v;
        for (Node<K,V> n = findFirst(); n != null; n = n.next) {
            if ((v = n.getValidValue()) != null)
                action.accept(n.key, v);
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null) throw new NullPointerException();
        V v;
        for (Node<K,V> n = findFirst(); n != null; n = n.next) {
            while ((v = n.getValidValue()) != null) {
                V r = function.apply(n.key, v);
                if (r == null) throw new NullPointerException();
                if (n.casValue(v, r))
                    break;
            }
        }
    }

    /**
     * 提供 Spliterators 共同结构的基类。
     * （尽管并不是所有的功能都那么常见；与通常的视图类一样，键、值和条目子类的细节以不值得抽象出来的方式变化。）
     *
     * 基本的拆分策略是从顶层递归下降，逐行下降，当拆分或行结束时下降到下一行。拆分次数的控制依赖于一些统计估计：当在跳表中向前或向下推进时，剩余元素的预期数量减少约 25%。为了使这一观察有用，我们需要知道初始大小，但我们不知道。但我们可以使用 Integer.MAX_VALUE，这样在拆分时不会过早归零。
     */
    abstract static class CSLMSpliterator<K,V> {
        final Comparator<? super K> comparator;
        final K fence;     // 键的独占上限，或 null 表示到末尾
        Index<K,V> row;    // 要拆分的级别
        Node<K,V> current; // 当前遍历节点；在原点初始化
        int est;           // 伪大小估计
        CSLMSpliterator(Comparator<? super K> comparator, Index<K,V> row,
                        Node<K,V> origin, K fence, int est) {
            this.comparator = comparator; this.row = row;
            this.current = origin; this.fence = fence; this.est = est;
        }

        public final long estimateSize() { return (long)est; }
    }

    static final class KeySpliterator<K,V> extends CSLMSpliterator<K,V>
        implements Spliterator<K> {
        KeySpliterator(Comparator<? super K> comparator, Index<K,V> row,
                       Node<K,V> origin, K fence, int est) {
            super(comparator, row, origin, fence, est);
        }

        public Spliterator<K> trySplit() {
            Node<K,V> e; K ek;
            Comparator<? super K> cmp = comparator;
            K f = fence;
            if ((e = current) != null && (ek = e.key) != null) {
                for (Index<K,V> q = row; q != null; q = row = q.down) {
                    Index<K,V> s; Node<K,V> b, n; K sk;
                    if ((s = q.right) != null && (b = s.node) != null &&
                        (n = b.next) != null && n.value != null &&
                        (sk = n.key) != null && cpr(cmp, sk, ek) > 0 &&
                        (f == null || cpr(cmp, sk, f) < 0)) {
                        current = n;
                        Index<K,V> r = q.down;
                        row = (s.right != null) ? s : s.down;
                        est -= est >>> 2;
                        return new KeySpliterator<K,V>(cmp, r, e, sk, est);
                    }
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null) throw new NullPointerException();
            Comparator<? super K> cmp = comparator;
            K f = fence;
            Node<K,V> e = current;
            current = null;
            for (; e != null; e = e.next) {
                K k; Object v;
                if ((k = e.key) != null && f != null && cpr(cmp, f, k) <= 0)
                    break;
                if ((v = e.value) != null && v != e)
                    action.accept(k);
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null) throw new NullPointerException();
            Comparator<? super K> cmp = comparator;
            K f = fence;
            Node<K,V> e = current;
            for (; e != null; e = e.next) {
                K k; Object v;
                if ((k = e.key) != null && f != null && cpr(cmp, f, k) <= 0) {
                    e = null;
                    break;
                }
                if ((v = e.value) != null && v != e) {
                    current = e.next;
                    action.accept(k);
                    return true;
                }
            }
            current = e;
            return false;
        }

        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.SORTED |
                Spliterator.ORDERED | Spliterator.CONCURRENT |
                Spliterator.NONNULL;
        }

        public final Comparator<? super K> getComparator() {
            return comparator;
        }
    }
    // KeySpliterator 的工厂方法
    final KeySpliterator<K,V> keySpliterator() {
        Comparator<? super K> cmp = comparator;
        for (;;) { // 确保 h 对应于原点 p
            HeadIndex<K,V> h; Node<K,V> p;
            Node<K,V> b = (h = head).node;
            if ((p = b.next) == null || p.value != null)
                return new KeySpliterator<K,V>(cmp, h, p, null, (p == null) ?
                                               0 : Integer.MAX_VALUE);
            p.helpDelete(b, p.next);
        }
    }

    static final class ValueSpliterator<K,V> extends CSLMSpliterator<K,V>
        implements Spliterator<V> {
        ValueSpliterator(Comparator<? super K> comparator, Index<K,V> row,
                       Node<K,V> origin, K fence, int est) {
            super(comparator, row, origin, fence, est);
        }

        public Spliterator<V> trySplit() {
            Node<K,V> e; K ek;
            Comparator<? super K> cmp = comparator;
            K f = fence;
            if ((e = current) != null && (ek = e.key) != null) {
                for (Index<K,V> q = row; q != null; q = row = q.down) {
                    Index<K,V> s; Node<K,V> b, n; K sk;
                    if ((s = q.right) != null && (b = s.node) != null &&
                        (n = b.next) != null && n.value != null &&
                        (sk = n.key) != null && cpr(cmp, sk, ek) > 0 &&
                        (f == null || cpr(cmp, sk, f) < 0)) {
                        current = n;
                        Index<K,V> r = q.down;
                        row = (s.right != null) ? s : s.down;
                        est -= est >>> 2;
                        return new ValueSpliterator<K,V>(cmp, r, e, sk, est);
                    }
                }
            }
            return null;
        }


                    public void forEachRemaining(Consumer<? super V> action) {
            if (action == null) throw new NullPointerException();
            Comparator<? super K> cmp = comparator;
            K f = fence;
            Node<K,V> e = current;
            current = null;
            for (; e != null; e = e.next) {
                K k; Object v;
                if ((k = e.key) != null && f != null && cpr(cmp, f, k) <= 0)
                    break;
                if ((v = e.value) != null && v != e) {
                    @SuppressWarnings("unchecked") V vv = (V)v;
                    action.accept(vv);
                }
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action == null) throw new NullPointerException();
            Comparator<? super K> cmp = comparator;
            K f = fence;
            Node<K,V> e = current;
            for (; e != null; e = e.next) {
                K k; Object v;
                if ((k = e.key) != null && f != null && cpr(cmp, f, k) <= 0) {
                    e = null;
                    break;
                }
                if ((v = e.value) != null && v != e) {
                    current = e.next;
                    @SuppressWarnings("unchecked") V vv = (V)v;
                    action.accept(vv);
                    return true;
                }
            }
            current = e;
            return false;
        }

        public int characteristics() {
            return Spliterator.CONCURRENT | Spliterator.ORDERED |
                Spliterator.NONNULL;
        }
    }

    // 几乎与 keySpliterator() 相同
    final ValueSpliterator<K,V> valueSpliterator() {
        Comparator<? super K> cmp = comparator;
        for (;;) {
            HeadIndex<K,V> h; Node<K,V> p;
            Node<K,V> b = (h = head).node;
            if ((p = b.next) == null || p.value != null)
                return new ValueSpliterator<K,V>(cmp, h, p, null, (p == null) ?
                                                 0 : Integer.MAX_VALUE);
            p.helpDelete(b, p.next);
        }
    }

    static final class EntrySpliterator<K,V> extends CSLMSpliterator<K,V>
        implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(Comparator<? super K> comparator, Index<K,V> row,
                         Node<K,V> origin, K fence, int est) {
            super(comparator, row, origin, fence, est);
        }

        public Spliterator<Map.Entry<K,V>> trySplit() {
            Node<K,V> e; K ek;
            Comparator<? super K> cmp = comparator;
            K f = fence;
            if ((e = current) != null && (ek = e.key) != null) {
                for (Index<K,V> q = row; q != null; q = row = q.down) {
                    Index<K,V> s; Node<K,V> b, n; K sk;
                    if ((s = q.right) != null && (b = s.node) != null &&
                        (n = b.next) != null && n.value != null &&
                        (sk = n.key) != null && cpr(cmp, sk, ek) > 0 &&
                        (f == null || cpr(cmp, sk, f) < 0)) {
                        current = n;
                        Index<K,V> r = q.down;
                        row = (s.right != null) ? s : s.down;
                        est -= est >>> 2;
                        return new EntrySpliterator<K,V>(cmp, r, e, sk, est);
                    }
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K,V>> action) {
            if (action == null) throw new NullPointerException();
            Comparator<? super K> cmp = comparator;
            K f = fence;
            Node<K,V> e = current;
            current = null;
            for (; e != null; e = e.next) {
                K k; Object v;
                if ((k = e.key) != null && f != null && cpr(cmp, f, k) <= 0)
                    break;
                if ((v = e.value) != null && v != e) {
                    @SuppressWarnings("unchecked") V vv = (V)v;
                    action.accept
                        (new AbstractMap.SimpleImmutableEntry<K,V>(k, vv));
                }
            }
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            if (action == null) throw new NullPointerException();
            Comparator<? super K> cmp = comparator;
            K f = fence;
            Node<K,V> e = current;
            for (; e != null; e = e.next) {
                K k; Object v;
                if ((k = e.key) != null && f != null && cpr(cmp, f, k) <= 0) {
                    e = null;
                    break;
                }
                if ((v = e.value) != null && v != e) {
                    current = e.next;
                    @SuppressWarnings("unchecked") V vv = (V)v;
                    action.accept
                        (new AbstractMap.SimpleImmutableEntry<K,V>(k, vv));
                    return true;
                }
            }
            current = e;
            return false;
        }

        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.SORTED |
                Spliterator.ORDERED | Spliterator.CONCURRENT |
                Spliterator.NONNULL;
        }

        public final Comparator<Map.Entry<K,V>> getComparator() {
            // 适应或创建一个基于键的比较器
            if (comparator != null) {
                return Map.Entry.comparingByKey(comparator);
            }
            else {
                return (Comparator<Map.Entry<K,V>> & Serializable) (e1, e2) -> {
                    @SuppressWarnings("unchecked")
                    Comparable<? super K> k1 = (Comparable<? super K>) e1.getKey();
                    return k1.compareTo(e2.getKey());
                };
            }
        }
    }

    // 几乎与 keySpliterator() 相同
    final EntrySpliterator<K,V> entrySpliterator() {
        Comparator<? super K> cmp = comparator;
        for (;;) { // 几乎与 key 版本相同
            HeadIndex<K,V> h; Node<K,V> p;
            Node<K,V> b = (h = head).node;
            if ((p = b.next) == null || p.value != null)
                return new EntrySpliterator<K,V>(cmp, h, p, null, (p == null) ?
                                                 0 : Integer.MAX_VALUE);
            p.helpDelete(b, p.next);
        }
    }

    // Unsafe 机制
    private static final sun.misc.Unsafe UNSAFE;
    private static final long headOffset;
    private static final long SECONDARY;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentSkipListMap.class;
            headOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("head"));
            Class<?> tk = Thread.class;
            SECONDARY = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomSecondarySeed"));

        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
