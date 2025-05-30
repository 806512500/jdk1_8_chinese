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

package java.lang.invoke;

import java.lang.annotation.*;

/**
 * 如果一个字段的所有组成部分变量最多只改变一次，可以将其注解为稳定。
 * 字段的值被视为其组成部分值。
 * 如果字段的类型为数组，则数组的所有非空组成部分，直到字段数组类型的秩的深度，
 * 也视为组成部分值。
 * 由此延伸，任何被注解为稳定的变量（无论是数组还是字段）称为稳定变量，
 * 其非空或非零值称为稳定值。
 * <p>
 * 由于所有字段都以引用的默认值为 null（或原始类型的零）开始，
 * 因此该注解表示存储在字段中的第一个非空（或非零）值将永远不会改变。
 * <p>
 * 如果字段不是数组类型，则没有数组元素，
 * 那么被标记为稳定的值就是字段的值。
 * 如果字段值的动态类型是数组，但静态类型不是，
 * 则数组的组成部分 <em>不</em> 被视为稳定。
 * <p>
 * 如果字段是数组类型，则字段值和字段值的所有组成部分（如果字段值非空）
 * 都被标记为稳定。
 * 如果字段类型是秩为 {@code N > 1} 的数组类型，
 * 则字段值的每个组成部分（如果字段值非空）被视为秩为 {@code N-1} 的稳定数组。
 * <p>
 * 被声明为 {@code final} 的字段也可以注解为稳定。
 * 由于 final 字段已经表现为稳定值，因此这样的注解不提供额外的信息，
 * 除非字段的类型是数组类型。
 * <p>
 * 如果一个被注解为稳定的字段被赋予第三个值，其行为（目前）是未定义的。
 * 实际上，如果 JVM 依赖此注解将字段引用提升为常量，
 * 那么如果在字段值改变后仍使用该常量（字段的第二个值）作为字段的值，
 * 可能会显得 Java 内存模型被破坏。
 */
/* package-private */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface Stable {
}
