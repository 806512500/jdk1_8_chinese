/*
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.util.zip;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * 一个输入流，同时维护正在读取的数据的校验和。
 * 该校验和可用于验证输入数据的完整性。
 *
 * @see         Checksum
 * @author      David Connelly
 */
public
class CheckedInputStream extends FilterInputStream {
    private Checksum cksum;

    /**
     * 使用指定的Checksum创建一个输入流。
     * @param in 输入流
     * @param cksum 校验和
     */
    public CheckedInputStream(InputStream in, Checksum cksum) {
        super(in);
        this.cksum = cksum;
    }

    /**
     * 读取一个字节。如果没有可用的输入，将阻塞。
     * @return 读取的字节，或如果到达流的末尾则返回-1。
     * @exception IOException 如果发生I/O错误
     */
    public int read() throws IOException {
        int b = in.read();
        if (b != -1) {
            cksum.update(b);
        }
        return b;
    }

    /**
     * 读取到字节数组中。如果<code>len</code>不为零，该方法将阻塞直到有输入可用；
     * 否则，不读取任何字节并返回<code>0</code>。
     * @param buf 读取数据的缓冲区
     * @param off 目标数组<code>b</code>中的起始偏移量
     * @param len 最大读取的字节数
     * @return 实际读取的字节数，或如果到达流的末尾则返回-1。
     * @exception  NullPointerException 如果<code>buf</code>为<code>null</code>。
     * @exception  IndexOutOfBoundsException 如果<code>off</code>为负数，
     * <code>len</code>为负数，或<code>len</code>大于<code>buf.length - off</code>
     * @exception IOException 如果发生I/O错误
     */
    public int read(byte[] buf, int off, int len) throws IOException {
        len = in.read(buf, off, len);
        if (len != -1) {
            cksum.update(buf, off, len);
        }
        return len;
    }

    /**
     * 跳过指定数量的输入字节。
     * @param n 要跳过的字节数
     * @return 实际跳过的字节数
     * @exception IOException 如果发生I/O错误
     */
    public long skip(long n) throws IOException {
        byte[] buf = new byte[512];
        long total = 0;
        while (total < n) {
            long len = n - total;
            len = read(buf, 0, len < buf.length ? (int)len : buf.length);
            if (len == -1) {
                return total;
            }
            total += len;
        }
        return total;
    }

    /**
     * 返回此输入流的Checksum。
     * @return 校验和值
     */
    public Checksum getChecksum() {
        return cksum;
    }
}
