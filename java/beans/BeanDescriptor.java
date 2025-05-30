/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.beans;

import java.lang.ref.Reference;

/**
 * BeanDescriptor 提供关于“bean”的全局信息，
 * 包括其 Java 类、其 displayName 等。
 * <p>
 * 这是 BeanInfo 对象返回的几种描述符之一，
 * 其他还包括属性、方法和事件的描述符。
 */

public class BeanDescriptor extends FeatureDescriptor {

    private Reference<? extends Class<?>> beanClassRef;
    private Reference<? extends Class<?>> customizerClassRef;

    /**
     * 为没有自定义器的 bean 创建 BeanDescriptor。
     *
     * @param beanClass  实现 bean 的 Java 类的 Class 对象。例如 sun.beans.OurButton.class。
     */
    public BeanDescriptor(Class<?> beanClass) {
        this(beanClass, null);
    }

    /**
     * 为有自定义器的 bean 创建 BeanDescriptor。
     *
     * @param beanClass  实现 bean 的 Java 类的 Class 对象。例如 sun.beans.OurButton.class。
     * @param customizerClass  实现 bean 的自定义器的 Java 类的 Class 对象。例如 sun.beans.OurButtonCustomizer.class。
     */
    public BeanDescriptor(Class<?> beanClass, Class<?> customizerClass) {
        this.beanClassRef = getWeakReference(beanClass);
        this.customizerClassRef = getWeakReference(customizerClass);

        String name = beanClass.getName();
        while (name.indexOf('.') >= 0) {
            name = name.substring(name.indexOf('.')+1);
        }
        setName(name);
    }

    /**
     * 获取 bean 的 Class 对象。
     *
     * @return bean 的 Class 对象。
     */
    public Class<?> getBeanClass() {
        return (this.beanClassRef != null)
                ? this.beanClassRef.get()
                : null;
    }

    /**
     * 获取 bean 的自定义器的 Class 对象。
     *
     * @return bean 的自定义器的 Class 对象。如果没有自定义器，则可能为 null。
     */
    public Class<?> getCustomizerClass() {
        return (this.customizerClassRef != null)
                ? this.customizerClassRef.get()
                : null;
    }

    /*
     * 包私有的复制构造函数
     * 必须使新对象与旧对象的任何更改隔离。
     */
    BeanDescriptor(BeanDescriptor old) {
        super(old);
        beanClassRef = old.beanClassRef;
        customizerClassRef = old.customizerClassRef;
    }

    void appendTo(StringBuilder sb) {
        appendTo(sb, "beanClass", this.beanClassRef);
        appendTo(sb, "customizerClass", this.customizerClassRef);
    }
}
