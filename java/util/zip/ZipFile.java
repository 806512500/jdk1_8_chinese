
/*
 * Copyright (c) 1995, 2021, Oracle and/or its affiliates. All rights reserved.
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

import java.io.Closeable;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.WeakHashMap;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.zip.ZipConstants64.*;

/**
 * 该类用于从 zip 文件中读取条目。
 *
 * <p>除非另有说明，否则将 <tt>null</tt> 参数传递给此类的构造函数或方法将导致抛出 {@link NullPointerException}。
 *
 * @author      David Connelly
 */
public
class ZipFile implements ZipConstants, Closeable {
    private long jzfile;  // jzfile 数据的地址
    private final String name;     // zip 文件名
    private final int total;       // 条目总数
    private final boolean locsig;  // 如果 zip 文件以 LOCSIG 开头（通常为 true）
    private volatile boolean closeRequested = false;
    private int manifestNum = 0;       // META-INF/MANIFEST.MF 的数量，不区分大小写

    private static final int STORED = ZipEntry.STORED;
    private static final int DEFLATED = ZipEntry.DEFLATED;

    /**
     * 用于以读取模式打开 zip 文件的模式标志。
     */
    public static final int OPEN_READ = 0x1;

    /**
     * 用于以读取模式打开 zip 文件并标记为删除的模式标志。文件将在打开和关闭之间某个时刻被删除，但其内容在调用 close 方法或虚拟机退出之前仍可通过 <tt>ZipFile</tt> 对象访问。
     */
    public static final int OPEN_DELETE = 0x4;

    static {
        /* Zip 库在 System.initializeSystemClass 中加载 */
        initIDs();
    }

    private static native void initIDs();

    private static final boolean usemmap;

    private static final boolean ensuretrailingslash;

    static {
        // 一个系统属性，用于禁用内存映射以避免在使用中的 zip 文件被意外覆盖时导致 VM 崩溃。
        String prop = sun.misc.VM.getSavedProperty("sun.zip.disableMemoryMapping");
        usemmap = (prop == null ||
                   !(prop.length() == 0 || prop.equalsIgnoreCase("true")));

        // 请参阅 getEntry() 以获取详细信息
        prop = sun.misc.VM.getSavedProperty("jdk.util.zip.ensureTrailingSlash");
        ensuretrailingslash = prop == null || !prop.equalsIgnoreCase("false");
    }

    /**
     * 打开 zip 文件以进行读取。
     *
     * <p>首先，如果有安全经理，其 <code>checkRead</code> 方法将被调用，以确保允许读取。
     *
     * <p>使用 UTF-8 {@link java.nio.charset.Charset charset} 解码条目名称和注释。
     *
     * @param name zip 文件的名称
     * @throws ZipException 如果发生 ZIP 格式错误
     * @throws IOException 如果发生 I/O 错误
     * @throws SecurityException 如果存在安全经理且其 <code>checkRead</code> 方法不允许读取文件。
     *
     * @see SecurityManager#checkRead(java.lang.String)
     */
    public ZipFile(String name) throws IOException {
        this(new File(name), OPEN_READ);
    }

    /**
     * 打开一个新的 <code>ZipFile</code> 以在指定模式下读取指定的 <code>File</code> 对象。模式参数必须是 <tt>OPEN_READ</tt> 或 <tt>OPEN_READ | OPEN_DELETE</tt>。
     *
     * <p>首先，如果有安全经理，其 <code>checkRead</code> 方法将被调用，以确保允许读取。
     *
     * <p>使用 UTF-8 {@link java.nio.charset.Charset charset} 解码条目名称和注释
     *
     * @param file 要打开以进行读取的 ZIP 文件
     * @param mode 文件的打开模式
     * @throws ZipException 如果发生 ZIP 格式错误
     * @throws IOException 如果发生 I/O 错误
     * @throws SecurityException 如果存在安全经理且其 <code>checkRead</code> 方法不允许读取文件，或其 <code>checkDelete</code> 方法不允许删除文件（当设置 <tt>OPEN_DELETE</tt> 标志时）。
     * @throws IllegalArgumentException 如果 <tt>mode</tt> 参数无效
     * @see SecurityManager#checkRead(java.lang.String)
     * @since 1.3
     */
    public ZipFile(File file, int mode) throws IOException {
        this(file, mode, StandardCharsets.UTF_8);
    }

