
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
 * {@code DataInput} 接口提供了从二进制流中读取字节并
 * 从中重建任何 Java 基本类型数据的功能。还提供了
 * 从 <a href="#modified-utf-8">修改的 UTF-8</a> 格式数据中
 * 重建 {@code String} 的功能。
 * <p>
 * 通常，此接口中的所有读取
 * 例程都会在文件结束前无法读取所需数量的字节时抛出 {@code EOFException}
 * （这是 {@code IOException} 的一种）。如果由于任何原因（文件结束除外）无法读取任何字节，
 * 则会抛出除 {@code EOFException} 之外的 {@code IOException}。
 * 特别是，如果输入流已被关闭，可能会抛出 {@code IOException}。
 *
 * <h3><a name="modified-utf-8">修改的 UTF-8</a></h3>
 * <p>
 * DataInput 和 DataOutput 接口的实现以稍作修改的 UTF-8 格式表示
 * Unicode 字符串。（有关标准 UTF-8 格式的信息，请参阅《Unicode 标准 4.0》的
 * <i>3.9 Unicode 编码形式</i> 部分）。
 * 请注意，下表中，最高有效位出现在最左列。
 *
 * <blockquote>
 *   <table border="1" cellspacing="0" cellpadding="8"
 *          summary="Bit values and bytes">
 *     <tr>
 *       <th colspan="9"><span style="font-weight:normal">
 *         范围在 {@code '\u005Cu0001'} 到
 *         {@code '\u005Cu007F'} 之间的所有字符都由一个字节表示：</span></th>
 *     </tr>
 *     <tr>
 *       <td></td>
 *       <th colspan="8" id="bit_a">位值</th>
 *     </tr>
 *     <tr>
 *       <th id="byte1_a">字节 1</th>
 *       <td><center>0</center>
 *       <td colspan="7"><center>位 6-0</center>
 *     </tr>
 *     <tr>
 *       <th colspan="9"><span style="font-weight:normal">
 *         空字符 {@code '\u005Cu0000'} 和范围在 {@code '\u005Cu0080'} 到 {@code '\u005Cu07FF'} 之间的字符
 *         由两个字节表示：</span></th>
 *     </tr>
 *     <tr>
 *       <td></td>
 *       <th colspan="8" id="bit_b">位值</th>
 *     </tr>
 *     <tr>
 *       <th id="byte1_b">字节 1</th>
 *       <td><center>1</center>
 *       <td><center>1</center>
 *       <td><center>0</center>
 *       <td colspan="5"><center>位 10-6</center>
 *     </tr>
 *     <tr>
 *       <th id="byte2_a">字节 2</th>
 *       <td><center>1</center>
 *       <td><center>0</center>
 *       <td colspan="6"><center>位 5-0</center>
 *     </tr>
 *     <tr>
 *       <th colspan="9"><span style="font-weight:normal">
 *         范围在 {@code '\u005Cu0800'}
 *         到 {@code '\u005CuFFFF'} 之间的 {@code char} 值由三个字节表示：</span></th>
 *     </tr>
 *     <tr>
 *       <td></td>
 *       <th colspan="8"id="bit_c">位值</th>
 *     </tr>
 *     <tr>
 *       <th id="byte1_c">字节 1</th>
 *       <td><center>1</center>
 *       <td><center>1</center>
 *       <td><center>1</center>
 *       <td><center>0</center>
 *       <td colspan="4"><center>位 15-12</center>
 *     </tr>
 *     <tr>
 *       <th id="byte2_b">字节 2</th>
 *       <td><center>1</center>
 *       <td><center>0</center>
 *       <td colspan="6"><center>位 11-6</center>
 *     </tr>
 *     <tr>
 *       <th id="byte3">字节 3</th>
 *       <td><center>1</center>
 *       <td><center>0</center>
 *       <td colspan="6"><center>位 5-0</center>
 *     </tr>
 *   </table>
 * </blockquote>
 * <p>
 * 该格式与标准 UTF-8 格式之间的差异如下：
 * <ul>
 * <li>空字节 {@code '\u005Cu0000'} 以 2 字节格式编码
 *     而不是 1 字节，因此编码的字符串中永远不会出现嵌入的空字节。
 * <li>仅使用 1 字节、2 字节和 3 字节格式。
 * <li><a href="../lang/Character.html#unicode">补充字符</a>
 *     以代理对的形式表示。
 * </ul>
 * @author  Frank Yellin
 * @see     java.io.DataInputStream
 * @see     java.io.DataOutput
 * @since   JDK1.0
 */
