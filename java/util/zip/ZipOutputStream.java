
/*
 * Copyright (c) 1996, 2022, Oracle and/or its affiliates. All rights reserved.
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

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Vector;
import java.util.HashSet;
import static java.util.zip.ZipConstants64.*;
import static java.util.zip.ZipUtils.*;

/**
 * 该类实现了用于写入ZIP文件格式文件的输出流过滤器。支持压缩和未压缩的条目。
 *
 * @author      David Connelly
 */
public
class ZipOutputStream extends DeflaterOutputStream implements ZipConstants {

    /**
     * 是否在包含超过64k条目的ZIP文件中使用ZIP64。
     * 由于ZIP64支持在ZIP实现中尚未普及，此系统属性允许创建可以被旧版ZIP实现读取的ZIP文件，
     * 这些旧版ZIP实现可以容忍“不正确”的总条目数字段，例如JDK6中的，甚至一些JDK7中的。
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
     * 检查此流是否已关闭。
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
     * 创建一个新的ZIP输出流。
     *
     * <p>使用UTF-8 {@link java.nio.charset.Charset 字符集}编码条目名称和注释。
     *
     * @param out 实际的输出流
     */
    public ZipOutputStream(OutputStream out) {
        this(out, StandardCharsets.UTF_8);
    }

    /**
     * 创建一个新的ZIP输出流。
     *
     * @param out 实际的输出流
     *
     * @param charset 用于编码条目名称和注释的{@linkplain java.nio.charset.Charset 字符集}
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
     * 设置ZIP文件的注释。
     * @param comment 注释字符串
     * @exception IllegalArgumentException 如果指定的ZIP文件注释长度大于0xFFFF字节
     */
    public void setComment(String comment) {
        if (comment != null) {
            this.comment = zc.getBytes(comment);
            if (this.comment.length > 0xffff)
                throw new IllegalArgumentException("ZIP文件注释过长。");
        }
    }

    /**
     * 设置后续条目的默认压缩方法。如果未为单个ZIP文件条目指定压缩方法，则将使用此默认值，初始设置为DEFLATED。
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
     * 设置后续DEFLATED条目的压缩级别。默认设置为DEFAULT_COMPRESSION。
     * @param level 压缩级别（0-9）
     * @exception IllegalArgumentException 如果压缩级别无效
     */
    public void setLevel(int level) {
        def.setLevel(level);
    }

