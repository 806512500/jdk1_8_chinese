
/*
 * Copyright (c) 1995, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;

/**
 * {@code StreamTokenizer} 类从输入流中解析出“标记”，允许一次读取一个标记。解析过程由一个表和多个可以设置为不同状态的标志控制。流解析器可以识别标识符、数字、带引号的字符串和各种注释样式。
 * <p>
 * 从输入流中读取的每个字节都被视为范围 {@code '\u005Cu0000'} 到 {@code '\u005Cu00FF'} 之间的字符。字符值用于查找该字符的五个可能属性：空白、字母、数字、字符串引号和注释字符。每个字符可以具有零个或多个这些属性。
 * <p>
 * 此外，实例有四个标志。这些标志表示：
 * <ul>
 * <li>行终止符是作为标记返回还是被视为仅分隔标记的空白。
 * <li>是否识别并跳过 C 风格的注释。
 * <li>是否识别并跳过 C++ 风格的注释。
 * <li>标识符的字符是否转换为小写。
 * </ul>
 * <p>
 * 典型的应用程序首先构造此类的实例，设置语法表，然后在循环中反复调用 {@code nextToken} 方法，直到该方法返回值 {@code TT_EOF}。
 *
 * @author  James Gosling
 * @see     java.io.StreamTokenizer#nextToken()
 * @see     java.io.StreamTokenizer#TT_EOF
 * @since   JDK1.0
 */

public class StreamTokenizer {

    /* 以下之一将非空 */
    private Reader reader = null;
    private InputStream input = null;

    private char buf[] = new char[20];

    /**
     * 由 {@code nextToken} 方法考虑的下一个字符。也可以是 NEED_CHAR，表示应读取新字符，或 SKIP_LF，表示应读取新字符，并且如果它是 '\n' 字符，则应丢弃并读取第二个新字符。
     */
    private int peekc = NEED_CHAR;

    private static final int NEED_CHAR = Integer.MAX_VALUE;
    private static final int SKIP_LF = Integer.MAX_VALUE - 1;

    private boolean pushedBack;
    private boolean forceLower;
    /** 最后读取的标记的行号 */
    private int LINENO = 1;

    private boolean eolIsSignificantP = false;
    private boolean slashSlashCommentsP = false;
    private boolean slashStarCommentsP = false;

    private byte ctype[] = new byte[256];
    private static final byte CT_WHITESPACE = 1;
    private static final byte CT_DIGIT = 2;
    private static final byte CT_ALPHA = 4;
    private static final byte CT_QUOTE = 8;
    private static final byte CT_COMMENT = 16;

    /**
     * 调用 {@code nextToken} 方法后，此字段包含刚读取的标记类型。对于单字符标记，其值是该字符转换为整数。对于带引号的字符串标记，其值是引号字符。否则，其值是以下之一：
     * <ul>
     * <li>{@code TT_WORD} 表示标记是单词。
     * <li>{@code TT_NUMBER} 表示标记是数字。
     * <li>{@code TT_EOL} 表示已读取行尾。此字段仅在调用 {@code eolIsSignificant} 方法时带有参数 {@code true} 时才可能具有此值。
     * <li>{@code TT_EOF} 表示已到达输入流的末尾。
     * </ul>
     * <p>
     * 此字段的初始值为 -4。
     *
     * @see     java.io.StreamTokenizer#eolIsSignificant(boolean)
     * @see     java.io.StreamTokenizer#nextToken()
     * @see     java.io.StreamTokenizer#quoteChar(int)
     * @see     java.io.StreamTokenizer#TT_EOF
     * @see     java.io.StreamTokenizer#TT_EOL
     * @see     java.io.StreamTokenizer#TT_NUMBER
     * @see     java.io.StreamTokenizer#TT_WORD
     */
    public int ttype = TT_NOTHING;

    /**
     * 表示已读取流末的常量。
     */
    public static final int TT_EOF = -1;

    /**
     * 表示已读取行尾的常量。
     */
    public static final int TT_EOL = '\n';

    /**
     * 表示已读取数字标记的常量。
     */
    public static final int TT_NUMBER = -2;

    /**
     * 表示已读取单词标记的常量。
     */
    public static final int TT_WORD = -3;

