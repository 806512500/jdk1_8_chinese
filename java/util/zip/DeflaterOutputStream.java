
/*
 * 版权所有 (c) 1996, 2022, Oracle 和/或其附属公司。保留所有权利。
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
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * 该类实现了用于压缩数据的输出流过滤器，压缩格式为 "deflate"。它还作为其他类型压缩过滤器的基础，如 GZIPOutputStream。
 *
 * @see         Deflater
 * @author      David Connelly
 */
public
class DeflaterOutputStream extends FilterOutputStream {
    /**
     * 此流的压缩器。
     */
    protected Deflater def;

    /**
     * 用于写入压缩数据的输出缓冲区。
     */
    protected byte[] buf;

    /**
     * 表示流已关闭。
     */

    private boolean closed = false;

    private final boolean syncFlush;

    /**
     * 使用指定的压缩器、缓冲区大小和刷新模式创建新的输出流。
     *
     * @param out 输出流
     * @param def 压缩器 ("deflater")
     * @param size 输出缓冲区大小
     * @param syncFlush
     *        如果为 {@code true}，则此实例的 {@link #flush()} 方法在刷新输出流之前会以 {@link Deflater#SYNC_FLUSH} 模式刷新压缩器，否则仅刷新输出流
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
     * 使用指定的压缩器和缓冲区大小创建新的输出流。
     *
     * <p>新的输出流实例的创建方式与调用 4 参数构造函数 DeflaterOutputStream(out, def, size, false) 相同。
     *
     * @param out 输出流
     * @param def 压缩器 ("deflater")
     * @param size 输出缓冲区大小
     * @exception IllegalArgumentException 如果 {@code size <= 0}
     */
    public DeflaterOutputStream(OutputStream out, Deflater def, int size) {
        this(out, def, size, false);
    }

    /**
     * 使用指定的压缩器、刷新模式和默认缓冲区大小创建新的输出流。
     *
     * @param out 输出流
     * @param def 压缩器 ("deflater")
     * @param syncFlush
     *        如果为 {@code true}，则此实例的 {@link #flush()} 方法在刷新输出流之前会以 {@link Deflater#SYNC_FLUSH} 模式刷新压缩器，否则仅刷新输出流
     *
     * @since 1.7
     */
    public DeflaterOutputStream(OutputStream out,
                                Deflater def,
                                boolean syncFlush) {
        this(out, def, 512, syncFlush);
    }


    /**
     * 使用指定的压缩器和默认缓冲区大小创建新的输出流。
     *
     * <p>新的输出流实例的创建方式与调用 3 参数构造函数 DeflaterOutputStream(out, def, false) 相同。
     *
     * @param out 输出流
     * @param def 压缩器 ("deflater")
     */
    public DeflaterOutputStream(OutputStream out, Deflater def) {
        this(out, def, 512, false);
    }

    boolean usesDefaultDeflater = false;


    /**
     * 使用默认压缩器、默认缓冲区大小和指定的刷新模式创建新的输出流。
     *
     * @param out 输出流
     * @param syncFlush
     *        如果为 {@code true}，则此实例的 {@link #flush()} 方法在刷新输出流之前会以 {@link Deflater#SYNC_FLUSH} 模式刷新压缩器，否则仅刷新输出流
     *
     * @since 1.7
     */
    public DeflaterOutputStream(OutputStream out, boolean syncFlush) {
        this(out, new Deflater(), 512, syncFlush);
        usesDefaultDeflater = true;
    }

    /**
     * 使用默认压缩器和缓冲区大小创建新的输出流。
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
     * 完成向输出流写入压缩数据，但不关闭底层流。当对同一输出流连续应用多个过滤器时使用此方法。
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
     * syncFlush} 为 {@code true}，此方法首先使用 {@link Deflater#SYNC_FLUSH} 刷新模式刷新底层 {@code compressor}，
     * 以强制将所有待处理的数据刷新到输出流，然后刷新输出流。否则，此方法仅刷新输出流而不刷新 {@code compressor}。
     *
     * @throws IOException 如果发生 I/O 错误
     *
     * @since 1.7
     */
    public void flush() throws IOException {
        if (syncFlush && !def.finished()) {
            int len = 0;
            while ((len = def.deflate(buf, 0, buf.length, Deflater.SYNC_FLUSH)) > 0)
            {
                out.write(buf, 0, len);
                if (len < buf.length)
                    break;
            }
        }
        out.flush();
    }
}
