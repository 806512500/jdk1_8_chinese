
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.color.ICC_ColorSpace;

/**
 * 一个 <CODE>ColorModel</CODE> 类，用于表示颜色和 alpha 信息作为单独样本的像素值，并且每个样本存储在单独的数据元素中。此类可以与任意 <CODE>ColorSpace</CODE> 一起使用。像素值中的颜色样本数量必须与 <CODE>ColorSpace</CODE> 中的颜色组件数量相同。可能有一个单独的 alpha 样本。
 * <p>
 * 对于使用类型为 <CODE>transferType</CODE> 的原始数组表示像素的方法，数组长度与颜色和 alpha 样本的数量相同。颜色样本首先存储在数组中，然后是 alpha 样本（如果存在）。颜色样本的顺序由 <CODE>ColorSpace</CODE> 指定。通常，此顺序反映了颜色空间类型的名称。例如，对于 <CODE>TYPE_RGB</CODE>，索引 0 对应红色，索引 1 对应绿色，索引 2 对应蓝色。
 * <p>
 * 从像素样本值到颜色/alpha 组件的转换用于显示或处理目的，基于样本与组件的一对一对应关系。根据用于创建 <code>ComponentColorModel</code> 实例的传输类型，该实例表示的像素样本值可能是有符号或无符号的，并且可能是整数类型或浮点或双精度类型（详见下文）。从样本值到归一化颜色/alpha 组件的转换必须遵循某些规则。对于浮点和双精度样本，转换是恒等的，即归一化组件值等于相应的样本值。对于整数样本，转换应该是简单的缩放和偏移，其中每个组件的缩放和偏移常量可能不同。应用缩放和偏移常量的结果是一组颜色/alpha 组件值，这些值保证落在一定范围内。通常，颜色组件的范围将由 <code>ColorSpace</code> 类的 <code>getMinValue</code> 和 <code>getMaxValue</code> 方法定义。alpha 组件的范围应为 0.0 到 1.0。
 * <p>
 * 使用传输类型 <CODE>DataBuffer.TYPE_BYTE</CODE>、<CODE>DataBuffer.TYPE_USHORT</CODE> 和 <CODE>DataBuffer.TYPE_INT</CODE> 创建的 <code>ComponentColorModel</code> 实例将像素样本值视为无符号整数值。颜色或 alpha 样本的位数可能与传递给 <code>ComponentColorModel(ColorSpace, int[], boolean, boolean, int, int)</code> 构造函数的相应颜色或 alpha 样本的位数不同。在这种情况下，此类假定样本值的最低 n 位包含组件值，其中 n 是传递给构造函数的组件的显著位数。它还假定样本值的任何较高位为零。因此，样本值范围从 0 到 2<sup>n</sup> - 1。此类将这些样本值映射到归一化的颜色组件值，使得 0 映射到 <code>ColorSpace's</code> <code>getMinValue</code> 方法为每个组件获取的值，2<sup>n</sup> - 1 映射到 <code>getMaxValue</code> 获取的值。要创建具有不同颜色样本映射的 <code>ComponentColorModel</code>，需要子类化此类并覆盖 <code>getNormalizedComponents(Object, float[], int)</code> 方法。alpha 样本的映射始终将 0 映射到 0.0，2<sup>n</sup> - 1 映射到 1.0。
 * <p>
 * 对于具有无符号样本值的实例，只有在两个条件满足的情况下才支持未归一化的颜色/alpha 组件表示。首先，样本值 0 必须映射到归一化组件值 0.0，样本值 2<sup>n</sup> - 1 映射到 1.0。其次，<code>ColorSpace</code> 的所有颜色组件的最小/最大范围必须为 0.0 到 1.0。在这种情况下，组件表示是对应样本的最低 n 位。因此，每个组件是一个无符号整数值，范围从 0 到 2<sup>n</sup> - 1，其中 n 是特定组件的显著位数。如果不满足这些条件，任何接受未归一化组件参数的方法将抛出 <code>IllegalArgumentException</code>。
 * <p>
 * 使用传输类型 <CODE>DataBuffer.TYPE_SHORT</CODE>、<CODE>DataBuffer.TYPE_FLOAT</CODE> 和 <CODE>DataBuffer.TYPE_DOUBLE</CODE> 创建的 <code>ComponentColorModel</code> 实例将像素样本值视为有符号短整型、浮点或双精度值。这些实例不支持未归一化的颜色/alpha 组件表示，因此任何接受此类表示作为参数的方法在调用这些实例时将抛出 <code>IllegalArgumentException</code>。此类实例的归一化组件值范围取决于传输类型如下：对于浮点样本，范围为浮点数据类型的全部范围；对于双精度样本，范围为浮点数据类型的全部范围（通过将双精度转换为浮点获得）；对于短整型样本，范围大约为 -maxVal 到 +maxVal，其中 maxVal 是 <code>ColorSpace</code> 的每个组件的最大值（-32767 映射到 -maxVal，0 映射到 0.0，32767 映射到 +maxVal）。子类可以通过覆盖 <code>getNormalizedComponents(Object, float[], int)</code> 方法来覆盖短整型样本值到归一化组件值的缩放。对于浮点和双精度样本，归一化组件值被认为等于相应的样本值，子类不应尝试为这些传输类型添加任何非恒等缩放。
 * <p>
 * 使用传输类型 <CODE>DataBuffer.TYPE_SHORT</CODE>、<CODE>DataBuffer.TYPE_FLOAT</CODE> 和 <CODE>DataBuffer.TYPE_DOUBLE</CODE> 创建的 <code>ComponentColorModel</code> 实例使用所有样本值的所有位。因此，当使用 <CODE>DataBuffer.TYPE_SHORT</CODE> 时，所有颜色/alpha 组件有 16 位；当使用 <CODE>DataBuffer.TYPE_FLOAT</CODE> 时，有 32 位；当使用 <CODE>DataBuffer.TYPE_DOUBLE</CODE> 时，有 64 位。当使用 <code>ComponentColorModel(ColorSpace, int[], boolean, boolean, int, int)</code> 形式的构造函数与这些传输类型之一时，位数组参数将被忽略。
 * <p>
 * 可能存在无法合理解释为渲染组件值的颜色/alpha 样本值。这可能发生在 <code>ComponentColorModel</code> 被子类化以覆盖无符号样本值到归一化颜色组件值的映射，或者使用超出一定范围的有符号样本值时。例如，指定一个超出范围 0 到 32767（归一化范围 0.0 到 1.0）的有符号短整型 alpha 组件可能导致意外结果。应用程序有责任在渲染之前适当缩放像素数据，使得颜色组件落在 <code>ColorSpace</code> 的归一化范围内（使用 <code>ColorSpace</code> 类的 <code>getMinValue</code> 和 <code>getMaxValue</code> 方法获取），alpha 组件在 0.0 和 1.0 之间。如果颜色或 alpha 组件值超出这些范围，渲染结果是不确定的。
 * <p>
 * 使用单个 int 像素表示的方法将抛出 <CODE>IllegalArgumentException</CODE>，除非 <CODE>ComponentColorModel</CODE> 的组件数量为一且组件值为无符号——换句话说，单个颜色组件使用传输类型 <CODE>DataBuffer.TYPE_BYTE</CODE>、<CODE>DataBuffer.TYPE_USHORT</CODE> 或 <CODE>DataBuffer.TYPE_INT</CODE>，并且没有 alpha。
 * <p>
 * <CODE>ComponentColorModel</CODE> 可以与 <CODE>ComponentSampleModel</CODE>、<CODE>BandedSampleModel</CODE> 或 <CODE>PixelInterleavedSampleModel</CODE> 一起使用来构建 <CODE>BufferedImage</CODE>。
 *
 * @see ColorModel
 * @see ColorSpace
 * @see ComponentSampleModel
 * @see BandedSampleModel
 * @see PixelInterleavedSampleModel
 * @see BufferedImage
 *
 */
public class ComponentColorModel extends ColorModel {

    /**
     * <code>signed</code> 对于 <code>short</code>、<code>float</code> 和 <code>double</code> 传输类型为 <code>true</code>；对于 <code>byte</code>、<code>ushort</code> 和 <code>int</code> 传输类型为 <code>false</code>。
     */
    private boolean signed; // true for transfer types short, float, double
                            // false for byte, ushort, int
    private boolean is_sRGB_stdScale;
    private boolean is_LinearRGB_stdScale;
    private boolean is_LinearGray_stdScale;
    private boolean is_ICCGray_stdScale;
    private byte[] tosRGB8LUT;
    private byte[] fromsRGB8LUT8;
    private short[] fromsRGB8LUT16;
    private byte[] fromLinearGray16ToOtherGray8LUT;
    private short[] fromLinearGray16ToOtherGray16LUT;
    private boolean needScaleInit;
    private boolean noUnnorm;
    private boolean nonStdScale;
    private float[] min;
    private float[] diffMinMax;
    private float[] compOffset;
    private float[] compScale;

    /**
     * 从指定参数构造 <CODE>ComponentColorModel</CODE>。颜色组件将位于指定的 <CODE>ColorSpace</CODE> 中。支持的传输类型为 <CODE>DataBuffer.TYPE_BYTE</CODE>、<CODE>DataBuffer.TYPE_USHORT</CODE>、<CODE>DataBuffer.TYPE_INT</CODE>、<CODE>DataBuffer.TYPE_SHORT</CODE>、<CODE>DataBuffer.TYPE_FLOAT</CODE> 和 <CODE>DataBuffer.TYPE_DOUBLE</CODE>。如果 <CODE>bits</CODE> 数组不为 null，则指定每个颜色和 alpha 组件的显著位数，其长度应至少为 <CODE>ColorSpace</CODE> 中的组件数量，如果像素值中没有 alpha 信息，则长度应比这个数量多一个。当 <CODE>transferType</CODE> 为 <CODE>DataBuffer.TYPE_SHORT</CODE>、<CODE>DataBuffer.TYPE_FLOAT</CODE> 或 <CODE>DataBuffer.TYPE_DOUBLE</CODE> 时，<CODE>bits</CODE> 数组参数将被忽略。<CODE>hasAlpha</CODE> 表示是否包含 alpha 信息。如果 <CODE>hasAlpha</CODE> 为 true，则布尔值 <CODE>isAlphaPremultiplied</CODE> 指定如何解释像素值中的颜色和 alpha 样本。如果布尔值为 true，则假定颜色样本已被 alpha 样本乘以。<CODE>transparency</CODE> 指定此颜色模型可以表示的 alpha 值。可接受的 <code>transparency</code> 值为 <CODE>OPAQUE</CODE>、<CODE>BITMASK</CODE> 或 <CODE>TRANSLUCENT</CODE>。<CODE>transferType</CODE> 指定用于表示像素值的原始数组类型。
     *
     * @param colorSpace       与此颜色模型关联的 <CODE>ColorSpace</CODE>。
     * @param bits             每个组件的显著位数。可以为 null，在这种情况下，所有组件样本的所有位都将显著。如果传输类型为 <CODE>DataBuffer.TYPE_SHORT</CODE>、<CODE>DataBuffer.TYPE_FLOAT</CODE> 或 <CODE>DataBuffer.TYPE_DOUBLE</CODE>，则忽略此参数，所有组件样本的所有位都将显著。
     * @param hasAlpha         如果为 true，此颜色模型支持 alpha。
     * @param isAlphaPremultiplied 如果为 true，alpha 是预乘的。
     * @param transparency     指定此颜色模型可以表示的 alpha 值。
     * @param transferType     指定用于表示像素值的原始数组类型。
     *
     * @throws IllegalArgumentException 如果 <CODE>bits</CODE> 数组参数不为 null，其长度小于颜色和 alpha 组件的数量，并且传输类型为 <CODE>DataBuffer.TYPE_BYTE</CODE>、<CODE>DataBuffer.TYPE_USHORT</CODE> 或 <CODE>DataBuffer.TYPE_INT</CODE>。
     * @throws IllegalArgumentException 如果传输类型不是 <CODE>DataBuffer.TYPE_BYTE</CODE>、<CODE>DataBuffer.TYPE_USHORT</CODE>、<CODE>DataBuffer.TYPE_INT</CODE>、<CODE>DataBuffer.TYPE_SHORT</CODE>、<CODE>DataBuffer.TYPE_FLOAT</CODE> 或 <CODE>DataBuffer.TYPE_DOUBLE</CODE>。
     *
     * @see ColorSpace
     * @see java.awt.Transparency
     */
    public ComponentColorModel (ColorSpace colorSpace,
                                int[] bits,
                                boolean hasAlpha,
                                boolean isAlphaPremultiplied,
                                int transparency,
                                int transferType) {
        super (bitsHelper(transferType, colorSpace, hasAlpha),
               bitsArrayHelper(bits, transferType, colorSpace, hasAlpha),
               colorSpace, hasAlpha, isAlphaPremultiplied, transparency,
               transferType);
        switch(transferType) {
            case DataBuffer.TYPE_BYTE:
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_INT:
                signed = false;
                needScaleInit = true;
                break;
            case DataBuffer.TYPE_SHORT:
                signed = true;
                needScaleInit = true;
                break;
            case DataBuffer.TYPE_FLOAT:
            case DataBuffer.TYPE_DOUBLE:
                signed = true;
                needScaleInit = false;
                noUnnorm = true;
                nonStdScale = false;
                break;
            default:
                throw new IllegalArgumentException("此构造函数不兼容传输类型 " + transferType);
        }
        setupLUTs();
    }


