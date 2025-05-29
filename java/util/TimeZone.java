
/*
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

package java.util;

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.ZoneId;
import sun.security.action.GetPropertyAction;
import sun.util.calendar.ZoneInfo;
import sun.util.calendar.ZoneInfoFile;
import sun.util.locale.provider.TimeZoneNameUtility;

/**
 * <code>TimeZone</code> 表示一个时区偏移，并且计算夏令时。
 *
 * <p>
 * 通常，使用 <code>getDefault</code> 获取一个 <code>TimeZone</code>，它根据程序运行的时区创建一个 <code>TimeZone</code>。
 * 例如，对于在日本运行的程序，<code>getDefault</code> 会创建一个基于日本标准时间的 <code>TimeZone</code> 对象。
 *
 * <p>
 * 也可以使用 <code>getTimeZone</code> 和一个时区 ID 来获取一个 <code>TimeZone</code>。
 * 例如，美国太平洋时间的时区 ID 是 "America/Los_Angeles"。因此，可以使用以下代码获取一个美国太平洋时间的 <code>TimeZone</code> 对象：
 * <blockquote><pre>
 * TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
 * </pre></blockquote>
 * 可以使用 <code>getAvailableIDs</code> 方法遍历所有支持的时区 ID。然后可以选择一个支持的 ID 来获取一个 <code>TimeZone</code>。
 * 如果所需的时区没有被支持的 ID 表示，可以指定一个自定义的时区 ID 来生成一个 <code>TimeZone</code>。自定义时区 ID 的语法如下：
 *
 * <blockquote><pre>
 * <a name="CustomID"><i>自定义ID:</i></a>
 *         <code>GMT</code> <i>符号</i> <i>小时</i> <code>:</code> <i>分钟</i>
 *         <code>GMT</code> <i>符号</i> <i>小时</i> <i>分钟</i>
 *         <code>GMT</code> <i>符号</i> <i>小时</i>
 * <i>符号:</i> 之一
 *         <code>+ -</code>
 * <i>小时:</i>
 *         <i>数字</i>
 *         <i>数字</i> <i>数字</i>
 * <i>分钟:</i>
 *         <i>数字</i> <i>数字</i>
 * <i>数字:</i> 之一
 *         <code>0 1 2 3 4 5 6 7 8 9</code>
 * </pre></blockquote>
 *
 * <i>小时</i> 必须在 0 到 23 之间，<i>分钟</i> 必须在 00 到 59 之间。例如，"GMT+10" 和 "GMT+0010" 分别表示比 GMT 快 10 小时和 10 分钟。
 * <p>
 * 该格式与地区无关，数字必须来自 Unicode 标准的基本拉丁字符块。自定义时区 ID 不能指定夏令时转换时间表。如果指定的字符串不符合语法，将使用 <code>"GMT"</code>。
 * <p>
 * 在创建 <code>TimeZone</code> 时，指定的自定义时区 ID 将被规范化为以下语法：
 * <blockquote><pre>
 * <a name="NormalizedCustomID"><i>规范化自定义ID:</i></a>
 *         <code>GMT</code> <i>符号</i> <i>两位数小时</i> <code>:</code> <i>分钟</i>
 * <i>符号:</i> 之一
 *         <code>+ -</code>
 * <i>两位数小时:</i>
 *         <i>数字</i> <i>数字</i>
 * <i>分钟:</i>
 *         <i>数字</i> <i>数字</i>
 * <i>数字:</i> 之一
 *         <code>0 1 2 3 4 5 6 7 8 9</code>
 * </pre></blockquote>
 * 例如，TimeZone.getTimeZone("GMT-8").getID() 返回 "GMT-08:00"。
 *
 * <h3>三字母时区 ID</h3>
 *
 * 为了与 JDK 1.1.x 兼容，还支持一些其他三字母时区 ID（如 "PST"、"CTT"、"AST"）。然而，<strong>它们的使用已不推荐</strong>，因为同一个缩写通常用于多个时区（例如，"CST" 可以是美国的 "Central Standard Time" 或 "China Standard Time"），而 Java 平台只能识别其中一个。
 *
 *
 * @see          Calendar
 * @see          GregorianCalendar
 * @see          SimpleTimeZone
 * @author       Mark Davis, David Goldsmith, Chen-Lieh Huang, Alan Liu
 * @since        JDK1.1
 */
