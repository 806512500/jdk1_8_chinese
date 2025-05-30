
/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Transparency;
import java.awt.color.ColorSpace;

/**
 * <code>PackedColorModel</code> 类是一个抽象的 {@link ColorModel} 类，用于处理将颜色和 alpha 信息表示为单独样本，并将所有样本打包到单个 int、short 或 byte 量中的像素值。此类可以与任意 {@link ColorSpace} 一起使用。像素值中的颜色样本数量必须与 <code>ColorSpace</code> 中的颜色组件数量相同。可以有一个 alpha 样本。对于使用原始数组像素表示法的方法，数组长度始终为 1，类型为 <code>transferType</code>。支持的传输类型包括 DataBuffer.TYPE_BYTE、DataBuffer.TYPE_USHORT 和 DataBuffer.TYPE_INT。
 * 颜色和 alpha 样本存储在数组的单个元素中，由位掩码指示。每个位掩码必须是连续的，且掩码之间不能重叠。相同的掩码适用于其他方法使用的单个 int 像素表示法。掩码与颜色/alpha 样本的对应关系如下：
 * <ul>
 * <li> 掩码由从 0 到 {@link ColorModel#getNumComponents() getNumComponents}&nbsp;-&nbsp;1 的索引标识。
 * <li> 前 {@link ColorModel#getNumColorComponents() getNumColorComponents} 个索引指代颜色样本。
 * <li> 如果存在 alpha 样本，它对应最后一个索引。
 * <li> 颜色索引的顺序由 <code>ColorSpace</code> 指定。通常，这反映了颜色空间类型的名称（例如，TYPE_RGB），索引 0 对应红色，索引 1 对应绿色，索引 2 对应蓝色。
 * </ul>
 * <p>
 * 将像素值转换为颜色/alpha 组件以用于显示或处理时，样本与组件之间是一对一的对应关系。
 * <code>PackedColorModel</code> 通常用于使用掩码定义打包样本的图像数据。例如，<code>PackedColorModel</code> 可以与 {@link SinglePixelPackedSampleModel} 一起使用来构建 {@link BufferedImage}。通常，{@link SampleModel} 和 <code>ColorModel</code> 使用的掩码是相同的。但是，如果它们不同，像素数据的颜色解释将根据 <code>ColorModel</code> 的掩码进行。
 * <p>
 * 由于此类的所有对象都可以在单个 <code>int</code> 中表示像素值，因此单个 <code>int</code> 像素表示法对所有此类对象都是有效的。因此，使用此表示法的方法不会因为无效的像素值而抛出 <code>IllegalArgumentException</code>。
 * <p>
 * <code>PackedColorModel</code> 的子类是 {@link DirectColorModel}，它类似于 X11 TrueColor 视觉。
 *
 * @see DirectColorModel
 * @see SinglePixelPackedSampleModel
 * @see BufferedImage
 */

public abstract class PackedColorModel extends ColorModel {
    int[] maskArray;
    int[] maskOffsets;
    float[] scaleFactors;

