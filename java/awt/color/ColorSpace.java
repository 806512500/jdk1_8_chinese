
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

/*
 **********************************************************************
 **********************************************************************
 **********************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 *** As  an unpublished  work pursuant to Title 17 of the United    ***
 *** States Code.  All rights reserved.                             ***
 **********************************************************************
 **********************************************************************
 **********************************************************************/

package java.awt.color;

import java.lang.annotation.Native;

import sun.java2d.cmm.PCMM;
import sun.java2d.cmm.CMSManager;


/**
 * 这个抽象类用于作为颜色空间标签，以标识 Color 对象或通过 ColorModel 对象标识的
 * 图像、BufferedImage 或 GraphicsDevice 的特定颜色空间。它包含将特定颜色空间中的颜色
 * 转换为 sRGB 和 CIEXYZ 颜色空间的方法。
 * <p>
 * 对于此类中的方法，颜色表示为浮点数数组，其范围由每个 ColorSpace 定义。对于许多
 * ColorSpaces（例如 sRGB），这个范围是 0.0 到 1.0。然而，某些 ColorSpaces 的组件值
 * 有不同的范围。提供了查询每个组件最小和最大归一化值的方法。
 * <p>
 * 定义了几个变量，用于引用颜色空间类型（例如 TYPE_RGB、TYPE_XYZ 等）和特定颜色空间
 * （例如 CS_sRGB 和 CS_CIEXYZ）。sRGB 是一个提议的标准 RGB 颜色空间。更多信息请参见
 * <A href="http://www.w3.org/pub/WWW/Graphics/Color/sRGB.html">
 * http://www.w3.org/pub/WWW/Graphics/Color/sRGB.html
 * </A>。
 * <p>
 * 将颜色转换为 CIEXYZ 颜色空间的方法的目的是支持在任何两个颜色空间之间进行高精度的
 * 转换。预计特定实现的 ColorSpace 子类（例如 ICC_ColorSpace）将基于底层平台颜色管理
 * 系统支持高性能转换。
 * <p>
 * toCIEXYZ/fromCIEXYZ 方法中使用的 CS_CIEXYZ 空间可以描述如下：
<pre>

&nbsp;     CIEXYZ
&nbsp;     视图照明：200 勒克斯
&nbsp;     视图白点：CIE D50
&nbsp;     媒体白点：“完全反射漫射器”——D50
&nbsp;     媒体黑点：0 勒克斯或 0 反射率
&nbsp;     光晕：1%
&nbsp;     环境：媒体白点的 20%
&nbsp;     媒体描述：反射打印（即 RLAB，Hunt 视图媒体）
&nbsp;     注意：对于创建此转换空间的 ICC 配置文件的开发人员，以下内容适用。使用简单的
&nbsp;           Von Kries 白点适应并将其折叠到 3X3 矩阵参数中，并将光晕和环境效果折叠到
&nbsp;           三个一维查找表中（假设使用最小模型进行监视器）。

</pre>
 *
 * @see ICC_ColorSpace
 */

public abstract class ColorSpace implements java.io.Serializable {

    static final long serialVersionUID = -409452704308689724L;

    private int type;
    private int numComponents;
    private transient String [] compName = null;

    // 预定义颜色空间的单例缓存。
    private static ColorSpace sRGBspace;
    private static ColorSpace XYZspace;
    private static ColorSpace PYCCspace;
    private static ColorSpace GRAYspace;
    private static ColorSpace LINEAR_RGBspace;

    /**
     * 任何 XYZ 颜色空间系列。
     */
    @Native public static final int TYPE_XYZ = 0;

    /**
     * 任何 Lab 颜色空间系列。
     */
    @Native public static final int TYPE_Lab = 1;

    /**
     * 任何 Luv 颜色空间系列。
     */
    @Native public static final int TYPE_Luv = 2;

    /**
     * 任何 YCbCr 颜色空间系列。
     */
    @Native public static final int TYPE_YCbCr = 3;

