
/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.util.stream;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * 实现了各种有用的归约操作的 {@link Collector}，例如将元素累积到集合中，根据各种标准汇总元素等。
 *
 * <p>以下是使用预定义的收集器执行常见可变归约任务的示例：
 *
 * <pre>{@code
 *     // 将名称累积到 List 中
 *     List<String> list = people.stream().map(Person::getName).collect(Collectors.toList());
 *
 *     // 将名称累积到 TreeSet 中
 *     Set<String> set = people.stream().map(Person::getName).collect(Collectors.toCollection(TreeSet::new));
 *
 *     // 将元素转换为字符串并连接它们，以逗号分隔
 *     String joined = things.stream()
 *                           .map(Object::toString)
 *                           .collect(Collectors.joining(", "));
 *
 *     // 计算员工的工资总和
 *     int total = employees.stream()
 *                          .collect(Collectors.summingInt(Employee::getSalary)));
 *
 *     // 按部门分组员工
 *     Map<Department, List<Employee>> byDept
 *         = employees.stream()
 *                    .collect(Collectors.groupingBy(Employee::getDepartment));
 *
 *     // 按部门计算工资总和
 *     Map<Department, Integer> totalByDept
 *         = employees.stream()
 *                    .collect(Collectors.groupingBy(Employee::getDepartment,
 *                                                   Collectors.summingInt(Employee::getSalary)));
 *
 *     // 将学生分为及格和不及格
 *     Map<Boolean, List<Student>> passingFailing =
 *         students.stream()
 *                 .collect(Collectors.partitioningBy(s -> s.getGrade() >= PASS_THRESHOLD));
 *
 * }</pre>
 *
 * @since 1.8
 */
public final class Collectors {

