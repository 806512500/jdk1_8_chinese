/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 一个 <code>ByteArrayInputStream</code> 包含
 * 一个内部缓冲区，该缓冲区包含可以从流中读取的字节。一个内部
 * 计数器跟踪 <code>read</code> 方法要提供的下一个字节。
 * <p>
 * 关闭一个 <tt>ByteArrayInputStream</tt> 没有效果。此类中的方法
 * 在流关闭后仍可调用，而不会生成 <tt>IOException</tt>。
 *
 * @author  Arthur van Hoff
 * @see     java.io.StringBufferInputStream
 * @since   JDK1.0
 */
public
class ByteArrayInputStream extends InputStream {

    /**
     * 由流的创建者提供的一个字节数组。元素 <code>buf[0]</code>
     * 到 <code>buf[count-1]</code> 是可以从
     * 流中读取的唯一字节；元素 <code>buf[pos]</code> 是
     * 要读取的下一个字节。
     */
    protected byte buf[];

    /**
     * 从输入流缓冲区中读取的下一个字符的索引。
     * 此值应始终为非负数
     * 并且不大于 <code>count</code> 的值。
     * 从输入流缓冲区中读取的下一个字节
     * 将是 <code>buf[pos]</code>。
     */
    protected int pos;

    /**
     * 流中的当前位置标记。
     * ByteArrayInputStream 对象在构造时默认在位置零处标记。
     * 可以通过 <code>mark()</code> 方法在缓冲区中的另一个
     * 位置进行标记。通过 <code>reset()</code> 方法
     * 将当前缓冲区位置设置为该点。
     * <p>
     * 如果没有设置标记，则标记的值为传递给构造函数的偏移量
     * （如果未提供偏移量，则为 0）。
     *
     * @since   JDK1.1
     */
    protected int mark = 0;

    /**
     * 输入流缓冲区中最后一个有效字符的索引加一。
     * 此值应始终为非负数
     * 并且不大于 <code>buf</code> 的长度。
     * 它比 <code>buf</code> 中可以读取的最后一个字节的位置大一。
     */
    protected int count;

    /**
     * 创建一个 <code>ByteArrayInputStream</code>
     * 使其使用 <code>buf</code> 作为其
     * 缓冲数组。缓冲数组不会被复制。
     * <code>pos</code> 的初始值
     * 是 <code>0</code>，<code>count</code> 的初始值
     * 是 <code>buf</code> 的长度。
     *
     * @param   buf   输入缓冲区。
     */
    public ByteArrayInputStream(byte buf[]) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }

    /**
     * 创建一个 <code>ByteArrayInputStream</code>
     * 使用 <code>buf</code> 作为其
     * 缓冲数组。<code>pos</code> 的初始值
     * 是 <code>offset</code>，<code>count</code> 的初始值
     * 是 <code>offset+length</code> 和 <code>buf.length</code> 中的较小值。
     * 缓冲数组不会被复制。缓冲区的标记
     * 被设置为指定的偏移量。
     *
     * @param   buf      输入缓冲区。
     * @param   offset   缓冲区中要读取的第一个字节的偏移量。
     * @param   length   从缓冲区中读取的最大字节数。
     */
    public ByteArrayInputStream(byte buf[], int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.mark = offset;
    }

    /**
     * 从该输入流中读取下一个字节的数据。该字节的值
     * 作为 <code>int</code> 返回，范围为
     * <code>0</code> 到 <code>255</code>。如果因为
     * 流的末尾已到达而没有字节可用，则返回值
     * <code>-1</code>。
     * <p>
     * 此 <code>read</code> 方法
     * 不会阻塞。
     *
     * @return  下一个字节的数据，或者如果流的末尾已到达则返回 <code>-1</code>。
     */
    public synchronized int read() {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }

    /**
     * 从该输入流中读取最多 <code>len</code> 个字节的数据到字节数组中。
     * 如果 <code>pos</code> 等于 <code>count</code>，
     * 则返回 <code>-1</code> 表示文件结束。否则，读取的字节数 <code>k</code>
     * 等于 <code>len</code> 和 <code>count-pos</code> 中的较小值。
     * 如果 <code>k</code> 为正，则将
     * <code>buf[pos]</code> 到 <code>buf[pos+k-1]</code>
     * 复制到 <code>b[off]</code> 到
     * <code>b[off+k-1]</code>，复制方式与 <code>System.arraycopy</code> 相同。
     * 将 <code>k</code> 加到 <code>pos</code> 上并返回 <code>k</code>。
     * <p>
     * 此 <code>read</code> 方法不会阻塞。
     *
     * @param   b     存储数据的缓冲区。
     * @param   off   目标数组 <code>b</code> 中的起始偏移量。
     * @param   len   要读取的最大字节数。
     * @return  读入缓冲区的总字节数，或者如果因为流的末尾已到达而没有更多数据则返回 <code>-1</code>。
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，
     * <code>len</code> 为负数，或者 <code>len</code> 大于 <code>b.length - off</code>。
     */
    public synchronized int read(byte b[], int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        if (pos >= count) {
            return -1;
        }

        int avail = count - pos;
        if (len > avail) {
            len = avail;
        }
        if (len <= 0) {
            return 0;
        }
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    /**
     * 从该输入流中跳过 <code>n</code> 个字节。如果到达输入流的末尾，
     * 可能会跳过更少的字节。
     * 要跳过的实际字节数 <code>k</code>
     * 等于 <code>n</code> 和 <code>count-pos</code> 中的较小值。
     * 将 <code>k</code> 加到 <code>pos</code> 上并返回 <code>k</code>。
     *
     * @param   n   要跳过的字节数。
     * @return  实际跳过的字节数。
     */
    public synchronized long skip(long n) {
        long k = count - pos;
        if (n < k) {
            k = n < 0 ? 0 : n;
        }

        pos += k;
        return k;
    }

    /**
     * 返回可以从该输入流中读取（或跳过）的剩余字节数。
     * <p>
     * 返回的值是 <code>count&nbsp;- pos</code>，
     * 即从输入缓冲区中剩余可读取的字节数。
     *
     * @return  从该输入流中读取（或跳过）的剩余字节数，不会阻塞。
     */
    public synchronized int available() {
        return count - pos;
    }

    /**
     * 测试此 <code>InputStream</code> 是否支持 mark/reset。对于
     * <code>ByteArrayInputStream</code>，<code>markSupported</code> 方法
     * 始终返回 <code>true</code>。
     *
     * @since   JDK1.1
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * 设置流中的当前标记位置。
     * ByteArrayInputStream 对象在构造时默认在位置零处标记。
     * 可以通过此方法在缓冲区中的另一个
     * 位置进行标记。
     * <p>
     * 如果没有设置标记，则标记的值为传递给构造函数的偏移量
     * （如果未提供偏移量，则为 0）。
     *
     * <p> 注意：对于此类，<code>readAheadLimit</code>
     * 没有意义。
     *
     * @since   JDK1.1
     */
    public void mark(int readAheadLimit) {
        mark = pos;
    }

    /**
     * 将缓冲区重置到标记位置。标记位置
     * 为 0，除非设置了另一个位置或在构造函数中指定了偏移量。
     */
    public synchronized void reset() {
        pos = mark;
    }

    /**
     * 关闭一个 <tt>ByteArrayInputStream</tt> 没有效果。此类中的方法
     * 在流关闭后仍可调用，而不会生成 <tt>IOException</tt>。
     */
    public void close() throws IOException {
    }

}
