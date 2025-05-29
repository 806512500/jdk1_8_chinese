/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.util.spi;

import java.util.Calendar;
import java.util.Locale;

/**
 * 一个为提供与地区相关的 {@link
 * Calendar} 参数的服务提供者定义的抽象类。
 *
 * @author Masayoshi Okutsu
 * @since 1.8
 * @see CalendarNameProvider
 */
public abstract class CalendarDataProvider extends LocaleServiceProvider {

    /**
     * 唯一的构造函数。通常由子类构造函数隐式调用。
     */
    protected CalendarDataProvider() {
    }

    /**
     * 返回给定 {@code locale} 的一周的第一天。此信息由 {@link Calendar} 用于支持与周相关的日历字段的操作。
     *
     * @param locale
     *        所需的地区
     * @return 一周的第一天；可能是 {@link Calendar#SUNDAY} ..
     *         {@link Calendar#SATURDAY}，
     *         如果 {@code locale} 没有可用的值，则返回 0
     * @throws NullPointerException
     *         如果 {@code locale} 为 {@code null}。
     * @see java.util.Calendar#getFirstDayOfWeek()
     * @see <a href="../Calendar.html#first_week">第一周</a>
     */
    public abstract int getFirstDayOfWeek(Locale locale);

    /**
     * 返回一年中第一周所需的最少天数。此信息由 {@link Calendar} 用于确定一年中的第一周。请参阅 <a
     * href="../Calendar.html#first_week"> {@code Calendar} 如何确定第一周</a> 的描述。
     *
     * @param locale
     *        所需的地区
     * @return 第一周的最少天数，
     *         如果 {@code locale} 没有可用的值，则返回 0
     * @throws NullPointerException
     *         如果 {@code locale} 为 {@code null}。
     * @see java.util.Calendar#getMinimalDaysInFirstWeek()
     */
    public abstract int getMinimalDaysInFirstWeek(Locale locale);
}
