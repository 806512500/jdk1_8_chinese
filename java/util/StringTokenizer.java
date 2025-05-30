
/*
 * Copyright (c) 1994, 2004, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.*;

/**
 * 字符串分词器类允许应用程序将字符串分解为分词。分词方法比
 * <code>StreamTokenizer</code> 类使用的分词方法简单得多。分词器方法不区分
 * 标识符、数字和带引号的字符串，也不识别和跳过注释。
 * <p>
 * 分隔符集（分隔分词的字符）可以在创建时或按分词指定。
 * <p>
 * <code>StringTokenizer</code> 的实例根据是否在创建时使用
 * <code>returnDelims</code> 标志设置为 <code>true</code> 或 <code>false</code>
 * 而表现出两种行为之一：
 * <ul>
 * <li>如果标志为 <code>false</code>，分隔符字符用作分词的分隔符。分词是最大连续非分隔符字符序列。
 * <li>如果标志为 <code>true</code>，分隔符字符本身被视为分词。分词因此是一个分隔符字符，或最大连续非分隔符字符序列。
 * </ul><p>
 * <tt>StringTokenizer</tt> 对象在内部维护一个当前位置，该位置位于要分词的字符串内。某些操作会将此当前位置向前推进到已处理的字符之后。<p>
 * 通过从用于创建 <tt>StringTokenizer</tt> 对象的字符串中提取子字符串来返回分词。
 * <p>
 * 以下是一个使用分词器的示例。代码：
 * <blockquote><pre>
 *     StringTokenizer st = new StringTokenizer("this is a test");
 *     while (st.hasMoreTokens()) {
 *         System.out.println(st.nextToken());
 *     }
 * </pre></blockquote>
 * <p>
 * 打印以下输出：
 * <blockquote><pre>
 *     this
 *     is
 *     a
 *     test
 * </pre></blockquote>
 *
 * <p>
 * <tt>StringTokenizer</tt> 是一个遗留类，出于兼容性原因而保留，尽管在新代码中不建议使用。建议任何寻求此功能的人使用 <tt>String</tt> 的 <tt>split</tt>
 * 方法或 java.util.regex 包。以下示例说明了如何使用 <tt>String.split</tt>
 * 方法将字符串分解为其基本分词：
 * <blockquote><pre>
 *     String[] result = "this is a test".split("\\s");
 *     for (int x=0; x<result.length; x++)
 *         System.out.println(result[x]);
 * </pre></blockquote>
 * <p>
 * 打印以下输出：
 * <blockquote><pre>
 *     this
 *     is
 *     a
 *     test
 * </pre></blockquote>
 *
 * @author  未署名
 * @see     java.io.StreamTokenizer
 * @since   JDK1.0
 */
public
class StringTokenizer implements Enumeration<Object> {
    private int currentPosition;
    private int newPosition;
    private int maxPosition;
    private String str;
    private String delimiters;
    private boolean retDelims;
    private boolean delimsChanged;

    /**
     * maxDelimCodePoint 存储分隔符集中值最高的分隔符字符的值。它用于优化分隔符字符的检测。
     *
     * 在 hasSurrogates 情况下，它不太可能提供任何优化好处，因为大多数字符串字符将小于限制，但我们保留它以使两条代码路径保持相似。
     */
    private int maxDelimCodePoint;

    /**
     * 如果分隔符包含任何代理（包括代理对），则 hasSurrogates 为 true，分词器使用不同的代码路径。这是因为 String.indexOf(int)
     * 不将未配对的代理视为单个字符。
     */
    private boolean hasSurrogates = false;

    /**
     * 当 hasSurrogates 为 true 时，分隔符转换为代码点，并使用 isDelimiter(int) 确定给定代码点是否为分隔符。
     */
    private int[] delimiterCodePoints;

    /**
     * 将 maxDelimCodePoint 设置为分隔符集中的最高字符。
     */
    private void setMaxDelimCodePoint() {
        if (delimiters == null) {
            maxDelimCodePoint = 0;
            return;
        }

        int m = 0;
        int c;
        int count = 0;
        for (int i = 0; i < delimiters.length(); i += Character.charCount(c)) {
            c = delimiters.charAt(i);
            if (c >= Character.MIN_HIGH_SURROGATE && c <= Character.MAX_LOW_SURROGATE) {
                c = delimiters.codePointAt(i);
                hasSurrogates = true;
            }
            if (m < c)
                m = c;
            count++;
        }
        maxDelimCodePoint = m;

        if (hasSurrogates) {
            delimiterCodePoints = new int[count];
            for (int i = 0, j = 0; i < count; i++, j += Character.charCount(c)) {
                c = delimiters.codePointAt(j);
                delimiterCodePoints[i] = c;
            }
        }
    }

