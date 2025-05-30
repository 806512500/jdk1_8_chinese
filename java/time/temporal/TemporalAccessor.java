/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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
 * Copyright (c) 2012, Stephen Colebourne & Michael Nascimento Santos
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
package java.time.temporal;

import java.time.DateTimeException;
import java.util.Objects;

/**
 * 定义对时间对象（如日期、时间、偏移量或这些的组合）的只读访问的框架级接口。
 * <p>
 * 这是日期、时间和偏移量对象的基础接口类型。
 * 它由那些可以提供 {@linkplain TemporalField 字段} 或 {@linkplain TemporalQuery 查询} 信息的类实现。
 * <p>
 * 大多数日期和时间信息可以表示为数字。
 * 这些信息使用 {@code TemporalField} 建模，数字使用 {@code long} 存储以处理大值。年、月和日是简单字段的例子，
 * 但它们还包括瞬时和偏移量。有关标准字段集，请参见 {@link ChronoField}。
 * <p>
 * 两部分日期/时间信息不能用数字表示，即 {@linkplain java.time.chrono.Chronology 日历系统} 和
 * {@linkplain java.time.ZoneId 时区}。这些可以通过 {@linkplain #query(TemporalQuery) 查询} 使用
 * {@link TemporalQuery} 中定义的静态方法访问。
 * <p>
 * 子接口 {@link Temporal} 扩展了此定义，支持在更完整的时间对象上进行调整和操作。
 * <p>
 * 此接口是框架级别的接口，不建议在应用程序代码中广泛使用。
 * 相反，应用程序应创建并传递具体类型的实例，如 {@code LocalDate}。
 * 这样做的原因有很多，部分原因是此接口的实现可能使用 ISO 以外的日历系统。
 * 有关此问题的更多讨论，请参见 {@link java.time.chrono.ChronoLocalDate}。
 *
 * @implSpec
 * 此接口对实现的可变性没有限制，但强烈建议不可变。
 *
 * @since 1.8
 */
