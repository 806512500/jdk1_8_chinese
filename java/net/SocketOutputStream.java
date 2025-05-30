/*
 * Copyright (c) 1995, 2016, Oracle and/or its affiliates. All rights reserved.
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

package java.net;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 此流扩展了 FileOutputStream 以实现 SocketOutputStream。请注意，此类不应
 * 是公共的。
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
     * 创建一个新的 SocketOutputStream。只能由 Socket 调用。此方法需要保留所有者 Socket
     * 以防止 fd 被关闭。
     * @param impl 实现的套接字输出流
     */
    SocketOutputStream(AbstractPlainSocketImpl impl) throws IOException {
        super(impl.getFileDescriptor());
        this.impl = impl;
        socket = impl.getSocket();
    }

    /**
     * 返回与此文件输出流关联的唯一的 {@link java.nio.channels.FileChannel FileChannel}
     * 对象。 </p>
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
     * 将数据写入套接字。
     * @param fd 文件描述符
     * @param b 要写入的数据
     * @param off 数据中的起始偏移量
     * @param len 要写入的字节数
     * @exception IOException 如果发生 I/O 错误。
     */
    private native void socketWrite0(FileDescriptor fd, byte[] b, int off,
                                     int len) throws IOException;

    /**
     * 以适当的锁定文件描述符的方式将数据写入套接字。
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
                se = new SocketException("Connection reset");
            }
            if (impl.isClosedOrPending()) {
                throw new SocketException("Socket closed");
            } else {
                throw se;
            }
        } finally {
            impl.releaseFD();
        }
    }

    /**
     * 将一个字节写入套接字。
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
     * 从缓冲区 <i>b</i> 开始写入 <i>length</i> 字节，起始偏移量为 <i>len</i>。
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
