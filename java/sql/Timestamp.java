
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

package java.sql;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.StringTokenizer;

/**
 * <P>一个轻量级的包装器，围绕 <code>java.util.Date</code>，使 JDBC API 能够识别这是一个 SQL <code>TIMESTAMP</code> 值。
 * 它增加了持有 SQL <code>TIMESTAMP</code> 小数秒值的能力，允许指定精确到纳秒的小数秒。
 * Timestamp 还提供了格式化和解析操作，以支持 JDBC 用于时间戳值的转义语法。
 *
 * <p>Timestamp 对象的精度计算为以下两种情况之一：
 * <ul>
 * <li><code>19 </code>，这是 yyyy-mm-dd hh:mm:ss 中的字符数
 * <li> <code> 20 + s </code>，这是 yyyy-mm-dd hh:mm:ss.[fff...] 中的字符数，<code>s</code> 表示给定 Timestamp 的精度，
 * 即其小数秒的精度。
 *</ul>
 *
 * <P><B>注意：</B>此类型是 <code>java.util.Date</code> 和单独的纳秒值的组合。只有整秒存储在 <code>java.util.Date</code> 组件中。
 * 小数秒 - 纳秒 - 是分开的。 <code>Timestamp.equals(Object)</code> 方法在传递一个不是 <code>java.sql.Timestamp</code> 实例的对象时
 * 从不返回 <code>true</code>，因为日期的纳秒部分是未知的。
 * 因此，<code>Timestamp.equals(Object)</code> 方法相对于 <code>java.util.Date.equals(Object)</code> 方法是不对称的。
 * 此外，<code>hashCode</code> 方法使用底层的 <code>java.util.Date</code> 实现，因此在其计算中不包括纳秒。
 * <P>
 * 由于上述 <code>Timestamp</code> 类和 <code>java.util.Date</code> 类之间的差异，建议代码不要将 <code>Timestamp</code> 值
 * 通用地视为 <code>java.util.Date</code> 的实例。 <code>Timestamp</code> 和 <code>java.util.Date</code> 之间的继承关系
 * 实际上表示实现继承，而不是类型继承。
 */
public class Timestamp extends java.util.Date {

    /**
     * 使用给定的值构造一个 <code>Timestamp</code> 对象。
     *
     * @param year 年份减去 1900
     * @param month 0 到 11
     * @param date 1 到 31
     * @param hour 0 到 23
     * @param minute 0 到 59
     * @param second 0 到 59
     * @param nano 0 到 999,999,999
     * @deprecated 请改用构造函数 <code>Timestamp(long millis)</code>
     * @exception IllegalArgumentException 如果 nano 参数超出范围
     */
    @Deprecated
    public Timestamp(int year, int month, int date,
                     int hour, int minute, int second, int nano) {
        super(year, month, date, hour, minute, second);
        if (nano > 999999999 || nano < 0) {
            throw new IllegalArgumentException("nanos > 999999999 or < 0");
        }
        nanos = nano;
    }

    /**
     * 使用毫秒时间值构造一个 <code>Timestamp</code> 对象。整秒存储在底层的日期值中；
     * 小数秒存储在 <code>Timestamp</code> 对象的 <code>nanos</code> 字段中。
     *
     * @param time 自 1970 年 1 月 1 日 00:00:00 GMT 以来的毫秒数。
     *        负数表示 1970 年 1 月 1 日 00:00:00 GMT 之前的毫秒数。
     * @see java.util.Calendar
     */
    public Timestamp(long time) {
        super((time/1000)*1000);
        nanos = (int)((time%1000) * 1000000);
        if (nanos < 0) {
            nanos = 1000000000 + nanos;
            super.setTime(((time/1000)-1)*1000);
        }
    }

    /**
     * 将此 <code>Timestamp</code> 对象设置为表示自 1970 年 1 月 1 日 00:00:00 GMT 以来 <tt>time</tt> 毫秒的时间点。
     *
     * @param time   毫秒数。
     * @see #getTime
     * @see #Timestamp(long time)
     * @see java.util.Calendar
     */
    public void setTime(long time) {
        super.setTime((time/1000)*1000);
        nanos = (int)((time%1000) * 1000000);
        if (nanos < 0) {
            nanos = 1000000000 + nanos;
            super.setTime(((time/1000)-1)*1000);
        }
    }

