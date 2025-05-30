
/*
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

/**
 * 为 RadialGradientPaint 提供实际实现。
 * 这里是进行像素处理的地方。RadialGradientPaint 仅支持圆形渐变，但可以通过传递到 RadialGradientPaint 构造函数中的渐变变换来缩放圆，使其看起来近似椭圆形。
 *
 * @author Nicholas Talian, Vincent Hardy, Jim Graham, Jerry Evans
 */
final class RadialGradientPaintContext extends MultipleGradientPaintContext {

    /** 当 (focus == center) 时为真。 */
    private boolean isSimpleFocus = false;

    /** 当 (cycleMethod == NO_CYCLE) 时为真。 */
    private boolean isNonCyclic = false;

    /** 定义 100% 渐变停止的最外圈的半径。 */
    private float radius;

    /** 表示中心点和焦点的变量。 */
    private float centerX, centerY, focusX, focusY;

    /** 渐变圆的半径的平方。 */
    private float radiusSq;

    /** X, Y 用户空间坐标的常数部分。 */
    private float constA, constB;

    /** 简单循环的常数二阶增量。 */
    private float gDeltaDelta;

    /**
     * 当 focusX == X 时，此值表示解决方案。它被称为“简单”解决方案，因为比一般情况更容易计算。
     */
    private float trivial;

    /** 当夹紧焦点时的偏移量。 */
    private static final float SCALEBACK = .99f;

    /**
     * RadialGradientPaintContext 的构造函数。
     *
     * @param paint 从中创建此上下文的 {@code RadialGradientPaint}
     * @param cm 接收 {@code Paint} 数据的 {@code ColorModel}（仅用作提示）
     * @param deviceBounds 被渲染的图形基元的设备空间边界框
     * @param userBounds 被渲染的图形基元的用户空间边界框
     * @param t 从用户空间到设备空间的 {@code AffineTransform}（应与 gradientTransform 连接）
     * @param hints 上下文对象用于选择渲染替代方案的提示
     * @param cx 定义渐变的圆的中心 X 坐标（用户空间）。渐变的最后一个颜色映射到该圆的周长。
     * @param cy 定义渐变的圆的中心 Y 坐标（用户空间）。渐变的最后一个颜色映射到该圆的周长。
     * @param r 定义颜色渐变范围的圆的半径
     * @param fx 第一个颜色映射到的 X 坐标（用户空间）
     * @param fy 第一个颜色映射到的 Y 坐标（用户空间）
     * @param fractions 指定渐变分布的分数
     * @param colors 渐变颜色
     * @param cycleMethod 无循环、反射或重复
     * @param colorSpace 用于插值的颜色空间，可以是 SRGB 或 LINEAR_RGB
     */
    RadialGradientPaintContext(RadialGradientPaint paint,
                               ColorModel cm,
                               Rectangle deviceBounds,
                               Rectangle2D userBounds,
                               AffineTransform t,
                               RenderingHints hints,
                               float cx, float cy,
                               float r,
                               float fx, float fy,
                               float[] fractions,
                               Color[] colors,
                               CycleMethod cycleMethod,
                               ColorSpaceType colorSpace)
    {
        super(paint, cm, deviceBounds, userBounds, t, hints,
              fractions, colors, cycleMethod, colorSpace);

        // 复制一些参数
        centerX = cx;
        centerY = cy;
        focusX = fx;
        focusY = fy;
        radius = r;

        this.isSimpleFocus = (focusX == centerX) && (focusY == centerY);
        this.isNonCyclic = (cycleMethod == CycleMethod.NO_CYCLE);

        // 用于二次方程
        radiusSq = radius * radius;

        float dX = focusX - centerX;
        float dY = focusY - centerY;

        double distSq = (dX * dX) + (dY * dY);

        // 测试焦点到中心的距离是否大于半径
        if (distSq > radiusSq * SCALEBACK) {
            // 夹紧焦点到半径
            float scalefactor = (float)Math.sqrt(radiusSq * SCALEBACK / distSq);
            dX = dX * scalefactor;
            dY = dY * scalefactor;
            focusX = centerX + dX;
            focusY = centerY + dY;
        }

        // 计算在 cyclicCircularGradientFillRaster() 中 X == focusX 时使用的解决方案
        trivial = (float)Math.sqrt(radiusSq - (dX * dX));

        // X, Y 用户空间坐标的常数部分
        constA = a02 - centerX;
        constB = a12 - centerY;

        // 简单循环的常数二阶增量
        gDeltaDelta = 2 * ( a00 *  a00 +  a10 *  a10) / radiusSq;
    }

    /**
     * 返回包含为图形操作生成的颜色的 Raster。
     *
     * @param x,y,w,h 生成颜色的设备空间区域。
     */
    protected void fillRaster(int pixels[], int off, int adjust,
                              int x, int y, int w, int h)
    {
        if (isSimpleFocus && isNonCyclic && isSimpleLookup) {
            simpleNonCyclicFillRaster(pixels, off, adjust, x, y, w, h);
        } else {
            cyclicCircularGradientFillRaster(pixels, off, adjust, x, y, w, h);
        }
    }

