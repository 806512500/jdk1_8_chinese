
/*
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 该类实现了由 Vladimir Yaroslavskiy, Jon Bentley 和 Josh Bloch 提出的双轴快速排序算法。该算法在许多数据集上提供 O(n log(n)) 的性能，而其他快速排序算法在这些数据集上会退化为二次性能，并且通常比传统的（单轴）快速排序实现更快。
 *
 * 所有公开的方法都是包私有的，设计为从公共方法（在 Arrays 类中）调用，执行必要的数组边界检查并将参数扩展为所需的格式。
 *
 * @author Vladimir Yaroslavskiy
 * @author Jon Bentley
 * @author Josh Bloch
 *
 * @version 2011.02.11 m765.827.12i:5\7pm
 * @since 1.7
 */
final class DualPivotQuicksort {

    /**
     * 防止实例化。
     */
    private DualPivotQuicksort() {}

    /*
     * 调优参数。
     */

    /**
     * 合并排序中运行的最大数量。
     */
    private static final int MAX_RUN_COUNT = 67;

    /**
     * 合并排序中运行的最大长度。
     */
    private static final int MAX_RUN_LENGTH = 33;

    /**
     * 如果要排序的数组长度小于此常量，则优先使用快速排序而不是合并排序。
     */
    private static final int QUICKSORT_THRESHOLD = 286;

    /**
     * 如果要排序的数组长度小于此常量，则优先使用插入排序而不是快速排序。
     */
    private static final int INSERTION_SORT_THRESHOLD = 47;

    /**
     * 如果要排序的字节数组长度大于此常量，则优先使用计数排序而不是插入排序。
     */
    private static final int COUNTING_SORT_THRESHOLD_FOR_BYTE = 29;

    /**
     * 如果要排序的短整型或字符数组长度大于此常量，则优先使用计数排序而不是快速排序。
     */
    private static final int COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR = 3200;

    /*
     * 七种基本类型的排序方法。
     */

    /**
     * 使用给定的工作空间数组切片（如果可能）对指定范围的数组进行排序以进行合并。
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param work 工作空间数组（切片）
     * @param workBase 工作数组中可用空间的起始位置
     * @param workLen 工作数组的可用大小
     */
    static void sort(int[] a, int left, int right,
                     int[] work, int workBase, int workLen) {
        // 对小数组使用快速排序
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }

        /*
         * 索引 run[i] 是第 i 个运行的起始位置
         * （升序或降序序列）。
         */
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0; run[0] = left;

