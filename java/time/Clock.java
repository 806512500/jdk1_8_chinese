
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

import static java.time.LocalTime.NANOS_PER_MINUTE;
import static java.time.LocalTime.NANOS_PER_SECOND;

import java.io.Serializable;
import java.util.Objects;
import java.util.TimeZone;

/**
 * 提供访问当前时间戳、日期和时间的时钟，使用时区。
 * <p>
 * 该类的实例用于查找当前时间戳，可以使用存储的时区解释为当前日期和时间。
 * 因此，时钟可以用于替代 {@link System#currentTimeMillis()}
 * 和 {@link TimeZone#getDefault()}。
 * <p>
 * 使用 {@code Clock} 是可选的。所有关键日期时间类也有一个
 * {@code now()} 工厂方法，使用系统时钟和默认时区。
 * 该抽象的主要目的是允许在需要时插入备用时钟。应用程序使用对象来获取当前时间，
 * 而不是使用静态方法。这可以简化测试。
 * <p>
 * 应用程序的最佳实践是将 {@code Clock} 传递给任何需要当前时间戳的方法。
 * 依赖注入框架是实现这一点的一种方式：
 * <pre>
 *  public class MyBean {
 *    private Clock clock;  // 依赖注入
 *    ...
 *    public void process(LocalDate eventDate) {
 *      if (eventDate.isBefore(LocalDate.now(clock)) {
 *        ...
 *      }
 *    }
 *  }
 * </pre>
 * 这种方法允许在测试期间使用 {@link #fixed(Instant, ZoneId) 固定} 或
 * {@link #offset(Clock, Duration) 偏移} 时钟。
 * <p>
 * {@code system} 工厂方法提供基于最佳可用系统时钟的时钟。这可能使用
 * {@link System#currentTimeMillis()}，或者如果可用的话，使用更高分辨率的时钟。
 *
 * @implSpec
 * 必须谨慎实现此抽象类以确保其他类正确运行。
 * 所有可以实例化的实现必须是最终的、不可变的和线程安全的。
 * <p>
 * 主要方法定义允许抛出异常。
 * 在正常使用中，不会抛出任何异常，但一种可能的实现是从中央时间服务器通过网络获取时间。
 * 显然，在这种情况下，查找可能会失败，因此允许方法抛出异常。
 * <p>
 * 从 {@code Clock} 返回的时间戳在时间尺度上忽略闰秒，如 {@link Instant} 所述。
 * 如果实现包装了提供闰秒信息的源，则应使用某种机制来“平滑”闰秒。
 * Java 时间尺度要求使用 UTC-SLS，但时钟实现可以选择如何准确地遵循时间尺度，
 * 只要它们记录了它们的工作方式。实现不需要实际执行 UTC-SLS 调整或以其他方式意识到闰秒。
 * <p>
 * 实现应尽可能实现 {@code Serializable} 并且必须记录是否支持序列化。
 *
 * @implNote
 * 提供的时钟实现基于 {@link System#currentTimeMillis()}。
 * 该方法对时钟的准确性几乎没有保证。
 * 需要更准确时钟的应用程序必须自己实现此抽象类，使用不同的外部时钟，如 NTP 服务器。
 *
 * @since 1.8
 */
public abstract class Clock {

    /**
     * 获取一个时钟，返回使用最佳可用系统时钟的当前时间戳，并使用 UTC 时区转换为日期和时间。
     * <p>
     * 该时钟，而不是 {@link #systemDefaultZone()}，应在需要当前时间戳但不需要日期或时间时使用。
     * <p>
     * 该时钟基于最佳可用系统时钟。这可能使用 {@link System#currentTimeMillis()}，
 * 或者如果可用的话，使用更高分辨率的时钟。
     * <p>
     * 从时间戳转换为日期或时间使用 {@linkplain ZoneOffset#UTC UTC 时区}。
     * <p>
     * 返回的实现是不可变的、线程安全的且 {@code Serializable}。
     * 它等同于 {@code system(ZoneOffset.UTC)}。
     *
     * @return 使用 UTC 时区的最佳可用系统时钟的时钟，不为空
     */
    public static Clock systemUTC() {
        return new SystemClock(ZoneOffset.UTC);
    }