    /**
     * 打开一个 ZIP 文件以读取指定的 File 对象。
     *
     * <p>使用 UTF-8 {@link java.nio.charset.Charset charset} 解码条目名称和注释。
     *
     * @param file 要打开以进行读取的 ZIP 文件
     * @throws ZipException 如果发生 ZIP 格式错误
     * @throws IOException 如果发生 I/O 错误
     */
    public ZipFile(File file) throws ZipException, IOException {
        this(file, OPEN_READ);
    }

    private ZipCoder zc;

    /**
     * 打开一个新的 <code>ZipFile</code> 以在指定模式下读取指定的 <code>File</code> 对象。模式参数必须是 <tt>OPEN_READ</tt> 或 <tt>OPEN_READ | OPEN_DELETE</tt>。
     *
     * <p>首先，如果有安全经理，其 <code>checkRead</code> 方法将被调用，以确保允许读取。
     *
     * @param file 要打开以进行读取的 ZIP 文件
     * @param mode 文件的打开模式
     * @param charset
     *        用于解码 ZIP 条目名称和注释的 {@linkplain java.nio.charset.Charset charset}（如果条目的通用目的标志指示未使用 UTF-8 编码）。
     *
     * @throws ZipException 如果发生 ZIP 格式错误
     * @throws IOException 如果发生 I/O 错误
     *
     * @throws SecurityException
     *         如果存在安全经理且其 <code>checkRead</code>
     *         方法不允许读取文件，或其 <code>checkDelete</code> 方法不允许删除文件（当设置 <tt>OPEN_DELETE</tt> 标志时）。
     *
     * @throws IllegalArgumentException 如果 <tt>mode</tt> 参数无效
     *
     * @see SecurityManager#checkRead(java.lang.String)
     *
     * @since 1.7
     */
    public ZipFile(File file, int mode, Charset charset) throws IOException
    {
        if (((mode & OPEN_READ) == 0) ||
            ((mode & ~(OPEN_READ | OPEN_DELETE)) != 0)) {
            throw new IllegalArgumentException("非法模式: 0x"+
                                               Integer.toHexString(mode));
        }
        String name = file.getPath();
        if (name.indexOf(0) >= 0) {
            throw new IOException("非法文件名");
        }
        file = new File(name);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkRead(name);
            if ((mode & OPEN_DELETE) != 0) {
                sm.checkDelete(name);
            }
        }
        if (charset == null)
            throw new NullPointerException("charset 为 null");
        this.zc = ZipCoder.get(charset);
        long t0 = System.nanoTime();
        jzfile = open(name, mode, file.lastModified(), usemmap);
        sun.misc.PerfCounter.getZipFileOpenTime().addElapsedTimeFrom(t0);
        sun.misc.PerfCounter.getZipFileCount().increment();
        this.name = name;
        this.total = getTotal(jzfile);
        this.locsig = startsWithLOC(jzfile);
        this.manifestNum = getManifestNum(jzfile);
    }

    /**
     * 打开 zip 文件以进行读取。
     *
     * <p>首先，如果有安全经理，其 <code>checkRead</code> 方法将被调用，以确保允许读取。
     *
     * @param name zip 文件的名称
     * @param charset
     *        用于解码 ZIP 条目名称和注释的 {@linkplain java.nio.charset.Charset charset}（如果条目的通用目的标志指示未使用 UTF-8 编码）。
     *
     * @throws ZipException 如果发生 ZIP 格式错误
     * @throws IOException 如果发生 I/O 错误
     * @throws SecurityException
     *         如果存在安全经理且其 <code>checkRead</code> 方法不允许读取文件
     *
     * @see SecurityManager#checkRead(java.lang.String)
     *
     * @since 1.7
     */
    public ZipFile(String name, Charset charset) throws IOException
    {
        this(new File(name), OPEN_READ, charset);
    }

    /**
     * 打开一个 ZIP 文件以读取指定的 File 对象。
     * @param file 要打开以进行读取的 ZIP 文件
     * @param charset
     *        用于解码 ZIP 条目名称和注释的 {@linkplain java.nio.charset.Charset charset}（如果设置了 ZIP 条目的通用目的位标志中的 <a href="package-summary.html#lang_encoding"> 语言编码位</a>，则忽略）。
     *
     * @throws ZipException 如果发生 ZIP 格式错误
     * @throws IOException 如果发生 I/O 错误
     *
     * @since 1.7
     */
    public ZipFile(File file, Charset charset) throws IOException
    {
        this(file, OPEN_READ, charset);
    }

    /**
     * 返回 zip 文件的注释，如果没有则返回 null。
     *
     * @return zip 文件的注释字符串，如果没有则返回 null
     *
     * @throws IllegalStateException 如果 zip 文件已关闭
     *
     * Since 1.7
     */
    public String getComment() {
        synchronized (this) {
            ensureOpen();
            byte[] bcomm = getCommentBytes(jzfile);
            if (bcomm == null)
                return null;
            return zc.toString(bcomm, bcomm.length);
        }
    }

    /**
     * 返回指定名称的 zip 文件条目，如果没有找到则返回 null。
     *
     * @param name 条目的名称
     * @return zip 文件条目，如果没有找到则返回 null
     * @throws IllegalStateException 如果 zip 文件已关闭
     */
    public ZipEntry getEntry(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        long jzentry = 0;
        synchronized (this) {
            ensureOpen();
            jzentry = getEntry(jzfile, zc.getBytes(name), true);
            if (jzentry != 0) {
                // 如果没有找到指定的 'name' 条目，并且 'name' 不以正斜杠 '/' 结尾，
                // 实现将尝试查找以正斜杠 '/' 结尾的条目，然后再返回 null。当找到这样的条目时，实际找到的名称（附加了正斜杠 '/'）将被使用
                // （如果 jdk.util.zip.ensureTrailingSlash=false，则禁用此功能）
                ZipEntry ze = ensuretrailingslash ? getZipEntry(null, jzentry)
                                                  : getZipEntry(name, jzentry);
                freeEntry(jzfile, jzentry);
                return ze;
            }
        }
        return null;
    }

    private static native long getEntry(long jzfile, byte[] name,
                                        boolean addSlash);

    // freeEntry 释放 C jzentry 结构。
    private static native void freeEntry(long jzfile, long jzentry);

    // 需要关闭的未完成输入流，映射到它们使用的 inflater 对象。
    private final Map<InputStream, Inflater> streams = new WeakHashMap<>();

    /**
     * 返回用于读取指定 zip 文件条目内容的输入流。
     *
     * <p>关闭此 ZIP 文件将关闭所有通过调用此方法返回的输入流。
     *
     * @param entry zip 文件条目
     * @return 用于读取指定 zip 文件条目内容的输入流。
     * @throws ZipException 如果发生 ZIP 格式错误
     * @throws IOException 如果发生 I/O 错误
     * @throws IllegalStateException 如果 zip 文件已关闭
     */
    public InputStream getInputStream(ZipEntry entry) throws IOException {
        if (entry == null) {
            throw new NullPointerException("entry");
        }
        long jzentry = 0;
        ZipFileInputStream in = null;
        synchronized (this) {
            ensureOpen();
            if (!zc.isUTF8() && (entry.flag & EFS) != 0) {
                jzentry = getEntry(jzfile, zc.getBytesUTF8(entry.name), false);
            } else {
                jzentry = getEntry(jzfile, zc.getBytes(entry.name), false);
            }
            if (jzentry == 0) {
                return null;
            }
            in = new ZipFileInputStream(jzentry);

            switch (getEntryMethod(jzentry)) {
            case STORED:
                synchronized (streams) {
                    streams.put(in, null);
                }
                return in;
            case DEFLATED:
                // MORE: 计算 inflater 流的良好大小：
                long size = getEntrySize(jzentry) + 2; // Inflater 喜欢有一点松弛
                if (size > 65536) size = 8192;
                if (size <= 0) size = 4096;
                Inflater inf = getInflater();
                InputStream is =
                    new ZipFileInflaterInputStream(in, inf, (int)size);
                synchronized (streams) {
                    streams.put(is, inf);
                }
                return is;
            default:
                throw new ZipException("无效的压缩方法");
            }
        }
    }


