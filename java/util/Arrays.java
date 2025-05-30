
/*
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.Array;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 该类包含各种用于操作数组的方法（如排序和搜索）。该类还包含一个静态工厂，允许将数组视为列表。
 *
 * <p>该类中的所有方法如果指定的数组引用为空，都会抛出 {@code NullPointerException}，除非另有说明。
 *
 * <p>该类中方法的文档包括对其 <i>实现</i> 的简要描述。这些描述应被视为 <i>实现说明</i>，而不是 <i>规范</i> 的一部分。实现者可以自由地替换其他算法，只要遵循规范本身即可。（例如，用于 {@code sort(Object[])} 的算法不必是归并排序，但必须是 <i>稳定的</i>。）
 *
 * <p>该类是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a> 的成员。
 *
 * @author Josh Bloch
 * @author Neal Gafter
 * @author John Rose
 * @since  1.2
 */
public class Arrays {

    /**
     * 并行排序算法不会进一步划分排序任务的最小数组长度。使用更小的大小通常会导致任务之间的内存争用，使得并行加速不太可能。
     */
    private static final int MIN_ARRAY_SORT_GRAN = 1 << 13;

    // 抑制默认构造函数，确保不可实例化。
    private Arrays() {}

    /**
     * 一个实现自然排序的比较器，当提供的比较器为空时可以使用。为了简化底层实现中的代码共享，compare 方法仅声明类型 Object 作为其第二个参数。
     *
     * Arrays 类实现者注意：ComparableTimSort 是否比使用此比较器的 TimSort 提供任何性能优势是一个经验问题。如果没有，你最好删除或绕过 ComparableTimSort。目前没有将它们分开用于并行排序的实证依据，因此所有公共 Object 并行排序方法都使用基于相同比较器的实现。
     */
    static final class NaturalOrder implements Comparator<Object> {
        @SuppressWarnings("unchecked")
        public int compare(Object first, Object second) {
            return ((Comparable<Object>)first).compareTo(second);
        }
        static final NaturalOrder INSTANCE = new NaturalOrder();
    }

    /**
     * 检查 {@code fromIndex} 和 {@code toIndex} 是否在范围内，如果不在范围内则抛出异常。
     */
    private static void rangeCheck(int arrayLength, int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException(
                    "fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        if (fromIndex < 0) {
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        }
        if (toIndex > arrayLength) {
            throw new ArrayIndexOutOfBoundsException(toIndex);
        }
    }

    /*
     * 排序方法。注意所有公共 "sort" 方法的形式：如果需要，执行参数检查，然后将参数扩展为其他包私有类中的内部实现方法所需的参数（除了包含在该类中的 legacyMergeSort）。
     */

