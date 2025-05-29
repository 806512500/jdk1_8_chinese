
/*
 * Copyright (c) 1996, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.util.zip;

import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import static java.util.zip.ZipConstants64.*;
import static java.util.zip.ZipUtils.*;

/**
 * 该类实现了一个用于读取 ZIP 文件格式文件的输入流过滤器。支持压缩和未压缩的条目。
 *
 * @author      David Connelly
 */
public
class ZipInputStream extends InflaterInputStream implements ZipConstants {
    private ZipEntry entry;
    private int flag;
    private CRC32 crc = new CRC32();
    private long remaining;
    private byte[] tmpbuf = new byte[512];

    private static final int STORED = ZipEntry.STORED;
    private static final int DEFLATED = ZipEntry.DEFLATED;

    private boolean closed = false;
    // 当一个条目到达 EOF 时，此标志设置为 true
    private boolean entryEOF = false;

    private ZipCoder zc;

    /**
     * 检查此流是否已关闭
     */
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * 创建一个新的 ZIP 输入流。
     *
     * <p>使用 UTF-8 {@link java.nio.charset.Charset 字符集} 解码条目名称。
     *
     * @param in 实际的输入流
     */
    public ZipInputStream(InputStream in) {
        this(in, StandardCharsets.UTF_8);
    }

    /**
     * 创建一个新的 ZIP 输入流。
     *
     * @param in 实际的输入流
     *
     * @param charset
     *        用于解码 ZIP 条目名称的 {@linkplain java.nio.charset.Charset 字符集}（如果 ZIP 条目的通用目的位标志中的
     *        <a href="package-summary.html#lang_encoding"> 语言编码位</a> 已设置，则忽略）。
     *
     * @since 1.7
     */
    public ZipInputStream(InputStream in, Charset charset) {
        super(new PushbackInputStream(in, 512), new Inflater(true), 512);
        usesDefaultInflater = true;
        if(in == null) {
            throw new NullPointerException("in is null");
        }
        if (charset == null)
            throw new NullPointerException("charset is null");
        this.zc = ZipCoder.get(charset);
    }

    /**
     * 读取下一个 ZIP 文件条目并将流定位到条目数据的开头。
     * @return 下一个 ZIP 文件条目，如果没有更多条目则返回 null
     * @exception ZipException 如果发生 ZIP 文件错误
     * @exception IOException 如果发生 I/O 错误
     */
    public ZipEntry getNextEntry() throws IOException {
        ensureOpen();
        if (entry != null) {
            closeEntry();
        }
        crc.reset();
        inf.reset();
        if ((entry = readLOC()) == null) {
            return null;
        }
        if (entry.method == STORED) {
            remaining = entry.size;
        }
        entryEOF = false;
        return entry;
    }

    /**
     * 关闭当前 ZIP 条目并将流定位到读取下一个条目的位置。
     * @exception ZipException 如果发生 ZIP 文件错误
     * @exception IOException 如果发生 I/O 错误
     */
    public void closeEntry() throws IOException {
        ensureOpen();
        while (read(tmpbuf, 0, tmpbuf.length) != -1) ;
        entryEOF = true;
    }

