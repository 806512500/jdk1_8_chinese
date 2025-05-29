/*
 * 版权所有 (c) 1996, 2005, Oracle 和/或其附属公司。保留所有权利。
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
 * 该类实现了一个可以作为 Writer 使用的字符缓冲区。
 * 当数据写入流时，缓冲区会自动增长。可以使用 toCharArray() 和 toString() 获取数据。
 * <P>
 * 注意：调用此类的 close() 方法不会产生任何效果，即使流已关闭，也可以调用此类的方法
 * 而不会生成 IOException。
 *
 * @author      Herb Jellinek
 * @since       JDK1.1
 */
public
class CharArrayWriter extends Writer {
    /**
     * 存储数据的缓冲区。
     */
    protected char buf[];

    /**
     * 缓冲区中的字符数。
     */
    protected int count;

    /**
     * 创建一个新的 CharArrayWriter。
     */
    public CharArrayWriter() {
        this(32);
    }

    /**
     * 使用指定的初始大小创建一个新的 CharArrayWriter。
     *
     * @param initialSize  指定初始缓冲区大小的整数。
     * @exception IllegalArgumentException 如果 initialSize 为负数
     */
    public CharArrayWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Negative initial size: "
                                               + initialSize);
        }
        buf = new char[initialSize];
    }

    /**
     * 将字符写入缓冲区。
     */
    public void write(int c) {
        synchronized (lock) {
            int newcount = count + 1;
            if (newcount > buf.length) {
                buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
            }
            buf[count] = (char)c;
            count = newcount;
        }
    }

    /**
     * 将字符写入缓冲区。
     * @param c 要写入的数据
     * @param off 数据中的起始偏移量
     * @param len 要写入的字符数
     */
    public void write(char c[], int off, int len) {
        if ((off < 0) || (off > c.length) || (len < 0) ||
            ((off + len) > c.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        synchronized (lock) {
            int newcount = count + len;
            if (newcount > buf.length) {
                buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
            }
            System.arraycopy(c, off, buf, count, len);
            count = newcount;
        }
    }

    /**
     * 将字符串的一部分写入缓冲区。
     * @param  str  要从中写入的字符串
     * @param  off  开始读取字符的偏移量
     * @param  len  要写入的字符数
     */
    public void write(String str, int off, int len) {
        synchronized (lock) {
            int newcount = count + len;
            if (newcount > buf.length) {
                buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
            }
            str.getChars(off, off + len, buf, count);
            count = newcount;
        }
    }

    /**
     * 将缓冲区的内容写入另一个字符流。
     *
     * @param out       要写入的输出流
     * @throws IOException 如果发生 I/O 错误。
     */
    public void writeTo(Writer out) throws IOException {
        synchronized (lock) {
            out.write(buf, 0, count);
        }
    }

    /**
     * 将指定的字符序列追加到此写入器。
     *
     * <p> 以 <tt>out.append(csq)</tt> 形式调用此方法的行为与调用
     *
     * <pre>
     *     out.write(csq.toString()) </pre>
     *
     * <p> 完全相同。根据字符序列 <tt>csq</tt> 的 <tt>toString</tt> 方法的规范，
     * 可能不会追加整个序列。例如，调用字符缓冲区的 <tt>toString</tt> 方法将返回一个子序列，
     * 其内容取决于缓冲区的位置和限制。
     *
     * @param  csq
     *         要追加的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将四个字符 <tt>"null"</tt>
     *         追加到此写入器。
     *
     * @return  此写入器
     *
     * @since  1.5
     */
    public CharArrayWriter append(CharSequence csq) {
        String s = (csq == null ? "null" : csq.toString());
        write(s, 0, s.length());
        return this;
    }

    /**
     * 将指定字符序列的子序列追加到此写入器。
     *
     * <p> 当 <tt>csq</tt> 不为 <tt>null</tt> 时，以 <tt>out.append(csq, start,
     * end)</tt> 形式调用此方法的行为与调用
     *
     * <pre>
     *     out.write(csq.subSequence(start, end).toString()) </pre>
     *
     * <p> 完全相同。
     *
     * @param  csq
     *         要从中追加子序列的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将字符
     *         作为如果 <tt>csq</tt> 包含四个字符 <tt>"null"</tt> 一样追加。
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
     *          如果 <tt>start</tt> 或 <tt>end</tt> 为负数，<tt>start</tt>
     *          大于 <tt>end</tt>，或 <tt>end</tt> 大于 <tt>csq.length()</tt>
     *
     * @since  1.5
     */
    public CharArrayWriter append(CharSequence csq, int start, int end) {
        String s = (csq == null ? "null" : csq).subSequence(start, end).toString();
        write(s, 0, s.length());
        return this;
    }

    /**
     * 将指定的字符追加到此写入器。
     *
     * <p> 以 <tt>out.append(c)</tt> 形式调用此方法的行为与调用
     *
     * <pre>
     *     out.write(c) </pre>
     *
     * <p> 完全相同。
     *
     * @param  c
     *         要追加的 16 位字符
     *
     * @return  此写入器
     *
     * @since 1.5
     */
    public CharArrayWriter append(char c) {
        write(c);
        return this;
    }

}

                /**
     * 重置缓冲区，以便您可以再次使用它，而无需丢弃已分配的缓冲区。
     */
    public void reset() {
        count = 0;
    }

    /**
     * 返回输入数据的副本。
     *
     * @return 从输入数据复制的字符数组。
     */
    public char toCharArray()[] {
        synchronized (lock) {
            return Arrays.copyOf(buf, count);
        }
    }

    /**
     * 返回缓冲区的当前大小。
     *
     * @return 表示缓冲区当前大小的整数。
     */
    public int size() {
        return count;
    }

    /**
     * 将输入数据转换为字符串。
     * @return 字符串。
     */
    public String toString() {
        synchronized (lock) {
            return new String(buf, 0, count);
        }
    }

    /**
     * 刷新流。
     */
    public void flush() { }

    /**
     * 关闭流。此方法不会释放缓冲区，因为其内容可能仍然需要。注意：在此类中调用此方法将不会产生任何效果。
     */
    public void close() { }

}
