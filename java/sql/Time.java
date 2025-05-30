/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.sql;

import java.time.Instant;
import java.time.LocalTime;

/**
 * <P>一个对 <code>java.util.Date</code> 类的轻量级包装，允许 JDBC
 * API 将其识别为 SQL <code>TIME</code> 值。 <code>Time</code>
 * 类添加了格式化和
 * 解析操作以支持 JDBC 逃逸语法中的时间值。
 * <p>日期组件应设置为“零纪元”
 * 值 1970 年 1 月 1 日，不应访问这些组件。
 */
public class Time extends java.util.Date {

    /**
     * 使用给定的小时、分钟和秒值构造一个 <code>Time</code> 对象。
     * 驱动程序将日期组件设置为 1970 年 1 月 1 日。
     * 任何尝试访问 <code>Time</code> 对象的日期组件的方法都将抛出
     * <code>java.lang.IllegalArgumentException</code>。
     * <P>
     * 如果给定的参数超出范围，结果是未定义的。
     *
     * @param hour 0 到 23
     * @param minute 0 到 59
     * @param second 0 到 59
     *
     * @deprecated 使用接受毫秒值的构造函数
     *             代替此构造函数
     */
    @Deprecated
    public Time(int hour, int minute, int second) {
        super(70, 0, 1, hour, minute, second);
    }

    /**
     * 使用毫秒时间值构造一个 <code>Time</code> 对象。
     *
     * @param time 自 1970 年 1 月 1 日 00:00:00 GMT 以来的毫秒数；
     *             负数表示 1970 年 1 月 1 日 00:00:00 GMT 之前的毫秒数
     */
    public Time(long time) {
        super(time);
    }

    /**
     * 使用毫秒时间值设置一个 <code>Time</code> 对象。
     *
     * @param time 自 1970 年 1 月 1 日 00:00:00 GMT 以来的毫秒数；
     *             负数表示 1970 年 1 月 1 日 00:00:00 GMT 之前的毫秒数
     */
    public void setTime(long time) {
        super.setTime(time);
    }

    /**
     * 将 JDBC 时间逃逸格式的字符串转换为 <code>Time</code> 值。
     *
     * @param s 时间格式为 "hh:mm:ss"
     * @return 对应的 <code>Time</code> 对象
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
     * 将时间格式化为 JDBC 时间逃逸格式。
     *
     * @return 格式为 hh:mm:ss 的 <code>String</code>
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

    // 覆盖从 java.util.Date 继承的所有日期操作；

   /**
    * 此方法已废弃，不应使用，因为 SQL <code>TIME</code>
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
    * 此方法已废弃，不应使用，因为 SQL <code>TIME</code>
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
    * 此方法已废弃，不应使用，因为 SQL <code>TIME</code>
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
    * 此方法已废弃，不应使用，因为 SQL <code>TIME</code>
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
    * 此方法已废弃，不应使用，因为 SQL <code>TIME</code>
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
    * 此方法已废弃，不应使用，因为 SQL <code>TIME</code>
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
    * 此方法已废弃，不应使用，因为 SQL <code>TIME</code>
    * 值没有日期组件。
    *
    * @deprecated
    * @exception java.lang.IllegalArgumentException 如果调用此方法
    * @see #getDate
    */
    @Deprecated
    public void setDate(int i) {
        throw new java.lang.IllegalArgumentException();
    }

   /**
    * 私有序列化版本唯一 ID 以确保序列化兼容性。
    */
    static final long serialVersionUID = 8397324403548013681L;

    /**
     * 从一个 {@link LocalTime} 对象获取一个 {@code Time} 实例
     * 具有与给定的 {@code LocalTime} 相同的小时、分钟和秒时间值。
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
     * 将此 {@code Time} 对象转换为一个 {@code LocalTime}。
     * <p>
     * 转换创建一个表示与此 {@code Time} 相同
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
