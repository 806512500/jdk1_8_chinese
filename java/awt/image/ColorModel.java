
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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import sun.java2d.cmm.CMSManager;
import sun.java2d.cmm.ColorTransform;
import sun.java2d.cmm.PCMM;
import java.awt.Toolkit;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * <code>ColorModel</code> 抽象类封装了将像素值转换为颜色组件（例如，红色、绿色和蓝色）和 alpha 组件的方法。
 * 为了将图像渲染到屏幕、打印机或其他图像，像素值必须转换为颜色和 alpha 组件。
 * 作为此类方法的参数或返回值，像素表示为 32 位整数或原始类型的数组。
 * <code>ColorModel</code> 的颜色组件的数量、顺序和解释由其 <code>ColorSpace</code> 指定。
 * 一个 <code>ColorModel</code> 用于不包含 alpha 信息的像素数据时，将所有像素视为不透明，即 alpha 值为 1.0。
 * <p>
 * 此 <code>ColorModel</code> 类支持两种表示像素值的形式。像素值可以是一个 32 位整数或一个原始类型的数组。
 * Java(tm) 平台 1.0 和 1.1 API 将像素表示为单个 <code>byte</code> 或单个 <code>int</code> 值。
 * 为了 <code>ColorModel</code> 类，像素值参数以 int 形式传递。Java(tm) 2 平台 API 引入了表示图像的其他类。
 * 基于 {@link Raster} 和 {@link SampleModel} 类的 {@link BufferedImage} 或 {@link RenderedImage} 对象，
 * 像素值可能不便于表示为单个 int。因此，<code>ColorModel</code> 现在有接受表示为原始类型数组的像素值的方法。
 * 特定 <code>ColorModel</code> 对象使用的原始类型称为其传输类型。
 * <p>
 * 用于图像的 <code>ColorModel</code> 对象，如果像素值不便于表示为单个 int，则在调用接受单个 int 像素参数的方法时抛出
 * {@link IllegalArgumentException}。<code>ColorModel</code> 的子类必须指定这种情况发生的时间。
 * 这不会发生在 {@link DirectColorModel} 或 {@link IndexColorModel} 对象上。
 * <p>
 * 目前，Java 2D(tm) API 支持的传输类型有 DataBuffer.TYPE_BYTE、DataBuffer.TYPE_USHORT、DataBuffer.TYPE_INT、
 * DataBuffer.TYPE_SHORT、DataBuffer.TYPE_FLOAT 和 DataBuffer.TYPE_DOUBLE。大多数渲染操作在使用前三种类型时会更快。
 * 此外，某些图像过滤操作不支持基于后三种类型的 <code>ColorModel</code> 和图像。
 * 特定 <code>ColorModel</code> 对象的传输类型在创建对象时指定，可以显式指定或默认指定。
 * 所有 <code>ColorModel</code> 的子类必须指定可能的传输类型以及确定表示像素的原始数组元素数量的方法。
 * <p>
 * 对于 <code>BufferedImages</code>，其 <code>Raster</code> 和 <code>Raster</code> 对象的
 * <code>SampleModel</code>（可从这些类的 <code>getTransferType</code> 方法获取）的传输类型必须与
 * <code>ColorModel</code> 匹配。表示 <code>Raster</code> 和 <code>SampleModel</code> 像素的数组元素数量
 * （可从这些类的 <code>getNumDataElements</code> 方法获取）必须与 <code>ColorModel</code> 匹配。
 * <p>
 * 从像素值转换为颜色和 alpha 组件的算法因子类而异。例如，从 <code>BufferedImage</code> 对象的
 * <code>Raster</code> 的 <code>SampleModel</code> 获取的样本与颜色/alpha 组件之间不一定有一对一的对应关系。
 * 即使有这种对应关系，样本中的位数也不一定与相应颜色/alpha 组件中的位数相同。每个子类必须指定如何进行从
 * 像素值到颜色/alpha 组件的转换。
 * <p>
 * <code>ColorModel</code> 类中的方法使用两种不同的颜色和 alpha 组件表示形式——归一化形式和非归一化形式。
 * 在归一化形式中，每个组件是一个 <code>float</code> 值，介于某个最小值和最大值之间。对于 alpha 组件，最小值为 0.0，最大值为 1.0。
 * 对于颜色组件，每个组件的最小值和最大值可以从 <code>ColorSpace</code> 对象获取。这些值通常为 0.0 和 1.0
 * （例如，默认 sRGB 颜色空间的归一化组件值范围从 0.0 到 1.0），但某些颜色空间的组件值有不同的上限和下限。
 * 这些限制可以使用 <code>ColorSpace</code> 类的 <code>getMinValue</code> 和 <code>getMaxValue</code> 方法获取。
 * 归一化颜色组件值未预乘。所有 <code>ColorModels</code> 必须支持归一化形式。
 * <p>
 * 在非归一化形式中，每个组件是一个介于 0 和 2<sup>n</sup> - 1 之间的无符号整数值，其中 n 是特定组件的显著位数。
 * 如果特定 <code>ColorModel</code> 的像素值表示为由 alpha 样本预乘的颜色样本，则非归一化颜色组件值也预乘。
 * 非归一化形式仅用于 <code>ColorSpace</code> 的所有组件的最小值为 0.0 且最大值为 1.0 的 <code>ColorModel</code> 实例。
 * 对于颜色和 alpha 组件的非归一化形式，当归一化组件值全部介于 0.0 和 1.0 之间时，可以方便地表示。
 * 在这种情况下，整数值 0 映射到 0.0，值 2<sup>n</sup> - 1 映射到 1.0。在其他情况下，例如当归一化组件值可以是负数或正数时，
 * 非归一化形式不便于使用。这样的 <code>ColorModel</code> 对象在调用涉及非归一化参数的方法时抛出
 * {@link IllegalArgumentException}。<code>ColorModel</code> 的子类必须指定这种情况发生的时间。
 *
 * @see IndexColorModel
 * @see ComponentColorModel
 * @see PackedColorModel
 * @see DirectColorModel
 * @see java.awt.Image
 * @see BufferedImage
 * @see RenderedImage
 * @see java.awt.color.ColorSpace
 * @see SampleModel
 * @see Raster
 * @see DataBuffer
 */
public abstract class ColorModel implements Transparency {
    private long pData;         // 用于本地函数的数据占位符

    /**
     * 像素中的总位数。
     */
    protected int pixel_bits;
    int nBits[];
    int transparency = Transparency.TRANSLUCENT;
    boolean supportsAlpha = true;
    boolean isAlphaPremultiplied = false;
    int numComponents = -1;
    int numColorComponents = -1;
    ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    int colorSpaceType = ColorSpace.TYPE_RGB;
    int maxBits;
    boolean is_sRGB = true;

    /**
     * 用于表示像素值的数组的数据类型。
     */
    protected int transferType;

