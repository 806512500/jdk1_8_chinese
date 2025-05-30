/*
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
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

/**
 * {@code TimeUnit} 表示给定粒度的时间段，并提供将时间单位相互转换的实用方法，以及在这些单位中执行定时和延迟操作的方法。{@code TimeUnit} 不维护时间信息，而只是帮助组织和使用可能在各种上下文中单独维护的时间表示。纳秒定义为微秒的千分之一，微秒定义为毫秒的千分之一，毫秒定义为秒的千分之一，分钟定义为六十秒，小时定义为六十分钟，天定义为二十四小时。
 *
 * <p>{@code TimeUnit} 主要用于告知基于时间的方法如何解释给定的定时参数。例如，以下代码将在 50 毫秒内超时，如果 {@link java.util.concurrent.locks.Lock 锁} 不可用：
 *
 *  <pre> {@code
 * Lock lock = ...;
 * if (lock.tryLock(50L, TimeUnit.MILLISECONDS)) ...}</pre>
 *
 * 而这段代码将在 50 秒内超时：
 *  <pre> {@code
 * Lock lock = ...;
 * if (lock.tryLock(50L, TimeUnit.SECONDS)) ...}</pre>
 *
 * 但是请注意，没有保证特定的超时实现能够以与给定 {@code TimeUnit} 相同的粒度注意到时间的流逝。
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
        public long toMicros(long d)  { return d / (C1 / C0); }
        public long toMillis(long d)  { return d / (C2 / C0); }
        public long toSeconds(long d) { return d / (C3 / C0); }
        public long toMinutes(long d) { return d / (C4 / C0); }
        public long toHours(long d)   { return d / (C5 / C0); }
        public long toDays(long d)    { return d / (C6 / C0); }
        public long convert(long d, TimeUnit u) { return u.toNanos(d); }
        int excessNanos(long d, long m) { return (int)(d - (m * C2)); }
    },

    /**
     * 表示毫秒的千分之一的时间单位
     */
    MICROSECONDS {
        public long toNanos(long d)   { return x(d, C1 / C0, MAX / (C1 / C0)); }
        public long toMicros(long d)  { return d; }
        public long toMillis(long d)  { return d / (C2 / C1); }
        public long toSeconds(long d) { return d / (C3 / C1); }
        public long toMinutes(long d) { return d / (C4 / C1); }
        public long toHours(long d)   { return d / (C5 / C1); }
        public long toDays(long d)    { return d / (C6 / C1); }
        public long convert(long d, TimeUnit u) { return u.toMicros(d); }
        int excessNanos(long d, long m) { return (int)((d * C1) - (m * C2)); }
    },

    /**
     * 表示秒的千分之一的时间单位
     */
    MILLISECONDS {
        public long toNanos(long d)   { return x(d, C2 / C0, MAX / (C2 / C0)); }
        public long toMicros(long d)  { return x(d, C2 / C1, MAX / (C2 / C1)); }
        public long toMillis(long d)  { return d; }
        public long toSeconds(long d) { return d / (C3 / C2); }
        public long toMinutes(long d) { return d / (C4 / C2); }
        public long toHours(long d)   { return d / (C5 / C2); }
        public long toDays(long d)    { return d / (C6 / C2); }
        public long convert(long d, TimeUnit u) { return u.toMillis(d); }
        int excessNanos(long d, long m) { return 0; }
    },

    /**
     * 表示一秒的时间单位
     */
    SECONDS {
        public long toNanos(long d)   { return x(d, C3 / C0, MAX / (C3 / C0)); }
        public long toMicros(long d)  { return x(d, C3 / C1, MAX / (C3 / C1)); }
        public long toMillis(long d)  { return x(d, C3 / C2, MAX / (C3 / C2)); }
        public long toSeconds(long d) { return d; }
        public long toMinutes(long d) { return d / (C4 / C3); }
        public long toHours(long d)   { return d / (C5 / C3); }
        public long toDays(long d)    { return d / (C6 / C3); }
        public long convert(long d, TimeUnit u) { return u.toSeconds(d); }
        int excessNanos(long d, long m) { return 0; }
    },

    /**
     * 表示六十秒的时间单位
     */
    MINUTES {
        public long toNanos(long d)   { return x(d, C4 / C0, MAX / (C4 / C0)); }
        public long toMicros(long d)  { return x(d, C4 / C1, MAX / (C4 / C1)); }
        public long toMillis(long d)  { return x(d, C4 / C2, MAX / (C4 / C2)); }
        public long toSeconds(long d) { return x(d, C4 / C3, MAX / (C4 / C3)); }
        public long toMinutes(long d) { return d; }
        public long toHours(long d)   { return d / (C5 / C4); }
        public long toDays(long d)    { return d / (C6 / C4); }
        public long convert(long d, TimeUnit u) { return u.toMinutes(d); }
        int excessNanos(long d, long m) { return 0; }
    },

    /**
     * 表示六十分钟的时间单位
     */
    HOURS {
        public long toNanos(long d)   { return x(d, C5 / C0, MAX / (C5 / C0)); }
        public long toMicros(long d)  { return x(d, C5 / C1, MAX / (C5 / C1)); }
        public long toMillis(long d)  { return x(d, C5 / C2, MAX / (C5 / C2)); }
        public long toSeconds(long d) { return x(d, C5 / C3, MAX / (C5 / C3)); }
        public long toMinutes(long d) { return x(d, C5 / C4, MAX / (C5 / C4)); }
        public long toHours(long d)   { return d; }
        public long toDays(long d)    { return d / (C6 / C5); }
        public long convert(long d, TimeUnit u) { return u.toHours(d); }
        int excessNanos(long d, long m) { return 0; }
    },

    /**
     * 表示二十四小时的时间单位
     */
    DAYS {
        public long toNanos(long d)   { return x(d, C6 / C0, MAX / (C6 / C0)); }
        public long toMicros(long d)  { return x(d, C6 / C1, MAX / (C6 / C1)); }
        public long toMillis(long d)  { return x(d, C6 / C2, MAX / (C6 / C2)); }
        public long toSeconds(long d) { return x(d, C6 / C3, MAX / (C6 / C3)); }
        public long toMinutes(long d) { return x(d, C6 / C4, MAX / (C6 / C4)); }
        public long toHours(long d)   { return x(d, C6 / C5, MAX / (C6 / C5)); }
        public long toDays(long d)    { return d; }
        public long convert(long d, TimeUnit u) { return u.toDays(d); }
        int excessNanos(long d, long m) { return 0; }
    };

    // 方便转换方法使用的常量
    static final long C0 = 1L;
    static final long C1 = C0 * 1000L;
    static final long C2 = C1 * 1000L;
    static final long C3 = C2 * 1000L;
    static final long C4 = C3 * 60L;
    static final long C5 = C4 * 60L;
    static final long C6 = C5 * 24L;

    static final long MAX = Long.MAX_VALUE;

    /**
     * 将 d 乘以 m，并检查溢出。
     * 这个方法的名称较短，以使上面的代码更易读。
     */
    static long x(long d, long m, long over) {
        if (d > over) return Long.MAX_VALUE;
        if (d < -over) return Long.MIN_VALUE;
        return d * m;
    }

    // 为了保持与 1.5 的完全签名兼容性，并提高生成的 javadoc 的清晰度（参见 6287639: Abstract methods in
    // enum classes should not be listed as abstract），方法 convert
    // 等不是声明为抽象的，但其他方面作为抽象方法使用。

    /**
     * 将给定单位的时间段转换为本单位的时间段。
     * 从更细粒度到更粗粒度的转换会截断，因此会丢失精度。例如，将 {@code 999} 毫秒转换为秒会得到 {@code 0}。从更粗粒度到
     * 更细粒度的转换，如果数值上会溢出，则会饱和到 {@code Long.MIN_VALUE}（如果为负数）或
     * {@code Long.MAX_VALUE}（如果为正数）。
     *
     * <p>例如，将 10 分钟转换为毫秒，使用：
     * {@code TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES)}
     *
     * @param sourceDuration 给定 {@code sourceUnit} 的时间段
     * @param sourceUnit {@code sourceDuration} 参数的单位
     * @return 转换后的时间段，如果转换会负溢出则返回 {@code Long.MIN_VALUE}，如果会正溢出则返回 {@code Long.MAX_VALUE}。
     */
    public long convert(long sourceDuration, TimeUnit sourceUnit) {
        throw new AbstractMethodError();
    }

    /**
     * 等效于
     * {@link #convert(long, TimeUnit) NANOSECONDS.convert(duration, this)}。
     * @param duration 时间段
     * @return 转换后的时间段，如果转换会负溢出则返回 {@code Long.MIN_VALUE}，如果会正溢出则返回 {@code Long.MAX_VALUE}。
     */
    public long toNanos(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 等效于
     * {@link #convert(long, TimeUnit) MICROSECONDS.convert(duration, this)}。
     * @param duration 时间段
     * @return 转换后的时间段，如果转换会负溢出则返回 {@code Long.MIN_VALUE}，如果会正溢出则返回 {@code Long.MAX_VALUE}。
     */
    public long toMicros(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 等效于
     * {@link #convert(long, TimeUnit) MILLISECONDS.convert(duration, this)}。
     * @param duration 时间段
     * @return 转换后的时间段，如果转换会负溢出则返回 {@code Long.MIN_VALUE}，如果会正溢出则返回 {@code Long.MAX_VALUE}。
     */
    public long toMillis(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 等效于
     * {@link #convert(long, TimeUnit) SECONDS.convert(duration, this)}。
     * @param duration 时间段
     * @return 转换后的时间段，如果转换会负溢出则返回 {@code Long.MIN_VALUE}，如果会正溢出则返回 {@code Long.MAX_VALUE}。
     */
    public long toSeconds(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 等效于
     * {@link #convert(long, TimeUnit) MINUTES.convert(duration, this)}。
     * @param duration 时间段
     * @return 转换后的时间段，如果转换会负溢出则返回 {@code Long.MIN_VALUE}，如果会正溢出则返回 {@code Long.MAX_VALUE}。
     * @since 1.6
     */
    public long toMinutes(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 等效于
     * {@link #convert(long, TimeUnit) HOURS.convert(duration, this)}。
     * @param duration 时间段
     * @return 转换后的时间段，如果转换会负溢出则返回 {@code Long.MIN_VALUE}，如果会正溢出则返回 {@code Long.MAX_VALUE}。
     * @since 1.6
     */
    public long toHours(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 等效于
     * {@link #convert(long, TimeUnit) DAYS.convert(duration, this)}。
     * @param duration 时间段
     * @return 转换后的时间段
     * @since 1.6
     */
    public long toDays(long duration) {
        throw new AbstractMethodError();
    }

    /**
     * 计算 wait、sleep、join 的 excess-nanosecond 参数。
     * @param d 时间段
     * @param m 毫秒数
     * @return 纳秒数
     */
    abstract int excessNanos(long d, long m);

    /**
     * 使用本时间单位执行一个定时的 {@link Object#wait(long, int) Object.wait}。
     * 这是一个方便的方法，将超时参数转换为 {@code Object.wait} 方法所需的形式。
     *
     * <p>例如，你可以实现一个阻塞的 {@code poll}
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
     * @throws InterruptedException 如果等待时被中断
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
     * 使用本时间单位执行一个定时的 {@link Thread#join(long, int) Thread.join}。
     * 这是一个方便的方法，将时间参数转换为 {@code Thread.join} 方法所需的形式。
     *
     * @param thread 要等待的线程
     * @param timeout 最大等待时间。如果小于或等于零，则不等待。
     * @throws InterruptedException 如果等待时被中断
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
     * 使用本时间单位执行一个 {@link Thread#sleep(long, int) Thread.sleep}。
     * 这是一个方便的方法，将时间参数转换为 {@code Thread.sleep} 方法所需的形式。
     *
     * @param timeout 最小睡眠时间。如果小于或等于零，则不睡眠。
     * @throws InterruptedException 如果睡眠时被中断
     */
    public void sleep(long timeout) throws InterruptedException {
        if (timeout > 0) {
            long ms = toMillis(timeout);
            int ns = excessNanos(timeout, ms);
            Thread.sleep(ms, ns);
        }
    }

}
