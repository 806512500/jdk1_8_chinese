/*
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.beans.ConstructorProperties;

/**
 * <code>GradientPaint</code> 类提供了一种方式，用于使用线性颜色渐变模式填充 {@link Shape}。
 * 如果在用户空间中指定了点 P1 和颜色 C1 以及点 P2 和颜色 C2，那么 P1 和 P2 连接线上的颜色将从 C1 比例变化到 C2。
 * 任何不在扩展的 P1 和 P2 连接线上的点 P 将具有该点 P' 的颜色，P' 是 P 在扩展的 P1 和 P2 连接线上的垂直投影。
 * 扩展线段外的点可以以两种方式之一着色。
 * <ul>
 * <li>
 * 如果渐变是循环的，则扩展的 P1 和 P2 连接线上的点将在颜色 C1 和 C2 之间循环。
 * <li>
 * 如果渐变是非循环的，则线段 P1 侧的点具有恒定的颜色 C1，而线段 P2 侧的点具有恒定的颜色 C2。
 * </ul>
 *
 * @see Paint
 * @see Graphics2D#setPaint
 * @version 10 Feb 1997
 */

public class GradientPaint implements Paint {
    Point2D.Float p1;
    Point2D.Float p2;
    Color color1;
    Color color2;
    boolean cyclic;

    /**
     * 构造一个简单的非循环 <code>GradientPaint</code> 对象。
     * @param x1 第一个指定的点在用户空间中的 x 坐标
     * @param y1 第一个指定的点在用户空间中的 y 坐标
     * @param color1 第一个指定的点的颜色
     * @param x2 第二个指定的点在用户空间中的 x 坐标
     * @param y2 第二个指定的点在用户空间中的 y 坐标
     * @param color2 第二个指定的点的颜色
     * @throws NullPointerException 如果颜色之一为 null
     */
    public GradientPaint(float x1,
                         float y1,
                         Color color1,
                         float x2,
                         float y2,
                         Color color2) {
        if ((color1 == null) || (color2 == null)) {
            throw new NullPointerException("颜色不能为 null");
        }

        p1 = new Point2D.Float(x1, y1);
        p2 = new Point2D.Float(x2, y2);
        this.color1 = color1;
        this.color2 = color2;
    }

    /**
     * 构造一个简单的非循环 <code>GradientPaint</code> 对象。
     * @param pt1 第一个指定的点在用户空间中的坐标
     * @param color1 第一个指定的点的颜色
     * @param pt2 第二个指定的点在用户空间中的坐标
     * @param color2 第二个指定的点的颜色
     * @throws NullPointerException 如果颜色或点之一为 null
     */
    public GradientPaint(Point2D pt1,
                         Color color1,
                         Point2D pt2,
                         Color color2) {
        if ((color1 == null) || (color2 == null) ||
            (pt1 == null) || (pt2 == null)) {
            throw new NullPointerException("颜色和点应非 null");
        }

        p1 = new Point2D.Float((float)pt1.getX(), (float)pt1.getY());
        p2 = new Point2D.Float((float)pt2.getX(), (float)pt2.getY());
        this.color1 = color1;
        this.color2 = color2;
    }

    /**
     * 根据 <code>boolean</code> 参数构造一个循环或非循环的 <code>GradientPaint</code> 对象。
     * @param x1 第一个指定的点在用户空间中的 x 坐标
     * @param y1 第一个指定的点在用户空间中的 y 坐标
     * @param color1 第一个指定的点的颜色
     * @param x2 第二个指定的点在用户空间中的 x 坐标
     * @param y2 第二个指定的点在用户空间中的 y 坐标
     * @param color2 第二个指定的点的颜色
     * @param cyclic 如果渐变模式应在两个颜色之间循环，则为 <code>true</code>；否则为 <code>false</code>
     */
    public GradientPaint(float x1,
                         float y1,
                         Color color1,
                         float x2,
                         float y2,
                         Color color2,
                         boolean cyclic) {
        this (x1, y1, color1, x2, y2, color2);
        this.cyclic = cyclic;
    }

