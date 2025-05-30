
/*
 * Copyright (c) 1996, 2016, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright Taligent, Inc. 1996 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - All Rights Reserved
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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.text.spi.DateFormatSymbolsProvider;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleServiceProviderPool;
import sun.util.locale.provider.ResourceBundleBasedAdapter;
import sun.util.locale.provider.TimeZoneNameUtility;

/**
 * <code>DateFormatSymbols</code> 是一个公共类，用于封装日期时间格式化的本地化数据，如月份名称、星期几的名称和时区数据。
 * <code>SimpleDateFormat</code> 使用 <code>DateFormatSymbols</code> 来封装这些信息。
 *
 * <p>
 * 通常你不应该直接使用 <code>DateFormatSymbols</code>。相反，建议使用 <code>DateFormat</code> 类的工厂方法来创建日期时间格式化器：
 * <code>getTimeInstance</code>、<code>getDateInstance</code> 或 <code>getDateTimeInstance</code>。
 * 这些方法会自动为格式化器创建一个 <code>DateFormatSymbols</code>，因此你不需要自己创建。创建格式化器后，可以使用 <code>setPattern</code> 方法修改其格式模式。
 * 有关使用 <code>DateFormat</code> 的工厂方法创建格式化器的更多信息，请参见 {@link DateFormat}。
 *
 * <p>
 * 如果你决定为特定的格式模式和特定的区域创建日期时间格式化器，可以这样做：
 * <blockquote>
 * <pre>
 * new SimpleDateFormat(aPattern, DateFormatSymbols.getInstance(aLocale)).
 * </pre>
 * </blockquote>
 *
 * <p>
 * <code>DateFormatSymbols</code> 对象是可克隆的。当你获得一个 <code>DateFormatSymbols</code> 对象时，可以自由地修改日期时间格式化数据。
 * 例如，你可以将本地化的日期时间格式模式字符替换为你容易记住的字符，或者将代表城市更改为你最喜欢的城市。
 *
 * <p>
 * 可以添加新的 <code>DateFormatSymbols</code> 子类来支持 <code>SimpleDateFormat</code> 为更多区域提供日期时间格式化。
 *
 * @see          DateFormat
 * @see          SimpleDateFormat
 * @see          java.util.SimpleTimeZone
 * @author       Chen-Lieh Huang
 */
public class DateFormatSymbols implements Serializable, Cloneable {

