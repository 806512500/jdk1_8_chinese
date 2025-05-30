
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import sun.awt.geom.Crossings;
import java.util.Arrays;

/**
 * <code>Polygon</code> 类封装了坐标空间中一个封闭的二维区域的描述。这个区域由任意数量的线段组成，每条线段是多边形的一边。内部，一个多边形由一系列的 {@code (x,y)}
 * 坐标对组成，每个对定义了多边形的一个顶点，两个连续的对是线段的端点。第一个和最后一个 {@code (x,y)} 点由一条线段连接，形成封闭的多边形。此 <code>Polygon</code> 采用偶奇绕组规则。有关偶奇绕组规则的定义，请参见
 * {@link java.awt.geom.PathIterator#WIND_EVEN_ODD WIND_EVEN_ODD}。
 * 本类的命中测试方法，包括 <code>contains</code>、<code>intersects</code> 和 <code>inside</code> 方法，使用在
 * {@link Shape} 类注释中描述的“内部性”定义。
 *
 * @author      Sami Shaio
 * @see Shape
 * @author      Herb Jellinek
 * @since       1.0
 */
public class Polygon implements Shape, java.io.Serializable {

    /**
     * 总点数。<code>npoints</code> 的值表示此 <code>Polygon</code> 中的有效点数，可能少于
     * {@link #xpoints xpoints} 或 {@link #ypoints ypoints} 中的元素数。此值可以为 NULL。
     *
     * @serial
     * @see #addPoint(int, int)
     * @since 1.0
     */
    public int npoints;

    /**
     * X 坐标数组。此数组中的元素数可能多于此 <code>Polygon</code> 中的 X 坐标数。额外的元素允许向此 <code>Polygon</code> 添加新点而无需重新创建此数组。{@link #npoints npoints} 的值等于此 <code>Polygon</code> 中的有效点数。
     *
     * @serial
     * @see #addPoint(int, int)
     * @since 1.0
     */
    public int xpoints[];

    /**
     * Y 坐标数组。此数组中的元素数可能多于此 <code>Polygon</code> 中的 Y 坐标数。额外的元素允许向此 <code>Polygon</code> 添加新点而无需重新创建此数组。<code>npoints</code> 的值等于此 <code>Polygon</code> 中的有效点数。
     *
     * @serial
     * @see #addPoint(int, int)
     * @since 1.0
     */
    public int ypoints[];

    /**
     * 此 {@code Polygon} 的边界。此值可以为 null。
     *
     * @serial
     * @see #getBoundingBox()
     * @see #getBounds()
     * @since 1.0
     */
    protected Rectangle bounds;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -6460061437900069969L;

    /*
     * xpoints 和 ypoints 的默认长度。
     */
    private static final int MIN_LENGTH = 4;

    /**
     * 创建一个空的多边形。
     * @since 1.0
     */
    public Polygon() {
        xpoints = new int[MIN_LENGTH];
        ypoints = new int[MIN_LENGTH];
    }

    /**
     * 从指定的参数构造并初始化一个 <code>Polygon</code>。
     * @param xpoints X 坐标数组
     * @param ypoints Y 坐标数组
     * @param npoints 此 <code>Polygon</code> 中的总点数
     * @exception  NegativeArraySizeException 如果 <code>npoints</code> 的值为负。
     * @exception  IndexOutOfBoundsException 如果 <code>npoints</code> 大于 <code>xpoints</code>
     *             或 <code>ypoints</code> 的长度。
     * @exception  NullPointerException 如果 <code>xpoints</code> 或
     *             <code>ypoints</code> 为 <code>null</code>。
     * @since 1.0
     */
    public Polygon(int xpoints[], int ypoints[], int npoints) {
        // 修复 4489009：如果 npoints 很大且 > {x,y}points.length，则应抛出 IndexOutofBoundsException 而不是 OutofMemoryException
        if (npoints > xpoints.length || npoints > ypoints.length) {
            throw new IndexOutOfBoundsException("npoints > xpoints.length || "+
                                                "npoints > ypoints.length");
        }
        // 修复 6191114：如果 npoints 为负，应抛出 NegativeArraySizeException
        if (npoints < 0) {
            throw new NegativeArraySizeException("npoints < 0");
        }
        // 修复 6343431：如果数组的长度不完全等于 npoints，可能会导致小程序兼容问题
        this.npoints = npoints;
        this.xpoints = Arrays.copyOf(xpoints, npoints);
        this.ypoints = Arrays.copyOf(ypoints, npoints);
    }

