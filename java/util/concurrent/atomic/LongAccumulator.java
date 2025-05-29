
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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并按照以下网址的解释发布到公共领域：
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;
import java.io.Serializable;
import java.util.function.LongBinaryOperator;

/**
 * 一个或多个变量共同维护一个使用提供的函数更新的 {@code long} 值。当更新（方法 {@link #accumulate}）在多个线程中发生竞争时，变量集可能会动态增长以减少竞争。方法 {@link #get}
 * （或等效的 {@link #longValue}）返回维护更新的变量的当前值。
 *
 * <p>当多个线程更新一个用于收集统计数据而非细粒度同步控制的公共值时，此类通常优于 {@link AtomicLong}。在低更新竞争下，这两个类具有类似的特性。但在高竞争下，此类的预期吞吐量显著更高，代价是占用更多空间。
 *
 * <p>累积顺序在内部或跨线程中不保证且不可依赖，因此此类仅适用于累积顺序无关紧要的函数。提供的累积函数应该是无副作用的，因为当线程间发生竞争导致尝试更新失败时，可能会重新应用该函数。该函数以当前值作为第一个参数，给定的更新作为第二个参数。例如，要维护一个运行的最大值，您可以提供 {@code Long::max} 以及 {@code
 * Long.MIN_VALUE} 作为标识。
 *
 * <p>类 {@link LongAdder} 为维护计数和总和这一常见特殊情况提供了此类功能的类似实现。调用 {@code new LongAdder()} 等效于 {@code new
 * LongAccumulator((x, y) -> x + y, 0L}。
 *
 * <p>此类扩展了 {@link Number}，但未定义 {@code equals}、{@code hashCode} 和 {@code
 * compareTo} 等方法，因为实例预计会被修改，因此不适合作为集合键。
 *
 * @since 1.8
 * @author Doug Lea
 */
public class LongAccumulator extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    private final LongBinaryOperator function;
    private final long identity;

    /**
     * 使用给定的累积函数和标识元素创建一个新实例。
     * @param accumulatorFunction 无副作用的双参数函数
     * @param identity 累积函数的标识（初始值）
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
     * 返回当前值。返回的值<em>不是</em>原子快照；在没有并发更新的情况下调用时返回准确结果，但在值计算过程中发生的并发更新可能不会被纳入。
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
     * 将维护更新的变量重置为标识值。如果不存在并发更新，此方法可能是一个创建新更新器的有效替代方案。由于此方法本质上是竞争的，因此只有在已知没有线程并发更新时才应使用。
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
     * 效果等同于先调用 {@link #get} 再调用 {@link
     * #reset}。例如，在多线程计算之间的静默点时，此方法可能适用。如果此方法存在并发更新，则返回的值<em>不是</em>重置前发生的最终值。
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
     * 等同于 {@link #get}。
     *
     * @return 当前值
     */
    public long longValue() {
        return get();
    }

    /**
     * 返回 {@linkplain #get 当前值} 经过缩小原始转换后的 {@code int} 值。
     */
    public int intValue() {
        return (int)get();
    }

    /**
     * 返回 {@linkplain #get 当前值} 经过扩展原始转换后的 {@code float} 值。
     */
    public float floatValue() {
        return (float)get();
    }

    /**
     * 返回 {@linkplain #get 当前值} 经过扩展原始转换后的 {@code double} 值。
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
         * 身份值
         * @serial
         */
        private final long identity;

        SerializationProxy(LongAccumulator a) {
            function = a.function;
            identity = a.identity;
            value = a.get();
        }

        /**
         * 返回一个具有由本代理持有的初始状态的 {@code LongAccumulator} 对象。
         *
         * @return 一个具有由本代理持有的初始状态的 {@code LongAccumulator} 对象。
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
     * @return 一个表示此实例状态的 {@link SerializationProxy}
     */
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    /**
     * @param s the stream
     * @throws java.io.InvalidObjectException always
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.InvalidObjectException {
        throw new java.io.InvalidObjectException("Proxy required");
    }

}
