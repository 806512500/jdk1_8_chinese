
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
 * Written by Doug Lea and Josh Bloch with assistance from members of
 * JCP JSR-166 Expert Group and released to the public domain, as explained
 * at http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util;

/**
 * 一个支持在两端插入和移除元素的线性集合。名称 <i>deque</i> 是 "double ended queue" 的缩写，
 * 通常发音为 "deck"。大多数 {@code Deque} 实现对它们可以包含的元素数量没有固定的限制，但此接口
 * 也支持容量受限的 deque 以及没有固定大小限制的 deque。
 *
 * <p>此接口定义了访问 deque 两端元素的方法。提供了插入、移除和检查元素的方法。每种方法都存在两种形式：
 * 一种在操作失败时抛出异常，另一种返回一个特殊值（根据操作不同，可能是 {@code null} 或 {@code false}）。
 * 后者形式的插入操作特别设计用于容量受限的 {@code Deque} 实现；在大多数实现中，插入操作不会失败。
 *
 * <p>上述十二种方法总结在下表中：
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>Deque 方法概览</caption>
 *  <tr>
 *    <td></td>
 *    <td ALIGN=CENTER COLSPAN = 2> <b>第一个元素（头部）</b></td>
 *    <td ALIGN=CENTER COLSPAN = 2> <b>最后一个元素（尾部）</b></td>
 *  </tr>
 *  <tr>
 *    <td></td>
 *    <td ALIGN=CENTER><em>抛出异常</em></td>
 *    <td ALIGN=CENTER><em>特殊值</em></td>
 *    <td ALIGN=CENTER><em>抛出异常</em></td>
 *    <td ALIGN=CENTER><em>特殊值</em></td>
 *  </tr>
 *  <tr>
 *    <td><b>插入</b></td>
 *    <td>{@link Deque#addFirst addFirst(e)}</td>
 *    <td>{@link Deque#offerFirst offerFirst(e)}</td>
 *    <td>{@link Deque#addLast addLast(e)}</td>
 *    <td>{@link Deque#offerLast offerLast(e)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>移除</b></td>
 *    <td>{@link Deque#removeFirst removeFirst()}</td>
 *    <td>{@link Deque#pollFirst pollFirst()}</td>
 *    <td>{@link Deque#removeLast removeLast()}</td>
 *    <td>{@link Deque#pollLast pollLast()}</td>
 *  </tr>
 *  <tr>
 *    <td><b>检查</b></td>
 *    <td>{@link Deque#getFirst getFirst()}</td>
 *    <td>{@link Deque#peekFirst peekFirst()}</td>
 *    <td>{@link Deque#getLast getLast()}</td>
 *    <td>{@link Deque#peekLast peekLast()}</td>
 *  </tr>
 * </table>
 *
 * <p>此接口扩展了 {@link Queue} 接口。当 deque 用作队列时，结果是 FIFO（先进先出）行为。元素被添加到 deque 的末尾并从开头移除。
 * 从 {@code Queue} 接口继承的方法与 {@code Deque} 方法完全等效，如下表所示：
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>Queue 和 Deque 方法对比</caption>
 *  <tr>
 *    <td ALIGN=CENTER> <b>{@code Queue} 方法</b></td>
 *    <td ALIGN=CENTER> <b>等效的 {@code Deque} 方法</b></td>
 *  </tr>
 *  <tr>
 *    <td>{@link java.util.Queue#add add(e)}</td>
 *    <td>{@link #addLast addLast(e)}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link java.util.Queue#offer offer(e)}</td>
 *    <td>{@link #offerLast offerLast(e)}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link java.util.Queue#remove remove()}</td>
 *    <td>{@link #removeFirst removeFirst()}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link java.util.Queue#poll poll()}</td>
 *    <td>{@link #pollFirst pollFirst()}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link java.util.Queue#element element()}</td>
 *    <td>{@link #getFirst getFirst()}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link java.util.Queue#peek peek()}</td>
 *    <td>{@link #peek peekFirst()}</td>
 *  </tr>
 * </table>
 *
 * <p>Deque 还可以用作 LIFO（后进先出）栈。建议使用此接口而不是旧的 {@link Stack} 类。
 * 当 deque 用作栈时，元素从 deque 的开头推入和弹出。栈方法与 {@code Deque} 方法完全等效，如下表所示：
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>Stack 和 Deque 方法对比</caption>
 *  <tr>
 *    <td ALIGN=CENTER> <b>Stack 方法</b></td>
 *    <td ALIGN=CENTER> <b>等效的 {@code Deque} 方法</b></td>
 *  </tr>
 *  <tr>
 *    <td>{@link #push push(e)}</td>
 *    <td>{@link #addFirst addFirst(e)}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #pop pop()}</td>
 *    <td>{@link #removeFirst removeFirst()}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #peek peek()}</td>
 *    <td>{@link #peekFirst peekFirst()}</td>
 *  </tr>
 * </table>
 *
 * <p>请注意，当 deque 用作队列或栈时，{@link #peek peek} 方法同样有效；在这两种情况下，元素都是从 deque 的开头获取的。
 *
 * <p>此接口提供了两种方法来移除内部元素，{@link #removeFirstOccurrence removeFirstOccurrence} 和
 * {@link #removeLastOccurrence removeLastOccurrence}。
 *
 * <p>与 {@link List} 接口不同，此接口不提供对元素的索引访问支持。
 *
 * <p>虽然 {@code Deque} 实现不要求禁止插入 null 元素，但强烈建议这样做。使用允许插入 null 元素的任何 {@code Deque} 实现的用户
 * 强烈建议 <i>不要</i> 利用插入 null 的能力。这是因为 {@code null} 用作各种方法的特殊返回值，以表示 deque 为空。
 *
 * <p>{@code Deque} 实现通常不定义基于元素的 {@code equals} 和 {@code hashCode} 方法版本，而是继承自类
 * {@code Object} 的基于身份的版本。
 *
 * <p>此接口是 <a
 * href="{@docRoot}/../technotes/guides/collections/index.html"> Java Collections
 * Framework</a> 的成员。
 *
 * @author Doug Lea
 * @author Josh Bloch
 * @since  1.6
 * @param <E> 此集合中持有的元素类型
 */
