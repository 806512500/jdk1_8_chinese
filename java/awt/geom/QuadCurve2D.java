
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.io.Serializable;
import sun.awt.geom.Curve;

/**
 * <code>QuadCurve2D</code> 类定义了一个在 {@code (x,y)} 坐标空间中的二次参数曲线段。
 * <p>
 * 这个类是所有存储 2D 二次曲线段的对象的抽象超类。
 * 实际的坐标存储表示留给子类。
 *
 * @author      Jim Graham
 * @since 1.2
 */
public abstract class QuadCurve2D implements Shape, Cloneable {

    /**
     * 用 {@code float} 坐标指定的二次参数曲线段。
     *
     * @since 1.2
     */
    public static class Float extends QuadCurve2D implements Serializable {
        /**
         * 二次曲线段的起点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float x1;

        /**
         * 二次曲线段的起点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float y1;

        /**
         * 二次曲线段的控制点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float ctrlx;

        /**
         * 二次曲线段的控制点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float ctrly;

        /**
         * 二次曲线段的终点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float x2;

        /**
         * 二次曲线段的终点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float y2;

        /**
         * 构造并初始化一个 <code>QuadCurve2D</code>，坐标为 (0, 0, 0, 0, 0, 0)。
         * @since 1.2
         */
        public Float() {
        }

