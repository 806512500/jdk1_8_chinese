/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import sun.nio.cs.StreamDecoder;


/**
 * InputStreamReader 是一个从字节流到字符流的桥梁：它读取字节并使用指定的 {@link
 * java.nio.charset.Charset 字符集} 将其解码为字符。它可以按名称指定字符集，也可以显式提供字符集，或者接受平台的默认字符集。
 *
 * <p> 每次调用 InputStreamReader 的 read() 方法都可能导致从底层字节输入流读取一个或多个字节。为了高效地将字节转换为字符，从底层流读取的字节数可能比当前读取操作所需的字节数多。
 *
 * <p> 为了获得最高效率，可以将 InputStreamReader 包装在 BufferedReader 中。例如：
 *
 * <pre>
 * BufferedReader in
 *   = new BufferedReader(new InputStreamReader(System.in));
 * </pre>
 *
 * @see BufferedReader
 * @see InputStream
 * @see java.nio.charset.Charset
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class InputStreamReader extends Reader {

    private final StreamDecoder sd;

    /**
     * 创建一个使用默认字符集的 InputStreamReader。
     *
     * @param  in   一个 InputStream
     */
    public InputStreamReader(InputStream in) {
        super(in);
        try {
            sd = StreamDecoder.forInputStreamReader(in, this, (String)null); // ## 检查锁对象
        } catch (UnsupportedEncodingException e) {
            // 默认编码应该始终可用
            throw new Error(e);
        }
    }

    /**
     * 创建一个使用指定名称字符集的 InputStreamReader。
     *
     * @param  in
     *         一个 InputStream
     *
     * @param  charsetName
     *         支持的 {@link java.nio.charset.Charset 字符集} 的名称
     *
     * @exception  UnsupportedEncodingException
     *             如果指定的字符集不受支持
     */
    public InputStreamReader(InputStream in, String charsetName)
        throws UnsupportedEncodingException
    {
        super(in);
        if (charsetName == null)
            throw new NullPointerException("charsetName");
        sd = StreamDecoder.forInputStreamReader(in, this, charsetName);
    }

    /**
     * 创建一个使用给定字符集的 InputStreamReader。
     *
     * @param  in       一个 InputStream
     * @param  cs       一个字符集
     *
     * @since 1.4
     * @spec JSR-51
     */
    public InputStreamReader(InputStream in, Charset cs) {
        super(in);
        if (cs == null)
            throw new NullPointerException("charset");
        sd = StreamDecoder.forInputStreamReader(in, this, cs);
    }

    /**
     * 创建一个使用给定字符集解码器的 InputStreamReader。
     *
     * @param  in       一个 InputStream
     * @param  dec      一个字符集解码器
     *
     * @since 1.4
     * @spec JSR-51
     */
    public InputStreamReader(InputStream in, CharsetDecoder dec) {
        super(in);
        if (dec == null)
            throw new NullPointerException("charset decoder");
        sd = StreamDecoder.forInputStreamReader(in, this, dec);
    }

    /**
     * 返回此流使用的字符编码的名称。
     *
     * <p> 如果编码有历史名称，则返回历史名称；否则返回编码的规范名称。
     *
     * <p> 如果此实例是使用 {@link
     * #InputStreamReader(InputStream, String)} 构造函数创建的，则返回的名称（对于编码是唯一的）可能与传递给构造函数的名称不同。如果流已关闭，此方法将返回 <code>null</code>。
     * </p>
     * @return 此编码的历史名称，或如果流已关闭则返回 <code>null</code>
     *
     * @see java.nio.charset.Charset
     *
     * @revised 1.4
     * @spec JSR-51
     */
    public String getEncoding() {
        return sd.getEncoding();
    }

    /**
     * 读取单个字符。
     *
     * @return 读取的字符，或如果已到达流的末尾则返回 -1
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public int read() throws IOException {
        return sd.read();
    }

    /**
     * 将字符读入数组的一部分。
     *
     * @param      cbuf     目标缓冲区
     * @param      offset   开始存储字符的偏移量
     * @param      length   最大读取的字符数
     *
     * @return     读取的字符数，或如果已到达流的末尾则返回 -1
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public int read(char cbuf[], int offset, int length) throws IOException {
        return sd.read(cbuf, offset, length);
    }

    /**
     * 告诉此流是否准备好读取。如果 InputStreamReader 的输入缓冲区不为空，或者可以从底层字节流读取字节，则 InputStreamReader 是准备好的。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public boolean ready() throws IOException {
        return sd.ready();
    }

    public void close() throws IOException {
        sd.close();
    }
}
