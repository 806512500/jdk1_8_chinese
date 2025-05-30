
/*
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.beans.ConstructorProperties;

/**
 * {@code RadialGradientPaint} 类提供了一种使用圆形径向颜色渐变模式填充形状的方法。用户可以指定 2 个或更多渐变颜色，此画笔将在每种颜色之间进行插值。
 * <p>
 * 用户必须指定控制渐变模式的圆，该圆由中心点和半径描述。用户还可以指定该圆内的一个单独的焦点，该焦点控制渐变的第一个颜色的位置。默认情况下，焦点设置为圆的中心。
 * <p>
 * 此画笔将渐变的第一个颜色映射到焦点，最后一个颜色映射到圆的周长，用户指定的任何中间颜色都会平滑插值。因此，从焦点到圆周的任何一条线都会跨越所有渐变颜色。
 * <p>
 * 指定圆外的焦点会导致渐变模式的环中心位于焦点方向的圆边缘内的点上。渲染将内部使用这个修改后的位置，就像它是指定的焦点一样。
 * <p>
 * 用户必须提供一个浮点数组，指定如何沿渐变分布颜色。这些值应从 0.0 到 1.0，作用类似于渐变上的关键帧（它们标记渐变应精确为特定颜色的位置）。
 * <p>
 * 如果用户没有将第一个关键帧值设置为 0 和/或最后一个关键帧值设置为 1，将在这些位置创建关键帧，并复制第一个和最后一个颜色。因此，如果用户指定以下数组来构造渐变：<br>
 * <pre>
 *     {Color.BLUE, Color.RED}, {.3f, .7f}
 * </pre>
 * 这将转换为具有以下关键帧的渐变：<br>
 * <pre>
 *     {Color.BLUE, Color.BLUE, Color.RED, Color.RED}, {0f, .3f, .7f, 1f}
 * </pre>
 *
 * <p>
 * 用户还可以选择 {@code RadialGradientPaint} 对象在填充圆半径外的空间时采取的行动，通过将 {@code CycleMethod} 设置为 {@code REFLECTION} 或 {@code REPEAT}。
 * 渐变颜色比例对于从焦点绘制的任何一条线都是相等的。下图显示了距离 AB 等于距离 BC，距离 AD 等于距离 DE。
 * <center>
 * <img src = "doc-files/RadialGradientPaint-3.png" alt="图像显示距离 AB=BC，AD=DE">
 * </center>
 * 如果渐变和图形渲染变换均匀缩放，并且用户将焦点设置为与圆的中心重合，则渐变颜色比例对于从中心绘制的任何一条线都是相等的。下图显示了距离 AB、BC、AD 和 DE。它们都相等。
 * <center>
 * <img src = "doc-files/RadialGradientPaint-4.png" alt="图像显示距离 AB、BC、AD 和 DE 都相等">
 * </center>
 * 请注意，由于像素粒度的采样，可能会出现一些微小的距离差异。
 * 如果未指定循环方法，默认选择 {@code NO_CYCLE}，这意味着将使用最后一个关键帧颜色填充剩余区域。
 * <p>
 * colorSpace 参数允许用户指定插值应在哪个颜色空间中进行，默认为 sRGB 或线性化 RGB。
 *
 * <p>
 * 以下代码演示了 {@code RadialGradientPaint} 的典型用法，其中中心点和焦点相同：
 * <pre>
 *     Point2D center = new Point2D.Float(50, 50);
 *     float radius = 25;
 *     float[] dist = {0.0f, 0.2f, 1.0f};
 *     Color[] colors = {Color.RED, Color.WHITE, Color.BLUE};
 *     RadialGradientPaint p =
 *         new RadialGradientPaint(center, radius, dist, colors);
 * </pre>
 *
 * <p>
 * 下图展示了上述示例代码的输出，每个循环方法的焦点都是默认（居中）的：
 * <center>
 * <img src = "doc-files/RadialGradientPaint-1.png" alt="图像显示示例代码的输出">
 * </center>
 *
 * <p>
 * 也可以指定非居中的焦点，如下代码所示：
 * <pre>
 *     Point2D center = new Point2D.Float(50, 50);
 *     float radius = 25;
 *     Point2D focus = new Point2D.Float(40, 40);
 *     float[] dist = {0.0f, 0.2f, 1.0f};
 *     Color[] colors = {Color.RED, Color.WHITE, Color.BLUE};
 *     RadialGradientPaint p =
 *         new RadialGradientPaint(center, radius, focus,
 *                                 dist, colors,
 *                                 CycleMethod.NO_CYCLE);
 * </pre>
 *
 * <p>
 * 下图展示了上述示例代码的输出，每个循环方法的焦点都是非居中的：
 * <center>
 * <img src = "doc-files/RadialGradientPaint-2.png" alt="图像显示示例代码的输出">
 * </center>
 *
 * @see java.awt.Paint
 * @see java.awt.Graphics2D#setPaint
 * @author Nicholas Talian, Vincent Hardy, Jim Graham, Jerry Evans
 * @since 1.6
 */
