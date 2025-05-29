/*
 * 版权所有 (c) 1994, 2004, Oracle 和/或其子公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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
 * 数据输出流允许应用程序以可移植的方式将原始 Java 数据类型写入输出流。应用程序可以使用数据输入流将数据读回。
 *
 * @author  未署名
 * @see     java.io.DataInputStream
 * @since   JDK1.0
 */
public
class DataOutputStream extends FilterOutputStream implements DataOutput {
    /**
     * 到目前为止已写入数据输出流的字节数。如果此计数器溢出，它将被包装为 Integer.MAX_VALUE。
     */
    protected int written;

    /**
     * bytearr 由 writeUTF 按需初始化
     */
    private byte[] bytearr = null;

    /**
     * 创建一个新的数据输出流，将数据写入指定的基础输出流。计数器 <code>written</code> 被设置为零。
     *
     * @param   out   要保存以供后续使用的基础输出流。
     * @see     java.io.FilterOutputStream#out
     */
    public DataOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * 将指定的值增加到 written 计数器，直到它达到 Integer.MAX_VALUE。
     */
    private void incCount(int value) {
        int temp = written + value;
        if (temp < 0) {
            temp = Integer.MAX_VALUE;
        }
        written = temp;
    }

    /**
     * 将指定的字节（参数 <code>b</code> 的低八位）写入基础输出流。如果没有抛出异常，计数器 <code>written</code> 将增加 <code>1</code>。
     * <p>
     * 实现 <code>OutputStream</code> 的 <code>write</code> 方法。
     *
     * @param      b   要写入的 <code>byte</code>。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     */
    public synchronized void write(int b) throws IOException {
        out.write(b);
        incCount(1);
    }

    /**
     * 从指定的字节数组中写入 <code>len</code> 个字节，从偏移量 <code>off</code> 开始写入基础输出流。如果没有抛出异常，计数器 <code>written</code> 将增加 <code>len</code>。
     *
     * @param      b     数据。
     * @param      off   数据中的起始偏移量。
     * @param      len   要写入的字节数。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     */
    public synchronized void write(byte b[], int off, int len)
        throws IOException
    {
        out.write(b, off, len);
        incCount(len);
    }

    /**
     * 刷新此数据输出流。这会强制任何缓冲的输出字节被写入流中。
     * <p>
     * <code>DataOutputStream</code> 的 <code>flush</code> 方法调用其基础输出流的 <code>flush</code> 方法。
     *
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     * @see        java.io.OutputStream#flush()
     */
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * 将一个 <code>boolean</code> 写入基础输出流，作为一个 1 字节的值。值 <code>true</code> 被写入为值 <code>(byte)1</code>；值 <code>false</code> 被写入为值 <code>(byte)0</code>。如果没有抛出异常，计数器 <code>written</code> 将增加 <code>1</code>。
     *
     * @param      v   要写入的 <code>boolean</code> 值。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     */
    public final void writeBoolean(boolean v) throws IOException {
        out.write(v ? 1 : 0);
        incCount(1);
    }

    /**
     * 将一个 <code>byte</code> 写入基础输出流，作为一个 1 字节的值。如果没有抛出异常，计数器 <code>written</code> 将增加 <code>1</code>。
     *
     * @param      v   要写入的 <code>byte</code> 值。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     */
    public final void writeByte(int v) throws IOException {
        out.write(v);
        incCount(1);
    }

    /**
     * 将一个 <code>short</code> 写入基础输出流，作为两个字节，高位字节在前。如果没有抛出异常，计数器 <code>written</code> 将增加 <code>2</code>。
     *
     * @param      v   要写入的 <code>short</code>。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     */
    public final void writeShort(int v) throws IOException {
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
        incCount(2);
    }

    /**
     * 将一个 <code>char</code> 写入基础输出流，作为一个 2 字节的值，高位字节在前。如果没有抛出异常，计数器 <code>written</code> 将增加 <code>2</code>。
     *
     * @param      v   要写入的 <code>char</code> 值。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     */
    public final void writeChar(int v) throws IOException {
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
        incCount(2);
    }

