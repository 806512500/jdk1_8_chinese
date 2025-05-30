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
 * <code>PushbackInputStream</code> 类为另一个输入流添加了功能，即能够“回退”或“未读”一个字节。这在需要读取以特定字节值分隔的不定数量的数据字节的情况下非常有用；读取终止字节后，代码片段可以“未读”该字节，以便下次读取输入流时重新读取该字节。例如，表示标识符的字节可能以表示操作符字符的字节终止；读取标识符的方法可以在看到操作符后将其回退，以便重新读取。
 *
 * @author  David Connelly
 * @author  Jonathan Payne
 * @since   JDK1.0
 */
public
class PushbackInputStream extends FilterInputStream {
    /**
     * 回退缓冲区。
     * @since   JDK1.1
     */
    protected byte[] buf;

    /**
     * 下一个字节将从回退缓冲区的此位置读取。当缓冲区为空时，<code>pos</code> 等于 <code>buf.length</code>；当缓冲区满时，<code>pos</code> 等于零。
     *
     * @since   JDK1.1
     */
    protected int pos;

    /**
     * 检查此流是否已关闭
     */
    private void ensureOpen() throws IOException {
        if (in == null)
            throw new IOException("Stream closed");
    }

    /**
     * 创建一个具有指定 <code>size</code> 大小的回退缓冲区的 <code>PushbackInputStream</code>，并保存其参数，即输入流 <code>in</code> 以供后续使用。最初，没有回退的字节（字段 <code>pushBack</code> 初始化为 <code>-1</code>）。
     *
     * @param  in    将从中读取字节的输入流。
     * @param  size  回退缓冲区的大小。
     * @exception IllegalArgumentException 如果 {@code size <= 0}
     * @since  JDK1.1
     */
    public PushbackInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        this.buf = new byte[size];
        this.pos = size;
    }

    /**
     * 创建一个 <code>PushbackInputStream</code> 并保存其参数，即输入流 <code>in</code> 以供后续使用。最初，没有回退的字节（字段 <code>pushBack</code> 初始化为 <code>-1</code>）。
     *
     * @param   in   将从中读取字节的输入流。
     */
    public PushbackInputStream(InputStream in) {
        this(in, 1);
    }

    /**
     * 从该输入流中读取下一个字节的数据。该字节的值作为 <code>int</code> 返回，范围在 <code>0</code> 到 <code>255</code> 之间。如果因为到达流的末尾而没有可用的字节，则返回 <code>-1</code>。此方法在有输入数据可用、检测到流的末尾或抛出异常时阻塞。
     *
     * <p> 如果有最近回退的字节，此方法将返回该字节，否则调用其底层输入流的 <code>read</code> 方法并返回该方法的值。
     *
     * @return     下一个字节的数据，或者如果到达流的末尾则返回 <code>-1</code>。
     * @exception  IOException  如果通过调用其 {@link #close()} 方法关闭了此输入流，或发生 I/O 错误。
     * @see        java.io.InputStream#read()
     */
    public int read() throws IOException {
        ensureOpen();
        if (pos < buf.length) {
            return buf[pos++] & 0xff;
        }
        return super.read();
    }

    /**
     * 从该输入流中读取最多 <code>len</code> 个字节的数据到字节数组中。此方法首先读取任何回退的字节；之后，如果读取的字节少于 <code>len</code>，则从底层输入流中读取。如果 <code>len</code> 不为零，该方法将阻塞直到至少有 1 个字节的输入可用；否则，不读取任何字节并返回 <code>0</code>。
     *
     * @param      b     数据读入的缓冲区。
     * @param      off   目标数组 <code>b</code> 中的起始偏移量。
     * @param      len   最大读取的字节数。
     * @return     读入缓冲区的总字节数，或者如果因为到达流的末尾而没有更多数据则返回 <code>-1</code>。
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，<code>len</code> 为负数，或 <code>len</code> 大于 <code>b.length - off</code>。
     * @exception  IOException  如果通过调用其 {@link #close()} 方法关闭了此输入流，或发生 I/O 错误。
     * @see        java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int avail = buf.length - pos;
        if (avail > 0) {
            if (len < avail) {
                avail = len;
            }
            System.arraycopy(buf, pos, b, off, avail);
            pos += avail;
            off += avail;
            len -= avail;
        }
        if (len > 0) {
            len = super.read(b, off, len);
            if (len == -1) {
                return avail == 0 ? -1 : avail;
            }
            return avail + len;
        }
        return avail;
    }

    /**
     * 通过将其复制到回退缓冲区的前端来回退一个字节。此方法返回后，下一个读取的字节将具有 <code>(byte)b</code> 的值。
     *
     * @param      b   其低阶字节将被回退的 <code>int</code> 值。
     * @exception IOException 如果回退缓冲区中没有足够的空间，或通过调用其 {@link #close()} 方法关闭了此输入流。
     */
    public void unread(int b) throws IOException {
        ensureOpen();
        if (pos == 0) {
            throw new IOException("Push back buffer is full");
        }
        buf[--pos] = (byte)b;
    }

    /**
     * 通过将其复制到回退缓冲区的前端来回退字节数组的一部分。此方法返回后，下一个读取的字节将具有 <code>b[off]</code> 的值，之后的字节将具有 <code>b[off+1]</code> 的值，依此类推。
     *
     * @param b 要回退的字节数组。
     * @param off 数据的起始偏移量。
     * @param len 要回退的字节数。
     * @exception IOException 如果回退缓冲区中没有足够的空间，或通过调用其 {@link #close()} 方法关闭了此输入流。
     * @since     JDK1.1
     */
    public void unread(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (len > pos) {
            throw new IOException("Push back buffer is full");
        }
        pos -= len;
        System.arraycopy(b, off, buf, pos, len);
    }

    /**
     * 通过将其复制到回退缓冲区的前端来回退字节数组。此方法返回后，下一个读取的字节将具有 <code>b[0]</code> 的值，之后的字节将具有 <code>b[1]</code> 的值，依此类推。
     *
     * @param b 要回退的字节数组。
     * @exception IOException 如果回退缓冲区中没有足够的空间，或通过调用其 {@link #close()} 方法关闭了此输入流。
     * @since     JDK1.1
     */
    public void unread(byte[] b) throws IOException {
        unread(b, 0, b.length);
    }

    /**
     * 返回可以从该输入流中读取（或跳过）而不阻塞的字节数的估计值。下一次调用可能是同一个线程或另一个线程。单次读取或跳过这些字节不会阻塞，但可能读取或跳过更少的字节。
     *
     * <p> 该方法返回已回退的字节数加上 {@link
     * java.io.FilterInputStream#available available} 返回的值。
     *
     * @return     可以从输入流中读取（或跳过）而不阻塞的字节数。
     * @exception  IOException  如果通过调用其 {@link #close()} 方法关闭了此输入流，或发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     * @see        java.io.InputStream#available()
     */
    public int available() throws IOException {
        ensureOpen();
        int n = buf.length - pos;
        int avail = super.available();
        return n > (Integer.MAX_VALUE - avail)
                    ? Integer.MAX_VALUE
                    : n + avail;
    }

    /**
     * 从该输入流中跳过并丢弃 <code>n</code> 个字节的数据。由于各种原因，<code>skip</code> 方法可能最终跳过更少的字节，甚至可能为零。如果 <code>n</code> 为负数，则不跳过任何字节。
     *
     * <p> <code>PushbackInputStream</code> 类的 <code>skip</code> 方法首先跳过回退缓冲区中的字节（如果有）。如果需要跳过更多字节，则调用底层输入流的 <code>skip</code> 方法。实际跳过的字节数返回。
     *
     * @param      n  {@inheritDoc}
     * @return     {@inheritDoc}
     * @exception  IOException  如果流不支持 seek，或通过调用其 {@link #close()} 方法关闭了此输入流，或发生 I/O 错误。
     * @see        java.io.FilterInputStream#in
     * @see        java.io.InputStream#skip(long n)
     * @since      1.2
     */
    public long skip(long n) throws IOException {
        ensureOpen();
        if (n <= 0) {
            return 0;
        }

        long pskip = buf.length - pos;
        if (pskip > 0) {
            if (n < pskip) {
                pskip = n;
            }
            pos += pskip;
            n -= pskip;
        }
        if (n > 0) {
            pskip += super.skip(n);
        }
        return pskip;
    }

    /**
     * 测试此输入流是否支持 <code>mark</code> 和 <code>reset</code> 方法，它不支持。
     *
     * @return   <code>false</code>，因为此类不支持 <code>mark</code> 和 <code>reset</code> 方法。
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * 标记此输入流中的当前位置。
     *
     * <p> <code>PushbackInputStream</code> 类的 <code>mark</code> 方法不执行任何操作。
     *
     * @param   readlimit   在标记位置失效之前可以读取的最大字节数。
     * @see     java.io.InputStream#reset()
     */
    public synchronized void mark(int readlimit) {
    }

    /**
     * 将此流重新定位到上次调用 <code>mark</code> 方法时的位置。
     *
     * <p> <code>PushbackInputStream</code> 类的 <code>reset</code> 方法除了抛出 <code>IOException</code> 外不执行任何操作。
     *
     * @exception  IOException  如果调用此方法。
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.IOException
     */
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    /**
     * 关闭此输入流并释放与该流关联的任何系统资源。
     * 一旦流被关闭，进一步的 read()、unread()、available()、reset() 或 skip() 调用将抛出 IOException。关闭已关闭的流没有效果。
     *
     * @exception  IOException  如果发生 I/O 错误。
     */
    public synchronized void close() throws IOException {
        if (in == null)
            return;
        in.close();
        in = null;
        buf = null;
    }
}
