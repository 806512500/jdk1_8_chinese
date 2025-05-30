
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
import java.util.Arrays;
import java.io.Serializable;
import sun.awt.geom.Curve;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.ulp;

/**
 * <code>CubicCurve2D</code> 类定义了一个在 {@code (x,y)} 坐标空间中的三次参数曲线段。
 * <p>
 * 该类是所有存储 2D 三次曲线段对象的抽象超类。
 * 实际的坐标存储表示由子类决定。
 *
 * @author      Jim Graham
 * @since 1.2
 */
public abstract class CubicCurve2D implements Shape, Cloneable {

    /**
     * 用 {@code float} 坐标指定的三次参数曲线段。
     * @since 1.2
     */
    public static class Float extends CubicCurve2D implements Serializable {
        /**
         * 三次曲线段的起点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float x1;

        /**
         * 三次曲线段的起点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float y1;

        /**
         * 三次曲线段的第一个控制点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float ctrlx1;

        /**
         * 三次曲线段的第一个控制点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float ctrly1;

        /**
         * 三次曲线段的第二个控制点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float ctrlx2;

        /**
         * 三次曲线段的第二个控制点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float ctrly2;

        /**
         * 三次曲线段的终点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float x2;

        /**
         * 三次曲线段的终点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float y2;

        /**
         * 构造并初始化一个坐标为 (0, 0, 0, 0, 0, 0, 0, 0) 的三次曲线。
         * @since 1.2
         */
        public Float() {
        }

