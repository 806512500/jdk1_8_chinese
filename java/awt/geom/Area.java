
/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Vector;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import sun.awt.geom.Curve;
import sun.awt.geom.Crossings;
import sun.awt.geom.AreaOp;

/**
 * <code>Area</code> 对象存储和操作一个分辨率无关的二维空间封闭区域的描述。
 * <code>Area</code> 对象可以进行变换，并且可以与其他 <code>Area</code> 对象结合执行各种构造性区域几何 (CAG) 操作。
 * CAG 操作包括区域的 {@link #add 加法}、{@link #subtract 减法}、
 * {@link #intersect 交集} 和 {@link #exclusiveOr 异或}。请参阅链接的方法文档以了解各种操作的示例。
 * <p>
 * <code>Area</code> 类实现了 <code>Shape</code> 接口，并提供了其所有命中测试和路径迭代功能的完整支持，但 <code>Area</code> 比通用路径更为具体，具体表现在以下几个方面：
 * <ul>
 * <li>仅存储闭合的路径和子路径。从未闭合的路径构造的 <code>Area</code> 对象在构造时会隐式闭合，就像这些路径通过 <code>Graphics2D.fill</code> 方法填充一样。
 * <li>各个存储的子路径的内部都是非空且不重叠的。路径在构造时会被分解成单独的非重叠部分，空的部分会被丢弃，然后这些非空和非重叠的属性会通过所有后续的 CAG 操作保持不变。
 *     不同组件子路径的轮廓可以相互接触，只要它们不交叉，使得它们的封闭区域不重叠。
 * <li>描述 <code>Area</code> 轮廓的路径几何形状与构造它的路径相似，只是它描述了相同的封闭二维区域，但可能使用完全不同的路径段类型和顺序。
 * </ul>
 * 使用 <code>Area</code> 时需要注意的一些问题包括：
 * <ul>
 * <li>从一个未闭合（开放）的 <code>Shape</code> 创建 <code>Area</code> 会导致 <code>Area</code> 对象中的闭合轮廓。
 * <li>从一个不封闭任何区域的 <code>Shape</code>（即使“闭合”）创建 <code>Area</code> 会产生一个空的 <code>Area</code>。
 *     一个常见的例子是，从一条线创建 <code>Area</code> 将是空的，因为线不封闭任何区域。一个空的 <code>Area</code> 将在其 <code>PathIterator</code> 对象中不迭代任何几何形状。
 * <li>一个自相交的 <code>Shape</code> 可能会被分成两个（或更多）子路径，每个子路径封闭原始路径的非相交部分。
 * <li>即使原始轮廓简单且明显，<code>Area</code> 可能需要更多的路径段来描述相同的几何形状。<code>Area</code> 类对路径进行的分析可能不会反映人类感知的“简单和明显”。
 * </ul>
 *
 * @since 1.2
 */
public class Area implements Shape, Cloneable {
    private static Vector EmptyCurves = new Vector();

    private Vector curves;

    /**
     * 默认构造函数，创建一个空区域。
     * @since 1.2
     */
    public Area() {
        curves = EmptyCurves;
    }

    /**
     * 从指定的 {@link Shape} 对象创建一个区域几何形状。如果 <code>Shape</code> 未闭合，则显式闭合。
     * 使用 <code>Shape</code> 的几何形状指定的填充规则（偶奇或绕行）来确定结果的封闭区域。
     * @param s  用于构造区域的 <code>Shape</code>
     * @throws NullPointerException 如果 <code>s</code> 为 null
     * @since 1.2
     */
    public Area(Shape s) {
        if (s instanceof Area) {
            curves = ((Area) s).curves;
        } else {
            curves = pathToCurves(s.getPathIterator(null));
        }
    }

