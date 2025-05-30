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
 * 一个可以原子更新的 {@code long} 值。有关原子变量的属性，请参见 {@link java.util.concurrent.atomic} 包的说明。
 * {@code AtomicLong} 用于原子递增的序列号等应用，不能用作 {@link java.lang.Long} 的替代品。
 * 但是，此类扩展了 {@code Number} 以允许工具和实用程序以统一的方式访问基于数值的类。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicLong extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 1927816293512124184L;

    // 设置以使用 Unsafe.compareAndSwapLong 进行更新
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;

    /**
     * 记录底层 JVM 是否支持无锁的 long 类型的 compareAndSwap。
     * 虽然 Unsafe.compareAndSwapLong 方法在这两种情况下都能工作，但某些构造应该在 Java 层面处理以避免锁定用户可见的锁。
     */
    static final boolean VM_SUPPORTS_LONG_CAS = VMSupportsCS8();

    /**
     * 返回底层 JVM 是否支持无锁的 long 类型的 CompareAndSet。
     * 仅调用一次并缓存在 VM_SUPPORTS_LONG_CAS 中。
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
     * 如果当前值 {@code ==} 期望值，则原子地设置为给定的更新值。
     *
     * @param expect 期望值
     * @param update 新值
     * @return 如果成功则返回 {@code true}。返回 {@code false} 表示实际值不等于期望值。
     */
    public final boolean compareAndSet(long expect, long update) {
        return unsafe.compareAndSwapLong(this, valueOffset, expect, update);
    }

    /**
     * 如果当前值 {@code ==} 期望值，则原子地设置为给定的更新值。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">可能会失败且不提供顺序保证</a>，因此很少是 {@code compareAndSet} 的合适替代方案。
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
     * 原子地将给定的值加到当前值上。
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
     * 原子地将给定的值加到当前值上。
     *
     * @param delta 要加的值
     * @return 更新后的值
     */
    public final long addAndGet(long delta) {
        return unsafe.getAndAddLong(this, valueOffset, delta) + delta;
    }

    /**
     * 原子地使用给定的函数更新当前值，返回之前的值。该函数应该是无副作用的，因为当线程之间发生争用时，尝试更新失败时可能会重新应用该函数。
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
     * 原子地使用给定的函数更新当前值，返回更新后的值。该函数应该是无副作用的，因为当线程之间发生争用时，尝试更新失败时可能会重新应用该函数。
     *
     * @param updateFunction 无副作用的函数
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
     * 原子地使用给定的函数将当前值和给定值的结果更新为当前值，返回之前的值。该函数应该是无副作用的，因为当线程之间发生争用时，尝试更新失败时可能会重新应用该函数。该函数以当前值作为第一个参数，给定的更新作为第二个参数。
     *
     * @param x 更新值
     * @param accumulatorFunction 无副作用的双参数函数
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
     * 原子地使用给定的函数将当前值和给定值的结果更新为当前值，返回更新后的值。该函数应该是无副作用的，因为当线程之间发生争用时，尝试更新失败时可能会重新应用该函数。该函数以当前值作为第一个参数，给定的更新作为第二个参数。
     *
     * @param x 更新值
     * @param accumulatorFunction 无副作用的双参数函数
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
     * 返回此 {@code AtomicLong} 的值作为 {@code int}，经过窄化原始转换。
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
     * 返回此 {@code AtomicLong} 的值作为 {@code float}，经过扩展原始转换。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public float floatValue() {
        return (float)get();
    }

    /**
     * 返回此 {@code AtomicLong} 的值作为 {@code double}，经过扩展原始转换。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public double doubleValue() {
        return (double)get();
    }

}
