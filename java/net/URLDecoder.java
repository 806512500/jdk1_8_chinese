/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;

/**
 * HTML 表单解码工具类。此类包含用于从 <CODE>application/x-www-form-urlencoded</CODE>
 * MIME 格式解码字符串的静态方法。
 * <p>
 * 转换过程是 URLEncoder 类使用的转换过程的逆过程。假设编码字符串中的所有字符都是以下字符之一：
 * &quot;{@code a}&quot; 通过 &quot;{@code z}&quot;，
 * &quot;{@code A}&quot; 通过 &quot;{@code Z}&quot;，
 * &quot;{@code 0}&quot; 通过 &quot;{@code 9}&quot;，以及
 * &quot;{@code -}&quot;，&quot;{@code _}&quot;，
 * &quot;{@code .}&quot; 和 &quot;{@code *}&quot;。字符
 * &quot;{@code %}&quot; 允许但被视为特殊转义序列的开始。
 * <p>
 * 转换过程中应用以下规则：
 *
 * <ul>
 * <li>字符 &quot;{@code a}&quot; 通过
 *     &quot;{@code z}&quot;，&quot;{@code A}&quot; 通过
 *     &quot;{@code Z}&quot; 和 &quot;{@code 0}&quot;
 *     通过 &quot;{@code 9}&quot; 保持不变。
 * <li>特殊字符 &quot;{@code .}&quot;，
 *     &quot;{@code -}&quot;，&quot;{@code *}&quot; 和
 *     &quot;{@code _}&quot; 保持不变。
 * <li>加号 &quot;{@code +}&quot; 转换为空格字符 &quot; &nbsp; &quot;。
 * <li>形式为 "<i>{@code %xy}</i>" 的序列将被视为表示一个字节，其中 <i>xy</i> 是两位的
 *     十六进制表示形式。然后，包含一个或多个这些字节序列的连续子字符串将被替换为编码结果为这些连续字节的字符。
 *     可以指定用于解码这些字符的编码方案，或者如果不指定，则使用平台的默认编码。
 * </ul>
 * <p>
 * 解码器可以有两种处理非法字符串的方式。它可以保留非法字符或抛出一个 {@link java.lang.IllegalArgumentException}。
 * 解码器采取哪种方法由实现决定。
 *
 * @author  Mark Chamness
 * @author  Michael McCloskey
 * @since   1.2
 */

public class URLDecoder {

    // 平台默认编码
    static String dfltEncName = URLEncoder.dfltEncName;

    /**
     * 解码一个 {@code x-www-form-urlencoded} 字符串。
     * 使用平台的默认编码来确定形式为 "<i>{@code %xy}</i>" 的连续序列表示的字符。
     * @param s 要解码的 {@code String}
     * @deprecated 结果字符串可能因平台的默认编码而异。相反，使用 decode(String,String) 方法指定编码。
     * @return 新解码的 {@code String}
     */
    @Deprecated
    public static String decode(String s) {

        String str = null;

        try {
            str = decode(s, dfltEncName);
        } catch (UnsupportedEncodingException e) {
            // 系统应始终具有平台默认编码
        }

        return str;
    }

    /**
     * 使用特定的编码方案解码一个 {@code application/x-www-form-urlencoded} 字符串。
     * 使用提供的编码来确定形式为 "<i>{@code %xy}</i>" 的连续序列表示的字符。
     * <p>
     * <em><strong>注意：</strong> 世界万维网联盟建议使用
     * <a href="http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
     * UTF-8</a>。不这样做可能会引入不兼容性。</em>
     *
     * @param s 要解码的 {@code String}
     * @param enc   支持的
     *    <a href="../lang/package-summary.html#charenc">字符
     *    编码</a> 的名称。
     * @return 新解码的 {@code String}
     * @exception  UnsupportedEncodingException
     *             如果需要咨询字符编码，但命名的字符编码不受支持
     * @see URLEncoder#encode(java.lang.String, java.lang.String)
     * @since 1.4
     */
    public static String decode(String s, String enc)
        throws UnsupportedEncodingException{

        boolean needToChange = false;
        int numChars = s.length();
        StringBuffer sb = new StringBuffer(numChars > 500 ? numChars / 2 : numChars);
        int i = 0;

        if (enc.length() == 0) {
            throw new UnsupportedEncodingException ("URLDecoder: empty string enc parameter");
        }

        char c;
        byte[] bytes = null;
        while (i < numChars) {
            c = s.charAt(i);
            switch (c) {
            case '+':
                sb.append(' ');
                i++;
                needToChange = true;
                break;
            case '%':
                /*
                 * 从这个 % 实例开始，处理所有形式为 %xy 的连续子字符串。每个
                 * 子字符串 %xy 将生成一个字节。将通过这种方式获得的所有连续字节
                 * 转换为提供的编码中表示的字符。
                 */

                try {

                    // (numChars-i)/3 是剩余字节数的上限
                    if (bytes == null)
                        bytes = new byte[(numChars-i)/3];
                    int pos = 0;

                    while ( ((i+2) < numChars) &&
                            (c=='%')) {
                        int v = Integer.parseInt(s.substring(i+1,i+3),16);
                        if (v < 0)
                            throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - negative value");
                        bytes[pos++] = (byte) v;
                        i+= 3;
                        if (i < numChars)
                            c = s.charAt(i);
                    }

                    // 一个尾部不完整的字节编码，如 "%x" 将导致抛出异常

                    if ((i < numChars) && (c=='%'))
                        throw new IllegalArgumentException(
                         "URLDecoder: Incomplete trailing escape (%) pattern");

                    sb.append(new String(bytes, 0, pos, enc));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                    "URLDecoder: Illegal hex characters in escape (%) pattern - "
                    + e.getMessage());
                }
                needToChange = true;
                break;
            default:
                sb.append(c);
                i++;
                break;
            }
        }

        return (needToChange? sb.toString() : s);
    }
}
