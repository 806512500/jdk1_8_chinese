/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 管道字符输入流。
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class PipedReader extends Reader {
    boolean closedByWriter = false;
    boolean closedByReader = false;
    boolean connected = false;

    /* 提醒：读取和写入端的识别需要更加复杂。可以使用线程组（但管道内的线程怎么办？）或使用终结化（但可能需要很长时间才能进行下一次垃圾回收）。 */
    Thread readSide;
    Thread writeSide;

   /**
    * 管道的循环输入缓冲区大小。
    */
    private static final int DEFAULT_PIPE_SIZE = 1024;

    /**
     * 用于存储传入数据的循环缓冲区。
     */
    char buffer[];

    /**
     * 循环缓冲区中将要存储的下一个字符数据的位置索引。<code>in&lt;0</code> 表示缓冲区为空，<code>in==out</code> 表示缓冲区已满。
     */
    int in = -1;

    /**
     * 从这个管道读取器中读取的下一个字符数据在循环缓冲区中的位置索引。
     */
    int out = 0;

    /**
     * 创建一个 <code>PipedReader</code>，使其连接到管道写入器 <code>src</code>。写入到 <code>src</code> 的数据将作为输入从此流中可用。
     *
     * @param      src   要连接的流。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public PipedReader(PipedWriter src) throws IOException {
        this(src, DEFAULT_PIPE_SIZE);
    }

    /**
     * 创建一个 <code>PipedReader</code>，使其连接到管道写入器 <code>src</code> 并使用指定的管道缓冲区大小。写入到 <code>src</code> 的数据将作为输入从此流中可用。
     *
     * @param      src       要连接的流。
     * @param      pipeSize  管道缓冲区的大小。
     * @exception  IOException  如果发生 I/O 错误。
     * @exception  IllegalArgumentException 如果 {@code pipeSize <= 0}。
     * @since      1.6
     */
    public PipedReader(PipedWriter src, int pipeSize) throws IOException {
        initPipe(pipeSize);
        connect(src);
    }


    /**
     * 创建一个 <code>PipedReader</code>，使其尚未 {@linkplain #connect(java.io.PipedWriter) 连接}。必须在使用前 {@linkplain java.io.PipedWriter#connect(
     * java.io.PipedReader) 连接到} 一个 <code>PipedWriter</code>。
     */
    public PipedReader() {
        initPipe(DEFAULT_PIPE_SIZE);
    }

    /**
     * 创建一个 <code>PipedReader</code>，使其尚未 {@link #connect(java.io.PipedWriter) 连接} 并使用指定的管道缓冲区大小。必须在使用前 {@linkplain java.io.PipedWriter#connect(
     * java.io.PipedReader) 连接到} 一个 <code>PipedWriter</code>。
     *
     * @param   pipeSize 管道缓冲区的大小。
     * @exception  IllegalArgumentException 如果 {@code pipeSize <= 0}。
     * @since      1.6
     */
    public PipedReader(int pipeSize) {
        initPipe(pipeSize);
    }

    private void initPipe(int pipeSize) {
        if (pipeSize <= 0) {
            throw new IllegalArgumentException("管道大小 <= 0");
        }
        buffer = new char[pipeSize];
    }

    /**
     * 使此管道读取器连接到管道写入器 <code>src</code>。如果此对象已连接到其他管道写入器，则抛出 <code>IOException</code>。
     * <p>
     * 如果 <code>src</code> 是未连接的管道写入器且 <code>snk</code> 是未连接的管道读取器，它们可以通过以下调用之一连接：
     *
     * <pre><code>snk.connect(src)</code> </pre>
     * <p>
     * 或者：
     *
     * <pre><code>src.connect(snk)</code> </pre>
     * <p>
     * 这两个调用具有相同的效果。
     *
     * @param      src   要连接的管道写入器。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void connect(PipedWriter src) throws IOException {
        src.connect(this);
    }

    /**
     * 接收一个字符数据。如果没有可用的输入，此方法将阻塞。
     */
    synchronized void receive(int c) throws IOException {
        if (!connected) {
            throw new IOException("管道未连接");
        } else if (closedByWriter || closedByReader) {
            throw new IOException("管道已关闭");
        } else if (readSide != null && !readSide.isAlive()) {
            throw new IOException("读取端已死");
        }

        writeSide = Thread.currentThread();
        while (in == out) {
            if ((readSide != null) && !readSide.isAlive()) {
                throw new IOException("管道已断开");
            }
            /* 已满：唤醒任何等待的读取者 */
            notifyAll();
            try {
                wait(1000);
            } catch (InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
        if (in < 0) {
            in = 0;
            out = 0;
        }
        buffer[in++] = (char) c;
        if (in >= buffer.length) {
            in = 0;
        }
    }

    /**
     * 将数据接收进字符数组。如果没有可用的输入，此方法将阻塞。
     */
    synchronized void receive(char c[], int off, int len)  throws IOException {
        while (--len >= 0) {
            receive(c[off++]);
        }
    }

    /**
     * 通知所有等待的线程，最后一个字符数据已接收。
     */
    synchronized void receivedLast() {
        closedByWriter = true;
        notifyAll();
    }

    /**
     * 从这个管道流中读取下一个字符数据。如果因为到达流的末尾而没有可用的字符，返回值为 <code>-1</code>。此方法在有输入数据可用、检测到流的末尾或抛出异常时阻塞。
     *
     * @return     下一个字符数据，或如果到达流的末尾则返回 <code>-1</code>。
     * @exception  IOException  如果管道 <a href=PipedInputStream.html#BROKEN> <code>已断开</code></a>、
     *          {@link #connect(java.io.PipedWriter) 未连接}、已关闭或发生 I/O 错误。
     */
    public synchronized int read()  throws IOException {
        if (!connected) {
            throw new IOException("管道未连接");
        } else if (closedByReader) {
            throw new IOException("管道已关闭");
        } else if (writeSide != null && !writeSide.isAlive()
                   && !closedByWriter && (in < 0)) {
            throw new IOException("写入端已死");
        }

        readSide = Thread.currentThread();
        int trials = 2;
        while (in < 0) {
            if (closedByWriter) {
                /* 写入端已关闭，返回 EOF */
                return -1;
            }
            if ((writeSide != null) && (!writeSide.isAlive()) && (--trials < 0)) {
                throw new IOException("管道已断开");
            }
            /* 可能有等待的写入者 */
            notifyAll();
            try {
                wait(1000);
            } catch (InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
        int ret = buffer[out++];
        if (out >= buffer.length) {
            out = 0;
        }
        if (in == out) {
            /* 现在为空 */
            in = -1;
        }
        return ret;
    }

    /**
     * 从这个管道流中读取最多 <code>len</code> 个字符数据到字符数组中。如果到达数据流的末尾或 <code>len</code> 超过管道的缓冲区大小，读取的字符将少于 <code>len</code> 个。此方法在至少有一个字符的输入可用时阻塞。
     *
     * @param      cbuf     读取数据的缓冲区。
     * @param      off   数据的起始偏移量。
     * @param      len   最大读取的字符数。
     * @return     读取到缓冲区中的字符总数，或如果因为到达流的末尾而没有更多数据则返回 <code>-1</code>。
     * @exception  IOException  如果管道 <a href=PipedInputStream.html#BROKEN> <code>已断开</code></a>、
     *                  {@link #connect(java.io.PipedWriter) 未连接}、已关闭或发生 I/O 错误。
     */
    public synchronized int read(char cbuf[], int off, int len)  throws IOException {
        if (!connected) {
            throw new IOException("管道未连接");
        } else if (closedByReader) {
            throw new IOException("管道已关闭");
        } else if (writeSide != null && !writeSide.isAlive()
                   && !closedByWriter && (in < 0)) {
            throw new IOException("写入端已死");
        }

        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
            ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        /* 可能需要等待第一个字符 */
        int c = read();
        if (c < 0) {
            return -1;
        }
        cbuf[off] =  (char)c;
        int rlen = 1;
        while ((in >= 0) && (--len > 0)) {
            cbuf[off + rlen] = buffer[out++];
            rlen++;
            if (out >= buffer.length) {
                out = 0;
            }
            if (in == out) {
                /* 现在为空 */
                in = -1;
            }
        }
        return rlen;
    }

    /**
     * 告知此流是否准备好读取。如果循环缓冲区不为空，则管道字符流已准备好。
     *
     * @exception  IOException  如果管道 <a href=PipedInputStream.html#BROKEN> <code>已断开</code></a>、
     *                  {@link #connect(java.io.PipedWriter) 未连接} 或已关闭。
     */
    public synchronized boolean ready() throws IOException {
        if (!connected) {
            throw new IOException("管道未连接");
        } else if (closedByReader) {
            throw new IOException("管道已关闭");
        } else if (writeSide != null && !writeSide.isAlive()
                   && !closedByWriter && (in < 0)) {
            throw new IOException("写入端已死");
        }
        if (in < 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 关闭此管道流并释放与此流关联的系统资源。
     *
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void close()  throws IOException {
        in = -1;
        closedByReader = true;
    }
}
