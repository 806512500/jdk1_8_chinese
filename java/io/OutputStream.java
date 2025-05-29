/*
 * 版权所有 (c) 1994, 2004，Oracle 和/或其附属公司。保留所有权利。
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
 * 此抽象类是所有表示字节输出流的类的超类。输出流接受输出字节并将其发送到某个目的地。
 * <p>
 * 需要定义 <code>OutputStream</code> 子类的应用程序必须至少提供一个写入一个输出字节的方法。
 *
 * @author  Arthur van Hoff
 * @see     java.io.BufferedOutputStream
 * @see     java.io.ByteArrayOutputStream
 * @see     java.io.DataOutputStream
 * @see     java.io.FilterOutputStream
 * @see     java.io.InputStream
 * @see     java.io.OutputStream#write(int)
 * @since   JDK1.0
 */
public abstract class OutputStream implements Closeable, Flushable {
    /**
     * 将指定的字节写入此输出流。对于 <code>write</code> 的一般约定是，一个字节被写入输出流。
     * 要写入的字节是参数 <code>b</code> 的八个低位。<code>b</code> 的 24 个高位被忽略。
     * <p>
     * <code>OutputStream</code> 的子类必须为此方法提供实现。
     *
     * @param      b   要写的 <code>字节</code>。
     * @exception  IOException  如果发生 I/O 错误。特别是，如果输出流已关闭，可能会抛出 <code>IOException</code>。
     */
    public abstract void write(int b) throws IOException;

    /**
     * 从指定的字节数组写入 <code>b.length</code> 个字节到此输出流。对于 <code>write(b)</code> 的一般约定是，
     * 它应该与调用 <code>write(b, 0, b.length)</code> 有完全相同的效果。
     *
     * @param      b   数据。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * 从指定的字节数组的偏移量 <code>off</code> 开始写入 <code>len</code> 个字节到此输出流。
     * 对于 <code>write(b, off, len)</code> 的一般约定是，字节数组 <code>b</code> 中的一些字节按顺序写入输出流；
     * 元素 <code>b[off]</code> 是第一个写入的字节，<code>b[off+len-1]</code> 是此操作写入的最后一个字节。
     * <p>
     * <code>OutputStream</code> 的 <code>write</code> 方法对每个要写入的字节调用一个参数的写入方法。
     * 建议子类覆盖此方法并提供更高效的实现。
     * <p>
     * 如果 <code>b</code> 为 <code>null</code>，则抛出 <code>NullPointerException</code>。
     * <p>
     * 如果 <code>off</code> 为负数，或 <code>len</code> 为负数，或 <code>off+len</code> 大于数组 <code>b</code> 的长度，
     * 则抛出 <tt>IndexOutOfBoundsException</tt>。
     *
     * @param      b     数据。
     * @param      off   数据中的起始偏移量。
     * @param      len   要写的字节数。
     * @exception  IOException  如果发生 I/O 错误。特别是，如果输出流已关闭，可能会抛出 <code>IOException</code>。
     */
    public void write(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        for (int i = 0 ; i < len ; i++) {
            write(b[off + i]);
        }
    }

    /**
     * 刷新此输出流并强制任何缓冲的输出字节被写入。对于 <code>flush</code> 的一般约定是，
     * 调用它是指示如果输出流的实现之前缓冲了任何已写入的字节，这些字节应立即写入其预期的目的地。
     * <p>
     * 如果此流的预期目的地是由底层操作系统提供的抽象，例如文件，那么刷新流仅保证之前写入流的字节被传递给操作系统进行写入；
     * 它不保证这些字节实际上被写入到物理设备，如磁盘驱动器。
     * <p>
     * <code>OutputStream</code> 的 <code>flush</code> 方法不执行任何操作。
     *
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void flush() throws IOException {
    }

    /**
     * 关闭此输出流并释放与此流关联的任何系统资源。对于 <code>close</code> 的一般约定是，它关闭输出流。
     * 关闭的流不能执行输出操作，也不能重新打开。
     * <p>
     * <code>OutputStream</code> 的 <code>close</code> 方法不执行任何操作。
     *
     * @exception  IOException  如果发生 I/O 错误。
     */
    public void close() throws IOException {
    }

}
