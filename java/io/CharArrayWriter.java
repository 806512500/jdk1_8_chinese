/*
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;

/**
 * 这个类实现了一个可以作为 Writer 使用的字符缓冲区。
 * 缓冲区在数据写入流时会自动增长。可以使用 toCharArray() 和 toString() 方法检索数据。
 * <P>
 * 注意：在这个类上调用 close() 方法不会产生任何效果，即使流已关闭，该类的方法也可以调用而不会生成 IOException。
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
     * @param initialSize  指定的初始缓冲区大小。
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
     * 将一个字符写入缓冲区。
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
     * @param off       数据的起始偏移量
     * @param len       写入的字符数
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
     * @param  str  要写入的字符串
     * @param  off  从字符串中开始读取字符的偏移量
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
     * <p> 以 <tt>out.append(csq)</tt> 形式调用此方法的行为与以下调用完全相同：
     *
     * <pre>
     *     out.write(csq.toString()) </pre>
     *
     * <p> 根据字符序列 <tt>csq</tt> 的 <tt>toString</tt> 方法的规范，可能不会追加整个序列。例如，调用字符缓冲区的 <tt>toString</tt> 方法将返回一个子序列，其内容取决于缓冲区的位置和限制。
     *
     * @param  csq
     *         要追加的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将四个字符 <tt>"null"</tt> 追加到此写入器。
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
     * <p> 以 <tt>out.append(csq, start, end)</tt> 形式调用此方法时，如果 <tt>csq</tt> 不为 <tt>null</tt>，则行为与以下调用完全相同：
     *
     * <pre>
     *     out.write(csq.subSequence(start, end).toString()) </pre>
     *
     * @param  csq
     *         要从中追加子序列的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将四个字符 <tt>"null"</tt> 追加到此写入器。
     *
     * @param  start
     *         子序列的第一个字符的索引
     *
     * @param  end
     *         子序列最后一个字符之后的字符的索引
     *
     * @return  此写入器
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>start</tt> 或 <tt>end</tt> 为负数，<tt>start</tt> 大于 <tt>end</tt>，或 <tt>end</tt> 大于 <tt>csq.length()</tt>
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
     * <p> 以 <tt>out.append(c)</tt> 形式调用此方法的行为与以下调用完全相同：
     *
     * <pre>
     *     out.write(c) </pre>
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

    /**
     * 重置缓冲区，以便可以再次使用，而不会丢弃已分配的缓冲区。
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
     * 关闭流。此方法不会释放缓冲区，因为其内容可能仍然需要。注意：在此类上调用此方法不会产生任何效果。
     */
    public void close() { }

}
