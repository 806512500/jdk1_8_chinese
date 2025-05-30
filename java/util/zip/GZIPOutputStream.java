/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.OutputStream;
import java.io.IOException;

/**
 * 该类实现了用于写入 GZIP 文件格式压缩数据的流过滤器。
 * @author      David Connelly
 *
 */
public
class GZIPOutputStream extends DeflaterOutputStream {
    /**
     * 未压缩数据的 CRC-32。
     */
    protected CRC32 crc = new CRC32();

    /*
     * GZIP 头部的魔数。
     */
    private final static int GZIP_MAGIC = 0x8b1f;

    /*
     * 尾部的大小（以字节为单位）。
     *
     */
    private final static int TRAILER_SIZE = 8;

    /**
     * 创建具有指定缓冲区大小的新输出流。
     *
     * <p>新的输出流实例的创建方式与调用 3 参数构造函数 GZIPOutputStream(out, size, false) 相同。
     *
     * @param out 输出流
     * @param size 输出缓冲区大小
     * @exception IOException 如果发生 I/O 错误。
     * @exception IllegalArgumentException 如果 {@code size <= 0}
     */
    public GZIPOutputStream(OutputStream out, int size) throws IOException {
        this(out, size, false);
    }

    /**
     * 创建具有指定缓冲区大小和刷新模式的新输出流。
     *
     * @param out 输出流
     * @param size 输出缓冲区大小
     * @param syncFlush
     *        如果为 {@code true}，调用此实例的继承自 {@link DeflaterOutputStream#flush() flush()} 方法将使用 {@link Deflater#SYNC_FLUSH} 模式刷新压缩器，然后再刷新输出流；否则仅刷新输出流
     * @exception IOException 如果发生 I/O 错误。
     * @exception IllegalArgumentException 如果 {@code size <= 0}
     *
     * @since 1.7
     */
    public GZIPOutputStream(OutputStream out, int size, boolean syncFlush)
        throws IOException
    {
        super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true),
              size,
              syncFlush);
        usesDefaultDeflater = true;
        writeHeader();
        crc.reset();
    }


    /**
     * 创建具有默认缓冲区大小的新输出流。
     *
     * <p>新的输出流实例的创建方式与调用 2 参数构造函数 GZIPOutputStream(out, false) 相同。
     *
     * @param out 输出流
     * @exception IOException 如果发生 I/O 错误。
     */
    public GZIPOutputStream(OutputStream out) throws IOException {
        this(out, DeflaterOutputStream.DEFAULT_BUF_SIZE, false);
    }

    /**
     * 创建具有默认缓冲区大小和指定刷新模式的新输出流。
     *
     * @param out 输出流
     * @param syncFlush
     *        如果为 {@code true}，调用此实例的继承自 {@link DeflaterOutputStream#flush() flush()} 方法将使用 {@link Deflater#SYNC_FLUSH} 模式刷新压缩器，然后再刷新输出流；否则仅刷新输出流
     *
     * @exception IOException 如果发生 I/O 错误。
     *
     * @since 1.7
     */
    public GZIPOutputStream(OutputStream out, boolean syncFlush)
        throws IOException
    {
        this(out, DeflaterOutputStream.DEFAULT_BUF_SIZE, syncFlush);
    }

    /**
     * 将字节数组写入压缩输出流。此方法会阻塞，直到所有字节都被写入。
     * @param buf 要写入的数据
     * @param off 数据的起始偏移量
     * @param len 数据的长度
     * @exception IOException 如果发生 I/O 错误。
     */
    public synchronized void write(byte[] buf, int off, int len)
        throws IOException
    {
        super.write(buf, off, len);
        crc.update(buf, off, len);
    }

    /**
     * 完成向输出流写入压缩数据，但不关闭底层流。当对同一输出流应用多个过滤器时，使用此方法。
     * @exception IOException 如果发生 I/O 错误
     */
    public void finish() throws IOException {
        if (!def.finished()) {
            try {
                def.finish();
                while (!def.finished()) {
                    int len = def.deflate(buf, 0, buf.length);
                    if (def.finished() && len <= buf.length - TRAILER_SIZE) {
                        // 最后一个压缩器缓冲区。将尾部放在末尾
                        writeTrailer(buf, len);
                        len = len + TRAILER_SIZE;
                        out.write(buf, 0, len);
                        return;
                    }
                    if (len > 0)
                        out.write(buf, 0, len);
                }
                // 如果不能在最后一个压缩器缓冲区的末尾放置尾部，我们单独写入
                byte[] trailer = new byte[TRAILER_SIZE];
                writeTrailer(trailer, 0);
                out.write(trailer);
            } catch (IOException e) {
                if (usesDefaultDeflater)
                    def.end();
                throw e;
            }
        }
    }

    /*
     * 写入 GZIP 成员头部。
     */
    private void writeHeader() throws IOException {
        out.write(new byte[] {
                      (byte) GZIP_MAGIC,        // 魔数（短整型）
                      (byte)(GZIP_MAGIC >> 8),  // 魔数（短整型）
                      Deflater.DEFLATED,        // 压缩方法（CM）
                      0,                        // 标志（FLG）
                      0,                        // 修改时间 MTIME（整型）
                      0,                        // 修改时间 MTIME（整型）
                      0,                        // 修改时间 MTIME（整型）
                      0,                        // 修改时间 MTIME（整型）
                      0,                        // 额外标志（XFLG）
                      0                         // 操作系统（OS）
                  });
    }

    /*
     * 将 GZIP 成员尾部写入字节数组，从给定偏移量开始。
     */
    private void writeTrailer(byte[] buf, int offset) throws IOException {
        writeInt((int)crc.getValue(), buf, offset); // 未压缩数据的 CRC-32
        writeInt(def.getTotalIn(), buf, offset + 4); // 未压缩字节数
    }

    /*
     * 将整数以 Intel 字节顺序写入字节数组，从给定偏移量开始。
     */
    private void writeInt(int i, byte[] buf, int offset) throws IOException {
        writeShort(i & 0xffff, buf, offset);
        writeShort((i >> 16) & 0xffff, buf, offset + 2);
    }

    /*
     * 将短整数以 Intel 字节顺序写入字节数组，从给定偏移量开始
     */
    private void writeShort(int s, byte[] buf, int offset) throws IOException {
        buf[offset] = (byte)(s & 0xff);
        buf[offset + 1] = (byte)((s >> 8) & 0xff);
    }
}