    /**
     * 任何 Yxy 颜色空间系列。
     */
    @Native public static final int TYPE_Yxy = 4;

    /**
     * 任何 RGB 颜色空间系列。
     */
    @Native public static final int TYPE_RGB = 5;

    /**
     * 任何 GRAY 颜色空间系列。
     */
    @Native public static final int TYPE_GRAY = 6;

    /**
     * 任何 HSV 颜色空间系列。
     */
    @Native public static final int TYPE_HSV = 7;

    /**
     * 任何 HLS 颜色空间系列。
     */
    @Native public static final int TYPE_HLS = 8;

    /**
     * 任何 CMYK 颜色空间系列。
     */
    @Native public static final int TYPE_CMYK = 9;

    /**
     * 任何 CMY 颜色空间系列。
     */
    @Native public static final int TYPE_CMY = 11;

    /**
     * 通用 2 组件颜色空间。
     */
    @Native public static final int TYPE_2CLR = 12;

    /**
     * 通用 3 组件颜色空间。
     */
    @Native public static final int TYPE_3CLR = 13;

    /**
     * 通用 4 组件颜色空间。
     */
    @Native public static final int TYPE_4CLR = 14;

    /**
     * 通用 5 组件颜色空间。
     */
    @Native public static final int TYPE_5CLR = 15;

    /**
     * 通用 6 组件颜色空间。
     */
    @Native public static final int TYPE_6CLR = 16;

    /**
     * 通用 7 组件颜色空间。
     */
    @Native public static final int TYPE_7CLR = 17;

    /**
     * 通用 8 组件颜色空间。
     */
    @Native public static final int TYPE_8CLR = 18;

    /**
     * 通用 9 组件颜色空间。
     */
    @Native public static final int TYPE_9CLR = 19;

    /**
     * 通用 10 组件颜色空间。
     */
    @Native public static final int TYPE_ACLR = 20;

    /**
     * 通用 11 组件颜色空间。
     */
    @Native public static final int TYPE_BCLR = 21;

    /**
     * 通用 12 组件颜色空间。
     */
    @Native public static final int TYPE_CCLR = 22;

    /**
     * 通用 13 组件颜色空间。
     */
    @Native public static final int TYPE_DCLR = 23;

    /**
     * 通用 14 组件颜色空间。
     */
    @Native public static final int TYPE_ECLR = 24;

    /**
     * 通用 15 组件颜色空间。
     */
    @Native public static final int TYPE_FCLR = 25;

    /**
     * 定义在
     * <A href="http://www.w3.org/pub/WWW/Graphics/Color/sRGB.html">
     * http://www.w3.org/pub/WWW/Graphics/Color/sRGB.html
     * </A> 的 sRGB 颜色空间。
     */
    @Native public static final int CS_sRGB = 1000;

    /**
     * 内置的线性 RGB 颜色空间。此空间基于与 CS_sRGB 相同的 RGB 原色，但具有线性色调再现曲线。
     */
    @Native public static final int CS_LINEAR_RGB = 1004;

    /**
     * 上面定义的 CIEXYZ 转换颜色空间。
     */
    @Native public static final int CS_CIEXYZ = 1001;

    /**
     * Photo YCC 转换颜色空间。
     */
    @Native public static final int CS_PYCC = 1002;

    /**
     * 内置的线性灰度颜色空间。
     */
    @Native public static final int CS_GRAY = 1003;


    /**
     * 构造一个给定颜色空间类型和组件数量的 ColorSpace 对象。
     * @param type 一个 <CODE>ColorSpace</CODE> 类型常量
     * @param numcomponents 颜色空间中的组件数量
     */
    protected ColorSpace (int type, int numcomponents) {
        this.type = type;
        this.numComponents = numcomponents;
    }


