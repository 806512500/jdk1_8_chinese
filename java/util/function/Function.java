/*
 * 版权所有 (c) 2010, 2013, Oracle 和/或其附属公司。保留所有权利。
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
package java.util.function;

import java.util.Objects;

/**
 * 表示接受一个参数并产生结果的函数。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>
 * 其函数方法是 {@link #apply(Object)}。
 *
 * @param <T> 函数输入的类型
 * @param <R> 函数结果的类型
 *
 * @since 1.8
 */
@FunctionalInterface
public interface Function<T, R> {

    /**
     * 将此函数应用于给定的参数。
     *
     * @param t 函数参数
     * @return 函数结果
     */
    R apply(T t);

    /**
     * 返回一个复合函数，该函数首先应用 {@code before} 函数到其输入，然后将此函数应用于结果。
     * 如果任一函数的评估抛出异常，它将被传递给复合函数的调用者。
     *
     * @param <V> {@code before} 函数和复合函数的输入类型
     * @param before 在此函数应用之前应用的函数
     * @return 一个复合函数，首先应用 {@code before} 函数，然后应用此函数
     * @throws NullPointerException 如果 before 为 null
     *
     * @see #andThen(Function)
     */
    default <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    /**
     * 返回一个复合函数，该函数首先应用此函数到其输入，然后将 {@code after} 函数应用于结果。
     * 如果任一函数的评估抛出异常，它将被传递给复合函数的调用者。
     *
     * @param <V> {@code after} 函数和复合函数的输出类型
     * @param after 在此函数应用之后应用的函数
     * @return 一个复合函数，首先应用此函数，然后应用 {@code after} 函数
     * @throws NullPointerException 如果 after 为 null
     *
     * @see #compose(Function)
     */
    default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    /**
     * 返回一个总是返回其输入参数的函数。
     *
     * @param <T> 函数输入和输出对象的类型
     * @return 一个总是返回其输入参数的函数
     */
    static <T> Function<T, T> identity() {
        return t -> t;
    }
}