public interface Deque<E> extends Queue<E> {
    /**
     * 如果可以在不违反容量限制的情况下立即将指定元素插入到此 deque 的前端，则插入该元素，如果当前没有可用空间，则抛出
     * {@code IllegalStateException}。当使用容量受限的 deque 时，通常建议使用方法 {@link #offerFirst}。
     *
     * @param e 要添加的元素
     * @throws IllegalStateException 如果由于容量限制而无法在此时添加元素
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此 deque
     * @throws NullPointerException 如果指定元素为 null 且此 deque 不允许 null 元素
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此 deque
     */
    void addFirst(E e);


                /**
     * 在此双端队列的末尾插入指定的元素，如果可以立即插入而不违反容量限制，则插入成功；
     * 如果当前没有可用空间，则抛出 {@code IllegalStateException}。当使用容量受限的双端队列时，
     * 通常建议使用方法 {@link #offerLast}。
     *
     * <p>此方法等同于 {@link #add}。
     *
     * @param e 要添加的元素
     * @throws IllegalStateException 如果由于容量限制当前无法添加元素
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列中
     * @throws NullPointerException 如果指定的元素为 null 且此双端队列不允许 null 元素
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列中
     */
    void addLast(E e);

    /**
     * 在此双端队列的前端插入指定的元素，除非违反容量限制。当使用容量受限的双端队列时，
     * 通常建议使用此方法而不是 {@link #addFirst} 方法，因为后者只能通过抛出异常来失败。
     *
     * @param e 要添加的元素
     * @return 如果元素被添加到此双端队列中，则返回 {@code true}，否则返回 {@code false}
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列中
     * @throws NullPointerException 如果指定的元素为 null 且此双端队列不允许 null 元素
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列中
     */
    boolean offerFirst(E e);

    /**
     * 在此双端队列的末尾插入指定的元素，除非违反容量限制。当使用容量受限的双端队列时，
     * 通常建议使用此方法而不是 {@link #addLast} 方法，因为后者只能通过抛出异常来失败。
     *
     * @param e 要添加的元素
     * @return 如果元素被添加到此双端队列中，则返回 {@code true}，否则返回 {@code false}
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列中
     * @throws NullPointerException 如果指定的元素为 null 且此双端队列不允许 null 元素
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列中
     */
    boolean offerLast(E e);

    /**
     * 获取并移除此双端队列的第一个元素。此方法与 {@link #pollFirst pollFirst} 的区别在于，
     * 如果此双端队列为空，则抛出异常。
     *
     * @return 此双端队列的头部
     * @throws NoSuchElementException 如果此双端队列为空
     */
    E removeFirst();

    /**
     * 获取并移除此双端队列的最后一个元素。此方法与 {@link #pollLast pollLast} 的区别在于，
     * 如果此双端队列为空，则抛出异常。
     *
     * @return 此双端队列的尾部
     * @throws NoSuchElementException 如果此双端队列为空
     */
    E removeLast();

