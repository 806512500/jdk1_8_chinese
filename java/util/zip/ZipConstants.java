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
 * 此接口定义了用于操作 ZIP 文件的类中使用的常量。
 *
 * @author      David Connelly
 */
interface ZipConstants {
    /*
     * 头部签名
     */
    static long LOCSIG = 0x04034b50L;   // "PK\003\004"
    static long EXTSIG = 0x08074b50L;   // "PK\007\008"
    static long CENSIG = 0x02014b50L;   // "PK\001\002"
    static long ENDSIG = 0x06054b50L;   // "PK\005\006"

    /*
     * 头部大小（包括签名）以字节为单位
     */
    static final int LOCHDR = 30;       // LOC 头部大小
    static final int EXTHDR = 16;       // EXT 头部大小
    static final int CENHDR = 46;       // CEN 头部大小
    static final int ENDHDR = 22;       // END 头部大小

    /*
     * 本地文件（LOC）头部字段偏移量
     */
    static final int LOCVER = 4;        // 提取所需版本
    static final int LOCFLG = 6;        // 通用目的位标志
    static final int LOCHOW = 8;        // 压缩方法
    static final int LOCTIM = 10;       // 修改时间
    static final int LOCCRC = 14;       // 未压缩文件的 crc-32 值
    static final int LOCSIZ = 18;       // 压缩大小
    static final int LOCLEN = 22;       // 未压缩大小
    static final int LOCNAM = 26;       // 文件名长度
    static final int LOCEXT = 28;       // 额外字段长度

    /*
     * 额外本地（EXT）头部字段偏移量
     */
    static final int EXTCRC = 4;        // 未压缩文件的 crc-32 值
    static final int EXTSIZ = 8;        // 压缩大小
    static final int EXTLEN = 12;       // 未压缩大小

    /*
     * 中央目录（CEN）头部字段偏移量
     */
    static final int CENVEM = 4;        // 创建版本
    static final int CENVER = 6;        // 提取所需版本
    static final int CENFLG = 8;        // 加密、解密标志
    static final int CENHOW = 10;       // 压缩方法
    static final int CENTIM = 12;       // 修改时间
    static final int CENCRC = 16;       // 未压缩文件的 crc-32 值
    static final int CENSIZ = 20;       // 压缩大小
    static final int CENLEN = 24;       // 未压缩大小
    static final int CENNAM = 28;       // 文件名长度
    static final int CENEXT = 30;       // 额外字段长度
    static final int CENCOM = 32;       // 注释长度
    static final int CENDSK = 34;       // 起始磁盘号
    static final int CENATT = 36;       // 内部文件属性
    static final int CENATX = 38;       // 外部文件属性
    static final int CENOFF = 42;       // LOC 头部偏移量

    /*
     * 中央目录结束（END）头部字段偏移量
     */
    static final int ENDSUB = 8;        // 本磁盘上的条目数
    static final int ENDTOT = 10;       // 条目总数
    static final int ENDSIZ = 12;       // 中央目录大小（以字节为单位）
    static final int ENDOFF = 16;       // 第一个 CEN 头部的偏移量
    static final int ENDCOM = 20;       // ZIP 文件注释长度
}
