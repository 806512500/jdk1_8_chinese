
/*
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

/*
 *
 *
 *
 *
 *
 * 由 Doug Lea 与 JCP JSR-166 专家小组成员合作编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的条款发布到公共领域。
 */

package java.util.concurrent.atomic;
import java.util.function.LongUnaryOperator;
import java.util.function.LongBinaryOperator;
import sun.misc.Unsafe;

/**
 * 一个可以原子更新的 {@code long} 值。有关原子变量属性的描述，请参见
 * {@link java.util.concurrent.atomic} 包的说明。{@code AtomicLong}
 * 用于需要原子递增序列号的应用程序，不能用作 {@link java.lang.Long} 的替代品。
 * 但是，此类确实扩展了 {@code Number} 以允许工具和实用程序以统一的方式访问基于数字的类。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicLong extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 1927816293512124184L;

    // 设置使用 Unsafe.compareAndSwapLong 进行更新
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;

    /**
     * 记录底层 JVM 是否支持无锁的 long 类型的 compareAndSwap。虽然 Unsafe.compareAndSwapLong
     * 方法在这两种情况下都能工作，但为了避免锁定用户可见的锁，某些构造应在 Java 层面处理。
     */
    static final boolean VM_SUPPORTS_LONG_CAS = VMSupportsCS8();

    /**
     * 返回底层 JVM 是否支持无锁的 long 类型的 CompareAndSet。仅调用一次并缓存在 VM_SUPPORTS_LONG_CAS 中。
     */
    private static native boolean VMSupportsCS8();

    static {
        try {
            valueOffset = unsafe.objectFieldOffset
                (AtomicLong.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    private volatile long value;

    /**
     * 使用给定的初始值创建一个新的 AtomicLong。
     *
     * @param initialValue 初始值
     */
    public AtomicLong(long initialValue) {
        value = initialValue;
    }

    /**
     * 使用初始值 {@code 0} 创建一个新的 AtomicLong。
     */
    public AtomicLong() {
    }

    /**
     * 获取当前值。
     *
     * @return 当前值
     */
    public final long get() {
        return value;
    }

    /**
     * 设置为给定的值。
     *
     * @param newValue 新值
     */
    public final void set(long newValue) {
        value = newValue;
    }

    /**
     * 最终设置为给定的值。
     *
     * @param newValue 新值
     * @since 1.6
     */
    public final void lazySet(long newValue) {
        unsafe.putOrderedLong(this, valueOffset, newValue);
    }

    /**
     * 原子地设置为给定的值并返回旧值。
     *
     * @param newValue 新值
     * @return 之前的值
     */
    public final long getAndSet(long newValue) {
        return unsafe.getAndSetLong(this, valueOffset, newValue);
    }

    /**
     * 如果当前值 {@code ==} 期望值，则原子地设置值为给定的更新值。
     *
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}。返回 {@code false} 表示实际值不等于期望值。
     */
    public final boolean compareAndSet(long expect, long update) {
        return unsafe.compareAndSwapLong(this, valueOffset, expect, update);
    }

    /**
     * 如果当前值 {@code ==} 期望值，则原子地设置值为给定的更新值。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会无故失败并且不提供顺序保证</a>，
     * 因此通常不是 {@code compareAndSet} 的合适替代方案。
     *
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     */
    public final boolean weakCompareAndSet(long expect, long update) {
        return unsafe.compareAndSwapLong(this, valueOffset, expect, update);
    }

    /**
     * 原子地将当前值加一。
     *
     * @return 之前的值
     */
    public final long getAndIncrement() {
        return unsafe.getAndAddLong(this, valueOffset, 1L);
    }

    /**
     * 原子地将当前值减一。
     *
     * @return 之前的值
     */
    public final long getAndDecrement() {
        return unsafe.getAndAddLong(this, valueOffset, -1L);
    }

    /**
     * 原子地将给定值加到当前值上。
     *
     * @param delta 要加的值
     * @return 之前的值
     */
    public final long getAndAdd(long delta) {
        return unsafe.getAndAddLong(this, valueOffset, delta);
    }

    /**
     * 原子地将当前值加一。
     *
     * @return 更新后的值
     */
    public final long incrementAndGet() {
        return unsafe.getAndAddLong(this, valueOffset, 1L) + 1L;
    }

    /**
     * 原子地将当前值减一。
     *
     * @return 更新后的值
     */
    public final long decrementAndGet() {
        return unsafe.getAndAddLong(this, valueOffset, -1L) - 1L;
    }

    /**
     * 原子地将给定值加到当前值上。
     *
     * @param delta 要加的值
     * @return 更新后的值
     */
    public final long addAndGet(long delta) {
        return unsafe.getAndAddLong(this, valueOffset, delta) + delta;
    }

    /**
     * 原子地使用给定的函数更新当前值，返回旧值。函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，可能会重新应用该函数。
     *
     * @param updateFunction 无副作用的函数
     * @return 之前的值
     * @since 1.8
     */
    public final long getAndUpdate(LongUnaryOperator updateFunction) {
        long prev, next;
        do {
            prev = get();
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSet(prev, next));
        return prev;
    }


/**
 * 以原子方式使用给定函数的结果更新当前值，并返回更新后的值。该函数应该是没有副作用的，因为当线程之间发生争用时，可能会重新应用该函数。
 *
 * @param updateFunction 一个没有副作用的函数
 * @return 更新后的值
 * @since 1.8
 */
public final long updateAndGet(LongUnaryOperator updateFunction) {
    long prev, next;
    do {
        prev = get();
        next = updateFunction.applyAsLong(prev);
    } while (!compareAndSet(prev, next));
    return next;
}

/**
 * 以原子方式使用给定函数对当前值和给定值进行操作的结果更新当前值，并返回之前的值。该函数应该是没有副作用的，因为当线程之间发生争用时，可能会重新应用该函数。该函数以当前值作为第一个参数，给定的更新值作为第二个参数。
 *
 * @param x 更新值
 * @param accumulatorFunction 一个没有副作用的双参数函数
 * @return 之前的值
 * @since 1.8
 */
public final long getAndAccumulate(long x,
                                   LongBinaryOperator accumulatorFunction) {
    long prev, next;
    do {
        prev = get();
        next = accumulatorFunction.applyAsLong(prev, x);
    } while (!compareAndSet(prev, next));
    return prev;
}

/**
 * 以原子方式使用给定函数对当前值和给定值进行操作的结果更新当前值，并返回更新后的值。该函数应该是没有副作用的，因为当线程之间发生争用时，可能会重新应用该函数。该函数以当前值作为第一个参数，给定的更新值作为第二个参数。
 *
 * @param x 更新值
 * @param accumulatorFunction 一个没有副作用的双参数函数
 * @return 更新后的值
 * @since 1.8
 */
public final long accumulateAndGet(long x,
                                   LongBinaryOperator accumulatorFunction) {
    long prev, next;
    do {
        prev = get();
        next = accumulatorFunction.applyAsLong(prev, x);
    } while (!compareAndSet(prev, next));
    return next;
}

/**
 * 返回当前值的字符串表示形式。
 * @return 当前值的字符串表示形式
 */
public String toString() {
    return Long.toString(get());
}

/**
 * 返回此 {@code AtomicLong} 的值作为 {@code int}，在进行了窄化原始类型转换之后。
 * @jls 5.1.3 Narrowing Primitive Conversions
 */
public int intValue() {
    return (int)get();
}

/**
 * 返回此 {@code AtomicLong} 的值作为 {@code long}。
 */
public long longValue() {
    return get();
}

/**
 * 返回此 {@code AtomicLong} 的值作为 {@code float}，在进行了宽化原始类型转换之后。
 * @jls 5.1.2 Widening Primitive Conversions
 */
public float floatValue() {
    return (float)get();
}

/**
 * 返回此 {@code AtomicLong} 的值作为 {@code double}，在进行了宽化原始类型转换之后。
 * @jls 5.1.2 Widening Primitive Conversions
 */
public double doubleValue() {
    return (double)get();
}

}
