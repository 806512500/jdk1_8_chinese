
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

import sun.java2d.cmm.PCMM;
import sun.java2d.cmm.CMSManager;
import sun.java2d.cmm.Profile;
import sun.java2d.cmm.ProfileDataVerifier;
import sun.java2d.cmm.ProfileDeferralMgr;
import sun.java2d.cmm.ProfileDeferralInfo;
import sun.java2d.cmm.ProfileActivator;
import sun.misc.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;

import java.util.StringTokenizer;

import java.security.AccessController;
import java.security.PrivilegedAction;


/**
 * 设备独立和设备相关颜色空间的ICC配置文件数据表示，基于国际颜色
 * 联盟规范ICC.1:2001-12，颜色配置文件文件格式（参见
 * <A href="http://www.color.org"> http://www.color.org</A>）。
 * <p>
 * 可以从适当的ICC_Profile构造一个ICC_ColorSpace对象。
 * 通常，一个ICC_ColorSpace会与一个ICC配置文件相关联，该配置文件可以是输入、显示或输出配置文件（参见
 * ICC规范）。还有设备链接、抽象、颜色空间转换和命名颜色配置文件。这些配置文件对于标记颜色或图像不太有用，
 * 但在其他用途中非常有用（特别是设备链接配置文件可以提供从一个设备的颜色空间转换到另一个设备的颜色空间的改进性能）。
 * <p>
 * ICC配置文件表示从配置文件的颜色空间（例如显示器）到配置文件连接空间（PCS）的转换。
 * 用于标记图像或颜色的配置文件通常具有两个特定的设备独立空间之一（一个CIEXYZ空间和一个CIELab空间）作为PCS，
 * 这些空间在ICC配置文件格式规范中定义。大多数感兴趣的配置文件要么具有可逆的转换，要么明确指定了两个方向的转换。
 * @see ICC_ColorSpace
 */


public class ICC_Profile implements Serializable {

    private static final long serialVersionUID = -3938515861990936766L;

    private transient Profile cmmProfile;

    private transient ProfileDeferralInfo deferralInfo;
    private transient ProfileActivator profileActivator;

    // 在ColorSpace类中定义的特定颜色空间的单例配置文件对象注册表（例如CS_sRGB），参见
    // getInstance(int cspace)工厂方法。
    private static ICC_Profile sRGBprofile;
    private static ICC_Profile XYZprofile;
    private static ICC_Profile PYCCprofile;
    private static ICC_Profile GRAYprofile;
    private static ICC_Profile LINEAR_RGBprofile;


    /**
     * 配置文件类是输入。
     */
    public static final int CLASS_INPUT = 0;

    /**
     * 配置文件类是显示。
     */
    public static final int CLASS_DISPLAY = 1;

    /**
     * 配置文件类是输出。
     */
    public static final int CLASS_OUTPUT = 2;

    /**
     * 配置文件类是设备链接。
     */
    public static final int CLASS_DEVICELINK = 3;

    /**
     * 配置文件类是颜色空间转换。
     */
    public static final int CLASS_COLORSPACECONVERSION = 4;

    /**
     * 配置文件类是抽象。
     */
    public static final int CLASS_ABSTRACT = 5;

    /**
     * 配置文件类是命名颜色。
     */
    public static final int CLASS_NAMEDCOLOR = 6;


    /**
     * ICC配置文件颜色空间类型签名：'XYZ '。
     */
    public static final int icSigXYZData        = 0x58595A20;    /* 'XYZ ' */

    /**
     * ICC配置文件颜色空间类型签名：'Lab '。
     */
    public static final int icSigLabData        = 0x4C616220;    /* 'Lab ' */

    /**
     * ICC配置文件颜色空间类型签名：'Luv '。
     */
    public static final int icSigLuvData        = 0x4C757620;    /* 'Luv ' */

    /**
     * ICC配置文件颜色空间类型签名：'YCbr'。
     */
    public static final int icSigYCbCrData        = 0x59436272;    /* 'YCbr' */

    /**
     * ICC配置文件颜色空间类型签名：'Yxy '。
     */
    public static final int icSigYxyData        = 0x59787920;    /* 'Yxy ' */

    /**
     * ICC配置文件颜色空间类型签名：'RGB '。
     */
    public static final int icSigRgbData        = 0x52474220;    /* 'RGB ' */

    /**
     * ICC配置文件颜色空间类型签名：'GRAY'。
     */
    public static final int icSigGrayData        = 0x47524159;    /* 'GRAY' */

    /**
     * ICC配置文件颜色空间类型签名：'HSV'。
     */
    public static final int icSigHsvData        = 0x48535620;    /* 'HSV ' */

    /**
     * ICC配置文件颜色空间类型签名：'HLS'。
     */
    public static final int icSigHlsData        = 0x484C5320;    /* 'HLS ' */

    /**
     * ICC配置文件颜色空间类型签名：'CMYK'。
     */
    public static final int icSigCmykData        = 0x434D594B;    /* 'CMYK' */

    /**
     * ICC配置文件颜色空间类型签名：'CMY '。
     */
    public static final int icSigCmyData        = 0x434D5920;    /* 'CMY ' */

    /**
     * ICC配置文件颜色空间类型签名：'2CLR'。
     */
    public static final int icSigSpace2CLR        = 0x32434C52;    /* '2CLR' */

    /**
     * ICC配置文件颜色空间类型签名：'3CLR'。
     */
    public static final int icSigSpace3CLR        = 0x33434C52;    /* '3CLR' */

    /**
     * ICC配置文件颜色空间类型签名：'4CLR'。
     */
    public static final int icSigSpace4CLR        = 0x34434C52;    /* '4CLR' */

    /**
     * ICC配置文件颜色空间类型签名：'5CLR'。
     */
    public static final int icSigSpace5CLR        = 0x35434C52;    /* '5CLR' */

    /**
     * ICC配置文件颜色空间类型签名：'6CLR'。
     */
    public static final int icSigSpace6CLR        = 0x36434C52;    /* '6CLR' */

    /**
     * ICC配置文件颜色空间类型签名：'7CLR'。
     */
    public static final int icSigSpace7CLR        = 0x37434C52;    /* '7CLR' */

    /**
     * ICC配置文件颜色空间类型签名：'8CLR'。
     */
    public static final int icSigSpace8CLR        = 0x38434C52;    /* '8CLR' */

    /**
     * ICC配置文件颜色空间类型签名：'9CLR'。
     */
    public static final int icSigSpace9CLR        = 0x39434C52;    /* '9CLR' */

    /**
     * ICC配置文件颜色空间类型签名：'ACLR'。
     */
    public static final int icSigSpaceACLR        = 0x41434C52;    /* 'ACLR' */

    /**
     * ICC配置文件颜色空间类型签名：'BCLR'。
     */
    public static final int icSigSpaceBCLR        = 0x42434C52;    /* 'BCLR' */

    /**
     * ICC配置文件颜色空间类型签名：'CCLR'。
     */
    public static final int icSigSpaceCCLR        = 0x43434C52;    /* 'CCLR' */

    /**
     * ICC配置文件颜色空间类型签名：'DCLR'。
     */
    public static final int icSigSpaceDCLR        = 0x44434C52;    /* 'DCLR' */

    /**
     * ICC配置文件颜色空间类型签名：'ECLR'。
     */
    public static final int icSigSpaceECLR        = 0x45434C52;    /* 'ECLR' */

    /**
     * ICC配置文件颜色空间类型签名：'FCLR'。
     */
    public static final int icSigSpaceFCLR        = 0x46434C52;    /* 'FCLR' */


    /**
     * ICC配置文件类签名：'scnr'。
     */
    public static final int icSigInputClass       = 0x73636E72;    /* 'scnr' */

    /**
     * ICC配置文件类签名：'mntr'。
     */
    public static final int icSigDisplayClass     = 0x6D6E7472;    /* 'mntr' */

