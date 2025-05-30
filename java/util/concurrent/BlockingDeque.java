
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
import java.util.*;

/**
 * 一个 {@link Deque}，此外还支持等待操作，这些操作在检索元素时等待双端队列变为非空，在存储元素时等待双端队列中有可用空间。
 *
 * <p>{@code BlockingDeque} 方法有四种形式，分别以不同的方式处理无法立即满足但将来可能满足的操作：
 * 一种抛出异常，第二种返回一个特殊值（根据操作的不同，可能是 {@code null} 或 {@code false}），第三种无限期地阻塞当前线程直到操作可以成功，
 * 第四种在给定的最大时间限制内阻塞，如果在该时间内无法成功则放弃。这些方法总结在下表中：
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>BlockingDeque 方法概览</caption>
 *  <tr>
 *    <td ALIGN=CENTER COLSPAN = 5> <b>第一个元素（头部）</b></td>
 *  </tr>
 *  <tr>
 *    <td></td>
 *    <td ALIGN=CENTER><em>抛出异常</em></td>
 *    <td ALIGN=CENTER><em>特殊值</em></td>
 *    <td ALIGN=CENTER><em>阻塞</em></td>
 *    <td ALIGN=CENTER><em>超时</em></td>
 *  </tr>
 *  <tr>
 *    <td><b>插入</b></td>
 *    <td>{@link #addFirst addFirst(e)}</td>
 *    <td>{@link #offerFirst(Object) offerFirst(e)}</td>
 *    <td>{@link #putFirst putFirst(e)}</td>
 *    <td>{@link #offerFirst(Object, long, TimeUnit) offerFirst(e, time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>移除</b></td>
 *    <td>{@link #removeFirst removeFirst()}</td>
 *    <td>{@link #pollFirst pollFirst()}</td>
 *    <td>{@link #takeFirst takeFirst()}</td>
 *    <td>{@link #pollFirst(long, TimeUnit) pollFirst(time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>检查</b></td>
 *    <td>{@link #getFirst getFirst()}</td>
 *    <td>{@link #peekFirst peekFirst()}</td>
 *    <td><em>不适用</em></td>
 *    <td><em>不适用</em></td>
 *  </tr>
 *  <tr>
 *    <td ALIGN=CENTER COLSPAN = 5> <b>最后一个元素（尾部）</b></td>
 *  </tr>
 *  <tr>
 *    <td></td>
 *    <td ALIGN=CENTER><em>抛出异常</em></td>
 *    <td ALIGN=CENTER><em>特殊值</em></td>
 *    <td ALIGN=CENTER><em>阻塞</em></td>
 *    <td ALIGN=CENTER><em>超时</em></td>
 *  </tr>
 *  <tr>
 *    <td><b>插入</b></td>
 *    <td>{@link #addLast addLast(e)}</td>
 *    <td>{@link #offerLast(Object) offerLast(e)}</td>
 *    <td>{@link #putLast putLast(e)}</td>
 *    <td>{@link #offerLast(Object, long, TimeUnit) offerLast(e, time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>移除</b></td>
 *    <td>{@link #removeLast() removeLast()}</td>
 *    <td>{@link #pollLast() pollLast()}</td>
 *    <td>{@link #takeLast takeLast()}</td>
 *    <td>{@link #pollLast(long, TimeUnit) pollLast(time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>检查</b></td>
 *    <td>{@link #getLast getLast()}</td>
 *    <td>{@link #peekLast peekLast()}</td>
 *    <td><em>不适用</em></td>
 *    <td><em>不适用</em></td>
 *  </tr>
 * </table>
 *
 * <p>像任何 {@link BlockingQueue} 一样，一个 {@code BlockingDeque} 是线程安全的，不允许 null 元素，并且可能是（或可能不是）容量受限的。
 *
 * <p>一个 {@code BlockingDeque} 实现可以直接用作 FIFO {@code BlockingQueue}。从 {@code BlockingQueue} 接口继承的方法与 {@code BlockingDeque} 方法完全等效，如下表所示：
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>BlockingQueue 和 BlockingDeque 方法对比</caption>
 *  <tr>
 *    <td ALIGN=CENTER> <b>{@code BlockingQueue} 方法</b></td>
 *    <td ALIGN=CENTER> <b>等效的 {@code BlockingDeque} 方法</b></td>
 *  </tr>
 *  <tr>
 *    <td ALIGN=CENTER COLSPAN = 2> <b>插入</b></td>
 *  </tr>
 *  <tr>
 *    <td>{@link #add(Object) add(e)}</td>
 *    <td>{@link #addLast(Object) addLast(e)}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #offer(Object) offer(e)}</td>
 *    <td>{@link #offerLast(Object) offerLast(e)}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #put(Object) put(e)}</td>
 *    <td>{@link #putLast(Object) putLast(e)}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #offer(Object, long, TimeUnit) offer(e, time, unit)}</td>
 *    <td>{@link #offerLast(Object, long, TimeUnit) offerLast(e, time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td ALIGN=CENTER COLSPAN = 2> <b>移除</b></td>
 *  </tr>
 *  <tr>
 *    <td>{@link #remove() remove()}</td>
 *    <td>{@link #removeFirst() removeFirst()}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #poll() poll()}</td>
 *    <td>{@link #pollFirst() pollFirst()}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #take() take()}</td>
 *    <td>{@link #takeFirst() takeFirst()}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #poll(long, TimeUnit) poll(time, unit)}</td>
 *    <td>{@link #pollFirst(long, TimeUnit) pollFirst(time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td ALIGN=CENTER COLSPAN = 2> <b>检查</b></td>
 *  </tr>
 *  <tr>
 *    <td>{@link #element() element()}</td>
 *    <td>{@link #getFirst() getFirst()}</td>
 *  </tr>
 *  <tr>
 *    <td>{@link #peek() peek()}</td>
 *    <td>{@link #peekFirst() peekFirst()}</td>
 *  </tr>
 * </table>
 *
 * <p>内存一致性效果：与其他并发集合一样，一个线程在将对象放入 {@code BlockingDeque} 之前的操作
 * <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 另一个线程访问或移除该元素后的操作。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @since 1.6
 * @author Doug Lea
 * @param <E> 此集合中元素的类型
 */
