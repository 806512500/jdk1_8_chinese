
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

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.text;

import java.io.InvalidObjectException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * <code>MessageFormat</code> 提供了一种以语言中立的方式生成连接消息的手段。使用此方法来构造显示给最终用户的消息。
 *
 * <p>
 * <code>MessageFormat</code> 接收一组对象，对其进行格式化，然后将格式化的字符串插入到模式中适当的位置。
 *
 * <p>
 * <strong>注意：</strong>
 * <code>MessageFormat</code> 与其他 <code>Format</code> 类不同，您需要使用其构造函数之一创建 <code>MessageFormat</code> 对象（而不是使用 <code>getInstance</code> 风格的工厂方法）。工厂方法不是必需的，因为 <code>MessageFormat</code> 本身不实现特定于区域的行为。任何特定于区域的行为都是由您提供的模式以及用于插入参数的子格式定义的。
 *
 * <h3><a name="patterns">模式及其解释</a></h3>
 *
 * <code>MessageFormat</code> 使用以下形式的模式：
 * <blockquote><pre>
 * <i>MessageFormatPattern:</i>
 *         <i>String</i>
 *         <i>MessageFormatPattern</i> <i>FormatElement</i> <i>String</i>
 *
 * <i>FormatElement:</i>
 *         { <i>ArgumentIndex</i> }
 *         { <i>ArgumentIndex</i> , <i>FormatType</i> }
 *         { <i>ArgumentIndex</i> , <i>FormatType</i> , <i>FormatStyle</i> }
 *
 * <i>FormatType: 之一 </i>
 *         number date time choice
 *
 * <i>FormatStyle:</i>
 *         short
 *         medium
 *         long
 *         full
 *         integer
 *         currency
 *         percent
 *         <i>SubformatPattern</i>
 * </pre></blockquote>
 *
 * <p>在 <i>String</i> 中，一对单引号可以用来引用任意字符，除了单引号。例如，模式字符串 <code>"'{0}'"</code> 表示字符串
 * <code>"{0}"</code>，而不是一个 <i>FormatElement</i>。单引号本身必须在 <i>String</i> 中用两个单引号 {@code ''} 表示。例如，模式字符串 <code>"'{''}'"</code>
 * 被解释为 <code>'{</code>（开始引用和左大括号）、<code>''</code>（一个单引号）和
 * <code>}'</code>（右大括号和结束引用），<em>不是</em> <code>'{'</code> 和 <code>'}'</code>（引用的左和右大括号）：表示字符串 <code>"{'}"</code>，
 * <em>不是</em> <code>"{}"</code>。
 *
 * <p><i>SubformatPattern</i> 由其对应的子格式解释，子格式特定的模式规则适用。例如，模式字符串 <code>"{1,number,<u>$'#',##</u>}"</code>
 * （带有下划线的 <i>SubformatPattern</i>）将生成一个带有井号引用的数字格式，结果如：{@code
 * "$#31,45"}. 请参阅每个 {@code Format} 子类的文档以获取详细信息。
 *
 * <p>任何未匹配的引号被视为在给定模式的末尾关闭。例如，模式字符串 {@code "'{0}"} 被视为模式 {@code "'{0}'"}。
 *
 * <p>任何未引用的模式中的大括号必须是平衡的。例如，<code>"ab {0} de"</code> 和 <code>"ab '}' de"</code> 是有效的模式，但 <code>"ab {0'}' de"</code>，<code>"ab } de"</code>
 * 和 <code>"''{''"</code> 不是。
 *
 * <dl><dt><b>警告：</b><dd>使用消息格式模式中的引号规则不幸地显示出了某种程度的混淆。特别是，本地化人员并不总是清楚何时需要加倍单引号。确保告知本地化人员规则，并通过使用资源束源文件中的注释（例如）告诉他们哪些字符串将由 {@code MessageFormat} 处理。
 * 注意，本地化人员可能需要在翻译的字符串中使用单引号，而原始版本中没有这些单引号。
 * </dl>
 * <p>
 * <i>ArgumentIndex</i> 值是非负整数，使用 {@code '0'} 到 {@code '9'} 之间的数字表示，表示一个索引，该索引用于传递给 {@code format} 方法或由 {@code parse} 方法返回的结果数组中的
 * {@code arguments} 数组。
 * <p>
 * <i>FormatType</i> 和 <i>FormatStyle</i> 值用于为格式元素创建一个 {@code Format} 实例。下表显示了这些值如何映射到 {@code Format} 实例。表中未显示的组合是非法的。一个 <i>SubformatPattern</i> 必须
 * 是用于该 {@code Format} 子类的有效模式字符串。
 *
 * <table border=1 summary="显示 FormatType 和 FormatStyle 值如何映射到 Format 实例">
 *    <tr>
 *       <th id="ft" class="TableHeadingColor">FormatType
 *       <th id="fs" class="TableHeadingColor">FormatStyle
 *       <th id="sc" class="TableHeadingColor">创建的子格式
 *    <tr>
 *       <td headers="ft"><i>(无)</i>
 *       <td headers="fs"><i>(无)</i>
 *       <td headers="sc"><code>null</code>
 *    <tr>
 *       <td headers="ft" rowspan=5><code>number</code>
 *       <td headers="fs"><i>(无)</i>
 *       <td headers="sc">{@link NumberFormat#getInstance(Locale) NumberFormat.getInstance}{@code (getLocale())}
 *    <tr>
 *       <td headers="fs"><code>integer</code>
 *       <td headers="sc">{@link NumberFormat#getIntegerInstance(Locale) NumberFormat.getIntegerInstance}{@code (getLocale())}
 *    <tr>
 *       <td headers="fs"><code>currency</code>
 *       <td headers="sc">{@link NumberFormat#getCurrencyInstance(Locale) NumberFormat.getCurrencyInstance}{@code (getLocale())}
 *    <tr>
 *       <td headers="fs"><code>percent</code>
 *       <td headers="sc">{@link NumberFormat#getPercentInstance(Locale) NumberFormat.getPercentInstance}{@code (getLocale())}
 *    <tr>
 *       <td headers="fs"><i>SubformatPattern</i>
 *       <td headers="sc">{@code new} {@link DecimalFormat#DecimalFormat(String,DecimalFormatSymbols) DecimalFormat}{@code (subformatPattern,} {@link DecimalFormatSymbols#getInstance(Locale) DecimalFormatSymbols.getInstance}{@code (getLocale()))}
 *    <tr>
 *       <td headers="ft" rowspan=6><code>date</code>
 *       <td headers="fs"><i>(无)</i>
 *       <td headers="sc">{@link DateFormat#getDateInstance(int,Locale) DateFormat.getDateInstance}{@code (}{@link DateFormat#DEFAULT}{@code , getLocale())}
 *    <tr>
 *       <td headers="fs"><code>short</code>
 *       <td headers="sc">{@link DateFormat#getDateInstance(int,Locale) DateFormat.getDateInstance}{@code (}{@link DateFormat#SHORT}{@code , getLocale())}
 *    <tr>
 *       <td headers="fs"><code>medium</code>
 *       <td headers="sc">{@link DateFormat#getDateInstance(int,Locale) DateFormat.getDateInstance}{@code (}{@link DateFormat#DEFAULT}{@code , getLocale())}
 *    <tr>
 *       <td headers="fs"><code>long</code>
 *       <td headers="sc">{@link DateFormat#getDateInstance(int,Locale) DateFormat.getDateInstance}{@code (}{@link DateFormat#LONG}{@code , getLocale())}
 *    <tr>
 *       <td headers="fs"><code>full</code>
 *       <td headers="sc">{@link DateFormat#getDateInstance(int,Locale) DateFormat.getDateInstance}{@code (}{@link DateFormat#FULL}{@code , getLocale())}
 *    <tr>
 *       <td headers="fs"><i>SubformatPattern</i>
 *       <td headers="sc">{@code new} {@link SimpleDateFormat#SimpleDateFormat(String,Locale) SimpleDateFormat}{@code (subformatPattern, getLocale())}
 *    <tr>
 *       <td headers="ft" rowspan=6><code>time</code>
 *       <td headers="fs"><i>(无)</i>
 *       <td headers="sc">{@link DateFormat#getTimeInstance(int,Locale) DateFormat.getTimeInstance}{@code (}{@link DateFormat#DEFAULT}{@code , getLocale())}
 *    <tr>
 *       <td headers="fs"><code>short</code>
 *       <td headers="sc">{@link DateFormat#getTimeInstance(int,Locale) DateFormat.getTimeInstance}{@code (}{@link DateFormat#SHORT}{@code , getLocale())}
 *    <tr>
 *       <td headers="fs"><code>medium</code>
 *       <td headers="sc">{@link DateFormat#getTimeInstance(int,Locale) DateFormat.getTimeInstance}{@code (}{@link DateFormat#DEFAULT}{@code , getLocale())}
 *    <tr>
 *       <td headers="fs"><code>long</code>
 *       <td headers="sc">{@link DateFormat#getTimeInstance(int,Locale) DateFormat.getTimeInstance}{@code (}{@link DateFormat#LONG}{@code , getLocale())}
 *    <tr>
 *       <td headers="fs"><code>full</code>
 *       <td headers="sc">{@link DateFormat#getTimeInstance(int,Locale) DateFormat.getTimeInstance}{@code (}{@link DateFormat#FULL}{@code , getLocale())}
 *    <tr>
 *       <td headers="fs"><i>SubformatPattern</i>
 *       <td headers="sc">{@code new} {@link SimpleDateFormat#SimpleDateFormat(String,Locale) SimpleDateFormat}{@code (subformatPattern, getLocale())}
 *    <tr>
 *       <td headers="ft"><code>choice</code>
 *       <td headers="fs"><i>SubformatPattern</i>
 *       <td headers="sc">{@code new} {@link ChoiceFormat#ChoiceFormat(String) ChoiceFormat}{@code (subformatPattern)}
 * </table>
 *
 * <h4>使用信息</h4>
 *
 * <p>
 * 以下是一些使用示例。
 * 在实际的国际化程序中，消息格式模式和其他静态字符串当然将从资源束中获取。其他参数将在运行时动态确定。
 * <p>
 * 第一个示例使用静态方法 <code>MessageFormat.format</code>，该方法内部创建一个 <code>MessageFormat</code> 用于一次性使用：
 * <blockquote><pre>
 * int planet = 7;
 * String event = "a disturbance in the Force";
 *
 * String result = MessageFormat.format(
 *     "At {1,time} on {1,date}, there was {2} on planet {0,number,integer}.",
 *     planet, new Date(), event);
 * </pre></blockquote>
 * 输出是：
 * <blockquote><pre>
 * At 12:30 PM on Jul 3, 2053, there was a disturbance in the Force on planet 7.
 * </pre></blockquote>
 *
 * <p>
 * 下一个示例创建一个可以重复使用的 <code>MessageFormat</code> 实例：
 * <blockquote><pre>
 * int fileCount = 1273;
 * String diskName = "MyDisk";
 * Object[] testArgs = {new Long(fileCount), diskName};
 *
 * MessageFormat form = new MessageFormat(
 *     "The disk \"{1}\" contains {0} file(s).");
 *
 * System.out.println(form.format(testArgs));
 * </pre></blockquote>
 * 使用不同的 <code>fileCount</code> 值时的输出：
 * <blockquote><pre>
 * The disk "MyDisk" contains 0 file(s).
 * The disk "MyDisk" contains 1 file(s).
 * The disk "MyDisk" contains 1,273 file(s).
 * </pre></blockquote>
 *
 * <p>
 * 对于更复杂的模式，您可以使用 <code>ChoiceFormat</code> 来生成单数和复数的正确形式：
 * <blockquote><pre>
 * MessageFormat form = new MessageFormat("The disk \"{1}\" contains {0}.");
 * double[] filelimits = {0,1,2};
 * String[] filepart = {"no files","one file","{0,number} files"};
 * ChoiceFormat fileform = new ChoiceFormat(filelimits, filepart);
 * form.setFormatByArgumentIndex(0, fileform);
 *
 * int fileCount = 1273;
 * String diskName = "MyDisk";
 * Object[] testArgs = {new Long(fileCount), diskName};
 *
 * System.out.println(form.format(testArgs));
 * </pre></blockquote>
 * 使用不同的 <code>fileCount</code> 值时的输出：
 * <blockquote><pre>
 * The disk "MyDisk" contains no files.
 * The disk "MyDisk" contains one file.
 * The disk "MyDisk" contains 1,273 files.
 * </pre></blockquote>
 *
 * <p>
 * 您可以像上述示例那样编程创建 <code>ChoiceFormat</code>，也可以使用模式创建。有关更多信息，请参阅 {@link ChoiceFormat}。
 * <blockquote><pre>{@code
 * form.applyPattern(
 *    "There {0,choice,0#are no files|1#is one file|1<are {0,number,integer} files}.");
 * }</pre></blockquote>
 *
 * <p>
 * <strong>注意：</strong> 如上所述，<code>MessageFormat</code> 中 <code>ChoiceFormat</code> 生成的字符串被视为特殊；
 * 出现 '{' 用于指示子格式，并导致递归。
 * 如果您编程创建了 <code>MessageFormat</code> 和 <code>ChoiceFormat</code>（而不是使用字符串模式），则要小心不要
 * 生成一个递归自身的格式，这将导致无限循环。
 * <p>
 * 当单个参数在字符串中被解析多次时，最后一次匹配将是解析的最终结果。例如，
 * <blockquote><pre>
 * MessageFormat mf = new MessageFormat("{0,number,#.##}, {0,number,#.#}");
 * Object[] objs = {new Double(3.1415)};
 * String result = mf.format( objs );
 * // result 现在等于 "3.14, 3.1"
 * objs = null;
 * objs = mf.parse(result, new ParsePosition(0));
 * // objs 现在等于 {new Double(3.1)}
 * </pre></blockquote>
 *
 * <p>
 * 同样，使用包含多个相同参数出现的模式的 {@code MessageFormat} 对象进行解析时，返回的将是最后一次匹配。例如，
 * <blockquote><pre>
 * MessageFormat mf = new MessageFormat("{0}, {0}, {0}");
 * String forParsing = "x, y, z";
 * Object[] objs = mf.parse(forParsing, new ParsePosition(0));
 * // result 现在等于 {new String("z")}
 * </pre></blockquote>
 *
 * <h4><a name="synchronization">同步</a></h4>
 *
 * <p>
 * 消息格式不是同步的。
 * 建议为每个线程创建单独的格式实例。
 * 如果多个线程同时访问格式，则必须外部同步。
 *
 * @see          java.util.Locale
 * @see          Format
 * @see          NumberFormat
 * @see          DecimalFormat
 * @see          DecimalFormatSymbols
 * @see          ChoiceFormat
 * @see          DateFormat
 * @see          SimpleDateFormat
 *
 * @author       Mark Davis
 */


public class MessageFormat extends Format {

    private static final long serialVersionUID = 6479157306784022952L;

    /**
     * 构造一个用于默认
     * {@link java.util.Locale.Category#FORMAT FORMAT} 语言环境和
     * 指定模式的消息格式。
     * 构造函数首先设置语言环境，然后解析模式并
     * 为模式中包含的格式元素创建子格式列表。
     * 模式及其解释在
     * <a href="#patterns">类描述</a> 中指定。
     *
     * @param pattern 此消息格式的模式
     * @exception IllegalArgumentException 如果模式无效
     */
    public MessageFormat(String pattern) {
        this.locale = Locale.getDefault(Locale.Category.FORMAT);
        applyPattern(pattern);
    }

    /**
     * 构造一个用于指定语言环境和
     * 模式的消息格式。
     * 构造函数首先设置语言环境，然后解析模式并
     * 为模式中包含的格式元素创建子格式列表。
     * 模式及其解释在
     * <a href="#patterns">类描述</a> 中指定。
     *
     * @param pattern 此消息格式的模式
     * @param locale 此消息格式的语言环境
     * @exception IllegalArgumentException 如果模式无效
     * @since 1.4
     */
    public MessageFormat(String pattern, Locale locale) {
        this.locale = locale;
        applyPattern(pattern);
    }

    /**
     * 设置在创建或比较子格式时使用的语言环境。
     * 这会影响后续调用
     * <ul>
     * <li>到 {@link #applyPattern applyPattern}
     *     和 {@link #toPattern toPattern} 方法，如果格式元素指定了
     *     格式类型，则在 <code>applyPattern</code> 方法中创建子格式，以及
     * <li>到 <code>format</code> 和
     *     {@link #formatToCharacterIterator formatToCharacterIterator} 方法，
     *     如果格式元素未指定格式类型，则在格式化方法中创建子格式。
     * </ul>
     * 已创建的子格式不受影响。
     *
     * @param locale 在创建或比较子格式时使用的语言环境
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * 获取在创建或比较子格式时使用的语言环境。
     *
     * @return 用于创建或比较子格式的语言环境
     */
    public Locale getLocale() {
        return locale;
    }


    /**
     * 设置此消息格式使用的模式。
     * 该方法解析模式并为模式中包含的格式元素创建子格式列表。
     * 模式及其解释在
     * <a href="#patterns">类描述</a> 中指定。
     *
     * @param pattern 此消息格式的模式
     * @exception IllegalArgumentException 如果模式无效
     */
    @SuppressWarnings("fallthrough") // switch 中的 fallthrough 是预期的，抑制它
    public void applyPattern(String pattern) {
            StringBuilder[] segments = new StringBuilder[4];
            // 仅在此处分配 segments[SEG_RAW]。其余部分
            // 按需分配。
            segments[SEG_RAW] = new StringBuilder();

            int part = SEG_RAW;
            int formatNumber = 0;
            boolean inQuote = false;
            int braceStack = 0;
            maxOffset = -1;
            for (int i = 0; i < pattern.length(); ++i) {
                char ch = pattern.charAt(i);
                if (part == SEG_RAW) {
                    if (ch == '\'') {
                        if (i + 1 < pattern.length()
                            && pattern.charAt(i+1) == '\'') {
                            segments[part].append(ch);  // 处理双引号
                            ++i;
                        } else {
                            inQuote = !inQuote;
                        }
                    } else if (ch == '{' && !inQuote) {
                        part = SEG_INDEX;
                        if (segments[SEG_INDEX] == null) {
                            segments[SEG_INDEX] = new StringBuilder();
                        }
                    } else {
                        segments[part].append(ch);
                    }
                } else  {
                    if (inQuote) {              // 在部分中复制引号
                        segments[part].append(ch);
                        if (ch == '\'') {
                            inQuote = false;
                        }
                    } else {
                        switch (ch) {
                        case ',':
                            if (part < SEG_MODIFIER) {
                                if (segments[++part] == null) {
                                    segments[part] = new StringBuilder();
                                }
                            } else {
                                segments[part].append(ch);
                            }
                            break;
                        case '{':
                            ++braceStack;
                            segments[part].append(ch);
                            break;
                        case '}':
                            if (braceStack == 0) {
                                part = SEG_RAW;
                                makeFormat(i, formatNumber, segments);
                                formatNumber++;
                                // 丢弃其他部分
                                segments[SEG_INDEX] = null;
                                segments[SEG_TYPE] = null;
                                segments[SEG_MODIFIER] = null;
                            } else {
                                --braceStack;
                                segments[part].append(ch);
                            }
                            break;
                        case ' ':
                            // 跳过 SEG_TYPE 的任何前导空格。
                            if (part != SEG_TYPE || segments[SEG_TYPE].length() > 0) {
                                segments[part].append(ch);
                            }
                            break;
                        case '\'':
                            inQuote = true;
                            // fall through，因此我们在其他部分中保留引号
                        default:
                            segments[part].append(ch);
                            break;
                        }
                    }
                }
            }
            if (braceStack == 0 && part != 0) {
                maxOffset = -1;
                throw new IllegalArgumentException("模式中的不匹配大括号。");
            }
            this.pattern = segments[0].toString();
    }


    /**
     * 返回表示当前消息格式状态的模式。
     * 该字符串是从内部信息构造的，因此
     * 不一定等于先前应用的模式。
     *
     * @return 表示当前消息格式状态的模式
     */
    public String toPattern() {
        // 以后，使这更具扩展性
        int lastOffset = 0;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i <= maxOffset; ++i) {
            copyAndFixQuotes(pattern, lastOffset, offsets[i], result);
            lastOffset = offsets[i];
            result.append('{').append(argumentNumbers[i]);
            Format fmt = formats[i];
            if (fmt == null) {
                // 什么都不做，字符串格式
            } else if (fmt instanceof NumberFormat) {
                if (fmt.equals(NumberFormat.getInstance(locale))) {
                    result.append(",number");
                } else if (fmt.equals(NumberFormat.getCurrencyInstance(locale))) {
                    result.append(",number,currency");
                } else if (fmt.equals(NumberFormat.getPercentInstance(locale))) {
                    result.append(",number,percent");
                } else if (fmt.equals(NumberFormat.getIntegerInstance(locale))) {
                    result.append(",number,integer");
                } else {
                    if (fmt instanceof DecimalFormat) {
                        result.append(",number,").append(((DecimalFormat)fmt).toPattern());
                    } else if (fmt instanceof ChoiceFormat) {
                        result.append(",choice,").append(((ChoiceFormat)fmt).toPattern());
                    } else {
                        // 未知
                    }
                }
            } else if (fmt instanceof DateFormat) {
                int index;
                for (index = MODIFIER_DEFAULT; index < DATE_TIME_MODIFIERS.length; index++) {
                    DateFormat df = DateFormat.getDateInstance(DATE_TIME_MODIFIERS[index],
                                                               locale);
                    if (fmt.equals(df)) {
                        result.append(",date");
                        break;
                    }
                    df = DateFormat.getTimeInstance(DATE_TIME_MODIFIERS[index],
                                                    locale);
                    if (fmt.equals(df)) {
                        result.append(",time");
                        break;
                    }
                }
                if (index >= DATE_TIME_MODIFIERS.length) {
                    if (fmt instanceof SimpleDateFormat) {
                        result.append(",date,").append(((SimpleDateFormat)fmt).toPattern());
                    } else {
                        // 未知
                    }
                } else if (index != MODIFIER_DEFAULT) {
                    result.append(',').append(DATE_TIME_MODIFIER_KEYWORDS[index]);
                }
            } else {
                //result.append(", unknown");
            }
            result.append('}');
        }
        copyAndFixQuotes(pattern, lastOffset, pattern.length(), result);
        return result.toString();
    }

    /**
     * 设置用于传递给
     * <code>format</code> 方法或从 <code>parse</code>
     * 方法返回的值的格式。 <code>newFormats</code> 中元素的索引
     * 对应于先前设置的模式字符串中使用的参数索引。
     * <code>newFormats</code> 中格式的顺序因此对应于
     * 传递给 <code>format</code> 方法的 <code>arguments</code> 数组的顺序
     * 或由 <code>parse</code> 方法返回的结果数组的顺序。
     * <p>
     * 如果模式字符串中的一个参数索引用于多个格式元素，
     * 则相应的新格式用于所有这些格式元素。如果模式字符串中的一个参数索引未用于任何格式元素，
     * 则相应的格式被忽略。如果提供的格式少于需要的数量，
     * 则仅替换参数索引小于 <code>newFormats.length</code> 的格式。
     *
     * @param newFormats 要使用的新的格式
     * @exception NullPointerException 如果 <code>newFormats</code> 为 null
     * @since 1.4
     */
    public void setFormatsByArgumentIndex(Format[] newFormats) {
        for (int i = 0; i <= maxOffset; i++) {
            int j = argumentNumbers[i];
            if (j < newFormats.length) {
                formats[i] = newFormats[j];
            }
        }
    }

    /**
     * 设置用于先前设置的模式字符串中的格式元素的格式。
     * <code>newFormats</code> 中格式的顺序对应于
     * 模式字符串中格式元素的顺序。
     * <p>
     * 如果提供的格式多于模式字符串需要的数量，
     * 则多余的格式被忽略。如果提供的格式少于需要的数量，
     * 则仅替换前 <code>newFormats.length</code> 个格式。
     * <p>
     * 由于模式字符串中格式元素的顺序在本地化过程中经常变化，
     * 通常最好使用 {@link #setFormatsByArgumentIndex setFormatsByArgumentIndex}
     * 方法，该方法假设格式的顺序对应于传递给
     * <code>format</code> 方法的 <code>arguments</code> 数组的顺序
     * 或由 <code>parse</code> 方法返回的结果数组的顺序。
     *
     * @param newFormats 要使用的新的格式
     * @exception NullPointerException 如果 <code>newFormats</code> 为 null
     */
    public void setFormats(Format[] newFormats) {
        int runsToCopy = newFormats.length;
        if (runsToCopy > maxOffset + 1) {
            runsToCopy = maxOffset + 1;
        }
        for (int i = 0; i < runsToCopy; i++) {
            formats[i] = newFormats[i];
        }
    }

    /**
     * 设置用于先前设置的模式字符串中使用给定参数
     * 索引的格式元素的格式。
     * 参数索引是格式元素定义的一部分，表示传递给
     * <code>format</code> 方法的 <code>arguments</code> 数组的索引
     * 或由 <code>parse</code> 方法返回的结果数组的索引。
     * <p>
     * 如果模式字符串中的一个参数索引用于多个格式元素，
     * 则新的格式用于所有这些格式元素。如果模式字符串中的一个参数索引未用于任何格式元素，
     * 则新的格式被忽略。
     *
     * @param argumentIndex 要使用新格式的参数索引
     * @param newFormat 要使用的新格式
     * @since 1.4
     */
    public void setFormatByArgumentIndex(int argumentIndex, Format newFormat) {
        for (int j = 0; j <= maxOffset; j++) {
            if (argumentNumbers[j] == argumentIndex) {
                formats[j] = newFormat;
            }
        }
    }

    /**
     * 设置用于先前设置的模式字符串中具有给定格式元素索引的格式元素的格式。
     * 格式元素索引是从模式字符串开头开始的格式元素的零基编号。
     * <p>
     * 由于模式字符串中格式元素的顺序在本地化过程中经常变化，
     * 通常最好使用 {@link #setFormatByArgumentIndex setFormatByArgumentIndex}
     * 方法，该方法基于它们指定的参数索引访问格式元素。
     *
     * @param formatElementIndex 模式中的格式元素索引
     * @param newFormat 要用于指定格式元素的格式
     * @exception ArrayIndexOutOfBoundsException 如果 {@code formatElementIndex} 等于或
     *            大于模式字符串中的格式元素数量
     */
    public void setFormat(int formatElementIndex, Format newFormat) {
        formats[formatElementIndex] = newFormat;
    }

    /**
     * 获取用于传递给
     * <code>format</code> 方法或从 <code>parse</code>
     * 方法返回的值的格式。返回数组中元素的索引
     * 对应于先前设置的模式字符串中使用的参数索引。
     * 返回数组中格式的顺序因此对应于
     * 传递给 <code>format</code> 方法的 <code>arguments</code> 数组的顺序
     * 或由 <code>parse</code> 方法返回的结果数组的顺序。
     * <p>
     * 如果模式字符串中的一个参数索引用于多个格式元素，
     * 则返回数组中返回用于最后一个这样的格式元素的格式。如果模式字符串中的一个参数索引未用于任何格式元素，
     * 则返回数组中返回 null。
     *
     * @return 用于模式中参数的格式
     * @since 1.4
     */
    public Format[] getFormatsByArgumentIndex() {
        int maximumArgumentNumber = -1;
        for (int i = 0; i <= maxOffset; i++) {
            if (argumentNumbers[i] > maximumArgumentNumber) {
                maximumArgumentNumber = argumentNumbers[i];
            }
        }
        Format[] resultArray = new Format[maximumArgumentNumber + 1];
        for (int i = 0; i <= maxOffset; i++) {
            resultArray[argumentNumbers[i]] = formats[i];
        }
        return resultArray;
    }
}


                /**
     * 获取格式化元素在先前设置的模式字符串中使用的格式。
     * 返回数组中的格式顺序对应于模式字符串中格式化元素的顺序。
     * <p>
     * 由于模式字符串中格式化元素的顺序在本地化过程中经常发生变化，因此通常最好使用
     * {@link #getFormatsByArgumentIndex getFormatsByArgumentIndex} 方法，该方法假设格式的顺序对应于
     * 传递给 <code>format</code> 方法或由 <code>parse</code> 方法返回的结果数组中的元素顺序。
     *
     * @return 模式中格式化元素使用的格式。
     */
    public Format[] getFormats() {
        Format[] resultArray = new Format[maxOffset + 1];
        System.arraycopy(formats, 0, resultArray, 0, maxOffset + 1);
        return resultArray;
    }

    /**
     * 格式化对象数组，并将 <code>MessageFormat</code> 的模式（格式化元素被格式化对象替换）追加到提供的 <code>StringBuffer</code> 中。
     * <p>
     * 替换各个格式化元素的文本是从格式化元素的当前子格式和 <code>arguments</code> 数组中格式化元素的参数索引处的元素派生的，
     * 具体取决于以下表格中的第一匹配行。如果 <code>arguments</code> 为 <code>null</code> 或者元素少于 argumentIndex+1，则参数 <i>不可用</i>。
     *
     * <table border=1 summary="Examples of subformat,argument,and formatted text">
     *    <tr>
     *       <th>子格式
     *       <th>参数
     *       <th>格式化文本
     *    <tr>
     *       <td><i>任何</i>
     *       <td><i>不可用</i>
     *       <td><code>"{" + argumentIndex + "}"</code>
     *    <tr>
     *       <td><i>任何</i>
     *       <td><code>null</code>
     *       <td><code>"null"</code>
     *    <tr>
     *       <td><code>instanceof ChoiceFormat</code>
     *       <td><i>任何</i>
     *       <td><code>subformat.format(argument).indexOf('{') &gt;= 0 ?<br>
     *           (new MessageFormat(subformat.format(argument), getLocale())).format(argument) :
     *           subformat.format(argument)</code>
     *    <tr>
     *       <td><code>!= null</code>
     *       <td><i>任何</i>
     *       <td><code>subformat.format(argument)</code>
     *    <tr>
     *       <td><code>null</code>
     *       <td><code>instanceof Number</code>
     *       <td><code>NumberFormat.getInstance(getLocale()).format(argument)</code>
     *    <tr>
     *       <td><code>null</code>
     *       <td><code>instanceof Date</code>
     *       <td><code>DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getLocale()).format(argument)</code>
     *    <tr>
     *       <td><code>null</code>
     *       <td><code>instanceof String</code>
     *       <td><code>argument</code>
     *    <tr>
     *       <td><code>null</code>
     *       <td><i>任何</i>
     *       <td><code>argument.toString()</code>
     * </table>
     * <p>
     * 如果 <code>pos</code> 非空，并且引用 <code>Field.ARGUMENT</code>，则返回第一个格式化字符串的位置。
     *
     * @param arguments 要格式化和替换的对象数组。
     * @param result 追加文本的位置。
     * @param pos 输入：对齐字段，如果需要的话。
     *            输出：对齐字段的偏移量。
     * @return 作为 {@code result} 传递的字符串缓冲区，附加了格式化文本。
     * @exception IllegalArgumentException 如果 <code>arguments</code> 数组中的参数不是格式化元素使用的预期类型。
     */
    public final StringBuffer format(Object[] arguments, StringBuffer result,
                                     FieldPosition pos)
    {
        return subformat(arguments, result, pos, null);
    }

    /**
     * 使用给定的模式创建一个 <code>MessageFormat</code>，并使用它来格式化给定的参数。这等同于
     * <blockquote>
     *     <code>(new {@link #MessageFormat(String) MessageFormat}(pattern)).{@link #format(java.lang.Object[], java.lang.StringBuffer, java.text.FieldPosition) format}(arguments, new StringBuffer(), null).toString()</code>
     * </blockquote>
     *
     * @param pattern 模式字符串
     * @param arguments 要格式化的对象
     * @return 格式化后的字符串
     * @exception IllegalArgumentException 如果模式无效，或者 <code>arguments</code> 数组中的参数不是格式化元素使用的预期类型。
     */
    public static String format(String pattern, Object ... arguments) {
        MessageFormat temp = new MessageFormat(pattern);
        return temp.format(arguments);
    }

    // Overrides
    /**
     * 格式化对象数组，并将 <code>MessageFormat</code> 的模式（格式化元素被格式化对象替换）追加到提供的 <code>StringBuffer</code> 中。
     * 这等同于
     * <blockquote>
     *     <code>{@link #format(java.lang.Object[], java.lang.StringBuffer, java.text.FieldPosition) format}((Object[]) arguments, result, pos)</code>
     * </blockquote>
     *
     * @param arguments 要格式化和替换的对象数组。
     * @param result 追加文本的位置。
     * @param pos 输入：对齐字段，如果需要的话。
     *            输出：对齐字段的偏移量。
     * @exception IllegalArgumentException 如果 <code>arguments</code> 数组中的参数不是格式化元素使用的预期类型。
     */
    public final StringBuffer format(Object arguments, StringBuffer result,
                                     FieldPosition pos)
    {
        return subformat((Object[]) arguments, result, pos, null);
    }

    /**
     * 格式化对象数组，并将它们插入到 <code>MessageFormat</code> 的模式中，生成一个 <code>AttributedCharacterIterator</code>。
     * 可以使用返回的 <code>AttributedCharacterIterator</code> 构建结果字符串，以及确定结果字符串的信息。
     * <p>
     * 返回的 <code>AttributedCharacterIterator</code> 的文本与
     * <blockquote>
     *     <code>{@link #format(java.lang.Object[], java.lang.StringBuffer, java.text.FieldPosition) format}(arguments, new StringBuffer(), null).toString()</code>
     * </blockquote>
     * 返回的文本相同。
     * <p>
     * 此外，<code>AttributedCharacterIterator</code> 包含至少指示文本从 <code>arguments</code> 数组中的参数生成的位置的属性。这些属性的键是 <code>MessageFormat.Field</code> 类型，其值是 <code>Integer</code> 对象，指示生成文本的参数在 <code>arguments</code> 数组中的索引。
     * <p>
     * <code>MessageFormat</code> 使用的底层 <code>Format</code> 实例的属性/值也将被放置在结果的 <code>AttributedCharacterIterator</code> 中。这不仅允许您找到参数在结果字符串中的位置，还可以找到它们包含的字段。
     *
     * @param arguments 要格式化和替换的对象数组。
     * @return 描述格式化值的 <code>AttributedCharacterIterator</code>。
     * @exception NullPointerException 如果 <code>arguments</code> 为 null。
     * @exception IllegalArgumentException 如果 <code>arguments</code> 数组中的参数不是格式化元素使用的预期类型。
     * @since 1.4
     */
    public AttributedCharacterIterator formatToCharacterIterator(Object arguments) {
        StringBuffer result = new StringBuffer();
        ArrayList<AttributedCharacterIterator> iterators = new ArrayList<>();

        if (arguments == null) {
            throw new NullPointerException(
                   "formatToCharacterIterator 必须传递非空对象");
        }
        subformat((Object[]) arguments, result, null, iterators);
        if (iterators.size() == 0) {
            return createAttributedCharacterIterator("");
        }
        return createAttributedCharacterIterator(
                     iterators.toArray(
                     new AttributedCharacterIterator[iterators.size()]));
    }

    /**
     * 解析字符串。
     *
     * <p>注意事项：解析可能在多种情况下失败。例如：
     * <ul>
     * <li>如果参数之一不在模式中。
     * <li>如果参数的格式丢失信息，例如，使用选择格式将大数字格式化为 "many"。
     * <li>尚未处理递归（其中替换的字符串包含 {n} 引用）。
     * <li>如果解析的某些部分是模棱两可的，则可能无法找到匹配项（或正确的匹配项）。
     *     例如，如果使用模式 "{1},{2}" 与字符串参数 {"a,b", "c"}，它将格式化为 "a,b,c"。
     *     当结果被解析时，它将返回 {"a", "b,c"}。
     * <li>如果字符串中多次解析同一个参数，则后一次解析获胜。
     * </ul>
     * 当解析失败时，使用 ParsePosition.getErrorIndex() 找出字符串中解析失败的位置。返回的错误索引是字符串比较的子模式的起始偏移量。例如，如果解析字符串 "AAA {0} BBB" 与模式 "AAD {0} BBB" 进行比较，错误索引为 0。当发生错误时，调用此方法将返回 null。如果源为 null，则返回空数组。
     *
     * @param source 要解析的字符串
     * @param pos 解析位置
     * @return 解析后的对象数组
     */
    public Object[] parse(String source, ParsePosition pos) {
        if (source == null) {
            Object[] empty = {};
            return empty;
        }

        int maximumArgumentNumber = -1;
        for (int i = 0; i <= maxOffset; i++) {
            if (argumentNumbers[i] > maximumArgumentNumber) {
                maximumArgumentNumber = argumentNumbers[i];
            }
        }

        // 构造函数/applyPattern 确保 resultArray.length < MAX_ARGUMENT_INDEX
        Object[] resultArray = new Object[maximumArgumentNumber + 1];

        int patternOffset = 0;
        int sourceOffset = pos.index;
        ParsePosition tempStatus = new ParsePosition(0);
        for (int i = 0; i <= maxOffset; ++i) {
            // 匹配到格式
            int len = offsets[i] - patternOffset;
            if (len == 0 || pattern.regionMatches(patternOffset,
                                                  source, sourceOffset, len)) {
                sourceOffset += len;
                patternOffset += len;
            } else {
                pos.errorIndex = sourceOffset;
                return null; // 保持索引不变以指示错误
            }

            // 现在使用格式
            if (formats[i] == null) {   // 字符串格式
                // 如果在末尾，使用最长的可能匹配
                // 否则使用到下一个字符串的第一次匹配
                // 不递归尝试所有可能性
                int tempLength = (i != maxOffset) ? offsets[i+1] : pattern.length();

                int next;
                if (patternOffset >= tempLength) {
                    next = source.length();
                }else{
                    next = source.indexOf(pattern.substring(patternOffset, tempLength),
                                          sourceOffset);
                }

                if (next < 0) {
                    pos.errorIndex = sourceOffset;
                    return null; // 保持索引不变以指示错误
                } else {
                    String strValue= source.substring(sourceOffset,next);
                    if (!strValue.equals("{"+argumentNumbers[i]+"}"))
                        resultArray[argumentNumbers[i]]
                            = source.substring(sourceOffset,next);
                    sourceOffset = next;
                }
            } else {
                tempStatus.index = sourceOffset;
                resultArray[argumentNumbers[i]]
                    = formats[i].parseObject(source,tempStatus);
                if (tempStatus.index == sourceOffset) {
                    pos.errorIndex = sourceOffset;
                    return null; // 保持索引不变以指示错误
                }
                sourceOffset = tempStatus.index; // 更新
            }
        }
        int len = pattern.length() - patternOffset;
        if (len == 0 || pattern.regionMatches(patternOffset,
                                              source, sourceOffset, len)) {
            pos.index = sourceOffset + len;
        } else {
            pos.errorIndex = sourceOffset;
            return null; // 保持索引不变以指示错误
        }
        return resultArray;
    }

    /**
     * 从给定字符串的开头解析文本以生成对象数组。
     * 该方法可能不会使用给定字符串的全部文本。
     * <p>
     * 有关消息解析的更多信息，请参阅 {@link #parse(String, ParsePosition)} 方法。
     *
     * @param source 一个 <code>String</code>，其开头应被解析。
     * @return 从字符串解析的对象数组。
     * @exception ParseException 如果指定字符串的开头无法解析。
     */
    public Object[] parse(String source) throws ParseException {
        ParsePosition pos  = new ParsePosition(0);
        Object[] result = parse(source, pos);
        if (pos.index == 0)  // 未更改，返回的对象为 null
            throw new ParseException("MessageFormat 解析错误！", pos.errorIndex);

        return result;
    }

    /**
     * 从字符串中解析文本以生成对象数组。
     * <p>
     * 该方法尝试从 <code>pos</code> 给定的索引开始解析文本。
     * 如果解析成功，则 <code>pos</code> 的索引将更新为最后一个使用字符之后的索引（解析不一定使用字符串末尾之前的所有字符），并返回解析的对象数组。更新后的 <code>pos</code> 可用于指示下一次调用此方法的起始点。
     * 如果发生错误，则 <code>pos</code> 的索引不会更改，<code>pos</code> 的错误索引将设置为错误发生处的字符索引，并返回 null。
     * <p>
     * 有关消息解析的更多信息，请参阅 {@link #parse(String, ParsePosition)} 方法。
     *
     * @param source 一个 <code>String</code>，其部分应被解析。
     * @param pos 一个 <code>ParsePosition</code> 对象，包含上述的索引和错误索引信息。
     * @return 从字符串解析的对象数组。在错误情况下，返回 null。
     * @exception NullPointerException 如果 <code>pos</code> 为 null。
     */
    public Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }


                /**
                 * 创建并返回此对象的副本。
                 *
                 * @return 此实例的克隆。
                 */
                public Object clone() {
                    MessageFormat other = (MessageFormat) super.clone();

                    // 由于Cloneable中的错误，不能使用工具进行克隆
                    other.formats = formats.clone(); // 浅克隆
                    for (int i = 0; i < formats.length; ++i) {
                        if (formats[i] != null)
                            other.formats[i] = (Format)formats[i].clone();
                    }
                    // 对于原始类型或不可变类型，浅克隆就足够了
                    other.offsets = offsets.clone();
                    other.argumentNumbers = argumentNumbers.clone();

                    return other;
                }

                /**
                 * 两个消息格式对象之间的相等性比较。
                 */
                public boolean equals(Object obj) {
                    if (this == obj)                      // 快速检查
                        return true;
                    if (obj == null || getClass() != obj.getClass())
                        return false;
                    MessageFormat other = (MessageFormat) obj;
                    return (maxOffset == other.maxOffset
                            && pattern.equals(other.pattern)
                            && ((locale != null && locale.equals(other.locale))
                             || (locale == null && other.locale == null))
                            && Arrays.equals(offsets,other.offsets)
                            && Arrays.equals(argumentNumbers,other.argumentNumbers)
                            && Arrays.equals(formats,other.formats));
                }

                /**
                 * 为消息格式对象生成哈希码。
                 */
                public int hashCode() {
                    return pattern.hashCode(); // 足够用于合理的分布
                }


                /**
                 * 定义在从 <code>MessageFormat.formatToCharacterIterator</code> 返回的
                 * <code>AttributedCharacterIterator</code> 中用作属性键的常量。
                 *
                 * @since 1.4
                 */
                public static class Field extends Format.Field {

                    // 声明与1.4 FCS的序列化兼容性
                    private static final long serialVersionUID = 7899943957617360810L;

                    /**
                     * 使用指定名称创建一个Field。
                     *
                     * @param name 属性的名称
                     */
                    protected Field(String name) {
                        super(name);
                    }

                    /**
                     * 将正在反序列化的实例解析为预定义的常量。
                     *
                     * @throws InvalidObjectException 如果常量无法解析。
                     * @return 解析后的 MessageFormat.Field 常量
                     */
                    protected Object readResolve() throws InvalidObjectException {
                        if (this.getClass() != MessageFormat.Field.class) {
                            throw new InvalidObjectException("子类没有正确实现 readResolve");
                        }

                        return ARGUMENT;
                    }

                    //
                    // 常量
                    //

                    /**
                     * 从 <code>formatToCharacterIterator</code> 传递的参数生成的消息部分的标识符。
                     * 与键关联的值将是一个 <code>Integer</code>，表示生成文本的参数在 <code>arguments</code> 数组中的索引。
                     */
                    public final static Field ARGUMENT =
                                   new Field("message argument field");
                }

                // ===========================私有成员============================

                /**
                 * 用于格式化数字和日期的区域设置。
                 * @serial
                 */
                private Locale locale;

                /**
                 * 要插入格式化值的字符串。换句话说，这是构造时提供的模式，去除了所有的 {} 表达式。
                 * @serial
                 */
                private String pattern = "";

                /** 格式化器最初预期的数量 */
                private static final int INITIAL_FORMATS = 10;

                /**
                 * 用于格式化参数的格式化器数组。
                 * @serial
                 */
                private Format[] formats = new Format[INITIAL_FORMATS];

                /**
                 * 将格式化每个参数的结果插入到模式中的位置。
                 * @serial
                 */
                private int[] offsets = new int[INITIAL_FORMATS];

                /**
                 * 与每个格式化器对应的参数编号。格式化器存储的顺序是它们在模式中出现的顺序，而不是参数指定的顺序。
                 * @serial
                 */
                private int[] argumentNumbers = new int[INITIAL_FORMATS];
                // ArgumentIndex 模式元素的实现限制。有效索引必须小于该值
                private static final int MAX_ARGUMENT_INDEX = 10000;

                /**
                 * <code>offsets</code> 中使用的条目数减一。也可以认为是 <code>offsets</code> 中最高编号元素的索引。
                 * 所有这些数组使用的条目数应与 <code>offsets</code> 相同，因此该变量足以告诉我们所有数组中有多少条目。
                 * @serial
                 */
                private int maxOffset = -1;

                /**
                 * 由 format 使用的内部例程。如果 <code>characterIterators</code> 非空，则将根据需要从子格式化器创建 AttributedCharacterIterator。
                 * 如果 <code>characterIterators</code> 为空且 <code>fp</code> 非空并标识 <code>Field.MESSAGE_ARGUMENT</code>，
                 * 则将在其中设置第一个被替换参数的位置。
                 *
                 * @exception IllegalArgumentException 如果 <code>arguments</code> 数组中的参数不是格式元素使用的类型。
                 */
                private StringBuffer subformat(Object[] arguments, StringBuffer result,
                                               FieldPosition fp, List<AttributedCharacterIterator> characterIterators) {
                    // 注意：此实现假设 substring 和 index 快速。如果不是这样，最好逐个追加字符。
                    int lastOffset = 0;
                    int last = result.length();
                    for (int i = 0; i <= maxOffset; ++i) {
                        result.append(pattern.substring(lastOffset, offsets[i]));
                        lastOffset = offsets[i];
                        int argumentNumber = argumentNumbers[i];
                        if (arguments == null || argumentNumber >= arguments.length) {
                            result.append('{').append(argumentNumber).append('}');
                            continue;
                        }
                        // int argRecursion = ((recursionProtection >> (argumentNumber*2)) & 0x3);
                        if (false) { // if (argRecursion == 3){
                            // 防止循环！
                            result.append('\uFFFD');
                        } else {
                            Object obj = arguments[argumentNumber];
                            String arg = null;
                            Format subFormatter = null;
                            if (obj == null) {
                                arg = "null";
                            } else if (formats[i] != null) {
                                subFormatter = formats[i];
                                if (subFormatter instanceof ChoiceFormat) {
                                    arg = formats[i].format(obj);
                                    if (arg.indexOf('{') >= 0) {
                                        subFormatter = new MessageFormat(arg, locale);
                                        obj = arguments;
                                        arg = null;
                                    }
                                }
                            } else if (obj instanceof Number) {
                                // 如果可以，格式化数字
                                subFormatter = NumberFormat.getInstance(locale);
                            } else if (obj instanceof Date) {
                                // 如果可以，格式化日期
                                subFormatter = DateFormat.getDateTimeInstance(
                                         DateFormat.SHORT, DateFormat.SHORT, locale);//fix
                            } else if (obj instanceof String) {
                                arg = (String) obj;

                            } else {
                                arg = obj.toString();
                                if (arg == null) arg = "null";
                            }

                            // 此时我们处于两种状态之一，要么 subFormatter 非空，表示我们应该使用它来格式化 obj，
                            // 要么 arg 非空，我们应该使用它作为值。

                            if (characterIterators != null) {
                                // 如果 characterIterators 非空，表示我们需要从子格式化器获取 CharacterIterator。
                                if (last != result.length()) {
                                    characterIterators.add(
                                        createAttributedCharacterIterator(result.substring
                                                                          (last)));
                                    last = result.length();
                                }
                                if (subFormatter != null) {
                                    AttributedCharacterIterator subIterator =
                                               subFormatter.formatToCharacterIterator(obj);

                                    append(result, subIterator);
                                    if (last != result.length()) {
                                        characterIterators.add(
                                                     createAttributedCharacterIterator(
                                                     subIterator, Field.ARGUMENT,
                                                     Integer.valueOf(argumentNumber)));
                                        last = result.length();
                                    }
                                    arg = null;
                                }
                                if (arg != null && arg.length() > 0) {
                                    result.append(arg);
                                    characterIterators.add(
                                             createAttributedCharacterIterator(
                                             arg, Field.ARGUMENT,
                                             Integer.valueOf(argumentNumber)));
                                    last = result.length();
                                }
                            }
                            else {
                                if (subFormatter != null) {
                                    arg = subFormatter.format(obj);
                                }
                                last = result.length();
                                result.append(arg);
                                if (i == 0 && fp != null && Field.ARGUMENT.equals(
                                              fp.getFieldAttribute())) {
                                    fp.setBeginIndex(last);
                                    fp.setEndIndex(result.length());
                                }
                                last = result.length();
                            }
                        }
                    }
                    result.append(pattern.substring(lastOffset, pattern.length()));
                    if (characterIterators != null && last != result.length()) {
                        characterIterators.add(createAttributedCharacterIterator(
                                               result.substring(last)));
                    }
                    return result;
                }

                /**
                 * 便利方法，将 <code>iterator</code> 中的所有字符追加到 StringBuffer <code>result</code> 中。
                 */
                private void append(StringBuffer result, CharacterIterator iterator) {
                    if (iterator.first() != CharacterIterator.DONE) {
                        char aChar;

                        result.append(iterator.first());
                        while ((aChar = iterator.next()) != CharacterIterator.DONE) {
                            result.append(aChar);
                        }
                    }
                }

                // 段的索引
                private static final int SEG_RAW      = 0;
                private static final int SEG_INDEX    = 1;
                private static final int SEG_TYPE     = 2;
                private static final int SEG_MODIFIER = 3; // 修改器或子格式

                // 类型关键字的索引
                private static final int TYPE_NULL    = 0;
                private static final int TYPE_NUMBER  = 1;
                private static final int TYPE_DATE    = 2;
                private static final int TYPE_TIME    = 3;
                private static final int TYPE_CHOICE  = 4;

                private static final String[] TYPE_KEYWORDS = {
                    "",
                    "number",
                    "date",
                    "time",
                    "choice"
                };

                // 数字修改器的索引
                private static final int MODIFIER_DEFAULT  = 0; // 数字和日期时间中常见
                private static final int MODIFIER_CURRENCY = 1;
                private static final int MODIFIER_PERCENT  = 2;
                private static final int MODIFIER_INTEGER  = 3;

                private static final String[] NUMBER_MODIFIER_KEYWORDS = {
                    "",
                    "currency",
                    "percent",
                    "integer"
                };

                // 日期时间修改器的索引
                private static final int MODIFIER_SHORT   = 1;
                private static final int MODIFIER_MEDIUM  = 2;
                private static final int MODIFIER_LONG    = 3;
                private static final int MODIFIER_FULL    = 4;

                private static final String[] DATE_TIME_MODIFIER_KEYWORDS = {
                    "",
                    "short",
                    "medium",
                    "long",
                    "full"
                };

                // 与日期时间修改器对应的日期时间样式值。
                private static final int[] DATE_TIME_MODIFIERS = {
                    DateFormat.DEFAULT,
                    DateFormat.SHORT,
                    DateFormat.MEDIUM,
                    DateFormat.LONG,
                    DateFormat.FULL,
                };

                private void makeFormat(int position, int offsetNumber,
                                        StringBuilder[] textSegments)
                {
                    String[] segments = new String[textSegments.length];
                    for (int i = 0; i < textSegments.length; i++) {
                        StringBuilder oneseg = textSegments[i];
                        segments[i] = (oneseg != null) ? oneseg.toString() : "";
                    }

                    // 获取参数编号
                    int argumentNumber;
                    try {
                        argumentNumber = Integer.parseInt(segments[SEG_INDEX]); // 始终未本地化！
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("无法解析参数编号: "
                                                         + segments[SEG_INDEX], e);
                    }
                    if (argumentNumber < 0) {
                        throw new IllegalArgumentException("负参数编号: "
                                                         + argumentNumber);
                    }

                    if (argumentNumber >= MAX_ARGUMENT_INDEX) {
                        throw new IllegalArgumentException(
                                argumentNumber + " 超过 ArgumentIndex 实现限制");
                    }

                    // 如果必要，调整格式信息数组的大小
                    if (offsetNumber >= formats.length) {
                        int newLength = formats.length * 2;
                        Format[] newFormats = new Format[newLength];
                        int[] newOffsets = new int[newLength];
                        int[] newArgumentNumbers = new int[newLength];
                        System.arraycopy(formats, 0, newFormats, 0, maxOffset + 1);
                        System.arraycopy(offsets, 0, newOffsets, 0, maxOffset + 1);
                        System.arraycopy(argumentNumbers, 0, newArgumentNumbers, 0, maxOffset + 1);
                        formats = newFormats;
                        offsets = newOffsets;
                        argumentNumbers = newArgumentNumbers;
                    }
                    int oldMaxOffset = maxOffset;
                    maxOffset = offsetNumber;
                    offsets[offsetNumber] = segments[SEG_RAW].length();
                    argumentNumbers[offsetNumber] = argumentNumber;

                    // 现在获取格式
                    Format newFormat = null;
                    if (segments[SEG_TYPE].length() != 0) {
                        int type = findKeyword(segments[SEG_TYPE], TYPE_KEYWORDS);
                        switch (type) {
                        case TYPE_NULL:
                            // 类型 "" 是允许的。例如，"{0,}"、"{0,,}" 和 "{0,,#}" 被视为 "{0}"。
                            break;


                        case TYPE_NUMBER:
                switch (findKeyword(segments[SEG_MODIFIER], NUMBER_MODIFIER_KEYWORDS)) {
                case MODIFIER_DEFAULT:
                    newFormat = NumberFormat.getInstance(locale);
                    break;
                case MODIFIER_CURRENCY:
                    newFormat = NumberFormat.getCurrencyInstance(locale);
                    break;
                case MODIFIER_PERCENT:
                    newFormat = NumberFormat.getPercentInstance(locale);
                    break;
                case MODIFIER_INTEGER:
                    newFormat = NumberFormat.getIntegerInstance(locale);
                    break;
                default: // DecimalFormat pattern
                    try {
                        newFormat = new DecimalFormat(segments[SEG_MODIFIER],
                                                      DecimalFormatSymbols.getInstance(locale));
                    } catch (IllegalArgumentException e) {
                        maxOffset = oldMaxOffset;
                        throw e;
                    }
                    break;
                }
                break;

            case TYPE_DATE:
            case TYPE_TIME:
                int mod = findKeyword(segments[SEG_MODIFIER], DATE_TIME_MODIFIER_KEYWORDS);
                if (mod >= 0 && mod < DATE_TIME_MODIFIER_KEYWORDS.length) {
                    if (type == TYPE_DATE) {
                        newFormat = DateFormat.getDateInstance(DATE_TIME_MODIFIERS[mod],
                                                               locale);
                    } else {
                        newFormat = DateFormat.getTimeInstance(DATE_TIME_MODIFIERS[mod],
                                                               locale);
                    }
                } else {
                    // SimpleDateFormat pattern
                    try {
                        newFormat = new SimpleDateFormat(segments[SEG_MODIFIER], locale);
                    } catch (IllegalArgumentException e) {
                        maxOffset = oldMaxOffset;
                        throw e;
                    }
                }
                break;

            case TYPE_CHOICE:
                try {
                    // ChoiceFormat pattern
                    newFormat = new ChoiceFormat(segments[SEG_MODIFIER]);
                } catch (Exception e) {
                    maxOffset = oldMaxOffset;
                    throw new IllegalArgumentException("Choice Pattern incorrect: "
                                                       + segments[SEG_MODIFIER], e);
                }
                break;

            default:
                maxOffset = oldMaxOffset;
                throw new IllegalArgumentException("未知格式类型: " +
                                                   segments[SEG_TYPE]);
            }
        }
        formats[offsetNumber] = newFormat;
    }

    private static final int findKeyword(String s, String[] list) {
        for (int i = 0; i < list.length; ++i) {
            if (s.equals(list[i]))
                return i;
        }

        // 尝试去除空格并转换为小写。
        String ls = s.trim().toLowerCase(Locale.ROOT);
        if (ls != s) {
            for (int i = 0; i < list.length; ++i) {
                if (ls.equals(list[i]))
                    return i;
            }
        }
        return -1;
    }

    private static final void copyAndFixQuotes(String source, int start, int end,
                                               StringBuilder target) {
        boolean quoted = false;

        for (int i = start; i < end; ++i) {
            char ch = source.charAt(i);
            if (ch == '{') {
                if (!quoted) {
                    target.append('\'');
                    quoted = true;
                }
                target.append(ch);
            } else if (ch == '\'') {
                target.append("''");
            } else {
                if (quoted) {
                    target.append('\'');
                    quoted = false;
                }
                target.append(ch);
            }
        }
        if (quoted) {
            target.append('\'');
        }
    }

    /**
     * 从输入流中读取对象后，进行简单的验证以保持类的不变性。
     * @throws InvalidObjectException 如果从流中读取的对象无效。
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        if (fields.defaulted("argumentNumbers") || fields.defaulted("offsets")
                || fields.defaulted("formats") || fields.defaulted("locale")
                || fields.defaulted("pattern") || fields.defaulted("maxOffset")){
            throw new InvalidObjectException("流中缺少数据");
        }

        locale = (Locale) fields.get("locale", null);
        String patt = (String) fields.get("pattern", null);
        int maxOff = fields.get("maxOffset", -2);
        int[] argNums = ((int[]) fields.get("argumentNumbers", null)).clone();
        int[] offs = ((int[]) fields.get("offsets", null)).clone();
        Format[] fmts = ((Format[]) fields.get("formats", null)).clone();

        // 检查数组和 maxOffset 是否有正确的值/长度
        boolean isValid = maxOff >= -1 && argNums.length > maxOff
                && offs.length > maxOff && fmts.length > maxOff;

        // 检查参数和偏移量的正确性
        if (isValid) {
            int lastOffset = patt.length() + 1;
            for (int i = maxOff; i >= 0; --i) {
                if (argNums[i] < 0 || argNums[i] >= MAX_ARGUMENT_INDEX
                        || offs[i] < 0 || offs[i] > lastOffset) {
                    isValid = false;
                    break;
                } else {
                    lastOffset = offs[i];
                }
            }
        }

        if (!isValid) {
            throw new InvalidObjectException("流中有无效数据");
        }
        maxOffset = maxOff;
        pattern = patt;
        offsets = offs;
        formats = fmts;
        argumentNumbers = argNums;
    }

    /**
     * 该类不支持无数据的序列化。
     */
    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("反序列化的 MessageFormat 对象需要数据");
    }
}
