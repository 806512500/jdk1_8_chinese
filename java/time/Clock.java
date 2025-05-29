
/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其关联公司。保留所有权利。
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
 * 版权所有 (c) 2007-2012, Stephen Colebourne 和 Michael Nascimento Santos
 *
 * 保留所有权利。
 *
 * 重新分发源代码和二进制形式，无论是否修改，均需满足以下条件：
 *
 *  * 重新分发源代码必须保留上述版权声明，此条件列表和以下免责声明。
 *
 *  * 重新分发二进制形式必须在提供的文档和/或其他材料中复制上述版权声明，此条件列表和以下免责声明。
 *
 *  * 未经特定事先书面许可，不得使用 JSR-310 或其贡献者的名字来支持或推广从本软件衍生的产品。
 *
 * 本软件由版权所有者和贡献者“按原样”提供，不附带任何明示或暗示的保证，包括但不限于适销性和特定用途适用性的暗示保证。在任何情况下，版权所有者或贡献者均不对任何直接、间接、偶然、特殊、示范性或后果性损害（包括但不限于采购替代商品或服务；使用损失、数据丢失或利润损失；或业务中断）负责，无论是在合同、严格责任还是侵权行为（包括疏忽或其他）中，即使已告知可能发生此类损害。
 */
package java.time;

import static java.time.LocalTime.NANOS_PER_MINUTE;
import static java.time.LocalTime.NANOS_PER_SECOND;

import java.io.Serializable;
import java.util.Objects;
import java.util.TimeZone;

/**
 * 提供使用时区访问当前时刻、日期和时间的时钟。
 * <p>
 * 该类的实例用于查找当前时刻，该时刻可以使用存储的时区解释为当前日期和时间。
 * 因此，时钟可以用于替代 {@link System#currentTimeMillis()}
 * 和 {@link TimeZone#getDefault()}。
 * <p>
 * 使用 {@code Clock} 是可选的。所有关键日期时间类也有一个使用系统时钟在默认时区的
 * {@code now()} 工厂方法。此抽象的主要目的是允许在需要时插入替代时钟。应用程序使用对象来获取当前时间，而不是静态方法。这可以简化测试。
 * <p>
 * 应用程序的最佳实践是将 {@code Clock} 传递给任何需要当前时刻的方法。依赖注入框架是实现这一点的一种方式：
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
 * 这种方法允许在测试期间使用替代时钟，如 {@link #fixed(Instant, ZoneId) 固定}
 * 或 {@link #offset(Clock, Duration) 偏移}。
 * <p>
 * {@code system} 工厂方法提供基于最佳可用系统时钟的时钟。这可能使用 {@link System#currentTimeMillis()}，或者如果可用的话，使用更高分辨率的时钟。
 *
 * @implSpec
 * 必须谨慎实现此抽象类以确保其他类正确运行。
 * 所有可以实例化的实现必须是最终的、不可变的和线程安全的。
 * <p>
 * 主要方法定义允许抛出异常。
 * 在正常情况下，不会抛出任何异常，但一种可能的实现是从网络中的中央时间服务器获取时间。显然，在这种情况下，查找可能会失败，因此允许该方法抛出异常。
 * <p>
 * 从 {@code Clock} 返回的时刻基于忽略闰秒的时间尺度，如 {@link Instant} 中所述。如果实现包装了提供闰秒信息的源，则应使用某种机制来“平滑”闰秒。
 * Java 时间尺度要求使用 UTC-SLS，但时钟实现可以选择它们对时间尺度的准确性，只要它们记录了它们的工作方式即可。
 * 因此，实现不需要实际执行 UTC-SLS 调整或以其他方式意识到闰秒。
 * <p>
 * 实现应在可能的情况下实现 {@code Serializable} 并且必须记录它们是否支持序列化。
 *
 * @implNote
 * 提供的时钟实现基于 {@link System#currentTimeMillis()}。
 * 该方法对时钟的准确性几乎没有保证。
 * 需要更准确时钟的应用程序必须使用不同的外部时钟（如 NTP 服务器）自行实现此抽象类。
 *
 * @since 1.8
 */
