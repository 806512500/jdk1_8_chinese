/*
 * 版权所有 (c) 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.function.DoubleConsumer;
import java.util.stream.StreamSupport;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.DoubleStream;

/**
 * 一个生成适用于（在其他上下文中）隔离并行计算的均匀伪随机值的生成器，这些计算可能生成子任务。类 {@code SplittableRandom} 支持生成类型为 {@code int}、{@code long} 和 {@code double} 的伪随机数的方法，其使用方式与类 {@link java.util.Random} 类似，但在以下方面有所不同：
 *
 * <ul>
 *
 * <li>生成的值系列通过了 DieHarder 套件测试，验证了随机数生成器的独立性和均匀性属性。 （最近使用 <a
 * href="http://www.phy.duke.edu/~rgb/General/dieharder.php">版本
 * 3.31.1</a> 进行了验证。）这些测试仅验证了某些类型和范围的方法，但预计其他方法也具有类似的属性，至少是近似的。 <em>周期</em>
 * （任何生成值系列在重复之前的长度）至少为 2<sup>64</sup>。</li>
 *
 * <li>方法 {@link #split} 构造并返回一个新的
 * SplittableRandom 实例，该实例与当前实例不共享任何可变状态。然而，以非常高的概率，两个对象集体生成的值具有与单个线程使用单个 {@code
 * SplittableRandom} 对象生成相同数量的值时相同的统计属性。</li>
 *
 * <li>SplittableRandom 的实例 <em>不是</em> 线程安全的。它们设计为在不同线程之间拆分，而不是共享。例如，使用随机数的 {@link java.util.concurrent.ForkJoinTask
 * 分支/合并风格} 计算可能包括形式为 {@code new
 * Subtask(aSplittableRandom.split()).fork()} 的构造。</li>
 *
 * <li>此类提供了用于生成随机流的附加方法，当在 {@code
 * stream.parallel()} 模式下使用时，这些方法采用上述技术。</li>
 *
 * </ul>
 *
 * <p>{@code SplittableRandom} 的实例不是加密安全的。在安全敏感的应用程序中，请考虑使用 {@link java.security.SecureRandom}。此外，默认构造的实例不会使用加密随机种子，除非系统属性
 * {@linkplain System#getProperty} {@code java.util.secureRandomSeed} 设置为 {@code true}。</p>
 *
 * @author  Guy Steele
 * @author  Doug Lea
 * @since   1.8
 */
public final class SplittableRandom {

    /*
     * 实现概述。
     *
     * 该算法受到 Leiserson, Schardl, 和 Sukha "Deterministic Parallel
     * Random-Number Generation for Dynamic-Multithreading Platforms",
     * PPoPP 2012 以及 "Parallel random numbers: as
     * easy as 1, 2, 3" by Salmon, Morae, Dror, and Shaw, SC 2011 中的算法的启发。它主要在简化和降低操作成本方面有所不同。
     *
     * 主要的更新步骤（方法 nextSeed()）是将一个常数（“gamma”）加到当前（64 位）种子上，形成一个简单的序列。任何两个
     * SplittableRandom 实例的种子和 gamma 值很可能不同。
     *
     * 方法 nextLong、nextInt 及其派生方法不返回序列（种子）值，而是返回其位的类似哈希的位混合，生成更独立分布的序列。对于 nextLong，mix64 函数基于 David Stafford 的
     * (http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html)
     * “Mix13” 变体，该变体是 Austin Appleby 的 MurmurHash3 算法中的 “64 位终结器” 函数（参见
     * http://code.google.com/p/smhasher/wiki/MurmurHash3）。mix32 函数基于 Stafford 的 Mix04 混合函数，但返回作为 int 类型的高 32 位。
     *
     * split 操作使用当前生成器形成另一个 SplittableRandom 的种子和 gamma。为了避免种子和值生成之间的潜在相关性，gamma 选择（方法 mixGamma）使用不同的（Murmurhash3 的）混合常数。为了避免位混合变换中的潜在弱点，我们将 gammas 限制为具有至少 24 个 0-1 或 1-0 位转换的奇数值。而不是拒绝设置位太少或太多的候选者，方法 mixGamma 翻转一些位（这具有将最多 4 个映射到任何给定的 gamma 值的效果）。这将 64 位奇数 gamma 值的有效集减少了约 2%，并作为序列常量选择的自动化筛选，这在其他一些哈希和加密算法中是作为经验决策留下的。
     *
     * 因此，生成器将一个序列转换为在每一步（通常）许多位变化的序列，使用成本低廉的混合器，具有良好的（但不如加密安全的）雪崩效果。
     *
     * 默认（无参数）构造函数，本质上，调用一个常见的 "defaultGen" SplittableRandom 的 split()。与其他情况不同，此拆分必须以线程安全的方式执行，因此我们使用 AtomicLong 而不是显式的 SplittableRandom 来表示种子。为了引导 defaultGen，我们从基于当前时间的种子开始，除非设置了 java.util.secureRandomSeed 属性。这充当了一个简化版（且不安全）的 SecureRandom 变体，同时避免了使用 /dev/random 时可能出现的停滞。
     *
     * 将此处的基本设计应用于 128 位种子是相对简单的事情。然而，模拟 128 位算术和携带两倍的状态增加了更多开销，这似乎对于当前的用法来说是不值得的。
     *
     * 文件组织：首先是构成主要算法的非公共方法，然后是主要的公共方法，最后是流方法所需的一些自定义拆分器类。
     */

