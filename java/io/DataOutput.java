
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * <code>DataOutput</code> 接口提供了将 Java
 * 基本类型的数据转换为一系列字节并
 * 将这些字节写入二进制流的功能。
 * 还提供了一个将 <code>String</code> 转换为
 * <a href="DataInput.html#modified-utf-8">修改后的 UTF-8</a>
 * 格式并写入结果字节序列的设施。
 * <p>
 * 对于此接口中所有写入字节的方法，
 * 通常情况下，如果由于任何原因无法写入字节，
 * 则会抛出 <code>IOException</code>。
 *
 * @author  Frank Yellin
 * @see     java.io.DataInput
 * @see     java.io.DataOutputStream
 * @since   JDK1.0
 */
public
interface DataOutput {
    /**
     * 将参数 <code>b</code> 的八个低阶位写入输出流。
     * <code>b</code> 的 24 个高阶位被忽略。
     *
     * @param      b   要写入的字节。
     * @throws     IOException  如果发生 I/O 错误。
     */
    void write(int b) throws IOException;

    /**
     * 将数组 <code>b</code> 中的所有字节写入输出流。
     * 如果 <code>b</code> 为 <code>null</code>，
     * 则抛出 <code>NullPointerException</code>。
     * 如果 <code>b.length</code> 为零，则
     * 不写入任何字节。否则，首先写入字节
     * <code>b[0]</code>，然后是 <code>b[1]</code>，依此类推；
     * 最后写入的字节是 <code>b[b.length-1]</code>。
     *
     * @param      b   数据。
     * @throws     IOException  如果发生 I/O 错误。
     */
    void write(byte b[]) throws IOException;

    /**
     * 按顺序从数组 <code>b</code> 中写入 <code>len</code> 个字节
     * 到输出流。如果 <code>b</code> 为 <code>null</code>，
     * 则抛出 <code>NullPointerException</code>。
     * 如果 <code>off</code> 为负数，或 <code>len</code> 为负数，
     * 或 <code>off+len</code> 大于数组 <code>b</code> 的长度，
     * 则抛出 <code>IndexOutOfBoundsException</code>。
     * 如果 <code>len</code> 为零，则不写入任何字节。否则，
     * 首先写入字节 <code>b[off]</code>，然后是 <code>b[off+1]</code>，依此类推；
     * 最后写入的字节是 <code>b[off+len-1]</code>。
     *
     * @param      b     数据。
     * @param      off   数据中的起始偏移量。
     * @param      len   要写入的字节数。
     * @throws     IOException  如果发生 I/O 错误。
     */
    void write(byte b[], int off, int len) throws IOException;

    /**
     * 将一个 <code>boolean</code> 值写入此输出流。
     * 如果参数 <code>v</code> 为 <code>true</code>，则写入值 <code>(byte)1</code>；
     * 如果 <code>v</code> 为 <code>false</code>，则写入值 <code>(byte)0</code>。
     * 通过此方法写入的字节可以由 <code>DataInput</code> 接口的 <code>readBoolean</code>
     * 方法读取，该方法将返回一个等于 <code>v</code> 的 <code>boolean</code>。
     *
     * @param      v   要写入的布尔值。
     * @throws     IOException  如果发生 I/O 错误。
     */
    void writeBoolean(boolean v) throws IOException;

    /**
     * 将参数 <code>v</code> 的八个低阶位写入输出流。
     * <code>v</code> 的 24 个高阶位被忽略。（这意味着 <code>writeByte</code>
     * 与对整数参数的 <code>write</code> 完全相同。）通过此方法写入的字节可以由
     * <code>DataInput</code> 接口的 <code>readByte</code> 方法读取，该方法将返回一个
     * 等于 <code>(byte)v</code> 的 <code>byte</code>。
     *
     * @param      v   要写入的字节值。
     * @throws     IOException  如果发生 I/O 错误。
     */
    void writeByte(int v) throws IOException;