public
interface DataInput {
    /**
     * 从输入流中读取一些字节并将其存储到缓冲区
     * 数组 {@code b} 中。读取的字节数
     * 等于 {@code b} 的长度。
     * <p>
     * 此方法会阻塞，直到以下条件之一发生：
     * <ul>
     * <li>{@code b.length}
     * 字节的输入数据可用，此时将正常返回。
     *
     * <li>检测到文件结束，此时将抛出 {@code EOFException}。
     *
     * <li>发生 I/O 错误，此时将抛出除
     * {@code EOFException} 之外的 {@code IOException}。
     * </ul>
     * <p>
     * 如果 {@code b} 为 {@code null}，
     * 则抛出 {@code NullPointerException}。
     * 如果 {@code b.length} 为零，则
     * 不读取任何字节。否则，第一个
     * 读取的字节存储到元素 {@code b[0]} 中，
     * 下一个字节存储到 {@code b[1]} 中，
     * 依此类推。
     * 如果此方法抛出异常，则可能
     * 只更新了 {@code b} 中的部分字节。
     *
     * @param     b   用于读取数据的缓冲区。
     * @exception  EOFException  如果此流在读取所有字节之前到达结尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    void readFully(byte b[]) throws IOException;

    /**
     *
     * 从输入流中读取 {@code len}
     * 字节。
     * <p>
     * 此方法
     * 会阻塞，直到以下条件之一发生：
     * <ul>
     * <li>{@code len} 字节
     * 的输入数据可用，此时将正常返回。
     *
     * <li>检测到文件结束，此时将抛出 {@code EOFException}。
     *
     * <li>发生 I/O 错误，此时将抛出除
     * {@code EOFException} 之外的 {@code IOException}。
     * </ul>
     * <p>
     * 如果 {@code b} 为 {@code null}，
     * 则抛出 {@code NullPointerException}。
     * 如果 {@code off} 为负数，或 {@code len}
     * 为负数，或 {@code off+len} 大于数组 {@code b} 的长度，
     * 则抛出 {@code IndexOutOfBoundsException}。
     * 如果 {@code len} 为零，
     * 则不读取任何字节。否则，第一个
     * 读取的字节存储到元素 {@code b[off]} 中，
     * 下一个字节存储到 {@code b[off+1]} 中，
     * 依此类推。读取的字节数最多等于 {@code len}。
     *
     * @param     b   用于读取数据的缓冲区。
     * @param off  指定偏移量的 int。
     * @param len  指定要读取的字节数的 int。
     * @exception  EOFException  如果此流在读取所有字节之前到达结尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    void readFully(byte b[], int off, int len) throws IOException;

                /**
     * 尝试跳过
     * {@code n} 个字节
     * 的输入流数据，并丢弃跳过的字节。然而，
     * 它可能跳过
     * 一些较小数量的
     * 字节，甚至可能是零。这可能是由
     * 许多条件引起的；
     * 在跳过 {@code n} 个字节之前到达
     * 文件末尾只是其中之一。
     * 此方法从不抛出 {@code EOFException}。
     * 实际
     * 跳过的字节数被返回。
     *
     * @param      n   要跳过的字节数。
     * @return     实际跳过的字节数。
     * @exception  IOException   如果发生 I/O 错误。
     */
    int skipBytes(int n) throws IOException;

