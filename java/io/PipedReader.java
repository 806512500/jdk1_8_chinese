
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

    /* REMIND: identification of the read and write sides needs to be
       more sophisticated.  Either using thread groups (but what about
       pipes within a thread?) or using finalization (but it may be a
       long time until the next GC). */
    Thread readSide;
    Thread writeSide;

   /**
    * 管道的循环输入缓冲区大小。
    */
    private static final int DEFAULT_PIPE_SIZE = 1024;

    /**
     * 用于存放传入数据的循环缓冲区。
     */
    char buffer[];

    /**
     * 在循环缓冲区中存储从连接的管道写入器接收到的下一个数据字符的位置的索引。<code>in&lt;0</code> 表示缓冲区为空，
     * <code>in==out</code> 表示缓冲区已满。
     */
    int in = -1;

    /**
     * 从该管道读取器读取下一个数据字符在循环缓冲区中的位置的索引。
     */
    int out = 0;

    /**
     * 创建一个 <code>PipedReader</code>，使其连接到管道写入器 <code>src</code>。写入到 <code>src</code>
     * 的数据将作为此流的输入可用。
     *
     * @param      src   要连接的流。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public PipedReader(PipedWriter src) throws IOException {
        this(src, DEFAULT_PIPE_SIZE);
    }

    /**
     * 创建一个 <code>PipedReader</code>，使其连接到管道写入器 <code>src</code> 并使用指定的管道缓冲区大小。
     * 写入到 <code>src</code> 的数据将作为此流的输入可用。
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
     * 创建一个 <code>PipedReader</code>，使其尚未 {@linkplain #connect(java.io.PipedWriter)
     * 连接}。在使用之前，必须将其 {@linkplain java.io.PipedWriter#connect(
     * java.io.PipedReader) 连接} 到一个 <code>PipedWriter</code>。
     */
    public PipedReader() {
        initPipe(DEFAULT_PIPE_SIZE);
    }

    /**
     * 创建一个 <code>PipedReader</code>，使其尚未 {@link #connect(java.io.PipedWriter) 连接} 并使用
     * 指定的管道缓冲区大小。在使用之前，必须将其 {@linkplain java.io.PipedWriter#connect(
     * java.io.PipedReader) 连接} 到一个 <code>PipedWriter</code>。
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
            throw new IllegalArgumentException("Pipe size <= 0");
        }
        buffer = new char[pipeSize];
    }

    /**
     * 使此管道读取器连接到管道写入器 <code>src</code>。如果此对象已连接到其他管道写入器，
     * 则抛出 <code>IOException</code>。
     * <p>
     * 如果 <code>src</code> 是一个未连接的管道写入器，而 <code>snk</code> 是一个未连接的管道读取器，
     * 它们可以通过以下调用之一连接：
     *
     * <pre><code>snk.connect(src)</code> </pre>
     * <p>
     * 或者：
     *
     * <pre><code>src.connect(snk)</code> </pre>
     * <p>
     * 两个调用的效果相同。
     *
     * @param      src   要连接的管道写入器。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void connect(PipedWriter src) throws IOException {
        src.connect(this);
    }

    /**
     * 接收一个字符的数据。如果无输入可用，此方法将阻塞。
     */
    synchronized void receive(int c) throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByWriter || closedByReader) {
            throw new IOException("Pipe closed");
        } else if (readSide != null && !readSide.isAlive()) {
            throw new IOException("Read end dead");
        }

        writeSide = Thread.currentThread();
        while (in == out) {
            if ((readSide != null) && !readSide.isAlive()) {
                throw new IOException("Pipe broken");
            }
            /* full: kick any waiting readers */
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
     * 将数据接收进字符数组。此方法将阻塞直到有输入可用。
     */
    synchronized void receive(char c[], int off, int len)  throws IOException {
        while (--len >= 0) {
            receive(c[off++]);
        }
    }

    /**
     * 通知所有等待线程已接收到最后一个字符的数据。
     */
    synchronized void receivedLast() {
        closedByWriter = true;
        notifyAll();
    }


    /**
     * 从这个管道流中读取下一个数据字符。
     * 如果因为到达流的末尾而没有可用的字符，将返回值 <code>-1</code>。
     * 此方法会阻塞，直到输入数据可用，检测到流的末尾，或抛出异常。
     *
     * @return     下一个数据字符，或如果到达流的末尾则返回 <code>-1</code>。
     * @exception  IOException  如果管道
     *          <a href=PipedInputStream.html#BROKEN> <code>broken</code></a>，
     *          {@link #connect(java.io.PipedWriter) 未连接}，关闭，
     *          或发生 I/O 错误。
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
     * 从这个管道流中读取最多 <code>len</code> 个字符的数据到字符数组中。如果到达数据流的末尾或
     * <code>len</code> 超过管道的缓冲区大小，则读取的字符数会少于 <code>len</code>。此方法
     * 会阻塞，直到至少有一个字符的输入可用。
     *
     * @param      cbuf     读取数据的缓冲区。
     * @param      off   数据的起始偏移量。
     * @param      len   最大读取的字符数。
     * @return     读入缓冲区的总字符数，或如果因为到达流的末尾而没有更多数据则返回
     *             <code>-1</code>。
     * @exception  IOException  如果管道
     *                  <a href=PipedInputStream.html#BROKEN> <code>broken</code></a>，
     *                  {@link #connect(java.io.PipedWriter) 未连接}，关闭，
     *                  或发生 I/O 错误。
     */
    public synchronized int read(char cbuf[], int off, int len)  throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByReader) {
            throw new IOException("Pipe closed");
        } else if (writeSide != null && !writeSide.isAlive()
                   && !closedByWriter && (in < 0)) {
            throw new IOException("Write end dead");
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
     * 告知此流是否准备好被读取。如果循环缓冲区不为空，则管道字符流准备好。
     *
     * @exception  IOException  如果管道
     *                  <a href=PipedInputStream.html#BROKEN> <code>broken</code></a>，
     *                  {@link #connect(java.io.PipedWriter) 未连接}，或关闭。
     */
    public synchronized boolean ready() throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByReader) {
            throw new IOException("Pipe closed");
        } else if (writeSide != null && !writeSide.isAlive()
                   && !closedByWriter && (in < 0)) {
            throw new IOException("Write end dead");
        }
        if (in < 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 关闭此管道流并释放与流关联的任何系统资源。
     *
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void close()  throws IOException {
        in = -1;
        closedByReader = true;
    }
}
