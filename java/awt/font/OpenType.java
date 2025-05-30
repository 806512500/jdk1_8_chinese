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

package java.awt.font;

/**
 * <code>OpenType</code> 接口表示 OpenType 和 TrueType 字体。此接口使得可以从字体中获取 <i>sfnt</i> 表。特定的
 * <code>Font</code> 对象可以实现此接口。
 * <p>
 * 有关 TrueType 和 OpenType 字体的更多信息，请参见 OpenType 规范。
 * ( <a href="http://www.microsoft.com/typography/otspec/">http://www.microsoft.com/typography/otspec/</a> )。
 */
public interface OpenType {

  /* 51 标签类型 */

  /**
   * 字符到字形映射。Open Type 规范中的表标签 "cmap"。
   */
  public final static int       TAG_CMAP        = 0x636d6170;

  /**
   * 字体头。Open Type 规范中的表标签 "head"。
   */
  public final static int       TAG_HEAD        = 0x68656164;

  /**
   * 命名表。Open Type 规范中的表标签 "name"。
   */
  public final static int       TAG_NAME        = 0x6e616d65;

  /**
   * 字形数据。Open Type 规范中的表标签 "glyf"。
   */
  public final static int       TAG_GLYF        = 0x676c7966;

  /**
   * 最大轮廓。Open Type 规范中的表标签 "maxp"。
   */
  public final static int       TAG_MAXP        = 0x6d617870;

  /**
   * CVT 预编程。Open Type 规范中的表标签 "prep"。
   */
  public final static int       TAG_PREP        = 0x70726570;

  /**
   * 水平度量。Open Type 规范中的表标签 "hmtx"。
   */
  public final static int       TAG_HMTX        = 0x686d7478;

  /**
   * 字距调整。Open Type 规范中的表标签 "kern"。
   */
  public final static int       TAG_KERN        = 0x6b65726e;

  /**
   * 水平设备度量。Open Type 规范中的表标签 "hdmx"。
   */
  public final static int       TAG_HDMX        = 0x68646d78;

  /**
   * 索引到位置。Open Type 规范中的表标签 "loca"。
   */
  public final static int       TAG_LOCA        = 0x6c6f6361;

  /**
   * PostScript 信息。Open Type 规范中的表标签 "post"。
   */
  public final static int       TAG_POST        = 0x706f7374;

  /**
   * OS/2 和 Windows 特定度量。Open Type 规范中的表标签 "OS/2"。
   */
  public final static int       TAG_OS2 = 0x4f532f32;

  /**
   * 控制值表。Open Type 规范中的表标签 "cvt "。
   */
  public final static int       TAG_CVT = 0x63767420;

  /**
   * 网格拟合和扫描转换过程。Open Type 规范中的表标签 "gasp"。
   */
  public final static int       TAG_GASP        = 0x67617370;

  /**
   * 垂直设备度量。Open Type 规范中的表标签 "VDMX"。
   */
  public final static int       TAG_VDMX        = 0x56444d58;

  /**
   * 垂直度量。Open Type 规范中的表标签 "vmtx"。
   */
  public final static int       TAG_VMTX        = 0x766d7478;

  /**
   * 垂直度量头。Open Type 规范中的表标签 "vhea"。
   */
  public final static int       TAG_VHEA        = 0x76686561;

  /**
   * 水平度量头。Open Type 规范中的表标签 "hhea"。
   */
  public final static int       TAG_HHEA        = 0x68686561;

  /**
   * Adobe Type 1 字体数据。Open Type 规范中的表标签 "typ1"。
   */
  public final static int       TAG_TYP1        = 0x74797031;

  /**
   * 基线表。Open Type 规范中的表标签 "bsln"。
   */
  public final static int       TAG_BSLN        = 0x62736c6e;

  /**
   * 字形替换。Open Type 规范中的表标签 "GSUB"。
   */
  public final static int       TAG_GSUB        = 0x47535542;