    static final Set<Collector.Characteristics> CH_CONCURRENT_ID
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.CONCURRENT,
                                                     Collector.Characteristics.UNORDERED,
                                                     Collector.Characteristics.IDENTITY_FINISH));
    static final Set<Collector.Characteristics> CH_CONCURRENT_NOID
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.CONCURRENT,
                                                     Collector.Characteristics.UNORDERED));
    static final Set<Collector.Characteristics> CH_ID
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
    static final Set<Collector.Characteristics> CH_UNORDERED_ID
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED,
                                                     Collector.Characteristics.IDENTITY_FINISH));
    static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();

    private Collectors() { }

    /**
     * 返回一个合并函数，适用于
     * {@link Map#merge(Object, Object, BiFunction) Map.merge()} 或
     * {@link #toMap(Function, Function, BinaryOperator) toMap()}，该函数始终
     * 抛出 {@code IllegalStateException}。这可以用于强制执行被收集的元素是唯一的假设。
     *
     * @param <T> 合并函数的输入参数类型
     * @return 一个始终抛出 {@code IllegalStateException} 的合并函数
     */
    private static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }

    @SuppressWarnings("unchecked")
    private static <I, R> Function<I, R> castingIdentity() {
        return i -> (R) i;
    }

    /**
     * {@code Collector} 的简单实现类。
     *
     * @param <T> 要收集的元素类型
     * @param <R> 结果类型
     */
    static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        private final Set<Characteristics> characteristics;

        CollectorImpl(Supplier<A> supplier,
                      BiConsumer<A, T> accumulator,
                      BinaryOperator<A> combiner,
                      Function<A,R> finisher,
                      Set<Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }

        CollectorImpl(Supplier<A> supplier,
                      BiConsumer<A, T> accumulator,
                      BinaryOperator<A> combiner,
                      Set<Characteristics> characteristics) {
            this(supplier, accumulator, combiner, castingIdentity(), characteristics);
        }

        @Override
        public BiConsumer<A, T> accumulator() {
            return accumulator;
        }

        @Override
        public Supplier<A> supplier() {
            return supplier;
        }

        @Override
        public BinaryOperator<A> combiner() {
            return combiner;
        }

        @Override
        public Function<A, R> finisher() {
            return finisher;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return characteristics;
        }
    }

    /**
     * 返回一个 {@code Collector}，将输入元素累积到一个新的 {@code Collection} 中，按遇到顺序。{@code Collection} 由提供的工厂创建。
     *
     * @param <T> 输入元素的类型
     * @param <C> 结果 {@code Collection} 的类型
     * @param collectionFactory 一个返回新的、空的适当类型的 {@code Collection} 的 {@code Supplier}
     * @return 一个 {@code Collector}，将所有输入元素累积到一个 {@code Collection} 中，按遇到顺序
     */
    public static <T, C extends Collection<T>>
    Collector<T, ?, C> toCollection(Supplier<C> collectionFactory) {
        return new CollectorImpl<>(collectionFactory, Collection<T>::add,
                                   (r1, r2) -> { r1.addAll(r2); return r1; },
                                   CH_ID);
    }

    /**
     * 返回一个 {@code Collector}，将输入元素累积到一个新的 {@code List} 中。返回的 {@code List} 的类型、可变性、可序列化性或线程安全性没有保证；如果需要对返回的 {@code List} 进行更多控制，请使用 {@link #toCollection(Supplier)}。
     *
     * @param <T> 输入元素的类型
     * @return 一个 {@code Collector}，将所有输入元素累积到一个 {@code List} 中，按遇到顺序
     */
    public static <T>
    Collector<T, ?, List<T>> toList() {
        return new CollectorImpl<>((Supplier<List<T>>) ArrayList::new, List::add,
                                   (left, right) -> { left.addAll(right); return left; },
                                   CH_ID);
    }

    /**
     * 返回一个 {@code Collector}，将输入元素累积到一个新的 {@code Set} 中。返回的 {@code Set} 的类型、可变性、可序列化性或线程安全性没有保证；如果需要对返回的 {@code Set} 进行更多控制，请使用
     * {@link #toCollection(Supplier)}。
     *
     * <p>这是一个 {@link Collector.Characteristics#UNORDERED 无序} 的 {@code Collector}。
     *
     * @param <T> 输入元素的类型
     * @return 一个 {@code Collector}，将所有输入元素累积到一个 {@code Set} 中
     */
    public static <T>
    Collector<T, ?, Set<T>> toSet() {
        return new CollectorImpl<>((Supplier<Set<T>>) HashSet::new, Set::add,
                                   (left, right) -> { left.addAll(right); return left; },
                                   CH_UNORDERED_ID);
    }

    /**
     * 返回一个 {@code Collector}，将输入元素按遇到顺序连接成一个 {@code String}。
     *
     * @return 一个 {@code Collector}，将输入元素按遇到顺序连接成一个 {@code String}
     */
    public static Collector<CharSequence, ?, String> joining() {
        return new CollectorImpl<CharSequence, StringBuilder, String>(
                StringBuilder::new, StringBuilder::append,
                (r1, r2) -> { r1.append(r2); return r1; },
                StringBuilder::toString, CH_NOID);
    }

    /**
     * 返回一个 {@code Collector}，将输入元素按遇到顺序连接起来，每个元素之间用指定的分隔符分隔。
     *
     * @param delimiter 每个元素之间使用的分隔符
     * @return 一个 {@code Collector}，将 CharSequence 元素按遇到顺序连接起来，每个元素之间用指定的分隔符分隔
     */
    public static Collector<CharSequence, ?, String> joining(CharSequence delimiter) {
        return joining(delimiter, "", "");
    }

    /**
     * 返回一个 {@code Collector}，将输入元素按遇到顺序连接起来，每个元素之间用指定的分隔符分隔，并在连接结果的开头和结尾分别使用指定的前缀和后缀。
     *
     * @param delimiter 每个元素之间使用的分隔符
     * @param  prefix 连接结果开头使用的字符序列
     * @param  suffix 连接结果结尾使用的字符序列
     * @return 一个 {@code Collector}，将 CharSequence 元素按遇到顺序连接起来，每个元素之间用指定的分隔符分隔
     */
    public static Collector<CharSequence, ?, String> joining(CharSequence delimiter,
                                                             CharSequence prefix,
                                                             CharSequence suffix) {
        return new CollectorImpl<>(
                () -> new StringJoiner(delimiter, prefix, suffix),
                StringJoiner::add, StringJoiner::merge,
                StringJoiner::toString, CH_NOID);
    }

    /**
     * 将其右参数的内容合并到其左参数中，使用提供的合并函数处理重复的键。
     *
     * @param <K> 地图键的类型
     * @param <V> 地图值的类型
     * @param <M> 地图的类型
     * @param mergeFunction 适用于 {@link Map#merge(Object, Object, BiFunction) Map.merge()} 的合并函数
     * @return 两个地图的合并函数
     */
    private static <K, V, M extends Map<K,V>>
    BinaryOperator<M> mapMerger(BinaryOperator<V> mergeFunction) {
        return (m1, m2) -> {
            for (Map.Entry<K,V> e : m2.entrySet())
                m1.merge(e.getKey(), e.getValue(), mergeFunction);
            return m1;
        };
    }

    /**
     * 通过应用映射函数将类型为 {@code U} 的元素转换为类型为 {@code T} 的元素，从而将接受类型为 {@code U} 的元素的 {@code Collector} 适配为接受类型为 {@code T} 的元素。
     *
     * @apiNote
     * {@code mapping()} 收集器在多级归约中最有用，例如作为 {@code groupingBy} 或 {@code partitioningBy} 的下游。例如，给定一个 {@code Person} 流，累积每个城市中的姓氏集合：
     * <pre>{@code
     *     Map<City, Set<String>> lastNamesByCity
     *         = people.stream().collect(groupingBy(Person::getCity,
     *                                              mapping(Person::getLastName, toSet())));
     * }</pre>
     *
     * @param <T> 输入元素的类型
     * @param <U> 下游收集器接受的元素类型
     * @param <A> 下游收集器的中间累积类型
     * @param <R> 收集器的结果类型
     * @param mapper 应用于输入元素的函数
     * @param downstream 接受映射值的收集器
     * @return 一个收集器，将映射函数应用于输入元素，并将映射结果提供给下游收集器
     */
    public static <T, U, A, R>
    Collector<T, ?, R> mapping(Function<? super T, ? extends U> mapper,
                               Collector<? super U, A, R> downstream) {
        BiConsumer<A, ? super U> downstreamAccumulator = downstream.accumulator();
        return new CollectorImpl<>(downstream.supplier(),
                                   (r, t) -> downstreamAccumulator.accept(r, mapper.apply(t)),
                                   downstream.combiner(), downstream.finisher(),
                                   downstream.characteristics());
    }

    /**
     * 适配一个 {@code Collector} 以执行额外的最终转换。例如，可以将 {@link #toList()}
     * 收集器适配为始终生成不可变列表：
     * <pre>{@code
     *     List<String> people
     *         = people.stream().collect(collectingAndThen(toList(), Collections::unmodifiableList));
     * }</pre>
     *
     * @param <T> 输入元素的类型
     * @param <A> 下游收集器的中间累积类型
     * @param <R> 下游收集器的结果类型
     * @param <RR> 结果收集器的结果类型
     * @param downstream 一个收集器
     * @param finisher 应用于下游收集器最终结果的函数
     * @return 一个收集器，执行下游收集器的操作，然后进行额外的最终步骤
     */
    public static<T,A,R,RR> Collector<T,A,RR> collectingAndThen(Collector<T,A,R> downstream,
                                                                Function<R,RR> finisher) {
        Set<Collector.Characteristics> characteristics = downstream.characteristics();
        if (characteristics.contains(Collector.Characteristics.IDENTITY_FINISH)) {
            if (characteristics.size() == 1)
                characteristics = Collectors.CH_NOID;
            else {
                characteristics = EnumSet.copyOf(characteristics);
                characteristics.remove(Collector.Characteristics.IDENTITY_FINISH);
                characteristics = Collections.unmodifiableSet(characteristics);
            }
        }
        return new CollectorImpl<>(downstream.supplier(),
                                   downstream.accumulator(),
                                   downstream.combiner(),
                                   downstream.finisher().andThen(finisher),
                                   characteristics);
    }


                /**
     * 返回一个接受类型为 {@code T} 的元素的 {@code Collector}，用于计算输入元素的数量。如果没有元素，则结果为 0。
     *
     * @implSpec
     * 这产生的结果等价于：
     * <pre>{@code
     *     reducing(0L, e -> 1L, Long::sum)
     * }</pre>
     *
     * @param <T> 输入元素的类型
     * @return 一个计算输入元素数量的 {@code Collector}
     */
    public static <T> Collector<T, ?, Long>
    counting() {
        return reducing(0L, e -> 1L, Long::sum);
    }

    /**
     * 返回一个根据给定的 {@code Comparator} 产生最小元素的 {@code Collector}，描述为 {@code Optional<T>}。
     *
     * @implSpec
     * 这产生的结果等价于：
     * <pre>{@code
     *     reducing(BinaryOperator.minBy(comparator))
     * }</pre>
     *
     * @param <T> 输入元素的类型
     * @param comparator 用于比较元素的 {@code Comparator}
     * @return 一个产生最小值的 {@code Collector}
     */
    public static <T> Collector<T, ?, Optional<T>>
    minBy(Comparator<? super T> comparator) {
        return reducing(BinaryOperator.minBy(comparator));
    }

    /**
     * 返回一个根据给定的 {@code Comparator} 产生最大元素的 {@code Collector}，描述为 {@code Optional<T>}。
     *
     * @implSpec
     * 这产生的结果等价于：
     * <pre>{@code
     *     reducing(BinaryOperator.maxBy(comparator))
     * }</pre>
     *
     * @param <T> 输入元素的类型
     * @param comparator 用于比较元素的 {@code Comparator}
     * @return 一个产生最大值的 {@code Collector}
     */
    public static <T> Collector<T, ?, Optional<T>>
    maxBy(Comparator<? super T> comparator) {
        return reducing(BinaryOperator.maxBy(comparator));
    }

    /**
     * 返回一个产生输入元素应用整数值函数后总和的 {@code Collector}。如果没有元素，则结果为 0。
     *
     * @param <T> 输入元素的类型
     * @param mapper 提取要被求和属性的函数
     * @return 一个产生派生属性总和的 {@code Collector}
     */
    public static <T> Collector<T, ?, Integer>
    summingInt(ToIntFunction<? super T> mapper) {
        return new CollectorImpl<>(
                () -> new int[1],
                (a, t) -> { a[0] += mapper.applyAsInt(t); },
                (a, b) -> { a[0] += b[0]; return a; },
                a -> a[0], CH_NOID);
    }

    /**
     * 返回一个产生输入元素应用长整数值函数后总和的 {@code Collector}。如果没有元素，则结果为 0。
     *
     * @param <T> 输入元素的类型
     * @param mapper 提取要被求和属性的函数
     * @return 一个产生派生属性总和的 {@code Collector}
     */
    public static <T> Collector<T, ?, Long>
    summingLong(ToLongFunction<? super T> mapper) {
        return new CollectorImpl<>(
                () -> new long[1],
                (a, t) -> { a[0] += mapper.applyAsLong(t); },
                (a, b) -> { a[0] += b[0]; return a; },
                a -> a[0], CH_NOID);
    }

    /**
     * 返回一个产生输入元素应用双精度浮点数值函数后总和的 {@code Collector}。如果没有元素，则结果为 0。
     *
     * <p>返回的总和可能因值记录的顺序而异，这是由于不同数量级值相加时累积的舍入误差。按绝对值递增排序的值往往会产生更准确的结果。如果任何记录的值为 {@code NaN} 或总和在任何点为 {@code NaN}，则总和将为 {@code NaN}。
     *
     * @param <T> 输入元素的类型
     * @param mapper 提取要被求和属性的函数
     * @return 一个产生派生属性总和的 {@code Collector}
     */
    public static <T> Collector<T, ?, Double>
    summingDouble(ToDoubleFunction<? super T> mapper) {
        /*
         * 在为收集操作分配的数组中，索引 0 保存运行总和的高阶位，索引 1 保存通过补偿求和计算的总和的低阶位，索引 2 保存用于计算流中包含相同符号的无限值的正确结果的简单总和。
         */
        return new CollectorImpl<>(
                () -> new double[3],
                (a, t) -> { sumWithCompensation(a, mapper.applyAsDouble(t));
                            a[2] += mapper.applyAsDouble(t);},
                (a, b) -> { sumWithCompensation(a, b[0]);
                            a[2] += b[2];
                            return sumWithCompensation(a, b[1]); },
                a -> computeFinalSum(a),
                CH_NOID);
    }

    /**
     * 使用 Kahan 求和 / 补偿求和将新的双精度浮点值合并。
     *
     * 运行总和的高阶位在 intermediateSum[0] 中，低阶位在 intermediateSum[1] 中，任何其他元素都是应用程序特定的。
     *
     * @param intermediateSum 中间总和的高阶和低阶位
     * @param value 要包含在运行总和中的新值
     */
    static double[] sumWithCompensation(double[] intermediateSum, double value) {
        double tmp = value - intermediateSum[1];
        double sum = intermediateSum[0];
        double velvel = sum + tmp; // 小狼舍入误差
        intermediateSum[1] = (velvel - sum) - tmp;
        intermediateSum[0] = velvel;
        return intermediateSum;
    }

    /**
     * 如果补偿总和因累积一个或多个相同符号的无限值而错误地为 NaN，则返回简单总和中存储的正确符号的无限值。
     */
    static double computeFinalSum(double[] summands) {
        // 为了获得更好的误差界限，将两个术语相加作为最终总和
        double tmp = summands[0] + summands[1];
        double simpleSum = summands[summands.length - 1];
        if (Double.isNaN(tmp) && Double.isInfinite(simpleSum))
            return simpleSum;
        else
            return tmp;
    }

    /**
     * 返回一个产生输入元素应用整数值函数后算术平均值的 {@code Collector}。如果没有元素，则结果为 0。
     *
     * @param <T> 输入元素的类型
     * @param mapper 提取要被求和属性的函数
     * @return 一个产生派生属性总和的 {@code Collector}
     */
    public static <T> Collector<T, ?, Double>
    averagingInt(ToIntFunction<? super T> mapper) {
        return new CollectorImpl<>(
                () -> new long[2],
                (a, t) -> { a[0] += mapper.applyAsInt(t); a[1]++; },
                (a, b) -> { a[0] += b[0]; a[1] += b[1]; return a; },
                a -> (a[1] == 0) ? 0.0d : (double) a[0] / a[1], CH_NOID);
    }

    /**
     * 返回一个产生输入元素应用长整数值函数后算术平均值的 {@code Collector}。如果没有元素，则结果为 0。
     *
     * @param <T> 输入元素的类型
     * @param mapper 提取要被求和属性的函数
     * @return 一个产生派生属性总和的 {@code Collector}
     */
    public static <T> Collector<T, ?, Double>
    averagingLong(ToLongFunction<? super T> mapper) {
        return new CollectorImpl<>(
                () -> new long[2],
                (a, t) -> { a[0] += mapper.applyAsLong(t); a[1]++; },
                (a, b) -> { a[0] += b[0]; a[1] += b[1]; return a; },
                a -> (a[1] == 0) ? 0.0d : (double) a[0] / a[1], CH_NOID);
    }

    /**
     * 返回一个产生输入元素应用双精度浮点数值函数后算术平均值的 {@code Collector}。如果没有元素，则结果为 0。
     *
     * <p>返回的平均值可能因值记录的顺序而异，这是由于不同数量级值相加时累积的舍入误差。按绝对值递增排序的值往往会产生更准确的结果。如果任何记录的值为 {@code NaN} 或总和在任何点为 {@code NaN}，则平均值将为 {@code NaN}。
     *
     * @implNote {@code double} 格式可以表示 -2<sup>53</sup> 到 2<sup>53</sup> 范围内的所有连续整数。如果管道中的值超过 2<sup>53</sup>，平均计算中的除数将饱和在 2<sup>53</sup>，导致额外的数值错误。
     *
     * @param <T> 输入元素的类型
     * @param mapper 提取要被求和属性的函数
     * @return 一个产生派生属性总和的 {@code Collector}
     */
    public static <T> Collector<T, ?, Double>
    averagingDouble(ToDoubleFunction<? super T> mapper) {
        /*
         * 在为收集操作分配的数组中，索引 0 保存运行总和的高阶位，索引 1 保存通过补偿求和计算的总和的低阶位，索引 2 保存已看到的值的数量。
         */
        return new CollectorImpl<>(
                () -> new double[4],
                (a, t) -> { sumWithCompensation(a, mapper.applyAsDouble(t)); a[2]++; a[3]+= mapper.applyAsDouble(t);},
                (a, b) -> { sumWithCompensation(a, b[0]); sumWithCompensation(a, b[1]); a[2] += b[2]; a[3] += b[3]; return a; },
                a -> (a[2] == 0) ? 0.0d : (computeFinalSum(a) / a[2]),
                CH_NOID);
    }

    /**
     * 返回一个使用指定的 {@code BinaryOperator} 和提供的标识执行输入元素约简的 {@code Collector}。
     *
     * @apiNote
     * {@code reducing()} 收集器在多级约简中最有用，作为 {@code groupingBy} 或 {@code partitioningBy} 的下游。要在流上执行简单的约简，使用 {@link Stream#reduce(Object, BinaryOperator)}} 代替。
     *
     * @param <T> 输入和输出约简的元素类型
     * @param identity 约简的标识值（也即当没有输入元素时返回的值）
     * @param op 用于约简输入元素的 {@code BinaryOperator<T>}
     * @return 实现约简操作的 {@code Collector}
     *
     * @see #reducing(BinaryOperator)
     * @see #reducing(Object, Function, BinaryOperator)
     */
    public static <T> Collector<T, ?, T>
    reducing(T identity, BinaryOperator<T> op) {
        return new CollectorImpl<>(
                boxSupplier(identity),
                (a, t) -> { a[0] = op.apply(a[0], t); },
                (a, b) -> { a[0] = op.apply(a[0], b[0]); return a; },
                a -> a[0],
                CH_NOID);
    }

    @SuppressWarnings("unchecked")
    private static <T> Supplier<T[]> boxSupplier(T identity) {
        return () -> (T[]) new Object[] { identity };
    }

    /**
     * 返回一个使用指定的 {@code BinaryOperator} 执行输入元素约简的 {@code Collector}。结果描述为 {@code Optional<T>}。
     *
     * @apiNote
     * {@code reducing()} 收集器在多级约简中最有用，作为 {@code groupingBy} 或 {@code partitioningBy} 的下游。要在流上执行简单的约简，使用 {@link Stream#reduce(BinaryOperator)} 代替。
     *
     * <p>例如，给定一个 {@code Person} 流，计算每个城市的最高身高的人：
     * <pre>{@code
     *     Comparator<Person> byHeight = Comparator.comparing(Person::getHeight);
     *     Map<City, Person> tallestByCity
     *         = people.stream().collect(groupingBy(Person::getCity, reducing(BinaryOperator.maxBy(byHeight))));
     * }</pre>
     *
     * @param <T> 输入和输出约简的元素类型
     * @param op 用于约简输入元素的 {@code BinaryOperator<T>}
     * @return 实现约简操作的 {@code Collector}
     *
     * @see #reducing(Object, BinaryOperator)
     * @see #reducing(Object, Function, BinaryOperator)
     */
    public static <T> Collector<T, ?, Optional<T>>
    reducing(BinaryOperator<T> op) {
        class OptionalBox implements Consumer<T> {
            T value = null;
            boolean present = false;

            @Override
            public void accept(T t) {
                if (present) {
                    value = op.apply(value, t);
                }
                else {
                    value = t;
                    present = true;
                }
            }
        }

        return new CollectorImpl<T, OptionalBox, Optional<T>>(
                OptionalBox::new, OptionalBox::accept,
                (a, b) -> { if (b.present) a.accept(b.value); return a; },
                a -> Optional.ofNullable(a.value), CH_NOID);
    }

    /**
     * 返回一个使用指定的映射函数和 {@code BinaryOperator} 执行输入元素约简的 {@code Collector}。这是 {@link #reducing(Object, BinaryOperator)} 的泛化，允许在约简前对元素进行转换。
     *
     * @apiNote
     * {@code reducing()} 收集器在多级约简中最有用，作为 {@code groupingBy} 或 {@code partitioningBy} 的下游。要在流上执行简单的映射-约简，使用 {@link Stream#map(Function)} 和 {@link Stream#reduce(Object, BinaryOperator)} 代替。
     *
     * <p>例如，给定一个 {@code Person} 流，计算每个城市居民的最长姓氏：
     * <pre>{@code
     *     Comparator<String> byLength = Comparator.comparing(String::length);
     *     Map<City, String> longestLastNameByCity
     *         = people.stream().collect(groupingBy(Person::getCity,
     *                                              reducing(Person::getLastName, BinaryOperator.maxBy(byLength))));
     * }</pre>
     *
     * @param <T> 输入元素的类型
     * @param <U> 映射值的类型
     * @param identity 约简的标识值（也即当没有输入元素时返回的值）
     * @param mapper 应用于每个输入值的映射函数
     * @param op 用于约简映射值的 {@code BinaryOperator<U>}
     * @return 实现映射-约简操作的 {@code Collector}
     *
     * @see #reducing(Object, BinaryOperator)
     * @see #reducing(BinaryOperator)
     */
    public static <T, U>
    Collector<T, ?, U> reducing(U identity,
                                Function<? super T, ? extends U> mapper,
                                BinaryOperator<U> op) {
        return new CollectorImpl<>(
                boxSupplier(identity),
                (a, t) -> { a[0] = op.apply(a[0], mapper.apply(t)); },
                (a, b) -> { a[0] = op.apply(a[0], b[0]); return a; },
                a -> a[0], CH_NOID);
    }


                /**
     * 返回一个 {@code Collector} 实现对类型为 {@code T} 的输入元素的 "group by" 操作，根据分类函数对元素进行分组，并返回结果在 {@code Map} 中。
     *
     * <p>分类函数将元素映射到某些键类型 {@code K}。收集器生成一个 {@code Map<K, List<T>>}，其键是应用分类函数到输入元素后得到的值，对应的值是 {@code List}，包含映射到关联键下的输入元素。
     *
     * <p>返回的 {@code Map} 或 {@code List} 对象的类型、可变性、可序列化性或线程安全性没有保证。
     * @implSpec
     * 这产生的结果类似于：
     * <pre>{@code
     *     groupingBy(classifier, toList());
     * }</pre>
     *
     * @implNote
     * 返回的 {@code Collector} 不是并发的。对于并行流管道，{@code combiner} 函数通过将一个映射中的键合并到另一个映射中来操作，这可能是一个昂贵的操作。如果不要求结果 {@code Map} 收集器中元素的顺序，则使用 {@link #groupingByConcurrent(Function)}
     * 可能提供更好的并行性能。
     *
     * @param <T> 输入元素的类型
     * @param <K> 键的类型
     * @param classifier 映射输入元素到键的分类函数
     * @return 实现 group-by 操作的 {@code Collector}
     *
     * @see #groupingBy(Function, Collector)
     * @see #groupingBy(Function, Supplier, Collector)
     * @see #groupingByConcurrent(Function)
     */
    public static <T, K> Collector<T, ?, Map<K, List<T>>>
    groupingBy(Function<? super T, ? extends K> classifier) {
        return groupingBy(classifier, toList());
    }

    /**
     * 返回一个 {@code Collector} 实现对类型为 {@code T} 的输入元素的级联 "group by" 操作，根据分类函数对元素进行分组，然后使用指定的下游 {@code Collector} 对与给定键关联的值执行归约操作。
     *
     * <p>分类函数将元素映射到某些键类型 {@code K}。下游收集器对类型为 {@code T} 的元素进行操作，并生成类型为 {@code D} 的结果。生成的收集器生成一个 {@code Map<K, D>}。
     *
     * <p>返回的 {@code Map} 的类型、可变性、可序列化性或线程安全性没有保证。
     *
     * <p>例如，要计算每个城市中人的姓氏集合：
     * <pre>{@code
     *     Map<City, Set<String>> namesByCity
     *         = people.stream().collect(groupingBy(Person::getCity,
     *                                              mapping(Person::getLastName, toSet())));
     * }</pre>
     *
     * @implNote
     * 返回的 {@code Collector} 不是并发的。对于并行流管道，{@code combiner} 函数通过将一个映射中的键合并到另一个映射中来操作，这可能是一个昂贵的操作。如果不要求元素以特定顺序呈现给下游收集器，则使用 {@link #groupingByConcurrent(Function, Collector)}
     * 可能提供更好的并行性能。
     *
     * @param <T> 输入元素的类型
     * @param <K> 键的类型
     * @param <A> 下游收集器的中间累积类型
     * @param <D> 下游归约的结果类型
     * @param classifier 映射输入元素到键的分类函数
     * @param downstream 实现下游归约的 {@code Collector}
     * @return 实现级联 group-by 操作的 {@code Collector}
     * @see #groupingBy(Function)
     *
     * @see #groupingBy(Function, Supplier, Collector)
     * @see #groupingByConcurrent(Function, Collector)
     */
    public static <T, K, A, D>
    Collector<T, ?, Map<K, D>> groupingBy(Function<? super T, ? extends K> classifier,
                                          Collector<? super T, A, D> downstream) {
        return groupingBy(classifier, HashMap::new, downstream);
    }

    /**
     * 返回一个 {@code Collector} 实现对类型为 {@code T} 的输入元素的级联 "group by" 操作，根据分类函数对元素进行分组，然后使用指定的下游 {@code Collector} 对与给定键关联的值执行归约操作。生成的 {@code Map} 由提供的工厂函数创建。
     *
     * <p>分类函数将元素映射到某些键类型 {@code K}。下游收集器对类型为 {@code T} 的元素进行操作，并生成类型为 {@code D} 的结果。生成的收集器生成一个 {@code Map<K, D>}。
     *
     * <p>例如，要计算每个城市中人的姓氏集合，其中城市名称已排序：
     * <pre>{@code
     *     Map<City, Set<String>> namesByCity
     *         = people.stream().collect(groupingBy(Person::getCity, TreeMap::new,
     *                                              mapping(Person::getLastName, toSet())));
     * }</pre>
     *
     * @implNote
     * 返回的 {@code Collector} 不是并发的。对于并行流管道，{@code combiner} 函数通过将一个映射中的键合并到另一个映射中来操作，这可能是一个昂贵的操作。如果不要求元素以特定顺序呈现给下游收集器，则使用 {@link #groupingByConcurrent(Function, Supplier, Collector)}
     * 可能提供更好的并行性能。
     *
     * @param <T> 输入元素的类型
     * @param <K> 键的类型
     * @param <A> 下游收集器的中间累积类型
     * @param <D> 下游归约的结果类型
     * @param <M> 结果 {@code Map} 的类型
     * @param classifier 映射输入元素到键的分类函数
     * @param downstream 实现下游归约的 {@code Collector}
     * @param mapFactory 一个函数，调用时生成一个新的空 {@code Map}，类型为所需的类型
     * @return 实现级联 group-by 操作的 {@code Collector}
     *
     * @see #groupingBy(Function, Collector)
     * @see #groupingBy(Function)
     * @see #groupingByConcurrent(Function, Supplier, Collector)
     */
    public static <T, K, D, A, M extends Map<K, D>>
    Collector<T, ?, M> groupingBy(Function<? super T, ? extends K> classifier,
                                  Supplier<M> mapFactory,
                                  Collector<? super T, A, D> downstream) {
        Supplier<A> downstreamSupplier = downstream.supplier();
        BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
        BiConsumer<Map<K, A>, T> accumulator = (m, t) -> {
            K key = Objects.requireNonNull(classifier.apply(t), "element cannot be mapped to a null key");
            A container = m.computeIfAbsent(key, k -> downstreamSupplier.get());
            downstreamAccumulator.accept(container, t);
        };
        BinaryOperator<Map<K, A>> merger = Collectors.<K, A, Map<K, A>>mapMerger(downstream.combiner());
        @SuppressWarnings("unchecked")
        Supplier<Map<K, A>> mangledFactory = (Supplier<Map<K, A>>) mapFactory;

        if (downstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
            return new CollectorImpl<>(mangledFactory, accumulator, merger, CH_ID);
        }
        else {
            @SuppressWarnings("unchecked")
            Function<A, A> downstreamFinisher = (Function<A, A>) downstream.finisher();
            Function<Map<K, A>, M> finisher = intermediate -> {
                intermediate.replaceAll((k, v) -> downstreamFinisher.apply(v));
                @SuppressWarnings("unchecked")
                M castResult = (M) intermediate;
                return castResult;
            };
            return new CollectorImpl<>(mangledFactory, accumulator, merger, finisher, CH_NOID);
        }
    }

    /**
     * 返回一个并发的 {@code Collector} 实现对类型为 {@code T} 的输入元素的 "group by" 操作，根据分类函数对元素进行分组。
     *
     * <p>这是一个 {@link Collector.Characteristics#CONCURRENT 并发} 和 {@link Collector.Characteristics#UNORDERED 无序} 的 {@code Collector}。
     *
     * <p>分类函数将元素映射到某些键类型 {@code K}。收集器生成一个 {@code ConcurrentMap<K, List<T>>}，其键是应用分类函数到输入元素后得到的值，对应的值是 {@code List}，包含映射到关联键下的输入元素。
     *
     * <p>返回的 {@code Map} 或 {@code List} 对象的类型、可变性、可序列化性或线程安全性没有保证。
     * @implSpec
     * 这产生的结果类似于：
     * <pre>{@code
     *     groupingByConcurrent(classifier, toList());
     * }</pre>
     *
     * @param <T> 输入元素的类型
     * @param <K> 键的类型
     * @param classifier 映射输入元素到键的分类函数
     * @return 一个并发、无序的实现 group-by 操作的 {@code Collector}
     *
     * @see #groupingBy(Function)
     * @see #groupingByConcurrent(Function, Collector)
     * @see #groupingByConcurrent(Function, Supplier, Collector)
     */
    public static <T, K>
    Collector<T, ?, ConcurrentMap<K, List<T>>>
    groupingByConcurrent(Function<? super T, ? extends K> classifier) {
        return groupingByConcurrent(classifier, ConcurrentHashMap::new, toList());
    }

    /**
     * 返回一个并发的 {@code Collector} 实现对类型为 {@code T} 的输入元素的级联 "group by" 操作，根据分类函数对元素进行分组，然后使用指定的下游 {@code Collector} 对与给定键关联的值执行归约操作。
     *
     * <p>这是一个 {@link Collector.Characteristics#CONCURRENT 并发} 和 {@link Collector.Characteristics#UNORDERED 无序} 的 {@code Collector}。
     *
     * <p>分类函数将元素映射到某些键类型 {@code K}。下游收集器对类型为 {@code T} 的元素进行操作，并生成类型为 {@code D} 的结果。生成的收集器生成一个 {@code Map<K, D>}。
     *
     * <p>例如，要计算每个城市中人的姓氏集合，其中城市名称已排序：
     * <pre>{@code
     *     ConcurrentMap<City, Set<String>> namesByCity
     *         = people.stream().collect(groupingByConcurrent(Person::getCity,
     *                                                        mapping(Person::getLastName, toSet())));
     * }</pre>
     *
     * @param <T> 输入元素的类型
     * @param <K> 键的类型
     * @param <A> 下游收集器的中间累积类型
     * @param <D> 下游归约的结果类型
     * @param classifier 映射输入元素到键的分类函数
     * @param downstream 实现下游归约的 {@code Collector}
     * @return 一个并发、无序的实现级联 group-by 操作的 {@code Collector}
     *
     * @see #groupingBy(Function, Collector)
     * @see #groupingByConcurrent(Function)
     * @see #groupingByConcurrent(Function, Supplier, Collector)
     */
    public static <T, K, A, D>
    Collector<T, ?, ConcurrentMap<K, D>> groupingByConcurrent(Function<? super T, ? extends K> classifier,
                                                              Collector<? super T, A, D> downstream) {
        return groupingByConcurrent(classifier, ConcurrentHashMap::new, downstream);
    }

    /**
     * 返回一个并发的 {@code Collector} 实现对类型为 {@code T} 的输入元素的级联 "group by" 操作，根据分类函数对元素进行分组，然后使用指定的下游 {@code Collector} 对与给定键关联的值执行归约操作。生成的 {@code ConcurrentMap} 由提供的工厂函数创建。
     *
     * <p>这是一个 {@link Collector.Characteristics#CONCURRENT 并发} 和 {@link Collector.Characteristics#UNORDERED 无序} 的 {@code Collector}。
     *
     * <p>分类函数将元素映射到某些键类型 {@code K}。下游收集器对类型为 {@code T} 的元素进行操作，并生成类型为 {@code D} 的结果。生成的收集器生成一个 {@code Map<K, D>}。
     *
     * <p>例如，要计算每个城市中人的姓氏集合，其中城市名称已排序：
     * <pre>{@code
     *     ConcurrentMap<City, Set<String>> namesByCity
     *         = people.stream().collect(groupingBy(Person::getCity, ConcurrentSkipListMap::new,
     *                                              mapping(Person::getLastName, toSet())));
     * }</pre>
     *
     *
     * @param <T> 输入元素的类型
     * @param <K> 键的类型
     * @param <A> 下游收集器的中间累积类型
     * @param <D> 下游归约的结果类型
     * @param <M> 结果 {@code ConcurrentMap} 的类型
     * @param classifier 映射输入元素到键的分类函数
     * @param downstream 实现下游归约的 {@code Collector}
     * @param mapFactory 一个函数，调用时生成一个新的空 {@code ConcurrentMap}，类型为所需的类型
     * @return 一个并发、无序的实现级联 group-by 操作的 {@code Collector}
     *
     * @see #groupingByConcurrent(Function)
     * @see #groupingByConcurrent(Function, Collector)
     * @see #groupingBy(Function, Supplier, Collector)
     */
    public static <T, K, A, D, M extends ConcurrentMap<K, D>>
    Collector<T, ?, M> groupingByConcurrent(Function<? super T, ? extends K> classifier,
                                            Supplier<M> mapFactory,
                                            Collector<? super T, A, D> downstream) {
        Supplier<A> downstreamSupplier = downstream.supplier();
        BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
        BinaryOperator<ConcurrentMap<K, A>> merger = Collectors.<K, A, ConcurrentMap<K, A>>mapMerger(downstream.combiner());
        @SuppressWarnings("unchecked")
        Supplier<ConcurrentMap<K, A>> mangledFactory = (Supplier<ConcurrentMap<K, A>>) mapFactory;
        BiConsumer<ConcurrentMap<K, A>, T> accumulator;
        if (downstream.characteristics().contains(Collector.Characteristics.CONCURRENT)) {
            accumulator = (m, t) -> {
                K key = Objects.requireNonNull(classifier.apply(t), "element cannot be mapped to a null key");
                A resultContainer = m.computeIfAbsent(key, k -> downstreamSupplier.get());
                downstreamAccumulator.accept(resultContainer, t);
            };
        }
        else {
            accumulator = (m, t) -> {
                K key = Objects.requireNonNull(classifier.apply(t), "element cannot be mapped to a null key");
                A resultContainer = m.computeIfAbsent(key, k -> downstreamSupplier.get());
                synchronized (resultContainer) {
                    downstreamAccumulator.accept(resultContainer, t);
                }
            };
        }


                    if (downstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
            return new CollectorImpl<>(mangledFactory, accumulator, merger, CH_CONCURRENT_ID);
        }
        else {
            @SuppressWarnings("unchecked")
            Function<A, A> downstreamFinisher = (Function<A, A>) downstream.finisher();
            Function<ConcurrentMap<K, A>, M> finisher = intermediate -> {
                intermediate.replaceAll((k, v) -> downstreamFinisher.apply(v));
                @SuppressWarnings("unchecked")
                M castResult = (M) intermediate;
                return castResult;
            };
            return new CollectorImpl<>(mangledFactory, accumulator, merger, finisher, CH_CONCURRENT_NOID);
        }
    }

    /**
     * 返回一个 {@code Collector}，根据 {@code Predicate} 将输入元素分区，并将它们组织成
     * {@code Map<Boolean, List<T>>}。
     *
     * 返回的 {@code Map} 类型、可变性、序列化性或线程安全性没有保证。
     *
     * @param <T> 输入元素的类型
     * @param predicate 用于分类输入元素的谓词
     * @return 实现分区操作的 {@code Collector}
     *
     * @see #partitioningBy(Predicate, Collector)
     */
    public static <T>
    Collector<T, ?, Map<Boolean, List<T>>> partitioningBy(Predicate<? super T> predicate) {
        return partitioningBy(predicate, toList());
    }

    /**
     * 返回一个 {@code Collector}，根据 {@code Predicate} 将输入元素分区，使用另一个 {@code Collector} 对每个分区中的值进行规约，并将它们组织成
     * {@code Map<Boolean, D>}，其值是下游规约的结果。
     *
     * <p>返回的 {@code Map} 类型、可变性、序列化性或线程安全性没有保证。
     *
     * @param <T> 输入元素的类型
     * @param <A> 下游收集器的中间累加类型
     * @param <D> 下游规约的结果类型
     * @param predicate 用于分类输入元素的谓词
     * @param downstream 实现下游规约的 {@code Collector}
     * @return 实现级联分区操作的 {@code Collector}
     *
     * @see #partitioningBy(Predicate)
     */
    public static <T, D, A>
    Collector<T, ?, Map<Boolean, D>> partitioningBy(Predicate<? super T> predicate,
                                                    Collector<? super T, A, D> downstream) {
        BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
        BiConsumer<Partition<A>, T> accumulator = (result, t) ->
                downstreamAccumulator.accept(predicate.test(t) ? result.forTrue : result.forFalse, t);
        BinaryOperator<A> op = downstream.combiner();
        BinaryOperator<Partition<A>> merger = (left, right) ->
                new Partition<>(op.apply(left.forTrue, right.forTrue),
                                op.apply(left.forFalse, right.forFalse));
        Supplier<Partition<A>> supplier = () ->
                new Partition<>(downstream.supplier().get(),
                                downstream.supplier().get());
        if (downstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
            return new CollectorImpl<>(supplier, accumulator, merger, CH_ID);
        }
        else {
            Function<Partition<A>, Map<Boolean, D>> finisher = par ->
                    new Partition<>(downstream.finisher().apply(par.forTrue),
                                    downstream.finisher().apply(par.forFalse));
            return new CollectorImpl<>(supplier, accumulator, merger, finisher, CH_NOID);
        }
    }

    /**
     * 返回一个 {@code Collector}，将元素累积到一个 {@code Map} 中，其键和值是通过应用提供的映射函数到输入元素得到的结果。
     *
     * <p>如果映射的键包含重复项（根据 {@link Object#equals(Object)}），则在执行收集操作时会抛出 {@code IllegalStateException}。如果映射的键可能有重复项，请使用 {@link #toMap(Function, Function, BinaryOperator)}。
     *
     * @apiNote
     * 通常，键或值之一是输入元素。在这种情况下，实用方法
     * {@link java.util.function.Function#identity()} 可能会有所帮助。
     * 例如，以下代码生成一个将学生映射到其平均绩点的 {@code Map}：
     * <pre>{@code
     *     Map<Student, Double> studentToGPA
     *         students.stream().collect(toMap(Functions.identity(),
     *                                         student -> computeGPA(student)));
     * }</pre>
     * 以下代码生成一个将唯一标识符映射到学生的 {@code Map}：
     * <pre>{@code
     *     Map<String, Student> studentIdToStudent
     *         students.stream().collect(toMap(Student::getId,
     *                                         Functions.identity());
     * }</pre>
     *
     * @implNote
     * 返回的 {@code Collector} 不是并发的。对于并行流管道，组合函数通过将一个映射中的键合并到另一个映射中来操作，这可能是一个昂贵的操作。如果不要求结果按遇到的顺序插入到 {@code Map} 中，使用 {@link #toConcurrentMap(Function, Function)}
     * 可能会提供更好的并行性能。
     *
     * @param <T> 输入元素的类型
     * @param <K> 键映射函数的输出类型
     * @param <U> 值映射函数的输出类型
     * @param keyMapper 生成键的映射函数
     * @param valueMapper 生成值的映射函数
     * @return 一个 {@code Collector}，将元素累积到一个 {@code Map} 中，其键和值是通过应用映射函数到输入元素得到的结果
     *
     * @see #toMap(Function, Function, BinaryOperator)
     * @see #toMap(Function, Function, BinaryOperator, Supplier)
     * @see #toConcurrentMap(Function, Function)
     */
    public static <T, K, U>
    Collector<T, ?, Map<K,U>> toMap(Function<? super T, ? extends K> keyMapper,
                                    Function<? super T, ? extends U> valueMapper) {
        return toMap(keyMapper, valueMapper, throwingMerger(), HashMap::new);
    }

    /**
     * 返回一个 {@code Collector}，将元素累积到一个 {@code Map} 中，其键和值是通过应用提供的映射函数到输入元素得到的结果。
     *
     * <p>如果映射的键包含重复项（根据 {@link Object#equals(Object)}），则将值映射函数应用于每个相等的元素，并使用提供的合并函数将结果合并。
     *
     * @apiNote
     * 有多种方法可以处理多个元素映射到同一键的冲突。其他形式的 {@code toMap} 仅使用一个无条件抛出异常的合并函数，但您可以轻松编写更灵活的合并策略。例如，如果您有一个 {@code Person} 流，并且您希望生成一个将姓名映射到地址的“电话簿”，但可能有两个人同名，您可以这样做以优雅地处理这些冲突，并生成一个将姓名映射到地址列表的 {@code Map}：
     * <pre>{@code
     *     Map<String, String> phoneBook
     *         people.stream().collect(toMap(Person::getName,
     *                                       Person::getAddress,
     *                                       (s, a) -> s + ", " + a));
     * }</pre>
     *
     * @implNote
     * 返回的 {@code Collector} 不是并发的。对于并行流管道，组合函数通过将一个映射中的键合并到另一个映射中来操作，这可能是一个昂贵的操作。如果不要求结果按遇到的顺序合并到 {@code Map} 中，使用 {@link #toConcurrentMap(Function, Function, BinaryOperator)}
     * 可能会提供更好的并行性能。
     *
     * @param <T> 输入元素的类型
     * @param <K> 键映射函数的输出类型
     * @param <U> 值映射函数的输出类型
     * @param keyMapper 生成键的映射函数
     * @param valueMapper 生成值的映射函数
     * @param mergeFunction 用于解决与同一键关联的值之间的冲突的合并函数，如 {@link Map#merge(Object, Object, BiFunction)} 所提供的
     * @return 一个 {@code Collector}，将元素累积到一个 {@code Map} 中，其键是通过应用键映射函数到输入元素得到的结果，其值是通过应用值映射函数到所有等于键的输入元素并使用合并函数组合它们得到的结果
     *
     * @see #toMap(Function, Function)
     * @see #toMap(Function, Function, BinaryOperator, Supplier)
     * @see #toConcurrentMap(Function, Function, BinaryOperator)
     */
    public static <T, K, U>
    Collector<T, ?, Map<K,U>> toMap(Function<? super T, ? extends K> keyMapper,
                                    Function<? super T, ? extends U> valueMapper,
                                    BinaryOperator<U> mergeFunction) {
        return toMap(keyMapper, valueMapper, mergeFunction, HashMap::new);
    }

    /**
     * 返回一个 {@code Collector}，将元素累积到一个 {@code Map} 中，其键和值是通过应用提供的映射函数到输入元素得到的结果。
     *
     * <p>如果映射的键包含重复项（根据 {@link Object#equals(Object)}），则将值映射函数应用于每个相等的元素，并使用提供的合并函数将结果合并。映射由提供的供应商函数创建。
     *
     * @implNote
     * 返回的 {@code Collector} 不是并发的。对于并行流管道，组合函数通过将一个映射中的键合并到另一个映射中来操作，这可能是一个昂贵的操作。如果不要求结果按遇到的顺序合并到 {@code Map} 中，使用 {@link #toConcurrentMap(Function, Function, BinaryOperator, Supplier)}
     * 可能会提供更好的并行性能。
     *
     * @param <T> 输入元素的类型
     * @param <K> 键映射函数的输出类型
     * @param <U> 值映射函数的输出类型
     * @param <M> 结果 {@code Map} 的类型
     * @param keyMapper 生成键的映射函数
     * @param valueMapper 生成值的映射函数
     * @param mergeFunction 用于解决与同一键关联的值之间的冲突的合并函数，如 {@link Map#merge(Object, Object, BiFunction)} 所提供的
     * @param mapSupplier 返回一个新的、空的 {@code Map} 的函数，结果将插入其中
     * @return 一个 {@code Collector}，将元素累积到一个 {@code Map} 中，其键是通过应用键映射函数到输入元素得到的结果，其值是通过应用值映射函数到所有等于键的输入元素并使用合并函数组合它们得到的结果
     *
     * @see #toMap(Function, Function)
     * @see #toMap(Function, Function, BinaryOperator)
     * @see #toConcurrentMap(Function, Function, BinaryOperator, Supplier)
     */
    public static <T, K, U, M extends Map<K, U>>
    Collector<T, ?, M> toMap(Function<? super T, ? extends K> keyMapper,
                                Function<? super T, ? extends U> valueMapper,
                                BinaryOperator<U> mergeFunction,
                                Supplier<M> mapSupplier) {
        BiConsumer<M, T> accumulator
                = (map, element) -> map.merge(keyMapper.apply(element),
                                              valueMapper.apply(element), mergeFunction);
        return new CollectorImpl<>(mapSupplier, accumulator, mapMerger(mergeFunction), CH_ID);
    }

    /**
     * 返回一个并发的 {@code Collector}，将元素累积到一个 {@code ConcurrentMap} 中，其键和值是通过应用提供的映射函数到输入元素得到的结果。
     *
     * <p>如果映射的键包含重复项（根据 {@link Object#equals(Object)}），则在执行收集操作时会抛出 {@code IllegalStateException}。如果映射的键可能有重复项，请使用
     * {@link #toConcurrentMap(Function, Function, BinaryOperator)}。
     *
     * @apiNote
     * 通常，键或值之一是输入元素。在这种情况下，实用方法
     * {@link java.util.function.Function#identity()} 可能会有所帮助。
     * 例如，以下代码生成一个将学生映射到其平均绩点的 {@code Map}：
     * <pre>{@code
     *     Map<Student, Double> studentToGPA
     *         students.stream().collect(toMap(Functions.identity(),
     *                                         student -> computeGPA(student)));
     * }</pre>
     * 以下代码生成一个将唯一标识符映射到学生的 {@code Map}：
     * <pre>{@code
     *     Map<String, Student> studentIdToStudent
     *         students.stream().collect(toConcurrentMap(Student::getId,
     *                                                   Functions.identity());
     * }</pre>
     *
     * <p>这是一个 {@link Collector.Characteristics#CONCURRENT 并发} 和
     * {@link Collector.Characteristics#UNORDERED 无序} 的 {@code Collector}。
     *
     * @param <T> 输入元素的类型
     * @param <K> 键映射函数的输出类型
     * @param <U> 值映射函数的输出类型
     * @param keyMapper 生成键的映射函数
     * @param valueMapper 生成值的映射函数
     * @return 一个并发、无序的 {@code Collector}，将元素累积到一个 {@code ConcurrentMap} 中，其键是通过应用键映射函数到输入元素得到的结果，其值是通过应用值映射函数到输入元素得到的结果
     *
     * @see #toMap(Function, Function)
     * @see #toConcurrentMap(Function, Function, BinaryOperator)
     * @see #toConcurrentMap(Function, Function, BinaryOperator, Supplier)
     */
    public static <T, K, U>
    Collector<T, ?, ConcurrentMap<K,U>> toConcurrentMap(Function<? super T, ? extends K> keyMapper,
                                                        Function<? super T, ? extends U> valueMapper) {
        return toConcurrentMap(keyMapper, valueMapper, throwingMerger(), ConcurrentHashMap::new);
    }


                /**
     * 返回一个并发的 {@code Collector}，该收集器将元素累积到一个
     * {@code ConcurrentMap} 中，该映射的键和值是通过应用提供的映射函数到输入元素得到的结果。
     *
     * <p>如果映射的键包含重复项（根据 {@link Object#equals(Object)} 判断），
     * 则值映射函数将应用于每个相等的元素，结果使用提供的合并函数进行合并。
     *
     * @apiNote
     * 处理多个元素映射到相同键的冲突有多种方法。其他形式的 {@code toConcurrentMap} 仅使用
     * 无条件抛出异常的合并函数，但您可以轻松编写更灵活的合并策略。例如，如果您有一个
     * {@code Person} 流，并且您希望生成一个将名称映射到地址的“电话簿”，但可能存在两个
     * 人有相同的名字，您可以如下操作，优雅地处理这些冲突，并生成一个将名字映射到地址列表的
     * {@code Map}：
     * <pre>{@code
     *     Map<String, String> phoneBook
     *         people.stream().collect(toConcurrentMap(Person::getName,
     *                                                 Person::getAddress,
     *                                                 (s, a) -> s + ", " + a));
     * }</pre>
     *
     * <p>这是一个 {@link Collector.Characteristics#CONCURRENT 并发} 和
     * {@link Collector.Characteristics#UNORDERED 无序} 的收集器。
     *
     * @param <T> 输入元素的类型
     * @param <K> 键映射函数的输出类型
     * @param <U> 值映射函数的输出类型
     * @param keyMapper 生成键的映射函数
     * @param valueMapper 生成值的映射函数
     * @param mergeFunction 用于解决相同键关联的值之间的冲突的合并函数，如
     *                      供给 {@link Map#merge(Object, Object, BiFunction)} 所用
     * @return 一个并发、无序的 {@code Collector}，该收集器将元素累积到一个
     * {@code ConcurrentMap} 中，该映射的键是通过应用键映射函数到输入元素得到的结果，值是通过
     * 应用值映射函数到所有等于键的输入元素并使用合并函数组合它们得到的结果
     *
     * @see #toConcurrentMap(Function, Function)
     * @see #toConcurrentMap(Function, Function, BinaryOperator, Supplier)
     * @see #toMap(Function, Function, BinaryOperator)
     */
    public static <T, K, U>
    Collector<T, ?, ConcurrentMap<K,U>>
    toConcurrentMap(Function<? super T, ? extends K> keyMapper,
                    Function<? super T, ? extends U> valueMapper,
                    BinaryOperator<U> mergeFunction) {
        return toConcurrentMap(keyMapper, valueMapper, mergeFunction, ConcurrentHashMap::new);
    }

    /**
     * 返回一个并发的 {@code Collector}，该收集器将元素累积到一个
     * {@code ConcurrentMap} 中，该映射的键和值是通过应用提供的映射函数到输入元素得到的结果。
     *
     * <p>如果映射的键包含重复项（根据 {@link Object#equals(Object)} 判断），
     * 则值映射函数将应用于每个相等的元素，结果使用提供的合并函数进行合并。该
     * {@code ConcurrentMap} 由提供的供应商函数创建。
     *
     * <p>这是一个 {@link Collector.Characteristics#CONCURRENT 并发} 和
     * {@link Collector.Characteristics#UNORDERED 无序} 的收集器。
     *
     * @param <T> 输入元素的类型
     * @param <K> 键映射函数的输出类型
     * @param <U> 值映射函数的输出类型
     * @param <M> 结果 {@code ConcurrentMap} 的类型
     * @param keyMapper 生成键的映射函数
     * @param valueMapper 生成值的映射函数
     * @param mergeFunction 用于解决相同键关联的值之间的冲突的合并函数，如
     *                      供给 {@link Map#merge(Object, Object, BiFunction)} 所用
     * @param mapSupplier 一个返回新的、空的 {@code Map} 的函数，结果将插入其中
     * @return 一个并发、无序的 {@code Collector}，该收集器将元素累积到一个
     * {@code ConcurrentMap} 中，该映射的键是通过应用键映射函数到输入元素得到的结果，值是通过
     * 应用值映射函数到所有等于键的输入元素并使用合并函数组合它们得到的结果
     *
     * @see #toConcurrentMap(Function, Function)
     * @see #toConcurrentMap(Function, Function, BinaryOperator)
     * @see #toMap(Function, Function, BinaryOperator, Supplier)
     */
    public static <T, K, U, M extends ConcurrentMap<K, U>>
    Collector<T, ?, M> toConcurrentMap(Function<? super T, ? extends K> keyMapper,
                                       Function<? super T, ? extends U> valueMapper,
                                       BinaryOperator<U> mergeFunction,
                                       Supplier<M> mapSupplier) {
        BiConsumer<M, T> accumulator
                = (map, element) -> map.merge(keyMapper.apply(element),
                                              valueMapper.apply(element), mergeFunction);
        return new CollectorImpl<>(mapSupplier, accumulator, mapMerger(mergeFunction), CH_CONCURRENT_ID);
    }

    /**
     * 返回一个 {@code Collector}，该收集器将一个生成 {@code int} 的
     * 映射函数应用于每个输入元素，并返回生成值的汇总统计信息。
     *
     * @param <T> 输入元素的类型
     * @param mapper 应用于每个元素的映射函数
     * @return 实现汇总统计信息归约的 {@code Collector}
     *
     * @see #summarizingDouble(ToDoubleFunction)
     * @see #summarizingLong(ToLongFunction)
     */
    public static <T>
    Collector<T, ?, IntSummaryStatistics> summarizingInt(ToIntFunction<? super T> mapper) {
        return new CollectorImpl<T, IntSummaryStatistics, IntSummaryStatistics>(
                IntSummaryStatistics::new,
                (r, t) -> r.accept(mapper.applyAsInt(t)),
                (l, r) -> { l.combine(r); return l; }, CH_ID);
    }

    /**
     * 返回一个 {@code Collector}，该收集器将一个生成 {@code long} 的
     * 映射函数应用于每个输入元素，并返回生成值的汇总统计信息。
     *
     * @param <T> 输入元素的类型
     * @param mapper 应用于每个元素的映射函数
     * @return 实现汇总统计信息归约的 {@code Collector}
     *
     * @see #summarizingDouble(ToDoubleFunction)
     * @see #summarizingInt(ToIntFunction)
     */
    public static <T>
    Collector<T, ?, LongSummaryStatistics> summarizingLong(ToLongFunction<? super T> mapper) {
        return new CollectorImpl<T, LongSummaryStatistics, LongSummaryStatistics>(
                LongSummaryStatistics::new,
                (r, t) -> r.accept(mapper.applyAsLong(t)),
                (l, r) -> { l.combine(r); return l; }, CH_ID);
    }

    /**
     * 返回一个 {@code Collector}，该收集器将一个生成 {@code double} 的
     * 映射函数应用于每个输入元素，并返回生成值的汇总统计信息。
     *
     * @param <T> 输入元素的类型
     * @param mapper 应用于每个元素的映射函数
     * @return 实现汇总统计信息归约的 {@code Collector}
     *
     * @see #summarizingLong(ToLongFunction)
     * @see #summarizingInt(ToIntFunction)
     */
    public static <T>
    Collector<T, ?, DoubleSummaryStatistics> summarizingDouble(ToDoubleFunction<? super T> mapper) {
        return new CollectorImpl<T, DoubleSummaryStatistics, DoubleSummaryStatistics>(
                DoubleSummaryStatistics::new,
                (r, t) -> r.accept(mapper.applyAsDouble(t)),
                (l, r) -> { l.combine(r); return l; }, CH_ID);
    }

    /**
     * 由 partitioningBy 使用的实现类。
     */
    private static final class Partition<T>
            extends AbstractMap<Boolean, T>
            implements Map<Boolean, T> {
        final T forTrue;
        final T forFalse;

        Partition(T forTrue, T forFalse) {
            this.forTrue = forTrue;
            this.forFalse = forFalse;
        }

        @Override
        public Set<Map.Entry<Boolean, T>> entrySet() {
            return new AbstractSet<Map.Entry<Boolean, T>>() {
                @Override
                public Iterator<Map.Entry<Boolean, T>> iterator() {
                    Map.Entry<Boolean, T> falseEntry = new SimpleImmutableEntry<>(false, forFalse);
                    Map.Entry<Boolean, T> trueEntry = new SimpleImmutableEntry<>(true, forTrue);
                    return Arrays.asList(falseEntry, trueEntry).iterator();
                }

                @Override
                public int size() {
                    return 2;
                }
            };
        }
    }
}
