
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

import sun.java2d.cmm.ColorTransform;
import sun.java2d.cmm.CMSManager;
import sun.java2d.cmm.PCMM;


/**
 *
 * ICC_ColorSpace 类是抽象类 ColorSpace 的一个实现。此表示法基于
 * 国际色彩联盟规范 ICC.1:2001-12，色彩配置文件文件格式（参见 <A href="http://www.color.org">http://www.color.org</A>）。
 * <p>
 * 通常，一个 Color 或 ColorModel 会与一个 ICC 配置文件相关联，该配置文件可以是输入、显示或输出配置文件（参见
 * ICC 规范）。还有其他类型的 ICC 配置文件，例如抽象配置文件、设备链接配置文件和命名颜色配置文件，
 * 这些配置文件不包含适合表示颜色、图像或设备颜色空间的信息（参见 ICC_Profile）。
 * 尝试从不合适的 ICC 配置文件创建 ICC_ColorSpace 对象是错误的。
 * <p>
 * ICC 配置文件表示从配置文件的颜色空间（例如显示器）到配置文件连接空间（PCS）的转换。
 * 对于标记图像或颜色感兴趣的配置文件，其 PCS 是定义在 ICC 配置文件格式规范中的设备独立空间之一
 * （一个 CIEXYZ 空间和两个 CIELab 空间）。大多数感兴趣的配置文件要么具有可逆转换，要么明确指定了
 * 双向转换。如果以需要从 PCS 转换到配置文件的原生空间的方式使用 ICC_ColorSpace 对象，并且没有足够的数据
 * 来正确执行转换，ICC_ColorSpace 对象将生成指定类型的颜色空间（例如 TYPE_RGB、TYPE_CMYK 等）的输出，
 * 但输出数据的具体颜色值将是未定义的。
 * <p>
 * 对于简单的小程序，这些类的细节并不重要，这些小程序在默认颜色空间中绘制或操作和显示具有已知颜色空间的导入图像。
 * 至多，这样的小程序需要通过 ColorSpace.getInstance() 获取其中一个默认颜色空间。
 * @see ColorSpace
 * @see ICC_Profile
 */



public class ICC_ColorSpace extends ColorSpace {

    static final long serialVersionUID = 3455889114070431483L;

    private ICC_Profile    thisProfile;
    private float[] minVal;
    private float[] maxVal;
    private float[] diffMinMax;
    private float[] invDiffMinMax;
    private boolean needScaleInit = true;

    // {to,from}{RGB,CIEXYZ} 方法在需要时创建并缓存这些对象
    private transient ColorTransform this2srgb;
    private transient ColorTransform srgb2this;
    private transient ColorTransform this2xyz;
    private transient ColorTransform xyz2this;


    /**
    * 从一个 ICC_Profile 对象构造一个新的 ICC_ColorSpace。
    * @param profile 指定的 ICC_Profile 对象
    * @exception IllegalArgumentException 如果配置文件不适合表示颜色空间。
    */
    public ICC_ColorSpace (ICC_Profile profile) {
        super (profile.getColorSpaceType(), profile.getNumComponents());

        int profileClass = profile.getProfileClass();

        /* REMIND - is NAMEDCOLOR OK? */
        if ((profileClass != ICC_Profile.CLASS_INPUT) &&
            (profileClass != ICC_Profile.CLASS_DISPLAY) &&
            (profileClass != ICC_Profile.CLASS_OUTPUT) &&
            (profileClass != ICC_Profile.CLASS_COLORSPACECONVERSION) &&
            (profileClass != ICC_Profile.CLASS_NAMEDCOLOR) &&
            (profileClass != ICC_Profile.CLASS_ABSTRACT)) {
            throw new IllegalArgumentException("Invalid profile type");
        }

        thisProfile = profile;
        setMinMax();
    }

    /**
     * 验证从对象输入流读取的 ICC_ColorSpace
     */
    private void readObject(java.io.ObjectInputStream s)
        throws ClassNotFoundException, java.io.IOException {

        s.defaultReadObject();
        if (thisProfile == null) {
            thisProfile = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
        }
    }

    /**
    * 返回此 ICC_ColorSpace 的 ICC_Profile。
    * @return 此 ICC_ColorSpace 的 ICC_Profile。
    */
    public ICC_Profile getProfile() {
        return thisProfile;
    }