    /* 表示未读取任何标记的常量，用于初始化 ttype。FIXME 这可以公开并在未来的版本中作为 API 的一部分提供。 */
    private static final int TT_NOTHING = -4;

    /**
     * 如果当前标记是单词标记，此字段包含单词标记的字符字符串。当当前标记是带引号的字符串标记时，此字段包含字符串的主体。
     * <p>
     * 当 {@code ttype} 字段的值为 {@code TT_WORD} 时，当前标记是单词。当 {@code ttype} 字段的值为引号字符时，当前标记是带引号的字符串标记。
     * <p>
     * 此字段的初始值为 null。
     *
     * @see     java.io.StreamTokenizer#quoteChar(int)
     * @see     java.io.StreamTokenizer#TT_WORD
     * @see     java.io.StreamTokenizer#ttype
     */
    public String sval;

    /**
     * 如果当前标记是数字，此字段包含该数字的值。当 {@code ttype} 字段的值为 {@code TT_NUMBER} 时，当前标记是数字。
     * <p>
     * 此字段的初始值为 0.0。
     *
     * @see     java.io.StreamTokenizer#TT_NUMBER
     * @see     java.io.StreamTokenizer#ttype
     */
    public double nval;

    /** 私有构造函数，初始化除流之外的所有内容。 */
    private StreamTokenizer() {
        wordChars('a', 'z');
        wordChars('A', 'Z');
        wordChars(128 + 32, 255);
        whitespaceChars(0, ' ');
        commentChar('/');
        quoteChar('"');
        quoteChar('\'');
        parseNumbers();
    }

    /**
     * 创建一个解析指定输入流的流解析器。流解析器初始化为以下默认状态：
     * <ul>
     * <li>所有字节值 {@code 'A'} 到 {@code 'Z'}，{@code 'a'} 到 {@code 'z'}，以及 {@code '\u005Cu00A0'} 到 {@code '\u005Cu00FF'} 被视为字母。
     * <li>所有字节值 {@code '\u005Cu0000'} 到 {@code '\u005Cu0020'} 被视为空白。
     * <li>{@code '/'} 是注释字符。
     * <li>单引号 {@code '\u005C''} 和双引号 {@code '"'} 是字符串引号字符。
     * <li>数字被解析。
     * <li>行终止符被视为空白，而不是单独的标记。
     * <li>不识别 C 风格和 C++ 风格的注释。
     * </ul>
     *
     * @deprecated 从 JDK 版本 1.1 开始，解析输入流的首选方法是将其转换为字符流，例如：
     * <blockquote><pre>
     *   Reader r = new BufferedReader(new InputStreamReader(is));
     *   StreamTokenizer st = new StreamTokenizer(r);
     * </pre></blockquote>
     *
     * @param      is        输入流。
     * @see        java.io.BufferedReader
     * @see        java.io.InputStreamReader
     * @see        java.io.StreamTokenizer#StreamTokenizer(java.io.Reader)
     */
    @Deprecated
    public StreamTokenizer(InputStream is) {
        this();
        if (is == null) {
            throw new NullPointerException();
        }
        input = is;
    }

    /**
     * 创建一个解析给定字符流的解析器。
     *
     * @param r  提供输入流的 Reader 对象。
     * @since   JDK1.1
     */
    public StreamTokenizer(Reader r) {
        this();
        if (r == null) {
            throw new NullPointerException();
        }
        reader = r;
    }

    /**
     * 重置此解析器的语法表，使所有字符都为“普通”字符。有关字符为普通字符的更多信息，请参见 {@code ordinaryChar} 方法。
     *
     * @see     java.io.StreamTokenizer#ordinaryChar(int)
     */
    public void resetSyntax() {
        for (int i = ctype.length; --i >= 0;)
            ctype[i] = 0;
    }

    /**
     * 指定范围 <code>low&nbsp;&lt;=&nbsp;<i>c</i>&nbsp;&lt;=&nbsp;high</code> 内的所有字符 <i>c</i> 都是单词组成字符。单词标记由单词组成字符后跟零个或多个单词组成字符或数字组成字符组成。
     *
     * @param   low   范围的低端。
     * @param   hi    范围的高端。
     */
    public void wordChars(int low, int hi) {
        if (low < 0)
            low = 0;
        if (hi >= ctype.length)
            hi = ctype.length - 1;
        while (low <= hi)
            ctype[low++] |= CT_ALPHA;
    }

