/*
 * Copyright (c) 2012, 2017, Oracle and/or its affiliates. All rights reserved.
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
package java.time.chrono;

import static java.time.temporal.ChronoField.ERA;
import static java.time.temporal.ChronoUnit.ERAS;

import java.time.DateTimeException;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.ValueRange;
import java.util.Locale;

/**
 * 一个时间线的纪元。
 * <p>
 * 大多数历法系统有一个单个的纪元，将时间线分为两个纪元。
 * 然而，一些历法系统有多个纪元，例如每个领导者统治时期一个纪元。
 * 在所有情况下，纪元是时间线概念上最大的划分。
 * 每个历法定义了已知的纪元，并且可以通过
 * {@link Chronology#eras Chronology.eras} 获取有效的纪元。
 * <p>
 * 例如，泰国佛教历法系统将时间分为两个纪元，一个纪元在某个日期之前，一个纪元在该日期之后。
 * 相比之下，日本历法系统为每位天皇的统治时期设有一个纪元。
 * <p>
 * {@code Era} 的实例可以使用 {@code ==} 操作符进行比较。
 *
 * @implSpec
 * 必须谨慎实现此接口以确保其他类正确运行。
 * 所有实现必须是单例 - 最终的、不可变的和线程安全的。
 * 推荐尽可能使用枚举。
 *
 * @since 1.8
 */
public interface Era extends TemporalAccessor, TemporalAdjuster {

    /**
     * 获取与纪元关联的数值，由历法定义。
     * 每个历法定义了预定义的纪元和列出历法纪元的方法。
     * <p>
     * 所有字段，包括纪元，都有一个关联的数值。
     * 纪元数值的含义由历法根据以下原则确定：
     * <ul>
     * <li>1970-01-01（ISO）时使用的纪元值为1。
     * <li>之后的纪元值依次递增。
     * <li>之前的纪元值依次递减，可能是负数。
     * </ul>
     *
     * @return 纪元的数值
     */
    int getValue();

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 这个方法检查此纪元是否可以查询指定的字段。
     * 如果返回 false，则调用 {@link #range(TemporalField) range} 和
     * {@link #get(TemporalField) get} 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@code ERA} 字段返回 true。
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.isSupportedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得。
     * 字段是否受支持由字段决定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果字段在此纪元上受支持则返回 true，否则返回 false
     */
    @Override
    default boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field == ERA;
        }
        return field != null && field.isSupportedBy(this);
    }

    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的最小和最大有效值。
     * 此纪元用于增强返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@code ERA} 字段返回范围。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.rangeRefinedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得。
     * 是否可以获取范围由字段决定。
     * <p>
     * 默认实现必须为 {@code ERA} 返回从零到一的范围，
     * 适用于两个纪元的历法系统，如 ISO。
     *
     * @param field  要查询范围的字段，不为 null
     * @return 字段的有效值范围，不为 null
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     */
    @Override  // 重写以提供 Javadoc
    default ValueRange range(TemporalField field) {
        return TemporalAccessor.super.range(field);
    }

    /**
     * 从这个纪元获取指定字段的值，作为 {@code int}。
     * <p>
     * 此方法查询此纪元以获取指定字段的值。
     * 返回的值将始终在字段的有效值范围内。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@code ERA} 字段返回纪元的值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得。
     * 是否可以获取值以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或值超出字段的有效值范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // 重写以提供 Javadoc 和性能
    default int get(TemporalField field) {
        if (field == ERA) {
            return getValue();
        }
        return TemporalAccessor.super.get(field);
    }

    /**
     * 从这个纪元获取指定字段的值，作为 {@code long}。
     * <p>
     * 此方法查询此纪元以获取指定字段的值。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@code ERA} 字段返回纪元的值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得。
     * 是否可以获取值以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    default long getLong(TemporalField field) {
        if (field == ERA) {
            return getValue();
        } else if (field instanceof ChronoField) {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询策略对象查询此纪元。
     * <p>
     * 此方法使用指定的查询策略对象查询此纪元。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。
     * 请阅读查询的文档以了解此方法的结果。
     * <p>
     * 此方法的结果通过调用
     * {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并
     * 将 {@code this} 作为参数传递来获得。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为 null
     * @return 查询结果，可能为 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    default <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.precision()) {
            return (R) ERAS;
        }
        return TemporalAccessor.super.query(query);
    }

    /**
     * 调整指定的时间对象以具有与此对象相同的纪元。
     * <p>
     * 此方法返回一个与输入具有相同可观察类型的对象，但纪元已更改为与此对象相同。
     * <p>
     * 调整等效于使用 {@link Temporal#with(TemporalField, long)}
     * 并传递 {@link ChronoField#ERA} 作为字段。
     * <p>
     * 在大多数情况下，反转调用模式更清晰，使用
     * {@link Temporal#with(TemporalAdjuster)}：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisEra.adjustInto(temporal);
     *   temporal = temporal.with(thisEra);
     * </pre>
     * <p>
     * 此实例是不可变的，不受此方法调用的影响。
     *
     * @param temporal  要调整的目标对象，不为 null
     * @return 调整后的对象，不为 null
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    default Temporal adjustInto(Temporal temporal) {
        return temporal.with(ERA, getValue());
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此纪元的文本表示。
     * <p>
     * 此方法返回用于标识纪元的文本名称，
     * 适合呈现给用户。
     * 参数控制返回文本的样式和语言环境。
     * <p>
     * 如果未找到文本映射，则返回 {@link #getValue() 数值值}。
     *
     * @apiNote 此默认实现适用于大多数实现。
     *
     * @param style  所需的文本样式，不为 null
     * @param locale  要使用的语言环境，不为 null
     * @return 纪元的文本值，不为 null
     */
    default String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendText(ERA, style).toFormatter(locale).format(this);
    }

    // NOTE: methods to convert year-of-era/proleptic-year cannot be here as they may depend on month/day (Japanese)
}
