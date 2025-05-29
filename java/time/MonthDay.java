
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
 * 重新分发源代码和二进制形式，无论是否修改，都必须保留上述版权声明、
 * 此条件列表和以下免责声明。
 *
 * 重新分发二进制形式必须在随附的文档和/或其他材料中复制上述版权声明、
 * 此条件列表和以下免责声明。
 *
 * 未经特定事先书面许可，不得使用 JSR-310 的名称或其贡献者的名字
 * 来支持或推广从本软件衍生的产品。
 *
 * 本软件由版权所有者和贡献者“按原样”提供，不提供任何明示或暗示的保证，
 * 包括但不限于适销性和适合特定目的的默示保证。在任何情况下，版权所有者或
 * 贡献者均不对任何直接、间接、偶然、特殊、示范性或后果性损害（包括但不限于
 * 采购替代商品或服务；使用丧失、数据丢失或利润损失；或业务中断）负责，
 * 无论是在合同、严格责任或侵权行为（包括疏忽或其他）中，即使已告知发生此类损害的可能性。
 */
package java.time;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Objects;

/**
 * ISO-8601 日历系统中的月-日，例如 {@code --12-03}。
 * <p>
 * {@code MonthDay} 是一个不可变的日期时间对象，表示月和日的组合。任何可以从月和日派生的字段，
 * 如季度，都可以获取。
 * <p>
 * 该类不存储或表示年份、时间或时区。例如，值 "12月3日" 可以存储在 {@code MonthDay} 中。
 * <p>
 * 由于 {@code MonthDay} 不包含年份，2月29日的闰日被认为是有效的。
 * <p>
 * 该类实现了 {@link TemporalAccessor} 而不是 {@link Temporal}。这是因为没有外部信息，
 * 无法确定2月29日是否有效，这阻止了 plus/minus 的实现。与此相关的是，{@code MonthDay} 仅提供访问和设置
 * 字段 {@code MONTH_OF_YEAR} 和 {@code DAY_OF_MONTH} 的方法。
 * <p>
 * ISO-8601 日历系统是当今世界上大多数地区使用的现代民用日历系统。它等同于回溯性格里高利日历系统，
 * 在该系统中，今天的闰年规则适用于所有时间。对于今天编写的大多数应用程序，ISO-8601 规则完全适用。
 * 然而，任何使用历史日期并要求其准确性的应用程序将发现 ISO-8601 方法不适合。
 *
 * <p>
 * 这是一个 <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">基于值的</a>
 * 类；对 {@code MonthDay} 实例使用身份敏感操作（包括引用相等性
 * ({@code ==})、身份哈希码或同步）可能会产生不可预测的结果，应避免使用。
 * 应使用 {@code equals} 方法进行比较。
 *
 * @implSpec
 * 该类是不可变的且线程安全的。
 *
 * @since 1.8
 */
