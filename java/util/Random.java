
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.io.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

import sun.misc.Unsafe;

/**
 * 该类的实例用于生成伪随机数流。该类使用48位种子，通过线性同余公式进行修改。（参见Donald Knuth，
 * 《计算机程序设计艺术，第2卷》，第3.2.1节。）
 * <p>
 * 如果使用相同的种子创建两个 {@code Random} 实例，并且对每个实例调用相同的方法序列，
 * 它们将生成并返回相同的数字序列。为了保证这一属性，为 {@code Random} 类指定了特定的算法。
 * Java 实现必须使用此处显示的所有算法，以确保 Java 代码的绝对可移植性。
 * 但是，允许 {@code Random} 类的子类使用其他算法，只要它们遵守所有方法的一般合同即可。
 * <p>
 * 该类实现的算法使用一个 {@code protected} 实用方法，每次调用时最多可以提供32位伪随机生成的位。
 * <p>
 * 许多应用程序会发现使用 {@link Math#random} 方法更简单。
 *
 * <p>{@code java.util.Random} 的实例是线程安全的。
 * 但是，在多个线程中并发使用同一个 {@code java.util.Random} 实例可能会遇到竞争和随之而来的性能下降。
 * 考虑在多线程设计中使用 {@link java.util.concurrent.ThreadLocalRandom}。
 *
 * <p>{@code java.util.Random} 的实例不是密码学安全的。
 * 考虑使用 {@link java.security.SecureRandom} 为安全敏感的应用程序提供密码学安全的伪随机数生成器。
 *
 * @author  Frank Yellin
 * @since   1.0
 */
public
class Random implements java.io.Serializable {
    /** 用于互操作性的 JDK 1.1 的 serialVersionUID */
    static final long serialVersionUID = 3905348978240129619L;

    /**
     * 与这个伪随机数生成器关联的内部状态。
     * （该类的方法规格描述了此值的持续计算。）
     */
    private final AtomicLong seed;

    private static final long multiplier = 0x5DEECE66DL;
    private static final long addend = 0xBL;
    private static final long mask = (1L << 48) - 1;

    private static final double DOUBLE_UNIT = 0x1.0p-53; // 1.0 / (1L << 53)

    // IllegalArgumentException 消息
    static final String BadBound = "bound must be positive";
    static final String BadRange = "bound must be greater than origin";
    static final String BadSize  = "size must be non-negative";

    /**
     * 创建一个新的随机数生成器。此构造函数将随机数生成器的种子设置为一个非常可能
     * 与该构造函数的任何其他调用不同的值。
     */
    public Random() {
        this(seedUniquifier() ^ System.nanoTime());
    }

    private static long seedUniquifier() {
        // L'Ecuyer, "Tables of Linear Congruential Generators of
        // Different Sizes and Good Lattice Structure", 1999
        for (;;) {
            long current = seedUniquifier.get();
            long next = current * 181783497276652981L;
            if (seedUniquifier.compareAndSet(current, next))
                return next;
        }
    }

    private static final AtomicLong seedUniquifier
        = new AtomicLong(8682522807148012L);

    /**
     * 使用单个 {@code long} 种子创建一个新的随机数生成器。
     * 种子是伪随机数生成器内部状态的初始值，该状态由方法 {@link #next} 维护。
     *
     * <p>调用 {@code new Random(seed)} 等效于：
     *  <pre> {@code
     * Random rnd = new Random();
     * rnd.setSeed(seed);}</pre>
     *
     * @param seed 初始种子
     * @see   #setSeed(long)
     */
    public Random(long seed) {
        if (getClass() == Random.class)
            this.seed = new AtomicLong(initialScramble(seed));
        else {
            // 子类可能已覆盖 setSeed
            this.seed = new AtomicLong();
            setSeed(seed);
        }
    }

    private static long initialScramble(long seed) {
        return (seed ^ multiplier) & mask;
    }

    /**
     * 使用单个 {@code long} 种子设置此随机数生成器的种子。{@code setSeed} 的一般合同是
     * 它会改变此随机数生成器对象的状态，使其处于与使用参数 {@code seed} 作为种子刚刚创建时完全相同的状态。
     * {@code setSeed} 方法由 {@code Random} 类通过原子更新种子实现为
     *  <pre>{@code (seed ^ 0x5DEECE66DL) & ((1L << 48) - 1)}</pre>
     * 并清除 {@link #nextGaussian} 使用的 {@code haveNextNextGaussian} 标志。
     *
     * <p>{@code Random} 类实现的 {@code setSeed} 恰好使用给定种子的48位。但是，
     * 通常情况下，重写方法可能会使用 {@code long} 参数的所有64位作为种子值。
     *
     * @param seed 初始种子
     */
    synchronized public void setSeed(long seed) {
        this.seed.set(initialScramble(seed));
        haveNextNextGaussian = false;
    }


