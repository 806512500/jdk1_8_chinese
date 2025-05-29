
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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的公共领域发布。
 */

package java.util.concurrent.atomic;
import java.util.function.UnaryOperator;
import java.util.function.BinaryOperator;
import java.util.Arrays;
import java.lang.reflect.Array;
import sun.misc.Unsafe;

/**
 * 一个对象引用数组，其中的元素可以原子地更新。有关原子变量属性的描述，请参阅
 * {@link java.util.concurrent.atomic} 包的规范。
 * @since 1.5
 * @author Doug Lea
 * @param <E> 此数组中持有的元素的基本类
 */
public class AtomicReferenceArray<E> implements java.io.Serializable {
    private static final long serialVersionUID = -6209656149925076980L;

    private static final Unsafe unsafe;
    private static final int base;
    private static final int shift;
    private static final long arrayFieldOffset;
    private final Object[] array; // 必须具有确切类型 Object[]

    static {
        try {
            unsafe = Unsafe.getUnsafe();
            arrayFieldOffset = unsafe.objectFieldOffset
                (AtomicReferenceArray.class.getDeclaredField("array"));
            base = unsafe.arrayBaseOffset(Object[].class);
            int scale = unsafe.arrayIndexScale(Object[].class);
            if ((scale & (scale - 1)) != 0)
                throw new Error("数据类型比例不是2的幂");
            shift = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new Error(e);
        }
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
     * 创建一个给定长度的新 AtomicReferenceArray，所有元素最初为 null。
     *
     * @param length 数组的长度
     */
    public AtomicReferenceArray(int length) {
        array = new Object[length];
    }

    /**
     * 创建一个与给定数组长度相同且所有元素都从给定数组复制的新 AtomicReferenceArray。
     *
     * @param array 要从中复制元素的数组
     * @throws NullPointerException 如果数组为 null
     */
    public AtomicReferenceArray(E[] array) {
        // 通过 final 字段保证可见性
        this.array = Arrays.copyOf(array, array.length, Object[].class);
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
    public final E get(int i) {
        return getRaw(checkedByteOffset(i));
    }

    @SuppressWarnings("unchecked")
    private E getRaw(long offset) {
        return (E) unsafe.getObjectVolatile(array, offset);
    }

    /**
     * 将位置 {@code i} 的元素设置为给定值。
     *
     * @param i 索引
     * @param newValue 新值
     */
    public final void set(int i, E newValue) {
        unsafe.putObjectVolatile(array, checkedByteOffset(i), newValue);
    }

    /**
     * 最终将位置 {@code i} 的元素设置为给定值。
     *
     * @param i 索引
     * @param newValue 新值
     * @since 1.6
     */
    public final void lazySet(int i, E newValue) {
        unsafe.putOrderedObject(array, checkedByteOffset(i), newValue);
    }

    /**
     * 原子地将位置 {@code i} 的元素设置为给定值，并返回旧值。
     *
     * @param i 索引
     * @param newValue 新值
     * @return 旧值
     */
    @SuppressWarnings("unchecked")
    public final E getAndSet(int i, E newValue) {
        return (E)unsafe.getAndSetObject(array, checkedByteOffset(i), newValue);
    }

    /**
     * 如果当前值 {@code ==} 期望值，则原子地将位置 {@code i} 的元素设置为给定的更新值。
     *
     * @param i 索引
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}。返回 {@code false} 表示实际值不等于期望值。
     */
    public final boolean compareAndSet(int i, E expect, E update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }

    private boolean compareAndSetRaw(long offset, E expect, E update) {
        return unsafe.compareAndSwapObject(array, offset, expect, update);
    }

    /**
     * 如果当前值 {@code ==} 期望值，则原子地将位置 {@code i} 的元素设置为给定的更新值。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会失败并无法提供顺序保证</a>，因此很少是 {@code compareAndSet} 的适当替代方案。
     *
     * @param i 索引
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}
     */
    public final boolean weakCompareAndSet(int i, E expect, E update) {
        return compareAndSet(i, expect, update);
    }

    /**
     * 原子地使用给定函数的结果更新索引 {@code i} 处的元素，返回旧值。该函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，可能会重新应用该函数。
     *
     * @param i 索引
     * @param updateFunction 无副作用的函数
     * @return 旧值
     * @since 1.8
     */
    public final E getAndUpdate(int i, UnaryOperator<E> updateFunction) {
        long offset = checkedByteOffset(i);
        E prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.apply(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

                /**
     * 以原子方式更新索引 {@code i} 处的元素，应用给定的函数并返回更新后的值。该函数应该是没有副作用的，因为当由于线程间的竞争导致尝试更新失败时，它可能会被重新应用。
     *
     * @param i 索引
     * @param updateFunction 一个没有副作用的函数
     * @return 更新后的值
     * @since 1.8
     */
    public final E updateAndGet(int i, UnaryOperator<E> updateFunction) {
        long offset = checkedByteOffset(i);
        E prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.apply(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    /**
     * 以原子方式更新索引 {@code i} 处的元素，应用给定的函数到当前值和给定值，返回之前的值。该函数应该是没有副作用的，因为当由于线程间的竞争导致尝试更新失败时，它可能会被重新应用。该函数应用于索引 {@code i} 处的当前值作为第一个参数，给定的更新作为第二个参数。
     *
     * @param i 索引
     * @param x 更新值
     * @param accumulatorFunction 一个没有副作用的双参数函数
     * @return 之前的值
     * @since 1.8
     */
    public final E getAndAccumulate(int i, E x,
                                    BinaryOperator<E> accumulatorFunction) {
        long offset = checkedByteOffset(i);
        E prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.apply(prev, x);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

    /**
     * 以原子方式更新索引 {@code i} 处的元素，应用给定的函数到当前值和给定值，返回更新后的值。该函数应该是没有副作用的，因为当由于线程间的竞争导致尝试更新失败时，它可能会被重新应用。该函数应用于索引 {@code i} 处的当前值作为第一个参数，给定的更新作为第二个参数。
     *
     * @param i 索引
     * @param x 更新值
     * @param accumulatorFunction 一个没有副作用的双参数函数
     * @return 更新后的值
     * @since 1.8
     */
    public final E accumulateAndGet(int i, E x,
                                    BinaryOperator<E> accumulatorFunction) {
        long offset = checkedByteOffset(i);
        E prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.apply(prev, x);
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

    /**
     * 从流中恢复实例（即反序列化）。
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException,
        java.io.InvalidObjectException {
        // 注意：如果定义了任何附加字段，则必须更改此内容
        Object a = s.readFields().get("array", null);
        if (a == null || !a.getClass().isArray())
            throw new java.io.InvalidObjectException("Not array type");
        if (a.getClass() != Object[].class)
            a = Arrays.copyOf((Object[])a, Array.getLength(a), Object[].class);
        unsafe.putObjectVolatile(this, arrayFieldOffset, a);
    }

}
