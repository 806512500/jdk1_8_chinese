/*
 * Copyright (c) 2004, 2010, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

/**
 * FomattableFlags 传递给 {@link Formattable#formatTo
 * Formattable.formatTo()} 方法，并修改 {@linkplain
 * Formattable Formattables} 的输出格式。 {@link Formattable} 的实现负责解释和验证任何标志。
 *
 * @since  1.5
 */
public class FormattableFlags {

    // 禁止显式实例化此类。
    private FormattableFlags() {}

    /**
     * 左对齐输出。将在转换值的末尾添加空格 (<tt>'&#92;u0020'</tt>) 以填充字段的最小宽度。
     * 如果未设置此标志，则输出将右对齐。
     *
     * <p> 此标志对应于格式说明符中的 <tt>'-'</tt> (<tt>'&#92;u002d'</tt>)。
     */
    public static final int LEFT_JUSTIFY = 1<<0; // '-'

    /**
     * 根据创建 <tt>formatter</tt> 参数时提供的 {@linkplain java.util.Locale 语言环境} 将输出转换为大写。
     * 输出应等效于以下调用 {@link String#toUpperCase(java.util.Locale)} 的结果
     *
     * <pre>
     *     out.toUpperCase() </pre>
     *
     * <p> 此标志对应于格式说明符中的 <tt>'S'</tt> (<tt>'&#92;u0053'</tt>)。
     */
    public static final int UPPERCASE = 1<<1;    // 'S'

    /**
     * 要求输出使用备用形式。形式的定义由 <tt>Formattable</tt> 指定。
     *
     * <p> 此标志对应于格式说明符中的 <tt>'#'</tt> (<tt>'&#92;u0023'</tt>)。
     */
    public static final int ALTERNATE = 1<<2;    // '#'
}
