/*
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
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
import static java.lang.annotation.ElementType.*;

/**
 * 表示应在注解元素（及其包含的所有程序元素）中抑制命名的编译器警告。请注意，给定元素中抑制的警告集是所有包含元素中抑制警告的超集。例如，如果您注解一个类以抑制一个警告，并注解一个方法以抑制另一个警告，那么这两个警告都将在该方法中被抑制。
 *
 * <p>作为风格问题，程序员应始终在注解最深层的元素上使用此注解。如果您希望在一个特定方法中抑制警告，应注解该方法而不是其类。
 *
 * @author Josh Bloch
 * @since 1.5
 * @jls 4.8 原始类型
 * @jls 4.12.2 引用类型的变量
 * @jls 5.1.9 未检查的转换
 * @jls 5.5.2 已检查的转换和未检查的转换
 * @jls 9.6.3.5 @SuppressWarnings
 */
@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE})
@Retention(RetentionPolicy.SOURCE)
public @interface SuppressWarnings {
    /**
     * 编译器应在注解元素中抑制的警告集。允许重复名称。第二个及后续出现的名称将被忽略。存在未识别的警告名称不是错误：编译器必须忽略它们不认识的任何警告名称。但是，如果注解包含未识别的警告名称，编译器可以自由地发出警告。
     *
     * <p>字符串 {@code "unchecked"} 用于抑制未检查的警告。编译器供应商应记录与此注解类型结合使用的其他警告名称。他们被鼓励合作以确保相同的名称在多个编译器中有效。
     * @return 要抑制的警告集
     */
    String[] value();
}
