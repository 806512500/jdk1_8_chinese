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

/**
 * 该类提供使用流行的 ZLIB 压缩库进行通用解压缩的支持。ZLIB 压缩库最初是作为 PNG 图形标准的一部分开发的，不受专利保护。它在 <a href="package-summary.html#package_description">java.util.zip
 * 包描述</a> 中有详细说明。
 *
 * <p>以下代码片段演示了使用 <tt>Deflater</tt> 和 <tt>Inflater</tt> 对字符串进行简单的压缩和解压缩。
 *
 * <blockquote><pre>
 * try {
 *     // 将字符串编码为字节
 *     String inputString = "blahblahblah\u20AC\u20AC";
 *     byte[] input = inputString.getBytes("UTF-8");
 *
 *     // 压缩字节
 *     byte[] output = new byte[100];
 *     Deflater compresser = new Deflater();
 *     compresser.setInput(input);
 *     compresser.finish();
 *     int compressedDataLength = compresser.deflate(output);
 *
 *     // 解压缩字节
 *     Inflater decompresser = new Inflater();
 *     decompresser.setInput(output, 0, compressedDataLength);
 *     byte[] result = new byte[100];
 *     int resultLength = decompresser.inflate(result);
 *     decompresser.end();
 *
 *     // 将字节解码为字符串
 *     String outputString = new String(result, 0, resultLength, "UTF-8");
 * } catch(java.io.UnsupportedEncodingException ex) {
 *     // 处理
 * } catch (java.util.zip.DataFormatException ex) {
 *     // 处理
 * }
 * </pre></blockquote>
 *
 * @see         Deflater
 * @author      David Connelly
 *
 */
public
class Inflater {

    private final ZStreamRef zsRef;
    private byte[] buf = defaultBuf;
    private int off, len;
    private boolean finished;
    private boolean needDict;
    private long bytesRead;
    private long bytesWritten;

    private static final byte[] defaultBuf = new byte[0];

    static {
        /* Zip 库从 System.initializeSystemClass 加载 */
        initIDs();
    }

    /**
     * 创建一个新的解压缩器。如果参数 'nowrap' 为 true，则不使用 ZLIB 头和校验字段。这提供了与 GZIP 和 PKZIP 使用的压缩格式的兼容性。
     * <p>
     * 注意：使用 'nowrap' 选项时，还需要提供一个额外的“虚拟”字节作为输入。这是 ZLIB 本地库为了支持某些优化而要求的。
     *
     * @param nowrap 如果为 true，则支持 GZIP 兼容的压缩
     */
    public Inflater(boolean nowrap) {
        zsRef = new ZStreamRef(init(nowrap));
    }

    /**
     * 创建一个新的解压缩器。
     */
    public Inflater() {
        this(false);
    }

    /**
     * 为解压缩设置输入数据。当 needsInput() 返回 true 表示需要更多输入数据时，应调用此方法。
     * @param b 输入数据字节
     * @param off 输入数据的起始偏移量
     * @param len 输入数据的长度
     * @see Inflater#needsInput
     */
    public void setInput(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        synchronized (zsRef) {
            this.buf = b;
            this.off = off;
            this.len = len;
        }
    }

    /**
     * 为解压缩设置输入数据。当 needsInput() 返回 true 表示需要更多输入数据时，应调用此方法。
     * @param b 输入数据字节
     * @see Inflater#needsInput
     */
    public void setInput(byte[] b) {
        setInput(b, 0, b.length);
    }

    /**
     * 将预设字典设置为给定的字节数组。当 inflate() 返回 0 且 needsDictionary() 返回 true 表示需要预设字典时，应调用此方法。可以使用 getAdler() 方法获取所需字典的 Adler-32 值。
     * @param b 字典数据字节
     * @param off 数据的起始偏移量
     * @param len 数据的长度
     * @see Inflater#needsDictionary
     * @see Inflater#getAdler
     */
    public void setDictionary(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        synchronized (zsRef) {
            ensureOpen();
            setDictionary(zsRef.address(), b, off, len);
            needDict = false;
        }
    }

    /**
     * 将预设字典设置为给定的字节数组。当 inflate() 返回 0 且 needsDictionary() 返回 true 表示需要预设字典时，应调用此方法。可以使用 getAdler() 方法获取所需字典的 Adler-32 值。
     * @param b 字典数据字节
     * @see Inflater#needsDictionary
     * @see Inflater#getAdler
     */
    public void setDictionary(byte[] b) {
        setDictionary(b, 0, b.length);
    }

    /**
     * 返回输入缓冲区中剩余的总字节数。这可以用于确定解压缩完成后输入缓冲区中仍剩余哪些字节。
     * @return 输入缓冲区中剩余的总字节数
     */
    public int getRemaining() {
        synchronized (zsRef) {
            return len;
        }
    }

    /**
     * 如果输入缓冲区中没有数据，则返回 true。这可以用于确定是否应调用 #setInput 以提供更多输入。
     * @return 如果输入缓冲区中没有数据，则返回 true
     */
    public boolean needsInput() {
        synchronized (zsRef) {
            return len <= 0;
        }
    }

