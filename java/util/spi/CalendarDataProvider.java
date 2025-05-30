/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Calendar;
import java.util.Locale;

/**
 * 一个抽象类，用于提供依赖于区域设置的 {@link
 * Calendar} 参数。
 *
 * @author Masayoshi Okutsu
 * @since 1.8
 * @see CalendarNameProvider
 */
public abstract class CalendarDataProvider extends LocaleServiceProvider {

    /**
     * 唯一的构造函数。（通常由子类构造函数隐式调用。）
     */
    protected CalendarDataProvider() {
    }

    /**
     * 返回给定 {@code locale} 的一周的第一天。此信息由 {@link Calendar} 用于支持与周相关的日历字段的操作。
     *
     * @param locale
     *        所需的区域设置
     * @return 一周的第一天；可能是 {@link Calendar#SUNDAY} ..
     *         {@link Calendar#SATURDAY}，
     *         或者如果 {@code locale} 没有可用的值则为 0
     * @throws NullPointerException
     *         如果 {@code locale} 为 {@code null}。
     * @see java.util.Calendar#getFirstDayOfWeek()
     * @see <a href="../Calendar.html#first_week">第一周</a>
     */
    public abstract int getFirstDayOfWeek(Locale locale);

    /**
     * 返回一年中第一周所需的最小天数。此信息由 {@link Calendar} 用于确定一年中的第一周。请参阅 <a
     * href="../Calendar.html#first_week"> {@code Calendar} 如何确定第一周</a> 的描述。
     *
     * @param locale
     *        所需的区域设置
     * @return 第一周的最小天数，
     *         或者如果 {@code locale} 没有可用的值则为 0
     * @throws NullPointerException
     *         如果 {@code locale} 为 {@code null}。
     * @see java.util.Calendar#getMinimalDaysInFirstWeek()
     */
    public abstract int getMinimalDaysInFirstWeek(Locale locale);
}
