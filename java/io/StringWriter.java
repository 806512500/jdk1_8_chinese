/*
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.io;


/**
 * 一个字符流，它将其输出收集在一个字符串缓冲区中，然后可以用来构造一个字符串。
 * <p>
 * 关闭一个 <tt>StringWriter</tt> 没有影响。这个类中的方法可以在流关闭后调用，而不会生成一个
 * <tt>IOException</tt>。
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
     *        可以在缓冲区自动扩展之前容纳的 <tt>char</tt> 值的数量
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
     * 写入一个字符。
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
     * <p> 此方法的调用形式 <tt>out.append(csq)</tt> 的行为与调用
     *
     * <pre>
     *     out.write(csq.toString()) </pre>
     *
     * <p> 完全相同。根据字符序列 <tt>csq</tt> 的 <tt>toString</tt> 方法的规范，
     * 可能不会附加整个序列。例如，调用字符缓冲区的 <tt>toString</tt> 方法将返回一个子序列，
     * 其内容取决于缓冲区的位置和限制。
     *
     * @param  csq
     *         要附加的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将四个字符 <tt>"null"</tt>
     *         附加到此写入器。
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
     * <p> 当 <tt>csq</tt> 不为 <tt>null</tt> 时，此方法的调用形式 <tt>out.append(csq, start,
     * end)</tt> 的行为与调用
     *
     * <pre>
     *     out.write(csq.subSequence(start, end).toString()) </pre>
     *
     * <p> 完全相同。
     *
     * @param  csq
     *         要从中附加子序列的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将四个字符
     *         <tt>"null"</tt> 附加到此写入器。
     *
     * @param  start
     *         子序列的第一个字符的索引
     *
     * @param  end
     *         子序列的最后一个字符之后的字符的索引
     *
     * @return  此写入器
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>start</tt> 或 <tt>end</tt> 为负数，<tt>start</tt>
     *          大于 <tt>end</tt>，或 <tt>end</tt> 大于 <tt>csq.length()</tt>
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
     * <p> 此方法的调用形式 <tt>out.append(c)</tt> 的行为与调用
     *
     * <pre>
     *     out.write(c) </pre>
     *
     * <p> 完全相同。
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
     * 返回缓冲区的当前值作为字符串。
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
     * 关闭一个 <tt>StringWriter</tt> 没有影响。这个类中的方法可以在流关闭后调用，而不会生成一个
     * <tt>IOException</tt>。
     */
    public void close() throws IOException {
    }

}
