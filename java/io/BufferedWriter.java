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
 * 将文本写入字符输出流，通过缓冲字符以提供高效地写入单个字符、数组和字符串。
 *
 * <p> 可以指定缓冲区大小，也可以接受默认大小。默认大小对于大多数用途来说已经足够大。
 *
 * <p> 提供了一个 newLine() 方法，该方法使用平台自身的行分隔符，该分隔符由系统属性 <tt>line.separator</tt> 定义。
 * 并非所有平台都使用换行符 ('\n') 来终止行。因此，调用此方法来终止每一行输出比直接写入换行符更为可取。
 *
 * <p> 通常情况下，Writer 会立即将其输出发送到底层的字符或字节流。除非需要立即输出，否则建议在任何 write() 操作可能代价较高的 Writer（如 FileWriter 和 OutputStreamWriter）周围包装一个 BufferedWriter。例如，
 *
 * <pre>
 * PrintWriter out
 *   = new PrintWriter(new BufferedWriter(new FileWriter("foo.out")));
 * </pre>
 *
 * 将缓冲 PrintWriter 的输出到文件。如果没有缓冲，每次调用 print() 方法都会导致字符被转换为字节并立即写入文件，这可能非常低效。
 *
 * @see PrintWriter
 * @see FileWriter
 * @see OutputStreamWriter
 * @see java.nio.file.Files#newBufferedWriter
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class BufferedWriter extends Writer {

    private Writer out;

    private char cb[];
    private int nChars, nextChar;

    private static int defaultCharBufferSize = 8192;

    /**
     * 行分隔符字符串。这是在流创建时的 line.separator 属性的值。
     */
    private String lineSeparator;

    /**
     * 创建一个使用默认大小输出缓冲区的缓冲字符输出流。
     *
     * @param  out  一个 Writer
     */
    public BufferedWriter(Writer out) {
        this(out, defaultCharBufferSize);
    }

    /**
     * 创建一个使用指定大小输出缓冲区的新缓冲字符输出流。
     *
     * @param  out  一个 Writer
     * @param  sz   输出缓冲区大小，一个正整数
     *
     * @exception  IllegalArgumentException  如果 {@code sz <= 0}
     */
    public BufferedWriter(Writer out, int sz) {
        super(out);
        if (sz <= 0)
            throw new IllegalArgumentException("Buffer size <= 0");
        this.out = out;
        cb = new char[sz];
        nChars = sz;
        nextChar = 0;

        lineSeparator = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("line.separator"));
    }

    /** 检查流是否未关闭 */
    private void ensureOpen() throws IOException {
        if (out == null)
            throw new IOException("Stream closed");
    }

    /**
     * 将输出缓冲区刷新到底层字符流，但不刷新流本身。此方法不是私有的，以便 PrintStream 可以调用它。
     */
    void flushBuffer() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (nextChar == 0)
                return;
            out.write(cb, 0, nextChar);
            nextChar = 0;
        }
    }

    /**
     * 写入单个字符。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void write(int c) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (nextChar >= nChars)
                flushBuffer();
            cb[nextChar++] = (char) c;
        }
    }

    /**
     * 我们自己的小 min 方法，以避免在文件描述符用尽且尝试打印堆栈跟踪时加载 java.lang.Math。
     */
    private int min(int a, int b) {
        if (a < b) return a;
        return b;
    }

    /**
     * 写入字符数组的一部分。
     *
     * <p> 通常情况下，此方法将字符从给定数组存储到此流的缓冲区中，必要时将缓冲区刷新到底层流。如果请求的长度至少与缓冲区大小相同，则此方法将刷新缓冲区并将字符直接写入底层流。因此，冗余的
     * <code>BufferedWriter</code> 不会不必要的复制数据。
     *
     * @param  cbuf  字符数组
     * @param  off   从哪个位置开始读取字符
     * @param  len   要写入的字符数
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }

            if (len >= nChars) {
                /* 如果请求长度超过输出缓冲区的大小，
                   刷新缓冲区，然后直接写入数据。这样，缓冲流将无害地级联。 */
                flushBuffer();
                out.write(cbuf, off, len);
                return;
            }

            int b = off, t = off + len;
            while (b < t) {
                int d = min(nChars - nextChar, t - b);
                System.arraycopy(cbuf, b, cb, nextChar, d);
                b += d;
                nextChar += d;
                if (nextChar >= nChars)
                    flushBuffer();
            }
        }
    }

    /**
     * 写入字符串的一部分。
     *
     * <p> 如果 <tt>len</tt> 参数的值为负，则不写入任何字符。这与该方法在
     * {@linkplain java.io.Writer#write(java.lang.String,int,int)
     * 超类} 中的规范相反，该规范要求抛出 {@link IndexOutOfBoundsException}。
     *
     * @param  s     要写入的字符串
     * @param  off   从哪个位置开始读取字符
     * @param  len   要写入的字符数
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void write(String s, int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();

            int b = off, t = off + len;
            while (b < t) {
                int d = min(nChars - nextChar, t - b);
                s.getChars(b, b + d, cb, nextChar);
                b += d;
                nextChar += d;
                if (nextChar >= nChars)
                    flushBuffer();
            }
        }
    }

    /**
     * 写入行分隔符。行分隔符字符串由系统属性 <tt>line.separator</tt> 定义，不一定是一个单独的换行符 ('\n')。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void newLine() throws IOException {
        write(lineSeparator);
    }

    /**
     * 刷新流。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void flush() throws IOException {
        synchronized (lock) {
            flushBuffer();
            out.flush();
        }
    }

    @SuppressWarnings("try")
    public void close() throws IOException {
        synchronized (lock) {
            if (out == null) {
                return;
            }
            try (Writer w = out) {
                flushBuffer();
            } finally {
                out = null;
                cb = null;
            }
        }
    }
}