    /**
     * 获取一个时钟，返回使用最佳可用系统时钟的当前时间戳，并使用默认时区转换为日期和时间。
     * <p>
     * 该时钟基于最佳可用系统时钟。这可能使用 {@link System#currentTimeMillis()}，
 * 或者如果可用的话，使用更高分辨率的时钟。
     * <p>
     * 使用此方法会将对默认时区的依赖硬编码到您的应用程序中。
     * 建议避免这种情况，并尽可能使用特定的时区。
     * 当需要当前时间戳但不需要日期或时间时，应使用 {@link #systemUTC() UTC 时钟}。
     * <p>
     * 返回的实现是不可变的、线程安全的且 {@code Serializable}。
     * 它等同于 {@code system(ZoneId.systemDefault())}。
     *
     * @return 使用默认时区的最佳可用系统时钟的时钟，不为空
     * @see ZoneId#systemDefault()
     */
    public static Clock systemDefaultZone() {
        return new SystemClock(ZoneId.systemDefault());
    }

    /**
     * 获取一个时钟，返回使用最佳可用系统时钟的当前时间戳。
     * <p>
     * 该时钟基于最佳可用系统时钟。这可能使用 {@link System#currentTimeMillis()}，
 * 或者如果可用的话，使用更高分辨率的时钟。
     * <p>
     * 从时间戳转换为日期或时间使用指定的时区。
     * <p>
     * 返回的实现是不可变的、线程安全的且 {@code Serializable}。
     *
     * @param zone  用于将时间戳转换为日期时间的时区，不为空
     * @return 使用指定时区的最佳可用系统时钟的时钟，不为空
     */
    public static Clock system(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        return new SystemClock(zone);
    }

    //-------------------------------------------------------------------------
    /**
     * 获取一个时钟，返回使用最佳可用系统时钟的当前时间戳，每秒跳动一次。
     * <p>
     * 该时钟的纳秒字段始终设置为零。这确保了可见时间每秒跳动一次。
     * 底层时钟是最佳可用系统时钟，等同于使用 {@link #system(ZoneId)}。
     * <p>
     * 实现可能出于性能原因使用缓存策略。因此，通过此时钟观察到的秒的开始
 * 可能会晚于通过底层时钟直接观察到的秒的开始。
     * <p>
     * 返回的实现是不可变的、线程安全的且 {@code Serializable}。
     * 它等同于 {@code tick(system(zone), Duration.ofSeconds(1))}。
     *
     * @param zone  用于将时间戳转换为日期时间的时区，不为空
     * @return 使用指定时区每秒跳动一次的时钟，不为空
     */
    public static Clock tickSeconds(ZoneId zone) {
        return new TickClock(system(zone), NANOS_PER_SECOND);
    }

    /**
     * 获取一个时钟，返回使用最佳可用系统时钟的当前时间戳，每分钟跳动一次。
     * <p>
     * 该时钟的纳秒字段和秒字段始终设置为零。这确保了可见时间每分钟跳动一次。
     * 底层时钟是最佳可用系统时钟，等同于使用 {@link #system(ZoneId)}。
     * <p>
     * 实现可能出于性能原因使用缓存策略。因此，通过此钟观察到的分钟的开始
 * 可能会晚于通过底层时钟直接观察到的分钟的开始。
     * <p>
     * 返回的实现是不可变的、线程安全的且 {@code Serializable}。
     * 它等同于 {@code tick(system(zone), Duration.ofMinutes(1))}。
     *
     * @param zone  用于将时间戳转换为日期时间的时区，不为空
     * @return 使用指定时区每分钟跳动一次的时钟，不为空
     */
    public static Clock tickMinutes(ZoneId zone) {
        return new TickClock(system(zone), NANOS_PER_MINUTE);
    }