                /**
     * 生成下一个伪随机数。子类应覆盖此方法，因为其他所有方法都使用此方法。
     *
     * <p>{@code next} 的一般约定是它返回一个 {@code int} 值，如果参数 {@code bits} 在
     * {@code 1} 和 {@code 32}（包括）之间，那么返回值的低位中将有这么多位（大约）独立选择的位值，
     * 每个位值（大约）等可能为 {@code 0} 或 {@code 1}。{@code Random} 类通过原子更新种子来实现 {@code next} 方法：
     *  <pre>{@code (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1)}</pre>
     * 并返回
     *  <pre>{@code (int)(seed >>> (48 - bits))}.</pre>
     *
     * 这是一个线性同余伪随机数生成器，由 D. H. Lehmer 定义，并由 Donald E. Knuth 在
     * <i>计算机程序设计艺术，</i> 第 3 卷：<i>半数值算法</i> 第 3.2.1 节中描述。
     *
     * @param  bits 随机位
     * @return 从此随机数生成器序列中返回的下一个伪随机值
     * @since  1.1
     */
    protected int next(int bits) {
        long oldseed, nextseed;
        AtomicLong seed = this.seed;
        do {
            oldseed = seed.get();
            nextseed = (oldseed * multiplier + addend) & mask;
        } while (!seed.compareAndSet(oldseed, nextseed));
        return (int)(nextseed >>> (48 - bits));
    }

    /**
     * 生成随机字节并将其放入用户提供的字节数组中。生成的随机字节数等于字节数组的长度。
     *
     * <p>{@code nextBytes} 方法由 {@code Random} 类实现，如下所示：
     *  <pre> {@code
     * public void nextBytes(byte[] bytes) {
     *   for (int i = 0; i < bytes.length; )
     *     for (int rnd = nextInt(), n = Math.min(bytes.length - i, 4);
     *          n-- > 0; rnd >>= 8)
     *       bytes[i++] = (byte)rnd;
     * }}</pre>
     *
     * @param  bytes 要填充随机字节的字节数组
     * @throws NullPointerException 如果字节数组为 null
     * @since  1.1
     */
    public void nextBytes(byte[] bytes) {
        for (int i = 0, len = bytes.length; i < len; )
            for (int rnd = nextInt(),
                     n = Math.min(len - i, Integer.SIZE/Byte.SIZE);
                 n-- > 0; rnd >>= Byte.SIZE)
                bytes[i++] = (byte)rnd;
    }

    /**
     * 由 LongStream Spliterators 使用的 nextLong 形式。如果 origin 大于 bound，则作为无界形式的 nextLong，
     * 否则作为有界形式的 nextLong。
     *
     * @param origin 最小值，除非大于 bound
     * @param bound 上界（不包括），不得等于 origin
     * @return 伪随机值
     */
    final long internalNextLong(long origin, long bound) {
        long r = nextLong();
        if (origin < bound) {
            long n = bound - origin, m = n - 1;
            if ((n & m) == 0L)  // 2 的幂
                r = (r & m) + origin;
            else if (n > 0L) {  // 拒绝过度表示的候选值
                for (long u = r >>> 1;            // 确保非负
                     u + m - (r = u % n) < 0L;    // 拒绝检查
                     u = nextLong() >>> 1) // 重试
                    ;
                r += origin;
            }
            else {              // 范围不能表示为 long
                while (r < origin || r >= bound)
                    r = nextLong();
            }
        }
        return r;
    }

    /**
     * 由 IntStream Spliterators 使用的 nextInt 形式。
     * 对于无界情况：使用 nextInt()。
     * 对于可表示范围的有界情况：使用 nextInt(int bound)。
     * 对于不可表示范围的有界情况：使用 nextInt()。
     *
     * @param origin 最小值，除非大于 bound
     * @param bound 上界（不包括），不得等于 origin
     * @return 伪随机值
     */
    final int internalNextInt(int origin, int bound) {
        if (origin < bound) {
            int n = bound - origin;
            if (n > 0) {
                return nextInt(n) + origin;
            }
            else {  // 范围不能表示为 int
                int r;
                do {
                    r = nextInt();
                } while (r < origin || r >= bound);
                return r;
            }
        }
        else {
            return nextInt();
        }
    }

    /**
     * 由 DoubleStream Spliterators 使用的 nextDouble 形式。
     *
     * @param origin 最小值，除非大于 bound
     * @param bound 上界（不包括），不得等于 origin
     * @return 伪随机值
     */
    final double internalNextDouble(double origin, double bound) {
        double r = nextDouble();
        if (origin < bound) {
            r = r * (bound - origin) + origin;
            if (r >= bound) // 修正舍入
                r = Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
        }
        return r;
    }