    /**
     * 获取并移除此双端队列的第一个元素，如果此双端队列为空，则返回 {@code null}。
     *
     * @return 此双端队列的头部，如果此双端队列为空，则返回 {@code null}
     */
    E pollFirst();

    /**
     * 获取并移除此双端队列的最后一个元素，如果此双端队列为空，则返回 {@code null}。
     *
     * @return 此双端队列的尾部，如果此双端队列为空，则返回 {@code null}
     */
    E pollLast();

    /**
     * 获取但不移除此双端队列的第一个元素。
     *
     * 此方法与 {@link #peekFirst peekFirst} 的区别在于，如果此双端队列为空，则抛出异常。
     *
     * @return 此双端队列的头部
     * @throws NoSuchElementException 如果此双端队列为空
     */
    E getFirst();

    /**
     * 获取但不移除此双端队列的最后一个元素。
     * 此方法与 {@link #peekLast peekLast} 的区别在于，如果此双端队列为空，则抛出异常。
     *
     * @return 此双端队列的尾部
     * @throws NoSuchElementException 如果此双端队列为空
     */
    E getLast();

    /**
     * 获取但不移除此双端队列的第一个元素，如果此双端队列为空，则返回 {@code null}。
     *
     * @return 此双端队列的头部，如果此双端队列为空，则返回 {@code null}
     */
    E peekFirst();

    /**
     * 获取但不移除此双端队列的最后一个元素，如果此双端队列为空，则返回 {@code null}。
     *
     * @return 此双端队列的尾部，如果此双端队列为空，则返回 {@code null}
     */
    E peekLast();

    /**
     * 从此双端队列中移除指定元素的第一个出现。如果双端队列中不包含该元素，则保持不变。
     * 更正式地说，移除第一个满足 <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>
     * 的元素（如果存在这样的元素）。如果此双端队列包含指定的元素（或等效地，如果此双端队列因调用而改变），
     * 则返回 {@code true}。
     *
     * @param o 要从此双端队列中移除的元素，如果存在
     * @return 如果因调用此方法而移除了一个元素，则返回 {@code true}
     * @throws ClassCastException 如果指定元素的类与此双端队列不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为 null 且此双端队列不允许 null 元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     */
    boolean removeFirstOccurrence(Object o);


                /**
     * 从双端队列中移除指定元素的最后一个出现。如果双端队列不包含该元素，则保持不变。
     * 更正式地说，移除满足以下条件的最后一个元素 {@code e}：
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>
     * （如果存在这样的元素）。
     * 如果双端队列包含指定的元素（或等效地，如果此调用导致双端队列发生变化），则返回 {@code true}。
     *
     * @param o 要从双端队列中移除的元素（如果存在）
     * @return 如果由于此调用移除了一个元素，则返回 {@code true}
     * @throws ClassCastException 如果指定元素的类与双端队列不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为 null 且双端队列不允许 null 元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     */
    boolean removeLastOccurrence(Object o);

    // *** 队列方法 ***

    /**
     * 将指定的元素插入到此双端队列表示的队列中（换句话说，插入到双端队列的尾部），如果可以立即插入而不违反容量限制，则返回
     * {@code true}，如果当前没有可用空间，则抛出 {@code IllegalStateException}。
     * 当使用容量受限的双端队列时，通常更倾向于使用 {@link #offer(Object) offer}。
     *
     * <p>此方法等同于 {@link #addLast}。
     *
     * @param e 要添加的元素
     * @return {@code true}（如 {@link Collection#add} 所指定）
     * @throws IllegalStateException 如果由于容量限制当前无法添加元素
     * @throws ClassCastException 如果指定元素的类阻止其被添加到双端队列
     * @throws NullPointerException 如果指定的元素为 null 且双端队列不允许 null 元素
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到双端队列
     */
    boolean add(E e);

    /**
     * 将指定的元素插入到此双端队列表示的队列中（换句话说，插入到双端队列的尾部），如果可以立即插入而不违反容量限制，则返回
     * {@code true}，如果当前没有可用空间，则返回 {@code false}。
     * 当使用容量受限的双端队列时，此方法通常比 {@link #add} 方法更可取，因为后者只能通过抛出异常来失败。
     *
     * <p>此方法等同于 {@link #offerLast}。
     *
     * @param e 要添加的元素
     * @return 如果元素被添加到双端队列，则返回 {@code true}，否则返回 {@code false}
     * @throws ClassCastException 如果指定元素的类阻止其被添加到双端队列
     * @throws NullPointerException 如果指定的元素为 null 且双端队列不允许 null 元素
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到双端队列
     */
    boolean offer(E e);

