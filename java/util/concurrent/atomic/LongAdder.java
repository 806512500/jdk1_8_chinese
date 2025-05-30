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
 * 一个或多个变量，它们共同维护一个初始为零的 {@code long} 和。当更新（方法 {@link #add}）在多个线程中竞争时，
 * 变量集可能会动态增长以减少竞争。方法 {@link #sum}（或等效的 {@link #longValue}）返回当前跨维护和的变量的总和。
 *
 * <p>当多个线程更新一个用于收集统计信息（而不是用于细粒度同步控制）的公共和时，此类通常优于 {@link AtomicLong}。
 * 在低更新竞争下，这两个类具有相似的特性。但在高竞争下，此类的预期吞吐量显著更高，但以更高的空间消耗为代价。
 *
 * <p>LongAdders 可以与 {@link java.util.concurrent.ConcurrentHashMap} 一起使用，以维护一个可扩展的频率映射
 * （一种直方图或多重集）。例如，要向 {@code ConcurrentHashMap<String,LongAdder> freqs} 添加一个计数，
 * 如果不存在则初始化，可以使用 {@code freqs.computeIfAbsent(k -> new LongAdder()).increment();}
 *
 * <p>此类扩展了 {@link Number}，但未定义 {@code equals}、{@code hashCode} 和 {@code compareTo} 方法，
 * 因为实例预计会被修改，因此不适合作为集合键。
 *
 * @since 1.8
 * @author Doug Lea
 */
public class LongAdder extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    /**
     * 创建一个新的和为零的加法器。
     */
    public LongAdder() {
    }

    /**
     * 添加给定的值。
     *
     * @param x 要添加的值
     */
    public void add(long x) {
        Cell[] as; long b, v; int m; Cell a;
        if ((as = cells) != null || !casBase(b = base, b + x)) {
            boolean uncontended = true;
            if (as == null || (m = as.length - 1) < 0 ||
                (a = as[getProbe() & m]) == null ||
                !(uncontended = a.cas(v = a.value, v + x)))
                longAccumulate(x, null, uncontended);
        }
    }

    /**
     * 等效于 {@code add(1)}。
     */
    public void increment() {
        add(1L);
    }

    /**
     * 等效于 {@code add(-1)}。
     */
    public void decrement() {
        add(-1L);
    }

    /**
     * 返回当前的和。返回的值<em>不是</em>原子快照；在没有并发更新的情况下调用时返回一个准确的结果，
     * 但在计算和时发生的并发更新可能不会被包含。
     *
     * @return 和
     */
    public long sum() {
        Cell[] as = cells; Cell a;
        long sum = base;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    sum += a.value;
            }
        }
        return sum;
    }

    /**
     * 将维护和的变量重置为零。如果在调用此方法时没有并发更新，此方法可能是一个创建新加法器的有效替代方案。
     * 由于此方法本质上是竞争的，因此只有在已知没有线程并发更新时才应使用。
     */
    public void reset() {
        Cell[] as = cells; Cell a;
        base = 0L;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    a.value = 0L;
            }
        }
    }

    /**
     * 等效于 {@link #sum} 后跟 {@link #reset}。此方法可能在多线程计算之间的静止点应用。
     * 如果在调用此方法时有并发更新，返回的值<em>不</em>保证是重置前发生的最终值。
     *
     * @return 和
     */
    public long sumThenReset() {
        Cell[] as = cells; Cell a;
        long sum = base;
        base = 0L;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null) {
                    sum += a.value;
                    a.value = 0L;
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
        return Long.toString(sum());
    }

    /**
     * 等效于 {@link #sum}。
     *
     * @return 和
     */
    public long longValue() {
        return sum();
    }

    /**
     * 返回 {@link #sum} 作为 int 类型，经过窄化原始转换。
     */
    public int intValue() {
        return (int)sum();
    }

    /**
     * 返回 {@link #sum} 作为 float 类型，经过扩展原始转换。
     */
    public float floatValue() {
        return (float)sum();
    }

    /**
     * 返回 {@link #sum} 作为 double 类型，经过扩展原始转换。
     */
    public double doubleValue() {
        return (double)sum();
    }

    /**
     * 序列化代理，用于避免在序列化形式中引用非公开的 Striped64 超类。
     * @serial include
     */
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 7249069246863182397L;

        /**
         * 由 sum() 返回的当前值。
         * @serial
         */
        private final long value;

        SerializationProxy(LongAdder a) {
            value = a.sum();
        }

        /**
         * 返回一个具有此代理持有的初始状态的 {@code LongAdder} 对象。
         *
         * @return 一个具有此代理持有的初始状态的 {@code LongAdder} 对象。
         */
        private Object readResolve() {
            LongAdder a = new LongAdder();
            a.base = value;
            return a;
        }
    }

    /**
     * 返回一个表示此实例状态的
     * <a href="../../../../serialized-form.html#java.util.concurrent.atomic.LongAdder.SerializationProxy">
     * SerializationProxy</a>。
     *
     * @return 一个表示此实例状态的 {@link SerializationProxy}
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
