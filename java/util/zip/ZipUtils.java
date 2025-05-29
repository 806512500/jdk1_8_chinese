/*
 * Copyright (c) 2013, 2015, Oracle and/or its affiliates. All rights reserved.
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

package java.util.zip;

import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

class ZipUtils {

    // 用于调整 Windows 和 Java 时代之间的值
    private static final long WINDOWS_EPOCH_IN_MICROSECONDS = -11644473600000000L;

    /**
     * 将 Windows 时间（以微秒为单位，UTC/GMT）转换为 FileTime。
     */
    public static final FileTime winTimeToFileTime(long wtime) {
        return FileTime.from(wtime / 10 + WINDOWS_EPOCH_IN_MICROSECONDS,
                             TimeUnit.MICROSECONDS);
    }

    /**
     * 将 FileTime 转换为 Windows 时间。
     */
    public static final long fileTimeToWinTime(FileTime ftime) {
        return (ftime.to(TimeUnit.MICROSECONDS) - WINDOWS_EPOCH_IN_MICROSECONDS) * 10;
    }

    /**
     * 将“标准 Unix 时间”（以秒为单位，UTC/GMT）转换为 FileTime
     */
    public static final FileTime unixTimeToFileTime(long utime) {
        return FileTime.from(utime, TimeUnit.SECONDS);
    }

    /**
     * 将 FileTime 转换为“标准 Unix 时间”。
     */
    public static final long fileTimeToUnixTime(FileTime ftime) {
        return ftime.to(TimeUnit.SECONDS);
    }

    /**
     * 将 DOS 时间转换为 Java 时间（自纪元以来的毫秒数）。
     */
    private static long dosToJavaTime(long dtime) {
        @SuppressWarnings("deprecation") // 使用日期构造函数。
        Date d = new Date((int)(((dtime >> 25) & 0x7f) + 80),
                          (int)(((dtime >> 21) & 0x0f) - 1),
                          (int)((dtime >> 16) & 0x1f),
                          (int)((dtime >> 11) & 0x1f),
                          (int)((dtime >> 5) & 0x3f),
                          (int)((dtime << 1) & 0x3e));
        return d.getTime();
    }

    /**
     * 将扩展的 DOS 时间转换为 Java 时间，其中最多 1999 毫秒可能编码到返回的 long 的上半部分。
     *
     * @param xdostime 扩展的 DOS 时间值
     * @return 自纪元以来的毫秒数
     */
    public static long extendedDosToJavaTime(long xdostime) {
        long time = dosToJavaTime(xdostime);
        return time + (xdostime >> 32);
    }

    /**
     * 将 Java 时间转换为 DOS 时间。
     */
    @SuppressWarnings("deprecation") // 使用日期方法
    private static long javaToDosTime(long time) {
        Date d = new Date(time);
        int year = d.getYear() + 1900;
        if (year < 1980) {
            return ZipEntry.DOSTIME_BEFORE_1980;
        }
        return (year - 1980) << 25 | (d.getMonth() + 1) << 21 |
               d.getDate() << 16 | d.getHours() << 11 | d.getMinutes() << 5 |
               d.getSeconds() >> 1;
    }

    /**
     * 将 Java 时间转换为 DOS 时间，将转换中丢失的任何毫秒编码到返回的 long 的上半部分。
     *
     * @param time 自纪元以来的毫秒数
     * @return DOS 时间，2 秒余数编码到上半部分
     */
    public static long javaToExtendedDosTime(long time) {
        if (time < 0) {
            return ZipEntry.DOSTIME_BEFORE_1980;
        }
        long dostime = javaToDosTime(time);
        return (dostime != ZipEntry.DOSTIME_BEFORE_1980)
                ? dostime + ((time % 2000) << 32)
                : ZipEntry.DOSTIME_BEFORE_1980;
    }

    /**
     * 从指定偏移量的字节数组中获取无符号 16 位值。
     * 假设字节顺序为 Intel（小端）字节顺序。
     */
    public static final int get16(byte b[], int off) {
        return Byte.toUnsignedInt(b[off]) | (Byte.toUnsignedInt(b[off+1]) << 8);
    }

    /**
     * 从指定偏移量的字节数组中获取无符号 32 位值。
     * 假设字节顺序为 Intel（小端）字节顺序。
     */
    public static final long get32(byte b[], int off) {
        return (get16(b, off) | ((long)get16(b, off+2) << 16)) & 0xffffffffL;
    }

    /**
     * 从指定偏移量的字节数组中获取有符号 64 位值。
     * 假设字节顺序为 Intel（小端）字节顺序。
     */
    public static final long get64(byte b[], int off) {
        return get32(b, off) | (get32(b, off+4) << 32);
    }
}
