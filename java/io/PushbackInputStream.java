
/*
 * 版权所有 (c) 1994, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * <code>PushbackInputStream</code> 为另一个输入流添加了功能，特别是
 * “推送回”或“取消读取”一个字节的能力。这在代码片段需要读取
 * 由特定字节值分隔的不定数量的数据字节的情况下非常有用；
 * 在读取终止字节后，代码片段可以“取消读取”它，以便
 * 下一次对输入流的读取操作将重新读取被推送回的字节。
 * 例如，表示标识符的字节可能以表示操作符字符的字节终止；
 * 一个专门读取标识符的方法可以读取直到看到操作符，
 * 然后将操作符推送回以重新读取。
 *
 * @author  David Connelly
 * @author  Jonathan Payne
 * @since   JDK1.0
 */
public
class PushbackInputStream extends FilterInputStream {
    /**
     * 推送回缓冲区。
     * @since   JDK1.1
     */
    protected byte[] buf;

    /**
     * 推送回缓冲区中下一个将被读取的字节的位置。当缓冲区为空时，<code>pos</code> 等于
     * <code>buf.length</code>；当缓冲区满时，<code>pos</code> 等于零。
     *
     * @since   JDK1.1
     */
    protected int pos;

    /**
     * 检查此流是否未被关闭
     */
    private void ensureOpen() throws IOException {
        if (in == null)
            throw new IOException("Stream closed");
    }

