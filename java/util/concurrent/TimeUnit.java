/*
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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并如 http://creativecommons.org/publicdomain/zero/1.0/ 所解释的那样发布到公共领域。
 */

package java.util.concurrent;

/**
 * {@code TimeUnit} 表示给定粒度的时间持续时间，并提供用于在不同单位之间转换的方法，以及在这些单位中执行定时和延迟操作的方法。{@code TimeUnit} 不维护时间信息，而只是帮助组织和使用可能在各种上下文中单独维护的时间表示。纳秒定义为微秒的千分之一，微秒定义为毫秒的千分之一，毫秒定义为秒的千分之一，分钟定义为六十秒，小时定义为六十分钟，天定义为二十四小时。
 *
 * <p>{@code TimeUnit} 主要用于告知基于时间的方法如何解释给定的定时参数。例如，以下代码如果无法获取 {@link
 * java.util.concurrent.locks.Lock 锁}，将在 50 毫秒后超时：
 *
 *  <pre> {@code
 * Lock lock = ...;
 * if (lock.tryLock(50L, TimeUnit.MILLISECONDS)) ...}</pre>
 *
 * 而这段代码将在 50 秒后超时：
 *  <pre> {@code
 * Lock lock = ...;
 * if (lock.tryLock(50L, TimeUnit.SECONDS)) ...}</pre>
 *
 * 然而，请注意，没有保证特定的超时实现能够以与给定的 {@code TimeUnit} 相同的粒度注意到时间的流逝。
 *
 * @since 1.5
 * @author Doug Lea
 */
public enum TimeUnit {
    /**
     * 表示微秒的千分之一的时间单位
     */
    NANOSECONDS {
        public long toNanos(long d)   { return d; }
        public long toMicros(long d)  { return d/(C1/C0); }
        public long toMillis(long d)  { return d/(C2/C0); }
        public long toSeconds(long d) { return d/(C3/C0); }
        public long toMinutes(long d) { return d/(C4/C0); }
        public long toHours(long d)   { return d/(C5/C0); }
        public long toDays(long d)    { return d/(C6/C0); }
        public long convert(long d, TimeUnit u) { return u.toNanos(d); }
        int excessNanos(long d, long m) { return (int)(d - (m*C2)); }
    },

    /**
     * 表示毫秒的千分之一的时间单位
     */
    MICROSECONDS {
        public long toNanos(long d)   { return x(d, C1/C0, MAX/(C1/C0)); }
        public long toMicros(long d)  { return d; }
        public long toMillis(long d)  { return d/(C2/C1); }
        public long toSeconds(long d) { return d/(C3/C1); }
        public long toMinutes(long d) { return d/(C4/C1); }
        public long toHours(long d)   { return d/(C5/C1); }
        public long toDays(long d)    { return d/(C6/C1); }
        public long convert(long d, TimeUnit u) { return u.toMicros(d); }
        int excessNanos(long d, long m) { return (int)((d*C1) - (m*C2)); }
    },

    /**
     * 表示秒的千分之一的时间单位
     */
    MILLISECONDS {
        public long toNanos(long d)   { return x(d, C2/C0, MAX/(C2/C0)); }
        public long toMicros(long d)  { return x(d, C2/C1, MAX/(C2/C1)); }
        public long toMillis(long d)  { return d; }
        public long toSeconds(long d) { return d/(C3/C2); }
        public long toMinutes(long d) { return d/(C4/C2); }
        public long toHours(long d)   { return d/(C5/C2); }
        public long toDays(long d)    { return d/(C6/C2); }
        public long convert(long d, TimeUnit u) { return u.toMillis(d); }
        int excessNanos(long d, long m) { return 0; }
    },

    /**
     * 表示一秒的时间单位
     */
    SECONDS {
        public long toNanos(long d)   { return x(d, C3/C0, MAX/(C3/C0)); }
        public long toMicros(long d)  { return x(d, C3/C1, MAX/(C3/C1)); }
        public long toMillis(long d)  { return x(d, C3/C2, MAX/(C3/C2)); }
        public long toSeconds(long d) { return d; }
        public long toMinutes(long d) { return d/(C4/C3); }
        public long toHours(long d)   { return d/(C5/C3); }
        public long toDays(long d)    { return d/(C6/C3); }
        public long convert(long d, TimeUnit u) { return u.toSeconds(d); }
        int excessNanos(long d, long m) { return 0; }
    },

