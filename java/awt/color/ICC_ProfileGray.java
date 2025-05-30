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
 * ICC_Profile 类的子类，表示符合以下条件的配置文件：配置文件的颜色空间类型为 TYPE_GRAY，并且配置文件包含 grayTRCTag 和 mediaWhitePointTag 标签。这种类型的配置文件示例包括单色输入配置文件、单色显示配置文件和单色输出配置文件。当满足上述条件时，ICC_Profile 类中的 getInstance 方法将返回一个 ICC_ProfileGray 对象。此类的优点是它提供了一个查找表，Java 或本机方法在某些情况下可以直接使用该查找表来优化颜色转换。
 * <p>
 * 从 GRAY 设备配置文件颜色空间转换到 CIEXYZ 配置文件连接空间时，设备灰度分量通过色调再现曲线 (TRC) 查找表进行转换。结果被视为 PCS 的无色分量。
<pre>

&nbsp;               PCSY = grayTRC[deviceGray]

</pre>
 * 逆变换通过将 PCS Y 分量通过 grayTRC 的逆变换转换为设备 Gray 来完成。
 */



public class ICC_ProfileGray
extends ICC_Profile {

    static final long serialVersionUID = -1124721290732002649L;

    /**
     * 从 CMM ID 构造一个新的 ICC_ProfileGray。
     */
    ICC_ProfileGray(Profile p) {
        super(p);
    }

    /**
     * 从 ProfileDeferralInfo 对象构造一个新的 ICC_ProfileGray。
     */
    ICC_ProfileGray(ProfileDeferralInfo pdi) {
        super(pdi);
    }


    /**
     * 返回一个长度为 3 的 float 数组，包含 ICC 配置文件中 mediaWhitePointTag 的 X、Y 和 Z 分量。
     * @return 包含 ICC 配置文件中 mediaWhitePointTag 分量的数组。
     */
    public float[] getMediaWhitePoint() {
        return super.getMediaWhitePoint();
    }


    /**
     * 返回一个表示色调再现曲线 (TRC) 的伽玛值。如果配置文件将 TRC 表示为表格而不是单个伽玛值，则会抛出异常。在这种情况下，可以通过 getTRC() 方法获取实际的表格。当使用伽玛值时，PCS Y 分量的计算方法如下：
<pre>

&nbsp;                         gamma
&nbsp;        PCSY = deviceGray

</pre>
     * @return 伽玛值，类型为 float。
     * @exception ProfileDataException 如果配置文件没有将 TRC 指定为单个伽玛值。
     */
    public float getGamma() {
    float theGamma;

        theGamma = super.getGamma(ICC_Profile.icSigGrayTRCTag);
        return theGamma;
    }

    /**
     * 返回 TRC 作为 short 数组。如果配置文件将 TRC 指定为线性（伽玛 = 1.0）或简单的伽玛值，此方法会抛出异常，应使用 getGamma() 方法获取伽玛值。否则，这里返回的 short 数组表示一个查找表，其中输入的 Gray 值概念上在 [0.0, 1.0] 范围内。值 0.0 映射到数组索引 0，值 1.0 映射到数组索引 length-1。可以使用插值生成不完全映射到数组索引的输入值的输出值。输出值也线性映射到 [0.0, 1.0] 范围。值 0.0 由数组值 0x0000 表示，值 1.0 由 0xFFFF 表示，即这些值实际上是无符号 short 值，尽管它们以 short 数组的形式返回。
     * @return 代表 TRC 的 short 数组。
     * @exception ProfileDataException 如果配置文件没有将 TRC 指定为表格。
     */
    public short[] getTRC() {
    short[]    theTRC;

        theTRC = super.getTRC(ICC_Profile.icSigGrayTRCTag);
        return theTRC;
    }

}