public abstract class Clock {

    /**
     * 获取使用最佳可用系统时钟返回当前时刻的时钟，并使用 UTC 时区转换为日期和时间。
     * <p>
     * 当需要当前时刻而不需要日期或时间时，应使用此时钟，而不是 {@link #systemDefaultZone()}。
     * <p>
     * 该时钟基于最佳可用系统时钟。
     * 这可能使用 {@link System#currentTimeMillis()}，或者如果可用的话，使用更高分辨率的时钟。
     * <p>
     * 从时刻转换为日期或时间使用 {@linkplain ZoneOffset#UTC UTC 时区}。
     * <p>
     * 返回的实现是不可变的、线程安全的和 {@code Serializable}。
     * 它等同于 {@code system(ZoneOffset.UTC)}。
     *
     * @return 使用最佳可用系统时钟在 UTC 时区的时钟，不为空
     */
    public static Clock systemUTC() {
        return new SystemClock(ZoneOffset.UTC);
    }

                /**
     * 获取一个时钟，该时钟使用最佳可用系统时钟返回当前时间点，并使用默认时区转换为日期和时间。
     * <p>
     * 此时钟基于最佳可用系统时钟。
     * 这可能会使用 {@link System#currentTimeMillis()}，或者如果有更高分辨率的时钟可用，则使用该时钟。
     * <p>
     * 使用此方法会将对默认时区的依赖性硬编码到您的应用程序中。
     * 建议避免这种情况，并尽可能使用特定的时区。
     * 当您需要当前时间点而不需要日期或时间时，应使用 {@link #systemUTC() UTC 时钟}。
     * <p>
     * 返回的实现是不可变的、线程安全的并且是 {@code Serializable}。
     * 它等同于 {@code system(ZoneId.systemDefault())}。
     *
     * @return 使用默认时区中的最佳可用系统时钟的时钟，不为空
     * @see ZoneId#systemDefault()
     */
    public static Clock systemDefaultZone() {
        return new SystemClock(ZoneId.systemDefault());
    }

    /**
     * 获取一个时钟，该时钟使用最佳可用系统时钟返回当前时间点。
     * <p>
     * 此时钟基于最佳可用系统时钟。
     * 这可能会使用 {@link System#currentTimeMillis()}，或者如果有更高分辨率的时钟可用，则使用该时钟。
     * <p>
     * 从时间点转换为日期或时间使用指定的时区。
     * <p>
     * 返回的实现是不可变的、线程安全的并且是 {@code Serializable}。
     *
     * @param zone  用于将时间点转换为日期时间的时区，不为空
     * @return 使用指定时区中的最佳可用系统时钟的时钟，不为空
     */
    public static Clock system(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        return new SystemClock(zone);
    }

    //-------------------------------------------------------------------------
    /**
     * 获取一个时钟，该时钟使用最佳可用系统时钟以整秒为单位返回当前时间点。
     * <p>
     * 此时钟的纳秒字段始终设置为零。
     * 这确保了可见时间以整秒为单位跳动。
     * 底层时钟是最佳可用系统时钟，等同于使用 {@link #system(ZoneId)}。
     * <p>
     * 实现可能出于性能原因使用缓存策略。
     * 因此，通过此时钟观察到的秒的开始时间可能晚于直接通过底层时钟观察到的时间。
     * <p>
     * 返回的实现是不可变的、线程安全的并且是 {@code Serializable}。
     * 它等同于 {@code tick(system(zone), Duration.ofSeconds(1))}。
     *
     * @param zone  用于将时间点转换为日期时间的时区，不为空
     * @return 使用指定时区以整秒为单位跳动的时钟，不为空
     */
    public static Clock tickSeconds(ZoneId zone) {
        return new TickClock(system(zone), NANOS_PER_SECOND);
    }

