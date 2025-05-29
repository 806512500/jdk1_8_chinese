
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

import java.io.OutputStream;
import java.io.IOException;

/**
 * 该类实现了用于写入 GZIP 文件格式压缩数据的流过滤器。
 * @author      David Connelly
 *
 */
public
class GZIPOutputStream extends DeflaterOutputStream {
    /**
     * 未压缩数据的 CRC-32。
     */
    protected CRC32 crc = new CRC32();

    /*
     * GZIP 标头的魔数。
     */
    private final static int GZIP_MAGIC = 0x8b1f;

    /*
     * 尾部的大小（以字节为单位）。
     *
     */
    private final static int TRAILER_SIZE = 8;

    /**
     * 使用指定的缓冲区大小创建新的输出流。
     *
     * <p>新的输出流实例的创建方式与调用 3 参数构造函数 GZIPOutputStream(out, size, false) 相同。
     *
     * @param out 输出流
     * @param size 输出缓冲区大小
     * @exception IOException 如果发生 I/O 错误。
     * @exception IllegalArgumentException 如果 {@code size <= 0}
     */
    public GZIPOutputStream(OutputStream out, int size) throws IOException {
        this(out, size, false);
    }

    /**
     * 使用指定的缓冲区大小和刷新模式创建新的输出流。
     *
     * @param out 输出流
     * @param size 输出缓冲区大小
     * @param syncFlush
     *        如果为 {@code true}，调用此实例继承的
     *        {@link DeflaterOutputStream#flush() flush()} 方法时，会先以
     *        {@link Deflater#SYNC_FLUSH} 模式刷新压缩器，然后再刷新输出流；
     *        否则仅刷新输出流
     * @exception IOException 如果发生 I/O 错误。
     * @exception IllegalArgumentException 如果 {@code size <= 0}
     *
     * @since 1.7
     */
    public GZIPOutputStream(OutputStream out, int size, boolean syncFlush)
        throws IOException
    {
        super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true),
              size,
              syncFlush);
        usesDefaultDeflater = true;
        writeHeader();
        crc.reset();
    }


    /**
     * 使用默认缓冲区大小创建新的输出流。
     *
     * <p>新的输出流实例的创建方式与调用 2 参数构造函数 GZIPOutputStream(out, false) 相同。
     *
     * @param out 输出流
     * @exception IOException 如果发生 I/O 错误。
     */
    public GZIPOutputStream(OutputStream out) throws IOException {
        this(out, 512, false);
    }

    /**
     * 使用默认缓冲区大小和指定的刷新模式创建新的输出流。
     *
     * @param out 输出流
     * @param syncFlush
     *        如果为 {@code true}，调用此实例继承的
     *        {@link DeflaterOutputStream#flush() flush()} 方法时，会先以
     *        {@link Deflater#SYNC_FLUSH} 模式刷新压缩器，然后再刷新输出流；
     *        否则仅刷新输出流
     *
     * @exception IOException 如果发生 I/O 错误。
     *
     * @since 1.7
     */
    public GZIPOutputStream(OutputStream out, boolean syncFlush)
        throws IOException
    {
        this(out, 512, syncFlush);
    }

    /**
     * 将字节数组写入压缩输出流。此方法将阻塞，直到所有字节都被写入。
     * @param buf 要写入的数据
     * @param off 数据的起始偏移量
     * @param len 数据的长度
     * @exception IOException 如果发生 I/O 错误。
     */
    public synchronized void write(byte[] buf, int off, int len)
        throws IOException
    {
        super.write(buf, off, len);
        crc.update(buf, off, len);
    }

    /**
     * 完成向输出流写入压缩数据，但不关闭底层流。当对同一输出流连续应用多个过滤器时使用此方法。
     * @exception IOException 如果发生 I/O 错误
     */
    public void finish() throws IOException {
        if (!def.finished()) {
            try {
            def.finish();
            while (!def.finished()) {
                int len = def.deflate(buf, 0, buf.length);
                if (def.finished() && len <= buf.length - TRAILER_SIZE) {
                    // 最后一个压缩器缓冲区。将尾部放在末尾
                    writeTrailer(buf, len);
                    len = len + TRAILER_SIZE;
                    out.write(buf, 0, len);
                    return;
                }
                if (len > 0)
                    out.write(buf, 0, len);
            }
            // 如果无法将尾部放在最后一个压缩器缓冲区的末尾，我们单独写入它
            byte[] trailer = new byte[TRAILER_SIZE];
            writeTrailer(trailer, 0);
            out.write(trailer);
            } catch (IOException e) {
                if (usesDefaultDeflater)
                    def.end();
                throw e;
            }
        }
    }

    /*
     * 写入 GZIP 成员标头。
     */
    private void writeHeader() throws IOException {
        out.write(new byte[] {
                      (byte) GZIP_MAGIC,        // 魔数（短整型）
                      (byte)(GZIP_MAGIC >> 8),  // 魔数（短整型）
                      Deflater.DEFLATED,        // 压缩方法（CM）
                      0,                        // 标志（FLG）
                      0,                        // 修改时间 MTIME（整型）
                      0,                        // 修改时间 MTIME（整型）
                      0,                        // 修改时间 MTIME（整型）
                      0,                        // 修改时间 MTIME（整型）
                      0,                        // 额外标志（XFLG）
                      0                         // 操作系统（OS）
                  });
    }

                /*
     * 将 GZIP 成员尾部写入字节数组，从给定的偏移量开始。
     */
    private void writeTrailer(byte[] buf, int offset) throws IOException {
        writeInt((int)crc.getValue(), buf, offset); // 未压缩数据的 CRC-32
        writeInt(def.getTotalIn(), buf, offset + 4); // 未压缩字节数
    }

    /*
     * 以 Intel 字节顺序将整数写入字节数组，从给定的偏移量开始。
     */
    private void writeInt(int i, byte[] buf, int offset) throws IOException {
        writeShort(i & 0xffff, buf, offset);
        writeShort((i >> 16) & 0xffff, buf, offset + 2);
    }

    /*
     * 以 Intel 字节顺序将短整数写入字节数组，从给定的偏移量开始。
     */
    private void writeShort(int s, byte[] buf, int offset) throws IOException {
        buf[offset] = (byte)(s & 0xff);
        buf[offset + 1] = (byte)((s >> 8) & 0xff);
    }
}
