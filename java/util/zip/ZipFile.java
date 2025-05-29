
/*
 * 版权所有 (c) 1995, 2015, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

    private static final int STORED = ZipEntry.STORED;
    private static final int DEFLATED = ZipEntry.DEFLATED;

    /**
     * 用于以读取模式打开 zip 文件的模式标志。
     */
    public static final int OPEN_READ = 0x1;

    /**
     * 用于打开 zip 文件并标记为删除的模式标志。文件将在打开时和关闭时之间的某个时刻被删除，但其内容将通过 <tt>ZipFile</tt> 对象保持可访问，直到调用 close 方法或虚拟机退出。
     */
    public static final int OPEN_DELETE = 0x4;

    static {
        /* Zip 库从 System.initializeSystemClass 加载 */
        initIDs();
    }

    private static native void initIDs();

    private static final boolean usemmap;

    private static final boolean ensuretrailingslash;

    static {
        // 一个系统属性，用于禁用 mmap 以避免在使用中的 zip 文件被意外覆盖时导致 vm 崩溃。
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
     * <p>首先，如果有安全经理，将调用其 <code>checkRead</code> 方法，以确保允许读取。
     *
     * <p>使用 UTF-8 {@link java.nio.charset.Charset 字符集} 解码条目名称和注释。
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
     * 以指定模式从指定的 <code>File</code> 对象打开新的 <code>ZipFile</code>。模式参数必须是 <tt>OPEN_READ</tt> 或 <tt>OPEN_READ | OPEN_DELETE</tt>。
     *
     * <p>首先，如果有安全经理，将调用其 <code>checkRead</code> 方法，以确保允许读取。
     *
     * <p>使用 UTF-8 {@link java.nio.charset.Charset 字符集} 解码条目名称和注释。
     *
     * @param file 要打开以读取的 ZIP 文件
     * @param mode 文件要打开的模式
     * @throws ZipException 如果发生 ZIP 格式错误
     * @throws IOException 如果发生 I/O 错误
     * @throws SecurityException 如果存在安全经理且其 <code>checkRead</code> 方法不允许读取文件，或者其 <code>checkDelete</code> 方法不允许删除文件（当设置了 <tt>OPEN_DELETE</tt> 标志时）。
     * @throws IllegalArgumentException 如果 <tt>mode</tt> 参数无效
     * @see SecurityManager#checkRead(java.lang.String)
     * @since 1.3
     */
    public ZipFile(File file, int mode) throws IOException {
        this(file, mode, StandardCharsets.UTF_8);
    }

    /**
     * 给定指定的 File 对象，打开 ZIP 文件以进行读取。
     *
     * <p>使用 UTF-8 {@link java.nio.charset.Charset 字符集} 解码条目名称和注释。
     *
     * @param file 要打开以读取的 ZIP 文件
     * @throws ZipException 如果发生 ZIP 格式错误
     * @throws IOException 如果发生 I/O 错误
     */
    public ZipFile(File file) throws ZipException, IOException {
        this(file, OPEN_READ);
    }

    private ZipCoder zc;

    /**
     * 以指定模式从指定的 <code>File</code> 对象打开新的 <code>ZipFile</code>。模式参数必须是 <tt>OPEN_READ</tt> 或 <tt>OPEN_READ | OPEN_DELETE</tt>。
     *
     * <p>首先，如果有安全经理，将调用其 <code>checkRead</code> 方法，以确保允许读取。
     *
     * @param file 要打开以读取的 ZIP 文件
     * @param mode 文件要打开的模式
     * @param charset
     *        用于解码未使用 UTF-8 编码的 ZIP 条目名称和注释的 {@linkplain java.nio.charset.Charset 字符集}（由条目的通用目的标志指示）。
     *
     * @throws ZipException 如果发生 ZIP 格式错误
     * @throws IOException 如果发生 I/O 错误
     *
     * @throws SecurityException
     *         如果存在安全经理且其 <code>checkRead</code>
     *         方法不允许读取文件，或者其 <code>checkDelete</code> 方法不允许删除文件（当设置了 <tt>OPEN_DELETE</tt> 标志时）。
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
        file = new File(name);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkRead(name);
            if ((mode & OPEN_DELETE) != 0) {
                sm.checkDelete(name);
            }
        }
        if (charset == null)
            throw new NullPointerException("charset is null");
        this.zc = ZipCoder.get(charset);
        long t0 = System.nanoTime();
        jzfile = open(name, mode, file.lastModified(), usemmap);
        sun.misc.PerfCounter.getZipFileOpenTime().addElapsedTimeFrom(t0);
        sun.misc.PerfCounter.getZipFileCount().increment();
        this.name = name;
        this.total = getTotal(jzfile);
        this.locsig = startsWithLOC(jzfile);
    }

                /**
     * 打开一个 zip 文件以供读取。
     *
     * <p>首先，如果有安全经理，其 <code>checkRead</code>
     * 方法将被调用，以 <code>name</code> 参数作为其参数
     * 以确保读取被允许。
     *
     * @param name zip 文件的名称
     * @param charset
     *        要用于解码未使用 UTF-8 编码（由条目的通用
     *        目的标志指示）的 ZIP 条目名称和注释的 {@linkplain java.nio.charset.Charset 字符集}。
     *
     * @throws ZipException 如果发生 ZIP 格式错误
     * @throws IOException 如果发生 I/O 错误
     * @throws SecurityException
     *         如果存在安全经理且其 <code>checkRead</code>
     *         方法不允许读取文件
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
     * 给定指定的 File 对象，打开一个 ZIP 文件以供读取。
     * @param file 要打开以供读取的 ZIP 文件
     * @param charset
     *        要用于解码 ZIP 条目名称和注释的 {@linkplain java.nio.charset.Charset 字符集}（如果
     *        ZIP 条目的通用目的位标志设置了 <a href="package-summary.html#lang_encoding"> 语言
     *        编码位</a>，则忽略）。
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
     * 返回指定名称的 zip 文件条目，如果未找到则返回 null。
     *
     * @param name 条目的名称
     * @return zip 文件条目，如果未找到则返回 null
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
                // 如果未找到指定的 'name' 条目，并且 'name' 不以正斜杠 '/' 结尾，
                // 实现将尝试找到以斜杠 '/' 附加到 'name' 末尾的条目，然后再返回 null。
                // 当找到这样的条目时，实际找到的名称（附加了斜杠 '/'）
                // 将被使用
                // （如果 jdk.util.zip.ensureTrailingSlash=false，则禁用）
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

    // 需要关闭的未解决的输入流，
    // 映射到它们使用的解压器对象。
    private final Map<InputStream, Inflater> streams = new WeakHashMap<>();

    /**
     * 返回一个用于读取指定 zip 文件条目内容的输入流。
     *
     * <p>关闭此 ZIP 文件将依次关闭所有由调用此方法返回的输入流。
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
                // MORE: 计算解压流的良好大小：
                long size = getEntrySize(jzentry) + 2; // 解压器喜欢有一点余地
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

        // 覆盖 fill() 方法以在输入流的末尾提供一个额外的“虚拟”字节。
        // 这在使用“nowrap” Inflater 选项时是必需的。
        protected void fill() throws IOException {
            if (eof) {
                throw new EOFException("Unexpected end of ZLIB input stream");
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
     * 从可用的 Inflater 列表中获取一个 Inflater，或者分配一个新的。
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
     * 将指定的 Inflater 释放回可用的 Inflater 列表。
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
                        message = "ZipFile concurrently closed";
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
     * 返回一个有序的 {@code Stream}，包含 ZIP 文件中的条目。
     * 条目在 {@code Stream} 中的顺序与 ZIP 文件的中心目录中的顺序相同。
     *
     * @return 一个有序的 {@code Stream}，包含此 ZIP 文件中的条目
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
     * <p> 关闭此 ZIP 文件将关闭所有先前通过调用 {@link #getInputStream
     * getInputStream} 方法返回的输入流。
     *
     * @throws IOException 如果发生 I/O 错误
     */
    public void close() throws IOException {
        if (closeRequested)
            return;
        closeRequested = true;

        synchronized (this) {
            // 关闭流，释放其解压器
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

            // 释放缓存的解压器
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
     * 确保当没有更多引用时，释放此 ZipFile 对象持有的系统资源。
     *
     * <p>
     * 由于不确定 GC 何时会调用此方法，强烈建议应用程序在完成对 <code>ZipFile</code> 的访问后立即调用 <code>close</code>
     * 方法。这将防止系统资源被无限期占用。
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
            throw new IllegalStateException("zip file closed");
        }

        if (jzfile == 0) {
            throw new IllegalStateException("The object is not initialized.");
        }
    }

    private void ensureOpenOrZipException() throws IOException {
        if (closeRequested) {
            throw new ZipException("ZipFile closed");
        }
    }

    /*
     * 内部类实现用于读取（可能是压缩的）ZIP 文件条目的输入流。
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
             }
        );
    }

    /**
     * 如果且仅如果 ZIP 文件以 {@code LOCSIG} 开头，则返回 {@code true}。
     */
    private boolean startsWithLocHeader() {
        return locsig;
    }

                private static native long open(String name, int mode, long lastModified,
                                    boolean usemmap) throws IOException;
    private static native int getTotal(long jzfile);
    private static native boolean startsWithLOC(long jzfile);
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
