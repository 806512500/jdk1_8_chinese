/*
 * 版权所有 (c) 2009, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.util;

import java.util.function.Supplier;

/**
 * 本类包含用于操作对象的 {@code static} 实用方法。这些实用方法包括计算对象哈希码、返回对象字符串和比较两个对象的 {@code null}-安全或 {@code
 * null}-容错方法。
 *
 * @since 1.7
 */
public final class Objects {
    private Objects() {
        throw new AssertionError("No java.util.Objects instances for you!");
    }

    /**
     * 如果参数相等则返回 {@code true}，否则返回 {@code false}。
     * 因此，如果两个参数都为 {@code null}，则返回 {@code true}；如果恰好有一个参数为 {@code null}，则返回 {@code
     * false}。否则，通过使用第一个参数的 {@link Object#equals equals} 方法来确定相等性。
     *
     * @param a 一个对象
     * @param b 与 {@code a} 比较相等性的对象
     * @return 如果参数相等则返回 {@code true}，否则返回 {@code false}
     * @see Object#equals(Object)
     */
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

   /**
    * 如果参数深度相等则返回 {@code true}，否则返回 {@code false}。
    *
    * 两个 {@code null} 值是深度相等的。如果两个参数都是数组，则使用 {@link Arrays#deepEquals(Object[],
    * Object[]) Arrays.deepEquals} 中的算法来确定相等性。否则，通过使用第一个参数的 {@link
    * Object#equals equals} 方法来确定相等性。
    *
    * @param a 一个对象
    * @param b 与 {@code a} 比较深度相等性的对象
    * @return 如果参数深度相等则返回 {@code true}，否则返回 {@code false}
    * @see Arrays#deepEquals(Object[], Object[])
    * @see Objects#equals(Object, Object)
    */
    public static boolean deepEquals(Object a, Object b) {
        if (a == b)
            return true;
        else if (a == null || b == null)
            return false;
        else
            return Arrays.deepEquals0(a, b);
    }

    /**
     * 返回非-{@code null} 参数的哈希码，对于 {@code null} 参数返回 0。
     *
     * @param o 一个对象
     * @return 非-{@code null} 参数的哈希码，对于 {@code null} 参数返回 0
     * @see Object#hashCode
     */
    public static int hashCode(Object o) {
        return o != null ? o.hashCode() : 0;
    }

   /**
    * 为输入值序列生成哈希码。哈希码的生成方式如同将所有输入值放入一个数组，并通过调用 {@link
    * Arrays#hashCode(Object[])} 对该数组进行哈希。
    *
    * <p>此方法适用于包含多个字段的对象实现 {@link
    * Object#hashCode()}。例如，如果一个对象有三个字段，{@code x}，{@code
    * y}，和 {@code z}，可以这样写：
    *
    * <blockquote><pre>
    * &#064;Override public int hashCode() {
    *     return Objects.hash(x, y, z);
    * }
    * </pre></blockquote>
    *
    * <b>警告：当提供单个对象引用时，返回的值不等于该对象引用的哈希码。</b> 可以通过调用 {@link #hashCode(Object)} 来计算此值。
    *
    * @param values 要哈希的值
    * @return 输入值序列的哈希值
    * @see Arrays#hashCode(Object[])
    * @see List#hashCode
    */
    public static int hash(Object... values) {
        return Arrays.hashCode(values);
    }

    /**
     * 对于非-{@code
     * null} 参数返回调用 {@code toString} 的结果，对于 {@code null} 参数返回 {@code "null"}。
     *
     * @param o 一个对象
     * @return 对于非-{@code
     * null} 参数返回调用 {@code toString} 的结果，对于 {@code null} 参数返回 {@code "null"}
     * @see Object#toString
     * @see String#valueOf(Object)
     */
    public static String toString(Object o) {
        return String.valueOf(o);
    }

    /**
     * 如果第一个参数不为 {@code null}，则返回调用 {@code toString} 的结果，否则返回第二个参数。
     *
     * @param o 一个对象
     * @param nullDefault 如果第一个参数为 {@code null} 时返回的字符串
     * @return 如果第一个参数不为 {@code null}，则返回调用 {@code toString} 的结果，否则返回第二个参数。
     * @see Objects#toString(Object)
     */
    public static String toString(Object o, String nullDefault) {
        return (o != null) ? o.toString() : nullDefault;
    }

