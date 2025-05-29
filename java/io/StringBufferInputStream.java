/*
 * 版权 (c) 1995, 2004, Oracle 和/或其附属公司。保留所有权利。
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
 * 该类允许应用程序创建一个输入流，其中读取的字节由字符串的内容提供。
 * 应用程序也可以通过使用 <code>ByteArrayInputStream</code> 从字节数组中读取字节。
 * <p>
 * 该类仅使用字符串中每个字符的低八位。
 *
 * @author     Arthur van Hoff
 * @see        java.io.ByteArrayInputStream
 * @see        java.io.StringReader
 * @since      JDK1.0
 * @deprecated 该类不能正确地将字符转换为字节。自 JDK&nbsp;1.1 起，从字符串创建流的首选方法是通过 <code>StringReader</code> 类。
 */
@Deprecated
public
class StringBufferInputStream extends InputStream {
    /**
     * 从中读取字节的字符串。
     */
    protected String buffer;

    /**
     * 从输入流缓冲区中读取的下一个字符的索引。
     *
     * @see        java.io.StringBufferInputStream#buffer
     */
    protected int pos;

    /**
     * 输入流缓冲区中的有效字符数。
     *
     * @see        java.io.StringBufferInputStream#buffer
     */
    protected int count;

    /**
     * 创建一个字符串输入流以从指定的字符串中读取数据。
     *
     * @param      s   底层输入缓冲区。
     */
    public StringBufferInputStream(String s) {
        this.buffer = s;
        count = s.length();
    }

    /**
     * 从该输入流中读取下一个字节的数据。返回的值是一个 <code>int</code>，范围在
     * <code>0</code> 到 <code>255</code> 之间。如果因为到达流的末尾而没有可用的字节，
     * 则返回 <code>-1</code>。
     * <p>
     * <code>StringBufferInputStream</code> 的 <code>read</code> 方法不会阻塞。它返回此输入流缓冲区中下一个字符的低八位。
     *
     * @return     下一个字节的数据，或者如果到达流的末尾则返回 <code>-1</code>。
     */
    public synchronized int read() {
        return (pos < count) ? (buffer.charAt(pos++) & 0xFF) : -1;
    }

    /**
     * 从该输入流中读取最多 <code>len</code> 个字节的数据到字节数组中。
     * <p>
     * <code>StringBufferInputStream</code> 的 <code>read</code> 方法不会阻塞。它将此输入流缓冲区中的字符的低八位复制到字节数组参数中。
     *
     * @param      b     读取数据的缓冲区。
     * @param      off   数据的起始偏移量。
     * @param      len   读取的最大字节数。
     * @return     读入缓冲区的总字节数，或者如果因为到达流的末尾而没有更多数据则返回 <code>-1</code>。
     */
    public synchronized int read(byte b[], int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (pos >= count) {
            return -1;
        }

        int avail = count - pos;
        if (len > avail) {
            len = avail;
        }
        if (len <= 0) {
            return 0;
        }
        String  s = buffer;
        int cnt = len;
        while (--cnt >= 0) {
            b[off++] = (byte)s.charAt(pos++);
        }

        return len;
    }

    /**
     * 从该输入流中跳过 <code>n</code> 个输入字节。如果到达输入流的末尾，可能会跳过更少的字节。
     *
     * @param      n   要跳过的字节数。
     * @return     实际跳过的字节数。
     */
    public synchronized long skip(long n) {
        if (n < 0) {
            return 0;
        }
        if (n > count - pos) {
            n = count - pos;
        }
        pos += n;
        return n;
    }

    /**
     * 返回可以从输入流中无阻塞地读取的字节数。
     *
     * @return     <code>count&nbsp;-&nbsp;pos</code> 的值，即输入缓冲区中剩余要读取的字节数。
     */
    public synchronized int available() {
        return count - pos;
    }

    /**
     * 重置输入流以从该输入流的底层缓冲区的第一个字符开始读取。
     */
    public synchronized void reset() {
        pos = 0;
    }
}
