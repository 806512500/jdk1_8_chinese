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

package java.lang;

import java.lang.annotation.*;

/**
 * 一个信息性注解类型，用于指示接口类型声明旨在成为 Java 语言规范中定义的 <i>函数式接口</i>。
 *
 * 概念上，一个函数式接口恰好有一个抽象方法。由于 {@linkplain java.lang.reflect.Method#isDefault()
 * 默认方法} 有实现，因此它们不是抽象的。如果接口声明了一个抽象方法覆盖了 {@code java.lang.Object} 的
 * 公共方法之一，这也不会计入接口的抽象方法数量，因为任何实现该接口的类都会从 {@code java.lang.Object}
 * 或其他地方获得实现。
 *
 * <p>注意，函数式接口的实例可以使用 lambda 表达式、方法引用或构造器引用来创建。
 *
 * <p>如果一个类型被此注解类型标注，编译器必须生成错误信息，除非：
 *
 * <ul>
 * <li> 该类型是接口类型而不是注解类型、枚举或类。
 * <li> 被注解的类型满足函数式接口的要求。
 * </ul>
 *
 * <p>然而，编译器会将任何符合函数式接口定义的接口视为函数式接口，无论该接口声明上是否有 {@code FunctionalInterface}
 * 注解。
 *
 * @jls 4.3.2. 类对象
 * @jls 9.8 函数式接口
 * @jls 9.4.3 接口方法体
 * @since 1.8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FunctionalInterface {}
