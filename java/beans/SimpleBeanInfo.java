/*
 * Copyright (c) 1996, 2015, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.Toolkit;
import java.awt.image.ImageProducer;
import java.net.URL;

/**
 * 这是一个支持类，使人们更容易提供
 * BeanInfo 类。
 * <p>
 * 它默认提供“noop”信息，并可以选择性地
 * 覆盖以在选定主题上提供更明确的信息。
 * 当 Introspector 看到“noop”值时，它将应用低
 * 级别内省和设计模式来自动分析目标 bean。
 */

public class SimpleBeanInfo implements BeanInfo {

    /**
     * 拒绝提供有关 bean 的类和定制器的知识。
     * 如果您希望提供显式信息，可以覆盖此方法。
     */
    public BeanDescriptor getBeanDescriptor() {
        return null;
    }

    /**
     * 拒绝提供属性知识。如果您希望提供显式属性信息，
     * 可以覆盖此方法。
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return null;
    }

    /**
     * 拒绝提供默认属性的知识。如果您希望为 bean 定义默认属性，
     * 可以覆盖此方法。
     */
    public int getDefaultPropertyIndex() {
        return -1;
    }

    /**
     * 拒绝提供事件集的知识。如果您希望提供显式事件集信息，
     * 可以覆盖此方法。
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return null;
    }

    /**
     * 拒绝提供默认事件的知识。如果您希望为 bean 定义默认事件，
     * 可以覆盖此方法。
     */
    public int getDefaultEventIndex() {
        return -1;
    }

    /**
     * 拒绝提供方法的知识。如果您希望提供显式方法信息，
     * 可以覆盖此方法。
     */
    public MethodDescriptor[] getMethodDescriptors() {
        return null;
    }

    /**
     * 声称没有其他相关的 BeanInfo 对象。如果您希望（例如）返回
     * 基类的 BeanInfo，可以覆盖此方法。
     */
    public BeanInfo[] getAdditionalBeanInfo() {
        return null;
    }

    /**
     * 声称没有可用的图标。如果您希望为您的 bean 提供图标，
     * 可以覆盖此方法。
     */
    public Image getIcon(int iconKind) {
        return null;
    }

    /**
     * 这是一个实用方法，用于加载图标图像。
     * 它接受与当前对象类文件关联的资源文件的名称，并从该文件中加载图像对象。
     * 通常图像将是 GIF。
     * <p>
     * @param resourceName  相对于当前类目录的路径名。例如，
     *          "wombat.gif"。
     * @return  图像对象。如果加载失败，可能为 null。
     */
    public Image loadImage(final String resourceName) {
        try {
            final URL url = getClass().getResource(resourceName);
            if (url != null) {
                final ImageProducer ip = (ImageProducer) url.getContent();
                if (ip != null) {
                    return Toolkit.getDefaultToolkit().createImage(ip);
                }
            }
        } catch (final Exception ignored) {
        }
        return null;
    }
}
