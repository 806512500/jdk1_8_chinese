/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.geom;

/**
 * <code>Dimension2D</code> 类用于封装宽度和高度尺寸。
 * <p>
 * 该类是所有存储2D尺寸对象的抽象超类。
 * 实际的尺寸存储表示形式由子类决定。
 *
 * @author      Jim Graham
 * @since 1.2
 */
public abstract class Dimension2D implements Cloneable {

    /**
     * 这是一个抽象类，不能直接实例化。
     * 提供了特定类型的实现子类，可以实例化并提供多种格式来存储
     * 满足以下各种访问方法所需的信息。
     *
     * @see java.awt.Dimension
     * @since 1.2
     */
    protected Dimension2D() {
    }

    /**
     * 返回此 <code>Dimension</code> 的宽度，精度为双精度。
     * @return 此 <code>Dimension</code> 的宽度。
     * @since 1.2
     */
    public abstract double getWidth();

    /**
     * 返回此 <code>Dimension</code> 的高度，精度为双精度。
     * @return 此 <code>Dimension</code> 的高度。
     * @since 1.2
     */
    public abstract double getHeight();

    /**
     * 将此 <code>Dimension</code> 对象的尺寸设置为指定的宽度和高度。
     * 包含此方法是为了完整性，以与
     * {@link java.awt.Component#getSize getSize} 方法平行。
     * @param width  <code>Dimension</code> 对象的新宽度
     * @param height  <code>Dimension</code> 对象的新高度
     * @since 1.2
     */
    public abstract void setSize(double width, double height);

    /**
     * 将此 <code>Dimension2D</code> 对象的尺寸设置为指定的尺寸。
     * 包含此方法是为了完整性，以与
     * <code>Component</code> 的 <code>getSize</code> 方法平行。
     * @param d  <code>Dimension2D</code> 对象的新尺寸
     * @since 1.2
     */
    public void setSize(Dimension2D d) {
        setSize(d.getWidth(), d.getHeight());
    }

    /**
     * 创建一个与本对象同类型的对象。
     *
     * @return     本实例的克隆。
     * @exception  OutOfMemoryError            如果内存不足。
     * @see        java.lang.Cloneable
     * @since      1.2
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们是可克隆的
            throw new InternalError(e);
        }
    }
}
