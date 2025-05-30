
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

import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

/**
 * 时区ID，例如 {@code Europe/Paris}。
 * <p>
 * {@code ZoneId} 用于标识将 {@link Instant} 转换为 {@link LocalDateTime} 的规则。
 * 有两种不同类型的ID：
 * <ul>
 * <li>固定偏移量 - 从UTC/格林尼治完全解析的偏移量，对所有本地日期时间使用相同的偏移量
 * <li>地理区域 - 一个特定规则集适用的区域，用于查找从UTC/格林尼治的偏移量
 * </ul>
 * 大多数固定偏移量由 {@link ZoneOffset} 表示。
 * 调用任何 {@code ZoneId} 的 {@link #normalized()} 将确保固定偏移量ID表示为 {@code ZoneOffset}。
 * <p>
 * 实际规则，描述偏移量何时和如何变化，由 {@link ZoneRules} 定义。
 * 本类只是一个用于获取底层规则的ID。
 * 这种方法是因为规则由政府定义并频繁更改，而ID是稳定的。
 * <p>
 * 区别还有其他影响。序列化 {@code ZoneId} 只会发送ID，而序列化规则会发送整个数据集。
 * 同样，两个ID的比较只检查ID，而两个规则的比较会检查整个数据集。
 *
 * <h3>时区ID</h3>
 * ID在系统中是唯一的。
 * 有三种类型的ID。
 * <p>
 * 最简单的ID类型是来自 {@code ZoneOffset} 的ID。
 * 这包括 'Z' 和以 '+' 或 '-' 开头的ID。
 * <p>
 * 下一个类型的ID是带有某种前缀的偏移量样式ID，例如 'GMT+2' 或 'UTC+01:00'。
 * 识别的前缀是 'UTC'、'GMT' 和 'UT'。
 * 偏移量是后缀，将在创建时进行规范化。
 * 这些ID可以使用 {@code normalized()} 规范化为 {@code ZoneOffset}。
 * <p>
 * 第三种类型的ID是基于区域的ID。基于区域的ID必须是两个或更多字符，并且不能以 'UTC'、'GMT'、'UT'、'+' 或 '-' 开头。
 * 基于区域的ID由配置定义，参见 {@link ZoneRulesProvider}。
 * 配置的重点是提供从ID到底层 {@code ZoneRules} 的查找。
 * <p>
 * 时区规则由政府定义并频繁更改。
 * 有许多组织，这里称为组，监控时区变化并汇总它们。
 * 默认组是IANA时区数据库（TZDB）。
 * 其他组织包括IATA（航空业机构）和Microsoft。
 * <p>
 * 每个组定义其提供的区域ID的格式。
 * TZDB组定义了如 'Europe/London' 或 'America/New_York' 的ID。
 * TZDB ID优先于其他组。
 * <p>
 * 强烈建议在所有非TZDB组提供的ID中包含组名以避免冲突。例如，IATA航空时区区域ID通常是三个字母的机场代码。
 * 但是，乌得勒支机场的代码是 'UTC'，这显然是一个冲突。
 * 非TZDB组的区域ID推荐格式是 'group~region'。
 * 因此，如果定义了IATA数据，乌得勒支机场将是 'IATA~UTC'。
 *
 * <h3>序列化</h3>
 * 本类可以序列化，并在外部形式中存储字符串时区ID。
 * {@code ZoneOffset} 子类使用专用格式，仅存储与UTC/格林尼治的偏移量。
 * <p>
 * {@code ZoneId} 可以在Java运行时中反序列化，即使该ID未知。
 * 例如，如果服务器端Java运行时已更新了新的时区ID，但客户端Java运行时尚未更新。
 * 在这种情况下，{@code ZoneId} 对象将存在，并且可以使用 {@code getId}、{@code equals}、
 * {@code hashCode}、{@code toString}、{@code getDisplayName} 和 {@code normalized} 进行查询。
 * 但是，任何调用 {@code getRules} 的方法都会因 {@code ZoneRulesException} 而失败。
 * 这种方法设计为允许在具有不完整时区信息的Java运行时中加载和查询 {@link ZonedDateTime}，但不能修改。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值</a>
 * 的类；使用身份敏感操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）对 {@code ZoneId} 实例进行操作可能会产生不可预测的结果，应避免。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 这个抽象类有两个实现，这两个实现都是不可变的且线程安全的。
 * 一个实现建模基于区域的ID，另一个是 {@code ZoneOffset} 建模基于偏移量的ID。这种差异在序列化中可见。
 *
 * @since 1.8
 */