    /**
     * 表示六十秒的时间单位
     */
    MINUTES {
        public long toNanos(long d)   { return x(d, C4/C0, MAX/(C4/C0)); }
        public long toMicros(long d)  { return x(d, C4/C1, MAX/(C4/C1)); }
        public long toMillis(long d)  { return x(d, C4/C2, MAX/(C4/C2)); }
        public long toSeconds(long d) { return x(d, C4/C3, MAX/(C4/C3)); }
        public long toMinutes(long d) { return d; }
        public long toHours(long d)   { return d/(C5/C4); }
        public long toDays(long d)    { return d/(C6/C4); }
        public long convert(long d, TimeUnit u) { return u.toMinutes(d); }
        int excessNanos(long d, long m) { return 0; }
    },

    /**
     * 表示六十分钟的时间单位
     */
    HOURS {
        public long toNanos(long d)   { return x(d, C5/C0, MAX/(C5/C0)); }
        public long toMicros(long d)  { return x(d, C5/C1, MAX/(C5/C1)); }
        public long toMillis(long d)  { return x(d, C5/C2, MAX/(C5/C2)); }
        public long toSeconds(long d) { return x(d, C5/C3, MAX/(C5/C3)); }
        public long toMinutes(long d) { return x(d, C5/C4, MAX/(C5/C4)); }
        public long toHours(long d)   { return d; }
        public long toDays(long d)    { return d/(C6/C5); }
        public long convert(long d, TimeUnit u) { return u.toHours(d); }
        int excessNanos(long d, long m) { return 0; }
    },

    /**
     * 表示二十四小时的时间单位
     */
    DAYS {
        public long toNanos(long d)   { return x(d, C6/C0, MAX/(C6/C0)); }
        public long toMicros(long d)  { return x(d, C6/C1, MAX/(C6/C1)); }
        public long toMillis(long d)  { return x(d, C6/C2, MAX/(C6/C2)); }
        public long toSeconds(long d) { return x(d, C6/C3, MAX/(C6/C3)); }
        public long toMinutes(long d) { return x(d, C6/C4, MAX/(C6/C4)); }
        public long toHours(long d)   { return x(d, C6/C5, MAX/(C6/C5)); }
        public long toDays(long d)    { return d; }
        public long convert(long d, TimeUnit u) { return u.toDays(d); }
        int excessNanos(long d, long m) { return 0; }
    };

    // 用于转换方法的便捷常量
    static final long C0 = 1L;
    static final long C1 = C0 * 1000L;
    static final long C2 = C1 * 1000L;
    static final long C3 = C2 * 1000L;
    static final long C4 = C3 * 60L;
    static final long C5 = C4 * 60L;
    static final long C6 = C5 * 24L;

    static final long MAX = Long.MAX_VALUE;

    /**
     * 按照 m 缩放 d，并检查溢出。
     * 该方法名称较短，以使上述代码更易读。
     */
    static long x(long d, long m, long over) {
        if (d >  over) return Long.MAX_VALUE;
        if (d < -over) return Long.MIN_VALUE;
        return d * m;
    }

    // 为了保持与 1.5 的完整签名兼容性，并提高生成的 javadoc 的清晰度（参见 6287639: 枚举类中的抽象方法不应列为抽象），方法 convert
    // 等不是声明为抽象的，但其他方面作为抽象方法运行。

    /**
     * 将给定单位的时间持续时间转换为该单位。
     * 从更细粒度到更粗粒度的转换会截断，因此会丢失精度。例如，将 {@code 999} 毫秒转换为秒会得到 {@code 0}。从更粗粒度到
     * 更细粒度的转换，如果数值上溢出，则会饱和到 {@code Long.MIN_VALUE}（如果为负数）或
     * {@code Long.MAX_VALUE}（如果为正数）。
     *
     * <p>例如，要将 10 分钟转换为毫秒，使用：
     * {@code TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES)}
     *
     * @param sourceDuration 给定 {@code sourceUnit} 的时间持续时间
     * @param sourceUnit {@code sourceDuration} 参数的单位
     * @return 转换后的时间持续时间，如果转换会导致负溢出，则返回 {@code Long.MIN_VALUE}，如果会导致正溢出，则返回 {@code Long.MAX_VALUE}。
     */
    public long convert(long sourceDuration, TimeUnit sourceUnit) {
        throw new AbstractMethodError();
    }

