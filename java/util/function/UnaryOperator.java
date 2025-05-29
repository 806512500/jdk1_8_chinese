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
 * 表示对单个操作数进行操作并产生与操作数类型相同的结果的操作。这是 {@code Function} 的特化，用于操作数和结果类型相同的情况。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #apply(Object)}。
 *
 * @param <T> 操作符的操作数和结果的类型
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface UnaryOperator<T> extends Function<T, T> {

    /**
     * 返回一个总是返回其输入参数的单目操作符。
     *
     * @param <T> 操作符的输入和输出类型
     * @return 一个总是返回其输入参数的单目操作符
     */
    static <T> UnaryOperator<T> identity() {
        return t -> t;
    }
}