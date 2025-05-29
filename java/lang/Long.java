/*
 * 版权所有 (c) 1994, 2024, Oracle 和/或其关联公司。保留所有权利。
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

package java.lang;

import java.lang.annotation.Long;
import java.math.BigInteger;

/**
 * {@code Long} 类将基本类型 {@code long} 的值包装在一个对象中。{@code Long} 类型的对象包含一个类型为 {@code long} 的单一字段。
 *
 * <p>此外，此提供了多种方法，用于将 {@code long} 转换为 {@code String} 以及将 {@code String} 转换为 {@code long}，
 * 以及处理 {@code long} 时有用的其他常量和方法。
 *
 * <p>实现说明：位操作方法的实现（如 {@link #highestOneBit(long) highestOneBit} 和
 * {@link #numberOfTrailingZeros(long) numberOfTrailingZeros}）基于Henry S. Warren, Jr.的《黑客的乐趣》（Addison Wesley, 2002）。
 *
 * @作者 Lee Boynton
 * @作者 Arthur van Hoff
 * @作者 Josh Bloch
 * @作者 Joseph D. Darcy
 * @since JDK1.0
 */
public final class Long extends Number implements Comparable<Long> {
    /**
     * 一个包含 {@code long} 可以拥有的最小值的常量，值为 -2<sup>63</sup>。
     */
    @Native public static final long MIN_VALUE = 0x8000000000000000L;

    /**
     * 一个包含 {@code long} 可以拥有的最大值的常量，值为 2<sup>63</sup>-1。
     */
    @Native public static final long MAX_VALUE = 0x7fffffffffffffffL;

    /**
     * 表示基本类型 {@code long} 的 {@code Class} 实例。
     *
     * @since JDK1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Long> TYPE = (Class<Long>) Class.getPrimitiveClass("long");

    /**
     * 返回以指定第二参数的基数表示的第一个参数的字符串表示形式。
     *
     * <p>如果基数小于 {@code Character.MIN_RADIX} 或大于 {@code Character.MAX_RADIX}，则改用基数 {@code 10}。
     *
     * <p>如果第一个参数为负，结果的第一个元素是 ASCII 减号 {@code '-'} ({@code '\u002D'})。
     * 如果第一个参数不为负，则结果中不出现符号字符。
     *
     * <p>结果的其余字符表示第一个参数的幅度。如果幅度为零，则用单个零字符 {@code '0'} ({@code '\u0030'}) 表示；
     * 否则，幅度表示的第一个字符不会是零字符。以下 ASCII 字符用作数字：
     *
     * <blockquote>
     *   {@code 0123456789abcdefghijklmnopqrstuvwxyz}
     * </blockquote>
     *
     * 这些是 {@code '\u0030'} 到 {@code '\u0039'} 以及 {@code '\u0061'} 到 {@code '\u007A'}。
     * 如果 {@code radix} 是 <var>N</var>，则这些字符中的前 <var>N</var> 个按所示顺序用作基数-<var>N</var> 数字。
     * 因此，十六进制（基数 16）的数字是 {@code 0123456789abcdef}。如果需要大写字母，
     * 可以在结果上调用 {@link java.lang.String#toUpperCase()} 方法：
     *
     * <blockquote>
     *  {@code Long.toString(n, 16).toUpperCase()}
     * </blockquote>
     *
     * @param i 要转换为字符串的 {@code long}。
     * @param radix 字符串表示形式中使用的基数。
     * @return 指定基数的参数的字符串表示形式。
     * @see java.lang.Character#MAX_RADIX
     * @see java.lang.Character#MIN_RADIX
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
     * 返回以指定基数的无符号整数值的第一个参数的字符串表示形式。
     *
     * <p>如果基数小于 {@code Character.MIN_RADIX} 或大于 {@code Character.MAX_RADIX}，则改用基数 {@code 10}。
     *
     * <p>请注意，由于第一个参数被视为无符号值，因此不会打印前导符号字符。
     *
     * <p>如果幅度为零，则用单个零字符 {@code '0'} ({@code '\u0030'}) 表示；
     * 否则，幅度表示的第一个字符不会是零字符。
     *
     * <p>基数和用作数字的字符的行为与 {@link #toString(long, int) toString} 相同。
     *
     * @param i 要转换为无符号字符串的整数。
     * @param radix 字符串表示形式中使用的基数。
     * @return 指定基数的参数的无符号字符串表示形式。
     * @see #toString(long, int)
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
                 * 我们可以通过先右移得到一个正值，然后除以 5，
                 * 来实现对 long 值的无符号除以 10 的效果。
                 * 这允许比通过初始转换为 BigInteger 更快地分离出最后一位和前面的数字。
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
     * 返回等于参数的无符号值的 BigInteger。
     */
    private static BigInteger toUnsignedBigInteger(long i) {
        if (i >= 0L)
            return BigInteger.valueOf(i);
        else {
            int upper = (int) (i >>> 32);
            int lower = (int) i;

            // 返回 (upper << 32) + lower
            return (BigInteger.valueOf(Integer.toUnsignedLong(upper))).shiftLeft(32).
                add(BigInteger.valueOf(Integer.toUnsignedLong(lower)));
        }
    }

