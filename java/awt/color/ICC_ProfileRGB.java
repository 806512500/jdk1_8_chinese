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

import sun.java2d.cmm.Profile;
import sun.java2d.cmm.ProfileDeferralInfo;

/**
 *
 * ICC_ProfileRGB 类是 ICC_Profile 类的子类，表示符合以下条件的配置文件：
 * <ul>
 * <li>配置文件的颜色空间类型为 RGB。</li>
 * <li>配置文件包含 <code>redColorantTag</code>、
 * <code>greenColorantTag</code>、<code>blueColorantTag</code>、
 * <code>redTRCTag</code>、<code>greenTRCTag</code>、
 * <code>blueTRCTag</code> 和 <code>mediaWhitePointTag</code> 标签。</li>
 * </ul>
 * 当这些条件满足时，<code>ICC_Profile</code> 的 <code>getInstance</code> 方法将
 * 返回一个 <code>ICC_ProfileRGB</code> 对象。三组件、基于矩阵的输入配置文件和 RGB 显示配置文件
 * 是这种类型配置文件的示例。
 * <p>
 * 该配置文件类提供了颜色转换矩阵和查找表，Java 或本机方法可以直接使用这些矩阵和表
 * 在某些情况下优化颜色转换。
 * <p>
 * 从设备配置文件颜色空间转换到 CIEXYZ 配置文件连接空间时，每个设备颜色组件首先通过
 * 对应的色调再现曲线（TRC）查找进行线性化。生成的线性 RGB 组件使用从 RGB 色彩成分构建的
 * 3x3 矩阵转换为 CIEXYZ PCS。
 * <pre>
 *
 * &nbsp;               linearR = redTRC[deviceR]
 *
 * &nbsp;               linearG = greenTRC[deviceG]
 *
 * &nbsp;               linearB = blueTRC[deviceB]
 *
 * &nbsp; _      _       _                                             _   _         _
 * &nbsp;[  PCSX  ]     [  redColorantX  greenColorantX  blueColorantX  ] [  linearR  ]
 * &nbsp;[        ]     [                                               ] [           ]
 * &nbsp;[  PCSY  ]  =  [  redColorantY  greenColorantY  blueColorantY  ] [  linearG  ]
 * &nbsp;[        ]     [                                               ] [           ]
 * &nbsp;[_ PCSZ _]     [_ redColorantZ  greenColorantZ  blueColorantZ _] [_ linearB _]
 *
 * </pre>
 * 反向转换是通过使用上述 3x3 矩阵的逆将 PCS XYZ 组件转换为线性
 * RGB 组件，然后通过 TRC 的逆将线性 RGB 转换为设备 RGB。
 */