    /**
     * 从 java.awt.Toolkit 复制而来，因为我们还需要在 java.awt.image 中加载库：
     *
     * 警告：这是 AWT 加载本机库问题的临时解决方法。AWT 包中的许多类都有一个本机方法，initIDs()，该方法初始化
     * 本机部分中使用的 JNI 字段和方法 id。
     *
     * 由于这些 id 的使用和存储是由实现库完成的，因此这些方法的实现由特定的 AWT 实现（例如，“Toolkit”/Peer），如 Motif、
     * Microsoft Windows 或 Tiny 提供。问题是这意味着本机库必须由 java.* 类加载，而这些类不一定知道要加载的库的名称。
     * 一个更好的方法是提供一个定义 java.awt.* initIDs 的单独库，并将相关符号导出到实现库。
     *
     * 目前，我们知道这是由实现完成的，我们假设库的名称是 "awt"。-br。
     */
    private static boolean loaded = false;
    static void loadLibraries() {
        if (!loaded) {
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<Void>() {
                    public Void run() {
                        System.loadLibrary("awt");
                        return null;
                    }
                });
            loaded = true;
        }
    }
    private static native void initIDs();
    static {
        /* 确保加载了正确的库 */
        loadLibraries();
        initIDs();
    }
    private static ColorModel RGBdefault;

    /**
     * 返回一个描述许多 AWT 图像接口方法中使用的整数 RGB 值的默认格式的 <code>DirectColorModel</code>，
     * 以方便程序员。颜色空间是默认的 {@link ColorSpace}，即 sRGB。
     * RGB 值的格式是一个整数，其中 8 位分别表示 alpha、红色、绿色和蓝色颜色组件，从最高有效字节到最低有效字节依次排列，如：0xAARRGGBB。
     * 颜色组件未预乘 alpha 组件。此格式不一定表示特定设备或所有图像的本机或最有效的 <code>ColorModel</code>。
 * 它仅用作通用颜色模型格式。
 * @return 描述默认 RGB 值的 <code>DirectColorModel</code> 对象。
 */
    public static ColorModel getRGBdefault() {
        if (RGBdefault == null) {
            RGBdefault = new DirectColorModel(32,
                                              0x00ff0000,       // 红色
                                              0x0000ff00,       // 绿色
                                              0x000000ff,       // 蓝色
                                              0xff000000        // Alpha
                                              );
        }
        return RGBdefault;
    }

    /**
     * 构造一个将指定位数的像素转换为颜色/alpha 组件的 <code>ColorModel</code>。颜色空间是默认的 RGB <code>ColorSpace</code>，即 sRGB。
     * 像素值假定包含 alpha 信息。如果颜色和 alpha 信息在像素值中作为单独的空间带表示，则假定颜色带未与 alpha 值预乘。
     * 透明度类型是 java.awt.Transparency.TRANSLUCENT。传输类型将是 DataBuffer.TYPE_BYTE、DataBuffer.TYPE_USHORT、
     * 或 DataBuffer.TYPE_INT 中可以容纳单个像素的最小类型（如果 bits 大于 32，则为 DataBuffer.TYPE_UNDEFINED）。
     * 由于此构造函数没有关于每个颜色和 alpha 组件的位数的信息，任何调用此构造函数的子类应覆盖需要此信息的任何方法。
     * @param bits 像素的位数
     * @throws IllegalArgumentException 如果 <code>bits</code> 中的位数小于 1
     */
    public ColorModel(int bits) {
        pixel_bits = bits;
        if (bits < 1) {
            throw new IllegalArgumentException("位数必须 > 0");
        }
        numComponents = 4;
        numColorComponents = 3;
        maxBits = bits;
        // 提醒：确保传输类型设置正确
        transferType = ColorModel.getDefaultTransferType(bits);
    }

    /**
     * 构造一个将像素值转换为颜色/alpha 组件的 <code>ColorModel</code>。颜色组件将在指定的 <code>ColorSpace</code> 中。
     * <code>pixel_bits</code> 是像素值中的位数。位数组指定每个颜色和 alpha 组件的显著位数。
     * 如果像素值中没有 alpha 信息，其长度应为 <code>ColorSpace</code> 中的组件数；如果包含 alpha 信息，则应比该数量多一个。
     * <code>hasAlpha</code> 表示是否包含 alpha 信息。<code>isAlphaPremultiplied</code> 指定如何解释颜色和 alpha 信息作为单独空间带表示的像素值。
     * 如果该布尔值为 <code>true</code>，则假定颜色样本已乘以 alpha 样本。<code>transparency</code> 指定此颜色模型可以表示的 alpha 值。
     * 传输类型是用于表示像素值的原始数组的类型。注意，位数组包含从像素值转换后的每个颜色/alpha 组件的显著位数。
     * 例如，对于 <code>pixel_bits</code> 为 16 的 <code>IndexColorModel</code>，位数组可能有四个元素，每个元素设置为 8。
     * @param pixel_bits 像素值中的位数
     * @param bits 指定每个颜色和 alpha 组件的显著位数的数组
     * @param cspace 指定的 <code>ColorSpace</code>
     * @param hasAlpha 如果包含 alpha 信息，则为 <code>true</code>；否则为 <code>false</code>
     * @param isAlphaPremultiplied 如果假定颜色样本已乘以 alpha 样本，则为 <code>true</code>；否则为 <code>false</code>
     * @param transparency 此颜色模型可以表示的 alpha 值
     * @param transferType 用于表示像素值的数组的类型
     * @throws IllegalArgumentException 如果位数组的长度小于此 <code>ColorModel</code> 中的颜色或 alpha 组件数，或如果
     *          透明度不是有效值。
     * @throws IllegalArgumentException 如果 <code>bits</code> 中的位数之和小于 1 或 <code>bits</code> 中的任何元素小于 0。
     * @see java.awt.Transparency
     */
    protected ColorModel(int pixel_bits, int[] bits, ColorSpace cspace,
                         boolean hasAlpha,
                         boolean isAlphaPremultiplied,
                         int transparency,
                         int transferType) {
        colorSpace                = cspace;
        colorSpaceType            = cspace.getType();
        numColorComponents        = cspace.getNumComponents();
        numComponents             = numColorComponents + (hasAlpha ? 1 : 0);
        supportsAlpha             = hasAlpha;
        if (bits.length < numComponents) {
            throw new IllegalArgumentException("颜色/alpha 组件的数量应为 " +
                                               numComponents +
                                               " 但位数组的长度为 " +
                                               bits.length);
        }


                    // 4186669
        if (transparency < Transparency.OPAQUE ||
            transparency > Transparency.TRANSLUCENT)
        {
            throw new IllegalArgumentException("未知的透明度: "+
                                               transparency);
        }

        if (supportsAlpha == false) {
            this.isAlphaPremultiplied = false;
            this.transparency = Transparency.OPAQUE;
        }
        else {
            this.isAlphaPremultiplied = isAlphaPremultiplied;
            this.transparency         = transparency;
        }

        nBits = bits.clone();
        this.pixel_bits = pixel_bits;
        if (pixel_bits <= 0) {
            throw new IllegalArgumentException("像素位数必须大于0");
        }
        // 检查位数是否小于0
        maxBits = 0;
        for (int i=0; i < bits.length; i++) {
            // bug 4304697
            if (bits[i] < 0) {
                throw new
                    IllegalArgumentException("位数必须大于等于0");
            }
            if (maxBits < bits[i]) {
                maxBits = bits[i];
            }
        }

        // 确保我们没有全部0位的组件
        if (maxBits == 0) {
            throw new IllegalArgumentException("至少必须有一个组件的像素位数大于0");
        }

        // 保存此值，因为我们总是需要检查它是否为默认颜色空间
        if (cspace != ColorSpace.getInstance(ColorSpace.CS_sRGB)) {
            is_sRGB = false;
        }

        // 保存传输类型
        this.transferType = transferType;
    }

    /**
     * 返回此<code>ColorModel</code>是否支持alpha。
     * @return 如果此<code>ColorModel</code>支持alpha，则返回<code>true</code>；否则返回<code>false</code>。
     */
    final public boolean hasAlpha() {
        return supportsAlpha;
    }

    /**
     * 返回alpha是否已在要由此<code>ColorModel</code>转换的像素值中预乘。
     * 如果布尔值为<code>true</code>，则此<code>ColorModel</code>用于解释颜色和alpha信息作为单独的空间带表示的像素值，且颜色样本假定已乘以alpha样本。
     * @return 如果alpha值已在要由此<code>ColorModel</code>转换的像素值中预乘，则返回<code>true</code>；否则返回<code>false</code>。
     */
    final public boolean isAlphaPremultiplied() {
        return isAlphaPremultiplied;
    }

    /**
     * 返回此<code>ColorModel</code>的传输类型。
     * 传输类型是用于表示作为数组的像素值的原始数组类型。
     * @return 传输类型。
     * @since 1.3
     */
    final public int getTransferType() {
        return transferType;
    }

    /**
     * 返回此<code>ColorModel</code>描述的每像素位数。
     * @return 每像素位数。
     */
    public int getPixelSize() {
        return pixel_bits;
    }

    /**
     * 返回指定颜色/alpha组件的位数。
     * 颜色组件按<code>ColorSpace</code>指定的顺序索引。通常，此顺序反映了颜色空间类型的名称。例如，对于TYPE_RGB，索引0对应红色，索引1对应绿色，索引2对应蓝色。如果此<code>ColorModel</code>支持alpha，则alpha组件对应于最后一个颜色组件之后的索引。
     * @param componentIdx 颜色/alpha组件的索引
     * @return 指定索引处的颜色/alpha组件的位数。
     * @throws ArrayIndexOutOfBoundsException 如果<code>componentIdx</code>大于组件数或小于零
     * @throws NullPointerException 如果位数数组为<code>null</code>
     */
    public int getComponentSize(int componentIdx) {
        // REMIND:
        if (nBits == null) {
            throw new NullPointerException("位数数组为null。");
        }

        return nBits[componentIdx];
    }

    /**
     * 返回每个颜色/alpha组件的位数数组。
     * 数组包含按<code>ColorSpace</code>指定的顺序排列的颜色组件，后面跟着alpha组件（如果存在）。
     * @return 每个颜色/alpha组件的位数数组
     */
    public int[] getComponentSize() {
        if (nBits != null) {
            return nBits.clone();
        }

        return null;
    }

    /**
     * 返回透明度。返回值为OPAQUE、BITMASK或TRANSLUCENT。
     * @return 此<code>ColorModel</code>的透明度。
     * @see Transparency#OPAQUE
     * @see Transparency#BITMASK
     * @see Transparency#TRANSLUCENT
     */
    public int getTransparency() {
        return transparency;
    }

    /**
     * 返回此<code>ColorModel</code>中的组件数，包括alpha。这等于颜色组件数，加上一个（如果存在alpha组件）。
     * @return 此<code>ColorModel</code>中的组件数
     */
    public int getNumComponents() {
        return numComponents;
    }

    /**
     * 返回此<code>ColorModel</code>中的颜色组件数。
     * 这是{@link ColorSpace#getNumComponents}返回的组件数。
     * @return 此<code>ColorModel</code>中的颜色组件数。
     * @see ColorSpace#getNumComponents
     */
    public int getNumColorComponents() {
        return numColorComponents;
    }

    /**
     * 返回指定像素的红色组件，按默认RGB颜色空间sRGB从0到255缩放。如果需要，将进行颜色转换。像素值指定为int。
     * 如果此<code>ColorModel</code>的像素值不能方便地表示为单个int，则抛出<code>IllegalArgumentException</code>。返回的值不是预乘值。例如，如果alpha已预乘，此方法将除以alpha再返回值。如果alpha值为0，则红色值为0。
     * @param pixel 指定的像素
     * @return 指定像素的红色组件值。
     */
    public abstract int getRed(int pixel);

    /**
     * 返回指定像素的绿色组件，按默认RGB颜色空间sRGB从0到255缩放。如果需要，将进行颜色转换。像素值指定为int。
     * 如果此<code>ColorModel</code>的像素值不能方便地表示为单个int，则抛出<code>IllegalArgumentException</code>。返回的值不是预乘值。例如，如果alpha已预乘，此方法将除以alpha再返回值。如果alpha值为0，则绿色值为0。
     * @param pixel 指定的像素
     * @return 指定像素的绿色组件值。
     */
    public abstract int getGreen(int pixel);

    /**
     * 返回指定像素的蓝色组件，按默认RGB颜色空间sRGB从0到255缩放。如果需要，将进行颜色转换。像素值指定为int。
     * 如果此<code>ColorModel</code>的像素值不能方便地表示为单个int，则抛出<code>IllegalArgumentException</code>。返回的值不是预乘值。例如，如果alpha已预乘，此方法将除以alpha再返回值。如果alpha值为0，则蓝色值为0。
     * @param pixel 指定的像素
     * @return 指定像素的蓝色组件值。
     */
    public abstract int getBlue(int pixel);

    /**
     * 返回指定像素的alpha组件，按0到255缩放。像素值指定为int。
     * 如果此<code>ColorModel</code>的像素值不能方便地表示为单个int，则抛出<code>IllegalArgumentException</code>。
     * @param pixel 指定的像素
     * @return 指定像素的alpha组件值。
     */
    public abstract int getAlpha(int pixel);

    /**
     * 返回像素在默认RGB颜色模型格式中的颜色/alpha组件。如果需要，将进行颜色转换。像素值指定为int。
     * 如果此<code>ColorModel</code>的像素值不能方便地表示为单个int，则抛出<code>IllegalArgumentException</code>。返回的值不是预乘格式。例如，如果alpha已预乘，此方法将除以alpha再返回值。如果alpha值为0，则颜色值为0。
     * @param pixel 指定的像素
     * @return 指定像素的颜色/alpha组件的RGB值。
     * @see ColorModel#getRGBdefault
     */
    public int getRGB(int pixel) {
        return (getAlpha(pixel) << 24)
            | (getRed(pixel) << 16)
            | (getGreen(pixel) << 8)
            | (getBlue(pixel) << 0);
    }

    /**
     * 返回指定像素的红色组件，按默认RGB<code>ColorSpace</code>sRGB从0到255缩放。如果需要，将进行颜色转换。像素值由作为对象引用传递的transferType类型的数据元素数组指定。返回的值不是预乘值。例如，如果alpha已预乘，此方法将除以alpha再返回值。如果alpha值为0，则红色值为0。
     * 如果<code>inData</code>不是transferType类型的原始数组，则抛出<code>ClassCastException</code>。如果<code>inData</code>不足以容纳此<code>ColorModel</code>的像素值，则抛出<code>ArrayIndexOutOfBoundsException</code>。
     * 如果此<code>transferType</code>不受支持，则抛出<code>UnsupportedOperationException</code>。由于<code>ColorModel</code>是抽象类，任何实例必须是子类的实例。子类继承此方法的实现，如果它们不覆盖此方法，且子类使用<code>DataBuffer.TYPE_BYTE</code>、<code>DataBuffer.TYPE_USHORT</code>或<code>DataBuffer.TYPE_INT</code>以外的<code>transferType</code>，则此方法将抛出异常。
     * @param inData 像素值数组
     * @return 指定像素的红色组件值。
     * @throws ClassCastException 如果<code>inData</code>不是transferType类型的原始数组
     * @throws ArrayIndexOutOfBoundsException 如果<code>inData</code>不足以容纳此<code>ColorModel</code>的像素值
     * @throws UnsupportedOperationException 如果此<code>tranferType</code>不受此<code>ColorModel</code>支持
     */
    public int getRed(Object inData) {
        int pixel=0,length=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[0] & 0xff;
               length = bdata.length;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[0] & 0xffff;
               length = sdata.length;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[0];
               length = idata.length;
            break;
            default:
               throw new UnsupportedOperationException("此方法尚未为transferType " + transferType + "实现");
        }
        if (length == 1) {
            return getRed(pixel);
        }
        else {
            throw new UnsupportedOperationException
                ("此颜色模型不支持此方法");
        }
    }

    /**
     * 返回指定像素的绿色组件，按默认RGB<code>ColorSpace</code>sRGB从0到255缩放。如果需要，将进行颜色转换。像素值由作为对象引用传递的transferType类型的数据元素数组指定。返回的值不是预乘值。例如，如果alpha已预乘，此方法将除以alpha再返回值。如果alpha值为0，则绿色值为0。
     * 如果<code>inData</code>不是transferType类型的原始数组，则抛出<code>ClassCastException</code>。如果<code>inData</code>不足以容纳此<code>ColorModel</code>的像素值，则抛出<code>ArrayIndexOutOfBoundsException</code>。
     * 如果此<code>transferType</code>不受支持，则抛出<code>UnsupportedOperationException</code>。由于<code>ColorModel</code>是抽象类，任何实例必须是子类的实例。子类继承此方法的实现，如果它们不覆盖此方法，且子类使用<code>DataBuffer.TYPE_BYTE</code>、<code>DataBuffer.TYPE_USHORT</code>或<code>DataBuffer.TYPE_INT</code>以外的<code>transferType</code>，则此方法将抛出异常。
     * @param inData 像素值数组
     * @return 指定像素的绿色组件值。
     * @throws ClassCastException 如果<code>inData</code>不是transferType类型的原始数组
     * @throws ArrayIndexOutOfBoundsException 如果<code>inData</code>不足以容纳此<code>ColorModel</code>的像素值
     * @throws UnsupportedOperationException 如果此<code>tranferType</code>不受此<code>ColorModel</code>支持
     */
    public int getGreen(Object inData) {
        int pixel=0,length=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[0] & 0xff;
               length = bdata.length;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[0] & 0xffff;
               length = sdata.length;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[0];
               length = idata.length;
            break;
            default:
               throw new UnsupportedOperationException("此方法尚未为transferType " + transferType + "实现");
        }
        if (length == 1) {
            return getGreen(pixel);
        }
        else {
            throw new UnsupportedOperationException
                ("此颜色模型不支持此方法");
        }
    }


                /**
     * 返回指定像素的蓝色分量，从 0 到 255 在默认的 RGB <code>ColorSpace</code> 中，即 sRGB。必要时会进行颜色转换。指定的像素值由作为对象引用传递的类型为 transferType 的数据元素数组指定。返回的值是非预乘值。例如，如果 alpha 已预乘，此方法会将其除以再返回值。如果 alpha 值为 0，蓝色值将为 0。如果 <code>inData</code> 不是类型为 transferType 的基本数组，将抛出 <code>ClassCastException</code>。如果 <code>inData</code> 不足以容纳此 <code>ColorModel</code> 的像素值，将抛出 <code>ArrayIndexOutOfBoundsException</code>。如果此 <code>transferType</code> 不受支持，将抛出 <code>UnsupportedOperationException</code>。由于 <code>ColorModel</code> 是一个抽象类，任何实例都必须是子类的实例。子类继承此方法的实现，如果它们不重写此方法，并且子类使用 <code>transferType</code> 不是 <code>DataBuffer.TYPE_BYTE</code>、<code>DataBuffer.TYPE_USHORT</code> 或 <code>DataBuffer.TYPE_INT</code>，此方法将抛出异常。
     * @param inData 一个像素值数组
     * @return 指定像素的蓝色分量的值。
     * @throws ClassCastException 如果 <code>inData</code>
     *  不是类型为 <code>transferType</code> 的基本数组
     * @throws ArrayIndexOutOfBoundsException 如果
     *  <code>inData</code> 不足以容纳此 <code>ColorModel</code> 的像素值
     * @throws UnsupportedOperationException 如果此
     *  <code>tranferType</code> 不受此 <code>ColorModel</code> 支持
     */
    public int getBlue(Object inData) {
        int pixel=0,length=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[0] & 0xff;
               length = bdata.length;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[0] & 0xffff;
               length = sdata.length;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[0];
               length = idata.length;
            break;
            default:
               throw new UnsupportedOperationException("This method has not been "+
                   "implemented for transferType " + transferType);
        }
        if (length == 1) {
            return getBlue(pixel);
        }
        else {
            throw new UnsupportedOperationException
                ("This method is not supported by this color model");
        }
    }

    /**
     * 返回指定像素的 alpha 分量，从 0 到 255。指定的像素值由作为对象引用传递的类型为 transferType 的数据元素数组指定。如果 inData 不是类型为 transferType 的基本数组，将抛出 <code>ClassCastException</code>。如果 <code>inData</code> 不足以容纳此 <code>ColorModel</code> 的像素值，将抛出 <code>ArrayIndexOutOfBoundsException</code>。如果此 <code>transferType</code> 不受支持，将抛出 <code>UnsupportedOperationException</code>。由于 <code>ColorModel</code> 是一个抽象类，任何实例都必须是子类的实例。子类继承此方法的实现，如果它们不重写此方法，并且子类使用 <code>transferType</code> 不是 <code>DataBuffer.TYPE_BYTE</code>、<code>DataBuffer.TYPE_USHORT</code> 或 <code>DataBuffer.TYPE_INT</code>，此方法将抛出异常。
     * @param inData 指定的像素
     * @return 指定像素的 alpha 分量，从 0 到 255。
     * @throws ClassCastException 如果 <code>inData</code>
     *  不是类型为 <code>transferType</code> 的基本数组
     * @throws ArrayIndexOutOfBoundsException 如果
     *  <code>inData</code> 不足以容纳此 <code>ColorModel</code> 的像素值
     * @throws UnsupportedOperationException 如果此
     *  <code>tranferType</code> 不受此 <code>ColorModel</code> 支持
     */
    public int getAlpha(Object inData) {
        int pixel=0,length=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[0] & 0xff;
               length = bdata.length;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[0] & 0xffff;
               length = sdata.length;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[0];
               length = idata.length;
            break;
            default:
               throw new UnsupportedOperationException("This method has not been "+
                   "implemented for transferType " + transferType);
        }
        if (length == 1) {
            return getAlpha(pixel);
        }
        else {
            throw new UnsupportedOperationException
                ("This method is not supported by this color model");
        }
    }

    /**
     * 返回指定像素的颜色/alpha 分量，默认的 RGB 颜色模型格式。必要时会进行颜色转换。指定的像素值由作为对象引用传递的类型为 transferType 的数据元素数组指定。如果 inData 不是类型为 transferType 的基本数组，将抛出 <code>ClassCastException</code>。如果 <code>inData</code> 不足以容纳此 <code>ColorModel</code> 的像素值，将抛出 <code>ArrayIndexOutOfBoundsException</code>。返回的值将为非预乘格式，即如果 alpha 已预乘，此方法将从颜色分量中将其除以（如果 alpha 值为 0，颜色值将为 0）。
     * @param inData 指定的像素
     * @return 指定像素的颜色和 alpha 分量。
     * @see ColorModel#getRGBdefault
     */
    public int getRGB(Object inData) {
        return (getAlpha(inData) << 24)
            | (getRed(inData) << 16)
            | (getGreen(inData) << 8)
            | (getBlue(inData) << 0);
    }

    /**
     * 返回此 <code>ColorModel</code> 中像素的数组表示形式，给定默认 RGB 颜色模型中的整数像素表示形式。此数组可以传递给 <code>WritableRaster</code> 对象的 {@link WritableRaster#setDataElements} 方法。如果像素变量为 <code>null</code>，将分配一个新数组。如果 <code>pixel</code> 不为 <code>null</code>，它必须是类型为 <code>transferType</code> 的基本数组；否则，将抛出 <code>ClassCastException</code>。如果 <code>pixel</code> 不足以容纳此 <code>ColorModel</code> 的像素值，将抛出 <code>ArrayIndexOutOfBoundsException</code>。返回像素数组。如果此 <code>transferType</code> 不受支持，将抛出 <code>UnsupportedOperationException</code>。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是子类的实例。子类必须重写此方法，因为此抽象类中的实现会抛出 <code>UnsupportedOperationException</code>。
     * @param rgb 默认 RGB 颜色模型中的整数像素表示形式
     * @param pixel 指定的像素
     * @return 此 <code>ColorModel</code> 中指定像素的数组表示形式。
     * @throws ClassCastException 如果 <code>pixel</code>
     *  不是类型为 <code>transferType</code> 的基本数组
     * @throws ArrayIndexOutOfBoundsException 如果
     *  <code>pixel</code> 不足以容纳此 <code>ColorModel</code> 的像素值
     * @throws UnsupportedOperationException 如果此
     *  方法不受此 <code>ColorModel</code> 支持
     * @see WritableRaster#setDataElements
     * @see SampleModel#setDataElements
     */
    public Object getDataElements(int rgb, Object pixel) {
        throw new UnsupportedOperationException
            ("This method is not supported by this color model.");
    }

    /**
     * 返回此 <code>ColorModel</code> 中像素的非归一化颜色/alpha 分量数组。像素值指定为 <code>int</code>。如果此 <code>ColorModel</code> 的像素值不能方便地表示为单个 <code>int</code>，或者此 <code>ColorModel</code> 的颜色分量值不能方便地表示为非归一化形式，将抛出 <code>IllegalArgumentException</code>。例如，此方法可用于检索 <code>DirectColorModel</code> 中特定像素值的分量。如果 components 数组为 <code>null</code>，将分配一个新数组。返回 components 数组。颜色/alpha 分量从 <code>offset</code> 开始存储在 components 数组中（即使此数组是由此方法分配的）。如果 components 数组不为 <code>null</code> 且不足以容纳所有颜色和 alpha 分量（从 offset 开始），将抛出 <code>ArrayIndexOutOfBoundsException</code>。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是子类的实例。子类必须重写此方法，因为此抽象类中的实现会抛出 <code>UnsupportedOperationException</code>。
     * @param pixel 指定的像素
     * @param components 接收指定像素的颜色和 alpha 分量的数组
     * @param offset 在 <code>components</code> 数组中开始存储颜色和 alpha 分量的位置
     * @return 从指定偏移量开始包含指定像素的颜色和 alpha 分量的数组。
     * @throws UnsupportedOperationException 如果此
     *          方法不受此 <code>ColorModel</code> 支持
     */
    public int[] getComponents(int pixel, int[] components, int offset) {
        throw new UnsupportedOperationException
            ("This method is not supported by this color model.");
    }

    /**
     * 返回此 <code>ColorModel</code> 中像素的非归一化颜色/alpha 分量数组。像素值由作为对象引用传递的类型为 transferType 的数据元素数组指定。如果 <code>pixel</code> 不是类型为 transferType 的基本数组，将抛出 <code>ClassCastException</code>。如果此 <code>ColorModel</code> 的颜色分量值不能方便地表示为非归一化形式，将抛出 <code>IllegalArgumentException</code>。如果 <code>pixel</code> 不足以容纳此 <code>ColorModel</code> 的像素值，将抛出 <code>ArrayIndexOutOfBoundsException</code>。此方法可用于检索任何 <code>ColorModel</code> 中特定像素值的分量。如果 components 数组为 <code>null</code>，将分配一个新数组。返回 components 数组。颜色/alpha 分量从 <code>offset</code> 开始存储在 <code>components</code> 数组中（即使此数组是由此方法分配的）。如果 components 数组不为 <code>null</code> 且不足以容纳所有颜色和 alpha 分量（从 <code>offset</code> 开始），将抛出 <code>ArrayIndexOutOfBoundsException</code>。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是子类的实例。子类必须重写此方法，因为此抽象类中的实现会抛出 <code>UnsupportedOperationException</code>。
     * @param pixel 指定的像素
     * @param components 接收指定像素的颜色和 alpha 分量的数组
     * @param offset 在 <code>components</code> 数组中开始存储指定像素的颜色和 alpha 分量的位置
     * @return 从指定偏移量开始包含指定像素的颜色和 alpha 分量的数组。
     * @throws UnsupportedOperationException 如果此
     *          方法不受此 <code>ColorModel</code> 支持
     */
    public int[] getComponents(Object pixel, int[] components, int offset) {
        throw new UnsupportedOperationException
            ("This method is not supported by this color model.");
    }

    /**
     * 返回给定归一化分量数组的所有颜色/alpha 分量的非归一化形式数组。非归一化分量是 0 到 2<sup>n</sup> - 1 之间的无符号整数值，其中 n 是特定分量的位数。归一化分量是 <code>ColorSpace</code> 对象为此 <code>ColorModel</code> 指定的每个分量的最小值和最大值之间的浮点值。如果此 <code>ColorModel</code> 的颜色分量值不能方便地表示为非归一化形式，将抛出 <code>IllegalArgumentException</code>。如果 <code>components</code> 数组为 <code>null</code>，将分配一个新数组。返回 <code>components</code> 数组。颜色/alpha 分量从 <code>offset</code> 开始存储在 <code>components</code> 数组中（即使此数组是由此方法分配的）。如果 <code>components</code> 数组不为 <code>null</code> 且不足以容纳所有颜色和 alpha 分量（从 <code>offset</code> 开始），将抛出 <code>ArrayIndexOutOfBoundsException</code>。如果 <code>normComponents</code> 数组不足以容纳从 <code>normOffset</code> 开始的所有颜色和 alpha 分量，将抛出 <code>IllegalArgumentException</code>。
     * @param normComponents 包含归一化分量的数组
     * @param normOffset 从 <code>normComponents</code> 数组中开始检索归一化分量的位置
     * @param components 接收 <code>normComponents</code> 中的分量的数组
     * @param offset 在 <code>components</code> 中开始存储 <code>normComponents</code> 中的归一化分量的位置
     * @return 包含非归一化颜色和 alpha 分量的数组。
     * @throws IllegalArgumentException 如果此 <code>ColorModel</code> 的分量值不能方便地表示为非归一化形式。
     * @throws IllegalArgumentException 如果 <code>normComponents</code> 的长度减去 <code>normOffset</code>
     *          小于 <code>numComponents</code>
     * @throws UnsupportedOperationException 如果此 <code>ColorModel</code> 的构造函数调用了 <code>super(bits)</code> 构造函数，但未重写此方法。请参见构造函数，{@link #ColorModel(int)}。
     */
    public int[] getUnnormalizedComponents(float[] normComponents,
                                           int normOffset,
                                           int[] components, int offset) {
        // 确保没有人使用自定义颜色模型
        // 调用了 super(bits) 构造函数。
        if (colorSpace == null) {
            throw new UnsupportedOperationException("This method is not supported "+
                                        "by this color model.");
        }


                    if (nBits == null) {
            throw new UnsupportedOperationException ("This method is not supported.  "+
                                         "无法确定每个组件的位数。");
        }
        if ((normComponents.length - normOffset) < numComponents) {
            throw new
                IllegalArgumentException(
                        "组件数量不正确。期望 " +
                        numComponents);
        }

        if (components == null) {
            components = new int[offset+numComponents];
        }

        if (supportsAlpha && isAlphaPremultiplied) {
            float normAlpha = normComponents[normOffset+numColorComponents];
            for (int i=0; i < numColorComponents; i++) {
                components[offset+i] = (int) (normComponents[normOffset+i]
                                              * ((1<<nBits[i]) - 1)
                                              * normAlpha + 0.5f);
            }
            components[offset+numColorComponents] = (int)
                (normAlpha * ((1<<nBits[numColorComponents]) - 1) + 0.5f);
        }
        else {
            for (int i=0; i < numComponents; i++) {
                components[offset+i] = (int) (normComponents[normOffset+i]
                                              * ((1<<nBits[i]) - 1) + 0.5f);
            }
        }

        return components;
    }

    /**
     * 返回一个包含所有颜色/alpha 组件的归一化形式的数组，给定一个未归一化的组件数组。未归一化的组件是介于 0 和 2<sup>n</sup> - 1 之间的无符号整数值，其中 n 是特定组件的位数。归一化的组件是介于每个组件的最小值和最大值之间的浮点值，这些值由此 <code>ColorModel</code> 的 <code>ColorSpace</code> 对象指定。如果此 <code>ColorModel</code> 的颜色组件值不能方便地表示为未归一化形式，则会抛出 <code>IllegalArgumentException</code>。如果 <code>normComponents</code> 数组为 <code>null</code>，则会分配一个新数组。返回 <code>normComponents</code> 数组。颜色/alpha 组件从 <code>normComponents</code> 数组的 <code>normOffset</code> 位置开始存储（即使该数组是由此方法分配的）。如果 <code>normComponents</code> 数组不为 <code>null</code> 且不足以容纳所有颜色和 alpha 组件（从 <code>normOffset</code> 开始），则会抛出 <code>ArrayIndexOutOfBoundsException</code>。如果 <code>components</code> 数组不足以容纳从 <code>offset</code> 开始的所有颜色和 alpha 组件，则会抛出 <code>IllegalArgumentException</code>。
     * <p>
     * 由于 <code>ColorModel</code> 是一个抽象类，任何实例都是一个子类的实例。此抽象类中的此方法的默认实现假设此类的组件值可以方便地表示为未归一化形式。因此，可能有实例不支持未归一化形式的子类必须覆盖此方法。
     * @param components 包含未归一化组件的数组
     * @param offset 从 <code>components</code> 数组中开始检索未归一化组件的偏移量
     * @param normComponents 接收归一化组件的数组
     * @param normOffset 从 <code>normComponents</code> 数组中开始存储归一化组件的索引
     * @return 包含归一化颜色和 alpha 组件的数组。
     * @throws IllegalArgumentException 如果此 <code>ColorModel</code> 的组件值不能方便地表示为未归一化形式。
     * @throws UnsupportedOperationException 如果此 <code>ColorModel</code> 的构造函数调用了 <code>super(bits)</code> 构造函数，但未覆盖此方法。请参阅构造函数，{@link #ColorModel(int)}。
     * @throws UnsupportedOperationException 如果此方法无法确定每个组件的位数。
     */
    public float[] getNormalizedComponents(int[] components, int offset,
                                           float[] normComponents,
                                           int normOffset) {
        // 确保没有使用自定义颜色模型
        // 调用了 super(bits) 构造函数。
        if (colorSpace == null) {
            throw new UnsupportedOperationException("此颜色模型不支持此方法。");
        }
        if (nBits == null) {
            throw new UnsupportedOperationException ("此方法不支持。" +
                                         "无法确定每个组件的位数。");
        }

        if ((components.length - offset) < numComponents) {
            throw new
                IllegalArgumentException(
                        "组件数量不正确。期望 " +
                        numComponents);
        }

        if (normComponents == null) {
            normComponents = new float[numComponents+normOffset];
        }

        if (supportsAlpha && isAlphaPremultiplied) {
            // 归一化坐标是非预乘的
            float normAlpha = (float)components[offset+numColorComponents];
            normAlpha /= (float) ((1<<nBits[numColorComponents]) - 1);
            if (normAlpha != 0.0f) {
                for (int i=0; i < numColorComponents; i++) {
                    normComponents[normOffset+i] =
                        ((float) components[offset+i]) /
                        (normAlpha * ((float) ((1<<nBits[i]) - 1)));
                }
            } else {
                for (int i=0; i < numColorComponents; i++) {
                    normComponents[normOffset+i] = 0.0f;
                }
            }
            normComponents[normOffset+numColorComponents] = normAlpha;
        }
        else {
            for (int i=0; i < numComponents; i++) {
                normComponents[normOffset+i] = ((float) components[offset+i]) /
                                               ((float) ((1<<nBits[i]) - 1));
            }
        }

        return normComponents;
    }

    /**
     * 返回一个表示此 <code>ColorModel</code> 中像素值的 <code>int</code>。如果此 <code>ColorModel</code> 的像素值不能方便地表示为单个 <code>int</code> 或者颜色组件值不能方便地表示为未归一化形式，则会抛出 <code>IllegalArgumentException</code>。如果 <code>components</code> 数组不足以容纳从 <code>offset</code> 开始的所有颜色和 alpha 组件，则会抛出 <code>ArrayIndexOutOfBoundsException</code>。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是一个子类的实例。子类必须覆盖此方法，因为此抽象类中的实现会抛出 <code>UnsupportedOperationException</code>。
     * @param components 包含未归一化颜色和 alpha 组件的数组
     * @param offset 从 <code>components</code> 数组中开始检索颜色和 alpha 组件的索引
     * @return 一个表示此 <code>ColorModel</code> 中指定组件的 <code>int</code> 像素值。
     * @throws IllegalArgumentException 如果此 <code>ColorModel</code> 的像素值不能方便地表示为单个 <code>int</code>
     * @throws IllegalArgumentException 如果此 <code>ColorModel</code> 的组件值不能方便地表示为未归一化形式
     * @throws ArrayIndexOutOfBoundsException 如果 <code>components</code> 数组不足以容纳从 <code>offset</code> 开始的所有颜色和 alpha 组件
     * @throws UnsupportedOperationException 如果此 <code>ColorModel</code> 不支持此方法
     */
    public int getDataElement(int[] components, int offset) {
        throw new UnsupportedOperationException("此颜色模型不支持此方法。");
    }

    /**
     * 返回一个表示此 <code>ColorModel</code> 中像素值的数据元素数组，给定一个包含未归一化颜色/alpha 组件的数组。此数组可以传递给 <code>WritableRaster</code> 对象的 <code>setDataElements</code> 方法。如果此 <code>ColorModel</code> 的颜色组件值不能方便地表示为未归一化形式，则会抛出 <code>IllegalArgumentException</code>。如果 <code>components</code> 数组不足以容纳从 <code>offset</code> 开始的所有颜色和 alpha 组件，则会抛出 <code>ArrayIndexOutOfBoundsException</code>。如果 <code>obj</code> 变量为 <code>null</code>，则会分配一个新数组。如果 <code>obj</code> 不为 <code>null</code>，则必须是类型为 <code>transferType</code> 的原始数组；否则，会抛出 <code>ClassCastException</code>。如果 <code>obj</code> 不足以容纳此 <code>ColorModel</code> 的像素值，则会抛出 <code>ArrayIndexOutOfBoundsException</code>。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是一个子类的实例。子类必须覆盖此方法，因为此抽象类中的实现会抛出 <code>UnsupportedOperationException</code>。
     * @param components 包含未归一化颜色和 alpha 组件的数组
     * @param offset 从 <code>components</code> 数组中开始检索颜色和 alpha 组件的索引
     * @param obj 表示颜色和 alpha 组件的 <code>Object</code>
     * @return 表示颜色和 alpha 组件的 <code>Object</code> 数组。
     * @throws ClassCastException 如果 <code>obj</code> 不是类型为 <code>transferType</code> 的原始数组
     * @throws ArrayIndexOutOfBoundsException 如果 <code>obj</code> 不足以容纳此 <code>ColorModel</code> 的像素值，或者 <code>components</code> 数组不足以容纳从 <code>offset</code> 开始的所有颜色和 alpha 组件
     * @throws IllegalArgumentException 如果此 <code>ColorModel</code> 的组件值不能方便地表示为未归一化形式
     * @throws UnsupportedOperationException 如果此 <code>ColorModel</code> 不支持此方法
     * @see WritableRaster#setDataElements
     * @see SampleModel#setDataElements
     */
    public Object getDataElements(int[] components, int offset, Object obj) {
        throw new UnsupportedOperationException("此颜色模型尚未实现此方法。");
    }

    /**
     * 返回一个表示此 <code>ColorModel</code> 中像素值的 <code>int</code>，给定一个包含归一化颜色/alpha 组件的数组。如果此 <code>ColorModel</code> 的像素值不能方便地表示为单个 <code>int</code>，则会抛出 <code>IllegalArgumentException</code>。如果 <code>normComponents</code> 数组不足以容纳从 <code>normOffset</code> 开始的所有颜色和 alpha 组件，则会抛出 <code>ArrayIndexOutOfBoundsException</code>。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是一个子类的实例。此抽象类中的此方法的默认实现首先将归一化形式转换为未归一化形式，然后调用 <code>getDataElement(int[], int)</code>。可能有实例不支持未归一化形式的子类必须覆盖此方法。
     * @param normComponents 包含归一化颜色和 alpha 组件的数组
     * @param normOffset 从 <code>normComponents</code> 数组中开始检索颜色和 alpha 组件的索引
     * @return 一个表示此 <code>ColorModel</code> 中指定组件的 <code>int</code> 像素值。
     * @throws IllegalArgumentException 如果此 <code>ColorModel</code> 的像素值不能方便地表示为单个 <code>int</code>
     * @throws ArrayIndexOutOfBoundsException 如果 <code>normComponents</code> 数组不足以容纳从 <code>normOffset</code> 开始的所有颜色和 alpha 组件
     * @since 1.4
     */
    public int getDataElement(float[] normComponents, int normOffset) {
        int components[] = getUnnormalizedComponents(normComponents,
                                                     normOffset, null, 0);
        return getDataElement(components, 0);
    }

    /**
     * 返回一个表示此 <code>ColorModel</code> 中像素值的数据元素数组，给定一个包含归一化颜色/alpha 组件的数组。此数组可以传递给 <code>WritableRaster</code> 对象的 <code>setDataElements</code> 方法。如果 <code>normComponents</code> 数组不足以容纳从 <code>normOffset</code> 开始的所有颜色和 alpha 组件，则会抛出 <code>ArrayIndexOutOfBoundsException</code>。如果 <code>obj</code> 变量为 <code>null</code>，则会分配一个新数组。如果 <code>obj</code> 不为 <code>null</code>，则必须是类型为 <code>transferType</code> 的原始数组；否则，会抛出 <code>ClassCastException</code>。如果 <code>obj</code> 不足以容纳此 <code>ColorModel</code> 的像素值，则会抛出 <code>ArrayIndexOutOfBoundsException</code>。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是一个子类的实例。此抽象类中的此方法的默认实现首先将归一化形式转换为未归一化形式，然后调用 <code>getDataElement(int[], int, Object)</code>。可能有实例不支持未归一化形式的子类必须覆盖此方法。
     * @param normComponents 包含归一化颜色和 alpha 组件的数组
     * @param normOffset 从 <code>normComponents</code> 数组中开始检索颜色和 alpha 组件的索引
     * @param obj 用于存储返回像素的原始数据数组
     * @return 一个表示像素的原始数据数组的 <code>Object</code>
     * @throws ClassCastException 如果 <code>obj</code> 不是类型为 <code>transferType</code> 的原始数组
     * @throws ArrayIndexOutOfBoundsException 如果 <code>obj</code> 不足以容纳此 <code>ColorModel</code> 的像素值，或者 <code>normComponents</code> 数组不足以容纳从 <code>normOffset</code> 开始的所有颜色和 alpha 组件
     * @see WritableRaster#setDataElements
     * @see SampleModel#setDataElements
     * @since 1.4
     */
    public Object getDataElements(float[] normComponents, int normOffset,
                                  Object obj) {
        int components[] = getUnnormalizedComponents(normComponents,
                                                     normOffset, null, 0);
        return getDataElements(components, 0, obj);
    }


                /**
     * 返回此 <code>ColorModel</code> 中所有颜色/透明度分量的归一化形式的数组。像素值由作为对象引用传递的 transferType 类型的数据元素数组指定。如果像素不是 transferType 类型的原始数组，则会抛出 <code>ClassCastException</code>。如果 <code>pixel</code> 不足以容纳此 <code>ColorModel</code> 的像素值，则会抛出 <code>ArrayIndexOutOfBoundsException</code>。归一化分量是介于 <code>ColorSpace</code> 对象为该 <code>ColorModel</code> 指定的每个分量的最小值和最大值之间的浮点值。如果 <code>normComponents</code> 数组为 <code>null</code>，将分配一个新数组。返回 <code>normComponents</code> 数组。颜色/透明度分量存储在 <code>normComponents</code> 数组中，从 <code>normOffset</code> 开始（即使该数组是由此方法分配的）。如果 <code>normComponents</code> 数组不为 <code>null</code> 且不足以容纳所有颜色和透明度分量（从 <code>normOffset</code> 开始），则会抛出 <code>ArrayIndexOutOfBoundsException</code>。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是一个子类的实例。此抽象类中的此方法的默认实现首先使用 <code>getComponents(Object, int[], int)</code> 获取未归一化的颜色和透明度分量，然后调用 <code>getNormalizedComponents(int[], int, float[], int)</code>。不支持未归一化形式的子类实例必须覆盖此方法。
     * @param pixel 指定的像素
     * @param normComponents 接收归一化分量的数组
     * @param normOffset <code>normComponents</code> 数组中开始存储归一化分量的位置
     * @return 包含归一化颜色和透明度分量的数组。
     * @throws ClassCastException 如果 <code>pixel</code> 不是 transferType 类型的原始数组
     * @throws ArrayIndexOutOfBoundsException 如果 <code>normComponents</code> 数组不足以容纳所有颜色和透明度分量（从 <code>normOffset</code> 开始）
     * @throws ArrayIndexOutOfBoundsException 如果 <code>pixel</code> 不足以容纳此 <code>ColorModel</code> 的像素值
     * @throws UnsupportedOperationException 如果此 <code>ColorModel</code> 的构造函数调用了 <code>super(bits)</code> 构造函数但未覆盖此方法。请参阅构造函数，{@link #ColorModel(int)}。
     * @throws UnsupportedOperationException 如果此方法无法确定每个分量的位数
     * @since 1.4
     */
    public float[] getNormalizedComponents(Object pixel,
                                           float[] normComponents,
                                           int normOffset) {
        int components[] = getComponents(pixel, null, 0);
        return getNormalizedComponents(components, 0,
                                       normComponents, normOffset);
    }

    /**
     * 测试指定的 <code>Object</code> 是否是 <code>ColorModel</code> 的实例，并且是否等于此 <code>ColorModel</code>。
     * @param obj 要测试的 <code>Object</code>
     * @return 如果指定的 <code>Object</code> 是 <code>ColorModel</code> 的实例并且等于此 <code>ColorModel</code>，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof ColorModel)) {
            return false;
        }
        ColorModel cm = (ColorModel) obj;

        if (this == cm) {
            return true;
        }
        if (supportsAlpha != cm.hasAlpha() ||
            isAlphaPremultiplied != cm.isAlphaPremultiplied() ||
            pixel_bits != cm.getPixelSize() ||
            transparency != cm.getTransparency() ||
            numComponents != cm.getNumComponents())
        {
            return false;
        }

        int[] nb = cm.getComponentSize();

        if ((nBits != null) && (nb != null)) {
            for (int i = 0; i < numComponents; i++) {
                if (nBits[i] != nb[i]) {
                    return false;
                }
            }
        } else {
            return ((nBits == null) && (nb == null));
        }

        return true;
    }

    /**
     * 返回此 <code>ColorModel</code> 的哈希码。
     *
     * @return 此 <code>ColorModel</code> 的哈希码。
     */
    public int hashCode() {

        int result = 0;

        result = (supportsAlpha ? 2 : 3) +
                 (isAlphaPremultiplied ? 4 : 5) +
                 pixel_bits * 6 +
                 transparency * 7 +
                 numComponents * 8;

        if (nBits != null) {
            for (int i = 0; i < numComponents; i++) {
                result = result + nBits[i] * (i + 9);
            }
        }

        return result;
    }

    /**
     * 返回与此 <code>ColorModel</code> 关联的 <code>ColorSpace</code>。
     * @return 此 <code>ColorModel</code> 的 <code>ColorSpace</code>。
     */
    final public ColorSpace getColorSpace() {
        return colorSpace;
    }

    /**
     * 强制使光栅数据与 <code>isAlphaPremultiplied</code> 变量指定的状态匹配，假设数据当前正确描述了此 <code>ColorModel</code>。它可能会乘以或除以颜色光栅数据的 alpha，或者如果数据处于正确状态，则不执行任何操作。如果需要强制转换，此方法还将返回一个 <code>ColorModel</code> 实例，其 <code>isAlphaPremultiplied</code> 标志设置适当。如果此 <code>ColorModel</code> 不支持此方法，将抛出 <code>UnsupportedOperationException</code>。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是一个子类的实例。子类必须覆盖此方法，因为此抽象类中的实现会抛出 <code>UnsupportedOperationException</code>。
     * @param raster <code>WritableRaster</code> 数据
     * @param isAlphaPremultiplied 如果 alpha 是预乘的，则为 <code>true</code>；否则为 <code>false</code>
     * @return 一个表示强制数据的 <code>ColorModel</code> 对象。
     */
    public ColorModel coerceData (WritableRaster raster,
                                  boolean isAlphaPremultiplied) {
        throw new UnsupportedOperationException
            ("此颜色模型不支持此方法");
    }

    /**
      * 如果 <code>raster</code> 与此 <code>ColorModel</code> 兼容，则返回 <code>true</code>；否则返回 <code>false</code>。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是一个子类的实例。子类必须覆盖此方法，因为此抽象类中的实现会抛出 <code>UnsupportedOperationException</code>。
      * @param raster 要测试兼容性的 {@link Raster} 对象
      * @return 如果 <code>raster</code> 与此 <code>ColorModel</code> 兼容，则返回 <code>true</code>。
      * @throws UnsupportedOperationException 如果此 <code>ColorModel</code> 未实现此方法
      */
    public boolean isCompatibleRaster(Raster raster) {
        throw new UnsupportedOperationException(
            "此 <code>ColorModel</code> 未实现此方法");
    }

    /**
     * 创建一个具有指定宽度和高度的 <code>WritableRaster</code>，其数据布局（<code>SampleModel</code>）与此 <code>ColorModel</code> 兼容。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是一个子类的实例。子类必须覆盖此方法，因为此抽象类中的实现会抛出 <code>UnsupportedOperationException</code>。
     * @param w 要应用于新 <code>WritableRaster</code> 的宽度
     * @param h 要应用于新 <code>WritableRaster</code> 的高度
     * @return 一个具有指定宽度和高度的 <code>WritableRaster</code> 对象。
     * @throws UnsupportedOperationException 如果此 <code>ColorModel</code> 不支持此方法
     * @see WritableRaster
     * @see SampleModel
     */
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        throw new UnsupportedOperationException
            ("此颜色模型不支持此方法");
    }

    /**
     * 创建一个具有指定宽度和高度的 <code>SampleModel</code>，其数据布局与此 <code>ColorModel</code> 兼容。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是一个子类的实例。子类必须覆盖此方法，因为此抽象类中的实现会抛出 <code>UnsupportedOperationException</code>。
     * @param w 要应用于新 <code>SampleModel</code> 的宽度
     * @param h 要应用于新 <code>SampleModel</code> 的高度
     * @return 一个具有指定宽度和高度的 <code>SampleModel</code> 对象。
     * @throws UnsupportedOperationException 如果此 <code>ColorModel</code> 不支持此方法
     * @see SampleModel
     */
    public SampleModel createCompatibleSampleModel(int w, int h) {
        throw new UnsupportedOperationException
            ("此颜色模型不支持此方法");
    }

    /** 检查 <code>SampleModel</code> 是否与此 <code>ColorModel</code> 兼容。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是一个子类的实例。子类必须覆盖此方法，因为此抽象类中的实现会抛出 <code>UnsupportedOperationException</code>。
     * @param sm 指定的 <code>SampleModel</code>
     * @return 如果指定的 <code>SampleModel</code> 与此 <code>ColorModel</code> 兼容，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @throws UnsupportedOperationException 如果此 <code>ColorModel</code> 不支持此方法
     * @see SampleModel
     */
    public boolean isCompatibleSampleModel(SampleModel sm) {
        throw new UnsupportedOperationException
            ("此颜色模型不支持此方法");
    }

    /**
     * 释放与此 <code>ColorModel</code> 关联的系统资源，一旦此 <code>ColorModel</code> 不再被引用。
     */
    public void finalize() {
    }


    /**
     * 从输入的 <code>Raster</code> 中提取图像的 alpha 通道，返回一个 <code>Raster</code>，前提是此 <code>ColorModel</code> 的像素值表示颜色和 alpha 信息为单独的空间带（例如 {@link ComponentColorModel} 和 <code>DirectColorModel</code>）。此方法假设与此 <code>ColorModel</code> 关联的 <code>Raster</code> 对象如果存在 alpha 带，则将其存储为图像数据的最后一个带。如果此 <code>ColorModel</code> 没有单独的空间 alpha 通道，则返回 <code>null</code>。如果这是一个具有 alpha 查找表的 <code>IndexColorModel</code>，此方法将返回 <code>null</code>，因为没有离散的空间 alpha 通道。此方法将创建一个新的 <code>Raster</code>（但将共享数据数组）。由于 <code>ColorModel</code> 是一个抽象类，任何实例都是一个子类的实例。子类必须覆盖此方法以获得任何行为，而不是返回 <code>null</code>，因为此抽象类中的实现返回 <code>null</code>。
     * @param raster 指定的 <code>Raster</code>
     * @return 从指定的 <code>Raster</code> 中提取的 alpha 通道的 <code>Raster</code>。
     */
    public WritableRaster getAlphaRaster(WritableRaster raster) {
        return null;
    }

    /**
     * 返回此 <code>ColorModel</code> 对象的内容的 <code>String</code> 表示。
     * @return 一个表示此 <code>ColorModel</code> 对象内容的 <code>String</code>。
     */
    public String toString() {
       return new String("ColorModel: #pixelBits = "+pixel_bits
                         + " numComponents = "+numComponents
                         + " color space = "+colorSpace
                         + " transparency = "+transparency
                         + " has alpha = "+supportsAlpha
                         + " isAlphaPre = "+isAlphaPremultiplied
                         );
    }

    static int getDefaultTransferType(int pixel_bits) {
        if (pixel_bits <= 8) {
            return DataBuffer.TYPE_BYTE;
        } else if (pixel_bits <= 16) {
            return DataBuffer.TYPE_USHORT;
        } else if (pixel_bits <= 32) {
            return DataBuffer.TYPE_INT;
        } else {
            return DataBuffer.TYPE_UNDEFINED;
        }
    }

    static byte[] l8Tos8 = null;   // 8 位线性到 8 位非线性 sRGB 查找表
    static byte[] s8Tol8 = null;   // 8 位非线性 sRGB 到 8 位线性查找表
    static byte[] l16Tos8 = null;  // 16 位线性到 8 位非线性 sRGB 查找表
    static short[] s8Tol16 = null; // 8 位非线性 sRGB 到 16 位线性查找表

                                // 用于灰度转换的查找表映射
    static Map<ICC_ColorSpace, byte[]> g8Tos8Map = null;     // 8 位灰度值到 8 位 sRGB 值
    static Map<ICC_ColorSpace, byte[]> lg16Toog8Map = null;  // 16 位线性到 8 位“其他”灰度
    static Map<ICC_ColorSpace, byte[]> g16Tos8Map = null;    // 16 位灰度值到 8 位 sRGB 值
    static Map<ICC_ColorSpace, short[]> lg16Toog16Map = null; // 16 位线性到 16 位“其他”灰度

    static boolean isLinearRGBspace(ColorSpace cs) {
        // 注意：如果线性 RGB 空间尚未创建，CMM.LINEAR_RGBspace 将为 null。
        return (cs == CMSManager.LINEAR_RGBspace);
    }

    static boolean isLinearGRAYspace(ColorSpace cs) {
        // 注意：如果线性灰度空间尚未创建，CMM.GRAYspace 将为 null。
        return (cs == CMSManager.GRAYspace);
    }

    static byte[] getLinearRGB8TosRGB8LUT() {
        if (l8Tos8 == null) {
            l8Tos8 = new byte[256];
            float input, output;
            // 线性 RGB 到非线性 sRGB 转换的算法来自 IEC 61966-2-1 国际标准，
            // 颜色管理 - 默认 RGB 颜色空间 - sRGB，
            // 第一版，1999-10，
            // 可在 http://www.iec.ch 订购
            for (int i = 0; i <= 255; i++) {
                input = ((float) i) / 255.0f;
                if (input <= 0.0031308f) {
                    output = input * 12.92f;
                } else {
                    output = 1.055f * ((float) Math.pow(input, (1.0 / 2.4)))
                             - 0.055f;
                }
                l8Tos8[i] = (byte) Math.round(output * 255.0f);
            }
        }
        return l8Tos8;
    }


                static byte[] getsRGB8ToLinearRGB8LUT() {
        if (s8Tol8 == null) {
            s8Tol8 = new byte[256];
            float input, output;
            // IEC 61966-2-1 国际标准中的算法
            for (int i = 0; i <= 255; i++) {
                input = ((float) i) / 255.0f;
                if (input <= 0.04045f) {
                    output = input / 12.92f;
                } else {
                    output = (float) Math.pow((input + 0.055f) / 1.055f, 2.4);
                }
                s8Tol8[i] = (byte) Math.round(output * 255.0f);
            }
        }
        return s8Tol8;
    }

    static byte[] getLinearRGB16TosRGB8LUT() {
        if (l16Tos8 == null) {
            l16Tos8 = new byte[65536];
            float input, output;
            // IEC 61966-2-1 国际标准中的算法
            for (int i = 0; i <= 65535; i++) {
                input = ((float) i) / 65535.0f;
                if (input <= 0.0031308f) {
                    output = input * 12.92f;
                } else {
                    output = 1.055f * ((float) Math.pow(input, (1.0 / 2.4)))
                             - 0.055f;
                }
                l16Tos8[i] = (byte) Math.round(output * 255.0f);
            }
        }
        return l16Tos8;
    }

    static short[] getsRGB8ToLinearRGB16LUT() {
        if (s8Tol16 == null) {
            s8Tol16 = new short[256];
            float input, output;
            // IEC 61966-2-1 国际标准中的算法
            for (int i = 0; i <= 255; i++) {
                input = ((float) i) / 255.0f;
                if (input <= 0.04045f) {
                    output = input / 12.92f;
                } else {
                    output = (float) Math.pow((input + 0.055f) / 1.055f, 2.4);
                }
                s8Tol16[i] = (short) Math.round(output * 65535.0f);
            }
        }
        return s8Tol16;
    }

    /*
     * 返回一个字节查找表，将 8 位灰度值从 grayCS 颜色空间转换为适当的 8 位 sRGB 值。即，如果 lut
     * 是此方法返回的字节数组，且 sval = lut[gval]，那么 sRGB 三元组 (sval,sval,sval) 是 gval 的最佳匹配。
     * 将任何计算出的查找表的引用缓存到 Map 中。
     */
    static byte[] getGray8TosRGB8LUT(ICC_ColorSpace grayCS) {
        if (isLinearGRAYspace(grayCS)) {
            return getLinearRGB8TosRGB8LUT();
        }
        if (g8Tos8Map != null) {
            byte[] g8Tos8LUT = g8Tos8Map.get(grayCS);
            if (g8Tos8LUT != null) {
                return g8Tos8LUT;
            }
        }
        byte[] g8Tos8LUT = new byte[256];
        for (int i = 0; i <= 255; i++) {
            g8Tos8LUT[i] = (byte) i;
        }
        ColorTransform[] transformList = new ColorTransform[2];
        PCMM mdl = CMSManager.getModule();
        ICC_ColorSpace srgbCS =
            (ICC_ColorSpace) ColorSpace.getInstance(ColorSpace.CS_sRGB);
        transformList[0] = mdl.createTransform(
            grayCS.getProfile(), ColorTransform.Any, ColorTransform.In);
        transformList[1] = mdl.createTransform(
            srgbCS.getProfile(), ColorTransform.Any, ColorTransform.Out);
        ColorTransform t = mdl.createTransform(transformList);
        byte[] tmp = t.colorConvert(g8Tos8LUT, null);
        for (int i = 0, j= 2; i <= 255; i++, j += 3) {
            // 由于输入颜色空间是灰度空间，colorConvert 的所有三个组件应该相等。
            // 但是，结果中存在轻微的异常。
            // 从索引 2 开始复制 tmp，因为 colorConvert 对第三个组件似乎更准确！
            g8Tos8LUT[i] = tmp[j];
        }
        if (g8Tos8Map == null) {
            g8Tos8Map = Collections.synchronizedMap(new WeakHashMap<ICC_ColorSpace, byte[]>(2));
        }
        g8Tos8Map.put(grayCS, g8Tos8LUT);
        return g8Tos8LUT;
    }

    /*
     * 返回一个字节查找表，将 CS_GRAY 线性灰度颜色空间中的 16 位灰度值转换为 grayCS 颜色空间中的适当 8 位值。
     * 将任何计算出的查找表的引用缓存到 Map 中。
     */
    static byte[] getLinearGray16ToOtherGray8LUT(ICC_ColorSpace grayCS) {
        if (lg16Toog8Map != null) {
            byte[] lg16Toog8LUT = lg16Toog8Map.get(grayCS);
            if (lg16Toog8LUT != null) {
                return lg16Toog8LUT;
            }
        }
        short[] tmp = new short[65536];
        for (int i = 0; i <= 65535; i++) {
            tmp[i] = (short) i;
        }
        ColorTransform[] transformList = new ColorTransform[2];
        PCMM mdl = CMSManager.getModule();
        ICC_ColorSpace lgCS =
            (ICC_ColorSpace) ColorSpace.getInstance(ColorSpace.CS_GRAY);
        transformList[0] = mdl.createTransform (
            lgCS.getProfile(), ColorTransform.Any, ColorTransform.In);
        transformList[1] = mdl.createTransform (
            grayCS.getProfile(), ColorTransform.Any, ColorTransform.Out);
        ColorTransform t = mdl.createTransform(transformList);
        tmp = t.colorConvert(tmp, null);
        byte[] lg16Toog8LUT = new byte[65536];
        for (int i = 0; i <= 65535; i++) {
            // 将无符号短整型 (0 - 65535) 缩放到无符号字节 (0 - 255)
            lg16Toog8LUT[i] =
                (byte) (((float) (tmp[i] & 0xffff)) * (1.0f /257.0f) + 0.5f);
        }
        if (lg16Toog8Map == null) {
            lg16Toog8Map = Collections.synchronizedMap(new WeakHashMap<ICC_ColorSpace, byte[]>(2));
        }
        lg16Toog8Map.put(grayCS, lg16Toog8LUT);
        return lg16Toog8LUT;
    }

    /*
     * 返回一个字节查找表，将 grayCS 颜色空间中的 16 位灰度值转换为适当的 8 位 sRGB 值。即，如果 lut
     * 是此方法返回的字节数组，且 sval = lut[gval]，那么 sRGB 三元组 (sval,sval,sval) 是 gval 的最佳匹配。
     * 将任何计算出的查找表的引用缓存到 Map 中。
     */
    static byte[] getGray16TosRGB8LUT(ICC_ColorSpace grayCS) {
        if (isLinearGRAYspace(grayCS)) {
            return getLinearRGB16TosRGB8LUT();
        }
        if (g16Tos8Map != null) {
            byte[] g16Tos8LUT = g16Tos8Map.get(grayCS);
            if (g16Tos8LUT != null) {
                return g16Tos8LUT;
            }
        }
        short[] tmp = new short[65536];
        for (int i = 0; i <= 65535; i++) {
            tmp[i] = (short) i;
        }
        ColorTransform[] transformList = new ColorTransform[2];
        PCMM mdl = CMSManager.getModule();
        ICC_ColorSpace srgbCS =
            (ICC_ColorSpace) ColorSpace.getInstance(ColorSpace.CS_sRGB);
        transformList[0] = mdl.createTransform (
            grayCS.getProfile(), ColorTransform.Any, ColorTransform.In);
        transformList[1] = mdl.createTransform (
            srgbCS.getProfile(), ColorTransform.Any, ColorTransform.Out);
        ColorTransform t = mdl.createTransform(transformList);
        tmp = t.colorConvert(tmp, null);
        byte[] g16Tos8LUT = new byte[65536];
        for (int i = 0, j= 2; i <= 65535; i++, j += 3) {
            // 由于输入颜色空间是灰度空间，colorConvert 的所有三个组件应该相等。
            // 但是，结果中存在轻微的异常。
            // 从索引 2 开始复制 tmp，因为 colorConvert 对第三个组件似乎更准确！

            // 将无符号短整型 (0 - 65535) 缩放到无符号字节 (0 - 255)
            g16Tos8LUT[i] =
                (byte) (((float) (tmp[j] & 0xffff)) * (1.0f /257.0f) + 0.5f);
        }
        if (g16Tos8Map == null) {
            g16Tos8Map = Collections.synchronizedMap(new WeakHashMap<ICC_ColorSpace, byte[]>(2));
        }
        g16Tos8Map.put(grayCS, g16Tos8LUT);
        return g16Tos8LUT;
    }

    /*
     * 返回一个短整型查找表，将 CS_GRAY 线性灰度颜色空间中的 16 位灰度值转换为 grayCS 颜色空间中的适当 16 位值。
     * 将任何计算出的查找表的引用缓存到 Map 中。
     */
    static short[] getLinearGray16ToOtherGray16LUT(ICC_ColorSpace grayCS) {
        if (lg16Toog16Map != null) {
            short[] lg16Toog16LUT = lg16Toog16Map.get(grayCS);
            if (lg16Toog16LUT != null) {
                return lg16Toog16LUT;
            }
        }
        short[] tmp = new short[65536];
        for (int i = 0; i <= 65535; i++) {
            tmp[i] = (short) i;
        }
        ColorTransform[] transformList = new ColorTransform[2];
        PCMM mdl = CMSManager.getModule();
        ICC_ColorSpace lgCS =
            (ICC_ColorSpace) ColorSpace.getInstance(ColorSpace.CS_GRAY);
        transformList[0] = mdl.createTransform (
            lgCS.getProfile(), ColorTransform.Any, ColorTransform.In);
        transformList[1] = mdl.createTransform(
            grayCS.getProfile(), ColorTransform.Any, ColorTransform.Out);
        ColorTransform t = mdl.createTransform(
            transformList);
        short[] lg16Toog16LUT = t.colorConvert(tmp, null);
        if (lg16Toog16Map == null) {
            lg16Toog16Map = Collections.synchronizedMap(new WeakHashMap<ICC_ColorSpace, short[]>(2));
        }
        lg16Toog16Map.put(grayCS, lg16Toog16LUT);
        return lg16Toog16LUT;
    }

}
