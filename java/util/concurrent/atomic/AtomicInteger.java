
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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并按照
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的方式发布到公共领域。
 */

package java.util.concurrent.atomic;
import java.util.function.IntUnaryOperator;
import java.util.function.IntBinaryOperator;
import sun.misc.Unsafe;

/**
 * 一个可以原子更新的 {@code int} 值。有关原子变量属性的描述，请参阅
 * {@link java.util.concurrent.atomic} 包的规范。{@code AtomicInteger}
 * 用于原子递增计数器等应用程序，不能作为 {@link java.lang.Integer} 的替代品。然而，此类确实扩展了
 * {@code Number} 以允许工具和实用程序以统一的方式处理基于数字的类。
 *
 * @since 1.5
 * @author Doug Lea
*/
public class AtomicInteger extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 6214790243416807050L;

    // 设置使用 Unsafe.compareAndSwapInt 进行更新
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;

    static {
        try {
            valueOffset = unsafe.objectFieldOffset
                (AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    private volatile int value;

    /**
     * 使用给定的初始值创建一个新的 AtomicInteger。
     *
     * @param initialValue 初始值
     */
    public AtomicInteger(int initialValue) {
        value = initialValue;
    }

    /**
     * 使用初始值 {@code 0} 创建一个新的 AtomicInteger。
     */
    public AtomicInteger() {
    }

    /**
     * 获取当前值。
     *
     * @return 当前值
     */
    public final int get() {
        return value;
    }

    /**
     * 设置为给定的值。
     *
     * @param newValue 新值
     */
    public final void set(int newValue) {
        value = newValue;
    }

    /**
     * 最终设置为给定的值。
     *
     * @param newValue 新值
     * @since 1.6
     */
    public final void lazySet(int newValue) {
        unsafe.putOrderedInt(this, valueOffset, newValue);
    }

    /**
     * 原子地设置为给定的值并返回旧值。
     *
     * @param newValue 新值
     * @return 之前的值
     */
    public final int getAndSet(int newValue) {
        return unsafe.getAndSetInt(this, valueOffset, newValue);
    }

    /**
     * 如果当前值 {@code ==} 预期值，则原子地设置值为给定的更新值。
     *
     * @param expect 预期值
     * @param update 新值
     * @return 如果成功则返回 {@code true}。返回 {@code false} 表示实际值不等于预期值。
     */
    public final boolean compareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }

    /**
     * 如果当前值 {@code ==} 预期值，则原子地设置值为给定的更新值。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会无故失败且不提供顺序保证</a>，因此很少是 {@code compareAndSet} 的适当替代品。
     *
     * @param expect 预期值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     */
    public final boolean weakCompareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }

    /**
     * 原子地将当前值递增一。
     *
     * @return 之前的值
     */
    public final int getAndIncrement() {
        return unsafe.getAndAddInt(this, valueOffset, 1);
    }

    /**
     * 原子地将当前值递减一。
     *
     * @return 之前的值
     */
    public final int getAndDecrement() {
        return unsafe.getAndAddInt(this, valueOffset, -1);
    }

    /**
     * 原子地将给定值加到当前值上。
     *
     * @param delta 要加的值
     * @return 之前的值
     */
    public final int getAndAdd(int delta) {
        return unsafe.getAndAddInt(this, valueOffset, delta);
    }

    /**
     * 原子地将当前值递增一。
     *
     * @return 更新后的值
     */
    public final int incrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
    }

    /**
     * 原子地将当前值递减一。
     *
     * @return 更新后的值
     */
    public final int decrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, -1) - 1;
    }

    /**
     * 原子地将给定值加到当前值上。
     *
     * @param delta 要加的值
     * @return 更新后的值
     */
    public final int addAndGet(int delta) {
        return unsafe.getAndAddInt(this, valueOffset, delta) + delta;
    }

    /**
     * 原子地使用给定函数的结果更新当前值，返回之前的值。函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，可能会重新应用该函数。
     *
     * @param updateFunction 无副作用的函数
     * @return 之前的值
     * @since 1.8
     */
    public final int getAndUpdate(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = get();
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    /**
     * 原子地使用给定函数的结果更新当前值，返回更新后的值。函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，可能会重新应用该函数。
     *
     * @param updateFunction 无副作用的函数
     * @return 更新后的值
     * @since 1.8
     */
    public final int updateAndGet(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = get();
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }


/**
 * 原子地使用给定函数对当前值和给定值进行操作的结果更新当前值，
 * 返回之前的值。该函数应该是没有副作用的，因为当由于线程间的竞争导致尝试更新失败时，
 * 可能会重新应用该函数。该函数以当前值作为第一个参数，给定的更新作为第二个参数。
 *
 * @param x 更新值
 * @param accumulatorFunction 一个没有副作用的二元函数
 * @return 之前的值
 * @since 1.8
 */
public final int getAndAccumulate(int x,
                                  IntBinaryOperator accumulatorFunction) {
    int prev, next;
    do {
        prev = get();
        next = accumulatorFunction.applyAsInt(prev, x);
    } while (!compareAndSet(prev, next));
    return prev;
}

/**
 * 原子地使用给定函数对当前值和给定值进行操作的结果更新当前值，
 * 返回更新后的值。该函数应该是没有副作用的，因为当由于线程间的竞争导致尝试更新失败时，
 * 可能会重新应用该函数。该函数以当前值作为第一个参数，给定的更新作为第二个参数。
 *
 * @param x 更新值
 * @param accumulatorFunction 一个没有副作用的二元函数
 * @return 更新后的值
 * @since 1.8
 */
public final int accumulateAndGet(int x,
                                  IntBinaryOperator accumulatorFunction) {
    int prev, next;
    do {
        prev = get();
        next = accumulatorFunction.applyAsInt(prev, x);
    } while (!compareAndSet(prev, next));
    return next;
}

/**
 * 返回当前值的字符串表示形式。
 * @return 当前值的字符串表示形式
 */
public String toString() {
    return Integer.toString(get());
}

/**
 * 返回此 {@code AtomicInteger} 的值作为 {@code int}。
 */
public int intValue() {
    return get();
}

/**
 * 返回此 {@code AtomicInteger} 的值作为 {@code long}，经过扩展的原始类型转换。
 * @jls 5.1.2 扩展原始类型转换
 */
public long longValue() {
    return (long)get();
}

/**
 * 返回此 {@code AtomicInteger} 的值作为 {@code float}，经过扩展的原始类型转换。
 * @jls 5.1.2 扩展原始类型转换
 */
public float floatValue() {
    return (float)get();
}

/**
 * 返回此 {@code AtomicInteger} 的值作为 {@code double}，经过扩展的原始类型转换。
 * @jls 5.1.2 扩展原始类型转换
 */
public double doubleValue() {
    return (double)get();
}
