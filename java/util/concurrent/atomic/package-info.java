/*
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

/*
 *
 *
 *
 *
 *
 * 由 Doug Lea 在 JCP JSR-166 专家组成员的帮助下编写，并按照
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的方式发布到公共领域。
 */

/**
 * 一个小型工具类集合，支持单个变量上的无锁线程安全编程。本质上，此包中的类扩展了 {@code volatile} 值、字段和数组元素的概念，以提供以下形式的原子条件更新操作：
 *
 *  <pre> {@code boolean compareAndSet(expectedValue, updateValue);}</pre>
 *
 * <p>此方法（在不同类中参数类型不同）原子地将变量设置为 {@code updateValue}，如果它当前持有 {@code expectedValue}，则报告 {@code true}。此包中的类还包含获取和无条件设置值的方法，以及下面描述的较弱的条件原子更新操作 {@code weakCompareAndSet}。
 *
 * <p>这些方法的规范允许实现利用现代处理器上可用的高效机器级原子指令。然而，在某些平台上，支持可能涉及某种形式的内部锁定。因此，这些方法严格来说并不是非阻塞的——线程可能在执行操作之前短暂阻塞。
 *
 * <p>类 {@link java.util.concurrent.atomic.AtomicBoolean}、
 * {@link java.util.concurrent.atomic.AtomicInteger}、
 * {@link java.util.concurrent.atomic.AtomicLong} 和
 * {@link java.util.concurrent.atomic.AtomicReference}
 * 每个类都提供对相应类型单个变量的访问和更新。每个类还提供该类型的适当实用方法。例如，类 {@code AtomicLong} 和
 * {@code AtomicInteger} 提供原子递增方法。一个应用是生成序列号，如下所示：
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
 * <p>定义新的实用函数，如 {@code getAndIncrement}，应用一个函数原子地，是直接的。例如，给定某个转换
 * <pre> {@code long transform(long input)}</pre>
 *
 * 将您的实用方法编写如下：
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
 * <p>对原子变量的访问和更新的内存效果通常遵循 volatile 变量的规则，如
 * <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.4">
 * Java 语言规范 (17.4 内存模型)</a> 中所述：
 *
 * <ul>
 *
 *   <li> {@code get} 具有读取 {@code volatile} 变量的内存效果。
 *
 *   <li> {@code set} 具有写入（赋值）{@code volatile} 变量的内存效果。
 *
 *   <li> {@code lazySet} 具有写入（赋值）{@code volatile} 变量的内存效果，但允许与后续（但不是先前）不强制重新排序约束的普通非-{@code volatile} 写入重新排序。在某些使用上下文中，例如为了垃圾回收而置空一个不再访问的引用，可以使用 {@code lazySet}。
 *
 *   <li>{@code weakCompareAndSet} 原子地读取并有条件地写入变量，但<em>不</em>
 *   创建任何 happens-before 顺序，因此对目标变量以外的任何其他变量的先前或后续读取和写入不提供任何保证。
 *
 *   <li> {@code compareAndSet}
 *   和所有其他读取和更新操作，如 {@code getAndIncrement}，具有读取和
 *   写入 {@code volatile} 变量的内存效果。
 * </ul>
 *
 * <p>除了表示单个值的类之外，此包还包含 <em>Updater</em> 类，可用于获取任何选定类的任何选定 {@code volatile} 字段的 {@code compareAndSet} 操作。
 *
 * {@link java.util.concurrent.atomic.AtomicReferenceFieldUpdater}、
 * {@link java.util.concurrent.atomic.AtomicIntegerFieldUpdater} 和
 * {@link java.util.concurrent.atomic.AtomicLongFieldUpdater} 是基于反射的实用工具，提供对关联字段类型的访问。这些类主要用于原子数据结构，其中同一节点的多个 {@code volatile} 字段（例如，树节点的链接）独立地受到原子更新。这些类使如何以及何时使用原子更新具有更大的灵活性，但代价是更复杂的基于反射的设置、更不方便的使用和较弱的保证。
 *
 * <p>
 * {@link java.util.concurrent.atomic.AtomicIntegerArray}、
 * {@link java.util.concurrent.atomic.AtomicLongArray} 和
 * {@link java.util.concurrent.atomic.AtomicReferenceArray} 类
 * 进一步扩展了对这些类型数组的原子操作支持。这些类还值得注意的是，它们为数组元素提供了 {@code volatile} 访问语义，而普通数组不支持这一点。
 *
 * <p id="weakCompareAndSet">原子类还支持方法
 * {@code weakCompareAndSet}，其适用范围有限。在某些平台上，弱版本在正常情况下可能比 {@code
 * compareAndSet} 更高效，但不同之处在于任何给定的
 * {@code weakCompareAndSet} 方法调用可能<em>无缘无故地</em>返回 {@code
 * false}（即，没有明显的原因）。{@code false} 返回仅意味着如果需要，可以重试该操作，依赖于当变量持有 {@code expectedValue} 且没有其他线程也在尝试设置变量时，重复调用最终会成功。此类无故失败可能是由于与预期值和当前值是否相等无关的内存争用效应。此外，{@code weakCompareAndSet} 不提供通常用于同步控制的顺序保证。然而，当更新与程序的其他 happens-before 顺序无关的计数器和统计信息时，此方法可能有用。当一个线程看到由 {@code weakCompareAndSet} 引起的原子变量更新时，它不一定看到在此之前的其他变量的任何更新。当例如更新性能统计信息时，这可能是可以接受的，但通常情况下则不然。
 *
 * <p>类 {@link java.util.concurrent.atomic.AtomicMarkableReference}
 * 将一个布尔值与一个引用关联。例如，这个位可以用于数据结构中，表示被引用的对象在逻辑上已被删除。
 *
 * 类 {@link java.util.concurrent.atomic.AtomicStampedReference}
 * 将一个整数值与一个引用关联。例如，这可以用于表示与一系列更新相对应的版本号。
 *
 * <p>原子类主要设计为实现非阻塞数据结构和相关基础设施类的构建块。{@code compareAndSet} 方法不是锁定的一般替代品。它仅适用于对象的关键更新局限于<em>单个</em>变量的情况。
 *
 * <p>原子类不是 {@code java.lang.Integer} 等类的一般替代品。它们<em>不</em>
 * 定义 {@code equals}、{@code hashCode} 和 {@code compareTo} 等方法。（因为原子变量预计会被修改，所以它们作为哈希表键是不合适的。）此外，仅提供在预期应用中常用类型的类。例如，没有表示 {@code byte} 的原子类。在少数情况下，如果您希望这样做，可以使用 {@code AtomicInteger} 来保存 {@code byte} 值，并适当转换。
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
