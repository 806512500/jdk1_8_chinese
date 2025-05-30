
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

package java.awt.image;

import java.awt.color.ColorSpace;
import java.awt.Transparency;

/**
 * <code>DirectColorModel</code> 类是一个 <code>ColorModel</code> 类，用于处理表示 RGB
 * 颜色和 alpha 信息的像素值，这些信息作为单独的样本存储，并且所有样本都打包到一个 int、short 或 byte 中。
 * 该类只能与类型为 ColorSpace.TYPE_RGB 的 ColorSpace 一起使用。此外，对于 ColorSpace 的每个组件，
 * 通过 ColorSpace 的 <code>getMinValue()</code> 方法获得的最小归一化组件值必须为 0.0，通过
 * <code>getMaxValue()</code> 方法获得的最大值必须为 1.0（这些最小/最大值对于 RGB 空间是典型的）。
 * 像素值中必须有三个颜色样本，可以有一个 alpha 样本。对于使用 <code>transferType</code> 类型的原始数组
 * 表示像素值的方法，数组长度始终为一。支持的传输类型有 DataBuffer.TYPE_BYTE、
 * DataBuffer.TYPE_USHORT 和 DataBuffer.TYPE_INT。
 * 颜色和 alpha 样本存储在数组的单个元素中，由位掩码指示。每个位掩码必须是连续的，且掩码之间不能重叠。
 * 同样的掩码适用于其他方法使用的单个 int 像素表示。掩码与颜色/alpha 样本的对应关系如下：
 * <ul>
 * <li> 掩码通过从 0 到 2（如果不存在 alpha）或 3（如果存在 alpha）的索引标识。
 * <li> 前三个索引对应于颜色样本；索引 0 对应红色，索引 1 对应绿色，索引 2 对应蓝色。
 * <li> 索引 3 对应 alpha 样本（如果存在）。
 * </ul>
 * <p>
 * 将像素值转换为用于显示或处理目的的颜色/alpha 组件时，样本与组件之间是一对一的对应关系。
 * <code>DirectColorModel</code> 通常用于使用掩码定义打包样本的图像数据。例如，可以将
 * <code>DirectColorModel</code> 与 <code>SinglePixelPackedSampleModel</code> 结合使用来构建
 * {@link BufferedImage}。通常，{@link SampleModel} 和 <code>ColorModel</code> 使用的掩码是相同的。
 * 但是，如果它们不同，像素数据的颜色解释将根据 <code>ColorModel</code> 的掩码进行。
 * <p>
 * 单个 int 像素表示对于此类的所有对象都是有效的，因为总是可以将用于此类的像素值表示为单个 int。
 * 因此，使用此表示的方法不会因无效的像素值而抛出 <code>IllegalArgumentException</code>。
 * <p>
 * 此颜色模型类似于 X11 TrueColor 视觉。由
 * {@link ColorModel#getRGBdefault() getRGBdefault} 方法指定的默认 RGB ColorModel 是一个
 * <code>DirectColorModel</code>，具有以下参数：
 * <pre>
 * 位数：        32
 * 红色掩码：    0x00ff0000
 * 绿色掩码：    0x0000ff00
 * 蓝色掩码：    0x000000ff
 * Alpha 掩码：  0xff000000
 * 颜色空间：    sRGB
 * 是否预乘 alpha：False
 * 透明度：      Transparency.TRANSLUCENT
 * 传输类型：    DataBuffer.TYPE_INT
 * </pre>
 * <p>
 * 该类中的许多方法都是 final 的。这是因为底层的本机图形代码对这个类的布局和操作做出了假设，
 * 这些假设反映在标记为 final 的方法的实现中。您可以出于其他原因子类化这个类，但不能覆盖或修改这些方法的行为。
 *
 * @see ColorModel
 * @see ColorSpace
 * @see SinglePixelPackedSampleModel
 * @see BufferedImage
 * @see ColorModel#getRGBdefault
 *
 */
public class DirectColorModel extends PackedColorModel {
    private int red_mask;
    private int green_mask;
    private int blue_mask;
    private int alpha_mask;
    private int red_offset;
    private int green_offset;
    private int blue_offset;
    private int alpha_offset;
    private int red_scale;
    private int green_scale;
    private int blue_scale;
    private int alpha_scale;
    private boolean is_LinearRGB;
    private int lRGBprecision;
    private byte[] tosRGB8LUT;
    private byte[] fromsRGB8LUT8;
    private short[] fromsRGB8LUT16;

    /**
     * 从指定的掩码构造一个 <code>DirectColorModel</code>，这些掩码指示 <code>int</code> 像素表示中
     * 哪些位包含红色、绿色和蓝色颜色样本。由于像素值不包含 alpha 信息，所有像素都被视为不透明，即 alpha = 1.0。
     * 每个掩码中的所有位必须是连续的，并且适合指定数量的 <code>int</code> 像素表示的最低有效位。
     * <code>ColorSpace</code> 是默认的 sRGB 空间。透明度值为 Transparency.OPAQUE。传输类型
     * 是可以容纳单个像素的 DataBuffer.TYPE_BYTE、DataBuffer.TYPE_USHORT 或 DataBuffer.TYPE_INT 中最小的一个。
     * @param bits 像素值中的位数；例如，掩码中的位数之和。
     * @param rmask 指示整数像素中红色组件的位的掩码
     * @param gmask 指示整数像素中绿色组件的位的掩码
     * @param bmask 指示整数像素中蓝色组件的位的掩码
     *
     */
    public DirectColorModel(int bits,
                            int rmask, int gmask, int bmask) {
        this(bits, rmask, gmask, bmask, 0);
    }