    /**
     * 返回从此随机数生成器序列中生成的下一个伪随机、均匀分布的 {@code int} 值。{@code nextInt} 的一般约定是
     * 生成并返回一个 {@code int} 值。所有 2<sup>32</sup> 个可能的 {@code int} 值都以（大约）相等的概率生成。
     *
     * <p>{@code nextInt} 方法由 {@code Random} 类实现如下：
     *  <pre> {@code
     * public int nextInt() {
     *   return next(32);
     * }}</pre>
     *
     * @return 从此随机数生成器序列中生成的下一个伪随机、均匀分布的 {@code int} 值
     */
    public int nextInt() {
        return next(32);
    }

    /**
     * 返回从此随机数生成器序列中生成的伪随机、均匀分布的 {@code int} 值，该值在 0（包括）和指定值（不包括）之间。
     * {@code nextInt} 的一般约定是生成并返回指定范围内的一个 {@code int} 值。所有 {@code bound} 个可能的
     * {@code int} 值都以（大约）相等的概率生成。{@code Random} 类实现的 {@code nextInt(int bound)} 方法如下：
     *  <pre> {@code
     * public int nextInt(int bound) {
     *   if (bound <= 0)
     *     throw new IllegalArgumentException("bound must be positive");
     *
     *   if ((bound & -bound) == bound)  // 即，bound 是 2 的幂
     *     return (int)((bound * (long)next(31)) >> 31);
     *
     *   int bits, val;
     *   do {
     *       bits = next(31);
     *       val = bits % bound;
     *   } while (bits - val + (bound-1) < 0);
     *   return val;
     * }}</pre>
     *
     * <p>在前述描述中使用了“大约”一词，仅因为 next 方法只是大约是一个无偏的独立选择位的源。如果它是一个完美的随机选择位的源，
     * 那么所示算法将从所述范围中以完美的均匀性选择 {@code int} 值。
     * <p>
     * 该算法稍微有些复杂。它拒绝会导致分布不均的值（由于 2^31 不能被 n 整除）。值被拒绝的概率取决于 n。最坏的情况是 n=2^30+1，
     * 在这种情况下，被拒绝的概率是 1/2，循环终止前的预期迭代次数是 2。
     * <p>
     * 该算法对 n 是 2 的幂的情况进行了特殊处理：它返回底层伪随机数生成器的正确数量的高位。如果没有特殊处理，
     * 则会返回正确数量的 <i>低位</i>。线性同余伪随机数生成器（如本类实现的）已知其低位的周期较短。因此，这种特殊情况大大增加了
     * 如果 n 是 2 的小幂，连续调用此方法返回的值序列的长度。
     *
     * @param bound 上界（不包括）。必须为正。
     * @return 从此随机数生成器序列中生成的下一个伪随机、均匀分布的 {@code int} 值，该值在零（包括）和 {@code bound}（不包括）之间
     * @throws IllegalArgumentException 如果 bound 不为正
     * @since 1.2
     */
    public int nextInt(int bound) {
        if (bound <= 0)
            throw new IllegalArgumentException(BadBound);


                    int r = next(31);
        int m = bound - 1;
        if ((bound & m) == 0)  // 即，bound 是 2 的幂
            r = (int)((bound * (long)r) >> 31);
        else {
            for (int u = r;
                 u - (r = u % bound) + m < 0;
                 u = next(31))
                ;
        }
        return r;
    }

    /**
     * 返回此随机数生成器序列中的下一个伪随机、均匀分布的 {@code long}
     * 值。{@code nextLong} 的一般约定是生成并返回一个 {@code long} 值。
     *
     * <p>{@code nextLong} 方法由 {@code Random} 类实现如下：
     *  <pre> {@code
     * public long nextLong() {
     *   return ((long)next(32) << 32) + next(32);
     * }}</pre>
     *
     * 由于 {@code Random} 类使用只有 48 位的种子，
     * 这种算法不会返回所有可能的 {@code long} 值。
     *
     * @return 从此随机数生成器序列中返回的下一个伪随机、均匀分布的 {@code long}
     *         值
     */
    public long nextLong() {
        // 底部字保持有符号是可以的。
        return ((long)(next(32)) << 32) + next(32);
    }

    /**
     * 返回此随机数生成器序列中的下一个伪随机、均匀分布的
     * {@code boolean} 值。{@code nextBoolean} 的一般约定是生成并返回一个
     * {@code boolean} 值。{@code true} 和 {@code false} 的值
     * 以（大约）相等的概率生成。
     *
     * <p>{@code nextBoolean} 方法由 {@code Random} 类实现如下：
     *  <pre> {@code
     * public boolean nextBoolean() {
     *   return next(1) != 0;
     * }}</pre>
     *
     * @return 从此随机数生成器序列中返回的下一个伪随机、均匀分布的
     *         {@code boolean} 值
     * @since 1.2
     */
    public boolean nextBoolean() {
        return next(1) != 0;
    }

