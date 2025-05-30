/*
 * Copyright (c) 1994, 2010, Oracle and/or its affiliates. All rights reserved.
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
 * 一个 <code>FilterInputStream</code> 包含
 * 一些其他输入流，它用作
 * 数据的基本来源，可能在途中转换
 * 数据或提供 附加功能。类 <code>FilterInputStream</code>
 * 本身只是覆盖了所有
 * <code>InputStream</code> 的方法，版本
 * 将所有请求传递给包含的 输入
 * 流。<code>FilterInputStream</code> 的子类
 * 可能进一步覆盖这些方法中的一些
 * 并可能还提供其他方法
 * 和字段。
 *
 * @author  Jonathan Payne
 * @since   JDK1.0
 */
public
class FilterInputStream extends InputStream {
    /**
     * 要过滤的输入流。
     */
    protected volatile InputStream in;

    /**
     * 通过将参数 <code>in</code>
     * 分配给字段 <code>this.in</code> 来创建 <code>FilterInputStream</code>
     * 以便稍后使用。
     *
     * @param   in   底层输入流，或 <code>null</code> 如果
     *          此实例要在没有底层流的情况下创建。
     */
    protected FilterInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * 从该输入流中读取下一个字节的数据。该字节的值
     * 作为 <code>int</code> 返回，范围在
     * <code>0</code> 到 <code>255</code> 之间。如果因为
     * 流的末尾已到达而没有可用的字节，返回值
     * <code>-1</code>。此方法会阻塞，直到输入数据
     * 可用，检测到流的末尾，或抛出异常。
     * <p>
     * 此方法
     * 仅执行 <code>in.read()</code> 并返回结果。
     *
     * @return     下一个字节的数据，或如果流的末尾已到达则返回 <code>-1</code>。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public int read() throws IOException {
        return in.read();
    }

    /**
     * 从该输入流中读取最多 <code>byte.length</code> 字节的数据到字节数组中。此方法会阻塞，直到有
     * 输入可用。
     * <p>
     * 此方法仅执行调用
     * <code>read(b, 0, b.length)</code> 并返回
     * 结果。它不会执行 <code>in.read(b)</code>；
     * 某些 <code>FilterInputStream</code> 的子类
     * 依赖于实际使用的实现策略。
     *
     * @param      b   存储数据的缓冲区。
     * @return     读入缓冲区的总字节数，或
     *             如果因为流的末尾已到达而没有更多数据则返回 <code>-1</code>。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterInputStream#read(byte[], int, int)
     */
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * 从该输入流中读取最多 <code>len</code> 字节的数据到字节数组中。如果 <code>len</code> 不为零，该方法
     * 会阻塞，直到有输入可用；否则，
     * 不读取任何字节并返回 <code>0</code>。
     * <p>
     * 此方法仅执行 <code>in.read(b, off, len)</code>
     * 并返回结果。
     *
     * @param      b     存储数据的缓冲区。
     * @param      off   目标数组 <code>b</code> 中的起始偏移量
     * @param      len   最大读取字节数。
     * @return     读入缓冲区的总字节数，或
     *             如果因为流的末尾已到达而没有更多数据则返回 <code>-1</code>。
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负，
     * <code>len</code> 为负，或 <code>len</code> 大于
     * <code>b.length - off</code>
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public int read(byte b[], int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    /**
     * 从输入流中跳过并丢弃 <code>n</code> 字节的数据。跳过方法可能由于各种原因
     * 结束时跳过的字节数少于请求的字节数，
     * 可能为 <code>0</code>。实际跳过的字节数
     * 作为返回值。
     * <p>
     * 此方法仅执行 <code>in.skip(n)</code>。
     *
     * @param      n   要跳过的字节数。
     * @return     实际跳过的字节数。
     * @exception  IOException  如果流不支持 seek，
     *                          或发生其他 I/O 错误。
     */
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    /**
     * 返回从该输入流中可以读取（或跳过）的字节数的估计值，而不会阻塞
     * 下一个调用该输入流方法的调用者。下一个调用者可能是
     * 同一个线程或另一个线程。单次读取或跳过这么多字节不会阻塞，但可能会读取或跳过更少的字节。
     * <p>
     * 此方法返回 {@link #in in}.available() 的结果。
     *
     * @return     从该输入流中可以读取（或跳过）的字节数的估计值，而不会阻塞。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public int available() throws IOException {
        return in.available();
    }

    /**
     * 关闭此输入流并释放与此流关联的系统资源。
     * 此方法
     * 仅执行 <code>in.close()</code>。
     *
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public void close() throws IOException {
        in.close();
    }

    /**
     * 标记此输入流中的当前位置。后续调用
     * <code>reset</code> 方法将此流重新定位到
     * 上次调用 <code>mark</code> 方法时的位置，以便后续读取重新读取相同的字节。
     * <p>
     * <code>readlimit</code> 参数告诉此输入流允许在标记位置失效之前
     * 读取这么多字节。
     * <p>
     * 此方法仅执行 <code>in.mark(readlimit)</code>。
     *
     * @param   readlimit   在标记位置失效之前允许读取的最大字节数。
     * @see     java.io.FilterInputStream#in
     * @see     java.io.FilterInputStream#reset()
     */
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    /**
     * 将此流重新定位到上次
     * 调用 <code>mark</code> 方法时的位置。
     * <p>
     * 此方法
     * 仅执行 <code>in.reset()</code>。
     * <p>
     * 流标记旨在用于
     * 需要向前读取一点以查看流中的内容的情况。通常这最容易通过调用
     * 通用解析器来完成。如果流是解析器处理的类型，它会愉快地继续读取。如果流不是
     * 该类型，解析器应在失败时抛出异常。
     * 如果在 readlimit 字节内发生这种情况，它允许外部
     * 代码重置流并尝试另一个解析器。
     *
     * @exception  IOException  如果流未被标记或标记已失效。
     * @see        java.io.FilterInputStream#in
     * @see        java.io.FilterInputStream#mark(int)
     */
    public synchronized void reset() throws IOException {
        in.reset();
    }

    /**
     * 测试此输入流是否支持 <code>mark</code>
     * 和 <code>reset</code> 方法。
     * 此方法
     * 仅执行 <code>in.markSupported()</code>。
     *
     * @return  如果此流类型支持
     *          <code>mark</code> 和 <code>reset</code> 方法，则返回 <code>true</code>；
     *          否则返回 <code>false</code>。
     * @see     java.io.FilterInputStream#in
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return in.markSupported();
    }
}