    /**
     * 返回表示特定预定义颜色空间的 ColorSpace。
     * @param colorspace 由预定义类常量（例如 CS_sRGB、CS_LINEAR_RGB、
     *        CS_CIEXYZ、CS_GRAY 或 CS_PYCC）标识的特定颜色空间
     * @return 请求的 <CODE>ColorSpace</CODE> 对象
     */
    // 注意：此方法可能由特权线程调用。
    //       不要在该线程上调用客户端代码！
    public static ColorSpace getInstance (int colorspace)
    {
    ColorSpace    theColorSpace;

        switch (colorspace) {
        case CS_sRGB:
            synchronized(ColorSpace.class) {
                if (sRGBspace == null) {
                    ICC_Profile theProfile = ICC_Profile.getInstance (CS_sRGB);
                    sRGBspace = new ICC_ColorSpace (theProfile);
                }

                theColorSpace = sRGBspace;
            }
            break;

        case CS_CIEXYZ:
            synchronized(ColorSpace.class) {
                if (XYZspace == null) {
                    ICC_Profile theProfile =
                        ICC_Profile.getInstance (CS_CIEXYZ);
                    XYZspace = new ICC_ColorSpace (theProfile);
                }

                theColorSpace = XYZspace;
            }
            break;

        case CS_PYCC:
            synchronized(ColorSpace.class) {
                if (PYCCspace == null) {
                    ICC_Profile theProfile = ICC_Profile.getInstance (CS_PYCC);
                    PYCCspace = new ICC_ColorSpace (theProfile);
                }

                theColorSpace = PYCCspace;
            }
            break;


        case CS_GRAY:
            synchronized(ColorSpace.class) {
                if (GRAYspace == null) {
                    ICC_Profile theProfile = ICC_Profile.getInstance (CS_GRAY);
                    GRAYspace = new ICC_ColorSpace (theProfile);
                    /* 以允许从 java.awt.ColorModel 访问 */
                    CMSManager.GRAYspace = GRAYspace;
                }

                theColorSpace = GRAYspace;
            }
            break;


        case CS_LINEAR_RGB:
            synchronized(ColorSpace.class) {
                if (LINEAR_RGBspace == null) {
                    ICC_Profile theProfile =
                        ICC_Profile.getInstance(CS_LINEAR_RGB);
                    LINEAR_RGBspace = new ICC_ColorSpace (theProfile);
                    /* 以允许从 java.awt.ColorModel 访问 */
                    CMSManager.LINEAR_RGBspace = LINEAR_RGBspace;
                }

                theColorSpace = LINEAR_RGBspace;
            }
            break;


        default:
            throw new IllegalArgumentException ("未知颜色空间");
        }

        return theColorSpace;
    }


    /**
     * 如果 ColorSpace 是 CS_sRGB，则返回 true。
     * @return 如果这是 <CODE>CS_sRGB</CODE> 颜色空间，则返回 <CODE>true</CODE>，
     *         否则返回 <code>false</code>
     */
    public boolean isCS_sRGB () {
        /* REMIND - 确保我们已经知道 sRGBspace 存在 */
        return (this == sRGBspace);
    }

    /**
     * 将假设为在本 ColorSpace 中的颜色值转换为默认的 CS_sRGB 颜色空间。
     * <p>
     * 此方法使用旨在产生输入和输出颜色之间最佳感知匹配的算法转换颜色值。为了进行颜色值的
     * 色度转换，您应使用此颜色空间的 <code>toCIEXYZ</code>
     * 方法首先将输入颜色空间转换为 CS_CIEXYZ 颜色空间，然后使用 CS_sRGB 颜色空间的
     * <code>fromCIEXYZ</code> 方法将 CS_CIEXYZ 转换为输出颜色空间。
     * 请参阅 {@link #toCIEXYZ(float[]) toCIEXYZ} 和
     * {@link #fromCIEXYZ(float[]) fromCIEXYZ} 以获取更多信息。
     * <p>
     * @param colorvalue 一个浮点数组，长度至少为本 ColorSpace 的组件数量
     * @return 一个长度为 3 的浮点数组
     * @throws ArrayIndexOutOfBoundsException 如果数组长度不是至少为本 ColorSpace 的组件数量
     */
    public abstract float[] toRGB(float[] colorvalue);


