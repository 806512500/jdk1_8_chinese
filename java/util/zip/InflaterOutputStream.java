
/*
 * 版权所有 (c) 2006, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 实现用于解压缩存储在 "deflate" 压缩格式中的数据的输出流过滤器。
 *
 * @since       1.6
 * @author      David R Tribble (david@tribble.com)
 *
 * @see InflaterInputStream
 * @see DeflaterInputStream
 * @see DeflaterOutputStream
 */

public class InflaterOutputStream extends FilterOutputStream {
    /** 该流的解压缩器。 */
    protected final Inflater inf;

    /** 用于写入未压缩数据的输出缓冲区。 */
    protected final byte[] buf;

    /** 临时写缓冲区。 */
    private final byte[] wbuf = new byte[1];

    /** 是否使用默认解压缩器。 */
    private boolean usesDefaultInflater = false;

    /** 如果 {@link #close()} 已被调用，则为 true。 */
    private boolean closed = false;

    /**
     * 检查此流是否已被关闭。
     */
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * 使用默认解压缩器和缓冲区大小创建新的输出流。
     *
     * @param out 用于写入未压缩数据的输出流
     * @throws NullPointerException 如果 {@code out} 为 null
     */
    public InflaterOutputStream(OutputStream out) {
        this(out, new Inflater());
        usesDefaultInflater = true;
    }

    /**
     * 使用指定的解压缩器和默认缓冲区大小创建新的输出流。
     *
     * @param out 用于写入未压缩数据的输出流
     * @param infl 该流的解压缩器（"inflater"）
     * @throws NullPointerException 如果 {@code out} 或 {@code infl} 为 null
     */
    public InflaterOutputStream(OutputStream out, Inflater infl) {
        this(out, infl, 512);
    }

    /**
     * 使用指定的解压缩器和缓冲区大小创建新的输出流。
     *
     * @param out 用于写入未压缩数据的输出流
     * @param infl 该流的解压缩器（"inflater"）
     * @param bufLen 解压缩缓冲区大小
     * @throws IllegalArgumentException 如果 {@code bufLen <= 0}
     * @throws NullPointerException 如果 {@code out} 或 {@code infl} 为 null
     */
    public InflaterOutputStream(OutputStream out, Inflater infl, int bufLen) {
        super(out);

        // 检查
        if (out == null)
            throw new NullPointerException("Null output");
        if (infl == null)
            throw new NullPointerException("Null inflater");
        if (bufLen <= 0)
            throw new IllegalArgumentException("Buffer size < 1");

        // 初始化
        inf = infl;
        buf = new byte[bufLen];
    }

    /**
     * 将任何剩余的未压缩数据写入输出流并关闭底层输出流。
     *
     * @throws IOException 如果发生 I/O 错误
     */
    public void close() throws IOException {
        if (!closed) {
            // 完成未压缩输出
            try {
                finish();
            } finally {
                out.close();
                closed = true;
            }
        }
    }

    /**
     * 刷新此输出流，强制任何待处理的缓冲输出字节被写入。
     *
     * @throws IOException 如果发生 I/O 错误或此流已关闭
     */
    public void flush() throws IOException {
        ensureOpen();

        // 完成解压缩和写入待处理的输出数据
        if (!inf.finished()) {
            try {
                while (!inf.finished()  &&  !inf.needsInput()) {
                    int n;

                    // 解压缩待处理的输出数据
                    n = inf.inflate(buf, 0, buf.length);
                    if (n < 1) {
                        break;
                    }

                    // 写入未压缩的输出数据块
                    out.write(buf, 0, n);
                }
                super.flush();
            } catch (DataFormatException ex) {
                // 格式不正确的压缩（ZIP）数据
                String msg = ex.getMessage();
                if (msg == null) {
                    msg = "Invalid ZLIB data format";
                }
                throw new ZipException(msg);
            }
        }
    }

    /**
     * 在不关闭底层流的情况下完成写入未压缩数据到输出流。当对同一输出流连续应用多个过滤器时使用此方法。
     *
     * @throws IOException 如果发生 I/O 错误或此流已关闭
     */
    public void finish() throws IOException {
        ensureOpen();

        // 完成解压缩和写入待处理的输出数据
        flush();
        if (usesDefaultInflater) {
            inf.end();
        }
    }

    /**
     * 将一个字节写入未压缩的输出流。
     *
     * @param b 要解压缩并写入输出流的单个字节的压缩数据
     * @throws IOException 如果发生 I/O 错误或此流已关闭
     * @throws ZipException 如果发生压缩（ZIP）格式错误
     */
    public void write(int b) throws IOException {
        // 写入单个字节的数据
        wbuf[0] = (byte) b;
        write(wbuf, 0, 1);
    }

    /**
     * 将一个字节数组写入未压缩的输出流。
     *
     * @param b 包含要解压缩并写入输出流的压缩数据的缓冲区
     * @param off 压缩数据在 {@code b} 中的起始偏移量
     * @param len 从 {@code b} 中解压缩的字节数
     * @throws IndexOutOfBoundsException 如果 {@code off < 0}，或 {@code len < 0}，或 {@code len > b.length - off}
     * @throws IOException 如果发生 I/O 错误或此流已关闭
     * @throws NullPointerException 如果 {@code b} 为 null
     * @throws ZipException 如果发生压缩（ZIP）格式错误
     */
    public void write(byte[] b, int off, int len) throws IOException {
        // 检查
        ensureOpen();
        if (b == null) {
            throw new NullPointerException("Null buffer for read");
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }


                    // 将未压缩的数据写入输出流
        try {
            for (;;) {
                int n;

                // 用输出数据填充解压缩器缓冲区
                if (inf.needsInput()) {
                    int part;

                    if (len < 1) {
                        break;
                    }

                    part = (len < 512 ? len : 512);
                    inf.setInput(b, off, part);
                    off += part;
                    len -= part;
                }

                // 解压缩并写入输出数据块
                do {
                    n = inf.inflate(buf, 0, buf.length);
                    if (n > 0) {
                        out.write(buf, 0, n);
                    }
                } while (n > 0);

                // 检查解压缩器
                if (inf.finished()) {
                    break;
                }
                if (inf.needsDictionary()) {
                    throw new ZipException("ZLIB字典缺失");
                }
            }
        } catch (DataFormatException ex) {
            // 格式不正确的压缩（ZIP）数据
            String msg = ex.getMessage();
            if (msg == null) {
                msg = "无效的ZLIB数据格式";
            }
            throw new ZipException(msg);
        }
    }
}
