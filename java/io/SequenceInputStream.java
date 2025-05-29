/*
 * 版权所有 (c) 1994, 2011, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 * <code>SequenceInputStream</code> 表示
 * 其他输入流的逻辑连接。它从一个有序
 * 的输入流集合开始，从第一个流读取直到文件结束，
 * 然后从第二个流读取，依此类推，直到从包含的最后一个输入流
 * 读取到文件结束。
 *
 * @author  Author van Hoff
 * @since   JDK1.0
 */
public
class SequenceInputStream extends InputStream {
    Enumeration<? extends InputStream> e;
    InputStream in;

    /**
     * 通过记住参数来初始化新创建的 <code>SequenceInputStream</code>，
     * 该参数必须是一个产生运行时类型为 <code>InputStream</code> 的对象的
     * <code>Enumeration</code>。枚举产生的输入流将按顺序读取，
     * 以提供从此 <code>SequenceInputStream</code> 读取的字节。在枚举中的
     * 每个输入流耗尽后，将通过调用其 <code>close</code> 方法来关闭。
     *
     * @param   e   输入流的枚举。
     * @see     java.util.Enumeration
     */
    public SequenceInputStream(Enumeration<? extends InputStream> e) {
        this.e = e;
        try {
            nextStream();
        } catch (IOException ex) {
            // 这不应该发生
            throw new Error("panic");
        }
    }

    /**
     * 通过记住两个参数来初始化新创建的 <code>SequenceInputStream</code>，
     * 这两个参数将按顺序读取，首先是 <code>s1</code>，然后是 <code>s2</code>，
     * 以提供从此 <code>SequenceInputStream</code> 读取的字节。
     *
     * @param   s1   要读取的第一个输入流。
     * @param   s2   要读取的第二个输入流。
     */
    public SequenceInputStream(InputStream s1, InputStream s2) {
        Vector<InputStream> v = new Vector<>(2);

        v.addElement(s1);
        v.addElement(s2);
        e = v.elements();
        try {
            nextStream();
        } catch (IOException ex) {
            // 这不应该发生
            throw new Error("panic");
        }
    }

    /**
     * 如果达到 EOF，则继续读取下一个流。
     */
    final void nextStream() throws IOException {
        if (in != null) {
            in.close();
        }

        if (e.hasMoreElements()) {
            in = (InputStream) e.nextElement();
            if (in == null)
                throw new NullPointerException();
        }
        else in = null;

    }

    /**
     * 返回一个估计值，表示在当前底层输入流中可以读取（或跳过）而不会
     * 阻塞的字节数。下次调用当前底层输入流的方法时，调用者可能是
     * 同一线程或另一个线程。单次读取或跳过这么多字节不会阻塞，但可能
     * 读取或跳过更少的字节。
     * <p>
     * 此方法仅调用当前底层输入流的 {@code available} 方法并返回结果。
     *
     * @return 一个估计值，表示在当前底层输入流中可以读取（或跳过）而不会
     *         阻塞的字节数，或者如果此输入流已通过调用其 {@link #close()} 方法关闭，则返回 {@code 0}
     * @exception  IOException  如果发生 I/O 错误。
     *
     * @since   JDK1.1
     */
    public int available() throws IOException {
        if (in == null) {
            return 0; // 无法从 available() 信号 EOF
        }
        return in.available();
    }

    /**
     * 从此输入流中读取下一个字节的数据。该字节作为范围在 <code>0</code> 到
     * <code>255</code> 之间的 <code>int</code> 返回。如果因为流已结束而没有可用的字节，
     * 则返回值 <code>-1</code>。此方法会阻塞，直到有输入数据可用，检测到流的结束，或抛出异常。
     * <p>
     * 此方法
     * 尝试从当前子流读取一个字符。如果它到达流的末尾，它将调用当前子流的 <code>close</code>
     * 方法并开始从下一个子流读取。
     *
     * @return     下一个字节的数据，或者如果到达流的末尾，则返回 <code>-1</code>。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public int read() throws IOException {
        while (in != null) {
            int c = in.read();
            if (c != -1) {
                return c;
            }
            nextStream();
        }
        return -1;
    }

    /**
     * 从此输入流中读取最多 <code>len</code> 个字节的数据到字节数组中。如果 <code>len</code> 不为零，
     * 该方法会阻塞，直到至少有 1 个字节的输入可用；否则，不读取任何字节并返回 <code>0</code>。
     * <p>
     * <code>SequenceInputStream</code> 的 <code>read</code> 方法尝试从当前子流读取数据。如果它因为
     * 子流已到达流的末尾而无法读取任何字符，它将调用当前子流的 <code>close</code> 方法并开始从下一个子流读取。
     *
     * @param      b     存储数据的缓冲区。
     * @param      off   数组 <code>b</code> 中数据写入的起始偏移量。
     * @param      len   最大读取的字节数。
     * @return     int   读取的字节数。
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，
     * <code>len</code> 为负数，或 <code>len</code> 大于 <code>b.length - off</code>
     * @exception  IOException  如果发生 I/O 错误。
     */
    public int read(byte b[], int off, int len) throws IOException {
        if (in == null) {
            return -1;
        } else if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        do {
            int n = in.read(b, off, len);
            if (n > 0) {
                return n;
            }
            nextStream();
        } while (in != null);
        return -1;
    }

}

                /**
     * 关闭此输入流并释放与流关联的任何系统资源。
     * 已关闭的 <code>SequenceInputStream</code>
     * 不能执行输入操作，也不能重新打开。
     * <p>
     * 如果此流是从枚举创建的，则在 <code>close</code> 方法返回之前，
     * 会从枚举中请求所有剩余元素并关闭它们。
     *
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void close() throws IOException {
        do {
            nextStream();
        } while (in != null);
    }
}
