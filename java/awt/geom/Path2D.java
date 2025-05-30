
/*
 * Copyright (c) 2006, 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Rectangle;
import java.awt.Shape;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Arrays;

import sun.awt.geom.Curve;

/**
 * {@code Path2D} 类提供了一个简单而灵活的形状，可以表示任意几何路径。
 * 它可以完全表示由 {@link PathIterator} 接口迭代的所有路径，包括所有类型的路径段和绕行规则，
 * 并实现了 {@link Shape} 接口的所有基本命中测试方法。
 * <p>
 * 当处理可以用浮点精度表示和使用的数据时，使用 {@link Path2D.Float}。
 * 当数据需要双精度的精度或范围时，使用 {@link Path2D.Double}。
 * <p>
 * {@code Path2D} 提供了基本构造和管理几何路径所需的所有功能，
 * 并实现了上述接口，而没有过多的解释。
 * 如果需要对封闭几何形状的内部进行更复杂的操作而不仅仅是简单的命中测试，
 * 则 {@link Area} 类提供了专门针对封闭图形的额外功能。
 * 虽然这两个类都实现了 {@code Shape} 接口，但它们的用途不同，
 * 一起提供了对几何形状的两种有用视图，其中 {@code Path2D} 主要处理由路径段形成的轨迹，
 * 而 {@code Area} 更多处理对封闭区域的解释和操作。
 * <p>
 * {@link PathIterator} 接口对构成路径的段类型和确定路径内外的绕行规则有更详细的描述。
 *
 * @author Jim Graham
 * @since 1.6
 */
public abstract class Path2D implements Shape, Cloneable {
    /**
     * 用于确定路径内部的奇偶绕行规则。
     *
     * @see PathIterator#WIND_EVEN_ODD
     * @since 1.6
     */
    public static final int WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD;

    /**
     * 用于确定路径内部的非零绕行规则。
     *
     * @see PathIterator#WIND_NON_ZERO
     * @since 1.6
     */
    public static final int WIND_NON_ZERO = PathIterator.WIND_NON_ZERO;

    // 为了代码的简洁性，将这些常量复制到我们的命名空间，并将它们转换为字节常量以便于存储。
    private static final byte SEG_MOVETO  = (byte) PathIterator.SEG_MOVETO;
    private static final byte SEG_LINETO  = (byte) PathIterator.SEG_LINETO;
    private static final byte SEG_QUADTO  = (byte) PathIterator.SEG_QUADTO;
    private static final byte SEG_CUBICTO = (byte) PathIterator.SEG_CUBICTO;
    private static final byte SEG_CLOSE   = (byte) PathIterator.SEG_CLOSE;

    transient byte[] pointTypes;
    transient int numTypes;
    transient int numCoords;
    transient int windingRule;

    static final int INIT_SIZE = 20;
    static final int EXPAND_MAX = 500;
    static final int EXPAND_MAX_COORDS = EXPAND_MAX * 2;
    static final int EXPAND_MIN = 10; // 确保 > 6 (cubics)

    /**
     * 构造一个新的空的 {@code Path2D} 对象。
     * 假设默认使用此构造函数的包级子类将填充所有值。
     *
     * @since 1.6
     */
    /* private protected */
    Path2D() {
    }

    /**
     * 从给定的初始值构造一个新的 {@code Path2D} 对象。
     * 此方法仅用于内部使用，不应公开，除非此类的其他构造函数被公开。
     *
     * @param rule 绕行规则
     * @param initialTypes 存储路径段类型初始数组的大小
     * @since 1.6
     */
    /* private protected */
    Path2D(int rule, int initialTypes) {
        setWindingRule(rule);
        this.pointTypes = new byte[initialTypes];
    }

    abstract float[] cloneCoordsFloat(AffineTransform at);
    abstract double[] cloneCoordsDouble(AffineTransform at);
    abstract void append(float x, float y);
    abstract void append(double x, double y);
    abstract Point2D getPoint(int coordindex);
    abstract void needRoom(boolean needMove, int newCoords);
    abstract int pointCrossings(double px, double py);
    abstract int rectCrossings(double rxmin, double rymin,
                               double rxmax, double rymax);

    static byte[] expandPointTypes(byte[] oldPointTypes, int needed) {
        final int oldSize = oldPointTypes.length;
        final int newSizeMin = oldSize + needed;
        if (newSizeMin < oldSize) {
            // 硬溢出失败 - 无法容纳新项目而不溢出
            throw new ArrayIndexOutOfBoundsException(
                          "pointTypes exceeds maximum capacity !");
        }
        // 增长算法计算
        int grow = oldSize;
        if (grow > EXPAND_MAX) {
            grow = Math.max(EXPAND_MAX, oldSize >> 3); // 1/8th min
        } else if (grow < EXPAND_MIN) {
            grow = EXPAND_MIN;
        }
        assert grow > 0;

        int newSize = oldSize + grow;
        if (newSize < newSizeMin) {
            // 增长算法计算中的溢出
            newSize = Integer.MAX_VALUE;
        }
        while (true) {
            try {
                // 尝试分配更大的数组
                return Arrays.copyOf(oldPointTypes, newSize);
            } catch (OutOfMemoryError oome) {
                if (newSize == newSizeMin) {
                    throw oome;
                }
            }
            newSize = newSizeMin + (newSize - newSizeMin) / 2;
        }
    }

    /**
     * {@code Float} 类定义了一个几何路径，其坐标存储为单精度浮点数。
     *
     * @since 1.6
     */
    public static class Float extends Path2D implements Serializable {
        transient float floatCoords[];

        /**
         * 构造一个新的空的单精度 {@code Path2D} 对象，使用默认的绕行规则 {@link #WIND_NON_ZERO}。
         *
         * @since 1.6
         */
        public Float() {
            this(WIND_NON_ZERO, INIT_SIZE);
        }

        /**
         * 构造一个新的空的单精度 {@code Path2D} 对象，使用指定的绕行规则来控制需要定义路径内部的操作。
         *
         * @param rule 绕行规则
         * @see #WIND_EVEN_ODD
         * @see #WIND_NON_ZERO
         * @since 1.6
         */
        public Float(int rule) {
            this(rule, INIT_SIZE);
        }

        /**
         * 构造一个新的空的单精度 {@code Path2D} 对象，使用指定的绕行规则和指定的初始容量来存储路径段。
         * 这个数字是对将添加到路径中的路径段数量的初始估计，但存储会根据需要扩展以存储添加的所有路径段。
         *
         * @param rule 绕行规则
         * @param initialCapacity 路径中路径段数量的估计值
         * @see #WIND_EVEN_ODD
         * @see #WIND_NON_ZERO
         * @since 1.6
         */
        public Float(int rule, int initialCapacity) {
            super(rule, initialCapacity);
            floatCoords = new float[initialCapacity * 2];
        }

        /**
         * 从任意 {@link Shape} 对象构造一个新的单精度 {@code Path2D} 对象。
         * 此路径的所有初始几何形状和绕行规则都从指定的 {@code Shape} 对象中获取。
         *
         * @param s 指定的 {@code Shape} 对象
         * @since 1.6
         */
        public Float(Shape s) {
            this(s, null);
        }

        /**
         * 从任意 {@link Shape} 对象构造一个新的单精度 {@code Path2D} 对象，并通过 {@link AffineTransform} 对象进行变换。
         * 此路径的所有初始几何形状和绕行规则都从指定的 {@code Shape} 对象中获取，并通过指定的 {@code AffineTransform} 对象进行变换。
         *
         * @param s 指定的 {@code Shape} 对象
         * @param at 指定的 {@code AffineTransform} 对象
         * @since 1.6
         */
        public Float(Shape s, AffineTransform at) {
            if (s instanceof Path2D) {
                Path2D p2d = (Path2D) s;
                setWindingRule(p2d.windingRule);
                this.numTypes = p2d.numTypes;
                // 裁剪数组：
                this.pointTypes = Arrays.copyOf(p2d.pointTypes, p2d.numTypes);
                this.numCoords = p2d.numCoords;
                this.floatCoords = p2d.cloneCoordsFloat(at);
            } else {
                PathIterator pi = s.getPathIterator(at);
                setWindingRule(pi.getWindingRule());
                this.pointTypes = new byte[INIT_SIZE];
                this.floatCoords = new float[INIT_SIZE * 2];
                append(pi, false);
            }
        }

        @Override
        float[] cloneCoordsFloat(AffineTransform at) {
            // 裁剪数组：
            float ret[];
            if (at == null) {
                ret = Arrays.copyOf(floatCoords, numCoords);
            } else {
                ret = new float[numCoords];
                at.transform(floatCoords, 0, ret, 0, numCoords / 2);
            }
            return ret;
        }

        @Override
        double[] cloneCoordsDouble(AffineTransform at) {
            // 裁剪数组：
            double ret[] = new double[numCoords];
            if (at == null) {
                for (int i = 0; i < numCoords; i++) {
                    ret[i] = floatCoords[i];
                }
            } else {
                at.transform(floatCoords, 0, ret, 0, numCoords / 2);
            }
            return ret;
        }

        void append(float x, float y) {
            floatCoords[numCoords++] = x;
            floatCoords[numCoords++] = y;
        }

        void append(double x, double y) {
            floatCoords[numCoords++] = (float) x;
            floatCoords[numCoords++] = (float) y;
        }

        Point2D getPoint(int coordindex) {
            return new Point2D.Float(floatCoords[coordindex],
                                     floatCoords[coordindex+1]);
        }

        @Override
        void needRoom(boolean needMove, int newCoords) {
            if ((numTypes == 0) && needMove) {
                throw new IllegalPathStateException("missing initial moveto "+
                                                    "in path definition");
            }
            if (numTypes >= pointTypes.length) {
                pointTypes = expandPointTypes(pointTypes, 1);
            }
            if (numCoords > (floatCoords.length - newCoords)) {
                floatCoords = expandCoords(floatCoords, newCoords);
            }
        }

