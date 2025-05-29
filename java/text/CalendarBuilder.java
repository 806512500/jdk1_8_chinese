/*
 * 版权所有 (c) 2010, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.text;

import java.util.Calendar;
import static java.util.GregorianCalendar.*;

/**
 * {@code CalendarBuilder} 保存用于设置给定 {@code Calendar} 的日历字段的字段值对。它具有用于支持周年的第
 * {@link Calendar#FIELD_COUNT FIELD_COUNT} 个字段。同时使用 {@code ISO_DAY_OF_WEEK} 来指定 ISO 周日编号中的
 * {@code DAY_OF_WEEK}。
 *
 * <p>{@code CalendarBuilder} 保留字段的伪时间戳语义。{@code CalendarBuilder} 使用一个单个的 int 数组，结合了
 * {@code Calendar} 的 fields[] 和 stamp[]。
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
        // 从最小时间戳到最大时间戳设置字段，以便在 Calendar 中进行字段解析。
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
            // 在宽松模式下稍后调整
            return isoDayOfWeek;
        }
        return isoDayOfWeek == 7 ? SUNDAY : isoDayOfWeek + 1;
    }

    static boolean isValidDayOfWeek(int dayOfWeek) {
        return dayOfWeek > 0 && dayOfWeek <= 7;
    }
}
