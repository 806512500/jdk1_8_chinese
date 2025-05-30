
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
 * <CODE>Arc2D</CODE> 是所有存储由框架矩形、起始角度、弧度（弧的长度）和闭合类型（<CODE>OPEN</CODE>、<CODE>CHORD</CODE> 或 <CODE>PIE</CODE>）定义的 2D 弧的对象的抽象超类。
 * <p>
 * <a name="inscribes">
 * 弧是完全椭圆的一部分，该椭圆内切于其父 {@link RectangularShape} 的框架矩形。
 * </a>
 * <a name="angles">
 * 角度相对于非正方形框架矩形指定，使得 45 度始终位于从椭圆中心到框架矩形右上角的线上。
 * 因此，如果框架矩形在一个轴上明显比另一个轴长，弧段的起始和结束角度将沿着框架的较长轴偏移得更远。
 * </a>
 * <p>
 * 坐标的实际存储表示形式留给子类。
 *
 * @author      Jim Graham
 * @since 1.2
 */
public abstract class Arc2D extends RectangularShape {

    /**
     * 无路径段连接弧段两端的开放弧的闭合类型。
     * @since 1.2
     */
    public final static int OPEN = 0;

    /**
     * 通过从弧段的起始点到弧段的结束点绘制直线段来闭合的弧的闭合类型。
     * @since 1.2
     */
    public final static int CHORD = 1;

    /**
     * 通过从弧段的起始点到完整椭圆的中心，再从该点到弧段的结束点绘制直线段来闭合的弧的闭合类型。
     * @since 1.2
     */
    public final static int PIE = 2;

    /**
     * 此类定义了以 {@code float} 精度指定的弧。
     * @since 1.2
     */
    public static class Float extends Arc2D implements Serializable {
        /**
         * 弧的框架矩形的左上角的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float x;

        /**
         * 弧的框架矩形的左上角的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float y;

        /**
         * 此弧所属的完整椭圆的总宽度（不考虑角度范围）。
         * @since 1.2
         * @serial
         */
        public float width;

        /**
         * 此弧所属的完整椭圆的总高度（不考虑角度范围）。
         * @since 1.2
         * @serial
         */
        public float height;

        /**
         * 弧的起始角度（以度为单位）。
         * @since 1.2
         * @serial
         */
        public float start;

        /**
         * 弧的角度范围（以度为单位）。
         * @since 1.2
         * @serial
         */
        public float extent;

        /**
         * 构造一个新的 OPEN 弧，初始化为位置 (0, 0)，大小 (0, 0)，角度范围 (起始 = 0, 范围 = 0)。
         * @since 1.2
         */
        public Float() {
            super(OPEN);
        }

        /**
         * 构造一个新的弧，初始化为位置 (0, 0)，大小 (0, 0)，角度范围 (起始 = 0, 范围 = 0)，并指定闭合类型。
         *
         * @param type 弧的闭合类型：{@link #OPEN}、{@link #CHORD} 或 {@link #PIE}。
         * @since 1.2
         */
        public Float(int type) {
            super(type);
        }

        /**
         * 构造一个新的弧，初始化为指定的位置、大小、角度范围和闭合类型。
         *
         * @param x 弧的框架矩形的左上角的 X 坐标。
         * @param y 弧的框架矩形的左上角的 Y 坐标。
         * @param w 此弧所属的完整椭圆的总宽度。
         * @param h 此弧所属的完整椭圆的总高度。
         * @param start 弧的起始角度（以度为单位）。
         * @param extent 弧的角度范围（以度为单位）。
         * @param type 弧的闭合类型：{@link #OPEN}、{@link #CHORD} 或 {@link #PIE}。
         * @since 1.2
         */
        public Float(float x, float y, float w, float h,
                     float start, float extent, int type) {
            super(type);
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.start = start;
            this.extent = extent;
        }

        /**
         * 构造一个新的弧，初始化为指定的位置、大小、角度范围和闭合类型。
         *
         * @param ellipseBounds 定义此弧所属的完整椭圆的外边界的框架矩形。
         * @param start 弧的起始角度（以度为单位）。
         * @param extent 弧的角度范围（以度为单位）。
         * @param type 弧的闭合类型：{@link #OPEN}、{@link #CHORD} 或 {@link #PIE}。
         * @since 1.2
         */
        public Float(Rectangle2D ellipseBounds,
                     float start, float extent, int type) {
            super(type);
            this.x = (float) ellipseBounds.getX();
            this.y = (float) ellipseBounds.getY();
            this.width = (float) ellipseBounds.getWidth();
            this.height = (float) ellipseBounds.getHeight();
            this.start = start;
            this.extent = extent;
        }

        /**
         * {@inheritDoc}
         * 注意，弧
         * <a href="Arc2D.html#inscribes">部分内切于</a>
         * 此 {@code RectangularShape} 的框架矩形。
         *
         * @since 1.2
         */
        public double getX() {
            return (double) x;
        }

