/*
 * 版权所有 (c) 1995, 2006, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.*;

/**
 * 一个管道输出流可以连接到一个管道输入流以创建一个通信管道。管道输出流是管道的发送端。通常，数据由一个线程写入
 * <code>PipedOutputStream</code> 对象，而数据由另一个线程从连接的 <code>PipedInputStream</code> 读取。不建议从单个线程使用这两个对象，因为这可能会导致线程死锁。
 * 如果从连接的管道输入流读取数据字节的线程不再存活，则认为管道<a name=BROKEN> <i>已断开</i> </a>。
 *
 * @author  James Gosling
 * @see     java.io.PipedInputStream
 * @since   JDK1.0
 */
public
class PipedOutputStream extends OutputStream {

        /* 提醒：需要更复杂的读写端识别方法。可以使用线程组（但线程内的管道怎么办？）
           或使用终结化（但可能需要很长时间才能进行下一次垃圾回收）。 */
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
     * 创建一个尚未连接到管道输入流的管道输出流。在使用之前，必须由接收方或发送方将其连接到管道输入流。
     *
     * @see     java.io.PipedInputStream#connect(java.io.PipedOutputStream)
     * @see     java.io.PipedOutputStream#connect(java.io.PipedInputStream)
     */
    public PipedOutputStream() {
    }

    /**
     * 将此管道输出流连接到接收方。如果此对象已经连接到其他管道输入流，则会抛出 <code>IOException</code>。
     * <p>
     * 如果 <code>snk</code> 是一个未连接的管道输入流，而 <code>src</code> 是一个未连接的管道输出流，它们可以通过以下任一调用进行连接：
     * <blockquote><pre>
     * src.connect(snk)</pre></blockquote>
     * 或者调用：
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
     * @exception IOException 如果管道<a href=#BROKEN> 断开</a>、
     *          {@link #connect(java.io.PipedInputStream) 未连接}、关闭，或者发生 I/O 错误。
     */
    public void write(int b)  throws IOException {
        if (sink == null) {
            throw new IOException("Pipe not connected");
        }
        sink.receive(b);
    }

    /**
     * 从指定的字节数组中写入 <code>len</code> 个字节，从偏移量 <code>off</code> 开始写入此管道输出流。
     * 此方法会阻塞，直到所有字节都写入输出流。
     *
     * @param      b     数据。
     * @param      off   数据中的起始偏移量。
     * @param      len   要写入的字节数。
     * @exception IOException 如果管道<a href=#BROKEN> 断开</a>、
     *          {@link #connect(java.io.PipedInputStream) 未连接}、关闭，或者发生 I/O 错误。
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
     * 刷新此输出流并强制任何缓冲的输出字节被写入。
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
     * 关闭此管道输出流并释放与此流关联的任何系统资源。此流不能再用于写入字节。
     *
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void close()  throws IOException {
        if (sink != null) {
            sink.receivedLast();
        }
    }
}