    /**
     * 获取一个时钟，返回从指定时钟截断到指定持续时间最近发生的时间戳。
     * <p>
     * 该时钟将仅按指定的持续时间跳动。因此，如果持续时间是半秒，时钟将返回截断到半秒的时间戳。
     * <p>
     * 跳动持续时间必须为正。如果它有小于一毫秒的部分，则整个持续时间必须能够被一秒钟整除。
     * 所有正常的跳动持续时间都将符合这些标准，包括任何小时、分钟、秒和毫秒的倍数，
 * 以及合理的纳秒持续时间，如 20ns、250,000ns 和 500,000ns。
     * <p>
     * 零或一纳秒的持续时间将没有截断效果。传递这些值将返回底层时钟。
     * <p>
     * 实现可能出于性能原因使用缓存策略。因此，通过此钟观察到的请求持续时间的开始
 * 可能会晚于通过底层时钟直接观察到的持续时间的开始。
     * <p>
     * 返回的实现是不可变的、线程安全的且 {@code Serializable}，前提是基础时钟也是如此。
     *
     * @param baseClock  用于基于跳动时钟的基础时钟，不为空
     * @param tickDuration  每个可见跳动的持续时间，不为负，不为空
     * @return 以持续时间为单位跳动的时钟，不为空
     * @throws IllegalArgumentException 如果持续时间为负，或者有小于一毫秒的部分，使得整个持续时间不能被一秒钟整除
     * @throws ArithmeticException 如果持续时间太大而无法表示为纳秒
     */
    public static Clock tick(Clock baseClock, Duration tickDuration) {
        Objects.requireNonNull(baseClock, "baseClock");
        Objects.requireNonNull(tickDuration, "tickDuration");
        if (tickDuration.isNegative()) {
            throw new IllegalArgumentException("跳动持续时间不能为负");
        }
        long tickNanos = tickDuration.toNanos();
        if (tickNanos % 1000_000 == 0) {
            // 好的，没有毫秒的小数部分
        } else if (1000_000_000 % tickNanos == 0) {
            // 好的，可以被一秒钟整除
        } else {
            throw new IllegalArgumentException("无效的跳动持续时间");
        }
        if (tickNanos <= 1) {
            return baseClock;
        }
        return new TickClock(baseClock, tickNanos);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取一个始终返回相同时间戳的时钟。
     * <p>
     * 该时钟仅返回指定的时间戳。因此，它不是传统意义上的时钟。
     * 主要用例是在测试中，固定的时钟确保测试不依赖于当前时钟。
     * <p>
     * 返回的实现是不可变的、线程安全的且 {@code Serializable}。
     *
     * @param fixedInstant  用作时钟的时间戳，不为空
     * @param zone  用于将时间戳转换为日期时间的时区，不为空
     * @return 始终返回相同时间戳的时钟，不为空
     */
    public static Clock fixed(Instant fixedInstant, ZoneId zone) {
        Objects.requireNonNull(fixedInstant, "fixedInstant");
        Objects.requireNonNull(zone, "zone");
        return new FixedClock(fixedInstant, zone);
    }

    //-------------------------------------------------------------------------
    /**
     * 获取一个时钟，返回从指定时钟加上指定持续时间的时间戳。
     * <p>
     * 该时钟包装另一个时钟，返回加上指定持续时间的时间戳。如果持续时间为负，时间戳将
 * 早于当前日期和时间。
     * 主要用例是模拟运行在未来或过去。
     * <p>
     * 零持续时间将没有偏移效果。传递零将返回基础时钟。
     * <p>
     * 返回的实现是不可变的、线程安全的且 {@code Serializable}，前提是基础时钟也是如此。
     *
     * @param baseClock  要添加持续时间的基础时钟，不为空
     * @param offsetDuration  要添加的持续时间，不为空
     * @return 基于基础时钟并加上持续时间的时钟，不为空
     */
    public static Clock offset(Clock baseClock, Duration offsetDuration) {
        Objects.requireNonNull(baseClock, "baseClock");
        Objects.requireNonNull(offsetDuration, "offsetDuration");
        if (offsetDuration.equals(Duration.ZERO)) {
            return baseClock;
        }
        return new OffsetClock(baseClock, offsetDuration);
    }


                //-----------------------------------------------------------------------
    /**
     * 由子类访问的构造函数。
     */
    protected Clock() {
    }

    //-----------------------------------------------------------------------
    /**
     * 获取用于创建日期和时间的时区。
     * <p>
     * 时钟通常会获取当前时刻，然后使用时区将其转换为日期或时间。此方法返回用于解释时刻的时区。
     *
     * @return 用于解释时刻的时区，不为空
     */
    public abstract ZoneId getZone();

    /**
     * 返回具有不同时区的此时钟的副本。
     * <p>
     * 时钟通常会获取当前时刻，然后使用时区将其转换为日期或时间。此方法返回具有相似属性但使用不同时区的时钟。
     *
     * @param zone  要更改的时区，不为空
     * @return 基于此时钟并使用指定时区的时钟，不为空
     */
    public abstract Clock withZone(ZoneId zone);

    //-------------------------------------------------------------------------
    /**
     * 获取此时钟的当前毫秒时刻。
     * <p>
     * 这返回基于1970-01-01T00:00Z (UTC)的毫秒时刻。
     * 这等同于 {@link System#currentTimeMillis()} 的定义。
     * <p>
     * 大多数应用程序应避免使用此方法，而使用 {@link Instant} 来表示时间线上的时刻，而不是原始的毫秒值。
     * 提供此方法是为了在高性能用例中使用时钟，其中创建对象是不可接受的。
     * <p>
     * 当前的默认实现调用 {@link #instant}。
     *
     * @return 从1970-01-01T00:00Z (UTC)开始的此时钟的当前毫秒时刻，不为空
     * @throws DateTimeException 如果无法获取时刻，大多数实现不会抛出此异常
     */
    public long millis() {
        return instant().toEpochMilli();
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此时钟的当前时刻。
     * <p>
     * 这返回一个表示此时钟定义的当前时刻的瞬间。
     *
     * @return 从此时钟获取的当前时刻，不为空
     * @throws DateTimeException 如果无法获取时刻，大多数实现不会抛出此异常
     */
    public abstract Instant instant();

    //-----------------------------------------------------------------------
    /**
     * 检查此钟是否等于另一个钟。
     * <p>
     * 时钟应重写此方法以基于其状态进行比较，并满足 {@link Object#equals} 的契约。
     * 如果未重写，则行为由 {@link Object#equals} 定义。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此钟等于另一个钟，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * 此时钟的哈希码。
     * <p>
     * 时钟应基于其状态重写此方法，并满足 {@link Object#hashCode} 的契约。
     * 如果未重写，则行为由 {@link Object#hashCode} 定义。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public  int hashCode() {
        return super.hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * 实现一个始终返回 {@link System#currentTimeMillis()} 的最新时间的时钟。
     */
    static final class SystemClock extends Clock implements Serializable {
        private static final long serialVersionUID = 6740630888130243051L;
        private final ZoneId zone;

        SystemClock(ZoneId zone) {
            this.zone = zone;
        }
        @Override
        public ZoneId getZone() {
            return zone;
        }
        @Override
        public Clock withZone(ZoneId zone) {
            if (zone.equals(this.zone)) {  // 故意的 NPE
                return this;
            }
            return new SystemClock(zone);
        }
        @Override
        public long millis() {
            return System.currentTimeMillis();
        }
        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis());
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SystemClock) {
                return zone.equals(((SystemClock) obj).zone);
            }
            return false;
        }
        @Override
        public int hashCode() {
            return zone.hashCode() + 1;
        }
        @Override
        public String toString() {
            return "SystemClock[" + zone + "]";
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 实现一个始终返回相同时刻的时钟。
     * 这通常用于测试。
     */
    static final class FixedClock extends Clock implements Serializable {
       private static final long serialVersionUID = 7430389292664866958L;
        private final Instant instant;
        private final ZoneId zone;

        FixedClock(Instant fixedInstant, ZoneId zone) {
            this.instant = fixedInstant;
            this.zone = zone;
        }
        @Override
        public ZoneId getZone() {
            return zone;
        }
        @Override
        public Clock withZone(ZoneId zone) {
            if (zone.equals(this.zone)) {  // 故意的 NPE
                return this;
            }
            return new FixedClock(instant, zone);
        }
        @Override
        public long millis() {
            return instant.toEpochMilli();
        }
        @Override
        public Instant instant() {
            return instant;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FixedClock) {
                FixedClock other = (FixedClock) obj;
                return instant.equals(other.instant) && zone.equals(other.zone);
            }
            return false;
        }
        @Override
        public int hashCode() {
            return instant.hashCode() ^ zone.hashCode();
        }
        @Override
        public String toString() {
            return "FixedClock[" + instant + "," + zone + "]";
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 实现一个为底层时钟添加偏移的时钟。
     */
    static final class OffsetClock extends Clock implements Serializable {
       private static final long serialVersionUID = 2007484719125426256L;
        private final Clock baseClock;
        private final Duration offset;

        OffsetClock(Clock baseClock, Duration offset) {
            this.baseClock = baseClock;
            this.offset = offset;
        }
        @Override
        public ZoneId getZone() {
            return baseClock.getZone();
        }
        @Override
        public Clock withZone(ZoneId zone) {
            if (zone.equals(baseClock.getZone())) {  // 故意的 NPE
                return this;
            }
            return new OffsetClock(baseClock.withZone(zone), offset);
        }
        @Override
        public long millis() {
            return Math.addExact(baseClock.millis(), offset.toMillis());
        }
        @Override
        public Instant instant() {
            return baseClock.instant().plus(offset);
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OffsetClock) {
                OffsetClock other = (OffsetClock) obj;
                return baseClock.equals(other.baseClock) && offset.equals(other.offset);
            }
            return false;
        }
        @Override
        public int hashCode() {
            return baseClock.hashCode() ^ offset.hashCode();
        }
        @Override
        public String toString() {
            return "OffsetClock[" + baseClock + "," + offset + "]";
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 实现一个为底层时钟添加偏移的时钟。
     */
    static final class TickClock extends Clock implements Serializable {
        private static final long serialVersionUID = 6504659149906368850L;
        private final Clock baseClock;
        private final long tickNanos;

        TickClock(Clock baseClock, long tickNanos) {
            this.baseClock = baseClock;
            this.tickNanos = tickNanos;
        }
        @Override
        public ZoneId getZone() {
            return baseClock.getZone();
        }
        @Override
        public Clock withZone(ZoneId zone) {
            if (zone.equals(baseClock.getZone())) {  // 故意的 NPE
                return this;
            }
            return new TickClock(baseClock.withZone(zone), tickNanos);
        }
        @Override
        public long millis() {
            long millis = baseClock.millis();
            return millis - Math.floorMod(millis, tickNanos / 1000_000L);
        }
        @Override
        public Instant instant() {
            if ((tickNanos % 1000_000) == 0) {
                long millis = baseClock.millis();
                return Instant.ofEpochMilli(millis - Math.floorMod(millis, tickNanos / 1000_000L));
            }
            Instant instant = baseClock.instant();
            long nanos = instant.getNano();
            long adjust = Math.floorMod(nanos, tickNanos);
            return instant.minusNanos(adjust);
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TickClock) {
                TickClock other = (TickClock) obj;
                return baseClock.equals(other.baseClock) && tickNanos == other.tickNanos;
            }
            return false;
        }
        @Override
        public int hashCode() {
            return baseClock.hashCode() ^ ((int) (tickNanos ^ (tickNanos >>> 32)));
        }
        @Override
        public String toString() {
            return "TickClock[" + baseClock + "," + Duration.ofNanos(tickNanos) + "]";
        }
    }

}