    /**
     * 将此 <code>Polygon</code> 对象重置为空多边形。
     * 坐标数组及其内容保持不变，但点数重置为零，以标记旧顶点数据无效，并从头开始累积新顶点数据。
     * 与旧顶点相关的所有内部缓存数据都被丢弃。
     * 请注意，由于重置前的坐标数组被重用，如果新多边形数据中的顶点数显著少于重置前的数据中的顶点数，创建一个新的空 <code>Polygon</code> 可能比重置当前的更节省内存。
     * @see         java.awt.Polygon#invalidate
     * @since 1.4
     */
    public void reset() {
        npoints = 0;
        bounds = null;
    }

    /**
     * 使依赖于此 <code>Polygon</code> 顶点坐标的任何内部缓存数据失效或刷新。
     * 在直接操作 <code>xpoints</code> 或 <code>ypoints</code> 数组中的坐标后，应调用此方法，以避免 <code>getBounds</code> 或 <code>contains</code> 等方法因缓存了与顶点坐标相关的早期计算数据而产生不一致的结果。
     * @see         java.awt.Polygon#getBounds
     * @since 1.4
     */
    public void invalidate() {
        bounds = null;
    }

    /**
     * 沿 X 轴平移 <code>deltaX</code>，沿 Y 轴平移 <code>deltaY</code>，以平移 <code>Polygon</code> 的顶点。
     * @param deltaX 沿 X 轴平移的量
     * @param deltaY 沿 Y 轴平移的量
     * @since 1.1
     */
    public void translate(int deltaX, int deltaY) {
        for (int i = 0; i < npoints; i++) {
            xpoints[i] += deltaX;
            ypoints[i] += deltaY;
        }
        if (bounds != null) {
            bounds.translate(deltaX, deltaY);
        }
    }

    /*
     * 计算传递给构造函数的点的边界框。将 <code>bounds</code> 设置为结果。
     * @param xpoints[] <i>x</i> 坐标数组
     * @param ypoints[] <i>y</i> 坐标数组
     * @param npoints 总点数
     */
    void calculateBounds(int xpoints[], int ypoints[], int npoints) {
        int boundsMinX = Integer.MAX_VALUE;
        int boundsMinY = Integer.MAX_VALUE;
        int boundsMaxX = Integer.MIN_VALUE;
        int boundsMaxY = Integer.MIN_VALUE;

        for (int i = 0; i < npoints; i++) {
            int x = xpoints[i];
            boundsMinX = Math.min(boundsMinX, x);
            boundsMaxX = Math.max(boundsMaxX, x);
            int y = ypoints[i];
            boundsMinY = Math.min(boundsMinY, y);
            boundsMaxY = Math.max(boundsMaxY, y);
        }
        bounds = new Rectangle(boundsMinX, boundsMinY,
                               boundsMaxX - boundsMinX,
                               boundsMaxY - boundsMinY);
    }

    /*
     * 调整边界框以容纳指定的坐标。
     * @param x,&nbsp;y 指定的坐标
     */
    void updateBounds(int x, int y) {
        if (x < bounds.x) {
            bounds.width = bounds.width + (bounds.x - x);
            bounds.x = x;
        }
        else {
            bounds.width = Math.max(bounds.width, x - bounds.x);
            // bounds.x = bounds.x;
        }

        if (y < bounds.y) {
            bounds.height = bounds.height + (bounds.y - y);
            bounds.y = y;
        }
        else {
            bounds.height = Math.max(bounds.height, y - bounds.y);
            // bounds.y = bounds.y;
        }
    }

    /**
     * 将指定的坐标添加到此 <code>Polygon</code>。
     * <p>
     * 如果已经执行了计算此 <code>Polygon</code> 的边界框的操作，例如 <code>getBounds</code> 或 <code>contains</code>，则此方法会更新边界框。
     * @param       x 指定的 X 坐标
     * @param       y 指定的 Y 坐标
     * @see         java.awt.Polygon#getBounds
     * @see         java.awt.Polygon#contains
     * @since 1.0
     */
    public void addPoint(int x, int y) {
        if (npoints >= xpoints.length || npoints >= ypoints.length) {
            int newLength = npoints * 2;
            // 确保 newLength 大于 MIN_LENGTH 并且是 2 的幂
            if (newLength < MIN_LENGTH) {
                newLength = MIN_LENGTH;
            } else if ((newLength & (newLength - 1)) != 0) {
                newLength = Integer.highestOneBit(newLength);
            }

            xpoints = Arrays.copyOf(xpoints, newLength);
            ypoints = Arrays.copyOf(ypoints, newLength);
        }
        xpoints[npoints] = x;
        ypoints[npoints] = y;
        npoints++;
        if (bounds != null) {
            updateBounds(x, y);
        }
    }

