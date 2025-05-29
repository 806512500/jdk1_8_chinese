/*
 * 版权所有 (c) 2010, 2013, Oracle 及/或其附属公司。保留所有权利。
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
 * 表示一个参数的谓词（布尔值函数）。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，
 * 其函数方法是 {@link #test(Object)}。
 *
 * @param <T> 谓词输入的类型
 *
 * @since 1.8
 */
@FunctionalInterface
public interface Predicate<T> {

    /**
     * 在给定参数上评估此谓词。
     *
     * @param t 输入参数
     * @return 如果输入参数符合谓词，则返回 {@code true}，否则返回 {@code false}
     */
    boolean test(T t);

    /**
     * 返回一个组合谓词，表示此谓词与另一个谓词的短路逻辑 AND。在评估组合谓词时，如果此谓词为 {@code false}，则不会评估 {@code other} 谓词。
     *
     * <p>在评估任一谓词时抛出的任何异常都会传递给调用者；如果评估此谓词时抛出异常，则不会评估 {@code other} 谓词。
     *
     * @param other 将与此谓词逻辑 AND 的谓词
     * @return 一个组合谓词，表示此谓词与 {@code other} 谓词的短路逻辑 AND
     * @throws NullPointerException 如果 other 为 null
     */
    default Predicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }

    /**
     * 返回一个表示此谓词逻辑否定的谓词。
     *
     * @return 一个表示此谓词逻辑否定的谓词
     */
    default Predicate<T> negate() {
        return (t) -> !test(t);
    }

    /**
     * 返回一个组合谓词，表示此谓词与另一个谓词的短路逻辑 OR。在评估组合谓词时，如果此谓词为 {@code true}，则不会评估 {@code other} 谓词。
     *
     * <p>在评估任一谓词时抛出的任何异常都会传递给调用者；如果评估此谓词时抛出异常，则不会评估 {@code other} 谓词。
     *
     * @param other 将与此谓词逻辑 OR 的谓词
     * @return 一个组合谓词，表示此谓词与 {@code other} 谓词的短路逻辑 OR
     * @throws NullPointerException 如果 other 为 null
     */
    default Predicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) || other.test(t);
    }

    /**
     * 返回一个谓词，用于测试两个参数是否根据 {@link Objects#equals(Object, Object)} 相等。
     *
     * @param <T> 谓词参数的类型
     * @param targetRef 用于比较相等的对象引用，可以为 {@code null}
     * @return 一个谓词，用于测试两个参数是否根据 {@link Objects#equals(Object, Object)} 相等
     */
    static <T> Predicate<T> isEqual(Object targetRef) {
        return (null == targetRef)
                ? Objects::isNull
                : object -> targetRef.equals(object);
    }
}