/*
 * Copyright (c) 1996, 1999, ...
 */
private class ZipFileInflaterInputStream extends InflaterInputStream {
    private volatile boolean closeRequested = false;
    private boolean eof = false;
    private final ZipFileInputStream zfin;

    ZipFileInflaterInputStream(ZipFileInputStream zfin, Inflater inf,
            int size) {
        super(zfin, inf, size);
        this.zfin = zfin;
    }

    public void close() throws IOException {
        if (closeRequested)
            return;
        closeRequested = true;

        super.close();
        Inflater inf;
        synchronized (streams) {
            inf = streams.remove(this);
        }
        if (inf != null) {
            releaseInflater(inf);
        }
    }

    // 覆盖 fill() 方法以在输入流的末尾提供一个额外的“虚拟”字节。这是在使用“nowrap” Inflater 选项时所需的。
    protected void fill() throws IOException {
        if (eof) {
            throw new EOFException("ZLIB 输入流意外结束");
        }
        len = in.read(buf, 0, buf.length);
        if (len == -1) {
            buf[0] = 0;
            len = 1;
            eof = true;
        }
        inf.setInput(buf, 0, len);
    }

    public int available() throws IOException {
        if (closeRequested)
            return 0;
        long avail = zfin.size() - inf.getBytesWritten();
        return (avail > (long) Integer.MAX_VALUE ?
                Integer.MAX_VALUE : (int) avail);
    }

