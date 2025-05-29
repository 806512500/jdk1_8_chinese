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
 * 表示接受一个 {@code int} 类型参数且不返回结果的操作。这是 {@link Consumer} 接口针对 {@code int} 类型的原始类型特化版本。与其他大多数函数式接口不同，{@code IntConsumer} 预期通过副作用来操作。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法为 {@link #accept(int)}。
 *
 * @see Consumer
 * @since 1.8
 */
@FunctionalInterface
public interface IntConsumer {

    /**
     * 在给定参数上执行此操作。
     *
     * @param value 输入参数
     */
    void accept(int value);

    /**
     * 返回一个组合的 {@code IntConsumer}，该组合按顺序执行此操作，然后执行 {@code after} 操作。如果执行任一操作时抛出异常，该异常将被传递给组合操作的调用者。如果执行此操作时抛出异常，则不会执行 {@code after} 操作。
     *
     * @param after 在此操作之后执行的操作
     * @return 一个组合的 {@code IntConsumer}，按顺序执行此操作，然后执行 {@code after} 操作
     * @throws NullPointerException 如果 {@code after} 为 null
     */
    default IntConsumer andThen(IntConsumer after) {
        Objects.requireNonNull(after);
        return (int t) -> { accept(t); after.accept(t); };
    }
}