
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
 * 重新分发二进制形式必须在随分发提供的文档和/或其他材料中
 * 重现上述版权声明、此条件列表和以下免责声明。
 *
 * 未经特定事先书面许可，不得使用 JSR-310 或其贡献者的名字
 * 为源自本软件的产品进行背书或促销。
 *
 * 本软件由版权所有者和贡献者“按原样”提供，不附带任何明示或暗示的保证，
 * 包括但不限于适销性和特定用途适用性的暗示保证。在任何情况下，版权所有者或
 * 贡献者均不对任何直接、间接、偶然、特殊、示范性或后果性损害（包括但不限于
 * 采购替代商品或服务；使用、数据或利润损失；或业务中断）负责，无论是在合同、
 * 严格责任或侵权（包括疏忽或其他）理论下，即使已被告知可能发生此类损害。
 */
package java.time;

import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoUnit.MONTHS;

import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Locale;

/**
 * 一年中的月份，例如 '七月'。
 * <p>
 * {@code Month} 是一个枚举，表示一年中的 12 个月份 -
 * 一月、二月、三月、四月、五月、六月、七月、八月、九月、十月、
 * 十一月和十二月。
 * <p>
 * 除了文本枚举名称外，每个月份还有一个 {@code int} 值。
 * {@code int} 值遵循常规用法和 ISO-8601 标准，从 1（一月）到 12（十二月）。
 * 建议应用程序使用枚举而不是 {@code int} 值，以确保代码清晰。
 * <p>
 * <b>不要使用 {@code ordinal()} 来获取 {@code Month} 的数字表示。
 * 使用 {@code getValue()} 代替。</b>
 * <p>
 * 此枚举表示许多日历系统中常见的概念。
 * 因此，此枚举可以被任何具有与 ISO-8601 日历系统完全等效的月份概念的日历系统使用。
 *
 * @implSpec
 * 这是一个不可变且线程安全的枚举。
 *
 * @since 1.8
 */
public enum Month implements TemporalAccessor, TemporalAdjuster {

    /**
     * 月份为一月的单例实例，有 31 天。
     * 这个值为 {@code 1}。
     */
    JANUARY,
    /**
     * 月份为二月的单例实例，有 28 天，闰年有 29 天。
     * 这个值为 {@code 2}。
     */
    FEBRUARY,
    /**
     * 月份为三月的单例实例，有 31 天。
     * 这个值为 {@code 3}。
     */
    MARCH,
    /**
     * 月份为四月的单例实例，有 30 天。
     * 这个值为 {@code 4}。
     */
    APRIL,
    /**
     * 月份为五月的单例实例，有 31 天。
     * 这个值为 {@code 5}。
     */
    MAY,
    /**
     * 月份为六月的单例实例，有 30 天。
     * 这个值为 {@code 6}。
     */
    JUNE,
    /**
     * 月份为七月的单例实例，有 31 天。
     * 这个值为 {@code 7}。
     */
    JULY,
    /**
     * 月份为八月的单例实例，有 31 天。
     * 这个值为 {@code 8}。
     */
    AUGUST,
    /**
     * 月份为九月的单例实例，有 30 天。
     * 这个值为 {@code 9}。
     */
    SEPTEMBER,
    /**
     * 月份为十月的单例实例，有 31 天。
     * 这个值为 {@code 10}。
     */
    OCTOBER,
    /**
     * 月份为十一月的单例实例，有 30 天。
     * 这个值为 {@code 11}。
     */
    NOVEMBER,
    /**
     * 月份为十二月的单例实例，有 31 天。
     * 这个值为 {@code 12}。
     */
    DECEMBER;
    /**
     * 所有常量的私有缓存。
     */
    private static final Month[] ENUMS = Month.values();

