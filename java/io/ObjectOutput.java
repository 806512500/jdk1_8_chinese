/*
 * Copyright (c) 1996, 2010, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

/**
 * ObjectOutput 扩展了 DataOutput 接口，包括对象的写入。
 * DataOutput 包括了基本类型的输出方法，ObjectOutput
 * 扩展了该接口以包括对象、数组和字符串的写入。
 *
 * @author  未署名
 * @see java.io.InputStream
 * @see java.io.ObjectOutputStream
 * @see java.io.ObjectInputStream
 * @since   JDK1.1
 */
public interface ObjectOutput extends DataOutput, AutoCloseable {
    /**
     * 将一个对象写入底层存储或流。实现此接口的类定义了对象如何被写入。
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
     * 关闭流。必须调用此方法以释放与流关联的任何资源。
     * @exception IOException 如果发生 I/O 错误。
     */
    public void close() throws IOException;
}
