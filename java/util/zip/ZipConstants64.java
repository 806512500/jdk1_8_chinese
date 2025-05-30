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

package java.util.zip;

/*
 * 该类定义了用于操作 Zip64 文件的类中使用的常量。
 */

class ZipConstants64 {

    /*
     * ZIP64 常量
     */
    static final long ZIP64_ENDSIG = 0x06064b50L;  // "PK\006\006"
    static final long ZIP64_LOCSIG = 0x07064b50L;  // "PK\006\007"
    static final int  ZIP64_ENDHDR = 56;           // ZIP64 结束头大小
    static final int  ZIP64_LOCHDR = 20;           // ZIP64 结束定位头大小
    static final int  ZIP64_EXTHDR = 24;           // 扩展头大小
    static final int  ZIP64_EXTID  = 0x0001;       // 扩展字段 Zip64 头标识

    static final int  ZIP64_MAGICCOUNT = 0xFFFF;
    static final long ZIP64_MAGICVAL = 0xFFFFFFFFL;

    /*
     * Zip64 中央目录结束 (END) 头字段偏移量
     */
    static final int  ZIP64_ENDLEN = 4;       // 中央目录结束大小
    static final int  ZIP64_ENDVEM = 12;      // 创建版本
    static final int  ZIP64_ENDVER = 14;      // 提取所需版本
    static final int  ZIP64_ENDNMD = 16;      // 该磁盘编号
    static final int  ZIP64_ENDDSK = 20;      // 开始磁盘编号
    static final int  ZIP64_ENDTOD = 24;      // 该磁盘上的条目总数
    static final int  ZIP64_ENDTOT = 32;      // 条目总数
    static final int  ZIP64_ENDSIZ = 40;      // 中央目录大小（字节）
    static final int  ZIP64_ENDOFF = 48;      // 第一个 CEN 头的偏移量
    static final int  ZIP64_ENDEXT = 56;      // ZIP64 扩展数据扇区

    /*
     * Zip64 中央目录结束定位字段偏移量
     */
    static final int  ZIP64_LOCDSK = 4;       // 开始磁盘编号
    static final int  ZIP64_LOCOFF = 8;       // ZIP64 结束的偏移量
    static final int  ZIP64_LOCTOT = 16;      // 磁盘总数

    /*
     * Zip64 扩展本地 (EXT) 头字段偏移量
     */
    static final int  ZIP64_EXTCRC = 4;       // 未压缩文件的 CRC-32 值
    static final int  ZIP64_EXTSIZ = 8;       // 压缩大小，8 字节
    static final int  ZIP64_EXTLEN = 16;      // 未压缩大小，8 字节

    /*
     * 语言编码标志 EFS
     */
    static final int EFS = 0x800;       // 如果设置了该位，则该文件的文件名和注释字段必须使用 UTF-8 编码。

    /*
     * 以下常量在此处定义（而不是在 ZipConstants 中定义）
     * 以避免作为 ZipFile、ZipEntry、ZipInputStream 和 ZipOutputstream 的公共字段暴露。
     */

    /*
     * 扩展字段头标识
     */
    static final int  EXTID_ZIP64 = 0x0001;    // Zip64
    static final int  EXTID_NTFS  = 0x000a;    // NTFS
    static final int  EXTID_UNIX  = 0x000d;    // UNIX
    static final int  EXTID_EXTT  = 0x5455;    // Info-ZIP 扩展时间戳

    /*
     * EXTT 时间戳标志
     */
    static final int  EXTT_FLAG_LMT = 0x1;       // 最后修改时间
    static final int  EXTT_FLAG_LAT = 0x2;       // 最后访问时间
    static final int  EXTT_FLAT_CT  = 0x4;       // 创建时间

    private ZipConstants64() {}
}
