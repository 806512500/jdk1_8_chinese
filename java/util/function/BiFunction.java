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
 * 表示接受两个参数并产生结果的函数。
 * 这是 {@link Function} 的二元特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数接口</a>
 * 其函数方法是 {@link #apply(Object, Object)}。
 *
 * @param <T> 函数的第一个参数类型
 * @param <U> 函数的第二个参数类型
 * @param <R> 函数的结果类型
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface BiFunction<T, U, R> {

    /**
     * 将此函数应用于给定的参数。
     *
     * @param t 第一个函数参数
     * @param u 第二个函数参数
     * @return 函数结果
     */
    R apply(T t, U u);

    /**
     * 返回一个组合函数，该函数首先应用此函数，然后将 {@code after} 函数应用于结果。
     * 如果任一函数的评估抛出异常，它将被传递给组合函数的调用者。
     *
     * @param <V> {@code after} 函数的输出类型，也是组合函数的输出类型
     * @param after 应用于此函数应用后的结果的函数
     * @return 一个组合函数，首先应用此函数，然后应用 {@code after} 函数
     * @throws NullPointerException 如果 after 为 null
     */
    default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(apply(t, u));
    }
}