    /**
     * 将假定在此 ColorSpace 中的颜色值转换为默认的 CS_sRGB 颜色空间。
     * <p>
     * 此方法使用旨在产生输入和输出颜色之间最佳感知匹配的算法转换颜色值。为了进行颜色值的颜色测量转换，
     * 您应使用此颜色空间的 <code>toCIEXYZ</code>
     * 方法首先将输入颜色空间转换为 CS_CIEXYZ 颜色空间，然后使用 CS_sRGB 颜色空间的
     * <code>fromCIEXYZ</code> 方法将 CS_CIEXYZ 转换为输出颜色空间。
     * 有关更多信息，请参见 {@link #toCIEXYZ(float[]) toCIEXYZ} 和
     * {@link #fromCIEXYZ(float[]) fromCIEXYZ}。
     * <p>
     * @param colorvalue 一个长度至少为此 ColorSpace 组件数的 float 数组。
     * @return 一个长度为 3 的 float 数组。
     * @throws ArrayIndexOutOfBoundsException 如果数组长度不是至少为
     * 此 ColorSpace 的组件数。
     */
    public float[]    toRGB (float[] colorvalue) {

        if (this2srgb == null) {
            ColorTransform[] transformList = new ColorTransform [2];
            ICC_ColorSpace srgbCS =
                (ICC_ColorSpace) ColorSpace.getInstance (CS_sRGB);
            PCMM mdl = CMSManager.getModule();
            transformList[0] = mdl.createTransform(
                thisProfile, ColorTransform.Any, ColorTransform.In);
            transformList[1] = mdl.createTransform(
                srgbCS.getProfile(), ColorTransform.Any, ColorTransform.Out);
            this2srgb = mdl.createTransform(transformList);
            if (needScaleInit) {
                setComponentScaling();
            }
        }

        int nc = this.getNumComponents();
        short tmp[] = new short[nc];
        for (int i = 0; i < nc; i++) {
            tmp[i] = (short)
                ((colorvalue[i] - minVal[i]) * invDiffMinMax[i] + 0.5f);
        }
        tmp = this2srgb.colorConvert(tmp, null);
        float[] result = new float [3];
        for (int i = 0; i < 3; i++) {
            result[i] = ((float) (tmp[i] & 0xffff)) / 65535.0f;
        }
        return result;
    }

    /**
     * 将假定在默认 CS_sRGB 颜色空间中的颜色值转换为此 ColorSpace。
     * <p>
     * 此方法使用旨在产生输入和输出颜色之间最佳感知匹配的算法转换颜色值。为了进行颜色值的颜色测量转换，
     * 您应使用 CS_sRGB 颜色空间的 <code>toCIEXYZ</code>
     * 方法首先将输入颜色空间转换为 CS_CIEXYZ 颜色空间，然后使用此颜色空间的
     * <code>fromCIEXYZ</code> 方法将 CS_CIEXYZ 转换为输出颜色空间。
     * 有关更多信息，请参见 {@link #toCIEXYZ(float[]) toCIEXYZ} 和
     * {@link #fromCIEXYZ(float[]) fromCIEXYZ}。
     * <p>
     * @param rgbvalue 一个长度至少为 3 的 float 数组。
     * @return 一个长度等于此 ColorSpace 组件数的 float 数组。
     * @throws ArrayIndexOutOfBoundsException 如果数组长度不是至少 3。
     */
    public float[]    fromRGB(float[] rgbvalue) {

        if (srgb2this == null) {
            ColorTransform[] transformList = new ColorTransform [2];
            ICC_ColorSpace srgbCS =
                (ICC_ColorSpace) ColorSpace.getInstance (CS_sRGB);
            PCMM mdl = CMSManager.getModule();
            transformList[0] = mdl.createTransform(
                srgbCS.getProfile(), ColorTransform.Any, ColorTransform.In);
            transformList[1] = mdl.createTransform(
                thisProfile, ColorTransform.Any, ColorTransform.Out);
            srgb2this = mdl.createTransform(transformList);
            if (needScaleInit) {
                setComponentScaling();
            }
        }

        short tmp[] = new short[3];
        for (int i = 0; i < 3; i++) {
            tmp[i] = (short) ((rgbvalue[i] * 65535.0f) + 0.5f);
        }
        tmp = srgb2this.colorConvert(tmp, null);
        int nc = this.getNumComponents();
        float[] result = new float [nc];
        for (int i = 0; i < nc; i++) {
            result[i] = (((float) (tmp[i] & 0xffff)) / 65535.0f) *
                        diffMinMax[i] + minVal[i];
        }
        return result;
    }


