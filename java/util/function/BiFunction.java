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
 * 表示接受两个参数并产生结果的函数。
 * 这是 {@link Function} 的二元特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>
 * 其函数方法是 {@link #apply(Object, Object)}。
 *
 * @param <T> 函数的第一个参数的类型
 * @param <U> 函数的第二个参数的类型
 * @param <R> 函数结果的类型
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface BiFunction<T, U, R> {

    /**
     * 将此函数应用于给定的参数。
     *
     * @param t 函数的第一个参数
     * @param u 函数的第二个参数
     * @return 函数结果
     */
    R apply(T t, U u);

    /**
     * 返回一个复合函数，该函数首先将此函数应用于其输入，然后将 {@code after} 函数应用于结果。
     * 如果任一函数的评估抛出异常，它将被传递给复合函数的调用者。
     *
     * @param <V> {@code after} 函数的输出类型，也是复合函数的输出类型
     * @param after 在此函数应用后要应用的函数
     * @return 一个复合函数，首先应用此函数，然后应用 {@code after} 函数
     * @throws NullPointerException 如果 after 为 null
     */
    default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(apply(t, u));
    }
}