/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.util.zip;

import java.nio.ByteBuffer;
import sun.nio.ch.DirectBuffer;

/**
 * 一个可以用于计算数据流的Adler-32校验和的类。Adler-32校验和几乎与CRC-32一样可靠，
 * 但计算速度要快得多。
 *
 * <p> 如果将{@code null}参数传递给此类中的方法，将抛出
 * {@link NullPointerException}。
 *
 * @see         Checksum
 * @author      David Connelly
 */
public
class Adler32 implements Checksum {

    private int adler = 1;

    /**
     * 创建一个新的Adler32对象。
     */
    public Adler32() {
    }

    /**
     * 使用指定的字节（参数b的低八位）更新校验和。
     *
     * @param b 用于更新校验和的字节
     */
    public void update(int b) {
        adler = update(adler, b);
    }

    /**
     * 使用指定的字节数组更新校验和。
     *
     * @throws  ArrayIndexOutOfBoundsException
     *          如果{@code off}为负，或{@code len}为负，或{@code off+len}大于
     *          数组{@code b}的长度
     */
    public void update(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        adler = updateBytes(adler, b, off, len);
    }

    /**
     * 使用指定的字节数组更新校验和。
     *
     * @param b 用于更新校验和的字节数组
     */
    public void update(byte[] b) {
        adler = updateBytes(adler, b, 0, b.length);
    }


    /**
     * 使用指定缓冲区中的字节更新校验和。
     *
     * 校验和使用
     * buffer.{@link java.nio.Buffer#remaining() remaining()}
     * 从
     * buffer.{@link java.nio.Buffer#position() position()}
     * 开始的字节进行更新
     * 返回时，缓冲区的位置将更新为其限制；其限制不会改变。
     *
     * @param buffer 用于更新校验和的ByteBuffer
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
            adler = updateByteBuffer(adler, ((DirectBuffer)buffer).address(), pos, rem);
        } else if (buffer.hasArray()) {
            adler = updateBytes(adler, buffer.array(), pos + buffer.arrayOffset(), rem);
        } else {
            byte[] b = new byte[rem];
            buffer.get(b);
            adler = updateBytes(adler, b, 0, b.length);
        }
        buffer.position(limit);
    }

    /**
     * 将校验和重置为初始值。
     */
    public void reset() {
        adler = 1;
    }

    /**
     * 返回校验和值。
     */
    public long getValue() {
        return (long)adler & 0xffffffffL;
    }

    private native static int update(int adler, int b);
    private native static int updateBytes(int adler, byte[] b, int off,
                                          int len);
    private native static int updateByteBuffer(int adler, long addr,
                                               int off, int len);
}
