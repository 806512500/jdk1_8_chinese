/*
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Image;

/**
 * 使用 {@code BeanInfo} 接口
 * 创建一个 {@code BeanInfo} 类
 * 并提供关于方法、属性、事件和其他功能的显式信息。
 * <p>
 * 在开发 bean 时，您可以实现应用程序任务所需的功能，
 * 而忽略其余的 {@code BeanInfo} 功能。
 * 它们将通过自动分析获得
 * 通过使用 bean 方法的低级反射
 * 并应用标准设计模式。
 * 您有机会通过各种描述符类提供额外的 bean 信息。
 * <p>
 * 请参阅 {@link SimpleBeanInfo} 类，
 * 它是 {@code BeanInfo} 类的便捷基础类。
 * 您可以覆盖 {@code SimpleBeanInfo} 类的方法和属性以定义特定信息。
 * <p>
 * 有关 bean 行为的更多信息，请参阅 {@link Introspector} 类。
 */
public interface BeanInfo {

    /**
     * 返回提供关于 bean 的总体信息的 bean 描述符，
     * 例如其显示名称或自定义器。
     *
     * @return  一个 {@link BeanDescriptor} 对象，
     *          或 {@code null} 如果信息是通过自动分析获得的
     */
    BeanDescriptor getBeanDescriptor();

    /**
     * 返回定义此 bean 触发的事件类型的事件描述符。
     *
     * @return  一个 {@link EventSetDescriptor} 对象数组，
     *          或 {@code null} 如果信息是通过自动分析获得的
     */
    EventSetDescriptor[] getEventSetDescriptors();

    /**
     * 一个 bean 可能有一个默认事件，通常在使用此 bean 时应用。
     *
     * @return  由 {@code getEventSetDescriptors} 方法返回的 {@code EventSetDescriptor} 数组中的默认事件索引，
     *          或 -1 如果没有默认事件
     */
    int getDefaultEventIndex();

    /**
     * 返回 bean 的所有属性的描述符。
     * <p>
     * 如果属性是索引的，则其在结果数组中的条目
     * 属于 {@link IndexedPropertyDescriptor} 子类
     * 的 {@link PropertyDescriptor} 类。
     * {@code getPropertyDescriptors} 方法的客户端
     * 可以使用 {@code instanceof} 操作符检查
     * 给定的 {@code PropertyDescriptor}
     * 是否是 {@code IndexedPropertyDescriptor}。
     *
     * @return  一个 {@code PropertyDescriptor} 对象数组，
     *          或 {@code null} 如果信息是通过自动分析获得的
     */
    PropertyDescriptor[] getPropertyDescriptors();

    /**
     * 一个 bean 可能有一个默认属性，通常在自定义此 bean 时更新。
     *
     * @return  由 {@code getPropertyDescriptors} 方法返回的 {@code PropertyDescriptor} 数组中的默认属性索引，
     *          或 -1 如果没有默认属性
     */
    int getDefaultPropertyIndex();

    /**
     * 返回定义此 bean 支持的外部可见方法的方法描述符。
     *
     * @return  一个 {@link MethodDescriptor} 对象数组，
     *          或 {@code null} 如果信息是通过自动分析获得的
     */
    MethodDescriptor[] getMethodDescriptors();

    /**
     * 此方法使当前 {@code BeanInfo} 对象
     * 返回一个任意的其他 {@code BeanInfo} 对象集合，
     * 提供有关当前 bean 的额外信息。
     * <p>
     * 如果不同 {@code BeanInfo} 对象提供的信息
     * 有冲突或重叠，
     * 当前 {@code BeanInfo} 对象优先于额外的 {@code BeanInfo} 对象。
     * 索引较高的数组元素优先于索引较低的元素。
     *
     * @return  一个 {@code BeanInfo} 对象数组，
     *          或 {@code null} 如果没有额外的 {@code BeanInfo} 对象
     */
    BeanInfo[] getAdditionalBeanInfo();

    /**
     * 返回一个可以用于在工具箱或工具栏中表示 bean 的图像。
     * <p>
     * 有四种可能的图标类型：
     * 16 x 16 彩色，32 x 32 彩色，16 x 16 单色，32 x 32 单色。
     * 如果您实现一个仅支持单个图标的 bean，
     * 建议使用 16 x 16 彩色。
     * 另一个建议是为图标设置透明背景。
     *
     * @param  iconKind  请求的图标类型
     * @return           一个表示请求图标的图像对象，
     *                   或 {@code null} 如果没有合适的图标可用
     *
     * @see #ICON_COLOR_16x16
     * @see #ICON_COLOR_32x32
     * @see #ICON_MONO_16x16
     * @see #ICON_MONO_32x32
     */
    Image getIcon(int iconKind);

    /**
     * 表示 16 x 16 彩色图标的常量。
     */
    final static int ICON_COLOR_16x16 = 1;

    /**
     * 表示 32 x 32 彩色图标的常量。
     */
    final static int ICON_COLOR_32x32 = 2;

    /**
     * 表示 16 x 16 单色图标的常量。
     */
    final static int ICON_MONO_16x16 = 3;

    /**
     * 表示 32 x 32 单色图标的常量。
     */
    final static int ICON_MONO_32x32 = 4;
}
