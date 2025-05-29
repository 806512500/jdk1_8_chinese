
/*
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

/*
 *
 *
 *
 *
 *
 * 由 Doug Lea 在 JCP JSR-166 专家组成员的帮助下编写，并按照
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的那样发布到公共领域。
 */

package java.util.concurrent.atomic;
import java.util.function.LongUnaryOperator;
import java.util.function.LongBinaryOperator;
import sun.misc.Unsafe;

/**
 * 一个元素可以原子更新的 {@code long} 数组。
 * 有关原子变量属性的描述，请参阅 {@link java.util.concurrent.atomic} 包说明。
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicLongArray implements java.io.Serializable {
    private static final long serialVersionUID = -2308431214976778248L;

    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final int base = unsafe.arrayBaseOffset(long[].class);
    private static final int shift;
    private final long[] array;

    static {
        int scale = unsafe.arrayIndexScale(long[].class);
        if ((scale & (scale - 1)) != 0)
            throw new Error("数据类型比例不是2的幂");
        shift = 31 - Integer.numberOfLeadingZeros(scale);
    }

    private long checkedByteOffset(int i) {
        if (i < 0 || i >= array.length)
            throw new IndexOutOfBoundsException("索引 " + i);

        return byteOffset(i);
    }

    private static long byteOffset(int i) {
        return ((long) i << shift) + base;
    }

    /**
     * 创建一个给定长度的新 AtomicLongArray，所有元素初始值为零。
     *
     * @param length 数组的长度
     */
    public AtomicLongArray(int length) {
        array = new long[length];
    }

    /**
     * 创建一个与给定数组长度相同且所有元素都从给定数组复制的新 AtomicLongArray。
     *
     * @param array 要从中复制元素的数组
     * @throws NullPointerException 如果数组为 null
     */
    public AtomicLongArray(long[] array) {
        // 由最终字段保证的可见性
        this.array = array.clone();
    }

    /**
     * 返回数组的长度。
     *
     * @return 数组的长度
     */
    public final int length() {
        return array.length;
    }

    /**
     * 获取位置 {@code i} 的当前值。
     *
     * @param i 索引
     * @return 当前值
     */
    public final long get(int i) {
        return getRaw(checkedByteOffset(i));
    }

    private long getRaw(long offset) {
        return unsafe.getLongVolatile(array, offset);
    }

    /**
     * 将位置 {@code i} 的元素设置为给定值。
     *
     * @param i 索引
     * @param newValue 新值
     */
    public final void set(int i, long newValue) {
        unsafe.putLongVolatile(array, checkedByteOffset(i), newValue);
    }

    /**
     * 最终将位置 {@code i} 的元素设置为给定值。
     *
     * @param i 索引
     * @param newValue 新值
     * @since 1.6
     */
    public final void lazySet(int i, long newValue) {
        unsafe.putOrderedLong(array, checkedByteOffset(i), newValue);
    }

    /**
     * 原子地将位置 {@code i} 的元素设置为给定值并返回旧值。
     *
     * @param i 索引
     * @param newValue 新值
     * @return 之前的值
     */
    public final long getAndSet(int i, long newValue) {
        return unsafe.getAndSetLong(array, checkedByteOffset(i), newValue);
    }

    /**
     * 如果当前值 {@code ==} 期望值，则原子地将位置 {@code i} 的元素设置为给定的更新值。
     *
     * @param i 索引
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}。返回 {@code false} 表示实际值不等于期望值。
     */
    public final boolean compareAndSet(int i, long expect, long update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }

    private boolean compareAndSetRaw(long offset, long expect, long update) {
        return unsafe.compareAndSwapLong(array, offset, expect, update);
    }

    /**
     * 如果当前值 {@code ==} 期望值，则原子地将位置 {@code i} 的元素设置为给定的更新值。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能无故失败且不提供排序保证</a>，因此很少是 {@code compareAndSet} 的适当替代。
     *
     * @param i 索引
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     */
    public final boolean weakCompareAndSet(int i, long expect, long update) {
        return compareAndSet(i, expect, update);
    }

    /**
     * 原子地将索引 {@code i} 处的元素加一。
     *
     * @param i 索引
     * @return 之前的值
     */
    public final long getAndIncrement(int i) {
        return getAndAdd(i, 1);
    }

    /**
     * 原子地将索引 {@code i} 处的元素减一。
     *
     * @param i 索引
     * @return 之前的值
     */
    public final long getAndDecrement(int i) {
        return getAndAdd(i, -1);
    }

    /**
     * 原子地将给定值加到索引 {@code i} 处的元素。
     *
     * @param i 索引
     * @param delta 要加的值
     * @return 之前的值
     */
    public final long getAndAdd(int i, long delta) {
        return unsafe.getAndAddLong(array, checkedByteOffset(i), delta);
    }

    /**
     * 原子地将索引 {@code i} 处的元素加一。
     *
     * @param i 索引
     * @return 更新后的值
     */
    public final long incrementAndGet(int i) {
        return getAndAdd(i, 1) + 1;
    }

    /**
     * 原子地将索引 {@code i} 处的元素减一。
     *
     * @param i 索引
     * @return 更新后的值
     */
    public final long decrementAndGet(int i) {
        return getAndAdd(i, -1) - 1;
    }


                /**
     * 以原子方式将给定的值加到索引 {@code i} 处的元素。
     *
     * @param i 索引
     * @param delta 要添加的值
     * @return 更新后的值
     */
    public long addAndGet(int i, long delta) {
        return getAndAdd(i, delta) + delta;
    }

    /**
     * 以原子方式使用给定的函数更新索引 {@code i} 处的元素，返回旧值。该函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，它可能会被重新应用。
     *
     * @param i 索引
     * @param updateFunction 无副作用的函数
     * @return 旧值
     * @since 1.8
     */
    public final long getAndUpdate(int i, LongUnaryOperator updateFunction) {
        long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

    /**
     * 以原子方式使用给定的函数更新索引 {@code i} 处的元素，返回更新后的值。该函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，它可能会被重新应用。
     *
     * @param i 索引
     * @param updateFunction 无副作用的函数
     * @return 更新后的值
     * @since 1.8
     */
    public final long updateAndGet(int i, LongUnaryOperator updateFunction) {
        long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    /**
     * 以原子方式使用给定的函数对当前值和给定值应用结果来更新索引 {@code i} 处的元素，返回旧值。该函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，它可能会被重新应用。该函数以索引 {@code i} 处的当前值作为第一个参数，给定更新作为第二个参数。
     *
     * @param i 索引
     * @param x 更新值
     * @param accumulatorFunction 无副作用的双参数函数
     * @return 旧值
     * @since 1.8
     */
    public final long getAndAccumulate(int i, long x,
                                      LongBinaryOperator accumulatorFunction) {
        long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

    /**
     * 以原子方式使用给定的函数对当前值和给定值应用结果来更新索引 {@code i} 处的元素，返回更新后的值。该函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，它可能会被重新应用。该函数以索引 {@code i} 处的当前值作为第一个参数，给定更新作为第二个参数。
     *
     * @param i 索引
     * @param x 更新值
     * @param accumulatorFunction 无副作用的双参数函数
     * @return 更新后的值
     * @since 1.8
     */
    public final long accumulateAndGet(int i, long x,
                                      LongBinaryOperator accumulatorFunction) {
        long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    /**
     * 返回数组当前值的字符串表示形式。
     * @return 数组当前值的字符串表示形式
     */
    public String toString() {
        int iMax = array.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(getRaw(byteOffset(i)));
            if (i == iMax)
                return b.append(']').toString();
            b.append(',').append(' ');
        }
    }

}
