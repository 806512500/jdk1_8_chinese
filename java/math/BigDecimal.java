
/*
 * Copyright (c) 1996, 2019, Oracle and/or its affiliates. All rights reserved.
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

/*
 * Portions Copyright IBM Corporation, 2001. All Rights Reserved.
 */

package java.math;

import static java.math.BigInteger.LONG_MASK;
import java.util.Arrays;

/**
 * 不可变的、任意精度的有符号十进制数。一个 {@code BigDecimal} 由一个任意精度的整数 <i>未缩放值</i> 和一个 32 位整数 <i>比例</i> 组成。如果比例为零或正数，则比例是小数点右边的数字位数。如果比例为负数，则数字的未缩放值乘以 10 的比例的相反数次幂。因此，{@code BigDecimal} 表示的数字值为 <tt>(未缩放值 &times; 10<sup>-比例</sup>)</tt>。
 *
 * <p>{@code BigDecimal} 类提供了算术运算、比例操作、舍入、比较、哈希和格式转换的操作。{@link #toString} 方法提供了 {@code BigDecimal} 的规范表示形式。
 *
 * <p>{@code BigDecimal} 类为其用户提供了对舍入行为的完全控制。如果没有指定舍入模式且无法表示精确结果，则会抛出异常；否则，通过向操作提供适当的 {@link MathContext} 对象，可以以选定的精度和舍入模式进行计算。在任何情况下，都提供了八种 <em>舍入模式</em> 以控制舍入。使用此类中的整数字段（如 {@link #ROUND_HALF_UP}）来表示舍入模式已基本过时；应使用 {@code RoundingMode} {@code enum} 的枚举值（如 {@link RoundingMode#HALF_UP}）。
 *
 * <p>当提供具有精度设置为 0 的 {@code MathContext} 对象时（例如，{@link MathContext#UNLIMITED}），算术运算是精确的，不提供 {@code MathContext} 对象的算术方法也是如此。（这是在 5 之前版本中支持的唯一行为。）作为计算精确结果的必然结果，具有精度设置为 0 的 {@code MathContext} 对象的舍入模式设置未被使用，因此无关紧要。在除法的情况下，商可能具有无限长的小数扩展；例如，1 除以 3。如果商具有非终止的小数扩展且操作指定返回精确结果，则会抛出 {@code ArithmeticException}。否则，返回除法的精确结果，就像其他操作一样。
 *
 * <p>当精度设置不为 0 时，{@code BigDecimal} 算术的规则与 ANSI X3.274-1996 和 ANSI X3.274-1996/AM 1-2000（第 7.4 节）中定义的算术的选定操作模式大致兼容。与这些标准不同的是，{@code BigDecimal} 包含许多舍入模式，这些模式在 5 之前的 {@code BigDecimal} 版本中是除法的强制性要求。任何与这些 ANSI 标准和 {@code BigDecimal} 规范之间的冲突都以 {@code BigDecimal} 为准。
 *
 * <p>由于相同的数值可以有不同的表示形式（具有不同的比例），因此算术和舍入的规则必须指定结果的数值和结果表示的比例。
 *
 *
 * <p>一般来说，舍入模式和精度设置决定了当精确结果具有比返回的数字更多的数字时（在除法的情况下可能是无限多的数字），操作如何返回具有有限数字的结果。
 *
 * 首先，返回的数字总数由 {@code MathContext} 的 {@code precision} 设置指定；这决定了结果的 <i>精度</i>。从精确结果的最左边的非零数字开始计数。舍入模式决定了任何丢弃的尾随数字如何影响返回的结果。
 *
 * <p>对于所有算术运算符，操作首先计算出一个逻辑上的精确中间结果，然后根据精度设置（如果需要）使用选定的舍入模式将其舍入到指定的数字。如果未返回精确结果，则会丢弃精确结果的一些数字位置。当舍入增加了返回结果的大小时，由于进位传播到前导 {@literal "9"} 数字，可能会创建一个新的数字位置。例如，将值 999.9 舍入到三位数并向上舍入，数值上等于一千，表示为 100&times;10<sup>1</sup>。在这些情况下，新的 {@literal "1"} 是返回结果的前导数字位置。
 *
 * <p>除了逻辑上的精确结果外，每个算术运算都有一个表示结果的首选比例。每个操作的首选比例列在下表中。
 *
 * <table border>
 * <caption><b>算术运算结果的首选比例</b></caption>
 * <tr><th>运算</th><th>结果的首选比例</th></tr>
 * <tr><td>加</td><td>max(加数的比例(), 被加数的比例())</td>
 * <tr><td>减</td><td>max(被减数的比例(), 减数的比例())</td>
 * <tr><td>乘</td><td>乘数的比例() + 被乘数的比例()</td>
 * <tr><td>除</td><td>被除数的比例() - 除数的比例()</td>
 * </table>
 *
 * 这些比例是返回精确算术结果的方法使用的比例；但精确除法可能需要使用更大的比例，因为精确结果可能具有更多的数字。例如，{@code 1/32} 是 {@code 0.03125}。
 *
 * <p>在舍入之前，逻辑上的精确中间结果的比例是该操作的首选比例。如果精确的数值结果无法用 {@code precision} 位数字表示，舍入将选择要返回的数字集，并将结果的比例从中间结果的比例减少到可以表示实际返回的 {@code precision} 位数字的最小比例。如果精确结果可以用最多 {@code precision} 位数字表示，则返回具有最接近首选比例的表示形式。特别是，可以精确表示的商可能通过删除尾随零并减少比例来用少于 {@code precision} 位数字表示。例如，使用 {@linkplain RoundingMode#FLOOR 地板} 舍入模式舍入到三位数，<br>
 *
 * {@code 19/100 = 0.19   // 整数=19,  比例=2} <br>
 *
 * 但是<br>
 *
 * {@code 21/110 = 0.190  // 整数=190, 比例=3} <br>
 *
 * <p>对于加法、减法和乘法，比例的减少将等于精确结果中被丢弃的数字位置的数量。如果舍入导致进位传播并创建一个新的高位数字位置，则丢弃的数字位置比没有创建新的数字位置时多一个。
 *
 * <p>其他方法可能具有稍微不同的舍入语义。例如，使用指定算法的 {@code pow} 方法的结果偶尔可能与舍入的数学结果相差超过一个单位，即一个 <i>{@linkplain #ulp() ulp}</i>。
 *
 * <p>提供了两种操作来操作 {@code BigDecimal} 的比例：缩放/舍入操作和小数点移动操作。缩放/舍入操作（{@link #setScale setScale} 和 {@link #round round}）返回一个值大约（或完全）等于操作数的 {@code BigDecimal}，但其比例或精度是指定的值；也就是说，它们增加或减少存储数字的精度，对值的影响最小。小数点移动操作（{@link #movePointLeft movePointLeft} 和 {@link #movePointRight movePointRight}）返回一个从操作数通过将小数点移动指定距离和指定方向创建的 {@code BigDecimal}。
 *
 * <p>为了简洁和清晰，描述 {@code BigDecimal} 方法时使用了伪代码。伪代码表达式 {@code (i + j)} 是 "一个 {@code BigDecimal}，其值是 {@code BigDecimal} {@code i} 加上 {@code BigDecimal} {@code j} 的值" 的简写。伪代码表达式 {@code (i == j)} 是 "当且仅当 {@code BigDecimal} {@code i} 表示的值与 {@code BigDecimal} {@code j} 表示的值相同时，为 {@code true}" 的简写。其他伪代码表达式类似解释。方括号用于表示定义 {@code BigDecimal} 值的特定 {@code BigInteger} 和比例对；例如 [19, 2] 是数值上等于 0.19 且比例为 2 的 {@code BigDecimal}。
 *
 * <p>注意：如果将 {@code BigDecimal} 对象用作 {@link java.util.SortedMap SortedMap} 的键或 {@link java.util.SortedSet SortedSet} 的元素，应谨慎使用，因为 {@code BigDecimal} 的 <i>自然排序</i> 与 <i>equals</i> <i>不一致</i>。有关更多信息，请参见 {@link Comparable}、{@link java.util.SortedMap} 或 {@link java.util.SortedSet}。
 *
 * <p>此类的所有方法和构造函数在传递 {@code null} 对象引用的任何输入参数时都会抛出 {@code NullPointerException}。
 *
 * @see     BigInteger
 * @see     MathContext
 * @see     RoundingMode
 * @see     java.util.SortedMap
 * @see     java.util.SortedSet
 * @author  Josh Bloch
 * @author  Mike Cowlishaw
 * @author  Joseph D. Darcy
 * @author  Sergey V. Kuksenko
 */
public class BigDecimal extends Number implements Comparable<BigDecimal> {
    /**
     * 这个 BigDecimal 的未缩放值，如 {@link #unscaledValue} 返回的。
     *
     * @serial
     * @see #unscaledValue
     */
    private final BigInteger intVal;

                /**
     * 返回 {@link #scale} 方法的 BigDecimal 的比例。
     *
     * @serial
     * @see #scale
     */
    private final int scale;  // 注意：此值可以是任何值，因此
                              // 计算必须使用 long 类型

    /**
     * 此 BigDecimal 的十进制数字数量，如果数字数量未知（旁观者信息），则为 0。如果非零，该值保证正确。如果可能为 0，请使用 precision() 方法获取和设置值。此字段在设置非零值之前是可变的。
     *
     * @since  1.5
     */
    private transient int precision;

    /**
     * 用于存储计算出的规范字符串表示形式。
     */
    private transient String stringCache;

    /**
     * 用于指示 {@link #intCompact} 中的有效数字信息仅从 {@code intVal} 可用的哨兵值。
     */
    static final long INFLATED = Long.MIN_VALUE;

    private static final BigInteger INFLATED_BIGINT = BigInteger.valueOf(INFLATED);

    /**
     * 如果此 BigDecimal 的有效数字的绝对值小于或等于 {@code Long.MAX_VALUE}，则该值可以紧凑地存储在此字段中并用于计算。
     */
    private final transient long intCompact;

    // 所有 18 位十进制字符串都可以放入 long 中；不是所有 19 位字符串都可以
    private static final int MAX_COMPACT_DIGITS = 18;

    /* 安抚序列化之神 */
    private static final long serialVersionUID = 6108874887143696463L;

    private static final ThreadLocal<StringBuilderHelper>
        threadLocalStringBuilderHelper = new ThreadLocal<StringBuilderHelper>() {
        @Override
        protected StringBuilderHelper initialValue() {
            return new StringBuilderHelper();
        }
    };

    // 常见小 BigDecimal 值的缓存。
    private static final BigDecimal zeroThroughTen[] = {
        new BigDecimal(BigInteger.ZERO,       0,  0, 1),
        new BigDecimal(BigInteger.ONE,        1,  0, 1),
        new BigDecimal(BigInteger.valueOf(2), 2,  0, 1),
        new BigDecimal(BigInteger.valueOf(3), 3,  0, 1),
        new BigDecimal(BigInteger.valueOf(4), 4,  0, 1),
        new BigDecimal(BigInteger.valueOf(5), 5,  0, 1),
        new BigDecimal(BigInteger.valueOf(6), 6,  0, 1),
        new BigDecimal(BigInteger.valueOf(7), 7,  0, 1),
        new BigDecimal(BigInteger.valueOf(8), 8,  0, 1),
        new BigDecimal(BigInteger.valueOf(9), 9,  0, 1),
        new BigDecimal(BigInteger.TEN,        10, 0, 2),
    };

    // 缩放 0 - 15 的缓存
    private static final BigDecimal[] ZERO_SCALED_BY = {
        zeroThroughTen[0],
        new BigDecimal(BigInteger.ZERO, 0, 1, 1),
        new BigDecimal(BigInteger.ZERO, 0, 2, 1),
        new BigDecimal(BigInteger.ZERO, 0, 3, 1),
        new BigDecimal(BigInteger.ZERO, 0, 4, 1),
        new BigDecimal(BigInteger.ZERO, 0, 5, 1),
        new BigDecimal(BigInteger.ZERO, 0, 6, 1),
        new BigDecimal(BigInteger.ZERO, 0, 7, 1),
        new BigDecimal(BigInteger.ZERO, 0, 8, 1),
        new BigDecimal(BigInteger.ZERO, 0, 9, 1),
        new BigDecimal(BigInteger.ZERO, 0, 10, 1),
        new BigDecimal(BigInteger.ZERO, 0, 11, 1),
        new BigDecimal(BigInteger.ZERO, 0, 12, 1),
        new BigDecimal(BigInteger.ZERO, 0, 13, 1),
        new BigDecimal(BigInteger.ZERO, 0, 14, 1),
        new BigDecimal(BigInteger.ZERO, 0, 15, 1),
    };

    // Long.MIN_VALUE 和 Long.MAX_VALUE 的一半。
    private static final long HALF_LONG_MAX_VALUE = Long.MAX_VALUE / 2;
    private static final long HALF_LONG_MIN_VALUE = Long.MIN_VALUE / 2;

    // 常量
    /**
     * 值为 0，比例为 0。
     *
     * @since  1.5
     */
    public static final BigDecimal ZERO =
        zeroThroughTen[0];

    /**
     * 值为 1，比例为 0。
     *
     * @since  1.5
     */
    public static final BigDecimal ONE =
        zeroThroughTen[1];

    /**
     * 值为 10，比例为 0。
     *
     * @since  1.5
     */
    public static final BigDecimal TEN =
        zeroThroughTen[10];

    // 构造函数

    /**
     * 受信任的包私有构造函数。
     * 受信任仅意味着如果 val 是 INFLATED，intVal 不能为 null，如果 intVal 为 null，val 不能是 INFLATED。
     */
    BigDecimal(BigInteger intVal, long val, int scale, int prec) {
        this.scale = scale;
        this.precision = prec;
        this.intCompact = val;
        this.intVal = intVal;
    }

    /**
     * 将 {@code BigDecimal} 的字符数组表示形式转换为 {@code BigDecimal}，接受与 {@link #BigDecimal(String)}
     * 构造函数相同的字符序列，同时允许指定子数组。
     *
     * <p>注意，如果字符序列已经在字符数组中可用，使用此构造函数比将 {@code char} 数组转换为字符串并使用
     * {@code BigDecimal(String)} 构造函数更快。
     *
     * @param  in {@code char} 数组，是字符的来源。
     * @param  offset 数组中要检查的第一个字符。
     * @param  len 要考虑的字符数。
     * @throws NumberFormatException 如果 {@code in} 不是 {@code BigDecimal} 的有效表示形式或定义的子数组不在 {@code in} 中。
     * @since  1.5
     */
    public BigDecimal(char[] in, int offset, int len) {
        this(in,offset,len,MathContext.UNLIMITED);
    }

