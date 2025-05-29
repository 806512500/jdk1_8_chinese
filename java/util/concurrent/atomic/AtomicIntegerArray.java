
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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写并发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的。
 */

package java.util.concurrent.atomic;
import java.util.function.IntUnaryOperator;
import java.util.function.IntBinaryOperator;
import sun.misc.Unsafe;

/**
 * 一个可以原子更新元素的 {@code int} 数组。
 * 有关原子变量属性的描述，请参阅 {@link java.util.concurrent.atomic} 包
 * 规范。
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicIntegerArray implements java.io.Serializable {
    private static final long serialVersionUID = 2862133569453604235L;

    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final int base = unsafe.arrayBaseOffset(int[].class);
    private static final int shift;
    private final int[] array;

    static {
        int scale = unsafe.arrayIndexScale(int[].class);
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
     * 创建一个给定长度的新 AtomicIntegerArray，所有元素最初为零。
     *
     * @param length 数组的长度
     */
    public AtomicIntegerArray(int length) {
        array = new int[length];
    }

    /**
     * 创建一个与给定数组长度相同且所有元素都从给定数组复制的新 AtomicIntegerArray。
     *
     * @param array 要从中复制元素的数组
     * @throws NullPointerException 如果数组为 null
     */
    public AtomicIntegerArray(int[] array) {
        // 由最终字段保证可见性
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
    public final int get(int i) {
        return getRaw(checkedByteOffset(i));
    }

    private int getRaw(long offset) {
        return unsafe.getIntVolatile(array, offset);
    }

    /**
     * 将位置 {@code i} 的元素设置为给定值。
     *
     * @param i 索引
     * @param newValue 新值
     */
    public final void set(int i, int newValue) {
        unsafe.putIntVolatile(array, checkedByteOffset(i), newValue);
    }

    /**
     * 最终将位置 {@code i} 的元素设置为给定值。
     *
     * @param i 索引
     * @param newValue 新值
     * @since 1.6
     */
    public final void lazySet(int i, int newValue) {
        unsafe.putOrderedInt(array, checkedByteOffset(i), newValue);
    }

    /**
     * 原子地将位置 {@code i} 的元素设置为给定值并返回旧值。
     *
     * @param i 索引
     * @param newValue 新值
     * @return 旧值
     */
    public final int getAndSet(int i, int newValue) {
        return unsafe.getAndSetInt(array, checkedByteOffset(i), newValue);
    }

    /**
     * 如果当前值 {@code ==} 期望值，则原子地将位置 {@code i} 的元素设置为给定的更新值。
     *
     * @param i 索引
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}。返回 {@code false} 表示实际值不等于期望值。
     */
    public final boolean compareAndSet(int i, int expect, int update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }

    private boolean compareAndSetRaw(long offset, int expect, int update) {
        return unsafe.compareAndSwapInt(array, offset, expect, update);
    }

    /**
     * 如果当前值 {@code ==} 期望值，则原子地将位置 {@code i} 的元素设置为给定的更新值。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会失败，并且不提供顺序保证</a>，因此很少是 {@code compareAndSet} 的合适替代方案。
     *
     * @param i 索引
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     */
    public final boolean weakCompareAndSet(int i, int expect, int update) {
        return compareAndSet(i, expect, update);
    }

    /**
     * 原子地将索引 {@code i} 处的元素加一。
     *
     * @param i 索引
     * @return 旧值
     */
    public final int getAndIncrement(int i) {
        return getAndAdd(i, 1);
    }

    /**
     * 原子地将索引 {@code i} 处的元素减一。
     *
     * @param i 索引
     * @return 旧值
     */
    public final int getAndDecrement(int i) {
        return getAndAdd(i, -1);
    }

    /**
     * 原子地将给定值加到索引 {@code i} 处的元素。
     *
     * @param i 索引
     * @param delta 要加的值
     * @return 旧值
     */
    public final int getAndAdd(int i, int delta) {
        return unsafe.getAndAddInt(array, checkedByteOffset(i), delta);
    }

    /**
     * 原子地将索引 {@code i} 处的元素加一。
     *
     * @param i 索引
     * @return 更新后的值
     */
    public final int incrementAndGet(int i) {
        return getAndAdd(i, 1) + 1;
    }

    /**
     * 原子地将索引 {@code i} 处的元素减一。
     *
     * @param i 索引
     * @return 更新后的值
     */
    public final int decrementAndGet(int i) {
        return getAndAdd(i, -1) - 1;
    }


/**
 * 以原子方式将给定的值添加到索引 {@code i} 处的元素。
 *
 * @param i 索引
 * @param delta 要添加的值
 * @return 更新后的值
 */
public final int addAndGet(int i, int delta) {
    return getAndAdd(i, delta) + delta;
}


/**
 * 以原子方式使用给定的函数更新索引 {@code i} 处的元素，返回旧值。该函数应该是没有副作用的，因为当线程间竞争导致更新尝试失败时，它可能会被重新应用。
 *
 * @param i 索引
 * @param updateFunction 没有副作用的函数
 * @return 旧值
 * @since 1.8
 */
public final int getAndUpdate(int i, IntUnaryOperator updateFunction) {
    long offset = checkedByteOffset(i);
    int prev, next;
    do {
        prev = getRaw(offset);
        next = updateFunction.applyAsInt(prev);
    } while (!compareAndSetRaw(offset, prev, next));
    return prev;
}

/**
 * 以原子方式使用给定的函数更新索引 {@code i} 处的元素，返回更新后的值。该函数应该是没有副作用的，因为当线程间竞争导致更新尝试失败时，它可能会被重新应用。
 *
 * @param i 索引
 * @param updateFunction 没有副作用的函数
 * @return 更新后的值
 * @since 1.8
 */
public final int updateAndGet(int i, IntUnaryOperator updateFunction) {
    long offset = checkedByteOffset(i);
    int prev, next;
    do {
        prev = getRaw(offset);
        next = updateFunction.applyAsInt(prev);
    } while (!compareAndSetRaw(offset, prev, next));
    return next;
}

/**
 * 以原子方式使用给定的函数对当前值和给定值应用结果来更新索引 {@code i} 处的元素，返回旧值。该函数应该是没有副作用的，因为当线程间竞争导致更新尝试失败时，它可能会被重新应用。该函数以索引 {@code i} 处的当前值作为第一个参数，给定的更新值作为第二个参数。
 *
 * @param i 索引
 * @param x 更新值
 * @param accumulatorFunction 没有副作用的双参数函数
 * @return 旧值
 * @since 1.8
 */
public final int getAndAccumulate(int i, int x,
                                  IntBinaryOperator accumulatorFunction) {
    long offset = checkedByteOffset(i);
    int prev, next;
    do {
        prev = getRaw(offset);
        next = accumulatorFunction.applyAsInt(prev, x);
    } while (!compareAndSetRaw(offset, prev, next));
    return prev;
}

/**
 * 以原子方式使用给定的函数对当前值和给定值应用结果来更新索引 {@code i} 处的元素，返回更新后的值。该函数应该是没有副作用的，因为当线程间竞争导致更新尝试失败时，它可能会被重新应用。该函数以索引 {@code i} 处的当前值作为第一个参数，给定的更新值作为第二个参数。
 *
 * @param i 索引
 * @param x 更新值
 * @param accumulatorFunction 没有副作用的双参数函数
 * @return 更新后的值
 * @since 1.8
 */
public final int accumulateAndGet(int i, int x,
                                  IntBinaryOperator accumulatorFunction) {
    long offset = checkedByteOffset(i);
    int prev, next;
    do {
        prev = getRaw(offset);
        next = accumulatorFunction.applyAsInt(prev, x);
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