abstract public class TimeZone implements Serializable, Cloneable {
    /**
     * 唯一构造函数。通常由子类构造函数隐式调用。
     */
    public TimeZone() {
    }

    /**
     * 用于 <code>getDisplayName()</code> 的样式指定符，表示短名称，如 "PST"。
     * @see #LONG
     * @since 1.2
     */
    public static final int SHORT = 0;

    /**
     * 用于 <code>getDisplayName()</code> 的样式指定符，表示长名称，如 "Pacific Standard Time"。
     * @see #SHORT
     * @since 1.2
     */
    public static final int LONG  = 1;

    // 内部使用的常量；单位是毫秒
    private static final int ONE_MINUTE = 60*1000;
    private static final int ONE_HOUR   = 60*ONE_MINUTE;
    private static final int ONE_DAY    = 24*ONE_HOUR;

    // 声明与 JDK 1.1 的序列化兼容性
    static final long serialVersionUID = 3581463369166924961L;

    /**
     * 获取当前日期的时区偏移，如果存在夏令时则进行调整。这是要加到 UTC 上以获取本地时间的偏移。
     * <p>
     * 如果底层的 <code>TimeZone</code> 实现子类支持历史夏令时时间表和 GMT 偏移变化，此方法将返回一个历史正确的偏移。
     *
     * @param era 给定日期的纪元。
     * @param year 给定日期的年份。
     * @param month 给定日期的月份。
     * 月份从 0 开始。例如，0 表示 1 月。
     * @param day 给定日期的月份中的日期。
     * @param dayOfWeek 给定日期的星期几。
     * @param milliseconds 一天中的毫秒数，以 <em>标准</em> 本地时间表示。
     *
     * @return 要加到 GMT 上以获取本地时间的偏移，以毫秒为单位。
     *
     * @see Calendar#ZONE_OFFSET
     * @see Calendar#DST_OFFSET
     */
    public abstract int getOffset(int era, int year, int month, int day,
                                  int dayOfWeek, int milliseconds);

                /**
     * 返回指定日期时此时区与 UTC 的偏移量。如果指定日期实行夏令时，则偏移量值会根据夏令时进行调整。
     * <p>
     * 如果底层 TimeZone 实现子类支持历史夏令时安排和 GMT 偏移量变化，则此方法将返回历史正确的偏移量值。
     *
     * @param date 从 1970 年 1 月 1 日 00:00:00 GMT 开始计算的毫秒数表示的日期
     * @return 需要添加到 UTC 以获取本地时间的时间量（毫秒）。
     *
     * @see Calendar#ZONE_OFFSET
     * @see Calendar#DST_OFFSET
     * @since 1.4
     */
    public int getOffset(long date) {
        if (inDaylightTime(new Date(date))) {
            return getRawOffset() + getDSTSavings();
        }
        return getRawOffset();
    }

    /**
     * 获取此时间在给定时间的原始 GMT 偏移量和夏令时量。
     * @param date 从 1970 年 1 月 1 日 00:00:00.000 GMT 开始计算的毫秒数，用于查找时区偏移量和夏令时量
     * @param offsets 一个 int 数组，用于存储原始 GMT 偏移量（offset[0]）和夏令时量（offset[1]），如果不需要这些值，则为 null。该方法假定给定数组的长度为两个或更大。
     * @return 指定日期的原始 GMT 偏移量和夏令时量的总和。
     *
     * @see Calendar#ZONE_OFFSET
     * @see Calendar#DST_OFFSET
     */
    int getOffsets(long date, int[] offsets) {
        int rawoffset = getRawOffset();
        int dstoffset = 0;
        if (inDaylightTime(new Date(date))) {
            dstoffset = getDSTSavings();
        }
        if (offsets != null) {
            offsets[0] = rawoffset;
            offsets[1] = dstoffset;
        }
        return rawoffset + dstoffset;
    }