    /**
     * 获取一个时钟，该时钟使用最佳可用系统时钟以整分钟为单位返回当前时间点。
     * <p>
     * 此时钟的纳秒字段和秒字段始终设置为零。
     * 这确保了可见时间以整分钟为单位跳动。
     * 底层时钟是最佳可用系统时钟，等同于使用 {@link #system(ZoneId)}。
     * <p>
     * 实现可能出于性能原因使用缓存策略。
     * 因此，通过此钟观察到的分钟的开始时间可能晚于直接通过底层时钟观察到的时间。
     * <p>
     * 返回的实现是不可变的、线程安全的并且是 {@code Serializable}。
     * 它等同于 {@code tick(system(zone), Duration.ofMinutes(1))}。
     *
     * @param zone  用于将时间点转换为日期时间的时区，不为空
     * @return 使用指定时区以整分钟为单位跳动的时钟，不为空
     */
    public static Clock tickMinutes(ZoneId zone) {
        return new TickClock(system(zone), NANOS_PER_MINUTE);
    }

    /**
     * 获取一个时钟，该时钟从指定的时钟返回截断到指定持续时间最近发生的时间点。
     * <p>
     * 此时钟将仅按指定的持续时间跳动。因此，如果持续时间是半秒，时钟将返回截断到半秒的时间点。
     * <p>
     * 跳动持续时间必须为正。如果它有一个小于一毫秒的部分，那么整个持续时间必须能够被一秒整除而没有余数。所有正常的跳动持续时间都将满足这些标准，包括任何小时、分钟、秒和毫秒的倍数，以及合理的纳秒持续时间，如 20ns、250,000ns 和 500,000ns。
     * <p>
     * 持续时间为零或一纳秒将没有截断效果。
     * 传递这些值将返回底层时钟。
     * <p>
     * 实现可能出于性能原因使用缓存策略。
     * 因此，通过此钟观察到的请求持续时间的开始时间可能晚于直接通过底层时钟观察到的时间。
     * <p>
     * 返回的实现是不可变的、线程安全的并且是 {@code Serializable}，前提是基础时钟也是如此。
     *
     * @param baseClock  用于基于的底层时钟，不为空
     * @param tickDuration  每个可见跳动的持续时间，不为负，不为空
     * @return 以持续时间的整数单位跳动的时钟，不为空
     * @throws IllegalArgumentException 如果持续时间为负，或者有一个小于一毫秒的部分，使得整个持续时间不能被一秒整除
     * @throws ArithmeticException 如果持续时间太大，无法表示为纳秒
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
            // 好的，可以被一秒整除而没有余数
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
     * 获取一个始终返回相同时间点的时钟。
     * <p>
     * 该时钟仅返回指定的时间点。
     * 因此，它并不是传统意义上的时钟。
     * 这种时钟的主要用例是在测试中，固定的时钟确保
     * 测试不依赖于当前时钟。
     * <p>
     * 返回的实现是不可变的、线程安全的并且是{@code Serializable}的。
     *
     * @param fixedInstant  用作时钟的时间点，不得为null
     * @param zone  用于将时间点转换为日期时间的时区，不得为null
     * @return 始终返回相同时间点的时钟，不得为null
     */
    public static Clock fixed(Instant fixedInstant, ZoneId zone) {
        Objects.requireNonNull(fixedInstant, "fixedInstant");
        Objects.requireNonNull(zone, "zone");
        return new FixedClock(fixedInstant, zone);
    }

