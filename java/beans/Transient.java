/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 表示在 {@link Introspector} 构造
 * 与注解代码元素相关的 {@link PropertyDescriptor} 或 {@link EventSetDescriptor}
 * 类时，应声明一个名为 "transient" 的属性，并赋予给定的 {@code value}。
 * "transient" 属性的 {@code true} 值
 * 表示从 {@link Encoder} 派生的编码器
 * 应忽略此功能。
 * <p>
 * {@code Transient} 注解可用于
 * 任何涉及 {@link FeatureDescriptor} 子类的方法中，
 * 以在注解类及其子类中标识瞬态功能。
 * 通常，以 "get" 开头的方法是放置注解的最佳位置，
 * 并且在为同一功能定义了多个注解的情况下，
 * 该声明优先。
 * <p>
 * 要在类中声明一个非瞬态功能，
 * 而其超类声明为瞬态，
 * 使用 {@code @Transient(false)}。
 * 在所有情况下，{@link Introspector} 通过引用
 * 最具体的超类上的注解来决定
 * 一个功能是否为瞬态。
 * 如果在任何超类中都没有 {@code Transient} 注解，
 * 则该功能不是瞬态的。
 *
 * @since 1.7
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface Transient {
    /**
     * 返回 {@code Introspector} 是否应
     * 为注解的方法构造工件。
     * @return {@code Introspector} 是否应
     * 为注解的方法构造工件
     */
    boolean value() default true;
}
