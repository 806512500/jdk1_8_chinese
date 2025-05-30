
/*
 * Copyright (c) 1994, 2006, Oracle and/or its affiliates. All rights reserved.
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
 * DataInputStream 类允许应用程序以与机器无关的方式从底层输入流读取基本 Java 数据类型。
 * 应用程序使用 DataOutputStream 写入数据，这些数据可以稍后由 DataInputStream 读取。
 * <p>
 * DataInputStream 不一定适合多线程访问。线程安全是可选的，由使用此类方法的用户负责。
 *
 * @author  Arthur van Hoff
 * @see     java.io.DataOutputStream
 * @since   JDK1.0
 */
public
class DataInputStream extends FilterInputStream implements DataInput {

    /**
     * 创建一个使用指定底层 InputStream 的 DataInputStream。
     *
     * @param  in   指定的输入流
     */
    public DataInputStream(InputStream in) {
        super(in);
    }

    /**
     * 由 readUTF 按需初始化的工作数组。
     */
    private byte bytearr[] = new byte[80];
    private char chararr[] = new char[80];

    /**
     * 从包含的输入流中读取一些字节并存储到缓冲数组 <code>b</code> 中。实际读取的字节数作为整数返回。此方法会阻塞，直到有输入数据可用、检测到文件结束或抛出异常。
     *
     * <p>如果 <code>b</code> 为 null，则抛出 <code>NullPointerException</code>。如果 <code>b</code> 的长度为零，则不读取任何字节并返回 <code>0</code>；否则，尝试读取至少一个字节。如果因为流已到达文件结束而没有可用的字节，则返回 <code>-1</code>；否则，至少读取一个字节并存储到 <code>b</code> 中。
     *
     * <p>第一个读取的字节存储到元素 <code>b[0]</code> 中，下一个字节存储到 <code>b[1]</code> 中，依此类推。读取的字节数最多等于 <code>b</code> 的长度。设 <code>k</code> 为实际读取的字节数；这些字节将存储在元素 <code>b[0]</code> 到 <code>b[k-1]</code> 中，元素 <code>b[k]</code> 到 <code>b[b.length-1]</code> 不受影响。
     *
     * <p><code>read(b)</code> 方法的效果与以下相同：
     * <blockquote><pre>
     * read(b, 0, b.length)
     * </pre></blockquote>
     *
     * @param      b   用于存储数据的缓冲区。
     * @return     读取到缓冲区中的总字节数，如果因为到达流的结束而没有更多数据，则返回 <code>-1</code>。
     * @exception  IOException 如果由于任何原因（文件结束除外）无法读取第一个字节，流已关闭且底层输入流不支持关闭后的读取，或发生其他 I/O 错误。
     * @see        java.io.FilterInputStream#in
     * @see        java.io.InputStream#read(byte[], int, int)
     */
    public final int read(byte b[]) throws IOException {
        return in.read(b, 0, b.length);
    }

