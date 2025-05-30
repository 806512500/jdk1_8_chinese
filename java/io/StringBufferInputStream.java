/*
 * Copyright (c) 1995, 2004, Oracle and/or its affiliates. All rights reserved.
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
 * 该类允许应用程序创建一个输入流，其中读取的字节由字符串的内容提供。
 * 应用程序也可以通过使用 <code>ByteArrayInputStream</code> 从字节数组中读取字节。
 * <p>
 * 该类仅使用每个字符的低八位。
 *
 * @author     Arthur van Hoff
 * @see        java.io.ByteArrayInputStream
 * @see        java.io.StringReader
 * @since      JDK1.0
 * @deprecated 该类不能正确地将字符转换为字节。自 JDK&nbsp;1.1 起，从字符串创建流的首选方法是使用 <code>StringReader</code> 类。
 */
@Deprecated
public
class StringBufferInputStream extends InputStream {
    /**
     * 从输入流缓冲区中读取字节的字符串。
     */
    protected String buffer;

    /**
     * 从输入流缓冲区中读取的下一个字符的索引。
     *
     * @see        java.io.StringBufferInputStream#buffer
     */
    protected int pos;

    /**
     * 输入流缓冲区中的有效字符数。
     *
     * @see        java.io.StringBufferInputStream#buffer
     */
    protected int count;

    /**
     * 创建一个从指定字符串读取数据的字符串输入流。
     *
     * @param      s   底层输入缓冲区。
     */
    public StringBufferInputStream(String s) {
        this.buffer = s;
        count = s.length();
    }

    /**
     * 从该输入流中读取下一个字节的数据。返回的值是一个范围在
     * <code>0</code> 到 <code>255</code> 之间的 <code>int</code>。如果因为到达流的末尾而没有字节可用，
     * 则返回 <code>-1</code>。
     * <p>
     * <code>StringBufferInputStream</code> 的 <code>read</code> 方法不能阻塞。它返回此输入流缓冲区中下一个字符的低八位。
     *
     * @return     下一个字节的数据，或如果到达流的末尾则返回 <code>-1</code>。
     */
    public synchronized int read() {
        return (pos < count) ? (buffer.charAt(pos++) & 0xFF) : -1;
    }

    /**
     * 从该输入流中读取最多 <code>len</code> 个字节的数据到字节数组中。
     * <p>
     * <code>StringBufferInputStream</code> 的 <code>read</code> 方法不能阻塞。它将此输入流缓冲区中的字符的低八位复制到字节数组参数中。
     *
     * @param      b     读取数据的缓冲区。
     * @param      off   数据的起始偏移量。
     * @param      len   最大读取字节数。
     * @return     读入缓冲区的总字节数，或如果因为到达流的末尾而没有更多数据则返回 <code>-1</code>。
     */
    public synchronized int read(byte b[], int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
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
        String  s = buffer;
        int cnt = len;
        while (--cnt >= 0) {
            b[off++] = (byte)s.charAt(pos++);
        }

        return len;
    }

    /**
     * 从该输入流中跳过 <code>n</code> 个输入字节。如果到达输入流的末尾，跳过的字节数可能会更少。
     *
     * @param      n   要跳过的字节数。
     * @return     实际跳过的字节数。
     */
    public synchronized long skip(long n) {
        if (n < 0) {
            return 0;
        }
        if (n > count - pos) {
            n = count - pos;
        }
        pos += n;
        return n;
    }

    /**
     * 返回可以从输入流中读取而不阻塞的字节数。
     *
     * @return     <code>count&nbsp;-&nbsp;pos</code> 的值，即输入缓冲区中剩余的字节数。
     */
    public synchronized int available() {
        return count - pos;
    }

    /**
     * 重置输入流以从该输入流的底层缓冲区的第一个字符开始读取。
     */
    public synchronized void reset() {
        pos = 0;
    }
}
