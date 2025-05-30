/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.awt;

import java.awt.geom.Dimension2D;
import java.beans.Transient;

/**
 * <code>Dimension</code> 类封装了组件的宽度和高度（以整数精度）在一个单独的对象中。
 * 该类与组件的某些属性相关联。由 <code>Component</code> 类和
 * <code>LayoutManager</code> 接口定义的几个方法返回一个
 * <code>Dimension</code> 对象。
 * <p>
 * 通常情况下，<code>width</code>
 * 和 <code>height</code> 的值是非负整数。
 * 创建维度的构造函数并不阻止你为这些属性设置负值。
 * 如果 <code>width</code> 或 <code>height</code> 的值
 * 为负，其他对象定义的某些方法的行为是未定义的。
 *
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 * @see         java.awt.Component
 * @see         java.awt.LayoutManager
 * @since       1.0
 */
public class Dimension extends Dimension2D implements java.io.Serializable {

    /**
     * 宽度维度；可以使用负值。
     *
     * @serial
     * @see #getSize
     * @see #setSize
     * @since 1.0
     */
    public int width;

    /**
     * 高度维度；可以使用负值。
     *
     * @serial
     * @see #getSize
     * @see #setSize
     * @since 1.0
     */
    public int height;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = 4723952579491349524L;

    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * 创建一个宽度为零、高度为零的 <code>Dimension</code> 实例。
     */
    public Dimension() {
        this(0, 0);
    }

    /**
     * 创建一个 <code>Dimension</code> 实例，其宽度和高度与指定的维度相同。
     *
     * @param    d   指定的维度，用于
     *               <code>width</code> 和
     *               <code>height</code> 值
     */
    public Dimension(Dimension d) {
        this(d.width, d.height);
    }

    /**
     * 构造一个 <code>Dimension</code> 并初始化
     * 为指定的宽度和指定的高度。
     *
     * @param width 指定的宽度
     * @param height 指定的高度
     */
    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getWidth() {
        return width;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getHeight() {
        return height;
    }

    /**
     * 将此 <code>Dimension</code> 对象的大小设置为
     * 指定的宽度和高度（双精度）。
     * 注意，如果 <code>width</code> 或 <code>height</code>
     * 大于 <code>Integer.MAX_VALUE</code>，它们将
     * 被重置为 <code>Integer.MAX_VALUE</code>。
     *
     * @param width  此 <code>Dimension</code> 对象的新宽度
     * @param height 此 <code>Dimension</code> 对象的新高度
     * @since 1.2
     */
    public void setSize(double width, double height) {
        this.width = (int) Math.ceil(width);
        this.height = (int) Math.ceil(height);
    }

    /**
     * 获取此 <code>Dimension</code> 对象的大小。
     * 此方法包含在内以确保完整性，与 <code>Component</code>
     * 定义的 <code>getSize</code> 方法平行。
     *
     * @return   此维度的大小，一个新的 <code>Dimension</code> 实例，具有相同的宽度和高度
     * @see      java.awt.Dimension#setSize
     * @see      java.awt.Component#getSize
     * @since    1.1
     */
    @Transient
    public Dimension getSize() {
        return new Dimension(width, height);
    }

    /**
     * 将此 <code>Dimension</code> 对象的大小设置为指定的大小。
     * 此方法包含在内以确保完整性，与 <code>Component</code>
     * 定义的 <code>setSize</code> 方法平行。
     * @param    d  此 <code>Dimension</code> 对象的新大小
     * @see      java.awt.Dimension#getSize
     * @see      java.awt.Component#setSize
     * @since    1.1
     */
    public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }

    /**
     * 将此 <code>Dimension</code> 对象的大小设置为
     * 指定的宽度和高度。
     * 此方法包含在内以确保完整性，与 <code>Component</code>
     * 定义的 <code>setSize</code> 方法平行。
     *
     * @param    width   此 <code>Dimension</code> 对象的新宽度
     * @param    height  此 <code>Dimension</code> 对象的新高度
     * @see      java.awt.Dimension#getSize
     * @see      java.awt.Component#setSize
     * @since    1.1
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * 检查两个维度对象是否有相等的值。
     */
    public boolean equals(Object obj) {
        if (obj instanceof Dimension) {
            Dimension d = (Dimension)obj;
            return (width == d.width) && (height == d.height);
        }
        return false;
    }

    /**
     * 返回此 <code>Dimension</code> 的哈希码。
     *
     * @return    此 <code>Dimension</code> 的哈希码
     */
    public int hashCode() {
        int sum = width + height;
        return sum * (sum + 1)/2 + width;
    }

    /**
     * 返回此 <code>Dimension</code> 对象的 <code>height</code> 和
     * <code>width</code> 字段值的字符串表示形式。此方法仅用于调试目的，返回的
     * 字符串的内容和格式可能因实现而异。返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return  此 <code>Dimension</code> 对象的字符串表示形式
     */
    public String toString() {
        return getClass().getName() + "[width=" + width + ",height=" + height + "]";
    }
}
