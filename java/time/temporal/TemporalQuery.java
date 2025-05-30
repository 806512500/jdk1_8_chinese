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

/**
 * 用于查询时间对象的策略。
 * <p>
 * 查询是提取时间对象信息的关键工具。
 * 它们存在是为了外部化查询过程，允许不同的方法，遵循策略设计模式。
 * 例如，查询可能检查日期是否是闰年的2月29日前一天，或计算到你下一个生日的天数。
 * <p>
 * {@link TemporalField} 接口提供了另一种查询时间对象的机制。该接口仅限于返回一个 {@code long}。
 * 相比之下，查询可以返回任何类型。
 * <p>
 * 使用 {@code TemporalQuery} 有两种等效的方法。
 * 第一种是直接调用此接口上的方法。
 * 第二种是使用 {@link TemporalAccessor#query(TemporalQuery)}：
 * <pre>
 *   // 这两行是等效的，但推荐使用第二种方法
 *   temporal = thisQuery.queryFrom(temporal);
 *   temporal = temporal.query(thisQuery);
 * </pre>
 * 推荐使用第二种方法，{@code query(TemporalQuery)}，因为它在代码中更清晰易读。
 * <p>
 * 最常见的实现是方法引用，例如 {@code LocalDate::from} 和 {@code ZoneId::from}。
 * {@link TemporalQueries} 中提供了额外的常用查询。
 *
 * @implSpec
 * 本接口对实现的可变性没有限制，但强烈建议实现为不可变。
 *
 * @param <R> 查询返回的类型
 *
 * @since 1.8
 */
@FunctionalInterface
public interface TemporalQuery<R> {

    /**
     * 查询指定的时间对象。
     * <p>
     * 此方法查询指定的时间对象，使用实现类中封装的逻辑返回一个对象。
     * 例如，查询可能检查日期是否是闰年的2月29日前一天，或计算到你下一个生日的天数。
     * <p>
     * 使用此方法有两种等效的方法。
     * 第一种是直接调用此方法。
     * 第二种是使用 {@link TemporalAccessor#query(TemporalQuery)}：
     * <pre>
     *   // 这两行是等效的，但推荐使用第二种方法
     *   temporal = thisQuery.queryFrom(temporal);
     *   temporal = temporal.query(thisQuery);
     * </pre>
     * 推荐使用第二种方法，{@code query(TemporalQuery)}，因为它在代码中更清晰易读。
     *
     * @implSpec
     * 实现必须接受输入对象并查询它。
     * 实现定义查询的逻辑，并负责记录该逻辑。
     * 它可以使用 {@code TemporalAccessor} 上的任何方法来确定结果。
     * 输入对象不得被修改。
     * <p>
     * 输入的时间对象可能属于非ISO日历系统。
     * 实现可以选择记录与其他日历系统的兼容性，或通过 {@link TemporalQueries#chronology()} 查询纪元来拒绝非ISO时间对象。
     * <p>
     * 此方法可能被多个线程并行调用。
     * 调用时必须是线程安全的。
     *
     * @param temporal  要查询的时间对象，不得为 null
     * @return 查询的结果，如果未找到则返回 null
     * @throws DateTimeException 如果无法查询
     * @throws ArithmeticException 如果发生数值溢出
     */
    R queryFrom(TemporalAccessor temporal);

}
