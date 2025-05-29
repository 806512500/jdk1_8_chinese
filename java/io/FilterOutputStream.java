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

/**
 * 该类是所有过滤输出流类的超类。这些流位于已存在的输出流（<i>底层</i>输出流）之上，使用它作为数据的基本接收器，但可能在传输过程中转换数据或提供额外的功能。
 * <p>
 * 类<code>FilterOutputStream</code>本身只是重写了<code>OutputStream</code>的所有方法，将所有请求传递给底层输出流。<code>FilterOutputStream</code>的子类可以进一步重写这些方法中的某些方法，以及提供额外的方法和字段。
 *
 * @author  Jonathan Payne
 * @since   JDK1.0
 */
public
class FilterOutputStream extends OutputStream {
    /**
     * 要过滤的底层输出流。
     */
    protected OutputStream out;

    /**
     * 创建一个基于指定底层输出流的输出流过滤器。
     *
     * @param   out   要分配给字段<tt>this.out</tt>以供后续使用的底层输出流，
     *                或<code>null</code>，如果此实例要创建而没有底层流。
     */
    public FilterOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * 将指定的<code>byte</code>写入此输出流。
     * <p>
     * <code>FilterOutputStream</code>的<code>write</code>方法调用其底层输出流的<code>write</code>方法，
     * 即执行<tt>out.write(b)</tt>。
     * <p>
     * 实现<tt>OutputStream</tt>的抽象<tt>write</tt>方法。
     *
     * @param      b   要写的<code>byte</code>。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void write(int b) throws IOException {
        out.write(b);
    }

    /**
     * 将<code>b.length</code>字节写入此输出流。
     * <p>
     * <code>FilterOutputStream</code>的<code>write</code>方法调用其三个参数的<code>write</code>方法，
     * 参数为<code>b</code>、<code>0</code>和<code>b.length</code>。
     * <p>
     * 请注意，此方法不会调用其底层流的单参数<code>write</code>方法，参数为<code>b</code>。
     *
     * @param      b   要写入的数据。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#write(byte[], int, int)
     */
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * 从指定的<code>byte</code>数组的偏移量<code>off</code>开始，写入<code>len</code>字节到此输出流。
     * <p>
     * <code>FilterOutputStream</code>的<code>write</code>方法对每个要输出的<code>byte</code>调用单参数的<code>write</code>方法。
     * <p>
     * 请注意，此方法不会调用其底层输入流的<code>write</code>方法，参数相同。<code>FilterOutputStream</code>的子类应提供此方法的更高效实现。
     *
     * @param      b     数据。
     * @param      off   数据中的起始偏移量。
     * @param      len   要写的字节数。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#write(int)
     */
    public void write(byte b[], int off, int len) throws IOException {
        if ((off | len | (b.length - (len + off)) | (off + len)) < 0)
            throw new IndexOutOfBoundsException();

        for (int i = 0 ; i < len ; i++) {
            write(b[off + i]);
        }
    }

    /**
     * 刷新此输出流并强制任何缓冲的输出字节写入流。
     * <p>
     * <code>FilterOutputStream</code>的<code>flush</code>方法调用其底层输出流的<code>flush</code>方法。
     *
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     */
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * 关闭此输出流并释放与此流关联的任何系统资源。
     * <p>
     * <code>FilterOutputStream</code>的<code>close</code>方法调用其<code>flush</code>方法，然后调用其底层输出流的<code>close</code>方法。
     *
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#flush()
     * @see        java.io.FilterOutputStream#out
     */
    @SuppressWarnings("try")
    public void close() throws IOException {
        try (OutputStream ostream = out) {
            flush();
        }
    }
}