    /**
     * 在当前条目数据到达 EOF 后返回 0，否则始终返回 1。
     * <p>
     * 程序不应依赖此方法返回在不阻塞的情况下可以读取的实际字节数。
     *
     * @return 在当前条目到达 EOF 前返回 1，到达 EOF 后返回 0。
     * @exception  IOException  如果发生 I/O 错误。
     *
     */
    public int available() throws IOException {
        ensureOpen();
        if (entryEOF) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * 从当前 ZIP 条目读取到字节数组中。
     * 如果 <code>len</code> 不为零，该方法会阻塞直到有输入可用；否则，不读取任何字节并返回 <code>0</code>。
     * @param b 读取数据的缓冲区
     * @param off 目标数组 <code>b</code> 中的起始偏移量
     * @param len 最大读取字节数
     * @return 实际读取的字节数，或如果到达条目的末尾则返回 -1
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，<code>len</code> 为负数，或 <code>len</code> 大于
     * <code>b.length - off</code>
     * @exception ZipException 如果发生 ZIP 文件错误
     * @exception IOException 如果发生 I/O 错误
     */
    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        if (entry == null) {
            return -1;
        }
        switch (entry.method) {
        case DEFLATED:
            len = super.read(b, off, len);
            if (len == -1) {
                readEnd(entry);
                entryEOF = true;
                entry = null;
            } else {
                crc.update(b, off, len);
            }
            return len;
        case STORED:
            if (remaining <= 0) {
                entryEOF = true;
                entry = null;
                return -1;
            }
            if (len > remaining) {
                len = (int)remaining;
            }
            len = in.read(b, off, len);
            if (len == -1) {
                throw new ZipException("unexpected EOF");
            }
            crc.update(b, off, len);
            remaining -= len;
            if (remaining == 0 && entry.crc != crc.getValue()) {
                throw new ZipException(
                    "invalid entry CRC (expected 0x" + Long.toHexString(entry.crc) +
                    " but got 0x" + Long.toHexString(crc.getValue()) + ")");
            }
            return len;
        default:
            throw new ZipException("invalid compression method");
        }
    }


