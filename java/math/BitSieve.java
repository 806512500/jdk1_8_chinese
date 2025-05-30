/*
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
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

package java.math;

/**
 * 一个用于查找质数候选的简单位筛。允许在存储数组中设置和清除位。假设筛子的大小是固定的，以减少开销。新位筛的所有位都是零，位通过设置它们来移除。
 *
 * 为了减少存储空间并提高效率，筛子中不表示偶数（每个位表示一个奇数）。位的索引和它表示的数之间的关系由以下公式给出：
 * N = offset + (2*index + 1);
 * 其中 N 是筛子中一个位表示的整数，offset 是某个偶数偏移量，表示筛子的起始位置，index 是筛子数组中的位索引。
 *
 * @see     BigInteger
 * @author  Michael McCloskey
 * @since   1.3
 */
class BitSieve {
    /**
     * 存储此位筛中的位。
     */
    private long bits[];

    /**
     * 长度是此筛子包含的位数。
     */
    private int length;

    /**
     * 用于在搜索筛子中过滤掉小质数的倍数的小筛子。
     */
    private static BitSieve smallSieve = new BitSieve();

    /**
     * 构造一个以 0 为基的“小筛子”。此构造函数用于内部生成“小质数”集，这些质数的倍数将从主（包私有）构造函数生成的筛子中排除，即 BitSieve(BigInteger base, int searchLen)。此构造函数生成的筛子的长度是根据性能选择的；它控制着构造其他筛子所花费的时间与测试合数候选数的质数性所浪费的时间之间的权衡。长度是通过实验选择的，以获得良好的性能。
     */
    private BitSieve() {
        length = 150 * 64;
        bits = new long[(unitIndex(length - 1) + 1)];

        // 标记 1 为合数
        set(0);
        int nextIndex = 1;
        int nextPrime = 3;

        // 查找质数并从筛子中移除它们的倍数
        do {
            sieveSingle(length, nextIndex + nextPrime, nextPrime);
            nextIndex = sieveSearch(length, nextIndex + 1);
            nextPrime = 2 * nextIndex + 1;
        } while ((nextIndex > 0) && (nextPrime < length));
    }

    /**
     * 构造一个用于查找质数候选的搜索长度为 searchLen 位的位筛。新的筛子从指定的基开始，基必须是偶数。
     */
    BitSieve(BigInteger base, int searchLen) {
        /*
         * 候选数由筛子中的清除位表示。当计算出候选数的非质数性时，会在筛子中设置一个位以消除它。为了减少存储空间并提高效率，筛子中不表示偶数（每个位表示一个奇数）。
         */
        bits = new long[(unitIndex(searchLen - 1) + 1)];
        length = searchLen;
        int start = 0;

        int step = smallSieve.sieveSearch(smallSieve.length, start);
        int convertedStep = (step * 2) + 1;

        // 在由基指定的偶数偏移处构造大筛子
        MutableBigInteger b = new MutableBigInteger(base);
        MutableBigInteger q = new MutableBigInteger();
        do {
            // 计算 base mod convertedStep
            start = b.divideOneWord(convertedStep, q);

            // 从筛子中移除 step 的每个倍数
            start = convertedStep - start;
            if (start % 2 == 0)
                start += convertedStep;
            sieveSingle(searchLen, (start - 1) / 2, convertedStep);

            // 从小筛子中找到下一个质数
            step = smallSieve.sieveSearch(smallSieve.length, step + 1);
            convertedStep = (step * 2) + 1;
        } while (step > 0);
    }

    /**
     * 给定一个位索引，返回包含它的单元索引。
     */
    private static int unitIndex(int bitIndex) {
        return bitIndex >>> 6;
    }

    /**
     * 返回一个掩码，该掩码在单元中指定位。
     */
    private static long bit(int bitIndex) {
        return 1L << (bitIndex & ((1 << 6) - 1));
    }

    /**
     * 获取指定索引处的位的值。
     */
    private boolean get(int bitIndex) {
        int unitIndex = unitIndex(bitIndex);
        return ((bits[unitIndex] & bit(bitIndex)) != 0);
    }

    /**
     * 设置指定索引处的位。
     */
    private void set(int bitIndex) {
        int unitIndex = unitIndex(bitIndex);
        bits[unitIndex] |= bit(bitIndex);
    }

    /**
     * 返回从 start 开始在搜索数组中第一个清除位的索引。不会搜索超过指定的限制。如果没有这样的清除位，则返回 -1。
     */
    private int sieveSearch(int limit, int start) {
        if (start >= limit)
            return -1;

        int index = start;
        do {
            if (!get(index))
                return index;
            index++;
        } while (index < limit - 1);
        return -1;
    }

    /**
     * 从筛子中移除指定步长的单个倍数集。从指定的开始索引开始移除指定步长的倍数，直到指定的限制。
     */
    private void sieveSingle(int limit, int start, int step) {
        while (start < limit) {
            set(start);
            start += step;
        }
    }

    /**
     * 测试筛子中的可能质数并返回成功的候选数。
     */
    BigInteger retrieve(BigInteger initValue, int certainty, java.util.Random random) {
        // 一次检查筛子中的一个 long 以查找可能的质数
        int offset = 1;
        for (int i = 0; i < bits.length; i++) {
            long nextLong = ~bits[i];
            for (int j = 0; j < 64; j++) {
                if ((nextLong & 1) == 1) {
                    BigInteger candidate = initValue.add(
                                           BigInteger.valueOf(offset));
                    if (candidate.primeToCertainty(certainty, random))
                        return candidate;
                }
                nextLong >>>= 1;
                offset += 2;
            }
        }
        return null;
    }
}