    /**
     * 将假定在此 ColorSpace 中的颜色值转换为 CS_CIEXYZ 转换颜色空间。
     * <p>
     * 此方法使用 ICC 规范定义的相对色度法转换颜色值。这意味着此方法返回的 XYZ 值表示为
     * CS_CIEXYZ 颜色空间的 D50 白点的相对值。这种表示在两步颜色转换过程中很有用，
     * 在此过程中，颜色从输入颜色空间转换为 CS_CIEXYZ，然后再转换为输出颜色空间。这种表示
     * 与使用当前 CIE 推荐方法测量给定颜色值的 XYZ 值不同。需要进一步转换以计算使用当前 CIE 推荐方法
     * 测量的 XYZ 值。以下段落对此进行了更详细的解释。
     * <p>
     * ICC 标准使用设备独立颜色空间（DICS）作为将颜色从一个设备转换为另一个设备的机制。在此架构中，
     * 颜色从源设备的颜色空间转换为 ICC DICS，然后再从 ICC DICS 转换为目标设备的颜色空间。ICC 标准定义了
     * 设备配置文件，其中包含将设备的颜色空间转换为 ICC DICS 的转换。从源设备的颜色转换为目标设备的颜色
     * 是通过将源设备配置文件的设备到 DICS 转换连接到目标设备配置文件的 DICS 到设备转换来完成的。
     * 因此，ICC DICS 通常被称为配置文件连接空间（PCS）。toCIEXYZ 和 fromCIEXYZ 方法中使用的颜色空间
     * 是 ICC 规范定义的 CIEXYZ PCS。这也就是 ColorSpace.CS_CIEXYZ 表示的颜色空间。
     * <p>
     * 颜色的 XYZ 值通常表示为相对于某个白点，因此在不知道这些值的白点的情况下，XYZ 值的实际含义是未知的。
     * 这被称为相对色度法。PCS 使用 D50 作为白点，因此 PCS 的 XYZ 值是相对于 D50 的。例如，PCS 中的白色
     * 将具有 D50 的 XYZ 值，定义为 X=.9642，Y=1.000，Z=0.8249。这个白点通常用于图形艺术应用，但在其他应用中
     * 也经常使用其他白点。
     * <p>
     * 为了量化打印机或显示器等设备的颜色特性，通常会对特定设备颜色的 XYZ 值进行测量。为了讨论的目的，
     * 设备 XYZ 值是指使用当前 CIE 推荐方法测量的设备颜色的 XYZ 值。
     * <p>
     * 在设备 XYZ 值和此方法返回的 PCS XYZ 值之间进行转换对应于在设备的颜色空间（由 CIE 色度值表示）和 PCS 之间
     * 进行转换。这个过程中涉及许多因素，其中一些因素相当微妙。然而，最重要的是为了补偿设备白点和 PCS 白点之间的差异
     * 而进行的调整。有许多方法可以做到这一点，这是当前研究和争议的主题。一些常用的方法包括 XYZ 缩放、von Kries 转换
     * 和 Bradford 转换。使用哪种方法取决于每个特定的应用。
     * <p>
     * 最简单的方法是 XYZ 缩放。在此方法中，每个设备 XYZ 值通过乘以 PCS 白点（D50）与设备白点的比值转换为 PCS XYZ 值。
     * <pre>
     *
     * Xd, Yd, Zd 是设备 XYZ 值
     * Xdw, Ydw, Zdw 是设备 XYZ 白点值
     * Xp, Yp, Zp 是 PCS XYZ 值
     * Xd50, Yd50, Zd50 是 PCS XYZ 白点值
     *
     * Xp = Xd * (Xd50 / Xdw)
     * Yp = Yd * (Yd50 / Ydw)
     * Zp = Zd * (Zd50 / Zdw)
     *
     * </pre>
     * <p>
     * 从 PCS 转换到设备将通过反转这些方程来完成：
     * <pre>
     *
     * Xd = Xp * (Xdw / Xd50)
     * Yd = Yp * (Ydw / Yd50)
     * Zd = Zp * (Zdw / Zd50)
     *
     * </pre>
     * <p>
     * 请注意，ICC 配置文件中的媒体白点标签与设备白点不同。媒体白点标签以 PCS 值表示，用于表示设备照明下的
     * 设备介质的 XYZ 值之间的差异。设备白点表示为设备上显示的白色对应的设备 XYZ 值。例如，在 sRGB 设备上显示
     * RGB 颜色 (1.0, 1.0, 1.0) 将导致测量的设备 XYZ 值为 D65。这将与 sRGB 设备 ICC 配置文件中的媒体白点标签 XYZ 值不同。
     * <p>
     * @param colorvalue 一个长度至少为该 ColorSpace 组件数的 float 数组。
     * @return 一个长度为 3 的 float 数组。
     * @throws ArrayIndexOutOfBoundsException 如果数组长度不是至少为
     * 该 ColorSpace 的组件数。
     */
    public float[]    toCIEXYZ(float[] colorvalue) {


                    if (this2xyz == null) {
            ColorTransform[] transformList = new ColorTransform [2];
            ICC_ColorSpace xyzCS =
                (ICC_ColorSpace) ColorSpace.getInstance (CS_CIEXYZ);
            PCMM mdl = CMSManager.getModule();
            try {
                transformList[0] = mdl.createTransform(
                    thisProfile, ICC_Profile.icRelativeColorimetric,
                    ColorTransform.In);
            } catch (CMMException e) {
                transformList[0] = mdl.createTransform(
                    thisProfile, ColorTransform.Any, ColorTransform.In);
            }
            transformList[1] = mdl.createTransform(
                xyzCS.getProfile(), ColorTransform.Any, ColorTransform.Out);
            this2xyz = mdl.createTransform (transformList);
            if (needScaleInit) {
                setComponentScaling();
            }
        }

        int nc = this.getNumComponents();
        short tmp[] = new short[nc];
        for (int i = 0; i < nc; i++) {
            tmp[i] = (short)
                ((colorvalue[i] - minVal[i]) * invDiffMinMax[i] + 0.5f);
        }
        tmp = this2xyz.colorConvert(tmp, null);
        float ALMOST_TWO = 1.0f + (32767.0f / 32768.0f);
        // 对于 CIEXYZ，所有组件的最小值为 0.0，最大值为 ALMOST_TWO
        float[] result = new float [3];
        for (int i = 0; i < 3; i++) {
            result[i] = (((float) (tmp[i] & 0xffff)) / 65535.0f) * ALMOST_TWO;
        }
        return result;
    }


