/*
 * Copyright (c) 1994, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

/**
 * 抽象类 {@code Number} 是表示可转换为原始类型 {@code byte}、{@code double}、
 * {@code float}、{@code int}、{@code long} 和 {@code short} 的数值值的平台类的超类。
 *
 * 从特定的 {@code Number} 实现的数值值到给定原始类型的特定语义由该 {@code Number} 实现定义。
 *
 * 对于平台类，转换通常类似于在 <cite>The Java&trade; Language Specification</cite>
 * 中定义的原始类型之间的扩展原始转换或缩小原始转换。因此，转换可能会丢失数值的整体大小信息，
 * 可能会丢失精度，甚至可能返回与输入符号不同的结果。
 *
 * 有关转换细节，请参阅给定的 {@code Number} 实现的文档。
 *
 * @author      Lee Boynton
 * @author      Arthur van Hoff
 * @jls 5.1.2 Widening Primitive Conversions
 * @jls 5.1.3 Narrowing Primitive Conversions
 * @since   JDK1.0
 */
public abstract class Number implements java.io.Serializable {
    /**
     * 返回指定数字作为 {@code int} 的值，这可能涉及四舍五入或截断。
     *
     * @return  转换为类型 {@code int} 后，此对象表示的数值。
     */
    public abstract int intValue();

    /**
     * 返回指定数字作为 {@code long} 的值，这可能涉及四舍五入或截断。
     *
     * @return  转换为类型 {@code long} 后，此对象表示的数值。
     */
    public abstract long longValue();

    /**
     * 返回指定数字作为 {@code float} 的值，这可能涉及四舍五入。
     *
     * @return  转换为类型 {@code float} 后，此对象表示的数值。
     */
    public abstract float floatValue();

    /**
     * 返回指定数字作为 {@code double} 的值，这可能涉及四舍五入。
     *
     * @return  转换为类型 {@code double} 后，此对象表示的数值。
     */
    public abstract double doubleValue();

    /**
     * 返回指定数字作为 {@code byte} 的值，这可能涉及四舍五入或截断。
     *
     * <p>此实现返回 {@link #intValue} 转换为 {@code byte} 的结果。
     *
     * @return  转换为类型 {@code byte} 后，此对象表示的数值。
     * @since   JDK1.1
     */
    public byte byteValue() {
        return (byte)intValue();
    }

    /**
     * 返回指定数字作为 {@code short} 的值，这可能涉及四舍五入或截断。
     *
     * <p>此实现返回 {@link #intValue} 转换为 {@code short} 的结果。
     *
     * @return  转换为类型 {@code short} 后，此对象表示的数值。
     * @since   JDK1.1
     */
    public short shortValue() {
        return (short)intValue();
    }

    /** 使用 JDK 1.0.2 的 serialVersionUID 以确保互操作性 */
    private static final long serialVersionUID = -8742448824652078965L;
}
