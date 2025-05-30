
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

package java.awt.image;

import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.color.*;
import sun.java2d.cmm.ColorTransform;
import sun.java2d.cmm.CMSManager;
import sun.java2d.cmm.ProfileDeferralMgr;
import sun.java2d.cmm.PCMM;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.RenderingHints;

/**
 * 此类对源图像中的数据进行逐像素颜色转换。转换后的颜色值会被缩放到目标图像的精度。颜色转换可以通过一个 ColorSpace 对象数组或一个 ICC_Profile 对象数组来指定。
 * <p>
 * 如果源图像是一个带有预乘 alpha 的 BufferedImage，颜色分量在颜色转换前会被 alpha 分量除。如果目标图像是一个带有预乘 alpha 的 BufferedImage，颜色分量在转换后会被 alpha 分量乘。Raster 被视为没有 alpha 通道，即所有带都是颜色带。
 * <p>
 * 如果在构造函数中指定了 RenderingHints 对象，颜色渲染提示和抖动提示可以用来控制颜色转换。
 * <p>
 * 注意，源和目标可以是同一个对象。
 * @see java.awt.RenderingHints#KEY_COLOR_RENDERING
 * @see java.awt.RenderingHints#KEY_DITHERING
 */
public class ColorConvertOp implements BufferedImageOp, RasterOp {
    ICC_Profile[]    profileList;
    ColorSpace[]     CSList;
    ColorTransform    thisTransform, thisRasterTransform;
    ICC_Profile      thisSrcProfile, thisDestProfile;
    RenderingHints   hints;
    boolean          gotProfiles;
    float[]          srcMinVals, srcMaxVals, dstMinVals, dstMaxVals;

    /* 类初始化器 */
    static {
        if (ProfileDeferralMgr.deferring) {
            ProfileDeferralMgr.activateProfiles();
        }
    }

    /**
     * 构造一个新的 ColorConvertOp，用于从源颜色空间转换到目标颜色空间。RenderingHints 参数可以为 null。
     * 此 Op 仅能用于 BufferedImage，并且将直接从源图像的颜色空间转换到目标图像的颜色空间。filter 方法的目标参数不能为 null。
     * @param hints 用于控制颜色转换的 <code>RenderingHints</code> 对象，或 <code>null</code>
     */
    public ColorConvertOp (RenderingHints hints)
    {
        profileList = new ICC_Profile [0];    /* 0 长度的列表 */
        this.hints  = hints;
    }

    /**
     * 从一个 ColorSpace 对象构造一个新的 ColorConvertOp。RenderingHints 参数可以为 null。此 Op 仅能用于 BufferedImage，主要用于调用带有 null 目标参数的 {@link #filter(BufferedImage, BufferedImage) filter} 方法。
     * 在这种情况下，ColorSpace 定义了由 filter 方法创建的目标的颜色空间。否则，ColorSpace 定义了一个中间空间，源将被转换到这个中间空间，然后再转换到目标空间。
     * @param cspace 定义目标 <code>ColorSpace</code> 或中间 <code>ColorSpace</code>
     * @param hints 用于控制颜色转换的 <code>RenderingHints</code> 对象，或 <code>null</code>
     * @throws NullPointerException 如果 cspace 为 null
     */
    public ColorConvertOp (ColorSpace cspace, RenderingHints hints)
    {
        if (cspace == null) {
            throw new NullPointerException("ColorSpace 不能为 null");
        }
        if (cspace instanceof ICC_ColorSpace) {
            profileList = new ICC_Profile [1];    /* 列表中有 1 个配置文件 */

            profileList [0] = ((ICC_ColorSpace) cspace).getProfile();
        }
        else {
            CSList = new ColorSpace[1]; /* 非 ICC 情况：列表中有 1 个 ColorSpace */
            CSList[0] = cspace;
        }
        this.hints  = hints;
    }


    /**
     * 从两个 ColorSpace 对象构造一个新的 ColorConvertOp。RenderingHints 参数可以为 null。
     * 此 Op 主要用于调用 Rasters 的 filter 方法，在这种情况下，两个 ColorSpaces 定义了将在 Rasters 上执行的操作。在这种情况下，源 Raster 的带数必须与 srcCspace 的组件数匹配，目标 Raster 的带数必须与 dstCspace 的组件数匹配。对于 BufferedImage，两个 ColorSpaces 定义了源将被转换到的中间空间，然后再转换到目标空间。
     * @param srcCspace 源 <code>ColorSpace</code>
     * @param dstCspace 目标 <code>ColorSpace</code>
     * @param hints 用于控制颜色转换的 <code>RenderingHints</code> 对象，或 <code>null</code>
     * @throws NullPointerException 如果 srcCspace 或 dstCspace 为 null
     */
    public ColorConvertOp(ColorSpace srcCspace, ColorSpace dstCspace,
                           RenderingHints hints)
    {
        if ((srcCspace == null) || (dstCspace == null)) {
            throw new NullPointerException("ColorSpaces 不能为 null");
        }
        if ((srcCspace instanceof ICC_ColorSpace) &&
            (dstCspace instanceof ICC_ColorSpace)) {
            profileList = new ICC_Profile [2];    /* 列表中有 2 个配置文件 */

            profileList [0] = ((ICC_ColorSpace) srcCspace).getProfile();
            profileList [1] = ((ICC_ColorSpace) dstCspace).getProfile();

            getMinMaxValsFromColorSpaces(srcCspace, dstCspace);
        } else {
            /* 非 ICC 情况：列表中有 2 个 ColorSpaces */
            CSList = new ColorSpace[2];
            CSList[0] = srcCspace;
            CSList[1] = dstCspace;
        }
        this.hints  = hints;
    }