    /**
     * 设置基础时区偏移量到 GMT。这是需要添加到 UTC 以获取本地时间的偏移量。
     * <p>
     * 如果底层 <code>TimeZone</code> 实现子类支持历史 GMT 偏移量变化，则指定的 GMT 偏移量将被设置为最新的 GMT 偏移量，并且与已知最新 GMT 偏移量值的差异将用于调整所有历史 GMT 偏移量值。
     *
     * @param offsetMillis 到 GMT 的给定基础时区偏移量。
     */
    abstract public void setRawOffset(int offsetMillis);

    /**
     * 返回需要添加到 UTC 以获取此时区标准时间的时间量（毫秒）。因为这个值不受夏令时影响，所以称为 <I>原始偏移量</I>。
     * <p>
     * 如果底层 <code>TimeZone</code> 实现子类支持历史 GMT 偏移量变化，则该方法返回当前日期的原始偏移量值。例如，在檀香山，其原始偏移量从 GMT-10:30 变为 GMT-10:00（1947 年），此方法始终返回 -36000000 毫秒（即 -10 小时）。
     *
     * @return 需要添加到 UTC 的原始偏移量时间（毫秒）。
     * @see Calendar#ZONE_OFFSET
     */
    public abstract int getRawOffset();

    /**
     * 获取此时间的 ID。
     * @return 此时区的 ID。
     */
    public String getID()
    {
        return ID;
    }

    /**
     * 设置时区 ID。这不会改变时区对象中的任何其他数据。
     * @param ID 新的时区 ID。
     */
    public void setID(String ID)
    {
        if (ID == null) {
            throw new NullPointerException();
        }
        this.ID = ID;
    }

    /**
     * 返回一个适合在默认区域设置中向用户展示的此 {@code TimeZone} 的标准时间名称。
     *
     * <p>此方法等效于：
     * <blockquote><pre>
     * getDisplayName(false, {@link #LONG},
     *                Locale.getDefault({@link Locale.Category#DISPLAY}))
     * </pre></blockquote>
     *
     * @return 在默认区域设置中此时间区的人类可读名称。
     * @since 1.2
     * @see #getDisplayName(boolean, int, Locale)
     * @see Locale#getDefault(Locale.Category)
     * @see Locale.Category
     */
    public final String getDisplayName() {
        return getDisplayName(false, LONG,
                              Locale.getDefault(Locale.Category.DISPLAY));
    }

    /**
     * 返回一个适合在指定 {@code locale} 中向用户展示的此 {@code TimeZone} 的标准时间名称。
     *
     * <p>此方法等效于：
     * <blockquote><pre>
     * getDisplayName(false, {@link #LONG}, locale)
     * </pre></blockquote>
     *
     * @param locale 用于提供显示名称的区域设置。
     * @return 在给定区域设置中此时间区的人类可读名称。
     * @exception NullPointerException 如果 {@code locale} 为 {@code null}。
     * @since 1.2
     * @see #getDisplayName(boolean, int, Locale)
     */
    public final String getDisplayName(Locale locale) {
        return getDisplayName(false, LONG, locale);
    }

    /**
     * 返回一个适合在默认区域设置中向用户展示的此 {@code TimeZone} 的指定 {@code style} 名称。如果指定的 {@code daylight} 为 {@code true}，则返回夏令时名称（即使此 {@code TimeZone} 不实行夏令时）。否则，返回标准时间名称。
     *
     * <p>此方法等效于：
     * <blockquote><pre>
     * getDisplayName(daylight, style,
     *                Locale.getDefault({@link Locale.Category#DISPLAY}))
     * </pre></blockquote>
     *
     * @param daylight {@code true} 表示夏令时名称，或 {@code false} 表示标准时间名称
     * @param style {@link #LONG} 或 {@link #SHORT}
     * @return 在默认区域设置中此时间区的人类可读名称。
     * @exception IllegalArgumentException 如果 {@code style} 无效。
     * @since 1.2
     * @see #getDisplayName(boolean, int, Locale)
     * @see Locale#getDefault(Locale.Category)
     * @see Locale.Category
     * @see java.text.DateFormatSymbols#getZoneStrings()
     */
    public final String getDisplayName(boolean daylight, int style) {
        return getDisplayName(daylight, style,
                              Locale.getDefault(Locale.Category.DISPLAY));
    }