    /**
     * 开始写入新的ZIP文件条目，并将流定位到条目数据的起始位置。如果当前条目仍处于活动状态，则关闭当前条目。
     * 如果未为条目指定压缩方法，则使用默认压缩方法；如果条目没有设置修改时间，则使用当前时间。
     * @param e 要写入的ZIP条目
     * @exception ZipException 如果发生ZIP格式错误
     * @exception IOException 如果发生I/O错误
     */
    public void putNextEntry(ZipEntry e) throws IOException {
        ensureOpen();
        if (current != null) {
            closeEntry();       // 关闭前一个条目
        }
        if (e.xdostime == -1) {
            // 默认情况下，暂时不在额外数据中使用扩展时间戳。
            e.setTime(System.currentTimeMillis());
        }
        if (e.method == -1) {
            e.method = method;  // 使用默认方法
        }
        // 在LOC头中存储大小、压缩大小和CRC-32
        e.flag = 0;
        switch (e.method) {
        case DEFLATED:
            // 在紧随压缩条目数据之后的数据描述符中存储大小、压缩大小和CRC-32
            if (e.size  == -1 || e.csize == -1 || e.crc   == -1)
                e.flag = 8;


                        break;
        case STORED:
            // 压缩大小、未压缩大小和 crc-32 必须全部设置
            // 对于使用 STORED 压缩方法的条目
            if (e.size == -1) {
                e.size = e.csize;
            } else if (e.csize == -1) {
                e.csize = e.size;
            } else if (e.size != e.csize) {
                throw new ZipException(
                    "STORED 条目中压缩大小 != 未压缩大小");
            }
            if (e.size == -1 || e.crc == -1) {
                throw new ZipException(
                    "STORED 条目缺少大小、压缩大小或 crc-32");
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
     * 关闭当前 ZIP 条目并将流定位为写入下一个条目。
     * @exception ZipException 如果发生 ZIP 格式错误
     * @exception IOException 如果发生 I/O 错误
     */
    public void closeEntry() throws IOException {
        ensureOpen();
        if (current != null) {
            try {
            ZipEntry e = current.entry;
            switch (e.method) {
            case DEFLATED:
                def.finish();
                while (!def.finished()) {
                    deflate();
                }
                if ((e.flag & 8) == 0) {
                    // 验证大小、压缩大小和 crc-32 设置
                    if (e.size != def.getBytesRead()) {
                        throw new ZipException(
                            "无效的条目大小 (预期 " + e.size +
                            " 但实际为 " + def.getBytesRead() + " 字节)");
                    }
                    if (e.csize != def.getBytesWritten()) {
                        throw new ZipException(
                            "无效的条目压缩大小 (预期 " +
                            e.csize + " 但实际为 " + def.getBytesWritten() + " 字节)");
                    }
                    if (e.crc != crc.getValue()) {
                        throw new ZipException(
                            "无效的条目 CRC-32 (预期 0x" +
                            Long.toHexString(e.crc) + " 但实际为 0x" +
                            Long.toHexString(crc.getValue()) + ")");
                    }
                } else {
                    e.size  = def.getBytesRead();
                    e.csize = def.getBytesWritten();
                    e.crc = crc.getValue();
                    writeEXT(e);
                }
                def.reset();
                written += e.csize;
                break;
            case STORED:
                // 我们已经知道 e.size 和 e.csize 是相同的
                if (e.size != written - locoff) {
                    throw new ZipException(
                        "无效的条目大小 (预期 " + e.size +
                        " 但实际为 " + (written - locoff) + " 字节)");
                }
                if (e.crc != crc.getValue()) {
                    throw new ZipException(
                         "无效的条目 crc-32 (预期 0x" +
                         Long.toHexString(e.crc) + " 但实际为 0x" +
                         Long.toHexString(crc.getValue()) + ")");
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
     * 将字节数组写入当前 ZIP 条目数据。此方法将阻塞直到所有字节都被写入。
     * @param b 要写入的数据
     * @param off 数据中的起始偏移量
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
            throw new ZipException("没有当前 ZIP 条目");
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
     * 完成写入 ZIP 输出流的内容而不关闭底层流。当对同一输出流连续应用多个过滤器时使用此方法。
     * @exception ZipException 如果发生 ZIP 文件错误
     * @exception IOException 如果发生 I/O 异常
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
            writeShort(version(e));     // 提取所需的版本
            writeShort(flag);           // 通用目的位标志
            writeShort(e.method);       // 压缩方法
            writeInt(e.xdostime);       // 最后修改时间
            // 在压缩条目数据后立即存储大小、未压缩大小和 crc-32
            // 在数据描述符中
            writeInt(0);
            writeInt(0);
            writeInt(0);
        } else {
            if (e.csize >= ZIP64_MAGICVAL || e.size >= ZIP64_MAGICVAL) {
                hasZip64 = true;
                writeShort(45);         // zip64 的版本 4.5
            } else {
                writeShort(version(e)); // 提取所需的版本
            }
            writeShort(flag);           // 通用目的位标志
            writeShort(e.method);       // 压缩方法
            writeInt(e.xdostime);       // 最后修改时间
            writeInt(e.crc);            // crc-32
            if (hasZip64) {
                writeInt(ZIP64_MAGICVAL);
                writeInt(ZIP64_MAGICVAL);
                elen += 20;        // headid(2) + size(2) + size(8) + csize(8)
            } else {
                writeInt(e.csize);  // 压缩大小
                writeInt(e.size);   // 未压缩大小
            }
        }
        byte[] nameBytes = zc.getBytes(e.name);
        writeShort(nameBytes.length);

        int elenEXTT = 0;               // info-zip 扩展时间戳
        int flagEXTT = 0;
        if (e.mtime != null) {
            elenEXTT += 4;
            flagEXTT |= EXTT_FLAG_LMT;
        }
        if (e.atime != null) {
            elenEXTT += 4;
            flagEXTT |= EXTT_FLAG_LAT;
        }
        if (e.ctime != null) {
            elenEXTT += 4;
            flagEXTT |= EXTT_FLAT_CT;
        }
        if (flagEXTT != 0)
            elen += (elenEXTT + 5);    // headid(2) + size(2) + flag(1) + data
        writeShort(elen);
        writeBytes(nameBytes, 0, nameBytes.length);
        if (hasZip64) {
            writeShort(ZIP64_EXTID);
            writeShort(16);
            writeLong(e.size);
            writeLong(e.csize);
        }
        if (flagEXTT != 0) {
            writeShort(EXTID_EXTT);
            writeShort(elenEXTT + 1);      // flag + data
            writeByte(flagEXTT);
            if (e.mtime != null)
                writeInt(fileTimeToUnixTime(e.mtime));
            if (e.atime != null)
                writeInt(fileTimeToUnixTime(e.atime));
            if (e.ctime != null)
                writeInt(fileTimeToUnixTime(e.ctime));
        }
        writeExtra(e.extra);
        locoff = written;
    }

    /*
     * 为指定的条目写入额外的数据描述符（EXT）。
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
     * 为指定的条目写入中心目录（CEN）头。
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
            writeShort(45);         // zip64 的版本 4.5
            writeShort(45);
        } else {
            writeShort(version);    // 创建版本
            writeShort(version);    // 提取所需的版本
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
        writeInt(offset);           // 本地头的相对偏移量
        writeBytes(nameBytes, 0, nameBytes.length);


                    // 处理 EXTID_ZIP64 和 EXTID_EXTT
        if (hasZip64) {
            writeShort(ZIP64_EXTID);// Zip64 扩展
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
                writeShort(5);      // 标志 + 修改时间
                writeByte(flagEXTT);
                writeInt(fileTimeToUnixTime(e.mtime));
            } else {
                writeShort(1);      // 仅标志
                writeByte(flagEXTT);
            }
        }
        writeExtra(e.extra);
        if (commentBytes != null) {
            writeBytes(commentBytes, 0, Math.min(commentBytes.length, 0xffff));
        }
    }

    /*
     * 写入中心目录结束 (END) 头。
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
            // Zip64 中心目录结束记录
            writeInt(ZIP64_ENDSIG);        // Zip64 END 记录签名
            writeLong(ZIP64_ENDHDR - 12);  // Zip64 结束记录的大小
            writeShort(45);                // 创建版本
            writeShort(45);                // 提取版本
            writeInt(0);                   // 当前磁盘编号
            writeInt(0);                   // 中心目录开始磁盘
            writeLong(xentries.size());    // 当前磁盘上的目录项数
            writeLong(xentries.size());    // 目录项总数
            writeLong(len);                // 中心目录的长度
            writeLong(off);                // 中心目录的偏移

            // Zip64 中心目录结束定位器
            writeInt(ZIP64_LOCSIG);        // Zip64 END 定位器签名
            writeInt(0);                   // Zip64 END 开始磁盘
            writeLong(off64);              // Zip64 END 的偏移
            writeInt(1);                   // 磁盘总数 (?)
        }
        writeInt(ENDSIG);                 // END 记录签名
        writeShort(0);                    // 当前磁盘编号
        writeShort(0);                    // 中心目录开始磁盘
        writeShort(count);                // 当前磁盘上的目录项数
        writeShort(count);                // 目录项总数
        writeInt(xlen);                   // 中心目录的长度
        writeInt(xoff);                   // 中心目录的偏移
        if (comment != null) {            // ZIP 文件注释
            writeShort(comment.length);
            writeBytes(comment, 0, comment.length);
        } else {
            writeShort(0);
        }
    }

    /*
     * 返回不包含 EXTT 和 ZIP64 的额外数据的长度。
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
     * 时间戳扩展和 ZIP64 数据在 writeLOC 和 writeCEN 中单独处理/输出。
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
     * 向输出流写入 8 位字节。
     */
    private void writeByte(int v) throws IOException {
        OutputStream out = this.out;
        out.write(v & 0xff);
        written += 1;
    }

    /*
     * 以小端字节序向输出流写入 16 位短整型。
     */
    private void writeShort(int v) throws IOException {
        OutputStream out = this.out;
        out.write((v >>> 0) & 0xff);
        out.write((v >>> 8) & 0xff);
        written += 2;
    }

    /*
     * 以小端字节序向输出流写入 32 位整型。
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
     * 以小端字节序向输出流写入 64 位整型。
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
     * 向输出流写入字节数组。
     */
    private void writeBytes(byte[] b, int off, int len) throws IOException {
        super.out.write(b, off, len);
        written += len;
    }
}
