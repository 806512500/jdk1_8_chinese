/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 一个字符流，其源是一个字符串。
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class StringReader extends Reader {

    private String str;
    private int length;
    private int next = 0;
    private int mark = 0;

    /**
     * 创建一个新的字符串读取器。
     *
     * @param s  提供字符流的字符串。
     */
    public StringReader(String s) {
        this.str = s;
        this.length = s.length();
    }

    /** 检查流是否已关闭 */
    private void ensureOpen() throws IOException {
        if (str == null)
            throw new IOException("流已关闭");
    }

    /**
     * 读取单个字符。
     *
     * @return     读取的字符，或如果已到达流的末尾则返回 -1
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public int read() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (next >= length)
                return -1;
            return str.charAt(next++);
        }
    }

    /**
     * 将字符读取到数组的一部分中。
     *
     * @param      cbuf  目标缓冲区
     * @param      off   开始写入字符的偏移量
     * @param      len   要读取的最大字符数
     *
     * @return     读取的字符数，或如果已到达流的末尾则返回 -1
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }
            if (next >= length)
                return -1;
            int n = Math.min(length - next, len);
            str.getChars(next, next + n, cbuf, off);
            next += n;
            return n;
        }
    }

    /**
     * 在流中跳过指定数量的字符。返回跳过的字符数。
     *
     * <p>参数 <code>ns</code> 可以是负数，即使 <code>skip</code> 方法在 {@link Reader} 超类中会抛出异常。负数的 <code>ns</code> 会导致流向后跳过。负数的返回值表示向后跳过。不可能向后跳过字符串的开头。
     *
     * <p>如果整个字符串已被读取或跳过，则此方法无效，始终返回 0。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public long skip(long ns) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (next >= length)
                return 0;
            // 用字符串的开头和结尾限制跳过
            long n = Math.min(length - next, ns);
            n = Math.max(-next, n);
            next += n;
            return n;
        }
    }

    /**
     * 告诉此流是否准备好读取。
     *
     * @return 如果下一个 read() 调用保证不会因输入而阻塞，则返回 true
     *
     * @exception  IOException  如果流已关闭
     */
    public boolean ready() throws IOException {
        synchronized (lock) {
        ensureOpen();
        return true;
        }
    }

    /**
     * 告诉此流是否支持 mark() 操作，它确实支持。
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * 标记流中的当前位置。后续的 reset() 调用将重新定位流到此点。
     *
     * @param  readAheadLimit  读取时仍保留标记的字符数限制。因为流的输入来自字符串，所以没有实际的限制，所以这个参数不能为负数，但会被忽略。
     *
     * @exception  IllegalArgumentException  如果 {@code readAheadLimit < 0}
     * @exception  IOException  如果发生 I/O 错误
     */
    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0){
            throw new IllegalArgumentException("读取超前限制 < 0");
        }
        synchronized (lock) {
            ensureOpen();
            mark = next;
        }
    }

    /**
     * 重置流到最近的标记，或如果从未标记过，则重置到字符串的开头。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void reset() throws IOException {
        synchronized (lock) {
            ensureOpen();
            next = mark;
        }
    }

    /**
     * 关闭流并释放与之关联的任何系统资源。一旦流被关闭，进一步的 read()、ready()、mark() 或 reset() 调用将抛出 IOException。关闭已关闭的流没有效果。
     */
    public void close() {
        str = null;
    }
}