    /**
     * 通过从默认的 {@link java.util.Locale.Category#FORMAT FORMAT} 区域加载格式数据来构造一个 <code>DateFormatSymbols</code> 对象。
     * 该构造函数只能构造由 Java 运行时环境支持的区域的实例，而不是由安装的
     * {@link java.text.spi.DateFormatSymbolsProvider DateFormatSymbolsProvider} 实现支持的区域。为了获得完整的区域支持，使用
     * {@link #getInstance(Locale) getInstance} 方法。
     * <p>这相当于调用
     * {@link #DateFormatSymbols(Locale)
     *     DateFormatSymbols(Locale.getDefault(Locale.Category.FORMAT))}。
     * @see #getInstance()
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     * @exception  java.util.MissingResourceException
     *             如果找不到或无法加载默认区域的资源，则抛出此异常。
     */
    public DateFormatSymbols()
    {
        initializeData(Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 通过从给定区域的资源加载格式数据来构造一个 <code>DateFormatSymbols</code> 对象。
     * 该构造函数只能构造由 Java 运行时环境支持的区域的实例，而不是由安装的
     * {@link java.text.spi.DateFormatSymbolsProvider DateFormatSymbolsProvider} 实现支持的区域。为了获得完整的区域支持，使用
     * {@link #getInstance(Locale) getInstance} 方法。
     *
     * @param locale 所需的区域
     * @see #getInstance(Locale)
     * @exception  java.util.MissingResourceException
     *             如果找不到或无法加载指定区域的资源，则抛出此异常。
     */
    public DateFormatSymbols(Locale locale)
    {
        initializeData(locale);
    }

    /**
     * 构造一个未初始化的 <code>DateFormatSymbols</code>。
     */
    private DateFormatSymbols(boolean flag) {
    }

    /**
     * 时代字符串。例如："AD" 和 "BC"。一个包含 2 个字符串的数组，索引为 <code>Calendar.BC</code> 和 <code>Calendar.AD</code>。
     * @serial
     */
    String eras[] = null;

    /**
     * 月份字符串。例如："January"、"February" 等。一个包含 13 个字符串的数组（某些日历有 13 个月），索引为
     * <code>Calendar.JANUARY</code>、<code>Calendar.FEBRUARY</code> 等。
     * @serial
     */
    String months[] = null;

    /**
     * 短月份字符串。例如："Jan"、"Feb" 等。一个包含 13 个字符串的数组（某些日历有 13 个月），索引为
     * <code>Calendar.JANUARY</code>、<code>Calendar.FEBRUARY</code> 等。
     * @serial
     */
    String shortMonths[] = null;

    /**
     * 星期几字符串。例如："Sunday"、"Monday" 等。一个包含 8 个字符串的数组，索引为 <code>Calendar.SUNDAY</code>、
     * <code>Calendar.MONDAY</code> 等。
     * 元素 <code>weekdays[0]</code> 被忽略。
     * @serial
     */
    String weekdays[] = null;

    /**
     * 短星期几字符串。例如："Sun"、"Mon" 等。一个包含 8 个字符串的数组，索引为 <code>Calendar.SUNDAY</code>、
     * <code>Calendar.MONDAY</code> 等。
     * 元素 <code>shortWeekdays[0]</code> 被忽略。
     * @serial
     */
    String shortWeekdays[] = null;

    /**
     * AM 和 PM 字符串。例如："AM" 和 "PM"。一个包含 2 个字符串的数组，索引为 <code>Calendar.AM</code> 和
     * <code>Calendar.PM</code>。
     * @serial
     */
    String ampms[] = null;

    /**
     * 该区域的本地化时区名称。这是一个大小为 <em>n</em> x <em>m</em> 的二维字符串数组，其中 <em>m</em> 至少为 5。
     * 每个 <em>n</em> 行是一个条目，包含单个 <code>TimeZone</code> 的本地化名称。
     * 每个这样的行包含（<code>i</code> 范围为 0..<em>n</em>-1）：
     * <ul>
     * <li><code>zoneStrings[i][0]</code> - 时区 ID</li>
     * <li><code>zoneStrings[i][1]</code> - 标准时间的时区长名称</li>
     * <li><code>zoneStrings[i][2]</code> - 标准时间的时区短名称</li>
     * <li><code>zoneStrings[i][3]</code> - 夏令时的时区长名称</li>
     * <li><code>zoneStrings[i][4]</code> - 夏令时的时区短名称</li>
     * </ul>
     * 时区 ID <em>不是</em> 本地化的；它是 {@link java.util.TimeZone TimeZone} 类的有效 ID 之一，而不是
     * <a href="../java/util/TimeZone.html#CustomID">自定义 ID</a>。
     * 其他所有条目都是本地化名称。
     * @see java.util.TimeZone
     * @serial
     */
    String zoneStrings[][] = null;

    /**
     * 表示 zoneStrings 是通过 setZoneStrings() 方法外部设置的。
     */
    transient boolean isZoneStringsSet = false;

    /**
     * 未本地化的日期时间模式字符。例如：'y'、'd' 等。所有区域都使用相同的这些未本地化的模式字符。
     */
    static final String  patternChars = "GyMdkHmsSEDFwWahKzZYuXL";

    static final int PATTERN_ERA                  =  0; // G
    static final int PATTERN_YEAR                 =  1; // y
    static final int PATTERN_MONTH                =  2; // M
    static final int PATTERN_DAY_OF_MONTH         =  3; // d
    static final int PATTERN_HOUR_OF_DAY1         =  4; // k
    static final int PATTERN_HOUR_OF_DAY0         =  5; // H
    static final int PATTERN_MINUTE               =  6; // m
    static final int PATTERN_SECOND               =  7; // s
    static final int PATTERN_MILLISECOND          =  8; // S
    static final int PATTERN_DAY_OF_WEEK          =  9; // E
    static final int PATTERN_DAY_OF_YEAR          = 10; // D
    static final int PATTERN_DAY_OF_WEEK_IN_MONTH = 11; // F
    static final int PATTERN_WEEK_OF_YEAR         = 12; // w
    static final int PATTERN_WEEK_OF_MONTH        = 13; // W
    static final int PATTERN_AM_PM                = 14; // a
    static final int PATTERN_HOUR1                = 15; // h
    static final int PATTERN_HOUR0                = 16; // K
    static final int PATTERN_ZONE_NAME            = 17; // z
    static final int PATTERN_ZONE_VALUE           = 18; // Z
    static final int PATTERN_WEEK_YEAR            = 19; // Y
    static final int PATTERN_ISO_DAY_OF_WEEK      = 20; // u
    static final int PATTERN_ISO_ZONE             = 21; // X
    static final int PATTERN_MONTH_STANDALONE     = 22; // L

    /**
     * 本地化的日期时间模式字符。例如，某个区域可能希望在其日期格式模式字符串中使用 'u' 而不是 'y' 来表示年份。
     * 该字符串必须恰好 18 个字符长，字符的索引由 <code>DateFormat.ERA_FIELD</code>、
     * <code>DateFormat.YEAR_FIELD</code> 等描述。因此，如果字符串为 "Xz..."，则本地化模式将使用 'X' 表示时代，'z' 表示年份。
     * @serial
     */
    String  localPatternChars = null;

    /**
     * 用于初始化此 <code>DateFormatSymbols</code> 对象的区域。
     *
     * @since 1.6
     * @serial
     */
    Locale locale = null;

    /* 使用 JDK 1.1.4 的 serialVersionUID 以确保互操作性 */
    static final long serialVersionUID = -5987973545549424702L;

    /**
     * 返回此类的 <code>getInstance</code> 方法可以返回本地化实例的所有区域。
     * 返回的数组表示 Java 运行时支持的区域和安装的
     * {@link java.text.spi.DateFormatSymbolsProvider DateFormatSymbolsProvider} 实现支持的区域的并集。
     * 它必须至少包含一个等于 {@link java.util.Locale#US Locale.US} 的 <code>Locale</code> 实例。
     *
     * @return 可以获得本地化 <code>DateFormatSymbols</code> 实例的所有区域的数组。
     * @since 1.6
     */
    public static Locale[] getAvailableLocales() {
        LocaleServiceProviderPool pool=
            LocaleServiceProviderPool.getPool(DateFormatSymbolsProvider.class);
        return pool.getAvailableLocales();
    }

    /**
     * 获取默认区域的 <code>DateFormatSymbols</code> 实例。此方法提供了访问 Java 运行时本身支持的
     * <code>DateFormatSymbols</code> 实例以及安装的
     * {@link java.text.spi.DateFormatSymbolsProvider DateFormatSymbolsProvider} 实现支持的区域的实例。
     * <p>这相当于调用 {@link #getInstance(Locale)
     *     getInstance(Locale.getDefault(Locale.Category.FORMAT))}。
     * @see java.util.Locale#getDefault(java.util.Locale.Category)
     * @see java.util.Locale.Category#FORMAT
     * @return 一个 <code>DateFormatSymbols</code> 实例。
     * @since 1.6
     */
    public static final DateFormatSymbols getInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * 获取指定区域的 <code>DateFormatSymbols</code> 实例。此方法提供了访问 Java 运行时本身支持的
     * <code>DateFormatSymbols</code> 实例以及安装的
     * {@link java.text.spi.DateFormatSymbolsProvider DateFormatSymbolsProvider} 实现支持的区域的实例。
     * @param locale 给定的区域。
     * @return 一个 <code>DateFormatSymbols</code> 实例。
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @since 1.6
     */
    public static final DateFormatSymbols getInstance(Locale locale) {
        DateFormatSymbols dfs = getProviderInstance(locale);
        if (dfs != null) {
            return dfs;
        }
        throw new RuntimeException("DateFormatSymbols 实例创建失败。");
    }

    /**
     * 返回由提供者提供的或在缓存中找到的 DateFormatSymbols。注意，此方法返回的是缓存的实例，而不是其克隆。
     * 因此，该实例不应提供给应用程序。
     */
    static final DateFormatSymbols getInstanceRef(Locale locale) {
        DateFormatSymbols dfs = getProviderInstance(locale);
        if (dfs != null) {
            return dfs;
        }
        throw new RuntimeException("DateFormatSymbols 实例创建失败。");
    }

    private static DateFormatSymbols getProviderInstance(Locale locale) {
        LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(DateFormatSymbolsProvider.class, locale);
        DateFormatSymbolsProvider provider = adapter.getDateFormatSymbolsProvider();
        DateFormatSymbols dfsyms = provider.getInstance(locale);
        if (dfsyms == null) {
            provider = LocaleProviderAdapter.forJRE().getDateFormatSymbolsProvider();
            dfsyms = provider.getInstance(locale);
        }
        return dfsyms;
    }


                /**
     * 获取纪元字符串。例如："AD" 和 "BC"。
     * @return 纪元字符串。
     */
    public String[] getEras() {
        return Arrays.copyOf(eras, eras.length);
    }

    /**
     * 设置纪元字符串。例如："AD" 和 "BC"。
     * @param newEras 新的纪元字符串。
     */
    public void setEras(String[] newEras) {
        eras = Arrays.copyOf(newEras, newEras.length);
        cachedHashCode = 0;
    }

    /**
     * 获取月份字符串。例如："January"，"February" 等。
     *
     * <p>如果语言在格式化和独立使用时需要不同的形式，此方法返回格式化形式的月份名称。例如，捷克语中 January 的首选名称是 <em>ledna</em>（格式化形式），而独立形式是 <em>leden</em>。在这种情况下，此方法返回 {@code "ledna"}。更多详情请参阅 <a href="http://unicode.org/reports/tr35/#Calendar_Elements">
     * Unicode Locale Data Markup Language (LDML) 规范中的日历元素</a>。
     *
     * @return 月份字符串。
     */
    public String[] getMonths() {
        return Arrays.copyOf(months, months.length);
    }

    /**
     * 设置月份字符串。例如："January"，"February" 等。
     * @param newMonths 新的月份字符串。
     */
    public void setMonths(String[] newMonths) {
        months = Arrays.copyOf(newMonths, newMonths.length);
        cachedHashCode = 0;
    }

    /**
     * 获取简短月份字符串。例如："Jan"，"Feb" 等。
     *
     * <p>如果语言在格式化和独立使用时需要不同的形式，此方法返回格式化形式的简短月份名称。例如，加泰罗尼亚语中 January 的首选缩写是 <em>de gen.</em>（格式化形式），而独立形式是 <em>gen.</em>。在这种情况下，此方法返回 {@code "de gen."}。更多详情请参阅 <a href="http://unicode.org/reports/tr35/#Calendar_Elements">
     * Unicode Locale Data Markup Language (LDML) 规范中的日历元素</a>。
     *
     * @return 简短月份字符串。
     */
    public String[] getShortMonths() {
        return Arrays.copyOf(shortMonths, shortMonths.length);
    }

    /**
     * 设置简短月份字符串。例如："Jan"，"Feb" 等。
     * @param newShortMonths 新的简短月份字符串。
     */
    public void setShortMonths(String[] newShortMonths) {
        shortMonths = Arrays.copyOf(newShortMonths, newShortMonths.length);
        cachedHashCode = 0;
    }

    /**
     * 获取星期字符串。例如："Sunday"，"Monday" 等。
     * @return 星期字符串。使用 <code>Calendar.SUNDAY</code>，
     * <code>Calendar.MONDAY</code> 等来索引结果数组。
     */
    public String[] getWeekdays() {
        return Arrays.copyOf(weekdays, weekdays.length);
    }

    /**
     * 设置星期字符串。例如："Sunday"，"Monday" 等。
     * @param newWeekdays 新的星期字符串。数组应使用 <code>Calendar.SUNDAY</code>，
     * <code>Calendar.MONDAY</code> 等来索引。
     */
    public void setWeekdays(String[] newWeekdays) {
        weekdays = Arrays.copyOf(newWeekdays, newWeekdays.length);
        cachedHashCode = 0;
    }

    /**
     * 获取简短星期字符串。例如："Sun"，"Mon" 等。
     * @return 简短星期字符串。使用 <code>Calendar.SUNDAY</code>，
     * <code>Calendar.MONDAY</code> 等来索引结果数组。
     */
    public String[] getShortWeekdays() {
        return Arrays.copyOf(shortWeekdays, shortWeekdays.length);
    }

    /**
     * 设置简短星期字符串。例如："Sun"，"Mon" 等。
     * @param newShortWeekdays 新的简短星期字符串。数组应使用 <code>Calendar.SUNDAY</code>，
     * <code>Calendar.MONDAY</code> 等来索引。
     */
    public void setShortWeekdays(String[] newShortWeekdays) {
        shortWeekdays = Arrays.copyOf(newShortWeekdays, newShortWeekdays.length);
        cachedHashCode = 0;
    }

    /**
     * 获取 AM/PM 字符串。例如："AM" 和 "PM"。
     * @return AM/PM 字符串。
     */
    public String[] getAmPmStrings() {
        return Arrays.copyOf(ampms, ampms.length);
    }

    /**
     * 设置 AM/PM 字符串。例如："AM" 和 "PM"。
     * @param newAmpms 新的 AM/PM 字符串。
     */
    public void setAmPmStrings(String[] newAmpms) {
        ampms = Arrays.copyOf(newAmpms, newAmpms.length);
        cachedHashCode = 0;
    }

    /**
     * 获取时区字符串。不建议使用此方法；建议使用
     * {@link java.util.TimeZone#getDisplayName() TimeZone.getDisplayName()}
     * 代替。
     * <p>
     * 返回值是一个大小为 <em>n</em> x <em>m</em> 的二维字符串数组，其中 <em>m</em> 至少为 5。每个 <em>n</em> 行是一个包含单个 <code>TimeZone</code> 的本地化名称的条目。每个这样的行包含（<code>i</code> 范围从 0..<em>n</em>-1）：
     * <ul>
     * <li><code>zoneStrings[i][0]</code> - 时区 ID</li>
     * <li><code>zoneStrings[i][1]</code> - 标准时间下的时区长名称</li>
     * <li><code>zoneStrings[i][2]</code> - 标准时间下的时区短名称</li>
     * <li><code>zoneStrings[i][3]</code> - 夏令时下的时区长名称</li>
     * <li><code>zoneStrings[i][4]</code> - 夏令时下的时区短名称</li>
     * </ul>
     * 时区 ID <em>不</em> 本地化；它是 {@link java.util.TimeZone TimeZone} 类的有效 ID 之一，而不是
     * <a href="../util/TimeZone.html#CustomID">自定义 ID</a>。
     * 其他所有条目都是本地化名称。如果时区不实施夏令时，则不应使用夏令时名称。
     * <p>
     * 如果已调用 {@link #setZoneStrings(String[][]) setZoneStrings} 方法，则返回该调用提供的字符串。否则，返回的数组包含 Java 运行时和已安装的
     * {@link java.util.spi.TimeZoneNameProvider TimeZoneNameProvider} 实现提供的名称。
     *
     * @return 时区字符串。
     * @see #setZoneStrings(String[][])
     */
    public String[][] getZoneStrings() {
        return getZoneStringsImpl(true);
    }

    /**
     * 设置时区字符串。参数必须是一个大小为 <em>n</em> x <em>m</em> 的二维字符串数组，其中 <em>m</em> 至少为 5。每个 <em>n</em> 行是一个包含单个 <code>TimeZone</code> 的本地化名称的条目。每个这样的行包含（<code>i</code> 范围从 0..<em>n</em>-1）：
     * <ul>
     * <li><code>zoneStrings[i][0]</code> - 时区 ID</li>
     * <li><code>zoneStrings[i][1]</code> - 标准时间下的时区长名称</li>
     * <li><code>zoneStrings[i][2]</code> - 标准时间下的时区短名称</li>
     * <li><code>zoneStrings[i][3]</code> - 夏令时下的时区长名称</li>
     * <li><code>zoneStrings[i][4]</code> - 夏令时下的时区短名称</li>
     * </ul>
     * 时区 ID <em>不</em> 本地化；它是 {@link java.util.TimeZone TimeZone} 类的有效 ID 之一，而不是
     * <a href="../util/TimeZone.html#CustomID">自定义 ID</a>。
     * 其他所有条目都是本地化名称。
     *
     * @param newZoneStrings 新的时区字符串。
     * @exception IllegalArgumentException 如果 <code>newZoneStrings</code> 中任何行的长度小于 5
     * @exception NullPointerException 如果 <code>newZoneStrings</code> 为 null
     * @see #getZoneStrings()
     */
    public void setZoneStrings(String[][] newZoneStrings) {
        String[][] aCopy = new String[newZoneStrings.length][];
        for (int i = 0; i < newZoneStrings.length; ++i) {
            int len = newZoneStrings[i].length;
            if (len < 5) {
                throw new IllegalArgumentException();
            }
            aCopy[i] = Arrays.copyOf(newZoneStrings[i], len);
        }
        zoneStrings = aCopy;
        isZoneStringsSet = true;
        cachedHashCode = 0;
    }

    /**
     * 获取本地化日期时间模式字符。例如：'u'，'t' 等。
     * @return 本地化日期时间模式字符。
     */
    public String getLocalPatternChars() {
        return localPatternChars;
    }

    /**
     * 设置本地化日期时间模式字符。例如：'u'，'t' 等。
     * @param newLocalPatternChars 新的本地化日期时间模式字符。
     */
    public void setLocalPatternChars(String newLocalPatternChars) {
        // 调用 toString() 以在参数为 null 时抛出 NPE
        localPatternChars = newLocalPatternChars.toString();
        cachedHashCode = 0;
    }

    /**
     * 覆盖 Cloneable
     */
    public Object clone()
    {
        try
        {
            DateFormatSymbols other = (DateFormatSymbols)super.clone();
            copyMembers(this, other);
            return other;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 覆盖 hashCode。
     * 为 DateFormatSymbols 对象生成哈希码。
     */
    @Override
    public int hashCode() {
        int hashCode = cachedHashCode;
        if (hashCode == 0) {
            hashCode = 5;
            hashCode = 11 * hashCode + Arrays.hashCode(eras);
            hashCode = 11 * hashCode + Arrays.hashCode(months);
            hashCode = 11 * hashCode + Arrays.hashCode(shortMonths);
            hashCode = 11 * hashCode + Arrays.hashCode(weekdays);
            hashCode = 11 * hashCode + Arrays.hashCode(shortWeekdays);
            hashCode = 11 * hashCode + Arrays.hashCode(ampms);
            hashCode = 11 * hashCode + Arrays.deepHashCode(getZoneStringsWrapper());
            hashCode = 11 * hashCode + Objects.hashCode(localPatternChars);
            cachedHashCode = hashCode;
        }

        return hashCode;
    }

    /**
     * 覆盖 equals
     */
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DateFormatSymbols that = (DateFormatSymbols) obj;
        return (Arrays.equals(eras, that.eras)
                && Arrays.equals(months, that.months)
                && Arrays.equals(shortMonths, that.shortMonths)
                && Arrays.equals(weekdays, that.weekdays)
                && Arrays.equals(shortWeekdays, that.shortWeekdays)
                && Arrays.equals(ampms, that.ampms)
                && Arrays.deepEquals(getZoneStringsWrapper(), that.getZoneStringsWrapper())
                && ((localPatternChars != null
                  && localPatternChars.equals(that.localPatternChars))
                 || (localPatternChars == null
                  && that.localPatternChars == null)));
    }

    // =======================privates===============================

    /**
     * 定义时区偏移的有用常量。
     */
    static final int millisPerHour = 60*60*1000;

    /**
     * 用于按区域设置缓存 DateFormatSymbols 实例的缓存。
     */
    private static final ConcurrentMap<Locale, SoftReference<DateFormatSymbols>> cachedInstances
        = new ConcurrentHashMap<>(3);

    private transient int lastZoneIndex = 0;

    /**
     * 缓存的哈希码
     */
    transient volatile int cachedHashCode = 0;

    /**
     * 使用区域设置数据初始化此 DateFormatSymbols。如果可用，此方法使用给定区域设置的缓存 DateFormatSymbols 实例。如果没有缓存的实例，此方法创建一个未初始化的实例，并从区域设置的资源包中填充其字段，并缓存该实例。注意：zoneStrings 在此方法中未初始化。
     */
    private void initializeData(Locale locale) {
        SoftReference<DateFormatSymbols> ref = cachedInstances.get(locale);
        DateFormatSymbols dfs;
        if (ref == null || (dfs = ref.get()) == null) {
            if (ref != null) {
                // 移除空的 SoftReference
                cachedInstances.remove(locale, ref);
            }
            dfs = new DateFormatSymbols(false);

            // 从区域设置的资源包中初始化字段。
            LocaleProviderAdapter adapter
                = LocaleProviderAdapter.getAdapter(DateFormatSymbolsProvider.class, locale);
            // 避免任何潜在的递归
            if (!(adapter instanceof ResourceBundleBasedAdapter)) {
                adapter = LocaleProviderAdapter.getResourceBundleBased();
            }
            ResourceBundle resource
                = ((ResourceBundleBasedAdapter)adapter).getLocaleData().getDateFormatData(locale);

            dfs.locale = locale;
            // JRE 和 CLDR 使用不同的键
            // JRE: Eras, short.Eras 和 narrow.Eras
            // CLDR: long.Eras, Eras 和 narrow.Eras
            if (resource.containsKey("Eras")) {
                dfs.eras = resource.getStringArray("Eras");
            } else if (resource.containsKey("long.Eras")) {
                dfs.eras = resource.getStringArray("long.Eras");
            } else if (resource.containsKey("short.Eras")) {
                dfs.eras = resource.getStringArray("short.Eras");
            }
            dfs.months = resource.getStringArray("MonthNames");
            dfs.shortMonths = resource.getStringArray("MonthAbbreviations");
            dfs.ampms = resource.getStringArray("AmPmMarkers");
            dfs.localPatternChars = resource.getString("DateTimePatternChars");

            // 星期名称存储在 1 基数的数组中。
            dfs.weekdays = toOneBasedArray(resource.getStringArray("DayNames"));
            dfs.shortWeekdays = toOneBasedArray(resource.getStringArray("DayAbbreviations"));

            // 将 dfs 放入缓存中
            ref = new SoftReference<>(dfs);
            SoftReference<DateFormatSymbols> x = cachedInstances.putIfAbsent(locale, ref);
            if (x != null) {
                DateFormatSymbols y = x.get();
                if (y == null) {
                    // 用 ref 替换空的 SoftReference
                    cachedInstances.replace(locale, x, ref);
                } else {
                    ref = x;
                    dfs = y;
                }
            }
            // 如果资源包的区域设置不是目标区域设置，则为资源包的区域设置添加另一个缓存条目。
            Locale bundleLocale = resource.getLocale();
            if (!bundleLocale.equals(locale)) {
                SoftReference<DateFormatSymbols> z
                    = cachedInstances.putIfAbsent(bundleLocale, ref);
                if (z != null && z.get() == null) {
                    cachedInstances.replace(bundleLocale, z, ref);
                }
            }
        }

        // 将 dfs 的字段值复制到此实例。
        copyMembers(dfs, this);
    }


                private static String[] toOneBasedArray(String[] src) {
        int len = src.length;
        String[] dst = new String[len + 1];
        dst[0] = "";
        for (int i = 0; i < len; i++) {
            dst[i + 1] = src[i];
        }
        return dst;
    }

    /**
     * 包私有：由 SimpleDateFormat 使用
     * 获取给定时区 ID 的索引，以获取用于格式化的时区字符串。时区 ID 仅用于编程查找。未本地化！！！
     * @param ID 给定的时区 ID。
     * @return 给定时区 ID 的索引。如果在 DateFormatSymbols 对象中找不到给定时区 ID，则返回 -1。
     * @see java.util.SimpleTimeZone
     */
    final int getZoneIndex(String ID) {
        String[][] zoneStrings = getZoneStringsWrapper();

        /*
         * 为了性能原因，getZoneIndex 已经重写。不再每次遍历 zoneStrings 数组，而是缓存上次使用的时区索引
         */
        if (lastZoneIndex < zoneStrings.length && ID.equals(zoneStrings[lastZoneIndex][0])) {
            return lastZoneIndex;
        }

        /* 慢路径，搜索整个列表 */
        for (int index = 0; index < zoneStrings.length; index++) {
            if (ID.equals(zoneStrings[index][0])) {
                lastZoneIndex = index;
                return index;
            }
        }

        return -1;
    }

    /**
     * 包装方法，调用 getZoneStrings()，该方法在 java.text 包内调用，不修改返回的数组，因此不需要创建防御性副本。
     */
    final String[][] getZoneStringsWrapper() {
        if (isSubclassObject()) {
            return getZoneStrings();
        } else {
            return getZoneStringsImpl(false);
        }
    }

    private String[][] getZoneStringsImpl(boolean needsCopy) {
        if (zoneStrings == null) {
            zoneStrings = TimeZoneNameUtility.getZoneStrings(locale);
        }

        if (!needsCopy) {
            return zoneStrings;
        }

        int len = zoneStrings.length;
        String[][] aCopy = new String[len][];
        for (int i = 0; i < len; i++) {
            aCopy[i] = Arrays.copyOf(zoneStrings[i], zoneStrings[i].length);
        }
        return aCopy;
    }

    private boolean isSubclassObject() {
        return !getClass().getName().equals("java.text.DateFormatSymbols");
    }

    /**
     * 从源 DateFormatSymbols 复制所有数据成员到目标 DateFormatSymbols。
     *
     * @param src 源 DateFormatSymbols。
     * @param dst 目标 DateFormatSymbols。
     */
    private void copyMembers(DateFormatSymbols src, DateFormatSymbols dst)
    {
        dst.locale = src.locale;
        dst.eras = Arrays.copyOf(src.eras, src.eras.length);
        dst.months = Arrays.copyOf(src.months, src.months.length);
        dst.shortMonths = Arrays.copyOf(src.shortMonths, src.shortMonths.length);
        dst.weekdays = Arrays.copyOf(src.weekdays, src.weekdays.length);
        dst.shortWeekdays = Arrays.copyOf(src.shortWeekdays, src.shortWeekdays.length);
        dst.ampms = Arrays.copyOf(src.ampms, src.ampms.length);
        if (src.zoneStrings != null) {
            dst.zoneStrings = src.getZoneStringsImpl(true);
        } else {
            dst.zoneStrings = null;
        }
        dst.localPatternChars = src.localPatternChars;
        dst.cachedHashCode = 0;
    }

    /**
     * 在确保 <code>zoneStrings</code> 字段已初始化以确保向后兼容性后，写入默认的可序列化数据。
     *
     * @since 1.6
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        if (zoneStrings == null) {
            zoneStrings = TimeZoneNameUtility.getZoneStrings(locale);
        }
        stream.defaultWriteObject();
    }
}