    /**
     * ICC配置文件类签名：'prtr'。
     */
    public static final int icSigOutputClass      = 0x70727472;    /* 'prtr' */

    /**
     * ICC配置文件类签名：'link'。
     */
    public static final int icSigLinkClass        = 0x6C696E6B;    /* 'link' */

    /**
     * ICC配置文件类签名：'abst'。
     */
    public static final int icSigAbstractClass    = 0x61627374;    /* 'abst' */

    /**
     * ICC配置文件类签名：'spac'。
     */
    public static final int icSigColorSpaceClass  = 0x73706163;    /* 'spac' */

    /**
     * ICC配置文件类签名：'nmcl'。
     */
    public static final int icSigNamedColorClass  = 0x6e6d636c;    /* 'nmcl' */


    /**
     * ICC配置文件渲染意图：感知。
     */
    public static final int icPerceptual            = 0;

    /**
     * ICC配置文件渲染意图：相对色度。
     */
    public static final int icRelativeColorimetric    = 1;

    /**
     * ICC配置文件渲染意图：媒体相对色度。
     * @since 1.5
     */
    public static final int icMediaRelativeColorimetric = 1;

    /**
     * ICC配置文件渲染意图：饱和度。
     */
    public static final int icSaturation            = 2;

    /**
     * ICC配置文件渲染意图：绝对色度。
     */
    public static final int icAbsoluteColorimetric    = 3;

    /**
     * ICC配置文件渲染意图：ICC绝对色度。
     * @since 1.5
     */
    public static final int icICCAbsoluteColorimetric = 3;


    /**
     * ICC配置文件标签签名：'head' - 特殊。
     */
    public static final int icSigHead      = 0x68656164; /* 'head' - 特殊 */

    /**
     * ICC配置文件标签签名：'A2B0'。
     */
    public static final int icSigAToB0Tag         = 0x41324230;    /* 'A2B0' */

    /**
     * ICC配置文件标签签名：'A2B1'。
     */
    public static final int icSigAToB1Tag         = 0x41324231;    /* 'A2B1' */

    /**
     * ICC配置文件标签签名：'A2B2'。
     */
    public static final int icSigAToB2Tag         = 0x41324232;    /* 'A2B2' */

    /**
     * ICC配置文件标签签名：'bXYZ'。
     */
    public static final int icSigBlueColorantTag  = 0x6258595A;    /* 'bXYZ' */

    /**
     * ICC配置文件标签签名：'bXYZ'。
     * @since 1.5
     */
    public static final int icSigBlueMatrixColumnTag = 0x6258595A; /* 'bXYZ' */

    /**
     * ICC配置文件标签签名：'bTRC'。
     */
    public static final int icSigBlueTRCTag       = 0x62545243;    /* 'bTRC' */

    /**
     * ICC配置文件标签签名：'B2A0'。
     */
    public static final int icSigBToA0Tag         = 0x42324130;    /* 'B2A0' */

    /**
     * ICC配置文件标签签名：'B2A1'。
     */
    public static final int icSigBToA1Tag         = 0x42324131;    /* 'B2A1' */

    /**
     * ICC配置文件标签签名：'B2A2'。
     */
    public static final int icSigBToA2Tag         = 0x42324132;    /* 'B2A2' */

    /**
     * ICC配置文件标签签名：'calt'。
     */
    public static final int icSigCalibrationDateTimeTag = 0x63616C74;
                                                                   /* 'calt' */

    /**
     * ICC配置文件标签签名：'targ'。
     */
    public static final int icSigCharTargetTag    = 0x74617267;    /* 'targ' */

    /**
     * ICC配置文件标签签名：'cprt'。
     */
    public static final int icSigCopyrightTag     = 0x63707274;    /* 'cprt' */

    /**
     * ICC配置文件标签签名：'crdi'。
     */
    public static final int icSigCrdInfoTag       = 0x63726469;    /* 'crdi' */

    /**
     * ICC配置文件标签签名：'dmnd'。
     */
    public static final int icSigDeviceMfgDescTag = 0x646D6E64;    /* 'dmnd' */

    /**
     * ICC配置文件标签签名：'dmdd'。
     */
    public static final int icSigDeviceModelDescTag = 0x646D6464;  /* 'dmdd' */

    /**
     * ICC配置文件标签签名：'devs'。
     */
    public static final int icSigDeviceSettingsTag =  0x64657673;  /* 'devs' */

    /**
     * ICC配置文件标签签名：'gamt'。
     */
    public static final int icSigGamutTag         = 0x67616D74;    /* 'gamt' */

    /**
     * ICC配置文件标签签名：'kTRC'。
     */
    public static final int icSigGrayTRCTag       = 0x6b545243;    /* 'kTRC' */

    /**
     * ICC配置文件标签签名：'gXYZ'。
     */
    public static final int icSigGreenColorantTag = 0x6758595A;    /* 'gXYZ' */

    /**
     * ICC配置文件标签签名：'gXYZ'。
     * @since 1.5
     */
    public static final int icSigGreenMatrixColumnTag = 0x6758595A;/* 'gXYZ' */

    /**
     * ICC配置文件标签签名：'gTRC'。
     */
    public static final int icSigGreenTRCTag      = 0x67545243;    /* 'gTRC' */

    /**
     * ICC配置文件标签签名：'lumi'。
     */
    public static final int icSigLuminanceTag     = 0x6C756d69;    /* 'lumi' */

    /**
     * ICC配置文件标签签名：'meas'。
     */
    public static final int icSigMeasurementTag   = 0x6D656173;    /* 'meas' */

    /**
     * ICC配置文件标签签名：'bkpt'。
     */
    public static final int icSigMediaBlackPointTag = 0x626B7074;  /* 'bkpt' */

    /**
     * ICC配置文件标签签名：'wtpt'。
     */
    public static final int icSigMediaWhitePointTag = 0x77747074;  /* 'wtpt' */

    /**
     * ICC配置文件标签签名：'ncl2'。
     */
    public static final int icSigNamedColor2Tag   = 0x6E636C32;    /* 'ncl2' */

    /**
     * ICC配置文件标签签名：'resp'。
     */
    public static final int icSigOutputResponseTag = 0x72657370;   /* 'resp' */

    /**
     * ICC配置文件标签签名：'pre0'。
     */
    public static final int icSigPreview0Tag      = 0x70726530;    /* 'pre0' */

    /**
     * ICC配置文件标签签名：'pre1'。
     */
    public static final int icSigPreview1Tag      = 0x70726531;    /* 'pre1' */

    /**
     * ICC配置文件标签签名：'pre2'。
     */
    public static final int icSigPreview2Tag      = 0x70726532;    /* 'pre2' */


                /**
     * ICC 配置文件标签签名：'desc'。
     */
    public static final int icSigProfileDescriptionTag = 0x64657363;
                                                                   /* 'desc' */

    /**
     * ICC 配置文件标签签名：'pseq'。
     */
    public static final int icSigProfileSequenceDescTag = 0x70736571;
                                                                   /* 'pseq' */

    /**
     * ICC 配置文件标签签名：'psd0'。
     */
    public static final int icSigPs2CRD0Tag       = 0x70736430;    /* 'psd0' */

    /**
     * ICC 配置文件标签签名：'psd1'。
     */
    public static final int icSigPs2CRD1Tag       = 0x70736431;    /* 'psd1' */

    /**
     * ICC 配置文件标签签名：'psd2'。
     */
    public static final int icSigPs2CRD2Tag       = 0x70736432;    /* 'psd2' */

    /**
     * ICC 配置文件标签签名：'psd3'。
     */
    public static final int icSigPs2CRD3Tag       = 0x70736433;    /* 'psd3' */

    /**
     * ICC 配置文件标签签名：'ps2s'。
     */
    public static final int icSigPs2CSATag        = 0x70733273;    /* 'ps2s' */