    /**
     * 返回此 <code>Timestamp</code> 对象表示的自 1970 年 1 月 1 日 00:00:00 GMT 以来的毫秒数。
     *
     * @return  自 1970 年 1 月 1 日 00:00:00 GMT 以来的毫秒数。
     * @see #setTime
     */
    public long getTime() {
        long time = super.getTime();
        return (time + (nanos / 1000000));
    }


    /**
     * @serial
     */
    private int nanos;

    /**
     * 将 JDBC 时间戳转义格式的 <code>String</code> 对象转换为 <code>Timestamp</code> 值。
     *
     * @param s 格式为 <code>yyyy-[m]m-[d]d hh:mm:ss[.f...]</code> 的时间戳。小数秒可以省略。
     * 年、月和日的前导零也可以省略。
     *
     * @return 对应的 <code>Timestamp</code> 值
     * @exception java.lang.IllegalArgumentException 如果给定的参数不符合 <code>yyyy-[m]m-[d]d hh:mm:ss[.f...]</code> 格式
     */
    public static Timestamp valueOf(String s) {
        final int YEAR_LENGTH = 4;
        final int MONTH_LENGTH = 2;
        final int DAY_LENGTH = 2;
        final int MAX_MONTH = 12;
        final int MAX_DAY = 31;
        String date_s;
        String time_s;
        String nanos_s;
        int year = 0;
        int month = 0;
        int day = 0;
        int hour;
        int minute;
        int second;
        int a_nanos = 0;
        int firstDash;
        int secondDash;
        int dividingSpace;
        int firstColon = 0;
        int secondColon = 0;
        int period = 0;
        String formatError = "Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]";
        String zeros = "000000000";
        String delimiterDate = "-";
        String delimiterTime = ":";


                    if (s == null) throw new java.lang.IllegalArgumentException("null string");

        // 将字符串分割为日期和时间部分
        s = s.trim();
        dividingSpace = s.indexOf(' ');
        if (dividingSpace > 0) {
            date_s = s.substring(0,dividingSpace);
            time_s = s.substring(dividingSpace+1);
        } else {
            throw new java.lang.IllegalArgumentException(formatError);
        }

        // 解析日期
        firstDash = date_s.indexOf('-');
        secondDash = date_s.indexOf('-', firstDash+1);

        // 解析时间
        if (time_s == null)
            throw new java.lang.IllegalArgumentException(formatError);
        firstColon = time_s.indexOf(':');
        secondColon = time_s.indexOf(':', firstColon+1);
        period = time_s.indexOf('.', secondColon+1);

        // 转换日期
        boolean parsedDate = false;
        if ((firstDash > 0) && (secondDash > 0) && (secondDash < date_s.length() - 1)) {
            String yyyy = date_s.substring(0, firstDash);
            String mm = date_s.substring(firstDash + 1, secondDash);
            String dd = date_s.substring(secondDash + 1);
            if (yyyy.length() == YEAR_LENGTH &&
                    (mm.length() >= 1 && mm.length() <= MONTH_LENGTH) &&
                    (dd.length() >= 1 && dd.length() <= DAY_LENGTH)) {
                 year = Integer.parseInt(yyyy);
                 month = Integer.parseInt(mm);
                 day = Integer.parseInt(dd);

                if ((month >= 1 && month <= MAX_MONTH) && (day >= 1 && day <= MAX_DAY)) {
                    parsedDate = true;
                }
            }
        }
        if (! parsedDate) {
            throw new java.lang.IllegalArgumentException(formatError);
        }

        // 转换时间；默认缺失的纳秒
        if ((firstColon > 0) & (secondColon > 0) &
            (secondColon < time_s.length()-1)) {
            hour = Integer.parseInt(time_s.substring(0, firstColon));
            minute =
                Integer.parseInt(time_s.substring(firstColon+1, secondColon));
            if ((period > 0) & (period < time_s.length()-1)) {
                second =
                    Integer.parseInt(time_s.substring(secondColon+1, period));
                nanos_s = time_s.substring(period+1);
                if (nanos_s.length() > 9)
                    throw new java.lang.IllegalArgumentException(formatError);
                if (!Character.isDigit(nanos_s.charAt(0)))
                    throw new java.lang.IllegalArgumentException(formatError);
                nanos_s = nanos_s + zeros.substring(0,9-nanos_s.length());
                a_nanos = Integer.parseInt(nanos_s);
            } else if (period > 0) {
                throw new java.lang.IllegalArgumentException(formatError);
            } else {
                second = Integer.parseInt(time_s.substring(secondColon+1));
            }
        } else {
            throw new java.lang.IllegalArgumentException(formatError);
        }

        return new Timestamp(year - 1900, month - 1, day, hour, minute, second, a_nanos);
    }