    /**
     * 将 {@code BigDecimal} 的字符数组表示形式转换为 {@code BigDecimal}，接受与 {@link #BigDecimal(String)}
     * 构造函数相同的字符序列，同时允许指定子数组，并根据上下文设置进行舍入。
     *
     * <p>注意，如果字符序列已经在字符数组中可用，使用此构造函数比将 {@code char} 数组转换为字符串并使用
     * {@code BigDecimal(String)} 构造函数更快。
     *
     * @param  in {@code char} 数组，是字符的来源。
     * @param  offset 数组中要检查的第一个字符。
     * @param  len 要考虑的字符数。
     * @param  mc 要使用的上下文。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}。
     * @throws NumberFormatException 如果 {@code in} 不是 {@code BigDecimal} 的有效表示形式或定义的子数组不在 {@code in} 中。
     * @since  1.5
     */
    public BigDecimal(char[] in, int offset, int len, MathContext mc) {
        // 保护防止长度过大、负值和整数溢出
        if ((in.length | len | offset) < 0 || len > in.length - offset) {
            throw new NumberFormatException
                ("Bad offset or len arguments for char[] input.");
        }


                    // 这是主要的字符串到BigDecimal的构造函数；所有传入的字符串最终都会到这里；它使用显式（内联）
        // 解析以提高速度，并且在非紧凑情况下最多生成一个中间
        // （临时）对象（一个char[]数组）。

        // 使用局部变量存储所有字段值直到完成
        int prec = 0;                 // 记录精度值
        int scl = 0;                  // 记录比例值
        long rs = 0;                  // 长整型中的紧凑值
        BigInteger rb = null;         // BigInteger中的膨胀值
        // 使用数组边界检查处理过长、len == 0、
        // 偏移量错误等。
        try {
            // 处理符号
            boolean isneg = false;          // 假设为正
            if (in[offset] == '-') {
                isneg = true;               // 前导减号表示负数
                offset++;
                len--;
            } else if (in[offset] == '+') { // 允许前导 +
                offset++;
                len--;
            }

            // 现在应该是数字部分的小数
            boolean dot = false;             // 当存在'.'时为true
            long exp = 0;                    // 指数
            char c;                          // 当前字符
            boolean isCompact = (len <= MAX_COMPACT_DIGITS);
            // 整数小数数组 & idx 是其索引。该数组
            // 仅在不能使用紧凑表示时使用。
            int idx = 0;
            if (isCompact) {
                // 第一个紧凑情况，我们不需要保留字符
                // 可以直接在原地计算值。
                for (; len > 0; offset++, len--) {
                    c = in[offset];
                    if ((c == '0')) { // 有零
                        if (prec == 0)
                            prec = 1;
                        else if (rs != 0) {
                            rs *= 10;
                            ++prec;
                        } // 否则数字是冗余的前导零
                        if (dot)
                            ++scl;
                    } else if ((c >= '1' && c <= '9')) { // 有数字
                        int digit = c - '0';
                        if (prec != 1 || rs != 0)
                            ++prec; // 如果前面是0s，精度不变
                        rs = rs * 10 + digit;
                        if (dot)
                            ++scl;
                    } else if (c == '.') {   // 有小数点
                        // 有小数点
                        if (dot) // 两个小数点
                            throw new NumberFormatException();
                        dot = true;
                    } else if (Character.isDigit(c)) { // 慢路径
                        int digit = Character.digit(c, 10);
                        if (digit == 0) {
                            if (prec == 0)
                                prec = 1;
                            else if (rs != 0) {
                                rs *= 10;
                                ++prec;
                            } // 否则数字是冗余的前导零
                        } else {
                            if (prec != 1 || rs != 0)
                                ++prec; // 如果前面是0s，精度不变
                            rs = rs * 10 + digit;
                        }
                        if (dot)
                            ++scl;
                    } else if ((c == 'e') || (c == 'E')) {
                        exp = parseExp(in, offset, len);
                        // 下一个测试是为了向后兼容
                        if ((int) exp != exp) // 溢出
                            throw new NumberFormatException();
                        break; // [节省一个测试]
                    } else {
                        throw new NumberFormatException();
                    }
                }
                if (prec == 0) // 没有找到数字
                    throw new NumberFormatException();
                // 如果exp不为零，调整比例。
                if (exp != 0) { // 有重要的指数
                    scl = adjustScale(scl, exp);
                }
                rs = isneg ? -rs : rs;
                int mcp = mc.precision;
                int drop = prec - mcp; // prec 的范围是 [1, MAX_INT]，mcp 的范围是 [0, MAX_INT]；
                                       // 因此，这个减法不会溢出
                if (mcp > 0 && drop > 0) {  // 进行舍入
                    while (drop > 0) {
                        scl = checkScaleNonZero((long) scl - drop);
                        rs = divideAndRound(rs, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                        prec = longDigitLength(rs);
                        drop = prec - mcp;
                    }
                }
            } else {
                char coeff[] = new char[len];
                for (; len > 0; offset++, len--) {
                    c = in[offset];
                    // 有数字
                    if ((c >= '0' && c <= '9') || Character.isDigit(c)) {
                        // 第一个紧凑情况，我们不需要保留字符
                        // 可以直接在原地计算值。
                        if (c == '0' || Character.digit(c, 10) == 0) {
                            if (prec == 0) {
                                coeff[idx] = c;
                                prec = 1;
                            } else if (idx != 0) {
                                coeff[idx++] = c;
                                ++prec;
                            } // 否则 c 必须是冗余的前导零
                        } else {
                            if (prec != 1 || idx != 0)
                                ++prec; // 如果前面是0s，精度不变
                            coeff[idx++] = c;
                        }
                        if (dot)
                            ++scl;
                        continue;
                    }
                    // 有小数点
                    if (c == '.') {
                        // 有小数点
                        if (dot) // 两个小数点
                            throw new NumberFormatException();
                        dot = true;
                        continue;
                    }
                    // 期望指数
                    if ((c != 'e') && (c != 'E'))
                        throw new NumberFormatException();
                    exp = parseExp(in, offset, len);
                    // 下一个测试是为了向后兼容
                    if ((int) exp != exp) // 溢出
                        throw new NumberFormatException();
                    break; // [节省一个测试]
                }
                // 当没有字符剩余时
                if (prec == 0) // 没有找到数字
                    throw new NumberFormatException();
                // 如果exp不为零，调整比例。
                if (exp != 0) { // 有重要的指数
                    scl = adjustScale(scl, exp);
                }
                // 从精度（数字计数）中移除前导零
                rb = new BigInteger(coeff, isneg ? -1 : 1, prec);
                rs = compactValFor(rb);
                int mcp = mc.precision;
                if (mcp > 0 && (prec > mcp)) {
                    if (rs == INFLATED) {
                        int drop = prec - mcp;
                        while (drop > 0) {
                            scl = checkScaleNonZero((long) scl - drop);
                            rb = divideAndRoundByTenPow(rb, drop, mc.roundingMode.oldMode);
                            rs = compactValFor(rb);
                            if (rs != INFLATED) {
                                prec = longDigitLength(rs);
                                break;
                            }
                            prec = bigDigitLength(rb);
                            drop = prec - mcp;
                        }
                    }
                    if (rs != INFLATED) {
                        int drop = prec - mcp;
                        while (drop > 0) {
                            scl = checkScaleNonZero((long) scl - drop);
                            rs = divideAndRound(rs, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                            prec = longDigitLength(rs);
                            drop = prec - mcp;
                        }
                        rb = null;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NumberFormatException();
        } catch (NegativeArraySizeException e) {
            throw new NumberFormatException();
        }
        this.scale = scl;
        this.precision = prec;
        this.intCompact = rs;
        this.intVal = rb;
    }


                private int adjustScale(int scl, long exp) {
        long adjustedScale = scl - exp;
        if (adjustedScale > Integer.MAX_VALUE || adjustedScale < Integer.MIN_VALUE)
            throw new NumberFormatException("Scale out of range.");
        scl = (int) adjustedScale;
        return scl;
    }

    /*
     * 解析指数
     */
    private static long parseExp(char[] in, int offset, int len){
        long exp = 0;
        offset++;
        char c = in[offset];
        len--;
        boolean negexp = (c == '-');
        // 可选符号
        if (negexp || c == '+') {
            offset++;
            c = in[offset];
            len--;
        }
        if (len <= 0) // 没有指数数字
            throw new NumberFormatException();
        // 跳过指数中的前导零
        while (len > 10 && (c=='0' || (Character.digit(c, 10) == 0))) {
            offset++;
            c = in[offset];
            len--;
        }
        if (len > 10) // 非零指数数字太多
            throw new NumberFormatException();
        // c 现在持有指数的第一个数字
        for (;; len--) {
            int v;
            if (c >= '0' && c <= '9') {
                v = c - '0';
            } else {
                v = Character.digit(c, 10);
                if (v < 0) // 不是数字
                    throw new NumberFormatException();
            }
            exp = exp * 10 + v;
            if (len == 1)
                break; // 那是最后一个字符
            offset++;
            c = in[offset];
        }
        if (negexp) // 应用符号
            exp = -exp;
        return exp;
    }

    /**
     * 将字符数组表示的 {@code BigDecimal} 转换为 {@code BigDecimal}，接受与 {@link #BigDecimal(String)}
     * 构造函数相同的字符序列。
     *
     * <p>注意，如果字符序列已经作为字符数组可用，使用此构造函数比将 {@code char} 数组转换为字符串并使用
     * {@code BigDecimal(String)} 构造函数更快。
     *
     * @param in {@code char} 数组，是字符的来源。
     * @throws NumberFormatException 如果 {@code in} 不是 {@code BigDecimal} 的有效表示。
     * @since  1.5
     */
    public BigDecimal(char[] in) {
        this(in, 0, in.length);
    }

    /**
     * 将字符数组表示的 {@code BigDecimal} 转换为 {@code BigDecimal}，接受与 {@link #BigDecimal(String)}
     * 构造函数相同的字符序列，并根据上下文设置进行舍入。
     *
     * <p>注意，如果字符序列已经作为字符数组可用，使用此构造函数比将 {@code char} 数组转换为字符串并使用
     * {@code BigDecimal(String)} 构造函数更快。
     *
     * @param  in {@code char} 数组，是字符的来源。
     * @param  mc 要使用的上下文。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}。
     * @throws NumberFormatException 如果 {@code in} 不是 {@code BigDecimal} 的有效表示。
     * @since  1.5
     */
    public BigDecimal(char[] in, MathContext mc) {
        this(in, 0, in.length, mc);
    }

    /**
     * 将 {@code BigDecimal} 的字符串表示转换为 {@code BigDecimal}。字符串表示由可选的符号（{@code '+'} 或
     * {@code '-'}），后跟零个或多个十进制数字（“整数部分”），可选地后跟小数部分，再可选地后跟指数组成。
     *
     * <p>小数部分由小数点后跟零个或多个十进制数字组成。字符串必须在整数部分或小数部分中至少包含一个数字。由符号、整数部分和小数部分组成的数称为 <i>尾数</i>。
     *
     * <p>指数由字符 {@code 'e'} 或 {@code 'E'} 后跟一个或多个十进制数字组成。指数的值必须在 -{@link Integer#MAX_VALUE} ({@link
     * Integer#MIN_VALUE}+1) 和 {@link Integer#MAX_VALUE} 之间，包括这两个值。
     *
     * <p>更正式地，此构造函数接受的字符串由以下语法描述：
     * <blockquote>
     * <dl>
     * <dt><i>BigDecimalString:</i>
     * <dd><i>Sign<sub>opt</sub> Significand Exponent<sub>opt</sub></i>
     * <dt><i>Sign:</i>
     * <dd>{@code +}
     * <dd>{@code -}
     * <dt><i>Significand:</i>
     * <dd><i>IntegerPart</i> {@code .} <i>FractionPart<sub>opt</sub></i>
     * <dd>{@code .} <i>FractionPart</i>
     * <dd><i>IntegerPart</i>
     * <dt><i>IntegerPart:</i>
     * <dd><i>Digits</i>
     * <dt><i>FractionPart:</i>
     * <dd><i>Digits</i>
     * <dt><i>Exponent:</i>
     * <dd><i>ExponentIndicator SignedInteger</i>
     * <dt><i>ExponentIndicator:</i>
     * <dd>{@code e}
     * <dd>{@code E}
     * <dt><i>SignedInteger:</i>
     * <dd><i>Sign<sub>opt</sub> Digits</i>
     * <dt><i>Digits:</i>
     * <dd><i>Digit</i>
     * <dd><i>Digits Digit</i>
     * <dt><i>Digit:</i>
     * <dd>任何使 {@link Character#isDigit} 返回 {@code true} 的字符，包括 0, 1, 2 ...
     * </dl>
     * </blockquote>
     *
     * <p>返回的 {@code BigDecimal} 的标度是小数部分中的数字个数，如果没有小数点，则为零，根据指数进行调整；如果字符串包含指数，则从标度中减去指数。结果标度的值必须在 {@code Integer.MIN_VALUE} 和
     * {@code Integer.MAX_VALUE} 之间，包括这两个值。
     *
     * <p>字符到数字的映射由 {@link java.lang.Character#digit} 提供，设置为转换为基数 10。字符串中不得包含任何额外的字符（例如空格）。
     *
     * <p><b>示例：</b><br>
     * 返回的 {@code BigDecimal} 的值等于
     * <i>尾数</i> &times; 10<sup>&nbsp;<i>指数</i></sup>。对于左边的每个字符串，右边显示了结果表示
     * [{@code BigInteger}, {@code scale}]。
     * <pre>
     * "0"            [0,0]
     * "0.00"         [0,2]
     * "123"          [123,0]
     * "-123"         [-123,0]
     * "1.23E3"       [123,-1]
     * "1.23E+3"      [123,-1]
     * "12.3E+7"      [123,-6]
     * "12.0"         [120,1]
     * "12.3"         [123,1]
     * "0.00123"      [123,5]
     * "-1.23E-12"    [-123,14]
     * "1234.5E-4"    [12345,5]
     * "0E+7"         [0,-7]
     * "-0"           [0,0]
     * </pre>
     *
     * <p>注意：对于除 {@code float} 和 {@code double} NaN 和 &plusmn;Infinity 之外的值，此构造函数与 {@link Float#toString}
     * 和 {@link Double#toString} 返回的值兼容。这通常是将 {@code float} 或 {@code double} 转换为 {@code BigDecimal} 的首选方法，因为它不会受到 {@link #BigDecimal(double)} 构造函数的不可预测性的影响。
     *
     * @param val {@code BigDecimal} 的字符串表示。
     *
     * @throws NumberFormatException 如果 {@code val} 不是 {@code BigDecimal} 的有效表示。
     */
    public BigDecimal(String val) {
        this(val.toCharArray(), 0, val.length());
    }

                /**
     * 将 {@code BigDecimal} 的字符串表示形式转换为 {@code BigDecimal}，接受与
     * {@link #BigDecimal(String)} 构造函数相同的字符串，并根据上下文设置进行舍入。
     *
     * @param  val {@code BigDecimal} 的字符串表示形式。
     * @param  mc 要使用的上下文。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}。
     * @throws NumberFormatException 如果 {@code val} 不是有效的 {@code BigDecimal} 表示形式。
     * @since  1.5
     */
    public BigDecimal(String val, MathContext mc) {
        this(val.toCharArray(), 0, val.length(), mc);
    }

    /**
     * 将 {@code double} 转换为 {@code BigDecimal}，该 {@code BigDecimal} 是 {@code double} 的二进制浮点值的确切十进制表示形式。
     * 返回的 {@code BigDecimal} 的比例是最小值，使得 <tt>(10<sup>scale</sup> &times; val)</tt> 是一个整数。
     * <p>
     * <b>注意事项：</b>
     * <ol>
     * <li>
     * 此构造函数的结果可能有些不可预测。人们可能会认为在 Java 中写入 {@code new BigDecimal(0.1)}
     * 会创建一个恰好等于 0.1（无缩放值为 1，比例为 1）的 {@code BigDecimal}，但实际上它等于
     * 0.1000000000000000055511151231257827021181583404541015625。
     * 这是因为 0.1 不能精确表示为 {@code double}（或者，实际上，任何有限长度的二进制分数）。
     * 因此，传递给构造函数的值实际上并不等于 0.1，尽管看起来如此。
     *
     * <li>
     * 另一方面，{@code String} 构造函数是完全可预测的：写入 {@code new BigDecimal("0.1")}
     * 会创建一个 <i>恰好</i> 等于 0.1 的 {@code BigDecimal}，正如人们所期望的那样。
     * 因此，通常建议优先使用 {@linkplain #BigDecimal(String) <tt>String</tt> 构造函数}。
     *
     * <li>
     * 当必须将 {@code double} 用作 {@code BigDecimal} 的源时，请注意，此构造函数提供了一个精确的转换；
     * 它不会产生与使用 {@link Double#toString(double)} 方法将 {@code double} 转换为 {@code String}，
     * 然后使用 {@link #BigDecimal(String)} 构造函数相同的结果。要获得该结果，请使用静态的
     * {@link #valueOf(double)} 方法。
     * </ol>
     *
     * @param val 要转换为 {@code BigDecimal} 的 {@code double} 值。
     * @throws NumberFormatException 如果 {@code val} 是无穷大或 NaN。
     */
    public BigDecimal(double val) {
        this(val, MathContext.UNLIMITED);
    }

    /**
     * 将 {@code double} 转换为 {@code BigDecimal}，并根据上下文设置进行舍入。返回的 {@code BigDecimal} 的比例是最小值，
     * 使得 <tt>(10<sup>scale</sup> &times; val)</tt> 是一个整数。
     *
     * <p>此构造函数的结果可能有些不可预测，通常不推荐使用；请参阅 {@link #BigDecimal(double)} 构造函数的注意事项。
     *
     * @param  val 要转换为 {@code BigDecimal} 的 {@code double} 值。
     * @param  mc 要使用的上下文。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 UNNECESSARY。
     * @throws NumberFormatException 如果 {@code val} 是无穷大或 NaN。
     * @since  1.5
     */
    public BigDecimal(double val, MathContext mc) {
        if (Double.isInfinite(val) || Double.isNaN(val))
            throw new NumberFormatException("Infinite or NaN");
        // 将 double 转换为符号、指数和尾数，根据 JLS，第 20.10.22 节的公式。
        long valBits = Double.doubleToLongBits(val);
        int sign = ((valBits >> 63) == 0 ? 1 : -1);
        int exponent = (int) ((valBits >> 52) & 0x7ffL);
        long significand = (exponent == 0
                ? (valBits & ((1L << 52) - 1)) << 1
                : (valBits & ((1L << 52) - 1)) | (1L << 52));
        exponent -= 1075;
        // 此时，val == sign * significand * 2**exponent。

        /*
         * 特殊情况零，以抑制非终止规范化和错误的比例计算。
         */
        if (significand == 0) {
            this.intVal = BigInteger.ZERO;
            this.scale = 0;
            this.intCompact = 0;
            this.precision = 1;
            return;
        }
        // 规范化
        while ((significand & 1) == 0) { // 即，significand 是偶数
            significand >>= 1;
            exponent++;
        }
        int scale = 0;
        // 计算 intVal 和 scale
        BigInteger intVal;
        long compactVal = sign * significand;
        if (exponent == 0) {
            intVal = (compactVal == INFLATED) ? INFLATED_BIGINT : null;
        } else {
            if (exponent < 0) {
                intVal = BigInteger.valueOf(5).pow(-exponent).multiply(compactVal);
                scale = -exponent;
            } else { //  (exponent > 0)
                intVal = BigInteger.valueOf(2).pow(exponent).multiply(compactVal);
            }
            compactVal = compactValFor(intVal);
        }
        int prec = 0;
        int mcp = mc.precision;
        if (mcp > 0) { // 进行舍入
            int mode = mc.roundingMode.oldMode;
            int drop;
            if (compactVal == INFLATED) {
                prec = bigDigitLength(intVal);
                drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    intVal = divideAndRoundByTenPow(intVal, drop, mode);
                    compactVal = compactValFor(intVal);
                    if (compactVal != INFLATED) {
                        break;
                    }
                    prec = bigDigitLength(intVal);
                    drop = prec - mcp;
                }
            }
            if (compactVal != INFLATED) {
                prec = longDigitLength(compactVal);
                drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                    prec = longDigitLength(compactVal);
                    drop = prec - mcp;
                }
                intVal = null;
            }
        }
        this.intVal = intVal;
        this.intCompact = compactVal;
        this.scale = scale;
        this.precision = prec;
    }


                /**
     * 将 {@code BigInteger} 转换为 {@code BigDecimal}。
     * {@code BigDecimal} 的比例为零。
     *
     * @param val 要转换为 {@code BigDecimal} 的 {@code BigInteger} 值。
     */
    public BigDecimal(BigInteger val) {
        scale = 0;
        intVal = val;
        intCompact = compactValFor(val);
    }

    /**
     * 根据上下文设置将 {@code BigInteger} 转换为 {@code BigDecimal} 并进行舍入。 {@code BigDecimal} 的比例为零。
     *
     * @param val 要转换为 {@code BigDecimal} 的 {@code BigInteger} 值。
     * @param  mc 要使用的上下文。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}。
     * @since  1.5
     */
    public BigDecimal(BigInteger val, MathContext mc) {
        this(val,0,mc);
    }

    /**
     * 将 {@code BigInteger} 无比例值和 {@code int} 比例转换为 {@code BigDecimal}。 {@code BigDecimal} 的值为
     * <tt>(unscaledVal &times; 10<sup>-scale</sup>)</tt>。
     *
     * @param unscaledVal {@code BigDecimal} 的无比例值。
     * @param scale {@code BigDecimal} 的比例。
     */
    public BigDecimal(BigInteger unscaledVal, int scale) {
        // 允许负比例
        this.intVal = unscaledVal;
        this.intCompact = compactValFor(unscaledVal);
        this.scale = scale;
    }

    /**
     * 将 {@code BigInteger} 无比例值和 {@code int} 比例转换为 {@code BigDecimal}，并根据上下文设置进行舍入。 {@code BigDecimal} 的值为
     * <tt>(unscaledVal &times; 10<sup>-scale</sup>)</tt>，并根据 {@code precision} 和舍入模式设置进行舍入。
     *
     * @param  unscaledVal {@code BigDecimal} 的无比例值。
     * @param  scale {@code BigDecimal} 的比例。
     * @param  mc 要使用的上下文。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}。
     * @since  1.5
     */
    public BigDecimal(BigInteger unscaledVal, int scale, MathContext mc) {
        long compactVal = compactValFor(unscaledVal);
        int mcp = mc.precision;
        int prec = 0;
        if (mcp > 0) { // 进行舍入
            int mode = mc.roundingMode.oldMode;
            if (compactVal == INFLATED) {
                prec = bigDigitLength(unscaledVal);
                int drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    unscaledVal = divideAndRoundByTenPow(unscaledVal, drop, mode);
                    compactVal = compactValFor(unscaledVal);
                    if (compactVal != INFLATED) {
                        break;
                    }
                    prec = bigDigitLength(unscaledVal);
                    drop = prec - mcp;
                }
            }
            if (compactVal != INFLATED) {
                prec = longDigitLength(compactVal);
                int drop = prec - mcp;     // drop 不能超过 18
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mode);
                    prec = longDigitLength(compactVal);
                    drop = prec - mcp;
                }
                unscaledVal = null;
            }
        }
        this.intVal = unscaledVal;
        this.intCompact = compactVal;
        this.scale = scale;
        this.precision = prec;
    }

    /**
     * 将 {@code int} 转换为 {@code BigDecimal}。 {@code BigDecimal} 的比例为零。
     *
     * @param val 要转换为 {@code BigDecimal} 的 {@code int} 值。
     * @since  1.5
     */
    public BigDecimal(int val) {
        this.intCompact = val;
        this.scale = 0;
        this.intVal = null;
    }

    /**
     * 将 {@code int} 转换为 {@code BigDecimal}，并根据上下文设置进行舍入。 {@code BigDecimal} 的比例在任何舍入之前为零。
     *
     * @param  val 要转换为 {@code BigDecimal} 的 {@code int} 值。
     * @param  mc 要使用的上下文。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}。
     * @since  1.5
     */
    public BigDecimal(int val, MathContext mc) {
        int mcp = mc.precision;
        long compactVal = val;
        int scale = 0;
        int prec = 0;
        if (mcp > 0) { // 进行舍入
            prec = longDigitLength(compactVal);
            int drop = prec - mcp; // drop 不能超过 18
            while (drop > 0) {
                scale = checkScaleNonZero((long) scale - drop);
                compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                prec = longDigitLength(compactVal);
                drop = prec - mcp;
            }
        }
        this.intVal = null;
        this.intCompact = compactVal;
        this.scale = scale;
        this.precision = prec;
    }

    /**
     * 将 {@code long} 转换为 {@code BigDecimal}。 {@code BigDecimal} 的比例为零。
     *
     * @param val 要转换为 {@code BigDecimal} 的 {@code long} 值。
     * @since  1.5
     */
    public BigDecimal(long val) {
        this.intCompact = val;
        this.intVal = (val == INFLATED) ? INFLATED_BIGINT : null;
        this.scale = 0;
    }

    /**
     * 将 {@code long} 转换为 {@code BigDecimal}，并根据上下文设置进行舍入。 {@code BigDecimal} 的比例在任何舍入之前为零。
     *
     * @param  val 要转换为 {@code BigDecimal} 的 {@code long} 值。
     * @param  mc 要使用的上下文。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}。
     * @since  1.5
     */
    public BigDecimal(long val, MathContext mc) {
        int mcp = mc.precision;
        int mode = mc.roundingMode.oldMode;
        int prec = 0;
        int scale = 0;
        BigInteger intVal = (val == INFLATED) ? INFLATED_BIGINT : null;
        if (mcp > 0) { // 进行舍入
            if (val == INFLATED) {
                prec = 19;
                int drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    intVal = divideAndRoundByTenPow(intVal, drop, mode);
                    val = compactValFor(intVal);
                    if (val != INFLATED) {
                        break;
                    }
                    prec = bigDigitLength(intVal);
                    drop = prec - mcp;
                }
            }
            if (val != INFLATED) {
                prec = longDigitLength(val);
                int drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    val = divideAndRound(val, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                    prec = longDigitLength(val);
                    drop = prec - mcp;
                }
                intVal = null;
            }
        }
        this.intVal = intVal;
        this.intCompact = val;
        this.scale = scale;
        this.precision = prec;
    }

                // Static Factory Methods

    /**
     * 将一个 {@code long} 无缩放值和一个 {@code int} 缩放值转换为一个 {@code BigDecimal}。这个
     * {@literal "静态工厂方法"} 优先于 ({@code long}, {@code int}) 构造函数提供，因为它
     * 允许重用频繁使用的 {@code BigDecimal} 值。
     *
     * @param unscaledVal {@code BigDecimal} 的无缩放值。
     * @param scale {@code BigDecimal} 的缩放值。
     * @return 一个 {@code BigDecimal}，其值为
     *         <tt>(unscaledVal &times; 10<sup>-scale</sup>)</tt>。
     */
    public static BigDecimal valueOf(long unscaledVal, int scale) {
        if (scale == 0)
            return valueOf(unscaledVal);
        else if (unscaledVal == 0) {
            return zeroValueOf(scale);
        }
        return new BigDecimal(unscaledVal == INFLATED ?
                              INFLATED_BIGINT : null,
                              unscaledVal, scale, 0);
    }

    /**
     * 将一个 {@code long} 值转换为一个缩放值为零的 {@code BigDecimal}。这个
     * {@literal "静态工厂方法"} 优先于 ({@code long}) 构造函数提供，因为它
     * 允许重用频繁使用的 {@code BigDecimal} 值。
     *
     * @param val {@code BigDecimal} 的值。
     * @return 一个 {@code BigDecimal}，其值为 {@code val}。
     */
    public static BigDecimal valueOf(long val) {
        if (val >= 0 && val < zeroThroughTen.length)
            return zeroThroughTen[(int)val];
        else if (val != INFLATED)
            return new BigDecimal(null, val, 0, 0);
        return new BigDecimal(INFLATED_BIGINT, val, 0, 0);
    }

    static BigDecimal valueOf(long unscaledVal, int scale, int prec) {
        if (scale == 0 && unscaledVal >= 0 && unscaledVal < zeroThroughTen.length) {
            return zeroThroughTen[(int) unscaledVal];
        } else if (unscaledVal == 0) {
            return zeroValueOf(scale);
        }
        return new BigDecimal(unscaledVal == INFLATED ? INFLATED_BIGINT : null,
                unscaledVal, scale, prec);
    }

    static BigDecimal valueOf(BigInteger intVal, int scale, int prec) {
        long val = compactValFor(intVal);
        if (val == 0) {
            return zeroValueOf(scale);
        } else if (scale == 0 && val >= 0 && val < zeroThroughTen.length) {
            return zeroThroughTen[(int) val];
        }
        return new BigDecimal(intVal, val, scale, prec);
    }

    static BigDecimal zeroValueOf(int scale) {
        if (scale >= 0 && scale < ZERO_SCALED_BY.length)
            return ZERO_SCALED_BY[scale];
        else
            return new BigDecimal(BigInteger.ZERO, 0, scale, 1);
    }

    /**
     * 使用 {@code double} 的规范字符串表示（由 {@link Double#toString(double)} 方法提供）将
     * {@code double} 转换为 {@code BigDecimal}。
     *
     * <p><b>注意：</b> 通常这是将 {@code double}（或 {@code float}）转换为
     * {@code BigDecimal} 的首选方法，因为返回的值等于或近似等于从使用
     * {@link Double#toString(double)} 的结果构造的 {@code BigDecimal} 的值。
     *
     * @param  val 要转换为 {@code BigDecimal} 的 {@code double}。
     * @return 一个 {@code BigDecimal}，其值等于或近似等于 {@code val} 的值。
     * @throws NumberFormatException 如果 {@code val} 是无穷大或 NaN。
     * @since  1.5
     */
    public static BigDecimal valueOf(double val) {
        // 提醒：零 double 返回 '0.0'，因此我们不能快速路径使用常量 ZERO。这可能足够重要，值得采用
        // 工厂方法、缓存或一些私有常量，稍后。
        return new BigDecimal(Double.toString(val));
    }

    // 算术运算
    /**
     * 返回一个 {@code BigDecimal}，其值为 {@code (this + augend)}，其缩放值为
     * {@code max(this.scale(), augend.scale())}。
     *
     * @param  augend 要加到这个 {@code BigDecimal} 上的值。
     * @return {@code this + augend}
     */
    public BigDecimal add(BigDecimal augend) {
        if (this.intCompact != INFLATED) {
            if ((augend.intCompact != INFLATED)) {
                return add(this.intCompact, this.scale, augend.intCompact, augend.scale);
            } else {
                return add(this.intCompact, this.scale, augend.intVal, augend.scale);
            }
        } else {
            if ((augend.intCompact != INFLATED)) {
                return add(augend.intCompact, augend.scale, this.intVal, this.scale);
            } else {
                return add(this.intVal, this.scale, augend.intVal, augend.scale);
            }
        }
    }

    /**
     * 返回一个 {@code BigDecimal}，其值为 {@code (this + augend)}，并根据上下文设置进行舍入。
     *
     * 如果任一数字为零且精度设置非零，则使用另一个数字（必要时进行舍入）作为结果。
     *
     * @param  augend 要加到这个 {@code BigDecimal} 上的值。
     * @param  mc 要使用的上下文。
     * @return {@code this + augend}，必要时进行舍入。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}。
     * @since  1.5
     */
    public BigDecimal add(BigDecimal augend, MathContext mc) {
        if (mc.precision == 0)
            return add(augend);
        BigDecimal lhs = this;

        // 如果任一数字为零，则使用另一个数字（必要时进行舍入和缩放）作为结果。
        {
            boolean lhsIsZero = lhs.signum() == 0;
            boolean augendIsZero = augend.signum() == 0;

            if (lhsIsZero || augendIsZero) {
                int preferredScale = Math.max(lhs.scale(), augend.scale());
                BigDecimal result;

                if (lhsIsZero && augendIsZero)
                    return zeroValueOf(preferredScale);
                result = lhsIsZero ? doRound(augend, mc) : doRound(lhs, mc);


                            if (result.scale() == preferredScale)
                    return result;
                else if (result.scale() > preferredScale) {
                    return stripZerosToMatchScale(result.intVal, result.intCompact, result.scale, preferredScale);
                } else { // result.scale < preferredScale
                    int precisionDiff = mc.precision - result.precision();
                    int scaleDiff     = preferredScale - result.scale();

                    if (precisionDiff >= scaleDiff)
                        return result.setScale(preferredScale); // can achieve target scale
                    else
                        return result.setScale(result.scale() + precisionDiff);
                }
            }
        }

        long padding = (long) lhs.scale - augend.scale;
        if (padding != 0) { // scales differ; alignment needed
            BigDecimal arg[] = preAlign(lhs, augend, padding, mc);
            matchScale(arg);
            lhs = arg[0];
            augend = arg[1];
        }
        return doRound(lhs.inflated().add(augend.inflated()), lhs.scale, mc);
    }

    /**
     * 返回一个长度为二的数组，其元素之和等于 {@code BigDecimal} 参数的四舍五入后的和。
     *
     * <p>如果参数的数字位置之间有足够的差距，较小的值可以被浓缩成一个 {@literal "sticky bit"}，并且最终结果的精度不包括小值的最高有效数字时，最终结果将四舍五入。
     *
     * <p>虽然严格来说这是一个优化，但它使更广泛的加法变得实际。
     *
     * <p>这对应于固定精度浮点加法器中的预移位操作；此方法因结果的可变精度而复杂，该精度由 MathContext 确定。更精细的操作可以对较小的值进行 {@literal "右移"}，即使有效数部分重叠，也可以减少较小值的位数。
     */
    private BigDecimal[] preAlign(BigDecimal lhs, BigDecimal augend, long padding, MathContext mc) {
        assert padding != 0;
        BigDecimal big;
        BigDecimal small;

        if (padding < 0) { // lhs 较大；augend 较小
            big = lhs;
            small = augend;
        } else { // lhs 较小；augend 较大
            big = augend;
            small = lhs;
        }

        /*
         * 这是结果的 ulp 的估计尺度；它假定结果在真实加法中没有进位（例如 999 + 1 => 1000）或任何借位（例如 100 - 1.2 => 98.8）
         */
        long estResultUlpScale = (long) big.scale - big.precision() + mc.precision;

        /*
         * big 的低阶数字位置是 big.scale()。这无论 big 的尺度是正还是负都是正确的。small 的高阶数字位置是 small.scale - (small.precision() - 1)。为了进行完全的浓缩，big 和 small 的数字位置必须是不相交的，并且 small 的数字位置不应直接在结果中可见。
         */
        long smallHighDigitPos = (long) small.scale - small.precision() + 1;
        if (smallHighDigitPos > big.scale + 2 && // big 和 small 不相交
            smallHighDigitPos > estResultUlpScale + 2) { // small 的数字位置不直接可见
            small = BigDecimal.valueOf(small.signum(), this.checkScale(Math.max(big.scale, estResultUlpScale) + 3));
        }

        // 由于加法是对称的，返回的操作数的输入顺序并不重要
        BigDecimal[] result = {big, small};
        return result;
    }

    /**
     * 返回一个 {@code BigDecimal}，其值为 {@code (this - subtrahend)}，其尺度为 {@code max(this.scale(), subtrahend.scale())}。
     *
     * @param  subtrahend 要从这个 {@code BigDecimal} 中减去的值。
     * @return {@code this - subtrahend}
     */
    public BigDecimal subtract(BigDecimal subtrahend) {
        if (this.intCompact != INFLATED) {
            if ((subtrahend.intCompact != INFLATED)) {
                return add(this.intCompact, this.scale, -subtrahend.intCompact, subtrahend.scale);
            } else {
                return add(this.intCompact, this.scale, subtrahend.intVal.negate(), subtrahend.scale);
            }
        } else {
            if ((subtrahend.intCompact != INFLATED)) {
                // 为了避免在专用的 add 方法中需要方法重载，先给出 subtrahend 的值对
                return add(-subtrahend.intCompact, subtrahend.scale, this.intVal, this.scale);
            } else {
                return add(this.intVal, this.scale, subtrahend.intVal.negate(), subtrahend.scale);
            }
        }
    }

    /**
     * 返回一个 {@code BigDecimal}，其值为 {@code (this - subtrahend)}，并根据上下文设置进行四舍五入。
     *
     * 如果 {@code subtrahend} 为零，则使用必要时四舍五入的 this 作为结果。如果 this 为零，则结果为 {@code subtrahend.negate(mc)}。
     *
     * @param  subtrahend 要从这个 {@code BigDecimal} 中减去的值。
     * @param  mc 要使用的上下文。
     * @return {@code this - subtrahend}，必要时四舍五入。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}。
     * @since  1.5
     */
    public BigDecimal subtract(BigDecimal subtrahend, MathContext mc) {
        if (mc.precision == 0)
            return subtract(subtrahend);
        // 共享 add() 中的特殊四舍五入代码
        return add(subtrahend.negate(), mc);
    }

    /**
     * 返回一个 {@code BigDecimal}，其值为 <tt>(this &times; multiplicand)</tt>，其尺度为 {@code (this.scale() + multiplicand.scale())}。
     *
     * @param  multiplicand 要与这个 {@code BigDecimal} 相乘的值。
     * @return {@code this * multiplicand}
     */
    public BigDecimal multiply(BigDecimal multiplicand) {
        int productScale = checkScale((long) scale + multiplicand.scale);
        if (this.intCompact != INFLATED) {
            if ((multiplicand.intCompact != INFLATED)) {
                return multiply(this.intCompact, multiplicand.intCompact, productScale);
            } else {
                return multiply(this.intCompact, multiplicand.intVal, productScale);
            }
        } else {
            if ((multiplicand.intCompact != INFLATED)) {
                return multiply(multiplicand.intCompact, this.intVal, productScale);
            } else {
                return multiply(this.intVal, multiplicand.intVal, productScale);
            }
        }
    }


