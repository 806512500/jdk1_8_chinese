
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

/**
 * 该类提供了使用流行的 ZLIB 压缩库进行通用压缩的支持。ZLIB 压缩库最初是作为 PNG 图形标准的一部分开发的，不受专利保护。它在 <a href="package-summary.html#package_description">java.util.zip
 * 包描述</a> 中有详细说明。
 *
 * <p>以下代码片段演示了使用 <tt>Deflater</tt> 和 <tt>Inflater</tt> 对字符串进行简单的压缩和解压缩。
 *
 * <blockquote><pre>
 * try {
 *     // 将字符串编码为字节
 *     String inputString = "blahblahblah";
 *     byte[] input = inputString.getBytes("UTF-8");
 *
 *     // 压缩字节
 *     byte[] output = new byte[100];
 *     Deflater compresser = new Deflater();
 *     compresser.setInput(input);
 *     compresser.finish();
 *     int compressedDataLength = compresser.deflate(output);
 *     compresser.end();
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
 * @see         Inflater
 * @author      David Connelly
 */
public
class Deflater {

    private final ZStreamRef zsRef;
    private byte[] buf = new byte[0];
    private int off, len;
    private int level, strategy;
    private boolean setParams;
    private boolean finish, finished;
    private long bytesRead;
    private long bytesWritten;

    /**
     * 压缩算法的压缩方法（目前唯一支持的方法）。
     */
    public static final int DEFLATED = 8;

    /**
     * 无压缩的压缩级别。
     */
    public static final int NO_COMPRESSION = 0;

    /**
     * 最快压缩的压缩级别。
     */
    public static final int BEST_SPEED = 1;

    /**
     * 最佳压缩的压缩级别。
     */
    public static final int BEST_COMPRESSION = 9;

    /**
     * 默认压缩级别。
     */
    public static final int DEFAULT_COMPRESSION = -1;

    /**
     * 适用于主要由小值组成且分布较为随机的数据的压缩策略。强制使用更多的霍夫曼编码和较少的字符串匹配。
     */
    public static final int FILTERED = 1;

    /**
     * 仅使用霍夫曼编码的压缩策略。
     */
    public static final int HUFFMAN_ONLY = 2;

    /**
     * 默认压缩策略。
     */
    public static final int DEFAULT_STRATEGY = 0;

    /**
     * 用于实现最佳压缩效果的压缩刷新模式。
     *
     * @see Deflater#deflate(byte[], int, int, int)
     * @since 1.7
     */
    public static final int NO_FLUSH = 0;

    /**
     * 用于刷新所有待处理输出的压缩刷新模式；可能会降低某些压缩算法的压缩效果。
     *
     * @see Deflater#deflate(byte[], int, int, int)
     * @since 1.7
     */
    public static final int SYNC_FLUSH = 2;

    /**
     * 用于刷新所有待处理输出并重置压缩器的压缩刷新模式。频繁使用此模式可能会严重影响压缩效果。
     *
     * @see Deflater#deflate(byte[], int, int, int)
     * @since 1.7
     */
    public static final int FULL_FLUSH = 3;

    static {
        /* Zip 库从 System.initializeSystemClass 加载 */
        initIDs();
    }

    /**
     * 使用指定的压缩级别创建新的压缩器。如果 'nowrap' 为 true，则不会使用 ZLIB 头和校验和字段，以支持 GZIP 和 PKZIP 中使用的压缩格式。
     * @param level 压缩级别（0-9）
     * @param nowrap 如果为 true，则使用 GZIP 兼容的压缩
     */
    public Deflater(int level, boolean nowrap) {
        this.level = level;
        this.strategy = DEFAULT_STRATEGY;
        this.zsRef = new ZStreamRef(init(level, DEFAULT_STRATEGY, nowrap));
    }

    /**
     * 使用指定的压缩级别创建新的压缩器。压缩数据将以 ZLIB 格式生成。
     * @param level 压缩级别（0-9）
     */
    public Deflater(int level) {
        this(level, false);
    }

    /**
     * 使用默认压缩级别创建新的压缩器。压缩数据将以 ZLIB 格式生成。
     */
    public Deflater() {
        this(DEFAULT_COMPRESSION, false);
    }

    /**
     * 设置压缩的输入数据。每当 needsInput() 返回 true 表示需要更多输入数据时，应调用此方法。
     * @param b 输入数据字节
     * @param off 数据的起始偏移量
     * @param len 数据的长度
     * @see Deflater#needsInput
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
     * 设置压缩的输入数据。每当 needsInput() 返回 true 表示需要更多输入数据时，应调用此方法。
     * @param b 输入数据字节
     * @see Deflater#needsInput
     */
    public void setInput(byte[] b) {
        setInput(b, 0, b.length);
    }