    /**
     * ICC 配置文件标签签名：'ps2i'。
     */
    public static final int icSigPs2RenderingIntentTag = 0x70733269;
                                                                   /* 'ps2i' */

    /**
     * ICC 配置文件标签签名：'rXYZ'。
     */
    public static final int icSigRedColorantTag   = 0x7258595A;    /* 'rXYZ' */

    /**
     * ICC 配置文件标签签名：'rXYZ'。
     * @since 1.5
     */
    public static final int icSigRedMatrixColumnTag = 0x7258595A;  /* 'rXYZ' */

    /**
     * ICC 配置文件标签签名：'rTRC'。
     */
    public static final int icSigRedTRCTag        = 0x72545243;    /* 'rTRC' */

    /**
     * ICC 配置文件标签签名：'scrd'。
     */
    public static final int icSigScreeningDescTag = 0x73637264;    /* 'scrd' */

    /**
     * ICC 配置文件标签签名：'scrn'。
     */
    public static final int icSigScreeningTag     = 0x7363726E;    /* 'scrn' */

    /**
     * ICC 配置文件标签签名：'tech'。
     */
    public static final int icSigTechnologyTag    = 0x74656368;    /* 'tech' */

    /**
     * ICC 配置文件标签签名：'bfd '。
     */
    public static final int icSigUcrBgTag         = 0x62666420;    /* 'bfd ' */

    /**
     * ICC 配置文件标签签名：'vued'。
     */
    public static final int icSigViewingCondDescTag = 0x76756564;  /* 'vued' */

    /**
     * ICC 配置文件标签签名：'view'。
     */
    public static final int icSigViewingConditionsTag = 0x76696577;/* 'view' */

    /**
     * ICC 配置文件标签签名：'chrm'。
     */
    public static final int icSigChromaticityTag  = 0x6368726d;    /* 'chrm' */

    /**
     * ICC 配置文件标签签名：'chad'。
     * @since 1.5
     */
    public static final int icSigChromaticAdaptationTag = 0x63686164;/* 'chad' */

    /**
     * ICC 配置文件标签签名：'clro'。
     * @since 1.5
     */
    public static final int icSigColorantOrderTag = 0x636C726F;    /* 'clro' */

    /**
     * ICC 配置文件标签签名：'clrt'。
     * @since 1.5
     */
    public static final int icSigColorantTableTag = 0x636C7274;    /* 'clrt' */


    /**
     * ICC 配置文件头位置：配置文件大小（字节）。
     */
    public static final int icHdrSize         = 0;  /* 配置文件大小（字节） */

    /**
     * ICC 配置文件头位置：此配置文件的 CMM。
     */
    public static final int icHdrCmmId        = 4;  /* 此配置文件的 CMM */

    /**
     * ICC 配置文件头位置：格式版本号。
     */
    public static final int icHdrVersion      = 8;  /* 格式版本号 */

    /**
     * ICC 配置文件头位置：配置文件类型。
     */
    public static final int icHdrDeviceClass  = 12; /* 配置文件类型 */

    /**
     * ICC 配置文件头位置：数据的颜色空间。
     */
    public static final int icHdrColorSpace   = 16; /* 数据的颜色空间 */

    /**
     * ICC 配置文件头位置：PCS - 仅 XYZ 或 Lab。
     */
    public static final int icHdrPcs          = 20; /* PCS - 仅 XYZ 或 Lab */

    /**
     * ICC 配置文件头位置：配置文件创建日期。
     */
    public static final int icHdrDate       = 24; /* 配置文件创建日期 */

    /**
     * ICC 配置文件头位置：icMagicNumber。
     */
    public static final int icHdrMagic        = 36; /* icMagicNumber */

    /**
     * ICC 配置文件头位置：主要平台。
     */
    public static final int icHdrPlatform     = 40; /* 主要平台 */

    /**
     * ICC 配置文件头位置：各种位设置。
     */
    public static final int icHdrFlags        = 44; /* 各种位设置 */

    /**
     * ICC 配置文件头位置：设备制造商。
     */
    public static final int icHdrManufacturer = 48; /* 设备制造商 */

    /**
     * ICC 配置文件头位置：设备型号。
     */
    public static final int icHdrModel        = 52; /* 设备型号 */

    /**
     * ICC 配置文件头位置：设备属性。
     */
    public static final int icHdrAttributes   = 56; /* 设备属性 */

    /**
     * ICC 配置文件头位置：渲染意图。
     */
    public static final int icHdrRenderingIntent = 64; /* 渲染意图 */

    /**
     * ICC 配置文件头位置：配置文件的照明条件。
     */
    public static final int icHdrIlluminant   = 68; /* 配置文件的照明条件 */

    /**
     * ICC 配置文件头位置：配置文件创建者。
     */
    public static final int icHdrCreator      = 80; /* 配置文件创建者 */

    /**
     * ICC 配置文件头位置：配置文件的 ID。
     * @since 1.5
     */
    public static final int icHdrProfileID = 84; /* 配置文件的 ID */


    /**
     * ICC 配置文件常量：标签类型签名。
     */
    public static final int icTagType          = 0;    /* 标签类型签名 */

    /**
     * ICC 配置文件常量：保留。
     */
    public static final int icTagReserved      = 4;    /* 保留 */

    /**
     * ICC 配置文件常量：curveType 计数。
     */
    public static final int icCurveCount       = 8;    /* curveType 计数 */

    /**
     * ICC 配置文件常量：curveType 数据。
     */
    public static final int icCurveData        = 12;   /* curveType 数据 */

    /**
     * ICC 配置文件常量：XYZNumber X。
     */
    public static final int icXYZNumberX       = 8;    /* XYZNumber X */


    /**
     * 构造一个具有给定 ID 的 ICC_Profile 对象。
     */
    ICC_Profile(Profile p) {
        this.cmmProfile = p;
    }


    /**
     * 构造一个加载将被延迟的 ICC_Profile 对象。
     * 在配置文件加载之前，ID 将为 0。
     */
    ICC_Profile(ProfileDeferralInfo pdi) {
        this.deferralInfo = pdi;
        this.profileActivator = new ProfileActivator() {
            public void activate() throws ProfileDataException {
                activateDeferredProfile();
            }
        };
        ProfileDeferralMgr.registerDeferral(this.profileActivator);
    }


    /**
     * 释放与 ICC_Profile 对象关联的资源。
     */
    protected void finalize () {
        if (cmmProfile != null) {
            CMSManager.getModule().freeProfile(cmmProfile);
        } else if (profileActivator != null) {
            ProfileDeferralMgr.unregisterDeferral(profileActivator);
        }
    }


    /**
     * 构造一个对应于字节数组数据的 ICC_Profile 对象。
     * 如果数据不符合有效的 ICC 配置文件，将抛出 IllegalArgumentException。
     * @param data 指定的 ICC 配置文件数据
     * @return 一个对应于指定 <code>data</code> 数组数据的 <code>ICC_Profile</code> 对象。
     */
    public static ICC_Profile getInstance(byte[] data) {
    ICC_Profile thisProfile;

        Profile p = null;

        if (ProfileDeferralMgr.deferring) {
            ProfileDeferralMgr.activateProfiles();
        }

        ProfileDataVerifier.verify(data);

        try {
            p = CMSManager.getModule().loadProfile(data);
        } catch (CMMException c) {
            throw new IllegalArgumentException("无效的 ICC 配置文件数据");
        }

        try {
            if ((getColorSpaceType (p) == ColorSpace.TYPE_GRAY) &&
                (getData (p, icSigMediaWhitePointTag) != null) &&
                (getData (p, icSigGrayTRCTag) != null)) {
                thisProfile = new ICC_ProfileGray (p);
            }
            else if ((getColorSpaceType (p) == ColorSpace.TYPE_RGB) &&
                (getData (p, icSigMediaWhitePointTag) != null) &&
                (getData (p, icSigRedColorantTag) != null) &&
                (getData (p, icSigGreenColorantTag) != null) &&
                (getData (p, icSigBlueColorantTag) != null) &&
                (getData (p, icSigRedTRCTag) != null) &&
                (getData (p, icSigGreenTRCTag) != null) &&
                (getData (p, icSigBlueTRCTag) != null)) {
                thisProfile = new ICC_ProfileRGB (p);
            }
            else {
                thisProfile = new ICC_Profile (p);
            }
        } catch (CMMException c) {
            thisProfile = new ICC_Profile (p);
        }
        return thisProfile;
    }