        static float[] expandCoords(float[] oldCoords, int needed) {
            final int oldSize = oldCoords.length;
            final int newSizeMin = oldSize + needed;
            if (newSizeMin < oldSize) {
                // 硬溢出失败 - 无法容纳新项目而不溢出
                throw new ArrayIndexOutOfBoundsException(
                              "coords exceeds maximum capacity !");
            }
            // 增长算法计算
            int grow = oldSize;
            if (grow > EXPAND_MAX_COORDS) {
                grow = Math.max(EXPAND_MAX_COORDS, oldSize >> 3); // 1/8th min
            } else if (grow < EXPAND_MIN) {
                grow = EXPAND_MIN;
            }
            assert grow > needed;

            int newSize = oldSize + grow;
            if (newSize < newSizeMin) {
                // 增长算法计算中的溢出
                newSize = Integer.MAX_VALUE;
            }
            while (true) {
                try {
                    // 尝试分配更大的数组
                    return Arrays.copyOf(oldCoords, newSize);
                } catch (OutOfMemoryError oome) {
                    if (newSize == newSizeMin) {
                        throw oome;
                    }
                }
                newSize = newSizeMin + (newSize - newSizeMin) / 2;
            }
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public final synchronized void moveTo(double x, double y) {
            if (numTypes > 0 && pointTypes[numTypes - 1] == SEG_MOVETO) {
                floatCoords[numCoords-2] = (float) x;
                floatCoords[numCoords-1] = (float) y;
            } else {
                needRoom(false, 2);
                pointTypes[numTypes++] = SEG_MOVETO;
                floatCoords[numCoords++] = (float) x;
                floatCoords[numCoords++] = (float) y;
            }
        }

        /**
         * 通过移动到指定的浮点精度坐标来向路径添加一个点。
         * <p>
         * 此方法提供了基类 {@code Path2D} 中双精度 {@code moveTo()} 方法的单精度变体。
         *
         * @param x 指定的 X 坐标
         * @param y 指定的 Y 坐标
         * @see Path2D#moveTo
         * @since 1.6
         */
        public final synchronized void moveTo(float x, float y) {
            if (numTypes > 0 && pointTypes[numTypes - 1] == SEG_MOVETO) {
                floatCoords[numCoords-2] = x;
                floatCoords[numCoords-1] = y;
            } else {
                needRoom(false, 2);
                pointTypes[numTypes++] = SEG_MOVETO;
                floatCoords[numCoords++] = x;
                floatCoords[numCoords++] = y;
            }
        }


        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public final synchronized void lineTo(double x, double y) {
            needRoom(true, 2);
            pointTypes[numTypes++] = SEG_LINETO;
            floatCoords[numCoords++] = (float) x;
            floatCoords[numCoords++] = (float) y;
        }

        /**
         * 向路径添加一个点，通过从当前坐标绘制一条直线到新的指定坐标
         * 指定为浮点精度。
         * <p>
         * 此方法提供了 {@code Path2D} 基类中双精度 {@code lineTo()} 方法的单精度变体。
         *
         * @param x 指定的 X 坐标
         * @param y 指定的 Y 坐标
         * @see Path2D#lineTo
         * @since 1.6
         */
        public final synchronized void lineTo(float x, float y) {
            needRoom(true, 2);
            pointTypes[numTypes++] = SEG_LINETO;
            floatCoords[numCoords++] = x;
            floatCoords[numCoords++] = y;
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public final synchronized void quadTo(double x1, double y1,
                                              double x2, double y2)
        {
            needRoom(true, 4);
            pointTypes[numTypes++] = SEG_QUADTO;
            floatCoords[numCoords++] = (float) x1;
            floatCoords[numCoords++] = (float) y1;
            floatCoords[numCoords++] = (float) x2;
            floatCoords[numCoords++] = (float) y2;
        }

        /**
         * 向路径添加一个由两个新点定义的曲线段，通过绘制一条二次曲线，该曲线同时交于当前坐标和指定坐标 {@code (x2,y2)}，
         * 使用指定的点 {@code (x1,y1)} 作为二次参数控制点。
         * 所有坐标均以浮点精度指定。
         * <p>
         * 此方法提供了 {@code Path2D} 基类中双精度 {@code quadTo()} 方法的单精度变体。
         *
         * @param x1 二次控制点的 X 坐标
         * @param y1 二次控制点的 Y 坐标
         * @param x2 最终终点的 X 坐标
         * @param y2 最终终点的 Y 坐标
         * @see Path2D#quadTo
         * @since 1.6
         */
        public final synchronized void quadTo(float x1, float y1,
                                              float x2, float y2)
        {
            needRoom(true, 4);
            pointTypes[numTypes++] = SEG_QUADTO;
            floatCoords[numCoords++] = x1;
            floatCoords[numCoords++] = y1;
            floatCoords[numCoords++] = x2;
            floatCoords[numCoords++] = y2;
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public final synchronized void curveTo(double x1, double y1,
                                               double x2, double y2,
                                               double x3, double y3)
        {
            needRoom(true, 6);
            pointTypes[numTypes++] = SEG_CUBICTO;
            floatCoords[numCoords++] = (float) x1;
            floatCoords[numCoords++] = (float) y1;
            floatCoords[numCoords++] = (float) x2;
            floatCoords[numCoords++] = (float) y2;
            floatCoords[numCoords++] = (float) x3;
            floatCoords[numCoords++] = (float) y3;
        }

        /**
         * 向路径添加一个由三个新点定义的曲线段，通过绘制一条贝塞尔曲线，该曲线同时交于当前坐标和指定坐标 {@code (x3,y3)}，
         * 使用指定的点 {@code (x1,y1)} 和 {@code (x2,y2)} 作为贝塞尔控制点。
         * 所有坐标均以浮点精度指定。
         * <p>
         * 此方法提供了 {@code Path2D} 基类中双精度 {@code curveTo()} 方法的单精度变体。
         *
         * @param x1 第一个贝塞尔控制点的 X 坐标
         * @param y1 第一个贝塞尔控制点的 Y 坐标
         * @param x2 第二个贝塞尔控制点的 X 坐标
         * @param y2 第二个贝塞尔控制点的 Y 坐标
         * @param x3 最终终点的 X 坐标
         * @param y3 最终终点的 Y 坐标
         * @see Path2D#curveTo
         * @since 1.6
         */
        public final synchronized void curveTo(float x1, float y1,
                                               float x2, float y2,
                                               float x3, float y3)
        {
            needRoom(true, 6);
            pointTypes[numTypes++] = SEG_CUBICTO;
            floatCoords[numCoords++] = x1;
            floatCoords[numCoords++] = y1;
            floatCoords[numCoords++] = x2;
            floatCoords[numCoords++] = y2;
            floatCoords[numCoords++] = x3;
            floatCoords[numCoords++] = y3;
        }

        int pointCrossings(double px, double py) {
            if (numTypes == 0) {
                return 0;
            }
            double movx, movy, curx, cury, endx, endy;
            float coords[] = floatCoords;
            curx = movx = coords[0];
            cury = movy = coords[1];
            int crossings = 0;
            int ci = 2;
            for (int i = 1; i < numTypes; i++) {
                switch (pointTypes[i]) {
                case PathIterator.SEG_MOVETO:
                    if (cury != movy) {
                        crossings +=
                            Curve.pointCrossingsForLine(px, py,
                                                        curx, cury,
                                                        movx, movy);
                    }
                    movx = curx = coords[ci++];
                    movy = cury = coords[ci++];
                    break;
                case PathIterator.SEG_LINETO:
                    crossings +=
                        Curve.pointCrossingsForLine(px, py,
                                                    curx, cury,
                                                    endx = coords[ci++],
                                                    endy = coords[ci++]);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.SEG_QUADTO:
                    crossings +=
                        Curve.pointCrossingsForQuad(px, py,
                                                    curx, cury,
                                                    coords[ci++],
                                                    coords[ci++],
                                                    endx = coords[ci++],
                                                    endy = coords[ci++],
                                                    0);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.SEG_CUBICTO:
                    crossings +=
                        Curve.pointCrossingsForCubic(px, py,
                                                     curx, cury,
                                                     coords[ci++],
                                                     coords[ci++],
                                                     coords[ci++],
                                                     coords[ci++],
                                                     endx = coords[ci++],
                                                     endy = coords[ci++],
                                                     0);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.SEG_CLOSE:
                    if (cury != movy) {
                        crossings +=
                            Curve.pointCrossingsForLine(px, py,
                                                        curx, cury,
                                                        movx, movy);
                    }
                    curx = movx;
                    cury = movy;
                    break;
                }
            }
            if (cury != movy) {
                crossings +=
                    Curve.pointCrossingsForLine(px, py,
                                                curx, cury,
                                                movx, movy);
            }
            return crossings;
        }

        int rectCrossings(double rxmin, double rymin,
                          double rxmax, double rymax)
        {
            if (numTypes == 0) {
                return 0;
            }
            float coords[] = floatCoords;
            double curx, cury, movx, movy, endx, endy;
            curx = movx = coords[0];
            cury = movy = coords[1];
            int crossings = 0;
            int ci = 2;
            for (int i = 1;
                 crossings != Curve.RECT_INTERSECTS && i < numTypes;
                 i++)
            {
                switch (pointTypes[i]) {
                case PathIterator.SEG_MOVETO:
                    if (curx != movx || cury != movy) {
                        crossings =
                            Curve.rectCrossingsForLine(crossings,
                                                       rxmin, rymin,
                                                       rxmax, rymax,
                                                       curx, cury,
                                                       movx, movy);
                    }
                    // 计数在这里应始终是 2 的倍数。
                    // assert((crossings & 1) != 0);
                    movx = curx = coords[ci++];
                    movy = cury = coords[ci++];
                    break;
                case PathIterator.SEG_LINETO:
                    crossings =
                        Curve.rectCrossingsForLine(crossings,
                                                   rxmin, rymin,
                                                   rxmax, rymax,
                                                   curx, cury,
                                                   endx = coords[ci++],
                                                   endy = coords[ci++]);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.SEG_QUADTO:
                    crossings =
                        Curve.rectCrossingsForQuad(crossings,
                                                   rxmin, rymin,
                                                   rxmax, rymax,
                                                   curx, cury,
                                                   coords[ci++],
                                                   coords[ci++],
                                                   endx = coords[ci++],
                                                   endy = coords[ci++],
                                                   0);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.SEG_CUBICTO:
                    crossings =
                        Curve.rectCrossingsForCubic(crossings,
                                                    rxmin, rymin,
                                                    rxmax, rymax,
                                                    curx, cury,
                                                    coords[ci++],
                                                    coords[ci++],
                                                    coords[ci++],
                                                    coords[ci++],
                                                    endx = coords[ci++],
                                                    endy = coords[ci++],
                                                    0);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.SEG_CLOSE:
                    if (curx != movx || cury != movy) {
                        crossings =
                            Curve.rectCrossingsForLine(crossings,
                                                       rxmin, rymin,
                                                       rxmax, rymax,
                                                       curx, cury,
                                                       movx, movy);
                    }
                    curx = movx;
                    cury = movy;
                    // 计数在这里应始终是 2 的倍数。
                    // assert((crossings & 1) != 0);
                    break;
                }
            }
            if (crossings != Curve.RECT_INTERSECTS &&
                (curx != movx || cury != movy))
            {
                crossings =
                    Curve.rectCrossingsForLine(crossings,
                                               rxmin, rymin,
                                               rxmax, rymax,
                                               curx, cury,
                                               movx, movy);
            }
            // 计数在这里应始终是 2 的倍数。
            // assert((crossings & 1) != 0);
            return crossings;
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public final void append(PathIterator pi, boolean connect) {
            float coords[] = new float[6];
            while (!pi.isDone()) {
                switch (pi.currentSegment(coords)) {
                case SEG_MOVETO:
                    if (!connect || numTypes < 1 || numCoords < 1) {
                        moveTo(coords[0], coords[1]);
                        break;
                    }
                    if (pointTypes[numTypes - 1] != SEG_CLOSE &&
                        floatCoords[numCoords-2] == coords[0] &&
                        floatCoords[numCoords-1] == coords[1])
                    {
                        // 压缩初始 moveto/lineto
                        break;
                    }
                    lineTo(coords[0], coords[1]);
                    break;
                case SEG_LINETO:
                    lineTo(coords[0], coords[1]);
                    break;
                case SEG_QUADTO:
                    quadTo(coords[0], coords[1],
                           coords[2], coords[3]);
                    break;
                case SEG_CUBICTO:
                    curveTo(coords[0], coords[1],
                            coords[2], coords[3],
                            coords[4], coords[5]);
                    break;
                case SEG_CLOSE:
                    closePath();
                    break;
                }
                pi.next();
                connect = false;
            }
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public final void transform(AffineTransform at) {
            at.transform(floatCoords, 0, floatCoords, 0, numCoords / 2);
        }


        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public final synchronized Rectangle2D getBounds2D() {
            float x1, y1, x2, y2;
            int i = numCoords;
            if (i > 0) {
                y1 = y2 = floatCoords[--i];
                x1 = x2 = floatCoords[--i];
                while (i > 0) {
                    float y = floatCoords[--i];
                    float x = floatCoords[--i];
                    if (x < x1) x1 = x;
                    if (y < y1) y1 = y;
                    if (x > x2) x2 = x;
                    if (y > y2) y2 = y;
                }
            } else {
                x1 = y1 = x2 = y2 = 0.0f;
            }
            return new Rectangle2D.Float(x1, y1, x2 - x1, y2 - y1);
        }

        /**
         * {@inheritDoc}
         * <p>
         * 该类的迭代器不是线程安全的，这意味着 {@code Path2D} 类不保证对
         * 此 {@code Path2D} 对象的几何形状的修改不会影响已经进行中的
         * 几何形状的迭代。
         *
         * @since 1.6
         */
        public final PathIterator getPathIterator(AffineTransform at) {
            if (at == null) {
                return new CopyIterator(this);
            } else {
                return new TxIterator(this, at);
            }
        }

        /**
         * 创建一个与此对象相同类的新对象。
         *
         * @return     该实例的克隆。
         * @exception  OutOfMemoryError    如果内存不足。
         * @see        java.lang.Cloneable
         * @since      1.6
         */
        public final Object clone() {
            // 注意：最好让这个方法返回 Path2D
            // 但我们的一个子类（GeneralPath）需要提供 "public Object clone()"
            // 以保持向后兼容，因此我们不能进一步限制它。
            // REMIND: 我们可以同时实现吗？
            if (this instanceof GeneralPath) {
                return new GeneralPath(this);
            } else {
                return new Path2D.Float(this);
            }
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = 6990832515060788886L;

        /**
         * 将默认的可序列化字段写入 {@code ObjectOutputStream}，然后显式序列化
         * 存储在此路径中的路径段。
         *
         * @serialData
         * <a name="Path2DSerialData"><!-- --></a>
         * <ol>
         * <li>默认的可序列化字段。
         * 从 1.6 开始没有默认的可序列化字段。
         * <li>然后是
         * 一个字节，表示原始对象的存储类型作为提示（SERIAL_STORAGE_FLT_ARRAY）
         * <li>然后是
         * 一个整数，表示要跟随的路径段数（NP）
         * 或 -1 表示未知数量的路径段跟随
         * <li>然后是
         * 一个整数，表示要跟随的坐标总数（NC）
         * 或 -1 表示未知数量的坐标跟随
         * （NC 应该总是偶数，因为坐标总是成对出现，表示 x, y 对）
         * <li>然后是
         * 一个字节，表示绕组规则
         * （{@link #WIND_EVEN_ODD WIND_EVEN_ODD} 或
         *  {@link #WIND_NON_ZERO WIND_NON_ZERO}）
         * <li>然后是
         * {@code NP}（或无限，如果 {@code NP < 0}）组值，每组由
         * 一个字节表示路径段类型
         * 后跟一个或多个浮点或双精度值对，表示路径段的坐标
         * <li>然后是
         * 一个字节，表示路径的结束（SERIAL_PATH_END）。
         * </ol>
         * <p>
         * 以下字节值常量用于 {@code Path2D} 对象的序列化形式：
         * <table>
         * <tr>
         * <th>常量名称</th>
         * <th>字节值</th>
         * <th>后跟</th>
         * <th>描述</th>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_STORAGE_FLT_ARRAY}</td>
         * <td>0x30</td>
         * <td></td>
         * <td>提示原始 {@code Path2D} 对象将坐标存储在 Java 浮点数组中。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_STORAGE_DBL_ARRAY}</td>
         * <td>0x31</td>
         * <td></td>
         * <td>提示原始 {@code Path2D} 对象将坐标存储在 Java 双精度数组中。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_FLT_MOVETO}</td>
         * <td>0x40</td>
         * <td>2 个浮点数</td>
         * <td>一个 {@link #moveTo moveTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_FLT_LINETO}</td>
         * <td>0x41</td>
         * <td>2 个浮点数</td>
         * <td>一个 {@link #lineTo lineTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_FLT_QUADTO}</td>
         * <td>0x42</td>
         * <td>4 个浮点数</td>
         * <td>一个 {@link #quadTo quadTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_FLT_CUBICTO}</td>
         * <td>0x43</td>
         * <td>6 个浮点数</td>
         * <td>一个 {@link #curveTo curveTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_DBL_MOVETO}</td>
         * <td>0x50</td>
         * <td>2 个双精度数</td>
         * <td>一个 {@link #moveTo moveTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_DBL_LINETO}</td>
         * <td>0x51</td>
         * <td>2 个双精度数</td>
         * <td>一个 {@link #lineTo lineTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_DBL_QUADTO}</td>
         * <td>0x52</td>
         * <td>4 个双精度数</td>
         * <td>一个 {@link #curveTo curveTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_DBL_CUBICTO}</td>
         * <td>0x53</td>
         * <td>6 个双精度数</td>
         * <td>一个 {@link #curveTo curveTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_CLOSE}</td>
         * <td>0x60</td>
         * <td></td>
         * <td>一个 {@link #closePath closePath} 路径段。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_PATH_END}</td>
         * <td>0x61</td>
         * <td></td>
         * <td>没有更多的路径段跟随。</td>
         * </table>
         *
         * @since 1.6
         */
        private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException
        {
            super.writeObject(s, false);
        }

        /**
         * 从 {@code ObjectInputStream} 读取默认的可序列化字段，然后显式序列化
         * 存储在此路径中的路径段。
         * <p>
         * 从 1.6 开始没有默认的可序列化字段。
         * <p>
         * 该对象的序列化数据在 writeObject 方法中描述。
         *
         * @since 1.6
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.lang.ClassNotFoundException, java.io.IOException
        {
            super.readObject(s, false);
        }

        static class CopyIterator extends Path2D.Iterator {
            float floatCoords[];

            CopyIterator(Path2D.Float p2df) {
                super(p2df);
                this.floatCoords = p2df.floatCoords;
            }

            public int currentSegment(float[] coords) {
                int type = path.pointTypes[typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    System.arraycopy(floatCoords, pointIdx,
                                     coords, 0, numCoords);
                }
                return type;
            }

            public int currentSegment(double[] coords) {
                int type = path.pointTypes[typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    for (int i = 0; i < numCoords; i++) {
                        coords[i] = floatCoords[pointIdx + i];
                    }
                }
                return type;
            }
        }

        static class TxIterator extends Path2D.Iterator {
            float floatCoords[];
            AffineTransform affine;

            TxIterator(Path2D.Float p2df, AffineTransform at) {
                super(p2df);
                this.floatCoords = p2df.floatCoords;
                this.affine = at;
            }

            public int currentSegment(float[] coords) {
                int type = path.pointTypes[typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    affine.transform(floatCoords, pointIdx,
                                     coords, 0, numCoords / 2);
                }
                return type;
            }

            public int currentSegment(double[] coords) {
                int type = path.pointTypes[typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    affine.transform(floatCoords, pointIdx,
                                     coords, 0, numCoords / 2);
                }
                return type;
            }
        }

    }

    /**
     * {@code Double} 类定义了一个几何路径，坐标存储为双精度浮点数。
     *
     * @since 1.6
     */
    public static class Double extends Path2D implements Serializable {
        transient double doubleCoords[];

        /**
         * 构造一个新的空的双精度 {@code Path2D} 对象，使用默认的绕组规则
         * {@link #WIND_NON_ZERO}。
         *
         * @since 1.6
         */
        public Double() {
            this(WIND_NON_ZERO, INIT_SIZE);
        }

        /**
         * 构造一个新的空的双精度 {@code Path2D} 对象，使用指定的绕组规则来控制
         * 需要定义路径内部的操作。
         *
         * @param rule 绕组规则
         * @see #WIND_EVEN_ODD
         * @see #WIND_NON_ZERO
         * @since 1.6
         */
        public Double(int rule) {
            this(rule, INIT_SIZE);
        }

        /**
         * 构造一个新的空的双精度 {@code Path2D} 对象，使用指定的绕组规则和
         * 指定的初始容量来存储路径段。
         * 这个数字是对路径中路径段数量的初始估计，但存储会根据需要扩展以存储
         * 添加到此路径中的任何路径段。
         *
         * @param rule 绕组规则
         * @param initialCapacity 路径中路径段数量的估计
         * @see #WIND_EVEN_ODD
         * @see #WIND_NON_ZERO
         * @since 1.6
         */
        public Double(int rule, int initialCapacity) {
            super(rule, initialCapacity);
            doubleCoords = new double[initialCapacity * 2];
        }

        /**
         * 从任意 {@link Shape} 对象构造一个新的双精度 {@code Path2D} 对象。
         * 该路径的所有初始几何形状和绕组规则都来自指定的 {@code Shape} 对象。
         *
         * @param s 指定的 {@code Shape} 对象
         * @since 1.6
         */
        public Double(Shape s) {
            this(s, null);
        }

        /**
         * 从任意 {@link Shape} 对象构造一个新的双精度 {@code Path2D} 对象，并
         * 通过 {@link AffineTransform} 对象进行变换。
         * 该路径的所有初始几何形状和绕组规则都来自指定的 {@code Shape} 对象，并
         * 通过指定的 {@code AffineTransform} 对象进行变换。
         *
         * @param s 指定的 {@code Shape} 对象
         * @param at 指定的 {@code AffineTransform} 对象
         * @since 1.6
         */
        public Double(Shape s, AffineTransform at) {
            if (s instanceof Path2D) {
                Path2D p2d = (Path2D) s;
                setWindingRule(p2d.windingRule);
                this.numTypes = p2d.numTypes;
                // 裁剪数组：
                this.pointTypes = Arrays.copyOf(p2d.pointTypes, p2d.numTypes);
                this.numCoords = p2d.numCoords;
                this.doubleCoords = p2d.cloneCoordsDouble(at);
            } else {
                PathIterator pi = s.getPathIterator(at);
                setWindingRule(pi.getWindingRule());
                this.pointTypes = new byte[INIT_SIZE];
                this.doubleCoords = new double[INIT_SIZE * 2];
                append(pi, false);
            }
        }

        @Override
        float[] cloneCoordsFloat(AffineTransform at) {
            // 裁剪数组：
            float ret[] = new float[numCoords];
            if (at == null) {
                for (int i = 0; i < numCoords; i++) {
                    ret[i] = (float) doubleCoords[i];
                }
            } else {
                at.transform(doubleCoords, 0, ret, 0, numCoords / 2);
            }
            return ret;
        }

        @Override
        double[] cloneCoordsDouble(AffineTransform at) {
            // 裁剪数组：
            double ret[];
            if (at == null) {
                ret = Arrays.copyOf(doubleCoords, numCoords);
            } else {
                ret = new double[numCoords];
                at.transform(doubleCoords, 0, ret, 0, numCoords / 2);
            }
            return ret;
        }

        void append(float x, float y) {
            doubleCoords[numCoords++] = x;
            doubleCoords[numCoords++] = y;
        }

        void append(double x, double y) {
            doubleCoords[numCoords++] = x;
            doubleCoords[numCoords++] = y;
        }

        Point2D getPoint(int coordindex) {
            return new Point2D.Double(doubleCoords[coordindex],
                                      doubleCoords[coordindex+1]);
        }

        @Override
        void needRoom(boolean needMove, int newCoords) {
            if ((numTypes == 0) && needMove) {
                throw new IllegalPathStateException("在路径定义中缺少初始 moveto");
            }
            if (numTypes >= pointTypes.length) {
                pointTypes = expandPointTypes(pointTypes, 1);
            }
            if (numCoords > (doubleCoords.length - newCoords)) {
                doubleCoords = expandCoords(doubleCoords, newCoords);
            }
        }

        static double[] expandCoords(double[] oldCoords, int needed) {
            final int oldSize = oldCoords.length;
            final int newSizeMin = oldSize + needed;
            if (newSizeMin < oldSize) {
                // 硬溢出失败 - 我们甚至无法容纳
                // 新项目而不溢出
                throw new ArrayIndexOutOfBoundsException(
                              "坐标超出最大容量！");
            }
            // 增长算法计算
            int grow = oldSize;
            if (grow > EXPAND_MAX_COORDS) {
                grow = Math.max(EXPAND_MAX_COORDS, oldSize >> 3); // 1/8 最小
            } else if (grow < EXPAND_MIN) {
                grow = EXPAND_MIN;
            }
            assert grow > needed;


                        int newSize = oldSize + grow;
            if (newSize < newSizeMin) {
                // 溢出计算
                newSize = Integer.MAX_VALUE;
            }
            while (true) {
                try {
                    // 尝试分配更大的数组
                    return Arrays.copyOf(oldCoords, newSize);
                } catch (OutOfMemoryError oome) {
                    if (newSize == newSizeMin) {
                        throw oome;
                    }
                }
                newSize = newSizeMin + (newSize - newSizeMin) / 2;
            }
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public final synchronized void moveTo(double x, double y) {
            if (numTypes > 0 && pointTypes[numTypes - 1] == SEG_MOVETO) {
                doubleCoords[numCoords-2] = x;
                doubleCoords[numCoords-1] = y;
            } else {
                needRoom(false, 2);
                pointTypes[numTypes++] = SEG_MOVETO;
                doubleCoords[numCoords++] = x;
                doubleCoords[numCoords++] = y;
            }
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public final synchronized void lineTo(double x, double y) {
            needRoom(true, 2);
            pointTypes[numTypes++] = SEG_LINETO;
            doubleCoords[numCoords++] = x;
            doubleCoords[numCoords++] = y;
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public final synchronized void quadTo(double x1, double y1,
                                              double x2, double y2)
        {
            needRoom(true, 4);
            pointTypes[numTypes++] = SEG_QUADTO;
            doubleCoords[numCoords++] = x1;
            doubleCoords[numCoords++] = y1;
            doubleCoords[numCoords++] = x2;
            doubleCoords[numCoords++] = y2;
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public final synchronized void curveTo(double x1, double y1,
                                               double x2, double y2,
                                               double x3, double y3)
        {
            needRoom(true, 6);
            pointTypes[numTypes++] = SEG_CUBICTO;
            doubleCoords[numCoords++] = x1;
            doubleCoords[numCoords++] = y1;
            doubleCoords[numCoords++] = x2;
            doubleCoords[numCoords++] = y2;
            doubleCoords[numCoords++] = x3;
            doubleCoords[numCoords++] = y3;
        }

        int pointCrossings(double px, double py) {
            if (numTypes == 0) {
                return 0;
            }
            double movx, movy, curx, cury, endx, endy;
            double coords[] = doubleCoords;
            curx = movx = coords[0];
            cury = movy = coords[1];
            int crossings = 0;
            int ci = 2;
            for (int i = 1; i < numTypes; i++) {
                switch (pointTypes[i]) {
                case PathIterator.SEG_MOVETO:
                    if (cury != movy) {
                        crossings +=
                            Curve.pointCrossingsForLine(px, py,
                                                        curx, cury,
                                                        movx, movy);
                    }
                    movx = curx = coords[ci++];
                    movy = cury = coords[ci++];
                    break;
                case PathIterator.SEG_LINETO:
                    crossings +=
                        Curve.pointCrossingsForLine(px, py,
                                                    curx, cury,
                                                    endx = coords[ci++],
                                                    endy = coords[ci++]);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.SEG_QUADTO:
                    crossings +=
                        Curve.pointCrossingsForQuad(px, py,
                                                    curx, cury,
                                                    coords[ci++],
                                                    coords[ci++],
                                                    endx = coords[ci++],
                                                    endy = coords[ci++],
                                                    0);
                    curx = endx;
                    cury = endy;
                    break;
            case PathIterator.SEG_CUBICTO:
                    crossings +=
                        Curve.pointCrossingsForCubic(px, py,
                                                     curx, cury,
                                                     coords[ci++],
                                                     coords[ci++],
                                                     coords[ci++],
                                                     coords[ci++],
                                                     endx = coords[ci++],
                                                     endy = coords[ci++],
                                                     0);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.SEG_CLOSE:
                    if (cury != movy) {
                        crossings +=
                            Curve.pointCrossingsForLine(px, py,
                                                        curx, cury,
                                                        movx, movy);
                    }
                    curx = movx;
                    cury = movy;
                    break;
                }
            }
            if (cury != movy) {
                crossings +=
                    Curve.pointCrossingsForLine(px, py,
                                                curx, cury,
                                                movx, movy);
            }
            return crossings;
        }

        int rectCrossings(double rxmin, double rymin,
                          double rxmax, double rymax)
        {
            if (numTypes == 0) {
                return 0;
            }
            double coords[] = doubleCoords;
            double curx, cury, movx, movy, endx, endy;
            curx = movx = coords[0];
            cury = movy = coords[1];
            int crossings = 0;
            int ci = 2;
            for (int i = 1;
                 crossings != Curve.RECT_INTERSECTS && i < numTypes;
                 i++)
            {
                switch (pointTypes[i]) {
                case PathIterator.SEG_MOVETO:
                    if (curx != movx || cury != movy) {
                        crossings =
                            Curve.rectCrossingsForLine(crossings,
                                                       rxmin, rymin,
                                                       rxmax, rymax,
                                                       curx, cury,
                                                       movx, movy);
                    }
                    // 计数应始终是2的倍数。
                    // assert((crossings & 1) != 0);
                    movx = curx = coords[ci++];
                    movy = cury = coords[ci++];
                    break;
                case PathIterator.SEG_LINETO:
                    endx = coords[ci++];
                    endy = coords[ci++];
                    crossings =
                        Curve.rectCrossingsForLine(crossings,
                                                   rxmin, rymin,
                                                   rxmax, rymax,
                                                   curx, cury,
                                                   endx, endy);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.SEG_QUADTO:
                    crossings =
                        Curve.rectCrossingsForQuad(crossings,
                                                   rxmin, rymin,
                                                   rxmax, rymax,
                                                   curx, cury,
                                                   coords[ci++],
                                                   coords[ci++],
                                                   endx = coords[ci++],
                                                   endy = coords[ci++],
                                                   0);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.SEG_CUBICTO:
                    crossings =
                        Curve.rectCrossingsForCubic(crossings,
                                                    rxmin, rymin,
                                                    rxmax, rymax,
                                                    curx, cury,
                                                    coords[ci++],
                                                    coords[ci++],
                                                    coords[ci++],
                                                    coords[ci++],
                                                    endx = coords[ci++],
                                                    endy = coords[ci++],
                                                    0);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.SEG_CLOSE:
                    if (curx != movx || cury != movy) {
                        crossings =
                            Curve.rectCrossingsForLine(crossings,
                                                       rxmin, rymin,
                                                       rxmax, rymax,
                                                       curx, cury,
                                                       movx, movy);
                    }
                    curx = movx;
                    cury = movy;
                    // 计数应始终是2的倍数。
                    // assert((crossings & 1) != 0);
                    break;
                }
            }
            if (crossings != Curve.RECT_INTERSECTS &&
                (curx != movx || cury != movy))
            {
                crossings =
                    Curve.rectCrossingsForLine(crossings,
                                               rxmin, rymin,
                                               rxmax, rymax,
                                               curx, cury,
                                               movx, movy);
            }
            // 计数应始终是2的倍数。
            // assert((crossings & 1) != 0);
            return crossings;
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public final void append(PathIterator pi, boolean connect) {
            double coords[] = new double[6];
            while (!pi.isDone()) {
                switch (pi.currentSegment(coords)) {
                case SEG_MOVETO:
                    if (!connect || numTypes < 1 || numCoords < 1) {
                        moveTo(coords[0], coords[1]);
                        break;
                    }
                    if (pointTypes[numTypes - 1] != SEG_CLOSE &&
                        doubleCoords[numCoords-2] == coords[0] &&
                        doubleCoords[numCoords-1] == coords[1])
                    {
                        // 压缩初始的 moveto/lineto
                        break;
                    }
                    lineTo(coords[0], coords[1]);
                    break;
                case SEG_LINETO:
                    lineTo(coords[0], coords[1]);
                    break;
                case SEG_QUADTO:
                    quadTo(coords[0], coords[1],
                           coords[2], coords[3]);
                    break;
                case SEG_CUBICTO:
                    curveTo(coords[0], coords[1],
                            coords[2], coords[3],
                            coords[4], coords[5]);
                    break;
                case SEG_CLOSE:
                    closePath();
                    break;
                }
                pi.next();
                connect = false;
            }
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public final void transform(AffineTransform at) {
            at.transform(doubleCoords, 0, doubleCoords, 0, numCoords / 2);
        }

        /**
         * {@inheritDoc}
         * <p>
         * 该类的迭代器不是线程安全的，
         * 这意味着 {@code Path2D} 类不保证对
         * 此 {@code Path2D} 对象的几何形状的修改
         * 不会影响已经进行中的该几何形状的迭代。
         *
         * @param at 一个 {@code AffineTransform}
         * @return 一个新的 {@code PathIterator}，它沿着此 {@code Shape} 的边界迭代
         *         并提供对此 {@code Shape} 轮廓几何形状的访问
         * @since 1.6
         */
        public final PathIterator getPathIterator(AffineTransform at) {
            if (at == null) {
                return new CopyIterator(this);
            } else {
                return new TxIterator(this, at);
            }
        }

        /**
         * 创建一个与此对象相同类的新对象。
         *
         * @return     该实例的克隆。
         * @exception  OutOfMemoryError    如果没有足够的内存。
         * @see        java.lang.Cloneable
         * @since      1.6
         */
        public final Object clone() {
            // 注意：最好让其返回 Path2D
            // 但我们的一个子类 (GeneralPath) 需要提供 "public Object clone()"
            // 以保持向后兼容性，因此我们不能进一步限制它。
            // REMIND: Can we do both somehow?
            return new Path2D.Double(this);
        }

        /*
         * JDK 1.6 serialVersionUID
         */
        private static final long serialVersionUID = 1826762518450014216L;

        /**
         * 将默认的可序列化字段写入
         * {@code ObjectOutputStream}，然后显式序列化存储在此
         * 路径中的路径段。
         *
         * @serialData
         * <a name="Path2DSerialData"><!-- --></a>
         * <ol>
         * <li>默认的可序列化字段。
         * 1.6 版本中没有默认的可序列化字段。
         * <li>后跟
         * 一个字节，表示原始对象的存储类型作为提示 (SERIAL_STORAGE_DBL_ARRAY)
         * <li>后跟
         * 一个整数，表示要跟随的路径段数 (NP)
         * 或 -1 表示未知数量的路径段跟随
         * <li>后跟
         * 一个整数，表示要跟随的坐标总数 (NC)
         * 或 -1 表示未知数量的坐标跟随
         * (NC 应始终是偶数，因为坐标总是成对出现，表示一个 x,y 对)
         * <li>后跟
         * 一个字节，表示绕行规则
         * ({@link #WIND_EVEN_ODD WIND_EVEN_ODD} 或
         *  {@link #WIND_NON_ZERO WIND_NON_ZERO})
         * <li>后跟
         * {@code NP} (或无限多，如果 {@code NP < 0}) 组值，每组由
         * 一个字节表示路径段类型
         * 后跟一个或多个浮点或双精度值对，表示路径段的坐标
         * <li>后跟
         * 一个字节，表示路径的结束 (SERIAL_PATH_END)。
         * </ol>
         * <p>
         * 以下字节值常量用于 {@code Path2D} 对象的序列化形式：
         * <table>
         * <tr>
         * <th>常量名称</th>
         * <th>字节值</th>
         * <th>后跟</th>
         * <th>描述</th>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_STORAGE_FLT_ARRAY}</td>
         * <td>0x30</td>
         * <td></td>
         * <td>一个提示，表示原始 {@code Path2D} 对象将坐标存储在 Java 浮点数组中。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_STORAGE_DBL_ARRAY}</td>
         * <td>0x31</td>
         * <td></td>
         * <td>一个提示，表示原始 {@code Path2D} 对象将坐标存储在 Java 双精度数组中。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_FLT_MOVETO}</td>
         * <td>0x40</td>
         * <td>2 个浮点数</td>
         * <td>一个 {@link #moveTo moveTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_FLT_LINETO}</td>
         * <td>0x41</td>
         * <td>2 个浮点数</td>
         * <td>一个 {@link #lineTo lineTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_FLT_QUADTO}</td>
         * <td>0x42</td>
         * <td>4 个浮点数</td>
         * <td>一个 {@link #quadTo quadTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_FLT_CUBICTO}</td>
         * <td>0x43</td>
         * <td>6 个浮点数</td>
         * <td>一个 {@link #curveTo curveTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_DBL_MOVETO}</td>
         * <td>0x50</td>
         * <td>2 个双精度数</td>
         * <td>一个 {@link #moveTo moveTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_DBL_LINETO}</td>
         * <td>0x51</td>
         * <td>2 个双精度数</td>
         * <td>一个 {@link #lineTo lineTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_DBL_QUADTO}</td>
         * <td>0x52</td>
         * <td>4 个双精度数</td>
         * <td>一个 {@link #curveTo curveTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_DBL_CUBICTO}</td>
         * <td>0x53</td>
         * <td>6 个双精度数</td>
         * <td>一个 {@link #curveTo curveTo} 路径段跟随。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_SEG_CLOSE}</td>
         * <td>0x60</td>
         * <td></td>
         * <td>一个 {@link #closePath closePath} 路径段。</td>
         * </tr>
         * <tr>
         * <td>{@code SERIAL_PATH_END}</td>
         * <td>0x61</td>
         * <td></td>
         * <td>没有更多的路径段跟随。</td>
         * </table>
         *
         * @since 1.6
         */
        private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException
        {
            super.writeObject(s, true);
        }


                    /**
         * 从 {@code ObjectInputStream} 中读取默认的可序列化字段，然后显式序列化存储在此路径中的路径段。
         * <p>
         * 从 1.6 开始没有默认的可序列化字段。
         * <p>
         * 该对象的序列化数据在 writeObject 方法中描述。
         *
         * @since 1.6
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.lang.ClassNotFoundException, java.io.IOException
        {
            super.readObject(s, true);
        }

        static class CopyIterator extends Path2D.Iterator {
            double doubleCoords[];

            CopyIterator(Path2D.Double p2dd) {
                super(p2dd);
                this.doubleCoords = p2dd.doubleCoords;
            }

            public int currentSegment(float[] coords) {
                int type = path.pointTypes[typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    for (int i = 0; i < numCoords; i++) {
                        coords[i] = (float) doubleCoords[pointIdx + i];
                    }
                }
                return type;
            }

            public int currentSegment(double[] coords) {
                int type = path.pointTypes[typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    System.arraycopy(doubleCoords, pointIdx,
                                     coords, 0, numCoords);
                }
                return type;
            }
        }

        static class TxIterator extends Path2D.Iterator {
            double doubleCoords[];
            AffineTransform affine;

            TxIterator(Path2D.Double p2dd, AffineTransform at) {
                super(p2dd);
                this.doubleCoords = p2dd.doubleCoords;
                this.affine = at;
            }

            public int currentSegment(float[] coords) {
                int type = path.pointTypes[typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    affine.transform(doubleCoords, pointIdx,
                                     coords, 0, numCoords / 2);
                }
                return type;
            }

            public int currentSegment(double[] coords) {
                int type = path.pointTypes[typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    affine.transform(doubleCoords, pointIdx,
                                     coords, 0, numCoords / 2);
                }
                return type;
            }
        }
    }

    /**
     * 通过移动到指定的双精度坐标来向路径添加一个点。
     *
     * @param x 指定的 X 坐标
     * @param y 指定的 Y 坐标
     * @since 1.6
     */
    public abstract void moveTo(double x, double y);

    /**
     * 通过从当前坐标绘制一条直线到新的指定坐标来向路径添加一个点。
     *
     * @param x 指定的 X 坐标
     * @param y 指定的 Y 坐标
     * @since 1.6
     */
    public abstract void lineTo(double x, double y);

    /**
     * 通过绘制一条交于当前坐标和指定坐标 {@code (x2,y2)} 的二次曲线来向路径添加一个曲线段，
     * 使用指定的点 {@code (x1,y1)} 作为二次参数控制点。
     * 所有坐标均以双精度指定。
     *
     * @param x1 二次控制点的 X 坐标
     * @param y1 二次控制点的 Y 坐标
     * @param x2 最终终点的 X 坐标
     * @param y2 最终终点的 Y 坐标
     * @since 1.6
     */
    public abstract void quadTo(double x1, double y1,
                                double x2, double y2);

    /**
     * 通过绘制一条交于当前坐标和指定坐标 {@code (x3,y3)} 的贝塞尔曲线来向路径添加一个曲线段，
     * 使用指定的点 {@code (x1,y1)} 和 {@code (x2,y2)} 作为贝塞尔控制点。
     * 所有坐标均以双精度指定。
     *
     * @param x1 第一个贝塞尔控制点的 X 坐标
     * @param y1 第一个贝塞尔控制点的 Y 坐标
     * @param x2 第二个贝塞尔控制点的 X 坐标
     * @param y2 第二个贝塞尔控制点的 Y 坐标
     * @param x3 最终终点的 X 坐标
     * @param y3 最终终点的 Y 坐标
     * @since 1.6
     */
    public abstract void curveTo(double x1, double y1,
                                 double x2, double y2,
                                 double x3, double y3);

    /**
     * 通过绘制一条直线回到最后一个 {@code moveTo} 的坐标来关闭当前子路径。
     * 如果路径已经关闭，则此方法无效。
     *
     * @since 1.6
     */
    public final synchronized void closePath() {
        if (numTypes == 0 || pointTypes[numTypes - 1] != SEG_CLOSE) {
            needRoom(true, 0);
            pointTypes[numTypes++] = SEG_CLOSE;
        }
    }

    /**
     * 将指定的 {@code Shape} 对象的几何形状追加到路径中，可能通过线段将新几何形状连接到现有路径段。
     * 如果 {@code connect} 参数为 {@code true} 且路径不为空，则追加的 {@code Shape} 的初始 {@code moveTo} 将变为 {@code lineTo} 段。
     * 如果连接的 {@code lineTo} 段的目标坐标与当前打开的子路径的结束坐标匹配，则该段将被省略为多余的。
     * 忽略指定的 {@code Shape} 的绕组规则，追加的几何形状由此路径的绕组规则控制。
     *
     * @param s 要追加其几何形状的 {@code Shape}
     * @param connect 一个布尔值，用于控制是否将初始的 {@code moveTo} 段变为 {@code lineTo} 段以连接新几何形状到现有路径
     * @since 1.6
     */
    public final void append(Shape s, boolean connect) {
        append(s.getPathIterator(null), connect);
    }

    /**
     * 将指定的 {@link PathIterator} 对象的几何形状追加到路径中，可能通过线段将新几何形状连接到现有路径段。
     * 如果 {@code connect} 参数为 {@code true} 且路径不为空，则追加的 {@code Shape} 的初始 {@code moveTo} 将变为 {@code lineTo} 段。
     * 如果连接的 {@code lineTo} 段的目标坐标与当前打开的子路径的结束坐标匹配，则该段将被省略为多余的。
     * 忽略指定的 {@code Shape} 的绕组规则，追加的几何形状由此路径的绕组规则控制。
     *
     * @param pi 要追加其几何形状的 {@code PathIterator}
     * @param connect 一个布尔值，用于控制是否将初始的 {@code moveTo} 段变为 {@code lineTo} 段以连接新几何形状到现有路径
     * @since 1.6
     */
    public abstract void append(PathIterator pi, boolean connect);

    /**
     * 返回填充样式绕组规则。
     *
     * @return 一个表示当前绕组规则的整数。
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     * @see #setWindingRule
     * @since 1.6
     */
    public final synchronized int getWindingRule() {
        return windingRule;
    }

    /**
     * 将此路径的绕组规则设置为指定的值。
     *
     * @param rule 一个表示指定绕组规则的整数
     * @exception IllegalArgumentException 如果
     *          {@code rule} 不是
     *          {@link #WIND_EVEN_ODD} 或
     *          {@link #WIND_NON_ZERO}
     * @see #getWindingRule
     * @since 1.6
     */
    public final void setWindingRule(int rule) {
        if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO) {
            throw new IllegalArgumentException("绕组规则必须是 " +
                                               "WIND_EVEN_ODD 或 " +
                                               "WIND_NON_ZERO");
        }
        windingRule = rule;
    }

    /**
     * 返回路径末尾最近添加的坐标作为一个 {@link Point2D} 对象。
     *
     * @return 一个包含路径结束坐标的 {@code Point2D} 对象，如果路径中没有点，则返回 {@code null}。
     * @since 1.6
     */
    public final synchronized Point2D getCurrentPoint() {
        int index = numCoords;
        if (numTypes < 1 || index < 1) {
            return null;
        }
        if (pointTypes[numTypes - 1] == SEG_CLOSE) {
        loop:
            for (int i = numTypes - 2; i > 0; i--) {
                switch (pointTypes[i]) {
                case SEG_MOVETO:
                    break loop;
                case SEG_LINETO:
                    index -= 2;
                    break;
                case SEG_QUADTO:
                    index -= 4;
                    break;
                case SEG_CUBICTO:
                    index -= 6;
                    break;
                case SEG_CLOSE:
                    break;
                }
            }
        }
        return getPoint(index - 2);
    }

    /**
     * 将路径重置为空。将追加位置设置回路径的开头，并忘记所有坐标和点类型。
     *
     * @since 1.6
     */
    public final synchronized void reset() {
        numTypes = numCoords = 0;
    }

    /**
     * 使用指定的 {@link AffineTransform} 转换此路径的几何形状。
     * 几何形状在原地转换，这会永久改变此对象定义的边界。
     *
     * @param at 用于转换区域的 {@code AffineTransform}
     * @since 1.6
     */
    public abstract void transform(AffineTransform at);

    /**
     * 返回一个表示此 {@code Path2D} 转换版本的新 {@code Shape}。
     * 注意，此方法的返回值的类型和坐标精度未指定。
     * 该方法将返回一个包含不比此 {@code Path2D} 当前维护的转换几何形状精度低的 Shape，但精度也可能不更高。
     * 如果结果的精度与存储大小之间的权衡很重要，则应使用
     * {@link Path2D.Float#Path2D.Float(Shape, AffineTransform) Path2D.Float}
     * 和
     * {@link Path2D.Double#Path2D.Double(Shape, AffineTransform) Path2D.Double}
     * 子类中的便利构造函数来明确选择。
     *
     * @param at 用于转换新 {@code Shape} 的 {@code AffineTransform}
     * @return 一个使用指定的 {@code AffineTransform} 转换的新 {@code Shape}。
     * @since 1.6
     */
    public final synchronized Shape createTransformedShape(AffineTransform at) {
        Path2D p2d = (Path2D) clone();
        if (at != null) {
            p2d.transform(at);
        }
        return p2d;
    }

    /**
     * {@inheritDoc}
     * @since 1.6
     */
    public final Rectangle getBounds() {
        return getBounds2D().getBounds();
    }

    /**
     * 测试指定的坐标是否在指定的 {@link PathIterator} 的闭合边界内。
     * <p>
     * 此方法为 {@link Shape} 接口的实现者提供了一个基本设施，以实现对
     * {@link Shape#contains(double, double)} 方法的支持。
     *
     * @param pi 指定的 {@code PathIterator}
     * @param x 指定的 X 坐标
     * @param y 指定的 Y 坐标
     * @return 如果指定的坐标在指定的 {@code PathIterator} 内，则返回 {@code true}；否则返回 {@code false}
     * @since 1.6
     */
    public static boolean contains(PathIterator pi, double x, double y) {
        if (x * 0.0 + y * 0.0 == 0.0) {
            /* N * 0.0 是 0.0 仅当 N 是有限的。
             * 这里我们知道 x 和 y 都是有限的。
             */
            int mask = (pi.getWindingRule() == WIND_NON_ZERO ? -1 : 1);
            int cross = Curve.pointCrossingsForPath(pi, x, y);
            return ((cross & mask) != 0);
        } else {
            /* x 或 y 是无限或 NaN。
             * NaN 在任何测试中总是产生否定的响应，
             * 无限值不能在任何路径内，因此也应该返回 false。
             */
            return false;
        }
    }

    /**
     * 测试指定的 {@link Point2D} 是否在指定的 {@link PathIterator} 的闭合边界内。
     * <p>
     * 此方法为 {@link Shape} 接口的实现者提供了一个基本设施，以实现对
     * {@link Shape#contains(Point2D)} 方法的支持。
     *
     * @param pi 指定的 {@code PathIterator}
     * @param p 指定的 {@code Point2D}
     * @return 如果指定的坐标在指定的 {@code PathIterator} 内，则返回 {@code true}；否则返回 {@code false}
     * @since 1.6
     */
    public static boolean contains(PathIterator pi, Point2D p) {
        return contains(pi, p.getX(), p.getY());
    }

    /**
     * {@inheritDoc}
     * @since 1.6
     */
    public final boolean contains(double x, double y) {
        if (x * 0.0 + y * 0.0 == 0.0) {
            /* N * 0.0 是 0.0 仅当 N 是有限的。
             * 这里我们知道 x 和 y 都是有限的。
             */
            if (numTypes < 2) {
                return false;
            }
            int mask = (windingRule == WIND_NON_ZERO ? -1 : 1);
            return ((pointCrossings(x, y) & mask) != 0);
        } else {
            /* x 或 y 是无限或 NaN。
             * NaN 在任何测试中总是产生否定的响应，
             * 无限值不能在任何路径内，因此也应该返回 false。
             */
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * @since 1.6
     */
    public final boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    /**
     * 测试指定的矩形区域是否完全在指定的 {@link PathIterator} 的闭合边界内。
     * <p>
     * 此方法为 {@link Shape} 接口的实现者提供了一个基本设施，以实现对
     * {@link Shape#contains(double, double, double, double)} 方法的支持。
     * <p>
     * 此方法对象可能在指定的矩形区域与路径的某一段相交但该段不是路径内部和外部之间的边界时保守地返回 false。
     * 这样的段如果属于具有 {@link #WIND_NON_ZERO} 绕组规则的路径，或者这些段以相反的方向重叠，使得两组段相互抵消而没有外部区域落在它们之间，则可能完全位于路径的内部。
     * 要确定段是否代表路径内部的真实边界需要对路径的所有段和绕组规则进行广泛的计算，这超出了此实现的范围。
     *
     * @param pi 指定的 {@code PathIterator}
     * @param x 指定的 X 坐标
     * @param y 指定的 Y 坐标
     * @param w 指定的矩形区域的宽度
     * @param h 指定的矩形区域的高度
     * @return 如果指定的 {@code PathIterator} 包含指定的矩形区域，则返回 {@code true}；否则返回 {@code false}。
     * @since 1.6
     */
    public static boolean contains(PathIterator pi,
                                   double x, double y, double w, double h)
    {
        if (java.lang.Double.isNaN(x+w) || java.lang.Double.isNaN(y+h)) {
            /* [xy]+[wh] 是 NaN 如果这些值中的任何一个为 NaN，
             * 或者将它们相加会产生 NaN（因为加了相反的无限值）。
             * 由于我们下面需要将它们相加，它们的和不能是 NaN。
             * 我们返回 false，因为 NaN 总是在测试中产生否定的响应。
             */
            return false;
        }
        if (w <= 0 || h <= 0) {
            return false;
        }
        int mask = (pi.getWindingRule() == WIND_NON_ZERO ? -1 : 2);
        int crossings = Curve.rectCrossingsForPath(pi, x, y, x+w, y+h);
        return (crossings != Curve.RECT_INTERSECTS &&
                (crossings & mask) != 0);
    }


/**
 * 测试指定的 {@link Rectangle2D} 是否完全位于指定的 {@link PathIterator} 的闭合边界内。
 * <p>
 * 此方法为 {@link Shape} 接口的实现者提供了一个基本设施，以实现对 {@link Shape#contains(Rectangle2D)} 方法的支持。
 * <p>
 * 此方法对象在某些情况下可能会保守地返回 false，即指定的矩形区域与路径的某个段相交，但该段并不表示路径的内部和外部之间的边界。
 * 这样的段可能完全位于路径的内部，如果它们是具有 {@link #WIND_NON_ZERO} 绕组规则的路径的一部分，或者这些段以相反的方向重叠，使得两组段相互抵消，而没有外部区域位于它们之间。
 * 要确定段是否代表路径内部的真实边界，需要进行广泛的计算，涉及路径的所有段和绕组规则，这超出了此实现的范围。
 *
 * @param pi 指定的 {@code PathIterator}
 * @param r 指定的 {@code Rectangle2D}
 * @return 如果指定的 {@code PathIterator} 包含指定的 {@code Rectangle2D}，则返回 {@code true}；否则返回 {@code false}。
 * @since 1.6
 */
public static boolean contains(PathIterator pi, Rectangle2D r) {
    return contains(pi, r.getX(), r.getY(), r.getWidth(), r.getHeight());
}

/**
 * {@inheritDoc}
 * <p>
 * 此方法对象在某些情况下可能会保守地返回 false，即指定的矩形区域与路径的某个段相交，但该段并不表示路径的内部和外部之间的边界。
 * 这样的段可能完全位于路径的内部，如果它们是具有 {@link #WIND_NON_ZERO} 绕组规则的路径的一部分，或者这些段以相反的方向重叠，使得两组段相互抵消，而没有外部区域位于它们之间。
 * 要确定段是否代表路径内部的真实边界，需要进行广泛的计算，涉及路径的所有段和绕组规则，这超出了此实现的范围。
 *
 * @since 1.6
 */
public final boolean contains(double x, double y, double w, double h) {
    if (java.lang.Double.isNaN(x+w) || java.lang.Double.isNaN(y+h)) {
        /* [xy]+[wh] 是 NaN，如果这些值中的任何一个为 NaN，
         * 或者将它们相加会产生 NaN，因为它们是相反的无穷大值。
         * 由于我们需要在下面将它们相加，它们的和不能为 NaN。
         * 我们返回 false，因为 NaN 总是产生测试的负响应
         */
        return false;
    }
    if (w <= 0 || h <= 0) {
        return false;
    }
    int mask = (windingRule == WIND_NON_ZERO ? -1 : 2);
    int crossings = rectCrossings(x, y, x+w, y+h);
    return (crossings != Curve.RECT_INTERSECTS &&
            (crossings & mask) != 0);
}

/**
 * {@inheritDoc}
 * <p>
 * 此方法对象在某些情况下可能会保守地返回 false，即指定的矩形区域与路径的某个段相交，但该段并不表示路径的内部和外部之间的边界。
 * 这样的段可能完全位于路径的内部，如果它们是具有 {@link #WIND_NON_ZERO} 绕组规则的路径的一部分，或者这些段以相反的方向重叠，使得两组段相互抵消，而没有外部区域位于它们之间。
 * 要确定段是否代表路径内部的真实边界，需要进行广泛的计算，涉及路径的所有段和绕组规则，这超出了此实现的范围。
 *
 * @since 1.6
 */
public final boolean contains(Rectangle2D r) {
    return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
}

/**
 * 测试指定的 {@link PathIterator} 的内部是否与指定的矩形坐标集的内部相交。
 * <p>
 * 此方法为 {@link Shape} 接口的实现者提供了一个基本设施，以实现对 {@link Shape#intersects(double, double, double, double)} 方法的支持。
 * <p>
 * 此方法对象在某些情况下可能会保守地返回 true，即指定的矩形区域与路径的某个段相交，但该段并不表示路径的内部和外部之间的边界。
 * 这样的情况可能发生在路径的某些段以相反的方向重叠，使得两组段相互抵消，而没有内部区域位于它们之间。
 * 要确定段是否代表路径内部的真实边界，需要进行广泛的计算，涉及路径的所有段和绕组规则，这超出了此实现的范围。
 *
 * @param pi 指定的 {@code PathIterator}
 * @param x 指定的 X 坐标
 * @param y 指定的 Y 坐标
 * @param w 指定的矩形坐标的宽度
 * @param h 指定的矩形坐标的高度
 * @return 如果指定的 {@code PathIterator} 和指定的矩形坐标集的内部相交，则返回 {@code true}；否则返回 {@code false}。
 * @since 1.6
 */
public static boolean intersects(PathIterator pi,
                                 double x, double y, double w, double h)
{
    if (java.lang.Double.isNaN(x+w) || java.lang.Double.isNaN(y+h)) {
        /* [xy]+[wh] 是 NaN，如果这些值中的任何一个为 NaN，
         * 或者将它们相加会产生 NaN，因为它们是相反的无穷大值。
         * 由于我们需要在下面将它们相加，它们的和不能为 NaN。
         * 我们返回 false，因为 NaN 总是产生测试的负响应
         */
        return false;
    }
    if (w <= 0 || h <= 0) {
        return false;
    }
    int mask = (pi.getWindingRule() == WIND_NON_ZERO ? -1 : 2);
    int crossings = Curve.rectCrossingsForPath(pi, x, y, x+w, y+h);
    return (crossings == Curve.RECT_INTERSECTS ||
            (crossings & mask) != 0);
}

/**
 * 测试指定的 {@link PathIterator} 的内部是否与指定的 {@link Rectangle2D} 的内部相交。
 * <p>
 * 此方法为 {@link Shape} 接口的实现者提供了一个基本设施，以实现对 {@link Shape#intersects(Rectangle2D)} 方法的支持。
 * <p>
 * 此方法对象在某些情况下可能会保守地返回 true，即指定的矩形区域与路径的某个段相交，但该段并不表示路径的内部和外部之间的边界。
 * 这样的情况可能发生在路径的某些段以相反的方向重叠，使得两组段相互抵消，而没有内部区域位于它们之间。
 * 要确定段是否代表路径内部的真实边界，需要进行广泛的计算，涉及路径的所有段和绕组规则，这超出了此实现的范围。
 *
 * @param pi 指定的 {@code PathIterator}
 * @param r 指定的 {@code Rectangle2D}
 * @return 如果指定的 {@code PathIterator} 和指定的 {@code Rectangle2D} 的内部相交，则返回 {@code true}；否则返回 {@code false}。
 * @since 1.6
 */
public static boolean intersects(PathIterator pi, Rectangle2D r) {
    return intersects(pi, r.getX(), r.getY(), r.getWidth(), r.getHeight());
}

/**
 * {@inheritDoc}
 * <p>
 * 此方法对象在某些情况下可能会保守地返回 true，即指定的矩形区域与路径的某个段相交，但该段并不表示路径的内部和外部之间的边界。
 * 这样的情况可能发生在路径的某些段以相反的方向重叠，使得两组段相互抵消，而没有内部区域位于它们之间。
 * 要确定段是否代表路径内部的真实边界，需要进行广泛的计算，涉及路径的所有段和绕组规则，这超出了此实现的范围。
 *
 * @since 1.6
 */
public final boolean intersects(double x, double y, double w, double h) {
    if (java.lang.Double.isNaN(x+w) || java.lang.Double.isNaN(y+h)) {
        /* [xy]+[wh] 是 NaN，如果这些值中的任何一个为 NaN，
         * 或者将它们相加会产生 NaN，因为它们是相反的无穷大值。
         * 由于我们需要在下面将它们相加，它们的和不能为 NaN。
         * 我们返回 false，因为 NaN 总是产生测试的负响应
         */
        return false;
    }
    if (w <= 0 || h <= 0) {
        return false;
    }
    int mask = (windingRule == WIND_NON_ZERO ? -1 : 2);
    int crossings = rectCrossings(x, y, x+w, y+h);
    return (crossings == Curve.RECT_INTERSECTS ||
            (crossings & mask) != 0);
}

/**
 * {@inheritDoc}
 * <p>
 * 此方法对象在某些情况下可能会保守地返回 true，即指定的矩形区域与路径的某个段相交，但该段并不表示路径的内部和外部之间的边界。
 * 这样的情况可能发生在路径的某些段以相反的方向重叠，使得两组段相互抵消，而没有内部区域位于它们之间。
 * 要确定段是否代表路径内部的真实边界，需要进行广泛的计算，涉及路径的所有段和绕组规则，这超出了此实现的范围。
 *
 * @since 1.6
 */
public final boolean intersects(Rectangle2D r) {
    return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
}

/**
 * {@inheritDoc}
 * <p>
 * 此类的迭代器不是多线程安全的，这意味着此 {@code Path2D} 类不保证对这个 {@code Path2D} 对象的几何形状的修改不会影响已经进行中的该几何形状的迭代。
 *
 * @since 1.6
 */
public final PathIterator getPathIterator(AffineTransform at,
                                          double flatness)
{
    return new FlatteningPathIterator(getPathIterator(at), flatness);
}

/**
 * 创建一个与此对象相同类的新对象。
 *
 * @return 一个此实例的克隆。
 * @exception  OutOfMemoryError 如果没有足够的内存。
 * @see        java.lang.Cloneable
 * @since      1.6
 */
public abstract Object clone();
    // Note: It would be nice to have this return Path2D
    // but one of our subclasses (GeneralPath) needs to
    // offer "public Object clone()" for backwards
    // compatibility so we cannot restrict it further.
    // REMIND: Can we do both somehow?

/*
 * 支持子类序列化的字段和方法。
 */
private static final byte SERIAL_STORAGE_FLT_ARRAY = 0x30;
private static final byte SERIAL_STORAGE_DBL_ARRAY = 0x31;

private static final byte SERIAL_SEG_FLT_MOVETO    = 0x40;
private static final byte SERIAL_SEG_FLT_LINETO    = 0x41;
private static final byte SERIAL_SEG_FLT_QUADTO    = 0x42;
private static final byte SERIAL_SEG_FLT_CUBICTO   = 0x43;

private static final byte SERIAL_SEG_DBL_MOVETO    = 0x50;
private static final byte SERIAL_SEG_DBL_LINETO    = 0x51;
private static final byte SERIAL_SEG_DBL_QUADTO    = 0x52;
private static final byte SERIAL_SEG_DBL_CUBICTO   = 0x53;

private static final byte SERIAL_SEG_CLOSE         = 0x60;
private static final byte SERIAL_PATH_END          = 0x61;

final void writeObject(java.io.ObjectOutputStream s, boolean isdbl)
    throws java.io.IOException
{
    s.defaultWriteObject();

    float fCoords[];
    double dCoords[];

    if (isdbl) {
        dCoords = ((Path2D.Double) this).doubleCoords;
        fCoords = null;
    } else {
        fCoords = ((Path2D.Float) this).floatCoords;
        dCoords = null;
    }

    int numTypes = this.numTypes;

    s.writeByte(isdbl
                ? SERIAL_STORAGE_DBL_ARRAY
                : SERIAL_STORAGE_FLT_ARRAY);
    s.writeInt(numTypes);
    s.writeInt(numCoords);
    s.writeByte((byte) windingRule);

    int cindex = 0;
    for (int i = 0; i < numTypes; i++) {
        int npoints;
        byte serialtype;
        switch (pointTypes[i]) {
        case SEG_MOVETO:
            npoints = 1;
            serialtype = (isdbl
                          ? SERIAL_SEG_DBL_MOVETO
                          : SERIAL_SEG_FLT_MOVETO);
            break;
        case SEG_LINETO:
            npoints = 1;
            serialtype = (isdbl
                          ? SERIAL_SEG_DBL_LINETO
                          : SERIAL_SEG_FLT_LINETO);
            break;
        case SEG_QUADTO:
            npoints = 2;
            serialtype = (isdbl
                          ? SERIAL_SEG_DBL_QUADTO
                          : SERIAL_SEG_FLT_QUADTO);
            break;
        case SEG_CUBICTO:
            npoints = 3;
            serialtype = (isdbl
                          ? SERIAL_SEG_DBL_CUBICTO
                          : SERIAL_SEG_FLT_CUBICTO);
            break;
        case SEG_CLOSE:
            npoints = 0;
            serialtype = SERIAL_SEG_CLOSE;
            break;

        default:
            // Should never happen
            throw new InternalError("unrecognized path type");
        }
        s.writeByte(serialtype);
        while (--npoints >= 0) {
            if (isdbl) {
                s.writeDouble(dCoords[cindex++]);
                s.writeDouble(dCoords[cindex++]);
            } else {
                s.writeFloat(fCoords[cindex++]);
                s.writeFloat(fCoords[cindex++]);
            }
        }
    }
    s.writeByte(SERIAL_PATH_END);
}


                final void readObject(java.io.ObjectInputStream s, boolean storedbl)
        throws java.lang.ClassNotFoundException, java.io.IOException
    {
        s.defaultReadObject();

        // 子类调用此方法时使用他们希望我们使用的存储类型（storedbl），因此我们忽略流中的存储方法提示。
        s.readByte();
        int nT = s.readInt();
        int nC = s.readInt();
        try {
            setWindingRule(s.readByte());
        } catch (IllegalArgumentException iae) {
            throw new java.io.InvalidObjectException(iae.getMessage());
        }

        // 仅当流中的大小小于 INIT_SIZE 时接受流中的大小，否则大小将基于流中的实际数据。
        pointTypes = new byte[(nT < 0 || nT > INIT_SIZE) ? INIT_SIZE : nT];
        final int initX2 = INIT_SIZE * 2;
        if (nC < 0 || nC > initX2) {
            nC = initX2;
        }
        if (storedbl) {
            ((Path2D.Double) this).doubleCoords = new double[nC];
        } else {
            ((Path2D.Float) this).floatCoords = new float[nC];
        }

    PATHDONE:
        for (int i = 0; nT < 0 || i < nT; i++) {
            boolean isdbl;
            int npoints;
            byte segtype;

            byte serialtype = s.readByte();
            switch (serialtype) {
            case SERIAL_SEG_FLT_MOVETO:
                isdbl = false;
                npoints = 1;
                segtype = SEG_MOVETO;
                break;
            case SERIAL_SEG_FLT_LINETO:
                isdbl = false;
                npoints = 1;
                segtype = SEG_LINETO;
                break;
            case SERIAL_SEG_FLT_QUADTO:
                isdbl = false;
                npoints = 2;
                segtype = SEG_QUADTO;
                break;
            case SERIAL_SEG_FLT_CUBICTO:
                isdbl = false;
                npoints = 3;
                segtype = SEG_CUBICTO;
                break;

            case SERIAL_SEG_DBL_MOVETO:
                isdbl = true;
                npoints = 1;
                segtype = SEG_MOVETO;
                break;
            case SERIAL_SEG_DBL_LINETO:
                isdbl = true;
                npoints = 1;
                segtype = SEG_LINETO;
                break;
            case SERIAL_SEG_DBL_QUADTO:
                isdbl = true;
                npoints = 2;
                segtype = SEG_QUADTO;
                break;
            case SERIAL_SEG_DBL_CUBICTO:
                isdbl = true;
                npoints = 3;
                segtype = SEG_CUBICTO;
                break;

            case SERIAL_SEG_CLOSE:
                isdbl = false;
                npoints = 0;
                segtype = SEG_CLOSE;
                break;

            case SERIAL_PATH_END:
                if (nT < 0) {
                    break PATHDONE;
                }
                throw new StreamCorruptedException("unexpected PATH_END");

            default:
                throw new StreamCorruptedException("unrecognized path type");
            }
            needRoom(segtype != SEG_MOVETO, npoints * 2);
            if (isdbl) {
                while (--npoints >= 0) {
                    append(s.readDouble(), s.readDouble());
                }
            } else {
                while (--npoints >= 0) {
                    append(s.readFloat(), s.readFloat());
                }
            }
            pointTypes[numTypes++] = segtype;
        }
        if (nT >= 0 && s.readByte() != SERIAL_PATH_END) {
            throw new StreamCorruptedException("missing PATH_END");
        }
    }

    static abstract class Iterator implements PathIterator {
        int typeIdx;
        int pointIdx;
        Path2D path;

        static final int curvecoords[] = {2, 2, 4, 6, 0};

        Iterator(Path2D path) {
            this.path = path;
        }

        public int getWindingRule() {
            return path.getWindingRule();
        }

        public boolean isDone() {
            return (typeIdx >= path.numTypes);
        }

        public void next() {
            int type = path.pointTypes[typeIdx++];
            pointIdx += curvecoords[type];
        }
    }
}