    /**
     * 将假设为在默认 CS_sRGB 颜色空间中的颜色值转换为本 ColorSpace。
     * <p>
     * 此方法使用旨在产生输入和输出颜色之间最佳感知匹配的算法转换颜色值。为了进行颜色值的
     * 色度转换，您应使用 CS_sRGB 颜色空间的 <code>toCIEXYZ</code>
     * 方法首先将输入颜色空间转换为 CS_CIEXYZ 颜色空间，然后使用本颜色空间的
     * <code>fromCIEXYZ</code> 方法将 CS_CIEXYZ 转换为输出颜色空间。
     * 请参阅 {@link #toCIEXYZ(float[]) toCIEXYZ} 和
     * {@link #fromCIEXYZ(float[]) fromCIEXYZ} 以获取更多信息。
     * <p>
     * @param rgbvalue 一个长度至少为 3 的浮点数组
     * @return 一个长度等于本 ColorSpace 组件数量的浮点数组
     * @throws ArrayIndexOutOfBoundsException 如果数组长度不是至少为 3
     */
    public abstract float[] fromRGB(float[] rgbvalue);


    /**
     * 将假设为在本 ColorSpace 中的颜色值转换为 CS_CIEXYZ 转换颜色空间。
     * <p>
     * 此方法使用国际颜色联盟标准定义的相对色度法转换颜色值。这意味着此方法返回的 XYZ 值
     * 是相对于 CS_CIEXYZ 颜色空间的 D50 白点表示的。这种表示在两步颜色转换过程中很有用，
     * 在此过程中，颜色从输入颜色空间转换为 CS_CIEXYZ，然后转换为输出颜色空间。这种表示
     * 与使用色度计从给定颜色值测量的 XYZ 值不同。需要进一步的转换才能计算使用当前 CIE
     * 推荐实践测量的 XYZ 值。请参阅 <code>ICC_ColorSpace</code> 的
     * {@link ICC_ColorSpace#toCIEXYZ(float[]) toCIEXYZ} 方法以获取更多信息。
     * <p>
     * @param colorvalue 一个长度至少为本 ColorSpace 组件数量的浮点数组
     * @return 一个长度为 3 的浮点数组
     * @throws ArrayIndexOutOfBoundsException 如果数组长度不是至少为本 ColorSpace 的组件数量。
     */
    public abstract float[] toCIEXYZ(float[] colorvalue);


    /**
     * 将假设为 CS_CIEXYZ 转换颜色空间的颜色值转换为这个 ColorSpace。
     * <p>
     * 此方法使用相对色度法转换颜色值，这是由国际色彩联盟标准定义的。这意味着传递给此方法的 XYZ 参数值相对于 CS_CIEXYZ 颜色空间的 D50 白点表示。
     * 这种表示在两步颜色转换过程中非常有用，其中颜色从输入颜色空间转换为 CS_CIEXYZ，然后再转换为输出颜色空间。此方法返回的颜色值并不是通过色度计测量传递给方法的 XYZ 值时产生的颜色值。
     * 如果您有使用当前 CIE 推荐做法测量的 XYZ 值，它们必须转换为 D50 相对值，然后才能传递给此方法。
     * 有关详细信息，请参阅 <code>ICC_ColorSpace</code> 的 {@link ICC_ColorSpace#fromCIEXYZ(float[]) fromCIEXYZ} 方法。
     * <p>
     * @param colorvalue 长度至少为 3 的 float 数组
     * @return 长度等于此 ColorSpace 组件数量的 float 数组
     * @throws ArrayIndexOutOfBoundsException 如果数组长度不足 3
     */
    public abstract float[] fromCIEXYZ(float[] colorvalue);