    /**
     * 将一个 <code>int</code> 写入基础输出流，作为四个字节，高位字节在前。如果没有抛出异常，计数器 <code>written</code> 将增加 <code>4</code>。
     *
     * @param      v   要写入的 <code>int</code>。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     */
    public final void writeInt(int v) throws IOException {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>>  8) & 0xFF);
        out.write((v >>>  0) & 0xFF);
        incCount(4);
    }


                private byte writeBuffer[] = new byte[8];

    /**
     * 将一个 <code>long</code> 写入底层输出流，作为八个字节，最高字节在前。如果没有抛出异常，计数器
     * <code>written</code> 会增加 <code>8</code>。
     *
     * @param      v   要写入的 <code>long</code>。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     */
    public final void writeLong(long v) throws IOException {
        writeBuffer[0] = (byte)(v >>> 56);
        writeBuffer[1] = (byte)(v >>> 48);
        writeBuffer[2] = (byte)(v >>> 40);
        writeBuffer[3] = (byte)(v >>> 32);
        writeBuffer[4] = (byte)(v >>> 24);
        writeBuffer[5] = (byte)(v >>> 16);
        writeBuffer[6] = (byte)(v >>>  8);
        writeBuffer[7] = (byte)(v >>>  0);
        out.write(writeBuffer, 0, 8);
        incCount(8);
    }

    /**
     * 使用 <code>Float</code> 类中的 <code>floatToIntBits</code> 方法将浮点参数转换为 <code>int</code>，
     * 然后将该 <code>int</code> 值作为 4 字节的量写入底层输出流，最高字节在前。如果没有抛出异常，计数器
     * <code>written</code> 会增加 <code>4</code>。
     *
     * @param      v   要写入的 <code>float</code> 值。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     * @see        java.lang.Float#floatToIntBits(float)
     */
    public final void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    /**
     * 使用 <code>Double</code> 类中的 <code>doubleToLongBits</code> 方法将双精度参数转换为 <code>long</code>，
     * 然后将该 <code>long</code> 值作为 8 字节的量写入底层输出流，最高字节在前。如果没有抛出异常，计数器
     * <code>written</code> 会增加 <code>8</code>。
     *
     * @param      v   要写入的 <code>double</code> 值。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     * @see        java.lang.Double#doubleToLongBits(double)
     */
    public final void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    /**
     * 将字符串作为一系列字节写入底层输出流。字符串中的每个字符按顺序写入，丢弃其高八位。如果没有抛出异常，计数器
     * <code>written</code> 会增加 <code>s</code> 的长度。
     *
     * @param      s   要写入的字节字符串。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.FilterOutputStream#out
     */
    public final void writeBytes(String s) throws IOException {
        int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            out.write((byte)s.charAt(i));
        }
        incCount(len);
    }

    /**
     * 将字符串作为一系列字符写入底层输出流。每个字符都像 <code>writeChar</code> 方法一样写入数据输出流。如果没有抛出异常，计数器
     * <code>written</code> 会增加 <code>s</code> 长度的两倍。
     *
     * @param      s   要写入的 <code>String</code> 值。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.DataOutputStream#writeChar(int)
     * @see        java.io.FilterOutputStream#out
     */
    public final void writeChars(String s) throws IOException {
        int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            int v = s.charAt(i);
            out.write((v >>> 8) & 0xFF);
            out.write((v >>> 0) & 0xFF);
        }
        incCount(len * 2);
    }

    /**
     * 以独立于机器的方式使用 <a href="DataInput.html#modified-utf-8">修改的 UTF-8</a>
     * 编码将字符串写入底层输出流。
     * <p>
     * 首先，将两个字节写入输出流，就像使用 <code>writeShort</code> 方法一样，表示要跟随的字节数。这个值是实际写入的字节数，而不是字符串的长度。在长度之后，字符串中的每个字符按顺序输出，使用修改的 UTF-8 编码。如果没有抛出异常，计数器
     * <code>written</code> 会增加写入输出流的总字节数。这至少是 <code>str</code> 的长度加 2，最多是 <code>str</code> 的长度加 3 倍。
     *
     * @param      str   要写入的字符串。
     * @exception  IOException  如果发生 I/O 错误。
     */
    public final void writeUTF(String str) throws IOException {
        writeUTF(str, this);
    }

    /**
     * 以独立于机器的方式使用 <a href="DataInput.html#modified-utf-8">修改的 UTF-8</a>
     * 编码将字符串写入指定的 DataOutput。
     * <p>
     * 首先，将两个字节写入 out，就像使用 <code>writeShort</code> 方法一样，表示要跟随的字节数。这个值是实际写入的字节数，而不是字符串的长度。在长度之后，字符串中的每个字符按顺序输出，使用修改的 UTF-8 编码。如果没有抛出异常，计数器
     * <code>written</code> 会增加写入输出流的总字节数。这至少是 <code>str</code> 的长度加 2，最多是 <code>str</code> 的长度加 3 倍。
     *
     * @param      str   要写入的字符串。
     * @param      out   写入的目标。
     * @return     写出的字节数。
     * @exception  IOException  如果发生 I/O 错误。
     */
    static int writeUTF(String str, DataOutput out) throws IOException {
        int strlen = str.length();
        int utflen = 0;
        int c, count = 0;


                    /* 使用 charAt 而不是将字符串复制到字符数组 */
        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        if (utflen > 65535)
            throw new UTFDataFormatException(
                "编码后的字符串太长: " + utflen + " 字节");

        byte[] bytearr = null;
        if (out instanceof DataOutputStream) {
            DataOutputStream dos = (DataOutputStream)out;
            if(dos.bytearr == null || (dos.bytearr.length < (utflen+2)))
                dos.bytearr = new byte[(utflen*2) + 2];
            bytearr = dos.bytearr;
        } else {
            bytearr = new byte[utflen+2];
        }

        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);

        int i=0;
        for (i=0; i<strlen; i++) {
           c = str.charAt(i);
           if (!((c >= 0x0001) && (c <= 0x007F))) break;
           bytearr[count++] = (byte) c;
        }

        for (;i < strlen; i++){
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;

            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            }
        }
        out.write(bytearr, 0, utflen+2);
        return utflen + 2;
    }

    /**
     * 返回计数器 <code>written</code> 的当前值，
     * 即到目前为止写入此数据输出流的字节数。
     * 如果计数器溢出，它将被包装为 Integer.MAX_VALUE。
     *
     * @return  <code>written</code> 字段的值。
     * @see     java.io.DataOutputStream#written
     */
    public final int size() {
        return written;
    }
}