public final class MonthDay
        implements TemporalAccessor, TemporalAdjuster, Comparable<MonthDay>, Serializable {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -939150713474957432L;
    /**
     * 月份，不为空。
     */
    private final int month;
    /**
     * 日期。
     */
    private final int day;

    //-----------------------------------------------------------------------
    /**
     * 从默认时区的系统时钟获取当前月-日。
     * <p>
     * 这将查询默认时区的 {@link Clock#systemDefaultZone() 系统时钟} 以获取当前月-日。
     * <p>
     * 使用此方法将阻止在测试中使用替代时钟，因为时钟是硬编码的。
     *
     * @return 使用系统时钟和默认时区的当前月-日，不为空
     */
    public static MonthDay now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * 从指定时区的系统时钟获取当前月-日。
     * <p>
     * 这将查询 {@link Clock#system(ZoneId) 系统时钟} 以获取当前月-日。
     * 指定时区可以避免依赖默认时区。
     * <p>
     * 使用此方法将阻止在测试中使用替代时钟，因为时钟是硬编码的。
     *
     * @param zone  要使用的时区 ID，不为空
     * @return 使用系统时钟的当前月-日，不为空
     */
    public static MonthDay now(ZoneId zone) {
        return now(Clock.system(zone));
    }

                /**
     * 从指定的时钟获取当前的月-日。
     * <p>
     * 这将查询指定的时钟以获取当前的月-日。
     * 使用此方法允许在测试中使用替代时钟。
     * 可以通过 {@link Clock 依赖注入} 引入替代时钟。
     *
     * @param clock  要使用的时钟，不为空
     * @return 当前的月-日，不为空
     */
    public static MonthDay now(Clock clock) {
        final LocalDate now = LocalDate.now(clock);  // 调用一次
        return MonthDay.of(now.getMonth(), now.getDayOfMonth());
    }

    //-----------------------------------------------------------------------
    /**
     * 获取 {@code MonthDay} 的实例。
     * <p>
     * 月中的日期必须在闰年中对该月有效。
     * 因此，对于二月，29号是有效的。
     * <p>
     * 例如，传入四月和31号将抛出异常，因为任何年份都不会有4月31日。相比之下，
     * 传入2月29日是允许的，因为该月-日有时是有效的。
     *
     * @param month  要表示的年中的月，不为空
     * @param dayOfMonth  要表示的月中的日期，从1到31
     * @return 月-日，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，
     *  或者月中的日期对月无效
     */
    public static MonthDay of(Month month, int dayOfMonth) {
        Objects.requireNonNull(month, "month");
        DAY_OF_MONTH.checkValidValue(dayOfMonth);
        if (dayOfMonth > month.maxLength()) {
            throw new DateTimeException("非法的 DayOfMonth 字段值，值 " + dayOfMonth +
                    " 对于月 " + month.name() + " 无效");
        }
        return new MonthDay(month.getValue(), dayOfMonth);
    }

    /**
     * 获取 {@code MonthDay} 的实例。
     * <p>
     * 月中的日期必须在闰年中对该月有效。
     * 因此，对于2月（二月），29号是有效的。
     * <p>
     * 例如，传入4月（四月）和31号将抛出异常，因为任何年份都不会有4月31日。相比之下，
     * 传入2月29日是允许的，因为该月-日有时是有效的。
     *
     * @param month  要表示的年中的月，从1（一月）到12（十二月）
     * @param dayOfMonth  要表示的月中的日期，从1到31
     * @return 月-日，不为空
     * @throws DateTimeException 如果任何字段的值超出范围，
     *  或者月中的日期对月无效
     */
    public static MonthDay of(int month, int dayOfMonth) {
        return of(Month.of(month), dayOfMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code MonthDay} 的实例。
     * <p>
     * 这基于指定的时间获取月-日。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，
     * 该工厂将这些信息转换为 {@code MonthDay} 的实例。
     * <p>
     * 转换提取 {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR} 和
     * {@link ChronoField#DAY_OF_MONTH DAY_OF_MONTH} 字段。
     * 仅当时间对象具有ISO
     * 日历，或可以转换为 {@code LocalDate} 时，提取才是允许的。
     * <p>
     * 此方法匹配函数接口 {@link TemporalQuery} 的签名
     * 允许它作为查询通过方法引用使用，{@code MonthDay::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 月-日，不为空
     * @throws DateTimeException 如果无法转换为 {@code MonthDay}
     */
    public static MonthDay from(TemporalAccessor temporal) {
        if (temporal instanceof MonthDay) {
            return (MonthDay) temporal;
        }
        try {
            if (IsoChronology.INSTANCE.equals(Chronology.from(temporal)) == false) {
                temporal = LocalDate.from(temporal);
            }
            return of(temporal.get(MONTH_OF_YEAR), temporal.get(DAY_OF_MONTH));
        } catch (DateTimeException ex) {
            throw new DateTimeException("无法从 TemporalAccessor 转换 MonthDay: " +
                    temporal + " 类型 " + temporal.getClass().getName(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 从文本字符串（如 {@code --12-03}）获取 {@code MonthDay} 的实例。
     * <p>
     * 字符串必须表示一个有效的月-日。
     * 格式为 {@code --MM-dd}。
     *
     * @param text  要解析的文本，如 "--12-03"，不为空
     * @return 解析的月-日，不为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static MonthDay parse(CharSequence text) {
        return parse(text, PARSER);
    }

    /**
     * 使用特定的格式化器从文本字符串获取 {@code MonthDay} 的实例。
     * <p>
     * 文本使用格式化器解析，返回月-日。
     *
     * @param text  要解析的文本，不为空
     * @param formatter  要使用的格式化器，不为空
     * @return 解析的月-日，不为空
     * @throws DateTimeParseException 如果文本无法解析
     */
    public static MonthDay parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, MonthDay::from);
    }

    //-----------------------------------------------------------------------
    /**
     * 构造函数，已验证。
     *
     * @param month  要表示的年中的月，已验证从1到12
     * @param dayOfMonth  要表示的月中的日期，已验证从1到29-31
     */
    private MonthDay(int month, int dayOfMonth) {
        this.month = month;
        this.day = dayOfMonth;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 这检查此月-日是否可以查询指定的字段。
     * 如果为 false，则调用 {@link #range(TemporalField) range} 和
     * {@link #get(TemporalField) get} 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则在此处实现查询。
     * 支持的字段有：
     * <ul>
     * <li>{@code MONTH_OF_YEAR}
     * <li>{@code YEAR}
     * </ul>
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.isSupportedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。
     * 字段是否受支持由字段决定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果字段在此月-日上受支持，则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field == MONTH_OF_YEAR || field == DAY_OF_MONTH;
        }
        return field != null && field.isSupportedBy(this);
    }


                /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表达了字段的最小和最大有效值。
     * 此月-日用于增强返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将返回适当的范围实例。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.rangeRefinedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取。
     * 是否可以获取范围由字段决定。
     *
     * @param field  要查询范围的字段，不为空
     * @return 字段的有效值范围，不为空
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     */
    @Override
    public ValueRange range(TemporalField field) {
        if (field == MONTH_OF_YEAR) {
            return field.range();
        } else if (field == DAY_OF_MONTH) {
            return ValueRange.of(1, getMonth().minLength(), getMonth().maxLength());
        }
        return TemporalAccessor.super.range(field);
    }

    /**
     * 以 {@code int} 形式获取此月-日中指定字段的值。
     * <p>
     * 此查询此月-日中指定字段的值。
     * 返回的值将始终在字段的有效值范围内。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将基于此月-日返回有效值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取。是否可以获取值，
     * 以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值或
     *         字段的值超出有效值范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或
     *         值的范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override  // 重写以提供 Javadoc
    public int get(TemporalField field) {
        return range(field).checkValidIntValue(getLong(field), field);
    }

    /**
     * 以 {@code long} 形式获取此月-日中指定字段的值。
     * <p>
     * 此查询此月-日中指定字段的值。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField}，则查询在此实现。
     * {@link #isSupported(TemporalField) 支持的字段} 将基于此月-日返回有效值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取。是否可以获取值，
     * 以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                // alignedDOW 和 alignedWOM 不受支持，因为它们不能在 with() 中设置
                case DAY_OF_MONTH: return day;
                case MONTH_OF_YEAR: return month;
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 获取从 1 到 12 的月-年字段。
     * <p>
     * 此方法返回从 1 到 12 的月。
     * 应用代码通常更清晰，如果通过调用 {@link #getMonth()} 使用枚举 {@link Month}。
     *
     * @return 从 1 到 12 的月-年
     * @see #getMonth()
     */
    public int getMonthValue() {
        return month;
    }

    /**
     * 使用 {@code Month} 枚举获取月-年字段。
     * <p>
     * 此方法返回月的枚举 {@link Month}。
     * 这样可以避免对 {@code int} 值的含义产生混淆。
     * 如果需要访问原始的 {@code int} 值，枚举提供了 {@link Month#getValue() int 值}。
     *
     * @return 月-年，不为空
     * @see #getMonthValue()
     */
    public Month getMonth() {
        return Month.of(month);
    }

    /**
     * 获取月中的日字段。
     * <p>
     * 此方法返回月中的日的原始 {@code int} 值。
     *
     * @return 从 1 到 31 的月中的日
     */
    public int getDayOfMonth() {
        return day;
    }

                //-----------------------------------------------------------------------
    /**
     * 检查年份对于这个月-日是否有效。
     * <p>
     * 此方法检查此月和日与输入年份是否形成有效的日期。这只能在2月29日返回false。
     *
     * @param year  要验证的年份
     * @return 如果年份对于这个月-日有效，则返回true
     * @see Year#isValidMonthDay(MonthDay)
     */
    public boolean isValidYear(int year) {
        return (day == 29 && month == 2 && Year.isLeap(year) == false) == false;
    }

    //-----------------------------------------------------------------------
    /**
     * 返回一个副本，其中此 {@code MonthDay} 的月份已更改。
     * <p>
     * 这将返回一个具有指定月份的月-日。如果指定月份的月中的日期无效，日期将
     * 调整为该月的最后一个有效日期。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param month  要在返回的月-日中设置的月份，从1（一月）到12（十二月）
     * @return 基于此月-日并具有请求月份的 {@code MonthDay}，不为空
     * @throws DateTimeException 如果月份值无效
     */
    public MonthDay withMonth(int month) {
        return with(Month.of(month));
    }

    /**
     * 返回一个副本，其中此 {@code MonthDay} 的月份已更改。
     * <p>
     * 这将返回一个具有指定月份的月-日。如果指定月份的月中的日期无效，日期将
     * 调整为该月的最后一个有效日期。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param month  要在返回的月-日中设置的月份，不为空
     * @return 基于此月-日并具有请求月份的 {@code MonthDay}，不为空
     */
    public MonthDay with(Month month) {
        Objects.requireNonNull(month, "month");
        if (month.getValue() == this.month) {
            return this;
        }
        int day = Math.min(this.day, month.maxLength());
        return new MonthDay(month.getValue(), day);
    }

    /**
     * 返回一个副本，其中此 {@code MonthDay} 的月中的日期已更改。
     * <p>
     * 这将返回一个具有指定月中的日期的月-日。如果指定的月中的日期对于该月无效，则会抛出异常。
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param dayOfMonth  要在返回的月-日中设置的月中的日期，从1到31
     * @return 基于此月-日并具有请求日期的 {@code MonthDay}，不为空
     * @throws DateTimeException 如果月中的日期值无效，
     *  或者月中的日期对于该月无效
     */
    public MonthDay withDayOfMonth(int dayOfMonth) {
        if (dayOfMonth == this.day) {
            return this;
        }
        return of(month, dayOfMonth);
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询策略查询此月-日。
     * <p>
     * 此方法使用指定的查询策略对象查询此月-日。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。阅读查询的文档以了解
     * 此方法的结果将是什么。
     * <p>
     * 通过调用指定查询的 {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数来获取此方法的结果。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为空
     * @return 查询结果，可能返回null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.chronology()) {
            return (R) IsoChronology.INSTANCE;
        }
        return TemporalAccessor.super.query(query);
    }

    /**
     * 调整指定的时间对象以具有此月-日。
     * <p>
     * 这将返回一个与输入具有相同可观察类型的临时对象，但月份和月中的日期已更改为与此相同。
     * <p>
     * 调整等同于使用 {@link Temporal#with(TemporalField, long)} 两次，传递 {@link ChronoField#MONTH_OF_YEAR} 和
     * {@link ChronoField#DAY_OF_MONTH} 作为字段。如果指定的时间对象不使用ISO日历系统，则会抛出 {@code DateTimeException}。
     * <p>
     * 在大多数情况下，通过使用 {@link Temporal#with(TemporalAdjuster)} 反转调用模式会更清晰：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisMonthDay.adjustInto(temporal);
     *   temporal = temporal.with(thisMonthDay);
     * </pre>
     * <p>
     * 本实例是不可变的，不会受此方法调用的影响。
     *
     * @param temporal  要调整的目标对象，不为空
     * @return 调整后的对象，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        if (Chronology.from(temporal).equals(IsoChronology.INSTANCE) == false) {
            throw new DateTimeException("Adjustment only supported on ISO date-time");
        }
        temporal = temporal.with(MONTH_OF_YEAR, month);
        return temporal.with(DAY_OF_MONTH, Math.min(temporal.range(DAY_OF_MONTH).getMaximum(), day));
    }

    /**
     * 使用指定的格式化器格式化此月-日。
     * <p>
     * 此月-日将传递给格式化器以生成字符串。
     *
     * @param formatter  要使用的格式化器，不为空
     * @return 格式化的月-日字符串，不为空
     * @throws DateTimeException 如果在打印过程中发生错误
     */
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

                //-----------------------------------------------------------------------
    /**
     * 将此月-日与年份结合以创建一个 {@code LocalDate}。
     * <p>
     * 这将返回一个由该月-日和指定年份组成的 {@code LocalDate}。
     * <p>
     * 如果年份不是闰年，2月29日将调整为2月28日。
     * <p>
     * 本实例是不可变的，调用此方法不会受到影响。
     *
     * @param year  要使用的年份，范围从 MIN_YEAR 到 MAX_YEAR
     * @return 由该月-日和指定年份组成的本地日期，不为空
     * @throws DateTimeException 如果年份超出有效年份范围
     */
    public LocalDate atYear(int year) {
        return LocalDate.of(year, month, isValidYear(year) ? day : 28);
    }

    //-----------------------------------------------------------------------
    /**
     * 比较此月-日与另一个月-日。
     * <p>
     * 比较首先基于月份的值，然后基于日期的值。
     * 它是“与 equals 一致”的，如 {@link Comparable} 所定义。
     *
     * @param other  要比较的其他月-日，不为空
     * @return 比较值，小于0表示小于，大于0表示大于
     */
    @Override
    public int compareTo(MonthDay other) {
        int cmp = (month - other.month);
        if (cmp == 0) {
            cmp = (day - other.day);
        }
        return cmp;
    }

    /**
     * 检查此月-日是否在指定的月-日之后。
     *
     * @param other  要比较的其他月-日，不为空
     * @return 如果此月-日在指定的月-日之后，则返回 true
     */
    public boolean isAfter(MonthDay other) {
        return compareTo(other) > 0;
    }

    /**
     * 检查此月-日是否在指定的月-日之前。
     *
     * @param other  要比较的其他月-日，不为空
     * @return 如果此月-日在指定的月-日之前，则返回 true
     */
    public boolean isBefore(MonthDay other) {
        return compareTo(other) < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * 检查此月-日是否等于另一个月-日。
     * <p>
     * 比较基于月-日在一年中的时间线位置。
     *
     * @param obj  要检查的对象，null 返回 false
     * @return 如果此月-日等于其他月-日，则返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MonthDay) {
            MonthDay other = (MonthDay) obj;
            return month == other.month && day == other.day;
        }
        return false;
    }

    /**
     * 为此月-日生成一个哈希码。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public int hashCode() {
        return (month << 6) + day;
    }

    //-----------------------------------------------------------------------
    /**
     * 将此月-日输出为一个 {@code String}，例如 {@code --12-03}。
     * <p>
     * 输出格式为 {@code --MM-dd}：
     *
     * @return 此月-日的字符串表示，不为空
     */
    @Override
    public String toString() {
        return new StringBuilder(10).append("--")
            .append(month < 10 ? "0" : "").append(month)
            .append(day < 10 ? "-0" : "-").append(day)
            .toString();
    }

    //-----------------------------------------------------------------------
    /**
     * 使用
     * <a href="../../serialized-form.html#java.time.Ser">专用序列化形式</a> 写入对象。
     * @serialData
     * <pre>
     *  out.writeByte(13);  // 标识一个 MonthDay
     *  out.writeByte(month);
     *  out.writeByte(day);
     * </pre>
     *
     * @return {@code Ser} 的实例，不为空
     */
    private Object writeReplace() {
        return new Ser(Ser.MONTH_DAY_TYPE, this);
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

    void writeExternal(DataOutput out) throws IOException {
        out.writeByte(month);
        out.writeByte(day);
    }

    static MonthDay readExternal(DataInput in) throws IOException {
        byte month = in.readByte();
        byte day = in.readByte();
        return MonthDay.of(month, day);
    }

}