    /**
     * 为指定的字符串构造一个字符串分词器。<code>delim</code> 参数中的所有字符都是用于分隔分词的分隔符。
     * <p>
     * 如果 <code>returnDelims</code> 标志为 <code>true</code>，则分隔符字符也作为分词返回。每个分隔符都作为一个长度为一的字符串返回。如果标志为
     * <code>false</code>，则分隔符字符被跳过，仅用作分词之间的分隔符。
     * <p>
     * 注意，如果 <tt>delim</tt> 为 <tt>null</tt>，此构造函数不会抛出异常。但是，尝试在生成的 <tt>StringTokenizer</tt> 上调用其他方法可能会导致
     * <tt>NullPointerException</tt>。
     *
     * @param   str            要解析的字符串。
     * @param   delim          分隔符。
     * @param   returnDelims   标志，指示是否将分隔符作为分词返回。
     * @exception NullPointerException 如果 str 为 <CODE>null</CODE>
     */
    public StringTokenizer(String str, String delim, boolean returnDelims) {
        currentPosition = 0;
        newPosition = -1;
        delimsChanged = false;
        this.str = str;
        maxPosition = str.length();
        delimiters = delim;
        retDelims = returnDelims;
        setMaxDelimCodePoint();
    }

    /**
     * 为指定的字符串构造一个字符串分词器。<code>delim</code> 参数中的字符是用于分隔分词的分隔符。分隔符字符本身不会被视为分词。
     * <p>
     * 注意，如果 <tt>delim</tt> 为 <tt>null</tt>，此构造函数不会抛出异常。但是，尝试在生成的 <tt>StringTokenizer</tt> 上调用其他方法可能会导致
     * <tt>NullPointerException</tt>。
     *
     * @param   str     要解析的字符串。
     * @param   delim   分隔符。
     * @exception NullPointerException 如果 str 为 <CODE>null</CODE>
     */
    public StringTokenizer(String str, String delim) {
        this(str, delim, false);
    }

    /**
     * 为指定的字符串构造一个字符串分词器。分词器使用默认的分隔符集，即
     * <code>"&nbsp;&#92;t&#92;n&#92;r&#92;f"</code>：空格字符、制表符字符、换行符字符、回车符字符和换页符字符。分隔符字符本身不会被视为分词。
     *
     * @param   str   要解析的字符串。
     * @exception NullPointerException 如果 str 为 <CODE>null</CODE>
     */
    public StringTokenizer(String str) {
        this(str, " \t\n\r\f", false);
    }

    /**
     * 从指定位置跳过分隔符。如果 retDelims 为 false，返回 startPos 或之后的第一个非分隔符字符的索引。如果 retDelims 为 true，返回 startPos。
     */
    private int skipDelimiters(int startPos) {
        if (delimiters == null)
            throw new NullPointerException();

        int position = startPos;
        while (!retDelims && position < maxPosition) {
            if (!hasSurrogates) {
                char c = str.charAt(position);
                if ((c > maxDelimCodePoint) || (delimiters.indexOf(c) < 0))
                    break;
                position++;
            } else {
                int c = str.codePointAt(position);
                if ((c > maxDelimCodePoint) || !isDelimiter(c)) {
                    break;
                }
                position += Character.charCount(c);
            }
        }
        return position;
    }

    /**
     * 从 startPos 向前跳转并返回遇到的下一个分隔符字符的索引，如果未找到这样的分隔符，则返回 maxPosition。
     */
    private int scanToken(int startPos) {
        int position = startPos;
        while (position < maxPosition) {
            if (!hasSurrogates) {
                char c = str.charAt(position);
                if ((c <= maxDelimCodePoint) && (delimiters.indexOf(c) >= 0))
                    break;
                position++;
            } else {
                int c = str.codePointAt(position);
                if ((c <= maxDelimCodePoint) && isDelimiter(c))
                    break;
                position += Character.charCount(c);
            }
        }
        if (retDelims && (startPos == position)) {
            if (!hasSurrogates) {
                char c = str.charAt(position);
                if ((c <= maxDelimCodePoint) && (delimiters.indexOf(c) >= 0))
                    position++;
            } else {
                int c = str.codePointAt(position);
                if ((c <= maxDelimCodePoint) && isDelimiter(c))
                    position += Character.charCount(c);
            }
        }
        return position;
    }

