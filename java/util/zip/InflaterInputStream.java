/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;

/**
 * 该类实现了用于解压缩 "deflate" 压缩格式数据的流过滤器。它也是其他解压缩过滤器（如 GZIPInputStream）的基础。
 *
 * @see         Inflater
 * @author      David Connelly
 */
public
class InflaterInputStream extends FilterInputStream {
    /**
     * 该流的解压缩器。
     */
    protected Inflater inf;

    /**
     * 解压缩的输入缓冲区。
     */
    protected byte[] buf;

    /**
     * 输入缓冲区的长度。
     */
    protected int len;

    private boolean closed = false;
    // 当到达 EOF 时，此标志设置为 true
    private boolean reachEOF = false;

    /**
     * 检查此流是否未被关闭。
     */
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }


    /**
     * 使用指定的解压缩器和缓冲区大小创建新的输入流。
     * @param in 输入流
     * @param inf 解压缩器（"inflater"）
     * @param size 输入缓冲区大小
     * @exception IllegalArgumentException 如果 {@code size <= 0}
     */
    public InflaterInputStream(InputStream in, Inflater inf, int size) {
        super(in);
        if (in == null || inf == null) {
            throw new NullPointerException();
        } else if (size <= 0) {
            throw new IllegalArgumentException("buffer size <= 0");
        }
        this.inf = inf;
        buf = new byte[size];
    }

    /**
     * 使用指定的解压缩器和默认缓冲区大小创建新的输入流。
     * @param in 输入流
     * @param inf 解压缩器（"inflater"）
     */
    public InflaterInputStream(InputStream in, Inflater inf) {
        this(in, inf, 512);
    }

    boolean usesDefaultInflater = false;

    /**
     * 使用默认解压缩器和缓冲区大小创建新的输入流。
     * @param in 输入流
     */
    public InflaterInputStream(InputStream in) {
        this(in, new Inflater());
        usesDefaultInflater = true;
    }

    private byte[] singleByteBuf = new byte[1];

    /**
     * 读取一个未压缩的数据字节。此方法将阻塞，直到有足够的输入进行解压缩。
     * @return 读取的字节，或如果到达压缩输入的末尾则为 -1
     * @exception IOException 如果发生 I/O 错误
     */
    public int read() throws IOException {
        ensureOpen();
        return read(singleByteBuf, 0, 1) == -1 ? -1 : Byte.toUnsignedInt(singleByteBuf[0]);
    }

    /**
     * 将未压缩的数据读入字节数组。如果 <code>len</code> 不为零，该方法将阻塞，直到可以解压缩一些输入；否则，不读取任何字节并返回 <code>0</code>。
     * @param b 读取数据的缓冲区
     * @param off 目标数组 <code>b</code> 中的开始偏移量
     * @param len 最大读取字节数
     * @return 实际读取的字节数，或如果到达压缩输入的末尾或需要预设字典则为 -1
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，
     * <code>len</code> 为负数，或 <code>len</code> 大于 <code>b.length - off</code>
     * @exception ZipException 如果发生 ZIP 格式错误
     * @exception IOException 如果发生 I/O 错误
     */
    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        try {
            int n;
            while ((n = inf.inflate(b, off, len)) == 0) {
                if (inf.finished() || inf.needsDictionary()) {
                    reachEOF = true;
                    return -1;
                }
                if (inf.needsInput()) {
                    fill();
                }
            }
            return n;
        } catch (DataFormatException e) {
            String s = e.getMessage();
            throw new ZipException(s != null ? s : "Invalid ZLIB data format");
        }
    }

    /**
     * 在到达 EOF 后返回 0，否则始终返回 1。
     * <p>
     * 程序不应依赖此方法返回在不阻塞的情况下可以读取的实际字节数。
     *
     * @return 在 EOF 之前返回 1，在 EOF 之后返回 0。
     * @exception  IOException  如果发生 I/O 错误。
     *
     */
    public int available() throws IOException {
        ensureOpen();
        if (reachEOF) {
            return 0;
        } else {
            return 1;
        }
    }

    private byte[] b = new byte[512];

    /**
     * 跳过指定数量的未压缩数据字节。
     * @param n 要跳过的字节数
     * @return 实际跳过的字节数。
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
            if (len > b.length) {
                len = b.length;
            }
            len = read(b, 0, len);
            if (len == -1) {
                reachEOF = true;
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
            if (usesDefaultInflater)
                inf.end();
            in.close();
            closed = true;
        }
    }

    /**
     * 用更多数据填充输入缓冲区以进行解压缩。
     * @exception IOException 如果发生 I/O 错误
     */
    protected void fill() throws IOException {
        ensureOpen();
        len = in.read(buf, 0, buf.length);
        if (len == -1) {
            throw new EOFException("ZLIB 输入流意外结束");
        }
        inf.setInput(buf, 0, len);
    }

    /**
     * 测试此输入流是否支持 <code>mark</code> 和 <code>reset</code> 方法。
     * <code>InflaterInputStream</code> 的 <code>markSupported</code> 方法返回 <code>false</code>。
     *
     * @return  一个 <code>boolean</code> 值，指示此流类型是否支持 <code>mark</code> 和 <code>reset</code> 方法。
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * 在此输入流中标记当前位置。
     *
     * <p> <code>InflaterInputStream</code> 的 <code>mark</code> 方法不执行任何操作。
     *
     * @param   readlimit   在标记位置失效之前可以读取的最大字节数。
     * @see     java.io.InputStream#reset()
     */
    public synchronized void mark(int readlimit) {
    }

    /**
     * 将此流重新定位到上次在此输入流上调用 <code>mark</code> 方法时的位置。
     *
     * <p> <code>InflaterInputStream</code> 类的 <code>reset</code> 方法除了抛出一个 <code>IOException</code> 外，不执行任何操作。
     *
     * @exception  IOException  如果调用此方法。
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.IOException
     */
    public synchronized void reset() throws IOException {
        throw new IOException("不支持 mark/reset");
    }
}
