/*
 * Copyright (c) 1996, 2019, Oracle and/or its affiliates. All rights reserved.
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
 * 一个带行号跟踪功能的缓冲字符输入流。此类定义了 {@link #setLineNumber(int)} 和 {@link
 * #getLineNumber()} 方法，用于设置和获取当前行号。
 *
 * <p> 默认情况下，行号从 0 开始。每当读取数据时遇到 <a href="#lt">行终止符</a>，行号会递增，可以通过调用 <tt>setLineNumber(int)</tt> 方法更改行号。
 * 但是，<tt>setLineNumber(int)</tt> 不会实际改变流中的当前位置；它只会改变 <tt>getLineNumber()</tt> 返回的值。
 *
 * <p> 行被认为是由以下任意一个终止符结束：<a name="lt">行终止符</a> 包括换行符 ('\n')、回车符 ('\r') 或回车符后紧跟换行符。
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class LineNumberReader extends BufferedReader {

    /** 当前行号 */
    private int lineNumber = 0;

    /** 标记的行号，如果有的话 */
    private int markedLineNumber; // 默认为 0

    /** 如果下一个字符是换行符，则跳过它 */
    private boolean skipLF;

    /** 设置标记时的 skipLF 标志 */
    private boolean markedSkipLF;

    /**
     * 创建一个新的行号读取器，使用默认的输入缓冲区大小。
     *
     * @param  in
     *         提供底层流的 Reader 对象
     */
    public LineNumberReader(Reader in) {
        super(in);
    }

    /**
     * 创建一个新的行号读取器，将字符读入指定大小的缓冲区。
     *
     * @param  in
     *         提供底层流的 Reader 对象
     *
     * @param  sz
     *         指定缓冲区大小的 int
     */
    public LineNumberReader(Reader in, int sz) {
        super(in, sz);
    }

    /**
     * 设置当前行号。
     *
     * @param  lineNumber
     *         指定行号的 int
     *
     * @see #getLineNumber
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * 获取当前行号。
     *
     * @return  当前行号
     *
     * @see #setLineNumber
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * 读取一个字符。 <a href="#lt">行终止符</a> 被压缩成单个换行符 ('\n')。每当读取到行终止符时，当前行号递增。
     *
     * @return  读取的字符，或在到达流末尾时返回 -1
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    @SuppressWarnings("fallthrough")
    public int read() throws IOException {
        synchronized (lock) {
            int c = super.read();
            if (skipLF) {
                if (c == '\n')
                    c = super.read();
                skipLF = false;
            }
            switch (c) {
            case '\r':
                skipLF = true;
            case '\n':          /* Fall through */
                lineNumber++;
                return '\n';
            }
            return c;
        }
    }

    /**
     * 读取字符到数组的一部分。每当读取到 <a
     * href="#lt">行终止符</a> 时，当前行号递增。
     *
     * @param  cbuf
     *         目标缓冲区
     *
     * @param  off
     *         开始存储字符的偏移量
     *
     * @param  len
     *         最大读取字符数
     *
     * @return  读取的字符数，或在已到达流末尾时返回 -1
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    @SuppressWarnings("fallthrough")
    public int read(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            int n = super.read(cbuf, off, len);

            for (int i = off; i < off + n; i++) {
                int c = cbuf[i];
                if (skipLF) {
                    skipLF = false;
                    if (c == '\n')
                        continue;
                }
                switch (c) {
                case '\r':
                    skipLF = true;
                case '\n':      /* Fall through */
                    lineNumber++;
                    break;
                }
            }

            return n;
        }
    }

    /**
     * 读取一行文本。每当读取到 <a href="#lt">行终止符</a> 时，当前行号递增。
     *
     * @return  包含行内容的字符串，不包括 <a href="#lt">行终止符</a>，或在到达流末尾时返回 <tt>null</tt>
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public String readLine() throws IOException {
        synchronized (lock) {
            String l = super.readLine(skipLF);
            skipLF = false;
            if (l != null)
                lineNumber++;
            return l;
        }
    }

    /** 最大跳过缓冲区大小 */
    private static final int maxSkipBufferSize = 8192;

    /** 跳过缓冲区，直到分配时为 null */
    private char skipBuffer[] = null;

    /**
     * 跳过字符。
     *
     * @param  n
     *         要跳过的字符数
     *
     * @return  实际跳过的字符数
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @throws  IllegalArgumentException
     *          如果 <tt>n</tt> 为负数
     */
    public long skip(long n) throws IOException {
        if (n < 0)
            throw new IllegalArgumentException("skip() value is negative");
        int nn = (int) Math.min(n, maxSkipBufferSize);
        synchronized (lock) {
            if ((skipBuffer == null) || (skipBuffer.length < nn))
                skipBuffer = new char[nn];
            long r = n;
            while (r > 0) {
                int nc = read(skipBuffer, 0, (int) Math.min(r, nn));
                if (nc == -1)
                    break;
                r -= nc;
            }
            return n - r;
        }
    }

    /**
     * 标记流中的当前位置。后续调用 reset() 将尝试将流重置到此位置，并适当重置行号。
     *
     * @param  readAheadLimit
     *         读取字符数的限制，以保持标记。读取此数量的字符后，尝试重置流可能会失败。
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public void mark(int readAheadLimit) throws IOException {
        synchronized (lock) {
            // 如果最近读取的字符是 '\r'，则增加读取提前限制，因为在这种情况下，如果下一个字符是 '\n'，
            // 下一个 read() 实际上会读取两个字符。
            if (skipLF)
                readAheadLimit++;
            super.mark(readAheadLimit);
            markedLineNumber = lineNumber;
            markedSkipLF     = skipLF;
        }
    }

    /**
     * 将流重置到最近的标记。
     *
     * @throws  IOException
     *          如果流未被标记，或标记已失效
     */
    public void reset() throws IOException {
        synchronized (lock) {
            super.reset();
            lineNumber = markedLineNumber;
            skipLF     = markedSkipLF;
        }
    }

}