    /**
     * 读取一个输入字节并返回
     * {@code true} 如果该字节非零，
     * {@code false} 如果该字节为零。
     * 此方法适用于读取
     * 由 {@code writeBoolean}
     * 方法写入的字节。
     *
     * @return     读取的 {@code boolean} 值。
     * @exception  EOFException  如果此流在读取
     *               所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    boolean readBoolean() throws IOException;

    /**
     * 读取并返回一个输入字节。
     * 该字节被视为
     * 范围在 {@code -128} 到 {@code 127} 之间的有符号值。
     * 此方法适用于
     * 读取由 {@code writeByte}
     * 方法写入的字节。
     *
     * @return     读取的 8 位值。
     * @exception  EOFException  如果此流在读取
     *               所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    byte readByte() throws IOException;

    /**
     * 读取一个输入字节，将其扩展为
     * 类型 {@code int}，并返回
     * 结果，因此范围在
     * {@code 0}
     * 到 {@code 255} 之间。
     * 此方法适用于读取
     * 由 {@code writeByte}
     * 方法写入的字节
     * 如果 {@code writeByte} 的参数
     * 打算是 {@code 0} 到 {@code 255} 之间的值。
     *
     * @return     读取的无符号 8 位值。
     * @exception  EOFException  如果此流在读取
     *               所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    int readUnsignedByte() throws IOException;

    /**
     * 读取两个输入字节并返回
     * 一个 {@code short} 值。设 {@code a}
     * 是读取的第一个字节，{@code b}
     * 是读取的第二个字节。返回的值
     * 是：
     * <pre>{@code (short)((a << 8) | (b & 0xff))
     * }</pre>
     * 此方法
     * 适用于读取由
     * {@code writeShort} 方法写入的字节。
     *
     * @return     读取的 16 位值。
     * @exception  EOFException  如果此流在读取
     *               所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    short readShort() throws IOException;

    /**
     * 读取两个输入字节并返回
     * 一个 {@code int} 值，范围在 {@code 0}
     * 到 {@code 65535} 之间。设 {@code a}
     * 是读取的第一个字节，
     * {@code b}
     * 是读取的第二个字节。返回的值是：
     * <pre>{@code (((a & 0xff) << 8) | (b & 0xff))
     * }</pre>
     * 此方法适用于读取由
     * {@code writeShort} 方法写入的字节
     * 如果 {@code writeShort} 的参数
     * 打算是 {@code 0} 到 {@code 65535} 之间的值。
     *
     * @return     读取的无符号 16 位值。
     * @exception  EOFException  如果此流在读取
     *               所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    int readUnsignedShort() throws IOException;

    /**
     * 读取两个输入字节并返回一个 {@code char} 值。
     * 设 {@code a}
     * 是读取的第一个字节，{@code b}
     * 是读取的第二个字节。返回的值
     * 是：
     * <pre>{@code (char)((a << 8) | (b & 0xff))
     * }</pre>
     * 此方法
     * 适用于读取由
     * {@code writeChar} 方法写入的字节。
     *
     * @return     读取的 {@code char} 值。
     * @exception  EOFException  如果此流在读取
     *               所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    char readChar() throws IOException;

    /**
     * 读取四个输入字节并返回一个
     * {@code int} 值。设 {@code a-d}
     * 是读取的第一到第四个字节。返回的值是：
     * <pre>{@code
     * (((a & 0xff) << 24) | ((b & 0xff) << 16) |
     *  ((c & 0xff) <<  8) | (d & 0xff))
     * }</pre>
     * 此方法适用于
     * 读取由 {@code writeInt}
     * 方法写入的字节。
     *
     * @return     读取的 {@code int} 值。
     * @exception  EOFException  如果此流在读取
     *               所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    int readInt() throws IOException;

    /**
     * 读取八个输入字节并返回
     * 一个 {@code long} 值。设 {@code a-h}
     * 是读取的第一到第八个字节。
     * 返回的值是：
     * <pre>{@code
     * (((long)(a & 0xff) << 56) |
     *  ((long)(b & 0xff) << 48) |
     *  ((long)(c & 0xff) << 40) |
     *  ((long)(d & 0xff) << 32) |
     *  ((long)(e & 0xff) << 24) |
     *  ((long)(f & 0xff) << 16) |
     *  ((long)(g & 0xff) <<  8) |
     *  ((long)(h & 0xff)))
     * }</pre>
     * <p>
     * 此方法适用于
     * 读取由 {@code writeLong}
     * 方法写入的字节。
     *
     * @return     读取的 {@code long} 值。
     * @exception  EOFException  如果此流在读取
     *               所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    long readLong() throws IOException;

                /**
     * 读取四个输入字节并返回
     * 一个 {@code float} 值。它通过首先以
     * {@code readInt} 方法完全相同的方式
     * 构造一个 {@code int} 值，然后将这个 {@code int}
     * 值以 {@code Float.intBitsToFloat} 方法完全相同的方式
     * 转换为 {@code float} 值。
     * 该方法适用于读取
     * 由 {@code DataOutput} 接口的 {@code writeFloat}
     * 方法写入的字节。
     *
     * @return     读取的 {@code float} 值。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    float readFloat() throws IOException;

    /**
     * 读取八个输入字节并返回
     * 一个 {@code double} 值。它通过首先以
     * {@code readLong} 方法完全相同的方式
     * 构造一个 {@code long} 值，然后将这个 {@code long}
     * 值以 {@code Double.longBitsToDouble} 方法完全相同的方式
     * 转换为 {@code double} 值。
     * 该方法适用于读取
     * 由 {@code DataOutput} 接口的 {@code writeDouble}
     * 方法写入的字节。
     *
     * @return     读取的 {@code double} 值。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    double readDouble() throws IOException;

    /**
     * 从输入流中读取下一行文本。
     * 它依次读取字节，将
     * 每个字节单独转换为一个字符，
     * 直到遇到行终止符或
     * 文件末尾；读取的字符随后
     * 作为 {@code String} 返回。注意
     * 由于此
     * 方法处理字节，
     * 因此不支持完整的 Unicode
     * 字符集。
     * <p>
     * 如果在读取一个字节之前遇到文件末尾，
     * 则返回 {@code null}。否则，每个读取的字节
     * 都转换为类型 {@code char}
     * 通过零扩展。如果遇到字符 {@code '\n'}
     * 则丢弃并停止读取。如果遇到字符 {@code '\r'}
     * 则丢弃，并且如果
     * 下一个字节转换为
     * 字符 {@code '\n'}，则该字符
     * 也被丢弃；然后停止读取。如果在遇到
     * 字符 {@code '\n'} 和
     * {@code '\r'} 之前遇到文件末尾，读取
     * 停止。一旦读取停止，返回一个 {@code String}
     * 包含所有读取且未丢弃的字符，
     * 按顺序排列。请注意，此字符串中的每个字符
     * 的值都小于 {@code \u005Cu0100}，
     * 即 {@code (char)256}。
     *
     * @return 从输入流中读取的下一行文本，
     *         或如果在读取一个字节之前遇到文件末尾，
     *         则返回 {@code null}。
     * @exception  IOException  如果发生 I/O 错误。
     */
    String readLine() throws IOException;

