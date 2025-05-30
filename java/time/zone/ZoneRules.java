
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
 * Copyright (c) 2009-2012, Stephen Colebourne & Michael Nascimento Santos
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
package java.time.zone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 定义时区偏移量如何变化的规则。
 * <p>
 * 规则模型包括时区的所有历史和未来转换。
 * {@link ZoneOffsetTransition} 用于已知的转换，通常是历史性的。
 * {@link ZoneOffsetTransitionRule} 用于基于算法结果的未来转换。
 * <p>
 * 规则是通过 {@link ZoneRulesProvider} 使用 {@link ZoneId} 加载的。
 * 相同的规则可能在多个时区 ID 之间内部共享。
 * <p>
 * 序列化 {@code ZoneRules} 的实例将存储整套规则。
 * 它不存储时区 ID，因为这不是此对象的状态的一部分。
 * <p>
 * 规则实现可能或可能不存储关于历史和未来转换的完整信息，存储的信息仅与规则提供者提供的信息一样准确。
 * 应用程序应将提供的数据视为此规则实现可用的最佳信息。
 *
 * @implSpec
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class ZoneRules implements Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 3044319355680032515L;
    /**
     * 缓存转换的最后一年。
     */
    private static final int LAST_CACHED_YEAR = 2100;

    /**
     * 标准偏移量之间的转换（纪元秒），已排序。
     */
    private final long[] standardTransitions;
    /**
     * 标准偏移量。
     */
    private final ZoneOffset[] standardOffsets;
    /**
     * 时刻之间的转换（纪元秒），已排序。
     */
    private final long[] savingsInstantTransitions;
    /**
     * 本地日期时间之间的转换，已排序。
     * 这是一个配对数组，其中第一个条目是转换的开始，第二个条目是转换的结束。
     */
    private final LocalDateTime[] savingsLocalTransitions;
    /**
     * 墙面偏移量。
     */
    private final ZoneOffset[] wallOffsets;
    /**
     * 最后一条规则。
     */
    private final ZoneOffsetTransitionRule[] lastRules;
    /**
     * 最近转换的映射。
     */
    private final transient ConcurrentMap<Integer, ZoneOffsetTransition[]> lastRulesCache =
                new ConcurrentHashMap<Integer, ZoneOffsetTransition[]>();
    /**
     * 零长度的 long 数组。
     */
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    /**
     * 零长度的 lastRules 数组。
     */
    private static final ZoneOffsetTransitionRule[] EMPTY_LASTRULES =
        new ZoneOffsetTransitionRule[0];
    /**
     * 零长度的 ldt 数组。
     */
    private static final LocalDateTime[] EMPTY_LDT_ARRAY = new LocalDateTime[0];

    /**
     * 获取 ZoneRules 的实例。
     *
     * @param baseStandardOffset  法定规则设置前使用的标准偏移量，不为空
     * @param baseWallOffset  法定规则设置前使用的墙面偏移量，不为空
     * @param standardOffsetTransitionList  标准偏移量的变化列表，不为空
     * @param transitionList  转换列表，不为空
     * @param lastRules  重复的最后规则，大小为 16 或更小，不为空
     * @return 时区规则，不为空
     */
    public static ZoneRules of(ZoneOffset baseStandardOffset,
                               ZoneOffset baseWallOffset,
                               List<ZoneOffsetTransition> standardOffsetTransitionList,
                               List<ZoneOffsetTransition> transitionList,
                               List<ZoneOffsetTransitionRule> lastRules) {
        Objects.requireNonNull(baseStandardOffset, "baseStandardOffset");
        Objects.requireNonNull(baseWallOffset, "baseWallOffset");
        Objects.requireNonNull(standardOffsetTransitionList, "standardOffsetTransitionList");
        Objects.requireNonNull(transitionList, "transitionList");
        Objects.requireNonNull(lastRules, "lastRules");
        return new ZoneRules(baseStandardOffset, baseWallOffset,
                             standardOffsetTransitionList, transitionList, lastRules);
    }

    /**
     * 获取具有固定时区规则的 ZoneRules 实例。
     *
     * @param offset  此固定时区规则基于的偏移量，不为空
     * @return 时区规则，不为空
     * @see #isFixedOffset()
     */
    public static ZoneRules of(ZoneOffset offset) {
        Objects.requireNonNull(offset, "offset");
        return new ZoneRules(offset);
    }

    /**
     * 创建实例。
     *
     * @param baseStandardOffset  法定规则设置前使用的标准偏移量，不为空
     * @param baseWallOffset  法定规则设置前使用的墙面偏移量，不为空
     * @param standardOffsetTransitionList  标准偏移量的变化列表，不为空
     * @param transitionList  转换列表，不为空
     * @param lastRules  重复的最后规则，大小为 16 或更小，不为空
     */
    ZoneRules(ZoneOffset baseStandardOffset,
              ZoneOffset baseWallOffset,
              List<ZoneOffsetTransition> standardOffsetTransitionList,
              List<ZoneOffsetTransition> transitionList,
              List<ZoneOffsetTransitionRule> lastRules) {
        super();

        // 转换标准偏移量

        this.standardTransitions = new long[standardOffsetTransitionList.size()];

        this.standardOffsets = new ZoneOffset[standardOffsetTransitionList.size() + 1];
        this.standardOffsets[0] = baseStandardOffset;
        for (int i = 0; i < standardOffsetTransitionList.size(); i++) {
            this.standardTransitions[i] = standardOffsetTransitionList.get(i).toEpochSecond();
            this.standardOffsets[i + 1] = standardOffsetTransitionList.get(i).getOffsetAfter();
        }

        // 转换节省时间的转换为本地时间
        List<LocalDateTime> localTransitionList = new ArrayList<>();
        List<ZoneOffset> localTransitionOffsetList = new ArrayList<>();
        localTransitionOffsetList.add(baseWallOffset);
        for (ZoneOffsetTransition trans : transitionList) {
            if (trans.isGap()) {
                localTransitionList.add(trans.getDateTimeBefore());
                localTransitionList.add(trans.getDateTimeAfter());
            } else {
                localTransitionList.add(trans.getDateTimeAfter());
                localTransitionList.add(trans.getDateTimeBefore());
            }
            localTransitionOffsetList.add(trans.getOffsetAfter());
        }
        this.savingsLocalTransitions = localTransitionList.toArray(new LocalDateTime[localTransitionList.size()]);
        this.wallOffsets = localTransitionOffsetList.toArray(new ZoneOffset[localTransitionOffsetList.size()]);

        // 转换节省时间的转换为时刻
        this.savingsInstantTransitions = new long[transitionList.size()];
        for (int i = 0; i < transitionList.size(); i++) {
            this.savingsInstantTransitions[i] = transitionList.get(i).toEpochSecond();
        }

        // 最后规则
        if (lastRules.size() > 16) {
            throw new IllegalArgumentException("Too many transition rules");
        }
        this.lastRules = lastRules.toArray(new ZoneOffsetTransitionRule[lastRules.size()]);
    }

    /**
     * 构造函数。
     *
     * @param standardTransitions  标准转换，不为空
     * @param standardOffsets  标准偏移量，不为空
     * @param savingsInstantTransitions  节约时间的转换，不为空
     * @param wallOffsets  墙面偏移量，不为空
     * @param lastRules  重复的最后规则，大小为 15 或更小，不为空
     */
    private ZoneRules(long[] standardTransitions,
                      ZoneOffset[] standardOffsets,
                      long[] savingsInstantTransitions,
                      ZoneOffset[] wallOffsets,
                      ZoneOffsetTransitionRule[] lastRules) {
        super();

        this.standardTransitions = standardTransitions;
        this.standardOffsets = standardOffsets;
        this.savingsInstantTransitions = savingsInstantTransitions;
        this.wallOffsets = wallOffsets;
        this.lastRules = lastRules;

        if (savingsInstantTransitions.length == 0) {
            this.savingsLocalTransitions = EMPTY_LDT_ARRAY;
        } else {
            // 转换节省时间的转换为本地时间
            List<LocalDateTime> localTransitionList = new ArrayList<>();
            for (int i = 0; i < savingsInstantTransitions.length; i++) {
                ZoneOffset before = wallOffsets[i];
                ZoneOffset after = wallOffsets[i + 1];
                ZoneOffsetTransition trans = new ZoneOffsetTransition(savingsInstantTransitions[i], before, after);
                if (trans.isGap()) {
                    localTransitionList.add(trans.getDateTimeBefore());
                    localTransitionList.add(trans.getDateTimeAfter());
                } else {
                    localTransitionList.add(trans.getDateTimeAfter());
                    localTransitionList.add(trans.getDateTimeBefore());
               }
            }
            this.savingsLocalTransitions = localTransitionList.toArray(new LocalDateTime[localTransitionList.size()]);
        }
    }

    /**
     * 创建具有固定时区规则的 ZoneRules 实例。
     *
     * @param offset  此固定时区规则基于的偏移量，不为空
     * @return 时区规则，不为空
     * @see #isFixedOffset()
     */
    private ZoneRules(ZoneOffset offset) {
        this.standardOffsets = new ZoneOffset[1];
        this.standardOffsets[0] = offset;
        this.standardTransitions = EMPTY_LONG_ARRAY;
        this.savingsInstantTransitions = EMPTY_LONG_ARRAY;
        this.savingsLocalTransitions = EMPTY_LDT_ARRAY;
        this.wallOffsets = standardOffsets;
        this.lastRules = EMPTY_LASTRULES;
    }

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
     * 使用
     * <a href="../../../serialized-form.html#java.time.zone.Ser">专用序列化形式</a> 写入对象。
     * @serialData
     * <pre style="font-size:1.0em">{@code
     *
     *   out.writeByte(1);  // 标识 ZoneRules
     *   out.writeInt(standardTransitions.length);
     *   for (long trans : standardTransitions) {
     *       Ser.writeEpochSec(trans, out);
     *   }
     *   for (ZoneOffset offset : standardOffsets) {
     *       Ser.writeOffset(offset, out);
     *   }
     *   out.writeInt(savingsInstantTransitions.length);
     *   for (long trans : savingsInstantTransitions) {
     *       Ser.writeEpochSec(trans, out);
     *   }
     *   for (ZoneOffset offset : wallOffsets) {
     *       Ser.writeOffset(offset, out);
     *   }
     *   out.writeByte(lastRules.length);
     *   for (ZoneOffsetTransitionRule rule : lastRules) {
     *       rule.writeExternal(out);
     *   }
     * }
     * </pre>
     * <p>
     * 用于偏移量的纪元秒值以可变长度形式编码，以使常见情况在流中占用更少的字节。
     * <pre style="font-size:1.0em">{@code
     *
     *  static void writeEpochSec(long epochSec, DataOutput out) throws IOException {
     *     if (epochSec >= -4575744000L && epochSec < 10413792000L && epochSec % 900 == 0) {  // 1825 年到 2300 年之间的每 15 分钟
     *         int store = (int) ((epochSec + 4575744000L) / 900);
     *         out.writeByte((store >>> 16) & 255);
     *         out.writeByte((store >>> 8) & 255);
     *         out.writeByte(store & 255);
     *      } else {
     *          out.writeByte(255);
     *          out.writeLong(epochSec);
     *      }
     *  }
     * }
     * </pre>
     * <p>
     * ZoneOffset 值以可变长度形式编码，以便常见情况在流中占用更少的字节。
     * <pre style="font-size:1.0em">{@code
     *
     *  static void writeOffset(ZoneOffset offset, DataOutput out) throws IOException {
     *     final int offsetSecs = offset.getTotalSeconds();
     *     int offsetByte = offsetSecs % 900 == 0 ? offsetSecs / 900 : 127;  // 压缩到 -72 到 +72
     *     out.writeByte(offsetByte);
     *     if (offsetByte == 127) {
     *         out.writeInt(offsetSecs);
     *     }
     * }
     *}
     * </pre>
     * @return 替换对象，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.ZRULES, this);
    }


                /**
     * 将状态写入流中。
     *
     * @param out  输出流，不为空
     * @throws IOException 如果发生错误
     */
    void writeExternal(DataOutput out) throws IOException {
        out.writeInt(standardTransitions.length);
        for (long trans : standardTransitions) {
            Ser.writeEpochSec(trans, out);
        }
        for (ZoneOffset offset : standardOffsets) {
            Ser.writeOffset(offset, out);
        }
        out.writeInt(savingsInstantTransitions.length);
        for (long trans : savingsInstantTransitions) {
            Ser.writeEpochSec(trans, out);
        }
        for (ZoneOffset offset : wallOffsets) {
            Ser.writeOffset(offset, out);
        }
        out.writeByte(lastRules.length);
        for (ZoneOffsetTransitionRule rule : lastRules) {
            rule.writeExternal(out);
        }
    }

    /**
     * 从流中读取状态。1,024 限制了 stdTrans 和 savSize 的长度，旨在足够大以容纳当前 tzdb 数据中的最大转换数量
     * （例如 Asia/Tehran 有 203 个转换）。
     *
     * @param in  输入流，不为空
     * @return 创建的对象，不为空
     * @throws IOException 如果发生错误
     */
    static ZoneRules readExternal(DataInput in) throws IOException, ClassNotFoundException {
        int stdSize = in.readInt();
        if (stdSize > 1024) {
            throw new InvalidObjectException("转换过多");
        }
        long[] stdTrans = (stdSize == 0) ? EMPTY_LONG_ARRAY
                                         : new long[stdSize];
        for (int i = 0; i < stdSize; i++) {
            stdTrans[i] = Ser.readEpochSec(in);
        }
        ZoneOffset[] stdOffsets = new ZoneOffset[stdSize + 1];
        for (int i = 0; i < stdOffsets.length; i++) {
            stdOffsets[i] = Ser.readOffset(in);
        }
        int savSize = in.readInt();
        if (savSize > 1024) {
            throw new InvalidObjectException("夏令时偏移过多");
        }
        long[] savTrans = (savSize == 0) ? EMPTY_LONG_ARRAY
                                         : new long[savSize];
        for (int i = 0; i < savSize; i++) {
            savTrans[i] = Ser.readEpochSec(in);
        }
        ZoneOffset[] savOffsets = new ZoneOffset[savSize + 1];
        for (int i = 0; i < savOffsets.length; i++) {
            savOffsets[i] = Ser.readOffset(in);
        }
        int ruleSize = in.readByte();
        if (ruleSize > 16) {
            throw new InvalidObjectException("转换规则过多");
        }
        ZoneOffsetTransitionRule[] rules = (ruleSize == 0) ?
            EMPTY_LASTRULES : new ZoneOffsetTransitionRule[ruleSize];
        for (int i = 0; i < ruleSize; i++) {
            rules[i] = ZoneOffsetTransitionRule.readExternal(in);
        }
        return new ZoneRules(stdTrans, stdOffsets, savTrans, savOffsets, rules);
    }

    /**
     * 检查时区规则是否固定，即偏移量从不变化。
     *
     * @return 如果时区固定且偏移量从不变化，则返回 true
     */
    public boolean isFixedOffset() {
        return savingsInstantTransitions.length == 0;
    }

    /**
     * 获取在这些规则中指定时间点适用的偏移量。
     * <p>
     * 从时间点到偏移量的映射很简单，每个时间点只有一个有效的偏移量。
     * 该方法返回该偏移量。
     *
     * @param instant  要查找偏移量的时间点，不为空，但如果规则对所有时间点都有单一偏移量，则可能忽略 null
     * @return 偏移量，不为空
     */
    public ZoneOffset getOffset(Instant instant) {
        if (savingsInstantTransitions.length == 0) {
            return standardOffsets[0];
        }
        long epochSec = instant.getEpochSecond();
        // 检查是否使用最后的规则
        if (lastRules.length > 0 &&
                epochSec > savingsInstantTransitions[savingsInstantTransitions.length - 1]) {
            int year = findYear(epochSec, wallOffsets[wallOffsets.length - 1]);
            ZoneOffsetTransition[] transArray = findTransitionArray(year);
            ZoneOffsetTransition trans = null;
            for (int i = 0; i < transArray.length; i++) {
                trans = transArray[i];
                if (epochSec < trans.toEpochSecond()) {
                    return trans.getOffsetBefore();
                }
            }
            return trans.getOffsetAfter();
        }

        // 使用历史规则
        int index  = Arrays.binarySearch(savingsInstantTransitions, epochSec);
        if (index < 0) {
            // 将负的插入位置转换为匹配范围的起始位置
            index = -index - 2;
        }
        return wallOffsets[index + 1];
    }

    /**
     * 获取在这些规则中指定本地日期时间适用的偏移量。
     * <p>
     * 从本地日期时间到偏移量的映射并不简单。有三种情况：
     * <ul>
     * <li>正常，有一个有效的偏移量。对于一年中的大部分时间，正常情况适用，即本地日期时间有一个单一的有效偏移量。</li>
     * <li>跳跃，没有有效的偏移量。这是当钟表向前跳动时，通常由于春季夏令时从“冬季”变为“夏季”。
     *  在跳跃期间，本地日期时间值没有有效的偏移量。</li>
     * <li>重叠，有两个有效的偏移量。这是当钟表向后调整时，通常由于秋季夏令时从“夏季”变为“冬季”。
     *  在重叠期间，本地日期时间值有两个有效的偏移量。</li>
     * </ul>
     * 因此，对于任何给定的本地日期时间，可以有零个、一个或两个有效的偏移量。
     * 该方法在正常情况下返回单个偏移量，在跳跃或重叠情况下返回转换前的偏移量。
     * <p>
     * 由于在跳跃和重叠情况下返回的偏移量是一个“最佳”值，而不是“正确”值，因此应谨慎对待。
     * 关心正确偏移量的应用程序应结合使用此方法、{@link #getValidOffsets(LocalDateTime)} 和 {@link #getTransition(LocalDateTime)}。
     *
     * @param localDateTime  要查询的本地日期时间，不为空，但如果规则对所有时间点都有单一偏移量，则可能忽略 null
     * @return 本地日期时间的最佳可用偏移量，不为空
     */
    public ZoneOffset getOffset(LocalDateTime localDateTime) {
        Object info = getOffsetInfo(localDateTime);
        if (info instanceof ZoneOffsetTransition) {
            return ((ZoneOffsetTransition) info).getOffsetBefore();
        }
        return (ZoneOffset) info;
    }

    /**
     * 获取在这些规则中指定本地日期时间适用的偏移量。
     * <p>
     * 从本地日期时间到偏移量的映射并不简单。有三种情况：
     * <ul>
     * <li>正常，有一个有效的偏移量。对于一年中的大部分时间，正常情况适用，即本地日期时间有一个单一的有效偏移量。</li>
     * <li>跳跃，没有有效的偏移量。这是当钟表向前跳动时，通常由于春季夏令时从“冬季”变为“夏季”。
     *  在跳跃期间，本地日期时间值没有有效的偏移量。</li>
     * <li>重叠，有两个有效的偏移量。这是当钟表向后调整时，通常由于秋季夏令时从“夏季”变为“冬季”。
     *  在重叠期间，本地日期时间值有两个有效的偏移量。</li>
     * </ul>
     * 因此，对于任何给定的本地日期时间，可以有零个、一个或两个有效的偏移量。
     * 该方法返回有效偏移量的列表，该列表的大小为 0、1 或 2。
     * 在有两个偏移量的情况下，较早的偏移量在索引 0 处返回，较晚的偏移量在索引 1 处返回。
     * <p>
     * 从 {@code LocalDateTime} 转换有多种处理方式。一种技术，使用此方法，如下所示：
     * <pre>
     *  List&lt;ZoneOffset&gt; validOffsets = rules.getOffset(localDT);
     *  if (validOffsets.size() == 1) {
     *    // 正常情况：只有一个有效的偏移量
     *    zoneOffset = validOffsets.get(0);
     *  } else {
     *    // 跳跃或重叠：从转换（非空）中确定要做什么
     *    ZoneOffsetTransition trans = rules.getTransition(localDT);
     *  }
     * </pre>
     * <p>
     * 从理论上讲，可能有超过两个有效的偏移量。
     * 这将发生在钟表在短时间内多次向后调整的情况下。
     * 这在时区历史上从未发生过，因此没有特殊处理。
     * 但是，如果发生这种情况，列表将返回超过 2 个条目。
     *
     * @param localDateTime  要查询有效偏移量的本地日期时间，不为空，但如果规则对所有时间点都有单一偏移量，则可能忽略 null
     * @return 有效偏移量的列表，可能是不可变的，不为空
     */
    public List<ZoneOffset> getValidOffsets(LocalDateTime localDateTime) {
        // 应该进行优化
        Object info = getOffsetInfo(localDateTime);
        if (info instanceof ZoneOffsetTransition) {
            return ((ZoneOffsetTransition) info).getValidOffsets();
        }
        return Collections.singletonList((ZoneOffset) info);
    }

    /**
     * 获取在这些规则中指定本地日期时间适用的偏移量转换。
     * <p>
     * 从本地日期时间到偏移量的映射并不简单。有三种情况：
     * <ul>
     * <li>正常，有一个有效的偏移量。对于一年中的大部分时间，正常情况适用，即本地日期时间有一个单一的有效偏移量。</li>
     * <li>跳跃，没有有效的偏移量。这是当钟表向前跳动时，通常由于春季夏令时从“冬季”变为“夏季”。
     *  在跳跃期间，本地日期时间值没有有效的偏移量。</li>
     * <li>重叠，有两个有效的偏移量。这是当钟表向后调整时，通常由于秋季夏令时从“夏季”变为“冬季”。
     *  在重叠期间，本地日期时间值有两个有效的偏移量。</li>
     * </ul>
     * 转换用于建模跳跃或重叠的情况。
     * 正常情况下将返回 null。
     * <p>
     * 从 {@code LocalDateTime} 转换有多种处理方式。一种技术，使用此方法，如下所示：
     * <pre>
     *  ZoneOffsetTransition trans = rules.getTransition(localDT);
     *  if (trans == null) {
     *    // 跳跃或重叠：从转换中确定要做什么
     *  } else {
     *    // 正常情况：只有一个有效的偏移量
     *    zoneOffset = rule.getOffset(localDT);
     *  }
     * </pre>
     *
     * @param localDateTime  要查询偏移量转换的本地日期时间，不为空，但如果规则对所有时间点都有单一偏移量，则可能忽略 null
     * @return 偏移量转换，如果本地日期时间不在转换中，则返回 null
     */
    public ZoneOffsetTransition getTransition(LocalDateTime localDateTime) {
        Object info = getOffsetInfo(localDateTime);
        return (info instanceof ZoneOffsetTransition ? (ZoneOffsetTransition) info : null);
    }

    private Object getOffsetInfo(LocalDateTime dt) {
        if (savingsInstantTransitions.length == 0) {
            return standardOffsets[0];
        }
        // 检查是否使用最后的规则
        if (lastRules.length > 0 &&
                dt.isAfter(savingsLocalTransitions[savingsLocalTransitions.length - 1])) {
            ZoneOffsetTransition[] transArray = findTransitionArray(dt.getYear());
            Object info = null;
            for (ZoneOffsetTransition trans : transArray) {
                info = findOffsetInfo(dt, trans);
                if (info instanceof ZoneOffsetTransition || info.equals(trans.getOffsetBefore())) {
                    return info;
                }
            }
            return info;
        }

        // 使用历史规则
        int index  = Arrays.binarySearch(savingsLocalTransitions, dt);
        if (index == -1) {
            // 在第一个转换之前
            return wallOffsets[0];
        }
        if (index < 0) {
            // 将负的插入位置转换为匹配范围的起始位置
            index = -index - 2;
        } else if (index < savingsLocalTransitions.length - 1 &&
                savingsLocalTransitions[index].equals(savingsLocalTransitions[index + 1])) {
            // 处理紧随跳跃之后的重叠
            index++;
        }
        if ((index & 1) == 0) {
            // 跳跃或重叠
            LocalDateTime dtBefore = savingsLocalTransitions[index];
            LocalDateTime dtAfter = savingsLocalTransitions[index + 1];
            ZoneOffset offsetBefore = wallOffsets[index / 2];
            ZoneOffset offsetAfter = wallOffsets[index / 2 + 1];
            if (offsetAfter.getTotalSeconds() > offsetBefore.getTotalSeconds()) {
                // 跳跃
                return new ZoneOffsetTransition(dtBefore, offsetBefore, offsetAfter);
            } else {
                // 重叠
                return new ZoneOffsetTransition(dtAfter, offsetBefore, offsetAfter);
            }
        } else {
            // 正常（既不是跳跃也不是重叠）
            return wallOffsets[index / 2 + 1];
        }
    }

    /**
     * 查找本地日期时间和转换的偏移量信息。
     *
     * @param dt  日期时间，不为空
     * @param trans  转换，不为空
     * @return 偏移量信息，不为空
     */
    private Object findOffsetInfo(LocalDateTime dt, ZoneOffsetTransition trans) {
        LocalDateTime localTransition = trans.getDateTimeBefore();
        if (trans.isGap()) {
            if (dt.isBefore(localTransition)) {
                return trans.getOffsetBefore();
            }
            if (dt.isBefore(trans.getDateTimeAfter())) {
                return trans;
            } else {
                return trans.getOffsetAfter();
            }
        } else {
            if (dt.isBefore(localTransition) == false) {
                return trans.getOffsetAfter();
            }
            if (dt.isBefore(trans.getDateTimeAfter())) {
                return trans.getOffsetBefore();
            } else {
                return trans;
            }
        }
    }

    /**
     * 查找给定年份的适当转换数组。
     *
     * @param year  年份，不为空
     * @return 转换数组，不为空
     */
    private ZoneOffsetTransition[] findTransitionArray(int year) {
        Integer yearObj = year;  // 应使用 Year 类，但这样可以节省类加载
        ZoneOffsetTransition[] transArray = lastRulesCache.get(yearObj);
        if (transArray != null) {
            return transArray;
        }
        ZoneOffsetTransitionRule[] ruleArray = lastRules;
        transArray  = new ZoneOffsetTransition[ruleArray.length];
        for (int i = 0; i < ruleArray.length; i++) {
            transArray[i] = ruleArray[i].createTransition(year);
        }
        if (year < LAST_CACHED_YEAR) {
            lastRulesCache.putIfAbsent(yearObj, transArray);
        }
        return transArray;
    }


                /**
     * 获取此区域在指定时间的标准偏移量。
     * <p>
     * 此方法提供了访问历史信息，说明标准偏移量如何随时间变化。
     * 标准偏移量是在应用夏令时之前的偏移量。
     * 这通常是冬季适用的偏移量。
     *
     * @param instant  要查找偏移信息的时间点，不允许为 null，但如果规则对所有时间点都有单一偏移量，则可能忽略 null
     * @return 标准偏移量，不允许为 null
     */
    public ZoneOffset getStandardOffset(Instant instant) {
        if (savingsInstantTransitions.length == 0) {
            return standardOffsets[0];
        }
        long epochSec = instant.getEpochSecond();
        int index  = Arrays.binarySearch(standardTransitions, epochSec);
        if (index < 0) {
            // 将负插入位置转换为匹配范围的起始位置
            index = -index - 2;
        }
        return standardOffsets[index + 1];
    }

    /**
     * 获取此区域在指定时间使用的夏令时偏移量。
     * <p>
     * 此方法提供了访问历史信息，说明夏令时偏移量如何随时间变化。
     * 这是标准偏移量和实际偏移量之间的差值。
     * 通常在冬季偏移量为零，在夏季为一小时。
     * 时区基于秒，因此持续时间的纳秒部分为零。
     * <p>
     * 此默认实现从 {@link #getOffset(java.time.Instant) 实际} 和
     * {@link #getStandardOffset(java.time.Instant) 标准} 偏移量计算持续时间。
     *
     * @param instant  要查找夏令时的时间点，不允许为 null，但如果规则对所有时间点都有单一偏移量，则可能忽略 null
     * @return 标准偏移量和实际偏移量之间的差值，不允许为 null
     */
    public Duration getDaylightSavings(Instant instant) {
        if (savingsInstantTransitions.length == 0) {
            return Duration.ZERO;
        }
        ZoneOffset standardOffset = getStandardOffset(instant);
        ZoneOffset actualOffset = getOffset(instant);
        return Duration.ofSeconds(actualOffset.getTotalSeconds() - standardOffset.getTotalSeconds());
    }

    /**
     * 检查指定时间点是否处于夏令时。
     * <p>
     * 此方法检查指定时间点的标准偏移量和实际偏移量是否相同。
     * 如果不同，则认为夏令时正在生效。
     * <p>
     * 此默认实现比较 {@link #getOffset(java.time.Instant) 实际} 和
     * {@link #getStandardOffset(java.time.Instant) 标准} 偏移量。
     *
     * @param instant  要查找偏移信息的时间点，不允许为 null，但如果规则对所有时间点都有单一偏移量，则可能忽略 null
     * @return 标准偏移量，不允许为 null
     */
    public boolean isDaylightSavings(Instant instant) {
        return (getStandardOffset(instant).equals(getOffset(instant)) == false);
    }

    /**
     * 检查指定的本地日期时间和偏移量是否对此规则有效。
     * <p>
     * 要有效，本地日期时间不能处于间隙中，且偏移量必须匹配其中一个有效偏移量。
     * <p>
     * 此默认实现检查 {@link #getValidOffsets(java.time.LocalDateTime)}
     * 是否包含指定的偏移量。
     *
     * @param localDateTime  要检查的日期时间，不允许为 null，但如果规则对所有时间点都有单一偏移量，则可能忽略 null
     * @param offset  要检查的偏移量，null 返回 false
     * @return 如果偏移日期时间对此规则有效，则返回 true
     */
    public boolean isValidOffset(LocalDateTime localDateTime, ZoneOffset offset) {
        return getValidOffsets(localDateTime).contains(offset);
    }

    /**
     * 获取指定时间点之后的下一个转换。
     * <p>
     * 此方法返回指定时间点之后的下一个转换的详细信息。
     * 例如，如果时间点表示“夏季”夏令时适用的时间点，则该方法将返回转换到下一个“冬季”时间的转换。
     *
     * @param instant  要获取下一个转换的时间点，不允许为 null，但如果规则对所有时间点都有单一偏移量，则可能忽略 null
     * @return 指定时间点之后的下一个转换，如果这是最后一个转换之后，则返回 null
     */
    public ZoneOffsetTransition nextTransition(Instant instant) {
        if (savingsInstantTransitions.length == 0) {
            return null;
        }
        long epochSec = instant.getEpochSecond();
        // 检查是否使用最后的规则
        if (epochSec >= savingsInstantTransitions[savingsInstantTransitions.length - 1]) {
            if (lastRules.length == 0) {
                return null;
            }
            // 搜索时间点所在的年份
            int year = findYear(epochSec, wallOffsets[wallOffsets.length - 1]);
            ZoneOffsetTransition[] transArray = findTransitionArray(year);
            for (ZoneOffsetTransition trans : transArray) {
                if (epochSec < trans.toEpochSecond()) {
                    return trans;
                }
            }
            // 使用下一年的第一个转换
            if (year < Year.MAX_VALUE) {
                transArray = findTransitionArray(year + 1);
                return transArray[0];
            }
            return null;
        }

        // 使用历史规则
        int index  = Arrays.binarySearch(savingsInstantTransitions, epochSec);
        if (index < 0) {
            index = -index - 1;  // 转换值是下一个转换
        } else {
            index += 1;  // 完全匹配，因此需要加一以获取下一个
        }
        return new ZoneOffsetTransition(savingsInstantTransitions[index], wallOffsets[index], wallOffsets[index + 1]);
    }

    /**
     * 获取指定时间点之前的上一个转换。
     * <p>
     * 此方法返回指定时间点之前的上一个转换的详细信息。
     * 例如，如果时间点表示“夏季”夏令时适用的时间点，则该方法将返回从上一个“冬季”时间的转换。
     *
     * @param instant  要获取上一个转换的时间点，不允许为 null，但如果规则对所有时间点都有单一偏移量，则可能忽略 null
     * @return 指定时间点之前的上一个转换，如果这是第一个转换之前，则返回 null
     */
    public ZoneOffsetTransition previousTransition(Instant instant) {
        if (savingsInstantTransitions.length == 0) {
            return null;
        }
        long epochSec = instant.getEpochSecond();
        if (instant.getNano() > 0 && epochSec < Long.MAX_VALUE) {
            epochSec += 1;  // 允许方法的其余部分仅使用秒
        }

        // 检查是否使用最后的规则
        long lastHistoric = savingsInstantTransitions[savingsInstantTransitions.length - 1];
        if (lastRules.length > 0 && epochSec > lastHistoric) {
            // 搜索时间点所在的年份
            ZoneOffset lastHistoricOffset = wallOffsets[wallOffsets.length - 1];
            int year = findYear(epochSec, lastHistoricOffset);
            ZoneOffsetTransition[] transArray = findTransitionArray(year);
            for (int i = transArray.length - 1; i >= 0; i--) {
                if (epochSec > transArray[i].toEpochSecond()) {
                    return transArray[i];
                }
            }
            // 使用前一年的最后一个转换
            int lastHistoricYear = findYear(lastHistoric, lastHistoricOffset);
            if (--year > lastHistoricYear) {
                transArray = findTransitionArray(year);
                return transArray[transArray.length - 1];
            }
            // 继续
        }

        // 使用历史规则
        int index  = Arrays.binarySearch(savingsInstantTransitions, epochSec);
        if (index < 0) {
            index = -index - 1;
        }
        if (index <= 0) {
            return null;
        }
        return new ZoneOffsetTransition(savingsInstantTransitions[index - 1], wallOffsets[index - 1], wallOffsets[index]);
    }

    private int findYear(long epochSecond, ZoneOffset offset) {
        // 为性能内联
        long localSecond = epochSecond + offset.getTotalSeconds();
        long localEpochDay = Math.floorDiv(localSecond, 86400);
        return LocalDate.ofEpochDay(localEpochDay).getYear();
    }

    /**
     * 获取完全定义的转换的完整列表。
     * <p>
     * 此规则实例的完整转换集由此方法和 {@link #getTransitionRules()} 定义。此方法返回那些已完全定义的转换。
     * 这些通常是历史的，但也可能在未来。
     * <p>
     * 对于固定偏移规则和只有单一偏移量的时区，列表将为空。如果转换规则未知，列表也将为空。
     *
     * @return 完全定义的转换的不可变列表，不允许为 null
     */
    public List<ZoneOffsetTransition> getTransitions() {
        List<ZoneOffsetTransition> list = new ArrayList<>();
        for (int i = 0; i < savingsInstantTransitions.length; i++) {
            list.add(new ZoneOffsetTransition(savingsInstantTransitions[i], wallOffsets[i], wallOffsets[i + 1]));
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * 获取超出转换列表定义年份的转换规则列表。
     * <p>
     * 此规则实例的完整转换集由此方法和 {@link #getTransitions()} 定义。此方法返回 {@link ZoneOffsetTransitionRule}
     * 的实例，这些实例定义了转换发生的时间算法。
     * <p>
     * 对于任何给定的 {@code ZoneRules}，此列表包含超出已完全定义年份的转换规则。这些规则通常涉及未来的夏令时规则变化。
     * <p>
     * 如果时区定义了未来的夏令时，则列表通常大小为两个，包含关于进入和退出夏令时的信息。如果时区没有夏令时，或者关于未来变化的信息不确定，则列表将为空。
     * <p>
     * 对于固定偏移规则和没有夏令时的时区，列表将为空。如果转换规则未知，列表也将为空。
     *
     * @return 转换规则的不可变列表，不允许为 null
     */
    public List<ZoneOffsetTransitionRule> getTransitionRules() {
        return Collections.unmodifiableList(Arrays.asList(lastRules));
    }

    /**
     * 检查此规则集是否等于另一个规则集。
     * <p>
     * 如果两个规则集对任何给定的输入时间点或本地日期时间总是产生相同的输出，则它们是相等的。
     * 来自不同组的规则即使实际上是相同的，也可能返回 false。
     * <p>
     * 此定义应导致实现比较其整个状态。
     *
     * @param otherRules  其他规则集，null 返回 false
     * @return 如果此规则集与指定的规则集相同，则返回 true
     */
    @Override
    public boolean equals(Object otherRules) {
        if (this == otherRules) {
           return true;
        }
        if (otherRules instanceof ZoneRules) {
            ZoneRules other = (ZoneRules) otherRules;
            return Arrays.equals(standardTransitions, other.standardTransitions) &&
                    Arrays.equals(standardOffsets, other.standardOffsets) &&
                    Arrays.equals(savingsInstantTransitions, other.savingsInstantTransitions) &&
                    Arrays.equals(wallOffsets, other.wallOffsets) &&
                    Arrays.equals(lastRules, other.lastRules);
        }
        return false;
    }

    /**
     * 根据 {@code #equals} 的定义返回合适的哈希码。
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(standardTransitions) ^
                Arrays.hashCode(standardOffsets) ^
                Arrays.hashCode(savingsInstantTransitions) ^
                Arrays.hashCode(wallOffsets) ^
                Arrays.hashCode(lastRules);
    }

    /**
     * 返回描述此对象的字符串。
     *
     * @return 用于调试的字符串，不允许为 null
     */
    @Override
    public String toString() {
        return "ZoneRules[currentStandardOffset=" + standardOffsets[standardOffsets.length - 1] + "]";
    }

}