    /**
     * 构造一个对应于 ColorSpace 类定义的特定颜色空间之一的 ICC_Profile。
     * 如果 cspace 不是定义的颜色空间之一，将抛出 IllegalArgumentException。
     *
     * @param cspace 要创建配置文件的颜色空间类型。
     * 指定的类型是 <CODE>ColorSpace</CODE> 类中定义的颜色空间常量之一。
     *
     * @return 一个对应于指定 <code>ColorSpace</code> 类型的 <code>ICC_Profile</code> 对象。
     * @exception IllegalArgumentException 如果 <CODE>cspace</CODE> 不是预定义的颜色空间类型之一。
     */
    public static ICC_Profile getInstance (int cspace) {
        ICC_Profile thisProfile = null;
        String fileName;

        switch (cspace) {
        case ColorSpace.CS_sRGB:
            synchronized(ICC_Profile.class) {
                if (sRGBprofile == null) {
                    /*
                     * 延迟仅用于标准配置文件。
                     * 适当的访问权限处理在较低级别进行。
                     */
                    ProfileDeferralInfo pInfo =
                        new ProfileDeferralInfo("sRGB.pf",
                                                ColorSpace.TYPE_RGB, 3,
                                                CLASS_DISPLAY);
                    sRGBprofile = getDeferredInstance(pInfo);
                }
                thisProfile = sRGBprofile;
            }

            break;

        case ColorSpace.CS_CIEXYZ:
            synchronized(ICC_Profile.class) {
                if (XYZprofile == null) {
                    ProfileDeferralInfo pInfo =
                        new ProfileDeferralInfo("CIEXYZ.pf",
                                                ColorSpace.TYPE_XYZ, 3,
                                                CLASS_DISPLAY);
                    XYZprofile = getDeferredInstance(pInfo);
                }
                thisProfile = XYZprofile;
            }

            break;

        case ColorSpace.CS_PYCC:
            synchronized(ICC_Profile.class) {
                if (PYCCprofile == null) {
                    if (standardProfileExists("PYCC.pf"))
                    {
                        ProfileDeferralInfo pInfo =
                            new ProfileDeferralInfo("PYCC.pf",
                                                    ColorSpace.TYPE_3CLR, 3,
                                                    CLASS_DISPLAY);
                        PYCCprofile = getDeferredInstance(pInfo);
                    } else {
                        throw new IllegalArgumentException(
                                "无法加载标准配置文件：PYCC.pf");
                    }
                }
                thisProfile = PYCCprofile;
            }

            break;

        case ColorSpace.CS_GRAY:
            synchronized(ICC_Profile.class) {
                if (GRAYprofile == null) {
                    ProfileDeferralInfo pInfo =
                        new ProfileDeferralInfo("GRAY.pf",
                                                ColorSpace.TYPE_GRAY, 1,
                                                CLASS_DISPLAY);
                    GRAYprofile = getDeferredInstance(pInfo);
                }
                thisProfile = GRAYprofile;
            }

            break;

        case ColorSpace.CS_LINEAR_RGB:
            synchronized(ICC_Profile.class) {
                if (LINEAR_RGBprofile == null) {
                    ProfileDeferralInfo pInfo =
                        new ProfileDeferralInfo("LINEAR_RGB.pf",
                                                ColorSpace.TYPE_RGB, 3,
                                                CLASS_DISPLAY);
                    LINEAR_RGBprofile = getDeferredInstance(pInfo);
                }
                thisProfile = LINEAR_RGBprofile;
            }

            break;

        default:
            throw new IllegalArgumentException("未知的颜色空间");
        }

        return thisProfile;
    }

    /* 这个方法断言系统权限，因此仅用于标准配置文件。 */
    private static ICC_Profile getStandardProfile(final String name) {

        return AccessController.doPrivileged(
            new PrivilegedAction<ICC_Profile>() {
                 public ICC_Profile run() {
                     ICC_Profile p = null;
                     try {
                         p = getInstance (name);
                     } catch (IOException ex) {
                         throw new IllegalArgumentException(
                               "无法加载标准配置文件： " + name);
                     }
                     return p;
                 }
             });
    }

    /**
     * 构造一个对应于文件数据的 ICC_Profile。
     * fileName 可以是绝对文件路径或相对文件路径。
     * 相对文件名会在多个位置查找：首先，在由 java.iccprofile.path 属性指定的任何目录中；
     * 其次，在由 java.class.path 属性指定的任何目录中；最后，在用于存储始终可用的配置文件的目录中，
     * 例如 sRGB 配置文件。内置配置文件使用 .pf 作为文件扩展名，例如 sRGB.pf。
     * 如果指定的文件无法打开或在读取文件时发生 I/O 错误，此方法将抛出 IOException。
     * 如果文件不包含有效的 ICC 配置文件数据，将抛出 IllegalArgumentException。
     * @param fileName 包含配置文件数据的文件。
     *
     * @return 一个对应于指定文件数据的 <code>ICC_Profile</code> 对象。
     * @exception IOException 如果指定的文件无法打开或在读取文件时发生 I/O 错误。
     *
     * @exception IllegalArgumentException 如果文件不包含有效的 ICC 配置文件数据。
     *
     * @exception SecurityException 如果安装了安全经理且其不允许读取给定文件。
     */
    public static ICC_Profile getInstance(String fileName) throws IOException {
        ICC_Profile thisProfile;
        FileInputStream fis = null;


        File f = getProfileFile(fileName);
        if (f != null) {
            fis = new FileInputStream(f);
        }
        if (fis == null) {
            throw new IOException("无法打开文件 " + fileName);
        }

        thisProfile = getInstance(fis);

        fis.close();    /* 关闭文件 */

        return thisProfile;
    }


    /**
     * 构造一个与 InputStream 中的数据对应的 ICC_Profile。
     * 如果流中不包含有效的 ICC 配置文件数据，则此方法会抛出 IllegalArgumentException。
     * 如果在读取流时发生 I/O 错误，则抛出 IOException。
     * @param s 从中读取配置文件数据的输入流。
     *
     * @return 与指定 InputStream 中的数据对应的 <CODE>ICC_Profile</CODE> 对象。
     *
     * @exception IOException 如果在读取流时发生 I/O 错误。
     *
     * @exception IllegalArgumentException 如果流中不包含有效的 ICC 配置文件数据。
     */
    public static ICC_Profile getInstance(InputStream s) throws IOException {
    byte profileData[];

        if (s instanceof ProfileDeferralInfo) {
            /* 用于检测可以延迟加载的配置文件的技巧 */
            return getDeferredInstance((ProfileDeferralInfo) s);
        }

        if ((profileData = getProfileDataFromStream(s)) == null) {
            throw new IllegalArgumentException("无效的 ICC 配置文件数据");
        }

        return getInstance(profileData);
    }


    static byte[] getProfileDataFromStream(InputStream s) throws IOException {

        BufferedInputStream bis = new BufferedInputStream(s);
        bis.mark(128); // 128 是 ICC 配置文件头的长度

        byte[] header = IOUtils.readNBytes(bis, 128);
        if (header.length < 128 || header[36] != 0x61 || header[37] != 0x63 ||
            header[38] != 0x73 || header[39] != 0x70) {
            return null;   /* 非有效配置文件 */
        }
        int profileSize = ((header[0] & 0xff) << 24) |
                          ((header[1] & 0xff) << 16) |
                          ((header[2] & 0xff) << 8) |
                          (header[3] & 0xff);
        bis.reset();
        try {
            return IOUtils.readNBytes(bis, profileSize);
        } catch (OutOfMemoryError e) {
            throw new IOException("颜色配置文件太大");
        }
    }


