
/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

import java.lang.annotation.Native;
import java.math.*;


/**
 * {@code Long} 类将一个原始类型 {@code long} 的值封装在一个对象中。一个 {@code Long} 类型的对象包含一个类型为 {@code long} 的单个字段。
 *
 * <p>此外，此类提供了将 {@code long} 转换为 {@code String} 和将 {@code String} 转换为 {@code long} 的几种方法，以及其他在处理 {@code long} 时有用的常量和方法。
 *
 * <p>实现注释：位操作方法（如 {@link #highestOneBit(long) highestOneBit} 和 {@link #numberOfTrailingZeros(long) numberOfTrailingZeros}）的实现基于 Henry S. Warren, Jr. 的《Hacker's Delight》（Addison Wesley, 2002）中的材料。
 *
 * @author  Lee Boynton
 * @author  Arthur van Hoff
 * @author  Josh Bloch
 * @author  Joseph D. Darcy
 * @since   JDK1.0
 */
public final class Long extends Number implements Comparable<Long> {
    /**
     * 一个常量，表示 {@code long} 可以具有的最小值，-2<sup>63</sup>。
     */
    @Native public static final long MIN_VALUE = 0x8000000000000000L;

    /**
     * 一个常量，表示 {@code long} 可以具有的最大值，2<sup>63</sup>-1。
     */
    @Native public static final long MAX_VALUE = 0x7fffffffffffffffL;

    /**
     * 表示原始类型 {@code long} 的 {@code Class} 实例。
     *
     * @since   JDK1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Long>     TYPE = (Class<Long>) Class.getPrimitiveClass("long");

    /**
     * 返回第一个参数在由第二个参数指定的基数中的字符串表示形式。
     *
     * <p>如果基数小于 {@code Character.MIN_RADIX} 或大于 {@code Character.MAX_RADIX}，则使用基数 {@code 10}。
     *
     * <p>如果第一个参数为负数，则结果的第一个字符是 ASCII 负号 {@code '-'} ({@code '\u005Cu002d'})。如果第一个参数不为负数，则结果中不会出现符号字符。
     *
     * <p>结果的其余字符表示第一个参数的绝对值。如果绝对值为零，则用单个零字符 {@code '0'} ({@code '\u005Cu0030'}) 表示；否则，表示绝对值的第一个字符不会是零字符。以下 ASCII 字符用作数字：
     *
     * <blockquote>
     *   {@code 0123456789abcdefghijklmnopqrstuvwxyz}
     * </blockquote>
     *
     * 这些是 {@code '\u005Cu0030'} 到 {@code '\u005Cu0039'} 和 {@code '\u005Cu0061'} 到 {@code '\u005Cu007a'}。如果基数为 <var>N</var>，则使用这些字符中的前 <var>N</var> 个字符作为基数-<var>N</var> 的数字，顺序如上所示。因此，十六进制（基数 16）的数字为 {@code 0123456789abcdef}。如果需要大写字母，可以调用结果的 {@link java.lang.String#toUpperCase()} 方法：
     *
     * <blockquote>
     *  {@code Long.toString(n, 16).toUpperCase()}
     * </blockquote>
     *
     * @param   i       要转换为字符串的 {@code long}。
     * @param   radix   字符串表示形式中使用的基数。
     * @return  指定基数中的参数的字符串表示形式。
     * @see     java.lang.Character#MAX_RADIX
     * @see     java.lang.Character#MIN_RADIX
     */
    public static String toString(long i, int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;
        if (radix == 10)
            return toString(i);
        char[] buf = new char[65];
        int charPos = 64;
        boolean negative = (i < 0);

        if (!negative) {
            i = -i;
        }

        while (i <= -radix) {
            buf[charPos--] = Integer.digits[(int)(-(i % radix))];
            i = i / radix;
        }
        buf[charPos] = Integer.digits[(int)(-i)];

        if (negative) {
            buf[--charPos] = '-';
        }

        return new String(buf, charPos, (65 - charPos));
    }

    /**
     * 返回第一个参数作为无符号整数值在由第二个参数指定的基数中的字符串表示形式。
     *
     * <p>如果基数小于 {@code Character.MIN_RADIX} 或大于 {@code Character.MAX_RADIX}，则使用基数 {@code 10}。
     *
     * <p>由于第一个参数被视为无符号值，因此不会打印前导符号字符。
     *
     * <p>如果绝对值为零，则用单个零字符 {@code '0'} ({@code '\u005Cu0030'}) 表示；否则，表示绝对值的第一个字符不会是零字符。
     *
     * <p>基数和用作数字的字符的行为与 {@link #toString(long, int) toString} 相同。
     *
     * @param   i       要转换为无符号字符串的整数。
     * @param   radix   字符串表示形式中使用的基数。
     * @return  指定基数中的参数的无符号字符串表示形式。
     * @see     #toString(long, int)
     * @since 1.8
     */
    public static String toUnsignedString(long i, int radix) {
        if (i >= 0)
            return toString(i, radix);
        else {
            switch (radix) {
            case 2:
                return toBinaryString(i);

            case 4:
                return toUnsignedString0(i, 2);

            case 8:
                return toOctalString(i);

            case 10:
                /*
                 * 通过首先右移得到一个正数，然后再除以 5，可以得到一个 long 值的无符号除以 10 的效果。
                 * 这样可以比初始转换为 BigInteger 更快地隔离最后一位数字和前面的数字。
                 */
                long quot = (i >>> 1) / 5;
                long rem = i - quot * 10;
                return toString(quot) + rem;

            case 16:
                return toHexString(i);

            case 32:
                return toUnsignedString0(i, 5);

            default:
                return toUnsignedBigInteger(i).toString(radix);
            }
        }
    }

    /**
     * 返回一个等于参数的无符号值的 BigInteger。
     */
    private static BigInteger toUnsignedBigInteger(long i) {
        if (i >= 0L)
            return BigInteger.valueOf(i);
        else {
            int upper = (int) (i >>> 32);
            int lower = (int) i;

            // return (upper << 32) + lower
            return (BigInteger.valueOf(Integer.toUnsignedLong(upper))).shiftLeft(32).
                add(BigInteger.valueOf(Integer.toUnsignedLong(lower)));
        }
    }

