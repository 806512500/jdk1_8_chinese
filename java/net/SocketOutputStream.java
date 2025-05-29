/*
 * 版权所有 (c) 1995, 2016，Oracle 和/或其附属公司。保留所有权利。
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 此流扩展了 FileOutputStream 以实现 SocketOutputStream。请注意，此类应<b>不</b>是公共的。
 *
 * @author      Jonathan Payne
 * @author      Arthur van Hoff
 */
class SocketOutputStream extends FileOutputStream
{
    static {
        init();
    }

    private AbstractPlainSocketImpl impl = null;
    private byte temp[] = new byte[1];
    private Socket socket = null;

    /**
     * 创建一个新的 SocketOutputStream。只能由 Socket 调用。此方法需要保留所有者 Socket 以防止 fd 被关闭。
     * @param impl 实现的套接字输出流
     */
    SocketOutputStream(AbstractPlainSocketImpl impl) throws IOException {
        super(impl.getFileDescriptor());
        this.impl = impl;
        socket = impl.getSocket();
    }

    /**
     * 返回与此文件输出流关联的唯一 {@link java.nio.channels.FileChannel FileChannel} 对象。 </p>
     *
     * {@code SocketOutputStream} 的 {@code getChannel} 方法返回 {@code null}，因为它是基于套接字的流。</p>
     *
     * @return 与此文件输出流关联的文件通道
     *
     * @since 1.4
     * @spec JSR-51
     */
    public final FileChannel getChannel() {
        return null;
    }

    /**
     * 向套接字写入数据。
     * @param fd 文件描述符
     * @param b 要写入的数据
     * @param off 数据中的起始偏移量
     * @param len 要写入的字节数
     * @exception IOException 如果发生 I/O 错误。
     */
    private native void socketWrite0(FileDescriptor fd, byte[] b, int off,
                                     int len) throws IOException;

    /**
     * 以适当的文件描述符锁定向套接字写入数据。
     * @param b 要写入的数据
     * @param off 数据中的起始偏移量
     * @param len 要写入的字节数
     * @exception IOException 如果发生 I/O 错误。
     */
    private void socketWrite(byte b[], int off, int len) throws IOException {


        if (len <= 0 || off < 0 || len > b.length - off) {
            if (len == 0) {
                return;
            }
            throw new ArrayIndexOutOfBoundsException("len == " + len
                    + " off == " + off + " buffer length == " + b.length);
        }

        FileDescriptor fd = impl.acquireFD();
        try {
            socketWrite0(fd, b, off, len);
        } catch (SocketException se) {
            if (se instanceof sun.net.ConnectionResetException) {
                impl.setConnectionResetPending();
                se = new SocketException("连接重置");
            }
            if (impl.isClosedOrPending()) {
                throw new SocketException("套接字已关闭");
            } else {
                throw se;
            }
        } finally {
            impl.releaseFD();
        }
    }

    /**
     * 向套接字写入一个字节。
     * @param b 要写入的数据
     * @exception IOException 如果发生 I/O 错误。
     */
    public void write(int b) throws IOException {
        temp[0] = (byte)b;
        socketWrite(temp, 0, 1);
    }

    /**
     * 将缓冲区 <i>b</i> 的内容写入套接字。
     * @param b 要写入的数据
     * @exception SocketException 如果发生 I/O 错误。
     */
    public void write(byte b[]) throws IOException {
        socketWrite(b, 0, b.length);
    }

    /**
     * 从缓冲区 <i>b</i> 开始在偏移量 <i>len</i> 处写入 <i>length</i> 字节。
     * @param b 要写入的数据
     * @param off 数据中的起始偏移量
     * @param len 要写入的字节数
     * @exception SocketException 如果发生 I/O 错误。
     */
    public void write(byte b[], int off, int len) throws IOException {
        socketWrite(b, off, len);
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

    /**
     * 覆盖 finalize，fd 由 Socket 关闭。
     */
    protected void finalize() {}

    /**
     * 执行类加载时的初始化。
     */
    private native static void init();

}
