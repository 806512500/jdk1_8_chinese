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
package java.util.function;

import java.util.Objects;

/**
 * 表示对单个 {@code int} 值操作数进行操作并生成 {@code int} 值结果的操作。这是 {@link UnaryOperator}
 * 对于 {@code int} 的原始类型特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #applyAsInt(int)}。
 *
 * @see UnaryOperator
 * @since 1.8
 */
@FunctionalInterface
public interface IntUnaryOperator {

    /**
     * 将此操作符应用于给定的操作数。
     *
     * @param operand 操作数
     * @return 操作结果
     */
    int applyAsInt(int operand);

    /**
     * 返回一个复合操作符，该操作符首先应用 {@code before} 操作符到其输入，然后将此操作符应用于结果。
     * 如果任一操作符的评估抛出异常，则该异常将传递给复合操作符的调用者。
     *
     * @param before 在此操作符应用之前应用的操作符
     * @return 一个复合操作符，首先应用 {@code before} 操作符，然后应用此操作符
     * @throws NullPointerException 如果 before 为 null
     *
     * @see #andThen(IntUnaryOperator)
     */
    default IntUnaryOperator compose(IntUnaryOperator before) {
        Objects.requireNonNull(before);
        return (int v) -> applyAsInt(before.applyAsInt(v));
    }

    /**
     * 返回一个复合操作符，该操作符首先将此操作符应用于其输入，然后将 {@code after} 操作符应用于结果。
     * 如果任一操作符的评估抛出异常，则该异常将传递给复合操作符的调用者。
     *
     * @param after 在此操作符应用之后应用的操作符
     * @return 一个复合操作符，首先应用此操作符，然后应用 {@code after} 操作符
     * @throws NullPointerException 如果 after 为 null
     *
     * @see #compose(IntUnaryOperator)
     */
    default IntUnaryOperator andThen(IntUnaryOperator after) {
        Objects.requireNonNull(after);
        return (int t) -> after.applyAsInt(applyAsInt(t));
    }

    /**
     * 返回一个始终返回其输入参数的单目操作符。
     *
     * @return 一个始终返回其输入参数的单目操作符
     */
    static IntUnaryOperator identity() {
        return t -> t;
    }
}
