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

package java.net;

import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.CharArrayWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException ;
import java.util.BitSet;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetPropertyAction;

/**
 * HTML 表单编码的工具类。此类包含将字符串转换为 <CODE>application/x-www-form-urlencoded</CODE> MIME
 * 格式的静态方法。有关 HTML 表单编码的更多信息，请参阅 HTML
 * <A HREF="http://www.w3.org/TR/html4/">规范</A>。
 *
 * <p>
 * 当编码字符串时，应用以下规则：
 *
 * <ul>
 * <li>字符 “{@code a}” 到 “{@code z}”，“{@code A}” 到 “{@code Z}” 和 “{@code 0}”
 *     到 “{@code 9}” 保持不变。
 * <li>特殊字符 “{@code .}”，“{@code -}”，“{@code *}” 和
 *     “{@code _}” 保持不变。
 * <li>空格字符 “ &nbsp; ” 转换为加号 “{@code +}”。
 * <li>所有其他字符都是不安全的，首先使用某种编码方案将其转换为一个或多个字节。然后每个字节
 *     由 3 个字符的字符串
 *     &quot;<i>{@code %xy}</i>&quot; 表示，其中 <i>xy</i> 是
 *     该字节的两位十六进制表示。
 *     推荐使用的编码方案是 UTF-8。然而，为了兼容性，如果没有指定编码方案，
 *     则使用平台的默认编码。
 * </ul>
 *
 * <p>
 * 例如，使用 UTF-8 作为编码方案，字符串 “The
 * string &#252;@foo-bar” 将被转换为
 * “The+string+%C3%BC%40foo-bar”，因为在 UTF-8 中字符
 * &#252; 被编码为两个字节 C3（十六进制）和 BC（十六进制），字符 @ 被编码为一个字节 40（十六进制）。
 *
 * @author  Herb Jellinek
 * @since   JDK1.0
 */
public class URLEncoder {
    static BitSet dontNeedEncoding;
    static final int caseDiff = ('a' - 'A');
    static String dfltEncName = null;

    static {

        /* 不需要编码的字符列表如下：
         *
         * RFC 2396 指出：
         * -----
         * 在 URI 中允许但没有保留用途的数据字符称为未保留字符。这些包括大写和小写字母、十进制数字和一组有限的标点符号和符号。
         *
         * 未保留  = 字母数字 | 标记
         *
         * 标记        = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
         *
         * 未保留字符可以被转义而不改变 URI 的语义，但除非 URI 用于不允许未转义字符出现的上下文中，否则不应这样做。
         * -----
         *
         * 看起来 Netscape 和 Internet Explorer 都会转义此列表中的所有特殊字符，除了
         * “-”，“_”，“.” 和 “*”。虽然不清楚为什么它们会转义其他字符，但最安全的假设是，在某些情况下，如果不转义这些字符可能会不安全。因此，我们将使用相同的列表。值得注意的是，这与 O'Reilly 的 “HTML: The Definitive Guide”（第 164 页）一致。
         *
         * 最后，Internet Explorer 不会编码 “@” 字符，这显然不是 RFC 中的未保留字符。在这一点上，我们遵循 RFC 的规定，与 Netscape 保持一致。
         *
         */

        dontNeedEncoding = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = '0'; i <= '9'; i++) {
            dontNeedEncoding.set(i);
        }
        dontNeedEncoding.set(' '); /* 在 encode() 方法中将空格编码为 + */
        dontNeedEncoding.set('-');
        dontNeedEncoding.set('_');
        dontNeedEncoding.set('.');
        dontNeedEncoding.set('*');

