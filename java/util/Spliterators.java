/*
 * 版权所有 2013，Oracle和/或其附属公司。保留所有权利。
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
package java.util;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * 用于操作或创建 {@link Spliterator} 及其原始特化
 * {@link Spliterator.OfInt}，{@link Spliterator.OfLong} 和
 * {@link Spliterator.OfDouble} 实例的静态类和方法。
 *
 * @see Spliterator
 * @since 1.8
 */
public final class Spliterators {

    // 抑制默认构造函数，确保不可实例化。
    private Spliterators() {}

    // 空的Spliterators

    /**
     * 创建一个空的 {@code Spliterator}
     *
     * <p>空的Spliterator报告 {@link Spliterator#SIZED} 和
     * {@link Spliterator#SUBSIZED}。调用
     * {@link java.util.Spliterator#trySplit()} 始终返回 {@code null}。
     *
     * @param <T> 元素类型
     * @return 一个空的Spliterator
     */
    @SuppressWarnings("unchecked")
    public static <T> Spliterator<T> emptySpliterator() {
        return (Spliterator<T>) EMPTY_SPLITERATOR;
    }

    private static final Spliterator<Object> EMPTY_SPLITERATOR =
            new EmptySpliterator.OfRef<>();

    /**
     * 创建一个空的 {@code Spliterator.OfInt}
     *
     * <p>空的Spliterator报告 {@link Spliterator#SIZED} 和
     * {@link Spliterator#SUBSIZED}。调用
     * {@link java.util.Spliterator#trySplit()} 始终返回 {@code null}。
     *
     * @return 一个空的Spliterator
     */
    public static Spliterator.OfInt emptyIntSpliterator() {
        return EMPTY_INT_SPLITERATOR;
    }

    private static final Spliterator.OfInt EMPTY_INT_SPLITERATOR =
            new EmptySpliterator.OfInt();

    /**
     * 创建一个空的 {@code Spliterator.OfLong}
     *
     * <p>空的Spliterator报告 {@link Spliterator#SIZED} 和
     * {@link Spliterator#SUBSIZED}。调用
     * {@link java.util.Spliterator#trySplit()} 始终返回 {@code null}。
     *
     * @return 一个空的Spliterator
     */
    public static Spliterator.OfLong emptyLongSpliterator() {
        return EMPTY_LONG_SPLITERATOR;
    }

    private static final Spliterator.OfLong EMPTY_LONG_SPLITERATOR =
            new EmptySpliterator.OfLong();

    /**
     * 创建一个空的 {@code Spliterator.OfDouble}
     *
     * <p>空的Spliterator报告 {@link Spliterator#SIZED} 和
     * {@link Spliterator#SUBSIZED}。调用
     * {@link java.util.Spliterator#trySplit()} 始终返回 {@code null}。
     *
     * @return 一个空的Spliterator
     */
    public static Spliterator.OfDouble emptyDoubleSpliterator() {
        return EMPTY_DOUBLE_SPLITERATOR;
    }

    private static final Spliterator.OfDouble EMPTY_DOUBLE_SPLITERATOR =
            new EmptySpliterator.OfDouble();

    // 基于数组的Spliterators

    /**
     * 创建一个覆盖给定数组元素的 {@code Spliterator}，使用自定义的Spliterator特性集。
     *
     * <p>此方法作为实现便利提供给存储其元素部分在数组中的Spliterators，需要对Spliterator特性进行精细控制。大多数其他需要数组的Spliterator的情况应使用
     * {@link Arrays#spliterator(Object[])}。
     *
     * <p>返回的Spliterator始终报告特性
     * {@code SIZED} 和 {@code SUBSIZED}。调用者可以提供额外的特性供Spliterator报告；通常还会指定 {@code IMMUTABLE} 和 {@code ORDERED}。
     *
     * @param <T> 元素类型
     * @param array 假定在使用期间未修改的数组
     * @param additionalCharacteristics 除了始终报告的 {@code SIZED} 和
     *        {@code SUBSIZED} 之外，此Spliterator的源或元素的额外Spliterator特性
     * @return 一个数组的Spliterator
     * @throws NullPointerException 如果给定的数组为 {@code null}
     * @see Arrays#spliterator(Object[])
     */
    public static <T> Spliterator<T> spliterator(Object[] array,
                                                 int additionalCharacteristics) {
        return new ArraySpliterator<>(Objects.requireNonNull(array),
                                      additionalCharacteristics);
    }

