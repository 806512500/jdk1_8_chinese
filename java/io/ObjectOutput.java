/*
 * Copyright (c) 1996, 2010, Oracle and/or its affiliates. All rights reserved.
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
 * ObjectOutput 接口扩展了 DataOutput 接口，包括对象的写入。
 * DataOutput 包含了基本类型的输出方法，ObjectOutput 扩展了该接口以包括对象、数组和字符串的写入。
 *
 * @author  未署名
 * @see java.io.InputStream
 * @see java.io.ObjectOutputStream
 * @see java.io.ObjectInputStream
 * @since   JDK1.1
 */
public interface ObjectOutput extends DataOutput, AutoCloseable {
    /**
     * 将对象写入底层存储或流。实现此接口的类定义了对象的写入方式。
     *
     * @param obj 要写入的对象
     * @exception IOException 任何通常的输入/输出相关异常。
     */
    public void writeObject(Object obj)
      throws IOException;

    /**
     * 写入一个字节。此方法将阻塞，直到字节实际写入。
     * @param b 要写入的字节
     * @exception IOException 如果发生 I/O 错误。
     */
    public void write(int b) throws IOException;

    /**
     * 写入一个字节数组。此方法将阻塞，直到字节实际写入。
     * @param b 要写入的数据
     * @exception IOException 如果发生 I/O 错误。
     */
    public void write(byte b[]) throws IOException;

    /**
     * 写入一个子字节数组。
     * @param b 要写入的数据
     * @param off 数据中的起始偏移量
     * @param len 要写入的字节数
     * @exception IOException 如果发生 I/O 错误。
     */
    public void write(byte b[], int off, int len) throws IOException;

    /**
     * 刷新流。这将写入任何缓冲的输出字节。
     * @exception IOException 如果发生 I/O 错误。
     */
    public void flush() throws IOException;

    /**
     * 关闭流。此方法必须被调用以释放与流相关的任何资源。
     * @exception IOException 如果发生 I/O 错误。
     */
    public void close() throws IOException;
}
