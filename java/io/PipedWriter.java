/*
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
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


/**
 * 管道字符输出流。
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class PipedWriter extends Writer {

    /* 提醒：读取和写入端的识别需要更加复杂。可以使用线程组（但管道内的线程怎么办？）或使用终结（但可能需要很长时间才能进行下一次垃圾回收）。 */
    private PipedReader sink;

    /* 此标志记录此特定写入器的打开状态。它独立于PipedReader中定义的状态标志。用于连接时的合理性检查。 */
    private boolean closed = false;

    /**
     * 创建一个连接到指定管道读取器的管道写入器。写入此流的数据字符将作为输入从<code>snk</code>中获取。
     *
     * @param      snk   要连接的管道读取器。
     * @exception  IOException  如果发生I/O错误。
     */
    public PipedWriter(PipedReader snk)  throws IOException {
        connect(snk);
    }

    /**
     * 创建一个尚未连接到管道读取器的管道写入器。在使用之前，必须由接收方或发送方将其连接到管道读取器。
     *
     * @see     java.io.PipedReader#connect(java.io.PipedWriter)
     * @see     java.io.PipedWriter#connect(java.io.PipedReader)
     */
    public PipedWriter() {
    }

    /**
     * 将此管道写入器连接到接收器。如果此对象已连接到其他管道读取器，则抛出<code>IOException</code>。
     * <p>
     * 如果<code>snk</code>是未连接的管道读取器且<code>src</code>是未连接的管道写入器，它们可以通过以下任一调用连接：
     * <blockquote><pre>
     * src.connect(snk)</pre></blockquote>
     * 或者：
     * <blockquote><pre>
     * snk.connect(src)</pre></blockquote>
     * 两种调用的效果相同。
     *
     * @param      snk   要连接的管道读取器。
     * @exception  IOException  如果发生I/O错误。
     */
    public synchronized void connect(PipedReader snk) throws IOException {
        if (snk == null) {
            throw new NullPointerException();
        } else if (sink != null || snk.connected) {
            throw new IOException("Already connected");
        } else if (snk.closedByReader || closed) {
            throw new IOException("Pipe closed");
        }

        sink = snk;
        snk.in = -1;
        snk.out = 0;
        snk.connected = true;
    }

    /**
     * 将指定的<code>char</code>写入管道输出流。
     * 如果一个线程正在从连接的管道输入流中读取数据字符，但该线程已不再存活，则抛出<code>IOException</code>。
     * <p>
     * 实现<code>Writer</code>的<code>write</code>方法。
     *
     * @param      c   要写入的<code>char</code>。
     * @exception  IOException  如果管道
     *          <a href=PipedOutputStream.html#BROKEN> <code>broken</code></a>，
     *          {@link #connect(java.io.PipedReader) 未连接}，已关闭或发生I/O错误。
     */
    public void write(int c)  throws IOException {
        if (sink == null) {
            throw new IOException("Pipe not connected");
        }
        sink.receive(c);
    }

    /**
     * 从指定的字符数组中写入<code>len</code>个字符，从偏移量<code>off</code>开始写入此管道输出流。
     * 此方法会阻塞，直到所有字符都被写入输出流。
     * 如果一个线程正在从连接的管道输入流中读取数据字符，但该线程已不再存活，则抛出<code>IOException</code>。
     *
     * @param      cbuf  数据。
     * @param      off   数据中的起始偏移量。
     * @param      len   要写入的字符数。
     * @exception  IOException  如果管道
     *          <a href=PipedOutputStream.html#BROKEN> <code>broken</code></a>，
     *          {@link #connect(java.io.PipedReader) 未连接}，已关闭或发生I/O错误。
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        if (sink == null) {
            throw new IOException("Pipe not connected");
        } else if ((off | len | (off + len) | (cbuf.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        }
        sink.receive(cbuf, off, len);
    }

    /**
     * 刷新此输出流并强制任何缓冲的输出字符被写入。
     * 这将通知任何读取器管道中有字符等待。
     *
     * @exception  IOException  如果管道已关闭或发生I/O错误。
     */
    public synchronized void flush() throws IOException {
        if (sink != null) {
            if (sink.closedByReader || closed) {
                throw new IOException("Pipe closed");
            }
            synchronized (sink) {
                sink.notifyAll();
            }
        }
    }

    /**
     * 关闭此管道输出流并释放与此流关联的任何系统资源。此流将不能再用于写入字符。
     *
     * @exception  IOException  如果发生I/O错误。
     */
    public void close()  throws IOException {
        closed = true;
        if (sink != null) {
            sink.receivedLast();
        }
    }
}