public interface BlockingDeque<E> extends BlockingQueue<E>, Deque<E> {
    /*
     * 我们在这里有“菱形”多重接口继承，这引入了歧义。根据 javadoc 选择的分支，方法可能会有不同的规范。因此这里很多方法的规范都是从超接口复制过来的。
     */

    /**
     * 如果可以在不违反容量限制的情况下立即插入指定元素，则将其插入此双端队列的前端，如果当前没有可用空间，则抛出 {@code IllegalStateException}。
     * 当使用容量受限的双端队列时，通常更喜欢使用 {@link #offerFirst(Object) offerFirst}。
     *
     * @param e 要添加的元素
     * @throws IllegalStateException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    void addFirst(E e);

    /**
     * 如果可以在不违反容量限制的情况下立即插入指定元素，则将其插入此双端队列的末尾，如果当前没有可用空间，则抛出 {@code IllegalStateException}。
     * 当使用容量受限的双端队列时，通常更喜欢使用 {@link #offerLast(Object) offerLast}。
     *
     * @param e 要添加的元素
     * @throws IllegalStateException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    void addLast(E e);

    /**
     * 如果可以在不违反容量限制的情况下立即插入指定元素，则将其插入此双端队列的前端，成功时返回 {@code true}，当前没有可用空间时返回 {@code false}。
     * 当使用容量受限的双端队列时，此方法通常比 {@link #addFirst(Object) addFirst} 方法更可取，因为后者只能通过抛出异常来失败。
     *
     * @param e 要添加的元素
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    boolean offerFirst(E e);

    /**
     * 如果可以在不违反容量限制的情况下立即插入指定元素，则将其插入此双端队列的末尾，成功时返回 {@code true}，当前没有可用空间时返回 {@code false}。
     * 当使用容量受限的双端队列时，此方法通常比 {@link #addLast(Object) addLast} 方法更可取，因为后者只能通过抛出异常来失败。
     *
     * @param e 要添加的元素
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    boolean offerLast(E e);

    /**
     * 将指定元素插入此双端队列的前端，必要时等待空间可用。
     *
     * @param e 要添加的元素
     * @throws InterruptedException 如果等待时被中断
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列中
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列中
     */
    void putFirst(E e) throws InterruptedException;

    /**
     * 将指定元素插入此双端队列的末尾，必要时等待空间可用。
     *
     * @param e 要添加的元素
     * @throws InterruptedException 如果等待时被中断
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列中
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列中
     */
    void putLast(E e) throws InterruptedException;

