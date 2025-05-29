/*
 * 版权所有 (c) 2012, 2015, Oracle 及/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款的约束。
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
 * 版权所有 (c) 2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * 保留所有权利。
 *
 * 重新分发和使用源代码和二进制形式，无论是否修改，前提是满足以下条件：
 *
 *  * 重新分发源代码必须保留上述版权声明，此条件列表和以下免责声明。
 *
 *  * 重新分发二进制形式必须在随分发提供的文档和/或其他材料中复制上述版权声明，此条件列表和以下免责声明。
 *
 *  * 未经特定事先书面许可，不得使用 JSR-310 或其贡献者的名字来支持或推广从本软件衍生的产品。
 *
 * 本软件由版权所有者和贡献者“按原样”提供，不提供任何明示或暗示的保证，包括但不限于适销性和特定用途适用性的暗示保证。在任何情况下，版权所有者或贡献者均不对任何直接、间接、偶然、特殊、示范性或后果性损害（包括但不限于采购替代商品或服务；使用损失、数据损失或利润损失；或业务中断）承担责任，无论是基于合同、严格责任还是侵权（包括疏忽或其他）理论，即使已被告知此类损害的可能性。
 */

/**
 * <p>
 * 日期、时间、瞬间和持续时间的主要 API。
 * </p>
 * <p>
 * 本处定义的类表示主要的日期时间概念，包括瞬间、持续时间、日期、时间、时区和周期。
 * 它们基于 ISO 日历系统，这是遵循公历规则的 <i>事实上的</i> 世界日历。
 * 所有类都是不可变的和线程安全的。
 * </p>
 * <p>
 * 每个日期时间实例由字段组成，这些字段通过 API 方便地提供。对于字段的低级访问，请参阅 {@code java.time.temporal} 包。
 * 每个类都支持打印和解析各种日期和时间。有关自定义选项，请参阅 {@code java.time.format} 包。
 * </p>
 * <p>
 * {@code java.time.chrono} 包含日历中立的 API
 * {@link java.time.chrono.ChronoLocalDate ChronoLocalDate}，
 * {@link java.time.chrono.ChronoLocalDateTime ChronoLocalDateTime}，
 * {@link java.time.chrono.ChronoZonedDateTime ChronoZonedDateTime} 和
 * {@link java.time.chrono.Era Era}。
 * 这些 API 旨在供需要使用本地化日历的应用程序使用。
 * 建议应用程序在系统边界（如数据库或网络）之间使用本包中的 ISO-8601 日期和时间类。
 * 日历中立的 API 应保留用于与用户的交互。
 * </p>
 *
 * <h3>日期和时间</h3>
 * <p>
 * {@link java.time.Instant} 实质上是一个数字时间戳。
 * 可以从 {@link java.time.Clock} 获取当前的 Instant。
 * 这对于记录和持久化时间点非常有用，过去通常与存储 {@link java.lang.System#currentTimeMillis()} 的结果相关联。
 * </p>
 * <p>
 * {@link java.time.LocalDate} 存储没有时间的日期。
 * 这存储了一个日期，如 '2010-12-03'，可以用于存储生日。
 * </p>
 * <p>
 * {@link java.time.LocalTime} 存储没有日期的时间。
 * 这存储了一个时间，如 '11:30'，可以用于存储营业或关门时间。
 * </p>
 * <p>
 * {@link java.time.LocalDateTime} 存储日期和时间。
 * 这存储了一个日期时间，如 '2010-12-03T11:30'。
 * </p>
 * <p>
 * {@link java.time.ZonedDateTime} 存储带有时区的日期和时间。
 * 如果您希望执行考虑 {@link java.time.ZoneId}（如 'Europe/Paris'）的日期和时间的精确计算，这将非常有用。
 * 在可能的情况下，建议使用没有时区的更简单的类。
 * 广泛使用时区往往会增加应用程序的复杂性。
 * </p>
 *
 * <h3>持续时间和周期</h3>
 * <p>
 * 除了日期和时间之外，API 还允许存储时间和持续时间。
 * {@link java.time.Duration} 是时间线上的简单时间度量，以纳秒为单位。
 * {@link java.time.Period} 以人类有意义的单位（如年或天）表达时间量。
 * </p>
 *
 * <h3>其他值类型</h3>
 * <p>
 * {@link java.time.Month} 存储单独的月份。
 * 这存储了一个单独的月份，如 'DECEMBER'。
 * </p>
 * <p>
 * {@link java.time.DayOfWeek} 存储单独的星期几。
 * 这存储了一个单独的星期几，如 'TUESDAY'。
 * </p>
 * <p>
 * {@link java.time.Year} 存储单独的年份。
 * 这存储了一个单独的年份，如 '2010'。
 * </p>
 * <p>
 * {@link java.time.YearMonth} 存储没有天或时间的年份和月份。
 * 这存储了一个年份和月份，如 '2010-12'，可以用于存储信用卡过期日期。
 * </p>
 * <p>
 * {@link java.time.MonthDay} 存储没有年份或时间的月份和日期。
 * 这存储了一个月份和日期，如 '--12-03'，可以用于存储不存储年份的年度事件，如生日。
 * </p>
 * <p>
 * {@link java.time.OffsetTime} 存储没有日期的 UTC 偏移时间。
 * 这存储了一个时间，如 '11:30+01:00'。
 * {@link java.time.ZoneOffset ZoneOffset} 的形式为 '+01:00'。
 * </p>
 * <p>
 * {@link java.time.OffsetDateTime} 存储带有 UTC 偏移的日期和时间。
 * 这存储了一个日期时间，如 '2010-12-03T11:30+01:00'。
 * 这有时出现在 XML 消息和其他形式的持久化中，但包含的信息比完整时区少。
 * </p>
 *
 * <h3>包规范</h3>
 * <p>
 * 除非另有说明，否则将 null 参数传递给此包中任何类或接口的构造函数或方法将导致抛出 {@link java.lang.NullPointerException NullPointerException}。
 * Javadoc "@param" 定义用于总结 null 行为。
 * 每个方法中没有显式记录 "@throws {@link java.lang.NullPointerException}"。
 * </p>
 * <p>
 * 所有计算都应检查数值溢出并抛出 {@link java.lang.ArithmeticException} 或 {@link java.time.DateTimeException}。
 * </p>
 *
 * <h3>设计说明（非规范性）</h3>
 * <p>
 * API 设计为尽早拒绝 null 并明确这种行为。
 * 一个关键的例外是任何接受对象并返回布尔值的方法，为了检查或验证，通常会对 null 返回 false。
 * </p>
 * <p>
 * API 设计为在主要高级 API 中尽可能类型安全。
 * 因此，对于日期、时间、日期时间以及偏移和时区的不同概念，有单独的类。
 * 这可能会显得类很多，但大多数应用程序可以从五种日期/时间类型开始。
 * <ul>
 * <li>{@link java.time.Instant} - 时间戳</li>
 * <li>{@link java.time.LocalDate} - 没有时间或任何偏移或时区引用的日期</li>
 * <li>{@link java.time.LocalTime} - 没有日期或任何偏移或时区引用的时间</li>
 * <li>{@link java.time.LocalDateTime} - 结合日期和时间，但仍然没有偏移或时区</li>
 * <li>{@link java.time.ZonedDateTime} - 带有时区和从 UTC/Greenwich 解析的偏移的“完整”日期时间</li>
 * </ul>
 * <p>
 * {@code Instant} 是与 {@code java.util.Date} 最接近的等效类。
 * {@code ZonedDateTime} 是与 {@code java.util.GregorianCalendar} 最接近的等效类。
 * </p>
 * <p>
 * 在可能的情况下，应用程序应使用 {@code LocalDate}、{@code LocalTime} 和 {@code LocalDateTime} 更好地建模领域。
 * 例如，生日应存储在 {@code LocalDate} 中。
 * 请记住，使用 {@linkplain java.time.ZoneId 时区}（如 'Europe/Paris'）会增加计算的复杂性。
 * 许多应用程序可以仅使用 {@code LocalDate}、{@code LocalTime} 和 {@code Instant} 编写，时区仅在用户界面（UI）层添加。
 * </p>
 * <p>
 * 偏移日期时间类型 {@code OffsetTime} 和 {@code OffsetDateTime} 主要用于网络协议和数据库访问。
 * 例如，大多数数据库不能自动存储像 'Europe/Paris' 这样的时区，但可以存储像 '+02:00' 这样的偏移。
 * </p>
 * <p>
 * 还提供了表示日期最重要子部分的类，包括 {@code Month}、
 * {@code DayOfWeek}、{@code Year}、{@code YearMonth} 和 {@code MonthDay}。
 * 这些可以用于建模更复杂的日期时间概念。
 * 例如，{@code YearMonth} 适用于表示信用卡过期日期。
 * </p>
 * <p>
 * 请注意，虽然有许多类表示日期的不同方面，但处理时间不同方面的类相对较少。
 * 将类型安全推到逻辑结论会导致小时-分钟、小时-分钟-秒和小时-分钟-秒-纳秒的类。
 * 尽管逻辑上纯粹，但这不是一个实用的选择，因为由于日期和时间的组合，类的数量几乎会增加三倍。
 * 因此，{@code LocalTime} 用于所有时间精度，使用零表示较低的精度。
 * </p>
 * <p>
 * 将完整的类型安全推到最终结论可能会为日期时间的每个字段（如 HourOfDay 和 DayOfMonth）提供单独的类。
 * 这种方法已经尝试过，但在 Java 语言中过于复杂，缺乏可用性。
 * 周期也存在类似的问题。
 * 为每个周期单位（如 Years 和 Minutes）提供单独的类是有道理的。
 * 然而，这会产生很多类和类型转换问题。
 * 因此，提供的日期时间类型集是纯度和实用性的折衷。
 * </p>
 * <p>
 * API 在方法数量方面具有相对较大的表面区域。
 * 通过使用一致的方法前缀来管理这一点。
 * <ul>
 * <li>{@code of} - 静态工厂方法</li>
 * <li>{@code parse} - 专注于解析的静态工厂方法</li>
 * <li>{@code get} - 获取某物的值</li>
 * <li>{@code is} - 检查某物是否为真</li>
 * <li>{@code with} - 不可变的 setter 等价物</li>
 * <li>{@code plus} - 向对象添加数量</li>
 * <li>{@code minus} - 从对象中减去数量</li>
 * <li>{@code to} - 将此对象转换为另一种类型</li>
 * <li>{@code at} - 将此对象与另一个对象结合，如 {@code date.atTime(time)}</li>
 * </ul>
 * <p>
 * 多个日历系统是设计挑战中的一个尴尬的添加。
 * 第一个原则是大多数用户需要标准的 ISO 日历系统。
 * 因此，主要类是 ISO 专用的。第二个原则是大多数需要非 ISO 日历系统的用户需要它进行用户交互，因此这是一个 UI 本地化问题。
 * 因此，日期和时间对象应作为 ISO 对象存储在数据模型和持久存储中，仅在显示时转换为本地日历。
 * 日历系统应单独存储在用户偏好设置中。
 * </p>
 * <p>
 * 然而，有些有限的用例中，用户认为他们需要在应用程序中存储和使用任意日历系统的日期。
 * 这由 {@link java.time.chrono.ChronoLocalDate} 支持，但在使用它之前必须仔细阅读该接口的 Javadoc 中的所有相关警告。
 * 总的来说，需要在多个日历系统之间进行通用互操作的应用程序通常需要以与仅使用 ISO 日历的应用程序非常不同的方式编写，因此大多数应用程序应仅使用 ISO 并避免使用 {@code ChronoLocalDate}。
 * </p>
 * <p>
 * API 还设计为用户可扩展的，因为计算时间的方法有很多。
 * 通过 {@link java.time.temporal.TemporalAccessor TemporalAccessor} 和
 * {@link java.time.temporal.Temporal Temporal} 访问的 {@linkplain java.time.temporal.TemporalField 字段} 和 {@linkplain java.time.temporal.TemporalUnit 单位} API 为应用程序提供了相当大的灵活性。
 * 此外，{@link java.time.temporal.TemporalQuery TemporalQuery} 和
 * {@link java.time.temporal.TemporalAdjuster TemporalAdjuster} 接口提供了日常功能，使代码接近业务需求：
 * </p>
 * <pre>
 *   LocalDate customerBirthday = customer.loadBirthdayFromDatabase();
 *   LocalDate today = LocalDate.now();
 *   if (customerBirthday.equals(today)) {
 *     LocalDate specialOfferExpiryDate = today.plusWeeks(2).with(next(FRIDAY));
 *     customer.sendBirthdaySpecialOffer(specialOfferExpiryDate);
 *   }
 *
 * </pre>
 *
 * @since JDK1.8
 */
package java.time;
