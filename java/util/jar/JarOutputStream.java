/*
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.util.jar;

import java.util.zip.*;
import java.io.*;

/**
 * <code>JarOutputStream</code> 类用于将 JAR 文件的内容写入任何输出流。它扩展了
 * <code>java.util.zip.ZipOutputStream</code> 类，支持写入可选的 <code>Manifest</code> 条目。
 * <code>Manifest</code> 可用于指定有关 JAR 文件及其条目的元信息。
 *
 * @author  David Connelly
 * @see     Manifest
 * @see     java.util.zip.ZipOutputStream
 * @since   1.2
 */
public
class JarOutputStream extends ZipOutputStream {
    private static final int JAR_MAGIC = 0xCAFE;

    /**
     * 使用指定的 <code>Manifest</code> 创建新的 <code>JarOutputStream</code>。
     * 清单作为第一个条目写入输出流。
     *
     * @param out 实际的输出流
     * @param man 可选的 <code>Manifest</code>
     * @exception IOException 如果发生 I/O 错误
     */
    public JarOutputStream(OutputStream out, Manifest man) throws IOException {
        super(out);
        if (man == null) {
            throw new NullPointerException("man");
        }
        ZipEntry e = new ZipEntry(JarFile.MANIFEST_NAME);
        putNextEntry(e);
        man.write(new BufferedOutputStream(this));
        closeEntry();
    }

    /**
     * 创建没有清单的新 <code>JarOutputStream</code>。
     * @param out 实际的输出流
     * @exception IOException 如果发生 I/O 错误
     */
    public JarOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    /**
     * 开始写入新的 JAR 文件条目，并将流定位到条目数据的起始位置。
     * 此方法还将关闭任何先前的条目。如果未为条目指定压缩方法，则使用默认压缩方法。
     * 如果条目没有设置修改时间，则使用当前时间。
     *
     * @param ze 要写入的 ZIP/JAR 条目
     * @exception ZipException 如果发生 ZIP 错误
     * @exception IOException 如果发生 I/O 错误
     */
    public void putNextEntry(ZipEntry ze) throws IOException {
        if (firstEntry) {
            // 确保第一个 JAR 条目的额外字段数据包含 JAR 魔法数字 id。
            byte[] edata = ze.getExtra();
            if (edata == null || !hasMagic(edata)) {
                if (edata == null) {
                    edata = new byte[4];
                } else {
                    // 在现有额外数据前添加魔法数字
                    byte[] tmp = new byte[edata.length + 4];
                    System.arraycopy(edata, 0, tmp, 4, edata.length);
                    edata = tmp;
                }
                set16(edata, 0, JAR_MAGIC); // 额外字段 id
                set16(edata, 2, 0);         // 额外字段大小
                ze.setExtra(edata);
            }
            firstEntry = false;
        }
        super.putNextEntry(ze);
    }

    private boolean firstEntry = true;

    /*
     * 如果指定的字节数组包含 JAR 魔法数字额外字段 id，则返回 true。
     */
    private static boolean hasMagic(byte[] edata) {
        try {
            int i = 0;
            while (i < edata.length) {
                if (get16(edata, i) == JAR_MAGIC) {
                    return true;
                }
                i += get16(edata, i + 2) + 4;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // 无效的额外字段数据
        }
        return false;
    }

    /*
     * 从字节数组中获取指定偏移量的无符号 16 位值。
     * 假设字节顺序为 Intel（小端）。
     */
    private static int get16(byte[] b, int off) {
        return Byte.toUnsignedInt(b[off]) | ( Byte.toUnsignedInt(b[off+1]) << 8);
    }

    /*
     * 在指定偏移量设置 16 位值。假设字节顺序为 Intel（小端）。
     */
    private static void set16(byte[] b, int off, int value) {
        b[off+0] = (byte)value;
        b[off+1] = (byte)(value >> 8);
    }
}