                /**
     * 返回此 {@code TimeZone} 在指定 {@code style} 和 {@code
     * locale} 中适合向用户展示的名称。如果指定的 {@code daylight} 为 {@code true}，则返回夏令时名称（即使此 {@code TimeZone} 不遵循夏令时）。否则，返回标准时间名称。
     *
     * <p>在查找时区名称时，使用从指定 {@code locale} 派生的 {@linkplain
     * ResourceBundle.Control#getCandidateLocales(String,Locale) 默认
     * <code>Locale</code> 搜索路径}。不会执行任何 {@linkplain
     * ResourceBundle.Control#getFallbackLocale(String,Locale) <code>Locale</code> 回退搜索}。如果在搜索路径中的任何 {@code Locale}，包括 {@link Locale#ROOT}，找到时区名称，则返回该名称。否则，返回 <a href="#NormalizedCustomID">规范化自定义 ID 格式</a> 的字符串。
     *
     * @param daylight {@code true} 表示夏令时名称，或 {@code false} 表示标准时间名称
     * @param style 为 {@link #LONG} 或 {@link #SHORT}
     * @param locale   提供显示名称的区域设置。
     * @return 在给定区域设置中此时区的人类可读名称。
     * @exception IllegalArgumentException 如果 {@code style} 无效。
     * @exception NullPointerException 如果 {@code locale} 为 {@code null}。
     * @since 1.2
     * @see java.text.DateFormatSymbols#getZoneStrings()
     */
    public String getDisplayName(boolean daylight, int style, Locale locale) {
        if (style != SHORT && style != LONG) {
            throw new IllegalArgumentException("非法样式: " + style);
        }
        String id = getID();
        String name = TimeZoneNameUtility.retrieveDisplayName(id, daylight, style, locale);
        if (name != null) {
            return name;
        }

        if (id.startsWith("GMT") && id.length() > 3) {
            char sign = id.charAt(3);
            if (sign == '+' || sign == '-') {
                return id;
            }
        }
        int offset = getRawOffset();
        if (daylight) {
            offset += getDSTSavings();
        }
        return ZoneInfoFile.toCustomID(offset);
    }

    private static String[] getDisplayNames(String id, Locale locale) {
        return TimeZoneNameUtility.retrieveDisplayNames(id, locale);
    }

    /**
     * 返回应添加到本地标准时间以获取本地标准时间的时钟时间的时长。
     *
     * <p>默认实现如果调用 {@link #useDaylightTime()}
     * 返回 {@code true}，则返回 3600000 毫秒（即一小时）。否则，返回 0（零）。
     *
     * <p>如果底层 {@code TimeZone} 实现子类支持历史和未来的夏令时时间表更改，此方法返回最后已知夏令时规则的节省时间，这可能是未来的预测。
     *
     * <p>如果需要给定时间戳的节省时间，可以使用此 {@code
     * TimeZone} 和时间戳构造一个 {@link Calendar}，并调用 {@link Calendar#get(int)
     * Calendar.get}{@code (}{@link Calendar#DST_OFFSET}{@code )}。
     *
     * @return 节省时间的毫秒数
     * @since 1.4
     * @see #inDaylightTime(Date)
     * @see #getOffset(long)
     * @see #getOffset(int,int,int,int,int,int)
     * @see Calendar#ZONE_OFFSET
     */
    public int getDSTSavings() {
        if (useDaylightTime()) {
            return 3600000;
        }
        return 0;
    }

    /**
     * 查询此 {@code TimeZone} 是否使用夏令时。
     *
     * <p>如果底层 {@code TimeZone} 实现子类支持历史和未来的夏令时时间表更改，此方法引用最后已知的夏令时规则，这可能是未来的预测，可能与当前规则不同。如果应考虑当前规则，建议调用 {@link #observesDaylightTime()}。
     *
     * @return 如果此 {@code TimeZone} 使用夏令时，则返回 {@code true}，
     *         否则返回 {@code false}。
     * @see #inDaylightTime(Date)
     * @see Calendar#DST_OFFSET
     */
    public abstract boolean useDaylightTime();

