
/*
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

package java.math;

/**
 * 一个用于查找质数候选人的简单位筛。允许在存储数组中设置和清除位。假设位筛的大小是固定的，以减少开销。所有新位筛的位都是零，通过设置它们来从位筛中移除位。
 *
 * 为了减少存储空间并提高效率，位筛中不表示偶数（位筛中的每个位表示一个奇数）。位的索引和它所表示的数字之间的关系由以下公式给出：
 * N = offset + (2*index + 1);
 * 其中 N 是位筛中位表示的整数，offset 是一个偶数偏移量，表示位筛的起始位置，index 是位筛数组中位的索引。
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
     * 一个小筛子，用于在搜索筛子中过滤掉小质数的倍数。
     */
    private static BitSieve smallSieve = new BitSieve();

    /**
     * 构建一个基数为 0 的“小筛子”。此构造函数用于内部生成一组“小质数”，这些质数的倍数将从主（包私有）构造函数生成的筛子中排除，即 BitSieve(BigInteger base, int searchLen)。此构造函数生成的筛子的长度是根据性能选择的；它控制了构建其他筛子所需时间和测试复合候选数的质数性所浪费的时间之间的权衡。长度是通过实验选择的，以获得良好的性能。
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
     * 构建一个用于查找质数候选人的位筛，包含 searchLen 位。新的筛子从指定的基数开始，该基数必须是偶数。
     */
    BitSieve(BigInteger base, int searchLen) {
        /*
         * 候选人由筛子中的清除位表示。当计算出候选人的非质数性时，会在筛子中设置一个位以消除它。为了减少存储空间并提高效率，筛子中不表示偶数（每个位表示一个奇数）。
         */
        bits = new long[(unitIndex(searchLen - 1) + 1)];
        length = searchLen;
        int start = 0;

        int step = smallSieve.sieveSearch(smallSieve.length, start);
        int convertedStep = (step * 2) + 1;

        // 在由基数指定的偶数偏移处构建大筛子
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
     * 给定一个位索引，返回包含该位的单元索引。
     */
    private static int unitIndex(int bitIndex) {
        return bitIndex >>> 6;
    }

    /**
     * 返回一个在指定位处设置掩码的单元。
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
     * 返回从 start 开始或之后在搜索数组中第一个清除位的索引。不会搜索超过指定的限制。如果没有这样的清除位，则返回 -1。
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
     * 从筛子中筛选出一组倍数。从指定的起始索引开始，移除指定步长的倍数，直到指定的限制。
     */
    private void sieveSingle(int limit, int start, int step) {
        while (start < limit) {
            set(start);
            start += step;
        }
    }

    /**
     * 测试筛子中的可能质数并返回成功的候选人。
     */
    BigInteger retrieve(BigInteger initValue, int certainty, java.util.Random random) {
        // 一次检查筛子中的一个 long 以找到可能的质数
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