    /**
     * 将指定元素插入此双端队列的前端，必要时等待指定的等待时间，直到空间可用。
     *
     * @param e 要添加的元素
     * @param timeout 在放弃前等待的时间，以 {@code unit} 为单位
     * @param unit 一个 {@code TimeUnit}，用于解释 {@code timeout} 参数
     * @return 如果成功则返回 {@code true}，如果指定的等待时间到期前没有空间可用则返回 {@code false}
     * @throws InterruptedException 如果等待时被中断
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列中
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列中
     */
    boolean offerFirst(E e, long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 将指定元素插入此双端队列的末尾，必要时等待指定的等待时间，直到空间可用。
     *
     * @param e 要添加的元素
     * @param timeout 在放弃前等待的时间，以 {@code unit} 为单位
     * @param unit 一个 {@code TimeUnit}，用于解释 {@code timeout} 参数
     * @return 如果成功则返回 {@code true}，如果指定的等待时间到期前没有空间可用则返回 {@code false}
     * @throws InterruptedException 如果等待时被中断
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列中
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列中
     */
    boolean offerLast(E e, long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 检索并移除此双端队列的第一个元素，必要时等待元素可用。
     *
     * @return 此双端队列的头部
     * @throws InterruptedException 如果等待时被中断
     */
    E takeFirst() throws InterruptedException;

    /**
     * 检索并移除此双端队列的最后一个元素，必要时等待元素可用。
     *
     * @return 此双端队列的尾部
     * @throws InterruptedException 如果等待时被中断
     */
    E takeLast() throws InterruptedException;

    /**
     * 检索并移除此双端队列的第一个元素，必要时等待指定的等待时间，直到元素可用。
     *
     * @param timeout 在放弃前等待的时间，以 {@code unit} 为单位
     * @param unit 一个 {@code TimeUnit}，用于解释 {@code timeout} 参数
     * @return 此双端队列的头部，如果指定的等待时间到期前没有元素可用则返回 {@code null}
     * @throws InterruptedException 如果等待时被中断
     */
    E pollFirst(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 检索并移除此双端队列的最后一个元素，必要时等待指定的等待时间，直到元素可用。
     *
     * @param timeout 在放弃前等待的时间，以 {@code unit} 为单位
     * @param unit 一个 {@code TimeUnit}，用于解释 {@code timeout} 参数
     * @return 此双端队列的尾部，如果指定的等待时间到期前没有元素可用则返回 {@code null}
     * @throws InterruptedException 如果等待时被中断
     */
    E pollLast(long timeout, TimeUnit unit)
        throws InterruptedException;


                /**
     * 从双端队列中移除指定元素的第一个出现。如果双端队列不包含该元素，则不改变。
     * 更正式地说，移除第一个满足 {@code o.equals(e)} 的元素（如果存在这样的元素）。
     * 如果双端队列包含指定的元素（或等效地，如果此调用导致双端队列发生变化），则返回 {@code true}。
     *
     * @param o 如果存在，则从双端队列中移除的元素
     * @return 如果由于此调用移除了一个元素，则返回 {@code true}
     * @throws ClassCastException 如果指定元素的类与此双端队列不兼容
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为 null
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)
     */
    boolean removeFirstOccurrence(Object o);

    /**
     * 从双端队列中移除指定元素的最后一个出现。如果双端队列不包含该元素，则不改变。
     * 更正式地说，移除最后一个满足 {@code o.equals(e)} 的元素（如果存在这样的元素）。
     * 如果双端队列包含指定的元素（或等效地，如果此调用导致双端队列发生变化），则返回 {@code true}。
     *
     * @param o 如果存在，则从双端队列中移除的元素
     * @return 如果由于此调用移除了一个元素，则返回 {@code true}
     * @throws ClassCastException 如果指定元素的类与此双端队列不兼容
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为 null
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)
     */
    boolean removeLastOccurrence(Object o);

    // *** BlockingQueue 方法 ***

    /**
     * 将指定的元素插入此双端队列表示的队列（换句话说，即此双端队列的尾部），如果可以立即插入而不违反容量限制，则返回
     * {@code true}，如果当前没有可用空间，则抛出 {@code IllegalStateException}。
     * 当使用容量受限的双端队列时，通常更倾向于使用 {@link #offer(Object) offer}。
     *
     * <p>此方法等同于 {@link #addLast(Object) addLast}。
     *
     * @param e 要添加的元素
     * @throws IllegalStateException {@inheritDoc}
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列
     */
    boolean add(E e);

    /**
     * 将指定的元素插入此双端队列表示的队列（换句话说，即此双端队列的尾部），如果可以立即插入而不违反容量限制，则返回
     * {@code true}，如果当前没有可用空间，则返回 {@code false}。当使用容量受限的双端队列时，此方法通常优于
     * {@link #add} 方法，因为后者只能通过抛出异常来失败。
     *
     * <p>此方法等同于 {@link #offerLast(Object) offerLast}。
     *
     * @param e 要添加的元素
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列
     */
    boolean offer(E e);

    /**
     * 将指定的元素插入此双端队列表示的队列（换句话说，即此双端队列的尾部），必要时等待空间可用。
     *
     * <p>此方法等同于 {@link #putLast(Object) putLast}。
     *
     * @param e 要添加的元素
     * @throws InterruptedException {@inheritDoc}
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列
     */
    void put(E e) throws InterruptedException;

    /**
     * 将指定的元素插入此双端队列表示的队列（换句话说，即此双端队列的尾部），必要时等待指定的等待时间，直到空间可用。
     *
     * <p>此方法等同于
     * {@link #offerLast(Object,long,TimeUnit) offerLast}。
     *
     * @param e 要添加的元素
     * @return 如果元素被添加到此双端队列，则返回 {@code true}，否则返回 {@code false}
     * @throws InterruptedException {@inheritDoc}
     * @throws ClassCastException 如果指定元素的类阻止其被添加到此双端队列
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止其被添加到此双端队列
     */
    boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 检索并移除此双端队列表示的队列的头部（换句话说，即此双端队列的第一个元素）。
     * 此方法与 {@link #poll poll} 的唯一区别是，如果此双端队列为空，则抛出异常。
     *
     * <p>此方法等同于 {@link #removeFirst() removeFirst}。
     *
     * @return 此双端队列表示的队列的头部
     * @throws NoSuchElementException 如果此双端队列为空
     */
    E remove();

    /**
     * 检索并移除此双端队列表示的队列的头部（换句话说，即此双端队列的第一个元素），如果此双端队列为空，则返回
     * {@code null}。
     *
     * <p>此方法等同于 {@link #pollFirst()}。
     *
     * @return 此双端队列的头部，如果此双端队列为空，则返回 {@code null}
     */
    E poll();

    /**
     * 检索并移除此双端队列表示的队列的头部（换句话说，即此双端队列的第一个元素），必要时等待，直到有元素可用。
     *
     * <p>此方法等同于 {@link #takeFirst() takeFirst}。
     *
     * @return 此双端队列的头部
     * @throws InterruptedException 如果等待时被中断
     */
    E take() throws InterruptedException;

    /**
     * 检索并移除此双端队列表示的队列的头部（换句话说，即此双端队列的第一个元素），必要时等待指定的等待时间，直到有元素可用。
     *
     * <p>此方法等同于
     * {@link #pollFirst(long,TimeUnit) pollFirst}。
     *
     * @return 此双端队列的头部，如果指定的等待时间过去后没有元素可用，则返回 {@code null}
     * @throws InterruptedException 如果等待时被中断
     */
    E poll(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 检索但不移除此双端队列表示的队列的头部（换句话说，即此双端队列的第一个元素）。
     * 此方法与 {@link #peek peek} 的唯一区别是，如果此双端队列为空，则抛出异常。
     *
     * <p>此方法等同于 {@link #getFirst() getFirst}。
     *
     * @return 此双端队列的头部
     * @throws NoSuchElementException 如果此双端队列为空
     */
    E element();

    /**
     * 检索但不移除此双端队列表示的队列的头部（换句话说，即此双端队列的第一个元素），如果此双端队列为空，则返回
     * {@code null}。
     *
     * <p>此方法等同于 {@link #peekFirst() peekFirst}。
     *
     * @return 此双端队列的头部，如果此双端队列为空，则返回 {@code null}
     */
    E peek();

    /**
     * 从双端队列中移除指定元素的第一个出现。如果双端队列不包含该元素，则不改变。
     * 更正式地说，移除第一个满足 {@code o.equals(e)} 的元素（如果存在这样的元素）。
     * 如果双端队列包含指定的元素（或等效地，如果此调用导致双端队列发生变化），则返回 {@code true}。
     *
     * <p>此方法等同于
     * {@link #removeFirstOccurrence(Object) removeFirstOccurrence}。
     *
     * @param o 如果存在，则从双端队列中移除的元素
     * @return 如果此调用导致双端队列发生变化，则返回 {@code true}
     * @throws ClassCastException 如果指定元素的类与此双端队列不兼容
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为 null
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)
     */
    boolean remove(Object o);

    /**
     * 如果此双端队列包含指定的元素，则返回 {@code true}。
     * 更正式地说，如果此双端队列包含至少一个满足 {@code o.equals(e)} 的元素，则返回 {@code true}。
     *
     * @param o 要检查是否包含在此双端队列中的对象
     * @return 如果此双端队列包含指定的元素，则返回 {@code true}
     * @throws ClassCastException 如果指定元素的类与此双端队列不兼容
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定的元素为 null
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)
     */
    public boolean contains(Object o);

    /**
     * 返回此双端队列中的元素数量。
     *
     * @return 此双端队列中的元素数量
     */
    public int size();

    /**
     * 返回一个按顺序遍历此双端队列中元素的迭代器。元素将按从第一个（头部）到最后一个（尾部）的顺序返回。
     *
     * @return 一个按顺序遍历此双端队列中元素的迭代器
     */
    Iterator<E> iterator();

    // *** 栈方法 ***

    /**
     * 将元素推入此双端队列表示的栈（换句话说，即此双端队列的头部），如果可以立即插入而不违反容量限制，则返回
     * {@code true}，如果当前没有可用空间，则抛出 {@code IllegalStateException}。
     *
     * <p>此方法等同于 {@link #addFirst(Object) addFirst}。
     *
     * @throws IllegalStateException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException 如果指定的元素为 null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    void push(E e);
}
