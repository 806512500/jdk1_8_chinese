
/*
 * Copyright (c) 2012, 2019, Oracle and/or its affiliates. All rights reserved.
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
 * All rights hg qreserved.
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
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeTextProvider.LocaleStore;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.ValueRange;
import java.time.temporal.WeekFields;
import java.time.zone.ZoneRulesProvider;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;
import sun.util.locale.provider.TimeZoneNameUtility;

/**
 * 构建器，用于创建日期时间格式化器。
 * <p>
 * 此类允许创建 {@code DateTimeFormatter}。
 * 所有日期时间格式化器最终都是使用此构建器创建的。
 * <p>
 * 日期时间的所有基本元素都可以添加：
 * <ul>
 * <li>值 - 数值</li>
 * <li>分数 - 包含小数点的分数值。输出分数时应始终使用此方法，以确保分数被正确解析</li>
 * <li>文本 - 值的文本等效形式</li>
 * <li>OffsetId/Offset - {@linkplain ZoneOffset 时区偏移量}</li>
 * <li>ZoneId - {@linkplain ZoneId 时区} id</li>
 * <li>ZoneText - 时区名称</li>
 * <li>ChronologyId - {@linkplain Chronology 历法} id</li>
 * <li>ChronologyText - 历法名称</li>
 * <li>字面量 - 文本字面量</li>
 * <li>嵌套和可选 - 格式可以嵌套或设为可选</li>
 * </ul>
 * 此外，任何元素都可以通过空格或其他字符进行填充装饰。
 * <p>
 * 最后，可以使用与 {@code java.text.SimpleDateFormat SimpleDateFormat} 大致兼容的简写模式，
 * 请参见 {@link #appendPattern(String)}。
 * 实际上，这仅仅是解析模式并调用构建器上的其他方法。
 *
 * @implSpec
 * 此类是一个旨在单线程使用的可变构建器。
 *
 * @since 1.8
 */
public final class DateTimeFormatterBuilder {

    /**
     * 查询仅限地区的时区。
     */
    private static final TemporalQuery<ZoneId> QUERY_REGION_ONLY = (temporal) -> {
        ZoneId zone = temporal.query(TemporalQueries.zoneId());
        return (zone != null && zone instanceof ZoneOffset == false ? zone : null);
    };

    /**
     * 当前活动的构建器，由最外层的构建器使用。
     */
    private DateTimeFormatterBuilder active = this;
    /**
     * 父构建器，对于最外层的构建器为 null。
     */
    private final DateTimeFormatterBuilder parent;
    /**
     * 将要使用的打印机列表。
     */
    private final List<DateTimePrinterParser> printerParsers = new ArrayList<>();
    /**
     * 此构建器是否生成可选格式化器。
     */
    private final boolean optional;
    /**
     * 下一个字段填充的宽度。
     */
    private int padNextWidth;
    /**
     * 下一个字段填充的字符。
     */
    private char padNextChar;
    /**
     * 最后一个可变宽度值解析器的索引。
     */
    private int valueParserIndex = -1;

    /**
     * 获取特定地区和历法的日期和时间样式的格式化模式。
     * 地区和历法用于查找请求的 dateStyle 和/或 timeStyle 的特定于地区的格式。
     *
     * @param dateStyle  日期的 FormatStyle，仅时间模式为 null
     * @param timeStyle  时间的 FormatStyle，仅日期模式为 null
     * @param chrono  历法，非 null
     * @param locale  地区，非 null
     * @return 特定于地区和历法的格式化模式
     * @throws IllegalArgumentException 如果 dateStyle 和 timeStyle 都为 null
     */
    public static String getLocalizedDateTimePattern(FormatStyle dateStyle, FormatStyle timeStyle,
            Chronology chrono, Locale locale) {
        Objects.requireNonNull(locale, "locale");
        Objects.requireNonNull(chrono, "chrono");
        if (dateStyle == null && timeStyle == null) {
            throw new IllegalArgumentException("Either dateStyle or timeStyle must be non-null");
        }
        LocaleResources lr = LocaleProviderAdapter.getResourceBundleBased().getLocaleResources(locale);
        String pattern = lr.getJavaTimeDateTimePattern(
                convertStyle(timeStyle), convertStyle(dateStyle), chrono.getCalendarType());
        return pattern;
    }

    /**
     * 将给定的 FormatStyle 转换为 java.text.DateFormat 样式。
     *
     * @param style  FormatStyle 样式
     * @return int 样式，如果 style 为 null，则返回 -1，表示不需要
     */
    private static int convertStyle(FormatStyle style) {
        if (style == null) {
            return -1;
        }
        return style.ordinal();  // 索引恰好对齐
    }

    /**
     * 构造构建器的新实例。
     */
    public DateTimeFormatterBuilder() {
        super();
        parent = null;
        optional = false;
    }

    /**
     * 构造构建器的新实例。
     *
     * @param parent  父构建器，非 null
     * @param optional  格式化器是否可选，非 null
     */
    private DateTimeFormatterBuilder(DateTimeFormatterBuilder parent, boolean optional) {
        super();
        this.parent = parent;
        this.optional = optional;
    }

    //-----------------------------------------------------------------------
    /**
     * 将解析样式更改为对格式化器其余部分的解析敏感。
     * <p>
     * 解析可以是大小写敏感或不敏感，默认情况下是大小写敏感的。
     * 此方法允许更改解析的大小写敏感性设置。
     * <p>
     * 调用此方法会更改构建器的状态，使所有
     * 随后的构建器方法调用将以大小写敏感模式解析文本。
     * 请参见 {@link #parseCaseInsensitive} 以获取相反的设置。
     * 解析大小写敏感/不敏感方法可以在构建器的任何点调用，因此解析器可以在解析过程中
     * 多次在大小写解析模式之间切换。
     * <p>
     * 由于默认是大小写敏感的，因此此方法通常仅在
     * 调用 {@code #parseCaseInsensitive} 之后使用。
     *
     * @return this，用于链式调用，非 null
     */
    public DateTimeFormatterBuilder parseCaseSensitive() {
        appendInternal(SettingsParser.SENSITIVE);
        return this;
    }

