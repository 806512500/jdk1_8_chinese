
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * {@code DataInput} 接口提供从二进制流中读取字节并将其重建为任何 Java 基本类型的功能。还提供了一个从
 * <a href="#modified-utf-8">修改的 UTF-8</a> 格式中重建 {@code String} 的功能。
 * <p>
 * 通常，如果在读取所需字节数之前到达文件末尾，此接口中的所有读取例程都会抛出 {@code EOFException}
 * （这是 {@code IOException} 的一种）。如果由于任何原因（文件末尾除外）无法读取任何字节，则会抛出
 * 除 {@code EOFException} 之外的 {@code IOException}。特别是，如果输入流已关闭，则可能会抛出
 * {@code IOException}。
 *
 * <h3><a name="modified-utf-8">Modified UTF-8</a></h3>
 * <p>
 * DataInput 和 DataOutput 接口的实现以稍作修改的 UTF-8 格式表示 Unicode 字符串。
 * （有关标准 UTF-8 格式的详细信息，请参阅《Unicode 标准 4.0》第 3.9 节《Unicode 编码形式》）。
 * 请注意，下表中，最高有效位出现在最左边的列中。
 *
 * <blockquote>
 *   <table border="1" cellspacing="0" cellpadding="8"
 *          summary="Bit values and bytes">
 *     <tr>
 *       <th colspan="9"><span style="font-weight:normal">
 *         所有在 {@code '\u005Cu0001'} 到 {@code '\u005Cu007F'} 范围内的字符都由一个字节表示：</span></th>
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
 *         空字符 {@code '\u005Cu0000'} 和在 {@code '\u005Cu0080'} 到 {@code '\u005Cu07FF'} 范围内的字符
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
 *         {@code char} 值在 {@code '\u005Cu0800'} 到 {@code '\u005CuFFFF'} 范围内的字符
 *         由三个字节表示：</span></th>
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
 * 该格式与标准 UTF-8 格式的不同之处如下：
 * <ul>
 * <li>空字符 {@code '\u005Cu0000'} 以 2 字节格式编码，而不是 1 字节格式，因此编码的字符串中不会出现嵌入的空字符。
 * <li>仅使用 1 字节、2 字节和 3 字节格式。
 * <li><a href="../lang/Character.html#unicode">补充字符</a> 以代理对的形式表示。
 * </ul>
 * @author  Frank Yellin
 * @see     java.io.DataInputStream
 * @see     java.io.DataOutput
 * @since   JDK1.0
 */
public
interface DataInput {
    /**
     * 从输入流中读取一些字节并将其存储到缓冲区数组 {@code b} 中。读取的字节数等于 {@code b} 的长度。
     * <p>
     * 此方法在以下条件之一发生时阻塞：
     * <ul>
     * <li>{@code b.length} 个输入数据字节可用，此时将正常返回。
     *
     * <li>检测到文件末尾，此时将抛出 {@code EOFException}。
     *
     * <li>发生 I/O 错误，此时将抛出除 {@code EOFException} 之外的 {@code IOException}。
     * </ul>
     * <p>
     * 如果 {@code b} 为 {@code null}，则抛出 {@code NullPointerException}。
     * 如果 {@code b.length} 为零，则不读取任何字节。否则，第一个读取的字节存储在元素 {@code b[0]} 中，
     * 下一个字节存储在 {@code b[1]} 中，依此类推。
     * 如果此方法抛出异常，则 {@code b} 的某些但不是全部字节可能已被输入流中的数据更新。
     *
     * @param     b   存储数据的缓冲区。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    void readFully(byte b[]) throws IOException;

    /**
     * 从输入流中读取 {@code len} 个字节。
     * <p>
     * 此方法在以下条件之一发生时阻塞：
     * <ul>
     * <li>{@code len} 个输入数据字节可用，此时将正常返回。
     *
     * <li>检测到文件末尾，此时将抛出 {@code EOFException}。
     *
     * <li>发生 I/O 错误，此时将抛出除 {@code EOFException} 之外的 {@code IOException}。
     * </ul>
     * <p>
     * 如果 {@code b} 为 {@code null}，则抛出 {@code NullPointerException}。
     * 如果 {@code off} 为负数，或 {@code len} 为负数，或 {@code off+len} 大于数组 {@code b} 的长度，
     * 则抛出 {@code IndexOutOfBoundsException}。
     * 如果 {@code len} 为零，则不读取任何字节。否则，第一个读取的字节存储在元素 {@code b[off]} 中，
     * 下一个字节存储在 {@code b[off+1]} 中，依此类推。读取的字节数最多等于 {@code len}。
     *
     * @param     b   存储数据的缓冲区。
     * @param off  指定偏移量的 int。
     * @param len  指定要读取的字节数的 int。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    void readFully(byte b[], int off, int len) throws IOException;

    /**
     * 尝试从输入流中跳过 {@code n} 个字节并丢弃跳过的字节。然而，它可能跳过更少的字节数，甚至可能是零。
     * 这可能是由多种情况引起的；在跳过 {@code n} 个字节之前到达文件末尾只是其中一种可能性。
     * 此方法从不抛出 {@code EOFException}。实际跳过的字节数将返回。
     *
     * @param      n   要跳过的字节数。
     * @return     实际跳过的字节数。
     * @exception  IOException   如果发生 I/O 错误。
     */
    int skipBytes(int n) throws IOException;