    /**
     * 指定范围 <code>low&nbsp;&lt;=&nbsp;<i>c</i>&nbsp;&lt;=&nbsp;high</code> 内的所有字符 <i>c</i> 都是空白字符。空白字符仅用于分隔输入流中的标记。
     *
     * <p>指定范围内的任何其他属性设置将被清除。
     *
     * @param   low   范围的低端。
     * @param   hi    范围的高端。
     */
    public void whitespaceChars(int low, int hi) {
        if (low < 0)
            low = 0;
        if (hi >= ctype.length)
            hi = ctype.length - 1;
        while (low <= hi)
            ctype[low++] = CT_WHITESPACE;
    }

    /**
     * 指定范围 <code>low&nbsp;&lt;=&nbsp;<i>c</i>&nbsp;&lt;=&nbsp;high</code> 内的所有字符 <i>c</i> 在此解析器中为“普通”字符。有关字符为普通字符的更多信息，请参见 {@code ordinaryChar} 方法。
     *
     * @param   low   范围的低端。
     * @param   hi    范围的高端。
     * @see     java.io.StreamTokenizer#ordinaryChar(int)
     */
    public void ordinaryChars(int low, int hi) {
        if (low < 0)
            low = 0;
        if (hi >= ctype.length)
            hi = ctype.length - 1;
        while (low <= hi)
            ctype[low++] = 0;
    }

    /**
     * 指定字符参数在此解析器中为“普通”字符。它移除了该字符作为注释字符、单词组成部分、字符串分隔符、空白字符或数字字符的任何特殊意义。当解析器遇到此类字符时，解析器将其视为单字符标记，并将 {@code ttype} 字段设置为字符值。
     *
     * <p>使行终止符字符“普通”可能会影响 {@code StreamTokenizer} 计算行数的能力。{@code lineno} 方法在其行计数中可能不再反映此类终止符字符的存在。
     *
     * @param   ch   字符。
     * @see     java.io.StreamTokenizer#ttype
     */
    public void ordinaryChar(int ch) {
        if (ch >= 0 && ch < ctype.length)
            ctype[ch] = 0;
    }

    /**
     * 指定字符参数开始单行注释。从注释字符到行尾的所有字符都被此流解析器忽略。
     *
     * <p>指定字符的任何其他属性设置将被清除。
     *
     * @param   ch   字符。
     */
    public void commentChar(int ch) {
        if (ch >= 0 && ch < ctype.length)
            ctype[ch] = CT_COMMENT;
    }

    /**
     * 指定匹配的字符对在此解析器中分隔字符串常量。
     * <p>
     * 当 {@code nextToken} 方法遇到字符串常量时，{@code ttype} 字段被设置为字符串分隔符，{@code sval} 字段被设置为字符串的主体。
     * <p>
     * 如果遇到字符串引号字符，则识别一个字符串，该字符串由所有字符组成，这些字符在（但不包括）字符串引号字符之后，直到（但不包括）下一个相同的字符串引号字符、行终止符或文件末尾。解析字符串时，通常的转义序列（如 {@code "\u005Cn"} 和 {@code "\u005Ct"}）被识别并转换为单个字符。
     *
     * <p>指定字符的任何其他属性设置将被清除。
     *
     * @param   ch   字符。
     * @see     java.io.StreamTokenizer#nextToken()
     * @see     java.io.StreamTokenizer#sval
     * @see     java.io.StreamTokenizer#ttype
     */
    public void quoteChar(int ch) {
        if (ch >= 0 && ch < ctype.length)
            ctype[ch] = CT_QUOTE;
    }

