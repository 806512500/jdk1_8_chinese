/*
 * 版权所有 (c) 1996, 2004, Oracle 和/或其附属公司。保留所有权利。
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
 * 一个字符流，它收集输出到一个字符串缓冲区中，然后可以用来构造一个字符串。
 * <p>
 * 关闭 <tt>StringWriter</tt> 没有影响。此类中的方法可以在流关闭后调用，而不会生成 <tt>IOException</tt>。
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class StringWriter extends Writer {

    private StringBuffer buf;

    /**
     * 使用默认的初始字符串缓冲区大小创建一个新的字符串写入器。
     */
    public StringWriter() {
        buf = new StringBuffer();
        lock = buf;
    }

    /**
     * 使用指定的初始字符串缓冲区大小创建一个新的字符串写入器。
     *
     * @param initialSize
     *        在自动扩展之前可以放入此缓冲区的 <tt>char</tt> 值的数量
     *
     * @throws IllegalArgumentException
     *         如果 <tt>initialSize</tt> 为负数
     */
    public StringWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Negative buffer size");
        }
        buf = new StringBuffer(initialSize);
        lock = buf;
    }

    /**
     * 写入单个字符。
     */
    public void write(int c) {
        buf.append((char) c);
    }

    /**
     * 写入字符数组的一部分。
     *
     * @param  cbuf  字符数组
     * @param  off   开始写入字符的偏移量
     * @param  len   要写入的字符数
     */
    public void write(char cbuf[], int off, int len) {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
            ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        buf.append(cbuf, off, len);
    }

    /**
     * 写入一个字符串。
     */
    public void write(String str) {
        buf.append(str);
    }

    /**
     * 写入字符串的一部分。
     *
     * @param  str  要写入的字符串
     * @param  off  开始写入字符的偏移量
     * @param  len  要写入的字符数
     */
    public void write(String str, int off, int len)  {
        buf.append(str.substring(off, off + len));
    }

    /**
     * 将指定的字符序列附加到此写入器。
     *
     * <p> 形如 <tt>out.append(csq)</tt> 的此方法调用的行为与调用
     *
     * <pre>
     *     out.write(csq.toString()) </pre>
     *
     * <p> 完全相同。根据字符序列 <tt>csq</tt> 的 <tt>toString</tt> 方法的规范，整个序列可能不会被附加。例如，调用字符缓冲区的 <tt>toString</tt> 方法将返回一个子序列，其内容取决于缓冲区的位置和限制。
     *
     * @param  csq
     *         要附加的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将四个字符 <tt>"null"</tt> 附加到此写入器。
     *
     * @return  此写入器
     *
     * @since  1.5
     */
    public StringWriter append(CharSequence csq) {
        if (csq == null)
            write("null");
        else
            write(csq.toString());
        return this;
    }

    /**
     * 将指定字符序列的子序列附加到此写入器。
     *
     * <p> 当 <tt>csq</tt> 不为 <tt>null</tt> 时，形如 <tt>out.append(csq, start,
     * end)</tt> 的此方法调用的行为与调用
     *
     * <pre>
     *     out.write(csq.subSequence(start, end).toString()) </pre>
     *
     * 完全相同。
     *
     * @param  csq
     *         将从中附加子序列的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将字符附加为 <tt>csq</tt> 包含四个字符 <tt>"null"</tt>。
     *
     * @param  start
     *         子序列中第一个字符的索引
     *
     * @param  end
     *         子序列中最后一个字符之后的字符索引
     *
     * @return  此写入器
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>start</tt> 或 <tt>end</tt> 为负数，<tt>start</tt> 大于 <tt>end</tt>，或 <tt>end</tt> 大于 <tt>csq.length()</tt>
     *
     * @since  1.5
     */
    public StringWriter append(CharSequence csq, int start, int end) {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }

    /**
     * 将指定的字符附加到此写入器。
     *
     * <p> 形如 <tt>out.append(c)</tt> 的此方法调用的行为与调用
     *
     * <pre>
     *     out.write(c) </pre>
     *
     * 完全相同。
     *
     * @param  c
     *         要附加的 16 位字符
     *
     * @return  此写入器
     *
     * @since 1.5
     */
    public StringWriter append(char c) {
        write(c);
        return this;
    }

    /**
     * 以字符串形式返回缓冲区的当前值。
     */
    public String toString() {
        return buf.toString();
    }

    /**
     * 返回当前缓冲区值的字符串缓冲区。
     *
     * @return 持有当前缓冲区值的 StringBuffer。
     */
    public StringBuffer getBuffer() {
        return buf;
    }

    /**
     * 刷新流。
     */
    public void flush() {
    }

    /**
     * 关闭 <tt>StringWriter</tt> 没有影响。此方法中的方法可以在流关闭后调用，而不会生成 <tt>IOException</tt>。
     */
    public void close() throws IOException {
    }

}
