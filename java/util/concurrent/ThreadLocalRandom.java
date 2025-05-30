
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

package java.util.concurrent;

import java.io.ObjectStreamField;
import java.util.Random;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;
import sun.misc.VM;

/**
 * 一个隔离到当前线程的随机数生成器。类似于 {@link java.util.Random} 类使用的全局生成器，
 * 一个 {@code ThreadLocalRandom} 使用内部生成的种子初始化，该种子不能以其他方式修改。
 * 在并发程序中，使用 {@code ThreadLocalRandom} 而不是共享的 {@code Random} 对象通常会遇到更少的开销和争用。
 * 使用 {@code ThreadLocalRandom} 特别适用于在线程池中并行使用随机数的多个任务（例如，每个 {@link ForkJoinTask}）。
 *
 * <p>此类的使用通常形式为：
 * {@code ThreadLocalRandom.current().nextX(...)}（其中 {@code X} 是 {@code Int}、{@code Long} 等）。
 * 当所有使用都是这种形式时，绝不可能意外地在多个线程之间共享 {@code ThreadLocalRandom}。
 *
 * <p>此类还提供了常用的有界随机生成方法。
 *
 * <p>{@code ThreadLocalRandom} 的实例不是密码安全的。在安全敏感的应用程序中，应考虑使用 {@link java.security.SecureRandom}。
 * 此外，默认构造的实例除非设置了系统属性 {@linkplain System#getProperty} {@code java.util.secureRandomSeed} 为 {@code true}，
 * 否则不会使用密码随机种子。
 *
 * @since 1.7
 * @author Doug Lea
 */
public class ThreadLocalRandom extends Random {
    /*
     * 此类实现了 java.util.Random API（并继承了 Random），使用单个静态实例访问类 Thread 中持有的随机数状态
     * （主要是字段 threadLocalRandomSeed）。通过这样做，它还为依赖于维护 ThreadLocalRandom 实例所需的确切状态的包私有实用程序提供了一个家园。
     * 我们利用需要初始化标志字段的需求，将其用作“探针”——一种用于避免争用的自调整线程哈希，以及一个更简单的（xorShift）随机种子，
     * 保守地使用以避免意外地劫持 ThreadLocalRandom 序列，从而让用户感到惊讶。双重用途是一种权宜之计，但是一种简单而高效的减少
     * 大多数并发程序的应用级开销和占用空间的方法。
     *
     * 尽管此类继承了 java.util.Random，但它使用了与 java.util.SplittableRandom 相同的基本算法。（请参阅其内部文档，此处不再重复。）
     * 由于 ThreadLocalRandom 不可拆分，我们仅使用单个 64 位 gamma。
     *
     * 由于此类位于与类 Thread 不同的包中，字段访问方法使用 Unsafe 以绕过访问控制规则。
     * 为了符合 Random 超类构造函数的要求，常见的静态 ThreadLocalRandom 维护了一个“已初始化”字段，以拒绝用户调用 setSeed，
     * 同时允许从构造函数调用。注意，由于只有一个静态单例，因此序列化是完全不必要的。但我们生成了一个包含“rnd”和“已初始化”字段的序列化形式，
     * 以确保版本之间的兼容性。
     *
     * 非核心方法的实现大多与 SplittableRandom 相同，这些方法部分源自此类的早期版本。
     *
     * nextLocalGaussian ThreadLocal 支持非常少用的 nextGaussian 方法，通过提供一对高斯数中的第二个数的持有者。
     * 与基类版本的此方法一样，这种时间和空间的权衡几乎从未值得，但我们提供了相同的统计属性。
     */

    /** 生成每个线程的初始化/探针字段 */
    private static final AtomicInteger probeGenerator =
        new AtomicInteger();

    /**
     * 默认构造函数的下一个种子。
     */
    private static final AtomicLong seeder = new AtomicLong(initialSeed());

