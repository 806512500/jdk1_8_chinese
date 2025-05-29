/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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

package java.text.spi;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

/**
 * 一个抽象类，为 {@link java.text.NumberFormat NumberFormat} 类提供具体实现的服务提供者。
 *
 * @since        1.6
 */
public abstract class NumberFormatProvider extends LocaleServiceProvider {

    /**
     * 唯一的构造函数。通常由子类构造函数隐式调用。
     */
    protected NumberFormatProvider() {
    }

    /**
     * 返回一个新的 <code>NumberFormat</code> 实例，用于格式化指定区域设置的货币值。
     *
     * @param locale 所需的区域设置。
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @return 一个货币格式化器
     * @see java.text.NumberFormat#getCurrencyInstance(java.util.Locale)
     */
    public abstract NumberFormat getCurrencyInstance(Locale locale);

    /**
     * 返回一个新的 <code>NumberFormat</code> 实例，用于格式化指定区域设置的整数值。
     * 返回的数字格式配置为使用半偶舍入（参见 {@link java.math.RoundingMode#HALF_EVEN HALF_EVEN}）
     * 对浮点数进行舍入，并且仅解析输入字符串的整数部分（参见 {@link
     * java.text.NumberFormat#isParseIntegerOnly isParseIntegerOnly}）。
     *
     * @param locale 所需的区域设置
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @return 一个用于整数值的数字格式
     * @see java.text.NumberFormat#getIntegerInstance(java.util.Locale)
     */
    public abstract NumberFormat getIntegerInstance(Locale locale);

    /**
     * 返回一个新的通用 <code>NumberFormat</code> 实例，用于指定的区域设置。
     *
     * @param locale 所需的区域设置
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @return 一个通用的数字格式化器
     * @see java.text.NumberFormat#getNumberInstance(java.util.Locale)
     */
    public abstract NumberFormat getNumberInstance(Locale locale);

    /**
     * 返回一个新的 <code>NumberFormat</code> 实例，用于格式化指定区域设置的百分比值。
     *
     * @param locale 所需的区域设置
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     * @exception IllegalArgumentException 如果 <code>locale</code> 不是
     *     {@link java.util.spi.LocaleServiceProvider#getAvailableLocales()
     *     getAvailableLocales()} 返回的区域设置之一。
     * @return 一个百分比格式化器
     * @see java.text.NumberFormat#getPercentInstance(java.util.Locale)
     */
    public abstract NumberFormat getPercentInstance(Locale locale);
}