    /**
     * 返回 {@code long} 参数作为无符号整数在基数 16 中的字符串表示形式。
     *
     * <p>无符号的 {@code long} 值是参数加上 2<sup>64</sup>（如果参数为负数）；否则，它等于参数。此值转换为无前导 {@code 0} 的 ASCII 十六进制（基数 16）数字字符串。
     *
     * <p>可以通过调用 {@link Long#parseUnsignedLong(String, int) Long.parseUnsignedLong(s, 16)} 从返回的字符串 {@code s} 中恢复参数的值。
     *
     * <p>如果无符号的绝对值为零，则用单个零字符 {@code '0'} ({@code '\u005Cu0030'}) 表示；否则，表示无符号绝对值的第一个字符不会是零字符。以下字符用作十六进制数字：
     *
     * <blockquote>
     *  {@code 0123456789abcdef}
     * </blockquote>
     *
     * 这些是 {@code '\u005Cu0030'} 到 {@code '\u005Cu0039'} 和 {@code '\u005Cu0061'} 到 {@code '\u005Cu0066'}。如果需要大写字母，可以调用结果的 {@link java.lang.String#toUpperCase()} 方法：
     *
     * <blockquote>
     *  {@code Long.toHexString(n).toUpperCase()}
     * </blockquote>
     *
     * @param   i   要转换为字符串的 {@code long}。
     * @return  参数在十六进制（基数 16）中的无符号 {@code long} 值的字符串表示形式。
     * @see #parseUnsignedLong(String, int)
     * @see #toUnsignedString(long, int)
     * @since   JDK 1.0.2
     */
    public static String toHexString(long i) {
        return toUnsignedString0(i, 4);
    }

    /**
     * 返回 {@code long} 参数作为无符号整数在基数 8 中的字符串表示形式。
     *
     * <p>无符号的 {@code long} 值是参数加上 2<sup>64</sup>（如果参数为负数）；否则，它等于参数。此值转换为无前导 {@code 0} 的 ASCII 八进制（基数 8）数字字符串。
     *
     * <p>可以通过调用 {@link Long#parseUnsignedLong(String, int) Long.parseUnsignedLong(s, 8)} 从返回的字符串 {@code s} 中恢复参数的值。
     *
     * <p>如果无符号的绝对值为零，则用单个零字符 {@code '0'} ({@code '\u005Cu0030'}) 表示；否则，表示无符号绝对值的第一个字符不会是零字符。以下字符用作八进制数字：
     *
     * <blockquote>
     *  {@code 01234567}
     * </blockquote>
     *
     * 这些是 {@code '\u005Cu0030'} 到 {@code '\u005Cu0037'}。
     *
     * @param   i   要转换为字符串的 {@code long}。
     * @return  参数在八进制（基数 8）中的无符号 {@code long} 值的字符串表示形式。
     * @see #parseUnsignedLong(String, int)
     * @see #toUnsignedString(long, int)
     * @since   JDK 1.0.2
     */
    public static String toOctalString(long i) {
        return toUnsignedString0(i, 3);
    }

    /**
     * 返回 {@code long} 参数作为无符号整数在基数 2 中的字符串表示形式。
     *
     * <p>无符号的 {@code long} 值是参数加上 2<sup>64</sup>（如果参数为负数）；否则，它等于参数。此值转换为无前导 {@code 0} 的 ASCII 二进制（基数 2）数字字符串。
     *
     * <p>可以通过调用 {@link Long#parseUnsignedLong(String, int) Long.parseUnsignedLong(s, 2)} 从返回的字符串 {@code s} 中恢复参数的值。
     *
     * <p>如果无符号的绝对值为零，则用单个零字符 {@code '0'} ({@code '\u005Cu0030'}) 表示；否则，表示无符号绝对值的第一个字符不会是零字符。字符 {@code '0'} ({@code '\u005Cu0030'}) 和 {@code '1'} ({@code '\u005Cu0031'}) 用作二进制数字。
     *
     * @param   i   要转换为字符串的 {@code long}。
     * @return  参数在二进制（基数 2）中的无符号 {@code long} 值的字符串表示形式。
     * @see #parseUnsignedLong(String, int)
     * @see #toUnsignedString(long, int)
     * @since   JDK 1.0.2
     */
    public static String toBinaryString(long i) {
        return toUnsignedString0(i, 1);
    }

    /**
     * 将 long（视为无符号）格式化为字符串。
     * @param val 要格式化的值
     * @param shift 基数的 log2 值（4 表示十六进制，3 表示八进制，1 表示二进制）
     */
    static String toUnsignedString0(long val, int shift) {
        // assert shift > 0 && shift <=5 : "Illegal shift value";
        int mag = Long.SIZE - Long.numberOfLeadingZeros(val);
        int chars = Math.max(((mag + (shift - 1)) / shift), 1);
        char[] buf = new char[chars];

        formatUnsignedLong(val, shift, buf, 0, chars);
        return new String(buf, true);
    }

    /**
     * 将 long（视为无符号）格式化为字符缓冲区。
     * @param val 要格式化的无符号 long
     * @param shift 基数的 log2 值（4 表示十六进制，3 表示八进制，1 表示二进制）
     * @param buf 要写入的字符缓冲区
     * @param offset 目标缓冲区中开始的位置
     * @param len 要写入的字符数
     * @return 使用的最低字符位置
     */
     static int formatUnsignedLong(long val, int shift, char[] buf, int offset, int len) {
        int charPos = len;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            buf[offset + --charPos] = Integer.digits[((int) val) & mask];
            val >>>= shift;
        } while (val != 0 && charPos > 0);

