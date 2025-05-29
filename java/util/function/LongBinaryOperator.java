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
 * 表示对两个 {@code long} 类型操作数进行操作并产生一个 {@code long} 类型结果的操作。这是 {@code long} 类型的 {@link BinaryOperator} 原始类型特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #applyAsLong(long, long)}。
 *
 * @see BinaryOperator
 * @see LongUnaryOperator
 * @since 1.8
 */
@FunctionalInterface
public interface LongBinaryOperator {

    /**
     * 将此操作符应用于给定的操作数。
     *
     * @param left 第一个操作数
     * @param right 第二个操作数
     * @return 操作结果
     */
    long applyAsLong(long left, long right);
}