    /**
     * 构造一个 ICC_Profile，其实际的配置文件数据加载和 CMM 的初始化应尽可能延迟。
     * 延迟仅用于标准配置文件。
     * 如果禁用了延迟，则 getStandardProfile() 确保在加载此配置文件时授予所有适当的访问权限。
     * 如果启用了延迟，则延迟激活代码将处理访问权限。
     * @see activateDeferredProfile()
     */
    static ICC_Profile getDeferredInstance(ProfileDeferralInfo pdi) {
        if (!ProfileDeferralMgr.deferring) {
            return getStandardProfile(pdi.filename);
        }
        if (pdi.colorSpaceType == ColorSpace.TYPE_RGB) {
            return new ICC_ProfileRGB(pdi);
        } else if (pdi.colorSpaceType == ColorSpace.TYPE_GRAY) {
            return new ICC_ProfileGray(pdi);
        } else {
            return new ICC_Profile(pdi);
        }
    }


    void activateDeferredProfile() throws ProfileDataException {
        byte profileData[];
        FileInputStream fis;
        final String fileName = deferralInfo.filename;

        profileActivator = null;
        deferralInfo = null;
        PrivilegedAction<FileInputStream> pa = new PrivilegedAction<FileInputStream>() {
            public FileInputStream run() {
                File f = getStandardProfileFile(fileName);
                if (f != null) {
                    try {
                        return new FileInputStream(f);
                    } catch (FileNotFoundException e) {}
                }
                return null;
            }
        };
        if ((fis = AccessController.doPrivileged(pa)) == null) {
            throw new ProfileDataException("无法打开文件 " + fileName);
        }
        try {
            profileData = getProfileDataFromStream(fis);
            fis.close();    /* 关闭文件 */
        }
        catch (IOException e) {
            ProfileDataException pde = new
                ProfileDataException("无效的 ICC 配置文件数据" + fileName);
            pde.initCause(e);
            throw pde;
        }
        if (profileData == null) {
            throw new ProfileDataException("无效的 ICC 配置文件数据" +
                fileName);
        }
        try {
            cmmProfile = CMSManager.getModule().loadProfile(profileData);
        } catch (CMMException c) {
            ProfileDataException pde = new
                ProfileDataException("无效的 ICC 配置文件数据" + fileName);
            pde.initCause(c);
            throw pde;
        }
    }


    /**
     * 返回配置文件的主要版本。
     * @return 配置文件的主要版本。
     */
    public int getMajorVersion() {
    byte[] theHeader;

        theHeader = getData(icSigHead); /* 如果必要，getData 将激活延迟的配置文件 */

        return (int) theHeader[8];
    }

    /**
     * 返回配置文件的次要版本。
     * @return 配置文件的次要版本。
     */
    public int getMinorVersion() {
    byte[] theHeader;

        theHeader = getData(icSigHead); /* 如果必要，getData 将激活延迟的配置文件 */

        return (int) theHeader[9];
    }

    /**
     * 返回配置文件类。
     * @return 预定义的配置文件类常量之一。
     */
    public int getProfileClass() {
    byte[] theHeader;
    int theClassSig, theClass;

        if (deferralInfo != null) {
            return deferralInfo.profileClass; /* 需要此信息用于 ICC_ColorSpace，而不会导致延迟配置文件被加载 */
        }

        theHeader = getData(icSigHead);

        theClassSig = intFromBigEndian (theHeader, icHdrDeviceClass);

        switch (theClassSig) {
        case icSigInputClass:
            theClass = CLASS_INPUT;
            break;

        case icSigDisplayClass:
            theClass = CLASS_DISPLAY;
            break;

        case icSigOutputClass:
            theClass = CLASS_OUTPUT;
            break;

        case icSigLinkClass:
            theClass = CLASS_DEVICELINK;
            break;

        case icSigColorSpaceClass:
            theClass = CLASS_COLORSPACECONVERSION;
            break;

        case icSigAbstractClass:
            theClass = CLASS_ABSTRACT;
            break;

        case icSigNamedColorClass:
            theClass = CLASS_NAMEDCOLOR;
            break;

        default:
            throw new IllegalArgumentException("未知的配置文件类");
        }

        return theClass;
    }

    /**
     * 返回颜色空间类型。返回 ColorSpace 类定义的颜色空间类型常量之一。
     * 这是配置文件的“输入”颜色空间。类型定义了颜色空间的组件数量及其解释，
     * 例如，TYPE_RGB 识别一个具有三个组件 - 红色、绿色和蓝色的颜色空间。它不定义空间的特定颜色特性，
     * 例如，主色的色度。
     * @return ColorSpace 类中定义的颜色空间类型常量之一。
     */
    public int getColorSpaceType() {
        if (deferralInfo != null) {
            return deferralInfo.colorSpaceType; /* 需要此信息用于 ICC_ColorSpace，而不会导致延迟配置文件被加载 */
        }
        return    getColorSpaceType(cmmProfile);
    }

    static int getColorSpaceType(Profile p) {
    byte[] theHeader;
    int theColorSpaceSig, theColorSpace;

        theHeader = getData(p, icSigHead);
        theColorSpaceSig = intFromBigEndian(theHeader, icHdrColorSpace);
        theColorSpace = iccCStoJCS (theColorSpaceSig);
        return theColorSpace;
    }

    /**
     * 返回 Profile Connection Space (PCS) 的颜色空间类型。
     * 返回 ColorSpace 类定义的颜色空间类型常量之一。这是配置文件的“输出”颜色空间。
     * 对于用于标记颜色或图像的输入、显示或输出配置文件，这将是 TYPE_XYZ 或 TYPE_Lab，并应解释为 ICC 规范中定义的相应特定颜色空间。
     * 对于设备链接配置文件，这可以是任何颜色空间类型常量。
     * @return ColorSpace 类中定义的颜色空间类型常量之一。
     */
    public int getPCSType() {
        if (ProfileDeferralMgr.deferring) {
            ProfileDeferralMgr.activateProfiles();
        }
        return getPCSType(cmmProfile);
    }


    static int getPCSType(Profile p) {
    byte[] theHeader;
    int thePCSSig, thePCS;

        theHeader = getData(p, icSigHead);
        thePCSSig = intFromBigEndian(theHeader, icHdrPcs);
        thePCS = iccCStoJCS(thePCSSig);
        return thePCS;
    }


    /**
     * 将此 ICC_Profile 写入文件。
     *
     * @param fileName 要写入配置文件数据的文件。
     *
     * @exception IOException 如果无法打开文件进行写入或在写入文件时发生 I/O 错误。
     */
    public void write(String fileName) throws IOException {
    FileOutputStream outputFile;
    byte profileData[];

        profileData = getData(); /* 如果必要，这将激活延迟的配置文件 */
        outputFile = new FileOutputStream(fileName);
        outputFile.write(profileData);
        outputFile.close ();
    }


    /**
     * 将此 ICC_Profile 写入 OutputStream。
     *
     * @param s 要写入配置文件数据的流。
     *
     * @exception IOException 如果在写入流时发生 I/O 错误。
     */
    public void write(OutputStream s) throws IOException {
    byte profileData[];

        profileData = getData(); /* 如果必要，这将激活延迟的配置文件 */
        s.write(profileData);
    }


    /**
     * 返回与此 ICC_Profile 对应的数据的字节数组。
     * @return 包含配置文件数据的字节数组。
     * @see #setData(int, byte[])
     */
    public byte[] getData() {
    int profileSize;
    byte[] profileData;

        if (ProfileDeferralMgr.deferring) {
            ProfileDeferralMgr.activateProfiles();
        }

        PCMM mdl = CMSManager.getModule();

        /* 获取此配置文件所需的字节数 */
        profileSize = mdl.getProfileSize(cmmProfile);

        profileData = new byte [profileSize];

        /* 获取配置文件的数据 */
        mdl.getProfileData(cmmProfile, profileData);

        return profileData;
    }


