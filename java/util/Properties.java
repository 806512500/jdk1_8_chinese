
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

package java.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;

import sun.util.spi.XmlPropertiesProvider;

/**
 * {@code Properties} 类表示一组持久化的属性。这些属性可以保存到流中或从流中加载。属性列表中的每个键及其对应的值都是字符串。
 * <p>
 * 一个属性列表可以包含另一个属性列表作为其“默认值”；如果在原始属性列表中找不到属性键，则会搜索第二个属性列表。
 * <p>
 * 由于 {@code Properties} 继承自 {@code Hashtable}，因此可以使用 {@code put} 和 {@code putAll} 方法应用于
 * {@code Properties} 对象。但强烈不建议使用这些方法，因为它们允许调用者插入键或值不是 {@code String} 的条目。
 * 应该使用 {@code setProperty} 方法。如果在包含非 {@code String} 键或值的“受损” {@code Properties} 对象上调用
 * {@code store} 或 {@code save} 方法，调用将失败。同样，如果在包含非 {@code String} 键的“受损” {@code Properties}
 * 对象上调用 {@code propertyNames} 或 {@code list} 方法，调用也会失败。
 *
 * <p>
 * {@link #load(java.io.Reader) load(Reader)} <tt>/</tt>
 * {@link #store(java.io.Writer, java.lang.String) store(Writer, String)}
 * 方法从和到基于字符的流加载和存储属性，格式简单，以行为单位，具体格式如下所述。
 *
 * {@link #load(java.io.InputStream) load(InputStream)} <tt>/</tt>
 * {@link #store(java.io.OutputStream, java.lang.String) store(OutputStream, String)}
 * 方法的工作方式与 load(Reader)/store(Writer, String) 对相同，不同之处在于输入/输出流使用 ISO 8859-1 字符编码。
 * 无法直接用此编码表示的字符可以使用《Java&trade; 语言规范》第 3.3 节中定义的 Unicode 转义序列编写；
 * 转义序列中只允许一个 'u' 字符。可以使用 native2ascii 工具将属性文件转换为其他字符编码。
 *
 * <p> {@link #loadFromXML(InputStream)} 和 {@link
 * #storeToXML(OutputStream, String, String)} 方法以简单的 XML 格式加载和存储属性。
 * 默认情况下使用 UTF-8 字符编码，但可以根据需要指定特定的编码。实现必须支持 UTF-8 和 UTF-16，可以支持其他编码。
 * XML 属性文档具有以下 DOCTYPE 声明：
 *
 * <pre>
 * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;
 * </pre>
 * 注意，系统 URI (http://java.sun.com/dtd/properties.dtd) 在导出或导入属性时不会被访问；它仅作为一个字符串，用于唯一标识 DTD，该 DTD 为：
 * <pre>
 *    &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *
 *    &lt;!-- DTD for properties --&gt;
 *
 *    &lt;!ELEMENT properties ( comment?, entry* ) &gt;
 *
 *    &lt;!ATTLIST properties version CDATA #FIXED "1.0"&gt;
 *
 *    &lt;!ELEMENT comment (#PCDATA) &gt;
 *
 *    &lt;!ELEMENT entry (#PCDATA) &gt;
 *
 *    &lt;!ATTLIST entry key CDATA #REQUIRED&gt;
 * </pre>
 *
 * <p>此类是线程安全的：多个线程可以共享一个 <tt>Properties</tt> 对象，而无需外部同步。
 *
 * @see <a href="../../../technotes/tools/solaris/native2ascii.html">Solaris 的 native2ascii 工具</a>
 * @see <a href="../../../technotes/tools/windows/native2ascii.html">Windows 的 native2ascii 工具</a>
 *
 * @author  Arthur van Hoff
 * @author  Michael McCloskey
 * @author  Xueming Shen
 * @since   JDK1.0
 */
public
class Properties extends Hashtable<Object,Object> {
    /**
     * 包含默认值的属性列表，用于在本属性列表中未找到键时提供默认值。
     *
     * @serial
     */
    protected Properties defaults;

    /**
     * 创建一个空的属性列表，没有默认值。
     */
    public Properties() {
        this(null);
    }

    /**
     * 使用指定的默认值创建一个空的属性列表。
     *
     * @param   defaults   默认值。
     */
    public Properties(Properties defaults) {
        this.defaults = defaults;
    }

    /**
     * 调用 {@code Hashtable} 方法 {@code put}。为了与 {@code getProperty} 方法保持一致而提供。
     * 强制使用字符串作为属性键和值。返回值是 {@code Hashtable} 调用 {@code put} 的结果。
     *
     * @param key 要放入此属性列表中的键。
     * @param value 与 <tt>key</tt> 对应的值。
     * @return     本属性列表中指定键的前一个值，如果它没有值则返回 {@code null}。
     * @see #getProperty
     * @since    1.2
     */
    public synchronized Object setProperty(String key, String value) {
        return put(key, value);
    }