    /**
     * 返回以十六进制（基数 16）表示的 {@code long} 参数的无符号整数的字符串表示形式。
     *
     * <p>如果参数为负，无符号 {@code long} 值是参数加上 2<sup>64</sup>；
     * 否则，它等于参数。该值转换为十六进制（基数 16）的 ASCII 数字字符串，没有额外的前导 {@code 0}。
     *
     * <p>可以通过调用 {@link Long#parseUnsignedLong(String, int) Long.parseUnsignedLong(s, 16)}
     * 从返回的字符串 {@code s} 恢复参数的值。
     *
     * <p>如果无符号幅度为零，则用单个零字符 {@code '0'} ({@code '\u0030'}) 表示；
     * 否则，无符号幅度表示的第一个字符不会是零字符。以下字符用作十六进制数字：
     *
     * <blockquote>
     *  {@code 0123456789abcdef}
     * </blockquote>
     *
     * 这些是字符 {@code '\u0030'} 到 {@code '\u0039'} 以及 {@code '\u0061'} 到 {@code '\u0066'}。
     * 如果需要大写字母，可以在结果上调用 {@link java.lang.String#toUpperCase()} 方法：
     *
     * <blockquote>
     *  {@code Long.toHexString(n).toUpperCase()}
     * </blockquote>
     *
     * @param i 要转换为字符串的 {@code long}。
     * @return 以十六进制（基数 16）表示的参数的无符号 {@code long} 值的字符串表示形式。
     * @see #parseUnsignedLong(String, int)
     * @see #toUnsignedString(long, int)
     * @since JDK 1.0.2
     */
    public static String toHexString(long i) {
        return toUnsignedString0(i, 4);
    }

    /**
     * 返回以八进制（基数 8）表示的 {@code long} 参数的无符号整数的字符串表示形式。
     *
     * <p>如果参数为负，无符号 {@code long} 值是参数加上 2<sup>64</sup>；
     * 否则，它等于参数。该值转换为八进制（基数 8）的 ASCII 数字字符串，没有额外的前导 {@code 0}。
     *
     * <p>可以通过调用 {@link Long#parseUnsignedLong(String, int) Long.parseUnsignedLong(s, 8)}
     * 从返回的字符串 {@code s} 恢复参数的值。
     *
     * <p>如果无符号幅度为零，则用单个零字符 {@code '0'} ({@code '\u0030'}) 表示；
     * 否则，无符号幅度表示的第一个字符不会是零字符。以下字符用作八进制数字：
     *
     * <blockquote>
     *  {@code 01234567}
     * </blockquote>
     *
     * 这些是字符 {@code '\u0030'} 到 {@code '\u0037'}。
     *
     * @param i 要转换为字符串的 {@code long}。
     * @return 以八进制（基数 8）表示的参数的无符号 {@code long} 值的字符串表示形式。
     * @see #parseUnsignedLong(String, int)
     * @see #toUnsignedString(long, int)
     * @since JDK 1.0.2
     */
    public static String toOctalString(long i) {
        return toUnsignedString0(i, 3);
    }

    /**
     * 返回以二进制（基数 2）表示的 {@code long} 参数的无符号整数的字符串表示形式。
     *
     * <p>如果参数为负，无符号 {@code long} 值是参数加上 2<sup>64</sup>；
     * 否则，它等于参数。该值转换为二进制（基数 2）的 ASCII 数字字符串，没有额外的前导 {@code 0}。
     *
     * <p>可以通过调用 {@link Long#parseUnsignedLong(String, int) Long.parseUnsignedLong(s, 2)}
     * 从返回的字符串 {@code s} 恢复参数的值。
     *
     * <p>如果无符号幅度为零，则用单个零字符 {@code '0'} ({@code '\u0030'}) 表示；
     * 否则，无符号幅度表示的第一个字符不会是零字符。使用字符 {@code '0'} ({@code '\u0030'})
     * 和 {@code '1'} ({@code '\u0031'}) 作为二进制数字。
     *
     * @param i 要转换为字符串的 {@code long}。
     * @return 以二进制（基数 2）表示的参数的无符号 {@code long} 值的字符串表示形式。
     * @see #parseUnsignedLong(String, int)
     * @see #toUnsignedString(long, int)
     * @since JDK 1.0.2
     */
    public static String toBinaryString(long i) {
        return toUnsignedString0(i, 1);
    }

    /**
     * 将一个 long（视为无符号）格式化为字符串。
     * @param val 要格式化的值
     * @param shift 格式化的基数的对数2（十六进制为4，八进制为3，二进制为1）
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
     * 将一个 long（视为无符号）格式化为字符缓冲区。
     * @param val 要格式化的无符号 long
     * @param shift 格式化的基数的对数2（十六进制为4，八进制为3，二进制为1）
     * @param buf 要写入的字符缓冲区
     * @param offset 目标缓冲区的起始偏移量
     * @param len 要写入的字符数
     * @return 使用的字符位置的最低位置
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
     * 返回表示指定 {@code long} 的 {@code String} 对象。参数转换为有符号十进制表示形式，
     * 并作为字符串返回，效果与将参数和基数 10 作为 {@link #toString(long, int)} 方法的参数完全相同。
     *
     * @param i 要转换的 {@code long}。
     * @return 基数 10 的参数的字符串表示形式。
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
     * 返回以无符号十进制值表示的参数的字符串表示形式。
     *
     * 参数转换为无符号十进制表示形式，并作为字符串返回，效果与将参数和基数 10 作为
     * {@link #toUnsignedString(long, int)} 方法的参数完全相同。
     *
     * @param i 要转换为无符号字符串的整数。
     * @return 参数的无符号字符串表示形式。
     * @see #toUnsignedString(long, int)
     * @since 1.8
     */
    public static String toUnsignedString(long i) {
        return toUnsignedString(i, 10);
    }