public class ICC_ProfileRGB
extends ICC_Profile {

    static final long serialVersionUID = 8505067385152579334L;

    /**
     * 用于获取红色组件的伽马值或 TRC。
     */
    public static final int REDCOMPONENT = 0;

    /**
     * 用于获取绿色组件的伽马值或 TRC。
     */
    public static final int GREENCOMPONENT = 1;

    /**
     * 用于获取蓝色组件的伽马值或 TRC。
     */
    public static final int BLUECOMPONENT = 2;


    /**
     * 从 CMM ID 构造一个新的 <code>ICC_ProfileRGB</code>。
     *
     * @param p 配置文件的 CMM ID。
     *
     */
    ICC_ProfileRGB(Profile p) {
        super(p);
    }

    /**
     * 从 ProfileDeferralInfo 对象构造一个新的 <code>ICC_ProfileRGB</code>。
     *
     * @param pdi
     */
    ICC_ProfileRGB(ProfileDeferralInfo pdi) {
        super(pdi);
    }


    /**
     * 返回一个包含配置文件的 <CODE>mediaWhitePointTag</CODE> 组件的数组。
     *
     * @return 一个包含配置文件的 <CODE>mediaWhitePointTag</CODE> 的 x、y 和 z 组件的 3 元素 <CODE>float</CODE> 数组。
     */
    public float[] getMediaWhitePoint() {
        return super.getMediaWhitePoint();
    }


    /**
     * 返回一个从配置文件的 <CODE>redColorantTag</CODE>、
     * <CODE>greenColorantTag</CODE> 和 <CODE>blueColorantTag</CODE> 的 X、Y 和 Z 组件构建的 3x3 <CODE>float</CODE> 矩阵。
     * <p>
     * 该矩阵可用于配置文件的前向方向的颜色转换——从配置文件颜色空间
     * 到 CIEXYZ PCS。
     *
     * @return 一个包含配置文件的 <CODE>redColorantTag</CODE>、
     * <CODE>greenColorantTag</CODE> 和 <CODE>blueColorantTag</CODE> 的 x、y 和 z 组件的 3x3 <CODE>float</CODE> 数组。
     */
    public float[][] getMatrix() {
        float[][] theMatrix = new float[3][3];
        float[] tmpMatrix;

        tmpMatrix = getXYZTag(ICC_Profile.icSigRedColorantTag);
        theMatrix[0][0] = tmpMatrix[0];
        theMatrix[1][0] = tmpMatrix[1];
        theMatrix[2][0] = tmpMatrix[2];
        tmpMatrix = getXYZTag(ICC_Profile.icSigGreenColorantTag);
        theMatrix[0][1] = tmpMatrix[0];
        theMatrix[1][1] = tmpMatrix[1];
        theMatrix[2][1] = tmpMatrix[2];
        tmpMatrix = getXYZTag(ICC_Profile.icSigBlueColorantTag);
        theMatrix[0][2] = tmpMatrix[0];
        theMatrix[1][2] = tmpMatrix[1];
        theMatrix[2][2] = tmpMatrix[2];
        return theMatrix;
    }

    /**
     * 返回表示特定组件的色调再现曲线（TRC）的伽马值。组件参数
     * 必须是 REDCOMPONENT、GREENCOMPONENT 或 BLUECOMPONENT 之一。
     * <p>
     * 如果配置文件
     * 将对应组件的 TRC 表示为表格而不是单个伽马值，则会抛出异常。
     * 在这种情况下，可以通过 {@link #getTRC(int)} 方法获取实际的表格。
     * 使用伽马值时，线性组件（R、G 或 B）的计算如下：
     * <pre>
     *
     * &nbsp;                                         gamma
     * &nbsp;        linearComponent = deviceComponent
     *
     *</pre>
     * @param component 代表要检索的 TRC 的 <CODE>ICC_ProfileRGB</CODE> 常量
     * @return 作为 float 的伽马值。
     * @exception ProfileDataException 如果配置文件未将对应的 TRC 指定为单个伽马值。
     */
    public float getGamma(int component) {
    float theGamma;
    int theSignature;

        switch (component) {
        case REDCOMPONENT:
            theSignature = ICC_Profile.icSigRedTRCTag;
            break;

        case GREENCOMPONENT:
            theSignature = ICC_Profile.icSigGreenTRCTag;
            break;

        case BLUECOMPONENT:
            theSignature = ICC_Profile.icSigBlueTRCTag;
            break;

        default:
            throw new IllegalArgumentException("必须是 Red、Green 或 Blue");
        }

        theGamma = super.getGamma(theSignature);

        return theGamma;
    }

    /**
     * 返回特定组件的 TRC 作为数组。组件必须是 <code>REDCOMPONENT</code>、
     * <code>GREENCOMPONENT</code> 或 <code>BLUECOMPONENT</code> 之一。
     * 否则，返回的数组
     * 表示一个查找表，其中输入组件值的概念范围为 [0.0, 1.0]。值 0.0 映射
     * 到数组索引 0，值 1.0 映射到数组索引 length-1。
     * 可能会使用插值来生成不完全映射到数组中索引的输入值的输出值。
     * 输出值也线性映射到范围 [0.0, 1.0]。值 0.0 由数组值 0x0000 表示，
     * 值 1.0 由 0xFFFF 表示。换句话说，这些值实际上是无符号
     * <code>short</code> 值，即使它们返回在一个 <code>short</code> 数组中。
     *
     * 如果配置文件将对应的 TRC 指定为线性（gamma = 1.0）或简单的伽马值，此方法
     * 抛出异常。在这种情况下，应使用 {@link #getGamma(int)}
     * 方法获取伽马值。
     *
     * @param component 代表要检索的 TRC 的 <CODE>ICC_ProfileRGB</CODE> 常量：
     * <CODE>REDCOMPONENT</CODE>、<CODE>GREENCOMPONENT</CODE> 或
     * <CODE>BLUECOMPONENT</CODE>。
     *
     * @return 一个表示 TRC 的 short 数组。
     * @exception ProfileDataException 如果配置文件未将对应的 TRC 指定为表格。
     */
    public short[] getTRC(int component) {
    short[] theTRC;
    int theSignature;

        switch (component) {
        case REDCOMPONENT:
            theSignature = ICC_Profile.icSigRedTRCTag;
            break;

        case GREENCOMPONENT:
            theSignature = ICC_Profile.icSigGreenTRCTag;
            break;

        case BLUECOMPONENT:
            theSignature = ICC_Profile.icSigBlueTRCTag;
            break;

        default:
            throw new IllegalArgumentException("必须是 Red、Green 或 Blue");
        }

        theTRC = super.getTRC(theSignature);

        return theTRC;
    }

}
