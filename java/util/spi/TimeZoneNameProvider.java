/*
 * 版权所有 (c) 2005, 2012, Oracle 和/或其附属公司。保留所有权利。
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

package java.util.spi;

import java.util.Locale;

/**
 * 一个抽象类，用于为 {@link java.util.TimeZone TimeZone} 类提供本地化的时间区域名称。
 * 该类的实现所提供的本地化时间区域名称也是
 * {@link java.text.DateFormatSymbols#getZoneStrings()
 * DateFormatSymbols.getZoneStrings()} 方法的来源。
 *
 * @since        1.6
 */
public abstract class TimeZoneNameProvider extends LocaleServiceProvider {

    /**
     * 唯一的构造函数。通常由子类构造函数隐式调用。
     */
    protected TimeZoneNameProvider() {
    }

    /**
     * 返回一个适合在指定区域设置中向用户显示的时间区域ID的名称。给定的时间区域ID是"GMT"或使用"Zone"条目定义的名称之一
     * 在 "tz 数据库" 中，这是一个位于
     * <a href="ftp://elsie.nci.nih.gov/pub/">ftp://elsie.nci.nih.gov/pub/</a> 的公共领域时间区域数据库。
     * 该数据库的数据包含在一个文件中，文件名以 "tzdata" 开头，数据格式的规范是 zic.8
     * 手册页的一部分，该手册页包含在一个文件中，文件名以 "tzcode" 开头。
     * <p>
     * 如果 <code>daylight</code> 为 true，即使指定的时间区域在过去没有实行夏令时，该方法也应返回一个适合夏令时的名称。
     *
     * @param ID 一个时间区域ID字符串
     * @param daylight 如果为 true，返回夏令时名称。
     * @param style 要么是 {@link java.util.TimeZone#LONG TimeZone.LONG} 要么是
     *    {@link java.util.TimeZone#SHORT TimeZone.SHORT}
     * @param locale 所需的区域设置
     * @return 给定时间区域在给定区域设置中的可读名称，如果不可用则返回 null。
     * @exception IllegalArgumentException 如果 <code>style</code> 无效，
     *     或 <code>locale</code> 不是 {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @exception NullPointerException 如果 <code>ID</code> 或 <code>locale</code>
     *     为 null
     * @see java.util.TimeZone#getDisplayName(boolean, int, java.util.Locale)
     */
    public abstract String getDisplayName(String ID, boolean daylight, int style, Locale locale);

    /**
     * 返回一个适合在指定区域设置中向用户显示的给定时间区域 {@code ID} 的通用名称。通用时间区域名称不区分标准时间和夏令时。
     * 例如，"PT" 是时间区域ID {@code America/Los_Angeles} 的简短通用名称，而其简短的标准时间和夏令时名称分别是 "PST" 和 "PDT"。
     * 有关有效的时间区域ID，请参阅
     * {@link #getDisplayName(String, boolean, int, Locale) getDisplayName}。
     *
     * <p>此方法的默认实现返回 {@code null}。
     *
     * @param ID 一个时间区域ID字符串
     * @param style 要么是 {@link java.util.TimeZone#LONG TimeZone.LONG} 要么是
     *    {@link java.util.TimeZone#SHORT TimeZone.SHORT}
     * @param locale 所需的区域设置
     * @return 给定时间区域在给定区域设置中的可读通用名称，如果不可用则返回 {@code null}。
     * @exception IllegalArgumentException 如果 <code>style</code> 无效，
     *     或 <code>locale</code> 不是 {@link LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @exception NullPointerException 如果 <code>ID</code> 或 <code>locale</code>
     *     为 {@code null}
     * @since 1.8
     */
    public String getGenericDisplayName(String ID, int style, Locale locale) {
        return null;
    }
}
