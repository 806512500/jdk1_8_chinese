
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
 * <code>Rectangle2D</code> 类描述了一个由位置 {@code (x,y)} 和尺寸
 * {@code (w x h)} 定义的矩形。
 * <p>
 * 这个类是所有存储 2D 矩形的对象的抽象超类。
 * 坐标的实际存储表示形式由子类决定。
 *
 * @author      Jim Graham
 * @since 1.2
 */
public abstract class Rectangle2D extends RectangularShape {
    /**
     * 表示一个点位于此 <code>Rectangle2D</code> 左侧的位掩码。
     * @since 1.2
     */
    public static final int OUT_LEFT = 1;

    /**
     * 表示一个点位于此 <code>Rectangle2D</code> 上方的位掩码。
     * @since 1.2
     */
    public static final int OUT_TOP = 2;

    /**
     * 表示一个点位于此 <code>Rectangle2D</code> 右侧的位掩码。
     * @since 1.2
     */
    public static final int OUT_RIGHT = 4;

    /**
     * 表示一个点位于此 <code>Rectangle2D</code> 下方的位掩码。
     * @since 1.2
     */
    public static final int OUT_BOTTOM = 8;

    /**
     * <code>Float</code> 类定义了一个用浮点坐标指定的矩形。
     * @since 1.2
     */
    public static class Float extends Rectangle2D implements Serializable {
        /**
         * 此 <code>Rectangle2D</code> 的 X 坐标。
         * @since 1.2
         * @serial
         */
        public float x;

        /**
         * 此 <code>Rectangle2D</code> 的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public float y;

        /**
         * 此 <code>Rectangle2D</code> 的宽度。
         * @since 1.2
         * @serial
         */
        public float width;

        /**
         * 此 <code>Rectangle2D</code> 的高度。
         * @since 1.2
         * @serial
         */
        public float height;

        /**
         * 构造一个新的 <code>Rectangle2D</code>，初始化为位置 (0.0,&nbsp;0.0) 和大小 (0.0,&nbsp;0.0)。
         * @since 1.2
         */
        public Float() {
        }

        /**
         * 从指定的 <code>float</code> 坐标构造并初始化一个 <code>Rectangle2D</code>。
         *
         * @param x 新构造的 <code>Rectangle2D</code> 的左上角 X 坐标
         * @param y 新构造的 <code>Rectangle2D</code> 的左上角 Y 坐标
         * @param w 新构造的 <code>Rectangle2D</code> 的宽度
         * @param h 新构造的 <code>Rectangle2D</code> 的高度
         * @since 1.2
        */
        public Float(float x, float y, float w, float h) {
            setRect(x, y, w, h);
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
            return (width <= 0.0f) || (height <= 0.0f);
        }