     /**
     * 从一个 ICC_Profiles 数组构造一个新的 ColorConvertOp。RenderingHints 参数可以为 null。
     * 配置文件序列可以包括表示颜色空间的配置文件、表示效果的配置文件等。如果整个序列没有定义一个明确的颜色转换，将抛出异常。
     * <p>对于 BufferedImage，如果源 BufferedImage 的 ColorSpace 不符合数组中第一个配置文件的要求，第一个转换将转换到一个适当的 ColorSpace。如果数组中最后一个配置文件的要求不被目标 BufferedImage 的 ColorSpace 满足，最后一个转换将转换到目标的 ColorSpace。
     * <p>对于 Raster，源 Raster 的带数必须符合数组中第一个配置文件的要求，目标 Raster 的带数必须符合数组中最后一个配置文件的要求。数组必须至少有两个元素，否则调用 Rasters 的 filter 方法将抛出 IllegalArgumentException。
     * @param profiles <code>ICC_Profile</code> 对象数组
     * @param hints 用于控制颜色转换的 <code>RenderingHints</code> 对象，或 <code>null</code>
     * @exception IllegalArgumentException 当配置文件序列没有定义一个明确的颜色转换时
     * @exception NullPointerException 如果 profiles 为 null
     */
    public ColorConvertOp (ICC_Profile[] profiles, RenderingHints hints)
    {
        if (profiles == null) {
            throw new NullPointerException("Profiles 不能为 null");
        }
        gotProfiles = true;
        profileList = new ICC_Profile[profiles.length];
        for (int i1 = 0; i1 < profiles.length; i1++) {
            profileList[i1] = profiles[i1];
        }
        this.hints  = hints;
    }


    /**
     * 返回用于构造此 ColorConvertOp 的 ICC_Profiles 数组。如果 ColorConvertOp 不是从这样的数组构造的，则返回 null。
     * @return 此 <code>ColorConvertOp</code> 的 <code>ICC_Profile</code> 对象数组，或如果此 <code>ColorConvertOp</code> 不是从 <code>ICC_Profile</code> 对象数组构造的，则返回 <code>null</code>
     */
    public final ICC_Profile[] getICC_Profiles() {
        if (gotProfiles) {
            ICC_Profile[] profiles = new ICC_Profile[profileList.length];
            for (int i1 = 0; i1 < profileList.length; i1++) {
                profiles[i1] = profileList[i1];
            }
            return profiles;
        }
        return null;
    }

    /**
     * 颜色转换源 BufferedImage。如果目标图像为 null，将创建一个具有适当 ColorModel 的 BufferedImage。
     * @param src 要转换的源 <code>BufferedImage</code>
     * @param dest 目标 <code>BufferedImage</code>，或 <code>null</code>
     * @return 从 <code>src</code> 颜色转换的 <code>dest</code>，或如果 <code>dest</code> 为 <code>null</code>，则返回一个新的转换后的 <code>BufferedImage</code>
     * @exception IllegalArgumentException 如果 dest 为 null 且此 op 是使用仅接受 RenderingHints 参数的构造函数构造的，因为操作未定义。
     */
    public final BufferedImage filter(BufferedImage src, BufferedImage dest) {
        ColorSpace srcColorSpace, destColorSpace;
        BufferedImage savdest = null;

        if (src.getColorModel() instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel) src.getColorModel();
            src = icm.convertToIntDiscrete(src.getRaster(), true);
        }
        srcColorSpace = src.getColorModel().getColorSpace();
        if (dest != null) {
            if (dest.getColorModel() instanceof IndexColorModel) {
                savdest = dest;
                dest = null;
                destColorSpace = null;
            } else {
                destColorSpace = dest.getColorModel().getColorSpace();
            }
        } else {
            destColorSpace = null;
        }

        if ((CSList != null) ||
            (!(srcColorSpace instanceof ICC_ColorSpace)) ||
            ((dest != null) &&
             (!(destColorSpace instanceof ICC_ColorSpace)))) {
            /* 非 ICC 情况 */
            dest = nonICCBIFilter(src, srcColorSpace, dest, destColorSpace);
        } else {
            dest = ICCBIFilter(src, srcColorSpace, dest, destColorSpace);
        }

