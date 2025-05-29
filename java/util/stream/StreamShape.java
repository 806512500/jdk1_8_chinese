/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
package java.util.stream;

/**
 * 一个枚举，描述了流抽象的已知形状专业化。
 * 每个专业化将对应于 {@link BaseStream} 的一个特定子接口
 * （例如，{@code REFERENCE} 对应于 {@code Stream}，{@code INT_VALUE}
 * 对应于 {@code IntStream}）。每个专业化也可能对应于
 * 值处理抽象（如 {@code Spliterator}、{@code Consumer} 等）的特定实现。
 *
 * @apiNote
 * 该枚举用于实现中确定流和操作之间的兼容性
 * （即，流的输出形状是否与下一个操作的输入形状兼容）。
 *
 * <p>某些 API 要求您为输入或输出元素指定一个泛型类型和一个流形状，
 * 例如 {@link TerminalOp}，它既有输入类型的泛型参数，也有输入形状的 getter。
 * 以这种方式表示原始流时，泛型类型参数应对应于该原始类型的包装类型。
 *
 * @since 1.8
 */
enum StreamShape {
    /**
     * 对应于 {@code Stream} 和对象引用元素的形状专业化。
     */
    REFERENCE,
    /**
     * 对应于 {@code IntStream} 和 {@code int} 值元素的形状专业化。
     */
    INT_VALUE,
    /**
     * 对应于 {@code LongStream} 和 {@code long} 值元素的形状专业化。
     */
    LONG_VALUE,
    /**
     * 对应于 {@code DoubleStream} 和 {@code double} 值元素的形状专业化。
     */
    DOUBLE_VALUE
}