    /**
     * 如果此 {@code TimeZone} 当前处于夏令时，或从标准时间到夏令时的转换在未来任何时候发生，则返回 {@code true}。
     *
     * <p>默认实现如果 {@code useDaylightTime()} 或 {@code inDaylightTime(new Date())}
     * 返回 {@code true}，则返回 {@code true}。
     *
     * @return 如果此 {@code TimeZone} 当前处于夏令时，或从标准时间到夏令时的转换在未来任何时候发生，则返回 {@code true}；否则返回 {@code false}。
     * @since 1.7
     * @see #useDaylightTime()
     * @see #inDaylightTime(Date)
     * @see Calendar#DST_OFFSET
     */
    public boolean observesDaylightTime() {
        return useDaylightTime() || inDaylightTime(new Date());
    }

    /**
     * 查询给定的 {@code date} 是否在此时区的夏令时。
     *
     * @param date 给定的日期。
     * @return 如果给定的日期处于夏令时，则返回 {@code true}，
     *         否则返回 {@code false}。
     */
    abstract public boolean inDaylightTime(Date date);

    /**
     * 获取给定 ID 的 <code>TimeZone</code>。
     *
     * @param ID 一个 <code>TimeZone</code> 的 ID，可以是缩写，如 "PST"，全名，如 "America/Los_Angeles"，或自定义 ID，如 "GMT-8:00"。注意，支持缩写仅为了兼容 JDK 1.1.x，应使用全名。
     *
     * @return 指定的 <code>TimeZone</code>，如果给定的 ID 无法识别，则返回 GMT 时区。
     */
    public static synchronized TimeZone getTimeZone(String ID) {
        return getTimeZone(ID, true);
    }


                /**
     * 获取给定 {@code zoneId} 的 {@code TimeZone}。
     *
     * @param zoneId 从中获取时区 ID 的 {@link ZoneId}
     * @return 指定的 {@code TimeZone}，如果给定的 ID 无法识别，则返回 GMT 时区。
     * @throws NullPointerException 如果 {@code zoneId} 为 {@code null}
     * @since 1.8
     */
    public static TimeZone getTimeZone(ZoneId zoneId) {
        String tzid = zoneId.getId(); // 如果为 null，则抛出 NPE
        char c = tzid.charAt(0);
        if (c == '+' || c == '-') {
            tzid = "GMT" + tzid;
        } else if (c == 'Z' && tzid.length() == 1) {
            tzid = "UTC";
        }
        return getTimeZone(tzid, true);
    }

    /**
     * 将此 {@code TimeZone} 对象转换为 {@code ZoneId}。
     *
     * @return 表示与此 {@code TimeZone} 相同时区的 {@code ZoneId}
     * @since 1.8
     */
    public ZoneId toZoneId() {
        String id = getID();
        if (ZoneInfoFile.useOldMapping() && id.length() == 3) {
            if ("EST".equals(id))
                return ZoneId.of("America/New_York");
            if ("MST".equals(id))
                return ZoneId.of("America/Denver");
            if ("HST".equals(id))
                return ZoneId.of("America/Honolulu");
        }
        return ZoneId.of(id, ZoneId.SHORT_IDS);
    }

    private static TimeZone getTimeZone(String ID, boolean fallback) {
        TimeZone tz = ZoneInfo.getTimeZone(ID);
        if (tz == null) {
            tz = parseCustomTimeZone(ID);
            if (tz == null && fallback) {
                tz = new ZoneInfo(GMT_ID, 0);
            }
        }
        return tz;
    }

    /**
     * 根据给定的时间区偏移量（以毫秒为单位）获取可用的 ID。
     *
     * @param rawOffset 给定的时间区 GMT 偏移量（以毫秒为单位）。
     * @return 一个 ID 数组，这些 ID 的时间区具有指定的 GMT 偏移量。例如，"America/Phoenix" 和 "America/Denver"
     * 都有 GMT-07:00，但在夏令时行为上有所不同。
     * @see #getRawOffset()
     */
    public static synchronized String[] getAvailableIDs(int rawOffset) {
        return ZoneInfo.getAvailableIDs(rawOffset);
    }

