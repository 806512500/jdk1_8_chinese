
/*
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file.attribute;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 表示文件时间戳属性的值。例如，它可以表示文件最后
 * {@link BasicFileAttributes#lastModifiedTime() 修改}，
 * {@link BasicFileAttributes#lastAccessTime() 访问}，
 * 或 {@link BasicFileAttributes#creationTime() 创建} 的时间。
 *
 * <p> 该类的实例是不可变的。
 *
 * @since 1.7
 * @see java.nio.file.Files#setLastModifiedTime
 * @see java.nio.file.Files#getLastModifiedTime
 */

public final class FileTime
    implements Comparable<FileTime>
{
    /**
     * 解释值的粒度单位。如果此 {@code FileTime} 是从 {@code Instant} 转换而来的，
     * 则 {@code value} 和 {@code unit} 对将不会在此场景中使用。
     */
    private final TimeUnit unit;

    /**
     * 自纪元以来的值；可以是负数。
     */
    private final long value;

    /**
     * 作为 Instant 的值（如果不是从 Instant 创建的，则懒惰创建）。
     */
    private Instant instant;

    /**
     * 由 toString 返回的值（懒惰创建）。
     */
    private String valueAsString;

    /**
     * 初始化此类的新实例。
     */
    private FileTime(long value, TimeUnit unit, Instant instant) {
        this.value = value;
        this.unit = unit;
        this.instant = instant;
    }

    /**
     * 返回表示给定粒度单位值的 {@code FileTime}。
     *
     * @param   value
     *          自纪元（1970-01-01T00:00:00Z）以来的值；可以是负数
     * @param   unit
     *          解释值的粒度单位
     *
     * @return  表示给定值的 {@code FileTime}
     */
    public static FileTime from(long value, TimeUnit unit) {
        Objects.requireNonNull(unit, "unit");
        return new FileTime(value, unit, null);
    }

    /**
     * 返回表示给定毫秒值的 {@code FileTime}。
     *
     * @param   value
     *          自纪元（1970-01-01T00:00:00Z）以来的毫秒值；可以是负数
     *
     * @return  表示给定值的 {@code FileTime}
     */
    public static FileTime fromMillis(long value) {
        return new FileTime(value, TimeUnit.MILLISECONDS, null);
    }

    /**
     * 返回表示与提供的 {@code Instant} 对象相同时间点的 {@code FileTime}。
     *
     * @param   instant
     *          要转换的瞬间
     * @return  表示与提供的瞬间相同时间点的 {@code FileTime}
     * @since 1.8
     */
    public static FileTime from(Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return new FileTime(0, null, instant);
    }

    /**
     * 返回给定粒度单位的值。
     *
     * <p> 从较粗粒度转换时，如果数值溢出，则饱和为负数时的 {@code Long.MIN_VALUE} 或正数时的 {@code Long.MAX_VALUE}。
     *
     * @param   unit
     *          返回值的粒度单位
     *
     * @return  自纪元（1970-01-01T00:00:00Z）以来的值，可以是负数
     */
    public long to(TimeUnit unit) {
        Objects.requireNonNull(unit, "unit");
        if (this.unit != null) {
            return unit.convert(this.value, this.unit);
        } else {
            long secs = unit.convert(instant.getEpochSecond(), TimeUnit.SECONDS);
            if (secs == Long.MIN_VALUE || secs == Long.MAX_VALUE) {
                return secs;
            }
            long nanos = unit.convert(instant.getNano(), TimeUnit.NANOSECONDS);
            long r = secs + nanos;
            // Math.addExact() 变体
            if (((secs ^ r) & (nanos ^ r)) < 0) {
                return (secs < 0) ? Long.MIN_VALUE : Long.MAX_VALUE;
            }
            return r;
        }
    }

    /**
     * 返回毫秒值。
     *
     * <p> 从较粗粒度转换时，如果数值溢出，则饱和为负数时的 {@code Long.MIN_VALUE} 或正数时的 {@code Long.MAX_VALUE}。
     *
     * @return  自纪元（1970-01-01T00:00:00Z）以来的毫秒值
     */
    public long toMillis() {
        if (unit != null) {
            return unit.toMillis(value);
        } else {
            long secs = instant.getEpochSecond();
            int  nanos = instant.getNano();
            // Math.multiplyExact() 变体
            long r = secs * 1000;
            long ax = Math.abs(secs);
            if (((ax | 1000) >>> 31 != 0)) {
                if ((r / 1000) != secs) {
                    return (secs < 0) ? Long.MIN_VALUE : Long.MAX_VALUE;
                }
            }
            return r + nanos / 1000_000;
        }
    }

    /**
     * 转换常量的时间单位。
     */
    private static final long HOURS_PER_DAY      = 24L;
    private static final long MINUTES_PER_HOUR   = 60L;
    private static final long SECONDS_PER_MINUTE = 60L;
    private static final long SECONDS_PER_HOUR   = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    private static final long SECONDS_PER_DAY    = SECONDS_PER_HOUR * HOURS_PER_DAY;
    private static final long MILLIS_PER_SECOND  = 1000L;
    private static final long MICROS_PER_SECOND  = 1000_000L;
    private static final long NANOS_PER_SECOND   = 1000_000_000L;
    private static final int  NANOS_PER_MILLI    = 1000_000;
    private static final int  NANOS_PER_MICRO    = 1000;
    // Instant.MIN 的纪元秒数。
    private static final long MIN_SECOND = -31557014167219200L;
    // Instant.MAX 的纪元秒数。
    private static final long MAX_SECOND = 31556889864403199L;

    /*
     * 将 d 乘以 m，检查溢出。
     */
    private static long scale(long d, long m, long over) {
        if (d >  over) return Long.MAX_VALUE;
        if (d < -over) return Long.MIN_VALUE;
        return d * m;
    }

    /**
     * 将此 {@code FileTime} 对象转换为 {@code Instant}。
     *
     * <p> 转换创建一个表示与此 {@code FileTime} 相同时间点的 {@code Instant}。
     *
     * <p> {@code FileTime} 可以存储比 {@code Instant} 更远的未来和更远的过去的时间点。从这些更远的时间点转换时，如果早于 {@code Instant.MIN} 则饱和为 {@link Instant#MIN}，如果晚于 {@code Instant.MAX} 则饱和为 {@link Instant#MAX}。
     *
     * @return  表示与此 {@code FileTime} 对象相同时间点的瞬间
     * @since 1.8
     */
    public Instant toInstant() {
        if (instant == null) {
            long secs = 0L;
            int nanos = 0;
            switch (unit) {
                case DAYS:
                    secs = scale(value, SECONDS_PER_DAY,
                                 Long.MAX_VALUE/SECONDS_PER_DAY);
                    break;
                case HOURS:
                    secs = scale(value, SECONDS_PER_HOUR,
                                 Long.MAX_VALUE/SECONDS_PER_HOUR);
                    break;
                case MINUTES:
                    secs = scale(value, SECONDS_PER_MINUTE,
                                 Long.MAX_VALUE/SECONDS_PER_MINUTE);
                    break;
                case SECONDS:
                    secs = value;
                    break;
                case MILLISECONDS:
                    secs = Math.floorDiv(value, MILLIS_PER_SECOND);
                    nanos = (int)Math.floorMod(value, MILLIS_PER_SECOND)
                            * NANOS_PER_MILLI;
                    break;
                case MICROSECONDS:
                    secs = Math.floorDiv(value, MICROS_PER_SECOND);
                    nanos = (int)Math.floorMod(value, MICROS_PER_SECOND)
                            * NANOS_PER_MICRO;
                    break;
                case NANOSECONDS:
                    secs = Math.floorDiv(value, NANOS_PER_SECOND);
                    nanos = (int)Math.floorMod(value, NANOS_PER_SECOND);
                    break;
                default : throw new AssertionError("Unit not handled");
            }
            if (secs <= MIN_SECOND)
                instant = Instant.MIN;
            else if (secs >= MAX_SECOND)
                instant = Instant.MAX;
            else
                instant = Instant.ofEpochSecond(secs, nanos);
        }
        return instant;
    }

    /**
     * 测试此 {@code FileTime} 是否与给定对象相等。
     *
     * <p> 结果为 {@code true} 当且仅当参数不为 {@code null} 并且是一个表示相同时间的 {@code FileTime}。此方法满足 {@code Object.equals} 方法的一般契约。
     *
     * @param   obj
     *          要比较的对象
     *
     * @return  {@code true} 如果且仅当给定对象是一个表示相同时间的 {@code FileTime}
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof FileTime) ? compareTo((FileTime)obj) == 0 : false;
    }

    /**
     * 计算此文件时间的哈希码。
     *
     * <p> 哈希码基于表示的值，并满足 {@link Object#hashCode} 方法的一般契约。
     *
     * @return  哈希码值
     */
    @Override
    public int hashCode() {
        // 为了满足与 equals 的契约，使用 instant 表示的哈希码
        return toInstant().hashCode();
    }

    private long toDays() {
        if (unit != null) {
            return unit.toDays(value);
        } else {
            return TimeUnit.SECONDS.toDays(toInstant().getEpochSecond());
        }
    }

    private long toExcessNanos(long days) {
        if (unit != null) {
            return unit.toNanos(value - unit.convert(days, TimeUnit.DAYS));
        } else {
            return TimeUnit.SECONDS.toNanos(toInstant().getEpochSecond()
                                            - TimeUnit.DAYS.toSeconds(days));
        }
    }

    /**
     * 比较两个 {@code FileTime} 对象的值以确定顺序。
     *
     * @param   other
     *          要比较的其他 {@code FileTime}
     *
     * @return  {@code 0} 如果此 {@code FileTime} 等于 {@code other}，一个小于 0 的值如果此 {@code FileTime} 表示的时间在 {@code other} 之前，一个大于 0 的值如果此 {@code FileTime} 表示的时间在 {@code other} 之后
     */
    @Override
    public int compareTo(FileTime other) {
        // 相同的粒度
        if (unit != null && unit == other.unit) {
            return Long.compare(value, other.value);
        } else {
            // 当粒度不同时，使用 instant 表示进行比较
            long secs = toInstant().getEpochSecond();
            long secsOther = other.toInstant().getEpochSecond();
            int cmp = Long.compare(secs, secsOther);
            if (cmp != 0) {
                return cmp;
            }
            cmp = Long.compare(toInstant().getNano(), other.toInstant().getNano());
            if (cmp != 0) {
                return cmp;
            }
            if (secs != MAX_SECOND && secs != MIN_SECOND) {
                return 0;
            }
            // 如果此和 other 的 Instant 表示都是 MIN/MAX，
            // 使用 daysSinceEpoch 和 nanosOfDays，这将不会在计算中饱和。
            long days = toDays();
            long daysOther = other.toDays();
            if (days == daysOther) {
                return Long.compare(toExcessNanos(days), other.toExcessNanos(daysOther));
            }
            return Long.compare(days, daysOther);
        }
    }

    // 400 年周期中的天数 = 146097
    // 10,000 年周期中的天数 = 146097 * 25
    // 每天的秒数 = 86400
    private static final long DAYS_PER_10000_YEARS = 146097L * 25L;
    private static final long SECONDS_PER_10000_YEARS = 146097L * 25L * 86400L;
    private static final long SECONDS_0000_TO_1970 = ((146097L * 5L) - (30L * 365L + 7L)) * 86400L;

    // 以宽度和 0 填充追加年/月/日/小时/分钟/秒/纳秒
    private StringBuilder append(StringBuilder sb, int w, int d) {
        while (w > 0) {
            sb.append((char)(d/w + '0'));
            d = d % w;
            w /= 10;
        }
        return sb;
    }

    /**
     * 返回此 {@code FileTime} 的字符串表示。字符串以 <a
     * href="http://www.w3.org/TR/NOTE-datetime">ISO 8601</a> 格式返回：
     * <pre>
     *     YYYY-MM-DDThh:mm:ss[.s+]Z
     * </pre>
     * 其中 "{@code [.s+]}" 表示一个点后跟一个或多个表示秒的小数部分的数字。仅当秒的小数部分不为零时才存在。例如，{@code
     * FileTime.fromMillis(1234567890000L).toString()} 返回 {@code
     * "2009-02-13T23:31:30Z"}，而 {@code FileTime.fromMillis(1234567890123L).toString()}
     * 返回 {@code "2009-02-13T23:31:30.123Z"}。
     *
     * <p> {@code FileTime} 主要用于表示文件的时间戳值。当用于表示 <i>极端值</i> 时，如果年份小于 "{@code 0001}" 或大于 "{@code 9999}"，则此方法与 ISO 8601 的偏差方式与 <a href="http://www.w3.org/TR/xmlschema-2/#deviantformats">XML Schema 语言</a> 相同。也就是说，年份可以扩展到超过四位数，并且可以是负数。如果超过四位数，则不会出现前导零。"{@code 0001}" 之前的年份是 "{@code -0001}"。
     *
     * @return  此文件时间的字符串表示
     */
    @Override
    public String toString() {
        if (valueAsString == null) {
            long secs = 0L;
            int  nanos = 0;
            if (instant == null && unit.compareTo(TimeUnit.SECONDS) >= 0) {
                secs = unit.toSeconds(value);
            } else {
                secs = toInstant().getEpochSecond();
                nanos = toInstant().getNano();
            }
            LocalDateTime ldt;
            int year = 0;
            if (secs >= -SECONDS_0000_TO_1970) {
                // 当前纪元
                long zeroSecs = secs - SECONDS_PER_10000_YEARS + SECONDS_0000_TO_1970;
                long hi = Math.floorDiv(zeroSecs, SECONDS_PER_10000_YEARS) + 1;
                long lo = Math.floorMod(zeroSecs, SECONDS_PER_10000_YEARS);
                ldt = LocalDateTime.ofEpochSecond(lo - SECONDS_0000_TO_1970, nanos, ZoneOffset.UTC);
                year = ldt.getYear() +  (int)hi * 10000;
            } else {
                // 当前纪元之前
                long zeroSecs = secs + SECONDS_0000_TO_1970;
                long hi = zeroSecs / SECONDS_PER_10000_YEARS;
                long lo = zeroSecs % SECONDS_PER_10000_YEARS;
                ldt = LocalDateTime.ofEpochSecond(lo - SECONDS_0000_TO_1970, nanos, ZoneOffset.UTC);
                year = ldt.getYear() + (int)hi * 10000;
            }
            if (year <= 0) {
                year = year - 1;
            }
            int fraction = ldt.getNano();
            StringBuilder sb = new StringBuilder(64);
            sb.append(year < 0 ? "-" : "");
            year = Math.abs(year);
            if (year < 10000) {
                append(sb, 1000, Math.abs(year));
            } else {
                sb.append(String.valueOf(year));
            }
            sb.append('-');
            append(sb, 10, ldt.getMonthValue());
            sb.append('-');
            append(sb, 10, ldt.getDayOfMonth());
            sb.append('T');
            append(sb, 10, ldt.getHour());
            sb.append(':');
            append(sb, 10, ldt.getMinute());
            sb.append(':');
            append(sb, 10, ldt.getSecond());
            if (fraction != 0) {
                sb.append('.');
                // 添加前导零并删除任何尾随零
                int w = 100_000_000;
                while (fraction % 10 == 0) {
                    fraction /= 10;
                    w /= 10;
                }
                append(sb, w, fraction);
            }
            sb.append('Z');
            valueAsString = sb.toString();
        }
        return valueAsString;
    }
}