    /**
     * 返回此 ColorSpace 的颜色空间类型（例如 TYPE_RGB、TYPE_XYZ 等）。类型定义了颜色空间的组件数量和解释，例如 TYPE_RGB 标识一个包含红色、绿色和蓝色三个组件的颜色空间。它不定义空间的特定颜色特性，例如原色的色度。
     *
     * @return 代表此 <CODE>ColorSpace</CODE> 类型的类型常量
     */
    public int getType() {
        return type;
    }

    /**
     * 返回此 ColorSpace 的组件数量。
     * @return 此 <CODE>ColorSpace</CODE> 的组件数量。
     */
    public int getNumComponents() {
        return numComponents;
    }

    /**
     * 返回给定组件索引的组件名称。
     *
     * @param idx 组件索引
     * @return 指定索引处的组件名称
     * @throws IllegalArgumentException 如果 <code>idx</code> 小于 0 或大于 numComponents - 1
     */
    public String getName (int idx) {
        /* REMIND - handle common cases here */
        if ((idx < 0) || (idx > numComponents - 1)) {
            throw new IllegalArgumentException(
                "Component index out of range: " + idx);
        }

        if (compName == null) {
            switch (type) {
                case ColorSpace.TYPE_XYZ:
                    compName = new String[] {"X", "Y", "Z"};
                    break;
                case ColorSpace.TYPE_Lab:
                    compName = new String[] {"L", "a", "b"};
                    break;
                case ColorSpace.TYPE_Luv:
                    compName = new String[] {"L", "u", "v"};
                    break;
                case ColorSpace.TYPE_YCbCr:
                    compName = new String[] {"Y", "Cb", "Cr"};
                    break;
                case ColorSpace.TYPE_Yxy:
                    compName = new String[] {"Y", "x", "y"};
                    break;
                case ColorSpace.TYPE_RGB:
                    compName = new String[] {"Red", "Green", "Blue"};
                    break;
                case ColorSpace.TYPE_GRAY:
                    compName = new String[] {"Gray"};
                    break;
                case ColorSpace.TYPE_HSV:
                    compName = new String[] {"Hue", "Saturation", "Value"};
                    break;
                case ColorSpace.TYPE_HLS:
                    compName = new String[] {"Hue", "Lightness",
                                             "Saturation"};
                    break;
                case ColorSpace.TYPE_CMYK:
                    compName = new String[] {"Cyan", "Magenta", "Yellow",
                                             "Black"};
                    break;
                case ColorSpace.TYPE_CMY:
                    compName = new String[] {"Cyan", "Magenta", "Yellow"};
                    break;
                default:
                    String [] tmp = new String[numComponents];
                    for (int i = 0; i < tmp.length; i++) {
                        tmp[i] = "Unnamed color component(" + i + ")";
                    }
                    compName = tmp;
            }
        }
        return compName[idx];
    }

    /**
     * 返回指定组件的最小归一化颜色组件值。此抽象类中的默认实现对所有组件返回 0.0。如果需要，子类应覆盖此方法。
     *
     * @param component 组件索引
     * @return 最小归一化组件值
     * @throws IllegalArgumentException 如果组件小于 0 或大于 numComponents - 1
     * @since 1.4
     */
    public float getMinValue(int component) {
        if ((component < 0) || (component > numComponents - 1)) {
            throw new IllegalArgumentException(
                "Component index out of range: " + component);
        }
        return 0.0f;
    }

    /**
     * 返回指定组件的最大归一化颜色组件值。此抽象类中的默认实现对所有组件返回 1.0。如果需要，子类应覆盖此方法。
     *
     * @param component 组件索引
     * @return 最大归一化组件值
     * @throws IllegalArgumentException 如果组件小于 0 或大于 numComponents - 1
     * @since 1.4
     */
    public float getMaxValue(int component) {
        if ((component < 0) || (component > numComponents - 1)) {
            throw new IllegalArgumentException(
                "Component index out of range: " + component);
        }
        return 1.0f;
    }

    /* 如果 cspace 是 XYZspace，则返回 true。
     */
    static boolean isCS_CIEXYZ(ColorSpace cspace) {
        return (cspace == XYZspace);
    }
}
