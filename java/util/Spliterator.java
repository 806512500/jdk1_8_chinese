
/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * 用于遍历和分区源元素的对象。源元素可以是数组、{@link Collection}、IO通道或生成器函数。
 *
 * <p>Spliterator 可以单独遍历元素（{@link
 * #tryAdvance tryAdvance()}）或批量顺序遍历（{@link #forEachRemaining forEachRemaining()}）。
 *
 * <p>Spliterator 还可以将部分元素（使用
 * {@link #trySplit}）分区为另一个 Spliterator，用于可能的并行操作。如果 Spliterator 无法拆分，或者拆分方式不平衡或效率低下，那么并行操作不太可能受益。遍历和拆分会耗尽元素；每个 Spliterator 仅适用于单个批量计算。
 *
 * <p>Spliterator 还报告其结构、源和元素的一组特性，包括 {@link #ORDERED}、
 * {@link #DISTINCT}、{@link #SORTED}、{@link #SIZED}、{@link #NONNULL}、
 * {@link #IMMUTABLE}、{@link #CONCURRENT} 和 {@link #SUBSIZED}。这些特性可以被 Spliterator 客户端用于控制、专门化或简化计算。例如，用于 {@link Collection} 的 Spliterator 会报告 {@code SIZED}，用于 {@link Set} 的 Spliterator 会报告
 * {@code DISTINCT}，用于 {@link SortedSet} 的 Spliterator 还会报告 {@code SORTED}。特性以简单的联合位集形式报告。
 *
 * 一些特性还约束方法行为；例如，如果报告了 {@code ORDERED}，遍历方法必须符合其文档中描述的顺序。未来可能会定义新的特性，因此实现者不应为未列出的值分配含义。
 *
 * <p><a name="binding">如果 Spliterator 不报告 {@code IMMUTABLE} 或
 * {@code CONCURRENT}，则应具有关于以下方面的文档化策略：Spliterator <em>绑定</em>到元素源的时间；以及在绑定后检测到的元素源结构干扰。</a> <em>延迟绑定</em> 的 Spliterator 在首次遍历、首次拆分或首次查询估计大小时绑定到元素源，而不是在创建 Spliterator 时绑定。不是 <em>延迟绑定</em> 的 Spliterator 在构造或首次调用任何方法时绑定到元素源。在绑定之前对源进行的修改将在遍历时反映。绑定后，Spliterator 应在尽力的基础上，如果检测到结构干扰，则抛出
 * {@link ConcurrentModificationException}。这样做的是 <em>快速失败</em> 的 Spliterator。批量遍历方法（{@link #forEachRemaining forEachRemaining()}）可以在遍历所有元素后优化遍历并检查结构干扰，而不是逐个元素检查并立即失败。
 *
 * <p>Spliterator 可以通过 {@link #estimateSize} 方法提供剩余元素数量的估计。理想情况下，如特性
 * {@link #SIZED} 所反映的，这个值与成功遍历时将遇到的元素数量完全对应。然而，即使不完全知道，估计值仍然可能对在源上执行的操作有用，例如帮助确定是否更优选进一步拆分或顺序遍历剩余元素。
 *
 * <p>尽管在并行算法中非常有用，但 Spliterator 不期望是线程安全的；相反，使用 Spliterator 实现并行算法应确保 Spliterator 同一时间只被一个线程使用。这通常通过 <em>串行线程限制</em> 来实现，这通常是典型通过递归分解工作的并行算法的自然结果。调用 {@link #trySplit()} 的线程可以将返回的 Spliterator 交给另一个线程，该线程可以遍历或进一步拆分该 Spliterator。如果两个或多个线程同时操作同一个 Spliterator，拆分和遍历的行为是未定义的。如果原始线程将 Spliterator 交给另一个线程进行处理，最好在使用 {@link #tryAdvance(Consumer)
 * tryAdvance()} 消耗任何元素之前进行移交，因为某些保证（如 {@code SIZED} Spliterator 的 {@link #estimateSize()} 的准确性）仅在遍历开始前有效。
 *
 * <p>提供了 {@code Spliterator} 的原始子类型特化，用于 {@link OfInt int}、{@link OfLong long} 和 {@link OfDouble double} 值。子类型默认实现
 * {@link Spliterator#tryAdvance(java.util.function.Consumer)}
 * 和 {@link Spliterator#forEachRemaining(java.util.function.Consumer)} 会将原始值装箱为其对应包装类的实例。这种装箱可能会削弱使用原始特化的性能优势。为了避免装箱，应使用相应的基于原始值的方法。例如，
 * {@link Spliterator.OfInt#tryAdvance(java.util.function.IntConsumer)}
 * 和 {@link Spliterator.OfInt#forEachRemaining(java.util.function.IntConsumer)}
 * 应优先于
 * {@link Spliterator.OfInt#tryAdvance(java.util.function.Consumer)} 和
 * {@link Spliterator.OfInt#forEachRemaining(java.util.function.Consumer)} 使用。使用基于装箱的方法
 * {@link #tryAdvance tryAdvance()} 和
 * {@link #forEachRemaining(java.util.function.Consumer) forEachRemaining()}
 * 遍历原始值不会影响值转换为装箱值后的顺序。
 *
 * @apiNote
 * <p>Spliterator，像 {@code Iterator} 一样，用于遍历源的元素。{@code Spliterator} API 被设计为支持高效的并行遍历以及顺序遍历，通过支持分解以及单元素迭代。此外，通过 Spliterator 访问元素的协议设计为比 {@code Iterator} 的单元素开销更小，并避免了 {@code hasNext()} 和
 * {@code next()} 方法之间的固有竞争。
 *
 * <p>对于可变源，如果在 Spliterator 绑定到数据源和遍历结束之间对源进行结构干扰（添加、替换或删除元素），可能会发生任意和不确定的行为。例如，使用 {@code java.util.stream} 框架时，这种干扰会产生任意的、不确定的结果。
 *
 * <p>可以通过以下方式管理源的结构干扰（按大致的可取性顺序）：
 * <ul>
 * <li>源不能进行结构干扰。
 * <br>例如，{@link java.util.concurrent.CopyOnWriteArrayList} 的实例是一个不可变源。从该源创建的 Spliterator 报告 {@code IMMUTABLE} 特性。</li>
 * <li>源管理并发修改。
 * <br>例如，{@link java.util.concurrent.ConcurrentHashMap} 的键集是一个并发源。从该源创建的 Spliterator 报告 {@code CONCURRENT} 特性。</li>
 * <li>可变源提供延迟绑定和快速失败的 Spliterator。
 * <br>延迟绑定缩小了干扰可能影响计算的时间窗口；快速失败在尽力的基础上检测到遍历开始后发生的结构干扰并抛出
 * {@link ConcurrentModificationException}。例如，{@link ArrayList} 和 JDK 中许多其他非并发的 {@code Collection} 类提供了延迟绑定、快速失败的 Spliterator。</li>
 * <li>可变源提供非延迟绑定但快速失败的 Spliterator。
 * <br>源增加了抛出 {@code ConcurrentModificationException} 的可能性，因为潜在干扰的时间窗口更大。</li>
 * <li>可变源提供延迟绑定但非快速失败的 Spliterator。
 * <br>源在遍历开始后存在任意的、不确定的行为风险，因为干扰未被检测。</li>
 * <li>可变源提供非延迟绑定且非快速失败的 Spliterator。
 * <br>源增加了任意的、不确定的行为风险，因为未检测到的干扰可能在构造后发生。</li>
 * </ul>
 *
 * <p><b>示例。</b>以下是一个类（除了说明外，这个类本身并不非常有用），该类维护一个数组，实际数据存储在偶数位置，无关的标签数据存储在奇数位置。其 Spliterator 忽略这些标签。
 *
 * <pre> {@code
 * class TaggedArray<T> {
 *   private final Object[] elements; // 构造后不可变
 *   TaggedArray(T[] data, Object[] tags) {
 *     int size = data.length;
 *     if (tags.length != size) throw new IllegalArgumentException();
 *     this.elements = new Object[2 * size];
 *     for (int i = 0, j = 0; i < size; ++i) {
 *       elements[j++] = data[i];
 *       elements[j++] = tags[i];
 *     }
 *   }
 *
 *   public Spliterator<T> spliterator() {
 *     return new TaggedArraySpliterator<>(elements, 0, elements.length);
 *   }
 *
 *   static class TaggedArraySpliterator<T> implements Spliterator<T> {
 *     private final Object[] array;
 *     private int origin; // 当前索引，在拆分或遍历时前进
 *     private final int fence; // 最大索引的下一个位置
 *
 *     TaggedArraySpliterator(Object[] array, int origin, int fence) {
 *       this.array = array; this.origin = origin; this.fence = fence;
 *     }
 *
 *     public void forEachRemaining(Consumer<? super T> action) {
 *       for (; origin < fence; origin += 2)
 *         action.accept((T) array[origin]);
 *     }
 *
 *     public boolean tryAdvance(Consumer<? super T> action) {
 *       if (origin < fence) {
 *         action.accept((T) array[origin]);
 *         origin += 2;
 *         return true;
 *       }
 *       else // 无法前进
 *         return false;
 *     }
 *
 *     public Spliterator<T> trySplit() {
 *       int lo = origin; // 将范围分成两半
 *       int mid = ((lo + fence) >>> 1) & ~1; // 强制中点为偶数
 *       if (lo < mid) { // 拆分出左半部分
 *         origin = mid; // 重置此 Spliterator 的起始位置
 *         return new TaggedArraySpliterator<>(array, lo, mid);
 *       }
 *       else       // 太小而无法拆分
 *         return null;
 *     }
 *
 *     public long estimateSize() {
 *       return (long)((fence - origin) / 2);
 *     }
 *
 *     public int characteristics() {
 *       return ORDERED | SIZED | IMMUTABLE | SUBSIZED;
 *     }
 *   }
 * }}</pre>
 *
 * <p>作为并行计算框架（如 {@code java.util.stream} 包）如何在并行计算中使用 Spliterator 的示例，以下是一种实现关联的并行 forEach 的方法，说明了拆分子任务直到估计的工作量足够小以顺序执行的主要使用模式。这里假设跨子任务的处理顺序不重要；不同的（分叉的）任务可以进一步拆分并以不确定的顺序并发处理元素。此示例使用了 {@link java.util.concurrent.CountedCompleter}；
 * 类似的用法适用于其他并行任务构造。
 *
 * <pre>{@code
 * static <T> void parEach(TaggedArray<T> a, Consumer<T> action) {
 *   Spliterator<T> s = a.spliterator();
 *   long targetBatchSize = s.estimateSize() / (ForkJoinPool.getCommonPoolParallelism() * 8);
 *   new ParEach(null, s, action, targetBatchSize).invoke();
 * }
 *
 * static class ParEach<T> extends CountedCompleter<Void> {
 *   final Spliterator<T> spliterator;
 *   final Consumer<T> action;
 *   final long targetBatchSize;
 *
 *   ParEach(ParEach<T> parent, Spliterator<T> spliterator,
 *           Consumer<T> action, long targetBatchSize) {
 *     super(parent);
 *     this.spliterator = spliterator; this.action = action;
 *     this.targetBatchSize = targetBatchSize;
 *   }
 *
 *   public void compute() {
 *     Spliterator<T> sub;
 *     while (spliterator.estimateSize() > targetBatchSize &&
 *            (sub = spliterator.trySplit()) != null) {
 *       addToPendingCount(1);
 *       new ParEach<>(this, sub, action, targetBatchSize).fork();
 *     }
 *     spliterator.forEachRemaining(action);
 *     propagateCompletion();
 *   }
 * }}</pre>
 *
 * @implNote
 * 如果系统属性 {@code org.openjdk.java.util.stream.tripwire} 设置为 {@code true}，则会在操作原始子类型特化时发生装箱时报告诊断警告。
 *
 * @param <T> 此 Spliterator 返回的元素类型
 *
 * @see Collection
 * @since 1.8
 */
public interface Spliterator<T> {
    /**
     * 如果存在剩余元素，则在其上执行给定操作，返回 {@code true}；否则返回 {@code false}。如果此
     * Spliterator 是 {@link #ORDERED} 的，则操作将在遇到顺序的下一个元素上执行。操作中抛出的异常将传递给调用者。
     *
     * @param action 操作
     * @return 如果进入此方法时没有剩余元素，则返回 {@code false}，否则返回 {@code true}。
     * @throws NullPointerException 如果指定的操作为 null
     */
    boolean tryAdvance(Consumer<? super T> action);


                /**
     * 对每个剩余元素执行给定的操作，顺序地在当前线程中，直到所有元素都被处理或操作抛出异常。如果此
     * Spliterator 是 {@link #ORDERED}，则操作按遇到顺序执行。操作抛出的异常将传递给调用者。
     *
     * @implSpec
     * 默认实现重复调用 {@link #tryAdvance} 直到返回 {@code false}。应尽可能覆盖此实现。
     *
     * @param action 操作
     * @throws NullPointerException 如果指定的操作为 null
     */
    default void forEachRemaining(Consumer<? super T> action) {
        do { } while (tryAdvance(action));
    }

    /**
     * 如果此 Spliterator 可以分区，则返回一个覆盖元素的 Spliterator，这些元素在调用此方法后将不再
     * 被此 Spliterator 覆盖。
     *
     * <p>如果此 Spliterator 是 {@link #ORDERED}，则返回的 Spliterator 必须覆盖元素的严格前缀。
     *
     * <p>除非此 Spliterator 覆盖无限数量的元素，否则对 {@code trySplit()} 的重复调用最终必须返回
     * {@code null}。在非空返回时：
     * <ul>
     * <li>在拆分前 {@code estimateSize()} 报告的值，在拆分后必须大于或等于此 Spliterator 和返回的
     * Spliterator 的 {@code estimateSize()} 之和；</li>
     * <li>如果此 Spliterator 是 {@code SUBSIZED}，则在拆分前 {@code estimateSize()} 必须等于拆分后
     * 此 Spliterator 和返回的 Spliterator 的 {@code estimateSize()} 之和。</li>
     * </ul>
     *
     * <p>此方法可能因任何原因返回 {@code null}，包括为空、在遍历开始后无法拆分、数据结构约束和效率
     * 考虑。
     *
     * @apiNote
     * 理想的 {@code trySplit} 方法高效（无需遍历）地将元素精确分成两半，允许平衡的并行计算。许多偏离
     * 这一理想的实现仍然非常有效；例如，仅近似分割近似平衡的树，或者对于叶节点可能包含一个或两个元素的树，
     * 无法进一步分割这些节点。然而，平衡和/或过于低效的 {@code trySplit} 机制通常会导致并行性能较差。
     *
     * @return 覆盖部分元素的 {@code Spliterator}，如果此 Spliterator 无法拆分则返回 {@code null}
     */
    Spliterator<T> trySplit();

    /**
     * 返回 {@link #forEachRemaining} 遍历将遇到的元素数量的估计值，如果无限、未知或计算成本过高，则返回
     * {@link Long#MAX_VALUE}。
     *
     * <p>如果此 Spliterator 是 {@link #SIZED} 且尚未部分遍历或拆分，或者此 Spliterator 是 {@link #SUBSIZED}
     * 且尚未部分遍历，则此估计值必须是完整遍历将遇到的元素的确切数量。否则，此估计值可能任意不准确，但必须
     * 按照 {@link #trySplit} 调用的规范减少。
     *
     * @apiNote
     * 即使是不精确的估计也通常是实用且计算成本低的。例如，近似平衡的二叉树的子 Spliterator 可能返回
     * 估计的元素数量是其父节点的一半；如果根 Spliterator 不维护准确的计数，它可以将大小估计为对应于其最大
     * 深度的二的幂。
     *
     * @return 估计的大小，如果无限、未知或计算成本过高，则返回 {@code Long.MAX_VALUE}。
     */
    long estimateSize();

    /**
     * 方便方法，如果此 Spliterator 是 {@link #SIZED}，则返回 {@link #estimateSize()}，否则返回 {@code -1}。
     * @implSpec
     * 默认实现如果 Spliterator 报告 {@code SIZED} 特性，则返回 {@code estimateSize()} 的结果，否则返回
     * {@code -1}。
     *
     * @return 如果已知则返回确切大小，否则返回 {@code -1}。
     */
    default long getExactSizeIfKnown() {
        return (characteristics() & SIZED) == 0 ? -1L : estimateSize();
    }

    /**
     * 返回此 Spliterator 及其元素的一组特性。结果表示为从 {@link #ORDERED}、{@link #DISTINCT}、
     * {@link #SORTED}、{@link #SIZED}、{@link #NONNULL}、{@link #IMMUTABLE}、{@link #CONCURRENT}、
     * {@link #SUBSIZED} 中的 OR 值。在给定 Spliterator 上调用 {@code characteristics()} 之前或
     * 之间调用 {@code trySplit} 时，重复调用应始终返回相同的结果。
     *
     * <p>如果 Spliterator 报告不一致的特性集（无论是单次调用还是多次调用的结果），则无法保证使用此
     * Spliterator 的任何计算。
     *
     * @apiNote 在拆分前和拆分后的 Spliterator 的特性可能不同。具体示例见特性值 {@link #SIZED}、
     * {@link #SUBSIZED} 和 {@link #CONCURRENT}。
     *
     * @return 特性的表示
     */
    int characteristics();

    /**
     * 如果此 Spliterator 的 {@link #characteristics} 包含所有给定的特性，则返回 {@code true}。
     *
     * @implSpec
     * 默认实现如果给定特性的相应位已设置，则返回 true。
     *
     * @param characteristics 要检查的特性
     * @return 如果所有指定的特性都存在，则返回 {@code true}，否则返回 {@code false}
     */
    default boolean hasCharacteristics(int characteristics) {
        return (characteristics() & characteristics) == characteristics;
    }


                /**
     * 如果此 Spliterator 的源由 {@link #SORTED} 按 {@link Comparator} 排序，
     * 则返回该 {@code Comparator}。如果源按 {@linkplain Comparable 自然顺序} 排序，
     * 则返回 {@code null}。否则，如果源未排序，则抛出 {@link IllegalStateException}。
     *
     * @implSpec
     * 默认实现总是抛出 {@link IllegalStateException}。
     *
     * @return 一个 Comparator，如果元素按自然顺序排序，则返回 {@code null}。
     * @throws IllegalStateException 如果 Spliterator 没有报告 {@code SORTED} 特性。
     */
    default Comparator<? super T> getComparator() {
        throw new IllegalStateException();
    }

    /**
     * 特性值表示元素定义了遍历顺序。如果如此，此 Spliterator 保证方法
     * {@link #trySplit} 分割元素的严格前缀，方法 {@link #tryAdvance} 按前缀顺序前进一个元素，
     * 以及 {@link #forEachRemaining} 按遍历顺序执行操作。
     *
     * <p>如果对应的 {@link Collection#iterator} 文档中指定了顺序，则 {@link Collection} 具有遍历顺序。
     * 如果如此，遍历顺序与文档中指定的顺序相同。否则，集合没有遍历顺序。
     *
     * @apiNote 任何 {@link List} 的遍历顺序保证为升序索引顺序。但对于基于哈希的集合（如 {@link HashSet}），
     * 不保证任何顺序。报告 {@code ORDERED} 的 Spliterator 的客户端在非交换并行计算中应保持顺序约束。
     */
    public static final int ORDERED    = 0x00000010;

    /**
     * 特性值表示对于每对遇到的元素 {@code x, y}，{@code !x.equals(y)}。例如，
     * 基于 {@link Set} 的 Spliterator 适用此特性。
     */
    public static final int DISTINCT   = 0x00000001;

    /**
     * 特性值表示遍历顺序遵循定义的排序顺序。如果如此，方法 {@link #getComparator()} 返回关联的
     * Comparator，如果所有元素都是 {@link Comparable} 并按自然顺序排序，则返回 {@code null}。
     *
     * <p>报告 {@code SORTED} 的 Spliterator 必须也报告 {@code ORDERED}。
     *
     * @apiNote JDK 中实现 {@link NavigableSet} 或 {@link SortedSet} 的 {@code Collection} 类的 Spliterator
     * 报告 {@code SORTED}。
     */
    public static final int SORTED     = 0x00000004;

    /**
     * 特性值表示在遍历或分割之前从 {@code estimateSize()} 返回的值表示一个有限大小，
     * 在没有结构源修改的情况下，表示完整遍历将遇到的元素的确切数量。
     *
     * @apiNote 大多数覆盖 {@code Collection} 所有元素的 Collection 的 Spliterator 报告此特性。
     * 例如，覆盖元素子集并近似报告其大小的子 Spliterator（如 {@link HashSet} 的子 Spliterator）不报告此特性。
     */
    public static final int SIZED      = 0x00000040;

    /**
     * 特性值表示源保证遇到的元素不会为 {@code null}。（例如，大多数并发集合、队列和映射适用此特性。）
     */
    public static final int NONNULL    = 0x00000100;

    /**
     * 特性值表示源保证在遍历期间不会发生结构修改；即，不能添加、替换或删除元素，因此不会发生此类更改。
     * 如果 Spliterator 不报告 {@code IMMUTABLE} 或 {@code CONCURRENT}，则应有文档记录的策略（例如抛出
     * {@link ConcurrentModificationException}）来处理在遍历期间检测到的结构干扰。
     */
    public static final int IMMUTABLE  = 0x00000400;

    /**
     * 特性值表示元素源可以由多个线程安全地并发修改（允许添加、替换和/或删除）而无需外部同步。
     * 如果如此，Spliterator 应有文档记录的策略来说明在遍历期间修改的影响。
     *
     * <p>顶级 Spliterator 不应同时报告 {@code CONCURRENT} 和 {@code SIZED}，因为如果在遍历期间并发修改源，
     * 已知的有限大小可能会改变。这样的 Spliterator 是不一致的，无法保证使用该 Spliterator 的任何计算。
     * 如果子分割的大小已知且在遍历时不会反映对源的添加或删除，则子 Spliterator 可以报告 {@code SIZED}。
     *
     * @apiNote 大多数并发集合维护一致性策略，保证相对于在 Spliterator 构造时存在的元素的准确性，
     * 但可能不反映随后的添加或删除。
     */
    public static final int CONCURRENT = 0x00001000;

    /**
     * 特性值表示从 {@code trySplit()} 生成的所有 Spliterator 都将同时是 {@link #SIZED} 和 {@link #SUBSIZED}。
     * （这意味着所有直接或间接的子 Spliterator 都将是 {@code SIZED}。）
     *
     * <p>如果 Spliterator 没有按 {@code SUBSIZED} 要求报告 {@code SIZED}，则该 Spliterator 是不一致的，
     * 无法保证使用该 Spliterator 的任何计算。
     *
     * @apiNote 例如，大约平衡的二叉树的顶级 Spliterator 可能报告 {@code SIZED} 但不报告 {@code SUBSIZED}，
     * 因为通常知道整个树的大小，但不知道子树的确切大小。
     */
    public static final int SUBSIZED = 0x00004000;


                /**
     * 一个专用于原始值的Spliterator。
     *
     * @param <T> 此Spliterator返回的元素类型。类型必须是原始类型的包装类型，例如 {@code Integer}
     * 对于原始 {@code int} 类型。
     * @param <T_CONS> 原始消费者类型。类型必须是 {@link java.util.function.Consumer} 的原始特化类型
     * 对于 {@code T}，例如 {@link java.util.function.IntConsumer} 对于 {@code Integer}。
     * @param <T_SPLITR> 原始Spliterator类型。类型必须是 {@code T} 的Spliterator的原始特化类型，例如
     * {@link Spliterator.OfInt} 对于 {@code Integer}。
     *
     * @see Spliterator.OfInt
     * @see Spliterator.OfLong
     * @see Spliterator.OfDouble
     * @since 1.8
     */
    public interface OfPrimitive<T, T_CONS, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>>
            extends Spliterator<T> {
        @Override
        T_SPLITR trySplit();

        /**
         * 如果存在剩余元素，则在其上执行给定的操作，返回 {@code true}；否则返回 {@code false}。如果此
         * Spliterator 是 {@link #ORDERED}，则操作在遇到顺序的下一个元素上执行。操作中抛出的异常将传递给调用者。
         *
         * @param action 操作
         * @return 如果进入此方法时没有剩余元素，则返回 {@code false}，否则返回 {@code true}。
         * @throws NullPointerException 如果指定的操作为 null
         */
        @SuppressWarnings("overloads")
        boolean tryAdvance(T_CONS action);

        /**
         * 顺序地在当前线程中对每个剩余元素执行给定的操作，直到所有元素都被处理或操作抛出异常。如果此Spliterator是 {@link #ORDERED}，
         * 操作将按遇到顺序执行。操作中抛出的异常将传递给调用者。
         *
         * @implSpec
         * 默认实现反复调用 {@link #tryAdvance} 直到返回 {@code false}。应尽可能地覆盖此方法。
         *
         * @param action 操作
         * @throws NullPointerException 如果指定的操作为 null
         */
        @SuppressWarnings("overloads")
        default void forEachRemaining(T_CONS action) {
            do { } while (tryAdvance(action));
        }
    }

    /**
     * 一个专用于 {@code int} 值的Spliterator。
     * @since 1.8
     */
    public interface OfInt extends OfPrimitive<Integer, IntConsumer, OfInt> {

        @Override
        OfInt trySplit();

        @Override
        boolean tryAdvance(IntConsumer action);

        @Override
        default void forEachRemaining(IntConsumer action) {
            do { } while (tryAdvance(action));
        }

        /**
         * {@inheritDoc}
         * @implSpec
         * 如果操作是 {@code IntConsumer} 的实例，则将其转换为 {@code IntConsumer} 并传递给
         * {@link #tryAdvance(java.util.function.IntConsumer)}；否则，将操作适配为 {@code IntConsumer} 的实例，
         * 通过装箱 {@code IntConsumer} 的参数，然后传递给 {@link #tryAdvance(java.util.function.IntConsumer)}。
         */
        @Override
        default boolean tryAdvance(Consumer<? super Integer> action) {
            if (action instanceof IntConsumer) {
                return tryAdvance((IntConsumer) action);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                                  "{0} calling Spliterator.OfInt.tryAdvance((IntConsumer) action::accept)");
                return tryAdvance((IntConsumer) action::accept);
            }
        }

        /**
         * {@inheritDoc}
         * @implSpec
         * 如果操作是 {@code IntConsumer} 的实例，则将其转换为 {@code IntConsumer} 并传递给
         * {@link #forEachRemaining(java.util.function.IntConsumer)}；否则，将操作适配为 {@code IntConsumer} 的实例，
         * 通过装箱 {@code IntConsumer} 的参数，然后传递给 {@link #forEachRemaining(java.util.function.IntConsumer)}。
         */
        @Override
        default void forEachRemaining(Consumer<? super Integer> action) {
            if (action instanceof IntConsumer) {
                forEachRemaining((IntConsumer) action);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                                  "{0} calling Spliterator.OfInt.forEachRemaining((IntConsumer) action::accept)");
                forEachRemaining((IntConsumer) action::accept);
            }
        }
    }

    /**
     * 一个专用于 {@code long} 值的Spliterator。
     * @since 1.8
     */
    public interface OfLong extends OfPrimitive<Long, LongConsumer, OfLong> {

        @Override
        OfLong trySplit();

        @Override
        boolean tryAdvance(LongConsumer action);

        @Override
        default void forEachRemaining(LongConsumer action) {
            do { } while (tryAdvance(action));
        }

        /**
         * {@inheritDoc}
         * @implSpec
         * 如果操作是 {@code LongConsumer} 的实例，则将其转换为 {@code LongConsumer} 并传递给
         * {@link #tryAdvance(java.util.function.LongConsumer)}；否则，将操作适配为 {@code LongConsumer} 的实例，
         * 通过装箱 {@code LongConsumer} 的参数，然后传递给 {@link #tryAdvance(java.util.function.LongConsumer)}。
         */
        @Override
        default boolean tryAdvance(Consumer<? super Long> action) {
            if (action instanceof LongConsumer) {
                return tryAdvance((LongConsumer) action);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                                  "{0} calling Spliterator.OfLong.tryAdvance((LongConsumer) action::accept)");
                return tryAdvance((LongConsumer) action::accept);
            }
        }


                    /**
         * {@inheritDoc}
         * @implSpec
         * 如果操作是 {@code LongConsumer} 的实例，则将其转换为 {@code LongConsumer} 并传递给
         * {@link #forEachRemaining(java.util.function.LongConsumer)}；否则
         * 通过装箱 {@code LongConsumer} 的参数将操作适配为 {@code LongConsumer} 的实例，然后传递给
         * {@link #forEachRemaining(java.util.function.LongConsumer)}。
         */
        @Override
        default void forEachRemaining(Consumer<? super Long> action) {
            if (action instanceof LongConsumer) {
                forEachRemaining((LongConsumer) action);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                                  "{0} calling Spliterator.OfLong.forEachRemaining((LongConsumer) action::accept)");
                forEachRemaining((LongConsumer) action::accept);
            }
        }
    }

    /**
     * 专用于 {@code double} 值的 Spliterator。
     * @since 1.8
     */
    public interface OfDouble extends OfPrimitive<Double, DoubleConsumer, OfDouble> {

        @Override
        OfDouble trySplit();

        @Override
        boolean tryAdvance(DoubleConsumer action);

        @Override
        default void forEachRemaining(DoubleConsumer action) {
            do { } while (tryAdvance(action));
        }

        /**
         * {@inheritDoc}
         * @implSpec
         * 如果操作是 {@code DoubleConsumer} 的实例，则将其转换为 {@code DoubleConsumer} 并传递给
         * {@link #tryAdvance(java.util.function.DoubleConsumer)}；否则
         * 通过装箱 {@code DoubleConsumer} 的参数将操作适配为 {@code DoubleConsumer} 的实例，然后传递给
         * {@link #tryAdvance(java.util.function.DoubleConsumer)}。
         */
        @Override
        default boolean tryAdvance(Consumer<? super Double> action) {
            if (action instanceof DoubleConsumer) {
                return tryAdvance((DoubleConsumer) action);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                                  "{0} calling Spliterator.OfDouble.tryAdvance((DoubleConsumer) action::accept)");
                return tryAdvance((DoubleConsumer) action::accept);
            }
        }

        /**
         * {@inheritDoc}
         * @implSpec
         * 如果操作是 {@code DoubleConsumer} 的实例，则将其转换为 {@code DoubleConsumer} 并传递给
         * {@link #forEachRemaining(java.util.function.DoubleConsumer)}；
         * 否则通过装箱 {@code DoubleConsumer} 的参数将操作适配为 {@code DoubleConsumer} 的实例，然后传递给
         * {@link #forEachRemaining(java.util.function.DoubleConsumer)}。
         */
        @Override
        default void forEachRemaining(Consumer<? super Double> action) {
            if (action instanceof DoubleConsumer) {
                forEachRemaining((DoubleConsumer) action);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                                  "{0} calling Spliterator.OfDouble.forEachRemaining((DoubleConsumer) action::accept)");
                forEachRemaining((DoubleConsumer) action::accept);
            }
        }
    }
}