    //-----------------------------------------------------------------------
    /**
     * 从 {@code int} 值获取 {@code Month} 的实例。
     * <p>
     * {@code Month} 是一个枚举，表示一年中的 12 个月份。
     * 此工厂允许从 {@code int} 值获取枚举。
     * {@code int} 值遵循 ISO-8601 标准，从 1（一月）到 12（十二月）。
     *
     * @param month  要表示的月份，从 1（一月）到 12（十二月）
     * @return 月份，不为空
     * @throws DateTimeException 如果月份无效
     */
    public static Month of(int month) {
        if (month < 1 || month > 12) {
            throw new DateTimeException("Invalid value for MonthOfYear: " + month);
        }
        return ENUMS[month - 1];
    }

                //-----------------------------------------------------------------------
    /**
     * 从时间对象中获取 {@code Month} 的实例。
     * <p>
     * 此方法根据指定的时间对象获取月份。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，
     * 该工厂方法将其转换为 {@code Month} 的实例。
     * <p>
     * 转换提取 {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR} 字段。
     * 仅当时间对象具有 ISO 历法，或可以转换为 {@code LocalDate} 时，才允许进行提取。
     * <p>
     * 该方法匹配函数接口 {@link TemporalQuery} 的签名，
     * 允许通过方法引用作为查询使用，例如 {@code Month::from}。
     *
     * @param temporal 要转换的时间对象，不为 null
     * @return 月份，不为 null
     * @throws DateTimeException 如果无法转换为 {@code Month}
     */
    public static Month from(TemporalAccessor temporal) {
        if (temporal instanceof Month) {
            return (Month) temporal;
        }
        try {
            if (IsoChronology.INSTANCE.equals(Chronology.from(temporal)) == false) {
                temporal = LocalDate.from(temporal);
            }
            return of(temporal.get(MONTH_OF_YEAR));
        } catch (DateTimeException ex) {
            throw new DateTimeException("无法从 TemporalAccessor 获取 Month: " +
                    temporal + " 类型为 " + temporal.getClass().getName(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 获取月份的 {@code int} 值。
     * <p>
     * 值按照 ISO-8601 标准编号，
     * 从 1（一月）到 12（十二月）。
     *
     * @return 月份，从 1（一月）到 12（十二月）
     */
    public int getValue() {
        return ordinal() + 1;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取文本表示，例如 'Jan' 或 'December'。
     * <p>
     * 此方法返回用于标识月份的文本名称，
     * 适合呈现给用户。
     * 参数控制返回文本的样式和语言环境。
     * <p>
     * 如果没有找到文本映射，则返回 {@link #getValue() 数字值}。
     *
     * @param style 所需文本的长度，不为 null
     * @param locale 使用的语言环境，不为 null
     * @return 月份的文本值，不为 null
     */
    public String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendText(MONTH_OF_YEAR, style).toFormatter(locale).format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查是否支持指定的字段。
     * <p>
     * 此方法检查是否可以查询此月份的指定字段。
     * 如果返回 false，则调用 {@link #range(TemporalField) range} 和
     * {@link #get(TemporalField) get} 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR}，则此方法返回 true。
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.isSupportedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 字段是否支持由字段决定。
     *
     * @param field 要检查的字段，null 返回 false
     * @return 如果字段在此月份上受支持则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field == MONTH_OF_YEAR;
        }
        return field != null && field.isSupportedBy(this);
    }

    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的有效最小值和最大值。
     * 此月份用于提高返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR}，则将返回
     * 月份的范围，从 1 到 12。所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.rangeRefinedBy(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 是否可以获取范围由字段决定。
     *
     * @param field 要查询范围的字段，不为 null
     * @return 字段的有效值范围，不为 null
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     */
    @Override
    public ValueRange range(TemporalField field) {
        if (field == MONTH_OF_YEAR) {
            return field.range();
        }
        return TemporalAccessor.super.range(field);
    }

    /**
     * 以 {@code int} 形式获取指定字段的值。
     * <p>
     * 此方法查询此月份的指定字段的值。
     * 返回的值将始终在字段的有效值范围内。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR}，则将返回
     * 月份的值，从 1 到 12。所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果是通过调用
     * {@code TemporalField.getFrom(TemporalAccessor)} 并将 {@code this} 作为参数传递获得的。
     * 是否可以获取值以及值的含义由字段决定。
     *
     * @param field 要获取的字段，不为 null
     * @return 字段的值，在有效值范围内
     * @throws DateTimeException 如果无法获取字段的值或值超出字段的有效值范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public int get(TemporalField field) {
        if (field == MONTH_OF_YEAR) {
            return getValue();
        }
        return TemporalAccessor.super.get(field);
    }

                /**
     * 从本月获取指定字段的值，作为 {@code long} 类型。
     * <p>
     * 此方法查询本月指定字段的值。
     * 如果由于字段不支持或其他原因无法返回值，将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR}，则将返回 1 到 12 之间的月份值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获取此方法的结果。值是否可以获取以及值的含义由字段决定。
     *
     * @param field  要获取的字段，不允许为 null
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field == MONTH_OF_YEAR) {
            return getValue();
        } else if (field instanceof ChronoField) {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.getFrom(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 返回在本月份之后指定季度数的月份。
     * <p>
     * 计算会从一年的末尾（12 月）滚动到下一年的开始（1 月）。
     * 指定的周期可以是负数。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param months  要添加的月份数，可以是正数或负数
     * @return 结果月份，不允许为 null
     */
    public Month plus(long months) {
        int amount = (int) (months % 12);
        return ENUMS[(ordinal() + (amount + 12)) % 12];
    }

    /**
     * 返回在本月份之前指定月份数的月份。
     * <p>
     * 计算会从一年的开始（1 月）滚动到上一年的末尾（12 月）。
     * 指定的周期可以是负数。
     * <p>
     * 本实例是不可变的，不受此方法调用的影响。
     *
     * @param months  要减去的月份数，可以是正数或负数
     * @return 结果月份，不允许为 null
     */
    public Month minus(long months) {
        return plus(-(months % 12));
    }

    //-----------------------------------------------------------------------
    /**
     * 获取本月的天数。
     * <p>
     * 该方法根据是否为闰年确定返回的天数。
     * <p>
     * 2 月在普通年份有 28 天，在闰年有 29 天。
     * 4 月、6 月、9 月和 11 月有 30 天。
     * 其他所有月份有 31 天。
     *
     * @param leapYear  如果需要闰年的长度，则为 true
     * @return 本月的天数，从 28 到 31
     */
    public int length(boolean leapYear) {
        switch (this) {
            case FEBRUARY:
                return (leapYear ? 29 : 28);
            case APRIL:
            case JUNE:
            case SEPTEMBER:
            case NOVEMBER:
                return 30;
            default:
                return 31;
        }
    }

    /**
     * 获取本月的最小天数。
     * <p>
     * 2 月的最小天数为 28 天。
     * 4 月、6 月、9 月和 11 月有 30 天。
     * 其他所有月份有 31 天。
     *
     * @return 本月的最小天数，从 28 到 31
     */
    public int minLength() {
        switch (this) {
            case FEBRUARY:
                return 28;
            case APRIL:
            case JUNE:
            case SEPTEMBER:
            case NOVEMBER:
                return 30;
            default:
                return 31;
        }
    }

    /**
     * 获取本月的最大天数。
     * <p>
     * 2 月的最大天数为 29 天。
     * 4 月、6 月、9 月和 11 月有 30 天。
     * 其他所有月份有 31 天。
     *
     * @return 本月的最大天数，从 29 到 31
     */
    public int maxLength() {
        switch (this) {
            case FEBRUARY:
                return 29;
            case APRIL:
            case JUNE:
            case SEPTEMBER:
            case NOVEMBER:
                return 30;
            default:
                return 31;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * 获取本月第一天对应的年份中的天数。
     * <p>
     * 该方法返回本月开始的年份中的天数，使用闰年标志确定 2 月的长度。
     *
     * @param leapYear  如果需要闰年的长度，则为 true
     * @return 本月第一天对应的年份中的天数，从 1 到 336
     */
    public int firstDayOfYear(boolean leapYear) {
        int leap = leapYear ? 1 : 0;
        switch (this) {
            case JANUARY:
                return 1;
            case FEBRUARY:
                return 32;
            case MARCH:
                return 60 + leap;
            case APRIL:
                return 91 + leap;
            case MAY:
                return 121 + leap;
            case JUNE:
                return 152 + leap;
            case JULY:
                return 182 + leap;
            case AUGUST:
                return 213 + leap;
            case SEPTEMBER:
                return 244 + leap;
            case OCTOBER:
                return 274 + leap;
            case NOVEMBER:
                return 305 + leap;
            case DECEMBER:
            default:
                return 335 + leap;
        }
    }

                /**
     * 获取本季度对应的第一个月。
     * <p>
     * 一年可以分为四个季度。
     * 该方法返回基础月份所在季度的第一个月。
     * 一月、二月和三月返回一月。
     * 四月、五月和六月返回四月。
     * 七月、八月和九月返回七月。
     * 十月、十一月和十二月返回十月。
     *
     * @return 对应此月份的季度的第一个月，不为空
     */
    public Month firstMonthOfQuarter() {
        return ENUMS[(ordinal() / 3) * 3];
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询对象查询此月份。
     * <p>
     * 该方法使用指定的查询策略对象查询此月份。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。阅读查询文档以了解
     * 该方法的结果是什么。
     * <p>
     * 该方法的结果是通过调用指定查询上的
     * {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数获得的。
     *
     * @param <R> 结果的类型
     * @param query  要调用的查询，不为空
     * @return 查询结果，可能返回 null（由查询定义）
     * @throws DateTimeException 如果无法查询（由查询定义）
     * @throws ArithmeticException 如果发生数值溢出（由查询定义）
     */
    @SuppressWarnings("unchecked")
    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.chronology()) {
            return (R) IsoChronology.INSTANCE;
        } else if (query == TemporalQueries.precision()) {
            return (R) MONTHS;
        }
        return TemporalAccessor.super.query(query);
    }

    /**
     * 调整指定的时间对象以具有此月份。
     * <p>
     * 该方法返回一个与输入类型相同的时间对象，但月份已更改为与本月份相同。
     * <p>
     * 调整等同于使用 {@link Temporal#with(TemporalField, long)}
     * 并传递 {@link ChronoField#MONTH_OF_YEAR} 作为字段。
     * 如果指定的时间对象不使用 ISO 日历系统，则会抛出 {@code DateTimeException}。
     * <p>
     * 在大多数情况下，通过使用
     * {@link Temporal#with(TemporalAdjuster)} 反转调用模式会更清晰：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisMonth.adjustInto(temporal);
     *   temporal = temporal.with(thisMonth);
     * </pre>
     * <p>
     * 例如，给定一个五月的日期，以下输出：
     * <pre>
     *   dateInMay.with(JANUARY);    // 四个月前
     *   dateInMay.with(APRIL);      // 一个月前
     *   dateInMay.with(MAY);        // 同一天
     *   dateInMay.with(JUNE);       // 一个月后
     *   dateInMay.with(DECEMBER);   // 七个月后
     * </pre>
     * <p>
     * 本实例是不可变的，调用此方法不会影响它。
     *
     * @param temporal  要调整的目标对象，不为空
     * @return 调整后的对象，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        if (Chronology.from(temporal).equals(IsoChronology.INSTANCE) == false) {
            throw new DateTimeException("仅支持 ISO 日期时间的调整");
        }
        return temporal.with(MONTH_OF_YEAR, getValue());
    }

}