    /**
     * 从指定的掩码构造一个 <code>DirectColorModel</code>，这些掩码指示 <code>int</code> 像素表示中
     * 哪些位包含红色、绿色和蓝色颜色样本以及 alpha 样本（如果存在）。如果 <code>amask</code> 为 0，
     * 像素值不包含 alpha 信息，所有像素都被视为不透明，即 alpha = 1.0。每个掩码中的所有位必须是连续的，
     * 并且适合指定数量的 <code>int</code> 像素表示的最低有效位。如果存在 alpha，它不会被预乘。
     * <code>ColorSpace</code> 是默认的 sRGB 空间。透明度值为 Transparency.OPAQUE（如果不存在 alpha）或
     * Transparency.TRANSLUCENT（如果存在 alpha）。传输类型是可以容纳单个像素的 DataBuffer.TYPE_BYTE、
     * DataBuffer.TYPE_USHORT 或 DataBuffer.TYPE_INT 中最小的一个。
     * @param bits 像素值中的位数；例如，掩码中的位数之和。
     * @param rmask 指示整数像素中红色组件的位的掩码
     * @param gmask 指示整数像素中绿色组件的位的掩码
     * @param bmask 指示整数像素中蓝色组件的位的掩码
     * @param amask 指示整数像素中 alpha 组件的位的掩码
     */
    public DirectColorModel(int bits, int rmask, int gmask,
                            int bmask, int amask) {
        super (ColorSpace.getInstance(ColorSpace.CS_sRGB),
               bits, rmask, gmask, bmask, amask, false,
               amask == 0 ? Transparency.OPAQUE : Transparency.TRANSLUCENT,
               ColorModel.getDefaultTransferType(bits));
        setFields();
    }

    /**
     * 从指定的参数构造一个 <code>DirectColorModel</code>。颜色组件在指定的
     * <code>ColorSpace</code> 中，必须是类型 ColorSpace.TYPE_RGB，并且所有最小归一化组件值都为 0.0，
     * 所有最大值都为 1.0。掩码指定 <code>int</code> 像素表示中哪些位包含红色、绿色和蓝色颜色样本以及
     * alpha 样本（如果存在）。如果 <code>amask</code> 为 0，像素值不包含 alpha 信息，所有像素都被视为不透明，
     * 即 alpha = 1.0。每个掩码中的所有位必须是连续的，并且适合指定数量的 <code>int</code> 像素表示的最低有效位。
     * 如果存在 alpha，<code>boolean</code> <code>isAlphaPremultiplied</code> 指定如何解释
     * 像素值中的颜色和 alpha 样本。如果 <code>boolean</code> 为 <code>true</code>，则假设颜色样本已被 alpha 样本乘以。
     * 透明度值为 Transparency.OPAQUE（如果不存在 alpha）或 Transparency.TRANSLUCENT（如果存在 alpha）。
     * 传输类型是用于表示像素值的原始数组类型，必须是 DataBuffer.TYPE_BYTE、DataBuffer.TYPE_USHORT 或
     * DataBuffer.TYPE_INT 中的一个。
     * @param space 指定的 <code>ColorSpace</code>
     * @param bits 像素值中的位数；例如，掩码中的位数之和。
     * @param rmask 指示整数像素中红色组件的位的掩码
     * @param gmask 指示整数像素中绿色组件的位的掩码
     * @param bmask 指示整数像素中蓝色组件的位的掩码
     * @param amask 指示整数像素中 alpha 组件的位的掩码
     * @param isAlphaPremultiplied 如果颜色样本已被 alpha 样本乘以，则为 <code>true</code>；否则为 <code>false</code>
     * @param transferType 用于表示像素值的数组类型
     * @throws IllegalArgumentException 如果 <code>space</code> 不是 TYPE_RGB 空间或最小/最大归一化组件值不是 0.0/1.0。
     */
    public DirectColorModel(ColorSpace space, int bits, int rmask,
                            int gmask, int bmask, int amask,
                            boolean isAlphaPremultiplied,
                            int transferType) {
        super (space, bits, rmask, gmask, bmask, amask,
               isAlphaPremultiplied,
               amask == 0 ? Transparency.OPAQUE : Transparency.TRANSLUCENT,
               transferType);
        if (ColorModel.isLinearRGBspace(colorSpace)) {
            is_LinearRGB = true;
            if (maxBits <= 8) {
                lRGBprecision = 8;
                tosRGB8LUT = ColorModel.getLinearRGB8TosRGB8LUT();
                fromsRGB8LUT8 = ColorModel.getsRGB8ToLinearRGB8LUT();
            } else {
                lRGBprecision = 16;
                tosRGB8LUT = ColorModel.getLinearRGB16TosRGB8LUT();
                fromsRGB8LUT16 = ColorModel.getsRGB8ToLinearRGB16LUT();
            }
        } else if (!is_sRGB) {
            for (int i = 0; i < 3; i++) {
                // super 构造函数检查 space 是否为 TYPE_RGB
                // 在这里检查最小/最大值是否都为 0.0/1.0
                if ((space.getMinValue(i) != 0.0f) ||
                    (space.getMaxValue(i) != 1.0f)) {
                    throw new IllegalArgumentException(
                        "非法的最小/最大 RGB 组件值");
                }
            }
        }
        setFields();
    }

    /**
     * 返回一个掩码，指示 <code>int</code> 像素表示中哪些位包含红色颜色组件。
     * @return 掩码，指示 <code>int</code> 像素表示中哪些位包含红色颜色样本。
     */
    final public int getRedMask() {
        return maskArray[0];
    }

    /**
     * 返回一个掩码，指示 <code>int</code> 像素表示中哪些位包含绿色颜色组件。
     * @return 掩码，指示 <code>int</code> 像素表示中哪些位包含绿色颜色样本。
     */
    final public int getGreenMask() {
        return maskArray[1];
    }

    /**
     * 返回一个掩码，指示 <code>int</code> 像素表示中哪些位包含蓝色颜色组件。
     * @return 掩码，指示 <code>int</code> 像素表示中哪些位包含蓝色颜色样本。
     */
    final public int getBlueMask() {
        return maskArray[2];
    }

    /**
     * 返回一个掩码，指示 <code>int</code> 像素表示中哪些位包含 alpha 组件。
     * @return 掩码，指示 <code>int</code> 像素表示中哪些位包含 alpha 样本。
     */
    final public int getAlphaMask() {
        if (supportsAlpha) {
            return maskArray[3];
        } else {
            return 0;
        }
    }


    /*
     * 给定一个此 ColorModel 的 ColorSpace 中的 int 像素，将其转换为默认的 sRGB ColorSpace 并返回 R、G 和 B
     * 组件作为 0.0 到 1.0 之间的浮点值。
     */
    private float[] getDefaultRGBComponents(int pixel) {
        int components[] = getComponents(pixel, null, 0);
        float norm[] = getNormalizedComponents(components, 0, null, 0);
        // 注意，getNormalizedComponents 返回非预乘值
        return colorSpace.toRGB(norm);
    }