    /**
     * 从颜色掩码数组和 alpha 掩码构造 <code>PackedColorModel</code>，这些掩码指定了 <code>int</code> 像素表示法中包含每个颜色样本的位。颜色组件在指定的 <code>ColorSpace</code> 中。<code>colorMaskArray</code> 的长度应为 <code>ColorSpace</code> 中的组件数量。每个掩码中的所有位必须是连续的，并且适合指定数量的 <code>int</code> 像素表示法的最低有效位。如果 <code>alphaMask</code> 为 0，则没有 alpha。如果有 alpha，<code>boolean</code> <code>isAlphaPremultiplied</code> 指定了如何解释像素值中的颜色和 alpha 样本。如果 <code>boolean</code> 为 <code>true</code>，则假设颜色样本已被 alpha 样本乘以。透明度 <code>trans</code> 指定了此颜色模型可以表示的 alpha 值。传输类型是用于表示像素值的原始数组类型。
     * @param space 指定的 <code>ColorSpace</code>
     * @param bits 像素值中的位数
     * @param colorMaskArray 数组，指定了表示颜色组件的像素值中的位的掩码
     * @param alphaMask 指定了表示 alpha 组件的像素值中的位的掩码
     * @param isAlphaPremultiplied 如果颜色样本已被 alpha 样本乘以，则为 <code>true</code>；否则为 <code>false</code>
     * @param trans 指定了此颜色模型可以表示的 alpha 值
     * @param transferType 用于表示像素值的数组类型
     * @throws IllegalArgumentException 如果 <code>bits</code> 小于 1 或大于 32
     */
    public PackedColorModel (ColorSpace space, int bits,
                             int[] colorMaskArray, int alphaMask,
                             boolean isAlphaPremultiplied,
                             int trans, int transferType) {
        super(bits, PackedColorModel.createBitsArray(colorMaskArray,
                                                     alphaMask),
              space, (alphaMask == 0 ? false : true),
              isAlphaPremultiplied, trans, transferType);
        if (bits < 1 || bits > 32) {
            throw new IllegalArgumentException("位数必须在 1 和 32 之间。");
        }
        maskArray   = new int[numComponents];
        maskOffsets = new int[numComponents];
        scaleFactors = new float[numComponents];

        for (int i=0; i < numColorComponents; i++) {
            // 获取掩码偏移量和位数
            DecomposeMask(colorMaskArray[i], i, space.getName(i));
        }
        if (alphaMask != 0) {
            DecomposeMask(alphaMask, numColorComponents, "alpha");
            if (nBits[numComponents-1] == 1) {
                transparency = Transparency.BITMASK;
            }
        }
    }

    /**
     * 从指定的掩码构造 <code>PackedColorModel</code>，这些掩码指定了 <code>int</code> 像素表示法中包含 alpha、红色、绿色和蓝色颜色样本的位。颜色组件在指定的 <code>ColorSpace</code> 中，该颜色空间必须是 ColorSpace.TYPE_RGB 类型。每个掩码中的所有位必须是连续的，并且适合指定数量的 <code>int</code> 像素表示法的最低有效位。如果 <code>amask</code> 为 0，则没有 alpha。如果有 alpha，<code>boolean</code> <code>isAlphaPremultiplied</code> 指定了如何解释像素值中的颜色和 alpha 样本。如果 <code>boolean</code> 为 <code>true</code>，则假设颜色样本已被 alpha 样本乘以。透明度 <code>trans</code> 指定了此颜色模型可以表示的 alpha 值。传输类型是用于表示像素值的原始数组类型。
     * @param space 指定的 <code>ColorSpace</code>
     * @param bits 像素值中的位数
     * @param rmask 指定了表示红色颜色组件的像素值中的位的掩码
     * @param gmask 指定了表示绿色颜色组件的像素值中的位的掩码
     * @param bmask 指定了表示蓝色颜色组件的像素值中的位的掩码
     * @param amask 指定了表示 alpha 组件的像素值中的位的掩码
     * @param isAlphaPremultiplied 如果颜色样本已被 alpha 样本乘以，则为 <code>true</code>；否则为 <code>false</code>
     * @param trans 指定了此颜色模型可以表示的 alpha 值
     * @param transferType 用于表示像素值的数组类型
     * @throws IllegalArgumentException 如果 <code>space</code> 不是 TYPE_RGB 空间
     * @see ColorSpace
     */
    public PackedColorModel(ColorSpace space, int bits, int rmask, int gmask,
                            int bmask, int amask,
                            boolean isAlphaPremultiplied,
                            int trans, int transferType) {
        super (bits, PackedColorModel.createBitsArray(rmask, gmask, bmask,
                                                      amask),
               space, (amask == 0 ? false : true),
               isAlphaPremultiplied, trans, transferType);

        if (space.getType() != ColorSpace.TYPE_RGB) {
            throw new IllegalArgumentException("ColorSpace 必须是 TYPE_RGB。");
        }
        maskArray = new int[numComponents];
        maskOffsets = new int[numComponents];
        scaleFactors = new float[numComponents];

        DecomposeMask(rmask, 0, "red");

        DecomposeMask(gmask, 1, "green");

        DecomposeMask(bmask, 2, "blue");

        if (amask != 0) {
            DecomposeMask(amask, 3, "alpha");
            if (nBits[3] == 1) {
                transparency = Transparency.BITMASK;
            }
        }
    }

