/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.annotation;

/**
 * 表示注解类型适用的上下文。注解类型可能适用的声明上下文和类型上下文在 JLS 9.6.4.1 中指定，并在源代码中由 {@link ElementType java.lang.annotation.ElementType} 枚举常量表示。
 *
 * <p>如果注解类型 {@code T} 上没有 {@code @Target} 元注解，则可以将类型为 {@code T} 的注解写为任何声明的修饰符，但类型参数声明除外。
 *
 * <p>如果存在 {@code @Target} 元注解，编译器将根据 JLS 9.7.4 中的 {@code ElementType} 枚举常量指示的使用限制进行强制。
 *
 * <p>例如，此 {@code @Target} 元注解表示声明的类型本身是一个元注解类型。它只能用于注解类型声明：
 * <pre>
 *    &#064;Target(ElementType.ANNOTATION_TYPE)
 *    public &#064;interface MetaAnnotationType {
 *        ...
 *    }
 * </pre>
 *
 * <p>此 {@code @Target} 元注解表示声明的类型仅用于复杂注解类型声明中的成员类型。它不能直接用于注解任何内容：
 * <pre>
 *    &#064;Target({})
 *    public &#064;interface MemberType {
 *        ...
 *    }
 * </pre>
 *
 * <p>如果在 {@code @Target} 注解中多次出现同一个 {@code ElementType} 常量，则会导致编译时错误。例如，以下 {@code @Target} 元注解是非法的：
 * <pre>
 *    &#064;Target({ElementType.FIELD, ElementType.METHOD, ElementType.FIELD})
 *    public &#064;interface Bogus {
 *        ...
 *    }
 * </pre>
 *
 * @since 1.5
 * @jls 9.6.4.1 @Target
 * @jls 9.7.4 注解可能出现的位置
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Target {
    /**
     * 返回注解类型可以应用到的元素种类数组。
     * @return 注解类型可以应用到的元素种类数组
     */
    ElementType[] value();
}