        /**
         * 从指定的 {@code float} 坐标构造并初始化一个 <code>QuadCurve2D</code>。
         *
         * @param x1 起点的 X 坐标
         * @param y1 起点的 Y 坐标
         * @param ctrlx 控制点的 X 坐标
         * @param ctrly 控制点的 Y 坐标
         * @param x2 终点的 X 坐标
         * @param y2 终点的 Y 坐标
         * @since 1.2
         */
        public Float(float x1, float y1,
                     float ctrlx, float ctrly,
                     float x2, float y2)
        {
            setCurve(x1, y1, ctrlx, ctrly, x2, y2);
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
        public double getCtrlX() {
            return (double) ctrlx;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getCtrlY() {
            return (double) ctrly;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Point2D getCtrlPt() {
            return new Point2D.Float(ctrlx, ctrly);
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
                             double ctrlx, double ctrly,
                             double x2, double y2)
        {
            this.x1    = (float) x1;
            this.y1    = (float) y1;
            this.ctrlx = (float) ctrlx;
            this.ctrly = (float) ctrly;
            this.x2    = (float) x2;
            this.y2    = (float) y2;
        }

        /**
         * 设置此曲线的端点和控制点的位置为指定的 {@code float} 坐标。
         *
         * @param x1 起点的 X 坐标
         * @param y1 起点的 Y 坐标
         * @param ctrlx 控制点的 X 坐标
         * @param ctrly 控制点的 Y 坐标
         * @param x2 终点的 X 坐标
         * @param y2 终点的 Y 坐标
         * @since 1.2
         */
        public void setCurve(float x1, float y1,
                             float ctrlx, float ctrly,
                             float x2, float y2)
        {
            this.x1    = x1;
            this.y1    = y1;
            this.ctrlx = ctrlx;
            this.ctrly = ctrly;
            this.x2    = x2;
            this.y2    = y2;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D getBounds2D() {
            float left   = Math.min(Math.min(x1, x2), ctrlx);
            float top    = Math.min(Math.min(y1, y2), ctrly);
            float right  = Math.max(Math.max(x1, x2), ctrlx);
            float bottom = Math.max(Math.max(y1, y2), ctrly);
            return new Rectangle2D.Float(left, top,
                                         right - left, bottom - top);
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = -8511188402130719609L;
    }

    /**
     * 用 {@code double} 坐标指定的二次参数曲线段。
     *
     * @since 1.2
     */
    public static class Double extends QuadCurve2D implements Serializable {
        /**
         * 二次曲线段的起点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double x1;

        /**
         * 二次曲线段的起点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double y1;

        /**
         * 二次曲线段的控制点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double ctrlx;

        /**
         * 二次曲线段的控制点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double ctrly;

        /**
         * 二次曲线段的终点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double x2;

        /**
         * 二次曲线段的终点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double y2;

        /**
         * 构造并初始化一个 <code>QuadCurve2D</code>，坐标为 (0, 0, 0, 0, 0, 0)。
         * @since 1.2
         */
        public Double() {
        }

        /**
         * 从指定的 {@code double} 坐标构造并初始化一个 <code>QuadCurve2D</code>。
         *
         * @param x1 起点的 X 坐标
         * @param y1 起点的 Y 坐标
         * @param ctrlx 控制点的 X 坐标
         * @param ctrly 控制点的 Y 坐标
         * @param x2 终点的 X 坐标
         * @param y2 终点的 Y 坐标
         * @since 1.2
         */
        public Double(double x1, double y1,
                      double ctrlx, double ctrly,
                      double x2, double y2)
        {
            setCurve(x1, y1, ctrlx, ctrly, x2, y2);
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
        public double getCtrlX() {
            return ctrlx;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getCtrlY() {
            return ctrly;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Point2D getCtrlPt() {
            return new Point2D.Double(ctrlx, ctrly);
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
                             double ctrlx, double ctrly,
                             double x2, double y2)
        {
            this.x1    = x1;
            this.y1    = y1;
            this.ctrlx = ctrlx;
            this.ctrly = ctrly;
            this.x2    = x2;
            this.y2    = y2;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D getBounds2D() {
            double left   = Math.min(Math.min(x1, x2), ctrlx);
            double top    = Math.min(Math.min(y1, y2), ctrly);
            double right  = Math.max(Math.max(x1, x2), ctrlx);
            double bottom = Math.max(Math.max(y1, y2), ctrly);
            return new Rectangle2D.Double(left, top,
                                          right - left, bottom - top);
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = 4217149928428559721L;
    }

    /**
     * 这是一个抽象类，不能直接实例化。
     * 提供了类型特定的实现子类，可以实例化并提供多种格式来存储
     * 满足以下各种访问方法所需的信息。
     *
     * @see java.awt.geom.QuadCurve2D.Float
     * @see java.awt.geom.QuadCurve2D.Double
     * @since 1.2
     */
    protected QuadCurve2D() {
    }

    /**
     * 返回起点的 X 坐标，精度为 <code>double</code>。
     * @return 起点的 X 坐标。
     * @since 1.2
     */
    public abstract double getX1();

    /**
     * 返回起点的 Y 坐标，精度为 <code>double</code>。
     * @return 起点的 Y 坐标。
     * @since 1.2
     */
    public abstract double getY1();

    /**
     * 返回起点。
     * @return 一个 <code>Point2D</code>，它是此 <code>QuadCurve2D</code> 的起点。
     * @since 1.2
     */
    public abstract Point2D getP1();

    /**
     * 返回控制点的 X 坐标，精度为 <code>double</code>。
     * @return 控制点的 X 坐标
     * @since 1.2
     */
    public abstract double getCtrlX();

    /**
     * 返回控制点的 Y 坐标，精度为 <code>double</code>。
     * @return 控制点的 Y 坐标。
     * @since 1.2
     */
    public abstract double getCtrlY();

    /**
     * 返回控制点。
     * @return 一个 <code>Point2D</code>，它是此 <code>QuadCurve2D</code> 的控制点。
     * @since 1.2
     */
    public abstract Point2D getCtrlPt();

    /**
     * 返回终点的 X 坐标，精度为 <code>double</code>。
     * @return 终点的 X 坐标。
     * @since 1.2
     */
    public abstract double getX2();

    /**
     * 返回终点的 Y 坐标，精度为 <code>double</code>。
     * @return 终点的 Y 坐标。
     * @since 1.2
     */
    public abstract double getY2();

    /**
     * 返回终点。
     * @return 一个 <code>Point</code> 对象，它是此 <code>QuadCurve2D</code> 的终点。
     * @since 1.2
     */
    public abstract Point2D getP2();

    /**
     * 将此曲线的端点和控制点的位置设置为指定的 <code>double</code> 坐标。
     *
     * @param x1 起点的 X 坐标
     * @param y1 起点的 Y 坐标
     * @param ctrlx 控制点的 X 坐标
     * @param ctrly 控制点的 Y 坐标
     * @param x2 终点的 X 坐标
     * @param y2 终点的 Y 坐标
     * @since 1.2
     */
    public abstract void setCurve(double x1, double y1,
                                  double ctrlx, double ctrly,
                                  double x2, double y2);

    /**
     * 将此 <code>QuadCurve2D</code> 的端点和控制点的位置设置为指定数组中指定偏移量处的 <code>double</code> 坐标。
     * @param coords 包含坐标值的数组
     * @param offset 从数组中开始获取坐标值并分配给此 <code>QuadCurve2D</code> 的索引
     * @since 1.2
     */
    public void setCurve(double[] coords, int offset) {
        setCurve(coords[offset + 0], coords[offset + 1],
                 coords[offset + 2], coords[offset + 3],
                 coords[offset + 4], coords[offset + 5]);
    }


                /**
     * 设置此 <code>QuadCurve2D</code> 的终点和控制点的位置为指定的 <code>Point2D</code>
     * 坐标。
     * @param p1 起点
     * @param cp 控制点
     * @param p2 终点
     * @since 1.2
     */
    public void setCurve(Point2D p1, Point2D cp, Point2D p2) {
        setCurve(p1.getX(), p1.getY(),
                 cp.getX(), cp.getY(),
                 p2.getX(), p2.getY());
    }

    /**
     * 设置此 <code>QuadCurve2D</code> 的终点和控制点的位置为指定数组中从指定偏移量开始的
     * <code>Point2D</code> 对象的坐标。
     * @param pts 包含定义坐标值的 <code>Point2D</code> 的数组
     * @param offset 从 <code>pts</code> 中开始获取坐标值并分配给此
     *          <code>QuadCurve2D</code> 的索引
     * @since 1.2
     */
    public void setCurve(Point2D[] pts, int offset) {
        setCurve(pts[offset + 0].getX(), pts[offset + 0].getY(),
                 pts[offset + 1].getX(), pts[offset + 1].getY(),
                 pts[offset + 2].getX(), pts[offset + 2].getY());
    }

    /**
     * 设置此 <code>QuadCurve2D</code> 的终点和控制点的位置与指定的
     * <code>QuadCurve2D</code> 相同。
     * @param c 指定的 <code>QuadCurve2D</code>
     * @since 1.2
     */
    public void setCurve(QuadCurve2D c) {
        setCurve(c.getX1(), c.getY1(),
                 c.getCtrlX(), c.getCtrlY(),
                 c.getX2(), c.getY2());
    }

    /**
     * 返回指定控制点定义的二次曲线的平坦度的平方，即控制点到连接端点的直线的最大距离的平方。
     *
     * @param x1 起点的 X 坐标
     * @param y1 起点的 Y 坐标
     * @param ctrlx 控制点的 X 坐标
     * @param ctrly 控制点的 Y 坐标
     * @param x2 终点的 X 坐标
     * @param y2 终点的 Y 坐标
     * @return 由指定坐标定义的二次曲线的平坦度的平方。
     * @since 1.2
     */
    public static double getFlatnessSq(double x1, double y1,
                                       double ctrlx, double ctrly,
                                       double x2, double y2) {
        return Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx, ctrly);
    }

    /**
     * 返回指定控制点定义的二次曲线的平坦度，即控制点到连接端点的直线的最大距离。
     *
     * @param x1 起点的 X 坐标
     * @param y1 起点的 Y 坐标
     * @param ctrlx 控制点的 X 坐标
     * @param ctrly 控制点的 Y 坐标
     * @param x2 终点的 X 坐标
     * @param y2 终点的 Y 坐标
     * @return 由指定坐标定义的二次曲线的平坦度。
     * @since 1.2
     */
    public static double getFlatness(double x1, double y1,
                                     double ctrlx, double ctrly,
                                     double x2, double y2) {
        return Line2D.ptSegDist(x1, y1, x2, y2, ctrlx, ctrly);
    }

    /**
     * 返回由指定数组中从指定索引开始的控制点定义的二次曲线的平坦度的平方，即控制点到连接端点的直线的最大距离的平方。
     * @param coords 包含坐标值的数组
     * @param offset 从 <code>coords</code> 中开始获取值的索引
     * @return 由指定数组中从指定索引开始的值定义的二次曲线的平坦度。
     * @since 1.2
     */
    public static double getFlatnessSq(double coords[], int offset) {
        return Line2D.ptSegDistSq(coords[offset + 0], coords[offset + 1],
                                  coords[offset + 4], coords[offset + 5],
                                  coords[offset + 2], coords[offset + 3]);
    }

    /**
     * 返回由指定数组中从指定索引开始的控制点定义的二次曲线的平坦度，即控制点到连接端点的直线的最大距离。
     * @param coords 包含坐标值的数组
     * @param offset 从 <code>coords</code> 中开始获取坐标值的索引
     * @return 由指定数组中从指定索引开始的值定义的二次曲线的平坦度。
     * @since 1.2
     */
    public static double getFlatness(double coords[], int offset) {
        return Line2D.ptSegDist(coords[offset + 0], coords[offset + 1],
                                coords[offset + 4], coords[offset + 5],
                                coords[offset + 2], coords[offset + 3]);
    }

    /**
     * 返回此 <code>QuadCurve2D</code> 的平坦度的平方，即控制点到连接端点的直线的最大距离的平方。
     * @return 此 <code>QuadCurve2D</code> 的平坦度的平方。
     * @since 1.2
     */
    public double getFlatnessSq() {
        return Line2D.ptSegDistSq(getX1(), getY1(),
                                  getX2(), getY2(),
                                  getCtrlX(), getCtrlY());
    }

    /**
     * 返回此 <code>QuadCurve2D</code> 的平坦度，即控制点到连接端点的直线的最大距离。
     * @return 此 <code>QuadCurve2D</code> 的平坦度。
     * @since 1.2
     */
    public double getFlatness() {
        return Line2D.ptSegDist(getX1(), getY1(),
                                getX2(), getY2(),
                                getCtrlX(), getCtrlY());
    }

    /**
     * 将此 <code>QuadCurve2D</code> 分割成两个子曲线，并将结果存储在 <code>left</code> 和
     * <code>right</code> 曲线参数中。
     * <code>left</code> 和 <code>right</code> 对象中的任何一个或两个都可以是此 <code>QuadCurve2D</code> 或
     * <code>null</code>。
     * @param left 用于存储分割曲线的左半部分或前半部分的 <code>QuadCurve2D</code> 对象
     * @param right 用于存储分割曲线的右半部分或后半部分的 <code>QuadCurve2D</code> 对象
     * @since 1.2
     */
    public void subdivide(QuadCurve2D left, QuadCurve2D right) {
        subdivide(this, left, right);
    }

    /**
     * 将由 <code>src</code> 参数指定的二次曲线分割成两个子曲线，并将结果存储在 <code>left</code> 和
     * <code>right</code> 曲线参数中。
     * <code>left</code> 和 <code>right</code> 对象中的任何一个或两个都可以是 <code>src</code> 对象或
     * <code>null</code>。
     * @param src 要分割的二次曲线
     * @param left 用于存储分割曲线的左半部分或前半部分的 <code>QuadCurve2D</code> 对象
     * @param right 用于存储分割曲线的右半部分或后半部分的 <code>QuadCurve2D</code> 对象
     * @since 1.2
     */
    public static void subdivide(QuadCurve2D src,
                                 QuadCurve2D left,
                                 QuadCurve2D right) {
        double x1 = src.getX1();
        double y1 = src.getY1();
        double ctrlx = src.getCtrlX();
        double ctrly = src.getCtrlY();
        double x2 = src.getX2();
        double y2 = src.getY2();
        double ctrlx1 = (x1 + ctrlx) / 2.0;
        double ctrly1 = (y1 + ctrly) / 2.0;
        double ctrlx2 = (x2 + ctrlx) / 2.0;
        double ctrly2 = (y2 + ctrly) / 2.0;
        ctrlx = (ctrlx1 + ctrlx2) / 2.0;
        ctrly = (ctrly1 + ctrly2) / 2.0;
        if (left != null) {
            left.setCurve(x1, y1, ctrlx1, ctrly1, ctrlx, ctrly);
        }
        if (right != null) {
            right.setCurve(ctrlx, ctrly, ctrlx2, ctrly2, x2, y2);
        }
    }

    /**
     * 将由 <code>src</code> 数组中从 <code>srcoff</code> 到 <code>srcoff</code> + 5 的坐标定义的二次曲线分割成两个子曲线，
     * 并将结果存储在两个结果数组中对应的位置。
     * <code>left</code> 和 <code>right</code> 数组中的任何一个或两个都可以是 <code>null</code> 或与 <code>src</code> 数组和偏移量相同的引用。
     * 注意，第一个分割曲线的最后一个点与第二个分割曲线的第一个点相同。因此，可以将相同的数组传递给 <code>left</code> 和
     * <code>right</code>，并使用偏移量使 <code>rightoff</code> 等于 <code>leftoff</code> + 4 以避免为这个公共点分配额外的存储空间。
     * @param src 用于存储源曲线坐标的数组
     * @param srcoff 源坐标数组中开始的偏移量
     * @param left 用于存储第一个分割曲线坐标的数组
     * @param leftoff 第一个分割曲线坐标数组中开始的偏移量
     * @param right 用于存储第二个分割曲线坐标的数组
     * @param rightoff 第二个分割曲线坐标数组中开始的偏移量
     * @since 1.2
     */
    public static void subdivide(double src[], int srcoff,
                                 double left[], int leftoff,
                                 double right[], int rightoff) {
        double x1 = src[srcoff + 0];
        double y1 = src[srcoff + 1];
        double ctrlx = src[srcoff + 2];
        double ctrly = src[srcoff + 3];
        double x2 = src[srcoff + 4];
        double y2 = src[srcoff + 5];
        if (left != null) {
            left[leftoff + 0] = x1;
            left[leftoff + 1] = y1;
        }
        if (right != null) {
            right[rightoff + 4] = x2;
            right[rightoff + 5] = y2;
        }
        x1 = (x1 + ctrlx) / 2.0;
        y1 = (y1 + ctrly) / 2.0;
        x2 = (x2 + ctrlx) / 2.0;
        y2 = (y2 + ctrly) / 2.0;
        ctrlx = (x1 + x2) / 2.0;
        ctrly = (y1 + y2) / 2.0;
        if (left != null) {
            left[leftoff + 2] = x1;
            left[leftoff + 3] = y1;
            left[leftoff + 4] = ctrlx;
            left[leftoff + 5] = ctrly;
        }
        if (right != null) {
            right[rightoff + 0] = ctrlx;
            right[rightoff + 1] = ctrly;
            right[rightoff + 2] = x2;
            right[rightoff + 3] = y2;
        }
    }

    /**
     * 求解由 <code>eqn</code> 数组中的系数定义的二次方程，并将非复数根返回到同一个数组中，返回根的数量。
     * 求解的二次方程表示为：
     * <pre>
     *     eqn = {C, B, A};
     *     ax^2 + bx + c = 0
     * </pre>
     * 返回值 <code>-1</code> 用于区分常数方程（可能总是 0 或从不为 0）和没有零点的方程。
     * @param eqn 包含二次系数的数组
     * @return 根的数量，或 <code>-1</code> 如果方程是常数
     * @since 1.2
     */
    public static int solveQuadratic(double eqn[]) {
        return solveQuadratic(eqn, eqn);
    }

    /**
     * 求解由 <code>eqn</code> 数组中的系数定义的二次方程，并将非复数根返回到 <code>res</code> 数组中，返回根的数量。
     * 求解的二次方程表示为：
     * <pre>
     *     eqn = {C, B, A};
     *     ax^2 + bx + c = 0
     * </pre>
     * 返回值 <code>-1</code> 用于区分常数方程（可能总是 0 或从不为 0）和没有零点的方程。
     * @param eqn 用于求解二次方程的指定系数数组
     * @param res 包含求解二次方程的非复数根的数组
     * @return 根的数量，或 <code>-1</code> 如果方程是常数。
     * @since 1.3
     */
    public static int solveQuadratic(double eqn[], double res[]) {
        double a = eqn[2];
        double b = eqn[1];
        double c = eqn[0];
        int roots = 0;
        if (a == 0.0) {
            // 二次抛物线退化为直线。
            if (b == 0.0) {
                // 直线退化为常数。
                return -1;
            }
            res[roots++] = -c / b;
        } else {
            // 从 Numerical Recipes, 5.6, Quadratic and Cubic Equations
            double d = b * b - 4.0 * a * c;
            if (d < 0.0) {
                // 如果 d < 0.0，则没有根
                return 0;
            }
            d = Math.sqrt(d);
            // 为了精度，计算一个根使用：
            //     (-b +/- d) / 2a
            // 另一个根使用：
            //     2c / (-b +/- d)
            // 选择 +/- 的符号使 b+d 的绝对值更大
            if (b < 0.0) {
                d = -d;
            }
            double q = (b + d) / -2.0;
            // 已经测试过 a 不为 0
            res[roots++] = q / a;
            if (q != 0.0) {
                res[roots++] = c / q;
            }
        }
        return roots;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(double x, double y) {

        double x1 = getX1();
        double y1 = getY1();
        double xc = getCtrlX();
        double yc = getCtrlY();
        double x2 = getX2();
        double y2 = getY2();

        /*
         * 我们有一个由二次曲线 Pc(t) 和线 Pl(t) 限定的凸形。
         *
         *     P1 = (x1, y1) - 曲线的起点
         *     P2 = (x2, y2) - 曲线的终点
         *     Pc = (xc, yc) - 控制点
         *
         *     Pq(t) = P1*(1 - t)^2 + 2*Pc*t*(1 - t) + P2*t^2 =
         *           = (P1 - 2*Pc + P2)*t^2 + 2*(Pc - P1)*t + P1
         *     Pl(t) = P1*(1 - t) + P2*t
         *     t = [0:1]
         *
         *     P = (x, y) - 感兴趣的点
         *
         * 让我们看看二次曲线方程的二阶导数：
         *
         *     Pq''(t) = 2 * (P1 - 2 * Pc + P2) = Pq''
         *     它是一个常数向量。
         *
         * 让我们通过 P 画一条平行于该向量的线，并找到二次曲线和该线的交点。
         *
         * Pq(t) 是交点如果下面的方程组有解。
         *
         *     L(s) = P + Pq''*s == Pq(t)
         *     Pq''*s + (P - Pq(t)) == 0
         *
         *     | xq''*s + (x - xq(t)) == 0
         *     | yq''*s + (y - yq(t)) == 0
         *
         * 如果该矩阵的秩等于 1，则该方程组有解。也就是说，矩阵的行列式应该为零。
         *
         *     (y - yq(t))*xq'' == (x - xq(t))*yq''
         *
         * 让我们解这个关于 't' 的方程。
         * 也令 kx = x1 - 2*xc + x2
         *          ky = y1 - 2*yc + y2
         *
         *     t0q = (1/2)*((x - x1)*ky - (y - y1)*kx) /
         *                 ((xc - x1)*ky - (yc - y1)*kx)
         *
         * 让我们对我们的线 Pl(t) 做同样的事情：
         *
         *     t0l = ((x - x1)*ky - (y - y1)*kx) /
         *           ((x2 - x1)*ky - (y2 - y1)*kx)
         *
         * 很容易检查 t0q == t0l。这个事实意味着我们可以只计算一次 t0。
         *
         * 如果 t0 < 0 或 t0 > 1，则交点在形状边界之外。因此，P 肯定在形状之外。
         *
         * 如果 t0 在 [0:1] 范围内，我们应该计算 Pq(t0) 和 Pl(t0)。现在我们有三个点，它们都在一条直线上。因此，我们只需要检测我们的兴趣点是否在交点之间。
         *
         * 如果 t0q 和 t0l 方程中的分母为零，则这些点必须共线，因此曲线是退化的，不包含任何面积。因此，结果为 false。
         */
        double kx = x1 - 2 * xc + x2;
        double ky = y1 - 2 * yc + y2;
        double dx = x - x1;
        double dy = y - y1;
        double dxl = x2 - x1;
        double dyl = y2 - y1;


/**
 * 刷新流。
 */
public void flush() { }

/**
 * 关闭流。
 */
public void close() { }

}

/**
 * {@inheritDoc}
 * @since 1.2
 */
public boolean contains(Point2D p) {
    return contains(p.getX(), p.getY());
}

/**
 * 填充参数方程的系数数组，准备好使用 solveQuadratic 解决与 val 的问题。
 * 我们目前有：
 *     val = Py(t) = C1*(1-t)^2 + 2*CP*t*(1-t) + C2*t^2
 *                 = C1 - 2*C1*t + C1*t^2 + 2*CP*t - 2*CP*t^2 + C2*t^2
 *                 = C1 + (2*CP - 2*C1)*t + (C1 - 2*CP + C2)*t^2
 *               0 = (C1 - val) + (2*CP - 2*C1)*t + (C1 - 2*CP + C2)*t^2
 *               0 = C + Bt + At^2
 *     C = C1 - val
 *     B = 2*CP - 2*C1
 *     A = C1 - 2*CP + C2
 */
private static void fillEqn(double eqn[], double val,
                            double c1, double cp, double c2) {
    eqn[0] = c1 - val;
    eqn[1] = cp + cp - c1 - c1;
    eqn[2] = c1 - cp - cp + c2;
    return;
}

/**
 * 评估 vals[] 数组中前 num 个槽中的 t 值，并将评估后的值放回同一个数组中。仅评估在范围 &lt;0, 1&gt; 内的 t 值，包括
 * 0 和 1 范围的端点，如果 include0 或 include1 布尔值为 true。如果传入了“拐点”方程，
 * 那么代表该二次方程拐点的任何点也将被忽略。
 */
private static int evalQuadratic(double vals[], int num,
                                 boolean include0,
                                 boolean include1,
                                 double inflect[],
                                 double c1, double ctrl, double c2) {
    int j = 0;
    for (int i = 0; i < num; i++) {
        double t = vals[i];
        if ((include0 ? t >= 0 : t > 0) &&
            (include1 ? t <= 1 : t < 1) &&
            (inflect == null ||
             inflect[1] + 2*inflect[2]*t != 0))
        {
            double u = 1 - t;
            vals[j++] = c1*u*u + 2*ctrl*t*u + c2*t*t;
        }
    }
    return j;
}

private static final int BELOW = -2;
private static final int LOWEDGE = -1;
private static final int INSIDE = 0;
private static final int HIGHEDGE = 1;
private static final int ABOVE = 2;

/**
 * 确定 coord 与从 low 到 high 的范围的关系。假设 low &lt;= high。返回值是 5 个值之一：BELOW, LOWEDGE, INSIDE, HIGHEDGE, 或 ABOVE。
 */
private static int getTag(double coord, double low, double high) {
    if (coord <= low) {
        return (coord < low ? BELOW : LOWEDGE);
    }
    if (coord >= high) {
        return (coord > high ? ABOVE : HIGHEDGE);
    }
    return INSIDE;
}

/**
 * 确定 pttag 表示的坐标是否已经在其测试范围内，或者与两个 opttags 表示的另一个坐标之一在该测试范围的“内部”。
 * 换句话说，两个“opt”点中是否有任何一个“将 pt 向内拉”？
 */
private static boolean inwards(int pttag, int opt1tag, int opt2tag) {
    switch (pttag) {
    case BELOW:
    case ABOVE:
    default:
        return false;
    case LOWEDGE:
        return (opt1tag >= INSIDE || opt2tag >= INSIDE);
    case INSIDE:
        return true;
    case HIGHEDGE:
        return (opt1tag <= INSIDE || opt2tag <= INSIDE);
    }
}

/**
 * {@inheritDoc}
 * @since 1.2
 */
public boolean intersects(double x, double y, double w, double h) {
    // 如果矩形不存在，则简单地拒绝
    if (w <= 0 || h <= 0) {
        return false;
    }

    // 如果任一端点在矩形内（不在边界上，因为它可能在那里结束而没有进入内部）
    // 记录它们相对于矩形的位置。
    //     -1 => 左，0 => 内部，1 => 右
    double x1 = getX1();
    double y1 = getY1();
    int x1tag = getTag(x1, x, x+w);
    int y1tag = getTag(y1, y, y+h);
    if (x1tag == INSIDE && y1tag == INSIDE) {
        return true;
    }
    double x2 = getX2();
    double y2 = getY2();
    int x2tag = getTag(x2, x, x+w);
    int y2tag = getTag(y2, y, y+h);
    if (x2tag == INSIDE && y2tag == INSIDE) {
        return true;
    }
    double ctrlx = getCtrlX();
    double ctrly = getCtrlY();
    int ctrlxtag = getTag(ctrlx, x, x+w);
    int ctrlytag = getTag(ctrly, y, y+h);

    // 如果所有点完全在矩形的一侧，则简单地拒绝
    if (x1tag < INSIDE && x2tag < INSIDE && ctrlxtag < INSIDE) {
        return false;       // 所有点在左侧
    }
    if (y1tag < INSIDE && y2tag < INSIDE && ctrlytag < INSIDE) {
        return false;       // 所有点在上方
    }
    if (x1tag > INSIDE && x2tag > INSIDE && ctrlxtag > INSIDE) {
        return false;       // 所有点在右侧
    }
    if (y1tag > INSIDE && y2tag > INSIDE && ctrlytag > INSIDE) {
        return false;       // 所有点在下方
    }

    // 测试端点在边界上，且从它们开始的线段或曲线向内移动
    // 注意：这些测试是上述快速端点测试的超集，因此重复了那些测试，但涵盖了更多情况
    if (inwards(x1tag, x2tag, ctrlxtag) &&
        inwards(y1tag, y2tag, ctrlytag))
    {
        // 第一个端点在边界上，且任一边缘向内移动
        return true;
    }
    if (inwards(x2tag, x1tag, ctrlxtag) &&
        inwards(y2tag, y1tag, ctrlytag))
    {
        // 第二个端点在边界上，且任一边缘向内移动
        return true;
    }

    // 如果端点直接跨越矩形，则简单地接受
    boolean xoverlap = (x1tag * x2tag <= 0);
    boolean yoverlap = (y1tag * y2tag <= 0);
    if (x1tag == INSIDE && x2tag == INSIDE && yoverlap) {
        return true;
    }
    if (y1tag == INSIDE && y2tag == INSIDE && xoverlap) {
        return true;
    }

    // 现在我们知道两个端点都在矩形外部，但 3 个点不在矩形的一侧。
    // 因此曲线不能包含在矩形内，但矩形可能包含在曲线内，或者曲线可能与矩形边界相交。

    double[] eqn = new double[3];
    double[] res = new double[3];
    if (!yoverlap) {
        // 关闭线段的两个 Y 坐标在矩形上方或下方，这意味着我们只能在曲线在矩形顶部（或底部）交叉
        // 超过一个地方，并且这些交叉位置跨越矩形的水平范围时相交。
        fillEqn(eqn, (y1tag < INSIDE ? y : y+h), y1, ctrly, y2);
        return (solveQuadratic(eqn, res) == 2 &&
                evalQuadratic(res, 2, true, true, null,
                              x1, ctrlx, x2) == 2 &&
                getTag(res[0], x, x+w) * getTag(res[1], x, x+w) <= 0);
    }

    // Y 范围重叠。现在我们检查 X 范围
    if (!xoverlap) {
        // 关闭线段的两个 X 坐标在矩形左侧或右侧，这意味着我们只能在曲线在矩形左侧（或右侧）边缘
        // 超过一个地方交叉，并且这些交叉位置跨越矩形的垂直范围时相交。
        fillEqn(eqn, (x1tag < INSIDE ? x : x+w), x1, ctrlx, x2);
        return (solveQuadratic(eqn, res) == 2 &&
                evalQuadratic(res, 2, true, true, null,
                              y1, ctrly, y2) == 2 &&
                getTag(res[0], y, y+h) * getTag(res[1], y, y+h) <= 0);
    }

    // 端点的 X 和 Y 范围与矩形的 X 和 Y 范围重叠，现在找出端点线段如何与矩形的 Y 范围相交
    double dx = x2 - x1;
    double dy = y2 - y1;
    double k = y2 * x1 - x2 * y1;
    int c1tag, c2tag;
    if (y1tag == INSIDE) {
        c1tag = x1tag;
    } else {
        c1tag = getTag((k + dx * (y1tag < INSIDE ? y : y+h)) / dy, x, x+w);
    }
    if (y2tag == INSIDE) {
        c2tag = x2tag;
    } else {
        c2tag = getTag((k + dx * (y2tag < INSIDE ? y : y+h)) / dy, x, x+w);
    }
    // 如果线段与矩形 Y 范围相交的部分水平跨越 - 简单地接受
    if (c1tag * c2tag <= 0) {
        return true;
    }

    // 现在我们知道 X 和 Y 范围都相交，并且端点线段没有直接穿过矩形。
    //
    // 我们几乎可以将这种情况视为上述一种情况，即两个端点都在一侧，只是我们将只有一个曲线与矩形垂直边的交点。
    // 这是因为端点线段已经占用了另一个交点。
    //
    // （记住 X 和 Y 范围都有重叠，这意味着线段必须至少穿过矩形的一个垂直边 - 特别是“近垂直边” -
    // 留给曲线的只有一个交点。）
    //
    // 现在我们计算两个交点在矩形“近垂直边”上的 Y 标签。我们将有一个与端点线段的交点，和一个与曲线的交点。
    // 如果这两个垂直交点重叠矩形的 Y 范围，我们就有一个交点。否则，我们没有。

    // c1tag = 端点线段的垂直交点类
    //
    // 选择与上述子段不在同一侧的端点的 Y 标签。
    // 注意我们可以“借用”该端点的现有 Y 标签，因为它将与垂直交点相同。
    c1tag = ((c1tag * x1tag <= 0) ? y1tag : y2tag);

    // c2tag = 曲线的垂直交点类
    //
    // 我们必须直接计算这个。
    // 注意 c2tag 仍然可以告诉我们测试哪个垂直边。
    fillEqn(eqn, (c2tag < INSIDE ? x : x+w), x1, ctrlx, x2);
    int num = solveQuadratic(eqn, res);

    // 注意：我们应该能够断言(num == 2); 因为 X 范围“穿过”（而不是接触）垂直边界，
    // 但为了完整性，我们将 num 传递给 evalQuadratic。
    evalQuadratic(res, num, true, true, null, y1, ctrly, y2);

    // 注意：我们可以断言(num evals == 1); 因为 2 个交点中的一个将超出 [0,1] 范围。
    c2tag = getTag(res[0], y, y+h);

    // 最后，如果两个交点重叠矩形的 Y 范围，我们就有一个交点。
    return (c1tag * c2tag <= 0);
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
    // 断言：由连接其端点闭合的二次曲线总是凸的。
    return (contains(x, y) &&
            contains(x + w, y) &&
            contains(x + w, y + h) &&
            contains(x, y + h));
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
 * 返回一个定义此 <code>QuadCurve2D</code> 形状边界的迭代对象。
 * 该类的迭代器不是多线程安全的，这意味着此 <code>QuadCurve2D</code> 类不保证
 * 对此 <code>QuadCurve2D</code> 对象的几何修改不会影响已经进行中的任何几何迭代。
 * @param at 一个可选的 {@link AffineTransform}，应用于形状边界
 * @return 一个 {@link PathIterator} 对象，定义形状的边界。
 * @since 1.2
 */
public PathIterator getPathIterator(AffineTransform at) {
    return new QuadIterator(this, at);
}

/**
 * 返回一个定义此 <code>QuadCurve2D</code> 形状扁平化边界的迭代对象。
 * 该类的迭代器不是多线程安全的，这意味着此 <code>QuadCurve2D</code> 类不保证
 * 对此 <code>QuadCurve2D</code> 对象的几何修改不会影响已经进行中的任何几何迭代。
 * @param at 一个可选的 <code>AffineTransform</code>，应用于形状边界
 * @param flatness 控制点对于细分曲线与连接端点的直线之间的最大距离，超过此距离时，此曲线将被直线连接端点的直线替代。
 * @return 一个 <code>PathIterator</code> 对象，定义形状的扁平化边界。
 * @since 1.2
 */
public PathIterator getPathIterator(AffineTransform at, double flatness) {
    return new FlatteningPathIterator(getPathIterator(at), flatness);
}

/**
 * 创建一个与此对象相同类且内容相同的新对象。
 *
 * @return 一个此实例的克隆。
 * @exception  OutOfMemoryError 如果没有足够的内存。
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
