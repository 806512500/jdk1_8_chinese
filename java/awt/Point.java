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

import java.awt.geom.Point2D;
import java.beans.Transient;

/**
 * 一个表示 {@code (x,y)} 坐标空间中位置的点，以整数精度指定。
 *
 * @author      Sami Shaio
 * @since       1.0
 */
public class Point extends Point2D implements java.io.Serializable {
    /**
     * 此 <code>Point</code> 的 X 坐标。
     * 如果未设置 X 坐标，它将默认为 0。
     *
     * @serial
     * @see #getLocation()
     * @see #move(int, int)
     * @since 1.0
     */
    public int x;

    /**
     * 此 <code>Point</code> 的 Y 坐标。
     * 如果未设置 Y 坐标，它将默认为 0。
     *
     * @serial
     * @see #getLocation()
     * @see #move(int, int)
     * @since 1.0
     */
    public int y;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -5276940640259749850L;

    /**
     * 构造并初始化一个位于坐标空间原点
     * (0,&nbsp;0) 的点。
     * @since       1.1
     */
    public Point() {
        this(0, 0);
    }

    /**
     * 构造并初始化一个与指定 <code>Point</code> 对象位置相同的点。
     * @param       p 一个点
     * @since       1.1
     */
    public Point(Point p) {
        this(p.x, p.y);
    }

    /**
     * 构造并初始化一个位于坐标空间中指定
     * {@code (x,y)} 位置的点。
     * @param x 新构造的 <code>Point</code> 的 X 坐标
     * @param y 新构造的 <code>Point</code> 的 Y 坐标
     * @since 1.0
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getX() {
        return x;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getY() {
        return y;
    }

    /**
     * 返回此点的位置。
     * 包含此方法是为了完整性，以与 <code>Component</code> 的
     * <code>getLocation</code> 方法平行。
     * @return      一个与此点位置相同的新点
     * @see         java.awt.Component#getLocation
     * @see         java.awt.Point#setLocation(java.awt.Point)
     * @see         java.awt.Point#setLocation(int, int)
     * @since       1.1
     */
    @Transient
    public Point getLocation() {
        return new Point(x, y);
    }

    /**
     * 设置此点的位置为指定位置。
     * 包含此方法是为了完整性，以与 <code>Component</code> 的
     * <code>setLocation</code> 方法平行。
     * @param       p 一个点，此点的新位置
     * @see         java.awt.Component#setLocation(java.awt.Point)
     * @see         java.awt.Point#getLocation
     * @since       1.1
     */
    public void setLocation(Point p) {
        setLocation(p.x, p.y);
    }

    /**
     * 更改此点的位置为指定位置。
     * <p>
     * 包含此方法是为了完整性，以与 <code>Component</code> 的
     * <code>setLocation</code> 方法平行。
     * 其行为与 <code>move(int,&nbsp;int)</code> 相同。
     * @param       x 新位置的 X 坐标
     * @param       y 新位置的 Y 坐标
     * @see         java.awt.Component#setLocation(int, int)
     * @see         java.awt.Point#getLocation
     * @see         java.awt.Point#move(int, int)
     * @since       1.1
     */
    public void setLocation(int x, int y) {
        move(x, y);
    }

    /**
     * 设置此点的位置为指定的双精度坐标。
     * 双精度值将被四舍五入为整数值。
     * 任何小于 <code>Integer.MIN_VALUE</code> 的数
     * 将被重置为 <code>MIN_VALUE</code>，任何大于
     * <code>Integer.MAX_VALUE</code> 的数将被
     * 重置为 <code>MAX_VALUE</code>。
     *
     * @param x 新位置的 X 坐标
     * @param y 新位置的 Y 坐标
     * @see #getLocation
     */
    public void setLocation(double x, double y) {
        this.x = (int) Math.floor(x+0.5);
        this.y = (int) Math.floor(y+0.5);
    }

    /**
     * 将此点移动到坐标平面中的指定位置。此方法
     * 与 <code>setLocation(int,&nbsp;int)</code> 相同。
     * @param       x 新位置的 X 坐标
     * @param       y 新位置的 Y 坐标
     * @see         java.awt.Component#setLocation(int, int)
     */
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 将此点从位置 {@code (x,y)} 沿 X 轴移动 {@code dx}，沿 Y 轴移动 {@code dy}，
     * 使其现在表示点 {@code (x+dx,y+dy)}。
     *
     * @param       dx   沿 X 轴移动此点的距离
     * @param       dy   沿 Y 轴移动此点的距离
     */
    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    /**
     * 确定两个点是否相等。两个 <code>Point2D</code> 实例
     * 如果它们的 <code>x</code> 和 <code>y</code> 成员字段值，
     * 表示它们在坐标空间中的位置相同，则认为它们相等。
     * @param obj 要与此 <code>Point2D</code> 比较的对象
     * @return 如果要比较的对象是 <code>Point2D</code> 的实例并且
     *         具有相同的值，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point pt = (Point)obj;
            return (x == pt.x) && (y == pt.y);
        }
        return super.equals(obj);
    }

    /**
     * 返回此点及其在 {@code (x,y)} 坐标空间中的位置的字符串表示。
     * 此方法仅用于调试目的，返回字符串的内容和格式可能因实现而异。
     * 返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return  此点的字符串表示
     */
    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + "]";
    }
}
