
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * <code>Shape</code> 接口提供了定义表示某种几何形状的对象的定义。<code>Shape</code> 由一个 {@link PathIterator} 对象描述，该对象可以表达 <code>Shape</code> 的轮廓以及确定轮廓如何将 2D 平面划分为内部和外部点的规则。每个 <code>Shape</code> 对象提供回调以获取几何图形的边界框，确定点或矩形是否部分或完全位于 <code>Shape</code> 的内部，并检索描述 <code>Shape</code> 轮廓轨迹路径的 <code>PathIterator</code> 对象。
 * <p>
 * <a name="def_insideness"><b>内部性的定义：</b></a>
 * 如果且仅如果满足以下条件之一，则认为一个点位于 <code>Shape</code> 内：
 * <ul>
 * <li> 完全位于 <code>Shape</code> 边界内 <i>或</i>
 * <li> 恰好位于 <code>Shape</code> 边界上 <i>且</i> 点在 <code>X</code> 方向上的相邻空间完全位于边界内 <i>或</i>
 * <li> 恰好位于水平边界段上 <b>且</b> 点在 <code>Y</code> 方向上的相邻空间位于边界内。
 * </ul>
 * <p><code>contains</code> 和 <code>intersects</code> 方法认为 <code>Shape</code> 的内部是其封闭的区域，即使形状未封闭，这些方法也会隐式地认为形状是封闭的，以确定形状是否包含或与矩形相交或形状是否包含点。
 *
 * @see java.awt.geom.PathIterator
 * @see java.awt.geom.AffineTransform
 * @see java.awt.geom.FlatteningPathIterator
 * @see java.awt.geom.GeneralPath
 *
 * @author Jim Graham
 * @since 1.2
 */
public interface Shape {
    /**
     * 返回一个完全包围 <code>Shape</code> 的整数 {@link Rectangle}。请注意，没有保证返回的 <code>Rectangle</code> 是包围 <code>Shape</code> 的最小边界框，只是 <code>Shape</code> 完全位于指示的 <code>Rectangle</code> 内。如果 <code>Shape</code> 超出整数数据类型的有限范围，返回的 <code>Rectangle</code> 可能无法完全包围 <code>Shape</code>。<code>getBounds2D</code> 方法通常返回更紧的边界框，因为它的表示更灵活。
     *
     * <p>
     * 请注意，<a href="{@docRoot}/java/awt/Shape.html#def_insideness">
     * 内部性的定义</a> 可能会导致以下情况：位于 {@code shape} 定义轮廓上的点可能不被视为包含在返回的 {@code bounds} 对象中，但仅在这些点也不被视为包含在原始 {@code shape} 中的情况下。
     * </p>
     * <p>
     * 如果一个 {@code point} 根据 {@link #contains(double x, double y) contains(point)} 方法位于 {@code shape} 内，则它必须根据 {@link #contains(double x, double y) contains(point)} 方法位于返回的 {@code Rectangle} bounds 对象内。具体来说：
     * </p>
     * <p>
     *  {@code shape.contains(x,y)} 要求 {@code bounds.contains(x,y)}
     * </p>
     * <p>
     * 如果一个 {@code point} 不在 {@code shape} 内，它可能仍然包含在 {@code bounds} 对象中：
     * </p>
     * <p>
     *  {@code bounds.contains(x,y)} 不意味着 {@code shape.contains(x,y)}
     * </p>
     * @return 一个完全包围 <code>Shape</code> 的整数 <code>Rectangle</code>。
     * @see #getBounds2D
     * @since 1.2
     */
    public Rectangle getBounds();

    /**
     * 返回比 <code>getBounds</code> 方法更精确和更准确的 <code>Shape</code> 的边界框。请注意，没有保证返回的 {@link Rectangle2D} 是包围 <code>Shape</code> 的最小边界框，只是 <code>Shape</code> 完全位于指示的 <code>Rectangle2D</code> 内。此方法返回的边界框通常比 <code>getBounds</code> 方法返回的更紧，且永远不会因溢出问题而失败，因为返回值可以是使用双精度值存储尺寸的 <code>Rectangle2D</code> 实例。
     *
     * <p>
     * 请注意，<a href="{@docRoot}/java/awt/Shape.html#def_insideness">
     * 内部性的定义</a> 可能会导致以下情况：位于 {@code shape} 定义轮廓上的点可能不被视为包含在返回的 {@code bounds} 对象中，但仅在这些点也不被视为包含在原始 {@code shape} 中的情况下。
     * </p>
     * <p>
     * 如果一个 {@code point} 根据 {@link #contains(Point2D p) contains(point)} 方法位于 {@code shape} 内，则它必须根据 {@link #contains(Point2D p) contains(point)} 方法位于返回的 {@code Rectangle2D} bounds 对象内。具体来说：
     * </p>
     * <p>
     *  {@code shape.contains(p)} 要求 {@code bounds.contains(p)}
     * </p>
     * <p>
     * 如果一个 {@code point} 不在 {@code shape} 内，它可能仍然包含在 {@code bounds} 对象中：
     * </p>
     * <p>
     *  {@code bounds.contains(p)} 不意味着 {@code shape.contains(p)}
     * </p>
     * @return 一个 <code>Rectangle2D</code> 实例，它是 <code>Shape</code> 的高精度边界框。
     * @see #getBounds
     * @since 1.2
     */
    public Rectangle2D getBounds2D();

