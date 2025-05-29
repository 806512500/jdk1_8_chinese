
/*
 * 版权所有 (c) 2012, 2015, Oracle 和/或其附属公司。保留所有权利。
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

/*
 *
 *
 *
 *
 *
 * 版权所有 (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * 保留所有权利。
 *
 * 重新分发源代码和二进制形式，无论是否修改，都必须保留上述版权声明，
 * 本许可条件列表和以下免责声明。
 *
 * 重新分发二进制形式必须在随附的文档和/或其他材料中复制上述版权声明，
 * 本许可条件列表和以下免责声明。
 *
 * 未经特定事先书面许可，不得使用 JSR-310 或其贡献者的名字来支持或推广从本软件派生的产品。
 *
 * 本软件由版权所有者和贡献者“按原样”提供，不提供任何明示或暗示的保证，
 * 包括但不限于适销性和特定用途适用性的暗示保证。
 * 无论是在合同、严格责任还是侵权行为（包括疏忽或其他）中，
 * 版权所有者或贡献者在任何情况下均不对因使用本软件而产生的任何直接、间接、偶然、特殊、
 * 示例性或后果性损害（包括但不限于采购替代商品或服务；使用损失、数据损失或利润损失；
 * 或业务中断）负责，即使已告知可能发生此类损害。
 */
package java.time;

import static java.time.LocalTime.NANOS_PER_HOUR;
import static java.time.LocalTime.NANOS_PER_MINUTE;
import static java.time.LocalTime.NANOS_PER_SECOND;
import static java.time.LocalTime.SECONDS_PER_DAY;
import static java.time.temporal.ChronoField.NANO_OF_DAY;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;
import static java.time.temporal.ChronoUnit.NANOS;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.time.zone.ZoneRules;
import java.util.Objects;

/**
 * ISO-8601 日历系统中 UTC/Greenwich 的偏移时间，例如 {@code 10:15:30+01:00}。
 * <p>
 * {@code OffsetTime} 是一个不可变的日期时间对象，表示时间，通常
 * 视为小时-分钟-秒-偏移。
 * 该类存储所有时间字段，精度达到纳秒，
 * 以及一个时区偏移。例如，值 "13:45.30.123456789+02:00" 可以存储
 * 在一个 {@code OffsetTime} 中。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code OffsetTime} 实例使用身份敏感操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变和线程安全的。
 *
 * @since 1.8
 */
