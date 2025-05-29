/*
 * 版权所有 (c) 1995, 2013，Oracle 和/或其附属公司。保留所有权利。
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

package java.io;

/**
 * 一个管道输入流应连接到一个管道输出流；然后管道输入流提供写入管道输出流的所有数据字节。
 * 通常，数据从 <code>PipedInputStream</code> 对象由一个线程读取，而数据由另一个线程写入相应的 <code>PipedOutputStream</code>。
 * 不建议从单个线程使用这两个对象，因为这可能会导致线程死锁。
 * 管道输入流包含一个缓冲区，将读操作与写操作解耦，但有一定的限制。
 * 如果向连接的管道输出流提供数据字节的线程不再存活，则管道被认为 <a name="BROKEN"> <i>已断开</i> </a>。
 *
 * @author  James Gosling
 * @see     java.io.PipedOutputStream
 * @since   JDK1.0
 */
public class PipedInputStream extends InputStream {
    boolean closedByWriter = false;
    volatile boolean closedByReader = false;
    boolean connected = false;

        /* 提醒：识别读取和写入端需要更加复杂。
           要么使用线程组（但线程内的管道怎么办？）
           要么使用终结化（但可能要等到下一次垃圾回收）。 */
    Thread readSide;
    Thread writeSide;

    private static final int DEFAULT_PIPE_SIZE = 1024;

    /**
     * 管道的循环输入缓冲区的默认大小。
     * @since   JDK1.1
     */
    // 这个字段以前是一个常量，在允许更改管道大小之前。
    // 为了向后兼容，将继续维护这个字段。
    protected static final int PIPE_SIZE = DEFAULT_PIPE_SIZE;

    /**
     * 放置传入数据的循环缓冲区。
     * @since   JDK1.1
     */
    protected byte buffer[];

    /**
     * 循环缓冲区中下一个数据字节将被存储的位置的索引，当从连接的管道输出流接收数据时。
     * <code>in&lt;0</code> 表示缓冲区为空，<code>in==out</code> 表示缓冲区已满
     * @since   JDK1.1
     */
    protected int in = -1;

    /**
     * 循环缓冲区中下一个数据字节将被此管道输入流读取的位置的索引。
     * @since   JDK1.1
     */
    protected int out = 0;

    /**
     * 创建一个 <code>PipedInputStream</code> 以便
     * 它连接到管道输出流 <code>src</code>。写入
     * <code>src</code> 的数据字节将作为输入从此流中可用。
     *
     * @param      src   要连接的流。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public PipedInputStream(PipedOutputStream src) throws IOException {
        this(src, DEFAULT_PIPE_SIZE);
    }

    /**
     * 创建一个 <code>PipedInputStream</code> 以便
     * 它连接到管道输出流 <code>src</code> 并使用指定的管道大小作为管道缓冲区。
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
     * 创建一个 <code>PipedInputStream</code> 以便
     * 它尚未 {@linkplain #connect(java.io.PipedOutputStream)
     * 连接}。
     * 在使用之前，必须将其 {@linkplain java.io.PipedOutputStream#connect(
     * java.io.PipedInputStream) 连接} 到一个 <code>PipedOutputStream</code>。
     */
    public PipedInputStream() {
        initPipe(DEFAULT_PIPE_SIZE);
    }