                /**
     * 从指定的参数构造 <CODE>ComponentColorModel</CODE>。颜色组件将位于指定的
     * <CODE>ColorSpace</CODE> 中。支持的传输类型为
     * <CODE>DataBuffer.TYPE_BYTE</CODE>、<CODE>DataBuffer.TYPE_USHORT</CODE>、
     * <CODE>DataBuffer.TYPE_INT</CODE>、<CODE>DataBuffer.TYPE_SHORT</CODE>、
     * <CODE>DataBuffer.TYPE_FLOAT</CODE> 和 <CODE>DataBuffer.TYPE_DOUBLE</CODE>。
     * 每个颜色和 alpha 组件的显著位数分别为 8、16、32、16、32 或 64。颜色组件的数量将为
     * <CODE>ColorSpace</CODE> 中的组件数量。如果 <CODE>hasAlpha</CODE> 为 <CODE>true</CODE>，
     * 则将有 alpha 组件。如果 <CODE>hasAlpha</CODE> 为 true，则
     * 布尔值 <CODE>isAlphaPremultiplied</CODE> 指定如何解释像素值中的颜色和 alpha 样本。
     * 如果该布尔值为 true，颜色样本假定已被 alpha 样本乘以。<CODE>transparency</CODE>
     * 指定此颜色模型可以表示的 alpha 值。可接受的 <code>transparency</code> 值为
     * <CODE>OPAQUE</CODE>、<CODE>BITMASK</CODE> 或 <CODE>TRANSLUCENT</CODE>。
     * <CODE>transferType</CODE> 是用于表示像素值的原始数组类型。
     *
     * @param colorSpace       与此颜色模型关联的 <CODE>ColorSpace</CODE>。
     * @param hasAlpha         如果为 true，此颜色模型支持 alpha。
     * @param isAlphaPremultiplied 如果为 true，alpha 已预先乘以。
     * @param transparency     指定此颜色模型可以表示的 alpha 值。
     * @param transferType     指定用于表示像素值的原始数组类型。
     *
     * @throws IllegalArgumentException 如果 transferType 不是
     *         <CODE>DataBuffer.TYPE_BYTE</CODE>、
     *         <CODE>DataBuffer.TYPE_USHORT</CODE>、
     *         <CODE>DataBuffer.TYPE_INT</CODE>、
     *         <CODE>DataBuffer.TYPE_SHORT</CODE>、
     *         <CODE>DataBuffer.TYPE_FLOAT</CODE> 或
     *         <CODE>DataBuffer.TYPE_DOUBLE</CODE>。
     *
     * @see ColorSpace
     * @see java.awt.Transparency
     * @since 1.4
     */
    public ComponentColorModel (ColorSpace colorSpace,
                                boolean hasAlpha,
                                boolean isAlphaPremultiplied,
                                int transparency,
                                int transferType) {
        this(colorSpace, null, hasAlpha, isAlphaPremultiplied,
             transparency, transferType);
    }

    private static int bitsHelper(int transferType,
                                  ColorSpace colorSpace,
                                  boolean hasAlpha) {
        int numBits = DataBuffer.getDataTypeSize(transferType);
        int numComponents = colorSpace.getNumComponents();
        if (hasAlpha) {
            ++numComponents;
        }
        return numBits * numComponents;
    }

    private static int[] bitsArrayHelper(int[] origBits,
                                         int transferType,
                                         ColorSpace colorSpace,
                                         boolean hasAlpha) {
        switch(transferType) {
            case DataBuffer.TYPE_BYTE:
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_INT:
                if (origBits != null) {
                    return origBits;
                }
                break;
            default:
                break;
        }
        int numBits = DataBuffer.getDataTypeSize(transferType);
        int numComponents = colorSpace.getNumComponents();
        if (hasAlpha) {
            ++numComponents;
        }
        int[] bits = new int[numComponents];
        for (int i = 0; i < numComponents; i++) {
            bits[i] = numBits;
        }
        return bits;
    }

    private void setupLUTs() {
        // REMIND: 有潜力加速 sRGB、LinearRGB、LinearGray、ICCGray 和非标准缩放的非 ICC Gray 空间，
        // 如果这变得重要
        //
        // 注意：当此方法在构造时被调用时，is_xxx_stdScale 和 nonStdScale 布尔值会临时设置。
        // 这些变量可能在稍后调用 initScale 时再次设置。
        // 当 setupLUTs 返回时，如果（transferType 不是 float 或 double）AND（某些最小 ColorSpace 组件值
        // 不是 0.0 OR 某些最大 ColorSpace 组件值不是 1.0），则 nonStdScale 为 true。
        // 这对于 initScale() 中对 getNormalizedComponents(Object, float[], int) 的调用是正确的。
        // initScale() 可能会根据 getNormalizedComponents() 的返回值更改 nonStdScale 的值 -
        // 这只会发生在 getNormalizedComponents() 被子类重写以使最小/最大像素样本值的映射
        // 与最小/最大颜色组件值不同。
        if (is_sRGB) {
            is_sRGB_stdScale = true;
            nonStdScale = false;
        } else if (ColorModel.isLinearRGBspace(colorSpace)) {
            // 注意，内置的 Linear RGB 空间假设每个坐标的归一化范围为 0.0 - 1.0。使用这些 LUT 时会做出此假设。
            is_LinearRGB_stdScale = true;
            nonStdScale = false;
            if (transferType == DataBuffer.TYPE_BYTE) {
                tosRGB8LUT = ColorModel.getLinearRGB8TosRGB8LUT();
                fromsRGB8LUT8 = ColorModel.getsRGB8ToLinearRGB8LUT();
            } else {
                tosRGB8LUT = ColorModel.getLinearRGB16TosRGB8LUT();
                fromsRGB8LUT16 = ColorModel.getsRGB8ToLinearRGB16LUT();
            }
        } else if ((colorSpaceType == ColorSpace.TYPE_GRAY) &&
                   (colorSpace instanceof ICC_ColorSpace) &&
                   (colorSpace.getMinValue(0) == 0.0f) &&
                   (colorSpace.getMaxValue(0) == 1.0f)) {
            // 注意，灰度组件的归一化范围为 0.0 - 1.0 是必需的，因为使用这些 LUT 时会做出此假设。
            ICC_ColorSpace ics = (ICC_ColorSpace) colorSpace;
            is_ICCGray_stdScale = true;
            nonStdScale = false;
            fromsRGB8LUT16 = ColorModel.getsRGB8ToLinearRGB16LUT();
            if (ColorModel.isLinearGRAYspace(ics)) {
                is_LinearGray_stdScale = true;
                if (transferType == DataBuffer.TYPE_BYTE) {
                    tosRGB8LUT = ColorModel.getGray8TosRGB8LUT(ics);
                } else {
                    tosRGB8LUT = ColorModel.getGray16TosRGB8LUT(ics);
                }
            } else {
                if (transferType == DataBuffer.TYPE_BYTE) {
                    tosRGB8LUT = ColorModel.getGray8TosRGB8LUT(ics);
                    fromLinearGray16ToOtherGray8LUT =
                        ColorModel.getLinearGray16ToOtherGray8LUT(ics);
                } else {
                    tosRGB8LUT = ColorModel.getGray16TosRGB8LUT(ics);
                    fromLinearGray16ToOtherGray16LUT =
                        ColorModel.getLinearGray16ToOtherGray16LUT(ics);
                }
            }
        } else if (needScaleInit) {
            // 如果 transferType 是 byte、ushort、int 或 short，并且我们还不知道
            // ColorSpace 的所有组件的 minVlaue 是否为 0.0f 且 maxValue 是否为 1.0f，
            // 我们现在需要检查这一点，并在必要时设置 min[] 和 diffMinMax[] 数组。
            nonStdScale = false;
            for (int i = 0; i < numColorComponents; i++) {
                if ((colorSpace.getMinValue(i) != 0.0f) ||
                    (colorSpace.getMaxValue(i) != 1.0f)) {
                    nonStdScale = true;
                    break;
                }
            }
            if (nonStdScale) {
                min = new float[numColorComponents];
                diffMinMax = new float[numColorComponents];
                for (int i = 0; i < numColorComponents; i++) {
                    min[i] = colorSpace.getMinValue(i);
                    diffMinMax[i] = colorSpace.getMaxValue(i) - min[i];
                }
            }
        }
    }

    private void initScale() {
        // 如果 transferType 支持非标准缩放（如上所述的 byte、ushort、int 和 short），
        // 则此方法将在第一次调用使用像素样本值到颜色组件值缩放信息的方法时被调用，
        // 除非该方法是 getNormalizedComponents(Object, float[], int)（该方法必须被重写以使用非标准缩放）。
        // 此方法还为这些 transferTypes 设置 noUnnorm 布尔变量。
        // 调用此方法后，如果 getNormalizedComponents() 将样本值 0 映射为任何非 0.0f 的值
        // 或将样本值 2^^n - 1（对于 short transferType 为 2^^15 - 1）映射为任何非 1.0f 的值，
        // 则 nonStdScale 变量将为 true。请注意，这可以独立于 colorSpace 的最小/最大组件值，
        // 如果出于某种原因重写了 getNormalizedComponents() 方法，例如为了在样本值中提供比颜色组件值更大的动态范围。
        // 不幸的是，此方法不能在构造时调用，因为子类可能仍有未初始化的状态，这会导致 getNormalizedComponents() 返回错误的结果。
        needScaleInit = false; // 只需调用一次
        if (nonStdScale || signed) {
            // 未归一化形式仅支持无符号 transferTypes 和当 ColorSpace 的最小/最大值为 0.0/1.0 时。
            // 当此方法被调用时，如果后者条件不成立，则 nonStdScale 为 true。此外，
            // 未归一化形式要求像素样本值的完整范围映射到颜色组件值的完整 0.0 - 1.0 范围。
            // 该条件在本方法的稍后部分检查。
            noUnnorm = true;
        } else {
            noUnnorm = false;
        }
        float[] lowVal, highVal;
        switch (transferType) {
        case DataBuffer.TYPE_BYTE:
            {
                byte[] bpixel = new byte[numComponents];
                for (int i = 0; i < numColorComponents; i++) {
                    bpixel[i] = 0;
                }
                if (supportsAlpha) {
                    bpixel[numColorComponents] =
                        (byte) ((1 << nBits[numColorComponents]) - 1);
                }
                lowVal = getNormalizedComponents(bpixel, null, 0);
                for (int i = 0; i < numColorComponents; i++) {
                    bpixel[i] = (byte) ((1 << nBits[i]) - 1);
                }
                highVal = getNormalizedComponents(bpixel, null, 0);
            }
            break;
        case DataBuffer.TYPE_USHORT:
            {
                short[] uspixel = new short[numComponents];
                for (int i = 0; i < numColorComponents; i++) {
                    uspixel[i] = 0;
                }
                if (supportsAlpha) {
                    uspixel[numColorComponents] =
                        (short) ((1 << nBits[numColorComponents]) - 1);
                }
                lowVal = getNormalizedComponents(uspixel, null, 0);
                for (int i = 0; i < numColorComponents; i++) {
                    uspixel[i] = (short) ((1 << nBits[i]) - 1);
                }
                highVal = getNormalizedComponents(uspixel, null, 0);
            }
            break;
        case DataBuffer.TYPE_INT:
            {
                int[] ipixel = new int[numComponents];
                for (int i = 0; i < numColorComponents; i++) {
                    ipixel[i] = 0;
                }
                if (supportsAlpha) {
                    ipixel[numColorComponents] =
                        ((1 << nBits[numColorComponents]) - 1);
                }
                lowVal = getNormalizedComponents(ipixel, null, 0);
                for (int i = 0; i < numColorComponents; i++) {
                    ipixel[i] = ((1 << nBits[i]) - 1);
                }
                highVal = getNormalizedComponents(ipixel, null, 0);
            }
            break;
        case DataBuffer.TYPE_SHORT:
            {
                short[] spixel = new short[numComponents];
                for (int i = 0; i < numColorComponents; i++) {
                    spixel[i] = 0;
                }
                if (supportsAlpha) {
                    spixel[numColorComponents] = 32767;
                }
                lowVal = getNormalizedComponents(spixel, null, 0);
                for (int i = 0; i < numColorComponents; i++) {
                    spixel[i] = 32767;
                }
                highVal = getNormalizedComponents(spixel, null, 0);
            }
            break;
        default:
            lowVal = highVal = null;  // 以避免编译器抱怨
            break;
        }
        nonStdScale = false;
        for (int i = 0; i < numColorComponents; i++) {
            if ((lowVal[i] != 0.0f) || (highVal[i] != 1.0f)) {
                nonStdScale = true;
                break;
            }
        }
        if (nonStdScale) {
            noUnnorm = true;
            is_sRGB_stdScale = false;
            is_LinearRGB_stdScale = false;
            is_LinearGray_stdScale = false;
            is_ICCGray_stdScale = false;
            compOffset = new float[numColorComponents];
            compScale = new float[numColorComponents];
            for (int i = 0; i < numColorComponents; i++) {
                compOffset[i] = lowVal[i];
                compScale[i] = 1.0f / (highVal[i] - lowVal[i]);
            }
        }
    }