    /**
     * 该代码适用于最简单的情况，即焦点 == 中心点，渐变是非循环的，并且渐变查找方法是快速的（单个数组索引，无需转换）。
     */
    private void simpleNonCyclicFillRaster(int pixels[], int off, int adjust,
                                           int x, int y, int w, int h)
    {
        /* 我们计算相对于半径大小的 sqrt(X^2 + Y^2) 以获取要使用的颜色的分数。
         *
         * 每次沿扫描线移动时，(X, Y) 增加 (a00, a10)。
         * 如果我们预先计算：
         *   gRel = X^2 + Y^2
         * 对于行的开始，然后对于每个步骤，我们需要计算：
         *   gRel' = (X + a00)^2 + (Y + a10)^2
         *         = X^2 + 2 * X * a00 + a00^2 + Y^2 + 2 * Y * a10 + a10^2
         *         = (X^2 + Y^2) + 2 * (X * a00 + Y * a10) + (a00^2 + a10^2)
         *         = gRel + 2 * (X * a00 + Y * a10) + (a00^2 + a10^2)
         *         = gRel + 2 * DP + SD
         * （其中 DP = X, Y 和 a00, a10 之间的点积
         *  和   SD = 差向量的点积平方）
         * 对于之后的步骤，我们得到：
         *   gRel'' = (X + 2 * a00)^2 + (Y + 2 * a10)^2
         *          = X^2 + 4 * X * a00 + 4 * a00^2 + Y^2 + 4 * Y * a10 + 4 * a10^2
         *          = (X^2 + Y^2) + 4 * (X * a00 + Y * a10) + 4 * (a00^2 + a10^2)
         *          = gRel + 4 * DP + 4 * SD
         *          = gRel' + 2 * DP + 3 * SD
         * 增量变化为：
         *     (gRel'' - gRel') - (gRel' - gRel)
         *   = (2 * DP + 3 * SD) - (2 * DP + SD)
         *   = 2 * SD
         * 注意，此值仅依赖于（逆）变换矩阵，因此是循环中的常数。
         * 为了使所有值相对于单位圆，我们需要按以下方式除以所有值：
         *   [XY] /= radius
         *   gRel /= radiusSq
         *   DP   /= radiusSq
         *   SD   /= radiusSq
         */
        // 相对于中心的用户空间中 UL 角的坐标
        float rowX = (a00 * x) + (a01 * y) + constA;
        float rowY = (a10 * x) + (a11 * y) + constB;

        // 在构造函数中计算的二阶增量
        float gDeltaDelta = this.gDeltaDelta;

        // adjust 是 (scan - w) 的 pixels 数组，我们需要 (scan)
        adjust += w;

        // 当距离超过渐变半径时使用的 1.0 颜色的 RGB
        int rgbclip = gradient[fastGradientArraySize];

        for (int j = 0; j < h; j++) {
            // 这些值取决于行开始的坐标
            float gRel   =      (rowX * rowX + rowY * rowY) / radiusSq;
            float gDelta = (2 * ( a00 * rowX +  a10 * rowY) / radiusSq +
                            gDeltaDelta / 2);

            /* 对于 gRel >= 1 的任何情况使用优化的循环。
             * 我们不需要计算这些值的 sqrt(gRel)，因为 sqrt(N >= 1) == (M >= 1)。
             * 注意 gRel 遵循一个抛物线，只能在每个扫描线中心周围的小区域内 < 1。特别是：
             *   gDeltaDelta 始终为正
             *   gDelta 在越过中点之前 < 0，然后 > 0
             * 在该区域的左侧和右侧，它将始终 >= 1，直到无穷大，因此我们可以按 3 个区域处理该行：
             *   向左 - 快速填充直到 gRel < 1，更新 gRel
             *   中心 - 当 gRel < 1 时慢速分数 = sqrt 填充
             *   向右 - 快速填充扫描线的其余部分，忽略 gRel
             */
            int i = 0;
            // 快速填充“向左”
            while (i < w && gRel >= 1.0f) {
                pixels[off + i] = rgbclip;
                gRel += gDelta;
                gDelta += gDeltaDelta;
                i++;
            }
            // 慢速填充“中心”
            while (i < w && gRel < 1.0f) {
                int gIndex;

                if (gRel <= 0) {
                    gIndex = 0;
                } else {
                    float fIndex = gRel * SQRT_LUT_SIZE;
                    int iIndex = (int) (fIndex);
                    float s0 = sqrtLut[iIndex];
                    float s1 = sqrtLut[iIndex + 1] - s0;
                    fIndex = s0 + (fIndex - iIndex) * s1;
                    gIndex = (int) (fIndex * fastGradientArraySize);
                }

                // 存储该点的颜色
                pixels[off + i] = gradient[gIndex];

                // 增量计算
                gRel += gDelta;
                gDelta += gDeltaDelta;
                i++;
            }
            // 快速填充“向右”到行的末尾
            while (i < w) {
                pixels[off + i] = rgbclip;
                i++;
            }

            off += adjust;
            rowX += a01;
            rowY += a11;
        }
    }

