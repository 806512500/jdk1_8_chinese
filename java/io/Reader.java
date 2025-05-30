/*
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
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
 * 用于读取字符流的抽象类。子类必须实现 read(char[], int, int) 和 close() 方法。大多数子类将覆盖这里定义的一些方法，以提供更高的效率、额外的功能或两者兼有。
 *
 *
 * @see BufferedReader
 * @see   LineNumberReader
 * @see CharArrayReader
 * @see InputStreamReader
 * @see   FileReader
 * @see FilterReader
 * @see   PushbackReader
 * @see PipedReader
 * @see StringReader
 * @see Writer
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public abstract class Reader implements Readable, Closeable {

    /**
     * 用于同步此流操作的对象。为了提高效率，字符流对象可以使用除自身以外的对象来保护关键部分。因此，子类应使用此字段中的对象，而不是 <tt>this</tt> 或同步方法。
     */
    protected Object lock;

    /**
     * 创建一个新的字符流读取器，其关键部分将同步到读取器本身。
     */
    protected Reader() {
        this.lock = this;
    }

    /**
     * 创建一个新的字符流读取器，其关键部分将同步到给定的对象。
     *
     * @param lock  用于同步的对象。
     */
    protected Reader(Object lock) {
        if (lock == null) {
            throw new NullPointerException();
        }
        this.lock = lock;
    }

    /**
     * 尝试将字符读取到指定的字符缓冲区。缓冲区作为字符存储库使用：唯一的变化是 put 操作的结果。不会对缓冲区进行翻转或重置。
     *
     * @param target 要读取字符的缓冲区
     * @return 添加到缓冲区的字符数，或 -1 表示字符源已结束
     * @throws IOException 如果发生 I/O 错误
     * @throws NullPointerException 如果 target 为 null
     * @throws java.nio.ReadOnlyBufferException 如果 target 是只读缓冲区
     * @since 1.5
     */
    public int read(java.nio.CharBuffer target) throws IOException {
        int len = target.remaining();
        char[] cbuf = new char[len];
        int n = read(cbuf, 0, len);
        if (n > 0)
            target.put(cbuf, 0, n);
        return n;
    }

    /**
     * 读取单个字符。此方法将阻塞，直到有字符可用、发生 I/O 错误或流结束。
     *
     * <p> 打算支持高效单字符输入的子类应覆盖此方法。
     *
     * @return 读取的字符，作为 0 到 65535 (<tt>0x00-0xffff</tt>) 范围内的整数，或 -1 表示流已结束
     *
     * @exception  IOException 如果发生 I/O 错误
     */
    public int read() throws IOException {
        char cb[] = new char[1];
        if (read(cb, 0, 1) == -1)
            return -1;
        else
            return cb[0];
    }

    /**
     * 将字符读取到数组中。此方法将阻塞，直到有输入可用、发生 I/O 错误或流结束。
     *
     * @param       cbuf  目标缓冲区
     *
     * @return      读取的字符数，或 -1 表示流已结束
     *
     * @exception   IOException 如果发生 I/O 错误
     */
    public int read(char cbuf[]) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    /**
     * 将字符读取到数组的一部分中。此方法将阻塞，直到有输入可用、发生 I/O 错误或流结束。
     *
     * @param      cbuf  目标缓冲区
     * @param      off   开始存储字符的偏移量
     * @param      len   最大读取字符数
     *
     * @return     读取的字符数，或 -1 表示流已结束
     *
     * @exception  IOException 如果发生 I/O 错误
     */
    abstract public int read(char cbuf[], int off, int len) throws IOException;

    /** 最大跳过缓冲区大小 */
    private static final int maxSkipBufferSize = 8192;

    /** 跳过缓冲区，直到分配前为 null */
    private char skipBuffer[] = null;

    /**
     * 跳过字符。此方法将阻塞，直到有字符可用、发生 I/O 错误或流结束。
     *
     * @param  n  要跳过的字符数
     *
     * @return    实际跳过的字符数
     *
     * @exception  IllegalArgumentException  如果 <code>n</code> 为负数。
     * @exception  IOException 如果发生 I/O 错误
     */
    public long skip(long n) throws IOException {
        if (n < 0L)
            throw new IllegalArgumentException("skip value is negative");
        int nn = (int) Math.min(n, maxSkipBufferSize);
        synchronized (lock) {
            if ((skipBuffer == null) || (skipBuffer.length < nn))
                skipBuffer = new char[nn];
            long r = n;
            while (r > 0) {
                int nc = read(skipBuffer, 0, (int)Math.min(r, nn));
                if (nc == -1)
                    break;
                r -= nc;
            }
            return n - r;
        }
    }

    /**
     * 告诉此流是否准备好读取。
     *
     * @return 如果下一个 read() 肯定不会因输入而阻塞，则返回 true，否则返回 false。请注意，返回 false 并不保证下一个 read 会阻塞。
     *
     * @exception  IOException 如果发生 I/O 错误
     */
    public boolean ready() throws IOException {
        return false;
    }

    /**
     * 告诉此流是否支持 mark() 操作。默认实现总是返回 false。子类应覆盖此方法。
     *
     * @return 如果且仅如果此流支持 mark 操作，则返回 true。
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * 标记流中的当前位置。后续调用 reset() 将尝试将流重新定位到此点。并非所有字符输入流都支持 mark() 操作。
     *
     * @param  readAheadLimit  读取的字符数限制，同时仍保留标记。读取这么多字符后，尝试重置流可能会失败。
     *
     * @exception  IOException 如果流不支持 mark()，或发生其他 I/O 错误
     */
    public void mark(int readAheadLimit) throws IOException {
        throw new IOException("mark() not supported");
    }

    /**
     * 重置流。如果流已标记，则尝试将其重新定位到标记处。如果流未标记，则尝试以某种方式重置流，例如将其重新定位到起始点。并非所有字符输入流都支持 reset() 操作，有些支持 reset() 但不支持 mark()。
     *
     * @exception  IOException 如果流未标记，或标记已失效，或流不支持 reset()，或发生其他 I/O 错误
     */
    public void reset() throws IOException {
        throw new IOException("reset() not supported");
    }

    /**
     * 关闭流并释放与之关联的系统资源。流关闭后，进一步的 read()、ready()、mark()、reset() 或 skip() 调用将抛出 IOException。关闭已关闭的流没有效果。
     *
     * @exception  IOException 如果发生 I/O 错误
     */
     abstract public void close() throws IOException;

}