    /**
     * 以字节数组的形式返回配置文件中的特定标记数据元素。元素由 ICC 规范中定义的签名标识。
     * 签名 icSigHead 可用于获取头。此方法对于需要直接访问配置文件数据的高级小程序或应用程序非常有用。
     *
     * @param tagSignature 您想要获取的数据元素的 ICC 标记签名。
     *
     * @return 包含标记数据元素的字节数组。如果指定的标记不存在，则返回 <code>null</code>。
     * @see #setData(int, byte[])
     */
    public byte[] getData(int tagSignature) {

        if (ProfileDeferralMgr.deferring) {
            ProfileDeferralMgr.activateProfiles();
        }

        return getData(cmmProfile, tagSignature);
    }


    static byte[] getData(Profile p, int tagSignature) {
    int tagSize;
    byte[] tagData;

        try {
            PCMM mdl = CMSManager.getModule();

            /* 获取此标记所需的字节数 */
            tagSize = mdl.getTagSize(p, tagSignature);

            tagData = new byte[tagSize]; /* 获取一个用于标记的数组 */

            /* 获取标记的数据 */
            mdl.getTagData(p, tagSignature, tagData);
        } catch(CMMException c) {
            tagData = null;
        }

        return tagData;
    }

    /**
     * 从字节数组中设置配置文件中的特定标记数据元素。数组应包含与 ICC 规范第 10 节中定义的 {@code tagSignature} 对应的格式数据。
     * 此方法对于需要直接访问配置文件数据的高级小程序或应用程序非常有用。
     *
     * @param tagSignature 您想要设置的数据元素的 ICC 标记签名。
     * @param tagData 要为指定的标记签名设置的数据。
     * @throws IllegalArgumentException 如果 {@code tagSignature} 不是 ICC 规范中定义的签名。
     * @throws IllegalArgumentException 如果 {@code tagData} 数组的内容不能被解释为与 {@code tagSignature} 对应的有效标记数据。
     * @see #getData
     */
    public void setData(int tagSignature, byte[] tagData) {

        if (ProfileDeferralMgr.deferring) {
            ProfileDeferralMgr.activateProfiles();
        }

        CMSManager.getModule().setTagData(cmmProfile, tagSignature, tagData);
    }

    /**
     * 设置配置文件的渲染意图。
     * 这用于从具有多个转换的配置文件中选择适当的转换。
     */
    void setRenderingIntent(int renderingIntent) {
        byte[] theHeader = getData(icSigHead);/* 如果必要，getData 将激活延迟的配置文件 */
        intToBigEndian (renderingIntent, theHeader, icHdrRenderingIntent);
                                                 /* 设置渲染意图 */
        setData (icSigHead, theHeader);
    }


    /**
     * 返回配置文件的渲染意图。
     * 这用于从具有多个转换的配置文件中选择适当的转换。通常在源配置文件中设置，以从输出配置文件中选择转换。
     */
    int getRenderingIntent() {
        byte[] theHeader = getData(icSigHead);/* 如果必要，getData 将激活延迟的配置文件 */

        int renderingIntent = intFromBigEndian(theHeader, icHdrRenderingIntent);
                                                 /* 设置渲染意图 */

        /* 根据 ICC 规范，只有最低有效 16 位用于编码渲染意图。最高有效 16 位应设置为零。因此，这里忽略了两个最高有效字节。
         *
         * 请参阅 http://www.color.org/ICC1v42_2006-05.pdf，第 7.2.15 节。
         */
        return (0xffff & renderingIntent);
    }


    /**
     * 返回此配置文件的“输入”颜色空间中的颜色分量数量。例如，如果此配置文件的颜色空间类型为 TYPE_RGB，则此方法将返回 3。
     *
     * @return 配置文件输入颜色空间中的颜色分量数量。
     *
     * @throws ProfileDataException 如果配置文件中的颜色空间无效
     */
    public int getNumComponents() {
    byte[]    theHeader;
    int    theColorSpaceSig, theNumComponents;

        if (deferralInfo != null) {
            return deferralInfo.numComponents; /* 需要这些信息来创建 ICC_ColorSpace，而不会导致延迟加载的配置文件被加载 */
        }
        theHeader = getData(icSigHead);

        theColorSpaceSig = intFromBigEndian (theHeader, icHdrColorSpace);

        switch (theColorSpaceSig) {
        case icSigGrayData:
            theNumComponents = 1;
            break;

        case icSigSpace2CLR:
            theNumComponents = 2;
            break;

        case icSigXYZData:
        case icSigLabData:
        case icSigLuvData:
        case icSigYCbCrData:
        case icSigYxyData:
        case icSigRgbData:
        case icSigHsvData:
        case icSigHlsData:
        case icSigCmyData:
        case icSigSpace3CLR:
            theNumComponents = 3;
            break;

        case icSigCmykData:
        case icSigSpace4CLR:
            theNumComponents = 4;
            break;

        case icSigSpace5CLR:
            theNumComponents = 5;
            break;

        case icSigSpace6CLR:
            theNumComponents = 6;
            break;

        case icSigSpace7CLR:
            theNumComponents = 7;
            break;

        case icSigSpace8CLR:
            theNumComponents = 8;
            break;

        case icSigSpace9CLR:
            theNumComponents = 9;
            break;

        case icSigSpaceACLR:
            theNumComponents = 10;
            break;

        case icSigSpaceBCLR:
            theNumComponents = 11;
            break;

        case icSigSpaceCCLR:
            theNumComponents = 12;
            break;

        case icSigSpaceDCLR:
            theNumComponents = 13;
            break;

        case icSigSpaceECLR:
            theNumComponents = 14;
            break;

        case icSigSpaceFCLR:
            theNumComponents = 15;
            break;

        default:
            throw new ProfileDataException ("无效的 ICC 颜色空间");
        }

        return theNumComponents;
    }


    /**
     * 返回一个长度为 3 的 float 数组，包含 ICC 配置文件中 mediaWhitePointTag 的 X、Y 和 Z 分量。
     */
    float[] getMediaWhitePoint() {
        return getXYZTag(icSigMediaWhitePointTag);
                                           /* 获取媒体白点标签 */
    }


    /**
     * 返回一个长度为 3 的 float 数组，包含 XYZType 标签中编码的 X、Y 和 Z 分量。
     */
    float[] getXYZTag(int theTagSignature) {
    byte[] theData;
    float[] theXYZNumber;
    int i1, i2, theS15Fixed16;

        theData = getData(theTagSignature); /* 获取标签数据 */
                                            /* 如果需要，getData 将激活延迟加载的配置文件 */

        theXYZNumber = new float [3];        /* 返回的数组 */

        /* 将 s15Fixed16Number 转换为 float */
        for (i1 = 0, i2 = icXYZNumberX; i1 < 3; i1++, i2 += 4) {
            theS15Fixed16 = intFromBigEndian(theData, i2);
            theXYZNumber [i1] = ((float) theS15Fixed16) / 65536.0f;
        }
        return theXYZNumber;
    }


