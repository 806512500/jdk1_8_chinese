/*
 * Copyright (c) 2006, 2011, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.image.ColorModel;
import java.lang.ref.SoftReference;
import java.util.Arrays;

/**
 * 这是使用多种颜色渐变填充其光栅的 Paint 的超类。它为
 * {@code LinearGradientPaint} 和 {@code RadialGradientPaint} 提供了存储变量和
 * 枚举值的公共部分。
 *
 * @author Nicholas Talian, Vincent Hardy, Jim Graham, Jerry Evans
 * @since 1.6
 */
public abstract class MultipleGradientPaint implements Paint {

    /** 用于在渐变边界外绘制的方法。
     * @since 1.6
     */
    public static enum CycleMethod {
        /**
         * 使用终端颜色填充剩余区域。
         */
        NO_CYCLE,

        /**
         * 循环渐变颜色，从开始到结束，从结束到开始
         * 填充剩余区域。
         */
        REFLECT,

        /**
         * 循环渐变颜色，从开始到结束，从开始到结束
         * 填充剩余区域。
         */
        REPEAT
    }

    /** 执行渐变插值的颜色空间。
     * @since 1.6
     */
    public static enum ColorSpaceType {
        /**
         * 表示颜色插值应在 sRGB 空间中进行。
         */
        SRGB,

        /**
         * 表示颜色插值应在线性化
         * RGB 空间中进行。
         */
        LINEAR_RGB
    }

    /** 此 Paint 对象的透明度。 */
    final int transparency;

    /** 范围在 0 到 1 之间的渐变关键帧值。 */
    final float[] fractions;

    /** 渐变颜色。 */
    final Color[] colors;

    /** 应用于渐变的变换。 */
    final AffineTransform gradientTransform;

    /** 用于在渐变边界外绘制的方法。 */
    final CycleMethod cycleMethod;

    /** 执行渐变插值的颜色空间。 */
    final ColorSpaceType colorSpace;

    /**
     * 以下字段仅由 MultipleGradientPaintContext 使用，用于缓存某些保持不变且不需要
     * 为从该 Paint 实例创建的每个上下文重新计算的值。
     */
    ColorModel model;
    float[] normalizedIntervals;
    boolean isSimpleLookup;
    SoftReference<int[][]> gradients;
    SoftReference<int[]> gradient;
    int fastGradientArraySize;