    /**
     * 指定此解析器应解析数字。此解析器的语法表被修改，使得以下十二个字符：
     * <blockquote><pre>
     *      0 1 2 3 4 5 6 7 8 9 . -
     * </pre></blockquote>
     * <p>
     * 具有“数字”属性。
     * <p>
     * 当解析器遇到具有双精度浮点数格式的单词标记时，它将该标记视为数字而不是单词，通过将 {@code ttype} 字段设置为值 {@code TT_NUMBER} 并将标记的数值放入 {@code nval} 字段中。
     *
     * @see     java.io.StreamTokenizer#nval
     * @see     java.io.StreamTokenizer#TT_NUMBER
     * @see     java.io.StreamTokenizer#ttype
     */
    public void parseNumbers() {
        for (int i = '0'; i <= '9'; i++)
            ctype[i] |= CT_DIGIT;
        ctype['.'] |= CT_DIGIT;
        ctype['-'] |= CT_DIGIT;
    }


                /**
     * 确定换行符是否被视为标记。
     * 如果标志参数为 true，此分词器将换行符视为标记；{@code nextToken} 方法返回
     * {@code TT_EOL} 并且还将 {@code ttype} 字段设置为
     * 此值，当读取换行符时。
     * <p>
     * 一行是以回车字符 ({@code '\u005Cr'}) 或换行符
     * 字符 ({@code '\u005Cn'}) 结束的一系列字符。此外，紧跟在回车字符后面的换行符
     * 被视为单个换行符标记。
     * <p>
     * 如果 {@code flag} 为 false，换行符被视为空白字符，仅用于分隔标记。
     *
     * @param   flag   {@code true} 表示换行符是单独的标记；{@code false} 表示
     *                 换行符是空白字符。
     * @see     java.io.StreamTokenizer#nextToken()
     * @see     java.io.StreamTokenizer#ttype
     * @see     java.io.StreamTokenizer#TT_EOL
     */
    public void eolIsSignificant(boolean flag) {
        eolIsSignificantP = flag;
    }

    /**
     * 确定分词器是否识别 C 风格的注释。
     * 如果标志参数为 {@code true}，此流分词器
     * 识别 C 风格的注释。所有出现在连续的
     * {@code /*} 和 <code>*&#47;</code> 之间的文本都将被丢弃。
     * <p>
     * 如果标志参数为 {@code false}，则 C 风格的注释
     * 不会被特殊处理。
     *
     * @param   flag   {@code true} 表示识别并忽略
     *                 C 风格的注释。
     */
    public void slashStarComments(boolean flag) {
        slashStarCommentsP = flag;
    }

    /**
     * 确定分词器是否识别 C++ 风格的注释。
     * 如果标志参数为 {@code true}，此流分词器
     * 识别 C++ 风格的注释。任何出现的两个连续的
     * 斜杠字符 ({@code '/'}) 都被视为注释的开始
     * 一直延伸到行尾。
     * <p>
     * 如果标志参数为 {@code false}，则 C++ 风格的
     * 注释不会被特殊处理。
     *
     * @param   flag   {@code true} 表示识别并忽略
     *                 C++ 风格的注释。
     */
    public void slashSlashComments(boolean flag) {
        slashSlashCommentsP = flag;
    }

    /**
     * 确定单词标记是否自动转换为小写。
     * 如果标志参数为 {@code true}，则每当返回单词标记时
     * （{@code ttype} 字段的值为 {@code TT_WORD}），由
     * {@code nextToken} 方法返回的 {@code sval} 字段将被转换为小写。
     * <p>
     * 如果标志参数为 {@code false}，则
     * {@code sval} 字段不会被修改。
     *
     * @param   fl   {@code true} 表示所有单词标记都应转换为小写。
     * @see     java.io.StreamTokenizer#nextToken()
     * @see     java.io.StreamTokenizer#ttype
     * @see     java.io.StreamTokenizer#TT_WORD
     */
    public void lowerCaseMode(boolean fl) {
        forceLower = fl;
    }

    /** 读取下一个字符 */
    private int read() throws IOException {
        if (reader != null)
            return reader.read();
        else if (input != null)
            return input.read();
        else
            throw new IllegalStateException();
    }