public final class RadialGradientPaint extends MultipleGradientPaint {

    /** 定义 0% 渐变停止点 X 坐标的焦点。 */
    private final Point2D focus;

    /** 定义 100% 渐变停止点 X 坐标的圆的中心。 */
    private final Point2D center;

    /** 定义 100% 渐变停止点的最外圆的半径。 */
    private final float radius;

    /**
     * 构造一个默认重复方法为 {@code NO_CYCLE} 和颜色空间为 {@code SRGB} 的 {@code RadialGradientPaint}，使用中心点作为焦点。
     *
     * @param cx 圆的中心点的用户空间 X 坐标。渐变的最后一个颜色映射到该圆的周长。
     * @param cy 圆的中心点的用户空间 Y 坐标。渐变的最后一个颜色映射到该圆的周长。
     * @param radius 定义颜色渐变范围的圆的半径。
     * @param fractions 从 0.0 到 1.0 的数字，指定沿渐变分布颜色的方式。
     * @param colors 用于渐变的颜色数组。第一个颜色用于焦点，最后一个颜色用于圆的周长。
     *
     * @throws NullPointerException
     * 如果 {@code fractions} 数组为 null，
     * 或 {@code colors} 数组为 null
     * @throws IllegalArgumentException
     * 如果 {@code radius} 非正，
     * 或 {@code fractions.length != colors.length}，
     * 或 {@code colors} 小于 2 个元素，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 未按严格递增顺序提供
     */
    public RadialGradientPaint(float cx, float cy, float radius,
                               float[] fractions, Color[] colors)
    {
        this(cx, cy,
             radius,
             cx, cy,
             fractions,
             colors,
             CycleMethod.NO_CYCLE);
    }

    /**
     * 构造一个默认重复方法为 {@code NO_CYCLE} 和颜色空间为 {@code SRGB} 的 {@code RadialGradientPaint}，使用中心点作为焦点。
     *
     * @param center 定义渐变的圆的中心点，位于用户空间。
     * @param radius 定义颜色渐变范围的圆的半径。
     * @param fractions 从 0.0 到 1.0 的数字，指定沿渐变分布颜色的方式。
     * @param colors 用于渐变的颜色数组。第一个颜色用于焦点，最后一个颜色用于圆的周长。
     *
     * @throws NullPointerException
     * 如果 {@code center} 点为 null，
     * 或 {@code fractions} 数组为 null，
     * 或 {@code colors} 数组为 null
     * @throws IllegalArgumentException
     * 如果 {@code radius} 非正，
     * 或 {@code fractions.length != colors.length}，
     * 或 {@code colors} 小于 2 个元素，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 未按严格递增顺序提供
     */
    public RadialGradientPaint(Point2D center, float radius,
                               float[] fractions, Color[] colors)
    {
        this(center,
             radius,
             center,
             fractions,
             colors,
             CycleMethod.NO_CYCLE);
    }

    /**
     * 构造一个默认颜色空间为 {@code SRGB} 的 {@code RadialGradientPaint}，使用中心点作为焦点。
     *
     * @param cx 圆的中心点的用户空间 X 坐标。渐变的最后一个颜色映射到该圆的周长。
     * @param cy 圆的中心点的用户空间 Y 坐标。渐变的最后一个颜色映射到该圆的周长。
     * @param radius 定义颜色渐变范围的圆的半径。
     * @param fractions 从 0.0 到 1.0 的数字，指定沿渐变分布颜色的方式。
     * @param colors 用于渐变的颜色数组。第一个颜色用于焦点，最后一个颜色用于圆的周长。
     * @param cycleMethod 为 {@code NO_CYCLE}、{@code REFLECT} 或 {@code REPEAT}。
     *
     * @throws NullPointerException
     * 如果 {@code fractions} 数组为 null，
     * 或 {@code colors} 数组为 null，
     * 或 {@code cycleMethod} 为 null
     * @throws IllegalArgumentException
     * 如果 {@code radius} 非正，
     * 或 {@code fractions.length != colors.length}，
     * 或 {@code colors} 小于 2 个元素，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 未按严格递增顺序提供
     */
    public RadialGradientPaint(float cx, float cy, float radius,
                               float[] fractions, Color[] colors,
                               CycleMethod cycleMethod)
    {
        this(cx, cy,
             radius,
             cx, cy,
             fractions,
             colors,
             cycleMethod);
    }

