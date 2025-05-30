
/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package java.time.format;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.DAY_OF_YEAR;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder.CompositePrinterParser;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 日期时间格式化器，用于打印和解析日期时间对象。
 * <p>
 * 该类提供了打印和解析的主要应用程序入口点，并提供了 {@code DateTimeFormatter} 的常见实现：
 * <ul>
 * <li>使用预定义的常量，例如 {@link #ISO_LOCAL_DATE}</li>
 * <li>使用模式字母，例如 {@code uuuu-MMM-dd}</li>
 * <li>使用本地化样式，例如 {@code long} 或 {@code medium}</li>
 * </ul>
 * <p>
 * 更复杂的格式化器由
 * {@link DateTimeFormatterBuilder DateTimeFormatterBuilder} 提供。
 *
 * <p>
 * 主要的日期时间类提供了两个方法 - 一个用于格式化，
 * {@code format(DateTimeFormatter formatter)}，一个用于解析，
 * {@code parse(CharSequence text, DateTimeFormatter formatter)}。
 * <p>例如：
 * <blockquote><pre>
 *  LocalDate date = LocalDate.now();
 *  String text = date.format(formatter);
 *  LocalDate parsedDate = LocalDate.parse(text, formatter);
 * </pre></blockquote>
 * <p>
 * 除了格式，格式化器还可以使用所需的 Locale、Chronology、ZoneId 和 DecimalStyle 创建。
 * <p>
 * {@link #withLocale withLocale} 方法返回一个新的格式化器，覆盖了 locale。locale 影响某些方面的格式化和解析。例如，{@link #ofLocalizedDate ofLocalizedDate} 提供了一个使用本地特定日期格式的格式化器。
 * <p>
 * {@link #withChronology withChronology} 方法返回一个新的格式化器，覆盖了 chronology。如果被覆盖，日期时间值在格式化前会被转换为 chronology。在解析时，日期时间值在返回前会被转换为 chronology。
 * <p>
 * {@link #withZone withZone} 方法返回一个新的格式化器，覆盖了 zone。如果被覆盖，日期时间值在格式化前会被转换为带有请求 ZoneId 的 ZonedDateTime。在解析时，ZoneId 会在值返回前被应用。
 * <p>
 * {@link #withDecimalStyle withDecimalStyle} 方法返回一个新的格式化器，覆盖了 {@link DecimalStyle}。DecimalStyle 符号用于格式化和解析。
 * <p>
 * 一些应用程序可能需要使用旧的 {@link Format java.text.Format} 类进行格式化。{@link #toFormat()} 方法返回一个 {@code java.text.Format} 的实现。
 *
 * <h3 id="predefined">预定义格式化器</h3>
 * <table summary="预定义格式化器" cellpadding="2" cellspacing="3" border="0" >
 * <thead>
 * <tr class="tableSubHeadingColor">
 * <th class="colFirst" align="left">格式化器</th>
 * <th class="colFirst" align="left">描述</th>
 * <th class="colLast" align="left">示例</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr class="rowColor">
 * <td>{@link #ofLocalizedDate ofLocalizedDate(dateStyle)} </td>
 * <td> 从本地化样式创建的日期格式化器 </td>
 * <td> '2011-12-03'</td>
 * </tr>
 * <tr class="altColor">
 * <td> {@link #ofLocalizedTime ofLocalizedTime(timeStyle)} </td>
 * <td> 从本地化样式创建的时间格式化器 </td>
 * <td> '10:15:30'</td>
 * </tr>
 * <tr class="rowColor">
 * <td> {@link #ofLocalizedDateTime ofLocalizedDateTime(dateTimeStyle)} </td>
 * <td> 从本地化样式创建的日期和时间格式化器</td>
 * <td> '3 Jun 2008 11:05:30'</td>
 * </tr>
 * <tr class="altColor">
 * <td> {@link #ofLocalizedDateTime ofLocalizedDateTime(dateStyle,timeStyle)}
 * </td>
 * <td> 从本地化样式创建的日期和时间格式化器 </td>
 * <td> '3 Jun 2008 11:05'</td>
 * </tr>
 * <tr class="rowColor">
 * <td> {@link #BASIC_ISO_DATE}</td>
 * <td>基本 ISO 日期 </td> <td>'20111203'</td>
 * </tr>
 * <tr class="altColor">
 * <td> {@link #ISO_LOCAL_DATE}</td>
 * <td> ISO 本地日期 </td>
 * <td>'2011-12-03'</td>
 * </tr>
 * <tr class="rowColor">
 * <td> {@link #ISO_OFFSET_DATE}</td>
 * <td> 带偏移的 ISO 日期 </td>
 * <td>'2011-12-03+01:00'</td>
 * </tr>
 * <tr class="altColor">
 * <td> {@link #ISO_DATE}</td>
 * <td> 带或不带偏移的 ISO 日期 </td>
 * <td> '2011-12-03+01:00'; '2011-12-03'</td>
 * </tr>
 * <tr class="rowColor">
 * <td> {@link #ISO_LOCAL_TIME}</td>
 * <td> 不带偏移的时间 </td>
 * <td>'10:15:30'</td>
 * </tr>
 * <tr class="altColor">
 * <td> {@link #ISO_OFFSET_TIME}</td>
 * <td> 带偏移的时间 </td>
 * <td>'10:15:30+01:00'</td>
 * </tr>
 * <tr class="rowColor">
 * <td> {@link #ISO_TIME}</td>
 * <td> 带或不带偏移的时间 </td>
 * <td>'10:15:30+01:00'; '10:15:30'</td>
 * </tr>
 * <tr class="altColor">
 * <td> {@link #ISO_LOCAL_DATE_TIME}</td>
 * <td> ISO 本地日期和时间 </td>
 * <td>'2011-12-03T10:15:30'</td>
 * </tr>
 * <tr class="rowColor">
 * <td> {@link #ISO_OFFSET_DATE_TIME}</td>
 * <td> 带偏移的日期时间
 * </td><td>2011-12-03T10:15:30+01:00'</td>
 * </tr>
 * <tr class="altColor">
 * <td> {@link #ISO_ZONED_DATE_TIME}</td>
 * <td> 带时区的日期时间 </td>
 * <td>'2011-12-03T10:15:30+01:00[Europe/Paris]'</td>
 * </tr>
 * <tr class="rowColor">
 * <td> {@link #ISO_DATE_TIME}</td>
 * <td> 带 ZoneId 的日期和时间 </td>
 * <td>'2011-12-03T10:15:30+01:00[Europe/Paris]'</td>
 * </tr>
 * <tr class="altColor">
 * <td> {@link #ISO_ORDINAL_DATE}</td>
 * <td> 年和年中的天数 </td>
 * <td>'2012-337'</td>
 * </tr>
 * <tr class="rowColor">
 * <td> {@link #ISO_WEEK_DATE}</td>
 * <td> 年和周 </td>
 * <td>2012-W48-6'</td></tr>
 * <tr class="altColor">
 * <td> {@link #ISO_INSTANT}</td>
 * <td> 瞬时的日期和时间 </td>
 * <td>'2011-12-03T10:15:30Z' </td>
 * </tr>
 * <tr class="rowColor">
 * <td> {@link #RFC_1123_DATE_TIME}</td>
 * <td> RFC 1123 / RFC 822 </td>
 * <td>'Tue, 3 Jun 2008 11:05:30 GMT'</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * <h3 id="patterns">格式化和解析的模式</h3>
 * 模式基于简单的字母和符号序列。
 * 模式用于使用
 * {@link #ofPattern(String)} 和 {@link #ofPattern(String, Locale)} 方法创建格式化器。
 * 例如，
 * {@code "d MMM uuuu"} 将 2011-12-03 格式化为 '3&nbsp;Dec&nbsp;2011'。
 * 从模式创建的格式化器可以多次使用，它是不可变的，线程安全的。
 * <p>
 * 例如：
 * <blockquote><pre>
 *  LocalDate date = LocalDate.now();
 *  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
 *  String text = date.format(formatter);
 *  LocalDate parsedDate = LocalDate.parse(text, formatter);
 * </pre></blockquote>
 * <p>
 * 所有字母 'A' 到 'Z' 和 'a' 到 'z' 都被保留为模式字母。定义了以下模式字母：
 * <pre>
 *  符号  意义                     表示方式      示例
 *  ------  -------                     ------------      -------
 *   G       时代                         文本              AD; Anno Domini; A
 *   u       年份                        年份              2004; 04
 *   y       时代年份                 年份              2004; 04
 *   D       年中的天数                 数字            189
 *   M/L     月份                       数字/文本       7; 07; Jul; July; J
 *   d       月份中的天数                数字            10
 *
 *   Q/q     年中的季度             数字/文本       3; 03; Q3; 3rd quarter
 *   Y       周年份                 年份              1996; 96
 *   w       周年份中的周数     数字            27
 *   W       月份中的周数               数字            4
 *   E       星期几                 文本              Tue; Tuesday; T
 *   e/c     本地化的星期几       数字/文本       2; 02; Tue; Tuesday; T
 *   F       月份中的周数               数字            3
 *
 *   a       上午/下午                文本              PM
 *   h       上午/下午的小时 (1-12)  数字            12
 *   K       上午/下午的小时 (0-11)        数字            0
 *   k       上午/下午的小时 (1-24)  数字            0
 *
 *   H       一天中的小时 (0-23)          数字            0
 *   m       小时中的分钟              数字            30
 *   s       分钟中的秒               数字            55
 *   S       秒的小数部分          小数部分          978
 *   A       一天中的毫秒            数字            1234
 *   n       一秒中的纳秒            数字            987654321
 *   N       一天中的纳秒            数字            1234000000
 *
 *   V       时区ID                时区ID           America/Los_Angeles; Z; -08:30
 *   z       时区名称              时区名称         Pacific Standard Time; PST
 *   O       本地化的时区偏移       偏移-O          GMT+8; GMT+08:00; UTC-08:00;
 *   X       时区偏移 'Z' 为零    偏移-X          Z; -08; -0830; -08:30; -083015; -08:30:15;
 *   x       时区偏移                 偏移-x          +0000; -08; -0830; -08:30; -083015; -08:30:15;
 *   Z       时区偏移                 偏移-Z          +0000; -0800; -08:00;
 *
 *   p       填充下一个                填充修饰符      1
 *
 *   '       文本转义                分隔符
 *   ''      单引号                字面量           '
 *   [       可选部分开始
 *   ]       可选部分结束
 *   #       保留用于未来使用
 *   {       保留用于未来使用
 *   }       保留用于未来使用
 * </pre>
 * <p>
 * 模式字母的数量决定了格式。
 * <p>
 * <b>文本</b>：文本样式基于使用的模式字母数量确定。少于 4 个模式字母将使用
 * {@link TextStyle#SHORT 短形式}。恰好 4 个模式字母将使用
 * {@link TextStyle#FULL 完整形式}。恰好 5 个模式字母将使用
 * {@link TextStyle#NARROW 窄形式}。
 * 模式字母 'L'、'c' 和 'q' 指定文本样式的独立形式。
 * <p>
 * <b>数字</b>：如果字母数量为 1，则使用最少的数字输出值，不填充。否则，字母的数量用作输出字段的宽度，必要时值将被零填充。
 * 以下模式字母对字母数量有约束。'c' 和 'F' 只能指定一个字母。
 * 'd'、'H'、'h'、'K'、'k'、'm' 和 's' 可以指定最多两个字母。
 * 'D' 可以指定最多三个字母。
 * <p>
 * <b>数字/文本</b>：如果模式字母的数量为 3 或更多，使用上述文本规则。否则使用上述数字规则。
 * <p>
 * <b>小数部分</b>：以小数部分的形式输出 nano-of-second 字段。
 * nano-of-second 值有九位数字，因此模式字母的数量为 1 到 9。如果它小于 9，则 nano-of-second 值会被截断，只输出最显著的数字。
 * <p>
 * <b>年份</b>：字母的数量决定了最小字段宽度，不足时使用填充。如果字母的数量为两个，则使用
 * {@link DateTimeFormatterBuilder#appendValueReduced 减少的} 两位形式。打印时，这会输出最右边的两位数字。解析时，这将使用 2000 作为基数，结果在 2000 到 2099（包括 2099）的范围内。如果字母的数量少于四个（但不是两个），则仅在年份为负数时输出符号，如
 * {@link SignStyle#NORMAL}。否则，如果填充宽度超过，则输出符号，如
 * {@link SignStyle#EXCEEDS_PAD}。
 * <p>
 * <b>时区ID</b>：这输出时区ID，例如 'Europe/Paris'。如果字母的数量为两个，则输出时区ID。其他数量的字母将抛出 {@code IllegalArgumentException}。
 * <p>
 * <b>时区名称</b>：这输出时区ID的显示名称。如果字母的数量为一、二或三个，则输出短名称。如果字母的数量为四个，则输出完整名称。五个或更多字母将抛出 {@code IllegalArgumentException}。
 * <p>
 * <b>偏移 X 和 x</b>：这基于模式字母的数量格式化偏移。一个字母输出仅小时，例如 '+01'，除非分钟不为零，此时也会输出分钟，例如 '+0130'。两个字母输出小时和分钟，不带冒号，例如 '+0130'。三个字母输出小时和分钟，带冒号，例如 '+01:30'。四个字母输出小时、分钟和可选的秒，不带冒号，例如 '+013015'。五个字母输出小时、分钟和可选的秒，带冒号，例如 '+01:30:15'。六个或更多字母将抛出
 * {@code IllegalArgumentException}。模式字母 'X'（大写）在偏移为零时输出 'Z'，而模式字母 'x'（小写）在偏移为零时输出 '+00'、'+0000' 或 '+00:00'。
 * <p>
 * <b>偏移 O</b>：这基于模式字母的数量格式化本地化的偏移。一个字母输出 {@linkplain TextStyle#SHORT 短形式} 的本地化偏移，即本地化偏移文本，例如 'GMT'，小时不带前导零，可选的两位分钟和秒（如果非零），带冒号，例如 'GMT+8'。四个字母输出 {@linkplain TextStyle#FULL 完整形式}，即本地化偏移文本，例如 'GMT'，带两位小时和分钟字段，可选的秒字段（如果非零），带冒号，例如 'GMT+08:00'。其他数量的字母将抛出
 * {@code IllegalArgumentException}。
 * <p>
 * <b>偏移 Z</b>：这基于模式字母的数量格式化偏移。一个、两个或三个字母输出小时和分钟，不带冒号，例如 '+0130'。偏移为零时输出 '+0000'。四个字母输出 {@linkplain TextStyle#FULL 完整形式} 的本地化偏移，等同于四个字母的 Offset-O。偏移为零时输出相应的本地化偏移文本。五个字母输出小时、分钟，可选的秒（如果非零），带冒号。偏移为零时输出 'Z'。六个或更多字母将抛出
 * {@code IllegalArgumentException}。
 * <p>
 * <b>可选部分</b>：可选部分标记的工作方式与调用
 * {@link DateTimeFormatterBuilder#optionalStart()} 和
 * {@link DateTimeFormatterBuilder#optionalEnd()} 完全相同。
 * <p>
 * <b>填充修饰符</b>：修改紧随其后的模式，使其用空格填充。填充宽度由模式字母的数量确定。这与调用
 * {@link DateTimeFormatterBuilder#padNext(int)} 相同。
 * <p>
 * 例如，'ppH' 输出小时，左填充空格，宽度为 2。
 * <p>
 * 任何未识别的字母都是错误。任何非字母字符，除了 '[', ']', '{', '}', '#' 和单引号，将直接输出。尽管如此，建议使用单引号围绕所有直接输出的字符，以确保未来的更改不会破坏您的应用程序。
 *
 * <h3 id="resolving">解析</h3>
 * 解析实现为两阶段操作。
 * 首先，使用格式化器定义的布局解析文本，生成
 * 一个字段到值的 {@code Map}、一个 {@code ZoneId} 和一个 {@code Chronology}。
 * 其次，解析的数据被 <em>解析</em>，通过验证、组合和简化各种字段生成更有用的字段。
 * <p>
 * 该类提供了五种解析方法。
 * 其中四种方法执行解析和解析两个阶段。
 * 第五种方法，{@link #parseUnresolved(CharSequence, ParsePosition)}，
 * 仅执行第一阶段，结果未解析。因此，它基本上是一个低级操作。
 * <p>
 * 解析阶段由两个参数控制，这两个参数设置在该类上。
 * <p>
 * {@link ResolverStyle} 是一个枚举，提供三种不同的方法：严格、智能和宽松。默认选项是智能。
 * 可以使用 {@link #withResolverStyle(ResolverStyle)} 设置。
 * <p>
 * {@link #withResolverFields(TemporalField...)} 参数允许在解析开始前过滤将被解析的字段集。
 * 例如，如果格式化器解析了年、月、日和年中的天数，则有两种方法可以解析日期：
 * (年 + 月 + 日) 和 (年 + 年中的天数)。解析字段允许选择其中一种方法。
 * 如果未设置解析字段，则两种方法都必须解析出相同的日期。
 * <p>
 * 将单独的字段解析为完整的日期和时间是一个复杂的过程，行为分布在多个类中。
 * 它遵循以下步骤：
 * <ol>
 * <li>确定历法。
 * 结果的历法要么是解析的历法，要么是未解析历法时设置在该类上的历法，如果该历法为 null，则为 {@code IsoChronology}。
 * <li>解析 {@code ChronoField} 日期字段。
 * 这是通过 {@link Chronology#resolveDate(Map, ResolverStyle)} 实现的。关于字段解析的文档位于 {@code Chronology} 的实现中。
 * <li>解析 {@code ChronoField} 时间字段。
 * 这在 {@link ChronoField} 上有文档，对所有历法都相同。
 * <li>处理任何不是 {@code ChronoField} 的字段。
 * 这是通过 {@link TemporalField#resolve(Map, TemporalAccessor, ResolverStyle)} 实现的。关于字段解析的文档位于 {@code TemporalField} 的实现中。
 * <li>重新解析 {@code ChronoField} 日期和时间字段。
 * 这允许第四步中生成的 {@code ChronoField} 值被处理成日期和时间。
 * <li>如果至少有小时可用，则形成一个 {@code LocalTime}。
 * 这涉及为分钟、秒和秒的小数部分提供默认值。
 * <li>任何剩余的未解析字段将与任何已解析的日期和/或时间进行交叉检查。因此，早期阶段将解析 (年 + 月 + 日) 为日期，此阶段将检查该日期的星期几是否有效。
 * <li>如果解析了 {@linkplain #parsedExcessDays() 过多的天数}，则在有日期可用时将其添加到日期中。
 * </ol>
 *
 * @implSpec
 * 该类是不可变的，线程安全的。
 *
 * @since 1.8
 */
public final class DateTimeFormatter {


                /**
     * 要使用的打印机和/或解析器，不为空。
     */
    private final CompositePrinterParser printerParser;
    /**
     * 用于格式化的区域设置，不为空。
     */
    private final Locale locale;
    /**
     * 用于格式化的符号，不为空。
     */
    private final DecimalStyle decimalStyle;
    /**
     * 要使用的解析样式，不为空。
     */
    private final ResolverStyle resolverStyle;
    /**
     * 用于解析的字段，null 表示所有字段。
     */
    private final Set<TemporalField> resolverFields;
    /**
     * 用于格式化的历法，null 表示不覆盖。
     */
    private final Chronology chrono;
    /**
     * 用于格式化的时区，null 表示不覆盖。
     */
    private final ZoneId zone;

    //-----------------------------------------------------------------------
    /**
     * 使用指定模式创建格式化器。
     * <p>
     * 该方法将基于一个简单的
     * <a href="#patterns">字母和符号的模式</a>
     * 创建格式化器，如类文档中所述。
     * 例如，{@code d MMM uuuu} 将格式化 2011-12-03 为 '3 Dec 2011'。
     * <p>
     * 格式化器将使用 {@link Locale#getDefault(Locale.Category) 默认的 FORMAT 区域设置}。
     * 可以使用 {@link DateTimeFormatter#withLocale(Locale)} 更改返回的格式化器的区域设置。
     * 或者使用此方法的 {@link #ofPattern(String, Locale)} 变体。
     * <p>
     * 返回的格式化器没有覆盖的历法或时区。
     * 它使用 {@link ResolverStyle#SMART SMART} 解析样式。
     *
     * @param pattern  要使用的模式，不为空
     * @return 基于模式的格式化器，不为空
     * @throws IllegalArgumentException 如果模式无效
     * @see DateTimeFormatterBuilder#appendPattern(String)
     */
    public static DateTimeFormatter ofPattern(String pattern) {
        return new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter();
    }

    /**
     * 使用指定模式和区域设置创建格式化器。
     * <p>
     * 该方法将基于一个简单的
     * <a href="#patterns">字母和符号的模式</a>
     * 创建格式化器，如类文档中所述。
     * 例如，{@code d MMM uuuu} 将格式化 2011-12-03 为 '3 Dec 2011'。
     * <p>
     * 格式化器将使用指定的区域设置。
     * 可以使用 {@link DateTimeFormatter#withLocale(Locale)} 更改返回的格式化器的区域设置。
     * <p>
     * 返回的格式化器没有覆盖的历法或时区。
     * 它使用 {@link ResolverStyle#SMART SMART} 解析样式。
     *
     * @param pattern  要使用的模式，不为空
     * @param locale  要使用的区域设置，不为空
     * @return 基于模式的格式化器，不为空
     * @throws IllegalArgumentException 如果模式无效
     * @see DateTimeFormatterBuilder#appendPattern(String)
     */
    public static DateTimeFormatter ofPattern(String pattern, Locale locale) {
        return new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter(locale);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回 ISO 历法的区域特定日期格式。
     * <p>
     * 这将返回一个格式化或解析日期的格式化器。
     * 使用的确切格式模式因区域设置而异。
     * <p>
     * 区域设置由格式化器确定。直接从此方法返回的格式化器将使用
     * {@link Locale#getDefault(Locale.Category) 默认的 FORMAT 区域设置}。
     * 可以使用 {@link DateTimeFormatter#withLocale(Locale) withLocale(Locale)}
     * 控制此方法的结果的区域设置。
     * <p>
     * 注意，本地化的模式是懒加载的。
     * 此 {@code DateTimeFormatter} 持有所需的样式和区域设置，
     * 按需查找所需的模式。
     * <p>
     * 返回的格式化器设置了 ISO 历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#SMART SMART} 解析样式。
     *
     * @param dateStyle  要获取的格式化器样式，不为空
     * @return 日期格式化器，不为空
     */
    public static DateTimeFormatter ofLocalizedDate(FormatStyle dateStyle) {
        Objects.requireNonNull(dateStyle, "dateStyle");
        return new DateTimeFormatterBuilder().appendLocalized(dateStyle, null)
                .toFormatter(ResolverStyle.SMART, IsoChronology.INSTANCE);
    }

    /**
     * 返回 ISO 历法的区域特定时间格式。
     * <p>
     * 这将返回一个格式化或解析时间的格式化器。
     * 使用的确切格式模式因区域设置而异。
     * <p>
     * 区域设置由格式化器确定。直接从此方法返回的格式化器将使用
     * {@link Locale#getDefault(Locale.Category) 默认的 FORMAT 区域设置}。
     * 可以使用 {@link DateTimeFormatter#withLocale(Locale) withLocale(Locale)}
     * 控制此方法的结果的区域设置。
     * <p>
     * 注意，本地化的模式是懒加载的。
     * 此 {@code DateTimeFormatter} 持有所需的样式和区域设置，
     * 按需查找所需的模式。
     * <p>
     * 返回的格式化器设置了 ISO 历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#SMART SMART} 解析样式。
     *
     * @param timeStyle  要获取的格式化器样式，不为空
     * @return 时间格式化器，不为空
     */
    public static DateTimeFormatter ofLocalizedTime(FormatStyle timeStyle) {
        Objects.requireNonNull(timeStyle, "timeStyle");
        return new DateTimeFormatterBuilder().appendLocalized(null, timeStyle)
                .toFormatter(ResolverStyle.SMART, IsoChronology.INSTANCE);
    }

    /**
     * 返回 ISO 历法的区域特定日期时间格式化器。
     * <p>
     * 这将返回一个格式化或解析日期时间的格式化器。
     * 使用的确切格式模式因区域设置而异。
     * <p>
     * 区域设置由格式化器确定。直接从此方法返回的格式化器将使用
     * {@link Locale#getDefault(Locale.Category) 默认的 FORMAT 区域设置}。
     * 可以使用 {@link DateTimeFormatter#withLocale(Locale) withLocale(Locale)}
     * 控制此方法的结果的区域设置。
     * <p>
     * 注意，本地化的模式是懒加载的。
     * 此 {@code DateTimeFormatter} 持有所需的样式和区域设置，
     * 按需查找所需的模式。
     * <p>
     * 返回的格式化器设置了 ISO 历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#SMART SMART} 解析样式。
     *
     * @param dateTimeStyle  要获取的格式化器样式，不为空
     * @return 日期时间格式化器，不为空
     */
    public static DateTimeFormatter ofLocalizedDateTime(FormatStyle dateTimeStyle) {
        Objects.requireNonNull(dateTimeStyle, "dateTimeStyle");
        return new DateTimeFormatterBuilder().appendLocalized(dateTimeStyle, dateTimeStyle)
                .toFormatter(ResolverStyle.SMART, IsoChronology.INSTANCE);
    }

    /**
     * 返回 ISO 历法的区域特定日期和时间格式。
     * <p>
     * 这将返回一个格式化或解析日期时间的格式化器。
     * 使用的确切格式模式因区域设置而异。
     * <p>
     * 区域设置由格式化器确定。直接从此方法返回的格式化器将使用
     * {@link Locale#getDefault() 默认的 FORMAT 区域设置}。
     * 可以使用 {@link DateTimeFormatter#withLocale(Locale) withLocale(Locale)}
     * 控制此方法的结果的区域设置。
     * <p>
     * 注意，本地化的模式是懒加载的。
     * 此 {@code DateTimeFormatter} 持有所需的样式和区域设置，
     * 按需查找所需的模式。
     * <p>
     * 返回的格式化器设置了 ISO 历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#SMART SMART} 解析样式。
     *
     * @param dateStyle  要获取的日期格式化器样式，不为空
     * @param timeStyle  要获取的时间格式化器样式，不为空
     * @return 日期、时间或日期时间格式化器，不为空
     */
    public static DateTimeFormatter ofLocalizedDateTime(FormatStyle dateStyle, FormatStyle timeStyle) {
        Objects.requireNonNull(dateStyle, "dateStyle");
        Objects.requireNonNull(timeStyle, "timeStyle");
        return new DateTimeFormatterBuilder().appendLocalized(dateStyle, timeStyle)
                .toFormatter(ResolverStyle.SMART, IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * 格式化或解析没有偏移的日期的 ISO 日期格式化器，例如 '2011-12-03'。
     * <p>
     * 这将返回一个不可变的格式化器，能够格式化和解析
     * ISO-8601 扩展本地日期格式。
     * 格式由以下部分组成：
     * <ul>
     * <li>四位或更多位的 {@link ChronoField#YEAR 年份}。
     * 年份在 0000 到 9999 范围内将前置零填充以确保四位数。
     * 范围之外的年份将带有正负号前缀。
     * <li>破折号
     * <li>两位的 {@link ChronoField#MONTH_OF_YEAR 月份}。
     *  这将前置零填充以确保两位数。
     * <li>破折号
     * <li>两位的 {@link ChronoField#DAY_OF_MONTH 日期}。
     *  这将前置零填充以确保两位数。
     * </ul>
     * <p>
     * 返回的格式化器设置了 ISO 历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter ISO_LOCAL_DATE;
    static {
        ISO_LOCAL_DATE = new DateTimeFormatterBuilder()
                .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .appendLiteral('-')
                .appendValue(MONTH_OF_YEAR, 2)
                .appendLiteral('-')
                .appendValue(DAY_OF_MONTH, 2)
                .toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * 格式化或解析带有偏移的日期的 ISO 日期格式化器，例如 '2011-12-03+01:00'。
     * <p>
     * 这将返回一个不可变的格式化器，能够格式化和解析
     * ISO-8601 扩展偏移日期格式。
     * 格式由以下部分组成：
     * <ul>
     * <li>{@link #ISO_LOCAL_DATE}
     * <li>{@link ZoneOffset#getId() 偏移 ID}。如果偏移有秒，则会处理，尽管这不是 ISO-8601 标准的一部分。
     *  解析时不区分大小写。
     * </ul>
     * <p>
     * 返回的格式化器设置了 ISO 历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter ISO_OFFSET_DATE;
    static {
        ISO_OFFSET_DATE = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_DATE)
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * 格式化或解析带有偏移（如果可用）的日期的 ISO 日期格式化器，例如 '2011-12-03' 或 '2011-12-03+01:00'。
     * <p>
     * 这将返回一个不可变的格式化器，能够格式化和解析
     * ISO-8601 扩展日期格式。
     * 格式由以下部分组成：
     * <ul>
     * <li>{@link #ISO_LOCAL_DATE}
     * <li>如果偏移不可用，则格式完成。
     * <li>{@link ZoneOffset#getId() 偏移 ID}。如果偏移有秒，则会处理，尽管这不是 ISO-8601 标准的一部分。
     *  解析时不区分大小写。
     * </ul>
     * <p>
     * 由于此格式化器有一个可选元素，可能需要使用
     * {@link DateTimeFormatter#parseBest} 进行解析。
     * <p>
     * 返回的格式化器设置了 ISO 历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter ISO_DATE;
    static {
        ISO_DATE = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_DATE)
                .optionalStart()
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * 格式化或解析没有偏移的时间的 ISO 时间格式化器，例如 '10:15' 或 '10:15:30'。
     * <p>
     * 这将返回一个不可变的格式化器，能够格式化和解析
     * ISO-8601 扩展本地时间格式。
     * 格式由以下部分组成：
     * <ul>
     * <li>两位的 {@link ChronoField#HOUR_OF_DAY 小时}。
     *  这将前置零填充以确保两位数。
     * <li>冒号
     * <li>两位的 {@link ChronoField#MINUTE_OF_HOUR 分钟}。
     *  这将前置零填充以确保两位数。
     * <li>如果秒不可用，则格式完成。
     * <li>冒号
     * <li>两位的 {@link ChronoField#SECOND_OF_MINUTE 秒}。
     *  这将前置零填充以确保两位数。
     * <li>如果纳秒为零或不可用，则格式完成。
     * <li>小数点
     * <li>一到九位的 {@link ChronoField#NANO_OF_SECOND 纳秒}。
     *  将输出尽可能多的位数。
     * </ul>
     * <p>
     * 返回的格式化器没有覆盖的历法或时区。
     * 它使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter ISO_LOCAL_TIME;
    static {
        ISO_LOCAL_TIME = new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .optionalStart()
                .appendFraction(NANO_OF_SECOND, 0, 9, true)
                .toFormatter(ResolverStyle.STRICT, null);
    }

    //-----------------------------------------------------------------------
    /**
     * 格式化或解析带有偏移的时间的 ISO 时间格式化器，例如 '10:15+01:00' 或 '10:15:30+01:00'。
     * <p>
     * 这将返回一个不可变的格式化器，能够格式化和解析
     * ISO-8601 扩展偏移时间格式。
     * 格式由以下部分组成：
     * <ul>
     * <li>{@link #ISO_LOCAL_TIME}
     * <li>{@link ZoneOffset#getId() 偏移 ID}。如果偏移有秒，则会处理，尽管这不是 ISO-8601 标准的一部分。
     *  解析时不区分大小写。
     * </ul>
     * <p>
     * 返回的格式化器没有覆盖的历法或时区。
     * 它使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter ISO_OFFSET_TIME;
    static {
        ISO_OFFSET_TIME = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_TIME)
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT, null);
    }


                //-----------------------------------------------------------------------
    /**
     * ISO时间格式器，用于格式化或解析带有偏移量的时间（如果可用），例如 '10:15'，'10:15:30' 或 '10:15:30+01:00'。
     * <p>
     * 此方法返回一个不可变的格式器，能够格式化和解析ISO-8601扩展偏移时间格式。
     * 格式由以下部分组成：
     * <ul>
     * <li>{@link #ISO_LOCAL_TIME}
     * <li>如果偏移量不可用，则格式完成。
     * <li>{@link ZoneOffset#getId() 偏移量ID}。如果偏移量包含秒，则会处理这些秒，尽管这不是ISO-8601标准的一部分。
     *  解析时不区分大小写。
     * </ul>
     * <p>
     * 由于此格式器包含可选元素，可能需要使用 {@link DateTimeFormatter#parseBest} 进行解析。
     * <p>
     * 返回的格式器没有覆盖的历法或时区。
     * 它使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter ISO_TIME;
    static {
        ISO_TIME = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_TIME)
                .optionalStart()
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT, null);
    }

    //-----------------------------------------------------------------------
    /**
     * ISO日期时间格式器，用于格式化或解析没有偏移量的日期时间，例如 '2011-12-03T10:15:30'。
     * <p>
     * 此方法返回一个不可变的格式器，能够格式化和解析ISO-8601扩展偏移日期时间格式。
     * 格式由以下部分组成：
     * <ul>
     * <li>{@link #ISO_LOCAL_DATE}
     * <li>字母 'T'。解析时不区分大小写。
     * <li>{@link #ISO_LOCAL_TIME}
     * </ul>
     * <p>
     * 返回的格式器设置为ISO历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter ISO_LOCAL_DATE_TIME;
    static {
        ISO_LOCAL_DATE_TIME = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_DATE)
                .appendLiteral('T')
                .append(ISO_LOCAL_TIME)
                .toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * ISO日期时间格式器，用于格式化或解析带有偏移量的日期时间，例如 '2011-12-03T10:15:30+01:00'。
     * <p>
     * 此方法返回一个不可变的格式器，能够格式化和解析ISO-8601扩展偏移日期时间格式。
     * 格式由以下部分组成：
     * <ul>
     * <li>{@link #ISO_LOCAL_DATE_TIME}
     * <li>{@link ZoneOffset#getId() 偏移量ID}。如果偏移量包含秒，则会处理这些秒，尽管这不是ISO-8601标准的一部分。
     *  解析时不区分大小写。
     * </ul>
     * <p>
     * 返回的格式器设置为ISO历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter ISO_OFFSET_DATE_TIME;
    static {
        ISO_OFFSET_DATE_TIME = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_DATE_TIME)
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * 类似ISO的日期时间格式器，用于格式化或解析带有偏移量和时区的日期时间，例如 '2011-12-03T10:15:30+01:00[Europe/Paris]'。
     * <p>
     * 此方法返回一个不可变的格式器，能够格式化和解析扩展ISO-8601扩展偏移日期时间格式以添加时区的格式。
     * 方括号中的部分不是ISO-8601标准的一部分。
     * 格式由以下部分组成：
     * <ul>
     * <li>{@link #ISO_OFFSET_DATE_TIME}
     * <li>如果时区ID不可用或是一个 {@code ZoneOffset}，则格式完成。
     * <li>一个方括号 '['。
     * <li>{@link ZoneId#getId() 时区ID}。这不是ISO-8601标准的一部分。
     *  解析时区分大小写。
     * <li>一个方括号 ']'。
     * </ul>
     * <p>
     * 返回的格式器设置为ISO历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter ISO_ZONED_DATE_TIME;
    static {
        ISO_ZONED_DATE_TIME = new DateTimeFormatterBuilder()
                .append(ISO_OFFSET_DATE_TIME)
                .optionalStart()
                .appendLiteral('[')
                .parseCaseSensitive()
                .appendZoneRegionId()
                .appendLiteral(']')
                .toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * 类似ISO的日期时间格式器，用于格式化或解析带有偏移量和时区（如果可用）的日期时间，例如 '2011-12-03T10:15:30'，
     * '2011-12-03T10:15:30+01:00' 或 '2011-12-03T10:15:30+01:00[Europe/Paris]'。
     * <p>
     * 此方法返回一个不可变的格式器，能够格式化和解析ISO-8601扩展本地或偏移日期时间格式，以及指定时区的扩展非ISO格式。
     * 格式由以下部分组成：
     * <ul>
     * <li>{@link #ISO_LOCAL_DATE_TIME}
     * <li>如果偏移量不可用于格式化或解析，则格式完成。
     * <li>{@link ZoneOffset#getId() 偏移量ID}。如果偏移量包含秒，则会处理这些秒，尽管这不是ISO-8601标准的一部分。
     * <li>如果时区ID不可用或是一个 {@code ZoneOffset}，则格式完成。
     * <li>一个方括号 '['。
     * <li>{@link ZoneId#getId() 时区ID}。这不是ISO-8601标准的一部分。
     *  解析时区分大小写。
     * <li>一个方括号 ']'。
     * </ul>
     * <p>
     * 由于此格式器包含可选元素，可能需要使用 {@link DateTimeFormatter#parseBest} 进行解析。
     * <p>
     * 返回的格式器设置为ISO历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter ISO_DATE_TIME;
    static {
        ISO_DATE_TIME = new DateTimeFormatterBuilder()
                .append(ISO_LOCAL_DATE_TIME)
                .optionalStart()
                .appendOffsetId()
                .optionalStart()
                .appendLiteral('[')
                .parseCaseSensitive()
                .appendZoneRegionId()
                .appendLiteral(']')
                .toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * ISO日期格式器，用于格式化或解析没有偏移量的序数日期，例如 '2012-337'。
     * <p>
     * 此方法返回一个不可变的格式器，能够格式化和解析ISO-8601扩展序数日期格式。
     * 格式由以下部分组成：
     * <ul>
     * <li>四位或更多位数表示 {@link ChronoField#YEAR 年}。
     * 0000到9999范围内的年份将用零前置填充以确保四位数。
     * 范围外的年份将带有正负号前缀。
     * <li>一个破折号
     * <li>三位数表示 {@link ChronoField#DAY_OF_YEAR 年中的天数}。
     * 用零前置填充以确保三位数。
     * <li>如果偏移量不可用于格式化或解析，则格式完成。
     * <li>{@link ZoneOffset#getId() 偏移量ID}。如果偏移量包含秒，则会处理这些秒，尽管这不是ISO-8601标准的一部分。
     *  解析时不区分大小写。
     * </ul>
     * <p>
     * 由于此格式器包含可选元素，可能需要使用 {@link DateTimeFormatter#parseBest} 进行解析。
     * <p>
     * 返回的格式器设置为ISO历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter ISO_ORDINAL_DATE;
    static {
        ISO_ORDINAL_DATE = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .appendLiteral('-')
                .appendValue(DAY_OF_YEAR, 3)
                .optionalStart()
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * ISO日期格式器，用于格式化或解析没有偏移量的基于周的日期，例如 '2012-W48-6'。
     * <p>
     * 此方法返回一个不可变的格式器，能够格式化和解析ISO-8601扩展基于周的日期格式。
     * 格式由以下部分组成：
     * <ul>
     * <li>四位或更多位数表示 {@link IsoFields#WEEK_BASED_YEAR 基于周的年}。
     * 0000到9999范围内的年份将用零前置填充以确保四位数。
     * 范围外的年份将带有正负号前缀。
     * <li>一个破折号
     * <li>字母 'W'。解析时不区分大小写。
     * <li>两位数表示 {@link IsoFields#WEEK_OF_WEEK_BASED_YEAR 基于周的年中的周数}。
     * 用零前置填充以确保三位数。
     * <li>一个破折号
     * <li>一位数表示 {@link ChronoField#DAY_OF_WEEK 周中的天数}。
     * 值从周一（1）到周日（7）。
     * <li>如果偏移量不可用于格式化或解析，则格式完成。
     * <li>{@link ZoneOffset#getId() 偏移量ID}。如果偏移量包含秒，则会处理这些秒，尽管这不是ISO-8601标准的一部分。
     *  解析时不区分大小写。
     * </ul>
     * <p>
     * 由于此格式器包含可选元素，可能需要使用 {@link DateTimeFormatter#parseBest} 进行解析。
     * <p>
     * 返回的格式器设置为ISO历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter ISO_WEEK_DATE;
    static {
        ISO_WEEK_DATE = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendValue(IsoFields.WEEK_BASED_YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .appendLiteral("-W")
                .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2)
                .appendLiteral('-')
                .appendValue(DAY_OF_WEEK, 1)
                .optionalStart()
                .appendOffsetId()
                .toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * ISO即时格式器，用于格式化或解析UTC中的即时，例如 '2011-12-03T10:15:30Z'。
     * <p>
     * 此方法返回一个不可变的格式器，能够格式化和解析ISO-8601即时格式。
     * 格式化时，分钟的秒数总是输出。
     * 纳秒的秒数根据需要输出零、三位、六位或九位数字。
     * 解析时，至少需要到秒的时间。
     * 从零到九的分数秒会被解析。
     * 不使用本地化的十进制样式。
     * <p>
     * 这是一个特殊格式器，旨在允许 {@link java.time.Instant} 的人类可读形式。
     * {@code Instant} 类设计为仅表示一个时间点，并在内部存储从1970-01-01Z固定纪元开始的纳秒值。
     * 因此，不提供某种形式的时区，{@code Instant} 不能格式化为日期或时间。
     * 通过使用 {@code ZoneOffset.UTC} 提供适当的转换，此格式器允许 {@code Instant} 被格式化。
     * <p>
     * 格式由以下部分组成：
     * <ul>
     * <li>{@link #ISO_OFFSET_DATE_TIME}，其中即时从 {@link ChronoField#INSTANT_SECONDS} 和
     *  {@link ChronoField#NANO_OF_SECOND} 使用 {@code UTC} 偏移量转换。解析时不区分大小写。
     * </ul>
     * <p>
     * 返回的格式器没有覆盖的历法或时区。
     * 它使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter ISO_INSTANT;
    static {
        ISO_INSTANT = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendInstant()
                .toFormatter(ResolverStyle.STRICT, null);
    }

    //-----------------------------------------------------------------------
    /**
     * ISO日期格式器，用于格式化或解析没有偏移量的日期，例如 '20111203'。
     * <p>
     * 此方法返回一个不可变的格式器，能够格式化和解析ISO-8601基本本地日期格式。
     * 格式由以下部分组成：
     * <ul>
     * <li>四位数表示 {@link ChronoField#YEAR 年}。
     * 仅支持0000到9999范围内的年份。
     * <li>两位数表示 {@link ChronoField#MONTH_OF_YEAR 月}。
     * 用零前置填充以确保两位数。
     * <li>两位数表示 {@link ChronoField#DAY_OF_MONTH 月中的天数}。
     * 用零前置填充以确保两位数。
     * <li>如果偏移量不可用于格式化或解析，则格式完成。
     * <li>没有冒号的 {@link ZoneOffset#getId() 偏移量ID}。如果偏移量包含秒，则会处理这些秒，尽管这不是ISO-8601标准的一部分。
     *  解析时不区分大小写。
     * </ul>
     * <p>
     * 由于此格式器包含可选元素，可能需要使用 {@link DateTimeFormatter#parseBest} 进行解析。
     * <p>
     * 返回的格式器设置为ISO历法，以确保其他日历系统的日期正确转换。
     * 它没有覆盖的时区，并使用 {@link ResolverStyle#STRICT STRICT} 解析样式。
     */
    public static final DateTimeFormatter BASIC_ISO_DATE;
    static {
        BASIC_ISO_DATE = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendValue(YEAR, 4)
                .appendValue(MONTH_OF_YEAR, 2)
                .appendValue(DAY_OF_MONTH, 2)
                .optionalStart()
                .appendOffset("+HHMMss", "Z")
                .toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
    }


                //-----------------------------------------------------------------------
    /**
     * RFC-1123 日期时间格式化器，例如 'Tue, 3 Jun 2008 11:05:30 GMT'。
     * <p>
     * 此方法返回一个不可变的格式化器，能够格式化和解析 RFC-1123 格式的大部分内容。
     * RFC-1123 更新了 RFC-822，将年份从两位数改为四位数。
     * 本实现要求四位数的年份。
     * 本实现也不处理北美或军事时区名称，仅处理 'GMT' 和偏移量。
     * <p>
     * 格式由以下部分组成：
     * <ul>
     * <li>如果无法格式化或解析星期几，则跳转到月份日期。
     * <li>英文的 {@link ChronoField#DAY_OF_WEEK 星期几} 的三个字母。
     * <li>逗号
     * <li>空格
     * <li>月份日期的 {@link ChronoField#DAY_OF_MONTH 一到两位数字}。
     * <li>空格
     * <li>英文的 {@link ChronoField#MONTH_OF_YEAR 月份} 的三个字母。
     * <li>空格
     * <li>四位数的 {@link ChronoField#YEAR 年份}。
     * 仅支持 0000 到 9999 年。
     * <li>空格
     * <li>两位数的 {@link ChronoField#HOUR_OF_DAY 小时}。
     * 通过前置零确保两位数。
     * <li>冒号
     * <li>两位数的 {@link ChronoField#MINUTE_OF_HOUR 分钟}。
     * 通过前置零确保两位数。
     * <li>如果秒数不可用，则跳转到下一个空格。
     * <li>冒号
     * <li>两位数的 {@link ChronoField#SECOND_OF_MINUTE 秒数}。
     * 通过前置零确保两位数。
     * <li>空格
     * <li>不带冒号或秒数的 {@link ZoneOffset#getId() 偏移量 ID}。
     * 偏移量为零时使用 "GMT"。不处理北美时区名称和军事时区名称。
     * </ul>
     * <p>
     * 解析时不区分大小写。
     * <p>
     * 返回的格式化器设置为 ISO 历法，以确保其他历法系统的日期正确转换。
     * 它没有覆盖时区，并使用 {@link ResolverStyle#SMART SMART} 解析器样式。
     */
    public static final DateTimeFormatter RFC_1123_DATE_TIME;
    static {
        // 手动编码映射以确保始终使用正确的数据
        // （应用程序代码可以更改区域设置数据）
        Map<Long, String> dow = new HashMap<>();
        dow.put(1L, "Mon");
        dow.put(2L, "Tue");
        dow.put(3L, "Wed");
        dow.put(4L, "Thu");
        dow.put(5L, "Fri");
        dow.put(6L, "Sat");
        dow.put(7L, "Sun");
        Map<Long, String> moy = new HashMap<>();
        moy.put(1L, "Jan");
        moy.put(2L, "Feb");
        moy.put(3L, "Mar");
        moy.put(4L, "Apr");
        moy.put(5L, "May");
        moy.put(6L, "Jun");
        moy.put(7L, "Jul");
        moy.put(8L, "Aug");
        moy.put(9L, "Sep");
        moy.put(10L, "Oct");
        moy.put(11L, "Nov");
        moy.put(12L, "Dec");
        RFC_1123_DATE_TIME = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .parseLenient()
                .optionalStart()
                .appendText(DAY_OF_WEEK, dow)
                .appendLiteral(", ")
                .optionalEnd()
                .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
                .appendLiteral(' ')
                .appendText(MONTH_OF_YEAR, moy)
                .appendLiteral(' ')
                .appendValue(YEAR, 4)  // 2 位数的年份不被处理
                .appendLiteral(' ')
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .optionalEnd()
                .appendLiteral(' ')
                .appendOffset("+HHMM", "GMT")  // 应处理 UT/Z/EST/EDT/CST/CDT/MST/MDT/PST/MDT
                .toFormatter(ResolverStyle.SMART, IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * 提供访问解析出的额外天数的查询。
     * <p>
     * 此方法返回一个单例 {@linkplain TemporalQuery 查询}，提供对解析结果中额外信息的访问。
     * 查询始终返回一个非空的期间，如果解析结果中没有额外天数，则返回零期间。
     * <p>
     * 有以下两种情况，此查询可能返回非零期间：
     * <ul>
     * <li>如果 {@code ResolverStyle} 是 {@code LENIENT} 且解析了一个没有日期的时间，则解析的完整结果由一个
     *  {@code LocalTime} 和一个额外的 {@code Period}（以天为单位）组成。
     *
     * <li>如果 {@code ResolverStyle} 是 {@code SMART} 且解析了一个没有日期的时间，且时间为 24:00:00，则解析的完整结果由一个
     *  {@code LocalTime}（00:00:00）和一个额外的 {@code Period}（一天）组成。
     * </ul>
     * <p>
     * 在这两种情况下，如果解析了一个完整的 {@code ChronoLocalDateTime} 或 {@code Instant}，则额外的天数将被添加到日期部分。
     * 因此，此查询将返回零期间。
     * <p>
     * {@code SMART} 行为处理常见的 "一天结束" 24:00 值。
     * 在 {@code LENIENT} 模式下处理时也会产生相同的结果：
     * <pre>
     *  要解析的文本        解析的对象                         额外的天数
     *  "2012-12-03T00:00"   LocalDateTime.of(2012, 12, 3, 0, 0)   零
     *  "2012-12-03T24:00"   LocalDateTime.of(2012, 12, 4, 0, 0)   零
     *  "00:00"              LocalTime.of(0, 0)                    零
     *  "24:00"              LocalTime.of(0, 0)                    Period.ofDays(1)
     * </pre>
     * 可以如下使用此查询：
     * <pre>
     *  TemporalAccessor parsed = formatter.parse(str);
     *  LocalTime time = parsed.query(LocalTime::from);
     *  Period extraDays = parsed.query(DateTimeFormatter.parsedExcessDays());
     * </pre>
     * @return 提供访问解析出的额外天数的查询
     */
    public static final TemporalQuery<Period> parsedExcessDays() {
        return PARSED_EXCESS_DAYS;
    }
    private static final TemporalQuery<Period> PARSED_EXCESS_DAYS = t -> {
        if (t instanceof Parsed) {
            return ((Parsed) t).excessDays;
        } else {
            return Period.ZERO;
        }
    };

    /**
     * 提供访问是否解析了闰秒的查询。
     * <p>
     * 此方法返回一个单例 {@linkplain TemporalQuery 查询}，提供对解析结果中额外信息的访问。
     * 查询始终返回一个非空的布尔值，如果解析过程中遇到闰秒则返回 true，否则返回 false。
     * <p>
     * 瞬时解析处理特殊的 "闰秒" 时间 '23:59:60'。
     * 闰秒发生在 UTC 时区的 '23:59:60'，但在其他时区的本地时间则不同。为了避免这种潜在的歧义，
     * 闰秒的处理仅限于 {@link DateTimeFormatterBuilder#appendInstant()}，因为该方法始终使用 UTC 时区偏移量进行解析。
     * <p>
     * 如果接收到时间 '23:59:60'，则会应用简单的转换，将秒数 60 替换为 59。可以使用此查询确定是否进行了闰秒调整。
     * 如果确实进行了调整以移除闰秒，则查询将返回 {@code true}，否则返回 {@code false}。请注意，应用闰秒平滑机制（如 UTC-SLS）是应用程序的责任，如下所示：
     * <pre>
     *  TemporalAccessor parsed = formatter.parse(str);
     *  Instant instant = parsed.query(Instant::from);
     *  if (parsed.query(DateTimeFormatter.parsedLeapSecond())) {
     *    // 验证闰秒是否正确并应用正确的平滑处理
     *  }
     * </pre>
     * @return 提供访问是否解析了闰秒的查询
     */
    public static final TemporalQuery<Boolean> parsedLeapSecond() {
        return PARSED_LEAP_SECOND;
    }
    private static final TemporalQuery<Boolean> PARSED_LEAP_SECOND = t -> {
        if (t instanceof Parsed) {
            return ((Parsed) t).leapSecond;
        } else {
            return Boolean.FALSE;
        }
    };

    //-----------------------------------------------------------------------
    /**
     * 构造函数。
     *
     * @param printerParser  要使用的打印/解析器，不为空
     * @param locale  要使用的区域设置，不为空
     * @param decimalStyle  要使用的 DecimalStyle，不为空
     * @param resolverStyle  要使用的解析器样式，不为空
     * @param resolverFields  解析时要使用的字段，null 表示所有字段
     * @param chrono  要使用的历法，null 表示无覆盖
     * @param zone  要使用的时区，null 表示无覆盖
     */
    DateTimeFormatter(CompositePrinterParser printerParser,
            Locale locale, DecimalStyle decimalStyle,
            ResolverStyle resolverStyle, Set<TemporalField> resolverFields,
            Chronology chrono, ZoneId zone) {
        this.printerParser = Objects.requireNonNull(printerParser, "printerParser");
        this.resolverFields = resolverFields;
        this.locale = Objects.requireNonNull(locale, "locale");
        this.decimalStyle = Objects.requireNonNull(decimalStyle, "decimalStyle");
        this.resolverStyle = Objects.requireNonNull(resolverStyle, "resolverStyle");
        this.chrono = chrono;
        this.zone = zone;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取格式化过程中要使用的区域设置。
     * <p>
     * 此方法用于查找格式化器中需要特定本地化的任何部分，例如文本或本地化模式。
     *
     * @return 此格式化器的区域设置，不为空
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * 返回具有新区域设置的格式化器副本。
     * <p>
     * 此方法用于查找格式化器中需要特定本地化的任何部分，例如文本或本地化模式。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param locale  新的区域设置，不为空
     * @return 基于此格式化器并具有请求区域设置的格式化器，不为空
     */
    public DateTimeFormatter withLocale(Locale locale) {
        if (this.locale.equals(locale)) {
            return this;
        }
        return new DateTimeFormatter(printerParser, locale, decimalStyle, resolverStyle, resolverFields, chrono, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取格式化过程中要使用的 DecimalStyle。
     *
     * @return 此格式化器的 DecimalStyle，不为空
     */
    public DecimalStyle getDecimalStyle() {
        return decimalStyle;
    }

    /**
     * 返回具有新 DecimalStyle 的格式化器副本。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param decimalStyle  新的 DecimalStyle，不为空
     * @return 基于此格式化器并具有请求 DecimalStyle 的格式化器，不为空
     */
    public DateTimeFormatter withDecimalStyle(DecimalStyle decimalStyle) {
        if (this.decimalStyle.equals(decimalStyle)) {
            return this;
        }
        return new DateTimeFormatter(printerParser, locale, decimalStyle, resolverStyle, resolverFields, chrono, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取格式化过程中要使用的覆盖历法。
     * <p>
     * 此方法返回覆盖历法，用于转换日期。
     * 默认情况下，格式化器没有覆盖历法，返回 null。
     * 有关覆盖的更多详细信息，请参见 {@link #withChronology(Chronology)}。
     *
     * @return 此格式化器的覆盖历法，无覆盖时返回 null
     */
    public Chronology getChronology() {
        return chrono;
    }

    /**
     * 返回具有新覆盖历法的格式化器副本。
     * <p>
     * 此方法返回一个与本格式化器状态相似但覆盖历法已设置的格式化器。
     * 默认情况下，格式化器没有覆盖历法，返回 null。
     * <p>
     * 如果添加了覆盖历法，则任何格式化或解析的日期都将受到影响。
     * <p>
     * 在格式化时，如果时间对象包含日期，则该日期将转换为覆盖历法中的日期。
     * 是否包含日期是通过查询 {@link ChronoField#EPOCH_DAY EPOCH_DAY} 字段确定的。
     * 任何时间或时区将保持不变，除非被覆盖。
     * <p>
     * 如果时间对象不包含日期，但包含一个或多个 {@code ChronoField} 日期字段，则将抛出 {@code DateTimeException}。
     * 在所有其他情况下，覆盖历法将被添加到时间对象中，替换任何先前的历法，但不会改变日期/时间。
     * <p>
     * 在解析时，需要考虑两种不同的情况。
     * 如果直接从文本中解析了历法，例如使用了 {@link DateTimeFormatterBuilder#appendChronologyId()}，则此覆盖历法将不起作用。
     * 如果没有解析时区，则此覆盖历法将用于根据历法的日期解析规则将 {@code ChronoField} 值解释为日期。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param chrono  新的历法，无覆盖时返回 null
     * @return 基于此格式化器并具有请求覆盖历法的格式化器，不为空
     */
    public DateTimeFormatter withChronology(Chronology chrono) {
        if (Objects.equals(this.chrono, chrono)) {
            return this;
        }
        return new DateTimeFormatter(printerParser, locale, decimalStyle, resolverStyle, resolverFields, chrono, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取格式化过程中要使用的覆盖时区。
     * <p>
     * 此方法返回覆盖时区，用于转换瞬时。
     * 默认情况下，格式化器没有覆盖时区，返回 null。
     * 有关覆盖的更多详细信息，请参见 {@link #withZone(ZoneId)}。
     *
     * @return 此格式化器的覆盖时区，无覆盖时返回 null
     */
    public ZoneId getZone() {
        return zone;
    }

    /**
     * 返回具有新覆盖时区的格式化器副本。
     * <p>
     * 此方法返回一个与本格式化器状态相似但覆盖时区已设置的格式化器。
     * 默认情况下，格式化器没有覆盖时区，返回 null。
     * <p>
     * 如果添加了覆盖时区，则任何格式化或解析的瞬时都将受到影响。
     * <p>
     * 在格式化时，如果时间对象包含瞬时，则该瞬时将使用覆盖时区转换为带时区的日期时间。
     * 是否包含瞬时是通过查询 {@link ChronoField#INSTANT_SECONDS INSTANT_SECONDS} 字段确定的。
     * 如果输入包含历法，则该历法将保持不变，除非被覆盖。
     * 如果输入不包含历法，例如 {@code Instant}，则将使用 ISO 历法。
     * <p>
     * 如果时间对象不包含瞬时，但包含偏移量，则会进行额外的检查。如果规范化后的覆盖时区是一个与时间对象偏移量不同的偏移量，则将抛出 {@code DateTimeException}。
     * 在所有其他情况下，覆盖时区将被添加到时间对象中，替换任何先前的时区，但不会改变日期/时间。
     * <p>
     * 在解析时，需要考虑两种不同的情况。
     * 如果直接从文本中解析了时区，例如使用了 {@link DateTimeFormatterBuilder#appendZoneId()}，则此覆盖时区将不起作用。
     * 如果没有解析时区，则此覆盖时区将包含在解析结果中，可用于构建瞬时和日期时间。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param zone  新的覆盖时区，无覆盖时返回 null
     * @return 基于此格式化器并具有请求覆盖时区的格式化器，不为空
     */
    public DateTimeFormatter withZone(ZoneId zone) {
        if (Objects.equals(this.zone, zone)) {
            return this;
        }
        return new DateTimeFormatter(printerParser, locale, decimalStyle, resolverStyle, resolverFields, chrono, zone);
    }


                //-----------------------------------------------------------------------
    /**
     * 获取在解析期间使用的解析器样式。
     * <p>
     * 此方法返回解析器样式，在解析的第二阶段使用，将字段解析为日期和时间。
     * 默认情况下，格式化器具有 {@link ResolverStyle#SMART SMART} 解析器样式。
     * 有关更多详细信息，请参阅 {@link #withResolverStyle(ResolverStyle)}。
     *
     * @return 此格式化器的解析器样式，不为空
     */
    public ResolverStyle getResolverStyle() {
        return resolverStyle;
    }

    /**
     * 返回具有新解析器样式的此格式化器的副本。
     * <p>
     * 此方法返回一个与此格式化器状态相似的格式化器，但设置了解析器样式。默认情况下，格式化器具有
     * {@link ResolverStyle#SMART SMART} 解析器样式。
     * <p>
     * 更改解析器样式仅在解析期间生效。
     * 解析文本字符串分为两个阶段。
     * 第一阶段是根据添加到构建器的字段进行基本文本解析。
     * 第二阶段将解析的字段-值对解析为日期和/或时间对象。
     * 解析器样式用于控制第二阶段（解析）的进行方式。
     * 有关可用选项的更多信息，请参阅 {@code ResolverStyle}。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param resolverStyle  新的解析器样式，不为空
     * @return 基于此格式化器并具有请求的解析器样式的格式化器，不为空
     */
    public DateTimeFormatter withResolverStyle(ResolverStyle resolverStyle) {
        Objects.requireNonNull(resolverStyle, "resolverStyle");
        if (Objects.equals(this.resolverStyle, resolverStyle)) {
            return this;
        }
        return new DateTimeFormatter(printerParser, locale, decimalStyle, resolverStyle, resolverFields, chrono, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取在解析期间使用的解析字段。
     * <p>
     * 此方法返回解析字段，在解析的第二阶段使用，将字段解析为日期和时间。
     * 默认情况下，格式化器没有解析字段，因此返回 null。
     * 有关更多详细信息，请参阅 {@link #withResolverFields(Set)}。
     *
     * @return 此格式化器的不可变解析字段集，如果没有字段则为 null
     */
    public Set<TemporalField> getResolverFields() {
        return resolverFields;
    }

    /**
     * 返回具有新解析字段集的此格式化器的副本。
     * <p>
     * 此方法返回一个与此格式化器状态相似的格式化器，但设置了解析字段。默认情况下，格式化器没有解析字段。
     * <p>
     * 更改解析字段仅在解析期间生效。
     * 解析文本字符串分为两个阶段。
     * 第一阶段是根据添加到构建器的字段进行基本文本解析。
     * 第二阶段将解析的字段-值对解析为日期和/或时间对象。
     * 解析字段用于在第一阶段和第二阶段之间过滤字段-值对。
     * <p>
     * 此方法可用于在两种或多种解析日期或时间的方式之间进行选择。例如，如果格式化器由年、月、日和年中的天数组成，
     * 则有两种解析日期的方式。
     * 使用 {@link ChronoField#YEAR YEAR} 和 {@link ChronoField#DAY_OF_YEAR DAY_OF_YEAR} 作为参数调用此方法
     * 将确保日期使用年和年中的天数进行解析，实际上意味着在解析阶段忽略月和日。
     * <p>
     * 以类似的方式，此方法可用于忽略其他字段，这些字段通常会进行交叉检查。例如，如果格式化器由年、月、日和周中的天数组成，
     * 则只有一种解析日期的方式，但解析的周中的天数将与解析的日期进行交叉检查。
     * 使用 {@link ChronoField#YEAR YEAR}、{@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR} 和
     * {@link ChronoField#DAY_OF_MONTH DAY_OF_MONTH} 作为参数调用此方法将确保日期正确解析，但不会对周中的天数进行交叉检查。
     * <p>
     * 在实现方面，此方法的行为如下。解析阶段的结果可以视为字段到值的映射。此方法的行为是在第一阶段和第二阶段之间过滤该映射，
     * 删除所有除作为此方法参数指定的字段之外的字段。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param resolverFields  新的解析字段集，如果没有字段则为 null
     * @return 基于此格式化器并具有请求的解析器样式的格式化器，不为空
     */
    public DateTimeFormatter withResolverFields(TemporalField... resolverFields) {
        Set<TemporalField> fields = null;
        if (resolverFields != null) {
            fields = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(resolverFields)));
        }
        if (Objects.equals(this.resolverFields, fields)) {
            return this;
        }
        return new DateTimeFormatter(printerParser, locale, decimalStyle, resolverStyle, fields, chrono, zone);
    }

    /**
     * 返回具有新解析字段集的此格式化器的副本。
     * <p>
     * 此方法返回一个与此格式化器状态相似的格式化器，但设置了解析字段。默认情况下，格式化器没有解析字段。
     * <p>
     * 更改解析字段仅在解析期间生效。
     * 解析文本字符串分为两个阶段。
     * 第一阶段是根据添加到构建器的字段进行基本文本解析。
     * 第二阶段将解析的字段-值对解析为日期和/或时间对象。
     * 解析字段用于在第一阶段和第二阶段之间过滤字段-值对。
     * <p>
     * 此方法可用于在两种或多种解析日期或时间的方式之间进行选择。例如，如果格式化器由年、月、日和年中的天数组成，
     * 则有两种解析日期的方式。
     * 使用 {@link ChronoField#YEAR YEAR} 和 {@link ChronoField#DAY_OF_YEAR DAY_OF_YEAR} 作为参数调用此方法
     * 将确保日期使用年和年中的天数进行解析，实际上意味着在解析阶段忽略月和日。
     * <p>
     * 以类似的方式，此方法可用于忽略其他字段，这些字段通常会进行交叉检查。例如，如果格式化器由年、月、日和周中的天数组成，
     * 则只有一种解析日期的方式，但解析的周中的天数将与解析的日期进行交叉检查。
     * 使用 {@link ChronoField#YEAR YEAR}、{@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR} 和
     * {@link ChronoField#DAY_OF_MONTH DAY_OF_MONTH} 作为参数调用此方法将确保日期正确解析，但不会对周中的天数进行交叉检查。
     * <p>
     * 在实现方面，此方法的行为如下。解析阶段的结果可以视为字段到值的映射。此方法的行为是在第一阶段和第二阶段之间过滤该映射，
     * 删除所有除作为此方法参数指定的字段之外的字段。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param resolverFields  新的解析字段集，如果没有字段则为 null
     * @return 基于此格式化器并具有请求的解析器样式的格式化器，不为空
     */
    public DateTimeFormatter withResolverFields(Set<TemporalField> resolverFields) {
        if (Objects.equals(this.resolverFields, resolverFields)) {
            return this;
        }
        if (resolverFields != null) {
            resolverFields = Collections.unmodifiableSet(new HashSet<>(resolverFields));
        }
        return new DateTimeFormatter(printerParser, locale, decimalStyle, resolverStyle, resolverFields, chrono, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用此格式化器格式化日期时间对象。
     * <p>
     * 此方法使用格式化器的规则将日期时间格式化为字符串。
     *
     * @param temporal  要格式化的日期时间对象，不为空
     * @return 格式化的字符串，不为空
     * @throws DateTimeException 如果格式化过程中发生错误
     */
    public String format(TemporalAccessor temporal) {
        StringBuilder buf = new StringBuilder(32);
        formatTo(temporal, buf);
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用此格式化器将日期时间对象格式化为 {@code Appendable}。
     * <p>
     * 此方法将格式化的日期时间输出到指定的目标。
     * {@link Appendable} 是一个通用接口，由所有关键字符输出类实现，包括 {@code StringBuffer}、{@code StringBuilder}、
     * {@code PrintStream} 和 {@code Writer}。
     * <p>
     * 虽然 {@code Appendable} 方法会抛出 {@code IOException}，但此方法不会。
     * 相反，任何 {@code IOException} 都会被包装在运行时异常中。
     *
     * @param temporal  要格式化的日期时间对象，不为空
     * @param appendable  要格式化的追加器，不为空
     * @throws DateTimeException 如果格式化过程中发生错误
     */
    public void formatTo(TemporalAccessor temporal, Appendable appendable) {
        Objects.requireNonNull(temporal, "temporal");
        Objects.requireNonNull(appendable, "appendable");
        try {
            DateTimePrintContext context = new DateTimePrintContext(temporal, this);
            if (appendable instanceof StringBuilder) {
                printerParser.format(context, (StringBuilder) appendable);
            } else {
                // 缓冲输出以避免在发生错误时写入追加器
                StringBuilder buf = new StringBuilder(32);
                printerParser.format(context, buf);
                appendable.append(buf);
            }
        } catch (IOException ex) {
            throw new DateTimeException(ex.getMessage(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 完全解析文本，生成一个日期时间对象。
     * <p>
     * 此方法解析整个文本，生成一个日期时间对象。
     * 通常使用 {@link #parse(CharSequence, TemporalQuery)} 更有用。
     * 该方法的结果是 {@code TemporalAccessor}，已解析并应用了基本验证检查，以确保日期时间的有效性。
     * <p>
     * 如果解析未读取整个文本长度，或在解析或合并过程中出现问题，则会抛出异常。
     *
     * @param text  要解析的文本，不为空
     * @return 解析的日期时间对象，不为空
     * @throws DateTimeParseException 如果无法解析请求的结果
     */
    public TemporalAccessor parse(CharSequence text) {
        Objects.requireNonNull(text, "text");
        try {
            return parseResolved0(text, null);
        } catch (DateTimeParseException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw createError(text, ex);
        }
    }

    /**
     * 使用此格式化器解析文本，提供对文本位置的控制。
     * <p>
     * 此方法解析文本，不要求解析从字符串的开头开始或在末尾结束。
     * 该方法的结果是 {@code TemporalAccessor}，已解析并应用了基本验证检查，以确保日期时间的有效性。
     * <p>
     * 文本将从指定的起始 {@code ParsePosition} 开始解析。
     * 不需要解析整个文本长度，{@code ParsePosition} 将更新为解析结束时的索引。
     * <p>
     * 该方法的操作与使用 {@code ParsePosition} 的 {@code java.text.Format} 类中的类似方法略有不同。
     * 该类将使用 {@code ParsePosition} 上的错误索引返回错误。相比之下，如果发生错误，此方法将抛出 {@link DateTimeParseException}，
     * 异常中包含错误索引。
     * 由于此 API 中解析和解析日期/时间的复杂性增加，这种行为的变化是必要的。
     * <p>
     * 如果格式化器使用不同的值多次解析相同的字段，结果将是一个错误。
     *
     * @param text  要解析的文本，不为空
     * @param position  要解析的起始位置，更新为解析长度和任何错误的索引，不为空
     * @return 解析的日期时间对象，不为空
     * @throws DateTimeParseException 如果无法解析请求的结果
     * @throws IndexOutOfBoundsException 如果位置无效
     */
    public TemporalAccessor parse(CharSequence text, ParsePosition position) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(position, "position");
        try {
            return parseResolved0(text, position);
        } catch (DateTimeParseException | IndexOutOfBoundsException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw createError(text, ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 完全解析文本，生成指定类型的对象。
     * <p>
     * 大多数应用程序应使用此方法进行解析。
     * 它解析整个文本以生成所需的日期时间。
     * 查询通常是 {@code from(TemporalAccessor)} 方法的引用。
     * 例如：
     * <pre>
     *  LocalDateTime dt = parser.parse(str, LocalDateTime::from);
     * </pre>
     * 如果解析未读取整个文本长度，或在解析或合并过程中出现问题，则会抛出异常。
     *
     * @param <T> 解析的日期时间的类型
     * @param text  要解析的文本，不为空
     * @param query  定义要解析的类型的查询，不为空
     * @return 解析的日期时间，不为空
     * @throws DateTimeParseException 如果无法解析请求的结果
     */
    public <T> T parse(CharSequence text, TemporalQuery<T> query) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(query, "query");
        try {
            return parseResolved0(text, null).query(query);
        } catch (DateTimeParseException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw createError(text, ex);
        }
    }


                /**
     * 完全解析文本，生成指定类型之一的对象。
     * <p>
     * 此解析方法在解析器可以处理可选元素时非常方便。
     * 例如，模式 'uuuu-MM-dd HH.mm[ VV]' 可以完全解析为 {@code ZonedDateTime}，
     * 或部分解析为 {@code LocalDateTime}。
     * 必须按顺序指定查询，从最佳匹配的完整解析选项开始，
     * 到最差匹配的最小解析选项结束。
     * 查询通常是 {@code from(TemporalAccessor)} 方法的引用。
     * <p>
     * 结果与第一个成功解析的类型相关联。
     * 通常，应用程序将使用 {@code instanceof} 检查结果。
     * 例如：
     * <pre>
     *  TemporalAccessor dt = parser.parseBest(str, ZonedDateTime::from, LocalDateTime::from);
     *  if (dt instanceof ZonedDateTime) {
     *   ...
     *  } else {
     *   ...
     *  }
     * </pre>
     * 如果解析没有读取整个文本长度，
     * 或在解析或合并过程中出现问题，则会抛出异常。
     *
     * @param text  要解析的文本，不为空
     * @param queries  定义要尝试解析的类型的查询，
     *  必须实现 {@code TemporalAccessor}，不为空
     * @return 解析的日期时间，不为空
     * @throws IllegalArgumentException 如果指定的类型少于 2 个
     * @throws DateTimeParseException 如果无法解析请求的结果
     */
    public TemporalAccessor parseBest(CharSequence text, TemporalQuery<?>... queries) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(queries, "queries");
        if (queries.length < 2) {
            throw new IllegalArgumentException("必须指定至少两个查询");
        }
        try {
            TemporalAccessor resolved = parseResolved0(text, null);
            for (TemporalQuery<?> query : queries) {
                try {
                    return (TemporalAccessor) resolved.query(query);
                } catch (RuntimeException ex) {
                    // 继续
                }
            }
            throw new DateTimeException("无法使用任何指定的查询转换解析的文本");
        } catch (DateTimeParseException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw createError(text, ex);
        }
    }

    private DateTimeParseException createError(CharSequence text, RuntimeException ex) {
        String abbr;
        if (text.length() > 64) {
            abbr = text.subSequence(0, 64).toString() + "...";
        } else {
            abbr = text.toString();
        }
        return new DateTimeParseException("文本 '" + abbr + "' 无法解析: " + ex.getMessage(), text, 0, ex);
    }

    //-----------------------------------------------------------------------
    /**
     * 解析并解析指定的文本。
     * <p>
     * 这将解析为 {@code TemporalAccessor}，确保文本被完全解析。
     *
     * @param text  要解析的文本，不为空
     * @param position  要解析的起始位置，更新解析长度
     *  和任何错误的索引，如果解析整个字符串则为 null
     * @return 解析结果，不为空
     * @throws DateTimeParseException 如果解析失败
     * @throws DateTimeException 如果解析日期或时间时发生错误
     * @throws IndexOutOfBoundsException 如果位置无效
     */
    private TemporalAccessor parseResolved0(final CharSequence text, final ParsePosition position) {
        ParsePosition pos = (position != null ? position : new ParsePosition(0));
        DateTimeParseContext context = parseUnresolved0(text, pos);
        if (context == null || pos.getErrorIndex() >= 0 || (position == null && pos.getIndex() < text.length())) {
            String abbr;
            if (text.length() > 64) {
                abbr = text.subSequence(0, 64).toString() + "...";
            } else {
                abbr = text.toString();
            }
            if (pos.getErrorIndex() >= 0) {
                throw new DateTimeParseException("文本 '" + abbr + "' 无法在索引 " +
                        pos.getErrorIndex() + " 处解析", text, pos.getErrorIndex());
            } else {
                throw new DateTimeParseException("文本 '" + abbr + "' 无法解析，未解析的文本在索引 " +
                        pos.getIndex() + " 处找到", text, pos.getIndex());
            }
        }
        return context.toResolved(resolverStyle, resolverFields);
    }

    /**
     * 使用此格式器解析文本，但不解析结果，适用于高级用例。
     * <p>
     * 解析实现为两阶段操作。
     * 首先，使用格式器定义的布局解析文本，生成
     * 一个 {@code Map}，其中包含字段到值的映射、{@code ZoneId} 和 {@code Chronology}。
     * 其次，解析的数据将被 <em>解析</em>，通过验证、组合和
     * 简化各种字段以生成更有用的字段。
     * 此方法执行解析阶段但不执行解析阶段。
     * <p>
     * 此方法的结果是 {@code TemporalAccessor}，表示输入中看到的数据。
     * 值未经过验证，因此解析日期字符串
     * '2012-00-65' 将生成一个包含三个字段的临时对象 - 年份为 '2012'，
     * 月份为 '0' 和月份中的天数为 '65'。
     * <p>
     * 文本将从指定的起始 {@code ParsePosition} 解析。
     * 不需要解析整个文本长度，{@code ParsePosition}
     * 将更新解析结束时的索引。
     * <p>
     * 错误使用 {@code ParsePosition} 的错误索引字段返回，
     * 而不是 {@code DateTimeParseException}。
     * 返回的错误索引将设置为指示错误的索引。
     * 调用者必须在使用结果之前检查错误。
     * <p>
     * 如果格式器使用不同值多次解析同一字段，
     * 结果将是一个错误。
     * <p>
     * 此方法适用于需要访问解析期间内部状态的高级用例。
     * 典型应用程序代码应使用
     * {@link #parse(CharSequence, TemporalQuery)} 或目标类型的解析方法。
     *
     * @param text  要解析的文本，不为空
     * @param position  要解析的起始位置，更新解析长度
     *  和任何错误的索引，不为空
     * @return 解析的文本，如果解析结果为错误则为 null
     * @throws DateTimeException 如果解析过程中出现问题
     * @throws IndexOutOfBoundsException 如果位置无效
     */
    public TemporalAccessor parseUnresolved(CharSequence text, ParsePosition position) {
        DateTimeParseContext context = parseUnresolved0(text, position);
        if (context == null) {
            return null;
        }
        return context.toUnresolved();
    }

    private DateTimeParseContext parseUnresolved0(CharSequence text, ParsePosition position) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(position, "position");
        DateTimeParseContext context = new DateTimeParseContext(this);
        int pos = position.getIndex();
        pos = printerParser.parse(context, text, pos);
        if (pos < 0) {
            position.setErrorIndex(~pos);  // 索引未从输入更新
            return null;
        }
        position.setIndex(pos);  // 错误索引未从输入更新
        return context;
    }

    //-----------------------------------------------------------------------
    /**
     * 返回格式器作为复合打印机解析器。
     *
     * @param optional  打印机/解析器是否可选
     * @return 打印机/解析器，不为空
     */
    CompositePrinterParser toPrinterParser(boolean optional) {
        return printerParser.withOptional(optional);
    }

    /**
     * 返回此格式器作为 {@code java.text.Format} 实例。
     * <p>
     * 返回的 {@link Format} 实例将格式化任何 {@link TemporalAccessor}
     * 并解析为已解析的 {@link TemporalAccessor}。
     * <p>
     * 异常将遵循 {@code Format} 的定义，参见这些方法
     * 有关格式化期间的 {@code IllegalArgumentException} 和
     * 解析期间的 {@code ParseException} 或 null 的详细信息。
     * 格式不支持返回格式字符串的属性。
     *
     * @return 作为经典格式实例的此格式器，不为空
     */
    public Format toFormat() {
        return new ClassicFormat(this, null);
    }

    /**
     * 返回此格式器作为 {@code java.text.Format} 实例，该实例将
     * 使用指定的查询进行解析。
     * <p>
     * 返回的 {@link Format} 实例将格式化任何 {@link TemporalAccessor}
     * 并解析为指定的类型。
     * 类型必须是 {@link #parse} 支持的类型。
     * <p>
     * 异常将遵循 {@code Format} 的定义，参见这些方法
     * 有关格式化期间的 {@code IllegalArgumentException} 和
     * 解析期间的 {@code ParseException} 或 null 的详细信息。
     * 格式不支持返回格式字符串的属性。
     *
     * @param parseQuery  定义要解析的类型的查询，不为空
     * @return 作为经典格式实例的此格式器，不为空
     */
    public Format toFormat(TemporalQuery<?> parseQuery) {
        Objects.requireNonNull(parseQuery, "parseQuery");
        return new ClassicFormat(this, parseQuery);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回底层格式器的描述。
     *
     * @return 此格式器的描述，不为空
     */
    @Override
    public String toString() {
        String pattern = printerParser.toString();
        pattern = pattern.startsWith("[") ? pattern : pattern.substring(1, pattern.length() - 1);
        return pattern;
        // TODO: 修复测试以不依赖于 toString()
//        return "DateTimeFormatter[" + locale +
//                (chrono != null ? "," + chrono : "") +
//                (zone != null ? "," + zone : "") +
//                pattern + "]";
    }

    //-----------------------------------------------------------------------
    /**
     * 实现经典 Java Format API。
     * @serial 排除
     */
    @SuppressWarnings("serial")  // 实际上不可序列化
    static class ClassicFormat extends Format {
        /** 格式器。 */
        private final DateTimeFormatter formatter;
        /** 要解析的类型。 */
        private final TemporalQuery<?> parseType;
        /** 构造函数。 */
        public ClassicFormat(DateTimeFormatter formatter, TemporalQuery<?> parseType) {
            this.formatter = formatter;
            this.parseType = parseType;
        }

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            Objects.requireNonNull(obj, "obj");
            Objects.requireNonNull(toAppendTo, "toAppendTo");
            Objects.requireNonNull(pos, "pos");
            if (obj instanceof TemporalAccessor == false) {
                throw new IllegalArgumentException("格式化目标必须实现 TemporalAccessor");
            }
            pos.setBeginIndex(0);
            pos.setEndIndex(0);
            try {
                formatter.formatTo((TemporalAccessor) obj, toAppendTo);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException(ex.getMessage(), ex);
            }
            return toAppendTo;
        }
        @Override
        public Object parseObject(String text) throws ParseException {
            Objects.requireNonNull(text, "text");
            try {
                if (parseType == null) {
                    return formatter.parseResolved0(text, null);
                }
                return formatter.parse(text, parseType);
            } catch (DateTimeParseException ex) {
                throw new ParseException(ex.getMessage(), ex.getErrorIndex());
            } catch (RuntimeException ex) {
                throw (ParseException) new ParseException(ex.getMessage(), 0).initCause(ex);
            }
        }
        @Override
        public Object parseObject(String text, ParsePosition pos) {
            Objects.requireNonNull(text, "text");
            DateTimeParseContext context;
            try {
                context = formatter.parseUnresolved0(text, pos);
            } catch (IndexOutOfBoundsException ex) {
                if (pos.getErrorIndex() < 0) {
                    pos.setErrorIndex(0);
                }
                return null;
            }
            if (context == null) {
                if (pos.getErrorIndex() < 0) {
                    pos.setErrorIndex(0);
                }
                return null;
            }
            try {
                TemporalAccessor resolved = context.toResolved(formatter.resolverStyle, formatter.resolverFields);
                if (parseType == null) {
                    return resolved;
                }
                return resolved.query(parseType);
            } catch (RuntimeException ex) {
                pos.setErrorIndex(0);
                return null;
            }
        }
    }

}