    /**
     * 创建一个具有指定 <code>size</code> 大小的推送回缓冲区的 <code>PushbackInputStream</code>，
     * 并保存其参数，输入流 <code>in</code> 以供后续使用。最初，
     * 没有推送回的字节（字段 <code>pushBack</code> 初始化为 <code>-1</code>）。
     *
     * @param  in    将从中读取字节的输入流。
     * @param  size  推送回缓冲区的大小。
     * @exception IllegalArgumentException 如果 {@code size <= 0}
     * @since  JDK1.1
     */
    public PushbackInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        this.buf = new byte[size];
        this.pos = size;
    }

    /**
     * 创建一个 <code>PushbackInputStream</code>
     * 并保存其参数，输入流 <code>in</code> 以供后续使用。最初，
     * 没有推送回的字节（字段 <code>pushBack</code> 初始化为 <code>-1</code>）。
     *
     * @param   in   将从中读取字节的输入流。
     */
    public PushbackInputStream(InputStream in) {
        this(in, 1);
    }

    /**
     * 从该输入流中读取下一个字节的数据。字节的值作为 <code>int</code> 返回，范围为
     * <code>0</code> 到 <code>255</code>。如果因为到达流的末尾而没有可用的字节，
     * 则返回值 <code>-1</code>。此方法会阻塞，直到有输入数据可用，检测到流的末尾，或抛出异常。
     *
     * <p> 如果有最近推送回的字节，此方法返回该字节，否则调用其底层输入流的 <code>read</code> 方法并返回该方法的返回值。
     *
     * @return     下一个字节的数据，或如果到达流的末尾则返回 <code>-1</code>。
     * @exception  IOException  如果此输入流已通过调用其 {@link #close()} 方法关闭，
     *             或发生 I/O 错误。
     * @see        java.io.InputStream#read()
     */
    public int read() throws IOException {
        ensureOpen();
        if (pos < buf.length) {
            return buf[pos++] & 0xff;
        }
        return super.read();
    }

    /**
     * 从该输入流中读取最多 <code>len</code> 个字节的数据到字节数组中。此方法首先读取任何推送回的字节；之后
     * 如果读取的字节少于 <code>len</code> 个，则从底层输入流中读取。如果 <code>len</code> 不为零，方法
     * 会阻塞直到至少有 1 个字节的输入可用；否则，不读取任何字节并返回 <code>0</code>。
     *
     * @param      b     数据读入的缓冲区。
     * @param      off   目标数组 <code>b</code> 中的起始偏移量
     * @param      len   最大读取字节数。
     * @return     读入缓冲区的总字节数，或如果因为到达流的末尾而没有更多数据则返回 <code>-1</code>。
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，
     * <code>len</code> 为负数，或 <code>len</code> 大于 <code>b.length - off</code>
     * @exception  IOException  如果此输入流已通过调用其 {@link #close()} 方法关闭，
     *             或发生 I/O 错误。
     * @see        java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int avail = buf.length - pos;
        if (avail > 0) {
            if (len < avail) {
                avail = len;
            }
            System.arraycopy(buf, pos, b, off, avail);
            pos += avail;
            off += avail;
            len -= avail;
        }
        if (len > 0) {
            len = super.read(b, off, len);
            if (len == -1) {
                return avail == 0 ? -1 : avail;
            }
            return avail + len;
        }
        return avail;
    }

                /**
     * 将一个字节通过复制到回退缓冲区的前面来回退。
     * 该方法返回后，下一个要读取的字节将具有值
     * <code>(byte)b</code>。
     *
     * @param      b   其低阶字节要被回退的<code>int</code>值。
     * @exception IOException 如果回退缓冲区中没有足够的空间来存储该字节，或者此输入流已通过调用其 {@link #close()} 方法关闭。
     */
    public void unread(int b) throws IOException {
        ensureOpen();
        if (pos == 0) {
            throw new IOException("回退缓冲区已满");
        }
        buf[--pos] = (byte)b;
    }

    /**
     * 通过将字节数组的一部分复制到回退缓冲区的前面来回退。
     * 该方法返回后，下一个要读取的字节将具有值<code>b[off]</code>，之后的字节将具有值<code>b[off+1]</code>，依此类推。
     *
     * @param b 要回退的字节数组。
     * @param off 数据的起始偏移量。
     * @param len 要回退的字节数。
     * @exception IOException 如果回退缓冲区中没有足够的空间来存储指定数量的字节，
     *            或者此输入流已通过调用其 {@link #close()} 方法关闭。
     * @since     JDK1.1
     */
    public void unread(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (len > pos) {
            throw new IOException("回退缓冲区已满");
        }
        pos -= len;
        System.arraycopy(b, off, buf, pos, len);
    }

    /**
     * 通过将字节数组复制到回退缓冲区的前面来回退。
     * 该方法返回后，下一个要读取的字节将具有值<code>b[0]</code>，之后的字节将具有值<code>b[1]</code>，依此类推。
     *
     * @param b 要回退的字节数组
     * @exception IOException 如果回退缓冲区中没有足够的空间来存储指定数量的字节，
     *            或者此输入流已通过调用其 {@link #close()} 方法关闭。
     * @since     JDK1.1
     */
    public void unread(byte[] b) throws IOException {
        unread(b, 0, b.length);
    }

    /**
     * 返回从该输入流中可以不阻塞地读取（或跳过）的字节数的估计值。下一次调用此输入流的方法可能是同一个线程或另一个线程。单次读取或跳过这么多字节不会阻塞，但可能读取或跳过更少的字节。
     *
     * <p> 该方法返回已回退的字节数加上 {@link
     * java.io.FilterInputStream#available available} 方法返回的值。
     *
     * @return     可以不阻塞地从输入流中读取（或跳过）的字节数。
     * @exception  IOException  如果此输入流已通过调用其 {@link #close()} 方法关闭，
     *             或发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     * @see        java.io.InputStream#available()
     */
    public int available() throws IOException {
        ensureOpen();
        int n = buf.length - pos;
        int avail = super.available();
        return n > (Integer.MAX_VALUE - avail)
                    ? Integer.MAX_VALUE
                    : n + avail;
    }

    /**
     * 从该输入流中跳过并丢弃 <code>n</code> 个数据字节。由于各种原因，<code>skip</code> 方法可能会跳过更少的字节数，甚至可能为零。如果 <code>n</code> 为负数，则不跳过任何字节。
     *
     * <p> <code>PushbackInputStream</code> 类的 <code>skip</code> 方法首先跳过回退缓冲区中的字节（如果有）。然后，如果需要跳过更多字节，它将调用底层输入流的 <code>skip</code> 方法。实际跳过的字节数将被返回。
     *
     * @param      n  {@inheritDoc}
     * @return     {@inheritDoc}
     * @exception  IOException  如果流不支持 seek，
     *            或者此输入流已通过调用其 {@link #close()} 方法关闭，
     *            或发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     * @see        java.io.InputStream#skip(long n)
     * @since      1.2
     */
    public long skip(long n) throws IOException {
        ensureOpen();
        if (n <= 0) {
            return 0;
        }

        long pskip = buf.length - pos;
        if (pskip > 0) {
            if (n < pskip) {
                pskip = n;
            }
            pos += pskip;
            n -= pskip;
        }
        if (n > 0) {
            pskip += super.skip(n);
        }
        return pskip;
    }

    /**
     * 测试此输入流是否支持 <code>mark</code> 和 <code>reset</code> 方法，实际上不支持。
     *
     * @return   <code>false</code>，因为此类不支持 <code>mark</code> 和 <code>reset</code> 方法。
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * 标记此输入流中的当前位置。
     *
     * <p> <code>PushbackInputStream</code> 类的 <code>mark</code> 方法不执行任何操作。
     *
     * @param   readlimit   在标记位置失效之前可以读取的最大字节数。
     * @see     java.io.InputStream#reset()
     */
    public synchronized void mark(int readlimit) {
    }

    /**
     * 将此流重新定位到上次调用 <code>mark</code> 方法时的位置。
     *
     * <p> <code>PushbackInputStream</code> 类的 <code>reset</code> 方法除了抛出一个 <code>IOException</code> 外，不执行任何操作。
     *
     * @exception  IOException  如果调用此方法。
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.IOException
     */
    public synchronized void reset() throws IOException {
        throw new IOException("不支持 mark/reset");
    }

                /**
     * 关闭此输入流并释放与该流关联的任何系统资源。
     * 一旦流被关闭，进一步的 read()、unread()、available()、reset() 或 skip() 调用将抛出 IOException。
     * 关闭已关闭的流没有任何效果。
     *
     * @exception  IOException  如果发生 I/O 错误。
     */
    public synchronized void close() throws IOException {
        if (in == null)
            return;
        in.close();
        in = null;
        buf = null;
    }
}