    /**
     * 构造一个默认颜色空间为 {@code SRGB} 的 {@code RadialGradientPaint}，使用中心点作为焦点。
     *
     * @param center 定义渐变的圆的中心点，位于用户空间。
     * @param radius 定义颜色渐变范围的圆的半径。
     * @param fractions 从 0.0 到 1.0 的数字，指定沿渐变分布颜色的方式。
     * @param colors 用于渐变的颜色数组。第一个颜色用于焦点，最后一个颜色用于圆的周长。
     * @param cycleMethod 为 {@code NO_CYCLE}、{@code REFLECT} 或 {@code REPEAT}。
     *
     * @throws NullPointerException
     * 如果 {@code center} 点为 null，
     * 或 {@code fractions} 数组为 null，
     * 或 {@code colors} 数组为 null，
     * 或 {@code cycleMethod} 为 null
     * @throws IllegalArgumentException
     * 如果 {@code radius} 非正，
     * 或 {@code fractions.length != colors.length}，
     * 或 {@code colors} 小于 2 个元素，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 未按严格递增顺序提供
     */
    public RadialGradientPaint(Point2D center, float radius,
                               float[] fractions, Color[] colors,
                               CycleMethod cycleMethod)
    {
        this(center,
             radius,
             center,
             fractions,
             colors,
             cycleMethod);
    }

    /**
     * 构造一个默认颜色空间为 {@code SRGB} 的 {@code RadialGradientPaint}。
     *
     * @param cx 圆的中心点的用户空间 X 坐标。渐变的最后一个颜色映射到该圆的周长。
     * @param cy 圆的中心点的用户空间 Y 坐标。渐变的最后一个颜色映射到该圆的周长。
     * @param radius 定义颜色渐变范围的圆的半径。
     * @param fx 用户空间中第一个颜色映射的点的 X 坐标。
     * @param fy 用户空间中第一个颜色映射的点的 Y 坐标。
     * @param fractions 从 0.0 到 1.0 的数字，指定沿渐变分布颜色的方式。
     * @param colors 用于渐变的颜色数组。第一个颜色用于焦点，最后一个颜色用于圆的周长。
     * @param cycleMethod 为 {@code NO_CYCLE}、{@code REFLECT} 或 {@code REPEAT}。
     *
     * @throws NullPointerException
     * 如果 {@code fractions} 数组为 null，
     * 或 {@code colors} 数组为 null，
     * 或 {@code cycleMethod} 为 null
     * @throws IllegalArgumentException
     * 如果 {@code radius} 非正，
     * 或 {@code fractions.length != colors.length}，
     * 或 {@code colors} 小于 2 个元素，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 未按严格递增顺序提供
     */
    public RadialGradientPaint(float cx, float cy, float radius,
                               float fx, float fy,
                               float[] fractions, Color[] colors,
                               CycleMethod cycleMethod)
    {
        this(new Point2D.Float(cx, cy),
             radius,
             new Point2D.Float(fx, fy),
             fractions,
             colors,
             cycleMethod);
    }


                /**
     * 构造一个具有默认 {@code SRGB} 颜色空间的 {@code RadialGradientPaint}。
     *
     * @param center 圆的中心点，定义渐变的圆在用户空间中的位置。渐变的最后一种颜色映射到该圆的周长。
     * @param radius 定义颜色渐变范围的圆的半径
     * @param focus 用户空间中的点，映射到第一种颜色
     * @param fractions 从 0.0 到 1.0 的数字，指定沿渐变分布的颜色
     * @param colors 用于渐变的颜色数组。第一种颜色用于焦点，最后一种颜色用于圆的周长。
     * @param cycleMethod 可以是 {@code NO_CYCLE}，{@code REFLECT}，或 {@code REPEAT}
     *
     * @throws NullPointerException
     * 如果其中一个点为 null，
     * 或 {@code fractions} 数组为 null，
     * 或 {@code colors} 数组为 null，
     * 或 {@code cycleMethod} 为 null
     * @throws IllegalArgumentException
     * 如果 {@code radius} 非正，
     * 或 {@code fractions.length != colors.length}，
     * 或 {@code colors} 小于 2 个元素，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 未按严格递增顺序提供
     */
    public RadialGradientPaint(Point2D center, float radius,
                               Point2D focus,
                               float[] fractions, Color[] colors,
                               CycleMethod cycleMethod)
    {
        this(center,
             radius,
             focus,
             fractions,
             colors,
             cycleMethod,
             ColorSpaceType.SRGB,
             new AffineTransform());
    }

