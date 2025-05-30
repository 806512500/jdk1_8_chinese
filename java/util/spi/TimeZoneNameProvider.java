/*
 * Copyright (c) 2005, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.util.spi;

import java.util.Locale;

/**
 * 一个抽象类，为 {@link java.util.TimeZone TimeZone} 类提供本地化的时间区域名称。
 * 从此类的实现中获取的本地化时间区域名称也是
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
     * 返回一个适合在指定区域设置中呈现给用户的时间区域 ID 的名称。给定的时间区域 ID 是 "GMT" 或者是
     * "tz 数据库" 中使用 "Zone" 条目定义的名称之一，这是一个位于
     * <a href="ftp://elsie.nci.nih.gov/pub/">ftp://elsie.nci.nih.gov/pub/</a> 的公共领域时间区域数据库。
     * 该数据库的数据包含在一个文件中，文件名以 "tzdata" 开头，数据格式的规范是 zic.8
     * 手册页的一部分，该手册页包含在一个文件中，文件名以 "tzcode" 开头。
     * <p>
     * 如果 <code>daylight</code> 为 true，则即使指定的时间区域在过去没有实行夏令时，该方法也应返回适合夏令时的名称。
     *
     * @param ID 一个时间区域 ID 字符串
     * @param daylight 如果为 true，返回夏令时名称。
     * @param style {@link java.util.TimeZone#LONG TimeZone.LONG} 或
     *    {@link java.util.TimeZone#SHORT TimeZone.SHORT} 之一
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
     * 返回一个适合在指定区域设置中呈现给用户的时间区域 {@code ID} 的通用名称。通用时间区域名称不区分标准时间和夏令时。
     * 例如，"PT" 是时间区域 ID {@code America/Los_Angeles} 的简短通用名称，而其标准时间和夏令时的简短名称分别是 "PST" 和 "PDT"。
     * 有关有效的时间区域 ID，请参阅
     * {@link #getDisplayName(String, boolean, int, Locale) getDisplayName}。
     *
     * <p>此方法的默认实现返回 {@code null}。
     *
     * @param ID 一个时间区域 ID 字符串
     * @param style {@link java.util.TimeZone#LONG TimeZone.LONG} 或
     *    {@link java.util.TimeZone#SHORT TimeZone.SHORT} 之一
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
