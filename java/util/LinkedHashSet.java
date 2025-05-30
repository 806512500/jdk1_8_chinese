
/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * <p>哈希表和链表实现的 <tt>Set</tt> 接口，具有可预测的迭代顺序。此实现与 <tt>HashSet</tt> 不同，因为它维护一个贯穿所有条目的双向链表。此链表定义了迭代顺序，即元素插入到集合中的顺序（<i>插入顺序</i>）。请注意，如果元素被<i>重新插入</i>到集合中，则插入顺序不会受到影响。（如果在调用 <tt>s.add(e)</tt> 之前 <tt>s.contains(e)</tt> 返回 <tt>true</tt>，则元素 <tt>e</tt> 被重新插入到集合 <tt>s</tt> 中。）
 *
 * <p>此实现使其客户端免受 {@link HashSet} 提供的未指定的、通常混乱的顺序的影响，而不会增加与 {@link TreeSet} 相关的成本。它可以用于生成一个具有与原始集合相同顺序的副本，无论原始集合的实现如何：
 * <pre>
 *     void foo(Set s) {
 *         Set copy = new LinkedHashSet(s);
 *         ...
 *     }
 * </pre>
 * 这种技术特别有用，如果一个模块在输入时接收一个集合，复制它，并在稍后返回结果，其顺序由副本的顺序决定。（客户端通常希望以相同的顺序返回事物。）
 *
 * <p>此类提供了所有可选的 <tt>Set</tt> 操作，并允许 null 元素。像 <tt>HashSet</tt> 一样，它为基本操作（<tt>add</tt>、<tt>contains</tt> 和 <tt>remove</tt>）提供常数时间性能，假设哈希函数将元素适当地分散到各个桶中。性能可能略低于 <tt>HashSet</tt>，因为维护链表的开销稍大，但有一个例外：迭代 <tt>LinkedHashSet</tt> 需要与集合的<i>大小</i>成正比的时间，而与容量无关。而迭代 <tt>HashSet</tt> 可能需要与<i>容量</i>成正比的时间。
 *
 * <p>链接哈希集有两个影响其性能的参数：<i>初始容量</i>和<i>加载因子</i>。它们的定义与 <tt>HashSet</tt> 完全相同。但是，请注意，为初始容量选择过高的值对这个类的惩罚比对 <tt>HashSet</tt> 的惩罚要小，因为此类的迭代时间不受容量的影响。
 *
 * <p><strong>请注意，此实现不是同步的。</strong> 如果多个线程同时访问一个链接哈希集，并且至少有一个线程修改了集合，那么必须从外部进行同步。这通常通过在自然封装集合的某个对象上进行同步来实现。
 *
 * 如果没有这样的对象，应该使用 {@link Collections#synchronizedSet Collections.synchronizedSet} 方法“包装”集合。最好在创建时进行，以防止意外的未同步访问： <pre>
 *   Set s = Collections.synchronizedSet(new LinkedHashSet(...));</pre>
 *
 * <p>此类的 <tt>iterator</tt> 方法返回的迭代器是<em>快速失败的</em>：如果在迭代器创建后以任何方式修改集合，除了通过迭代器自身的 <tt>remove</tt> 方法，迭代器将抛出 {@link ConcurrentModificationException}。因此，在面对并发修改时，迭代器会快速且干净地失败，而不是在未来的某个不确定时间冒任意的、不确定的行为风险。
 *
 * <p>请注意，迭代器的快速失败行为不能保证，因为通常情况下，无法在存在未同步的并发修改的情况下做出任何硬性保证。快速失败的迭代器在尽最大努力的基础上抛出 <tt>ConcurrentModificationException</tt>。因此，编写依赖于此异常正确性的程序是错误的：<i>迭代器的快速失败行为应仅用于检测错误。</i>
 *
 * <p>此类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @param <E> 由此集合维护的元素类型
 *
 * @author  Josh Bloch
 * @see     Object#hashCode()
 * @see     Collection
 * @see     Set
 * @see     HashSet
 * @see     TreeSet
 * @see     Hashtable
 * @since   1.4
 */

public class LinkedHashSet<E>
    extends HashSet<E>
    implements Set<E>, Cloneable, java.io.Serializable {

    private static final long serialVersionUID = -2851667679971038690L;

    /**
     * 构造一个新的、空的链接哈希集，具有指定的初始容量和加载因子。
     *
     * @param      initialCapacity  链接哈希集的初始容量
     * @param      loadFactor       链接哈希集的加载因子
     * @throws     IllegalArgumentException  如果初始容量小于零，或加载因子非正
     */
    public LinkedHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
    }


/**
 * 构造一个具有指定初始容量和默认负载因子（0.75）的新空链接哈希集。
 *
 * @param   initialCapacity   LinkedHashSet 的初始容量
 * @throws  IllegalArgumentException 如果初始容量小于零
 */
public LinkedHashSet(int initialCapacity) {
    super(initialCapacity, .75f, true);
}

/**
 * 构造一个具有默认初始容量（16）和负载因子（0.75）的新空链接哈希集。
 */
public LinkedHashSet() {
    super(16, .75f, true);
}

/**
 * 构造一个具有与指定集合相同元素的新链接哈希集。链接哈希集的初始容量足以容纳指定集合中的元素，并且具有默认负载因子（0.75）。
 *
 * @param c  要放置到此集合中的集合
 * @throws NullPointerException 如果指定的集合为 null
 */
public LinkedHashSet(Collection<? extends E> c) {
    super(Math.max(2*c.size(), 11), .75f, true);
    addAll(c);
}

/**
 * 创建一个对本集合中元素的 <em><a href="Spliterator.html#binding">延迟绑定</a></em>
 * 和 <em>快速失败</em> 的 {@code Spliterator}。
 *
 * <p>{@code Spliterator} 报告 {@link Spliterator#SIZED}，
 * {@link Spliterator#DISTINCT} 和 {@code ORDERED}。实现应记录额外特征值的报告。
 *
 * @implNote
 * 实现从集合的 {@code Iterator} 创建一个
 * <em><a href="Spliterator.html#binding">延迟绑定</a></em> spliterator。
 * spliterator 继承集合迭代器的 <em>快速失败</em> 属性。
 * 创建的 {@code Spliterator} 还报告 {@link Spliterator#SUBSIZED}。
 *
 * @return 一个遍历此集合中元素的 {@code Spliterator}
 * @since 1.8
 */
@Override
public Spliterator<E> spliterator() {
    return Spliterators.spliterator(this, Spliterator.DISTINCT | Spliterator.ORDERED);
}
