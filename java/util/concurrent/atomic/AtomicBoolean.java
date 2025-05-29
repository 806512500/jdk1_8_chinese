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
 * 由 Doug Lea 在 JCP JSR-166 专家组成员的帮助下编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释发布到公共领域。
 */

package java.util.concurrent.atomic;
import sun.misc.Unsafe;

/**
 * 一个可以原子更新的 {@code boolean} 值。有关原子变量属性的描述，请参阅
 * {@link java.util.concurrent.atomic} 包的规范。{@code AtomicBoolean}
 * 用于原子更新标志等应用程序，不能用作 {@link java.lang.Boolean} 的替代品。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicBoolean implements java.io.Serializable {
    private static final long serialVersionUID = 4654671469794556979L;
    // 设置以使用 Unsafe.compareAndSwapInt 进行更新
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;

    static {
        try {
            valueOffset = unsafe.objectFieldOffset
                (AtomicBoolean.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    private volatile int value;

    /**
     * 使用给定的初始值创建一个新的 {@code AtomicBoolean}。
     *
     * @param initialValue 初始值
     */
    public AtomicBoolean(boolean initialValue) {
        value = initialValue ? 1 : 0;
    }

    /**
     * 使用初始值 {@code false} 创建一个新的 {@code AtomicBoolean}。
     */
    public AtomicBoolean() {
    }

    /**
     * 返回当前值。
     *
     * @return 当前值
     */
    public final boolean get() {
        return value != 0;
    }

    /**
     * 如果当前值 {@code ==} 预期值，则原子地将值设置为给定的更新值。
     *
     * @param expect 预期值
     * @param update 新值
     * @return 如果成功则返回 {@code true}。返回 {@code false} 表示实际值不等于预期值。
     */
    public final boolean compareAndSet(boolean expect, boolean update) {
        int e = expect ? 1 : 0;
        int u = update ? 1 : 0;
        return unsafe.compareAndSwapInt(this, valueOffset, e, u);
    }

    /**
     * 如果当前值 {@code ==} 预期值，则原子地将值设置为给定的更新值。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会无故失败且不提供顺序保证</a>，因此很少是 {@code compareAndSet} 的合适替代品。
     *
     * @param expect 预期值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     */
    public boolean weakCompareAndSet(boolean expect, boolean update) {
        int e = expect ? 1 : 0;
        int u = update ? 1 : 0;
        return unsafe.compareAndSwapInt(this, valueOffset, e, u);
    }

    /**
     * 无条件地设置为给定值。
     *
     * @param newValue 新值
     */
    public final void set(boolean newValue) {
        value = newValue ? 1 : 0;
    }

    /**
     * 最终设置为给定值。
     *
     * @param newValue 新值
     * @since 1.6
     */
    public final void lazySet(boolean newValue) {
        int v = newValue ? 1 : 0;
        unsafe.putOrderedInt(this, valueOffset, v);
    }

    /**
     * 原子地设置为给定值并返回前一个值。
     *
     * @param newValue 新值
     * @return 前一个值
     */
    public final boolean getAndSet(boolean newValue) {
        boolean prev;
        do {
            prev = get();
        } while (!compareAndSet(prev, newValue));
        return prev;
    }

    /**
     * 返回当前值的字符串表示形式。
     * @return 当前值的字符串表示形式
     */
    public String toString() {
        return Boolean.toString(get());
    }

}
