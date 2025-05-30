/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.text;

import java.util.Calendar;
import static java.util.GregorianCalendar.*;

/**
 * {@code CalendarBuilder} 用于保存给定 {@code Calendar} 的字段值对。它具有用于支持周年的第 {@link Calendar#FIELD_COUNT FIELD_COUNT} 个字段。此外，使用 {@code ISO_DAY_OF_WEEK} 来指定 ISO 周日编号中的 {@code DAY_OF_WEEK}。
 *
 * <p>{@code CalendarBuilder} 保留字段的伪时间戳语义。{@code CalendarBuilder} 使用一个 int 数组，将 {@code Calendar} 中的 fields[] 和 stamp[] 结合在一起。
 *
 * @author Masayoshi Okutsu
 */
class CalendarBuilder {
    /*
     * 在 java.util.Calendar 中使用的伪时间戳常量
     */
    private static final int UNSET = 0;
    private static final int COMPUTED = 1;
    private static final int MINIMUM_USER_STAMP = 2;

    private static final int MAX_FIELD = FIELD_COUNT + 1;

    public static final int WEEK_YEAR = FIELD_COUNT;
    public static final int ISO_DAY_OF_WEEK = 1000; // 伪字段索引

    // 结合了 stamp[]（下半部分）和 field[]（上半部分）
    private final int[] field;
    private int nextStamp;
    private int maxFieldIndex;

    CalendarBuilder() {
        field = new int[MAX_FIELD * 2];
        nextStamp = MINIMUM_USER_STAMP;
        maxFieldIndex = -1;
    }

    CalendarBuilder set(int index, int value) {
        if (index == ISO_DAY_OF_WEEK) {
            index = DAY_OF_WEEK;
            value = toCalendarDayOfWeek(value);
        }
        field[index] = nextStamp++;
        field[MAX_FIELD + index] = value;
        if (index > maxFieldIndex && index < FIELD_COUNT) {
            maxFieldIndex = index;
        }
        return this;
    }

    CalendarBuilder addYear(int value) {
        field[MAX_FIELD + YEAR] += value;
        field[MAX_FIELD + WEEK_YEAR] += value;
        return this;
    }

    boolean isSet(int index) {
        if (index == ISO_DAY_OF_WEEK) {
            index = DAY_OF_WEEK;
        }
        return field[index] > UNSET;
    }

    CalendarBuilder clear(int index) {
        if (index == ISO_DAY_OF_WEEK) {
            index = DAY_OF_WEEK;
        }
        field[index] = UNSET;
        field[MAX_FIELD + index] = 0;
        return this;
    }

    Calendar establish(Calendar cal) {
        boolean weekDate = isSet(WEEK_YEAR)
                            && field[WEEK_YEAR] > field[YEAR];
        if (weekDate && !cal.isWeekDateSupported()) {
            // 使用 YEAR 代替
            if (!isSet(YEAR)) {
                set(YEAR, field[MAX_FIELD + WEEK_YEAR]);
            }
            weekDate = false;
        }

        cal.clear();
        // 从最小时间戳到最大时间戳设置字段，以便 Calendar 中的字段解析正常工作。
        for (int stamp = MINIMUM_USER_STAMP; stamp < nextStamp; stamp++) {
            for (int index = 0; index <= maxFieldIndex; index++) {
                if (field[index] == stamp) {
                    cal.set(index, field[MAX_FIELD + index]);
                    break;
                }
            }
        }

        if (weekDate) {
            int weekOfYear = isSet(WEEK_OF_YEAR) ? field[MAX_FIELD + WEEK_OF_YEAR] : 1;
            int dayOfWeek = isSet(DAY_OF_WEEK) ?
                                field[MAX_FIELD + DAY_OF_WEEK] : cal.getFirstDayOfWeek();
            if (!isValidDayOfWeek(dayOfWeek) && cal.isLenient()) {
                if (dayOfWeek >= 8) {
                    dayOfWeek--;
                    weekOfYear += dayOfWeek / 7;
                    dayOfWeek = (dayOfWeek % 7) + 1;
                } else {
                    while (dayOfWeek <= 0) {
                        dayOfWeek += 7;
                        weekOfYear--;
                    }
                }
                dayOfWeek = toCalendarDayOfWeek(dayOfWeek);
            }
            cal.setWeekDate(field[MAX_FIELD + WEEK_YEAR], weekOfYear, dayOfWeek);
        }
        return cal;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CalendarBuilder:[");
        for (int i = 0; i < field.length; i++) {
            if (isSet(i)) {
                sb.append(i).append('=').append(field[MAX_FIELD + i]).append(',');
            }
        }
        int lastIndex = sb.length() - 1;
        if (sb.charAt(lastIndex) == ',') {
            sb.setLength(lastIndex);
        }
        sb.append(']');
        return sb.toString();
    }

    static int toISODayOfWeek(int calendarDayOfWeek) {
        return calendarDayOfWeek == SUNDAY ? 7 : calendarDayOfWeek - 1;
    }

    static int toCalendarDayOfWeek(int isoDayOfWeek) {
        if (!isValidDayOfWeek(isoDayOfWeek)) {
            // 为宽松模式调整
            return isoDayOfWeek;
        }
        return isoDayOfWeek == 7 ? SUNDAY : isoDayOfWeek + 1;
    }

    static boolean isValidDayOfWeek(int dayOfWeek) {
        return dayOfWeek > 0 && dayOfWeek <= 7;
    }
}