    /**
     * 测试指定的坐标是否位于 <code>Shape</code> 的边界内，如 <a href="{@docRoot}/java/awt/Shape.html#def_insideness">
     * 内部性的定义</a> 所述。
     * @param x 要测试的指定 X 坐标
     * @param y 要测试的指定 Y 坐标
     * @return 如果指定的坐标位于 <code>Shape</code> 边界内，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean contains(double x, double y);

    /**
     * 测试指定的 {@link Point2D} 是否位于 <code>Shape</code> 的边界内，如 <a href="{@docRoot}/java/awt/Shape.html#def_insideness">
     * 内部性的定义</a> 所述。
     * @param p 要测试的指定 <code>Point2D</code>
     * @return 如果指定的 <code>Point2D</code> 位于 <code>Shape</code> 边界内，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean contains(Point2D p);

    /**
     * 测试 <code>Shape</code> 的内部是否与指定的矩形区域的内部相交。
     * 如果任何点同时包含在 <code>Shape</code> 的内部和指定的矩形区域内，则认为矩形区域与 <code>Shape</code> 相交。
     * <p>
     * 如果以下情况之一成立，<code>Shape.intersects()</code> 方法允许 <code>Shape</code> 实现保守地返回 <code>true</code>：
     * <ul>
     * <li>
     * 矩形区域和 <code>Shape</code> 很可能相交，但
     * <li>
     * 准确确定此相交的计算过于昂贵。
     * </ul>
     * 这意味着对于某些 <code>Shapes</code>，此方法可能会返回 <code>true</code>，即使矩形区域不与 <code>Shape</code> 相交。<code>Area</code> 类执行比大多数 <code>Shape</code> 对象更准确的几何相交计算，因此如果需要更精确的答案，可以使用它。
     *
     * @param x 指定矩形区域左上角的 X 坐标
     * @param y 指定矩形区域左上角的 Y 坐标
     * @param w 指定矩形区域的宽度
     * @param h 指定矩形区域的高度
     * @return 如果 <code>Shape</code> 的内部和指定矩形区域的内部相交，或者两者都极有可能相交且相交计算过于昂贵，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @see java.awt.geom.Area
     * @since 1.2
     */
    public boolean intersects(double x, double y, double w, double h);

    /**
     * 测试 <code>Shape</code> 的内部是否与指定的 <code>Rectangle2D</code> 的内部相交。
     * 如果以下情况之一成立，<code>Shape.intersects()</code> 方法允许 <code>Shape</code> 实现保守地返回 <code>true</code>：
     * <ul>
     * <li>
     * <code>Rectangle2D</code> 和 <code>Shape</code> 很可能相交，但
     * <li>
     * 准确确定此相交的计算过于昂贵。
     * </ul>
     * 这意味着对于某些 <code>Shapes</code>，此方法可能会返回 <code>true</code>，即使 <code>Rectangle2D</code> 不与 <code>Shape</code> 相交。<code>Area</code> 类执行比大多数 <code>Shape</code> 对象更准确的几何相交计算，因此如果需要更精确的答案，可以使用它。
     *
     * @param r 指定的 <code>Rectangle2D</code>
     * @return 如果 <code>Shape</code> 的内部和指定的 <code>Rectangle2D</code> 的内部相交，或者两者都极有可能相交且相交计算过于昂贵，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @see #intersects(double, double, double, double)
     * @since 1.2
     */
    public boolean intersects(Rectangle2D r);

