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

import java.awt.Shape;
import java.awt.Rectangle;
import java.beans.Transient;

/**
 * <code>RectangularShape</code> 是多个 {@link Shape} 对象的基类，这些对象的几何形状由一个矩形框架定义。
 * 该类本身并不直接指定任何具体的几何形状，而是提供了一组由其子类继承的方法，用于查询和修改矩形框架，
 * 为子类定义几何形状提供参考。
 *
 * @author      Jim Graham
 * @since 1.2
 */
public abstract class RectangularShape implements Shape, Cloneable {

    /**
     * 这是一个抽象类，不能直接实例化。
     *
     * @see Arc2D
     * @see Ellipse2D
     * @see Rectangle2D
     * @see RoundRectangle2D
     * @since 1.2
     */
    protected RectangularShape() {
    }

    /**
     * 返回矩形框架左上角的 X 坐标，精度为 <code>double</code>。
     * @return 矩形框架左上角的 X 坐标。
     * @since 1.2
     */
    public abstract double getX();

    /**
     * 返回矩形框架左上角的 Y 坐标，精度为 <code>double</code>。
     * @return 矩形框架左上角的 Y 坐标。
     * @since 1.2
     */
    public abstract double getY();

    /**
     * 返回矩形框架的宽度，精度为 <code>double</code>。
     * @return 矩形框架的宽度。
     * @since 1.2
     */
    public abstract double getWidth();

    /**
     * 返回矩形框架的高度，精度为 <code>double</code>。
     * @return 矩形框架的高度。
     * @since 1.2
     */
    public abstract double getHeight();

    /**
     * 返回 <code>Shape</code> 矩形框架的最小 X 坐标，精度为 <code>double</code>。
     * @return <code>Shape</code> 矩形框架的最小 X 坐标。
     * @since 1.2
     */
    public double getMinX() {
        return getX();
    }

    /**
     * 返回 <code>Shape</code> 矩形框架的最小 Y 坐标，精度为 <code>double</code>。
     * @return <code>Shape</code> 矩形框架的最小 Y 坐标。
     * @since 1.2
     */
    public double getMinY() {
        return getY();
    }

    /**
     * 返回 <code>Shape</code> 矩形框架的最大 X 坐标，精度为 <code>double</code>。
     * @return <code>Shape</code> 矩形框架的最大 X 坐标。
     * @since 1.2
     */
    public double getMaxX() {
        return getX() + getWidth();
    }

    /**
     * 返回 <code>Shape</code> 矩形框架的最大 Y 坐标，精度为 <code>double</code>。
     * @return <code>Shape</code> 矩形框架的最大 Y 坐标。
     * @since 1.2
     */
    public double getMaxY() {
        return getY() + getHeight();
    }

    /**
     * 返回 <code>Shape</code> 矩形框架的中心 X 坐标，精度为 <code>double</code>。
     * @return <code>Shape</code> 矩形框架的中心 X 坐标。
     * @since 1.2
     */
    public double getCenterX() {
        return getX() + getWidth() / 2.0;
    }

    /**
     * 返回 <code>Shape</code> 矩形框架的中心 Y 坐标，精度为 <code>double</code>。
     * @return <code>Shape</code> 矩形框架的中心 Y 坐标。
     * @since 1.2
     */
    public double getCenterY() {
        return getY() + getHeight() / 2.0;
    }

    /**
     * 返回定义此对象总体形状的框架 {@link Rectangle2D}。
     * @return 一个 <code>Rectangle2D</code>，坐标为 <code>double</code>。
     * @see #setFrame(double, double, double, double)
     * @see #setFrame(Point2D, Dimension2D)
     * @see #setFrame(Rectangle2D)
     * @since 1.2
     */
    @Transient
    public Rectangle2D getFrame() {
        return new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
    }

    /**
     * 确定 <code>RectangularShape</code> 是否为空。
     * 当 <code>RectangularShape</code> 为空时，它不包含任何区域。
     * @return 如果 <code>RectangularShape</code> 为空，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.2
     */
    public abstract boolean isEmpty();

    /**
     * 将此 <code>Shape</code> 的框架矩形的位置和大小设置为指定的矩形值。
     *
     * @param x 指定矩形形状左上角的 X 坐标
     * @param y 指定矩形形状左上角的 Y 坐标
     * @param w 指定矩形形状的宽度
     * @param h 指定矩形形状的高度
     * @see #getFrame
     * @since 1.2
     */
    public abstract void setFrame(double x, double y, double w, double h);

    /**
     * 将此 <code>Shape</code> 的框架矩形的位置和大小分别设置为指定的 {@link Point2D} 和 {@link Dimension2D}。
     * 框架矩形用于 <code>RectangularShape</code> 的子类定义其几何形状。
     * @param loc 指定的 <code>Point2D</code>
     * @param size 指定的 <code>Dimension2D</code>
     * @see #getFrame
     * @since 1.2
     */
    public void setFrame(Point2D loc, Dimension2D size) {
        setFrame(loc.getX(), loc.getY(), size.getWidth(), size.getHeight());
    }