    private int getsRGBComponentFromsRGB(int pixel, int idx) {
        int c = ((pixel & maskArray[idx]) >>> maskOffsets[idx]);
        if (isAlphaPremultiplied) {
            int a = ((pixel & maskArray[3]) >>> maskOffsets[3]);
            c = (a == 0) ? 0 :
                         (int) (((c * scaleFactors[idx]) * 255.0f /
                                 (a * scaleFactors[3])) + 0.5f);
        } else if (scaleFactors[idx] != 1.0f) {
            c = (int) ((c * scaleFactors[idx]) + 0.5f);
        }
        return c;
    }


    private int getsRGBComponentFromLinearRGB(int pixel, int idx) {
        int c = ((pixel & maskArray[idx]) >>> maskOffsets[idx]);
        if (isAlphaPremultiplied) {
            float factor = (float) ((1 << lRGBprecision) - 1);
            int a = ((pixel & maskArray[3]) >>> maskOffsets[3]);
            c = (a == 0) ? 0 :
                         (int) (((c * scaleFactors[idx]) * factor /
                                 (a * scaleFactors[3])) + 0.5f);
        } else if (nBits[idx] != lRGBprecision) {
            if (lRGBprecision == 16) {
                c = (int) ((c * scaleFactors[idx] * 257.0f) + 0.5f);
            } else {
                c = (int) ((c * scaleFactors[idx]) + 0.5f);
            }
        }
        // 现在 c 的范围是 0-255 或 0-65535，具体取决于 lRGBprecision
        return tosRGB8LUT[c] & 0xff;
    }


    /**
     * 返回指定像素的红色分量，范围从 0 到 255，默认 RGB <code>ColorSpace</code> 为 sRGB。如果需要，会进行颜色转换。像素值以 <code>int</code> 形式指定。
     * 返回的值是非预乘值。因此，如果 alpha 预乘，此方法会在返回值之前将其除以 alpha。例如，如果 alpha 值为 0，则红色值为 0。
     * @param pixel 指定的像素
     * @return 指定像素的红色分量，范围从 0 到 255，在 sRGB <code>ColorSpace</code> 中。
     */
    final public int getRed(int pixel) {
        if (is_sRGB) {
            return getsRGBComponentFromsRGB(pixel, 0);
        } else if (is_LinearRGB) {
            return getsRGBComponentFromLinearRGB(pixel, 0);
        }
        float rgb[] = getDefaultRGBComponents(pixel);
        return (int) (rgb[0] * 255.0f + 0.5f);
    }

    /**
     * 返回指定像素的绿色分量，范围从 0 到 255，默认 RGB <code>ColorSpace</code> 为 sRGB。如果需要，会进行颜色转换。像素值以 <code>int</code> 形式指定。
     * 返回的值是非预乘值。因此，如果 alpha 预乘，此方法会在返回值之前将其除以 alpha。例如，如果 alpha 值为 0，则绿色值为 0。
     * @param pixel 指定的像素
     * @return 指定像素的绿色分量，范围从 0 到 255，在 sRGB <code>ColorSpace</code> 中。
     */
    final public int getGreen(int pixel) {
        if (is_sRGB) {
            return getsRGBComponentFromsRGB(pixel, 1);
        } else if (is_LinearRGB) {
            return getsRGBComponentFromLinearRGB(pixel, 1);
        }
        float rgb[] = getDefaultRGBComponents(pixel);
        return (int) (rgb[1] * 255.0f + 0.5f);
    }

    /**
     * 返回指定像素的蓝色分量，范围从 0 到 255，默认 RGB <code>ColorSpace</code> 为 sRGB。如果需要，会进行颜色转换。像素值以 <code>int</code> 形式指定。
     * 返回的值是非预乘值。因此，如果 alpha 预乘，此方法会在返回值之前将其除以 alpha。例如，如果 alpha 值为 0，则蓝色值为 0。
     * @param pixel 指定的像素
     * @return 指定像素的蓝色分量，范围从 0 到 255，在 sRGB <code>ColorSpace</code> 中。
     */
    final public int getBlue(int pixel) {
        if (is_sRGB) {
            return getsRGBComponentFromsRGB(pixel, 2);
        } else if (is_LinearRGB) {
            return getsRGBComponentFromLinearRGB(pixel, 2);
        }
        float rgb[] = getDefaultRGBComponents(pixel);
        return (int) (rgb[2] * 255.0f + 0.5f);
    }

    /**
     * 返回指定像素的 alpha 分量，范围从 0 到 255。像素值以 <code>int</code> 形式指定。
     * @param pixel 指定的像素
     * @return 指定像素的 alpha 分量，范围从 0 到 255。
     */
    final public int getAlpha(int pixel) {
        if (!supportsAlpha) return 255;
        int a = ((pixel & maskArray[3]) >>> maskOffsets[3]);
        if (scaleFactors[3] != 1.0f) {
            a = (int)(a * scaleFactors[3] + 0.5f);
        }
        return a;
    }

    /**
     * 返回像素的默认 RGB 颜色模型格式的颜色/alpha 分量。如果需要，会进行颜色转换。像素值以 <code>int</code> 形式指定。
     * 返回的值是非预乘格式。因此，如果 alpha 预乘，此方法会在返回值之前将其除以 alpha。例如，如果 alpha 值为 0，则颜色值均为 0。
     * @param pixel 指定的像素
     * @return 指定像素的颜色/alpha 分量的 RGB 值。
     * @see ColorModel#getRGBdefault
     */
    final public int getRGB(int pixel) {
        if (is_sRGB || is_LinearRGB) {
            return (getAlpha(pixel) << 24)
                | (getRed(pixel) << 16)
                | (getGreen(pixel) << 8)
                | (getBlue(pixel) << 0);
        }
        float rgb[] = getDefaultRGBComponents(pixel);
        return (getAlpha(pixel) << 24)
            | (((int) (rgb[0] * 255.0f + 0.5f)) << 16)
            | (((int) (rgb[1] * 255.0f + 0.5f)) << 8)
            | (((int) (rgb[2] * 255.0f + 0.5f)) << 0);
    }

