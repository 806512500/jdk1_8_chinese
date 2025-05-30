
/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Copyright (c) 2011-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package java.time.format;

import static java.time.temporal.ChronoField.AMPM_OF_DAY;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.ERA;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;

import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.chrono.JapaneseChronology;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import sun.util.locale.provider.CalendarDataUtility;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;

/**
 * 提供日期时间字段的文本形式。
 *
 * @implSpec
 * 实现必须是线程安全的。
 * 实现应缓存文本信息。
 *
 * @since 1.8
 */
class DateTimeTextProvider {

    /** 缓存。 */
    private static final ConcurrentMap<Entry<TemporalField, Locale>, Object> CACHE = new ConcurrentHashMap<>(16, 0.75f, 2);
    /** 比较器。 */
    private static final Comparator<Entry<String, Long>> COMPARATOR = new Comparator<Entry<String, Long>>() {
        @Override
        public int compare(Entry<String, Long> obj1, Entry<String, Long> obj2) {
            return obj2.getKey().length() - obj1.getKey().length();  // 最长到最短
        }
    };

    DateTimeTextProvider() {}

    /**
     * 获取文本提供者。
     *
     * @return 提供者，不为空
     */
    static DateTimeTextProvider getInstance() {
        return new DateTimeTextProvider();
    }

    /**
     * 获取指定字段、区域设置和样式的文本，用于格式化。
     * <p>
     * 返回与值关联的文本。
     * 如果没有适用的文本，或者文本是值的数字表示形式，则应返回 null。
     *
     * @param field  要获取文本的字段，不为空
     * @param value  要获取文本的字段值，不为空
     * @param style  要获取文本的样式，不为空
     * @param locale  要获取文本的区域设置，不为空
     * @return 字段值的文本，如果没有找到文本则返回 null
     */
    public String getText(TemporalField field, long value, TextStyle style, Locale locale) {
        Object store = findStore(field, locale);
        if (store instanceof LocaleStore) {
            return ((LocaleStore) store).getText(value, style);
        }
        return null;
    }

    /**
     * 获取指定历法、字段、区域设置和样式的文本，用于格式化。
     * <p>
     * 返回与值关联的文本。
     * 如果没有适用的文本，或者文本是值的数字表示形式，则应返回 null。
     *
     * @param chrono  要获取文本的历法，不为空
     * @param field  要获取文本的字段，不为空
     * @param value  要获取文本的字段值，不为空
     * @param style  要获取文本的样式，不为空
     * @param locale  要获取文本的区域设置，不为空
     * @return 字段值的文本，如果没有找到文本则返回 null
     */
    public String getText(Chronology chrono, TemporalField field, long value,
                                    TextStyle style, Locale locale) {
        if (chrono == IsoChronology.INSTANCE
                || !(field instanceof ChronoField)) {
            return getText(field, value, style, locale);
        }

        int fieldIndex;
        int fieldValue;
        if (field == ERA) {
            fieldIndex = Calendar.ERA;
            if (chrono == JapaneseChronology.INSTANCE) {
                if (value == -999) {
                    fieldValue = 0;
                } else {
                    fieldValue = (int) value + 2;
                }
            } else {
                fieldValue = (int) value;
            }
        } else if (field == MONTH_OF_YEAR) {
            fieldIndex = Calendar.MONTH;
            fieldValue = (int) value - 1;
        } else if (field == DAY_OF_WEEK) {
            fieldIndex = Calendar.DAY_OF_WEEK;
            fieldValue = (int) value + 1;
            if (fieldValue > 7) {
                fieldValue = Calendar.SUNDAY;
            }
        } else if (field == AMPM_OF_DAY) {
            fieldIndex = Calendar.AM_PM;
            fieldValue = (int) value;
        } else {
            return null;
        }
        return CalendarDataUtility.retrieveJavaTimeFieldValueName(
                chrono.getCalendarType(), fieldIndex, fieldValue, style.toCalendarStyle(), locale);
    }

