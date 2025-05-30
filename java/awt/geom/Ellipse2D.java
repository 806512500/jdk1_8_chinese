/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
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

import java.io.Serializable;

/**
 * <code>Ellipse2D</code> 类描述了一个由矩形框架定义的椭圆。
 * <p>
 * 这个类是所有存储2D椭圆对象的抽象超类。
 * 实际的坐标存储表示留给子类。
 *
 * @author      Jim Graham
 * @since 1.2
 */
public abstract class Ellipse2D extends RectangularShape {

    /**
     * <code>Float</code> 类定义了一个以 <code>float</code> 精度指定的椭圆。
     * @since 1.2
     */
    public static class Float extends Ellipse2D implements Serializable {
        /**
         * 此 <code>Ellipse2D</code> 的框架矩形的左上角的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float x;

        /**
         * 此 <code>Ellipse2D</code> 的框架矩形的左上角的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float y;

        /**
         * 此 <code>Ellipse2D</code> 的总宽度。
         * @since 1.2
         * @serial
         */
        public float width;

        /**
         * 此 <code>Ellipse2D</code> 的总高度。
         * @since 1.2
         * @serial
         */
        public float height;

        /**
         * 构造一个新的 <code>Ellipse2D</code>，初始化为位置 (0,&nbsp;0) 和大小 (0,&nbsp;0)。
         * @since 1.2
         */
        public Float() {
        }

