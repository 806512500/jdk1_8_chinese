/*
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

import java.io.*;

/**
 * 一个管道输出流可以连接到一个管道输入流以创建一个通信管道。管道输出流是管道的发送端。通常，数据由一个线程写入
 * <code>PipedOutputStream</code> 对象，而数据由另一个线程从连接的 <code>PipedInputStream</code> 读取。不推荐从单个线程
 * 使用这两个对象，因为可能会导致线程死锁。如果从连接的管道输入流读取数据字节的线程不再存活，则认为管道已 <a name=BROKEN> <i>断开</i> </a>。
 *
 * @author  James Gosling
 * @see     java.io.PipedInputStream
 * @since   JDK1.0
 */
public
class PipedOutputStream extends OutputStream {

        /* REMIND: identification of the read and write sides needs to be
           more sophisticated.  Either using thread groups (but what about
           pipes within a thread?) or using finalization (but it may be a
           long time until the next GC). */
    private PipedInputStream sink;

    /**
     * 创建一个连接到指定管道输入流的管道输出流。写入此流的数据字节将作为输入从 <code>snk</code> 获取。
     *
     * @param      snk   要连接的管道输入流。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public PipedOutputStream(PipedInputStream snk)  throws IOException {
        connect(snk);
    }

    /**
     * 创建一个尚未连接到管道输入流的管道输出流。在使用前，必须由接收方或发送方将其连接到管道输入流。
     *
     * @see     java.io.PipedInputStream#connect(java.io.PipedOutputStream)
     * @see     java.io.PipedOutputStream#connect(java.io.PipedInputStream)
     */
    public PipedOutputStream() {
    }

    /**
     * 将此管道输出流连接到接收方。如果此对象已连接到其他管道输入流，则抛出 <code>IOException</code>。
     * <p>
     * 如果 <code>snk</code> 是一个未连接的管道输入流，<code>src</code> 是一个未连接的管道输出流，它们可以通过以下任一调用连接：
     * <blockquote><pre>
     * src.connect(snk)</pre></blockquote>
     * 或者：
     * <blockquote><pre>
     * snk.connect(src)</pre></blockquote>
     * 两种调用的效果相同。
     *
     * @param      snk   要连接的管道输入流。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public synchronized void connect(PipedInputStream snk) throws IOException {
        if (snk == null) {
            throw new NullPointerException();
        } else if (sink != null || snk.connected) {
            throw new IOException("Already connected");
        }
        sink = snk;
        snk.in = -1;
        snk.out = 0;
        snk.connected = true;
    }

    /**
     * 将指定的 <code>byte</code> 写入管道输出流。
     * <p>
     * 实现 <code>OutputStream</code> 的 <code>write</code> 方法。
     *
     * @param      b   要写入的 <code>byte</code>。
     * @exception IOException 如果管道已 <a href=#BROKEN> 断开</a>、
     *          {@link #connect(java.io.PipedInputStream) 未连接}、关闭，或发生 I/O 错误。
     */
    public void write(int b)  throws IOException {
        if (sink == null) {
            throw new IOException("Pipe not connected");
        }
        sink.receive(b);
    }

    /**
     * 从指定的字节数组中写入 <code>len</code> 个字节，从偏移量 <code>off</code> 开始，写入此管道输出流。此方法会阻塞，直到所有字节都写入输出流。
     *
     * @param      b     数据。
     * @param      off   数据中的起始偏移量。
     * @param      len   要写入的字节数。
     * @exception IOException 如果管道已 <a href=#BROKEN> 断开</a>、
     *          {@link #connect(java.io.PipedInputStream) 未连接}、关闭，或发生 I/O 错误。
     */
    public void write(byte b[], int off, int len) throws IOException {
        if (sink == null) {
            throw new IOException("Pipe not connected");
        } else if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        sink.receive(b, off, len);
    }

    /**
     * 刷新此输出流并强制任何缓冲的输出字节写入。
     * 这将通知任何读取者管道中有字节等待。
     *
     * @exception IOException 如果发生 I/O 错误。
     */
    public synchronized void flush() throws IOException {
        if (sink != null) {
            synchronized (sink) {
                sink.notifyAll();
            }
        }
    }

    /**
     * 关闭此管道输出流并释放与此流关联的任何系统资源。此后不能再使用此流写入字节。
     *
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void close()  throws IOException {
        if (sink != null) {
            sink.receivedLast();
        }
    }
}