    /**
     * 创建一个 <code>PipedInputStream</code> 以便
     * 它尚未 {@linkplain #connect(java.io.PipedOutputStream) 连接} 并使用指定的管道大小作为管道缓冲区。
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
     * 如果此对象已经连接到其他管道输出流，则抛出 <code>IOException</code>。
     * <p>
     * 如果 <code>src</code> 是一个未连接的管道输出流，而 <code>snk</code>
     * 是一个未连接的管道输入流，它们可以通过以下任一调用连接：
     *
     * <pre><code>snk.connect(src)</code> </pre>
     * <p>
     * 或者调用：
     *
     * <pre><code>src.connect(snk)</code> </pre>
     * <p>
     * 两个调用的效果相同。
     *
     * @param      src   要连接的管道输出流。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void connect(PipedOutputStream src) throws IOException {
        src.connect(this);
    }

    /**
     * 接收一个数据字节。如果无输入可用，此方法将阻塞。
     * @param b 正在接收的字节
     * @exception IOException 如果管道 <a href="#BROKEN"> <code>已断开</code></a>，
     *          {@link #connect(java.io.PipedOutputStream) 未连接}，
     *          已关闭，或发生 I/O 错误。
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
     * 接收数据到一个字节数组中。此方法将阻塞，直到有输入可用。
     * @param b 接收数据的缓冲区
     * @param off 数据的起始偏移量
     * @param len 接收的最大字节数
     * @exception IOException 如果管道<a href="#BROKEN">损坏</a>、
     *           {@link #connect(java.io.PipedOutputStream) 未连接}、
     *           关闭，或发生 I/O 错误。
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
            throw new IOException("管道未连接");
        } else if (closedByWriter || closedByReader) {
            throw new IOException("管道已关闭");
        } else if (readSide != null && !readSide.isAlive()) {
            throw new IOException("读取端已死");
        }
    }

    private void awaitSpace() throws IOException {
        while (in == out) {
            checkStateForReceive();

            /* 已满：唤醒任何等待的读取者 */
            notifyAll();
            try {
                wait(1000);
            } catch (InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
    }

    /**
     * 通知所有等待的线程，最后一个数据字节已被接收。
     */
    synchronized void receivedLast() {
        closedByWriter = true;
        notifyAll();
    }

    /**
     * 从此管道输入流中读取下一个字节的数据。返回的字节值为范围
     * <code>0</code> 到 <code>255</code> 的 <code>int</code>。
     * 此方法会阻塞，直到有输入数据可用、检测到流的末尾或抛出异常。
     *
     * @return     下一个字节的数据，如果到达流的末尾，则返回 <code>-1</code>。
     * @exception  IOException  如果管道
     *           {@link #connect(java.io.PipedOutputStream) 未连接}、
     *           <a href="#BROKEN"> <code>损坏</code></a>、关闭，
     *           或发生 I/O 错误。
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
                throw new IOException("管道损坏");
            }
            /* 可能有等待的写入者 */
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
     * 从此管道输入流中读取最多 <code>len</code> 个字节的数据到字节数组中。如果到达数据流的末尾或
     * <code>len</code> 超过管道的缓冲区大小，则读取的字节数将少于 <code>len</code>。
     * 如果 <code>len </code> 为零，则不读取任何字节并返回 0；
     * 否则，该方法将阻塞，直到至少有 1 个字节的输入可用、检测到流的末尾或抛出异常。
     *
     * @param      b     读取数据的缓冲区。
     * @param      off   目标数组 <code>b</code> 中的起始偏移量
     * @param      len   最大读取字节数。
     * @return     读取到缓冲区中的总字节数，如果因为到达流的末尾而没有更多数据，则返回
     *             <code>-1</code>。
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，
     * <code>len</code> 为负数，或 <code>len</code> 大于 <code>b.length - off</code>
     * @exception  IOException 如果管道<a href="#BROKEN"> <code>损坏</code></a>、
     *           {@link #connect(java.io.PipedOutputStream) 未连接}、
     *           关闭，或发生 I/O 错误。
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

            // 在循环外部预先读取一个字节
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
     * 返回可以从这个输入流中无阻塞地读取的字节数。
     *
     * @return 可以从这个输入流中无阻塞地读取的字节数，或者如果这个输入流已经通过调用其 {@link #close()} 方法关闭，或者管道
     *         {@link #connect(java.io.PipedOutputStream) 未连接}，或者 <a href="#BROKEN"> <code>已损坏</code></a>，则返回 {@code 0}。
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
     * 关闭这个管道输入流并释放与流关联的任何系统资源。
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