    /**
     * 设置压缩的预设字典。预设字典用于历史缓冲区可以预先确定的情况。当数据稍后使用 Inflater.inflate() 解压缩时，可以调用 Inflater.getAdler() 以获取解压缩所需的字典的 Adler-32 值。
     * @param b 字典数据字节
     * @param off 数据的起始偏移量
     * @param len 数据的长度
     * @see Inflater#inflate
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
        }
    }

    /**
     * 设置压缩的预设字典。预设字典用于历史缓冲区可以预先确定的情况。当数据稍后使用 Inflater.inflate() 解压缩时，可以调用 Inflater.getAdler() 以获取解压缩所需的字典的 Adler-32 值。
     * @param b 字典数据字节
     * @see Inflater#inflate
     * @see Inflater#getAdler
     */
    public void setDictionary(byte[] b) {
        setDictionary(b, 0, b.length);
    }

    /**
     * 将压缩策略设置为指定值。
     *
     * <p>如果压缩策略发生变化，下一次调用 {@code deflate} 时将使用旧策略压缩当前可用的输入数据（并可能刷新）；新策略仅在该调用之后生效。
     *
     * @param strategy 新的压缩策略
     * @exception IllegalArgumentException 如果压缩策略无效
     */
    public void setStrategy(int strategy) {
        switch (strategy) {
          case DEFAULT_STRATEGY:
          case FILTERED:
          case HUFFMAN_ONLY:
            break;
          default:
            throw new IllegalArgumentException();
        }
        synchronized (zsRef) {
            if (this.strategy != strategy) {
                this.strategy = strategy;
                setParams = true;
            }
        }
    }

    /**
     * 将压缩级别设置为指定值。
     *
     * <p>如果压缩级别发生变化，下一次调用 {@code deflate} 时将使用旧级别压缩当前可用的输入数据（并可能刷新）；新级别仅在该调用之后生效。
     *
     * @param level 新的压缩级别（0-9）
     * @exception IllegalArgumentException 如果压缩级别无效
     */
    public void setLevel(int level) {
        if ((level < 0 || level > 9) && level != DEFAULT_COMPRESSION) {
            throw new IllegalArgumentException("无效的压缩级别");
        }
        synchronized (zsRef) {
            if (this.level != level) {
                this.level = level;
                setParams = true;
            }
        }
    }

    /**
     * 如果输入数据缓冲区为空且应调用 setInput() 以提供更多输入，则返回 true。
     * @return 如果输入数据缓冲区为空且应调用 setInput() 以提供更多输入，则返回 true
     */
    public boolean needsInput() {
        synchronized (zsRef) {
            return len <= 0;
        }
    }

    /**
     * 当被调用时，表示压缩应以当前输入缓冲区的内容结束。
     */
    public void finish() {
        synchronized (zsRef) {
            finish = true;
        }
    }

    /**
     * 如果已到达压缩数据输出流的末尾，则返回 true。
     * @return 如果已到达压缩数据输出流的末尾，则返回 true
     */
    public boolean finished() {
        synchronized (zsRef) {
            return finished;
        }
    }

    /**
     * 压缩输入数据并用压缩数据填充指定的缓冲区。返回实际压缩数据的字节数。返回值为 0 表示应调用 {@link #needsInput() needsInput} 以确定是否需要更多输入数据。
     *
     * <p>此方法使用 {@link #NO_FLUSH} 作为其压缩刷新模式。形式为 {@code deflater.deflate(b, off, len)} 的此方法调用与
     * {@code deflater.deflate(b, off, len, Deflater.NO_FLUSH)} 的调用结果相同。
     *
     * @param b 压缩数据的缓冲区
     * @param off 数据的起始偏移量
     * @param len 压缩数据的最大字节数
     * @return 实际写入输出缓冲区的压缩数据字节数
     */
    public int deflate(byte[] b, int off, int len) {
        return deflate(b, off, len, NO_FLUSH);
    }

    /**
     * 压缩输入数据并用压缩数据填充指定的缓冲区。返回实际压缩数据的字节数。返回值为 0 表示应调用 {@link #needsInput() needsInput} 以确定是否需要更多输入数据。
     *
     * <p>此方法使用 {@link #NO_FLUSH} 作为其压缩刷新模式。形式为 {@code deflater.deflate(b)} 的此方法调用与
     * {@code deflater.deflate(b, 0, b.length, Deflater.NO_FLUSH)} 的调用结果相同。
     *
     * @param b 压缩数据的缓冲区
     * @return 实际写入输出缓冲区的压缩数据字节数
     */
    public int deflate(byte[] b) {
        return deflate(b, 0, b.length, NO_FLUSH);
    }

