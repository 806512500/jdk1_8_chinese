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
 * {@code GenericArrayType} 表示一个数组类型，其组件类型是参数化类型或类型变量。
 * @since 1.5
 */
public interface GenericArrayType extends Type {
    /**
     * 返回一个表示此数组组件类型的 {@code Type} 对象。此方法创建数组的组件类型。
     * 有关参数化类型的创建过程，请参见 {@link
     * java.lang.reflect.ParameterizedType ParameterizedType} 的声明，
     * 有关类型变量的创建过程，请参见 {@link java.lang.reflect.TypeVariable TypeVariable}。
     *
     * @return  表示此数组组件类型的 {@code Type} 对象
     * @throws TypeNotPresentException 如果底层数组类型的组件类型引用了不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果底层数组类型的组件类型引用了
     *     由于任何原因无法实例化的参数化类型
     */
    Type getGenericComponentType();
}
