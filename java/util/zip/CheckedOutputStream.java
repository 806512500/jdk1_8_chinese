/*
 * Copyright (c) 1996, 1999, Oracle and/or its affiliates. All rights reserved.
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

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * 一个输出流，同时维护写入数据的校验和。该校验和可用于验证输出数据的完整性。
 *
 * @see         Checksum
 * @author      David Connelly
 */
public
class CheckedOutputStream extends FilterOutputStream {
    private Checksum cksum;

    /**
     * 创建一个具有指定校验和的输出流。
     * @param out 输出流
     * @param cksum 校验和
     */
    public CheckedOutputStream(OutputStream out, Checksum cksum) {
        super(out);
        this.cksum = cksum;
    }

    /**
     * 写入一个字节。将阻塞直到字节实际写入。
     * @param b 要写入的字节
     * @exception IOException 如果发生 I/O 错误
     */
    public void write(int b) throws IOException {
        out.write(b);
        cksum.update(b);
    }

    /**
     * 写入一个字节数组。将阻塞直到字节实际写入。
     * @param b 要写入的数据
     * @param off 数据的起始偏移量
     * @param len 要写入的字节数
     * @exception IOException 如果发生 I/O 错误
     */
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        cksum.update(b, off, len);
    }

    /**
     * 返回此输出流的校验和。
     * @return 校验和
     */
    public Checksum getChecksum() {
        return cksum;
    }
}