    private boolean isDelimiter(int codePoint) {
        for (int i = 0; i < delimiterCodePoints.length; i++) {
            if (delimiterCodePoints[i] == codePoint) {
                return true;
            }
        }
        return false;
    }

    /**
     * 测试此分词器的字符串中是否还有更多分词可用。如果此方法返回 <tt>true</tt>，则随后调用无参数的 <tt>nextToken</tt> 方法将成功返回一个分词。
     *
     * @return  <code>true</code> 如果且仅当字符串中当前位置之后至少有一个分词；<code>false</code> 否则。
     */
    public boolean hasMoreTokens() {
        /*
         * 暂时存储此位置并在以下
         * nextToken() 方法中仅在该 nextToken() 调用中分隔符未更改时使用该值。
         */
        newPosition = skipDelimiters(currentPosition);
        return (newPosition < maxPosition);
    }

    /**
     * 返回此字符串分词器的下一个分词。
     *
     * @return     此字符串分词器的下一个分词。
     * @exception  NoSuchElementException  如果此分词器的字符串中没有更多分词。
     */
    public String nextToken() {
        /*
         * 如果在 hasMoreElements() 中已经计算了下一个位置，并且在该调用和此调用之间分隔符未更改，则使用计算的值。
         */

        currentPosition = (newPosition >= 0 && !delimsChanged) ?
            newPosition : skipDelimiters(currentPosition);

        /* 无论如何重置这些值 */
        delimsChanged = false;
        newPosition = -1;

        if (currentPosition >= maxPosition)
            throw new NoSuchElementException();
        int start = currentPosition;
        currentPosition = scanToken(currentPosition);
        return str.substring(start, currentPosition);
    }

    /**
     * 返回此字符串分词器字符串中的下一个分词。首先，此
     * <tt>StringTokenizer</tt> 对象认为的分隔符集更改为 <tt>delim</tt> 字符串中的字符。然后返回字符串中当前位置之后的下一个分词。当前位置
     * 被推进到识别的分词之后。新的分隔符集在调用后保持为默认值。
     *
     * @param      delim   新的分隔符。
     * @return     切换到新的分隔符集后的下一个分词。
     * @exception  NoSuchElementException  如果此分词器的字符串中没有更多分词。
     * @exception NullPointerException 如果 delim 为 <CODE>null</CODE>
     */
    public String nextToken(String delim) {
        delimiters = delim;

        /* 指定了分隔符字符串，因此设置适当的标志。 */
        delimsChanged = true;

        setMaxDelimCodePoint();
        return nextToken();
    }

    /**
     * 返回与 <code>hasMoreTokens</code> 方法相同的值。它存在是为了使此类能够实现
     * <code>Enumeration</code> 接口。
     *
     * @return  <code>true</code> 如果还有更多分词；<code>false</code> 否则。
     * @see     java.util.Enumeration
     * @see     java.util.StringTokenizer#hasMoreTokens()
     */
    public boolean hasMoreElements() {
        return hasMoreTokens();
    }

    /**
     * 返回与 <code>nextToken</code> 方法相同的值，但其声明的返回值是 <code>Object</code> 而不是
     * <code>String</code>。它存在是为了使此类能够实现
     * <code>Enumeration</code> 接口。
     *
     * @return     字符串中的下一个分词。
     * @exception  NoSuchElementException  如果此分词器的字符串中没有更多分词。
     * @see        java.util.Enumeration
     * @see        java.util.StringTokenizer#nextToken()
     */
    public Object nextElement() {
        return nextToken();
    }

    /**
     * 计算此分词器的 <code>nextToken</code> 方法在生成异常之前可以调用的次数。当前位置不会前进。
     *
     * @return  使用当前分隔符集时字符串中剩余的分词数。
     * @see     java.util.StringTokenizer#nextToken()
     */
    public int countTokens() {
        int count = 0;
        int currpos = currentPosition;
        while (currpos < maxPosition) {
            currpos = skipDelimiters(currpos);
            if (currpos >= maxPosition)
                break;
            currpos = scanToken(currpos);
            count++;
        }
        return count;
    }
}