    /**
     * 检索并移除此双端队列表示的队列的头部（换句话说，双端队列的第一个元素）。
     * 此方法与 {@link #poll poll} 的区别在于，如果双端队列为空，则抛出异常。
     *
     * <p>此方法等同于 {@link #removeFirst()}。
     *
     * @return 此双端队列表示的队列的头部
     * @throws NoSuchElementException 如果双端队列为空
     */
    E remove();

    /**
     * 检索并移除此双端队列表示的队列的头部（换句话说，双端队列的第一个元素），如果双端队列为空，则返回
     * {@code null}。
     *
     * <p>此方法等同于 {@link #pollFirst()}。
     *
     * @return 此双端队列的第一个元素，如果双端队列为空，则返回 {@code null}
     */
    E poll();

    /**
     * 检索但不移除此双端队列表示的队列的头部（换句话说，双端队列的第一个元素）。
     * 此方法与 {@link #peek peek} 的区别在于，如果双端队列为空，则抛出异常。
     *
     * <p>此方法等同于 {@link #getFirst()}。
     *
     * @return 此双端队列表示的队列的头部
     * @throws NoSuchElementException 如果双端队列为空
     */
    E element();

    /**
     * 检索但不移除此双端队列表示的队列的头部（换句话说，双端队列的第一个元素），如果双端队列为空，则返回
     * {@code null}。
     *
     * <p>此方法等同于 {@link #peekFirst()}。
     *
     * @return 此双端队列的头部，如果双端队列为空，则返回 {@code null}
     */
    E peek();

    // *** 栈方法 ***

    /**
     * 将元素推入此双端队列表示的栈中（换句话说，插入到双端队列的头部），如果可以立即插入而不违反容量限制，则抛出
     * {@code IllegalStateException}。
     *
     * <p>此方法等同于 {@link #addFirst}。
     *
     * @param e 要推入的元素
     * @throws IllegalStateException 如果由于容量限制当前无法添加元素
     * @throws ClassCastException 如果指定元素的类阻止其被添加到双端队列
     * @throws NullPointerException 如果指定的元素为 null 且双端队列不允许 null 元素
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到双端队列
     */
    void push(E e);


                /**
     * 从表示为双端队列的栈中弹出一个元素。换句话说，移除并返回此双端队列的第一个元素。
     *
     * <p>此方法等同于 {@link #removeFirst()}。
     *
     * @return 此双端队列前端的元素（即表示为双端队列的栈的顶部）
     * @throws NoSuchElementException 如果此双端队列为空
     */
    E pop();


    // *** Collection 方法 ***

    /**
     * 从此双端队列中移除指定元素的第一个出现位置。如果双端队列不包含该元素，则不进行更改。
     * 更正式地说，移除第一个满足 <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>
     * 的元素（如果存在这样的元素）。
     * 如果此双端队列包含指定的元素（或等效地，如果此双端队列因调用而改变），则返回 {@code true}。
     *
     * <p>此方法等同于 {@link #removeFirstOccurrence(Object)}。
     *
     * @param o 要从此双端队列中移除的元素（如果存在）
     * @return {@code true} 如果调用此方法后移除了一个元素
     * @throws ClassCastException 如果指定元素的类与此双端队列不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为 null 且此双端队列不允许 null 元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     */
    boolean remove(Object o);

    /**
     * 如果此双端队列包含指定的元素，则返回 {@code true}。
     * 更正式地说，如果且仅当此双端队列包含至少一个满足
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt> 的元素 {@code e} 时，返回 {@code true}。
     *
     * @param o 要测试其是否存在于此双端队列中的元素
     * @return {@code true} 如果此双端队列包含指定的元素
     * @throws ClassCastException 如果指定元素的类型与此双端队列不兼容
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为 null 且此双端队列不允许 null 元素
     * (<a href="Collection.html#optional-restrictions">可选</a>)
     */
    boolean contains(Object o);

    /**
     * 返回此双端队列中的元素数量。
     *
     * @return 此双端队列中的元素数量
     */
    public int size();

    /**
     * 返回一个按适当顺序遍历此双端队列中元素的迭代器。
     * 元素将按从第一个（头部）到最后一个（尾部）的顺序返回。
     *
     * @return 一个按适当顺序遍历此双端队列中元素的迭代器
     */
    Iterator<E> iterator();

    /**
     * 返回一个按逆序遍历此双端队列中元素的迭代器。
     * 元素将按从最后一个（尾部）到第一个（头部）的顺序返回。
     *
     * @return 一个按逆序遍历此双端队列中元素的迭代器
     */
    Iterator<E> descendingIterator();

}