        return charPos;
    }

    /**
     * 返回表示指定 {@code long} 的 {@code String} 对象。参数转换为带符号的十进制表示形式并返回为字符串，就像将参数和基数 10 作为参数传递给 {@link #toString(long, int)} 方法一样。
     *
     * @param   i   要转换的 {@code long}。
     * @return  参数在基数 10 中的字符串表示形式。
     */
    public static String toString(long i) {
        if (i == Long.MIN_VALUE)
            return "-9223372036854775808";
        int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
        char[] buf = new char[size];
        getChars(i, size, buf);
        return new String(buf, true);
    }


                /**
     * 返回参数的无符号十进制值的字符串表示。
     *
     * 参数被转换为无符号十进制表示，并作为字符串返回，就像参数和基数
     * 10被作为参数传递给 {@link #toUnsignedString(long,
     * int)} 方法一样。
     *
     * @param   i  要转换为无符号字符串的整数。
     * @return  参数的无符号字符串表示。
     * @see     #toUnsignedString(long, int)
     * @since 1.8
     */
    public static String toUnsignedString(long i) {
        return toUnsignedString(i, 10);
    }

    /**
     * 将表示整数 i 的字符放入字符数组 buf 中。字符被倒序放入
     * 缓冲区，从指定的索引（不包括）开始，从那里倒序放置。
     *
     * 如果 i == Long.MIN_VALUE 会失败
     */
    static void getChars(long i, int index, char[] buf) {
        long q;
        int r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // 使用 longs 获取每迭代 2 位数字，直到商适合放入 int 中
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // 真实情况：r = i - (q * 100);
            r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = Integer.DigitOnes[r];
            buf[--charPos] = Integer.DigitTens[r];
        }

        // 使用 ints 获取每迭代 2 位数字
        int q2;
        int i2 = (int)i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // 真实情况：r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = Integer.DigitOnes[r];
            buf[--charPos] = Integer.DigitTens[r];
        }

        // 转入更小数字的快速模式
        // assert(i2 <= 65536, i2);
        for (;;) {
            q2 = (i2 * 52429) >>> (16+3);
            r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
            buf[--charPos] = Integer.digits[r];
            i2 = q2;
            if (i2 == 0) break;
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    // 需要正数 x
    static int stringSize(long x) {
        long p = 10;
        for (int i=1; i<19; i++) {
            if (x < p)
                return i;
            p = 10*p;
        }
        return 19;
    }

    /**
     * 将字符串参数解析为指定基数的带符号 {@code long}。字符串中的字符必须都是指定基数的数字（通过
     * {@link java.lang.Character#digit(char, int)} 方法确定），但第一个字符可以是
     * ASCII 减号 {@code '-'} ({@code '\u005Cu002D'}) 表示负值或 ASCII 加号 {@code '+'}
     * ({@code '\u005Cu002B'}) 表示正值。返回解析得到的 {@code long} 值。
     *
     * <p>注意，字符 {@code L}
     * ({@code '\u005Cu004C'}) 或 {@code l}
     * ({@code '\u005Cu006C'}) 不允许出现在字符串末尾作为类型指示符，就像在 Java 编程语言源代码中允许的一样 - 除非
     * {@code L} 或 {@code l} 作为基数大于或等于 22 的数字出现。
     *
     * <p>如果发生以下任何情况，将抛出 {@code NumberFormatException} 异常：
     * <ul>
     *
     * <li>第一个参数为 {@code null} 或长度为零的字符串。
     *
     * <li>基数小于 {@link
     * java.lang.Character#MIN_RADIX} 或大于 {@link
     * java.lang.Character#MAX_RADIX}。
     *
     * <li>字符串中的任何字符都不是指定基数的数字，但第一个字符可以是减号
     * {@code '-'} ({@code '\u005Cu002d'}) 或加号 {@code
     * '+'} ({@code '\u005Cu002B'})，前提是字符串长度大于 1。
     *
     * <li>字符串表示的值不是 {@code long} 类型的值。
     * </ul>
     *
     * <p>示例：
     * <blockquote><pre>
     * parseLong("0", 10) 返回 0L
     * parseLong("473", 10) 返回 473L
     * parseLong("+42", 10) 返回 42L
     * parseLong("-0", 10) 返回 0L
     * parseLong("-FF", 16) 返回 -255L
     * parseLong("1100110", 2) 返回 102L
     * parseLong("99", 8) 抛出 NumberFormatException
     * parseLong("Hazelnut", 10) 抛出 NumberFormatException
     * parseLong("Hazelnut", 36) 返回 1356099454469L
     * </pre></blockquote>
     *
     * @param      s       包含要解析的 {@code long} 表示的 {@code String}。
     * @param      radix   解析 {@code s} 时使用的基数。
     * @return     由字符串参数在指定基数中表示的 {@code long}。
     * @throws     NumberFormatException  如果字符串不包含可解析的 {@code long}。
     */
    public static long parseLong(String s, int radix)
              throws NumberFormatException
    {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix +
                                            " less than Character.MIN_RADIX");
        }
        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix +
                                            " greater than Character.MAX_RADIX");
        }

        long result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        long limit = -Long.MAX_VALUE;
        long multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // 可能的前导 "+" 或 "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+')
                    throw NumberFormatException.forInputString(s);

                if (len == 1) // 不能有单独的 "+" 或 "-"
                    throw NumberFormatException.forInputString(s);
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // 累加负数以避免接近 MAX_VALUE 时的意外
                digit = Character.digit(s.charAt(i++),radix);
                if (digit < 0) {
                    throw NumberFormatException.forInputString(s);
                }
                if (result < multmin) {
                    throw NumberFormatException.forInputString(s);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw NumberFormatException.forInputString(s);
                }
                result -= digit;
            }
        } else {
            throw NumberFormatException.forInputString(s);
        }
        return negative ? result : -result;
    }

    /**
     * 将字符串参数解析为带符号的十进制 {@code long}。字符串中的字符必须全部是十进制数字，但
     * 第一个字符可以是 ASCII 减号 {@code '-'}
     * ({@code \u005Cu002D'}) 表示负值或 ASCII 加号 {@code '+'} ({@code '\u005Cu002B'}) 表示正值。返回解析得到的
     * {@code long} 值，就像将参数和基数 {@code 10} 作为参数传递给 {@link
     * #parseLong(java.lang.String, int)} 方法一样。
     *
     * <p>注意，字符 {@code L}
     * ({@code '\u005Cu004C'}) 或 {@code l}
     * ({@code '\u005Cu006C'}) 不允许出现在字符串末尾作为类型指示符，就像在 Java 编程语言源代码中允许的一样。
     *
     * @param      s   包含要解析的 {@code long} 表示的 {@code String}。
     * @return     由参数在十进制中表示的 {@code long}。
     * @throws     NumberFormatException  如果字符串不包含可解析的 {@code long}。
     */
    public static long parseLong(String s) throws NumberFormatException {
        return parseLong(s, 10);
    }

    /**
     * 将字符串参数解析为指定基数的无符号 {@code long}。无符号整数将通常与负数关联的值映射到大于
     * {@code MAX_VALUE} 的正数。
     *
     * 字符串中的字符必须全部是指定基数的数字（通过
     * {@link java.lang.Character#digit(char, int)} 方法确定），但第一个字符可以是
     * ASCII 加号 {@code '+'} ({@code '\u005Cu002B'})。返回解析得到的整数值。
     *
     * <p>如果发生以下任何情况，将抛出 {@code NumberFormatException} 异常：
     * <ul>
     * <li>第一个参数为 {@code null} 或长度为零的字符串。
     *
     * <li>基数小于
     * {@link java.lang.Character#MIN_RADIX} 或
     * 大于 {@link java.lang.Character#MAX_RADIX}。
     *
     * <li>字符串中的任何字符都不是指定基数的数字，但第一个字符可以是加号
     * {@code '+'} ({@code '\u005Cu002B'})，前提是字符串长度大于 1。
     *
     * <li>字符串表示的值大于最大的无符号 {@code long}，即 2<sup>64</sup>-1。
     *
     * </ul>
     *
     *
     * @param      s   包含要解析的无符号整数表示的 {@code String}。
     * @param      radix   解析 {@code s} 时使用的基数。
     * @return     由字符串参数在指定基数中表示的无符号 {@code long}。
     * @throws     NumberFormatException 如果 {@code String}
     *             不包含可解析的 {@code long}。
     * @since 1.8
     */
    public static long parseUnsignedLong(String s, int radix)
                throws NumberFormatException {
        if (s == null)  {
            throw new NumberFormatException("null");
        }

        int len = s.length();
        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar == '-') {
                throw new
                    NumberFormatException(String.format("非法的前导减号 " +
                                                       "在无符号字符串 %s 中。", s));
            } else {
                if (len <= 12 || // Long.MAX_VALUE 在 Character.MAX_RADIX 中是 13 位数字
                    (radix == 10 && len <= 18) ) { // Long.MAX_VALUE 在基数 10 中是 19 位数字
                    return parseLong(s, radix);
                }

                // 由于上述测试，无需对 len 进行范围检查。
                long first = parseLong(s.substring(0, len - 1), radix);
                int second = Character.digit(s.charAt(len - 1), radix);
                if (second < 0) {
                    throw new NumberFormatException("末尾的坏数字 " + s);
                }
                long result = first * radix + second;
                if (compareUnsigned(result, first) < 0) {
                    /*
                     * 最大的无符号值，(2^64)-1，最多比最大的有符号值，(2^63)-1，多一位数字。因此，
                     * 解析 (len - 1) 位数字将适合有符号解析的范围。换句话说，如果解析 (len -1) 位数字导致有符号解析溢出，
                     * 解析 len 位数字肯定会导致无符号解析溢出。
                     *
                     * 上面的 compareUnsigned 检查捕获了将最终数字的贡献纳入无符号溢出的情况。
                     */
                    throw new NumberFormatException(String.format("字符串值 %s 超过 " +
                                                                  "无符号 long 的范围。", s));
                }
                return result;
            }
        } else {
            throw NumberFormatException.forInputString(s);
        }
    }

    /**
     * 将字符串参数解析为无符号的十进制 {@code long}。字符串中的字符必须全部是十进制数字，但
     * 第一个字符可以是 ASCII 加号 {@code
     * '+'} ({@code '\u005Cu002B'})。返回解析得到的整数值，就像将参数和基数 10 作为参数传递给 {@link
     * #parseUnsignedLong(java.lang.String, int)} 方法一样。
     *
     * @param s   包含要解析的无符号 {@code long} 表示的 {@code String}。
     * @return    由十进制字符串参数表示的无符号 {@code long} 值
     * @throws    NumberFormatException  如果字符串不包含可解析的无符号整数。
     * @since 1.8
     */
    public static long parseUnsignedLong(String s) throws NumberFormatException {
        return parseUnsignedLong(s, 10);
    }

    /**
     * 返回一个包含从指定 {@code String} 解析出的值的 {@code Long} 对象。第一个
     * 参数被解释为指定基数的带符号 {@code long}，就像将参数传递给 {@link
     * #parseLong(java.lang.String, int)} 方法一样。结果是一个表示字符串中指定的
     * {@code long} 值的 {@code Long} 对象。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Long} 对象：
     *
     * <blockquote>
     *  {@code new Long(Long.parseLong(s, radix))}
     * </blockquote>
     *
     * @param      s       要解析的字符串
     * @param      radix   解释 {@code s} 时使用的基数
     * @return     一个包含由字符串参数在指定基数中表示的值的 {@code Long} 对象。
     * @throws     NumberFormatException  如果 {@code String} 不包含可解析的 {@code long}。
     */
    public static Long valueOf(String s, int radix) throws NumberFormatException {
        return Long.valueOf(parseLong(s, radix));
    }

    /**
     * 返回一个包含指定 {@code String} 的值的 {@code Long} 对象。参数被解释为带符号的十进制
     * {@code long}，就像将参数传递给 {@link
     * #parseLong(java.lang.String)} 方法一样。结果是一个表示字符串中指定的整数值的
     * {@code Long} 对象。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Long} 对象：
     *
     * <blockquote>
     *  {@code new Long(Long.parseLong(s))}
     * </blockquote>
     *
     * @param      s   要解析的字符串。
     * @return     一个包含由字符串参数表示的值的 {@code Long} 对象。
     * @throws     NumberFormatException  如果字符串不能解析为 {@code long}。
     */
    public static Long valueOf(String s) throws NumberFormatException
    {
        return Long.valueOf(parseLong(s, 10));
    }


                private static class LongCache {
        private LongCache(){}

        static final Long cache[] = new Long[-(-128) + 127 + 1];

        static {
            for(int i = 0; i < cache.length; i++)
                cache[i] = new Long(i - 128);
        }
    }

    /**
     * 返回一个表示指定 {@code long} 值的 {@code Long} 实例。
     * 如果不需要新的 {@code Long} 实例，通常应优先使用此方法而不是构造函数
     * {@link #Long(long)}，因为此方法通过缓存经常请求的值，可能会显著提高空间和时间性能。
     *
     * 请注意，与 {@linkplain Integer#valueOf(int)
     * 相应的方法} 不同，此方法 <em>不</em> 要求缓存特定范围内的值。
     *
     * @param  l 一个 long 值。
     * @return 一个表示 {@code l} 的 {@code Long} 实例。
     * @since  1.5
     */
    public static Long valueOf(long l) {
        final int offset = 128;
        if (l >= -128 && l <= 127) { // will cache
            return LongCache.cache[(int)l + offset];
        }
        return new Long(l);
    }

    /**
     * 将一个 {@code String} 解码为一个 {@code Long}。
     * 接受十进制、十六进制和八进制数字，由以下语法定义：
     *
     * <blockquote>
     * <dl>
     * <dt><i>可解码字符串：</i>
     * <dd><i>可选符号 十进制数字</i>
     * <dd><i>可选符号 0x 十六进制数字</i>
     * <dd><i>可选符号 0X 十六进制数字</i>
     * <dd><i>可选符号 # 十六进制数字</i>
     * <dd><i>可选符号 0 八进制数字</i>
     *
     * <dt><i>符号：</i>
     * <dd>{@code -}
     * <dd>{@code +}
     * </dl>
     * </blockquote>
     *
     * <i>十进制数字</i>、<i>十六进制数字</i> 和 <i>八进制数字</i>
     * 的定义见《Java&trade; 语言规范》第 3.10.1 节，但数字之间不允许使用下划线。
     *
     * <p>可选符号和/或基数指定符（“{@code 0x}”、“{@code 0X}”、“{@code #}”或前导零）之后的字符序列
     * 将按照 {@code Long.parseLong} 方法以指定的基数（10、16 或 8）进行解析。此字符序列必须表示一个正数，
     * 否则将抛出 {@link NumberFormatException}。如果指定的 {@code String} 的第一个字符是减号，则结果将取反。
     * {@code String} 中不允许有空白字符。
     *
     * @param     nm 要解码的 {@code String}。
     * @return    一个表示 {@code nm} 中的 {@code long} 值的 {@code Long} 对象
     * @throws    NumberFormatException 如果 {@code String} 不包含可解析的 {@code long}。
     * @see java.lang.Long#parseLong(String, int)
     * @since 1.2
     */
    public static Long decode(String nm) throws NumberFormatException {
        int radix = 10;
        int index = 0;
        boolean negative = false;
        Long result;

        if (nm.length() == 0)
            throw new NumberFormatException("Zero length string");
        char firstChar = nm.charAt(0);
        // 处理符号，如果存在
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+')
            index++;

        // 处理基数指定符，如果存在
        if (nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
            index += 2;
            radix = 16;
        }
        else if (nm.startsWith("#", index)) {
            index ++;
            radix = 16;
        }
        else if (nm.startsWith("0", index) && nm.length() > 1 + index) {
            index ++;
            radix = 8;
        }

        if (nm.startsWith("-", index) || nm.startsWith("+", index))
            throw new NumberFormatException("Sign character in wrong position");

        try {
            result = Long.valueOf(nm.substring(index), radix);
            result = negative ? Long.valueOf(-result.longValue()) : result;
        } catch (NumberFormatException e) {
            // 如果数字是 Long.MIN_VALUE，我们将在这里结束。下一行处理这种情况，
            // 并导致任何真正的格式错误被重新抛出。
            String constant = negative ? ("-" + nm.substring(index))
                                       : nm.substring(index);
            result = Long.valueOf(constant, radix);
        }
        return result;
    }

    /**
     * 该 {@code Long} 的值。
     *
     * @serial
     */
    private final long value;

    /**
     * 构造一个新的分配的 {@code Long} 对象，表示指定的 {@code long} 参数。
     *
     * @param   value   要由 {@code Long} 对象表示的值。
     */
    public Long(long value) {
        this.value = value;
    }

    /**
     * 构造一个新的分配的 {@code Long} 对象，表示由 {@code String} 参数指示的
     * {@code long} 值。字符串按照 {@code parseLong} 方法对基数 10 的处理方式转换为
     * {@code long} 值。
     *
     * @param      s   要转换为 {@code Long} 的 {@code String}。
     * @throws     NumberFormatException  如果 {@code String} 不包含可解析的 {@code long}。
     * @see        java.lang.Long#parseLong(java.lang.String, int)
     */
    public Long(String s) throws NumberFormatException {
        this.value = parseLong(s, 10);
    }

    /**
     * 返回此 {@code Long} 的值作为 {@code byte}，经过窄化原始转换。
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * 返回此 {@code Long} 的值作为 {@code short}，经过窄化原始转换。
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * 返回此 {@code Long} 的值作为 {@code int}，经过窄化原始转换。
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public int intValue() {
        return (int)value;
    }

    /**
     * 返回此 {@code Long} 的值作为 {@code long} 值。
     */
    public long longValue() {
        return value;
    }

    /**
     * 返回此 {@code Long} 的值作为 {@code float}，经过扩展原始转换。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * 返回此 {@code Long} 的值作为 {@code double}，经过扩展原始转换。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * 返回表示此 {@code Long} 值的 {@code String} 对象。该值转换为带符号的十进制表示形式并作为字符串返回，
     * 就像将 {@code long} 值作为参数传递给 {@link java.lang.Long#toString(long)} 方法一样。
     *
     * @return  此对象值的十进制字符串表示形式。
     */
    public String toString() {
        return toString(value);
    }

    /**
     * 返回此 {@code Long} 的哈希码。结果是此 {@code Long}
     * 对象持有的原始 {@code long} 值的两个部分的异或。也就是说，哈希码是以下表达式的值：
     *
     * <blockquote>
     *  {@code (int)(this.longValue()^(this.longValue()>>>32))}
     * </blockquote>
     *
     * @return  此对象的哈希码值。
     */
    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    /**
     * 返回 {@code long} 值的哈希码；与 {@code Long.hashCode()} 兼容。
     *
     * @param value 要哈希的值
     * @return 一个表示 {@code long} 值的哈希码。
     * @since 1.8
     */
    public static int hashCode(long value) {
        return (int)(value ^ (value >>> 32));
    }

    /**
     * 将此对象与指定对象进行比较。结果为
     * {@code true} 当且仅当参数不为
     * {@code null} 并且是一个包含与此对象相同的 {@code long} 值的 {@code Long} 对象。
     *
     * @param   obj   要比较的对象。
     * @return  如果对象相同则返回 {@code true}；
     *          否则返回 {@code false}。
     */
    public boolean equals(Object obj) {
        if (obj instanceof Long) {
            return value == ((Long)obj).longValue();
        }
        return false;
    }

    /**
     * 确定指定名称的系统属性的 {@code long} 值。
     *
     * <p>第一个参数被视为系统属性的名称。系统属性可以通过 {@link
     * java.lang.System#getProperty(java.lang.String)} 方法访问。此属性的字符串值
     * 然后根据 {@link Long#decode decode} 支持的语法解释为 {@code
     * long} 值，并返回一个表示此值的 {@code Long} 对象。
     *
     * <p>如果指定名称没有属性，指定名称为空或 {@code null}，或者属性没有正确的数字格式，
     * 则返回 {@code null}。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Long} 对象：
     *
     * <blockquote>
     *  {@code getLong(nm, null)}
     * </blockquote>
     *
     * @param   nm   属性名称。
     * @return  属性的 {@code Long} 值。
     * @throws  SecurityException 与 {@link System#getProperty(String) System.getProperty} 相同的原因
     * @see     java.lang.System#getProperty(java.lang.String)
     * @see     java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static Long getLong(String nm) {
        return getLong(nm, null);
    }

    /**
     * 确定指定名称的系统属性的 {@code long} 值。
     *
     * <p>第一个参数被视为系统属性的名称。系统属性可以通过 {@link
     * java.lang.System#getProperty(java.lang.String)} 方法访问。此属性的字符串值
     * 然后根据 {@link Long#decode decode} 支持的语法解释为 {@code
     * long} 值，并返回一个表示此值的 {@code Long} 对象。
     *
     * <p>第二个参数是默认值。如果指定名称没有属性，属性没有正确的数字格式，或者指定名称为空或 null，
     * 则返回一个表示第二个参数值的 {@code Long} 对象。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Long} 对象：
     *
     * <blockquote>
     *  {@code getLong(nm, new Long(val))}
     * </blockquote>
     *
     * 但在实践中，它可能实现为：
     *
     * <blockquote><pre>
     * Long result = getLong(nm, null);
     * return (result == null) ? new Long(val) : result;
     * </pre></blockquote>
     *
     * 以避免在不需要默认值时不必要的 {@code Long} 对象分配。
     *
     * @param   nm    属性名称。
     * @param   val   默认值。
     * @return  属性的 {@code Long} 值。
     * @throws  SecurityException 与 {@link System#getProperty(String) System.getProperty} 相同的原因
     * @see     java.lang.System#getProperty(java.lang.String)
     * @see     java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static Long getLong(String nm, long val) {
        Long result = Long.getLong(nm, null);
        return (result == null) ? Long.valueOf(val) : result;
    }

    /**
     * 返回指定名称的系统属性的 {@code long} 值。第一个参数被视为系统属性的名称。系统属性可以通过
     * {@link java.lang.System#getProperty(java.lang.String)}
     * 方法访问。此属性的字符串值然后根据
     * {@link Long#decode decode} 方法解释为 {@code long} 值，并返回一个表示此值的 {@code Long} 对象；
     * 概述如下：
     *
     * <ul>
     * <li>如果属性值以两个 ASCII 字符
     * {@code 0x} 或 ASCII 字符 {@code #} 开头，且不跟负号，则其余部分将解析为十六进制整数，
     * 就像 {@link #valueOf(java.lang.String, int)} 方法的基数 16 一样。
     * <li>如果属性值以 ASCII 字符
     * {@code 0} 开头并跟另一个字符，则解析为
     * 八进制整数，就像 {@link
     * #valueOf(java.lang.String, int)} 方法的基数 8 一样。
     * <li>否则，属性值将解析为十进制整数，就像
     * {@link #valueOf(java.lang.String, int)} 方法的基数 10 一样。
     * </ul>
     *
     * <p>请注意，在所有情况下，既不允许在属性值末尾出现 {@code L}
     * ({@code '\u005Cu004C'}) 也不允许出现 {@code l}
     * ({@code '\u005Cu006C'}) 作为类型指示符，就像在 Java 编程语言源代码中允许的一样。
     *
     * <p>第二个参数是默认值。如果指定名称没有属性，属性没有正确的数字格式，或者指定名称为空或 {@code null}，
     * 则返回默认值。
     *
     * @param   nm   属性名称。
     * @param   val   默认值。
     * @return  属性的 {@code Long} 值。
     * @throws  SecurityException 与 {@link System#getProperty(String) System.getProperty} 相同的原因
     * @see     System#getProperty(java.lang.String)
     * @see     System#getProperty(java.lang.String, java.lang.String)
     */
    public static Long getLong(String nm, Long val) {
        String v = null;
        try {
            v = System.getProperty(nm);
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        if (v != null) {
            try {
                return Long.decode(v);
            } catch (NumberFormatException e) {
            }
        }
        return val;
    }

    /**
     * 比较两个 {@code Long} 对象的数值。
     *
     * @param   anotherLong   要比较的 {@code Long}。
     * @return  如果此 {@code Long} 等于参数 {@code Long}，则返回值 {@code 0}；
     *          如果此 {@code Long} 数值小于参数 {@code Long}，则返回一个小于 {@code 0} 的值；
     *          如果此 {@code Long} 数值大于参数 {@code Long}，则返回一个大于 {@code 0} 的值（带符号比较）。
     * @since   1.2
     */
    public int compareTo(Long anotherLong) {
        return compare(this.value, anotherLong.value);
    }


/**
 * 比较两个 {@code long} 值的数值。
 * 返回值与以下代码返回的值相同：
 * <pre>
 *    Long.valueOf(x).compareTo(Long.valueOf(y))
 * </pre>
 *
 * @param  x 要比较的第一个 {@code long}
 * @param  y 要比较的第二个 {@code long}
 * @return 如果 {@code x == y}，则返回值为 {@code 0}；
 *         如果 {@code x < y}，则返回值小于 {@code 0}；
 *         如果 {@code x > y}，则返回值大于 {@code 0}
 * @since 1.7
 */
public static int compare(long x, long y) {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
}

/**
 * 比较两个 {@code long} 值的数值，将值视为无符号数。
 *
 * @param  x 要比较的第一个 {@code long}
 * @param  y 要比较的第二个 {@code long}
 * @return 如果 {@code x == y}，则返回值为 {@code 0}；
 *         如果 {@code x < y} 作为无符号数，则返回值小于 {@code 0}；
 *         如果 {@code x > y} 作为无符号数，则返回值大于 {@code 0}
 * @since 1.8
 */
public static int compareUnsigned(long x, long y) {
    return compare(x + MIN_VALUE, y + MIN_VALUE);
}

/**
 * 返回将第一个参数除以第二个参数得到的无符号商，其中每个参数和结果都被解释为无符号值。
 *
 * <p>注意，在二进制补码算术中，加法、减法和乘法这三个基本算术运算在将两个操作数视为都有符号或都无符号时是位相同的。
 * 因此没有提供单独的 {@code addUnsigned} 等方法。
 *
 * @param dividend 被除数
 * @param divisor 除数
 * @return 将第一个参数除以第二个参数得到的无符号商
 * @see #remainderUnsigned
 * @since 1.8
 */
public static long divideUnsigned(long dividend, long divisor) {
    if (divisor < 0L) { // 有符号比较
        // 答案必须是 0 或 1，取决于被除数和除数的相对大小
        return (compareUnsigned(dividend, divisor)) < 0 ? 0L :1L;
    }

    if (dividend > 0) // 两个输入都是非负数
        return dividend/divisor;
    else {
        /*
         * 为了代码简单，利用 BigInteger。直接用 long 的操作写更长更快的代码是可能的；
         * 有关除法和余数算法，请参见 "Hacker's Delight"。
         */
        return toUnsignedBigInteger(dividend).
            divide(toUnsignedBigInteger(divisor)).longValue();
    }
}

/**
 * 返回将第一个参数除以第二个参数得到的无符号余数，其中每个参数和结果都被解释为无符号值。
 *
 * @param dividend 被除数
 * @param divisor 除数
 * @return 将第一个参数除以第二个参数得到的无符号余数
 * @see #divideUnsigned
 * @since 1.8
 */
public static long remainderUnsigned(long dividend, long divisor) {
    if (dividend > 0 && divisor > 0) { // 有符号比较
        return dividend % divisor;
    } else {
        if (compareUnsigned(dividend, divisor) < 0) // 避免除数显式检查为 0
            return dividend;
        else
            return toUnsignedBigInteger(dividend).
                remainder(toUnsignedBigInteger(divisor)).longValue();
    }
}

// 位操作

/**
 * 用于表示一个 {@code long} 值在二进制补码形式中使用的位数。
 *
 * @since 1.5
 */
@Native public static final int SIZE = 64;

/**
 * 用于表示一个 {@code long} 值在二进制补码形式中使用的字节数。
 *
 * @since 1.8
 */
public static final int BYTES = SIZE / Byte.SIZE;

/**
 * 返回一个最多只有一个 1 位的 {@code long} 值，该位位于指定的 {@code long} 值的最高位（“最左边”）1 位的位置。
 * 如果指定的值在其二进制补码表示中没有 1 位，即等于零，则返回零。
 *
 * @param i 要计算最高 1 位的值
 * @return 一个最多只有一个 1 位的 {@code long} 值，该位位于指定值的最高位 1 位的位置，或如果指定的值本身等于零，则返回零。
 * @since 1.5
 */
public static long highestOneBit(long i) {
    // HD, Figure 3-1
    i |= (i >>  1);
    i |= (i >>  2);
    i |= (i >>  4);
    i |= (i >>  8);
    i |= (i >> 16);
    i |= (i >> 32);
    return i - (i >>> 1);
}

/**
 * 返回一个最多只有一个 1 位的 {@code long} 值，该位位于指定的 {@code long} 值的最低位（“最右边”）1 位的位置。
 * 如果指定的值在其二进制补码表示中没有 1 位，即等于零，则返回零。
 *
 * @param i 要计算最低 1 位的值
 * @return 一个最多只有一个 1 位的 {@code long} 值，该位位于指定值的最低位 1 位的位置，或如果指定的值本身等于零，则返回零。
 * @since 1.5
 */
public static long lowestOneBit(long i) {
    // HD, Section 2-1
    return i & -i;
}

/**
 * 返回指定的 {@code long} 值的二进制补码表示中最高位（“最左边”）1 位之前的零位数。如果指定的值在其二进制补码表示中没有 1 位，
 * 即等于零，则返回 64。
 *
 * <p>注意，此方法与以 2 为底的对数密切相关。对于所有正的 {@code long} 值 x：
 * <ul>
 * <li>floor(log<sub>2</sub>(x)) = {@code 63 - numberOfLeadingZeros(x)}
 * <li>ceil(log<sub>2</sub>(x)) = {@code 64 - numberOfLeadingZeros(x - 1)}
 * </ul>
 *
 * @param i 要计算前导零数的值
 * @return 指定的 {@code long} 值的二进制补码表示中最高位（“最左边”）1 位之前的零位数，或如果值等于零，则返回 64。
 * @since 1.5
 */
public static int numberOfLeadingZeros(long i) {
    // HD, Figure 5-6
     if (i == 0)
        return 64;
    int n = 1;
    int x = (int)(i >>> 32);
    if (x == 0) { n += 32; x = (int)i; }
    if (x >>> 16 == 0) { n += 16; x <<= 16; }
    if (x >>> 24 == 0) { n +=  8; x <<=  8; }
    if (x >>> 28 == 0) { n +=  4; x <<=  4; }
    if (x >>> 30 == 0) { n +=  2; x <<=  2; }
    n -= x >>> 31;
    return n;
}

/**
 * 返回指定的 {@code long} 值的二进制补码表示中最低位（“最右边”）1 位之后的零位数。如果指定的值在其二进制补码表示中没有 1 位，
 * 即等于零，则返回 64。
 *
 * @param i 要计算尾随零数的值
 * @return 指定的 {@code long} 值的二进制补码表示中最低位（“最右边”）1 位之后的零位数，或如果值等于零，则返回 64。
 * @since 1.5
 */
public static int numberOfTrailingZeros(long i) {
    // HD, Figure 5-14
    int x, y;
    if (i == 0) return 64;
    int n = 63;
    y = (int)i; if (y != 0) { n = n -32; x = y; } else x = (int)(i>>>32);
    y = x <<16; if (y != 0) { n = n -16; x = y; }
    y = x << 8; if (y != 0) { n = n - 8; x = y; }
    y = x << 4; if (y != 0) { n = n - 4; x = y; }
    y = x << 2; if (y != 0) { n = n - 2; x = y; }
    return n - ((x << 1) >>> 31);
}

/**
 * 返回指定的 {@code long} 值的二进制补码表示中的 1 位数。此函数有时被称为 <i>人口计数</i>。
 *
 * @param i 要计数位的值
 * @return 指定的 {@code long} 值的二进制补码表示中的 1 位数。
 * @since 1.5
 */
public static int bitCount(long i) {
    // HD, Figure 5-14
    i = i - ((i >>> 1) & 0x5555555555555555L);
    i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
    i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
    i = i + (i >>> 8);
    i = i + (i >>> 16);
    i = i + (i >>> 32);
    return (int)i & 0x7f;
}

/**
 * 返回将指定的 {@code long} 值的二进制补码表示左移指定的位数得到的值。（从左端或高位移出的位重新进入右端或低位。）
 *
 * <p>注意，左移负距离等同于右移：{@code rotateLeft(val, -distance) == rotateRight(val,
 * distance)}。注意，任何 64 的倍数的旋转都是无操作，因此可以忽略旋转距离的最后六位，即使距离是负数：{@code rotateLeft(val,
 * distance) == rotateLeft(val, distance & 0x3F)}。
 *
 * @param i 要左移位的值
 * @param distance 要左移的位数
 * @return 将指定的 {@code long} 值的二进制补码表示左移指定的位数得到的值。
 * @since 1.5
 */
public static long rotateLeft(long i, int distance) {
    return (i << distance) | (i >>> -distance);
}

/**
 * 返回将指定的 {@code long} 值的二进制补码表示右移指定的位数得到的值。（从右端或低位移出的位重新进入左端或高位。）
 *
 * <p>注意，右移负距离等同于左移：{@code rotateRight(val, -distance) == rotateLeft(val,
 * distance)}。注意，任何 64 的倍数的旋转都是无操作，因此可以忽略旋转距离的最后六位，即使距离是负数：{@code rotateRight(val,
 * distance) == rotateRight(val, distance & 0x3F)}。
 *
 * @param i 要右移位的值
 * @param distance 要右移的位数
 * @return 将指定的 {@code long} 值的二进制补码表示右移指定的位数得到的值。
 * @since 1.5
 */
public static long rotateRight(long i, int distance) {
    return (i >>> distance) | (i << -distance);
}

/**
 * 返回将指定的 {@code long} 值的二进制补码表示中的位顺序反转得到的值。
 *
 * @param i 要反转的值
 * @return 将指定的 {@code long} 值的位顺序反转得到的值。
 * @since 1.5
 */
public static long reverse(long i) {
    // HD, Figure 7-1
    i = (i & 0x5555555555555555L) << 1 | (i >>> 1) & 0x5555555555555555L;
    i = (i & 0x3333333333333333L) << 2 | (i >>> 2) & 0x3333333333333333L;
    i = (i & 0x0f0f0f0f0f0f0f0fL) << 4 | (i >>> 4) & 0x0f0f0f0f0f0f0f0fL;
    i = (i & 0x00ff00ff00ff00ffL) << 8 | (i >>> 8) & 0x00ff00ff00ff00ffL;
    i = (i << 48) | ((i & 0xffff0000L) << 16) |
        ((i >>> 16) & 0xffff0000L) | (i >>> 48);
    return i;
}

/**
 * 返回指定的 {@code long} 值的符号函数。（如果指定的值为负，则返回值为 -1；如果指定的值为零，则返回值为 0；
 * 如果指定的值为正，则返回值为 1。）
 *
 * @param i 要计算符号函数的值
 * @return 指定的 {@code long} 值的符号函数。
 * @since 1.5
 */
public static int signum(long i) {
    // HD, Section 2-7
    return (int) ((i >> 63) | (-i >>> 63));
}

/**
 * 返回将指定的 {@code long} 值的二进制补码表示中的字节顺序反转得到的值。
 *
 * @param i 要反转字节的值
 * @return 将指定的 {@code long} 值的字节顺序反转得到的值。
 * @since 1.5
 */
public static long reverseBytes(long i) {
    i = (i & 0x00ff00ff00ff00ffL) << 8 | (i >>> 8) & 0x00ff00ff00ff00ffL;
    return (i << 48) | ((i & 0xffff0000L) << 16) |
        ((i >>> 16) & 0xffff0000L) | (i >>> 48);
}

/**
 * 按照 + 运算符将两个 {@code long} 值相加。
 *
 * @param a 第一个操作数
 * @param b 第二个操作数
 * @return {@code a} 和 {@code b} 的和
 * @see java.util.function.BinaryOperator
 * @since 1.8
 */
public static long sum(long a, long b) {
    return a + b;
}

/**
 * 返回两个 {@code long} 值中较大的一个，如同调用 {@link Math#max(long, long) Math.max} 一样。
 *
 * @param a 第一个操作数
 * @param b 第二个操作数
 * @return 较大的 {@code a} 和 {@code b}
 * @see java.util.function.BinaryOperator
 * @since 1.8
 */
public static long max(long a, long b) {
    return Math.max(a, b);
}

/**
 * 返回两个 {@code long} 值中较小的一个，如同调用 {@link Math#min(long, long) Math.min} 一样。
 *
 * @param a 第一个操作数
 * @param b 第二个操作数
 * @return 较小的 {@code a} 和 {@code b}
 * @see java.util.function.BinaryOperator
 * @since 1.8
 */
public static long min(long a, long b) {
    return Math.min(a, b);
}

/** 使用 JDK 1.0.2 的 serialVersionUID 以确保互操作性 */
@Native private static final long serialVersionUID = 4290774380558885855L;
}
