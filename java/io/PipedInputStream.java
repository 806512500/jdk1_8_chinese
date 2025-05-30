
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 一个管道输入流应连接到一个管道输出流；管道输入流然后提供从管道输出流写入的所有数据字节。
 * 通常，数据由一个线程从 <code>PipedInputStream</code> 对象读取，而数据由另一个线程写入对应的 <code>PipedOutputStream</code>。
 * 不推荐从单个线程同时使用这两个对象，因为它可能会使线程死锁。
 * 管道输入流包含一个缓冲区，将读操作与写操作解耦，但有一定的限制。
 * 如果向连接的管道输出流提供数据字节的线程不再存活，则管道被认为是 <a name="BROKEN"> <i>断开的</i> </a>。
 *
 * @author  James Gosling
 * @see     java.io.PipedOutputStream
 * @since   JDK1.0
 */
public class PipedInputStream extends InputStream {
    boolean closedByWriter = false;
    volatile boolean closedByReader = false;
    boolean connected = false;

        /* REMIND: identification of the read and write sides needs to be
           more sophisticated.  Either using thread groups (but what about
           pipes within a thread?) or using finalization (but it may be a
           long time until the next GC). */
    Thread readSide;
    Thread writeSide;

    private static final int DEFAULT_PIPE_SIZE = 1024;

    /**
     * 管道的默认循环输入缓冲区大小。
     * @since   JDK1.1
     */
    // 这个字段以前是一个常量，在允许管道大小变化之前。
    // 为了向后兼容，这个字段将继续被维护。
    protected static final int PIPE_SIZE = DEFAULT_PIPE_SIZE;

    /**
     * 用于存储传入数据的循环缓冲区。
     * @since   JDK1.1
     */
    protected byte buffer[];

    /**
     * 循环缓冲区中下一个字节数据将被存储的位置的索引。
     * <code>in&lt;0</code> 表示缓冲区为空，<code>in==out</code> 表示缓冲区已满。
     * @since   JDK1.1
     */
    protected int in = -1;

    /**
     * 从该管道输入流读取下一个字节数据时，循环缓冲区中的位置索引。
     * @since   JDK1.1
     */
    protected int out = 0;

    /**
     * 创建一个 <code>PipedInputStream</code>，使其连接到管道输出流 <code>src</code>。
     * 写入 <code>src</code> 的数据字节将作为输入从此流中可用。
     *
     * @param      src   要连接的流。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public PipedInputStream(PipedOutputStream src) throws IOException {
        this(src, DEFAULT_PIPE_SIZE);
    }

    /**
     * 创建一个 <code>PipedInputStream</code>，使其连接到管道输出流 <code>src</code> 并使用指定的管道缓冲区大小。
     * 写入 <code>src</code> 的数据字节将作为输入从此流中可用。
     *
     * @param      src   要连接的流。
     * @param      pipeSize 管道缓冲区的大小。
     * @exception  IOException  如果发生 I/O 错误。
     * @exception  IllegalArgumentException 如果 {@code pipeSize <= 0}。
     * @since      1.6
     */
    public PipedInputStream(PipedOutputStream src, int pipeSize)
            throws IOException {
         initPipe(pipeSize);
         connect(src);
    }

    /**
     * 创建一个 <code>PipedInputStream</code>，使其尚未 {@linkplain #connect(java.io.PipedOutputStream) 连接}。
     * 在使用之前，必须将其 {@linkplain java.io.PipedOutputStream#connect(
     * java.io.PipedInputStream) 连接} 到一个 <code>PipedOutputStream</code>。
     */
    public PipedInputStream() {
        initPipe(DEFAULT_PIPE_SIZE);
    }