    /**
     * 返回此随机数生成器序列中的下一个伪随机、均匀分布的 {@code float}
     * 值，范围在 {@code 0.0} 和 {@code 1.0} 之间。
     *
     * <p>{@code nextFloat} 的一般约定是生成并返回一个
     * {@code float} 值，该值（大约）均匀地从范围 {@code 0.0f}（包含）到 {@code 1.0f}（不包含）中选择。所有 2<sup>24</sup> 个可能的
     * {@code float} 值，形式为 <i>m&nbsp;x&nbsp;</i>2<sup>-24</sup>，
     * 其中 <i>m</i> 是一个小于 2<sup>24</sup> 的正整数，都以（大约）相等的概率生成。
     *
     * <p>{@code nextFloat} 方法由 {@code Random} 类实现如下：
     *  <pre> {@code
     * public float nextFloat() {
     *   return next(24) / ((float)(1 << 24));
     * }}</pre>
     *
     * <p>在上述描述中使用了“大约”一词，仅因为 {@code next} 方法只是大约是一个无偏的独立选择位的来源。如果它是一个完美的随机选择位的来源，那么所示的算法将从所述范围中以完美的均匀性选择 {@code float} 值。<p>
     * [在早期版本的 Java 中，结果错误地计算为：
     *  <pre> {@code
     *   return next(30) / ((float)(1 << 30));}</pre>
     * 这看起来可能等效，甚至更好，但实际上它引入了一点非均匀性，因为浮点数的舍入偏差：低阶位为 0 的可能性略高于为 1 的可能性。]
     *
     * @return 从此随机数生成器序列中返回的下一个伪随机、均匀分布的 {@code float}
     *         值，范围在 {@code 0.0} 和 {@code 1.0} 之间
     */
    public float nextFloat() {
        return next(24) / ((float)(1 << 24));
    }

    /**
     * 返回此随机数生成器序列中的下一个伪随机、均匀分布的
     * {@code double} 值，范围在 {@code 0.0} 和
     * {@code 1.0} 之间。
     *
     * <p>{@code nextDouble} 的一般约定是生成并返回一个
     * {@code double} 值，该值（大约）均匀地从范围 {@code 0.0d}（包含）到 {@code 1.0d}（不包含）中选择。
     *
     * <p>{@code nextDouble} 方法由 {@code Random} 类实现如下：
     *  <pre> {@code
     * public double nextDouble() {
     *   return (((long)next(26) << 27) + next(27))
     *     / (double)(1L << 53);
     * }}</pre>
     *
     * <p>在上述描述中使用了“大约”一词，仅因为 {@code next} 方法只是大约是一个无偏的独立选择位的来源。如果它是一个完美的随机选择位的来源，那么所示的算法将从所述范围中以完美的均匀性选择 {@code double} 值。
     * <p>[在早期版本的 Java 中，结果错误地计算为：
     *  <pre> {@code
     *   return (((long)next(27) << 27) + next(27))
     *     / (double)(1L << 54);}</pre>
     * 这看起来可能等效，甚至更好，但实际上它引入了较大的非均匀性，因为浮点数的舍入偏差：低阶位为 0 的可能性是为 1 的三倍！这种非均匀性在实际中可能不重要，但我们追求完美。]
     *
     * @return 从此随机数生成器序列中返回的下一个伪随机、均匀分布的 {@code double}
     *         值，范围在 {@code 0.0} 和 {@code 1.0} 之间
     * @see Math#random
     */
    public double nextDouble() {
        return (((long)(next(26)) << 27) + next(27)) * DOUBLE_UNIT;
    }


                private double nextNextGaussian;
    private boolean haveNextNextGaussian = false;