    /**
     * 读取一个输入字节并返回 {@code true} 如果该字节非零，返回 {@code false} 如果该字节为零。
     * 此方法适合于读取由 {@code DataOutput} 接口的 {@code writeBoolean} 方法写入的字节。
     *
     * @return     读取的 {@code boolean} 值。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    boolean readBoolean() throws IOException;

    /**
     * 读取并返回一个输入字节。该字节被视为范围在 {@code -128} 到 {@code 127} 之间的有符号值。
     * 此方法适合于读取由 {@code DataOutput} 接口的 {@code writeByte} 方法写入的字节。
     *
     * @return     读取的 8 位值。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    byte readByte() throws IOException;

    /**
     * 读取一个输入字节，将其零扩展为 {@code int} 类型，并返回结果，因此结果在 {@code 0} 到 {@code 255} 范围内。
     * 此方法适合于读取由 {@code DataOutput} 接口的 {@code writeByte} 方法写入的字节，前提是 {@code writeByte} 的参数
     * 旨在表示 {@code 0} 到 {@code 255} 范围内的值。
     *
     * @return     读取的无符号 8 位值。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    int readUnsignedByte() throws IOException;

    /**
     * 读取两个输入字节并返回一个 {@code short} 值。设 {@code a} 为第一个读取的字节，{@code b} 为第二个读取的字节。
     * 返回的值为：
     * <pre>{@code (short)((a << 8) | (b & 0xff))
     * }</pre>
     * 此方法适合于读取由 {@code DataOutput} 接口的 {@code writeShort} 方法写入的字节。
     *
     * @return     读取的 16 位值。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    short readShort() throws IOException;

    /**
     * 读取两个输入字节并返回一个范围在 {@code 0} 到 {@code 65535} 之间的 {@code int} 值。设 {@code a} 为第一个读取的字节，
     * {@code b} 为第二个读取的字节。返回的值为：
     * <pre>{@code (((a & 0xff) << 8) | (b & 0xff))
     * }</pre>
     * 此方法适合于读取由 {@code DataOutput} 接口的 {@code writeShort} 方法写入的字节，前提是 {@code writeShort} 的参数
     * 旨在表示 {@code 0} 到 {@code 65535} 范围内的值。
     *
     * @return     读取的无符号 16 位值。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    int readUnsignedShort() throws IOException;

    /**
     * 读取两个输入字节并返回一个 {@code char} 值。设 {@code a} 为第一个读取的字节，{@code b} 为第二个读取的字节。
     * 返回的值为：
     * <pre>{@code (char)((a << 8) | (b & 0xff))
     * }</pre>
     * 此方法适合于读取由 {@code DataOutput} 接口的 {@code writeChar} 方法写入的字节。
     *
     * @return     读取的 {@code char} 值。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    char readChar() throws IOException;

    /**
     * 读取四个输入字节并返回一个 {@code int} 值。设 {@code a-d} 为第一个到第四个读取的字节。返回的值为：
     * <pre>{@code
     * (((a & 0xff) << 24) | ((b & 0xff) << 16) |
     *  ((c & 0xff) <<  8) | (d & 0xff))
     * }</pre>
     * 此方法适合于读取由 {@code DataOutput} 接口的 {@code writeInt} 方法写入的字节。
     *
     * @return     读取的 {@code int} 值。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    int readInt() throws IOException;

    /**
     * 读取八个输入字节并返回一个 {@code long} 值。设 {@code a-h} 为第一个到第八个读取的字节。返回的值为：
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
     * 此方法适合于读取由 {@code DataOutput} 接口的 {@code writeLong} 方法写入的字节。
     *
     * @return     读取的 {@code long} 值。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    long readLong() throws IOException;

    /**
     * 读取四个输入字节并返回一个 {@code float} 值。它首先以与 {@code readInt} 方法完全相同的方式构造一个 {@code int} 值，
     * 然后将此 {@code int} 值转换为 {@code float}，转换方式与 {@code Float.intBitsToFloat} 方法完全相同。
     * 此方法适合于读取由 {@code DataOutput} 接口的 {@code writeFloat} 方法写入的字节。
     *
     * @return     读取的 {@code float} 值。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    float readFloat() throws IOException;


                /**
     * 读取八个输入字节并返回一个 {@code double} 值。它首先以与 {@code readLong}
     * 方法完全相同的方式构造一个 {@code long} 值，然后以与 {@code Double.longBitsToDouble}
     * 方法完全相同的方式将此 {@code long} 值转换为 {@code double} 值。
     * 此方法适用于读取 {@code DataOutput} 接口的 {@code writeDouble}
     * 方法写入的字节。
     *
     * @return     读取的 {@code double} 值。
     * @exception  EOFException  如果此流在读取所有字节之前到达末尾。
     * @exception  IOException   如果发生 I/O 错误。
     */
    double readDouble() throws IOException;

