/*
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Method;

/**
 * 抛出此异常表示程序尝试访问一个注解的元素，而该元素的类型在注解被编译（或序列化）后发生了变化。
 * 该异常可以由 {@linkplain
 * java.lang.reflect.AnnotatedElement 用于反射读取注解的API} 抛出。
 *
 * @author  Josh Bloch
 * @see     java.lang.reflect.AnnotatedElement
 * @since 1.5
 */
public class AnnotationTypeMismatchException extends RuntimeException {
    private static final long serialVersionUID = 8125925355765570191L;

    /**
     * 注解元素的 <tt>Method</tt> 对象。
     */
    private final Method element;

    /**
     * 在注解中找到的数据的（错误的）类型。此字符串可能包含值，但不要求包含。字符串的确切格式未指定。
     */
    private final String foundType;

    /**
     * 构造一个指定注解类型元素和找到的数据类型的 AnnotationTypeMismatchException。
     *
     * @param element 注解元素的 <tt>Method</tt> 对象
     * @param foundType 在注解中找到的数据的（错误的）类型。此字符串可能包含值，但不要求包含。字符串的确切格式未指定。
     */
    public AnnotationTypeMismatchException(Method element, String foundType) {
        super("Incorrectly typed data found for annotation element " + element
              + " (Found data of type " + foundType + ")");
        this.element = element;
        this.foundType = foundType;
    }

    /**
     * 返回类型错误的元素的 <tt>Method</tt> 对象。
     *
     * @return 类型错误的元素的 <tt>Method</tt> 对象
     */
    public Method element() {
        return this.element;
    }

    /**
     * 返回类型错误的元素中找到的数据类型。返回的字符串可能包含值，但不要求包含。字符串的确切格式未指定。
     *
     * @return 类型错误的元素中找到的数据类型
     */
    public String foundType() {
        return this.foundType;
    }
}
