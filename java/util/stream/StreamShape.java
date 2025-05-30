/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.util.stream;

/**
 * 一个枚举，描述了流抽象的已知形状专业化。
 * 每个形状将对应于 {@link BaseStream} 的一个特定子接口
 * （例如，{@code REFERENCE} 对应于 {@code Stream}，{@code INT_VALUE}
 * 对应于 {@code IntStream}）。每个形状也可能对应于
 * 值处理抽象的专门化，如 {@code Spliterator}，{@code Consumer} 等。
 *
 * @apiNote
 * 此枚举由实现使用，以确定流和操作之间的兼容性
 * （即，流的输出形状是否与下一个操作的输入形状兼容）。
 *
 * <p>某些 API 要求您指定输入或输出元素的泛型类型和流形状，
 * 例如 {@link TerminalOp}，它既有输入类型的泛型类型参数，
 * 也有输入形状的 getter。当以这种方式表示原始流时，
 * 泛型类型参数应对应于该原始类型的包装类型。
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