/**
 * 返回一个其值为 <tt>(this &times; multiplicand)</tt> 的 {@code BigDecimal}，并根据上下文设置进行舍入。
 *
 * @param  multiplicand 要与这个 {@code BigDecimal} 相乘的值。
 * @param  mc 使用的上下文。
 * @return {@code this * multiplicand}，必要时进行舍入。
 * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}。
 * @since  1.5
 */
public BigDecimal multiply(BigDecimal multiplicand, MathContext mc) {
    if (mc.precision == 0)
        return multiply(multiplicand);
    int productScale = checkScale((long) scale + multiplicand.scale);
    if (this.intCompact != INFLATED) {
        if ((multiplicand.intCompact != INFLATED)) {
            return multiplyAndRound(this.intCompact, multiplicand.intCompact, productScale, mc);
        } else {
            return multiplyAndRound(this.intCompact, multiplicand.intVal, productScale, mc);
        }
    } else {
        if ((multiplicand.intCompact != INFLATED)) {
            return multiplyAndRound(multiplicand.intCompact, this.intVal, productScale, mc);
        } else {
            return multiplyAndRound(this.intVal, multiplicand.intVal, productScale, mc);
        }
    }
}

/**
 * 返回一个其值为 {@code (this / divisor)} 的 {@code BigDecimal}，并且其标度如指定。如果必须进行舍入以生成具有指定标度的结果，则应用指定的舍入模式。
 *
 * <p>应优先使用新的 {@link #divide(BigDecimal, int, RoundingMode)} 方法，而不是此旧方法。
 *
 * @param  divisor 要除以的值。
 * @param  scale 要返回的 {@code BigDecimal} 商的标度。
 * @param  roundingMode 应用的舍入模式。
 * @return {@code this / divisor}
 * @throws ArithmeticException 如果 {@code divisor} 为零，或者 {@code roundingMode==ROUND_UNNECESSARY} 且指定的标度不足以精确表示除法的结果。
 * @throws IllegalArgumentException 如果 {@code roundingMode} 不表示有效的舍入模式。
 * @see    #ROUND_UP
 * @see    #ROUND_DOWN
 * @see    #ROUND_CEILING
 * @see    #ROUND_FLOOR
 * @see    #ROUND_HALF_UP
 * @see    #ROUND_HALF_DOWN
 * @see    #ROUND_HALF_EVEN
 * @see    #ROUND_UNNECESSARY
 */
public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode) {
    if (roundingMode < ROUND_UP || roundingMode > ROUND_UNNECESSARY)
        throw new IllegalArgumentException("Invalid rounding mode");
    if (this.intCompact != INFLATED) {
        if ((divisor.intCompact != INFLATED)) {
            return divide(this.intCompact, this.scale, divisor.intCompact, divisor.scale, scale, roundingMode);
        } else {
            return divide(this.intCompact, this.scale, divisor.intVal, divisor.scale, scale, roundingMode);
        }
    } else {
        if ((divisor.intCompact != INFLATED)) {
            return divide(this.intVal, this.scale, divisor.intCompact, divisor.scale, scale, roundingMode);
        } else {
            return divide(this.intVal, this.scale, divisor.intVal, divisor.scale, scale, roundingMode);
        }
    }
}

/**
 * 返回一个其值为 {@code (this / divisor)} 的 {@code BigDecimal}，并且其标度如指定。如果必须进行舍入以生成具有指定标度的结果，则应用指定的舍入模式。
 *
 * @param  divisor 要除以的值。
 * @param  scale 要返回的 {@code BigDecimal} 商的标度。
 * @param  roundingMode 应用的舍入模式。
 * @return {@code this / divisor}
 * @throws ArithmeticException 如果 {@code divisor} 为零，或者 {@code roundingMode==RoundingMode.UNNECESSARY} 且指定的标度不足以精确表示除法的结果。
 * @since 1.5
 */
public BigDecimal divide(BigDecimal divisor, int scale, RoundingMode roundingMode) {
    return divide(divisor, scale, roundingMode.oldMode);
}

/**
 * 返回一个其值为 {@code (this / divisor)} 的 {@code BigDecimal}，并且其标度为 {@code this.scale()}。如果必须进行舍入以生成具有给定标度的结果，则应用指定的舍入模式。
 *
 * <p>应优先使用新的 {@link #divide(BigDecimal, RoundingMode)} 方法，而不是此旧方法。
 *
 * @param  divisor 要除以的值。
 * @param  roundingMode 应用的舍入模式。
 * @return {@code this / divisor}
 * @throws ArithmeticException 如果 {@code divisor==0}，或者 {@code roundingMode==ROUND_UNNECESSARY} 且 {@code this.scale()} 不足以精确表示除法的结果。
 * @throws IllegalArgumentException 如果 {@code roundingMode} 不表示有效的舍入模式。
 * @see    #ROUND_UP
 * @see    #ROUND_DOWN
 * @see    #ROUND_CEILING
 * @see    #ROUND_FLOOR
 * @see    #ROUND_HALF_UP
 * @see    #ROUND_HALF_DOWN
 * @see    #ROUND_HALF_EVEN
 * @see    #ROUND_UNNECESSARY
 */
public BigDecimal divide(BigDecimal divisor, int roundingMode) {
    return this.divide(divisor, scale, roundingMode);
}

/**
 * 返回一个其值为 {@code (this / divisor)} 的 {@code BigDecimal}，并且其标度为 {@code this.scale()}。如果必须进行舍入以生成具有给定标度的结果，则应用指定的舍入模式。
 *
 * @param  divisor 要除以的值。
 * @param  roundingMode 应用的舍入模式。
 * @return {@code this / divisor}
 * @throws ArithmeticException 如果 {@code divisor==0}，或者 {@code roundingMode==RoundingMode.UNNECESSARY} 且 {@code this.scale()} 不足以精确表示除法的结果。
 * @since 1.5
 */