public abstract class ZoneId implements Serializable {

    /**
     * 时区覆盖映射，以启用短时区名称的使用。
     * <p>
     * 在 {@code java.util.TimeZone} 中使用短时区ID已被弃用。
     * 该映射允许通过 {@link #of(String, Map)} 工厂方法继续使用这些ID。
     * <p>
     * 该映射与TZDB 2005r及以后版本一致，其中 'EST'、'MST' 和 'HST' 映射到不包括夏令时的ID。
     * <p>
     * 该映射如下：
     * <ul>
     * <li>EST - -05:00</li>
     * <li>HST - -10:00</li>
     * <li>MST - -07:00</li>
     * <li>ACT - Australia/Darwin</li>
     * <li>AET - Australia/Sydney</li>
     * <li>AGT - America/Argentina/Buenos_Aires</li>
     * <li>ART - Africa/Cairo</li>
     * <li>AST - America/Anchorage</li>
     * <li>BET - America/Sao_Paulo</li>
     * <li>BST - Asia/Dhaka</li>
     * <li>CAT - Africa/Harare</li>
     * <li>CNT - America/St_Johns</li>
     * <li>CST - America/Chicago</li>
     * <li>CTT - Asia/Shanghai</li>
     * <li>EAT - Africa/Addis_Ababa</li>
     * <li>ECT - Europe/Paris</li>
     * <li>IET - America/Indiana/Indianapolis</li>
     * <li>IST - Asia/Kolkata</li>
     * <li>JST - Asia/Tokyo</li>
     * <li>MIT - Pacific/Apia</li>
     * <li>NET - Asia/Yerevan</li>
     * <li>NST - Pacific/Auckland</li>
     * <li>PLT - Asia/Karachi</li>
     * <li>PNT - America/Phoenix</li>
     * <li>PRT - America/Puerto_Rico</li>
     * <li>PST - America/Los_Angeles</li>
     * <li>SST - Pacific/Guadalcanal</li>
     * <li>VST - Asia/Ho_Chi_Minh</li>
     * </ul>
     * 该映射是不可修改的。
     */
    public static final Map<String, String> SHORT_IDS;
    static {
        Map<String, String> map = new HashMap<>(64);
        map.put("ACT", "Australia/Darwin");
        map.put("AET", "Australia/Sydney");
        map.put("AGT", "America/Argentina/Buenos_Aires");
        map.put("ART", "Africa/Cairo");
        map.put("AST", "America/Anchorage");
        map.put("BET", "America/Sao_Paulo");
        map.put("BST", "Asia/Dhaka");
        map.put("CAT", "Africa/Harare");
        map.put("CNT", "America/St_Johns");
        map.put("CST", "America/Chicago");
        map.put("CTT", "Asia/Shanghai");
        map.put("EAT", "Africa/Addis_Ababa");
        map.put("ECT", "Europe/Paris");
        map.put("IET", "America/Indiana/Indianapolis");
        map.put("IST", "Asia/Kolkata");
        map.put("JST", "Asia/Tokyo");
        map.put("MIT", "Pacific/Apia");
        map.put("NET", "Asia/Yerevan");
        map.put("NST", "Pacific/Auckland");
        map.put("PLT", "Asia/Karachi");
        map.put("PNT", "America/Phoenix");
        map.put("PRT", "America/Puerto_Rico");
        map.put("PST", "America/Los_Angeles");
        map.put("SST", "Pacific/Guadalcanal");
        map.put("VST", "Asia/Ho_Chi_Minh");
        map.put("EST", "-05:00");
        map.put("MST", "-07:00");
        map.put("HST", "-10:00");
        SHORT_IDS = Collections.unmodifiableMap(map);
    }
    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 8352817235686L;

    //-----------------------------------------------------------------------
    /**
     * 获取系统默认时区。
     * <p>
     * 此方法查询 {@link TimeZone#getDefault()} 以查找默认时区并将其转换为 {@code ZoneId}。
     * 如果系统默认时区更改，则此方法的结果也会更改。
     *
     * @return 时区ID，不为空
     * @throws DateTimeException 如果转换的时区ID格式无效
     * @throws ZoneRulesException 如果转换的时区区域ID找不到
     */
    public static ZoneId systemDefault() {
        return TimeZone.getDefault().toZoneId();
    }

