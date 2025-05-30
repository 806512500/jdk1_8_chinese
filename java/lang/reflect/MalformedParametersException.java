/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 当 {@link java.lang.reflect.Executable#getParameters java.lang.reflect 包} 尝试从类文件中读取方法参数时，如果确定一个或多个参数是畸形的，则抛出此异常。
 *
 * <p>以下是在哪些情况下会抛出此异常的条件列表：
 * <ul>
 * <li> 方法的参数数量（parameter_count）不正确
 * <li> 常量池索引超出范围。
 * <li> 常量池索引不指向 UTF-8 条目
 * <li> 参数的名称为空（""），或包含非法字符
 * <li> 标志字段包含非法标志（除了 FINAL, SYNTHETIC, 或 MANDATED 之外的任何标志）
 * </ul>
 *
 * 有关更多信息，请参见 {@link java.lang.reflect.Executable#getParameters}。
 *
 * @see java.lang.reflect.Executable#getParameters
 * @since 1.8
 */
public class MalformedParametersException extends RuntimeException {

    /**
     * 用于序列化的版本。
     */
    private static final long serialVersionUID = 20130919L;

    /**
     * 创建一个带有空原因的 {@code MalformedParametersException}。
     */
    public MalformedParametersException() {}

    /**
     * 创建一个 {@code MalformedParametersException}。
     *
     * @param reason 异常的原因。
     */
    public MalformedParametersException(String reason) {
        super(reason);
    }
}