        /**
         * 从指定的 {@code float} 坐标构造并初始化一个 {@code CubicCurve2D}。
         *
         * @param x1 三次曲线段的起点的 X 坐标
         * @param y1 三次曲线段的起点的 Y 坐标
         * @param ctrlx1 三次曲线段的第一个控制点的 X 坐标
         * @param ctrly1 三次曲线段的第一个控制点的 Y 坐标
         * @param ctrlx2 三次曲线段的第二个控制点的 X 坐标
         * @param ctrly2 三次曲线段的第二个控制点的 Y 坐标
         * @param x2 三次曲线段的终点的 X 坐标
         * @param y2 三次曲线段的终点的 Y 坐标
         * @since 1.2
         */
        public Float(float x1, float y1,
                     float ctrlx1, float ctrly1,
                     float ctrlx2, float ctrly2,
                     float x2, float y2)
        {
            setCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getX1() {
            return (double) x1;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getY1() {
            return (double) y1;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Point2D getP1() {
            return new Point2D.Float(x1, y1);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getCtrlX1() {
            return (double) ctrlx1;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getCtrlY1() {
            return (double) ctrly1;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Point2D getCtrlP1() {
            return new Point2D.Float(ctrlx1, ctrly1);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getCtrlX2() {
            return (double) ctrlx2;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getCtrlY2() {
            return (double) ctrly2;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Point2D getCtrlP2() {
            return new Point2D.Float(ctrlx2, ctrly2);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getX2() {
            return (double) x2;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getY2() {
            return (double) y2;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Point2D getP2() {
            return new Point2D.Float(x2, y2);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setCurve(double x1, double y1,
                             double ctrlx1, double ctrly1,
                             double ctrlx2, double ctrly2,
                             double x2, double y2)
        {
            this.x1     = (float) x1;
            this.y1     = (float) y1;
            this.ctrlx1 = (float) ctrlx1;
            this.ctrly1 = (float) ctrly1;
            this.ctrlx2 = (float) ctrlx2;
            this.ctrly2 = (float) ctrly2;
            this.x2     = (float) x2;
            this.y2     = (float) y2;
        }

        /**
         * 设置此曲线的端点和控制点的位置为指定的 {@code float} 坐标。
         *
         * @param x1 用于设置此 {@code CubicCurve2D} 的起点的 X 坐标
         * @param y1 用于设置此 {@code CubicCurve2D} 的起点的 Y 坐标
         * @param ctrlx1 用于设置此 {@code CubicCurve2D} 的第一个控制点的 X 坐标
         * @param ctrly1 用于设置此 {@code CubicCurve2D} 的第一个控制点的 Y 坐标
         * @param ctrlx2 用于设置此 {@code CubicCurve2D} 的第二个控制点的 X 坐标
         * @param ctrly2 用于设置此 {@code CubicCurve2D} 的第二个控制点的 Y 坐标
         * @param x2 用于设置此 {@code CubicCurve2D} 的终点的 X 坐标
         * @param y2 用于设置此 {@code CubicCurve2D} 的终点的 Y 坐标
         * @since 1.2
         */
        public void setCurve(float x1, float y1,
                             float ctrlx1, float ctrly1,
                             float ctrlx2, float ctrly2,
                             float x2, float y2)
        {
            this.x1     = x1;
            this.y1     = y1;
            this.ctrlx1 = ctrlx1;
            this.ctrly1 = ctrly1;
            this.ctrlx2 = ctrlx2;
            this.ctrly2 = ctrly2;
            this.x2     = x2;
            this.y2     = y2;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D getBounds2D() {
            float left   = Math.min(Math.min(x1, x2),
                                    Math.min(ctrlx1, ctrlx2));
            float top    = Math.min(Math.min(y1, y2),
                                    Math.min(ctrly1, ctrly2));
            float right  = Math.max(Math.max(x1, x2),
                                    Math.max(ctrlx1, ctrlx2));
            float bottom = Math.max(Math.max(y1, y2),
                                    Math.max(ctrly1, ctrly2));
            return new Rectangle2D.Float(left, top,
                                         right - left, bottom - top);
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = -1272015596714244385L;
    }

    /**
     * 用 {@code double} 坐标指定的三次参数曲线段。
     * @since 1.2
     */
    public static class Double extends CubicCurve2D implements Serializable {
        /**
         * 三次曲线段的起点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double x1;

        /**
         * 三次曲线段的起点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double y1;

        /**
         * 三次曲线段的第一个控制点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double ctrlx1;

        /**
         * 三次曲线段的第一个控制点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double ctrly1;

        /**
         * 三次曲线段的第二个控制点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double ctrlx2;

        /**
         * 三次曲线段的第二个控制点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double ctrly2;

        /**
         * 三次曲线段的终点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double x2;

        /**
         * 三次曲线段的终点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double y2;

        /**
         * 构造并初始化一个坐标为 (0, 0, 0, 0, 0, 0, 0, 0) 的三次曲线。
         * @since 1.2
         */
        public Double() {
        }

        /**
         * 从指定的 {@code double} 坐标构造并初始化一个 {@code CubicCurve2D}。
         *
         * @param x1 三次曲线段的起点的 X 坐标
         * @param y1 三次曲线段的起点的 Y 坐标
         * @param ctrlx1 三次曲线段的第一个控制点的 X 坐标
         * @param ctrly1 三次曲线段的第一个控制点的 Y 坐标
         * @param ctrlx2 三次曲线段的第二个控制点的 X 坐标
         * @param ctrly2 三次曲线段的第二个控制点的 Y 坐标
         * @param x2 三次曲线段的终点的 X 坐标
         * @param y2 三次曲线段的终点的 Y 坐标
         * @since 1.2
         */
        public Double(double x1, double y1,
                      double ctrlx1, double ctrly1,
                      double ctrlx2, double ctrly2,
                      double x2, double y2)
        {
            setCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getX1() {
            return x1;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getY1() {
            return y1;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Point2D getP1() {
            return new Point2D.Double(x1, y1);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getCtrlX1() {
            return ctrlx1;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getCtrlY1() {
            return ctrly1;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Point2D getCtrlP1() {
            return new Point2D.Double(ctrlx1, ctrly1);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getCtrlX2() {
            return ctrlx2;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getCtrlY2() {
            return ctrly2;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Point2D getCtrlP2() {
            return new Point2D.Double(ctrlx2, ctrly2);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getX2() {
            return x2;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getY2() {
            return y2;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Point2D getP2() {
            return new Point2D.Double(x2, y2);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setCurve(double x1, double y1,
                             double ctrlx1, double ctrly1,
                             double ctrlx2, double ctrly2,
                             double x2, double y2)
        {
            this.x1     = x1;
            this.y1     = y1;
            this.ctrlx1 = ctrlx1;
            this.ctrly1 = ctrly1;
            this.ctrlx2 = ctrlx2;
            this.ctrly2 = ctrly2;
            this.x2     = x2;
            this.y2     = y2;
        }


                    /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D getBounds2D() {
            double left   = Math.min(Math.min(x1, x2),
                                     Math.min(ctrlx1, ctrlx2));
            double top    = Math.min(Math.min(y1, y2),
                                     Math.min(ctrly1, ctrly2));
            double right  = Math.max(Math.max(x1, x2),
                                     Math.max(ctrlx1, ctrlx2));
            double bottom = Math.max(Math.max(y1, y2),
                                     Math.max(ctrly1, ctrly2));
            return new Rectangle2D.Double(left, top,
                                          right - left, bottom - top);
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = -4202960122839707295L;
    }

    /**
     * 这是一个抽象类，不能直接实例化。
     * 提供了类型特定的实现子类，可以实例化并提供多种格式来存储
     * 满足以下各种访问方法所需的信息。
     *
     * @see java.awt.geom.CubicCurve2D.Float
     * @see java.awt.geom.CubicCurve2D.Double
     * @since 1.2
     */
    protected CubicCurve2D() {
    }

    /**
     * 返回起点的 X 坐标，双精度。
     * @return 起点的 X 坐标。
     * @since 1.2
     */
    public abstract double getX1();

    /**
     * 返回起点的 Y 坐标，双精度。
     * @return 起点的 Y 坐标。
     * @since 1.2
     */
    public abstract double getY1();

    /**
     * 返回起点。
     * @return 一个 {@code Point2D}，表示起点。
     * @since 1.2
     */
    public abstract Point2D getP1();

    /**
     * 返回第一个控制点的 X 坐标，双精度。
     * @return 第一个控制点的 X 坐标。
     * @since 1.2
     */
    public abstract double getCtrlX1();

    /**
     * 返回第一个控制点的 Y 坐标，双精度。
     * @return 第一个控制点的 Y 坐标。
     * @since 1.2
     */
    public abstract double getCtrlY1();

    /**
     * 返回第一个控制点。
     * @return 一个 {@code Point2D}，表示第一个控制点。
     * @since 1.2
     */
    public abstract Point2D getCtrlP1();

    /**
     * 返回第二个控制点的 X 坐标，双精度。
     * @return 第二个控制点的 X 坐标。
     * @since 1.2
     */
    public abstract double getCtrlX2();

    /**
     * 返回第二个控制点的 Y 坐标，双精度。
     * @return 第二个控制点的 Y 坐标。
     * @since 1.2
     */
    public abstract double getCtrlY2();

    /**
     * 返回第二个控制点。
     * @return 一个 {@code Point2D}，表示第二个控制点。
     * @since 1.2
     */
    public abstract Point2D getCtrlP2();

    /**
     * 返回终点的 X 坐标，双精度。
     * @return 终点的 X 坐标。
     * @since 1.2
     */
    public abstract double getX2();

    /**
     * 返回终点的 Y 坐标，双精度。
     * @return 终点的 Y 坐标。
     * @since 1.2
     */
    public abstract double getY2();

    /**
     * 返回终点。
     * @return 一个 {@code Point2D}，表示终点。
     * @since 1.2
     */
    public abstract Point2D getP2();

    /**
     * 将此曲线的端点和控制点的位置设置为指定的双精度坐标。
     *
     * @param x1 用于设置此 {@code CubicCurve2D} 起点的 X 坐标
     * @param y1 用于设置此 {@code CubicCurve2D} 起点的 Y 坐标
     * @param ctrlx1 用于设置此 {@code CubicCurve2D} 第一个控制点的 X 坐标
     * @param ctrly1 用于设置此 {@code CubicCurve2D} 第一个控制点的 Y 坐标
     * @param ctrlx2 用于设置此 {@code CubicCurve2D} 第二个控制点的 X 坐标
     * @param ctrly2 用于设置此 {@code CubicCurve2D} 第二个控制点的 Y 坐标
     * @param x2 用于设置此 {@code CubicCurve2D} 终点的 X 坐标
     * @param y2 用于设置此 {@code CubicCurve2D} 终点的 Y 坐标
     * @since 1.2
     */
    public abstract void setCurve(double x1, double y1,
                                  double ctrlx1, double ctrly1,
                                  double ctrlx2, double ctrly2,
                                  double x2, double y2);

    /**
     * 将此曲线的端点和控制点的位置设置为指定数组中指定偏移处的双精度坐标。
     * @param coords 包含坐标的双精度数组
     * @param offset 从 {@code coords} 中开始设置此曲线的端点和控制点的索引
     * @since 1.2
     */
    public void setCurve(double[] coords, int offset) {
        setCurve(coords[offset + 0], coords[offset + 1],
                 coords[offset + 2], coords[offset + 3],
                 coords[offset + 4], coords[offset + 5],
                 coords[offset + 6], coords[offset + 7]);
    }

    /**
     * 将此曲线的端点和控制点的位置设置为指定的 {@code Point2D} 坐标。
     * @param p1 用于设置此曲线起点的第一个指定的 {@code Point2D}
     * @param cp1 用于设置此曲线第一个控制点的第二个指定的 {@code Point2D}
     * @param cp2 用于设置此曲线第二个控制点的第三个指定的 {@code Point2D}
     * @param p2 用于设置此曲线终点的第四个指定的 {@code Point2D}
     * @since 1.2
     */
    public void setCurve(Point2D p1, Point2D cp1, Point2D cp2, Point2D p2) {
        setCurve(p1.getX(), p1.getY(), cp1.getX(), cp1.getY(),
                 cp2.getX(), cp2.getY(), p2.getX(), p2.getY());
    }

    /**
     * 将此曲线的端点和控制点的位置设置为指定数组中指定偏移处的 {@code Point2D} 对象的坐标。
     * @param pts 一个 {@code Point2D} 对象数组
     * @param offset 从 {@code pts} 中开始设置此曲线的端点和控制点的索引
     * @since 1.2
     */
    public void setCurve(Point2D[] pts, int offset) {
        setCurve(pts[offset + 0].getX(), pts[offset + 0].getY(),
                 pts[offset + 1].getX(), pts[offset + 1].getY(),
                 pts[offset + 2].getX(), pts[offset + 2].getY(),
                 pts[offset + 3].getX(), pts[offset + 3].getY());
    }

    /**
     * 将此曲线的端点和控制点的位置设置为指定的 {@code CubicCurve2D} 的相同位置。
     * @param c 指定的 {@code CubicCurve2D}
     * @since 1.2
     */
    public void setCurve(CubicCurve2D c) {
        setCurve(c.getX1(), c.getY1(), c.getCtrlX1(), c.getCtrlY1(),
                 c.getCtrlX2(), c.getCtrlY2(), c.getX2(), c.getY2());
    }

    /**
     * 返回由指定控制点定义的三次曲线的平坦度的平方。平坦度是控制点与连接端点的线之间的最大距离。
     *
     * @param x1 指定 {@code CubicCurve2D} 的起点的 X 坐标
     * @param y1 指定 {@code CubicCurve2D} 的起点的 Y 坐标
     * @param ctrlx1 指定 {@code CubicCurve2D} 的第一个控制点的 X 坐标
     * @param ctrly1 指定 {@code CubicCurve2D} 的第一个控制点的 Y 坐标
     * @param ctrlx2 指定 {@code CubicCurve2D} 的第二个控制点的 X 坐标
     * @param ctrly2 指定 {@code CubicCurve2D} 的第二个控制点的 Y 坐标
     * @param x2 指定 {@code CubicCurve2D} 的终点的 X 坐标
     * @param y2 指定 {@code CubicCurve2D} 的终点的 Y 坐标
     * @return 由指定坐标表示的 {@code CubicCurve2D} 的平坦度的平方。
     * @since 1.2
     */
    public static double getFlatnessSq(double x1, double y1,
                                       double ctrlx1, double ctrly1,
                                       double ctrlx2, double ctrly2,
                                       double x2, double y2) {
        return Math.max(Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx1, ctrly1),
                        Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx2, ctrly2));

    }

    /**
     * 返回由指定控制点定义的三次曲线的平坦度。平坦度是控制点与连接端点的线之间的最大距离。
     *
     * @param x1 指定 {@code CubicCurve2D} 的起点的 X 坐标
     * @param y1 指定 {@code CubicCurve2D} 的起点的 Y 坐标
     * @param ctrlx1 指定 {@code CubicCurve2D} 的第一个控制点的 X 坐标
     * @param ctrly1 指定 {@code CubicCurve2D} 的第一个控制点的 Y 坐标
     * @param ctrlx2 指定 {@code CubicCurve2D} 的第二个控制点的 X 坐标
     * @param ctrly2 指定 {@code CubicCurve2D} 的第二个控制点的 Y 坐标
     * @param x2 指定 {@code CubicCurve2D} 的终点的 X 坐标
     * @param y2 指定 {@code CubicCurve2D} 的终点的 Y 坐标
     * @return 由指定坐标表示的 {@code CubicCurve2D} 的平坦度。
     * @since 1.2
     */
    public static double getFlatness(double x1, double y1,
                                     double ctrlx1, double ctrly1,
                                     double ctrlx2, double ctrly2,
                                     double x2, double y2) {
        return Math.sqrt(getFlatnessSq(x1, y1, ctrlx1, ctrly1,
                                       ctrlx2, ctrly2, x2, y2));
    }

    /**
     * 返回由指定数组中指定索引处的控制点定义的三次曲线的平坦度的平方。平坦度是控制点与连接端点的线之间的最大距离。
     * @param coords 包含坐标的数组
     * @param offset 从 {@code coords} 中开始获取曲线的端点和控制点的索引
     * @return 由 {@code coords} 中指定偏移处的坐标表示的 {@code CubicCurve2D} 的平坦度的平方。
     * @since 1.2
     */
    public static double getFlatnessSq(double coords[], int offset) {
        return getFlatnessSq(coords[offset + 0], coords[offset + 1],
                             coords[offset + 2], coords[offset + 3],
                             coords[offset + 4], coords[offset + 5],
                             coords[offset + 6], coords[offset + 7]);
    }

    /**
     * 返回由指定数组中指定索引处的控制点定义的三次曲线的平坦度。平坦度是控制点与连接端点的线之间的最大距离。
     * @param coords 包含坐标的数组
     * @param offset 从 {@code coords} 中开始获取曲线的端点和控制点的索引
     * @return 由 {@code coords} 中指定偏移处的坐标表示的 {@code CubicCurve2D} 的平坦度。
     * @since 1.2
     */
    public static double getFlatness(double coords[], int offset) {
        return getFlatness(coords[offset + 0], coords[offset + 1],
                           coords[offset + 2], coords[offset + 3],
                           coords[offset + 4], coords[offset + 5],
                           coords[offset + 6], coords[offset + 7]);
    }

    /**
     * 返回此曲线的平坦度的平方。平坦度是控制点与连接端点的线之间的最大距离。
     * @return 此曲线的平坦度的平方。
     * @since 1.2
     */
    public double getFlatnessSq() {
        return getFlatnessSq(getX1(), getY1(), getCtrlX1(), getCtrlY1(),
                             getCtrlX2(), getCtrlY2(), getX2(), getY2());
    }

    /**
     * 返回此曲线的平坦度。平坦度是控制点与连接端点的线之间的最大距离。
     * @return 此曲线的平坦度。
     * @since 1.2
     */
    public double getFlatness() {
        return getFlatness(getX1(), getY1(), getCtrlX1(), getCtrlY1(),
                           getCtrlX2(), getCtrlY2(), getX2(), getY2());
    }

    /**
     * 将此三次曲线细分，并将结果的两个细分曲线存储到 left 和 right 参数中。
     * left 和 right 对象中的一个或两个可以是此对象或 null。
     * @param left 用于存储细分曲线的左半部分或前半部分的三次曲线对象
     * @param right 用于存储细分曲线的右半部分或后半部分的三次曲线对象
     * @since 1.2
     */
    public void subdivide(CubicCurve2D left, CubicCurve2D right) {
        subdivide(this, left, right);
    }

    /**
     * 将由 src 参数指定的三次曲线细分，并将结果的两个细分曲线存储到 left 和 right 参数中。
     * left 和 right 对象中的一个或两个可以是 src 对象或 null。
     * @param src 要细分的三次曲线
     * @param left 用于存储细分曲线的左半部分或前半部分的三次曲线对象
     * @param right 用于存储细分曲线的右半部分或后半部分的三次曲线对象
     * @since 1.2
     */
    public static void subdivide(CubicCurve2D src,
                                 CubicCurve2D left,
                                 CubicCurve2D right) {
        double x1 = src.getX1();
        double y1 = src.getY1();
        double ctrlx1 = src.getCtrlX1();
        double ctrly1 = src.getCtrlY1();
        double ctrlx2 = src.getCtrlX2();
        double ctrly2 = src.getCtrlY2();
        double x2 = src.getX2();
        double y2 = src.getY2();
        double centerx = (ctrlx1 + ctrlx2) / 2.0;
        double centery = (ctrly1 + ctrly2) / 2.0;
        ctrlx1 = (x1 + ctrlx1) / 2.0;
        ctrly1 = (y1 + ctrly1) / 2.0;
        ctrlx2 = (x2 + ctrlx2) / 2.0;
        ctrly2 = (y2 + ctrly2) / 2.0;
        double ctrlx12 = (ctrlx1 + centerx) / 2.0;
        double ctrly12 = (ctrly1 + centery) / 2.0;
        double ctrlx21 = (ctrlx2 + centerx) / 2.0;
        double ctrly21 = (ctrly2 + centery) / 2.0;
        centerx = (ctrlx12 + ctrlx21) / 2.0;
        centery = (ctrly12 + ctrly21) / 2.0;
        if (left != null) {
            left.setCurve(x1, y1, ctrlx1, ctrly1,
                          ctrlx12, ctrly12, centerx, centery);
        }
        if (right != null) {
            right.setCurve(centerx, centery, ctrlx21, ctrly21,
                           ctrlx2, ctrly2, x2, y2);
        }
    }


                /**
     * 将由 <code>src</code> 数组中索引 <code>srcoff</code>
     * 到 (<code>srcoff</code>&nbsp;+&nbsp;7) 存储的三次曲线细分，并将
     * 生成的两个细分曲线的结果存储到两个结果数组中相应的索引位置。
     * <code>left</code> 和 <code>right</code> 数组中的任何一个或两个都可能是
     * <code>null</code> 或与 <code>src</code> 数组相同的引用。
     * 请注意，第一个细分曲线的最后一个点与第二个细分曲线的第一个点相同。因此，
     * 可以将相同的数组传递给 <code>left</code> 和 <code>right</code>，并使用偏移量，
     * 例如 <code>rightoff</code> 等于 (<code>leftoff</code> + 6)，以避免为这个公共点分配额外的存储空间。
     * @param src 用于存储源曲线坐标的数组
     * @param srcoff 源坐标开始的数组偏移量
     * @param left 用于存储第一个细分曲线坐标的数组
     * @param leftoff 第一个细分曲线坐标开始的数组偏移量
     * @param right 用于存储第二个细分曲线坐标的数组
     * @param rightoff 第二个细分曲线坐标开始的数组偏移量
     * @since 1.2
     */
    public static void subdivide(double src[], int srcoff,
                                 double left[], int leftoff,
                                 double right[], int rightoff) {
        double x1 = src[srcoff + 0];
        double y1 = src[srcoff + 1];
        double ctrlx1 = src[srcoff + 2];
        double ctrly1 = src[srcoff + 3];
        double ctrlx2 = src[srcoff + 4];
        double ctrly2 = src[srcoff + 5];
        double x2 = src[srcoff + 6];
        double y2 = src[srcoff + 7];
        if (left != null) {
            left[leftoff + 0] = x1;
            left[leftoff + 1] = y1;
        }
        if (right != null) {
            right[rightoff + 6] = x2;
            right[rightoff + 7] = y2;
        }
        x1 = (x1 + ctrlx1) / 2.0;
        y1 = (y1 + ctrly1) / 2.0;
        x2 = (x2 + ctrlx2) / 2.0;
        y2 = (y2 + ctrly2) / 2.0;
        double centerx = (ctrlx1 + ctrlx2) / 2.0;
        double centery = (ctrly1 + ctrly2) / 2.0;
        ctrlx1 = (x1 + centerx) / 2.0;
        ctrly1 = (y1 + centery) / 2.0;
        ctrlx2 = (x2 + centerx) / 2.0;
        ctrly2 = (y2 + centery) / 2.0;
        centerx = (ctrlx1 + ctrlx2) / 2.0;
        centery = (ctrly1 + ctrly2) / 2.0;
        if (left != null) {
            left[leftoff + 2] = x1;
            left[leftoff + 3] = y1;
            left[leftoff + 4] = ctrlx1;
            left[leftoff + 5] = ctrly1;
            left[leftoff + 6] = centerx;
            left[leftoff + 7] = centery;
        }
        if (right != null) {
            right[rightoff + 0] = centerx;
            right[rightoff + 1] = centery;
            right[rightoff + 2] = ctrlx2;
            right[rightoff + 3] = ctrly2;
            right[rightoff + 4] = x2;
            right[rightoff + 5] = y2;
        }
    }

    /**
     * 解决系数在 <code>eqn</code> 数组中的三次方程，并将非复数根放回同一个数组中，
     * 返回根的数量。解决的三次方程表示为：
     * <pre>
     *     eqn = {c, b, a, d}
     *     dx^3 + ax^2 + bx + c = 0
     * </pre>
     * 返回值 -1 用于区分可能是始终为 0 或永远不为 0 的常数方程与没有零点的方程。
     * @param eqn 包含三次方程系数的数组
     * @return 根的数量，或 -1 如果方程是常数。
     * @since 1.2
     */
    public static int solveCubic(double eqn[]) {
        return solveCubic(eqn, eqn);
    }

    /**
     * 解决系数在 <code>eqn</code> 数组中的三次方程，并将非复数根放入 <code>res</code>
     * 数组中，返回根的数量。
     * 三次方程表示为：
     *     eqn = {c, b, a, d}
     *     dx^3 + ax^2 + bx + c = 0
     * 返回值 -1 用于区分可能是始终为 0 或永远不为 0 的常数方程与没有零点的方程。
     * @param eqn 用于解决三次方程的指定系数数组
     * @param res 用于存储三次方程解的非复数根的数组
     * @return 根的数量，或 -1 如果方程是常数
     * @since 1.3
     */
    public static int solveCubic(double eqn[], double res[]) {
        // 从 Graphics Gems:
        // http://tog.acm.org/resources/GraphicsGems/gems/Roots3And4.c
        final double d = eqn[3];
        if (d == 0) {
            return QuadCurve2D.solveQuadratic(eqn, res);
        }

        /* 标准形式: x^3 + Ax^2 + Bx + C = 0 */
        final double A = eqn[2] / d;
        final double B = eqn[1] / d;
        final double C = eqn[0] / d;

        // 用 x = y - A/3 替换以消除二次项：
        //     x^3 +Px + Q = 0
        //
        // 由于在下面的所有计算中实际上需要 P/3 和 Q/2，我们将计算
        // p = P/3
        // q = Q/2
        // 以简化代码。
        double sq_A = A * A;
        double p = 1.0/3 * (-1.0/3 * sq_A + B);
        double q = 1.0/2 * (2.0/27 * A * sq_A - 1.0/3 * A * B + C);

        /* 使用卡丹公式 */

        double cb_p = p * p * p;
        double D = q * q + cb_p;

        final double sub = 1.0/3 * A;

        int num;
        if (D < 0) { /* 不可约情况：三个实根 */
            // 参见: http://en.wikipedia.org/wiki/Cubic_function#Trigonometric_.28and_hyperbolic.29_method
            double phi = 1.0/3 * Math.acos(-q / Math.sqrt(-cb_p));
            double t = 2 * Math.sqrt(-p);

            if (res == eqn) {
                eqn = Arrays.copyOf(eqn, 4);
            }

            res[ 0 ] =  ( t * Math.cos(phi));
            res[ 1 ] =  (-t * Math.cos(phi + Math.PI / 3));
            res[ 2 ] =  (-t * Math.cos(phi - Math.PI / 3));
            num = 3;

            for (int i = 0; i < num; ++i) {
                res[ i ] -= sub;
            }

        } else {
            // 请在标记为 'XXX' 的 fixRoots 评论中更改此情况下的任何代码。
            double sqrt_D = Math.sqrt(D);
            double u = Math.cbrt(sqrt_D - q);
            double v = - Math.cbrt(sqrt_D + q);
            double uv = u+v;

            num = 1;

            double err = 1200000000*ulp(abs(uv) + abs(sub));
            if (iszero(D, err) || within(u, v, err)) {
                if (res == eqn) {
                    eqn = Arrays.copyOf(eqn, 4);
                }
                res[1] = -(uv / 2) - sub;
                num = 2;
            }
            // 这必须在潜在的 Arrays.copyOf 之后完成
            res[ 0 ] =  uv - sub;
        }

        if (num > 1) { // num == 3 || num == 2
            num = fixRoots(eqn, res, num);
        }
        if (num > 2 && (res[2] == res[1] || res[2] == res[0])) {
            num--;
        }
        if (num > 1 && res[1] == res[0]) {
            res[1] = res[--num]; // 如果需要，将 res[2] 复制到 res[1]
        }
        return num;
    }

    // 前提条件: eqn != res && eqn[3] != 0 && num > 1
    // 此方法尝试提高 eqn 的根的精度（这些根应该在 res 中）。它还可能从 res 中消除根，如果它决定这些根不是实根。它不会检查 res 的计算可能遗漏的根，因此此方法仅应在使用从不低估根数的算法（如上面的 solveCubic）计算 res 中的根时使用。
    private static int fixRoots(double[] eqn, double[] res, int num) {
        double[] intervals = {eqn[1], 2*eqn[2], 3*eqn[3]};
        int critCount = QuadCurve2D.solveQuadratic(intervals, intervals);
        if (critCount == 2 && intervals[0] == intervals[1]) {
            critCount--;
        }
        if (critCount == 2 && intervals[0] > intervals[1]) {
            double tmp = intervals[0];
            intervals[0] = intervals[1];
            intervals[1] = tmp;
        }

        // 下面我们使用 critCount 可能会过滤掉不应该计算的根。我们要求 eqn[3] != 0，所以 eqn 是一个标准的三次方程，这意味着它在 -/+inf 的极限是 -/+inf 或 +/-inf。
        // 因此，如果 critCount==2，曲线的形状像一个侧置的 S，可能有 1-3 个根。如果 critCount==0 它是单调的，如果 critCount==1 它是单调的，但有一个点是平的。在最后两种情况下，只能有一个根。所以在 num > 1 但 critCount < 2 的情况下，我们消除 res 中的所有根，只保留一个。

        if (num == 3) {
            double xe = getRootUpperBound(eqn);
            double x0 = -xe;

            Arrays.sort(res, 0, num);
            if (critCount == 2) {
                // 这只是尝试使用牛顿法提高计算根的精度。
                res[0] = refineRootWithHint(eqn, x0, intervals[0], res[0]);
                res[1] = refineRootWithHint(eqn, intervals[0], intervals[1], res[1]);
                res[2] = refineRootWithHint(eqn, intervals[1], xe, res[2]);
                return 3;
            } else if (critCount == 1) {
                // 我们只需要 fx0 和 fxe 来确定多项式在 -inf 和 +inf 的符号，所以不需要做
                // fx0 = solveEqn(eqn, 3, x0); fxe = solveEqn(eqn, 3, xe)
                double fxe = eqn[3];
                double fx0 = -fxe;

                double x1 = intervals[0];
                double fx1 = solveEqn(eqn, 3, x1);

                // 如果 critCount == 1 或 critCount == 0，但 num == 3，则
                // 出现了问题。这个分支和下面的分支理想情况下永远不会执行，但如果它们确实执行了，我们无法知道
                // 哪个计算的根最接近实际的根；因此，我们不能使用 refineRootWithHint。但即使我们知道，这里
                // 最有可能意味着曲线在两个计算的根附近非常平坦（或者可能是所有三个）。这可能会使牛顿法完全失败，
                // 检测和修复这将非常麻烦。这就是为什么我们使用非常稳定的二分法。
                if (oppositeSigns(fx0, fx1)) {
                    res[0] = bisectRootWithHint(eqn, x0, x1, res[0]);
                } else if (oppositeSigns(fx1, fxe)) {
                    res[0] = bisectRootWithHint(eqn, x1, xe, res[2]);
                } else /* fx1 必须为 0 */ {
                    res[0] = x1;
                }
                // 返回 1
            } else if (critCount == 0) {
                res[0] = bisectRootWithHint(eqn, x0, xe, res[1]);
                // 返回 1
            }
        } else if (num == 2 && critCount == 2) {
            // XXX: 这里我们假设 res[0] 的精度比 res[1] 高。
            // 这是真的，因为此方法仅从 solveCubic 使用
            // 它将无论如何都会计算的根放入 res[0]，即使 num==1。如果此方法从任何其他方法使用，或
            // solveCubic 的实现发生变化，这个假设应该重新评估，goodRoot 的选择可能需要变为
            // goodRoot = (abs(eqn'(res[0])) > abs(eqn'(res[1]))) ? res[0] : res[1]
            // 其中 eqn' 是 eqn 的导数。
            double goodRoot = res[0];
            double badRoot = res[1];
            double x1 = intervals[0];
            double x2 = intervals[1];
            // 如果三次曲线确实有 2 个根，其中一个根必须在临界点。那不能是 goodRoot，所以我们计算 x
            // 为离 goodRoot 最远的临界点。如果有两个根，x 必须是第二个，所以我们评估 eqn 在 x，如果
            // 它为零（或足够接近），我们将 x 放入 res[1]（或如果 |solveEqn(eqn, 3, badRoot)| < |solveEqn(eqn, 3, x)|，则放入 badRoot，但这不常发生）。
            double x = abs(x1 - goodRoot) > abs(x2 - goodRoot) ? x1 : x2;
            double fx = solveEqn(eqn, 3, x);

            if (iszero(fx, 10000000*ulp(x))) {
                double badRootVal = solveEqn(eqn, 3, badRoot);
                res[1] = abs(badRootVal) < abs(fx) ? badRoot : x;
                return 2;
            }
        } // 否则只能有一个根 - goodRoot，它已经在 res[0] 中

        return 1;
    }

    // 使用牛顿法。
    private static double refineRootWithHint(double[] eqn, double min, double max, double t) {
        if (!inInterval(t, min, max)) {
            return t;
        }
        double[] deriv = {eqn[1], 2*eqn[2], 3*eqn[3]};
        double origt = t;
        for (int i = 0; i < 3; i++) {
            double slope = solveEqn(deriv, 2, t);
            double y = solveEqn(eqn, 3, t);
            double delta = - (y / slope);
            double newt = t + delta;

            if (slope == 0 || y == 0 || t == newt) {
                break;
            }

            t = newt;
        }
        if (within(t, origt, 1000*ulp(origt)) && inInterval(t, min, max)) {
            return t;
        }
        return origt;
    }

    private static double bisectRootWithHint(double[] eqn, double x0, double xe, double hint) {
        double delta1 = Math.min(abs(hint - x0) / 64, 0.0625);
        double delta2 = Math.min(abs(hint - xe) / 64, 0.0625);
        double x02 = hint - delta1;
        double xe2 = hint + delta2;
        double fx02 = solveEqn(eqn, 3, x02);
        double fxe2 = solveEqn(eqn, 3, xe2);
        while (oppositeSigns(fx02, fxe2)) {
            if (x02 >= xe2) {
                return x02;
            }
            x0 = x02;
            xe = xe2;
            delta1 /= 64;
            delta2 /= 64;
            x02 = hint - delta1;
            xe2 = hint + delta2;
            fx02 = solveEqn(eqn, 3, x02);
            fxe2 = solveEqn(eqn, 3, xe2);
        }
        if (fx02 == 0) {
            return x02;
        }
        if (fxe2 == 0) {
            return xe2;
        }

        return bisectRoot(eqn, x0, xe);
    }

    private static double bisectRoot(double[] eqn, double x0, double xe) {
        double fx0 = solveEqn(eqn, 3, x0);
        double m = x0 + (xe - x0) / 2;
        while (m != x0 && m != xe) {
            double fm = solveEqn(eqn, 3, m);
            if (fm == 0) {
                return m;
            }
            if (oppositeSigns(fx0, fm)) {
                xe = m;
            } else {
                fx0 = fm;
                x0 = m;
            }
            m = x0 + (xe-x0)/2;
        }
        return m;
    }


                private static boolean inInterval(double t, double min, double max) {
        return min <= t && t <= max;
    }

    private static boolean within(double x, double y, double err) {
        double d = y - x;
        return (d <= err && d >= -err);
    }

    private static boolean iszero(double x, double err) {
        return within(x, 0, err);
    }

    private static boolean oppositeSigns(double x1, double x2) {
        return (x1 < 0 && x2 > 0) || (x1 > 0 && x2 < 0);
    }

    private static double solveEqn(double eqn[], int order, double t) {
        double v = eqn[order];
        while (--order >= 0) {
            v = v * t + eqn[order];
        }
        return v;
    }

    /*
     * 计算 M+1，其中 M 是方程所有根的上界。
     * 参见: http://en.wikipedia.org/wiki/Sturm%27s_theorem#Applications。
     * 上述链接没有包含证明，但我 [dlila] 自己证明了这一点，
     * 因此结果是可靠的。证明并不难，但在这里包含会有点长。
     * 前提条件：eqn 必须表示一个三次多项式
     */
    private static double getRootUpperBound(double[] eqn) {
        double d = eqn[3];
        double a = eqn[2];
        double b = eqn[1];
        double c = eqn[0];

        double M = 1 + max(max(abs(a), abs(b)), abs(c)) / abs(d);
        M += ulp(M) + 1;
        return M;
    }


    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(double x, double y) {
        if (!(x * 0.0 + y * 0.0 == 0.0)) {
            /* x 或 y 为无穷大或 NaN。
             * NaN 在任何测试中总是产生负响应，
             * 无穷大值不能在任何路径内，因此应返回 false。
             */
            return false;
        }
        // 我们计算 "Y" 交叉点以确定点是否在由其闭合线限定的曲线内。
        double x1 = getX1();
        double y1 = getY1();
        double x2 = getX2();
        double y2 = getY2();
        int crossings =
            (Curve.pointCrossingsForLine(x, y, x1, y1, x2, y2) +
             Curve.pointCrossingsForCubic(x, y,
                                          x1, y1,
                                          getCtrlX1(), getCtrlY1(),
                                          getCtrlX2(), getCtrlY2(),
                                          x2, y2, 0));
        return ((crossings & 1) == 1);
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
    public boolean intersects(double x, double y, double w, double h) {
        // 拒绝不存在的矩形
        if (w <= 0 || h <= 0) {
            return false;
        }

        int numCrossings = rectCrossings(x, y, w, h);
        // 预期的返回值是
        // numCrossings != 0 || numCrossings == Curve.RECT_INTERSECTS
        // 但如果 (numCrossings != 0) numCrossings == INTERSECTS 不重要
        // 如果 !(numCrossings != 0) 则 numCrossings == 0，所以
        // numCrossings != RECT_INTERSECT
        return numCrossings != 0;
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
    public boolean contains(double x, double y, double w, double h) {
        if (w <= 0 || h <= 0) {
            return false;
        }

        int numCrossings = rectCrossings(x, y, w, h);
        return !(numCrossings == 0 || numCrossings == Curve.RECT_INTERSECTS);
    }

    private int rectCrossings(double x, double y, double w, double h) {
        int crossings = 0;
        if (!(getX1() == getX2() && getY1() == getY2())) {
            crossings = Curve.rectCrossingsForLine(crossings,
                                                   x, y,
                                                   x+w, y+h,
                                                   getX1(), getY1(),
                                                   getX2(), getY2());
            if (crossings == Curve.RECT_INTERSECTS) {
                return crossings;
            }
        }
        // 我们以曲线的反方向调用此方法，因为我们希望先调用 rectCrossingsForLine，
        // 因为它更便宜。
        return Curve.rectCrossingsForCubic(crossings,
                                           x, y,
                                           x+w, y+h,
                                           getX2(), getY2(),
                                           getCtrlX2(), getCtrlY2(),
                                           getCtrlX1(), getCtrlY1(),
                                           getX1(), getY1(), 0);
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
        return getBounds2D().getBounds();
    }

    /**
     * 返回定义形状边界的迭代对象。
     * 该类的迭代器不是线程安全的，
     * 这意味着此 <code>CubicCurve2D</code> 类不保证对此
     * <code>CubicCurve2D</code> 对象的几何修改不会影响已经进行的几何迭代。
     * @param at 一个可选的 <code>AffineTransform</code>，应用于迭代返回的坐标，或 <code>null</code>
     * 如果需要未变换的坐标
     * @return    返回 <code>CubicCurve2D</code> 轮廓几何的 <code>PathIterator</code> 对象，
     * 一次一个段。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at) {
        return new CubicIterator(this, at);
    }

    /**
     * 返回定义展平形状边界的迭代对象。
     * 该类的迭代器不是线程安全的，
     * 这意味着此 <code>CubicCurve2D</code> 类不保证对此
     * <code>CubicCurve2D</code> 对象的几何修改不会影响已经进行的几何迭代。
     * @param at 一个可选的 <code>AffineTransform</code>，应用于迭代返回的坐标，或 <code>null</code>
     * 如果需要未变换的坐标
     * @param flatness 控制点相对于共线的最大偏差量，超过此值时，将用直线连接端点替换细分曲线
     * @return    返回 <code>CubicCurve2D</code> 轮廓几何的 <code>PathIterator</code> 对象，
     * 一次一个段。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }

    /**
     * 创建与此对象相同类的新对象。
     *
     * @return     此实例的克隆。
     * @exception  OutOfMemoryError            如果内存不足。
     * @see        java.lang.Cloneable
     * @since      1.2
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们实现了 Cloneable
            throw new InternalError(e);
        }
    }
}