    /**
     * 从该分词器的输入流中解析下一个标记。
     * 下一个标记的类型将返回在 {@code ttype}
     * 字段中。关于标记的其他信息可能在
     * {@code nval} 字段或 {@code sval} 字段中。
     * <p>
     * 该类的典型客户端首先设置语法表，然后进入一个循环
     * 调用 nextToken 以解析连续的标记，直到返回 TT_EOF
     * 为止。
     *
     * @return     {@code ttype} 字段的值。
     * @exception  IOException  如果发生 I/O 错误。
     * @see        java.io.StreamTokenizer#nval
     * @see        java.io.StreamTokenizer#sval
     * @see        java.io.StreamTokenizer#ttype
     */
    public int nextToken() throws IOException {
        if (pushedBack) {
            pushedBack = false;
            return ttype;
        }
        byte ct[] = ctype;
        sval = null;

        int c = peekc;
        if (c < 0)
            c = NEED_CHAR;
        if (c == SKIP_LF) {
            c = read();
            if (c < 0)
                return ttype = TT_EOF;
            if (c == '\n')
                c = NEED_CHAR;
        }
        if (c == NEED_CHAR) {
            c = read();
            if (c < 0)
                return ttype = TT_EOF;
        }
        ttype = c;              /* 为了安全起见 */

        /* 设置 peekc 以便下次调用 nextToken 时读取
         * 另一个字符，除非在此调用中重置 peekc
         */
        peekc = NEED_CHAR;

        int ctype = c < 256 ? ct[c] : CT_ALPHA;
        while ((ctype & CT_WHITESPACE) != 0) {
            if (c == '\r') {
                LINENO++;
                if (eolIsSignificantP) {
                    peekc = SKIP_LF;
                    return ttype = TT_EOL;
                }
                c = read();
                if (c == '\n')
                    c = read();
            } else {
                if (c == '\n') {
                    LINENO++;
                    if (eolIsSignificantP) {
                        return ttype = TT_EOL;
                    }
                }
                c = read();
            }
            if (c < 0)
                return ttype = TT_EOF;
            ctype = c < 256 ? ct[c] : CT_ALPHA;
        }

        if ((ctype & CT_DIGIT) != 0) {
            boolean neg = false;
            if (c == '-') {
                c = read();
                if (c != '.' && (c < '0' || c > '9')) {
                    peekc = c;
                    return ttype = '-';
                }
                neg = true;
            }
            double v = 0;
            int decexp = 0;
            int seendot = 0;
            while (true) {
                if (c == '.' && seendot == 0)
                    seendot = 1;
                else if ('0' <= c && c <= '9') {
                    v = v * 10 + (c - '0');
                    decexp += seendot;
                } else
                    break;
                c = read();
            }
            peekc = c;
            if (decexp != 0) {
                double denom = 10;
                decexp--;
                while (decexp > 0) {
                    denom *= 10;
                    decexp--;
                }
                /* 对一个可能更准确的数字进行一次除法 */
                v = v / denom;
            }
            nval = neg ? -v : v;
            return ttype = TT_NUMBER;
        }

        if ((ctype & CT_ALPHA) != 0) {
            int i = 0;
            do {
                if (i >= buf.length) {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                }
                buf[i++] = (char) c;
                c = read();
                ctype = c < 0 ? CT_WHITESPACE : c < 256 ? ct[c] : CT_ALPHA;
            } while ((ctype & (CT_ALPHA | CT_DIGIT)) != 0);
            peekc = c;
            sval = String.copyValueOf(buf, 0, i);
            if (forceLower)
                sval = sval.toLowerCase();
            return ttype = TT_WORD;
        }

        if ((ctype & CT_QUOTE) != 0) {
            ttype = c;
            int i = 0;
            /* 不变性（因为 \Octal 需要向前看）：
             *   (i)  c 包含字符值
             *   (ii) d 包含向前看的字符
             */
            int d = read();
            while (d >= 0 && d != ttype && d != '\n' && d != '\r') {
                if (d == '\\') {
                    c = read();
                    int first = c;   /* 为了允许 \377，但不允许 \477 */
                    if (c >= '0' && c <= '7') {
                        c = c - '0';
                        int c2 = read();
                        if ('0' <= c2 && c2 <= '7') {
                            c = (c << 3) + (c2 - '0');
                            c2 = read();
                            if ('0' <= c2 && c2 <= '7' && first <= '3') {
                                c = (c << 3) + (c2 - '0');
                                d = read();
                            } else
                                d = c2;
                        } else
                          d = c2;
                    } else {
                        switch (c) {
                        case 'a':
                            c = 0x7;
                            break;
                        case 'b':
                            c = '\b';
                            break;
                        case 'f':
                            c = 0xC;
                            break;
                        case 'n':
                            c = '\n';
                            break;
                        case 'r':
                            c = '\r';
                            break;
                        case 't':
                            c = '\t';
                            break;
                        case 'v':
                            c = 0xB;
                            break;
                        }
                        d = read();
                    }
                } else {
                    c = d;
                    d = read();
                }
                if (i >= buf.length) {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                }
                buf[i++] = (char)c;
            }

            /* 如果我们因为找到一个匹配的引号字符而跳出循环
             * 则安排下次读取一个新字符；否则，保存该字符。
             */
            peekc = (d == ttype) ? NEED_CHAR : d;

            sval = String.copyValueOf(buf, 0, i);
            return ttype;
        }

        if (c == '/' && (slashSlashCommentsP || slashStarCommentsP)) {
            c = read();
            if (c == '*' && slashStarCommentsP) {
                int prevc = 0;
                while ((c = read()) != '/' || prevc != '*') {
                    if (c == '\r') {
                        LINENO++;
                        c = read();
                        if (c == '\n') {
                            c = read();
                        }
                    } else {
                        if (c == '\n') {
                            LINENO++;
                            c = read();
                        }
                    }
                    if (c < 0)
                        return ttype = TT_EOF;
                    prevc = c;
                }
                return nextToken();
            } else if (c == '/' && slashSlashCommentsP) {
                while ((c = read()) != '\n' && c != '\r' && c >= 0);
                peekc = c;
                return nextToken();
            } else {
                /* 现在看看它是否仍然是单行注释 */
                if ((ct['/'] & CT_COMMENT) != 0) {
                    while ((c = read()) != '\n' && c != '\r' && c >= 0);
                    peekc = c;
                    return nextToken();
                } else {
                    peekc = c;
                    return ttype = '/';
                }
            }
        }

        if ((ctype & CT_COMMENT) != 0) {
            while ((c = read()) != '\n' && c != '\r' && c >= 0);
            peekc = c;
            return nextToken();
        }

        return ttype = c;
    }

