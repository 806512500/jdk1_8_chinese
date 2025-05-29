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

import java.util.Objects;

/**
 * 表示对单个 {@code double} 值操作数执行的操作，产生一个 {@code double} 值结果。这是 {@code double} 类型的 {@link UnaryOperator} 的原始类型特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #applyAsDouble(double)}。
 *
 * @see UnaryOperator
 * @since 1.8
 */
@FunctionalInterface
public interface DoubleUnaryOperator {

    /**
     * 将此操作符应用于给定的操作数。
     *
     * @param operand 操作数
     * @return 操作符结果
     */
    double applyAsDouble(double operand);

    /**
     * 返回一个组合操作符，首先应用 {@code before} 操作符到其输入，然后应用此操作符到结果。
     * 如果任一操作符的评估抛出异常，它将被传递给组合操作符的调用者。
     *
     * @param before 在应用此操作符之前要应用的操作符
     * @return 一个组合操作符，首先应用 {@code before} 操作符，然后应用此操作符
     * @throws NullPointerException 如果 before 为 null
     *
     * @see #andThen(DoubleUnaryOperator)
     */
    default DoubleUnaryOperator compose(DoubleUnaryOperator before) {
        Objects.requireNonNull(before);
        return (double v) -> applyAsDouble(before.applyAsDouble(v));
    }

    /**
     * 返回一个组合操作符，首先应用此操作符到其输入，然后应用 {@code after} 操作符到结果。
     * 如果任一操作符的评估抛出异常，它将被传递给组合操作符的调用者。
     *
     * @param after 在应用此操作符之后要应用的操作符
     * @return 一个组合操作符，首先应用此操作符，然后应用 {@code after} 操作符
     * @throws NullPointerException 如果 after 为 null
     *
     * @see #compose(DoubleUnaryOperator)
     */
    default DoubleUnaryOperator andThen(DoubleUnaryOperator after) {
        Objects.requireNonNull(after);
        return (double t) -> after.applyAsDouble(applyAsDouble(t));
    }

    /**
     * 返回一个始终返回其输入参数的单目操作符。
     *
     * @return 一个始终返回其输入参数的单目操作符
     */
    static DoubleUnaryOperator identity() {
        return t -> t;
    }
}