    /**
     * 返回一个表示色调再现曲线 (TRC) 的 gamma 值。如果配置文件将 TRC 表示为表而不是单个 gamma 值，则抛出异常。在这种情况下，可以通过 getTRC() 获取实际的表。
     * theTagSignature 应该是 icSigGrayTRCTag、icSigRedTRCTag、icSigGreenTRCTag 或 icSigBlueTRCTag 之一。
     * @return 作为 float 的 gamma 值。
     * @exception ProfileDataException 如果配置文件未指定 TRC 为单个 gamma 值。
     */
    float getGamma(int theTagSignature) {
    byte[] theTRCData;
    float theGamma;
    int theU8Fixed8;

        theTRCData = getData(theTagSignature); /* 获取 TRC */
                                               /* 如果需要，getData 将激活延迟加载的配置文件 */

        if (intFromBigEndian (theTRCData, icCurveCount) != 1) {
            throw new ProfileDataException ("TRC 不是 gamma");
        }

        /* 将 u8Fixed8 转换为 float */
        theU8Fixed8 = (shortFromBigEndian(theTRCData, icCurveData)) & 0xffff;

        theGamma = ((float) theU8Fixed8) / 256.0f;

        return theGamma;
    }


    /**
     * 返回 TRC 作为 short 数组。如果配置文件将 TRC 指定为线性（gamma = 1.0）或简单的 gamma 值，此方法将抛出异常，应使用 getGamma() 方法获取 gamma 值。否则，返回的 short 数组表示一个查找表，其中输入的 Gray 值概念上在 [0.0, 1.0] 范围内。值 0.0 映射到数组索引 0，值 1.0 映射到数组索引 length-1。可以使用插值生成不精确映射到数组索引的输入值的输出值。输出值也线性映射到 [0.0, 1.0] 范围。值 0.0 由数组值 0x0000 表示，值 1.0 由 0xFFFF 表示，即这些值实际上是无符号 short 值，尽管它们以 short 数组的形式返回。
     * theTagSignature 应该是 icSigGrayTRCTag、icSigRedTRCTag、icSigGreenTRCTag 或 icSigBlueTRCTag 之一。
     * @return 表示 TRC 的 short 数组。
     * @exception ProfileDataException 如果配置文件未将 TRC 指定为表。
     */
    short[] getTRC(int theTagSignature) {
    byte[] theTRCData;
    short[] theTRC;
    int i1, i2, nElements, theU8Fixed8;

        theTRCData = getData(theTagSignature); /* 获取 TRC */
                                               /* 如果需要，getData 将激活延迟加载的配置文件 */

        nElements = intFromBigEndian(theTRCData, icCurveCount);

        if (nElements == 1) {
            throw new ProfileDataException("TRC 不是表");
        }

        /* 创建 short 数组 */
        theTRC = new short [nElements];

        for (i1 = 0, i2 = icCurveData; i1 < nElements; i1++, i2 += 2) {
            theTRC[i1] = shortFromBigEndian(theTRCData, i2);
        }

        return theTRC;
    }


    /* 将 ICC 颜色空间签名转换为 Java 颜色空间类型 */
    static int iccCStoJCS(int theColorSpaceSig) {
    int theColorSpace;

        switch (theColorSpaceSig) {
        case icSigXYZData:
            theColorSpace = ColorSpace.TYPE_XYZ;
            break;

        case icSigLabData:
            theColorSpace = ColorSpace.TYPE_Lab;
            break;

        case icSigLuvData:
            theColorSpace = ColorSpace.TYPE_Luv;
            break;

        case icSigYCbCrData:
            theColorSpace = ColorSpace.TYPE_YCbCr;
            break;

        case icSigYxyData:
            theColorSpace = ColorSpace.TYPE_Yxy;
            break;

        case icSigRgbData:
            theColorSpace = ColorSpace.TYPE_RGB;
            break;

        case icSigGrayData:
            theColorSpace = ColorSpace.TYPE_GRAY;
            break;

        case icSigHsvData:
            theColorSpace = ColorSpace.TYPE_HSV;
            break;

        case icSigHlsData:
            theColorSpace = ColorSpace.TYPE_HLS;
            break;

        case icSigCmykData:
            theColorSpace = ColorSpace.TYPE_CMYK;
            break;

        case icSigCmyData:
            theColorSpace = ColorSpace.TYPE_CMY;
            break;

        case icSigSpace2CLR:
            theColorSpace = ColorSpace.TYPE_2CLR;
            break;

        case icSigSpace3CLR:
            theColorSpace = ColorSpace.TYPE_3CLR;
            break;

        case icSigSpace4CLR:
            theColorSpace = ColorSpace.TYPE_4CLR;
            break;

        case icSigSpace5CLR:
            theColorSpace = ColorSpace.TYPE_5CLR;
            break;

        case icSigSpace6CLR:
            theColorSpace = ColorSpace.TYPE_6CLR;
            break;

        case icSigSpace7CLR:
            theColorSpace = ColorSpace.TYPE_7CLR;
            break;

        case icSigSpace8CLR:
            theColorSpace = ColorSpace.TYPE_8CLR;
            break;

        case icSigSpace9CLR:
            theColorSpace = ColorSpace.TYPE_9CLR;
            break;

        case icSigSpaceACLR:
            theColorSpace = ColorSpace.TYPE_ACLR;
            break;

        case icSigSpaceBCLR:
            theColorSpace = ColorSpace.TYPE_BCLR;
            break;

        case icSigSpaceCCLR:
            theColorSpace = ColorSpace.TYPE_CCLR;
            break;

        case icSigSpaceDCLR:
            theColorSpace = ColorSpace.TYPE_DCLR;
            break;

        case icSigSpaceECLR:
            theColorSpace = ColorSpace.TYPE_ECLR;
            break;

        case icSigSpaceFCLR:
            theColorSpace = ColorSpace.TYPE_FCLR;
            break;

        default:
            throw new IllegalArgumentException ("未知的颜色空间");
        }

        return theColorSpace;
    }


    static int intFromBigEndian(byte[] array, int index) {
        return (((array[index]   & 0xff) << 24) |
                ((array[index+1] & 0xff) << 16) |
                ((array[index+2] & 0xff) <<  8) |
                 (array[index+3] & 0xff));
    }


    static void intToBigEndian(int value, byte[] array, int index) {
            array[index]   = (byte) (value >> 24);
            array[index+1] = (byte) (value >> 16);
            array[index+2] = (byte) (value >>  8);
            array[index+3] = (byte) (value);
    }


    static short shortFromBigEndian(byte[] array, int index) {
        return (short) (((array[index]   & 0xff) << 8) |
                         (array[index+1] & 0xff));
    }


    static void shortToBigEndian(short value, byte[] array, int index) {
            array[index]   = (byte) (value >> 8);
            array[index+1] = (byte) (value);
    }


    /*
     * fileName 可以是绝对文件路径或相对文件路径。相对文件名将在多个位置查找：首先，相对于由 java.iccprofile.path 属性指定的任何目录；其次，相对于由 java.class.path 属性指定的任何目录；最后，在用于存储始终可用的配置文件的目录中，例如 sRGB 配置文件。内置配置文件使用 .pf 作为文件名扩展名，例如 sRGB.pf。
     */
    private static File getProfileFile(String fileName) {
        String path, dir, fullPath;

        File f = new File(fileName); /* 尝试绝对文件名 */
        if (f.isAbsolute()) {
            /* 对于绝对路径名，其余代码没有意义，因此在这里返回。 */
            return f.isFile() ? f : null;
        }
        if ((!f.isFile()) &&
                ((path = System.getProperty("java.iccprofile.path")) != null)){
                                    /* 尝试相对于 java.iccprofile.path */
                StringTokenizer st =
                    new StringTokenizer(path, File.pathSeparator);
                while (st.hasMoreTokens() && ((f == null) || (!f.isFile()))) {
                    dir = st.nextToken();
                        fullPath = dir + File.separatorChar + fileName;
                    f = new File(fullPath);
                    if (!isChildOf(f, dir)) {
                        f = null;
                    }
                }
            }

        if (((f == null) || (!f.isFile())) &&
                ((path = System.getProperty("java.class.path")) != null)) {
                                    /* 尝试相对于 java.class.path */
                StringTokenizer st =
                    new StringTokenizer(path, File.pathSeparator);
                while (st.hasMoreTokens() && ((f == null) || (!f.isFile()))) {
                    dir = st.nextToken();
                        fullPath = dir + File.separatorChar + fileName;
                    f = new File(fullPath);
                }
            }

        if ((f == null) || (!f.isFile())) {
            /* 尝试内置配置文件目录 */
            f = getStandardProfileFile(fileName);
        }
        if (f != null && f.isFile()) {
            return f;
        }
        return null;
    }

