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

/**
 * 枚举处理正负号的方式。
 * <p>
 * 格式化引擎允许使用此枚举控制数字的正负号。
 * 请参阅 {@link DateTimeFormatterBuilder} 了解用法。
 *
 * @implSpec
 * 这是一个不可变且线程安全的枚举。
 *
 * @since 1.8
 */
public enum SignStyle {

    /**
     * 仅在值为负时输出符号的样式。
     * <p>
     * 在严格解析中，负号将被接受，正号将被拒绝。
     * 在宽松解析中，任何符号都将被接受。
     */
    NORMAL,
    /**
     * 始终输出符号的样式，其中零将输出 '+'。
     * <p>
     * 在严格解析中，缺少符号将被拒绝。
     * 在宽松解析中，任何符号都将被接受，缺少符号被视为正数。
     */
    ALWAYS,
    /**
     * 从不输出符号的样式，仅输出绝对值。
     * <p>
     * 在严格解析中，任何符号都将被拒绝。
     * 在宽松解析中，除非宽度固定，否则任何符号都将被接受。
     */
    NEVER,
    /**
     * 阻止负值的样式，打印时抛出异常。
     * <p>
     * 在严格解析中，任何符号都将被拒绝。
     * 在宽松解析中，除非宽度固定，否则任何符号都将被接受。
     */
    NOT_NEGATIVE,
    /**
     * 始终在值超过填充宽度时输出符号的样式。
     * 负值将始终输出 '-' 符号。
     * <p>
     * 在严格解析中，除非超过填充宽度，否则符号将被拒绝。
     * 在宽松解析中，任何符号都将被接受，缺少符号被视为正数。
     */
    EXCEEDS_PAD;

    /**
     * 解析辅助方法。
     *
     * @param positive  如果解析到正号为 true，负号为 false
     * @param strict  如果严格为 true，宽松为 false
     * @param fixedWidth  如果宽度固定为 true，否则为 false
     * @return
     */
    boolean parse(boolean positive, boolean strict, boolean fixedWidth) {
        switch (ordinal()) {
            case 0: // NORMAL
                // 如果为负或（正且宽松）则有效
                return !positive || !strict;
            case 1: // ALWAYS
            case 4: // EXCEEDS_PAD
                return true;
            default:
                // 如果宽松且宽度不固定则有效
                return !strict && !fixedWidth;
        }
    }

}