    /**
     * 获取所有支持的可用 ID。
     * @return 一个 ID 数组。
     */
    public static synchronized String[] getAvailableIDs() {
        return ZoneInfo.getAvailableIDs();
    }

    /**
     * 获取平台定义的时区 ID。
     **/
    private static native String getSystemTimeZoneID(String javaHome);

    /**
     * 根据平台的 GMT 偏移量获取自定义时区 ID（例如，"GMT+08:00"）。
     */
    private static native String getSystemGMTOffsetID();

    /**
     * 获取 Java 虚拟机的默认 {@code TimeZone}。如果缓存的默认 {@code TimeZone} 可用，则返回其克隆。
     * 否则，该方法将采取以下步骤来确定默认时区。
     *
     * <ul>
     * <li>如果可用，使用 {@code user.timezone} 属性值作为默认时区 ID。</li>
     * <li>检测平台时区 ID。平台时区和 ID 映射的来源可能因实现而异。</li>
     * <li>如果给定或检测到的时区 ID 未知，则使用 {@code GMT} 作为最后的手段。</li>
     * </ul>
     *
     * <p>从 ID 创建的默认 {@code TimeZone} 会被缓存，返回其克隆。返回时，{@code user.timezone} 属性值将被设置为 ID。
     *
     * @return 默认的 {@code TimeZone}
     * @see #setDefault(TimeZone)
     */
    public static TimeZone getDefault() {
        return (TimeZone) getDefaultRef().clone();
    }

    /**
     * 返回默认时区对象的引用。此方法不会创建克隆。
     */
    static TimeZone getDefaultRef() {
        TimeZone defaultZone = defaultTimeZone;
        if (defaultZone == null) {
            // 需要初始化默认时区。
            defaultZone = setDefaultZone();
            assert defaultZone != null;
        }
        // 不在此处克隆。
        return defaultZone;
    }

