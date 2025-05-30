
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
 * {@code LinearGradientPaint} 类提供了一种用线性颜色渐变模式填充 {@link java.awt.Shape} 的方法。用户可以指定两个或多个渐变颜色，此画笔将在每个颜色之间进行插值。用户还指定起始点和结束点，以定义颜色渐变在用户空间中开始和结束的位置。
 * <p>
 * 用户必须提供一个浮点数组，指定如何在渐变中分布颜色。这些值应介于 0.0 到 1.0 之间，类似于渐变中的关键帧（它们标记渐变在某个特定颜色处的确切位置）。
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
 * 用户还可以通过设置 {@code CycleMethod} 选择 {@code LinearGradientPaint} 对象在填充起始点和结束点之外的空间时的行为，可以选择 {@code REFLECTION} 或 {@code REPEAT}。渐变在反射或重复副本中的任何两个颜色之间的距离与起始点和结束点之间的距离相同。
 * 请注意，由于像素粒度的采样，可能会出现一些微小的距离变化。
 * 如果未指定循环方法，默认选择 {@code NO_CYCLE}，这意味着将使用端点颜色填充剩余区域。
 * <p>
 * {@code colorSpace} 参数允许用户指定插值应在哪个颜色空间中进行，默认为 sRGB 或线性化 RGB。
 *
 * <p>
 * 以下代码演示了 {@code LinearGradientPaint} 的典型用法：
 * <pre>
 *     Point2D start = new Point2D.Float(0, 0);
 *     Point2D end = new Point2D.Float(50, 50);
 *     float[] dist = {0.0f, 0.2f, 1.0f};
 *     Color[] colors = {Color.RED, Color.WHITE, Color.BLUE};
 *     LinearGradientPaint p =
 *         new LinearGradientPaint(start, end, dist, colors);
 * </pre>
 * <p>
 * 此代码将创建一个 {@code LinearGradientPaint}，在渐变的前 20% 之间插值红色和白色，在剩余的 80% 之间插值白色和蓝色。
 *
 * <p>
 * 以下图像演示了上述示例代码的三种循环方法：
 * <center>
 * <img src = "doc-files/LinearGradientPaint.png"
 * alt="显示示例代码输出的图像">
 * </center>
 *
 * @see java.awt.Paint
 * @see java.awt.Graphics2D#setPaint
 * @author Nicholas Talian, Vincent Hardy, Jim Graham, Jerry Evans
 * @since 1.6
 */
public final class LinearGradientPaint extends MultipleGradientPaint {

    /** 渐变的起始点和结束点。 */
    private final Point2D start, end;

    /**
     * 构造一个默认重复方法为 {@code NO_CYCLE} 和颜色空间为 {@code SRGB} 的 {@code LinearGradientPaint}。
     *
     * @param startX 渐变轴起始点的 X 坐标，位于用户空间
     * @param startY 渐变轴起始点的 Y 坐标，位于用户空间
     * @param endX 渐变轴结束点的 X 坐标，位于用户空间
     * @param endY 渐变轴结束点的 Y 坐标，位于用户空间
     * @param fractions 范围从 0.0 到 1.0 的数字，指定沿渐变分布的颜色
     * @param colors 与每个分数值对应的颜色数组
     *
     * @throws NullPointerException
     * 如果 {@code fractions} 数组为 null，
     * 或 {@code colors} 数组为 null，
     * @throws IllegalArgumentException
     * 如果起始点和结束点是相同的点，
     * 或 {@code fractions.length != colors.length}，
     * 或 {@code colors} 的大小小于 2，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 未按严格递增顺序提供
     */
    public LinearGradientPaint(float startX, float startY,
                               float endX, float endY,
                               float[] fractions, Color[] colors)
    {
        this(new Point2D.Float(startX, startY),
             new Point2D.Float(endX, endY),
             fractions,
             colors,
             CycleMethod.NO_CYCLE);
    }

    /**
     * 构造一个默认颜色空间为 {@code SRGB} 的 {@code LinearGradientPaint}。
     *
     * @param startX 渐变轴起始点的 X 坐标，位于用户空间
     * @param startY 渐变轴起始点的 Y 坐标，位于用户空间
     * @param endX 渐变轴结束点的 X 坐标，位于用户空间
     * @param endY 渐变轴结束点的 Y 坐标，位于用户空间
     * @param fractions 范围从 0.0 到 1.0 的数字，指定沿渐变分布的颜色
     * @param colors 与每个分数值对应的颜色数组
     * @param cycleMethod 重复方法，可以是 {@code NO_CYCLE}、{@code REFLECT} 或 {@code REPEAT}
     *
     * @throws NullPointerException
     * 如果 {@code fractions} 数组为 null，
     * 或 {@code colors} 数组为 null，
     * 或 {@code cycleMethod} 为 null
     * @throws IllegalArgumentException
     * 如果起始点和结束点是相同的点，
     * 或 {@code fractions.length != colors.length}，
     * 或 {@code colors} 的大小小于 2，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 未按严格递增顺序提供
     */
    public LinearGradientPaint(float startX, float startY,
                               float endX, float endY,
                               float[] fractions, Color[] colors,
                               CycleMethod cycleMethod)
    {
        this(new Point2D.Float(startX, startY),
             new Point2D.Float(endX, endY),
             fractions,
             colors,
             cycleMethod);
    }

