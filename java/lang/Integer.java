
/*
 * 版权所有 (c) 1994, 2020, Oracle 和/或其附属公司。保留所有权利。
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

import java.lang.annotation.Native;

/**
 * {@code Integer} 类将原始类型 {@code int} 的值包装在一个对象中。一个 {@code Integer} 类型的对象包含一个类型为 {@code int} 的单个字段。
 *
 * <p>此外，此类提供了几个将 {@code int} 转换为 {@code String} 和将 {@code String} 转换为 {@code int} 的方法，以及其他在处理 {@code int} 时有用的常量和方法。
 *
 * <p>实现说明：位操作方法（如 {@link #highestOneBit(int) highestOneBit} 和 {@link #numberOfTrailingZeros(int) numberOfTrailingZeros}）的实现基于 Henry S. Warren, Jr. 的《Hacker's Delight》（Addison Wesley, 2002）中的材料。
 *
 * @author  Lee Boynton
 * @author  Arthur van Hoff
 * @author  Josh Bloch
 * @author  Joseph D. Darcy
 * @since JDK1.0
 */
public final class Integer extends Number implements Comparable<Integer> {
    /**
     * 一个常量，表示 {@code int} 可以拥有的最小值，-2<sup>31</sup>。
     */
    @Native public static final int   MIN_VALUE = 0x80000000;

    /**
     * 一个常量，表示 {@code int} 可以拥有的最大值，2<sup>31</sup>-1。
     */
    @Native public static final int   MAX_VALUE = 0x7fffffff;

    /**
     * 表示原始类型 {@code int} 的 {@code Class} 实例。
     *
     * @since   JDK1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Integer>  TYPE = (Class<Integer>) Class.getPrimitiveClass("int");

    /**
     * 用于将数字表示为字符串的所有可能的字符
     */
    final static char[] digits = {
        '0' , '1' , '2' , '3' , '4' , '5' ,
        '6' , '7' , '8' , '9' , 'a' , 'b' ,
        'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
        'u' , 'v' , 'w' , 'x' , 'y' , 'z'
    };

    /**
     * 返回第一个参数在由第二个参数指定的基数下的字符串表示形式。
     *
     * <p>如果基数小于 {@code Character.MIN_RADIX} 或大于 {@code Character.MAX_RADIX}，则使用基数 {@code 10}。
     *
     * <p>如果第一个参数为负数，结果的第一个字符是 ASCII 减号字符 {@code '-'} ({@code '\u005Cu002D'})。如果第一个参数不是负数，则结果中不会出现符号字符。
     *
     * <p>结果中的其余字符表示第一个参数的绝对值。如果绝对值为零，则用单个零字符 {@code '0'} ({@code '\u005Cu0030'}) 表示；否则，表示绝对值的第一个字符不会是零字符。以下 ASCII 字符用作数字：
     *
     * <blockquote>
     *   {@code 0123456789abcdefghijklmnopqrstuvwxyz}
     * </blockquote>
     *
     * 这些是 {@code '\u005Cu0030'} 到 {@code '\u005Cu0039'} 和 {@code '\u005Cu0061'} 到 {@code '\u005Cu007A'}。如果基数为 <var>N</var>，则使用这些字符中的前 <var>N</var> 个作为基数-<var>N</var> 的数字，顺序如上所示。因此，十六进制（基数 16）的数字为 {@code 0123456789abcdef}。如果需要大写字母，可以调用结果的 {@link java.lang.String#toUpperCase()} 方法：
     *
     * <blockquote>
     *  {@code Integer.toString(n, 16).toUpperCase()}
     * </blockquote>
     *
     * @param   i       要转换为字符串的整数。
     * @param   radix   字符串表示形式中使用的基数。
     * @return  指定基数下的参数的字符串表示形式。
     * @see     java.lang.Character#MAX_RADIX
     * @see     java.lang.Character#MIN_RADIX
     */
    public static String toString(int i, int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;

        /* 使用更快的版本 */
        if (radix == 10) {
            return toString(i);
        }

        char buf[] = new char[33];
        boolean negative = (i < 0);
        int charPos = 32;

        if (!negative) {
            i = -i;
        }

        while (i <= -radix) {
            buf[charPos--] = digits[-(i % radix)];
            i = i / radix;
        }
        buf[charPos] = digits[-i];

        if (negative) {
            buf[--charPos] = '-';
        }

        return new String(buf, charPos, (33 - charPos));
    }

    /**
     * 返回第一个参数作为无符号整数值在由第二个参数指定的基数下的字符串表示形式。
     *
     * <p>如果基数小于 {@code Character.MIN_RADIX} 或大于 {@code Character.MAX_RADIX}，则使用基数 {@code 10}。
     *
     * <p>请注意，由于第一个参数被视为无符号值，因此不会打印前导符号字符。
     *
     * <p>如果绝对值为零，则用单个零字符 {@code '0'} ({@code '\u005Cu0030'}) 表示；否则，表示绝对值的第一个字符不会是零字符。
     *
     * <p>基数的行为和用作数字的字符与 {@link #toString(int, int) toString} 相同。
     *
     * @param   i       要转换为无符号字符串的整数。
     * @param   radix   字符串表示形式中使用的基数。
     * @return  指定基数下的参数的无符号字符串表示形式。
     * @see     #toString(int, int)
     * @since 1.8
     */
    public static String toUnsignedString(int i, int radix) {
        return Long.toUnsignedString(toUnsignedLong(i), radix);
    }