    /**
     * 创建一个 <code>PipedInputStream</code>，使其尚未 {@linkplain #connect(java.io.PipedOutputStream) 连接} 并使用指定的管道缓冲区大小。
     * 在使用之前，必须将其 {@linkplain java.io.PipedOutputStream#connect(
     * java.io.PipedInputStream) 连接} 到一个 <code>PipedOutputStream</code>。
     *
     * @param      pipeSize 管道缓冲区的大小。
     * @exception  IllegalArgumentException 如果 {@code pipeSize <= 0}。
     * @since      1.6
     */
    public PipedInputStream(int pipeSize) {
        initPipe(pipeSize);
    }

    private void initPipe(int pipeSize) {
         if (pipeSize <= 0) {
            throw new IllegalArgumentException("Pipe Size <= 0");
         }
         buffer = new byte[pipeSize];
    }

    /**
     * 使此管道输入流连接到管道输出流 <code>src</code>。
     * 如果此对象已经连接到其他管道输出流，则会抛出 <code>IOException</code>。
     * <p>
     * 如果 <code>src</code> 是一个未连接的管道输出流，而 <code>snk</code> 是一个未连接的管道输入流，它们
     * 可以通过以下调用之一连接：
     *
     * <pre><code>snk.connect(src)</code> </pre>
     * <p>
     * 或者：
     *
     * <pre><code>src.connect(snk)</code> </pre>
     * <p>
     * 这两个调用具有相同的效果。
     *
     * @param      src   要连接的管道输出流。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void connect(PipedOutputStream src) throws IOException {
        src.connect(this);
    }

    /**
     * 接收一个字节的数据。如果无输入可用，此方法将阻塞。
     * @param b 接收的字节
     * @exception IOException 如果管道 <a href="#BROKEN"> <code>断开</code></a>、
     *          {@link #connect(java.io.PipedOutputStream) 未连接}、已关闭，或发生 I/O 错误。
     * @since     JDK1.1
     */
    protected synchronized void receive(int b) throws IOException {
        checkStateForReceive();
        writeSide = Thread.currentThread();
        if (in == out)
            awaitSpace();
        if (in < 0) {
            in = 0;
            out = 0;
        }
        buffer[in++] = (byte)(b & 0xFF);
        if (in >= buffer.length) {
            in = 0;
        }
    }

    /**
     * 将数据接收进一个字节数组。此方法将阻塞，直到有输入可用。
     * @param b 接收数据的缓冲区
     * @param off 数据的起始偏移量
     * @param len 最大接收的字节数
     * @exception IOException 如果管道 <a href="#BROKEN"> 断开</a>、
     *           {@link #connect(java.io.PipedOutputStream) 未连接}、已关闭，或发生 I/O 错误。
     */
    synchronized void receive(byte b[], int off, int len)  throws IOException {
        checkStateForReceive();
        writeSide = Thread.currentThread();
        int bytesToTransfer = len;
        while (bytesToTransfer > 0) {
            if (in == out)
                awaitSpace();
            int nextTransferAmount = 0;
            if (out < in) {
                nextTransferAmount = buffer.length - in;
            } else if (in < out) {
                if (in == -1) {
                    in = out = 0;
                    nextTransferAmount = buffer.length - in;
                } else {
                    nextTransferAmount = out - in;
                }
            }
            if (nextTransferAmount > bytesToTransfer)
                nextTransferAmount = bytesToTransfer;
            assert(nextTransferAmount > 0);
            System.arraycopy(b, off, buffer, in, nextTransferAmount);
            bytesToTransfer -= nextTransferAmount;
            off += nextTransferAmount;
            in += nextTransferAmount;
            if (in >= buffer.length) {
                in = 0;
            }
        }
    }