    /**
     * 从输入字符流中以简单的行定向格式读取属性列表（键值对）。
     * <p>
     * 属性按照行进行处理。有两种类型的行：<i>自然行</i>和<i>逻辑行</i>。
     * 自然行定义为以换行符（{@code \n} 或 {@code \r} 或 {@code \r\n}）或流的结束终止的一行字符。
     * 自然行可能是空白行、注释行或包含键值对的一部分或全部。逻辑行包含键值对的所有数据，这些数据可以分布在多个相邻的自然行中，
     * 通过反斜杠字符 {@code \} 转义行终止符序列来实现。需要注意的是，注释行不能通过这种方式扩展；
     * 每个自然行的注释都必须有自己的注释指示符，如下所述。从输入中读取行，直到流的结束。
     *
     * <p>
     * 仅包含空白字符的自然行被认为是空白行，并被忽略。注释行以 ASCII {@code '#'} 或 {@code '!'} 作为其第一个非空白字符；
     * 注释行也被忽略，不编码键值对信息。除了行终止符，此格式还考虑空格
     * ({@code ' '}, {@code '\u005Cu0020'})、制表符
     * ({@code '\t'}, {@code '\u005Cu0009'}) 和换页符
     * ({@code '\f'}, {@code '\u005Cu000C'}) 为白空间。
     *
     * <p>
     * 如果逻辑行分布在多个自然行上，转义行终止符序列的反斜杠、行终止符序列以及下一行开头的任何空白字符对键或值没有影响。
     * 以下关于键和值解析（加载时）的讨论将假设所有构成键和值的字符都出现在单个自然行上，去除了行延续字符。注意，仅检查行终止符序列前的字符
     * 是不足以决定行终止符是否被转义的；必须有奇数个连续的反斜杠才能转义行终止符。由于输入是从左到右处理的，非零偶数个 2<i>n</i> 个连续的反斜杠
     * 在行终止符之前（或其他地方）编码 <i>n</i> 个反斜杠，经过转义处理后。
     *
     * <p>
     * 键包含从第一个非空白字符开始到但不包括第一个未转义的 {@code '='}、
     * {@code ':'} 或非行终止符的空白字符的所有字符。可以通过在这些键终止字符前加一个反斜杠字符来包含这些键终止字符；
     * 例如：<p>
     *
     * {@code \:\=}<p>
     *
     * 将是两个字符的键 {@code ":="}。行终止符可以使用 {@code \r} 和
     * {@code \n} 转义序列包含。键后的所有空白字符将被跳过；如果键后的第一个非空白字符是 {@code '='} 或 {@code ':'}，
     * 则它将被忽略，其后的所有空白字符也将被跳过。行中剩余的所有字符将成为关联的元素字符串；如果没有剩余的字符，
     * 元素是空字符串 {@code ""}。一旦识别出构成键和元素的原始字符序列，将执行上述转义处理。
     *
     * <p>
     * 例如，以下三行指定键为 {@code "Truth"}，关联的元素值为
     * {@code "Beauty"}：
     * <pre>
     * Truth = Beauty
     *  Truth:Beauty
     * Truth                    :Beauty
     * </pre>
     * 再举一个例子，以下三行指定一个属性：
     * <pre>
     * fruits                           apple, banana, pear, \
     *                                  cantaloupe, watermelon, \
     *                                  kiwi, mango
     * </pre>
     * 键是 {@code "fruits"}，关联的元素是：
     * <pre>"apple, banana, pear, cantaloupe, watermelon, kiwi, mango"</pre>
     * 注意，每个 {@code \} 前有一个空格，因此最终结果中每个逗号后会有一个空格；
     * {@code \}、行终止符和延续行开头的空白字符仅被丢弃，<i>不</i>被其他一个或多个字符替换。
     * <p>
     * 作为第三个例子，以下行：
     * <pre>cheeses
     * </pre>
     * 指定键为 {@code "cheeses"}，关联的元素是空字符串 {@code ""}。
     * <p>
     * <a name="unicodeescapes"></a>
     * 键和元素中的字符可以用类似于字符和字符串字面量中使用的转义序列表示
     * （参见《Java&trade; 语言规范》的第 3.3 和 3.10.6 节）。
     *
     * 与字符和字符串的字符转义序列和 Unicode 转义的不同之处在于：
     *
     * <ul>
     * <li> 不识别八进制转义。
     *
     * <li> 字符序列 {@code \b} <i>不</i> 表示退格字符。
     *
     * <li> 该方法不会将反斜杠字符 {@code \} 之前的一个非有效转义字符视为错误；反斜杠将被静默丢弃。例如，在 Java 字符串中，
     * 序列 {@code "\z"} 会导致编译时错误。相比之下，此方法会静默丢弃反斜杠。因此，此方法将两个字符的序列 {@code "\b"}
     * 视为等同于单个字符 {@code 'b'}。
     *
     * <li> 单引号和双引号不需要转义；但是，根据上述规则，单引号和双引号字符前加反斜杠仍然分别产生单引号和双引号字符。
     *
     * <li> Unicode 转义序列中只允许一个 'u' 字符。
     *
     * </ul>
     * <p>
     * 该方法返回后，指定的流保持打开状态。
     *
     * @param   reader   输入字符流。
     * @throws  IOException  如果从输入流读取时发生错误。
     * @throws  IllegalArgumentException 如果输入中出现格式错误的 Unicode 转义。
     * @since   1.6
     */
    public synchronized void load(Reader reader) throws IOException {
        load0(new LineReader(reader));
    }


