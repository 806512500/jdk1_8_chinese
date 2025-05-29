/*
 * 版权所有 (c) 2005, Oracle 和/或其附属公司。保留所有权利。
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

package java.text.spi;

import java.text.DateFormat;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

/**
 * 一个抽象类，为服务提供者提供
 * {@link java.text.DateFormat DateFormat} 类的具体实现。
 *
 * @since        1.6
 */
public abstract class DateFormatProvider extends LocaleServiceProvider {

    /**
     * 唯一的构造函数。 （通常由子类构造函数隐式调用。）
     */
    protected DateFormatProvider() {
    }

    /**
     * 返回一个新的 <code>DateFormat</code> 实例，该实例以指定的格式样式和区域设置格式化时间。
     * @param style 给定的格式样式。可以是
     *     {@link java.text.DateFormat#SHORT DateFormat.SHORT}，
     *     {@link java.text.DateFormat#MEDIUM DateFormat.MEDIUM}，
     *     {@link java.text.DateFormat#LONG DateFormat.LONG}，或
     *     {@link java.text.DateFormat#FULL DateFormat.FULL}。
     * @param locale 所需的区域设置。
     * @exception IllegalArgumentException 如果 <code>style</code> 无效，
     *     或者 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @return 一个时间格式化器。
     * @see java.text.DateFormat#getTimeInstance(int, java.util.Locale)
     */
    public abstract DateFormat getTimeInstance(int style, Locale locale);

    /**
     * 返回一个新的 <code>DateFormat</code> 实例，该实例以指定的格式样式和区域设置格式化日期。
     * @param style 给定的格式样式。可以是
     *     {@link java.text.DateFormat#SHORT DateFormat.SHORT}，
     *     {@link java.text.DateFormat#MEDIUM DateFormat.MEDIUM}，
     *     {@link java.text.DateFormat#LONG DateFormat.LONG}，或
     *     {@link java.text.DateFormat#FULL DateFormat.FULL}。
     * @param locale 所需的区域设置。
     * @exception IllegalArgumentException 如果 <code>style</code> 无效，
     *     或者 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @return 一个日期格式化器。
     * @see java.text.DateFormat#getDateInstance(int, java.util.Locale)
     */
    public abstract DateFormat getDateInstance(int style, Locale locale);

    /**
     * 返回一个新的 <code>DateFormat</code> 实例，该实例以指定的格式样式和区域设置格式化日期和时间。
     * @param dateStyle 给定的日期格式样式。可以是
     *     {@link java.text.DateFormat#SHORT DateFormat.SHORT}，
     *     {@link java.text.DateFormat#MEDIUM DateFormat.MEDIUM}，
     *     {@link java.text.DateFormat#LONG DateFormat.LONG}，或
     *     {@link java.text.DateFormat#FULL DateFormat.FULL}。
     * @param timeStyle 给定的时间格式样式。可以是
     *     {@link java.text.DateFormat#SHORT DateFormat.SHORT}，
     *     {@link java.text.DateFormat#MEDIUM DateFormat.MEDIUM}，
     *     {@link java.text.DateFormat#LONG DateFormat.LONG}，或
     *     {@link java.text.DateFormat#FULL DateFormat.FULL}。
     * @param locale 所需的区域设置。
     * @exception IllegalArgumentException 如果 <code>dateStyle</code> 或
     *     <code>timeStyle</code> 无效，
     *     或者 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @return 一个日期/时间格式化器。
     * @see java.text.DateFormat#getDateTimeInstance(int, int, java.util.Locale)
     */
    public abstract DateFormat
        getDateTimeInstance(int dateStyle, int timeStyle, Locale locale);
}