    /**
     * 如果参数相同则返回 0，否则返回 {@code
     * c.compare(a, b)}。因此，如果两个参数都为 {@code null}，则返回 0。
     *
     * <p>注意，如果其中一个参数为 {@code null}，则根据 {@link Comparator Comparator} 选择的 {@code null} 值排序策略，可能会或可能不会抛出 {@code
     * NullPointerException}。
     *
     * @param <T> 被比较对象的类型
     * @param a 一个对象
     * @param b 与 {@code a} 比较的对象
     * @param c 用于比较前两个参数的 {@code Comparator}
     * @return 如果参数相同则返回 0，否则返回 {@code
     * c.compare(a, b)}。
     * @see Comparable
     * @see Comparator
     */
    public static <T> int compare(T a, T b, Comparator<? super T> c) {
        return (a == b) ? 0 :  c.compare(a, b);
    }

    /**
     * 检查指定的对象引用是否不为 {@code null}。此方法主要用于在方法和构造函数中进行参数验证，如下所示：
     * <blockquote><pre>
     * public Foo(Bar bar) {
     *     this.bar = Objects.requireNonNull(bar);
     * }
     * </pre></blockquote>
     *
     * @param obj 要检查是否为 null 的对象引用
     * @param <T> 引用的类型
     * @return 如果不为 {@code null} 则返回 {@code obj}
     * @throws NullPointerException 如果 {@code obj} 为 {@code null}
     */
    public static <T> T requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException();
        return obj;
    }

}

                /**
     * 检查指定的对象引用是否不为 {@code null}，如果为 {@code null} 则抛出一个自定义的 {@link NullPointerException}。
     * 此方法主要用于在具有多个参数的方法和构造函数中进行参数验证，如下所示：
     * <blockquote><pre>
     * public Foo(Bar bar, Baz baz) {
     *     this.bar = Objects.requireNonNull(bar, "bar must not be null");
     *     this.baz = Objects.requireNonNull(baz, "baz must not be null");
     * }
     * </pre></blockquote>
     *
     * @param obj     要检查是否为 null 的对象引用
     * @param message 如果抛出 {@code
     *                NullPointerException} 时使用的详细消息
     * @param <T> 引用的类型
     * @return 如果不为 {@code null} 则返回 {@code obj}
     * @throws NullPointerException 如果 {@code obj} 为 {@code null}
     */
    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }

    /**
     * 如果提供的引用为 {@code null} 则返回 {@code true}，否则返回 {@code false}。
     *
     * @apiNote 此方法存在的目的是作为
     * {@link java.util.function.Predicate} 使用，例如 {@code filter(Objects::isNull)}
     *
     * @param obj 要检查是否为 {@code null} 的引用
     * @return 如果提供的引用为 {@code null} 则返回 {@code true}，否则返回 {@code false}
     *
     * @see java.util.function.Predicate
     * @since 1.8
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * 如果提供的引用不为 {@code null} 则返回 {@code true}，否则返回 {@code false}。
     *
     * @apiNote 此方法存在的目的是作为
     * {@link java.util.function.Predicate} 使用，例如 {@code filter(Objects::nonNull)}
     *
     * @param obj 要检查是否为 {@code null} 的引用
     * @return 如果提供的引用不为 {@code null} 则返回 {@code true}，否则返回 {@code false}
     *
     * @see java.util.function.Predicate
     * @since 1.8
     */
    public static boolean nonNull(Object obj) {
        return obj != null;
    }

    /**
     * 检查指定的对象引用是否不为 {@code null}，如果为 {@code null} 则抛出一个自定义的 {@link NullPointerException}。
     *
     * <p>与方法 {@link #requireNonNull(Object, String)} 不同，
     * 此方法允许在进行 null 检查后延迟创建消息。虽然这在非 null 情况下可能提供性能优势，但在决定调用此方法时应谨慎，
     * 确保创建消息供应商的成本低于直接创建字符串消息的成本。
     *
     * @param obj     要检查是否为 null 的对象引用
     * @param messageSupplier 提供在抛出 {@code NullPointerException} 时使用的详细消息的供应商
     * @param <T> 引用的类型
     * @return 如果不为 {@code null} 则返回 {@code obj}
     * @throws NullPointerException 如果 {@code obj} 为 {@code null}
     * @since 1.8
     */
    public static <T> T requireNonNull(T obj, Supplier<String> messageSupplier) {
        if (obj == null)
            throw new NullPointerException(messageSupplier.get());
        return obj;
    }
}