    private void checkStateForReceive() throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByWriter || closedByReader) {
            throw new IOException("Pipe closed");
        } else if (readSide != null && !readSide.isAlive()) {
            throw new IOException("Read end dead");
        }
    }

    private void awaitSpace() throws IOException {
        while (in == out) {
            checkStateForReceive();

            /* full: kick any waiting readers */
            notifyAll();
            try {
                wait(1000);
            } catch (InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
    }

    /**
     * 通知所有等待的线程，最后一个字节的数据已接收。
     */
    synchronized void receivedLast() {
        closedByWriter = true;
        notifyAll();
    }

    /**
     * 从此管道输入流读取下一个字节的数据。返回的字节值为 <code>int</code> 类型，范围为
     * <code>0</code> 到 <code>255</code>。
     * 此方法会阻塞，直到有输入数据可用，检测到流的末尾，或抛出异常。
     *
     * @return     下一个字节的数据，或 <code>-1</code> 表示已到达流的末尾。
     * @exception  IOException  如果管道
     *           {@link #connect(java.io.PipedOutputStream) 未连接}、
     *           <a href="#BROKEN"> <code>断开</code></a>、已关闭，或发生 I/O 错误。
     */
    public synchronized int read()  throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByReader) {
            throw new IOException("Pipe closed");
        } else if (writeSide != null && !writeSide.isAlive()
                   && !closedByWriter && (in < 0)) {
            throw new IOException("Write end dead");
        }

        readSide = Thread.currentThread();
        int trials = 2;
        while (in < 0) {
            if (closedByWriter) {
                /* 由写入端关闭，返回 EOF */
                return -1;
            }
            if ((writeSide != null) && (!writeSide.isAlive()) && (--trials < 0)) {
                throw new IOException("Pipe broken");
            }
            /* 可能有等待的写入端 */
            notifyAll();
            try {
                wait(1000);
            } catch (InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
        int ret = buffer[out++] & 0xFF;
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
     * 从此管道输入流读取最多 <code>len</code> 个字节的数据到一个字节数组中。如果到达数据流的末尾或
     * <code>len</code> 超过管道的缓冲区大小，则读取的字节数可能少于 <code>len</code>。
     * 如果 <code>len </code> 为零，则不读取任何字节并返回 0；否则，方法会阻塞，直到至少有 1 个字节的输入可用，
     * 检测到流的末尾，或抛出异常。
     *
     * @param      b     读取数据的缓冲区。
     * @param      off   目标数组 <code>b</code> 中的起始偏移量
     * @param      len   最大读取的字节数。
     * @return     读取到缓冲区中的总字节数，或 <code>-1</code> 表示已到达流的末尾。
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，
     * <code>len</code> 为负数，或 <code>len</code> 大于 <code>b.length - off</code>
     * @exception  IOException 如果管道 <a href="#BROKEN"> <code>断开</code></a>、
     *           {@link #connect(java.io.PipedOutputStream) 未连接}、已关闭，或发生 I/O 错误。
     */
    public synchronized int read(byte b[], int off, int len)  throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        /* 可能会等待第一个字符 */
        int c = read();
        if (c < 0) {
            return -1;
        }
        b[off] = (byte) c;
        int rlen = 1;
        while ((in >= 0) && (len > 1)) {

            int available;

            if (in > out) {
                available = Math.min((buffer.length - out), (in - out));
            } else {
                available = buffer.length - out;
            }

            // 在循环外预先读取一个字节
            if (available > (len - 1)) {
                available = len - 1;
            }
            System.arraycopy(buffer, out, b, off + rlen, available);
            out += available;
            rlen += available;
            len -= available;

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
     * 返回从此输入流读取数据而不阻塞的字节数。
     *
     * @return 从此输入流读取数据而不阻塞的字节数，或 {@code 0} 表示此输入流已通过调用其 {@link #close()} 方法关闭，
     * 或管道 {@link #connect(java.io.PipedOutputStream) 未连接}，或 <a href="#BROKEN"> <code>断开</code></a>。
     *
     * @exception  IOException  如果发生 I/O 错误。
     * @since   JDK1.0.2
     */
    public synchronized int available() throws IOException {
        if(in < 0)
            return 0;
        else if(in == out)
            return buffer.length;
        else if (in > out)
            return in - out;
        else
            return in + buffer.length - out;
    }


                /**
     * 关闭此管道输入流并释放与流关联的任何系统资源。
     *
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void close()  throws IOException {
        closedByReader = true;
        synchronized (this) {
            in = -1;
        }
    }
}