        /**
         * 将此 <code>Rectangle2D</code> 的位置和大小设置为指定的 <code>float</code> 值。
         *
         * @param x 此 <code>Rectangle2D</code> 的左上角 X 坐标
         * @param y 此 <code>Rectangle2D</code> 的左上角 Y 坐标
         * @param w 此 <code>Rectangle2D</code> 的宽度
         * @param h 此 <code>Rectangle2D</code> 的高度
         * @since 1.2
         */
        public void setRect(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setRect(double x, double y, double w, double h) {
            this.x = (float) x;
            this.y = (float) y;
            this.width = (float) w;
            this.height = (float) h;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setRect(Rectangle2D r) {
            this.x = (float) r.getX();
            this.y = (float) r.getY();
            this.width = (float) r.getWidth();
            this.height = (float) r.getHeight();
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public int outcode(double x, double y) {
            /*
             * 关于下面转换为 double 的注释。如果 x+w 或 y+h 的算术运算在 float 中进行，
             * 则如果 x/y 和 w/h 的二进制指数不相似，可能会丢失一些位。通过在加法之前转换为 double，
             * 我们强制加法在 double 中进行，以避免比较中的舍入误差。
             *
             * 请参阅 bug 4320890 了解这种不准确性引起的问题。
             */
            int out = 0;
            if (this.width <= 0) {
                out |= OUT_LEFT | OUT_RIGHT;
            } else if (x < this.x) {
                out |= OUT_LEFT;
            } else if (x > this.x + (double) this.width) {
                out |= OUT_RIGHT;
            }
            if (this.height <= 0) {
                out |= OUT_TOP | OUT_BOTTOM;
            } else if (y < this.y) {
                out |= OUT_TOP;
            } else if (y > this.y + (double) this.height) {
                out |= OUT_BOTTOM;
            }
            return out;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D getBounds2D() {
            return new Float(x, y, width, height);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D createIntersection(Rectangle2D r) {
            Rectangle2D dest;
            if (r instanceof Float) {
                dest = new Rectangle2D.Float();
            } else {
                dest = new Rectangle2D.Double();
            }
            Rectangle2D.intersect(this, r, dest);
            return dest;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D createUnion(Rectangle2D r) {
            Rectangle2D dest;
            if (r instanceof Float) {
                dest = new Rectangle2D.Float();
            } else {
                dest = new Rectangle2D.Double();
            }
            Rectangle2D.union(this, r, dest);
            return dest;
        }

        /**
         * 返回此 <code>Rectangle2D</code> 的 <code>String</code> 表示形式。
         * @return 一个表示此 <code>Rectangle2D</code> 的 <code>String</code>。
         * @since 1.2
         */
        public String toString() {
            return getClass().getName()
                + "[x=" + x +
                ",y=" + y +
                ",w=" + width +
                ",h=" + height + "]";
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = 3798716824173675777L;
    }

    /**
     * <code>Double</code> 类定义了一个用双精度坐标指定的矩形。
     * @since 1.2
     */
    public static class Double extends Rectangle2D implements Serializable {
        /**
         * 此 <code>Rectangle2D</code> 的 X 坐标。
         * @since 1.2
         * @serial
         */
        public double x;

        /**
         * 此 <code>Rectangle2D</code> 的 Y 坐标。
         * @since 1.2
         * @serial
         */
        public double y;

        /**
         * 此 <code>Rectangle2D</code> 的宽度。
         * @since 1.2
         * @serial
         */
        public double width;

        /**
         * 此 <code>Rectangle2D</code> 的高度。
         * @since 1.2
         * @serial
         */
        public double height;

        /**
         * 构造一个新的 <code>Rectangle2D</code>，初始化为位置 (0,&nbsp;0) 和大小 (0,&nbsp;0)。
         * @since 1.2
         */
        public Double() {
        }

        /**
         * 从指定的 <code>double</code> 坐标构造并初始化一个 <code>Rectangle2D</code>。
         *
         * @param x 新构造的 <code>Rectangle2D</code> 的左上角 X 坐标
         * @param y 新构造的 <code>Rectangle2D</code> 的左上角 Y 坐标
         * @param w 新构造的 <code>Rectangle2D</code> 的宽度
         * @param h 新构造的 <code>Rectangle2D</code> 的高度
         * @since 1.2
         */
        public Double(double x, double y, double w, double h) {
            setRect(x, y, w, h);
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
            return (width <= 0.0) || (height <= 0.0);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setRect(double x, double y, double w, double h) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public void setRect(Rectangle2D r) {
            this.x = r.getX();
            this.y = r.getY();
            this.width = r.getWidth();
            this.height = r.getHeight();
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public int outcode(double x, double y) {
            int out = 0;
            if (this.width <= 0) {
                out |= OUT_LEFT | OUT_RIGHT;
            } else if (x < this.x) {
                out |= OUT_LEFT;
            } else if (x > this.x + this.width) {
                out |= OUT_RIGHT;
            }
            if (this.height <= 0) {
                out |= OUT_TOP | OUT_BOTTOM;
            } else if (y < this.y) {
                out |= OUT_TOP;
            } else if (y > this.y + this.height) {
                out |= OUT_BOTTOM;
            }
            return out;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D getBounds2D() {
            return new Double(x, y, width, height);
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D createIntersection(Rectangle2D r) {
            Rectangle2D dest = new Rectangle2D.Double();
            Rectangle2D.intersect(this, r, dest);
            return dest;
        }

        /**
         * {@inheritDoc}
         * @since 1.2
         */
        public Rectangle2D createUnion(Rectangle2D r) {
            Rectangle2D dest = new Rectangle2D.Double();
            Rectangle2D.union(this, r, dest);
            return dest;
        }

        /**
         * 返回此 <code>Rectangle2D</code> 的 <code>String</code> 表示形式。
         * @return 一个表示此 <code>Rectangle2D</code> 的 <code>String</code>。
         * @since 1.2
         */
        public String toString() {
            return getClass().getName()
                + "[x=" + x +
                ",y=" + y +
                ",w=" + width +
                ",h=" + height + "]";
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = 7771313791441850493L;
    }

    /**
     * 这是一个抽象类，不能直接实例化。
     * 提供了类型特定的实现子类，可以实例化并提供多种格式来存储
     * 满足以下各种访问方法所需的信息。
     *
     * @see java.awt.geom.Rectangle2D.Float
     * @see java.awt.geom.Rectangle2D.Double
     * @see java.awt.Rectangle
     * @since 1.2
     */
    protected Rectangle2D() {
    }

    /**
     * 将此 <code>Rectangle2D</code> 的位置和大小设置为指定的 <code>double</code> 值。
     *
     * @param x 此 <code>Rectangle2D</code> 的左上角 X 坐标
     * @param y 此 <code>Rectangle2D</code> 的左上角 Y 坐标
     * @param w 此 <code>Rectangle2D</code> 的宽度
     * @param h 此 <code>Rectangle2D</code> 的高度
     * @since 1.2
     */
    public abstract void setRect(double x, double y, double w, double h);

    /**
     * 将此 <code>Rectangle2D</code> 设置为与指定的 <code>Rectangle2D</code> 相同。
     * @param r 指定的 <code>Rectangle2D</code>
     * @since 1.2
     */
    public void setRect(Rectangle2D r) {
        setRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }


/**
 * 测试指定线段是否与此 <code>Rectangle2D</code> 的内部相交。
 *
 * @param x1 指定线段起点的 X 坐标
 * @param y1 指定线段起点的 Y 坐标
 * @param x2 指定线段终点的 X 坐标
 * @param y2 指定线段终点的 Y 坐标
 * @return 如果指定线段与此 <code>Rectangle2D</code> 的内部相交，则返回 <code>true</code>；否则返回 <code>false</code>。
 * @since 1.2
 */
public boolean intersectsLine(double x1, double y1, double x2, double y2) {
    int out1, out2;
    if ((out2 = outcode(x2, y2)) == 0) {
        return true;
    }
    while ((out1 = outcode(x1, y1)) != 0) {
        if ((out1 & out2) != 0) {
            return false;
        }
        if ((out1 & (OUT_LEFT | OUT_RIGHT)) != 0) {
            double x = getX();
            if ((out1 & OUT_RIGHT) != 0) {
                x += getWidth();
            }
            y1 = y1 + (x - x1) * (y2 - y1) / (x2 - x1);
            x1 = x;
        } else {
            double y = getY();
            if ((out1 & OUT_BOTTOM) != 0) {
                y += getHeight();
            }
            x1 = x1 + (y - y1) * (x2 - x1) / (y2 - y1);
            y1 = y;
        }
    }
    return true;
}

/**
 * 测试指定线段是否与此 <code>Rectangle2D</code> 的内部相交。
 * @param l 要测试是否与此 <code>Rectangle2D</code> 的内部相交的指定 <code>Line2D</code>
 * @return 如果指定的 <code>Line2D</code> 与此 <code>Rectangle2D</code> 的内部相交，则返回 <code>true</code>；否则返回 <code>false</code>。
 * @since 1.2
 */
public boolean intersectsLine(Line2D l) {
    return intersectsLine(l.getX1(), l.getY1(), l.getX2(), l.getY2());
}

/**
 * 确定指定坐标相对于此 <code>Rectangle2D</code> 的位置。
 * 此方法计算适当的掩码值的二进制或，指示此 <code>Rectangle2D</code> 的每一侧，
 * 指定坐标是否在边的同一侧。
 * @param x 指定的 X 坐标
 * @param y 指定的 Y 坐标
 * @return 所有适当掩码值的逻辑或。
 * @see #OUT_LEFT
 * @see #OUT_TOP
 * @see #OUT_RIGHT
 * @see #OUT_BOTTOM
 * @since 1.2
 */
public abstract int outcode(double x, double y);

/**
 * 确定指定的 <code>Point2D</code> 相对于此 <code>Rectangle2D</code> 的位置。
 * 此方法计算适当的掩码值的二进制或，指示此 <code>Rectangle2D</code> 的每一侧，
 * 指定的 <code>Point2D</code> 是否在边的同一侧。
 * @param p 指定的 <code>Point2D</code>
 * @return 所有适当掩码值的逻辑或。
 * @see #OUT_LEFT
 * @see #OUT_TOP
 * @see #OUT_RIGHT
 * @see #OUT_BOTTOM
 * @since 1.2
 */
public int outcode(Point2D p) {
    return outcode(p.getX(), p.getY());
}

/**
 * 设置此 <code>Rectangle2D</code> 的外部边界的位置和大小为指定的矩形值。
 *
 * @param x 此 <code>Rectangle2D</code> 的左上角的 X 坐标
 * @param y 此 <code>Rectangle2D</code> 的左上角的 Y 坐标
 * @param w 此 <code>Rectangle2D</code> 的宽度
 * @param h 此 <code>Rectangle2D</code> 的高度
 * @since 1.2
 */
public void setFrame(double x, double y, double w, double h) {
    setRect(x, y, w, h);
}

/**
 * {@inheritDoc}
 * @since 1.2
 */
public Rectangle2D getBounds2D() {
    return (Rectangle2D) clone();
}

/**
 * {@inheritDoc}
 * @since 1.2
 */
public boolean contains(double x, double y) {
    double x0 = getX();
    double y0 = getY();
    return (x >= x0 &&
            y >= y0 &&
            x < x0 + getWidth() &&
            y < y0 + getHeight());
}

/**
 * {@inheritDoc}
 * @since 1.2
 */
public boolean intersects(double x, double y, double w, double h) {
    if (isEmpty() || w <= 0 || h <= 0) {
        return false;
    }
    double x0 = getX();
    double y0 = getY();
    return (x + w > x0 &&
            y + h > y0 &&
            x < x0 + getWidth() &&
            y < y0 + getHeight());
}

/**
 * {@inheritDoc}
 * @since 1.2
 */
public boolean contains(double x, double y, double w, double h) {
    if (isEmpty() || w <= 0 || h <= 0) {
        return false;
    }
    double x0 = getX();
    double y0 = getY();
    return (x >= x0 &&
            y >= y0 &&
            (x + w) <= x0 + getWidth() &&
            (y + h) <= y0 + getHeight());
}

/**
 * 返回一个表示此 <code>Rectangle2D</code> 与指定的 <code>Rectangle2D</code> 交集的新 <code>Rectangle2D</code> 对象。
 * @param r 要与此 <code>Rectangle2D</code> 交集的 <code>Rectangle2D</code>
 * @return 同时包含指定的 <code>Rectangle2D</code> 和此 <code>Rectangle2D</code> 的最大 <code>Rectangle2D</code>。
 * @since 1.2
 */
public abstract Rectangle2D createIntersection(Rectangle2D r);

/**
 * 交集指定的一对 <code>Rectangle2D</code> 对象，并将结果放入指定的目标 <code>Rectangle2D</code> 对象中。
 * 源矩形之一也可以是目标，以避免创建第三个 <code>Rectangle2D</code> 对象，但在此情况下，此源矩形的原始点将被此方法覆盖。
 * @param src1 要相互交集的一对 <code>Rectangle2D</code> 对象中的第一个
 * @param src2 要相互交集的一对 <code>Rectangle2D</code> 对象中的第二个
 * @param dest 保存 <code>src1</code> 和 <code>src2</code> 交集结果的 <code>Rectangle2D</code>
 * @since 1.2
 */
public static void intersect(Rectangle2D src1,
                             Rectangle2D src2,
                             Rectangle2D dest) {
    double x1 = Math.max(src1.getMinX(), src2.getMinX());
    double y1 = Math.max(src1.getMinY(), src2.getMinY());
    double x2 = Math.min(src1.getMaxX(), src2.getMaxX());
    double y2 = Math.min(src1.getMaxY(), src2.getMaxY());
    dest.setFrame(x1, y1, x2 - x1, y2 - y1);
}

/**
 * 返回一个表示此 <code>Rectangle2D</code> 与指定的 <code>Rectangle2D</code> 并集的新 <code>Rectangle2D</code> 对象。
 * @param r 要与此 <code>Rectangle2D</code> 结合的 <code>Rectangle2D</code>
 * @return 包含指定的 <code>Rectangle2D</code> 和此 <code>Rectangle2D</code> 的最小 <code>Rectangle2D</code>。
 * @since 1.2
 */
public abstract Rectangle2D createUnion(Rectangle2D r);

/**
 * 并集指定的一对 <code>Rectangle2D</code> 对象，并将结果放入指定的目标 <code>Rectangle2D</code> 对象中。
 * 源矩形之一也可以是目标，以避免创建第三个 <code>Rectangle2D</code> 对象，但在此情况下，此源矩形的原始点将被此方法覆盖。
 * @param src1 要相互结合的一对 <code>Rectangle2D</code> 对象中的第一个
 * @param src2 要相互结合的一对 <code>Rectangle2D</code> 对象中的第二个
 * @param dest 保存 <code>src1</code> 和 <code>src2</code> 并集结果的 <code>Rectangle2D</code>
 * @since 1.2
 */
public static void union(Rectangle2D src1,
                         Rectangle2D src2,
                         Rectangle2D dest) {
    double x1 = Math.min(src1.getMinX(), src2.getMinX());
    double y1 = Math.min(src1.getMinY(), src2.getMinY());
    double x2 = Math.max(src1.getMaxX(), src2.getMaxX());
    double y2 = Math.max(src1.getMaxY(), src2.getMaxY());
    dest.setFrameFromDiagonal(x1, y1, x2, y2);
}

/**
 * 将由双精度参数 <code>newx</code> 和 <code>newy</code> 指定的点添加到此 <code>Rectangle2D</code>。
 * 结果的 <code>Rectangle2D</code> 是包含原始 <code>Rectangle2D</code> 和指定点的最小 <code>Rectangle2D</code>。
 * <p>
 * 添加点后，使用添加的点作为参数调用 <code>contains</code> 方法不一定返回 <code>true</code>。
 * <code>contains</code> 方法对于矩形的右边缘或底边缘上的点不返回 <code>true</code>。
 * 因此，如果添加的点落在扩展矩形的左边缘或底边缘上，<code>contains</code> 方法将返回 <code>false</code>。
 * @param newx 新点的 X 坐标
 * @param newy 新点的 Y 坐标
 * @since 1.2
 */
public void add(double newx, double newy) {
    double x1 = Math.min(getMinX(), newx);
    double x2 = Math.max(getMaxX(), newx);
    double y1 = Math.min(getMinY(), newy);
    double y2 = Math.max(getMaxY(), newy);
    setRect(x1, y1, x2 - x1, y2 - y1);
}

/**
 * 将 <code>Point2D</code> 对象 <code>pt</code> 添加到此 <code>Rectangle2D</code>。
 * 结果的 <code>Rectangle2D</code> 是包含原始 <code>Rectangle2D</code> 和指定 <code>Point2D</code> 的最小 <code>Rectangle2D</code>。
 * <p>
 * 添加点后，使用添加的点作为参数调用 <code>contains</code> 方法不一定返回 <code>true</code>。
 * <code>contains</code> 方法对于矩形的右边缘或底边缘上的点不返回 <code>true</code>。
 * 因此，如果添加的点落在扩展矩形的左边缘或底边缘上，<code>contains</code> 方法将返回 <code>false</code>。
 * @param pt 要添加到此 <code>Rectangle2D</code> 的新 <code>Point2D</code>。
 * @since 1.2
 */
public void add(Point2D pt) {
    add(pt.getX(), pt.getY());
}

/**
 * 将一个 <code>Rectangle2D</code> 对象添加到此 <code>Rectangle2D</code>。
 * 结果的 <code>Rectangle2D</code> 是两个 <code>Rectangle2D</code> 对象的并集。
 * @param r 要添加到此 <code>Rectangle2D</code> 的 <code>Rectangle2D</code>。
 * @since 1.2
 */
public void add(Rectangle2D r) {
    double x1 = Math.min(getMinX(), r.getMinX());
    double x2 = Math.max(getMaxX(), r.getMaxX());
    double y1 = Math.min(getMinY(), r.getMinY());
    double y2 = Math.max(getMaxY(), r.getMaxY());
    setRect(x1, y1, x2 - x1, y2 - y1);
}

/**
 * 返回定义此 <code>Rectangle2D</code> 边界的迭代对象。
 * 该类的迭代器是多线程安全的，这意味着此 <code>Rectangle2D</code> 类保证对几何形状的修改不会影响已经进行的任何几何形状迭代。
 * @param at 一个可选的 <code>AffineTransform</code>，用于在迭代中返回坐标时应用，或者如果需要未变换的坐标，则为 <code>null</code>
 * @return 返回此 <code>Rectangle2D</code> 轮廓几何形状的 <code>PathIterator</code> 对象，一次一个线段。
 * @since 1.2
 */
public PathIterator getPathIterator(AffineTransform at) {
    return new RectIterator(this, at);
}

/**
 * 返回定义展平的 <code>Rectangle2D</code> 边界的迭代对象。由于矩形已经是平的，因此 <code>flatness</code> 参数被忽略。
 * 该类的迭代器是多线程安全的，这意味着此 <code>Rectangle2D</code> 类保证对几何形状的修改不会影响已经进行的任何几何形状迭代。
 * @param at 一个可选的 <code>AffineTransform</code>，用于在迭代中返回坐标时应用，或者如果需要未变换的坐标，则为 <code>null</code>
 * @param flatness 允许线段用于近似曲线段的最大距离。由于矩形已经是平的，因此 <code>flatness</code> 参数被忽略。
 * @return 返回此 <code>Rectangle2D</code> 轮廓几何形状的 <code>PathIterator</code> 对象，一次一个线段。
 * @since 1.2
 */
public PathIterator getPathIterator(AffineTransform at, double flatness) {
    return new RectIterator(this, at);
}

/**
 * 返回此 <code>Rectangle2D</code> 的哈希码。
 * @return 此 <code>Rectangle2D</code> 的哈希码。
 * @since 1.2
 */
public int hashCode() {
    long bits = java.lang.Double.doubleToLongBits(getX());
    bits += java.lang.Double.doubleToLongBits(getY()) * 37;
    bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
    bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
    return (((int) bits) ^ ((int) (bits >> 32)));
}

/**
 * 确定指定的 <code>Object</code> 是否等于此 <code>Rectangle2D</code>。
 * 指定的 <code>Object</code> 等于此 <code>Rectangle2D</code>，如果它是 <code>Rectangle2D</code> 的实例，并且其位置和大小与此 <code>Rectangle2D</code> 相同。
 * @param obj 要与此 <code>Rectangle2D</code> 比较的 <code>Object</code>。
 * @return 如果 <code>obj</code> 是 <code>Rectangle2D</code> 的实例并且具有相同的值，则返回 <code>true</code>；否则返回 <code>false</code>。
 * @since 1.2
 */
public boolean equals(Object obj) {
    if (obj == this) {
        return true;
    }
    if (obj instanceof Rectangle2D) {
        Rectangle2D r2d = (Rectangle2D) obj;
        return ((getX() == r2d.getX()) &&
                (getY() == r2d.getY()) &&
                (getWidth() == r2d.getWidth()) &&
                (getHeight() == r2d.getHeight()));
    }
    return false;
}
