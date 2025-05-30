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

/**
 * 表示一个接受一个 {@code double} 类型参数的布尔值函数。这是 {@link Predicate} 的
 * {@code double} 类型特化版本。
 *
 * <p>这是一个 <a href="package-summary.html">函数式接口</a>，其函数方法是 {@link #test(double)}。
 *
 * @see Predicate
 * @since 1.8
 */
@FunctionalInterface
public interface DoublePredicate {

    /**
     * 在给定参数上评估此谓词。
     *
     * @param value 输入参数
     * @return 如果输入参数匹配谓词，则返回 {@code true}，否则返回 {@code false}
     */
    boolean test(double value);

    /**
     * 返回一个表示此谓词与另一个谓词短路逻辑 AND 的组合谓词。在评估组合谓词时，如果此谓词为 {@code false}，
     * 则不会评估 {@code other} 谓词。
     *
     * <p>在评估任一谓词时抛出的任何异常都会传递给调用者；如果评估此谓词时抛出异常，则不会评估 {@code other} 谓词。
     *
     * @param other 将与本谓词进行逻辑 AND 运算的谓词
     * @return 一个表示此谓词与 {@code other} 谓词短路逻辑 AND 的组合谓词
     * @throws NullPointerException 如果 other 为 null
     */
    default DoublePredicate and(DoublePredicate other) {
        Objects.requireNonNull(other);
        return (value) -> test(value) && other.test(value);
    }

    /**
     * 返回一个表示此谓词逻辑否定的谓词。
     *
     * @return 一个表示此谓词逻辑否定的谓词
     */
    default DoublePredicate negate() {
        return (value) -> !test(value);
    }

    /**
     * 返回一个表示此谓词与另一个谓词短路逻辑 OR 的组合谓词。在评估组合谓词时，如果此谓词为 {@code true}，
     * 则不会评估 {@code other} 谓词。
     *
     * <p>在评估任一谓词时抛出的任何异常都会传递给调用者；如果评估此谓词时抛出异常，则不会评估 {@code other} 谓词。
     *
     * @param other 将与本谓词进行逻辑 OR 运算的谓词
     * @return 一个表示此谓词与 {@code other} 谓词短路逻辑 OR 的组合谓词
     * @throws NullPointerException 如果 other 为 null
     */
    default DoublePredicate or(DoublePredicate other) {
        Objects.requireNonNull(other);
        return (value) -> test(value) || other.test(value);
    }
}
