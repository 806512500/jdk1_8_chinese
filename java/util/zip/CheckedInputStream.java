/*
 * 版权所有 (c) 1996, 2006, Oracle 和/或其附属公司。保留所有权利。
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
 * 一个输入流，同时维护正在读取的数据的校验和。
 * 该校验和可用于验证输入数据的完整性。
 *
 * @see         Checksum
 * @author      David Connelly
 */
public
class CheckedInputStream extends FilterInputStream {
    private Checksum cksum;

    /**
     * 使用指定的校验和创建输入流。
     * @param in 输入流
     * @param cksum 校验和
     */
    public CheckedInputStream(InputStream in, Checksum cksum) {
        super(in);
        this.cksum = cksum;
    }

    /**
     * 读取一个字节。如果没有可用的输入，将阻塞。
     * @return 读取的字节，或如果到达流的末尾则返回 -1。
     * @exception IOException 如果发生 I/O 错误
     */
    public int read() throws IOException {
        int b = in.read();
        if (b != -1) {
            cksum.update(b);
        }
        return b;
    }

    /**
     * 读取到字节数组中。如果 <code>len</code> 不为零，该方法
     * 会阻塞直到有输入可用；否则，不读取任何字节并返回 <code>0</code>。
     * @param buf 读取数据的缓冲区
     * @param off 目标数组 <code>b</code> 中的起始偏移量
     * @param len 最大读取的字节数
     * @return 实际读取的字节数，或如果到达流的末尾则返回 -1。
     * @exception  NullPointerException 如果 <code>buf</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，
     * <code>len</code> 为负数，或 <code>len</code> 大于
     * <code>buf.length - off</code>
     * @exception IOException 如果发生 I/O 错误
     */
    public int read(byte[] buf, int off, int len) throws IOException {
        len = in.read(buf, off, len);
        if (len != -1) {
            cksum.update(buf, off, len);
        }
        return len;
    }

    /**
     * 跳过指定数量的输入字节。
     * @param n 要跳过的字节数
     * @return 实际跳过的字节数
     * @exception IOException 如果发生 I/O 错误
     */
    public long skip(long n) throws IOException {
        byte[] buf = new byte[512];
        long total = 0;
        while (total < n) {
            long len = n - total;
            len = read(buf, 0, len < buf.length ? (int)len : buf.length);
            if (len == -1) {
                return total;
            }
            total += len;
        }
        return total;
    }

    /**
     * 返回此输入流的校验和。
     * @return 校验和值
     */
    public Checksum getChecksum() {
        return cksum;
    }
}