    /**
     * 获取此 <code>Polygon</code> 的边界框。
     * 边界框是能够完全包含 <code>Polygon</code> 的最小 {@link Rectangle}，其边平行于坐标空间的 x 轴和 y 轴。
     * @return 定义此 <code>Polygon</code> 边界的 <code>Rectangle</code>。
     * @since 1.1
     */
    public Rectangle getBounds() {
        return getBoundingBox();
    }

    /**
     * 返回此 <code>Polygon</code> 的边界。
     * @return 此 <code>Polygon</code> 的边界。
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>getBounds()</code>。
     * @since 1.0
     */
    @Deprecated
    public Rectangle getBoundingBox() {
        if (npoints == 0) {
            return new Rectangle();
        }
        if (bounds == null) {
            calculateBounds(xpoints, ypoints, npoints);
        }
        return bounds.getBounds();
    }

    /**
     * 确定指定的 {@link Point} 是否在此 <code>Polygon</code> 内。
     * @param p 要测试的指定 <code>Point</code>
     * @return 如果 <code>Polygon</code> 包含 <code>Point</code>，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @see #contains(double, double)
     * @since 1.0
     */
    public boolean contains(Point p) {
        return contains(p.x, p.y);
    }

    /**
     * 确定指定的坐标是否在此 <code>Polygon</code> 内。
     * <p>
     * @param x 要测试的指定 X 坐标
     * @param y 要测试的指定 Y 坐标
     * @return 如果此 {@code Polygon} 包含指定坐标 {@code (x,y)}，则返回 {@code true}；否则返回 {@code false}。
     * @see #contains(double, double)
     * @since 1.1
     */
    public boolean contains(int x, int y) {
        return contains((double) x, (double) y);
    }

