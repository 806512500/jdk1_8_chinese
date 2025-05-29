/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其附属公司。保留所有权利。
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

/**
 * 表示接受两个参数并产生 long 类型结果的函数。这是 {@link BiFunction} 的 long 类型结果的原始类型特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #applyAsLong(Object, Object)}。
 *
 * @param <T> 函数的第一个参数类型
 * @param <U> 函数的第二个参数类型
 *
 * @see BiFunction
 * @since 1.8
 */
@FunctionalInterface
public interface ToLongBiFunction<T, U> {

    /**
     * 将此函数应用于给定的参数。
     *
     * @param t 第一个函数参数
     * @param u 第二个函数参数
     * @return 函数结果
     */
    long applyAsLong(T t, U u);
}