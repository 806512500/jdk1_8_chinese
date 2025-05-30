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

package java.lang;

import java.lang.annotation.*;

/**
 * 程序员断言，注解的方法或构造函数的主体不会对其变长参数执行潜在的不安全操作。应用此注解到方法或构造函数会抑制关于<i>不可再现实</i>变长参数类型的未检查警告，以及在调用站点抑制参数化数组创建的未检查警告。
 *
 * <p> 除了其 {@link
 * Target @Target} 元注解施加的使用限制外，编译器还必须实现对此注解类型的附加使用限制；如果方法或构造函数声明被 {@code @SafeVarargs} 注解注解，并且满足以下任一条件，则为编译时错误：
 * <ul>
 * <li> 声明是一个固定参数数量的方法或构造函数
 *
 * <li> 声明是一个变长参数方法，但既不是 {@code static} 也不是 {@code final}。
 *
 * </ul>
 *
 * <p> 编译器被鼓励在将此注解类型应用于方法或构造函数声明时发出警告，如果：
 *
 * <ul>
 *
 * <li> 变长参数具有可再现实元素类型，包括原始类型、{@code Object} 和 {@code String}。（此注解类型抑制的未检查警告对于可再现实元素类型已经不会发生。）
 *
 * <li> 方法或构造函数声明的主体执行潜在的不安全操作，例如，对变长参数数组的元素进行赋值，从而生成未检查警告。一些不安全操作不会触发未检查警告。例如，以下代码中的别名问题：
 *
 * <blockquote><pre>
 * &#64;SafeVarargs // 实际上不安全！
 * static void m(List&lt;String&gt;... stringLists) {
 *   Object[] array = stringLists;
 *   List&lt;Integer&gt; tmpList = Arrays.asList(42);
 *   array[0] = tmpList; // 语义上无效，但编译时没有警告
 *   String s = stringLists[0].get(0); // 哦不，运行时出现 ClassCastException！
 * }
 * </pre></blockquote>
 *
 * 会导致运行时的 {@code ClassCastException}。
 *
 * <p>平台的未来版本可能会强制编译器在执行此类不安全操作时产生错误。
 *
 * </ul>
 *
 * @since 1.7
 * @jls 4.7 可再现实类型
 * @jls 8.4.1 形式参数
 * @jls 9.6.3.7 @SafeVarargs
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface SafeVarargs {}
