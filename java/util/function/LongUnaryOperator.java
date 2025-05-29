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
 * 表示对单个 {@code long} 类型操作数进行操作并产生 {@code long} 类型结果的操作。这是 {@code long} 类型的 {@link UnaryOperator} 的原始类型特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #applyAsLong(long)}。
 *
 * @see UnaryOperator
 * @since 1.8
 */
@FunctionalInterface
public interface LongUnaryOperator {

    /**
     * 将此操作符应用于给定的操作数。
     *
     * @param operand 操作数
     * @return 操作结果
     */
    long applyAsLong(long operand);

    /**
     * 返回一个复合操作符，该操作符首先将 {@code before} 操作符应用于其输入，然后将此操作符应用于结果。
     * 如果任一操作符的评估抛出异常，则该异常将传递给复合操作符的调用者。
     *
     * @param before 在此操作符应用前要应用的操作符
     * @return 一个复合操作符，首先应用 {@code before} 操作符，然后应用此操作符
     * @throws NullPointerException 如果 before 为 null
     *
     * @see #andThen(LongUnaryOperator)
     */
    default LongUnaryOperator compose(LongUnaryOperator before) {
        Objects.requireNonNull(before);
        return (long v) -> applyAsLong(before.applyAsLong(v));
    }

    /**
     * 返回一个复合操作符，该操作符首先将此操作符应用于其输入，然后将 {@code after} 操作符应用于结果。
     * 如果任一操作符的评估抛出异常，则该异常将传递给复合操作符的调用者。
     *
     * @param after 在此操作符应用后要应用的操作符
     * @return 一个复合操作符，首先应用此操作符，然后应用 {@code after} 操作符
     * @throws NullPointerException 如果 after 为 null
     *
     * @see #compose(LongUnaryOperator)
     */
    default LongUnaryOperator andThen(LongUnaryOperator after) {
        Objects.requireNonNull(after);
        return (long t) -> after.applyAsLong(applyAsLong(t));
    }

    /**
     * 返回一个始终返回其输入参数的单目操作符。
     *
     * @return 一个始终返回其输入参数的单目操作符
     */
    static LongUnaryOperator identity() {
        return t -> t;
    }
}