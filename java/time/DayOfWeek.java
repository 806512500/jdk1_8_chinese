
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
 * 重新分发源代码和二进制形式，无论是否修改，前提是保留上述版权声明，
 * 本许可条件列表和以下免责声明。
 *
 * 重新分发二进制形式必须在随附的文档和/或其他材料中复制上述版权声明，
 * 本许可条件列表和以下免责声明。
 *
 * 未经特定事先书面许可，不得使用 JSR-310 或其贡献者的名字来支持或推广从本软件衍生的产品。
 *
 * 本软件由版权所有者和贡献者“按原样”提供，不提供任何明示或暗示的保证，
 * 包括但不限于适销性和特定用途适用性的暗示保证。
 * 无论在任何情况下，版权所有者或贡献者均不对任何直接、间接、偶然、特殊、
 * 示例性或后果性损害（包括但不限于采购替代商品或服务；使用损失、数据丢失或
 * 利润损失；或业务中断）负责，无论是在合同、严格责任或侵权行为（包括疏忽或其他）中
 * 由于使用本软件而引起，即使已被告知可能发生此类损害。
 */
package java.time;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoUnit.DAYS;

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
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * 一周中的某一天，例如 'Tuesday'。
 * <p>
 * {@code DayOfWeek} 是一个枚举，表示一周中的7天 -
 * 星期一、星期二、星期三、星期四、星期五、星期六和星期日。
 * <p>
 * 除了文本枚举名称外，每个星期几还有一个 {@code int} 值。
 * {@code int} 值遵循 ISO-8601 标准，从 1（星期一）到 7（星期日）。
 * 建议应用程序使用枚举而不是 {@code int} 值，以确保代码清晰。
 * <p>
 * 本枚举提供了访问星期几的本地化文本形式的方法。
 * 一些地区还为星期几分配了不同的数字值，声明星期日的值为 1，但本类不支持这一点。
 * 有关本地化周编号，请参见 {@link WeekFields}。
 * <p>
 * <b>不要使用 {@code ordinal()} 来获取 {@code DayOfWeek} 的数字表示。
 * 使用 {@code getValue()} 代替。</b>
 * <p>
 * 本枚举表示一个在许多日历系统中都存在的通用概念。
 * 因此，任何具有与 ISO 日历系统完全等效的星期几概念的日历系统都可以使用此枚举。
 *
 * @implSpec
 * 这是一个不可变且线程安全的枚举。
 *
 * @since 1.8
 */
public enum DayOfWeek implements TemporalAccessor, TemporalAdjuster {

    /**
     * 一周中的星期一的单例实例。
     * 这个值为 {@code 1}。
     */
    MONDAY,
    /**
     * 一周中的星期二的单例实例。
     * 这个值为 {@code 2}。
     */
    TUESDAY,
    /**
     * 一周中的星期三的单例实例。
     * 这个值为 {@code 3}。
     */
    WEDNESDAY,
    /**
     * 一周中的星期四的单例实例。
     * 这个值为 {@code 4}。
     */
    THURSDAY,
    /**
     * 一周中的星期五的单例实例。
     * 这个值为 {@code 5}。
     */
    FRIDAY,
    /**
     * 一周中的星期六的单例实例。
     * 这个值为 {@code 6}。
     */
    SATURDAY,
    /**
     * 一周中的星期日的单例实例。
     * 这个值为 {@code 7}。
     */
    SUNDAY;
    /**
     * 所有常量的私有缓存。
     */
    private static final DayOfWeek[] ENUMS = DayOfWeek.values();