    /**
     * 获取可用的时区ID集。
     * <p>
     * 该集包括所有可用的基于区域的ID的字符串形式。
     * 基于偏移量的时区ID不包含在返回的集中。
     * 该ID可以传递给 {@link #of(String)} 以创建 {@code ZoneId}。
     * <p>
     * 时区ID集可以随着时间增加，尽管在典型应用中ID集是固定的。
     * 每次调用此方法都是线程安全的。
     *
     * @return 时区ID集的可修改副本，不为空
     */
    public static Set<String> getAvailableZoneIds() {
        return ZoneRulesProvider.getAvailableZoneIds();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用ID和别名映射获取 {@code ZoneId} 的实例。
     * <p>
     * 许多时区用户使用短缩写，例如 PST 表示 '太平洋标准时间'，PDT 表示 '太平洋夏令时'。
     * 这些缩写不是唯一的，因此不能用作ID。
     * 此方法允许在应用中设置和重用字符串到时区的映射。
     *
     * @param zoneId  时区ID，不为空
     * @param aliasMap  别名时区ID（通常是缩写）到实际时区ID的映射，不为空
     * @return 时区ID，不为空
     * @throws DateTimeException 如果时区ID格式无效
     * @throws ZoneRulesException 如果时区ID是找不到的区域ID
     */
    public static ZoneId of(String zoneId, Map<String, String> aliasMap) {
        Objects.requireNonNull(zoneId, "zoneId");
        Objects.requireNonNull(aliasMap, "aliasMap");
        String id = aliasMap.get(zoneId);
        id = (id != null ? id : zoneId);
        return of(id);
    }

    /**
     * 从ID获取 {@code ZoneId} 的实例，确保ID有效且可用。
     * <p>
     * 此方法解析ID，生成 {@code ZoneId} 或 {@code ZoneOffset}。
     * 如果ID是 'Z' 或以 '+' 或 '-' 开头，则返回 {@code ZoneOffset}。
     * 结果始终是一个有效的ID，可以从 {@link ZoneRules} 获取。
     * <p>
     * 解析按以下步骤逐个匹配时区ID。
     * <ul>
     * <li>如果时区ID等于 'Z'，结果是 {@code ZoneOffset.UTC}。
     * <li>如果时区ID仅由一个字母组成，时区ID无效，将抛出 {@code DateTimeException}。
     * <li>如果时区ID以 '+' 或 '-' 开头，ID将使用 {@link ZoneOffset#of(String)} 解析为 {@code ZoneOffset}。
     * <li>如果时区ID等于 'GMT'、'UTC' 或 'UT'，结果是具有相同ID和与 {@code ZoneOffset.UTC} 等效规则的 {@code ZoneId}。
     * <li>如果时区ID以 'UTC+'、'UTC-'、'GMT+'、'GMT-'、'UT+' 或 'UT-' 开头，则ID是带有前缀的基于偏移量的ID。
     *     ID被拆分为两部分，前缀为两到三个字母，后缀以符号开头。
     *     后缀被解析为 {@link ZoneOffset#of(String) ZoneOffset}。
     *     结果将是具有指定的UTC/GMT/UT前缀和 {@link ZoneOffset#getId()} 规范化偏移量ID的 {@code ZoneId}。
     *     返回的 {@code ZoneId} 的规则将与解析的 {@code ZoneOffset} 等效。
     * <li>所有其他ID被解析为基于区域的时区ID。区域ID必须匹配正则表达式 <code>[A-Za-z][A-Za-z0-9~/._+-]+</code>，
     *     否则将抛出 {@code DateTimeException}。如果时区ID不在配置的ID集中，将抛出 {@code ZoneRulesException}。
     *     区域ID的详细格式取决于提供数据的组。默认的数据集由IANA时区数据库（TZDB）提供。
     *     这种格式的区域ID为 '{area}/{city}'，例如 'Europe/Paris' 或 'America/New_York'。
     *     这与大多数 {@link java.util.TimeZone} 的ID兼容。
     * </ul>
     *
     * @param zoneId  时区ID，不为空
     * @return 时区ID，不为空
     * @throws DateTimeException 如果时区ID格式无效
     * @throws ZoneRulesException 如果时区ID是找不到的区域ID
     */
    public static ZoneId of(String zoneId) {
        return of(zoneId, true);
    }


                /**
     * 获取一个包装了偏移量的 {@code ZoneId} 实例。
     * <p>
     * 如果前缀是 "GMT"、"UTC" 或 "UT"，则返回一个带有前缀和非零偏移量的 {@code ZoneId}。
     * 如果前缀为空字符串 {@code ""}，则返回 {@code ZoneOffset}。
     *
     * @param prefix  时区ID，不为空
     * @param offset  偏移量，不为空
     * @return 时区ID，不为空
     * @throws IllegalArgumentException 如果前缀不是 "GMT"、"UTC" 或 "UT"，或 ""
     */
    public static ZoneId ofOffset(String prefix, ZoneOffset offset) {
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(offset, "offset");
        if (prefix.length() == 0) {
            return offset;
        }

        if (!prefix.equals("GMT") && !prefix.equals("UTC") && !prefix.equals("UT")) {
             throw new IllegalArgumentException("prefix should be GMT, UTC or UT, is: " + prefix);
        }

        if (offset.getTotalSeconds() != 0) {
            prefix = prefix.concat(offset.getId());
        }
        return new ZoneRegion(prefix, offset.getRules());
    }

    /**
     * 解析ID，根据标志指示是否应抛出 {@code ZoneRulesException}，用于反序列化。
     *
     * @param zoneId  时区ID，不为空
     * @param checkAvailable  是否检查时区ID是否可用
     * @return 时区ID，不为空
     * @throws DateTimeException 如果ID格式无效
     * @throws ZoneRulesException 如果检查可用性且ID未找到
     */
    static ZoneId of(String zoneId, boolean checkAvailable) {
        Objects.requireNonNull(zoneId, "zoneId");
        if (zoneId.length() <= 1 || zoneId.startsWith("+") || zoneId.startsWith("-")) {
            return ZoneOffset.of(zoneId);
        } else if (zoneId.startsWith("UTC") || zoneId.startsWith("GMT")) {
            return ofWithPrefix(zoneId, 3, checkAvailable);
        } else if (zoneId.startsWith("UT")) {
            return ofWithPrefix(zoneId, 2, checkAvailable);
        }
        return ZoneRegion.ofId(zoneId, checkAvailable);
    }

    /**
     * 一旦前缀建立，进行解析。
     *
     * @param zoneId  时区ID，不为空
     * @param prefixLength  前缀长度，2 或 3
     * @return 时区ID，不为空
     * @throws DateTimeException 如果时区ID格式无效
     */
    private static ZoneId ofWithPrefix(String zoneId, int prefixLength, boolean checkAvailable) {
        String prefix = zoneId.substring(0, prefixLength);
        if (zoneId.length() == prefixLength) {
            return ofOffset(prefix, ZoneOffset.UTC);
        }
        if (zoneId.charAt(prefixLength) != '+' && zoneId.charAt(prefixLength) != '-') {
            return ZoneRegion.ofId(zoneId, checkAvailable);  // drop through to ZoneRulesProvider
        }
        try {
            ZoneOffset offset = ZoneOffset.of(zoneId.substring(prefixLength));
            if (offset == ZoneOffset.UTC) {
                return ofOffset(prefix, offset);
            }
            return ofOffset(prefix, offset);
        } catch (DateTimeException ex) {
            throw new DateTimeException("Invalid ID for offset-based ZoneId: " + zoneId, ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 从一个时间对象中获取一个 {@code ZoneId} 实例。
     * <p>
     * 根据指定的时间对象获取时区。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，
     * 该工厂将其转换为 {@code ZoneId} 的实例。
     * <p>
     * {@code TemporalAccessor} 表示一些形式的日期和时间信息。
     * 该工厂将任意的时间对象转换为 {@code ZoneId} 的实例。
     * <p>
     * 转换将尝试以优先使用基于区域的时区而非基于偏移量的时区的方式获取时区，使用 {@link TemporalQueries#zone()}。
     * <p>
     * 此方法匹配功能接口 {@link TemporalQuery} 的签名，
     * 允许通过方法引用作为查询使用，{@code ZoneId::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 时区ID，不为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code ZoneId}
     */
    public static ZoneId from(TemporalAccessor temporal) {
        ZoneId obj = temporal.query(TemporalQueries.zone());
        if (obj == null) {
            throw new DateTimeException("Unable to obtain ZoneId from TemporalAccessor: " +
                    temporal + " of type " + temporal.getClass().getName());
        }
        return obj;
    }

    //-----------------------------------------------------------------------
    /**
     * 仅在包内可访问的构造函数。
     */
    ZoneId() {
        if (getClass() != ZoneOffset.class && getClass() != ZoneRegion.class) {
            throw new AssertionError("Invalid subclass");
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 获取唯一的时区ID。
     * <p>
     * 此ID唯一定义此对象。
     * 基于偏移量的ID的格式由 {@link ZoneOffset#getId()} 定义。
     *
     * @return 时区唯一ID，不为空
     */
    public abstract String getId();

    //-----------------------------------------------------------------------
    /**
     * 获取时区的文本表示，如 'British Time' 或 '+02:00'。
     * <p>
     * 这返回用于标识时区ID的文本名称，
     * 适合向用户展示。
     * 参数控制返回文本的样式和语言环境。
     * <p>
     * 如果未找到文本映射，则返回 {@link #getId() 完整ID}。
     *
     * @param style  所需文本的长度，不为空
     * @param locale  使用的语言环境，不为空
     * @return 时区的文本值，不为空
     */
    public String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendZoneText(style).toFormatter(locale).format(toTemporal());
    }

    /**
     * 将此时区转换为 {@code TemporalAccessor}。
     * <p>
     * {@code ZoneId} 可以完全表示为 {@code TemporalAccessor}。
     * 但是，此类不实现该接口，因为接口上的大多数方法对 {@code ZoneId} 没有意义。
     * <p>
     * 返回的临时对象没有支持的字段，查询方法支持使用 {@link TemporalQueries#zoneId()} 返回时区。
     *
     * @return 与此时区等效的临时对象，不为空
     */
    private TemporalAccessor toTemporal() {
        return new TemporalAccessor() {
            @Override
            public boolean isSupported(TemporalField field) {
                return false;
            }
            @Override
            public long getLong(TemporalField field) {
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            }
            @SuppressWarnings("unchecked")
            @Override
            public <R> R query(TemporalQuery<R> query) {
                if (query == TemporalQueries.zoneId()) {
                    return (R) ZoneId.this;
                }
                return TemporalAccessor.super.query(query);
            }
        };
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此ID的时区规则，允许执行计算。
     * <p>
     * 规则提供了与时区相关的功能，
     * 如查找给定瞬间或本地日期时间的偏移量。
     * <p>
     * 如果时区在反序列化时所在的Java运行时没有加载相同的规则，
     * 则该时区可能无效。
     * 在这种情况下，调用此方法将抛出 {@code ZoneRulesException}。
     * <p>
     * 规则由 {@link ZoneRulesProvider} 提供。高级提供者可能支持在不重启Java运行时的情况下动态更新规则。
     * 如果是这样，那么此方法的结果可能会随时间变化。
     * 每个单独的调用仍然保持线程安全。
     * <p>
     * {@link ZoneOffset} 始终返回一个偏移量永不变化的规则集。
     *
     * @return 规则，不为空
     * @throws ZoneRulesException 如果没有可用的规则
     */
    public abstract ZoneRules getRules();

    /**
     * 规范化时区ID，如果可能则返回 {@code ZoneOffset}。
     * <p>
     * 返回一个可以替代此ID的规范化的 {@code ZoneId}。
     * 结果将具有与本对象返回的 {@code ZoneRules} 等效的规则，
     * 但是 {@code getId()} 返回的ID可能不同。
     * <p>
     * 规范化检查此 {@code ZoneId} 的规则是否有固定的偏移量。
     * 如果有，则返回与该偏移量相等的 {@code ZoneOffset}。
     * 否则返回 {@code this}。
     *
     * @return 时区唯一ID，不为空
     */
    public ZoneId normalized() {
        try {
            ZoneRules rules = getRules();
            if (rules.isFixedOffset()) {
                return rules.getOffset(Instant.EPOCH);
            }
        } catch (ZoneRulesException ex) {
            // 无效的 ZoneRegion 对此方法不重要
        }
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此时区ID是否等于另一个时区ID。
     * <p>
     * 比较基于ID。
     *
     * @param obj  要检查的对象，为空返回 false
     * @return 如果此对象等于其他时区ID，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
           return true;
        }
        if (obj instanceof ZoneId) {
            ZoneId other = (ZoneId) obj;
            return getId().equals(other.getId());
        }
        return false;
    }

    /**
     * 为此时区ID生成哈希码。
     *
     * @return 适合的哈希码
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * 防御恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 始终抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    /**
     * 以字符串形式输出此时区，使用ID。
     *
     * @return 此时区ID的字符串表示，不为空
     */
    @Override
    public String toString() {
        return getId();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用序列化形式</a>
     * 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(7);  // 标识一个 ZoneId（不是 ZoneOffset）
     *  out.writeUTF(getId());
     * </pre>
     * <p>
     * 当读回时，{@code ZoneId} 将像使用 {@link #of(String)} 一样创建，
     * 但在ID格式有效但不在已知的基于区域的ID集中时不会抛出异常。
     *
     * @return {@code Ser} 的实例，不为空
     */
    // 这里是为了序列化 Javadoc
    private Object writeReplace() {
        return new Ser(Ser.ZONE_REGION_TYPE, this);
    }

    abstract void write(DataOutput out) throws IOException;

}