        dfltEncName = AccessController.doPrivileged(
            new GetPropertyAction("file.encoding")
        );
    }

    /**
     * 你不能调用构造函数。
     */
    private URLEncoder() { }

    /**
     * 将字符串转换为 {@code x-www-form-urlencoded}
     * 格式。此方法使用平台的默认编码作为编码方案来获取不安全字符的字节。
     *
     * @param   s   要转换的 {@code String}。
     * @deprecated 转换后的字符串可能因平台的默认编码而异。相反，使用 encode(String,String)
     *             方法来指定编码。
     * @return  转换后的 {@code String}。
     */
    @Deprecated
    public static String encode(String s) {

        String str = null;

        try {
            str = encode(s, dfltEncName);
        } catch (UnsupportedEncodingException e) {
            // 系统应始终具有平台默认编码
        }

        return str;
    }

    /**
     * 使用特定的编码方案将字符串转换为 {@code application/x-www-form-urlencoded}
     * 格式。此方法使用提供的编码方案来获取不安全字符的字节。
     * <p>
     * <em><strong>注意：</strong> <a href=
     * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
     * 万维网联盟建议</a> 指出应使用 UTF-8。不这样做可能会引入
     * 兼容性问题。</em>
     *
     * @param   s   要转换的 {@code String}。
     * @param   enc   支持的
     *    <a href="../lang/package-summary.html#charenc">字符
     *    编码</a> 的名称。
     * @return  转换后的 {@code String}。
     * @exception  UnsupportedEncodingException
     *             如果命名的编码不受支持
     * @see URLDecoder#decode(java.lang.String, java.lang.String)
     * @since 1.4
     */
    public static String encode(String s, String enc)
        throws UnsupportedEncodingException {

        boolean needToChange = false;
        StringBuffer out = new StringBuffer(s.length());
        Charset charset;
        CharArrayWriter charArrayWriter = new CharArrayWriter();

        if (enc == null)
            throw new NullPointerException("charsetName");

        try {
            charset = Charset.forName(enc);
        } catch (IllegalCharsetNameException e) {
            throw new UnsupportedEncodingException(enc);
        } catch (UnsupportedCharsetException e) {
            throw new UnsupportedEncodingException(enc);
        }

        for (int i = 0; i < s.length();) {
            int c = (int) s.charAt(i);
            //System.out.println("Examining character: " + c);
            if (dontNeedEncoding.get(c)) {
                if (c == ' ') {
                    c = '+';
                    needToChange = true;
                }
                //System.out.println("Storing: " + c);
                out.append((char)c);
                i++;
            } else {
                // 转换为外部编码，然后再进行十六进制转换
                do {
                    charArrayWriter.write(c);
                    /*
                     * 如果此字符表示 Unicode 替代对的开始，则传递两个字符。目前尚不清楚
                     * 如果在不合法的替代对中出现保留的字节应如何处理。目前，将其视为任何其他字符处理。
                     */
                    if (c >= 0xD800 && c <= 0xDBFF) {
                        /*
                          System.out.println(Integer.toHexString(c)
                          + " is high surrogate");
                        */
                        if ( (i+1) < s.length()) {
                            int d = (int) s.charAt(i+1);
                            /*
                              System.out.println("\tExamining "
                              + Integer.toHexString(d));
                            */
                            if (d >= 0xDC00 && d <= 0xDFFF) {
                                /*
                                  System.out.println("\t"
                                  + Integer.toHexString(d)
                                  + " is low surrogate");
                                */
                                charArrayWriter.write(d);
                                i++;
                            }
                        }
                    }
                    i++;
                } while (i < s.length() && !dontNeedEncoding.get((c = (int) s.charAt(i))));

                charArrayWriter.flush();
                String str = new String(charArrayWriter.toCharArray());
                byte[] ba = str.getBytes(charset);
                for (int j = 0; j < ba.length; j++) {
                    out.append('%');
                    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
                    // 如果 ch 是字母，则将其转换为大写字母作为十六进制值的一部分
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    out.append(ch);
                    ch = Character.forDigit(ba[j] & 0xF, 16);
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    out.append(ch);
                }
                charArrayWriter.reset();
                needToChange = true;
            }
        }

        return (needToChange? out.toString() : s);
    }
}