                    /**
     * 返回整数参数的字符串表示形式，作为16进制的无符号整数。
     *
     * <p>无符号整数值是参数加上2<sup>32</sup>
     * 如果参数为负数；否则，它等于参数。此值转换为ASCII数字的字符串
     * 16进制（基数16），没有额外的前导
     * {@code 0}。
     *
     * <p>可以通过调用 {@link
     * Integer#parseUnsignedInt(String, int)
     * Integer.parseUnsignedInt(s, 16)} 从返回的字符串 {@code s} 中恢复参数的值。
     *
     * <p>如果无符号值为零，则表示为单个零字符 {@code '0'} ({@code '\u005Cu0030'}); 否则，无符号值表示的第一个字符不会是零字符。以下字符用作16进制数字：
     *
     * <blockquote>
     *  {@code 0123456789abcdef}
     * </blockquote>
     *
     * 这些是字符 {@code '\u005Cu0030'} 到
     * {@code '\u005Cu0039'} 和 {@code '\u005Cu0061'} 到
     * {@code '\u005Cu0066'}。如果需要大写字母，可以在结果上调用 {@link java.lang.String#toUpperCase()} 方法：
     *
     * <blockquote>
     *  {@code Integer.toHexString(n).toUpperCase()}
     * </blockquote>
     *
     * @param   i   要转换为字符串的整数。
     * @return  参数所表示的无符号整数值的16进制（基数16）字符串表示形式。
     * @see #parseUnsignedInt(String, int)
     * @see #toUnsignedString(int, int)
     * @since   JDK1.0.2
     */
    public static String toHexString(int i) {
        return toUnsignedString0(i, 4);
    }

    /**
     * 返回整数参数的字符串表示形式，作为8进制的无符号整数。
     *
     * <p>无符号整数值是参数加上2<sup>32</sup>
     * 如果参数为负数；否则，它等于参数。此值转换为ASCII数字的字符串
     * 8进制（基数8），没有额外的前导 {@code 0}。
     *
     * <p>可以通过调用 {@link
     * Integer#parseUnsignedInt(String, int)
     * Integer.parseUnsignedInt(s, 8)} 从返回的字符串 {@code s} 中恢复参数的值。
     *
     * <p>如果无符号值为零，则表示为单个零字符 {@code '0'} ({@code '\u005Cu0030'}); 否则，无符号值表示的第一个字符不会是零字符。以下字符用作8进制数字：
     *
     * <blockquote>
     * {@code 01234567}
     * </blockquote>
     *
     * 这些是字符 {@code '\u005Cu0030'} 到
     * {@code '\u005Cu0037'}。
     *
     * @param   i   要转换为字符串的整数。
     * @return  参数所表示的无符号整数值的8进制（基数8）字符串表示形式。
     * @see #parseUnsignedInt(String, int)
     * @see #toUnsignedString(int, int)
     * @since   JDK1.0.2
     */
    public static String toOctalString(int i) {
        return toUnsignedString0(i, 3);
    }

    /**
     * 返回整数参数的字符串表示形式，作为2进制的无符号整数。
     *
     * <p>无符号整数值是参数加上2<sup>32</sup>
     * 如果参数为负数；否则，它等于参数。此值转换为ASCII数字的字符串
     * 2进制（基数2），没有额外的前导 {@code 0}。
     *
     * <p>可以通过调用 {@link
     * Integer#parseUnsignedInt(String, int)
     * Integer.parseUnsignedInt(s, 2)} 从返回的字符串 {@code s} 中恢复参数的值。
     *
     * <p>如果无符号值为零，则表示为单个零字符 {@code '0'} ({@code '\u005Cu0030'}); 否则，无符号值表示的第一个字符不会是零字符。字符 {@code '0'} ({@code '\u005Cu0030'}) 和 {@code
     * '1'} ({@code '\u005Cu0031'}) 用作2进制数字。
     *
     * @param   i   要转换为字符串的整数。
     * @return  参数所表示的无符号整数值的2进制（基数2）字符串表示形式。
     * @see #parseUnsignedInt(String, int)
     * @see #toUnsignedString(int, int)
     * @since   JDK1.0.2
     */
    public static String toBinaryString(int i) {
        return toUnsignedString0(i, 1);
    }

    /**
     * 将整数转换为无符号数。
     */
    private static String toUnsignedString0(int val, int shift) {
        // assert shift > 0 && shift <=5 : "Illegal shift value";
        int mag = Integer.SIZE - Integer.numberOfLeadingZeros(val);
        int chars = Math.max(((mag + (shift - 1)) / shift), 1);
        char[] buf = new char[chars];

        formatUnsignedInt(val, shift, buf, 0, chars);

        // 使用特殊构造函数接管 "buf"。
        return new String(buf, true);
    }

    /**
     * 将长整数（视为无符号）格式化为字符缓冲区。
     * @param val 要格式化的无符号整数
     * @param shift 基数的log2值（4为16进制，3为8进制，1为2进制）
     * @param buf 要写入的字符缓冲区
     * @param offset 目标缓冲区中开始的位置
     * @param len 要写入的字符数
     * @return 使用的最低字符位置
     */
     static int formatUnsignedInt(int val, int shift, char[] buf, int offset, int len) {
        int charPos = len;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            buf[offset + --charPos] = Integer.digits[val & mask];
            val >>>= shift;
        } while (val != 0 && charPos > 0);

