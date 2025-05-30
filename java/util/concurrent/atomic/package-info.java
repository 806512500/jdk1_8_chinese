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

/**
 * 一个小型工具包，支持单个变量上的无锁线程安全编程。本质上，此包中的类扩展了 {@code volatile} 值、字段和数组元素的概念，以提供形式为：
 *
 *  <pre> {@code boolean compareAndSet(expectedValue, updateValue);}</pre>
 *
 * 的原子条件更新操作。
 *
 * <p>此方法（在不同类中参数类型不同）如果当前变量持有 {@code expectedValue}，则原子地将其设置为 {@code updateValue}，成功时返回 {@code true}。此包中的类还包含获取和无条件设置值的方法，以及以下描述的较弱的条件原子更新操作 {@code weakCompareAndSet}。
 *
 * <p>这些方法的规范允许实现使用现代处理器上可用的高效机器级原子指令。然而，在某些平台上，支持可能涉及某种形式的内部锁定。因此，这些方法不是严格保证为非阻塞的——线程可能在执行操作之前短暂阻塞。
 *
 * <p>{@link java.util.concurrent.atomic.AtomicBoolean}、
 * {@link java.util.concurrent.atomic.AtomicInteger}、
 * {@link java.util.concurrent.atomic.AtomicLong} 和
 * {@link java.util.concurrent.atomic.AtomicReference}
 * 类的每个实例都提供对相应类型单个变量的访问和更新。每个类还提供该类型的适当实用方法。例如，{@code AtomicLong} 和
 * {@code AtomicInteger} 类提供原子递增方法。一个应用是生成序列号，如下所示：
 *
 *  <pre> {@code
 * class Sequencer {
 *   private final AtomicLong sequenceNumber
 *     = new AtomicLong(0);
 *   public long next() {
 *     return sequenceNumber.getAndIncrement();
 *   }
 * }}</pre>
 *
 * <p>定义新的实用函数，如 {@code getAndIncrement}，将函数应用于值原子地，是直接的。例如，给定某个转换
 * <pre> {@code long transform(long input)}</pre>
 *
 * 可以将实用方法编写如下：
 *  <pre> {@code
 * long getAndTransform(AtomicLong var) {
 *   long prev, next;
 *   do {
 *     prev = var.get();
 *     next = transform(prev);
 *   } while (!var.compareAndSet(prev, next));
 *   return prev; // return next; for transformAndGet
 * }}</pre>
 *
 * <p>对原子变量的访问和更新的内存效果通常遵循 {@code volatile} 的规则，如
 * <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.4">
 * 《Java 语言规范（17.4 内存模型）》</a> 中所述：
 *
 * <ul>
 *
 *   <li> {@code get} 具有读取 {@code volatile} 变量的内存效果。
 *
 *   <li> {@code set} 具有写入（赋值）{@code volatile} 变量的内存效果。
 *
 *   <li> {@code lazySet} 具有写入（赋值）{@code volatile} 变量的内存效果，但允许与后续（但不是之前的）不强制重新排序约束的普通非-{@code volatile} 写入重新排序。在某些使用上下文中，例如为了垃圾回收而清空不再访问的引用时，可以使用 {@code lazySet}。
 *
 *   <li> {@code weakCompareAndSet} 原子地读取并有条件地写入变量，但 <em>不</em>
 *   创建任何 happens-before 顺序，因此对目标变量以外的任何其他变量的先前或后续读取和写入不提供任何保证。
 *
 *   <li> {@code compareAndSet}
 *   以及所有其他读取和更新操作（如 {@code getAndIncrement}）具有读取和
 *   写入 {@code volatile} 变量的内存效果。
 * </ul>
 *
 * <p>除了表示单个值的类之外，此包还包含 <em>Updater</em> 类，可以用于获取对任何选定类的任何选定 {@code volatile}
 * 字段的 {@code compareAndSet} 操作。
 *
 * {@link java.util.concurrent.atomic.AtomicReferenceFieldUpdater}、
 * {@link java.util.concurrent.atomic.AtomicIntegerFieldUpdater} 和
 * {@link java.util.concurrent.atomic.AtomicLongFieldUpdater} 是基于反射的实用工具，提供对关联字段类型的访问。这些类主要用于原子数据结构中，其中同一节点的多个 {@code volatile} 字段（例如，树节点的链接）独立地受原子更新。这些类在如何和何时使用原子更新方面提供了更大的灵活性，但以更复杂的基于反射的设置、不太方便的使用和较弱的保证为代价。
 *
 * <p>{@link java.util.concurrent.atomic.AtomicIntegerArray}、
 * {@link java.util.concurrent.atomic.AtomicLongArray} 和
 * {@link java.util.concurrent.atomic.AtomicReferenceArray} 类进一步扩展了对这些类型数组的原子操作支持。这些类还值得注意的是，它们为数组元素提供了 {@code volatile} 访问语义，而普通数组不支持这一点。
 *
 * <p id="weakCompareAndSet">原子类还支持方法
 * {@code weakCompareAndSet}，其适用范围有限。在某些平台上，弱版本可能在正常情况下比 {@code
 * compareAndSet} 更高效，但不同之处在于任何给定的 {@code weakCompareAndSet} 方法调用都可能 <em>无缘无故地</em> 返回 {@code
 * false}（即，没有任何明显的原因）。{@code false} 返回值仅表示如果需要，可以重试操作，依赖于当变量持有 {@code expectedValue} 且没有其他线程也在尝试设置变量时，重复调用最终会成功。例如，这种无故失败可能是由于与预期值和当前值是否相等无关的内存争用效应。此外，{@code weakCompareAndSet} 不提供通常用于同步控制的顺序保证。然而，当此类更新与程序的其他 happens-before 顺序无关时，例如更新性能统计信息时，该方法可能很有用。当线程看到由 {@code weakCompareAndSet} 引起的原子变量更新时，它不一定看到在此之前的任何 <em>其他</em> 变量的更新。例如，在更新性能统计信息时，这可能是可以接受的，但通常情况下则不然。
 *
 * <p>{@link java.util.concurrent.atomic.AtomicMarkableReference}
 * 类将一个布尔值与一个引用关联。例如，这个位可以用于表示逻辑上已删除的对象。
 *
 * {@link java.util.concurrent.atomic.AtomicStampedReference}
 * 类将一个整数值与一个引用关联。例如，这可以用于表示与一系列更新相对应的版本号。
 *
 * <p>原子类主要设计为实现无锁数据结构和相关基础设施类的构建块。{@code compareAndSet} 方法不是锁定的一般替代品。它仅适用于对象的关键更新局限于 <em>单个</em> 变量的情况。
 *
 * <p>原子类不是 {@code java.lang.Integer} 和相关类的一般替代品。它们 <em>不</em>
 * 定义 {@code equals}、{@code hashCode} 和 {@code compareTo} 方法。（因为期望原子变量会被修改，所以它们作为哈希表键是不合适的。）此外，仅提供在预期应用中常用的类型类。例如，没有用于表示 {@code byte} 的原子类。在那些很少需要的情况下，可以使用 {@code AtomicInteger} 来保存 {@code byte} 值，并适当转换。
 *
 * 您还可以使用
 * {@link java.lang.Float#floatToRawIntBits} 和
 * {@link java.lang.Float#intBitsToFloat} 转换来保存浮点数，以及使用
 * {@link java.lang.Double#doubleToRawLongBits} 和
 * {@link java.lang.Double#longBitsToDouble} 转换来保存双精度浮点数。
 *
 * @since 1.5
 */
package java.util.concurrent.atomic;
