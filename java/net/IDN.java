
/*
 * 版权所有 (c) 2005, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.InputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import sun.net.idn.StringPrep;
import sun.net.idn.Punycode;
import sun.text.normalizer.UCharacterIterator;

/**
 * 提供方法将国际化域名 (IDNs) 在常规 Unicode 表示和 ASCII 兼容编码 (ACE) 表示之间进行转换。
 * 国际化域名可以使用整个 Unicode 范围内的字符，而传统域名则限制为 ASCII 字符。
 * ACE 是一种使用仅 ASCII 字符编码 Unicode 字符串的方法，可以与仅理解传统域名的软件（如域名系统）一起使用。
 *
 * <p>国际化域名在 <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a> 中定义。
 * RFC 3490 定义了两种操作：ToASCII 和 ToUnicode。这两种操作使用
 * <a href="http://www.ietf.org/rfc/rfc3491.txt">Nameprep</a> 算法，该算法是 <a href="http://www.ietf.org/rfc/rfc3454.txt">Stringprep</a> 的一个配置文件，以及
 * <a href="http://www.ietf.org/rfc/rfc3492.txt">Punycode</a> 算法来转换域名字符串。
 *
 * <p>上述转换过程的行为可以通过各种标志进行调整：
 *   <ul>
 *     <li>如果使用 ALLOW_UNASSIGNED 标志，要转换的域名字符串可以包含在 Unicode 3.2 中未分配的代码点，这是 IDN 转换所基于的 Unicode 版本。如果不使用该标志，则存在此类未分配的代码点被视为错误。
 *     <li>如果使用 USE_STD3_ASCII_RULES 标志，ASCII 字符串将根据 <a href="http://www.ietf.org/rfc/rfc1122.txt">RFC 1122</a> 和 <a href="http://www.ietf.org/rfc/rfc1123.txt">RFC 1123</a> 进行检查。
 *         如果它们不符合要求，则视为错误。
 *   </ul>
 * 这些标志可以逻辑或在一起。
 *
 * <p>关于国际化域名支持的安全考虑非常重要。例如，英文域名可能会被<i>同形异义词</i>恶意拼写错误，即用非拉丁字母替换。
 * <a href="http://www.unicode.org/reports/tr36/">Unicode 技术报告 #36</a> 讨论了 IDN 支持的安全问题以及可能的解决方案。
 * 应用程序在使用国际化域名时负责采取适当的安全措施。
 *
 * @author Edward Wang
 * @since 1.6
 *
 */
public final class IDN {
    /**
     * 允许处理未分配代码点的标志
     */
    public static final int ALLOW_UNASSIGNED = 0x01;

    /**
     * 打开对 STD-3 ASCII 规则的检查的标志
     */
    public static final int USE_STD3_ASCII_RULES = 0x02;


    /**
     * 将字符串从 Unicode 转换为 ASCII 兼容编码 (ACE)，如 <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a> 中定义的 ToASCII 操作。
     *
     * <p>ToASCII 操作可能会失败。如果 ToASCII 的任何步骤失败，则 ToASCII 失败。
     * 如果 ToASCII 操作失败，将抛出 IllegalArgumentException。在这种情况下，输入字符串不应在国际化域名中使用。
     *
     * <p> 标签是域名的一个独立部分。原始的 ToASCII 操作，如 RFC 3490 中定义的，仅对单个标签进行操作。此方法可以处理
     * 标签和整个域名，假设域名中的标签总是由点分隔。以下字符被识别为点：
     * &#0092;u002E (全停)，&#0092;u3002 (意象全停)，&#0092;uFF0E (全宽全停)，
     * 和 &#0092;uFF61 (半宽意象全停)。如果点用作标签分隔符，此方法还将输出转换字符串中的所有点更改为 &#0092;u002E (全停)。
     *
     * @param input     要处理的字符串
     * @param flag      处理标志；可以是 0 或可能的标志的任何逻辑或
     *
     * @return          转换后的 {@code String}
     *
     * @throws IllegalArgumentException   如果输入字符串不符合 RFC 3490 规范
     */
    public static String toASCII(String input, int flag)
    {
        int p = 0, q = 0;
        StringBuffer out = new StringBuffer();

        if (isRootLabel(input)) {
            return ".";
        }

        while (p < input.length()) {
            q = searchDots(input, p);
            out.append(toASCIIInternal(input.substring(p, q),  flag));
            if (q != (input.length())) {
               // 有更多标签，或保留当前的尾随点
               out.append('.');
            }
            p = q + 1;
        }

        return out.toString();
    }