    /**
     * 返回指定像素的红色分量，范围从 0 到 255，默认 RGB <code>ColorSpace</code> 为 sRGB。如果需要，会进行颜色转换。像素值由作为对象引用传递的 <code>transferType</code> 类型的数据元素数组指定。
     * 返回的值是非预乘值。因此，如果 alpha 预乘，此方法会在返回值之前将其除以 alpha。例如，如果 alpha 值为 0，则红色值为 0。
     * 如果 <code>inData</code> 不是 <code>transferType</code> 类型的原始数组，则抛出 <code>ClassCastException</code>。如果 <code>inData</code> 不足以容纳此 <code>ColorModel</code> 的像素值，则抛出 <code>ArrayIndexOutOfBoundsException</code>。由于 <code>DirectColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不重写该方法并且使用不受支持的 <code>transferType</code>，则会抛出异常。
     * 如果此 <code>transferType</code> 不受支持，则抛出 <code>UnsupportedOperationException</code>。
     * @param inData 包含像素值的数组
     * @return 指定像素的红色分量值。
     * @throws ArrayIndexOutOfBoundsException 如果 <code>inData</code> 不足以容纳此颜色模型的像素值
     * @throws ClassCastException 如果 <code>inData</code> 不是 <code>transferType</code> 类型的原始数组
     * @throws UnsupportedOperationException 如果此颜色模型不支持此 <code>transferType</code>
     */
    public int getRed(Object inData) {
        int pixel=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[0] & 0xff;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[0] & 0xffff;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[0];
            break;
            default:
               throw new UnsupportedOperationException("此方法尚未实现 transferType " + transferType);
        }
        return getRed(pixel);
    }


    /**
     * 返回指定像素的绿色分量，范围从 0 到 255，默认 RGB <code>ColorSpace</code> 为 sRGB。如果需要，会进行颜色转换。像素值由作为对象引用传递的 <code>transferType</code> 类型的数据元素数组指定。
     * 返回的值是非预乘值。因此，如果 alpha 预乘，此方法会在返回值之前将其除以 alpha。例如，如果 alpha 值为 0，则绿色值为 0。如果 <code>inData</code> 不是 <code>transferType</code> 类型的原始数组，则抛出 <code>ClassCastException</code>。如果 <code>inData</code> 不足以容纳此 <code>ColorModel</code> 的像素值，则抛出 <code>ArrayIndexOutOfBoundsException</code>。由于 <code>DirectColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不重写该方法并且使用不受支持的 <code>transferType</code>，则会抛出异常。
     * 如果此 <code>transferType</code> 不受支持，则抛出 <code>UnsupportedOperationException</code>。
     * @param inData 包含像素值的数组
     * @return 指定像素的绿色分量值。
     * @throws ArrayIndexOutOfBoundsException 如果 <code>inData</code> 不足以容纳此颜色模型的像素值
     * @throws ClassCastException 如果 <code>inData</code> 不是 <code>transferType</code> 类型的原始数组
     * @throws UnsupportedOperationException 如果此颜色模型不支持此 <code>transferType</code>
     */
    public int getGreen(Object inData) {
        int pixel=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[0] & 0xff;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[0] & 0xffff;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[0];
            break;
            default:
               throw new UnsupportedOperationException("此方法尚未实现 transferType " + transferType);
        }
        return getGreen(pixel);
    }


    /**
     * 返回指定像素的蓝色分量，范围从 0 到 255，默认 RGB <code>ColorSpace</code> 为 sRGB。如果需要，会进行颜色转换。像素值由作为对象引用传递的 <code>transferType</code> 类型的数据元素数组指定。
     * 返回的值是非预乘值。因此，如果 alpha 预乘，此方法会在返回值之前将其除以 alpha。例如，如果 alpha 值为 0，则蓝色值为 0。如果 <code>inData</code> 不是 <code>transferType</code> 类型的原始数组，则抛出 <code>ClassCastException</code>。如果 <code>inData</code> 不足以容纳此 <code>ColorModel</code> 的像素值，则抛出 <code>ArrayIndexOutOfBoundsException</code>。由于 <code>DirectColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不重写该方法并且使用不受支持的 <code>transferType</code>，则会抛出异常。
     * 如果此 <code>transferType</code> 不受支持，则抛出 <code>UnsupportedOperationException</code>。
     * @param inData 包含像素值的数组
     * @return 指定像素的蓝色分量值。
     * @throws ArrayIndexOutOfBoundsException 如果 <code>inData</code> 不足以容纳此颜色模型的像素值
     * @throws ClassCastException 如果 <code>inData</code> 不是 <code>transferType</code> 类型的原始数组
     * @throws UnsupportedOperationException 如果此颜色模型不支持此 <code>transferType</code>
     */
    public int getBlue(Object inData) {
        int pixel=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[0] & 0xff;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[0] & 0xffff;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[0];
            break;
            default:
               throw new UnsupportedOperationException("此方法尚未实现 transferType " + transferType);
        }
        return getBlue(pixel);
    }

    /**
     * 返回指定像素的 alpha 分量，范围从 0 到 255。像素值由作为对象引用传递的 <code>transferType</code> 类型的数据元素数组指定。
     * 如果 <code>inData</code> 不是 <code>transferType</code> 类型的原始数组，则抛出 <code>ClassCastException</code>。如果 <code>inData</code> 不足以容纳此 <code>ColorModel</code> 的像素值，则抛出 <code>ArrayIndexOutOfBoundsException</code>。由于 <code>DirectColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不重写该方法并且使用不受支持的 <code>transferType</code>，则会抛出异常。
     * 如果此 <code>transferType</code> 不受支持，则抛出 <code>UnsupportedOperationException</code>。
     * @param inData 指定的像素
     * @return 指定像素的 alpha 分量，范围从 0 到 255。
     * @exception ClassCastException 如果 <code>inData</code> 不是 <code>transferType</code> 类型的原始数组
     * @exception ArrayIndexOutOfBoundsException 如果 <code>inData</code> 不足以容纳此 <code>ColorModel</code> 的像素值
     * @exception UnsupportedOperationException 如果此 <code>transferType</code> 不受支持
     */
    public int getAlpha(Object inData) {
        int pixel=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[0] & 0xff;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[0] & 0xffff;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[0];
            break;
            default:
               throw new UnsupportedOperationException("此方法尚未实现 transferType " + transferType);
        }
        return getAlpha(pixel);
    }


                /**
     * 返回指定像素的默认 RGB 颜色模型格式的颜色/alpha 组件。如果需要，会进行颜色转换。像素值由作为对象引用传递的类型为 <code>transferType</code> 的数据元素数组指定。如果 <code>inData</code> 不是类型为 <code>transferType</code> 的原始数组，则抛出 <code>ClassCastException</code>。如果 <code>inData</code> 不足以保存此 <code>ColorModel</code> 的像素值，则抛出 <code>ArrayIndexOutOfBoundsException</code>。返回的值为非预乘格式。因此，如果 alpha 已经预乘，则此方法会将其从颜色组件中除掉。如果 alpha 值为 0，例如，颜色值为 0。由于 <code>DirectColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不覆盖它，则如果它们使用不受支持的 <code>transferType</code>，则会抛出异常。
     *
     * @param inData 指定的像素
     * @return 指定像素的颜色和 alpha 组件。
     * @exception UnsupportedOperationException 如果此 <code>transferType</code> 不被此 <code>ColorModel</code> 支持
     * @see ColorModel#getRGBdefault
     */
    public int getRGB(Object inData) {
        int pixel=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[0] & 0xff;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[0] & 0xffff;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[0];
            break;
            default:
               throw new UnsupportedOperationException("This method has not been "+
                   "implemented for transferType " + transferType);
        }
        return getRGB(pixel);
    }

    /**
     * 返回此 <code>ColorModel</code> 中给定默认 RGB 颜色模型的整数像素表示的数据元素数组表示。此数组可以传递给 <code>WritableRaster</code> 对象的 <code>setDataElements</code> 方法。如果 <code>pixel</code> 变量为 <code>null</code>，则分配新数组。如果 <code>pixel</code> 不为 <code>null</code>，则它必须是类型为 <code>transferType</code> 的原始数组；否则，抛出 <code>ClassCastException</code>。如果 <code>pixel</code> 不足以保存此 <code>ColorModel</code> 的像素值，则抛出 <code>ArrayIndexOutOfBoundsException</code>。返回像素数组。由于 <code>DirectColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不覆盖它，则如果它们使用不受支持的 <code>transferType</code>，则会抛出异常。
     *
     * @param rgb 默认 RGB 颜色模型中的整数像素表示
     * @param pixel 指定的像素
     * @return 此 <code>ColorModel</code> 中指定像素的数组表示
     * @exception ClassCastException 如果 <code>pixel</code>
     *  不是类型为 <code>transferType</code> 的原始数组
     * @exception ArrayIndexOutOfBoundsException 如果
     *  <code>pixel</code> 不足以保存此 <code>ColorModel</code> 的像素值
     * @exception UnsupportedOperationException 如果此
     *  <code>transferType</code> 不被此 <code>ColorModel</code> 支持
     * @see WritableRaster#setDataElements
     * @see SampleModel#setDataElements
     */
    public Object getDataElements(int rgb, Object pixel) {
        //REMIND: maybe more efficient not to use int array for
        //DataBuffer.TYPE_USHORT and DataBuffer.TYPE_INT
        int intpixel[] = null;
        if (transferType == DataBuffer.TYPE_INT &&
            pixel != null) {
            intpixel = (int[])pixel;
            intpixel[0] = 0;
        } else {
            intpixel = new int[1];
        }

        ColorModel defaultCM = ColorModel.getRGBdefault();
        if (this == defaultCM || equals(defaultCM)) {
            intpixel[0] = rgb;
            return intpixel;
        }

        int red, grn, blu, alp;
        red = (rgb>>16) & 0xff;
        grn = (rgb>>8) & 0xff;
        blu = rgb & 0xff;
        if (is_sRGB || is_LinearRGB) {
            int precision;
            float factor;
            if (is_LinearRGB) {
                if (lRGBprecision == 8) {
                    red = fromsRGB8LUT8[red] & 0xff;
                    grn = fromsRGB8LUT8[grn] & 0xff;
                    blu = fromsRGB8LUT8[blu] & 0xff;
                    precision = 8;
                    factor = 1.0f / 255.0f;
                } else {
                    red = fromsRGB8LUT16[red] & 0xffff;
                    grn = fromsRGB8LUT16[grn] & 0xffff;
                    blu = fromsRGB8LUT16[blu] & 0xffff;
                    precision = 16;
                    factor = 1.0f / 65535.0f;
                }
            } else {
                precision = 8;
                factor = 1.0f / 255.0f;
            }
            if (supportsAlpha) {
                alp = (rgb>>24) & 0xff;
                if (isAlphaPremultiplied) {
                    factor *= (alp * (1.0f / 255.0f));
                    precision = -1;  // 强制组件计算
                }
                if (nBits[3] != 8) {
                    alp = (int)
                        ((alp * (1.0f / 255.0f) * ((1<<nBits[3]) - 1)) + 0.5f);
                    if (alp > ((1<<nBits[3]) - 1)) {
                        // 修复 4412670 - 参见下面的注释
                        alp = (1<<nBits[3]) - 1;
                    }
                }
                intpixel[0] = alp << maskOffsets[3];
            }
            if (nBits[0] != precision) {
                red = (int) ((red * factor * ((1<<nBits[0]) - 1)) + 0.5f);
            }
            if (nBits[1] != precision) {
                grn = (int) ((grn * factor * ((1<<nBits[1]) - 1)) + 0.5f);
            }
            if (nBits[2] != precision) {
                blu = (int) ((blu * factor * ((1<<nBits[2]) - 1)) + 0.5f);
            }
        } else {
            // 需要转换颜色
            float[] norm = new float[3];
            float factor = 1.0f / 255.0f;
            norm[0] = red * factor;
            norm[1] = grn * factor;
            norm[2] = blu * factor;
            norm = colorSpace.fromRGB(norm);
            if (supportsAlpha) {
                alp = (rgb>>24) & 0xff;
                if (isAlphaPremultiplied) {
                    factor *= alp;
                    for (int i = 0; i < 3; i++) {
                        norm[i] *= factor;
                    }
                }
                if (nBits[3] != 8) {
                    alp = (int)
                        ((alp * (1.0f / 255.0f) * ((1<<nBits[3]) - 1)) + 0.5f);
                    if (alp > ((1<<nBits[3]) - 1)) {
                        // 修复 4412670 - 参见下面的注释
                        alp = (1<<nBits[3]) - 1;
                    }
                }
                intpixel[0] = alp << maskOffsets[3];
            }
            red = (int) ((norm[0] * ((1<<nBits[0]) - 1)) + 0.5f);
            grn = (int) ((norm[1] * ((1<<nBits[1]) - 1)) + 0.5f);
            blu = (int) ((norm[2] * ((1<<nBits[2]) - 1)) + 0.5f);
        }

        if (maxBits > 23) {
            // 修复 4412670 - 对于 24 位或更多位的组件
            // 上面使用浮点精度进行的一些计算
            // 可能会丢失足够的精度，导致整数结果
            // 溢出 nBits，因此我们需要进行裁剪。
            if (red > ((1<<nBits[0]) - 1)) {
                red = (1<<nBits[0]) - 1;
            }
            if (grn > ((1<<nBits[1]) - 1)) {
                grn = (1<<nBits[1]) - 1;
            }
            if (blu > ((1<<nBits[2]) - 1)) {
                blu = (1<<nBits[2]) - 1;
            }
        }

        intpixel[0] |= (red << maskOffsets[0]) |
                       (grn << maskOffsets[1]) |
                       (blu << maskOffsets[2]);

        switch (transferType) {
            case DataBuffer.TYPE_BYTE: {
               byte bdata[];
               if (pixel == null) {
                   bdata = new byte[1];
               } else {
                   bdata = (byte[])pixel;
               }
               bdata[0] = (byte)(0xff&intpixel[0]);
               return bdata;
            }
            case DataBuffer.TYPE_USHORT:{
               short sdata[];
               if (pixel == null) {
                   sdata = new short[1];
               } else {
                   sdata = (short[])pixel;
               }
               sdata[0] = (short)(intpixel[0]&0xffff);
               return sdata;
            }
            case DataBuffer.TYPE_INT:
               return intpixel;
        }
        throw new UnsupportedOperationException("This method has not been "+
                 "implemented for transferType " + transferType);

    }

    /**
     * 返回此 <code>ColorModel</code> 中给定像素的非归一化颜色/alpha 组件。像素值指定为 <code>int</code>。如果 <code>components</code> 数组为 <code>null</code>，则分配新数组。返回 <code>components</code> 数组。颜色/alpha 组件存储在 <code>components</code> 数组中，从 <code>offset</code> 开始，即使数组是由此方法分配的。如果 <code>components</code> 数组不为 <code>null</code> 且不足以保存从 <code>offset</code> 开始的所有颜色和 alpha 组件，则抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param pixel 指定的像素
     * @param components 接收指定像素的颜色和 alpha 组件的数组
     * @param offset 在 <code>components</code> 数组中开始存储颜色和 alpha 组件的位置
     * @return 从指定偏移开始包含指定像素的颜色和 alpha 组件的数组。
     */
    final public int[] getComponents(int pixel, int[] components, int offset) {
        if (components == null) {
            components = new int[offset+numComponents];
        }

        for (int i=0; i < numComponents; i++) {
            components[offset+i] = (pixel & maskArray[i]) >>> maskOffsets[i];
        }

        return components;
    }

    /**
     * 返回此 <code>ColorModel</code> 中给定像素的非归一化颜色/alpha 组件。像素值由作为对象引用传递的类型为 <code>transferType</code> 的数据元素数组指定。如果 <code>pixel</code> 不是类型为 <code>transferType</code> 的原始数组，则抛出 <code>ClassCastException</code>。如果 <code>pixel</code> 不足以保存此 <code>ColorModel</code> 的像素值，则抛出 <code>ArrayIndexOutOfBoundsException</code>。如果 <code>components</code> 数组为 <code>null</code>，则分配新数组。返回 <code>components</code> 数组。颜色/alpha 组件存储在 <code>components</code> 数组中，从 <code>offset</code> 开始，即使数组是由此方法分配的。如果 <code>components</code> 数组不为 <code>null</code> 且不足以保存从 <code>offset</code> 开始的所有颜色和 alpha 组件，则抛出 <code>ArrayIndexOutOfBoundsException</code>。由于 <code>DirectColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不覆盖它，则如果它们使用不受支持的 <code>transferType</code>，则会抛出异常。
     * @param pixel 指定的像素
     * @param components 接收指定像素的颜色和 alpha 组件的数组
     * @param offset 在 <code>components</code> 数组中开始存储颜色和 alpha 组件的位置
     * @return 从指定偏移开始包含指定像素的颜色和 alpha 组件的数组。
     * @exception ClassCastException 如果 <code>pixel</code>
     *  不是类型为 <code>transferType</code> 的原始数组
     * @exception ArrayIndexOutOfBoundsException 如果
     *  <code>pixel</code> 不足以保存此 <code>ColorModel</code> 的像素值，或者 <code>components</code>
     *  不为 <code>null</code> 且不足以保存从 <code>offset</code> 开始的所有颜色和 alpha 组件
     * @exception UnsupportedOperationException 如果此
     *            <code>transferType</code> 不被此颜色模型支持
     */
    final public int[] getComponents(Object pixel, int[] components,
                                     int offset) {
        int intpixel=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])pixel;
               intpixel = bdata[0] & 0xff;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])pixel;
               intpixel = sdata[0] & 0xffff;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])pixel;
               intpixel = idata[0];
            break;
            default:
               throw new UnsupportedOperationException("This method has not been "+
                   "implemented for transferType " + transferType);
        }
        return getComponents(intpixel, components, offset);
    }

    /**
     * 创建具有指定宽度和高度且数据布局（<code>SampleModel</code>）与此 <code>ColorModel</code> 兼容的 <code>WritableRaster</code>。
     * @param w 应用于新 <code>WritableRaster</code> 的宽度
     * @param h 应用于新 <code>WritableRaster</code> 的高度
     * @return 具有指定宽度和高度的 <code>WritableRaster</code> 对象。
     * @throws IllegalArgumentException 如果 <code>w</code> 或 <code>h</code>
     *         小于或等于零
     * @see WritableRaster
     * @see SampleModel
     */
    final public WritableRaster createCompatibleWritableRaster (int w,
                                                                int h) {
        if ((w <= 0) || (h <= 0)) {
            throw new IllegalArgumentException("Width (" + w + ") and height (" + h +
                                               ") cannot be <= 0");
        }
        int[] bandmasks;
        if (supportsAlpha) {
            bandmasks = new int[4];
            bandmasks[3] = alpha_mask;
        }
        else {
            bandmasks = new int[3];
        }
        bandmasks[0] = red_mask;
        bandmasks[1] = green_mask;
        bandmasks[2] = blue_mask;


                    if (pixel_bits > 16) {
            return Raster.createPackedRaster(DataBuffer.TYPE_INT,
                                             w,h,bandmasks,null);
        }
        else if (pixel_bits > 8) {
            return Raster.createPackedRaster(DataBuffer.TYPE_USHORT,
                                             w,h,bandmasks,null);
        }
        else {
            return Raster.createPackedRaster(DataBuffer.TYPE_BYTE,
                                             w,h,bandmasks,null);
        }
    }

    /**
     * 返回一个表示此 <code>ColorModel</code> 中像素值的 <code>int</code>，给定一个未归一化的颜色/透明度分量数组。
     * 如果 <code>components</code> 数组从 <code>offset</code> 开始不足以容纳所有颜色和透明度分量，则抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * @param components 一个未归一化的颜色和透明度分量数组
     * @param offset 从 <code>components</code> 中开始检索颜色和透明度分量的索引
     * @return 一个表示此 <code>ColorModel</code> 中指定分量的 <code>int</code> 像素值。
     * @exception ArrayIndexOutOfBoundsException 如果 <code>components</code> 数组从 <code>offset</code> 开始不足以容纳所有颜色和透明度分量
     */
    public int getDataElement(int[] components, int offset) {
        int pixel = 0;
        for (int i=0; i < numComponents; i++) {
            pixel |= ((components[offset+i]<<maskOffsets[i])&maskArray[i]);
        }
        return pixel;
    }

    /**
     * 返回此 <code>ColorModel</code> 中像素的数据元素数组表示，给定一个未归一化的颜色/透明度分量数组。
     * 此数组可以传递给 <code>WritableRaster</code> 对象的 <code>setDataElements</code> 方法。
     * 如果 <code>components</code> 数组从 <code>offset</code> 开始不足以容纳所有颜色和透明度分量，则抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * 如果 <code>obj</code> 变量为 <code>null</code>，则分配一个新数组。如果 <code>obj</code> 不为 <code>null</code>，则它必须是类型为 <code>transferType</code> 的原始数组；否则，抛出 <code>ClassCastException</code>。
     * 如果 <code>obj</code> 不足以容纳此 <code>ColorModel</code> 的像素值，则抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * 由于 <code>DirectColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不重写此方法且使用不受支持的 <code>transferType</code>，则抛出异常。
     * @param components 一个未归一化的颜色和透明度分量数组
     * @param offset 从 <code>components</code> 中开始检索颜色和透明度分量的索引
     * @param obj 表示颜色和透明度分量数组的 <code>Object</code>
     * @return 表示颜色和透明度分量数组的 <code>Object</code>
     * @exception ClassCastException 如果 <code>obj</code> 不是类型为 <code>transferType</code> 的原始数组
     * @exception ArrayIndexOutOfBoundsException 如果 <code>obj</code> 不足以容纳此 <code>ColorModel</code> 的像素值，或 <code>components</code> 数组从 <code>offset</code> 开始不足以容纳所有颜色和透明度分量
     * @exception UnsupportedOperationException 如果此 <code>ColorModel</code> 不支持此 <code>transferType</code>
     * @see WritableRaster#setDataElements
     * @see SampleModel#setDataElements
     */
    public Object getDataElements(int[] components, int offset, Object obj) {
        int pixel = 0;
        for (int i=0; i < numComponents; i++) {
            pixel |= ((components[offset+i]<<maskOffsets[i])&maskArray[i]);
        }
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               if (obj instanceof byte[]) {
                   byte bdata[] = (byte[])obj;
                   bdata[0] = (byte)(pixel&0xff);
                   return bdata;
               } else {
                   byte bdata[] = {(byte)(pixel&0xff)};
                   return bdata;
               }
            case DataBuffer.TYPE_USHORT:
               if (obj instanceof short[]) {
                   short sdata[] = (short[])obj;
                   sdata[0] = (short)(pixel&0xffff);
                   return sdata;
               } else {
                   short sdata[] = {(short)(pixel&0xffff)};
                   return sdata;
               }
            case DataBuffer.TYPE_INT:
               if (obj instanceof int[]) {
                   int idata[] = (int[])obj;
                   idata[0] = pixel;
                   return idata;
               } else {
                   int idata[] = {pixel};
                   return idata;
               }
            default:
               throw new ClassCastException("此方法尚未实现 transferType " + transferType);
        }
    }

    /**
     * 强制光栅数据与 <code>isAlphaPremultiplied</code> 变量指定的状态匹配，假设数据当前由此 <code>ColorModel</code> 正确描述。
     * 它可能会将颜色光栅数据乘以或除以透明度，或者如果数据处于正确状态则不执行任何操作。
     * 如果数据需要强制转换，此方法还将返回一个 <code>ColorModel</code> 实例，其 <code>isAlphaPremultiplied</code> 标志设置适当。
     * 如果此 <code>transferType</code> 不受此 <code>ColorModel</code> 支持，则此方法将抛出 <code>UnsupportedOperationException</code>。
     * 由于 <code>ColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不重写此方法且使用不受支持的 <code>transferType</code>，则抛出异常。
     *
     * @param raster <code>WritableRaster</code> 数据
     * @param isAlphaPremultiplied <code>true</code> 表示透明度已预乘；<code>false</code> 表示未预乘
     * @return 表示强制数据的 <code>ColorModel</code> 对象。
     * @exception UnsupportedOperationException 如果此 <code>transferType</code> 不受此 <code>ColorModel</code> 支持
     */
    final public ColorModel coerceData (WritableRaster raster,
                                        boolean isAlphaPremultiplied)
    {
        if (!supportsAlpha ||
            this.isAlphaPremultiplied() == isAlphaPremultiplied) {
            return this;
        }

        int w = raster.getWidth();
        int h = raster.getHeight();
        int aIdx = numColorComponents;
        float normAlpha;
        float alphaScale = 1.0f / ((float) ((1 << nBits[aIdx]) - 1));

        int rminX = raster.getMinX();
        int rY = raster.getMinY();
        int rX;
        int pixel[] = null;
        int zpixel[] = null;

        if (isAlphaPremultiplied) {
            // 必须意味着当前未预乘，因此乘以透明度
            switch (transferType) {
                case DataBuffer.TYPE_BYTE: {
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = raster.getPixel(rX, rY, pixel);
                            normAlpha = pixel[aIdx] * alphaScale;
                            if (normAlpha != 0.f) {
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (int) (pixel[c] * normAlpha +
                                                      0.5f);
                                }
                                raster.setPixel(rX, rY, pixel);
                            } else {
                                if (zpixel == null) {
                                    zpixel = new int[numComponents];
                                    java.util.Arrays.fill(zpixel, 0);
                                }
                                raster.setPixel(rX, rY, zpixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_USHORT: {
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = raster.getPixel(rX, rY, pixel);
                            normAlpha = pixel[aIdx] * alphaScale;
                            if (normAlpha != 0.f) {
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (int) (pixel[c] * normAlpha +
                                                      0.5f);
                                }
                                raster.setPixel(rX, rY, pixel);
                            } else {
                                if (zpixel == null) {
                                    zpixel = new int[numComponents];
                                    java.util.Arrays.fill(zpixel, 0);
                                }
                                raster.setPixel(rX, rY, zpixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_INT: {
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = raster.getPixel(rX, rY, pixel);
                            normAlpha = pixel[aIdx] * alphaScale;
                            if (normAlpha != 0.f) {
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (int) (pixel[c] * normAlpha +
                                                      0.5f);
                                }
                                raster.setPixel(rX, rY, pixel);
                            } else {
                                if (zpixel == null) {
                                    zpixel = new int[numComponents];
                                    java.util.Arrays.fill(zpixel, 0);
                                }
                                raster.setPixel(rX, rY, zpixel);
                            }
                        }
                    }
                }
                break;
                default:
                    throw new UnsupportedOperationException("此方法尚未实现 transferType " + transferType);
            }
        }
        else {
            // 当前已预乘，需要除以透明度
            switch (transferType) {
                case DataBuffer.TYPE_BYTE: {
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = raster.getPixel(rX, rY, pixel);
                            normAlpha = pixel[aIdx] * alphaScale;
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (int) (pixel[c] * invAlpha +
                                                      0.5f);
                                }
                                raster.setPixel(rX, rY, pixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_USHORT: {
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = raster.getPixel(rX, rY, pixel);
                            normAlpha = pixel[aIdx] * alphaScale;
                            if (normAlpha != 0) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (int) (pixel[c] * invAlpha +
                                                      0.5f);
                                }
                                raster.setPixel(rX, rY, pixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_INT: {
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = raster.getPixel(rX, rY, pixel);
                            normAlpha = pixel[aIdx] * alphaScale;
                            if (normAlpha != 0) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (int) (pixel[c] * invAlpha +
                                                      0.5f);
                                }
                                raster.setPixel(rX, rY, pixel);
                            }
                        }
                    }
                }
                break;
                default:
                    throw new UnsupportedOperationException("此方法尚未实现 transferType " + transferType);
            }
        }

        // 返回一个新的颜色模型
        return new DirectColorModel(colorSpace, pixel_bits, maskArray[0],
                                    maskArray[1], maskArray[2], maskArray[3],
                                    isAlphaPremultiplied,
                                    transferType);

    }

    /**
      * 如果 <code>raster</code> 与此 <code>ColorModel</code> 兼容，则返回 <code>true</code>；否则返回 <code>false</code>。
      * @param raster 要测试兼容性的 {@link Raster} 对象
      * @return 如果 <code>raster</code> 与此 <code>ColorModel</code> 兼容，则返回 <code>true</code>；否则返回 <code>false</code>。
      */
    public boolean isCompatibleRaster(Raster raster) {
        SampleModel sm = raster.getSampleModel();
        SinglePixelPackedSampleModel spsm;
        if (sm instanceof SinglePixelPackedSampleModel) {
            spsm = (SinglePixelPackedSampleModel) sm;
        }
        else {
            return false;
        }
        if (spsm.getNumBands() != getNumComponents()) {
            return false;
        }

        int[] bitMasks = spsm.getBitMasks();
        for (int i=0; i<numComponents; i++) {
            if (bitMasks[i] != maskArray[i]) {
                return false;
            }
        }


                    return (raster.getTransferType() == transferType);
    }

    private void setFields() {
        // 设置私有字段
        // REMIND: 从本机代码中删除这些字段
        red_mask     = maskArray[0];
        red_offset   = maskOffsets[0];
        green_mask   = maskArray[1];
        green_offset = maskOffsets[1];
        blue_mask    = maskArray[2];
        blue_offset  = maskOffsets[2];
        if (nBits[0] < 8) {
            red_scale = (1 << nBits[0]) - 1;
        }
        if (nBits[1] < 8) {
            green_scale = (1 << nBits[1]) - 1;
        }
        if (nBits[2] < 8) {
            blue_scale = (1 << nBits[2]) - 1;
        }
        if (supportsAlpha) {
            alpha_mask   = maskArray[3];
            alpha_offset = maskOffsets[3];
            if (nBits[3] < 8) {
                alpha_scale = (1 << nBits[3]) - 1;
            }
        }
    }

    /**
     * 返回表示此 <code>DirectColorModel</code> 的 <code>String</code>。
     * @return 表示此 <code>DirectColorModel</code> 的 <code>String</code>。
     */
    public String toString() {
        return new String("DirectColorModel: rmask="
                          +Integer.toHexString(red_mask)+" gmask="
                          +Integer.toHexString(green_mask)+" bmask="
                          +Integer.toHexString(blue_mask)+" amask="
                          +Integer.toHexString(alpha_mask));
    }
}
