/*
 * 版权所有 (c) 2010, 2013，Oracle 及/或其附属公司。保留所有权利。
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
 * 表示接受一个 {@code long} 类型参数且不返回结果的操作。这是 {@code long} 类型的 {@link Consumer} 的原始类型特化。
 * 与大多数其他函数接口不同，{@code LongConsumer} 预期通过副作用进行操作。
 *
 * <p>这是一个 <a href="package-summary.html">函数接口</a>，其函数方法是 {@link #accept(long)}。
 *
 * @see Consumer
 * @since 1.8
 */
@FunctionalInterface
public interface LongConsumer {

    /**
     * 在给定的参数上执行此操作。
     *
     * @param value 输入参数
     */
    void accept(long value);

    /**
     * 返回一个组合的 {@code LongConsumer}，该组合按顺序执行此操作，然后执行 {@code after} 操作。如果执行任一操作抛出异常，
     * 则将其传递给组合操作的调用者。如果执行此操作抛出异常，则不会执行 {@code after} 操作。
     *
     * @param after 在此操作之后执行的操作
     * @return 一个组合的 {@code LongConsumer}，按顺序执行此操作，然后执行 {@code after} 操作
     * @throws NullPointerException 如果 {@code after} 为 null
     */
    default LongConsumer andThen(LongConsumer after) {
        Objects.requireNonNull(after);
        return (long t) -> { accept(t); after.accept(t); };
    }
}