    private static long initialSeed() {
        String sec = VM.getSavedProperty("java.util.secureRandomSeed");
        if (Boolean.parseBoolean(sec)) {
            byte[] seedBytes = java.security.SecureRandom.getSeed(8);
            long s = (long)(seedBytes[0]) & 0xffL;
            for (int i = 1; i < 8; ++i)
                s = (s << 8) | ((long)(seedBytes[i]) & 0xffL);
            return s;
        }
        return (mix64(System.currentTimeMillis()) ^
                mix64(System.nanoTime()));
    }

    /**
     * 种子增量
     */
    private static final long GAMMA = 0x9e3779b97f4a7c15L;

    /**
     * 生成探针值的增量
     */
    private static final int PROBE_INCREMENT = 0x9e3779b9;

    /**
     * 每个新实例的 seeder 增量
     */
    private static final long SEEDER_INCREMENT = 0xbb67ae8584caa73bL;

    // 常量来自 SplittableRandom
    private static final double DOUBLE_UNIT = 0x1.0p-53;  // 1.0  / (1L << 53)
    private static final float  FLOAT_UNIT  = 0x1.0p-24f; // 1.0f / (1 << 24)

    /** 罕用的高斯数对的第二个数的持有者 */
    private static final ThreadLocal<Double> nextLocalGaussian =
        new ThreadLocal<Double>();

    private static long mix64(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return z ^ (z >>> 33);
    }

