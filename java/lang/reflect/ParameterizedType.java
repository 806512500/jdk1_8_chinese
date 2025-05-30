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
 * ParameterizedType 表示一个参数化类型，例如 Collection&lt;String&gt;。
 *
 * <p>参数化类型是在需要时由反射方法首次创建的，如本包中所指定。当创建参数化类型 p 时，p 实例化的泛型类型声明将被解析，并且 p 的所有类型参数将递归地创建。有关类型变量的创建过程，请参见 {@link java.lang.reflect.TypeVariable
 * TypeVariable}。重复创建参数化类型不会产生影响。
 *
 * <p>实现此接口的类的实例必须实现一个 equals() 方法，该方法将任何共享相同泛型类型声明且具有相等类型参数的两个实例视为相等。
 *
 * @since 1.5
 */
public interface ParameterizedType extends Type {
    /**
     * 返回一个 {@code Type} 对象数组，表示此类型的实际类型参数。
     *
     * <p>请注意，在某些情况下，返回的数组可能为空。这可能发生在此类型表示嵌套在参数化类型内的非参数化类型时。
     *
     * @return 一个 {@code Type} 对象数组，表示此类型的实际类型参数
     * @throws TypeNotPresentException 如果任何实际类型参数引用不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果任何实际类型参数引用无法实例化的参数化类型
     * @since 1.5
     */
    Type[] getActualTypeArguments();

    /**
     * 返回一个 {@code Type} 对象，表示声明此类型的类或接口。
     *
     * @return 一个 {@code Type} 对象，表示声明此类型的类或接口
     * @since 1.5
     */
    Type getRawType();

    /**
     * 返回一个 {@code Type} 对象，表示此类型所属的类型。例如，如果此类型是 {@code O<T>.I<S>}，则返回 {@code O<T>} 的表示。
     *
     * <p>如果此类型是顶级类型，则返回 {@code null}。
     *
     * @return 一个 {@code Type} 对象，表示此类型所属的类型。如果此类型是顶级类型，则返回 {@code null}
     * @throws TypeNotPresentException 如果所有者类型引用不存在的类型声明
     * @throws MalformedParameterizedTypeException 如果所有者类型引用无法实例化的参数化类型
     * @since 1.5
     */
    Type getOwnerType();
}