        if (savdest != null) {
            Graphics2D big = savdest.createGraphics();
            try {
                big.drawImage(dest, 0, 0, null);
            } finally {
                big.dispose();
            }
            return savdest;
        } else {
            return dest;
        }
    }

    private final BufferedImage ICCBIFilter(BufferedImage src,
                                            ColorSpace srcColorSpace,
                                            BufferedImage dest,
                                            ColorSpace destColorSpace) {
    int              nProfiles = profileList.length;
    ICC_Profile      srcProfile = null, destProfile = null;

        srcProfile = ((ICC_ColorSpace) srcColorSpace).getProfile();

        if (dest == null) {        /* 列表中的最后一个配置文件定义了输出颜色空间 */
            if (nProfiles == 0) {
                throw new IllegalArgumentException(
                    "目标 ColorSpace 未定义");
            }
            destProfile = profileList [nProfiles - 1];
            dest = createCompatibleDestImage(src, null);
        }
        else {
            if (src.getHeight() != dest.getHeight() ||
                src.getWidth() != dest.getWidth()) {
                throw new IllegalArgumentException(
                    "BufferedImages 的宽度或高度不匹配");
            }
            destProfile = ((ICC_ColorSpace) destColorSpace).getProfile();
        }

        /* 检查转换序列中的所有配置文件是否相同。
         * 如果是，只复制数据。
         */
        if (srcProfile == destProfile) {
            boolean noTrans = true;
            for (int i = 0; i < nProfiles; i++) {
                if (srcProfile != profileList[i]) {
                    noTrans = false;
                    break;
                }
            }
            if (noTrans) {
                Graphics2D g = dest.createGraphics();
                try {
                    g.drawImage(src, 0, 0, null);
                } finally {
                    g.dispose();
                }

                return dest;
            }
        }

        /* 如果需要，创建一个新的转换 */
        if ((thisTransform == null) || (thisSrcProfile != srcProfile) ||
            (thisDestProfile != destProfile) ) {
            updateBITransform(srcProfile, destProfile);
        }

        /* 颜色转换图像 */
        thisTransform.colorConvert(src, dest);

        return dest;
    }

    private void updateBITransform(ICC_Profile srcProfile,
                                   ICC_Profile destProfile) {
        ICC_Profile[]    theProfiles;
        int              i1, nProfiles, nTransforms, whichTrans, renderState;
        ColorTransform[]  theTransforms;
        boolean          useSrc = false, useDest = false;


                    nProfiles = profileList.length;
        nTransforms = nProfiles;
        if ((nProfiles == 0) || (srcProfile != profileList[0])) {
            nTransforms += 1;
            useSrc = true;
        }
        if ((nProfiles == 0) || (destProfile != profileList[nProfiles - 1]) ||
            (nTransforms < 2)) {
            nTransforms += 1;
            useDest = true;
        }

        /* 创建配置文件列表 */
        theProfiles = new ICC_Profile[nTransforms]; /* 该操作的配置文件列表 */

        int idx = 0;
        if (useSrc) {
            /* 将源作为第一个配置文件插入 */
            theProfiles[idx++] = srcProfile;
        }

        for (i1 = 0; i1 < nProfiles; i1++) {
                                   /* 插入此操作中定义的配置文件 */
            theProfiles[idx++] = profileList [i1];
        }

        if (useDest) {
            /* 将目标作为最后一个配置文件插入 */
            theProfiles[idx] = destProfile;
        }

        /* 创建转换列表 */
        theTransforms = new ColorTransform [nTransforms];

        /* 初始化转换获取循环 */
        if (theProfiles[0].getProfileClass() == ICC_Profile.CLASS_OUTPUT) {
                                        /* 如果第一个配置文件是打印机
                                           则以色度方式渲染 */
            renderState = ICC_Profile.icRelativeColorimetric;
        }
        else {
            renderState = ICC_Profile.icPerceptual; /* 以感知方式渲染其他类 */
        }

        whichTrans = ColorTransform.In;

        PCMM mdl = CMSManager.getModule();

        /* 从每个配置文件获取转换 */
        for (i1 = 0; i1 < nTransforms; i1++) {
            if (i1 == nTransforms -1) {         /* 最后一个配置文件？ */
                whichTrans = ColorTransform.Out; /* 获取输出转换 */
            }
            else {      /* 检查是否为抽象配置文件 */
                if ((whichTrans == ColorTransform.Simulation) &&
                    (theProfiles[i1].getProfileClass () ==
                     ICC_Profile.CLASS_ABSTRACT)) {
                renderState = ICC_Profile.icPerceptual;
                    whichTrans = ColorTransform.In;
                }
            }

            theTransforms[i1] = mdl.createTransform (
                theProfiles[i1], renderState, whichTrans);

            /* 获取此配置文件的渲染意图以选择下一个配置文件的转换 */
            renderState = getRenderingIntent(theProfiles[i1]);

            /* “中间”配置文件使用模拟转换 */
            whichTrans = ColorTransform.Simulation;
        }

        /* 创建网络转换 */
        thisTransform = mdl.createTransform(theTransforms);

        /* 更新相应的源和目标配置文件 */
        thisSrcProfile = srcProfile;
        thisDestProfile = destProfile;
    }

    /**
     * 将源光栅中的图像数据进行颜色转换。
     * 如果目标光栅为 null，则将创建一个新的光栅。
     * 源和目标光栅的波段数必须满足上述要求。创建此 ColorConvertOp 的构造函数必须提供足够的信息来定义源和目标颜色空间。否则，将抛出异常。
     * @param src 要转换的源 <code>Raster</code>
     * @param dest 目标 <code>WritableRaster</code>，或 <code>null</code>
     * @return 从 <code>src</code> 颜色转换后的 <code>dest</code>，或如果 <code>dest</code> 为 <code>null</code>，则返回一个新的转换后的 <code>WritableRaster</code>
     * @exception IllegalArgumentException 如果源或目标波段数不正确，源或目标颜色空间未定义，或此操作是使用仅适用于 BufferedImage 操作的构造函数创建的。
     */
    public final WritableRaster filter (Raster src, WritableRaster dest)  {

        if (CSList != null) {
            /* 非 ICC 情况 */
            return nonICCRasterFilter(src, dest);
        }
        int nProfiles = profileList.length;
        if (nProfiles < 2) {
            throw new IllegalArgumentException(
                "源或目标颜色空间未定义");
        }
        if (src.getNumBands() != profileList[0].getNumComponents()) {
            throw new IllegalArgumentException(
                "源光栅波段数与源颜色空间组件数不匹配");
        }
        if (dest == null) {
            dest = createCompatibleDestRaster(src);
        }
        else {
            if (src.getHeight() != dest.getHeight() ||
                src.getWidth() != dest.getWidth()) {
                throw new IllegalArgumentException(
                    "光栅的宽度或高度不匹配");
            }
            if (dest.getNumBands() !=
                profileList[nProfiles-1].getNumComponents()) {
                throw new IllegalArgumentException(
                    "目标光栅波段数与目标颜色空间组件数不匹配");
            }
        }

        /* 如果需要，创建一个新的转换 */
        if (thisRasterTransform == null) {
            int              i1, whichTrans, renderState;
            ColorTransform[]  theTransforms;

            /* 创建转换列表 */
            theTransforms = new ColorTransform [nProfiles];

            /* 初始化转换获取循环 */
            if (profileList[0].getProfileClass() == ICC_Profile.CLASS_OUTPUT) {
                                            /* 如果第一个配置文件是打印机
                                               则以色度方式渲染 */
                renderState = ICC_Profile.icRelativeColorimetric;
            }
            else {
                renderState = ICC_Profile.icPerceptual; /* 以感知方式渲染其他类 */
            }

            whichTrans = ColorTransform.In;

            PCMM mdl = CMSManager.getModule();

            /* 从每个配置文件获取转换 */
            for (i1 = 0; i1 < nProfiles; i1++) {
                if (i1 == nProfiles -1) {         /* 最后一个配置文件？ */
                    whichTrans = ColorTransform.Out; /* 获取输出转换 */
                }
                else {  /* 检查是否为抽象配置文件 */
                    if ((whichTrans == ColorTransform.Simulation) &&
                        (profileList[i1].getProfileClass () ==
                         ICC_Profile.CLASS_ABSTRACT)) {
                        renderState = ICC_Profile.icPerceptual;
                        whichTrans = ColorTransform.In;
                    }
                }

                theTransforms[i1] = mdl.createTransform (
                    profileList[i1], renderState, whichTrans);

                /* 获取此配置文件的渲染意图以选择下一个配置文件的转换 */
                renderState = getRenderingIntent(profileList[i1]);

                /* “中间”配置文件使用模拟转换 */
                whichTrans = ColorTransform.Simulation;
            }

            /* 创建网络转换 */
            thisRasterTransform = mdl.createTransform(theTransforms);
        }

        int srcTransferType = src.getTransferType();
        int dstTransferType = dest.getTransferType();
        if ((srcTransferType == DataBuffer.TYPE_FLOAT) ||
            (srcTransferType == DataBuffer.TYPE_DOUBLE) ||
            (dstTransferType == DataBuffer.TYPE_FLOAT) ||
            (dstTransferType == DataBuffer.TYPE_DOUBLE)) {
            if (srcMinVals == null) {
                getMinMaxValsFromProfiles(profileList[0],
                                          profileList[nProfiles-1]);
            }
            /* 颜色转换光栅 */
            thisRasterTransform.colorConvert(src, dest,
                                             srcMinVals, srcMaxVals,
                                             dstMinVals, dstMaxVals);
        } else {
            /* 颜色转换光栅 */
            thisRasterTransform.colorConvert(src, dest);
        }


        return dest;
    }

    /**
     * 返回给定源的 destination 的边界框。
     * 注意，这将与源的边界框相同。
     * @param src 源 <code>BufferedImage</code>
     * @return 给定 <code>src</code> 的 destination 的边界框
     */
    public final Rectangle2D getBounds2D (BufferedImage src) {
        return getBounds2D(src.getRaster());
    }

    /**
     * 返回给定源的 destination 的边界框。
     * 注意，这将与源的边界框相同。
     * @param src 源 <code>Raster</code>
     * @return 给定 <code>src</code> 的 destination 的边界框
     */
    public final Rectangle2D getBounds2D (Raster src) {
        /*        return new Rectangle (src.getXOffset(),
                              src.getYOffset(),
                              src.getWidth(), src.getHeight()); */
        return src.getBounds();
    }

    /**
     * 创建一个具有正确大小和波段数的零化目标图像，给定此源。
     * @param src       滤波操作的源图像。
     * @param destCM    目标的颜色模型。如果为 null，则将使用适当的颜色模型。
     * @return 从指定的 <code>src</code> 创建的具有正确大小和波段数的 <code>BufferedImage</code>。
     * @throws IllegalArgumentException 如果 <code>destCM</code> 为 <code>null</code> 且此 <code>ColorConvertOp</code> 未定义任何 <code>ICC_Profile</code> 或 <code>ColorSpace</code> 用于目标。
     */
    public BufferedImage createCompatibleDestImage (BufferedImage src,
                                                    ColorModel destCM) {
        ColorSpace cs = null;;
        if (destCM == null) {
            if (CSList == null) {
                /* ICC 情况 */
                int nProfiles = profileList.length;
                if (nProfiles == 0) {
                    throw new IllegalArgumentException(
                        "目标颜色空间未定义");
                }
                ICC_Profile destProfile = profileList[nProfiles - 1];
                cs = new ICC_ColorSpace(destProfile);
            } else {
                /* 非 ICC 情况 */
                int nSpaces = CSList.length;
                cs = CSList[nSpaces - 1];
            }
        }
        return createCompatibleDestImage(src, destCM, cs);
    }

    private BufferedImage createCompatibleDestImage(BufferedImage src,
                                                    ColorModel destCM,
                                                    ColorSpace destCS) {
        BufferedImage image;
        if (destCM == null) {
            ColorModel srcCM = src.getColorModel();
            int nbands = destCS.getNumComponents();
            boolean hasAlpha = srcCM.hasAlpha();
            if (hasAlpha) {
               nbands += 1;
            }
            int[] nbits = new int[nbands];
            for (int i = 0; i < nbands; i++) {
                nbits[i] = 8;
            }
            destCM = new ComponentColorModel(destCS, nbits, hasAlpha,
                                             srcCM.isAlphaPremultiplied(),
                                             srcCM.getTransparency(),
                                             DataBuffer.TYPE_BYTE);
        }
        int w = src.getWidth();
        int h = src.getHeight();
        image = new BufferedImage(destCM,
                                  destCM.createCompatibleWritableRaster(w, h),
                                  destCM.isAlphaPremultiplied(), null);
        return image;
    }


    /**
     * 创建一个具有正确大小和波段数的零化目标光栅，给定此源。
     * @param src 指定的 <code>Raster</code>
     * @return 从指定的 <code>src</code> 创建的具有正确大小和波段数的 <code>WritableRaster</code>
     * @throws IllegalArgumentException 如果此 <code>ColorConvertOp</code> 未提供足够的信息来定义 <code>dst</code> 和 <code>src</code> 颜色空间。
     */
    public WritableRaster createCompatibleDestRaster (Raster src) {
        int ncomponents;

        if (CSList != null) {
            /* 非 ICC 情况 */
            if (CSList.length != 2) {
                throw new IllegalArgumentException(
                    "目标颜色空间未定义");
            }
            ncomponents = CSList[1].getNumComponents();
        } else {
            /* ICC 情况 */
            int nProfiles = profileList.length;
            if (nProfiles < 2) {
                throw new IllegalArgumentException(
                    "目标颜色空间未定义");
            }
            ncomponents = profileList[nProfiles-1].getNumComponents();
        }

        WritableRaster dest =
            Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                                  src.getWidth(),
                                  src.getHeight(),
                                  ncomponents,
                                  new Point(src.getMinX(), src.getMinY()));
        return dest;
    }

    /**
     * 返回给定源点的目标点位置。如果 <code>dstPt</code> 不为 null，
     * 则将使用它来保存返回值。注意，对于此类，目标点将与源点相同。
     * @param srcPt 指定的源 <code>Point2D</code>
     * @param dstPt 目标 <code>Point2D</code>
     * @return 设置其位置与 <code>srcPt</code> 相同的 <code>dstPt</code>
     */
    public final Point2D getPoint2D (Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Float();
        }
        dstPt.setLocation(srcPt.getX(), srcPt.getY());

        return dstPt;
    }


    /**
     * 返回指定 ICC 配置文件的渲染意图。
     */
    private int getRenderingIntent (ICC_Profile profile) {
        byte[] header = profile.getData(ICC_Profile.icSigHead);
        int index = ICC_Profile.icHdrRenderingIntent;

        /* 根据 ICC 规范，只有最低有效 16 位用于编码渲染意图。最高有效 16 位应设置为零。因此，这里忽略了两个最高有效字节。
         *
         *  请参阅 http://www.color.org/ICC1v42_2006-05.pdf，第 7.2.15 节。
         */
        return ((header[index+2] & 0xff) <<  8) |
                (header[index+3] & 0xff);
    }


                /**
     * 返回此操作使用的渲染提示。
     * @return 此 <code>ColorConvertOp</code> 的 <code>RenderingHints</code> 对象
     */
    public final RenderingHints getRenderingHints() {
        return hints;
    }

    private final BufferedImage nonICCBIFilter(BufferedImage src,
                                               ColorSpace srcColorSpace,
                                               BufferedImage dst,
                                               ColorSpace dstColorSpace) {

        int w = src.getWidth();
        int h = src.getHeight();
        ICC_ColorSpace ciespace =
            (ICC_ColorSpace) ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);
        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
            dstColorSpace = dst.getColorModel().getColorSpace();
        } else {
            if ((h != dst.getHeight()) || (w != dst.getWidth())) {
                throw new IllegalArgumentException(
                    "BufferedImages 的宽度或高度不匹配");
            }
        }
        Raster srcRas = src.getRaster();
        WritableRaster dstRas = dst.getRaster();
        ColorModel srcCM = src.getColorModel();
        ColorModel dstCM = dst.getColorModel();
        int srcNumComp = srcCM.getNumColorComponents();
        int dstNumComp = dstCM.getNumColorComponents();
        boolean dstHasAlpha = dstCM.hasAlpha();
        boolean needSrcAlpha = srcCM.hasAlpha() && dstHasAlpha;
        ColorSpace[] list;
        if ((CSList == null) && (profileList.length != 0)) {
            /* 可能是非 ICC 源，一些配置文件，可能是非 ICC 目标 */
            boolean nonICCSrc, nonICCDst;
            ICC_Profile srcProfile, dstProfile;
            if (!(srcColorSpace instanceof ICC_ColorSpace)) {
                nonICCSrc = true;
                srcProfile = ciespace.getProfile();
            } else {
                nonICCSrc = false;
                srcProfile = ((ICC_ColorSpace) srcColorSpace).getProfile();
            }
            if (!(dstColorSpace instanceof ICC_ColorSpace)) {
                nonICCDst = true;
                dstProfile = ciespace.getProfile();
            } else {
                nonICCDst = false;
                dstProfile = ((ICC_ColorSpace) dstColorSpace).getProfile();
            }
            /* 如果需要，创建一个新的转换 */
            if ((thisTransform == null) || (thisSrcProfile != srcProfile) ||
                (thisDestProfile != dstProfile) ) {
                updateBITransform(srcProfile, dstProfile);
            }
            // 按扫描行处理
            float maxNum = 65535.0f; // 在 CMM 中使用 16 位精度
            ColorSpace cs;
            int iccSrcNumComp;
            if (nonICCSrc) {
                cs = ciespace;
                iccSrcNumComp = 3;
            } else {
                cs = srcColorSpace;
                iccSrcNumComp = srcNumComp;
            }
            float[] srcMinVal = new float[iccSrcNumComp];
            float[] srcInvDiffMinMax = new float[iccSrcNumComp];
            for (int i = 0; i < srcNumComp; i++) {
                srcMinVal[i] = cs.getMinValue(i);
                srcInvDiffMinMax[i] = maxNum / (cs.getMaxValue(i) - srcMinVal[i]);
            }
            int iccDstNumComp;
            if (nonICCDst) {
                cs = ciespace;
                iccDstNumComp = 3;
            } else {
                cs = dstColorSpace;
                iccDstNumComp = dstNumComp;
            }
            float[] dstMinVal = new float[iccDstNumComp];
            float[] dstDiffMinMax = new float[iccDstNumComp];
            for (int i = 0; i < dstNumComp; i++) {
                dstMinVal[i] = cs.getMinValue(i);
                dstDiffMinMax[i] = (cs.getMaxValue(i) - dstMinVal[i]) / maxNum;
            }
            float[] dstColor;
            if (dstHasAlpha) {
                int size = ((dstNumComp + 1) > 3) ? (dstNumComp + 1) : 3;
                dstColor = new float[size];
            } else {
                int size = (dstNumComp  > 3) ? dstNumComp : 3;
                dstColor = new float[size];
            }
            short[] srcLine = new short[w * iccSrcNumComp];
            short[] dstLine = new short[w * iccDstNumComp];
            Object pixel;
            float[] color;
            float[] alpha = null;
            if (needSrcAlpha) {
                alpha = new float[w];
            }
            int idx;
            // 处理每个扫描行
            for (int y = 0; y < h; y++) {
                // 转换源扫描行
                pixel = null;
                color = null;
                idx = 0;
                for (int x = 0; x < w; x++) {
                    pixel = srcRas.getDataElements(x, y, pixel);
                    color = srcCM.getNormalizedComponents(pixel, color, 0);
                    if (needSrcAlpha) {
                        alpha[x] = color[srcNumComp];
                    }
                    if (nonICCSrc) {
                        color = srcColorSpace.toCIEXYZ(color);
                    }
                    for (int i = 0; i < iccSrcNumComp; i++) {
                        srcLine[idx++] = (short)
                            ((color[i] - srcMinVal[i]) * srcInvDiffMinMax[i] +
                             0.5f);
                    }
                }
                // 将 srcLine 转换为 dstLine
                thisTransform.colorConvert(srcLine, dstLine);
                // 转换目标扫描行
                pixel = null;
                idx = 0;
                for (int x = 0; x < w; x++) {
                    for (int i = 0; i < iccDstNumComp; i++) {
                        dstColor[i] = ((float) (dstLine[idx++] & 0xffff)) *
                                      dstDiffMinMax[i] + dstMinVal[i];
                    }
                    if (nonICCDst) {
                        color = srcColorSpace.fromCIEXYZ(dstColor);
                        for (int i = 0; i < dstNumComp; i++) {
                            dstColor[i] = color[i];
                        }
                    }
                    if (needSrcAlpha) {
                        dstColor[dstNumComp] = alpha[x];
                    } else if (dstHasAlpha) {
                        dstColor[dstNumComp] = 1.0f;
                    }
                    pixel = dstCM.getDataElements(dstColor, 0, pixel);
                    dstRas.setDataElements(x, y, pixel);
                }
            }
        } else {
            /* 可能是非 ICC 源，可能是 CSList，可能是非 ICC 目标 */
            // 按像素处理
            int numCS;
            if (CSList == null) {
                numCS = 0;
            } else {
                numCS = CSList.length;
            }
            float[] dstColor;
            if (dstHasAlpha) {
                dstColor = new float[dstNumComp + 1];
            } else {
                dstColor = new float[dstNumComp];
            }
            Object spixel = null;
            Object dpixel = null;
            float[] color = null;
            float[] tmpColor;
            // 处理每个像素
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    spixel = srcRas.getDataElements(x, y, spixel);
                    color = srcCM.getNormalizedComponents(spixel, color, 0);
                    tmpColor = srcColorSpace.toCIEXYZ(color);
                    for (int i = 0; i < numCS; i++) {
                        tmpColor = CSList[i].fromCIEXYZ(tmpColor);
                        tmpColor = CSList[i].toCIEXYZ(tmpColor);
                    }
                    tmpColor = dstColorSpace.fromCIEXYZ(tmpColor);
                    for (int i = 0; i < dstNumComp; i++) {
                        dstColor[i] = tmpColor[i];
                    }
                    if (needSrcAlpha) {
                        dstColor[dstNumComp] = color[srcNumComp];
                    } else if (dstHasAlpha) {
                        dstColor[dstNumComp] = 1.0f;
                    }
                    dpixel = dstCM.getDataElements(dstColor, 0, dpixel);
                    dstRas.setDataElements(x, y, dpixel);

                }
            }
        }

        return dst;
    }

    /* 颜色转换 Raster - 处理 byte, ushort, int, short, float,
       或 double transferTypes */
    private final WritableRaster nonICCRasterFilter(Raster src,
                                                    WritableRaster dst)  {

        if (CSList.length != 2) {
            throw new IllegalArgumentException(
                "目标颜色空间未定义");
        }
        if (src.getNumBands() != CSList[0].getNumComponents()) {
            throw new IllegalArgumentException(
                "源 Raster 带数和源颜色空间组件数不匹配");
        }
        if (dst == null) {
            dst = createCompatibleDestRaster(src);
        } else {
            if (src.getHeight() != dst.getHeight() ||
                src.getWidth() != dst.getWidth()) {
                throw new IllegalArgumentException(
                    "Rasters 的宽度或高度不匹配");
            }
            if (dst.getNumBands() != CSList[1].getNumComponents()) {
                throw new IllegalArgumentException(
                    "目标 Raster 带数和目标颜色空间组件数不匹配");
            }
        }

        if (srcMinVals == null) {
            getMinMaxValsFromColorSpaces(CSList[0], CSList[1]);
        }

        SampleModel srcSM = src.getSampleModel();
        SampleModel dstSM = dst.getSampleModel();
        boolean srcIsFloat, dstIsFloat;
        int srcTransferType = src.getTransferType();
        int dstTransferType = dst.getTransferType();
        if ((srcTransferType == DataBuffer.TYPE_FLOAT) ||
            (srcTransferType == DataBuffer.TYPE_DOUBLE)) {
            srcIsFloat = true;
        } else {
            srcIsFloat = false;
        }
        if ((dstTransferType == DataBuffer.TYPE_FLOAT) ||
            (dstTransferType == DataBuffer.TYPE_DOUBLE)) {
            dstIsFloat = true;
        } else {
            dstIsFloat = false;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        int srcNumBands = src.getNumBands();
        int dstNumBands = dst.getNumBands();
        float[] srcScaleFactor = null;
        float[] dstScaleFactor = null;
        if (!srcIsFloat) {
            srcScaleFactor = new float[srcNumBands];
            for (int i = 0; i < srcNumBands; i++) {
                if (srcTransferType == DataBuffer.TYPE_SHORT) {
                    srcScaleFactor[i] = (srcMaxVals[i] - srcMinVals[i]) /
                                        32767.0f;
                } else {
                    srcScaleFactor[i] = (srcMaxVals[i] - srcMinVals[i]) /
                        ((float) ((1 << srcSM.getSampleSize(i)) - 1));
                }
            }
        }
        if (!dstIsFloat) {
            dstScaleFactor = new float[dstNumBands];
            for (int i = 0; i < dstNumBands; i++) {
                if (dstTransferType == DataBuffer.TYPE_SHORT) {
                    dstScaleFactor[i] = 32767.0f /
                                        (dstMaxVals[i] - dstMinVals[i]);
                } else {
                    dstScaleFactor[i] =
                        ((float) ((1 << dstSM.getSampleSize(i)) - 1)) /
                        (dstMaxVals[i] - dstMinVals[i]);
                }
            }
        }
        int ys = src.getMinY();
        int yd = dst.getMinY();
        int xs, xd;
        float sample;
        float[] color = new float[srcNumBands];
        float[] tmpColor;
        ColorSpace srcColorSpace = CSList[0];
        ColorSpace dstColorSpace = CSList[1];
        // 处理每个像素
        for (int y = 0; y < h; y++, ys++, yd++) {
            // 获取源扫描行
            xs = src.getMinX();
            xd = dst.getMinX();
            for (int x = 0; x < w; x++, xs++, xd++) {
                for (int i = 0; i < srcNumBands; i++) {
                    sample = src.getSampleFloat(xs, ys, i);
                    if (!srcIsFloat) {
                        sample = sample * srcScaleFactor[i] + srcMinVals[i];
                    }
                    color[i] = sample;
                }
                tmpColor = srcColorSpace.toCIEXYZ(color);
                tmpColor = dstColorSpace.fromCIEXYZ(tmpColor);
                for (int i = 0; i < dstNumBands; i++) {
                    sample = tmpColor[i];
                    if (!dstIsFloat) {
                        sample = (sample - dstMinVals[i]) * dstScaleFactor[i];
                    }
                    dst.setSample(xd, yd, i, sample);
                }
            }
        }
        return dst;
    }

    private void getMinMaxValsFromProfiles(ICC_Profile srcProfile,
                                           ICC_Profile dstProfile) {
        int type = srcProfile.getColorSpaceType();
        int nc = srcProfile.getNumComponents();
        srcMinVals = new float[nc];
        srcMaxVals = new float[nc];
        setMinMax(type, nc, srcMinVals, srcMaxVals);
        type = dstProfile.getColorSpaceType();
        nc = dstProfile.getNumComponents();
        dstMinVals = new float[nc];
        dstMaxVals = new float[nc];
        setMinMax(type, nc, dstMinVals, dstMaxVals);
    }

    private void setMinMax(int type, int nc, float[] minVals, float[] maxVals) {
        if (type == ColorSpace.TYPE_Lab) {
            minVals[0] = 0.0f;    // L
            maxVals[0] = 100.0f;
            minVals[1] = -128.0f; // a
            maxVals[1] = 127.0f;
            minVals[2] = -128.0f; // b
            maxVals[2] = 127.0f;
        } else if (type == ColorSpace.TYPE_XYZ) {
            minVals[0] = minVals[1] = minVals[2] = 0.0f; // X, Y, Z
            maxVals[0] = maxVals[1] = maxVals[2] = 1.0f + (32767.0f/ 32768.0f);
        } else {
            for (int i = 0; i < nc; i++) {
                minVals[i] = 0.0f;
                maxVals[i] = 1.0f;
            }
        }
    }

    private void getMinMaxValsFromColorSpaces(ColorSpace srcCspace,
                                              ColorSpace dstCspace) {
        int nc = srcCspace.getNumComponents();
        srcMinVals = new float[nc];
        srcMaxVals = new float[nc];
        for (int i = 0; i < nc; i++) {
            srcMinVals[i] = srcCspace.getMinValue(i);
            srcMaxVals[i] = srcCspace.getMaxValue(i);
        }
        nc = dstCspace.getNumComponents();
        dstMinVals = new float[nc];
        dstMaxVals = new float[nc];
        for (int i = 0; i < nc; i++) {
            dstMinVals[i] = dstCspace.getMinValue(i);
            dstMaxVals[i] = dstCspace.getMaxValue(i);
        }
    }

}