    //-------------------------------------------------------------------------
    /**
     * 获取一个返回从指定时钟加上指定持续时间的时间点的时钟。
     * <p>
     * 该时钟包装另一个时钟，返回的时间点比指定的持续时间晚。如果持续时间是负数，时间点将
     * 早于当前日期和时间。
     * 这种时钟的主要用例是模拟未来或过去的运行。
     * <p>
     * 持续时间为零则没有偏移效果。
     * 传递零将返回基础时钟。
     * <p>
     * 返回的实现是不可变的、线程安全的并且是{@code Serializable}的，前提是基础时钟也是如此。
     *
     * @param baseClock  要添加持续时间的基础时钟，不得为null
     * @param offsetDuration  要添加的持续时间，不得为null
     * @return 基于基础时钟并添加了持续时间的时钟，不得为null
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
     * 时钟通常会获取当前时间点，然后使用时区将其转换为日期或时间。此方法返回使用的时区。
     *
     * @return 用于解释时间点的时区，不得为null
     */
    public abstract ZoneId getZone();

    /**
     * 返回一个基于此时钟但使用不同时区的时钟副本。
     * <p>
     * 时钟通常会获取当前时间点，然后使用时区将其转换为日期或时间。此方法返回一个具有
     * 类似属性但使用不同时区的时钟。
     *
     * @param zone  要更改的时区，不得为null
     * @return 基于此时钟并使用指定时区的时钟，不得为null
     */
    public abstract Clock withZone(ZoneId zone);

    //-------------------------------------------------------------------------
    /**
     * 获取时钟的当前毫秒时间点。
     * <p>
     * 这返回基于1970-01-01T00:00Z (UTC)的毫秒时间点。
     * 这等同于{@link System#currentTimeMillis()}的定义。
     * <p>
     * 大多数应用程序应避免使用此方法，而应使用{@link Instant}来表示
     * 时间线上的时间点，而不是原始的毫秒值。
     * 提供此方法是为了在高性能用例中使用时钟，
     * 在这些情况下，创建对象是不可接受的。
     * <p>
     * 当前默认实现调用{@link #instant}。
     *
     * @return 从此时钟获取的当前毫秒时间点，基于
     *  Java纪元1970-01-01T00:00Z (UTC)，不得为null
     * @throws DateTimeException 如果无法获取时间点，大多数实现不会抛出此异常
     */
    public long millis() {
        return instant().toEpochMilli();
    }

    //-----------------------------------------------------------------------
    /**
     * 获取时钟的当前时间点。
     * <p>
     * 这返回一个表示时钟定义的当前时间点的实例。
     *
     * @return 从此时钟获取的当前时间点，不得为null
     * @throws DateTimeException 如果无法获取时间点，大多数实现不会抛出此异常
     */
    public abstract Instant instant();

    //-----------------------------------------------------------------------
    /**
     * 检查此钟是否等于另一个钟。
     * <p>
     * 时钟应覆盖此方法以基于其状态进行比较，并满足{@link Object#equals}的契约。
     * 如果未覆盖，则行为由{@link Object#equals}定义。
     *
     * @param obj  要检查的对象，null返回false
     * @return 如果此钟等于其他钟，则返回true
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * 此钟的哈希码。
     * <p>
     * 时钟应基于其状态覆盖此方法，并满足{@link Object#hashCode}的契约。
     * 如果未覆盖，则行为由{@link Object#hashCode}定义。
     *
     * @return 一个合适的哈希码
     */
    @Override
    public  int hashCode() {
        return super.hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * 实现一个始终返回来自{@link System#currentTimeMillis()}的最新时间的时钟。
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
            if (zone.equals(this.zone)) {  // 故意引发空指针异常
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
     * 实现一个始终返回同一时刻的时钟。
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
            if (zone.equals(this.zone)) {  // 故意引发空指针异常
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
     * 实现一个在基础时钟上添加偏移量的时钟。
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
            if (zone.equals(baseClock.getZone())) {  // 故意引发空指针异常
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
     * 实现一个在基础时钟上添加偏移量的时钟。
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
            if (zone.equals(baseClock.getZone())) {  // 故意引发空指针异常
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
