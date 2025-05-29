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
 * 由 Doug Lea 编写，并在 JCP JSR-166 专家小组成员的帮助下发布到公共领域，如
 * http://creativecommons.org/publicdomain/zero/1.0/ 所解释的。
 */

package java.util.concurrent.atomic;
import java.io.Serializable;

/**
 * 一个或多个变量共同维护一个初始为零的 {@code double} 和。当线程间更新（方法 {@link #add}）发生争用时，
 * 变量集可能会动态增长以减少争用。方法 {@link #sum}（或等效的 {@link
 * #doubleValue}）返回跨维护总和的变量的当前总和。累积的顺序在或跨线程中不保证。因此，如果需要数值稳定性，特别是当组合值的量级差异很大时，此类可能不适用。
 *
 * <p>当多个线程更新一个通常用于频繁更新但较少读取的公共值（如汇总统计信息）时，此类通常优于其他替代方案。
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
     * 注意，我们必须使用“long”作为底层表示，因为没有用于 double 的 compareAndSet，原因是
     * 任何 CAS 实现中使用的位等于不是 double 精度的等于。然而，我们仅使用 CAS 来检测和缓解争用，对于这一点，位等于工作得最好。原则上，这里使用的 long/double 转换在大多数平台上应该是几乎免费的，因为它们只是重新解释位。
     */

    /**
     * 创建一个新的 adder，初始和为零。
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
     * 返回当前的和。返回的值<em>不是</em>原子快照；在没有并发更新的情况下调用返回准确的结果，但在和正在计算时发生的并发更新可能不会被包含。此外，由于浮点算术不是严格关联的，返回的结果不必与对单个变量进行一系列顺序更新所获得的值相同。
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
     * 将维护和的变量重置为零。如果已知没有线程正在并发更新，此方法可以作为创建新 adder 的有用替代。由于此方法本质上是竞争的，因此只有在已知没有线程正在并发更新时才应使用。
     */
    public void reset() {
        Cell[] as = cells; Cell a;
        base = 0L; // 依赖于 double 0 必须具有与 long 相同的表示形式的事实
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    a.value = 0L;
            }
        }
    }

    /**
     * 与 {@link #sum} 后跟 {@link
     * #reset} 等效。此方法例如可以在多线程计算之间的静止点应用。如果有与该方法并发的更新，返回的值<em>不是</em>保证是重置前发生的最终值。
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
     * 返回 {@link #sum} 作为 {@code long}，经过缩小的原始转换。
     */
    public long longValue() {
        return (long)sum();
    }

    /**
     * 返回 {@link #sum} 作为 {@code int}，经过缩小的原始转换。
     */
    public int intValue() {
        return (int)sum();
    }

}

                /**
     * 返回 {@link #sum} 作为 {@code float} 类型
     * 经过缩小原始类型转换后。
     */
    public float floatValue() {
        return (float)sum();
    }

    /**
     * 序列化代理，用于避免在序列化形式中引用非公共
     * Striped64 超类。
     * @serial include
     */
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 7249069246863182397L;

        /**
         * sum() 返回的当前值。
         * @serial
         */
        private final double value;

        SerializationProxy(DoubleAdder a) {
            value = a.sum();
        }

        /**
         * 返回一个具有此代理持有的初始状态的 {@code DoubleAdder} 对象。
         *
         * @return 一个具有此代理持有的初始状态的 {@code DoubleAdder} 对象。
         */
        private Object readResolve() {
            DoubleAdder a = new DoubleAdder();
            a.base = Double.doubleToRawLongBits(value);
            return a;
        }
    }

    /**
     * 返回一个表示此实例状态的
     * <a href="../../../../serialized-form.html#java.util.concurrent.atomic.DoubleAdder.SerializationProxy">
     * SerializationProxy</a>。
     *
     * @return 一个表示此实例状态的 {@link SerializationProxy}。
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