        /**
         * 从指定的坐标构造并初始化一个 <code>Ellipse2D</code>。
         *
         * @param x 框架矩形的左上角的 X 坐标
         * @param y 框架矩形的左上角的 Y 坐标
         * @param w 框架矩形的宽度
         * @param h 框架矩形的高度
         * @since 1.2
         */
        public Float(float x, float y, float w, float h) {
            setFrame(x, y, w, h);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getX() {
            return (double) x;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getY() {
            return (double) y;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getWidth() {
            return (double) width;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getHeight() {
            return (double) height;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public boolean isEmpty() {
            return (width <= 0.0 || height <= 0.0);
        }

        /**
         * 将此 <code>Shape</code> 的框架矩形的位置和大小设置为指定的矩形值。
         *
         * @param x 指定矩形形状的左上角的 X 坐标
         * @param y 指定矩形形状的左上角的 Y 坐标
         * @param w 指定矩形形状的宽度
         * @param h 指定矩形形状的高度
         * @since 1.2
         */
        public void setFrame(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setFrame(double x, double y, double w, double h) {
            this.x = (float) x;
            this.y = (float) y;
            this.width = (float) w;
            this.height = (float) h;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D getBounds2D() {
            return new Rectangle2D.Float(x, y, width, height);
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = -6633761252372475977L;
    }

    /**
     * <code>Double</code> 类定义了一个以 <code>double</code> 精度指定的椭圆。
     * @since 1.2
     */
    public static class Double extends Ellipse2D implements Serializable {
        /**
         * 此 <code>Ellipse2D</code> 的框架矩形的左上角的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double x;

        /**
         * 此 <code>Ellipse2D</code> 的框架矩形的左上角的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double y;

        /**
         * 此 <code>Ellipse2D</code> 的总宽度。
         * @since 1.2
         * @serial
         */
        public double width;

        /**
         * 此 <code>Ellipse2D</code> 的总高度。
         * @since 1.2
         * @serial
         */
        public double height;

        /**
         * 构造一个新的 <code>Ellipse2D</code>，初始化为位置 (0,&nbsp;0) 和大小 (0,&nbsp;0)。
         * @since 1.2
         */
        public Double() {
        }

        /**
         * 从指定的坐标构造并初始化一个 <code>Ellipse2D</code>。
         *
         * @param x 框架矩形的左上角的 X 坐标
         * @param y 框架矩形的左上角的 Y 坐标
         * @param w 框架矩形的宽度
         * @param h 框架矩形的高度
         * @since 1.2
         */
        public Double(double x, double y, double w, double h) {
            setFrame(x, y, w, h);
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
         * {@inheritDoc}
         * @since 1.2
         */
        public boolean isEmpty() {
            return (width <= 0.0 || height <= 0.0);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setFrame(double x, double y, double w, double h) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D getBounds2D() {
            return new Rectangle2D.Double(x, y, width, height);
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = 5555464816372320683L;
    }

    /**
     * 这是一个抽象类，不能直接实例化。
     * 提供了类型特定的实现子类，可以实例化并提供多种格式来存储
     * 满足各种访问方法所需的信息。
     *
     * @see java.awt.geom.Ellipse2D.Float
     * @see java.awt.geom.Ellipse2D.Double
     * @since 1.2
     */
    protected Ellipse2D() {
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(double x, double y) {
        // 将坐标归一化为椭圆中心在 (0,0) 且半径为 0.5 的椭圆
        double ellw = getWidth();
        if (ellw <= 0.0) {
            return false;
        }
        double normx = (x - getX()) / ellw - 0.5;
        double ellh = getHeight();
        if (ellh <= 0.0) {
            return false;
        }
        double normy = (y - getY()) / ellh - 0.5;
        return (normx * normx + normy * normy) < 0.25;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean intersects(double x, double y, double w, double h) {
        if (w <= 0.0 || h <= 0.0) {
            return false;
        }
        // 将矩形坐标归一化为椭圆中心在 (0,0) 且半径为 0.5 的椭圆
        double ellw = getWidth();
        if (ellw <= 0.0) {
            return false;
        }
        double normx0 = (x - getX()) / ellw - 0.5;
        double normx1 = normx0 + w / ellw;
        double ellh = getHeight();
        if (ellh <= 0.0) {
            return false;
        }
        double normy0 = (y - getY()) / ellh - 0.5;
        double normy1 = normy0 + h / ellh;
        // 找到最近的 x (左边缘，右边缘，0.0)
        // 找到最近的 y (上边缘，下边缘，0.0)
        // 如果最近的 x, y 在半径为 0.5 的圆内，则相交
        double nearx, neary;
        if (normx0 > 0.0) {
            // 中心在 X 范围的左侧
            nearx = normx0;
        } else if (normx1 < 0.0) {
            // 中心在 X 范围的右侧
            nearx = normx1;
        } else {
            nearx = 0.0;
        }
        if (normy0 > 0.0) {
            // 中心在 Y 范围的上方
            neary = normy0;
        } else if (normy1 < 0.0) {
            // 中心在 Y 范围的下方
            neary = normy1;
        } else {
            neary = 0.0;
        }
        return (nearx * nearx + neary * neary) < 0.25;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(double x, double y, double w, double h) {
        return (contains(x, y) &&
                contains(x + w, y) &&
                contains(x, y + h) &&
                contains(x + w, y + h));
    }

    /**
     * 返回一个定义此 <code>Ellipse2D</code> 边界的迭代对象。
     * 该类的迭代器是多线程安全的，这意味着
     * 对此 <code>Ellipse2D</code> 几何形状的修改不会影响任何已经进行的迭代。
     * @param at 一个可选的 <code>AffineTransform</code>，用于在迭代时应用到返回的坐标，
     * 或 <code>null</code> 表示不需要转换的坐标
     * @return 返回一个 <code>PathIterator</code> 对象，该对象一次返回此 <code>Ellipse2D</code> 轮廓的几何形状。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at) {
        return new EllipseIterator(this, at);
    }

    /**
     * 返回此 <code>Ellipse2D</code> 的哈希码。
     * @return 此 <code>Ellipse2D</code> 的哈希码。
     * @since 1.6
     */
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits += java.lang.Double.doubleToLongBits(getY()) * 37;
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * 确定指定的 <code>Object</code> 是否等于此 <code>Ellipse2D</code>。
     * 指定的 <code>Object</code> 与此 <code>Ellipse2D</code> 相等
     * 如果它是一个 <code>Ellipse2D</code> 实例，并且其位置和大小与此 <code>Ellipse2D</code> 相同。
     * @param obj 要与此 <code>Ellipse2D</code> 比较的 <code>Object</code>。
     * @return 如果 <code>obj</code> 是一个 <code>Ellipse2D</code> 实例且具有相同的值，则返回 <code>true</code>；
     * 否则返回 <code>false</code>。
     * @since 1.6
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Ellipse2D) {
            Ellipse2D e2d = (Ellipse2D) obj;
            return ((getX() == e2d.getX()) &&
                    (getY() == e2d.getY()) &&
                    (getWidth() == e2d.getWidth()) &&
                    (getHeight() == e2d.getHeight()));
        }
        return false;
    }
}
