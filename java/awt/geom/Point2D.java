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

import java.io.Serializable;

/**
 * <code>Point2D</code> 类定义了一个表示 {@code (x,y)} 坐标空间中位置的点。
 * <p>
 * 该类仅是所有存储 2D 坐标的对象的抽象超类。
 * 坐标的实际存储表示形式由子类决定。
 *
 * @author      Jim Graham
 * @since 1.2
 */
public abstract class Point2D implements Cloneable {

    /**
     * <code>Float</code> 类定义了一个以浮点精度指定的点。
     * @since 1.2
     */
    public static class Float extends Point2D implements Serializable {
        /**
         * 此 <code>Point2D</code> 的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float x;

        /**
         * 此 <code>Point2D</code> 的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float y;

        /**
         * 构造并初始化一个坐标为 (0,&nbsp;0) 的 <code>Point2D</code>。
         * @since 1.2
         */
        public Float() {
        }

        /**
         * 构造并初始化一个具有指定坐标的 <code>Point2D</code>。
         *
         * @param x 新构造的 <code>Point2D</code> 的 X 坐标
         * @param y 新构造的 <code>Point2D</code> 的 Y 坐标
         * @since 1.2
         */
        public Float(float x, float y) {
            this.x = x;
            this.y = y;
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
        public void setLocation(double x, double y) {
            this.x = (float) x;
            this.y = (float) y;
        }

        /**
         * 将此 <code>Point2D</code> 的位置设置为指定的 <code>float</code> 坐标。
         *
         * @param x 此 {@code Point2D} 的新 X 坐标
         * @param y 此 {@code Point2D} 的新 Y 坐标
         * @since 1.2
         */
        public void setLocation(float x, float y) {
            this.x = x;
            this.y = y;
        }

        /**
         * 返回一个表示此 <code>Point2D</code> 值的 <code>String</code>。
         * @return 此 <code>Point2D</code> 的字符串表示形式。
         * @since 1.2
         */
        public String toString() {
            return "Point2D.Float["+x+", "+y+"]";
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = -2870572449815403710L;
    }

    /**
     * <code>Double</code> 类定义了一个以双精度指定的点。
     * @since 1.2
     */
    public static class Double extends Point2D implements Serializable {
        /**
         * 此 <code>Point2D</code> 的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double x;

        /**
         * 此 <code>Point2D</code> 的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double y;

        /**
         * 构造并初始化一个坐标为 (0,&nbsp;0) 的 <code>Point2D</code>。
         * @since 1.2
         */
        public Double() {
        }

        /**
         * 构造并初始化一个具有指定坐标的 <code>Point2D</code>。
         *
         * @param x 新构造的 <code>Point2D</code> 的 X 坐标
         * @param y 新构造的 <code>Point2D</code> 的 Y 坐标
         * @since 1.2
         */
        public Double(double x, double y) {
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
         * {@inheritDoc}
         * @since 1.2
         */
        public void setLocation(double x, double y) {
            this.x = x;
            this.y = y;
        }

        /**
         * 返回一个表示此 <code>Point2D</code> 值的 <code>String</code>。
         * @return 此 <code>Point2D</code> 的字符串表示形式。
         * @since 1.2
         */
        public String toString() {
            return "Point2D.Double["+x+", "+y+"]";
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = 6150783262733311327L;
    }

    /**
     * 这是一个抽象类，不能直接实例化。
     * 提供了类型特定的实现子类，可以实例化并提供多种格式来存储
     * 满足各种访问方法所需的信息。
     *
     * @see java.awt.geom.Point2D.Float
     * @see java.awt.geom.Point2D.Double
     * @see java.awt.Point
     * @since 1.2
     */
    protected Point2D() {
    }

    /**
     * 返回此 <code>Point2D</code> 的 X 坐标，精度为 <code>double</code>。
     * @return 此 <code>Point2D</code> 的 X 坐标。
     * @since 1.2
     */
    public abstract double getX();

    /**
     * 返回此 <code>Point2D</code> 的 Y 坐标，精度为 <code>double</code>。
     * @return 此 <code>Point2D</code> 的 Y 坐标。
     * @since 1.2
     */
    public abstract double getY();

    /**
     * 将此 <code>Point2D</code> 的位置设置为指定的 <code>double</code> 坐标。
     *
     * @param x 此 {@code Point2D} 的新 X 坐标
     * @param y 此 {@code Point2D} 的新 Y 坐标
     * @since 1.2
     */
    public abstract void setLocation(double x, double y);

    /**
     * 将此 <code>Point2D</code> 的位置设置为指定的 <code>Point2D</code> 对象的相同坐标。
     * @param p 要设置此 <code>Point2D</code> 的指定 <code>Point2D</code>
     * @since 1.2
     */
    public void setLocation(Point2D p) {
        setLocation(p.getX(), p.getY());
    }

    /**
     * 返回两个点之间的距离的平方。
     *
     * @param x1 第一个指定点的 X 坐标
     * @param y1 第一个指定点的 Y 坐标
     * @param x2 第二个指定点的 X 坐标
     * @param y2 第二个指定点的 Y 坐标
     * @return 两个指定坐标集之间的距离的平方。
     * @since 1.2
     */
    public static double distanceSq(double x1, double y1,
                                    double x2, double y2)
    {
        x1 -= x2;
        y1 -= y2;
        return (x1 * x1 + y1 * y1);
    }

    /**
     * 返回两个点之间的距离。
     *
     * @param x1 第一个指定点的 X 坐标
     * @param y1 第一个指定点的 Y 坐标
     * @param x2 第二个指定点的 X 坐标
     * @param y2 第二个指定点的 Y 坐标
     * @return 两个指定坐标集之间的距离。
     * @since 1.2
     */
    public static double distance(double x1, double y1,
                                  double x2, double y2)
    {
        x1 -= x2;
        y1 -= y2;
        return Math.sqrt(x1 * x1 + y1 * y1);
    }

    /**
     * 返回此 <code>Point2D</code> 到指定点的距离的平方。
     *
     * @param px 要测量的指定点的 X 坐标
     * @param py 要测量的指定点的 Y 坐标
     * @return 此 <code>Point2D</code> 到指定点的距离的平方。
     * @since 1.2
     */
    public double distanceSq(double px, double py) {
        px -= getX();
        py -= getY();
        return (px * px + py * py);
    }

    /**
     * 返回此 <code>Point2D</code> 到指定 <code>Point2D</code> 的距离的平方。
     *
     * @param pt 要测量的指定点
     * @return 此 <code>Point2D</code> 到指定 <code>Point2D</code> 的距离的平方。
     * @since 1.2
     */
    public double distanceSq(Point2D pt) {
        double px = pt.getX() - this.getX();
        double py = pt.getY() - this.getY();
        return (px * px + py * py);
    }

    /**
     * 返回此 <code>Point2D</code> 到指定点的距离。
     *
     * @param px 要测量的指定点的 X 坐标
     * @param py 要测量的指定点的 Y 坐标
     * @return 此 <code>Point2D</code> 到指定点的距离。
     * @since 1.2
     */
    public double distance(double px, double py) {
        px -= getX();
        py -= getY();
        return Math.sqrt(px * px + py * py);
    }

    /**
     * 返回此 <code>Point2D</code> 到指定 <code>Point2D</code> 的距离。
     *
     * @param pt 要测量的指定点
     * @return 此 <code>Point2D</code> 到指定 <code>Point2D</code> 的距离。
     * @since 1.2
     */
    public double distance(Point2D pt) {
        double px = pt.getX() - this.getX();
        double py = pt.getY() - this.getY();
        return Math.sqrt(px * px + py * py);
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

    /**
     * 返回此 <code>Point2D</code> 的哈希码。
     * @return 此 <code>Point2D</code> 的哈希码。
     */
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits ^= java.lang.Double.doubleToLongBits(getY()) * 31;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * 确定两个点是否相等。如果两个 <code>Point2D</code> 实例的
     * <code>x</code> 和 <code>y</code> 成员字段值，表示它们在坐标空间中的位置相同，则它们相等。
     * @param obj 要与该 <code>Point2D</code> 比较的对象
     * @return 如果要比较的对象是 <code>Point2D</code> 的实例且具有相同的值，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean equals(Object obj) {
        if (obj instanceof Point2D) {
            Point2D p2d = (Point2D) obj;
            return (getX() == p2d.getX()) && (getY() == p2d.getY());
        }
        return super.equals(obj);
    }
}
