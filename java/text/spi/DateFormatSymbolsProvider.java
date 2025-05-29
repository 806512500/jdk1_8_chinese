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

import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

/**
 * 一个抽象类，用于提供 {@link java.text.DateFormatSymbols DateFormatSymbols} 类的实例。
 *
 * @since        1.6
 */
public abstract class DateFormatSymbolsProvider extends LocaleServiceProvider {

    /**
     * 唯一的构造函数。 （通常由子类构造函数隐式调用。）
     */
    protected DateFormatSymbolsProvider() {
    }

    /**
     * 返回指定区域设置的新的 <code>DateFormatSymbols</code> 实例。
     *
     * @param locale 所需的区域设置
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @return 一个 <code>DateFormatSymbols</code> 实例。
     * @see java.text.DateFormatSymbols#getInstance(java.util.Locale)
     */
    public abstract DateFormatSymbols getInstance(Locale locale);
}
