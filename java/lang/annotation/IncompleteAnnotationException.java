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
 * 抛出此异常表示程序尝试访问在注解编译（或序列化）后添加到注解类型定义中的元素。如果新元素有默认值，则不会抛出此异常。
 * 此异常可以通过 {@linkplain
 * java.lang.reflect.AnnotatedElement 用于反射读取注解的API} 抛出。
 *
 * @author  Josh Bloch
 * @see     java.lang.reflect.AnnotatedElement
 * @since 1.5
 */
public class IncompleteAnnotationException extends RuntimeException {
    private static final long serialVersionUID = 8445097402741811912L;

    private Class<? extends Annotation> annotationType;
    private String elementName;

    /**
     * 构造一个 IncompleteAnnotationException，表示指定注解类型中缺少命名的元素。
     *
     * @param annotationType 注解类型的 Class 对象
     * @param elementName 缺少的元素的名称
     * @throws NullPointerException 如果任一参数为 {@code null}
     */
    public IncompleteAnnotationException(
            Class<? extends Annotation> annotationType,
            String elementName) {
        super(annotationType.getName() + " 缺少元素 " +
              elementName.toString());

        this.annotationType = annotationType;
        this.elementName = elementName;
    }

    /**
     * 返回缺少元素的注解类型的 Class 对象。
     *
     * @return 缺少元素的注解类型的 Class 对象
     */
    public Class<? extends Annotation> annotationType() {
        return annotationType;
    }

    /**
     * 返回缺少的元素的名称。
     *
     * @return 缺少的元素的名称
     */
    public String elementName() {
        return elementName;
    }
}
