
/*
 * 版权所有 (c) 2009, 2013, Oracle 和/或其关联公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
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
 * <p> 本类的实例是不可变的。
 *
 * @since 1.7
 * @see java.nio.file.Files#setLastModifiedTime
 * @see java.nio.file.Files#getLastModifiedTime
 */

public final class FileTime
    implements Comparable<FileTime>
{
    /**
     * 解释值的粒度单位。如果此 {@code FileTime} 是从 {@code Instant} 转换而来，
     * 则 {@code value} 和 {@code unit} 对将不会在此场景中使用。
     */
    private final TimeUnit unit;

    /**
     * 自纪元以来的值；可以是负数。
     */
    private final long value;

    /**
     * 作为 Instant 的值（如果不是从 Instant 创建，则懒惰创建）
     */
    private Instant instant;

    /**
     * 由 toString 返回的值（懒惰创建）
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
     * <p> 从较粗粒度转换时，如果数值溢出，则饱和为 {@code Long.MIN_VALUE}（如果为负数）或 {@code Long.MAX_VALUE}（如果为正数）。
     *
     * @param   unit
     *          返回值的粒度单位
     *
     * @return  自纪元（1970-01-01T00:00:00Z）以来的给定粒度单位的值；可以是负数
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
            // Math.addExact() 的变体
            if (((secs ^ r) & (nanos ^ r)) < 0) {
                return (secs < 0) ? Long.MIN_VALUE : Long.MAX_VALUE;
            }
            return r;
        }
    }

    /**
     * 返回毫秒值。
     *
     * <p> 从较粗粒度转换时，如果数值溢出，则饱和为 {@code Long.MIN_VALUE}（如果为负数）或 {@code Long.MAX_VALUE}（如果为正数）。
     *
     * @return  自纪元（1970-01-01T00:00:00Z）以来的毫秒值
     */
    public long toMillis() {
        if (unit != null) {
            return unit.toMillis(value);
        } else {
            long secs = instant.getEpochSecond();
            int  nanos = instant.getNano();
            // Math.multiplyExact() 的变体
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
     * 转换的时间单位常量。
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
    // Instant.MIN 的纪元秒。
    private static final long MIN_SECOND = -31557014167219200L;
    // Instant.MAX 的纪元秒。
    private static final long MAX_SECOND = 31556889864403199L;


                /*
     * 按 m 缩放 d，并检查溢出。
     */
    private static long scale(long d, long m, long over) {
        if (d >  over) return Long.MAX_VALUE;
        if (d < -over) return Long.MIN_VALUE;
        return d * m;
    }

    /**
     * 将此 {@code FileTime} 对象转换为 {@code Instant}。
     *
     * <p> 转换创建一个表示与该 {@code FileTime} 相同时间点的 {@code Instant}。
     *
     * <p> {@code FileTime} 可以存储比 {@code Instant} 更远的未来和更远的过去的时间点。从这些更远的时间点进行转换时，如果早于 {@code Instant.MIN} 则饱和到 {@link Instant#MIN}，如果晚于 {@code Instant.MAX} 则饱和到 {@link Instant#MAX}。
     *
     * @return  一个表示与该 {@code FileTime} 对象相同时间点的瞬时
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
     * <p> 如果且仅当参数不是 {@code null} 并且是一个表示相同时间的 {@code FileTime} 时，结果为 {@code true}。此方法满足 {@code Object.equals} 方法的一般约定。
     *
     * @param   obj
     *          要比较的对象
     *
     * @return  如果且仅当给定对象是一个表示相同时间的 {@code FileTime} 时，返回 {@code true}
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof FileTime) ? compareTo((FileTime)obj) == 0 : false;
    }

    /**
     * 计算此文件时间的哈希码。
     *
     * <p> 哈希码基于表示的值，并满足 {@link Object#hashCode} 方法的一般约定。
     *
     * @return  哈希码值
     */
    @Override
    public int hashCode() {
        // 使用瞬时表示的哈希码以满足与 equals 的约定
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
     * @return  如果此 {@code FileTime} 等于 {@code other}，则返回 {@code 0}；如果此 {@code FileTime} 表示的时间早于 {@code other}，则返回小于 0 的值；如果此 {@code FileTime} 表示的时间晚于 {@code other}，则返回大于 0 的值
     */
    @Override
    public int compareTo(FileTime other) {
        // 相同的粒度
        if (unit != null && unit == other.unit) {
            return Long.compare(value, other.value);
        } else {
            // 当单位不同时使用瞬时表示进行比较
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
            // 如果此对象和 other 的 Instant 表示都是 MIN/MAX，
            // 使用 daysSinceEpoch 和 nanosOfDays，这在计算过程中不会饱和。
            long days = toDays();
            long daysOther = other.toDays();
            if (days == daysOther) {
                return Long.compare(toExcessNanos(days), other.toExcessNanos(daysOther));
            }
            return Long.compare(days, daysOther);
        }
    }


                // 400年周期中的天数 = 146097
    // 10,000年周期中的天数 = 146097 * 25
    // 每天的秒数 = 86400
    private static final long DAYS_PER_10000_YEARS = 146097L * 25L;
    private static final long SECONDS_PER_10000_YEARS = 146097L * 25L * 86400L;
    private static final long SECONDS_0000_TO_1970 = ((146097L * 5L) - (30L * 365L + 7L)) * 86400L;

    // 以指定宽度和0填充追加年/月/日/小时/分钟/秒/纳秒
    private StringBuilder append(StringBuilder sb, int w, int d) {
        while (w > 0) {
            sb.append((char)(d/w + '0'));
            d = d % w;
            w /= 10;
        }
        return sb;
    }

    /**
     * 返回此 {@code FileTime} 的字符串表示形式。字符串以 <a
     * href="http://www.w3.org/TR/NOTE-datetime">ISO&nbsp;8601</a> 格式返回：
     * <pre>
     *     YYYY-MM-DDThh:mm:ss[.s+]Z
     * </pre>
     * 其中 "{@code [.s+]}" 表示一个点后跟一个或多个表示秒的小数部分的数字。仅当秒的小数部分不为零时才出现。例如，{@code
     * FileTime.fromMillis(1234567890000L).toString()} 返回 {@code
     * "2009-02-13T23:31:30Z"}，而 {@code FileTime.fromMillis(1234567890123L).toString()}
     * 返回 {@code "2009-02-13T23:31:30.123Z"}。
     *
     * <p> {@code FileTime} 主要用于表示文件的时间戳值。当用于表示 <i>极端值</i>，即年份小于 "{@code 0001}" 或大于 "{@code 9999}" 时，
     * 此方法与 ISO 8601 的偏差与 <a href="http://www.w3.org/TR/xmlschema-2/#deviantformats">XML Schema
     * 语言</a> 相同。也就是说，年份可以扩展到多于四位数，并且可以带有负号。如果超过四位数，则不会出现前导零。"{@code 0001}" 之前的年份是 "{@code -0001}"。
     *
     * @return  此文件时间的字符串表示形式
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
                // 添加前导零并去除任何尾随零
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