    /**
     * 将表示整数 i 的字符放入字符数组 buf 中。字符从指定的索引（独占）开始向后放置，
     * 从最低有效数字开始，并从那里向后工作。
     *
     * 如果 i == Long.MIN_VALUE 将失败
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

        // 使用 long 每次迭代获取 2 位数字，直到商适合 int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // 实际上：r = i - (q * 100);
            r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = Integer.DigitOnes[r];
            buf[--charPos] = Integer.DigitTens[r];
        }

        // 使用 int 每次迭代获取 2 位数字
        int q2;
        int i2 = (int)i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // 实际上：r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = Integer.DigitOnes[r];
            buf[--charPos] = Integer.DigitTens[r];
        }

        // 对于较小的数字进入快速模式
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
     * 将字符串参数解析为指定基数的有符号 {@code long}。字符串中的字符必须全部是指定基数的数字
     * （由 {@link java.lang.Character#digit(char, int)} 是否返回非负值决定），
     * 除了第一个字符可以是 ASCII 减号 {@code '-'} ({@code '\u002D'}) 表示负值，
     * 或 ASCII 加号 {@code '+'} ({@code '\u002B'}) 表示正值。返回结果的 {@code long} 值。
     *
     * <p>请注意，字符串末尾不允许出现字符 {@code L} ({@code '\u004C'}) 或 {@code l} ({@code '\u006C'})
     * 作为类型指示符，如 Java 编程语言源代码中允许的那样——除非基数大于或等于 22 时，
     * {@code L} 或 {@code l} 可以作为数字出现。
     *
     * <p>如果发生以下任一情况，将抛出 {@code NumberFormatException} 类型的异常：
     * <ul>
     *
     * <li>第一个参数为 {@code null} 或长度为零的字符串。
     *
     * <li>{@code radix} 小于 {@link java.lang.Character#MIN_RADIX} 或大于 {@link java.lang.Character#MAX_RADIX}。
     *
     * <li>字符串的任何字符不是指定基数的数字，除了第一个字符可以是减号 {@code '-'} ({@code '\u002D'})
     * 或加号 {@code '+'} ({@code '\u002B'})，前提是字符串长度大于 1。
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
     * @param s 包含要解析的 {@code long} 表示形式的 {@code String}。
     * @param radix 解析 {@code s} 时使用的基数。
     * @return 以指定基数表示的字符串参数的 {@code long}。
     * @throws NumberFormatException 如果字符串不包含可解析的 {@code long}。
     */
    public static long parseLong(String s, int radix)
              throws NumberFormatException
    {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("基数 " + radix +
                                            " 小于 Character.MIN_RADIX");
        }
        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("基数 " + radix +
                                            " 大于 Character.MAX_RADIX");
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

