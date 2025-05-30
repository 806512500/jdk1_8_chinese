
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

package java.util;

/**
 * 一个用于在处理前保存元素的集合。
 * 除了基本的 {@link java.util.Collection Collection} 操作外，
 * 队列还提供了额外的插入、提取和检查操作。每个这些方法都存在两种形式：一种在操作失败时抛出异常，
 * 另一种返回一个特殊值（根据操作的不同，可能是 {@code null} 或 {@code false}）。
 * 后一种插入操作形式特别设计用于容量受限的 {@code Queue} 实现；在大多数实现中，插入操作不会失败。
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>队列方法概要</caption>
 *  <tr>
 *    <td></td>
 *    <td ALIGN=CENTER><em>抛出异常</em></td>
 *    <td ALIGN=CENTER><em>返回特殊值</em></td>
 *  </tr>
 *  <tr>
 *    <td><b>插入</b></td>
 *    <td>{@link Queue#add add(e)}</td>
 *    <td>{@link Queue#offer offer(e)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>移除</b></td>
 *    <td>{@link Queue#remove remove()}</td>
 *    <td>{@link Queue#poll poll()}</td>
 *  </tr>
 *  <tr>
 *    <td><b>检查</b></td>
 *    <td>{@link Queue#element element()}</td>
 *    <td>{@link Queue#peek peek()}</td>
 *  </tr>
 * </table>
 *
 * <p>队列通常但不一定是按 FIFO（先进先出）顺序排列元素。例外情况包括优先队列，
 * 它根据提供的比较器或元素的自然顺序对元素进行排序，以及 LIFO 队列（或栈），
 * 它按 LIFO（后进先出）顺序排列元素。无论使用何种排序方式，队列的 <em>头部</em>
 * 都是调用 {@link #remove() } 或 {@link #poll()} 时将被移除的元素。在 FIFO 队列中，
 * 所有新元素都插入到队列的 <em>尾部</em>。其他类型的队列可能使用不同的放置规则。
 * 每个 {@code Queue} 实现都必须指定其排序属性。
 *
 * <p>{@link #offer offer} 方法在可能的情况下插入一个元素，否则返回 {@code false}。
 * 这与 {@link java.util.Collection#add Collection.add} 方法不同，后者只能通过抛出未检查的异常来失败。
 * {@code offer} 方法设计用于失败是正常而非异常情况的场景，例如在固定容量（或“有界”）队列中。
 *
 * <p>{@link #remove()} 和 {@link #poll()} 方法移除并返回队列的头部。
 * 具体移除哪个元素取决于队列的排序策略，这在不同的实现中有所不同。{@code remove()}
 * 和 {@code poll()} 方法的区别在于队列为空时的行为：{@code remove()} 方法抛出异常，
 * 而 {@code poll()} 方法返回 {@code null}。
 *
 * <p>{@link #element()} 和 {@link #peek()} 方法返回但不移除队列的头部。
 *
 * <p>{@code Queue} 接口未定义 <i>阻塞队列方法</i>，这些方法在并发编程中很常见。
 * 这些方法等待元素出现或空间变得可用，定义在扩展此接口的 {@link java.util.concurrent.BlockingQueue} 接口中。
 *
 * <p>{@code Queue} 实现通常不允许插入 {@code null} 元素，尽管某些实现（如 {@link LinkedList}）不禁止插入 {@code null}。
 * 即使在允许插入的实现中，也不应将 {@code null} 插入到 {@code Queue} 中，因为 {@code null}
 * 也用作 {@code poll} 方法的特殊返回值，表示队列中没有元素。
 *
 * <p>{@code Queue} 实现通常不定义基于元素的方法 {@code equals} 和 {@code hashCode}，
 * 而是继承自 {@code Object} 类的基于身份的版本，因为基于元素的相等性在具有相同元素但不同排序属性的队列中不一定总是定义良好。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @see java.util.Collection
 * @see LinkedList
 * @see PriorityQueue
 * @see java.util.concurrent.LinkedBlockingQueue
 * @see java.util.concurrent.BlockingQueue
 * @see java.util.concurrent.ArrayBlockingQueue
 * @see java.util.concurrent.LinkedBlockingQueue
 * @see java.util.concurrent.PriorityBlockingQueue
 * @since 1.5
 * @author Doug Lea
 * @param <E> 此集合中持有的元素类型
 */
public interface Queue<E> extends Collection<E> {
    /**
     * 如果可以在不违反容量限制的情况下立即将指定元素插入此队列，则插入该元素，
     * 成功时返回 {@code true}，如果当前没有空间可用，则抛出 {@code IllegalStateException}。
     *
     * @param e 要添加的元素
     * @return {@code true}（如 {@link Collection#add} 所指定）
     * @throws IllegalStateException 如果由于容量限制此时无法添加元素
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此队列
     * @throws NullPointerException 如果指定元素为 null 且此队列不允许 null 元素
     * @throws IllegalArgumentException 如果此元素的某些属性阻止其被添加到此队列
     */
    boolean add(E e);


                /**
     * 将指定的元素插入此队列，如果可以立即插入而不违反容量限制。
     * 使用容量受限的队列时，此方法通常优于 {@link #add}，因为后者只能通过抛出异常来失败插入元素。
     *
     * @param e 要添加的元素
     * @return 如果元素已添加到此队列，则返回 {@code true}，否则返回 {@code false}
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此队列
     * @throws NullPointerException 如果指定元素为 null 且此队列不允许 null 元素
     * @throws IllegalArgumentException 如果此元素的某些属性阻止其被添加到此队列
     */
    boolean offer(E e);

    /**
     * 检索并移除此队列的头部。此方法与 {@link #poll poll} 的不同之处在于，如果此队列为空，它会抛出异常。
     *
     * @return 此队列的头部
     * @throws NoSuchElementException 如果此队列为空
     */
    E remove();

    /**
     * 检索并移除此队列的头部，如果此队列为空，则返回 {@code null}。
     *
     * @return 此队列的头部，如果此队列为空，则返回 {@code null}
     */
    E poll();

    /**
     * 检索但不移除此队列的头部。此方法与 {@link #peek peek} 的不同之处在于，如果此队列为空，它会抛出异常。
     *
     * @return 此队列的头部
     * @throws NoSuchElementException 如果此队列为空
     */
    E element();

    /**
     * 检索但不移除此队列的头部，如果此队列为空，则返回 {@code null}。
     *
     * @return 此队列的头部，如果此队列为空，则返回 {@code null}
     */
    E peek();
}