public final class OffsetTime
        implements Temporal, TemporalAdjuster, Comparable<OffsetTime>, Serializable {

    /**
     * 支持的最小 {@code OffsetTime}，'00:00:00+18:00'。
     * 这是最大偏移（更大的偏移在时间线上更早）的日期开始时的午夜时间。
     * 这结合了 {@link LocalTime#MIN} 和 {@link ZoneOffset#MAX}。
     * 应用程序可以将其用作“远过去”的日期。
     */
    public static final OffsetTime MIN = LocalTime.MIN.atOffset(ZoneOffset.MAX);
    /**
     * 支持的最大 {@code OffsetTime}，'23:59:59.999999999-18:00'。
     * 这是最小偏移（更大的负偏移在时间线上更晚）的日期结束时的午夜前的时间。
     * 这结合了 {@link LocalTime#MAX} 和 {@link ZoneOffset#MIN}。
     * 应用程序可以将其用作“远未来”的日期。
     */
    public static final OffsetTime MAX = LocalTime.MAX.atOffset(ZoneOffset.MIN);

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = 7264499704384272492L;

    /**
     * 本地时间。
     */
    private final LocalTime time;
    /**
     * 从 UTC/Greenwich 的偏移。
     */
    private final ZoneOffset offset;

    //-----------------------------------------------------------------------
    /**
     * 从默认时区的系统时钟获取当前时间。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前时间。
     * 偏移将从时钟中的时区计算得出。
     * <p>
     * 使用此方法将防止在测试中使用替代时钟，因为时钟是硬编码的。
     *
     * @return 使用系统时钟和默认时区的当前时间，不为空
     */
    public static OffsetTime now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 从指定时区的系统时钟获取当前时间。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前时间。
     * 指定时区避免了对默认时区的依赖。
     * 偏移将从指定的时区计算得出。
     * <p>
     * 使用此方法将防止在测试中使用替代时钟，因为时钟是硬编码的。
     *
     * @param zone  要使用的时区 ID，不为空
     * @return 使用系统时钟的当前时间，不为空
     */
    public static OffsetTime now(ZoneId zone) {
        return now(Clock.system(zone));
    }


                /**
     * 从指定的时钟获取当前时间。
     * <p>
     * 这将查询指定的时钟以获取当前时间。
     * 时区偏移量将从时钟中的时区计算得出。
     * <p>
     * 使用此方法允许在测试中使用备用时钟。
     * 可以使用 {@link Clock 依赖注入} 引入备用时钟。
     *
     * @param clock  要使用的时钟，不允许为 null
     * @return 当前时间，不允许为 null
     */
    public static OffsetTime now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        final Instant now = clock.instant();  // 调用一次
        return ofInstant(now, clock.getZone().getRules().getOffset(now));
    }

    //-----------------------------------------------------------------------
    /**
     * 从本地时间和偏移量获取 {@code OffsetTime} 的实例。
     *
     * @param time  本地时间，不允许为 null
     * @param offset  时区偏移量，不允许为 null
     * @return 偏移时间，不允许为 null
     */
    public static OffsetTime of(LocalTime time, ZoneOffset offset) {
        return new OffsetTime(time, offset);
    }

    /**
     * 从小时、分钟、秒和纳秒创建 {@code OffsetTime} 的实例。
     * <p>
     * 这将使用四个指定的字段创建一个偏移时间。
     * <p>
     * 此方法主要用于编写测试用例。
     * 非测试代码通常会使用其他方法来创建偏移时间。
     * {@code LocalTime} 有两个额外的便利变体，这些变体接受较少的参数。
     * 为了减少 API 的占用，这里没有提供这些变体。
     *
     * @param hour  要表示的小时数，从 0 到 23
     * @param minute  要表示的分钟数，从 0 到 59
     * @param second  要表示的秒数，从 0 到 59
     * @param nanoOfSecond  要表示的纳秒数，从 0 到 999,999,999
     * @param offset  时区偏移量，不允许为 null
     * @return 偏移时间，不允许为 null
     * @throws DateTimeException 如果任何字段的值超出范围
     */
    public static OffsetTime of(int hour, int minute, int second, int nanoOfSecond, ZoneOffset offset) {
        return new OffsetTime(LocalTime.of(hour, minute, second, nanoOfSecond), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 从 {@code Instant} 和时区 ID 获取 {@code OffsetTime} 的实例。
     * <p>
     * 这将创建一个与指定的即时相同的偏移时间。
     * 查找从 UTC/格林尼治的偏移量很简单，因为每个即时只有一个有效的偏移量。
     * <p>
     * 在转换过程中会丢弃即时的日期部分。
     * 这意味着转换永远不会因为即时超出有效日期范围而失败。
     *
     * @param instant  要创建时间的即时，不允许为 null
     * @param zone  时区，可以是偏移量，不允许为 null
     * @return 偏移时间，不允许为 null
     */
    public static OffsetTime ofInstant(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        ZoneRules rules = zone.getRules();
        ZoneOffset offset = rules.getOffset(instant);
        long localSecond = instant.getEpochSecond() + offset.getTotalSeconds();  // 溢出将在稍后捕获
        int secsOfDay = (int) Math.floorMod(localSecond, SECONDS_PER_DAY);
        LocalTime time = LocalTime.ofNanoOfDay(secsOfDay * NANOS_PER_SECOND + instant.getNano());
        return new OffsetTime(time, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code OffsetTime} 的实例。
     * <p>
     * 这将根据指定的时间对象获取偏移时间。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，此工厂将其转换为 {@code OffsetTime} 的实例。
     * <p>
     * 转换从时间对象中提取并组合 {@code ZoneOffset} 和 {@code LocalTime}。
     * 实现允许执行优化，例如访问等效于相关对象的字段。
     * <p>
     * 此方法匹配函数接口 {@link TemporalQuery} 的签名，允许它通过方法引用作为查询使用，例如 {@code OffsetTime::from}。
     *
     * @param temporal  要转换的时间对象，不允许为 null
     * @return 偏移时间，不允许为 null
     * @throws DateTimeException 如果无法从时间对象转换为 {@code OffsetTime}
     */
    public static OffsetTime from(TemporalAccessor temporal) {
        if (temporal instanceof OffsetTime) {
            return (OffsetTime) temporal;
        }
        try {
            LocalTime time = LocalTime.from(temporal);
            ZoneOffset offset = ZoneOffset.from(temporal);
            return new OffsetTime(time, offset);
        } catch (DateTimeException ex) {
            throw new DateTimeException("无法从 TemporalAccessor 转换为 OffsetTime: " +
                    temporal + " 类型为 " + temporal.getClass().getName(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 从文本字符串（如 {@code 10:15:30+01:00}）获取 {@code OffsetTime} 的实例。
     * <p>
     * 字符串必须表示一个有效的时间，并使用 {@link java.time.format.DateTimeFormatter#ISO_OFFSET_TIME} 进行解析。
     *
     * @param text  要解析的文本，如 "10:15:30+01:00"，不允许为 null
     * @return 解析的本地时间，不允许为 null
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static OffsetTime parse(CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_OFFSET_TIME);
    }

    /**
     * 使用特定的格式化器从文本字符串获取 {@code OffsetTime} 的实例。
     * <p>
     * 文本使用格式化器进行解析，返回时间。
     *
     * @param text  要解析的文本，不允许为 null
     * @param formatter  要使用的格式化器，不允许为 null
     * @return 解析的偏移时间，不允许为 null
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static OffsetTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, OffsetTime::from);
    }

                //-----------------------------------------------------------------------
    /**
     * 构造函数。
     *
     * @param time  本地时间，不为空
     * @param offset  时区偏移，不为空
     */
    private OffsetTime(LocalTime time, ZoneOffset offset) {
        this.time = Objects.requireNonNull(time, "time");
        this.offset = Objects.requireNonNull(offset, "offset");
    }

    /**
     * 基于此时间返回一个新的时间，尽可能返回 {@code this}。
     *
     * @param time  要创建的时间，不为空
     * @param offset  要创建的时区偏移，不为空
     */
    private OffsetTime with(LocalTime time, ZoneOffset offset) {
        if (this.time == time && this.offset.equals(offset)) {
            return this;
        }
        return new OffsetTime(time, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否支持。
     * <p>
     * 检查此时间是否可以查询指定的字段。
     * 如果返回 false，则调用 {@link #range(TemporalField) range}、
     * {@link #get(TemporalField) get} 和 {@link #with(TemporalField, long)}
     * 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * 支持的字段有：
     * <ul>
     * <li>{@code NANO_OF_SECOND}
     * <li>{@code NANO_OF_DAY}
     * <li>{@code MICRO_OF_SECOND}
     * <li>{@code MICRO_OF_DAY}
     * <li>{@code MILLI_OF_SECOND}
     * <li>{@code MILLI_OF_DAY}
     * <li>{@code SECOND_OF_MINUTE}
     * <li>{@code SECOND_OF_DAY}
     * <li>{@code MINUTE_OF_HOUR}
     * <li>{@code MINUTE_OF_DAY}
     * <li>{@code HOUR_OF_AMPM}
     * <li>{@code CLOCK_HOUR_OF_AMPM}
     * <li>{@code HOUR_OF_DAY}
     * <li>{@code CLOCK_HOUR_OF_DAY}
     * <li>{@code AMPM_OF_DAY}
     * <li>{@code OFFSET_SECONDS}
     * </ul>
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用
     * {@code TemporalField.isSupportedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 字段是否支持由字段决定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果字段在此时间上支持则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field.isTimeBased() || field == OFFSET_SECONDS;
        }
        return field != null && field.isSupportedBy(this);
    }

    /**
     * 检查指定的单位是否支持。
     * <p>
     * 检查指定的单位是否可以添加到或从这个偏移时间中减去。
     * 如果返回 false，则调用 {@link #plus(long, TemporalUnit)} 和
     * {@link #minus(long, TemporalUnit) minus} 方法将抛出异常。
     * <p>
     * 如果单位是 {@link ChronoUnit}，则查询在此实现。
     * 支持的单位有：
     * <ul>
     * <li>{@code NANOS}
     * <li>{@code MICROS}
     * <li>{@code MILLIS}
     * <li>{@code SECONDS}
     * <li>{@code MINUTES}
     * <li>{@code HOURS}
     * <li>{@code HALF_DAYS}
     * </ul>
     * 所有其他 {@code ChronoUnit} 实例将返回 false。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则通过调用
     * {@code TemporalUnit.isSupportedBy(Temporal)} 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 单位是否支持由单位决定。
     *
     * @param unit  要检查的单位，null 返回 false
     * @return 如果单位可以添加/减去则返回 true，否则返回 false
     */
    @Override  // 为 Javadoc 覆盖
    public boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit.isTimeBased();
        }
        return unit != null && unit.isSupportedBy(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的有效最小值和最大值。
     * 此时间用于提高返回范围的准确性。
     * 如果由于字段不支持或其他原因无法返回范围，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用
     * {@code TemporalField.rangeRefinedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 范围是否可以获取由字段决定。
     *
     * @param field  要查询范围的字段，不为空
     * @return 字段的有效值范围，不为空
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不支持
     */
    @Override
    public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == OFFSET_SECONDS) {
                return field.range();
            }
            return time.range(field);
        }
        return field.rangeRefinedBy(this);
    }

    /**
     * 以 {@code int} 形式获取此时间指定字段的值。
     * <p>
     * 查询此时间指定字段的值。
     * 返回的值将始终在字段的有效值范围内。
     * 如果由于字段不支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将基于此时间返回有效值，
     * 但 {@code NANO_OF_DAY} 和 {@code MICRO_OF_DAY} 由于值太大无法放入 {@code int} 而抛出 {@code DateTimeException}。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 值是否可以获取以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或值超出字段的有效值范围
     * @throws UnsupportedTemporalTypeException 如果字段不支持或值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // 为 Javadoc 覆盖
    public int get(TemporalField field) {
        return Temporal.super.get(field);
    }

                /**
     * 从这个时间中获取指定字段的值，类型为 {@code long}。
     * <p>
     * 这个方法查询此时间指定字段的值。
     * 如果由于字段不支持或其他原因无法返回值，将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此处实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将基于此时间返回有效的值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 是否可以获取值以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不得为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == OFFSET_SECONDS) {
                return offset.getTotalSeconds();
            }
            return time.getLong(field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取时区偏移量，例如 '+01:00'。
     * <p>
     * 这是本地时间与 UTC/格林尼治时间的偏移量。
     *
     * @return 时区偏移量，不得为 null
     */
    public ZoneOffset getOffset() {
        return offset;
    }

    /**
     * 返回一个基于此 {@code OffsetTime} 的副本，并指定偏移量，确保结果具有相同的时间。
     * <p>
     * 此方法返回一个具有相同 {@code LocalTime} 和指定 {@code ZoneOffset} 的对象。
     * 不需要进行任何计算。
     * 例如，如果此时间表示 {@code 10:30+02:00}，而指定的偏移量是 {@code +03:00}，
     * 那么此方法将返回 {@code 10:30+03:00}。
     * <p>
     * 要考虑偏移量之间的差异并调整时间字段，使用 {@link #withOffsetSameInstant}。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param offset  要更改的时区偏移量，不得为 null
     * @return 基于此时间并具有请求偏移量的 {@code OffsetTime}，不得为 null
     */
    public OffsetTime withOffsetSameLocal(ZoneOffset offset) {
        return offset != null && offset.equals(this.offset) ? this : new OffsetTime(time, offset);
    }

    /**
     * 返回一个基于此 {@code OffsetTime} 的副本，并指定偏移量，确保结果在隐含的一天中表示相同的瞬间。
     * <p>
     * 此方法返回一个具有指定 {@code ZoneOffset} 和调整了两个偏移量之间差异的 {@code LocalTime} 的对象。
     * 这将导致旧对象和新对象在隐含的一天中表示相同的瞬间。
     * 这对于查找不同偏移量下的本地时间非常有用。
     * 例如，如果此时间表示 {@code 10:30+02:00}，而指定的偏移量是 {@code +03:00}，
     * 那么此方法将返回 {@code 11:30+03:00}。
     * <p>
     * 要更改偏移量而不调整本地时间，使用 {@link #withOffsetSameLocal}。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param offset  要更改的时区偏移量，不得为 null
     * @return 基于此时间并具有请求偏移量的 {@code OffsetTime}，不得为 null
     */
    public OffsetTime withOffsetSameInstant(ZoneOffset offset) {
        if (offset.equals(this.offset)) {
            return this;
        }
        int difference = offset.getTotalSeconds() - this.offset.getTotalSeconds();
        LocalTime adjusted = time.plusSeconds(difference);
        return new OffsetTime(adjusted, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取此日期时间的 {@code LocalTime} 部分。
     * <p>
     * 这将返回一个具有与本日期时间相同小时、分钟、秒和纳秒的 {@code LocalTime}。
     *
     * @return 本日期时间的时间部分，不得为 null
     */
    public LocalTime toLocalTime() {
        return time;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取小时字段。
     *
     * @return 小时，从 0 到 23
     */
    public int getHour() {
        return time.getHour();
    }

    /**
     * 获取分钟字段。
     *
     * @return 分钟，从 0 到 59
     */
    public int getMinute() {
        return time.getMinute();
    }

    /**
     * 获取秒字段。
     *
     * @return 秒，从 0 到 59
     */
    public int getSecond() {
        return time.getSecond();
    }

    /**
     * 获取纳秒字段。
     *
     * @return 纳秒，从 0 到 999,999,999
     */
    public int getNano() {
        return time.getNano();
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个调整后的时间副本。
     * <p>
     * 这个方法返回一个基于此时间的 {@code OffsetTime}，时间已调整。
     * 调整使用指定的调整器策略对象进行。
     * 请阅读调整器的文档以了解将进行何种调整。
     * <p>
     * 简单的调整器可能只是设置某个字段，如小时字段。
     * 更复杂的调整器可能将时间设置为一天的最后一小时。
     * <p>
     * 类 {@link LocalTime} 和 {@link ZoneOffset} 实现了 {@code TemporalAdjuster}，
     * 因此可以使用此方法更改时间或偏移量：
     * <pre>
     *  result = offsetTime.with(time);
     *  result = offsetTime.with(offset);
     * </pre>
     * <p>
     * 该方法的结果是通过调用指定调整器的
     * {@link TemporalAdjuster#adjustInto(Temporal)} 方法并传递 {@code this} 作为参数获得的。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param adjuster 要使用的调整器，不得为 null
     * @return 基于 {@code this} 并已进行调整的 {@code OffsetTime}，不得为 null
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetTime with(TemporalAdjuster adjuster) {
        // 优化
        if (adjuster instanceof LocalTime) {
            return with((LocalTime) adjuster, offset);
        } else if (adjuster instanceof ZoneOffset) {
            return with(time, (ZoneOffset) adjuster);
        } else if (adjuster instanceof OffsetTime) {
            return (OffsetTime) adjuster;
        }
        return (OffsetTime) adjuster.adjustInto(this);
    }

                /**
     * 返回一个副本，其中指定字段设置为新值。
     * <p>
     * 这将返回一个基于此对象的 {@code OffsetTime}，指定字段的值已更改。
     * 可以用于更改任何支持的字段，如小时、分钟或秒。
     * 如果无法设置值，因为字段不受支持或其他原因，将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则调整在此处实现。
     * <p>
     * {@code OFFSET_SECONDS} 字段将返回具有指定偏移量的时间。
     * 本地时间保持不变。如果新的偏移量值超出有效范围，
     * 则会抛出 {@code DateTimeException}。
     * <p>
     * 其他 {@link #isSupported(TemporalField) 支持的字段} 将按照
     * {@link LocalTime#with(TemporalField, long)} LocalTime} 的相应方法进行处理。
     * 在这种情况下，偏移量不参与计算，将保持不变。
     * <p>
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用
     * {@code TemporalField.adjustInto(Temporal, long)} 方法并传递 {@code this} 作为参数来获取此方法的结果。
     * 在这种情况下，字段确定是否以及如何调整时间点。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param field  要在结果中设置的字段，不为空
     * @param newValue  结果中字段的新值
     * @return 基于 {@code this} 的 {@code OffsetTime}，其中指定了字段设置，不为空
     * @throws DateTimeException 如果无法设置字段
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetTime with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            if (field == OFFSET_SECONDS) {
                ChronoField f = (ChronoField) field;
                return with(time, ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue)));
            }
            return with(time.with(field, newValue), offset);
        }
        return field.adjustInto(this, newValue);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中此 {@code OffsetTime} 的小时被更改。
     * <p>
     * 偏移量不影响计算，结果中的偏移量将相同。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param hour  要在结果中设置的小时，从 0 到 23
     * @return 基于此时间的 {@code OffsetTime}，具有请求的小时，不为空
     * @throws DateTimeException 如果小时值无效
     */
    public OffsetTime withHour(int hour) {
        return with(time.withHour(hour), offset);
    }

    /**
     * 返回一个副本，其中此 {@code OffsetTime} 的分钟被更改。
     * <p>
     * 偏移量不影响计算，结果中的偏移量将相同。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param minute  要在结果中设置的分钟，从 0 到 59
     * @return 基于此时间的 {@code OffsetTime}，具有请求的分钟，不为空
     * @throws DateTimeException 如果分钟值无效
     */
    public OffsetTime withMinute(int minute) {
        return with(time.withMinute(minute), offset);
    }

    /**
     * 返回一个副本，其中此 {@code OffsetTime} 的秒被更改。
     * <p>
     * 偏移量不影响计算，结果中的偏移量将相同。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param second  要在结果中设置的秒，从 0 到 59
     * @return 基于此时间的 {@code OffsetTime}，具有请求的秒，不为空
     * @throws DateTimeException 如果秒值无效
     */
    public OffsetTime withSecond(int second) {
        return with(time.withSecond(second), offset);
    }

    /**
     * 返回一个副本，其中此 {@code OffsetTime} 的纳秒被更改。
     * <p>
     * 偏移量不影响计算，结果中的偏移量将相同。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param nanoOfSecond  要在结果中设置的纳秒，从 0 到 999,999,999
     * @return 基于此时间的 {@code OffsetTime}，具有请求的纳秒，不为空
     * @throws DateTimeException 如果纳秒值无效
     */
    public OffsetTime withNano(int nanoOfSecond) {
        return with(time.withNano(nanoOfSecond), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中此 {@code OffsetTime} 的时间被截断。
     * <p>
     * 截断返回一个原始时间的副本，其中小于指定单位的字段被设置为零。
     * 例如，使用 {@link ChronoUnit#MINUTES 分钟} 单位截断将把秒和纳秒字段设置为零。
     * <p>
     * 单位必须具有可以整除标准天长度的 {@linkplain TemporalUnit#getDuration() 时长}。
     * 这包括 {@link ChronoUnit} 上提供的所有时间单位和 {@link ChronoUnit#DAYS DAYS}。
     * 其他单位将抛出异常。
     * <p>
     * 偏移量不影响计算，结果中的偏移量将相同。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param unit  要截断到的单位，不为空
     * @return 基于此时间的 {@code OffsetTime}，时间被截断，不为空
     * @throws DateTimeException 如果无法截断
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     */
    public OffsetTime truncatedTo(TemporalUnit unit) {
        return with(time.truncatedTo(unit), offset);
    }

                //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中添加了指定的数量。
     * <p>
     * 此方法返回一个基于此对象的 {@code OffsetTime}，并添加了指定的数量。
     * 该数量通常是 {@link Duration}，但也可以是实现
     * {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算委托给数量对象，通过调用
     * {@link TemporalAmount#addTo(Temporal)}。数量实现可以自由地
     * 以任何方式实现加法，但通常会回调到 {@link #plus(long, TemporalUnit)}。请参阅
     * 数量实现的文档以确定是否可以成功添加。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToAdd  要添加的数量，不能为空
     * @return 基于此时间并进行了添加的 {@code OffsetTime}，不能为空
     * @throws DateTimeException 如果无法进行添加
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetTime plus(TemporalAmount amountToAdd) {
        return (OffsetTime) amountToAdd.addTo(this);
    }

    /**
     * 返回一个副本，其中添加了指定的数量。
     * <p>
     * 此方法返回一个基于此对象的 {@code OffsetTime}，并以单位形式添加了数量。
     * 如果由于单位不受支持或其他原因无法添加数量，则会抛出异常。
     * <p>
     * 如果字段是 {@link ChronoUnit}，则加法由
     * {@link LocalTime#plus(long, TemporalUnit)} 实现。
     * 偏移量不参与计算，结果中将保持不变。
     * <p>
     * 如果字段不是 {@code ChronoUnit}，则通过调用
     * {@code TemporalUnit.addTo(Temporal, long)} 并将 {@code this} 作为参数传递来获得此方法的结果。
     * 在这种情况下，单位确定是否以及如何执行加法。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToAdd  要添加到结果中的单位数量，可以为负数
     * @param unit  要添加的单位，不能为空
     * @return 基于此时间并添加了指定数量的 {@code OffsetTime}，不能为空
     * @throws DateTimeException 如果无法进行添加
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetTime plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return with(time.plus(amountToAdd, unit), offset);
        }
        return unit.addTo(this, amountToAdd);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中添加了指定数量的小时。
     * <p>
     * 此方法将指定数量的小时添加到此时间，返回一个新的时间。
     * 计算会跨越午夜。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param hours  要添加的小时数，可以为负数
     * @return 基于此时间并添加了小时数的 {@code OffsetTime}，不能为空
     */
    public OffsetTime plusHours(long hours) {
        return with(time.plusHours(hours), offset);
    }

    /**
     * 返回一个副本，其中添加了指定数量的分钟。
     * <p>
     * 此方法将指定数量的分钟添加到此时间，返回一个新的时间。
     * 计算会跨越午夜。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param minutes  要添加的分钟数，可以为负数
     * @return 基于此时间并添加了分钟数的 {@code OffsetTime}，不能为空
     */
    public OffsetTime plusMinutes(long minutes) {
        return with(time.plusMinutes(minutes), offset);
    }

    /**
     * 返回一个副本，其中添加了指定数量的秒。
     * <p>
     * 此方法将指定数量的秒添加到此时间，返回一个新的时间。
     * 计算会跨越午夜。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param seconds  要添加的秒数，可以为负数
     * @return 基于此时间并添加了秒数的 {@code OffsetTime}，不能为空
     */
    public OffsetTime plusSeconds(long seconds) {
        return with(time.plusSeconds(seconds), offset);
    }

    /**
     * 返回一个副本，其中添加了指定数量的纳秒。
     * <p>
     * 此方法将指定数量的纳秒添加到此时间，返回一个新的时间。
     * 计算会跨越午夜。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param nanos  要添加的纳秒数，可以为负数
     * @return 基于此时间并添加了纳秒数的 {@code OffsetTime}，不能为空
     */
    public OffsetTime plusNanos(long nanos) {
        return with(time.plusNanos(nanos), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中减去了指定的数量。
     * <p>
     * 此方法返回一个基于此对象的 {@code OffsetTime}，并减去了指定的数量。
     * 该数量通常是 {@link Duration}，但也可以是实现
     * {@link TemporalAmount} 接口的任何其他类型。
     * <p>
     * 计算委托给数量对象，通过调用
     * {@link TemporalAmount#subtractFrom(Temporal)}。数量实现可以自由地
     * 以任何方式实现减法，但通常会回调到 {@link #minus(long, TemporalUnit)}。请参阅
     * 数量实现的文档以确定是否可以成功减去。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param amountToSubtract  要减去的数量，不能为空
     * @return 基于此时间并进行了减法的 {@code OffsetTime}，不能为空
     * @throws DateTimeException 如果无法进行减法
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetTime minus(TemporalAmount amountToSubtract) {
        return (OffsetTime) amountToSubtract.subtractFrom(this);
    }

                /**
     * 返回一个副本，其中指定了要减去的数量。
     * <p>
     * 这将返回一个基于此对象的 {@code OffsetTime}，减去了以单位表示的数量。如果由于单位不受支持或其他原因无法减去该数量，则会抛出异常。
     * <p>
     * 此方法等同于 {@link #plus(long, TemporalUnit)}，但数量为负数。请参阅该方法以了解加法和减法的详细工作原理。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param amountToSubtract  从结果中减去的单位数量，可以为负数
     * @param unit  要减去的数量的单位，不能为空
     * @return 基于此时间并减去了指定数量的 {@code OffsetTime}，不能为空
     * @throws DateTimeException 如果无法进行减法操作
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public OffsetTime minus(long amountToSubtract, TemporalUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中指定了要减去的小时数。
     * <p>
     * 这将从当前时间中减去指定的小时数，返回一个新的时间。计算会跨过午夜。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param hours  要减去的小时数，可以为负数
     * @return 基于此时间并减去了指定小时数的 {@code OffsetTime}，不能为空
     */
    public OffsetTime minusHours(long hours) {
        return with(time.minusHours(hours), offset);
    }

    /**
     * 返回一个副本，其中指定了要减去的分钟数。
     * <p>
     * 这将从当前时间中减去指定的分钟数，返回一个新的时间。计算会跨过午夜。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param minutes  要减去的分钟数，可以为负数
     * @return 基于此时间并减去了指定分钟数的 {@code OffsetTime}，不能为空
     */
    public OffsetTime minusMinutes(long minutes) {
        return with(time.minusMinutes(minutes), offset);
    }

    /**
     * 返回一个副本，其中指定了要减去的秒数。
     * <p>
     * 这将从当前时间中减去指定的秒数，返回一个新的时间。计算会跨过午夜。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param seconds  要减去的秒数，可以为负数
     * @return 基于此时间并减去了指定秒数的 {@code OffsetTime}，不能为空
     */
    public OffsetTime minusSeconds(long seconds) {
        return with(time.minusSeconds(seconds), offset);
    }

    /**
     * 返回一个副本，其中指定了要减去的纳秒数。
     * <p>
     * 这将从当前时间中减去指定的纳秒数，返回一个新的时间。计算会跨过午夜。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param nanos  要减去的纳秒数，可以为负数
     * @return 基于此时间并减去了指定纳秒数的 {@code OffsetTime}，不能为空
     */
    public OffsetTime minusNanos(long nanos) {
        return with(time.minusNanos(nanos), offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询来查询此时间。
     * <p>
     * 使用指定的查询策略对象查询此时间。{@code TemporalQuery} 对象定义了获取结果的逻辑。请阅读查询文档以了解此方法的返回结果。
     * <p>
     * 该方法的结果是通过调用指定查询的 {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数获得的。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不能为空
     * @return 查询结果，可能为 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.offset() || query == TemporalQueries.zone()) {
            return (R) offset;
        } else if (query == TemporalQueries.zoneId() | query == TemporalQueries.chronology() || query == TemporalQueries.localDate()) {
            return null;
        } else if (query == TemporalQueries.localTime()) {
            return (R) time;
        } else if (query == TemporalQueries.precision()) {
            return (R) NANOS;
        }
        // 作为优化，内联 TemporalAccessor.super.query(query)
        // 非 JDK 类不允许进行此优化
        return query.queryFrom(this);
    }

    /**
     * 调整指定的时间对象，使其具有与此对象相同的偏移量和时间。
     * <p>
     * 这将返回一个与输入具有相同可观察类型的临时对象，但偏移量和时间已更改为与此对象相同。
     * <p>
     * 调整等同于两次使用 {@link Temporal#with(TemporalField, long)}，分别传递 {@link ChronoField#NANO_OF_DAY} 和
     * {@link ChronoField#OFFSET_SECONDS} 作为字段。
     * <p>
     * 在大多数情况下，通过使用 {@link Temporal#with(TemporalAdjuster)} 反转调用模式会更清晰：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisOffsetTime.adjustInto(temporal);
     *   temporal = temporal.with(thisOffsetTime);
     * </pre>
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param temporal  要调整的目标对象，不能为空
     * @return 调整后的对象，不能为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal
                .with(NANO_OF_DAY, time.toNanoOfDay())
                .with(OFFSET_SECONDS, offset.getTotalSeconds());
    }

                /**
     * 计算到另一个时间的时间量，以指定单位表示。
     * <p>
     * 这计算两个 {@code OffsetTime} 对象之间的时间量，以单个 {@code TemporalUnit} 表示。
     * 起点和终点分别是 {@code this} 和指定的时间。
     * 如果终点在起点之前，结果将为负。
     * 例如，可以使用 {@code startTime.until(endTime, HOURS)} 计算两个时间之间的小时数。
     * <p>
     * 传递给此方法的 {@code Temporal} 被转换为使用 {@link #from(TemporalAccessor)} 的 {@code OffsetTime}。
     * 如果两个时间之间的偏移量不同，则指定的结束时间将被规范化为与该时间具有相同的偏移量。
     * <p>
     * 计算返回一个整数，表示两个时间之间的完整单位数。
     * 例如，11:30Z 和 13:29Z 之间的时间量仅为一个小时，因为它比两个小时少一分钟。
     * <p>
     * 使用此方法有两种等效的方式。
     * 第一种是调用此方法。
     * 第二种是使用 {@link TemporalUnit#between(Temporal, Temporal)}：
     * <pre>
     *   // 这两行是等效的
     *   amount = start.until(end, MINUTES);
     *   amount = MINUTES.between(start, end);
     * </pre>
     * 应根据哪个使代码更具可读性来选择。
     * <p>
     * 该计算在 {@link ChronoUnit} 的此方法中实现。
     * 支持的单位有 {@code NANOS}、{@code MICROS}、{@code MILLIS}、{@code SECONDS}、
     * {@code MINUTES}、{@code HOURS} 和 {@code HALF_DAYS}。
     * 其他 {@code ChronoUnit} 值将抛出异常。
     * <p>
     * 如果单位不是 {@code ChronoUnit}，则通过调用 {@code TemporalUnit.between(Temporal, Temporal)}
     * 并将 {@code this} 作为第一个参数和转换后的输入时间作为第二个参数传递来获取此方法的结果。
     * <p>
     * 该实例是不可变的，不受此方法调用的影响。
     *
     * @param endExclusive  结束时间，不包括，转换为 {@code OffsetTime}，不为空
     * @param unit  测量时间量的单位，不为空
     * @return 从这个时间到结束时间的时间量
     * @throws DateTimeException 如果无法计算时间量，或者结束时间无法转换为 {@code OffsetTime}
     * @throws UnsupportedTemporalTypeException 如果单位不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        OffsetTime end = OffsetTime.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            long nanosUntil = end.toEpochNano() - toEpochNano();  // no overflow
            switch ((ChronoUnit) unit) {
                case NANOS: return nanosUntil;
                case MICROS: return nanosUntil / 1000;
                case MILLIS: return nanosUntil / 1000_000;
                case SECONDS: return nanosUntil / NANOS_PER_SECOND;
                case MINUTES: return nanosUntil / NANOS_PER_MINUTE;
                case HOURS: return nanosUntil / NANOS_PER_HOUR;
                case HALF_DAYS: return nanosUntil / (12 * NANOS_PER_HOUR);
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return unit.between(this, end);
    }

    /**
     * 使用指定的格式化程序格式化此时间。
     * <p>
     * 此时间将传递给格式化程序以生成字符串。
     *
     * @param formatter  要使用的格式化程序，不为空
     * @return 格式化的时间字符串，不为空
     * @throws DateTimeException 如果打印过程中发生错误
     */
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此时间与日期组合以创建一个 {@code OffsetDateTime}。
     * <p>
     * 这将返回一个由此时间和指定日期组成的 {@code OffsetDateTime}。
     * 所有可能的日期和时间组合都是有效的。
     *
     * @param date  要组合的日期，不为空
     * @return 由此时间和指定日期组成的偏移日期时间，不为空
     */
    public OffsetDateTime atDate(LocalDate date) {
        return OffsetDateTime.of(date, time, offset);
    }

    //-----------------------------------------------------------------------
    /**
     * 将此时间转换为基于 1970-01-01Z 的纪元纳秒。
     *
     * @return 纪元纳秒值
     */
    private long toEpochNano() {
        long nod = time.toNanoOfDay();
        long offsetNanos = offset.getTotalSeconds() * NANOS_PER_SECOND;
        return nod - offsetNanos;
    }

    //-----------------------------------------------------------------------
    /**
     * 比较此 {@code OffsetTime} 与另一个时间。
     * <p>
     * 比较首先基于 UTC 等效时刻，然后基于本地时间。
     * 它是“与 equals 一致的”，如 {@link Comparable} 所定义。
     * <p>
     * 例如，以下是比较器顺序：
     * <ol>
     * <li>{@code 10:30+01:00}</li>
     * <li>{@code 11:00+01:00}</li>
     * <li>{@code 12:00+02:00}</li>
     * <li>{@code 11:30+01:00}</li>
     * <li>{@code 12:00+01:00}</li>
     * <li>{@code 12:30+01:00}</li>
     * </ol>
     * 值 #2 和 #3 表示时间线上的同一时刻。
     * 当两个值表示同一时刻时，比较本地时间以区分它们。
     * 这一步是使排序与 {@code equals()} 一致所必需的。
     * <p>
     * 要比较两个 {@code TemporalAccessor} 实例的底层本地时间，可以使用 {@link ChronoField#NANO_OF_DAY} 作为比较器。
     *
     * @param other  要比较的其他时间，不为空
     * @return 比较器值，小于 0 表示小于，大于 0 表示大于
     * @throws NullPointerException 如果 {@code other} 为空
     */
    @Override
    public int compareTo(OffsetTime other) {
        if (offset.equals(other.offset)) {
            return time.compareTo(other.time);
        }
        int compare = Long.compare(toEpochNano(), other.toEpochNano());
        if (compare == 0) {
            compare = time.compareTo(other.time);
        }
        return compare;
    }

                //-----------------------------------------------------------------------
    /**
     * 检查此 {@code OffsetTime} 的时刻是否在指定时间之后，通过将两个时间应用到同一日期进行比较。
     * <p>
     * 该方法与 {@link #compareTo} 中的比较不同，因为它仅比较时间的时刻。这相当于使用相同的日期将两个时间转换为时刻并比较这些时刻。
     *
     * @param other  要比较的其他时间，不得为 null
     * @return 如果此时间在指定时间的时刻之后，则返回 true
     */
    public boolean isAfter(OffsetTime other) {
        return toEpochNano() > other.toEpochNano();
    }

    /**
     * 检查此 {@code OffsetTime} 的时刻是否在指定时间之前，通过将两个时间应用到同一日期进行比较。
     * <p>
     * 该方法与 {@link #compareTo} 中的比较不同，因为它仅比较时间的时刻。这相当于使用相同的日期将两个时间转换为时刻并比较这些时刻。
     *
     * @param other  要比较的其他时间，不得为 null
     * @return 如果此时间在指定时间的时刻之前，则返回 true
     */
    public boolean isBefore(OffsetTime other) {
        return toEpochNano() < other.toEpochNano();
    }

    /**
     * 检查此 {@code OffsetTime} 的时刻是否等于指定时间的时刻，通过将两个时间应用到同一日期进行比较。
     * <p>
     * 该方法与 {@link #compareTo} 和 {@link #equals} 中的比较不同，因为它仅比较时间的时刻。这相当于使用相同的日期将两个时间转换为时刻并比较这些时刻。
     *
     * @param other  要比较的其他时间，不得为 null
     * @return 如果此时间等于指定时间的时刻，则返回 true
     */
    public boolean isEqual(OffsetTime other) {
        return toEpochNano() == other.toEpochNano();
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此时间是否等于另一个时间。
     * <p>
     * 比较基于本地时间和偏移量。
     * 要比较时间线上的同一时刻，请使用 {@link #isEqual(OffsetTime)}。
     * <p>
     * 仅比较 {@code OffsetTime} 类型的对象，其他类型返回 false。
     * 要比较两个 {@code TemporalAccessor} 实例的底层本地时间，可以使用 {@link ChronoField#NANO_OF_DAY} 作为比较器。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此时间等于其他时间，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof OffsetTime) {
            OffsetTime other = (OffsetTime) obj;
            return time.equals(other.time) && offset.equals(other.offset);
        }
        return false;
    }

    /**
     * 返回此时间的哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public int hashCode() {
        return time.hashCode() ^ offset.hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * 将此时间输出为 {@code String}，例如 {@code 10:15:30+01:00}。
     * <p>
     * 输出将是以下 ISO-8601 格式之一：
     * <ul>
     * <li>{@code HH:mmXXXXX}</li>
     * <li>{@code HH:mm:ssXXXXX}</li>
     * <li>{@code HH:mm:ss.SSSXXXXX}</li>
     * <li>{@code HH:mm:ss.SSSSSSXXXXX}</li>
     * <li>{@code HH:mm:ss.SSSSSSSSSXXXXX}</li>
     * </ul>
     * 使用的格式将是能够输出时间完整值的最短格式，省略的部分隐含为零。
     *
     * @return 此时间的字符串表示，不得为 null
     */
    @Override
    public String toString() {
        return time.toString() + offset.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用的序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(9);  // 标识一个 OffsetTime
     *  // <a href="../../serialized-form.html#java.time.LocalTime">时间</a>，不包括一个字节的头
     *  // <a href="../../serialized-form.html#java.time.ZoneOffset">偏移量</a>，不包括一个字节的头
     * </pre>
     *
     * @return {@code Ser} 的实例，不得为 null
     */
    private Object writeReplace() {
        return new Ser(Ser.OFFSET_TIME_TYPE, this);
    }

    /**
     * 防御恶意流。
     *
     * @param s 要读取的流
     * @throws InvalidObjectException 总是抛出
     */
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("通过序列化代理进行反序列化");
    }

    void writeExternal(ObjectOutput out) throws IOException {
        time.writeExternal(out);
        offset.writeExternal(out);
    }

    static OffsetTime readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        LocalTime time = LocalTime.readExternal(in);
        ZoneOffset offset = ZoneOffset.readExternal(in);
        return OffsetTime.of(time, offset);
    }

}