    /**
     * 将字符串从 Unicode 转换为 ASCII 兼容编码 (ACE)，如 <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a> 中定义的 ToASCII 操作。
     *
     * <p> 此便捷方法的行为就像调用了两个参数的对应方法一样：
     * <blockquote>
     * {@link #toASCII(String, int) toASCII}(input,&nbsp;0);
     * </blockquote>
     *
     * @param input     要处理的字符串
     *
     * @return          转换后的 {@code String}
     *
     * @throws IllegalArgumentException   如果输入字符串不符合 RFC 3490 规范
     */
    public static String toASCII(String input) {
        return toASCII(input, 0);
    }


    /**
     * 将字符串从 ASCII 兼容编码 (ACE) 转换为 Unicode，如 <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a> 中定义的 ToUnicode 操作。
     *
     * <p>ToUnicode 从不失败。在发生任何错误的情况下，输入字符串将不加修改地返回。
     *
     * <p> 标签是域名的一个独立部分。原始的 ToUnicode 操作，如 RFC 3490 中定义的，仅对单个标签进行操作。此方法可以处理
     * 标签和整个域名，假设域名中的标签总是由点分隔。以下字符被识别为点：
     * &#0092;u002E (全停)，&#0092;u3002 (意象全停)，&#0092;uFF0E (全宽全停)，
     * 和 &#0092;uFF61 (半宽意象全停)。
     *
     * @param input     要处理的字符串
     * @param flag      处理标志；可以是 0 或可能的标志的任何逻辑或
     *
     * @return          转换后的 {@code String}
     */
    public static String toUnicode(String input, int flag) {
        int p = 0, q = 0;
        StringBuffer out = new StringBuffer();


                    if (isRootLabel(input)) {
            return ".";
        }

        while (p < input.length()) {
            q = searchDots(input, p);
            out.append(toUnicodeInternal(input.substring(p, q),  flag));
            if (q != (input.length())) {
               // has more labels, or keep the trailing dot as at present
               out.append('.');
            }
            p = q + 1;
        }

        return out.toString();
    }


    /**
     * 将字符串从ASCII兼容编码（ACE）转换为Unicode，
     * 如<a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a>中定义的ToUnicode操作。
     *
     * <p>此便捷方法的工作方式类似于调用以下两个参数的方法：
     * <blockquote>
     * {@link #toUnicode(String, int) toUnicode}(input,&nbsp;0);
     * </blockquote>
     *
     * @param input     要处理的字符串
     *
     * @return          转换后的 {@code String}
     */
    public static String toUnicode(String input) {
        return toUnicode(input, 0);
    }


    /* ---------------- 私有成员 -------------- */

    // ACE 前缀是 "xn--"
    private static final String ACE_PREFIX = "xn--";
    private static final int ACE_PREFIX_LENGTH = ACE_PREFIX.length();

    private static final int MAX_LABEL_LENGTH   = 63;

    // 单个 nameprep 实例
    private static StringPrep namePrep = null;

    static {
        InputStream stream = null;

        try {
            final String IDN_PROFILE = "uidna.spp";
            if (System.getSecurityManager() != null) {
                stream = AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
                    public InputStream run() {
                        return StringPrep.class.getResourceAsStream(IDN_PROFILE);
                    }
                });
            } else {
                stream = StringPrep.class.getResourceAsStream(IDN_PROFILE);
            }

