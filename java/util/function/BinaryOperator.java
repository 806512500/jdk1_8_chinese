/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Comparator;

/**
 * 表示对两个相同类型的操作数进行操作，产生与操作数相同类型的结果。这是 {@link BiFunction} 的一个特化，
 * 用于操作数和结果类型相同的情况。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #apply(Object, Object)}。
 *
 * @param <T> 操作数和结果的类型
 *
 * @see BiFunction
 * @see UnaryOperator
 * @since 1.8
 */
@FunctionalInterface
public interface BinaryOperator<T> extends BiFunction<T,T,T> {
    /**
     * 返回一个 {@link BinaryOperator}，该操作返回根据指定的 {@code Comparator} 比较的两个元素中较小的一个。
     *
     * @param <T> 比较器的输入参数类型
     * @param comparator 用于比较两个值的 {@code Comparator}
     * @return 一个 {@code BinaryOperator}，该操作返回其操作数中较小的一个，根据提供的 {@code Comparator}
     * @throws NullPointerException 如果参数为 null
     */
    public static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
    }

    /**
     * 返回一个 {@link BinaryOperator}，该操作返回根据指定的 {@code Comparator} 比较的两个元素中较大的一个。
     *
     * @param <T> 比较器的输入参数类型
     * @param comparator 用于比较两个值的 {@code Comparator}
     * @return 一个 {@code BinaryOperator}，该操作返回其操作数中较大的一个，根据提供的 {@code Comparator}
     * @throws NullPointerException 如果参数为 null
     */
    public static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
    }
}