    protected void finalize() throws Throwable {
        close();
    }
}

/*
 * 从可用的 inflaters 列表中获取一个 inflater 或分配一个新的。
 */
private Inflater getInflater() {
    Inflater inf;
    synchronized (inflaterCache) {
        while (null != (inf = inflaterCache.poll())) {
            if (false == inf.ended()) {
                return inf;
            }
        }
    }
    return new Inflater(true);
}

/*
 * 将指定的 inflater 释放到可用的 inflaters 列表中。
 */
private void releaseInflater(Inflater inf) {
    if (false == inf.ended()) {
        inf.reset();
        synchronized (inflaterCache) {
            inflaterCache.add(inf);
        }
    }
}

// 用于解压缩的可用 Inflater 对象列表
private Deque<Inflater> inflaterCache = new ArrayDeque<>();

/**
 * 返回 ZIP 文件的路径名。
 * @return ZIP 文件的路径名
 */
public String getName() {
    return name;
}

private class ZipEntryIterator implements Enumeration<ZipEntry>, Iterator<ZipEntry> {
    private int i = 0;

    public ZipEntryIterator() {
        ensureOpen();
    }

    public boolean hasMoreElements() {
        return hasNext();
    }

    public boolean hasNext() {
        synchronized (ZipFile.this) {
            ensureOpen();
            return i < total;
        }
    }

    public ZipEntry nextElement() {
        return next();
    }

    public ZipEntry next() {
        synchronized (ZipFile.this) {
            ensureOpen();
            if (i >= total) {
                throw new NoSuchElementException();
            }
            long jzentry = getNextEntry(jzfile, i++);
            if (jzentry == 0) {
                String message;
                if (closeRequested) {
                    message = "ZipFile 并发关闭";
                } else {
                    message = getZipMessage(ZipFile.this.jzfile);
                }
                throw new ZipError("jzentry == 0" +
                                   ",\n jzfile = " + ZipFile.this.jzfile +
                                   ",\n total = " + ZipFile.this.total +
                                   ",\n name = " + ZipFile.this.name +
                                   ",\n i = " + i +
                                   ",\n message = " + message
                    );
            }
            ZipEntry ze = getZipEntry(null, jzentry);
            freeEntry(jzfile, jzentry);
            return ze;
        }
    }
}

/**
 * 返回 ZIP 文件条目的枚举。
 * @return ZIP 文件条目的枚举
 * @throws IllegalStateException 如果 ZIP 文件已关闭
 */
public Enumeration<? extends ZipEntry> entries() {
    return new ZipEntryIterator();
}