    /**
     * 以 JDBC 时间戳转义格式格式化时间戳。
     *         <code>yyyy-mm-dd hh:mm:ss.fffffffff</code>，
     * 其中 <code>ffffffffff</code> 表示纳秒。
     * <P>
     * @return 一个 <code>String</code> 对象，格式为
     *           <code>yyyy-mm-dd hh:mm:ss.fffffffff</code>
     */
    @SuppressWarnings("deprecation")
    public String toString () {

        int year = super.getYear() + 1900;
        int month = super.getMonth() + 1;
        int day = super.getDate();
        int hour = super.getHours();
        int minute = super.getMinutes();
        int second = super.getSeconds();
        String yearString;
        String monthString;
        String dayString;
        String hourString;
        String minuteString;
        String secondString;
        String nanosString;
        String zeros = "000000000";
        String yearZeros = "0000";
        StringBuffer timestampBuf;

        if (year < 1000) {
            // 添加前导零
            yearString = "" + year;
            yearString = yearZeros.substring(0, (4-yearString.length())) +
                yearString;
        } else {
            yearString = "" + year;
        }
        if (month < 10) {
            monthString = "0" + month;
        } else {
            monthString = Integer.toString(month);
        }
        if (day < 10) {
            dayString = "0" + day;
        } else {
            dayString = Integer.toString(day);
        }
        if (hour < 10) {
            hourString = "0" + hour;
        } else {
            hourString = Integer.toString(hour);
        }
        if (minute < 10) {
            minuteString = "0" + minute;
        } else {
            minuteString = Integer.toString(minute);
        }
        if (second < 10) {
            secondString = "0" + second;
        } else {
            secondString = Integer.toString(second);
        }
        if (nanos == 0) {
            nanosString = "0";
        } else {
            nanosString = Integer.toString(nanos);

            // 添加前导零
            nanosString = zeros.substring(0, (9-nanosString.length())) +
                nanosString;

            // 截断尾部零
            char[] nanosChar = new char[nanosString.length()];
            nanosString.getChars(0, nanosString.length(), nanosChar, 0);
            int truncIndex = 8;
            while (nanosChar[truncIndex] == '0') {
                truncIndex--;
            }

            nanosString = new String(nanosChar, 0, truncIndex + 1);
        }

        // 在这里使用字符串缓冲区
        timestampBuf = new StringBuffer(20+nanosString.length());
        timestampBuf.append(yearString);
        timestampBuf.append("-");
        timestampBuf.append(monthString);
        timestampBuf.append("-");
        timestampBuf.append(dayString);
        timestampBuf.append(" ");
        timestampBuf.append(hourString);
        timestampBuf.append(":");
        timestampBuf.append(minuteString);
        timestampBuf.append(":");
        timestampBuf.append(secondString);
        timestampBuf.append(".");
        timestampBuf.append(nanosString);


                    return (timestampBuf.toString());
    }

    /**
     * 获取此 <code>Timestamp</code> 对象的 <code>nanos</code> 值。
     *
     * @return 此 <code>Timestamp</code> 对象的小数秒部分
     * @see #setNanos
     */
    public int getNanos() {
        return nanos;
    }

