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
 * 表示接受一个双精度浮点数参数并产生结果的函数。这是 {@link Function} 的 {@code double} 消费基本类型特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #apply(double)}。
 *
 * @param <R> 函数结果的类型
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface DoubleFunction<R> {

    /**
     * 将此函数应用于给定的参数。
     *
     * @param value 函数参数
     * @return 函数结果
     */
    R apply(double value);
}