/**
 * 返回一个有序的 {@code Stream}，其中包含 ZIP 文件中的条目。
 * 条目在 {@code Stream} 中的顺序与 ZIP 文件的中心目录中的顺序相同。
 *
 * @return 一个有序的 {@code Stream}，其中包含此 ZIP 文件中的条目
 * @throws IllegalStateException 如果 ZIP 文件已关闭
 * @since 1.8
 */
public Stream<? extends ZipEntry> stream() {
    return StreamSupport.stream(Spliterators.spliterator(
            new ZipEntryIterator(), size(),
            Spliterator.ORDERED | Spliterator.DISTINCT |
                    Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
}

private ZipEntry getZipEntry(String name, long jzentry) {
    ZipEntry e = new ZipEntry();
    e.flag = getEntryFlag(jzentry);  // 先获取标志
    if (name != null) {
        e.name = name;
    } else {
        byte[] bname = getEntryBytes(jzentry, JZENTRY_NAME);
        if (bname == null) {
            e.name = "";             // 长度为 0 的空名称
        } else if (!zc.isUTF8() && (e.flag & EFS) != 0) {
            e.name = zc.toStringUTF8(bname, bname.length);
        } else {
            e.name = zc.toString(bname, bname.length);
        }
    }
    e.xdostime = getEntryTime(jzentry);
    e.crc = getEntryCrc(jzentry);
    e.size = getEntrySize(jzentry);
    e.csize = getEntryCSize(jzentry);
    e.method = getEntryMethod(jzentry);
    e.setExtra0(getEntryBytes(jzentry, JZENTRY_EXTRA), false);
    byte[] bcomm = getEntryBytes(jzentry, JZENTRY_COMMENT);
    if (bcomm == null) {
        e.comment = null;
    } else {
        if (!zc.isUTF8() && (e.flag & EFS) != 0) {
            e.comment = zc.toStringUTF8(bcomm, bcomm.length);
        } else {
            e.comment = zc.toString(bcomm, bcomm.length);
        }
    }
    return e;
}

private static native long getNextEntry(long jzfile, int i);

/**
 * 返回 ZIP 文件中的条目数量。
 * @return ZIP 文件中的条目数量
 * @throws IllegalStateException 如果 ZIP 文件已关闭
 */
public int size() {
    ensureOpen();
    return total;
}

/**
 * 关闭 ZIP 文件。
 * <p> 关闭此 ZIP 文件将关闭所有之前通过调用 {@link #getInputStream getInputStream} 方法返回的输入流。
 *
 * @throws IOException 如果发生 I/O 错误
 */
public void close() throws IOException {
    if (closeRequested)
        return;
    closeRequested = true;

    synchronized (this) {
        // 关闭流并释放它们的 inflaters
        synchronized (streams) {
            if (false == streams.isEmpty()) {
                Map<InputStream, Inflater> copy = new HashMap<>(streams);
                streams.clear();
                for (Map.Entry<InputStream, Inflater> e : copy.entrySet()) {
                    e.getKey().close();
                    Inflater inf = e.getValue();
                    if (inf != null) {
                        inf.end();
                    }
                }
            }
        }

        // 释放缓存的 inflaters
        Inflater inf;
        synchronized (inflaterCache) {
            while (null != (inf = inflaterCache.poll())) {
                inf.end();
            }
        }

        if (jzfile != 0) {
            // 关闭 ZIP 文件
            long zf = this.jzfile;
            jzfile = 0;

            close(zf);
        }
    }
}

/**
 * 确保在没有更多引用此 ZipFile 对象时释放其持有的系统资源。
 *
 * <p>
 * 由于 GC 何时调用此方法的时间不确定，强烈建议应用程序在访问完此 <code>ZipFile</code> 后立即调用 <code>close</code> 方法。
 * 这将防止系统资源被占用不确定的时间。
 *
 * @throws IOException 如果发生 I/O 错误
 * @see    java.util.zip.ZipFile#close()
 */
protected void finalize() throws IOException {
    close();
}

private static native void close(long jzfile);

private void ensureOpen() {
    if (closeRequested) {
        throw new IllegalStateException("zip 文件已关闭");
    }

    if (jzfile == 0) {
        throw new IllegalStateException("对象未初始化。");
    }
}

private void ensureOpenOrZipException() throws IOException {
    if (closeRequested) {
        throw new ZipException("ZipFile 已关闭");
    }
}

/*
 * 实现用于读取（可能是压缩的）ZIP 文件条目的输入流的内部类。
 */
private class ZipFileInputStream extends InputStream {
    private volatile boolean zfisCloseRequested = false;
    protected long jzentry; // jzentry 数据的地址
    private   long pos;     // 当前在条目数据中的位置
    protected long rem;     // 条目中剩余的字节数
    protected long size;    // 此条目的未压缩大小

    ZipFileInputStream(long jzentry) {
        pos = 0;
        rem = getEntryCSize(jzentry);
        size = getEntrySize(jzentry);
        this.jzentry = jzentry;
    }

    public int read(byte b[], int off, int len) throws IOException {
        synchronized (ZipFile.this) {
            long rem = this.rem;
            long pos = this.pos;
            if (rem == 0) {
                return -1;
            }
            if (len <= 0) {
                return 0;
            }
            if (len > rem) {
                len = (int) rem;
            }

            // 检查 ZIP 文件是否打开
            ensureOpenOrZipException();
            len = ZipFile.read(ZipFile.this.jzfile, jzentry, pos, b,
                               off, len);
            if (len > 0) {
                this.pos = (pos + len);
                this.rem = (rem - len);
            }
        }
        if (rem == 0) {
            close();
        }
        return len;
    }

    public int read() throws IOException {
        byte[] b = new byte[1];
        if (read(b, 0, 1) == 1) {
            return b[0] & 0xff;
        } else {
            return -1;
        }
    }

    public long skip(long n) {
        if (n > rem)
            n = rem;
        pos += n;
        rem -= n;
        if (rem == 0) {
            close();
        }
        return n;
    }

    public int available() {
        return rem > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) rem;
    }

    public long size() {
        return size;
    }

    public void close() {
        if (zfisCloseRequested)
            return;
        zfisCloseRequested = true;

        rem = 0;
        synchronized (ZipFile.this) {
            if (jzentry != 0 && ZipFile.this.jzfile != 0) {
                freeEntry(ZipFile.this.jzfile, jzentry);
                jzentry = 0;
            }
        }
        synchronized (streams) {
            streams.remove(this);
        }
    }

    protected void finalize() {
        close();
    }
}

static {
    sun.misc.SharedSecrets.setJavaUtilZipFileAccess(
        new sun.misc.JavaUtilZipFileAccess() {
            public boolean startsWithLocHeader(ZipFile zip) {
                return zip.startsWithLocHeader();
            }
            public int getManifestNum(JarFile jar) {
                return ((ZipFile)jar).getManifestNum();
            }
         }
    );
}

/**
 * 如果且仅当 ZIP 文件以 {@code LOCSIG} 开头时，返回 {@code true}。
 */
private boolean startsWithLocHeader() {
    return locsig;
}

/*
 * 返回不区分大小写的 META-INF/MANIFEST.MF 条目数量。
 * 当此数量大于 1 时，JarVerifier 将认为文件未签名。
 */
private int getManifestNum() {
    synchronized (this) {
        ensureOpen();
        return manifestNum;
    }
}

private static native long open(String name, int mode, long lastModified,
                                boolean usemmap) throws IOException;
private static native int getTotal(long jzfile);
private static native boolean startsWithLOC(long jzfile);
private static native int getManifestNum(long jzfile);
private static native int read(long jzfile, long jzentry,
                               long pos, byte[] b, int off, int len);

// 访问原生 zentry 对象
private static native long getEntryTime(long jzentry);
private static native long getEntryCrc(long jzentry);
private static native long getEntryCSize(long jzentry);
private static native long getEntrySize(long jzentry);
private static native int getEntryMethod(long jzentry);
private static native int getEntryFlag(long jzentry);
private static native byte[] getCommentBytes(long jzfile);

private static final int JZENTRY_NAME = 0;
private static final int JZENTRY_EXTRA = 1;
private static final int JZENTRY_COMMENT = 2;
private static native byte[] getEntryBytes(long jzentry, int type);

private static native String getZipMessage(long jzfile);
}