    /**
     * 将两个字节写入输出流以表示参数的值。
     * 要写入的字节值，按顺序显示如下：
     * <pre>{@code
     * (byte)(0xff & (v >> 8))
     * (byte)(0xff & v)
     * }</pre> <p>
     * 通过此方法写入的字节可以由 <code>DataInput</code> 接口的 <code>readShort</code> 方法读取，
     * 该方法将返回一个等于 <code>(short)v</code> 的 <code>short</code>。
     *
     * @param      v   要写入的 <code>short</code> 值。
     * @throws     IOException  如果发生 I/O 错误。
     */
    void writeShort(int v) throws IOException;

    /**
     * 将一个 <code>char</code> 值（由两个字节组成）写入输出流。
     * 要写入的字节值，按顺序显示如下：
     * <pre>{@code
     * (byte)(0xff & (v >> 8))
     * (byte)(0xff & v)
     * }</pre><p>
     * 通过此方法写入的字节可以由 <code>DataInput</code> 接口的 <code>readChar</code> 方法读取，
     * 该方法将返回一个等于 <code>(char)v</code> 的 <code>char</code>。
     *
     * @param      v   要写入的 <code>char</code> 值。
     * @throws     IOException  如果发生 I/O 错误。
     */
    void writeChar(int v) throws IOException;

