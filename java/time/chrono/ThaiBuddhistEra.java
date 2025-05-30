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
package java.time.chrono;

import java.time.DateTimeException;

/**
 * 泰国佛教日历系统中的一个时代。
 * <p>
 * 泰国佛教日历系统有两个时代。
 * 当前时代，从1年开始，被称为“佛教”时代。
 * 所有之前的年份，即在预设年份中为零或更早，或在时代年份中为一或更大，
 * 都属于“前佛教”时代。
 *
 * <table summary="佛教年份和时代" cellpadding="2" cellspacing="3" border="0" >
 * <thead>
 * <tr class="tableSubHeadingColor">
 * <th class="colFirst" align="left">时代年份</th>
 * <th class="colFirst" align="left">时代</th>
 * <th class="colFirst" align="left">预设年份</th>
 * <th class="colLast" align="left">ISO 预设年份</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr class="rowColor">
 * <td>2</td><td>BE</td><td>2</td><td>-542</td>
 * </tr>
 * <tr class="altColor">
 * <td>1</td><td>BE</td><td>1</td><td>-543</td>
 * </tr>
 * <tr class="rowColor">
 * <td>1</td><td>BEFORE_BE</td><td>0</td><td>-544</td>
 * </tr>
 * <tr class="altColor">
 * <td>2</td><td>BEFORE_BE</td><td>-1</td><td>-545</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * <b>不要使用 {@code ordinal()} 来获取 {@code ThaiBuddhistEra} 的数字表示。
 * 使用 {@code getValue()} 代替。</b>
 *
 * @implSpec
 * 这是一个不可变且线程安全的枚举。
 *
 * @since 1.8
 */
public enum ThaiBuddhistEra implements Era {

    /**
     * 当前时代之前的单一实例，'前佛教时代'，
     * 其数字值为 0。
     */
    BEFORE_BE,
    /**
     * 当前时代的单一实例，'佛教时代'，
     * 其数字值为 1。
     */
    BE;

    //-----------------------------------------------------------------------
    /**
     * 从 {@code int} 值中获取 {@code ThaiBuddhistEra} 的实例。
     * <p>
     * {@code ThaiBuddhistEra} 是一个表示泰国佛教时代的枚举。
     * 此工厂方法允许从 {@code int} 值中获取枚举。
     *
     * @param thaiBuddhistEra  要表示的时代，从 0 到 1
     * @return 佛教时代的单一实例，从不为 null
     * @throws DateTimeException 如果时代无效
     */
    public static ThaiBuddhistEra of(int thaiBuddhistEra) {
        switch (thaiBuddhistEra) {
            case 0:
                return BEFORE_BE;
            case 1:
                return BE;
            default:
                throw new DateTimeException("无效的时代: " + thaiBuddhistEra);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 获取数字时代 {@code int} 值。
     * <p>
     * 时代 BEFORE_BE 的值为 0，而时代 BE 的值为 1。
     *
     * @return 时代值，从 0 (BEFORE_BE) 到 1 (BE)
     */
    @Override
    public int getValue() {
        return ordinal();
    }

}