  /**
   * 数字签名。Open Type 规范中的表标签 "DSIG"。
   */
  public final static int       TAG_DSIG        = 0x44534947;

  /**
   * 字体程序。Open Type 规范中的表标签 "fpgm"。
   */
  public final static int       TAG_FPGM        = 0x6670676d;

  /**
   * 字体变体。Open Type 规范中的表标签 "fvar"。
   */
  public final static int       TAG_FVAR        = 0x66766172;

  /**
   * 字形变体。Open Type 规范中的表标签 "gvar"。
   */
  public final static int       TAG_GVAR        = 0x67766172;

  /**
   * 紧凑字体格式（Type1 字体）。Open Type 规范中的表标签 "CFF "。
   */
  public final static int       TAG_CFF = 0x43464620;

  /**
   * 多主数据补充。Open Type 规范中的表标签 "MMSD"。
   */
  public final static int       TAG_MMSD        = 0x4d4d5344;

  /**
   * 多主字体度量。Open Type 规范中的表标签 "MMFX"。
   */
  public final static int       TAG_MMFX        = 0x4d4d4658;

  /**
   * 基线数据。Open Type 规范中的表标签 "BASE"。
   */
  public final static int       TAG_BASE        = 0x42415345;

  /**
   * 字形定义。Open Type 规范中的表标签 "GDEF"。
   */
  public final static int       TAG_GDEF        = 0x47444546;

  /**
   * 字形定位。Open Type 规范中的表标签 "GPOS"。
   */
  public final static int       TAG_GPOS        = 0x47504f53;

  /**
   * 对齐。Open Type 规范中的表标签 "JSTF"。
   */
  public final static int       TAG_JSTF        = 0x4a535446;

  /**
   * 嵌入位图数据。Open Type 规范中的表标签 "EBDT"。
   */
  public final static int       TAG_EBDT        = 0x45424454;

  /**
   * 嵌入位图位置。Open Type 规范中的表标签 "EBLC"。
   */
  public final static int       TAG_EBLC        = 0x45424c43;

  /**
   * 嵌入位图缩放。Open Type 规范中的表标签 "EBSC"。
   */
  public final static int       TAG_EBSC        = 0x45425343;

  /**
   * 线性阈值。Open Type 规范中的表标签 "LTSH"。
   */
  public final static int       TAG_LTSH        = 0x4c545348;

  /**
   * PCL 5 数据。Open Type 规范中的表标签 "PCLT"。
   */
  public final static int       TAG_PCLT        = 0x50434c54;

  /**
   * 重音附件。Open Type 规范中的表标签 "acnt"。
   */
  public final static int       TAG_ACNT        = 0x61636e74;

  /**
   * 轴变体。Open Type 规范中的表标签 "avar"。
   */
  public final static int       TAG_AVAR        = 0x61766172;

  /**
   * 位图数据。Open Type 规范中的表标签 "bdat"。
   */
  public final static int       TAG_BDAT        = 0x62646174;

  /**
   * 位图位置。Open Type 规范中的表标签 "bloc"。
   */
  public final static int       TAG_BLOC        = 0x626c6f63;

  /**
   * CVT 变体。Open Type 规范中的表标签 "cvar"。
   */
  public final static int       TAG_CVAR        = 0x63766172;

  /**
   * 特性名称。Open Type 规范中的表标签 "feat"。
   */
  public final static int       TAG_FEAT        = 0x66656174;

  /**
   * 字体描述符。Open Type 规范中的表标签 "fdsc"。
   */
  public final static int       TAG_FDSC        = 0x66647363;

  /**
   * 字体度量。Open Type 规范中的表标签 "fmtx"。
   */
  public final static int       TAG_FMTX        = 0x666d7478;

  /**
   * 对齐。Open Type 规范中的表标签 "just"。
   */
  public final static int       TAG_JUST        = 0x6a757374;