    //-----------------------------------------------------------------------
    /**
     * 从 {@code int} 值获取 {@code DayOfWeek} 的实例。
     * <p>
     * {@code DayOfWeek} 是一个枚举，表示一周中的7天。
     * 此工厂允许从 {@code int} 值获取枚举。
     * {@code int} 值遵循 ISO-8601 标准，从 1（星期一）到 7（星期日）。
     *
     * @param dayOfWeek  要表示的星期几，从 1（星期一）到 7（星期日）
     * @return 星期几的单例，不为空
     * @throws DateTimeException 如果星期几无效
     */
    public static DayOfWeek of(int dayOfWeek) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new DateTimeException("Invalid value for DayOfWeek: " + dayOfWeek);
        }
        return ENUMS[dayOfWeek - 1];
    }

    //-----------------------------------------------------------------------
    /**
     * 从时间对象获取 {@code DayOfWeek} 的实例。
     * <p>
     * 此方法根据指定的时间对象获取星期几。
     * {@code TemporalAccessor} 表示一组任意的日期和时间信息，
     * 该工厂将其转换为 {@code DayOfWeek} 的实例。
     * <p>
     * 转换提取 {@link ChronoField#DAY_OF_WEEK DAY_OF_WEEK} 字段。
     * <p>
     * 此方法匹配 {@link TemporalQuery} 功能接口的签名，
     * 允许通过方法引用作为查询使用，{@code DayOfWeek::from}。
     *
     * @param temporal  要转换的时间对象，不为空
     * @return 星期几，不为空
     * @throws DateTimeException 如果无法从 {@code TemporalAccessor} 转换为 {@code DayOfWeek}
     */
    public static DayOfWeek from(TemporalAccessor temporal) {
        if (temporal instanceof DayOfWeek) {
            return (DayOfWeek) temporal;
        }
        try {
            return of(temporal.get(DAY_OF_WEEK));
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain DayOfWeek from TemporalAccessor: " +
                    temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

                //-----------------------------------------------------------------------
    /**
     * 获取星期几的 {@code int} 值。
     * <p>
     * 值按照 ISO-8601 标准编号，从 1（星期一）到 7（星期日）。
     * 有关本地化的星期编号，请参见 {@link java.time.temporal.WeekFields#dayOfWeek()}。
     *
     * @return 星期几，从 1（星期一）到 7（星期日）
     */
    public int getValue() {
        return ordinal() + 1;
    }

    //-----------------------------------------------------------------------
    /**
     * 获取文本表示，例如 'Mon' 或 'Friday'。
     * <p>
     * 这返回用于标识星期几的文本名称，适合呈现给用户。
     * 参数控制返回文本的样式和语言环境。
     * <p>
     * 如果找不到文本映射，则返回 {@link #getValue() 数值}。
     *
     * @param style  所需文本的长度，不为空
     * @param locale  使用的语言环境，不为空
     * @return 星期几的文本值，不为空
     */
    public String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendText(DAY_OF_WEEK, style).toFormatter(locale).format(this);
    }

    //-----------------------------------------------------------------------
    /**
     * 检查指定的字段是否受支持。
     * <p>
     * 检查此星期几是否可以查询指定的字段。
     * 如果为 false，则调用 {@link #range(TemporalField) range} 和
     * {@link #get(TemporalField) get} 方法将抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField#DAY_OF_WEEK DAY_OF_WEEK}，则
     * 此方法返回 true。
     * 所有其他 {@code ChronoField} 实例将返回 false。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.isSupportedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得。
     * 字段是否受支持由字段确定。
     *
     * @param field  要检查的字段，null 返回 false
     * @return 如果字段在此星期几上受支持，则返回 true，否则返回 false
     */
    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field == DAY_OF_WEEK;
        }
        return field != null && field.isSupportedBy(this);
    }

    /**
     * 获取指定字段的有效值范围。
     * <p>
     * 范围对象表示字段的最小和最大有效值。
     * 此星期几用于增强返回范围的准确性。
     * 如果由于字段不受支持或其他原因无法返回范围，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField#DAY_OF_WEEK DAY_OF_WEEK}，则
     * 将返回从 1 到 7 的星期几范围。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.rangeRefinedBy(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得。
     * 范围是否可以获取由字段确定。
     *
     * @param field  要查询范围的字段，不为空
     * @return 字段的有效值范围，不为空
     * @throws DateTimeException 如果无法获取字段的范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     */
    @Override
    public ValueRange range(TemporalField field) {
        if (field == DAY_OF_WEEK) {
            return field.range();
        }
        return TemporalAccessor.super.range(field);
    }

    /**
     * 以 {@code int} 形式从此星期几获取指定字段的值。
     * <p>
     * 此方法查询此星期几的指定字段的值。
     * 返回的值将始终在字段的有效值范围内。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField#DAY_OF_WEEK DAY_OF_WEEK}，则
     * 将返回从 1 到 7 的星期几值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得。值是否可以获取，
     * 以及值的含义由字段确定。
     *
     * @param field  要获取的字段，不为空
     * @return 字段的值，在有效值范围内
     * @throws DateTimeException 如果无法获取字段的值或
     *         值超出字段的有效值范围
     * @throws UnsupportedTemporalTypeException 如果字段不受支持或
     *         值范围超出 {@code int}
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public int get(TemporalField field) {
        if (field == DAY_OF_WEEK) {
            return getValue();
        }
        return TemporalAccessor.super.get(field);
    }

    /**
     * 以 {@code long} 形式从此星期几获取指定字段的值。
     * <p>
     * 此方法查询此星期几的指定字段的值。
     * 如果由于字段不受支持或其他原因无法返回值，则抛出异常。
     * <p>
     * 如果字段是 {@link ChronoField#DAY_OF_WEEK DAY_OF_WEEK}，则
     * 将返回从 1 到 7 的星期几值。
     * 所有其他 {@code ChronoField} 实例将抛出 {@code UnsupportedTemporalTypeException}。
     * <p>
     * 如果字段不是 {@code ChronoField}，则此方法的结果
     * 通过调用 {@code TemporalField.getFrom(TemporalAccessor)}
     * 并将 {@code this} 作为参数传递来获得。值是否可以获取，
     * 以及值的含义由字段确定。
     *
     * @param field  要获取的字段，不为空
     * @return 字段的值
     * @throws DateTimeException 如果无法获取字段的值
     * @throws UnsupportedTemporalTypeException 如果字段不受支持
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public long getLong(TemporalField field) {
        if (field == DAY_OF_WEEK) {
            return getValue();
        } else if (field instanceof ChronoField) {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.getFrom(this);
    }

                //-----------------------------------------------------------------------
    /**
     * 返回从当前日期开始指定天数后的星期几。
     * <p>
     * 计算会从周末（周日）滚动到周一。
     * 指定的周期可以是负数。
     * <p>
     * 该实例是不可变的，此方法调用不会影响它。
     *
     * @param days  要添加的天数，可以是正数或负数
     * @return 结果的星期几，不为空
     */
    public DayOfWeek plus(long days) {
        int amount = (int) (days % 7);
        return ENUMS[(ordinal() + (amount + 7)) % 7];
    }

    /**
     * 返回从当前日期开始指定天数前的星期几。
     * <p>
     * 计算会从年初（周一）滚动到周日。
     * 指定的周期可以是负数。
     * <p>
     * 该实例是不可变的，此方法调用不会影响它。
     *
     * @param days  要减去的天数，可以是正数或负数
     * @return 结果的星期几，不为空
     */
    public DayOfWeek minus(long days) {
        return plus(-(days % 7));
    }

    //-----------------------------------------------------------------------
    /**
     * 使用指定的查询来查询此星期几。
     * <p>
     * 使用指定的查询策略对象查询此星期几。
     * {@code TemporalQuery} 对象定义了获取结果的逻辑。阅读查询的文档以了解此方法的结果。
     * <p>
     * 通过调用指定查询上的 {@link TemporalQuery#queryFrom(TemporalAccessor)} 方法并传递 {@code this} 作为参数来获取此方法的结果。
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
        if (query == TemporalQueries.precision()) {
            return (R) DAYS;
        }
        return TemporalAccessor.super.query(query);
    }

    /**
     * 调整指定的时间对象以具有此星期几。
     * <p>
     * 返回与输入具有相同可观察类型的临时对象，但星期几已更改为与此相同。
     * <p>
     * 调整等同于使用 {@link Temporal#with(TemporalField, long)}，传递 {@link ChronoField#DAY_OF_WEEK} 作为字段。
     * 请注意，这在周一到周日的一周内向前或向后调整。
     * 有关本地化周开始日的更多信息，请参见 {@link java.time.temporal.WeekFields#dayOfWeek()}。
     * 有关具有更多控制的其他调整器，请参见 {@code TemporalAdjuster}，例如 {@code next(MONDAY)}。
     * <p>
     * 在大多数情况下，通过使用 {@link Temporal#with(TemporalAdjuster)} 反转调用模式会更清晰：
     * <pre>
     *   // 这两行是等效的，但推荐第二种方法
     *   temporal = thisDayOfWeek.adjustInto(temporal);
     *   temporal = temporal.with(thisDayOfWeek);
     * </pre>
     * <p>
     * 例如，给定一个周三的日期，以下输出：
     * <pre>
     *   dateOnWed.with(MONDAY);     // 两天前
     *   dateOnWed.with(TUESDAY);    // 一天前
     *   dateOnWed.with(WEDNESDAY);  // 同一天
     *   dateOnWed.with(THURSDAY);   // 一天后
     *   dateOnWed.with(FRIDAY);     // 两天后
     *   dateOnWed.with(SATURDAY);   // 三天后
     *   dateOnWed.with(SUNDAY);     // 四天后
     * </pre>
     * <p>
     * 该实例是不可变的，此方法调用不会影响它。
     *
     * @param temporal  要调整的目标对象，不为空
     * @return 调整后的对象，不为空
     * @throws DateTimeException 如果无法进行调整
     * @throws ArithmeticException 如果发生数值溢出
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(DAY_OF_WEEK, getValue());
    }

}
