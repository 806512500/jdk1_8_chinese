/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.reflect;

/**
 * WildcardType 表示一个通配符类型表达式，例如
 * {@code ?}, {@code ? extends Number}，或 {@code ? super Integer}。
 *
 * @since 1.5
 */
public interface WildcardType extends Type {
    /**
     * 返回一个 {@code Type} 对象数组，表示此类型变量的上界。注意，如果没有显式声明上界，
     * 上界为 {@code Object}。
     *
     * <p>对于每个上界 B：
     * <ul>
     *  <li>如果 B 是参数化类型或类型变量，则创建它，
     *  (参见 {@link java.lang.reflect.ParameterizedType ParameterizedType}
     *  了解参数化类型的创建过程)。
     *  <li>否则，解析 B。
     * </ul>
     *
     * @return 一个 Types 数组，表示此类型变量的上界
     * @throws TypeNotPresentException 如果任何上界引用了不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果任何上界引用了无法实例化的参数化类型
     */
    Type[] getUpperBounds();

    /**
     * 返回一个 {@code Type} 对象数组，表示此类型变量的下界。注意，如果没有显式声明下界，
     * 下界为 {@code null} 的类型。在这种情况下，返回一个长度为零的数组。
     *
     * <p>对于每个下界 B：
     * <ul>
     *   <li>如果 B 是参数化类型或类型变量，则创建它，
     *  (参见 {@link java.lang.reflect.ParameterizedType ParameterizedType}
     *  了解参数化类型的创建过程)。
     *   <li>否则，解析 B。
     * </ul>
     *
     * @return 一个 Types 数组，表示此类型变量的下界
     * @throws TypeNotPresentException 如果任何下界引用了不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果任何下界引用了无法实例化的参数化类型
     */
    Type[] getLowerBounds();
    // 一个或多个？取决于语言规范；目前只有一个，但此 API
    // 允许进行泛化。
}
