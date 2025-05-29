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
 * 表示接受两个输入参数且不返回结果的操作。这是 {@link Consumer} 的二元特化。
 * 与其他大多数函数接口不同，{@code BiConsumer} 预期通过副作用进行操作。
 *
 * <p>这是一个 <a href="package-summary.html">函数接口</a>，其函数方法是 {@link #accept(Object, Object)}。
 *
 * @param <T> 操作的第一个参数的类型
 * @param <U> 操作的第二个参数的类型
 *
 * @see Consumer
 * @since 1.8
 */
@FunctionalInterface
public interface BiConsumer<T, U> {

    /**
     * 在给定的参数上执行此操作。
     *
     * @param t 第一个输入参数
     * @param u 第二个输入参数
     */
    void accept(T t, U u);

    /**
     * 返回一个组合的 {@code BiConsumer}，该组合按顺序执行此操作，然后执行 {@code after} 操作。如果执行任一操作抛出异常，它将传递给组合操作的调用者。如果执行此操作抛出异常，则不会执行 {@code after} 操作。
     *
     * @param after 在此操作之后执行的操作
     * @return 一个组合的 {@code BiConsumer}，该组合按顺序执行此操作，然后执行 {@code after} 操作
     * @throws NullPointerException 如果 {@code after} 为 null
     */
    default BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);

        return (l, r) -> {
            accept(l, r);
            after.accept(l, r);
        };
    }
}