    /**
     * 返回从此随机数生成器的序列中生成的下一个伪随机的高斯（“正态”）分布的
     * {@code double} 值，均值为 {@code 0.0}，标准差为 {@code 1.0}。
     * <p>
     * {@code nextGaussian} 的一般约定是，从（大约）通常的
     * 正态分布中选择一个 {@code double} 值，均值为 {@code 0.0}，标准差为
     * {@code 1.0}，并伪随机地生成并返回。
     *
     * <p>{@code nextGaussian} 方法由 {@code Random} 类实现，如同以下线程安全版本：
     *  <pre> {@code
     * private double nextNextGaussian;
     * private boolean haveNextNextGaussian = false;
     *
     * public double nextGaussian() {
     *   if (haveNextNextGaussian) {
     *     haveNextNextGaussian = false;
     *     return nextNextGaussian;
     *   } else {
     *     double v1, v2, s;
     *     do {
     *       v1 = 2 * nextDouble() - 1;   // between -1.0 and 1.0
     *       v2 = 2 * nextDouble() - 1;   // between -1.0 and 1.0
     *       s = v1 * v1 + v2 * v2;
     *     } while (s >= 1 || s == 0);
     *     double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s)/s);
     *     nextNextGaussian = v2 * multiplier;
     *     haveNextNextGaussian = true;
     *     return v1 * multiplier;
     *   }
     * }}</pre>
     * 这使用了 G. E. P. Box, M. E. Muller, 和 G. Marsaglia 的 <i>极坐标方法</i>，
     * 如 Donald E. Knuth 在 <i>计算机程序设计艺术</i> 第 3 卷：<i>半数值算法</i>，
     * 第 3.4.1 节，子节 C，算法 P 中所述。请注意，它以仅调用一次 {@code StrictMath.log}
     * 和一次 {@code StrictMath.sqrt} 的代价生成两个独立的值。
     *
     * @return 从此随机数生成器的序列中生成的下一个伪随机的高斯（“正态”）分布的
     *         {@code double} 值，均值为 {@code 0.0}，标准差为 {@code 1.0}
     */
    synchronized public double nextGaussian() {
        // See Knuth, ACP, Section 3.4.1 Algorithm C.
        if (haveNextNextGaussian) {
            haveNextNextGaussian = false;
            return nextNextGaussian;
        } else {
            double v1, v2, s;
            do {
                v1 = 2 * nextDouble() - 1; // between -1 and 1
                v2 = 2 * nextDouble() - 1; // between -1 and 1
                s = v1 * v1 + v2 * v2;
            } while (s >= 1 || s == 0);
            double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s)/s);
            nextNextGaussian = v2 * multiplier;
            haveNextNextGaussian = true;
            return v1 * multiplier;
        }
    }

    // 流方法，以一种旨在更好地隔离维护目的的小差异的方式编码。

    /**
     * 返回生成给定 {@code streamSize} 数量的伪随机 {@code int} 值的流。
     *
     * <p>伪随机的 {@code int} 值生成方式如同调用了 {@link #nextInt()} 方法。
     *
     * @param streamSize 要生成的值的数量
     * @return 伪随机 {@code int} 值的流
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零
     * @since 1.8
     */
    public IntStream ints(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.intStream
                (new RandomIntsSpliterator
                         (this, 0L, streamSize, Integer.MAX_VALUE, 0),
                 false);
    }

    /**
     * 返回一个实际上无限的伪随机 {@code int} 值的流。
     *
     * <p>伪随机的 {@code int} 值生成方式如同调用了 {@link #nextInt()} 方法。
     *
     * @implNote 此方法实现为等效于 {@code ints(Long.MAX_VALUE)}。
     *
     * @return 伪随机 {@code int} 值的流
     * @since 1.8
     */
    public IntStream ints() {
        return StreamSupport.intStream
                (new RandomIntsSpliterator
                         (this, 0L, Long.MAX_VALUE, Integer.MAX_VALUE, 0),
                 false);
    }

    /**
     * 返回生成给定 {@code streamSize} 数量的伪随机 {@code int} 值的流，每个值都符合给定的
     * 起点（包含）和边界（不包含）。
     *
     * <p>伪随机的 {@code int} 值生成方式如同调用了以下方法，使用起点和边界：
     * <pre> {@code
     * int nextInt(int origin, int bound) {
     *   int n = bound - origin;
     *   if (n > 0) {
     *     return nextInt(n) + origin;
     *   }
     *   else {  // range not representable as int
     *     int r;
     *     do {
     *       r = nextInt();
     *     } while (r < origin || r >= bound);
     *     return r;
     *   }
     * }}</pre>
     *
     * @param streamSize 要生成的值的数量
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的边界（不包含）
     * @return 伪随机 {@code int} 值的流，每个值都有给定的起点（包含）和边界（不包含）
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零，或 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
     * @since 1.8
     */
    public IntStream ints(long streamSize, int randomNumberOrigin,
                          int randomNumberBound) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.intStream
                (new RandomIntsSpliterator
                         (this, 0L, streamSize, randomNumberOrigin, randomNumberBound),
                 false);
    }


                /**
     * 返回一个实际上无限的伪随机 {@code
     * int} 值流，每个值都符合给定的起点（包含）和上限
     * （不包含）。
     *
     * <p>一个伪随机 {@code int} 值的生成方式就像调用了以下方法并传入了起点和上限：
     * <pre> {@code
     * int nextInt(int origin, int bound) {
     *   int n = bound - origin;
     *   if (n > 0) {
     *     return nextInt(n) + origin;
     *   }
     *   else {  // 范围不能表示为 int
     *     int r;
     *     do {
     *       r = nextInt();
     *     } while (r < origin || r >= bound);
     *     return r;
     *   }
     * }}</pre>
     *
     * @implNote 该方法的实现等同于 {@code
     * ints(Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)}。
     *
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的上限（不包含）
     * @return 一个伪随机 {@code int} 值流，
     *         每个值都有给定的起点（包含）和上限（不包含）
     * @throws IllegalArgumentException 如果 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
     * @since 1.8
     */
    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.intStream
                (new RandomIntsSpliterator
                         (this, 0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound),
                 false);
    }

    /**
     * 返回一个生成给定 {@code streamSize} 数量的
     * 伪随机 {@code long} 值的流。
     *
     * <p>一个伪随机 {@code long} 值的生成方式就像调用了 {@link #nextLong()} 方法。
     *
     * @param streamSize 要生成的值的数量
     * @return 一个伪随机 {@code long} 值流
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零
     * @since 1.8
     */
    public LongStream longs(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.longStream
                (new RandomLongsSpliterator
                         (this, 0L, streamSize, Long.MAX_VALUE, 0L),
                 false);
    }

    /**
     * 返回一个实际上无限的伪随机 {@code long}
     * 值流。
     *
     * <p>一个伪随机 {@code long} 值的生成方式就像调用了 {@link #nextLong()} 方法。
     *
     * @implNote 该方法的实现等同于 {@code
     * longs(Long.MAX_VALUE)}。
     *
     * @return 一个伪随机 {@code long} 值流
     * @since 1.8
     */
    public LongStream longs() {
        return StreamSupport.longStream
                (new RandomLongsSpliterator
                         (this, 0L, Long.MAX_VALUE, Long.MAX_VALUE, 0L),
                 false);
    }

    /**
     * 返回一个生成给定 {@code streamSize} 数量的
     * 伪随机 {@code long} 值的流，每个值都符合给定的起点
     * （包含）和上限（不包含）。
     *
     * <p>一个伪随机 {@code long} 值的生成方式就像调用了以下方法并传入了起点和上限：
     * <pre> {@code
     * long nextLong(long origin, long bound) {
     *   long r = nextLong();
     *   long n = bound - origin, m = n - 1;
     *   if ((n & m) == 0L)  // 2的幂
     *     r = (r & m) + origin;
     *   else if (n > 0L) {  // 拒绝过度表示的候选值
     *     for (long u = r >>> 1;            // 确保非负
     *          u + m - (r = u % n) < 0L;    // 拒绝检查
     *          u = nextLong() >>> 1) // 重试
     *         ;
     *     r += origin;
     *   }
     *   else {              // 范围不能表示为 long
     *     while (r < origin || r >= bound)
     *       r = nextLong();
     *   }
     *   return r;
     * }}</pre>
     *
     * @param streamSize 要生成的值的数量
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的上限（不包含）
     * @return 一个伪随机 {@code long} 值流，
     *         每个值都有给定的起点（包含）和上限（不包含）
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零，或 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
     * @since 1.8
     */
    public LongStream longs(long streamSize, long randomNumberOrigin,
                            long randomNumberBound) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.longStream
                (new RandomLongsSpliterator
                         (this, 0L, streamSize, randomNumberOrigin, randomNumberBound),
                 false);
    }

    /**
     * 返回一个实际上无限的伪随机 {@code
     * long} 值流，每个值都符合给定的起点（包含）和上限
     * （不包含）。
     *
     * <p>一个伪随机 {@code long} 值的生成方式就像调用了以下方法并传入了起点和上限：
     * <pre> {@code
     * long nextLong(long origin, long bound) {
     *   long r = nextLong();
     *   long n = bound - origin, m = n - 1;
     *   if ((n & m) == 0L)  // 2的幂
     *     r = (r & m) + origin;
     *   else if (n > 0L) {  // 拒绝过度表示的候选值
     *     for (long u = r >>> 1;            // 确保非负
     *          u + m - (r = u % n) < 0L;    // 拒绝检查
     *          u = nextLong() >>> 1) // 重试
     *         ;
     *     r += origin;
     *   }
     *   else {              // 范围不能表示为 long
     *     while (r < origin || r >= bound)
     *       r = nextLong();
     *   }
     *   return r;
     * }}</pre>
     *
     * @implNote 该方法的实现等同于 {@code
     * longs(Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)}。
     *
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的上限（不包含）
     * @return 一个伪随机 {@code long} 值流，
     *         每个值都有给定的起点（包含）和上限（不包含）
     * @throws IllegalArgumentException 如果 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
     * @since 1.8
     */
    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.longStream
                (new RandomLongsSpliterator
                         (this, 0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound),
                 false);
    }


                /**
     * 返回一个生成给定 {@code streamSize} 个伪随机 {@code double} 值的流，每个值都在零
     * （包含）和一（不包含）之间。
     *
     * <p>一个伪随机 {@code double} 值的生成方式类似于调用方法 {@link #nextDouble()}。
     *
     * @param streamSize 要生成的值的数量
     * @return 一个 {@code double} 值的流
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零
     * @since 1.8
     */
    public DoubleStream doubles(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.doubleStream
                (new RandomDoublesSpliterator
                         (this, 0L, streamSize, Double.MAX_VALUE, 0.0),
                 false);
    }

    /**
     * 返回一个生成几乎无限数量的伪随机 {@code double} 值的流，每个值都在零（包含）和一
     * （不包含）之间。
     *
     * <p>一个伪随机 {@code double} 值的生成方式类似于调用方法 {@link #nextDouble()}。
     *
     * @implNote 此方法的实现等同于 {@code doubles(Long.MAX_VALUE)}。
     *
     * @return 一个伪随机 {@code double} 值的流
     * @since 1.8
     */
    public DoubleStream doubles() {
        return StreamSupport.doubleStream
                (new RandomDoublesSpliterator
                         (this, 0L, Long.MAX_VALUE, Double.MAX_VALUE, 0.0),
                 false);
    }

    /**
     * 返回一个生成给定 {@code streamSize} 个伪随机 {@code double} 值的流，每个值都符合给定的起点
     * （包含）和上限（不包含）。
     *
     * <p>一个伪随机 {@code double} 值的生成方式类似于调用以下方法并传入起点和上限：
     * <pre> {@code
     * double nextDouble(double origin, double bound) {
     *   double r = nextDouble();
     *   r = r * (bound - origin) + origin;
     *   if (r >= bound) // 修正舍入误差
     *     r = Math.nextDown(bound);
     *   return r;
     * }}</pre>
     *
     * @param streamSize 要生成的值的数量
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的上限（不包含）
     * @return 一个伪随机 {@code double} 值的流，每个值都有给定的起点（包含）和上限（不包含）
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零
     * @throws IllegalArgumentException 如果 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
     * @since 1.8
     */
    public DoubleStream doubles(long streamSize, double randomNumberOrigin,
                                double randomNumberBound) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        if (!(randomNumberOrigin < randomNumberBound))
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.doubleStream
                (new RandomDoublesSpliterator
                         (this, 0L, streamSize, randomNumberOrigin, randomNumberBound),
                 false);
    }

    /**
     * 返回一个生成几乎无限数量的伪随机 {@code double} 值的流，每个值都符合给定的起点（包含）和上限
     * （不包含）。
     *
     * <p>一个伪随机 {@code double} 值的生成方式类似于调用以下方法并传入起点和上限：
     * <pre> {@code
     * double nextDouble(double origin, double bound) {
     *   double r = nextDouble();
     *   r = r * (bound - origin) + origin;
     *   if (r >= bound) // 修正舍入误差
     *     r = Math.nextDown(bound);
     *   return r;
     * }}</pre>
     *
     * @implNote 此方法的实现等同于 {@code doubles(Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)}。
     *
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的上限（不包含）
     * @return 一个伪随机 {@code double} 值的流，每个值都有给定的起点（包含）和上限（不包含）
     * @throws IllegalArgumentException 如果 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
     * @since 1.8
     */
    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        if (!(randomNumberOrigin < randomNumberBound))
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.doubleStream
                (new RandomDoublesSpliterator
                         (this, 0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound),
                 false);
    }

    /**
     * 用于 int 流的 Spliterator。我们通过将上限小于起点视为无界，并且将“无限”等同于
     * Long.MAX_VALUE，将四个 int 版本合并到一个类中。对于拆分，它使用标准的二分法。
     * 该类的 long 和 double 版本除了类型外完全相同。
     */
    static final class RandomIntsSpliterator implements Spliterator.OfInt {
        final Random rng;
        long index;
        final long fence;
        final int origin;
        final int bound;
        RandomIntsSpliterator(Random rng, long index, long fence,
                              int origin, int bound) {
            this.rng = rng; this.index = index; this.fence = fence;
            this.origin = origin; this.bound = bound;
        }

        public RandomIntsSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null :
                   new RandomIntsSpliterator(rng, i, index = m, origin, bound);
        }

        public long estimateSize() {
            return fence - index;
        }


    /**
     * 刷新流。
     */
    public void flush() { }

    /**
     * 关闭流。
     */
    public void close() { }

}


                    public int characteristics() {
            return (Spliterator.SIZED | Spliterator.SUBSIZED |
                    Spliterator.NONNULL | Spliterator.IMMUTABLE);
        }

        public boolean tryAdvance(IntConsumer consumer) {
            if (consumer == null) throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                consumer.accept(rng.internalNextInt(origin, bound));
                index = i + 1;
                return true;
            }
            return false;
        }

        public void forEachRemaining(IntConsumer consumer) {
            if (consumer == null) throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                index = f;
                Random r = rng;
                int o = origin, b = bound;
                do {
                    consumer.accept(r.internalNextInt(o, b));
                } while (++i < f);
            }
        }
    }

    /**
     * 长整型流的 Spliterator。
     */
    static final class RandomLongsSpliterator implements Spliterator.OfLong {
        final Random rng;
        long index;
        final long fence;
        final long origin;
        final long bound;
        RandomLongsSpliterator(Random rng, long index, long fence,
                               long origin, long bound) {
            this.rng = rng; this.index = index; this.fence = fence;
            this.origin = origin; this.bound = bound;
        }

        public RandomLongsSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null :
                   new RandomLongsSpliterator(rng, i, index = m, origin, bound);
        }

        public long estimateSize() {
            return fence - index;
        }

        public int characteristics() {
            return (Spliterator.SIZED | Spliterator.SUBSIZED |
                    Spliterator.NONNULL | Spliterator.IMMUTABLE);
        }

        public boolean tryAdvance(LongConsumer consumer) {
            if (consumer == null) throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                consumer.accept(rng.internalNextLong(origin, bound));
                index = i + 1;
                return true;
            }
            return false;
        }

        public void forEachRemaining(LongConsumer consumer) {
            if (consumer == null) throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                index = f;
                Random r = rng;
                long o = origin, b = bound;
                do {
                    consumer.accept(r.internalNextLong(o, b));
                } while (++i < f);
            }
        }

    }