    /**
     * 如果需要预设字典进行解压缩，则返回 true。
     * @return 如果需要预设字典进行解压缩，则返回 true
     * @see Inflater#setDictionary
     */
    public boolean needsDictionary() {
        synchronized (zsRef) {
            return needDict;
        }
    }

    /**
     * 如果已到达压缩数据流的末尾，则返回 true。
     * @return 如果已到达压缩数据流的末尾，则返回 true
     */
    public boolean finished() {
        synchronized (zsRef) {
            return finished;
        }
    }

    /**
     * 将字节解压缩到指定的缓冲区。返回实际解压缩的字节数。返回值为 0 表示应调用 needsInput() 或 needsDictionary() 以确定是否需要更多输入数据或预设字典。在后一种情况下，可以使用 getAdler() 获取所需字典的 Adler-32 值。
     * @param b 用于解压缩数据的缓冲区
     * @param off 数据的起始偏移量
     * @param len 最大解压缩字节数
     * @return 实际解压缩的字节数
     * @exception DataFormatException 如果压缩数据格式无效
     * @see Inflater#needsInput
     * @see Inflater#needsDictionary
     */
    public int inflate(byte[] b, int off, int len)
        throws DataFormatException
    {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        synchronized (zsRef) {
            ensureOpen();
            int thisLen = this.len;
            int n = inflateBytes(zsRef.address(), b, off, len);
            bytesWritten += n;
            bytesRead += (thisLen - this.len);
            return n;
        }
    }

    /**
     * 将字节解压缩到指定的缓冲区。返回实际解压缩的字节数。返回值为 0 表示应调用 needsInput() 或 needsDictionary() 以确定是否需要更多输入数据或预设字典。在后一种情况下，可以使用 getAdler() 获取所需字典的 Adler-32 值。
     * @param b 用于解压缩数据的缓冲区
     * @return 实际解压缩的字节数
     * @exception DataFormatException 如果压缩数据格式无效
     * @see Inflater#needsInput
     * @see Inflater#needsDictionary
     */
    public int inflate(byte[] b) throws DataFormatException {
        return inflate(b, 0, b.length);
    }

    /**
     * 返回解压缩数据的 ADLER-32 值。
     * @return 解压缩数据的 ADLER-32 值
     */
    public int getAdler() {
        synchronized (zsRef) {
            ensureOpen();
            return getAdler(zsRef.address());
        }
    }

    /**
     * 返回到目前为止输入的总压缩字节数。
     *
     * <p>由于字节数可能大于 Integer.MAX_VALUE，现在推荐使用 {@link #getBytesRead()} 方法来获取此信息。</p>
     *
     * @return 到目前为止输入的总压缩字节数
     */
    public int getTotalIn() {
        return (int) getBytesRead();
    }

    /**
     * 返回到目前为止输入的总压缩字节数。
     *
     * @return 到目前为止输入的总（非负）压缩字节数
     * @since 1.5
     */
    public long getBytesRead() {
        synchronized (zsRef) {
            ensureOpen();
            return bytesRead;
        }
    }

    /**
     * 返回到目前为止输出的总解压缩字节数。
     *
     * <p>由于字节数可能大于 Integer.MAX_VALUE，现在推荐使用 {@link #getBytesWritten()} 方法来获取此信息。</p>
     *
     * @return 到目前为止输出的总解压缩字节数
     */
    public int getTotalOut() {
        return (int) getBytesWritten();
    }

    /**
     * 返回到目前为止输出的总解压缩字节数。
     *
     * @return 到目前为止输出的总（非负）解压缩字节数
     * @since 1.5
     */
    public long getBytesWritten() {
        synchronized (zsRef) {
            ensureOpen();
            return bytesWritten;
        }
    }

    /**
     * 重置解压缩器，以便可以处理新的输入数据集。
     */
    public void reset() {
        synchronized (zsRef) {
            ensureOpen();
            reset(zsRef.address());
            buf = defaultBuf;
            finished = false;
            needDict = false;
            off = len = 0;
            bytesRead = bytesWritten = 0;
        }
    }

    /**
     * 关闭解压缩器并丢弃任何未处理的输入。当解压缩器不再使用时应调用此方法，但也会在 finalize() 方法中自动调用。调用此方法后，Inflater 对象的行为是未定义的。
     */
    public void end() {
        synchronized (zsRef) {
            long addr = zsRef.address();
            zsRef.clear();
            if (addr != 0) {
                end(addr);
                buf = null;
            }
        }
    }

    /**
     * 当垃圾回收时关闭解压缩器。
     */
    protected void finalize() {
        end();
    }

    private void ensureOpen () {
        assert Thread.holdsLock(zsRef);
        if (zsRef.address() == 0)
            throw new NullPointerException("Inflater has been closed");
    }

    boolean ended() {
        synchronized (zsRef) {
            return zsRef.address() == 0;
        }
    }

    private native static void initIDs();
    private native static long init(boolean nowrap);
    private native static void setDictionary(long addr, byte[] b, int off,
                                             int len);
    private native int inflateBytes(long addr, byte[] b, int off, int len)
            throws DataFormatException;
    private native static int getAdler(long addr);
    private native static void reset(long addr);
    private native static void end(long addr);
}