    /**
     * 从输入流中读取下一行文本。
     * 它读取连续的字节，将每个字节单独转换为字符，
     * 直到遇到行终止符或文件末尾；读取的字符随后
     * 作为 {@code String} 返回。注意，因为此
     * 方法处理字节，所以不支持完整的 Unicode
     * 字符集。
     * <p>
     * 如果在读取任何字节之前遇到文件末尾，则返回 {@code null}。
     * 否则，每个读取的字节都转换为类型 {@code char}
     * 通过零扩展。如果遇到字符 {@code '\n'}，
     * 则丢弃并停止读取。如果遇到字符 {@code '\r'}，
     * 则丢弃，并且如果下一个字节转换为
     * 字符 {@code '\n'}，则该字符也被丢弃；
     * 然后停止读取。如果在遇到字符 {@code '\n'}
     * 和 {@code '\r'} 之前遇到文件末尾，则停止读取。
     * 一旦停止读取，返回一个包含所有读取且未丢弃的字符的 {@code String}，
     * 按顺序排列。注意，此字符串中的每个字符
     * 的值都小于 {@code \u005Cu0100}，即 {@code (char)256}。
     *
     * @return 从输入流中读取的下一行文本，
     *         或者如果在读取任何字节之前遇到文件末尾，则返回 {@code null}。
     * @exception  IOException  如果发生 I/O 错误。
     */
    String readLine() throws IOException;

    /**
     * 读取使用 <a href="#modified-utf-8">修改的 UTF-8</a>
     * 格式编码的字符串。
     * {@code readUTF} 的一般约定是它读取以修改的
     * UTF-8 格式编码的 Unicode 字符串的表示；这些字符
     * 然后作为 {@code String} 返回。
     * <p>
     * 首先，读取两个字节并以与 {@code readUnsignedShort}
     * 方法完全相同的方式构造一个无符号 16 位整数。此整数值称为
     * <i>UTF 长度</i>，并指定要读取的额外字节数。
     * 这些字节然后通过将它们分组来转换为字符。每个组的长度
     * 从该组的第一个字节的值计算得出。组后的字节，如果有，
     * 是下一组的第一个字节。
     * <p>
     * 如果一个组的第一个字节
     * 匹配位模式 {@code 0xxxxxxx}
     * （其中 {@code x} 表示“可以是 {@code 0}
     * 或 {@code 1}”），那么该组仅由该字节组成。该字节通过零扩展
     * 形成一个字符。
     * <p>
     * 如果一个组的第一个字节
     * 匹配位模式 {@code 110xxxxx}，
     * 那么该组由该字节 {@code a}
     * 和第二个字节 {@code b} 组成。如果不存在字节 {@code b}
     * （因为字节 {@code a} 是要读取的最后一个字节），或者字节 {@code b}
     * 不匹配位模式 {@code 10xxxxxx}，
     * 则抛出 {@code UTFDataFormatException}。
     * 否则，该组转换为字符：
     * <pre>{@code (char)(((a & 0x1F) << 6) | (b & 0x3F))
     * }</pre>
     * 如果一个组的第一个字节
     * 匹配位模式 {@code 1110xxxx}，
     * 那么该组由该字节 {@code a}
     * 和两个更多字节 {@code b} 和 {@code c} 组成。
     * 如果不存在字节 {@code c}
     * （因为字节 {@code a} 是要读取的最后两个字节之一），或者字节 {@code b}
     * 或字节 {@code c} 不匹配位模式 {@code 10xxxxxx}，
     * 则抛出 {@code UTFDataFormatException}。
     * 否则，该组转换为字符：
     * <pre>{@code
     * (char)(((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F))
     * }</pre>
     * 如果一个组的第一个字节匹配
     * 模式 {@code 1111xxxx} 或模式
     * {@code 10xxxxxx}，则抛出 {@code UTFDataFormatException}。
     * <p>
     * 如果在此整个过程中任何时候遇到文件末尾，
     * 则抛出 {@code EOFException}。
     * <p>
     * 通过此过程将每个组转换为
     * 字符后，这些字符按其对应的组从
     * 输入流中读取的相同顺序收集，形成一个 {@code String}，
     * 并返回。
     * <p>
     * {@code DataOutput} 接口的 {@code writeUTF}
     * 方法可用于写入适合
     * 由此方法读取的数据。
     * @return     一个 Unicode 字符串。
     * @exception  EOFException            如果此流在读取所有字节之前到达末尾。
     * @exception  IOException             如果发生 I/O 错误。
     * @exception  UTFDataFormatException  如果字节不表示
     *               有效的修改的 UTF-8 编码的字符串。
     */
    String readUTF() throws IOException;
}
