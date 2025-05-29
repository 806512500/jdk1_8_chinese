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

import java.util.function.Supplier;

/**
 * 一个 {@link Sink}，在接收元素时累积状态，并允许在计算完成后检索结果。
 *
 * @param <T> 要接收的元素类型
 * @param <R> 结果类型
 *
 * @since 1.8
 */
interface TerminalSink<T, R> extends Sink<T>, Supplier<R> { }
