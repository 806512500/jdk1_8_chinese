
/*
 * 版权所有 (c) 1995, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.net;

import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.CharArrayWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.BitSet;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetPropertyAction;

/**
 * HTML 表单编码的实用类。此类包含用于将 String 转换为 <CODE>application/x-www-form-urlencoded</CODE> MIME
 * 格式的静态方法。有关 HTML 表单编码的更多信息，请参阅 HTML
 * <A HREF="http://www.w3.org/TR/html4/">规范</A>。
 *
 * <p>
 * 当编码一个 String 时，应用以下规则：
 *
 * <ul>
 * <li>字母数字字符 &quot;{@code a}&quot; 到
 *     &quot;{@code z}&quot;，&quot;{@code A}&quot; 到
 *     &quot;{@code Z}&quot; 和 &quot;{@code 0}&quot;
 *     到 &quot;{@code 9}&quot; 保持不变。
 * <li>特殊字符 &quot;{@code .}&quot;，
 *     &quot;{@code -}&quot;，&quot;{@code *}&quot; 和
 *     &quot;{@code _}&quot; 保持不变。
 * <li>空格字符 &quot; &nbsp; &quot; 转换为加号 &quot;{@code +}&quot;。
 * <li>所有其他字符都是不安全的，首先使用某种编码方案将其转换为一个或多个字节。然后每个字节表示为
 *     3 个字符的字符串
 *     &quot;<i>{@code %xy}</i>&quot;，其中 <i>xy</i> 是
 *     字节的两位十六进制表示。
 *     推荐使用的编码方案是 UTF-8。但是，出于兼容性原因，如果未指定编码，
 *     则使用平台的默认编码。
 * </ul>
 *
 * <p>
 * 例如，使用 UTF-8 作为编码方案，字符串 &quot;The
 * string &#252;@foo-bar&quot; 将转换为
 * &quot;The+string+%C3%BC%40foo-bar&quot;，因为在 UTF-8 中字符
 * &#252; 编码为两个字节 C3 (十六进制) 和 BC (十六进制)，而字符 @ 编码为一个字节 40 (十六进制)。
 *
 * @author  Herb Jellinek
 * @since   JDK1.0
 */
public class URLEncoder {
    static BitSet dontNeedEncoding;
    static final int caseDiff = ('a' - 'A');
    static String dfltEncName = null;

    static {

        /* 不需要编码的字符列表已按以下方式确定：
         *
         * RFC 2396 指出：
         * -----
         * 在 URI 中允许但没有保留用途的数据字符称为未保留字符。这些包括大写和小写字母、十进制数字和一组有限的标点符号和符号。
         *
         * unreserved  = alphanum | mark
         *
         * mark        = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
         *
         * 未保留字符可以转义而不会改变 URI 的语义，但除非 URI 用于不允许未转义字符出现的上下文中，否则不应这样做。
         * -----
         *
         * 看起来 Netscape 和 Internet Explorer 都会转义此列表中的所有特殊字符，除了
         * "-", "_", ".", "*"。虽然不清楚为什么它们会转义其他字符，但最安全的做法可能是假设如果不转义，其他字符在某些上下文中可能是不安全的。因此，我们将使用相同的列表。值得注意的是，这与 O'Reilly 的“HTML: The Definitive Guide”（第 164 页）一致。
         *
         * 最后，Internet Explorer 不编码字符 "@"，这显然不是 RFC 中的未保留字符。在这点上，我们遵循 RFC 的规定，Netscape 也是如此。
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
     * 不能调用构造函数。
     */
    private URLEncoder() { }

    /**
     * 将字符串转换为 {@code x-www-form-urlencoded}
     * 格式。此方法使用平台的默认编码作为编码方案来获取不安全字符的字节。
     *
     * @param   s   要转换的 {@code String}。
     * @deprecated 结果字符串可能因平台的默认编码而异。相反，使用 encode(String,String)
     *             方法来指定编码。
     * @return  转换后的 {@code String}。
     */
    @Deprecated
    public static String encode(String s) {

        String str = null;

        try {
            str = encode(s, dfltEncName);
        } catch (UnsupportedEncodingException e) {
            // 系统应该始终具有平台默认编码
        }

        return str;
    }

    /**
     * 使用特定的编码方案将字符串转换为 {@code application/x-www-form-urlencoded}
     * 格式。此方法使用提供的编码方案来获取不安全字符的字节。
     * <p>
     * <em><strong>注意：</strong> <a href=
     * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
     * 万维网联盟建议</a> 指出应该使用 UTF-8。不这样做可能会引入
     * 不兼容性。</em>
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
            // 输出正在检查的字符（调试用）
            if (dontNeedEncoding.get(c)) {
                if (c == ' ') {
                    c = '+';
                    needToChange = true;
                }
                // 输出存储的字符（调试用）
                out.append((char)c);
                i++;
            } else {
                // 在进行十六进制转换之前，先转换为外部编码
                do {
                    charArrayWriter.write(c);
                    /*
                     * 如果这个字符代表一个Unicode代理对的开始，那么传递两个字符。如果在合法的代理对之外出现了一个保留的代理对范围的字节，目前的做法是将其视为任何其他字符。
                     */
                    if (c >= 0xD800 && c <= 0xDBFF) {
                        /*
                          输出高代理字符的十六进制值（调试用）
                        */
                        if ( (i+1) < s.length()) {
                            int d = (int) s.charAt(i+1);
                            /*
                              输出正在检查的字符的十六进制值（调试用）
                            */
                            if (d >= 0xDC00 && d <= 0xDFFF) {
                                /*
                                  输出低代理字符的十六进制值（调试用）
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
                    // 如果ch是一个字母，转换为大写
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