                /**
     * 从输入字节流中读取属性列表（键值对）。输入流采用简单的行定向格式，如
     * {@link #load(java.io.Reader) load(Reader)} 中指定的格式，并假定使用
     * ISO 8859-1 字符编码；即每个字节是一个 Latin1 字符。不在 Latin1 中的字符和某些特殊字符，
     * 在键和元素中使用《Java&trade; 语言规范》第 3.3 节中定义的 Unicode 转义表示。
     * <p>
     * 此方法返回后，指定的流保持打开状态。
     *
     * @param      inStream   输入流。
     * @exception  IOException  从输入流读取时发生错误。
     * @throws     IllegalArgumentException 如果输入流包含格式错误的 Unicode 转义序列。
     * @since 1.2
     */
    public synchronized void load(InputStream inStream) throws IOException {
        load0(new LineReader(inStream));
    }

    private void load0 (LineReader lr) throws IOException {
        char[] convtBuf = new char[1024];
        int limit;
        int keyLen;
        int valueStart;
        char c;
        boolean hasSep;
        boolean precedingBackslash;

        while ((limit = lr.readLine()) >= 0) {
            c = 0;
            keyLen = 0;
            valueStart = limit;
            hasSep = false;

            //System.out.println("line=<" + new String(lineBuf, 0, limit) + ">");
            precedingBackslash = false;
            while (keyLen < limit) {
                c = lr.lineBuf[keyLen];
                //需要检查是否转义。
                if ((c == '=' ||  c == ':') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    hasSep = true;
                    break;
                } else if ((c == ' ' || c == '\t' ||  c == '\f') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    break;
                }
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash;
                } else {
                    precedingBackslash = false;
                }
                keyLen++;
            }
            while (valueStart < limit) {
                c = lr.lineBuf[valueStart];
                if (c != ' ' && c != '\t' &&  c != '\f') {
                    if (!hasSep && (c == '=' ||  c == ':')) {
                        hasSep = true;
                    } else {
                        break;
                    }
                }
                valueStart++;
            }
            String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
            String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
            put(key, value);
        }
    }

    /* 从 InputStream/Reader 中读取一个“逻辑行”，跳过所有注释和空白行，并从“自然行”的开头
     * 过滤掉前导空白字符（\u0020, \u0009 和 \u000c）。方法返回“逻辑行”的字符长度，并将行存储在“lineBuf”中。
     */
    class LineReader {
        public LineReader(InputStream inStream) {
            this.inStream = inStream;
            inByteBuf = new byte[8192];
        }

        public LineReader(Reader reader) {
            this.reader = reader;
            inCharBuf = new char[8192];
        }

        byte[] inByteBuf;
        char[] inCharBuf;
        char[] lineBuf = new char[1024];
        int inLimit = 0;
        int inOff = 0;
        InputStream inStream;
        Reader reader;

        int readLine() throws IOException {
            int len = 0;
            char c = 0;

            boolean skipWhiteSpace = true;
            boolean isCommentLine = false;
            boolean isNewLine = true;
            boolean appendedLineBegin = false;
            boolean precedingBackslash = false;
            boolean skipLF = false;

            while (true) {
                if (inOff >= inLimit) {
                    inLimit = (inStream==null)?reader.read(inCharBuf)
                                              :inStream.read(inByteBuf);
                    inOff = 0;
                    if (inLimit <= 0) {
                        if (len == 0 || isCommentLine) {
                            return -1;
                        }
                        if (precedingBackslash) {
                            len--;
                        }
                        return len;
                    }
                }
                if (inStream != null) {
                    //下面的行等同于调用 ISO8859-1 解码器。
                    c = (char) (0xff & inByteBuf[inOff++]);
                } else {
                    c = inCharBuf[inOff++];
                }
                if (skipLF) {
                    skipLF = false;
                    if (c == '\n') {
                        continue;
                    }
                }
                if (skipWhiteSpace) {
                    if (c == ' ' || c == '\t' || c == '\f') {
                        continue;
                    }
                    if (!appendedLineBegin && (c == '\r' || c == '\n')) {
                        continue;
                    }
                    skipWhiteSpace = false;
                    appendedLineBegin = false;
                }
                if (isNewLine) {
                    isNewLine = false;
                    if (c == '#' || c == '!') {
                        isCommentLine = true;
                        continue;
                    }
                }

                if (c != '\n' && c != '\r') {
                    lineBuf[len++] = c;
                    if (len == lineBuf.length) {
                        int newLength = lineBuf.length * 2;
                        if (newLength < 0) {
                            newLength = Integer.MAX_VALUE;
                        }
                        char[] buf = new char[newLength];
                        System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
                        lineBuf = buf;
                    }
                    //翻转前导反斜杠标志
                    if (c == '\\') {
                        precedingBackslash = !precedingBackslash;
                    } else {
                        precedingBackslash = false;
                    }
                }
                else {
                    //到达行尾
                    if (isCommentLine || len == 0) {
                        isCommentLine = false;
                        isNewLine = true;
                        skipWhiteSpace = true;
                        len = 0;
                        continue;
                    }
                    if (inOff >= inLimit) {
                        inLimit = (inStream==null)
                                  ?reader.read(inCharBuf)
                                  :inStream.read(inByteBuf);
                        inOff = 0;
                        if (inLimit <= 0) {
                            if (precedingBackslash) {
                                len--;
                            }
                            return len;
                        }
                    }
                    if (precedingBackslash) {
                        len -= 1;
                        //跳过下一行中的前导空白字符
                        skipWhiteSpace = true;
                        appendedLineBegin = true;
                        precedingBackslash = false;
                        if (c == '\r') {
                            skipLF = true;
                        }
                    } else {
                        return len;
                    }
                }
            }
        }
    }


                /*
     * 将编码的 &#92;uxxxx 转换为 Unicode 字符
     * 并将特殊保存的字符转换为它们的原始形式
     */
    private String loadConvert (char[] in, int off, int len, char[] convtBuf) {
        if (convtBuf.length < len) {
            int newLen = len * 2;
            if (newLen < 0) {
                newLen = Integer.MAX_VALUE;
            }
            convtBuf = new char[newLen];
        }
        char aChar;
        char[] out = convtBuf;
        int outLen = 0;
        int end = off + len;

        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                aChar = in[off++];
                if(aChar == 'u') {
                    // 读取 xxxx
                    int value=0;
                    for (int i=0; i<4; i++) {
                        aChar = in[off++];
                        switch (aChar) {
                          case '0': case '1': case '2': case '3': case '4':
                          case '5': case '6': case '7': case '8': case '9':
                             value = (value << 4) + aChar - '0';
                             break;
                          case 'a': case 'b': case 'c':
                          case 'd': case 'e': case 'f':
                             value = (value << 4) + 10 + aChar - 'a';
                             break;
                          case 'A': case 'B': case 'C':
                          case 'D': case 'E': case 'F':
                             value = (value << 4) + 10 + aChar - 'A';
                             break;
                          default:
                              throw new IllegalArgumentException(
                                           "Malformed \\uxxxx 编码。");
                        }
                     }
                    out[outLen++] = (char)value;
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    out[outLen++] = aChar;
                }
            } else {
                out[outLen++] = aChar;
            }
        }
        return new String (out, 0, outLen);
    }

    /*
     * 将 Unicode 转换为编码的 &#92;uxxxx 并用反斜杠转义特殊字符
     */
    private String saveConvert(String theString,
                               boolean escapeSpace,
                               boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            // 先处理常见情况，选择最大的块以避免下面的特殊情况
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\'); outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch(aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                          break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                          break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                          break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                          break;
                case '=': // 穿透
                case ':': // 穿透
                case '#': // 穿透
                case '!':
                    outBuffer.append('\\'); outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode ) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >>  8) & 0xF));
                        outBuffer.append(toHex((aChar >>  4) & 0xF));
                        outBuffer.append(toHex( aChar        & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    private static void writeComments(BufferedWriter bw, String comments)
        throws IOException {
        bw.write("#");
        int len = comments.length();
        int current = 0;
        int last = 0;
        char[] uu = new char[6];
        uu[0] = '\\';
        uu[1] = 'u';
        while (current < len) {
            char c = comments.charAt(current);
            if (c > '\u00ff' || c == '\n' || c == '\r') {
                if (last != current)
                    bw.write(comments.substring(last, current));
                if (c > '\u00ff') {
                    uu[2] = toHex((c >> 12) & 0xf);
                    uu[3] = toHex((c >>  8) & 0xf);
                    uu[4] = toHex((c >>  4) & 0xf);
                    uu[5] = toHex( c        & 0xf);
                    bw.write(new String(uu));
                } else {
                    bw.newLine();
                    if (c == '\r' &&
                        current != len - 1 &&
                        comments.charAt(current + 1) == '\n') {
                        current++;
                    }
                    if (current == len - 1 ||
                        (comments.charAt(current + 1) != '#' &&
                        comments.charAt(current + 1) != '!'))
                        bw.write("#");
                }
                last = current + 1;
            }
            current++;
        }
        if (last != current)
            bw.write(comments.substring(last, current));
        bw.newLine();
    }


                /**
     * 调用 {@code store(OutputStream out, String comments)} 方法并抑制抛出的 IOException。
     *
     * @deprecated 如果在保存属性列表时发生 I/O 错误，此方法不会抛出 IOException。保存属性列表的首选方法是通过
     * {@code store(OutputStream out, String comments)} 方法或
     * {@code storeToXML(OutputStream os, String comment)} 方法。
     *
     * @param   out      一个输出流。
     * @param   comments   属性列表的描述。
     * @exception  ClassCastException  如果此 {@code Properties} 对象包含任何不是
     *             {@code Strings} 的键或值。
     */
    @Deprecated
    public void save(OutputStream out, String comments)  {
        try {
            store(out, comments);
        } catch (IOException e) {
        }
    }

    /**
     * 将此 {@code Properties} 表中的属性列表（键值对）写入输出字符流，格式适合使用
     * {@link #load(java.io.Reader) load(Reader)} 方法加载。
     * <p>
     * 此方法不会写入此 {@code Properties} 表的默认表中的属性（如果有）。
     * <p>
     * 如果 comments 参数不为 null，则首先将 ASCII {@code #} 字符、comments 字符串和行分隔符写入输出流。
     * 因此，comments 可以用作标识性注释。comments 中的换行符 ('\n')、回车符 ('\r') 或回车符后立即跟随换行符
     * 都会被 {@code Writer} 生成的行分隔符替换，并且如果 comments 中的下一个字符不是字符 {@code #} 或
     * 字符 {@code !}，则在该行分隔符后写入 ASCII {@code #}。
     * <p>
     * 接下来，总是会写入一个注释行，包含 ASCII {@code #} 字符、当前日期和时间（如同由 {@code Date} 的
     * {@code toString} 方法为当前时间生成的一样）以及由 {@code Writer} 生成的行分隔符。
     * <p>
     * 然后，此 {@code Properties} 表中的每一项都按行写入。对于每一项，先写入键字符串，然后是 ASCII {@code =}，
     * 然后是关联的元素字符串。对于键，所有空格字符都用前导的 {@code \} 字符写入。对于元素，只有前导空格字符，
     * 而不是嵌入或尾随的空格字符，用前导的 {@code \} 字符写入。键和元素字符 {@code #}、
     * {@code !}、{@code =} 和 {@code :} 用前导的反斜杠写入，以确保它们被正确加载。
     * <p>
     * 写入所有项后，刷新输出流。此方法返回后，输出流保持打开状态。
     * <p>
     *
     * @param   writer      一个输出字符流写入器。
     * @param   comments   属性列表的描述。
     * @exception  IOException 如果将此属性列表写入指定的输出流时抛出 <tt>IOException</tt>。
     * @exception  ClassCastException  如果此 {@code Properties} 对象包含任何不是 {@code Strings} 的键或值。
     * @exception  NullPointerException  如果 {@code writer} 为 null。
     * @since 1.6
     */
    public void store(Writer writer, String comments)
        throws IOException
    {
        store0((writer instanceof BufferedWriter)?(BufferedWriter)writer
                                                 : new BufferedWriter(writer),
               comments,
               false);
    }

    /**
     * 将此 {@code Properties} 表中的属性列表（键值对）写入输出流，格式适合使用
     * {@link #load(InputStream) load(InputStream)} 方法加载到 {@code Properties} 表中。
     * <p>
     * 此方法不会写入此 {@code Properties} 表的默认表中的属性（如果有）。
     * <p>
     * 此方法以与 {@link #store(java.io.Writer, java.lang.String) store(Writer)} 指定的格式相同的方式输出注释、
     * 属性键和值，但有以下不同：
     * <ul>
     * <li>流使用 ISO 8859-1 字符编码写入。
     *
     * <li>注释中不在 Latin-1 范围内的字符写为 {@code \u005Cu}<i>xxxx</i>，其中 <i>xxxx</i> 是其适当的 Unicode 十六进制值。
     *
     * <li>属性键或值中小于 {@code \u005Cu0020} 或大于 {@code \u005Cu007E} 的字符写为
     * {@code \u005Cu}<i>xxxx</i>，其中 <i>xxxx</i> 是其适当的十六进制值。
     * </ul>
     * <p>
     * 写入所有项后，刷新输出流。此方法返回后，输出流保持打开状态。
     * <p>
     * @param   out      一个输出流。
     * @param   comments   属性列表的描述。
     * @exception  IOException 如果将此属性列表写入指定的输出流时抛出 <tt>IOException</tt>。
     * @exception  ClassCastException  如果此 {@code Properties} 对象包含任何不是 {@code Strings} 的键或值。
     * @exception  NullPointerException  如果 {@code out} 为 null。
     * @since 1.2
     */
    public void store(OutputStream out, String comments)
        throws IOException
    {
        store0(new BufferedWriter(new OutputStreamWriter(out, "8859_1")),
               comments,
               true);
    }

    private void store0(BufferedWriter bw, String comments, boolean escUnicode)
        throws IOException
    {
        if (comments != null) {
            writeComments(bw, comments);
        }
        bw.write("#" + new Date().toString());
        bw.newLine();
        synchronized (this) {
            for (Enumeration<?> e = keys(); e.hasMoreElements();) {
                String key = (String)e.nextElement();
                String val = (String)get(key);
                key = saveConvert(key, true, escUnicode);
                /* 无需转义值中的嵌入和尾随空格，因此传递 false 给标志。 */
                val = saveConvert(val, false, escUnicode);
                bw.write(key + "=" + val);
                bw.newLine();
            }
        }
        bw.flush();
    }


                /**
     * 加载由指定输入流表示的 XML 文档中的所有属性到此属性表中。
     *
     * <p>XML 文档必须具有以下 DOCTYPE 声明：
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;
     * </pre>
     * 此外，文档必须满足上述属性 DTD 的要求。
     *
     * <p>实现必须能够读取使用 "{@code UTF-8}" 或 "{@code UTF-16}" 编码的 XML 文档。实现可能支持其他编码。
     *
     * <p>此方法返回后，指定的流将被关闭。
     *
     * @param in 从中读取 XML 文档的输入流。
     * @throws IOException 如果从指定输入流读取导致 <tt>IOException</tt>。
     * @throws java.io.UnsupportedEncodingException 如果可以读取文档的编码声明，并且它指定了不支持的编码。
     * @throws InvalidPropertiesFormatException 输入流中的数据不构成具有强制文档类型的有效的 XML 文档。
     * @throws NullPointerException 如果 {@code in} 为 null。
     * @see    #storeToXML(OutputStream, String, String)
     * @see    <a href="http://www.w3.org/TR/REC-xml/#charencoding">Character
     *         Encoding in Entities</a>
     * @since 1.5
     */
    public synchronized void loadFromXML(InputStream in)
        throws IOException, InvalidPropertiesFormatException
    {
        XmlSupport.load(this, Objects.requireNonNull(in));
        in.close();
    }

    /**
     * 发出表示此表中所有属性的 XML 文档。
     *
     * <p>此方法的调用形式 <tt>props.storeToXML(os, comment)</tt> 的行为与调用
     * <tt>props.storeToXML(os, comment, "UTF-8");</tt> 完全相同。
     *
     * @param os 发出 XML 文档的输出流。
     * @param comment 属性列表的描述，或如果不需要注释则为 {@code null}。
     * @throws IOException 如果写入指定输出流导致 <tt>IOException</tt>。
     * @throws NullPointerException 如果 {@code os} 为 null。
     * @throws ClassCastException  如果此 {@code Properties} 对象包含任何不是
     *         {@code Strings} 的键或值。
     * @see    #loadFromXML(InputStream)
     * @since 1.5
     */
    public void storeToXML(OutputStream os, String comment)
        throws IOException
    {
        storeToXML(os, comment, "UTF-8");
    }

    /**
     * 使用指定的编码发出表示此表中所有属性的 XML 文档。
     *
     * <p>XML 文档将具有以下 DOCTYPE 声明：
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;
     * </pre>
     *
     * <p>如果指定的注释为 {@code null}，则文档中不会存储注释。
     *
     * <p>实现必须支持写入使用 "{@code UTF-8}" 或 "{@code UTF-16}" 编码的 XML 文档。实现可能支持其他编码。
     *
     * <p>此方法返回后，指定的流保持打开状态。
     *
     * @param os        发出 XML 文档的输出流。
     * @param comment   属性列表的描述，或如果不需要注释则为 {@code null}。
     * @param  encoding 支持的 <a href="../lang/package-summary.html#charenc">
     *                  字符编码</a> 的名称。
     *
     * @throws IOException 如果写入指定输出流导致 <tt>IOException</tt>。
     * @throws java.io.UnsupportedEncodingException 如果实现不支持编码。
     * @throws NullPointerException 如果 {@code os} 为 {@code null}，
     *         或如果 {@code encoding} 为 {@code null}。
     * @throws ClassCastException  如果此 {@code Properties} 对象包含任何不是
     *         {@code Strings} 的键或值。
     * @see    #loadFromXML(InputStream)
     * @see    <a href="http://www.w3.org/TR/REC-xml/#charencoding">Character
     *         Encoding in Entities</a>
     * @since 1.5
     */
    public void storeToXML(OutputStream os, String comment, String encoding)
        throws IOException
    {
        XmlSupport.save(this, Objects.requireNonNull(os), comment,
                        Objects.requireNonNull(encoding));
    }

    /**
     * 在此属性列表中搜索具有指定键的属性。如果在此属性列表中未找到键，则递归检查默认属性列表及其默认值。如果未找到属性，方法返回
     * {@code null}。
     *
     * @param   key   属性键。
     * @return  具有指定键值的此属性列表中的值。
     * @see     #setProperty
     * @see     #defaults
     */
    public String getProperty(String key) {
        Object oval = super.get(key);
        String sval = (oval instanceof String) ? (String)oval : null;
        return ((sval == null) && (defaults != null)) ? defaults.getProperty(key) : sval;
    }

    /**
     * 在此属性列表中搜索具有指定键的属性。如果在此属性列表中未找到键，则递归检查默认属性列表及其默认值。如果未找到属性，方法返回
     * 默认值参数。
     *
     * @param   key            哈希表键。
     * @param   defaultValue   默认值。
     *
     * @return  具有指定键值的此属性列表中的值。
     * @see     #setProperty
     * @see     #defaults
     */
    public String getProperty(String key, String defaultValue) {
        String val = getProperty(key);
        return (val == null) ? defaultValue : val;
    }


                /**
     * 返回此属性列表中的所有键的枚举，
     * 包括默认属性列表中未从主属性列表中找到同名键的唯一键。
     *
     * @return  包括默认属性列表中的所有键的枚举。
     * @throws  ClassCastException 如果此属性列表中的任何键不是字符串。
     * @see     java.util.Enumeration
     * @see     java.util.Properties#defaults
     * @see     #stringPropertyNames
     */
    public Enumeration<?> propertyNames() {
        Hashtable<String,Object> h = new Hashtable<>();
        enumerate(h);
        return h.keys();
    }

    /**
     * 返回此属性列表中键及其对应值为字符串的键的集合，
     * 包括默认属性列表中未从主属性列表中找到同名键的唯一键。键或值不是
     * <tt>String</tt> 类型的属性将被省略。
     * <p>
     * 返回的集合不由 <tt>Properties</tt> 对象支持。
     * 对此 <tt>Properties</tt> 的更改不会反映在集合中，反之亦然。
     *
     * @return  键及其对应值为字符串的键的集合，
     *          包括默认属性列表中的键。
     * @see     java.util.Properties#defaults
     * @since   1.6
     */
    public Set<String> stringPropertyNames() {
        Hashtable<String, String> h = new Hashtable<>();
        enumerateStringProperties(h);
        return h.keySet();
    }

    /**
     * 将此属性列表打印到指定的输出流。
     * 此方法适用于调试。
     *
     * @param   out   一个输出流。
     * @throws  ClassCastException 如果此属性列表中的任何键不是字符串。
     */
    public void list(PrintStream out) {
        out.println("-- listing properties --");
        Hashtable<String,Object> h = new Hashtable<>();
        enumerate(h);
        for (Enumeration<String> e = h.keys() ; e.hasMoreElements() ;) {
            String key = e.nextElement();
            String val = (String)h.get(key);
            if (val.length() > 40) {
                val = val.substring(0, 37) + "...";
            }
            out.println(key + "=" + val);
        }
    }

    /**
     * 将此属性列表打印到指定的输出流。
     * 此方法适用于调试。
     *
     * @param   out   一个输出流。
     * @throws  ClassCastException 如果此属性列表中的任何键不是字符串。
     * @since   JDK1.1
     */
    /*
     * 为了确保非 1.1 编译器可以编译此文件，而不是使用匿名内部类来共享通用代码，
     * 此方法被复制。
     */
    public void list(PrintWriter out) {
        out.println("-- listing properties --");
        Hashtable<String,Object> h = new Hashtable<>();
        enumerate(h);
        for (Enumeration<String> e = h.keys() ; e.hasMoreElements() ;) {
            String key = e.nextElement();
            String val = (String)h.get(key);
            if (val.length() > 40) {
                val = val.substring(0, 37) + "...";
            }
            out.println(key + "=" + val);
        }
    }

    /**
     * 枚举指定哈希表中的所有键/值对。
     * @param h 哈希表
     * @throws ClassCastException 如果任何属性键不是字符串类型。
     */
    private synchronized void enumerate(Hashtable<String,Object> h) {
        if (defaults != null) {
            defaults.enumerate(h);
        }
        for (Enumeration<?> e = keys() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            h.put(key, get(key));
        }
    }

    /**
     * 枚举指定哈希表中的所有键/值对
     * 并省略键或值不是字符串的属性。
     * @param h 哈希表
     */
    private synchronized void enumerateStringProperties(Hashtable<String, String> h) {
        if (defaults != null) {
            defaults.enumerateStringProperties(h);
        }
        for (Enumeration<?> e = keys() ; e.hasMoreElements() ;) {
            Object k = e.nextElement();
            Object v = get(k);
            if (k instanceof String && v instanceof String) {
                h.put((String) k, (String) v);
            }
        }
    }

    /**
     * 将一个半字节转换为十六进制字符
     * @param   nibble  要转换的半字节。
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /** 十六进制数字表 */
    private static final char[] hexDigit = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    /**
     * 用于以 XML 格式加载/存储属性的支持类。
     *
     * <p> 这里定义的 {@code load} 和 {@code store} 方法委托给一个系统范围的 {@code XmlPropertiesProvider}。在首次调用任一方法时，系统范围的提供者将按以下方式定位： </p>
     *
     * <ol>
     *   <li> 如果系统属性 {@code sun.util.spi.XmlPropertiesProvider}
     *   已定义，则将其视为具体提供者类的全限定名。该类将使用系统类加载器作为启动加载器加载。如果无法加载或使用零参数构造函数实例化，则会抛出未指定的错误。 </li>
     *
     *   <li> 如果未定义系统属性，则使用 {@link ServiceLoader} 类定义的服务提供者加载设施来定位提供者，系统类加载器作为启动加载器，{@code sun.util.spi.XmlPropertiesProvider} 作为服务类型。如果此过程失败，则会抛出未指定的错误。如果有多个服务提供者安装，则不指定使用哪个提供者。 </li>
     *
     *   <li> 如果上述方法找不到提供者，则将实例化并使用系统默认提供者。 </li>
     * </ol>
     */
    private static class XmlSupport {


/**
 * 从系统属性加载 XmlPropertiesProvider。
 */
private static XmlPropertiesProvider loadProviderFromProperty(ClassLoader cl) {
    String cn = System.getProperty("sun.util.spi.XmlPropertiesProvider");
    if (cn == null)
        return null;
    try {
        Class<?> c = Class.forName(cn, true, cl);
        return (XmlPropertiesProvider)c.newInstance();
    } catch (ClassNotFoundException |
             IllegalAccessException |
             InstantiationException x) {
        throw new ServiceConfigurationError(null, x);
    }
}

/**
 * 作为服务加载 XmlPropertiesProvider。
 */
private static XmlPropertiesProvider loadProviderAsService(ClassLoader cl) {
    Iterator<XmlPropertiesProvider> iterator =
         ServiceLoader.load(XmlPropertiesProvider.class, cl).iterator();
    return iterator.hasNext() ? iterator.next() : null;
}

/**
 * 加载 XmlPropertiesProvider。
 */
private static XmlPropertiesProvider loadProvider() {
    return AccessController.doPrivileged(
        new PrivilegedAction<XmlPropertiesProvider>() {
            public XmlPropertiesProvider run() {
                ClassLoader cl = ClassLoader.getSystemClassLoader();
                XmlPropertiesProvider provider = loadProviderFromProperty(cl);
                if (provider != null)
                    return provider;
                provider = loadProviderAsService(cl);
                if (provider != null)
                    return provider;
                return new jdk.internal.util.xml.BasicXmlPropertiesProvider();
        }});
}

/**
 * 加载的 XmlPropertiesProvider 实例。
 */
private static final XmlPropertiesProvider PROVIDER = loadProvider();

/**
 * 从输入流加载属性。
 */
static void load(Properties props, InputStream in)
    throws IOException, InvalidPropertiesFormatException
{
    PROVIDER.load(props, in);
}

/**
 * 将属性保存到输出流。
 */
static void save(Properties props, OutputStream os, String comment,
                 String encoding)
    throws IOException
{
    PROVIDER.store(props, os, comment, encoding);
}
}