        return charPos;
    }

    final static char [] DigitTens = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
        } ;


                    final static char [] DigitOnes = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        } ;

        // 我使用了“不变除法乘法”技巧来加速 Integer.toString。特别是我们希望避免除以 10。
        //
        // 这个“技巧”在非 JIT 虚拟机上的性能特性与“经典”Integer.toString 代码大致相同。
        // 这个技巧避免了 .rem 和 .div 调用，但代码路径更长，因此主要受调度开销的影响。在
        // JIT 情况下，调度开销不存在，这个“技巧”比经典代码快得多。
        //
        // TODO-FIXME: 将 (x * 52429) 转换为等效的移位加法序列。
        //
        // 参考:  使用乘法进行不变整数除法
        //        T Gralund, P Montgomery
        //        ACM PLDI 1994
        //

    /**
     * 返回一个表示指定整数的 {@code String} 对象。参数被转换为带符号的十进制表示形式并作为字符串返回，就像
     * 参数和基数 10 被作为参数传递给 {@link #toString(int, int)} 方法一样。
     *
     * @param   i   要转换的整数。
     * @return  参数的十进制字符串表示形式。
     */
    public static String toString(int i) {
        if (i == Integer.MIN_VALUE)
            return "-2147483648";
        int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
        char[] buf = new char[size];
        getChars(i, size, buf);
        return new String(buf, true);
    }

    /**
     * 返回参数的无符号十进制值的字符串表示形式。
     *
     * 参数被转换为无符号十进制表示形式，并作为字符串返回，就像参数和基数 10 被作为参数传递给
     * {@link #toUnsignedString(int, int)} 方法一样。
     *
     * @param   i  要转换为无符号字符串的整数。
     * @return  参数的无符号字符串表示形式。
     * @see     #toUnsignedString(int, int)
     * @since 1.8
     */
    public static String toUnsignedString(int i) {
        return Long.toString(toUnsignedLong(i));
    }

    /**
     * 将表示整数 i 的字符放入字符数组 buf 中。字符从最不重要的数字开始，从指定的索引（不包括）向后放置。
     *
     * 如果 i == Integer.MIN_VALUE，将会失败。
     */
    static void getChars(int i, int index, char[] buf) {
        int q, r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // 每次迭代生成两个数字
        while (i >= 65536) {
            q = i / 100;
        // 真正的: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf [--charPos] = DigitOnes[r];
            buf [--charPos] = DigitTens[r];
        }

        // 对较小的数字使用快速模式
        // assert(i <= 65536, i);
        for (;;) {
            q = (i * 52429) >>> (16+3);
            r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            buf [--charPos] = digits [r];
            i = q;
            if (i == 0) break;
        }
        if (sign != 0) {
            buf [--charPos] = sign;
        }
    }

    final static int [] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999,
                                      99999999, 999999999, Integer.MAX_VALUE };

    // 要求 x 为正数
    static int stringSize(int x) {
        for (int i=0; ; i++)
            if (x <= sizeTable[i])
                return i+1;
    }

    /**
     * 将字符串参数解析为指定基数的带符号整数。字符串中的字符必须都是指定基数的数字（由
     * {@link java.lang.Character#digit(char, int)} 是否返回非负值确定），但第一个字符可以是
     * ASCII 减号 {@code '-'} ({@code '\u005Cu002D'}) 表示负值或 ASCII 加号 {@code '+'}
     * ({@code '\u005Cu002B'}) 表示正值。返回解析后的整数值。
     *
     * <p>如果发生以下任何情况，将抛出 {@code NumberFormatException} 异常：
     * <ul>
     * <li>第一个参数为 {@code null} 或长度为零的字符串。
     *
     * <li>基数小于 {@link java.lang.Character#MIN_RADIX} 或大于 {@link java.lang.Character#MAX_RADIX}。
     *
     * <li>字符串中的任何字符都不是指定基数的数字，但第一个字符可以是减号
     * {@code '-'} ({@code '\u005Cu002D'}) 或加号 {@code '+'} ({@code '\u005Cu002B'})，前提是
     * 字符串长度大于 1。
     *
     * <li>字符串表示的值不是 {@code int} 类型的值。
     * </ul>
     *
     * <p>示例：
     * <blockquote><pre>
     * parseInt("0", 10) 返回 0
     * parseInt("473", 10) 返回 473
     * parseInt("+42", 10) 返回 42
     * parseInt("-0", 10) 返回 0
     * parseInt("-FF", 16) 返回 -255
     * parseInt("1100110", 2) 返回 102
     * parseInt("2147483647", 10) 返回 2147483647
     * parseInt("-2147483648", 10) 返回 -2147483648
     * parseInt("2147483648", 10) 抛出 NumberFormatException
     * parseInt("99", 8) 抛出 NumberFormatException
     * parseInt("Kona", 10) 抛出 NumberFormatException
     * parseInt("Kona", 27) 返回 411787
     * </pre></blockquote>
     *
     * @param      s   包含要解析的整数表示形式的 {@code String}
     * @param      radix   解析 {@code s} 时使用的基数。
     * @return     指定基数下字符串参数表示的整数。
     * @exception  NumberFormatException 如果 {@code String} 不包含可解析的 {@code int}。
     */
    public static int parseInt(String s, int radix)
                throws NumberFormatException
    {
        /*
         * 警告：此方法可能在 VM 初始化早期被调用，此时 IntegerCache 尚未初始化。必须注意不要使用
         * valueOf 方法。
         */


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

        int result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // 可能的前导 "+" 或 "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+')
                    throw NumberFormatException.forInputString(s);

                if (len == 1) // 不能有单独的 "+" 或 "-"
                    throw NumberFormatException.forInputString(s);
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // 负数累加避免接近 MAX_VALUE 时的意外
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
     * 将字符串参数解析为带符号的十进制整数。字符串中的字符必须全部是十进制数字，但第一个字符可以是 ASCII 减号 {@code '-'}
     * ({@code '\u005Cu002D'}) 表示负值，或 ASCII 加号 {@code '+'} ({@code '\u005Cu002B'}) 表示正值。返回的整数值
     * 完全等同于将参数和基数 10 作为参数传递给 {@link #parseInt(java.lang.String, int)} 方法的结果。
     *
     * @param s    包含要解析的 {@code int} 表示的 {@code String}
     * @return     以十进制表示的参数的整数值。
     * @exception  NumberFormatException  如果字符串不包含可解析的整数。
     */
    public static int parseInt(String s) throws NumberFormatException {
        return parseInt(s,10);
    }

    /**
     * 将字符串参数解析为指定基数的无符号整数。无符号整数将通常与负数关联的值映射为大于 {@code MAX_VALUE} 的正数。
     *
     * 字符串中的字符必须全部是指定基数的数字（由 {@link java.lang.Character#digit(char, int)} 是否返回非负值确定），
     * 但第一个字符可以是 ASCII 加号 {@code '+'} ({@code '\u005Cu002B'})。返回解析后的整数值。
     *
     * <p>如果发生以下任何情况，将抛出类型为 {@code NumberFormatException} 的异常：
     * <ul>
     * <li>第一个参数为 {@code null} 或长度为零的字符串。
     *
     * <li>基数小于 {@link java.lang.Character#MIN_RADIX} 或大于 {@link java.lang.Character#MAX_RADIX}。
     *
     * <li>字符串中的任何字符都不是指定基数的数字，但第一个字符可以是加号 {@code '+'} ({@code '\u005Cu002B'})，
     * 前提是字符串的长度大于 1。
     *
     * <li>字符串表示的值大于最大的无符号 {@code int}，即 2<sup>32</sup>-1。
     *
     * </ul>
     *
     *
     * @param      s   包含要解析的无符号整数表示的 {@code String}
     * @param      radix   解析 {@code s} 时使用的基数。
     * @return     以指定基数表示的字符串参数的整数值。
     * @throws     NumberFormatException 如果 {@code String} 不包含可解析的 {@code int}。
     * @since 1.8
     */
    public static int parseUnsignedInt(String s, int radix)
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
                                                       "在无符号字符串 %s 中.", s));
            } else {
                if (len <= 5 || // Integer.MAX_VALUE 在 Character.MAX_RADIX 中是 6 位
                    (radix == 10 && len <= 9) ) { // Integer.MAX_VALUE 在基数 10 中是 10 位
                    return parseInt(s, radix);
                } else {
                    long ell = Long.parseLong(s, radix);
                    if ((ell & 0xffff_ffff_0000_0000L) == 0) {
                        return (int) ell;
                    } else {
                        throw new
                            NumberFormatException(String.format("字符串值 %s 超过 " +
                                                                "无符号 int 的范围.", s));
                    }
                }
            }
        } else {
            throw NumberFormatException.forInputString(s);
        }
    }

                    /**
     * 将字符串参数解析为无符号十进制整数。字符串中的字符必须全部为十进制数字，但第一个字符可以是ASCII加号 {@code
     * '+'} ({@code '\u005Cu002B'})。返回解析得到的整数值，就像将参数和基数10作为参数传递给 {@link
     * #parseUnsignedInt(java.lang.String, int)} 方法一样。
     *
     * @param s   包含要解析的无符号 {@code int} 表示形式的 {@code String}
     * @return    由参数表示的十进制无符号整数值。
     * @throws    NumberFormatException  如果字符串不包含可解析的无符号整数。
     * @since 1.8
     */
    public static int parseUnsignedInt(String s) throws NumberFormatException {
        return parseUnsignedInt(s, 10);
    }

    /**
     * 返回一个包含从指定 {@code String} 解析出的值的 {@code Integer} 对象。第一个参数被解释为以第二个参数指定的基数表示的有符号整数，就像将参数传递给 {@link #parseInt(java.lang.String, int)}
     * 方法一样。结果是一个表示字符串指定整数值的 {@code Integer} 对象。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Integer} 对象：
     *
     * <blockquote>
     *  {@code new Integer(Integer.parseInt(s, radix))}
     * </blockquote>
     *
     * @param      s   要解析的字符串。
     * @param      radix 用于解释 {@code s} 的基数
     * @return     一个包含由字符串参数在指定基数中表示的值的 {@code Integer} 对象。
     * @exception NumberFormatException 如果 {@code String}
     *            不包含可解析的 {@code int}。
     */
    public static Integer valueOf(String s, int radix) throws NumberFormatException {
        return Integer.valueOf(parseInt(s,radix));
    }

    /**
     * 返回一个包含指定 {@code String} 的值的 {@code Integer} 对象。参数被解释为表示有符号十进制整数，就像将参数传递给 {@link
     * #parseInt(java.lang.String)} 方法一样。结果是一个表示字符串指定整数值的 {@code Integer} 对象。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Integer} 对象：
     *
     * <blockquote>
     *  {@code new Integer(Integer.parseInt(s))}
     * </blockquote>
     *
     * @param      s   要解析的字符串。
     * @return     一个包含由字符串参数表示的值的 {@code Integer} 对象。
     * @exception  NumberFormatException  如果字符串不能被解析为整数。
     */
    public static Integer valueOf(String s) throws NumberFormatException {
        return Integer.valueOf(parseInt(s, 10));
    }

    /**
     * 缓存以支持 JLS 要求的 -128 到 127（包括）之间值的自动装箱对象标识语义。
     *
     * 缓存在首次使用时初始化。缓存的大小可以通过 {@code -XX:AutoBoxCacheMax=<size>} 选项控制。
     * 在 VM 初始化期间，java.lang.Integer.IntegerCache.high 属性可能会被设置并保存在 sun.misc.VM 类的私有系统属性中。
     */

    private static class IntegerCache {
        static final int low = -128;
        static final int high;
        static final Integer cache[];

        static {
            // 高值可以由属性配置
            int h = 127;
            String integerCacheHighPropValue =
                sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
            if (integerCacheHighPropValue != null) {
                try {
                    int i = parseInt(integerCacheHighPropValue);
                    i = Math.max(i, 127);
                    // 最大数组大小是 Integer.MAX_VALUE
                    h = Math.min(i, Integer.MAX_VALUE - (-low) -1);
                } catch( NumberFormatException nfe) {
                    // 如果属性不能解析为 int，则忽略它。
                }
            }
            high = h;

            cache = new Integer[(high - low) + 1];
            int j = low;
            for(int k = 0; k < cache.length; k++)
                cache[k] = new Integer(j++);

            // 范围 [-128, 127] 必须被 intern（JLS7 5.1.7）
            assert IntegerCache.high >= 127;
        }

        private IntegerCache() {}
    }

    /**
     * 返回一个表示指定 {@code int} 值的 {@code Integer} 实例。如果不需要新的 {@code Integer} 实例，通常应优先使用此方法而不是构造函数 {@link #Integer(int)}，因为此方法通过缓存经常请求的值，可能显著提高空间和时间性能。
     *
     * 此方法将始终缓存 -128 到 127 之间的值，也可能缓存此范围之外的其他值。
     *
     * @param  i 一个 {@code int} 值。
     * @return 一个表示 {@code i} 的 {@code Integer} 实例。
     * @since  1.5
     */
    public static Integer valueOf(int i) {
        if (i >= IntegerCache.low && i <= IntegerCache.high)
            return IntegerCache.cache[i + (-IntegerCache.low)];
        return new Integer(i);
    }

    /**
     * {@code Integer} 的值。
     *
     * @serial
     */
    private final int value;

    /**
     * 构造一个新的分配的 {@code Integer} 对象，表示指定的 {@code int} 值。
     *
     * @param   value   要由 {@code Integer} 对象表示的值。
     */
    public Integer(int value) {
        this.value = value;
    }

                    /**
     * 构造一个新的分配的 {@code Integer} 对象，该对象表示由
     * {@code String} 参数指示的 {@code int} 值。字符串被转换为
     * {@code int} 值，转换方式与 {@code parseInt} 方法对基数 10 的处理方式完全相同。
     *
     * @param      s   要转换为 {@code Integer} 的 {@code String}。
     * @exception  NumberFormatException  如果 {@code String} 不包含可解析的整数。
     * @see        java.lang.Integer#parseInt(java.lang.String, int)
     */
    public Integer(String s) throws NumberFormatException {
        this.value = parseInt(s, 10);
    }

    /**
     * 返回此 {@code Integer} 的值作为 {@code byte}，经过窄化原始转换。
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * 返回此 {@code Integer} 的值作为 {@code short}，经过窄化原始转换。
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * 返回此 {@code Integer} 的值作为 {@code int}。
     */
    public int intValue() {
        return value;
    }

    /**
     * 返回此 {@code Integer} 的值作为 {@code long}，经过扩展原始转换。
     * @jls 5.1.2 Widening Primitive Conversions
     * @see Integer#toUnsignedLong(int)
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * 返回此 {@code Integer} 的值作为 {@code float}，经过扩展原始转换。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * 返回此 {@code Integer} 的值作为 {@code double}，经过扩展原始转换。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * 返回表示此 {@code Integer} 值的 {@code String} 对象。该值被转换为带符号的十进制表示形式并作为字符串返回，就像将整数值作为参数传递给 {@link
     * java.lang.Integer#toString(int)} 方法一样。
     *
     * @return  表示此对象值的字符串，以十进制表示。
     */
    public String toString() {
        return toString(value);
    }

    /**
     * 返回此 {@code Integer} 的哈希码。
     *
     * @return  该对象的哈希码值，等于此 {@code Integer} 对象表示的原始 {@code int} 值的哈希码。
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    /**
     * 返回 {@code int} 值的哈希码；与 {@code Integer.hashCode()} 兼容。
     *
     * @param value 要哈希的值
     * @since 1.8
     *
     * @return  {@code int} 值的哈希码。
     */
    public static int hashCode(int value) {
        return value;
    }

    /**
     * 将此对象与指定对象进行比较。结果为
     * {@code true} 当且仅当参数不为
     * {@code null} 并且是一个包含与此对象相同的 {@code int} 值的 {@code Integer} 对象。
     *
     * @param   obj   要比较的对象。
     * @return  如果对象相同，则返回 {@code true}；
     *          否则返回 {@code false}。
     */
    public boolean equals(Object obj) {
        if (obj instanceof Integer) {
            return value == ((Integer)obj).intValue();
        }
        return false;
    }

    /**
     * 确定具有指定名称的系统属性的整数值。
     *
     * <p>第一个参数被视为系统属性的名称。系统属性可以通过 {@link
     * java.lang.System#getProperty(java.lang.String)} 方法访问。该属性的字符串值然后被解释为一个整数值，使用 {@link Integer#decode decode} 支持的语法，
     * 并返回表示该值的 {@code Integer} 对象。
     *
     * <p>如果没有具有指定名称的属性，指定的名称为空或 {@code null}，或者属性没有正确的数字格式，则返回 {@code null}。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Integer} 对象：
     *
     * <blockquote>
     *  {@code getInteger(nm, null)}
     * </blockquote>
     *
     * @param   nm   属性名称。
     * @return  属性的 {@code Integer} 值。
     * @throws  SecurityException 与 {@link System#getProperty(String) System.getProperty} 相同的原因
     * @see     java.lang.System#getProperty(java.lang.String)
     * @see     java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static Integer getInteger(String nm) {
        return getInteger(nm, null);
    }

    /**
     * 确定具有指定名称的系统属性的整数值。
     *
     * <p>第一个参数被视为系统属性的名称。系统属性可以通过 {@link
     * java.lang.System#getProperty(java.lang.String)} 方法访问。该属性的字符串值然后被解释为一个整数值，使用 {@link Integer#decode decode} 支持的语法，
     * 并返回表示该值的 {@code Integer} 对象。
     *
     * <p>第二个参数是默认值。如果不存在具有指定名称的属性，属性没有正确的数字格式，或者指定的名称为空或 {@code null}，则返回表示第二个参数值的 {@code Integer} 对象。
     *
     * <p>换句话说，此方法返回一个等于以下值的 {@code Integer} 对象：
     *
     * <blockquote>
     *  {@code getInteger(nm, new Integer(val))}
     * </blockquote>
     *
     * 但实际上，它可能以如下方式实现，以避免在不需要默认值时不必要的分配 {@code Integer} 对象：
     *
     * <blockquote><pre>
     * Integer result = getInteger(nm, null);
     * return (result == null) ? new Integer(val) : result;
     * </pre></blockquote>
     *
     * @param   nm   属性名称。
     * @param   val   默认值。
     * @return  属性的 {@code Integer} 值。
     * @throws  SecurityException 与 {@link System#getProperty(String) System.getProperty} 相同的原因
     * @see     java.lang.System#getProperty(java.lang.String)
     * @see     java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static Integer getInteger(String nm, int val) {
        Integer result = getInteger(nm, null);
        return (result == null) ? Integer.valueOf(val) : result;
    }

                    /**
     * 返回指定名称的系统属性的整数值。第一个参数被视为系统属性的名称。系统属性可通过
     * {@link java.lang.System#getProperty(java.lang.String)} 方法访问。
     * 该属性的字符串值将被解释为一个整数值，解释方式与 {@link Integer#decode decode} 方法相同，
     * 并返回一个表示该值的 {@code Integer} 对象；简而言之：
     *
     * <ul><li>如果属性值以两个ASCII字符 {@code 0x} 或ASCII字符 {@code #} 开头，且不以负号开头，
     *         那么其余部分将被解析为一个十六进制整数，解析方式与方法
     *      {@link #valueOf(java.lang.String, int)} 的基数16相同。
     * <li>如果属性值以ASCII字符 {@code 0} 开头并跟有另一个字符，它将被解析为一个
     *     八进制整数，解析方式与方法
     *     {@link #valueOf(java.lang.String, int)} 的基数8相同。
     * <li>否则，属性值将被解析为一个十进制整数
     *     解析方式与方法 {@link #valueOf(java.lang.String, int)}
     *     的基数10相同。
     * </ul>
     *
     * <p>第二个参数是默认值。如果不存在指定名称的属性，属性的格式不正确，或者指定的名称为空或 {@code null}，
     * 则返回默认值。
     *
     * @param   nm   属性名称。
     * @param   val   默认值。
     * @return  属性的 {@code Integer} 值。
     * @throws  SecurityException 与 {@link System#getProperty(String) System.getProperty}
     *          抛出的原因相同。
     * @see     System#getProperty(java.lang.String)
     * @see     System#getProperty(java.lang.String, java.lang.String)
     */
    public static Integer getInteger(String nm, Integer val) {
        String v = null;
        try {
            v = System.getProperty(nm);
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        if (v != null) {
            try {
                return Integer.decode(v);
            } catch (NumberFormatException e) {
            }
        }
        return val;
    }

    /**
     * 将一个 {@code String} 解码为一个 {@code Integer}。
     * 接受十进制、十六进制和八进制数，由以下语法定义：
     *
     * <blockquote>
     * <dl>
     * <dt><i>可解码字符串：</i>
     * <dd><i>可选符号 十进制数字</i>
     * <dd><i>可选符号</i> {@code 0x} <i>十六进制数字</i>
     * <dd><i>可选符号</i> {@code 0X} <i>十六进制数字</i>
     * <dd><i>可选符号</i> {@code #} <i>十六进制数字</i>
     * <dd><i>可选符号</i> {@code 0} <i>八进制数字</i>
     *
     * <dt><i>符号：</i>
     * <dd>{@code -}
     * <dd>{@code +}
     * </dl>
     * </blockquote>
     *
     * <i>十进制数字</i>、<i>十六进制数字</i>和<i>八进制数字</i>的定义见
     * <cite>The Java&trade; Language Specification</cite> 的第3.10.1节，
     * 但不接受数字之间的下划线。
     *
     * <p>可选符号和/或基数指定符（“{@code 0x}”、“{@code 0X}”、“{@code #}”或前导零）之后的字符序列
     * 将按照 {@code Integer.parseInt} 方法以指定的基数（10、16或8）解析。此字符序列必须表示一个正数，
     * 否则将抛出 {@link NumberFormatException}。如果指定的 {@code String} 的第一个字符是减号，则结果为负数。
     * 在 {@code String} 中不允许有空白字符。
     *
     * @param     nm 要解码的 {@code String}。
     * @return    一个包含 {@code nm} 表示的 {@code int} 值的 {@code Integer} 对象
     * @exception NumberFormatException 如果 {@code String} 不包含可解析的整数。
     * @see java.lang.Integer#parseInt(java.lang.String, int)
     */
    public static Integer decode(String nm) throws NumberFormatException {
        int radix = 10;
        int index = 0;
        boolean negative = false;
        Integer result;

        if (nm.isEmpty())
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
            result = Integer.valueOf(nm.substring(index), radix);
            result = negative ? Integer.valueOf(-result.intValue()) : result;
        } catch (NumberFormatException e) {
            // 如果数字是 Integer.MIN_VALUE，我们将在这里结束。下一行处理这种情况，
            // 并导致任何真正的格式错误被重新抛出。
            String constant = negative ? ("-" + nm.substring(index))
                                       : nm.substring(index);
            result = Integer.valueOf(constant, radix);
        }
        return result;
    }

    /**
     * 数值比较两个 {@code Integer} 对象。
     *
     * @param   anotherInteger   要比较的 {@code Integer}。
     * @return  如果此 {@code Integer} 等于参数 {@code Integer}，则返回值为 {@code 0}；
     *          如果此 {@code Integer} 数值上小于参数 {@code Integer}，则返回值小于 {@code 0}；
     *          如果此 {@code Integer} 数值上大于参数 {@code Integer}，则返回值大于 {@code 0}（带符号比较）。
     * @since   1.2
     */
    public int compareTo(Integer anotherInteger) {
        return compare(this.value, anotherInteger.value);
    }

                    /**
     * 比较两个 {@code int} 值的数值。
     * 返回的值与以下代码返回的值相同：
     * <pre>
     *    Integer.valueOf(x).compareTo(Integer.valueOf(y))
     * </pre>
     *
     * @param  x 要比较的第一个 {@code int}
     * @param  y 要比较的第二个 {@code int}
     * @return 如果 {@code x == y}，则返回 {@code 0}；
     *         如果 {@code x < y}，则返回小于 {@code 0} 的值；
     *         如果 {@code x > y}，则返回大于 {@code 0} 的值
     * @since 1.7
     */
    public static int compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    /**
     * 以无符号形式比较两个 {@code int} 值的数值。
     *
     * @param  x 要比较的第一个 {@code int}
     * @param  y 要比较的第二个 {@code int}
     * @return 如果 {@code x == y}，则返回 {@code 0}；如果 {@code x < y} 作为无符号值，则返回小于 {@code 0} 的值；
     *         如果 {@code x > y} 作为无符号值，则返回大于 {@code 0} 的值
     * @since 1.8
     */
    public static int compareUnsigned(int x, int y) {
        return compare(x + MIN_VALUE, y + MIN_VALUE);
    }

    /**
     * 通过无符号转换将参数转换为 {@code long}。在无符号转换为 {@code long} 时，{@code long} 的高 32 位为零，低 32 位等于整数参数的位。
     *
     * 因此，零和正 {@code int} 值映射到数值相等的 {@code long} 值，而负 {@code int} 值映射到等于输入值加上 2<sup>32</sup> 的 {@code long} 值。
     *
     * @param  x 要转换为无符号 {@code long} 的值
     * @return 通过无符号转换将参数转换为 {@code long}
     * @since 1.8
     */
    public static long toUnsignedLong(int x) {
        return ((long) x) & 0xffffffffL;
    }

    /**
     * 返回将第一个参数除以第二个参数的无符号商，其中每个参数和结果都被解释为无符号值。
     *
     * <p>注意，在二进制补码算术中，加法、减法和乘法这三种基本算术运算在将两个操作数视为有符号或无符号时是位相同的。因此没有提供单独的 {@code
     * addUnsigned} 等方法。
     *
     * @param dividend 被除数
     * @param divisor 除数
     * @return 第一个参数除以第二个参数的无符号商
     * @see #remainderUnsigned
     * @since 1.8
     */
    public static int divideUnsigned(int dividend, int divisor) {
        // 暂时使用长整型算术代替复杂的代码。
        return (int)(toUnsignedLong(dividend) / toUnsignedLong(divisor));
    }

    /**
     * 返回将第一个参数除以第二个参数的无符号余数，其中每个参数和结果都被解释为无符号值。
     *
     * @param dividend 被除数
     * @param divisor 除数
     * @return 第一个参数除以第二个参数的无符号余数
     * @see #divideUnsigned
     * @since 1.8
     */
    public static int remainderUnsigned(int dividend, int divisor) {
        // 暂时使用长整型算术代替复杂的代码。
        return (int)(toUnsignedLong(dividend) % toUnsignedLong(divisor));
    }


    // 位操作

    /**
     * 用于表示一个 {@code int} 值在二进制补码形式中使用的位数。
     *
     * @since 1.5
     */
    @Native public static final int SIZE = 32;

    /**
     * 用于表示一个 {@code int} 值在二进制补码形式中使用的字节数。
     *
     * @since 1.8
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /**
     * 返回一个最多只有一个 1 位的 {@code int} 值，该 1 位位于指定 {@code int} 值的最高位（"最左边"）1 位的位置。如果指定的值在其二进制补码表示中没有 1 位，即等于零，则返回零。
     *
     * @param i 要计算最高 1 位的值
     * @return 一个最多只有一个 1 位的 {@code int} 值，该 1 位位于指定值的最高位 1 位的位置，或者如果指定的值本身等于零，则返回零。
     * @since 1.5
     */
    public static int highestOneBit(int i) {
        // HD, Figure 3-1
        i |= (i >>  1);
        i |= (i >>  2);
        i |= (i >>  4);
        i |= (i >>  8);
        i |= (i >> 16);
        return i - (i >>> 1);
    }

    /**
     * 返回一个最多只有一个 1 位的 {@code int} 值，该 1 位位于指定 {@code int} 值的最低位（"最右边"）1 位的位置。如果指定的值在其二进制补码表示中没有 1 位，即等于零，则返回零。
     *
     * @param i 要计算最低 1 位的值
     * @return 一个最多只有一个 1 位的 {@code int} 值，该 1 位位于指定值的最低位 1 位的位置，或者如果指定的值本身等于零，则返回零。
     * @since 1.5
     */
    public static int lowestOneBit(int i) {
        // HD, Section 2-1
        return i & -i;
    }

    /**
     * 返回指定 {@code int} 值的二进制补码表示中最高位（"最左边"）1 位之前的零位数。如果指定的值在其二进制补码表示中没有 1 位，即等于零，则返回 32。
     *
     * <p>注意，此方法与以 2 为底的对数密切相关。对于所有正的 {@code int} 值 x：
     * <ul>
     * <li>floor(log<sub>2</sub>(x)) = {@code 31 - numberOfLeadingZeros(x)}
     * <li>ceil(log<sub>2</sub>(x)) = {@code 32 - numberOfLeadingZeros(x - 1)}
     * </ul>
     *
     * @param i 要计算前导零数的值
     * @return 指定 {@code int} 值的二进制补码表示中最高位（"最左边"）1 位之前的零位数，或者如果值等于零，则返回 32。
     * @since 1.5
     */
    public static int numberOfLeadingZeros(int i) {
        // HD, Figure 5-6
        if (i == 0)
            return 32;
        int n = 1;
        if (i >>> 16 == 0) { n += 16; i <<= 16; }
        if (i >>> 24 == 0) { n +=  8; i <<=  8; }
        if (i >>> 28 == 0) { n +=  4; i <<=  4; }
        if (i >>> 30 == 0) { n +=  2; i <<=  2; }
        n -= i >>> 31;
        return n;
    }

                    /**
     * 返回指定的 {@code int} 值的二进制补码表示中最低位（最右边）的1位之后的零位数。如果指定的值在其二进制补码表示中没有1位，即等于零，则返回32。
     *
     * @param i 要计算尾随零的数量的值
     * @return 指定的 {@code int} 值的二进制补码表示中最低位（最右边）的1位之后的零位数，如果值等于零，则返回32。
     * @since 1.5
     */
    public static int numberOfTrailingZeros(int i) {
        // HD, Figure 5-14
        int y;
        if (i == 0) return 32;
        int n = 31;
        y = i <<16; if (y != 0) { n = n -16; i = y; }
        y = i << 8; if (y != 0) { n = n - 8; i = y; }
        y = i << 4; if (y != 0) { n = n - 4; i = y; }
        y = i << 2; if (y != 0) { n = n - 2; i = y; }
        return n - ((i << 1) >>> 31);
    }

    /**
     * 返回指定的 {@code int} 值的二进制补码表示中的1位数。此函数有时被称为<i>人口计数</i>。
     *
     * @param i 要计数的值
     * @return 指定的 {@code int} 值的二进制补码表示中的1位数。
     * @since 1.5
     */
    public static int bitCount(int i) {
        // HD, Figure 5-2
        i = i - ((i >>> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
        i = (i + (i >>> 4)) & 0x0f0f0f0f;
        i = i + (i >>> 8);
        i = i + (i >>> 16);
        return i & 0x3f;
    }

    /**
     * 返回将指定的 {@code int} 值的二进制补码表示向左旋转指定位数后得到的值。（从左侧移出的位会重新进入右侧。）
     *
     * <p>注意，使用负距离向左旋转等同于向右旋转：{@code rotateLeft(val, -distance) == rotateRight(val,
     * distance)}。另外，任何32的倍数的旋转都是无操作，因此旋转距离的最后五位以外的位可以被忽略，即使距离是负数：{@code rotateLeft(val,
     * distance) == rotateLeft(val, distance & 0x1F)}。
     *
     * @param i 要向左旋转的值
     * @param distance 向左旋转的位数
     * @return 将指定的 {@code int} 值的二进制补码表示向左旋转指定位数后得到的值。
     * @since 1.5
     */
    public static int rotateLeft(int i, int distance) {
        return (i << distance) | (i >>> -distance);
    }

    /**
     * 返回将指定的 {@code int} 值的二进制补码表示向右旋转指定位数后得到的值。（从右侧移出的位会重新进入左侧。）
     *
     * <p>注意，使用负距离向右旋转等同于向左旋转：{@code rotateRight(val, -distance) == rotateLeft(val,
     * distance)}。另外，任何32的倍数的旋转都是无操作，因此旋转距离的最后五位以外的位可以被忽略，即使距离是负数：{@code rotateRight(val,
     * distance) == rotateRight(val, distance & 0x1F)}。
     *
     * @param i 要向右旋转的值
     * @param distance 向右旋转的位数
     * @return 将指定的 {@code int} 值的二进制补码表示向右旋转指定位数后得到的值。
     * @since 1.5
     */
    public static int rotateRight(int i, int distance) {
        return (i >>> distance) | (i << -distance);
    }

    /**
     * 返回将指定的 {@code int} 值的二进制补码表示中的位顺序反转后得到的值。
     *
     * @param i 要反转的值
     * @return 将指定的 {@code int} 值的二进制补码表示中的位顺序反转后得到的值。
     * @since 1.5
     */
    public static int reverse(int i) {
        // HD, Figure 7-1
        i = (i & 0x55555555) << 1 | (i >>> 1) & 0x55555555;
        i = (i & 0x33333333) << 2 | (i >>> 2) & 0x33333333;
        i = (i & 0x0f0f0f0f) << 4 | (i >>> 4) & 0x0f0f0f0f;
        i = (i << 24) | ((i & 0xff00) << 8) |
            ((i >>> 8) & 0xff00) | (i >>> 24);
        return i;
    }

    /**
     * 返回指定的 {@code int} 值的符号函数。（如果指定的值为负，则返回值为-1；如果指定的值为零，则返回值为0；如果指定的值为正，则返回值为1。）
     *
     * @param i 要计算符号函数的值
     * @return 指定的 {@code int} 值的符号函数。
     * @since 1.5
     */
    public static int signum(int i) {
        // HD, Section 2-7
        return (i >> 31) | (-i >>> 31);
    }

    /**
     * 返回将指定的 {@code int} 值的二进制补码表示中的字节顺序反转后得到的值。
     *
     * @param i 要反转字节的值
     * @return 将指定的 {@code int} 值的二进制补码表示中的字节顺序反转后得到的值。
     * @since 1.5
     */
    public static int reverseBytes(int i) {
        return ((i >>> 24)           ) |
               ((i >>   8) &   0xFF00) |
               ((i <<   8) & 0xFF0000) |
               ((i << 24));
    }

    /**
     * 根据 + 运算符将两个整数相加。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 的和
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static int sum(int a, int b) {
        return a + b;
    }

                    /**
     * 返回两个 {@code int} 值中较大的一个，如同调用 {@link Math#max(int, int) Math.max} 一样。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 中较大的一个
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    /**
     * 返回两个 {@code int} 值中较小的一个，如同调用 {@link Math#min(int, int) Math.min} 一样。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 中较小的一个
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    /** 使用 JDK 1.0.2 的 serialVersionUID 以确保互操作性 */
    @Native private static final long serialVersionUID = 1360826667806852920L;
}
