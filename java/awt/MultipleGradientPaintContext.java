
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

import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * 这是所有使用多种颜色渐变填充其光栅的 PaintContext 的超类。它提供了实际的颜色插值功能。子类只需要处理使用渐变来填充光栅中的像素。
 *
 * @author Nicholas Talian, Vincent Hardy, Jim Graham, Jerry Evans
 */
abstract class MultipleGradientPaintContext implements PaintContext {

    /**
     * PaintContext 的 ColorModel。如果所有颜色都不是完全不透明的，则为 ARGB，否则为 RGB。
     */
    protected ColorModel model;

    /** 如果渐变颜色都是不透明的，则使用的颜色模型。 */
    private static ColorModel xrgbmodel =
        new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff);

    /** 缓存的 ColorModel。 */
    protected static ColorModel cachedModel;

    /** 缓存的光栅，可以在实例之间重用。 */
    protected static WeakReference<Raster> cached;

    /** 可以在可能的情况下重用的光栅。 */
    protected Raster saved;

    /** 在渐变边界外绘制时使用的方法。 */
    protected CycleMethod cycleMethod;

    /** 执行插值的颜色空间。 */
    protected ColorSpaceType colorSpace;

    /** 逆变换矩阵的元素。 */
    protected float a00, a01, a10, a11, a02, a12;

    /**
     * 这个布尔值指定我们是否处于简单查找模式，其中 0 到 1 之间的输入值可以直接索引到单个渐变颜色数组中。如果这个布尔值为 false，则需要使用两步过程来确定我们属于哪个渐变数组，然后确定该数组中的索引。
     */
    protected boolean isSimpleLookup;

    /**
     * 渐变数组的大小，用于在快速查找颜色时缩放 0-1 索引。
     */
    protected int fastGradientArraySize;

    /**
     * 包含每个区间插值颜色值的数组，由 calculateSingleArrayGradient() 使用。它是受保护的，以便子类可以直接访问。
     */
    protected int[] gradient;

    /**
     * 每个区间的渐变数组。由 calculateMultipleArrayGradient() 使用。
     */
    private int[][] gradients;

    /** 归一化的区间数组。 */
    private float[] normalizedIntervals;

    /** 分数数组。 */
    private float[] fractions;

    /** 用于确定渐变颜色是否都是不透明的。 */
    private int transparencyTest;

    /** 颜色空间转换查找表。 */
    private static final int SRGBtoLinearRGB[] = new int[256];
    private static final int LinearRGBtoSRGB[] = new int[256];

    static {
        // 构建查找表
        for (int k = 0; k < 256; k++) {
            SRGBtoLinearRGB[k] = convertSRGBtoLinearRGB(k);
            LinearRGBtoSRGB[k] = convertLinearRGBtoSRGB(k);
        }
    }

    /**
     * 任意两种颜色之间最大颜色数的常量。用于创建和索引渐变数组。
     */
    protected static final int GRADIENT_SIZE = 256;
    protected static final int GRADIENT_SIZE_INDEX = GRADIENT_SIZE -1;

    /**
     * 快速单数组的最大长度。如果估计的数组大小大于此值，则切换到慢查找方法。选择这个数字没有特别的原因，但它似乎为常见情况（快速查找）提供了令人满意的性能。
     */
    private static final int MAX_GRADIENT_ARRAY_SIZE = 5000;

    /**
     * MultipleGradientPaintContext 超类的构造函数。
     */
    protected MultipleGradientPaintContext(MultipleGradientPaint mgp,
                                           ColorModel cm,
                                           Rectangle deviceBounds,
                                           Rectangle2D userBounds,
                                           AffineTransform t,
                                           RenderingHints hints,
                                           float[] fractions,
                                           Color[] colors,
                                           CycleMethod cycleMethod,
                                           ColorSpaceType colorSpace)
    {
        if (deviceBounds == null) {
            throw new NullPointerException("设备边界不能为空");
        }

        if (userBounds == null) {
            throw new NullPointerException("用户边界不能为空");
        }

        if (t == null) {
            throw new NullPointerException("变换不能为空");
        }

        if (hints == null) {
            throw new NullPointerException("RenderingHints 不能为空");
        }

        // 需要逆变换来从设备空间转换到用户空间。获取逆变换矩阵的所有组件。
        AffineTransform tInv;
        try {
            // 假设调用者已经复制了传入的变换，并且不关心它被修改
            t.invert();
            tInv = t;
        } catch (NoninvertibleTransformException e) {
            // 在这种情况下使用单位变换；显示（不正确）结果比抛出异常或不操作要好
            tInv = new AffineTransform();
        }
        double m[] = new double[6];
        tInv.getMatrix(m);
        a00 = (float)m[0];
        a10 = (float)m[1];
        a01 = (float)m[2];
        a11 = (float)m[3];
        a02 = (float)m[4];
        a12 = (float)m[5];

        // 复制一些标志
        this.cycleMethod = cycleMethod;
        this.colorSpace = colorSpace;

        // 我们可以避免复制这个数组，因为我们不会修改它的值
        this.fractions = fractions;

        // 请注意，这两个值中只有一个可以是非空的（我们要么存储快速渐变数组，要么存储慢速数组，但不会同时存储两者）
        int[] gradient =
            (mgp.gradient != null) ? mgp.gradient.get() : null;
        int[][] gradients =
            (mgp.gradients != null) ? mgp.gradients.get() : null;

        if (gradient == null && gradients == null) {
            // 需要（重新）创建适当的值
            calculateLookupData(colors);

            // 现在将计算的值缓存在 MultipleGradientPaint 实例中以供将来使用
            mgp.model               = this.model;
            mgp.normalizedIntervals = this.normalizedIntervals;
            mgp.isSimpleLookup      = this.isSimpleLookup;
            if (isSimpleLookup) {
                // 只缓存快速数组
                mgp.fastGradientArraySize = this.fastGradientArraySize;
                mgp.gradient = new SoftReference<int[]>(this.gradient);
            } else {
                // 只缓存慢速数组
                mgp.gradients = new SoftReference<int[][]>(this.gradients);
            }
        } else {
            // 使用 MultipleGradientPaint 实例中缓存的值
            this.model                 = mgp.model;
            this.normalizedIntervals   = mgp.normalizedIntervals;
            this.isSimpleLookup        = mgp.isSimpleLookup;
            this.gradient              = gradient;
            this.fastGradientArraySize = mgp.fastGradientArraySize;
            this.gradients             = gradients;
        }
    }

    /**
     * 这个函数是这个类的核心。它根据分数数组和这些分数处的颜色值计算渐变颜色数组。
     */
    private void calculateLookupData(Color[] colors) {
        Color[] normalizedColors;
        if (colorSpace == ColorSpaceType.LINEAR_RGB) {
            // 创建一个新的颜色数组
            normalizedColors = new Color[colors.length];
            // 使用查找表转换颜色
            for (int i = 0; i < colors.length; i++) {
                int argb = colors[i].getRGB();
                int a = argb >>> 24;
                int r = SRGBtoLinearRGB[(argb >> 16) & 0xff];
                int g = SRGBtoLinearRGB[(argb >>  8) & 0xff];
                int b = SRGBtoLinearRGB[(argb      ) & 0xff];
                normalizedColors[i] = new Color(r, g, b, a);
            }
        } else {
            // 在 SRGB 情况下，我们可以直接引用这个数组，因为我们不会修改它的值
            normalizedColors = colors;
        }

        // 这将存储渐变停止之间的区间（距离）
        normalizedIntervals = new float[fractions.length-1];

        // 将分数转换为区间
        for (int i = 0; i < normalizedIntervals.length; i++) {
            // 区间距离等于位置的差值
            normalizedIntervals[i] = this.fractions[i+1] - this.fractions[i];
        }

        // 初始化为完全不透明，以便与颜色进行 AND 操作
        transparencyTest = 0xff000000;

        // 插值数组的数组
        gradients = new int[normalizedIntervals.length][];

        // 找到最小的区间
        float Imin = 1;
        for (int i = 0; i < normalizedIntervals.length; i++) {
            Imin = (Imin > normalizedIntervals[i]) ?
                normalizedIntervals[i] : Imin;
        }

        // 估计整个渐变数组的大小。
        // 这是为了防止极小的区间导致数组大小急剧增加。如果估计的大小过大，则使用每个区间单独的数组，并在查找时使用索引方案。
        int estimatedSize = 0;
        for (int i = 0; i < normalizedIntervals.length; i++) {
            estimatedSize += (normalizedIntervals[i]/Imin) * GRADIENT_SIZE;
        }

        if (estimatedSize > MAX_GRADIENT_ARRAY_SIZE) {
            // 慢方法
            calculateMultipleArrayGradient(normalizedColors);
        } else {
            // 快方法
            calculateSingleArrayGradient(normalizedColors, Imin);
        }

        // 使用最“经济”的模型
        if ((transparencyTest >>> 24) == 0xff) {
            model = xrgbmodel;
        } else {
            model = ColorModel.getRGBdefault();
        }
    }

    /**
     * 快速查找方法
     *
     * 这个方法计算渐变颜色值，并将它们放在一个单个的 int 数组 gradient[] 中。它通过根据每个区间相对于数组中最小区间的大小分配空间来实现。最小区间分配 255 个插值值（24 位颜色系统中最大数量的唯一中间颜色），所有其他区间根据其大小与最小区间的比例分配空间。
     *
     * 这种方案加快了检索速度，因为颜色是根据用户指定的分布沿数组分布的。只需要一个从 0 到 1 的相对索引。
     *
     * 这种方法的唯一问题是，如果存在一个不成比例的小渐变区间，数组大小可能会急剧增加。在这种情况下，其他区间将分配巨大的空间，但其中大部分数据是冗余的。因此，我们需要使用下面的空间节省方案。
     *
     * @param Imin 最小区间的大小
     */
    private void calculateSingleArrayGradient(Color[] colors, float Imin) {
        // 设置标志，以便稍后知道这是一个简单的（快速）查找
        isSimpleLookup = true;

        // 需要插值的两种颜色
        int rgb1, rgb2;

        // 单个数组的最终大小
        int gradientsTot = 1;

        // 对于每个区间（两种颜色之间的过渡）
        for (int i = 0; i < gradients.length; i++) {
            // 根据与最小区间的比例创建一个数组
            int nGradients = (int)((normalizedIntervals[i]/Imin)*255f);
            gradientsTot += nGradients;
            gradients[i] = new int[nGradients];

            // 需要插值的两种颜色（关键帧）
            rgb1 = colors[i].getRGB();
            rgb2 = colors[i+1].getRGB();

            // 填充这个数组，使其包含 rgb1 和 rgb2 之间的颜色
            interpolate(rgb1, rgb2, gradients[i]);

            // 如果颜色是不透明的，透明度应仍为 0xff000000
            transparencyTest &= rgb1;
            transparencyTest &= rgb2;
        }

        // 将所有渐变放入一个单个数组中
        gradient = new int[gradientsTot];
        int curOffset = 0;
        for (int i = 0; i < gradients.length; i++){
            System.arraycopy(gradients[i], 0, gradient,
                             curOffset, gradients[i].length);
            curOffset += gradients[i].length;
        }
        gradient[gradient.length-1] = colors[colors.length-1].getRGB();

        // 如果插值是在 Linear RGB 空间中进行的，则使用查找表将渐变转换回 sRGB
        if (colorSpace == ColorSpaceType.LINEAR_RGB) {
            for (int i = 0; i < gradient.length; i++) {
                gradient[i] = convertEntireColorLinearRGBtoSRGB(gradient[i]);
            }
        }
    }


                    fastGradientArraySize = gradient.length - 1;
    }

    /**
     * 慢速查找方法
     *
     * 此方法计算每个区间的渐变颜色值，并将每个值放入一个大小为255的数组中。数组存储在
     * gradients[][] 中。（使用255是因为这是24位颜色系统中两个任意颜色之间最大数量的
     * 唯一颜色。）
     *
     * 此方法使用最少的空间（仅255 * 区间数量），但会加重查找过程，因为现在我们必须
     * 找出要选择哪个区间，然后计算该区间内的索引。这会导致显著的性能损失，因为每次渲染
     * 循环中的每个点都需要进行此计算。
     *
     * 对于感兴趣的读者，这是一个典型的时间-空间权衡示例。
     */
    private void calculateMultipleArrayGradient(Color[] colors) {
        // 设置标志，以便稍后知道这不是简单的查找
        isSimpleLookup = false;

        // 两个要插值的颜色
        int rgb1, rgb2;

        // 对于每个区间（两个颜色之间的过渡）
        for (int i = 0; i < gradients.length; i++){
            // 为每个区间创建一个最大理论大小的数组
            gradients[i] = new int[GRADIENT_SIZE];

            // 获取两个颜色
            rgb1 = colors[i].getRGB();
            rgb2 = colors[i+1].getRGB();

            // 填充此数组，包含rgb1和rgb2之间的颜色
            interpolate(rgb1, rgb2, gradients[i]);

            // 如果颜色是不透明的，透明度仍应为0xff000000
            transparencyTest &= rgb1;
            transparencyTest &= rgb2;
        }

        // 如果插值在Linear RGB空间中进行，使用查找表将渐变转换回SRGB
        if (colorSpace == ColorSpaceType.LINEAR_RGB) {
            for (int j = 0; j < gradients.length; j++) {
                for (int i = 0; i < gradients[j].length; i++) {
                    gradients[j][i] =
                        convertEntireColorLinearRGBtoSRGB(gradients[j][i]);
                }
            }
        }
    }

    /**
     * 又一个辅助函数。此函数线性插值两个颜色之间的值，填充输出数组。
     *
     * @param rgb1 起始颜色
     * @param rgb2 结束颜色
     * @param output 输出颜色数组；不能为空
     */
    private void interpolate(int rgb1, int rgb2, int[] output) {
        // 颜色分量
        int a1, r1, g1, b1, da, dr, dg, db;

        // 插值值之间的步长
        float stepSize = 1.0f / output.length;

        // 从打包的整数中提取颜色分量
        a1 = (rgb1 >> 24) & 0xff;
        r1 = (rgb1 >> 16) & 0xff;
        g1 = (rgb1 >>  8) & 0xff;
        b1 = (rgb1      ) & 0xff;

        // 计算alpha、红色、绿色、蓝色的总变化量
        da = ((rgb2 >> 24) & 0xff) - a1;
        dr = ((rgb2 >> 16) & 0xff) - r1;
        dg = ((rgb2 >>  8) & 0xff) - g1;
        db = ((rgb2      ) & 0xff) - b1;

        // 对于区间中的每个步长，通过将归一化当前位置乘以总颜色变化来计算中间颜色
        // （加0.5是为了防止截断舍入误差）
        for (int i = 0; i < output.length; i++) {
            output[i] =
                (((int) ((a1 + i * da * stepSize) + 0.5) << 24)) |
                (((int) ((r1 + i * dr * stepSize) + 0.5) << 16)) |
                (((int) ((g1 + i * dg * stepSize) + 0.5) <<  8)) |
                (((int) ((b1 + i * db * stepSize) + 0.5)      ));
        }
    }

    /**
     * 又一个辅助函数。此函数提取整数RGB三元组的颜色分量，将它们从LinearRGB转换为SRGB，
     * 然后重新压缩成一个整数。
     */
    private int convertEntireColorLinearRGBtoSRGB(int rgb) {
        // 颜色分量
        int a1, r1, g1, b1;

        // 提取红色、绿色、蓝色分量
        a1 = (rgb >> 24) & 0xff;
        r1 = (rgb >> 16) & 0xff;
        g1 = (rgb >>  8) & 0xff;
        b1 = (rgb      ) & 0xff;

        // 使用查找表
        r1 = LinearRGBtoSRGB[r1];
        g1 = LinearRGBtoSRGB[g1];
        b1 = LinearRGBtoSRGB[b1];

        // 重新压缩分量
        return ((a1 << 24) |
                (r1 << 16) |
                (g1 <<  8) |
                (b1      ));
    }

    /**
     * 辅助函数，用于索引渐变数组。这是必要的，因为每个区间都有一个大小为255的颜色数组。
     * 然而，颜色区间不一定长度均匀，因此需要进行转换。
     *
     * @param position 未处理的位置，将被映射到0到1的范围内
     * @returns 要显示的整数颜色
     */
    protected final int indexIntoGradientsArrays(float position) {
        // 首先，根据循环方法调整位置值
        if (cycleMethod == CycleMethod.NO_CYCLE) {
            if (position > 1) {
                // 上限为1
                position = 1;
            } else if (position < 0) {
                // 下限为0
                position = 0;
            }
        } else if (cycleMethod == CycleMethod.REPEAT) {
            // 获取小数部分
            // （取模运算丢弃整数部分）
            position = position - (int)position;

            // 位置现在应在-1到1之间
            if (position < 0) {
                // 强制其在0-1范围内
                position = position + 1;
            }
        } else { // cycleMethod == CycleMethod.REFLECT
            if (position < 0) {
                // 取绝对值
                position = -position;
            }

            // 获取整数部分
            int part = (int)position;

            // 获取小数部分
            position = position - part;

            if ((part & 1) == 1) {
                // 整数部分为奇数，获取反射颜色
                position = 1 - position;
            }
        }

        // 现在，根据这个0-1的位置获取颜色...

        if (isSimpleLookup) {
            // 计算简单：只需按数组大小缩放索引
            return gradient[(int)(position * fastGradientArraySize)];
        } else {
            // 更复杂的计算，以节省空间

            // 对于所有渐变区间数组
            for (int i = 0; i < gradients.length; i++) {
                if (position < fractions[i+1]) {
                    // 这是我们要的数组
                    float delta = position - fractions[i];

                    // 这是我们要的区间
                    int index = (int)((delta / normalizedIntervals[i])
                                      * (GRADIENT_SIZE_INDEX));

                    return gradients[i][index];
                }
            }
        }

        return gradients[gradients.length - 1][GRADIENT_SIZE_INDEX];
    }

    /**
     * 辅助函数，用于将sRGB空间中的颜色分量转换为线性RGB空间。用于构建静态查找表。
     */
    private static int convertSRGBtoLinearRGB(int color) {
        float input, output;

        input = color / 255.0f;
        if (input <= 0.04045f) {
            output = input / 12.92f;
        } else {
            output = (float)Math.pow((input + 0.055) / 1.055, 2.4);
        }

        return Math.round(output * 255.0f);
    }

    /**
     * 辅助函数，用于将线性RGB空间中的颜色分量转换为sRGB空间。用于构建静态查找表。
     */
    private static int convertLinearRGBtoSRGB(int color) {
        float input, output;

        input = color/255.0f;
        if (input <= 0.0031308) {
            output = input * 12.92f;
        } else {
            output = (1.055f *
                ((float) Math.pow(input, (1.0 / 2.4)))) - 0.055f;
        }

        return Math.round(output * 255.0f);
    }

    /**
     * {@inheritDoc}
     */
    public final Raster getRaster(int x, int y, int w, int h) {
        // 如果工作光栅足够大，则重用它。否则，
        // 构建一个足够大的新光栅。
        Raster raster = saved;
        if (raster == null ||
            raster.getWidth() < w || raster.getHeight() < h)
        {
            raster = getCachedRaster(model, w, h);
            saved = raster;
        }

        // 访问光栅内部的int数组。因为我们使用的是DirectColorModel，
        // 我们知道DataBuffer是DataBufferInt类型，SampleModel是SinglePixelPackedSampleModel。
        // 调整DataBuffer中的初始偏移量以及扫描线步长。
        // 这些调用会使DataBuffer无法加速，但光栅从未长时间稳定到可以加速的程度...
        DataBufferInt rasterDB = (DataBufferInt)raster.getDataBuffer();
        int[] pixels = rasterDB.getData(0);
        int off = rasterDB.getOffset();
        int scanlineStride = ((SinglePixelPackedSampleModel)
                              raster.getSampleModel()).getScanlineStride();
        int adjust = scanlineStride - w;

        fillRaster(pixels, off, adjust, x, y, w, h); // 委托给子类

        return raster;
    }

    protected abstract void fillRaster(int pixels[], int off, int adjust,
                                       int x, int y, int w, int h);


    /**
     * 从GradientPaint中提取了此cacheRaster代码。它似乎会回收光栅以供任何其他实例使用，
     * 只要它们足够大。
     */
    private static synchronized Raster getCachedRaster(ColorModel cm,
                                                       int w, int h)
    {
        if (cm == cachedModel) {
            if (cached != null) {
                Raster ras = (Raster) cached.get();
                if (ras != null &&
                    ras.getWidth() >= w &&
                    ras.getHeight() >= h)
                {
                    cached = null;
                    return ras;
                }
            }
        }
        return cm.createCompatibleWritableRaster(w, h);
    }

    /**
     * 从GradientPaint中提取了此cacheRaster代码。它似乎会回收光栅以供任何其他实例使用，
     * 只要它们足够大。
     */
    private static synchronized void putCachedRaster(ColorModel cm,
                                                     Raster ras)
    {
        if (cached != null) {
            Raster cras = (Raster) cached.get();
            if (cras != null) {
                int cw = cras.getWidth();
                int ch = cras.getHeight();
                int iw = ras.getWidth();
                int ih = ras.getHeight();
                if (cw >= iw && ch >= ih) {
                    return;
                }
                if (cw * ch >= iw * ih) {
                    return;
                }
            }
        }
        cachedModel = cm;
        cached = new WeakReference<Raster>(ras);
    }

    /**
     * {@inheritDoc}
     */
    public final void dispose() {
        if (saved != null) {
            putCachedRaster(model, saved);
            saved = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public final ColorModel getColorModel() {
        return model;
    }
}
