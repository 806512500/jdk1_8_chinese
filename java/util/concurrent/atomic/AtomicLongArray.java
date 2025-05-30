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

package java.util.concurrent.atomic;
import java.util.function.LongUnaryOperator;
import java.util.function.LongBinaryOperator;
import sun.misc.Unsafe;

/**
 * 一个 {@code long} 数组，其中的元素可以原子地更新。
 * 有关原子变量的属性的描述，请参见 {@link java.util.concurrent.atomic} 包的说明。
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
     * 创建一个新的给定长度的 AtomicLongArray，所有元素初始值为零。
     *
     * @param length 数组的长度
     */
    public AtomicLongArray(int length) {
        array = new long[length];
    }

    /**
     * 创建一个新的 AtomicLongArray，长度与给定数组相同，并且所有元素从给定数组复制。
     *
     * @param array 要复制元素的数组
     * @throws NullPointerException 如果数组为 null
     */
    public AtomicLongArray(long[] array) {
        // 通过最终字段保证可见性
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
     * 原子地将位置 {@code i} 的元素设置为给定值，并返回旧值。
     *
     * @param i 索引
     * @param newValue 新值
     * @return 旧值
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
     * <p><a href="package-summary.html#weakCompareAndSet">可能会无故失败，并且不提供顺序保证</a>，因此很少是 {@code compareAndSet} 的合适替代方案。
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
     * @return 旧值
     */
    public final long getAndIncrement(int i) {
        return getAndAdd(i, 1);
    }

    /**
     * 原子地将索引 {@code i} 处的元素减一。
     *
     * @param i 索引
     * @return 旧值
     */
    public final long getAndDecrement(int i) {
        return getAndAdd(i, -1);
    }

    /**
     * 原子地将给定值加到索引 {@code i} 处的元素。
     *
     * @param i 索引
     * @param delta 要加的值
     * @return 旧值
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
     * 原子地将给定值加到索引 {@code i} 处的元素。
     *
     * @param i 索引
     * @param delta 要加的值
     * @return 更新后的值
     */
    public long addAndGet(int i, long delta) {
        return getAndAdd(i, delta) + delta;
    }

    /**
     * 原子地使用给定函数的结果更新索引 {@code i} 处的元素，返回旧值。该函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，它可能会被重新应用。
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
     * 原子地使用给定函数的结果更新索引 {@code i} 处的元素，返回更新后的值。该函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，它可能会被重新应用。
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
     * 原子地使用给定函数的结果更新索引 {@code i} 处的元素，返回旧值。该函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，它可能会被重新应用。该函数应用于索引 {@code i} 处的当前值作为第一个参数，给定更新作为第二个参数。
     *
     * @param i 索引
     * @param x 更新值
     * @param accumulatorFunction 无副作用的二元函数
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
     * 原子地使用给定函数的结果更新索引 {@code i} 处的元素，返回更新后的值。该函数应该是无副作用的，因为当线程间竞争导致尝试更新失败时，它可能会被重新应用。该函数应用于索引 {@code i} 处的当前值作为第一个参数，给定更新作为第二个参数。
     *
     * @param i 索引
     * @param x 更新值
     * @param accumulatorFunction 无副作用的二元函数
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