    private static int mix32(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        return (int)(((z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L) >>> 32);
    }

    /**
     * 仅在单例初始化期间使用的字段。
     * 当构造函数完成时为 true。
     */
    boolean initialized;

    /** 仅用于静态单例的构造函数 */
    private ThreadLocalRandom() {
        initialized = true; // 在 super() 调用期间为 false
    }

    /** 公共的 ThreadLocalRandom */
    static final ThreadLocalRandom instance = new ThreadLocalRandom();

    /**
     * 初始化当前线程的字段。仅当 Thread.threadLocalRandomProbe 为零时调用，表示需要生成线程本地种子值。
     * 即使初始化是纯粹的线程本地的，我们也需要依赖（静态）原子生成器来初始化这些值。
     */
    static final void localInit() {
        int p = probeGenerator.addAndGet(PROBE_INCREMENT);
        int probe = (p == 0) ? 1 : p; // 跳过 0
        long seed = mix64(seeder.getAndAdd(SEEDER_INCREMENT));
        Thread t = Thread.currentThread();
        UNSAFE.putLong(t, SEED, seed);
        UNSAFE.putInt(t, PROBE, probe);
    }

    /**
     * 返回当前线程的 {@code ThreadLocalRandom}。
     *
     * @return 当前线程的 {@code ThreadLocalRandom}
     */
    public static ThreadLocalRandom current() {
        if (UNSAFE.getInt(Thread.currentThread(), PROBE) == 0)
            localInit();
        return instance;
    }

    /**
     * 抛出 {@code UnsupportedOperationException}。此生成器不支持设置种子。
     *
     * @throws UnsupportedOperationException 始终抛出
     */
    public void setSeed(long seed) {
        // 仅允许从 super() 构造函数调用
        if (initialized)
            throw new UnsupportedOperationException();
    }

    final long nextSeed() {
        Thread t; long r; // 读取并更新每个线程的种子
        UNSAFE.putLong(t = Thread.currentThread(), SEED,
                       r = UNSAFE.getLong(t, SEED) + GAMMA);
        return r;
    }

    // 我们必须定义这个，但从未使用它。
    protected int next(int bits) {
        return (int)(mix64(nextSeed()) >>> (64 - bits));
    }

    // IllegalArgumentException 消息
    static final String BadBound = "bound 必须为正数";
    static final String BadRange = "bound 必须大于 origin";
    static final String BadSize  = "size 必须为非负数";

    /**
     * 由 LongStream Spliterators 使用的 nextLong 形式。如果 origin 大于 bound，则作为无界形式的 nextLong，
     * 否则作为有界形式的 nextLong。
     *
     * @param origin 最小值，除非大于 bound
     * @param bound 上界（不包括），不得等于 origin
     * @return 伪随机值
     */
    final long internalNextLong(long origin, long bound) {
        long r = mix64(nextSeed());
        if (origin < bound) {
            long n = bound - origin, m = n - 1;
            if ((n & m) == 0L)  // 2 的幂
                r = (r & m) + origin;
            else if (n > 0L) {  // 拒绝过度表示的候选值
                for (long u = r >>> 1;            // 确保非负
                     u + m - (r = u % n) < 0L;    // 拒绝检查
                     u = mix64(nextSeed()) >>> 1) // 重试
                    ;
                r += origin;
            }
            else {              // 范围不能表示为 long
                while (r < origin || r >= bound)
                    r = mix64(nextSeed());
            }
        }
        return r;
    }

    /**
     * 由 IntStream Spliterators 使用的 nextInt 形式。与 long 版本完全相同，只是类型不同。
     *
     * @param origin 最小值，除非大于 bound
     * @param bound 上界（不包括），不得等于 origin
     * @return 伪随机值
     */
    final int internalNextInt(int origin, int bound) {
        int r = mix32(nextSeed());
        if (origin < bound) {
            int n = bound - origin, m = n - 1;
            if ((n & m) == 0)
                r = (r & m) + origin;
            else if (n > 0) {
                for (int u = r >>> 1;
                     u + m - (r = u % n) < 0;
                     u = mix32(nextSeed()) >>> 1)
                    ;
                r += origin;
            }
            else {
                while (r < origin || r >= bound)
                    r = mix32(nextSeed());
            }
        }
        return r;
    }

    /**
     * 由 DoubleStream Spliterators 使用的 nextDouble 形式。
     *
     * @param origin 最小值，除非大于 bound
     * @param bound 上界（不包括），不得等于 origin
     * @return 伪随机值
     */
    final double internalNextDouble(double origin, double bound) {
        double r = (nextLong() >>> 11) * DOUBLE_UNIT;
        if (origin < bound) {
            r = r * (bound - origin) + origin;
            if (r >= bound) // 修正舍入
                r = Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
        }
        return r;
    }

    /**
     * 返回一个伪随机的 {@code int} 值。
     *
     * @return 一个伪随机的 {@code int} 值
     */
    public int nextInt() {
        return mix32(nextSeed());
    }

    /**
     * 返回一个伪随机的 {@code int} 值，介于零（包括）和指定的上界（不包括）之间。
     *
     * @param bound 上界（不包括）。必须为正数。
     * @return 一个伪随机的 {@code int} 值，介于零（包括）和上界（不包括）之间
     * @throws IllegalArgumentException 如果 {@code bound} 不为正数
     */
    public int nextInt(int bound) {
        if (bound <= 0)
            throw new IllegalArgumentException(BadBound);
        int r = mix32(nextSeed());
        int m = bound - 1;
        if ((bound & m) == 0) // 2 的幂
            r &= m;
        else { // 拒绝过度表示的候选值
            for (int u = r >>> 1;
                 u + m - (r = u % bound) < 0;
                 u = mix32(nextSeed()) >>> 1)
                ;
        }
        return r;
    }

    /**
     * 返回一个伪随机的 {@code int} 值，介于指定的最小值（包括）和指定的上界（不包括）之间。
     *
     * @param origin 最小值
     * @param bound 上界（不包括）
     * @return 一个伪随机的 {@code int} 值，介于最小值（包括）和上界（不包括）之间
     * @throws IllegalArgumentException 如果 {@code origin} 大于或等于 {@code bound}
     */
    public int nextInt(int origin, int bound) {
        if (origin >= bound)
            throw new IllegalArgumentException(BadRange);
        return internalNextInt(origin, bound);
    }

    /**
     * 返回一个伪随机的 {@code long} 值。
     *
     * @return 一个伪随机的 {@code long} 值
     */
    public long nextLong() {
        return mix64(nextSeed());
    }

    /**
     * 返回一个伪随机的 {@code long} 值，介于零（包括）和指定的上界（不包括）之间。
     *
     * @param bound 上界（不包括）。必须为正数。
     * @return 一个伪随机的 {@code long} 值，介于零（包括）和上界（不包括）之间
     * @throws IllegalArgumentException 如果 {@code bound} 不为正数
     */
    public long nextLong(long bound) {
        if (bound <= 0)
            throw new IllegalArgumentException(BadBound);
        long r = mix64(nextSeed());
        long m = bound - 1;
        if ((bound & m) == 0L) // 2 的幂
            r &= m;
        else { // 拒绝过度表示的候选值
            for (long u = r >>> 1;
                 u + m - (r = u % bound) < 0L;
                 u = mix64(nextSeed()) >>> 1)
                ;
        }
        return r;
    }


                /**
     * 返回一个介于指定的起点（包含）和指定的上限（不包含）之间的伪随机 {@code long} 值。
     *
     * @param origin 最小返回值
     * @param bound 上限（不包含）
     * @return 一个介于起点（包含）和上限（不包含）之间的伪随机 {@code long} 值
     * @throws IllegalArgumentException 如果 {@code origin} 大于或等于 {@code bound}
     */
    public long nextLong(long origin, long bound) {
        if (origin >= bound)
            throw new IllegalArgumentException(BadRange);
        return internalNextLong(origin, bound);
    }

    /**
     * 返回一个介于零（包含）和一（不包含）之间的伪随机 {@code double} 值。
     *
     * @return 一个介于零（包含）和一（不包含）之间的伪随机 {@code double} 值
     */
    public double nextDouble() {
        return (mix64(nextSeed()) >>> 11) * DOUBLE_UNIT;
    }

    /**
     * 返回一个介于 0.0（包含）和指定的上限（不包含）之间的伪随机 {@code double} 值。
     *
     * @param bound 上限（不包含）。必须为正数。
     * @return 一个介于零（包含）和上限（不包含）之间的伪随机 {@code double} 值
     * @throws IllegalArgumentException 如果 {@code bound} 不为正数
     */
    public double nextDouble(double bound) {
        if (!(bound > 0.0))
            throw new IllegalArgumentException(BadBound);
        double result = (mix64(nextSeed()) >>> 11) * DOUBLE_UNIT * bound;
        return (result < bound) ?  result : // 修正舍入
            Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
    }

    /**
     * 返回一个介于指定的起点（包含）和上限（不包含）之间的伪随机 {@code double} 值。
     *
     * @param origin 最小返回值
     * @param bound 上限（不包含）
     * @return 一个介于起点（包含）和上限（不包含）之间的伪随机 {@code double} 值
     * @throws IllegalArgumentException 如果 {@code origin} 大于或等于 {@code bound}
     */
    public double nextDouble(double origin, double bound) {
        if (!(origin < bound))
            throw new IllegalArgumentException(BadRange);
        return internalNextDouble(origin, bound);
    }

    /**
     * 返回一个伪随机的 {@code boolean} 值。
     *
     * @return 一个伪随机的 {@code boolean} 值
     */
    public boolean nextBoolean() {
        return mix32(nextSeed()) < 0;
    }

    /**
     * 返回一个介于零（包含）和一（不包含）之间的伪随机 {@code float} 值。
     *
     * @return 一个介于零（包含）和一（不包含）之间的伪随机 {@code float} 值
     */
    public float nextFloat() {
        return (mix32(nextSeed()) >>> 8) * FLOAT_UNIT;
    }

    public double nextGaussian() {
        // 使用 nextLocalGaussian 而不是 nextGaussian 字段
        Double d = nextLocalGaussian.get();
        if (d != null) {
            nextLocalGaussian.set(null);
            return d.doubleValue();
        }
        double v1, v2, s;
        do {
            v1 = 2 * nextDouble() - 1; // 介于 -1 和 1 之间
            v2 = 2 * nextDouble() - 1; // 介于 -1 和 1 之间
            s = v1 * v1 + v2 * v2;
        } while (s >= 1 || s == 0);
        double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s)/s);
        nextLocalGaussian.set(new Double(v2 * multiplier));
        return v1 * multiplier;
    }

    // 流方法，编码方式旨在更好地隔离维护目的上的小差异。

    /**
     * 返回一个生成给定 {@code streamSize} 数量的伪随机 {@code int} 值的流。
     *
     * @param streamSize 要生成的值的数量
     * @return 一个生成伪随机 {@code int} 值的流
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零
     * @since 1.8
     */
    public IntStream ints(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.intStream
            (new RandomIntsSpliterator
             (0L, streamSize, Integer.MAX_VALUE, 0),
             false);
    }

    /**
     * 返回一个生成无限数量的伪随机 {@code int} 值的流。
     *
     * @implNote 此方法实现为等同于 {@code ints(Long.MAX_VALUE)}。
     *
     * @return 一个生成伪随机 {@code int} 值的流
     * @since 1.8
     */
    public IntStream ints() {
        return StreamSupport.intStream
            (new RandomIntsSpliterator
             (0L, Long.MAX_VALUE, Integer.MAX_VALUE, 0),
             false);
    }

    /**
     * 返回一个生成给定 {@code streamSize} 数量的伪随机 {@code int} 值的流，每个值都符合给定的起点（包含）和上限（不包含）。
     *
     * @param streamSize 要生成的值的数量
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的上限（不包含）
     * @return 一个生成伪随机 {@code int} 值的流，每个值都符合给定的起点（包含）和上限（不包含）
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
             (0L, streamSize, randomNumberOrigin, randomNumberBound),
             false);
    }

    /**
     * 返回一个生成无限数量的伪随机 {@code int} 值的流，每个值都符合给定的起点（包含）和上限（不包含）。
     *
     * @implNote 此方法实现为等同于 {@code ints(Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)}。
     *
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的上限（不包含）
     * @return 一个生成伪随机 {@code int} 值的流，每个值都符合给定的起点（包含）和上限（不包含）
     * @throws IllegalArgumentException 如果 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
     * @since 1.8
     */
    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.intStream
            (new RandomIntsSpliterator
             (0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound),
             false);
    }

    /**
     * 返回一个生成给定 {@code streamSize} 数量的伪随机 {@code long} 值的流。
     *
     * @param streamSize 要生成的值的数量
     * @return 一个生成伪随机 {@code long} 值的流
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零
     * @since 1.8
     */
    public LongStream longs(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.longStream
            (new RandomLongsSpliterator
             (0L, streamSize, Long.MAX_VALUE, 0L),
             false);
    }

    /**
     * 返回一个生成无限数量的伪随机 {@code long} 值的流。
     *
     * @implNote 此方法实现为等同于 {@code longs(Long.MAX_VALUE)}。
     *
     * @return 一个生成伪随机 {@code long} 值的流
     * @since 1.8
     */
    public LongStream longs() {
        return StreamSupport.longStream
            (new RandomLongsSpliterator
             (0L, Long.MAX_VALUE, Long.MAX_VALUE, 0L),
             false);
    }

    /**
     * 返回一个生成给定 {@code streamSize} 数量的伪随机 {@code long} 值的流，每个值都符合给定的起点（包含）和上限（不包含）。
     *
     * @param streamSize 要生成的值的数量
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的上限（不包含）
     * @return 一个生成伪随机 {@code long} 值的流，每个值都符合给定的起点（包含）和上限（不包含）
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
             (0L, streamSize, randomNumberOrigin, randomNumberBound),
             false);
    }

    /**
     * 返回一个生成无限数量的伪随机 {@code long} 值的流，每个值都符合给定的起点（包含）和上限（不包含）。
     *
     * @implNote 此方法实现为等同于 {@code longs(Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)}。
     *
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的上限（不包含）
     * @return 一个生成伪随机 {@code long} 值的流，每个值都符合给定的起点（包含）和上限（不包含）
     * @throws IllegalArgumentException 如果 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
     * @since 1.8
     */
    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.longStream
            (new RandomLongsSpliterator
             (0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound),
             false);
    }

    /**
     * 返回一个生成给定 {@code streamSize} 数量的伪随机 {@code double} 值的流，每个值都介于零（包含）和一（不包含）之间。
     *
     * @param streamSize 要生成的值的数量
     * @return 一个生成 {@code double} 值的流
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零
     * @since 1.8
     */
    public DoubleStream doubles(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.doubleStream
            (new RandomDoublesSpliterator
             (0L, streamSize, Double.MAX_VALUE, 0.0),
             false);
    }

    /**
     * 返回一个生成无限数量的伪随机 {@code double} 值的流，每个值都介于零（包含）和一（不包含）之间。
     *
     * @implNote 此方法实现为等同于 {@code doubles(Long.MAX_VALUE)}。
     *
     * @return 一个生成伪随机 {@code double} 值的流
     * @since 1.8
     */
    public DoubleStream doubles() {
        return StreamSupport.doubleStream
            (new RandomDoublesSpliterator
             (0L, Long.MAX_VALUE, Double.MAX_VALUE, 0.0),
             false);
    }

    /**
     * 返回一个生成给定 {@code streamSize} 数量的伪随机 {@code double} 值的流，每个值都符合给定的起点（包含）和上限（不包含）。
     *
     * @param streamSize 要生成的值的数量
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的上限（不包含）
     * @return 一个生成伪随机 {@code double} 值的流，每个值都符合给定的起点（包含）和上限（不包含）
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
             (0L, streamSize, randomNumberOrigin, randomNumberBound),
             false);
    }

    /**
     * 返回一个生成无限数量的伪随机 {@code double} 值的流，每个值都符合给定的起点（包含）和上限（不包含）。
     *
     * @implNote 此方法实现为等同于 {@code doubles(Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)}。
     *
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的上限（不包含）
     * @return 一个生成伪随机 {@code double} 值的流，每个值都符合给定的起点（包含）和上限（不包含）
     * @throws IllegalArgumentException 如果 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
     * @since 1.8
     */
    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        if (!(randomNumberOrigin < randomNumberBound))
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.doubleStream
            (new RandomDoublesSpliterator
             (0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound),
             false);
    }

    /**
     * 用于 int 流的 Spliterator。我们通过将上限小于起点视为无界，并将“无限”等同于 Long.MAX_VALUE，将四个 int
     * 版本合并到一个类中。对于拆分，它使用标准的二分法。此类的 long 和 double 版本除类型外完全相同。
     */
    static final class RandomIntsSpliterator implements Spliterator.OfInt {
        long index;
        final long fence;
        final int origin;
        final int bound;
        RandomIntsSpliterator(long index, long fence,
                              int origin, int bound) {
            this.index = index; this.fence = fence;
            this.origin = origin; this.bound = bound;
        }


    /**
     * 随机整数流的Spliterator。
     */
    static final class RandomIntsSpliterator implements Spliterator.OfInt {
        long index;
        final long fence;
        final int origin;
        final int bound;
        RandomIntsSpliterator(long index, long fence,
                              int origin, int bound) {
            this.index = index; this.fence = fence;
            this.origin = origin; this.bound = bound;
        }

        public RandomIntsSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null :
                new RandomIntsSpliterator(i, index = m, origin, bound);
        }

        public long estimateSize() {
            return fence - index;
        }

        public int characteristics() {
            return (Spliterator.SIZED | Spliterator.SUBSIZED |
                    Spliterator.NONNULL | Spliterator.IMMUTABLE);
        }

        public boolean tryAdvance(IntConsumer consumer) {
            if (consumer == null) throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                consumer.accept(ThreadLocalRandom.current().internalNextInt(origin, bound));
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
                int o = origin, b = bound;
                ThreadLocalRandom rng = ThreadLocalRandom.current();
                do {
                    consumer.accept(rng.internalNextInt(o, b));
                } while (++i < f);
            }
        }
    }

    /**
     * 随机长整数流的Spliterator。
     */
    static final class RandomLongsSpliterator implements Spliterator.OfLong {
        long index;
        final long fence;
        final long origin;
        final long bound;
        RandomLongsSpliterator(long index, long fence,
                               long origin, long bound) {
            this.index = index; this.fence = fence;
            this.origin = origin; this.bound = bound;
        }

        public RandomLongsSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null :
                new RandomLongsSpliterator(i, index = m, origin, bound);
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
                consumer.accept(ThreadLocalRandom.current().internalNextLong(origin, bound));
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
                long o = origin, b = bound;
                ThreadLocalRandom rng = ThreadLocalRandom.current();
                do {
                    consumer.accept(rng.internalNextLong(o, b));
                } while (++i < f);
            }
        }

    }

    /**
     * 随机双精度浮点数流的Spliterator。
     */
    static final class RandomDoublesSpliterator implements Spliterator.OfDouble {
        long index;
        final long fence;
        final double origin;
        final double bound;
        RandomDoublesSpliterator(long index, long fence,
                                 double origin, double bound) {
            this.index = index; this.fence = fence;
            this.origin = origin; this.bound = bound;
        }

        public RandomDoublesSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null :
                new RandomDoublesSpliterator(i, index = m, origin, bound);
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
                consumer.accept(ThreadLocalRandom.current().internalNextDouble(origin, bound));
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
                double o = origin, b = bound;
                ThreadLocalRandom rng = ThreadLocalRandom.current();
                do {
                    consumer.accept(rng.internalNextDouble(o, b));
                } while (++i < f);
            }
        }
    }


    // 包内工具

    /*
     * 以下方法的使用说明可以在使用这些方法的类中找到。简而言之，线程的“probe”值是一个非零哈希码，该哈希码（可能）不会与任何其他现有线程在任何2的幂次碰撞空间中发生碰撞。当发生碰撞时，它会使用Marsaglia XorShift伪随机调整。nextSecondarySeed方法用于与ThreadLocalRandom相同的上下文中，但仅用于瞬时使用，例如随机自适应旋转/阻塞序列，这些使用情况下廉价的RNG就足够了，并且如果使用主线程的ThreadLocalRandom可能会破坏用户可见的统计属性。
     *
     * 注意：由于包保护问题，某些子包类中也出现了这些方法的版本。
     */

    /**
     * 返回当前线程的probe值，但不强制初始化。注意，调用ThreadLocalRandom.current()可以用于在返回值为零时强制初始化。
     */
    static final int getProbe() {
        return UNSAFE.getInt(Thread.currentThread(), PROBE);
    }

    /**
     * 伪随机地推进并记录给定线程的给定probe值。
     */
    static final int advanceProbe(int probe) {
        probe ^= probe << 13;   // xorshift
        probe ^= probe >>> 17;
        probe ^= probe << 5;
        UNSAFE.putInt(Thread.currentThread(), PROBE, probe);
        return probe;
    }

    /**
     * 返回伪随机初始化或更新的次级种子。
     */
    static final int nextSecondarySeed() {
        int r;
        Thread t = Thread.currentThread();
        if ((r = UNSAFE.getInt(t, SECONDARY)) != 0) {
            r ^= r << 13;   // xorshift
            r ^= r >>> 17;
            r ^= r << 5;
        }
        else {
            localInit();
            if ((r = (int)UNSAFE.getLong(t, SEED)) == 0)
                r = 1; // 避免零
        }
        UNSAFE.putInt(t, SECONDARY, r);
        return r;
    }

    // 序列化支持

    private static final long serialVersionUID = -5851777807851030925L;

    /**
     * @serialField rnd long
     *              随机计算的种子
     * @serialField initialized boolean
     *              始终为true
     */
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("rnd", long.class),
            new ObjectStreamField("initialized", boolean.class),
    };

    /**
     * 将{@code ThreadLocalRandom}保存到流中（即序列化它）。
     * @param s 流
     * @throws java.io.IOException 如果发生I/O错误
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {

        java.io.ObjectOutputStream.PutField fields = s.putFields();
        fields.put("rnd", UNSAFE.getLong(Thread.currentThread(), SEED));
        fields.put("initialized", true);
        s.writeFields();
    }

    /**
     * 返回当前线程的{@code ThreadLocalRandom}。
     * @return 当前线程的{@code ThreadLocalRandom}
     */
    private Object readResolve() {
        return current();
    }

    // Unsafe机制
    private static final sun.misc.Unsafe UNSAFE;
    private static final long SEED;
    private static final long PROBE;
    private static final long SECONDARY;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> tk = Thread.class;
            SEED = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomSeed"));
            PROBE = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomProbe"));
            SECONDARY = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomSecondarySeed"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
