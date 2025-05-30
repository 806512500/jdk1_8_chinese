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
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
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

import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 用于日期和时间解析的上下文对象。
 * <p>
 * 该类表示解析的当前状态。
 * 它具有存储和检索解析值以及管理可选段的能力。
 * 它还为解析方法提供关键信息。
 * <p>
 * 一旦解析完成，使用 {@link #toUnresolved()} 获取未解析的结果数据。
 * 使用 {@link #toResolved()} 获取解析后的结果。
 *
 * @implSpec
 * 该类是一个可变上下文，旨在单线程使用。
 * 在标准解析中使用该类是线程安全的，因为每次解析都会自动创建该类的新实例，解析是单线程的。
 *
 * @since 1.8
 */
final class DateTimeParseContext {

    /**
     * 格式化器，不为空。
     */
    private DateTimeFormatter formatter;
    /**
     * 是否使用区分大小写的方式解析。
     */
    private boolean caseSensitive = true;
    /**
     * 是否使用严格规则解析。
     */
    private boolean strict = true;
    /**
     * 解析数据的列表。
     */
    private final ArrayList<Parsed> parsed = new ArrayList<>();
    /**
     * 用于在历法更改时通知的 Consumer<Chronology> 列表。
     */
    private ArrayList<Consumer<Chronology>> chronoListeners = null;

    /**
     * 创建上下文的新实例。
     *
     * @param formatter  控制解析的格式化器，不为空
     */
    DateTimeParseContext(DateTimeFormatter formatter) {
        super();
        this.formatter = formatter;
        parsed.add(new Parsed());
    }

    /**
     * 创建此上下文的副本。
     * 保留区分大小写和严格标志。
     */
    DateTimeParseContext copy() {
        DateTimeParseContext newContext = new DateTimeParseContext(formatter);
        newContext.caseSensitive = caseSensitive;
        newContext.strict = strict;
        return newContext;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取区域设置。
     * <p>
     * 该区域设置用于控制解析中的本地化，除非本地化由 DecimalStyle 控制。
     *
     * @return 区域设置，不为空
     */
    Locale getLocale() {
        return formatter.getLocale();
    }

    /**
     * 获取 DecimalStyle。
     * <p>
     * DecimalStyle 控制数字解析。
     *
     * @return DecimalStyle，不为空
     */
    DecimalStyle getDecimalStyle() {
        return formatter.getDecimalStyle();
    }

    /**
     * 获取解析期间的有效历法。
     *
     * @return 有效解析历法，不为空
     */
    Chronology getEffectiveChronology() {
        Chronology chrono = currentParsed().chrono;
        if (chrono == null) {
            chrono = formatter.getChronology();
            if (chrono == null) {
                chrono = IsoChronology.INSTANCE;
            }
        }
        return chrono;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查解析是否区分大小写。
     *
     * @return 如果解析区分大小写则返回 true，否则返回 false
     */
    boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * 设置解析是否区分大小写。
     *
     * @param caseSensitive  从现在起更改解析为区分大小写或不区分大小写
     */
    void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    //-----------------------------------------------------------------------
    /**
     * 比较两个 {@code CharSequence} 实例的辅助方法。
     * 使用 {@link #isCaseSensitive()}。
     *
     * @param cs1  第一个字符序列，不为空
     * @param offset1  第一个序列的偏移量，有效
     * @param cs2  第二个字符序列，不为空
     * @param offset2  第二个序列的偏移量，有效
     * @param length  要检查的长度，有效
     * @return 如果相等则返回 true
     */
    boolean subSequenceEquals(CharSequence cs1, int offset1, CharSequence cs2, int offset2, int length) {
        if (offset1 + length > cs1.length() || offset2 + length > cs2.length()) {
            return false;
        }
        if (isCaseSensitive()) {
            for (int i = 0; i < length; i++) {
                char ch1 = cs1.charAt(offset1 + i);
                char ch2 = cs2.charAt(offset2 + i);
                if (ch1 != ch2) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < length; i++) {
                char ch1 = cs1.charAt(offset1 + i);
                char ch2 = cs2.charAt(offset2 + i);
                if (ch1 != ch2 && Character.toUpperCase(ch1) != Character.toUpperCase(ch2) &&
                        Character.toLowerCase(ch1) != Character.toLowerCase(ch2)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 比较两个 {@code char} 的辅助方法。
     * 使用 {@link #isCaseSensitive()}。
     *
     * @param ch1  第一个字符
     * @param ch2  第二个字符
     * @return 如果相等则返回 true
     */
    boolean charEquals(char ch1, char ch2) {
        if (isCaseSensitive()) {
            return ch1 == ch2;
        }
        return charEqualsIgnoreCase(ch1, ch2);
    }

    /**
     * 比较两个字符时忽略大小写。
     *
     * @param c1  第一个字符
     * @param c2  第二个字符
     * @return 如果相等则返回 true
     */
    static boolean charEqualsIgnoreCase(char c1, char c2) {
        return c1 == c2 ||
                Character.toUpperCase(c1) == Character.toUpperCase(c2) ||
                Character.toLowerCase(c1) == Character.toLowerCase(c2);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查解析是否严格。
     * <p>
     * 严格解析要求文本和符号样式的精确匹配。
     *
     * @return 如果解析严格则返回 true，否则返回 false
     */
    boolean isStrict() {
        return strict;
    }

    /**
     * 设置解析是否严格。
     *
     * @param strict  从现在起更改解析为严格或宽松
     */
    void setStrict(boolean strict) {
        this.strict = strict;
    }

    //-----------------------------------------------------------------------
    /**
     * 开始解析输入的可选段。
     */
    void startOptional() {
        parsed.add(currentParsed().copy());
    }

    /**
     * 结束解析输入的可选段。
     *
     * @param successful  可选段是否成功解析
     */
    void endOptional(boolean successful) {
        if (successful) {
            parsed.remove(parsed.size() - 2);
        } else {
            parsed.remove(parsed.size() - 1);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 获取当前活动的临时对象。
     *
     * @return 当前临时对象，不为空
     */
    private Parsed currentParsed() {
        return parsed.get(parsed.size() - 1);
    }

    /**
     * 获取解析的未解析结果。
     *
     * @return 解析的结果，不为空
     */
    Parsed toUnresolved() {
        return currentParsed();
    }

    /**
     * 获取解析的解析结果。
     *
     * @return 解析的结果，不为空
     */
    TemporalAccessor toResolved(ResolverStyle resolverStyle, Set<TemporalField> resolverFields) {
        Parsed parsed = currentParsed();
        parsed.chrono = getEffectiveChronology();
        parsed.zone = (parsed.zone != null ? parsed.zone : formatter.getZone());
        return parsed.resolve(resolverStyle, resolverFields);
    }


    //-----------------------------------------------------------------------
    /**
     * 获取指定字段的第一个解析值。
     * <p>
     * 该方法搜索解析结果，返回指定字段的第一个找到的值。
     * 不尝试推导值。
     * 字段可能具有超出范围的值。
     * 例如，月中的天数可能设置为 50，或小时数设置为 1000。
     *
     * @param field  从映射中查询的字段，null 返回 null
     * @return 映射到指定字段的值，如果字段未解析则返回 null
     */
    Long getParsed(TemporalField field) {
        return currentParsed().fieldValues.get(field);
    }

    /**
     * 存储解析的字段。
     * <p>
     * 该方法存储已解析的字段-值对。
     * 存储的值可能超出字段的范围 - 不进行任何检查。
     *
     * @param field  要在字段-值映射中设置的字段，不为空
     * @param value  要在字段-值映射中设置的值
     * @param errorPos  要解析的字段的位置
     * @param successPos  要解析的字段之后的位置
     * @return 新的位置
     */
    int setParsedField(TemporalField field, long value, int errorPos, int successPos) {
        Objects.requireNonNull(field, "field");
        Long old = currentParsed().fieldValues.put(field, value);
        return (old != null && old.longValue() != value) ? ~errorPos : successPos;
    }

    /**
     * 存储解析的历法。
     * <p>
     * 该方法存储已解析的历法。
     * 除确保不为空外，不进行任何验证。
     * <p>
     * 监听器列表被复制并清空，以便每个监听器只被调用一次。
     * 监听器可以在需要时再次添加自己，以便在将来更改时收到通知。
     *
     * @param chrono  解析的历法，不为空
     */
    void setParsed(Chronology chrono) {
        Objects.requireNonNull(chrono, "chrono");
        currentParsed().chrono = chrono;
        if (chronoListeners != null && !chronoListeners.isEmpty()) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Consumer<Chronology>[] tmp = new Consumer[1];
            Consumer<Chronology>[] listeners = chronoListeners.toArray(tmp);
            chronoListeners.clear();
            for (Consumer<Chronology> l : listeners) {
                l.accept(chrono);
            }
        }
    }

    /**
     * 将 Consumer<Chronology> 添加到在历法更改时通知的监听器列表中。
     * @param listener  当历法更改时调用的 Consumer<Chronology>
     */
    void addChronoChangedListener(Consumer<Chronology> listener) {
        if (chronoListeners == null) {
            chronoListeners = new ArrayList<Consumer<Chronology>>();
        }
        chronoListeners.add(listener);
    }

    /**
     * 存储解析的时区。
     * <p>
     * 该方法存储已解析的时区。
     * 除确保不为空外，不进行任何验证。
     *
     * @param zone  解析的时区，不为空
     */
    void setParsed(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        currentParsed().zone = zone;
    }

    /**
     * 存储解析的闰秒。
     */
    void setParsedLeapSecond() {
        currentParsed().leapSecond = true;
    }

    //-----------------------------------------------------------------------
    /**
     * 返回上下文的字符串版本，用于调试。
     *
     * @return 上下文数据的字符串表示，不为空
     */
    @Override
    public String toString() {
        return currentParsed().toString();
    }

}