    /**
     * 等同于
     * {@link #convert(long, TimeUnit) NANOSECONDS.convert(duration, this)}。
     * @param duration 时间持续时间
     * @return 转换后的时间持续时间，如果转换会导致负溢出，则返回 {@code Long.MIN_VALUE}，如果会导致正溢出，则返回 {@code Long.MAX_VALUE}。
     */
    public long toNanos(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 等同于
     * {@link #convert(long, TimeUnit) MICROSECONDS.convert(duration, this)}。
     * @param duration 时间持续时间
     * @return 转换后的时间持续时间，如果转换会导致负溢出，则返回 {@code Long.MIN_VALUE}，如果会导致正溢出，则返回 {@code Long.MAX_VALUE}。
     */
    public long toMicros(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 等同于
     * {@link #convert(long, TimeUnit) MILLISECONDS.convert(duration, this)}。
     * @param duration 时间持续时间
     * @return 转换后的时间持续时间，如果转换会导致负溢出，则返回 {@code Long.MIN_VALUE}，如果会导致正溢出，则返回 {@code Long.MAX_VALUE}。
     */
    public long toMillis(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 等同于
     * {@link #convert(long, TimeUnit) SECONDS.convert(duration, this)}。
     * @param duration 时间持续时间
     * @return 转换后的时间持续时间，如果转换会导致负溢出，则返回 {@code Long.MIN_VALUE}，如果会导致正溢出，则返回 {@code Long.MAX_VALUE}。
     */
    public long toSeconds(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 等同于
     * {@link #convert(long, TimeUnit) MINUTES.convert(duration, this)}。
     * @param duration 时间持续时间
     * @return 转换后的时间持续时间，如果转换会导致负溢出，则返回 {@code Long.MIN_VALUE}，如果会导致正溢出，则返回 {@code Long.MAX_VALUE}。
     * @since 1.6
     */
    public long toMinutes(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 等同于
     * {@link #convert(long, TimeUnit) HOURS.convert(duration, this)}。
     * @param duration 时间持续时间
     * @return 转换后的时间持续时间，如果转换会导致负溢出，则返回 {@code Long.MIN_VALUE}，如果会导致正溢出，则返回 {@code Long.MAX_VALUE}。
     * @since 1.6
     */
    public long toHours(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 等同于
     * {@link #convert(long, TimeUnit) DAYS.convert(duration, this)}。
     * @param duration 时间持续时间
     * @return 转换后的时间持续时间
     * @since 1.6
     */
    public long toDays(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 计算 wait, sleep, join 的多余纳秒参数。
     * @param d 时间持续时间
     * @param m 毫秒数
     * @return 纳秒数
     */
    abstract int excessNanos(long d, long m);

    /**
     * 使用此时间单位执行带超时的 {@link Object#wait(long, int) Object.wait}。
     * 这是一个方便的方法，将超时参数转换为 {@code Object.wait} 方法所需的形式。
     *
     * <p>例如，您可以使用以下代码实现一个阻塞的 {@code poll}
     * 方法（参见 {@link BlockingQueue#poll BlockingQueue.poll}）：
     *
     *  <pre> {@code
     * public synchronized Object poll(long timeout, TimeUnit unit)
     *     throws InterruptedException {
     *   while (empty) {
     *     unit.timedWait(this, timeout);
     *     ...
     *   }
     * }}</pre>
     *
     * @param obj 要等待的对象
     * @param timeout 最大等待时间。如果小于或等于零，则不等待。
     * @throws InterruptedException 如果在等待过程中被中断
     */
    public void timedWait(Object obj, long timeout)
            throws InterruptedException {
        if (timeout > 0) {
            long ms = toMillis(timeout);
            int ns = excessNanos(timeout, ms);
            obj.wait(ms, ns);
        }
    }


    /**
     * 使用此时间单位执行带超时的 {@link Thread#join(long, int) Thread.join}。
     * 这是一个方便的方法，将时间参数转换为 {@code Thread.join} 方法所需的格式。
     *
     * @param thread 要等待的线程
     * @param timeout 最大等待时间。如果小于或等于零，则不等待。
     * @throws InterruptedException 如果在等待过程中被中断
     */
    public void timedJoin(Thread thread, long timeout)
            throws InterruptedException {
        if (timeout > 0) {
            long ms = toMillis(timeout);
            int ns = excessNanos(timeout, ms);
            thread.join(ms, ns);
        }
    }

    /**
     * 使用此时间单位执行 {@link Thread#sleep(long, int) Thread.sleep}。
     * 这是一个方便的方法，将时间参数转换为 {@code Thread.sleep} 方法所需的格式。
     *
     * @param timeout 最小睡眠时间。如果小于或等于零，则不睡眠。
     * @throws InterruptedException 如果在睡眠过程中被中断
     */
    public void sleep(long timeout) throws InterruptedException {
        if (timeout > 0) {
            long ms = toMillis(timeout);
            int ns = excessNanos(timeout, ms);
            Thread.sleep(ms, ns);
        }
    }

}
