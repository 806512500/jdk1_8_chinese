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
import java.util.Comparator;

/**
 * 表示对两个相同类型的操作数进行操作，产生与操作数相同类型的结果。这是
 * {@link BiFunction} 的一个特化，适用于操作数和结果都是相同类型的情况。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>
 * 其函数方法是 {@link #apply(Object, Object)}。
 *
 * @param <T> 操作数和运算结果的类型
 *
 * @see BiFunction
 * @see UnaryOperator
 * @since 1.8
 */
@FunctionalInterface
public interface BinaryOperator<T> extends BiFunction<T,T,T> {
    /**
     * 返回一个 {@link BinaryOperator}，根据指定的 {@code Comparator} 返回两个元素中较小的一个。
     *
     * @param <T> 比较器输入参数的类型
     * @param comparator 用于比较两个值的 {@code Comparator}
     * @return 一个 {@code BinaryOperator}，根据提供的 {@code Comparator} 返回其操作数中较小的一个
     * @throws NullPointerException 如果参数为 null
     */
    public static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
    }

    /**
     * 返回一个 {@link BinaryOperator}，根据指定的 {@code Comparator} 返回两个元素中较大的一个。
     *
     * @param <T> 比较器输入参数的类型
     * @param comparator 用于比较两个值的 {@code Comparator}
     * @return 一个 {@code BinaryOperator}，根据提供的 {@code Comparator} 返回其操作数中较大的一个
     * @throws NullPointerException 如果参数为 null
     */
    public static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
    }
}