    /**
     * 构造一个 {@code RadialGradientPaint}。
     *
     * @param center 圆的中心点，定义渐变的圆在用户空间中的位置。渐变的最后一种颜色映射到该圆的周长。
     * @param radius 定义颜色渐变范围的圆的半径
     * @param focus 用户空间中的点，映射到第一种颜色
     * @param fractions 从 0.0 到 1.0 的数字，指定沿渐变分布的颜色
     * @param colors 用于渐变的颜色数组。第一种颜色用于焦点，最后一种颜色用于圆的周长。
     * @param cycleMethod 可以是 {@code NO_CYCLE}，{@code REFLECT}，或 {@code REPEAT}
     * @param colorSpace 用于插值的颜色空间，可以是 {@code SRGB} 或 {@code LINEAR_RGB}
     * @param gradientTransform 应用于渐变的变换
     *
     * @throws NullPointerException
     * 如果其中一个点为 null，
     * 或 {@code fractions} 数组为 null，
     * 或 {@code colors} 数组为 null，
     * 或 {@code cycleMethod} 为 null，
     * 或 {@code colorSpace} 为 null，
     * 或 {@code gradientTransform} 为 null
     * @throws IllegalArgumentException
     * 如果 {@code radius} 非正，
     * 或 {@code fractions.length != colors.length}，
     * 或 {@code colors} 小于 2 个元素，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 未按严格递增顺序提供
     */
    @ConstructorProperties({ "centerPoint", "radius", "focusPoint", "fractions", "colors", "cycleMethod", "colorSpace", "transform" })
    public RadialGradientPaint(Point2D center,
                               float radius,
                               Point2D focus,
                               float[] fractions, Color[] colors,
                               CycleMethod cycleMethod,
                               ColorSpaceType colorSpace,
                               AffineTransform gradientTransform)
    {
        super(fractions, colors, cycleMethod, colorSpace, gradientTransform);

        // 检查输入参数
        if (center == null) {
            throw new NullPointerException("中心点必须非空");
        }

        if (focus == null) {
            throw new NullPointerException("焦点必须非空");
        }

        if (radius <= 0) {
            throw new IllegalArgumentException("半径必须大于 " +
                                               "零");
        }

        // 复制参数
        this.center = new Point2D.Double(center.getX(), center.getY());
        this.focus = new Point2D.Double(focus.getX(), focus.getY());
        this.radius = radius;
    }