    /**
     * 返回一个掩码，指示像素中包含指定颜色/alpha 样本的位。对于颜色样本，<code>index</code> 对应于颜色样本名称在颜色空间中的位置。因此，对于 CMYK 颜色空间，<code>index</code> 等于 0 对应于青色，<code>index</code> 等于 1 对应于洋红色。如果有 alpha，alpha 的 <code>index</code> 为：
     * <pre>
     *      alphaIndex = numComponents() - 1;
     * </pre>
     * @param index 指定的颜色或 alpha 样本
     * @return 掩码，指示 <code>int</code> 像素表示法中包含由 <code>index</code> 指定的颜色或 alpha 样本的位。
     * @throws ArrayIndexOutOfBoundsException 如果 <code>index</code> 大于此 <code>PackedColorModel</code> 中的组件数量减 1 或 <code>index</code> 小于零
     */
    final public int getMask(int index) {
        return maskArray[index];
    }

    /**
     * 返回一个掩码数组，指示像素中包含颜色和 alpha 样本的位。
     * @return 掩码数组，指示 <code>int</code> 像素表示法中包含颜色或 alpha 样本的位。
     */
    final public int[] getMasks() {
        return (int[]) maskArray.clone();
    }

    /*
     * 一个实用函数，用于计算掩码偏移量和缩放因子，将这些值存储在实例数组中，并验证掩码是否适合指定的像素大小。
     */
    private void DecomposeMask(int mask,  int idx, String componentName) {
        int off = 0;
        int count = nBits[idx];

        // 存储掩码
        maskArray[idx]   = mask;

        // 现在找到移位
        if (mask != 0) {
            while ((mask & 1) == 0) {
                mask >>>= 1;
                off++;
            }
        }

        if (off + count > pixel_bits) {
            throw new IllegalArgumentException(componentName + " 掩码 " +
                                        Integer.toHexString(maskArray[idx])+
                                               " 超出像素（期望 " +
                                               pixel_bits+" 位）");
        }

        maskOffsets[idx] = off;
        if (count == 0) {
            // 足够高以将任何 0-ff 值缩放到 0.0，但不足以在缩放回像素位时得到无穷大
            scaleFactors[idx] = 256.0f;
        } else {
            scaleFactors[idx] = 255.0f / ((1 << count) - 1);
        }

    }

    /**
     * 创建一个具有指定宽度和高度的 <code>SampleModel</code>，其数据布局与此 <code>ColorModel</code> 兼容。
     * @param w 描述的图像数据区域的宽度（以像素为单位）
     * @param h 描述的图像数据区域的高度（以像素为单位）
     * @return 新创建的 <code>SampleModel</code>。
     * @throws IllegalArgumentException 如果 <code>w</code> 或 <code>h</code> 不大于 0
     * @see SampleModel
     */
    public SampleModel createCompatibleSampleModel(int w, int h) {
        return new SinglePixelPackedSampleModel(transferType, w, h,
                                                maskArray);
    }

    /**
     * 检查指定的 <code>SampleModel</code> 是否与此 <code>ColorModel</code> 兼容。如果 <code>sm</code> 为 <code>null</code>，此方法返回 <code>false</code>。
     * @param sm 指定的 <code>SampleModel</code>，或 <code>null</code>
     * @return 如果指定的 <code>SampleModel</code> 与此 <code>ColorModel</code> 兼容，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @see SampleModel
     */
    public boolean isCompatibleSampleModel(SampleModel sm) {
        if (! (sm instanceof SinglePixelPackedSampleModel)) {
            return false;
        }

        // 必须具有相同数量的组件
        if (numComponents != sm.getNumBands()) {
            return false;
        }

        // 传输类型必须相同
        if (sm.getTransferType() != transferType) {
            return false;
        }


                    SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel) sm;
        // 现在比较特定的掩码
        int[] bitMasks = sppsm.getBitMasks();
        if (bitMasks.length != maskArray.length) {
            return false;
        }