    // SQRT_LUT_SIZE 必须是 2 的幂，以使上述测试有效。
    private static final int SQRT_LUT_SIZE = (1 << 11);
    private static float sqrtLut[] = new float[SQRT_LUT_SIZE + 1];
    static {
        for (int i = 0; i < sqrtLut.length; i++) {
            sqrtLut[i] = (float) Math.sqrt(i / ((float) SQRT_LUT_SIZE));
        }
    }

    /**
     * 填充光栅，当点落在 100% 停止圆的周长之外时循环渐变颜色。
     *
     * 该计算首先计算从焦点到光栅中当前点的线与渐变圆周长的交点。
     *
     * 然后确定当前点沿该线的百分比距离（焦点为 0%，周长为 100%）。
     *
     * 以 (a, b) 为中心、半径为 r 的圆的方程：
     *     (x - a)^2 + (y - b)^2 = r^2
     * 斜率为 m、y 轴截距为 b 的直线方程：
     *     y = mx + b
     * 用直线方程替换圆的方程并使用二次公式求解，产生以下方程组。常数因子已从内循环中提取出来。
     */
    private void cyclicCircularGradientFillRaster(int pixels[], int off,
                                                  int adjust,
                                                  int x, int y,
                                                  int w, int h)
    {
        // 二次方程的 C 因子的常数部分
        final double constC =
            -radiusSq + (centerX * centerX) + (centerY * centerY);

        // 二次方程的系数 (Ax^2 + Bx + C = 0)
        double A, B, C;

        // 焦点-周长线的斜率和 y 轴截距
        double slope, yintcpt;

        // 与圆相交的 X, Y 坐标
        double solutionX, solutionY;

        // X, Y 坐标的常数部分
        final float constX = (a00 * x) + (a01 * y) + a02;
        final float constY = (a10 * x) + (a11 * y) + a12;

        // 内循环二次公式中的常数
        final float precalc2 =  2 * centerY;
        final float precalc3 = -2 * centerX;

        // 0 到 1 之间的值，指定在渐变中的位置
        float g;

        // 二次公式的判别式（应始终 > 0）
        float det;

        // 当前点到焦点的平方距离
        float currentToFocusSq;

        // 交点到焦点的平方距离
        float intersectToFocusSq;

        // 临时变量，用于 X, Y 的平方变化
        float deltaXSq, deltaYSq;

        // 用于索引 pixels 数组
        int indexer = off;

        // pixels 数组的增量索引变化
        int pixInc = w + adjust;

        // 对于每一行
        for (int j = 0; j < h; j++) {

            // 用户空间点；这些在列与列之间是常数
            float X = (a01 * j) + constX;
            float Y = (a11 * j) + constY;


                        // 对于每一列（内循环从这里开始）
            for (int i = 0; i < w; i++) {

                if (X == focusX) {
                    // 特殊情况，避免除以零
                    solutionX = focusX;
                    solutionY = centerY;
                    solutionY += (Y > focusY) ? trivial : -trivial;
                } else {
                    // 焦点到周长线的斜率和y轴截距
                    slope = (Y - focusY) / (X - focusX);
                    yintcpt = Y - (slope * X);

                    // 使用二次公式计算交点
                    A = (slope * slope) + 1;
                    B = precalc3 + (-2 * slope * (centerY - yintcpt));
                    C = constC + (yintcpt * (yintcpt - precalc2));

                    det = (float)Math.sqrt((B * B) - (4 * A * C));
                    solutionX = -B;

                    // 根据X坐标相对于焦点的位置选择正根或负根
                    solutionX += (X < focusX)? -det : det;
                    solutionX = solutionX / (2 * A); // 除数
                    solutionY = (slope * solutionX) + yintcpt;
                }

                // 计算当前点到焦点的距离的平方和交点到焦点的距离的平方。
                // 想要平方值，以便在除法后进行一次平方根运算，而不是在除法前进行两次。

                deltaXSq = X - focusX;
                deltaXSq = deltaXSq * deltaXSq;

                deltaYSq = Y - focusY;
                deltaYSq = deltaYSq * deltaYSq;

                currentToFocusSq = deltaXSq + deltaYSq;

                deltaXSq = (float)solutionX - focusX;
                deltaXSq = deltaXSq * deltaXSq;

                deltaYSq = (float)solutionY - focusY;
                deltaYSq = deltaYSq * deltaYSq;

                intersectToFocusSq = deltaXSq + deltaYSq;

                // 获取当前点在焦点到周长线上的百分比（0-1）
                g = (float)Math.sqrt(currentToFocusSq / intersectToFocusSq);

                // 存储该点的颜色
                pixels[indexer + i] = indexIntoGradientsArrays(g);

                // X、Y的增量变化
                X += a00;
                Y += a10;
            } // 结束内循环

            indexer += pixInc;
        } // 结束外循环
    }
}
