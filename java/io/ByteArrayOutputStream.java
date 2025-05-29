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

import java.util.Arrays;

/**
 * 该类实现了一个输出流，其中数据被写入字节数组。缓冲区会随着数据的写入自动增长。
 * 可以使用 <code>toByteArray()</code> 和 <code>toString()</code> 获取数据。
 * <p>
 * 关闭 <tt>ByteArrayOutputStream</tt> 无效。即使流已关闭，该类中的方法也可以调用而不会生成 <tt>IOException</tt>。
 *
 * @author  Arthur van Hoff
 * @since   JDK1.0
 */

public class ByteArrayOutputStream extends OutputStream {

    /**
     * 存储数据的缓冲区。
     */
    protected byte buf[];

    /**
     * 缓冲区中的有效字节数。
     */
    protected int count;

    /**
     * 创建一个新的字节数组输出流。缓冲区的初始容量为 32 字节，但其大小会在必要时增加。
     */
    public ByteArrayOutputStream() {
        this(32);
    }

    /**
     * 创建一个新的字节数组输出流，缓冲区的容量为指定的大小（以字节为单位）。
     *
     * @param   size   初始大小。
     * @exception  IllegalArgumentException 如果大小为负数。
     */
    public ByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("负的初始大小: "
                                               + size);
        }
        buf = new byte[size];
    }

    /**
     * 如果必要，增加容量以确保它可以至少容纳由最小容量参数指定的元素数量。
     *
     * @param minCapacity 所需的最小容量
     * @throws OutOfMemoryError 如果 {@code minCapacity < 0}。这被解释为请求无法满足的大容量
     * {@code (long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)}。
     */
    private void ensureCapacity(int minCapacity) {
        // 防止溢出的代码
        if (minCapacity - buf.length > 0)
            grow(minCapacity);
    }

    /**
     * 可分配数组的最大大小。
     * 一些虚拟机在数组中预留了一些头部字。
     * 尝试分配更大的数组可能会导致
     * OutOfMemoryError: 请求的数组大小超过虚拟机限制
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 增加容量以确保它可以至少容纳由最小容量参数指定的元素数量。
     *
     * @param minCapacity 所需的最小容量
     */
    private void grow(int minCapacity) {
        // 防止溢出的代码
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        buf = Arrays.copyOf(buf, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // 溢出
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

    /**
     * 将指定的字节写入此字节数组输出流。
     *
     * @param   b   要写入的字节。
     */
    public synchronized void write(int b) {
        ensureCapacity(count + 1);
        buf[count] = (byte) b;
        count += 1;
    }

    /**
     * 从指定的字节数组中写入 <code>len</code> 个字节，从偏移量 <code>off</code> 开始写入此字节数组输出流。
     *
     * @param   b     数据。
     * @param   off   数据中的起始偏移量。
     * @param   len   要写入的字节数。
     */
    public synchronized void write(byte b[], int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) - b.length > 0)) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(count + len);
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    /**
     * 将此字节数组输出流的全部内容写入指定的输出流参数，就像调用输出流的 write 方法使用 <code>out.write(buf, 0, count)</code> 一样。
     *
     * @param      out   要写入数据的输出流。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public synchronized void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    /**
     * 将此字节数组输出流的 <code>count</code> 字段重置为零，以便丢弃输出流中当前累积的所有输出。可以再次使用输出流，重用已分配的缓冲区空间。
     *
     * @see     java.io.ByteArrayInputStream#count
     */
    public synchronized void reset() {
        count = 0;
    }

    /**
     * 创建一个新的字节数组。其大小为当前输出流的大小，缓冲区中的有效内容已复制到其中。
     *
     * @return  作为字节数组的此输出流的当前内容。
     * @see     java.io.ByteArrayOutputStream#size()
     */
    public synchronized byte toByteArray()[] {
        return Arrays.copyOf(buf, count);
    }

    /**
     * 返回缓冲区的当前大小。
     *
     * @return  <code>count</code> 字段的值，即此输出流中的有效字节数。
     * @see     java.io.ByteArrayOutputStream#count
     */
    public synchronized int size() {
        return count;
    }

    /**
     * 将缓冲区的内容转换为字符串，使用平台的默认字符集解码字节。新 <tt>String</tt> 的长度是字符集的函数，因此可能不等于缓冲区的大小。
     *
     * <p> 此方法始终用平台默认字符集的默认替换字符串替换格式错误的输入和无法映射的字符序列。如果需要对解码过程进行更多控制，应使用 {@linkplain java.nio.charset.CharsetDecoder} 类。
     *
     * @return 从缓冲区内容解码的字符串。
     * @since  JDK1.1
     */
    public synchronized String toString() {
        return new String(buf, 0, count);
    }

}

                /**
     * 将缓冲区的内容转换为字符串，通过使用命名的 {@link java.nio.charset.Charset 字符集} 解码字节。
     * 新字符串的长度取决于字符集，因此可能不等于字节数组的长度。
     *
     * <p>此方法始终使用该字符集的默认替换字符串替换格式错误的输入和无法映射的字符序列。如果需要对解码过程进行更多控制，
     * 应使用 {@link java.nio.charset.CharsetDecoder} 类。
     *
     * @param      charsetName  支持的 {@link java.nio.charset.Charset 字符集} 的名称
     * @return     从缓冲区内容解码的字符串。
     * @exception  UnsupportedEncodingException
     *             如果命名的字符集不受支持
     * @since      JDK1.1
     */
    public synchronized String toString(String charsetName)
        throws UnsupportedEncodingException
    {
        return new String(buf, 0, count, charsetName);
    }

    /**
     * 创建一个新的分配的字符串。其大小是输出流的当前大小，缓冲区中的有效内容已被复制到其中。结果字符串中的每个字符 <i>c</i>
     * 都是从字节数组中的相应元素 <i>b</i> 构造的，使得：
     * <blockquote><pre>
     *     c == (char)(((hibyte &amp; 0xff) &lt;&lt; 8) | (b &amp; 0xff))
     * </pre></blockquote>
     *
     * @deprecated 此方法不能正确地将字节转换为字符。自 JDK&nbsp;1.1 起，推荐的方法是通过带有编码名称参数的
     * <code>toString(String enc)</code> 方法，或使用平台默认字符编码的 <code>toString()</code> 方法来实现。
     *
     * @param      hibyte    每个结果 Unicode 字符的高字节。
     * @return     输出流的当前内容，作为字符串。
     * @see        java.io.ByteArrayOutputStream#size()
     * @see        java.io.ByteArrayOutputStream#toString(String)
     * @see        java.io.ByteArrayOutputStream#toString()
     */
    @Deprecated
    public synchronized String toString(int hibyte) {
        return new String(buf, hibyte, 0, count);
    }

    /**
     * 关闭 <tt>ByteArrayOutputStream</tt> 没有作用。在流关闭后，可以调用此类中的方法而不会生成 <tt>IOException</tt>。
     */
    public void close() throws IOException {
    }

}