        /* 仅比较“有效”掩码，即仅比较适合传输类型容量的掩码部分。 */
        int maxMask = (int)((1L << DataBuffer.getDataTypeSize(transferType)) - 1);
        for (int i=0; i < bitMasks.length; i++) {
            if ((maxMask & bitMasks[i]) != (maxMask & maskArray[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * 返回一个表示图像的 alpha 通道的 {@link WritableRaster}，从输入的 <code>WritableRaster</code> 中提取。
     * 此方法假设与该 <code>ColorModel</code> 关联的 <code>WritableRaster</code> 对象
     * 如果存在 alpha 通道，则将其存储为图像数据的最后一通道。如果没有与该 <code>ColorModel</code>
     * 关联的独立空间 alpha 通道，则返回 <code>null</code>。此方法创建一个新的 <code>WritableRaster</code>，
     * 但共享数据数组。
     * @param raster 包含图像的 <code>WritableRaster</code>
     * @return 表示 <code>raster</code> 中图像的 alpha 通道的 <code>WritableRaster</code>。
     */
    public WritableRaster getAlphaRaster(WritableRaster raster) {
        if (hasAlpha() == false) {
            return null;
        }

        int x = raster.getMinX();
        int y = raster.getMinY();
        int[] band = new int[1];
        band[0] = raster.getNumBands() - 1;
        return raster.createWritableChild(x, y, raster.getWidth(),
                                          raster.getHeight(), x, y,
                                          band);
    }

    /**
     * 测试指定的 <code>Object</code> 是否是 <code>PackedColorModel</code> 的实例
     * 并且等于此 <code>PackedColorModel</code>。
     * @param obj 要测试的 <code>Object</code>
     * @return 如果指定的 <code>Object</code> 是 <code>PackedColorModel</code> 的实例
     * 并且等于此 <code>PackedColorModel</code>，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof PackedColorModel)) {
            return false;
        }

        if (!super.equals(obj)) {
            return false;
        }

        PackedColorModel cm = (PackedColorModel) obj;
        int numC = cm.getNumComponents();
        if (numC != numComponents) {
            return false;
        }
        for(int i=0; i < numC; i++) {
            if (maskArray[i] != cm.getMask(i)) {
                return false;
            }
        }
        return true;
    }

    private final static int[] createBitsArray(int[] colorMaskArray,
                                               int alphaMask) {
        int numColors = colorMaskArray.length;
        int numAlpha = (alphaMask == 0 ? 0 : 1);
        int[] arr = new int[numColors+numAlpha];
        for (int i=0; i < numColors; i++) {
            arr[i] = countBits(colorMaskArray[i]);
            if (arr[i] < 0) {
                throw new IllegalArgumentException("不连续的颜色掩码 ("
                                     + Integer.toHexString(colorMaskArray[i])+
                                     "在索引 "+i);
            }
        }
        if (alphaMask != 0) {
            arr[numColors] = countBits(alphaMask);
            if (arr[numColors] < 0) {
                throw new IllegalArgumentException("不连续的 alpha 掩码 ("
                                     + Integer.toHexString(alphaMask));
            }
        }
        return arr;
    }

    private final static int[] createBitsArray(int rmask, int gmask, int bmask,
                                         int amask) {
        int[] arr = new int[3 + (amask == 0 ? 0 : 1)];
        arr[0] = countBits(rmask);
        arr[1] = countBits(gmask);
        arr[2] = countBits(bmask);
        if (arr[0] < 0) {
            throw new IllegalArgumentException("不连续的红色掩码 ("
                                     + Integer.toHexString(rmask));
        }
        else if (arr[1] < 0) {
            throw new IllegalArgumentException("不连续的绿色掩码 ("
                                     + Integer.toHexString(gmask));
        }
        else if (arr[2] < 0) {
            throw new IllegalArgumentException("不连续的蓝色掩码 ("
                                     + Integer.toHexString(bmask));
        }
        if (amask != 0) {
            arr[3] = countBits(amask);
            if (arr[3] < 0) {
                throw new IllegalArgumentException("不连续的 alpha 掩码 ("
                                     + Integer.toHexString(amask));
            }
        }
        return arr;
    }

    private final static int countBits(int mask) {
        int count = 0;
        if (mask != 0) {
            while ((mask & 1) == 0) {
                mask >>>= 1;
            }
            while ((mask & 1) == 1) {
                mask >>>= 1;
                count++;
            }
        }
        if (mask != 0) {
            return -1;
        }
        return count;
    }

}
