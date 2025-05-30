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
import java.nio.charset.CharsetEncoder;
import sun.nio.cs.StreamEncoder;


/**
 * OutputStreamWriter 是一个从字符流到字节流的桥梁：
 * 写入到它的字符会被使用指定的 {@link java.nio.charset.Charset 字符集} 编码成字节。可以按名称指定字符集，也可以显式提供，或者接受平台的默认字符集。
 *
 * <p> 每次调用 write() 方法都会导致编码转换器被调用，处理给定的字符。生成的字节会累积在一个缓冲区中，然后写入底层的输出流。此缓冲区的大小可以指定，但默认情况下足够大，适用于大多数用途。请注意，传递给 write() 方法的字符不会被缓冲。
 *
 * <p> 为了提高效率，可以考虑将 OutputStreamWriter 包装在 BufferedWriter 中，以避免频繁调用转换器。例如：
 *
 * <pre>
 * Writer out
 *   = new BufferedWriter(new OutputStreamWriter(System.out));
 * </pre>
 *
 * <p> 一个 <i>代理对</i> 是由两个 <tt>char</tt> 值组成的序列，表示一个字符：一个范围在 '&#92;uD800' 到 '&#92;uDBFF' 之间的 <i>高</i> 代理，后跟一个范围在 '&#92;uDC00' 到 '&#92;uDFFF' 之间的 <i>低</i> 代理。
 *
 * <p> 一个 <i>畸形的代理元素</i> 是一个没有后跟低代理的高代理，或者是一个没有前导高代理的低代理。
 *
 * <p> 本类总是用字符集的默认 <i>替换序列</i> 替换畸形的代理元素和无法映射的字符序列。如果需要对编码过程进行更多控制，应使用 {@linkplain java.nio.charset.CharsetEncoder} 类。
 *
 * @see BufferedWriter
 * @see OutputStream
 * @see java.nio.charset.Charset
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class OutputStreamWriter extends Writer {

    private final StreamEncoder se;

    /**
     * 创建一个使用指定字符集的 OutputStreamWriter。
     *
     * @param  out
     *         一个 OutputStream
     *
     * @param  charsetName
     *         支持的 {@link java.nio.charset.Charset 字符集} 的名称
     *
     * @exception  UnsupportedEncodingException
     *             如果指定的编码不被支持
     */
    public OutputStreamWriter(OutputStream out, String charsetName)
        throws UnsupportedEncodingException
    {
        super(out);
        if (charsetName == null)
            throw new NullPointerException("charsetName");
        se = StreamEncoder.forOutputStreamWriter(out, this, charsetName);
    }

    /**
     * 创建一个使用默认字符编码的 OutputStreamWriter。
     *
     * @param  out  一个 OutputStream
     */
    public OutputStreamWriter(OutputStream out) {
        super(out);
        try {
            se = StreamEncoder.forOutputStreamWriter(out, this, (String)null);
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    /**
     * 创建一个使用给定字符集的 OutputStreamWriter。
     *
     * @param  out
     *         一个 OutputStream
     *
     * @param  cs
     *         一个字符集
     *
     * @since 1.4
     * @spec JSR-51
     */
    public OutputStreamWriter(OutputStream out, Charset cs) {
        super(out);
        if (cs == null)
            throw new NullPointerException("charset");
        se = StreamEncoder.forOutputStreamWriter(out, this, cs);
    }

    /**
     * 创建一个使用给定字符集编码器的 OutputStreamWriter。
     *
     * @param  out
     *         一个 OutputStream
     *
     * @param  enc
     *         一个字符集编码器
     *
     * @since 1.4
     * @spec JSR-51
     */
    public OutputStreamWriter(OutputStream out, CharsetEncoder enc) {
        super(out);
        if (enc == null)
            throw new NullPointerException("charset encoder");
        se = StreamEncoder.forOutputStreamWriter(out, this, enc);
    }

    /**
     * 返回此流使用的字符编码的名称。
     *
     * <p> 如果编码有历史名称，则返回该名称；否则返回编码的规范名称。
     *
     * <p> 如果此实例是使用 {@link
     * #OutputStreamWriter(OutputStream, String)} 构造函数创建的，则返回的名称（对于编码是唯一的）可能与传递给构造函数的名称不同。如果流已关闭，此方法可能返回 <tt>null</tt>。</p>
     *
     * @return 此编码的历史名称，或如果流已关闭则可能返回 <code>null</code>
     *
     * @see java.nio.charset.Charset
     *
     * @revised 1.4
     * @spec JSR-51
     */
    public String getEncoding() {
        return se.getEncoding();
    }

    /**
     * 将输出缓冲区刷新到底层字节流，但不刷新字节流本身。此方法不是私有的，以便 PrintStream 调用。
     */
    void flushBuffer() throws IOException {
        se.flushBuffer();
    }

    /**
     * 写入单个字符。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void write(int c) throws IOException {
        se.write(c);
    }

    /**
     * 写入字符数组的一部分。
     *
     * @param  cbuf  字符缓冲区
     * @param  off   开始写入字符的偏移量
     * @param  len   要写入的字符数
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        se.write(cbuf, off, len);
    }

    /**
     * 写入字符串的一部分。
     *
     * @param  str  一个字符串
     * @param  off  开始写入字符的偏移量
     * @param  len  要写入的字符数
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void write(String str, int off, int len) throws IOException {
        se.write(str, off, len);
    }

    /**
     * 刷新流。
     *
     * @exception  IOException  如果发生 I/O 错误
     */
    public void flush() throws IOException {
        se.flush();
    }

    public void close() throws IOException {
        se.close();
    }
}
