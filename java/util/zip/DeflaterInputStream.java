
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

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * 实现了一个用于压缩数据的输入流过滤器，使用 "deflate" 压缩格式。
 *
 * @since       1.6
 * @author      David R Tribble (david@tribble.com)
 *
 * @see DeflaterOutputStream
 * @see InflaterOutputStream
 * @see InflaterInputStream
 */

public class DeflaterInputStream extends FilterInputStream {
    /** 该流的压缩器。 */
    protected final Deflater def;

    /** 用于读取压缩数据的输入缓冲区。 */
    protected final byte[] buf;

    /** 临时读取缓冲区。 */
    private byte[] rbuf = new byte[1];

    /** 使用默认压缩器。 */
    private boolean usesDefaultDeflater = false;

    /** 底层输入流已到达末尾。 */
    private boolean reachEOF = false;

    /**
     * 检查此流是否已关闭。
     */
    private void ensureOpen() throws IOException {
        if (in == null) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * 创建一个新的输入流，使用默认的压缩器和缓冲区大小。
     *
     * @param in 用于读取未压缩数据的输入流
     * @throws NullPointerException 如果 {@code in} 为 null
     */
    public DeflaterInputStream(InputStream in) {
        this(in, new Deflater());
        usesDefaultDeflater = true;
    }

    /**
     * 创建一个新的输入流，使用指定的压缩器和默认的缓冲区大小。
     *
     * @param in 用于读取未压缩数据的输入流
     * @param defl 该流的压缩器（"deflater"）
     * @throws NullPointerException 如果 {@code in} 或 {@code defl} 为 null
     */
    public DeflaterInputStream(InputStream in, Deflater defl) {
        this(in, defl, 512);
    }

    /**
     * 创建一个新的输入流，使用指定的压缩器和缓冲区大小。
     *
     * @param in 用于读取未压缩数据的输入流
     * @param defl 该流的压缩器（"deflater"）
     * @param bufLen 压缩缓冲区大小
     * @throws IllegalArgumentException 如果 {@code bufLen <= 0}
     * @throws NullPointerException 如果 {@code in} 或 {@code defl} 为 null
     */
    public DeflaterInputStream(InputStream in, Deflater defl, int bufLen) {
        super(in);

        // 检查
        if (in == null)
            throw new NullPointerException("Null input");
        if (defl == null)
            throw new NullPointerException("Null deflater");
        if (bufLen < 1)
            throw new IllegalArgumentException("Buffer size < 1");

        // 初始化
        def = defl;
        buf = new byte[bufLen];
    }

    /**
     * 关闭此输入流及其底层输入流，丢弃任何待处理的未压缩数据。
     *
     * @throws IOException 如果发生 I/O 错误
     */
    public void close() throws IOException {
        if (in != null) {
            try {
                // 清理
                if (usesDefaultDeflater) {
                    def.end();
                }

                in.close();
            } finally {
                in = null;
            }
        }
    }

    /**
     * 从输入流中读取一个字节的压缩数据。
     * 此方法将阻塞，直到可以读取并压缩一些输入。
     *
     * @return 一个字节的压缩数据，或如果到达未压缩输入流的末尾则返回 -1
     * @throws IOException 如果发生 I/O 错误或此流已关闭
     */
    public int read() throws IOException {
        // 读取一个字节的压缩数据
        int len = read(rbuf, 0, 1);
        if (len <= 0)
            return -1;
        return (rbuf[0] & 0xFF);
    }

    /**
     * 将压缩数据读取到字节数组中。
     * 此方法将阻塞，直到可以读取并压缩一些输入。
     *
     * @param b 读取数据的缓冲区
     * @param off 数据在 {@code b} 中的起始偏移量
     * @param len 要读取到 {@code b} 中的最大压缩字节数
     * @return 实际读取的字节数，或如果到达未压缩输入流的末尾则返回 -1
     * @throws IndexOutOfBoundsException 如果 {@code len > b.length - off}
     * @throws IOException 如果发生 I/O 错误或此输入流已关闭
     */
    public int read(byte[] b, int off, int len) throws IOException {
        // 检查
        ensureOpen();
        if (b == null) {
            throw new NullPointerException("Null buffer for read");
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        // 读取并压缩（deflate）输入数据字节
        int cnt = 0;
        while (len > 0 && !def.finished()) {
            int n;

            // 从输入流中读取数据
            if (def.needsInput()) {
                n = in.read(buf, 0, buf.length);
                if (n < 0) {
                    // 到达输入流的末尾
                    def.finish();
                } else if (n > 0) {
                    def.setInput(buf, 0, n);
                }
            }

            // 压缩输入数据，填充读取缓冲区
            n = def.deflate(b, off, len);
            cnt += n;
            off += n;
            len -= n;
        }
        if (cnt == 0 && def.finished()) {
            reachEOF = true;
            cnt = -1;
        }

        return cnt;
    }

    /**
     * 从输入流中跳过并丢弃数据。
     * 此方法可能阻塞，直到读取并跳过指定的字节数。注意：虽然 {@code n} 被指定为 {@code long}，
     * 但可以跳过的最大字节数为 {@code Integer.MAX_VALUE}。
     *
     * @param n 要跳过的字节数
     * @return 实际跳过的字节数
     * @throws IOException 如果发生 I/O 错误或此流已关闭
     */
    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("negative skip length");
        }
        ensureOpen();


                    // 通过反复解压缩小块来跳过字节
        if (rbuf.length < 512)
            rbuf = new byte[512];

        int total = (int)Math.min(n, Integer.MAX_VALUE);
        long cnt = 0;
        while (total > 0) {
            // 读取一小块未压缩的字节
            int len = read(rbuf, 0, (total <= rbuf.length ? total : rbuf.length));

            if (len < 0) {
                break;
            }
            cnt += len;
            total -= len;
        }
        return cnt;
    }

    /**
     * 在达到EOF后返回0，否则总是返回1。
     * <p>
     * 程序不应依赖此方法返回在不阻塞的情况下可以读取的实际字节数
     * @return 在达到底层输入流的末尾后返回零，否则总是返回1
     * @throws IOException 如果发生I/O错误或此流已关闭
     */
    public int available() throws IOException {
        ensureOpen();
        if (reachEOF) {
            return 0;
        }
        return 1;
    }

    /**
     * 始终返回 {@code false}，因为此输入流不支持
     * {@link #mark mark()} 和 {@link #reset reset()} 方法。
     *
     * @return 始终返回false
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * <i>此操作不受支持</i>。
     *
     * @param limit 在使位置标记失效之前可以读取的最大字节数
     */
    public void mark(int limit) {
        // 操作不受支持
    }

    /**
     * <i>此操作不受支持</i>。
     *
     * @throws IOException 始终抛出
     */
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
}
