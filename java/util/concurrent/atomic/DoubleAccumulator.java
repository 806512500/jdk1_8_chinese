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
import java.io.Serializable;
import java.util.function.DoubleBinaryOperator;

/**
 * 一个或多个变量共同维护一个使用提供的函数更新的 {@code double} 值。当线程间更新（方法 {@link #accumulate}）发生竞争时，
 * 变量集可能会动态增长以减少竞争。方法 {@link #get}（或等效的 {@link #doubleValue}）返回维护更新的变量的当前值。
 *
 * <p>当多个线程更新一个常用值，该值用于频繁更新但较少读取的汇总统计信息时，此类通常优于其他替代方案。
 *
 * <p>提供的累加函数应该是无副作用的，因为它可能在由于线程间的竞争而导致尝试更新失败时重新应用。该函数以当前值作为第一个参数，
 * 给定的更新作为第二个参数。例如，要维护一个运行的最大值，您可以提供 {@code Double::max} 以及 {@code
 * Double.NEGATIVE_INFINITY} 作为标识。累积的顺序在内部或跨线程中不保证。因此，如果需要数值稳定性，尤其是当组合值的量级差异很大时，
 * 本类可能不适用。
 *
 * <p>类 {@link DoubleAdder} 为维护总和的常见特殊情况提供了此类功能的类似实现。调用 {@code new DoubleAdder()} 等同于
 * {@code new DoubleAccumulator((x, y) -> x + y, 0.0)}。
 *
 * <p>此类扩展了 {@link Number}，但不定义 {@code equals}、{@code hashCode} 和 {@code
 * compareTo} 方法，因为实例预计会被修改，因此不适合作为集合键。
 *
 * @since 1.8
 * @author Doug Lea
 */
public class DoubleAccumulator extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    private final DoubleBinaryOperator function;
    private final long identity; // 使用 long 表示

    /**
     * 使用给定的累加函数和标识元素创建一个新实例。
     * @param accumulatorFunction 无副作用的双参数函数
     * @param identity 累加函数的标识（初始值）
     */
    public DoubleAccumulator(DoubleBinaryOperator accumulatorFunction,
                             double identity) {
        this.function = accumulatorFunction;
        base = this.identity = Double.doubleToRawLongBits(identity);
    }

    /**
     * 使用给定的值进行更新。
     *
     * @param x 值
     */
    public void accumulate(double x) {
        Cell[] as; long b, v, r; int m; Cell a;
        if ((as = cells) != null ||
            (r = Double.doubleToRawLongBits
             (function.applyAsDouble
              (Double.longBitsToDouble(b = base), x))) != b  && !casBase(b, r)) {
            boolean uncontended = true;
            if (as == null || (m = as.length - 1) < 0 ||
                (a = as[getProbe() & m]) == null ||
                !(uncontended =
                  (r = Double.doubleToRawLongBits
                   (function.applyAsDouble
                    (Double.longBitsToDouble(v = a.value), x))) == v ||
                  a.cas(v, r)))
                doubleAccumulate(x, function, uncontended);
        }
    }

    /**
     * 返回当前值。返回的值 <em>不是</em> 原子快照；在没有并发更新的情况下调用返回准确的结果，但在值计算过程中发生的并发更新可能不会被包含。
     *
     * @return 当前值
     */
    public double get() {
        Cell[] as = cells; Cell a;
        double result = Double.longBitsToDouble(base);
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    result = function.applyAsDouble
                        (result, Double.longBitsToDouble(a.value));
            }
        }
        return result;
    }

    /**
     * 将维护更新的变量重置为标识值。如果存在并发更新，此方法可能是一个有用的替代方案，但只有在没有并发更新时才有效。因为此方法本质上是竞争的，
     * 所以只有在已知没有线程并发更新时才应使用。
     */
    public void reset() {
        Cell[] as = cells; Cell a;
        base = identity;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    a.value = identity;
            }
        }
    }

    /**
     * 效果等同于 {@link #get} 后跟 {@link #reset}。例如，在多线程计算之间的静默点期间，此方法可能适用。如果存在与此方法并发的更新，
     * 返回的值 <em>不</em> 保证是重置前发生的最终值。
     *
     * @return 重置前的值
     */
    public double getThenReset() {
        Cell[] as = cells; Cell a;
        double result = Double.longBitsToDouble(base);
        base = identity;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null) {
                    double v = Double.longBitsToDouble(a.value);
                    a.value = identity;
                    result = function.applyAsDouble(result, v);
                }
            }
        }
        return result;
    }

    /**
     * 返回当前值的字符串表示形式。
     * @return 当前值的字符串表示形式
     */
    public String toString() {
        return Double.toString(get());
    }

    /**
     * 效果等同于 {@link #get}。
     *
     * @return 当前值
     */
    public double doubleValue() {
        return get();
    }

    /**
     * 返回当前值作为 {@code long}，经过窄化原始转换。
     */
    public long longValue() {
        return (long)get();
    }

    /**
     * 返回当前值作为 {@code int}，经过窄化原始转换。
     */
    public int intValue() {
        return (int)get();
    }

    /**
     * 返回当前值作为 {@code float}，经过窄化原始转换。
     */
    public float floatValue() {
        return (float)get();
    }

    /**
     * 序列化代理，用于避免在序列化形式中引用非公共的 Striped64 超类。
     * @serial include
     */
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 7249069246863182397L;

        /**
         * 由 get() 返回的当前值。
         * @serial
         */
        private final double value;
        /**
         * 用于更新的函数。
         * @serial
         */
        private final DoubleBinaryOperator function;
        /**
         * 标识值
         * @serial
         */
        private final long identity;

        SerializationProxy(DoubleAccumulator a) {
            function = a.function;
            identity = a.identity;
            value = a.get();
        }

        /**
         * 返回一个具有此代理持有的初始状态的 {@code DoubleAccumulator} 对象。
         *
         * @return 一个具有此代理持有的初始状态的 {@code DoubleAccumulator} 对象。
         */
        private Object readResolve() {
            double d = Double.longBitsToDouble(identity);
            DoubleAccumulator a = new DoubleAccumulator(function, d);
            a.base = Double.doubleToRawLongBits(value);
            return a;
        }
    }

    /**
     * 返回一个表示此实例状态的
     * <a href="../../../../serialized-form.html#java.util.concurrent.atomic.DoubleAccumulator.SerializationProxy">
     * SerializationProxy</a>。
     *
     * @return 一个表示此实例状态的 {@link SerializationProxy}
     */
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    /**
     * @param s 流
     * @throws java.io.InvalidObjectException 始终抛出
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.InvalidObjectException {
        throw new java.io.InvalidObjectException("Proxy required");
    }

}