  /**
   * 连字护理点。Open Type 规范中的表标签 "lcar"。
   */
  public final static int       TAG_LCAR        = 0x6c636172;

  /**
   * 字形变形。Open Type 规范中的表标签 "mort"。
   */
  public final static int       TAG_MORT        = 0x6d6f7274;

  /**
   * 光学边界。Open Type 规范中的表标签 "opbd"。
   */
  public final static int       TAG_OPBD        = 0x6d6f7274;

  /**
   * 字形属性。Open Type 规范中的表标签 "prop"。
   */
  public final static int       TAG_PROP        = 0x70726f70;

  /**
   * 跟踪。Open Type 规范中的表标签 "trak"。
   */
  public final static int       TAG_TRAK        = 0x7472616b;

  /**
   * 返回 <code>OpenType</code> 字体的版本。1.0 表示为 0x00010000。
   * @return <code>OpenType</code> 字体的版本。
   */
  public int getVersion();

  /**
   * 返回指定标签的表作为字节数组。sfnt 表的标签包括像 <i>cmap</i>、<i>name</i> 和 <i>head</i> 等项目。返回的
   * <code>byte</code> 数组是内存中字体数据的副本。
   * @param     sfntTag 32 位整数形式的四字符代码
   * @return 包含与指定标签对应的字体数据的表的 <code>byte</code> 数组。
   */
  public byte[] getFontTable(int sfntTag);

  /**
   * 返回指定标签的表作为字节数组。sfnt 表的标签包括像 <i>cmap</i>、<i>name</i> 和 <i>head</i> 等项目。返回的
   * <code>byte</code> 数组是内存中字体数据的副本。
   * @param     strSfntTag <code>String</code> 形式的四字符代码
   * @return 包含与指定标签对应的字体数据的表的 <code>byte</code> 数组。
   */
  public byte[] getFontTable(String strSfntTag);

  /**
   * 返回指定标签的表的子集作为字节数组。sfnt 表的标签包括像 <i>cmap</i>、<i>name</i> 和 <i>head</i> 等项目。返回的
   * <code>byte</code> 数组是内存中字体数据的副本。
   * @param     sfntTag 32 位整数形式的四字符代码
   * @param     offset 从表中返回的第一个字节的索引
   * @param     count 从表中返回的字节数
   * @return 包含从 <code>offset</code> 字节开始并包含 <code>count</code> 字节的与 <code>sfntTag</code> 对应的表的子集。
   */
  public byte[] getFontTable(int sfntTag, int offset, int count);

  /**
   * 返回指定标签的表的子集作为字节数组。sfnt 表的标签包括像 <i>cmap</i>、<i>name</i> 和 <i>head</i> 等项目。返回的
   * <code>byte</code> 数组是内存中字体数据的副本。
   * @param     strSfntTag <code>String</code> 形式的四字符代码
   * @param     offset 从表中返回的第一个字节的索引
   * @param     count 从表中返回的字节数
   * @return 包含从 <code>offset</code> 字节开始并包含 <code>count</code> 字节的与 <code>strSfntTag</code> 对应的表的子集。
   */
  public byte[] getFontTable(String strSfntTag, int offset, int count);

  /**
   * 返回指定标签的表的大小。sfnt 表的标签包括像 <i>cmap</i>、<i>name</i> 和 <i>head</i> 等项目。
   * @param     sfntTag 32 位整数形式的四字符代码
   * @return 与指定标签对应的表的大小。
   */
  public int getFontTableSize(int sfntTag);

  /**
   * 返回指定标签的表的大小。sfnt 表的标签包括像 <i>cmap</i>、<i>name</i> 和 <i>head</i> 等项目。
   * @param     strSfntTag <code>String</code> 形式的四字符代码
   * @return 与指定标签对应的表的大小。
   */
  public int getFontTableSize(String strSfntTag);

}