    /**
     * 按升序对指定数组进行排序。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的双轴快速排序。该算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，并且通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     */
    public static void sort(int[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
    }

    /**
     * 按升序对指定数组的范围进行排序。要排序的范围从索引 {@code fromIndex}（包含）到索引 {@code toIndex}（不包含）。如果 {@code fromIndex == toIndex}，则要排序的范围为空。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的双轴快速排序。该算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，并且通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包含）
     * @param toIndex 要排序的最后一个元素的索引（不包含）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     */
    public static void sort(int[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
    }

    /**
     * 按升序对指定数组进行排序。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的双轴快速排序。该算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，并且通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     */
    public static void sort(long[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
    }

    /**
     * 按升序对指定数组的范围进行排序。要排序的范围从索引 {@code fromIndex}（包含）到索引 {@code toIndex}（不包含）。如果 {@code fromIndex == toIndex}，则要排序的范围为空。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的双轴快速排序。该算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，并且通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包含）
     * @param toIndex 要排序的最后一个元素的索引（不包含）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     */
    public static void sort(long[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
    }

    /**
     * 按升序对指定数组进行排序。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的双轴快速排序。该算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，并且通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     */
    public static void sort(short[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
    }

    /**
     * 按升序对指定数组的范围进行排序。要排序的范围从索引 {@code fromIndex}（包含）到索引 {@code toIndex}（不包含）。如果 {@code fromIndex == toIndex}，则要排序的范围为空。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的双轴快速排序。该算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，并且通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包含）
     * @param toIndex 要排序的最后一个元素的索引（不包含）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     */
    public static void sort(short[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
    }

    /**
     * 按升序对指定数组进行排序。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的双轴快速排序。该算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，并且通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     */
    public static void sort(char[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
    }

    /**
     * 按升序对指定数组的范围进行排序。要排序的范围从索引 {@code fromIndex}（包含）到索引 {@code toIndex}（不包含）。如果 {@code fromIndex == toIndex}，则要排序的范围为空。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的双轴快速排序。该算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，并且通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包含）
     * @param toIndex 要排序的最后一个元素的索引（不包含）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     */
    public static void sort(char[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
    }

    /**
     * 按升序对指定数组进行排序。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的双轴快速排序。该算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，并且通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     */
    public static void sort(byte[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1);
    }

    /**
     * 按升序对指定数组的范围进行排序。要排序的范围从索引 {@code fromIndex}（包含）到索引 {@code toIndex}（不包含）。如果 {@code fromIndex == toIndex}，则要排序的范围为空。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的双轴快速排序。该算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，并且通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包含）
     * @param toIndex 要排序的最后一个元素的索引（不包含）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     */
    public static void sort(byte[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1);
    }

    /**
     * 按升序对指定数组进行排序。
     *
     * <p>{@code <} 关系不提供所有浮点值的全序：{@code -0.0f == 0.0f} 为 {@code true}，且 {@code Float.NaN} 值既不小于、也不大于、也不等于任何值，包括其自身。此方法使用 {@link Float#compareTo} 方法施加的全序：{@code -0.0f} 被视为小于值 {@code 0.0f}，且 {@code Float.NaN} 被视为大于任何其他值，所有 {@code Float.NaN} 值被视为相等。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的双轴快速排序。该算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，并且通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     */
    public static void sort(float[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
    }


                /**
     * 按升序对指定范围的数组进行排序。要排序的范围从索引 {@code fromIndex}（包含）到
     * 索引 {@code toIndex}（不包含）。如果 {@code fromIndex == toIndex}，
     * 则要排序的范围为空。
     *
     * <p>{@code <} 关系不提供对所有 float 值的全序：{@code -0.0f == 0.0f} 为 {@code true}，
     * 且 {@code Float.NaN} 值既不小于、也不大于、也不等于任何值，包括其自身。此方法使用
     * {@link Float#compareTo} 方法强制的全序：{@code -0.0f} 被视为小于值 {@code 0.0f}，
     * 且 {@code Float.NaN} 被视为大于任何其他值，所有 {@code Float.NaN} 值都被视为相等。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的
     * 双轴快速排序。此算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，
     * 通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包含）
     * @param toIndex 要排序的最后一个元素的索引（不包含）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     */
    public static void sort(float[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
    }

    /**
     * 按升序对指定数组进行数值排序。
     *
     * <p>{@code <} 关系不提供对所有 double 值的全序：{@code -0.0d == 0.0d} 为 {@code true}，
     * 且 {@code Double.NaN} 值既不小于、也不大于、也不等于任何值，包括其自身。此方法使用
     * {@link Double#compareTo} 方法强制的全序：{@code -0.0d} 被视为小于值 {@code 0.0d}，
     * 且 {@code Double.NaN} 被视为大于任何其他值，所有 {@code Double.NaN} 值都被视为相等。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的
     * 双轴快速排序。此算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，
     * 通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     */
    public static void sort(double[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
    }

    /**
     * 按升序对指定范围的数组进行排序。要排序的范围从索引 {@code fromIndex}（包含）到
     * 索引 {@code toIndex}（不包含）。如果 {@code fromIndex == toIndex}，
     * 则要排序的范围为空。
     *
     * <p>{@code <} 关系不提供对所有 double 值的全序：{@code -0.0d == 0.0d} 为 {@code true}，
     * 且 {@code Double.NaN} 值既不小于、也不大于、也不等于任何值，包括其自身。此方法使用
     * {@link Double#compareTo} 方法强制的全序：{@code -0.0d} 被视为小于值 {@code 0.0d}，
     * 且 {@code Double.NaN} 被视为大于任何其他值，所有 {@code Double.NaN} 值都被视为相等。
     *
     * <p>实现说明：排序算法是 Vladimir Yaroslavskiy、Jon Bentley 和 Joshua Bloch 的
     * 双轴快速排序。此算法在许多数据集上提供 O(n log(n)) 性能，这些数据集会导致其他快速排序退化为二次性能，
     * 通常比传统的（单轴）快速排序实现更快。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包含）
     * @param toIndex 要排序的最后一个元素的索引（不包含）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     */
    public static void sort(double[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
    }

    /**
     * 按升序对指定数组进行数值排序。
     *
     * @implNote 排序算法是一种并行排序-合并算法，将数组分解为子数组，这些子数组本身被排序然后合并。
     * 当子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(byte[]) Arrays.sort} 方法进行排序。
     * 如果指定数组的长度小于最小粒度，则使用适当的 {@link Arrays#sort(byte[]) Arrays.sort} 方法进行排序。
     * 该算法所需的临时空间不超过原始数组的大小。使用 {@link ForkJoinPool#commonPool() ForkJoin 公共池}
     * 执行任何并行任务。
     *
     * @param a 要排序的数组
     *
     * @since 1.8
     */
    public static void parallelSort(byte[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1);
        else
            new ArraysParallelSortHelpers.FJByte.Sorter
                (null, a, new byte[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * 按升序对指定范围的数组进行数值排序。要排序的范围从索引 {@code fromIndex}（包含）到
     * 索引 {@code toIndex}（不包含）。如果 {@code fromIndex == toIndex}，
     * 则要排序的范围为空。
     *
     * @implNote 排序算法是一种并行排序-合并算法，将数组分解为子数组，这些子数组本身被排序然后合并。
     * 当子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(byte[]) Arrays.sort} 方法进行排序。
     * 如果指定数组的长度小于最小粒度，则使用适当的 {@link Arrays#sort(byte[]) Arrays.sort} 方法进行排序。
     * 该算法所需的临时空间不超过指定范围的原始数组的大小。使用 {@link ForkJoinPool#commonPool() ForkJoin 公共池}
     * 执行任何并行任务。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包含）
     * @param toIndex 要排序的最后一个元素的索引（不包含）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(byte[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1);
        else
            new ArraysParallelSortHelpers.FJByte.Sorter
                (null, a, new byte[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * 按升序对指定数组进行数值排序。
     *
     * @implNote 排序算法是一种并行排序-合并算法，将数组分解为子数组，这些子数组本身被排序然后合并。
     * 当子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(char[]) Arrays.sort} 方法进行排序。
     * 如果指定数组的长度小于最小粒度，则使用适当的 {@link Arrays#sort(char[]) Arrays.sort} 方法进行排序。
     * 该算法所需的临时空间不超过原始数组的大小。使用 {@link ForkJoinPool#commonPool() ForkJoin 公共池}
     * 执行任何并行任务。
     *
     * @param a 要排序的数组
     *
     * @since 1.8
     */
    public static void parallelSort(char[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJChar.Sorter
                (null, a, new char[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * 按升序对指定范围的数组进行数值排序。要排序的范围从索引 {@code fromIndex}（包含）到
     * 索引 {@code toIndex}（不包含）。如果 {@code fromIndex == toIndex}，
     * 则要排序的范围为空。
     *
     * @implNote 排序算法是一种并行排序-合并算法，将数组分解为子数组，这些子数组本身被排序然后合并。
     * 当子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(char[]) Arrays.sort} 方法进行排序。
     * 如果指定数组的长度小于最小粒度，则使用适当的 {@link Arrays#sort(char[]) Arrays.sort} 方法进行排序。
     * 该算法所需的临时空间不超过指定范围的原始数组的大小。使用 {@link ForkJoinPool#commonPool() ForkJoin 公共池}
     * 执行任何并行任务。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包含）
     * @param toIndex 要排序的最后一个元素的索引（不包含）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(char[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJChar.Sorter
                (null, a, new char[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * 按升序对指定数组进行数值排序。
     *
     * @implNote 排序算法是一种并行排序-合并算法，将数组分解为子数组，这些子数组本身被排序然后合并。
     * 当子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(short[]) Arrays.sort} 方法进行排序。
     * 如果指定数组的长度小于最小粒度，则使用适当的 {@link Arrays#sort(short[]) Arrays.sort} 方法进行排序。
     * 该算法所需的临时空间不超过原始数组的大小。使用 {@link ForkJoinPool#commonPool() ForkJoin 公共池}
     * 执行任何并行任务。
     *
     * @param a 要排序的数组
     *
     * @since 1.8
     */
    public static void parallelSort(short[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJShort.Sorter
                (null, a, new short[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * 按升序对指定范围的数组进行数值排序。要排序的范围从索引 {@code fromIndex}（包含）到
     * 索引 {@code toIndex}（不包含）。如果 {@code fromIndex == toIndex}，
     * 则要排序的范围为空。
     *
     * @implNote 排序算法是一种并行排序-合并算法，将数组分解为子数组，这些子数组本身被排序然后合并。
     * 当子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(short[]) Arrays.sort} 方法进行排序。
     * 如果指定数组的长度小于最小粒度，则使用适当的 {@link Arrays#sort(short[]) Arrays.sort} 方法进行排序。
     * 该算法所需的临时空间不超过指定范围的原始数组的大小。使用 {@link ForkJoinPool#commonPool() ForkJoin 公共池}
     * 执行任何并行任务。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包含）
     * @param toIndex 要排序的最后一个元素的索引（不包含）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(short[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJShort.Sorter
                (null, a, new short[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * 按升序对指定数组进行数值排序。
     *
     * @implNote 排序算法是一种并行排序-合并算法，将数组分解为子数组，这些子数组本身被排序然后合并。
     * 当子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(int[]) Arrays.sort} 方法进行排序。
     * 如果指定数组的长度小于最小粒度，则使用适当的 {@link Arrays#sort(int[]) Arrays.sort} 方法进行排序。
     * 该算法所需的临时空间不超过原始数组的大小。使用 {@link ForkJoinPool#commonPool() ForkJoin 公共池}
     * 执行任何并行任务。
     *
     * @param a 要排序的数组
     *
     * @since 1.8
     */
    public static void parallelSort(int[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJInt.Sorter
                (null, a, new int[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }


    /**
     * 将指定范围的数组按升序数值排序。
     * 要排序的范围从索引 {@code fromIndex} 开始（包括），到索引 {@code toIndex} 结束（不包括）。如果
     * {@code fromIndex == toIndex}，则要排序的范围为空。
     *
     * @implNote 排序算法是一种并行排序合并算法，它将数组分解为子数组，这些子数组本身被排序然后合并。当
     * 子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(int[]) Arrays.sort}
     * 方法进行排序。如果指定数组的长度小于最小粒度，则使用适当的 {@link
     * Arrays#sort(int[]) Arrays.sort} 方法进行排序。该算法所需的临时空间不超过指定范围的原始数组的大小。使用
     * {@link ForkJoinPool#commonPool() ForkJoin 公共池} 执行任何并行任务。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包括）
     * @param toIndex 要排序的最后一个元素的索引（不包括）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(int[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJInt.Sorter
                (null, a, new int[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * 将指定数组按升序数值排序。
     *
     * @implNote 排序算法是一种并行排序合并算法，它将数组分解为子数组，这些子数组本身被排序然后合并。当
     * 子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(long[]) Arrays.sort}
     * 方法进行排序。如果指定数组的长度小于最小粒度，则使用适当的 {@link
     * Arrays#sort(long[]) Arrays.sort} 方法进行排序。该算法所需的临时空间不超过原始数组的大小。使用
     * {@link ForkJoinPool#commonPool() ForkJoin 公共池} 执行任何并行任务。
     *
     * @param a 要排序的数组
     *
     * @since 1.8
     */
    public static void parallelSort(long[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJLong.Sorter
                (null, a, new long[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * 将指定范围的数组按升序数值排序。
     * 要排序的范围从索引 {@code fromIndex} 开始（包括），到索引 {@code toIndex} 结束（不包括）。如果
     * {@code fromIndex == toIndex}，则要排序的范围为空。
     *
     * @implNote 排序算法是一种并行排序合并算法，它将数组分解为子数组，这些子数组本身被排序然后合并。当
     * 子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(long[]) Arrays.sort}
     * 方法进行排序。如果指定数组的长度小于最小粒度，则使用适当的 {@link
     * Arrays#sort(long[]) Arrays.sort} 方法进行排序。该算法所需的临时空间不超过指定范围的原始数组的大小。使用
     * {@link ForkJoinPool#commonPool() ForkJoin 公共池} 执行任何并行任务。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包括）
     * @param toIndex 要排序的最后一个元素的索引（不包括）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(long[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJLong.Sorter
                (null, a, new long[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * 将指定数组按升序数值排序。
     *
     * <p>{@code <} 关系不提供所有浮点值的全序：{@code -0.0f == 0.0f} 为 {@code true}，并且
     * {@code Float.NaN} 值既不小于、也不大于、也不等于任何值，甚至不等于自身。此方法使用由
     * {@link Float#compareTo} 方法强加的全序：{@code -0.0f} 被视为小于 {@code 0.0f}，而
     * {@code Float.NaN} 被视为大于任何其他值，所有 {@code Float.NaN} 值都被视为相等。
     *
     * @implNote 排序算法是一种并行排序合并算法，它将数组分解为子数组，这些子数组本身被排序然后合并。当
     * 子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(float[]) Arrays.sort}
     * 方法进行排序。如果指定数组的长度小于最小粒度，则使用适当的 {@link
     * Arrays#sort(float[]) Arrays.sort} 方法进行排序。该算法所需的临时空间不超过原始数组的大小。使用
     * {@link ForkJoinPool#commonPool() ForkJoin 公共池} 执行任何并行任务。
     *
     * @param a 要排序的数组
     *
     * @since 1.8
     */
    public static void parallelSort(float[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJFloat.Sorter
                (null, a, new float[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * 将指定范围的数组按升序数值排序。
     * 要排序的范围从索引 {@code fromIndex} 开始（包括），到索引 {@code toIndex} 结束（不包括）。如果
     * {@code fromIndex == toIndex}，则要排序的范围为空。
     *
     * <p>{@code <} 关系不提供所有浮点值的全序：{@code -0.0f == 0.0f} 为 {@code true}，并且
     * {@code Float.NaN} 值既不小于、也不大于、也不等于任何值，甚至不等于自身。此方法使用由
     * {@link Float#compareTo} 方法强加的全序：{@code -0.0f} 被视为小于 {@code 0.0f}，而
     * {@code Float.NaN} 被视为大于任何其他值，所有 {@code Float.NaN} 值都被视为相等。
     *
     * @implNote 排序算法是一种并行排序合并算法，它将数组分解为子数组，这些子数组本身被排序然后合并。当
     * 子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(float[]) Arrays.sort}
     * 方法进行排序。如果指定数组的长度小于最小粒度，则使用适当的 {@link
     * Arrays#sort(float[]) Arrays.sort} 方法进行排序。该算法所需的临时空间不超过指定范围的原始数组的大小。使用
     * {@link ForkJoinPool#commonPool() ForkJoin 公共池} 执行任何并行任务。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包括）
     * @param toIndex 要排序的最后一个元素的索引（不包括）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(float[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJFloat.Sorter
                (null, a, new float[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * 将指定数组按升序数值排序。
     *
     * <p>{@code <} 关系不提供所有双精度值的全序：{@code -0.0d == 0.0d} 为 {@code true}，并且
     * {@code Double.NaN} 值既不小于、也不大于、也不等于任何值，甚至不等于自身。此方法使用由
     * {@link Double#compareTo} 方法强加的全序：{@code -0.0d} 被视为小于 {@code 0.0d}，而
     * {@code Double.NaN} 被视为大于任何其他值，所有 {@code Double.NaN} 值都被视为相等。
     *
     * @implNote 排序算法是一种并行排序合并算法，它将数组分解为子数组，这些子数组本身被排序然后合并。当
     * 子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(double[]) Arrays.sort}
     * 方法进行排序。如果指定数组的长度小于最小粒度，则使用适当的 {@link
     * Arrays#sort(double[]) Arrays.sort} 方法进行排序。该算法所需的临时空间不超过原始数组的大小。使用
     * {@link ForkJoinPool#commonPool() ForkJoin 公共池} 执行任何并行任务。
     *
     * @param a 要排序的数组
     *
     * @since 1.8
     */
    public static void parallelSort(double[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, 0, n - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJDouble.Sorter
                (null, a, new double[n], 0, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * 将指定范围的数组按升序数值排序。
     * 要排序的范围从索引 {@code fromIndex} 开始（包括），到索引 {@code toIndex} 结束（不包括）。如果
     * {@code fromIndex == toIndex}，则要排序的范围为空。
     *
     * <p>{@code <} 关系不提供所有双精度值的全序：{@code -0.0d == 0.0d} 为 {@code true}，并且
     * {@code Double.NaN} 值既不小于、也不大于、也不等于任何值，甚至不等于自身。此方法使用由
     * {@link Double#compareTo} 方法强加的全序：{@code -0.0d} 被视为小于 {@code 0.0d}，而
     * {@code Double.NaN} 被视为大于任何其他值，所有 {@code Double.NaN} 值都被视为相等。
     *
     * @implNote 排序算法是一种并行排序合并算法，它将数组分解为子数组，这些子数组本身被排序然后合并。当
     * 子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(double[]) Arrays.sort}
     * 方法进行排序。如果指定数组的长度小于最小粒度，则使用适当的 {@link
     * Arrays#sort(double[]) Arrays.sort} 方法进行排序。该算法所需的临时空间不超过指定范围的原始数组的大小。使用
     * {@link ForkJoinPool#commonPool() ForkJoin 公共池} 执行任何并行任务。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包括）
     * @param toIndex 要排序的最后一个元素的索引（不包括）
     *
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > a.length}
     *
     * @since 1.8
     */
    public static void parallelSort(double[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            DualPivotQuicksort.sort(a, fromIndex, toIndex - 1, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJDouble.Sorter
                (null, a, new double[n], fromIndex, n, 0,
                 ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g).invoke();
    }

    /**
     * 将指定对象数组按其元素的 {@linkplain Comparable 自然顺序} 升序排序。
     * 数组中的所有元素都必须实现 {@link Comparable} 接口。此外，数组中的所有元素都必须
     * <i>相互可比较</i>（即，对于数组中的任何元素 {@code e1} 和 {@code e2}，{@code e1.compareTo(e2)}
     * 不应抛出 {@code ClassCastException}）。
     *
     * <p>此排序保证是 <i>稳定的</i>：相等的元素不会因为排序而重新排序。
     *
     * @implNote 排序算法是一种并行排序合并算法，它将数组分解为子数组，这些子数组本身被排序然后合并。当
     * 子数组长度达到最小粒度时，子数组使用适当的 {@link Arrays#sort(Object[]) Arrays.sort}
     * 方法进行排序。如果指定数组的长度小于最小粒度，则使用适当的 {@link
     * Arrays#sort(Object[]) Arrays.sort} 方法进行排序。该算法所需的临时空间不超过原始数组的大小。使用
     * {@link ForkJoinPool#commonPool() ForkJoin 公共池} 执行任何并行任务。
     *
     * @param <T> 要排序的对象的类
     * @param a 要排序的数组
     *
     * @throws ClassCastException 如果数组包含不 <i>相互可比较</i> 的元素（例如，字符串和整数）
     * @throws IllegalArgumentException （可选）如果发现数组元素的自然顺序违反了
     *         {@link Comparable} 合约
     *
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> void parallelSort(T[] a) {
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            TimSort.sort(a, 0, n, NaturalOrder.INSTANCE, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJObject.Sorter<T>
                (null, a,
                 (T[])Array.newInstance(a.getClass().getComponentType(), n),
                 0, n, 0, ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g, NaturalOrder.INSTANCE).invoke();
    }


                /**
     * 按照指定对象数组的指定范围的元素的 {@linkplain Comparable 自然顺序} 对其进行升序排序。
     * 要排序的范围从索引 {@code fromIndex}（包括）到索引 {@code toIndex}（不包括）。
     * （如果 {@code fromIndex==toIndex}，则要排序的范围为空。）此范围中的所有元素都必须实现 {@link Comparable}
     * 接口。此外，此范围中的所有元素必须是 <i>相互可比较的</i>（即，对于数组中的任何元素 {@code e1} 和
     * {@code e2}，{@code e1.compareTo(e2)} 不应抛出 {@code ClassCastException}）。
     *
     * <p>此排序保证是 <i>稳定的</i>：相等的元素不会因为排序而重新排序。
     *
     * @implNote 排序算法是一种并行排序-合并算法，将数组分解为子数组，这些子数组本身被排序并合并。当子数组长度达到最小粒度时，
     * 子数组使用适当的 {@link Arrays#sort(Object[]) Arrays.sort} 方法进行排序。如果指定数组的长度小于最小粒度，
     * 则使用适当的 {@link Arrays#sort(Object[]) Arrays.sort} 方法进行排序。该算法所需的工作空间不超过指定范围的原始数组的大小。
     * {@link ForkJoinPool#commonPool() ForkJoin 公共池} 用于执行任何并行任务。
     *
     * @param <T> 要排序的对象的类
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包括）
     * @param toIndex 要排序的最后一个元素的索引（不包括）
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex} 或
     *         （可选）如果数组元素的自然顺序违反了 {@link Comparable} 合约
     * @throws ArrayIndexOutOfBoundsException 如果 {@code fromIndex < 0} 或
     *         {@code toIndex > a.length}
     * @throws ClassCastException 如果数组包含的元素不是 <i>相互可比较的</i>（例如，字符串和整数）。
     *
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>>
    void parallelSort(T[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            TimSort.sort(a, fromIndex, toIndex, NaturalOrder.INSTANCE, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJObject.Sorter<T>
                (null, a,
                 (T[])Array.newInstance(a.getClass().getComponentType(), n),
                 fromIndex, n, 0, ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g, NaturalOrder.INSTANCE).invoke();
    }

    /**
     * 按照指定比较器诱导的顺序对指定对象数组进行排序。数组中的所有元素都必须是
     * <i>相互可比较的</i>（即，对于数组中的任何元素 {@code e1} 和 {@code e2}，
     * {@code c.compare(e1, e2)} 不应抛出 {@code ClassCastException}）。
     *
     * <p>此排序保证是 <i>稳定的</i>：相等的元素不会因为排序而重新排序。
     *
     * @implNote 排序算法是一种并行排序-合并算法，将数组分解为子数组，这些子数组本身被排序并合并。当子数组长度达到最小粒度时，
     * 子数组使用适当的 {@link Arrays#sort(Object[]) Arrays.sort} 方法进行排序。如果指定数组的长度小于最小粒度，
     * 则使用适当的 {@link Arrays#sort(Object[]) Arrays.sort} 方法进行排序。该算法所需的工作空间不超过原始数组的大小。
     * {@link ForkJoinPool#commonPool() ForkJoin 公共池} 用于执行任何并行任务。
     *
     * @param <T> 要排序的对象的类
     * @param a 要排序的数组
     * @param cmp 确定数组顺序的比较器。{@code null} 值表示应使用元素的
     *        {@linkplain Comparable 自然顺序}。
     * @throws ClassCastException 如果数组包含的元素不是 <i>相互可比较的</i> 使用指定的比较器
     * @throws IllegalArgumentException （可选）如果比较器违反了 {@link java.util.Comparator} 合约
     *
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <T> void parallelSort(T[] a, Comparator<? super T> cmp) {
        if (cmp == null)
            cmp = NaturalOrder.INSTANCE;
        int n = a.length, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            TimSort.sort(a, 0, n, cmp, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJObject.Sorter<T>
                (null, a,
                 (T[])Array.newInstance(a.getClass().getComponentType(), n),
                 0, n, 0, ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g, cmp).invoke();
    }

    /**
     * 按照指定比较器诱导的顺序对指定对象数组的指定范围进行排序。要排序的范围从索引
     * {@code fromIndex}（包括）到索引 {@code toIndex}（不包括）。如果 {@code fromIndex==toIndex}，
     * 则要排序的范围为空。此范围中的所有元素都必须是 <i>相互可比较的</i>（即，对于此范围中的任何元素 {@code e1} 和
     * {@code e2}，{@code c.compare(e1, e2)} 不应抛出 {@code ClassCastException}）。
     *
     * <p>此排序保证是 <i>稳定的</i>：相等的元素不会因为排序而重新排序。
     *
     * @implNote 排序算法是一种并行排序-合并算法，将数组分解为子数组，这些子数组本身被排序并合并。当子数组长度达到最小粒度时，
     * 子数组使用适当的 {@link Arrays#sort(Object[]) Arrays.sort} 方法进行排序。如果指定数组的长度小于最小粒度，
     * 则使用适当的 {@link Arrays#sort(Object[]) Arrays.sort} 方法进行排序。该算法所需的工作空间不超过指定范围的原始数组的大小。
     * {@link ForkJoinPool#commonPool() ForkJoin 公共池} 用于执行任何并行任务。
     *
     * @param <T> 要排序的对象的类
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包括）
     * @param toIndex 要排序的最后一个元素的索引（不包括）
     * @param cmp 确定数组顺序的比较器。{@code null} 值表示应使用元素的
     *        {@linkplain Comparable 自然顺序}。
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex} 或
     *         （可选）如果数组元素的自然顺序违反了 {@link Comparable} 合约
     * @throws ArrayIndexOutOfBoundsException 如果 {@code fromIndex < 0} 或
     *         {@code toIndex > a.length}
     * @throws ClassCastException 如果数组包含的元素不是 <i>相互可比较的</i>（例如，字符串和整数）。
     *
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <T> void parallelSort(T[] a, int fromIndex, int toIndex,
                                        Comparator<? super T> cmp) {
        rangeCheck(a.length, fromIndex, toIndex);
        if (cmp == null)
            cmp = NaturalOrder.INSTANCE;
        int n = toIndex - fromIndex, p, g;
        if (n <= MIN_ARRAY_SORT_GRAN ||
            (p = ForkJoinPool.getCommonPoolParallelism()) == 1)
            TimSort.sort(a, fromIndex, toIndex, cmp, null, 0, 0);
        else
            new ArraysParallelSortHelpers.FJObject.Sorter<T>
                (null, a,
                 (T[])Array.newInstance(a.getClass().getComponentType(), n),
                 fromIndex, n, 0, ((g = n / (p << 2)) <= MIN_ARRAY_SORT_GRAN) ?
                 MIN_ARRAY_SORT_GRAN : g, cmp).invoke();
    }

    /*
     * 复杂类型数组的排序。
     */

    /**
     * 可以使用系统属性选择旧的合并排序实现（为了与损坏的比较器兼容）。
     * 由于循环依赖关系，不能在封闭类中使用静态布尔值。将在未来的版本中移除。
     */
    static final class LegacyMergeSort {
        private static final boolean userRequested =
            java.security.AccessController.doPrivileged(
                new sun.security.action.GetBooleanAction(
                    "java.util.Arrays.useLegacyMergeSort")).booleanValue();
    }

    /**
     * 按照指定对象数组的元素的 {@linkplain Comparable 自然顺序} 对其进行升序排序。
     * 数组中的所有元素都必须实现 {@link Comparable} 接口。此外，数组中的所有元素必须是
     * <i>相互可比较的</i>（即，对于数组中的任何元素 {@code e1} 和 {@code e2}，
     * {@code e1.compareTo(e2)} 不应抛出 {@code ClassCastException}）。
     *
     * <p>此排序保证是 <i>稳定的</i>：相等的元素不会因为排序而重新排序。
     *
     * <p>实现说明：此实现是一种稳定的、自适应的、迭代的归并排序，当输入数组部分排序时，所需的比较次数远少于 n lg(n)，
     * 而当输入数组随机排序时，提供传统归并排序的性能。如果输入数组接近排序，实现所需的比较次数约为 n。
     * 临时存储需求从接近排序的输入数组的小常数到随机排序的输入数组的 n/2 个对象引用不等。
     *
     * <p>该实现充分利用输入数组中的升序和降序，并且可以在同一输入数组的不同部分利用升序和降序。
     * 它非常适合合并两个或多个已排序的数组：只需将数组连接起来并排序结果数组。
     *
     * <p>该实现改编自 Tim Peters 的 Python 列表排序（<a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">
     * TimSort</a>）。它使用了 Peter McIlroy 的 "Optimistic Sorting and Information Theoretic Complexity"，
     * 在 Proceedings of the Fourth Annual ACM-SIAM Symposium on Discrete Algorithms, pp 467-474, January 1993 中的技术。
     *
     * @param a 要排序的数组
     * @throws ClassCastException 如果数组包含的元素不是 <i>相互可比较的</i>（例如，字符串和整数）
     * @throws IllegalArgumentException （可选）如果数组元素的自然顺序违反了 {@link Comparable} 合约
     */
    public static void sort(Object[] a) {
        if (LegacyMergeSort.userRequested)
            legacyMergeSort(a);
        else
            ComparableTimSort.sort(a, 0, a.length, null, 0, 0);
    }

    /** 将在未来的版本中移除。 */
    private static void legacyMergeSort(Object[] a) {
        Object[] aux = a.clone();
        mergeSort(aux, a, 0, a.length, 0);
    }

    /**
     * 按照指定对象数组的元素的 {@linkplain Comparable 自然顺序} 对其指定范围进行升序排序。
     * 要排序的范围从索引 {@code fromIndex}（包括）到索引 {@code toIndex}（不包括）。
     * （如果 {@code fromIndex==toIndex}，则要排序的范围为空。）此范围中的所有元素都必须实现 {@link Comparable}
     * 接口。此外，此范围中的所有元素必须是 <i>相互可比较的</i>（即，对于数组中的任何元素 {@code e1} 和
     * {@code e2}，{@code e1.compareTo(e2)} 不应抛出 {@code ClassCastException}）。
     *
     * <p>此排序保证是 <i>稳定的</i>：相等的元素不会因为排序而重新排序。
     *
     * <p>实现说明：此实现是一种稳定的、自适应的、迭代的归并排序，当输入数组部分排序时，所需的比较次数远少于 n lg(n)，
     * 而当输入数组随机排序时，提供传统归并排序的性能。如果输入数组接近排序，实现所需的比较次数约为 n。
     * 临时存储需求从接近排序的输入数组的小常数到随机排序的输入数组的 n/2 个对象引用不等。
     *
     * <p>该实现充分利用输入数组中的升序和降序，并且可以在同一输入数组的不同部分利用升序和降序。
     * 它非常适合合并两个或多个已排序的数组：只需将数组连接起来并排序结果数组。
     *
     * <p>该实现改编自 Tim Peters 的 Python 列表排序（<a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">
     * TimSort</a>）。它使用了 Peter McIlroy 的 "Optimistic Sorting and Information Theoretic Complexity"，
     * 在 Proceedings of the Fourth Annual ACM-SIAM Symposium on Discrete Algorithms, pp 467-474, January 1993 中的技术。
     *
     * @param a 要排序的数组
     * @param fromIndex 要排序的第一个元素的索引（包括）
     * @param toIndex 要排序的最后一个元素的索引（不包括）
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex} 或
     *         （可选）如果数组元素的自然顺序违反了 {@link Comparable} 合约
     * @throws ArrayIndexOutOfBoundsException 如果 {@code fromIndex < 0} 或
     *         {@code toIndex > a.length}
     * @throws ClassCastException 如果数组包含的元素不是 <i>相互可比较的</i>（例如，字符串和整数）。
     */
    public static void sort(Object[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        if (LegacyMergeSort.userRequested)
            legacyMergeSort(a, fromIndex, toIndex);
        else
            ComparableTimSort.sort(a, fromIndex, toIndex, null, 0, 0);
    }


                /** To be removed in a future release. */
    private static void legacyMergeSort(Object[] a,
                                        int fromIndex, int toIndex) {
        Object[] aux = copyOfRange(a, fromIndex, toIndex);
        mergeSort(aux, a, fromIndex, toIndex, -fromIndex);
    }

    /**
     * 调整参数：列表大小等于或小于该值时，将使用插入排序而不是归并排序。
     * 将在未来版本中移除。
     */
    private static final int INSERTIONSORT_THRESHOLD = 7;

    /**
     * Src 是从索引 0 开始的源数组
     * Dest 是（可能更大）的目标数组，可能有偏移
     * low 是目标数组中开始排序的索引
     * high 是目标数组中结束排序的索引
     * off 是生成 src 中对应的 low 和 high 的偏移
     * 将在未来版本中移除。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void mergeSort(Object[] src,
                                  Object[] dest,
                                  int low,
                                  int high,
                                  int off) {
        int length = high - low;

        // 对最小的数组进行插入排序
        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i = low; i < high; i++)
                for (int j = i; j > low &&
                         ((Comparable) dest[j - 1]).compareTo(dest[j]) > 0; j--)
                    swap(dest, j, j - 1);
            return;
        }

        // 递归地将 dest 的一半排序到 src
        int destLow = low;
        int destHigh = high;
        low += off;
        high += off;
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off);
        mergeSort(dest, src, mid, high, -off);

        // 如果列表已经排序，只需从 src 复制到 dest。这是对几乎有序列表的优化。
        if (((Comparable) src[mid - 1]).compareTo(src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        // 合并已排序的两半（现在在 src 中）到 dest
        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && ((Comparable) src[p]).compareTo(src[q]) <= 0)
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }

    /**
     * 交换 x[a] 和 x[b]。
     */
    private static void swap(Object[] x, int a, int b) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    /**
     * 按照指定比较器确定的顺序对指定对象数组进行排序。数组中的所有元素必须是
     * <i>相互可比较的</i>（即，{@code c.compare(e1, e2)} 不应为任何元素
     * {@code e1} 和 {@code e2} 抛出 {@code ClassCastException}）。
     *
     * <p>此排序保证是 <i>稳定的</i>：相等的元素不会因排序而重新排序。
     *
     * <p>实现说明：此实现是一个稳定的、自适应的、迭代的归并排序，当输入数组部分排序时，所需的比较次数远少于 n lg(n)，而当输入数组随机排序时，性能与传统的归并排序相当。如果输入数组几乎排序，实现所需的比较次数约为 n。临时存储需求从几乎排序的输入数组的小常数到随机排序的输入数组的 n/2 个对象引用不等。
     *
     * <p>实现充分利用了输入数组中的升序和降序，并且可以在同一输入数组的不同部分中利用升序和降序。它非常适合合并两个或多个已排序的数组：只需连接数组并排序结果数组。
     *
     * <p>实现改编自 Tim Peters 的 Python 列表排序（<a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">
     * TimSort</a>）。它使用了 Peter McIlroy 的“Optimistic Sorting and Information Theoretic Complexity”中的一些技术，该论文发表在第四届年度 ACM-SIAM 离散算法研讨会论文集，第 467-474 页，1993 年 1 月。
     *
     * @param <T> 要排序的对象的类
     * @param a 要排序的数组
     * @param c 确定数组顺序的比较器。{@code null} 值表示应使用元素的
     *        {@linkplain Comparable 自然顺序}。
     * @throws ClassCastException 如果数组包含使用指定比较器不可<i>相互比较的</i>元素
     * @throws IllegalArgumentException （可选）如果发现比较器违反了 {@link Comparator} 合约
     */
    public static <T> void sort(T[] a, Comparator<? super T> c) {
        if (c == null) {
            sort(a);
        } else {
            if (LegacyMergeSort.userRequested)
                legacyMergeSort(a, c);
            else
                TimSort.sort(a, 0, a.length, c, null, 0, 0);
        }
    }

    /** To be removed in a future release. */
    private static <T> void legacyMergeSort(T[] a, Comparator<? super T> c) {
        T[] aux = a.clone();
        if (c == null)
            mergeSort(aux, a, 0, a.length, 0);
        else
            mergeSort(aux, a, 0, a.length, 0, c);
    }

    /**
     * 按照指定比较器确定的顺序对指定对象数组的指定范围进行排序。要排序的范围从索引
     * {@code fromIndex}（包含）到索引 {@code toIndex}（不包含）。如果
     * {@code fromIndex==toIndex}，则要排序的范围为空。范围中的所有元素必须是
     * <i>相互可比较的</i>（即，{@code c.compare(e1, e2)} 不应为任何元素
     * {@code e1} 和 {@code e2} 抛出 {@code ClassCastException}）。
     *
     * <p>此排序保证是 <i>稳定的</i>：相等的元素不会因排序而重新排序。
     *
     * <p>实现说明：此实现是一个稳定的、自适应的、迭代的归并排序，当输入数组部分排序时，所需的比较次数远少于 n lg(n)，而当输入数组随机排序时，性能与传统的归并排序相当。如果输入数组几乎排序，实现所需的比较次数约为 n。临时存储需求从几乎排序的输入数组的小常数到随机排序的输入数组的 n/2 个对象引用不等。
     *
     * <p>实现充分利用了输入数组中的升序和降序，并且可以在同一输入数组的不同部分中利用升序和降序。它非常适合合并两个或多个已排序的数组：只需连接数组并排序结果数组。
     *
     * <p>实现改编自 Tim Peters 的 Python 列表排序（<a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">
     * TimSort</a>）。它使用了 Peter McIlroy 的“Optimistic Sorting and Information Theoretic Complexity”中的一些技术，该论文发表在第四届年度 ACM-SIAM 离散算法研讨会论文集，第 467-474 页，1993 年 1 月。
     *
     * @param <T> 要排序的对象的类
     * @param a 要排序的数组
     * @param fromIndex 第一个元素的索引（包含）要排序
     * @param toIndex 最后一个元素的索引（不包含）要排序
     * @param c 确定数组顺序的比较器。{@code null} 值表示应使用元素的
     *        {@linkplain Comparable 自然顺序}。
     * @throws ClassCastException 如果数组包含不可<i>相互比较的</i>元素。
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex} 或
     *         （可选）如果发现比较器违反了 {@link Comparator} 合约
     * @throws ArrayIndexOutOfBoundsException 如果 {@code fromIndex < 0} 或
     *         {@code toIndex > a.length}
     */
    public static <T> void sort(T[] a, int fromIndex, int toIndex,
                                Comparator<? super T> c) {
        if (c == null) {
            sort(a, fromIndex, toIndex);
        } else {
            rangeCheck(a.length, fromIndex, toIndex);
            if (LegacyMergeSort.userRequested)
                legacyMergeSort(a, fromIndex, toIndex, c);
            else
                TimSort.sort(a, fromIndex, toIndex, c, null, 0, 0);
        }
    }

    /** To be removed in a future release. */
    private static <T> void legacyMergeSort(T[] a, int fromIndex, int toIndex,
                                            Comparator<? super T> c) {
        T[] aux = copyOfRange(a, fromIndex, toIndex);
        if (c == null)
            mergeSort(aux, a, fromIndex, toIndex, -fromIndex);
        else
            mergeSort(aux, a, fromIndex, toIndex, -fromIndex, c);
    }

    /**
     * Src 是从索引 0 开始的源数组
     * Dest 是（可能更大）的目标数组，可能有偏移
     * low 是目标数组中开始排序的索引
     * high 是目标数组中结束排序的索引
     * off 是生成 src 中对应的 low 和 high 的偏移
     * 将在未来版本中移除。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void mergeSort(Object[] src,
                                  Object[] dest,
                                  int low, int high, int off,
                                  Comparator c) {
        int length = high - low;

        // 对最小的数组进行插入排序
        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i = low; i < high; i++)
                for (int j = i; j > low && c.compare(dest[j - 1], dest[j]) > 0; j--)
                    swap(dest, j, j - 1);
            return;
        }

        // 递归地将 dest 的一半排序到 src
        int destLow = low;
        int destHigh = high;
        low += off;
        high += off;
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off, c);
        mergeSort(dest, src, mid, high, -off, c);

        // 如果列表已经排序，只需从 src 复制到 dest。这是对几乎有序列表的优化。
        if (c.compare(src[mid - 1], src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        // 合并已排序的两半（现在在 src 中）到 dest
        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0)
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }

    // 并行前缀

    /**
     * 并行地在给定数组中就地累积每个元素，使用提供的函数。例如，如果数组最初
     * 包含 {@code [2, 1, 0, 3]} 并且操作执行加法，则返回时数组包含 {@code [2, 3, 3, 6]}。
     * 并行前缀计算通常比对大数组的顺序循环更高效。
     *
     * @param <T> 数组中对象的类
     * @param array 由该方法就地修改的数组
     * @param op 执行累积的无副作用、结合函数
     * @throws NullPointerException 如果指定的数组或函数为 null
     * @since 1.8
     */
    public static <T> void parallelPrefix(T[] array, BinaryOperator<T> op) {
        Objects.requireNonNull(op);
        if (array.length > 0)
            new ArrayPrefixHelpers.CumulateTask<>
                    (null, op, array, 0, array.length).invoke();
    }

    /**
     * 对数组的给定子范围执行 {@link #parallelPrefix(Object[], BinaryOperator)}。
     *
     * @param <T> 数组中对象的类
     * @param array 数组
     * @param fromIndex 第一个元素的索引（包含）
     * @param toIndex 最后一个元素的索引（不包含）
     * @param op 执行累积的无副作用、结合函数
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > array.length}
     * @throws NullPointerException 如果指定的数组或函数为 null
     * @since 1.8
     */
    public static <T> void parallelPrefix(T[] array, int fromIndex,
                                          int toIndex, BinaryOperator<T> op) {
        Objects.requireNonNull(op);
        rangeCheck(array.length, fromIndex, toIndex);
        if (fromIndex < toIndex)
            new ArrayPrefixHelpers.CumulateTask<>
                    (null, op, array, fromIndex, toIndex).invoke();
    }

    /**
     * 并行地在给定数组中就地累积每个元素，使用提供的函数。例如，如果数组最初
     * 包含 {@code [2, 1, 0, 3]} 并且操作执行加法，则返回时数组包含 {@code [2, 3, 3, 6]}。
     * 并行前缀计算通常比对大数组的顺序循环更高效。
     *
     * @param array 由该方法就地修改的数组
     * @param op 执行累积的无副作用、结合函数
     * @throws NullPointerException 如果指定的数组或函数为 null
     * @since 1.8
     */
    public static void parallelPrefix(long[] array, LongBinaryOperator op) {
        Objects.requireNonNull(op);
        if (array.length > 0)
            new ArrayPrefixHelpers.LongCumulateTask
                    (null, op, array, 0, array.length).invoke();
    }

    /**
     * 对数组的给定子范围执行 {@link #parallelPrefix(long[], LongBinaryOperator)}。
     *
     * @param array 数组
     * @param fromIndex 第一个元素的索引（包含）
     * @param toIndex 最后一个元素的索引（不包含）
     * @param op 执行累积的无副作用、结合函数
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > array.length}
     * @throws NullPointerException 如果指定的数组或函数为 null
     * @since 1.8
     */
    public static void parallelPrefix(long[] array, int fromIndex,
                                      int toIndex, LongBinaryOperator op) {
        Objects.requireNonNull(op);
        rangeCheck(array.length, fromIndex, toIndex);
        if (fromIndex < toIndex)
            new ArrayPrefixHelpers.LongCumulateTask
                    (null, op, array, fromIndex, toIndex).invoke();
    }


                /**
     * 并行地使用提供的函数在给定数组中就地累积每个元素。例如，如果数组最初
     * 包含 {@code [2.0, 1.0, 0.0, 3.0]} 并且操作执行加法，
     * 那么返回时数组将包含 {@code [2.0, 3.0, 3.0, 6.0]}。
     * 对于大型数组，平行前缀计算通常比顺序循环更有效。
     *
     * <p> 由于浮点运算可能不是严格关联的，
     * 返回的结果可能与顺序执行操作时获得的值不完全相同。
     *
     * @param array 该数组，此方法会就地修改该数组
     * @param op 用于累积的无副作用函数
     * @throws NullPointerException 如果指定的数组或函数为 null
     * @since 1.8
     */
    public static void parallelPrefix(double[] array, DoubleBinaryOperator op) {
        Objects.requireNonNull(op);
        if (array.length > 0)
            new ArrayPrefixHelpers.DoubleCumulateTask
                    (null, op, array, 0, array.length).invoke();
    }

    /**
     * 对数组的给定子范围执行 {@link #parallelPrefix(double[], DoubleBinaryOperator)}。
     *
     * @param array 数组
     * @param fromIndex 第一个元素的索引，包含
     * @param toIndex 最后一个元素的索引，不包含
     * @param op 用于累积的无副作用、关联函数
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > array.length}
     * @throws NullPointerException 如果指定的数组或函数为 null
     * @since 1.8
     */
    public static void parallelPrefix(double[] array, int fromIndex,
                                      int toIndex, DoubleBinaryOperator op) {
        Objects.requireNonNull(op);
        rangeCheck(array.length, fromIndex, toIndex);
        if (fromIndex < toIndex)
            new ArrayPrefixHelpers.DoubleCumulateTask
                    (null, op, array, fromIndex, toIndex).invoke();
    }

    /**
     * 并行地使用提供的函数在给定数组中就地累积每个元素。例如，如果数组最初
     * 包含 {@code [2, 1, 0, 3]} 并且操作执行加法，
     * 那么返回时数组将包含 {@code [2, 3, 3, 6]}。
     * 对于大型数组，平行前缀计算通常比顺序循环更有效。
     *
     * @param array 该数组，此方法会就地修改该数组
     * @param op 用于累积的无副作用、关联函数
     * @throws NullPointerException 如果指定的数组或函数为 null
     * @since 1.8
     */
    public static void parallelPrefix(int[] array, IntBinaryOperator op) {
        Objects.requireNonNull(op);
        if (array.length > 0)
            new ArrayPrefixHelpers.IntCumulateTask
                    (null, op, array, 0, array.length).invoke();
    }

    /**
     * 对数组的给定子范围执行 {@link #parallelPrefix(int[], IntBinaryOperator)}。
     *
     * @param array 数组
     * @param fromIndex 第一个元素的索引，包含
     * @param toIndex 最后一个元素的索引，不包含
     * @param op 用于累积的无副作用、关联函数
     * @throws IllegalArgumentException 如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *     如果 {@code fromIndex < 0} 或 {@code toIndex > array.length}
     * @throws NullPointerException 如果指定的数组或函数为 null
     * @since 1.8
     */
    public static void parallelPrefix(int[] array, int fromIndex,
                                      int toIndex, IntBinaryOperator op) {
        Objects.requireNonNull(op);
        rangeCheck(array.length, fromIndex, toIndex);
        if (fromIndex < toIndex)
            new ArrayPrefixHelpers.IntCumulateTask
                    (null, op, array, fromIndex, toIndex).invoke();
    }

    // 搜索

    /**
     * 使用二分搜索算法在指定的 long 型数组中搜索指定的值。数组必须先排序
     * （如通过 {@link #sort(long[])} 方法）才能调用此方法。如果数组未排序，
     * 结果是未定义的。如果数组包含多个指定值的元素，则不保证找到哪一个。
     *
     * @param a 要搜索的数组
     * @param key 要搜索的值
     * @return 如果数组包含搜索键，则返回搜索键的索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：第一个大于键的元素的索引，
     *         或者如果数组中所有元素都小于指定键，则为 <tt>a.length</tt>。
     *         这保证了只有在找到键时返回值才会 &gt;= 0。
     */
    public static int binarySearch(long[] a, long key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * 使用二分搜索算法在指定的 long 型数组的范围内搜索指定的值。
     * 范围必须先排序
     * （如通过 {@link #sort(long[], int, int)} 方法）才能调用此方法。如果范围未排序，
     * 结果是未定义的。如果范围包含多个指定值的元素，则不保证找到哪一个。
     *
     * @param a 要搜索的数组
     * @param fromIndex 第一个元素的索引（包含）要搜索
     * @param toIndex 最后一个元素的索引（不包含）要搜索
     * @param key 要搜索的值
     * @return 如果数组在指定范围内包含搜索键，则返回搜索键的索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：范围内第一个大于键的元素的索引，
     *         或者如果范围内所有元素都小于指定键，则为 <tt>toIndex</tt>。
     *         这保证了只有在找到键时返回值才会 &gt;= 0。
     * @throws IllegalArgumentException
     *         如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         如果 {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(long[] a, int fromIndex, int toIndex,
                                   long key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // 类似于公共版本，但没有范围检查。
    private static int binarySearch0(long[] a, int fromIndex, int toIndex,
                                     long key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // 找到键
        }
        return -(low + 1);  // 未找到键。
    }

    /**
     * 使用二分搜索算法在指定的 int 型数组中搜索指定的值。数组必须先排序
     * （如通过 {@link #sort(int[])} 方法）才能调用此方法。如果数组未排序，
     * 结果是未定义的。如果数组包含多个指定值的元素，则不保证找到哪一个。
     *
     * @param a 要搜索的数组
     * @param key 要搜索的值
     * @return 如果数组包含搜索键，则返回搜索键的索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：第一个大于键的元素的索引，
     *         或者如果数组中所有元素都小于指定键，则为 <tt>a.length</tt>。
     *         这保证了只有在找到键时返回值才会 &gt;= 0。
     */
    public static int binarySearch(int[] a, int key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * 使用二分搜索算法在指定的 int 型数组的范围内搜索指定的值。
     * 范围必须先排序
     * （如通过 {@link #sort(int[], int, int)} 方法）才能调用此方法。如果范围未排序，
     * 结果是未定义的。如果范围包含多个指定值的元素，则不保证找到哪一个。
     *
     * @param a 要搜索的数组
     * @param fromIndex 第一个元素的索引（包含）要搜索
     * @param toIndex 最后一个元素的索引（不包含）要搜索
     * @param key 要搜索的值
     * @return 如果数组在指定范围内包含搜索键，则返回搜索键的索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：范围内第一个大于键的元素的索引，
     *         或者如果范围内所有元素都小于指定键，则为 <tt>toIndex</tt>。
     *         这保证了只有在找到键时返回值才会 &gt;= 0。
     * @throws IllegalArgumentException
     *         如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         如果 {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(int[] a, int fromIndex, int toIndex,
                                   int key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // 类似于公共版本，但没有范围检查。
    private static int binarySearch0(int[] a, int fromIndex, int toIndex,
                                     int key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // 找到键
        }
        return -(low + 1);  // 未找到键。
    }

    /**
     * 使用二分搜索算法在指定的 short 型数组中搜索指定的值。数组必须先排序
     * （如通过 {@link #sort(short[])} 方法）才能调用此方法。如果数组未排序，
     * 结果是未定义的。如果数组包含多个指定值的元素，则不保证找到哪一个。
     *
     * @param a 要搜索的数组
     * @param key 要搜索的值
     * @return 如果数组包含搜索键，则返回搜索键的索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：第一个大于键的元素的索引，
     *         或者如果数组中所有元素都小于指定键，则为 <tt>a.length</tt>。
     *         这保证了只有在找到键时返回值才会 &gt;= 0。
     */
    public static int binarySearch(short[] a, short key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * 使用二分搜索算法在指定的 short 型数组的范围内搜索指定的值。
     * 范围必须先排序
     * （如通过 {@link #sort(short[], int, int)} 方法）才能调用此方法。如果范围未排序，
     * 结果是未定义的。如果范围包含多个指定值的元素，则不保证找到哪一个。
     *
     * @param a 要搜索的数组
     * @param fromIndex 第一个元素的索引（包含）要搜索
     * @param toIndex 最后一个元素的索引（不包含）要搜索
     * @param key 要搜索的值
     * @return 如果数组在指定范围内包含搜索键，则返回搜索键的索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：范围内第一个大于键的元素的索引，
     *         或者如果范围内所有元素都小于指定键，则为 <tt>toIndex</tt>。
     *         这保证了只有在找到键时返回值才会 &gt;= 0。
     * @throws IllegalArgumentException
     *         如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         如果 {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(short[] a, int fromIndex, int toIndex,
                                   short key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // 类似于公共版本，但没有范围检查。
    private static int binarySearch0(short[] a, int fromIndex, int toIndex,
                                     short key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            short midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // 找到键
        }
        return -(low + 1);  // 未找到键。
    }

    /**
     * 使用二分搜索算法在指定的 char 型数组中搜索指定的值。数组必须先排序
     * （如通过 {@link #sort(char[])} 方法）才能调用此方法。如果数组未排序，
     * 结果是未定义的。如果数组包含多个指定值的元素，则不保证找到哪一个。
     *
     * @param a 要搜索的数组
     * @param key 要搜索的值
     * @return 如果数组包含搜索键，则返回搜索键的索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：第一个大于键的元素的索引，
     *         或者如果数组中所有元素都小于指定键，则为 <tt>a.length</tt>。
     *         这保证了只有在找到键时返回值才会 &gt;= 0。
     */
    public static int binarySearch(char[] a, char key) {
        return binarySearch0(a, 0, a.length, key);
    }


                /**
     * 在指定的字符数组的范围内使用二分查找算法查找指定的值。
     * 范围必须是已排序的（如通过 {@link #sort(char[], int, int)} 方法）
     * 在调用此方法之前。如果未排序，结果是未定义的。如果范围内包含
     * 多个具有指定值的元素，则不能保证找到哪一个。
     *
     * @param a 要搜索的数组
     * @param fromIndex 要搜索的第一个元素的索引（包括）
     * @param toIndex 要搜索的最后一个元素的索引（不包括）
     * @param key 要搜索的值
     * @return 如果在指定范围内找到搜索键，则返回其索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：大于键的第一个元素的索引，
     *         或 <tt>toIndex</tt> 如果范围内的所有元素都小于指定的键。注意
     *         这保证了返回值将为 &gt;= 0 当且仅当找到键。
     * @throws IllegalArgumentException
     *         如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         如果 {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(char[] a, int fromIndex, int toIndex,
                                   char key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // 类似于公共版本，但没有范围检查。
    private static int binarySearch0(char[] a, int fromIndex, int toIndex,
                                     char key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            char midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // 找到键
        }
        return -(low + 1);  // 未找到键。
    }

    /**
     * 使用二分查找算法在指定的字节数组中查找指定的值。数组必须是已排序的
     * （如通过 {@link #sort(byte[])} 方法）在调用此方法之前。如果未排序，
     * 结果是未定义的。如果数组包含多个具有指定值的元素，则不能保证找到哪一个。
     *
     * @param a 要搜索的数组
     * @param key 要搜索的值
     * @return 如果在数组中找到搜索键，则返回其索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：大于键的第一个元素的索引，或 <tt>a.length</tt>
     *         如果数组中的所有元素都小于指定的键。注意
     *         这保证了返回值将为 &gt;= 0 当且仅当找到键。
     */
    public static int binarySearch(byte[] a, byte key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * 在指定的字节数组的范围内使用二分查找算法查找指定的值。
     * 范围必须是已排序的（如通过 {@link #sort(byte[], int, int)} 方法）
     * 在调用此方法之前。如果未排序，结果是未定义的。如果范围内包含
     * 多个具有指定值的元素，则不能保证找到哪一个。
     *
     * @param a 要搜索的数组
     * @param fromIndex 要搜索的第一个元素的索引（包括）
     * @param toIndex 要搜索的最后一个元素的索引（不包括）
     * @param key 要搜索的值
     * @return 如果在指定范围内找到搜索键，则返回其索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：大于键的第一个元素的索引，
     *         或 <tt>toIndex</tt> 如果范围内的所有元素都小于指定的键。注意
     *         这保证了返回值将为 &gt;= 0 当且仅当找到键。
     * @throws IllegalArgumentException
     *         如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         如果 {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(byte[] a, int fromIndex, int toIndex,
                                   byte key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // 类似于公共版本，但没有范围检查。
    private static int binarySearch0(byte[] a, int fromIndex, int toIndex,
                                     byte key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            byte midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // 找到键
        }
        return -(low + 1);  // 未找到键。
    }

    /**
     * 使用二分查找算法在指定的双精度浮点数数组中查找指定的值。数组必须是已排序的
     * （如通过 {@link #sort(double[])} 方法）在调用此方法之前。
     * 如果未排序，结果是未定义的。如果数组包含多个具有指定值的元素，则不能保证找到哪一个。
     * 此方法认为所有 NaN 值是等效且相等的。
     *
     * @param a 要搜索的数组
     * @param key 要搜索的值
     * @return 如果在数组中找到搜索键，则返回其索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：大于键的第一个元素的索引，或 <tt>a.length</tt>
     *         如果数组中的所有元素都小于指定的键。注意
     *         这保证了返回值将为 &gt;= 0 当且仅当找到键。
     */
    public static int binarySearch(double[] a, double key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * 在指定的双精度浮点数数组的范围内使用二分查找算法查找指定的值。
     * 范围必须是已排序的（如通过 {@link #sort(double[], int, int)} 方法）
     * 在调用此方法之前。如果未排序，结果是未定义的。如果范围内包含
     * 多个具有指定值的元素，则不能保证找到哪一个。此方法认为所有 NaN 值是
     * 等效且相等的。
     *
     * @param a 要搜索的数组
     * @param fromIndex 要搜索的第一个元素的索引（包括）
     * @param toIndex 要搜索的最后一个元素的索引（不包括）
     * @param key 要搜索的值
     * @return 如果在指定范围内找到搜索键，则返回其索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：大于键的第一个元素的索引，
     *         或 <tt>toIndex</tt> 如果范围内的所有元素都小于指定的键。注意
     *         这保证了返回值将为 &gt;= 0 当且仅当找到键。
     * @throws IllegalArgumentException
     *         如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         如果 {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(double[] a, int fromIndex, int toIndex,
                                   double key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // 类似于公共版本，但没有范围检查。
    private static int binarySearch0(double[] a, int fromIndex, int toIndex,
                                     double key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            double midVal = a[mid];

            if (midVal < key)
                low = mid + 1;  // 两个值都不是 NaN，此值较小
            else if (midVal > key)
                high = mid - 1; // 两个值都不是 NaN，此值较大
            else {
                long midBits = Double.doubleToLongBits(midVal);
                long keyBits = Double.doubleToLongBits(key);
                if (midBits == keyBits)     // 值相等
                    return mid;             // 找到键
                else if (midBits < keyBits) // (-0.0, 0.0) 或 (!NaN, NaN)
                    low = mid + 1;
                else                        // (0.0, -0.0) 或 (NaN, !NaN)
                    high = mid - 1;
            }
        }
        return -(low + 1);  // 未找到键。
    }

    /**
     * 使用二分查找算法在指定的单精度浮点数数组中查找指定的值。数组必须是已排序的
     * （如通过 {@link #sort(float[])} 方法）在调用此方法之前。如果未排序，
     * 结果是未定义的。如果数组包含多个具有指定值的元素，则不能保证找到哪一个。
     * 此方法认为所有 NaN 值是等效且相等的。
     *
     * @param a 要搜索的数组
     * @param key 要搜索的值
     * @return 如果在数组中找到搜索键，则返回其索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：大于键的第一个元素的索引，或 <tt>a.length</tt>
     *         如果数组中的所有元素都小于指定的键。注意
     *         这保证了返回值将为 &gt;= 0 当且仅当找到键。
     */
    public static int binarySearch(float[] a, float key) {
        return binarySearch0(a, 0, a.length, key);
    }

    /**
     * 在指定的单精度浮点数数组的范围内使用二分查找算法查找指定的值。
     * 范围必须是已排序的（如通过 {@link #sort(float[], int, int)} 方法）
     * 在调用此方法之前。如果未排序，结果是未定义的。如果范围内包含
     * 多个具有指定值的元素，则不能保证找到哪一个。此方法认为所有 NaN 值是
     * 等效且相等的。
     *
     * @param a 要搜索的数组
     * @param fromIndex 要搜索的第一个元素的索引（包括）
     * @param toIndex 要搜索的最后一个元素的索引（不包括）
     * @param key 要搜索的值
     * @return 如果在指定范围内找到搜索键，则返回其索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：大于键的第一个元素的索引，
     *         或 <tt>toIndex</tt> 如果范围内的所有元素都小于指定的键。注意
     *         这保证了返回值将为 &gt;= 0 当且仅当找到键。
     * @throws IllegalArgumentException
     *         如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         如果 {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(float[] a, int fromIndex, int toIndex,
                                   float key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // 类似于公共版本，但没有范围检查。
    private static int binarySearch0(float[] a, int fromIndex, int toIndex,
                                     float key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            float midVal = a[mid];

            if (midVal < key)
                low = mid + 1;  // 两个值都不是 NaN，此值较小
            else if (midVal > key)
                high = mid - 1; // 两个值都不是 NaN，此值较大
            else {
                int midBits = Float.floatToIntBits(midVal);
                int keyBits = Float.floatToIntBits(key);
                if (midBits == keyBits)     // 值相等
                    return mid;             // 找到键
                else if (midBits < keyBits) // (-0.0, 0.0) 或 (!NaN, NaN)
                    low = mid + 1;
                else                        // (0.0, -0.0) 或 (NaN, !NaN)
                    high = mid - 1;
            }
        }
        return -(low + 1);  // 未找到键。
    }

    /**
     * 使用二分查找算法在指定的数组中查找指定的对象。数组必须按升序排序
     * 根据其元素的
     * {@linkplain Comparable 自然顺序}
     * 在调用此方法之前。如果未排序，结果是未定义的。
     * （如果数组包含不相互可比较的元素（例如，字符串和整数），则
     * 无法根据其元素的自然顺序进行排序，因此结果是未定义的。）
     * 如果数组包含多个
     * 与指定对象相等的元素，则不能保证找到哪一个。
     *
     * @param a 要搜索的数组
     * @param key 要搜索的值
     * @return 如果在数组中找到搜索键，则返回其索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i>
     *         定义为键将被插入数组的位置：大于键的第一个元素的索引，或 <tt>a.length</tt>
     *         如果数组中的所有元素都小于指定的键。注意
     *         这保证了返回值将为 &gt;= 0 当且仅当找到键。
     * @throws ClassCastException 如果搜索键与数组的元素不可比较。
     */
    public static int binarySearch(Object[] a, Object key) {
        return binarySearch0(a, 0, a.length, key);
    }


                /**
     * 使用二分查找算法在指定数组的范围内查找指定对象。
     * 该范围必须按照元素的自然顺序（如
     * {@linkplain Comparable 自然顺序}
     * 排序（例如使用
     * {@link #sort(Object[], int, int)} 方法）才能调用此方法。如果未排序，结果未定义。
     * （如果范围包含不可相互比较的元素（例如，字符串和整数），则无法根据元素的自然顺序进行排序，因此结果未定义。）
     * 如果范围包含多个与指定对象相等的元素，则不能保证找到哪一个。
     *
     * @param a 要搜索的数组
     * @param fromIndex 要搜索的第一个元素的索引（包含）
     * @param toIndex 要搜索的最后一个元素的索引（不包含）
     * @param key 要搜索的值
     * @return 如果在指定范围内找到搜索键，则返回其索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i> 定义为键应插入数组的位置：范围中第一个大于键的元素的索引，
     *         或 <tt>toIndex</tt> 如果范围中的所有元素都小于指定键。请注意，这保证了返回值将为 &gt;= 0 当且仅当找到键时。
     * @throws ClassCastException 如果搜索键与指定范围内的数组元素不可比较。
     * @throws IllegalArgumentException
     *         如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         如果 {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(Object[] a, int fromIndex, int toIndex,
                                   Object key) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    // 类似于公共版本，但没有范围检查。
    private static int binarySearch0(Object[] a, int fromIndex, int toIndex,
                                     Object key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            @SuppressWarnings("rawtypes")
            Comparable midVal = (Comparable)a[mid];
            @SuppressWarnings("unchecked")
            int cmp = midVal.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // 找到键
        }
        return -(low + 1);  // 未找到键。
    }

    /**
     * 使用二分查找算法在指定数组中查找指定对象。数组必须按照指定的比较器排序（如
     * {@link #sort(Object[], Comparator) sort(T[], Comparator)}
     * 方法）才能调用此方法。如果未排序，结果未定义。
     * 如果数组包含多个与指定对象相等的元素，则不能保证找到哪一个。
     *
     * @param <T> 数组中对象的类
     * @param a 要搜索的数组
     * @param key 要搜索的值
     * @param c 用于排序数组的比较器。 <tt>null</tt> 值表示应使用元素的
     *        {@linkplain Comparable 自然顺序}。
     * @return 如果在数组中找到搜索键，则返回其索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i> 定义为键应插入数组的位置：第一个大于键的元素的索引，
     *         或 <tt>a.length</tt> 如果数组中的所有元素都小于指定键。请注意，这保证了返回值将为 &gt;= 0 当且仅当找到键时。
     * @throws ClassCastException 如果数组包含使用指定比较器不可相互比较的元素，
     *         或搜索键与使用此比较器的数组元素不可比较。
     */
    public static <T> int binarySearch(T[] a, T key, Comparator<? super T> c) {
        return binarySearch0(a, 0, a.length, key, c);
    }

    /**
     * 使用二分查找算法在指定数组的范围内查找指定对象。
     * 该范围必须按照指定的比较器排序（如
     * {@link #sort(Object[], int, int, Comparator)
     * sort(T[], int, int, Comparator)}
     * 方法）才能调用此方法。
     * 如果未排序，结果未定义。
     * 如果范围包含多个与指定对象相等的元素，则不能保证找到哪一个。
     *
     * @param <T> 数组中对象的类
     * @param a 要搜索的数组
     * @param fromIndex 要搜索的第一个元素的索引（包含）
     * @param toIndex 要搜索的最后一个元素的索引（不包含）
     * @param key 要搜索的值
     * @param c 用于排序数组的比较器。 <tt>null</tt> 值表示应使用元素的
     *        {@linkplain Comparable 自然顺序}。
     * @return 如果在指定范围内找到搜索键，则返回其索引；
     *         否则，返回 <tt>(-(<i>插入点</i>) - 1)</tt>。 <i>插入点</i> 定义为键应插入数组的位置：范围中第一个大于键的元素的索引，
     *         或 <tt>toIndex</tt> 如果范围中的所有元素都小于指定键。请注意，这保证了返回值将为 &gt;= 0 当且仅当找到键时。
     * @throws ClassCastException 如果范围包含使用指定比较器不可相互比较的元素，
     *         或搜索键与使用此比较器的范围中的元素不可比较。
     * @throws IllegalArgumentException
     *         如果 {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *         如果 {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static <T> int binarySearch(T[] a, int fromIndex, int toIndex,
                                       T key, Comparator<? super T> c) {
        rangeCheck(a.length, fromIndex, toIndex);
        return binarySearch0(a, fromIndex, toIndex, key, c);
    }

    // 类似于公共版本，但没有范围检查。
    private static <T> int binarySearch0(T[] a, int fromIndex, int toIndex,
                                         T key, Comparator<? super T> c) {
        if (c == null) {
            return binarySearch0(a, fromIndex, toIndex, key);
        }
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            T midVal = a[mid];
            int cmp = c.compare(midVal, key);
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // 找到键
        }
        return -(low + 1);  // 未找到键。
    }

    // 等值测试

    /**
     * 如果两个指定的 long 数组 <i>相等</i>，则返回 <tt>true</tt>。两个数组被认为是相等的，如果两个数组包含相同数量的元素，并且两个数组中所有对应的元素对都相等。
     * 换句话说，如果两个数组包含相同顺序的相同元素，则它们是相等的。此外，如果两个数组引用都为 <tt>null</tt>，则认为它们相等。<p>
     *
     * @param a 要测试相等性的第一个数组
     * @param a2 要测试相等性的另一个数组
     * @return 如果两个数组相等，则返回 <tt>true</tt>
     */
    public static boolean equals(long[] a, long[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    /**
     * 如果两个指定的 int 数组 <i>相等</i>，则返回 <tt>true</tt>。两个数组被认为是相等的，如果两个数组包含相同数量的元素，并且两个数组中所有对应的元素对都相等。
     * 换句话说，如果两个数组包含相同顺序的相同元素，则它们是相等的。此外，如果两个数组引用都为 <tt>null</tt>，则认为它们相等。<p>
     *
     * @param a 要测试相等性的第一个数组
     * @param a2 要测试相等性的另一个数组
     * @return 如果两个数组相等，则返回 <tt>true</tt>
     */
    public static boolean equals(int[] a, int[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    /**
     * 如果两个指定的 short 数组 <i>相等</i>，则返回 <tt>true</tt>。两个数组被认为是相等的，如果两个数组包含相同数量的元素，并且两个数组中所有对应的元素对都相等。
     * 换句话说，如果两个数组包含相同顺序的相同元素，则它们是相等的。此外，如果两个数组引用都为 <tt>null</tt>，则认为它们相等。<p>
     *
     * @param a 要测试相等性的第一个数组
     * @param a2 要测试相等性的另一个数组
     * @return 如果两个数组相等，则返回 <tt>true</tt>
     */
    public static boolean equals(short[] a, short a2[]) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    /**
     * 如果两个指定的 char 数组 <i>相等</i>，则返回 <tt>true</tt>。两个数组被认为是相等的，如果两个数组包含相同数量的元素，并且两个数组中所有对应的元素对都相等。
     * 换句话说，如果两个数组包含相同顺序的相同元素，则它们是相等的。此外，如果两个数组引用都为 <tt>null</tt>，则认为它们相等。<p>
     *
     * @param a 要测试相等性的第一个数组
     * @param a2 要测试相等性的另一个数组
     * @return 如果两个数组相等，则返回 <tt>true</tt>
     */
    public static boolean equals(char[] a, char[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    /**
     * 如果两个指定的 byte 数组 <i>相等</i>，则返回 <tt>true</tt>。两个数组被认为是相等的，如果两个数组包含相同数量的元素，并且两个数组中所有对应的元素对都相等。
     * 换句话说，如果两个数组包含相同顺序的相同元素，则它们是相等的。此外，如果两个数组引用都为 <tt>null</tt>，则认为它们相等。<p>
     *
     * @param a 要测试相等性的第一个数组
     * @param a2 要测试相等性的另一个数组
     * @return 如果两个数组相等，则返回 <tt>true</tt>
     */
    public static boolean equals(byte[] a, byte[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    /**
     * 如果两个指定的 boolean 数组 <i>相等</i>，则返回 <tt>true</tt>。两个数组被认为是相等的，如果两个数组包含相同数量的元素，并且两个数组中所有对应的元素对都相等。
     * 换句话说，如果两个数组包含相同顺序的相同元素，则它们是相等的。此外，如果两个数组引用都为 <tt>null</tt>，则认为它们相等。<p>
     *
     * @param a 要测试相等性的第一个数组
     * @param a2 要测试相等性的另一个数组
     * @return 如果两个数组相等，则返回 <tt>true</tt>
     */
    public static boolean equals(boolean[] a, boolean[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

    /**
     * 如果两个指定的 double 数组 <i>相等</i>，则返回 <tt>true</tt>。两个数组被认为是相等的，如果两个数组包含相同数量的元素，并且两个数组中所有对应的元素对都相等。
     * 换句话说，如果两个数组包含相同顺序的相同元素，则它们是相等的。此外，如果两个数组引用都为 <tt>null</tt>，则认为它们相等。<p>
     *
     * 两个 double <tt>d1</tt> 和 <tt>d2</tt> 被认为相等如果：
     * <pre>    <tt>new Double(d1).equals(new Double(d2))</tt></pre>
     * （与 <tt>==</tt> 运算符不同，此方法认为 <tt>NaN</tt> 等于自身，0.0d 不等于 -0.0d。）
     *
     * @param a 要测试相等性的第一个数组
     * @param a2 要测试相等性的另一个数组
     * @return 如果两个数组相等，则返回 <tt>true</tt>
     * @see Double#equals(Object)
     */
    public static boolean equals(double[] a, double[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;


                    int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (Double.doubleToLongBits(a[i])!=Double.doubleToLongBits(a2[i]))
                return false;

        return true;
    }

    /**
     * 如果两个指定的浮点数数组彼此<i>相等</i>，则返回<tt>true</tt>。如果两个数组包含相同数量的元素，并且两个数组中的所有对应元素对都相等，则认为这两个数组相等。换句话说，如果两个数组包含相同顺序的相同元素，则认为它们相等。此外，如果两个数组引用都为<tt>null</tt>，则认为它们相等。<p>
     *
     * 两个浮点数<tt>f1</tt>和<tt>f2</tt>被认为相等，如果：
     * <pre>    <tt>new Float(f1).equals(new Float(f2))</tt></pre>
     * （与<tt>==</tt>运算符不同，此方法认为<tt>NaN</tt>等于自身，而0.0f不等于-0.0f。）
     *
     * @param a 要测试相等性的第一个数组
     * @param a2 要测试相等性的另一个数组
     * @return 如果两个数组相等，则返回<tt>true</tt>
     * @see Float#equals(Object)
     */
    public static boolean equals(float[] a, float[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (Float.floatToIntBits(a[i])!=Float.floatToIntBits(a2[i]))
                return false;

        return true;
    }

    /**
     * 如果两个指定的对象数组彼此<i>相等</i>，则返回<tt>true</tt>。如果两个数组包含相同数量的元素，并且两个数组中的所有对应元素对都相等，则认为这两个数组相等。两个对象<tt>e1</tt>和<tt>e2</tt>被认为<i>相等</i>，如果<tt>(e1==null ? e2==null : e1.equals(e2))</tt>。换句话说，如果两个数组包含相同顺序的相同元素，则认为它们相等。此外，如果两个数组引用都为<tt>null</tt>，则认为它们相等。<p>
     *
     * @param a 要测试相等性的第一个数组
     * @param a2 要测试相等性的另一个数组
     * @return 如果两个数组相等，则返回<tt>true</tt>
     */
    public static boolean equals(Object[] a, Object[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++) {
            Object o1 = a[i];
            Object o2 = a2[i];
            if (!(o1==null ? o2==null : o1.equals(o2)))
                return false;
        }

        return true;
    }

    // 填充

    /**
     * 将指定的长整型值赋给指定的长整型数组的每个元素。
     *
     * @param a 要填充的数组
     * @param val 要存储在数组所有元素中的值
     */
    public static void fill(long[] a, long val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * 将指定的长整型值赋给指定的长整型数组的指定范围的每个元素。要填充的范围从索引<tt>fromIndex</tt>（包含）到索引<tt>toIndex</tt>（不包含）。如果<tt>fromIndex==toIndex</tt>，则要填充的范围为空。
     *
     * @param a 要填充的数组
     * @param fromIndex 要用指定值填充的第一个元素的索引（包含）
     * @param toIndex 要用指定值填充的最后一个元素的索引（不包含）
     * @param val 要存储在数组所有元素中的值
     * @throws IllegalArgumentException 如果<tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException 如果<tt>fromIndex &lt; 0</tt>或<tt>toIndex &gt; a.length</tt>
     */
    public static void fill(long[] a, int fromIndex, int toIndex, long val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * 将指定的整型值赋给指定的整型数组的每个元素。
     *
     * @param a 要填充的数组
     * @param val 要存储在数组所有元素中的值
     */
    public static void fill(int[] a, int val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * 将指定的整型值赋给指定的整型数组的指定范围的每个元素。要填充的范围从索引<tt>fromIndex</tt>（包含）到索引<tt>toIndex</tt>（不包含）。如果<tt>fromIndex==toIndex</tt>，则要填充的范围为空。
     *
     * @param a 要填充的数组
     * @param fromIndex 要用指定值填充的第一个元素的索引（包含）
     * @param toIndex 要用指定值填充的最后一个元素的索引（不包含）
     * @param val 要存储在数组所有元素中的值
     * @throws IllegalArgumentException 如果<tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException 如果<tt>fromIndex &lt; 0</tt>或<tt>toIndex &gt; a.length</tt>
     */
    public static void fill(int[] a, int fromIndex, int toIndex, int val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * 将指定的短整型值赋给指定的短整型数组的每个元素。
     *
     * @param a 要填充的数组
     * @param val 要存储在数组所有元素中的值
     */
    public static void fill(short[] a, short val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * 将指定的短整型值赋给指定的短整型数组的指定范围的每个元素。要填充的范围从索引<tt>fromIndex</tt>（包含）到索引<tt>toIndex</tt>（不包含）。如果<tt>fromIndex==toIndex</tt>，则要填充的范围为空。
     *
     * @param a 要填充的数组
     * @param fromIndex 要用指定值填充的第一个元素的索引（包含）
     * @param toIndex 要用指定值填充的最后一个元素的索引（不包含）
     * @param val 要存储在数组所有元素中的值
     * @throws IllegalArgumentException 如果<tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException 如果<tt>fromIndex &lt; 0</tt>或<tt>toIndex &gt; a.length</tt>
     */
    public static void fill(short[] a, int fromIndex, int toIndex, short val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * 将指定的字符值赋给指定的字符数组的每个元素。
     *
     * @param a 要填充的数组
     * @param val 要存储在数组所有元素中的值
     */
    public static void fill(char[] a, char val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * 将指定的字符值赋给指定的字符数组的指定范围的每个元素。要填充的范围从索引<tt>fromIndex</tt>（包含）到索引<tt>toIndex</tt>（不包含）。如果<tt>fromIndex==toIndex</tt>，则要填充的范围为空。
     *
     * @param a 要填充的数组
     * @param fromIndex 要用指定值填充的第一个元素的索引（包含）
     * @param toIndex 要用指定值填充的最后一个元素的索引（不包含）
     * @param val 要存储在数组所有元素中的值
     * @throws IllegalArgumentException 如果<tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException 如果<tt>fromIndex &lt; 0</tt>或<tt>toIndex &gt; a.length</tt>
     */
    public static void fill(char[] a, int fromIndex, int toIndex, char val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * 将指定的字节值赋给指定的字节数组的每个元素。
     *
     * @param a 要填充的数组
     * @param val 要存储在数组所有元素中的值
     */
    public static void fill(byte[] a, byte val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * 将指定的字节值赋给指定的字节数组的指定范围的每个元素。要填充的范围从索引<tt>fromIndex</tt>（包含）到索引<tt>toIndex</tt>（不包含）。如果<tt>fromIndex==toIndex</tt>，则要填充的范围为空。
     *
     * @param a 要填充的数组
     * @param fromIndex 要用指定值填充的第一个元素的索引（包含）
     * @param toIndex 要用指定值填充的最后一个元素的索引（不包含）
     * @param val 要存储在数组所有元素中的值
     * @throws IllegalArgumentException 如果<tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException 如果<tt>fromIndex &lt; 0</tt>或<tt>toIndex &gt; a.length</tt>
     */
    public static void fill(byte[] a, int fromIndex, int toIndex, byte val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * 将指定的布尔值赋给指定的布尔数组的每个元素。
     *
     * @param a 要填充的数组
     * @param val 要存储在数组所有元素中的值
     */
    public static void fill(boolean[] a, boolean val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * 将指定的布尔值赋给指定的布尔数组的指定范围的每个元素。要填充的范围从索引<tt>fromIndex</tt>（包含）到索引<tt>toIndex</tt>（不包含）。如果<tt>fromIndex==toIndex</tt>，则要填充的范围为空。
     *
     * @param a 要填充的数组
     * @param fromIndex 要用指定值填充的第一个元素的索引（包含）
     * @param toIndex 要用指定值填充的最后一个元素的索引（不包含）
     * @param val 要存储在数组所有元素中的值
     * @throws IllegalArgumentException 如果<tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException 如果<tt>fromIndex &lt; 0</tt>或<tt>toIndex &gt; a.length</tt>
     */
    public static void fill(boolean[] a, int fromIndex, int toIndex,
                            boolean val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * 将指定的双精度浮点值赋给指定的双精度浮点数组的每个元素。
     *
     * @param a 要填充的数组
     * @param val 要存储在数组所有元素中的值
     */
    public static void fill(double[] a, double val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * 将指定的双精度浮点值赋给指定的双精度浮点数组的指定范围的每个元素。要填充的范围从索引<tt>fromIndex</tt>（包含）到索引<tt>toIndex</tt>（不包含）。如果<tt>fromIndex==toIndex</tt>，则要填充的范围为空。
     *
     * @param a 要填充的数组
     * @param fromIndex 要用指定值填充的第一个元素的索引（包含）
     * @param toIndex 要用指定值填充的最后一个元素的索引（不包含）
     * @param val 要存储在数组所有元素中的值
     * @throws IllegalArgumentException 如果<tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException 如果<tt>fromIndex &lt; 0</tt>或<tt>toIndex &gt; a.length</tt>
     */
    public static void fill(double[] a, int fromIndex, int toIndex,double val){
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * 将指定的单精度浮点值赋给指定的单精度浮点数组的每个元素。
     *
     * @param a 要填充的数组
     * @param val 要存储在数组所有元素中的值
     */
    public static void fill(float[] a, float val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * 将指定的单精度浮点值赋给指定的单精度浮点数组的指定范围的每个元素。要填充的范围从索引<tt>fromIndex</tt>（包含）到索引<tt>toIndex</tt>（不包含）。如果<tt>fromIndex==toIndex</tt>，则要填充的范围为空。
     *
     * @param a 要填充的数组
     * @param fromIndex 要用指定值填充的第一个元素的索引（包含）
     * @param toIndex 要用指定值填充的最后一个元素的索引（不包含）
     * @param val 要存储在数组所有元素中的值
     * @throws IllegalArgumentException 如果<tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException 如果<tt>fromIndex &lt; 0</tt>或<tt>toIndex &gt; a.length</tt>
     */
    public static void fill(float[] a, int fromIndex, int toIndex, float val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }

    /**
     * 将指定的对象引用赋给指定的对象数组的每个元素。
     *
     * @param a 要填充的数组
     * @param val 要存储在数组所有元素中的值
     * @throws ArrayStoreException 如果指定的值不是可以存储在指定数组中的运行时类型
     */
    public static void fill(Object[] a, Object val) {
        for (int i = 0, len = a.length; i < len; i++)
            a[i] = val;
    }

    /**
     * 将指定的对象引用赋给指定的对象数组的指定范围的每个元素。要填充的范围从索引<tt>fromIndex</tt>（包含）到索引<tt>toIndex</tt>（不包含）。如果<tt>fromIndex==toIndex</tt>，则要填充的范围为空。
     *
     * @param a 要填充的数组
     * @param fromIndex 要用指定值填充的第一个元素的索引（包含）
     * @param toIndex 要用指定值填充的最后一个元素的索引（不包含）
     * @param val 要存储在数组所有元素中的值
     * @throws IllegalArgumentException 如果<tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException 如果<tt>fromIndex &lt; 0</tt>或<tt>toIndex &gt; a.length</tt>
     * @throws ArrayStoreException 如果指定的值不是可以存储在指定数组中的运行时类型
     */
    public static void fill(Object[] a, int fromIndex, int toIndex, Object val) {
        rangeCheck(a.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }


                // Cloning

    /**
     * 复制指定的数组，必要时截断或用 null 填充，以使副本具有指定的长度。对于原始数组和副本中都有效的所有索引，两个数组将包含相同的值。对于副本中有效但原始数组中无效的任何索引，副本将包含 <tt>null</tt>。当且仅当指定的长度大于原始数组的长度时，才会存在这样的索引。结果数组与原始数组完全属于同一类。
     *
     * @param <T> 数组中对象的类
     * @param original 要复制的数组
     * @param newLength 要返回的副本的长度
     * @return 原始数组的副本，必要时截断或用 null 填充以达到指定的长度
     * @throws NegativeArraySizeException 如果 <tt>newLength</tt> 为负
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] copyOf(T[] original, int newLength) {
        return (T[]) copyOf(original, newLength, original.getClass());
    }

    /**
     * 复制指定的数组，必要时截断或用 null 填充，以使副本具有指定的长度。对于原始数组和副本中都有效的所有索引，两个数组将包含相同的值。对于副本中有效但原始数组中无效的任何索引，副本将包含 <tt>null</tt>。当且仅当指定的长度大于原始数组的长度时，才会存在这样的索引。结果数组属于 <tt>newType</tt> 类。
     *
     * @param <U> 原始数组中对象的类
     * @param <T> 返回数组中对象的类
     * @param original 要复制的数组
     * @param newLength 要返回的副本的长度
     * @param newType 要返回的副本的类
     * @return 原始数组的副本，必要时截断或用 null 填充以达到指定的长度
     * @throws NegativeArraySizeException 如果 <tt>newLength</tt> 为负
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @throws ArrayStoreException 如果从 <tt>original</tt> 复制的元素不是可以在 <tt>newType</tt> 类数组中存储的运行时类型
     * @since 1.6
     */
    public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        @SuppressWarnings("unchecked")
        T[] copy = ((Object)newType == (Object)Object[].class)
            ? (T[]) new Object[newLength]
            : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * 复制指定的数组，必要时截断或用零填充，以使副本具有指定的长度。对于原始数组和副本中都有效的所有索引，两个数组将包含相同的值。对于副本中有效但原始数组中无效的任何索引，副本将包含 <tt>(byte)0</tt>。当且仅当指定的长度大于原始数组的长度时，才会存在这样的索引。
     *
     * @param original 要复制的数组
     * @param newLength 要返回的副本的长度
     * @return 原始数组的副本，必要时截断或用零填充以达到指定的长度
     * @throws NegativeArraySizeException 如果 <tt>newLength</tt> 为负
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static byte[] copyOf(byte[] original, int newLength) {
        byte[] copy = new byte[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * 复制指定的数组，必要时截断或用零填充，以使副本具有指定的长度。对于原始数组和副本中都有效的所有索引，两个数组将包含相同的值。对于副本中有效但原始数组中无效的任何索引，副本将包含 <tt>(short)0</tt>。当且仅当指定的长度大于原始数组的长度时，才会存在这样的索引。
     *
     * @param original 要复制的数组
     * @param newLength 要返回的副本的长度
     * @return 原始数组的副本，必要时截断或用零填充以达到指定的长度
     * @throws NegativeArraySizeException 如果 <tt>newLength</tt> 为负
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static short[] copyOf(short[] original, int newLength) {
        short[] copy = new short[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * 复制指定的数组，必要时截断或用零填充，以使副本具有指定的长度。对于原始数组和副本中都有效的所有索引，两个数组将包含相同的值。对于副本中有效但原始数组中无效的任何索引，副本将包含 <tt>0</tt>。当且仅当指定的长度大于原始数组的长度时，才会存在这样的索引。
     *
     * @param original 要复制的数组
     * @param newLength 要返回的副本的长度
     * @return 原始数组的副本，必要时截断或用零填充以达到指定的长度
     * @throws NegativeArraySizeException 如果 <tt>newLength</tt> 为负
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static int[] copyOf(int[] original, int newLength) {
        int[] copy = new int[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * 复制指定的数组，必要时截断或用零填充，以使副本具有指定的长度。对于原始数组和副本中都有效的所有索引，两个数组将包含相同的值。对于副本中有效但原始数组中无效的任何索引，副本将包含 <tt>0L</tt>。当且仅当指定的长度大于原始数组的长度时，才会存在这样的索引。
     *
     * @param original 要复制的数组
     * @param newLength 要返回的副本的长度
     * @return 原始数组的副本，必要时截断或用零填充以达到指定的长度
     * @throws NegativeArraySizeException 如果 <tt>newLength</tt> 为负
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static long[] copyOf(long[] original, int newLength) {
        long[] copy = new long[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * 复制指定的数组，必要时截断或用空字符填充，以使副本具有指定的长度。对于原始数组和副本中都有效的所有索引，两个数组将包含相同的值。对于副本中有效但原始数组中无效的任何索引，副本将包含 <tt>'\\u000'</tt>。当且仅当指定的长度大于原始数组的长度时，才会存在这样的索引。
     *
     * @param original 要复制的数组
     * @param newLength 要返回的副本的长度
     * @return 原始数组的副本，必要时截断或用空字符填充以达到指定的长度
     * @throws NegativeArraySizeException 如果 <tt>newLength</tt> 为负
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static char[] copyOf(char[] original, int newLength) {
        char[] copy = new char[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * 复制指定的数组，必要时截断或用零填充，以使副本具有指定的长度。对于原始数组和副本中都有效的所有索引，两个数组将包含相同的值。对于副本中有效但原始数组中无效的任何索引，副本将包含 <tt>0f</tt>。当且仅当指定的长度大于原始数组的长度时，才会存在这样的索引。
     *
     * @param original 要复制的数组
     * @param newLength 要返回的副本的长度
     * @return 原始数组的副本，必要时截断或用零填充以达到指定的长度
     * @throws NegativeArraySizeException 如果 <tt>newLength</tt> 为负
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static float[] copyOf(float[] original, int newLength) {
        float[] copy = new float[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * 复制指定的数组，必要时截断或用零填充，以使副本具有指定的长度。对于原始数组和副本中都有效的所有索引，两个数组将包含相同的值。对于副本中有效但原始数组中无效的任何索引，副本将包含 <tt>0d</tt>。当且仅当指定的长度大于原始数组的长度时，才会存在这样的索引。
     *
     * @param original 要复制的数组
     * @param newLength 要返回的副本的长度
     * @return 原始数组的副本，必要时截断或用零填充以达到指定的长度
     * @throws NegativeArraySizeException 如果 <tt>newLength</tt> 为负
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static double[] copyOf(double[] original, int newLength) {
        double[] copy = new double[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * 复制指定的数组，必要时截断或用 <tt>false</tt> 填充，以使副本具有指定的长度。对于原始数组和副本中都有效的所有索引，两个数组将包含相同的值。对于副本中有效但原始数组中无效的任何索引，副本将包含 <tt>false</tt>。当且仅当指定的长度大于原始数组的长度时，才会存在这样的索引。
     *
     * @param original 要复制的数组
     * @param newLength 要返回的副本的长度
     * @return 原始数组的副本，必要时截断或用 false 元素填充以达到指定的长度
     * @throws NegativeArraySizeException 如果 <tt>newLength</tt> 为负
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static boolean[] copyOf(boolean[] original, int newLength) {
        boolean[] copy = new boolean[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    /**
     * 复制指定数组的指定范围到新数组中。范围的起始索引（<tt>from</tt>）必须在零和 <tt>original.length</tt> 之间，包括零和 <tt>original.length</tt>。值 <tt>original[from]</tt> 被放置在副本的初始元素中（除非 <tt>from == original.length</tt> 或 <tt>from == to</tt>）。原始数组中的后续元素的值被放置在副本中的后续元素中。范围的结束索引（<tt>to</tt>），必须大于或等于 <tt>from</tt>，可以大于 <tt>original.length</tt>，在这种情况下，索引大于或等于 <tt>original.length - from</tt> 的所有副本元素将被设置为 <tt>null</tt>。返回数组的长度将是 <tt>to - from</tt>。
     * <p>
     * 结果数组与原始数组完全属于同一类。
     *
     * @param <T> 数组中对象的类
     * @param original 要复制范围的数组
     * @param from 要复制的范围的起始索引，包括
     * @param to 要复制的范围的结束索引，不包括。（此索引可以超出数组。）
     * @return 一个新数组，包含原始数组的指定范围，必要时截断或用 null 填充以达到所需的长度
     * @throws ArrayIndexOutOfBoundsException 如果 {@code from < 0}
     *     或 {@code from > original.length}
     * @throws IllegalArgumentException 如果 <tt>from &gt; to</tt>
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] copyOfRange(T[] original, int from, int to) {
        return copyOfRange(original, from, to, (Class<? extends T[]>) original.getClass());
    }

    /**
     * 复制指定数组的指定范围到新数组中。范围的起始索引（<tt>from</tt>）必须在零和 <tt>original.length</tt> 之间，包括零和 <tt>original.length</tt>。值 <tt>original[from]</tt> 被放置在副本的初始元素中（除非 <tt>from == original.length</tt> 或 <tt>from == to</tt>）。原始数组中的后续元素的值被放置在副本中的后续元素中。范围的结束索引（<tt>to</tt>），必须大于或等于 <tt>from</tt>，可以大于 <tt>original.length</tt>，在这种情况下，索引大于或等于 <tt>original.length - from</tt> 的所有副本元素将被设置为 <tt>null</tt>。返回数组的长度将是 <tt>to - from</tt>。结果数组属于 <tt>newType</tt> 类。
     *
     * @param <U> 原始数组中对象的类
     * @param <T> 返回数组中对象的类
     * @param original 要复制范围的数组
     * @param from 要复制的范围的起始索引，包括
     * @param to 要复制的范围的结束索引，不包括。（此索引可以超出数组。）
     * @param newType 要返回的副本的类
     * @return 一个新数组，包含原始数组的指定范围，必要时截断或用 null 填充以达到所需的长度
     * @throws ArrayIndexOutOfBoundsException 如果 {@code from < 0}
     *     或 {@code from > original.length}
     * @throws IllegalArgumentException 如果 <tt>from &gt; to</tt>
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @throws ArrayStoreException 如果从 <tt>original</tt> 复制的元素不是可以在 <tt>newType</tt> 类数组中存储的运行时类型。
     * @since 1.6
     */
    public static <T,U> T[] copyOfRange(U[] original, int from, int to, Class<? extends T[]> newType) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        @SuppressWarnings("unchecked")
        T[] copy = ((Object)newType == (Object)Object[].class)
            ? (T[]) new Object[newLength]
            : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }


                /**
     * 将指定数组的指定范围复制到一个新数组中。
     * 范围的初始索引（<tt>from</tt>）必须在零和<tt>original.length</tt>之间，包括零和<tt>original.length</tt>。 
     * <tt>original[from]</tt>的值被放置到副本的初始元素中
     * （除非<tt>from == original.length</tt>或<tt>from == to</tt>）。
     * 原数组后续元素的值被放置到副本的后续元素中。
     * 范围的最终索引（<tt>to</tt>），必须大于或等于<tt>from</tt>，
     * 可能大于<tt>original.length</tt>，在这种情况下，
     * <tt>(byte)0</tt>被放置在所有索引大于或等于<tt>original.length - from</tt>的副本元素中。 
     * 返回数组的长度将是<tt>to - from</tt>。
     *
     * @param original 要从中复制范围的数组
     * @param from 要复制的范围的初始索引，包括
     * @param to 要复制的范围的最终索引，不包括。
     *     （此索引可能位于数组之外。）
     * @return 一个新数组，包含从原数组复制的指定范围，
     *     为了达到所需长度，被截断或用零填充
     * @throws ArrayIndexOutOfBoundsException 如果 {@code from < 0}
     *     或 {@code from > original.length}
     * @throws IllegalArgumentException 如果 <tt>from &gt; to</tt>
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static byte[] copyOfRange(byte[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * 将指定数组的指定范围复制到一个新数组中。
     * 范围的初始索引（<tt>from</tt>）必须在零和<tt>original.length</tt>之间，包括零和<tt>original.length</tt>。 
     * <tt>original[from]</tt>的值被放置到副本的初始元素中
     * （除非<tt>from == original.length</tt>或<tt>from == to</tt>）。
     * 原数组后续元素的值被放置到副本的后续元素中。
     * 范围的最终索引（<tt>to</tt>），必须大于或等于<tt>from</tt>，
     * 可能大于<tt>original.length</tt>，在这种情况下，
     * <tt>(short)0</tt>被放置在所有索引大于或等于<tt>original.length - from</tt>的副本元素中。 
     * 返回数组的长度将是<tt>to - from</tt>。
     *
     * @param original 要从中复制范围的数组
     * @param from 要复制的范围的初始索引，包括
     * @param to 要复制的范围的最终索引，不包括。
     *     （此索引可能位于数组之外。）
     * @return 一个新数组，包含从原数组复制的指定范围，
     *     为了达到所需长度，被截断或用零填充
     * @throws ArrayIndexOutOfBoundsException 如果 {@code from < 0}
     *     或 {@code from > original.length}
     * @throws IllegalArgumentException 如果 <tt>from &gt; to</tt>
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static short[] copyOfRange(short[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        short[] copy = new short[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * 将指定数组的指定范围复制到一个新数组中。
     * 范围的初始索引（<tt>from</tt>）必须在零和<tt>original.length</tt>之间，包括零和<tt>original.length</tt>。 
     * <tt>original[from]</tt>的值被放置到副本的初始元素中
     * （除非<tt>from == original.length</tt>或<tt>from == to</tt>）。
     * 原数组后续元素的值被放置到副本的后续元素中。
     * 范围的最终索引（<tt>to</tt>），必须大于或等于<tt>from</tt>，
     * 可能大于<tt>original.length</tt>，在这种情况下，
     * <tt>0</tt>被放置在所有索引大于或等于<tt>original.length - from</tt>的副本元素中。 
     * 返回数组的长度将是<tt>to - from</tt>。
     *
     * @param original 要从中复制范围的数组
     * @param from 要复制的范围的初始索引，包括
     * @param to 要复制的范围的最终索引，不包括。
     *     （此索引可能位于数组之外。）
     * @return 一个新数组，包含从原数组复制的指定范围，
     *     为了达到所需长度，被截断或用零填充
     * @throws ArrayIndexOutOfBoundsException 如果 {@code from < 0}
     *     或 {@code from > original.length}
     * @throws IllegalArgumentException 如果 <tt>from &gt; to</tt>
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static int[] copyOfRange(int[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        int[] copy = new int[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * 将指定数组的指定范围复制到一个新数组中。
     * 范围的初始索引（<tt>from</tt>）必须在零和<tt>original.length</tt>之间，包括零和<tt>original.length</tt>。 
     * <tt>original[from]</tt>的值被放置到副本的初始元素中
     * （除非<tt>from == original.length</tt>或<tt>from == to</tt>）。
     * 原数组后续元素的值被放置到副本的后续元素中。
     * 范围的最终索引（<tt>to</tt>），必须大于或等于<tt>from</tt>，
     * 可能大于<tt>original.length</tt>，在这种情况下，
     * <tt>0L</tt>被放置在所有索引大于或等于<tt>original.length - from</tt>的副本元素中。 
     * 返回数组的长度将是<tt>to - from</tt>。
     *
     * @param original 要从中复制范围的数组
     * @param from 要复制的范围的初始索引，包括
     * @param to 要复制的范围的最终索引，不包括。
     *     （此索引可能位于数组之外。）
     * @return 一个新数组，包含从原数组复制的指定范围，
     *     为了达到所需长度，被截断或用零填充
     * @throws ArrayIndexOutOfBoundsException 如果 {@code from < 0}
     *     或 {@code from > original.length}
     * @throws IllegalArgumentException 如果 <tt>from &gt; to</tt>
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static long[] copyOfRange(long[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        long[] copy = new long[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * 将指定数组的指定范围复制到一个新数组中。
     * 范围的初始索引（<tt>from</tt>）必须在零和<tt>original.length</tt>之间，包括零和<tt>original.length</tt>。 
     * <tt>original[from]</tt>的值被放置到副本的初始元素中
     * （除非<tt>from == original.length</tt>或<tt>from == to</tt>）。
     * 原数组后续元素的值被放置到副本的后续元素中。
     * 范围的最终索引（<tt>to</tt>），必须大于或等于<tt>from</tt>，
     * 可能大于<tt>original.length</tt>，在这种情况下，
     * <tt>'\\u000'</tt>被放置在所有索引大于或等于<tt>original.length - from</tt>的副本元素中。 
     * 返回数组的长度将是<tt>to - from</tt>。
     *
     * @param original 要从中复制范围的数组
     * @param from 要复制的范围的初始索引，包括
     * @param to 要复制的范围的最终索引，不包括。
     *     （此索引可能位于数组之外。）
     * @return 一个新数组，包含从原数组复制的指定范围，
     *     为了达到所需长度，被截断或用空字符填充
     * @throws ArrayIndexOutOfBoundsException 如果 {@code from < 0}
     *     或 {@code from > original.length}
     * @throws IllegalArgumentException 如果 <tt>from &gt; to</tt>
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static char[] copyOfRange(char[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        char[] copy = new char[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * 将指定数组的指定范围复制到一个新数组中。
     * 范围的初始索引（<tt>from</tt>）必须在零和<tt>original.length</tt>之间，包括零和<tt>original.length</tt>。 
     * <tt>original[from]</tt>的值被放置到副本的初始元素中
     * （除非<tt>from == original.length</tt>或<tt>from == to</tt>）。
     * 原数组后续元素的值被放置到副本的后续元素中。
     * 范围的最终索引（<tt>to</tt>），必须大于或等于<tt>from</tt>，
     * 可能大于<tt>original.length</tt>，在这种情况下，
     * <tt>0f</tt>被放置在所有索引大于或等于<tt>original.length - from</tt>的副本元素中。 
     * 返回数组的长度将是<tt>to - from</tt>。
     *
     * @param original 要从中复制范围的数组
     * @param from 要复制的范围的初始索引，包括
     * @param to 要复制的范围的最终索引，不包括。
     *     （此索引可能位于数组之外。）
     * @return 一个新数组，包含从原数组复制的指定范围，
     *     为了达到所需长度，被截断或用零填充
     * @throws ArrayIndexOutOfBoundsException 如果 {@code from < 0}
     *     或 {@code from > original.length}
     * @throws IllegalArgumentException 如果 <tt>from &gt; to</tt>
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static float[] copyOfRange(float[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        float[] copy = new float[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * 将指定数组的指定范围复制到一个新数组中。
     * 范围的初始索引（<tt>from</tt>）必须在零和<tt>original.length</tt>之间，包括零和<tt>original.length</tt>。 
     * <tt>original[from]</tt>的值被放置到副本的初始元素中
     * （除非<tt>from == original.length</tt>或<tt>from == to</tt>）。
     * 原数组后续元素的值被放置到副本的后续元素中。
     * 范围的最终索引（<tt>to</tt>），必须大于或等于<tt>from</tt>，
     * 可能大于<tt>original.length</tt>，在这种情况下，
     * <tt>0d</tt>被放置在所有索引大于或等于<tt>original.length - from</tt>的副本元素中。 
     * 返回数组的长度将是<tt>to - from</tt>。
     *
     * @param original 要从中复制范围的数组
     * @param from 要复制的范围的初始索引，包括
     * @param to 要复制的范围的最终索引，不包括。
     *     （此索引可能位于数组之外。）
     * @return 一个新数组，包含从原数组复制的指定范围，
     *     为了达到所需长度，被截断或用零填充
     * @throws ArrayIndexOutOfBoundsException 如果 {@code from < 0}
     *     或 {@code from > original.length}
     * @throws IllegalArgumentException 如果 <tt>from &gt; to</tt>
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static double[] copyOfRange(double[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        double[] copy = new double[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * 将指定数组的指定范围复制到一个新数组中。
     * 范围的初始索引（<tt>from</tt>）必须在零和<tt>original.length</tt>之间，包括零和<tt>original.length</tt>。 
     * <tt>original[from]</tt>的值被放置到副本的初始元素中
     * （除非<tt>from == original.length</tt>或<tt>from == to</tt>）。
     * 原数组后续元素的值被放置到副本的后续元素中。
     * 范围的最终索引（<tt>to</tt>），必须大于或等于<tt>from</tt>，
     * 可能大于<tt>original.length</tt>，在这种情况下，
     * <tt>false</tt>被放置在所有索引大于或等于<tt>original.length - from</tt>的副本元素中。 
     * 返回数组的长度将是<tt>to - from</tt>。
     *
     * @param original 要从中复制范围的数组
     * @param from 要复制的范围的初始索引，包括
     * @param to 要复制的范围的最终索引，不包括。
     *     （此索引可能位于数组之外。）
     * @return 一个新数组，包含从原数组复制的指定范围，
     *     为了达到所需长度，被截断或用 false 元素填充
     * @throws ArrayIndexOutOfBoundsException 如果 {@code from < 0}
     *     或 {@code from > original.length}
     * @throws IllegalArgumentException 如果 <tt>from &gt; to</tt>
     * @throws NullPointerException 如果 <tt>original</tt> 为 null
     * @since 1.6
     */
    public static boolean[] copyOfRange(boolean[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        boolean[] copy = new boolean[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }


                // Misc

    /**
     * 返回由指定数组支持的固定大小的列表。（对返回列表的更改会“写入”到数组中。）此方法充当基于数组的API和基于集合的API之间的桥梁，
     * 结合使用 {@link Collection#toArray}。返回的列表是可序列化的，并实现了 {@link RandomAccess}。
     *
     * <p>此方法还提供了一种方便的方式来创建初始化为包含几个元素的固定大小列表：
     * <pre>
     *     List&lt;String&gt; stooges = Arrays.asList("Larry", "Moe", "Curly");
     * </pre>
     *
     * @param <T> 数组中对象的类
     * @param a 由其支持列表的数组
     * @return 指定数组的列表视图
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> asList(T... a) {
        return new ArrayList<>(a);
    }

    /**
     * @serial include
     */
    private static class ArrayList<E> extends AbstractList<E>
        implements RandomAccess, java.io.Serializable
    {
        private static final long serialVersionUID = -2764017481108945198L;
        private final E[] a;

        ArrayList(E[] array) {
            a = Objects.requireNonNull(array);
        }

        @Override
        public int size() {
            return a.length;
        }

        @Override
        public Object[] toArray() {
            return a.clone();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            int size = size();
            if (a.length < size)
                return Arrays.copyOf(this.a, size,
                                     (Class<? extends T[]>) a.getClass());
            System.arraycopy(this.a, 0, a, 0, size);
            if (a.length > size)
                a[size] = null;
            return a;
        }

        @Override
        public E get(int index) {
            return a[index];
        }

        @Override
        public E set(int index, E element) {
            E oldValue = a[index];
            a[index] = element;
            return oldValue;
        }

        @Override
        public int indexOf(Object o) {
            E[] a = this.a;
            if (o == null) {
                for (int i = 0; i < a.length; i++)
                    if (a[i] == null)
                        return i;
            } else {
                for (int i = 0; i < a.length; i++)
                    if (o.equals(a[i]))
                        return i;
            }
            return -1;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) != -1;
        }

        @Override
        public Spliterator<E> spliterator() {
            return Spliterators.spliterator(a, Spliterator.ORDERED);
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            for (E e : a) {
                action.accept(e);
            }
        }

        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            Objects.requireNonNull(operator);
            E[] a = this.a;
            for (int i = 0; i < a.length; i++) {
                a[i] = operator.apply(a[i]);
            }
        }

        @Override
        public void sort(Comparator<? super E> c) {
            Arrays.sort(a, c);
        }
    }

    /**
     * 基于指定数组的内容返回一个哈希码。对于任何两个 <tt>long</tt> 数组 <tt>a</tt> 和 <tt>b</tt>
     * 如果 <tt>Arrays.equals(a, b)</tt>，那么也有 <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>。
     *
     * <p>此方法返回的值与调用包含表示 <tt>a</tt> 中元素的 {@link Long} 实例序列的 {@link List} 的
     * {@link List#hashCode() <tt>hashCode</tt>} 方法所获得的值相同。如果 <tt>a</tt> 为 <tt>null</tt>，此方法返回 0。
     *
     * @param a 要计算哈希值的数组
     * @return 基于内容的 <tt>a</tt> 的哈希码
     * @since 1.5
     */
    public static int hashCode(long a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (long element : a) {
            int elementHash = (int)(element ^ (element >>> 32));
            result = 31 * result + elementHash;
        }

        return result;
    }

    /**
     * 基于指定数组的内容返回一个哈希码。对于任何两个非空 <tt>int</tt> 数组 <tt>a</tt> 和 <tt>b</tt>
     * 如果 <tt>Arrays.equals(a, b)</tt>，那么也有 <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>。
     *
     * <p>此方法返回的值与调用包含表示 <tt>a</tt> 中元素的 {@link Integer} 实例序列的 {@link List} 的
     * {@link List#hashCode() <tt>hashCode</tt>} 方法所获得的值相同。如果 <tt>a</tt> 为 <tt>null</tt>，此方法返回 0。
     *
     * @param a 要计算哈希值的数组
     * @return 基于内容的 <tt>a</tt> 的哈希码
     * @since 1.5
     */
    public static int hashCode(int a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (int element : a)
            result = 31 * result + element;

        return result;
    }

    /**
     * 基于指定数组的内容返回一个哈希码。对于任何两个 <tt>short</tt> 数组 <tt>a</tt> 和 <tt>b</tt>
     * 如果 <tt>Arrays.equals(a, b)</tt>，那么也有 <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>。
     *
     * <p>此方法返回的值与调用包含表示 <tt>a</tt> 中元素的 {@link Short} 实例序列的 {@link List} 的
     * {@link List#hashCode() <tt>hashCode</tt>} 方法所获得的值相同。如果 <tt>a</tt> 为 <tt>null</tt>，此方法返回 0。
     *
     * @param a 要计算哈希值的数组
     * @return 基于内容的 <tt>a</tt> 的哈希码
     * @since 1.5
     */
    public static int hashCode(short a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (short element : a)
            result = 31 * result + element;

        return result;
    }

    /**
     * 基于指定数组的内容返回一个哈希码。对于任何两个 <tt>char</tt> 数组 <tt>a</tt> 和 <tt>b</tt>
     * 如果 <tt>Arrays.equals(a, b)</tt>，那么也有 <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>。
     *
     * <p>此方法返回的值与调用包含表示 <tt>a</tt> 中元素的 {@link Character} 实例序列的 {@link List} 的
     * {@link List#hashCode() <tt>hashCode</tt>} 方法所获得的值相同。如果 <tt>a</tt> 为 <tt>null</tt>，此方法返回 0。
     *
     * @param a 要计算哈希值的数组
     * @return 基于内容的 <tt>a</tt> 的哈希码
     * @since 1.5
     */
    public static int hashCode(char a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (char element : a)
            result = 31 * result + element;

        return result;
    }

    /**
     * 基于指定数组的内容返回一个哈希码。对于任何两个 <tt>byte</tt> 数组 <tt>a</tt> 和 <tt>b</tt>
     * 如果 <tt>Arrays.equals(a, b)</tt>，那么也有 <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>。
     *
     * <p>此方法返回的值与调用包含表示 <tt>a</tt> 中元素的 {@link Byte} 实例序列的 {@link List} 的
     * {@link List#hashCode() <tt>hashCode</tt>} 方法所获得的值相同。如果 <tt>a</tt> 为 <tt>null</tt>，此方法返回 0。
     *
     * @param a 要计算哈希值的数组
     * @return 基于内容的 <tt>a</tt> 的哈希码
     * @since 1.5
     */
    public static int hashCode(byte a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (byte element : a)
            result = 31 * result + element;

        return result;
    }

    /**
     * 基于指定数组的内容返回一个哈希码。对于任何两个 <tt>boolean</tt> 数组 <tt>a</tt> 和 <tt>b</tt>
     * 如果 <tt>Arrays.equals(a, b)</tt>，那么也有 <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>。
     *
     * <p>此方法返回的值与调用包含表示 <tt>a</tt> 中元素的 {@link Boolean} 实例序列的 {@link List} 的
     * {@link List#hashCode() <tt>hashCode</tt>} 方法所获得的值相同。如果 <tt>a</tt> 为 <tt>null</tt>，此方法返回 0。
     *
     * @param a 要计算哈希值的数组
     * @return 基于内容的 <tt>a</tt> 的哈希码
     * @since 1.5
     */
    public static int hashCode(boolean a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (boolean element : a)
            result = 31 * result + (element ? 1231 : 1237);

        return result;
    }

    /**
     * 基于指定数组的内容返回一个哈希码。对于任何两个 <tt>float</tt> 数组 <tt>a</tt> 和 <tt>b</tt>
     * 如果 <tt>Arrays.equals(a, b)</tt>，那么也有 <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>。
     *
     * <p>此方法返回的值与调用包含表示 <tt>a</tt> 中元素的 {@link Float} 实例序列的 {@link List} 的
     * {@link List#hashCode() <tt>hashCode</tt>} 方法所获得的值相同。如果 <tt>a</tt> 为 <tt>null</tt>，此方法返回 0。
     *
     * @param a 要计算哈希值的数组
     * @return 基于内容的 <tt>a</tt> 的哈希码
     * @since 1.5
     */
    public static int hashCode(float a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (float element : a)
            result = 31 * result + Float.floatToIntBits(element);

        return result;
    }

    /**
     * 基于指定数组的内容返回一个哈希码。对于任何两个 <tt>double</tt> 数组 <tt>a</tt> 和 <tt>b</tt>
     * 如果 <tt>Arrays.equals(a, b)</tt>，那么也有 <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>。
     *
     * <p>此方法返回的值与调用包含表示 <tt>a</tt> 中元素的 {@link Double} 实例序列的 {@link List} 的
     * {@link List#hashCode() <tt>hashCode</tt>} 方法所获得的值相同。如果 <tt>a</tt> 为 <tt>null</tt>，此方法返回 0。
     *
     * @param a 要计算哈希值的数组
     * @return 基于内容的 <tt>a</tt> 的哈希码
     * @since 1.5
     */
    public static int hashCode(double a[]) {
        if (a == null)
            return 0;

        int result = 1;
        for (double element : a) {
            long bits = Double.doubleToLongBits(element);
            result = 31 * result + (int)(bits ^ (bits >>> 32));
        }
        return result;
    }

    /**
     * 基于指定数组的内容返回一个哈希码。如果数组包含其他数组作为元素，哈希码是基于它们的身份而不是内容。因此，调用此方法于包含自身作为元素的数组是可接受的，
     * 无论是直接还是通过一个或多个级别的数组间接包含。
     *
     * <p>对于任何两个数组 <tt>a</tt> 和 <tt>b</tt>，如果 <tt>Arrays.equals(a, b)</tt>，那么也有 <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>。
     *
     * <p>此方法返回的值等于调用 <tt>Arrays.asList(a).hashCode()</tt> 所返回的值，除非 <tt>a</tt> 为 <tt>null</tt>，在这种情况下返回 <tt>0</tt>。
     *
     * @param a 要计算基于内容的哈希码的数组
     * @return 基于内容的 <tt>a</tt> 的哈希码
     * @see #deepHashCode(Object[])
     * @since 1.5
     */
    public static int hashCode(Object a[]) {
        if (a == null)
            return 0;

        int result = 1;

        for (Object element : a)
            result = 31 * result + (element == null ? 0 : element.hashCode());

        return result;
    }

    /**
     * 基于指定数组的“深层内容”返回一个哈希码。如果数组包含其他数组作为元素，哈希码是基于它们的内容，依此类推，直到无穷大。
     * 因此，调用此方法于包含自身作为元素的数组是不可接受的，无论是直接还是通过一个或多个级别的数组间接包含。此类调用的行为是未定义的。
     *
     * <p>对于任何两个数组 <tt>a</tt> 和 <tt>b</tt>，如果 <tt>Arrays.deepEquals(a, b)</tt>，那么也有 <tt>Arrays.deepHashCode(a) == Arrays.deepHashCode(b)</tt>。
     *
     * <p>此方法返回的值的计算类似于调用包含与 <tt>a</tt> 中元素相同且顺序相同的列表的 {@link List#hashCode()} 方法所返回的值，但有一个不同点：
     * 如果 <tt>a</tt> 的元素 <tt>e</tt> 本身是一个数组，其哈希码不是通过调用 <tt>e.hashCode()</tt> 计算的，而是通过调用适当的 <tt>Arrays.hashCode(e)</tt> 重载
     * （如果 <tt>e</tt> 是一个基本类型的数组）或通过递归调用 <tt>Arrays.deepHashCode(e)</tt>（如果 <tt>e</tt> 是一个引用类型的数组）计算的。
     * 如果 <tt>a</tt> 为 <tt>null</tt>，此方法返回 0。
     *
     * @param a 要计算基于深层内容的哈希码的数组
     * @return 基于深层内容的 <tt>a</tt> 的哈希码
     * @see #hashCode(Object[])
     * @since 1.5
     */
    public static int deepHashCode(Object a[]) {
        if (a == null)
            return 0;

        int result = 1;

        for (Object element : a) {
            int elementHash = 0;
            if (element instanceof Object[])
                elementHash = deepHashCode((Object[]) element);
            else if (element instanceof byte[])
                elementHash = hashCode((byte[]) element);
            else if (element instanceof short[])
                elementHash = hashCode((short[]) element);
            else if (element instanceof int[])
                elementHash = hashCode((int[]) element);
            else if (element instanceof long[])
                elementHash = hashCode((long[]) element);
            else if (element instanceof char[])
                elementHash = hashCode((char[]) element);
            else if (element instanceof float[])
                elementHash = hashCode((float[]) element);
            else if (element instanceof double[])
                elementHash = hashCode((double[]) element);
            else if (element instanceof boolean[])
                elementHash = hashCode((boolean[]) element);
            else if (element != null)
                elementHash = element.hashCode();


                        result = 31 * result + elementHash;
        }

        return result;
    }

    /**
     * 返回两个指定数组是否 <i>深度相等</i>。与 {@link #equals(Object[],Object[])}
     * 方法不同，此方法适用于任意深度的嵌套数组。
     *
     * <p>如果两个数组引用都为 <tt>null</tt>，或者它们引用的数组包含相同数量的元素且所有对应元素对在两个数组中都是深度相等的，
     * 则认为这两个数组引用是深度相等的。
     *
     * <p>两个可能为 <tt>null</tt> 的元素 <tt>e1</tt> 和 <tt>e2</tt> 是深度相等的，如果满足以下任一条件：
     * <ul>
     *    <li> <tt>e1</tt> 和 <tt>e2</tt> 都是对象引用类型的数组，且 <tt>Arrays.deepEquals(e1, e2) 会返回 true</tt>
     *    <li> <tt>e1</tt> 和 <tt>e2</tt> 是相同原始类型的数组，且适当的 <tt>Arrays.equals(e1, e2)</tt> 重载会返回 true。
     *    <li> <tt>e1 == e2</tt>
     *    <li> <tt>e1.equals(e2)</tt> 会返回 true。
     * </ul>
     * 注意，此定义允许在任何深度上存在 <tt>null</tt> 元素。
     *
     * <p>如果指定的任一数组直接或通过一个或多个级别的数组间接包含自身作为元素，则此方法的行为是未定义的。
     *
     * @param a1 一个要测试相等性的数组
     * @param a2 另一个要测试相等性的数组
     * @return 如果两个数组相等，则返回 <tt>true</tt>
     * @see #equals(Object[],Object[])
     * @see Objects#deepEquals(Object, Object)
     * @since 1.5
     */
    public static boolean deepEquals(Object[] a1, Object[] a2) {
        if (a1 == a2)
            return true;
        if (a1 == null || a2==null)
            return false;
        int length = a1.length;
        if (a2.length != length)
            return false;

        for (int i = 0; i < length; i++) {
            Object e1 = a1[i];
            Object e2 = a2[i];

            if (e1 == e2)
                continue;
            if (e1 == null)
                return false;

            // 确定两个元素是否相等
            boolean eq = deepEquals0(e1, e2);

            if (!eq)
                return false;
        }
        return true;
    }

    static boolean deepEquals0(Object e1, Object e2) {
        assert e1 != null;
        boolean eq;
        if (e1 instanceof Object[] && e2 instanceof Object[])
            eq = deepEquals ((Object[]) e1, (Object[]) e2);
        else if (e1 instanceof byte[] && e2 instanceof byte[])
            eq = equals((byte[]) e1, (byte[]) e2);
        else if (e1 instanceof short[] && e2 instanceof short[])
            eq = equals((short[]) e1, (short[]) e2);
        else if (e1 instanceof int[] && e2 instanceof int[])
            eq = equals((int[]) e1, (int[]) e2);
        else if (e1 instanceof long[] && e2 instanceof long[])
            eq = equals((long[]) e1, (long[]) e2);
        else if (e1 instanceof char[] && e2 instanceof char[])
            eq = equals((char[]) e1, (char[]) e2);
        else if (e1 instanceof float[] && e2 instanceof float[])
            eq = equals((float[]) e1, (float[]) e2);
        else if (e1 instanceof double[] && e2 instanceof double[])
            eq = equals((double[]) e1, (double[]) e2);
        else if (e1 instanceof boolean[] && e2 instanceof boolean[])
            eq = equals((boolean[]) e1, (boolean[]) e2);
        else
            eq = e1.equals(e2);
        return eq;
    }

    /**
     * 返回指定数组内容的字符串表示形式。字符串表示形式由数组的元素列表组成，用方括号 (<tt>"[]"</tt>) 包围。
     * 相邻元素由字符 <tt>", "</tt>（逗号后跟一个空格）分隔。元素转换为字符串的方式与 <tt>String.valueOf(long)</tt> 相同。
     * 如果 <tt>a</tt> 为 <tt>null</tt>，则返回 <tt>"null"</tt>。
     *
     * @param a 要返回其字符串表示形式的数组
     * @return <tt>a</tt> 的字符串表示形式
     * @since 1.5
     */
    public static String toString(long[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * 返回指定数组内容的字符串表示形式。字符串表示形式由数组的元素列表组成，用方括号 (<tt>"[]"</tt>) 包围。
     * 相邻元素由字符 <tt>", "</tt>（逗号后跟一个空格）分隔。元素转换为字符串的方式与 <tt>String.valueOf(int)</tt> 相同。
     * 如果 <tt>a</tt> 为 <tt>null</tt>，则返回 <tt>"null"</tt>。
     *
     * @param a 要返回其字符串表示形式的数组
     * @return <tt>a</tt> 的字符串表示形式
     * @since 1.5
     */
    public static String toString(int[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * 返回指定数组内容的字符串表示形式。字符串表示形式由数组的元素列表组成，用方括号 (<tt>"[]"</tt>) 包围。
     * 相邻元素由字符 <tt>", "</tt>（逗号后跟一个空格）分隔。元素转换为字符串的方式与 <tt>String.valueOf(short)</tt> 相同。
     * 如果 <tt>a</tt> 为 <tt>null</tt>，则返回 <tt>"null"</tt>。
     *
     * @param a 要返回其字符串表示形式的数组
     * @return <tt>a</tt> 的字符串表示形式
     * @since 1.5
     */
    public static String toString(short[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * 返回指定数组内容的字符串表示形式。字符串表示形式由数组的元素列表组成，用方括号 (<tt>"[]"</tt>) 包围。
     * 相邻元素由字符 <tt>", "</tt>（逗号后跟一个空格）分隔。元素转换为字符串的方式与 <tt>String.valueOf(char)</tt> 相同。
     * 如果 <tt>a</tt> 为 <tt>null</tt>，则返回 <tt>"null"</tt>。
     *
     * @param a 要返回其字符串表示形式的数组
     * @return <tt>a</tt> 的字符串表示形式
     * @since 1.5
     */
    public static String toString(char[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * 返回指定数组内容的字符串表示形式。字符串表示形式由数组的元素列表组成，用方括号 (<tt>"[]"</tt>) 包围。
     * 相邻元素由字符 <tt>", "</tt>（逗号后跟一个空格）分隔。元素转换为字符串的方式与 <tt>String.valueOf(byte)</tt> 相同。
     * 如果 <tt>a</tt> 为 <tt>null</tt>，则返回 <tt>"null"</tt>。
     *
     * @param a 要返回其字符串表示形式的数组
     * @return <tt>a</tt> 的字符串表示形式
     * @since 1.5
     */
    public static String toString(byte[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * 返回指定数组内容的字符串表示形式。字符串表示形式由数组的元素列表组成，用方括号 (<tt>"[]"</tt>) 包围。
     * 相邻元素由字符 <tt>", "</tt>（逗号后跟一个空格）分隔。元素转换为字符串的方式与 <tt>String.valueOf(boolean)</tt> 相同。
     * 如果 <tt>a</tt> 为 <tt>null</tt>，则返回 <tt>"null"</tt>。
     *
     * @param a 要返回其字符串表示形式的数组
     * @return <tt>a</tt> 的字符串表示形式
     * @since 1.5
     */
    public static String toString(boolean[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * 返回指定数组内容的字符串表示形式。字符串表示形式由数组的元素列表组成，用方括号 (<tt>"[]"</tt>) 包围。
     * 相邻元素由字符 <tt>", "</tt>（逗号后跟一个空格）分隔。元素转换为字符串的方式与 <tt>String.valueOf(float)</tt> 相同。
     * 如果 <tt>a</tt> 为 <tt>null</tt>，则返回 <tt>"null"</tt>。
     *
     * @param a 要返回其字符串表示形式的数组
     * @return <tt>a</tt> 的字符串表示形式
     * @since 1.5
     */
    public static String toString(float[] a) {
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * 返回指定数组内容的字符串表示形式。字符串表示形式由数组的元素列表组成，用方括号 (<tt>"[]"</tt>) 包围。
     * 相邻元素由字符 <tt>", "</tt>（逗号后跟一个空格）分隔。元素转换为字符串的方式与 <tt>String.valueOf(double)</tt> 相同。
     * 如果 <tt>a</tt> 为 <tt>null</tt>，则返回 <tt>"null"</tt>。
     *
     * @param a 要返回其字符串表示形式的数组
     * @return <tt>a</tt> 的字符串表示形式
     * @since 1.5
     */
    public static String toString(double[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * 返回指定数组内容的字符串表示形式。如果数组包含其他数组作为元素，这些元素将通过继承自 <tt>Object</tt> 的
     * {@link Object#toString} 方法转换为字符串，该方法描述的是它们的 <i>身份</i> 而不是内容。
     *
     * <p>此方法返回的值等于 <tt>Arrays.asList(a).toString()</tt> 返回的值，除非 <tt>a</tt> 为 <tt>null</tt>，
     * 在这种情况下返回 <tt>"null"</tt>。
     *
     * @param a 要返回其字符串表示形式的数组
     * @return <tt>a</tt> 的字符串表示形式
     * @see #deepToString(Object[])
     * @since 1.5
     */
    public static String toString(Object[] a) {
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(String.valueOf(a[i]));
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    /**
     * 返回指定数组的“深度内容”的字符串表示形式。如果数组的元素是其他数组，字符串表示形式将包含这些数组的内容，依此类推。
     * 此方法设计用于将多维数组转换为字符串。
     *
     * <p>字符串表示形式由数组的元素列表组成，用方括号 (<tt>"[]"</tt>) 包围。相邻元素由字符 <tt>", "</tt>（逗号后跟一个空格）分隔。
     * 元素转换为字符串的方式与 <tt>String.valueOf(Object)</tt> 相同，除非它们本身就是数组。
     *
     * <p>如果元素 <tt>e</tt> 是原始类型的数组，它将通过调用适当的 <tt>Arrays.toString(e)</tt> 重载转换为字符串。
     * 如果元素 <tt>e</tt> 是引用类型的数组，它将通过递归调用此方法转换为字符串。
     *
     * <p>为了避免无限递归，如果指定的数组直接或通过一个或多个级别的数组间接包含自身作为元素，自引用将转换为字符串 <tt>"[...]"</tt>。
     * 例如，仅包含自身引用的数组将呈现为 <tt>"[[...]]"</tt>。
     *
     * <p>如果指定的数组为 <tt>null</tt>，此方法返回 <tt>"null"</tt>。
     *
     * @param a 要返回其字符串表示形式的数组
     * @return <tt>a</tt> 的字符串表示形式
     * @see #toString(Object[])
     * @since 1.5
     */
    public static String deepToString(Object[] a) {
        if (a == null)
            return "null";

        int bufLen = 20 * a.length;
        if (a.length != 0 && bufLen <= 0)
            bufLen = Integer.MAX_VALUE;
        StringBuilder buf = new StringBuilder(bufLen);
        deepToString(a, buf, new HashSet<Object[]>());
        return buf.toString();
    }

    private static void deepToString(Object[] a, StringBuilder buf,
                                     Set<Object[]> dejaVu) {
        if (a == null) {
            buf.append("null");
            return;
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            buf.append("[]");
            return;
        }


                    dejaVu.add(a);
        buf.append('[');
        for (int i = 0; ; i++) {

            Object element = a[i];
            if (element == null) {
                buf.append("null");
            } else {
                Class<?> eClass = element.getClass();

                if (eClass.isArray()) {
                    if (eClass == byte[].class)
                        buf.append(toString((byte[]) element));
                    else if (eClass == short[].class)
                        buf.append(toString((short[]) element));
                    else if (eClass == int[].class)
                        buf.append(toString((int[]) element));
                    else if (eClass == long[].class)
                        buf.append(toString((long[]) element));
                    else if (eClass == char[].class)
                        buf.append(toString((char[]) element));
                    else if (eClass == float[].class)
                        buf.append(toString((float[]) element));
                    else if (eClass == double[].class)
                        buf.append(toString((double[]) element));
                    else if (eClass == boolean[].class)
                        buf.append(toString((boolean[]) element));
                    else { // element is an array of object references
                        if (dejaVu.contains(element))
                            buf.append("[...]");
                        else
                            deepToString((Object[])element, buf, dejaVu);
                    }
                } else {  // element is non-null and not an array
                    buf.append(element.toString());
                }
            }
            if (i == iMax)
                break;
            buf.append(", ");
        }
        buf.append(']');
        dejaVu.remove(a);
    }


    /**
     * 使用提供的生成器函数计算每个元素，初始化指定数组的所有元素。
     *
     * <p>如果生成器函数抛出异常，异常将传递给调用者，数组将处于不确定状态。
     *
     * @param <T> 数组元素的类型
     * @param array 要初始化的数组
     * @param generator 接受索引并生成该位置所需值的函数
     * @throws NullPointerException 如果生成器为 null
     * @since 1.8
     */
    public static <T> void setAll(T[] array, IntFunction<? extends T> generator) {
        Objects.requireNonNull(generator);
        for (int i = 0; i < array.length; i++)
            array[i] = generator.apply(i);
    }

    /**
     * 使用提供的生成器函数并行计算每个元素，初始化指定数组的所有元素。
     *
     * <p>如果生成器函数抛出异常，将从 {@code parallelSetAll} 抛出未检查异常，数组将处于不确定状态。
     *
     * @param <T> 数组元素的类型
     * @param array 要初始化的数组
     * @param generator 接受索引并生成该位置所需值的函数
     * @throws NullPointerException 如果生成器为 null
     * @since 1.8
     */
    public static <T> void parallelSetAll(T[] array, IntFunction<? extends T> generator) {
        Objects.requireNonNull(generator);
        IntStream.range(0, array.length).parallel().forEach(i -> { array[i] = generator.apply(i); });
    }

    /**
     * 使用提供的生成器函数计算每个元素，初始化指定数组的所有元素。
     *
     * <p>如果生成器函数抛出异常，异常将传递给调用者，数组将处于不确定状态。
     *
     * @param array 要初始化的数组
     * @param generator 接受索引并生成该位置所需值的函数
     * @throws NullPointerException 如果生成器为 null
     * @since 1.8
     */
    public static void setAll(int[] array, IntUnaryOperator generator) {
        Objects.requireNonNull(generator);
        for (int i = 0; i < array.length; i++)
            array[i] = generator.applyAsInt(i);
    }

    /**
     * 使用提供的生成器函数并行计算每个元素，初始化指定数组的所有元素。
     *
     * <p>如果生成器函数抛出异常，将从 {@code parallelSetAll} 抛出未检查异常，数组将处于不确定状态。
     *
     * @param array 要初始化的数组
     * @param generator 接受索引并生成该位置所需值的函数
     * @throws NullPointerException 如果生成器为 null
     * @since 1.8
     */
    public static void parallelSetAll(int[] array, IntUnaryOperator generator) {
        Objects.requireNonNull(generator);
        IntStream.range(0, array.length).parallel().forEach(i -> { array[i] = generator.applyAsInt(i); });
    }

    /**
     * 使用提供的生成器函数计算每个元素，初始化指定数组的所有元素。
     *
     * <p>如果生成器函数抛出异常，异常将传递给调用者，数组将处于不确定状态。
     *
     * @param array 要初始化的数组
     * @param generator 接受索引并生成该位置所需值的函数
     * @throws NullPointerException 如果生成器为 null
     * @since 1.8
     */
    public static void setAll(long[] array, IntToLongFunction generator) {
        Objects.requireNonNull(generator);
        for (int i = 0; i < array.length; i++)
            array[i] = generator.applyAsLong(i);
    }

    /**
     * 使用提供的生成器函数并行计算每个元素，初始化指定数组的所有元素。
     *
     * <p>如果生成器函数抛出异常，将从 {@code parallelSetAll} 抛出未检查异常，数组将处于不确定状态。
     *
     * @param array 要初始化的数组
     * @param generator 接受索引并生成该位置所需值的函数
     * @throws NullPointerException 如果生成器为 null
     * @since 1.8
     */
    public static void parallelSetAll(long[] array, IntToLongFunction generator) {
        Objects.requireNonNull(generator);
        IntStream.range(0, array.length).parallel().forEach(i -> { array[i] = generator.applyAsLong(i); });
    }

    /**
     * 使用提供的生成器函数计算每个元素，初始化指定数组的所有元素。
     *
     * <p>如果生成器函数抛出异常，异常将传递给调用者，数组将处于不确定状态。
     *
     * @param array 要初始化的数组
     * @param generator 接受索引并生成该位置所需值的函数
     * @throws NullPointerException 如果生成器为 null
     * @since 1.8
     */
    public static void setAll(double[] array, IntToDoubleFunction generator) {
        Objects.requireNonNull(generator);
        for (int i = 0; i < array.length; i++)
            array[i] = generator.applyAsDouble(i);
    }

    /**
     * 使用提供的生成器函数并行计算每个元素，初始化指定数组的所有元素。
     *
     * <p>如果生成器函数抛出异常，将从 {@code parallelSetAll} 抛出未检查异常，数组将处于不确定状态。
     *
     * @param array 要初始化的数组
     * @param generator 接受索引并生成该位置所需值的函数
     * @throws NullPointerException 如果生成器为 null
     * @since 1.8
     */
    public static void parallelSetAll(double[] array, IntToDoubleFunction generator) {
        Objects.requireNonNull(generator);
        IntStream.range(0, array.length).parallel().forEach(i -> { array[i] = generator.applyAsDouble(i); });
    }

    /**
     * 返回覆盖指定数组所有元素的 {@link Spliterator}。
     *
     * <p>该迭代器报告 {@link Spliterator#SIZED}、
     * {@link Spliterator#SUBSIZED}、{@link Spliterator#ORDERED} 和
     * {@link Spliterator#IMMUTABLE}。
     *
     * @param <T> 元素类型
     * @param array 假定在使用期间不会修改的数组
     * @return 数组元素的迭代器
     * @since 1.8
     */
    public static <T> Spliterator<T> spliterator(T[] array) {
        return Spliterators.spliterator(array,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * 返回覆盖指定数组指定范围的 {@link Spliterator}。
     *
     * <p>该迭代器报告 {@link Spliterator#SIZED}、
     * {@link Spliterator#SUBSIZED}、{@link Spliterator#ORDERED} 和
     * {@link Spliterator#IMMUTABLE}。
     *
     * @param <T> 元素类型
     * @param array 假定在使用期间不会修改的数组
     * @param startInclusive 覆盖的起始索引（包含）
     * @param endExclusive 覆盖的结束索引（不包含）
     * @return 数组元素的迭代器
     * @throws ArrayIndexOutOfBoundsException 如果 {@code startInclusive} 为负数，
     *         {@code endExclusive} 小于 {@code startInclusive}，或 {@code endExclusive} 大于数组大小
     * @since 1.8
     */
    public static <T> Spliterator<T> spliterator(T[] array, int startInclusive, int endExclusive) {
        return Spliterators.spliterator(array, startInclusive, endExclusive,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * 返回覆盖指定数组所有元素的 {@link Spliterator.OfInt}。
     *
     * <p>该迭代器报告 {@link Spliterator#SIZED}、
     * {@link Spliterator#SUBSIZED}、{@link Spliterator#ORDERED} 和
     * {@link Spliterator#IMMUTABLE}。
     *
     * @param array 假定在使用期间不会修改的数组
     * @return 数组元素的迭代器
     * @since 1.8
     */
    public static Spliterator.OfInt spliterator(int[] array) {
        return Spliterators.spliterator(array,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * 返回覆盖指定数组指定范围的 {@link Spliterator.OfInt}。
     *
     * <p>该迭代器报告 {@link Spliterator#SIZED}、
     * {@link Spliterator#SUBSIZED}、{@link Spliterator#ORDERED} 和
     * {@link Spliterator#IMMUTABLE}。
     *
     * @param array 假定在使用期间不会修改的数组
     * @param startInclusive 覆盖的起始索引（包含）
     * @param endExclusive 覆盖的结束索引（不包含）
     * @return 数组元素的迭代器
     * @throws ArrayIndexOutOfBoundsException 如果 {@code startInclusive} 为负数，
     *         {@code endExclusive} 小于 {@code startInclusive}，或 {@code endExclusive} 大于数组大小
     * @since 1.8
     */
    public static Spliterator.OfInt spliterator(int[] array, int startInclusive, int endExclusive) {
        return Spliterators.spliterator(array, startInclusive, endExclusive,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * 返回覆盖指定数组所有元素的 {@link Spliterator.OfLong}。
     *
     * <p>该迭代器报告 {@link Spliterator#SIZED}、
     * {@link Spliterator#SUBSIZED}、{@link Spliterator#ORDERED} 和
     * {@link Spliterator#IMMUTABLE}。
     *
     * @param array 假定在使用期间不会修改的数组
     * @return 数组元素的迭代器
     * @since 1.8
     */
    public static Spliterator.OfLong spliterator(long[] array) {
        return Spliterators.spliterator(array,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * 返回覆盖指定数组指定范围的 {@link Spliterator.OfLong}。
     *
     * <p>该迭代器报告 {@link Spliterator#SIZED}、
     * {@link Spliterator#SUBSIZED}、{@link Spliterator#ORDERED} 和
     * {@link Spliterator#IMMUTABLE}。
     *
     * @param array 假定在使用期间不会修改的数组
     * @param startInclusive 覆盖的起始索引（包含）
     * @param endExclusive 覆盖的结束索引（不包含）
     * @return 数组元素的迭代器
     * @throws ArrayIndexOutOfBoundsException 如果 {@code startInclusive} 为负数，
     *         {@code endExclusive} 小于 {@code startInclusive}，或 {@code endExclusive} 大于数组大小
     * @since 1.8
     */
    public static Spliterator.OfLong spliterator(long[] array, int startInclusive, int endExclusive) {
        return Spliterators.spliterator(array, startInclusive, endExclusive,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * 返回覆盖指定数组所有元素的 {@link Spliterator.OfDouble}。
     *
     * <p>该迭代器报告 {@link Spliterator#SIZED}、
     * {@link Spliterator#SUBSIZED}、{@link Spliterator#ORDERED} 和
     * {@link Spliterator#IMMUTABLE}。
     *
     * @param array 假定在使用期间不会修改的数组
     * @return 数组元素的迭代器
     * @since 1.8
     */
    public static Spliterator.OfDouble spliterator(double[] array) {
        return Spliterators.spliterator(array,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * 返回覆盖指定数组指定范围的 {@link Spliterator.OfDouble}。
     *
     * <p>该迭代器报告 {@link Spliterator#SIZED}、
     * {@link Spliterator#SUBSIZED}、{@link Spliterator#ORDERED} 和
     * {@link Spliterator#IMMUTABLE}。
     *
     * @param array 假定在使用期间不会修改的数组
     * @param startInclusive 覆盖的起始索引（包含）
     * @param endExclusive 覆盖的结束索引（不包含）
     * @return 数组元素的迭代器
     * @throws ArrayIndexOutOfBoundsException 如果 {@code startInclusive} 为负数，
     *         {@code endExclusive} 小于 {@code startInclusive}，或 {@code endExclusive} 大于数组大小
     * @since 1.8
     */
    public static Spliterator.OfDouble spliterator(double[] array, int startInclusive, int endExclusive) {
        return Spliterators.spliterator(array, startInclusive, endExclusive,
                                        Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * 返回以指定数组为源的顺序 {@link Stream}。
     *
     * @param <T> 数组元素的类型
     * @param array 假定在使用期间不会修改的数组
     * @return 数组的 {@code Stream}
     * @since 1.8
     */
    public static <T> Stream<T> stream(T[] array) {
        return stream(array, 0, array.length);
    }


                /**
     * 返回一个以指定数组的指定范围作为源的顺序 {@link Stream}。
     *
     * @param <T> 数组元素的类型
     * @param array 假定在使用期间不会被修改的数组
     * @param startInclusive 覆盖的第一个索引，包含
     * @param endExclusive 立即超过最后一个覆盖索引的索引
     * @return 用于数组范围的 {@code Stream}
     * @throws ArrayIndexOutOfBoundsException 如果 {@code startInclusive} 为负数，{@code endExclusive} 小于
     *         {@code startInclusive}，或 {@code endExclusive} 大于
     *         数组大小
     * @since 1.8
     */
    public static <T> Stream<T> stream(T[] array, int startInclusive, int endExclusive) {
        return StreamSupport.stream(spliterator(array, startInclusive, endExclusive), false);
    }

    /**
     * 返回一个以指定数组作为源的顺序 {@link IntStream}。
     *
     * @param array 假定在使用期间不会被修改的数组
     * @return 用于数组的 {@code IntStream}
     * @since 1.8
     */
    public static IntStream stream(int[] array) {
        return stream(array, 0, array.length);
    }

    /**
     * 返回一个以指定数组的指定范围作为源的顺序 {@link IntStream}。
     *
     * @param array 假定在使用期间不会被修改的数组
     * @param startInclusive 覆盖的第一个索引，包含
     * @param endExclusive 立即超过最后一个覆盖索引的索引
     * @return 用于数组范围的 {@code IntStream}
     * @throws ArrayIndexOutOfBoundsException 如果 {@code startInclusive} 为负数，{@code endExclusive} 小于
     *         {@code startInclusive}，或 {@code endExclusive} 大于
     *         数组大小
     * @since 1.8
     */
    public static IntStream stream(int[] array, int startInclusive, int endExclusive) {
        return StreamSupport.intStream(spliterator(array, startInclusive, endExclusive), false);
    }

    /**
     * 返回一个以指定数组作为源的顺序 {@link LongStream}。
     *
     * @param array 假定在使用期间不会被修改的数组
     * @return 用于数组的 {@code LongStream}
     * @since 1.8
     */
    public static LongStream stream(long[] array) {
        return stream(array, 0, array.length);
    }

    /**
     * 返回一个以指定数组的指定范围作为源的顺序 {@link LongStream}。
     *
     * @param array 假定在使用期间不会被修改的数组
     * @param startInclusive 覆盖的第一个索引，包含
     * @param endExclusive 立即超过最后一个覆盖索引的索引
     * @return 用于数组范围的 {@code LongStream}
     * @throws ArrayIndexOutOfBoundsException 如果 {@code startInclusive} 为负数，{@code endExclusive} 小于
     *         {@code startInclusive}，或 {@code endExclusive} 大于
     *         数组大小
     * @since 1.8
     */
    public static LongStream stream(long[] array, int startInclusive, int endExclusive) {
        return StreamSupport.longStream(spliterator(array, startInclusive, endExclusive), false);
    }

    /**
     * 返回一个以指定数组作为源的顺序 {@link DoubleStream}。
     *
     * @param array 假定在使用期间不会被修改的数组
     * @return 用于数组的 {@code DoubleStream}
     * @since 1.8
     */
    public static DoubleStream stream(double[] array) {
        return stream(array, 0, array.length);
    }

    /**
     * 返回一个以指定数组的指定范围作为源的顺序 {@link DoubleStream}。
     *
     * @param array 假定在使用期间不会被修改的数组
     * @param startInclusive 覆盖的第一个索引，包含
     * @param endExclusive 立即超过最后一个覆盖索引的索引
     * @return 用于数组范围的 {@code DoubleStream}
     * @throws ArrayIndexOutOfBoundsException 如果 {@code startInclusive} 为负数，{@code endExclusive} 小于
     *         {@code startInclusive}，或 {@code endExclusive} 大于
     *         数组大小
     * @since 1.8
     */
    public static DoubleStream stream(double[] array, int startInclusive, int endExclusive) {
        return StreamSupport.doubleStream(spliterator(array, startInclusive, endExclusive), false);
    }
}