    private static Vector pathToCurves(PathIterator pi) {
        Vector curves = new Vector();
        int windingRule = pi.getWindingRule();
        // coords 数组足够大，可以存储：
        //     currentSegment 返回的坐标（6 个）
        //     OR
        //         两个细分的二次曲线（2+4+4=10）
        //         AND
        //             0-1 个水平分割参数
        //             OR
        //             2 个参数方程导数系数
        //     OR
        //         三个细分的三次曲线（2+6+6+6=20）
        //         AND
        //             0-2 个水平分割参数
        //             OR
        //             3 个参数方程导数系数
        double coords[] = new double[23];
        double movx = 0, movy = 0;
        double curx = 0, cury = 0;
        double newx, newy;
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO:
                Curve.insertLine(curves, curx, cury, movx, movy);
                curx = movx = coords[0];
                cury = movy = coords[1];
                Curve.insertMove(curves, movx, movy);
                break;
            case PathIterator.SEG_LINETO:
                newx = coords[0];
                newy = coords[1];
                Curve.insertLine(curves, curx, cury, newx, newy);
                curx = newx;
                cury = newy;
                break;
            case PathIterator.SEG_QUADTO:
                newx = coords[2];
                newy = coords[3];
                Curve.insertQuad(curves, curx, cury, coords);
                curx = newx;
                cury = newy;
                break;
            case PathIterator.SEG_CUBICTO:
                newx = coords[4];
                newy = coords[5];
                Curve.insertCubic(curves, curx, cury, coords);
                curx = newx;
                cury = newy;
                break;
            case PathIterator.SEG_CLOSE:
                Curve.insertLine(curves, curx, cury, movx, movy);
                curx = movx;
                cury = movy;
                break;
            }
            pi.next();
        }
        Curve.insertLine(curves, curx, cury, movx, movy);
        AreaOp operator;
        if (windingRule == PathIterator.WIND_EVEN_ODD) {
            operator = new AreaOp.EOWindOp();
        } else {
            operator = new AreaOp.NZWindOp();
        }
        return operator.calculate(curves, EmptyCurves);
    }

    /**
     * 将指定 <code>Area</code> 的形状添加到此 <code>Area</code> 的形状中。
     * 结果形状将包括两个形状的并集，即包含在此 <code>Area</code> 或指定 <code>Area</code> 中的所有区域。
     * <pre>
     *     // 示例：
     *     Area a1 = new Area([三角形 0,0 =&gt; 8,0 =&gt; 0,8]);
     *     Area a2 = new Area([三角形 0,0 =&gt; 8,0 =&gt; 8,8]);
     *     a1.add(a2);
     *
     *        a1(之前)     +         a2         =     a1(之后)
     *
     *     ################     ################     ################
     *     ##############         ##############     ################
     *     ############             ############     ################
     *     ##########                 ##########     ################
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     * @param   rhs  要添加到当前形状的 <code>Area</code>
     * @throws NullPointerException 如果 <code>rhs</code> 为 null
     * @since 1.2
     */
    public void add(Area rhs) {
        curves = new AreaOp.AddOp().calculate(this.curves, rhs.curves);
        invalidateBounds();
    }

    /**
     * 从此 <code>Area</code> 的形状中减去指定 <code>Area</code> 的形状。
     * 结果形状将包括仅在此 <code>Area</code> 中且不在指定 <code>Area</code> 中的区域。
     * <pre>
     *     // 示例：
     *     Area a1 = new Area([三角形 0,0 =&gt; 8,0 =&gt; 0,8]);
     *     Area a2 = new Area([三角形 0,0 =&gt; 8,0 =&gt; 8,8]);
     *     a1.subtract(a2);
     *
     *        a1(之前)     -         a2         =     a1(之后)
     *
     *     ################     ################
     *     ##############         ##############     ##
     *     ############             ############     ####
     *     ##########                 ##########     ######
     *     ########                     ########     ########
     *     ######                         ######     ######
     *     ####                             ####     ####
     *     ##                                 ##     ##
     * </pre>
     * @param   rhs  要从当前形状中减去的 <code>Area</code>
     * @throws NullPointerException 如果 <code>rhs</code> 为 null
     * @since 1.2
     */
    public void subtract(Area rhs) {
        curves = new AreaOp.SubOp().calculate(this.curves, rhs.curves);
        invalidateBounds();
    }

    /**
     * 将此 <code>Area</code> 的形状设置为当前形状和指定 <code>Area</code> 形状的交集。
     * 结果形状将包括仅在此 <code>Area</code> 和指定 <code>Area</code> 中的区域。
     * <pre>
     *     // 示例：
     *     Area a1 = new Area([三角形 0,0 =&gt; 8,0 =&gt; 0,8]);
     *     Area a2 = new Area([三角形 0,0 =&gt; 8,0 =&gt; 8,8]);
     *     a1.intersect(a2);
     *
     *      a1(之前)   交集     a2         =     a1(之后)
     *
     *     ################     ################     ################
     *     ##############         ##############       ############
     *     ############             ############         ########
     *     ##########                 ##########           ####
     *     ########                     ########
     *     ######                         ######
     *     ####                             ####
     *     ##                                 ##
     * </pre>
     * @param   rhs  要与此 <code>Area</code> 交集的 <code>Area</code>
     * @throws NullPointerException 如果 <code>rhs</code> 为 null
     * @since 1.2
     */
    public void intersect(Area rhs) {
        curves = new AreaOp.IntOp().calculate(this.curves, rhs.curves);
        invalidateBounds();
    }

    /**
     * 将此 <code>Area</code> 的形状设置为当前形状和指定 <code>Area</code> 形状的并集，减去它们的交集。
     * 结果形状将包括仅在此 <code>Area</code> 或指定 <code>Area</code> 中的区域，但不在两者中。
     * <pre>
     *     // 示例：
     *     Area a1 = new Area([三角形 0,0 =&gt; 8,0 =&gt; 0,8]);
     *     Area a2 = new Area([三角形 0,0 =&gt; 8,0 =&gt; 8,8]);
     *     a1.exclusiveOr(a2);
     *
     *        a1(之前)    异或        a2         =     a1(之后)
     *
     *     ################     ################
     *     ##############         ##############     ##            ##
     *     ############             ############     ####        ####
     *     ##########                 ##########     ######    ######
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     * @param   rhs  要与此 <code>Area</code> 异或的 <code>Area</code>。
     * @throws NullPointerException 如果 <code>rhs</code> 为 null
     * @since 1.2
     */
    public void exclusiveOr(Area rhs) {
        curves = new AreaOp.XorOp().calculate(this.curves, rhs.curves);
        invalidateBounds();
    }

    /**
     * 从此 <code>Area</code> 中移除所有几何形状，并将其恢复为空区域。
     * @since 1.2
     */
    public void reset() {
        curves = new Vector();
        invalidateBounds();
    }

    /**
     * 测试此 <code>Area</code> 对象是否封闭任何区域。
     * @return    如果此 <code>Area</code> 对象表示一个空区域，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean isEmpty() {
        return (curves.size() == 0);
    }

    /**
     * 测试此 <code>Area</code> 是否完全由直线边缘的多边形几何形状组成。
     * @return    如果此 <code>Area</code> 的几何形状完全由线段组成，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean isPolygonal() {
        Enumeration enum_ = curves.elements();
        while (enum_.hasMoreElements()) {
            if (((Curve) enum_.nextElement()).getOrder() > 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * 测试此 <code>Area</code> 的形状是否为矩形。
     * @return    如果此 <code>Area</code> 的几何形状为矩形，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean isRectangular() {
        int size = curves.size();
        if (size == 0) {
            return true;
        }
        if (size > 3) {
            return false;
        }
        Curve c1 = (Curve) curves.get(1);
        Curve c2 = (Curve) curves.get(2);
        if (c1.getOrder() != 1 || c2.getOrder() != 1) {
            return false;
        }
        if (c1.getXTop() != c1.getXBot() || c2.getXTop() != c2.getXBot()) {
            return false;
        }
        if (c1.getYTop() != c2.getYTop() || c1.getYBot() != c2.getYBot()) {
            // 也许可以证明这是不可能的...
            return false;
        }
        return true;
    }


                /**
     * 测试此 <code>Area</code> 是否由单个闭合子路径组成。如果路径包含 0 或 1 个子路径，则此方法返回 <code>true</code>；如果路径包含多于 1 个子路径，则返回 <code>false</code>。子路径的数量由路径中出现的 {@link PathIterator#SEG_MOVETO SEG_MOVETO} 段的数量决定。
     * @return    如果 <code>Area</code> 由单个基本几何形状组成，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean isSingular() {
        if (curves.size() < 3) {
            return true;
        }
        Enumeration enum_ = curves.elements();
        enum_.nextElement(); // 第一个 Order0 "moveto"
        while (enum_.hasMoreElements()) {
            if (((Curve) enum_.nextElement()).getOrder() == 0) {
                return false;
            }
        }
        return true;
    }

    private Rectangle2D cachedBounds;
    private void invalidateBounds() {
        cachedBounds = null;
    }
    private Rectangle2D getCachedBounds() {
        if (cachedBounds != null) {
            return cachedBounds;
        }
        Rectangle2D r = new Rectangle2D.Double();
        if (curves.size() > 0) {
            Curve c = (Curve) curves.get(0);
            // 第一个点总是 order 0 的曲线 (moveto)
            r.setRect(c.getX0(), c.getY0(), 0, 0);
            for (int i = 1; i < curves.size(); i++) {
                ((Curve) curves.get(i)).enlarge(r);
            }
        }
        return (cachedBounds = r);
    }

    /**
     * 返回一个高精度的 {@link Rectangle2D}，完全包围此 <code>Area</code>。
     * <p>
     * Area 类将尝试返回尽可能紧的包围盒。包围盒不会扩展以包含形状轮廓中的曲线控制点，但应紧密贴合轮廓的实际几何形状。
     * @return    用于 <code>Area</code> 的包围 <code>Rectangle2D</code>。
     * @since 1.2
     */
    public Rectangle2D getBounds2D() {
        return getCachedBounds().getBounds2D();
    }

    /**
     * 返回一个完全包围此 <code>Area</code> 的 {@link Rectangle}。
     * <p>
     * Area 类将尝试返回尽可能紧的包围盒。包围盒不会扩展以包含形状轮廓中的曲线控制点，但应紧密贴合轮廓的实际几何形状。由于返回的对象表示使用整数的包围盒，因此包围盒只能尽可能紧密地包含几何形状。
     * @return    用于 <code>Area</code> 的包围 <code>Rectangle</code>。
     * @since 1.2
     */
    public Rectangle getBounds() {
        return getCachedBounds().getBounds();
    }

    /**
     * 返回此 <code>Area</code> 对象的精确副本。
     * @return    创建的克隆对象
     * @since 1.2
     */
    public Object clone() {
        return new Area(this);
    }

    /**
     * 测试两个 <code>Area</code> 对象的几何形状是否相等。
     * 如果参数为 null，则此方法将返回 false。
     * @param   other  要与此 <code>Area</code> 比较的 <code>Area</code>
     * @return  如果两个几何形状相等，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean equals(Area other) {
        // REMIND: A *much* simpler operation should be possible...
        // 应该能够进行逐曲线的比较，因为所有 Area 都应该按相同的自上而下的顺序评估其曲线。
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        Vector c = new AreaOp.XorOp().calculate(this.curves, other.curves);
        return c.isEmpty();
    }

    /**
     * 使用指定的 {@link AffineTransform} 转换此 <code>Area</code> 的几何形状。几何形状在原地转换，这将永久改变此对象定义的封闭区域。
     * @param t  用于转换区域的变换
     * @throws NullPointerException 如果 <code>t</code> 为 null
     * @since 1.2
     */
    public void transform(AffineTransform t) {
        if (t == null) {
            throw new NullPointerException("transform must not be null");
        }
        // REMIND: A simpler operation can be performed for some types
        // of transform.
        curves = pathToCurves(getPathIterator(t));
        invalidateBounds();
    }

    /**
     * 创建一个新的 <code>Area</code> 对象，该对象包含使用指定的 <code>AffineTransform</code> 转换的与此 <code>Area</code> 相同的几何形状。此 <code>Area</code> 对象保持不变。
     * @param t  用于转换新 <code>Area</code> 的指定 <code>AffineTransform</code>
     * @throws NullPointerException 如果 <code>t</code> 为 null
     * @return   表示转换后几何形状的新 <code>Area</code> 对象。
     * @since 1.2
     */
    public Area createTransformedArea(AffineTransform t) {
        Area a = new Area(this);
        a.transform(t);
        return a;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(double x, double y) {
        if (!getCachedBounds().contains(x, y)) {
            return false;
        }
        Enumeration enum_ = curves.elements();
        int crossings = 0;
        while (enum_.hasMoreElements()) {
            Curve c = (Curve) enum_.nextElement();
            crossings += c.crossingsFor(x, y);
        }
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
    public boolean contains(double x, double y, double w, double h) {
        if (w < 0 || h < 0) {
            return false;
        }
        if (!getCachedBounds().contains(x, y, w, h)) {
            return false;
        }
        Crossings c = Crossings.findCrossings(curves, x, y, x+w, y+h);
        return (c != null && c.covers(y, y+h));
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
    public boolean intersects(double x, double y, double w, double h) {
        if (w < 0 || h < 0) {
            return false;
        }
        if (!getCachedBounds().intersects(x, y, w, h)) {
            return false;
        }
        Crossings c = Crossings.findCrossings(curves, x, y, x+w, y+h);
        return (c == null || !c.isEmpty());
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * 为这个 <code>Area</code> 对象的轮廓创建一个 {@link PathIterator}。此 <code>Area</code> 对象保持不变。
     * @param at 一个可选的 <code>AffineTransform</code>，用于在迭代返回坐标时应用，或 <code>null</code> 表示不需要变换的坐标
     * @return    返回此 <code>Area</code> 轮廓的几何形状的 <code>PathIterator</code> 对象，一次一个段。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at) {
        return new AreaIterator(curves, at);
    }

    /**
     * 为这个 <code>Area</code> 对象的展平轮廓创建一个 <code>PathIterator</code>。仅返回由 SEG_MOVETO、SEG_LINETO 和 SEG_CLOSE 点类型表示的非曲线路径段。此 <code>Area</code> 对象保持不变。
     * @param at 一个可选的 <code>AffineTransform</code>，用于在迭代返回坐标时应用，或 <code>null</code> 表示不需要变换的坐标
     * @param flatness 曲线控制点偏离共线的最大量，超过此值时，将用一条连接端点的直线替换细分曲线
     * @return    返回此 <code>Area</code> 轮廓的几何形状的 <code>PathIterator</code> 对象，一次一个段。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }
}

class AreaIterator implements PathIterator {
    private AffineTransform transform;
    private Vector curves;
    private int index;
    private Curve prevcurve;
    private Curve thiscurve;

    public AreaIterator(Vector curves, AffineTransform at) {
        this.curves = curves;
        this.transform = at;
        if (curves.size() >= 1) {
            thiscurve = (Curve) curves.get(0);
        }
    }

    public int getWindingRule() {
        // REMIND: Which is better, EVEN_ODD or NON_ZERO?
        //         The paths calculated could be classified either way.
        //return WIND_EVEN_ODD;
        return WIND_NON_ZERO;
    }

    public boolean isDone() {
        return (prevcurve == null && thiscurve == null);
    }

    public void next() {
        if (prevcurve != null) {
            prevcurve = null;
        } else {
            prevcurve = thiscurve;
            index++;
            if (index < curves.size()) {
                thiscurve = (Curve) curves.get(index);
                if (thiscurve.getOrder() != 0 &&
                    prevcurve.getX1() == thiscurve.getX0() &&
                    prevcurve.getY1() == thiscurve.getY0())
                {
                    prevcurve = null;
                }
            } else {
                thiscurve = null;
            }
        }
    }

    public int currentSegment(float coords[]) {
        double dcoords[] = new double[6];
        int segtype = currentSegment(dcoords);
        int numpoints = (segtype == SEG_CLOSE ? 0
                         : (segtype == SEG_QUADTO ? 2
                            : (segtype == SEG_CUBICTO ? 3
                               : 1)));
        for (int i = 0; i < numpoints * 2; i++) {
            coords[i] = (float) dcoords[i];
        }
        return segtype;
    }

    public int currentSegment(double coords[]) {
        int segtype;
        int numpoints;
        if (prevcurve != null) {
            // 需要完成曲线之间的连接
            if (thiscurve == null || thiscurve.getOrder() == 0) {
                return SEG_CLOSE;
            }
            coords[0] = thiscurve.getX0();
            coords[1] = thiscurve.getY0();
            segtype = SEG_LINETO;
            numpoints = 1;
        } else if (thiscurve == null) {
            throw new NoSuchElementException("area iterator out of bounds");
        } else {
            segtype = thiscurve.getSegment(coords);
            numpoints = thiscurve.getOrder();
            if (numpoints == 0) {
                numpoints = 1;
            }
        }
        if (transform != null) {
            transform.transform(coords, 0, coords, 0, numpoints);
        }
        return segtype;
    }
}