        // 检查数组是否几乎已排序
        for (int k = left; k < right; run[count] = k) {
            if (a[k] < a[k + 1]) { // 升序
                while (++k <= right && a[k - 1] <= a[k]);
            } else if (a[k] > a[k + 1]) { // 降序
                while (++k <= right && a[k - 1] >= a[k]);
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    int t = a[lo]; a[lo] = a[hi]; a[hi] = t;
                }
            } else { // 相等
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    if (--m == 0) {
                        sort(a, left, right, true);
                        return;
                    }
                }
            }

            /*
             * 数组不是高度结构化的，
             * 使用快速排序而不是合并排序。
             */
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }

        // 检查特殊情况
        // 实现注释：变量 "right" 增加了 1。
        if (run[count] == right++) { // 最后一个运行包含一个元素
            run[++count] = right;
        } else if (count == 1) { // 数组已经排序
            return;
        }

        // 确定合并的交替基
        byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1);

        // 使用或创建临时数组 b 进行合并
        int[] b;                 // 临时数组；与 a 交替
        int ao, bo;              // 从 'left' 开始的数组偏移量
        int blen = right - left; // b 所需的空间
        if (work == null || workLen < blen || workBase + blen > work.length) {
            work = new int[blen];
            workBase = 0;
        }
        if (odd == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }

        // 合并
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
                        b[i + bo] = a[p++ + ao];
                    } else {
                        b[i + bo] = a[q++ + ao];
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                    b[i + bo] = a[i + ao]
                );
                run[++last] = right;
            }
            int[] t = a; a = b; b = t;
            int o = ao; ao = bo; bo = o;
        }
    }

    /**
     * 使用双轴快速排序对指定范围的数组进行排序。
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param leftmost 表示此部分是否是范围的最左部分
     */
    private static void sort(int[] a, int left, int right, boolean leftmost) {
        int length = right - left + 1;

        // 对小数组使用插入排序
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                /*
                 * 传统的（没有哨兵）插入排序，
                 * 优化用于服务器 VM，在最左部分的情况下使用。
                 */
                for (int i = left, j = i; i < right; j = ++i) {
                    int ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        if (j-- == left) {
                            break;
                        }
                    }
                    a[j + 1] = ai;
                }
            } else {
                /*
                 * 跳过最长的升序序列。
                 */
                do {
                    if (left >= right) {
                        return;
                    }
                } while (a[++left] >= a[left - 1]);

                /*
                 * 每个来自相邻部分的元素都充当哨兵，因此这允许我们在每次迭代中避免左范围检查。此外，我们使用了更优化的算法，即所谓的成对插入排序，这在快速排序的上下文中比传统的插入排序实现更快。
                 */
                for (int k = left; ++left <= right; k = ++left) {
                    int a1 = a[k], a2 = a[left];

                    if (a1 < a2) {
                        a2 = a1; a1 = a[left];
                    }
                    while (a1 < a[--k]) {
                        a[k + 2] = a[k];
                    }
                    a[++k + 1] = a1;

                    while (a2 < a[--k]) {
                        a[k + 1] = a[k];
                    }
                    a[k + 1] = a2;
                }
                int last = a[right];

                while (last < a[--right]) {
                    a[right + 1] = a[right];
                }
                a[right + 1] = last;
            }
            return;
        }

        // 便宜的 length / 7 近似值
        int seventh = (length >> 3) + (length >> 6) + 1;

        /*
         * 对范围内的五个等间距元素（包括中心元素）进行排序。这些元素将用于枢轴选择，如下所述。这些元素的间距选择是通过经验确定的，适用于各种输入。
         */
        int e3 = (left + right) >>> 1; // 中点
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;

        // 使用插入排序对这些元素进行排序
        if (a[e2] < a[e1]) { int t = a[e2]; a[e2] = a[e1]; a[e1] = t; }

        if (a[e3] < a[e2]) { int t = a[e3]; a[e3] = a[e2]; a[e2] = t;
            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
        }
        if (a[e4] < a[e3]) { int t = a[e4]; a[e4] = a[e3]; a[e3] = t;
            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
            }
        }
        if (a[e5] < a[e4]) { int t = a[e5]; a[e5] = a[e4]; a[e4] = t;
            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                }
            }
        }

        // 指针
        int less  = left;  // 中心部分的第一个元素的索引
        int great = right; // 右部分的第一个元素前的索引

        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            /*
             * 使用五个排序元素中的第二个和第四个作为枢轴。
             * 这些值是数组第一和第二三分位的廉价近似值。注意 pivot1 <= pivot2。
             */
            int pivot1 = a[e2];
            int pivot2 = a[e4];

            /*
             * 第一个和最后一个要排序的元素被移动到枢轴先前占据的位置。当分区完成时，枢轴被交换回它们的最终位置，并从后续排序中排除。
             */
            a[e2] = a[left];
            a[e4] = a[right];

            /*
             * 跳过小于或大于枢轴值的元素。
             */
            while (a[++less] < pivot1);
            while (a[--great] > pivot2);

            /*
             * 分区：
             *
             *   左部分           中心部分                   右部分
             * +--------------------------------------------------------------+
             * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
             * +--------------------------------------------------------------+
             *               ^                          ^       ^
             *               |                          |       |
             *              less                        k     great
             *
             * 不变量：
             *
             *              所有在 (left, less)   < pivot1
             *    pivot1 <= 所有在 [less, k)     <= pivot2
             *              所有在 (great, right) > pivot2
             *
             * 指针 k 是 ?-部分的第一个索引。
             */
            outer:
            for (int k = less - 1; ++k <= great; ) {
                int ak = a[k];
                if (ak < pivot1) { // 将 a[k] 移动到左部分
                    a[k] = a[less];
                    /*
                     * 这里和下面我们使用 "a[i] = b; i++;" 而不是 "a[i++] = b;" 由于性能问题。
                     */
                    a[less] = ak;
                    ++less;
                } else if (ak > pivot2) { // 将 a[k] 移动到右部分
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) { // a[great] <= pivot2
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // pivot1 <= a[great] <= pivot2
                        a[k] = a[great];
                    }
                    /*
                     * 这里和下面我们使用 "a[i] = b; i--;" 而不是 "a[i--] = b;" 由于性能问题。
                     */
                    a[great] = ak;
                    --great;
                }
            }

            // 将枢轴交换到它们的最终位置
            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
            a[right] = a[great + 1]; a[great + 1] = pivot2;

            // 递归地对左部分和右部分进行排序，排除已知的枢轴
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);

            /*
             * 如果中心部分太大（超过数组的 4/7），
             * 将内部枢轴值交换到两端。
             */
            if (less < e1 && e5 < great) {
                /*
                 * 跳过等于枢轴值的元素。
                 */
                while (a[less] == pivot1) {
                    ++less;
                }

                while (a[great] == pivot2) {
                    --great;
                }


                            /*
                             * 分区：
                             *
                             *   左部分         中心部分                  右部分
                             * +----------------------------------------------------------+
                             * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
                             * +----------------------------------------------------------+
                             *              ^                        ^       ^
                             *              |                        |       |
                             *             less                      k     great
                             *
                             * 不变量：
                             *
                             *              所有在 (*,  less) == pivot1
                             *     pivot1 < 所有在 [less,  k)  < pivot2
                             *              所有在 (great, *) == pivot2
                             *
                             * 指针 k 是 ?-部分的第一个索引。
                             */
                            outer:
                            for (int k = less - 1; ++k <= great; ) {
                                int ak = a[k];
                                if (ak == pivot1) { // 将 a[k] 移动到左部分
                                    a[k] = a[less];
                                    a[less] = ak;
                                    ++less;
                                } else if (ak == pivot2) { // 将 a[k] 移动到右部分
                                    while (a[great] == pivot2) {
                                        if (great-- == k) {
                                            break outer;
                                        }
                                    }
                                    if (a[great] == pivot1) { // a[great] < pivot2
                                        a[k] = a[less];
                                        /*
                                         * 即使 a[great] 等于 pivot1，赋值 a[less] = pivot1 也可能是不正确的，
                                         * 如果 a[great] 和 pivot1 是不同符号的浮点零。因此在 float 和
                                         * double 排序方法中，我们必须使用更准确的赋值 a[less] = a[great]。
                                         */
                                        a[less] = pivot1;
                                        ++less;
                                    } else { // pivot1 < a[great] < pivot2
                                        a[k] = a[great];
                                    }
                                    a[great] = ak;
                                    --great;
                                }
                            }
                        }

                        // 递归排序中心部分
                        sort(a, less, great, false);

                    } else { // 单个枢轴分区
                        /*
                         * 使用五个排序元素中的第三个作为枢轴。
                         * 这个值是中位数的廉价近似。
                         */
                        int pivot = a[e3];

                        /*
                         * 分区退化为传统的 3 路（或“荷兰国旗”）模式：
                         *
                         *   左部分    中心部分              右部分
                         * +-------------------------------------------------+
                         * |  < pivot  |   == pivot   |     ?    |  > pivot  |
                         * +-------------------------------------------------+
                         *              ^              ^        ^
                         *              |              |        |
                         *             less            k      great
                         *
                         * 不变量：
                         *
                         *   所有在 (left, less)   < pivot
                         *   所有在 [less, k)     == pivot
                         *   所有在 (great, right) > pivot
                         *
                         * 指针 k 是 ?-部分的第一个索引。
                         */
                        for (int k = less; k <= great; ++k) {
                            if (a[k] == pivot) {
                                continue;
                            }
                            int ak = a[k];
                            if (ak < pivot) { // 将 a[k] 移动到左部分
                                a[k] = a[less];
                                a[less] = ak;
                                ++less;
                            } else { // a[k] > pivot - 将 a[k] 移动到右部分
                                while (a[great] > pivot) {
                                    --great;
                                }
                                if (a[great] < pivot) { // a[great] <= pivot
                                    a[k] = a[less];
                                    a[less] = a[great];
                                    ++less;
                                } else { // a[great] == pivot
                                    /*
                                     * 即使 a[great] 等于 pivot，赋值 a[k] = pivot 也可能是不正确的，
                                     * 如果 a[great] 和 pivot 是不同符号的浮点零。因此在 float
                                     * 和 double 排序方法中，我们必须使用更准确的赋值 a[k] = a[great]。
                                     */
                                    a[k] = pivot;
                                }
                                a[great] = ak;
                                --great;
                            }
                        }

                        /*
                         * 递归排序左部分和右部分。
                         * 中心部分的所有元素都相等，
                         * 因此已经排序。
                         */
                        sort(a, left, less - 1, leftmost);
                        sort(a, great + 1, right, false);
                    }
                }

                /**
                 * 使用给定的工作空间数组切片（如果可能的话）对指定范围的数组进行排序
                 *
                 * @param a 要排序的数组
                 * @param left 要排序的第一个元素的索引（包含）
                 * @param right 要排序的最后一个元素的索引（包含）
                 * @param work 工作空间数组（切片）
                 * @param workBase 工作数组中可用空间的起始位置
                 * @param workLen 工作数组的可用大小
                 */
                static void sort(long[] a, int left, int right,
                                 long[] work, int workBase, int workLen) {
                    // 在小数组上使用快速排序
                    if (right - left < QUICKSORT_THRESHOLD) {
                        sort(a, left, right, true);
                        return;
                    }

                    /*
                     * 索引 run[i] 是第 i 个运行的起始位置
                     * （升序或降序序列）。
                     */
                    int[] run = new int[MAX_RUN_COUNT + 1];
                    int count = 0; run[0] = left;

                    // 检查数组是否接近排序
                    for (int k = left; k < right; run[count] = k) {
                        if (a[k] < a[k + 1]) { // 升序
                            while (++k <= right && a[k - 1] <= a[k]);
                        } else if (a[k] > a[k + 1]) { // 降序
                            while (++k <= right && a[k - 1] >= a[k]);
                            for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                                long t = a[lo]; a[lo] = a[hi]; a[hi] = t;
                            }
                        } else { // 相等
                            for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                                if (--m == 0) {
                                    sort(a, left, right, true);
                                    return;
                                }
                            }
                        }

                        /*
                         * 数组不是高度结构化的，
                         * 使用快速排序而不是归并排序。
                         */
                        if (++count == MAX_RUN_COUNT) {
                            sort(a, left, right, true);
                            return;
                        }
                    }

                    // 检查特殊情况
                    // 实现注释：变量 "right" 增加了 1。
                    if (run[count] == right++) { // 最后一个运行包含一个元素
                        run[++count] = right;
                    } else if (count == 1) { // 数组已经排序
                        return;
                    }

                    // 确定合并的交替基
                    byte odd = 0;
                    for (int n = 1; (n <<= 1) < count; odd ^= 1);

                    // 使用或创建临时数组 b 用于合并
                    long[] b;                 // 临时数组；与 a 交替
                    int ao, bo;              // 从 'left' 开始的数组偏移量
                    int blen = right - left; // b 需要的空间
                    if (work == null || workLen < blen || workBase + blen > work.length) {
                        work = new long[blen];
                        workBase = 0;
                    }
                    if (odd == 0) {
                        System.arraycopy(a, left, work, workBase, blen);
                        b = a;
                        bo = 0;
                        a = work;
                        ao = workBase - left;
                    } else {
                        b = work;
                        ao = 0;
                        bo = workBase - left;
                    }

                    // 合并
                    for (int last; count > 1; count = last) {
                        for (int k = (last = 0) + 2; k <= count; k += 2) {
                            int hi = run[k], mi = run[k - 1];
                            for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                                if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
                                    b[i + bo] = a[p++ + ao];
                                } else {
                                    b[i + bo] = a[q++ + ao];
                                }
                            }
                            run[++last] = hi;
                        }
                        if ((count & 1) != 0) {
                            for (int i = right, lo = run[count - 1]; --i >= lo;
                                 b[i + bo] = a[i + ao]
                            );
                            run[++last] = right;
                        }
                        long[] t = a; a = b; b = t;
                        int o = ao; ao = bo; bo = o;
                    }
                }

                /**
                 * 使用双枢轴快速排序对指定范围的数组进行排序。
                 *
                 * @param a 要排序的数组
                 * @param left 要排序的第一个元素的索引（包含）
                 * @param right 要排序的最后一个元素的索引（包含）
                 * @param leftmost 表示这部分是否是范围的最左边部分
                 */
                private static void sort(long[] a, int left, int right, boolean leftmost) {
                    int length = right - left + 1;

                    // 在小数组上使用插入排序
                    if (length < INSERTION_SORT_THRESHOLD) {
                        if (leftmost) {
                            /*
                             * 传统的（没有哨兵）插入排序，
                             * 优化用于服务器 VM，在最左边部分的情况下使用。
                             */
                            for (int i = left, j = i; i < right; j = ++i) {
                                long ai = a[i + 1];
                                while (ai < a[j]) {
                                    a[j + 1] = a[j];
                                    if (j-- == left) {
                                        break;
                                    }
                                }
                                a[j + 1] = ai;
                            }
                        } else {
                            /*
                             * 跳过最长的升序序列。
                             */
                            do {
                                if (left >= right) {
                                    return;
                                }
                            } while (a[++left] >= a[left - 1]);

                            /*
                             * 每个相邻部分的元素都充当哨兵，因此这允许我们在每次迭代中避免
                             * 左范围检查。此外，我们使用更优化的算法，称为成对插入
                             * 排序，这在快速排序的上下文中比传统实现的插入排序更快。
                             */
                            for (int k = left; ++left <= right; k = ++left) {
                                long a1 = a[k], a2 = a[left];

                                if (a1 < a2) {
                                    a2 = a1; a1 = a[left];
                                }
                                while (a1 < a[--k]) {
                                    a[k + 2] = a[k];
                                }
                                a[++k + 1] = a1;

                                while (a2 < a[--k]) {
                                    a[k + 1] = a[k];
                                }
                                a[k + 1] = a2;
                            }
                            long last = a[right];

                            while (last < a[--right]) {
                                a[right + 1] = a[right];
                            }
                            a[right + 1] = last;
                        }
                        return;
                    }

                    // 便宜的 length / 7 近似
                    int seventh = (length >> 3) + (length >> 6) + 1;

                    /*
                     * 对范围内的五个均匀分布的元素（包括中心元素）进行排序。这些元素将用于
                     * 选择枢轴，如下所述。这些元素的间距选择是通过经验确定的，适用于
                     * 广泛的输入。
                     */
                    int e3 = (left + right) >>> 1; // 中点
                    int e2 = e3 - seventh;
                    int e1 = e2 - seventh;
                    int e4 = e3 + seventh;
                    int e5 = e4 + seventh;

                    // 使用插入排序对这些元素进行排序
                    if (a[e2] < a[e1]) { long t = a[e2]; a[e2] = a[e1]; a[e1] = t; }

                    if (a[e3] < a[e2]) { long t = a[e3]; a[e3] = a[e2]; a[e2] = t;
                        if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                    }
                    if (a[e4] < a[e3]) { long t = a[e4]; a[e4] = a[e3]; a[e3] = t;
                        if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                        }
                    }
                    if (a[e5] < a[e4]) { long t = a[e5]; a[e5] = a[e4]; a[e4] = t;
                        if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
                            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                            }
                        }
                    }

                    // 指针
                    int less  = left;  // 中心部分第一个元素的索引
                    int great = right; // 右部分第一个元素之前的索引

                    if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
                        /*
                         * 使用五个排序元素中的第二个和第四个作为枢轴。
                         * 这些值是数组的第一个和第二个三分位数的廉价近似。注意 pivot1 <= pivot2。
                         */
                        long pivot1 = a[e2];
                        long pivot2 = a[e4];

                        /*
                         * 第一个和最后一个要排序的元素被移动到枢轴之前的位置。当分区完成时，
                         * 枢轴被交换回其最终位置，并从后续排序中排除。
                         */
                        a[e2] = a[left];
                        a[e4] = a[right];

                        /*
                         * 跳过小于或大于枢轴值的元素。
                         */
                        while (a[++less] < pivot1);
                        while (a[--great] > pivot2);

                        /*
                         * 分区：
                         *
                         *   左部分           中心部分                   右部分
                         * +--------------------------------------------------------------+
                         * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
                         * +--------------------------------------------------------------+
                         *               ^                          ^       ^
                         *               |                          |       |
                         *              less                        k     great
                         *
                         * 不变量：
                         *
                         *              所有在 (left, less)   < pivot1
                         *    pivot1 <= 所有在 [less, k)     <= pivot2
                         *              所有在 (great, right) > pivot2
                         *
                         * 指针 k 是 ?-部分的第一个索引。
                         */
                        outer:
                        for (int k = less - 1; ++k <= great; ) {
                            long ak = a[k];
                            if (ak < pivot1) { // 将 a[k] 移动到左部分
                                a[k] = a[less];
                                /*
                                 * 这里和下面我们使用 "a[i] = b; i++;" 而不是 "a[i++] = b;" 由于性能问题。
                                 */
                                a[less] = ak;
                                ++less;
                            } else if (ak > pivot2) { // 将 a[k] 移动到右部分
                                while (a[great] > pivot2) {
                                    if (great-- == k) {
                                        break outer;
                                    }
                                }
                                if (a[great] < pivot1) { // a[great] <= pivot2
                                    a[k] = a[less];
                                    a[less] = a[great];
                                    ++less;
                                } else { // pivot1 <= a[great] <= pivot2
                                    a[k] = a[great];
                                }
                                /*
                                 * 这里和下面我们使用 "a[i] = b; i--;" 而不是 "a[i--] = b;" 由于性能问题。
                                 */
                                a[great] = ak;
                                --great;
                            }
                        }


                        // 交换枢轴到它们的最终位置
            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
            a[right] = a[great + 1]; a[great + 1] = pivot2;

            // 递归地对左部和右部进行排序，排除已知的枢轴
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);

            /*
             * 如果中心部分太大（占数组的 > 4/7），
             * 将内部枢轴值交换到两端。
             */
            if (less < e1 && e5 < great) {
                /*
                 * 跳过等于枢轴值的元素。
                 */
                while (a[less] == pivot1) {
                    ++less;
                }

                while (a[great] == pivot2) {
                    --great;
                }

                /*
                 * 分区：
                 *
                 *   左部         中心部                  右部
                 * +----------------------------------------------------------+
                 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
                 * +----------------------------------------------------------+
                 *              ^                        ^       ^
                 *              |                        |       |
                 *             less                      k     great
                 *
                 * 不变量：
                 *
                 *              所有在 (*,  less) == pivot1
                 *     pivot1 < 所有在 [less,  k)  < pivot2
                 *              所有在 (great, *) == pivot2
                 *
                 * 指针 k 是 ?-部分的第一个索引。
                 */
                outer:
                for (int k = less - 1; ++k <= great; ) {
                    long ak = a[k];
                    if (ak == pivot1) { // 将 a[k] 移动到左部
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else if (ak == pivot2) { // 将 a[k] 移动到右部
                        while (a[great] == pivot2) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (a[great] == pivot1) { // a[great] < pivot2
                            a[k] = a[less];
                            /*
                             * 即使 a[great] 等于 pivot1，赋值 a[less] = pivot1 也可能是不正确的，
                             * 如果 a[great] 和 pivot1 是不同符号的浮点零。因此在 float 和
                             * double 排序方法中，我们必须使用更准确的赋值 a[less] = a[great]。
                             */
                            a[less] = pivot1;
                            ++less;
                        } else { // pivot1 < a[great] < pivot2
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        --great;
                    }
                }
            }

            // 递归地对中心部分进行排序
            sort(a, less, great, false);

        } else { // 使用一个枢轴进行分区
            /*
             * 使用五个已排序元素中的第三个作为枢轴。
             * 这个值是对中位数的廉价近似。
             */
            long pivot = a[e3];

            /*
             * 分区退化为传统的 3-路（或“荷兰国旗”）方案：
             *
             *   左部    中心部              右部
             * +-------------------------------------------------+
             * |  < pivot  |   == pivot   |     ?    |  > pivot  |
             * +-------------------------------------------------+
             *              ^              ^        ^
             *              |              |        |
             *             less            k      great
             *
             * 不变量：
             *
             *   所有在 (left, less)   < pivot
             *   所有在 [less, k)     == pivot
             *   所有在 (great, right) > pivot
             *
             * 指针 k 是 ?-部分的第一个索引。
             */
            for (int k = less; k <= great; ++k) {
                if (a[k] == pivot) {
                    continue;
                }
                long ak = a[k];
                if (ak < pivot) { // 将 a[k] 移动到左部
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else { // a[k] > pivot - 将 a[k] 移动到右部
                    while (a[great] > pivot) {
                        --great;
                    }
                    if (a[great] < pivot) { // a[great] <= pivot
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // a[great] == pivot
                        /*
                         * 即使 a[great] 等于 pivot，赋值 a[k] = pivot 也可能是不正确的，
                         * 如果 a[great] 和 pivot 是不同符号的浮点零。因此在 float
                         * 和 double 排序方法中，我们必须使用更准确的赋值 a[k] = a[great]。
                         */
                        a[k] = pivot;
                    }
                    a[great] = ak;
                    --great;
                }
            }

            /*
             * 递归地对左部和右部进行排序。
             * 中心部分的所有元素都相等
             * 因此已经排序。
             */
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        }
    }

    /**
     * 对指定范围的数组进行排序，如果可能则使用给定的工作区数组切片进行合并
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param work 工作区数组（切片）
     * @param workBase 工作区数组中可用空间的起始位置
     * @param workLen 工作区数组的可用大小
     */
    static void sort(short[] a, int left, int right,
                     short[] work, int workBase, int workLen) {
        // 在大数组上使用计数排序
        if (right - left > COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR) {
            int[] count = new int[NUM_SHORT_VALUES];

            for (int i = left - 1; ++i <= right;
                count[a[i] - Short.MIN_VALUE]++
            );
            for (int i = NUM_SHORT_VALUES, k = right + 1; k > left; ) {
                while (count[--i] == 0);
                short value = (short) (i + Short.MIN_VALUE);
                int s = count[i];

                do {
                    a[--k] = value;
                } while (--s > 0);
            }
        } else { // 在小数组上使用双枢轴快速排序
            doSort(a, left, right, work, workBase, workLen);
        }
    }

    /** 短整型值的数量。 */
    private static final int NUM_SHORT_VALUES = 1 << 16;

    /**
     * 对指定范围的数组进行排序。
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param work 工作区数组（切片）
     * @param workBase 工作区数组中可用空间的起始位置
     * @param workLen 工作区数组的可用大小
     */
    private static void doSort(short[] a, int left, int right,
                               short[] work, int workBase, int workLen) {
        // 在小数组上使用快速排序
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }

        /*
         * 索引 run[i] 是第 i 个运行的起始位置
         * （升序或降序序列）。
         */
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0; run[0] = left;

        // 检查数组是否接近排序
        for (int k = left; k < right; run[count] = k) {
            if (a[k] < a[k + 1]) { // 升序
                while (++k <= right && a[k - 1] <= a[k]);
            } else if (a[k] > a[k + 1]) { // 降序
                while (++k <= right && a[k - 1] >= a[k]);
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    short t = a[lo]; a[lo] = a[hi]; a[hi] = t;
                }
            } else { // 相等
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    if (--m == 0) {
                        sort(a, left, right, true);
                        return;
                    }
                }
            }

            /*
             * 数组不是高度结构化的，
             * 使用快速排序而不是归并排序。
             */
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }

        // 检查特殊情况
        // 实现注释：变量 "right" 增加了 1。
        if (run[count] == right++) { // 最后一个运行包含一个元素
            run[++count] = right;
        } else if (count == 1) { // 数组已经排序
            return;
        }

        // 确定合并的交替基
        byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1);

        // 使用或创建临时数组 b 进行合并
        short[] b;                 // 临时数组；与 a 交替
        int ao, bo;              // 从 'left' 开始的数组偏移量
        int blen = right - left; // b 需要的空间
        if (work == null || workLen < blen || workBase + blen > work.length) {
            work = new short[blen];
            workBase = 0;
        }
        if (odd == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }

        // 合并
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
                        b[i + bo] = a[p++ + ao];
                    } else {
                        b[i + bo] = a[q++ + ao];
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                    b[i + bo] = a[i + ao]
                );
                run[++last] = right;
            }
            short[] t = a; a = b; b = t;
            int o = ao; ao = bo; bo = o;
        }
    }

    /**
     * 使用双枢轴快速排序对指定范围的数组进行排序。
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param leftmost 表示这部分是否是范围的最左部分
     */
    private static void sort(short[] a, int left, int right, boolean leftmost) {
        int length = right - left + 1;

        // 在非常小的数组上使用插入排序
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                /*
                 * 传统的（没有哨兵）插入排序，
                 * 优化用于服务器 VM，在最左部分的情况下使用。
                 */
                for (int i = left, j = i; i < right; j = ++i) {
                    short ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        if (j-- == left) {
                            break;
                        }
                    }
                    a[j + 1] = ai;
                }
            } else {
                /*
                 * 跳过最长的升序序列。
                 */
                do {
                    if (left >= right) {
                        return;
                    }
                } while (a[++left] >= a[left - 1]);

                /*
                 * 每个来自相邻部分的元素都充当哨兵，因此这允许我们在每次迭代中
                 * 避免左范围检查。此外，我们使用更优化的算法，称为成对插入
                 * 排序，这在快速排序的上下文中比传统的插入排序实现更快。
                 */
                for (int k = left; ++left <= right; k = ++left) {
                    short a1 = a[k], a2 = a[left];

                    if (a1 < a2) {
                        a2 = a1; a1 = a[left];
                    }
                    while (a1 < a[--k]) {
                        a[k + 2] = a[k];
                    }
                    a[++k + 1] = a1;

                    while (a2 < a[--k]) {
                        a[k + 1] = a[k];
                    }
                    a[k + 1] = a2;
                }
                short last = a[right];

                while (last < a[--right]) {
                    a[right + 1] = a[right];
                }
                a[right + 1] = last;
            }
            return;
        }

        // 便宜地近似 length / 7
        int seventh = (length >> 3) + (length >> 6) + 1;

        /*
         * 对范围中（包括）中心元素周围的五个均匀间隔的元素进行排序。这些元素将用于
         * 枢轴选择，如下所述。选择这些元素的间距是通过经验确定的，适用于各种输入。
         */
        int e3 = (left + right) >>> 1; // 中点
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;

        // 使用插入排序对这些元素进行排序
        if (a[e2] < a[e1]) { short t = a[e2]; a[e2] = a[e1]; a[e1] = t; }

        if (a[e3] < a[e2]) { short t = a[e3]; a[e3] = a[e2]; a[e2] = t;
            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
        }
        if (a[e4] < a[e3]) { short t = a[e4]; a[e4] = a[e3]; a[e3] = t;
            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
            }
        }
        if (a[e5] < a[e4]) { short t = a[e5]; a[e5] = a[e4]; a[e4] = t;
            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                }
            }
        }


                    // 指针
        int less  = left;  // 中心部分的第一个元素的索引
        int great = right; // 右部分第一个元素前的索引

        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            /*
             * 使用五个排序元素中的第二个和第四个作为枢轴。
             * 这些值是数组第一和第二三分位数的廉价近似值。注意 pivot1 <= pivot2。
             */
            short pivot1 = a[e2];
            short pivot2 = a[e4];

            /*
             * 将要排序的第一个和最后一个元素移动到枢轴先前占据的位置。当分区完成时，
             * 枢轴被交换回它们的最终位置，并从后续排序中排除。
             */
            a[e2] = a[left];
            a[e4] = a[right];

            /*
             * 跳过小于或大于枢轴值的元素。
             */
            while (a[++less] < pivot1);
            while (a[--great] > pivot2);

            /*
             * 分区：
             *
             *   左部分           中心部分                   右部分
             * +--------------------------------------------------------------+
             * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
             * +--------------------------------------------------------------+
             *               ^                          ^       ^
             *               |                          |       |
             *              less                        k     great
             *
             * 不变量：
             *
             *              所有在 (left, less)   < pivot1
             *    pivot1 <= 所有在 [less, k)     <= pivot2
             *              所有在 (great, right) > pivot2
             *
             * 指针 k 是 ?-部分的第一个索引。
             */
            outer:
            for (int k = less - 1; ++k <= great; ) {
                short ak = a[k];
                if (ak < pivot1) { // 将 a[k] 移动到左部分
                    a[k] = a[less];
                    /*
                     * 这里和下面我们使用 "a[i] = b; i++;" 而不是 "a[i++] = b;" 由于性能问题。
                     */
                    a[less] = ak;
                    ++less;
                } else if (ak > pivot2) { // 将 a[k] 移动到右部分
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) { // a[great] <= pivot2
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // pivot1 <= a[great] <= pivot2
                        a[k] = a[great];
                    }
                    /*
                     * 这里和下面我们使用 "a[i] = b; i--;" 而不是 "a[i--] = b;" 由于性能问题。
                     */
                    a[great] = ak;
                    --great;
                }
            }

            // 将枢轴交换到它们的最终位置
            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
            a[right] = a[great + 1]; a[great + 1] = pivot2;

            // 递归地对左部分和右部分进行排序，排除已知的枢轴
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);

            /*
             * 如果中心部分太大（占数组的 > 4/7），
             * 将内部枢轴值交换到两端。
             */
            if (less < e1 && e5 < great) {
                /*
                 * 跳过等于枢轴值的元素。
                 */
                while (a[less] == pivot1) {
                    ++less;
                }

                while (a[great] == pivot2) {
                    --great;
                }

                /*
                 * 分区：
                 *
                 *   左部分         中心部分                  右部分
                 * +----------------------------------------------------------+
                 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
                 * +----------------------------------------------------------+
                 *              ^                        ^       ^
                 *              |                        |       |
                 *             less                      k     great
                 *
                 * 不变量：
                 *
                 *              所有在 (*,  less) == pivot1
                 *     pivot1 < 所有在 [less,  k)  < pivot2
                 *              所有在 (great, *) == pivot2
                 *
                 * 指针 k 是 ?-部分的第一个索引。
                 */
                outer:
                for (int k = less - 1; ++k <= great; ) {
                    short ak = a[k];
                    if (ak == pivot1) { // 将 a[k] 移动到左部分
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else if (ak == pivot2) { // 将 a[k] 移动到右部分
                        while (a[great] == pivot2) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (a[great] == pivot1) { // a[great] < pivot2
                            a[k] = a[less];
                            /*
                             * 即使 a[great] 等于 pivot1，赋值 a[less] = pivot1 可能是不正确的，
                             * 如果 a[great] 和 pivot1 是不同符号的浮点零。因此在 float 和
                             * double 排序方法中我们必须使用更准确的赋值 a[less] = a[great]。
                             */
                            a[less] = pivot1;
                            ++less;
                        } else { // pivot1 < a[great] < pivot2
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        --great;
                    }
                }
            }

            // 递归地对中心部分进行排序
            sort(a, less, great, false);

        } else { // 使用一个枢轴进行分区
            /*
             * 使用五个排序元素中的第三个作为枢轴。
             * 这个值是中位数的廉价近似值。
             */
            short pivot = a[e3];

            /*
             * 分区退化为传统的 3 路（或“荷兰国旗”）模式：
             *
             *   左部分    中心部分              右部分
             * +-------------------------------------------------+
             * |  < pivot  |   == pivot   |     ?    |  > pivot  |
             * +-------------------------------------------------+
             *              ^              ^        ^
             *              |              |        |
             *             less            k      great
             *
             * 不变量：
             *
             *   所有在 (left, less)   < pivot
             *   所有在 [less, k)     == pivot
             *   所有在 (great, right) > pivot
             *
             * 指针 k 是 ?-部分的第一个索引。
             */
            for (int k = less; k <= great; ++k) {
                if (a[k] == pivot) {
                    continue;
                }
                short ak = a[k];
                if (ak < pivot) { // 将 a[k] 移动到左部分
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else { // a[k] > pivot - 将 a[k] 移动到右部分
                    while (a[great] > pivot) {
                        --great;
                    }
                    if (a[great] < pivot) { // a[great] <= pivot
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // a[great] == pivot
                        /*
                         * 即使 a[great] 等于 pivot，赋值 a[k] = pivot 可能是不正确的，
                         * 如果 a[great] 和 pivot 是不同符号的浮点零。因此在 float
                         * 和 double 排序方法中我们必须使用更准确的赋值 a[k] = a[great]。
                         */
                        a[k] = pivot;
                    }
                    a[great] = ak;
                    --great;
                }
            }

            /*
             * 递归地对左部分和右部分进行排序。
             * 中心部分的所有元素都相等
             * 因此已经排序。
             */
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        }
    }

    /**
     * 使用给定的工作空间数组切片（如果可能）对数组的指定范围进行排序
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param work 工作空间数组（切片）
     * @param workBase 工作数组中可用空间的起始位置
     * @param workLen 工作数组的可用大小
     */
    static void sort(char[] a, int left, int right,
                     char[] work, int workBase, int workLen) {
        // 在大数组上使用计数排序
        if (right - left > COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR) {
            int[] count = new int[NUM_CHAR_VALUES];

            for (int i = left - 1; ++i <= right;
                count[a[i]]++
            );
            for (int i = NUM_CHAR_VALUES, k = right + 1; k > left; ) {
                while (count[--i] == 0);
                char value = (char) i;
                int s = count[i];

                do {
                    a[--k] = value;
                } while (--s > 0);
            }
        } else { // 在小数组上使用双枢轴快速排序
            doSort(a, left, right, work, workBase, workLen);
        }
    }

    /** 字符的不同值的数量。 */
    private static final int NUM_CHAR_VALUES = 1 << 16;

    /**
     * 对数组的指定范围进行排序。
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param work 工作空间数组（切片）
     * @param workBase 工作数组中可用空间的起始位置
     * @param workLen 工作数组的可用大小
     */
    private static void doSort(char[] a, int left, int right,
                               char[] work, int workBase, int workLen) {
        // 在小数组上使用快速排序
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }

        /*
         * 索引 run[i] 是第 i 个运行的起始位置
         * （升序或降序序列）。
         */
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0; run[0] = left;

        // 检查数组是否几乎已排序
        for (int k = left; k < right; run[count] = k) {
            if (a[k] < a[k + 1]) { // 升序
                while (++k <= right && a[k - 1] <= a[k]);
            } else if (a[k] > a[k + 1]) { // 降序
                while (++k <= right && a[k - 1] >= a[k]);
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    char t = a[lo]; a[lo] = a[hi]; a[hi] = t;
                }
            } else { // 相等
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    if (--m == 0) {
                        sort(a, left, right, true);
                        return;
                    }
                }
            }

            /*
             * 数组不是高度结构化的，
             * 使用快速排序而不是归并排序。
             */
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }

        // 检查特殊情况
        // 实现注释：变量 "right" 增加了 1。
        if (run[count] == right++) { // 最后一个运行包含一个元素
            run[++count] = right;
        } else if (count == 1) { // 数组已经排序
            return;
        }

        // 确定合并的交替基数
        byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1);

        // 使用或创建临时数组 b 用于合并
        char[] b;                 // 临时数组；与 a 交替
        int ao, bo;              // 从 'left' 开始的数组偏移量
        int blen = right - left; // b 所需的空间
        if (work == null || workLen < blen || workBase + blen > work.length) {
            work = new char[blen];
            workBase = 0;
        }
        if (odd == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }

        // 合并
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
                        b[i + bo] = a[p++ + ao];
                    } else {
                        b[i + bo] = a[q++ + ao];
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                    b[i + bo] = a[i + ao]
                );
                run[++last] = right;
            }
            char[] t = a; a = b; b = t;
            int o = ao; ao = bo; bo = o;
        }
    }

    /**
     * 使用双枢轴快速排序对数组的指定范围进行排序。
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param leftmost 表示此部分是否是范围的最左边部分
     */
    private static void sort(char[] a, int left, int right, boolean leftmost) {
        int length = right - left + 1;

        // 在非常小的数组上使用插入排序
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                /*
                 * 传统的（没有哨兵）插入排序，
                 * 优化用于服务器 VM，在最左边部分的情况下使用。
                 */
                for (int i = left, j = i; i < right; j = ++i) {
                    char ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        if (j-- == left) {
                            break;
                        }
                    }
                    a[j + 1] = ai;
                }
            } else {
                /*
                 * 跳过最长的升序序列。
                 */
                do {
                    if (left >= right) {
                        return;
                    }
                } while (a[++left] >= a[left - 1]);


                            /*
                 * 每个相邻部分的元素都充当哨兵的角色，因此这使我们能够避免每次迭代时的左范围检查。此外，我们使用了一种更优化的算法，即所谓的成对插入排序，这种算法（在快速排序的上下文中）比传统的插入排序实现更快。
                 */
                for (int k = left; ++left <= right; k = ++left) {
                    char a1 = a[k], a2 = a[left];

                    if (a1 < a2) {
                        a2 = a1; a1 = a[left];
                    }
                    while (a1 < a[--k]) {
                        a[k + 2] = a[k];
                    }
                    a[++k + 1] = a1;

                    while (a2 < a[--k]) {
                        a[k + 1] = a[k];
                    }
                    a[k + 1] = a2;
                }
                char last = a[right];

                while (last < a[--right]) {
                    a[right + 1] = a[right];
                }
                a[right + 1] = last;
            }
            return;
        }

        // 代价低廉的 length / 7 的近似值
        int seventh = (length >> 3) + (length >> 6) + 1;

        /*
         * 对范围内的五个均匀分布的元素（包括中心元素）进行排序。这些元素将用于枢轴选择，如下所述。这些元素的间隔选择是通过经验确定的，适用于各种输入。
         */
        int e3 = (left + right) >>> 1; // 中点
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;

        // 使用插入排序对这些元素进行排序
        if (a[e2] < a[e1]) { char t = a[e2]; a[e2] = a[e1]; a[e1] = t; }

        if (a[e3] < a[e2]) { char t = a[e3]; a[e3] = a[e2]; a[e2] = t;
            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
        }
        if (a[e4] < a[e3]) { char t = a[e4]; a[e4] = a[e3]; a[e3] = t;
            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
            }
        }
        if (a[e5] < a[e4]) { char t = a[e5]; a[e5] = a[e4]; a[e4] = t;
            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                }
            }
        }

        // 指针
        int less  = left;  // 中心部分的第一个元素的索引
        int great = right; // 右部分的第一个元素之前的索引

        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            /*
             * 使用五个排序元素中的第二个和第四个作为枢轴。这些值是数组第一和第二三分位数的廉价近似值。注意 pivot1 <= pivot2。
             */
            char pivot1 = a[e2];
            char pivot2 = a[e4];

            /*
             * 将要排序的第一个和最后一个元素移动到枢轴先前占据的位置。当分区完成时，枢轴被交换回其最终位置，并从后续排序中排除。
             */
            a[e2] = a[left];
            a[e4] = a[right];

            /*
             * 跳过小于或大于枢轴值的元素。
             */
            while (a[++less] < pivot1);
            while (a[--great] > pivot2);

            /*
             * 分区：
             *
             *   左部分           中心部分                   右部分
             * +--------------------------------------------------------------+
             * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
             * +--------------------------------------------------------------+
             *               ^                          ^       ^
             *               |                          |       |
             *              less                        k     great
             *
             * 不变量：
             *
             *              所有在 (left, less)   < pivot1
             *    pivot1 <= 所有在 [less, k)     <= pivot2
             *              所有在 (great, right) > pivot2
             *
             * 指针 k 是 ?-部分的第一个索引。
             */
            outer:
            for (int k = less - 1; ++k <= great; ) {
                char ak = a[k];
                if (ak < pivot1) { // 将 a[k] 移动到左部分
                    a[k] = a[less];
                    /*
                     * 在这里和下面我们使用 "a[i] = b; i++;" 而不是 "a[i++] = b;" 由于性能问题。
                     */
                    a[less] = ak;
                    ++less;
                } else if (ak > pivot2) { // 将 a[k] 移动到右部分
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) { // a[great] <= pivot2
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // pivot1 <= a[great] <= pivot2
                        a[k] = a[great];
                    }
                    /*
                     * 在这里和下面我们使用 "a[i] = b; i--;" 而不是 "a[i--] = b;" 由于性能问题。
                     */
                    a[great] = ak;
                    --great;
                }
            }

            // 将枢轴交换到其最终位置
            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
            a[right] = a[great + 1]; a[great + 1] = pivot2;

            // 递归地对左部分和右部分进行排序，排除已知的枢轴
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);

            /*
             * 如果中心部分太大（超过数组的 4/7），
             * 将内部枢轴值交换到两端。
             */
            if (less < e1 && e5 < great) {
                /*
                 * 跳过等于枢轴值的元素。
                 */
                while (a[less] == pivot1) {
                    ++less;
                }

                while (a[great] == pivot2) {
                    --great;
                }

                /*
                 * 分区：
                 *
                 *   左部分         中心部分                  右部分
                 * +----------------------------------------------------------+
                 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
                 * +----------------------------------------------------------+
                 *              ^                        ^       ^
                 *              |                        |       |
                 *             less                      k     great
                 *
                 * 不变量：
                 *
                 *              所有在 (*,  less) == pivot1
                 *     pivot1 < 所有在 [less,  k)  < pivot2
                 *              所有在 (great, *) == pivot2
                 *
                 * 指针 k 是 ?-部分的第一个索引。
                 */
                outer:
                for (int k = less - 1; ++k <= great; ) {
                    char ak = a[k];
                    if (ak == pivot1) { // 将 a[k] 移动到左部分
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else if (ak == pivot2) { // 将 a[k] 移动到右部分
                        while (a[great] == pivot2) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (a[great] == pivot1) { // a[great] < pivot2
                            a[k] = a[less];
                            /*
                             * 即使 a[great] 等于 pivot1，赋值 a[less] = pivot1 可能是不正确的，
                             * 如果 a[great] 和 pivot1 是不同符号的浮点零。因此在 float 和
                             * double 排序方法中，我们必须使用更准确的赋值 a[less] = a[great]。
                             */
                            a[less] = pivot1;
                            ++less;
                        } else { // pivot1 < a[great] < pivot2
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        --great;
                    }
                }
            }

            // 递归地对中心部分进行排序
            sort(a, less, great, false);

        } else { // 使用一个枢轴进行分区
            /*
             * 使用五个排序元素中的第三个作为枢轴。这个值是中位数的廉价近似值。
             */
            char pivot = a[e3];

            /*
             * 分区退化为传统的 3 路（或“荷兰国旗”）模式：
             *
             *   左部分    中心部分              右部分
             * +-------------------------------------------------+
             * |  < pivot  |   == pivot   |     ?    |  > pivot  |
             * +-------------------------------------------------+
             *              ^              ^        ^
             *              |              |        |
             *             less            k      great
             *
             * 不变量：
             *
             *   所有在 (left, less)   < pivot
             *   所有在 [less, k)     == pivot
             *   所有在 (great, right) > pivot
             *
             * 指针 k 是 ?-部分的第一个索引。
             */
            for (int k = less; k <= great; ++k) {
                if (a[k] == pivot) {
                    continue;
                }
                char ak = a[k];
                if (ak < pivot) { // 将 a[k] 移动到左部分
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else { // a[k] > pivot - 将 a[k] 移动到右部分
                    while (a[great] > pivot) {
                        --great;
                    }
                    if (a[great] < pivot) { // a[great] <= pivot
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // a[great] == pivot
                        /*
                         * 即使 a[great] 等于 pivot，赋值 a[k] = pivot 可能是不正确的，
                         * 如果 a[great] 和 pivot 是不同符号的浮点零。因此在 float
                         * 和 double 排序方法中，我们必须使用更准确的赋值 a[k] = a[great]。
                         */
                        a[k] = pivot;
                    }
                    a[great] = ak;
                    --great;
                }
            }

            /*
             * 递归地对左部分和右部分进行排序。
             * 中心部分的所有元素都相等
             * 因此已经排序。
             */
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        }
    }

    /** 不同字节值的数量。 */
    private static final int NUM_BYTE_VALUES = 1 << 8;

    /**
     * 对指定范围的数组进行排序。
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     */
    static void sort(byte[] a, int left, int right) {
        // 在大数组上使用计数排序
        if (right - left > COUNTING_SORT_THRESHOLD_FOR_BYTE) {
            int[] count = new int[NUM_BYTE_VALUES];

            for (int i = left - 1; ++i <= right;
                count[a[i] - Byte.MIN_VALUE]++
            );
            for (int i = NUM_BYTE_VALUES, k = right + 1; k > left; ) {
                while (count[--i] == 0);
                byte value = (byte) (i + Byte.MIN_VALUE);
                int s = count[i];

                do {
                    a[--k] = value;
                } while (--s > 0);
            }
        } else { // 在小数组上使用插入排序
            for (int i = left, j = i; i < right; j = ++i) {
                byte ai = a[i + 1];
                while (ai < a[j]) {
                    a[j + 1] = a[j];
                    if (j-- == left) {
                        break;
                    }
                }
                a[j + 1] = ai;
            }
        }
    }

    /**
     * 使用给定的工作空间数组切片（如果可能的话）对指定范围的数组进行排序以进行合并
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param work 工作空间数组（切片）
     * @param workBase 工作数组中可用空间的起点
     * @param workLen 工作数组的可用大小
     */
    static void sort(float[] a, int left, int right,
                     float[] work, int workBase, int workLen) {
        /*
         * 第 1 阶段：将 NaN 移动到数组的末尾。
         */
        while (left <= right && Float.isNaN(a[right])) {
            --right;
        }
        for (int k = right; --k >= left; ) {
            float ak = a[k];
            if (ak != ak) { // a[k] 是 NaN
                a[k] = a[right];
                a[right] = ak;
                --right;
            }
        }

        /*
         * 第 2 阶段：对除 NaN 之外的所有元素进行排序（NaN 已经就位）。
         */
        doSort(a, left, right, work, workBase, workLen);

        /*
         * 第 3 阶段：将负零放在正零之前。
         */
        int hi = right;

        /*
         * 找到第一个零，或第一个正数，或最后一个负数元素。
         */
        while (left < hi) {
            int middle = (left + hi) >>> 1;
            float middleValue = a[middle];

            if (middleValue < 0.0f) {
                left = middle + 1;
            } else {
                hi = middle;
            }
        }

        /*
         * 跳过最后一个负值（如果有）或所有前导负零。
         */
        while (left <= right && Float.floatToRawIntBits(a[left]) < 0) {
            ++left;
        }

        /*
         * 将负零移动到子范围的开头。
         *
         * 分区：
         *
         * +----------------------------------------------------+
         * |   < 0.0   |   -0.0   |   0.0   |   ?  ( >= 0.0 )   |
         * +----------------------------------------------------+
         *              ^          ^         ^
         *              |          |         |
         *             left        p         k
         *
         * 不变量：
         *
         *   所有在 (*,  left)  <  0.0
         *   所有在 [left,  p) == -0.0
         *   所有在 [p,     k) ==  0.0
         *   所有在 [k, right] >=  0.0
         *
         * 指针 k 是 ?-部分的第一个索引。
         */
        for (int k = left, p = left - 1; ++k <= right; ) {
            float ak = a[k];
            if (ak != 0.0f) {
                break;
            }
            if (Float.floatToRawIntBits(ak) < 0) { // ak 是 -0.0f
                a[k] = 0.0f;
                a[++p] = -0.0f;
            }
        }
    }


                /**
     * 对指定范围的数组进行排序。
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param work 一个工作空间数组（切片）
     * @param workBase 工作数组中可用空间的起始位置
     * @param workLen 工作数组的可用大小
     */
    private static void doSort(float[] a, int left, int right,
                               float[] work, int workBase, int workLen) {
        // 在小数组上使用快速排序
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }

        /*
         * 索引 run[i] 是第 i 个运行的起始位置
         * （升序或降序序列）。
         */
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0; run[0] = left;

        // 检查数组是否接近排序
        for (int k = left; k < right; run[count] = k) {
            if (a[k] < a[k + 1]) { // 升序
                while (++k <= right && a[k - 1] <= a[k]);
            } else if (a[k] > a[k + 1]) { // 降序
                while (++k <= right && a[k - 1] >= a[k]);
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    float t = a[lo]; a[lo] = a[hi]; a[hi] = t;
                }
            } else { // 相等
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    if (--m == 0) {
                        sort(a, left, right, true);
                        return;
                    }
                }
            }

            /*
             * 数组不是高度结构化的，
             * 使用快速排序而不是归并排序。
             */
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }

        // 检查特殊情况
        // 实现注释：变量 "right" 增加了 1。
        if (run[count] == right++) { // 最后一个运行包含一个元素
            run[++count] = right;
        } else if (count == 1) { // 数组已经排序
            return;
        }

        // 确定合并的交替基
        byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1);

        // 使用或创建临时数组 b 进行合并
        float[] b;                 // 临时数组；与 a 交替
        int ao, bo;              // 从 'left' 开始的数组偏移量
        int blen = right - left; // b 所需的空间
        if (work == null || workLen < blen || workBase + blen > work.length) {
            work = new float[blen];
            workBase = 0;
        }
        if (odd == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }

        // 合并
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
                        b[i + bo] = a[p++ + ao];
                    } else {
                        b[i + bo] = a[q++ + ao];
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                    b[i + bo] = a[i + ao]
                );
                run[++last] = right;
            }
            float[] t = a; a = b; b = t;
            int o = ao; ao = bo; bo = o;
        }
    }

    /**
     * 通过双轴快速排序对指定范围的数组进行排序。
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param leftmost 表示这部分是否是范围的最左边
     */
    private static void sort(float[] a, int left, int right, boolean leftmost) {
        int length = right - left + 1;

        // 在小数组上使用插入排序
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                /*
                 * 传统的（没有哨兵）插入排序，
                 * 优化用于服务器 VM，在最左边部分使用。
                 */
                for (int i = left, j = i; i < right; j = ++i) {
                    float ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        if (j-- == left) {
                            break;
                        }
                    }
                    a[j + 1] = ai;
                }
            } else {
                /*
                 * 跳过最长的升序序列。
                 */
                do {
                    if (left >= right) {
                        return;
                    }
                } while (a[++left] >= a[left - 1]);

                /*
                 * 每个相邻部分的元素都充当哨兵，因此这允许我们在每次迭代中避免左范围检查。
                 * 此外，我们使用更优化的算法，称为配对插入排序，这在快速排序的上下文中
                 * 比传统的插入排序实现更快。
                 */
                for (int k = left; ++left <= right; k = ++left) {
                    float a1 = a[k], a2 = a[left];

                    if (a1 < a2) {
                        a2 = a1; a1 = a[left];
                    }
                    while (a1 < a[--k]) {
                        a[k + 2] = a[k];
                    }
                    a[++k + 1] = a1;

                    while (a2 < a[--k]) {
                        a[k + 1] = a[k];
                    }
                    a[k + 1] = a2;
                }
                float last = a[right];

                while (last < a[--right]) {
                    a[right + 1] = a[right];
                }
                a[right + 1] = last;
            }
            return;
        }

        // 便宜的长度 / 7 的近似值
        int seventh = (length >> 3) + (length >> 6) + 1;

        /*
         * 对范围中（包括）中心元素周围的五个均匀间隔的元素进行排序。
         * 这些元素将用于选择枢轴，如下所述。这些元素的间距选择是通过经验确定的，
         * 在各种输入上表现良好。
         */
        int e3 = (left + right) >>> 1; // 中点
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;

        // 使用插入排序对这些元素进行排序
        if (a[e2] < a[e1]) { float t = a[e2]; a[e2] = a[e1]; a[e1] = t; }

        if (a[e3] < a[e2]) { float t = a[e3]; a[e3] = a[e2]; a[e2] = t;
            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
        }
        if (a[e4] < a[e3]) { float t = a[e4]; a[e4] = a[e3]; a[e3] = t;
            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
            }
        }
        if (a[e5] < a[e4]) { float t = a[e5]; a[e5] = a[e4]; a[e4] = t;
            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                }
            }
        }

        // 指针
        int less  = left;  // 中心部分的第一个元素的索引
        int great = right; // 右部分第一个元素之前的索引

        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            /*
             * 使用五个排序元素中的第二个和第四个作为枢轴。
             * 这些值是数组第一和第二三分位的便宜近似值。注意 pivot1 <= pivot2。
             */
            float pivot1 = a[e2];
            float pivot2 = a[e4];

            /*
             * 第一个和最后一个要排序的元素被移动到枢轴原来的位置。
             * 当分区完成时，枢轴被交换回它们的最终位置，并从后续排序中排除。
             */
            a[e2] = a[left];
            a[e4] = a[right];

            /*
             * 跳过小于或大于枢轴值的元素。
             */
            while (a[++less] < pivot1);
            while (a[--great] > pivot2);

            /*
             * 分区：
             *
             *   左部分           中心部分                   右部分
             * +--------------------------------------------------------------+
             * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
             * +--------------------------------------------------------------+
             *               ^                          ^       ^
             *               |                          |       |
             *              less                        k     great
             *
             * 不变量：
             *
             *              所有在 (left, less)   < pivot1
             *    pivot1 <= 所有在 [less, k)     <= pivot2
             *              所有在 (great, right) > pivot2
             *
             * 指针 k 是 ?-部分的第一个索引。
             */
            outer:
            for (int k = less - 1; ++k <= great; ) {
                float ak = a[k];
                if (ak < pivot1) { // 将 a[k] 移动到左部分
                    a[k] = a[less];
                    /*
                     * 这里和下面我们使用 "a[i] = b; i++;" 而不是 "a[i++] = b;" 由于性能问题。
                     */
                    a[less] = ak;
                    ++less;
                } else if (ak > pivot2) { // 将 a[k] 移动到右部分
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) { // a[great] <= pivot2
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // pivot1 <= a[great] <= pivot2
                        a[k] = a[great];
                    }
                    /*
                     * 这里和下面我们使用 "a[i] = b; i--;" 而不是 "a[i--] = b;" 由于性能问题。
                     */
                    a[great] = ak;
                    --great;
                }
            }

            // 将枢轴交换到它们的最终位置
            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
            a[right] = a[great + 1]; a[great + 1] = pivot2;

            // 递归地对左部分和右部分进行排序，排除已知的枢轴
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);

            /*
             * 如果中心部分太大（超过数组的 4/7），
             * 将内部枢轴值交换到两端。
             */
            if (less < e1 && e5 < great) {
                /*
                 * 跳过等于枢轴值的元素。
                 */
                while (a[less] == pivot1) {
                    ++less;
                }

                while (a[great] == pivot2) {
                    --great;
                }

                /*
                 * 分区：
                 *
                 *   左部分         中心部分                  右部分
                 * +----------------------------------------------------------+
                 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
                 * +----------------------------------------------------------+
                 *              ^                        ^       ^
                 *              |                        |       |
             *             less                      k     great
                 *
                 * 不变量：
                 *
                 *              所有在 (*,  less) == pivot1
                 *     pivot1 < 所有在 [less,  k)  < pivot2
                 *              所有在 (great, *) == pivot2
                 *
                 * 指针 k 是 ?-部分的第一个索引。
                 */
                outer:
                for (int k = less - 1; ++k <= great; ) {
                    float ak = a[k];
                    if (ak == pivot1) { // 将 a[k] 移动到左部分
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else if (ak == pivot2) { // 将 a[k] 移动到右部分
                        while (a[great] == pivot2) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (a[great] == pivot1) { // a[great] < pivot2
                            a[k] = a[less];
                            /*
                             * 即使 a[great] 等于 pivot1，赋值 a[less] = pivot1 也可能是不正确的，
                             * 如果 a[great] 和 pivot1 是不同符号的浮点零。因此在 float 和
                             * double 排序方法中，我们必须使用更准确的赋值 a[less] = a[great]。
                             */
                            a[less] = a[great];
                            ++less;
                        } else { // pivot1 < a[great] < pivot2
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        --great;
                    }
                }
            }

            // 递归地对中心部分进行排序
            sort(a, less, great, false);

        } else { // 使用一个枢轴进行分区
            /*
             * 使用五个排序元素中的第三个作为枢轴。
             * 这个值是中位数的便宜近似值。
             */
            float pivot = a[e3];

            /*
             * 分区退化为传统的 3 路（或“荷兰国旗”）方案：
             *
             *   左部分    中心部分              右部分
             * +-------------------------------------------------+
             * |  < pivot  |   == pivot   |     ?    |  > pivot  |
             * +-------------------------------------------------+
             *              ^              ^        ^
             *              |              |        |
             *             less            k      great
             *
             * 不变量：
             *
             *   所有在 (left, less)   < pivot
             *   所有在 [less, k)     == pivot
             *   所有在 (great, right) > pivot
             *
             * 指针 k 是 ?-部分的第一个索引。
             */
            for (int k = less; k <= great; ++k) {
                if (a[k] == pivot) {
                    continue;
                }
                float ak = a[k];
                if (ak < pivot) { // 将 a[k] 移动到左部分
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else { // a[k] > pivot - 将 a[k] 移动到右部分
                    while (a[great] > pivot) {
                        --great;
                    }
                    if (a[great] < pivot) { // a[great] <= pivot
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // a[great] == pivot
                        /*
                         * 即使 a[great] 等于 pivot，赋值 a[k] = pivot 也可能是不正确的，
                         * 如果 a[great] 和 pivot 是不同符号的浮点零。因此在 float
                         * 和 double 排序方法中，我们必须使用更准确的赋值 a[k] = a[great]。
                         */
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    --great;
                }
            }
        }
    }


                        /*
             * 递归地对左右部分进行排序。
             * 中间部分的所有元素都相等
             * 因此，它们已经是排序好的。
             */
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        }
    }

    /**
     * 对数组的指定范围进行排序，如果可能的话使用给定的工作空间数组切片进行合并
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param work 工作空间数组（切片）
     * @param workBase 工作数组中可用空间的起始位置
     * @param workLen 工作数组的可用大小
     */
    static void sort(double[] a, int left, int right,
                     double[] work, int workBase, int workLen) {
        /*
         * 第一阶段：将 NaN 移到数组末尾。
         */
        while (left <= right && Double.isNaN(a[right])) {
            --right;
        }
        for (int k = right; --k >= left; ) {
            double ak = a[k];
            if (ak != ak) { // a[k] 是 NaN
                a[k] = a[right];
                a[right] = ak;
                --right;
            }
        }

        /*
         * 第二阶段：对除了 NaN 以外的所有元素进行排序（NaN 已经在正确的位置）。
         */
        doSort(a, left, right, work, workBase, workLen);

        /*
         * 第三阶段：将负零放在正零之前。
         */
        int hi = right;

        /*
         * 查找第一个零，或第一个正数，或最后一个负数元素。
         */
        while (left < hi) {
            int middle = (left + hi) >>> 1;
            double middleValue = a[middle];

            if (middleValue < 0.0d) {
                left = middle + 1;
            } else {
                hi = middle;
            }
        }

        /*
         * 跳过最后一个负值（如果有）或所有前导负零。
         */
        while (left <= right && Double.doubleToRawLongBits(a[left]) < 0) {
            ++left;
        }

        /*
         * 将负零移动到子范围的开头。
         *
         * 分区：
         *
         * +----------------------------------------------------+
         * |   < 0.0   |   -0.0   |   0.0   |   ?  ( >= 0.0 )   |
         * +----------------------------------------------------+
         *              ^          ^         ^
         *              |          |         |
         *             left        p         k
         *
         * 不变量：
         *
         *   all in (*,  left)  <  0.0
         *   all in [left,  p) == -0.0
         *   all in [p,     k) ==  0.0
         *   all in [k, right] >=  0.0
         *
         * 指针 k 是 ?-部分的第一个索引。
         */
        for (int k = left, p = left - 1; ++k <= right; ) {
            double ak = a[k];
            if (ak != 0.0d) {
                break;
            }
            if (Double.doubleToRawLongBits(ak) < 0) { // ak 是 -0.0d
                a[k] = 0.0d;
                a[++p] = -0.0d;
            }
        }
    }

    /**
     * 对数组的指定范围进行排序。
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param work 工作空间数组（切片）
     * @param workBase 工作数组中可用空间的起始位置
     * @param workLen 工作数组的可用大小
     */
    private static void doSort(double[] a, int left, int right,
                               double[] work, int workBase, int workLen) {
        // 对小数组使用快速排序
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }

        /*
         * 索引 run[i] 是第 i 个运行的起始位置
         * （升序或降序序列）。
         */
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0; run[0] = left;

        // 检查数组是否几乎已排序
        for (int k = left; k < right; run[count] = k) {
            if (a[k] < a[k + 1]) { // 升序
                while (++k <= right && a[k - 1] <= a[k]);
            } else if (a[k] > a[k + 1]) { // 降序
                while (++k <= right && a[k - 1] >= a[k]);
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    double t = a[lo]; a[lo] = a[hi]; a[hi] = t;
                }
            } else { // 相等
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    if (--m == 0) {
                        sort(a, left, right, true);
                        return;
                    }
                }
            }

            /*
             * 数组不是高度结构化的，
             * 使用快速排序而不是归并排序。
             */
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }

        // 检查特殊情况
        // 实现注释：变量 "right" 增加了 1。
        if (run[count] == right++) { // 最后一个运行包含一个元素
            run[++count] = right;
        } else if (count == 1) { // 数组已经排序
            return;
        }

        // 确定合并的交替基数
        byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1);

        // 使用或创建临时数组 b 进行合并
        double[] b;                 // 临时数组；与 a 交替
        int ao, bo;              // 数组偏移量从 'left'
        int blen = right - left; // b 需要的空间
        if (work == null || workLen < blen || workBase + blen > work.length) {
            work = new double[blen];
            workBase = 0;
        }
        if (odd == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }

        // 合并
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
                        b[i + bo] = a[p++ + ao];
                    } else {
                        b[i + bo] = a[q++ + ao];
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                    b[i + bo] = a[i + ao]
                );
                run[++last] = right;
            }
            double[] t = a; a = b; b = t;
            int o = ao; ao = bo; bo = o;
        }
    }

    /**
     * 使用双轴快速排序对数组的指定范围进行排序。
     *
     * @param a 要排序的数组
     * @param left 要排序的第一个元素的索引（包含）
     * @param right 要排序的最后一个元素的索引（包含）
     * @param leftmost 表示这部分是否是范围的最左边
     */
    private static void sort(double[] a, int left, int right, boolean leftmost) {
        int length = right - left + 1;

        // 对非常小的数组使用插入排序
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                /*
                 * 传统的（没有哨兵）插入排序，
                 * 优化用于服务器 VM，在最左边部分使用。
                 */
                for (int i = left, j = i; i < right; j = ++i) {
                    double ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        if (j-- == left) {
                            break;
                        }
                    }
                    a[j + 1] = ai;
                }
            } else {
                /*
                 * 跳过最长的升序序列。
                 */
                do {
                    if (left >= right) {
                        return;
                    }
                } while (a[++left] >= a[left - 1]);

                /*
                 * 每个相邻部分的元素都充当哨兵，因此这允许我们在每次迭代中避免左范围检查。此外，我们使用了更优化的算法，即所谓的成对插入排序，这在快速排序的上下文中比传统的插入排序实现更快。
                 */
                for (int k = left; ++left <= right; k = ++left) {
                    double a1 = a[k], a2 = a[left];

                    if (a1 < a2) {
                        a2 = a1; a1 = a[left];
                    }
                    while (a1 < a[--k]) {
                        a[k + 2] = a[k];
                    }
                    a[++k + 1] = a1;

                    while (a2 < a[--k]) {
                        a[k + 1] = a[k];
                    }
                    a[k + 1] = a2;
                }
                double last = a[right];

                while (last < a[--right]) {
                    a[right + 1] = a[right];
                }
                a[right + 1] = last;
            }
            return;
        }

        // 便宜的 length / 7 近似
        int seventh = (length >> 3) + (length >> 6) + 1;

        /*
         * 对范围中（包括）中心元素周围的五个均匀分布的元素进行排序。这些元素将用于枢轴选择，如下所述。这些元素的选择是通过经验确定的，适用于各种输入。
         */
        int e3 = (left + right) >>> 1; // 中点
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;

        // 使用插入排序对这些元素进行排序
        if (a[e2] < a[e1]) { double t = a[e2]; a[e2] = a[e1]; a[e1] = t; }

        if (a[e3] < a[e2]) { double t = a[e3]; a[e3] = a[e2]; a[e2] = t;
            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
        }
        if (a[e4] < a[e3]) { double t = a[e4]; a[e4] = a[e3]; a[e3] = t;
            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
            }
        }
        if (a[e5] < a[e4]) { double t = a[e5]; a[e5] = a[e4]; a[e4] = t;
            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                }
            }
        }

        // 指针
        int less  = left;  // 中心部分第一个元素的索引
        int great = right; // 右部分第一个元素前的索引

        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            /*
             * 使用五个排序元素中的第二个和第四个作为枢轴。
             * 这些值是数组第一和第二三分位的廉价近似。注意 pivot1 <= pivot2。
             */
            double pivot1 = a[e2];
            double pivot2 = a[e4];

            /*
             * 第一个和最后一个要排序的元素被移动到枢轴之前的位置。当分区完成时，枢轴被交换回它们的最终位置，并从后续排序中排除。
             */
            a[e2] = a[left];
            a[e4] = a[right];

            /*
             * 跳过小于或大于枢轴值的元素。
             */
            while (a[++less] < pivot1);
            while (a[--great] > pivot2);

            /*
             * 分区：
             *
             *   左部分           中心部分                   右部分
             * +--------------------------------------------------------------+
             * |  < pivot1  |  pivot1 <= && <= pivot2  |    ?    |  > pivot2  |
             * +--------------------------------------------------------------+
             *               ^                          ^       ^
             *               |                          |       |
             *              less                        k     great
             *
             * 不变量：
             *
             *              all in (left, less)   < pivot1
             *    pivot1 <= all in [less, k)     <= pivot2
             *              all in (great, right) > pivot2
             *
             * 指针 k 是 ?-部分的第一个索引。
             */
            outer:
            for (int k = less - 1; ++k <= great; ) {
                double ak = a[k];
                if (ak < pivot1) { // 将 a[k] 移到左部分
                    a[k] = a[less];
                    /*
                     * 这里和下面我们使用 "a[i] = b; i++;" 而不是 "a[i++] = b;" 由于性能问题。
                     */
                    a[less] = ak;
                    ++less;
                } else if (ak > pivot2) { // 将 a[k] 移到右部分
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) { // a[great] <= pivot2
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // pivot1 <= a[great] <= pivot2
                        a[k] = a[great];
                    }
                    /*
                     * 这里和下面我们使用 "a[i] = b; i--;" 而不是 "a[i--] = b;" 由于性能问题。
                     */
                    a[great] = ak;
                    --great;
                }
            }

            // 将枢轴交换到它们的最终位置
            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
            a[right] = a[great + 1]; a[great + 1] = pivot2;

            // 递归地对左右部分进行排序，排除已知的枢轴
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);

            /*
             * 如果中心部分太大（超过数组的 4/7），
             * 将内部枢轴值交换到两端。
             */
            if (less < e1 && e5 < great) {
                /*
                 * 跳过等于枢轴值的元素。
                 */
                while (a[less] == pivot1) {
                    ++less;
                }

                while (a[great] == pivot2) {
                    --great;
                }


                            /*
                 * 分区：
                 *
                 *   左部分         中心部分                  右部分
                 * +----------------------------------------------------------+
                 * | == pivot1 |  pivot1 < && < pivot2  |    ?    | == pivot2 |
                 * +----------------------------------------------------------+
                 *              ^                        ^       ^
                 *              |                        |       |
                 *             less                      k     great
                 *
                 * 不变量：
                 *
                 *              所有在 (*,  less) == pivot1
                 *     pivot1 < 所有在 [less,  k)  < pivot2
                 *              所有在 (great, *) == pivot2
                 *
                 * 指针 k 是 ?-部分的第一个索引。
                 */
                outer:
                for (int k = less - 1; ++k <= great; ) {
                    double ak = a[k];
                    if (ak == pivot1) { // 将 a[k] 移动到左部分
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else if (ak == pivot2) { // 将 a[k] 移动到右部分
                        while (a[great] == pivot2) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (a[great] == pivot1) { // a[great] < pivot2
                            a[k] = a[less];
                            /*
                             * 即使 a[great] 等于 pivot1，赋值 a[less] = pivot1 可能是不正确的，
                             * 如果 a[great] 和 pivot1 是不同符号的浮点零。因此在 float 和
                             * double 排序方法中，我们必须使用更准确的赋值 a[less] = a[great]。
                             */
                            a[less] = a[great];
                            ++less;
                        } else { // pivot1 < a[great] < pivot2
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        --great;
                    }
                }
            }

            // 递归排序中心部分
            sort(a, less, great, false);

        } else { // 单枢轴分区
            /*
             * 使用五个排序元素中的第三个作为枢轴。
             * 这个值是对中位数的廉价近似。
             */
            double pivot = a[e3];

            /*
             * 分区退化为传统的 3 路
             * （或“荷兰国旗”）模式：
             *
             *   左部分    中心部分              右部分
             * +-------------------------------------------------+
             * |  < pivot  |   == pivot   |     ?    |  > pivot  |
             * +-------------------------------------------------+
             *              ^              ^        ^
             *              |              |        |
             *             less            k      great
             *
             * 不变量：
             *
             *   所有在 (left, less)   < pivot
             *   所有在 [less, k)     == pivot
             *   所有在 (great, right) > pivot
             *
             * 指针 k 是 ?-部分的第一个索引。
             */
            for (int k = less; k <= great; ++k) {
                if (a[k] == pivot) {
                    continue;
                }
                double ak = a[k];
                if (ak < pivot) { // 将 a[k] 移动到左部分
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else { // a[k] > pivot - 将 a[k] 移动到右部分
                    while (a[great] > pivot) {
                        --great;
                    }
                    if (a[great] < pivot) { // a[great] <= pivot
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { // a[great] == pivot
                        /*
                         * 即使 a[great] 等于 pivot，赋值 a[k] = pivot 可能是不正确的，
                         * 如果 a[great] 和 pivot 是不同符号的浮点零。因此在 float
                         * 和 double 排序方法中，我们必须使用更准确的赋值 a[k] = a[great]。
                         */
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    --great;
                }
            }

            /*
             * 递归排序左部分和右部分。
             * 中心部分的所有元素都相等
             * 因此已经排序。
             */
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        }
    }
}
