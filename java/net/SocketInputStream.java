/*
 * 版权所有 (c) 1995, 2016，Oracle 及/或其附属公司。保留所有权利。
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

package java.net;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import sun.net.ConnectionResetException;

/**
 * 此流扩展了 FileInputStream 以实现 SocketInputStream。请注意，此类不应
 * 为公共类。
 *
 * @author      Jonathan Payne
 * @author      Arthur van Hoff
 */
class SocketInputStream extends FileInputStream
{
    static {
        init();
    }

    private boolean eof;
    private AbstractPlainSocketImpl impl = null;
    private byte temp[];
    private Socket socket = null;

    /**
     * 创建一个新的 SocketInputStream。只能由 Socket 调用。
     * 此方法需要保留所有者 Socket 以防止 fd 被关闭。
     * @param impl 实现的套接字输入流
     */
    SocketInputStream(AbstractPlainSocketImpl impl) throws IOException {
        super(impl.getFileDescriptor());
        this.impl = impl;
        socket = impl.getSocket();
    }

    /**
     * 返回与此文件输入流关联的唯一 {@link java.nio.channels.FileChannel FileChannel}
     * 对象。</p>
     *
     * {@code SocketInputStream} 的 {@code getChannel} 方法返回 {@code null}，因为它是一个基于套接字的流。</p>
     *
     * @return 与此文件输入流关联的文件通道
     *
     * @since 1.4
     * @spec JSR-51
     */
    public final FileChannel getChannel() {
        return null;
    }

    /**
     * 使用接收到的套接字原语在指定偏移处读取到字节数组中。
     * @param fd 文件描述符
     * @param b 读取数据的缓冲区
     * @param off 数据的起始偏移
     * @param len 读取的最大字节数
     * @param timeout 读取超时时间（毫秒）
     * @return 实际读取的字节数，到达流末尾时返回 -1。
     * @exception IOException 如果发生 I/O 错误。
     */
    private native int socketRead0(FileDescriptor fd,
                                   byte b[], int off, int len,
                                   int timeout)
        throws IOException;

    // 包装原生调用以允许仪器化
    /**
     * 使用接收到的套接字原语在指定偏移处读取到字节数组中。
     * @param fd 文件描述符
     * @param b 读取数据的缓冲区
     * @param off 数据的起始偏移
     * @param len 读取的最大字节数
     * @param timeout 读取超时时间（毫秒）
     * @return 实际读取的字节数，到达流末尾时返回 -1。
     * @exception IOException 如果发生 I/O 错误。
     */
    private int socketRead(FileDescriptor fd,
                           byte b[], int off, int len,
                           int timeout)
        throws IOException {
        return socketRead0(fd, b, off, len, timeout);
    }

    /**
     * 从套接字读取数据到字节数组中。
     * @param b 读取数据的缓冲区
     * @return 实际读取的字节数，到达流末尾时返回 -1。
     * @exception IOException 如果发生 I/O 错误。
     */
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * 从字节数组 <i>b</i> 的偏移 <i>off</i> 处读取 <i>length</i> 字节的数据。
     * @param b 读取数据的缓冲区
     * @param off 数据的起始偏移
     * @param length 读取的最大字节数
     * @return 实际读取的字节数，到达流末尾时返回 -1。
     * @exception IOException 如果发生 I/O 错误。
     */
    public int read(byte b[], int off, int length) throws IOException {
        return read(b, off, length, impl.getTimeout());
    }

    int read(byte b[], int off, int length, int timeout) throws IOException {
        int n;

        // 已经遇到 EOF
        if (eof) {
            return -1;
        }

        // 连接重置
        if (impl.isConnectionReset()) {
            throw new SocketException("连接重置");
        }

        // 边界检查
        if (length <= 0 || off < 0 || length > b.length - off) {
            if (length == 0) {
                return 0;
            }
            throw new ArrayIndexOutOfBoundsException("length == " + length
                    + " off == " + off + " 缓冲区长度 == " + b.length);
        }

        boolean gotReset = false;

        // 获取文件描述符并执行读取
        FileDescriptor fd = impl.acquireFD();
        try {
            n = socketRead(fd, b, off, length, timeout);
            if (n > 0) {
                return n;
            }
        } catch (ConnectionResetException rstExc) {
            gotReset = true;
        } finally {
            impl.releaseFD();
        }

        /*
         * 收到“连接重置”，但套接字上可能仍有缓冲的字节
         */
        if (gotReset) {
            impl.setConnectionResetPending();
            impl.acquireFD();
            try {
                n = socketRead(fd, b, off, length, timeout);
                if (n > 0) {
                    return n;
                }
            } catch (ConnectionResetException rstExc) {
            } finally {
                impl.releaseFD();
            }
        }

        /*
         * 如果到达这里，我们处于 EOF，套接字已被关闭，
         * 或者连接已被重置。
         */
        if (impl.isClosedOrPending()) {
            throw new SocketException("套接字已关闭");
        }
        if (impl.isConnectionResetPending()) {
            impl.setConnectionReset();
        }
        if (impl.isConnectionReset()) {
            throw new SocketException("连接重置");
        }
        eof = true;
        return -1;
    }

}

                /**
     * 从套接字中读取一个字节。
     */
    public int read() throws IOException {
        if (eof) {
            return -1;
        }
        temp = new byte[1];
        int n = read(temp, 0, 1);
        if (n <= 0) {
            return -1;
        }
        return temp[0] & 0xff;
    }

    /**
     * 跳过 n 个输入字节。
     * @param numbytes 要跳过的字节数
     * @return 实际跳过的字节数。
     * @exception IOException 如果发生 I/O 错误。
     */
    public long skip(long numbytes) throws IOException {
        if (numbytes <= 0) {
            return 0;
        }
        long n = numbytes;
        int buflen = (int) Math.min(1024, n);
        byte data[] = new byte[buflen];
        while (n > 0) {
            int r = read(data, 0, (int) Math.min((long) buflen, n));
            if (r < 0) {
                break;
            }
            n -= r;
        }
        return numbytes - n;
    }

    /**
     * 返回可以不阻塞读取的字节数。
     * @return 立即可用的字节数
     */
    public int available() throws IOException {
        return impl.available();
    }

    /**
     * 关闭流。
     */
    private boolean closing = false;
    public void close() throws IOException {
        // 防止递归。参见 BugId 4484411
        if (closing)
            return;
        closing = true;
        if (socket != null) {
            if (!socket.isClosed())
                socket.close();
        } else
            impl.close();
        closing = false;
    }

    void setEOF(boolean eof) {
        this.eof = eof;
    }

    /**
     * 覆盖 finalize，fd 由 Socket 关闭。
     */
    protected void finalize() {}

    /**
     * 执行类加载时的初始化。
     */
    private native static void init();
}
