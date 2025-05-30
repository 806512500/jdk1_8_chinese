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
 * Copyright (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
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
package java.time;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.Objects;

/**
 * 一个地理区域，其中应用相同的时区规则。
 * <p>
 * 时区信息被分类为定义 UTC/Greenwich 偏移量何时以及如何变化的一组规则。这些规则使用基于地理区域的标识符访问，如国家或州。
 * 最常见的区域分类是时区数据库 (TZDB)，它定义了如 'Europe/Paris' 和 'Asia/Tokyo' 这样的区域。
 * <p>
 * 本类建模的区域标识符与底层规则（由 {@link ZoneRules} 建模）是分开的。
 * 规则由政府定义，经常变化。相比之下，区域标识符定义明确且长期存在。
 * 这种分离还允许在适当的情况下在区域之间共享规则。
 *
 * @implSpec
 * 该类是不可变的，线程安全。
 *
 * @since 1.8
 */
final class ZoneRegion extends ZoneId implements Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 8386373296231747096L;
    /**
     * 时区 ID，不为空。
     */
    private final String id;
    /**
     * 时区规则，如果时区 ID 是宽松加载的，则为 null。
     */
    private final transient ZoneRules rules;

    /**
     * 从标识符中获取 {@code ZoneId} 的实例。
     *
     * @param zoneId  时区 ID，不为空
     * @param checkAvailable  是否检查时区 ID 是否可用
     * @return 时区 ID，不为空
     * @throws DateTimeException 如果 ID 格式无效
     * @throws ZoneRulesException 如果检查可用性且找不到 ID
     */
    static ZoneRegion ofId(String zoneId, boolean checkAvailable) {
        Objects.requireNonNull(zoneId, "zoneId");
        checkName(zoneId);
        ZoneRules rules = null;
        try {
            // 总是尝试加载以在反序列化后获得更好的行为
            rules = ZoneRulesProvider.getRules(zoneId, true);
        } catch (ZoneRulesException ex) {
            if (checkAvailable) {
                throw ex;
            }
        }
        return new ZoneRegion(zoneId, rules);
    }

    /**
     * 检查给定的字符串是否是合法的 ZoneId 名称。
     *
     * @param zoneId  时区 ID，不为空
     * @throws DateTimeException 如果 ID 格式无效
     */
    private static void checkName(String zoneId) {
        int n = zoneId.length();
        if (n < 2) {
           throw new DateTimeException("无效的基于区域的 ZoneId 标识符，格式无效: " + zoneId);
        }
        for (int i = 0; i < n; i++) {
            char c = zoneId.charAt(i);
            if (c >= 'a' && c <= 'z') continue;
            if (c >= 'A' && c <= 'Z') continue;
            if (c == '/' && i != 0) continue;
            if (c >= '0' && c <= '9' && i != 0) continue;
            if (c == '~' && i != 0) continue;
            if (c == '.' && i != 0) continue;
            if (c == '_' && i != 0) continue;
            if (c == '+' && i != 0) continue;
            if (c == '-' && i != 0) continue;
            throw new DateTimeException("无效的基于区域的 ZoneId 标识符，格式无效: " + zoneId);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * 构造函数。
     *
     * @param id  时区 ID，不为空
     * @param rules  规则，null 表示懒加载
     */
    ZoneRegion(String id, ZoneRules rules) {
        this.id = id;
        this.rules = rules;
    }

    //-----------------------------------------------------------------------
    @Override
    public String getId() {
        return id;
    }

    @Override
    public ZoneRules getRules() {
        // 当为 null 时，对组提供者进行额外查询，允许在 ZoneId 创建后提供者被更新的可能性
        return (rules != null ? rules : ZoneRulesProvider.getRules(id, false));
    }

    //-----------------------------------------------------------------------
    /**
     * 使用专用的序列化形式写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(7);  // 识别 ZoneId（而不是 ZoneOffset）
     *  out.writeUTF(zoneId);
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.ZONE_REGION_TYPE, this);
    }

    /**
     * 防御恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("通过序列化代理进行反序列化");
    }

    @Override
    void write(DataOutput out) throws IOException {
        out.writeByte(Ser.ZONE_REGION_TYPE);
        writeExternal(out);
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeUTF(id);
    }

    static ZoneId readExternal(DataInput in) throws IOException {
        String id = in.readUTF();
        return ZoneId.of(id, false);
    }

}