    private static synchronized TimeZone setDefaultZone() {
        TimeZone tz;
        // 从系统属性中获取时区 ID
        String zoneID = AccessController.doPrivileged(
                new GetPropertyAction("user.timezone"));

        // 如果时区 ID 尚未设置，执行平台到 Java 时区 ID 的映射。
        if (zoneID == null || zoneID.isEmpty()) {
            String javaHome = AccessController.doPrivileged(
                    new GetPropertyAction("java.home"));
            try {
                zoneID = getSystemTimeZoneID(javaHome);
                if (zoneID == null) {
                    zoneID = GMT_ID;
                }
            } catch (NullPointerException e) {
                zoneID = GMT_ID;
            }
        }

        // 获取 zoneID 的时区。但在这里不要回退到 "GMT"。
        tz = getTimeZone(zoneID, false);

        if (tz == null) {
            // 如果给定的时区 ID 在 Java 中未知，尝试获取基于 GMT 偏移量的时区 ID，
            // 也称为自定义时区 ID（例如，"GMT-08:00"）。
            String gmtOffsetID = getSystemGMTOffsetID();
            if (gmtOffsetID != null) {
                zoneID = gmtOffsetID;
            }
            tz = getTimeZone(zoneID, true);
        }
        assert tz != null;

        final String id = zoneID;
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
                public Void run() {
                    System.setProperty("user.timezone", id);
                    return null;
                }
            });

        defaultTimeZone = tz;
        return tz;
    }

    /**
     * 设置由 {@code getDefault} 方法返回的 {@code TimeZone}。{@code zone} 会被缓存。如果 {@code zone} 为 null，
     * 则清除缓存的默认 {@code TimeZone}。此方法不会更改 {@code user.timezone} 属性的值。
     *
     * @param zone 新的默认 {@code TimeZone}，或 null
     * @throws SecurityException 如果安全管理器的 {@code checkPermission}
     *                           拒绝 {@code PropertyPermission("user.timezone",
     *                           "write")}
     * @see #getDefault
     * @see PropertyPermission
     */
    public static void setDefault(TimeZone zone)
    {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new PropertyPermission
                               ("user.timezone", "write"));
        }
        defaultTimeZone = zone;
    }

                /**
     * 如果此时区与另一个时区具有相同的规则和偏移量，则返回 true。
     * 也就是说，如果此区域仅在 ID 上有所不同（如果有的话）。如果其他区域为 null，则返回 false。
     * @param other 要比较的 <code>TimeZone</code> 对象
     * @return 如果其他区域不为 null 且与此区域相同，则返回 true，
     * 除了 ID 可能不同外
     * @since 1.2
     */
    public boolean hasSameRules(TimeZone other) {
        return other != null && getRawOffset() == other.getRawOffset() &&
            useDaylightTime() == other.useDaylightTime();
    }

    /**
     * 创建此 <code>TimeZone</code> 的副本。
     *
     * @return 此 <code>TimeZone</code> 的克隆
     */
    public Object clone()
    {
        try {
            TimeZone other = (TimeZone) super.clone();
            other.ID = ID;
            return other;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 作为时区的 null 常量。
     */
    static final TimeZone NO_TIMEZONE = null;

    // =======================私有成员===============================

    /**
     * 此 <code>TimeZone</code> 的字符串标识符。这是用于从系统表中查找 <code>TimeZone</code>
     * 对象的内部程序标识符，也用于将它们映射到其本地化的显示名称。<code>ID</code> 值在系统
     * 表中是唯一的，但对于动态创建的区域可能不是唯一的。
     * @serial
     */
    private String           ID;
    private static volatile TimeZone defaultTimeZone;

    static final String         GMT_ID        = "GMT";
    private static final int    GMT_ID_LENGTH = 3;

    // 如果没有 AppContext，我们可以引用的静态时区
    private static volatile TimeZone mainAppContextDefault;

    /**
     * 解析自定义时区标识符并返回相应的区域。
     * 此方法不支持 RFC 822 时区格式（例如，+hhmm）。
     *
     * @param id <a href="#CustomID">自定义 ID 形式</a> 的字符串。
     * @return 一个具有给定偏移量且没有夏令时的新创建的时区，或者如果无法解析 id，则返回 null。
     */
    private static final TimeZone parseCustomTimeZone(String id) {
        int length;

        // 如果 id 的长度不够长或 id 不以 "GMT" 开头，则出错。
        if ((length = id.length()) < (GMT_ID_LENGTH + 2) ||
            id.indexOf(GMT_ID) != 0) {
            return null;
        }

        ZoneInfo zi;

        // 首先，我们尝试使用给定的 id 在缓存中找到它。即使 id 未规范化，返回的 ZoneInfo
        // 也应该具有其规范化的 id。
        zi = ZoneInfoFile.getZoneInfo(id);
        if (zi != null) {
            return zi;
        }

        int index = GMT_ID_LENGTH;
        boolean negative = false;
        char c = id.charAt(index++);
        if (c == '-') {
            negative = true;
        } else if (c != '+') {
            return null;
        }

        int hours = 0;
        int num = 0;
        int countDelim = 0;
        int len = 0;
        while (index < length) {
            c = id.charAt(index++);
            if (c == ':') {
                if (countDelim > 0) {
                    return null;
                }
                if (len > 2) {
                    return null;
                }
                hours = num;
                countDelim++;
                num = 0;
                len = 0;
                continue;
            }
            if (c < '0' || c > '9') {
                return null;
            }
            num = num * 10 + (c - '0');
            len++;
        }
        if (index != length) {
            return null;
        }
        if (countDelim == 0) {
            if (len <= 2) {
                hours = num;
                num = 0;
            } else {
                hours = num / 100;
                num %= 100;
            }
        } else {
            if (len != 2) {
                return null;
            }
        }
        if (hours > 23 || num > 59) {
            return null;
        }
        int gmtOffset =  (hours * 60 + num) * 60 * 1000;

        if (gmtOffset == 0) {
            zi = ZoneInfoFile.getZoneInfo(GMT_ID);
            if (negative) {
                zi.setID("GMT-00:00");
            } else {
                zi.setID("GMT+00:00");
            }
        } else {
            zi = ZoneInfoFile.getCustomTimeZone(id, negative ? -gmtOffset : gmtOffset);
        }
        return zi;
    }
}