        /**
         * {@inheritDoc}
         * 注意，弧
         * <a href="Arc2D.html#inscribes">部分内切于</a>
         * 此 {@code RectangularShape} 的框架矩形。
         *
         * @since 1.2
         */
        public double getY() {
            return (double) y;
        }

        /**
         * {@inheritDoc}
         * 注意，弧
         * <a href="Arc2D.html#inscribes">部分内切于</a>
         * 此 {@code RectangularShape} 的框架矩形。
         *
         * @since 1.2
         */
        public double getWidth() {
            return (double) width;
        }

        /**
         * {@inheritDoc}
         * 注意，弧
         * <a href="Arc2D.html#inscribes">部分内切于</a>
         * 此 {@code RectangularShape} 的框架矩形。
         *
         * @since 1.2
         */
        public double getHeight() {
            return (double) height;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getAngleStart() {
            return (double) start;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getAngleExtent() {
            return (double) extent;
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
        public void setArc(double x, double y, double w, double h,
                           double angSt, double angExt, int closure) {
            this.setArcType(closure);
            this.x = (float) x;
            this.y = (float) y;
            this.width = (float) w;
            this.height = (float) h;
            this.start = (float) angSt;
            this.extent = (float) angExt;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setAngleStart(double angSt) {
            this.start = (float) angSt;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setAngleExtent(double angExt) {
            this.extent = (float) angExt;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        protected Rectangle2D makeBounds(double x, double y,
                                         double w, double h) {
            return new Rectangle2D.Float((float) x, (float) y,
                                         (float) w, (float) h);
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = 9130893014586380278L;

        /**
         * 将默认的可序列化字段写入 <code>ObjectOutputStream</code>，然后写入一个表示此 <code>Arc2D</code> 实例的弧类型的字节。
         *
         * @serialData
         * <ol>
         * <li>默认的可序列化字段。
         * <li>
         * 然后是一个表示弧类型 {@link #OPEN}、{@link #CHORD} 或 {@link #PIE} 的 <code>byte</code>。
         * </ol>
         */
        private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException
        {
            s.defaultWriteObject();

            s.writeByte(getArcType());
        }

        /**
         * 从 <code>ObjectInputStream</code> 读取默认的可序列化字段，然后读取一个表示此 <code>Arc2D</code> 实例的弧类型的字节。
         *
         * @serialData
         * <ol>
         * <li>默认的可序列化字段。
         * <li>
         * 然后是一个表示弧类型 {@link #OPEN}、{@link #CHORD} 或 {@link #PIE} 的 <code>byte</code>。
         * </ol>
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.lang.ClassNotFoundException, java.io.IOException
        {
            s.defaultReadObject();

            try {
                setArcType(s.readByte());
            } catch (IllegalArgumentException iae) {
                throw new java.io.InvalidObjectException(iae.getMessage());
            }
        }
    }

    /**
     * 此类定义了以 {@code double} 精度指定的弧。
     * @since 1.2
     */
    public static class Double extends Arc2D implements Serializable {
        /**
         * 弧的框架矩形的左上角的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double x;

        /**
         * 弧的框架矩形的左上角的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double y;

        /**
         * 此弧所属的完整椭圆的总宽度（不考虑角度范围）。
         * @since 1.2
         * @serial
         */
        public double width;

        /**
         * 此弧所属的完整椭圆的总高度（不考虑角度范围）。
         * @since 1.2
         * @serial
         */
        public double height;

        /**
         * 弧的起始角度（以度为单位）。
         * @since 1.2
         * @serial
         */
        public double start;

        /**
         * 弧的角度范围（以度为单位）。
         * @since 1.2
         * @serial
         */
        public double extent;

        /**
         * 构造一个新的 OPEN 弧，初始化为位置 (0, 0)，大小 (0, 0)，角度范围 (起始 = 0, 范围 = 0)。
         * @since 1.2
         */
        public Double() {
            super(OPEN);
        }

        /**
         * 构造一个新的弧，初始化为位置 (0, 0)，大小 (0, 0)，角度范围 (起始 = 0, 范围 = 0)，并指定闭合类型。
         *
         * @param type 弧的闭合类型：{@link #OPEN}、{@link #CHORD} 或 {@link #PIE}。
         * @since 1.2
         */
        public Double(int type) {
            super(type);
        }

        /**
         * 构造一个新的弧，初始化为指定的位置、大小、角度范围和闭合类型。
         *
         * @param x 弧的框架矩形的左上角的 X 坐标。
         * @param y 弧的框架矩形的左上角的 Y 坐标。
         * @param w 此弧所属的完整椭圆的总宽度。
         * @param h 此弧所属的完整椭圆的总高度。
         * @param start 弧的起始角度（以度为单位）。
         * @param extent 弧的角度范围（以度为单位）。
         * @param type 弧的闭合类型：{@link #OPEN}、{@link #CHORD} 或 {@link #PIE}。
         * @since 1.2
         */
        public Double(double x, double y, double w, double h,
                      double start, double extent, int type) {
            super(type);
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.start = start;
            this.extent = extent;
        }

        /**
         * 构造一个新的弧，初始化为指定的位置、大小、角度范围和闭合类型。
         *
         * @param ellipseBounds 定义此弧所属的完整椭圆的外边界的框架矩形。
         * @param start 弧的起始角度（以度为单位）。
         * @param extent 弧的角度范围（以度为单位）。
         * @param type 弧的闭合类型：{@link #OPEN}、{@link #CHORD} 或 {@link #PIE}。
         * @since 1.2
         */
        public Double(Rectangle2D ellipseBounds,
                      double start, double extent, int type) {
            super(type);
            this.x = ellipseBounds.getX();
            this.y = ellipseBounds.getY();
            this.width = ellipseBounds.getWidth();
            this.height = ellipseBounds.getHeight();
            this.start = start;
            this.extent = extent;
        }


        /**
         * {@inheritDoc}
         * 注意，弧
         * <a href="Arc2D.html#inscribes">部分内切</a>
         * 此 {@code RectangularShape} 的框架矩形。
         *
         * @since 1.2
         */
        public double getX() {
            return x;
        }

        /**
         * {@inheritDoc}
         * 注意，弧
         * <a href="Arc2D.html#inscribes">部分内切</a>
         * 此 {@code RectangularShape} 的框架矩形。
         *
         * @since 1.2
         */
        public double getY() {
            return y;
        }

        /**
         * {@inheritDoc}
         * 注意，弧
         * <a href="Arc2D.html#inscribes">部分内切</a>
         * 此 {@code RectangularShape} 的框架矩形。
         *
         * @since 1.2
         */
        public double getWidth() {
            return width;
        }

        /**
         * {@inheritDoc}
         * 注意，弧
         * <a href="Arc2D.html#inscribes">部分内切</a>
         * 此 {@code RectangularShape} 的框架矩形。
         *
         * @since 1.2
         */
        public double getHeight() {
            return height;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getAngleStart() {
            return start;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public double getAngleExtent() {
            return extent;
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
        public void setArc(double x, double y, double w, double h,
                           double angSt, double angExt, int closure) {
            this.setArcType(closure);
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.start = angSt;
            this.extent = angExt;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setAngleStart(double angSt) {
            this.start = angSt;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setAngleExtent(double angExt) {
            this.extent = angExt;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        protected Rectangle2D makeBounds(double x, double y,
                                         double w, double h) {
            return new Rectangle2D.Double(x, y, w, h);
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = 728264085846882001L;

        /**
         * 将默认的可序列化字段写入
         * <code>ObjectOutputStream</code>，然后写入一个字节
         * 表示此 <code>Arc2D</code> 实例的弧类型。
         *
         * @serialData
         * <ol>
         * <li>默认的可序列化字段。
         * <li>
         * 然后是一个 <code>byte</code>，表示弧类型
         * {@link #OPEN}, {@link #CHORD} 或 {@link #PIE}。
         * </ol>
         */
        private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException
        {
            s.defaultWriteObject();

            s.writeByte(getArcType());
        }

        /**
         * 从 <code>ObjectInputStream</code> 读取默认的可序列化字段，然后读取一个字节
         * 表示此 <code>Arc2D</code> 实例的弧类型。
         *
         * @serialData
         * <ol>
         * <li>默认的可序列化字段。
         * <li>
         * 然后是一个 <code>byte</code>，表示弧类型
         * {@link #OPEN}, {@link #CHORD} 或 {@link #PIE}。
         * </ol>
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.lang.ClassNotFoundException, java.io.IOException
        {
            s.defaultReadObject();

            try {
                setArcType(s.readByte());
            } catch (IllegalArgumentException iae) {
                throw new java.io.InvalidObjectException(iae.getMessage());
            }
        }
    }

    private int type;

    /**
     * 这是一个抽象类，不能直接实例化。
     * 可以实例化的类型特定实现子类提供多种格式来存储
     * 满足各种访问方法所需的信息。
     * <p>
     * 此构造函数创建一个默认闭合类型为 {@link #OPEN} 的对象。它仅用于
     * 子类的序列化。
     *
     * @see java.awt.geom.Arc2D.Float
     * @see java.awt.geom.Arc2D.Double
     */
    protected Arc2D() {
        this(OPEN);
    }

    /**
     * 这是一个抽象类，不能直接实例化。
     * 可以实例化的类型特定实现子类提供多种格式来存储
     * 满足各种访问方法所需的信息。
     *
     * @param type 此弧的闭合类型：
     * {@link #OPEN}, {@link #CHORD} 或 {@link #PIE}。
     * @see java.awt.geom.Arc2D.Float
     * @see java.awt.geom.Arc2D.Double
     * @since 1.2
     */
    protected Arc2D(int type) {
        setArcType(type);
    }

    /**
     * 返回弧的起始角度。
     *
     * @return 一个表示弧的起始角度的 double 值（以度为单位）。
     * @see #setAngleStart
     * @since 1.2
     */
    public abstract double getAngleStart();

    /**
     * 返回弧的角度范围。
     *
     * @return 一个表示弧的角度范围的 double 值（以度为单位）。
     * @see #setAngleExtent
     * @since 1.2
     */
    public abstract double getAngleExtent();

    /**
     * 返回弧的闭合类型：{@link #OPEN},
     * {@link #CHORD} 或 {@link #PIE}。
     * @return 本类中定义的一个整数常量闭合类型。
     * @see #setArcType
     * @since 1.2
     */
    public int getArcType() {
        return type;
    }

    /**
     * 返回弧的起始点。该点是起始角度定义的射线与弧的椭圆边界相交的点。
     *
     * @return 一个表示弧的起始点的 x,y 坐标的 <CODE>Point2D</CODE> 对象。
     * @since 1.2
     */
    public Point2D getStartPoint() {
        double angle = Math.toRadians(-getAngleStart());
        double x = getX() + (Math.cos(angle) * 0.5 + 0.5) * getWidth();
        double y = getY() + (Math.sin(angle) * 0.5 + 0.5) * getHeight();
        return new Point2D.Double(x, y);
    }

    /**
     * 返回弧的结束点。该点是起始角度加上弧的角度范围定义的射线与弧的椭圆边界相交的点。
     *
     * @return 一个表示弧的结束点的 x,y 坐标的 <CODE>Point2D</CODE> 对象。
     * @since 1.2
     */
    public Point2D getEndPoint() {
        double angle = Math.toRadians(-getAngleStart() - getAngleExtent());
        double x = getX() + (Math.cos(angle) * 0.5 + 0.5) * getWidth();
        double y = getY() + (Math.sin(angle) * 0.5 + 0.5) * getHeight();
        return new Point2D.Double(x, y);
    }

    /**
     * 将此弧的位置、大小、角度范围和闭合类型设置为指定的 double 值。
     *
     * @param x 弧的左上角的 X 坐标。
     * @param y 弧的左上角的 Y 坐标。
     * @param w 完整椭圆的总宽度，其中此弧是部分段。
     * @param h 完整椭圆的总高度，其中此弧是部分段。
     * @param angSt 弧的起始角度（以度为单位）。
     * @param angExt 弧的角度范围（以度为单位）。
     * @param closure 弧的闭合类型：
     * {@link #OPEN}, {@link #CHORD} 或 {@link #PIE}。
     * @since 1.2
     */
    public abstract void setArc(double x, double y, double w, double h,
                                double angSt, double angExt, int closure);

    /**
     * 将此弧的位置、大小、角度范围和闭合类型设置为指定的值。
     *
     * @param loc 表示弧的左上角坐标的 <CODE>Point2D</CODE>。
     * @param size 表示完整椭圆的宽度和高度的 <CODE>Dimension2D</CODE>，其中此弧是部分段。
     * @param angSt 弧的起始角度（以度为单位）。
     * @param angExt 弧的角度范围（以度为单位）。
     * @param closure 弧的闭合类型：
     * {@link #OPEN}, {@link #CHORD} 或 {@link #PIE}。
     * @since 1.2
     */
    public void setArc(Point2D loc, Dimension2D size,
                       double angSt, double angExt, int closure) {
        setArc(loc.getX(), loc.getY(), size.getWidth(), size.getHeight(),
               angSt, angExt, closure);
    }

    /**
     * 将此弧的位置、大小、角度范围和闭合类型设置为指定的值。
     *
     * @param rect 定义完整椭圆外边界的框架矩形，其中此弧是部分段。
     * @param angSt 弧的起始角度（以度为单位）。
     * @param angExt 弧的角度范围（以度为单位）。
     * @param closure 弧的闭合类型：
     * {@link #OPEN}, {@link #CHORD} 或 {@link #PIE}。
     * @since 1.2
     */
    public void setArc(Rectangle2D rect, double angSt, double angExt,
                       int closure) {
        setArc(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(),
               angSt, angExt, closure);
    }

    /**
     * 将此弧设置为与指定的弧相同。
     *
     * @param a 用于设置弧值的 <CODE>Arc2D</CODE>。
     * @since 1.2
     */
    public void setArc(Arc2D a) {
        setArc(a.getX(), a.getY(), a.getWidth(), a.getHeight(),
               a.getAngleStart(), a.getAngleExtent(), a.type);
    }

    /**
     * 将此弧的位置、大小、角度范围和闭合类型设置为指定的值。弧由中心点和半径定义，而不是完整椭圆的框架矩形。
     *
     * @param x 弧的中心的 X 坐标。
     * @param y 弧的中心的 Y 坐标。
     * @param radius 弧的半径。
     * @param angSt 弧的起始角度（以度为单位）。
     * @param angExt 弧的角度范围（以度为单位）。
     * @param closure 弧的闭合类型：
     * {@link #OPEN}, {@link #CHORD} 或 {@link #PIE}。
     * @since 1.2
     */
    public void setArcByCenter(double x, double y, double radius,
                               double angSt, double angExt, int closure) {
        setArc(x - radius, y - radius, radius * 2.0, radius * 2.0,
               angSt, angExt, closure);
    }

    /**
     * 将此弧的位置、大小和角度范围设置为指定的值。弧的起始角度与点 (p1, p2) 定义的线相切，结束角度与点 (p2, p3) 定义的线相切，弧具有指定的半径。
     *
     * @param p1 定义弧的第一个点。弧的起始角度与点 (p1, p2) 定义的线相切。
     * @param p2 定义弧的第二个点。弧的起始角度与点 (p1, p2) 定义的线相切。
     * 弧的结束角度与点 (p2, p3) 定义的线相切。
     * @param p3 定义弧的第三个点。弧的结束角度与点 (p2, p3) 定义的线相切。
     * @param radius 弧的半径。
     * @since 1.2
     */
    public void setArcByTangent(Point2D p1, Point2D p2, Point2D p3,
                                double radius) {
        double ang1 = Math.atan2(p1.getY() - p2.getY(),
                                 p1.getX() - p2.getX());
        double ang2 = Math.atan2(p3.getY() - p2.getY(),
                                 p3.getX() - p2.getX());
        double diff = ang2 - ang1;
        if (diff > Math.PI) {
            ang2 -= Math.PI * 2.0;
        } else if (diff < -Math.PI) {
            ang2 += Math.PI * 2.0;
        }
        double bisect = (ang1 + ang2) / 2.0;
        double theta = Math.abs(ang2 - bisect);
        double dist = radius / Math.sin(theta);
        double x = p2.getX() + dist * Math.cos(bisect);
        double y = p2.getY() + dist * Math.sin(bisect);
        // REMIND: This needs some work...
        if (ang1 < ang2) {
            ang1 -= Math.PI / 2.0;
            ang2 += Math.PI / 2.0;
        } else {
            ang1 += Math.PI / 2.0;
            ang2 -= Math.PI / 2.0;
        }
        ang1 = Math.toDegrees(-ang1);
        ang2 = Math.toDegrees(-ang2);
        diff = ang2 - ang1;
        if (diff < 0) {
            diff += 360;
        } else {
            diff -= 360;
        }
        setArcByCenter(x, y, radius, ang1, diff, type);
    }

    /**
     * 将此弧的起始角度设置为指定的 double 值。
     *
     * @param angSt 弧的起始角度（以度为单位）。
     * @see #getAngleStart
     * @since 1.2
     */
    public abstract void setAngleStart(double angSt);

    /**
     * 将此弧的角度范围设置为指定的 double 值。
     *
     * @param angExt 弧的角度范围（以度为单位）。
     * @see #getAngleExtent
     * @since 1.2
     */
    public abstract void setAngleExtent(double angExt);

    /**
     * 将此弧的起始角度设置为相对于此弧中心的指定点定义的角度。
     * 弧的角度范围将保持不变。
     *
     * @param p 定义起始角度的 <CODE>Point2D</CODE>。
     * @see #getAngleStart
     * @since 1.2
     */
    public void setAngleStart(Point2D p) {
        // 通过椭圆的高度和宽度调整 dx 和 dy。
        double dx = getHeight() * (p.getX() - getCenterX());
        double dy = getWidth() * (p.getY() - getCenterY());
        setAngleStart(-Math.toDegrees(Math.atan2(dy, dx)));
    }

    /**
     * 使用两组坐标设置此弧的起始角度和角度范围。第一组坐标用于确定起始点相对于弧中心的角度。第二组坐标用于确定结束点相对于弧中心的角度。
     * 弧将始终非空，并从第一个点沿逆时针方向延伸到第二个点。
     *
     * @param x1 弧的起始点的 X 坐标。
     * @param y1 弧的起始点的 Y 坐标。
     * @param x2 弧的结束点的 X 坐标。
     * @param y2 弧的结束点的 Y 坐标。
     * @since 1.2
     */
    public void setAngles(double x1, double y1, double x2, double y2) {
        double x = getCenterX();
        double y = getCenterY();
        double w = getWidth();
        double h = getHeight();
        // 注意：反转 Y 方程会否定角度以调整上下颠倒的坐标系。
        // 还应通过椭圆的高度和宽度调整 atan。
        double ang1 = Math.atan2(w * (y - y1), h * (x1 - x));
        double ang2 = Math.atan2(w * (y - y2), h * (x2 - x));
        ang2 -= ang1;
        if (ang2 <= 0.0) {
            ang2 += Math.PI * 2.0;
        }
        setAngleStart(Math.toDegrees(ang1));
        setAngleExtent(Math.toDegrees(ang2));
    }


    /**
     * 使用两个点设置弧的起始角度和角度范围。第一个点用于确定起始点相对于弧中心的角度。
     * 第二个点用于确定终点相对于弧中心的角度。
     * 弧将始终是非空的，并且从第一个点逆时针延伸到第二个点。
     *
     * @param p1 定义弧的起始点的 <CODE>Point2D</CODE>。
     * @param p2 定义弧的结束点的 <CODE>Point2D</CODE>。
     * @since 1.2
     */
    public void setAngles(Point2D p1, Point2D p2) {
        setAngles(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    /**
     * 将此弧的闭合类型设置为指定值：<CODE>OPEN</CODE>、<CODE>CHORD</CODE> 或 <CODE>PIE</CODE>。
     *
     * @param type 表示此弧闭合类型的整数常量：{@link #OPEN}、{@link #CHORD} 或
     * {@link #PIE}。
     *
     * @throws IllegalArgumentException 如果 <code>type</code> 不是 0、1 或 2。
     * @see #getArcType
     * @since 1.2
     */
    public void setArcType(int type) {
        if (type < OPEN || type > PIE) {
            throw new IllegalArgumentException("无效的弧类型: "+type);
        }
        this.type = type;
    }

    /**
     * {@inheritDoc}
     * 注意，弧
     * <a href="Arc2D.html#inscribes">部分内切</a>
     * 此 {@code RectangularShape} 的框架矩形。
     *
     * @since 1.2
     */
    public void setFrame(double x, double y, double w, double h) {
        setArc(x, y, w, h, getAngleStart(), getAngleExtent(), type);
    }

    /**
     * 返回弧的高精度框架矩形。框架矩形仅包含此 <code>Arc2D</code> 从起始角度到结束角度之间的部分，
     * 并且如果此 <code>Arc2D</code> 的闭合类型为 <code>PIE</code>，则包含饼楔。
     * <p>
     * 该方法与
     * {@link RectangularShape#getBounds() getBounds} 的不同之处在于，
     * <code>getBounds</code> 方法仅返回此 <code>Arc2D</code> 的椭圆边界，而不考虑此 <code>Arc2D</code> 的起始和结束角度。
     *
     * @return 代表弧框架矩形的 <CODE>Rectangle2D</CODE>。
     * @since 1.2
     */
    public Rectangle2D getBounds2D() {
        if (isEmpty()) {
            return makeBounds(getX(), getY(), getWidth(), getHeight());
        }
        double x1, y1, x2, y2;
        if (getArcType() == PIE) {
            x1 = y1 = x2 = y2 = 0.0;
        } else {
            x1 = y1 = 1.0;
            x2 = y2 = -1.0;
        }
        double angle = 0.0;
        for (int i = 0; i < 6; i++) {
            if (i < 4) {
                // 0-3 是四个象限
                angle += 90.0;
                if (!containsAngle(angle)) {
                    continue;
                }
            } else if (i == 4) {
                // 4 是起始角度
                angle = getAngleStart();
            } else {
                // 5 是结束角度
                angle += getAngleExtent();
            }
            double rads = Math.toRadians(-angle);
            double xe = Math.cos(rads);
            double ye = Math.sin(rads);
            x1 = Math.min(x1, xe);
            y1 = Math.min(y1, ye);
            x2 = Math.max(x2, xe);
            y2 = Math.max(y2, ye);
        }
        double w = getWidth();
        double h = getHeight();
        x2 = (x2 - x1) * 0.5 * w;
        y2 = (y2 - y1) * 0.5 * h;
        x1 = getX() + (x1 * 0.5 + 0.5) * w;
        y1 = getY() + (y1 * 0.5 + 0.5) * h;
        return makeBounds(x1, y1, x2, y2);
    }

    /**
     * 构造一个适当精度的 <code>Rectangle2D</code>，以容纳计算出的此弧的框架矩形参数。
     *
     * @param x 框架矩形左上角的 X 坐标。
     * @param y 框架矩形左上角的 Y 坐标。
     * @param w 框架矩形的宽度。
     * @param h 框架矩形的高度。
     * @return 代表此弧框架矩形的 <code>Rectangle2D</code>。
     * @since 1.2
     */
    protected abstract Rectangle2D makeBounds(double x, double y,
                                              double w, double h);

    /*
     * 将指定的角度归一化到 -180 到 180 的范围内。
     */
    static double normalizeDegrees(double angle) {
        if (angle > 180.0) {
            if (angle <= (180.0 + 360.0)) {
                angle = angle - 360.0;
            } else {
                angle = Math.IEEEremainder(angle, 360.0);
                // IEEEremainder 可能会为某些输入值返回 -180...
                if (angle == -180.0) {
                    angle = 180.0;
                }
            }
        } else if (angle <= -180.0) {
            if (angle > (-180.0 - 360.0)) {
                angle = angle + 360.0;
            } else {
                angle = Math.IEEEremainder(angle, 360.0);
                // IEEEremainder 可能会为某些输入值返回 -180...
                if (angle == -180.0) {
                    angle = 180.0;
                }
            }
        }
        return angle;
    }

    /**
     * 确定指定的角度是否在弧的角度范围内。
     *
     * @param angle 要测试的角度。
     *
     * @return 如果弧包含角度，则返回 <CODE>true</CODE>；如果弧不包含角度，则返回 <CODE>false</CODE>。
     * @since 1.2
     */
    public boolean containsAngle(double angle) {
        double angExt = getAngleExtent();
        boolean backwards = (angExt < 0.0);
        if (backwards) {
            angExt = -angExt;
        }
        if (angExt >= 360.0) {
            return true;
        }
        angle = normalizeDegrees(angle) - normalizeDegrees(getAngleStart());
        if (backwards) {
            angle = -angle;
        }
        if (angle < 0.0) {
            angle += 360.0;
        }

        return (angle >= 0.0) && (angle < angExt);
    }

    /**
     * 确定指定的点是否在弧的边界内。
     *
     * @param x 要测试的点的 X 坐标。
     * @param y 要测试的点的 Y 坐标。
     *
     * @return 如果点在弧的边界内，则返回 <CODE>true</CODE>；如果点在弧的边界外，则返回 <CODE>false</CODE>。
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
        double distSq = (normx * normx + normy * normy);
        if (distSq >= 0.25) {
            return false;
        }
        double angExt = Math.abs(getAngleExtent());
        if (angExt >= 360.0) {
            return true;
        }
        boolean inarc = containsAngle(-Math.toDegrees(Math.atan2(normy,
                                                                 normx)));
        if (type == PIE) {
            return inarc;
        }
        // CHORD 和 OPEN 的行为相同
        if (inarc) {
            if (angExt >= 180.0) {
                return true;
            }
            // 点必须在“饼三角形”之外
        } else {
            if (angExt <= 180.0) {
                return false;
            }
            // 点必须在“饼三角形”之内
        }
        // 点在饼三角形内当且仅当它在连接弧端点的线的同一侧
        double angle = Math.toRadians(-getAngleStart());
        double x1 = Math.cos(angle);
        double y1 = Math.sin(angle);
        angle += Math.toRadians(-getAngleExtent());
        double x2 = Math.cos(angle);
        double y2 = Math.sin(angle);
        boolean inside = (Line2D.relativeCCW(x1, y1, x2, y2, 2*normx, 2*normy) *
                          Line2D.relativeCCW(x1, y1, x2, y2, 0, 0) >= 0);
        return inarc ? !inside : inside;
    }

    /**
     * 确定弧的内部是否与指定矩形的内部相交。
     *
     * @param x 矩形左上角的 X 坐标。
     * @param y 矩形左上角的 Y 坐标。
     * @param w 矩形的宽度。
     * @param h 矩形的高度。
     *
     * @return 如果弧与矩形相交，则返回 <CODE>true</CODE>；如果弧不与矩形相交，则返回 <CODE>false</CODE>。
     * @since 1.2
     */
    public boolean intersects(double x, double y, double w, double h) {

        double aw = getWidth();
        double ah = getHeight();

        if ( w <= 0 || h <= 0 || aw <= 0 || ah <= 0 ) {
            return false;
        }
        double ext = getAngleExtent();
        if (ext == 0) {
            return false;
        }

        double ax  = getX();
        double ay  = getY();
        double axw = ax + aw;
        double ayh = ay + ah;
        double xw  = x + w;
        double yh  = y + h;

        // 检查边界框
        if (x >= axw || y >= ayh || xw <= ax || yh <= ay) {
            return false;
        }

        // 提取必要数据
        double axc = getCenterX();
        double ayc = getCenterY();
        Point2D sp = getStartPoint();
        Point2D ep = getEndPoint();
        double sx = sp.getX();
        double sy = sp.getY();
        double ex = ep.getX();
        double ey = ep.getY();

        /*
         * 尝试捕捉与弧相交但在左上角坐标
         * (min(center x, start point x, end point x),
         *  min(center y, start point y, end point y))
         * 和右下角坐标
         * (max(center x, start point x, end point x),
         *  max(center y, start point y, end point y))
         * 之外的矩形。
         * 因此我们将检查上述矩形之外的轴段。
         */
        if (ayc >= y && ayc <= yh) { // 0 和 180
            if ((sx < xw && ex < xw && axc < xw &&
                 axw > x && containsAngle(0)) ||
                (sx > x && ex > x && axc > x &&
                 ax < xw && containsAngle(180))) {
                return true;
            }
        }
        if (axc >= x && axc <= xw) { // 90 和 270
            if ((sy > y && ey > y && ayc > y &&
                 ay < yh && containsAngle(90)) ||
                (sy < yh && ey < yh && ayc < yh &&
                 ayh > y && containsAngle(270))) {
                return true;
            }
        }

        /*
         * 对于 PIE，我们应该检查与饼片的交点；
         * 同样，对于角度范围大于 180 的弧，我们也应该这样做，
         * 因为我们需要覆盖位于弧中心和弦之间但不与弦相交的矩形的情况。
         */
        Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);
        if (type == PIE || Math.abs(ext) > 180) {
            // 对于 PIE：尝试找到与饼片的交点
            if (rect.intersectsLine(axc, ayc, sx, sy) ||
                rect.intersectsLine(axc, ayc, ex, ey)) {
                return true;
            }
        } else {
            // 对于 CHORD 和 OPEN：尝试找到与弦的交点
            if (rect.intersectsLine(sx, sy, ex, ey)) {
                return true;
            }
        }

        // 最后检查矩形的角点是否在弧内
        if (contains(x, y) || contains(x + w, y) ||
            contains(x, y + h) || contains(x + w, y + h)) {
            return true;
        }

        return false;
    }

    /**
     * 确定弧的内部是否完全包含指定的矩形。
     *
     * @param x 矩形左上角的 X 坐标。
     * @param y 矩形左上角的 Y 坐标。
     * @param w 矩形的宽度。
     * @param h 矩形的高度。
     *
     * @return 如果弧包含矩形，则返回 <CODE>true</CODE>；如果弧不包含矩形，则返回 <CODE>false</CODE>。
     * @since 1.2
     */
    public boolean contains(double x, double y, double w, double h) {
        return contains(x, y, w, h, null);
    }

    /**
     * 确定弧的内部是否完全包含指定的矩形。
     *
     * @param r 要测试的 <CODE>Rectangle2D</CODE>。
     *
     * @return 如果弧包含矩形，则返回 <CODE>true</CODE>；如果弧不包含矩形，则返回 <CODE>false</CODE>。
     * @since 1.2
     */
    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight(), r);
    }

    private boolean contains(double x, double y, double w, double h,
                             Rectangle2D origrect) {
        if (!(contains(x, y) &&
              contains(x + w, y) &&
              contains(x, y + h) &&
              contains(x + w, y + h))) {
            return false;
        }
        // 如果形状是凸的，则我们已经完成了所有需要的测试。
        // 只有角度范围大于 180 度的 PIE 弧可能是凹的。
        if (type != PIE || Math.abs(getAngleExtent()) <= 180.0) {
            return true;
        }
        // 对于 PIE 形状，我们有一个额外的测试，用于角度范围大于 180 度且所有四个矩形角点都在形状内，
        // 但矩形的一条边跨越了弧的“缺失楔形”的情况。
        // 我们可以通过检查矩形是否与任一饼角度段相交来测试这种情况。
        if (origrect == null) {
            origrect = new Rectangle2D.Double(x, y, w, h);
        }
        double halfW = getWidth() / 2.0;
        double halfH = getHeight() / 2.0;
        double xc = getX() + halfW;
        double yc = getY() + halfH;
        double angle = Math.toRadians(-getAngleStart());
        double xe = xc + halfW * Math.cos(angle);
        double ye = yc + halfH * Math.sin(angle);
        if (origrect.intersectsLine(xc, yc, xe, ye)) {
            return false;
        }
        angle += Math.toRadians(-getAngleExtent());
        xe = xc + halfW * Math.cos(angle);
        ye = yc + halfH * Math.sin(angle);
        return !origrect.intersectsLine(xc, yc, xe, ye);
    }

    /**
     * 返回定义弧边界的迭代对象。
     * 此迭代器是多线程安全的。
     * <code>Arc2D</code> 保证对弧几何的修改不会影响任何正在进行的几何迭代。
     *
     * @param at 一个可选的 <CODE>AffineTransform</CODE>，用于在迭代中返回坐标时应用，或 null 表示返回未变换的坐标。
     *
     * @return 定义弧边界的 <CODE>PathIterator</CODE>。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at) {
        return new ArcIterator(this, at);
    }


                /**
     * 返回此 <code>Arc2D</code> 的哈希码。
     * @return 此 <code>Arc2D</code> 的哈希码。
     * @since 1.6
     */
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits += java.lang.Double.doubleToLongBits(getY()) * 37;
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
        bits += java.lang.Double.doubleToLongBits(getAngleStart()) * 53;
        bits += java.lang.Double.doubleToLongBits(getAngleExtent()) * 59;
        bits += getArcType() * 61;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * 确定指定的 <code>Object</code> 是否与此 <code>Arc2D</code> 相等。如果指定的
     * <code>Object</code> 是 <code>Arc2D</code> 的实例，并且其位置、大小、弧度范围和类型与此
     * <code>Arc2D</code> 相同，则认为它是相等的。
     * @param obj  要与此 <code>Arc2D</code> 进行比较的 <code>Object</code>。
     * @return  如果 <code>obj</code> 是 <code>Arc2D</code> 的实例且具有相同的值，则返回
     *          <code>true</code>；否则返回 <code>false</code>。
     * @since 1.6
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Arc2D) {
            Arc2D a2d = (Arc2D) obj;
            return ((getX() == a2d.getX()) &&
                    (getY() == a2d.getY()) &&
                    (getWidth() == a2d.getWidth()) &&
                    (getHeight() == a2d.getHeight()) &&
                    (getAngleStart() == a2d.getAngleStart()) &&
                    (getAngleExtent() == a2d.getAngleExtent()) &&
                    (getArcType() == a2d.getArcType()));
        }
        return false;
    }
}