    /**
     * 返回与 fileName 指定的内置配置文件对应的文件对象。
     * 如果没有名称为 fileName 的内置配置文件，则此方法返回 null。
     */
    private static File getStandardProfileFile(String fileName) {
        String dir = System.getProperty("java.home") +
            File.separatorChar + "lib" + File.separatorChar + "cmm";
        String fullPath = dir + File.separatorChar + fileName;
        File f = new File(fullPath);
        return (f.isFile() && isChildOf(f, dir)) ? f : null;
    }

    /**
     * 检查给定文件是否位于给定目录内。
     */
    private static boolean isChildOf(File f, String dirName) {
        try {
            File dir = new File(dirName);
            String canonicalDirName = dir.getCanonicalPath();
            if (!canonicalDirName.endsWith(File.separator)) {
                canonicalDirName += File.separator;
            }
            String canonicalFileName = f.getCanonicalPath();
            return canonicalFileName.startsWith(canonicalDirName);
        } catch (IOException e) {
            /* 我们不期望在这里捕获 IOException，因为调用此函数之前总是会调用 isFile()。 */
            return false;
        }
    }

    /**
     * 检查由 fileName 指定的内置配置文件是否存在。
     */
    private static boolean standardProfileExists(final String fileName) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    return getStandardProfileFile(fileName) != null;
                }
            });
    }


    /*
     * 序列化支持。
     *
     * 直接反序列化的配置文件是没有用的，因为它们没有注册到 CMM。我们不允许直接调用构造函数，而是让客户端调用其中一个 getInstance 工厂方法，该方法将配置文件注册到 CMM。对于反序列化，我们实现了 readResolve 方法，该方法将无效的反序列化配置文件对象解析为通过 getInstance 获得的对象。
     *
     * ICC 配置文件有两个主要的工厂方法：getInstance(int cspace) 和 getInstance(byte[] data)。此 ICC_Profile 实现使用前者返回缓存的单例配置文件对象，其他实现也可能使用这种技术。为了在序列化过程中保留单例模式，我们以这样的方式序列化缓存的单例配置文件，使得反序列化的 VM 可以调用 getInstance(int cspace) 方法，该方法将反序列化的对象解析为相应的单例。
     *
     * 由于单例是 ICC_Profile 的私有成员，因此 readResolve 方法必须是 `protected' 而不是 `private'，以便子类的单例实例可以正确反序列化。
     */


    /**
     * 附加序列化数据在流中的格式版本。版本&nbsp;<code>1</code> 对应于 Java&nbsp;2
     * 平台,&nbsp;v1.3。
     * @since 1.3
     * @serial
     */
    private int iccProfileSerializedDataVersion = 1;


    /**
     * 将默认的可序列化字段写入流中。将一个字符串和一个字节数组作为附加数据写入流中。
     *
     * @param s 用于序列化的流。
     * @throws IOException
     *     由 <code>ObjectInputStream</code> 抛出。
     * @serialData
     *     <code>String</code> 是 {@link ColorSpace} 类中定义的
     *     <code>CS_<var>*</var></code> 常量之一的名称，如果配置文件对象是预定义颜色空间的配置文件
     *     （例如 <code>"CS_sRGB"</code>）。否则，字符串为 <code>null</code>。
     *     <p>
     *     <code>byte[]</code> 数组是配置文件的配置文件数据。对于预定义的颜色空间，将写入 <code>null</code>
     *     而不是配置文件数据。如果将来 Java API 版本中添加了新的预定义颜色空间，此类的未来版本可能会选择为新的预定义颜色空间
     *     写入不仅颜色空间名称，还有配置文件数据，以便旧版本仍能反序列化对象。
     */
    private void writeObject(ObjectOutputStream s)
      throws IOException
    {
        s.defaultWriteObject();

        String csName = null;
        if (this == sRGBprofile) {
            csName = "CS_sRGB";
        } else if (this == XYZprofile) {
            csName = "CS_CIEXYZ";
        } else if (this == PYCCprofile) {
            csName = "CS_PYCC";
        } else if (this == GRAYprofile) {
            csName = "CS_GRAY";
        } else if (this == LINEAR_RGBprofile) {
            csName = "CS_LINEAR_RGB";
        }

        // 未来版本可能会选择为新的预定义颜色空间写入配置文件数据，如果引入了新的预定义颜色空间，
        // 以便旧版本不识别新的 CS 名称时，可以回退到从数据构造配置文件。
        byte[] data = null;
        if (csName == null) {
            // getData 将在必要时激活延迟配置文件
            data = getData();
        }

        s.writeObject(csName);
        s.writeObject(data);
    }

    // 由 readObject 用于存储解析的配置文件（通过 getInstance 获得）以供 readResolve 返回。
    private transient ICC_Profile resolvedDeserializedProfile;

    /**
     * 从流中读取默认的可序列化字段。从流中读取一个字符串和一个字节数组作为附加数据。
     *
     * @param s 用于反序列化的流。
     * @throws IOException
     *     由 <code>ObjectInputStream</code> 抛出。
     * @throws ClassNotFoundException
     *     由 <code>ObjectInputStream</code> 抛出。
     * @serialData
     *     <code>String</code> 是 {@link ColorSpace} 类中定义的
     *     <code>CS_<var>*</var></code> 常量之一的名称，如果配置文件对象是预定义颜色空间的配置文件
     *     （例如 <code>"CS_sRGB"</code>）。否则，字符串为 <code>null</code>。
     *     <p>
     *     <code>byte[]</code> 数组是配置文件的配置文件数据。对于预定义的配置文件，通常为 <code>null</code>。
     *     <p>
     *     如果字符串被识别为预定义颜色空间的常量名称，对象将解析为通过
     *     <code>getInstance(int&nbsp;cspace)</code> 获得的配置文件，配置文件数据将被忽略。否则，对象将解析为通过
     *     <code>getInstance(byte[]&nbsp;data)</code> 获得的配置文件。
     * @see #readResolve()
     * @see #getInstance(int)
     * @see #getInstance(byte[])
     */
    private void readObject(ObjectInputStream s)
      throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();

        String csName = (String)s.readObject();
        byte[] data = (byte[])s.readObject();

        int cspace = 0;         // 如果已知则为 ColorSpace.CS_* 常量
        boolean isKnownPredefinedCS = false;
        if (csName != null) {
            isKnownPredefinedCS = true;
            if (csName.equals("CS_sRGB")) {
                cspace = ColorSpace.CS_sRGB;
            } else if (csName.equals("CS_CIEXYZ")) {
                cspace = ColorSpace.CS_CIEXYZ;
            } else if (csName.equals("CS_PYCC")) {
                cspace = ColorSpace.CS_PYCC;
            } else if (csName.equals("CS_GRAY")) {
                cspace = ColorSpace.CS_GRAY;
            } else if (csName.equals("CS_LINEAR_RGB")) {
                cspace = ColorSpace.CS_LINEAR_RGB;
            } else {
                isKnownPredefinedCS = false;
            }
        }

        if (isKnownPredefinedCS) {
            resolvedDeserializedProfile = getInstance(cspace);
        } else {
            resolvedDeserializedProfile = getInstance(data);
        }
    }

    /**
     * 将反序列化的实例解析为注册到 CMM 的实例。
     * @return 注册到 CMM 的 ICC_Profile 对象。
     * @throws ObjectStreamException
     *     从不抛出，但由序列化规范规定。
     * @since 1.3
     */
    protected Object readResolve() throws ObjectStreamException {
        return resolvedDeserializedProfile;
    }
}
