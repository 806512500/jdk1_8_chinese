/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * 用于写入字符流的抽象类。子类必须实现 write(char[], int, int)、flush() 和 close() 方法。
 * 大多数子类将覆盖此处定义的一些方法，以提供更高的效率、额外的功能或两者兼备。
 *
 * @see Writer
 * @see   BufferedWriter
 * @see   CharArrayWriter
 * @see   FilterWriter
 * @see   OutputStreamWriter
 * @see     FileWriter
 * @see   PipedWriter
 * @see   PrintWriter
 * @see   StringWriter
 * @see Reader
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public abstract class Writer implements Appendable, Closeable, Flushable {

    /**
     * 用于保存写入字符串和单个字符的临时缓冲区
     */
    private char[] writeBuffer;

    /**
     * writeBuffer 的大小，必须大于等于 1
     */
    private static final int WRITE_BUFFER_SIZE = 1024;

    /**
     * 用于同步此流操作的对象。为了提高效率，字符流对象可以使用其他对象而不是自身来保护关键部分。
     * 因此，子类应使用此字段中的对象而不是 <tt>this</tt> 或同步方法。
     */
    protected Object lock;

    /**
     * 创建一个新的字符流写入器，其关键部分将同步到写入器本身。
     */
    protected Writer() {
        this.lock = this;
    }

    /**
     * 创建一个新的字符流写入器，其关键部分将同步到给定的对象。
     *
     * @param  lock
     *         要同步的对象
     */
    protected Writer(Object lock) {
        if (lock == null) {
            throw new NullPointerException();
        }
        this.lock = lock;
    }

    /**
     * 写入单个字符。要写入的字符包含在给定整数值的 16 个低位中；16 个高位被忽略。
     *
     * <p> 打算支持高效单字符输出的子类应覆盖此方法。
     *
     * @param  c
     *         指定要写入的字符的 int
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public void write(int c) throws IOException {
        synchronized (lock) {
            if (writeBuffer == null){
                writeBuffer = new char[WRITE_BUFFER_SIZE];
            }
            writeBuffer[0] = (char) c;
            write(writeBuffer, 0, 1);
        }
    }

    /**
     * 写入字符数组。
     *
     * @param  cbuf
     *         要写入的字符数组
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public void write(char cbuf[]) throws IOException {
        write(cbuf, 0, cbuf.length);
    }

    /**
     * 写入字符数组的一部分。
     *
     * @param  cbuf
     *         字符数组
     *
     * @param  off
     *         从该偏移量开始写入字符
     *
     * @param  len
     *         要写入的字符数
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    abstract public void write(char cbuf[], int off, int len) throws IOException;

    /**
     * 写入字符串。
     *
     * @param  str
     *         要写入的字符串
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public void write(String str) throws IOException {
        write(str, 0, str.length());
    }

    /**
     * 写入字符串的一部分。
     *
     * @param  str
     *         字符串
     *
     * @param  off
     *         从该偏移量开始写入字符
     *
     * @param  len
     *         要写入的字符数
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>off</tt> 为负数，或 <tt>len</tt> 为负数，或 <tt>off+len</tt> 为负数或大于给定字符串的长度
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    public void write(String str, int off, int len) throws IOException {
        synchronized (lock) {
            char cbuf[];
            if (len <= WRITE_BUFFER_SIZE) {
                if (writeBuffer == null) {
                    writeBuffer = new char[WRITE_BUFFER_SIZE];
                }
                cbuf = writeBuffer;
            } else {    // 不永久分配非常大的缓冲区。
                cbuf = new char[len];
            }
            str.getChars(off, (off + len), cbuf, 0);
            write(cbuf, 0, len);
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
     * <p> 完全相同。根据字符序列 <tt>csq</tt> 的 <tt>toString</tt> 方法的规范，整个序列可能不会被追加。
     * 例如，调用字符缓冲区的 <tt>toString</tt> 方法将返回一个子序列，其内容取决于缓冲区的位置和限制。
     *
     * @param  csq
     *         要追加的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将四个字符 <tt>"null"</tt> 追加到此写入器。
     *
     * @return  此写入器
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @since  1.5
     */
    public Writer append(CharSequence csq) throws IOException {
        if (csq == null)
            write("null");
        else
            write(csq.toString());
        return this;
    }

    /**
     * 将指定字符序列的子序列追加到此写入器。
     * <tt>Appendable</tt>。
     *
     * <p> 以 <tt>out.append(csq, start, end)</tt> 形式调用此方法且 <tt>csq</tt> 不为 <tt>null</tt> 时，
     * 行为与调用
     *
     * <pre>
     *     out.write(csq.subSequence(start, end).toString()) </pre>
     *
     * <p> 完全相同。
     *
     * @param  csq
     *         要从中追加子序列的字符序列。如果 <tt>csq</tt> 为 <tt>null</tt>，则将字符追加为
     *         如果 <tt>csq</tt> 包含四个字符 <tt>"null"</tt>。
     *
     * @param  start
     *         子序列中第一个字符的索引
     *
     * @param  end
     *         子序列中最后一个字符之后的字符的索引
     *
     * @return  此写入器
     *
     * @throws  IndexOutOfBoundsException
     *          如果 <tt>start</tt> 或 <tt>end</tt> 为负数，<tt>start</tt> 大于 <tt>end</tt>，
     *          或 <tt>end</tt> 大于 <tt>csq.length()</tt>
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @since  1.5
     */
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }

    /**
     * 将指定字符追加到此写入器。
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
     * @throws  IOException
     *          如果发生 I/O 错误
     *
     * @since 1.5
     */
    public Writer append(char c) throws IOException {
        write(c);
        return this;
    }

    /**
     * 刷新流。如果流在各种 write() 方法中保存了任何字符在缓冲区中，立即将它们写入其预期目的地。
     * 然后，如果该目的地是另一个字符或字节流，刷新它。因此，一次 flush() 调用将刷新所有 Writers 和 OutputStreams 的缓冲区。
     *
     * <p> 如果此流的预期目的地是由底层操作系统提供的抽象，例如文件，则刷新流仅保证将之前写入流的字节传递给操作系统进行写入；
     * 它不保证这些字节实际上写入物理设备，如磁盘驱动器。
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    abstract public void flush() throws IOException;

    /**
     * 关闭流，先刷新它。流关闭后，进一步的 write() 或 flush() 调用将导致抛出 IOException。
     * 关闭已关闭的流没有效果。
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     */
    abstract public void close() throws IOException;

}