/**
 * 双精度浮点型流的 Spliterator。
 */
static final class RandomDoublesSpliterator implements Spliterator.OfDouble {
    final Random rng;
    long index;
    final long fence;
    final double origin;
    final double bound;
    RandomDoublesSpliterator(Random rng, long index, long fence,
                                double origin, double bound) {
        this.rng = rng; this.index = index; this.fence = fence;
        this.origin = origin; this.bound = bound;
    }

    public RandomDoublesSpliterator trySplit() {
        long i = index, m = (i + fence) >>> 1;
        return (m <= i) ? null :
                new RandomDoublesSpliterator(rng, i, index = m, origin, bound);
    }

    public long estimateSize() {
        return fence - index;
    }

    public int characteristics() {
        return (Spliterator.SIZED | Spliterator.SUBSIZED |
                Spliterator.NONNULL | Spliterator.IMMUTABLE);
    }

    public boolean tryAdvance(DoubleConsumer consumer) {
        if (consumer == null) throw new NullPointerException();
        long i = index, f = fence;
        if (i < f) {
            consumer.accept(rng.internalNextDouble(origin, bound));
            index = i + 1;
            return true;
        }
        return false;
    }

    public void forEachRemaining(DoubleConsumer consumer) {
        if (consumer == null) throw new NullPointerException();
        long i = index, f = fence;
        if (i < f) {
            index = f;
            Random r = rng;
            double o = origin, b = bound;
            do {
                consumer.accept(r.internalNextDouble(o, b));
            } while (++i < f);
        }
    }


