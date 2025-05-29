/*
 * 版权所有 (c) 2010, 2013，Oracle 和/或其附属公司。保留所有权利。
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
 * 表示一个接受两个参数的谓词（布尔值函数）。这是 {@link Predicate} 的二元特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #test(Object, Object)}。
 *
 * @param <T> 谓词的第一个参数类型
 * @param <U> 谓词的第二个参数类型
 *
 * @see Predicate
 * @since 1.8
 */
@FunctionalInterface
public interface BiPredicate<T, U> {

    /**
     * 在给定的参数上评估此谓词。
     *
     * @param t 第一个输入参数
     * @param u 第二个输入参数
     * @return 如果输入参数满足谓词，则返回 {@code true}，否则返回 {@code false}
     */
    boolean test(T t, U u);

    /**
     * 返回一个组合谓词，表示此谓词与另一个谓词的短路逻辑 AND。在评估组合谓词时，如果此谓词为 {@code false}，则不会评估 {@code other} 谓词。
     *
     * <p>在评估任一谓词时抛出的任何异常都会传递给调用者；如果评估此谓词时抛出异常，则不会评估 {@code other} 谓词。
     *
     * @param other 将与此谓词进行逻辑 AND 的谓词
     * @return 一个表示此谓词与 {@code other} 谓词的短路逻辑 AND 的组合谓词
     * @throws NullPointerException 如果 other 为 null
     */
    default BiPredicate<T, U> and(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (T t, U u) -> test(t, u) && other.test(t, u);
    }

    /**
     * 返回一个表示此谓词逻辑否定的谓词。
     *
     * @return 一个表示此谓词逻辑否定的谓词
     */
    default BiPredicate<T, U> negate() {
        return (T t, U u) -> !test(t, u);
    }

    /**
     * 返回一个组合谓词，表示此谓词与另一个谓词的短路逻辑 OR。在评估组合谓词时，如果此谓词为 {@code true}，则不会评估 {@code other} 谓词。
     *
     * <p>在评估任一谓词时抛出的任何异常都会传递给调用者；如果评估此谓词时抛出异常，则不会评估 {@code other} 谓词。
     *
     * @param other 将与此谓词进行逻辑 OR 的谓词
     * @return 一个表示此谓词与 {@code other} 谓词的短路逻辑 OR 的组合谓词
     * @throws NullPointerException 如果 other 为 null
     */
    default BiPredicate<T, U> or(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (T t, U u) -> test(t, u) || other.test(t, u);
    }
}