    /**
     * 将假设为 CS_CIEXYZ 转换颜色空间中的颜色值转换为该 ColorSpace。
     * <p>
     * 此方法使用相对色度法进行颜色值转换，这是 ICC 规范中定义的。这意味着传递给此方法的 XYZ 参数值表示为 CS_CIEXYZ 颜色空间的 D50 白点的相对值。
     * 这种表示在两步颜色转换过程中非常有用，其中颜色从输入颜色空间转换为 CS_CIEXYZ，然后再转换为输出颜色空间。此方法返回的颜色值并不是使用色度计测量时会产生传递给方法的 XYZ 值的颜色值。
     * 如果您有使用当前 CIE 推荐做法测量的 XYZ 值，它们必须转换为 D50 相对值，然后才能传递给此方法。以下段落对此进行了更详细的解释。
     * <p>
     * ICC 标准使用设备独立颜色空间 (DICS) 作为将颜色从一个设备转换到另一个设备的机制。在此架构中，颜色从源设备的颜色空间转换为 ICC DICS，然后再从 ICC DICS 转换为目标设备的颜色空间。
     * ICC 标准定义了设备配置文件，其中包含将设备的颜色空间转换为 ICC DICS 的转换。从源设备到目标设备的颜色的整体转换是通过将源设备配置文件的设备到 DICS 转换连接到目标设备配置文件的 DICS 到设备转换来完成的。
     * 因此，ICC DICS 通常被称为配置文件连接空间 (PCS)。toCIEXYZ 和 fromCIEXYZ 方法中使用的是 ICC 规范定义的 CIEXYZ PCS。这也是 ColorSpace.CS_CIEXYZ 表示的颜色空间。
     * <p>
     * 颜色的 XYZ 值通常表示为相对于某个白点，因此在不知道这些值的白点的情况下，无法知道 XYZ 值的实际含义。这被称为相对色度法。PCS 使用 D50 作为白点，因此 PCS 的 XYZ 值是相对于 D50 的。
     * 例如，PCS 中的白色将具有 D50 的 XYZ 值，定义为 X=.9642，Y=1.000，Z=0.8249。这个白点通常用于图形艺术应用，但在其他应用中经常使用其他白点。
     * <p>
     * 为了量化打印机或显示器等设备的颜色特性，通常会对特定设备颜色进行 XYZ 值的测量。为了讨论的目的，设备 XYZ 值是指使用当前 CIE 推荐做法从设备颜色测量得到的 XYZ 值。
     * <p>
     * 在设备 XYZ 值和作为此方法参数的 PCS XYZ 值之间进行转换，相当于在设备的颜色空间（由 CIE 色度值表示）和 PCS 之间进行转换。这个过程中涉及许多因素，其中一些非常微妙。
     * 然而，最重要的是调整设备白点和 PCS 白点之间的差异。有许多技术可以做到这一点，这是当前研究和争议的主题。一些常用的方法包括 XYZ 缩放、von Kries 转换和 Bradford 转换。使用哪种方法取决于每个特定的应用。
     * <p>
     * 最简单的方法是 XYZ 缩放。在此方法中，每个设备 XYZ 值通过乘以 PCS 白点（D50）与设备白点的比值来转换为 PCS XYZ 值。
     * <pre>
     *
     * Xd, Yd, Zd 是设备 XYZ 值
     * Xdw, Ydw, Zdw 是设备 XYZ 白点值
     * Xp, Yp, Zp 是 PCS XYZ 值
     * Xd50, Yd50, Zd50 是 PCS XYZ 白点值
     *
     * Xp = Xd * (Xd50 / Xdw)
     * Yp = Yd * (Yd50 / Ydw)
     * Zp = Zd * (Zd50 / Zdw)
     *
     * </pre>
     * <p>
     * 从 PCS 到设备的转换可以通过反转这些方程来完成：
     * <pre>
     *
     * Xd = Xp * (Xdw / Xd50)
     * Yd = Yp * (Ydw / Yd50)
     * Zd = Zp * (Zdw / Zd50)
     *
     * </pre>
     * <p>
     * 请注意，ICC 配置文件中的媒体白点标签与设备白点不同。媒体白点标签以 PCS 值表示，用于表示设备照明下的设备介质的 XYZ 值之间的差异。设备白点表示为设备上显示的白色对应的设备 XYZ 值。
     * 例如，在 sRGB 设备上显示 RGB 颜色 (1.0, 1.0, 1.0) 将导致测量的设备 XYZ 值为 D65。这与 sRGB 设备 ICC 配置文件中的媒体白点标签 XYZ 值不同。
     * <p>
     * @param colorvalue 长度至少为 3 的浮点数组。
     * @return 长度等于此 ColorSpace 组件数的浮点数组。
     * @throws ArrayIndexOutOfBoundsException 如果数组长度不是至少 3。
     */
    public float[]    fromCIEXYZ(float[] colorvalue) {

        if (xyz2this == null) {
            ColorTransform[] transformList = new ColorTransform [2];
            ICC_ColorSpace xyzCS =
                (ICC_ColorSpace) ColorSpace.getInstance (CS_CIEXYZ);
            PCMM mdl = CMSManager.getModule();
            transformList[0] = mdl.createTransform (
                xyzCS.getProfile(), ColorTransform.Any, ColorTransform.In);
            try {
                transformList[1] = mdl.createTransform(
                    thisProfile, ICC_Profile.icRelativeColorimetric,
                    ColorTransform.Out);
            } catch (CMMException e) {
                transformList[1] = CMSManager.getModule().createTransform(
                thisProfile, ColorTransform.Any, ColorTransform.Out);
            }
            xyz2this = mdl.createTransform(transformList);
            if (needScaleInit) {
                setComponentScaling();
            }
        }

        short tmp[] = new short[3];
        float ALMOST_TWO = 1.0f + (32767.0f / 32768.0f);
        float factor = 65535.0f / ALMOST_TWO;
        // 对于 CIEXYZ，所有组件的最小值为 0.0，最大值为 ALMOST_TWO
        for (int i = 0; i < 3; i++) {
            tmp[i] = (short) ((colorvalue[i] * factor) + 0.5f);
        }
        tmp = xyz2this.colorConvert(tmp, null);
        int nc = this.getNumComponents();
        float[] result = new float [nc];
        for (int i = 0; i < nc; i++) {
            result[i] = (((float) (tmp[i] & 0xffff)) / 65535.0f) *
                        diffMinMax[i] + minVal[i];
        }
        return result;
    }