public interface TemporalAccessor {

    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 此方法检查日期时间是否可以查询指定的字段。
     * 如果返回 false，则调用 {@link #range(TemporalField) range} 和 {@link #get(TemporalField) get}
     * 方法将抛出异常。
     *
     * @implSpec
     * 实现必须检查并处理 {@link ChronoField} 中定义的所有字段。
     * 如果字段受支持，则必须返回 true，否则返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.isSupportedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。
     * <p>
     * 实现必须确保在调用此只读方法时不会更改任何可观察的状态。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果此日期时间可以查询字段，则返回 true，否则返回 false
     */
    boolean isSupported(TemporalField field);

    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 所有字段都可以表示为 {@code long} 整数。
     * 此方法返回一个描述该值有效范围的对象。
     * 此时间对象的值用于提高返回范围的准确性。
     * 如果日期时间无法返回范围，因为字段不受支持或其他原因，将抛出异常。
     * <p>
     * 请注意，结果仅描述最小和最大有效值，不应过度解读。
     * 例如，范围内的某些值可能对字段无效。
     *
     * @implSpec
     * 实现必须检查并处理 {@link ChronoField} 中定义的所有字段。
     * 如果字段受支持，则必须返回字段的范围。
     * 如果不受支持，则必须抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.rangeRefinedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。
     * <p>
     * 实现必须确保在调用此只读方法时不会更改任何可观察的状态。
     * <p>
     * 默认实现必须等同于以下代码：
     * <pre>
     *  if (field instanceof ChronoField) {
     *    if (isSupported(field)) {
     *      return field.range();
     *    }
     *    throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
     *  }
     *  return field.rangeRefinedBy(this);
     * </pre>
     *
     * @param field  要查询范围的字段，不为 null
     * @return 字段的有效值范围，不为 null
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     */
    default ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            if (isSupported(field)) {
                return field.range();
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        Objects.requireNonNull(field, "field");
        return field.rangeRefinedBy(this);
    }

    /**
     * 获取指定字段的值作为 {@code int}。
     * <p>
     * 此方法查询日期时间以获取指定字段的值。
     * 返回的值将始终在字段的有效值范围内。
     * 如果日期时间无法返回值，因为字段不受支持或其他原因，将抛出异常。
     *
     * @implSpec
     * 实现必须检查并处理 {@link ChronoField} 中定义的所有字段。
     * 如果字段受支持且具有 {@code int} 范围，则必须返回字段的值。
     * 如果不受支持，则必须抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。
     * <p>
     * 实现必须确保在调用此只读方法时不会更改任何可观察的状态。
     * <p>
     * 默认实现必须等同于以下代码：
     * <pre>
     *  if (range(field).isIntValue()) {
     *    return range(field).checkValidIntValue(getLong(field), field);
     *  }
     *  throw new UnsupportedTemporalTypeException("Invalid field " + field + " for get() method, use getLong() instead");
     * </pre>
     *
     * @param field  要获取的字段，不为 null
     * @return 字段的值，在有效值范围内
     * @throws DateTimeException 如果无法获取字段的值或值超出有效值范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或值范围超过 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    default int get(TemporalField field) {
        ValueRange range = range(field);
        if (range.isIntValue() == false) {
            throw new UnsupportedTemporalTypeException("Invalid field " + field + " for get() method, use getLong() instead");
        }
        long value = getLong(field);
        if (range.isValidValue(value) == false) {
            throw new DateTimeException("Invalid value for " + field + " (valid values " + range + "): " + value);
        }
        return (int) value;
    }

    /**
     * 获取指定字段的值作为 {@code long}。
     * <p>
     * 此方法查询日期时间以获取指定字段的值。
     * 返回的值可能超出字段的有效值范围。
     * 如果日期时间无法返回值，因为字段不受支持或其他原因，将抛出异常。
     *
     * @implSpec
     * 实现必须检查并处理 {@link ChronoField} 中定义的所有字段。
     * 如果字段受支持，则必须返回字段的值。
     * 如果不受支持，则必须抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。
     * <p>
     * 实现必须确保在调用此只读方法时不会更改任何可观察的状态。
     *
     * @param field  要获取的字段，不为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    long getLong(TemporalField field);

    /**
     * 查询此日期时间。
     * <p>
     * 此方法使用指定的查询策略对象查询此日期时间。
     * <p>
     * 查询是提取日期时间信息的关键工具。
     * 它们的存在是为了外部化查询过程，允许不同的方法，符合策略设计模式。
     * 例如，查询可能检查日期是否是闰年2月29日前的一天，或计算到下一次生日的天数。
     * <p>
     * 最常见的查询实现是方法引用，如 {@code LocalDate::from} 和 {@code ZoneId::from}。
     * {@link TemporalQuery} 提供了额外的实现作为静态方法。
     *
     * @implSpec
     * 默认实现必须等同于以下代码：
     * <pre>
     *  if (query == TemporalQueries.zoneId() ||
     *        query == TemporalQueries.chronology() || query == TemporalQueries.precision()) {
     *    return null;
     *  }
     *  return query.queryFrom(this);
     * </pre>
     * 未来版本允许在 if 语句中添加更多的查询。
     * <p>
     * 所有实现此接口并重写此方法的类必须调用 {@code TemporalAccessor.super.query(query)}。
     * JDK 类可以避免调用 super，如果它们提供与默认行为等效的行为，但非 JDK 类不得利用此优化，必须调用 {@code super}。
     * <p>
     * 如果实现可以为默认实现 if 语句中列出的查询之一提供值，则必须这样做。
     * 例如，应用程序定义的 {@code HourMin} 类存储小时和分钟，必须重写此方法如下：
     * <pre>
     *  if (query == TemporalQueries.precision()) {
     *    return MINUTES;
     *  }
     *  return TemporalAccessor.super.query(query);
     * </pre>
     * <p>
     * 实现必须确保在调用此只读方法时不会更改任何可观察的状态。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为 null
     * @return 查询结果，可能返回 null（由查询定义）
     * @throws DateTimeException 如果无法查询
     * @throws ArithmeticException 如果发生数值溢出
     */
    default <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zoneId()
                || query == TemporalQueries.chronology()
                || query == TemporalQueries.precision()) {
            return null;
        }
        return query.queryFrom(this);
    }

}
