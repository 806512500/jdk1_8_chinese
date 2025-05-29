
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
 * 由 Doug Lea 编写，并在 JCP JSR-166 专家小组成员的帮助下发布到公共领域，如在
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的。
 */

package java.util.concurrent.atomic;
import java.util.function.UnaryOperator;
import java.util.function.BinaryOperator;
import sun.misc.Unsafe;

/**
 * 一个可以原子更新的对象引用。有关原子变量属性的描述，请参见 {@link
 * java.util.concurrent.atomic} 包的说明。
 * @since 1.5
 * @author Doug Lea
 * @param <V> 此引用指向的对象类型
 */
public class AtomicReference<V> implements java.io.Serializable {
    private static final long serialVersionUID = -1848883965231344442L;

    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;

    static {
        try {
            valueOffset = unsafe.objectFieldOffset
                (AtomicReference.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    private volatile V value;

    /**
     * 使用给定的初始值创建一个新的 AtomicReference。
     *
     * @param initialValue 初始值
     */
    public AtomicReference(V initialValue) {
        value = initialValue;
    }

    /**
     * 使用 null 作为初始值创建一个新的 AtomicReference。
     */
    public AtomicReference() {
    }

    /**
     * 获取当前值。
     *
     * @return 当前值
     */
    public final V get() {
        return value;
    }

    /**
     * 设置为给定的值。
     *
     * @param newValue 新值
     */
    public final void set(V newValue) {
        value = newValue;
    }

    /**
     * 最终设置为给定的值。
     *
     * @param newValue 新值
     * @since 1.6
     */
    public final void lazySet(V newValue) {
        unsafe.putOrderedObject(this, valueOffset, newValue);
    }

    /**
     * 如果当前值 {@code ==} 期望值，则原子地将值设置为给定的更新值。
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}。返回 {@code false} 表示实际值不等于期望值。
     */
    public final boolean compareAndSet(V expect, V update) {
        return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
    }

    /**
     * 如果当前值 {@code ==} 期望值，则原子地将值设置为给定的更新值。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会无故失败并且不提供顺序保证</a>，因此很少是 {@code compareAndSet} 的合适替代方案。
     *
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     */
    public final boolean weakCompareAndSet(V expect, V update) {
        return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
    }

    /**
     * 原子地将值设置为给定值并返回旧值。
     *
     * @param newValue 新值
     * @return 旧值
     */
    @SuppressWarnings("unchecked")
    public final V getAndSet(V newValue) {
        return (V)unsafe.getAndSetObject(this, valueOffset, newValue);
    }

    /**
     * 原子地使用给定函数的结果更新当前值，返回旧值。该函数应该是无副作用的，因为当由于线程间的竞争导致尝试更新失败时，可能会重新应用该函数。
     *
     * @param updateFunction 无副作用的函数
     * @return 旧值
     * @since 1.8
     */
    public final V getAndUpdate(UnaryOperator<V> updateFunction) {
        V prev, next;
        do {
            prev = get();
            next = updateFunction.apply(prev);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    /**
     * 原子地使用给定函数的结果更新当前值，返回更新后的值。该函数应该是无副作用的，因为当由于线程间的竞争导致尝试更新失败时，可能会重新应用该函数。
     *
     * @param updateFunction 无副作用的函数
     * @return 更新后的值
     * @since 1.8
     */
    public final V updateAndGet(UnaryOperator<V> updateFunction) {
        V prev, next;
        do {
            prev = get();
            next = updateFunction.apply(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }

    /**
     * 原子地使用给定函数对当前值和给定值应用的结果更新当前值，返回旧值。该函数应该是无副作用的，因为当由于线程间的竞争导致尝试更新失败时，可能会重新应用该函数。该函数以当前值作为第一个参数，给定更新作为第二个参数。
     *
     * @param x 更新值
     * @param accumulatorFunction 无副作用的双参数函数
     * @return 旧值
     * @since 1.8
     */
    public final V getAndAccumulate(V x,
                                    BinaryOperator<V> accumulatorFunction) {
        V prev, next;
        do {
            prev = get();
            next = accumulatorFunction.apply(prev, x);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    /**
     * 原子地使用给定函数对当前值和给定值应用的结果更新当前值，返回更新后的值。该函数应该是无副作用的，因为当由于线程间的竞争导致尝试更新失败时，可能会重新应用该函数。该函数以当前值作为第一个参数，给定更新作为第二个参数。
     *
     * @param x 更新值
     * @param accumulatorFunction 无副作用的双参数函数
     * @return 更新后的值
     * @since 1.8
     */
    public final V accumulateAndGet(V x,
                                    BinaryOperator<V> accumulatorFunction) {
        V prev, next;
        do {
            prev = get();
            next = accumulatorFunction.apply(prev, x);
        } while (!compareAndSet(prev, next));
        return next;
    }

    /**
     * 返回当前值的字符串表示形式。
     * @return 当前值的字符串表示形式
     */
    public String toString() {
        return String.valueOf(get());
    }

}