    /**
     * 返回指定组件的最小归一化颜色组件值。对于 TYPE_XYZ 空间，此方法返回所有组件的最小值 0.0。对于 TYPE_Lab 空间，
     * 此方法返回 L 的 0.0 和 a 和 b 组件的 -128.0。这与 ICC 规范中 XYZ 和 Lab 配置文件连接空间的编码一致。对于所有其他类型，此方法返回所有组件的 0.0。
     * 当使用需要不同最小组件值的 ICC_ColorSpace 配置文件时，需要子类化此类并覆盖此方法。
     * @param component 组件索引。
     * @return 最小归一化组件值。
     * @throws IllegalArgumentException 如果 component 小于 0 或大于 numComponents - 1。
     * @since 1.4
     */
    public float getMinValue(int component) {
        if ((component < 0) || (component > this.getNumComponents() - 1)) {
            throw new IllegalArgumentException(
                "Component index out of range: " + component);
        }
        return minVal[component];
    }

    /**
     * 返回指定组件的最大归一化颜色组件值。对于 TYPE_XYZ 空间，此方法返回所有组件的最大值 1.0 + (32767.0 / 32768.0)。对于 TYPE_Lab 空间，
     * 此方法返回 L 的 100.0 和 a 和 b 组件的 127.0。这与 ICC 规范中 XYZ 和 Lab 配置文件连接空间的编码一致。对于所有其他类型，此方法返回所有组件的 1.0。
     * 当使用需要不同最大组件值的 ICC_ColorSpace 配置文件时，需要子类化此类并覆盖此方法。
     * @param component 组件索引。
     * @return 最大归一化组件值。
     * @throws IllegalArgumentException 如果 component 小于 0 或大于 numComponents - 1。
     * @since 1.4
     */
    public float getMaxValue(int component) {
        if ((component < 0) || (component > this.getNumComponents() - 1)) {
            throw new IllegalArgumentException(
                "Component index out of range: " + component);
        }
        return maxVal[component];
    }

