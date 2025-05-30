/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Copyright (c) 2008-2013, Stephen Colebourne & Michael Nascimento Santos
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
 * 不同解析日期和时间方式的枚举。
 * <p>
 * 解析文本字符串分为两个阶段。
 * 第一阶段是根据添加到构建器中的字段进行基本文本解析。
 * 第二阶段将解析的字段-值对解析为日期和/或时间对象。
 * 此样式用于控制第二阶段（解析）如何发生。
 *
 * @implSpec
 * 这是一个不可变且线程安全的枚举。
 *
 * @since 1.8
 */
public enum ResolverStyle {

    /**
     * 严格解析日期和时间的样式。
     * <p>
     * 使用严格解析将确保所有解析的值都在字段的外部有效值范围内。个别字段可能会进一步处理严格性。
     * <p>
     * 例如，使用严格模式解析 ISO 日历系统中的年月和日，将确保日对于年月是有效的，拒绝无效的值。
     */
    STRICT,
    /**
     * 以智能或智能方式解析日期和时间的样式。
     * <p>
     * 使用智能解析将为每个字段执行合理的默认操作，这可能与严格相同，与宽松相同，或第三种行为。个别字段将不同地解释这一点。
     * <p>
     * 例如，使用智能模式解析 ISO 日历系统中的年月和日，将确保日从 1 到 31，将任何超出最后有效日的值转换为最后有效日。
     */
    SMART,
    /**
     * 宽松解析日期和时间的样式。
     * <p>
     * 使用宽松解析将以适当的方式宽松地解析值。个别字段将不同地解释这一点。
     * <p>
     * 例如，宽松模式允许 ISO 日历系统中的月超出 1 到 12 的范围。
     * 例如，月 15 被视为比月 12 晚 3 个月。
     */
    LENIENT;

}