    /**
     * 压缩输入数据并用压缩数据填充指定的缓冲区。返回实际压缩的数据字节数。
     *
     * <p>压缩刷新模式是以下三种模式之一：
     *
     * <ul>
     * <li>{@link #NO_FLUSH}：允许压缩器决定在生成输出之前累积多少数据，以实现最佳压缩（应在正常使用场景中使用）。在此刷新模式下，返回值为 0 表示应调用 {@link #needsInput()} 以确定是否需要更多输入数据。
     *
     * <li>{@link #SYNC_FLUSH}：压缩器中所有待处理的输出将被刷新到指定的输出缓冲区，以便处理压缩数据的解压缩器可以获取到目前为止所有可用的输入数据（特别是如果提供了足够的输出空间，{@link #needsInput()} 在此调用后返回 {@code true}）。使用 {@link #SYNC_FLUSH} 刷新可能会降低某些压缩算法的压缩效果，因此应仅在必要时使用。
     *
     * <li>{@link #FULL_FLUSH}：所有待处理的输出将被刷新，就像使用 {@link #SYNC_FLUSH} 一样。压缩状态将被重置，以便处理压缩输出数据的解压缩器可以从这一点重新开始，如果之前的压缩数据已损坏或需要随机访问。频繁使用 {@link #FULL_FLUSH} 可能会严重影响压缩效果。
     * </ul>
     *
     * <p>在 {@link #FULL_FLUSH} 或 {@link #SYNC_FLUSH} 模式下，如果返回值为 {@code len}，即输出缓冲区 {@code b} 的可用空间，应再次调用此方法，使用相同的 {@code flush} 参数和更多的输出空间。
     *
     * @param b 压缩数据的缓冲区
     * @param off 数据的起始偏移量
     * @param len 压缩数据的最大字节数
     * @param flush 压缩刷新模式
     * @return 实际写入输出缓冲区的压缩数据字节数
     *
     * @throws IllegalArgumentException 如果刷新模式无效
     * @since 1.7
     */
    public int deflate(byte[] b, int off, int len, int flush) {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        synchronized (zsRef) {
            ensureOpen();
            if (flush == NO_FLUSH || flush == SYNC_FLUSH ||
                flush == FULL_FLUSH) {
                int thisLen = this.len;
                int n = deflateBytes(zsRef.address(), b, off, len, flush);
                bytesWritten += n;
                bytesRead += (thisLen - this.len);
                return n;
            }
            throw new IllegalArgumentException();
        }
    }


                /**
     * 返回未压缩数据的 ADLER-32 值。
     * @return 未压缩数据的 ADLER-32 值
     */
    public int getAdler() {
        synchronized (zsRef) {
            ensureOpen();
            return getAdler(zsRef.address());
        }
    }

    /**
     * 返回到目前为止输入的未压缩字节总数。
     *
     * <p>由于字节数可能大于
     * Integer.MAX_VALUE，现在推荐使用 {@link #getBytesRead()} 方法
     * 获取此信息。</p>
     *
     * @return 到目前为止输入的未压缩字节总数
     */
    public int getTotalIn() {
        return (int) getBytesRead();
    }

    /**
     * 返回到目前为止输入的未压缩字节总数。
     *
     * @return 到目前为止输入的未压缩字节总数（非负）
     * @since 1.5
     */
    public long getBytesRead() {
        synchronized (zsRef) {
            ensureOpen();
            return bytesRead;
        }
    }

    /**
     * 返回到目前为止输出的压缩字节总数。
     *
     * <p>由于字节数可能大于
     * Integer.MAX_VALUE，现在推荐使用 {@link #getBytesWritten()} 方法
     * 获取此信息。</p>
     *
     * @return 到目前为止输出的压缩字节总数
     */
    public int getTotalOut() {
        return (int) getBytesWritten();
    }

    /**
     * 返回到目前为止输出的压缩字节总数。
     *
     * @return 到目前为止输出的压缩字节总数（非负）
     * @since 1.5
     */
    public long getBytesWritten() {
        synchronized (zsRef) {
            ensureOpen();
            return bytesWritten;
        }
    }

    /**
     * 重置压缩器，以便可以处理新的输入数据集。
     * 保留当前的压缩级别和策略设置。
     */
    public void reset() {
        synchronized (zsRef) {
            ensureOpen();
            reset(zsRef.address());
            finish = false;
            finished = false;
            off = len = 0;
            bytesRead = bytesWritten = 0;
        }
    }

    /**
     * 关闭压缩器并丢弃任何未处理的输入。
     * 当压缩器不再使用时，应调用此方法，但也会在
     * finalize() 方法中自动调用。一旦调用此方法，Deflater 对象的行为
     * 将变得不确定。
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
     * 当垃圾回收时关闭压缩器。
     */
    protected void finalize() {
        end();
    }

    private void ensureOpen() {
        assert Thread.holdsLock(zsRef);
        if (zsRef.address() == 0)
            throw new NullPointerException("Deflater has been closed");
    }

    /**
     * 返回 'finish' 标志的值。
     * 如果调用了 def.finish() 方法，'finish' 将被设置为 true。
     */
    boolean shouldFinish() {
        synchronized (zsRef) {
            return finish;
        }
    }

    private static native void initIDs();
    private native static long init(int level, int strategy, boolean nowrap);
    private native static void setDictionary(long addr, byte[] b, int off, int len);
    private native int deflateBytes(long addr, byte[] b, int off, int len,
                                    int flush);
    private native static int getAdler(long addr);
    private native static void reset(long addr);
    private native static void end(long addr);
}