    /**
     * 将此 <code>Timestamp</code> 对象的 <code>nanos</code> 字段设置为给定值。
     *
     * @param n 新的小数秒部分
     * @exception java.lang.IllegalArgumentException 如果给定参数大于 999999999 或小于 0
     * @see #getNanos
     */
    public void setNanos(int n) {
        if (n > 999999999 || n < 0) {
            throw new IllegalArgumentException("nanos > 999999999 or < 0");
        }
        nanos = n;
    }

    /**
     * 测试此 <code>Timestamp</code> 对象是否等于给定的 <code>Timestamp</code> 对象。
     *
     * @param ts 要比较的 <code>Timestamp</code> 值
     * @return 如果给定的 <code>Timestamp</code> 对象等于此 <code>Timestamp</code> 对象，则返回 <code>true</code>；
     *         否则返回 <code>false</code>
     */
    public boolean equals(Timestamp ts) {
        if (super.equals(ts)) {
            if  (nanos == ts.nanos) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 测试此 <code>Timestamp</code> 对象是否等于给定的对象。
     *
     * 此方法 <code>equals</code> 的版本已添加，以修复 <code>Timestamp.equals(Timestamp)</code> 的不正确签名并保持与现有类文件的向后兼容性。
     *
     * 注意：此方法与基类中的 <code>equals(Object)</code> 方法不对称。
     *
     * @param ts 要比较的 <code>Object</code> 值
     * @return 如果给定的 <code>Object</code> 是一个与这个 <code>Timestamp</code> 对象相等的 <code>Timestamp</code> 实例，则返回 <code>true</code>；
     *         否则返回 <code>false</code>
     */
    public boolean equals(java.lang.Object ts) {
      if (ts instanceof Timestamp) {
        return this.equals((Timestamp)ts);
      } else {
        return false;
      }
    }

    /**
     * 指示此 <code>Timestamp</code> 对象是否早于给定的 <code>Timestamp</code> 对象。
     *
     * @param ts 要比较的 <code>Timestamp</code> 值
     * @return 如果此 <code>Timestamp</code> 对象早于给定的 <code>Timestamp</code> 对象，则返回 <code>true</code>；
     *        否则返回 <code>false</code>
     */
    public boolean before(Timestamp ts) {
        return compareTo(ts) < 0;
    }

    /**
     * 指示此 <code>Timestamp</code> 对象是否晚于给定的 <code>Timestamp</code> 对象。
     *
     * @param ts 要比较的 <code>Timestamp</code> 值
     * @return 如果此 <code>Timestamp</code> 对象晚于给定的 <code>Timestamp</code> 对象，则返回 <code>true</code>；
     *        否则返回 <code>false</code>
     */
    public boolean after(Timestamp ts) {
        return compareTo(ts) > 0;
    }

    /**
     * 比较此 <code>Timestamp</code> 对象与给定的 <code>Timestamp</code> 对象。
     *
     * @param   ts   要与此 <code>Timestamp</code> 对象比较的 <code>Timestamp</code> 对象
     * @return  如果两个 <code>Timestamp</code> 对象相等，则返回值 <code>0</code>；如果此 <code>Timestamp</code> 对象早于给定的参数，则返回小于 <code>0</code> 的值；
     *          如果此 <code>Timestamp</code> 对象晚于给定的参数，则返回大于 <code>0</code> 的值。
     * @since   1.4
     */
    public int compareTo(Timestamp ts) {
        long thisTime = this.getTime();
        long anotherTime = ts.getTime();
        int i = (thisTime<anotherTime ? -1 :(thisTime==anotherTime?0 :1));
        if (i == 0) {
            if (nanos > ts.nanos) {
                    return 1;
            } else if (nanos < ts.nanos) {
                return -1;
            }
        }
        return i;
    }

    /**
     * 比较此 <code>Timestamp</code> 对象与给定的 <code>Date</code> 对象。
     *
     * @param o 要与此 <code>Timestamp</code> 对象比较的 <code>Date</code>
     * @return  如果此 <code>Timestamp</code> 对象与给定的对象相等，则返回值 <code>0</code>；如果此 <code>Timestamp</code> 对象早于给定的参数，则返回小于 <code>0</code> 的值；
     *          如果此 <code>Timestamp</code> 对象晚于给定的参数，则返回大于 <code>0</code> 的值。
     *
     * @since   1.5
     */
    public int compareTo(java.util.Date o) {
       if(o instanceof Timestamp) {
            // 当 Timestamp 实例比较时，它与一个 Timestamp 对象比较
            // 因此基本上是在调用 this.compareTo((Timestamp))o);
            // 注意类型转换是安全的，因为 o 是 Timestamp 的实例
           return compareTo((Timestamp)o);
      } else {
            // 当 Date 对象进行 o.compareTo(this) 比较时
            // 将给出错误的结果。
          Timestamp ts = new Timestamp(o.getTime());
          return this.compareTo(ts);
      }
    }

    /**
     * {@inheritDoc}
     *
     * 该 {@code hashCode} 方法使用底层的 {@code java.util.Date} 实现，因此在计算中不包括 nanos。
     *
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    static final long serialVersionUID = 2745179027874758501L;

    private static final int MILLIS_PER_SECOND = 1000;

    /**
     * 从一个 {@code LocalDateTime} 对象中获取一个 {@code Timestamp} 实例，该实例具有与提供的 {@code LocalDateTime} 相同的年、月、日、小时、分钟、秒和纳秒日期时间值。
     * <p>
     * 提供的 {@code LocalDateTime} 被解释为本地时区的本地日期时间。
     *
     * @param dateTime 要转换的 {@code LocalDateTime}
     * @return 一个 {@code Timestamp} 对象
     * @exception NullPointerException 如果 {@code dateTime} 为 null。
     * @since 1.8
     */
    @SuppressWarnings("deprecation")
    public static Timestamp valueOf(LocalDateTime dateTime) {
        return new Timestamp(dateTime.getYear() - 1900,
                             dateTime.getMonthValue() - 1,
                             dateTime.getDayOfMonth(),
                             dateTime.getHour(),
                             dateTime.getMinute(),
                             dateTime.getSecond(),
                             dateTime.getNano());
    }

                /**
     * 将此 {@code Timestamp} 对象转换为 {@code LocalDateTime}。
     * <p>
     * 转换创建一个 {@code LocalDateTime}，表示与本地时区中的此 {@code Timestamp} 相同的年、月、日、小时、分钟、秒和纳秒日期时间值。
     *
     * @return 表示相同日期时间值的 {@code LocalDateTime} 对象
     * @since 1.8
     */
    @SuppressWarnings("deprecation")
    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.of(getYear() + 1900,
                                getMonth() + 1,
                                getDate(),
                                getHours(),
                                getMinutes(),
                                getSeconds(),
                                getNanos());
    }

    /**
     * 从 {@link Instant} 对象获取 {@code Timestamp} 的实例。
     * <p>
     * {@code Instant} 可以存储比 {@code Date} 更远的未来和更远的过去的时间线上的点。在这种情况下，此方法将抛出异常。
     *
     * @param instant  要转换的瞬间
     * @return 一个表示与提供的瞬间相同时间线点的 {@code Timestamp}
     * @exception NullPointerException 如果 {@code instant} 为 null。
     * @exception IllegalArgumentException 如果瞬间太大，无法表示为 {@code Timestamp}
     * @since 1.8
     */
    public static Timestamp from(Instant instant) {
        try {
            Timestamp stamp = new Timestamp(instant.getEpochSecond() * MILLIS_PER_SECOND);
            stamp.nanos = instant.getNano();
            return stamp;
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * 将此 {@code Timestamp} 对象转换为 {@code Instant}。
     * <p>
     * 转换创建一个表示与此 {@code Timestamp} 相同时间线点的 {@code Instant}。
     *
     * @return 表示相同时间线点的瞬间
     * @since 1.8
     */
    @Override
    public Instant toInstant() {
        return Instant.ofEpochSecond(super.getTime() / MILLIS_PER_SECOND, nanos);
    }
}