    /**
     * 根据 <code>boolean</code> 参数构造一个循环或非循环的 <code>GradientPaint</code> 对象。
     * @param pt1 第一个指定的点在用户空间中的坐标
     * @param color1 第一个指定的点的颜色
     * @param pt2 第二个指定的点在用户空间中的坐标
     * @param color2 第二个指定的点的颜色
     * @param cyclic 如果渐变模式应在两个颜色之间循环，则为 <code>true</code>；否则为 <code>false</code>
     * @throws NullPointerException 如果颜色或点之一为 null
     */
    @ConstructorProperties({ "point1", "color1", "point2", "color2", "cyclic" })
    public GradientPaint(Point2D pt1,
                         Color color1,
                         Point2D pt2,
                         Color color2,
                         boolean cyclic) {
        this (pt1, color1, pt2, color2);
        this.cyclic = cyclic;
    }

    /**
     * 返回锚定第一个颜色的点 P1 的副本。
     * @return 一个 {@link Point2D} 对象，它是锚定第一个颜色的点的副本。
     */
    public Point2D getPoint1() {
        return new Point2D.Float(p1.x, p1.y);
    }

    /**
     * 返回由点 P1 锚定的颜色 C1。
     * @return 一个 <code>Color</code> 对象，它是由 P1 锚定的颜色。
     */
    public Color getColor1() {
        return color1;
    }

    /**
     * 返回锚定第二个颜色的点 P2 的副本。
     * @return 一个 {@link Point2D} 对象，它是锚定第二个颜色的点的副本。
     */
    public Point2D getPoint2() {
        return new Point2D.Float(p2.x, p2.y);
    }

    /**
     * 返回由点 P2 锚定的颜色 C2。
     * @return 一个 <code>Color</code> 对象，它是由 P2 锚定的颜色。
     */
    public Color getColor2() {
        return color2;
    }

    /**
     * 如果渐变在两个颜色 C1 和 C2 之间循环，则返回 <code>true</code>。
     * @return 如果渐变在两个颜色之间循环，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean isCyclic() {
        return cyclic;
    }

    /**
     * 创建并返回一个用于生成线性颜色渐变模式的 {@link PaintContext}。
     * 有关 null 参数处理的信息，请参见 {@link Paint} 接口中的 {@link Paint#createContext} 方法说明。
     *
     * @param cm 一个表示调用者接收像素数据最方便格式的首选 {@link ColorModel}，如果无偏好则为 {@code null}。
     * @param deviceBounds 被渲染的图形基元的设备空间边界框。
     * @param userBounds 被渲染的图形基元的用户空间边界框。
     * @param xform 从用户空间到设备空间的 {@link AffineTransform}。
     * @param hints 一组提示，上下文对象可以使用这些提示来选择不同的渲染选项。
     * @return 一个用于生成颜色模式的 {@code PaintContext}。
     * @see Paint
     * @see PaintContext
     * @see ColorModel
     * @see Rectangle
     * @see Rectangle2D
     * @see AffineTransform
     * @see RenderingHints
     */
    public PaintContext createContext(ColorModel cm,
                                      Rectangle deviceBounds,
                                      Rectangle2D userBounds,
                                      AffineTransform xform,
                                      RenderingHints hints) {

        return new GradientPaintContext(cm, p1, p2, xform,
                                        color1, color2, cyclic);
    }

    /**
     * 返回此 <code>GradientPaint</code> 的透明度模式。
     * @return 一个表示此 <code>GradientPaint</code> 对象透明度模式的整数值。
     * @see Transparency
     */
    public int getTransparency() {
        int a1 = color1.getAlpha();
        int a2 = color2.getAlpha();
        return (((a1 & a2) == 0xff) ? OPAQUE : TRANSLUCENT);
    }

}
