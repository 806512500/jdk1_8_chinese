/*
 * Copyright (c) 1994, 2003, Oracle and/or its affiliates. All rights reserved.
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
 * 该类实现了一个带缓冲的输出流。通过设置这样的输出流，应用程序可以将字节写入底层输出流，而不必为每个写入的字节都调用底层系统。
 *
 * @author  Arthur van Hoff
 * @since   JDK1.0
 */
public
class BufferedOutputStream extends FilterOutputStream {
    /**
     * 用于存储数据的内部缓冲区。
     */
    protected byte buf[];

    /**
     * 缓冲区中有效字节的数量。此值始终在 <tt>0</tt> 到 <tt>buf.length</tt> 之间；元素 <tt>buf[0]</tt> 到 <tt>buf[count-1]</tt> 包含有效的字节数据。
     */
    protected int count;

    /**
     * 创建一个新的带缓冲的输出流，用于将数据写入指定的底层输出流。
     *
     * @param   out   底层输出流。
     */
    public BufferedOutputStream(OutputStream out) {
        this(out, 8192);
    }

    /**
     * 创建一个新的带缓冲的输出流，用于将数据写入指定的底层输出流，并指定缓冲区大小。
     *
     * @param   out    底层输出流。
     * @param   size   缓冲区大小。
     * @exception IllegalArgumentException 如果 size &lt;= 0。
     */
    public BufferedOutputStream(OutputStream out, int size) {
        super(out);
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        buf = new byte[size];
    }

    /** 刷新内部缓冲区 */
    private void flushBuffer() throws IOException {
        if (count > 0) {
            out.write(buf, 0, count);
            count = 0;
        }
    }

    /**
     * 将指定的字节写入此带缓冲的输出流。
     *
     * @param      b   要写入的字节。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public synchronized void write(int b) throws IOException {
        if (count >= buf.length) {
            flushBuffer();
        }
        buf[count++] = (byte)b;
    }

    /**
     * 从指定的字节数组中从偏移量 <code>off</code> 开始写入 <code>len</code> 个字节到此带缓冲的输出流。
     *
     * <p> 通常，此方法将字节数组中的字节存储到此流的缓冲区中，必要时将缓冲区刷新到底层输出流。如果请求的长度至少与此流的缓冲区大小相同，则此方法将刷新缓冲区并将字节直接写入底层输出流。因此，冗余的 <code>BufferedOutputStream</code> 不会不必要的复制数据。
     *
     * @param      b     数据。
     * @param      off   数据中的起始偏移量。
     * @param      len   要写入的字节数。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public synchronized void write(byte b[], int off, int len) throws IOException {
        if (len >= buf.length) {
            /* 如果请求的长度超过输出缓冲区的大小，
               则刷新输出缓冲区，然后直接写入数据。
               这样，带缓冲的流将无害地级联。 */
            flushBuffer();
            out.write(b, off, len);
            return;
        }
        if (len > buf.length - count) {
            flushBuffer();
        }
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    /**
     * 刷新此带缓冲的输出流。这会强制将任何缓冲的输出字节写入底层输出流。
     *
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     */
    public synchronized void flush() throws IOException {
        flushBuffer();
        out.flush();
    }
}