                /**
     * 黄金比例缩放到64位，用作（未拆分的）SplittableRandoms的初始gamma值。
     */
    private static final long GOLDEN_GAMMA = 0x9e3779b97f4a7c15L;

    /**
     * nextDouble()返回的最小非零值。该值通过53位的随机值缩放以产生结果。
     */
    private static final double DOUBLE_UNIT = 0x1.0p-53; // 1.0 / (1L << 53);

    /**
     * 种子。仅通过方法nextSeed更新。
     */
    private long seed;

    /**
     * 步长值。
     */
    private final long gamma;

    /**
     * 除默认构造函数外，所有其他构造函数使用的内部构造函数。
     */
    private SplittableRandom(long seed, long gamma) {
        this.seed = seed;
        this.gamma = gamma;
    }

    /**
     * 计算Stafford变体13的64位混合函数。
     */
    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }

    /**
     * 返回Stafford变体4的mix64函数的高32位作为int。
     */
    private static int mix32(long z) {
        z = (z ^ (z >>> 33)) * 0x62a9d9ed799705f5L;
        return (int)(((z ^ (z >>> 28)) * 0xcb24d0a5c88c35b3L) >>> 32);
    }

    /**
     * 返回新拆分实例使用的gamma值。
     */
    private static long mixGamma(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL; // MurmurHash3混合常量
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
        z = (z ^ (z >>> 33)) | 1L;                  // 强制为奇数
        int n = Long.bitCount(z ^ (z >>> 1));       // 确保有足够的转换
        return (n < 24) ? z ^ 0xaaaaaaaaaaaaaaaaL : z;
    }

    /**
     * 将gamma加到seed上。
     */
    private long nextSeed() {
        return seed += gamma;
    }

    /**
     * 默认构造函数的种子生成器。
     */
    private static final AtomicLong defaultGen = new AtomicLong(initialSeed());

    private static long initialSeed() {
        String pp = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction(
                        "java.util.secureRandomSeed"));
        if (pp != null && pp.equalsIgnoreCase("true")) {
            byte[] seedBytes = java.security.SecureRandom.getSeed(8);
            long s = (long)(seedBytes[0]) & 0xffL;
            for (int i = 1; i < 8; ++i)
                s = (s << 8) | ((long)(seedBytes[i]) & 0xffL);
            return s;
        }
        return (mix64(System.currentTimeMillis()) ^
                mix64(System.nanoTime()));
    }

    // IllegalArgumentException消息
    static final String BadBound = "bound must be positive";
    static final String BadRange = "bound must be greater than origin";
    static final String BadSize  = "size must be non-negative";

    /*
     * 由流使用的nextX方法的内部版本，以及公共的nextX(origin, bound)方法。这些方法主要存在是为了避免在不同形式的流中使用多个版本的流拆分器。
     */

    /**
     * 由LongStream拆分器使用的nextLong形式。如果origin大于bound，则作为无界形式的nextLong，否则作为有界形式。
     *
     * @param origin 最小值，除非大于bound
     * @param bound 上界（不包括），必须不等于origin
     * @return 一个伪随机值
     */
    final long internalNextLong(long origin, long bound) {
        /*
         * 四种情况：
         *
         * 1. 如果参数表示无界形式，则作为nextLong()。
         *
         * 2. 如果范围是2的幂，则应用相应的位掩码。
         *
         * 3. 如果范围为正，则循环以避免潜在的偏差，当隐式nextLong()范围（2^64）不能被范围整除时。循环拒绝从其他过度表示的值计算的候选值。理想生成器下的预期迭代次数从1到2不等，具体取决于范围。循环本身采用了一种不可爱的形式。因为第一个候选值已经可用，我们需要一个中间断开的构造，这在for循环的条件中简洁但隐晦地执行。
         *
         * 4. 否则，范围不能表示为正长整数。循环重复生成无界长整数，直到获得满足约束条件的候选值（预期迭代次数小于两次）。
         */

        long r = mix64(nextSeed());
        if (origin < bound) {
            long n = bound - origin, m = n - 1;
            if ((n & m) == 0L)  // 2的幂
                r = (r & m) + origin;
            else if (n > 0L) {  // 拒绝过度表示的候选值
                for (long u = r >>> 1;            // 确保非负
                     u + m - (r = u % n) < 0L;    // 拒绝检查
                     u = mix64(nextSeed()) >>> 1) // 重试
                    ;
                r += origin;
            }
            else {              // 范围不能表示为长整数
                while (r < origin || r >= bound)
                    r = mix64(nextSeed());
            }
        }
        return r;
    }

    /**
     * 由IntStream拆分器使用的nextInt形式。与长整数版本完全相同，只是类型不同。
     *
     * @param origin 最小值，除非大于bound
     * @param bound 上界（不包括），必须不等于origin
     * @return 一个伪随机值
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
     * @return 一个伪随机值
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

    /* ---------------- public methods ---------------- */

    /**
     * 使用指定的初始种子创建一个新的 SplittableRandom 实例。使用相同种子在同一个程序中创建的 SplittableRandom 实例生成相同的值序列。
     *
     * @param seed 初始种子
     */
    public SplittableRandom(long seed) {
        this(seed, GOLDEN_GAMMA);
    }

    /**
     * 创建一个新的 SplittableRandom 实例，该实例可能生成与当前程序中其他实例统计独立的值序列；
     * 并且可能在程序调用之间有所不同。
     */
    public SplittableRandom() { // 模拟 defaultGen.split()
        long s = defaultGen.getAndAdd(2 * GOLDEN_GAMMA);
        this.seed = mix64(s);
        this.gamma = mixGamma(s + GOLDEN_GAMMA);
    }

    /**
     * 构造并返回一个新的 SplittableRandom 实例，该实例与这个实例不共享任何可变状态。然而，以非常高的概率，这两个对象集体生成的值集具有与单个线程使用单个 SplittableRandom 对象生成相同数量的值时相同的统计属性。可以使用 {@code split()} 方法进一步拆分这两个对象中的任何一个或两个，整个生成器集的预期统计属性同样适用。
     *
     * @return 新的 SplittableRandom 实例
     */
    public SplittableRandom split() {
        return new SplittableRandom(nextLong(), mixGamma(nextSeed()));
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
     * 返回一个介于零（包括）和指定边界（不包括）之间的伪随机 {@code int} 值。
     *
     * @param bound 上界（不包括）。必须为正数。
     * @return 一个介于零（包括）和边界（不包括）之间的伪随机 {@code int} 值
     * @throws IllegalArgumentException 如果 {@code bound} 不为正数
     */
    public int nextInt(int bound) {
        if (bound <= 0)
            throw new IllegalArgumentException(BadBound);
        // 为 origin 0 专门化 internalNextInt
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
     * 返回一个介于指定的原点（包括）和指定的边界（不包括）之间的伪随机 {@code int} 值。
     *
     * @param origin 返回的最小值
     * @param bound 上界（不包括）
     * @return 一个介于原点（包括）和边界（不包括）之间的伪随机 {@code int} 值
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
     * 返回一个介于零（包括）和指定边界（不包括）之间的伪随机 {@code long} 值。
     *
     * @param bound 上界（不包括）。必须为正数。
     * @return 一个介于零（包括）和边界（不包括）之间的伪随机 {@code long} 值
     * @throws IllegalArgumentException 如果 {@code bound} 不为正数
     */
    public long nextLong(long bound) {
        if (bound <= 0)
            throw new IllegalArgumentException(BadBound);
        // 为 origin 0 专门化 internalNextLong
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
     * 返回一个介于指定的原点（包括）和指定的边界（不包括）之间的伪随机 {@code long} 值。
     *
     * @param origin 返回的最小值
     * @param bound 上界（不包括）
     * @return 一个介于原点（包括）和边界（不包括）之间的伪随机 {@code long} 值
     * @throws IllegalArgumentException 如果 {@code origin} 大于或等于 {@code bound}
     */
    public long nextLong(long origin, long bound) {
        if (origin >= bound)
            throw new IllegalArgumentException(BadRange);
        return internalNextLong(origin, bound);
    }

    /**
     * 返回一个介于零（包括）和一（不包括）之间的伪随机 {@code double} 值。
     *
     * @return 一个介于零（包括）和一（不包括）之间的伪随机 {@code double} 值
     */
    public double nextDouble() {
        return (mix64(nextSeed()) >>> 11) * DOUBLE_UNIT;
    }


                /**
     * 返回一个介于 0.0（包含）和指定上限（不包含）之间的伪随机 {@code double} 值。
     *
     * @param bound 上限（不包含）。 必须为正数。
     * @return 介于零（包含）和上限（不包含）之间的伪随机 {@code double} 值
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
     * @return 介于起点（包含）和上限（不包含）之间的伪随机 {@code double} 值
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

    // 流方法，编码方式旨在更好地隔离维护目的上的小差异。

    /**
     * 返回一个从该生成器和/或从中拆分的生成器生成的给定 {@code streamSize} 数量的伪随机 {@code int} 值的流。
     *
     * @param streamSize 要生成的值的数量
     * @return 伪随机 {@code int} 值的流
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零
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
     * 返回一个从该生成器和/或从中拆分的生成器生成的实际上无限的伪随机 {@code int} 值的流。
     *
     * @implNote 此方法实现为等同于 {@code ints(Long.MAX_VALUE)}。
     *
     * @return 伪随机 {@code int} 值的流
     */
    public IntStream ints() {
        return StreamSupport.intStream
            (new RandomIntsSpliterator
             (this, 0L, Long.MAX_VALUE, Integer.MAX_VALUE, 0),
             false);
    }

    /**
     * 返回一个从该生成器和/或从中拆分的生成器生成的给定 {@code streamSize} 数量的伪随机 {@code int} 值的流；每个值符合给定的起点（包含）和上限（不包含）。
     *
     * @param streamSize 要生成的值的数量
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的上限（不包含）
     * @return 伪随机 {@code int} 值的流，每个值都有给定的起点（包含）和上限（不包含）
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零，或 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
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
     * 返回一个从该生成器和/或从中拆分的生成器生成的实际上无限的伪随机 {@code
     * int} 值的流；每个值符合给定的起点（包含）和上限（不包含）。
     *
     * @implNote 此方法实现为等同于 {@code
     * ints(Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)}。
     *
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的上限（不包含）
     * @return 伪随机 {@code int} 值的流，每个值都有给定的起点（包含）和上限（不包含）
     * @throws IllegalArgumentException 如果 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
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
     * 返回一个从该生成器和/或从中拆分的生成器生成的给定 {@code streamSize} 数量的伪随机 {@code long} 值的流。
     *
     * @param streamSize 要生成的值的数量
     * @return 伪随机 {@code long} 值的流
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零
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
     * 返回一个从该生成器和/或从中拆分的生成器生成的实际上无限的伪随机 {@code
     * long} 值流。
     *
     * @implNote 该方法的实现等同于 {@code
     * longs(Long.MAX_VALUE)}。
     *
     * @return 一个伪随机 {@code long} 值流
     */
    public LongStream longs() {
        return StreamSupport.longStream
            (new RandomLongsSpliterator
             (this, 0L, Long.MAX_VALUE, Long.MAX_VALUE, 0L),
             false);
    }

    /**
     * 返回一个从该生成器和/或从中拆分的生成器生成的指定 {@code streamSize} 数量的
     * 伪随机 {@code long} 值流；每个值都符合给定的起点（包含）和边界
     * （不包含）。
     *
     * @param streamSize 要生成的值的数量
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的边界（不包含）
     * @return 一个伪随机 {@code long} 值流，
     *         每个值都有给定的起点（包含）和边界（不包含）
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零，或者 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
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
     * 返回一个从该生成器和/或从中拆分的生成器生成的实际上无限的伪随机 {@code
     * long} 值流；每个值都符合给定的起点（包含）和边界（不包含）。
     *
     * @implNote 该方法的实现等同于 {@code
     * longs(Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)}。
     *
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的边界（不包含）
     * @return 一个伪随机 {@code long} 值流，
     *         每个值都有给定的起点（包含）和边界（不包含）
     * @throws IllegalArgumentException 如果 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
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
     * 返回一个从该生成器和/或从中拆分的生成器生成的指定 {@code streamSize} 数量的
     * 伪随机 {@code double} 值流；每个值都在零（包含）和一（不包含）之间。
     *
     * @param streamSize 要生成的值的数量
     * @return 一个 {@code double} 值流
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零
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
     * 返回一个从该生成器和/或从中拆分的生成器生成的实际上无限的伪随机 {@code
     * double} 值流；每个值都在零（包含）和一（不包含）之间。
     *
     * @implNote 该方法的实现等同于 {@code
     * doubles(Long.MAX_VALUE)}。
     *
     * @return 一个伪随机 {@code double} 值流
     */
    public DoubleStream doubles() {
        return StreamSupport.doubleStream
            (new RandomDoublesSpliterator
             (this, 0L, Long.MAX_VALUE, Double.MAX_VALUE, 0.0),
             false);
    }

    /**
     * 返回一个从该生成器和/或从中拆分的生成器生成的指定 {@code streamSize} 数量的
     * 伪随机 {@code double} 值流；每个值都符合给定的起点（包含）和边界
     * （不包含）。
     *
     * @param streamSize 要生成的值的数量
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的边界（不包含）
     * @return 一个伪随机 {@code double} 值流，
     *         每个值都有给定的起点（包含）和边界（不包含）
     * @throws IllegalArgumentException 如果 {@code streamSize} 小于零
     * @throws IllegalArgumentException 如果 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
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
     * 返回一个从该生成器和/或从中拆分的生成器生成的实际上无限的伪随机 {@code
     * double} 值流；每个值都符合给定的起点（包含）和边界（不包含）。
     *
     * @implNote 该方法的实现等同于 {@code
     * doubles(Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)}。
     *
     * @param randomNumberOrigin 每个随机值的起点（包含）
     * @param randomNumberBound 每个随机值的边界（不包含）
     * @return 一个伪随机 {@code double} 值流，
     *         每个值都有给定的起点（包含）和边界（不包含）
     * @throws IllegalArgumentException 如果 {@code randomNumberOrigin}
     *         大于或等于 {@code randomNumberBound}
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
     * 整数流的Spliterator。我们通过将边界小于起始值视为无界，并将“无限”等同于Long.MAX_VALUE，将四种整数版本复用到一个类中。
     * 对于拆分，它使用标准的二分法。该类的长整型和双精度版本除了类型外完全相同。
     */
    static final class RandomIntsSpliterator implements Spliterator.OfInt {
        final SplittableRandom rng;
        long index;
        final long fence;
        final int origin;
        final int bound;
        RandomIntsSpliterator(SplittableRandom rng, long index, long fence,
                              int origin, int bound) {
            this.rng = rng; this.index = index; this.fence = fence;
            this.origin = origin; this.bound = bound;
        }

        public RandomIntsSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null :
                new RandomIntsSpliterator(rng.split(), i, index = m, origin, bound);
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
                SplittableRandom r = rng;
                int o = origin, b = bound;
                do {
                    consumer.accept(r.internalNextInt(o, b));
                } while (++i < f);
            }
        }
    }

    /**
     * 长整数流的Spliterator。
     */
    static final class RandomLongsSpliterator implements Spliterator.OfLong {
        final SplittableRandom rng;
        long index;
        final long fence;
        final long origin;
        final long bound;
        RandomLongsSpliterator(SplittableRandom rng, long index, long fence,
                               long origin, long bound) {
            this.rng = rng; this.index = index; this.fence = fence;
            this.origin = origin; this.bound = bound;
        }

        public RandomLongsSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null :
                new RandomLongsSpliterator(rng.split(), i, index = m, origin, bound);
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
                SplittableRandom r = rng;
                long o = origin, b = bound;
                do {
                    consumer.accept(r.internalNextLong(o, b));
                } while (++i < f);
            }
        }

    }

    /**
     * 双精度流的Spliterator。
     */
    static final class RandomDoublesSpliterator implements Spliterator.OfDouble {
        final SplittableRandom rng;
        long index;
        final long fence;
        final double origin;
        final double bound;
        RandomDoublesSpliterator(SplittableRandom rng, long index, long fence,
                                 double origin, double bound) {
            this.rng = rng; this.index = index; this.fence = fence;
            this.origin = origin; this.bound = bound;
        }

        public RandomDoublesSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null :
                new RandomDoublesSpliterator(rng.split(), i, index = m, origin, bound);
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
                SplittableRandom r = rng;
                double o = origin, b = bound;
                do {
                    consumer.accept(r.internalNextDouble(o, b));
                } while (++i < f);
            }
        }
    }

}
