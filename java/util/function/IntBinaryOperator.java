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
 * 表示对两个 {@code int} 值的操作数进行操作并产生一个 {@code int} 值的结果。这是 {@code int} 类型的
 * {@link BinaryOperator} 的原始类型特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，
 * 其函数方法是 {@link #applyAsInt(int, int)}。
 *
 * @see BinaryOperator
 * @see IntUnaryOperator
 * @since 1.8
 */
@FunctionalInterface
public interface IntBinaryOperator {

    /**
     * 将此操作符应用于给定的操作数。
     *
     * @param left 第一个操作数
     * @param right 第二个操作数
     * @return 操作结果
     */
    int applyAsInt(int left, int right);
}