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
 * 表示接受一个 double 类型参数并产生一个 int 类型结果的函数。这是 {@link Function} 的 {@code double}-to-{@code int} 原始类型特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #applyAsInt(double)}。
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface DoubleToIntFunction {

    /**
     * 将此函数应用于给定的参数。
     *
     * @param value 函数参数
     * @return 函数结果
     */
    int applyAsInt(double value);
}