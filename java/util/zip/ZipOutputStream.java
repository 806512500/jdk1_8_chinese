
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

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Vector;
import java.util.HashSet;
import static java.util.zip.ZipConstants64.*;
import static java.util.zip.ZipUtils.*;

/**
 * 该类实现了用于写入 ZIP 文件格式文件的输出流过滤器。包括对压缩和未压缩条目的支持。
 *
 * @author      David Connelly
 */
public
class ZipOutputStream extends DeflaterOutputStream implements ZipConstants {

    /**
     * 是否在包含超过 64k 条目的 ZIP 文件中使用 ZIP64。直到 ZIP64 支持在 ZIP 实现中普遍可用，此
     * 系统属性允许创建可以被旧版 ZIP 实现读取的 ZIP 文件，这些旧版 ZIP 实现容忍“不正确”的
     * 总条目数字段，例如 JDK 6 中的字段，甚至 JDK 7 中的一些字段。
     */
    private static final boolean inhibitZip64 =
        Boolean.parseBoolean(
            java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction(
                    "jdk.util.zip.inhibitZip64", "false")));

    private static class XEntry {
        final ZipEntry entry;
        final long offset;
        public XEntry(ZipEntry entry, long offset) {
            this.entry = entry;
            this.offset = offset;
        }
    }

    private XEntry current;
    private Vector<XEntry> xentries = new Vector<>();
    private HashSet<String> names = new HashSet<>();
    private CRC32 crc = new CRC32();
    private long written = 0;
    private long locoff = 0;
    private byte[] comment;
    private int method = DEFLATED;
    private boolean finished;

    private boolean closed = false;

    private final ZipCoder zc;

    private static int version(ZipEntry e) throws ZipException {
        switch (e.method) {
        case DEFLATED: return 20;
        case STORED:   return 10;
        default: throw new ZipException("不支持的压缩方法");
        }
    }

    /**
     * 检查此流是否未被关闭。
     */
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("流已关闭");
        }
    }
    /**
     * 未压缩（STORED）条目的压缩方法。
     */
    public static final int STORED = ZipEntry.STORED;

    /**
     * 压缩（DEFLATED）条目的压缩方法。
     */
    public static final int DEFLATED = ZipEntry.DEFLATED;

    /**
     * 创建一个新的 ZIP 输出流。
     *
     * <p>使用 UTF-8 {@link java.nio.charset.Charset 字符集} 编码条目名称和注释。
     *
     * @param out 实际的输出流
     */
    public ZipOutputStream(OutputStream out) {
        this(out, StandardCharsets.UTF_8);
    }

    /**
     * 创建一个新的 ZIP 输出流。
     *
     * @param out 实际的输出流
     *
     * @param charset 用于编码条目名称和注释的 {@linkplain java.nio.charset.Charset 字符集}
     *
     * @since 1.7
     */
    public ZipOutputStream(OutputStream out, Charset charset) {
        super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        if (charset == null)
            throw new NullPointerException("字符集为空");
        this.zc = ZipCoder.get(charset);
        usesDefaultDeflater = true;
    }

    /**
     * 设置 ZIP 文件注释。
     * @param comment 注释字符串
     * @exception IllegalArgumentException 如果指定的 ZIP 文件注释长度大于 0xFFFF 字节
     */
    public void setComment(String comment) {
        if (comment != null) {
            this.comment = zc.getBytes(comment);
            if (this.comment.length > 0xffff)
                throw new IllegalArgumentException("ZIP 文件注释太长。");
        }
    }

    /**
     * 设置后续条目的默认压缩方法。如果未为单个 ZIP 文件条目指定压缩方法，则将使用此默认方法，初始设置为 DEFLATED。
     * @param method 默认压缩方法
     * @exception IllegalArgumentException 如果指定的压缩方法无效
     */
    public void setMethod(int method) {
        if (method != DEFLATED && method != STORED) {
            throw new IllegalArgumentException("无效的压缩方法");
        }
        this.method = method;
    }

    /**
     * 设置后续 DEFLATED 条目的压缩级别。默认设置为 DEFAULT_COMPRESSION。
     * @param level 压缩级别（0-9）
     * @exception IllegalArgumentException 如果压缩级别无效
     */
    public void setLevel(int level) {
        def.setLevel(level);
    }

    /**
     * 开始写入新的 ZIP 文件条目，并将流定位到条目数据的起始位置。如果当前条目仍处于活动状态，则关闭当前条目。
     * 如果未为条目指定压缩方法，则将使用默认压缩方法，如果条目没有设置修改时间，则将使用当前时间。
     * @param e 要写入的 ZIP 条目
     * @exception ZipException 如果发生 ZIP 格式错误
     * @exception IOException 如果发生 I/O 错误
     */
    public void putNextEntry(ZipEntry e) throws IOException {
        ensureOpen();
        if (current != null) {
            closeEntry();       // 关闭前一个条目
        }
        if (e.xdostime == -1) {
            // 默认情况下，不在额外数据中使用扩展时间戳。
            e.setTime(System.currentTimeMillis());
        }
        if (e.method == -1) {
            e.method = method;  // 使用默认方法
        }
        // 在 LOC 头中存储大小、压缩大小和 CRC-32
        e.flag = 0;
        switch (e.method) {
        case DEFLATED:
            // 在紧随压缩条目数据之后的数据描述符中存储大小、压缩大小和 CRC-32
            if (e.size  == -1 || e.csize == -1 || e.crc   == -1)
                e.flag = 8;

            break;
        case STORED:
            // 对于使用 STORED 压缩方法的条目，必须设置压缩大小、未压缩大小和 CRC-32
            if (e.size == -1) {
                e.size = e.csize;
            } else if (e.csize == -1) {
                e.csize = e.size;
            } else if (e.size != e.csize) {
                throw new ZipException(
                    "STORED 条目中压缩大小不等于未压缩大小");
            }
            if (e.size == -1 || e.crc == -1) {
                throw new ZipException(
                    "STORED 条目缺少大小、压缩大小或 CRC-32");
            }
            break;
        default:
            throw new ZipException("不支持的压缩方法");
        }
        if (! names.add(e.name)) {
            throw new ZipException("重复条目: " + e.name);
        }
        if (zc.isUTF8())
            e.flag |= EFS;
        current = new XEntry(e, written);
        xentries.add(current);
        writeLOC(current);
    }

    /**
     * 关闭当前 ZIP 条目，并将流定位到写入下一个条目的位置。
     * @exception ZipException 如果发生 ZIP 格式错误
     * @exception IOException 如果发生 I/O 错误
     */
    public void closeEntry() throws IOException {
        ensureOpen();
        if (current != null) {
            try {
                ZipEntry e = current.entry;
                switch (e.method) {
                case DEFLATED: {
                        def.finish();
                        while (!def.finished()) {
                            deflate();
                        }
                        if ((e.flag & 8) == 0) {
                            // 验证大小、压缩大小和 CRC-32 设置
                            if (e.size != def.getBytesRead()) {
                                throw new ZipException(
                                    "无效的条目大小（期望 " + e.size +
                                    " 但实际为 " + def.getBytesRead() + " 字节）");
                            }
                            if (e.csize != def.getBytesWritten()) {
                                throw new ZipException(
                                    "无效的条目压缩大小（期望 " +
                                    e.csize + " 但实际为 " + def.getBytesWritten() + " 字节）");
                            }
                            if (e.crc != crc.getValue()) {
                                throw new ZipException(
                                    "无效的条目 CRC-32（期望 0x" +
                                    Long.toHexString(e.crc) + " 但实际为 0x" +
                                    Long.toHexString(crc.getValue()) + "）");
                            }
                        } else {
                            e.size = def.getBytesRead();
                            e.csize = def.getBytesWritten();
                            e.crc = crc.getValue();
                            writeEXT(e);
                        }
                        def.reset();
                        written += e.csize;
                    }
                    break;
                case STORED: {
                        // 我们已经知道 e.size 和 e.csize 是相同的
                        if (e.size != written - locoff) {
                            throw new ZipException(
                                "无效的条目大小（期望 " + e.size +
                                " 但实际为 " + (written - locoff) + " 字节）");
                        }
                        if (e.crc != crc.getValue()) {
                            throw new ZipException(
                                "无效的条目 CRC-32（期望 0x" +
                                Long.toHexString(e.crc) + " 但实际为 0x" +
                                Long.toHexString(crc.getValue()) + "）");
                        }
                    }
                    break;
                default:
                    throw new ZipException("无效的压缩方法");
                }
                crc.reset();
                current = null;
            } catch (IOException e) {
                if (def.shouldFinish() && usesDefaultDeflater && !(e instanceof ZipException))
                    def.end();
                throw e;
            }
        }
    }

    /**
     * 将字节数组写入当前 ZIP 条目数据。此方法将阻塞，直到所有字节都被写入。
     * @param b 要写入的数据
     * @param off 数据的起始偏移量
     * @param len 要写入的字节数
     * @exception ZipException 如果发生 ZIP 文件错误
     * @exception IOException 如果发生 I/O 错误
     */
    public synchronized void write(byte[] b, int off, int len)
        throws IOException
    {
        ensureOpen();
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        if (current == null) {
            throw new ZipException("没有当前的 ZIP 条目");
        }
        ZipEntry entry = current.entry;
        switch (entry.method) {
        case DEFLATED:
            super.write(b, off, len);
            break;
        case STORED:
            written += len;
            if (written - locoff > entry.size) {
                throw new ZipException(
                    "尝试写入 STORED 条目末尾之外的数据");
            }
            out.write(b, off, len);
            break;
        default:
            throw new ZipException("无效的压缩方法");
        }
        crc.update(b, off, len);
    }

    /**
     * 完成写入 ZIP 输出流的内容，但不关闭底层流。当对同一输出流应用多个过滤器时，使用此方法。
     * @exception ZipException 如果发生 ZIP 文件错误
     * @exception IOException 如果发生 I/O 错误
     */
    public void finish() throws IOException {
        ensureOpen();
        if (finished) {
            return;
        }
        if (current != null) {
            closeEntry();
        }
        // 写入中心目录
        long off = written;
        for (XEntry xentry : xentries)
            writeCEN(xentry);
        writeEND(off, written - off);
        finished = true;
    }

    /**
     * 关闭 ZIP 输出流以及被过滤的流。
     * @exception ZipException 如果发生 ZIP 文件错误
     * @exception IOException 如果发生 I/O 错误
     */
    public void close() throws IOException {
        if (!closed) {
            super.close();
            closed = true;
        }
    }

    /*
     * 为指定的条目写入本地文件（LOC）头。
     */
    private void writeLOC(XEntry xentry) throws IOException {
        ZipEntry e = xentry.entry;
        int flag = e.flag;
        boolean hasZip64 = false;
        int elen = getExtraLen(e.extra);

        writeInt(LOCSIG);               // LOC 头签名
        if ((flag & 8) == 8) {
            writeShort(version(e));     // 提取所需版本
            writeShort(flag);           // 通用目的位标志
            writeShort(e.method);       // 压缩方法
            writeInt(e.xdostime);       // 最后修改时间
            // 在紧随压缩条目数据之后的数据描述符中存储大小、未压缩大小和 CRC-32
            writeInt(0);
            writeInt(0);
            writeInt(0);
        } else {
            if (e.csize >= ZIP64_MAGICVAL || e.size >= ZIP64_MAGICVAL) {
                hasZip64 = true;
                writeShort(45);         // 版本 4.5 用于 ZIP64
            } else {
                writeShort(version(e)); // 提取所需版本
            }
            writeShort(flag);           // 通用目的位标志
            writeShort(e.method);       // 压缩方法
            writeInt(e.xdostime);       // 最后修改时间
            writeInt(e.crc);            // CRC-32
            if (hasZip64) {
                writeInt(ZIP64_MAGICVAL);
                writeInt(ZIP64_MAGICVAL);
                elen += 20;        //headid(2) + size(2) + size(8) + csize(8)
            } else {
                writeInt(e.csize);  // 压缩大小
                writeInt(e.size);   // 未压缩大小
            }
        }
        byte[] nameBytes = zc.getBytes(e.name);
        writeShort(nameBytes.length);


    /*
     * 写入指定条目的额外数据描述符（EXT）。
     */
    private void writeEXT(ZipEntry e) throws IOException {
        writeInt(EXTSIG);           // EXT 头签名
        writeInt(e.crc);            // crc-32
        if (e.csize >= ZIP64_MAGICVAL || e.size >= ZIP64_MAGICVAL) {
            writeLong(e.csize);
            writeLong(e.size);
        } else {
            writeInt(e.csize);          // 压缩大小
            writeInt(e.size);           // 未压缩大小
        }
    }

    /*
     * 为指定条目写入中心目录（CEN）头。
     * 提醒：添加文件属性支持
     */
    private void writeCEN(XEntry xentry) throws IOException {
        ZipEntry e  = xentry.entry;
        int flag = e.flag;
        int version = version(e);
        long csize = e.csize;
        long size = e.size;
        long offset = xentry.offset;
        int elenZIP64 = 0;
        boolean hasZip64 = false;

        if (e.csize >= ZIP64_MAGICVAL) {
            csize = ZIP64_MAGICVAL;
            elenZIP64 += 8;              // csize(8)
            hasZip64 = true;
        }
        if (e.size >= ZIP64_MAGICVAL) {
            size = ZIP64_MAGICVAL;    // size(8)
            elenZIP64 += 8;
            hasZip64 = true;
        }
        if (xentry.offset >= ZIP64_MAGICVAL) {
            offset = ZIP64_MAGICVAL;
            elenZIP64 += 8;              // offset(8)
            hasZip64 = true;
        }
        writeInt(CENSIG);           // CEN 头签名
        if (hasZip64) {
            writeShort(45);         // 版本 4.5 用于 zip64
            writeShort(45);
        } else {
            writeShort(version);    // 创建版本
            writeShort(version);    // 提取所需版本
        }
        writeShort(flag);           // 通用目的位标志
        writeShort(e.method);       // 压缩方法
        writeInt(e.xdostime);       // 最后修改时间
        writeInt(e.crc);            // crc-32
        writeInt(csize);            // 压缩大小
        writeInt(size);             // 未压缩大小
        byte[] nameBytes = zc.getBytes(e.name);
        writeShort(nameBytes.length);

        int elen = getExtraLen(e.extra);
        if (hasZip64) {
            elen += (elenZIP64 + 4);// + headid(2) + datasize(2)
        }
        // cen info-zip 扩展时间戳仅输出 mtime
        // 但如果在 loc 中存在 a/ctime，则设置标志
        int flagEXTT = 0;
        if (e.mtime != null) {
            elen += 4;              // + mtime(4)
            flagEXTT |= EXTT_FLAG_LMT;
        }
        if (e.atime != null) {
            flagEXTT |= EXTT_FLAG_LAT;
        }
        if (e.ctime != null) {
            flagEXTT |= EXTT_FLAT_CT;
        }
        if (flagEXTT != 0) {
            elen += 5;             // headid + sz + flag
        }
        writeShort(elen);
        byte[] commentBytes;
        if (e.comment != null) {
            commentBytes = zc.getBytes(e.comment);
            writeShort(Math.min(commentBytes.length, 0xffff));
        } else {
            commentBytes = null;
            writeShort(0);
        }
        writeShort(0);              // 起始磁盘编号
        writeShort(0);              // 内部文件属性（未使用）
        writeInt(0);                // 外部文件属性（未使用）
        writeInt(offset);           // 本地头的相对偏移
        writeBytes(nameBytes, 0, nameBytes.length);

        // 处理 EXTID_ZIP64 和 EXTID_EXTT
        if (hasZip64) {
            writeShort(ZIP64_EXTID);// Zip64 额外数据
            writeShort(elenZIP64);
            if (size == ZIP64_MAGICVAL)
                writeLong(e.size);
            if (csize == ZIP64_MAGICVAL)
                writeLong(e.csize);
            if (offset == ZIP64_MAGICVAL)
                writeLong(xentry.offset);
        }
        if (flagEXTT != 0) {
            writeShort(EXTID_EXTT);
            if (e.mtime != null) {
                writeShort(5);      // flag + mtime
                writeByte(flagEXTT);
                writeInt(fileTimeToUnixTime(e.mtime));
            } else {
                writeShort(1);      // 仅 flag
                writeByte(flagEXTT);
            }
        }
        writeExtra(e.extra);
        if (commentBytes != null) {
            writeBytes(commentBytes, 0, Math.min(commentBytes.length, 0xffff));
        }
    }

    /*
     * 写入中心目录（END）头的结束。
     */
    private void writeEND(long off, long len) throws IOException {
        boolean hasZip64 = false;
        long xlen = len;
        long xoff = off;
        if (xlen >= ZIP64_MAGICVAL) {
            xlen = ZIP64_MAGICVAL;
            hasZip64 = true;
        }
        if (xoff >= ZIP64_MAGICVAL) {
            xoff = ZIP64_MAGICVAL;
            hasZip64 = true;
        }
        int count = xentries.size();
        if (count >= ZIP64_MAGICCOUNT) {
            hasZip64 |= !inhibitZip64;
            if (hasZip64) {
                count = ZIP64_MAGICCOUNT;
            }
        }
        if (hasZip64) {
            long off64 = written;
            // zip64 中心目录记录结束
            writeInt(ZIP64_ENDSIG);        // zip64 END 记录签名
            writeLong(ZIP64_ENDHDR - 12);  // zip64 end 大小
            writeShort(45);                // 创建版本
            writeShort(45);                // 提取所需版本
            writeInt(0);                   // 本磁盘编号
            writeInt(0);                   // 中心目录开始磁盘
            writeLong(xentries.size());    // 本磁盘上的目录条目数
            writeLong(xentries.size());    // 目录条目总数
            writeLong(len);                // 中心目录长度
            writeLong(off);                // 中心目录偏移

            // zip64 中心目录定位器结束
            writeInt(ZIP64_LOCSIG);        // zip64 END 定位器签名
            writeInt(0);                   // zip64 END 开始磁盘
            writeLong(off64);              // zip64 END 偏移
            writeInt(1);                   // 磁盘总数（？）
        }
        writeInt(ENDSIG);                 // END 记录签名
        writeShort(0);                    // 本磁盘编号
        writeShort(0);                    // 中心目录开始磁盘
        writeShort(count);                // 本磁盘上的目录条目数
        writeShort(count);                // 总目录条目数
        writeInt(xlen);                   // 中心目录长度
        writeInt(xoff);                   // 中心目录偏移
        if (comment != null) {            // zip 文件注释
            writeShort(comment.length);
            writeBytes(comment, 0, comment.length);
        } else {
            writeShort(0);
        }
    }

    /*
     * 返回不包含 EXTT 和 ZIP64 的额外数据长度。
     */
    private int getExtraLen(byte[] extra) {
        if (extra == null)
            return 0;
        int skipped = 0;
        int len = extra.length;
        int off = 0;
        while (off + 4 <= len) {
            int tag = get16(extra, off);
            int sz = get16(extra, off + 2);
            if (sz < 0 || (off + 4 + sz) > len) {
                break;
            }
            if (tag == EXTID_EXTT || tag == EXTID_ZIP64) {
                skipped += (sz + 4);
            }
            off += (sz + 4);
        }
        return len - skipped;
    }

    /*
     * 写入不包含 EXTT 和 ZIP64 的额外数据。
     *
     * 扩展时间戳和 ZIP64 数据在 writeLOC 和 writeCEN 中单独处理/输出。
     */
    private void writeExtra(byte[] extra) throws IOException {
        if (extra != null) {
            int len = extra.length;
            int off = 0;
            while (off + 4 <= len) {
                int tag = get16(extra, off);
                int sz = get16(extra, off + 2);
                if (sz < 0 || (off + 4 + sz) > len) {
                    writeBytes(extra, off, len - off);
                    return;
                }
                if (tag != EXTID_EXTT && tag != EXTID_ZIP64) {
                    writeBytes(extra, off, sz + 4);
                }
                off += (sz + 4);
            }
            if (off < len) {
                writeBytes(extra, off, len - off);
            }
        }
    }

    /*
     * 写入 8 位字节到输出流。
     */
    private void writeByte(int v) throws IOException {
        OutputStream out = this.out;
        out.write(v & 0xff);
        written += 1;
    }

    /*
     * 写入 16 位短整型到输出流，使用小端字节序。
     */
    private void writeShort(int v) throws IOException {
        OutputStream out = this.out;
        out.write((v >>> 0) & 0xff);
        out.write((v >>> 8) & 0xff);
        written += 2;
    }

    /*
     * 写入 32 位整型到输出流，使用小端字节序。
     */
    private void writeInt(long v) throws IOException {
        OutputStream out = this.out;
        out.write((int)((v >>>  0) & 0xff));
        out.write((int)((v >>>  8) & 0xff));
        out.write((int)((v >>> 16) & 0xff));
        out.write((int)((v >>> 24) & 0xff));
        written += 4;
    }

    /*
     * 写入 64 位整型到输出流，使用小端字节序。
     */
    private void writeLong(long v) throws IOException {
        OutputStream out = this.out;
        out.write((int)((v >>>  0) & 0xff));
        out.write((int)((v >>>  8) & 0xff));
        out.write((int)((v >>> 16) & 0xff));
        out.write((int)((v >>> 24) & 0xff));
        out.write((int)((v >>> 32) & 0xff));
        out.write((int)((v >>> 40) & 0xff));
        out.write((int)((v >>> 48) & 0xff));
        out.write((int)((v >>> 56) & 0xff));
        written += 8;
    }

    /*
     * 写入字节数组到输出流。
     */
    private void writeBytes(byte[] b, int off, int len) throws IOException {
        super.out.write(b, off, len);
        written += len;
    }
}
