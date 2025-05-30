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
import java.util.function.LongBinaryOperator;

/**
 * 一个或多个变量共同维护一个使用提供的函数更新的 {@code long} 值。当更新（方法 {@link #accumulate}）在多个线程中竞争时，变量集可能会动态增长以减少竞争。方法 {@link #get}（或等效的 {@link #longValue}）返回维护更新的变量的当前值。
 *
 * <p>当多个线程更新一个用于收集统计信息而不是用于细粒度同步控制的公共值时，这个类通常优于 {@link AtomicLong}。在低更新竞争下，这两个类具有相似的特性。但在高竞争下，这个类的预期吞吐量显著更高，但代价是占用更多空间。
 *
 * <p>累积的顺序在内部或跨线程中不保证且不能依赖，因此这个类仅适用于累积顺序无关紧要的函数。提供的累积函数应该是无副作用的，因为当线程间竞争导致更新尝试失败时，可能会重新应用该函数。该函数以当前值作为第一个参数，给定的更新作为第二个参数。例如，要维护一个运行的最大值，可以提供 {@code Long::max} 以及 {@code Long.MIN_VALUE} 作为初始值。
 *
 * <p>类 {@link LongAdder} 为维护计数和总和的常见特殊情况提供了与此类功能的类似实现。调用 {@code new LongAdder()} 等效于 {@code new LongAccumulator((x, y) -> x + y, 0L}。
 *
 * <p>此类扩展了 {@link Number}，但未定义 {@code equals}、{@code hashCode} 和 {@code compareTo} 方法，因为实例预计会被修改，因此不适合作为集合键。
 *
 * @since 1.8
 * @author Doug Lea
 */
public class LongAccumulator extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    private final LongBinaryOperator function;
    private final long identity;

    /**
     * 使用给定的累积函数和初始值创建一个新实例。
     * @param accumulatorFunction 无副作用的双参数函数
     * @param identity 累积函数的初始值
     */
    public LongAccumulator(LongBinaryOperator accumulatorFunction,
                           long identity) {
        this.function = accumulatorFunction;
        base = this.identity = identity;
    }

    /**
     * 使用给定的值进行更新。
     *
     * @param x 值
     */
    public void accumulate(long x) {
        Cell[] as; long b, v, r; int m; Cell a;
        if ((as = cells) != null ||
            (r = function.applyAsLong(b = base, x)) != b && !casBase(b, r)) {
            boolean uncontended = true;
            if (as == null || (m = as.length - 1) < 0 ||
                (a = as[getProbe() & m]) == null ||
                !(uncontended =
                  (r = function.applyAsLong(v = a.value, x)) == v ||
                  a.cas(v, r)))
                longAccumulate(x, function, uncontended);
        }
    }

    /**
     * 返回当前值。返回的值<em>不是</em>原子快照；在没有并发更新的情况下调用时返回准确的结果，但在值计算过程中发生的并发更新可能不会被纳入。
     *
     * @return 当前值
     */
    public long get() {
        Cell[] as = cells; Cell a;
        long result = base;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    result = function.applyAsLong(result, a.value);
            }
        }
        return result;
    }

    /**
     * 将维护更新的变量重置为初始值。如果在调用此方法时没有并发更新，这可能是一个有用的替代方案。由于此方法本质上是竞争性的，因此只有在已知没有线程并发更新时才应使用。
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
     * 效果等同于 {@link #get} 后跟 {@link #reset}。此方法可能适用于多线程计算之间的静默点。如果在调用此方法时有并发更新，返回的值<em>不是</em>重置前的最终值。
     *
     * @return 重置前的值
     */
    public long getThenReset() {
        Cell[] as = cells; Cell a;
        long result = base;
        base = identity;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null) {
                    long v = a.value;
                    a.value = identity;
                    result = function.applyAsLong(result, v);
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
        return Long.toString(get());
    }

    /**
     * 效果等同于 {@link #get}。
     *
     * @return 当前值
     */
    public long longValue() {
        return get();
    }

    /**
     * 返回 {@linkplain #get 当前值} 作为 {@code int}，经过窄化原始转换。
     */
    public int intValue() {
        return (int)get();
    }

    /**
     * 返回 {@linkplain #get 当前值} 作为 {@code float}，经过扩展原始转换。
     */
    public float floatValue() {
        return (float)get();
    }

    /**
     * 返回 {@linkplain #get 当前值} 作为 {@code double}，经过扩展原始转换。
     */
    public double doubleValue() {
        return (double)get();
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
        private final long value;
        /**
         * 用于更新的函数。
         * @serial
         */
        private final LongBinaryOperator function;
        /**
         * 初始值
         * @serial
         */
        private final long identity;

        SerializationProxy(LongAccumulator a) {
            function = a.function;
            identity = a.identity;
            value = a.get();
        }

        /**
         * 返回一个具有此代理持有的初始状态的 {@code LongAccumulator} 对象。
         *
         * @return 一个具有此代理持有的初始状态的 {@code LongAccumulator} 对象。
         */
        private Object readResolve() {
            LongAccumulator a = new LongAccumulator(function, identity);
            a.base = value;
            return a;
        }
    }

    /**
     * 返回一个表示此实例状态的
     * <a href="../../../../serialized-form.html#java.util.concurrent.atomic.LongAccumulator.SerializationProxy">
     * SerializationProxy</a>。
     *
     * @return 一个表示此实例状态的 {@link SerializationProxy}。
     */
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    /**
     * @param s 流
     * @throws java.io.InvalidObjectException 总是抛出
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.InvalidObjectException {
        throw new java.io.InvalidObjectException("Proxy required");
    }

}