    private void setMinMax() {
        int nc = this.getNumComponents();
        int type = this.getType();
        minVal = new float[nc];
        maxVal = new float[nc];
        if (type == ColorSpace.TYPE_Lab) {
            minVal[0] = 0.0f;    // L
            maxVal[0] = 100.0f;
            minVal[1] = -128.0f; // a
            maxVal[1] = 127.0f;
            minVal[2] = -128.0f; // b
            maxVal[2] = 127.0f;
        } else if (type == ColorSpace.TYPE_XYZ) {
            minVal[0] = minVal[1] = minVal[2] = 0.0f; // X, Y, Z
            maxVal[0] = maxVal[1] = maxVal[2] = 1.0f + (32767.0f/ 32768.0f);
        } else {
            for (int i = 0; i < nc; i++) {
                minVal[i] = 0.0f;
                maxVal[i] = 1.0f;
            }
        }
    }

    private void setComponentScaling() {
        int nc = this.getNumComponents();
        diffMinMax = new float[nc];
        invDiffMinMax = new float[nc];
        for (int i = 0; i < nc; i++) {
            minVal[i] = this.getMinValue(i); // 以防 getMinVal 被覆盖
            maxVal[i] = this.getMaxValue(i); // 以防 getMaxVal 被覆盖
            diffMinMax[i] = maxVal[i] - minVal[i];
            invDiffMinMax[i] = 65535.0f / diffMinMax[i];
        }
        needScaleInit = false;
    }

}
