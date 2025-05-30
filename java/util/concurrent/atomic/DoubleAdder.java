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

/**
 * 一个或多个变量共同维护一个初始为零的 {@code double} 和。当更新（方法 {@link #add}）在多个线程中竞争时，
 * 变量集可能会动态增长以减少竞争。方法 {@link #sum}（或等效的 {@link #doubleValue}）返回当前总和，
 * 该总和是跨维护和的变量累积的。累积的顺序在或跨线程中不保证。因此，如果需要数值稳定性，
 * 尤其是当组合值的量级差异很大时，此类可能不适用。
 *
 * <p>当多个线程更新一个常用值，该值用于频繁更新但较少读取的汇总统计时，此类通常优于其他替代方案。
 *
 * <p>此类扩展了 {@link Number}，但不定义 {@code equals}、{@code hashCode} 和 {@code
 * compareTo} 方法，因为实例预计会被修改，因此不适合作为集合键。
 *
 * @since 1.8
 * @author Doug Lea
 */
public class DoubleAdder extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    /*
     * 注意我们必须使用“long”作为底层表示，因为没有用于 double 的 compareAndSet，因为任何 CAS 实现中使用的位等于
     * 与双精度等于不同。然而，我们仅使用 CAS 来检测和缓解竞争，因此位等于在这里效果最好。原则上，这里使用的 long/double 转换
     * 在大多数平台上应该是几乎免费的，因为它们只是重新解释位。
     */

    /**
     * 创建一个新的和为零的加法器。
     */
    public DoubleAdder() {
    }

    /**
     * 添加给定的值。
     *
     * @param x 要添加的值
     */
    public void add(double x) {
        Cell[] as; long b, v; int m; Cell a;
        if ((as = cells) != null ||
            !casBase(b = base,
                     Double.doubleToRawLongBits
                     (Double.longBitsToDouble(b) + x))) {
            boolean uncontended = true;
            if (as == null || (m = as.length - 1) < 0 ||
                (a = as[getProbe() & m]) == null ||
                !(uncontended = a.cas(v = a.value,
                                      Double.doubleToRawLongBits
                                      (Double.longBitsToDouble(v) + x))))
                doubleAccumulate(x, null, uncontended);
        }
    }

    /**
     * 返回当前和。返回的值<em>不是</em>原子快照；在没有并发更新的情况下调用时返回准确的结果，
     * 但并发更新在计算和时可能会不被纳入。此外，由于浮点算术不是严格关联的，返回的结果不必与
     * 顺序更新单个变量时获得的值相同。
     *
     * @return 和
     */
    public double sum() {
        Cell[] as = cells; Cell a;
        double sum = Double.longBitsToDouble(base);
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    sum += Double.longBitsToDouble(a.value);
            }
        }
        return sum;
    }

    /**
     * 将维护和的变量重置为零。如果已知没有线程正在并发更新，此方法可以作为创建新加法器的有效替代。
     * 由于此方法本质上是竞争的，因此只有在已知没有线程正在并发更新时才应使用。
     */
    public void reset() {
        Cell[] as = cells; Cell a;
        base = 0L; // 依赖于 double 0 必须具有与 long 相同的表示形式
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    a.value = 0L;
            }
        }
    }

    /**
     * 效果等同于 {@link #sum} 后跟 {@link #reset}。例如，在多线程计算之间的静默点时，此方法可能适用。
     * 如果有与该方法并发的更新，返回的值<em>不</em>保证是重置前发生的最终值。
     *
     * @return 和
     */
    public double sumThenReset() {
        Cell[] as = cells; Cell a;
        double sum = Double.longBitsToDouble(base);
        base = 0L;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null) {
                    long v = a.value;
                    a.value = 0L;
                    sum += Double.longBitsToDouble(v);
                }
            }
        }
        return sum;
    }

    /**
     * 返回 {@link #sum} 的字符串表示形式。
     * @return {@link #sum} 的字符串表示形式
     */
    public String toString() {
        return Double.toString(sum());
    }

    /**
     * 等效于 {@link #sum}。
     *
     * @return 和
     */
    public double doubleValue() {
        return sum();
    }

    /**
     * 返回 {@link #sum} 转换为 {@code long} 后的值。
     */
    public long longValue() {
        return (long)sum();
    }

    /**
     * 返回 {@link #sum} 转换为 {@code int} 后的值。
     */
    public int intValue() {
        return (int)sum();
    }

    /**
     * 返回 {@link #sum} 转换为 {@code float} 后的值。
     */
    public float floatValue() {
        return (float)sum();
    }

    /**
     * 序列化代理，用于避免在序列化形式中引用非公共的 Striped64 超类。
     * @serial include
     */
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 7249069246863182397L;

        /**
         * 由 sum() 返回的当前值。
         * @serial
         */
        private final double value;

        SerializationProxy(DoubleAdder a) {
            value = a.sum();
        }

        /**
         * 返回一个初始状态由此代理持有的 {@code DoubleAdder} 对象。
         *
         * @return 一个初始状态由此代理持有的 {@code DoubleAdder} 对象。
         */
        private Object readResolve() {
            DoubleAdder a = new DoubleAdder();
            a.base = Double.doubleToRawLongBits(value);
            return a;
        }
    }

    /**
     * 返回表示此实例状态的
     * <a href="../../../../serialized-form.html#java.util.concurrent.atomic.DoubleAdder.SerializationProxy">
     * SerializationProxy</a>。
     *
     * @return 表示此实例状态的 {@link SerializationProxy}
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
