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

package java.util.zip;

import java.nio.ByteBuffer;
import sun.nio.ch.DirectBuffer;

/**
 * 一个可以用于计算数据流的CRC-32的类。
 *
 * <p> 如果将 {@code null} 参数传递给此类中的方法，将导致抛出 {@link NullPointerException}。
 *
 * @see         Checksum
 * @author      David Connelly
 */
public
class CRC32 implements Checksum {
    private int crc;

    /**
     * 创建一个新的 CRC32 对象。
     */
    public CRC32() {
    }


    /**
     * 使用指定的字节（参数 b 的低八位）更新 CRC-32 校验和。
     *
     * @param b 用于更新校验和的字节
     */
    public void update(int b) {
        crc = update(crc, b);
    }

    /**
     * 使用指定的字节数组更新 CRC-32 校验和。
     *
     * @throws  ArrayIndexOutOfBoundsException
     *          如果 {@code off} 为负，或 {@code len} 为负，或 {@code off+len} 大于数组 {@code b} 的长度
     */
    public void update(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        crc = updateBytes(crc, b, off, len);
    }

    /**
     * 使用指定的字节数组更新 CRC-32 校验和。
     *
     * @param b 用于更新校验和的字节数组
     */
    public void update(byte[] b) {
        crc = updateBytes(crc, b, 0, b.length);
    }

    /**
     * 使用指定的缓冲区中的字节更新校验和。
     *
     * 校验和使用 buffer.{@link java.nio.Buffer#remaining() remaining()}
     * 从 buffer.{@link java.nio.Buffer#position() position()} 开始的字节进行更新。
     * 返回时，缓冲区的位置将更新为其限制；其限制不会被更改。
     *
     * @param buffer 用于更新校验和的 ByteBuffer
     * @since 1.8
     */
    public void update(ByteBuffer buffer) {
        int pos = buffer.position();
        int limit = buffer.limit();
        assert (pos <= limit);
        int rem = limit - pos;
        if (rem <= 0)
            return;
        if (buffer instanceof DirectBuffer) {
            crc = updateByteBuffer(crc, ((DirectBuffer)buffer).address(), pos, rem);
        } else if (buffer.hasArray()) {
            crc = updateBytes(crc, buffer.array(), pos + buffer.arrayOffset(), rem);
        } else {
            byte[] b = new byte[rem];
            buffer.get(b);
            crc = updateBytes(crc, b, 0, b.length);
        }
        buffer.position(limit);
    }

    /**
     * 重置 CRC-32 到初始值。
     */
    public void reset() {
        crc = 0;
    }

    /**
     * 返回 CRC-32 值。
     */
    public long getValue() {
        return (long)crc & 0xffffffffL;
    }

    private native static int update(int crc, int b);
    private native static int updateBytes(int crc, byte[] b, int off, int len);

    private native static int updateByteBuffer(int adler, long addr,
                                               int off, int len);
}
