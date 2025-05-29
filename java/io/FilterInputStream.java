/*
 * 版权所有 (c) 1994, 2010, Oracle 和/或其子公司。保留所有权利。
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
 * <code>FilterInputStream</code> 包含另一个输入流，该输入流作为其基本数据源，可能在传输过程中转换数据或提供额外的功能。类 <code>FilterInputStream</code>
 * 本身只是覆盖了 <code>InputStream</code> 的所有方法，这些方法将所有请求传递给包含的输入流。<code>FilterInputStream</code> 的子类
 * 可能会进一步覆盖其中的一些方法，并可能提供额外的方法和字段。
 *
 * @author  Jonathan Payne
 * @since   JDK1.0
 */
public
class FilterInputStream extends InputStream {
    /**
     * 要过滤的输入流。
     */
    protected volatile InputStream in;

    /**
     * 通过将参数 <code>in</code> 分配给字段 <code>this.in</code> 来创建 <code>FilterInputStream</code>，
     * 以便稍后使用。
     *
     * @param   in   底层输入流，或 <code>null</code>，如果此实例是在没有底层流的情况下创建的。
     */
    protected FilterInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * 从该输入流中读取下一个字节的数据。该字节的值作为范围 <code>0</code> 到 <code>255</code> 的 <code>int</code> 返回。
     * 如果因为到达流的末尾而没有可用的字节，则返回值 <code>-1</code>。此方法会阻塞，直到有输入数据可用，检测到流的末尾，或抛出异常。
     * <p>
     * 此方法只是执行 <code>in.read()</code> 并返回结果。
     *
     * @return     下一个字节的数据，或如果到达流的末尾则返回 <code>-1</code>。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public int read() throws IOException {
        return in.read();
    }

    /**
     * 从该输入流中读取最多 <code>byte.length</code> 个字节的数据到字节数组中。此方法会阻塞，直到有输入数据可用。
     * <p>
     * 此方法只是执行调用 <code>read(b, 0, b.length)</code> 并返回结果。重要的是它不执行 <code>in.read(b)</code>；
     * <code>FilterInputStream</code> 的某些子类依赖于实际使用的实现策略。
     *
     * @param      b   读取数据的缓冲区。
     * @return     读入缓冲区的总字节数，或如果因为到达流的末尾而没有更多数据则返回 <code>-1</code>。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterInputStream#read(byte[], int, int)
     */
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * 从该输入流中读取最多 <code>len</code> 个字节的数据到字节数组中。如果 <code>len</code> 不为零，该方法会阻塞，直到有输入数据可用；
     * 否则，不读取任何字节并返回 <code>0</code>。
     * <p>
     * 此方法只是执行 <code>in.read(b, off, len)</code> 并返回结果。
     *
     * @param      b     读取数据的缓冲区。
     * @param      off   目标数组 <code>b</code> 中的开始偏移量。
     * @param      len   最大读取字节数。
     * @return     读入缓冲区的总字节数，或如果因为到达流的末尾而没有更多数据则返回 <code>-1</code>。
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，<code>len</code> 为负数，或 <code>len</code> 大于 <code>b.length - off</code>。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public int read(byte b[], int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    /**
     * 从输入流中跳过并丢弃 <code>n</code> 个字节的数据。由于各种原因，<code>skip</code> 方法可能会跳过较少的字节数，
     * 可能是 <code>0</code>。实际跳过的字节数被返回。
     * <p>
     * 此方法只是执行 <code>in.skip(n)</code>。
     *
     * @param      n   要跳过的字节数。
     * @return     实际跳过的字节数。
     * @exception  IOException  如果流不支持寻址，或发生其他 I/O 错误。
     */
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    /**
     * 返回从该输入流中可以读取（或跳过）的字节数的估计值，而不会阻塞下一个调用该输入流方法的调用者。下一个调用者可能是同一个线程或另一个线程。
     * 单次读取或跳过这么多字节不会阻塞，但可能会读取或跳过较少的字节。
     * <p>
     * 此方法返回 {@link #in in}.available() 的结果。
     *
     * @return     从该输入流中可以读取（或跳过）的字节数的估计值，而不会阻塞。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public int available() throws IOException {
        return in.available();
    }

    /**
     * 关闭此输入流并释放与此流关联的任何系统资源。
     * 此
     * 方法只是执行 <code>in.close()</code>。
     *
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public void close() throws IOException {
        in.close();
    }

}

                /**
     * 标记此输入流中的当前位置。后续对 <code>reset</code> 方法的调用会将此流重新定位到最后标记的位置，
     * 以便后续的读取会重新读取相同的字节。
     * <p>
     * <code>readlimit</code> 参数告诉此输入流在标记位置失效之前允许读取多少字节。
     * <p>
     * 此方法只是执行 <code>in.mark(readlimit)</code>。
     *
     * @param   readlimit   在标记位置失效之前可以读取的最大字节数。
     * @see     java.io.FilterInputStream#in
     * @see     java.io.FilterInputStream#reset()
     */
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    /**
     * 将此流重新定位到上次调用 <code>mark</code> 方法时的位置。
     * <p>
     * 此方法
     * 只是执行 <code>in.reset()</code>。
     * <p>
     * 流标记旨在用于需要向前读取一点以查看流中的内容的情况。
     * 通常这最容易通过调用一些通用解析器来完成。如果流是解析器处理的类型，它会愉快地继续处理。
     * 如果流不是那种类型，解析器在失败时应该抛出异常。
     * 如果这种情况发生在 readlimit 字节内，它允许外部代码重置流并尝试另一个解析器。
     *
     * @exception  IOException  如果流未被标记或标记已失效。
     * @see        java.io.FilterInputStream#in
     * @see        java.io.FilterInputStream#mark(int)
     */
    public synchronized void reset() throws IOException {
        in.reset();
    }

    /**
     * 测试此输入流是否支持 <code>mark</code>
     * 和 <code>reset</code> 方法。
     * 此方法
     * 只是执行 <code>in.markSupported()</code>。
     *
     * @return  <code>true</code> 如果此流类型支持 <code>mark</code> 和 <code>reset</code> 方法；
     *          否则返回 <code>false</code>。
     * @see     java.io.FilterInputStream#in
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return in.markSupported();
    }
}