    /**
     * Random 的可序列化字段。
     *
     * @serialField    seed long
     *              用于随机计算的种子
     * @serialField    nextNextGaussian double
     *              下一个要返回的高斯值
     * @serialField      haveNextNextGaussian boolean
     *              nextNextGaussian 是否有效
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("seed", Long.TYPE),
        new ObjectStreamField("nextNextGaussian", Double.TYPE),
        new ObjectStreamField("haveNextNextGaussian", Boolean.TYPE)
    };

    /**
     * 从流中重新构建 Random 实例（即，反序列化它）。
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {

        ObjectInputStream.GetField fields = s.readFields();

        // 由于历史原因，种子以 long 类型读取，但转换为 AtomicLong。
        long seedVal = fields.get("seed", -1L);
        if (seedVal < 0)
          throw new java.io.StreamCorruptedException(
                              "Random: 无效的种子");
        resetSeed(seedVal);
        nextNextGaussian = fields.get("nextNextGaussian", 0.0);
        haveNextNextGaussian = fields.get("haveNextNextGaussian", false);
    }

    /**
     * 将 Random 实例保存到流中。
     */
    synchronized private void writeObject(ObjectOutputStream s)
        throws IOException {


                    // 设置可序列化字段的值
        ObjectOutputStream.PutField fields = s.putFields();

        // 由于历史原因，种子被序列化为 long 类型。
        fields.put("seed", seed.get());
        fields.put("nextNextGaussian", nextNextGaussian);
        fields.put("haveNextNextGaussian", haveNextNextGaussian);

        // 保存它们
        s.writeFields();
    }

    // 支持在反序列化时重置种子
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long seedOffset;
    static {
        try {
            seedOffset = unsafe.objectFieldOffset
                (Random.class.getDeclaredField("seed"));
        } catch (Exception ex) { throw new Error(ex); }
    }
    private void resetSeed(long seedVal) {
        unsafe.putObjectVolatile(this, seedOffset, new AtomicLong(seedVal));
    }

}
