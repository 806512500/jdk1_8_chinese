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
 * 表示接受单个输入参数且不返回结果的操作。与其他大多数函数式接口不同，{@code Consumer} 预期通过副作用进行操作。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，
 * 其函数方法是 {@link #accept(Object)}。
 *
 * @param <T> 操作的输入类型
 *
 * @since 1.8
 */
@FunctionalInterface
public interface Consumer<T> {

    /**
     * 在给定参数上执行此操作。
     *
     * @param t 输入参数
     */
    void accept(T t);

    /**
     * 返回一个组合的 {@code Consumer}，该组合按顺序执行此操作，然后执行 {@code after} 操作。如果执行任一操作抛出异常，该异常将传递给组合操作的调用者。如果执行此操作抛出异常，则不会执行 {@code after} 操作。
     *
     * @param after 在此操作之后执行的操作
     * @return 一个组合的 {@code Consumer}，按顺序执行此操作，然后执行 {@code after} 操作
     * @throws NullPointerException 如果 {@code after} 为 null
     */
    default Consumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }
}