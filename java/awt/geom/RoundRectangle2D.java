
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
 * <code>RoundRectangle2D</code> 类定义了一个由位置 {@code (x,y)}、尺寸 {@code (w x h)} 和用于圆角的弧的宽度和高度定义的矩形。
 * <p>
 * 这个类是所有存储2D圆角矩形的对象的抽象超类。
 * 实际的坐标存储表示留给子类。
 *
 * @author      Jim Graham
 * @since 1.2
 */
public abstract class RoundRectangle2D extends RectangularShape {

    /**
     * <code>Float</code> 类定义了一个所有坐标均为 <code>float</code> 类型的圆角矩形。
     * @since 1.2
     */
    public static class Float extends RoundRectangle2D
        implements Serializable
    {
        /**
         * 此 <code>RoundRectangle2D</code> 的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float x;

        /**
         * 此 <code>RoundRectangle2D</code> 的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float y;

        /**
         * 此 <code>RoundRectangle2D</code> 的宽度。
         * @since 1.2
         * @serial
         */
        public float width;

        /**
         * 此 <code>RoundRectangle2D</code> 的高度。
         * @since 1.2
         * @serial
         */
        public float height;

        /**
         * 用于圆角的弧的宽度。
         * @since 1.2
         * @serial
         */
        public float arcwidth;

        /**
         * 用于圆角的弧的高度。
         * @since 1.2
         * @serial
         */
        public float archeight;

        /**
         * 构造一个新的 <code>RoundRectangle2D</code>，初始化为位置 (0.0,&nbsp;0.0)，尺寸 (0.0,&nbsp;0.0)，圆角弧半径为 0.0。
         * @since 1.2
         */
        public Float() {
        }

        /**
         * 从指定的 <code>float</code> 坐标构造并初始化一个 <code>RoundRectangle2D</code>。
         *
         * @param x 新构造的 <code>RoundRectangle2D</code> 的 X 坐标
         * @param y 新构造的 <code>RoundRectangle2D</code> 的 Y 坐标
         * @param w 新构造的 <code>RoundRectangle2D</code> 的宽度
         * @param h 新构造的 <code>RoundRectangle2D</code> 的高度
         * @param arcw 用于圆角的弧的宽度
         * @param arch 用于圆角的弧的高度
         * @since 1.2
         */
        public Float(float x, float y, float w, float h,
                     float arcw, float arch)
        {
            setRoundRect(x, y, w, h, arcw, arch);
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
        public double getArcWidth() {
            return (double) arcwidth;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getArcHeight() {
            return (double) archeight;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public boolean isEmpty() {
            return (width <= 0.0f) || (height <= 0.0f);
        }

        /**
         * 将此 <code>RoundRectangle2D</code> 的位置、尺寸和圆角半径设置为指定的 <code>float</code> 值。
         *
         * @param x 要设置为此 <code>RoundRectangle2D</code> 的位置的 X 坐标
         * @param y 要设置为此 <code>RoundRectangle2D</code> 的位置的 Y 坐标
         * @param w 要设置为此 <code>RoundRectangle2D</code> 的宽度
         * @param h 要设置为此 <code>RoundRectangle2D</code> 的高度
         * @param arcw 要设置为此 <code>RoundRectangle2D</code> 的弧的宽度
         * @param arch 要设置为此 <code>RoundRectangle2D</code> 的弧的高度
         * @since 1.2
         */
        public void setRoundRect(float x, float y, float w, float h,
                                 float arcw, float arch)
        {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.arcwidth = arcw;
            this.archeight = arch;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setRoundRect(double x, double y, double w, double h,
                                 double arcw, double arch)
        {
            this.x = (float) x;
            this.y = (float) y;
            this.width = (float) w;
            this.height = (float) h;
            this.arcwidth = (float) arcw;
            this.archeight = (float) arch;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setRoundRect(RoundRectangle2D rr) {
            this.x = (float) rr.getX();
            this.y = (float) rr.getY();
            this.width = (float) rr.getWidth();
            this.height = (float) rr.getHeight();
            this.arcwidth = (float) rr.getArcWidth();
            this.archeight = (float) rr.getArcHeight();
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
        private static final long serialVersionUID = -3423150618393866922L;
    }

    /**
     * <code>Double</code> 类定义了一个所有坐标均为 <code>double</code> 类型的圆角矩形。
     * @since 1.2
     */
    public static class Double extends RoundRectangle2D
        implements Serializable
    {
        /**
         * 此 <code>RoundRectangle2D</code> 的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double x;

        /**
         * 此 <code>RoundRectangle2D</code> 的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double y;

        /**
         * 此 <code>RoundRectangle2D</code> 的宽度。
         * @since 1.2
         * @serial
         */
        public double width;

        /**
         * 此 <code>RoundRectangle2D</code> 的高度。
         * @since 1.2
         * @serial
         */
        public double height;

        /**
         * 用于圆角的弧的宽度。
         * @since 1.2
         * @serial
         */
        public double arcwidth;

        /**
         * 用于圆角的弧的高度。
         * @since 1.2
         * @serial
         */
        public double archeight;

        /**
         * 构造一个新的 <code>RoundRectangle2D</code>，初始化为位置 (0.0,&nbsp;0.0)，尺寸 (0.0,&nbsp;0.0)，圆角弧半径为 0.0。
         * @since 1.2
         */
        public Double() {
        }

        /**
         * 从指定的 <code>double</code> 坐标构造并初始化一个 <code>RoundRectangle2D</code>。
         *
         * @param x 新构造的 <code>RoundRectangle2D</code> 的 X 坐标
         * @param y 新构造的 <code>RoundRectangle2D</code> 的 Y 坐标
         * @param w 新构造的 <code>RoundRectangle2D</code> 的宽度
         * @param h 新构造的 <code>RoundRectangle2D</code> 的高度
         * @param arcw 用于圆角的弧的宽度
         * @param arch 用于圆角的弧的高度
         * @since 1.2
         */
        public Double(double x, double y, double w, double h,
                      double arcw, double arch)
        {
            setRoundRect(x, y, w, h, arcw, arch);
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
        public double getArcWidth() {
            return arcwidth;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getArcHeight() {
            return archeight;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public boolean isEmpty() {
            return (width <= 0.0f) || (height <= 0.0f);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setRoundRect(double x, double y, double w, double h,
                                 double arcw, double arch)
        {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.arcwidth = arcw;
            this.archeight = arch;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setRoundRect(RoundRectangle2D rr) {
            this.x = rr.getX();
            this.y = rr.getY();
            this.width = rr.getWidth();
            this.height = rr.getHeight();
            this.arcwidth = rr.getArcWidth();
            this.archeight = rr.getArcHeight();
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
        private static final long serialVersionUID = 1048939333485206117L;
    }

    /**
     * 这是一个抽象类，不能直接实例化。
     * 提供了类型特定的实现子类，可以实例化并提供多种格式来存储满足以下各种访问方法所需的信息。
     *
     * @see java.awt.geom.RoundRectangle2D.Float
     * @see java.awt.geom.RoundRectangle2D.Double
     * @since 1.2
     */
    protected RoundRectangle2D() {
    }

    /**
     * 获取用于圆角的弧的宽度。
     * @return 用于圆角的弧的宽度
     * @since 1.2
     */
    public abstract double getArcWidth();

    /**
     * 获取用于圆角的弧的高度。
     * @return 用于圆角的弧的高度
     * @since 1.2
     */
    public abstract double getArcHeight();

    /**
     * 将此 <code>RoundRectangle2D</code> 的位置、尺寸和圆角半径设置为指定的 <code>double</code> 值。
     *
     * @param x 要设置为此 <code>RoundRectangle2D</code> 的位置的 X 坐标
     * @param y 要设置为此 <code>RoundRectangle2D</code> 的位置的 Y 坐标
     * @param w 要设置为此 <code>RoundRectangle2D</code> 的宽度
     * @param h 要设置为此 <code>RoundRectangle2D</code> 的高度
     * @param arcWidth 要设置为此 <code>RoundRectangle2D</code> 的弧的宽度
     * @param arcHeight 要设置为此 <code>RoundRectangle2D</code> 的弧的高度
     * @since 1.2
     */
    public abstract void setRoundRect(double x, double y, double w, double h,
                                      double arcWidth, double arcHeight);

    /**
     * 将此 <code>RoundRectangle2D</code> 设置为与指定的 <code>RoundRectangle2D</code> 相同。
     * @param rr 指定的 <code>RoundRectangle2D</code>
     * @since 1.2
     */
    public void setRoundRect(RoundRectangle2D rr) {
        setRoundRect(rr.getX(), rr.getY(), rr.getWidth(), rr.getHeight(),
                     rr.getArcWidth(), rr.getArcHeight());
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public void setFrame(double x, double y, double w, double h) {
        setRoundRect(x, y, w, h, getArcWidth(), getArcHeight());
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(double x, double y) {
        if (isEmpty()) {
            return false;
        }
        double rrx0 = getX();
        double rry0 = getY();
        double rrx1 = rrx0 + getWidth();
        double rry1 = rry0 + getHeight();
        // 检查是否可以简单拒绝 - 点在边界矩形之外
        if (x < rrx0 || y < rry0 || x >= rrx1 || y >= rry1) {
            return false;
        }
        double aw = Math.min(getWidth(), Math.abs(getArcWidth())) / 2.0;
        double ah = Math.min(getHeight(), Math.abs(getArcHeight())) / 2.0;
        // 检查点在哪个角，并进行圆形包含测试 - 否则简单接受
        if (x >= (rrx0 += aw) && x < (rrx0 = rrx1 - aw)) {
            return true;
        }
        if (y >= (rry0 += ah) && y < (rry0 = rry1 - ah)) {
            return true;
        }
        x = (x - rrx0) / aw;
        y = (y - rry0) / ah;
        return (x * x + y * y <= 1.0);
    }


                private int classify(double coord, double left, double right,
                         double arcsize)
    {
        if (coord < left) {
            return 0;
        } else if (coord < left + arcsize) {
            return 1;
        } else if (coord < right - arcsize) {
            return 2;
        } else if (coord < right) {
            return 3;
        } else {
            return 4;
        }
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean intersects(double x, double y, double w, double h) {
        if (isEmpty() || w <= 0 || h <= 0) {
            return false;
        }
        double rrx0 = getX();
        double rry0 = getY();
        double rrx1 = rrx0 + getWidth();
        double rry1 = rry0 + getHeight();
        // 检查是否可以简单拒绝 - 边界矩形不相交
        if (x + w <= rrx0 || x >= rrx1 || y + h <= rry0 || y >= rry1) {
            return false;
        }
        double aw = Math.min(getWidth(), Math.abs(getArcWidth())) / 2.0;
        double ah = Math.min(getHeight(), Math.abs(getArcHeight())) / 2.0;
        int x0class = classify(x, rrx0, rrx1, aw);
        int x1class = classify(x + w, rrx0, rrx1, aw);
        int y0class = classify(y, rry0, rry1, ah);
        int y1class = classify(y + h, rry0, rry1, ah);
        // 如果任何点在内部矩形内，则简单接受
        if (x0class == 2 || x1class == 2 || y0class == 2 || y1class == 2) {
            return true;
        }
        // 如果任一边跨越内部矩形，则简单接受
        if ((x0class < 2 && x1class > 2) || (y0class < 2 && y1class > 2)) {
            return true;
        }
        // 由于任一边都不跨越中心，那么其中一个角
        // 必须在一个圆角中。我们通过检测 [xy]0class 是否为 3 或 [xy]1class 是否为 1 来检测这种情况。
        // 这两种情况中的一种必须在每个方向上为真。
        // 现在我们找到一个“最近点”来测试是否在圆角内。
        x = (x1class == 1) ? (x = x + w - (rrx0 + aw)) : (x = x - (rrx1 - aw));
        y = (y1class == 1) ? (y = y + h - (rry0 + ah)) : (y = y - (rry1 - ah));
        x = x / aw;
        y = y / ah;
        return (x * x + y * y <= 1.0);
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(double x, double y, double w, double h) {
        if (isEmpty() || w <= 0 || h <= 0) {
            return false;
        }
        return (contains(x, y) &&
                contains(x + w, y) &&
                contains(x, y + h) &&
                contains(x + w, y + h));
    }

    /**
     * 返回一个定义此 <code>RoundRectangle2D</code> 边界的迭代对象。
     * 该迭代器是多线程安全的，这意味着
     * 此 <code>RoundRectangle2D</code> 类保证对
     * 此 <code>RoundRectangle2D</code> 几何形状的修改不会影响任何正在进行的迭代。
     * @param at 一个可选的 <code>AffineTransform</code>，用于在迭代返回坐标时应用，
     * 或者如果需要未变换的坐标，则为 <code>null</code>
     * @return 返回一个 <code>PathIterator</code> 对象，该对象逐段返回
     * 此 <code>RoundRectangle2D</code> 的轮廓几何形状。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at) {
        return new RoundRectIterator(this, at);
    }

    /**
     * 返回此 <code>RoundRectangle2D</code> 的哈希码。
     * @return 此 <code>RoundRectangle2D</code> 的哈希码。
     * @since 1.6
     */
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits += java.lang.Double.doubleToLongBits(getY()) * 37;
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
        bits += java.lang.Double.doubleToLongBits(getArcWidth()) * 53;
        bits += java.lang.Double.doubleToLongBits(getArcHeight()) * 59;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * 确定指定的 <code>Object</code> 是否等于此 <code>RoundRectangle2D</code>。
     * 指定的 <code>Object</code> 等于此 <code>RoundRectangle2D</code>
     * 如果它是 <code>RoundRectangle2D</code> 的实例，并且其位置、大小和圆角尺寸与此
     * <code>RoundRectangle2D</code> 相同。
     * @param obj 要与此 <code>RoundRectangle2D</code> 比较的 <code>Object</code>。
     * @return 如果 <code>obj</code> 是 <code>RoundRectangle2D</code> 的实例并且具有相同的值，则返回 <code>true</code>；
     * 否则返回 <code>false</code>。
     * @since 1.6
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof RoundRectangle2D) {
            RoundRectangle2D rr2d = (RoundRectangle2D) obj;
            return ((getX() == rr2d.getX()) &&
                    (getY() == rr2d.getY()) &&
                    (getWidth() == rr2d.getWidth()) &&
                    (getHeight() == rr2d.getHeight()) &&
                    (getArcWidth() == rr2d.getArcWidth()) &&
                    (getArcHeight() == rr2d.getArcHeight()));
        }
        return false;
    }
}