    /**
     * 创建一个覆盖给定数组元素范围的 {@code Spliterator}，使用自定义的Spliterator特性集。
     *
     * <p>此方法作为实现便利提供给存储其元素部分在数组中的Spliterators，需要对Spliterator特性进行精细控制。大多数其他需要数组的Spliterator的情况应使用
     * {@link Arrays#spliterator(Object[])}。
     *
     * <p>返回的Spliterator始终报告特性
     * {@code SIZED} 和 {@code SUBSIZED}。调用者可以提供额外的特性供Spliterator报告；通常还会指定 {@code IMMUTABLE} 和 {@code ORDERED}。
     *
     * @param <T> 元素类型
     * @param array 假定在使用期间未修改的数组
     * @param fromIndex 覆盖的最小索引（包含）
     * @param toIndex 覆盖的最大索引（不包含）
     * @param additionalCharacteristics 除了始终报告的 {@code SIZED} 和
     *        {@code SUBSIZED} 之外，此Spliterator的源或元素的额外Spliterator特性
     * @return 一个数组的Spliterator
     * @throws NullPointerException 如果给定的数组为 {@code null}
     * @throws ArrayIndexOutOfBoundsException 如果 {@code fromIndex} 为负数，
     *         {@code toIndex} 小于 {@code fromIndex}，或
     *         {@code toIndex} 大于数组大小
     * @see Arrays#spliterator(Object[], int, int)
     */
    public static <T> Spliterator<T> spliterator(Object[] array, int fromIndex, int toIndex,
                                                 int additionalCharacteristics) {
        checkFromToBounds(Objects.requireNonNull(array).length, fromIndex, toIndex);
        return new ArraySpliterator<>(array, fromIndex, toIndex, additionalCharacteristics);
    }

}


                /**
     * 创建一个覆盖给定数组元素的 {@code Spliterator.OfInt}，
     * 使用一组自定义的Spliterator特性。
     *
     * <p>此方法作为实现便利提供给
     * 将其元素部分存储在数组中的Spliterators，需要
     * 对Spliterator特性的精细控制。大多数其他情况下
     * 需要数组的Spliterator应使用
     * {@link Arrays#spliterator(int[])}。
     *
     * <p>返回的Spliterator始终报告
     * {@code SIZED} 和 {@code SUBSIZED} 特性。调用者可以提供额外的
     * Spliterator特性；通常还会指定 {@code IMMUTABLE} 和 {@code ORDERED}。
     *
     * @param array 假定在使用期间不会被修改的数组
     * @param additionalCharacteristics 除了始终报告的 {@code SIZED} 和 {@code SUBSIZED} 之外，
     *        该Spliterator的源或元素的额外Spliterator特性
     * @return 一个数组的Spliterator
     * @throws NullPointerException 如果给定数组为 {@code null}
     * @see Arrays#spliterator(int[])
     */
    public static Spliterator.OfInt spliterator(int[] array,
                                                int additionalCharacteristics) {
        return new IntArraySpliterator(Objects.requireNonNull(array), additionalCharacteristics);
    }

    /**
     * 创建一个覆盖给定数组元素范围的 {@code Spliterator.OfInt}，
     * 使用一组自定义的Spliterator特性。
     *
     * <p>此方法作为实现便利提供给
     * 将其元素部分存储在数组中的Spliterators，需要
     * 对Spliterator特性的精细控制。大多数其他情况下
     * 需要数组的Spliterator应使用
     * {@link Arrays#spliterator(int[], int, int)}。
     *
     * <p>返回的Spliterator始终报告
     * {@code SIZED} 和 {@code SUBSIZED} 特性。调用者可以提供额外的
     * Spliterator特性；通常还会指定 {@code IMMUTABLE} 和 {@code ORDERED}。
     *
     * @param array 假定在使用期间不会被修改的数组
     * @param fromIndex 覆盖的最小索引（包含）
     * @param toIndex 覆盖的最大索引加一
     * @param additionalCharacteristics 除了始终报告的 {@code SIZED} 和 {@code SUBSIZED} 之外，
     *        该Spliterator的源或元素的额外Spliterator特性
     * @return 一个数组的Spliterator
     * @throws NullPointerException 如果给定数组为 {@code null}
     * @throws ArrayIndexOutOfBoundsException 如果 {@code fromIndex} 为负数，
     *         {@code toIndex} 小于 {@code fromIndex}，或
     *         {@code toIndex} 大于数组大小
     * @see Arrays#spliterator(int[], int, int)
     */
    public static Spliterator.OfInt spliterator(int[] array, int fromIndex, int toIndex,
                                                int additionalCharacteristics) {
        checkFromToBounds(Objects.requireNonNull(array).length, fromIndex, toIndex);
        return new IntArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
    }

    /**
     * 创建一个覆盖给定数组元素的 {@code Spliterator.OfLong}，
     * 使用一组自定义的Spliterator特性。
     *
     * <p>此方法作为实现便利提供给
     * 将其元素部分存储在数组中的Spliterators，需要
     * 对Spliterator特性的精细控制。大多数其他情况下
     * 需要数组的Spliterator应使用
     * {@link Arrays#spliterator(long[])}。
     *
     * <p>返回的Spliterator始终报告
     * {@code SIZED} 和 {@code SUBSIZED} 特性。调用者可以提供额外的
     * Spliterator特性；通常还会指定 {@code IMMUTABLE} 和 {@code ORDERED}。
     *
     * @param array 假定在使用期间不会被修改的数组
     * @param additionalCharacteristics 除了始终报告的 {@code SIZED} 和 {@code SUBSIZED} 之外，
     *        该Spliterator的源或元素的额外Spliterator特性
     * @return 一个数组的Spliterator
     * @throws NullPointerException 如果给定数组为 {@code null}
     * @see Arrays#spliterator(long[])
     */
    public static Spliterator.OfLong spliterator(long[] array,
                                                 int additionalCharacteristics) {
        return new LongArraySpliterator(Objects.requireNonNull(array), additionalCharacteristics);
    }

    /**
     * 创建一个覆盖给定数组元素范围的 {@code Spliterator.OfLong}，
     * 使用一组自定义的Spliterator特性。
     *
     * <p>此方法作为实现便利提供给
     * 将其元素部分存储在数组中的Spliterators，需要
     * 对Spliterator特性的精细控制。大多数其他情况下
     * 需要数组的Spliterator应使用
     * {@link Arrays#spliterator(long[], int, int)}。
     *
     * <p>返回的Spliterator始终报告
     * {@code SIZED} 和 {@code SUBSIZED} 特性。调用者可以提供额外的
     * Spliterator特性。（例如，如果已知数组不会进一步修改，指定 {@code IMMUTABLE}；
     * 如果数组数据被认为具有遇到顺序，指定 {@code ORDERED}）。方法
     * {@link Arrays#spliterator(long[], int, int)} 通常可以替代使用，它返回一个报告
     * {@code SIZED}，{@code SUBSIZED}，{@code IMMUTABLE} 和 {@code ORDERED} 的Spliterator。
     *
     * @param array 假定在使用期间不会被修改的数组
     * @param fromIndex 覆盖的最小索引（包含）
     * @param toIndex 覆盖的最大索引加一
     * @param additionalCharacteristics 除了始终报告的 {@code SIZED} 和 {@code SUBSIZED} 之外，
     *        该Spliterator的源或元素的额外Spliterator特性
     * @return 一个数组的Spliterator
     * @throws NullPointerException 如果给定数组为 {@code null}
     * @throws ArrayIndexOutOfBoundsException 如果 {@code fromIndex} 为负数，
     *         {@code toIndex} 小于 {@code fromIndex}，或
     *         {@code toIndex} 大于数组大小
     * @see Arrays#spliterator(long[], int, int)
     */
    public static Spliterator.OfLong spliterator(long[] array, int fromIndex, int toIndex,
                                                 int additionalCharacteristics) {
        checkFromToBounds(Objects.requireNonNull(array).length, fromIndex, toIndex);
        return new LongArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
    }


                /**
     * 创建一个覆盖给定数组元素的 {@code Spliterator.OfDouble}，
     * 使用一组自定义的拆分器特性。
     *
     * <p>此方法作为实现便利提供，用于存储其元素部分在数组中的拆分器，并且需要对拆分器特性进行精细控制。
     * 大多数其他需要数组拆分器的情况应使用 {@link Arrays#spliterator(double[])}。
     *
     * <p>返回的拆分器始终报告特性 {@code SIZED} 和 {@code SUBSIZED}。调用者可以提供额外的特性，
     * 通常还会指定 {@code IMMUTABLE} 和 {@code ORDERED}。
     *
     * @param array 假定在使用期间不会被修改的数组
     * @param additionalCharacteristics 除了始终报告的 {@code SIZED} 和 {@code SUBSIZED} 之外，
     *        该拆分器的源或元素的额外特性
     * @return 一个数组的拆分器
     * @throws NullPointerException 如果给定的数组为 {@code null}
     * @see Arrays#spliterator(double[])
     */
    public static Spliterator.OfDouble spliterator(double[] array,
                                                   int additionalCharacteristics) {
        return new DoubleArraySpliterator(Objects.requireNonNull(array), additionalCharacteristics);
    }

    /**
     * 创建一个覆盖给定数组元素范围的 {@code Spliterator.OfDouble}，
     * 使用一组自定义的拆分器特性。
     *
     * <p>此方法作为实现便利提供，用于存储其元素部分在数组中的拆分器，并且需要对拆分器特性进行精细控制。
     * 大多数其他需要数组拆分器的情况应使用 {@link Arrays#spliterator(double[], int, int)}。
     *
     * <p>返回的拆分器始终报告特性 {@code SIZED} 和 {@code SUBSIZED}。调用者可以提供额外的特性。
     * （例如，如果已知数组不会进一步修改，指定 {@code IMMUTABLE}；如果数组数据被认为具有遇到顺序，指定
     * {@code ORDERED}）。通常可以使用 {@link Arrays#spliterator(long[], int, int)}，它返回一个报告
     * {@code SIZED}，{@code SUBSIZED}，{@code IMMUTABLE} 和 {@code ORDERED} 的拆分器。
     *
     * @param array 假定在使用期间不会被修改的数组
     * @param fromIndex 覆盖的最小索引（包含）
     * @param toIndex 覆盖的最大索引（不包含）
     * @param additionalCharacteristics 除了始终报告的 {@code SIZED} 和 {@code SUBSIZED} 之外，
     *        该拆分器的源或元素的额外特性
     * @return 一个数组的拆分器
     * @throws NullPointerException 如果给定的数组为 {@code null}
     * @throws ArrayIndexOutOfBoundsException 如果 {@code fromIndex} 为负数，
     *         {@code toIndex} 小于 {@code fromIndex}，或 {@code toIndex} 大于数组长度
     * @see Arrays#spliterator(double[], int, int)
     */
    public static Spliterator.OfDouble spliterator(double[] array, int fromIndex, int toIndex,
                                                   int additionalCharacteristics) {
        checkFromToBounds(Objects.requireNonNull(array).length, fromIndex, toIndex);
        return new DoubleArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
    }

    /**
     * 验证数组长度的包含开始索引和不包含结束索引。
     * @param arrayLength 数组的长度
     * @param origin 包含的开始索引
     * @param fence 不包含的结束索引
     * @throws ArrayIndexOutOfBoundsException 如果开始索引大于结束索引，开始索引为负数，或结束索引大于数组长度
     */
    private static void checkFromToBounds(int arrayLength, int origin, int fence) {
        if (origin > fence) {
            throw new ArrayIndexOutOfBoundsException(
                    "origin(" + origin + ") > fence(" + fence + ")");
        }
        if (origin < 0) {
            throw new ArrayIndexOutOfBoundsException(origin);
        }
        if (fence > arrayLength) {
            throw new ArrayIndexOutOfBoundsException(fence);
        }
    }

    // 基于迭代器的拆分器

    /**
     * 使用给定集合的 {@link java.util.Collection#iterator()} 作为元素的来源，并报告其 {@link java.util.Collection#size()}
     * 作为初始大小，创建一个 {@code Spliterator}。
     *
     * <p>拆分器是 <em><a href="Spliterator.html#binding">延迟绑定</a></em>，继承了集合迭代器的 <em>快速失败</em> 属性，
     * 并实现了 {@code trySplit} 以允许有限的并行性。
     *
     * @param <T> 元素类型
     * @param c 集合
     * @param characteristics 该拆分器的源或元素的特性。除非提供了 {@code CONCURRENT}，否则还会报告特性 {@code SIZED} 和 {@code SUBSIZED}。
     * @return 从迭代器创建的拆分器
     * @throws NullPointerException 如果给定的集合为 {@code null}
     */
    public static <T> Spliterator<T> spliterator(Collection<? extends T> c,
                                                 int characteristics) {
        return new IteratorSpliterator<>(Objects.requireNonNull(c),
                                         characteristics);
    }

    /**
     * 使用给定的 {@code Iterator} 作为元素的来源，并报告给定的初始大小，创建一个 {@code Spliterator}。
     *
     * <p>拆分器不是 <em><a href="Spliterator.html#binding">延迟绑定</a></em>，继承了迭代器的 <em>快速失败</em> 属性，
     * 并实现了 {@code trySplit} 以允许有限的并行性。
     *
     * <p>应通过拆分器遍历元素。如果在返回拆分器后操作迭代器，或初始报告的大小不等于源中的实际元素数量，则拆分和遍历的行为是未定义的。
     *
     * @param <T> 元素类型
     * @param iterator 源的迭代器
     * @param size 源中的元素数量，报告为初始 {@code estimateSize}
     * @param characteristics 该拆分器的源或元素的特性。除非提供了 {@code CONCURRENT}，否则还会报告特性 {@code SIZED} 和 {@code SUBSIZED}。
     * @return 从迭代器创建的拆分器
     * @throws NullPointerException 如果给定的迭代器为 {@code null}
     */
    public static <T> Spliterator<T> spliterator(Iterator<? extends T> iterator,
                                                 long size,
                                                 int characteristics) {
        return new IteratorSpliterator<>(Objects.requireNonNull(iterator), size,
                                         characteristics);
    }

                /**
     * 使用给定的 {@code Iterator} 作为元素的来源创建一个 {@code Spliterator}，没有初始大小估计。
     *
     * <p>该 spliterator 不是
     * <em><a href="Spliterator.html#binding">延迟绑定</a></em>，继承了迭代器的 <em>快速失败</em> 特性，并实现了
     * {@code trySplit} 以允许有限的并行性。
     *
     * <p>元素的遍历应该通过 spliterator 完成。
     * 如果在返回 spliterator 后操作迭代器，则拆分和遍历的行为是未定义的。
     *
     * @param <T> 元素类型
     * @param iterator 源的迭代器
     * @param characteristics 此 spliterator 的源或元素的特性（如果提供了 {@code SIZED} 和 {@code SUBSIZED}，则会被忽略且不会报告。）
     * @return 从迭代器创建的 spliterator
     * @throws NullPointerException 如果给定的迭代器为 {@code null}
     */
    public static <T> Spliterator<T> spliteratorUnknownSize(Iterator<? extends T> iterator,
                                                            int characteristics) {
        return new IteratorSpliterator<>(Objects.requireNonNull(iterator), characteristics);
    }

    /**
     * 使用给定的 {@code IntStream.IntIterator} 作为元素的来源创建一个 {@code Spliterator.OfInt}，并报告给定的初始大小。
     *
     * <p>该 spliterator 不是
     * <em><a href="Spliterator.html#binding">延迟绑定</a></em>，继承了迭代器的 <em>快速失败</em> 特性，并实现了
     * {@code trySplit} 以允许有限的并行性。
     *
     * <p>元素的遍历应该通过 spliterator 完成。
     * 如果在返回 spliterator 后操作迭代器，或者报告的初始大小与源中的实际元素数量不相等，则拆分和遍历的行为是未定义的。
     *
     * @param iterator 源的迭代器
     * @param size 源中的元素数量，作为初始 {@code estimateSize} 报告。
     * @param characteristics 此 spliterator 的源或元素的特性。除非提供了 {@code CONCURRENT}，否则还会报告 {@code SIZED} 和 {@code SUBSIZED} 特性。
     * @return 从迭代器创建的 spliterator
     * @throws NullPointerException 如果给定的迭代器为 {@code null}
     */
    public static Spliterator.OfInt spliterator(PrimitiveIterator.OfInt iterator,
                                                long size,
                                                int characteristics) {
        return new IntIteratorSpliterator(Objects.requireNonNull(iterator),
                                          size, characteristics);
    }

    /**
     * 使用给定的 {@code IntStream.IntIterator} 作为元素的来源创建一个 {@code Spliterator.OfInt}，没有初始大小估计。
     *
     * <p>该 spliterator 不是
     * <em><a href="Spliterator.html#binding">延迟绑定</a></em>，继承了迭代器的 <em>快速失败</em> 特性，并实现了
     * {@code trySplit} 以允许有限的并行性。
     *
     * <p>元素的遍历应该通过 spliterator 完成。
     * 如果在返回 spliterator 后操作迭代器，则拆分和遍历的行为是未定义的。
     *
     * @param iterator 源的迭代器
     * @param characteristics 此 spliterator 的源或元素的特性（如果提供了 {@code SIZED} 和 {@code SUBSIZED}，则会被忽略且不会报告。）
     * @return 从迭代器创建的 spliterator
     * @throws NullPointerException 如果给定的迭代器为 {@code null}
     */
    public static Spliterator.OfInt spliteratorUnknownSize(PrimitiveIterator.OfInt iterator,
                                                           int characteristics) {
        return new IntIteratorSpliterator(Objects.requireNonNull(iterator), characteristics);
    }

    /**
     * 使用给定的 {@code LongStream.LongIterator} 作为元素的来源创建一个 {@code Spliterator.OfLong}，并报告给定的初始大小。
     *
     * <p>该 spliterator 不是
     * <em><a href="Spliterator.html#binding">延迟绑定</a></em>，继承了迭代器的 <em>快速失败</em> 特性，并实现了
     * {@code trySplit} 以允许有限的并行性。
     *
     * <p>元素的遍历应该通过 spliterator 完成。
     * 如果在返回 spliterator 后操作迭代器，或者报告的初始大小与源中的实际元素数量不相等，则拆分和遍历的行为是未定义的。
     *
     * @param iterator 源的迭代器
     * @param size 源中的元素数量，作为初始 {@code estimateSize} 报告。
     * @param characteristics 此 spliterator 的源或元素的特性。除非提供了 {@code CONCURRENT}，否则还会报告 {@code SIZED} 和 {@code SUBSIZED} 特性。
     * @return 从迭代器创建的 spliterator
     * @throws NullPointerException 如果给定的迭代器为 {@code null}
     */
    public static Spliterator.OfLong spliterator(PrimitiveIterator.OfLong iterator,
                                                 long size,
                                                 int characteristics) {
        return new LongIteratorSpliterator(Objects.requireNonNull(iterator),
                                           size, characteristics);
    }

    /**
     * 使用给定的 {@code LongStream.LongIterator} 作为元素的来源创建一个 {@code Spliterator.OfLong}，没有初始大小估计。
     *
     * <p>该 spliterator 不是
     * <em><a href="Spliterator.html#binding">延迟绑定</a></em>，继承了迭代器的 <em>快速失败</em> 特性，并实现了
     * {@code trySplit} 以允许有限的并行性。
     *
     * <p>元素的遍历应该通过 spliterator 完成。
     * 如果在返回 spliterator 后操作迭代器，则拆分和遍历的行为是未定义的。
     *
     * @param iterator 源的迭代器
     * @param characteristics 此 spliterator 的源或元素的特性（如果提供了 {@code SIZED} 和 {@code SUBSIZED}，则会被忽略且不会报告。）
     * @return 从迭代器创建的 spliterator
     * @throws NullPointerException 如果给定的迭代器为 {@code null}
     */
    public static Spliterator.OfLong spliteratorUnknownSize(PrimitiveIterator.OfLong iterator,
                                                            int characteristics) {
        return new LongIteratorSpliterator(Objects.requireNonNull(iterator), characteristics);
    }


                /**
     * 使用给定的 {@code DoubleStream.DoubleIterator} 作为元素的来源，以及一个给定的初始报告大小，创建一个 {@code Spliterator.OfDouble}。
     *
     * <p>该 spliterator 不是
     * <em><a href="Spliterator.html#binding">延迟绑定</a></em>，继承了迭代器的 <em>快速失败</em> 属性，并实现了
     * {@code trySplit} 以允许有限的并行性。
     *
     * <p>元素的遍历应通过 spliterator 完成。如果在返回 spliterator 后操作迭代器，或者初始报告的大小不等于源中的实际元素数量，则拆分和遍历的行为是未定义的。
     *
     * @param iterator 源的迭代器
     * @param size 源中的元素数量，报告为初始 {@code estimateSize}
     * @param characteristics 此 spliterator 的源或元素的特性。除非提供了 {@code CONCURRENT}，否则还会报告 {@code SIZED} 和 {@code SUBSIZED} 特性。
     * @return 从迭代器创建的 spliterator
     * @throws NullPointerException 如果给定的迭代器为 {@code null}
     */
    public static Spliterator.OfDouble spliterator(PrimitiveIterator.OfDouble iterator,
                                                   long size,
                                                   int characteristics) {
        return new DoubleIteratorSpliterator(Objects.requireNonNull(iterator),
                                             size, characteristics);
    }

    /**
     * 使用给定的 {@code DoubleStream.DoubleIterator} 作为元素的来源，不提供初始大小估计，创建一个 {@code Spliterator.OfDouble}。
     *
     * <p>该 spliterator 不是
     * <em><a href="Spliterator.html#binding">延迟绑定</a></em>，继承了迭代器的 <em>快速失败</em> 属性，并实现了
     * {@code trySplit} 以允许有限的并行性。
     *
     * <p>元素的遍历应通过 spliterator 完成。如果在返回 spliterator 后操作迭代器，则拆分和遍历的行为是未定义的。
     *
     * @param iterator 源的迭代器
     * @param characteristics 此 spliterator 的源或元素的特性（如果提供了 {@code SIZED} 和 {@code SUBSIZED}，则会被忽略且不会报告。）
     * @return 从迭代器创建的 spliterator
     * @throws NullPointerException 如果给定的迭代器为 {@code null}
     */
    public static Spliterator.OfDouble spliteratorUnknownSize(PrimitiveIterator.OfDouble iterator,
                                                              int characteristics) {
        return new DoubleIteratorSpliterator(Objects.requireNonNull(iterator), characteristics);
    }

    // 从 Spliterators 创建 Iterators

    /**
     * 从 {@code Spliterator} 创建一个 {@code Iterator}。
     *
     * <p>元素的遍历应通过迭代器完成。如果在返回迭代器后操作 spliterator，则遍历的行为是未定义的。
     *
     * @param <T> 元素类型
     * @param spliterator spliterator
     * @return 一个迭代器
     * @throws NullPointerException 如果给定的 spliterator 为 {@code null}
     */
    public static<T> Iterator<T> iterator(Spliterator<? extends T> spliterator) {
        Objects.requireNonNull(spliterator);
        class Adapter implements Iterator<T>, Consumer<T> {
            boolean valueReady = false;
            T nextElement;

            @Override
            public void accept(T t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public T next() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }
        }

        return new Adapter();
    }

    /**
     * 从 {@code Spliterator.OfInt} 创建一个 {@code PrimitiveIterator.OfInt}。
     *
     * <p>元素的遍历应通过迭代器完成。如果在返回迭代器后操作 spliterator，则遍历的行为是未定义的。
     *
     * @param spliterator spliterator
     * @return 一个迭代器
     * @throws NullPointerException 如果给定的 spliterator 为 {@code null}
     */
    public static PrimitiveIterator.OfInt iterator(Spliterator.OfInt spliterator) {
        Objects.requireNonNull(spliterator);
        class Adapter implements PrimitiveIterator.OfInt, IntConsumer {
            boolean valueReady = false;
            int nextElement;

            @Override
            public void accept(int t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public int nextInt() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }
        }

        return new Adapter();
    }

    /**
     * 从 {@code Spliterator.OfLong} 创建一个 {@code PrimitiveIterator.OfLong}。
     *
     * <p>元素的遍历应通过迭代器完成。如果在返回迭代器后操作 spliterator，则遍历的行为是未定义的。
     *
     * @param spliterator spliterator
     * @return 一个迭代器
     * @throws NullPointerException 如果给定的 spliterator 为 {@code null}
     */
    public static PrimitiveIterator.OfLong iterator(Spliterator.OfLong spliterator) {
        Objects.requireNonNull(spliterator);
        class Adapter implements PrimitiveIterator.OfLong, LongConsumer {
            boolean valueReady = false;
            long nextElement;


                        @Override
            public void accept(long t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public long nextLong() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }
        }

        return new Adapter();
    }

    /**
     * 从 {@code Spliterator.OfDouble} 创建一个 {@code PrimitiveIterator.OfDouble}。
     *
     * <p>元素的遍历应该通过迭代器完成。
     * 如果在返回迭代器后操作分隔器，则遍历行为是未定义的。
     *
     * @param spliterator 分隔器
     * @return 一个迭代器
     * @throws NullPointerException 如果给定的分隔器为 {@code null}
     */
    public static PrimitiveIterator.OfDouble iterator(Spliterator.OfDouble spliterator) {
        Objects.requireNonNull(spliterator);
        class Adapter implements PrimitiveIterator.OfDouble, DoubleConsumer {
            boolean valueReady = false;
            double nextElement;

            @Override
            public void accept(double t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public double nextDouble() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }
        }

        return new Adapter();
    }

    // 实现

    private static abstract class EmptySpliterator<T, S extends Spliterator<T>, C> {

        EmptySpliterator() { }

        public S trySplit() {
            return null;
        }

        public boolean tryAdvance(C consumer) {
            Objects.requireNonNull(consumer);
            return false;
        }

        public void forEachRemaining(C consumer) {
            Objects.requireNonNull(consumer);
        }

        public long estimateSize() {
            return 0;
        }

        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        private static final class OfRef<T>
                extends EmptySpliterator<T, Spliterator<T>, Consumer<? super T>>
                implements Spliterator<T> {
            OfRef() { }
        }

        private static final class OfInt
                extends EmptySpliterator<Integer, Spliterator.OfInt, IntConsumer>
                implements Spliterator.OfInt {
            OfInt() { }
        }

        private static final class OfLong
                extends EmptySpliterator<Long, Spliterator.OfLong, LongConsumer>
                implements Spliterator.OfLong {
            OfLong() { }
        }

        private static final class OfDouble
                extends EmptySpliterator<Double, Spliterator.OfDouble, DoubleConsumer>
                implements Spliterator.OfDouble {
            OfDouble() { }
        }
    }

    // 基于数组的分隔器

    /**
     * 一个设计用于遍历和分割存储在不可修改的 {@code Object[]} 数组中的元素的分隔器。
     */
    static final class ArraySpliterator<T> implements Spliterator<T> {
        /**
         * 数组，显式类型为 Object[]。与某些其他类（例如 CR 6260652）不同，我们不需要
         * 筛选参数以确保它们确实是 Object[] 类型，只要没有方法写入数组或序列化它，
         * 这里通过将此类定义为 final 来确保。
         */
        private final Object[] array;
        private int index;        // 当前索引，在前进/分割时修改
        private final int fence;  // 最后一个索引的下一个
        private final int characteristics;

        /**
         * 创建一个覆盖给定数组的所有元素的分隔器。
         * @param array 假设在使用期间未修改的数组
         * @param additionalCharacteristics 除了总是报告的 {@code SIZED} 和
         * {@code SUBSIZED} 之外的分隔器的附加特性
         */
        public ArraySpliterator(Object[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        /**
         * 创建一个覆盖给定数组和范围的分隔器
         * @param array 假设在使用期间未修改的数组
         * @param origin 最小索引（包含）
         * @param fence 最大索引的下一个
         * @param additionalCharacteristics 除了总是报告的 {@code SIZED} 和
         * {@code SUBSIZED} 之外的分隔器的附加特性
         */
        public ArraySpliterator(Object[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator<T> trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                   ? null
                   : new ArraySpliterator<>(array, lo, index = mid, characteristics);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            Object[] a; int i, hi; // 提取循环中的访问和检查
            if (action == null)
                throw new NullPointerException();
            if ((a = array).length >= (hi = fence) &&
                (i = index) >= 0 && i < (index = hi)) {
                do { action.accept((T)a[i]); } while (++i < hi);
            }
        }


                    @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                @SuppressWarnings("unchecked") T e = (T) array[index++];
                action.accept(e);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() { return (long)(fence - index); }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super T> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    /**
     * 一个设计用于遍历和拆分存储在不可修改的 {@code int[]} 数组中的元素的 Spliterator.OfInt。
     */
    static final class IntArraySpliterator implements Spliterator.OfInt {
        private final int[] array;
        private int index;        // 当前索引，在前进/拆分时修改
        private final int fence;  // 最后一个索引的下一个位置
        private final int characteristics;

        /**
         * 创建一个覆盖给定数组所有元素的 spliterator。
         * @param array 假设在使用过程中不会修改的数组
         * @param additionalCharacteristics 除 {@code SIZED} 和 {@code SUBSIZED} 之外的额外 spliterator 特性，这些特性总是被报告
         */
        public IntArraySpliterator(int[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        /**
         * 创建一个覆盖给定数组和范围的 spliterator。
         * @param array 假设在使用过程中不会修改的数组
         * @param origin 最小索引（包含）
         * @param fence 最大索引的下一个位置
         * @param additionalCharacteristics 除 {@code SIZED} 和 {@code SUBSIZED} 之外的额外 spliterator 特性，这些特性总是被报告
         */
        public IntArraySpliterator(int[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public OfInt trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                   ? null
                   : new IntArraySpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            int[] a; int i, hi; // 提取循环中的访问和检查
            if (action == null)
                throw new NullPointerException();
            if ((a = array).length >= (hi = fence) &&
                (i = index) >= 0 && i < (index = hi)) {
                do { action.accept(a[i]); } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() { return (long)(fence - index); }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Integer> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    /**
     * 一个设计用于遍历和拆分存储在不可修改的 {@code long[]} 数组中的元素的 Spliterator.OfLong。
     */
    static final class LongArraySpliterator implements Spliterator.OfLong {
        private final long[] array;
        private int index;        // 当前索引，在前进/拆分时修改
        private final int fence;  // 最后一个索引的下一个位置
        private final int characteristics;

        /**
         * 创建一个覆盖给定数组所有元素的 spliterator。
         * @param array 假设在使用过程中不会修改的数组
         * @param additionalCharacteristics 除 {@code SIZED} 和 {@code SUBSIZED} 之外的额外 spliterator 特性，这些特性总是被报告
         */
        public LongArraySpliterator(long[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        /**
         * 创建一个覆盖给定数组和范围的 spliterator。
         * @param array 假设在使用过程中不会修改的数组
         * @param origin 最小索引（包含）
         * @param fence 最大索引的下一个位置
         * @param additionalCharacteristics 除 {@code SIZED} 和 {@code SUBSIZED} 之外的额外 spliterator 特性，这些特性总是被报告
         */
        public LongArraySpliterator(long[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public OfLong trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                   ? null
                   : new LongArraySpliterator(array, lo, index = mid, characteristics);
        }


                    @Override
        public void forEachRemaining(LongConsumer action) {
            long[] a; int i, hi; // 提取循环中的访问和检查
            if (action == null)
                throw new NullPointerException();
            if ((a = array).length >= (hi = fence) &&
                (i = index) >= 0 && i < (index = hi)) {
                do { action.accept(a[i]); } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(LongConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() { return (long)(fence - index); }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Long> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    /**
     * 一个设计用于遍历和分割存储在不可修改的 {@code int[]} 数组中的元素的 Spliterator.OfDouble。
     */
    static final class DoubleArraySpliterator implements Spliterator.OfDouble {
        private final double[] array;
        private int index;        // 当前索引，在前进/分割时修改
        private final int fence;  // 最后一个索引的下一个
        private final int characteristics;

        /**
         * 创建一个覆盖整个给定数组的 spliterator。
         * @param array 假定在使用期间不被修改的数组
         * @param additionalCharacteristics 除了总是报告的 {@code SIZED} 和 {@code SUBSIZED} 之外，此 spliterator 的源或元素的其他 spliterator 特性
         */
        public DoubleArraySpliterator(double[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        /**
         * 创建一个覆盖给定数组和范围的 spliterator。
         * @param array 假定在使用期间不被修改的数组
         * @param origin 覆盖的最小索引（包含）
         * @param fence 覆盖的最大索引的下一个
         * @param additionalCharacteristics 除了总是报告的 {@code SIZED} 和 {@code SUBSIZED} 之外，此 spliterator 的源或元素的其他 spliterator 特性
         */
        public DoubleArraySpliterator(double[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public OfDouble trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                   ? null
                   : new DoubleArraySpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(DoubleConsumer action) {
            double[] a; int i, hi; // 提取循环中的访问和检查
            if (action == null)
                throw new NullPointerException();
            if ((a = array).length >= (hi = fence) &&
                (i = index) >= 0 && i < (index = hi)) {
                do { action.accept(a[i]); } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(DoubleConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() { return (long)(fence - index); }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Double> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    //

    /**
     * 一个抽象的 {@code Spliterator}，实现了 {@code trySplit} 以允许有限的并行性。
     *
     * <p>扩展类只需要实现 {@link #tryAdvance(java.util.function.Consumer) tryAdvance}。
     * 如果扩展类可以提供更高效的实现，应该覆盖 {@link #forEachRemaining(java.util.function.Consumer) forEach}。
     *
     * @apiNote
     * 当无法或难以高效地以允许平衡并行计算的方式分区元素时，此类是一个有用的辅助工具。
     *
     * <p>除了使用此类之外，允许有限并行性的另一种方法是通过迭代器创建 spliterator
     * （参见 {@link #spliterator(Iterator, long, int)}。根据情况，使用迭代器可能比扩展此类更容易或更方便，例如，当已经有可用的迭代器时。
     *
     * @see #spliterator(Iterator, long, int)
     * @since 1.8
     */
    public static abstract class AbstractSpliterator<T> implements Spliterator<T> {
        static final int BATCH_UNIT = 1 << 10;  // 批处理数组大小增量
        static final int MAX_BATCH = 1 << 25;  // 最大批处理数组大小
        private final int characteristics;
        private long est;             // 大小估计
        private int batch;            // 分割的批处理大小

        /**
         * 创建一个报告给定估计大小和 additionalCharacteristics 的 spliterator。
         *
         * @param est 如果已知，此 spliterator 的估计大小，否则为 {@code Long.MAX_VALUE}。
         * @param additionalCharacteristics 此 spliterator 的源或元素的属性。如果报告了 {@code SIZED}，则此 spliterator 还将报告 {@code SUBSIZED}。
         */
        protected AbstractSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                                   ? additionalCharacteristics | Spliterator.SUBSIZED
                                   : additionalCharacteristics;
        }


                    static final class HoldingConsumer<T> implements Consumer<T> {
            Object value;

            @Override
            public void accept(T value) {
                this.value = value;
            }
        }

        /**
         * {@inheritDoc}
         *
         * 此实现允许有限的并行性。
         */
        @Override
        public Spliterator<T> trySplit() {
            /*
             * 按照算术递增的批次大小进行拆分。
             * 仅当每个元素的 Consumer 操作比将它们传输到数组中更昂贵时，这才会提高并行性能。
             * 在拆分大小中使用算术级数提供了开销与并行性界限，这些界限不会特别偏袒或惩罚轻量级与重量级元素操作的情况，无论元素数量与核心数量是否已知。
             * 我们生成 O(sqrt(#elements)) 拆分，允许 O(sqrt(#cores)) 的潜在加速。
             */
            HoldingConsumer<T> holder = new HoldingConsumer<>();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do { a[j] = holder.value; } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new ArraySpliterator<>(a, 0, j, characteristics());
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * 此实现返回创建时报告的估计大小，并且如果估计大小已知，则在拆分时会减小。
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * 此实现返回创建时报告的特性。
         */
        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    /**
     * 一个抽象的 {@code Spliterator.OfInt}，实现了 {@code trySplit} 以允许有限的并行性。
     *
     * <p>为了实现一个拆分器，扩展类只需要实现 {@link #tryAdvance(java.util.function.IntConsumer)}
     * tryAdvance}。如果扩展类可以提供更高效的实现，应覆盖 {@link #forEachRemaining(java.util.function.IntConsumer)} forEach}。
     *
     * @apiNote
     * 当无法或难以高效地以允许平衡并行计算的方式对元素进行分区时，此类是创建拆分器的有用辅助工具。
     *
     * <p>除了使用此类之外，允许有限并行性的另一种方法是使用迭代器创建拆分器
     * （参见 {@link #spliterator(java.util.PrimitiveIterator.OfInt, long, int)}。
     * 根据情况，使用迭代器可能比扩展此类更容易或更方便。例如，如果已经有可用的迭代器，则无需扩展此类。
     *
     * @see #spliterator(java.util.PrimitiveIterator.OfInt, long, int)
     * @since 1.8
     */
    public static abstract class AbstractIntSpliterator implements Spliterator.OfInt {
        static final int MAX_BATCH = AbstractSpliterator.MAX_BATCH;
        static final int BATCH_UNIT = AbstractSpliterator.BATCH_UNIT;
        private final int characteristics;
        private long est;             // 大小估计
        private int batch;            // 拆分的批次大小

        /**
         * 创建一个报告给定估计大小和特性的拆分器。
         *
         * @param est 如果已知，此拆分器的估计大小，否则为 {@code Long.MAX_VALUE}。
         * @param additionalCharacteristics 此拆分器的源或元素的属性。如果报告了 {@code SIZED}，则此拆分器还将报告 {@code SUBSIZED}。
         */
        protected AbstractIntSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                                   ? additionalCharacteristics | Spliterator.SUBSIZED
                                   : additionalCharacteristics;
        }

        static final class HoldingIntConsumer implements IntConsumer {
            int value;

            @Override
            public void accept(int value) {
                this.value = value;
            }
        }

        /**
         * {@inheritDoc}
         *
         * 此实现允许有限的并行性。
         */
        @Override
        public Spliterator.OfInt trySplit() {
            HoldingIntConsumer holder = new HoldingIntConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                int[] a = new int[n];
                int j = 0;
                do { a[j] = holder.value; } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new IntArraySpliterator(a, 0, j, characteristics());
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * 此实现返回创建时报告的估计大小，并且如果估计大小已知，则在拆分时会减小。
         */
        @Override
        public long estimateSize() {
            return est;
        }


                    /**
         * {@inheritDoc}
         *
         * @implSpec
         * 此实现返回创建时报告的特性。
         */
        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    /**
     * 一个抽象的 {@code Spliterator.OfLong}，实现了 {@code trySplit}
     * 以允许有限的并行性。
     *
     * <p>为了实现一个拆分器，扩展类只需要实现 {@link #tryAdvance(java.util.function.LongConsumer)}
     * tryAdvance}。如果扩展类可以提供更高效的实现，应覆盖
     * {@link #forEachRemaining(java.util.function.LongConsumer)} forEach}。
     *
     * @apiNote
     * 当无法或难以高效地以允许平衡并行计算的方式对元素进行分区时，此类是创建拆分器的有用辅助工具。
     *
     * <p>除了使用此类外，还可以从迭代器创建拆分器
     * (参见 {@link #spliterator(java.util.PrimitiveIterator.OfLong, long, int)}。
     * 根据情况，使用迭代器可能比扩展此类更简单或更方便。例如，如果已经有可用的迭代器，则无需扩展此类。
     *
     * @see #spliterator(java.util.PrimitiveIterator.OfLong, long, int)
     * @since 1.8
     */
    public static abstract class AbstractLongSpliterator implements Spliterator.OfLong {
        static final int MAX_BATCH = AbstractSpliterator.MAX_BATCH;
        static final int BATCH_UNIT = AbstractSpliterator.BATCH_UNIT;
        private final int characteristics;
        private long est;             // 大小估计
        private int batch;            // 拆分的批处理大小

        /**
         * 创建一个报告给定估计大小和特性的拆分器。
         *
         * @param est 如果已知，此拆分器的估计大小，否则为
         *        {@code Long.MAX_VALUE}。
         * @param additionalCharacteristics 此拆分器的源或元素的属性。如果报告了 {@code SIZED}，则此
         *        拆分器还将报告 {@code SUBSIZED}。
         */
        protected AbstractLongSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                                   ? additionalCharacteristics | Spliterator.SUBSIZED
                                   : additionalCharacteristics;
        }

        static final class HoldingLongConsumer implements LongConsumer {
            long value;

            @Override
            public void accept(long value) {
                this.value = value;
            }
        }

        /**
         * {@inheritDoc}
         *
         * 此实现允许有限的并行性。
         */
        @Override
        public Spliterator.OfLong trySplit() {
            HoldingLongConsumer holder = new HoldingLongConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                long[] a = new long[n];
                int j = 0;
                do { a[j] = holder.value; } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new LongArraySpliterator(a, 0, j, characteristics());
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * 此实现返回创建时报告的估计大小，并且如果已知估计大小，在拆分时会减少。
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * 此实现返回创建时报告的特性。
         */
        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    /**
     * 一个抽象的 {@code Spliterator.OfDouble}，实现了 {@code trySplit}
     * 以允许有限的并行性。
     *
     * <p>为了实现一个拆分器，扩展类只需要实现 {@link #tryAdvance(java.util.function.DoubleConsumer)}
     * tryAdvance}。如果扩展类可以提供更高效的实现，应覆盖
     * {@link #forEachRemaining(java.util.function.DoubleConsumer)} forEach}。
     *
     * @apiNote
     * 当无法或难以高效地以允许平衡并行计算的方式对元素进行分区时，此类是创建拆分器的有用辅助工具。
     *
     * <p>除了使用此类外，还可以从迭代器创建拆分器
     * (参见 {@link #spliterator(java.util.PrimitiveIterator.OfDouble, long, int)}。
     * 根据情况，使用迭代器可能比扩展此类更简单或更方便。例如，如果已经有可用的迭代器，则无需扩展此类。
     *
     * @see #spliterator(java.util.PrimitiveIterator.OfDouble, long, int)
     * @since 1.8
     */
    public static abstract class AbstractDoubleSpliterator implements Spliterator.OfDouble {
        static final int MAX_BATCH = AbstractSpliterator.MAX_BATCH;
        static final int BATCH_UNIT = AbstractSpliterator.BATCH_UNIT;
        private final int characteristics;
        private long est;             // 大小估计
        private int batch;            // 拆分的批处理大小


                    /**
         * 创建一个报告给定估计大小和特性的 Spliterator。
         *
         * @param est 如果已知，此 Spliterator 的估计大小，否则为 {@code Long.MAX_VALUE}。
         * @param additionalCharacteristics 此 Spliterator 的源或元素的属性。如果报告了 {@code SIZED}，
         *        则此 Spliterator 还将报告 {@code SUBSIZED}。
         */
        protected AbstractDoubleSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                                   ? additionalCharacteristics | Spliterator.SUBSIZED
                                   : additionalCharacteristics;
        }

        static final class HoldingDoubleConsumer implements DoubleConsumer {
            double value;

            @Override
            public void accept(double value) {
                this.value = value;
            }
        }

        /**
         * {@inheritDoc}
         *
         * 此实现允许有限的并行性。
         */
        @Override
        public Spliterator.OfDouble trySplit() {
            HoldingDoubleConsumer holder = new HoldingDoubleConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                double[] a = new double[n];
                int j = 0;
                do { a[j] = holder.value; } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new DoubleArraySpliterator(a, 0, j, characteristics());
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * 此实现返回创建时报告的估计大小，并且如果估计大小已知，在拆分时会减少。
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * 此实现返回创建时报告的特性。
         */
        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    // 基于 Iterator 的 Spliterators

    /**
     * 使用给定的 Iterator 进行元素操作的 Spliterator。该 Spliterator 实现了 {@code trySplit} 以
     * 允许有限的并行性。
     */
    static class IteratorSpliterator<T> implements Spliterator<T> {
        static final int BATCH_UNIT = 1 << 10;  // 批处理数组大小增量
        static final int MAX_BATCH = 1 << 25;  // 最大批处理数组大小
        private final Collection<? extends T> collection; // 可以为 null
        private Iterator<? extends T> it;
        private final int characteristics;
        private long est;             // 大小估计
        private int batch;            // 拆分的批处理大小

        /**
         * 创建一个使用给定集合的 {@link java.util.Collection#iterator()} 进行遍历的 Spliterator，
         * 并报告其 {@link java.util.Collection#size()} 作为其初始大小。
         *
         * @param c 集合
         * @param characteristics 此 Spliterator 的源或元素的属性。
         */
        public IteratorSpliterator(Collection<? extends T> collection, int characteristics) {
            this.collection = collection;
            this.it = null;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                                   ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                                   : characteristics;
        }

        /**
         * 创建一个使用给定迭代器进行遍历的 Spliterator，并报告给定的初始大小和特性。
         *
         * @param iterator 源的迭代器
         * @param size 源中的元素数量
         * @param characteristics 此 Spliterator 的源或元素的属性。
         */
        public IteratorSpliterator(Iterator<? extends T> iterator, long size, int characteristics) {
            this.collection = null;
            this.it = iterator;
            this.est = size;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                                   ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                                   : characteristics;
        }

        /**
         * 创建一个使用给定迭代器进行遍历的 Spliterator，并报告给定的初始大小和特性。
         *
         * @param iterator 源的迭代器
         * @param characteristics 此 Spliterator 的源或元素的属性。
         */
        public IteratorSpliterator(Iterator<? extends T> iterator, int characteristics) {
            this.collection = null;
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public Spliterator<T> trySplit() {
            /*
             * 将其拆分为算术递增的批处理大小。这只有在每个元素的 Consumer 操作比将它们传输到数组中更昂贵时，
             * 才能提高并行性能。使用算术级数的拆分大小提供了不会特别偏爱或惩罚轻量级与重量级元素操作的开销
             * 与并行性界限，适用于 #elements 与 #cores 的组合，无论这些组合是否已知。我们生成 O(sqrt(#elements))
             * 次拆分，允许 O(sqrt(#cores)) 的潜在加速。
             */
            Iterator<? extends T> i;
            long s;
            if ((i = it) == null) {
                i = it = collection.iterator();
                s = est = (long) collection.size();
            }
            else
                s = est;
            if (s > 1 && i.hasNext()) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do { a[j] = i.next(); } while (++j < n && i.hasNext());
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new ArraySpliterator<>(a, 0, j, characteristics);
            }
            return null;
        }


                    @Override
        public void forEachRemaining(Consumer<? super T> action) {
            if (action == null) throw new NullPointerException();
            Iterator<? extends T> i;
            if ((i = it) == null) {
                i = it = collection.iterator();
                est = (long)collection.size();
            }
            i.forEachRemaining(action);
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (action == null) throw new NullPointerException();
            if (it == null) {
                it = collection.iterator();
                est = (long) collection.size();
            }
            if (it.hasNext()) {
                action.accept(it.next());
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            if (it == null) {
                it = collection.iterator();
                return est = (long)collection.size();
            }
            return est;
        }

        @Override
        public int characteristics() { return characteristics; }

        @Override
        public Comparator<? super T> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    /**
     * 使用给定的 IntStream.IntIterator 进行元素操作的 Spliterator.OfInt。
     * 该迭代器实现了 {@code trySplit} 以允许有限的并行性。
     */
    static final class IntIteratorSpliterator implements Spliterator.OfInt {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private PrimitiveIterator.OfInt it;
        private final int characteristics;
        private long est;             // 大小估计
        private int batch;            // 分割的批处理大小

        /**
         * 创建一个使用给定迭代器进行遍历的迭代器，
         * 并报告给定的初始大小和特性。
         *
         * @param iterator 源的迭代器
         * @param size 源中的元素数量
         * @param characteristics 此迭代器的源或元素的属性。
         */
        public IntIteratorSpliterator(PrimitiveIterator.OfInt iterator, long size, int characteristics) {
            this.it = iterator;
            this.est = size;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                                   ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                                   : characteristics;
        }

        /**
         * 创建一个使用给定迭代器的迭代器，源的大小未知，
         * 并报告给定的特性。
         *
         * @param iterator 源的迭代器
         * @param characteristics 此迭代器的源或元素的属性。
         */
        public IntIteratorSpliterator(PrimitiveIterator.OfInt iterator, int characteristics) {
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public OfInt trySplit() {
            PrimitiveIterator.OfInt i = it;
            long s = est;
            if (s > 1 && i.hasNext()) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                int[] a = new int[n];
                int j = 0;
                do { a[j] = i.nextInt(); } while (++j < n && i.hasNext());
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new IntArraySpliterator(a, 0, j, characteristics);
            }
            return null;
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            if (action == null) throw new NullPointerException();
            it.forEachRemaining(action);
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            if (action == null) throw new NullPointerException();
            if (it.hasNext()) {
                action.accept(it.nextInt());
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return est;
        }

        @Override
        public int characteristics() { return characteristics; }

        @Override
        public Comparator<? super Integer> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    static final class LongIteratorSpliterator implements Spliterator.OfLong {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private PrimitiveIterator.OfLong it;
        private final int characteristics;
        private long est;             // 大小估计
        private int batch;            // 分割的批处理大小

        /**
         * 创建一个使用给定迭代器进行遍历的迭代器，
         * 并报告给定的初始大小和特性。
         *
         * @param iterator 源的迭代器
         * @param size 源中的元素数量
         * @param characteristics 此迭代器的源或元素的属性。
         */
        public LongIteratorSpliterator(PrimitiveIterator.OfLong iterator, long size, int characteristics) {
            this.it = iterator;
            this.est = size;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                                   ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                                   : characteristics;
        }


                    /**
         * 使用给定的迭代器创建一个分隔器，该迭代器用于未知大小的源，并报告给定的特性。
         *
         * @param iterator 源的迭代器
         * @param characteristics 该分隔器的源或元素的属性。
         */
        public LongIteratorSpliterator(PrimitiveIterator.OfLong iterator, int characteristics) {
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public OfLong trySplit() {
            PrimitiveIterator.OfLong i = it;
            long s = est;
            if (s > 1 && i.hasNext()) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                long[] a = new long[n];
                int j = 0;
                do { a[j] = i.nextLong(); } while (++j < n && i.hasNext());
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new LongArraySpliterator(a, 0, j, characteristics);
            }
            return null;
        }

        @Override
        public void forEachRemaining(LongConsumer action) {
            if (action == null) throw new NullPointerException();
            it.forEachRemaining(action);
        }

        @Override
        public boolean tryAdvance(LongConsumer action) {
            if (action == null) throw new NullPointerException();
            if (it.hasNext()) {
                action.accept(it.nextLong());
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return est;
        }

        @Override
        public int characteristics() { return characteristics; }

        @Override
        public Comparator<? super Long> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    static final class DoubleIteratorSpliterator implements Spliterator.OfDouble {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private PrimitiveIterator.OfDouble it;
        private final int characteristics;
        private long est;             // 大小估计
        private int batch;            // 分割的批次大小

        /**
         * 使用给定的迭代器创建一个分隔器，用于遍历，并报告给定的初始大小和特性。
         *
         * @param iterator 源的迭代器
         * @param size 源中的元素数量
         * @param characteristics 该分隔器的源或元素的属性。
         */
        public DoubleIteratorSpliterator(PrimitiveIterator.OfDouble iterator, long size, int characteristics) {
            this.it = iterator;
            this.est = size;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                                   ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                                   : characteristics;
        }

        /**
         * 使用给定的迭代器创建一个分隔器，该迭代器用于未知大小的源，并报告给定的特性。
         *
         * @param iterator 源的迭代器
         * @param characteristics 该分隔器的源或元素的属性。
         */
        public DoubleIteratorSpliterator(PrimitiveIterator.OfDouble iterator, int characteristics) {
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public OfDouble trySplit() {
            PrimitiveIterator.OfDouble i = it;
            long s = est;
            if (s > 1 && i.hasNext()) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                double[] a = new double[n];
                int j = 0;
                do { a[j] = i.nextDouble(); } while (++j < n && i.hasNext());
                batch = j;
                if (est != Long.MAX_VALUE)
                    est -= j;
                return new DoubleArraySpliterator(a, 0, j, characteristics);
            }
            return null;
        }

        @Override
        public void forEachRemaining(DoubleConsumer action) {
            if (action == null) throw new NullPointerException();
            it.forEachRemaining(action);
        }

        @Override
        public boolean tryAdvance(DoubleConsumer action) {
            if (action == null) throw new NullPointerException();
            if (it.hasNext()) {
                action.accept(it.nextDouble());
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return est;
        }

        @Override
        public int characteristics() { return characteristics; }

        @Override
        public Comparator<? super Double> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }
}
