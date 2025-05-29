/*
 * 版权所有 (c) 1994, 2003, Oracle 和/或其附属公司。保留所有权利。
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
 * 该类实现了一个缓冲输出流。通过设置此类输出流，应用程序可以将字节写入底层输出流，而无需为每个写入的字节调用底层系统。
 *
 * @author  Arthur van Hoff
 * @since   JDK1.0
 */
public
class BufferedOutputStream extends FilterOutputStream {
    /**
     * 存储数据的内部缓冲区。
     */
    protected byte buf[];

    /**
     * 缓冲区中的有效字节数。此值始终在 <tt>0</tt> 到 <tt>buf.length</tt> 范围内；元素
     * <tt>buf[0]</tt> 到 <tt>buf[count-1]</tt> 包含有效字节数据。
     */
    protected int count;

    /**
     * 创建一个新的缓冲输出流，用于将数据写入指定的底层输出流。
     *
     * @param   out   底层输出流。
     */
    public BufferedOutputStream(OutputStream out) {
        this(out, 8192);
    }

    /**
     * 创建一个新的缓冲输出流，用于将数据写入指定的底层输出流，并具有指定的缓冲区大小。
     *
     * @param   out    底层输出流。
     * @param   size   缓冲区大小。
     * @exception IllegalArgumentException 如果 size &lt;= 0。
     */
    public BufferedOutputStream(OutputStream out, int size) {
        super(out);
        if (size <= 0) {
            throw new IllegalArgumentException("缓冲区大小 <= 0");
        }
        buf = new byte[size];
    }

    /** 刷新内部缓冲区 */
    private void flushBuffer() throws IOException {
        if (count > 0) {
            out.write(buf, 0, count);
            count = 0;
        }
    }

    /**
     * 将指定的字节写入此缓冲输出流。
     *
     * @param      b   要写入的字节。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public synchronized void write(int b) throws IOException {
        if (count >= buf.length) {
            flushBuffer();
        }
        buf[count++] = (byte)b;
    }

    /**
     * 从指定的字节数组中写入 <code>len</code> 个字节，从偏移量 <code>off</code> 开始，到此缓冲输出流。
     *
     * <p> 通常，此方法将字节数组中的字节存储到此流的缓冲区中，根据需要将缓冲区刷新到底层输出流。但是，如果请求的长度至少与此流的缓冲区大小相同，则此方法将刷新缓冲区并将字节直接写入底层输出流。因此，冗余的 <code>BufferedOutputStream</code> 不会不必要的复制数据。
     *
     * @param      b     数据。
     * @param      off   数据中的起始偏移量。
     * @param      len   要写入的字节数。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public synchronized void write(byte b[], int off, int len) throws IOException {
        if (len >= buf.length) {
            /* 如果请求的长度超过输出缓冲区的大小，
               则刷新输出缓冲区，然后直接写入数据。
               这样，缓冲流将无害地级联。 */
            flushBuffer();
            out.write(b, off, len);
            return;
        }
        if (len > buf.length - count) {
            flushBuffer();
        }
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    /**
     * 刷新此缓冲输出流。这会强制将任何缓冲的输出字节写入底层输出流。
     *
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     */
    public synchronized void flush() throws IOException {
        flushBuffer();
        out.flush();
    }
}
