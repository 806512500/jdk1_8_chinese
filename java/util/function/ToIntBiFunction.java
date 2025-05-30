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

/**
 * 表示接受两个参数并生成 int 值结果的函数。这是 {@link BiFunction} 的 {@code int} 值生成原始特化。
 *
 * <p>这是一个 <a href="package-summary.html">函数接口</a>
 * 其函数方法是 {@link #applyAsInt(Object, Object)}。
 *
 * @param <T> 函数的第一个参数类型
 * @param <U> 函数的第二个参数类型
 *
 * @see BiFunction
 * @since 1.8
 */
@FunctionalInterface
public interface ToIntBiFunction<T, U> {

    /**
     * 将此函数应用于给定的参数。
     *
     * @param t 第一个函数参数
     * @param u 第二个函数参数
     * @return 函数结果
     */
    int applyAsInt(T t, U u);
}