    /**
     * 构造一个具有默认 {@code SRGB} 颜色空间的 {@code RadialGradientPaint}。
     * {@code RadialGradientPaint} 的渐变圆由给定的边界框定义。
     * <p>
     * 此构造函数是一种更方便的方式来表达以下（等效）代码：<br>
     *
     * <pre>
     *     double gw = gradientBounds.getWidth();
     *     double gh = gradientBounds.getHeight();
     *     double cx = gradientBounds.getCenterX();
     *     double cy = gradientBounds.getCenterY();
     *     Point2D center = new Point2D.Double(cx, cy);
     *
     *     AffineTransform gradientTransform = new AffineTransform();
     *     gradientTransform.translate(cx, cy);
     *     gradientTransform.scale(gw / 2, gh / 2);
     *     gradientTransform.translate(-cx, -cy);
     *
     *     RadialGradientPaint gp =
     *         new RadialGradientPaint(center, 1.0f, center,
     *                                 fractions, colors,
     *                                 cycleMethod,
     *                                 ColorSpaceType.SRGB,
     *                                 gradientTransform);
     * </pre>
     *
     * @param gradientBounds 定义渐变最外层范围的圆在用户空间中的边界框
     * @param fractions 从 0.0 到 1.0 的数字，指定沿渐变分布的颜色
     * @param colors 用于渐变的颜色数组。第一种颜色用于焦点，最后一种颜色用于圆的周长。
     * @param cycleMethod 可以是 {@code NO_CYCLE}，{@code REFLECT}，或 {@code REPEAT}
     *
     * @throws NullPointerException
     * 如果 {@code gradientBounds} 为 null，
     * 或 {@code fractions} 数组为 null，
     * 或 {@code colors} 数组为 null，
     * 或 {@code cycleMethod} 为 null
     * @throws IllegalArgumentException
     * 如果 {@code gradientBounds} 为空，
     * 或 {@code fractions.length != colors.length}，
     * 或 {@code colors} 小于 2 个元素，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 未按严格递增顺序提供
     */
    public RadialGradientPaint(Rectangle2D gradientBounds,
                               float[] fractions, Color[] colors,
                               CycleMethod cycleMethod)
    {
        // 渐变中心/焦点是边界框的中心，
        // 半径设置为 1.0，然后我们设置一个缩放变换
        // 以实现由边界框定义的椭圆形渐变
        this(new Point2D.Double(gradientBounds.getCenterX(),
                                gradientBounds.getCenterY()),
             1.0f,
             new Point2D.Double(gradientBounds.getCenterX(),
                                gradientBounds.getCenterY()),
             fractions,
             colors,
             cycleMethod,
             ColorSpaceType.SRGB,
             createGradientTransform(gradientBounds));

        if (gradientBounds.isEmpty()) {
            throw new IllegalArgumentException("渐变边界必须 " +
                                               "非空");
        }
    }

    private static AffineTransform createGradientTransform(Rectangle2D r) {
        double cx = r.getCenterX();
        double cy = r.getCenterY();
        AffineTransform xform = AffineTransform.getTranslateInstance(cx, cy);
        xform.scale(r.getWidth()/2, r.getHeight()/2);
        xform.translate(-cx, -cy);
        return xform;
    }

    /**
     * 创建并返回一个用于生成圆形径向颜色渐变图案的 {@link PaintContext}。
     * 有关 null 参数处理的信息，请参阅 {@link Paint#createContext createContext} 方法的描述。
     *
     * @param cm 代表调用者接收像素数据最方便格式的首选 {@link ColorModel}，或 {@code null} 表示没有偏好。
     * @param deviceBounds 被渲染的图形基元的设备空间边界框。
     * @param userBounds 被渲染的图形基元的用户空间边界框。
     * @param transform 从用户空间到设备空间的 {@link AffineTransform}。
     * @param hints 上下文对象可以用来选择渲染替代方案的一组提示。
     * @return 用于生成颜色图案的 {@code PaintContext}。
     * @see Paint
     * @see PaintContext
     * @see ColorModel
     * @see Rectangle
     * @see Rectangle2D
     * @see AffineTransform
     * @see RenderingHints
     */
    public PaintContext createContext(ColorModel cm,
                                      Rectangle deviceBounds,
                                      Rectangle2D userBounds,
                                      AffineTransform transform,
                                      RenderingHints hints)
    {
        // 避免修改用户的变换...
        transform = new AffineTransform(transform);
        // 结合渐变变换
        transform.concatenate(gradientTransform);

        return new RadialGradientPaintContext(this, cm,
                                              deviceBounds, userBounds,
                                              transform, hints,
                                              (float)center.getX(),
                                              (float)center.getY(),
                                              radius,
                                              (float)focus.getX(),
                                              (float)focus.getY(),
                                              fractions, colors,
                                              cycleMethod, colorSpace);
    }

    /**
     * 返回径向渐变的中心点的副本。
     *
     * @return 一个 {@code Point2D} 对象，是中心点的副本
     */
    public Point2D getCenterPoint() {
        return new Point2D.Double(center.getX(), center.getY());
    }

    /**
     * 返回径向渐变的焦点的副本。
     * 注意，如果在构造径向渐变时指定的焦点位于圆的半径之外，此方法仍将返回原始焦点，即使渲染可能会将颜色环中心化在位于半径内的不同点上。
     *
     * @return 一个 {@code Point2D} 对象，是焦点的副本
     */
    public Point2D getFocusPoint() {
        return new Point2D.Double(focus.getX(), focus.getY());
    }

    /**
     * 返回定义径向渐变的圆的半径。
     *
     * @return 定义径向渐变的圆的半径
     */
    public float getRadius() {
        return radius;
    }
}