            namePrep = new StringPrep(stream);
            stream.close();
        } catch (IOException e) {
            // 不应该到达这里
            assert false;
        }
    }


    /* ---------------- 私有操作 -------------- */


    //
    // 抑制默认的无参数构造函数
    //
    private IDN() {}

    //
    // toASCII 操作；仅适用于单个标签
    //
    private static String toASCIIInternal(String label, int flag)
    {
        // 步骤 1
        // 检查字符串是否包含ASCII范围0..0x7c之外的代码点。
        boolean isASCII  = isAllASCII(label);
        StringBuffer dest;

        // 步骤 2
        // 执行 nameprep 操作；这里使用 ALLOW_UNASSIGNED 标志
        if (!isASCII) {
            UCharacterIterator iter = UCharacterIterator.getInstance(label);
            try {
                dest = namePrep.prepare(iter, flag);
            } catch (java.text.ParseException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            dest = new StringBuffer(label);
        }

        // 步骤 8，向前移动以检查代码点的最小数量
        // 长度必须在1..63之间
        if (dest.length() == 0) {
            throw new IllegalArgumentException(
                        "空标签不是合法名称");
        }

        // 步骤 3
        // 验证不存在非LDH ASCII代码点
        //   0..0x2c, 0x2e..0x2f, 0x3a..0x40, 0x5b..0x60, 0x7b..0x7f
        // 验证不存在前导和尾随连字符
        boolean useSTD3ASCIIRules = ((flag & USE_STD3_ASCII_RULES) != 0);
        if (useSTD3ASCIIRules) {
            for (int i = 0; i < dest.length(); i++) {
                int c = dest.charAt(i);
                if (isNonLDHAsciiCodePoint(c)) {
                    throw new IllegalArgumentException(
                        "包含非LDH ASCII字符");
                }
            }

            if (dest.charAt(0) == '-' ||
                dest.charAt(dest.length() - 1) == '-') {

                throw new IllegalArgumentException(
                        "有前导或尾随连字符");
            }
        }

        if (!isASCII) {
            // 步骤 4
            // 如果所有代码点都在0..0x7f范围内，跳到步骤8
            if (!isAllASCII(dest.toString())) {
                // 步骤 5
                // 验证序列不以ACE前缀开头
                if(!startsWithACEPrefix(dest)){

                    // 步骤 6
                    // 使用punycode编码序列
                    try {
                        dest = Punycode.encode(dest, null);
                    } catch (java.text.ParseException e) {
                        throw new IllegalArgumentException(e);
                    }

                    dest = toASCIILower(dest);

                    // 步骤 7
                    // 在前面加上ACE前缀
                    dest.insert(0, ACE_PREFIX);
                } else {
                    throw new IllegalArgumentException("输入以ACE前缀开头");
                }

            }
        }

        // 步骤 8
        // 长度必须在1..63之间
        if (dest.length() > MAX_LABEL_LENGTH) {
            throw new IllegalArgumentException("输入中的标签太长");
        }

        return dest.toString();
    }

    //
    // toUnicode 操作；仅适用于单个标签
    //
    private static String toUnicodeInternal(String label, int flag) {
        boolean[] caseFlags = null;
        StringBuffer dest;

        // 步骤 1
        // 查找输入中的所有代码点是否为ASCII
        boolean isASCII = isAllASCII(label);

        if(!isASCII){
            // 步骤 2
            // 执行 nameprep 操作；这里使用 ALLOW_UNASSIGNED 标志
            try {
                UCharacterIterator iter = UCharacterIterator.getInstance(label);
                dest = namePrep.prepare(iter, flag);
            } catch (Exception e) {
                // toUnicode 永远不会失败；如果任何步骤失败，返回输入字符串
                return label;
            }
        } else {
            dest = new StringBuffer(label);
        }


                    // 第 3 步
        // 验证 ACE 前缀
        if(startsWithACEPrefix(dest)) {

            // 第 4 步
            // 移除 ACE 前缀
            String temp = dest.substring(ACE_PREFIX_LENGTH, dest.length());

            try {
                // 第 5 步
                // 使用 punycode 解码
                StringBuffer decodeOut = Punycode.decode(new StringBuffer(temp), null);

                // 第 6 步
                // 应用 toASCII
                String toASCIIOut = toASCII(decodeOut.toString(), flag);

                // 第 7 步
                // 验证
                if (toASCIIOut.equalsIgnoreCase(dest.toString())) {
                    // 第 8 步
                    // 返回第 5 步的输出
                    return decodeOut.toString();
                }
            } catch (Exception ignored) {
                // 无操作
            }
        }

        // 直接返回输入
        return label;
    }


    //
    // LDH 代表 "字母/数字/连字符"，字符限制为 26 个拉丁字母 <A-Z a-z>，数字 <0-9>，和连字符
    // <->。
    // 非 LDH 指的是 ASCII 范围内的字符，但不是字母、数字或连字符。
    //
    // 非 LDH = 0..0x2C, 0x2E..0x2F, 0x3A..0x40, 0x5B..0x60, 0x7B..0x7F
    //
    private static boolean isNonLDHAsciiCodePoint(int ch){
        return (0x0000 <= ch && ch <= 0x002C) ||
               (0x002E <= ch && ch <= 0x002F) ||
               (0x003A <= ch && ch <= 0x0040) ||
               (0x005B <= ch && ch <= 0x0060) ||
               (0x007B <= ch && ch <= 0x007F);
    }

    //
    // 在字符串中查找点字符并返回该字符的索引；
    // 如果没有点字符，则返回输入字符串的长度
    // 点字符可能包括：\u002E（全点），\u3002（中文全点），\uFF0E（全角全点），
    // 和 \uFF61（半角中文全点）。
    //
    private static int searchDots(String s, int start) {
        int i;
        for (i = start; i < s.length(); i++) {
            if (isLabelSeparator(s.charAt(i))) {
                break;
            }
        }

        return i;
    }

    //
    // 检查字符串是否为根标签，"."。
    //
    private static boolean isRootLabel(String s) {
        return (s.length() == 1 && isLabelSeparator(s.charAt(0)));
    }

    //
    // 检查字符是否为标签分隔符，即点字符。
    //
    private static boolean isLabelSeparator(char c) {
        return (c == '.' || c == '\u3002' || c == '\uFF0E' || c == '\uFF61');
    }

    //
    // 检查字符串是否仅包含 US-ASCII 代码点
    //
    private static boolean isAllASCII(String input) {
        boolean isASCII = true;
        for (int i = 0; i < input.length(); i++) {
            int c = input.charAt(i);
            if (c > 0x7F) {
                isASCII = false;
                break;
            }
        }
        return isASCII;
    }

    //
    // 检查字符串是否以 ACE 前缀开头
    //
    private static boolean startsWithACEPrefix(StringBuffer input){
        boolean startsWithPrefix = true;

        if(input.length() < ACE_PREFIX_LENGTH){
            return false;
        }
        for(int i = 0; i < ACE_PREFIX_LENGTH; i++){
            if(toASCIILower(input.charAt(i)) != ACE_PREFIX.charAt(i)){
                startsWithPrefix = false;
            }
        }
        return startsWithPrefix;
    }

    private static char toASCIILower(char ch){
        if('A' <= ch && ch <= 'Z'){
            return (char)(ch + 'a' - 'A');
        }
        return ch;
    }

    private static StringBuffer toASCIILower(StringBuffer input){
        StringBuffer dest = new StringBuffer();
        for(int i = 0; i < input.length();i++){
            dest.append(toASCIILower(input.charAt(i)));
        }
        return dest;
    }
}