    /**
     * 将解析样式更改为对格式化器其余部分的解析不敏感。
     * <p>
     * 解析可以是大小写敏感或不敏感，默认情况下是大小写敏感的。
     * 此方法允许更改解析的大小写敏感性设置。
     * <p>
     * 调用此方法会更改构建器的状态，使所有
     * 随后的构建器方法调用将以大小写不敏感模式解析文本。
     * 请参见 {@link #parseCaseSensitive()} 以获取相反的设置。
     * 解析大小写敏感/不敏感方法可以在构建器的任何点调用，因此解析器可以在解析过程中
     * 多次在大小写解析模式之间切换。
     *
     * @return this，用于链式调用，非 null
     */
    public DateTimeFormatterBuilder parseCaseInsensitive() {
        appendInternal(SettingsParser.INSENSITIVE);
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 将解析样式更改为对格式化器其余部分的解析严格。
     * <p>
     * 解析可以是严格的或宽松的，默认情况下是严格的。
     * 这控制了解析文本和符号样式的灵活性程度。
     * <p>
     * 使用时，此方法会从这一点开始将解析更改为严格模式。
     * 由于默认是严格的，因此通常仅在调用 {@link #parseLenient()} 之后需要此方法。
     * 更改将一直有效，直到最终构建的格式化器结束或调用 {@code parseLenient} 为止。
     *
     * @return this，用于链式调用，非 null
     */
    public DateTimeFormatterBuilder parseStrict() {
        appendInternal(SettingsParser.STRICT);
        return this;
    }

    /**
     * 将解析样式更改为对格式化器其余部分的解析宽松。
     * 注意，大小写敏感性是单独设置的。
     * <p>
     * 解析可以是严格的或宽松的，默认情况下是严格的。
     * 这控制了解析文本和符号样式的灵活性程度。
     * 调用此方法的应用程序通常还应调用 {@link #parseCaseInsensitive()}。
     * <p>
     * 使用时，此方法会从这一点开始将解析更改为宽松模式。
     * 更改将一直有效，直到最终构建的格式化器结束或调用 {@code parseStrict} 为止。
     *
     * @return this，用于链式调用，非 null
     */
    public DateTimeFormatterBuilder parseLenient() {
        appendInternal(SettingsParser.LENIENT);
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 将字段的默认值附加到格式化器以供解析使用。
     * <p>
     * 这会向构建器附加一条指令，以将默认值注入
     * 解析结果中。这与格式化器的可选部分结合使用特别有用。
     * <p>
     * 例如，考虑一个解析年份，后跟
     * 可选月份，再跟可选日的格式化器。使用这样的
     * 格式化器将要求调用代码检查是否解析了完整日期、年-月或仅年份。
     * 此方法可以用于将月份和日默认为合理的值，例如
     * 月份的第一天，使调用代码始终可以获得日期。
     * <p>
     * 在格式化期间，此方法没有效果。
     * <p>
     * 在解析期间，会检查解析的当前状态。
     * 如果指定的字段没有关联的值，因为该点尚未成功解析，
     * 则将指定的值注入解析结果中。注入是立即的，因此字段-值对
     * 将对格式化器中的任何后续元素可见。
     * 因此，此方法通常在构建器的末尾调用。
     *
     * @param field  要默认值的字段，非 null
     * @param value  要默认的字段值
     * @return this，用于链式调用，非 null
     */
    public DateTimeFormatterBuilder parseDefaulting(TemporalField field, long value) {
        Objects.requireNonNull(field, "field");
        appendInternal(new DefaultValueParser(field, value));
        return this;
    }


                //-----------------------------------------------------------------------
    /**
     * 将日期时间字段的值附加到格式化程序，使用正常的输出样式。
     * <p>
     * 字段的值将在格式化期间输出。
     * 如果无法获取值，则会抛出异常。
     * <p>
     * 值将按照整数值的正常格式打印。
     * 仅负数会带有符号。不会添加填充。
     * <p>
     * 变长值（如此类值）的解析器通常表现为贪婪模式，
     * 需要一个数字，但接受尽可能多的数字。
     * 这种行为可以通过“相邻值解析”来影响。
     * 有关详细信息，请参见 {@link #appendValue(java.time.temporal.TemporalField, int)}。
     *
     * @param field  要附加的字段，不允许为 null
     * @return this，用于链式调用，不允许为 null
     */
    public DateTimeFormatterBuilder appendValue(TemporalField field) {
        Objects.requireNonNull(field, "field");
        appendValue(new NumberPrinterParser(field, 1, 19, SignStyle.NORMAL));
        return this;
    }

    /**
     * 将日期时间字段的值附加到格式化程序，使用固定宽度和零填充的方法。
     * <p>
     * 字段的值将在格式化期间输出。
     * 如果无法获取值，则会抛出异常。
     * <p>
     * 值将在左侧用零填充。如果值的大小意味着它不能在指定的宽度内打印，则会抛出异常。
     * 如果字段的值为负数，则在格式化期间会抛出异常。
     * <p>
     * 此方法支持一种称为“相邻值解析”的特殊解析技术。
     * 这种技术解决了变长或固定宽度的值后面紧跟着一个或多个固定长度值的问题。
     * 标准解析器是贪婪的，因此它通常会占用固定宽度值解析器所需的数字。
     * <p>
     * 无需任何操作即可启动“相邻值解析”。
     * 当调用 {@code appendValue} 时，构建器
     * 进入相邻值解析设置模式。如果紧接着在同一个构建器上的方法调用或调用是固定宽度值，
     * 则解析器将保留空间以便固定宽度值可以解析。
     * <p>
     * 例如，考虑 {@code builder.appendValue(YEAR).appendValue(MONTH_OF_YEAR, 2);}
     * 年份是一个变长解析，范围在 1 到 19 位数字之间。
     * 月份是一个固定宽度解析，为 2 位数字。
     * 由于这些是在同一个构建器上立即追加的，
     * 年份解析器将为月份保留两个数字。
     * 因此，文本 '201106' 将正确解析为年份 2011 和月份 6。
     * 没有相邻值解析，年份会贪婪地解析所有六个数字，而月份则没有数字可解析。
     * <p>
     * 相邻值解析适用于解析器中紧随任何类型值（变长或固定宽度）之后的每组固定宽度非负值。
     * 调用任何其他附加方法将结束相邻值解析的设置。
     * 因此，如果您需要避免相邻值解析行为，
     * 只需将 {@code appendValue} 添加到另一个 {@code DateTimeFormatterBuilder}
     * 并将其添加到此构建器。
     * <p>
     * 如果相邻解析处于活动状态，则在严格和宽松模式下解析必须精确匹配指定的
     * 数字数量。此外，不允许使用正负号。
     *
     * @param field  要附加的字段，不允许为 null
     * @param width  打印字段的宽度，从 1 到 19
     * @return this，用于链式调用，不允许为 null
     * @throws IllegalArgumentException 如果宽度无效
     */
    public DateTimeFormatterBuilder appendValue(TemporalField field, int width) {
        Objects.requireNonNull(field, "field");
        if (width < 1 || width > 19) {
            throw new IllegalArgumentException("The width must be from 1 to 19 inclusive but was " + width);
        }
        NumberPrinterParser pp = new NumberPrinterParser(field, width, width, SignStyle.NOT_NEGATIVE);
        appendValue(pp);
        return this;
    }

    /**
     * 将日期时间字段的值附加到格式化程序，提供对格式化的完全控制。
     * <p>
     * 字段的值将在格式化期间输出。
     * 如果无法获取值，则会抛出异常。
     * <p>
     * 此方法提供了对数值格式化的完全控制，包括
     * 零填充和正负号。
     * <p>
     * 变长值（如此类值）的解析器通常表现为贪婪模式，
     * 接受尽可能多的数字。
     * 这种行为可以通过“相邻值解析”来影响。
     * 有关详细信息，请参见 {@link #appendValue(java.time.temporal.TemporalField, int)}。
     * <p>
     * 在严格解析模式下，解析的最小数字数量是 {@code minWidth}
     * 最大数量是 {@code maxWidth}。
     * 在宽松解析模式下，解析的最小数字数量是 1
     * 最大数量是 19（除非受相邻值解析限制）。
     * <p>
     * 如果此方法以相等的最小和最大宽度以及正负号样式为
     * {@code NOT_NEGATIVE} 调用，则它将委托给 {@code appendValue(TemporalField,int)}。
     * 在这种情况下，将发生那里描述的格式化和解析行为。
     *
     * @param field  要附加的字段，不允许为 null
     * @param minWidth  打印字段的最小宽度，从 1 到 19
     * @param maxWidth  打印字段的最大宽度，从 1 到 19
     * @param signStyle  正负号输出样式，不允许为 null
     * @return this，用于链式调用，不允许为 null
     * @throws IllegalArgumentException 如果宽度无效
     */
    public DateTimeFormatterBuilder appendValue(
            TemporalField field, int minWidth, int maxWidth, SignStyle signStyle) {
        if (minWidth == maxWidth && signStyle == SignStyle.NOT_NEGATIVE) {
            return appendValue(field, maxWidth);
        }
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(signStyle, "signStyle");
        if (minWidth < 1 || minWidth > 19) {
            throw new IllegalArgumentException("The minimum width must be from 1 to 19 inclusive but was " + minWidth);
        }
        if (maxWidth < 1 || maxWidth > 19) {
            throw new IllegalArgumentException("The maximum width must be from 1 to 19 inclusive but was " + maxWidth);
        }
        if (maxWidth < minWidth) {
            throw new IllegalArgumentException("The maximum width must exceed or equal the minimum width but " +
                    maxWidth + " < " + minWidth);
        }
        NumberPrinterParser pp = new NumberPrinterParser(field, minWidth, maxWidth, signStyle);
        appendValue(pp);
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 将日期时间字段的简化值附加到格式化程序。
     * <p>
     * 由于年份等字段因历法而异，建议在大多数情况下使用
     * {@link #appendValueReduced(TemporalField, int, int, ChronoLocalDate)} 日期}
     * 变体。此变体适用于简单字段或仅使用 ISO 历法。
     * <p>
     * 对于格式化，使用 {@code width} 和 {@code maxWidth} 来
     * 确定要格式化的字符数量。
     * 如果它们相等，则格式为固定宽度。
     * 如果字段的值在 {@code baseValue} 的范围内
     * 使用 {@code width} 个字符，则格式化简化值，否则值将
     * 被截断以适应 {@code maxWidth}。
     * 输出最右侧的字符以匹配宽度，左侧用零填充。
     * <p>
     * 对于严格解析，允许解析 {@code width} 到 {@code maxWidth} 之间的字符数量。
     * 对于宽松解析，解析的字符数量必须至少为 1 且小于 10。
     * 如果解析的数字数量等于 {@code width} 且值为正数，
     * 字段的值将计算为大于或等于
     * 或等于 {@code baseValue} 且具有相同最低有效字符的第一个数字，
     * 否则解析的值为字段值。
     * 这允许在 baseValue 和宽度范围内的值输入简化值，
     * 而范围外的值可以输入绝对值。
     * <p>
     * 例如，基数为 {@code 1980} 且宽度为 {@code 2} 将有
     * 有效值从 {@code 1980} 到 {@code 2079}。
     * 在解析期间，文本 {@code "12"} 将解析为值 {@code 2012}，因为这是
     * 最后两位字符为 "12" 的范围内值。
     * 相比之下，解析文本 {@code "1915"} 将解析为值 {@code 1915}。
     *
     * @param field  要附加的字段，不允许为 null
     * @param width  打印和解析字段的宽度，从 1 到 10
     * @param maxWidth  打印字段的最大宽度，从 1 到 10
     * @param baseValue  有效值范围的基数
     * @return this，用于链式调用，不允许为 null
     * @throws IllegalArgumentException 如果宽度或基数无效
     */
    public DateTimeFormatterBuilder appendValueReduced(TemporalField field,
            int width, int maxWidth, int baseValue) {
        Objects.requireNonNull(field, "field");
        ReducedPrinterParser pp = new ReducedPrinterParser(field, width, maxWidth, baseValue, null);
        appendValue(pp);
        return this;
    }

    /**
     * 将日期时间字段的简化值附加到格式化程序。
     * <p>
     * 这通常用于格式化和解析两位数的年份。
     * <p>
     * 基准日期用于在解析期间计算完整值。
     * 例如，如果基准日期是 1950-01-01，则解析的两位数年份值
     * 将在 1950-01-01 到 2049-12-31 的范围内。
     * 只会从日期中提取年份，因此基准日期为
     * 1950-08-25 也将解析为 1950-01-01 到 2049-12-31 的范围。
     * 这种行为是必要的，以支持基于周的年份
     * 或其他日历系统，其中解析的值不与
     * 标准 ISO 年份对齐。
     * <p>
     * 具体行为如下。解析所有字段并
     * 确定有效的历法，如果历法出现多次，则使用最后一个历法。
     * 然后将基准日期转换为
     * 有效的历法。然后从历法特定的基准日期中提取指定的字段并使用它来确定
     * 以下使用的 {@code baseValue}。
     * <p>
     * 对于格式化，使用 {@code width} 和 {@code maxWidth} 来
     * 确定要格式化的字符数量。
     * 如果它们相等，则格式为固定宽度。
     * 如果字段的值在 {@code baseValue} 的范围内
     * 使用 {@code width} 个字符，则格式化简化值，否则值将
     * 被截断以适应 {@code maxWidth}。
     * 输出最右侧的字符以匹配宽度，左侧用零填充。
     * <p>
     * 对于严格解析，允许解析 {@code width} 到 {@code maxWidth} 之间的字符数量。
     * 对于宽松解析，解析的字符数量必须至少为 1 且小于 10。
     * 如果解析的数字数量等于 {@code width} 且值为正数，
     * 字段的值将计算为大于或等于
     * 或等于 {@code baseValue} 且具有相同最低有效字符的第一个数字，
     * 否则解析的值为字段值。
     * 这允许在 baseValue 和宽度范围内的值输入简化值，
     * 而范围外的值可以输入绝对值。
     * <p>
     * 例如，基数为 {@code 1980} 且宽度为 {@code 2} 将有
     * 有效值从 {@code 1980} 到 {@code 2079}。
     * 在解析期间，文本 {@code "12"} 将解析为值 {@code 2012}，因为这是
     * 最后两位字符为 "12" 的范围内值。
     * 相比之下，解析文本 {@code "1915"} 将解析为值 {@code 1915}。
     *
     * @param field  要附加的字段，不允许为 null
     * @param width  打印和解析字段的宽度，从 1 到 10
     * @param maxWidth  打印字段的最大宽度，从 1 到 10
     * @param baseDate  用于计算解析历法中有效值范围的基数的基准日期，不允许为 null
     * @return this，用于链式调用，不允许为 null
     * @throws IllegalArgumentException 如果宽度或基数无效
     */
    public DateTimeFormatterBuilder appendValueReduced(
            TemporalField field, int width, int maxWidth, ChronoLocalDate baseDate) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(baseDate, "baseDate");
        ReducedPrinterParser pp = new ReducedPrinterParser(field, width, maxWidth, 0, baseDate);
        appendValue(pp);
        return this;
    }

    /**
     * 附加一个固定或变长的打印机-解析器，处理相邻值模式。
     * 如果没有活动的 PrinterParser，则新的 PrinterParser 成为
     * 活动的 PrinterParser。
     * 否则，根据新的 PrinterParser 修改活动的 PrinterParser。
     * 如果新的 PrinterParser 是固定宽度且具有正负号样式 {@code NOT_NEGATIVE}
     * 则将其宽度添加到活动 PP 的后续宽度中，
     * 并将新的 PrinterParser 强制为固定宽度。
     * 如果新的 PrinterParser 是变长的，则将活动的 PrinterParser 更改为
     * 固定宽度，新的 PrinterParser 成为新的活动 PP。
     *
     * @param pp  打印机-解析器，不允许为 null
     * @return this，用于链式调用，不允许为 null
     */
    private DateTimeFormatterBuilder appendValue(NumberPrinterParser pp) {
        if (active.valueParserIndex >= 0) {
            final int activeValueParser = active.valueParserIndex;

            // 相邻解析模式，更新前一个解析器的设置
            NumberPrinterParser basePP = (NumberPrinterParser) active.printerParsers.get(activeValueParser);
            if (pp.minWidth == pp.maxWidth && pp.signStyle == SignStyle.NOT_NEGATIVE) {
                // 将宽度附加到活动解析器的后续宽度
                basePP = basePP.withSubsequentWidth(pp.maxWidth);
                // 以固定宽度附加新的解析器
                appendInternal(pp.withFixedWidth());
                // 保留前一个活动解析器
                active.valueParserIndex = activeValueParser;
            } else {
                // 将活动解析器更改为固定宽度
                basePP = basePP.withFixedWidth();
                // 新的解析器成为新的活动解析器
                active.valueParserIndex = appendInternal(pp);
            }
            // 用更新的解析器替换修改后的解析器
            active.printerParsers.set(activeValueParser, basePP);
        } else {
            // 新的解析器成为活动解析器
            active.valueParserIndex = appendInternal(pp);
        }
        return this;
    }


                //-----------------------------------------------------------------------
    /**
     * 将日期时间字段的小数值追加到格式化器。
     * <p>
     * 字段的小数值将包括前面的小数点一起输出。前面的值不会输出。
     * 例如，分钟的秒数值为15将输出为 {@code .25}。
     * <p>
     * 可以控制打印的小数位数。将最小宽度设置为零将不会生成任何输出。
     * 打印的小数将具有最小和最大宽度之间的必要宽度 - 尾随零被省略。
     * 由于最大宽度不会发生四舍五入 - 数字会被简单地丢弃。
     * <p>
     * 在严格模式下解析时，解析的数字位数必须在最小和最大宽度之间。在宽松模式下解析时，最小
     * 宽度被视为零，最大宽度为九。
     * <p>
     * 如果无法获取值，则会抛出异常。如果值为负数，则会抛出异常。
     * 如果字段没有固定的一组有效值，则会抛出异常。
     * 如果要打印的日期时间中的字段值无效，则无法打印并会抛出异常。
     *
     * @param field  要追加的字段，不为空
     * @param minWidth  字段的最小宽度（不包括小数点），从0到9
     * @param maxWidth  字段的最大宽度（不包括小数点），从1到9
     * @param decimalPoint  是否输出本地化的小数点符号
     * @return this，用于链式调用，不为空
     * @throws IllegalArgumentException 如果字段有一组可变的有效值或
     * 任意宽度无效
     */
    public DateTimeFormatterBuilder appendFraction(
            TemporalField field, int minWidth, int maxWidth, boolean decimalPoint) {
        appendInternal(new FractionPrinterParser(field, minWidth, maxWidth, decimalPoint));
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 使用完整文本样式将日期时间字段的文本追加到格式化器。
     * <p>
     * 在格式化期间，字段的文本将被输出。
     * 值必须在字段的有效范围内。
     * 如果无法获取值，则会抛出异常。如果字段没有文本表示，则使用数值。
     * <p>
     * 值将按照整数值的正常格式打印。只有负数会被加上符号。不会添加填充。
     *
     * @param field  要追加的字段，不为空
     * @return this，用于链式调用，不为空
     */
    public DateTimeFormatterBuilder appendText(TemporalField field) {
        return appendText(field, TextStyle.FULL);
    }

    /**
     * 将日期时间字段的文本追加到格式化器。
     * <p>
     * 在格式化期间，字段的文本将被输出。
     * 值必须在字段的有效范围内。
     * 如果无法获取值，则会抛出异常。如果字段没有文本表示，则使用数值。
     * <p>
     * 值将按照整数值的正常格式打印。只有负数会被加上符号。不会添加填充。
     *
     * @param field  要追加的字段，不为空
     * @param textStyle  要使用的文本样式，不为空
     * @return this，用于链式调用，不为空
     */
    public DateTimeFormatterBuilder appendText(TemporalField field, TextStyle textStyle) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(textStyle, "textStyle");
        appendInternal(new TextPrinterParser(field, textStyle, DateTimeTextProvider.getInstance()));
        return this;
    }

    /**
     * 使用指定的映射来提供文本，将日期时间字段的文本追加到格式化器。
     * <p>
     * 标准文本输出方法使用JDK中的本地化文本。此方法允许直接指定该文本。
     * 提供的映射不会由构建器验证以确保格式化或解析是可能的，因此无效的映射可能在稍后使用时抛出错误。
     * <p>
     * 提供文本映射在格式化和解析中提供了相当大的灵活性。
     * 例如，一个遗留应用程序可能需要或提供一年中的月份为 "JNY"、"FBY"、"MCH" 等。这些不匹配标准的本地化月份名称。
     * 使用此方法，可以创建一个映射，定义每个值和文本之间的连接：
     * <pre>
     * Map&lt;Long, String&gt; map = new HashMap&lt;&gt;();
     * map.put(1L, "JNY");
     * map.put(2L, "FBY");
     * map.put(3L, "MCH";
     * ...
     * builder.appendText(MONTH_OF_YEAR, map);
     * </pre>
     * <p>
     * 其他用途可能是以带有后缀的方式输出值，如 "1st"、"2nd"、"3rd"，或作为罗马数字 "I"、"II"、"III"、"IV"。
     * <p>
     * 在格式化期间，获取值并检查其是否在有效范围内。如果值没有可用的文本，则输出为数字。
     * 在解析期间，解析器将匹配映射中的文本和数值。
     *
     * @param field  要追加的字段，不为空
     * @param textLookup  从值到文本的映射
     * @return this，用于链式调用，不为空
     */
    public DateTimeFormatterBuilder appendText(TemporalField field, Map<Long, String> textLookup) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(textLookup, "textLookup");
        Map<Long, String> copy = new LinkedHashMap<>(textLookup);
        Map<TextStyle, Map<Long, String>> map = Collections.singletonMap(TextStyle.FULL, copy);
        final LocaleStore store = new LocaleStore(map);
        DateTimeTextProvider provider = new DateTimeTextProvider() {
            @Override
            public String getText(Chronology chrono, TemporalField field,
                                  long value, TextStyle style, Locale locale) {
                return store.getText(value, style);
            }
            @Override
            public String getText(TemporalField field, long value, TextStyle style, Locale locale) {
                return store.getText(value, style);
            }
            @Override
            public Iterator<Entry<String, Long>> getTextIterator(Chronology chrono,
                    TemporalField field, TextStyle style, Locale locale) {
                return store.getTextIterator(style);
            }
            @Override
            public Iterator<Entry<String, Long>> getTextIterator(TemporalField field,
                    TextStyle style, Locale locale) {
                return store.getTextIterator(style);
            }
        };
        appendInternal(new TextPrinterParser(field, TextStyle.FULL, provider));
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 使用ISO-8601将瞬时值追加到格式化器，以三位一组的方式格式化小数位。
     * <p>
     * 瞬时值具有固定的输出格式。它们被转换为带有UTC时区偏移的日期时间，并使用标准的ISO-8601格式进行格式化。
     * 使用此方法，格式化纳秒会根据需要输出零、三位、六位或九位数字。不会使用本地化的小数样式。
     * <p>
     * 瞬时值使用 {@link ChronoField#INSTANT_SECONDS INSTANT_SECONDS} 和可选的 (@code NANO_OF_SECOND) 获取。
     * {@code INSTANT_SECONDS} 的值可能超出 {@code LocalDateTime} 的最大范围。
     * <p>
     * 解析器样式对瞬时值的解析没有影响。一天结束的时间 '24:00' 被处理为次日午夜的开始。
     * 跃秒时间 '23:59:59' 会被处理到一定程度，详见 {@link DateTimeFormatter#parsedLeapSecond()} 的完整细节。
     * <p>
     * 作为此方法的替代，可以将瞬时值格式化/解析为单个纪元秒值。这可以通过使用 {@code appendValue(INSTANT_SECONDS)} 实现。
     *
     * @return this，用于链式调用，不为空
     */
    public DateTimeFormatterBuilder appendInstant() {
        appendInternal(new InstantPrinterParser(-2));
        return this;
    }

    /**
     * 使用ISO-8601将瞬时值追加到格式化器，并控制小数位数。
     * <p>
     * 瞬时值具有固定的输出格式，尽管此方法提供了一些对小数位数的控制。它们被转换为带有UTC时区偏移的日期时间，并使用标准的ISO-8601格式进行打印。不会使用本地化的小数样式。
     * <p>
     * {@code fractionalDigits} 参数允许控制小数秒的输出。指定零将不会输出小数位。从1到9将输出越来越多的数字，必要时使用零右填充。特殊值 -1 用于输出尽可能多的数字以避免任何尾随零。
     * <p>
     * 在严格模式下解析时，解析的数字位数必须与小数位数匹配。在宽松模式下解析时，接受零到九位的任何小数位数。
     * <p>
     * 瞬时值使用 {@link ChronoField#INSTANT_SECONDS INSTANT_SECONDS} 和可选的 (@code NANO_OF_SECOND) 获取。
     * {@code INSTANT_SECONDS} 的值可能超出 {@code LocalDateTime} 的最大范围。
     * <p>
     * 解析器样式对瞬时值的解析没有影响。一天结束的时间 '24:00' 被处理为次日午夜的开始。
     * 跃秒时间 '23:59:60' 会被处理到一定程度，详见 {@link DateTimeFormatter#parsedLeapSecond()} 的完整细节。
     * <p>
     * 作为此方法的替代，可以将瞬时值格式化/解析为单个纪元秒值。这可以通过使用 {@code appendValue(INSTANT_SECONDS)} 实现。
     *
     * @param fractionalDigits  要格式化的小数秒位数，从0到9，或-1以使用尽可能多的数字
     * @return this，用于链式调用，不为空
     */
    public DateTimeFormatterBuilder appendInstant(int fractionalDigits) {
        if (fractionalDigits < -1 || fractionalDigits > 9) {
            throw new IllegalArgumentException("The fractional digits must be from -1 to 9 inclusive but was " + fractionalDigits);
        }
        appendInternal(new InstantPrinterParser(fractionalDigits));
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 将时区偏移（如 '+01:00'）追加到格式化器。
     * <p>
     * 这将追加一个指令以格式化/解析偏移ID到构建器。这等同于调用 {@code appendOffset("+HH:MM:ss", "Z")}。
     *
     * @return this，用于链式调用，不为空
     */
    public DateTimeFormatterBuilder appendOffsetId() {
        appendInternal(OffsetIdPrinterParser.INSTANCE_ID_Z);
        return this;
    }

    /**
     * 将时区偏移（如 '+01:00'）追加到格式化器。
     * <p>
     * 这将追加一个指令以格式化/解析偏移ID到构建器。
     * <p>
     * 在格式化期间，偏移使用等效于查询时间对象的 {@link TemporalQueries#offset()} 的机制获取。
     * 它将使用下面定义的格式进行打印。如果无法获取偏移，则会抛出异常，除非格式化器的该部分是可选的。
     * <p>
     * 在解析期间，偏移使用下面定义的格式进行解析。如果无法解析偏移，则会抛出异常，除非格式化器的该部分是可选的。
     * <p>
     * 偏移的格式由一个模式控制，该模式必须是以下之一：
     * <ul>
     * <li>{@code +HH} - 仅小时，忽略分钟和秒
     * <li>{@code +HHmm} - 小时，如果分钟非零则包含分钟，忽略秒，不带冒号
     * <li>{@code +HH:mm} - 小时，如果分钟非零则包含分钟，忽略秒，带冒号
     * <li>{@code +HHMM} - 小时和分钟，忽略秒，不带冒号
     * <li>{@code +HH:MM} - 小时和分钟，忽略秒，带冒号
     * <li>{@code +HHMMss} - 小时和分钟，如果秒非零则包含秒，不带冒号
     * <li>{@code +HH:MM:ss} - 小时和分钟，如果秒非零则包含秒，带冒号
     * <li>{@code +HHMMSS} - 小时、分钟和秒，不带冒号
     * <li>{@code +HH:MM:SS} - 小时、分钟和秒，带冒号
     * </ul>
     * “无偏移”文本控制当输出的偏移字段总和为零时打印的文本。
     * 示例值可以是 'Z'、'+00:00'、'UTC' 或 'GMT'。解析UTC时接受三种格式 - “无偏移”文本，以及由模式定义的零的加号和减号版本。
     *
     * @param pattern  要使用的模式，不为空
     * @param noOffsetText  偏移为零时使用的文本，不为空
     * @return this，用于链式调用，不为空
     */
    public DateTimeFormatterBuilder appendOffset(String pattern, String noOffsetText) {
        appendInternal(new OffsetIdPrinterParser(pattern, noOffsetText));
        return this;
    }

    /**
     * 将本地化的时区偏移（如 'GMT+01:00'）追加到格式化器。
     * <p>
     * 这将追加一个本地化的时区偏移到构建器，本地化偏移的格式由传递给此方法的 {@link FormatStyle 样式} 控制：
     * <ul>
     * <li>{@link TextStyle#FULL 完整} - 以本地化的偏移文本格式化，如 'GMT'，两位小时和分钟字段，如果秒非零则包含秒字段，带冒号。
     * <li>{@link TextStyle#SHORT 简短} - 以本地化的偏移文本格式化，如 'GMT'，小时不带前导零，如果分钟和秒非零则包含两位分钟和秒，带冒号。
     * </ul>
     * <p>
     * 在格式化期间，偏移使用等效于查询时间对象的 {@link TemporalQueries#offset()} 的机制获取。
     * 如果无法获取偏移，则会抛出异常，除非格式化器的该部分是可选的。
     * <p>
     * 在解析期间，偏移使用上面定义的格式进行解析。如果无法解析偏移，则会抛出异常，除非格式化器的该部分是可选的。
     * <p>
     * @param style  要使用的格式样式，不为空
     * @return this，用于链式调用，不为空
     * @throws IllegalArgumentException 如果样式既不是 {@link TextStyle#FULL 完整} 也不是 {@link TextStyle#SHORT 简短}
     */
    public DateTimeFormatterBuilder appendLocalizedOffset(TextStyle style) {
        Objects.requireNonNull(style, "style");
        if (style != TextStyle.FULL && style != TextStyle.SHORT) {
            throw new IllegalArgumentException("Style must be either full or short");
        }
        appendInternal(new LocalizedOffsetIdPrinterParser(style));
        return this;
    }


                //-----------------------------------------------------------------------
    /**
     * 将时区ID（如 'Europe/Paris' 或 '+02:00'）附加到格式化器。
     * <p>
     * 这将附加一个指令，以格式化/解析时区ID到构建器。
     * 时区ID以严格的方式获取，适用于 {@code ZonedDateTime}。
     * 相反，{@code OffsetDateTime} 没有适合使用此方法的时区ID，
     * 请参见 {@link #appendZoneOrOffsetId()}。
     * <p>
     * 在格式化期间，时区通过等效于使用 {@link TemporalQueries#zoneId()} 查询时间对象的方式获取。
     * 它将使用 {@link ZoneId#getId()} 的结果打印。
     * 如果无法获取时区，则除非格式化器的该部分是可选的，否则将抛出异常。
     * <p>
     * 在解析期间，文本必须匹配已知的时区或偏移量。
     * 时区ID有两种类型，基于偏移量的，如 '+01:30' 和基于区域的，如 'Europe/London'。这些解析方式不同。
     * 如果解析以 '+', '-', 'UT', 'UTC' 或 'GMT' 开头，那么解析器期望一个基于偏移量的时区，并且不会匹配基于区域的时区。
     * 偏移量ID，如 '+02:30'，可以在解析的开始处，或者由 'UT', 'UTC' 或 'GMT' 前缀。偏移量ID的解析等效于使用
     * {@link #appendOffset(String, String)}，参数为 'HH:MM:ss' 和无偏移字符串 '0'。
     * 如果解析以 'UT', 'UTC' 或 'GMT' 开头，并且解析器无法匹配后续的偏移量ID，则选择 {@link ZoneOffset#UTC}。
     * 在所有其他情况下，使用已知的基于区域的时区列表来查找最长的可用匹配。如果未找到匹配，并且解析以 'Z' 开头，则选择 {@code ZoneOffset.UTC}。
     * 解析器使用 {@linkplain #parseCaseInsensitive() 不区分大小写} 设置。
     * <p>
     * 例如，以下将解析：
     * <pre>
     *   "Europe/London"           -- ZoneId.of("Europe/London")
     *   "Z"                       -- ZoneOffset.UTC
     *   "UT"                      -- ZoneId.of("UT")
     *   "UTC"                     -- ZoneId.of("UTC")
     *   "GMT"                     -- ZoneId.of("GMT")
     *   "+01:30"                  -- ZoneOffset.of("+01:30")
     *   "UT+01:30"                -- ZoneOffset.of("+01:30")
     *   "UTC+01:30"               -- ZoneOffset.of("+01:30")
     *   "GMT+01:30"               -- ZoneOffset.of("+01:30")
     * </pre>
     *
     * @return this, 用于链式调用，不为空
     * @see #appendZoneRegionId()
     */
    public DateTimeFormatterBuilder appendZoneId() {
        appendInternal(new ZoneIdPrinterParser(TemporalQueries.zoneId(), "ZoneId()"));
        return this;
    }

    /**
     * 将时区区域ID（如 'Europe/Paris'）附加到格式化器，如果时区ID是 {@code ZoneOffset} 则拒绝。
     * <p>
     * 这将附加一个指令，以格式化/解析时区ID到构建器，仅当它是基于区域的ID时。
     * <p>
     * 在格式化期间，时区通过等效于使用 {@link TemporalQueries#zoneId()} 查询时间对象的方式获取。
     * 如果时区是 {@code ZoneOffset} 或无法获取，则除非格式化器的该部分是可选的，否则将抛出异常。
     * 如果时区不是偏移量，则将使用 {@link ZoneId#getId()} 的区域ID打印时区。
     * <p>
     * 在解析期间，文本必须匹配已知的时区或偏移量。
     * 时区ID有两种类型，基于偏移量的，如 '+01:30' 和基于区域的，如 'Europe/London'。这些解析方式不同。
     * 如果解析以 '+', '-', 'UT', 'UTC' 或 'GMT' 开头，那么解析器期望一个基于偏移量的时区，并且不会匹配基于区域的时区。
     * 偏移量ID，如 '+02:30'，可以在解析的开始处，或者由 'UT', 'UTC' 或 'GMT' 前缀。偏移量ID的解析等效于使用
     * {@link #appendOffset(String, String)}，参数为 'HH:MM:ss' 和无偏移字符串 '0'。
     * 如果解析以 'UT', 'UTC' 或 'GMT' 开头，并且解析器无法匹配后续的偏移量ID，则选择 {@link ZoneOffset#UTC}。
     * 在所有其他情况下，使用已知的基于区域的时区列表来查找最长的可用匹配。如果未找到匹配，并且解析以 'Z' 开头，则选择 {@code ZoneOffset.UTC}。
     * 解析器使用 {@linkplain #parseCaseInsensitive() 不区分大小写} 设置。
     * <p>
     * 例如，以下将解析：
     * <pre>
     *   "Europe/London"           -- ZoneId.of("Europe/London")
     *   "Z"                       -- ZoneOffset.UTC
     *   "UT"                      -- ZoneId.of("UT")
     *   "UTC"                     -- ZoneId.of("UTC")
     *   "GMT"                     -- ZoneId.of("GMT")
     *   "+01:30"                  -- ZoneOffset.of("+01:30")
     *   "UT+01:30"                -- ZoneOffset.of("+01:30")
     *   "UTC+01:30"               -- ZoneOffset.of("+01:30")
     *   "GMT+01:30"               -- ZoneOffset.of("+01:30")
     * </pre>
     * <p>
     * 注意，此方法与 {@code appendZoneId()} 相同，除了用于获取时区的机制。
     * 另外，解析接受偏移量，而格式化永远不会产生一个偏移量。
     *
     * @return this, 用于链式调用，不为空
     * @see #appendZoneId()
     */
    public DateTimeFormatterBuilder appendZoneRegionId() {
        appendInternal(new ZoneIdPrinterParser(QUERY_REGION_ONLY, "ZoneRegionId()"));
        return this;
    }

    /**
     * 将时区ID（如 'Europe/Paris' 或 '+02:00'）附加到格式化器，使用最佳可用的时区ID。
     * <p>
     * 这将附加一个指令，以格式化/解析最佳可用的时区或偏移量ID到构建器。
     * 时区ID以宽松的方式获取，首先尝试找到真正的时区ID，如 {@code ZonedDateTime} 上的时区ID，
     * 然后尝试找到偏移量，如 {@code OffsetDateTime} 上的偏移量。
     * <p>
     * 在格式化期间，时区通过等效于使用 {@link TemporalQueries#zone()} 查询时间对象的方式获取。
     * 它将使用 {@link ZoneId#getId()} 的结果打印。
     * 如果无法获取时区，则除非格式化器的该部分是可选的，否则将抛出异常。
     * <p>
     * 在解析期间，文本必须匹配已知的时区或偏移量。
     * 时区ID有两种类型，基于偏移量的，如 '+01:30' 和基于区域的，如 'Europe/London'。这些解析方式不同。
     * 如果解析以 '+', '-', 'UT', 'UTC' 或 'GMT' 开头，那么解析器期望一个基于偏移量的时区，并且不会匹配基于区域的时区。
     * 偏移量ID，如 '+02:30'，可以在解析的开始处，或者由 'UT', 'UTC' 或 'GMT' 前缀。偏移量ID的解析等效于使用
     * {@link #appendOffset(String, String)}，参数为 'HH:MM:ss' 和无偏移字符串 '0'。
     * 如果解析以 'UT', 'UTC' 或 'GMT' 开头，并且解析器无法匹配后续的偏移量ID，则选择 {@link ZoneOffset#UTC}。
     * 在所有其他情况下，使用已知的基于区域的时区列表来查找最长的可用匹配。如果未找到匹配，并且解析以 'Z' 开头，则选择 {@code ZoneOffset.UTC}。
     * 解析器使用 {@linkplain #parseCaseInsensitive() 不区分大小写} 设置。
     * <p>
     * 例如，以下将解析：
     * <pre>
     *   "Europe/London"           -- ZoneId.of("Europe/London")
     *   "Z"                       -- ZoneOffset.UTC
     *   "UT"                      -- ZoneId.of("UT")
     *   "UTC"                     -- ZoneId.of("UTC")
     *   "GMT"                     -- ZoneId.of("GMT")
     *   "+01:30"                  -- ZoneOffset.of("+01:30")
     *   "UT+01:30"                -- ZoneOffset.of("UT+01:30")
     *   "UTC+01:30"               -- ZoneOffset.of("UTC+01:30")
     *   "GMT+01:30"               -- ZoneOffset.of("GMT+01:30")
     * </pre>
     * <p>
     * 注意，此方法与 {@code appendZoneId()} 相同，除了用于获取时区的机制。
     *
     * @return this, 用于链式调用，不为空
     * @see #appendZoneId()
     */
    public DateTimeFormatterBuilder appendZoneOrOffsetId() {
        appendInternal(new ZoneIdPrinterParser(TemporalQueries.zone(), "ZoneOrOffsetId()"));
        return this;
    }

    /**
     * 将时区名称（如 'British Summer Time'）附加到格式化器。
     * <p>
     * 这将附加一个指令，以格式化/解析时区的文本名称到构建器。
     * <p>
     * 在格式化期间，时区通过等效于使用 {@link TemporalQueries#zoneId()} 查询时间对象的方式获取。
     * 如果时区是 {@code ZoneOffset}，它将使用 {@link ZoneOffset#getId()} 的结果打印。
     * 如果时区不是偏移量，将根据 {@link DateTimeFormatter} 中设置的区域查找文本名称。
     * 如果时间对象表示一个瞬间，那么文本将是适当的夏令时或冬令时文本。
     * 如果查找文本未找到任何合适的结果，则将打印 {@link ZoneId#getId() ID}。
     * 如果无法获取时区，则除非格式化器的该部分是可选的，否则将抛出异常。
     * <p>
     * 在解析期间，接受文本时区名称、时区ID或偏移量。许多文本时区名称不是唯一的，例如 CST 可以表示
     * "Central Standard Time" 和 "China Standard Time"。在这种情况下，时区ID将由格式化器的
     * {@link DateTimeFormatter#getLocale() 区域} 信息和该区域的标准时区ID确定，例如，America/New_York 用于美国东部时区。
     * 可以使用 {@link #appendZoneText(TextStyle, Set)} 指定一组首选的 {@link ZoneId}。
     *
     * @param textStyle  要使用的文本样式，不为空
     * @return this, 用于链式调用，不为空
     */
    public DateTimeFormatterBuilder appendZoneText(TextStyle textStyle) {
        appendInternal(new ZoneTextPrinterParser(textStyle, null));
        return this;
    }

    /**
     * 将时区名称（如 'British Summer Time'）附加到格式化器。
     * <p>
     * 这将附加一个指令，以格式化/解析时区的文本名称到构建器。
     * <p>
     * 在格式化期间，时区通过等效于使用 {@link TemporalQueries#zoneId()} 查询时间对象的方式获取。
     * 如果时区是 {@code ZoneOffset}，它将使用 {@link ZoneOffset#getId()} 的结果打印。
     * 如果时区不是偏移量，将根据 {@link DateTimeFormatter} 中设置的区域查找文本名称。
     * 如果时间对象表示一个瞬间，那么文本将是适当的夏令时或冬令时文本。
     * 如果查找文本未找到任何合适的结果，则将打印 {@link ZoneId#getId() ID}。
     * 如果无法获取时区，则除非格式化器的该部分是可选的，否则将抛出异常。
     * <p>
     * 在解析期间，接受文本时区名称、时区ID或偏移量。许多文本时区名称不是唯一的，例如 CST 可以表示
     * "Central Standard Time" 和 "China Standard Time"。在这种情况下，时区ID将由格式化器的
     * {@link DateTimeFormatter#getLocale() 区域} 信息和该区域的标准时区ID确定，例如，America/New_York 用于美国东部时区。
     * 此方法还允许为解析指定一组首选的 {@link ZoneId}。如果解析的文本时区名称不唯一，则使用匹配的首选时区ID。
     * <p>
     * 如果无法解析时区，则除非格式化器的该部分是可选的，否则将抛出异常。
     *
     * @param textStyle  要使用的文本样式，不为空
     * @param preferredZones  一组首选的时区ID，不为空
     * @return this, 用于链式调用，不为空
     */
    public DateTimeFormatterBuilder appendZoneText(TextStyle textStyle,
                                                   Set<ZoneId> preferredZones) {
        Objects.requireNonNull(preferredZones, "preferredZones");
        appendInternal(new ZoneTextPrinterParser(textStyle, preferredZones));
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 将历法ID（如 'ISO' 或 'ThaiBuddhist'）附加到格式化器。
     * <p>
     * 这将附加一个指令，以格式化/解析历法ID到构建器。
     * <p>
     * 在格式化期间，历法通过等效于使用 {@link TemporalQueries#chronology()} 查询时间对象的方式获取。
     * 它将使用 {@link Chronology#getId()} 的结果打印。
     * 如果无法获取历法，则除非格式化器的该部分是可选的，否则将抛出异常。
     * <p>
     * 在解析期间，历法必须匹配 {@link Chronology#getAvailableChronologies()} 中的一个历法。
     * 如果无法解析历法，则除非格式化器的该部分是可选的，否则将抛出异常。
     * 解析器使用 {@linkplain #parseCaseInsensitive() 不区分大小写} 设置。
     *
     * @return this, 用于链式调用，不为空
     */
    public DateTimeFormatterBuilder appendChronologyId() {
        appendInternal(new ChronoPrinterParser(null));
        return this;
    }

    /**
     * 将历法名称附加到格式化器。
     * <p>
     * 在格式化期间，将输出日历系统名称。
     * 如果无法获取历法，则将抛出异常。
     *
     * @param textStyle  要使用的文本样式，不为空
     * @return this, 用于链式调用，不为空
     */
    public DateTimeFormatterBuilder appendChronologyText(TextStyle textStyle) {
        Objects.requireNonNull(textStyle, "textStyle");
        appendInternal(new ChronoPrinterParser(textStyle));
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 将本地化的日期时间模式附加到格式化器。
     * <p>
     * 这将附加一个本地化的部分到构建器，适用于输出日期、时间或日期时间组合。本地化部分的格式基于四个项目：
     * <ul>
     * <li>指定给此方法的 {@code dateStyle}
     * <li>指定给此方法的 {@code timeStyle}
     * <li>{@code DateTimeFormatter} 的 {@code Locale}
     * <li>{@code Chronology}，选择最佳可用的
     * </ul>
     * 在格式化期间，从正在格式化的时间对象中获取历法，该时间对象可能已被
     * {@link DateTimeFormatter#withChronology(Chronology)} 覆盖。
     * <p>
     * 在解析期间，如果已经解析了一个历法，则使用它。否则使用 {@code DateTimeFormatter.withChronology(Chronology)}
     * 的默认值，以 {@code IsoChronology} 作为回退。
     * <p>
     * 注意，此方法提供了与 {@code DateFormat} 上的方法（如 {@link java.text.DateFormat#getDateTimeInstance(int, int)}）类似的功能。
     *
     * @param dateStyle  要使用的日期样式，null 表示不需要日期
     * @param timeStyle  要使用的时间样式，null 表示不需要时间
     * @return this, 用于链式调用，不为空
     * @throws IllegalArgumentException 如果日期和时间样式都是 null
     */
    public DateTimeFormatterBuilder appendLocalized(FormatStyle dateStyle, FormatStyle timeStyle) {
        if (dateStyle == null && timeStyle == null) {
            throw new IllegalArgumentException("日期或时间样式必须非 null");
        }
        appendInternal(new LocalizedPrinterParser(dateStyle, timeStyle));
        return this;
    }


                //-----------------------------------------------------------------------
    /**
     * 将字符字面量附加到格式化器。
     * <p>
     * 此字符将在格式化期间输出。
     *
     * @param literal  要附加的字面量，不能为空
     * @return this，用于链式调用，不能为空
     */
    public DateTimeFormatterBuilder appendLiteral(char literal) {
        appendInternal(new CharLiteralPrinterParser(literal));
        return this;
    }

    /**
     * 将字符串字面量附加到格式化器。
     * <p>
     * 此字符串将在格式化期间输出。
     * <p>
     * 如果字面量为空，则不会添加到格式化器中。
     *
     * @param literal  要附加的字面量，不能为空
     * @return this，用于链式调用，不能为空
     */
    public DateTimeFormatterBuilder appendLiteral(String literal) {
        Objects.requireNonNull(literal, "literal");
        if (literal.length() > 0) {
            if (literal.length() == 1) {
                appendInternal(new CharLiteralPrinterParser(literal.charAt(0)));
            } else {
                appendInternal(new StringLiteralPrinterParser(literal));
            }
        }
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 将格式化器的所有元素附加到构建器。
     * <p>
     * 此方法与将格式化器的每个组成部分直接附加到此构建器的效果相同。
     *
     * @param formatter  要添加的格式化器，不能为空
     * @return this，用于链式调用，不能为空
     */
    public DateTimeFormatterBuilder append(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        appendInternal(formatter.toPrinterParser(false));
        return this;
    }

    /**
     * 将一个可选格式化/解析的格式化器附加到构建器。
     * <p>
     * 此方法与将格式化器的每个组成部分直接附加到此构建器，并用 {@link #optionalStart()} 和
     * {@link #optionalEnd()} 包围的效果相同。
     * <p>
     * 如果格式化器中的所有字段都有可用数据，则格式化器将进行格式化。
     * 如果字符串匹配，则格式化器将进行解析，否则不会返回错误。
     *
     * @param formatter  要添加的格式化器，不能为空
     * @return this，用于链式调用，不能为空
     */
    public DateTimeFormatterBuilder appendOptional(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        appendInternal(formatter.toPrinterParser(true));
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 将指定模式定义的元素附加到构建器。
     * <p>
     * 所有字母 'A' 到 'Z' 和 'a' 到 'z' 都被保留为模式字母。
     * 字符 '#'、'{' 和 '}' 保留供将来使用。
     * 字符 '[' 和 ']' 表示可选模式。
     * 以下定义了模式字母：
     * <pre>
     *  符号  意义                     表示方式      示例
     *  ------  -------                     ------------      -------
     *   G       时代                         文本              AD; Anno Domini; A
     *   u       年份                        年份              2004; 04
     *   y       时代年份                 年份              2004; 04
     *   D       年份中的天数                 数字            189
     *   M/L     月份中的天数               数字/文本       7; 07; Jul; July; J
     *   d       月份中的天数                数字            10
     *
     *   Q/q     年份中的季度             数字/文本       3; 03; Q3; 3rd quarter
     *   Y       周年份                    年份              1996; 96
     *   w       周年份中的周数     数字            27
     *   W       月份中的周数               数字            4
     *   E       星期几                     文本              Tue; Tuesday; T
     *   e/c     本地化星期几       数字/文本       2; 02; Tue; Tuesday; T
     *   F       月份中的周数               数字            3
     *
     *   a       上午/下午                     文本              PM
     *   h       上午/下午的小时 (1-12)  数字            12
     *   K       上午/下午的小时 (0-11)        数字            0
     *   k       上午/下午的小时 (1-24)  数字            0
     *
     *   H       一天中的小时 (0-23)          数字            0
     *   m       小时中的分钟              数字            30
     *   s       分钟中的秒数            数字            55
     *   S       秒的小数部分          小数            978
     *   A       一天中的毫秒数                数字            1234
     *   n       一秒中的纳秒数              数字            987654321
     *   N       一天中的纳秒数                数字            1234000000
     *
     *   V       时区ID                时区ID           America/Los_Angeles; Z; -08:30
     *   z       时区名称              时区名称         Pacific Standard Time; PST
     *   O       本地化时区偏移量       偏移量-O          GMT+8; GMT+08:00; UTC-08:00;
     *   X       时区偏移量 'Z' 表示零    偏移量-X          Z; -08; -0830; -08:30; -083015; -08:30:15;
     *   x       时区偏移量                 偏移量-x          +0000; -08; -0830; -08:30; -083015; -08:30:15;
     *   Z       时区偏移量                 偏移量-Z          +0000; -0800; -08:00;
     *
     *   p       填充下一个                    填充修饰符      1
     *
     *   '       文本的转义                     分隔符
     *   ''      单引号                字面量           '
     *   [       可选部分开始
     *   ]       可选部分结束
     *   #       保留供将来使用
     *   {       保留供将来使用
     *   }       保留供将来使用
     * </pre>
     * <p>
     * 模式字母的数量决定了格式。
     * 有关模式的用户导向描述，请参见 <a href="DateTimeFormatter.html#patterns">DateTimeFormatter</a>。
     * 以下表格定义了模式字母如何映射到构建器。
     * <p>
     * <b>日期字段</b>：输出日期的模式字母。
     * <pre>
     *  模式  数量  等效的构建器方法
     *  -------  -----  --------------------------
     *    G       1      appendText(ChronoField.ERA, TextStyle.SHORT)
     *    GG      2      appendText(ChronoField.ERA, TextStyle.SHORT)
     *    GGG     3      appendText(ChronoField.ERA, TextStyle.SHORT)
     *    GGGG    4      appendText(ChronoField.ERA, TextStyle.FULL)
     *    GGGGG   5      appendText(ChronoField.ERA, TextStyle.NARROW)
     *
     *    u       1      appendValue(ChronoField.YEAR, 1, 19, SignStyle.NORMAL);
     *    uu      2      appendValueReduced(ChronoField.YEAR, 2, 2000);
     *    uuu     3      appendValue(ChronoField.YEAR, 3, 19, SignStyle.NORMAL);
     *    u..u    4..n   appendValue(ChronoField.YEAR, n, 19, SignStyle.EXCEEDS_PAD);
     *    y       1      appendValue(ChronoField.YEAR_OF_ERA, 1, 19, SignStyle.NORMAL);
     *    yy      2      appendValueReduced(ChronoField.YEAR_OF_ERA, 2, 2000);
     *    yyy     3      appendValue(ChronoField.YEAR_OF_ERA, 3, 19, SignStyle.NORMAL);
     *    y..y    4..n   appendValue(ChronoField.YEAR_OF_ERA, n, 19, SignStyle.EXCEEDS_PAD);
     *    Y       1      附加特殊的本地化 WeekFields 元素，用于数字周基年
     *    YY      2      附加特殊的本地化 WeekFields 元素，用于减少的数字周基年 2 位；
     *    YYY     3      附加特殊的本地化 WeekFields 元素，用于数字周基年 (3, 19, SignStyle.NORMAL);
     *    Y..Y    4..n   附加特殊的本地化 WeekFields 元素，用于数字周基年 (n, 19, SignStyle.EXCEEDS_PAD);
     *
     *    Q       1      appendValue(IsoFields.QUARTER_OF_YEAR);
     *    QQ      2      appendValue(IsoFields.QUARTER_OF_YEAR, 2);
     *    QQQ     3      appendText(IsoFields.QUARTER_OF_YEAR, TextStyle.SHORT)
     *    QQQQ    4      appendText(IsoFields.QUARTER_OF_YEAR, TextStyle.FULL)
     *    QQQQQ   5      appendText(IsoFields.QUARTER_OF_YEAR, TextStyle.NARROW)
     *    q       1      appendValue(IsoFields.QUARTER_OF_YEAR);
     *    qq      2      appendValue(IsoFields.QUARTER_OF_YEAR, 2);
     *    qqq     3      appendText(IsoFields.QUARTER_OF_YEAR, TextStyle.SHORT_STANDALONE)
     *    qqqq    4      appendText(IsoFields.QUARTER_OF_YEAR, TextStyle.FULL_STANDALONE)
     *    qqqqq   5      appendText(IsoFields.QUARTER_OF_YEAR, TextStyle.NARROW_STANDALONE)
     *
     *    M       1      appendValue(ChronoField.MONTH_OF_YEAR);
     *    MM      2      appendValue(ChronoField.MONTH_OF_YEAR, 2);
     *    MMM     3      appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
     *    MMMM    4      appendText(ChronoField.MONTH_OF_YEAR, TextStyle.FULL)
     *    MMMMM   5      appendText(ChronoField.MONTH_OF_YEAR, TextStyle.NARROW)
     *    L       1      appendValue(ChronoField.MONTH_OF_YEAR);
     *    LL      2      appendValue(ChronoField.MONTH_OF_YEAR, 2);
     *    LLL     3      appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT_STANDALONE)
     *    LLLL    4      appendText(ChronoField.MONTH_OF_YEAR, TextStyle.FULL_STANDALONE)
     *    LLLLL   5      appendText(ChronoField.MONTH_OF_YEAR, TextStyle.NARROW_STANDALONE)
     *
     *    w       1      附加特殊的本地化 WeekFields 元素，用于数字年中的周数
     *    ww      2      附加特殊的本地化 WeekFields 元素，用于数字年中的周数，零填充
     *    W       1      附加特殊的本地化 WeekFields 元素，用于数字月中的周数
     *    d       1      appendValue(ChronoField.DAY_OF_MONTH)
     *    dd      2      appendValue(ChronoField.DAY_OF_MONTH, 2)
     *    D       1      appendValue(ChronoField.DAY_OF_YEAR)
     *    DD      2      appendValue(ChronoField.DAY_OF_YEAR, 2)
     *    DDD     3      appendValue(ChronoField.DAY_OF_YEAR, 3)
     *    F       1      appendValue(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH)
     *    E       1      appendText(ChronoField.DAY_OF_WEEK, TextStyle.SHORT)
     *    EE      2      appendText(ChronoField.DAY_OF_WEEK, TextStyle.SHORT)
     *    EEE     3      appendText(ChronoField.DAY_OF_WEEK, TextStyle.SHORT)
     *    EEEE    4      appendText(ChronoField.DAY_OF_WEEK, TextStyle.FULL)
     *    EEEEE   5      appendText(ChronoField.DAY_OF_WEEK, TextStyle.NARROW)
     *    e       1      附加特殊的本地化 WeekFields 元素，用于数字星期几
     *    ee      2      附加特殊的本地化 WeekFields 元素，用于数字星期几，零填充
     *    eee     3      appendText(ChronoField.DAY_OF_WEEK, TextStyle.SHORT)
     *    eeee    4      appendText(ChronoField.DAY_OF_WEEK, TextStyle.FULL)
     *    eeeee   5      appendText(ChronoField.DAY_OF_WEEK, TextStyle.NARROW)
     *    c       1      附加特殊的本地化 WeekFields 元素，用于数字星期几
     *    ccc     3      appendText(ChronoField.DAY_OF_WEEK, TextStyle.SHORT_STANDALONE)
     *    cccc    4      appendText(ChronoField.DAY_OF_WEEK, TextStyle.FULL_STANDALONE)
     *    ccccc   5      appendText(ChronoField.DAY_OF_WEEK, TextStyle.NARROW_STANDALONE)
     * </pre>
     * <p>
     * <b>时间字段</b>：输出时间的模式字母。
     * <pre>
     *  模式  数量  等效的构建器方法
     *  -------  -----  --------------------------
     *    a       1      appendText(ChronoField.AMPM_OF_DAY, TextStyle.SHORT)
     *    h       1      appendValue(ChronoField.CLOCK_HOUR_OF_AMPM)
     *    hh      2      appendValue(ChronoField.CLOCK_HOUR_OF_AMPM, 2)
     *    H       1      appendValue(ChronoField.HOUR_OF_DAY)
     *    HH      2      appendValue(ChronoField.HOUR_OF_DAY, 2)
     *    k       1      appendValue(ChronoField.CLOCK_HOUR_OF_DAY)
     *    kk      2      appendValue(ChronoField.CLOCK_HOUR_OF_DAY, 2)
     *    K       1      appendValue(ChronoField.HOUR_OF_AMPM)
     *    KK      2      appendValue(ChronoField.HOUR_OF_AMPM, 2)
     *    m       1      appendValue(ChronoField.MINUTE_OF_HOUR)
     *    mm      2      appendValue(ChronoField.MINUTE_OF_HOUR, 2)
     *    s       1      appendValue(ChronoField.SECOND_OF_MINUTE)
     *    ss      2      appendValue(ChronoField.SECOND_OF_MINUTE, 2)
     *
     *    S..S    1..n   appendFraction(ChronoField.NANO_OF_SECOND, n, n, false)
     *    A       1      appendValue(ChronoField.MILLI_OF_DAY)
     *    A..A    2..n   appendValue(ChronoField.MILLI_OF_DAY, n)
     *    n       1      appendValue(ChronoField.NANO_OF_SECOND)
     *    n..n    2..n   appendValue(ChronoField.NANO_OF_SECOND, n)
     *    N       1      appendValue(ChronoField.NANO_OF_DAY)
     *    N..N    2..n   appendValue(ChronoField.NANO_OF_DAY, n)
     * </pre>
     * <p>
     * <b>时区ID</b>：输出 {@code ZoneId} 的模式字母。
     * <pre>
     *  模式  数量  等效的构建器方法
     *  -------  -----  --------------------------
     *    VV      2      appendZoneId()
     *    z       1      appendZoneText(TextStyle.SHORT)
     *    zz      2      appendZoneText(TextStyle.SHORT)
     *    zzz     3      appendZoneText(TextStyle.SHORT)
     *    zzzz    4      appendZoneText(TextStyle.FULL)
     * </pre>
     * <p>
     * <b>时区偏移量</b>：输出 {@code ZoneOffset} 的模式字母。
     * <pre>
     *  模式  数量  等效的构建器方法
     *  -------  -----  --------------------------
     *    O       1      appendLocalizedOffsetPrefixed(TextStyle.SHORT);
     *    OOOO    4      appendLocalizedOffsetPrefixed(TextStyle.FULL);
     *    X       1      appendOffset("+HHmm","Z")
     *    XX      2      appendOffset("+HHMM","Z")
     *    XXX     3      appendOffset("+HH:MM","Z")
     *    XXXX    4      appendOffset("+HHMMss","Z")
     *    XXXXX   5      appendOffset("+HH:MM:ss","Z")
     *    x       1      appendOffset("+HHmm","+00")
     *    xx      2      appendOffset("+HHMM","+0000")
     *    xxx     3      appendOffset("+HH:MM","+00:00")
     *    xxxx    4      appendOffset("+HHMMss","+0000")
     *    xxxxx   5      appendOffset("+HH:MM:ss","+00:00")
     *    Z       1      appendOffset("+HHMM","+0000")
     *    ZZ      2      appendOffset("+HHMM","+0000")
     *    ZZZ     3      appendOffset("+HHMM","+0000")
     *    ZZZZ    4      appendLocalizedOffset(TextStyle.FULL);
     *    ZZZZZ   5      appendOffset("+HH:MM:ss","Z")
     * </pre>
     * <p>
     * <b>修饰符</b>：修改其余模式的模式字母：
     * <pre>
     *  模式  数量  等效的构建器方法
     *  -------  -----  --------------------------
     *    [       1      optionalStart()
     *    ]       1      optionalEnd()
     *    p..p    1..n   padNext(n)
     * </pre>
     * <p>
     * 任何未在上述中指定的字母序列、未识别的字母或保留字符都将抛出异常。
     * 未来版本可能会增加模式集。
     * 建议使用单引号围绕所有要直接输出的字符，以确保未来的更改不会破坏您的应用程序。
     * <p>
     * 请注意，模式字符串与 {@link java.text.SimpleDateFormat SimpleDateFormat} 类似，但不完全相同。
     * 模式字符串也与 Unicode Common Locale Data Repository (CLDR/LDML) 定义的模式类似，但不完全相同。
     * 模式字母 'X' 和 'u' 与 Unicode CLDR/LDML 对齐。
     * 相比之下，{@code SimpleDateFormat} 使用 'u' 表示数字星期几。
     * 模式字母 'y' 和 'Y' 在解析两位数和四位数以上的年份时有所不同。
     * 模式字母 'n'、'A'、'N' 和 'p' 被添加。
     * 数字类型将拒绝大数字。
     *
     * @param pattern  要添加的模式，不能为空
     * @return this，用于链式调用，不能为空
     * @throws IllegalArgumentException 如果模式无效
     */
    public DateTimeFormatterBuilder appendPattern(String pattern) {
        Objects.requireNonNull(pattern, "pattern");
        parsePattern(pattern);
        return this;
    }


                private void parsePattern(String pattern) {
        for (int pos = 0; pos < pattern.length(); pos++) {
            char cur = pattern.charAt(pos);
            if ((cur >= 'A' && cur <= 'Z') || (cur >= 'a' && cur <= 'z')) {
                int start = pos++;
                for ( ; pos < pattern.length() && pattern.charAt(pos) == cur; pos++);  // 短循环
                int count = pos - start;
                // 填充
                if (cur == 'p') {
                    int pad = 0;
                    if (pos < pattern.length()) {
                        cur = pattern.charAt(pos);
                        if ((cur >= 'A' && cur <= 'Z') || (cur >= 'a' && cur <= 'z')) {
                            pad = count;
                            start = pos++;
                            for ( ; pos < pattern.length() && pattern.charAt(pos) == cur; pos++);  // 短循环
                            count = pos - start;
                        }
                    }
                    if (pad == 0) {
                        throw new IllegalArgumentException(
                                "填充字母 'p' 必须后跟有效的填充模式: " + pattern);
                    }
                    padNext(pad); // 填充并继续解析
                }
                // 主要规则
                TemporalField field = FIELD_MAP.get(cur);
                if (field != null) {
                    parseField(cur, count, field);
                } else if (cur == 'z') {
                    if (count > 4) {
                        throw new IllegalArgumentException("模式字母过多: " + cur);
                    } else if (count == 4) {
                        appendZoneText(TextStyle.FULL);
                    } else {
                        appendZoneText(TextStyle.SHORT);
                    }
                } else if (cur == 'V') {
                    if (count != 2) {
                        throw new IllegalArgumentException("模式字母计数必须为 2: " + cur);
                    }
                    appendZoneId();
                } else if (cur == 'Z') {
                    if (count < 4) {
                        appendOffset("+HHMM", "+0000");
                    } else if (count == 4) {
                        appendLocalizedOffset(TextStyle.FULL);
                    } else if (count == 5) {
                        appendOffset("+HH:MM:ss","Z");
                    } else {
                        throw new IllegalArgumentException("模式字母过多: " + cur);
                    }
                } else if (cur == 'O') {
                    if (count == 1) {
                        appendLocalizedOffset(TextStyle.SHORT);
                    } else if (count == 4) {
                        appendLocalizedOffset(TextStyle.FULL);
                    } else {
                        throw new IllegalArgumentException("模式字母计数必须为 1 或 4: " + cur);
                    }
                } else if (cur == 'X') {
                    if (count > 5) {
                        throw new IllegalArgumentException("模式字母过多: " + cur);
                    }
                    appendOffset(OffsetIdPrinterParser.PATTERNS[count + (count == 1 ? 0 : 1)], "Z");
                } else if (cur == 'x') {
                    if (count > 5) {
                        throw new IllegalArgumentException("模式字母过多: " + cur);
                    }
                    String zero = (count == 1 ? "+00" : (count % 2 == 0 ? "+0000" : "+00:00"));
                    appendOffset(OffsetIdPrinterParser.PATTERNS[count + (count == 1 ? 0 : 1)], zero);
                } else if (cur == 'W') {
                    // 由 Locale 定义的字段
                    if (count > 1) {
                        throw new IllegalArgumentException("模式字母过多: " + cur);
                    }
                    appendInternal(new WeekBasedFieldPrinterParser(cur, count));
                } else if (cur == 'w') {
                    // 由 Locale 定义的字段
                    if (count > 2) {
                        throw new IllegalArgumentException("模式字母过多: " + cur);
                    }
                    appendInternal(new WeekBasedFieldPrinterParser(cur, count));
                } else if (cur == 'Y') {
                    // 由 Locale 定义的字段
                    appendInternal(new WeekBasedFieldPrinterParser(cur, count));
                } else {
                    throw new IllegalArgumentException("未知的模式字母: " + cur);
                }
                pos--;

            } else if (cur == '\'') {
                // 解析字面量
                int start = pos++;
                for ( ; pos < pattern.length(); pos++) {
                    if (pattern.charAt(pos) == '\'') {
                        if (pos + 1 < pattern.length() && pattern.charAt(pos + 1) == '\'') {
                            pos++;
                        } else {
                            break;  // 字面量结束
                        }
                    }
                }
                if (pos >= pattern.length()) {
                    throw new IllegalArgumentException("模式以不完整的字符串字面量结束: " + pattern);
                }
                String str = pattern.substring(start + 1, pos);
                if (str.length() == 0) {
                    appendLiteral('\'');
                } else {
                    appendLiteral(str.replace("''", "'"));
                }

            } else if (cur == '[') {
                optionalStart();

            } else if (cur == ']') {
                if (active.parent == null) {
                    throw new IllegalArgumentException("模式无效，因为它包含没有前一个 [ 的 ]");
                }
                optionalEnd();

            } else if (cur == '{' || cur == '}' || cur == '#') {
                throw new IllegalArgumentException("模式包含保留字符: '" + cur + "'");
            } else {
                appendLiteral(cur);
            }
        }
    }

    @SuppressWarnings("fallthrough")
    private void parseField(char cur, int count, TemporalField field) {
        boolean standalone = false;
        switch (cur) {
            case 'u':
            case 'y':
                if (count == 2) {
                    appendValueReduced(field, 2, 2, ReducedPrinterParser.BASE_DATE);
                } else if (count < 4) {
                    appendValue(field, count, 19, SignStyle.NORMAL);
                } else {
                    appendValue(field, count, 19, SignStyle.EXCEEDS_PAD);
                }
                break;
            case 'c':
                if (count == 2) {
                    throw new IllegalArgumentException("无效的模式 \"cc\"");
                }
                /*fallthrough*/
            case 'L':
            case 'q':
                standalone = true;
                /*fallthrough*/
            case 'M':
            case 'Q':
            case 'E':
            case 'e':
                switch (count) {
                    case 1:
                    case 2:
                        if (cur == 'c' || cur == 'e') {
                            appendInternal(new WeekBasedFieldPrinterParser(cur, count));
                        } else if (cur == 'E') {
                            appendText(field, TextStyle.SHORT);
                        } else {
                            if (count == 1) {
                                appendValue(field);
                            } else {
                                appendValue(field, 2);
                            }
                        }
                        break;
                    case 3:
                        appendText(field, standalone ? TextStyle.SHORT_STANDALONE : TextStyle.SHORT);
                        break;
                    case 4:
                        appendText(field, standalone ? TextStyle.FULL_STANDALONE : TextStyle.FULL);
                        break;
                    case 5:
                        appendText(field, standalone ? TextStyle.NARROW_STANDALONE : TextStyle.NARROW);
                        break;
                    default:
                        throw new IllegalArgumentException("模式字母过多: " + cur);
                }
                break;
            case 'a':
                if (count == 1) {
                    appendText(field, TextStyle.SHORT);
                } else {
                    throw new IllegalArgumentException("模式字母过多: " + cur);
                }
                break;
            case 'G':
                switch (count) {
                    case 1:
                    case 2:
                    case 3:
                        appendText(field, TextStyle.SHORT);
                        break;
                    case 4:
                        appendText(field, TextStyle.FULL);
                        break;
                    case 5:
                        appendText(field, TextStyle.NARROW);
                        break;
                    default:
                        throw new IllegalArgumentException("模式字母过多: " + cur);
                }
                break;
            case 'S':
                appendFraction(NANO_OF_SECOND, count, count, false);
                break;
            case 'F':
                if (count == 1) {
                    appendValue(field);
                } else {
                    throw new IllegalArgumentException("模式字母过多: " + cur);
                }
                break;
            case 'd':
            case 'h':
            case 'H':
            case 'k':
            case 'K':
            case 'm':
            case 's':
                if (count == 1) {
                    appendValue(field);
                } else if (count == 2) {
                    appendValue(field, count);
                } else {
                    throw new IllegalArgumentException("模式字母过多: " + cur);
                }
                break;
            case 'D':
                if (count == 1) {
                    appendValue(field);
                } else if (count <= 3) {
                    appendValue(field, count);
                } else {
                    throw new IllegalArgumentException("模式字母过多: " + cur);
                }
                break;
            default:
                if (count == 1) {
                    appendValue(field);
                } else {
                    appendValue(field, count);
                }
                break;
        }
    }

    /** 字母到字段的映射。 */
    private static final Map<Character, TemporalField> FIELD_MAP = new HashMap<>();
    static {
        // SDF = SimpleDateFormat
        FIELD_MAP.put('G', ChronoField.ERA);                       // SDF, LDML (与两者在 1/2 个字符时不同)
        FIELD_MAP.put('y', ChronoField.YEAR_OF_ERA);               // SDF, LDML
        FIELD_MAP.put('u', ChronoField.YEAR);                      // LDML (与 SDF 不同)
        FIELD_MAP.put('Q', IsoFields.QUARTER_OF_YEAR);             // LDML (从 310 中移除季度)
        FIELD_MAP.put('q', IsoFields.QUARTER_OF_YEAR);             // LDML (独立)
        FIELD_MAP.put('M', ChronoField.MONTH_OF_YEAR);             // SDF, LDML
        FIELD_MAP.put('L', ChronoField.MONTH_OF_YEAR);             // SDF, LDML (独立)
        FIELD_MAP.put('D', ChronoField.DAY_OF_YEAR);               // SDF, LDML
        FIELD_MAP.put('d', ChronoField.DAY_OF_MONTH);              // SDF, LDML
        FIELD_MAP.put('F', ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH);  // SDF, LDML
        FIELD_MAP.put('E', ChronoField.DAY_OF_WEEK);               // SDF, LDML (与两者在 1/2 个字符时不同)
        FIELD_MAP.put('c', ChronoField.DAY_OF_WEEK);               // LDML (独立)
        FIELD_MAP.put('e', ChronoField.DAY_OF_WEEK);               // LDML (需要本地化的周数)
        FIELD_MAP.put('a', ChronoField.AMPM_OF_DAY);               // SDF, LDML
        FIELD_MAP.put('H', ChronoField.HOUR_OF_DAY);               // SDF, LDML
        FIELD_MAP.put('k', ChronoField.CLOCK_HOUR_OF_DAY);         // SDF, LDML
        FIELD_MAP.put('K', ChronoField.HOUR_OF_AMPM);              // SDF, LDML
        FIELD_MAP.put('h', ChronoField.CLOCK_HOUR_OF_AMPM);        // SDF, LDML
        FIELD_MAP.put('m', ChronoField.MINUTE_OF_HOUR);            // SDF, LDML
        FIELD_MAP.put('s', ChronoField.SECOND_OF_MINUTE);          // SDF, LDML
        FIELD_MAP.put('S', ChronoField.NANO_OF_SECOND);            // LDML (SDF 使用毫秒数)
        FIELD_MAP.put('A', ChronoField.MILLI_OF_DAY);              // LDML
        FIELD_MAP.put('n', ChronoField.NANO_OF_SECOND);            // 310 (提议用于 LDML)
        FIELD_MAP.put('N', ChronoField.NANO_OF_DAY);               // 310 (提议用于 LDML)
        // 310 - z - 时区名称，匹配 LDML 和 SimpleDateFormat 1 到 4
        // 310 - Z - 匹配 SimpleDateFormat 和 LDML
        // 310 - V - 时区 id，匹配 LDML
        // 310 - p - 填充前缀
        // 310 - X - 匹配 LDML，几乎匹配 SDF 1，精确匹配 2&3，扩展 4&5
        // 310 - x - 匹配 LDML
        // 310 - w, W, 和 Y 是匹配 LDML 的本地化形式
        // LDML - U - 周期年名称，310 尚不支持
        // LDML - l - 已弃用
        // LDML - j - 不相关
        // LDML - g - 修改的儒略日
        // LDML - v,V - 扩展的时区名称
    }

    //-----------------------------------------------------------------------
    /**
     * 导致下一个添加的打印机/解析器使用空格填充到固定宽度。
     * <p>
     * 此填充将使用空格填充到固定宽度。
     * <p>
     * 在格式化期间，装饰元素将被输出并填充到指定的宽度。如果填充宽度被超过，格式化期间将抛出异常。
     * <p>
     * 在解析期间，填充和装饰元素将被解析。
     * 如果解析是宽松的，那么填充宽度将被视为最大值。
     * 填充将贪婪地解析。因此，如果装饰元素以填充字符开始，它将不会被解析。
     *
     * @param padWidth  填充宽度，1 或更大
     * @return this，用于链式调用，不为空
     * @throws IllegalArgumentException 如果填充宽度太小
     */
    public DateTimeFormatterBuilder padNext(int padWidth) {
        return padNext(padWidth, ' ');
    }

    /**
     * 导致下一个添加的打印机/解析器填充到固定宽度。
     * <p>
     * 此填充旨在用于非零填充。
     * 零填充应使用 appendValue 方法实现。
     * <p>
     * 在格式化期间，装饰元素将被输出并填充到指定的宽度。如果填充宽度被超过，格式化期间将抛出异常。
     * <p>
     * 在解析期间，填充和装饰元素将被解析。
     * 如果解析是宽松的，那么填充宽度将被视为最大值。
     * 如果解析是不区分大小写的，那么填充字符将忽略大小写进行匹配。
     * 填充将贪婪地解析。因此，如果装饰元素以填充字符开始，它将不会被解析。
     *
     * @param padWidth  填充宽度，1 或更大
     * @param padChar  填充字符
     * @return this，用于链式调用，不为空
     * @throws IllegalArgumentException 如果填充宽度太小
     */
    public DateTimeFormatterBuilder padNext(int padWidth, char padChar) {
        if (padWidth < 1) {
            throw new IllegalArgumentException("填充宽度必须至少为一，但为 " + padWidth);
        }
        active.padNextWidth = padWidth;
        active.padNextChar = padChar;
        active.valueParserIndex = -1;
        return this;
    }


                //-----------------------------------------------------------------------
    /**
     * 标记可选部分的开始。
     * <p>
     * 格式化输出可以包含可选部分，这些部分可以嵌套。
     * 可选部分通过调用此方法开始，并通过调用 {@link #optionalEnd()} 或结束构建过程来结束。
     * <p>
     * 可选部分中的所有元素都被视为可选的。
     * 在格式化过程中，只有当 {@code TemporalAccessor} 中包含该部分中所有元素的数据时，该部分才会被输出。
     * 在解析过程中，整个部分可能从解析的字符串中缺失。
     * <p>
     * 例如，考虑一个设置为
     * {@code builder.appendValue(HOUR_OF_DAY,2).optionalStart().appendValue(MINUTE_OF_HOUR,2)} 的构建器。
     * 可选部分在构建器结束时自动结束。
     * 在格式化过程中，只有当可以从日期时间中获取分钟的值时，才会输出分钟。
     * 在解析过程中，无论分钟是否存在，输入都会被成功解析。
     *
     * @return this, 用于链式调用，不为空
     */
    public DateTimeFormatterBuilder optionalStart() {
        active.valueParserIndex = -1;
        active = new DateTimeFormatterBuilder(active, true);
        return this;
    }

    /**
     * 结束一个可选部分。
     * <p>
     * 格式化输出可以包含可选部分，这些部分可以嵌套。
     * 可选部分通过调用 {@link #optionalStart()} 开始，并通过调用此方法（或在构建器结束时）来结束。
     * <p>
     * 如果之前没有调用 {@code optionalStart}，调用此方法将抛出异常。
     * 在调用 {@code optionalStart} 后立即调用此方法，除了结束（空的）可选部分外，对格式化器没有其他影响。
     * <p>
     * 可选部分中的所有元素都被视为可选的。
     * 在格式化过程中，只有当 {@code TemporalAccessor} 中包含该部分中所有元素的数据时，该部分才会被输出。
     * 在解析过程中，整个部分可能从解析的字符串中缺失。
     * <p>
     * 例如，考虑一个设置为
     * {@code builder.appendValue(HOUR_OF_DAY,2).optionalStart().appendValue(MINUTE_OF_HOUR,2).optionalEnd()} 的构建器。
     * 在格式化过程中，只有当可以从日期时间中获取分钟的值时，才会输出分钟。
     * 在解析过程中，无论分钟是否存在，输入都会被成功解析。
     *
     * @return this, 用于链式调用，不为空
     * @throws IllegalStateException 如果之前没有调用 {@code optionalStart}
     */
    public DateTimeFormatterBuilder optionalEnd() {
        if (active.parent == null) {
            throw new IllegalStateException("Cannot call optionalEnd() as there was no previous call to optionalStart()");
        }
        if (active.printerParsers.size() > 0) {
            CompositePrinterParser cpp = new CompositePrinterParser(active.printerParsers, active.optional);
            active = active.parent;
            appendInternal(cpp);
        } else {
            active = active.parent;
        }
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 将打印机和/或解析器添加到处理填充的内部列表。
     *
     * @param pp  要添加的打印机-解析器，不为空
     * @return 活动解析器列表中的索引
     */
    private int appendInternal(DateTimePrinterParser pp) {
        Objects.requireNonNull(pp, "pp");
        if (active.padNextWidth > 0) {
            if (pp != null) {
                pp = new PadPrinterParserDecorator(pp, active.padNextWidth, active.padNextChar);
            }
            active.padNextWidth = 0;
            active.padNextChar = 0;
        }
        active.printerParsers.add(pp);
        active.valueParserIndex = -1;
        return active.printerParsers.size() - 1;
    }

    //-----------------------------------------------------------------------
    /**
     * 完成此构建器，通过使用默认区域设置创建 {@code DateTimeFormatter}。
     * <p>
     * 这将使用 {@linkplain Locale#getDefault(Locale.Category) 默认 FORMAT 区域设置} 创建格式化器。
     * 数字将使用标准 DecimalStyle 进行打印和解析。
     * 解析器样式将是 {@link ResolverStyle#SMART SMART}。
     * <p>
     * 调用此方法将通过重复调用 {@link #optionalEnd()} 来结束任何打开的可选部分，然后创建格式化器。
     * <p>
     * 如果需要，创建格式化器后仍可以使用此构建器，尽管调用 {@code optionalEnd} 可能会改变状态。
     *
     * @return 创建的格式化器，不为空
     */
    public DateTimeFormatter toFormatter() {
        return toFormatter(Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 完成此构建器，通过使用指定的区域设置创建 {@code DateTimeFormatter}。
     * <p>
     * 这将使用指定的区域设置创建格式化器。
     * 数字将使用标准 DecimalStyle 进行打印和解析。
     * 解析器样式将是 {@link ResolverStyle#SMART SMART}。
     * <p>
     * 调用此方法将通过重复调用 {@link #optionalEnd()} 来结束任何打开的可选部分，然后创建格式化器。
     * <p>
     * 如果需要，创建格式化器后仍可以使用此构建器，尽管调用 {@code optionalEnd} 可能会改变状态。
     *
     * @param locale  用于格式化的区域设置，不为空
     * @return 创建的格式化器，不为空
     */
    public DateTimeFormatter toFormatter(Locale locale) {
        return toFormatter(locale, ResolverStyle.SMART, null);
    }

    /**
     * 完成此构建器，通过创建格式化器。
     * 这使用默认区域设置。
     *
     * @param resolverStyle  要使用的解析器样式，不为空
     * @return 创建的格式化器，不为空
     */
    DateTimeFormatter toFormatter(ResolverStyle resolverStyle, Chronology chrono) {
        return toFormatter(Locale.getDefault(Locale.Category.FORMAT), resolverStyle, chrono);
    }

    /**
     * 完成此构建器，通过创建格式化器。
     *
     * @param locale  用于格式化的区域设置，不为空
     * @param chrono  要使用的历法，可能为空
     * @return 创建的格式化器，不为空
     */
    private DateTimeFormatter toFormatter(Locale locale, ResolverStyle resolverStyle, Chronology chrono) {
        Objects.requireNonNull(locale, "locale");
        while (active.parent != null) {
            optionalEnd();
        }
        CompositePrinterParser pp = new CompositePrinterParser(printerParsers, false);
        return new DateTimeFormatter(pp, locale, DecimalStyle.STANDARD,
                resolverStyle, null, chrono, null);
    }

    //-----------------------------------------------------------------------
    /**
     * 格式化/解析日期时间信息的策略。
     * <p>
     * 打印机可以格式化输入日期时间对象的任何部分或全部。
     * 通常，一个完整的格式是由多个较小的单元构建的，每个单元输出一个字段。
     * <p>
     * 解析器可以从输入文本中解析任何部分，将结果存储在上下文中。通常，每个单独的解析器只会解析一个字段，如月中的日期，将值存储在上下文中。
     * 一旦解析完成，调用者将解析的值解析以创建所需的对象，如 {@code LocalDate}。
     * <p>
     * 解析位置将在解析过程中更新。解析将从指定的索引开始，返回值指定下一个解析器的新解析位置。
     * 如果发生错误，返回的索引将为负数，并使用补码运算符编码错误位置。
     *
     * @implSpec
     * 必须谨慎实现此接口以确保其他类正确操作。
     * 所有可以实例化的实现必须是 final、不可变和线程安全的。
     * <p>
     * 上下文不是线程安全的对象，每次格式化时都会创建一个新的实例。
     * 上下文不得存储在实例变量中或与其他线程共享。
     */
    interface DateTimePrinterParser {

        /**
         * 将日期时间对象打印到缓冲区。
         * <p>
         * 上下文包含格式化期间使用的信息。
         * 它还包含要打印的日期时间信息。
         * <p>
         * 缓冲区不得超出实现控制的内容进行变异。
         *
         * @param context  用于格式化的上下文，不为空
         * @param buf  要追加到的缓冲区，不为空
         * @return 如果无法从日期时间查询值，则返回 false，否则返回 true
         * @throws DateTimeException 如果日期时间无法成功打印
         */
        boolean format(DateTimePrintContext context, StringBuilder buf);

        /**
         * 将文本解析为日期时间信息。
         * <p>
         * 上下文包含解析期间使用的信息。
         * 它还用于存储解析的日期时间信息。
         *
         * @param context  用于解析的上下文，不为空
         * @param text  要解析的输入文本，不为空
         * @param position  开始解析的位置，从 0 到文本长度
         * @return 新的解析位置，负数表示错误，错误位置使用补码 ~ 运算符编码
         * @throws NullPointerException 如果上下文或文本为空
         * @throws IndexOutOfBoundsException 如果位置无效
         */
        int parse(DateTimeParseContext context, CharSequence text, int position);
    }

    //-----------------------------------------------------------------------
    /**
     * 组合打印机和解析器。
     */
    static final class CompositePrinterParser implements DateTimePrinterParser {
        private final DateTimePrinterParser[] printerParsers;
        private final boolean optional;

        CompositePrinterParser(List<DateTimePrinterParser> printerParsers, boolean optional) {
            this(printerParsers.toArray(new DateTimePrinterParser[printerParsers.size()]), optional);
        }

        CompositePrinterParser(DateTimePrinterParser[] printerParsers, boolean optional) {
            this.printerParsers = printerParsers;
            this.optional = optional;
        }

        /**
         * 返回一个带有更改的可选标志的打印机-解析器副本。
         *
         * @param optional  要在副本中设置的可选标志
         * @return 新的打印机-解析器，不为空
         */
        public CompositePrinterParser withOptional(boolean optional) {
            if (optional == this.optional) {
                return this;
            }
            return new CompositePrinterParser(printerParsers, optional);
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            int length = buf.length();
            if (optional) {
                context.startOptional();
            }
            try {
                for (DateTimePrinterParser pp : printerParsers) {
                    if (pp.format(context, buf) == false) {
                        buf.setLength(length);  // 重置缓冲区
                        return true;
                    }
                }
            } finally {
                if (optional) {
                    context.endOptional();
                }
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            if (optional) {
                context.startOptional();
                int pos = position;
                for (DateTimePrinterParser pp : printerParsers) {
                    pos = pp.parse(context, text, pos);
                    if (pos < 0) {
                        context.endOptional(false);
                        return position;  // 返回原始位置
                    }
                }
                context.endOptional(true);
                return pos;
            } else {
                for (DateTimePrinterParser pp : printerParsers) {
                    position = pp.parse(context, text, position);
                    if (position < 0) {
                        break;
                    }
                }
                return position;
            }
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            if (printerParsers != null) {
                buf.append(optional ? "[" : "(");
                for (DateTimePrinterParser pp : printerParsers) {
                    buf.append(pp);
                }
                buf.append(optional ? "]" : ")");
            }
            return buf.toString();
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 将输出填充到固定宽度。
     */
    static final class PadPrinterParserDecorator implements DateTimePrinterParser {
        private final DateTimePrinterParser printerParser;
        private final int padWidth;
        private final char padChar;

        /**
         * 构造函数。
         *
         * @param printerParser  打印机，不为空
         * @param padWidth  填充到的宽度，1 或更大
         * @param padChar  填充字符
         */
        PadPrinterParserDecorator(DateTimePrinterParser printerParser, int padWidth, char padChar) {
            // 输入由 DateTimeFormatterBuilder 检查
            this.printerParser = printerParser;
            this.padWidth = padWidth;
            this.padChar = padChar;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            int preLen = buf.length();
            if (printerParser.format(context, buf) == false) {
                return false;
            }
            int len = buf.length() - preLen;
            if (len > padWidth) {
                throw new DateTimeException(
                    "无法打印，因为 " + len + " 个字符的输出超过了 " + padWidth + " 的填充宽度");
            }
            for (int i = 0; i < padWidth - len; i++) {
                buf.insert(preLen, padChar);
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            // 在装饰解析器更改上下文之前缓存上下文
            final boolean strict = context.isStrict();
            // 解析
            if (position > text.length()) {
                throw new IndexOutOfBoundsException();
            }
            if (position == text.length()) {
                return ~position;  // 字符串中没有更多字符
            }
            int endPos = position + padWidth;
            if (endPos > text.length()) {
                if (strict) {
                    return ~position;  // 字符串中的字符不足以满足解析宽度
                }
                endPos = text.length();
            }
            int pos = position;
            while (pos < endPos && context.charEquals(text.charAt(pos), padChar)) {
                pos++;
            }
            text = text.subSequence(0, endPos);
            int resultPos = printerParser.parse(context, text, pos);
            if (resultPos != endPos && strict) {
                return ~(position + pos);  // 装饰字段的解析没有解析到末尾
            }
            return resultPos;
        }


        }

    }
```

```java
    //-----------------------------------------------------------------------
    /**
     * 枚举以应用简单的解析设置。
     */
    static enum SettingsParser implements DateTimePrinterParser {
        SENSITIVE,
        INSENSITIVE,
        STRICT,
        LENIENT;

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            return true;  // 这里不需要做任何事情
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            // 使用序数以避免 javac 生成的内部类
            switch (ordinal()) {
                case 0: context.setCaseSensitive(true); break;
                case 1: context.setCaseSensitive(false); break;
                case 2: context.setStrict(true); break;
                case 3: context.setStrict(false); break;
            }
            return position;
        }

        @Override
        public String toString() {
            // 使用序数以避免 javac 生成的内部类
            switch (ordinal()) {
                case 0: return "ParseCaseSensitive(true)";
                case 1: return "ParseCaseSensitive(false)";
                case 2: return "ParseStrict(true)";
                case 3: return "ParseStrict(false)";
            }
            throw new IllegalStateException("无法到达");
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 如果当前不存在，则在解析中设置默认值。
     */
    static class DefaultValueParser implements DateTimePrinterParser {
        private final TemporalField field;
        private final long value;

        DefaultValueParser(TemporalField field, long value) {
            this.field = field;
            this.value = value;
        }

        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            return true;
        }

        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            if (context.getParsed(field) == null) {
                context.setParsedField(field, value, position, position);
            }
            return position;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 打印或解析字符字面量。
     */
    static final class CharLiteralPrinterParser implements DateTimePrinterParser {
        private final char literal;

        CharLiteralPrinterParser(char literal) {
            this.literal = literal;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            buf.append(literal);
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int length = text.length();
            if (position == length) {
                return ~position;
            }
            char ch = text.charAt(position);
            if (ch != literal) {
                if (context.isCaseSensitive() ||
                        (Character.toUpperCase(ch) != Character.toUpperCase(literal) &&
                         Character.toLowerCase(ch) != Character.toLowerCase(literal))) {
                    return ~position;
                }
            }
            return position + 1;
        }

        @Override
        public String toString() {
            if (literal == '\'') {
                return "''";
            }
            return "'" + literal + "'";
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 打印或解析字符串字面量。
     */
    static final class StringLiteralPrinterParser implements DateTimePrinterParser {
        private final String literal;

        StringLiteralPrinterParser(String literal) {
            this.literal = literal;  // 由调用者验证
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            buf.append(literal);
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int length = text.length();
            if (position > length || position < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (context.subSequenceEquals(text, position, literal, 0, literal.length()) == false) {
                return ~position;
            }
            return position + literal.length();
        }

        @Override
        public String toString() {
            String converted = literal.replace("'", "''");
            return "'" + converted + "'";
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 打印和解析带有可选填充的数字日期时间字段。
     */
    static class NumberPrinterParser implements DateTimePrinterParser {

        /**
         * 10 的 n 次方数组。
         */
        static final long[] EXCEED_POINTS = new long[] {
            0L,
            10L,
            100L,
            1000L,
            10000L,
            100000L,
            1000000L,
            10000000L,
            100000000L,
            1000000000L,
            10000000000L,
        };

        final TemporalField field;
        final int minWidth;
        final int maxWidth;
        private final SignStyle signStyle;
        final int subsequentWidth;

        /**
         * 构造函数。
         *
         * @param field  要格式化的字段，不为空
         * @param minWidth  最小字段宽度，从 1 到 19
         * @param maxWidth  最大字段宽度，从 minWidth 到 19
         * @param signStyle  正/负号样式，不为空
         */
        NumberPrinterParser(TemporalField field, int minWidth, int maxWidth, SignStyle signStyle) {
            // 由调用者验证
            this.field = field;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.signStyle = signStyle;
            this.subsequentWidth = 0;
        }

        /**
         * 构造函数。
         *
         * @param field  要格式化的字段，不为空
         * @param minWidth  最小字段宽度，从 1 到 19
         * @param maxWidth  最大字段宽度，从 minWidth 到 19
         * @param signStyle  正/负号样式，不为空
         * @param subsequentWidth  后续非负数的宽度，0 或更大，-1 表示由于相邻解析激活而固定宽度
         */
        protected NumberPrinterParser(TemporalField field, int minWidth, int maxWidth, SignStyle signStyle, int subsequentWidth) {
            // 由调用者验证
            this.field = field;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.signStyle = signStyle;
            this.subsequentWidth = subsequentWidth;
        }

        /**
         * 返回一个设置了固定宽度标志的新实例。
         *
         * @return 一个新的更新的打印机-解析器，不为空
         */
        NumberPrinterParser withFixedWidth() {
            if (subsequentWidth == -1) {
                return this;
            }
            return new NumberPrinterParser(field, minWidth, maxWidth, signStyle, -1);
        }

        /**
         * 返回一个具有更新的后续宽度的新实例。
         *
         * @param subsequentWidth  后续非负数的宽度，0 或更大
         * @return 一个新的更新的打印机-解析器，不为空
         */
        NumberPrinterParser withSubsequentWidth(int subsequentWidth) {
            return new NumberPrinterParser(field, minWidth, maxWidth, signStyle, this.subsequentWidth + subsequentWidth);
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long valueLong = context.getValue(field);
            if (valueLong == null) {
                return false;
            }
            long value = getValue(context, valueLong);
            DecimalStyle decimalStyle = context.getDecimalStyle();
            String str = (value == Long.MIN_VALUE ? "9223372036854775808" : Long.toString(Math.abs(value)));
            if (str.length() > maxWidth) {
                throw new DateTimeException("字段 " + field +
                    " 无法打印，因为值 " + value +
                    " 超过了最大打印宽度 " + maxWidth);
            }
            str = decimalStyle.convertNumberToI18N(str);

            if (value >= 0) {
                switch (signStyle) {
                    case EXCEEDS_PAD:
                        if (minWidth < 19 && value >= EXCEED_POINTS[minWidth]) {
                            buf.append(decimalStyle.getPositiveSign());
                        }
                        break;
                    case ALWAYS:
                        buf.append(decimalStyle.getPositiveSign());
                        break;
                }
            } else {
                switch (signStyle) {
                    case NORMAL:
                    case EXCEEDS_PAD:
                    case ALWAYS:
                        buf.append(decimalStyle.getNegativeSign());
                        break;
                    case NOT_NEGATIVE:
                        throw new DateTimeException("字段 " + field +
                            " 无法打印，因为值 " + value +
                            " 根据 SignStyle 不能为负");
                }
            }
            for (int i = 0; i < minWidth - str.length(); i++) {
                buf.append(decimalStyle.getZeroDigit());
            }
            buf.append(str);
            return true;
        }

        /**
         * 获取要输出的值。
         *
         * @param context  上下文
         * @param value  字段的值，不为空
         * @return 值
         */
        long getValue(DateTimePrintContext context, long value) {
            return value;
        }

        /**
         * 对于 NumberPrinterParser，宽度取决于
         * minWidth, maxWidth, signStyle 以及后续字段是否固定。
         * @param context 上下文
         * @return 如果字段是固定宽度，则返回 true
         * @see DateTimeFormatterBuilder#appendValue(java.time.temporal.TemporalField, int)
         */
        boolean isFixedWidth(DateTimeParseContext context) {
            return subsequentWidth == -1 ||
                (subsequentWidth > 0 && minWidth == maxWidth && signStyle == SignStyle.NOT_NEGATIVE);
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int length = text.length();
            if (position == length) {
                return ~position;
            }
            char sign = text.charAt(position);  // 如果位置无效，将引发 IOOBE
            boolean negative = false;
            boolean positive = false;
            if (sign == context.getDecimalStyle().getPositiveSign()) {
                if (signStyle.parse(true, context.isStrict(), minWidth == maxWidth) == false) {
                    return ~position;
                }
                positive = true;
                position++;
            } else if (sign == context.getDecimalStyle().getNegativeSign()) {
                if (signStyle.parse(false, context.isStrict(), minWidth == maxWidth) == false) {
                    return ~position;
                }
                negative = true;
                position++;
            } else {
                if (signStyle == SignStyle.ALWAYS && context.isStrict()) {
                    return ~position;
                }
            }
            int effMinWidth = (context.isStrict() || isFixedWidth(context) ? minWidth : 1);
            int minEndPos = position + effMinWidth;
            if (minEndPos > length) {
                return ~position;
            }
            int effMaxWidth = (context.isStrict() || isFixedWidth(context) ? maxWidth : 9) + Math.max(subsequentWidth, 0);
            long total = 0;
            BigInteger totalBig = null;
            int pos = position;
            for (int pass = 0; pass < 2; pass++) {
                int maxEndPos = Math.min(pos + effMaxWidth, length);
                while (pos < maxEndPos) {
                    char ch = text.charAt(pos++);
                    int digit = context.getDecimalStyle().convertToDigit(ch);
                    if (digit < 0) {
                        pos--;
                        if (pos < minEndPos) {
                            return ~position;  // 需要至少最小宽度的数字
                        }
                        break;
                    }
                    if ((pos - position) > 18) {
                        if (totalBig == null) {
                            totalBig = BigInteger.valueOf(total);
                        }
                        totalBig = totalBig.multiply(BigInteger.TEN).add(BigInteger.valueOf(digit));
                    } else {
                        total = total * 10 + digit;
                    }
                }
                if (subsequentWidth > 0 && pass == 0) {
                    // 现在我们知道正确的宽度，重新解析
                    int parseLen = pos - position;
                    effMaxWidth = Math.max(effMinWidth, parseLen - subsequentWidth);
                    pos = position;
                    total = 0;
                    totalBig = null;
                } else {
                    break;
                }
            }
            if (negative) {
                if (totalBig != null) {
                    if (totalBig.equals(BigInteger.ZERO) && context.isStrict()) {
                        return ~(position - 1);  // 不允许负零
                    }
                    totalBig = totalBig.negate();
                } else {
                    if (total == 0 && context.isStrict()) {
                        return ~(position - 1);  // 不允许负零
                    }
                    total = -total;
                }
            } else if (signStyle == SignStyle.EXCEEDS_PAD && context.isStrict()) {
                int parseLen = pos - position;
                if (positive) {
                    if (parseLen <= minWidth) {
                        return ~(position - 1);  // 只有当 minWidth 超过时才解析 '+'
                    }
                } else {
                    if (parseLen > minWidth) {
                        return ~position;  // 如果 minWidth 超过，则必须解析 '+'
                    }
                }
            }
            if (totalBig != null) {
                if (totalBig.bitLength() > 63) {
                    // 溢出，解析少一位数字
                    totalBig = totalBig.divide(BigInteger.TEN);
                    pos--;
                }
                return setValue(context, totalBig.longValue(), position, pos);
            }
            return setValue(context, total, position, pos);
        }


                    /**
         * 存储值。
         *
         * @param context  存储到的上下文，不为空
         * @param value  值
         * @param errorPos  正在解析的字段的位置
         * @param successPos  解析后的字段位置
         * @return 新的位置
         */
        int setValue(DateTimeParseContext context, long value, int errorPos, int successPos) {
            return context.setParsedField(field, value, errorPos, successPos);
        }

        @Override
        public String toString() {
            if (minWidth == 1 && maxWidth == 19 && signStyle == SignStyle.NORMAL) {
                return "Value(" + field + ")";
            }
            if (minWidth == maxWidth && signStyle == SignStyle.NOT_NEGATIVE) {
                return "Value(" + field + "," + minWidth + ")";
            }
            return "Value(" + field + "," + minWidth + "," + maxWidth + "," + signStyle + ")";
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 打印和解析一个简化的数字日期时间字段。
     */
    static final class ReducedPrinterParser extends NumberPrinterParser {
        /**
         * 用于简化值解析的基准日期。
         */
        static final LocalDate BASE_DATE = LocalDate.of(2000, 1, 1);

        private final int baseValue;
        private final ChronoLocalDate baseDate;

        /**
         * 构造函数。
         *
         * @param field  要格式化的字段，已验证不为空
         * @param minWidth  最小字段宽度，从1到10
         * @param maxWidth  最大字段宽度，从1到10
         * @param baseValue  基准值
         * @param baseDate  基准日期
         */
        ReducedPrinterParser(TemporalField field, int minWidth, int maxWidth,
                int baseValue, ChronoLocalDate baseDate) {
            this(field, minWidth, maxWidth, baseValue, baseDate, 0);
            if (minWidth < 1 || minWidth > 10) {
                throw new IllegalArgumentException("The minWidth must be from 1 to 10 inclusive but was " + minWidth);
            }
            if (maxWidth < 1 || maxWidth > 10) {
                throw new IllegalArgumentException("The maxWidth must be from 1 to 10 inclusive but was " + minWidth);
            }
            if (maxWidth < minWidth) {
                throw new IllegalArgumentException("Maximum width must exceed or equal the minimum width but " +
                        maxWidth + " < " + minWidth);
            }
            if (baseDate == null) {
                if (field.range().isValidValue(baseValue) == false) {
                    throw new IllegalArgumentException("The base value must be within the range of the field");
                }
                if ((((long) baseValue) + EXCEED_POINTS[maxWidth]) > Integer.MAX_VALUE) {
                    throw new DateTimeException("Unable to add printer-parser as the range exceeds the capacity of an int");
                }
            }
        }

        /**
         * 构造函数。
         * 参数已验证。
         *
         * @param field  要格式化的字段，已验证不为空
         * @param minWidth  最小字段宽度，从1到10
         * @param maxWidth  最大字段宽度，从1到10
         * @param baseValue  基准值
         * @param baseDate  基准日期
         * @param subsequentWidth  该实例的后续宽度
         */
        private ReducedPrinterParser(TemporalField field, int minWidth, int maxWidth,
                int baseValue, ChronoLocalDate baseDate, int subsequentWidth) {
            super(field, minWidth, maxWidth, SignStyle.NOT_NEGATIVE, subsequentWidth);
            this.baseValue = baseValue;
            this.baseDate = baseDate;
        }

        @Override
        long getValue(DateTimePrintContext context, long value) {
            long absValue = Math.abs(value);
            int baseValue = this.baseValue;
            if (baseDate != null) {
                Chronology chrono = Chronology.from(context.getTemporal());
                baseValue = chrono.date(baseDate).get(field);
            }
            if (value >= baseValue && value < baseValue + EXCEED_POINTS[minWidth]) {
                // 如果值在minWidth范围内，则使用简化值
                return absValue % EXCEED_POINTS[minWidth];
            }
            // 否则截断以适应maxWidth
            return absValue % EXCEED_POINTS[maxWidth];
        }

        @Override
        int setValue(DateTimeParseContext context, long value, int errorPos, int successPos) {
            int baseValue = this.baseValue;
            if (baseDate != null) {
                Chronology chrono = context.getEffectiveChronology();
                baseValue = chrono.date(baseDate).get(field);

                // 如果Chronology稍后更改，添加一个回调
                final long initialValue = value;
                context.addChronoChangedListener(
                        (_unused) ->  {
                            /* 使用当前Chronology重复设置字段
                             * 忽略成功/错误位置，因为值将被有意覆盖。
                             */
                            setValue(context, initialValue, errorPos, successPos);
                        });
            }
            int parseLen = successPos - errorPos;
            if (parseLen == minWidth && value >= 0) {
                long range = EXCEED_POINTS[minWidth];
                long lastPart = baseValue % range;
                long basePart = baseValue - lastPart;
                if (baseValue > 0) {
                    value = basePart + value;
                } else {
                    value = basePart - value;
                }
                if (value < baseValue) {
                    value += range;
                }
            }
            return context.setParsedField(field, value, errorPos, successPos);
        }

        /**
         * 返回一个设置了固定宽度标志的新实例。
         *
         * @return 一个更新的打印解析器，不为空
         */
        @Override
        ReducedPrinterParser withFixedWidth() {
            if (subsequentWidth == -1) {
                return this;
            }
            return new ReducedPrinterParser(field, minWidth, maxWidth, baseValue, baseDate, -1);
        }

        /**
         * 返回一个更新了后续宽度的新实例。
         *
         * @param subsequentWidth  后续非负数的宽度，0或更大
         * @return 一个更新的打印解析器，不为空
         */
        @Override
        ReducedPrinterParser withSubsequentWidth(int subsequentWidth) {
            return new ReducedPrinterParser(field, minWidth, maxWidth, baseValue, baseDate,
                    this.subsequentWidth + subsequentWidth);
        }

        /**
         * 对于ReducedPrinterParser，如果模式为严格，则固定宽度为false，
         * 否则设置为NumberPrinterParser。
         * @param context 上下文
         * @return 字段是否为固定宽度
         * @see DateTimeFormatterBuilder#appendValueReduced(java.time.temporal.TemporalField, int, int, int)
         */
        @Override
        boolean isFixedWidth(DateTimeParseContext context) {
           if (context.isStrict() == false) {
               return false;
           }
           return super.isFixedWidth(context);
        }

        @Override
        public String toString() {
            return "ReducedValue(" + field + "," + minWidth + "," + maxWidth + "," + (baseDate != null ? baseDate : baseValue) + ")";
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 打印和解析一个带有可选填充的数字日期时间字段。
     */
    static final class FractionPrinterParser implements DateTimePrinterParser {
        private final TemporalField field;
        private final int minWidth;
        private final int maxWidth;
        private final boolean decimalPoint;

        /**
         * 构造函数。
         *
         * @param field  要输出的字段，不为空
         * @param minWidth  最小输出宽度，从0到9
         * @param maxWidth  最大输出宽度，从0到9
         * @param decimalPoint  是否输出本地化的十进制点符号
         */
        FractionPrinterParser(TemporalField field, int minWidth, int maxWidth, boolean decimalPoint) {
            Objects.requireNonNull(field, "field");
            if (field.range().isFixed() == false) {
                throw new IllegalArgumentException("Field must have a fixed set of values: " + field);
            }
            if (minWidth < 0 || minWidth > 9) {
                throw new IllegalArgumentException("Minimum width must be from 0 to 9 inclusive but was " + minWidth);
            }
            if (maxWidth < 1 || maxWidth > 9) {
                throw new IllegalArgumentException("Maximum width must be from 1 to 9 inclusive but was " + maxWidth);
            }
            if (maxWidth < minWidth) {
                throw new IllegalArgumentException("Maximum width must exceed or equal the minimum width but " +
                        maxWidth + " < " + minWidth);
            }
            this.field = field;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.decimalPoint = decimalPoint;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long value = context.getValue(field);
            if (value == null) {
                return false;
            }
            DecimalStyle decimalStyle = context.getDecimalStyle();
            BigDecimal fraction = convertToFraction(value);
            if (fraction.scale() == 0) {  // 如果值为零，则scale为零
                if (minWidth > 0) {
                    if (decimalPoint) {
                        buf.append(decimalStyle.getDecimalSeparator());
                    }
                    for (int i = 0; i < minWidth; i++) {
                        buf.append(decimalStyle.getZeroDigit());
                    }
                }
            } else {
                int outputScale = Math.min(Math.max(fraction.scale(), minWidth), maxWidth);
                fraction = fraction.setScale(outputScale, RoundingMode.FLOOR);
                String str = fraction.toPlainString().substring(2);
                str = decimalStyle.convertNumberToI18N(str);
                if (decimalPoint) {
                    buf.append(decimalStyle.getDecimalSeparator());
                }
                buf.append(str);
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int effectiveMin = (context.isStrict() ? minWidth : 0);
            int effectiveMax = (context.isStrict() ? maxWidth : 9);
            int length = text.length();
            if (position == length) {
                // 如果整个字段是可选的，则有效，否则无效
                return (effectiveMin > 0 ? ~position : position);
            }
            if (decimalPoint) {
                if (text.charAt(position) != context.getDecimalStyle().getDecimalSeparator()) {
                    // 如果整个字段是可选的，则有效，否则无效
                    return (effectiveMin > 0 ? ~position : position);
                }
                position++;
            }
            int minEndPos = position + effectiveMin;
            if (minEndPos > length) {
                return ~position;  // 需要至少minWidth位数字
            }
            int maxEndPos = Math.min(position + effectiveMax, length);
            int total = 0;  // 只解析最多9位数字，因此可以使用int
            int pos = position;
            while (pos < maxEndPos) {
                char ch = text.charAt(pos++);
                int digit = context.getDecimalStyle().convertToDigit(ch);
                if (digit < 0) {
                    if (pos < minEndPos) {
                        return ~position;  // 需要至少minWidth位数字
                    }
                    pos--;
                    break;
                }
                total = total * 10 + digit;
            }
            BigDecimal fraction = new BigDecimal(total).movePointLeft(pos - position);
            long value = convertFromFraction(fraction);
            return context.setParsedField(field, value, position, pos);
        }

        /**
         * 将此字段的值转换为0到1之间的分数。
         * <p>
         * 分数值在0（包含）和1（不包含）之间。
         * 只有当 {@link java.time.temporal.TemporalField#range() 值范围} 是固定的时，才能返回分数。
         * 分数通过从字段范围计算得出，使用9位小数和 {@link RoundingMode#FLOOR FLOOR} 舍入模式。
         * 如果值不连续从最小到最大，则计算不准确。
         * <p>
         * 例如，假设标准定义为1分钟60秒，15秒的分数值将返回为0.25。
         *
         * @param value  要转换的值，必须对此规则有效
         * @return 0到1之间的分数值，不为空
         * @throws DateTimeException 如果值不能转换为分数
         */
        private BigDecimal convertToFraction(long value) {
            ValueRange range = field.range();
            range.checkValidValue(value, field);
            BigDecimal minBD = BigDecimal.valueOf(range.getMinimum());
            BigDecimal rangeBD = BigDecimal.valueOf(range.getMaximum()).subtract(minBD).add(BigDecimal.ONE);
            BigDecimal valueBD = BigDecimal.valueOf(value).subtract(minBD);
            BigDecimal fraction = valueBD.divide(rangeBD, 9, RoundingMode.FLOOR);
            // stripTrailingZeros bug
            return fraction.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : fraction.stripTrailingZeros();
        }

        /**
         * 将0到1之间的分数转换为此字段的值。
         * <p>
         * 分数值必须在0（包含）和1（不包含）之间。
         * 只有当 {@link java.time.temporal.TemporalField#range() 值范围} 是固定的时，才能返回分数。
         * 值通过从字段范围计算得出，并使用 {@link RoundingMode#FLOOR FLOOR} 舍入模式。
         * 如果值不连续从最小到最大，则计算不准确。
         * <p>
         * 例如，假设标准定义为1分钟60秒，0.25的分数值将转换为15。
         *
         * @param fraction  要转换的分数，不为空
         * @return 此规则的有效字段值
         * @throws DateTimeException 如果值不能转换
         */
        private long convertFromFraction(BigDecimal fraction) {
            ValueRange range = field.range();
            BigDecimal minBD = BigDecimal.valueOf(range.getMinimum());
            BigDecimal rangeBD = BigDecimal.valueOf(range.getMaximum()).subtract(minBD).add(BigDecimal.ONE);
            BigDecimal valueBD = fraction.multiply(rangeBD).setScale(0, RoundingMode.FLOOR).add(minBD);
            return valueBD.longValueExact();
        }


    //-----------------------------------------------------------------------
    /**
     * 打印或解析字段文本。
     */
    static final class TextPrinterParser implements DateTimePrinterParser {
        private final TemporalField field;
        private final TextStyle textStyle;
        private final DateTimeTextProvider provider;
        /**
         * 缓存的数字打印解析器。
         * 不可变且易失，因此不需要同步。
         */
        private volatile NumberPrinterParser numberPrinterParser;

        /**
         * 构造函数。
         *
         * @param field  要输出的字段，不为空
         * @param textStyle  文本样式，不为空
         * @param provider  文本提供者，不为空
         */
        TextPrinterParser(TemporalField field, TextStyle textStyle, DateTimeTextProvider provider) {
            // 由调用者验证
            this.field = field;
            this.textStyle = textStyle;
            this.provider = provider;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long value = context.getValue(field);
            if (value == null) {
                return false;
            }
            String text;
            Chronology chrono = context.getTemporal().query(TemporalQueries.chronology());
            if (chrono == null || chrono == IsoChronology.INSTANCE) {
                text = provider.getText(field, value, textStyle, context.getLocale());
            } else {
                text = provider.getText(chrono, field, value, textStyle, context.getLocale());
            }
            if (text == null) {
                return numberPrinterParser().format(context, buf);
            }
            buf.append(text);
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence parseText, int position) {
            int length = parseText.length();
            if (position < 0 || position > length) {
                throw new IndexOutOfBoundsException();
            }
            TextStyle style = (context.isStrict() ? textStyle : null);
            Chronology chrono = context.getEffectiveChronology();
            Iterator<Entry<String, Long>> it;
            if (chrono == null || chrono == IsoChronology.INSTANCE) {
                it = provider.getTextIterator(field, style, context.getLocale());
            } else {
                it = provider.getTextIterator(chrono, field, style, context.getLocale());
            }
            if (it != null) {
                while (it.hasNext()) {
                    Entry<String, Long> entry = it.next();
                    String itText = entry.getKey();
                    if (context.subSequenceEquals(itText, 0, parseText, position, itText.length())) {
                        return context.setParsedField(field, entry.getValue(), position, position + itText.length());
                    }
                }
                if (context.isStrict()) {
                    return ~position;
                }
            }
            return numberPrinterParser().parse(context, parseText, position);
        }

        /**
         * 创建并缓存一个数字打印解析器。
         * @return 该字段的数字打印解析器，不为空
         */
        private NumberPrinterParser numberPrinterParser() {
            if (numberPrinterParser == null) {
                numberPrinterParser = new NumberPrinterParser(field, 1, 19, SignStyle.NORMAL);
            }
            return numberPrinterParser;
        }

        @Override
        public String toString() {
            if (textStyle == TextStyle.FULL) {
                return "Text(" + field + ")";
            }
            return "Text(" + field + "," + textStyle + ")";
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 打印或解析 ISO-8601 瞬时时间。
     */
    static final class InstantPrinterParser implements DateTimePrinterParser {
        // 400 年周期中的天数 = 146097
        // 10,000 年周期中的天数 = 146097 * 25
        // 每天的秒数 = 86400
        private static final long SECONDS_PER_10000_YEARS = 146097L * 25L * 86400L;
        private static final long SECONDS_0000_TO_1970 = ((146097L * 5L) - (30L * 365L + 7L)) * 86400L;
        private final int fractionalDigits;

        InstantPrinterParser(int fractionalDigits) {
            this.fractionalDigits = fractionalDigits;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            // 使用 INSTANT_SECONDS，因此此代码不受 Instant.MAX 的限制
            Long inSecs = context.getValue(INSTANT_SECONDS);
            Long inNanos = null;
            if (context.getTemporal().isSupported(NANO_OF_SECOND)) {
                inNanos = context.getTemporal().getLong(NANO_OF_SECOND);
            }
            if (inSecs == null) {
                return false;
            }
            long inSec = inSecs;
            int inNano = NANO_OF_SECOND.checkValidIntValue(inNanos != null ? inNanos : 0);
            // 大部分使用 LocalDateTime.toString 格式化
            if (inSec >= -SECONDS_0000_TO_1970) {
                // 当前纪元
                long zeroSecs = inSec - SECONDS_PER_10000_YEARS + SECONDS_0000_TO_1970;
                long hi = Math.floorDiv(zeroSecs, SECONDS_PER_10000_YEARS) + 1;
                long lo = Math.floorMod(zeroSecs, SECONDS_PER_10000_YEARS);
                LocalDateTime ldt = LocalDateTime.ofEpochSecond(lo - SECONDS_0000_TO_1970, 0, ZoneOffset.UTC);
                if (hi > 0) {
                    buf.append('+').append(hi);
                }
                buf.append(ldt);
                if (ldt.getSecond() == 0) {
                    buf.append(":00");
                }
            } else {
                // 当前纪元之前
                long zeroSecs = inSec + SECONDS_0000_TO_1970;
                long hi = zeroSecs / SECONDS_PER_10000_YEARS;
                long lo = zeroSecs % SECONDS_PER_10000_YEARS;
                LocalDateTime ldt = LocalDateTime.ofEpochSecond(lo - SECONDS_0000_TO_1970, 0, ZoneOffset.UTC);
                int pos = buf.length();
                buf.append(ldt);
                if (ldt.getSecond() == 0) {
                    buf.append(":00");
                }
                if (hi < 0) {
                    if (ldt.getYear() == -10_000) {
                        buf.replace(pos, pos + 2, Long.toString(hi - 1));
                    } else if (lo == 0) {
                        buf.insert(pos, hi);
                    } else {
                        buf.insert(pos + 1, Math.abs(hi));
                    }
                }
            }
            // 添加小数部分
            if ((fractionalDigits < 0 && inNano > 0) || fractionalDigits > 0) {
                buf.append('.');
                int div = 100_000_000;
                for (int i = 0; ((fractionalDigits == -1 && inNano > 0) ||
                                    (fractionalDigits == -2 && (inNano > 0 || (i % 3) != 0)) ||
                                    i < fractionalDigits); i++) {
                    int digit = inNano / div;
                    buf.append((char) (digit + '0'));
                    inNano = inNano - (digit * div);
                    div = div / 10;
                }
            }
            buf.append('Z');
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            // 新的上下文以避免覆盖年/月/日等字段
            int minDigits = (fractionalDigits < 0 ? 0 : fractionalDigits);
            int maxDigits = (fractionalDigits < 0 ? 9 : fractionalDigits);
            CompositePrinterParser parser = new DateTimeFormatterBuilder()
                    .append(DateTimeFormatter.ISO_LOCAL_DATE).appendLiteral('T')
                    .appendValue(HOUR_OF_DAY, 2).appendLiteral(':')
                    .appendValue(MINUTE_OF_HOUR, 2).appendLiteral(':')
                    .appendValue(SECOND_OF_MINUTE, 2)
                    .appendFraction(NANO_OF_SECOND, minDigits, maxDigits, true)
                    .appendLiteral('Z')
                    .toFormatter().toPrinterParser(false);
            DateTimeParseContext newContext = context.copy();
            int pos = parser.parse(newContext, text, position);
            if (pos < 0) {
                return pos;
            }
            // 解析器将大多数字段限制为 2 位，因此肯定是 int
            // 正确解析的纳秒也保证是有效的
            long yearParsed = newContext.getParsed(YEAR);
            int month = newContext.getParsed(MONTH_OF_YEAR).intValue();
            int day = newContext.getParsed(DAY_OF_MONTH).intValue();
            int hour = newContext.getParsed(HOUR_OF_DAY).intValue();
            int min = newContext.getParsed(MINUTE_OF_HOUR).intValue();
            Long secVal = newContext.getParsed(SECOND_OF_MINUTE);
            Long nanoVal = newContext.getParsed(NANO_OF_SECOND);
            int sec = (secVal != null ? secVal.intValue() : 0);
            int nano = (nanoVal != null ? nanoVal.intValue() : 0);
            int days = 0;
            if (hour == 24 && min == 0 && sec == 0 && nano == 0) {
                hour = 0;
                days = 1;
            } else if (hour == 23 && min == 59 && sec == 60) {
                context.setParsedLeapSecond();
                sec = 59;
            }
            int year = (int) yearParsed % 10_000;
            long instantSecs;
            try {
                LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, min, sec, 0).plusDays(days);
                instantSecs = ldt.toEpochSecond(ZoneOffset.UTC);
                instantSecs += Math.multiplyExact(yearParsed / 10_000L, SECONDS_PER_10000_YEARS);
            } catch (RuntimeException ex) {
                return ~position;
            }
            int successPos = pos;
            successPos = context.setParsedField(INSTANT_SECONDS, instantSecs, position, successPos);
            return context.setParsedField(NANO_OF_SECOND, nano, position, successPos);
        }

        @Override
        public String toString() {
            return "Instant()";
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 打印或解析偏移 ID。
     */
    static final class OffsetIdPrinterParser implements DateTimePrinterParser {
        static final String[] PATTERNS = new String[] {
            "+HH", "+HHmm", "+HH:mm", "+HHMM", "+HH:MM", "+HHMMss", "+HH:MM:ss", "+HHMMSS", "+HH:MM:SS",
        };  // 模式构建器中使用的顺序
        static final OffsetIdPrinterParser INSTANCE_ID_Z = new OffsetIdPrinterParser("+HH:MM:ss", "Z");
        static final OffsetIdPrinterParser INSTANCE_ID_ZERO = new OffsetIdPrinterParser("+HH:MM:ss", "0");

        private final String noOffsetText;
        private final int type;

        /**
         * 构造函数。
         *
         * @param pattern  模式
         * @param noOffsetText  UTC 的文本，不为空
         */
        OffsetIdPrinterParser(String pattern, String noOffsetText) {
            Objects.requireNonNull(pattern, "pattern");
            Objects.requireNonNull(noOffsetText, "noOffsetText");
            this.type = checkPattern(pattern);
            this.noOffsetText = noOffsetText;
        }

        private int checkPattern(String pattern) {
            for (int i = 0; i < PATTERNS.length; i++) {
                if (PATTERNS[i].equals(pattern)) {
                    return i;
                }
            }
            throw new IllegalArgumentException("Invalid zone offset pattern: " + pattern);
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long offsetSecs = context.getValue(OFFSET_SECONDS);
            if (offsetSecs == null) {
                return false;
            }
            int totalSecs = Math.toIntExact(offsetSecs);
            if (totalSecs == 0) {
                buf.append(noOffsetText);
            } else {
                int absHours = Math.abs((totalSecs / 3600) % 100);  // 大于 99 的值会被静默丢弃
                int absMinutes = Math.abs((totalSecs / 60) % 60);
                int absSeconds = Math.abs(totalSecs % 60);
                int bufPos = buf.length();
                int output = absHours;
                buf.append(totalSecs < 0 ? "-" : "+")
                    .append((char) (absHours / 10 + '0')).append((char) (absHours % 10 + '0'));
                if (type >= 3 || (type >= 1 && absMinutes > 0)) {
                    buf.append((type % 2) == 0 ? ":" : "")
                        .append((char) (absMinutes / 10 + '0')).append((char) (absMinutes % 10 + '0'));
                    output += absMinutes;
                    if (type >= 7 || (type >= 5 && absSeconds > 0)) {
                        buf.append((type % 2) == 0 ? ":" : "")
                            .append((char) (absSeconds / 10 + '0')).append((char) (absSeconds % 10 + '0'));
                        output += absSeconds;
                    }
                }
                if (output == 0) {
                    buf.setLength(bufPos);
                    buf.append(noOffsetText);
                }
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int length = text.length();
            int noOffsetLen = noOffsetText.length();
            if (noOffsetLen == 0) {
                if (position == length) {
                    return context.setParsedField(OFFSET_SECONDS, 0, position, position);
                }
            } else {
                if (position == length) {
                    return ~position;
                }
                if (context.subSequenceEquals(text, position, noOffsetText, 0, noOffsetLen)) {
                    return context.setParsedField(OFFSET_SECONDS, 0, position, position + noOffsetLen);
                }
            }

            // 解析正常的正负偏移
            char sign = text.charAt(position);  // 无效位置时抛出 IOOBE
            if (sign == '+' || sign == '-') {
                // 开始
                int negative = (sign == '-' ? -1 : 1);
                int[] array = new int[4];
                array[0] = position + 1;
                if ((parseNumber(array, 1, text, true) ||
                        parseNumber(array, 2, text, type >= 3) ||
                        parseNumber(array, 3, text, false)) == false) {
                    // 成功
                    long offsetSecs = negative * (array[1] * 3600L + array[2] * 60L + array[3]);
                    return context.setParsedField(OFFSET_SECONDS, offsetSecs, position, array[0]);
                }
            }
            // 处理 noOffsetText 为空的特殊情况
            if (noOffsetLen == 0) {
                return context.setParsedField(OFFSET_SECONDS, 0, position, position + noOffsetLen);
            }
            return ~position;
        }
    }


                    /**
         * 解析一个两位数的零前缀数字。
         *
         * @param array  解析数据的数组，0=pos,1=小时,2=分钟,3=秒，不为空
         * @param arrayIndex  要解析的值的索引
         * @param parseText  偏移ID，不为空
         * @param required  该数字是否必需
         * @return 如果发生错误返回true
         */
        private boolean parseNumber(int[] array, int arrayIndex, CharSequence parseText, boolean required) {
            if ((type + 3) / 2 < arrayIndex) {
                return false;  // 忽略秒/分钟
            }
            int pos = array[0];
            if ((type % 2) == 0 && arrayIndex > 1) {
                if (pos + 1 > parseText.length() || parseText.charAt(pos) != ':') {
                    return required;
                }
                pos++;
            }
            if (pos + 2 > parseText.length()) {
                return required;
            }
            char ch1 = parseText.charAt(pos++);
            char ch2 = parseText.charAt(pos++);
            if (ch1 < '0' || ch1 > '9' || ch2 < '0' || ch2 > '9') {
                return required;
            }
            int value = (ch1 - 48) * 10 + (ch2 - 48);
            if (value < 0 || value > 59) {
                return required;
            }
            array[arrayIndex] = value;
            array[0] = pos;
            return false;
        }

        @Override
        public String toString() {
            String converted = noOffsetText.replace("'", "''");
            return "Offset(" + PATTERNS[type] + ",'" + converted + "')";
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 打印或解析一个偏移ID。
     */
    static final class LocalizedOffsetIdPrinterParser implements DateTimePrinterParser {
        private final TextStyle style;

        /**
         * 构造函数。
         *
         * @param style  样式，不为空
         */
        LocalizedOffsetIdPrinterParser(TextStyle style) {
            this.style = style;
        }

        private static StringBuilder appendHMS(StringBuilder buf, int t) {
            return buf.append((char)(t / 10 + '0'))
                      .append((char)(t % 10 + '0'));
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long offsetSecs = context.getValue(OFFSET_SECONDS);
            if (offsetSecs == null) {
                return false;
            }
            String gmtText = "GMT";  // TODO: 获取 'GMT' 的本地化版本
            if (gmtText != null) {
                buf.append(gmtText);
            }
            int totalSecs = Math.toIntExact(offsetSecs);
            if (totalSecs != 0) {
                int absHours = Math.abs((totalSecs / 3600) % 100);  // 任何大于99的值都会被静默丢弃
                int absMinutes = Math.abs((totalSecs / 60) % 60);
                int absSeconds = Math.abs(totalSecs % 60);
                buf.append(totalSecs < 0 ? "-" : "+");
                if (style == TextStyle.FULL) {
                    appendHMS(buf, absHours);
                    buf.append(':');
                    appendHMS(buf, absMinutes);
                    if (absSeconds != 0) {
                       buf.append(':');
                       appendHMS(buf, absSeconds);
                    }
                } else {
                    if (absHours >= 10) {
                        buf.append((char)(absHours / 10 + '0'));
                    }
                    buf.append((char)(absHours % 10 + '0'));
                    if (absMinutes != 0 || absSeconds != 0) {
                        buf.append(':');
                        appendHMS(buf, absMinutes);
                        if (absSeconds != 0) {
                            buf.append(':');
                            appendHMS(buf, absSeconds);
                        }
                    }
                }
            }
            return true;
        }

        int getDigit(CharSequence text, int position) {
            char c = text.charAt(position);
            if (c < '0' || c > '9') {
                return -1;
            }
            return c - '0';
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int pos = position;
            int end = pos + text.length();
            String gmtText = "GMT";  // TODO: 获取 'GMT' 的本地化版本
            if (gmtText != null) {
                if (!context.subSequenceEquals(text, pos, gmtText, 0, gmtText.length())) {
                    return ~position;
                }
                pos += gmtText.length();
            }
            // 解析正常的加/减偏移
            int negative = 0;
            if (pos == end) {
                return context.setParsedField(OFFSET_SECONDS, 0, position, pos);
            }
            char sign = text.charAt(pos);  // 如果位置无效则抛出IOOBE
            if (sign == '+') {
                negative = 1;
            } else if (sign == '-') {
                negative = -1;
            } else {
                return context.setParsedField(OFFSET_SECONDS, 0, position, pos);
            }
            pos++;
            int h = 0;
            int m = 0;
            int s = 0;
            if (style == TextStyle.FULL) {
                int h1 = getDigit(text, pos++);
                int h2 = getDigit(text, pos++);
                if (h1 < 0 || h2 < 0 || text.charAt(pos++) != ':') {
                    return ~position;
                }
                h = h1 * 10 + h2;
                int m1 = getDigit(text, pos++);
                int m2 = getDigit(text, pos++);
                if (m1 < 0 || m2 < 0) {
                    return ~position;
                }
                m = m1 * 10 + m2;
                if (pos + 2 < end && text.charAt(pos) == ':') {
                    int s1 = getDigit(text, pos + 1);
                    int s2 = getDigit(text, pos + 2);
                    if (s1 >= 0 && s2 >= 0) {
                        s = s1 * 10 + s2;
                        pos += 3;
                    }
                }
            } else {
                h = getDigit(text, pos++);
                if (h < 0) {
                    return ~position;
                }
                if (pos < end) {
                    int h2 = getDigit(text, pos);
                    if (h2 >=0) {
                        h = h * 10 + h2;
                        pos++;
                    }
                    if (pos + 2 < end && text.charAt(pos) == ':') {
                        if (pos + 2 < end && text.charAt(pos) == ':') {
                            int m1 = getDigit(text, pos + 1);
                            int m2 = getDigit(text, pos + 2);
                            if (m1 >= 0 && m2 >= 0) {
                                m = m1 * 10 + m2;
                                pos += 3;
                                if (pos + 2 < end && text.charAt(pos) == ':') {
                                    int s1 = getDigit(text, pos + 1);
                                    int s2 = getDigit(text, pos + 2);
                                    if (s1 >= 0 && s2 >= 0) {
                                        s = s1 * 10 + s2;
                                        pos += 3;
                                   }
                                }
                            }
                        }
                    }
                }
            }
            long offsetSecs = negative * (h * 3600L + m * 60L + s);
            return context.setParsedField(OFFSET_SECONDS, offsetSecs, position, pos);
        }

        @Override
        public String toString() {
            return "LocalizedOffset(" + style + ")";
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 打印或解析一个时区ID。
     */
    static final class ZoneTextPrinterParser extends ZoneIdPrinterParser {

        /** 要输出的文本样式。 */
        private final TextStyle textStyle;

        /** 优先的时区ID映射 */
        private Set<String> preferredZones;

        ZoneTextPrinterParser(TextStyle textStyle, Set<ZoneId> preferredZones) {
            super(TemporalQueries.zone(), "ZoneText(" + textStyle + ")");
            this.textStyle = Objects.requireNonNull(textStyle, "textStyle");
            if (preferredZones != null && preferredZones.size() != 0) {
                this.preferredZones = new HashSet<>();
                for (ZoneId id : preferredZones) {
                    this.preferredZones.add(id.getId());
                }
            }
        }

        private static final int STD = 0;
        private static final int DST = 1;
        private static final int GENERIC = 2;
        private static final Map<String, SoftReference<Map<Locale, String[]>>> cache =
            new ConcurrentHashMap<>();

        private String getDisplayName(String id, int type, Locale locale) {
            if (textStyle == TextStyle.NARROW) {
                return null;
            }
            String[] names;
            SoftReference<Map<Locale, String[]>> ref = cache.get(id);
            Map<Locale, String[]> perLocale = null;
            if (ref == null || (perLocale = ref.get()) == null ||
                (names = perLocale.get(locale)) == null) {
                names = TimeZoneNameUtility.retrieveDisplayNames(id, locale);
                if (names == null) {
                    return null;
                }
                names = Arrays.copyOfRange(names, 0, 7);
                names[5] =
                    TimeZoneNameUtility.retrieveGenericDisplayName(id, TimeZone.LONG, locale);
                if (names[5] == null) {
                    names[5] = names[0]; // 使用ID
                }
                names[6] =
                    TimeZoneNameUtility.retrieveGenericDisplayName(id, TimeZone.SHORT, locale);
                if (names[6] == null) {
                    names[6] = names[0];
                }
                if (perLocale == null) {
                    perLocale = new ConcurrentHashMap<>();
                }
                perLocale.put(locale, names);
                cache.put(id, new SoftReference<>(perLocale));
            }
            switch (type) {
            case STD:
                return names[textStyle.zoneNameStyleIndex() + 1];
            case DST:
                return names[textStyle.zoneNameStyleIndex() + 3];
            }
            return names[textStyle.zoneNameStyleIndex() + 5];
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            ZoneId zone = context.getValue(TemporalQueries.zoneId());
            if (zone == null) {
                return false;
            }
            String zname = zone.getId();
            if (!(zone instanceof ZoneOffset)) {
                TemporalAccessor dt = context.getTemporal();
                String name = getDisplayName(zname,
                                             dt.isSupported(ChronoField.INSTANT_SECONDS)
                                             ? (zone.getRules().isDaylightSavings(Instant.from(dt)) ? DST : STD)
                                             : GENERIC,
                                             context.getLocale());
                if (name != null) {
                    zname = name;
                }
            }
            buf.append(zname);
            return true;
        }

        // 每个实例的缓存
        private final Map<Locale, Entry<Integer, SoftReference<PrefixTree>>>
            cachedTree = new HashMap<>();
        private final Map<Locale, Entry<Integer, SoftReference<PrefixTree>>>
            cachedTreeCI = new HashMap<>();

        @Override
        protected PrefixTree getTree(DateTimeParseContext context) {
            if (textStyle == TextStyle.NARROW) {
                return super.getTree(context);
            }
            Locale locale = context.getLocale();
            boolean isCaseSensitive = context.isCaseSensitive();
            Set<String> regionIds = ZoneRulesProvider.getAvailableZoneIds();
            int regionIdsSize = regionIds.size();

            Map<Locale, Entry<Integer, SoftReference<PrefixTree>>> cached =
                isCaseSensitive ? cachedTree : cachedTreeCI;

            Entry<Integer, SoftReference<PrefixTree>> entry = null;
            PrefixTree tree = null;
            String[][] zoneStrings = null;
            if ((entry = cached.get(locale)) == null ||
                (entry.getKey() != regionIdsSize ||
                (tree = entry.getValue().get()) == null)) {
                tree = PrefixTree.newTree(context);
                zoneStrings = TimeZoneNameUtility.getZoneStrings(locale);
                for (String[] names : zoneStrings) {
                    String zid = names[0];
                    if (!regionIds.contains(zid)) {
                        continue;
                    }
                    tree.add(zid, zid);    // 不转换 zid -> metazone
                    zid = ZoneName.toZid(zid, locale);
                    int i = textStyle == TextStyle.FULL ? 1 : 2;
                    for (; i < names.length; i += 2) {
                        tree.add(names[i], zid);
                    }
                }
                // 如果有一组优先的时区，需要复制并再次添加优先的时区以覆盖
                if (preferredZones != null) {
                    for (String[] names : zoneStrings) {
                        String zid = names[0];
                        if (!preferredZones.contains(zid) || !regionIds.contains(zid)) {
                            continue;
                        }
                        int i = textStyle == TextStyle.FULL ? 1 : 2;
                        for (; i < names.length; i += 2) {
                            tree.add(names[i], zid);
                       }
                    }
                }
                cached.put(locale, new SimpleImmutableEntry<>(regionIdsSize, new SoftReference<>(tree)));
            }
            return tree;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 打印或解析一个时区ID。
     */
    static class ZoneIdPrinterParser implements DateTimePrinterParser {
        private final TemporalQuery<ZoneId> query;
        private final String description;

        ZoneIdPrinterParser(TemporalQuery<ZoneId> query, String description) {
            this.query = query;
            this.description = description;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            ZoneId zone = context.getValue(query);
            if (zone == null) {
                return false;
            }
            buf.append(zone.getId());
            return true;
        }

        /**
         * 用于加速解析的缓存树。
         */
        private static volatile Entry<Integer, PrefixTree> cachedPrefixTree;
        private static volatile Entry<Integer, PrefixTree> cachedPrefixTreeCI;

        protected PrefixTree getTree(DateTimeParseContext context) {
            // 准备解析树
            Set<String> regionIds = ZoneRulesProvider.getAvailableZoneIds();
            final int regionIdsSize = regionIds.size();
            Entry<Integer, PrefixTree> cached = context.isCaseSensitive()
                                                ? cachedPrefixTree : cachedPrefixTreeCI;
            if (cached == null || cached.getKey() != regionIdsSize) {
                synchronized (this) {
                    cached = context.isCaseSensitive() ? cachedPrefixTree : cachedPrefixTreeCI;
                    if (cached == null || cached.getKey() != regionIdsSize) {
                        cached = new SimpleImmutableEntry<>(regionIdsSize, PrefixTree.newTree(regionIds, context));
                        if (context.isCaseSensitive()) {
                            cachedPrefixTree = cached;
                        } else {
                            cachedPrefixTreeCI = cached;
                        }
                    }
                }
            }
            return cached.getValue();
        }
    }


                    /**
         * 此实现查找最长的匹配字符串。
         * 例如，解析 Etc/GMT-2 将返回 Etc/GMC-2 而不是仅返回 Etc/GMC，尽管两者都是有效的。
         */
        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int length = text.length();
            if (position > length) {
                throw new IndexOutOfBoundsException();
            }
            if (position == length) {
                return ~position;
            }

            // 处理固定时区 ID
            char nextChar = text.charAt(position);
            if (nextChar == '+' || nextChar == '-') {
                return parseOffsetBased(context, text, position, position, OffsetIdPrinterParser.INSTANCE_ID_Z);
            } else if (length >= position + 2) {
                char nextNextChar = text.charAt(position + 1);
                if (context.charEquals(nextChar, 'U') && context.charEquals(nextNextChar, 'T')) {
                    if (length >= position + 3 && context.charEquals(text.charAt(position + 2), 'C')) {
                        return parseOffsetBased(context, text, position, position + 3, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                    }
                    return parseOffsetBased(context, text, position, position + 2, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                } else if (context.charEquals(nextChar, 'G') && length >= position + 3 &&
                        context.charEquals(nextNextChar, 'M') && context.charEquals(text.charAt(position + 2), 'T')) {
                    return parseOffsetBased(context, text, position, position + 3, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                }
            }

            // 解析
            PrefixTree tree = getTree(context);
            ParsePosition ppos = new ParsePosition(position);
            String parsedZoneId = tree.match(text, ppos);
            if (parsedZoneId == null) {
                if (context.charEquals(nextChar, 'Z')) {
                    context.setParsed(ZoneOffset.UTC);
                    return position + 1;
                }
                return ~position;
            }
            context.setParsed(ZoneId.of(parsedZoneId));
            return ppos.getIndex();
        }

        /**
         * 解析前缀后的偏移量并设置 ZoneId（如果有效）。
         * 为了匹配 ZoneId.of 的解析，值不会被规范化为 ZoneOffsets。
         *
         * @param context 解析上下文
         * @param text 输入文本
         * @param prefixPos 前缀的起始位置
         * @param position 前缀后的文本起始位置
         * @param parser 前缀后的值的解析器
         * @return 解析后的位置
         */
        private int parseOffsetBased(DateTimeParseContext context, CharSequence text, int prefixPos, int position, OffsetIdPrinterParser parser) {
            String prefix = text.toString().substring(prefixPos, position).toUpperCase();
            if (position >= text.length()) {
                context.setParsed(ZoneId.of(prefix));
                return position;
            }

            // '0' 或 'Z' 在前缀后不是有效的 ZoneId 的一部分；使用裸前缀
            if (text.charAt(position) == '0' ||
                context.charEquals(text.charAt(position), 'Z')) {
                context.setParsed(ZoneId.of(prefix));
                return position;
            }

            DateTimeParseContext newContext = context.copy();
            int endPos = parser.parse(newContext, text, position);
            try {
                if (endPos < 0) {
                    if (parser == OffsetIdPrinterParser.INSTANCE_ID_Z) {
                        return ~prefixPos;
                    }
                    context.setParsed(ZoneId.of(prefix));
                    return position;
                }
                int offset = (int) newContext.getParsed(OFFSET_SECONDS).longValue();
                ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(offset);
                context.setParsed(ZoneId.ofOffset(prefix, zoneOffset));
                return endPos;
            } catch (DateTimeException dte) {
                return ~prefixPos;
            }
        }

        @Override
        public String toString() {
            return description;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 用于解析时区名称的基于字符串的前缀树。
     */
    static class PrefixTree {
        protected String key;
        protected String value;
        protected char c0;    // 性能优化，避免 key.charAt(0) 的边界检查成本
        protected PrefixTree child;
        protected PrefixTree sibling;

        private PrefixTree(String k, String v, PrefixTree child) {
            this.key = k;
            this.value = v;
            this.child = child;
            if (k.length() == 0){
                c0 = 0xffff;
            } else {
                c0 = key.charAt(0);
            }
        }

        /**
         * 基于解析上下文创建新的前缀解析树。
         *
         * @param context 解析上下文
         * @return 树，不为空
         */
        public static PrefixTree newTree(DateTimeParseContext context) {
            //if (!context.isStrict()) {
            //    return new LENIENT("", null, null);
            //}
            if (context.isCaseSensitive()) {
                return new PrefixTree("", null, null);
            }
            return new CI("", null, null);
        }

        /**
         * 创建新的前缀解析树。
         *
         * @param keys 用于构建前缀解析树的字符串集，不为空
         * @param context 解析上下文
         * @return 树，不为空
         */
        public static  PrefixTree newTree(Set<String> keys, DateTimeParseContext context) {
            PrefixTree tree = newTree(context);
            for (String k : keys) {
                tree.add0(k, k);
            }
            return tree;
        }

        /**
         * 克隆此树的副本
         */
        public PrefixTree copyTree() {
            PrefixTree copy = new PrefixTree(key, value, null);
            if (child != null) {
                copy.child = child.copyTree();
            }
            if (sibling != null) {
                copy.sibling = sibling.copyTree();
            }
            return copy;
        }


        /**
         * 将 {key, value} 对添加到前缀树中。
         *
         * @param k 键，不为空
         * @param v 值，不为空
         * @return 如果对成功添加则返回 true
         */
        public boolean add(String k, String v) {
            return add0(k, v);
        }

        private boolean add0(String k, String v) {
            k = toKey(k);
            int prefixLen = prefixLength(k);
            if (prefixLen == key.length()) {
                if (prefixLen < k.length()) {  // 沿树向下
                    String subKey = k.substring(prefixLen);
                    PrefixTree c = child;
                    while (c != null) {
                        if (isEqual(c.c0, subKey.charAt(0))) {
                            return c.add0(subKey, v);
                        }
                        c = c.sibling;
                    }
                    // 将节点作为当前节点的子节点添加
                    c = newNode(subKey, v, null);
                    c.sibling = child;
                    child = c;
                    return true;
                }
                // 已经存在 <key, value>，覆盖它
                // if (value != null) {
                //    return false;
                //}
                value = v;
                return true;
            }
            // 分割现有节点
            PrefixTree n1 = newNode(key.substring(prefixLen), value, child);
            key = k.substring(0, prefixLen);
            child = n1;
            if (prefixLen < k.length()) {
                PrefixTree n2 = newNode(k.substring(prefixLen), v, null);
                child.sibling = n2;
                value = null;
            } else {
                value = v;
            }
            return true;
        }

        /**
         * 用前缀树匹配文本。
         *
         * @param text 要解析的输入文本，不为空
         * @param off 开始解析的偏移位置
         * @param end 停止解析的结束位置
         * @return 匹配的结果字符串，如果没有找到匹配项则返回 null。
         */
        public String match(CharSequence text, int off, int end) {
            if (!prefixOf(text, off, end)){
                return null;
            }
            if (child != null && (off += key.length()) != end) {
                PrefixTree c = child;
                do {
                    if (isEqual(c.c0, text.charAt(off))) {
                        String found = c.match(text, off, end);
                        if (found != null) {
                            return found;
                        }
                        return value;
                    }
                    c = c.sibling;
                } while (c != null);
            }
            return value;
        }

        /**
         * 用前缀树匹配文本。
         *
         * @param text 要解析的输入文本，不为空
         * @param pos 开始解析的位置，从 0 到文本长度。返回时，位置将更新为新的解析位置，如果没有找到匹配项则位置不变。
         * @return 匹配的结果字符串，如果没有找到匹配项则返回 null。
         */
        public String match(CharSequence text, ParsePosition pos) {
            int off = pos.getIndex();
            int end = text.length();
            if (!prefixOf(text, off, end)){
                return null;
            }
            off += key.length();
            if (child != null && off != end) {
                PrefixTree c = child;
                do {
                    if (isEqual(c.c0, text.charAt(off))) {
                        pos.setIndex(off);
                        String found = c.match(text, pos);
                        if (found != null) {
                            return found;
                        }
                        break;
                    }
                    c = c.sibling;
                } while (c != null);
            }
            pos.setIndex(off);
            return value;
        }

        protected String toKey(String k) {
            return k;
        }

        protected PrefixTree newNode(String k, String v, PrefixTree child) {
            return new PrefixTree(k, v, child);
        }

        protected boolean isEqual(char c1, char c2) {
            return c1 == c2;
        }

        protected boolean prefixOf(CharSequence text, int off, int end) {
            if (text instanceof String) {
                return ((String)text).startsWith(key, off);
            }
            int len = key.length();
            if (len > end - off) {
                return false;
            }
            int off0 = 0;
            while (len-- > 0) {
                if (!isEqual(key.charAt(off0++), text.charAt(off++))) {
                    return false;
                }
            }
            return true;
        }

        private int prefixLength(String k) {
            int off = 0;
            while (off < k.length() && off < key.length()) {
                if (!isEqual(k.charAt(off), key.charAt(off))) {
                    return off;
                }
                off++;
            }
            return off;
        }

        /**
         * 不区分大小写的前缀树。
         */
        private static class CI extends PrefixTree {

            private CI(String k, String v, PrefixTree child) {
                super(k, v, child);
            }

            @Override
            protected CI newNode(String k, String v, PrefixTree child) {
                return new CI(k, v, child);
            }

            @Override
            protected boolean isEqual(char c1, char c2) {
                return DateTimeParseContext.charEqualsIgnoreCase(c1, c2);
            }

            @Override
            protected boolean prefixOf(CharSequence text, int off, int end) {
                int len = key.length();
                if (len > end - off) {
                    return false;
                }
                int off0 = 0;
                while (len-- > 0) {
                    if (!isEqual(key.charAt(off0++), text.charAt(off++))) {
                        return false;
                    }
                }
                return true;
            }
        }

        /**
         * 宽松的前缀树。不区分大小写并忽略空格、下划线和斜杠等字符。
         */
        private static class LENIENT extends CI {

            private LENIENT(String k, String v, PrefixTree child) {
                super(k, v, child);
            }

            @Override
            protected CI newNode(String k, String v, PrefixTree child) {
                return new LENIENT(k, v, child);
            }

            private boolean isLenientChar(char c) {
                return c == ' ' || c == '_' || c == '/';
            }

            protected String toKey(String k) {
                for (int i = 0; i < k.length(); i++) {
                    if (isLenientChar(k.charAt(i))) {
                        StringBuilder sb = new StringBuilder(k.length());
                        sb.append(k, 0, i);
                        i++;
                        while (i < k.length()) {
                            if (!isLenientChar(k.charAt(i))) {
                                sb.append(k.charAt(i));
                            }
                            i++;
                        }
                        return sb.toString();
                    }
                }
                return k;
            }

            @Override
            public String match(CharSequence text, ParsePosition pos) {
                int off = pos.getIndex();
                int end = text.length();
                int len = key.length();
                int koff = 0;
                while (koff < len && off < end) {
                    if (isLenientChar(text.charAt(off))) {
                        off++;
                        continue;
                    }
                    if (!isEqual(key.charAt(koff++), text.charAt(off++))) {
                        return null;
                    }
                }
                if (koff != len) {
                    return null;
                }
                if (child != null && off != end) {
                    int off0 = off;
                    while (off0 < end && isLenientChar(text.charAt(off0))) {
                        off0++;
                    }
                    if (off0 < end) {
                        PrefixTree c = child;
                        do {
                            if (isEqual(c.c0, text.charAt(off0))) {
                                pos.setIndex(off0);
                                String found = c.match(text, pos);
                                if (found != null) {
                                    return found;
                                }
                                break;
                            }
                            c = c.sibling;
                        } while (c != null);
                    }
                }
                pos.setIndex(off);
                return value;
            }
        }
    }


                //-----------------------------------------------------------------------
    /**
     * 打印或解析一个时间轴。
     */
    static final class ChronoPrinterParser implements DateTimePrinterParser {
        /** 要输出的文本样式，null 表示 ID。 */
        private final TextStyle textStyle;

        ChronoPrinterParser(TextStyle textStyle) {
            // 由调用者验证
            this.textStyle = textStyle;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Chronology chrono = context.getValue(TemporalQueries.chronology());
            if (chrono == null) {
                return false;
            }
            if (textStyle == null) {
                buf.append(chrono.getId());
            } else {
                buf.append(getChronologyName(chrono, context.getLocale()));
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            // 简单的循环解析器以找到时间轴
            if (position < 0 || position > text.length()) {
                throw new IndexOutOfBoundsException();
            }
            Set<Chronology> chronos = Chronology.getAvailableChronologies();
            Chronology bestMatch = null;
            int matchLen = -1;
            for (Chronology chrono : chronos) {
                String name;
                if (textStyle == null) {
                    name = chrono.getId();
                } else {
                    name = getChronologyName(chrono, context.getLocale());
                }
                int nameLen = name.length();
                if (nameLen > matchLen && context.subSequenceEquals(text, position, name, 0, nameLen)) {
                    bestMatch = chrono;
                    matchLen = nameLen;
                }
            }
            if (bestMatch == null) {
                return ~position;
            }
            context.setParsed(bestMatch);
            return position + matchLen;
        }

        /**
         * 返回给定时间轴在给定区域设置中的时间轴名称（如果可用），否则返回时间轴 ID。
         * 使用常规的 ResourceBundle 搜索路径来查找时间轴名称。
         *
         * @param chrono  时间轴，不为 null
         * @param locale  区域设置，不为 null
         * @return 时间轴在区域设置中的名称，或如果名称不可用则返回 ID
         * @throws NullPointerException 如果 chrono 或 locale 为 null
         */
        private String getChronologyName(Chronology chrono, Locale locale) {
            String key = "calendarname." + chrono.getCalendarType();
            String name = DateTimeTextProvider.getLocalizedResource(key, locale);
            return name != null ? name : chrono.getId();
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 打印或解析一个本地化模式。
     */
    static final class LocalizedPrinterParser implements DateTimePrinterParser {
        /** 格式化程序缓存。 */
        private static final ConcurrentMap<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>(16, 0.75f, 2);

        private final FormatStyle dateStyle;
        private final FormatStyle timeStyle;

        /**
         * 构造函数。
         *
         * @param dateStyle  要使用的日期样式，可以为 null
         * @param timeStyle  要使用的时间样式，可以为 null
         */
        LocalizedPrinterParser(FormatStyle dateStyle, FormatStyle timeStyle) {
            // 由调用者验证
            this.dateStyle = dateStyle;
            this.timeStyle = timeStyle;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Chronology chrono = Chronology.from(context.getTemporal());
            return formatter(context.getLocale(), chrono).toPrinterParser(false).format(context, buf);
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            Chronology chrono = context.getEffectiveChronology();
            return formatter(context.getLocale(), chrono).toPrinterParser(false).parse(context, text, position);
        }

        /**
         * 获取要使用的格式化程序。
         * <p>
         * 格式化程序将是根据区域设置中最适合日期和时间样式的格式化程序。
         * 例如，某些区域设置将使用月份名称，而其他区域设置将使用数字。
         *
         * @param locale  要使用的区域设置，不为 null
         * @param chrono  要使用的时间轴，不为 null
         * @return 格式化程序，不为 null
         * @throws IllegalArgumentException 如果找不到格式化程序
         */
        private DateTimeFormatter formatter(Locale locale, Chronology chrono) {
            String key = chrono.getId() + '|' + locale.toString() + '|' + dateStyle + timeStyle;
            DateTimeFormatter formatter = FORMATTER_CACHE.get(key);
            if (formatter == null) {
                String pattern = getLocalizedDateTimePattern(dateStyle, timeStyle, chrono, locale);
                formatter = new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter(locale);
                DateTimeFormatter old = FORMATTER_CACHE.putIfAbsent(key, formatter);
                if (old != null) {
                    formatter = old;
                }
            }
            return formatter;
        }

        @Override
        public String toString() {
            return "Localized(" + (dateStyle != null ? dateStyle : "") + "," +
                (timeStyle != null ? timeStyle : "") + ")";
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 打印或解析一个从本地化字段中获取的本地化模式。
     * 具体的格式化程序和参数在打印或解析字段时才选择。
     * 需要区域设置来选择适当的 WeekFields，从中选择星期几、月份中的星期数或年份中的星期数的字段。
     */
    static final class WeekBasedFieldPrinterParser implements DateTimePrinterParser {
        private char chr;
        private int count;

        /**
         * 构造函数。
         *
         * @param chr 添加此 PrinterParser 的模式格式字母。
         * @param count 格式字母的重复计数
         */
        WeekBasedFieldPrinterParser(char chr, int count) {
            this.chr = chr;
            this.count = count;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            return printerParser(context.getLocale()).format(context, buf);
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            return printerParser(context.getLocale()).parse(context, text, position);
        }

        /**
         * 根据字段和区域设置获取要使用的打印机解析器。
         *
         * @param locale  要使用的区域设置，不为 null
         * @return 格式化程序，不为 null
         * @throws IllegalArgumentException 如果找不到格式化程序
         */
        private DateTimePrinterParser printerParser(Locale locale) {
            WeekFields weekDef = WeekFields.of(locale);
            TemporalField field = null;
            switch (chr) {
                case 'Y':
                    field = weekDef.weekBasedYear();
                    if (count == 2) {
                        return new ReducedPrinterParser(field, 2, 2, 0, ReducedPrinterParser.BASE_DATE, 0);
                    } else {
                        return new NumberPrinterParser(field, count, 19,
                                (count < 4) ? SignStyle.NORMAL : SignStyle.EXCEEDS_PAD, -1);
                    }
                case 'e':
                case 'c':
                    field = weekDef.dayOfWeek();
                    break;
                case 'w':
                    field = weekDef.weekOfWeekBasedYear();
                    break;
                case 'W':
                    field = weekDef.weekOfMonth();
                    break;
                default:
                    throw new IllegalStateException("unreachable");
            }
            return new NumberPrinterParser(field, (count == 2 ? 2 : 1), 2, SignStyle.NOT_NEGATIVE);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(30);
            sb.append("Localized(");
            if (chr == 'Y') {
                if (count == 1) {
                    sb.append("WeekBasedYear");
                } else if (count == 2) {
                    sb.append("ReducedValue(WeekBasedYear,2,2,2000-01-01)");
                } else {
                    sb.append("WeekBasedYear,").append(count).append(",")
                            .append(19).append(",")
                            .append((count < 4) ? SignStyle.NORMAL : SignStyle.EXCEEDS_PAD);
                }
            } else {
                switch (chr) {
                    case 'c':
                    case 'e':
                        sb.append("DayOfWeek");
                        break;
                    case 'w':
                        sb.append("WeekOfWeekBasedYear");
                        break;
                    case 'W':
                        sb.append("WeekOfMonth");
                        break;
                    default:
                        break;
                }
                sb.append(",");
                sb.append(count);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    //-------------------------------------------------------------------------
    /**
     * 长度比较器。
     */
    static final Comparator<String> LENGTH_SORT = new Comparator<String>() {
        @Override
        public int compare(String str1, String str2) {
            return str1.length() == str2.length() ? str1.compareTo(str2) : str1.length() - str2.length();
        }
    };
}
