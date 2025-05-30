/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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

package java.text.spi;

import java.text.DateFormat;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

/**
 * 一个抽象类，为 {@link java.text.DateFormat DateFormat} 类提供具体的实现。
 *
 * @since        1.6
 */
public abstract class DateFormatProvider extends LocaleServiceProvider {

    /**
     * 唯一的构造函数。通常由子类构造函数隐式调用。
     */
    protected DateFormatProvider() {
    }

    /**
     * 返回一个新的 <code>DateFormat</code> 实例，该实例使用指定的格式样式为指定的区域设置格式化时间。
     * @param style 给定的格式样式。可以是以下之一：
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
     * 返回一个新的 <code>DateFormat</code> 实例，该实例使用指定的格式样式为指定的区域设置格式化日期。
     * @param style 给定的格式样式。可以是以下之一：
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
     * 返回一个新的 <code>DateFormat</code> 实例，该实例使用指定的格式样式为指定的区域设置格式化日期和时间。
     * @param dateStyle 给定的日期格式样式。可以是以下之一：
     *     {@link java.text.DateFormat#SHORT DateFormat.SHORT}，
     *     {@link java.text.DateFormat#MEDIUM DateFormat.MEDIUM}，
     *     {@link java.text.DateFormat#LONG DateFormat.LONG}，或
     *     {@link java.text.DateFormat#FULL DateFormat.FULL}。
     * @param timeStyle 给定的时间格式样式。可以是以下之一：
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