    /**
     * 读取使用
     * <a href="#modified-utf-8">修改的 UTF-8</a>
     * 格式编码的字符串。
     * {@code readUTF} 的一般约定是它读取一个以修改
     * UTF-8 格式编码的 Unicode
     * 字符串的表示；然后将这些字符
     * 返回为一个 {@code String}。
     * <p>
     * 首先，读取两个字节并用于
     * 以 {@code readUnsignedShort}
     * 方法完全相同的方式构造一个无符号 16 位整数。这个整数值称为
     * <i>UTF 长度</i>，并指定要读取的其他字节数。
     * 这些字节然后通过将它们分组转换为字符。每个组的长度
     * 从组中第一个字节的值计算得出。组后的字节，如果有，
     * 是下一个组的第一个字节。
     * <p>
     * 如果组的第一个字节
     * 匹配位模式 {@code 0xxxxxxx}
     * （其中 {@code x} 表示“可能是 {@code 0}
     * 或 {@code 1}”），则该组仅包含
     * 该字节。该字节通过零扩展
     * 形成一个字符。
     * <p>
     * 如果组的第一个字节
     * 匹配位模式 {@code 110xxxxx}，
     * 则该组包含该字节 {@code a}
     * 和第二个字节 {@code b}。如果
     * 没有字节 {@code b}（因为字节
     * {@code a} 是要读取的最后一个字节），或者字节 {@code b}
     * 不匹配位模式 {@code 10xxxxxx}，
     * 则抛出 {@code UTFDataFormatException}。
     * 否则，该组转换为字符：
     * <pre>{@code (char)(((a & 0x1F) << 6) | (b & 0x3F))
     * }</pre>
     * 如果组的第一个字节
     * 匹配位模式 {@code 1110xxxx}，
     * 则该组包含该字节 {@code a}
     * 和另外两个字节 {@code b} 和 {@code c}。
     * 如果没有字节 {@code c}（因为
     * 字节 {@code a} 是要读取的最后
     * 两个字节之一），或者字节 {@code b} 或字节 {@code c}
     * 不匹配位模式 {@code 10xxxxxx}，
     * 则抛出 {@code UTFDataFormatException}。
     * 否则，该组转换为字符：
     * <pre>{@code
     * (char)(((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F))
     * }</pre>
     * 如果组的第一个字节匹配
     * 模式 {@code 1111xxxx} 或模式
     * {@code 10xxxxxx}，则抛出 {@code UTFDataFormatException}。
     * <p>
     * 如果在执行此整个过程中的任何时候遇到文件末尾，
     * 则抛出 {@code EOFException}。
     * <p>
     * 通过此过程将每个组转换为
     * 一个字符后，这些字符
     * 按照其对应的组从输入流中读取的顺序收集，
     * 形成一个 {@code String}，
     * 并返回。
     * <p>
     * {@code DataOutput} 接口的 {@code writeUTF}
     * 方法可用于写入适合
     * 由此方法读取的数据。
     * @return     一个 Unicode 字符串。
     * @exception  EOFException            如果此流在读取所有字节之前到达末尾。
     * @exception  IOException             如果发生 I/O 错误。
     * @exception  UTFDataFormatException  如果字节不表示
     *               有效的修改 UTF-8 编码的字符串。
     */
    String readUTF() throws IOException;
}
