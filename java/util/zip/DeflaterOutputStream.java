/*
 * Copyright (c) 1996, 2022, Oracle and/or its affiliates. All rights reserved.
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

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * 该类实现了一个用于压缩数据的“deflate”压缩格式的输出流过滤器。它也是其他类型压缩过滤器（如 GZIPOutputStream）的基础。
 *
 * @see         Deflater
 * @author      David Connelly
 */
public
class DeflaterOutputStream extends FilterOutputStream {

    /*
     * 输出缓冲区的默认大小
     */
    static final int DEFAULT_BUF_SIZE = 512;

    /*
     * 当使用 Deflater.SYNC_FLUSH 或 Deflater.FULL_FLUSH 调用 Deflater.deflate() 时，
     * 调用者应确保缓冲区的大小大于 6。这一要求来自底层的 zlib 库，其在 zlib.h 中声明：
     * "如果 deflate 返回 avail_out == 0，必须再次调用此函数，使用相同的 flush 参数和更多的输出空间（更新的 avail_out），
     * 直到 flush 完成（deflate 返回非零的 avail_out）。在 Z_FULL_FLUSH 或 Z_SYNC_FLUSH 的情况下，
     * 确保在 flush 标记开始时 avail_out 大于六，以避免在 avail_out == 0 时再次调用 deflate() 时重复输出 flush 标记。"
     */
    private static final int SYNC_FLUSH_MIN_BUF_SIZE = 7;

    /**
     * 该流的压缩器。
     */
    protected Deflater def;

    /**
     * 用于写入压缩数据的输出缓冲区。
     */
    protected byte[] buf;

    /**
     * 表示流是否已关闭。
     */

    private boolean closed = false;

    private final boolean syncFlush;

    /**
     * 创建一个具有指定压缩器、缓冲区大小和刷新模式的新输出流。
     *
     * @param out 输出流
     * @param def 压缩器（“deflater”）
     * @param size 输出缓冲区大小
     * @param syncFlush
     *        如果为 {@code true}，则此实例的 {@link #flush()} 方法在刷新输出流之前以
     *        {@link Deflater#SYNC_FLUSH} 模式刷新压缩器，否则仅刷新输出流
     *
     * @throws IllegalArgumentException 如果 {@code size <= 0}
     *
     * @since 1.7
     */
    public DeflaterOutputStream(OutputStream out,
                                Deflater def,
                                int size,
                                boolean syncFlush) {
        super(out);
        if (out == null || def == null) {
            throw new NullPointerException();
        } else if (size <= 0) {
            throw new IllegalArgumentException("buffer size <= 0");
        }
        this.def = def;
        this.buf = new byte[size];
        this.syncFlush = syncFlush;
    }


    /**
     * 创建一个具有指定压缩器和缓冲区大小的新输出流。
     *
     * <p>新的输出流实例的创建方式与调用 4 参数构造函数 DeflaterOutputStream(out, def, size, false) 相同。
     *
     * @param out 输出流
     * @param def 压缩器（“deflater”）
     * @param size 输出缓冲区大小
     * @exception IllegalArgumentException 如果 {@code size <= 0}
     */
    public DeflaterOutputStream(OutputStream out, Deflater def, int size) {
        this(out, def, size, false);
    }

    /**
     * 创建一个具有指定压缩器、刷新模式和默认缓冲区大小的新输出流。
     *
     * @param out 输出流
     * @param def 压缩器（“deflater”）
     * @param syncFlush
     *        如果为 {@code true}，则此实例的 {@link #flush()} 方法在刷新输出流之前以
     *        {@link Deflater#SYNC_FLUSH} 模式刷新压缩器，否则仅刷新输出流
     *
     * @since 1.7
     */
    public DeflaterOutputStream(OutputStream out,
                                Deflater def,
                                boolean syncFlush) {
        this(out, def, DEFAULT_BUF_SIZE, syncFlush);
    }


    /**
     * 创建一个具有指定压缩器和默认缓冲区大小的新输出流。
     *
     * <p>新的输出流实例的创建方式与调用 3 参数构造函数 DeflaterOutputStream(out, def, false) 相同。
     *
     * @param out 输出流
     * @param def 压缩器（“deflater”）
     */
    public DeflaterOutputStream(OutputStream out, Deflater def) {
        this(out, def, DEFAULT_BUF_SIZE, false);
    }

