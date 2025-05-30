
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

/**
 * 此 <code>Line2D</code> 表示 {@code (x,y)} 坐标空间中的线段。此类，如 Java 2D API 的所有部分，使用一个默认的坐标系统，称为 <i>用户空间</i>，其中 y 轴值向下增加，x 轴值向右增加。有关用户空间坐标系统的更多信息，请参阅 Java 2D 程序员指南的 <a href="https://docs.oracle.com/javase/1.3/docs/guide/2d/spec/j2d-intro.fm2.html#61857">坐标系统</a> 部分。
 * <p>
 * 此类仅是所有存储 2D 线段的对象的抽象超类。
 * 实际的坐标存储表示由子类决定。
 *
 * @author      Jim Graham
 * @since 1.2
 */
public abstract class Line2D implements Shape, Cloneable {

    /**
     * 用浮点坐标指定的线段。
     * @since 1.2
     */
    public static class Float extends Line2D implements Serializable {
        /**
         * 线段起点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float x1;

        /**
         * 线段起点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float y1;

        /**
         * 线段终点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float x2;

        /**
         * 线段终点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float y2;

        /**
         * 构造并初始化一个坐标为 (0, 0) &rarr; (0, 0) 的线段。
         * @since 1.2
         */
        public Float() {
        }

        /**
         * 从指定的坐标构造并初始化一个线段。
         * @param x1 起点的 X 坐标
         * @param y1 起点的 Y 坐标
         * @param x2 终点的 X 坐标
         * @param y2 终点的 Y 坐标
         * @since 1.2
         */
        public Float(float x1, float y1, float x2, float y2) {
            setLine(x1, y1, x2, y2);
        }

