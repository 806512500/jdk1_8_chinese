/*
 * 版权所有 (c) 2010, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * 表示一个以 {@code double} 值为参数的谓词（布尔值函数）。这是 {@link Predicate} 的 {@code double} 类型特化版本。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #test(double)}。
 *
 * @see Predicate
 * @since 1.8
 */
@FunctionalInterface
public interface DoublePredicate {

    /**
     * 在给定参数上评估此谓词。
     *
     * @param value 输入参数
     * @return 如果输入参数符合谓词，则返回 {@code true}，否则返回 {@code false}
     */
    boolean test(double value);

    /**
     * 返回一个组合谓词，表示此谓词与另一个谓词的短路逻辑 AND。在评估组合谓词时，如果此谓词为 {@code false}，则不会评估 {@code other} 谓词。
     *
     * <p>在评估任一谓词时抛出的任何异常都会传递给调用者；如果评估此谓词时抛出异常，则不会评估 {@code other} 谓词。
     *
     * @param other 一个将与此谓词进行逻辑 AND 运算的谓词
     * @return 一个表示此谓词与 {@code other} 谓词的短路逻辑 AND 的组合谓词
     * @throws NullPointerException 如果 other 为 null
     */
    default DoublePredicate and(DoublePredicate other) {
        Objects.requireNonNull(other);
        return (value) -> test(value) && other.test(value);
    }

    /**
     * 返回一个表示此谓词逻辑否定的谓词。
     *
     * @return 一个表示此谓词逻辑否定的谓词
     */
    default DoublePredicate negate() {
        return (value) -> !test(value);
    }

    /**
     * 返回一个组合谓词，表示此谓词与另一个谓词的短路逻辑 OR。在评估组合谓词时，如果此谓词为 {@code true}，则不会评估 {@code other} 谓词。
     *
     * <p>在评估任一谓词时抛出的任何异常都会传递给调用者；如果评估此谓词时抛出异常，则不会评估 {@code other} 谓词。
     *
     * @param other 一个将与此谓词进行逻辑 OR 运算的谓词
     * @return 一个表示此谓词与 {@code other} 谓词的短路逻辑 OR 的组合谓词
     * @throws NullPointerException 如果 other 为 null
     */
    default DoublePredicate or(DoublePredicate other) {
        Objects.requireNonNull(other);
        return (value) -> test(value) || other.test(value);
    }
}