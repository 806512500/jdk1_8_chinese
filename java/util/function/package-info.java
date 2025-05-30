/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * <em>函数式接口</em>为 lambda 表达式和方法引用提供了目标类型。
 * 每个函数式接口都有一个单一的抽象方法，称为该函数式接口的 <em>函数方法</em>，
 * lambda 表达式的参数和返回类型将与之匹配或适应。函数式接口可以在多种上下文中提供目标类型，
 * 例如赋值上下文、方法调用上下文或类型转换上下文：
 *
 * <pre>{@code
 *     // 赋值上下文
 *     Predicate<String> p = String::isEmpty;
 *
 *     // 方法调用上下文
 *     stream.filter(e -> e.getSize() > 10)...
 *
 *     // 类型转换上下文
 *     stream.map((ToIntFunction) e -> e.getSize())...
 * }</pre>
 *
 * <p>此包中的接口是通用的函数式接口，用于 JDK，并且也可以由用户代码使用。
 * 虽然它们没有提供 lambda 表达式可能适应的所有函数形状的完整集合，但提供了足够的内容以满足常见需求。
 * 用于特定目的的其他函数式接口，如 {@link java.io.FileFilter}，在它们被使用的包中定义。
 *
 * <p>此包中的接口被注解为 {@link java.lang.FunctionalInterface}。
 * 这个注解不是编译器识别接口为函数式接口的必要条件，但仅是捕获设计意图的辅助工具，
 * 并帮助编译器识别设计意图的意外违反。
 *
 * <p>函数式接口通常代表抽象概念，如函数、动作或谓词。
 * 在记录函数式接口或引用类型为函数式接口的变量时，通常直接引用这些抽象概念，
 * 例如使用“此函数”而不是“此对象表示的函数”。
 * 当 API 方法以这种方式接受或返回函数式接口时，例如“应用提供的函数...”，
 * 这被理解为对实现适当函数式接口的对象的 <i>非空</i> 引用，除非显式指定了潜在的空性。
 *
 * <p>此包中的函数式接口遵循可扩展的命名约定，如下所示：
 *
 * <ul>
 *     <li>有几个基本的函数形状，包括
 *     {@link java.util.function.Function}（从 {@code T} 到 {@code R} 的一元函数），
 *     {@link java.util.function.Consumer}（从 {@code T} 到 {@code void} 的一元函数），
 *     {@link java.util.function.Predicate}（从 {@code T} 到 {@code boolean} 的一元函数），
 *     和 {@link java.util.function.Supplier}（到 {@code R} 的零元函数）。
 *     </li>
 *
 *     <li>函数形状具有基于它们最常用方式的自然元数。
 *     可以通过元数前缀修改基本形状以表示不同的元数，例如
 *     {@link java.util.function.BiFunction}（从 {@code T} 和 {@code U} 到 {@code R} 的二元函数）。
 *     </li>
 *
 *     <li>有额外的派生函数形状扩展了基本函数形状，包括
 *     {@link java.util.function.UnaryOperator}（扩展 {@code Function}）和
 *     {@link java.util.function.BinaryOperator}（扩展 {@code BiFunction}）。
 *     </li>
 *
 *     <li>函数式接口的类型参数可以专门化为基本类型，添加额外的类型前缀。
 *     为了专门化具有通用返回类型和通用参数的类型的返回类型，我们使用 {@code ToXxx} 前缀，
 *     例如 {@link java.util.function.ToIntFunction}。否则，类型参数从左到右专门化，
 *     例如 {@link java.util.function.DoubleConsumer} 或 {@link java.util.function.ObjIntConsumer}。
 *     （类型前缀 {@code Obj} 用于表示我们不想专门化此参数，而是继续下一个参数，
 *     例如 {@link java.util.function.ObjIntConsumer}。）这些方案可以组合，例如 {@code IntToDoubleFunction}。
 *     </li>
 *
 *     <li>如果所有参数都有专门化前缀，则可以省略元数前缀（例如 {@link java.util.function.ObjIntConsumer}）。
 *     </li>
 * </ul>
 *
 * @see java.lang.FunctionalInterface
 * @since 1.8
 */
package java.util.function;
