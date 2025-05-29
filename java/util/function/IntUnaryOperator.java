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
 * 表示对单个 {@code int} 值操作数执行操作并产生 {@code int} 值结果的操作。这是 {@code int} 类型的 {@link UnaryOperator} 的原始类型特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #applyAsInt(int)}。
 *
 * @see UnaryOperator
 * @since 1.8
 */
@FunctionalInterface
public interface IntUnaryOperator {

    /**
     * 将此操作符应用于给定的操作数。
     *
     * @param operand 操作数
     * @return 操作结果
     */
    int applyAsInt(int operand);

    /**
     * 返回一个组合操作符，该操作符首先应用 {@code before} 操作符到其输入，然后将此操作符应用于结果。
     * 如果任一操作符的评估抛出异常，该异常将传递给组合操作符的调用者。
     *
     * @param before 在此操作符应用之前应用的操作符
     * @return 一个组合操作符，首先应用 {@code before} 操作符，然后应用此操作符
     * @throws NullPointerException 如果 before 为 null
     *
     * @see #andThen(IntUnaryOperator)
     */
    default IntUnaryOperator compose(IntUnaryOperator before) {
        Objects.requireNonNull(before);
        return (int v) -> applyAsInt(before.applyAsInt(v));
    }

    /**
     * 返回一个组合操作符，该操作符首先将此操作符应用于其输入，然后将 {@code after} 操作符应用于结果。
     * 如果任一操作符的评估抛出异常，该异常将传递给组合操作符的调用者。
     *
     * @param after 在此操作符应用之后应用的操作符
     * @return 一个组合操作符，首先应用此操作符，然后应用 {@code after} 操作符
     * @throws NullPointerException 如果 after 为 null
     *
     * @see #compose(IntUnaryOperator)
     */
    default IntUnaryOperator andThen(IntUnaryOperator after) {
        Objects.requireNonNull(after);
        return (int t) -> after.applyAsInt(applyAsInt(t));
    }

    /**
     * 返回一个总是返回其输入参数的单目操作符。
     *
     * @return 一个总是返回其输入参数的单目操作符
     */
    static IntUnaryOperator identity() {
        return t -> t;
    }
}