    /**
     * 包私有构造函数。
     *
     * @param fractions 从 0.0 到 1.0 的数字，指定沿渐变分布的颜色
     * @param colors 与每个分数值对应的颜色数组
     * @param cycleMethod 用于在渐变边界外绘制的方法，可以是 {@code NO_CYCLE}、{@code REFLECT} 或 {@code REPEAT}
     * @param colorSpace 用于插值的颜色空间，可以是 {@code SRGB} 或 {@code LINEAR_RGB}
     * @param gradientTransform 应用于渐变的变换
     *
     * @throws NullPointerException
     * 如果 {@code fractions} 数组为 null，
     * 或 {@code colors} 数组为 null，
     * 或 {@code gradientTransform} 为 null，
     * 或 {@code cycleMethod} 为 null，
     * 或 {@code colorSpace} 为 null
     * @throws IllegalArgumentException
     * 如果 {@code fractions.length != colors.length}，
     * 或 {@code colors} 的长度小于 2，
     * 或 {@code fractions} 值小于 0.0 或大于 1.0，
     * 或 {@code fractions} 不是严格递增的
     */
    MultipleGradientPaint(float[] fractions,
                          Color[] colors,
                          CycleMethod cycleMethod,
                          ColorSpaceType colorSpace,
                          AffineTransform gradientTransform)
    {
        if (fractions == null) {
            throw new NullPointerException("Fractions array cannot be null");
        }

        if (colors == null) {
            throw new NullPointerException("Colors array cannot be null");
        }

        if (cycleMethod == null) {
            throw new NullPointerException("Cycle method cannot be null");
        }

        if (colorSpace == null) {
            throw new NullPointerException("Color space cannot be null");
        }

        if (gradientTransform == null) {
            throw new NullPointerException("Gradient transform cannot be "+
                                           "null");
        }

        if (fractions.length != colors.length) {
            throw new IllegalArgumentException("Colors and fractions must " +
                                               "have equal size");
        }

        if (colors.length < 2) {
            throw new IllegalArgumentException("User must specify at least " +
                                               "2 colors");
        }

        // 检查值是否在正确范围内并按递增顺序排列
        float previousFraction = -1.0f;
        for (float currentFraction : fractions) {
            if (currentFraction < 0f || currentFraction > 1f) {
                throw new IllegalArgumentException("Fraction values must " +
                                                   "be in the range 0 to 1: " +
                                                   currentFraction);
            }

            if (currentFraction <= previousFraction) {
                throw new IllegalArgumentException("Keyframe fractions " +
                                                   "must be increasing: " +
                                                   currentFraction);
            }

            previousFraction = currentFraction;
        }

        // 处理第一个渐变停止点不等于 0 和/或最后一个渐变停止点不等于 1 的情况。
        // 在这两种情况下，创建一个新点并复制前一个极端点的颜色。
        boolean fixFirst = false;
        boolean fixLast = false;
        int len = fractions.length;
        int off = 0;

        if (fractions[0] != 0f) {
            // 第一个停止点不等于零，修复此条件
            fixFirst = true;
            len++;
            off++;
        }
        if (fractions[fractions.length-1] != 1f) {
            // 最后一个停止点不等于一，修复此条件
            fixLast = true;
            len++;
        }

        this.fractions = new float[len];
        System.arraycopy(fractions, 0, this.fractions, off, fractions.length);
        this.colors = new Color[len];
        System.arraycopy(colors, 0, this.colors, off, colors.length);

        if (fixFirst) {
            this.fractions[0] = 0f;
            this.colors[0] = colors[0];
        }
        if (fixLast) {
            this.fractions[len-1] = 1f;
            this.colors[len-1] = colors[colors.length - 1];
        }

        // 复制一些标志
        this.colorSpace = colorSpace;
        this.cycleMethod = cycleMethod;

        // 复制渐变变换
        this.gradientTransform = new AffineTransform(gradientTransform);

        // 确定透明度
        boolean opaque = true;
        for (int i = 0; i < colors.length; i++){
            opaque = opaque && (colors[i].getAlpha() == 0xff);
        }
        this.transparency = opaque ? OPAQUE : TRANSLUCENT;
    }

    /**
     * 返回用于计算颜色分布的浮点数数组的副本。
     * 返回的数组始终以 0 作为第一个值，以 1 作为最后一个值，中间值递增。
     *
     * @return 用于计算颜色分布的浮点数数组的副本
     */
    public final float[] getFractions() {
        return Arrays.copyOf(fractions, fractions.length);
    }

    /**
     * 返回用于渐变的颜色数组的副本。
     * 第一个颜色映射到分数数组中的第一个值，
     * 最后一个颜色映射到分数数组中的最后一个值。
     *
     * @return 用于渐变的颜色数组的副本
     */
    public final Color[] getColors() {
        return Arrays.copyOf(colors, colors.length);
    }

    /**
     * 返回指定循环行为的枚举类型。
     *
     * @return 指定循环行为的枚举类型
     */
    public final CycleMethod getCycleMethod() {
        return cycleMethod;
    }

    /**
     * 返回指定插值颜色空间的枚举类型。
     *
     * @return 指定插值颜色空间的枚举类型
     */
    public final ColorSpaceType getColorSpace() {
        return colorSpace;
    }

    /**
     * 返回应用于渐变的变换的副本。
     *
     * <p>
     * 注意，如果在创建渐变时未应用任何变换，
     * 则使用单位变换。
     *
     * @return 应用于渐变的变换的副本
     */
    public final AffineTransform getTransform() {
        return new AffineTransform(gradientTransform);
    }

    /**
     * 返回此 {@code Paint} 对象的透明度模式。
     *
     * @return 如果此 {@code Paint} 对象使用的所有颜色都是不透明的，则返回 {@code OPAQUE}，
     *         如果此 {@code Paint} 对象使用的颜色中至少有一个不是不透明的，则返回 {@code TRANSLUCENT}。
     * @see java.awt.Transparency
     */
    public final int getTransparency() {
        return transparency;
    }
}