    /**
     * 构造一个默认重复方法为 {@code NO_CYCLE} 和颜色空间为 {@code SRGB} 的 {@code LinearGradientPaint}。
     *
     * @param start 渐变轴起始点的 {@code Point2D}，位于用户空间
     * @param end 渐变轴结束点的 {@code Point2D}，位于用户空间
     * @param fractions 范围从 0.0 到 1.0 的数字，指定沿渐变分布的颜色
     * @param colors 与每个分数值对应的颜色数组
     *
     * @throws NullPointerException
     * 如果其中一个点为 null，
     * 或 {@code fractions} 数组为 null，
     * 或 {@code colors} 数组为 null
     * @throws IllegalArgumentException
     * 如果起始点和结束点是相同的点，
     * 或 {@code fractions.length != colors.length}，
     * 或 {@code colors} 的大小小于 2，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 未按严格递增顺序提供
     */
    public LinearGradientPaint(Point2D start, Point2D end,
                               float[] fractions, Color[] colors)
    {
        this(start, end,
             fractions, colors,
             CycleMethod.NO_CYCLE);
    }

    /**
     * 构造一个默认颜色空间为 {@code SRGB} 的 {@code LinearGradientPaint}。
     *
     * @param start 渐变轴起始点的 {@code Point2D}，位于用户空间
     * @param end 渐变轴结束点的 {@code Point2D}，位于用户空间
     * @param fractions 范围从 0.0 到 1.0 的数字，指定沿渐变分布的颜色
     * @param colors 与每个分数值对应的颜色数组
     * @param cycleMethod 重复方法，可以是 {@code NO_CYCLE}、{@code REFLECT} 或 {@code REPEAT}
     *
     * @throws NullPointerException
     * 如果其中一个点为 null，
     * 或 {@code fractions} 数组为 null，
     * 或 {@code colors} 数组为 null，
     * 或 {@code cycleMethod} 为 null
     * @throws IllegalArgumentException
     * 如果起始点和结束点是相同的点，
     * 或 {@code fractions.length != colors.length}，
     * 或 {@code colors} 的大小小于 2，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 未按严格递增顺序提供
     */
    public LinearGradientPaint(Point2D start, Point2D end,
                               float[] fractions, Color[] colors,
                               CycleMethod cycleMethod)
    {
        this(start, end,
             fractions, colors,
             cycleMethod,
             ColorSpaceType.SRGB,
             new AffineTransform());
    }

    /**
     * 构造一个 {@code LinearGradientPaint}。
     *
     * @param start 渐变轴起始点的 {@code Point2D}，位于用户空间
     * @param end 渐变轴结束点的 {@code Point2D}，位于用户空间
     * @param fractions 范围从 0.0 到 1.0 的数字，指定沿渐变分布的颜色
     * @param colors 与每个分数值对应的颜色数组
     * @param cycleMethod 重复方法，可以是 {@code NO_CYCLE}、{@code REFLECT} 或 {@code REPEAT}
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
     * 如果起始点和结束点是相同的点，
     * 或 {@code fractions.length != colors.length}，
     * 或 {@code colors} 的大小小于 2，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 未按严格递增顺序提供
     */
    @ConstructorProperties({ "startPoint", "endPoint", "fractions", "colors", "cycleMethod", "colorSpace", "transform" })
    public LinearGradientPaint(Point2D start, Point2D end,
                               float[] fractions, Color[] colors,
                               CycleMethod cycleMethod,
                               ColorSpaceType colorSpace,
                               AffineTransform gradientTransform)
    {
        super(fractions, colors, cycleMethod, colorSpace, gradientTransform);

        // 检查输入参数
        if (start == null || end == null) {
            throw new NullPointerException("起始点和结束点必须非空");
        }

        if (start.equals(end)) {
            throw new IllegalArgumentException("起始点不能等于结束点");
        }

        // 复制点...
        this.start = new Point2D.Double(start.getX(), start.getY());
        this.end = new Point2D.Double(end.getX(), end.getY());
    }

    /**
     * 创建并返回一个用于生成线性颜色渐变模式的 {@link PaintContext}。
     * 有关空参数处理的信息，请参阅 {@link Paint} 接口中的 {@link Paint#createContext} 方法说明。
     *
     * @param cm 代表调用者接收像素数据最方便格式的首选 {@link ColorModel}，或 {@code null} 表示没有偏好。
     * @param deviceBounds 被渲染的图形基元的设备空间边界框。
     * @param userBounds 被渲染的图形基元的用户空间边界框。
     * @param transform 从用户空间到设备空间的 {@link AffineTransform}。
     * @param hints 上下文对象可以用来选择渲染替代方案的一组提示。
     * @return 用于生成颜色模式的 {@code PaintContext}。
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

        if ((fractions.length == 2) &&
            (cycleMethod != CycleMethod.REPEAT) &&
            (colorSpace == ColorSpaceType.SRGB))
        {
            // 对于这种常见情况，使用基本的 GradientPaintContext 更快
            boolean cyclic = (cycleMethod != CycleMethod.NO_CYCLE);
            return new GradientPaintContext(cm, start, end,
                                            transform,
                                            colors[0], colors[1],
                                            cyclic);
        } else {
            return new LinearGradientPaintContext(this, cm,
                                                  deviceBounds, userBounds,
                                                  transform, hints,
                                                  start, end,
                                                  fractions, colors,
                                                  cycleMethod, colorSpace);
        }
    }


                /**
     * 返回渐变轴起点的副本。
     *
     * @return 一个 {@code Point2D} 对象，它是此 {@code LinearGradientPaint} 的第一个颜色的锚点的副本
     */
    public Point2D getStartPoint() {
        return new Point2D.Double(start.getX(), start.getY());
    }

    /**
     * 返回渐变轴终点的副本。
     *
     * @return 一个 {@code Point2D} 对象，它是此 {@code LinearGradientPaint} 的最后一个颜色的锚点的副本
     */
    public Point2D getEndPoint() {
        return new Point2D.Double(end.getX(), end.getY());
    }
}
