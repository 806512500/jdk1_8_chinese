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
 * 民国历系统中的一个时代。
 * <p>
 * 民国历系统有两个时代。
 * 当前时代，从1年开始，被称为“中华民国”时代。
 * 所有之前的年份，零或更早的预推年份或一年及以上的时代年份，都是“中华民国前”时代的一部分。
 *
 * <table summary="民国年份和时代" cellpadding="2" cellspacing="3" border="0" >
 * <thead>
 * <tr class="tableSubHeadingColor">
 * <th class="colFirst" align="left">时代年份</th>
 * <th class="colFirst" align="left">时代</th>
 * <th class="colFirst" align="left">预推年份</th>
 * <th class="colLast" align="left">ISO 预推年份</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr class="rowColor">
 * <td>2</td><td>ROC</td><td>2</td><td>1913</td>
 * </tr>
 * <tr class="altColor">
 * <td>1</td><td>ROC</td><td>1</td><td>1912</td>
 * </tr>
 * <tr class="rowColor">
 * <td>1</td><td>BEFORE_ROC</td><td>0</td><td>1911</td>
 * </tr>
 * <tr class="altColor">
 * <td>2</td><td>BEFORE_ROC</td><td>-1</td><td>1910</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * <b>不要使用 {@code ordinal()} 来获取 {@code MinguoEra} 的数字表示。
 * 使用 {@code getValue()} 代替。</b>
 *
 * @implSpec
 * 这是一个不可变和线程安全的枚举。
 *
 * @since 1.8
 */
public enum MinguoEra implements Era {

    /**
     * 当前时代之前的单一实例，'中华民国前时代'，
     * 数值为 0。
     */
    BEFORE_ROC,
    /**
     * 当前时代的单一实例，'中华民国时代'，
     * 数值为 1。
     */
    ROC;

    //-----------------------------------------------------------------------
    /**
     * 从 {@code int} 值中获取 {@code MinguoEra} 的实例。
     * <p>
     * {@code MinguoEra} 是一个表示民国时代 BEFORE_ROC/ROC 的枚举。
     * 此工厂方法允许从 {@code int} 值中获取枚举实例。
     *
     * @param minguoEra  要表示的 BEFORE_ROC/ROC 值，从 0 (BEFORE_ROC) 到 1 (ROC)
     * @return 时代单例，不为空
     * @throws DateTimeException 如果值无效
     */
    public static MinguoEra of(int minguoEra) {
        switch (minguoEra) {
            case 0:
                return BEFORE_ROC;
            case 1:
                return ROC;
            default:
                throw new DateTimeException("无效的时代: " + minguoEra);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 获取时代的数字 {@code int} 值。
     * <p>
     * 时代 BEFORE_ROC 的值为 0，而时代 ROC 的值为 1。
     *
     * @return 时代值，从 0 (BEFORE_ROC) 到 1 (ROC)
     */
    @Override
    public int getValue() {
        return ordinal();
    }

}