    /**
     * 导致此分词器的 {@code nextToken} 方法的下一次调用返回
     * 当前 {@code ttype} 字段中的值，并且不修改
     * {@code nval} 或 {@code sval} 字段中的值。
     *
     * @see     java.io.StreamTokenizer#nextToken()
     * @see     java.io.StreamTokenizer#nval
     * @see     java.io.StreamTokenizer#sval
     * @see     java.io.StreamTokenizer#ttype
     */
    public void pushBack() {
        if (ttype != TT_NOTHING)   /* 如果未调用 nextToken()，则为无操作 */
            pushedBack = true;
    }

    /**
     * 返回当前行号。
     *
     * @return  此流分词器的当前行号。
     */
    public int lineno() {
        return LINENO;
    }

    /**
     * 返回当前流标记的字符串表示形式及其出现的行号。
     *
     * <p>返回的精确字符串是未指定的，尽管以下示例可以视为典型：
     *
     * <blockquote><pre>Token['a'], line 10</pre></blockquote>
     *
     * @return  标记的字符串表示形式
     * @see     java.io.StreamTokenizer#nval
     * @see     java.io.StreamTokenizer#sval
     * @see     java.io.StreamTokenizer#ttype
     */
    public String toString() {
        String ret;
        switch (ttype) {
          case TT_EOF:
            ret = "EOF";
            break;
          case TT_EOL:
            ret = "EOL";
            break;
          case TT_WORD:
            ret = sval;
            break;
          case TT_NUMBER:
            ret = "n=" + nval;
            break;
          case TT_NOTHING:
            ret = "NOTHING";
            break;
          default: {
                /*
                 * ttype 是引号字符串的第一个字符或
                 * 普通字符。ttype 肯定不能小于 0，因为这些是保留值
                 * 用于前面的 case 语句
                 */
                if (ttype < 256 &&
                    ((ctype[ttype] & CT_QUOTE) != 0)) {
                    ret = sval;
                    break;
                }

                char s[] = new char[3];
                s[0] = s[2] = '\'';
                s[1] = (char) ttype;
                ret = new String(s);
                break;
            }
        }
        return "Token[" + ret + "], line " + LINENO;
    }

}