                /**
     * 在当前 ZIP 条目中跳过指定数量的字节。
     * @param n 要跳过的字节数
     * @return 实际跳过的字节数
     * @exception ZipException 如果发生 ZIP 文件错误
     * @exception IOException 如果发生 I/O 错误
     * @exception IllegalArgumentException 如果 {@code n < 0}
     */
    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("negative skip length");
        }
        ensureOpen();
        int max = (int)Math.min(n, Integer.MAX_VALUE);
        int total = 0;
        while (total < max) {
            int len = max - total;
            if (len > tmpbuf.length) {
                len = tmpbuf.length;
            }
            len = read(tmpbuf, 0, len);
            if (len == -1) {
                entryEOF = true;
                break;
            }
            total += len;
        }
        return total;
    }

    /**
     * 关闭此输入流并释放与流关联的任何系统资源。
     * @exception IOException 如果发生 I/O 错误
     */
    public void close() throws IOException {
        if (!closed) {
            super.close();
            closed = true;
        }
    }

    private byte[] b = new byte[256];

    /*
     * 读取下一个条目的本地文件（LOC）头。
     */
    private ZipEntry readLOC() throws IOException {
        try {
            readFully(tmpbuf, 0, LOCHDR);
        } catch (EOFException e) {
            return null;
        }
        if (get32(tmpbuf, 0) != LOCSIG) {
            return null;
        }
        // 首先获取标志，我们需要检查 EFS。
        flag = get16(tmpbuf, LOCFLG);
        // 获取条目名称并首先创建 ZipEntry
        int len = get16(tmpbuf, LOCNAM);
        int blen = b.length;
        if (len > blen) {
            do {
                blen = blen * 2;
            } while (len > blen);
            b = new byte[blen];
        }
        readFully(b, 0, len);
        // 如果 EFS 位为 ON，即使 cs 不是 UTF-8，也强制使用 UTF-8
        ZipEntry e = createZipEntry(((flag & EFS) != 0)
                                    ? zc.toStringUTF8(b, len)
                                    : zc.toString(b, len));
        // 现在获取条目的其余字段
        if ((flag & 1) == 1) {
            throw new ZipException("不支持加密的 ZIP 条目");
        }
        e.method = get16(tmpbuf, LOCHOW);
        e.xdostime = get32(tmpbuf, LOCTIM);
        if ((flag & 8) == 8) {
            /* "数据描述符"存在 */
            if (e.method != DEFLATED) {
                throw new ZipException(
                        "只有 DEFLATED 条目可以有 EXT 描述符");
            }
        } else {
            e.crc = get32(tmpbuf, LOCCRC);
            e.csize = get32(tmpbuf, LOCSIZ);
            e.size = get32(tmpbuf, LOCLEN);
        }
        len = get16(tmpbuf, LOCEXT);
        if (len > 0) {
            byte[] extra = new byte[len];
            readFully(extra, 0, len);
            e.setExtra0(extra,
                        e.csize == ZIP64_MAGICVAL || e.size == ZIP64_MAGICVAL);
        }
        return e;
    }

    /**
     * 为指定的条目名称创建一个新的 <code>ZipEntry</code> 对象。
     *
     * @param name ZIP 文件条目名称
     * @return 刚创建的 ZipEntry
     */
    protected ZipEntry createZipEntry(String name) {
        return new ZipEntry(name);
    }

    /*
     * 读取已解压条目的结尾以及如果存在的话读取 EXT 描述符。
     */
    private void readEnd(ZipEntry e) throws IOException {
        int n = inf.getRemaining();
        if (n > 0) {
            ((PushbackInputStream)in).unread(buf, len - n, n);
        }
        if ((flag & 8) == 8) {
            /* "数据描述符"存在 */
            if (inf.getBytesWritten() > ZIP64_MAGICVAL ||
                inf.getBytesRead() > ZIP64_MAGICVAL) {
                // ZIP64 格式
                readFully(tmpbuf, 0, ZIP64_EXTHDR);
                long sig = get32(tmpbuf, 0);
                if (sig != EXTSIG) { // 没有 EXTSIG 存在
                    e.crc = sig;
                    e.csize = get64(tmpbuf, ZIP64_EXTSIZ - ZIP64_EXTCRC);
                    e.size = get64(tmpbuf, ZIP64_EXTLEN - ZIP64_EXTCRC);
                    ((PushbackInputStream)in).unread(
                        tmpbuf, ZIP64_EXTHDR - ZIP64_EXTCRC - 1, ZIP64_EXTCRC);
                } else {
                    e.crc = get32(tmpbuf, ZIP64_EXTCRC);
                    e.csize = get64(tmpbuf, ZIP64_EXTSIZ);
                    e.size = get64(tmpbuf, ZIP64_EXTLEN);
                }
            } else {
                readFully(tmpbuf, 0, EXTHDR);
                long sig = get32(tmpbuf, 0);
                if (sig != EXTSIG) { // 没有 EXTSIG 存在
                    e.crc = sig;
                    e.csize = get32(tmpbuf, EXTSIZ - EXTCRC);
                    e.size = get32(tmpbuf, EXTLEN - EXTCRC);
                    ((PushbackInputStream)in).unread(
                                               tmpbuf, EXTHDR - EXTCRC - 1, EXTCRC);
                } else {
                    e.crc = get32(tmpbuf, EXTCRC);
                    e.csize = get32(tmpbuf, EXTSIZ);
                    e.size = get32(tmpbuf, EXTLEN);
                }
            }
        }
        if (e.size != inf.getBytesWritten()) {
            throw new ZipException(
                "无效的条目大小（预期 " + e.size +
                " 但实际为 " + inf.getBytesWritten() + " 字节）");
        }
        if (e.csize != inf.getBytesRead()) {
            throw new ZipException(
                "无效的条目压缩大小（预期 " + e.csize +
                " 但实际为 " + inf.getBytesRead() + " 字节）");
        }
        if (e.crc != crc.getValue()) {
            throw new ZipException(
                "无效的条目 CRC（预期 0x" + Long.toHexString(e.crc) +
                " 但实际为 0x" + Long.toHexString(crc.getValue()) + "）");
        }
    }

    /*
     * 读取字节，直到所有字节都读取完毕。
     */
    private void readFully(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int n = in.read(b, off, len);
            if (n == -1) {
                throw new EOFException();
            }
            off += n;
            len -= n;
        }
    }

}