    private int getRGBComponent(int pixel, int idx) {
        if (numComponents > 1) {
            throw new
                IllegalArgumentException("每个像素的组件数超过一个");
        }
        if (signed) {
            throw new
                IllegalArgumentException("组件值为有符号");
        }
        if (needScaleInit) {
            initScale();
        }
        // 由于只有一个组件，因此没有 alpha

        // 为了转换它，先对像素进行归一化
        Object opixel = null;
        switch (transferType) {
        case DataBuffer.TYPE_BYTE:
            {
                byte[] bpixel = { (byte) pixel };
                opixel = bpixel;
            }
            break;
        case DataBuffer.TYPE_USHORT:
            {
                short[] spixel = { (short) pixel };
                opixel = spixel;
            }
            break;
        case DataBuffer.TYPE_INT:
            {
                int[] ipixel = { pixel };
                opixel = ipixel;
            }
            break;
        }
        float[] norm = getNormalizedComponents(opixel, null, 0);
        float[] rgb = colorSpace.toRGB(norm);


    /**
     * 返回指定像素的红色颜色分量，缩放范围为 0 到 255，默认的 RGB 颜色空间为 sRGB。如果需要，将进行颜色转换。像素值指定为一个 int。
     * 返回的值将是一个非预乘值。如果 alpha 是预乘的，此方法会在返回值之前将其除以 alpha（如果 alpha 值为 0，则红色值将为 0）。
     *
     * @param pixel 您想要从中获取红色颜色分量的像素。
     *
     * @return 指定像素的红色颜色分量，作为 int。
     *
     * @throws IllegalArgumentException 如果此 <CODE>ColorModel</CODE> 中有多个组件。
     * @throws IllegalArgumentException 如果此 <CODE>ColorModel</CODE> 的组件值为有符号值。
     */
    public int getRed(int pixel) {
        return getRGBComponent(pixel, 0);
    }

    /**
     * 返回指定像素的绿色颜色分量，缩放范围为 0 到 255，默认的 RGB 颜色空间为 sRGB。如果需要，将进行颜色转换。像素值指定为一个 int。
     * 返回的值将是一个非预乘值。如果 alpha 是预乘的，此方法会在返回值之前将其除以 alpha（如果 alpha 值为 0，则绿色值将为 0）。
     *
     * @param pixel 您想要从中获取绿色颜色分量的像素。
     *
     * @return 指定像素的绿色颜色分量，作为 int。
     *
     * @throws IllegalArgumentException 如果此 <CODE>ColorModel</CODE> 中有多个组件。
     * @throws IllegalArgumentException 如果此 <CODE>ColorModel</CODE> 的组件值为有符号值。
     */
    public int getGreen(int pixel) {
        return getRGBComponent(pixel, 1);
    }

    /**
     * 返回指定像素的蓝色颜色分量，缩放范围为 0 到 255，默认的 RGB 颜色空间为 sRGB。如果需要，将进行颜色转换。像素值指定为一个 int。
     * 返回的值将是一个非预乘值。如果 alpha 是预乘的，此方法会在返回值之前将其除以 alpha（如果 alpha 值为 0，则蓝色值将为 0）。
     *
     * @param pixel 您想要从中获取蓝色颜色分量的像素。
     *
     * @return 指定像素的蓝色颜色分量，作为 int。
     *
     * @throws IllegalArgumentException 如果此 <CODE>ColorModel</CODE> 中有多个组件。
     * @throws IllegalArgumentException 如果此 <CODE>ColorModel</CODE> 的组件值为有符号值。
     */
    public int getBlue(int pixel) {
        return getRGBComponent(pixel, 2);
    }

    /**
     * 返回指定像素的 alpha 组件，缩放范围为 0 到 255。像素值指定为一个 int。
     *
     * @param pixel 您想要从中获取 alpha 组件的像素。
     *
     * @return 指定像素的 alpha 组件，作为 int。
     *
     * @throws IllegalArgumentException 如果此 <CODE>ColorModel</CODE> 中有多个组件。
     * @throws IllegalArgumentException 如果此 <CODE>ColorModel</CODE> 的组件值为有符号值。
     */
    public int getAlpha(int pixel) {
        if (supportsAlpha == false) {
            return 255;
        }
        if (numComponents > 1) {
            throw new
                IllegalArgumentException("More than one component per pixel");
        }
        if (signed) {
            throw new
                IllegalArgumentException("Component value is signed");
        }

        return (int) ((((float) pixel) / ((1<<nBits[0])-1)) * 255.0f + 0.5f);
    }

    /**
     * 返回指定像素的颜色/alpha 组件，默认的 RGB 颜色模型格式。如果需要，将进行颜色转换。返回的值将是一个非预乘格式。如果 alpha 是预乘的，
     * 此方法会在返回值之前将其除以 alpha（如果 alpha 值为 0，则颜色值将为 0）。
     *
     * @param pixel 您想要从中获取颜色/alpha 组件的像素。
     *
     * @return 指定像素的颜色/alpha 组件，作为 int。
     *
     * @throws IllegalArgumentException 如果此 <CODE>ColorModel</CODE> 中有多个组件。
     * @throws IllegalArgumentException 如果此 <CODE>ColorModel</CODE> 的组件值为有符号值。
     */
    public int getRGB(int pixel) {
        if (numComponents > 1) {
            throw new
                IllegalArgumentException("More than one component per pixel");
        }
        if (signed) {
            throw new
                IllegalArgumentException("Component value is signed");
        }

        return (getAlpha(pixel) << 24)
            | (getRed(pixel) << 16)
            | (getGreen(pixel) << 8)
            | (getBlue(pixel) << 0);
    }

    private int extractComponent(Object inData, int idx, int precision) {
        // 从 inData 中提取组件 idx。精度参数应为 8 或 16。如果为 8，此方法将返回 8 位值。如果为 16，此方法将返回 16 位值（对于传输类型不是 TYPE_BYTE 的情况）。
        // 对于 TYPE_BYTE，将返回 8 位值。

        // 此方法将输入值映射到归一化的 ColorSpace 组件值 0.0 对应 0，归一化的 ColorSpace 组件值 1.0 对应 2^n - 1（其中 n 为 8 或 16），因此仅适用于最小/最大组件值为 0.0/1.0 的 ColorSpaces。
        // 这对于 sRGB、内置的线性 RGB 和线性灰度空间以及我们预计算了 LUT 的任何其他 ICC 灰度空间都是正确的。

        boolean needAlpha = (supportsAlpha && isAlphaPremultiplied);
        int alp = 0;
        int comp;
        int mask = (1 << nBits[idx]) - 1;

        switch (transferType) {
            // 注意：我们在这里不对像素数据进行任何裁剪——我们假设数据已正确缩放
            case DataBuffer.TYPE_SHORT: {
                short sdata[] = (short[]) inData;
                float scalefactor = (float) ((1 << precision) - 1);
                if (needAlpha) {
                    short s = sdata[numColorComponents];
                    if (s != (short) 0) {
                        return (int) ((((float) sdata[idx]) /
                                       ((float) s)) * scalefactor + 0.5f);
                    } else {
                        return 0;
                    }
                } else {
                    return (int) ((sdata[idx] / 32767.0f) * scalefactor + 0.5f);
                }
            }
            case DataBuffer.TYPE_FLOAT: {
                float fdata[] = (float[]) inData;
                float scalefactor = (float) ((1 << precision) - 1);
                if (needAlpha) {
                    float f = fdata[numColorComponents];
                    if (f != 0.0f) {
                        return (int) (((fdata[idx] / f) * scalefactor) + 0.5f);
                    } else {
                        return 0;
                    }
                } else {
                    return (int) (fdata[idx] * scalefactor + 0.5f);
                }
            }
            case DataBuffer.TYPE_DOUBLE: {
                double ddata[] = (double[]) inData;
                double scalefactor = (double) ((1 << precision) - 1);
                if (needAlpha) {
                    double d = ddata[numColorComponents];
                    if (d != 0.0) {
                        return (int) (((ddata[idx] / d) * scalefactor) + 0.5);
                    } else {
                        return 0;
                    }
                } else {
                    return (int) (ddata[idx] * scalefactor + 0.5);
                }
            }
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               comp = bdata[idx] & mask;
               precision = 8;
               if (needAlpha) {
                   alp = bdata[numColorComponents] & mask;
               }
            break;
            case DataBuffer.TYPE_USHORT:
               short usdata[] = (short[])inData;
               comp = usdata[idx] & mask;
               if (needAlpha) {
                   alp = usdata[numColorComponents] & mask;
               }
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               comp = idata[idx];
               if (needAlpha) {
                   alp = idata[numColorComponents];
               }
            break;
            default:
               throw new
                   UnsupportedOperationException("此方法尚未实现传输类型 " + transferType);
        }
        if (needAlpha) {
            if (alp != 0) {
                float scalefactor = (float) ((1 << precision) - 1);
                float fcomp = ((float) comp) / ((float)mask);
                float invalp = ((float) ((1<<nBits[numColorComponents]) - 1)) /
                               ((float) alp);
                return (int) (fcomp * invalp * scalefactor + 0.5f);
            } else {
                return 0;
            }
        } else {
            if (nBits[idx] != precision) {
                float scalefactor = (float) ((1 << precision) - 1);
                float fcomp = ((float) comp) / ((float)mask);
                return (int) (fcomp * scalefactor + 0.5f);
            }
            return comp;
        }
    }

    private int getRGBComponent(Object inData, int idx) {
        if (needScaleInit) {
            initScale();
        }
        if (is_sRGB_stdScale) {
            return extractComponent(inData, idx, 8);
        } else if (is_LinearRGB_stdScale) {
            int lutidx = extractComponent(inData, idx, 16);
            return tosRGB8LUT[lutidx] & 0xff;
        } else if (is_ICCGray_stdScale) {
            int lutidx = extractComponent(inData, 0, 16);
            return tosRGB8LUT[lutidx] & 0xff;
        }

        // 不是 CS_sRGB、CS_LINEAR_RGB 或任何 TYPE_GRAY ICC_ColorSpace
        float[] norm = getNormalizedComponents(inData, null, 0);
        // 注意 getNormalizedComponents 返回非预乘值
        float[] rgb = colorSpace.toRGB(norm);
        return (int) (rgb[idx] * 255.0f + 0.5f);
    }

    /**
     * 返回指定像素的红色颜色分量，缩放范围为 0 到 255，默认的 RGB 颜色空间为 sRGB。如果需要，将进行颜色转换。像素值由作为对象引用传递的类型为 <CODE>transferType</CODE> 的数据元素数组指定。
     * 返回的值将是一个非预乘值。如果 alpha 是预乘的，此方法会在返回值之前将其除以 alpha（如果 alpha 值为 0，则红色值将为 0）。由于 <code>ComponentColorModel</code> 可以被子类化，
     * 子类继承此方法的实现，如果它们不重写此方法并且使用不受支持的 <code>transferType</code>，则会抛出异常。
     *
     * @param inData 您想要从中获取红色颜色分量的像素，由类型为 <CODE>transferType</CODE> 的数据元素数组指定。
     *
     * @return 指定像素的红色颜色分量，作为 int。
     *
     * @throws ClassCastException 如果 <CODE>inData</CODE> 不是类型为 <CODE>transferType</CODE> 的基本数组。
     * @throws ArrayIndexOutOfBoundsException 如果 <CODE>inData</CODE> 不足以容纳此 <CODE>ColorModel</CODE> 的像素值。
     * @throws UnsupportedOperationException 如果此 <CODE>ComponentColorModel</CODE> 的传输类型不是支持的传输类型之一：
     * <CODE>DataBuffer.TYPE_BYTE</CODE>、<CODE>DataBuffer.TYPE_USHORT</CODE>、<CODE>DataBuffer.TYPE_INT</CODE>、<CODE>DataBuffer.TYPE_SHORT</CODE>、
     * <CODE>DataBuffer.TYPE_FLOAT</CODE> 或 <CODE>DataBuffer.TYPE_DOUBLE</CODE>。
     */
    public int getRed(Object inData) {
        return getRGBComponent(inData, 0);
    }


    /**
     * 返回指定像素的绿色颜色分量，缩放范围为 0 到 255，默认的 RGB <CODE>ColorSpace</CODE> 为 sRGB。如果需要，将进行颜色转换。像素值由作为对象引用传递的类型为 <CODE>transferType</CODE> 的数据元素数组指定。
     * 返回的值是一个非预乘值。如果 alpha 是预乘的，此方法会在返回值之前将其除以 alpha（如果 alpha 值为 0，则绿色值将为 0）。由于 <code>ComponentColorModel</code> 可以被子类化，
     * 子类继承此方法的实现，如果它们不重写此方法并且使用不受支持的 <code>transferType</code>，则会抛出异常。
     *
     * @param inData 您想要从中获取绿色颜色分量的像素，由类型为 <CODE>transferType</CODE> 的数据元素数组指定。
     *
     * @return 指定像素的绿色颜色分量，作为 int。
     *
     * @throws ClassCastException 如果 <CODE>inData</CODE> 不是类型为 <CODE>transferType</CODE> 的基本数组。
     * @throws ArrayIndexOutOfBoundsException 如果 <CODE>inData</CODE> 不足以容纳此 <CODE>ColorModel</CODE> 的像素值。
     * @throws UnsupportedOperationException 如果此 <CODE>ComponentColorModel</CODE> 的传输类型不是支持的传输类型之一：
     * <CODE>DataBuffer.TYPE_BYTE</CODE>、<CODE>DataBuffer.TYPE_USHORT</CODE>、<CODE>DataBuffer.TYPE_INT</CODE>、<CODE>DataBuffer.TYPE_SHORT</CODE>、
     * <CODE>DataBuffer.TYPE_FLOAT</CODE> 或 <CODE>DataBuffer.TYPE_DOUBLE</CODE>。
     */
    public int getGreen(Object inData) {
        return getRGBComponent(inData, 1);
    }


    /**
     * 返回指定像素的蓝色颜色分量，缩放范围为 0 到 255，默认的 RGB <CODE>ColorSpace</CODE> 为 sRGB。如果需要，将进行颜色转换。像素值由作为对象引用传递的类型为 <CODE>transferType</CODE> 的数据元素数组指定。
     * 返回的值是一个非预乘值。如果 alpha 是预乘的，此方法会在返回值之前将其除以 alpha（如果 alpha 值为 0，则蓝色值将为 0）。由于 <code>ComponentColorModel</code> 可以被子类化，
     * 子类继承此方法的实现，如果它们不重写此方法并且使用不受支持的 <code>transferType</code>，则会抛出异常。
     *
     * @param inData 您想要从中获取蓝色颜色分量的像素，由类型为 <CODE>transferType</CODE> 的数据元素数组指定。
     *
     * @return 指定像素的蓝色颜色分量，作为 int。
     *
     * @throws ClassCastException 如果 <CODE>inData</CODE> 不是类型为 <CODE>transferType</CODE> 的基本数组。
     * @throws ArrayIndexOutOfBoundsException 如果 <CODE>inData</CODE> 不足以容纳此 <CODE>ColorModel</CODE> 的像素值。
     * @throws UnsupportedOperationException 如果此 <CODE>ComponentColorModel</CODE> 的传输类型不是支持的传输类型之一：
     * <CODE>DataBuffer.TYPE_BYTE</CODE>、<CODE>DataBuffer.TYPE_USHORT</CODE>、<CODE>DataBuffer.TYPE_INT</CODE>、<CODE>DataBuffer.TYPE_SHORT</CODE>、
     * <CODE>DataBuffer.TYPE_FLOAT</CODE> 或 <CODE>DataBuffer.TYPE_DOUBLE</CODE>。
     */
    public int getBlue(Object inData) {
        return getRGBComponent(inData, 2);
    }


                /**
     * 返回指定像素的 alpha 分量，从 0 到 255 缩放。像素值由作为对象引用传递的类型为 <CODE>transferType</CODE> 的数据元素数组指定。由于 <code>ComponentColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不重写此方法且使用了不支持的 <code>transferType</code>，则会抛出异常。
     *
     * @param inData 指定要获取 alpha 分量的像素，由类型为 <CODE>transferType</CODE> 的数据元素数组指定。
     *
     * @return 指定像素的 alpha 分量，作为 int 类型。
     *
     * @throws ClassCastException 如果 <CODE>inData</CODE> 不是类型为 <CODE>transferType</CODE> 的原始数组。
     * @throws ArrayIndexOutOfBoundsException 如果 <CODE>inData</CODE> 不足以容纳此 <CODE>ColorModel</CODE> 的像素值。
     * @throws UnsupportedOperationException 如果此 <CODE>ComponentColorModel</CODE> 的传输类型不是支持的传输类型之一：
     * <CODE>DataBuffer.TYPE_BYTE</CODE>, <CODE>DataBuffer.TYPE_USHORT</CODE>,
     * <CODE>DataBuffer.TYPE_INT</CODE>, <CODE>DataBuffer.TYPE_SHORT</CODE>,
     * <CODE>DataBuffer.TYPE_FLOAT</CODE>, 或 <CODE>DataBuffer.TYPE_DOUBLE</CODE>。
     */
    public int getAlpha(Object inData) {
        if (supportsAlpha == false) {
            return 255;
        }

        int alpha = 0;
        int aIdx = numColorComponents;
        int mask = (1 << nBits[aIdx]) - 1;

        switch (transferType) {
            case DataBuffer.TYPE_SHORT:
                short sdata[] = (short[])inData;
                alpha = (int) ((sdata[aIdx] / 32767.0f) * 255.0f + 0.5f);
                return alpha;
            case DataBuffer.TYPE_FLOAT:
                float fdata[] = (float[])inData;
                alpha = (int) (fdata[aIdx] * 255.0f + 0.5f);
                return alpha;
            case DataBuffer.TYPE_DOUBLE:
                double ddata[] = (double[])inData;
                alpha = (int) (ddata[aIdx] * 255.0 + 0.5);
                return alpha;
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               alpha = bdata[aIdx] & mask;
            break;
            case DataBuffer.TYPE_USHORT:
               short usdata[] = (short[])inData;
               alpha = usdata[aIdx] & mask;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               alpha = idata[aIdx];
            break;
            default:
               throw new
                   UnsupportedOperationException("This method has not "+
                   "been implemented for transferType " + transferType);
        }

        if (nBits[aIdx] == 8) {
            return alpha;
        } else {
            return (int)
                ((((float) alpha) / ((float) ((1 << nBits[aIdx]) - 1))) *
                 255.0f + 0.5f);
        }
    }

    /**
     * 返回指定像素的色彩/alpha 分量，默认为 RGB 色彩模型格式。如果必要，会进行色彩转换。像素值由作为对象引用传递的类型为 <CODE>transferType</CODE> 的数据元素数组指定。返回值为非预乘格式。如果 alpha 是预乘的，此方法会将其从色彩分量中除掉（如果 alpha 值为 0，色彩值将为 0）。由于 <code>ComponentColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不重写此方法且使用了不支持的 <code>transferType</code>，则会抛出异常。
     *
     * @param inData 指定要获取色彩/alpha 分量的像素，由类型为 <CODE>transferType</CODE> 的数据元素数组指定。
     *
     * @return 指定像素的色彩/alpha 分量，作为 int 类型。
     *
     * @throws ClassCastException 如果 <CODE>inData</CODE> 不是类型为 <CODE>transferType</CODE> 的原始数组。
     * @throws ArrayIndexOutOfBoundsException 如果 <CODE>inData</CODE> 不足以容纳此 <CODE>ColorModel</CODE> 的像素值。
     * @throws UnsupportedOperationException 如果此 <CODE>ComponentColorModel</CODE> 的传输类型不是支持的传输类型之一：
     * <CODE>DataBuffer.TYPE_BYTE</CODE>, <CODE>DataBuffer.TYPE_USHORT</CODE>,
     * <CODE>DataBuffer.TYPE_INT</CODE>, <CODE>DataBuffer.TYPE_SHORT</CODE>,
     * <CODE>DataBuffer.TYPE_FLOAT</CODE>, 或 <CODE>DataBuffer.TYPE_DOUBLE</CODE>。
     * @see ColorModel#getRGBdefault
     */
    public int getRGB(Object inData) {
        if (needScaleInit) {
            initScale();
        }
        if (is_sRGB_stdScale || is_LinearRGB_stdScale) {
            return (getAlpha(inData) << 24)
                | (getRed(inData) << 16)
                | (getGreen(inData) << 8)
                | (getBlue(inData));
        } else if (colorSpaceType == ColorSpace.TYPE_GRAY) {
            int gray = getRed(inData); // Red sRGB component should equal
                                       // green and blue components
            return (getAlpha(inData) << 24)
                | (gray << 16)
                | (gray <<  8)
                | gray;
        }
        float[] norm = getNormalizedComponents(inData, null, 0);
        // Note that getNormalizedComponents returns non-premult values
        float[] rgb = colorSpace.toRGB(norm);
        return (getAlpha(inData) << 24)
            | (((int) (rgb[0] * 255.0f + 0.5f)) << 16)
            | (((int) (rgb[1] * 255.0f + 0.5f)) << 8)
            | (((int) (rgb[2] * 255.0f + 0.5f)) << 0);
    }

    /**
     * 返回此 <CODE>ColorModel</CODE> 中像素的数据元素数组表示，给定默认 RGB 色彩模型中的整数像素表示。此数组可以传递给 <CODE>WritableRaster</CODE> 对象的 <CODE>setDataElements</CODE> 方法。如果 <CODE>pixel</CODE> 参数为 null，则分配新数组。由于 <code>ComponentColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不重写此方法且使用了不支持的 <code>transferType</code>，则会抛出异常。
     *
     * @param rgb 在 RGB 色彩模型中的整数像素表示
     * @param pixel 指定的像素
     * @return 此 <CODE>ColorModel</CODE> 中像素的数据元素数组表示。
     * @throws ClassCastException 如果 <CODE>pixel</CODE> 不为 null 且不是类型为 <CODE>transferType</CODE> 的原始数组。
     * @throws ArrayIndexOutOfBoundsException 如果 <CODE>pixel</CODE> 不足以容纳此 <CODE>ColorModel</CODE> 的像素值。
     * @throws UnsupportedOperationException 如果此 <CODE>ComponentColorModel</CODE> 的传输类型不是支持的传输类型之一：
     * <CODE>DataBuffer.TYPE_BYTE</CODE>, <CODE>DataBuffer.TYPE_USHORT</CODE>,
     * <CODE>DataBuffer.TYPE_INT</CODE>, <CODE>DataBuffer.TYPE_SHORT</CODE>,
     * <CODE>DataBuffer.TYPE_FLOAT</CODE>, 或 <CODE>DataBuffer.TYPE_DOUBLE</CODE>。
     *
     * @see WritableRaster#setDataElements
     * @see SampleModel#setDataElements
     */
    public Object getDataElements(int rgb, Object pixel) {
        // REMIND: Use rendering hints?

        int red, grn, blu, alp;
        red = (rgb>>16) & 0xff;
        grn = (rgb>>8) & 0xff;
        blu = rgb & 0xff;

        if (needScaleInit) {
            initScale();
        }
        if (signed) {
            // Handle SHORT, FLOAT, & DOUBLE here

            switch(transferType) {
            case DataBuffer.TYPE_SHORT:
                {
                    short sdata[];
                    if (pixel == null) {
                        sdata = new short[numComponents];
                    } else {
                        sdata = (short[])pixel;
                    }
                    float factor;
                    if (is_sRGB_stdScale || is_LinearRGB_stdScale) {
                        factor = 32767.0f / 255.0f;
                        if (is_LinearRGB_stdScale) {
                            red = fromsRGB8LUT16[red] & 0xffff;
                            grn = fromsRGB8LUT16[grn] & 0xffff;
                            blu = fromsRGB8LUT16[blu] & 0xffff;
                            factor = 32767.0f / 65535.0f;
                        }
                        if (supportsAlpha) {
                            alp = (rgb>>24) & 0xff;
                            sdata[3] =
                                (short) (alp * (32767.0f / 255.0f) + 0.5f);
                            if (isAlphaPremultiplied) {
                                factor = alp * factor * (1.0f / 255.0f);
                            }
                        }
                        sdata[0] = (short) (red * factor + 0.5f);
                        sdata[1] = (short) (grn * factor + 0.5f);
                        sdata[2] = (short) (blu * factor + 0.5f);
                    } else if (is_LinearGray_stdScale) {
                        red = fromsRGB8LUT16[red] & 0xffff;
                        grn = fromsRGB8LUT16[grn] & 0xffff;
                        blu = fromsRGB8LUT16[blu] & 0xffff;
                        float gray = ((0.2125f * red) +
                                      (0.7154f * grn) +
                                      (0.0721f * blu)) / 65535.0f;
                        factor = 32767.0f;
                        if (supportsAlpha) {
                            alp = (rgb>>24) & 0xff;
                            sdata[1] =
                                (short) (alp * (32767.0f / 255.0f) + 0.5f);
                            if (isAlphaPremultiplied) {
                                factor = alp * factor * (1.0f / 255.0f);
                            }
                        }
                        sdata[0] = (short) (gray * factor + 0.5f);
                    } else if (is_ICCGray_stdScale) {
                        red = fromsRGB8LUT16[red] & 0xffff;
                        grn = fromsRGB8LUT16[grn] & 0xffff;
                        blu = fromsRGB8LUT16[blu] & 0xffff;
                        int gray = (int) ((0.2125f * red) +
                                          (0.7154f * grn) +
                                          (0.0721f * blu) + 0.5f);
                        gray = fromLinearGray16ToOtherGray16LUT[gray] & 0xffff;
                        factor = 32767.0f / 65535.0f;
                        if (supportsAlpha) {
                            alp = (rgb>>24) & 0xff;
                            sdata[1] =
                                (short) (alp * (32767.0f / 255.0f) + 0.5f);
                            if (isAlphaPremultiplied) {
                                factor = alp * factor * (1.0f / 255.0f);
                            }
                        }
                        sdata[0] = (short) (gray * factor + 0.5f);
                    } else {
                        factor = 1.0f / 255.0f;
                        float norm[] = new float[3];
                        norm[0] = red * factor;
                        norm[1] = grn * factor;
                        norm[2] = blu * factor;
                        norm = colorSpace.fromRGB(norm);
                        if (nonStdScale) {
                            for (int i = 0; i < numColorComponents; i++) {
                                norm[i] = (norm[i] - compOffset[i]) *
                                          compScale[i];
                                // REMIND: need to analyze whether this
                                // clamping is necessary
                                if (norm[i] < 0.0f) {
                                    norm[i] = 0.0f;
                                }
                                if (norm[i] > 1.0f) {
                                    norm[i] = 1.0f;
                                }
                            }
                        }
                        factor = 32767.0f;
                        if (supportsAlpha) {
                            alp = (rgb>>24) & 0xff;
                            sdata[numColorComponents] =
                                (short) (alp * (32767.0f / 255.0f) + 0.5f);
                            if (isAlphaPremultiplied) {
                                factor *= alp * (1.0f / 255.0f);
                            }
                        }
                        for (int i = 0; i < numColorComponents; i++) {
                            sdata[i] = (short) (norm[i] * factor + 0.5f);
                        }
                    }
                    return sdata;
                }
            case DataBuffer.TYPE_FLOAT:
                {
                    float fdata[];
                    if (pixel == null) {
                        fdata = new float[numComponents];
                    } else {
                        fdata = (float[])pixel;
                    }
                    float factor;
                    if (is_sRGB_stdScale || is_LinearRGB_stdScale) {
                        if (is_LinearRGB_stdScale) {
                            red = fromsRGB8LUT16[red] & 0xffff;
                            grn = fromsRGB8LUT16[grn] & 0xffff;
                            blu = fromsRGB8LUT16[blu] & 0xffff;
                            factor = 1.0f / 65535.0f;
                        } else {
                            factor = 1.0f / 255.0f;
                        }
                        if (supportsAlpha) {
                            alp = (rgb>>24) & 0xff;
                            fdata[3] = alp * (1.0f / 255.0f);
                            if (isAlphaPremultiplied) {
                                factor *= fdata[3];
                            }
                        }
                        fdata[0] = red * factor;
                        fdata[1] = grn * factor;
                        fdata[2] = blu * factor;
                    } else if (is_LinearGray_stdScale) {
                        red = fromsRGB8LUT16[red] & 0xffff;
                        grn = fromsRGB8LUT16[grn] & 0xffff;
                        blu = fromsRGB8LUT16[blu] & 0xffff;
                        fdata[0] = ((0.2125f * red) +
                                    (0.7154f * grn) +
                                    (0.0721f * blu)) / 65535.0f;
                        if (supportsAlpha) {
                            alp = (rgb>>24) & 0xff;
                            fdata[1] = alp * (1.0f / 255.0f);
                            if (isAlphaPremultiplied) {
                                fdata[0] *= fdata[1];
                            }
                        }
                    } else if (is_ICCGray_stdScale) {
                        red = fromsRGB8LUT16[red] & 0xffff;
                        grn = fromsRGB8LUT16[grn] & 0xffff;
                        blu = fromsRGB8LUT16[blu] & 0xffff;
                        int gray = (int) ((0.2125f * red) +
                                          (0.7154f * grn) +
                                          (0.0721f * blu) + 0.5f);
                        fdata[0] = (fromLinearGray16ToOtherGray16LUT[gray] &
                                    0xffff) / 65535.0f;
                        if (supportsAlpha) {
                            alp = (rgb>>24) & 0xff;
                            fdata[1] = alp * (1.0f / 255.0f);
                            if (isAlphaPremultiplied) {
                                fdata[0] *= fdata[1];
                            }
                        }
                    } else {
                        float norm[] = new float[3];
                        factor = 1.0f / 255.0f;
                        norm[0] = red * factor;
                        norm[1] = grn * factor;
                        norm[2] = blu * factor;
                        norm = colorSpace.fromRGB(norm);
                        if (supportsAlpha) {
                            alp = (rgb>>24) & 0xff;
                            fdata[numColorComponents] = alp * factor;
                            if (isAlphaPremultiplied) {
                                factor *= alp;
                                for (int i = 0; i < numColorComponents; i++) {
                                    norm[i] *= factor;
                                }
                            }
                        }
                        for (int i = 0; i < numColorComponents; i++) {
                            fdata[i] = norm[i];
                        }
                    }
                    return fdata;
                }
            case DataBuffer.TYPE_DOUBLE:
                {
                    double ddata[];
                    if (pixel == null) {
                        ddata = new double[numComponents];
                    } else {
                        ddata = (double[])pixel;
                    }
                    if (is_sRGB_stdScale || is_LinearRGB_stdScale) {
                        double factor;
                        if (is_LinearRGB_stdScale) {
                            red = fromsRGB8LUT16[red] & 0xffff;
                            grn = fromsRGB8LUT16[grn] & 0xffff;
                            blu = fromsRGB8LUT16[blu] & 0xffff;
                            factor = 1.0 / 65535.0;
                        } else {
                            factor = 1.0 / 255.0;
                        }
                        if (supportsAlpha) {
                            alp = (rgb>>24) & 0xff;
                            ddata[3] = alp * (1.0 / 255.0);
                            if (isAlphaPremultiplied) {
                                factor *= ddata[3];
                            }
                        }
                        ddata[0] = red * factor;
                        ddata[1] = grn * factor;
                        ddata[2] = blu * factor;
                    } else if (is_LinearGray_stdScale) {
                        red = fromsRGB8LUT16[red] & 0xffff;
                        grn = fromsRGB8LUT16[grn] & 0xffff;
                        blu = fromsRGB8LUT16[blu] & 0xffff;
                        ddata[0] = ((0.2125 * red) +
                                    (0.7154 * grn) +
                                    (0.0721 * blu)) / 65535.0;
                        if (supportsAlpha) {
                            alp = (rgb>>24) & 0xff;
                            ddata[1] = alp * (1.0 / 255.0);
                            if (isAlphaPremultiplied) {
                                ddata[0] *= ddata[1];
                            }
                        }
                    } else if (is_ICCGray_stdScale) {
                        red = fromsRGB8LUT16[red] & 0xffff;
                        grn = fromsRGB8LUT16[grn] & 0xffff;
                        blu = fromsRGB8LUT16[blu] & 0xffff;
                        int gray = (int) ((0.2125f * red) +
                                          (0.7154f * grn) +
                                          (0.0721f * blu) + 0.5f);
                        ddata[0] = (fromLinearGray16ToOtherGray16LUT[gray] &
                                    0xffff) / 65535.0;
                        if (supportsAlpha) {
                            alp = (rgb>>24) & 0xff;
                            ddata[1] = alp * (1.0 / 255.0);
                            if (isAlphaPremultiplied) {
                                ddata[0] *= ddata[1];
                            }
                        }
                    } else {
                        float factor = 1.0f / 255.0f;
                        float norm[] = new float[3];
                        norm[0] = red * factor;
                        norm[1] = grn * factor;
                        norm[2] = blu * factor;
                        norm = colorSpace.fromRGB(norm);
                        if (supportsAlpha) {
                            alp = (rgb>>24) & 0xff;
                            ddata[numColorComponents] = alp * (1.0 / 255.0);
                            if (isAlphaPremultiplied) {
                                factor *= alp;
                                for (int i = 0; i < numColorComponents; i++) {
                                    norm[i] *= factor;
                                }
                            }
                        }
                        for (int i = 0; i < numColorComponents; i++) {
                            ddata[i] = norm[i];
                        }
                    }
                    return ddata;
                }
            }
        }


                    // 处理 BYTE, USHORT, & INT
        // 提醒：可能不使用 int 数组对于
        // DataBuffer.TYPE_USHORT 和 DataBuffer.TYPE_INT 更高效
        int intpixel[];
        if (transferType == DataBuffer.TYPE_INT &&
            pixel != null) {
           intpixel = (int[])pixel;
        } else {
            intpixel = new int[numComponents];
        }

        if (is_sRGB_stdScale || is_LinearRGB_stdScale) {
            int precision;
            float factor;
            if (is_LinearRGB_stdScale) {
                if (transferType == DataBuffer.TYPE_BYTE) {
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
                alp = (rgb>>24)&0xff;
                if (nBits[3] == 8) {
                    intpixel[3] = alp;
                }
                else {
                    intpixel[3] = (int)
                        (alp * (1.0f / 255.0f) * ((1<<nBits[3]) - 1) + 0.5f);
                }
                if (isAlphaPremultiplied) {
                    factor *= (alp * (1.0f / 255.0f));
                    precision = -1;  // 强制组件计算
                }
            }
            if (nBits[0] == precision) {
                intpixel[0] = red;
            }
            else {
                intpixel[0] = (int) (red * factor * ((1<<nBits[0]) - 1) + 0.5f);
            }
            if (nBits[1] == precision) {
                intpixel[1] = (int)(grn);
            }
            else {
                intpixel[1] = (int) (grn * factor * ((1<<nBits[1]) - 1) + 0.5f);
            }
            if (nBits[2] == precision) {
                intpixel[2] = (int)(blu);
            }
            else {
                intpixel[2] = (int) (blu * factor * ((1<<nBits[2]) - 1) + 0.5f);
            }
        } else if (is_LinearGray_stdScale) {
            red = fromsRGB8LUT16[red] & 0xffff;
            grn = fromsRGB8LUT16[grn] & 0xffff;
            blu = fromsRGB8LUT16[blu] & 0xffff;
            float gray = ((0.2125f * red) +
                          (0.7154f * grn) +
                          (0.0721f * blu)) / 65535.0f;
            if (supportsAlpha) {
                alp = (rgb>>24) & 0xff;
                if (nBits[1] == 8) {
                    intpixel[1] = alp;
                } else {
                    intpixel[1] = (int) (alp * (1.0f / 255.0f) *
                                         ((1 << nBits[1]) - 1) + 0.5f);
                }
                if (isAlphaPremultiplied) {
                    gray *= (alp * (1.0f / 255.0f));
                }
            }
            intpixel[0] = (int) (gray * ((1 << nBits[0]) - 1) + 0.5f);
        } else if (is_ICCGray_stdScale) {
            red = fromsRGB8LUT16[red] & 0xffff;
            grn = fromsRGB8LUT16[grn] & 0xffff;
            blu = fromsRGB8LUT16[blu] & 0xffff;
            int gray16 = (int) ((0.2125f * red) +
                                (0.7154f * grn) +
                                (0.0721f * blu) + 0.5f);
            float gray = (fromLinearGray16ToOtherGray16LUT[gray16] &
                          0xffff) / 65535.0f;
            if (supportsAlpha) {
                alp = (rgb>>24) & 0xff;
                if (nBits[1] == 8) {
                    intpixel[1] = alp;
                } else {
                    intpixel[1] = (int) (alp * (1.0f / 255.0f) *
                                         ((1 << nBits[1]) - 1) + 0.5f);
                }
                if (isAlphaPremultiplied) {
                    gray *= (alp * (1.0f / 255.0f));
                }
            }
            intpixel[0] = (int) (gray * ((1 << nBits[0]) - 1) + 0.5f);
        } else {
            // 需要转换颜色
            float[] norm = new float[3];
            float factor = 1.0f / 255.0f;
            norm[0] = red * factor;
            norm[1] = grn * factor;
            norm[2] = blu * factor;
            norm = colorSpace.fromRGB(norm);
            if (nonStdScale) {
                for (int i = 0; i < numColorComponents; i++) {
                    norm[i] = (norm[i] - compOffset[i]) *
                              compScale[i];
                    // 提醒：需要分析是否需要此
                    // 钳制
                    if (norm[i] < 0.0f) {
                        norm[i] = 0.0f;
                    }
                    if (norm[i] > 1.0f) {
                        norm[i] = 1.0f;
                    }
                }
            }
            if (supportsAlpha) {
                alp = (rgb>>24) & 0xff;
                if (nBits[numColorComponents] == 8) {
                    intpixel[numColorComponents] = alp;
                }
                else {
                    intpixel[numColorComponents] =
                        (int) (alp * factor *
                               ((1<<nBits[numColorComponents]) - 1) + 0.5f);
                }
                if (isAlphaPremultiplied) {
                    factor *= alp;
                    for (int i = 0; i < numColorComponents; i++) {
                        norm[i] *= factor;
                    }
                }
            }
            for (int i = 0; i < numColorComponents; i++) {
                intpixel[i] = (int) (norm[i] * ((1<<nBits[i]) - 1) + 0.5f);
            }
        }

        switch (transferType) {
            case DataBuffer.TYPE_BYTE: {
               byte bdata[];
               if (pixel == null) {
                   bdata = new byte[numComponents];
               } else {
                   bdata = (byte[])pixel;
               }
               for (int i = 0; i < numComponents; i++) {
                   bdata[i] = (byte)(0xff&intpixel[i]);
               }
               return bdata;
            }
            case DataBuffer.TYPE_USHORT:{
               short sdata[];
               if (pixel == null) {
                   sdata = new short[numComponents];
               } else {
                   sdata = (short[])pixel;
               }
               for (int i = 0; i < numComponents; i++) {
                   sdata[i] = (short)(intpixel[i]&0xffff);
               }
               return sdata;
            }
            case DataBuffer.TYPE_INT:
                if (maxBits > 23) {
                    // 修复 4412670 - 对于 24 位或更多位的组件
                    // 使用浮点精度进行的一些计算
                    // 可能会丢失足够的精度，导致整数结果
                    // 溢出 nBits，因此需要钳制。
                    for (int i = 0; i < numComponents; i++) {
                        if (intpixel[i] > ((1<<nBits[i]) - 1)) {
                            intpixel[i] = (1<<nBits[i]) - 1;
                        }
                    }
                }
                return intpixel;
        }
        throw new IllegalArgumentException("此方法尚未实现 transferType " + transferType);
    }

   /** 返回给定此 <CODE>ColorModel</CODE> 像素的未归一化颜色/alpha 组件数组。
     * 如果此 <CODE>ColorModel</CODE> 的组件值不能方便地表示为未归一化形式，则抛出 IllegalArgumentException。颜色/alpha 组件存储在
     * <CODE>components</CODE> 数组中，从 <CODE>offset</CODE> 开始（即使此数组是由此方法分配的）。
     *
     * @param pixel 以整数指定的像素值。
     * @param components 用于存储未归一化颜色/alpha 组件的整数数组。如果 <CODE>components</CODE> 数组为 null，则分配新数组。
     * @param offset <CODE>components</CODE> 数组中的偏移量。
     *
     * @return 组件数组。
     *
     * @throws IllegalArgumentException 如果此 <CODE>ColorModel</CODE> 的组件数大于 1。
     * @throws IllegalArgumentException 如果此 <CODE>ColorModel</CODE> 不支持未归一化形式。
     * @throws ArrayIndexOutOfBoundsException 如果 <CODE>components</CODE> 数组不为 null 且不足以容纳所有颜色和 alpha 组件（从偏移量开始）。
     */
    public int[] getComponents(int pixel, int[] components, int offset) {
        if (numComponents > 1) {
            throw new
                IllegalArgumentException("每个像素的组件数超过 1");
        }
        if (needScaleInit) {
            initScale();
        }
        if (noUnnorm) {
            throw new
                IllegalArgumentException(
                    "此 ColorModel 不支持未归一化形式");
        }
        if (components == null) {
            components = new int[offset+1];
        }

        components[offset+0] = (pixel & ((1<<nBits[0]) - 1));
        return components;
    }

    /**
     * 返回给定此 <CODE>ColorModel</CODE> 像素的未归一化颜色/alpha 组件数组。像素值由作为对象引用传递的
     * <CODE>transferType</CODE> 类型的数据元素数组指定。
     * 如果此 <CODE>ColorModel</CODE> 的组件值不能方便地表示为未归一化形式，则抛出 IllegalArgumentException。颜色/alpha 组件存储在
     * <CODE>components</CODE> 数组中，从 <CODE>offset</CODE> 开始（即使此数组是由此方法分配的）。由于 <code>ComponentColorModel</code> 可以被子类化，子类继承了
     * 此方法的实现，如果它们不重写此方法并且使用不受支持的 <code>transferType</code>，则此方法可能会抛出异常。
     *
     * @param pixel 由 <CODE>transferType</CODE> 类型的数据元素数组指定的像素值。
     * @param components 用于存储未归一化颜色/alpha 组件的整数数组。如果 <CODE>components</CODE> 数组为 null，则分配新数组。
     * @param offset <CODE>components</CODE> 数组中的偏移量。
     *
     * @return <CODE>components</CODE> 数组。
     *
     * @throws IllegalArgumentException 如果此 <CODE>ComponentColorModel</CODE> 不支持未归一化形式。
     * @throws UnsupportedOperationException 在某些情况下，如果此 <CODE>ComponentColorModel</CODE> 的传输类型不是以下传输类型之一：
     * <CODE>DataBuffer.TYPE_BYTE</CODE>、<CODE>DataBuffer.TYPE_USHORT</CODE> 或 <CODE>DataBuffer.TYPE_INT</CODE>。
     * @throws ClassCastException 如果 <CODE>pixel</CODE> 不是 <CODE>transferType</CODE> 类型的原始数组。
     * @throws IllegalArgumentException 如果 <CODE>components</CODE> 数组不为 null 且不足以容纳所有颜色和 alpha 组件（从偏移量开始），或者 <CODE>pixel</CODE> 不足以容纳此 ColorModel 的像素值。
     */
    public int[] getComponents(Object pixel, int[] components, int offset) {
        int intpixel[];
        if (needScaleInit) {
            initScale();
        }
        if (noUnnorm) {
            throw new
                IllegalArgumentException(
                    "此 ColorModel 不支持未归一化形式");
        }
        if (pixel instanceof int[]) {
            intpixel = (int[])pixel;
        } else {
            intpixel = DataBuffer.toIntArray(pixel);
            if (intpixel == null) {
               throw new UnsupportedOperationException("此方法尚未实现 transferType " + transferType);
            }
        }
        if (intpixel.length < numComponents) {
            throw new IllegalArgumentException
                ("像素数组长度 < 模型中的组件数");
        }
        if (components == null) {
            components = new int[offset+numComponents];
        }
        else if ((components.length-offset) < numComponents) {
            throw new IllegalArgumentException
                ("components 数组长度 < 模型中的组件数");
        }
        System.arraycopy(intpixel, 0, components, offset, numComponents);

        return components;
    }

    /**
     * 返回给定归一化组件数组的所有颜色/alpha 组件的未归一化形式。未归一化组件是 0 到 2<sup>n</sup> - 1 之间的无符号整数值，其中
     * n 是特定组件的位数。归一化组件是 <code>ColorSpace</code> 对象为此 <code>ColorModel</code> 指定的每个组件的最小值和最大值之间的浮点值。如果
     * <code>ColorModel</code> 的颜色组件值不能方便地表示为未归一化形式，则抛出 <code>IllegalArgumentException</code>。如果
     * <code>components</code> 数组为 <code>null</code>，则分配新数组。将返回 <code>components</code> 数组。颜色/alpha 组件存储在
     * <code>components</code> 数组中，从 <code>offset</code> 开始（即使此数组是由此方法分配的）。如果
     * <code>components</code> 数组不为 <code>null</code> 且不足以容纳所有颜色和 alpha
     * 组件（从 <code>offset</code> 开始），则抛出 <code>ArrayIndexOutOfBoundsException</code>。如果
     * <code>normComponents</code> 数组不足以容纳从
     * <code>normOffset</code> 开始的所有颜色和 alpha 组件，则抛出 <code>IllegalArgumentException</code>。
     * @param normComponents 包含归一化组件的数组
     * @param normOffset <code>normComponents</code> 数组中开始检索归一化组件的偏移量
     * @param components 从 <code>normComponents</code> 接收组件的数组
     * @param offset <code>components</code> 中开始存储从 <code>normComponents</code> 获取的归一化组件的索引
     * @return 包含未归一化颜色和 alpha 组件的数组。
     * @throws IllegalArgumentException 如果此 <CODE>ComponentColorModel</CODE> 不支持未归一化形式。
     * @throws IllegalArgumentException 如果 <code>normComponents</code> 的长度减去 <code>normOffset</code>
     * 小于 <code>numComponents</code>。
     */
    public int[] getUnnormalizedComponents(float[] normComponents,
                                           int normOffset,
                                           int[] components, int offset) {
        if (needScaleInit) {
            initScale();
        }
        if (noUnnorm) {
            throw new
                IllegalArgumentException(
                    "此 ColorModel 不支持未归一化形式");
        }
        return super.getUnnormalizedComponents(normComponents, normOffset,
                                               components, offset);
    }


                /**
     * 返回一个包含所有颜色/透明度组件的归一化形式的数组，给定一个未归一化的组件数组。未归一化的组件是介于0和2<sup>n</sup> - 1之间的无符号整数值，其中n是特定组件的位数。归一化的组件是介于<code>ColorSpace</code>对象为该<code>ColorModel</code>指定的每个组件的最小值和最大值之间的浮点值。如果此<code>ColorModel</code>的组件值不能方便地表示为未归一化形式，则将抛出<code>IllegalArgumentException</code>。如果<code>normComponents</code>数组为<code>null</code>，将分配一个新的数组。<code>normComponents</code>数组将被返回。颜色/透明度组件将从<code>normOffset</code>开始存储在<code>normComponents</code>数组中（即使该数组是由此方法分配的）。如果<code>normComponents</code>数组不为<code>null</code>且不足以容纳所有颜色和透明度组件（从<code>normOffset</code>开始），则将抛出<code>ArrayIndexOutOfBoundsException</code>。如果<code>components</code>数组不足以容纳从<code>offset</code>开始的所有颜色和透明度组件，则将抛出<code>IllegalArgumentException</code>。
     * @param components 包含未归一化组件的数组
     * @param offset <code>components</code>数组中开始检索未归一化组件的偏移量
     * @param normComponents 接收归一化组件的数组
     * @param normOffset <code>normComponents</code>中开始存储归一化组件的索引
     * @return 包含归一化颜色和透明度组件的数组。
     * @throws IllegalArgumentException 如果此<code>ComponentColorModel</code>不支持未归一化形式
     */
    public float[] getNormalizedComponents(int[] components, int offset,
                                           float[] normComponents,
                                           int normOffset) {
        if (needScaleInit) {
            initScale();
        }
        if (noUnnorm) {
            throw new
                IllegalArgumentException(
                    "This ColorModel does not support the unnormalized form");
        }
        return super.getNormalizedComponents(components, offset,
                                             normComponents, normOffset);
    }

    /**
     * 返回此<CODE>ColorModel</CODE>中的一个像素值，表示为一个int，给定一个未归一化的颜色/透明度组件数组。
     *
     * @param components 一个包含未归一化的颜色/透明度组件的数组。
     * @param offset <CODE>components</CODE>数组中的偏移量。
     *
     * @return 一个表示为int的像素值。
     *
     * @throws IllegalArgumentException 如果此<CODE>ColorModel</CODE>中包含的组件多于一个。
     * @throws IllegalArgumentException 如果此<CODE>ComponentColorModel</CODE>不支持未归一化形式
     */
    public int getDataElement(int[] components, int offset) {
        if (needScaleInit) {
            initScale();
        }
        if (numComponents == 1) {
            if (noUnnorm) {
                throw new
                    IllegalArgumentException(
                    "This ColorModel does not support the unnormalized form");
            }
            return components[offset+0];
        }
        throw new IllegalArgumentException("This model returns "+
                                           numComponents+
                                           " elements in the pixel array.");
    }

    /**
     * 返回此<CODE>ColorModel</CODE>中的一个像素值的数据元素数组表示，给定一个未归一化的颜色/透明度组件数组。此数组可以传递给<CODE>WritableRaster</CODE>对象的<CODE>setDataElements</CODE>方法。
     *
     * @param components 一个包含未归一化的颜色/透明度组件的数组。
     * @param offset <CODE>components</CODE>数组中的偏移量。
     * @param obj 存储像素的数据元素数组表示的对象。如果<CODE>obj</CODE>变量为null，则分配一个新的数组。如果<CODE>obj</CODE>不为null，则必须是类型为<CODE>transferType</CODE>的原始数组。如果<CODE>obj</CODE>不足以容纳此<CODE>ColorModel</CODE>的像素值，则将抛出<CODE>ArrayIndexOutOfBoundsException</CODE>。由于<code>ComponentColorModel</code>可以被子类化，子类继承此方法的实现，如果不覆盖它，当它们使用不受支持的<code>transferType</code>时，将抛出异常。
     *
     * @return 此<CODE>ColorModel</CODE>中的像素值的数据元素数组表示。
     *
     * @throws IllegalArgumentException 如果组件数组不足以容纳从偏移量开始的所有颜色和透明度组件。
     * @throws ClassCastException 如果<CODE>obj</CODE>不为null且不是类型为<CODE>transferType</CODE>的原始数组。
     * @throws ArrayIndexOutOfBoundsException 如果<CODE>obj</CODE>不足以容纳此<CODE>ColorModel</CODE>的像素值。
     * @throws IllegalArgumentException 如果此<CODE>ComponentColorModel</CODE>不支持未归一化形式
     * @throws UnsupportedOperationException 如果此<CODE>ComponentColorModel</CODE>的传输类型不是以下传输类型之一：<CODE>DataBuffer.TYPE_BYTE</CODE>、<CODE>DataBuffer.TYPE_USHORT</CODE>或<CODE>DataBuffer.TYPE_INT</CODE>。
     *
     * @see WritableRaster#setDataElements
     * @see SampleModel#setDataElements
     */
    public Object getDataElements(int[] components, int offset, Object obj) {
        if (needScaleInit) {
            initScale();
        }
        if (noUnnorm) {
            throw new
                IllegalArgumentException(
                    "This ColorModel does not support the unnormalized form");
        }
        if ((components.length-offset) < numComponents) {
            throw new IllegalArgumentException("Component array too small"+
                                               " (should be "+numComponents);
        }
        switch(transferType) {
        case DataBuffer.TYPE_INT:
            {
                int[] pixel;
                if (obj == null) {
                    pixel = new int[numComponents];
                }
                else {
                    pixel = (int[]) obj;
                }
                System.arraycopy(components, offset, pixel, 0,
                                 numComponents);
                return pixel;
            }

        case DataBuffer.TYPE_BYTE:
            {
                byte[] pixel;
                if (obj == null) {
                    pixel = new byte[numComponents];
                }
                else {
                    pixel = (byte[]) obj;
                }
                for (int i=0; i < numComponents; i++) {
                    pixel[i] = (byte) (components[offset+i]&0xff);
                }
                return pixel;
            }

        case DataBuffer.TYPE_USHORT:
            {
                short[] pixel;
                if (obj == null) {
                    pixel = new short[numComponents];
                }
                else {
                    pixel = (short[]) obj;
                }
                for (int i=0; i < numComponents; i++) {
                    pixel[i] = (short) (components[offset+i]&0xffff);
                }
                return pixel;
            }

        default:
            throw new UnsupportedOperationException("This method has not been "+
                                        "implemented for transferType " +
                                        transferType);
        }
    }

    /**
     * 返回此<code>ColorModel</code>中的一个像素值，表示为一个<code>int</code>，给定一个归一化的颜色/透明度组件数组。如果此<code>ColorModel</code>的像素值不能方便地表示为一个<code>int</code>，则将抛出<code>IllegalArgumentException</code>。如果<code>normComponents</code>数组不足以容纳从<code>normOffset</code>开始的所有颜色和透明度组件，则将抛出<code>ArrayIndexOutOfBoundsException</code>。
     * @param normComponents 包含归一化的颜色和透明度组件的数组
     * @param normOffset <code>normComponents</code>中开始检索颜色和透明度组件的索引
     * @return 一个<code>int</code>像素值，对应于指定的组件。
     * @throws IllegalArgumentException 如果此<code>ColorModel</code>的像素值不能方便地表示为一个<code>int</code>
     * @throws ArrayIndexOutOfBoundsException 如果<code>normComponents</code>数组不足以容纳从<code>normOffset</code>开始的所有颜色和透明度组件
     * @since 1.4
     */
    public int getDataElement(float[] normComponents, int normOffset) {
        if (numComponents > 1) {
            throw new
                IllegalArgumentException("More than one component per pixel");
        }
        if (signed) {
            throw new
                IllegalArgumentException("Component value is signed");
        }
        if (needScaleInit) {
            initScale();
        }
        Object pixel = getDataElements(normComponents, normOffset, null);
        switch (transferType) {
        case DataBuffer.TYPE_BYTE:
            {
                byte bpixel[] = (byte[]) pixel;
                return bpixel[0] & 0xff;
            }
        case DataBuffer.TYPE_USHORT:
            {
                short[] uspixel = (short[]) pixel;
                return uspixel[0] & 0xffff;
            }
        case DataBuffer.TYPE_INT:
            {
                int[] ipixel = (int[]) pixel;
                return ipixel[0];
            }
        default:
            throw new UnsupportedOperationException("This method has not been "
                + "implemented for transferType " + transferType);
        }
    }

    /**
     * 返回此<code>ColorModel</code>中的一个像素值的数据元素数组表示，给定一个归一化的颜色/透明度组件数组。此数组可以传递给<code>WritableRaster</code>对象的<code>setDataElements</code>方法。如果<code>normComponents</code>数组不足以容纳从<code>normOffset</code>开始的所有颜色和透明度组件，则将抛出<code>ArrayIndexOutOfBoundsException</code>。如果<code>obj</code>变量为<code>null</code>，将分配一个新的数组。如果<code>obj</code>不为<code>null</code>，则必须是类型为<code>transferType</code>的原始数组；否则，将抛出<code>ClassCastException</code>。如果<code>obj</code>不足以容纳此<code>ColorModel</code>的像素值，则将抛出<code>ArrayIndexOutOfBoundsException</code>。
     * @param normComponents 包含归一化的颜色和透明度组件的数组
     * @param normOffset <code>normComponents</code>中开始检索颜色和透明度组件的索引
     * @param obj 存储返回的像素的原始数据数组
     * @return 一个原始数据数组表示的像素
     * @throws ClassCastException 如果<code>obj</code>不是类型为<code>transferType</code>的原始数组
     * @throws ArrayIndexOutOfBoundsException 如果<code>obj</code>不足以容纳此<code>ColorModel</code>的像素值或<code>normComponents</code>数组不足以容纳从<code>normOffset</code>开始的所有颜色和透明度组件
     * @see WritableRaster#setDataElements
     * @see SampleModel#setDataElements
     * @since 1.4
     */
    public Object getDataElements(float[] normComponents, int normOffset,
                                  Object obj) {
        boolean needAlpha = supportsAlpha && isAlphaPremultiplied;
        float[] stdNormComponents;
        if (needScaleInit) {
            initScale();
        }
        if (nonStdScale) {
            stdNormComponents = new float[numComponents];
            for (int c = 0, nc = normOffset; c < numColorComponents;
                 c++, nc++) {
                stdNormComponents[c] = (normComponents[nc] - compOffset[c]) *
                                       compScale[c];
                // REMIND: need to analyze whether this
                // clamping is necessary
                if (stdNormComponents[c] < 0.0f) {
                    stdNormComponents[c] = 0.0f;
                }
                if (stdNormComponents[c] > 1.0f) {
                    stdNormComponents[c] = 1.0f;
                }
            }
            if (supportsAlpha) {
                stdNormComponents[numColorComponents] =
                    normComponents[numColorComponents + normOffset];
            }
            normOffset = 0;
        } else {
            stdNormComponents = normComponents;
        }
        switch (transferType) {
        case DataBuffer.TYPE_BYTE:
            byte[] bpixel;
            if (obj == null) {
                bpixel = new byte[numComponents];
            } else {
                bpixel = (byte[]) obj;
            }
            if (needAlpha) {
                float alpha =
                    stdNormComponents[numColorComponents + normOffset];
                for (int c = 0, nc = normOffset; c < numColorComponents;
                     c++, nc++) {
                    bpixel[c] = (byte) ((stdNormComponents[nc] * alpha) *
                                        ((float) ((1 << nBits[c]) - 1)) + 0.5f);
                }
                bpixel[numColorComponents] =
                    (byte) (alpha *
                            ((float) ((1 << nBits[numColorComponents]) - 1)) +
                            0.5f);
            } else {
                for (int c = 0, nc = normOffset; c < numComponents;
                     c++, nc++) {
                    bpixel[c] = (byte) (stdNormComponents[nc] *
                                        ((float) ((1 << nBits[c]) - 1)) + 0.5f);
                }
            }
            return bpixel;
        case DataBuffer.TYPE_USHORT:
            short[] uspixel;
            if (obj == null) {
                uspixel = new short[numComponents];
            } else {
                uspixel = (short[]) obj;
            }
            if (needAlpha) {
                float alpha =
                    stdNormComponents[numColorComponents + normOffset];
                for (int c = 0, nc = normOffset; c < numColorComponents;
                     c++, nc++) {
                    uspixel[c] = (short) ((stdNormComponents[nc] * alpha) *
                                          ((float) ((1 << nBits[c]) - 1)) +
                                          0.5f);
                }
                uspixel[numColorComponents] =
                    (short) (alpha *
                             ((float) ((1 << nBits[numColorComponents]) - 1)) +
                             0.5f);
            } else {
                for (int c = 0, nc = normOffset; c < numComponents;
                     c++, nc++) {
                    uspixel[c] = (short) (stdNormComponents[nc] *
                                          ((float) ((1 << nBits[c]) - 1)) +
                                          0.5f);
                }
            }
            return uspixel;
        case DataBuffer.TYPE_INT:
            int[] ipixel;
            if (obj == null) {
                ipixel = new int[numComponents];
            } else {
                ipixel = (int[]) obj;
            }
            if (needAlpha) {
                float alpha =
                    stdNormComponents[numColorComponents + normOffset];
                for (int c = 0, nc = normOffset; c < numColorComponents;
                     c++, nc++) {
                    ipixel[c] = (int) ((stdNormComponents[nc] * alpha) *
                                       ((float) ((1 << nBits[c]) - 1)) + 0.5f);
                }
                ipixel[numColorComponents] =
                    (int) (alpha *
                           ((float) ((1 << nBits[numColorComponents]) - 1)) +
                           0.5f);
            } else {
                for (int c = 0, nc = normOffset; c < numComponents;
                     c++, nc++) {
                    ipixel[c] = (int) (stdNormComponents[nc] *
                                       ((float) ((1 << nBits[c]) - 1)) + 0.5f);
                }
            }
            return ipixel;
        case DataBuffer.TYPE_SHORT:
            short[] spixel;
            if (obj == null) {
                spixel = new short[numComponents];
            } else {
                spixel = (short[]) obj;
            }
            if (needAlpha) {
                float alpha =
                    stdNormComponents[numColorComponents + normOffset];
                for (int c = 0, nc = normOffset; c < numColorComponents;
                     c++, nc++) {
                    spixel[c] = (short)
                        (stdNormComponents[nc] * alpha * 32767.0f + 0.5f);
                }
                spixel[numColorComponents] = (short) (alpha * 32767.0f + 0.5f);
            } else {
                for (int c = 0, nc = normOffset; c < numComponents;
                     c++, nc++) {
                    spixel[c] = (short)
                        (stdNormComponents[nc] * 32767.0f + 0.5f);
                }
            }
            return spixel;
        case DataBuffer.TYPE_FLOAT:
            float[] fpixel;
            if (obj == null) {
                fpixel = new float[numComponents];
            } else {
                fpixel = (float[]) obj;
            }
            if (needAlpha) {
                float alpha = normComponents[numColorComponents + normOffset];
                for (int c = 0, nc = normOffset; c < numColorComponents;
                     c++, nc++) {
                    fpixel[c] = normComponents[nc] * alpha;
                }
                fpixel[numColorComponents] = alpha;
            } else {
                for (int c = 0, nc = normOffset; c < numComponents;
                     c++, nc++) {
                    fpixel[c] = normComponents[nc];
                }
            }
            return fpixel;
        case DataBuffer.TYPE_DOUBLE:
            double[] dpixel;
            if (obj == null) {
                dpixel = new double[numComponents];
            } else {
                dpixel = (double[]) obj;
            }
            if (needAlpha) {
                double alpha =
                    (double) (normComponents[numColorComponents + normOffset]);
                for (int c = 0, nc = normOffset; c < numColorComponents;
                     c++, nc++) {
                    dpixel[c] = normComponents[nc] * alpha;
                }
                dpixel[numColorComponents] = alpha;
            } else {
                for (int c = 0, nc = normOffset; c < numComponents;
                     c++, nc++) {
                    dpixel[c] = (double) normComponents[nc];
                }
            }
            return dpixel;
        default:
            throw new UnsupportedOperationException("This method has not been "+
                                        "implemented for transferType " +
                                        transferType);
        }
    }


                /**
     * 返回此 <code>ColorModel</code> 中所有颜色/透明度分量的归一化形式的数组。像素值由作为对象引用传递的类型为 transferType 的数据元素数组指定。如果像素不是类型为 transferType 的基本数组，则抛出 <code>ClassCastException</code>。如果 <code>pixel</code> 不足以容纳此 <code>ColorModel</code> 的像素值，则抛出 <code>ArrayIndexOutOfBoundsException</code>。归一化分量是介于 <code>ColorSpace</code> 对象为该 <code>ColorModel</code> 指定的每个分量的最小值和最大值之间的浮点值。如果 <code>normComponents</code> 数组为 <code>null</code>，则将分配一个新数组。将返回 <code>normComponents</code> 数组。颜色/透明度分量从 <code>normOffset</code> 开始存储在 <code>normComponents</code> 数组中（即使该数组是由此方法分配的）。如果 <code>normComponents</code> 数组不为 <code>null</code> 且不足以容纳所有颜色和透明度分量（从 <code>normOffset</code> 开始），则抛出 <code>ArrayIndexOutOfBoundsException</code>。
     * <p>
     * 如果子类设计为以非默认方式将像素样本值转换为颜色分量值，则必须重写此方法。此类实现的默认转换在类注释中描述。任何实现非默认转换的子类必须遵循在那里定义的允许转换的约束。
     * @param pixel 指定的像素
     * @param normComponents 接收归一化分量的数组
     * @param normOffset <code>normComponents</code> 数组中开始存储归一化分量的位置
     * @return 包含归一化颜色和透明度分量的数组。
     * @throws ClassCastException 如果 <code>pixel</code> 不是类型为 transferType 的基本数组
     * @throws ArrayIndexOutOfBoundsException 如果 <code>normComponents</code> 数组不足以容纳所有颜色和透明度分量（从 <code>normOffset</code> 开始）
     * @throws ArrayIndexOutOfBoundsException 如果 <code>pixel</code> 不足以容纳此 <code>ColorModel</code> 的像素值。
     * @since 1.4
     */
    public float[] getNormalizedComponents(Object pixel,
                                           float[] normComponents,
                                           int normOffset) {
        if (normComponents == null) {
            normComponents = new float[numComponents+normOffset];
        }
        switch (transferType) {
        case DataBuffer.TYPE_BYTE:
            byte[] bpixel = (byte[]) pixel;
            for (int c = 0, nc = normOffset; c < numComponents; c++, nc++) {
                normComponents[nc] = ((float) (bpixel[c] & 0xff)) /
                                     ((float) ((1 << nBits[c]) - 1));
            }
            break;
        case DataBuffer.TYPE_USHORT:
            short[] uspixel = (short[]) pixel;
            for (int c = 0, nc = normOffset; c < numComponents; c++, nc++) {
                normComponents[nc] = ((float) (uspixel[c] & 0xffff)) /
                                     ((float) ((1 << nBits[c]) - 1));
            }
            break;
        case DataBuffer.TYPE_INT:
            int[] ipixel = (int[]) pixel;
            for (int c = 0, nc = normOffset; c < numComponents; c++, nc++) {
                normComponents[nc] = ((float) ipixel[c]) /
                                     ((float) ((1 << nBits[c]) - 1));
            }
            break;
        case DataBuffer.TYPE_SHORT:
            short[] spixel = (short[]) pixel;
            for (int c = 0, nc = normOffset; c < numComponents; c++, nc++) {
                normComponents[nc] = ((float) spixel[c]) / 32767.0f;
            }
            break;
        case DataBuffer.TYPE_FLOAT:
            float[] fpixel = (float[]) pixel;
            for (int c = 0, nc = normOffset; c < numComponents; c++, nc++) {
                normComponents[nc] = fpixel[c];
            }
            break;
        case DataBuffer.TYPE_DOUBLE:
            double[] dpixel = (double[]) pixel;
            for (int c = 0, nc = normOffset; c < numComponents; c++, nc++) {
                normComponents[nc] = (float) dpixel[c];
            }
            break;
        default:
            throw new UnsupportedOperationException("This method has not been "+
                                        "implemented for transferType " +
                                        transferType);
        }

        if (supportsAlpha && isAlphaPremultiplied) {
            float alpha = normComponents[numColorComponents + normOffset];
            if (alpha != 0.0f) {
                float invAlpha = 1.0f / alpha;
                for (int c = normOffset; c < numColorComponents + normOffset;
                     c++) {
                    normComponents[c] *= invAlpha;
                }
            }
        }
        if (min != null) {
            // 通常（即当此类未被子类重写以覆盖此方法时），(min != null) 测试等同于 (nonStdScale) 测试。然而，有一个不太可能但可能的情况，即此方法被重写，nonStdScale 由 initScale() 设置为 true，子类方法出于某种原因调用此超类方法，但 min 和 diffMinMax 数组从未由 setupLUTs() 初始化。在这种情况下，正确的做法是遵循此方法的预期语义，仅在 setupLUTs() 检测到颜色空间的最小值/最大值不是 0.0/1.0 时重新缩放颜色分量。注意，这意味着传输类型为 byte、ushort、int 或 short - 即从 float 和 double 像素数据派生的分量从不重新缩放。
            for (int c = 0; c < numColorComponents; c++) {
                normComponents[c + normOffset] = min[c] +
                    diffMinMax[c] * normComponents[c + normOffset];
            }
        }
        return normComponents;
    }

    /**
     * 强制光栅数据与 <CODE>isAlphaPremultiplied</CODE> 变量指定的状态匹配，假设数据当前正确描述为此 <CODE>ColorModel</CODE>。它可能会乘以或除以颜色光栅数据的 alpha，或者如果数据处于正确状态，则什么也不做。如果需要强制数据，此方法还将返回一个此 <CODE>ColorModel</CODE> 的实例，其 <CODE>isAlphaPremultiplied</CODE> 标志设置为适当的值。由于 <code>ColorModel</code> 可以被子类化，子类继承此方法的实现，如果它们不重写它，则在使用不支持的 <code>transferType</code> 时会抛出异常。
     *
     * @throws NullPointerException 如果 <code>raster</code> 为 <code>null</code> 且需要数据强制。
     * @throws UnsupportedOperationException 如果此 <CODE>ComponentColorModel</CODE> 的传输类型不是支持的传输类型之一：<CODE>DataBuffer.TYPE_BYTE</CODE>、<CODE>DataBuffer.TYPE_USHORT</CODE>、<CODE>DataBuffer.TYPE_INT</CODE>、<CODE>DataBuffer.TYPE_SHORT</CODE>、<CODE>DataBuffer.TYPE_FLOAT</CODE> 或 <CODE>DataBuffer.TYPE_DOUBLE</CODE>。
     */
    public ColorModel coerceData (WritableRaster raster,
                                  boolean isAlphaPremultiplied) {
        if ((supportsAlpha == false) ||
            (this.isAlphaPremultiplied == isAlphaPremultiplied))
        {
            // 无需操作
            return this;
        }

        int w = raster.getWidth();
        int h = raster.getHeight();
        int aIdx = raster.getNumBands() - 1;
        float normAlpha;
        int rminX = raster.getMinX();
        int rY = raster.getMinY();
        int rX;
        if (isAlphaPremultiplied) {
            switch (transferType) {
                case DataBuffer.TYPE_BYTE: {
                    byte pixel[] = null;
                    byte zpixel[] = null;
                    float alphaScale = 1.0f / ((float) ((1<<nBits[aIdx]) - 1));
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = (byte[])raster.getDataElements(rX, rY,
                                                                   pixel);
                            normAlpha = (pixel[aIdx] & 0xff) * alphaScale;
                            if (normAlpha != 0.0f) {
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (byte)((pixel[c] & 0xff) *
                                                      normAlpha + 0.5f);
                                }
                                raster.setDataElements(rX, rY, pixel);
                            } else {
                                if (zpixel == null) {
                                    zpixel = new byte[numComponents];
                                    java.util.Arrays.fill(zpixel, (byte) 0);
                                }
                                raster.setDataElements(rX, rY, zpixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_USHORT: {
                    short pixel[] = null;
                    short zpixel[] = null;
                    float alphaScale = 1.0f / ((float) ((1<<nBits[aIdx]) - 1));
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = (short[])raster.getDataElements(rX, rY,
                                                                    pixel);
                            normAlpha = (pixel[aIdx] & 0xffff) * alphaScale;
                            if (normAlpha != 0.0f) {
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (short)
                                        ((pixel[c] & 0xffff) * normAlpha +
                                         0.5f);
                                }
                                raster.setDataElements(rX, rY, pixel);
                            } else {
                                if (zpixel == null) {
                                    zpixel = new short[numComponents];
                                    java.util.Arrays.fill(zpixel, (short) 0);
                                }
                                raster.setDataElements(rX, rY, zpixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_INT: {
                    int pixel[] = null;
                    int zpixel[] = null;
                    float alphaScale = 1.0f / ((float) ((1<<nBits[aIdx]) - 1));
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = (int[])raster.getDataElements(rX, rY,
                                                                  pixel);
                            normAlpha = pixel[aIdx] * alphaScale;
                            if (normAlpha != 0.0f) {
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (int) (pixel[c] * normAlpha +
                                                      0.5f);
                                }
                                raster.setDataElements(rX, rY, pixel);
                            } else {
                                if (zpixel == null) {
                                    zpixel = new int[numComponents];
                                    java.util.Arrays.fill(zpixel, 0);
                                }
                                raster.setDataElements(rX, rY, zpixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_SHORT: {
                    short pixel[] = null;
                    short zpixel[] = null;
                    float alphaScale = 1.0f / 32767.0f;
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = (short[]) raster.getDataElements(rX, rY,
                                                                     pixel);
                            normAlpha = pixel[aIdx] * alphaScale;
                            if (normAlpha != 0.0f) {
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (short) (pixel[c] * normAlpha +
                                                        0.5f);
                                }
                                raster.setDataElements(rX, rY, pixel);
                            } else {
                                if (zpixel == null) {
                                    zpixel = new short[numComponents];
                                    java.util.Arrays.fill(zpixel, (short) 0);
                                }
                                raster.setDataElements(rX, rY, zpixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_FLOAT: {
                    float pixel[] = null;
                    float zpixel[] = null;
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = (float[]) raster.getDataElements(rX, rY,
                                                                     pixel);
                            normAlpha = pixel[aIdx];
                            if (normAlpha != 0.0f) {
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] *= normAlpha;
                                }
                                raster.setDataElements(rX, rY, pixel);
                            } else {
                                if (zpixel == null) {
                                    zpixel = new float[numComponents];
                                    java.util.Arrays.fill(zpixel, 0.0f);
                                }
                                raster.setDataElements(rX, rY, zpixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_DOUBLE: {
                    double pixel[] = null;
                    double zpixel[] = null;
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = (double[]) raster.getDataElements(rX, rY,
                                                                      pixel);
                            double dnormAlpha = pixel[aIdx];
                            if (dnormAlpha != 0.0) {
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] *= dnormAlpha;
                                }
                                raster.setDataElements(rX, rY, pixel);
                            } else {
                                if (zpixel == null) {
                                    zpixel = new double[numComponents];
                                    java.util.Arrays.fill(zpixel, 0.0);
                                }
                                raster.setDataElements(rX, rY, zpixel);
                            }
                        }
                    }
                }
                break;
                default:
                    throw new UnsupportedOperationException("This method has not been "+
                         "implemented for transferType " + transferType);
            }
        }
        else {
            // 我们是预乘的，需要除以它
            switch (transferType) {
                case DataBuffer.TYPE_BYTE: {
                    byte pixel[] = null;
                    float alphaScale = 1.0f / ((float) ((1<<nBits[aIdx]) - 1));
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = (byte[])raster.getDataElements(rX, rY,
                                                                   pixel);
                            normAlpha = (pixel[aIdx] & 0xff) * alphaScale;
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (byte)
                                        ((pixel[c] & 0xff) * invAlpha + 0.5f);
                                }
                                raster.setDataElements(rX, rY, pixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_USHORT: {
                    short pixel[] = null;
                    float alphaScale = 1.0f / ((float) ((1<<nBits[aIdx]) - 1));
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = (short[])raster.getDataElements(rX, rY,
                                                                    pixel);
                            normAlpha = (pixel[aIdx] & 0xffff) * alphaScale;
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (short)
                                        ((pixel[c] & 0xffff) * invAlpha + 0.5f);
                                }
                                raster.setDataElements(rX, rY, pixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_INT: {
                    int pixel[] = null;
                    float alphaScale = 1.0f / ((float) ((1<<nBits[aIdx]) - 1));
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = (int[])raster.getDataElements(rX, rY,
                                                                  pixel);
                            normAlpha = pixel[aIdx] * alphaScale;
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (int)
                                        (pixel[c] * invAlpha + 0.5f);
                                }
                                raster.setDataElements(rX, rY, pixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_SHORT: {
                    short pixel[] = null;
                    float alphaScale = 1.0f / 32767.0f;
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = (short[])raster.getDataElements(rX, rY,
                                                                    pixel);
                            normAlpha = pixel[aIdx] * alphaScale;
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] = (short)
                                        (pixel[c] * invAlpha + 0.5f);
                                }
                                raster.setDataElements(rX, rY, pixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_FLOAT: {
                    float pixel[] = null;
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = (float[])raster.getDataElements(rX, rY,
                                                                    pixel);
                            normAlpha = pixel[aIdx];
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] *= invAlpha;
                                }
                                raster.setDataElements(rX, rY, pixel);
                            }
                        }
                    }
                }
                break;
                case DataBuffer.TYPE_DOUBLE: {
                    double pixel[] = null;
                    for (int y = 0; y < h; y++, rY++) {
                        rX = rminX;
                        for (int x = 0; x < w; x++, rX++) {
                            pixel = (double[]) raster.getDataElements(rX, rY,
                                                                      pixel);
                            double dnormAlpha = pixel[aIdx];
                            if (dnormAlpha != 0.0) {
                                double invAlpha = 1.0 / dnormAlpha;
                                for (int c=0; c < aIdx; c++) {
                                    pixel[c] *= invAlpha;
                                }
                                raster.setDataElements(rX, rY, pixel);
                            }
                        }
                    }
                }
                break;
                default:
                    throw new UnsupportedOperationException("This method has not been "+
                         "implemented for transferType " + transferType);
            }
        }


                    // 返回一个新的颜色模型
        if (!signed) {
            return new ComponentColorModel(colorSpace, nBits, supportsAlpha,
                                           isAlphaPremultiplied, transparency,
                                           transferType);
        } else {
            return new ComponentColorModel(colorSpace, supportsAlpha,
                                           isAlphaPremultiplied, transparency,
                                           transferType);
        }

    }

    /**
      * 如果 <CODE>raster</CODE> 与这个 <CODE>ColorModel</CODE> 兼容，则返回 <CODE>true</CODE>；否则返回 <CODE>false</CODE>。
      *
      * @param raster 要测试兼容性的 <CODE>Raster</CODE> 对象。
      *
      * @return 如果 <CODE>raster</CODE> 与这个 <CODE>ColorModel</CODE> 兼容，则返回 <CODE>true</CODE>；否则返回 <CODE>false</CODE>。
      */
    public boolean isCompatibleRaster(Raster raster) {

        SampleModel sm = raster.getSampleModel();

        if (sm instanceof ComponentSampleModel) {
            if (sm.getNumBands() != getNumComponents()) {
                return false;
            }
            for (int i=0; i<nBits.length; i++) {
                if (sm.getSampleSize(i) < nBits[i]) {
                    return false;
                }
            }
            return (raster.getTransferType() == transferType);
        }
        else {
            return false;
        }
    }

    /**
     * 创建一个具有指定宽度和高度的 <CODE>WritableRaster</CODE>，其数据布局（<CODE>SampleModel</CODE>）与这个 <CODE>ColorModel</CODE> 兼容。
     *
     * @param w 要创建的 <CODE>WritableRaster</CODE> 的宽度。
     * @param h 要创建的 <CODE>WritableRaster</CODE> 的高度。
     *
     * @return 与这个 <CODE>ColorModel</CODE> 兼容的 <CODE>WritableRaster</CODE>。
     * @see WritableRaster
     * @see SampleModel
     */
    public WritableRaster createCompatibleWritableRaster (int w, int h) {
        int dataSize = w*h*numComponents;
        WritableRaster raster = null;

        switch (transferType) {
        case DataBuffer.TYPE_BYTE:
        case DataBuffer.TYPE_USHORT:
            raster = Raster.createInterleavedRaster(transferType,
                                                    w, h,
                                                    numComponents, null);
            break;
        default:
            SampleModel sm = createCompatibleSampleModel(w, h);
            DataBuffer db = sm.createDataBuffer();
            raster = Raster.createWritableRaster(sm, db, null);
        }

        return raster;
    }

    /**
     * 创建一个具有指定宽度和高度的 <CODE>SampleModel</CODE>，其数据布局与这个 <CODE>ColorModel</CODE> 兼容。
     *
     * @param w 要创建的 <CODE>SampleModel</CODE> 的宽度。
     * @param h 要创建的 <CODE>SampleModel</CODE> 的高度。
     *
     * @return 与这个 <CODE>ColorModel</CODE> 兼容的 <CODE>SampleModel</CODE>。
     *
     * @see SampleModel
     */
    public SampleModel createCompatibleSampleModel(int w, int h) {
        int[] bandOffsets = new int[numComponents];
        for (int i=0; i < numComponents; i++) {
            bandOffsets[i] = i;
        }
        switch (transferType) {
        case DataBuffer.TYPE_BYTE:
        case DataBuffer.TYPE_USHORT:
            return new PixelInterleavedSampleModel(transferType, w, h,
                                                   numComponents,
                                                   w*numComponents,
                                                   bandOffsets);
        default:
            return new ComponentSampleModel(transferType, w, h,
                                            numComponents,
                                            w*numComponents,
                                            bandOffsets);
        }
    }

    /**
     * 检查指定的 <CODE>SampleModel</CODE> 是否与这个 <CODE>ColorModel</CODE> 兼容。
     *
     * @param sm 要测试兼容性的 <CODE>SampleModel</CODE>。
     *
     * @return 如果 <CODE>SampleModel</CODE> 与这个 <CODE>ColorModel</CODE> 兼容，则返回 <CODE>true</CODE>；否则返回 <CODE>false</CODE>。
     *
     * @see SampleModel
     */
    public boolean isCompatibleSampleModel(SampleModel sm) {
        if (!(sm instanceof ComponentSampleModel)) {
            return false;
        }

        // 必须具有相同数量的组件
        if (numComponents != sm.getNumBands()) {
            return false;
        }

        if (sm.getTransferType() != transferType) {
            return false;
        }

        return true;
    }

    /**
     * 返回一个表示图像的 alpha 通道的 <CODE>Raster</CODE>，从输入的 <CODE>Raster</CODE> 中提取。
     * 此方法假设与这个 <CODE>ColorModel</CODE> 关联的 <CODE>Raster</CODE> 对象，如果存在 alpha 通道，则将其存储为图像数据的最后一通道。如果没有与这个 <CODE>ColorModel</CODE> 关联的单独的空间 alpha 通道，则返回 null。
     * 此方法创建一个新的 <CODE>Raster</CODE>，但将共享数据数组。
     *
     * @param raster 从中提取 alpha 通道的 <CODE>WritableRaster</CODE>。
     *
     * @return 包含图像 alpha 通道的 <CODE>WritableRaster</CODE>。
     *
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
     * 比较此颜色模型与另一个颜色模型是否相等。
     *
     * @param obj 要与这个颜色模型比较的对象。
     * @return 如果颜色模型对象相等，则返回 <CODE>true</CODE>；否则返回 <CODE>false</CODE>。
     */
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        if (obj.getClass() !=  getClass()) {
            return false;
        }

        return true;
    }

}
