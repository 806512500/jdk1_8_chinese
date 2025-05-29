/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其关联公司。保留所有权利。
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
 * 表示生成 double 值结果的函数。这是 {@link Function} 的 double 生成原始类型特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #applyAsDouble(Object)}。
 *
 * @param <T> 函数输入的类型
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface ToDoubleFunction<T> {

    /**
     * 将此函数应用于给定参数。
     *
     * @param value 函数参数
     * @return 函数结果
     */
    double applyAsDouble(T value);
}