    /**
     * 将此 <code>Shape</code> 的框架矩形设置为指定的 <code>Rectangle2D</code>。
     * 框架矩形用于 <code>RectangularShape</code> 的子类定义其几何形状。
     * @param r 指定的 <code>Rectangle2D</code>
     * @see #getFrame
     * @since 1.2
     */
    public void setFrame(Rectangle2D r) {
        setFrame(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * 根据两个指定的坐标设置此 <code>Shape</code> 的框架矩形的对角线。
     * 框架矩形用于 <code>RectangularShape</code> 的子类定义其几何形状。
     *
     * @param x1 指定对角线起点的 X 坐标
     * @param y1 指定对角线起点的 Y 坐标
     * @param x2 指定对角线终点的 X 坐标
     * @param y2 指定对角线终点的 Y 坐标
     * @since 1.2
     */
    public void setFrameFromDiagonal(double x1, double y1,
                                     double x2, double y2) {
        if (x2 < x1) {
            double t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y2 < y1) {
            double t = y1;
            y1 = y2;
            y2 = t;
        }
        setFrame(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * 根据两个指定的 <code>Point2D</code> 对象设置此 <code>Shape</code> 的框架矩形的对角线。
     * 框架矩形用于 <code>RectangularShape</code> 的子类定义其几何形状。
     *
     * @param p1 指定对角线的起点 <code>Point2D</code>
     * @param p2 指定对角线的终点 <code>Point2D</code>
     * @since 1.2
     */
    public void setFrameFromDiagonal(Point2D p1, Point2D p2) {
        setFrameFromDiagonal(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    /**
     * 根据指定的中心点坐标和角点坐标设置此 <code>Shape</code> 的框架矩形。
     * 框架矩形用于 <code>RectangularShape</code> 的子类定义其几何形状。
     *
     * @param centerX 指定中心点的 X 坐标
     * @param centerY 指定中心点的 Y 坐标
     * @param cornerX 指定角点的 X 坐标
     * @param cornerY 指定角点的 Y 坐标
     * @since 1.2
     */
    public void setFrameFromCenter(double centerX, double centerY,
                                   double cornerX, double cornerY) {
        double halfW = Math.abs(cornerX - centerX);
        double halfH = Math.abs(cornerY - centerY);
        setFrame(centerX - halfW, centerY - halfH, halfW * 2.0, halfH * 2.0);
    }

    /**
     * 根据指定的中心 <code>Point2D</code> 和角点 <code>Point2D</code> 设置此 <code>Shape</code> 的框架矩形。
     * 框架矩形用于 <code>RectangularShape</code> 的子类定义其几何形状。
     * @param center 指定的中心 <code>Point2D</code>
     * @param corner 指定的角点 <code>Point2D</code>
     * @since 1.2
     */
    public void setFrameFromCenter(Point2D center, Point2D corner) {
        setFrameFromCenter(center.getX(), center.getY(),
                           corner.getX(), corner.getY());
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public Rectangle getBounds() {
        double width = getWidth();
        double height = getHeight();
        if (width < 0 || height < 0) {
            return new Rectangle();
        }
        double x = getX();
        double y = getY();
        double x1 = Math.floor(x);
        double y1 = Math.floor(y);
        double x2 = Math.ceil(x + width);
        double y2 = Math.ceil(y + height);
        return new Rectangle((int) x1, (int) y1,
                                      (int) (x2 - x1), (int) (y2 - y1));
    }

    /**
     * 返回一个迭代器对象，该对象沿 <code>Shape</code> 对象的边界迭代，并提供对 <code>Shape</code>
     * 对象几何形状的扁平化视图的访问。
     * <p>
     * 迭代器仅返回 SEG_MOVETO、SEG_LINETO 和 SEG_CLOSE 类型的点。
     * <p>
     * 曲线段的细分量由 <code>flatness</code> 参数控制，该参数指定了未扁平化变换曲线上的任何点与返回的扁平化路径段之间的最大偏差。
     * 可以指定一个可选的 {@link AffineTransform}，以便在迭代中返回的坐标相应地进行变换。
     * @param at 可选的 <code>AffineTransform</code>，用于在迭代中返回的坐标进行变换，
     *          或 <code>null</code> 表示不需要变换的坐标。
     * @param flatness 允许曲线段的线段与原始曲线上的任何点之间的最大偏差
     * @return 一个 <code>PathIterator</code> 对象，提供对 <code>Shape</code> 对象扁平化几何形状的访问。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }

    /**
     * 创建一个与该对象具有相同类和相同内容的新对象。
     * @return 该实例的克隆。
     * @exception  OutOfMemoryError 如果没有足够的内存。
     * @see        java.lang.Cloneable
     * @since      1.2
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们是 Cloneable
            throw new InternalError(e);
        }
    }
}