    boolean usesDefaultDeflater = false;


    /**
     * 创建一个具有默认压缩器、默认缓冲区大小和指定刷新模式的新输出流。
     *
     * @param out 输出流
     * @param syncFlush
     *        如果为 {@code true}，则此实例的 {@link #flush()} 方法在刷新输出流之前以
     *        {@link Deflater#SYNC_FLUSH} 模式刷新压缩器，否则仅刷新输出流
     *
     * @since 1.7
     */
    public DeflaterOutputStream(OutputStream out, boolean syncFlush) {
        this(out, new Deflater(), DEFAULT_BUF_SIZE, syncFlush);
        usesDefaultDeflater = true;
    }

    /**
     * 创建一个具有默认压缩器和缓冲区大小的新输出流。
     *
     * <p>新的输出流实例的创建方式与调用 2 参数构造函数 DeflaterOutputStream(out, false) 相同。
     *
     * @param out 输出流
     */
    public DeflaterOutputStream(OutputStream out) {
        this(out, false);
        usesDefaultDeflater = true;
    }

    /**
     * 将一个字节写入压缩输出流。此方法将阻塞，直到字节可以被写入。
     * @param b 要写入的字节
     * @exception IOException 如果发生 I/O 错误
     */
    @Override
    public void write(int b) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte)(b & 0xff);
        write(buf, 0, 1);
    }

    /**
     * 将一个字节数组写入压缩输出流。此方法将阻塞，直到所有字节都被写入。
     * @param b 要写入的数据
     * @param off 数据的起始偏移量
     * @param len 数据的长度
     * @exception IOException 如果发生 I/O 错误
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (def.finished()) {
            throw new IOException("write beyond end of stream");
        }
        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        if (!def.finished()) {
            def.setInput(b, off, len);
            while (!def.needsInput()) {
                deflate();
            }
        }
    }

    /**
     * 完成向输出流写入压缩数据，但不关闭底层流。当对同一输出流应用多个过滤器时，使用此方法。
     * @exception IOException 如果发生 I/O 错误
     */
    public void finish() throws IOException {
        if (!def.finished()) {
            try{
                def.finish();
                while (!def.finished()) {
                    deflate();
                }
            } catch(IOException e) {
                if (usesDefaultDeflater)
                    def.end();
                throw e;
            }
        }
    }

    /**
     * 将剩余的压缩数据写入输出流并关闭底层流。
     * @exception IOException 如果发生 I/O 错误
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            try {
                finish();
            } finally {
                if (usesDefaultDeflater)
                    def.end();
            }
            out.close();
            closed = true;
        }
    }

    /**
     * 将下一个压缩数据块写入输出流。
     * @throws IOException 如果发生 I/O 错误
     */
    protected void deflate() throws IOException {
        int len = def.deflate(buf, 0, buf.length);
        if (len > 0) {
            out.write(buf, 0, len);
        }
    }

    /**
     * 刷新压缩输出流。
     *
     * 如果在构造此压缩输出流时 {@link #DeflaterOutputStream(OutputStream, Deflater, int, boolean)
     * syncFlush} 为 {@code true}，此方法首先以 {@link Deflater#SYNC_FLUSH} 模式刷新底层 {@code 压缩器}，
     * 以强制所有待处理的数据被刷新到输出流，然后刷新输出流。否则，此方法仅刷新输出流而不刷新 {@code 压缩器}。
     *
     * @throws IOException 如果发生 I/O 错误
     *
     * @since 1.7
     */
    @Override
    public void flush() throws IOException {
        if (syncFlush && !def.finished()) {
            int len = 0;
            // 对于 SYNC_FLUSH，Deflater.deflate() 期望调用者使用长度大于 6 的缓冲区，以避免每次调用时
            // 反复输出 flush 标记（5 字节）到输出缓冲区。
            final byte[] flushBuf = buf.length < SYNC_FLUSH_MIN_BUF_SIZE
                    ? new byte[DEFAULT_BUF_SIZE]
                    : buf;
            while ((len = def.deflate(flushBuf, 0, flushBuf.length, Deflater.SYNC_FLUSH)) > 0) {
                out.write(flushBuf, 0, len);
                if (len < flushBuf.length)
                    break;
            }
        }
        out.flush();
    }
}
