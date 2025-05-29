
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
 * 由 Doug Lea 在 JCP JSR-166 专家组成员的帮助下编写，并根据以下网址的说明发布到公共领域：
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;
import java.io.Serializable;

/**
 * 一个或多个变量共同维护一个初始值为零的 {@code long} 型和。当更新（方法 {@link #add}）在多线程中竞争时，
 * 变量集可能会动态增长以减少竞争。方法 {@link #sum}（或等效的 {@link #longValue}）返回跨维护和的变量的当前总和。
 *
 * <p>当多个线程更新一个用于收集统计信息（而非细粒度同步控制）的公共和时，此类通常优于 {@link AtomicLong}。
 * 在低更新竞争下，这两个类具有相似的特性。但在高竞争下，此类的预期吞吐量显著更高，但代价是占用更多空间。
 *
 * <p>LongAdders 可以与 {@link java.util.concurrent.ConcurrentHashMap} 一起使用，以维护一个可扩展的频率映射
 * （一种直方图或多重集）。例如，要向 {@code ConcurrentHashMap<String,LongAdder> freqs} 添加一个计数，
 * 如果不存在则初始化，可以使用 {@code freqs.computeIfAbsent(k -> new LongAdder()).increment();}
 *
 * <p>此类扩展了 {@link Number}，但未定义 {@code equals}、{@code hashCode} 和 {@code compareTo} 方法，
 * 因为实例预计会被修改，因此不适合作为集合的键。
 *
 * @since 1.8
 * @author Doug Lea
 */
public class LongAdder extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    /**
     * 创建一个新的加法器，初始和为零。
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
     * 返回当前的和。返回的值<em>不是</em>原子快照；在没有并发更新的情况下调用时返回准确的结果，
     * 但在和正在计算时发生的并发更新可能不会被合并。
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
     * 将维护和的变量重置为零。如果已知没有线程并发更新，此方法可以作为创建新加法器的替代方案，但只有在这种情况下才有效。
     * 由于此方法本质上是竞争性的，因此只有在已知没有线程并发更新时才应使用。
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
     * 等效于 {@link #sum} 后跟 {@link #reset}。例如，在多线程计算之间的静止点时，此方法可能适用。
     * 如果有与该方法并发的更新，则返回的值<em>不</em>保证是重置前发生的最终值。
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
     * 序列化代理，用于在序列化形式中避免引用非公共的 Striped64 超类。
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
         * 返回一个初始状态由该代理持有的 {@code LongAdder} 对象。
         *
         * @return 一个初始状态由该代理持有的 {@code LongAdder} 对象。
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
     * @throws java.io.InvalidObjectException 始终抛出
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.InvalidObjectException {
        throw new java.io.InvalidObjectException("Proxy required");
    }

}