                if (len == 1) // 不能只有单独的 "+" 或 "-"
                    throw NumberFormatException.forInputString(s);
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // 负累积避免接近 MAX_VALUE 时的意外
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
     * 将字符串参数解析为有符号十进制 {@code long}。字符串中的字符必须全部是十进制数字，
     * 除了第一个字符可以是 ASCII 减号 {@code '-'} ({@code '\u002D'}) 表示负值，
     * 或 ASCII 加号 {@code '+'} ({@code '\u002B'}) 表示正值。返回结果的 {@code long} 值，
     * 效果与将参数和基数 {@code 10} 作为 {@link #parseLong(java.lang.String, int)} 方法的参数完全相同。
     *
     * <p>请注意，字符串末尾不允许出现字符 {@code L} ({@code '\u004C'}) 或 {@code l} ({@code '\u006C'})
     * 作为类型指示符，如 Java 编程语言源代码中允许的那样。
     *
     * @param s 包含要解析的 {@code long} 表示形式的 {@code String}
     * @return 以十进制表示的参数的 {@code long}。
     * @throws NumberFormatException 如果字符串不包含可解析的 {@code long}。
     */
    public static long parseLong(String s) throws NumberFormatException {
        return parseLong(s, 10);
    }

    /**
     * 将字符串参数解析为指定基数的无符号 {@code long}。无符号整数将通常与负数关联的值映射到大于 {@code MAX_VALUE} 的正数。
     *
     * 字符串中的字符必须全部是指定基数的数字（由 {@link java.lang.Character#digit(char, int)} 是否返回非负值决定），
     * 除了第一个字符可以是 ASCII 加号 {@code '+'} ({@code '\u002B'})。返回结果的整数值。
     *
     * <p>如果发生以下任一情况，将抛出 {@code NumberFormatException} 类型的异常：
     * <ul>
     * <li>第一个参数为 {@code null} 或长度为零的字符串。
     *
     * <li>基数小于 {@link java.lang.Character#MIN_RADIX} 或大于 {@link java.lang.Character#MAX_RADIX}。
     *
     * <li>字符串的任何字符不是指定基数的数字，除了第一个字符可以是加号 {@code '+'} ({@code '\u002B'})，
     * 前提是字符串长度大于 1。
     *
     * <li>字符串表示的值大于最大的无符号 {@code long}，即 2<sup>64</sup>-1。
     *
     * </ul>
     *
     *
     * @param s 包含要解析的无符号整数表示形式的 {@code String}
     * @param radix 解析 {@code s} 时使用的基数。
     * @return 以指定基数表示的字符串参数的无符号 {@code long}。
     * @throws NumberFormatException 如果 {@code String} 不包含可解析的 {@code long}。
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
                    NumberFormatException(String.format("无符号字符串 %s 上存在非法的负号前缀。", s));
            } else {
                if (len <= 12 || // Long.MAX_VALUE 在 Character.MAX_RADIX 中是 13 位
                    (radix == 10 && len <= 18) ) { // Long.MAX_VALUE 在基数 10 中是 19 位
                    return parseLong(s, radix);
                }

                // 由于上述测试，无需对 len 进行范围检查。
                long first = parseLong(s.substring(0, len - 1), radix);
                int second = Character.digit(s.charAt(len - 1), radix);
                if (second < 0) {
                    throw new NumberFormatException("字符串末尾的数字无效：" + s);
                }
                long result = first * radix + second;
                if (compareUnsigned(result, first) < 0) {
                    /*
                     * 最大无符号值 (2^64)-1 比最大有符号值 (2^63)-1 多至多一位来表示。
                     * 因此，解析 (len - 1) 位数字将适当地在有符号解析范围内。
                     * 换句话说，如果解析 (len -1) 位数字溢出有符号解析，
                     * 则解析 len 位数字肯定会溢出无符号解析。
                     *
                     * 上面的 compareUnsigned 检查捕获了包含最终数字贡献的无符号溢出情况。
                     */
                    throw new NumberFormatException(String.format("字符串值 %s 超出无符号 long 的范围。", s));
                }
                return result;
            }
        } else {
            throw NumberFormatException.forInputString(s);
        }
    }

    /**
     * 将字符串参数解析为无符号十进制 {@code long}。字符串中的字符必须全部是十进制数字，
     * 除了第一个字符可以是 ASCII 加号 {@code '+'} ({@code '\u002B'})。
     * 返回结果的整数值，效果与将参数和基数 10 作为 {@link #parseUnsignedLong(java.lang.String, int)} 方法的参数完全相同。
     *
     * @param s 包含要解析的无符号 {@code long} 表示形式的 {@code String}
     * @return 以十进制字符串参数表示的无符号 {@code long} 值
     * @throws NumberFormatException 如果字符串不包含可解析的无符号整数。
     * @since 1.8
     */
    public static long parseUnsignedLong(String s) throws NumberFormatException {
        return parseUnsignedLong(s, 10);
    }

    /**
     * 返回一个 {@code Long} 对象，包含从指定 {@code String} 中提取的值，
     * 该值使用第二个参数给定的基数进行解析。第一个参数被解释为表示指定基数的有符号 {@code long}，
     * 效果与将参数传递给 {@link #parseLong(java.lang.String, int)} 方法完全相同。
     * 结果是一个表示字符串指定的 {@code long} 值的 {@code Long} 对象。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Long} 对象：
     *
     * <blockquote>
     *  {@code new Long(Long.parseLong(s, radix))}
     * </blockquote>
     *
     * @param s 要解析的字符串
     * @param radix 解释 {@code s} 时使用的基数
     * @return 包含以指定基数表示的字符串参数的值的 {@code Long} 对象。
     * @throws NumberFormatException 如果 {@code String} 不包含可解析的 {@code long}。
     */
    public static Long valueOf(String s, int radix) throws NumberFormatException {
        return Long.valueOf(parseLong(s, radix));
    }

    /**
     * 返回一个 {@code Long} 表示指定值的 {@code String} 的对象。
     * 参数被解释为表示有符号十进制 {@code long}，效果与将参数传递给 {@link #parseLong(java.lang.String)}
     * 方法完全相同。结果是一个表示字符串指定的整数值的 {@code Long} 对象。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Long} 对象：
     *
     * <blockquote>
     *  {@code new Long(Long.parseLong(s))}
     * </blockquote>
     *
     * @param s 要解析的字符串。
     * @return 包含表示字符串的值的 {@code Long} 对象。
     * @throws NumberFormatException 如果字符串无法解析为 {@code long}。
     */
    public static Long valueOf(String s) throws NumberFormatException
    {
        return Long.valueOf(parseLong(s));
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
     * 返回表示指定 {@code long} 值的 {@code Long} 实例。
     * 如果不需要新的 {@code Long} 实例，通常应优先使用此方法而不是构造函数 {@code #Long(long)}，
     * 因为此方法通过缓存频繁请求的值可能显著提高空间和时间性能。
     *
     * 请注意，与 {@code Integer} 类中的 {@link Integer#valueOf(int)} 方法不同，
     * 此方法不要求在特定范围内缓存值。
     *
     * @param l long 值。
     * @return 表示 {@code l} 的 {@code Long} 实例。
     * @since 1.5
     */
    public static Long valueOf(long l) {
        final int offset = 128;
        if (l >= -128 && l <= 127) { // 将缓存
            return LongCache.cache[(int)l + offset];
        }
        return new Long(l);
    }

    /**
     * 将 {@code String} 解码为 {@code Long}。
     * 接受以下语法的十进制、十六进制和八进制数字：
     *
     * <p>
     * <pre>
     * <dl>
     * <dt><i>可解码字符串：
     </i></dt>
     * <dd><i>可选符号 十进制数字</i>
     * <dd><i>可选符号</i> <code> 0x </code> <i>十六进制数字</i>
     * <dd><i>可选符号</i> <code> 0X </code> <i>十六进制数字</i>
     * <dd><i>可选符号</i> <code> # </code> <i>十六进制数字</i>
     * <dd><i>可选符号</i> <code> 0 </code> <i>八进制数字</i>
     *
     * <dt><i>符号：
     </i>
     * <dd><code>-</code>
     * <dd><code> +</code>
     * </dl>
     * </pre>
     *
     * <i>DecimalNumeral、</i> <i>HexDigits、</i> 和 <i>OctalDigits</i>
     * 如《Java™语言规范》第3.10.1节定义，
     除了不支持数字之间的下划线。
     *
     * <p>可选符号和/或基数指示符（"<code>0x</code>", "<code>0X</code>", "<code>#</code>", 或前导零）后的字符序列
     * 按照指定的基数（10、16 或 8）由 {@code Long.parseLong 方法解析。}
     * 该字符序列必须表示一个正值，否则将抛出 {@link NumberFormatException}。
     * 如果指定的字符串的第一个字符是负号，则结果取反。
     * {@code String} 中不允许出现空白字符。
     *
     * @param nm 要解码的 {@code String}。
     * @return 包含由 {@code nm} 表示的 {@code long} 值的 {@code Long} 对象
     * @throws NumberFormatException 如果 {@code String} 不包含可解析的 {@code long}。
     * @see #parseLong(String, int)
     * @since 1.2
     */
    public static Long decode(String nm) throws NumberFormatException {
        int radix = 10;
        int index = 0;
        boolean negative = false;
        Long result;

        if (nm.isEmpty())
            throw new NumberFormatException("零长度字符串");
        char firstChar = nm.charAt(0);
        // 处理符号（如果存在）
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+')
            index++;

        // 处理基数指定符（如果存在）
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
            throw new NumberFormatException("符号字符位置错误");

        try {
            result = Long.valueOf(nm.substring(index), radix);
            result = negative ? Long.valueOf(-result.longValue()) : result;
        } catch (NumberFormatException e) {
            // 如果数字是 Long.MIN_VALUE，我们会在这里结束。下一行
            // 处理这种情况，并导致任何真正的格式错误被重新抛出。
            String constant = negative ? ("-" + nm.substring(index))
                                       : nm.substring(index);
            result = Long.valueOf(constant, radix);
        }
        return result;
    }

    /**
     * {@code Long} 的值。
     *
     * @serial
     */
    private final long value;

    /**
     * 构造一个新分配的 {@code Long} 对象，表示指定的 {@code long} 参数。
     *
     * @param value 由 {@code Long} 对象表示的值。
     */
    public Long(long value) {
        this.value = value;
    }

    /**
     * 构造一个新分配的 {@code Long} 对象，表示由 {@code String} 参数指示的 {@code long} 值。
     * 字符串按照基数 10 的 {@code parseLong} 方法完全相同的方式转换为 {@code long} 值。
     *
     * @param s 要转换为 {@code Long} 的 {@code String}。
     * @throws NumberFormatException 如果 {@code String} 不包含可解析的 {@code long}。
     * @see java.lang.Long#parseLong(java.lang.String, int)
     */
    public Long(String s) throws NumberFormatException {
        this.value = parseLong(s, 10);
    }

    /**
     * 在窄化基本类型转换后，返回此 {@code Long} 的值作为 {@code byte}。
     * @jls 5.1.3 窄化基本类型转换
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * 在窄化基本类型转换后，返回此 {@code Long} 的值作为 {@code short}。
     * @jls 5.1.3 窄化基本类型转换
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * 在窄化基本类型转换后，返回此 {@code Long} 的值作为 {@code int}。
     * @jls 5.1.3 窄化基本类型转换
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
     * 在拓宽基本类型转换后，返回此 {@code Long} 的值作为 {@code float}。
     * @jls 5.1.2 拓宽基本类型转换
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * 在拓宽基本类型转换后，返回此 {@code Long} 的值作为 {@code double}。
     * @jls 5.1.2 拓宽基本类型转换
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * 返回表示此 {@code Long} 值的 {@code String} 对象。值转换为有符号十进制表示形式，
     * 并作为字符串返回，效果与将 {@code long} 值作为 {@link java.lang.Long#toString(long)} 方法的参数完全相同。
     *
     * @return 以基数 10 表示的此对象值的字符串表示形式。
     */
    public String toString() {
        return toString(value);
    }

    /**
     * 返回此 {@code Long} 的哈希码。结果是此 {@code Long} 对象持有的基本 {@code long} 值
     * 的两半的异或运算。即，哈希码是以下表达式的值：
     *
     * <blockquote>
     *  {@code (int)(this.longValue()^(this.longValue()>>>32))}
     * </blockquote>
     *
     * @return 此对象的哈希码值。
     */
    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    /**
     * 为 {@code long} 值返回哈希码；与 {@code Long.hashCode()} 兼容。
     *
     * @param value 要哈希的值
     * @return {@code long} 值的哈希码值。
     * @since 1.8
     */
    public static int hashCode(long value) {
        return (int)(value ^ (value >>> 32));
    }

    /**
     * 将此对象与指定对象比较。仅当参数不为 {@code null} 且是一个包含与此对象相同 {@code long} 值的
     * {@code Long} 对象时，结果为 {@code true}。
     *
     * @param obj 要比较的对象。
     * @return 如果对象相同则为 {@code true}；否则为 {@code false}。
     */
    public boolean equals(Object obj) {
        if (obj instanceof Long) {
            return value == ((Long)obj).longValue();
        }
        return false;
    }

    /**
     * 确定具有指定名称的系统属性的 {@code long} 值。
     *
     * <p>第一个参数被视为系统属性的名称。可以通过 {@link java.lang.System#getProperty(java.lang.String)}
     * 方法访问系统属性。然后使用 {@link Long#decode decode} 支持的语法将此属性的字符串值解释为
     * {@code long} 值，并返回表示此值的 {@code Long} 对象。
     *
     * <p>如果没有指定名称的属性，如果指定名称为空或 {@code null}，或者如果属性没有正确的数字格式，
     * 则返回 {@code null}。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Long} 对象：
     *
     * <blockquote>
     *  {@code getLong(nm, null)}
     * </blockquote>
     *
     * @param nm 属性名称。
     * @return 属性的 {@code Long} 值。
     * @throws SecurityException 原因与 {@link System#getProperty(String) System.getProperty} 相同
     * @see java.lang.System#getProperty(java.lang.String)
     * @see java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static Long getLong(String nm) {
        return getLong(nm, null);
    }

    /**
     * 确定具有指定名称的系统属性的 {@code long} 值。
     *
     * <p>第一个参数被视为系统属性的名称。可以通过 {@link java.lang.System#getProperty(java.lang.String)}
     * 方法访问系统属性。然后使用 {@link Long#decode decode} 支持的语法将此属性的字符串值解释为
     * {@code long} 值，并返回表示此值的 {@code Long} 对象。
     *
     * <p>第二个参数是默认值。如果没有指定名称的属性，如果属性没有正确的数字格式，
     * 或者如果指定名称为空或 null，则返回表示第二个参数值的 {@code Long} 对象。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Long} 对象：
     *
     * <blockquote>
     *  {@code getLong(nm, new Long(val))}
     * </blockquote>
     *
     * 但实际上，它可能以如下方式实现：
     *
     * <blockquote><pre>
     * Long result = getLong(nm, null);
     * return (result == null) ? new Long(val) : result;
     * </pre></blockquote>
     *
     * 以避免在不需要默认值时不必要地分配 {@code Long} 对象。
     *
     * @param nm 属性名称。
     * @param val 默认值。
     * @return 属性的 {@code Long} 值。
     * @throws SecurityException 原因与 {@link System#getProperty(String) System.getProperty} 相同
     * @see java.lang.System#getProperty(java.lang.String)
     * @see java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static Long getLong(String nm, long val) {
        Long result = Long.getLong(nm, null);
        return (result == null) ? Long.valueOf(val) : result;
    }

    /**
     * 返回具有指定名称的系统属性的 {@code long} 值。第一个参数被视为系统属性的名称。
     * 可以通过 {@link java.lang.System#getProperty(java.lang.String)} 方法访问系统属性。
     * 然后按照 {@link Long#decode decode} 方法将此属性的字符串值解释为 {@code long} 值，
     * 并返回表示此值的 {@code Long} 对象；总结如下：
     *
     * <ul>
     * <li>如果属性值以两个 ASCII 字符 {@code 0x} 或 ASCII 字符 {@code #} 开头，且后面没有负号，
     * 则其余部分按照基数 16 的 {@link #valueOf(java.lang.String, int)} 方法解析为十六进制整数。
     * <li>如果属性值以 ASCII 字符 {@code 0} 开头，后跟另一个字符，则按照基数 8 的
     * {@link #valueOf(java.lang.String, int)} 方法解析为八进制整数。
     * <li>否则，属性值按照基数 10 的 {@link #valueOf(java.lang.String, int)} 方法解析为十进制整数。
     * </ul>
     *
     * <p>请注意，在任何情况下，属性值末尾都不允许出现 {@code L} ({@code '\u004C'}) 或
     * {@code l} ({@code '\u006C'}) 作为类型指示符，如 Java 编程语言源代码中允许的那样。
     *
     * <p>第二个参数是默认值。如果没有指定名称的属性，如果属性没有正确的数字格式，
     * 或者如果指定名称为空或 {@code null}，则返回默认值。
     *
     * @param nm 属性名称。
     * @param val 默认值。
     * @return 属性的 {@code Long} 值。
     * @throws SecurityException 原因与 {@link System#getProperty(String) System.getProperty} 相同
     * @see System#getProperty(java.lang.String)
     * @see System#getProperty(java.lang.String, java.lang.String)
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
     * 在数值上比较两个 {@code Long} 对象。
     *
     * @param anotherLong 要比较的 {@code Long}。
     * @return 如果此 {@code Long} 等于参数 {@code Long}，则返回 {@code 0}；
     * 如果此 {@code Long} 在数值上小于参数 {@code Long}，则返回小于 {@code 0} 的值；
     * 如果此 {@code Long} 在数值上大于参数 {@code Long}，则返回大于 {@code 0} 的值（有符号比较）。
     * @since 1.2
     */
    public int compareTo(Long anotherLong) {
        return compare(this.value, anotherLong.value);
    }

    /**
     * 在数值上比较两个 {@code long} 值。
     * 返回的值与以下内容相同：
     * <pre>
     *    Long.valueOf(x).compareTo(Long.valueOf(y))
     * </pre>
     *
     * @param x 要比较的第一个 {@code long}
     * @param y 要比较的第二个 {@code long}
     * @return 如果 {@code x == y}，则返回 {@code 0}；
     * 如果 {@code x < y}，则返回小于 {@code 0} 的值；
     * 如果 {@code x > y}，则返回大于 {@code 0} 的值
     * @since 1.7
     */
    public static int compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    /**
     * 在数值上比较两个 {@code long} 值，将值视为无符号。
     *
     * @param x 要比较的第一个 {@code long}
     * @param y 要比较的第二个 {@code long}
     * @return 如果 {@code x == y}，则返回 {@code 0}；
     * 如果作为无符号值 {@code x < y}，则返回小于 {@code 0} 的值；
     * 如果作为无符号值 {@code x > y}，则返回大于 {@code 0} 的值
     * @since 1.8
     */
    public static int compareUnsigned(long x, long y) {
        return compare(x + MIN_VALUE, y + MIN_VALUE);
    }

    /**
     * 返回第一个参数除以第二个参数的无符号商，其中每个参数和结果都被解释为无符号值。
     *
     * <p>请注意，在二进制补码运算中，添加、减去和乘法的其他三种基本运算在操作数都被视为有符号或无符号时，
     * 按位操作是相同的。因此，没有提供单独的 {@code addUnsigned} 等方法。
     *
     * @param dividend 被除数
     * @param divisor 除数
     * @return 第一个参数除以第二个参数的无符号商
     * @see #remainderUnsigned
     * @since 1.8
     */
    public static long divideUnsigned(long dividend, long divisor) {
        if (divisor < 0L) { // 有符号比较
            // 根据被除数和除数的相对幅度，答案必须为 0 或 1。
            return (compareUnsigned(dividend, divisor)) < 0 ? 0L :1L;
        }

        if (dividend > 0) // 两个输入均为非负
            return dividend/divisor;
        else {
            /*
             * 为简单起见，利用 BigInteger。可以直接用 long 运算编写更长且更快的代码；
             * 参见《黑客的乐趣》中的除法和余数算法。
             */
            return toUnsignedBigInteger(dividend).
                divide(toUnsignedBigInteger(divisor)).longValue();
        }
    }

    /**
     * 返回第一个参数除以第二个参数的无符号余数，其中每个参数和结果都被解释为无符号值。
     *
     * @param dividend 被除数
     * @param divisor 除数
     * @return 第一个参数除以第二个参数的无符号余数
     * @see #divideUnsigned
     * @since 1.8
     */
    public static long remainderUnsigned(long dividend, long divisor) {
        if (dividend > 0 && divisor > 0) { // 有符号比较
            return dividend % divisor;
        } else {
            if (compareUnsigned(dividend, divisor) < 0) // 避免显式检查除数为 0
                return dividend;
            else
                return toUnsignedBigInteger(dividend).
                    remainder(toUnsignedBigInteger(divisor)).longValue();
        }
    }

    // 位操作

    /**
     * 以二进制补码形式表示 {@code long} 值的位数。
     *
     * @since 1.5
     */
    @Native public static final int SIZE = 64;

    /**
     * 以二进制补码形式表示 {@code long} 值的字节数。
     *
     * @since 1.8
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /**
     * 返回最多具有单个 1 位的 {@code long} 值，位于指定 {@code long} 值中最高位（“最左”）1 位的位置。
     * 如果指定值的二进制补码表示中没有 1 位（即等于零），则返回零。
     *
     * @param i 要计算最高 1 位的值
     * @return 在指定值中最高位 1 位的位置具有单个 1 位的 {@code long} 值，
     * 如果指定值本身等于零，则返回零。
     * @since 1.5
     */
    public static long highestOneBit(long i) {
        // HD, 图 3-1
        i |= (i >>  1);
        i |= (i >>  2);
        i |= (i >>  4);
        i |= (i >>  8);
        i |= (i >> 16);
        i |= (i >> 32);
        return i - (i >>> 1);
    }

    /**
     * 返回最多具有单个 1 位的 {@code long} 值，位于指定 {@code long} 值中最低位（“最右”）1 位的位置。
     * 如果指定值的二进制补码表示中没有 1 位（即等于零），则返回零。
     *
     * @param i 要计算最低 1 位的值
     * @return 在指定值中最低位 1 位的位置具有单个 1 位的 {@code long} 值，
     * 如果指定值本身等于零，则返回零。
     * @since 1.5
     */
    public static long lowestOneBit(long i) {
        // HD, 章节 2-1
        return i & -i;
    }

    /**
     * 返回指定 {@code long} 值的二进制补码表示中最高位（“最左”）1 位之前的零位数。
     * 如果指定值的二进制补码表示中没有 1 位（即等于零），则返回 64。
     *
     * <p>请注意，此方法与以 2 为底的对数密切相关。对于所有正 {@code long} 值 x：
     * <ul>
     * <li>floor(log<sub>2</sub>(x)) = {@code 63 - numberOfLeadingZeros(x)}
     * <li>ceil(log<sub>2</sub>(x)) = {@code 64 - numberOfLeadingZeros(x - 1)}
     * </ul>
     *
     * @param i 要计算前导零位数的值
     * @return 指定 {@code long} 值的二进制补码表示中最高位（“最左”）1 位之前的零位数，
     * 如果值为零，则返回 64。
     * @since 1.5
     */
    public static int numberOfLeadingZeros(long i) {
        // HD, 图 5-6
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
     * 返回指定 {@code long} 值的二进制补码表示中最低位（“最右”）1 位之后的零位数。
     * 如果指定值的二进制补码表示中没有 1 位（即等于零），则返回 64。
     *
     * @param i 要计算尾随零位数的值
     * @return 指定 {@code long} 值的二进制补码表示中最低位（“最右”）1 位之后的零位数，
     * 如果值为零，则返回 64。
     * @since 1.5
     */
    public static int numberOfTrailingZeros(long i) {
        // HD, 图 5-14
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
     * 返回指定 {@code long} 值的二进制补码表示中的 1 位数。此函数有时被称为<i>人口计数</i>。
     *
     * @param i 要计数位的值
     * @return 指定 {@code long} 值的二进制补码表示中的 1 位数。
     * @since 1.5
     */
     public static int bitCount(long i) {
        // HD, 图 5-14
        i = i - ((i >>> 1) & 0x5555555555555555L);
        i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
        i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
        i = i + (i >>> 8);
        i = i + (i >>> 16);
        i = i + (i >>> 32);
        return (int)i & 0x7f;
     }

    /**
     * 返回通过将指定 {@code long} 值的二进制补码表示向左旋转指定位数获得的值。
     * （从左侧或高位移出的位在右侧或低位重新进入。）
     *
     * <p>请注意，负距离的左旋转等同于右旋转：
     * {@code rotateLeft(val, -distance) == rotateRight(val, distance)}。
     * 另外，任何 64 的倍数的旋转都是空操作，因此可以忽略旋转距离的最后六位以外的所有位，
     * 即使距离为负：{@code rotateLeft(val, distance) == rotateLeft(val, distance & 0x3F)}。
     *
     * @param i 要向左旋转位的值
     * @param distance 向左旋转的位数
     * @return 通过将指定 {@code long} 值的二进制补码表示向左旋转指定位数获得的值。
     * @since 1.5
     */
    public static long rotateLeft(long i, int distance) {
        return (i << distance) | (i >>> -distance);
    }

    /**
     * 返回通过将指定 {@code long} 值的二进制补码表示向右旋转指定位数获得的值。
     * （从右侧或低位移出的位在左侧或高位重新进入。）
     *
     * <p>请注意，负距离的右旋转等同于左旋转：
     * {@code rotateRight(val, -distance) == rotateLeft(val, distance)}。
     * 另外，任何 64 的倍数的旋转都是空操作，因此可以忽略旋转距离的最后六位以外的所有位，
     * 即使距离为负：{@code rotateRight(val, distance) == rotateRight(val, distance & 0x3F)}。
     *
     * @param i 要向右旋转位的值
     * @param distance 向右旋转的位数
     * @return 通过将指定 {@code long} 值的二进制补码表示向右旋转指定位数获得的值。
     * @since 1.5
     */
    public static long rotateRight(long i, int distance) {
        return (i >>> distance) | (i << -distance);
    }

    /**
     * 返回通过反转指定 {@code long} 值的二进制补码表示中的位顺序获得的值。
     *
     * @param i 要反转的值
     * @return 通过反转指定 {@code long} 值中的位顺序获得的值。
     * @since 1.5
     */
    public static long reverse(long i) {
        // HD, 图 7-1
        i = (i & 0x5555555555555555L) << 1 | (i >>> 1) & 0x5555555555555555L;
        i = (i & 0x3333333333333333L) << 2 | (i >>> 2) & 0x3333333333333333L;
        i = (i & 0x0f0f0f0f0f0f0f0fL) << 4 | (i >>> 4) & 0x0f0f0f0f0f0f0f0fL;
        i = (i & 0x00ff00ff00ff00ffL) << 8 | (i >>> 8) & 0x00ff00ff00ff00ffL;
        i = (i << 48) | ((i & 0xffff0000L) << 16) |
            ((i >>> 16) & 0xffff0000L) | (i >>> 48);
        return i;
    }

    /**
     * 返回指定 {@code long} 值的符号函数。（如果指定值为负，返回值是 -1；
     * 如果指定值为零，返回 0；如果指定值为正，返回 1。）
     *
     * @param i 要计算符号的值
     * @return 指定 {@code long} 值的符号函数。
     * @since 1.5
     */
    public static int signum(long i) {
        // HD, 章节 2-7
        return (int) ((i >> 63) | (-i >>> 63));
    }

    /**
     * 返回通过反转指定 {@code long} 值的二进制补码表示中的字节顺序获得的值。
     *
     * @param i 要反转字节的值
     * @return 通过反转指定 {@code long} 值中的字节顺序获得的值。
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
     * 返回两个 {@code long} 值中较大的一个，如同调用 {@link Math#max(long, long) Math.max}。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 中较大的一个
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static long max(long a, long b) {
        return Math.max(a, b);
    }

    /**
     * 返回两个 {@code long} 值中较小的一个，如同调用 {@link Math#min(long, long) Math.min}。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 中较小的一个
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static long min(long a, long b) {
        return Math.min(a, b);
    }

    /** 为互操作性使用 JDK 1.0.2 的 serialVersionUID */
    @Native private static final long serialVersionUID = 4290774380558885855L;
}