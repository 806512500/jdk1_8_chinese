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

import java.util.Calendar;

/**
 * 枚举文本格式和解析的样式。
 * <p>
 * 文本样式定义了三种格式大小 - 'full'（完整）、'short'（简短）和 'narrow'（狭窄）。
 * 每种大小都有 'standard'（标准）和 'stand-alone'（独立）两种变体。
 * <p>
 * 在大多数语言中，这三种大小的区别是显而易见的。
 * 例如，在英语中，'full' 月份是 'January'，'short' 月份是 'Jan'，'narrow' 月份是 'J'。
 * 请注意，'narrow' 大小通常不是唯一的。
 * 例如，'January'、'June' 和 'July' 都有 'narrow' 文本 'J'。
 * <p>
 * 'standard' 和 'stand-alone' 形式的区别较难描述，因为英语中没有区别。
 * 但是，在其他语言中，当文本单独使用时，与在完整日期中使用时，所用的词是不同的。
 * 例如，在日期选择器中单独使用的月份词与在日期中与日和年一起使用的月份词是不同的。
 *
 * @implSpec
 * 这是一个不可变和线程安全的枚举。
 */
public enum TextStyle {
    // 从大到小排序
    // 通过序数的第 0 位指示是否为独立形式。

    /**
     * 完整文本，通常是完整的描述。
     * 例如，星期一可能会输出 "Monday"。
     */
    FULL(Calendar.LONG_FORMAT, 0),
    /**
     * 用于独立使用的完整文本，通常是完整的描述。
     * 例如，星期一可能会输出 "Monday"。
     */
    FULL_STANDALONE(Calendar.LONG_STANDALONE, 0),
    /**
     * 简短文本，通常是缩写。
     * 例如，星期一可能会输出 "Mon"。
     */
    SHORT(Calendar.SHORT_FORMAT, 1),
    /**
     * 用于独立使用的简短文本，通常是缩写。
     * 例如，星期一可能会输出 "Mon"。
     */
    SHORT_STANDALONE(Calendar.SHORT_STANDALONE, 1),
    /**
     * 狭窄文本，通常是一个字母。
     * 例如，星期一可能会输出 "M"。
     */
    NARROW(Calendar.NARROW_FORMAT, 1),
    /**
     * 用于独立使用的狭窄文本，通常是一个字母。
     * 例如，星期一可能会输出 "M"。
     */
    NARROW_STANDALONE(Calendar.NARROW_STANDALONE, 1);

    private final int calendarStyle;
    private final int zoneNameStyleIndex;

    private TextStyle(int calendarStyle, int zoneNameStyleIndex) {
        this.calendarStyle = calendarStyle;
        this.zoneNameStyleIndex = zoneNameStyleIndex;
    }

    /**
     * 如果样式是独立样式，则返回 true。
     * @return 如果样式是独立样式，则返回 true。
     */
    public boolean isStandalone() {
        return (ordinal() & 1) == 1;
    }

    /**
     * 返回具有相同大小的独立样式。
     * @return 具有相同大小的独立样式
     */
    public TextStyle asStandalone() {
        return TextStyle.values()[ordinal()  | 1];
    }

    /**
     * 返回具有相同大小的普通样式。
     *
     * @return 具有相同大小的普通样式
     */
    public TextStyle asNormal() {
        return TextStyle.values()[ordinal() & ~1];
    }

    /**
     * 返回与此 {@code TextStyle} 对应的 {@code Calendar} 样式。
     *
     * @return 对应的 {@code Calendar} 样式
     */
    int toCalendarStyle() {
        return calendarStyle;
    }

    /**
     * 返回 {@link
     * java.text.DateFormatSymbols#getZoneStrings() DateFormatSymbols.getZoneStrings()}
     * 值的相对索引值，0 表示长名称，1 表示短名称（缩写）。请注意，这些值
     * <em>不</em> 对应于 {@link java.util.TimeZone#LONG} 和 {@link
     * java.util.TimeZone#SHORT} 值。
     *
     * @return 时区名称数组的相对索引值
     */
    int zoneNameStyleIndex() {
        return zoneNameStyleIndex;
    }
}