    /**
     * 获取指定字段、区域设置和样式的文本到字段的迭代器，用于解析。
     * <p>
     * 迭代器必须按从最长文本到最短文本的顺序返回。
     * <p>
     * 如果没有适用的可解析文本，或者文本是值的数字表示形式，则应返回 null。
     * 只有当该字段-样式-区域设置组合的所有值都是唯一的时，文本才能被解析。
     *
     * @param field  要获取文本的字段，不为空
     * @param style  要获取文本的样式，null 表示所有可解析的文本
     * @param locale  要获取文本的区域设置，不为空
     * @return 按从最长文本到最短文本顺序排列的文本到字段对的迭代器，
     *  如果字段或样式不可解析则返回 null
     */
    public Iterator<Entry<String, Long>> getTextIterator(TemporalField field, TextStyle style, Locale locale) {
        Object store = findStore(field, locale);
        if (store instanceof LocaleStore) {
            return ((LocaleStore) store).getTextIterator(style);
        }
        return null;
    }

    /**
     * 获取指定历法、字段、区域设置和样式的文本到字段的迭代器，用于解析。
     * <p>
     * 迭代器必须按从最长文本到最短文本的顺序返回。
     * <p>
     * 如果没有适用的可解析文本，或者文本是值的数字表示形式，则应返回 null。
     * 只有当该字段-样式-区域设置组合的所有值都是唯一的时，文本才能被解析。
     *
     * @param chrono  要获取文本的历法，不为空
     * @param field  要获取文本的字段，不为空
     * @param style  要获取文本的样式，null 表示所有可解析的文本
     * @param locale  要获取文本的区域设置，不为空
     * @return 按从最长文本到最短文本顺序排列的文本到字段对的迭代器，
     *  如果字段或样式不可解析则返回 null
     */
    public Iterator<Entry<String, Long>> getTextIterator(Chronology chrono, TemporalField field,
                                                         TextStyle style, Locale locale) {
        if (chrono == IsoChronology.INSTANCE
                || !(field instanceof ChronoField)) {
            return getTextIterator(field, style, locale);
        }

        int fieldIndex;
        switch ((ChronoField)field) {
        case ERA:
            fieldIndex = Calendar.ERA;
            break;
        case MONTH_OF_YEAR:
            fieldIndex = Calendar.MONTH;
            break;
        case DAY_OF_WEEK:
            fieldIndex = Calendar.DAY_OF_WEEK;
            break;
        case AMPM_OF_DAY:
            fieldIndex = Calendar.AM_PM;
            break;
        default:
            return null;
        }

        int calendarStyle = (style == null) ? Calendar.ALL_STYLES : style.toCalendarStyle();
        Map<String, Integer> map = CalendarDataUtility.retrieveJavaTimeFieldValueNames(
                chrono.getCalendarType(), fieldIndex, calendarStyle, locale);
        if (map == null) {
            return null;
        }
        List<Entry<String, Long>> list = new ArrayList<>(map.size());
        switch (fieldIndex) {
        case Calendar.ERA:
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                int era = entry.getValue();
                if (chrono == JapaneseChronology.INSTANCE) {
                    if (era == 0) {
                        era = -999;
                    } else {
                        era -= 2;
                    }
                }
                list.add(createEntry(entry.getKey(), (long)era));
            }
            break;
        case Calendar.MONTH:
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                list.add(createEntry(entry.getKey(), (long)(entry.getValue() + 1)));
            }
            break;
        case Calendar.DAY_OF_WEEK:
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                list.add(createEntry(entry.getKey(), (long)toWeekDay(entry.getValue())));
            }
            break;
        default:
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                list.add(createEntry(entry.getKey(), (long)entry.getValue()));
            }
            break;
        }
        return list.iterator();
    }

    private Object findStore(TemporalField field, Locale locale) {
        Entry<TemporalField, Locale> key = createEntry(field, locale);
        Object store = CACHE.get(key);
        if (store == null) {
            store = createStore(field, locale);
            CACHE.putIfAbsent(key, store);
            store = CACHE.get(key);
        }
        return store;
    }

    private static int toWeekDay(int calWeekDay) {
        if (calWeekDay == Calendar.SUNDAY) {
            return 7;
        } else {
            return calWeekDay - 1;
        }
    }

    private Object createStore(TemporalField field, Locale locale) {
        Map<TextStyle, Map<Long, String>> styleMap = new HashMap<>();
        if (field == ERA) {
            for (TextStyle textStyle : TextStyle.values()) {
                if (textStyle.isStandalone()) {
                    // 单独使用不适用于纪元名称。
                    continue;
                }
                Map<String, Integer> displayNames = CalendarDataUtility.retrieveJavaTimeFieldValueNames(
                        "gregory", Calendar.ERA, textStyle.toCalendarStyle(), locale);
                if (displayNames != null) {
                    Map<Long, String> map = new HashMap<>();
                    for (Entry<String, Integer> entry : displayNames.entrySet()) {
                        map.put((long) entry.getValue(), entry.getKey());
                    }
                    if (!map.isEmpty()) {
                        styleMap.put(textStyle, map);
                    }
                }
            }
            return new LocaleStore(styleMap);
        }

        if (field == MONTH_OF_YEAR) {
            for (TextStyle textStyle : TextStyle.values()) {
                Map<String, Integer> displayNames = CalendarDataUtility.retrieveJavaTimeFieldValueNames(
                        "gregory", Calendar.MONTH, textStyle.toCalendarStyle(), locale);
                Map<Long, String> map = new HashMap<>();
                if (displayNames != null) {
                    for (Entry<String, Integer> entry : displayNames.entrySet()) {
                        map.put((long) (entry.getValue() + 1), entry.getKey());
                    }

                } else {
                    // 窄名称可能有重复名称，例如 "J" 用于 January, Jun, July。
                    // 在这种情况下，逐个获取名称。
                    for (int month = Calendar.JANUARY; month <= Calendar.DECEMBER; month++) {
                        String name;
                        name = CalendarDataUtility.retrieveJavaTimeFieldValueName(
                                "gregory", Calendar.MONTH, month, textStyle.toCalendarStyle(), locale);
                        if (name == null) {
                            break;
                        }
                        map.put((long) (month + 1), name);
                    }
                }
                if (!map.isEmpty()) {
                    styleMap.put(textStyle, map);
                }
            }
            return new LocaleStore(styleMap);
        }


                    if (field == DAY_OF_WEEK) {
            for (TextStyle textStyle : TextStyle.values()) {
                Map<String, Integer> displayNames = CalendarDataUtility.retrieveJavaTimeFieldValueNames(
                        "gregory", Calendar.DAY_OF_WEEK, textStyle.toCalendarStyle(), locale);
                Map<Long, String> map = new HashMap<>();
                if (displayNames != null) {
                    for (Entry<String, Integer> entry : displayNames.entrySet()) {
                        map.put((long)toWeekDay(entry.getValue()), entry.getKey());
                    }

                } else {
                    // 窄名称可能有重复名称，例如“S”表示星期日和星期六。
                    // 在这种情况下，逐个获取名称。
                    for (int wday = Calendar.SUNDAY; wday <= Calendar.SATURDAY; wday++) {
                        String name;
                        name = CalendarDataUtility.retrieveJavaTimeFieldValueName(
                            "gregory", Calendar.DAY_OF_WEEK, wday, textStyle.toCalendarStyle(), locale);
                        if (name == null) {
                            break;
                        }
                        map.put((long)toWeekDay(wday), name);
                    }
                }
                if (!map.isEmpty()) {
                    styleMap.put(textStyle, map);
                }
            }
            return new LocaleStore(styleMap);
        }

        if (field == AMPM_OF_DAY) {
            for (TextStyle textStyle : TextStyle.values()) {
                if (textStyle.isStandalone()) {
                    // 独立形式不适用于AM/PM。
                    continue;
                }
                Map<String, Integer> displayNames = CalendarDataUtility.retrieveJavaTimeFieldValueNames(
                        "gregory", Calendar.AM_PM, textStyle.toCalendarStyle(), locale);
                if (displayNames != null) {
                    Map<Long, String> map = new HashMap<>();
                    for (Entry<String, Integer> entry : displayNames.entrySet()) {
                        map.put((long) entry.getValue(), entry.getKey());
                    }
                    if (!map.isEmpty()) {
                        styleMap.put(textStyle, map);
                    }
                }
            }
            return new LocaleStore(styleMap);
        }

        if (field == IsoFields.QUARTER_OF_YEAR) {
            // 键的顺序必须与TextStyle.values()的顺序相对应。
            final String[] keys = {
                "QuarterNames",
                "standalone.QuarterNames",
                "QuarterAbbreviations",
                "standalone.QuarterAbbreviations",
                "QuarterNarrows",
                "standalone.QuarterNarrows",
            };
            for (int i = 0; i < keys.length; i++) {
                String[] names = getLocalizedResource(keys[i], locale);
                if (names != null) {
                    Map<Long, String> map = new HashMap<>();
                    for (int q = 0; q < names.length; q++) {
                        map.put((long) (q + 1), names[q]);
                    }
                    styleMap.put(TextStyle.values()[i], map);
                }
            }
            return new LocaleStore(styleMap);
        }

        return "";  // null marker for map
    }

    /**
     * 辅助方法，用于创建不可变条目。
     *
     * @param text  文本，不为null
     * @param field  字段，不为null
     * @return 条目，不为null
     */
    private static <A, B> Entry<A, B> createEntry(A text, B field) {
        return new SimpleImmutableEntry<>(text, field);
    }

    /**
     * 返回给定键和区域设置的本地化资源，如果不存在本地化资源，则返回null。
     *
     * @param key  本地化资源的键，不为null
     * @param locale  区域设置，不为null
     * @return 本地化资源，如果不可用则返回null
     * @throws NullPointerException 如果键或区域设置为null
     */
    @SuppressWarnings("unchecked")
    static <T> T getLocalizedResource(String key, Locale locale) {
        LocaleResources lr = LocaleProviderAdapter.getResourceBundleBased()
                                    .getLocaleResources(locale);
        ResourceBundle rb = lr.getJavaTimeFormatData();
        return rb.containsKey(key) ? (T) rb.getObject(key) : null;
    }

    /**
     * 存储单个区域设置的文本。
     * <p>
     * 一些字段具有文本表示形式，例如星期几或一年中的月份。
     * 这些文本表示形式可以在此类中捕获，用于打印和解析。
     * <p>
     * 此类是不可变的和线程安全的。
     */
    static final class LocaleStore {
        /**
         * 值到文本的映射。
         */
        private final Map<TextStyle, Map<Long, String>> valueTextMap;
        /**
         * 可解析的数据。
         */
        private final Map<TextStyle, List<Entry<String, Long>>> parsable;

        /**
         * 构造函数。
         *
         * @param valueTextMap  要存储的值到文本的映射，分配且不改变，不为null
         */
        LocaleStore(Map<TextStyle, Map<Long, String>> valueTextMap) {
            this.valueTextMap = valueTextMap;
            Map<TextStyle, List<Entry<String, Long>>> map = new HashMap<>();
            List<Entry<String, Long>> allList = new ArrayList<>();
            for (Map.Entry<TextStyle, Map<Long, String>> vtmEntry : valueTextMap.entrySet()) {
                Map<String, Entry<String, Long>> reverse = new HashMap<>();
                for (Map.Entry<Long, String> entry : vtmEntry.getValue().entrySet()) {
                    if (reverse.put(entry.getValue(), createEntry(entry.getValue(), entry.getKey())) != null) {
                        // TODO: BUG: this has no effect
                        continue;  // 不可解析，尝试下一个样式
                    }
                }
                List<Entry<String, Long>> list = new ArrayList<>(reverse.values());
                Collections.sort(list, COMPARATOR);
                map.put(vtmEntry.getKey(), list);
                allList.addAll(list);
                map.put(null, allList);
            }
            Collections.sort(allList, COMPARATOR);
            this.parsable = map;
        }

        /**
         * 获取指定字段值、区域设置和样式的文本，用于打印。
         *
         * @param value  要获取文本的值，不为null
         * @param style  要获取文本的样式，不为null
         * @return 字段值的文本，如果未找到文本则返回null
         */
        String getText(long value, TextStyle style) {
            Map<Long, String> map = valueTextMap.get(style);
            return map != null ? map.get(value) : null;
        }

        /**
         * 获取指定样式的文本到字段的迭代器，用于解析。
         * <p>
         * 迭代器必须按从最长文本到最短文本的顺序返回。
         *
         * @param style  要获取文本的样式，null表示所有可解析文本
         * @return 文本到字段对的迭代器，按从最长文本到最短文本的顺序，如果样式不可解析则返回null
         */
        Iterator<Entry<String, Long>> getTextIterator(TextStyle style) {
            List<Entry<String, Long>> list = parsable.get(style);
            return list != null ? list.iterator() : null;
        }
    }
}
