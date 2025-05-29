
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
import java.time.LocalTime;

/**
 * <P>一个围绕<code>java.util.Date</code>类的轻量级包装器，使JDBC
 * API能够识别这是一个SQL <code>TIME</code>值。<code>Time</code>
 * 类添加了格式化和
 * 解析操作以支持JDBC时间值的转义语法。
 * <p>日期组件应设置为“零纪元”
 * 值1970年1月1日，并且不应访问。
 */
public class Time extends java.util.Date {

    /**
     * 使用给定的小时、分钟和秒值构造一个<code>Time</code>对象。
     * 驱动程序将日期组件设置为1970年1月1日。
     * 任何尝试访问<code>Time</code>对象日期组件的方法都将抛出
     * <code>java.lang.IllegalArgumentException</code>。
     * <P>
     * 如果给定的参数超出范围，则结果是不确定的。
     *
     * @param hour 0到23
     * @param minute 0到59
     * @param second 0到59
     *
     * @deprecated 使用接受毫秒值的构造函数代替此构造函数
     */
    @Deprecated
    public Time(int hour, int minute, int second) {
        super(70, 0, 1, hour, minute, second);
    }

    /**
     * 使用毫秒时间值构造一个<code>Time</code>对象。
     *
     * @param time 自1970年1月1日00:00:00 GMT以来的毫秒数；
     *             负数表示1970年1月1日00:00:00 GMT之前的毫秒数
     */
    public Time(long time) {
        super(time);
    }

    /**
     * 使用毫秒时间值设置一个<code>Time</code>对象。
     *
     * @param time 自1970年1月1日00:00:00 GMT以来的毫秒数；
     *             负数表示1970年1月1日00:00:00 GMT之前的毫秒数
     */
    public void setTime(long time) {
        super.setTime(time);
    }

    /**
     * 将JDBC时间转义格式的字符串转换为<code>Time</code>值。
     *
     * @param s 格式为"hh:mm:ss"的时间
     * @return 对应的<code>Time</code>对象
     */
    public static Time valueOf(String s) {
        int hour;
        int minute;
        int second;
        int firstColon;
        int secondColon;

        if (s == null) throw new java.lang.IllegalArgumentException();

        firstColon = s.indexOf(':');
        secondColon = s.indexOf(':', firstColon+1);
        if ((firstColon > 0) & (secondColon > 0) &
            (secondColon < s.length()-1)) {
            hour = Integer.parseInt(s.substring(0, firstColon));
            minute =
                Integer.parseInt(s.substring(firstColon+1, secondColon));
            second = Integer.parseInt(s.substring(secondColon+1));
        } else {
            throw new java.lang.IllegalArgumentException();
        }

        return new Time(hour, minute, second);
    }

    /**
     * 以JDBC时间转义格式格式化时间。
     *
     * @return 格式为hh:mm:ss的<code>String</code>
     */
    @SuppressWarnings("deprecation")
    public String toString () {
        int hour = super.getHours();
        int minute = super.getMinutes();
        int second = super.getSeconds();
        String hourString;
        String minuteString;
        String secondString;

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
        return (hourString + ":" + minuteString + ":" + secondString);
    }

    // 覆盖从java.util.Date继承的所有日期操作；

   /**
    * 此方法已弃用，不应使用，因为SQL <code>TIME</code>
    * 值没有年份组件。
    *
    * @deprecated
    * @exception java.lang.IllegalArgumentException 如果调用此方法
    * @see #setYear
    */
    @Deprecated
    public int getYear() {
        throw new java.lang.IllegalArgumentException();
    }

   /**
    * 此方法已弃用，不应使用，因为SQL <code>TIME</code>
    * 值没有月份组件。
    *
    * @deprecated
    * @exception java.lang.IllegalArgumentException 如果调用此方法
    * @see #setMonth
    */
    @Deprecated
    public int getMonth() {
        throw new java.lang.IllegalArgumentException();
    }

   /**
    * 此方法已弃用，不应使用，因为SQL <code>TIME</code>
    * 值没有日期组件。
    *
    * @deprecated
    * @exception java.lang.IllegalArgumentException 如果调用此方法
    */
    @Deprecated
    public int getDay() {
        throw new java.lang.IllegalArgumentException();
    }

   /**
    * 此方法已弃用，不应使用，因为SQL <code>TIME</code>
    * 值没有日期组件。
    *
    * @deprecated
    * @exception java.lang.IllegalArgumentException 如果调用此方法
    * @see #setDate
    */
    @Deprecated
    public int getDate() {
        throw new java.lang.IllegalArgumentException();
    }

   /**
    * 此方法已弃用，不应使用，因为SQL <code>TIME</code>
    * 值没有年份组件。
    *
    * @deprecated
    * @exception java.lang.IllegalArgumentException 如果调用此方法
    * @see #getYear
    */
    @Deprecated
    public void setYear(int i) {
        throw new java.lang.IllegalArgumentException();
    }

   /**
    * 此方法已弃用，不应使用，因为SQL <code>TIME</code>
    * 值没有月份组件。
    *
    * @deprecated
    * @exception java.lang.IllegalArgumentException 如果调用此方法
    * @see #getMonth
    */
    @Deprecated
    public void setMonth(int i) {
        throw new java.lang.IllegalArgumentException();
    }

               /**
    * 此方法已弃用，不应使用，因为 SQL <code>TIME</code>
    * 值没有日期组件。
    *
    * @deprecated
    * @exception java.lang.IllegalArgumentException 如果调用此
    *           方法
    * @see #getDate
    */
    @Deprecated
    public void setDate(int i) {
        throw new java.lang.IllegalArgumentException();
    }

   /**
    * 私有序列化版本唯一 ID，以确保序列化兼容性。
    */
    static final long serialVersionUID = 8397324403548013681L;

    /**
     * 从 {@link LocalTime} 对象中获取一个具有相同小时、分钟和秒时间值的 {@code Time} 实例
     *
     * @param time 要转换的 {@code LocalTime}
     * @return 一个 {@code Time} 对象
     * @exception NullPointerException 如果 {@code time} 为 null
     * @since 1.8
     */
    @SuppressWarnings("deprecation")
    public static Time valueOf(LocalTime time) {
        return new Time(time.getHour(), time.getMinute(), time.getSecond());
    }

    /**
     * 将此 {@code Time} 对象转换为 {@code LocalTime}。
     * <p>
     * 转换创建一个表示与该 {@code Time} 相同的
     * 小时、分钟和秒时间值的 {@code LocalTime}。
     *
     * @return 表示相同时间值的 {@code LocalTime} 对象
     * @since 1.8
     */
    @SuppressWarnings("deprecation")
    public LocalTime toLocalTime() {
        return LocalTime.of(getHours(), getMinutes(), getSeconds());
    }

   /**
    * 此方法总是抛出一个 UnsupportedOperationException，不应使用，因为 SQL {@code Time} 值没有日期
    * 组件。
    *
    * @exception java.lang.UnsupportedOperationException 如果调用此方法
    */
    @Override
    public Instant toInstant() {
        throw new java.lang.UnsupportedOperationException();
    }
}