    /**
     * 测试 <code>Shape</code> 的内部是否完全包含指定的矩形区域。所有位于矩形区域内的坐标必须位于 <code>Shape</code> 内，才能认为整个矩形区域包含在 <code>Shape</code> 内。
     * <p>
     * 如果以下情况之一成立，<code>Shape.contains()</code> 方法允许 <code>Shape</code> 实现保守地返回 <code>false</code>：
     * <ul>
     * <li>
     * <code>intersect</code> 方法返回 <code>true</code> 且
     * <li>
     * 确定 <code>Shape</code> 是否完全包含矩形区域的计算过于昂贵。
     * </ul>
     * 这意味着对于某些 <code>Shapes</code>，此方法可能会返回 <code>false</code>，即使 <code>Shape</code> 包含矩形区域。<code>Area</code> 类执行比大多数 <code>Shape</code> 对象更准确的几何计算，因此如果需要更精确的答案，可以使用它。
     *
     * @param x 指定矩形区域左上角的 X 坐标
     * @param y 指定矩形区域左上角的 Y 坐标
     * @param w 指定矩形区域的宽度
     * @param h 指定矩形区域的高度
     * @return 如果 <code>Shape</code> 的内部完全包含指定的矩形区域，则返回 <code>true</code>；否则返回 <code>false</code>，或者如果 <code>Shape</code> 包含矩形区域且 <code>intersects</code> 方法返回 <code>true</code> 且包含计算过于昂贵，则返回 <code>false</code>。
     * @see java.awt.geom.Area
     * @see #intersects
     * @since 1.2
     */
    public boolean contains(double x, double y, double w, double h);

    /**
     * 测试 <code>Shape</code> 的内部是否完全包含指定的 <code>Rectangle2D</code>。
     * 如果以下情况之一成立，<code>Shape.contains()</code> 方法允许 <code>Shape</code> 实现保守地返回 <code>false</code>：
     * <ul>
     * <li>
     * <code>intersect</code> 方法返回 <code>true</code> 且
     * <li>
     * 确定 <code>Shape</code> 是否完全包含 <code>Rectangle2D</code> 的计算过于昂贵。
     * </ul>
     * 这意味着对于某些 <code>Shapes</code>，此方法可能会返回 <code>false</code>，即使 <code>Shape</code> 包含 <code>Rectangle2D</code>。<code>Area</code> 类执行比大多数 <code>Shape</code> 对象更准确的几何计算，因此如果需要更精确的答案，可以使用它。
     *
     * @param r 指定的 <code>Rectangle2D</code>
     * @return 如果 <code>Shape</code> 的内部完全包含 <code>Rectangle2D</code>，则返回 <code>true</code>；否则返回 <code>false</code>，或者如果 <code>Shape</code> 包含 <code>Rectangle2D</code> 且 <code>intersects</code> 方法返回 <code>true</code> 且包含计算过于昂贵，则返回 <code>false</code>。
     * @see #contains(double, double, double, double)
     * @since 1.2
     */
    public boolean contains(Rectangle2D r);


                /**
     * 返回一个迭代器对象，该对象沿着 <code>Shape</code> 边界迭代并提供对 <code>Shape</code> 轮廓几何的访问。如果指定了可选的 {@link AffineTransform}，则迭代返回的坐标将相应地转换。
     * <p>
     * 每次调用此方法都会返回一个新的 <code>PathIterator</code> 对象，该对象独立于同时使用的任何其他 <code>PathIterator</code> 对象遍历 <code>Shape</code> 对象的几何。
     * <p>
     * 建议但不保证，实现 <code>Shape</code> 接口的对象将正在进行的迭代与原始对象几何在迭代期间可能发生的变化隔离。
     *
     * @param at 可选的 <code>AffineTransform</code>，用于在迭代返回坐标时应用，或者如果需要未转换的坐标，则为 <code>null</code>
     * @return 一个新的 <code>PathIterator</code> 对象，独立遍历 <code>Shape</code> 的几何。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at);

    /**
     * 返回一个迭代器对象，该对象沿着 <code>Shape</code> 边界迭代并提供对 <code>Shape</code> 轮廓几何的扁平化视图的访问。
     * <p>
     * 迭代器仅返回 SEG_MOVETO、SEG_LINETO 和 SEG_CLOSE 点类型。
     * <p>
     * 如果指定了可选的 <code>AffineTransform</code>，则迭代返回的坐标将相应地转换。
     * <p>
     * 曲线段的细分量由 <code>flatness</code> 参数控制，该参数指定未扁平化转换曲线上的任何点与返回的扁平化路径段的最大偏差距离。注意，可能会默默地施加对扁平化路径精度的限制，导致非常小的扁平化参数被视为更大的值。此限制（如果有）由所使用的特定实现定义。
     * <p>
     * 每次调用此方法都会返回一个新的 <code>PathIterator</code> 对象，该对象独立于同时使用的任何其他 <code>PathIterator</code> 对象遍历 <code>Shape</code> 对象的几何。
     * <p>
     * 建议但不保证，实现 <code>Shape</code> 接口的对象将正在进行的迭代与原始对象几何在迭代期间可能发生的变化隔离。
     *
     * @param at 可选的 <code>AffineTransform</code>，用于在迭代返回坐标时应用，或者如果需要未转换的坐标，则为 <code>null</code>
     * @param flatness 允许用于近似曲线段的线段与原始曲线上的任何点的最大偏差距离
     * @return 一个新的 <code>PathIterator</code>，独立遍历 <code>Shape</code> 的扁平化几何视图。
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at, double flatness);
}