public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode) {
    return this.divide(divisor, scale, roundingMode.oldMode);
}


                /**
     * 返回一个 {@code BigDecimal}，其值为 {@code (this / divisor)}，其首选比例为 {@code (this.scale() - divisor.scale())}；
     * 如果精确商无法表示（因为它具有非终止的十进制扩展），则抛出 {@code ArithmeticException}。
     *
     * @param  divisor 用于除以这个 {@code BigDecimal} 的值。
     * @throws ArithmeticException 如果精确商没有终止的十进制扩展
     * @return {@code this / divisor}
     * @since 1.5
     * @author Joseph D. Darcy
     */
    public BigDecimal divide(BigDecimal divisor) {
        /*
         * 首先处理零的情况。
         */
        if (divisor.signum() == 0) {   // x/0
            if (this.signum() == 0)    // 0/0
                throw new ArithmeticException("除法未定义");  // NaN
            throw new ArithmeticException("除以零");
        }

        // 计算首选比例
        int preferredScale = saturateLong((long) this.scale - divisor.scale);

        if (this.signum() == 0) // 0/y
            return zeroValueOf(preferredScale);
        else {
            /*
             * 如果商 this/divisor 有终止的十进制扩展，该扩展的数字不会超过
             * (a.precision() + ceil(10*b.precision)/3) 位。
             * 因此，创建一个具有此精度的 MathContext 对象，并使用 UNNECESSARY 舍入模式进行除法。
             */
            MathContext mc = new MathContext( (int)Math.min(this.precision() +
                                                            (long)Math.ceil(10.0*divisor.precision()/3.0),
                                                            Integer.MAX_VALUE),
                                              RoundingMode.UNNECESSARY);
            BigDecimal quotient;
            try {
                quotient = this.divide(divisor, mc);
            } catch (ArithmeticException e) {
                throw new ArithmeticException("非终止的十进制扩展；" +
                                              "没有精确的可表示的十进制结果。");
            }

            int quotientScale = quotient.scale();

            // divide(BigDecimal, mc) 试图通过移除尾随零来调整商到所需的一个
            // 由于精确除法方法没有显式的数字限制，我们也可以添加零。
            if (preferredScale > quotientScale)
                return quotient.setScale(preferredScale, ROUND_UNNECESSARY);

            return quotient;
        }
    }

    /**
     * 返回一个 {@code BigDecimal}，其值为 {@code (this / divisor)}，根据上下文设置进行舍入。
     *
     * @param  divisor 用于除以这个 {@code BigDecimal} 的值。
     * @param  mc 要使用的上下文。
     * @return {@code this / divisor}，必要时进行舍入。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY} 或
     *         {@code mc.precision == 0} 且商具有非终止的十进制扩展。
     * @since  1.5
     */
    public BigDecimal divide(BigDecimal divisor, MathContext mc) {
        int mcp = mc.precision;
        if (mcp == 0)
            return divide(divisor);

        BigDecimal dividend = this;
        long preferredScale = (long)dividend.scale - divisor.scale;
        // 现在计算答案。我们使用现有的
        // divide-and-round 方法，但由于此方法按比例舍入，我们
        // 必须在此处对值进行归一化以获得所需的结果。
        // 对于 x/y，我们首先处理 y=0 和 x=0，然后归一化 x 和
        // y 以给出 x' 和 y'，满足以下约束：
        //   (a) 0.1 <= x' < 1
        //   (b)  x' <= y' < 10*x'
        // 以 mc.precision 为所需比例除以 x'/y' 将
        // 给出一个范围在 0.1 到 1 之间的结果，精确到
        // 正确的位数（除非结果为 1.000...，这可能在 x=y 或舍入溢出时出现
        // 1.000... 情况将正确减少到 1。
        if (divisor.signum() == 0) {      // x/0
            if (dividend.signum() == 0)    // 0/0
                throw new ArithmeticException("除法未定义");  // NaN
            throw new ArithmeticException("除以零");
        }
        if (dividend.signum() == 0) // 0/y
            return zeroValueOf(saturateLong(preferredScale));
        int xscale = dividend.precision();
        int yscale = divisor.precision();
        if(dividend.intCompact!=INFLATED) {
            if(divisor.intCompact!=INFLATED) {
                return divide(dividend.intCompact, xscale, divisor.intCompact, yscale, preferredScale, mc);
            } else {
                return divide(dividend.intCompact, xscale, divisor.intVal, yscale, preferredScale, mc);
            }
        } else {
            if(divisor.intCompact!=INFLATED) {
                return divide(dividend.intVal, xscale, divisor.intCompact, yscale, preferredScale, mc);
            } else {
                return divide(dividend.intVal, xscale, divisor.intVal, yscale, preferredScale, mc);
            }
        }
    }

    /**
     * 返回一个 {@code BigDecimal}，其值为商 {@code (this / divisor)} 的整数部分，向下舍入。 
     * 结果的首选比例为 {@code (this.scale() - divisor.scale())}。
     *
     * @param  divisor 用于除以这个 {@code BigDecimal} 的值。
     * @return {@code this / divisor} 的整数部分。
     * @throws ArithmeticException 如果 {@code divisor==0}
     * @since  1.5
     */
    public BigDecimal divideToIntegralValue(BigDecimal divisor) {
        // 计算首选比例
        int preferredScale = saturateLong((long) this.scale - divisor.scale);
        if (this.compareMagnitude(divisor) < 0) {
            // 当 this << divisor 时，速度更快
            return zeroValueOf(preferredScale);
        }


                    if (this.signum() == 0 && divisor.signum() != 0)
            return this.setScale(preferredScale, ROUND_UNNECESSARY);

        // 执行除法，确保有足够的位数来四舍五入到正确的整数值；然后移除任何小数位

        int maxDigits = (int)Math.min(this.precision() +
                                      (long)Math.ceil(10.0*divisor.precision()/3.0) +
                                      Math.abs((long)this.scale() - divisor.scale()) + 2,
                                      Integer.MAX_VALUE);
        BigDecimal quotient = this.divide(divisor, new MathContext(maxDigits,
                                                                   RoundingMode.DOWN));
        if (quotient.scale > 0) {
            quotient = quotient.setScale(0, RoundingMode.DOWN);
            quotient = stripZerosToMatchScale(quotient.intVal, quotient.intCompact, quotient.scale, preferredScale);
        }

        if (quotient.scale < preferredScale) {
            // 如果需要，用零填充
            quotient = quotient.setScale(preferredScale, ROUND_UNNECESSARY);
        }

        return quotient;
    }

    /**
     * 返回一个 {@code BigDecimal}，其值为 {@code (this / divisor)} 的整数部分。由于精确商的整数部分不依赖于舍入模式，
     * 因此舍入模式不会影响此方法返回的值。结果的首选比例为 {@code (this.scale() - divisor.scale())}。
     * 如果精确商的整数部分需要超过 {@code mc.precision} 位，则抛出 {@code ArithmeticException}。
     *
     * @param  divisor 用于除以当前 {@code BigDecimal} 的值。
     * @param  mc 要使用的上下文。
     * @return {@code this / divisor} 的整数部分。
     * @throws ArithmeticException 如果 {@code divisor==0}
     * @throws ArithmeticException 如果 {@code mc.precision} {@literal >} 0 且结果需要超过 {@code mc.precision} 位。
     * @since  1.5
     * @author Joseph D. Darcy
     */
    public BigDecimal divideToIntegralValue(BigDecimal divisor, MathContext mc) {
        if (mc.precision == 0 || // 精确结果
            (this.compareMagnitude(divisor) < 0)) // 结果为零
            return divideToIntegralValue(divisor);

        // 计算首选比例
        int preferredScale = saturateLong((long)this.scale - divisor.scale);

        /*
         * 执行正常除法到 mc.precision 位。如果余数的绝对值小于除数，则商的整数部分可以放入 mc.precision 位。
         * 接下来，从商中移除任何小数位，并将比例调整到首选值。
         */
        BigDecimal result = this.divide(divisor, new MathContext(mc.precision, RoundingMode.DOWN));

        if (result.scale() < 0) {
            /*
             * 结果是整数。检查商是否表示精确商的完整整数部分；如果是，则计算的余数将小于除数。
             */
            BigDecimal product = result.multiply(divisor);
            // 如果商是完整的整数值，
            // |dividend-product| < |divisor|.
            if (this.subtract(product).compareMagnitude(divisor) >= 0) {
                throw new ArithmeticException("Division impossible");
            }
        } else if (result.scale() > 0) {
            /*
             * 商的整数部分可以放入精度位；重新计算比例为 0 的商以避免双重舍入，然后在必要时进行调整。
             */
            result = result.setScale(0, RoundingMode.DOWN);
        }
        // else result.scale() == 0;

        int precisionDiff;
        if ((preferredScale > result.scale()) &&
            (precisionDiff = mc.precision - result.precision()) > 0) {
            return result.setScale(result.scale() +
                                   Math.min(precisionDiff, preferredScale - result.scale) );
        } else {
            return stripZerosToMatchScale(result.intVal,result.intCompact,result.scale,preferredScale);
        }
    }

    /**
     * 返回一个 {@code BigDecimal}，其值为 {@code (this % divisor)}。
     *
     * <p>余数由
     * {@code this.subtract(this.divideToIntegralValue(divisor).multiply(divisor))} 给出。
     * 注意这不是模运算（结果可以是负数）。
     *
     * @param  divisor 用于除以当前 {@code BigDecimal} 的值。
     * @return {@code this % divisor}。
     * @throws ArithmeticException 如果 {@code divisor==0}
     * @since  1.5
     */
    public BigDecimal remainder(BigDecimal divisor) {
        BigDecimal divrem[] = this.divideAndRemainder(divisor);
        return divrem[1];
    }


    /**
     * 返回一个 {@code BigDecimal}，其值为 {@code (this % divisor)}，并根据上下文设置进行舍入。
     * {@code MathContext} 设置影响用于计算余数的隐式除法。余数计算本身是精确的。因此，余数可能包含超过 {@code mc.getPrecision()} 位。
     *
     * <p>余数由
     * {@code this.subtract(this.divideToIntegralValue(divisor, mc).multiply(divisor))} 给出。
     * 注意这不是模运算（结果可以是负数）。
     *
     * @param  divisor 用于除以当前 {@code BigDecimal} 的值。
     * @param  mc 要使用的上下文。
     * @return {@code this % divisor}，必要时进行舍入。
     * @throws ArithmeticException 如果 {@code divisor==0}
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}，或 {@code mc.precision} {@literal >} 0 且
     *         {@code this.divideToIntgralValue(divisor)} 的结果需要超过 {@code mc.precision} 位。
     * @see    #divideToIntegralValue(java.math.BigDecimal, java.math.MathContext)
     * @since  1.5
     */
    public BigDecimal remainder(BigDecimal divisor, MathContext mc) {
        BigDecimal divrem[] = this.divideAndRemainder(divisor, mc);
        return divrem[1];
    }

                /**
     * 返回一个包含两个元素的 {@code BigDecimal} 数组，该数组包含两个操作数的
     * {@code divideToIntegralValue} 结果，后跟 {@code remainder} 的结果。
     *
     * <p>请注意，如果需要整数商和余数，此方法比分别使用
     * {@code divideToIntegralValue} 和 {@code remainder} 方法更快，因为只需执行一次除法。
     *
     * @param  divisor 用于除以此 {@code BigDecimal} 并计算余数的值。
     * @return 一个包含两个元素的 {@code BigDecimal} 数组：商（{@code divideToIntegralValue} 的结果）是第一个元素，
     *         余数是最后一个元素。
     * @throws ArithmeticException 如果 {@code divisor==0}
     * @see    #divideToIntegralValue(java.math.BigDecimal, java.math.MathContext)
     * @see    #remainder(java.math.BigDecimal, java.math.MathContext)
     * @since  1.5
     */
    public BigDecimal[] divideAndRemainder(BigDecimal divisor) {
        // 我们使用身份 x = i * y + r 来确定 r
        BigDecimal[] result = new BigDecimal[2];

        result[0] = this.divideToIntegralValue(divisor);
        result[1] = this.subtract(result[0].multiply(divisor));
        return result;
    }

    /**
     * 返回一个包含两个元素的 {@code BigDecimal} 数组，该数组包含两个操作数的
     * {@code divideToIntegralValue} 结果，后跟根据上下文设置进行舍入的 {@code remainder} 结果。
     *
     * <p>请注意，如果需要整数商和余数，此方法比分别使用
     * {@code divideToIntegralValue} 和 {@code remainder} 方法更快，因为只需执行一次除法。
     *
     * @param  divisor 用于除以此 {@code BigDecimal} 并计算余数的值。
     * @param  mc 使用的上下文。
     * @return 一个包含两个元素的 {@code BigDecimal} 数组：商（{@code divideToIntegralValue} 的结果）是
     *         第一个元素，余数是最后一个元素。
     * @throws ArithmeticException 如果 {@code divisor==0}
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}，或者 {@code mc.precision}
     *         {@literal >} 0 且 {@code this.divideToIntgralValue(divisor)} 的结果需要超过 {@code mc.precision} 位的精度。
     * @see    #divideToIntegralValue(java.math.BigDecimal, java.math.MathContext)
     * @see    #remainder(java.math.BigDecimal, java.math.MathContext)
     * @since  1.5
     */
    public BigDecimal[] divideAndRemainder(BigDecimal divisor, MathContext mc) {
        if (mc.precision == 0)
            return divideAndRemainder(divisor);

        BigDecimal[] result = new BigDecimal[2];
        BigDecimal lhs = this;

        result[0] = lhs.divideToIntegralValue(divisor, mc);
        result[1] = lhs.subtract(result[0].multiply(divisor));
        return result;
    }

    /**
     * 返回一个其值为 <tt>(this<sup>n</sup>)</tt> 的 {@code BigDecimal}，幂次计算精确，精度无限。
     *
     * <p>参数 {@code n} 必须在 0 到 999999999（包括）的范围内。 {@code ZERO.pow(0)} 返回 {@link
     * #ONE}。
     *
     * 注意，未来的版本可能会扩展此方法的允许指数范围。
     *
     * @param  n 要将此 {@code BigDecimal} 提升到的幂次。
     * @return <tt>this<sup>n</sup></tt>
     * @throws ArithmeticException 如果 {@code n} 超出范围。
     * @since  1.5
     */
    public BigDecimal pow(int n) {
        if (n < 0 || n > 999999999)
            throw new ArithmeticException("Invalid operation");
        // 如果结果会溢出或下溢，则无需计算 pow(n)。
        // 不尝试支持“超正常”数字。
        int newScale = checkScale((long)scale * n);
        return new BigDecimal(this.inflated().pow(n), newScale);
    }


    /**
     * 返回一个其值为 <tt>(this<sup>n</sup>)</tt> 的 {@code BigDecimal}。当前实现使用 ANSI 标准 X3.274-1996 中定义的核心算法，
     * 并根据上下文设置进行舍入。通常，返回的数值值在所选精度的两个 ulps 范围内。请注意，未来的版本可能会使用不同的算法，
     * 允许的误差范围减小，允许的指数范围增加。
     *
     * <p>X3.274-1996 算法是：
     *
     * <ul>
     * <li> 如果
     *  <ul>
     *    <li>{@code abs(n) > 999999999}
     *    <li>{@code mc.precision == 0} 且 {@code n < 0}
     *    <li>{@code mc.precision > 0} 且 {@code n} 的十进制数字超过
     *    {@code mc.precision}
     *  </ul>
     *  则抛出 {@code ArithmeticException} 异常。
     *
     * <li> 如果 {@code n} 为零，即使 {@code this} 为零，也返回 {@link #ONE}，否则
     * <ul>
     *   <li> 如果 {@code n} 为正数，则通过重复平方技术将结果计算到单个累加器中。累加器与单个乘法使用相同的数学上下文设置，
     *   但精度增加到 {@code mc.precision + elength + 1}，其中 {@code elength} 是 {@code n} 的十进制数字数。
     *
     *   <li> 如果 {@code n} 为负数，则结果计算为 {@code n} 为正数的情况；然后使用上述指定的工作精度将该值除以一。
     *
     *   <li> 从正数或负数情况得出的最终值然后四舍五入到目标精度。
     *   </ul>
     * </ul>
     *
     * @param  n 要将此 {@code BigDecimal} 提升到的幂次。
     * @param  mc 使用的上下文。
     * @return <tt>this<sup>n</sup></tt> 使用 ANSI 标准 X3.274-1996 算法
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}，或者 {@code n} 超出范围。
     * @since  1.5
     */
    public BigDecimal pow(int n, MathContext mc) {
        if (mc.precision == 0)
            return pow(n);
        if (n < -999999999 || n > 999999999)
            throw new ArithmeticException("Invalid operation");
        if (n == 0)
            return ONE;                      // x**0 == 1 in X3.274
        BigDecimal lhs = this;
        MathContext workmc = mc;           // 工作设置
        int mag = Math.abs(n);               // n 的绝对值
        if (mc.precision > 0) {
            int elength = longDigitLength(mag); // n 的十进制数字数
            if (elength > mc.precision)        // X3.274 规则
                throw new ArithmeticException("Invalid operation");
            workmc = new MathContext(mc.precision + elength + 1,
                                      mc.roundingMode);
        }
        // 准备执行幂次计算...
        BigDecimal acc = ONE;           // 累加器
        boolean seenbit = false;        // 一旦看到 1 位就设置
        for (int i=1;;i++) {            // 对每个位 [最高位被忽略]
            mag += mag;                 // 左移 1 位
            if (mag < 0) {              // 最高位被设置
                seenbit = true;         // 好的，我们开始了
                acc = acc.multiply(lhs, workmc); // acc=acc*x
            }
            if (i == 31)
                break;                  // 那是最后一位
            if (seenbit)
                acc=acc.multiply(acc, workmc);   // acc=acc*acc [平方]
                // else (!seenbit) 没有必要平方 ONE
        }
        // 如果 n 为负数，使用工作精度计算倒数
        if (n < 0) // [因此 mc.precision>0]
            acc=ONE.divide(acc, workmc);
        // 四舍五入到最终精度并删除零
        return doRound(acc, mc);
    }


    /**
     * 返回一个其值为该 {@code BigDecimal} 绝对值的 {@code BigDecimal}，其标度为
     * {@code this.scale()}。
     *
     * @return {@code abs(this)}
     */
    public BigDecimal abs() {
        return (signum() < 0 ? negate() : this);
    }

    /**
     * 返回一个其值为该 {@code BigDecimal} 绝对值的 {@code BigDecimal}，根据上下文设置进行舍入。
     *
     * @param mc 要使用的上下文。
     * @return {@code abs(this)}，必要时进行舍入。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}。
     * @since 1.5
     */
    public BigDecimal abs(MathContext mc) {
        return (signum() < 0 ? negate(mc) : plus(mc));
    }

    /**
     * 返回一个其值为 {@code (-this)} 的 {@code BigDecimal}，其标度为 {@code this.scale()}。
     *
     * @return {@code -this}。
     */
    public BigDecimal negate() {
        if (intCompact == INFLATED) {
            return new BigDecimal(intVal.negate(), INFLATED, scale, precision);
        } else {
            return valueOf(-intCompact, scale, precision);
        }
    }

    /**
     * 返回一个其值为 {@code (-this)} 的 {@code BigDecimal}，根据上下文设置进行舍入。
     *
     * @param mc 要使用的上下文。
     * @return {@code -this}，必要时进行舍入。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}。
     * @since  1.5
     */
    public BigDecimal negate(MathContext mc) {
        return negate().plus(mc);
    }

    /**
     * 返回一个其值为 {@code (+this)} 的 {@code BigDecimal}，其标度为 {@code this.scale()}。
     *
     * <p>此方法仅返回此 {@code BigDecimal}，是为了与一元减法方法 {@link
     * #negate()} 保持对称。
     *
     * @return {@code this}。
     * @see #negate()
     * @since  1.5
     */
    public BigDecimal plus() {
        return this;
    }

    /**
     * 返回一个其值为 {@code (+this)} 的 {@code BigDecimal}，根据上下文设置进行舍入。
     *
     * <p>此方法的效果与 {@link
     * #round(MathContext)} 方法相同。
     *
     * @param mc 要使用的上下文。
     * @return {@code this}，必要时进行舍入。 零结果的标度为 0。
     * @throws ArithmeticException 如果结果不精确但舍入模式为 {@code UNNECESSARY}。
     * @see    #round(MathContext)
     * @since  1.5
     */
    public BigDecimal plus(MathContext mc) {
        if (mc.precision == 0)                 // 不进行舍入
            return this;
        return doRound(this, mc);
    }

    /**
     * 返回该 {@code BigDecimal} 的符号函数值。
     *
     * @return -1, 0, 或 1，分别表示此 {@code BigDecimal} 的值为负、零或正。
     */
    public int signum() {
        return (intCompact != INFLATED)?
            Long.signum(intCompact):
            intVal.signum();
    }

    /**
     * 返回该 {@code BigDecimal} 的 <i>标度</i>。如果为零或正数，则标度是小数点右侧的数字位数。如果为负数，则未缩放值乘以 10 的标度绝对值次幂。例如，标度为 {@code -3} 表示未缩放值乘以 1000。
     *
     * @return 该 {@code BigDecimal} 的标度。
     */
    public int scale() {
        return scale;
    }

    /**
     * 返回该 {@code BigDecimal} 的 <i>精度</i>。（精度是未缩放值中的数字位数。）
     *
     * <p>零值的精度为 1。
     *
     * @return 该 {@code BigDecimal} 的精度。
     * @since  1.5
     */
    public int precision() {
        int result = precision;
        if (result == 0) {
            long s = intCompact;
            if (s != INFLATED)
                result = longDigitLength(s);
            else
                result = bigDigitLength(intVal);
            precision = result;
        }
        return result;
    }


    /**
     * 返回一个其值为该 {@code BigDecimal} 的 <i>未缩放值</i> 的 {@code BigInteger}。（计算 <tt>(this *
     * 10<sup>this.scale()</sup>)</tt>。）
     *
     * @return 该 {@code BigDecimal} 的未缩放值。
     * @since  1.2
     */
    public BigInteger unscaledValue() {
        return this.inflated();
    }

    // 舍入模式

    /**
     * 舍入模式为远离零舍入。始终增加非零丢弃小数前的数字。注意，此舍入模式从不减少计算值的大小。
     */
    public final static int ROUND_UP =           0;

    /**
     * 舍入模式为向零舍入。从不增加丢弃小数前的数字（即，截断）。注意，此舍入模式从不增加计算值的大小。
     */
    public final static int ROUND_DOWN =         1;

    /**
     * 舍入模式为向正无穷舍入。如果 {@code BigDecimal} 为正，则行为与 {@code ROUND_UP} 相同；如果为负，则行为与 {@code ROUND_DOWN} 相同。注意，此舍入模式从不减少计算值。
     */
    public final static int ROUND_CEILING =      2;

    /**
     * 舍入模式为向负无穷舍入。如果 {@code BigDecimal} 为正，则行为与 {@code ROUND_DOWN} 相同；如果为负，则行为与 {@code ROUND_UP} 相同。注意，此舍入模式从不增加计算值。
     */
    public final static int ROUND_FLOOR =        3;

    /**
     * 舍入模式为向“最近邻居”舍入，除非两个邻居等距，此时向上舍入。如果丢弃的小数部分 ≥ 0.5，则行为与 {@code ROUND_UP} 相同；否则，行为与 {@code ROUND_DOWN} 相同。注意，这是大多数人在小学时学到的舍入模式。
     */
    public final static int ROUND_HALF_UP =      4;

                /**
     * 向“最近邻居”舍入的模式，除非两个邻居等距，在这种情况下向下舍入。
     * 如果舍弃的小数部分 {@literal >} 0.5，则行为与 {@code ROUND_UP} 相同；否则，行为与 {@code ROUND_DOWN} 相同。
     */
    public final static int ROUND_HALF_DOWN =    5;

    /**
     * 向“最近邻居”舍入的模式，除非两个邻居等距，在这种情况下，向偶数邻居舍入。
     * 如果舍弃的小数部分左边的数字是奇数，则行为与 {@code ROUND_HALF_UP} 相同；如果是偶数，则行为与 {@code ROUND_HALF_DOWN} 相同。
     * 注意，这是在一系列计算中重复应用时最小化累积误差的舍入模式。
     */
    public final static int ROUND_HALF_EVEN =    6;

    /**
     * 断言请求的操作具有精确结果，因此不需要舍入的舍入模式。
     * 如果在这种舍入模式下操作的结果不精确，则抛出 {@code ArithmeticException}。
     */
    public final static int ROUND_UNNECESSARY =  7;


    // 缩放/舍入操作

    /**
     * 根据 {@code MathContext} 设置返回一个舍入的 {@code BigDecimal}。如果精度设置为 0，则不进行舍入。
     *
     * <p>此方法的效果与 {@link #plus(MathContext)} 方法完全相同。
     *
     * @param mc 要使用的上下文。
     * @return 根据 {@code MathContext} 设置舍入的 {@code BigDecimal}。
     * @throws ArithmeticException 如果舍入模式为 {@code UNNECESSARY} 且 {@code BigDecimal} 操作需要舍入。
     * @see    #plus(MathContext)
     * @since  1.5
     */
    public BigDecimal round(MathContext mc) {
        return plus(mc);
    }

    /**
     * 返回一个其标度为指定值的 {@code BigDecimal}，其非标度值通过乘以或除以适当的十的幂来确定，以保持其总体值不变。
     * 如果操作减少了标度，则非标度值必须被除（而不是乘），并且值可能会改变；在这种情况下，将应用指定的舍入模式。
     *
     * <p>请注意，由于 BigDecimal 对象是不可变的，因此调用此方法不会导致原始对象被修改，这与通常的方法命名约定不同，通常方法名为 <tt>set<i>X</i></tt> 会改变字段 <i>{@code X}</i>。
     * 相反，{@code setScale} 返回一个具有适当标度的对象；返回的对象可能是新分配的，也可能不是。
     *
     * @param  newScale 要返回的 {@code BigDecimal} 值的标度。
     * @param  roundingMode 要应用的舍入模式。
     * @return 一个其标度为指定值的 {@code BigDecimal}，其非标度值通过乘以或除以适当的十的幂来确定，以保持其总体值不变。
     * @throws ArithmeticException 如果 {@code roundingMode==UNNECESSARY} 且指定的缩放操作需要舍入。
     * @see    RoundingMode
     * @since  1.5
     */
    public BigDecimal setScale(int newScale, RoundingMode roundingMode) {
        return setScale(newScale, roundingMode.oldMode);
    }

    /**
     * 返回一个其标度为指定值的 {@code BigDecimal}，其非标度值通过乘以或除以适当的十的幂来确定，以保持其总体值不变。
     * 如果操作减少了标度，则非标度值必须被除（而不是乘），并且值可能会改变；在这种情况下，将应用指定的舍入模式。
     *
     * <p>请注意，由于 BigDecimal 对象是不可变的，因此调用此方法不会导致原始对象被修改，这与通常的方法命名约定不同，通常方法名为 <tt>set<i>X</i></tt> 会改变字段 <i>{@code X}</i>。
     * 相反，{@code setScale} 返回一个具有适当标度的对象；返回的对象可能是新分配的，也可能不是。
     *
     * <p>应优先使用新的 {@link #setScale(int, RoundingMode)} 方法，而不是此遗留方法。
     *
     * @param  newScale 要返回的 {@code BigDecimal} 值的标度。
     * @param  roundingMode 要应用的舍入模式。
     * @return 一个其标度为指定值的 {@code BigDecimal}，其非标度值通过乘以或除以适当的十的幂来确定，以保持其总体值不变。
     * @throws ArithmeticException 如果 {@code roundingMode==ROUND_UNNECESSARY} 且指定的缩放操作需要舍入。
     * @throws IllegalArgumentException 如果 {@code roundingMode} 不表示有效的舍入模式。
     * @see    #ROUND_UP
     * @see    #ROUND_DOWN
     * @see    #ROUND_CEILING
     * @see    #ROUND_FLOOR
     * @see    #ROUND_HALF_UP
     * @see    #ROUND_HALF_DOWN
     * @see    #ROUND_HALF_EVEN
     * @see    #ROUND_UNNECESSARY
     */
    public BigDecimal setScale(int newScale, int roundingMode) {
        if (roundingMode < ROUND_UP || roundingMode > ROUND_UNNECESSARY)
            throw new IllegalArgumentException("无效的舍入模式");

        int oldScale = this.scale;
        if (newScale == oldScale)        // 简单情况
            return this;
        if (this.signum() == 0)            // 零可以具有任何标度
            return zeroValueOf(newScale);
        if(this.intCompact!=INFLATED) {
            long rs = this.intCompact;
            if (newScale > oldScale) {
                int raise = checkScale((long) newScale - oldScale);
                if ((rs = longMultiplyPowerTen(rs, raise)) != INFLATED) {
                    return valueOf(rs,newScale);
                }
                BigInteger rb = bigMultiplyPowerTen(raise);
                return new BigDecimal(rb, INFLATED, newScale, (precision > 0) ? precision + raise : 0);
            } else {
                // newScale < oldScale -- 删除一些数字
                // 由于舍入的影响，无法预测精度。
                int drop = checkScale((long) oldScale - newScale);
                if (drop < LONG_TEN_POWERS_TABLE.length) {
                    return divideAndRound(rs, LONG_TEN_POWERS_TABLE[drop], newScale, roundingMode, newScale);
                } else {
                    return divideAndRound(this.inflated(), bigTenToThe(drop), newScale, roundingMode, newScale);
                }
            }
        } else {
            if (newScale > oldScale) {
                int raise = checkScale((long) newScale - oldScale);
                BigInteger rb = bigMultiplyPowerTen(this.intVal,raise);
                return new BigDecimal(rb, INFLATED, newScale, (precision > 0) ? precision + raise : 0);
            } else {
                // newScale < oldScale -- 删除一些数字
                // 由于舍入的影响，无法预测精度。
                int drop = checkScale((long) oldScale - newScale);
                if (drop < LONG_TEN_POWERS_TABLE.length)
                    return divideAndRound(this.intVal, LONG_TEN_POWERS_TABLE[drop], newScale, roundingMode,
                                          newScale);
                else
                    return divideAndRound(this.intVal,  bigTenToThe(drop), newScale, roundingMode, newScale);
            }
        }
    }

                /**
     * 返回一个其标度为指定值且数值与此 {@code BigDecimal} 数值相等的 {@code BigDecimal}。
     * 如果这不可能，则抛出一个 {@code ArithmeticException}。
     *
     * <p>此调用通常用于增加标度，在这种情况下，可以保证存在一个具有指定标度和正确值的 {@code BigDecimal}。
     * 该调用也可以用于减少标度，前提是调用者知道 {@code BigDecimal} 的分数部分末尾有足够的零（即，其整数值中的十的因子）
     * 以允许重新缩放而不改变其值。
     *
     * <p>此方法返回与两个参数版本的 {@code setScale} 相同的结果，但可以省去调用者在不相关的情况下指定舍入模式的麻烦。
     *
     * <p>请注意，由于 {@code BigDecimal} 对象是不可变的，因此调用此方法不会导致原始对象被修改，这与通常的约定相反，
     * 通常方法名为 <tt>set<i>X</i></tt> 的方法会修改字段 <i>{@code X}</i>。相反，{@code setScale} 返回一个具有适当标度的对象；
     * 返回的对象可能是新分配的，也可能不是。
     *
     * @param  newScale 要返回的 {@code BigDecimal} 值的标度。
     * @return 一个标度为指定值的 {@code BigDecimal}，其非标度值由乘以或除以此 {@code BigDecimal} 的非标度值的适当十的幂确定，以保持其总体值。
     * @throws ArithmeticException 如果指定的缩放操作需要舍入。
     * @see    #setScale(int, int)
     * @see    #setScale(int, RoundingMode)
     */
    public BigDecimal setScale(int newScale) {
        return setScale(newScale, ROUND_UNNECESSARY);
    }

    // 小数点移动操作

    /**
     * 返回一个等效于此 {@code BigDecimal} 但小数点向左移动 {@code n} 位的 {@code BigDecimal}。
     * 如果 {@code n} 非负，则调用仅将 {@code n} 加到标度上。
     * 如果 {@code n} 为负，则调用等效于 {@code movePointRight(-n)}。
     * 通过此调用返回的 {@code BigDecimal} 的值为 <tt>(this &times; 10<sup>-n</sup>)</tt>，标度为 {@code max(this.scale()+n, 0)}。
     *
     * @param  n 小数点向左移动的位数。
     * @return 一个等效于此 {@code BigDecimal} 但小数点向左移动 {@code n} 位的 {@code BigDecimal}。
     * @throws ArithmeticException 如果标度溢出。
     */
    public BigDecimal movePointLeft(int n) {
        // 不能使用 movePointRight(-n)，以防 n==Integer.MIN_VALUE
        int newScale = checkScale((long)scale + n);
        BigDecimal num = new BigDecimal(intVal, intCompact, newScale, 0);
        return num.scale < 0 ? num.setScale(0, ROUND_UNNECESSARY) : num;
    }

    /**
     * 返回一个等效于此 {@code BigDecimal} 但小数点向右移动 {@code n} 位的 {@code BigDecimal}。
     * 如果 {@code n} 非负，则调用仅将 {@code n} 从标度中减去。
     * 如果 {@code n} 为负，则调用等效于 {@code movePointLeft(-n)}。
     * 通过此调用返回的 {@code BigDecimal} 的值为 <tt>(this &times; 10<sup>n</sup>)</tt>，标度为 {@code max(this.scale()-n, 0)}。
     *
     * @param  n 小数点向右移动的位数。
     * @return 一个等效于此 {@code BigDecimal} 但小数点向右移动 {@code n} 位的 {@code BigDecimal}。
     * @throws ArithmeticException 如果标度溢出。
     */
    public BigDecimal movePointRight(int n) {
        // 不能使用 movePointLeft(-n)，以防 n==Integer.MIN_VALUE
        int newScale = checkScale((long)scale - n);
        BigDecimal num = new BigDecimal(intVal, intCompact, newScale, 0);
        return num.scale < 0 ? num.setScale(0, ROUND_UNNECESSARY) : num;
    }

    /**
     * 返回一个数值等于 ({@code this} * 10<sup>n</sup>) 的 {@code BigDecimal}。
     * 结果的标度为 {@code (this.scale() - n)}。
     *
     * @param n 乘以的十的幂的指数。
     * @return 一个数值等于 ({@code this} * 10<sup>n</sup>) 的 {@code BigDecimal}。
     * @throws ArithmeticException 如果标度超出 32 位整数的范围。
     *
     * @since 1.5
     */
    public BigDecimal scaleByPowerOfTen(int n) {
        return new BigDecimal(intVal, intCompact,
                              checkScale((long)scale - n), precision);
    }

    /**
     * 返回一个数值等于此 {@code BigDecimal} 但表示中去除了尾随零的 {@code BigDecimal}。
     * 例如，从 {@code BigDecimal} 值 {@code 600.0}（其 [{@code BigInteger}, {@code scale}] 组件等于 [6000, 1]）
     * 去除尾随零，将得到 {@code 6E2}，其 [{@code BigInteger}, {@code scale}] 组件等于 [6, -2]。
     * 如果此 {@code BigDecimal} 数值等于零，则返回 {@code BigDecimal.ZERO}。
     *
     * @return 一个数值等于此 {@code BigDecimal} 但去除了尾随零的 {@code BigDecimal}。
     * @since 1.5
     */
    public BigDecimal stripTrailingZeros() {
        if (intCompact == 0 || (intVal != null && intVal.signum() == 0)) {
            return BigDecimal.ZERO;
        } else if (intCompact != INFLATED) {
            return createAndStripZerosToMatchScale(intCompact, scale, Long.MIN_VALUE);
        } else {
            return createAndStripZerosToMatchScale(intVal, scale, Long.MIN_VALUE);
        }
    }

    // 比较操作

    /**
     * 将此 {@code BigDecimal} 与指定的 {@code BigDecimal} 进行比较。
     * 数值相等但标度不同的两个 {@code BigDecimal} 对象（如 2.0 和 2.00）被视为相等。
     * 提供此方法是为了优先于为每个布尔比较运算符（<, ==, >, >=, !=, <=）提供的单独方法。
     * 执行这些比较的建议用法是：{@code (x.compareTo(y)} &lt;<i>op</i>&gt; {@code 0)}，其中
     * &lt;<i>op</i>&gt; 是六个比较运算符之一。
     *
     * @param  val 要与此 {@code BigDecimal} 比较的 {@code BigDecimal}。
     * @return -1, 0, 或 1，分别表示此 {@code BigDecimal} 数值小于、等于或大于 {@code val}。
     */
    public int compareTo(BigDecimal val) {
        // 快速路径，用于标度相等且未膨胀的情况。
        if (scale == val.scale) {
            long xs = intCompact;
            long ys = val.intCompact;
            if (xs != INFLATED && ys != INFLATED)
                return xs != ys ? ((xs > ys) ? 1 : -1) : 0;
        }
        int xsign = this.signum();
        int ysign = val.signum();
        if (xsign != ysign)
            return (xsign > ysign) ? 1 : -1;
        if (xsign == 0)
            return 0;
        int cmp = compareMagnitude(val);
        return (xsign > 0) ? cmp : -cmp;
    }


                /**
     * 比较大小时忽略符号的版本。
     */
    private int compareMagnitude(BigDecimal val) {
        // 匹配比例，避免不必要的膨胀
        long ys = val.intCompact;
        long xs = this.intCompact;
        if (xs == 0)
            return (ys == 0) ? 0 : -1;
        if (ys == 0)
            return 1;

        long sdiff = (long)this.scale - val.scale;
        if (sdiff != 0) {
            // 如果（调整后的）指数不同，则避免匹配比例
            long xae = (long)this.precision() - this.scale;   // [-1]
            long yae = (long)val.precision() - val.scale;     // [-1]
            if (xae < yae)
                return -1;
            if (xae > yae)
                return 1;
            BigInteger rb = null;
            if (sdiff < 0) {
                // 故意让 sdiff <= Integer.MIN_VALUE 的情况通过。
                if ( sdiff > Integer.MIN_VALUE &&
                      (xs == INFLATED ||
                      (xs = longMultiplyPowerTen(xs, (int)-sdiff)) == INFLATED) &&
                     ys == INFLATED) {
                    rb = bigMultiplyPowerTen((int)-sdiff);
                    return rb.compareMagnitude(val.intVal);
                }
            } else { // sdiff > 0
                // 故意让 sdiff > Integer.MAX_VALUE 的情况通过。
                if ( sdiff <= Integer.MAX_VALUE &&
                      (ys == INFLATED ||
                      (ys = longMultiplyPowerTen(ys, (int)sdiff)) == INFLATED) &&
                     xs == INFLATED) {
                    rb = val.bigMultiplyPowerTen((int)sdiff);
                    return this.intVal.compareMagnitude(rb);
                }
            }
        }
        if (xs != INFLATED)
            return (ys != INFLATED) ? longCompareMagnitude(xs, ys) : -1;
        else if (ys != INFLATED)
            return 1;
        else
            return this.intVal.compareMagnitude(val.intVal);
    }

    /**
     * 将此 {@code BigDecimal} 与指定的 {@code Object} 进行等值比较。与 {@link
     * #compareTo(BigDecimal) compareTo} 不同，此方法仅在值和比例都相等时才认为两个
     * {@code BigDecimal} 对象相等（因此 2.0 与 2.00 通过此方法比较时不相等）。
     *
     * @param  x 要与之比较的 {@code Object}。
     * @return 如果且仅当指定的 {@code Object} 是一个值和比例都与这个
     *         {@code BigDecimal} 相等的 {@code BigDecimal}，则返回 {@code true}。
     * @see    #compareTo(java.math.BigDecimal)
     * @see    #hashCode
     */
    @Override
    public boolean equals(Object x) {
        if (!(x instanceof BigDecimal))
            return false;
        BigDecimal xDec = (BigDecimal) x;
        if (x == this)
            return true;
        if (scale != xDec.scale)
            return false;
        long s = this.intCompact;
        long xs = xDec.intCompact;
        if (s != INFLATED) {
            if (xs == INFLATED)
                xs = compactValFor(xDec.intVal);
            return xs == s;
        } else if (xs != INFLATED)
            return xs == compactValFor(this.intVal);

        return this.inflated().equals(xDec.inflated());
    }

    /**
     * 返回此 {@code BigDecimal} 与 {@code val} 中的较小值。
     *
     * @param  val 要计算最小值的值。
     * @return 值为这两个 {@code BigDecimal} 中较小的那个的 {@code BigDecimal}。如果它们相等，
     *         如 {@link #compareTo(BigDecimal) compareTo} 方法所定义的，则返回 {@code this}。
     * @see    #compareTo(java.math.BigDecimal)
     */
    public BigDecimal min(BigDecimal val) {
        return (compareTo(val) <= 0 ? this : val);
    }

    /**
     * 返回此 {@code BigDecimal} 与 {@code val} 中的较大值。
     *
     * @param  val 要计算最大值的值。
     * @return 值为这两个 {@code BigDecimal} 中较大的那个的 {@code BigDecimal}。如果它们相等，
     *         如 {@link #compareTo(BigDecimal) compareTo} 方法所定义的，则返回 {@code this}。
     * @see    #compareTo(java.math.BigDecimal)
     */
    public BigDecimal max(BigDecimal val) {
        return (compareTo(val) >= 0 ? this : val);
    }

    // 哈希函数

    /**
     * 返回此 {@code BigDecimal} 的哈希码。注意，数值相等但比例不同的两个 {@code BigDecimal} 对象
     * 通常 <i>不会</i> 有相同的哈希码。
     *
     * @return 此 {@code BigDecimal} 的哈希码。
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        if (intCompact != INFLATED) {
            long val2 = (intCompact < 0)? -intCompact : intCompact;
            int temp = (int)( ((int)(val2 >>> 32)) * 31  +
                              (val2 & LONG_MASK));
            return 31*((intCompact < 0) ?-temp:temp) + scale;
        } else
            return 31*intVal.hashCode() + scale;
    }

    // 格式转换器

    /**
     * 返回此 {@code BigDecimal} 的字符串表示形式，如果需要使用科学记数法。
     *
     * <p>创建 {@code BigDecimal} 的标准规范字符串形式，就像通过以下步骤：首先，将 {@code BigDecimal}
     * 未缩放值的绝对值转换为十进制字符串，使用字符 {@code '0'} 到 {@code '9'}，没有前导零（除非其值为零，
     * 在这种情况下使用单个 {@code '0'} 字符）。
     *
     * <p>接下来，计算一个 <i>调整后的指数</i>；这是负比例加上转换后的未缩放值的字符数减一。即，
     * {@code -scale+(ulength-1)}，其中 {@code ulength} 是未缩放值的绝对值在十进制数字中的长度
     * （其 <i>精度</i>）。
     *
     * <p>如果比例大于或等于零且调整后的指数大于或等于 {@code -6}，则该数字将转换为不使用指数记数法的字符形式。
     * 在这种情况下，如果比例为零，则不添加小数点，如果比例为正，则插入小数点，比例指定小数点右边的字符数。
     * 如果在插入小数点后没有字符位于小数点前，则添加一个常规的 {@code '0'} 字符。
     *
     * <p>否则（即，如果比例为负，或调整后的指数小于 {@code -6}），该数字将转换为使用指数记数法的字符形式。
     * 在这种情况下，如果转换后的 {@code BigInteger} 有多个数字，则在第一个数字后插入小数点。然后将字符形式的指数
     * 追加到转换后的未缩放值（可能带有插入的小数点）；这包括字母 {@code 'E'}，后跟调整后的指数转换为字符形式。
     * 后者为十进制，使用字符 {@code '0'} 到 {@code '9'}，没有前导零，并且总是以符号字符 {@code '-'}
     * (<tt>'&#92;u002D'</tt>) 开头，如果调整后的指数为负，则为 {@code '+'} (<tt>'&#92;u002B'</tt>)。
     *
     * <p>最后，如果未缩放值小于零，则整个字符串前缀一个减号字符 {@code '-'} (<tt>'&#92;u002D'</tt>)。
     * 如果未缩放值为零或正数，则不添加符号字符。
     *
     * <p><b>示例：</b>
     * <p>对于每个表示形式 [<i>未缩放值</i>, <i>比例</i>]，右侧显示结果字符串。
     * <pre>
     * [123,0]      "123"
     * [-123,0]     "-123"
     * [123,-1]     "1.23E+3"
     * [123,-3]     "1.23E+5"
     * [123,1]      "12.3"
     * [123,5]      "0.00123"
     * [123,10]     "1.23E-8"
     * [-123,12]    "-1.23E-10"
     * </pre>
     *
     * <b>注意事项：</b>
     * <ol>
     *
     * <li>每个可区分的 {@code BigDecimal} 值（未缩放值和比例）都有一个唯一的字符串表示形式作为
     * 使用 {@code toString} 转换的结果。如果将该字符串表示形式使用 {@link #BigDecimal(String)}
     * 构造函数转换回 {@code BigDecimal}，则将恢复原始值。
     *
     * <li>给定数字的生成字符串总是相同的；不受区域设置影响。这意味着它可以作为交换十进制数据的规范字符串表示形式，
     * 或作为哈希表的键等。区域设置敏感的数字格式化和解析由 {@link java.text.NumberFormat} 类及其子类处理。
     *
     * <li>可以使用 {@link #toEngineeringString} 方法以工程记数法表示带有指数的数字，可以使用
     * {@link #setScale(int,RoundingMode) setScale} 方法对 {@code BigDecimal} 进行舍入，使其在小数点后有已知的数字位数。
     *
     * <li>使用 {@code Character.forDigit} 提供的数字到字符的映射。
     *
     * </ol>
     *
     * @return 此 {@code BigDecimal} 的字符串表示形式。
     * @see    Character#forDigit
     * @see    #BigDecimal(java.lang.String)
     */
    @Override
    public String toString() {
        String sc = stringCache;
        if (sc == null)
            stringCache = sc = layoutChars(true);
        return sc;
    }


                /**
     * 返回此 {@code BigDecimal} 的字符串表示形式，如果需要使用工程记数法。
     *
     * <p>返回一个表示 {@code BigDecimal} 的字符串，如 {@link #toString()} 方法中所述，但如果有使用指数记数法，
     * 十的幂将调整为三的倍数（工程记数法），使得非零值的整数部分将在 1 到 999 之间。如果为零值使用指数记数法，
     * 将使用小数点和一个或两个小数零位，以保持零值的精度。请注意，与 {@link #toString()} 的输出不同，
     * 该方法的输出 <em>不</em> 保证如果输出字符串使用 {@linkplain #BigDecimal(String) 字符串构造函数} 转换回 {@code BigDecimal} 时，
     * 能恢复相同 [整数, 精度] 对。该方法的结果满足总是从将字符串构造函数应用于方法输出时产生数值相等的结果。
     *
     * @return 使用工程记数法的此 {@code BigDecimal} 的字符串表示形式，如果需要使用指数。
     * @since  1.5
     */
    public String toEngineeringString() {
        return layoutChars(false);
    }

    /**
     * 返回此 {@code BigDecimal} 的字符串表示形式，不带指数字段。对于正精度值，小数点右侧的数字数量用于表示精度。
     * 对于零或负精度值，生成的字符串如同该值被转换为数值相等的零精度值，并且结果中好像所有零精度值的尾随零都存在。
     *
     * 如果无符号值小于零，则整个字符串前缀为减号字符 '-' (<tt>'&#92;u002D'</tt>)。如果无符号值为零或正数，则不添加符号字符。
     *
     * 注意，如果将此方法的结果传递给 {@linkplain #BigDecimal(String) 字符串构造函数}，则仅此 {@code BigDecimal} 的数值将被恢复；
     * 新的 {@code BigDecimal} 的表示形式可能具有不同的精度。特别是，如果此 {@code BigDecimal} 具有负精度，则此方法生成的字符串
     * 在被字符串构造函数处理后将具有零精度。
     *
     * （此方法的行为类似于 1.4 及更早版本中的 {@code toString} 方法。）
     *
     * @return 不带指数字段的此 {@code BigDecimal} 的字符串表示形式。
     * @since 1.5
     * @see #toString()
     * @see #toEngineeringString()
     */
    public String toPlainString() {
        if(scale==0) {
            if(intCompact!=INFLATED) {
                return Long.toString(intCompact);
            } else {
                return intVal.toString();
            }
        }
        if(this.scale<0) { // 没有小数点
            if(signum()==0) {
                return "0";
            }
            int tailingZeros = checkScaleNonZero((-(long)scale));
            StringBuilder buf;
            if(intCompact!=INFLATED) {
                buf = new StringBuilder(20+tailingZeros);
                buf.append(intCompact);
            } else {
                String str = intVal.toString();
                buf = new StringBuilder(str.length()+tailingZeros);
                buf.append(str);
            }
            for (int i = 0; i < tailingZeros; i++)
                buf.append('0');
            return buf.toString();
        }
        String str ;
        if(intCompact!=INFLATED) {
            str = Long.toString(Math.abs(intCompact));
        } else {
            str = intVal.abs().toString();
        }
        return getValueString(signum(), str, scale);
    }

    /* 返回一个 digit.digit 字符串 */
    private String getValueString(int signum, String intString, int scale) {
        /* 插入小数点 */
        StringBuilder buf;
        int insertionPoint = intString.length() - scale;
        if (insertionPoint == 0) {  /* 小数点正好在 intVal 前 */
            return (signum<0 ? "-0." : "0.") + intString;
        } else if (insertionPoint > 0) { /* 小数点在 intVal 内 */
            buf = new StringBuilder(intString);
            buf.insert(insertionPoint, '.');
            if (signum < 0)
                buf.insert(0, '-');
        } else { /* 必须在小数点和 intVal 之间插入零 */
            buf = new StringBuilder(3-insertionPoint + intString.length());
            buf.append(signum<0 ? "-0." : "0.");
            for (int i=0; i<-insertionPoint; i++)
                buf.append('0');
            buf.append(intString);
        }
        return buf.toString();
    }

    /**
     * 将此 {@code BigDecimal} 转换为 {@code BigInteger}。此转换类似于
     * 从 {@code double} 到 {@code long} 的 <i>窄化原始转换</i>，如
     * <cite>The Java&trade; Language Specification</cite> 第 5.1.3 节所定义：
     * 此 {@code BigDecimal} 的任何小数部分将被丢弃。请注意，此转换可能会丢失有关 {@code BigDecimal} 值的精度的信息。
     * <p>
     * 要在转换不精确时（换句话说，如果丢弃了非零小数部分）抛出异常，请使用 {@link #toBigIntegerExact()} 方法。
     *
     * @return 转换为 {@code BigInteger} 的此 {@code BigDecimal}。
     */
    public BigInteger toBigInteger() {
        // 强制转换为整数，静默处理
        return this.setScale(0, ROUND_DOWN).inflated();
    }

    /**
     * 将此 {@code BigDecimal} 转换为 {@code BigInteger}，并检查是否丢失信息。如果此
     * {@code BigDecimal} 有非零小数部分，则抛出异常。
     *
     * @return 转换为 {@code BigInteger} 的此 {@code BigDecimal}。
     * @throws ArithmeticException 如果此 {@code BigDecimal} 有非零小数部分。
     * @since  1.5
     */
    public BigInteger toBigIntegerExact() {
        // 转换为整数，如果小数部分非零则抛出异常
        return this.setScale(0, ROUND_UNNECESSARY).inflated();
    }


                /**
     * 将此 {@code BigDecimal} 转换为 {@code long}。
     * 此转换类似于在《Java&trade; 语言规范》第 5.1.3 节中定义的
     * 从 {@code double} 到 {@code short} 的 <i>缩小原始转换</i>：
     * 任何小数部分都将被丢弃，如果结果的
     * "{@code BigInteger}" 太大而无法放入
     * {@code long}，则仅返回低 64 位。
     * 请注意，此转换可能会丢失有关
     * 此 {@code BigDecimal} 值的整体大小和精度的信息，以及返回一个符号相反的结果。
     *
     * @return 转换为 {@code long} 的此 {@code BigDecimal}。
     */
    public long longValue(){
        if (intCompact != INFLATED && scale == 0) {
            return intCompact;
        } else {
            // 快速处理零和小值
            if (this.signum() == 0 || fractionOnly() ||
                // 快速处理将导致截断值为零的非常大的比例值。如果比例为 -64
                // 或更小，则数值结果中至少有 64 个 10 的幂。由于 10 = 2*5，
                // 在这种情况下结果中也会有 64 个 2 的幂，这意味着 long 的所有 64 位都将为零。
                scale <= -64) {
                return 0;
            } else {
                return toBigInteger().longValue();
            }
        }
    }

    /**
     * 如果非零的 BigDecimal 的绝对值小于一，则返回 true；即只有小数位。
     */
    private boolean fractionOnly() {
        assert this.signum() != 0;
        return (this.precision() - this.scale) <= 0;
    }

    /**
     * 将此 {@code BigDecimal} 转换为 {@code long}，并检查信息丢失。如果此 {@code BigDecimal} 有
     * 非零小数部分或超出 {@code long} 结果的可能范围，则抛出 {@code ArithmeticException}。
     *
     * @return 转换为 {@code long} 的此 {@code BigDecimal}。
     * @throws ArithmeticException 如果此值有非零小数部分，或无法放入 {@code long}。
     * @since  1.5
     */
    public long longValueExact() {
        if (intCompact != INFLATED && scale == 0)
            return intCompact;

        // 快速处理零
        if (this.signum() == 0)
            return 0;

        // 快速处理小于 1.0 的数字（如果非常小，后者可能非常慢）
        if (fractionOnly())
            throw new ArithmeticException("需要舍入");

        // 如果整数部分的数字超过 19 位，则不可能放入
        if ((precision() - scale) > 19) // [对负比例也有效]
            throw new java.lang.ArithmeticException("溢出");

        // 舍入为整数，如果小数部分非零则抛出异常
        BigDecimal num = this.setScale(0, ROUND_UNNECESSARY);
        if (num.precision() >= 19) // 需要仔细检查
            LongOverflow.check(num);
        return num.inflated().longValue();
    }

    private static class LongOverflow {
        /** 等于 Long.MIN_VALUE 的 BigInteger。 */
        private static final BigInteger LONGMIN = BigInteger.valueOf(Long.MIN_VALUE);

        /** 等于 Long.MAX_VALUE 的 BigInteger。 */
        private static final BigInteger LONGMAX = BigInteger.valueOf(Long.MAX_VALUE);

        public static void check(BigDecimal num) {
            BigInteger intVal = num.inflated();
            if (intVal.compareTo(LONGMIN) < 0 ||
                intVal.compareTo(LONGMAX) > 0)
                throw new java.lang.ArithmeticException("溢出");
        }
    }

    /**
     * 将此 {@code BigDecimal} 转换为 {@code int}。
     * 此转换类似于在《Java&trade; 语言规范》第 5.1.3 节中定义的
     * 从 {@code double} 到 {@code short} 的 <i>缩小原始转换</i>：
     * 任何小数部分都将被丢弃，如果结果的
     * "{@code BigInteger}" 太大而无法放入
     * {@code int}，则仅返回低 32 位。
     * 请注意，此转换可能会丢失有关
     * 此 {@code BigDecimal} 值的整体大小和精度的信息，以及返回一个符号相反的结果。
     *
     * @return 转换为 {@code int} 的此 {@code BigDecimal}。
     */
    public int intValue() {
        return  (intCompact != INFLATED && scale == 0) ?
            (int)intCompact :
            (int)longValue();
    }

    /**
     * 将此 {@code BigDecimal} 转换为 {@code int}，并检查信息丢失。如果此 {@code BigDecimal} 有
     * 非零小数部分或超出 {@code int} 结果的可能范围，则抛出 {@code ArithmeticException}。
     *
     * @return 转换为 {@code int} 的此 {@code BigDecimal}。
     * @throws ArithmeticException 如果此值有非零小数部分，或无法放入 {@code int}。
     * @since  1.5
     */
    public int intValueExact() {
       long num;
       num = this.longValueExact();     // 将检查小数部分
       if ((int)num != num)
           throw new java.lang.ArithmeticException("溢出");
       return (int)num;
    }

    /**
     * 将此 {@code BigDecimal} 转换为 {@code short}，并检查信息丢失。如果此 {@code BigDecimal} 有
     * 非零小数部分或超出 {@code short} 结果的可能范围，则抛出 {@code ArithmeticException}。
     *
     * @return 转换为 {@code short} 的此 {@code BigDecimal}。
     * @throws ArithmeticException 如果此值有非零小数部分，或无法放入 {@code short}。
     * @since  1.5
     */
    public short shortValueExact() {
       long num;
       num = this.longValueExact();     // 将检查小数部分
       if ((short)num != num)
           throw new java.lang.ArithmeticException("溢出");
       return (short)num;
    }

                /**
     * 将此 {@code BigDecimal} 转换为 {@code byte}，并检查是否有信息丢失。如果此 {@code BigDecimal}
     * 有非零的小数部分或超出 {@code byte} 结果的可能范围，则抛出 {@code ArithmeticException}。
     *
     * @return 转换为此 {@code byte} 的 {@code BigDecimal}。
     * @throws ArithmeticException 如果此 {@code BigDecimal} 有非零的小数部分，或无法转换为 {@code byte}。
     * @since  1.5
     */
    public byte byteValueExact() {
       long num;
       num = this.longValueExact();     // 将检查小数部分
       if ((byte)num != num)
           throw new java.lang.ArithmeticException("溢出");
       return (byte)num;
    }

    /**
     * 将此 {@code BigDecimal} 转换为 {@code float}。
     * 此转换类似于《Java&trade; 语言规范》第 5.1.3 节中定义的从 {@code double} 到
     * {@code float} 的 <i>窄化原始转换</i>：
     * 如果此 {@code BigDecimal} 的量级过大，无法表示为 {@code float}，则将根据情况转换为
     * {@link Float#NEGATIVE_INFINITY} 或 {@link Float#POSITIVE_INFINITY}。请注意，即使返回值是有限的，
     * 此转换也可能丢失 {@code BigDecimal} 值的精度信息。
     *
     * @return 转换为此 {@code float} 的 {@code BigDecimal}。
     */
    public float floatValue(){
        if(intCompact != INFLATED) {
            if (scale == 0) {
                return (float)intCompact;
            } else {
                /*
                 * 如果 intCompact 和 scale 都可以精确表示为 float 值，则执行单个 float 乘法或除法
                 * 以计算（正确舍入的）结果。
                 */
                if (Math.abs(intCompact) < 1L<<22 ) {
                    // 由于对外部检查了 INFLATED，因此不需要担心 Math.abs(MIN_VALUE)。
                    if (scale > 0 && scale < float10pow.length) {
                        return (float)intCompact / float10pow[scale];
                    } else if (scale < 0 && scale > -float10pow.length) {
                        return (float)intCompact * float10pow[-scale];
                    }
                }
            }
        }
        // 效率较低，但保证有效。
        return Float.parseFloat(this.toString());
    }

    /**
     * 将此 {@code BigDecimal} 转换为 {@code double}。
     * 此转换类似于《Java&trade; 语言规范》第 5.1.3 节中定义的从 {@code double} 到
     * {@code float} 的 <i>窄化原始转换</i>：
     * 如果此 {@code BigDecimal} 的量级过大，无法表示为 {@code double}，则将根据情况转换为
     * {@link Double#NEGATIVE_INFINITY} 或 {@link Double#POSITIVE_INFINITY}。请注意，即使返回值是有限的，
     * 此转换也可能丢失 {@code BigDecimal} 值的精度信息。
     *
     * @return 转换为此 {@code double} 的 {@code BigDecimal}。
     */
    public double doubleValue(){
        if(intCompact != INFLATED) {
            if (scale == 0) {
                return (double)intCompact;
            } else {
                /*
                 * 如果 intCompact 和 scale 都可以精确表示为 double 值，则执行单个 double 乘法或除法
                 * 以计算（正确舍入的）结果。
                 */
                if (Math.abs(intCompact) < 1L<<52 ) {
                    // 由于对外部检查了 INFLATED，因此不需要担心 Math.abs(MIN_VALUE)。
                    if (scale > 0 && scale < double10pow.length) {
                        return (double)intCompact / double10pow[scale];
                    } else if (scale < 0 && scale > -double10pow.length) {
                        return (double)intCompact * double10pow[-scale];
                    }
                }
            }
        }
        // 效率较低，但保证有效。
        return Double.parseDouble(this.toString());
    }

    /**
     * 可以在 {@code double} 中精确表示的 10 的幂。
     */
    private static final double double10pow[] = {
        1.0e0,  1.0e1,  1.0e2,  1.0e3,  1.0e4,  1.0e5,
        1.0e6,  1.0e7,  1.0e8,  1.0e9,  1.0e10, 1.0e11,
        1.0e12, 1.0e13, 1.0e14, 1.0e15, 1.0e16, 1.0e17,
        1.0e18, 1.0e19, 1.0e20, 1.0e21, 1.0e22
    };

    /**
     * 可以在 {@code float} 中精确表示的 10 的幂。
     */
    private static final float float10pow[] = {
        1.0e0f, 1.0e1f, 1.0e2f, 1.0e3f, 1.0e4f, 1.0e5f,
        1.0e6f, 1.0e7f, 1.0e8f, 1.0e9f, 1.0e10f
    };

    /**
     * 返回此 {@code BigDecimal} 的 ulp（最低有效位单位）的大小。非零 {@code BigDecimal} 值的 ulp
     * 是此值与具有相同位数的下一个较大值之间的正距离。零值的 ulp 在数值上等于具有 {@code this} 的
     * 量级的 1。结果以与 {@code this} 相同的量级存储，因此零值和非零值的结果等于 {@code [1, this.scale()]}。
     *
     * @return 此 {@code BigDecimal} 的 ulp 的大小
     * @since 1.5
     */
    public BigDecimal ulp() {
        return BigDecimal.valueOf(1, this.scale(), 1);
    }

    // 用于构建 BigDecimal 对象的字符串表示的私有类。
    // "StringBuilderHelper" 作为线程局部变量构造，因此是线程安全的。StringBuilder 字段用作缓冲区，
    // 用于保存 BigDecimal 的临时表示。cmpCharArray 保存 BigDecimal 的紧凑表示的所有字符（不包括负号）
    // 如果其 intCompact 字段不是 INFLATED。它由该特定线程中的所有 toString() 及其变体调用共享。
    static class StringBuilderHelper {
        final StringBuilder sb;    // 用于保存 BigDecimal 字符串的占位符
        final char[] cmpCharArray; // 用于放置 intCompact 的字符数组


                    StringBuilderHelper() {
            sb = new StringBuilder();
            // 所有非负 long 值都可以放入 19 个字符数组中。
            cmpCharArray = new char[19];
        }

        // 访问器。
        StringBuilder getStringBuilder() {
            sb.setLength(0);
            return sb;
        }

        char[] getCompactCharArray() {
            return cmpCharArray;
        }

        /**
         * 将表示 intCompact 的字符放入 cmpCharArray 中，并返回表示开始的数组偏移量。
         *
         * @param intCompact 要放入 cmpCharArray 中的数字。
         * @return 表示开始的数组偏移量。
         * 注意：intCompact 必须大于或等于零。
         */
        int putIntCompact(long intCompact) {
            assert intCompact >= 0;

            long q;
            int r;
            // 由于我们从最低有效位开始，charPos 指向 cmpCharArray 中的最后一个字符。
            int charPos = cmpCharArray.length;

            // 使用 longs 每次获取 2 位，直到商适合放入 int 中
            while (intCompact > Integer.MAX_VALUE) {
                q = intCompact / 100;
                r = (int)(intCompact - q * 100);
                intCompact = q;
                cmpCharArray[--charPos] = DIGIT_ONES[r];
                cmpCharArray[--charPos] = DIGIT_TENS[r];
            }

            // 当 i2 >= 100 时，使用 ints 每次获取 2 位
            int q2;
            int i2 = (int)intCompact;
            while (i2 >= 100) {
                q2 = i2 / 100;
                r  = i2 - q2 * 100;
                i2 = q2;
                cmpCharArray[--charPos] = DIGIT_ONES[r];
                cmpCharArray[--charPos] = DIGIT_TENS[r];
            }

            cmpCharArray[--charPos] = DIGIT_ONES[i2];
            if (i2 >= 10)
                cmpCharArray[--charPos] = DIGIT_TENS[i2];

            return charPos;
        }

        final static char[] DIGIT_TENS = {
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
        };

        final static char[] DIGIT_ONES = {
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
        };
    }

    /**
     * 将此 {@code BigDecimal} 布局到一个 {@code char[]} 数组中。
     * 这是 Java 1.2 中的 {@code getValueString} 的等效方法。
     *
     * @param  sci {@code true} 表示科学记数法；{@code false} 表示工程记数法
     * @return 此 {@code BigDecimal} 的规范字符串表示形式
     */
    private String layoutChars(boolean sci) {
        if (scale == 0)                      // 零标度是简单的
            return (intCompact != INFLATED) ?
                Long.toString(intCompact):
                intVal.toString();
        if (scale == 2  &&
            intCompact >= 0 && intCompact < Integer.MAX_VALUE) {
            // 货币快速路径
            int lowInt = (int)intCompact % 100;
            int highInt = (int)intCompact / 100;
            return (Integer.toString(highInt) + '.' +
                    StringBuilderHelper.DIGIT_TENS[lowInt] +
                    StringBuilderHelper.DIGIT_ONES[lowInt]) ;
        }

        StringBuilderHelper sbHelper = threadLocalStringBuilderHelper.get();
        char[] coeff;
        int offset;  // offset 是 coeff 数组的起始索引
        // 获取绝对值的尾数
        if (intCompact != INFLATED) {
            offset = sbHelper.putIntCompact(Math.abs(intCompact));
            coeff  = sbHelper.getCompactCharArray();
        } else {
            offset = 0;
            coeff  = intVal.abs().toString().toCharArray();
        }

        // 构建一个具有足够容量的缓冲区，以适应所有情况。
        // 如果需要 E-记数法，长度将为：如果为负数则 +1，如果需要 '.' 则 +1，"E+" 为 +2，调整后的指数最多为 +10。
        // 否则，它可能有 +1 如果为负数，加上前导 "0.00000"
        StringBuilder buf = sbHelper.getStringBuilder();
        if (signum() < 0)             // 如果为负数，则前缀 '-'
            buf.append('-');
        int coeffLen = coeff.length - offset;
        long adjusted = -(long)scale + (coeffLen -1);
        if ((scale >= 0) && (adjusted >= -6)) { // 普通数字
            int pad = scale - coeffLen;         // 填充零的计数
            if (pad >= 0) {                     // 0.xxx 形式
                buf.append('0');
                buf.append('.');
                for (; pad>0; pad--) {
                    buf.append('0');
                }
                buf.append(coeff, offset, coeffLen);
            } else {                         // xx.xx 形式
                buf.append(coeff, offset, -pad);
                buf.append('.');
                buf.append(coeff, -pad + offset, scale);
            }
        } else { // 需要 E-记数法
            if (sci) {                       // 科学记数法
                buf.append(coeff[offset]);   // 第一个字符
                if (coeffLen > 1) {          // 还有更多
                    buf.append('.');
                    buf.append(coeff, offset + 1, coeffLen - 1);
                }
            } else {                         // 工程记数法
                int sig = (int)(adjusted % 3);
                if (sig < 0)
                    sig += 3;                // [adjusted 为负数]
                adjusted -= sig;             // 现在是 3 的倍数
                sig++;
                if (signum() == 0) {
                    switch (sig) {
                    case 1:
                        buf.append('0'); // 指数是 3 的倍数
                        break;
                    case 2:
                        buf.append("0.00");
                        adjusted += 3;
                        break;
                    case 3:
                        buf.append("0.0");
                        adjusted += 3;
                        break;
                    default:
                        throw new AssertionError("Unexpected sig value " + sig);
                    }
                } else if (sig >= coeffLen) {   // 尾数全部在整数部分
                    buf.append(coeff, offset, coeffLen);
                    // 可能还需要一些零
                    for (int i = sig - coeffLen; i > 0; i--)
                        buf.append('0');
                } else {                     // xx.xxE 形式
                    buf.append(coeff, offset, sig);
                    buf.append('.');
                    buf.append(coeff, offset + sig, coeffLen - sig);
                }
            }
            if (adjusted != 0) {             // [!sci 可能已变为 0]
                buf.append('E');
                if (adjusted > 0)            // 强制正数的符号
                    buf.append('+');
                buf.append(adjusted);
            }
        }
        return buf.toString();
    }


                /**
     * 返回10的n次幂，作为 {@code BigInteger}。
     *
     * @param  n 要返回的10的幂次（>=0）
     * @return 一个值为 (10<sup>n</sup>) 的 {@code BigInteger}
     */
    private static BigInteger bigTenToThe(int n) {
        if (n < 0)
            return BigInteger.ZERO;

        if (n < BIG_TEN_POWERS_TABLE_MAX) {
            BigInteger[] pows = BIG_TEN_POWERS_TABLE;
            if (n < pows.length)
                return pows[n];
            else
                return expandBigIntegerTenPowers(n);
        }

        return BigInteger.TEN.pow(n);
    }

    /**
     * 扩展 BIG_TEN_POWERS_TABLE 数组，使其至少包含 10**n。
     *
     * @param n 要返回的10的幂次（>=0）
     * @return 一个值为 (10<sup>n</sup>) 的 {@code BigDecimal}，同时
     *         BIG_TEN_POWERS_TABLE 数组扩展到大于 n 的大小。
     */
    private static BigInteger expandBigIntegerTenPowers(int n) {
        synchronized(BigDecimal.class) {
            BigInteger[] pows = BIG_TEN_POWERS_TABLE;
            int curLen = pows.length;
            // 以下比较和上述同步语句是为了防止多个线程扩展同一个数组。
            if (curLen <= n) {
                int newLen = curLen << 1;
                while (newLen <= n)
                    newLen <<= 1;
                pows = Arrays.copyOf(pows, newLen);
                for (int i = curLen; i < newLen; i++)
                    pows[i] = pows[i - 1].multiply(BigInteger.TEN);
                // 基于以下事实：
                // 1. pows 是一个私有的局部变量；
                // 2. 下面的存储是一个 volatile 存储。
                // 新创建的数组元素可以安全地发布。
                BIG_TEN_POWERS_TABLE = pows;
            }
            return pows[n];
        }
    }

    private static final long[] LONG_TEN_POWERS_TABLE = {
        1,                     // 0 / 10^0
        10,                    // 1 / 10^1
        100,                   // 2 / 10^2
        1000,                  // 3 / 10^3
        10000,                 // 4 / 10^4
        100000,                // 5 / 10^5
        1000000,               // 6 / 10^6
        10000000,              // 7 / 10^7
        100000000,             // 8 / 10^8
        1000000000,            // 9 / 10^9
        10000000000L,          // 10 / 10^10
        100000000000L,         // 11 / 10^11
        1000000000000L,        // 12 / 10^12
        10000000000000L,       // 13 / 10^13
        100000000000000L,      // 14 / 10^14
        1000000000000000L,     // 15 / 10^15
        10000000000000000L,    // 16 / 10^16
        100000000000000000L,   // 17 / 10^17
        1000000000000000000L   // 18 / 10^18
    };

    private static volatile BigInteger BIG_TEN_POWERS_TABLE[] = {
        BigInteger.ONE,
        BigInteger.valueOf(10),
        BigInteger.valueOf(100),
        BigInteger.valueOf(1000),
        BigInteger.valueOf(10000),
        BigInteger.valueOf(100000),
        BigInteger.valueOf(1000000),
        BigInteger.valueOf(10000000),
        BigInteger.valueOf(100000000),
        BigInteger.valueOf(1000000000),
        BigInteger.valueOf(10000000000L),
        BigInteger.valueOf(100000000000L),
        BigInteger.valueOf(1000000000000L),
        BigInteger.valueOf(10000000000000L),
        BigInteger.valueOf(100000000000000L),
        BigInteger.valueOf(1000000000000000L),
        BigInteger.valueOf(10000000000000000L),
        BigInteger.valueOf(100000000000000000L),
        BigInteger.valueOf(1000000000000000000L)
    };

    private static final int BIG_TEN_POWERS_TABLE_INITLEN =
        BIG_TEN_POWERS_TABLE.length;
    private static final int BIG_TEN_POWERS_TABLE_MAX =
        16 * BIG_TEN_POWERS_TABLE_INITLEN;

    private static final long THRESHOLDS_TABLE[] = {
        Long.MAX_VALUE,                     // 0
        Long.MAX_VALUE/10L,                 // 1
        Long.MAX_VALUE/100L,                // 2
        Long.MAX_VALUE/1000L,               // 3
        Long.MAX_VALUE/10000L,              // 4
        Long.MAX_VALUE/100000L,             // 5
        Long.MAX_VALUE/1000000L,            // 6
        Long.MAX_VALUE/10000000L,           // 7
        Long.MAX_VALUE/100000000L,          // 8
        Long.MAX_VALUE/1000000000L,         // 9
        Long.MAX_VALUE/10000000000L,        // 10
        Long.MAX_VALUE/100000000000L,       // 11
        Long.MAX_VALUE/1000000000000L,      // 12
        Long.MAX_VALUE/10000000000000L,     // 13
        Long.MAX_VALUE/100000000000000L,    // 14
        Long.MAX_VALUE/1000000000000000L,   // 15
        Long.MAX_VALUE/10000000000000000L,  // 16
        Long.MAX_VALUE/100000000000000000L, // 17
        Long.MAX_VALUE/1000000000000000000L // 18
    };

    /**
     * 计算 val * 10 ^ n；如果结果可以表示为 long，则返回此乘积，否则返回 INFLATED。
     */
    private static long longMultiplyPowerTen(long val, int n) {
        if (val == 0 || n <= 0)
            return val;
        long[] tab = LONG_TEN_POWERS_TABLE;
        long[] bounds = THRESHOLDS_TABLE;
        if (n < tab.length && n < bounds.length) {
            long tenpower = tab[n];
            if (val == 1)
                return tenpower;
            if (Math.abs(val) <= bounds[n])
                return val * tenpower;
        }
        return INFLATED;
    }

    /**
     * 计算 this * 10 ^ n。
     * 主要是为了允许特殊情况处理，以捕获零值。
     */
    private BigInteger bigMultiplyPowerTen(int n) {
        if (n <= 0)
            return this.inflated();

        if (intCompact != INFLATED)
            return bigTenToThe(n).multiply(intCompact);
        else
            return intVal.multiply(bigTenToThe(n));
    }

    /**
     * 如果 intVal 为 null，即使用紧凑表示法时，返回 intVal 字段的适当 BigInteger。
     */
    private BigInteger inflated() {
        if (intVal == null) {
            return BigInteger.valueOf(intCompact);
        }
        return intVal;
    }

                /**
     * 对齐两个 {@code BigDecimal} 的标度，使它们的最低有效位对齐。
     *
     * <p>如果 val[0] 和 val[1] 的标度不同，则非破坏性地重新缩放标度较低的 {@code BigDecimal} 以使它们匹配。
     * 也就是说，标度较低的引用将被替换为一个新对象的引用，该对象具有与另一个 {@code BigDecimal} 相同的标度。
     *
     * @param  val 包含两个要对齐的 {@code BigDecimal} 的数组。
     */
    private static void matchScale(BigDecimal[] val) {
        if (val[0].scale == val[1].scale) {
            return;
        } else if (val[0].scale < val[1].scale) {
            val[0] = val[0].setScale(val[1].scale, ROUND_UNNECESSARY);
        } else if (val[1].scale < val[0].scale) {
            val[1] = val[1].setScale(val[0].scale, ROUND_UNNECESSARY);
        }
    }

    private static class UnsafeHolder {
        private static final sun.misc.Unsafe unsafe;
        private static final long intCompactOffset;
        private static final long intValOffset;
        static {
            try {
                unsafe = sun.misc.Unsafe.getUnsafe();
                intCompactOffset = unsafe.objectFieldOffset
                    (BigDecimal.class.getDeclaredField("intCompact"));
                intValOffset = unsafe.objectFieldOffset
                    (BigDecimal.class.getDeclaredField("intVal"));
            } catch (Exception ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }
        static void setIntCompactVolatile(BigDecimal bd, long val) {
            unsafe.putLongVolatile(bd, intCompactOffset, val);
        }

        static void setIntValVolatile(BigDecimal bd, BigInteger val) {
            unsafe.putObjectVolatile(bd, intValOffset, val);
        }
    }

    /**
     * 从流中重新构建（即反序列化）{@code BigDecimal} 实例。
     *
     * @param s 正在读取的流。
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // 读取所有字段
        s.defaultReadObject();
        // 验证可能的错误字段
        if (intVal == null) {
            String message = "BigDecimal: null intVal in stream";
            throw new java.io.StreamCorruptedException(message);
        // [现在允许所有标度值]
        }
        UnsafeHolder.setIntCompactVolatile(this, compactValFor(intVal));
    }

   /**
    * 将此 {@code BigDecimal} 序列化到指定的流中。
    *
    * @param s 要序列化的流。
    */
   private void writeObject(java.io.ObjectOutputStream s)
       throws java.io.IOException {
       // 必须膨胀以保持兼容的序列化形式。
       if (this.intVal == null)
           UnsafeHolder.setIntValVolatile(this, BigInteger.valueOf(this.intCompact));
       // 如果需要设置，可以将 intVal 重置回 null。
       s.defaultWriteObject();
   }

    /**
     * 返回 {@code long} 的绝对值的十进制数字长度。
     *
     * @param x {@code long} 类型的值。
     * @return 未缩放值的十进制数字长度。
     */
    static int longDigitLength(long x) {
        /*
         * 如 Sean Anderson 在 "Bit Twiddling Hacks" 中所述，
         * (http://graphics.stanford.edu/~seander/bithacks.html)
         * x 的整数对数 10 与 (1233/4096) * (1 + x 的整数对数 2) 之差不超过 1。
         * 分数 1233/4096 近似于 log10(2)。因此我们首先执行一个版本的 log2（一个 Long 类的变体，带有预检查和相反的方向性），
         * 然后缩放并检查幂表。这在当前上下文中比《Hacker's Delight》第 11-4 节中的版本稍微简单一些。
         * 对位长度加一允许从我们无论如何都需要的 LONG_TEN_POWERS_TABLE 中向下比较。
         */
        assert x != BigDecimal.INFLATED;
        if (x < 0)
            x = -x;
        if (x < 10) // 必须筛选 0，也可以筛选 10
            return 1;
        int r = ((64 - Long.numberOfLeadingZeros(x) + 1) * 1233) >>> 12;
        long[] tab = LONG_TEN_POWERS_TABLE;
        // 如果 r 大于等于长度，必须具有 long 的最大可能数字
        return (r >= tab.length || x < tab[r]) ? r : r + 1;
    }

    /**
     * 返回 {@code BigInteger} 的绝对值的十进制数字长度。
     *
     * @param b {@code BigInteger} 类型的值。
     * @return 未缩放值的十进制数字长度。
     */
    private static int bigDigitLength(BigInteger b) {
        /*
         * 与 long 版本相同的想法，但我们需要一个更好的 log10(2) 近似值。
         * 使用 646456993/2^31 在最大可能报告的 bitLength 内是准确的。
         */
        if (b.signum == 0)
            return 1;
        int r = (int)((((long)b.bitLength() + 1) * 646456993) >>> 31);
        return b.compareMagnitude(bigTenToThe(r)) < 0? r : r+1;
    }

    /**
     * 检查标度是否溢出或下溢。如果此 BigDecimal 非零，且标度超出范围，则抛出异常。
     * 如果此值为零，且标度超出范围，则将标度饱和为正确符号的极端值。
     *
     * @param val 新的标度。
     * @throws ArithmeticException (溢出或下溢) 如果新的标度超出范围。
     * @return 验证后的标度，作为 int 类型返回。
     */
    private int checkScale(long val) {
        int asInt = (int)val;
        if (asInt != val) {
            asInt = val>Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            BigInteger b;
            if (intCompact != 0 &&
                ((b = intVal) == null || b.signum() != 0))
                throw new ArithmeticException(asInt>0 ? "Underflow":"Overflow");
        }
        return asInt;
    }

   /**
     * 返回给定 {@code BigInteger} 的紧凑值，如果太大则返回 INFLATED。
     * 依赖于 {@code BigInteger} 的内部表示。
     */
    private static long compactValFor(BigInteger b) {
        int[] m = b.mag;
        int len = m.length;
        if (len == 0)
            return 0;
        int d = m[0];
        if (len > 2 || (len == 2 && d < 0))
            return INFLATED;


                    long u = (len == 2)?
            (((long) m[1] & LONG_MASK) + (((long)d) << 32)) :
            (((long)d)   & LONG_MASK);
        return (b.signum < 0)? -u : u;
    }

    private static int longCompareMagnitude(long x, long y) {
        if (x < 0)
            x = -x;
        if (y < 0)
            y = -y;
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    private static int saturateLong(long s) {
        int i = (int)s;
        return (s == i) ? i : (s < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE);
    }

    /*
     * 内部打印例程
     */
    private static void print(String name, BigDecimal bd) {
        System.err.format("%s:\tintCompact %d\tintVal %d\tscale %d\tprecision %d%n",
                          name,
                          bd.intCompact,
                          bd.intVal,
                          bd.scale,
                          bd.precision);
    }

    /**
     * 检查此 BigDecimal 的内部不变性。这些不变性包括：
     *
     * <ul>
     *
     * <li>对象必须已初始化；intCompact 不能是 INFLATED 或 intVal 非空。这两个条件可以同时为真。
     *
     * <li>如果 intCompact 和 intVal 都已设置，它们的值必须一致。
     *
     * <li>如果精度非零，它必须具有正确的值。
     * </ul>
     *
     * 注意：由于这是一个审计方法，我们不应对这个 BigDecimal 对象的状态进行更改。
     */
    private BigDecimal audit() {
        if (intCompact == INFLATED) {
            if (intVal == null) {
                print("audit", this);
                throw new AssertionError("null intVal");
            }
            // 检查精度
            if (precision > 0 && precision != bigDigitLength(intVal)) {
                print("audit", this);
                throw new AssertionError("precision mismatch");
            }
        } else {
            if (intVal != null) {
                long val = intVal.longValue();
                if (val != intCompact) {
                    print("audit", this);
                    throw new AssertionError("Inconsistent state, intCompact=" +
                                             intCompact + "\t intVal=" + val);
                }
            }
            // 检查精度
            if (precision > 0 && precision != longDigitLength(intCompact)) {
                print("audit", this);
                throw new AssertionError("precision mismatch");
            }
        }
        return this;
    }

    /* 与 checkScale 类似，但值不为 0 */
    private static int checkScaleNonZero(long val) {
        int asInt = (int)val;
        if (asInt != val) {
            throw new ArithmeticException(asInt>0 ? "Underflow":"Overflow");
        }
        return asInt;
    }

    private static int checkScale(long intCompact, long val) {
        int asInt = (int)val;
        if (asInt != val) {
            asInt = val>Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            if (intCompact != 0)
                throw new ArithmeticException(asInt>0 ? "Underflow":"Overflow");
        }
        return asInt;
    }

    private static int checkScale(BigInteger intVal, long val) {
        int asInt = (int)val;
        if (asInt != val) {
            asInt = val>Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            if (intVal.signum() != 0)
                throw new ArithmeticException(asInt>0 ? "Underflow":"Overflow");
        }
        return asInt;
    }

    /**
     * 根据 MathContext 设置返回一个四舍五入的 {@code BigDecimal}；
     * 如果需要四舍五入，则创建并返回一个新的 {@code BigDecimal}。
     *
     * @param val 要四舍五入的值
     * @param mc 要使用的上下文。
     * @return 一个根据 MathContext 设置四舍五入的 {@code BigDecimal}。如果不需要四舍五入，可能返回 {@code value}。
     * @throws ArithmeticException 如果舍入模式为 {@code RoundingMode.UNNECESSARY} 且结果不精确。
     */
    private static BigDecimal doRound(BigDecimal val, MathContext mc) {
        int mcp = mc.precision;
        boolean wasDivided = false;
        if (mcp > 0) {
            BigInteger intVal = val.intVal;
            long compactVal = val.intCompact;
            int scale = val.scale;
            int prec = val.precision();
            int mode = mc.roundingMode.oldMode;
            int drop;
            if (compactVal == INFLATED) {
                drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    intVal = divideAndRoundByTenPow(intVal, drop, mode);
                    wasDivided = true;
                    compactVal = compactValFor(intVal);
                    if (compactVal != INFLATED) {
                        prec = longDigitLength(compactVal);
                        break;
                    }
                    prec = bigDigitLength(intVal);
                    drop = prec - mcp;
                }
            }
            if (compactVal != INFLATED) {
                drop = prec - mcp;  // drop 不能超过 18
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                    wasDivided = true;
                    prec = longDigitLength(compactVal);
                    drop = prec - mcp;
                    intVal = null;
                }
            }
            return wasDivided ? new BigDecimal(intVal,compactVal,scale,prec) : val;
        }
        return val;
    }

    /*
     * 根据 MathContext 设置，从给定的 scale 的 {@code long} 值创建一个 {@code BigDecimal}
     */
    private static BigDecimal doRound(long compactVal, int scale, MathContext mc) {
        int mcp = mc.precision;
        if (mcp > 0 && mcp < 19) {
            int prec = longDigitLength(compactVal);
            int drop = prec - mcp;  // drop 不能超过 18
            while (drop > 0) {
                scale = checkScaleNonZero((long) scale - drop);
                compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                prec = longDigitLength(compactVal);
                drop = prec - mcp;
            }
            return valueOf(compactVal, scale, prec);
        }
        return valueOf(compactVal, scale);
    }


                /*
     * 返回一个根据给定的精度和舍入模式从 {@code BigInteger} 值创建的 {@code BigDecimal}
     */
    private static BigDecimal doRound(BigInteger intVal, int scale, MathContext mc) {
        int mcp = mc.precision;
        int prec = 0;
        if (mcp > 0) {
            long compactVal = compactValFor(intVal);
            int mode = mc.roundingMode.oldMode;
            int drop;
            if (compactVal == INFLATED) {
                prec = bigDigitLength(intVal);
                drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    intVal = divideAndRoundByTenPow(intVal, drop, mode);
                    compactVal = compactValFor(intVal);
                    if (compactVal != INFLATED) {
                        break;
                    }
                    prec = bigDigitLength(intVal);
                    drop = prec - mcp;
                }
            }
            if (compactVal != INFLATED) {
                prec = longDigitLength(compactVal);
                drop = prec - mcp;     // drop 不能超过 18
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                    prec = longDigitLength(compactVal);
                    drop = prec - mcp;
                }
                return valueOf(compactVal, scale, prec);
            }
        }
        return new BigDecimal(intVal, INFLATED, scale, prec);
    }

    /*
     * 将 {@code BigInteger} 值除以 10 的幂。
     */
    private static BigInteger divideAndRoundByTenPow(BigInteger intVal, int tenPow, int roundingMode) {
        if (tenPow < LONG_TEN_POWERS_TABLE.length)
            intVal = divideAndRound(intVal, LONG_TEN_POWERS_TABLE[tenPow], roundingMode);
        else
            intVal = divideAndRound(intVal, bigTenToThe(tenPow), roundingMode);
        return intVal;
    }

    /**
     * 用于执行 {@code long} 除以 {@code long} 的内部除法操作。
     * 返回的 {@code BigDecimal} 对象的标度设置为传入的标度。如果余数不为零，将根据传入的舍入模式进行舍入。
     * 另外，如果余数为零且最后一个参数（即 preferredScale）不等于标度，则结果的尾随零将被剥离以匹配 preferredScale。
     */
    private static BigDecimal divideAndRound(long ldividend, long ldivisor, int scale, int roundingMode,
                                             int preferredScale) {

        int qsign; // 商的符号
        long q = ldividend / ldivisor; // 将商存储在 long 中
        if (roundingMode == ROUND_DOWN && scale == preferredScale)
            return valueOf(q, scale);
        long r = ldividend % ldivisor; // 将余数存储在 long 中
        qsign = ((ldividend < 0) == (ldivisor < 0)) ? 1 : -1;
        if (r != 0) {
            boolean increment = needIncrement(ldivisor, roundingMode, qsign, q, r);
            return valueOf((increment ? q + qsign : q), scale);
        } else {
            if (preferredScale != scale)
                return createAndStripZerosToMatchScale(q, scale, preferredScale);
            else
                return valueOf(q, scale);
        }
    }

    /**
     * 将 {@code long} 除以 {@code long} 并根据传入的舍入模式进行舍入。
     */
    private static long divideAndRound(long ldividend, long ldivisor, int roundingMode) {
        int qsign; // 商的符号
        long q = ldividend / ldivisor; // 将商存储在 long 中
        if (roundingMode == ROUND_DOWN)
            return q;
        long r = ldividend % ldivisor; // 将余数存储在 long 中
        qsign = ((ldividend < 0) == (ldivisor < 0)) ? 1 : -1;
        if (r != 0) {
            boolean increment = needIncrement(ldivisor, roundingMode, qsign, q,     r);
            return increment ? q + qsign : q;
        } else {
            return q;
        }
    }

    /**
     * 需要增量计算的共享逻辑。
     */
    private static boolean commonNeedIncrement(int roundingMode, int qsign,
                                        int cmpFracHalf, boolean oddQuot) {
        switch(roundingMode) {
        case ROUND_UNNECESSARY:
            throw new ArithmeticException("需要舍入");

        case ROUND_UP: // 远离零
            return true;

        case ROUND_DOWN: // 靠近零
            return false;

        case ROUND_CEILING: // 靠近正无穷
            return qsign > 0;

        case ROUND_FLOOR: // 靠近负无穷
            return qsign < 0;

        default: // 某种形式的半舍入
            assert roundingMode >= ROUND_HALF_UP &&
                roundingMode <= ROUND_HALF_EVEN: "意外的舍入模式" + RoundingMode.valueOf(roundingMode);

            if (cmpFracHalf < 0 ) // 更接近高一位
                return false;
            else if (cmpFracHalf > 0 ) // 更接近低一位
                return true;
            else { // 半位
                assert cmpFracHalf == 0;

                switch(roundingMode) {
                case ROUND_HALF_DOWN:
                    return false;

                case ROUND_HALF_UP:
                    return true;

                case ROUND_HALF_EVEN:
                    return oddQuot;

                default:
                    throw new AssertionError("意外的舍入模式" + roundingMode);
                }
            }
        }
    }

    /**
     * 测试根据舍入模式是否需要增加商。
     */
    private static boolean needIncrement(long ldivisor, int roundingMode,
                                         int qsign, long q, long r) {
        assert r != 0L;

        int cmpFracHalf;
        if (r <= HALF_LONG_MIN_VALUE || r > HALF_LONG_MAX_VALUE) {
            cmpFracHalf = 1; // 2 * r 不能放入 long
        } else {
            cmpFracHalf = longCompareMagnitude(2 * r, ldivisor);
        }


                    return commonNeedIncrement(roundingMode, qsign, cmpFracHalf, (q & 1L) != 0L);
    }

    /**
     * 将 {@code BigInteger} 值除以 {@code long} 值，并根据传入的 roundingMode 进行四舍五入。
     */
    private static BigInteger divideAndRound(BigInteger bdividend, long ldivisor, int roundingMode) {
        boolean isRemainderZero; // 记录余数是否为零
        int qsign; // 商的符号
        long r = 0; // 存储商和余数的 long 变量
        MutableBigInteger mq = null; // 存储商
        // 为了更快的余数检查，使用可变对象
        MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
        mq = new MutableBigInteger();
        r = mdividend.divide(ldivisor, mq);
        isRemainderZero = (r == 0);
        qsign = (ldivisor < 0) ? -bdividend.signum : bdividend.signum;
        if (!isRemainderZero) {
            if(needIncrement(ldivisor, roundingMode, qsign, mq, r)) {
                mq.add(MutableBigInteger.ONE);
            }
        }
        return mq.toBigInteger(qsign);
    }

    /**
     * 用于 {@code BigInteger} 除以 {@code long} 的内部除法操作。
     * 返回的 {@code BigDecimal} 对象的 scale 被设置为传入的 scale。如果余数不为零，将根据传入的 roundingMode 进行四舍五入。
     * 另外，如果余数为零且最后一个参数 preferredScale 不等于 scale，结果的尾随零将被剥离以匹配 preferredScale。
     */
    private static BigDecimal divideAndRound(BigInteger bdividend,
                                             long ldivisor, int scale, int roundingMode, int preferredScale) {
        boolean isRemainderZero; // 记录余数是否为零
        int qsign; // 商的符号
        long r = 0; // 存储商和余数的 long 变量
        MutableBigInteger mq = null; // 存储商
        // 为了更快的余数检查，使用可变对象
        MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
        mq = new MutableBigInteger();
        r = mdividend.divide(ldivisor, mq);
        isRemainderZero = (r == 0);
        qsign = (ldivisor < 0) ? -bdividend.signum : bdividend.signum;
        if (!isRemainderZero) {
            if(needIncrement(ldivisor, roundingMode, qsign, mq, r)) {
                mq.add(MutableBigInteger.ONE);
            }
            return mq.toBigDecimal(qsign, scale);
        } else {
            if (preferredScale != scale) {
                long compactVal = mq.toCompactValue(qsign);
                if(compactVal!=INFLATED) {
                    return createAndStripZerosToMatchScale(compactVal, scale, preferredScale);
                }
                BigInteger intVal =  mq.toBigInteger(qsign);
                return createAndStripZerosToMatchScale(intVal,scale, preferredScale);
            } else {
                return mq.toBigDecimal(qsign, scale);
            }
        }
    }

    /**
     * 测试是否需要根据 roundingMode 增加商
     */
    private static boolean needIncrement(long ldivisor, int roundingMode,
                                         int qsign, MutableBigInteger mq, long r) {
        assert r != 0L;

        int cmpFracHalf;
        if (r <= HALF_LONG_MIN_VALUE || r > HALF_LONG_MAX_VALUE) {
            cmpFracHalf = 1; // 2 * r 不能放入 long
        } else {
            cmpFracHalf = longCompareMagnitude(2 * r, ldivisor);
        }

        return commonNeedIncrement(roundingMode, qsign, cmpFracHalf, mq.isOdd());
    }

    /**
     * 将 {@code BigInteger} 值除以 {@code BigInteger} 值，并根据传入的 roundingMode 进行四舍五入。
     */
    private static BigInteger divideAndRound(BigInteger bdividend, BigInteger bdivisor, int roundingMode) {
        boolean isRemainderZero; // 记录余数是否为零
        int qsign; // 商的符号
        // 为了更快的余数检查，使用可变对象
        MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
        MutableBigInteger mq = new MutableBigInteger();
        MutableBigInteger mdivisor = new MutableBigInteger(bdivisor.mag);
        MutableBigInteger mr = mdividend.divide(mdivisor, mq);
        isRemainderZero = mr.isZero();
        qsign = (bdividend.signum != bdivisor.signum) ? -1 : 1;
        if (!isRemainderZero) {
            if (needIncrement(mdivisor, roundingMode, qsign, mq, mr)) {
                mq.add(MutableBigInteger.ONE);
            }
        }
        return mq.toBigInteger(qsign);
    }

    /**
     * 用于 {@code BigInteger} 除以 {@code BigInteger} 的内部除法操作。
     * 返回的 {@code BigDecimal} 对象的 scale 被设置为传入的 scale。如果余数不为零，将根据传入的 roundingMode 进行四舍五入。
     * 另外，如果余数为零且最后一个参数 preferredScale 不等于 scale，结果的尾随零将被剥离以匹配 preferredScale。
     */
    private static BigDecimal divideAndRound(BigInteger bdividend, BigInteger bdivisor, int scale, int roundingMode,
                                             int preferredScale) {
        boolean isRemainderZero; // 记录余数是否为零
        int qsign; // 商的符号
        // 为了更快的余数检查，使用可变对象
        MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
        MutableBigInteger mq = new MutableBigInteger();
        MutableBigInteger mdivisor = new MutableBigInteger(bdivisor.mag);
        MutableBigInteger mr = mdividend.divide(mdivisor, mq);
        isRemainderZero = mr.isZero();
        qsign = (bdividend.signum != bdivisor.signum) ? -1 : 1;
        if (!isRemainderZero) {
            if (needIncrement(mdivisor, roundingMode, qsign, mq, mr)) {
                mq.add(MutableBigInteger.ONE);
            }
            return mq.toBigDecimal(qsign, scale);
        } else {
            if (preferredScale != scale) {
                long compactVal = mq.toCompactValue(qsign);
                if (compactVal != INFLATED) {
                    return createAndStripZerosToMatchScale(compactVal, scale, preferredScale);
                }
                BigInteger intVal = mq.toBigInteger(qsign);
                return createAndStripZerosToMatchScale(intVal, scale, preferredScale);
            } else {
                return mq.toBigDecimal(qsign, scale);
            }
        }
    }


                /**
     * 测试商是否需要根据舍入模式进行递增
     */
    private static boolean needIncrement(MutableBigInteger mdivisor, int roundingMode,
                                         int qsign, MutableBigInteger mq, MutableBigInteger mr) {
        assert !mr.isZero();
        int cmpFracHalf = mr.compareHalf(mdivisor);
        return commonNeedIncrement(roundingMode, qsign, cmpFracHalf, mq.isOdd());
    }

    /**
     * 从这个 {@code BigInteger} 值中移除不重要的尾随零，直到达到首选比例或无法再移除更多零。
     * 如果首选比例小于 Integer.MIN_VALUE，则所有尾随零都将被移除。
     *
     * @return 一个新的 {@code BigDecimal}，其比例可能减少以接近首选比例。
     */
    private static BigDecimal createAndStripZerosToMatchScale(BigInteger intVal, int scale, long preferredScale) {
        BigInteger qr[]; // 商-余数对
        while (intVal.compareMagnitude(BigInteger.TEN) >= 0
               && scale > preferredScale) {
            if (intVal.testBit(0))
                break; // 奇数不能以0结尾
            qr = intVal.divideAndRemainder(BigInteger.TEN);
            if (qr[1].signum() != 0)
                break; // 非0余数
            intVal = qr[0];
            scale = checkScale(intVal,(long) scale - 1); // 可能溢出
        }
        return valueOf(intVal, scale, 0);
    }

    /**
     * 从这个 {@code long} 值中移除不重要的尾随零，直到达到首选比例或无法再移除更多零。
     * 如果首选比例小于 Integer.MIN_VALUE，则所有尾随零都将被移除。
     *
     * @return 一个新的 {@code BigDecimal}，其比例可能减少以接近首选比例。
     */
    private static BigDecimal createAndStripZerosToMatchScale(long compactVal, int scale, long preferredScale) {
        while (Math.abs(compactVal) >= 10L && scale > preferredScale) {
            if ((compactVal & 1L) != 0L)
                break; // 奇数不能以0结尾
            long r = compactVal % 10L;
            if (r != 0L)
                break; // 非0余数
            compactVal /= 10;
            scale = checkScale(compactVal, (long) scale - 1); // 可能溢出
        }
        return valueOf(compactVal, scale);
    }

    private static BigDecimal stripZerosToMatchScale(BigInteger intVal, long intCompact, int scale, int preferredScale) {
        if(intCompact!=INFLATED) {
            return createAndStripZerosToMatchScale(intCompact, scale, preferredScale);
        } else {
            return createAndStripZerosToMatchScale(intVal==null ? INFLATED_BIGINT : intVal,
                                                   scale, preferredScale);
        }
    }

    /*
     * 如果溢出则返回 INFLATED
     */
    private static long add(long xs, long ys){
        long sum = xs + ys;
        // 参见 "Hacker's Delight" 第 2-12 节以了解溢出测试的解释。
        if ( (((sum ^ xs) & (sum ^ ys))) >= 0L) { // 未溢出
            return sum;
        }
        return INFLATED;
    }

    private static BigDecimal add(long xs, long ys, int scale){
        long sum = add(xs, ys);
        if (sum!=INFLATED)
            return BigDecimal.valueOf(sum, scale);
        return new BigDecimal(BigInteger.valueOf(xs).add(ys), scale);
    }

    private static BigDecimal add(final long xs, int scale1, final long ys, int scale2) {
        long sdiff = (long) scale1 - scale2;
        if (sdiff == 0) {
            return add(xs, ys, scale1);
        } else if (sdiff < 0) {
            int raise = checkScale(xs,-sdiff);
            long scaledX = longMultiplyPowerTen(xs, raise);
            if (scaledX != INFLATED) {
                return add(scaledX, ys, scale2);
            } else {
                BigInteger bigsum = bigMultiplyPowerTen(xs,raise).add(ys);
                return ((xs^ys)>=0) ? // 同号测试
                    new BigDecimal(bigsum, INFLATED, scale2, 0)
                    : valueOf(bigsum, scale2, 0);
            }
        } else {
            int raise = checkScale(ys,sdiff);
            long scaledY = longMultiplyPowerTen(ys, raise);
            if (scaledY != INFLATED) {
                return add(xs, scaledY, scale1);
            } else {
                BigInteger bigsum = bigMultiplyPowerTen(ys,raise).add(xs);
                return ((xs^ys)>=0) ?
                    new BigDecimal(bigsum, INFLATED, scale1, 0)
                    : valueOf(bigsum, scale1, 0);
            }
        }
    }

    private static BigDecimal add(final long xs, int scale1, BigInteger snd, int scale2) {
        int rscale = scale1;
        long sdiff = (long)rscale - scale2;
        boolean sameSigns =  (Long.signum(xs) == snd.signum);
        BigInteger sum;
        if (sdiff < 0) {
            int raise = checkScale(xs,-sdiff);
            rscale = scale2;
            long scaledX = longMultiplyPowerTen(xs, raise);
            if (scaledX == INFLATED) {
                sum = snd.add(bigMultiplyPowerTen(xs,raise));
            } else {
                sum = snd.add(scaledX);
            }
        } else { //if (sdiff > 0) {
            int raise = checkScale(snd,sdiff);
            snd = bigMultiplyPowerTen(snd,raise);
            sum = snd.add(xs);
        }
        return (sameSigns) ?
            new BigDecimal(sum, INFLATED, rscale, 0) :
            valueOf(sum, rscale, 0);
    }

    private static BigDecimal add(BigInteger fst, int scale1, BigInteger snd, int scale2) {
        int rscale = scale1;
        long sdiff = (long)rscale - scale2;
        if (sdiff != 0) {
            if (sdiff < 0) {
                int raise = checkScale(fst,-sdiff);
                rscale = scale2;
                fst = bigMultiplyPowerTen(fst,raise);
            } else {
                int raise = checkScale(snd,sdiff);
                snd = bigMultiplyPowerTen(snd,raise);
            }
        }
        BigInteger sum = fst.add(snd);
        return (fst.signum == snd.signum) ?
                new BigDecimal(sum, INFLATED, rscale, 0) :
                valueOf(sum, rscale, 0);
    }


                private static BigInteger bigMultiplyPowerTen(long value, int n) {
        if (n <= 0)
            return BigInteger.valueOf(value);
        return bigTenToThe(n).multiply(value);
    }

    private static BigInteger bigMultiplyPowerTen(BigInteger value, int n) {
        if (n <= 0)
            return value;
        if(n<LONG_TEN_POWERS_TABLE.length) {
                return value.multiply(LONG_TEN_POWERS_TABLE[n]);
        }
        return value.multiply(bigTenToThe(n));
    }

    /**
     * 返回一个值为 {@code (xs / ys)} 的 {@code BigDecimal}，根据上下文设置进行舍入。
     *
     * 快速路径 - 仅在 (xscale <= yscale && yscale < 18
     *  && mc.presision<18) 时使用。
     */
    private static BigDecimal divideSmallFastPath(final long xs, int xscale,
                                                  final long ys, int yscale,
                                                  long preferredScale, MathContext mc) {
        int mcp = mc.precision;
        int roundingMode = mc.roundingMode.oldMode;

        assert (xscale <= yscale) && (yscale < 18) && (mcp < 18);
        int xraise = yscale - xscale; // xraise >=0
        long scaledX = (xraise==0) ? xs :
            longMultiplyPowerTen(xs, xraise); // 这里不会溢出！
        BigDecimal quotient;

        int cmp = longCompareMagnitude(scaledX, ys);
        if(cmp > 0) { // 满足约束 (b)
            yscale -= 1; // [即，除数 *= 10]
            int scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp);
            if (checkScaleNonZero((long) mcp + yscale - xscale) > 0) {
                // 断言 newScale >= xscale
                int raise = checkScaleNonZero((long) mcp + yscale - xscale);
                long scaledXs;
                if ((scaledXs = longMultiplyPowerTen(xs, raise)) == INFLATED) {
                    quotient = null;
                    if((mcp-1) >=0 && (mcp-1)<LONG_TEN_POWERS_TABLE.length) {
                        quotient = multiplyDivideAndRound(LONG_TEN_POWERS_TABLE[mcp-1], scaledX, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
                    }
                    if(quotient==null) {
                        BigInteger rb = bigMultiplyPowerTen(scaledX,mcp-1);
                        quotient = divideAndRound(rb, ys,
                                                  scl, roundingMode, checkScaleNonZero(preferredScale));
                    }
                } else {
                    quotient = divideAndRound(scaledXs, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
                }
            } else {
                int newScale = checkScaleNonZero((long) xscale - mcp);
                // 断言 newScale >= yscale
                if (newScale == yscale) { // 简单情况
                    quotient = divideAndRound(xs, ys, scl, roundingMode,checkScaleNonZero(preferredScale));
                } else {
                    int raise = checkScaleNonZero((long) newScale - yscale);
                    long scaledYs;
                    if ((scaledYs = longMultiplyPowerTen(ys, raise)) == INFLATED) {
                        BigInteger rb = bigMultiplyPowerTen(ys,raise);
                        quotient = divideAndRound(BigInteger.valueOf(xs),
                                                  rb, scl, roundingMode,checkScaleNonZero(preferredScale));
                    } else {
                        quotient = divideAndRound(xs, scaledYs, scl, roundingMode,checkScaleNonZero(preferredScale));
                    }
                }
            }
        } else {
            // abs(scaledX) <= abs(ys)
            // 结果是 "scaledX * 10^msp / ys"
            int scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp);
            if(cmp==0) {
                // abs(scaleX)== abs(ys) => 结果将是 10^mcp + 正确的符号
                quotient = roundedTenPower(((scaledX < 0) == (ys < 0)) ? 1 : -1, mcp, scl, checkScaleNonZero(preferredScale));
            } else {
                // abs(scaledX) < abs(ys)
                long scaledXs;
                if ((scaledXs = longMultiplyPowerTen(scaledX, mcp)) == INFLATED) {
                    quotient = null;
                    if(mcp<LONG_TEN_POWERS_TABLE.length) {
                        quotient = multiplyDivideAndRound(LONG_TEN_POWERS_TABLE[mcp], scaledX, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
                    }
                    if(quotient==null) {
                        BigInteger rb = bigMultiplyPowerTen(scaledX,mcp);
                        quotient = divideAndRound(rb, ys,
                                                  scl, roundingMode, checkScaleNonZero(preferredScale));
                    }
                } else {
                    quotient = divideAndRound(scaledXs, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
                }
            }
        }
        // doRound 仅影响 1000000000 情况。
        return doRound(quotient,mc);
    }

    /**
     * 返回一个值为 {@code (xs / ys)} 的 {@code BigDecimal}，根据上下文设置进行舍入。
     */
    private static BigDecimal divide(final long xs, int xscale, final long ys, int yscale, long preferredScale, MathContext mc) {
        int mcp = mc.precision;
        if(xscale <= yscale && yscale < 18 && mcp<18) {
            return divideSmallFastPath(xs, xscale, ys, yscale, preferredScale, mc);
        }
        if (compareMagnitudeNormalized(xs, xscale, ys, yscale) > 0) {// 满足约束 (b)
            yscale -= 1; // [即，除数 *= 10]
        }
        int roundingMode = mc.roundingMode.oldMode;
        // 为了判断除法是否生成精确结果，
        // 我们避免调用上述除法方法。'quotient' 持有返回的 BigDecimal 对象，其 scale 将被设置为 'scl'。
        int scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp);
        BigDecimal quotient;
        if (checkScaleNonZero((long) mcp + yscale - xscale) > 0) {
            int raise = checkScaleNonZero((long) mcp + yscale - xscale);
            long scaledXs;
            if ((scaledXs = longMultiplyPowerTen(xs, raise)) == INFLATED) {
                BigInteger rb = bigMultiplyPowerTen(xs,raise);
                quotient = divideAndRound(rb, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
            } else {
                quotient = divideAndRound(scaledXs, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
            }
        } else {
            int newScale = checkScaleNonZero((long) xscale - mcp);
            // 断言 newScale >= yscale
            if (newScale == yscale) { // 简单情况
                quotient = divideAndRound(xs, ys, scl, roundingMode,checkScaleNonZero(preferredScale));
            } else {
                int raise = checkScaleNonZero((long) newScale - yscale);
                long scaledYs;
                if ((scaledYs = longMultiplyPowerTen(ys, raise)) == INFLATED) {
                    BigInteger rb = bigMultiplyPowerTen(ys,raise);
                    quotient = divideAndRound(BigInteger.valueOf(xs),
                                              rb, scl, roundingMode,checkScaleNonZero(preferredScale));
                } else {
                    quotient = divideAndRound(xs, scaledYs, scl, roundingMode,checkScaleNonZero(preferredScale));
                }
            }
        }
        // doRound 仅影响 1000000000 情况。
        return doRound(quotient,mc);
    }


/**
 * 返回一个值为 {@code (xs / ys)} 的 {@code BigDecimal}，根据上下文设置进行舍入。
 */
private static BigDecimal divide(BigInteger xs, int xscale, long ys, int yscale, long preferredScale, MathContext mc) {
    // 将被除数和除数归一化，使它们都落在 [0.1, 0.999...] 范围内
    if ((-compareMagnitudeNormalized(ys, yscale, xs, xscale)) > 0) { // 满足约束 (b)
        yscale -= 1; // [即，除数 *= 10]
    }
    int mcp = mc.precision;
    int roundingMode = mc.roundingMode.oldMode;

    // 为了确定除法是否产生精确结果，我们避免调用上述除法方法。'quotient' 持有
    // 返回的 BigDecimal 对象，其标度将设置为 'scl'。
    BigDecimal quotient;
    int scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp);
    if (checkScaleNonZero((long) mcp + yscale - xscale) > 0) {
        int raise = checkScaleNonZero((long) mcp + yscale - xscale);
        BigInteger rb = bigMultiplyPowerTen(xs, raise);
        quotient = divideAndRound(rb, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
    } else {
        int newScale = checkScaleNonZero((long) xscale - mcp);
        // assert newScale >= yscale
        if (newScale == yscale) { // 简单情况
            quotient = divideAndRound(xs, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
        } else {
            int raise = checkScaleNonZero((long) newScale - yscale);
            long scaledYs;
            if ((scaledYs = longMultiplyPowerTen(ys, raise)) == INFLATED) {
                BigInteger rb = bigMultiplyPowerTen(ys, raise);
                quotient = divideAndRound(xs, rb, scl, roundingMode, checkScaleNonZero(preferredScale));
            } else {
                quotient = divideAndRound(xs, scaledYs, scl, roundingMode, checkScaleNonZero(preferredScale));
            }
        }
    }
    // doRound 在这里只影响 1000000000 情况。
    return doRound(quotient, mc);
}

/**
 * 返回一个值为 {@code (xs / ys)} 的 {@code BigDecimal}，根据上下文设置进行舍入。
 */
private static BigDecimal divide(long xs, int xscale, BigInteger ys, int yscale, long preferredScale, MathContext mc) {
    // 将被除数和除数归一化，使它们都落在 [0.1, 0.999...] 范围内
    if (compareMagnitudeNormalized(xs, xscale, ys, yscale) > 0) { // 满足约束 (b)
        yscale -= 1; // [即，除数 *= 10]
    }
    int mcp = mc.precision;
    int roundingMode = mc.roundingMode.oldMode;

    // 为了确定除法是否产生精确结果，我们避免调用上述除法方法。'quotient' 持有
    // 返回的 BigDecimal 对象，其标度将设置为 'scl'。
    BigDecimal quotient;
    int scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp);
    if (checkScaleNonZero((long) mcp + yscale - xscale) > 0) {
        int raise = checkScaleNonZero((long) mcp + yscale - xscale);
        BigInteger rb = bigMultiplyPowerTen(xs, raise);
        quotient = divideAndRound(rb, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
    } else {
        int newScale = checkScaleNonZero((long) xscale - mcp);
        int raise = checkScaleNonZero((long) newScale - yscale);
        BigInteger rb = bigMultiplyPowerTen(ys, raise);
        quotient = divideAndRound(BigInteger.valueOf(xs), rb, scl, roundingMode, checkScaleNonZero(preferredScale));
    }
    // doRound 在这里只影响 1000000000 情况。
    return doRound(quotient, mc);
}

/**
 * 返回一个值为 {@code (xs / ys)} 的 {@code BigDecimal}，根据上下文设置进行舍入。
 */
private static BigDecimal divide(BigInteger xs, int xscale, BigInteger ys, int yscale, long preferredScale, MathContext mc) {
    // 将被除数和除数归一化，使它们都落在 [0.1, 0.999...] 范围内
    if (compareMagnitudeNormalized(xs, xscale, ys, yscale) > 0) { // 满足约束 (b)
        yscale -= 1; // [即，除数 *= 10]
    }
    int mcp = mc.precision;
    int roundingMode = mc.roundingMode.oldMode;

    // 为了确定除法是否产生精确结果，我们避免调用上述除法方法。'quotient' 持有
    // 返回的 BigDecimal 对象，其标度将设置为 'scl'。
    BigDecimal quotient;
    int scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp);
    if (checkScaleNonZero((long) mcp + yscale - xscale) > 0) {
        int raise = checkScaleNonZero((long) mcp + yscale - xscale);
        BigInteger rb = bigMultiplyPowerTen(xs, raise);
        quotient = divideAndRound(rb, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
    } else {
        int newScale = checkScaleNonZero((long) xscale - mcp);
        int raise = checkScaleNonZero((long) newScale - yscale);
        BigInteger rb = bigMultiplyPowerTen(ys, raise);
        quotient = divideAndRound(xs, rb, scl, roundingMode, checkScaleNonZero(preferredScale));
    }
    // doRound 在这里只影响 1000000000 情况。
    return doRound(quotient, mc);
}

/*
 * 对 (dividend0*dividend1, divisor) 进行 divideAndRound
 * 如果商不能适应 long 值，则返回 null；
 */
private static BigDecimal multiplyDivideAndRound(long dividend0, long dividend1, long divisor, int scale, int roundingMode,
                                                 int preferredScale) {
    int qsign = Long.signum(dividend0) * Long.signum(dividend1) * Long.signum(divisor);
    dividend0 = Math.abs(dividend0);
    dividend1 = Math.abs(dividend1);
    divisor = Math.abs(divisor);
    // 计算 dividend0 * dividend1
    long d0_hi = dividend0 >>> 32;
    long d0_lo = dividend0 & LONG_MASK;
    long d1_hi = dividend1 >>> 32;
    long d1_lo = dividend1 & LONG_MASK;
    long product = d0_lo * d1_lo;
    long d0 = product & LONG_MASK;
    long d1 = product >>> 32;
    product = d0_hi * d1_lo + d1;
    d1 = product & LONG_MASK;
    long d2 = product >>> 32;
    product = d0_lo * d1_hi + d1;
    d1 = product & LONG_MASK;
    d2 += product >>> 32;
    long d3 = d2 >>> 32;
    d2 &= LONG_MASK;
    product = d0_hi * d1_hi + d2;
    d2 = product & LONG_MASK;
    d3 = ((product >>> 32) + d3) & LONG_MASK;
    final long dividendHi = make64(d3, d2);
    final long dividendLo = make64(d1, d0);
    // 除法
    return divideAndRound128(dividendHi, dividendLo, divisor, qsign, scale, roundingMode, preferredScale);
}


                private static final long DIV_NUM_BASE = (1L<<32); // 数字基数（32位）。

    /*
     * 除以 long 除数并四舍五入 128 位值。
     * 如果商不能放入 long 值中，则返回 null；
     * 专门的 Knuth 除法版本
     */
    private static BigDecimal divideAndRound128(final long dividendHi, final long dividendLo, long divisor, int sign,
                                                int scale, int roundingMode, int preferredScale) {
        if (dividendHi >= divisor) {
            return null;
        }

        final int shift = Long.numberOfLeadingZeros(divisor);
        divisor <<= shift;

        final long v1 = divisor >>> 32;
        final long v0 = divisor & LONG_MASK;

        long tmp = dividendLo << shift;
        long u1 = tmp >>> 32;
        long u0 = tmp & LONG_MASK;

        tmp = (dividendHi << shift) | (dividendLo >>> 64 - shift);
        long u2 = tmp & LONG_MASK;
        long q1, r_tmp;
        if (v1 == 1) {
            q1 = tmp;
            r_tmp = 0;
        } else if (tmp >= 0) {
            q1 = tmp / v1;
            r_tmp = tmp - q1 * v1;
        } else {
            long[] rq = divRemNegativeLong(tmp, v1);
            q1 = rq[1];
            r_tmp = rq[0];
        }

        while(q1 >= DIV_NUM_BASE || unsignedLongCompare(q1*v0, make64(r_tmp, u1))) {
            q1--;
            r_tmp += v1;
            if (r_tmp >= DIV_NUM_BASE)
                break;
        }

        tmp = mulsub(u2,u1,v1,v0,q1);
        u1 = tmp & LONG_MASK;
        long q0;
        if (v1 == 1) {
            q0 = tmp;
            r_tmp = 0;
        } else if (tmp >= 0) {
            q0 = tmp / v1;
            r_tmp = tmp - q0 * v1;
        } else {
            long[] rq = divRemNegativeLong(tmp, v1);
            q0 = rq[1];
            r_tmp = rq[0];
        }

        while(q0 >= DIV_NUM_BASE || unsignedLongCompare(q0*v0,make64(r_tmp,u0))) {
            q0--;
            r_tmp += v1;
            if (r_tmp >= DIV_NUM_BASE)
                break;
        }

        if((int)q1 < 0) {
            // 结果（在此处为正且无符号）
            // 由于符号位用于值，无法放入 long 中
            MutableBigInteger mq = new MutableBigInteger(new int[]{(int)q1, (int)q0});
            if (roundingMode == ROUND_DOWN && scale == preferredScale) {
                return mq.toBigDecimal(sign, scale);
            }
            long r = mulsub(u1, u0, v1, v0, q0) >>> shift;
            if (r != 0) {
                if(needIncrement(divisor >>> shift, roundingMode, sign, mq, r)){
                    mq.add(MutableBigInteger.ONE);
                }
                return mq.toBigDecimal(sign, scale);
            } else {
                if (preferredScale != scale) {
                    BigInteger intVal =  mq.toBigInteger(sign);
                    return createAndStripZerosToMatchScale(intVal,scale, preferredScale);
                } else {
                    return mq.toBigDecimal(sign, scale);
                }
            }
        }

        long q = make64(q1,q0);
        q*=sign;

        if (roundingMode == ROUND_DOWN && scale == preferredScale)
            return valueOf(q, scale);

        long r = mulsub(u1, u0, v1, v0, q0) >>> shift;
        if (r != 0) {
            boolean increment = needIncrement(divisor >>> shift, roundingMode, sign, q, r);
            return valueOf((increment ? q + sign : q), scale);
        } else {
            if (preferredScale != scale) {
                return createAndStripZerosToMatchScale(q, scale, preferredScale);
            } else {
                return valueOf(q, scale);
            }
        }
    }

    /*
     * 计算 ldividend*10^raise / divisor 的四舍五入值
     * 当 abs(dividend)==abs(divisor) 时
     */
    private static BigDecimal roundedTenPower(int qsign, int raise, int scale, int preferredScale) {
        if (scale > preferredScale) {
            int diff = scale - preferredScale;
            if(diff < raise) {
                return scaledTenPow(raise - diff, qsign, preferredScale);
            } else {
                return valueOf(qsign,scale-raise);
            }
        } else {
            return scaledTenPow(raise, qsign, scale);
        }
    }

    static BigDecimal scaledTenPow(int n, int sign, int scale) {
        if (n < LONG_TEN_POWERS_TABLE.length)
            return valueOf(sign*LONG_TEN_POWERS_TABLE[n],scale);
        else {
            BigInteger unscaledVal = bigTenToThe(n);
            if(sign==-1) {
                unscaledVal = unscaledVal.negate();
            }
            return new BigDecimal(unscaledVal, INFLATED, scale, n+1);
        }
    }

    /**
     * 计算一个负 long 除以另一个 long 的商和余数。
     *
     * @param n 分子；必须为负数
     * @param d 分母；不能为 1
     * @return 一个包含余数和商的两元素 {@long} 数组，分别位于数组的初始和最终位置
     */
    private static long[] divRemNegativeLong(long n, long d) {
        assert n < 0 : "非负数分子 " + n;
        assert d != 1 : "单位分母";

        // 近似商和余数
        long q = (n >>> 1) / (d >>> 1);
        long r = n - q * d;

        // 修正近似值
        while (r < 0) {
            r += d;
            q--;
        }
        while (r >= d) {
            r -= d;
            q++;
        }

        // n - q*d == r && 0 <= r < d，因此完成。
        return new long[] {r, q};
    }

    private static long make64(long hi, long lo) {
        return hi<<32 | lo;
    }

    private static long mulsub(long u1, long u0, final long v1, final long v0, long q0) {
        long tmp = u0 - q0*v0;
        return make64(u1 + (tmp>>>32) - q0*v1,tmp & LONG_MASK);
    }

    private static boolean unsignedLongCompare(long one, long two) {
        return (one+Long.MIN_VALUE) > (two+Long.MIN_VALUE);
    }

    private static boolean unsignedLongCompareEq(long one, long two) {
        return (one+Long.MIN_VALUE) >= (two+Long.MIN_VALUE);
    }


    // 比较并归一化被除数和除数，使它们都落在 [0.1, 0.999...] 范围内
    private static int compareMagnitudeNormalized(long xs, int xscale, long ys, int yscale) {
        // 断言 xs 和 ys 都不为 0
        int sdiff = xscale - yscale;
        if (sdiff != 0) {
            if (sdiff < 0) {
                xs = longMultiplyPowerTen(xs, -sdiff);
            } else { // sdiff > 0
                ys = longMultiplyPowerTen(ys, sdiff);
            }
        }
        if (xs != INFLATED)
            return (ys != INFLATED) ? longCompareMagnitude(xs, ys) : -1;
        else
            return 1;
    }

    // 比较并归一化被除数和除数，使它们都落在 [0.1, 0.999...] 范围内
    private static int compareMagnitudeNormalized(long xs, int xscale, BigInteger ys, int yscale) {
        // 断言 "ys 不能表示为 long"
        if (xs == 0)
            return -1;
        int sdiff = xscale - yscale;
        if (sdiff < 0) {
            if (longMultiplyPowerTen(xs, -sdiff) == INFLATED ) {
                return bigMultiplyPowerTen(xs, -sdiff).compareMagnitude(ys);
            }
        }
        return -1;
    }

    // 比较并归一化被除数和除数，使它们都落在 [0.1, 0.999...] 范围内
    private static int compareMagnitudeNormalized(BigInteger xs, int xscale, BigInteger ys, int yscale) {
        int sdiff = xscale - yscale;
        if (sdiff < 0) {
            return bigMultiplyPowerTen(xs, -sdiff).compareMagnitude(ys);
        } else { // sdiff >= 0
            return xs.compareMagnitude(bigMultiplyPowerTen(ys, sdiff));
        }
    }

    private static long multiply(long x, long y){
                long product = x * y;
        long ax = Math.abs(x);
        long ay = Math.abs(y);
        if (((ax | ay) >>> 31 == 0) || (y == 0) || (product / y == x)){
                        return product;
                }
        return INFLATED;
    }

    private static BigDecimal multiply(long x, long y, int scale) {
        long product = multiply(x, y);
        if(product!=INFLATED) {
            return valueOf(product,scale);
        }
        return new BigDecimal(BigInteger.valueOf(x).multiply(y),INFLATED,scale,0);
    }

    private static BigDecimal multiply(long x, BigInteger y, int scale) {
        if(x==0) {
            return zeroValueOf(scale);
        }
        return new BigDecimal(y.multiply(x),INFLATED,scale,0);
    }

    private static BigDecimal multiply(BigInteger x, BigInteger y, int scale) {
        return new BigDecimal(x.multiply(y),INFLATED,scale,0);
    }

    /**
     * 乘以两个 long 值并根据 {@code MathContext} 进行舍入
     */
    private static BigDecimal multiplyAndRound(long x, long y, int scale, MathContext mc) {
        long product = multiply(x, y);
        if(product!=INFLATED) {
            return doRound(product, scale, mc);
        }
        // 尝试在 128 位中完成
        int rsign = 1;
        if(x < 0) {
            x = -x;
            rsign = -1;
        }
        if(y < 0) {
            y = -y;
            rsign *= -1;
        }
        // 乘以 dividend0 * dividend1
        long m0_hi = x >>> 32;
        long m0_lo = x & LONG_MASK;
        long m1_hi = y >>> 32;
        long m1_lo = y & LONG_MASK;
        product = m0_lo * m1_lo;
        long m0 = product & LONG_MASK;
        long m1 = product >>> 32;
        product = m0_hi * m1_lo + m1;
        m1 = product & LONG_MASK;
        long m2 = product >>> 32;
        product = m0_lo * m1_hi + m1;
        m1 = product & LONG_MASK;
        m2 += product >>> 32;
        long m3 = m2>>>32;
        m2 &= LONG_MASK;
        product = m0_hi*m1_hi + m2;
        m2 = product & LONG_MASK;
        m3 = ((product>>>32) + m3) & LONG_MASK;
        final long mHi = make64(m3,m2);
        final long mLo = make64(m1,m0);
        BigDecimal res = doRound128(mHi, mLo, rsign, scale, mc);
        if(res!=null) {
            return res;
        }
        res = new BigDecimal(BigInteger.valueOf(x).multiply(y*rsign), INFLATED, scale, 0);
        return doRound(res,mc);
    }

    private static BigDecimal multiplyAndRound(long x, BigInteger y, int scale, MathContext mc) {
        if(x==0) {
            return zeroValueOf(scale);
        }
        return doRound(y.multiply(x), scale, mc);
    }

    private static BigDecimal multiplyAndRound(BigInteger x, BigInteger y, int scale, MathContext mc) {
        return doRound(x.multiply(y), scale, mc);
    }

    /**
     * 根据 {@code MathContext} 舍入 128 位值
     * 如果结果不能表示为紧凑的 BigDecimal，则返回 null。
     */
    private static BigDecimal doRound128(long hi, long lo, int sign, int scale, MathContext mc) {
        int mcp = mc.precision;
        int drop;
        BigDecimal res = null;
        if(((drop = precision(hi, lo) - mcp) > 0)&&(drop<LONG_TEN_POWERS_TABLE.length)) {
            scale = checkScaleNonZero((long)scale - drop);
            res = divideAndRound128(hi, lo, LONG_TEN_POWERS_TABLE[drop], sign, scale, mc.roundingMode.oldMode, scale);
        }
        if(res!=null) {
            return doRound(res,mc);
        }
        return null;
    }

    private static final long[][] LONGLONG_TEN_POWERS_TABLE = {
        {   0L, 0x8AC7230489E80000L },  //10^19
        {       0x5L, 0x6bc75e2d63100000L },  //10^20
        {       0x36L, 0x35c9adc5dea00000L },  //10^21
        {       0x21eL, 0x19e0c9bab2400000L  },  //10^22
        {       0x152dL, 0x02c7e14af6800000L  },  //10^23
        {       0xd3c2L, 0x1bcecceda1000000L  },  //10^24
        {       0x84595L, 0x161401484a000000L  },  //10^25
        {       0x52b7d2L, 0xdcc80cd2e4000000L  },  //10^26
        {       0x33b2e3cL, 0x9fd0803ce8000000L  },  //10^27
        {       0x204fce5eL, 0x3e25026110000000L  },  //10^28
        {       0x1431e0faeL, 0x6d7217caa0000000L  },  //10^29
        {       0xc9f2c9cd0L, 0x4674edea40000000L  },  //10^30
        {       0x7e37be2022L, 0xc0914b2680000000L  },  //10^31
        {       0x4ee2d6d415bL, 0x85acef8100000000L  },  //10^32
        {       0x314dc6448d93L, 0x38c15b0a00000000L  },  //10^33
        {       0x1ed09bead87c0L, 0x378d8e6400000000L  },  //10^34
        {       0x13426172c74d82L, 0x2b878fe800000000L  },  //10^35
        {       0xc097ce7bc90715L, 0xb34b9f1000000000L  },  //10^36
        {       0x785ee10d5da46d9L, 0x00f436a000000000L  },  //10^37
        {       0x4b3b4ca85a86c47aL, 0x098a224000000000L  },  //10^38
    };


                /*
     * 返回128位值的精度
     */
    private static int precision(long hi, long lo){
        if(hi==0) {
            if(lo>=0) {
                return longDigitLength(lo);
            }
            return (unsignedLongCompareEq(lo, LONGLONG_TEN_POWERS_TABLE[0][1])) ? 20 : 19;
            // 0x8AC7230489E80000L  = 无符号 2^19
        }
        int r = ((128 - Long.numberOfLeadingZeros(hi) + 1) * 1233) >>> 12;
        int idx = r-19;
        return (idx >= LONGLONG_TEN_POWERS_TABLE.length || longLongCompareMagnitude(hi, lo,
                                                                                    LONGLONG_TEN_POWERS_TABLE[idx][0], LONGLONG_TEN_POWERS_TABLE[idx][1])) ? r : r + 1;
    }

    /*
     * 如果128位数 <hi0,lo0> 小于 <hi1,lo1>，则返回true
     * hi0 & hi1 应为非负数
     */
    private static boolean longLongCompareMagnitude(long hi0, long lo0, long hi1, long lo1) {
        if(hi0!=hi1) {
            return hi0<hi1;
        }
        return (lo0+Long.MIN_VALUE) <(lo1+Long.MIN_VALUE);
    }

    private static BigDecimal divide(long dividend, int dividendScale, long divisor, int divisorScale, int scale, int roundingMode) {
        if (checkScale(dividend,(long)scale + divisorScale) > dividendScale) {
            int newScale = scale + divisorScale;
            int raise = newScale - dividendScale;
            if(raise<LONG_TEN_POWERS_TABLE.length) {
                long xs = dividend;
                if ((xs = longMultiplyPowerTen(xs, raise)) != INFLATED) {
                    return divideAndRound(xs, divisor, scale, roundingMode, scale);
                }
                BigDecimal q = multiplyDivideAndRound(LONG_TEN_POWERS_TABLE[raise], dividend, divisor, scale, roundingMode, scale);
                if(q!=null) {
                    return q;
                }
            }
            BigInteger scaledDividend = bigMultiplyPowerTen(dividend, raise);
            return divideAndRound(scaledDividend, divisor, scale, roundingMode, scale);
        } else {
            int newScale = checkScale(divisor,(long)dividendScale - scale);
            int raise = newScale - divisorScale;
            if(raise<LONG_TEN_POWERS_TABLE.length) {
                long ys = divisor;
                if ((ys = longMultiplyPowerTen(ys, raise)) != INFLATED) {
                    return divideAndRound(dividend, ys, scale, roundingMode, scale);
                }
            }
            BigInteger scaledDivisor = bigMultiplyPowerTen(divisor, raise);
            return divideAndRound(BigInteger.valueOf(dividend), scaledDivisor, scale, roundingMode, scale);
        }
    }

    private static BigDecimal divide(BigInteger dividend, int dividendScale, long divisor, int divisorScale, int scale, int roundingMode) {
        if (checkScale(dividend,(long)scale + divisorScale) > dividendScale) {
            int newScale = scale + divisorScale;
            int raise = newScale - dividendScale;
            BigInteger scaledDividend = bigMultiplyPowerTen(dividend, raise);
            return divideAndRound(scaledDividend, divisor, scale, roundingMode, scale);
        } else {
            int newScale = checkScale(divisor,(long)dividendScale - scale);
            int raise = newScale - divisorScale;
            if(raise<LONG_TEN_POWERS_TABLE.length) {
                long ys = divisor;
                if ((ys = longMultiplyPowerTen(ys, raise)) != INFLATED) {
                    return divideAndRound(dividend, ys, scale, roundingMode, scale);
                }
            }
            BigInteger scaledDivisor = bigMultiplyPowerTen(divisor, raise);
            return divideAndRound(dividend, scaledDivisor, scale, roundingMode, scale);
        }
    }

    private static BigDecimal divide(long dividend, int dividendScale, BigInteger divisor, int divisorScale, int scale, int roundingMode) {
        if (checkScale(dividend,(long)scale + divisorScale) > dividendScale) {
            int newScale = scale + divisorScale;
            int raise = newScale - dividendScale;
            BigInteger scaledDividend = bigMultiplyPowerTen(dividend, raise);
            return divideAndRound(scaledDividend, divisor, scale, roundingMode, scale);
        } else {
            int newScale = checkScale(divisor,(long)dividendScale - scale);
            int raise = newScale - divisorScale;
            BigInteger scaledDivisor = bigMultiplyPowerTen(divisor, raise);
            return divideAndRound(BigInteger.valueOf(dividend), scaledDivisor, scale, roundingMode, scale);
        }
    }

    private static BigDecimal divide(BigInteger dividend, int dividendScale, BigInteger divisor, int divisorScale, int scale, int roundingMode) {
        if (checkScale(dividend,(long)scale + divisorScale) > dividendScale) {
            int newScale = scale + divisorScale;
            int raise = newScale - dividendScale;
            BigInteger scaledDividend = bigMultiplyPowerTen(dividend, raise);
            return divideAndRound(scaledDividend, divisor, scale, roundingMode, scale);
        } else {
            int newScale = checkScale(divisor,(long)dividendScale - scale);
            int raise = newScale - divisorScale;
            BigInteger scaledDivisor = bigMultiplyPowerTen(divisor, raise);
            return divideAndRound(dividend, scaledDivisor, scale, roundingMode, scale);
        }
    }

}