        /**
         * 从指定的 <code>Point2D</code> 对象构造并初始化一个 <code>Line2D</code>。
         * @param p1 此线段的起点 <code>Point2D</code>
         * @param p2 此线段的终点 <code>Point2D</code>
         * @since 1.2
         */
        public Float(Point2D p1, Point2D p2) {
            setLine(p1, p2);
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
        public void setLine(double x1, double y1, double x2, double y2) {
            this.x1 = (float) x1;
            this.y1 = (float) y1;
            this.x2 = (float) x2;
            this.y2 = (float) y2;
        }

        /**
         * 将此 <code>Line2D</code> 的端点位置设置为指定的浮点坐标。
         * @param x1 起点的 X 坐标
         * @param y1 起点的 Y 坐标
         * @param x2 终点的 X 坐标
         * @param y2 终点的 Y 坐标
         * @since 1.2
         */
        public void setLine(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D getBounds2D() {
            float x, y, w, h;
            if (x1 < x2) {
                x = x1;
                w = x2 - x1;
            } else {
                x = x2;
                w = x1 - x2;
            }
            if (y1 < y2) {
                y = y1;
                h = y2 - y1;
            } else {
                y = y2;
                h = y1 - y2;
            }
            return new Rectangle2D.Float(x, y, w, h);
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = 6161772511649436349L;
    }

    /**
     * 用双精度坐标指定的线段。
     * @since 1.2
     */
    public static class Double extends Line2D implements Serializable {
        /**
         * 线段起点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double x1;

        /**
         * 线段起点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double y1;

        /**
         * 线段终点的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double x2;

        /**
         * 线段终点的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double y2;

        /**
         * 构造并初始化一个坐标为 (0, 0) &rarr; (0, 0) 的线段。
         * @since 1.2
         */
        public Double() {
        }

        /**
         * 从指定的坐标构造并初始化一个 <code>Line2D</code>。
         * @param x1 起点的 X 坐标
         * @param y1 起点的 Y 坐标
         * @param x2 终点的 X 坐标
         * @param y2 终点的 Y 坐标
         * @since 1.2
         */
        public Double(double x1, double y1, double x2, double y2) {
            setLine(x1, y1, x2, y2);
        }

        /**
         * 从指定的 <code>Point2D</code> 对象构造并初始化一个 <code>Line2D</code>。
         * @param p1 此线段的起点 <code>Point2D</code>
         * @param p2 此线段的终点 <code>Point2D</code>
         * @since 1.2
         */
        public Double(Point2D p1, Point2D p2) {
            setLine(p1, p2);
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
        public void setLine(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D getBounds2D() {
            double x, y, w, h;
            if (x1 < x2) {
                x = x1;
                w = x2 - x1;
            } else {
                x = x2;
                w = x1 - x2;
            }
            if (y1 < y2) {
                y = y1;
                h = y2 - y1;
            } else {
                y = y2;
                h = y1 - y2;
            }
            return new Rectangle2D.Double(x, y, w, h);
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = 7979627399746467499L;
    }

    /**
     * 这是一个抽象类，不能直接实例化。
     * 提供了类型特定的实现子类，可以实例化并提供多种格式来存储满足以下各种辅助方法所需的信息。
     *
     * @see java.awt.geom.Line2D.Float
     * @see java.awt.geom.Line2D.Double
     * @since 1.2
     */
    protected Line2D() {
    }

    /**
     * 返回起点的 X 坐标（双精度）。
     * @return 此 {@code Line2D} 对象的起点的 X 坐标。
     * @since 1.2
     */
    public abstract double getX1();

    /**
     * 返回起点的 Y 坐标（双精度）。
     * @return 此 {@code Line2D} 对象的起点的 Y 坐标。
     * @since 1.2
     */
    public abstract double getY1();

    /**
     * 返回此 <code>Line2D</code> 的起点 <code>Point2D</code>。
     * @return 此 <code>Line2D</code> 的起点 <code>Point2D</code>。
     * @since 1.2
     */
    public abstract Point2D getP1();

    /**
     * 返回终点的 X 坐标（双精度）。
     * @return 此 {@code Line2D} 对象的终点的 X 坐标。
     * @since 1.2
     */
    public abstract double getX2();

    /**
     * 返回终点的 Y 坐标（双精度）。
     * @return 此 {@code Line2D} 对象的终点的 Y 坐标。
     * @since 1.2
     */
    public abstract double getY2();

    /**
     * 返回此 <code>Line2D</code> 的终点 <code>Point2D</code>。
     * @return 此 <code>Line2D</code> 的终点 <code>Point2D</code>。
     * @since 1.2
     */
    public abstract Point2D getP2();

    /**
     * 将此 <code>Line2D</code> 的端点位置设置为指定的双精度坐标。
     * @param x1 起点的 X 坐标
     * @param y1 起点的 Y 坐标
     * @param x2 终点的 X 坐标
     * @param y2 终点的 Y 坐标
     * @since 1.2
     */
    public abstract void setLine(double x1, double y1, double x2, double y2);

    /**
     * 将此 <code>Line2D</code> 的端点位置设置为指定的 <code>Point2D</code> 坐标。
     * @param p1 线段的起点 <code>Point2D</code>
     * @param p2 线段的终点 <code>Point2D</code>
     * @since 1.2
     */
    public void setLine(Point2D p1, Point2D p2) {
        setLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    /**
     * 将此 <code>Line2D</code> 的端点位置设置为与指定的 <code>Line2D</code> 的端点相同。
     * @param l 指定的 <code>Line2D</code>
     * @since 1.2
     */
    public void setLine(Line2D l) {
        setLine(l.getX1(), l.getY1(), l.getX2(), l.getY2());
    }

    /**
     * 返回指定点 {@code (px,py)} 与从 {@code (x1,y1)} 到 {@code (x2,y2)} 的线段之间的相对位置。
     * 返回值可以是 1、-1 或 0，表示指定线段必须围绕其第一个端点 {@code (x1,y1)} 旋转以指向指定点 {@code (px,py)} 的方向。
     * <p>返回值为 1 表示线段必须向正 X 轴向负 Y 轴的方向旋转。在 Java 2D 使用的默认坐标系统中，这个方向是逆时针方向。
     * <p>返回值为 -1 表示线段必须向正 X 轴向正 Y 轴的方向旋转。在默认坐标系统中，这个方向是顺时针方向。
     * <p>返回值为 0 表示点正好在线段上。注意，返回值为 0 很少见，且由于浮点数舍入问题，对确定共线性没有帮助。
     * <p>如果点与线段共线，但不在端点之间，则值为 -1 表示点在“超出 {@code (x1,y1)}”的位置，值为 1 表示点在“超出 {@code (x2,y2)}”的位置。
     *
     * @param x1 指定线段的起点 X 坐标
     * @param y1 指定线段的起点 Y 坐标
     * @param x2 指定线段的终点 X 坐标
     * @param y2 指定线段的终点 Y 坐标
     * @param px 要与指定线段比较的指定点的 X 坐标
     * @param py 要与指定线段比较的指定点的 Y 坐标
     * @return 一个整数，表示第三个指定坐标相对于由前两个指定坐标形成的线段的位置。
     * @since 1.2
     */
    public static int relativeCCW(double x1, double y1,
                                  double x2, double y2,
                                  double px, double py)
    {
        x2 -= x1;
        y2 -= y1;
        px -= x1;
        py -= y1;
        double ccw = px * y2 - py * x2;
        if (ccw == 0.0) {
            // 点共线，根据点落在线段的哪一侧进行分类。我们可以通过将 px,py 投影到线段上来计算一个相对值 - 负值表示点投影在线段的外部，相对于用于投影的特定端点。
            ccw = px * x2 + py * y2;
            if (ccw > 0.0) {
                // 反转投影，使其相对于原始 x2,y2
                // x2 和 y2 简单地取反。
                // px 和 py 需要减去 (x2 - x1) 或 (y2 - y1)（基于原始值）
                // 由于我们希望当点“超出 (x2,y2)”时得到正值，因此我们无论如何都要计算逆 - 因此我们保留 x2 & y2 的负值。
                px -= x2;
                py -= y2;
                ccw = px * x2 + py * y2;
                if (ccw < 0.0) {
                    ccw = 0.0;
                }
            }
        }
        return (ccw < 0.0) ? -1 : ((ccw > 0.0) ? 1 : 0);
    }


                /**
     * 返回指定点 {@code (px,py)} 与该线段相对位置的指示器。
     * 请参阅方法注释
     * {@link #relativeCCW(double, double, double, double, double, double)}
     * 以解释返回值。
     * @param px 要与该 <code>Line2D</code> 比较的指定点的 X 坐标
     * @param py 要与该 <code>Line2D</code> 比较的指定点的 Y 坐标
     * @return 一个整数，指示指定坐标相对于该 <code>Line2D</code> 的位置
     * @see #relativeCCW(double, double, double, double, double, double)
     * @since 1.2
     */
    public int relativeCCW(double px, double py) {
        return relativeCCW(getX1(), getY1(), getX2(), getY2(), px, py);
    }

    /**
     * 返回指定 <code>Point2D</code> 与该线段相对位置的指示器。
     * 请参阅方法注释
     * {@link #relativeCCW(double, double, double, double, double, double)}
     * 以解释返回值。
     * @param p 要与该 <code>Line2D</code> 比较的指定 <code>Point2D</code>
     * @return 一个整数，指示指定 <code>Point2D</code> 相对于该 <code>Line2D</code> 的位置
     * @see #relativeCCW(double, double, double, double, double, double)
     * @since 1.2
     */
    public int relativeCCW(Point2D p) {
        return relativeCCW(getX1(), getY1(), getX2(), getY2(),
                           p.getX(), p.getY());
    }

    /**
     * 测试从 {@code (x1,y1)} 到 {@code (x2,y2)} 的线段是否与从 {@code (x3,y3)}
     * 到 {@code (x4,y4)} 的线段相交。
     *
     * @param x1 第一条指定线段起点的 X 坐标
     * @param y1 第一条指定线段起点的 Y 坐标
     * @param x2 第一条指定线段终点的 X 坐标
     * @param y2 第一条指定线段终点的 Y 坐标
     * @param x3 第二条指定线段起点的 X 坐标
     * @param y3 第二条指定线段起点的 Y 坐标
     * @param x4 第二条指定线段终点的 X 坐标
     * @param y4 第二条指定线段终点的 Y 坐标
     * @return 如果第一条指定线段和第二条指定线段相交，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.2
     */
    public static boolean linesIntersect(double x1, double y1,
                                         double x2, double y2,
                                         double x3, double y3,
                                         double x4, double y4)
    {
        return ((relativeCCW(x1, y1, x2, y2, x3, y3) *
                 relativeCCW(x1, y1, x2, y2, x4, y4) <= 0)
                && (relativeCCW(x3, y3, x4, y4, x1, y1) *
                    relativeCCW(x3, y3, x4, y4, x2, y2) <= 0));
    }

    /**
     * 测试从 {@code (x1,y1)} 到 {@code (x2,y2)} 的线段是否与该线段相交。
     *
     * @param x1 指定线段起点的 X 坐标
     * @param y1 指定线段起点的 Y 坐标
     * @param x2 指定线段终点的 X 坐标
     * @param y2 指定线段终点的 Y 坐标
     * @return 如果该线段和指定线段相交，则返回 {@code <true>}；否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean intersectsLine(double x1, double y1, double x2, double y2) {
        return linesIntersect(x1, y1, x2, y2,
                              getX1(), getY1(), getX2(), getY2());
    }

    /**
     * 测试指定线段是否与该线段相交。
     * @param l 指定的 <code>Line2D</code>
     * @return 如果该线段和指定线段相交，则返回 <code>true</code>；
     *                  否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean intersectsLine(Line2D l) {
        return linesIntersect(l.getX1(), l.getY1(), l.getX2(), l.getY2(),
                              getX1(), getY1(), getX2(), getY2());
    }

    /**
     * 返回从点到线段的距离的平方。
     * 测量的距离是从指定点到指定端点之间的最近点的距离。
     * 如果指定点与线段在端点之间相交，此方法返回 0.0。
     *
     * @param x1 指定线段起点的 X 坐标
     * @param y1 指定线段起点的 Y 坐标
     * @param x2 指定线段终点的 X 坐标
     * @param y2 指定线段终点的 Y 坐标
     * @param px 要测量的指定点的 X 坐标
     * @param py 要测量的指定点的 Y 坐标
     * @return 一个双精度值，表示从指定点到指定线段的距离的平方。
     * @see #ptLineDistSq(double, double, double, double, double, double)
     * @since 1.2
     */
    public static double ptSegDistSq(double x1, double y1,
                                     double x2, double y2,
                                     double px, double py)
    {
        // 调整向量相对于 x1,y1
        // x2,y2 变为从 x1,y1 到线段终点的相对向量
        x2 -= x1;
        y2 -= y1;
        // px,py 变为从 x1,y1 到测试点的相对向量
        px -= x1;
        py -= y1;
        double dotprod = px * x2 + py * y2;
        double projlenSq;
        if (dotprod <= 0.0) {
            // px,py 在 x1,y1 一侧，远离 x2,y2
            // 到线段的距离是 px,py 向量的长度
            // "其（裁剪后）投影的长度" 现在是 0.0
            projlenSq = 0.0;
        } else {
            // 切换到相对于 x2,y2 的反向向量
            // x2,y2 已经是 x1,y1=>x2,y2 的负向量
            // 为了使 px,py 成为 px,py=>x2,y2 的负向量
            // 两个负向量的点积与两个正常向量的点积相同
            px = x2 - px;
            py = y2 - py;
            dotprod = px * x2 + py * y2;
            if (dotprod <= 0.0) {
                // px,py 在 x2,y2 一侧，远离 x1,y1
                // 到线段的距离是（反向）px,py 向量的长度
                // "其（裁剪后）投影的长度" 现在是 0.0
                projlenSq = 0.0;
            } else {
                // px,py 在 x1,y1 和 x2,y2 之间
                // dotprod 是 px,py 向量在 x2,y2=>x1,y1 向量上的投影长度
                // 乘以 x2,y2=>x1,y1 向量的长度
                projlenSq = dotprod * dotprod / (x2 * x2 + y2 * y2);
            }
        }
        // 到线的距离现在是相对点向量的长度
        // 减去其在线上的投影长度
        // （如果投影落在线段范围之外，则为零）
        double lenSq = px * px + py * py - projlenSq;
        if (lenSq < 0) {
            lenSq = 0;
        }
        return lenSq;
    }

    /**
     * 返回从点到线段的距离。
     * 测量的距离是从指定点到指定端点之间的最近点的距离。
     * 如果指定点与线段在端点之间相交，此方法返回 0.0。
     *
     * @param x1 指定线段起点的 X 坐标
     * @param y1 指定线段起点的 Y 坐标
     * @param x2 指定线段终点的 X 坐标
     * @param y2 指定线段终点的 Y 坐标
     * @param px 要测量的指定点的 X 坐标
     * @param py 要测量的指定点的 Y 坐标
     * @return 一个双精度值，表示从指定点到指定线段的距离。
     * @see #ptLineDist(double, double, double, double, double, double)
     * @since 1.2
     */
    public static double ptSegDist(double x1, double y1,
                                   double x2, double y2,
                                   double px, double py)
    {
        return Math.sqrt(ptSegDistSq(x1, y1, x2, y2, px, py));
    }

    /**
     * 返回从点到该线段的距离的平方。
     * 测量的距离是从指定点到当前线段端点之间的最近点的距离。
     * 如果指定点与线段在端点之间相交，此方法返回 0.0。
     *
     * @param px 要测量的指定点的 X 坐标
     * @param py 要测量的指定点的 Y 坐标
     * @return 一个双精度值，表示从指定点到当前线段的距离的平方。
     * @see #ptLineDistSq(double, double)
     * @since 1.2
     */
    public double ptSegDistSq(double px, double py) {
        return ptSegDistSq(getX1(), getY1(), getX2(), getY2(), px, py);
    }

    /**
     * 返回从 <code>Point2D</code> 到该线段的距离的平方。
     * 测量的距离是从指定点到当前线段端点之间的最近点的距离。
     * 如果指定点与线段在端点之间相交，此方法返回 0.0。
     * @param pt 要测量的指定 <code>Point2D</code>
     * @return 一个双精度值，表示从指定 <code>Point2D</code> 到当前线段的距离的平方。
     * @see #ptLineDistSq(Point2D)
     * @since 1.2
     */
    public double ptSegDistSq(Point2D pt) {
        return ptSegDistSq(getX1(), getY1(), getX2(), getY2(),
                           pt.getX(), pt.getY());
    }

    /**
     * 返回从点到该线段的距离。
     * 测量的距离是从指定点到当前线段端点之间的最近点的距离。
     * 如果指定点与线段在端点之间相交，此方法返回 0.0。
     *
     * @param px 要测量的指定点的 X 坐标
     * @param py 要测量的指定点的 Y 坐标
     * @return 一个双精度值，表示从指定点到当前线段的距离。
     * @see #ptLineDist(double, double)
     * @since 1.2
     */
    public double ptSegDist(double px, double py) {
        return ptSegDist(getX1(), getY1(), getX2(), getY2(), px, py);
    }

    /**
     * 返回从 <code>Point2D</code> 到该线段的距离。
     * 测量的距离是从指定点到当前线段端点之间的最近点的距离。
     * 如果指定点与线段在端点之间相交，此方法返回 0.0。
     * @param pt 要测量的指定 <code>Point2D</code>
     * @return 一个双精度值，表示从指定 <code>Point2D</code> 到当前线段的距离。
     * @see #ptLineDist(Point2D)
     * @since 1.2
     */
    public double ptSegDist(Point2D pt) {
        return ptSegDist(getX1(), getY1(), getX2(), getY2(),
                         pt.getX(), pt.getY());
    }

    /**
     * 返回从点到线的距离的平方。
     * 测量的距离是从指定点到由指定坐标定义的无限延长线的最近点的距离。
     * 如果指定点与线相交，此方法返回 0.0。
     *
     * @param x1 指定线起点的 X 坐标
     * @param y1 指定线起点的 Y 坐标
     * @param x2 指定线终点的 X 坐标
     * @param y2 指定线终点的 Y 坐标
     * @param px 要测量的指定点的 X 坐标
     * @param py 要测量的指定点的 Y 坐标
     * @return 一个双精度值，表示从指定点到指定线的距离的平方。
     * @see #ptSegDistSq(double, double, double, double, double, double)
     * @since 1.2
     */
    public static double ptLineDistSq(double x1, double y1,
                                      double x2, double y2,
                                      double px, double py)
    {
        // 调整向量相对于 x1,y1
        // x2,y2 变为从 x1,y1 到线段终点的相对向量
        x2 -= x1;
        y2 -= y1;
        // px,py 变为从 x1,y1 到测试点的相对向量
        px -= x1;
        py -= y1;
        double dotprod = px * x2 + py * y2;
        // dotprod 是 px,py 向量在 x1,y1=>x2,y2 向量上的投影长度
        // 乘以 x1,y1=>x2,y2 向量的长度
        double projlenSq = dotprod * dotprod / (x2 * x2 + y2 * y2);
        // 到线的距离现在是相对点向量的长度
        // 减去其在线上的投影长度
        double lenSq = px * px + py * py - projlenSq;
        if (lenSq < 0) {
            lenSq = 0;
        }
        return lenSq;
    }


                /**
     * 返回从点到直线的距离。
     * 测量的距离是从指定的点到由指定坐标定义的无限延伸直线的最近点之间的距离。如果指定的点与直线相交，此方法返回0.0。
     *
     * @param x1 指定直线的起点的X坐标
     * @param y1 指定直线的起点的Y坐标
     * @param x2 指定直线的终点的X坐标
     * @param y2 指定直线的终点的Y坐标
     * @param px 被测量的指定点的X坐标
     * @param py 被测量的指定点的Y坐标
     * @return 一个double值，表示从指定点到指定直线的距离。
     * @see #ptSegDist(double, double, double, double, double, double)
     * @since 1.2
     */
    public static double ptLineDist(double x1, double y1,
                                    double x2, double y2,
                                    double px, double py)
    {
        return Math.sqrt(ptLineDistSq(x1, y1, x2, y2, px, py));
    }

    /**
     * 返回从点到此直线的距离的平方。
     * 测量的距离是从指定的点到由这个<code>Line2D</code>定义的无限延伸直线的最近点之间的距离。如果指定的点与直线相交，此方法返回0.0。
     *
     * @param px 被测量的指定点的X坐标
     * @param py 被测量的指定点的Y坐标
     * @return 一个double值，表示从指定点到当前直线的距离的平方。
     * @see #ptSegDistSq(double, double)
     * @since 1.2
     */
    public double ptLineDistSq(double px, double py) {
        return ptLineDistSq(getX1(), getY1(), getX2(), getY2(), px, py);
    }

    /**
     * 返回从指定的<code>Point2D</code>到此直线的距离的平方。
     * 测量的距离是从指定的点到由这个<code>Line2D</code>定义的无限延伸直线的最近点之间的距离。如果指定的点与直线相交，此方法返回0.0。
     * @param pt 被测量的指定<code>Point2D</code>
     * @return 一个double值，表示从指定的<code>Point2D</code>到当前直线的距离的平方。
     * @see #ptSegDistSq(Point2D)
     * @since 1.2
     */
    public double ptLineDistSq(Point2D pt) {
        return ptLineDistSq(getX1(), getY1(), getX2(), getY2(),
                            pt.getX(), pt.getY());
    }

    /**
     * 返回从点到此直线的距离。
     * 测量的距离是从指定的点到由这个<code>Line2D</code>定义的无限延伸直线的最近点之间的距离。如果指定的点与直线相交，此方法返回0.0。
     *
     * @param px 被测量的指定点的X坐标
     * @param py 被测量的指定点的Y坐标
     * @return 一个double值，表示从指定点到当前直线的距离。
     * @see #ptSegDist(double, double)
     * @since 1.2
     */
    public double ptLineDist(double px, double py) {
        return ptLineDist(getX1(), getY1(), getX2(), getY2(), px, py);
    }

    /**
     * 返回从<code>Point2D</code>到此直线的距离。
     * 测量的距离是从指定的点到由这个<code>Line2D</code>定义的无限延伸直线的最近点之间的距离。如果指定的点与直线相交，此方法返回0.0。
     * @param pt 被测量的指定<code>Point2D</code>
     * @return 一个double值，表示从指定的<code>Point2D</code>到当前直线的距离。
     * @see #ptSegDist(Point2D)
     * @since 1.2
     */
    public double ptLineDist(Point2D pt) {
        return ptLineDist(getX1(), getY1(), getX2(), getY2(),
                         pt.getX(), pt.getY());
    }

    /**
     * 测试指定的坐标是否在该<code>Line2D</code>的边界内。此方法是实现<code>Shape</code>接口所必需的，但在<code>Line2D</code>对象的情况下，它总是返回<code>false</code>，因为直线没有面积。
     * @param x 要测试的指定点的X坐标
     * @param y 要测试的指定点的Y坐标
     * @return <code>false</code>，因为<code>Line2D</code>没有面积。
     * @since 1.2
     */
    public boolean contains(double x, double y) {
        return false;
    }

    /**
     * 测试给定的<code>Point2D</code>是否在该<code>Line2D</code>的边界内。
     * 此方法是实现<code>Shape</code>接口所必需的，但在<code>Line2D</code>对象的情况下，它总是返回<code>false</code>，因为直线没有面积。
     * @param p 要测试的指定<code>Point2D</code>
     * @return <code>false</code>，因为<code>Line2D</code>没有面积。
     * @since 1.2
     */
    public boolean contains(Point2D p) {
        return false;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean intersects(double x, double y, double w, double h) {
        return intersects(new Rectangle2D.Double(x, y, w, h));
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean intersects(Rectangle2D r) {
        return r.intersectsLine(getX1(), getY1(), getX2(), getY2());
    }

    /**
     * 测试此<code>Line2D</code>的内部是否完全包含指定的矩形坐标集。
     * 此方法是实现<code>Shape</code>接口所必需的，但在<code>Line2D</code>对象的情况下，它总是返回<code>false</code>，因为直线没有面积。
     * @param x 指定矩形区域左上角的X坐标
     * @param y 指定矩形区域左上角的Y坐标
     * @param w 指定矩形区域的宽度
     * @param h 指定矩形区域的高度
     * @return <code>false</code>，因为<code>Line2D</code>没有面积。
     * @since 1.2
     */
    public boolean contains(double x, double y, double w, double h) {
        return false;
    }

    /**
     * 测试此<code>Line2D</code>的内部是否完全包含指定的<code>Rectangle2D</code>。
     * 此方法是实现<code>Shape</code>接口所必需的，但在<code>Line2D</code>对象的情况下，它总是返回<code>false</code>，因为直线没有面积。
     * @param r 要测试的指定<code>Rectangle2D</code>
     * @return <code>false</code>，因为<code>Line2D</code>没有面积。
     * @since 1.2
     */
    public boolean contains(Rectangle2D r) {
        return false;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public Rectangle getBounds() {
        return getBounds2D().getBounds();
    }

    /**
     * 返回定义此<code>Line2D</code>边界的迭代对象。
     * 该类的迭代器不是多线程安全的，这意味着这个<code>Line2D</code>类不能保证对这个<code>Line2D</code>对象的几何形状的修改不会影响已经进行的几何形状的迭代。
     * @param at 指定的<code>AffineTransform</code>
     * @return 定义此<code>Line2D</code>边界的<code>PathIterator</code>。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at) {
        return new LineIterator(this, at);
    }

    /**
     * 返回定义此展平的<code>Line2D</code>边界的迭代对象。
     * 该类的迭代器不是多线程安全的，这意味着这个<code>Line2D</code>类不能保证对这个<code>Line2D</code>对象的几何形状的修改不会影响已经进行的几何形状的迭代。
     * @param at 指定的<code>AffineTransform</code>
     * @param flatness 控制点对于给定曲线可以偏离共线的最大量，如果超过这个量，将用直线连接端点来替换细分曲线。由于<code>Line2D</code>对象总是平直的，此参数被忽略。
     * @return 定义展平的<code>Line2D</code>边界的<code>PathIterator</code>。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new LineIterator(this, at);
    }

    /**
     * 创建与此对象相同类的新对象。
     *
     * @return 本实例的克隆。
     * @exception  OutOfMemoryError 如果没有足够的内存。
     * @see        java.lang.Cloneable
     * @since      1.2
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们是Cloneable
            throw new InternalError(e);
        }
    }
}