    /**
     * 将一个 <code>int</code> 值（由四个字节组成）写入输出流。
     * 要写入的字节值，按顺序显示如下：
     * <pre>{@code
     * (byte)(0xff & (v >> 24))
     * (byte)(0xff & (v >> 16))
     * (byte)(0xff & (v >>  8))
     * (byte)(0xff & v)
     * }</pre><p>
     * 通过此方法写入的字节可以由 <code>DataInput</code> 接口的 <code>readInt</code> 方法读取，
     * 该方法将返回一个等于 <code>v</code> 的 <code>int</code>。
     *
     * @param      v   要写入的 <code>int</code> 值。
     * @throws     IOException  如果发生 I/O 错误。
     */
    void writeInt(int v) throws IOException;


/**
 * 将一个 <code>long</code> 值（由八个字节组成）写入输出流。
 * 要写入的字节值，按以下顺序：
 * <pre>{@code
 * (byte)(0xff & (v >> 56))
 * (byte)(0xff & (v >> 48))
 * (byte)(0xff & (v >> 40))
 * (byte)(0xff & (v >> 32))
 * (byte)(0xff & (v >> 24))
 * (byte)(0xff & (v >> 16))
 * (byte)(0xff & (v >>  8))
 * (byte)(0xff & v)
 * }</pre><p>
 * 通过此方法写入的字节可以由 <code>DataInput</code> 接口的 <code>readLong</code> 方法读取，
 * 该方法将返回一个等于 <code>v</code> 的 <code>long</code> 值。
 *
 * @param      v   要写入的 <code>long</code> 值。
 * @throws     IOException  如果发生 I/O 错误。
 */
void writeLong(long v) throws IOException;

/**
 * 将一个 <code>float</code> 值（由四个字节组成）写入输出流。
 * 它首先将此 <code>float</code> 值转换为 <code>int</code>，
 * 转换方式与 <code>Float.floatToIntBits</code> 方法完全相同，
 * 然后以与 <code>writeInt</code> 方法完全相同的方式写入 <code>int</code> 值。
 * 通过此方法写入的字节可以由 <code>DataInput</code> 接口的 <code>readFloat</code> 方法读取，
 * 该方法将返回一个等于 <code>v</code> 的 <code>float</code> 值。
 *
 * @param      v   要写入的 <code>float</code> 值。
 * @throws     IOException  如果发生 I/O 错误。
 */
void writeFloat(float v) throws IOException;

/**
 * 将一个 <code>double</code> 值（由八个字节组成）写入输出流。
 * 它首先将此 <code>double</code> 值转换为 <code>long</code>，
 * 转换方式与 <code>Double.doubleToLongBits</code> 方法完全相同，
 * 然后以与 <code>writeLong</code> 方法完全相同的方式写入 <code>long</code> 值。
 * 通过此方法写入的字节可以由 <code>DataInput</code> 接口的 <code>readDouble</code> 方法读取，
 * 该方法将返回一个等于 <code>v</code> 的 <code>double</code> 值。
 *
 * @param      v   要写入的 <code>double</code> 值。
 * @throws     IOException  如果发生 I/O 错误。
 */
void writeDouble(double v) throws IOException;

/**
 * 将一个字符串写入输出流。
 * 对于字符串 <code>s</code> 中的每个字符，按顺序写入一个字节到输出流。
 * 如果 <code>s</code> 为 <code>null</code>，则抛出 <code>NullPointerException</code>。<p>
 * 如果 <code>s.length</code> 为零，则不写入任何字节。否则，
 * 首先写入字符 <code>s[0]</code>，然后是 <code>s[1]</code>，依此类推；
 * 最后写入的字符是 <code>s[s.length-1]</code>。
 * 每个字符写入一个字节，即低字节，写入方式与 <code>writeByte</code> 方法完全相同。
 * 字符串中每个字符的高八位被忽略。
 *
 * @param      s   要写入的字节字符串。
 * @throws     IOException  如果发生 I/O 错误。
 */
void writeBytes(String s) throws IOException;

/**
 * 按顺序将字符串 <code>s</code> 中的每个字符写入输出流，
 * 每个字符写入两个字节。如果 <code>s</code> 为 <code>null</code>，
 * 则抛出 <code>NullPointerException</code>。如果 <code>s.length</code> 为零，
 * 则不写入任何字符。否则，首先写入字符 <code>s[0]</code>，然后是 <code>s[1]</code>，
 * 依此类推；最后写入的字符是 <code>s[s.length-1]</code>。
 * 每个字符实际写入两个字节，先写高字节，写入方式与 <code>writeChar</code> 方法完全相同。
 *
 * @param      s   要写入的字符串值。
 * @throws     IOException  如果发生 I/O 错误。
 */
void writeChars(String s) throws IOException;

/**
 * 将两个字节的长度信息写入输出流，然后是字符串 <code>s</code> 中每个字符的
 * <a href="DataInput.html#modified-utf-8">修改后的 UTF-8</a> 表示形式。
 * 如果 <code>s</code> 为 <code>null</code>，则抛出 <code>NullPointerException</code>。
 * 字符串 <code>s</code> 中的每个字符根据字符值转换为一个、两个或三个字节。<p>
 * 如果字符 <code>c</code> 在范围 <code>&#92;u0001</code> 到 <code>&#92;u007f</code> 之间，
 * 则表示为一个字节：<pre>(byte)c </pre> <p>
 * 如果字符 <code>c</code> 为 <code>&#92;u0000</code> 或在范围 <code>&#92;u0080</code>
 * 到 <code>&#92;u07ff</code> 之间，则表示为两个字节，按以下顺序写入：<pre>{@code
 * (byte)(0xc0 | (0x1f & (c >> 6)))
 * (byte)(0x80 | (0x3f & c))
 * }</pre> <p> 如果字符 <code>c</code> 在范围 <code>&#92;u0800</code>
 * 到 <code>uffff</code> 之间，则表示为三个字节，按以下顺序写入：<pre>{@code
 * (byte)(0xe0 | (0x0f & (c >> 12)))
 * (byte)(0x80 | (0x3f & (c >>  6)))
 * (byte)(0x80 | (0x3f & c))
 * }</pre> <p> 首先，计算表示字符串 <code>s</code> 中所有字符所需的总字节数。
 * 如果此数字大于 <code>65535</code>，则抛出 <code>UTFDataFormatException</code>。
 * 否则，此长度以与 <code>writeShort</code> 方法完全相同的方式写入输出流；
 * 之后，字符串 <code>s</code> 中每个字符的一字节、二字节或三字节表示形式被写入。<p>
 * 通过此方法写入的字节可以由 <code>DataInput</code> 接口的 <code>readUTF</code> 方法读取，
 * 该方法将返回一个等于 <code>s</code> 的 <code>String</code>。
 *
 * @param      s   要写入的字符串值。
 * @throws     IOException  如果发生 I/O 错误。
 */
void writeUTF(String s) throws IOException;
}
