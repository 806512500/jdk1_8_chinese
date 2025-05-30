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

import java.io.SequenceInputStream;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;

/**
 * 该类实现了一个用于读取 GZIP 文件格式压缩数据的流过滤器。
 *
 * @see         InflaterInputStream
 * @author      David Connelly
 *
 */
public
class GZIPInputStream extends InflaterInputStream {
    /**
     * 未压缩数据的 CRC-32。
     */
    protected CRC32 crc = new CRC32();

    /**
     * 指示输入流的结束。
     */
    protected boolean eos;

    private boolean closed = false;

    /**
     * 检查此流是否未被关闭。
     */
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * 创建具有指定缓冲区大小的新输入流。
     * @param in 输入流
     * @param size 输入缓冲区大小
     *
     * @exception ZipException 如果发生 GZIP 格式错误或使用的压缩方法不受支持
     * @exception IOException 如果发生 I/O 错误
     * @exception IllegalArgumentException 如果 {@code size <= 0}
     */
    public GZIPInputStream(InputStream in, int size) throws IOException {
        super(in, new Inflater(true), size);
        usesDefaultInflater = true;
        readHeader(in);
    }

    /**
     * 创建具有默认缓冲区大小的新输入流。
     * @param in 输入流
     *
     * @exception ZipException 如果发生 GZIP 格式错误或使用的压缩方法不受支持
     * @exception IOException 如果发生 I/O 错误
     */
    public GZIPInputStream(InputStream in) throws IOException {
        this(in, 512);
    }

    /**
     * 将未压缩的数据读入字节数组。如果 <code>len</code> 不为零，该方法将阻塞直到可以解压缩某些输入；否则，
     * 不读取任何字节并返回 <code>0</code>。
     * @param buf 读取数据的缓冲区
     * @param off 目标数组 <code>b</code> 中的起始偏移量
     * @param len 最大读取字节数
     * @return 实际读取的字节数，或者如果到达压缩输入流的末尾则返回 -1
     *
     * @exception  NullPointerException 如果 <code>buf</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，
     * <code>len</code> 为负数，或 <code>len</code> 大于 <code>buf.length - off</code>
     * @exception ZipException 如果压缩输入数据损坏。
     * @exception IOException 如果发生 I/O 错误。
     *
     */
    public int read(byte[] buf, int off, int len) throws IOException {
        ensureOpen();
        if (eos) {
            return -1;
        }
        int n = super.read(buf, off, len);
        if (n == -1) {
            if (readTrailer())
                eos = true;
            else
                return this.read(buf, off, len);
        } else {
            crc.update(buf, off, n);
        }
        return n;
    }

    /**
     * 关闭此输入流并释放与此流关联的任何系统资源。
     * @exception IOException 如果发生 I/O 错误
     */
    public void close() throws IOException {
        if (!closed) {
            super.close();
            eos = true;
            closed = true;
        }
    }

    /**
     * GZIP 头部的魔术数字。
     */
    public final static int GZIP_MAGIC = 0x8b1f;

    /*
     * 文件头部标志。
     */
    private final static int FTEXT      = 1;    // 额外文本
    private final static int FHCRC      = 2;    // 头部 CRC
    private final static int FEXTRA     = 4;    // 额外字段
    private final static int FNAME      = 8;    // 文件名
    private final static int FCOMMENT   = 16;   // 文件注释

    /*
     * 读取 GZIP 成员头部并返回此成员头部的总字节数。
     */
    private int readHeader(InputStream this_in) throws IOException {
        CheckedInputStream in = new CheckedInputStream(this_in, crc);
        crc.reset();
        // 检查头部魔术数字
        if (readUShort(in) != GZIP_MAGIC) {
            throw new ZipException("Not in GZIP format");
        }
        // 检查压缩方法
        if (readUByte(in) != 8) {
            throw new ZipException("Unsupported compression method");
        }
        // 读取标志
        int flg = readUByte(in);
        // 跳过 MTIME, XFL, 和 OS 字段
        skipBytes(in, 6);
        int n = 2 + 2 + 6;
        // 跳过可选的额外字段
        if ((flg & FEXTRA) == FEXTRA) {
            int m = readUShort(in);
            skipBytes(in, m);
            n += m + 2;
        }
        // 跳过可选的文件名
        if ((flg & FNAME) == FNAME) {
            do {
                n++;
            } while (readUByte(in) != 0);
        }
        // 跳过可选的文件注释
        if ((flg & FCOMMENT) == FCOMMENT) {
            do {
                n++;
            } while (readUByte(in) != 0);
        }
        // 检查可选的头部 CRC
        if ((flg & FHCRC) == FHCRC) {
            int v = (int)crc.getValue() & 0xffff;
            if (readUShort(in) != v) {
                throw new ZipException("Corrupt GZIP header");
            }
            n += 2;
        }
        crc.reset();
        return n;
    }

    /*
     * 读取 GZIP 成员尾部并返回 true 如果 eos 达到，false 如果有更多（连接的 gzip 数据集）
     */
    private boolean readTrailer() throws IOException {
        InputStream in = this.in;
        int n = inf.getRemaining();
        if (n > 0) {
            in = new SequenceInputStream(
                        new ByteArrayInputStream(buf, len - n, n),
                        new FilterInputStream(in) {
                            public void close() throws IOException {}
                        });
        }
        // 使用从左到右的求值顺序
        if ((readUInt(in) != crc.getValue()) ||
            // rfc1952; ISIZE 是输入大小模 2^32
            (readUInt(in) != (inf.getBytesWritten() & 0xffffffffL)))
            throw new ZipException("Corrupt GZIP trailer");

        // 如果在 "in" 中有更多可用字节或
        // "inf" 中剩余的字节 > 26 字节：
        // this.trailer(8) + next.header.min(10) + next.trailer(8)
        // 尝试连接的情况
        if (this.in.available() > 0 || n > 26) {
            int m = 8;                  // this.trailer
            try {
                m += readHeader(in);    // next.header
            } catch (IOException ze) {
                return true;  // 忽略任何格式错误，不做任何处理
            }
            inf.reset();
            if (n > m)
                inf.setInput(buf, len - n + m, n - m);
            return false;
        }
        return true;
    }

    /*
     * 以 Intel 字节顺序读取无符号整数。
     */
    private long readUInt(InputStream in) throws IOException {
        long s = readUShort(in);
        return ((long)readUShort(in) << 16) | s;
    }

    /*
     * 以 Intel 字节顺序读取无符号短整数。
     */
    private int readUShort(InputStream in) throws IOException {
        int b = readUByte(in);
        return (readUByte(in) << 8) | b;
    }

    /*
     * 读取无符号字节。
     */
    private int readUByte(InputStream in) throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new EOFException();
        }
        if (b < -1 || b > 255) {
            // 报告 this.in，而不是参数 in；参见 read{Header, Trailer}。
            throw new IOException(this.in.getClass().getName()
                + ".read() returned value out of range -1..255: " + b);
        }
        return b;
    }

    private byte[] tmpbuf = new byte[128];

    /*
     * 跳过输入数据的字节，直到所有字节都被跳过。
     * 不假设输入流能够进行寻址。
     */
    private void skipBytes(InputStream in, int n) throws IOException {
        while (n > 0) {
            int len = in.read(tmpbuf, 0, n < tmpbuf.length ? n : tmpbuf.length);
            if (len == -1) {
                throw new EOFException();
            }
            n -= len;
        }
    }
}
