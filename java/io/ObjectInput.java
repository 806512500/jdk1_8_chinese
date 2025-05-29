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
 * ObjectInput 扩展了 DataInput 接口，包括对象的读取。DataInput 包含了基本类型的输入方法，
 * ObjectInput 扩展了该接口以包括对象、数组和字符串的读取。
 *
 * @author  未署名
 * @see java.io.InputStream
 * @see java.io.ObjectOutputStream
 * @see java.io.ObjectInputStream
 * @since   JDK1.1
 */
public interface ObjectInput extends DataInput, AutoCloseable {
    /**
     * 读取并返回一个对象。实现此接口的类定义了对象从何处“读取”。
     *
     * @return 从流中读取的对象
     * @exception java.lang.ClassNotFoundException 如果无法找到序列化对象的类。
     * @exception IOException 如果发生任何通常的输入/输出相关异常。
     */
    public Object readObject()
        throws ClassNotFoundException, IOException;

    /**
     * 读取一个字节的数据。如果没有输入可用，此方法将阻塞。
     * @return 读取的字节，如果到达流的末尾则返回 -1。
     * @exception IOException 如果发生 I/O 错误。
     */
    public int read() throws IOException;

    /**
     * 读取到一个字节数组中。此方法将阻塞直到有输入可用。
     * @param b 读取数据的缓冲区
     * @return 实际读取的字节数，如果到达流的末尾则返回 -1。
     * @exception IOException 如果发生 I/O 错误。
     */
    public int read(byte b[]) throws IOException;

    /**
     * 读取到一个字节数组中。此方法将阻塞直到有输入可用。
     * @param b 读取数据的缓冲区
     * @param off 数据的起始偏移量
     * @param len 最大读取的字节数
     * @return 实际读取的字节数，如果到达流的末尾则返回 -1。
     * @exception IOException 如果发生 I/O 错误。
     */
    public int read(byte b[], int off, int len) throws IOException;

    /**
     * 跳过 n 个输入字节。
     * @param n 要跳过的字节数
     * @return 实际跳过的字节数。
     * @exception IOException 如果发生 I/O 错误。
     */
    public long skip(long n) throws IOException;

    /**
     * 返回在不阻塞的情况下可以读取的字节数。
     * @return 可用的字节数。
     * @exception IOException 如果发生 I/O 错误。
     */
    public int available() throws IOException;

    /**
     * 关闭输入流。必须调用以释放与流关联的任何资源。
     * @exception IOException 如果发生 I/O 错误。
     */
    public void close() throws IOException;
}