    /**
     * 从包含的输入流中读取最多 <code>len</code> 个字节的数据到字节数组中。尝试读取多达 <code>len</code> 个字节，但实际读取的字节数可能更少，甚至为零。实际读取的字节数作为整数返回。
     *
     * <p>此方法会阻塞，直到有输入数据可用、检测到文件结束或抛出异常。
     *
     * <p>如果 <code>len</code> 为零，则不读取任何字节并返回 <code>0</code>；否则，尝试读取至少一个字节。如果因为流已到达文件结束而没有可用的字节，则返回 <code>-1</code>；否则，至少读取一个字节并存储到 <code>b</code> 中。
     *
     * <p>第一个读取的字节存储到元素 <code>b[off]</code> 中，下一个字节存储到 <code>b[off+1]</code> 中，依此类推。读取的字节数最多等于 <code>len</code>。设 <i>k</i> 为实际读取的字节数；这些字节将存储在元素 <code>b[off]</code> 到 <code>b[off+</code><i>k</i><code>-1]</code> 中，元素 <code>b[off+</code><i>k</i><code>]</code> 到 <code>b[off+len-1]</code> 不受影响。
     *
     * <p>在任何情况下，元素 <code>b[0]</code> 到 <code>b[off]</code> 和元素 <code>b[off+len]</code> 到 <code>b[b.length-1]</code> 都不受影响。
     *
     * @param      b     用于存储数据的缓冲区。
     * @param off the start offset in the destination array <code>b</code>
     * @param      len   最大读取字节数。
     * @return     读取到缓冲区中的总字节数，如果因为到达流的结束而没有更多数据，则返回 <code>-1</code>。
     * @exception  NullPointerException 如果 <code>b</code> 为 <code>null</code>。
     * @exception  IndexOutOfBoundsException 如果 <code>off</code> 为负数，<code>len</code> 为负数，或 <code>len</code> 大于 <code>b.length - off</code>
     * @exception  IOException 如果由于任何原因（文件结束除外）无法读取第一个字节，流已关闭且底层输入流不支持关闭后的读取，或发生其他 I/O 错误。
     * @see        java.io.FilterInputStream#in
     * @see        java.io.InputStream#read(byte[], int, int)
     */
    public final int read(byte b[], int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    /**
     * 参见 DataInput 接口的 <code>readFully</code> 方法的一般约定。
     * <p>
     * 此操作的字节从包含的输入流中读取。
     *
     * @param      b   用于存储数据的缓冲区。
     * @exception  EOFException  如果此输入流在读取所有字节之前到达文件结束。
     * @exception  IOException   如果流已关闭且包含的输入流不支持关闭后的读取，或发生其他 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public final void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }

    /**
     * 参见 DataInput 接口的 <code>readFully</code> 方法的一般约定。
     * <p>
     * 此操作的字节从包含的输入流中读取。
     *
     * @param      b     用于存储数据的缓冲区。
     * @param      off   数据的起始偏移量。
     * @param      len   要读取的字节数。
     * @exception  EOFException  如果此输入流在读取所有字节之前到达文件结束。
     * @exception  IOException   如果流已关闭且包含的输入流不支持关闭后的读取，或发生其他 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public final void readFully(byte b[], int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }

    /**
     * 参见 DataInput 接口的 <code>skipBytes</code> 方法的一般约定。
     * <p>
     * 此操作的字节从包含的输入流中读取。
     *
     * @param      n   要跳过的字节数。
     * @return     实际跳过的字节数。
     * @exception  IOException  如果包含的输入流不支持 seek，或流已关闭且包含的输入流不支持关闭后的读取，或发生其他 I/O 错误。
     */
    public final int skipBytes(int n) throws IOException {
        int total = 0;
        int cur = 0;

        while ((total<n) && ((cur = (int) in.skip(n-total)) > 0)) {
            total += cur;
        }

        return total;
    }

    /**
     * 参见 DataInput 接口的 <code>readBoolean</code> 方法的一般约定。
     * <p>
     * 此操作的字节从包含的输入流中读取。
     *
     * @return     读取的 <code>boolean</code> 值。
     * @exception  EOFException  如果此输入流已到达文件结束。
     * @exception  IOException   如果流已关闭且包含的输入流不支持关闭后的读取，或发生其他 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public final boolean readBoolean() throws IOException {
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        return (ch != 0);
    }

    /**
     * 参见 DataInput 接口的 <code>readByte</code> 方法的一般约定。
     * <p>
     * 此操作的字节从包含的输入流中读取。
     *
     * @return     作为有符号 8 位 <code>byte</code> 的此输入流的下一个字节。
     * @exception  EOFException  如果此输入流已到达文件结束。
     * @exception  IOException   如果流已关闭且包含的输入流不支持关闭后的读取，或发生其他 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public final byte readByte() throws IOException {
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        return (byte)(ch);
    }

    /**
     * 参见 DataInput 接口的 <code>readUnsignedByte</code> 方法的一般约定。
     * <p>
     * 此操作的字节从包含的输入流中读取。
     *
     * @return     作为无符号 8 位数的此输入流的下一个字节。
     * @exception  EOFException  如果此输入流已到达文件结束。
     * @exception  IOException   如果流已关闭且包含的输入流不支持关闭后的读取，或发生其他 I/O 错误。
     * @see         java.io.FilterInputStream#in
     */
    public final int readUnsignedByte() throws IOException {
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        return ch;
    }

    /**
     * 参见 DataInput 接口的 <code>readShort</code> 方法的一般约定。
     * <p>
     * 此操作的字节从包含的输入流中读取。
     *
     * @return     作为有符号 16 位数的此输入流的下一个两个字节。
     * @exception  EOFException  如果此输入流在读取两个字节之前到达文件结束。
     * @exception  IOException   如果流已关闭且包含的输入流不支持关闭后的读取，或发生其他 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public final short readShort() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    /**
     * 参见 DataInput 接口的 <code>readUnsignedShort</code> 方法的一般约定。
     * <p>
     * 此操作的字节从包含的输入流中读取。
     *
     * @return     作为无符号 16 位整数的此输入流的下一个两个字节。
     * @exception  EOFException  如果此输入流在读取两个字节之前到达文件结束。
     * @exception  IOException   如果流已关闭且包含的输入流不支持关闭后的读取，或发生其他 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public final int readUnsignedShort() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (ch1 << 8) + (ch2 << 0);
    }

    /**
     * 参见 DataInput 接口的 <code>readChar</code> 方法的一般约定。
     * <p>
     * 此操作的字节从包含的输入流中读取。
     *
     * @return     作为 <code>char</code> 的此输入流的下一个两个字节。
     * @exception  EOFException  如果此输入流在读取两个字节之前到达文件结束。
     * @exception  IOException   如果流已关闭且包含的输入流不支持关闭后的读取，或发生其他 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public final char readChar() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char)((ch1 << 8) + (ch2 << 0));
    }

    /**
     * 参见 DataInput 接口的 <code>readInt</code> 方法的一般约定。
     * <p>
     * 此操作的字节从包含的输入流中读取。
     *
     * @return     作为 <code>int</code> 的此输入流的下一个四个字节。
     * @exception  EOFException  如果此输入流在读取四个字节之前到达文件结束。
     * @exception  IOException   如果流已关闭且包含的输入流不支持关闭后的读取，或发生其他 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public final int readInt() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }


                private byte readBuffer[] = new byte[8];

    /**
     * 查看 <code>readLong</code> 方法的通用合同
     * <code>DataInput</code>。
     * <p>
     * 用于此操作的字节从包含的
     * 输入流中读取。
     *
     * @return     从这个输入流中读取的下一个八个字节，解释为
     *             <code>long</code>。
     * @exception  EOFException  如果此输入流在读取八个字节之前到达结尾。
     * @exception  IOException   如果流已关闭且包含的
     *             输入流不支持关闭后的读取，或者
     *             发生其他 I/O 错误。
     * @see        java.io.FilterInputStream#in
     */
    public final long readLong() throws IOException {
        readFully(readBuffer, 0, 8);
        return (((long)readBuffer[0] << 56) +
                ((long)(readBuffer[1] & 255) << 48) +
                ((long)(readBuffer[2] & 255) << 40) +
                ((long)(readBuffer[3] & 255) << 32) +
                ((long)(readBuffer[4] & 255) << 24) +
                ((readBuffer[5] & 255) << 16) +
                ((readBuffer[6] & 255) <<  8) +
                ((readBuffer[7] & 255) <<  0));
    }

    /**
     * 查看 <code>readFloat</code> 方法的通用合同
     * <code>DataInput</code>。
     * <p>
     * 用于此操作的字节从包含的
     * 输入流中读取。
     *
     * @return     从这个输入流中读取的下四个字节，解释为
     *             <code>float</code>。
     * @exception  EOFException  如果此输入流在读取四个字节之前到达结尾。
     * @exception  IOException   如果流已关闭且包含的
     *             输入流不支持关闭后的读取，或者
     *             发生其他 I/O 错误。
     * @see        java.io.DataInputStream#readInt()
     * @see        java.lang.Float#intBitsToFloat(int)
     */
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * 查看 <code>readDouble</code> 方法的通用合同
     * <code>DataInput</code>。
     * <p>
     * 用于此操作的字节从包含的
     * 输入流中读取。
     *
     * @return     从这个输入流中读取的下一个八个字节，解释为
     *             <code>double</code>。
     * @exception  EOFException  如果此输入流在读取八个字节之前到达结尾。
     * @exception  IOException   如果流已关闭且包含的
     *             输入流不支持关闭后的读取，或者
     *             发生其他 I/O 错误。
     * @see        java.io.DataInputStream#readLong()
     * @see        java.lang.Double#longBitsToDouble(long)
     */
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    private char lineBuffer[];

    /**
     * 查看 <code>readLine</code> 方法的通用合同
     * <code>DataInput</code>。
     * <p>
     * 用于此操作的字节从包含的
     * 输入流中读取。
     *
     * @deprecated 此方法不能正确地将字节转换为字符。
     * 自 JDK&nbsp;1.1 起，读取文本行的首选方法是使用
     * <code>BufferedReader.readLine()</code> 方法。使用
     * <code>DataInputStream</code> 类读取行的程序可以通过将以下代码：
     * <blockquote><pre>
     *     DataInputStream d =&nbsp;new&nbsp;DataInputStream(in);
     * </pre></blockquote>
     * 替换为：
     * <blockquote><pre>
     *     BufferedReader d
     *          =&nbsp;new&nbsp;BufferedReader(new&nbsp;InputStreamReader(in));
     * </pre></blockquote>
     *
     * @return     从这个输入流中读取的下一行文本。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.BufferedReader#readLine()
     * @see        java.io.FilterInputStream#in
     */
    @Deprecated
    public final String readLine() throws IOException {
        char buf[] = lineBuffer;

        if (buf == null) {
            buf = lineBuffer = new char[128];
        }

        int room = buf.length;
        int offset = 0;
        int c;

loop:   while (true) {
            switch (c = in.read()) {
              case -1:
              case '\n':
                break loop;

              case '\r':
                int c2 = in.read();
                if ((c2 != '\n') && (c2 != -1)) {
                    if (!(in instanceof PushbackInputStream)) {
                        this.in = new PushbackInputStream(in);
                    }
                    ((PushbackInputStream)in).unread(c2);
                }
                break loop;

              default:
                if (--room < 0) {
                    buf = new char[offset + 128];
                    room = buf.length - offset - 1;
                    System.arraycopy(lineBuffer, 0, buf, 0, offset);
                    lineBuffer = buf;
                }
                buf[offset++] = (char) c;
                break;
            }
        }
        if ((c == -1) && (offset == 0)) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }

    /**
     * 查看 <code>readUTF</code> 方法的通用合同
     * <code>DataInput</code>。
     * <p>
     * 用于此操作的字节从包含的
     * 输入流中读取。
     *
     * @return     一个 Unicode 字符串。
     * @exception  EOFException  如果此输入流在读取所有字节之前到达结尾。
     * @exception  IOException   如果流已关闭且包含的
     *             输入流不支持关闭后的读取，或者
     *             发生其他 I/O 错误。
     * @exception  UTFDataFormatException 如果字节不表示有效的
     *             修改后的 UTF-8 编码的字符串。
     * @see        java.io.DataInputStream#readUTF(java.io.DataInput)
     */
    public final String readUTF() throws IOException {
        return readUTF(this);
    }

    /**
     * 从
     * 流 <code>in</code> 中读取一个表示
     * Unicode 字符串的
     * <a href="DataInput.html#modified-utf-8">修改后的 UTF-8</a> 格式的编码；
     * 然后将这些字符作为 <code>String</code> 返回。
     * 修改后的 UTF-8 表示的详细信息
     * 与 <code>DataInput</code> 的 <code>readUTF</code>
     * 方法完全相同。
     *
     * @param      in   一个数据输入流。
     * @return     一个 Unicode 字符串。
     * @exception  EOFException            如果输入流在读取所有字节之前到达结尾。
     * @exception  IOException   如果流已关闭且包含的
     *             输入流不支持关闭后的读取，或者
     *             发生其他 I/O 错误。
     * @exception  UTFDataFormatException  如果字节不表示有效的
     *               修改后的 UTF-8 编码的 Unicode 字符串。
     * @see        java.io.DataInputStream#readUnsignedShort()
     */
    public final static String readUTF(DataInput in) throws IOException {
        int utflen = in.readUnsignedShort();
        byte[] bytearr = null;
        char[] chararr = null;
        if (in instanceof DataInputStream) {
            DataInputStream dis = (DataInputStream)in;
            if (dis.bytearr.length < utflen){
                dis.bytearr = new byte[utflen*2];
                dis.chararr = new char[utflen*2];
            }
            chararr = dis.chararr;
            bytearr = dis.bytearr;
        } else {
            bytearr = new byte[utflen];
            chararr = new char[utflen];
        }

        int c, char2, char3;
        int count = 0;
        int chararr_count=0;

        in.readFully(bytearr, 0, utflen);

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            if (c > 127) break;
            count++;
            chararr[chararr_count++]=(char)c;
        }

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            switch (c >> 4) {
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    /* 0xxxxxxx*/
                    count++;
                    chararr[chararr_count++]=(char)c;
                    break;
                case 12: case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                            "malformed input: partial character at end");
                    char2 = (int) bytearr[count-1];
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException(
                            "malformed input around byte " + count);
                    chararr[chararr_count++]=(char)(((c & 0x1F) << 6) |
                                                    (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                            "malformed input: partial character at end");
                    char2 = (int) bytearr[count-2];
                    char3 = (int) bytearr[count-1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException(
                            "malformed input around byte " + (count-1));
                    chararr[chararr_count++]=(char)(((c     & 0x0F) << 12) |
                                                    ((char2 & 0x3F) << 6)  |
                                                    ((char3 & 0x3F) << 0));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException(
                        "malformed input around byte " + count);
            }
        }
        // 产生的字符数可能少于 utflen
        return new String(chararr, 0, chararr_count);
    }
}