    /**
     * 确定指定的坐标是否包含在此 <code>Polygon</code> 内。
     * @param x 要测试的指定 X 坐标
     * @param y 要测试的指定 Y 坐标
     * @return 如果此 {@code Polygon} 包含指定坐标 {@code (x,y)}，则返回 {@code true}；否则返回 {@code false}。
     * @see #contains(double, double)
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>contains(int, int)</code>。
     * @since 1.0
     */
    @Deprecated
    public boolean inside(int x, int y) {
        return contains((double) x, (double) y);
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public Rectangle2D getBounds2D() {
        return getBounds();
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(double x, double y) {
        if (npoints <= 2 || !getBoundingBox().contains(x, y)) {
            return false;
        }
        int hits = 0;

        int lastx = xpoints[npoints - 1];
        int lasty = ypoints[npoints - 1];
        int curx, cury;

        // 遍历多边形的边
        for (int i = 0; i < npoints; lastx = curx, lasty = cury, i++) {
            curx = xpoints[i];
            cury = ypoints[i];

            if (cury == lasty) {
                continue;
            }

            int leftx;
            if (curx < lastx) {
                if (x >= lastx) {
                    continue;
                }
                leftx = curx;
            } else {
                if (x >= curx) {
                    continue;
                }
                leftx = lastx;
            }

            double test1, test2;
            if (cury < lasty) {
                if (y < cury || y >= lasty) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - curx;
                test2 = y - cury;
            } else {
                if (y < lasty || y >= cury) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - lastx;
                test2 = y - lasty;
            }


                        if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
                hits++;
            }
        }

        return ((hits & 1) != 0);
    }

    private Crossings getCrossings(double xlo, double ylo,
                                   double xhi, double yhi)
    {
        Crossings cross = new Crossings.EvenOdd(xlo, ylo, xhi, yhi);
        int lastx = xpoints[npoints - 1];
        int lasty = ypoints[npoints - 1];
        int curx, cury;

        // 遍历多边形的边
        for (int i = 0; i < npoints; i++) {
            curx = xpoints[i];
            cury = ypoints[i];
            if (cross.accumulateLine(lastx, lasty, curx, cury)) {
                return null;
            }
            lastx = curx;
            lasty = cury;
        }

        return cross;
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
        if (npoints <= 0 || !getBoundingBox().intersects(x, y, w, h)) {
            return false;
        }

        Crossings cross = getCrossings(x, y, x+w, y+h);
        return (cross == null || !cross.isEmpty());
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
        if (npoints <= 0 || !getBoundingBox().intersects(x, y, w, h)) {
            return false;
        }

        Crossings cross = getCrossings(x, y, x+w, y+h);
        return (cross != null && cross.covers(y, y+h));
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * 返回一个迭代器对象，该对象沿此 <code>Polygon</code> 的边界进行迭代，并提供对此 <code>Polygon</code> 轮廓几何的访问。
     * 可以指定一个可选的 {@link AffineTransform}，以便在迭代中返回的坐标相应地进行变换。
     * @param at 一个可选的 <code>AffineTransform</code>，用于在迭代中返回的坐标，或者如果需要未变换的坐标则为 <code>null</code>
     * @return 一个 {@link PathIterator} 对象，提供对此 <code>Polygon</code> 的几何访问。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at) {
        return new PolygonPathIterator(this, at);
    }

    /**
     * 返回一个迭代器对象，该对象沿 <code>Shape</code> 的边界进行迭代，并提供对此 <code>Shape</code> 轮廓几何的访问。
     * 迭代器仅返回 SEG_MOVETO、SEG_LINETO 和 SEG_CLOSE 点类型。
     * 由于多边形已经是平的，因此 <code>flatness</code> 参数被忽略。
     * 可以指定一个可选的 <code>AffineTransform</code>，在这种情况下，迭代中返回的坐标将相应地进行变换。
     * @param at 一个可选的 <code>AffineTransform</code>，用于在迭代中返回的坐标，或者如果需要未变换的坐标则为 <code>null</code>
     * @param flatness 控制点对于给定曲线的最大偏差，当控制点从共线偏离此值时，将用直线连接端点替换细分曲线。由于多边形已经是平的，因此 <code>flatness</code> 参数被忽略。
     * @return 一个 <code>PathIterator</code> 对象，提供对 <code>Shape</code> 对象的几何访问。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return getPathIterator(at);
    }

    class PolygonPathIterator implements PathIterator {
        Polygon poly;
        AffineTransform transform;
        int index;

        public PolygonPathIterator(Polygon pg, AffineTransform at) {
            poly = pg;
            transform = at;
            if (pg.npoints == 0) {
                // 防止出现多余的 SEG_CLOSE 段
                index = 1;
            }
        }

        /**
         * 返回用于确定路径内部的绕组规则。
         * @return 一个整数，表示当前的绕组规则。
         * @see PathIterator#WIND_NON_ZERO
         */
        public int getWindingRule() {
            return WIND_EVEN_ODD;
        }

        /**
         * 测试是否还有更多的点可以读取。
         * @return 如果还有更多的点可以读取，则返回 <code>true</code>；否则返回 <code>false</code>。
         */
        public boolean isDone() {
            return index > poly.npoints;
        }

        /**
         * 沿主要遍历方向向前移动迭代器，到路径的下一个段，当该方向还有更多点时。
         */
        public void next() {
            index++;
        }

        /**
         * 返回迭代中当前路径段的坐标和类型。
         * 返回值是路径段类型：SEG_MOVETO、SEG_LINETO 或 SEG_CLOSE。
         * 必须传递一个长度为 2 的 <code>float</code> 数组，可以用于存储点的坐标。
         * 每个点存储为一对 <code>float</code> x, y 坐标。SEG_MOVETO 和 SEG_LINETO 类型返回一个点，而 SEG_CLOSE 不返回任何点。
         * @param coords 一个 <code>float</code> 数组，指定点的坐标
         * @return 一个整数，表示当前路径段的类型和坐标。
         * @see PathIterator#SEG_MOVETO
         * @see PathIterator#SEG_LINETO
         * @see PathIterator#SEG_CLOSE
         */
        public int currentSegment(float[] coords) {
            if (index >= poly.npoints) {
                return SEG_CLOSE;
            }
            coords[0] = poly.xpoints[index];
            coords[1] = poly.ypoints[index];
            if (transform != null) {
                transform.transform(coords, 0, coords, 0, 1);
            }
            return (index == 0 ? SEG_MOVETO : SEG_LINETO);
        }

        /**
         * 返回迭代中当前路径段的坐标和类型。
         * 返回值是路径段类型：SEG_MOVETO、SEG_LINETO 或 SEG_CLOSE。
         * 必须传递一个长度为 2 的 <code>double</code> 数组，可以用于存储点的坐标。
         * 每个点存储为一对 <code>double</code> x, y 坐标。
         * SEG_MOVETO 和 SEG_LINETO 类型返回一个点，而 SEG_CLOSE 不返回任何点。
         * @param coords 一个 <code>double</code> 数组，指定点的坐标
         * @return 一个整数，表示当前路径段的类型和坐标。
         * @see PathIterator#SEG_MOVETO
         * @see PathIterator#SEG_LINETO
         * @see PathIterator#SEG_CLOSE
         */
        public int currentSegment(double[] coords) {
            if (index >= poly.npoints) {
                return SEG_CLOSE;
            }
            coords[0] = poly.xpoints[index];
            coords[1] = poly.ypoints[index];
            if (transform != null) {
                transform.transform(coords, 0, coords, 0, 1);
            }
            return (index == 0 ? SEG_MOVETO : SEG_LINETO);
        }
    }
}
