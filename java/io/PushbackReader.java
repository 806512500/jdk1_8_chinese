
/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * 允许字符被推回流中的字符流读取器。
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class PushbackReader extends FilterReader {

    /** 推回缓冲区 */
    private char[] buf;

    /** 缓冲区中的当前位置 */
    private int pos;

    /**
     * 创建一个新的具有给定大小的推回缓冲区的推回读取器。
     *
     * @param   in   从中读取字符的读取器
     * @param   size 推回缓冲区的大小
     * @exception IllegalArgumentException 如果 {@code size <= 0}
     */
    public PushbackReader(Reader in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        this.buf = new char[size];
        this.pos = size;
    }

    /**
     * 创建一个新的具有单字符推回缓冲区的推回读取器。
     *
     * @param   in  从中读取字符的读取器
     */
    public PushbackReader(Reader in) {
        this(in, 1);
    }

    /** 检查流是否未被关闭。 */
    private void ensureOpen() throws IOException {
        if (buf == null)
            throw new IOException("Stream closed");
    }

    /**
     * 读取单个字符。
     *
     * @return     读取的字符，或如果已到达流的末尾则返回 -1
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public int read() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (pos < buf.length)
                return buf[pos++];
            else
                return super.read();
        }
    }

    /**
     * 将字符读入数组的一部分。
     *
     * @param      cbuf  目标缓冲区
     * @param      off   开始写入字符的偏移量
     * @param      len   最大读取字符数
     *
     * @return     读取的字符数，或如果已到达流的末尾则返回 -1
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            try {
                if (len <= 0) {
                    if (len < 0) {
                        throw new IndexOutOfBoundsException();
                    } else if ((off < 0) || (off > cbuf.length)) {
                        throw new IndexOutOfBoundsException();
                    }
                    return 0;
                }
                int avail = buf.length - pos;
                if (avail > 0) {
                    if (len < avail)
                        avail = len;
                    System.arraycopy(buf, pos, cbuf, off, avail);
                    pos += avail;
                    off += avail;
                    len -= avail;
                }
                if (len > 0) {
                    len = super.read(cbuf, off, len);
                    if (len == -1) {
                        return (avail == 0) ? -1 : avail;
                    }
                    return avail + len;
                }
                return avail;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException();
            }
        }
    }

    /**
     * 通过将其复制到推回缓冲区的前面来推回单个字符。此方法返回后，下一个要读取的字符将具有 <code>(char)c</code> 的值。
     *
     * @param  c  表示要推回的字符的 int 值
     *
     * @exception  IOException  如果推回缓冲区已满，或发生其他 I/O 错误
     */
    public void unread(int c) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (pos == 0)
                throw new IOException("Pushback buffer overflow");
            buf[--pos] = (char) c;
        }
    }

    /**
     * 通过将其复制到推回缓冲区的前面来推回字符数组的一部分。此方法返回后，下一个要读取的字符将具有 <code>cbuf[off]</code> 的值，之后的字符将具有 <code>cbuf[off+1]</code> 的值，依此类推。
     *
     * @param  cbuf  字符数组
     * @param  off   要推回的第一个字符的偏移量
     * @param  len   要推回的字符数
     *
     * @exception  IOException  如果推回缓冲区空间不足，或发生其他 I/O 错误
     */
    public void unread(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (len > pos)
                throw new IOException("Pushback buffer overflow");
            pos -= len;
            System.arraycopy(cbuf, off, buf, pos, len);
        }
    }

    /**
     * 通过将其复制到推回缓冲区的前面来推回字符数组。此方法返回后，下一个要读取的字符将具有 <code>cbuf[0]</code> 的值，之后的字符将具有 <code>cbuf[1]</code> 的值，依此类推。
     *
     * @param  cbuf  要推回的字符数组
     *
     * @exception  IOException  如果推回缓冲区空间不足，或发生其他 I/O 错误
     */
    public void unread(char cbuf[]) throws IOException {
        unread(cbuf, 0, cbuf.length);
    }

    /**
     * 告知此流是否准备好读取。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public boolean ready() throws IOException {
        synchronized (lock) {
            ensureOpen();
            return (pos < buf.length) || super.ready();
        }
    }

                /**
     * 标记流中的当前位置。对于 <code>PushbackReader</code> 类，<code>mark</code>
     * 方法始终抛出异常。
     *
     * @exception  IOException  始终，因为不支持 mark
     */
    public void mark(int readAheadLimit) throws IOException {
        throw new IOException("mark/reset not supported");
    }

    /**
     * 重置流。对于 <code>PushbackReader</code> 类，<code>reset</code> 方法
     * 始终抛出异常。
     *
     * @exception  IOException  始终，因为不支持 reset
     */
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    /**
     * 告知此流是否支持 mark() 操作，实际上不支持。
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * 关闭流并释放与之关联的任何系统资源。一旦流被关闭，进一步的 read()、
     * unread()、ready() 或 skip() 调用将抛出 IOException。关闭已关闭的流没有效果。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void close() throws IOException {
        super.close();
        buf = null;
    }

    /**
     * 跳过字符。此方法将阻塞，直到有字符可用、发生 I/O 错误或到达流的末尾。
     *
     * @param  n  要跳过的字符数
     *
     * @return    实际跳过的字符数
     *
     * @exception  IllegalArgumentException  如果 <code>n</code> 为负数。
     * @exception  IOException  如果发生 I/O 错误
     */
    public long skip(long n) throws IOException {
        if (n < 0L)
            throw new IllegalArgumentException("skip value is negative");
        synchronized (lock) {
            ensureOpen();
            int avail = buf.length - pos;
            if (avail > 0) {
                if (n <= avail) {
                    pos += n;
                    return n;
                } else {
                    pos = buf.length;
                    n -= avail;
                }
            }
            return avail + super.skip(n);
        }
    }
}
