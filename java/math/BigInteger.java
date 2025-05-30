
/*
 * Copyright (c) 1996, 2018, Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyright (c) 1995  Colin Plumb.  All rights reserved.
 */

package java.math;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.ObjectStreamException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import sun.misc.DoubleConsts;
import sun.misc.FloatConsts;

/**
 * 不可变的任意精度整数。所有操作的行为都如同BigInteger是以二进制补码表示的（类似于Java的原始整数类型）。BigInteger提供了Java所有原始整数运算符的类似物，以及java.lang.Math中的所有相关方法。此外，BigInteger还提供了模运算、最大公约数计算、素性测试、素数生成、位操作和其他一些杂项操作。
 *
 * <p>算术运算的语义完全模仿Java的整数算术运算符的语义，如《Java语言规范》中所定义。例如，除以零会抛出一个ArithmeticException，负数除以正数会得到一个负数（或零）的余数。规范中关于溢出的所有细节都被忽略，因为BigInteger会根据需要变得足够大以容纳操作的结果。
 *
 * <p>移位运算的语义扩展了Java的移位运算符，以允许负的移位距离。右移负的移位距离会导致左移，反之亦然。无符号右移运算符（{@code >>>}）被省略，因为这种操作与本类提供的“无限字长”抽象结合时没有意义。
 *
 * <p>位逻辑运算的语义完全模仿Java的位整数运算符。二进制运算符（{@code and}，{@code or}，{@code xor}）在执行操作之前会隐式地对较短的操作数进行符号扩展。
 *
 * <p>比较运算执行带符号的整数比较，类似于Java的关系和相等运算符。
 *
 * <p>模运算提供了计算余数、执行指数运算和计算乘法逆元的方法。这些方法总是返回一个非负结果，介于{@code 0}和{@code (模数 - 1)}之间，包括这两个值。
 *
 * <p>位操作对操作数的二进制补码表示中的单个位进行操作。如果需要，操作数会被符号扩展以包含指定的位。任何单个位操作都不会产生与操作数具有不同符号的BigInteger，因为它们只影响单个位，而且本类提供的“无限字长”抽象确保每个BigInteger都有无限多个“虚拟符号位”。
 *
 * <p>为了简洁和清晰，BigInteger方法的描述中使用了伪代码。伪代码表达式{@code (i + j)}是“一个其值为BigInteger {@code i}加上BigInteger {@code j}的值的BigInteger”的简写。伪代码表达式{@code (i == j)}是“当且仅当BigInteger {@code i}表示的值与BigInteger {@code j}表示的值相同时为{@code true}”的简写。其他伪代码表达式也类似解释。
 *
 * <p>本类中的所有方法和构造函数在接收到任何输入参数的null对象引用时都会抛出{@code NullPointerException}。
 *
 * BigInteger必须支持值的范围为
 * -2<sup>{@code Integer.MAX_VALUE}</sup>（不包括）到
 * +2<sup>{@code Integer.MAX_VALUE}</sup>（不包括）
 * 并且可能支持该范围之外的值。
 *
 * 可能的素数值的范围是有限的，可能小于BigInteger支持的完整正数范围。该范围至少为1到2<sup>500000000</sup>。
 *
 * @implNote
 * BigInteger构造函数和操作在结果超出
 * -2<sup>{@code Integer.MAX_VALUE}</sup>（不包括）到
 * +2<sup>{@code Integer.MAX_VALUE}</sup>（不包括）的范围时会抛出{@code ArithmeticException}。
 *
 * @see     BigDecimal
 * @author  Josh Bloch
 * @author  Michael McCloskey
 * @author  Alan Eliasen
 * @author  Timothy Buktu
 * @since JDK1.1
 */

public class BigInteger extends Number implements Comparable<BigInteger> {
    /**
     * 此BigInteger的符号：负数为-1，零为0，正数为1。注意，BigInteger零<i>必须</i>有一个符号为0。这是为了确保每个BigInteger值都有唯一表示。
     *
     * @serial
     */
    final int signum;

    /**
     * 此BigInteger的大小，以<i>大端</i>顺序排列：此数组的零元素是大小的最高有效int。大小必须是“最小的”，即最高有效int（{@code mag[0]}）必须非零。这是为了确保每个BigInteger值都有唯一表示。注意，这暗示BigInteger零有一个零长度的mag数组。
     */
    final int[] mag;

    // 这些“冗余字段”被初始化为可识别的无意义值，并在需要时（或从不，如果不需要的话）缓存。

     /**
     * 此BigInteger的bitCount加一。零表示未初始化。
     *
     * @serial
     * @see #bitCount
     * @deprecated 由于逻辑值与存储值有偏移，访问器方法中应用了校正因子，因此已弃用。
     */
    @Deprecated
    private int bitCount;

    /**
     * 此BigInteger的bitLength加一。零表示未初始化。
     * （任一值都是可接受的）。
     *
     * @serial
     * @see #bitLength()
     * @deprecated 由于逻辑值与存储值有偏移，访问器方法中应用了校正因子，因此已弃用。
     */
    @Deprecated
    private int bitLength;

    /**
     * 此BigInteger的最低设置位，由getLowestSetBit()返回，加二。
     *
     * @serial
     * @see #getLowestSetBit
     * @deprecated 由于逻辑值与存储值有偏移，访问器方法中应用了校正因子，因此已弃用。
     */
    @Deprecated
    private int lowestSetBit;

    /**
     * 此BigInteger大小中包含非零int的最低阶int的索引加二，或-2（任一值都是可接受的）。最低有效int的int编号为0，下一个按重要性增加的int的int编号为1，依此类推。
     * @deprecated 由于逻辑值与存储值有偏移，访问器方法中应用了校正因子，因此已弃用。
     */
    @Deprecated
    private int firstNonzeroIntNum;

    /**
     * 用于获取int的值，就像它是无符号的一样。
     */
    final static long LONG_MASK = 0xffffffffL;

    /**
     * 限制BigIntegers的{@code mag.length}的常量，以支持的范围。
     */
    private static final int MAX_MAG_LENGTH = Integer.MAX_VALUE / Integer.SIZE + 1; // (1 << 26)

    /**
     * 比此常量更大的位长度可能导致searchLen计算和BitSieve.singleSearch方法中的溢出。
     */
    private static final  int PRIME_SEARCH_BIT_LENGTH_LIMIT = 500000000;

    /**
     * 使用Karatsuba乘法的阈值。如果两个mag数组中的int数量都大于此值，则使用Karatsuba乘法。此值通过实验发现效果良好。
     */
    private static final int KARATSUBA_THRESHOLD = 80;

    /**
     * 使用3路Toom-Cook乘法的阈值。如果每个mag数组中的int数量大于Karatsuba阈值，并且至少一个mag数组中的int数量大于此阈值，则使用Toom-Cook乘法。
     */
    private static final int TOOM_COOK_THRESHOLD = 240;

    /**
     * 使用Karatsuba平方的阈值。如果数字中的int数量大于此值，则使用Karatsuba平方。此值通过实验发现效果良好。
     */
    private static final int KARATSUBA_SQUARE_THRESHOLD = 128;

    /**
     * 使用Toom-Cook平方的阈值。如果数字中的int数量大于此值，则使用Toom-Cook平方。此值通过实验发现效果良好。
     */
    private static final int TOOM_COOK_SQUARE_THRESHOLD = 216;

    /**
     * 使用Burnikel-Ziegler除法的阈值。如果除数中的int数量大于此值，可能会使用Burnikel-Ziegler除法。此值通过实验发现效果良好。
     */
    static final int BURNIKEL_ZIEGLER_THRESHOLD = 80;

    /**
     * 使用Burnikel-Ziegler除法的偏移值。如果除数中的int数量超过Burnikel-Ziegler阈值，并且被除数中的int数量大于除数中的int数量加上此值，则使用Burnikel-Ziegler除法。此值通过实验发现效果良好。
     */
    static final int BURNIKEL_ZIEGLER_OFFSET = 40;

    /**
     * 使用Schoenhage递归基数转换的阈值。如果数字中的int数量大于此值，则使用Schoenhage算法。实际上，对于任何低至2的阈值，Schoenhage例程似乎都更快，并且对于2-25之间的阈值相对平坦，因此可以在这一范围内选择此值以获得非常小的效果。
     */
    private static final int SCHOENHAGE_BASE_CONVERSION_THRESHOLD = 20;

    /**
     * 使用平方代码执行BigInteger实例自乘的阈值。如果数字中的int数量大于此值，{@code multiply(this)}将返回{@code square()}。
     */
    private static final int MULTIPLY_SQUARE_THRESHOLD = 20;

    /**
     * 使用内在版本的implMontgomeryXXX执行Montgomery乘法的阈值。如果数字中的int数量大于此值，我们不使用内在版本。
     */
    private static final int MONTGOMERY_INTRINSIC_THRESHOLD = 512;


    // 构造函数

    /**
     * 将包含BigInteger的二进制补码表示的字节数组转换为BigInteger。假定输入数组是以<i>大端</i>字节顺序排列的：最高有效字节在零元素中。
     *
     * @param  val 大端二进制补码表示的BigInteger。
     * @throws NumberFormatException {@code val}长度为零。
     */
    public BigInteger(byte[] val) {
        if (val.length == 0)
            throw new NumberFormatException("Zero length BigInteger");

        if (val[0] < 0) {
            mag = makePositive(val);
            signum = -1;
        } else {
            mag = stripLeadingZeroBytes(val);
            signum = (mag.length == 0 ? 0 : 1);
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * 将包含BigInteger的二进制补码表示的int数组转换为BigInteger。假定输入数组是以<i>大端</i>int顺序排列的：最高有效int在零元素中。
     */
    private BigInteger(int[] val) {
        if (val.length == 0)
            throw new NumberFormatException("Zero length BigInteger");

        if (val[0] < 0) {
            mag = makePositive(val);
            signum = -1;
        } else {
            mag = trustedStripLeadingZeroInts(val);
            signum = (mag.length == 0 ? 0 : 1);
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * 将BigInteger的符号-大小表示转换为BigInteger。符号表示为一个整数signum值：-1表示负数，0表示零，1表示正数。大小是一个以<i>大端</i>字节顺序排列的字节数组：最高有效字节在零元素中。允许零长度大小数组，结果将是一个值为0的BigInteger，无论signum是-1、0还是1。
     *
     * @param  signum 数字的符号（-1表示负数，0表示零，1表示正数）。
     * @param  magnitude 数字大小的二进制表示。
     * @throws NumberFormatException {@code signum}不是三个合法值（-1、0和1）之一，或{@code signum}为0且{@code magnitude}包含一个或多个非零字节。
     */
    public BigInteger(int signum, byte[] magnitude) {
        this.mag = stripLeadingZeroBytes(magnitude);

        if (signum < -1 || signum > 1)
            throw(new NumberFormatException("Invalid signum value"));

        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            if (signum == 0)
                throw(new NumberFormatException("signum-magnitude mismatch"));
            this.signum = signum;
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * 一个用于内部使用的构造函数，将BigInteger的符号-大小表示转换为BigInteger。它检查参数并复制大小，因此这个构造函数对外部使用是安全的。
     */
    private BigInteger(int signum, int[] magnitude) {
        this.mag = stripLeadingZeroInts(magnitude);


                    if (signum < -1 || signum > 1)
            throw(new NumberFormatException("无效的 signum 值"));

        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            if (signum == 0)
                throw(new NumberFormatException("signum-数值不匹配"));
            this.signum = signum;
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * 将指定基数的 BigInteger 的字符串表示形式转换为 BigInteger。字符串表示形式由可选的负号或正号后跟一个或多个指定基数的数字序列组成。字符到数字的映射由 {@code
     * Character.digit} 提供。字符串中不得包含任何多余的字符（例如空格）。
     *
     * @param val BigInteger 的字符串表示形式。
     * @param radix 用于解释 {@code val} 的基数。
     * @throws NumberFormatException {@code val} 不是 BigInteger 在指定基数下的有效表示形式，或者 {@code radix} 超出了从 {@link Character#MIN_RADIX} 到
     *         {@link Character#MAX_RADIX} 的范围。
     * @see    Character#digit
     */
    public BigInteger(String val, int radix) {
        int cursor = 0, numDigits;
        final int len = val.length();

        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            throw new NumberFormatException("基数超出范围");
        if (len == 0)
            throw new NumberFormatException("零长度的 BigInteger");

        // 检查最多一个前导符号
        int sign = 1;
        int index1 = val.lastIndexOf('-');
        int index2 = val.lastIndexOf('+');
        if (index1 >= 0) {
            if (index1 != 0 || index2 >= 0) {
                throw new NumberFormatException("非法的嵌入符号字符");
            }
            sign = -1;
            cursor = 1;
        } else if (index2 >= 0) {
            if (index2 != 0) {
                throw new NumberFormatException("非法的嵌入符号字符");
            }
            cursor = 1;
        }
        if (cursor == len)
            throw new NumberFormatException("零长度的 BigInteger");

        // 跳过前导零并计算数值中的数字数量
        while (cursor < len &&
               Character.digit(val.charAt(cursor), radix) == 0) {
            cursor++;
        }

        if (cursor == len) {
            signum = 0;
            mag = ZERO.mag;
            return;
        }

        numDigits = len - cursor;
        signum = sign;

        // 预分配预期大小的数组。可能太大，但绝不会太小。通常情况下是准确的。
        long numBits = ((numDigits * bitsPerDigit[radix]) >>> 10) + 1;
        if (numBits + 31 >= (1L << 32)) {
            reportOverflow();
        }
        int numWords = (int) (numBits + 31) >>> 5;
        int[] magnitude = new int[numWords];

        // 处理第一个（可能是短的）数字组
        int firstGroupLen = numDigits % digitsPerInt[radix];
        if (firstGroupLen == 0)
            firstGroupLen = digitsPerInt[radix];
        String group = val.substring(cursor, cursor += firstGroupLen);
        magnitude[numWords - 1] = Integer.parseInt(group, radix);
        if (magnitude[numWords - 1] < 0)
            throw new NumberFormatException("非法的数字");

        // 处理剩余的数字组
        int superRadix = intRadix[radix];
        int groupVal = 0;
        while (cursor < len) {
            group = val.substring(cursor, cursor += digitsPerInt[radix]);
            groupVal = Integer.parseInt(group, radix);
            if (groupVal < 0)
                throw new NumberFormatException("非法的数字");
            destructiveMulAdd(magnitude, superRadix, groupVal);
        }
        // 用于处理数组过度分配的情况。
        mag = trustedStripLeadingZeroInts(magnitude);
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /*
     * 使用 char 数组和 radix=10 构造一个新的 BigInteger。符号由外部预计算，不允许在 val 中。
     */
    BigInteger(char[] val, int sign, int len) {
        int cursor = 0, numDigits;

        // 跳过前导零并计算数值中的数字数量
        while (cursor < len && Character.digit(val[cursor], 10) == 0) {
            cursor++;
        }
        if (cursor == len) {
            signum = 0;
            mag = ZERO.mag;
            return;
        }

        numDigits = len - cursor;
        signum = sign;
        // 预分配预期大小的数组
        int numWords;
        if (len < 10) {
            numWords = 1;
        } else {
            long numBits = ((numDigits * bitsPerDigit[10]) >>> 10) + 1;
            if (numBits + 31 >= (1L << 32)) {
                reportOverflow();
            }
            numWords = (int) (numBits + 31) >>> 5;
        }
        int[] magnitude = new int[numWords];

        // 处理第一个（可能是短的）数字组
        int firstGroupLen = numDigits % digitsPerInt[10];
        if (firstGroupLen == 0)
            firstGroupLen = digitsPerInt[10];
        magnitude[numWords - 1] = parseInt(val, cursor,  cursor += firstGroupLen);

        // 处理剩余的数字组
        while (cursor < len) {
            int groupVal = parseInt(val, cursor, cursor += digitsPerInt[10]);
            destructiveMulAdd(magnitude, intRadix[10], groupVal);
        }
        mag = trustedStripLeadingZeroInts(magnitude);
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    // 创建一个介于两个索引之间的整数
    // 假设 start < end。结果可能是负数，但应被视为无符号值。
    private int parseInt(char[] source, int start, int end) {
        int result = Character.digit(source[start++], 10);
        if (result == -1)
            throw new NumberFormatException(new String(source));

        for (int index = start; index < end; index++) {
            int nextVal = Character.digit(source[index], 10);
            if (nextVal == -1)
                throw new NumberFormatException(new String(source));
            result = 10*result + nextVal;
        }

        return result;
    }

    // 给定基数的 bitsPerDigit 乘以 1024
    // 向上取整以避免分配不足。
    private static long bitsPerDigit[] = { 0, 0,
        1024, 1624, 2048, 2378, 2648, 2875, 3072, 3247, 3402, 3543, 3672,
        3790, 3899, 4001, 4096, 4186, 4271, 4350, 4426, 4498, 4567, 4633,
        4696, 4756, 4814, 4870, 4923, 4975, 5025, 5074, 5120, 5166, 5210,
                                           5253, 5295};

    // 将 x 数组乘以 y 并加上 z
    private static void destructiveMulAdd(int[] x, int y, int z) {
        // 按字进行乘法
        long ylong = y & LONG_MASK;
        long zlong = z & LONG_MASK;
        int len = x.length;

        long product = 0;
        long carry = 0;
        for (int i = len-1; i >= 0; i--) {
            product = ylong * (x[i] & LONG_MASK) + carry;
            x[i] = (int)product;
            carry = product >>> 32;
        }

        // 进行加法
        long sum = (x[len-1] & LONG_MASK) + zlong;
        x[len-1] = (int)sum;
        carry = sum >>> 32;
        for (int i = len-2; i >= 0; i--) {
            sum = (x[i] & LONG_MASK) + carry;
            x[i] = (int)sum;
            carry = sum >>> 32;
        }
    }

    /**
     * 将 BigInteger 的十进制字符串表示形式转换为 BigInteger。字符串表示形式由可选的负号后跟一个或多个十进制数字组成。字符到数字的映射由 {@code Character.digit} 提供。字符串中不得包含任何多余的字符（例如空格）。
     *
     * @param val BigInteger 的十进制字符串表示形式。
     * @throws NumberFormatException {@code val} 不是 BigInteger 的有效表示形式。
     * @see    Character#digit
     */
    public BigInteger(String val) {
        this(val, 10);
    }

    /**
     * 构造一个随机生成的 BigInteger，均匀分布在 0 到 (2<sup>{@code numBits}</sup> - 1) 之间，包括两端。假设在 {@code rnd} 中提供了公平的随机位源。请注意，此构造函数始终构造一个非负的 BigInteger。
     *
     * @param  numBits 新 BigInteger 的最大位长。
     * @param  rnd 用于计算新 BigInteger 的随机位源。
     * @throws IllegalArgumentException {@code numBits} 为负数。
     * @see #bitLength()
     */
    public BigInteger(int numBits, Random rnd) {
        this(1, randomBits(numBits, rnd));
    }

    private static byte[] randomBits(int numBits, Random rnd) {
        if (numBits < 0)
            throw new IllegalArgumentException("numBits 必须是非负数");
        int numBytes = (int)(((long)numBits+7)/8); // 避免溢出
        byte[] randomBits = new byte[numBytes];

        // 生成随机字节并屏蔽任何多余的位
        if (numBytes > 0) {
            rnd.nextBytes(randomBits);
            int excessBits = 8*numBytes - numBits;
            randomBits[0] &= (1 << (8-excessBits)) - 1;
        }
        return randomBits;
    }

    /**
     * 构造一个随机生成的正 BigInteger，可能是质数，具有指定的位长。
     *
     * <p>建议使用 {@link #probablePrime probablePrime} 方法，除非有明确的需求指定确定性。
     *
     * @param  bitLength 返回的 BigInteger 的位长。
     * @param  certainty 调用者愿意容忍的不确定性度量。新 BigInteger 为质数的概率将超过
     *         (1 - 1/2<sup>{@code certainty}</sup>)。此参数的值与执行时间成正比。
     * @param  rnd 用于选择要测试质数的候选者的随机位源。
     * @throws ArithmeticException {@code bitLength < 2} 或 {@code bitLength} 太大。
     * @see    #bitLength()
     */
    public BigInteger(int bitLength, int certainty, Random rnd) {
        BigInteger prime;

        if (bitLength < 2)
            throw new ArithmeticException("bitLength < 2");
        prime = (bitLength < SMALL_PRIME_THRESHOLD
                                ? smallPrime(bitLength, certainty, rnd)
                                : largePrime(bitLength, certainty, rnd));
        signum = 1;
        mag = prime.mag;
    }

    // 请求的质数的最小位长
    // 在 95 位之前使用大质数生成算法。95 是通过实验选择的最佳性能阈值。
    private static final int SMALL_PRIME_THRESHOLD = 95;

    // 满足 probablePrime 规范所需的确定性
    private static final int DEFAULT_PRIME_CERTAINTY = 100;

    /**
     * 返回一个可能是质数的正 BigInteger，具有指定的位长。此方法返回的 BigInteger 是合数的概率不超过 2<sup>-100</sup>。
     *
     * @param  bitLength 返回的 BigInteger 的位长。
     * @param  rnd 用于选择要测试质数的候选者的随机位源。
     * @return 一个可能是质数的 {@code bitLength} 位的 BigInteger
     * @throws ArithmeticException {@code bitLength < 2} 或 {@code bitLength} 太大。
     * @see    #bitLength()
     * @since 1.4
     */
    public static BigInteger probablePrime(int bitLength, Random rnd) {
        if (bitLength < 2)
            throw new ArithmeticException("bitLength < 2");

        return (bitLength < SMALL_PRIME_THRESHOLD ?
                smallPrime(bitLength, DEFAULT_PRIME_CERTAINTY, rnd) :
                largePrime(bitLength, DEFAULT_PRIME_CERTAINTY, rnd));
    }

    /**
     * 找到一个可能是质数的指定位长的随机数。此方法适用于较小的质数，其性能在较大的位长下会下降。
     *
     * 此方法假设 bitLength > 1。
     */
    private static BigInteger smallPrime(int bitLength, int certainty, Random rnd) {
        int magLen = (bitLength + 31) >>> 5;
        int temp[] = new int[magLen];
        int highBit = 1 << ((bitLength+31) & 0x1f);  // 高位的高 int
        int highMask = (highBit << 1) - 1;  // 高 int 中要保留的位

        while (true) {
            // 构造一个候选者
            for (int i=0; i < magLen; i++)
                temp[i] = rnd.nextInt();
            temp[0] = (temp[0] & highMask) | highBit;  // 确保精确长度
            if (bitLength > 2)
                temp[magLen-1] |= 1;  // 如果 bitlen > 2，则使其为奇数

            BigInteger p = new BigInteger(temp, 1);

            // 如果适用，进行廉价的“预测试”
            if (bitLength > 6) {
                long r = p.remainder(SMALL_PRIME_PRODUCT).longValue();
                if ((r%3==0)  || (r%5==0)  || (r%7==0)  || (r%11==0) ||
                    (r%13==0) || (r%17==0) || (r%19==0) || (r%23==0) ||
                    (r%29==0) || (r%31==0) || (r%37==0) || (r%41==0))
                    continue; // 候选者是合数；尝试另一个
            }

            // 此时，所有位长为 2 和 3 的候选者都是质数
            if (bitLength < 4)
                return p;

            // 如果通过预测试（或不适用），进行昂贵的测试
            if (p.primeToCertainty(certainty, rnd))
                return p;
        }
    }

    private static final BigInteger SMALL_PRIME_PRODUCT
                       = valueOf(3L*5*7*11*13*17*19*23*29*31*37*41);

    /**
     * 找到一个可能是质数的指定位长的随机数。此方法更适合较大的位长，因为它使用筛选法来消除大多数合数，然后再进行更昂贵的测试。
     */
    private static BigInteger largePrime(int bitLength, int certainty, Random rnd) {
        BigInteger p;
        p = new BigInteger(bitLength, rnd).setBit(bitLength-1);
        p.mag[p.mag.length-1] &= 0xfffffffe;

        // 使用一个可能包含下一个质数的筛选长度
        int searchLen = getPrimeSearchLen(bitLength);
        BitSieve searchSieve = new BitSieve(p, searchLen);
        BigInteger candidate = searchSieve.retrieve(p, certainty, rnd);

        while ((candidate == null) || (candidate.bitLength() != bitLength)) {
            p = p.add(BigInteger.valueOf(2*searchLen));
            if (p.bitLength() != bitLength)
                p = new BigInteger(bitLength, rnd).setBit(bitLength-1);
            p.mag[p.mag.length-1] &= 0xfffffffe;
            searchSieve = new BitSieve(p, searchLen);
            candidate = searchSieve.retrieve(p, certainty, rnd);
        }
        return candidate;
    }


               /**
    * 返回大于此 {@code BigInteger} 的第一个可能是质数的整数。此方法返回的数字是合数的概率不超过 2<sup>-100</sup>。此方法在搜索时不会跳过任何质数：如果它返回 {@code p}，则不存在质数 {@code q} 使得 {@code this < q < p}。
    *
    * @return 大于此 {@code BigInteger} 的第一个可能是质数的整数。
    * @throws ArithmeticException {@code this < 0} 或 {@code this} 太大。
    * @since 1.5
    */
    public BigInteger nextProbablePrime() {
        if (this.signum < 0)
            throw new ArithmeticException("start < 0: " + this);

        // 处理简单情况
        if ((this.signum == 0) || this.equals(ONE))
            return TWO;

        BigInteger result = this.add(ONE);

        // 小数字的快速路径
        if (result.bitLength() < SMALL_PRIME_THRESHOLD) {

            // 确保是一个奇数
            if (!result.testBit(0))
                result = result.add(ONE);

            while (true) {
                // 如果适用，进行廉价的“预测试”
                if (result.bitLength() > 6) {
                    long r = result.remainder(SMALL_PRIME_PRODUCT).longValue();
                    if ((r%3==0)  || (r%5==0)  || (r%7==0)  || (r%11==0) ||
                        (r%13==0) || (r%17==0) || (r%19==0) || (r%23==0) ||
                        (r%29==0) || (r%31==0) || (r%37==0) || (r%41==0)) {
                        result = result.add(TWO);
                        continue; // 候选数是合数；尝试另一个
                    }
                }

                // 所有位长为 2 和 3 的候选数此时都是质数
                if (result.bitLength() < 4)
                    return result;

                // 昂贵的测试
                if (result.primeToCertainty(DEFAULT_PRIME_CERTAINTY, null))
                    return result;

                result = result.add(TWO);
            }
        }

        // 从上一个偶数开始
        if (result.testBit(0))
            result = result.subtract(ONE);

        // 寻找下一个大质数
        int searchLen = getPrimeSearchLen(result.bitLength());

        while (true) {
           BitSieve searchSieve = new BitSieve(result, searchLen);
           BigInteger candidate = searchSieve.retrieve(result,
                                                 DEFAULT_PRIME_CERTAINTY, null);
           if (candidate != null)
               return candidate;
           result = result.add(BigInteger.valueOf(2 * searchLen));
        }
    }

    private static int getPrimeSearchLen(int bitLength) {
        if (bitLength > PRIME_SEARCH_BIT_LENGTH_LIMIT + 1) {
            throw new ArithmeticException("Prime search implementation restriction on bitLength");
        }
        return bitLength / 20 * 64;
    }

    /**
     * 如果此 BigInteger 可能是质数，则返回 {@code true}，如果是确定的合数，则返回 {@code false}。
     *
     * 此方法假设 bitLength > 2。
     *
     * @param  certainty 调用者愿意容忍的不确定性度量：如果调用返回 {@code true}，则此 BigInteger 是质数的概率超过
     *         {@code (1 - 1/2<sup>certainty</sup>)}。此方法的执行时间与该参数的值成正比。
     * @return 如果此 BigInteger 可能是质数，则返回 {@code true}，如果是确定的合数，则返回 {@code false}。
     */
    boolean primeToCertainty(int certainty, Random random) {
        int rounds = 0;
        int n = (Math.min(certainty, Integer.MAX_VALUE-1)+1)/2;

        // 确定 certainty 和执行的轮数之间的关系
        // 该关系在草案标准 ANSI X9.80, "PRIME NUMBER GENERATION, PRIMALITY TESTING, AND PRIMALITY CERTIFICATES" 中给出。
        int sizeInBits = this.bitLength();
        if (sizeInBits < 100) {
            rounds = 50;
            rounds = n < rounds ? n : rounds;
            return passesMillerRabin(rounds, random);
        }

        if (sizeInBits < 256) {
            rounds = 27;
        } else if (sizeInBits < 512) {
            rounds = 15;
        } else if (sizeInBits < 768) {
            rounds = 8;
        } else if (sizeInBits < 1024) {
            rounds = 4;
        } else {
            rounds = 2;
        }
        rounds = n < rounds ? n : rounds;

        return passesMillerRabin(rounds, random) && passesLucasLehmer();
    }

    /**
     * 如果此 BigInteger 是 Lucas-Lehmer 可能的质数，则返回 true。
     *
     * 以下假设：
     * 此 BigInteger 是一个正的奇数。
     */
    private boolean passesLucasLehmer() {
        BigInteger thisPlusOne = this.add(ONE);

        // 第一步
        int d = 5;
        while (jacobiSymbol(d, this) != -1) {
            // 5, -7, 9, -11, ...
            d = (d < 0) ? Math.abs(d)+2 : -(d+2);
        }

        // 第二步
        BigInteger u = lucasLehmerSequence(d, thisPlusOne, this);

        // 第三步
        return u.mod(this).equals(ZERO);
    }

    /**
     * 计算 Jacobi(p,n)。
     * 假设 n 是正的、奇数，n>=3。
     */
    private static int jacobiSymbol(int p, BigInteger n) {
        if (p == 0)
            return 0;

        // 算法和注释改编自 Colin Plumb 的 C 库。
        int j = 1;
        int u = n.mag[n.mag.length-1];

        // 使 p 为正数
        if (p < 0) {
            p = -p;
            int n8 = u & 7;
            if ((n8 == 3) || (n8 == 7))
                j = -j; // 3 (011) 或 7 (111) 模 8
        }

        // 去除 p 中的 2 的因子
        while ((p & 3) == 0)
            p >>= 2;
        if ((p & 1) == 0) {
            p >>= 1;
            if (((u ^ (u>>1)) & 2) != 0)
                j = -j; // 3 (011) 或 5 (101) 模 8
        }
        if (p == 1)
            return j;
        // 然后，应用二次互反律
        if ((p & u & 2) != 0)   // p = u = 3 (模 4)?
            j = -j;
        // 并将 u 模 p
        u = n.mod(BigInteger.valueOf(p)).intValue();

        // 现在计算 Jacobi(u,p)，u < p
        while (u != 0) {
            while ((u & 3) == 0)
                u >>= 2;
            if ((u & 1) == 0) {
                u >>= 1;
                if (((p ^ (p>>1)) & 2) != 0)
                    j = -j;     // 3 (011) 或 5 (101) 模 8
            }
            if (u == 1)
                return j;
            // 现在 u 和 p 都是奇数，所以使用二次互反律
            assert (u < p);
            int t = u; u = p; p = t;
            if ((u & p & 2) != 0) // u = p = 3 (模 4)?
                j = -j;
            // 现在 u >= p，所以可以减少
            u %= p;
        }
        return 0;
    }

    private static BigInteger lucasLehmerSequence(int z, BigInteger k, BigInteger n) {
        BigInteger d = BigInteger.valueOf(z);
        BigInteger u = ONE; BigInteger u2;
        BigInteger v = ONE; BigInteger v2;

        for (int i=k.bitLength()-2; i >= 0; i--) {
            u2 = u.multiply(v).mod(n);

            v2 = v.square().add(d.multiply(u.square())).mod(n);
            if (v2.testBit(0))
                v2 = v2.subtract(n);

            v2 = v2.shiftRight(1);

            u = u2; v = v2;
            if (k.testBit(i)) {
                u2 = u.add(v).mod(n);
                if (u2.testBit(0))
                    u2 = u2.subtract(n);

                u2 = u2.shiftRight(1);
                v2 = v.add(d.multiply(u)).mod(n);
                if (v2.testBit(0))
                    v2 = v2.subtract(n);
                v2 = v2.shiftRight(1);

                u = u2; v = v2;
            }
        }
        return u;
    }

    /**
     * 如果此 BigInteger 通过指定数量的 Miller-Rabin 测试，则返回 true。此测试取自 DSA 规范 (NIST FIPS 186-2)。
     *
     * 以下假设：
     * 此 BigInteger 是一个正的奇数，大于 2。
     * iterations<=50。
     */
    private boolean passesMillerRabin(int iterations, Random rnd) {
        // 找到 a 和 m 使得 m 是奇数且 this == 1 + 2**a * m
        BigInteger thisMinusOne = this.subtract(ONE);
        BigInteger m = thisMinusOne;
        int a = m.getLowestSetBit();
        m = m.shiftRight(a);

        // 进行测试
        if (rnd == null) {
            rnd = ThreadLocalRandom.current();
        }
        for (int i=0; i < iterations; i++) {
            // 生成 (1, this) 之间的均匀随机数
            BigInteger b;
            do {
                b = new BigInteger(this.bitLength(), rnd);
            } while (b.compareTo(ONE) <= 0 || b.compareTo(this) >= 0);

            int j = 0;
            BigInteger z = b.modPow(m, this);
            while (!((j == 0 && z.equals(ONE)) || z.equals(thisMinusOne))) {
                if (j > 0 && z.equals(ONE) || ++j == a)
                    return false;
                z = z.modPow(TWO, this);
            }
        }
        return true;
    }

    /**
     * 该内部构造函数与公开的构造函数不同，参数顺序相反，它假设其参数是正确的，并且不会复制 magnitude 数组。
     */
    BigInteger(int[] magnitude, int signum) {
        this.signum = (magnitude.length == 0 ? 0 : signum);
        this.mag = magnitude;
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * 该私有构造函数用于内部，假设其参数是正确的。
     */
    private BigInteger(byte[] magnitude, int signum) {
        this.signum = (magnitude.length == 0 ? 0 : signum);
        this.mag = stripLeadingZeroBytes(magnitude);
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * 如果 BigInteger 超出支持的范围，则抛出 {@code ArithmeticException}。
     *
     * @throws ArithmeticException 如果 {@code this} 超出支持的范围。
     */
    private void checkRange() {
        if (mag.length > MAX_MAG_LENGTH || mag.length == MAX_MAG_LENGTH && mag[0] < 0) {
            reportOverflow();
        }
    }

    private static void reportOverflow() {
        throw new ArithmeticException("BigInteger would overflow supported range");
    }

    // 静态工厂方法

    /**
     * 返回一个值等于指定 {@code long} 的 BigInteger。提供此“静态工厂方法”是为了优先于 ({@code long}) 构造函数，因为它允许重用频繁使用的 BigInteger。
     *
     * @param  val 要返回的 BigInteger 的值。
     * @return 具有指定值的 BigInteger。
     */
    public static BigInteger valueOf(long val) {
        // 如果 -MAX_CONSTANT < val < MAX_CONSTANT，返回缓存的常量
        if (val == 0)
            return ZERO;
        if (val > 0 && val <= MAX_CONSTANT)
            return posConst[(int) val];
        else if (val < 0 && val >= -MAX_CONSTANT)
            return negConst[(int) -val];

        return new BigInteger(val);
    }

    /**
     * 构造一个具有指定值的 BigInteger，该值不能为零。
     */
    private BigInteger(long val) {
        if (val < 0) {
            val = -val;
            signum = -1;
        } else {
            signum = 1;
        }

        int highWord = (int)(val >>> 32);
        if (highWord == 0) {
            mag = new int[1];
            mag[0] = (int)val;
        } else {
            mag = new int[2];
            mag[0] = highWord;
            mag[1] = (int)val;
        }
    }

    /**
     * 返回具有给定二进制补码表示的 BigInteger。假设输入数组不会被修改（返回的 BigInteger 将在可行的情况下引用输入数组）。
     */
    private static BigInteger valueOf(int val[]) {
        return (val[0] > 0 ? new BigInteger(val, 1) : new BigInteger(val));
    }

    // 常量

    /**
     * 当类加载时初始化静态常量数组。
     */
    private final static int MAX_CONSTANT = 16;
    private static BigInteger posConst[] = new BigInteger[MAX_CONSTANT+1];
    private static BigInteger negConst[] = new BigInteger[MAX_CONSTANT+1];

    /**
     * 缓存每个基数的幂。这允许我们不必多次重新计算 radix^(2^n)。这显著加快了 Schoenhage 递归基数转换。
     */
    private static volatile BigInteger[][] powerCache;

    /** 基数转换的对数缓存。 */
    private static final double[] logCache;

    /** 自然对数 2。这在计算缓存索引时使用。 */
    private static final double LOG_TWO = Math.log(2.0);

    static {
        assert 0 < KARATSUBA_THRESHOLD
            && KARATSUBA_THRESHOLD < TOOM_COOK_THRESHOLD
            && TOOM_COOK_THRESHOLD < Integer.MAX_VALUE
            && 0 < KARATSUBA_SQUARE_THRESHOLD
            && KARATSUBA_SQUARE_THRESHOLD < TOOM_COOK_SQUARE_THRESHOLD
            && TOOM_COOK_SQUARE_THRESHOLD < Integer.MAX_VALUE :
            "Algorithm thresholds are inconsistent";

        for (int i = 1; i <= MAX_CONSTANT; i++) {
            int[] magnitude = new int[1];
            magnitude[0] = i;
            posConst[i] = new BigInteger(magnitude,  1);
            negConst[i] = new BigInteger(magnitude, -1);
        }

        /*
         * 用基数^(2^x) 的第一个值初始化用于基数转换的缓存。其他值将按需创建。
         */
        powerCache = new BigInteger[Character.MAX_RADIX+1][];
        logCache = new double[Character.MAX_RADIX+1];

        for (int i=Character.MIN_RADIX; i <= Character.MAX_RADIX; i++) {
            powerCache[i] = new BigInteger[] { BigInteger.valueOf(i) };
            logCache[i] = Math.log(i);
        }
    }

    /**
     * BigInteger 常量零。
     *
     * @since   1.2
     */
    public static final BigInteger ZERO = new BigInteger(new int[0], 0);

    /**
     * BigInteger 常量一。
     *
     * @since   1.2
     */
    public static final BigInteger ONE = valueOf(1);

    /**
     * BigInteger 常量二。 (不导出)
     */
    private static final BigInteger TWO = valueOf(2);

    /**
     * BigInteger 常量 -1。 (不导出)
     */
    private static final BigInteger NEGATIVE_ONE = valueOf(-1);

    /**
     * BigInteger 常量十。
     *
     * @since   1.5
     */
    public static final BigInteger TEN = valueOf(10);

    // 算术运算

    /**
     * 返回一个值为 {@code (this + val)} 的 BigInteger。
     *
     * @param  val 要加到此 BigInteger 的值。
     * @return {@code this + val}
     */
    public BigInteger add(BigInteger val) {
        if (val.signum == 0)
            return this;
        if (signum == 0)
            return val;
        if (val.signum == signum)
            return new BigInteger(add(mag, val.mag), signum);

        int cmp = compareMagnitude(val);
        if (cmp == 0)
            return ZERO;
        int[] resultMag = (cmp > 0 ? subtract(mag, val.mag)
                           : subtract(val.mag, mag));
        resultMag = trustedStripLeadingZeroInts(resultMag);


                    return new BigInteger(resultMag, cmp == signum ? 1 : -1);
    }

    /**
     * 包级私有方法，用于 BigDecimal 代码将 BigInteger 与 long 相加。假设 val 不等于 INFLATED。
     */
    BigInteger add(long val) {
        if (val == 0)
            return this;
        if (signum == 0)
            return valueOf(val);
        if (Long.signum(val) == signum)
            return new BigInteger(add(mag, Math.abs(val)), signum);
        int cmp = compareMagnitude(val);
        if (cmp == 0)
            return ZERO;
        int[] resultMag = (cmp > 0 ? subtract(mag, Math.abs(val)) : subtract(Math.abs(val), mag));
        resultMag = trustedStripLeadingZeroInts(resultMag);
        return new BigInteger(resultMag, cmp == signum ? 1 : -1);
    }

    /**
     * 将 int 数组 x 和 long 值 val 的内容相加。此方法分配一个新 int 数组来保存结果并返回该数组的引用。假设 x.length > 0 且 val 非负。
     */
    private static int[] add(int[] x, long val) {
        int[] y;
        long sum = 0;
        int xIndex = x.length;
        int[] result;
        int highWord = (int)(val >>> 32);
        if (highWord == 0) {
            result = new int[xIndex];
            sum = (x[--xIndex] & LONG_MASK) + val;
            result[xIndex] = (int)sum;
        } else {
            if (xIndex == 1) {
                result = new int[2];
                sum = val  + (x[0] & LONG_MASK);
                result[1] = (int)sum;
                result[0] = (int)(sum >>> 32);
                return result;
            } else {
                result = new int[xIndex];
                sum = (x[--xIndex] & LONG_MASK) + (val & LONG_MASK);
                result[xIndex] = (int)sum;
                sum = (x[--xIndex] & LONG_MASK) + (highWord & LONG_MASK) + (sum >>> 32);
                result[xIndex] = (int)sum;
            }
        }
        // 在需要进位传播时复制较长数字的其余部分
        boolean carry = (sum >>> 32 != 0);
        while (xIndex > 0 && carry)
            carry = ((result[--xIndex] = x[xIndex] + 1) == 0);
        // 复制较长数字的其余部分
        while (xIndex > 0)
            result[--xIndex] = x[xIndex];
        // 必要时扩展结果
        if (carry) {
            int bigger[] = new int[result.length + 1];
            System.arraycopy(result, 0, bigger, 1, result.length);
            bigger[0] = 0x01;
            return bigger;
        }
        return result;
    }

    /**
     * 将 int 数组 x 和 y 的内容相加。此方法分配一个新 int 数组来保存结果并返回该数组的引用。
     */
    private static int[] add(int[] x, int[] y) {
        // 如果 x 较短，则交换两个数组
        if (x.length < y.length) {
            int[] tmp = x;
            x = y;
            y = tmp;
        }

        int xIndex = x.length;
        int yIndex = y.length;
        int result[] = new int[xIndex];
        long sum = 0;
        if (yIndex == 1) {
            sum = (x[--xIndex] & LONG_MASK) + (y[0] & LONG_MASK) ;
            result[xIndex] = (int)sum;
        } else {
            // 加上两个数字的公共部分
            while (yIndex > 0) {
                sum = (x[--xIndex] & LONG_MASK) +
                      (y[--yIndex] & LONG_MASK) + (sum >>> 32);
                result[xIndex] = (int)sum;
            }
        }
        // 在需要进位传播时复制较长数字的其余部分
        boolean carry = (sum >>> 32 != 0);
        while (xIndex > 0 && carry)
            carry = ((result[--xIndex] = x[xIndex] + 1) == 0);

        // 复制较长数字的其余部分
        while (xIndex > 0)
            result[--xIndex] = x[xIndex];

        // 必要时扩展结果
        if (carry) {
            int bigger[] = new int[result.length + 1];
            System.arraycopy(result, 0, bigger, 1, result.length);
            bigger[0] = 0x01;
            return bigger;
        }
        return result;
    }

    private static int[] subtract(long val, int[] little) {
        int highWord = (int)(val >>> 32);
        if (highWord == 0) {
            int result[] = new int[1];
            result[0] = (int)(val - (little[0] & LONG_MASK));
            return result;
        } else {
            int result[] = new int[2];
            if (little.length == 1) {
                long difference = ((int)val & LONG_MASK) - (little[0] & LONG_MASK);
                result[1] = (int)difference;
                // 在需要借位传播时减去较长数字的其余部分
                boolean borrow = (difference >> 32 != 0);
                if (borrow) {
                    result[0] = highWord - 1;
                } else {        // 复制较长数字的其余部分
                    result[0] = highWord;
                }
                return result;
            } else { // little.length == 2
                long difference = ((int)val & LONG_MASK) - (little[1] & LONG_MASK);
                result[1] = (int)difference;
                difference = (highWord & LONG_MASK) - (little[0] & LONG_MASK) + (difference >> 32);
                result[0] = (int)difference;
                return result;
            }
        }
    }

    /**
     * 从第一个参数（big）中减去第二个参数（val）。第一个 int 数组（big）必须表示一个比第二个更大的数字。此方法分配必要的空间来保存结果。
     * 假设 val >= 0
     */
    private static int[] subtract(int[] big, long val) {
        int highWord = (int)(val >>> 32);
        int bigIndex = big.length;
        int result[] = new int[bigIndex];
        long difference = 0;

        if (highWord == 0) {
            difference = (big[--bigIndex] & LONG_MASK) - val;
            result[bigIndex] = (int)difference;
        } else {
            difference = (big[--bigIndex] & LONG_MASK) - (val & LONG_MASK);
            result[bigIndex] = (int)difference;
            difference = (big[--bigIndex] & LONG_MASK) - (highWord & LONG_MASK) + (difference >> 32);
            result[bigIndex] = (int)difference;
        }

        // 在需要借位传播时减去较长数字的其余部分
        boolean borrow = (difference >> 32 != 0);
        while (bigIndex > 0 && borrow)
            borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1);

        // 复制较长数字的其余部分
        while (bigIndex > 0)
            result[--bigIndex] = big[bigIndex];

        return result;
    }

    /**
     * 返回一个值为 {@code (this - val)} 的 BigInteger。
     *
     * @param  val 要从这个 BigInteger 中减去的值。
     * @return {@code this - val}
     */
    public BigInteger subtract(BigInteger val) {
        if (val.signum == 0)
            return this;
        if (signum == 0)
            return val.negate();
        if (val.signum != signum)
            return new BigInteger(add(mag, val.mag), signum);

        int cmp = compareMagnitude(val);
        if (cmp == 0)
            return ZERO;
        int[] resultMag = (cmp > 0 ? subtract(mag, val.mag)
                           : subtract(val.mag, mag));
        resultMag = trustedStripLeadingZeroInts(resultMag);
        return new BigInteger(resultMag, cmp == signum ? 1 : -1);
    }

    /**
     * 从第一个 int 数组（big）中减去第二个 int 数组（little）。第一个 int 数组（big）必须表示一个比第二个更大的数字。此方法分配必要的空间来保存结果。
     */
    private static int[] subtract(int[] big, int[] little) {
        int bigIndex = big.length;
        int result[] = new int[bigIndex];
        int littleIndex = little.length;
        long difference = 0;

        // 减去两个数字的公共部分
        while (littleIndex > 0) {
            difference = (big[--bigIndex] & LONG_MASK) -
                         (little[--littleIndex] & LONG_MASK) +
                         (difference >> 32);
            result[bigIndex] = (int)difference;
        }

        // 在需要借位传播时减去较长数字的其余部分
        boolean borrow = (difference >> 32 != 0);
        while (bigIndex > 0 && borrow)
            borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1);

        // 复制较长数字的其余部分
        while (bigIndex > 0)
            result[--bigIndex] = big[bigIndex];

        return result;
    }

    /**
     * 返回一个值为 {@code (this * val)} 的 BigInteger。
     *
     * @implNote 实现可能在 {@code val == this} 时提供更好的算法性能。
     *
     * @param  val 要与这个 BigInteger 相乘的值。
     * @return {@code this * val}
     */
    public BigInteger multiply(BigInteger val) {
        return multiply(val, false);
    }

    /**
     * 返回一个值为 {@code (this * val)} 的 BigInteger。如果调用是递归的，则跳过某些溢出检查。
     *
     * @param  val 要与这个 BigInteger 相乘的值。
     * @param  isRecursion 是否是递归调用
     * @return {@code this * val}
     */
    private BigInteger multiply(BigInteger val, boolean isRecursion) {
        if (val.signum == 0 || signum == 0)
            return ZERO;

        int xlen = mag.length;

        if (val == this && xlen > MULTIPLY_SQUARE_THRESHOLD) {
            return square();
        }

        int ylen = val.mag.length;

        if ((xlen < KARATSUBA_THRESHOLD) || (ylen < KARATSUBA_THRESHOLD)) {
            int resultSign = signum == val.signum ? 1 : -1;
            if (val.mag.length == 1) {
                return multiplyByInt(mag,val.mag[0], resultSign);
            }
            if (mag.length == 1) {
                return multiplyByInt(val.mag,mag[0], resultSign);
            }
            int[] result = multiplyToLen(mag, xlen,
                                         val.mag, ylen, null);
            result = trustedStripLeadingZeroInts(result);
            return new BigInteger(result, resultSign);
        } else {
            if ((xlen < TOOM_COOK_THRESHOLD) && (ylen < TOOM_COOK_THRESHOLD)) {
                return multiplyKaratsuba(this, val);
            } else {
                //
                // 在 "Hacker's Delight" 第 2-13 节，第 33 页，解释了如果 x 和 y 是无符号 32 位数量，m 和 n 分别是它们在 32 位内的前导零数量，
                // 那么它们的乘积作为 64 位无符号数量的前导零数量是 m + n 或 m + n + 1。如果它们的乘积不超过 32 位，
                // 则乘积的前导零数量必须至少为 32，即最高位设置在零相对位置 31 或更少。
                //
                // 从上述内容有三种情况：
                //
                //     m + n    最高位设置    条件
                //     -----    ----------    ---------
                //     >= 32    x <= 64 - 32 = 32   无溢出
                //     == 31    x >= 64 - 32 = 32   可能溢出
                //     <= 30    x >= 64 - 31 = 33   肯定溢出
                //
                // “可能溢出”条件不能仅通过检查数据长度来检测，需要进一步计算。
                //
                // 通过类比，如果 'this' 和 'val' 有 m 和 n 作为它们在 32*MAX_MAG_LENGTH 位内的前导零数量，那么：
                //
                //     m + n >= 32*MAX_MAG_LENGTH        无溢出
                //     m + n == 32*MAX_MAG_LENGTH - 1    可能溢出
                //     m + n <= 32*MAX_MAG_LENGTH - 2    肯定溢出
                //
                // 然而，如果结果中的 int 数量为 MAX_MAG_LENGTH 且 mag[0] < 0，则会有溢出。因此最左边的位（mag[0] 的）不能使用，
                // 约束条件必须调整一位：
                //
                //     m + n >  32*MAX_MAG_LENGTH        无溢出
                //     m + n == 32*MAX_MAG_LENGTH        可能溢出
                //     m + n <  32*MAX_MAG_LENGTH        肯定溢出
                //
                // 前面的基于前导零的讨论仅为了清晰。实际计算使用乘积的估计位长度，因为这是内部表示的幅度更自然的方式，没有前导零元素。
                //
                if (!isRecursion) {
                    // 这里不使用 bitLength() 实例方法，因为我们只考虑非负的幅度。Toom-Cook 乘法算法在最后从两个 signum 值确定符号。
                    if (bitLength(mag, mag.length) +
                        bitLength(val.mag, val.mag.length) >
                        32L*MAX_MAG_LENGTH) {
                        reportOverflow();
                    }
                }

                return multiplyToomCook3(this, val);
            }
        }
    }

    private static BigInteger multiplyByInt(int[] x, int y, int sign) {
        if (Integer.bitCount(y) == 1) {
            return new BigInteger(shiftLeft(x,Integer.numberOfTrailingZeros(y)), sign);
        }
        int xlen = x.length;
        int[] rmag =  new int[xlen + 1];
        long carry = 0;
        long yl = y & LONG_MASK;
        int rstart = rmag.length - 1;
        for (int i = xlen - 1; i >= 0; i--) {
            long product = (x[i] & LONG_MASK) * yl + carry;
            rmag[rstart--] = (int)product;
            carry = product >>> 32;
        }
        if (carry == 0L) {
            rmag = java.util.Arrays.copyOfRange(rmag, 1, rmag.length);
        } else {
            rmag[rstart] = (int)carry;
        }
        return new BigInteger(rmag, sign);
    }

    /**
     * 包级私有方法，用于 BigDecimal 代码将 BigInteger 与 long 相乘。假设 v 不等于 INFLATED。
     */
    BigInteger multiply(long v) {
        if (v == 0 || signum == 0)
          return ZERO;
        if (v == BigDecimal.INFLATED)
            return multiply(BigInteger.valueOf(v));
        int rsign = (v > 0 ? signum : -signum);
        if (v < 0)
            v = -v;
        long dh = v >>> 32;      // 高位
        long dl = v & LONG_MASK; // 低位

        int xlen = mag.length;
        int[] value = mag;
        int[] rmag = (dh == 0L) ? (new int[xlen + 1]) : (new int[xlen + 2]);
        long carry = 0;
        int rstart = rmag.length - 1;
        for (int i = xlen - 1; i >= 0; i--) {
            long product = (value[i] & LONG_MASK) * dl + carry;
            rmag[rstart--] = (int)product;
            carry = product >>> 32;
        }
        rmag[rstart] = (int)carry;
        if (dh != 0L) {
            carry = 0;
            rstart = rmag.length - 2;
            for (int i = xlen - 1; i >= 0; i--) {
                long product = (value[i] & LONG_MASK) * dh +
                    (rmag[rstart] & LONG_MASK) + carry;
                rmag[rstart--] = (int)product;
                carry = product >>> 32;
            }
            rmag[0] = (int)carry;
        }
        if (carry == 0L)
            rmag = java.util.Arrays.copyOfRange(rmag, 1, rmag.length);
        return new BigInteger(rmag, rsign);
    }


                /**
     * 将 int 数组 x 和 y 乘到指定长度，并将结果放入 z 中。结果数组中不会有前导零。
     */
    private static int[] multiplyToLen(int[] x, int xlen, int[] y, int ylen, int[] z) {
        int xstart = xlen - 1;
        int ystart = ylen - 1;

        if (z == null || z.length < (xlen + ylen))
             z = new int[xlen + ylen];

        long carry = 0;
        for (int j = ystart, k = ystart + 1 + xstart; j >= 0; j--, k--) {
            long product = (y[j] & LONG_MASK) *
                           (x[xstart] & LONG_MASK) + carry;
            z[k] = (int) product;
            carry = product >>> 32;
        }
        z[xstart] = (int) carry;

        for (int i = xstart - 1; i >= 0; i--) {
            carry = 0;
            for (int j = ystart, k = ystart + 1 + i; j >= 0; j--, k--) {
                long product = (y[j] & LONG_MASK) *
                               (x[i] & LONG_MASK) +
                               (z[k] & LONG_MASK) + carry;
                z[k] = (int) product;
                carry = product >>> 32;
            }
            z[i] = (int) carry;
        }
        return z;
    }

    /**
     * 使用 Karatsuba 乘法算法乘以两个 BigInteger。这是一种递归的分治算法，对于大数比常用的“小学”算法更有效。如果要相乘的数的长度为 n，“小学”算法的时间复杂度为 O(n^2)。相比之下，Karatsuba 算法的时间复杂度为 O(n^(log2(3)))，即 O(n^1.585)。它通过在计算乘积时只做 3 次乘法而不是 4 次来实现更高的性能。由于它有一定的开销，因此当两个数都大于某个阈值（通过实验确定）时才应使用。
     *
     * 参见：http://en.wikipedia.org/wiki/Karatsuba_algorithm
     */
    private static BigInteger multiplyKaratsuba(BigInteger x, BigInteger y) {
        int xlen = x.mag.length;
        int ylen = y.mag.length;

        // 每个数的一半的 int 数量。
        int half = (Math.max(xlen, ylen) + 1) / 2;

        // xl 和 yl 分别是 x 和 y 的较低部分，xh 和 yh 是较高部分。
        BigInteger xl = x.getLower(half);
        BigInteger xh = x.getUpper(half);
        BigInteger yl = y.getLower(half);
        BigInteger yh = y.getUpper(half);

        BigInteger p1 = xh.multiply(yh);  // p1 = xh * yh
        BigInteger p2 = xl.multiply(yl);  // p2 = xl * yl

        // p3 = (xh + xl) * (yh + yl)
        BigInteger p3 = xh.add(xl).multiply(yh.add(yl));

        // result = p1 * 2^(32 * 2 * half) + (p3 - p1 - p2) * 2^(32 * half) + p2
        BigInteger result = p1.shiftLeft(32 * half).add(p3.subtract(p1).subtract(p2)).shiftLeft(32 * half).add(p2);

        if (x.signum != y.signum) {
            return result.negate();
        } else {
            return result;
        }
    }

    /**
     * 使用 3 路 Toom-Cook 乘法算法乘以两个 BigInteger。这是一种递归的分治算法，对于大数比常用的“小学”算法更有效。如果要相乘的数的长度为 n，“小学”算法的时间复杂度为 O(n^2)。相比之下，3 路 Toom-Cook 的时间复杂度约为 O(n^1.465)。它通过将每个数分成三部分并在计算乘积时只做 5 次乘法而不是 9 次来实现更高的渐近性能。由于 Toom-Cook 算法中存在开销（加法、移位和一次除法），因此只有当两个数都大于某个阈值（通过实验确定）时才应使用。这个阈值通常大于 Karatsuba 乘法的阈值，因此这个算法通常只在数变得相当大时使用。
     *
     * 使用的算法是由 Marco Bodrato 提出的“最优”3 路 Toom-Cook 算法。
     *
     * 参见：http://bodrato.it/toom-cook/
     *       http://bodrato.it/papers/#WAIFI2007
     *
     * "Towards Optimal Toom-Cook Multiplication for Univariate and
     * Multivariate Polynomials in Characteristic 2 and 0." by Marco BODRATO;
     * In C.Carlet and B.Sunar, Eds., "WAIFI'07 proceedings", p. 116-133,
     * LNCS #4547. Springer, Madrid, Spain, June 21-22, 2007.
     *
     */
    private static BigInteger multiplyToomCook3(BigInteger a, BigInteger b) {
        int alen = a.mag.length;
        int blen = b.mag.length;

        int largest = Math.max(alen, blen);

        // k 是较低部分的大小（以 int 为单位）。
        int k = (largest + 2) / 3;   // 等于 ceil(largest / 3)

        // r 是最高部分的大小（以 int 为单位）。
        int r = largest - 2 * k;

        // 获取数的切片。a2 和 b2 是数 a 和 b 的最高有效位，a0 和 b0 是最低有效位。
        BigInteger a0, a1, a2, b0, b1, b2;
        a2 = a.getToomSlice(k, r, 0, largest);
        a1 = a.getToomSlice(k, r, 1, largest);
        a0 = a.getToomSlice(k, r, 2, largest);
        b2 = b.getToomSlice(k, r, 0, larger);
        b1 = b.getToomSlice(k, r, 1, largest);
        b0 = b.getToomSlice(k, r, 2, largest);

        BigInteger v0, v1, v2, vm1, vinf, t1, t2, tm1, da1, db1;

        v0 = a0.multiply(b0, true);
        da1 = a2.add(a0);
        db1 = b2.add(b0);
        vm1 = da1.subtract(a1).multiply(db1.subtract(b1), true);
        da1 = da1.add(a1);
        db1 = db1.add(b1);
        v1 = da1.multiply(db1, true);
        v2 = da1.add(a2).shiftLeft(1).subtract(a0).multiply(
             db1.add(b2).shiftLeft(1).subtract(b0), true);
        vinf = a2.multiply(b2, true);

        // 该算法需要两次除以 2 和一次除以 3。
        // 所有除法都是已知的精确除法，即不会产生余数，所有结果都是正数。除以 2 的操作通过右移实现，效率较高，只剩下一次精确的除以 3 的操作，这是通过一个专门的线性时间算法完成的。
        t2 = v2.subtract(vm1).exactDivideBy3();
        tm1 = v1.subtract(vm1).shiftRight(1);
        t1 = v1.subtract(v0);
        t2 = t2.subtract(t1).shiftRight(1);
        t1 = t1.subtract(tm1).subtract(vinf);
        t2 = t2.subtract(vinf.shiftLeft(1));
        tm1 = tm1.subtract(t2);

        // 需要左移的位数。
        int ss = k * 32;

        BigInteger result = vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0);

        if (a.signum != b.signum) {
            return result.negate();
        } else {
            return result;
        }
    }


    /**
     * 返回用于 Toom-Cook 乘法的 BigInteger 的切片。
     *
     * @param lowerSize 较低部分的大小。
     * @param upperSize 较高部分的大小。
     * @param slice 请求的切片索引，必须是从 0 到 size-1 的数字。切片 0 是最高有效位，切片 size-1 是最低有效位。切片 0 的大小可能与其他切片不同。
     * @param fullsize 较大整数数组的大小，用于在乘以不同大小的数时对齐切片到适当的位置。
     */
    private BigInteger getToomSlice(int lowerSize, int upperSize, int slice,
                                    int fullsize) {
        int start, end, sliceSize, len, offset;

        len = mag.length;
        offset = fullsize - len;

        if (slice == 0) {
            start = 0 - offset;
            end = upperSize - 1 - offset;
        } else {
            start = upperSize + (slice - 1) * lowerSize - offset;
            end = start + lowerSize - 1;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
           return ZERO;
        }

        sliceSize = (end - start) + 1;

        if (sliceSize <= 0) {
            return ZERO;
        }

        // 在执行 Toom-Cook 时，所有切片都是正数，最终数的符号在最终组合时调整。
        if (start == 0 && sliceSize >= len) {
            return this.abs();
        }

        int intSlice[] = new int[sliceSize];
        System.arraycopy(mag, start, intSlice, 0, sliceSize);

        return new BigInteger(trustedStripLeadingZeroInts(intSlice), 1);
    }

    /**
     * 对指定数进行精确除法（即余数已知为零）除以 3。这是在 Toom-Cook 乘法中使用的。这是一个高效的算法，运行时间为线性时间。如果参数不能被 3 整除，结果是未定义的。注意，这期望只对正参数调用。
     */
    private BigInteger exactDivideBy3() {
        int len = mag.length;
        int[] result = new int[len];
        long x, w, q, borrow;
        borrow = 0L;
        for (int i = len - 1; i >= 0; i--) {
            x = (mag[i] & LONG_MASK);
            w = x - borrow;
            if (borrow > x) {      // 数字是否变为负数？
                borrow = 1L;
            } else {
                borrow = 0L;
            }

            // 0xAAAAAAAB 是 3 (mod 2^32) 的模逆。因此，这实际上是除以 3 (mod 2^32)。
            // 这比大多数架构上的除法快得多。
            q = (w * 0xAAAAAAABL) & LONG_MASK;
            result[i] = (int) q;

            // 检查借位。当然，如果第一个检查失败，第二个检查可以省略。
            if (q >= 0x55555556L) {
                borrow++;
                if (q >= 0xAAAAAAABL)
                    borrow++;
            }
        }
        result = trustedStripLeadingZeroInts(result);
        return new BigInteger(result, signum);
    }

    /**
     * 返回表示数的 n 个较低 int 的新 BigInteger。这是在 Karatsuba 乘法和 Karatsuba 平方中使用的。
     */
    private BigInteger getLower(int n) {
        int len = mag.length;

        if (len <= n) {
            return abs();
        }

        int lowerInts[] = new int[n];
        System.arraycopy(mag, len - n, lowerInts, 0, n);

        return new BigInteger(trustedStripLeadingZeroInts(lowerInts), 1);
    }

    /**
     * 返回表示数的 mag.length-n 个较高 int 的新 BigInteger。这是在 Karatsuba 乘法和 Karatsuba 平方中使用的。
     */
    private BigInteger getUpper(int n) {
        int len = mag.length;

        if (len <= n) {
            return ZERO;
        }

        int upperLen = len - n;
        int upperInts[] = new int[upperLen];
        System.arraycopy(mag, 0, upperInts, 0, upperLen);

        return new BigInteger(trustedStripLeadingZeroInts(upperInts), 1);
    }

    // 平方

    /**
     * 返回一个值为 {@code (this<sup>2</sup>)} 的 BigInteger。
     *
     * @return {@code this<sup>2</sup>}
     */
    private BigInteger square() {
        return square(false);
    }

    /**
     * 返回一个值为 {@code (this<sup>2</sup>)} 的 BigInteger。如果调用是递归的，则跳过某些溢出检查。
     *
     * @param isRecursion 是否是递归调用
     * @return {@code this<sup>2</sup>}
     */
    private BigInteger square(boolean isRecursion) {
        if (signum == 0) {
            return ZERO;
        }
        int len = mag.length;

        if (len < KARATSUBA_SQUARE_THRESHOLD) {
            int[] z = squareToLen(mag, len, null);
            return new BigInteger(trustedStripLeadingZeroInts(z), 1);
        } else {
            if (len < TOOM_COOK_SQUARE_THRESHOLD) {
                return squareKaratsuba();
            } else {
                //
                // 关于溢出检测的讨论见 multiply()
                //
                if (!isRecursion) {
                    if (bitLength(mag, mag.length) > 16L * MAX_MAG_LENGTH) {
                        reportOverflow();
                    }
                }

                return squareToomCook3();
            }
        }
    }

    /**
     * 对 int 数组 x 的内容进行平方。结果放入 int 数组 z 中。x 的内容不变。
     */
    private static final int[] squareToLen(int[] x, int len, int[] z) {
         int zlen = len << 1;
         if (z == null || z.length < zlen)
             z = new int[zlen];

         // 在调用内联方法之前执行检查。
         implSquareToLenChecks(x, len, z, zlen);
         return implSquareToLen(x, len, z, zlen);
     }

     /**
      * 参数验证。
      */
     private static void implSquareToLenChecks(int[] x, int len, int[] z, int zlen) throws RuntimeException {
         if (len < 1) {
             throw new IllegalArgumentException("无效的输入长度: " + len);
         }
         if (len > x.length) {
             throw new IllegalArgumentException("输入长度超出范围: " +
                                        len + " > " + x.length);
         }
         if (len * 2 > z.length) {
             throw new IllegalArgumentException("输入长度超出范围: " +
                                        (len * 2) + " > " + z.length);
         }
         if (zlen < 1) {
             throw new IllegalArgumentException("无效的输入长度: " + zlen);
         }
         if (zlen > z.length) {
             throw new IllegalArgumentException("输入长度超出范围: " +
                                        len + " > " + z.length);
         }
     }

     /**
      * Java 运行时可能会使用内联方法。
      */
     private static final int[] implSquareToLen(int[] x, int len, int[] z, int zlen) {
        /*
         * 这里使用的算法改编自 Colin Plumb 的 C 库。
         * 技巧：考虑“abcde”与自身的部分乘积：
         *
         *               a  b  c  d  e
         *            *  a  b  c  d  e
         *          ==================
         *              ae be ce de ee
         *           ad bd cd dd de
         *        ac bc cc cd ce
         *     ab bb bc bd be
         *  aa ab ac ad ae
         *
         * 注意，主对角线以上的内容：
         *              ae be ce de = (abcd) * e
         *           ad bd cd       = (abc) * d
         *        ac bc             = (ab) * c
         *     ab                   = (a) * b
         *
         * 是主对角线以下内容的副本：
         *                       de
         *                 cd ce
         *           bc bd be
         *     ab ac ad ae
         *
         * 因此，总和是 2 * (非对角线) + 对角线。
         *
         * 从对角线开始累积（对角线由输入的各位的平方组成），然后除以二，加上非对角线，再乘以二。最低位只是输入的最低位的副本，因此不需要特别处理。
         */


                    // 存储平方值，右移一位（即除以2）
        int lastProductLowWord = 0;
        for (int j=0, i=0; j < len; j++) {
            long piece = (x[j] & LONG_MASK);
            long product = piece * piece;
            z[i++] = (lastProductLowWord << 31) | (int)(product >>> 33);
            z[i++] = (int)(product >>> 1);
            lastProductLowWord = (int)product;
        }

        // 添加非对角线和
        for (int i=len, offset=1; i > 0; i--, offset+=2) {
            int t = x[i-1];
            t = mulAdd(z, x, offset, i-1, t);
            addOne(z, offset-1, i, t);
        }

        // 向上移位并设置最低位
        primitiveLeftShift(z, zlen, 1);
        z[zlen-1] |= x[len-1] & 1;

        return z;
    }

    /**
     * 使用Karatsuba平方算法计算BigInteger的平方。当两个数都大于某个阈值（通过实验确定）时，应使用此方法。这是一种递归的分治算法，其渐近性能优于squareToLen中使用的算法。
     */
    private BigInteger squareKaratsuba() {
        int half = (mag.length+1) / 2;

        BigInteger xl = getLower(half);
        BigInteger xh = getUpper(half);

        BigInteger xhs = xh.square();  // xhs = xh^2
        BigInteger xls = xl.square();  // xls = xl^2

        // xh^2 << 64  +  (((xl+xh)^2 - (xh^2 + xl^2)) << 32) + xl^2
        return xhs.shiftLeft(half*32).add(xl.add(xh).square().subtract(xhs.add(xls))).shiftLeft(half*32).add(xls);
    }

    /**
     * 使用3路Toom-Cook平方算法计算BigInteger的平方。当两个数都大于某个阈值（通过实验确定）时，应使用此方法。这是一种递归的分治算法，其渐近性能优于squareToLen或squareKaratsuba中使用的算法。
     */
    private BigInteger squareToomCook3() {
        int len = mag.length;

        // k是低位切片的大小（以int为单位）。
        int k = (len+2)/3;   // 等于ceil(largest/3)

        // r是最高位切片的大小（以int为单位）。
        int r = len - 2*k;

        // 获取数字的切片。a2是最显著的位，a0是最不显著的位。
        BigInteger a0, a1, a2;
        a2 = getToomSlice(k, r, 0, len);
        a1 = getToomSlice(k, r, 1, len);
        a0 = getToomSlice(k, r, 2, len);
        BigInteger v0, v1, v2, vm1, vinf, t1, t2, tm1, da1;

        v0 = a0.square(true);
        da1 = a2.add(a0);
        vm1 = da1.subtract(a1).square(true);
        da1 = da1.add(a1);
        v1 = da1.square(true);
        vinf = a2.square(true);
        v2 = da1.add(a2).shiftLeft(1).subtract(a0).square(true);

        // 该算法需要两次除以2和一次除以3。
        // 所有除法都是精确的，即不会产生余数，所有结果都是正数。除以2的除法通过右移实现，效率较高，只剩下一次除以3的除法。
        // 除以3的除法通过优化的算法实现。
        t2 = v2.subtract(vm1).exactDivideBy3();
        tm1 = v1.subtract(vm1).shiftRight(1);
        t1 = v1.subtract(v0);
        t2 = t2.subtract(t1).shiftRight(1);
        t1 = t1.subtract(tm1).subtract(vinf);
        t2 = t2.subtract(vinf.shiftLeft(1));
        tm1 = tm1.subtract(t2);

        // 左移的位数。
        int ss = k*32;

        return vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0);
    }

    // 除法

    /**
     * 返回一个BigInteger，其值为 {@code (this / val)}。
     *
     * @param  val 用于除以这个BigInteger的值。
     * @return {@code this / val}
     * @throws ArithmeticException 如果 {@code val} 为零。
     */
    public BigInteger divide(BigInteger val) {
        if (val.mag.length < BURNIKEL_ZIEGLER_THRESHOLD ||
                mag.length - val.mag.length < BURNIKEL_ZIEGLER_OFFSET) {
            return divideKnuth(val);
        } else {
            return divideBurnikelZiegler(val);
        }
    }

    /**
     * 使用Knuth的O(n^2)算法返回一个BigInteger，其值为 {@code (this / val)}。
     *
     * @param  val 用于除以这个BigInteger的值。
     * @return {@code this / val}
     * @throws ArithmeticException 如果 {@code val} 为零。
     * @see MutableBigInteger#divideKnuth(MutableBigInteger, MutableBigInteger, boolean)
     */
    private BigInteger divideKnuth(BigInteger val) {
        MutableBigInteger q = new MutableBigInteger(),
                          a = new MutableBigInteger(this.mag),
                          b = new MutableBigInteger(val.mag);

        a.divideKnuth(b, q, false);
        return q.toBigInteger(this.signum * val.signum);
    }

    /**
     * 返回一个包含两个BigInteger的数组，第一个是 {@code (this / val)}，第二个是 {@code (this % val)}。
     *
     * @param  val 用于除以这个BigInteger的值，并计算余数。
     * @return 一个包含两个BigInteger的数组：商 {@code (this / val)} 是第一个元素，余数 {@code (this % val)} 是最后一个元素。
     * @throws ArithmeticException 如果 {@code val} 为零。
     */
    public BigInteger[] divideAndRemainder(BigInteger val) {
        if (val.mag.length < BURNIKEL_ZIEGLER_THRESHOLD ||
                mag.length - val.mag.length < BURNIKEL_ZIEGLER_OFFSET) {
            return divideAndRemainderKnuth(val);
        } else {
            return divideAndRemainderBurnikelZiegler(val);
        }
    }

    /** 长除法 */
    private BigInteger[] divideAndRemainderKnuth(BigInteger val) {
        BigInteger[] result = new BigInteger[2];
        MutableBigInteger q = new MutableBigInteger(),
                          a = new MutableBigInteger(this.mag),
                          b = new MutableBigInteger(val.mag);
        MutableBigInteger r = a.divideKnuth(b, q);
        result[0] = q.toBigInteger(this.signum == val.signum ? 1 : -1);
        result[1] = r.toBigInteger(this.signum);
        return result;
    }

    /**
     * 返回一个BigInteger，其值为 {@code (this % val)}。
     *
     * @param  val 用于除以这个BigInteger的值，并计算余数。
     * @return {@code this % val}
     * @throws ArithmeticException 如果 {@code val} 为零。
     */
    public BigInteger remainder(BigInteger val) {
        if (val.mag.length < BURNIKEL_ZIEGLER_THRESHOLD ||
                mag.length - val.mag.length < BURNIKEL_ZIEGLER_OFFSET) {
            return remainderKnuth(val);
        } else {
            return remainderBurnikelZiegler(val);
        }
    }

    /** 长除法 */
    private BigInteger remainderKnuth(BigInteger val) {
        MutableBigInteger q = new MutableBigInteger(),
                          a = new MutableBigInteger(this.mag),
                          b = new MutableBigInteger(val.mag);

        return a.divideKnuth(b, q).toBigInteger(this.signum);
    }

    /**
     * 使用Burnikel-Ziegler算法计算 {@code this / val}。
     * @param  val 除数
     * @return {@code this / val}
     */
    private BigInteger divideBurnikelZiegler(BigInteger val) {
        return divideAndRemainderBurnikelZiegler(val)[0];
    }

    /**
     * 使用Burnikel-Ziegler算法计算 {@code this % val}。
     * @param val 除数
     * @return {@code this % val}
     */
    private BigInteger remainderBurnikelZiegler(BigInteger val) {
        return divideAndRemainderBurnikelZiegler(val)[1];
    }

    /**
     * 使用Burnikel-Ziegler算法计算 {@code this / val} 和 {@code this % val}。
     * @param val 除数
     * @return 一个包含商和余数的数组
     */
    private BigInteger[] divideAndRemainderBurnikelZiegler(BigInteger val) {
        MutableBigInteger q = new MutableBigInteger();
        MutableBigInteger r = new MutableBigInteger(this).divideAndRemainderBurnikelZiegler(new MutableBigInteger(val), q);
        BigInteger qBigInt = q.isZero() ? ZERO : q.toBigInteger(signum*val.signum);
        BigInteger rBigInt = r.isZero() ? ZERO : r.toBigInteger(signum);
        return new BigInteger[] {qBigInt, rBigInt};
    }

    /**
     * 返回一个BigInteger，其值为 <tt>(this<sup>exponent</sup>)</tt>。
     * 注意，{@code exponent} 是一个整数而不是BigInteger。
     *
     * @param  exponent 这个BigInteger要被提升的指数。
     * @return <tt>this<sup>exponent</sup></tt>
     * @throws ArithmeticException {@code exponent} 为负数。这会导致操作结果不是整数。
     */
    public BigInteger pow(int exponent) {
        if (exponent < 0) {
            throw new ArithmeticException("Negative exponent");
        }
        if (signum == 0) {
            return (exponent == 0 ? ONE : this);
        }

        BigInteger partToSquare = this.abs();

        // 从基数中分解出2的幂，因为这些幂可以通过左移快速计算。
        // 剩余部分可以更快地进行指数运算。最后将2的幂乘回去。
        int powersOfTwo = partToSquare.getLowestSetBit();
        long bitsToShiftLong = (long)powersOfTwo * exponent;
        if (bitsToShiftLong > Integer.MAX_VALUE) {
            reportOverflow();
        }
        int bitsToShift = (int)bitsToShiftLong;

        int remainingBits;

        // 如果需要，通过右移快速分解出2的幂。
        if (powersOfTwo > 0) {
            partToSquare = partToSquare.shiftRight(powersOfTwo);
            remainingBits = partToSquare.bitLength();
            if (remainingBits == 1) {  // 除了 +/- 1 之外什么都没有？
                if (signum < 0 && (exponent&1) == 1) {
                    return NEGATIVE_ONE.shiftLeft(bitsToShift);
                } else {
                    return ONE.shiftLeft(bitsToShift);
                }
            }
        } else {
            remainingBits = partToSquare.bitLength();
            if (remainingBits == 1) { // 除了 +/- 1 之外什么都没有？
                if (signum < 0  && (exponent&1) == 1) {
                    return NEGATIVE_ONE;
                } else {
                    return ONE;
                }
            }
        }

        // 这是一种快速近似结果大小的方法，类似于做 log2[n] * exponent。这将给出结果的最大可能大小，以及使用哪种算法。
        long scaleFactor = (long)remainingBits * exponent;

        // 根据操作数的大小使用不同的算法。
        // 看看结果是否可以安全地放入一个long中。 (最大 2^63-1)
        if (partToSquare.mag.length == 1 && scaleFactor <= 62) {
            // 小数算法。所有内容都适合一个long。
            int newSign = (signum <0  && (exponent&1) == 1 ? -1 : 1);
            long result = 1;
            long baseToPow2 = partToSquare.mag[0] & LONG_MASK;

            int workingExponent = exponent;

            // 使用重复平方技巧进行指数运算
            while (workingExponent != 0) {
                if ((workingExponent & 1) == 1) {
                    result = result * baseToPow2;
                }

                if ((workingExponent >>>= 1) != 0) {
                    baseToPow2 = baseToPow2 * baseToPow2;
                }
            }

            // 通过左移快速乘回2的幂
            if (powersOfTwo > 0) {
                if (bitsToShift + scaleFactor <= 62) { // 适合long？
                    return valueOf((result << bitsToShift) * newSign);
                } else {
                    return valueOf(result*newSign).shiftLeft(bitsToShift);
                }
            } else {
                return valueOf(result*newSign);
            }
        } else {
            if ((long)bitLength() * exponent / Integer.SIZE > MAX_MAG_LENGTH) {
                reportOverflow();
            }

            // 大数算法。这基本上与上面的算法相同，但调用 multiply() 和 square()，这些方法可能对大数使用更高效的算法。
            BigInteger answer = ONE;

            int workingExponent = exponent;
            // 使用重复平方技巧进行指数运算
            while (workingExponent != 0) {
                if ((workingExponent & 1) == 1) {
                    answer = answer.multiply(partToSquare);
                }

                if ((workingExponent >>>= 1) != 0) {
                    partToSquare = partToSquare.square();
                }
            }
            // 通过左移快速乘回（指数化的）2的幂
            if (powersOfTwo > 0) {
                answer = answer.shiftLeft(bitsToShift);
            }

            if (signum < 0 && (exponent&1) == 1) {
                return answer.negate();
            } else {
                return answer;
            }
        }
    }

    /**
     * 返回一个BigInteger，其值为 {@code abs(this)} 和 {@code abs(val)} 的最大公约数。如果
     * {@code this == 0 && val == 0}，则返回0。
     *
     * @param  val 用于计算GCD的值。
     * @return {@code GCD(abs(this), abs(val))}
     */
    public BigInteger gcd(BigInteger val) {
        if (val.signum == 0)
            return this.abs();
        else if (this.signum == 0)
            return val.abs();

        MutableBigInteger a = new MutableBigInteger(this);
        MutableBigInteger b = new MutableBigInteger(val);

        MutableBigInteger result = a.hybridGCD(b);

        return result.toBigInteger(1);
    }

    /**
     * 包私有方法，返回整数的位长度。
     */
    static int bitLengthForInt(int n) {
        return 32 - Integer.numberOfLeadingZeros(n);
    }

    /**
     * 左移int数组a至len位，n位。返回移位后的数组，因为可能需要重新分配空间。
     */
    private static int[] leftShift(int[] a, int len, int n) {
        int nInts = n >>> 5;
        int nBits = n&0x1F;
        int bitsInHighWord = bitLengthForInt(a[0]);

        // 如果移位不需要重新复制，直接进行
        if (n <= (32-bitsInHighWord)) {
            primitiveLeftShift(a, len, nBits);
            return a;
        } else { // 数组必须调整大小
            if (nBits <= (32-bitsInHighWord)) {
                int result[] = new int[nInts+len];
                System.arraycopy(a, 0, result, 0, len);
                primitiveLeftShift(result, result.length, nBits);
                return result;
            } else {
                int result[] = new int[nInts+len+1];
                System.arraycopy(a, 0, result, 0, len);
                primitiveRightShift(result, result.length, 32 - nBits);
                return result;
            }
        }
    }


                // shifts a up to len right n bits assumes no leading zeros, 0<n<32
    static void primitiveRightShift(int[] a, int len, int n) {
        int n2 = 32 - n;
        for (int i=len-1, c=a[i]; i > 0; i--) {
            int b = c;
            c = a[i-1];
            a[i] = (c << n2) | (b >>> n);
        }
        a[0] >>>= n;
    }

    // shifts a up to len left n bits assumes no leading zeros, 0<=n<32
    static void primitiveLeftShift(int[] a, int len, int n) {
        if (len == 0 || n == 0)
            return;

        int n2 = 32 - n;
        for (int i=0, c=a[i], m=i+len-1; i < m; i++) {
            int b = c;
            c = a[i+1];
            a[i] = (b << n) | (c >>> n2);
        }
        a[len-1] <<= n;
    }

    /**
     * 计算 int 数组前 len 个元素的位长度，假设没有前导零 int。
     */
    private static int bitLength(int[] val, int len) {
        if (len == 0)
            return 0;
        return ((len - 1) << 5) + bitLengthForInt(val[0]);
    }

    /**
     * 返回一个其值为该 BigInteger 的绝对值的 BigInteger。
     *
     * @return {@code abs(this)}
     */
    public BigInteger abs() {
        return (signum >= 0 ? this : this.negate());
    }

    /**
     * 返回一个其值为 {@code (-this)} 的 BigInteger。
     *
     * @return {@code -this}
     */
    public BigInteger negate() {
        return new BigInteger(this.mag, -this.signum);
    }

    /**
     * 返回该 BigInteger 的符号函数。
     *
     * @return -1, 0 或 1，分别表示该 BigInteger 的值为负、零或正。
     */
    public int signum() {
        return this.signum;
    }

    // 模运算操作

    /**
     * 返回一个其值为 {@code (this mod m)} 的 BigInteger。此方法与 {@code remainder} 不同，它始终返回一个
     * <i>非负</i> 的 BigInteger。
     *
     * @param  m 模数。
     * @return {@code this mod m}
     * @throws ArithmeticException {@code m} &le; 0
     * @see    #remainder
     */
    public BigInteger mod(BigInteger m) {
        if (m.signum <= 0)
            throw new ArithmeticException("BigInteger: modulus not positive");

        BigInteger result = this.remainder(m);
        return (result.signum >= 0 ? result : result.add(m));
    }

    /**
     * 返回一个其值为
     * <tt>(this<sup>exponent</sup> mod m)</tt> 的 BigInteger。 （与 {@code pow} 不同，此方法允许负指数。）
     *
     * @param  exponent 指数。
     * @param  m 模数。
     * @return <tt>this<sup>exponent</sup> mod m</tt>
     * @throws ArithmeticException {@code m} &le; 0 或指数为负且该 BigInteger 与 {@code m} 不互质。
     * @see    #modInverse
     */
    public BigInteger modPow(BigInteger exponent, BigInteger m) {
        if (m.signum <= 0)
            throw new ArithmeticException("BigInteger: modulus not positive");

        // 特殊情况
        if (exponent.signum == 0)
            return (m.equals(ONE) ? ZERO : ONE);

        if (this.equals(ONE))
            return (m.equals(ONE) ? ZERO : ONE);

        if (this.equals(ZERO) && exponent.signum >= 0)
            return ZERO;

        if (this.equals(negConst[1]) && (!exponent.testBit(0)))
            return (m.equals(ONE) ? ZERO : ONE);

        boolean invertResult;
        if ((invertResult = (exponent.signum < 0)))
            exponent = exponent.negate();

        BigInteger base = (this.signum < 0 || this.compareTo(m) >= 0
                           ? this.mod(m) : this);
        BigInteger result;
        if (m.testBit(0)) { // 奇数模数
            result = base.oddModPow(exponent, m);
        } else {
            /*
             * 偶数模数。将其拆分为“奇数部分”（m1）和 2 的幂（m2），模 m1 指数运算，手动模 m2 指数运算，
             * 然后使用中国剩余定理组合结果。
             */

            // 将 m 拆分为奇数部分（m1）和 2 的幂（m2）
            int p = m.getLowestSetBit();   // 最大 2 的幂次方数，可以整除 m

            BigInteger m1 = m.shiftRight(p);  // m/2**p
            BigInteger m2 = ONE.shiftLeft(p); // 2**p

            // 计算新的基数 m1
            BigInteger base2 = (this.signum < 0 || this.compareTo(m1) >= 0
                                ? this.mod(m1) : this);

            // 计算 (base ** exponent) mod m1。
            BigInteger a1 = (m1.equals(ONE) ? ZERO :
                             base2.oddModPow(exponent, m1));

            // 计算 (this ** exponent) mod m2
            BigInteger a2 = base.modPow2(exponent, p);

            // 使用中国剩余定理组合结果
            BigInteger y1 = m2.modInverse(m1);
            BigInteger y2 = m1.modInverse(m2);

            if (m.mag.length < MAX_MAG_LENGTH / 2) {
                result = a1.multiply(m2).multiply(y1).add(a2.multiply(m1).multiply(y2)).mod(m);
            } else {
                MutableBigInteger t1 = new MutableBigInteger();
                new MutableBigInteger(a1.multiply(m2)).multiply(new MutableBigInteger(y1), t1);
                MutableBigInteger t2 = new MutableBigInteger();
                new MutableBigInteger(a2.multiply(m1)).multiply(new MutableBigInteger(y2), t2);
                t1.add(t2);
                MutableBigInteger q = new MutableBigInteger();
                result = t1.divide(new MutableBigInteger(m), q).toBigInteger();
            }
        }

        return (invertResult ? result.modInverse(m) : result);
    }

    // 蒙哥马利乘法。这些是为虚拟机内联函数准备的包装器。对于非常大的操作数，我们不使用内联函数：
    // MONTGOMERY_INTRINSIC_THRESHOLD 应大于任何合理的加密密钥。
    private static int[] montgomeryMultiply(int[] a, int[] b, int[] n, int len, long inv,
                                            int[] product) {
        implMontgomeryMultiplyChecks(a, b, n, len, product);
        if (len > MONTGOMERY_INTRINSIC_THRESHOLD) {
            // 非常长的参数：不使用内联函数
            product = multiplyToLen(a, len, b, len, product);
            return montReduce(product, n, len, (int)inv);
        } else {
            return implMontgomeryMultiply(a, b, n, len, inv, materialize(product, len));
        }
    }
    private static int[] montgomerySquare(int[] a, int[] n, int len, long inv,
                                          int[] product) {
        implMontgomeryMultiplyChecks(a, a, n, len, product);
        if (len > MONTGOMERY_INTRINSIC_THRESHOLD) {
            // 非常长的参数：不使用内联函数
            product = squareToLen(a, len, product);
            return montReduce(product, n, len, (int)inv);
        } else {
            return implMontgomerySquare(a, n, len, inv, materialize(product, len));
        }
    }

    // 检查所有范围。
    private static void implMontgomeryMultiplyChecks
        (int[] a, int[] b, int[] n, int len, int[] product) throws RuntimeException {
        if (len % 2 != 0) {
            throw new IllegalArgumentException("输入数组长度必须为偶数: " + len);
        }

        if (len < 1) {
            throw new IllegalArgumentException("无效的输入长度: " + len);
        }

        if (len > a.length ||
            len > b.length ||
            len > n.length ||
            (product != null && len > product.length)) {
            throw new IllegalArgumentException("输入数组长度超出范围: " + len);
        }
    }

    // 确保 int 数组 z（预期包含蒙哥马利乘法的结果）存在且足够大。
    private static int[] materialize(int[] z, int len) {
         if (z == null || z.length < len)
             z = new int[len];
         return z;
    }

    // 这些方法旨在被虚拟机内联函数替换。
    private static int[] implMontgomeryMultiply(int[] a, int[] b, int[] n, int len,
                                         long inv, int[] product) {
        product = multiplyToLen(a, len, b, len, product);
        return montReduce(product, n, len, (int)inv);
    }
    private static int[] implMontgomerySquare(int[] a, int[] n, int len,
                                       long inv, int[] product) {
        product = squareToLen(a, len, product);
        return montReduce(product, n, len, (int)inv);
    }

    static int[] bnExpModThreshTable = {7, 25, 81, 241, 673, 1793,
                                                Integer.MAX_VALUE}; // 哨兵

    /**
     * 返回一个其值为 x 的 y 次幂模 z 的 BigInteger。
     * 假设：z 是奇数 && x < z。
     */
    private BigInteger oddModPow(BigInteger y, BigInteger z) {
    /*
     * 该算法改编自 Colin Plumb 的 C 库。
     *
     * 窗口算法：
     * 思想是保持一个运行中的乘积 b1 = n^(高阶位的 exp)，然后继续附加指数位。以下模式适用于 3 位窗口（k = 3）：
     * 附加   0: 平方
     * 附加   1: 平方，乘以 n^1
     * 附加  10: 平方，乘以 n^1，平方
     * 附加  11: 平方，平方，乘以 n^3
     * 附加 100: 平方，乘以 n^1，平方，平方
     * 附加 101: 平方，平方，平方，乘以 n^5
     * 附加 110: 平方，平方，乘以 n^3，平方
     * 附加 111: 平方，平方，平方，乘以 n^7
     *
     * 由于每个模式只涉及一次乘法，模式越长越好，除了 0（没有乘法）可以直接附加。
     * 我们预先计算一个 n 的奇数幂表，最多 2^k，然后可以一次附加 k 位指数。实际上，假设随机指数，
     * 平均每次需要乘法之间有一个零位（1/2 的时间没有，1/4 的时间有一个，1/8 的时间有两个，1/32 的时间有三个，等等），
     * 所以你每次需要 k+1 位指数进行一次乘法。
     *
     * 循环遍历指数，随着循环进行平方结果缓冲区。有一个 wbits+1 位的预读缓冲区，buf，填充即将来临的指数位。
     * （读取指数末尾后的内容无关紧要，但这里填充为零。）当该缓冲区的最高位设置时，即 (buf & tblmask) != 0，
     * 我们必须决定要乘以哪个模式，以及何时进行。我们决定，记住在未来适当数量的平方后进行（例如，缓冲区中的“100”模式
     * 要求立即乘以 n^1；“110”模式要求在一次更多平方后乘以 n^3），清空缓冲区，然后继续。
     *
     * 当我们开始时，还有一个优化：结果缓冲区隐式为一，因此平方它或乘以它可以优化掉。此外，如果我们在预读窗口中
     * 以“100”模式开始，而不是将 n 放入缓冲区然后开始平方它，我们已经计算了 n^2 以计算奇数幂表，所以我们可以
     * 将其放入缓冲区并节省一次平方。
     *
     * 这意味着如果你有一个 k 位窗口，计算 n^z，其中 z 是指数的高 k 位，1/2 的时间不需要平方。1/4 的时间需要 1
     * 次平方，... 1/2^(k-1) 的时间需要 k-2 次平方。剩余的 1/2^(k-1) 的时间，最高 k 位是一个 1 后跟 k-1 个 0 位，
     * 所以它再次只需要 k-2 次平方，而不是 k-1。这些的平均值是 1。加上计算表所需的平方，
     * 你会看到 k 位窗口节省了 k-2 次平方以及减少了乘法。（实际上在 k = 1 的情况下也不会有损害。）
     */
        // 指数为一的特殊情况
        if (y.equals(ONE))
            return this;

        // 基数为零的特殊情况
        if (signum == 0)
            return ZERO;

        int[] base = mag.clone();
        int[] exp = y.mag;
        int[] mod = z.mag;
        int modLen = mod.length;

        // 使 modLen 为偶数。通常使用 512、768、1024 或 2048 位的加密模数，因此此代码通常不会执行。
        // 但是，对于 HotSpot 内联函数的正确运行是必要的。
        if ((modLen & 1) != 0) {
            int[] x = new int[modLen + 1];
            System.arraycopy(mod, 0, x, 1, modLen);
            mod = x;
            modLen++;
        }

        // 选择适当的窗口大小
        int wbits = 0;
        int ebits = bitLength(exp, exp.length);
        // 如果指数为 65537 (0x10001)，使用最小窗口大小
        if ((ebits != 17) || (exp[0] != 65537)) {
            while (ebits > bnExpModThreshTable[wbits]) {
                wbits++;
            }
        }

        // 计算适当的表大小
        int tblmask = 1 << wbits;

        // 分配表以预计算基数的蒙哥马利形式的奇数幂
        int[][] table = new int[tblmask][];
        for (int i=0; i < tblmask; i++)
            table[i] = new int[modLen];

        // 计算模数的最低 64 位数字的模逆
        long n0 = (mod[modLen-1] & LONG_MASK) + ((mod[modLen-2] & LONG_MASK) << 32);
        long inv = -MutableBigInteger.inverseMod64(n0);

        // 将基数转换为蒙哥马利形式
        int[] a = leftShift(base, base.length, modLen << 5);

        MutableBigInteger q = new MutableBigInteger(),
                          a2 = new MutableBigInteger(a),
                          b2 = new MutableBigInteger(mod);
        b2.normalize(); // MutableBigInteger.divide() 假设其除数处于正常形式。

        MutableBigInteger r= a2.divide(b2, q);
        table[0] = r.toIntArray();

        // 用前导零填充 table[0] 以确保其长度至少为 modLen
        if (table[0].length < modLen) {
           int offset = modLen - table[0].length;
           int[] t2 = new int[modLen];
           System.arraycopy(table[0], 0, t2, offset, table[0].length);
           table[0] = t2;
        }

        // 设置 b 为基数的平方
        int[] b = montgomerySquare(table[0], mod, modLen, inv, null);

        // 设置 t 为 b 的高半部分
        int[] t = Arrays.copyOf(b, modLen);

        // 用基数的奇数幂填充表
        for (int i=1; i < tblmask; i++) {
            table[i] = montgomeryMultiply(t, table[i-1], mod, modLen, inv, null);
        }

        // 预加载滑动窗口
        int bitpos = 1 << ((ebits-1) & (32-1));

        int buf = 0;
        int elen = exp.length;
        int eIndex = 0;
        for (int i = 0; i <= wbits; i++) {
            buf = (buf << 1) | (((exp[eIndex] & bitpos) != 0)?1:0);
            bitpos >>>= 1;
            if (bitpos == 0) {
                eIndex++;
                bitpos = 1 << (32-1);
                elen--;
            }
        }


    /*
     * Copyright (c) 1996, 1999, ...
     */
    int multpos = ebits;

    // 第一次迭代，从主循环中提取出来
    ebits--;
    boolean isone = true;

    multpos = ebits - wbits;
    while ((buf & 1) == 0) {
        buf >>>= 1;
        multpos++;
    }

    int[] mult = table[buf >>> 1];

    buf = 0;
    if (multpos == ebits)
        isone = false;

    // 主循环
    while (true) {
        ebits--;
        // 前进窗口
        buf <<= 1;

        if (elen != 0) {
            buf |= ((exp[eIndex] & bitpos) != 0) ? 1 : 0;
            bitpos >>>= 1;
            if (bitpos == 0) {
                eIndex++;
                bitpos = 1 << (32-1);
                elen--;
            }
        }

        // 检查窗口中是否有待乘法
        if ((buf & tblmask) != 0) {
            multpos = ebits - wbits;
            while ((buf & 1) == 0) {
                buf >>>= 1;
                multpos++;
            }
            mult = table[buf >>> 1];
            buf = 0;
        }

        // 执行乘法
        if (ebits == multpos) {
            if (isone) {
                b = mult.clone();
                isone = false;
            } else {
                t = b;
                a = montgomeryMultiply(t, mult, mod, modLen, inv, a);
                t = a; a = b; b = t;
            }
        }

        // 检查是否完成
        if (ebits == 0)
            break;

        // 平方输入
        if (!isone) {
            t = b;
            a = montgomerySquare(t, mod, modLen, inv, a);
            t = a; a = b; b = t;
        }
    }

    // 将结果转换出 Montgomery 形式并返回
    int[] t2 = new int[2*modLen];
    System.arraycopy(b, 0, t2, modLen, modLen);

    b = montReduce(t2, mod, modLen, (int)inv);

    t2 = Arrays.copyOf(b, modLen);

    return new BigInteger(1, t2);
}

/**
 * Montgomery 减少 n，模 mod。这减少了模数并除以 2^(32*mlen)。改编自 Colin Plumb 的 C 库。
 */
private static int[] montReduce(int[] n, int[] mod, int mlen, int inv) {
    int c=0;
    int len = mlen;
    int offset=0;

    do {
        int nEnd = n[n.length-1-offset];
        int carry = mulAdd(n, mod, offset, mlen, inv * nEnd);
        c += addOne(n, offset, mlen, carry);
        offset++;
    } while (--len > 0);

    while (c > 0)
        c += subN(n, mod, mlen);

    while (intArrayCmpToLen(n, mod, mlen) >= 0)
        subN(n, mod, mlen);

    return n;
}

/**
 * 返回 -1, 0 或 +1，表示大端无符号整数数组 arg1 是否小于、等于或大于 arg2，直到长度 len。
 */
private static int intArrayCmpToLen(int[] arg1, int[] arg2, int len) {
    for (int i=0; i < len; i++) {
        long b1 = arg1[i] & LONG_MASK;
        long b2 = arg2[i] & LONG_MASK;
        if (b1 < b2)
            return -1;
        if (b1 > b2)
            return 1;
    }
    return 0;
}

/**
 * 从两个相同长度的数中减去，返回借位。
 */
private static int subN(int[] a, int[] b, int len) {
    long sum = 0;

    while (--len >= 0) {
        sum = (a[len] & LONG_MASK) -
             (b[len] & LONG_MASK) + (sum >> 32);
        a[len] = (int)sum;
    }

    return (int)(sum >> 32);
}

/**
 * 将数组乘以一个字 k 并加到结果中，返回进位
 */
static int mulAdd(int[] out, int[] in, int offset, int len, int k) {
    implMulAddCheck(out, in, offset, len, k);
    return implMulAdd(out, in, offset, len, k);
}

/**
 * 参数验证。
 */
private static void implMulAddCheck(int[] out, int[] in, int offset, int len, int k) {
    if (len > in.length) {
        throw new IllegalArgumentException("输入长度超出范围: " + len + " > " + in.length);
    }
    if (offset < 0) {
        throw new IllegalArgumentException("输入偏移无效: " + offset);
    }
    if (offset > (out.length - 1)) {
        throw new IllegalArgumentException("输入偏移超出范围: " + offset + " > " + (out.length - 1));
    }
    if (len > (out.length - offset)) {
        throw new IllegalArgumentException("输入长度超出范围: " + len + " > " + (out.length - offset));
    }
}

/**
 * Java 运行时可能使用内联优化此方法。
 */
private static int implMulAdd(int[] out, int[] in, int offset, int len, int k) {
    long kLong = k & LONG_MASK;
    long carry = 0;

    offset = out.length-offset - 1;
    for (int j=len-1; j >= 0; j--) {
        long product = (in[j] & LONG_MASK) * kLong +
                       (out[offset] & LONG_MASK) + carry;
        out[offset--] = (int)product;
        carry = product >>> 32;
    }
    return (int)carry;
}

/**
 * 将一个字加到 a 中 mlen 个字的位置。返回结果的进位。
 */
static int addOne(int[] a, int offset, int mlen, int carry) {
    offset = a.length-1-mlen-offset;
    long t = (a[offset] & LONG_MASK) + (carry & LONG_MASK);

    a[offset] = (int)t;
    if ((t >>> 32) == 0)
        return 0;
    while (--mlen >= 0) {
        if (--offset < 0) { // 进位超出数
            return 1;
        } else {
            a[offset]++;
            if (a[offset] != 0)
                return 0;
        }
    }
    return 1;
}

/**
 * 返回一个 BigInteger，其值为 (this ** 指数) mod (2**p)
 */
private BigInteger modPow2(BigInteger exponent, int p) {
    /*
     * 使用重复平方技巧进行指数运算，根据模数截断高位。
     */
    BigInteger result = ONE;
    BigInteger baseToPow2 = this.mod2(p);
    int expOffset = 0;

    int limit = exponent.bitLength();

    if (this.testBit(0))
       limit = (p-1) < limit ? (p-1) : limit;

    while (expOffset < limit) {
        if (exponent.testBit(expOffset))
            result = result.multiply(baseToPow2).mod2(p);
        expOffset++;
        if (expOffset < limit)
            baseToPow2 = baseToPow2.square().mod2(p);
    }

    return result;
}

/**
 * 返回一个 BigInteger，其值为 this mod(2**p)。
 * 假设 this {@code BigInteger >= 0} 且 {@code p > 0}。
 */
private BigInteger mod2(int p) {
    if (bitLength() <= p)
        return this;

    // 复制剩余的 int
    int numInts = (p + 31) >>> 5;
    int[] mag = new int[numInts];
    System.arraycopy(this.mag, (this.mag.length - numInts), mag, 0, numInts);

    // 掩码掉任何多余的位
    int excessBits = (numInts << 5) - p;
    mag[0] &= (1L << (32-excessBits)) - 1;

    return (mag[0] == 0 ? new BigInteger(1, mag) : new BigInteger(mag, 1));
}

/**
 * 返回一个 BigInteger，其值为 {@code (this}<sup>-1</sup> {@code mod m)}。
 *
 * @param  m 模数。
 * @return {@code this}<sup>-1</sup> {@code mod m}。
 * @throws ArithmeticException {@code  m} &le; 0，或此 BigInteger
 *         没有模 m 的乘法逆元（即此 BigInteger
 *         与 m 不是 <i>互质</i>）。
 */
public BigInteger modInverse(BigInteger m) {
    if (m.signum != 1)
        throw new ArithmeticException("BigInteger: 模数不是正数");

    if (m.equals(ONE))
        return ZERO;

    // 计算 (this mod m)
    BigInteger modVal = this;
    if (signum < 0 || (this.compareMagnitude(m) >= 0))
        modVal = this.mod(m);

    if (modVal.equals(ONE))
        return ONE;

    MutableBigInteger a = new MutableBigInteger(modVal);
    MutableBigInteger b = new MutableBigInteger(m);

    MutableBigInteger result = a.mutableModInverse(b);
    return result.toBigInteger(1);
}

// 位移操作

/**
 * 返回一个 BigInteger，其值为 {@code (this << n)}。
 * 位移距离 {@code n} 可以是负数，此时此方法执行右移。
 * (计算 <tt>floor(this * 2<sup>n</sup>)</tt>.)
 *
 * @param  n 位移距离，以位为单位。
 * @return {@code this << n}
 * @see #shiftRight
 */
public BigInteger shiftLeft(int n) {
    if (signum == 0)
        return ZERO;
    if (n > 0) {
        return new BigInteger(shiftLeft(mag, n), signum);
    } else if (n == 0) {
        return this;
    } else {
        // 可能的 int 溢出在 (-n) 中不是问题，
        // 因为 shiftRightImpl 将其参数视为无符号
        return shiftRightImpl(-n);
    }
}

/**
 * 返回一个值为 {@code (mag << n)} 的 magnitude 数组。
 * 位移距离 {@code n} 被视为无符号。
 * (计算 <tt>this * 2<sup>n</sup></tt>.)
 *
 * @param mag 数值，最高有效 int ({@code mag[0]}) 必须非零。
 * @param  n 无符号位移距离，以位为单位。
 * @return {@code mag << n}
 */
private static int[] shiftLeft(int[] mag, int n) {
    int nInts = n >>> 5;
    int nBits = n & 0x1f;
    int magLen = mag.length;
    int newMag[] = null;

    if (nBits == 0) {
        newMag = new int[magLen + nInts];
        System.arraycopy(mag, 0, newMag, 0, magLen);
    } else {
        int i = 0;
        int nBits2 = 32 - nBits;
        int highBits = mag[0] >>> nBits2;
        if (highBits != 0) {
            newMag = new int[magLen + nInts + 1];
            newMag[i++] = highBits;
        } else {
            newMag = new int[magLen + nInts];
        }
        int j=0;
        while (j < magLen-1)
            newMag[i++] = mag[j++] << nBits | mag[j] >>> nBits2;
        newMag[i] = mag[j] << nBits;
    }
    return newMag;
}

/**
 * 返回一个 BigInteger，其值为 {@code (this >> n)}。 执行符号扩展。位移距离 {@code n} 可以是负数，此时此方法执行左移。
 * (计算 <tt>floor(this / 2<sup>n</sup>)</tt>.)
 *
 * @param  n 位移距离，以位为单位。
 * @return {@code this >> n}
 * @see #shiftLeft
 */
public BigInteger shiftRight(int n) {
    if (signum == 0)
        return ZERO;
    if (n > 0) {
        return shiftRightImpl(n);
    } else if (n == 0) {
        return this;
    } else {
        // 可能的 int 溢出在 {@code -n} 中不是问题，
        // 因为 shiftLeft 将其参数视为无符号
        return new BigInteger(shiftLeft(mag, -n), signum);
    }
}

/**
 * 返回一个 BigInteger，其值为 {@code (this >> n)}。位移距离 {@code n} 被视为无符号。
 * (计算 <tt>floor(this * 2<sup>-n</sup>)</tt>.)
 *
 * @param  n 无符号位移距离，以位为单位。
 * @return {@code this >> n}
 */
private BigInteger shiftRightImpl(int n) {
    int nInts = n >>> 5;
    int nBits = n & 0x1f;
    int magLen = mag.length;
    int newMag[] = null;

    // 特殊情况：整个内容被移出
    if (nInts >= magLen)
        return (signum >= 0 ? ZERO : negConst[1]);

    if (nBits == 0) {
        int newMagLen = magLen - nInts;
        newMag = Arrays.copyOf(mag, newMagLen);
    } else {
        int i = 0;
        int highBits = mag[0] >>> nBits;
        if (highBits != 0) {
            newMag = new int[magLen - nInts];
            newMag[i++] = highBits;
        } else {
            newMag = new int[magLen - nInts -1];
        }

        int nBits2 = 32 - nBits;
        int j=0;
        while (j < magLen - nInts - 1)
            newMag[i++] = (mag[j++] << nBits2) | (mag[j] >>> nBits);
    }

    if (signum < 0) {
        // 查找是否有任何一位被移出
        boolean onesLost = false;
        for (int i=magLen-1, j=magLen-nInts; i >= j && !onesLost; i--)
            onesLost = (mag[i] != 0);
        if (!onesLost && nBits != 0)
            onesLost = (mag[magLen - nInts - 1] << (32 - nBits) != 0);

        if (onesLost)
            newMag = javaIncrement(newMag);
    }

    return new BigInteger(newMag, signum);
}

int[] javaIncrement(int[] val) {
    int lastSum = 0;
    for (int i=val.length-1;  i >= 0 && lastSum == 0; i--)
        lastSum = (val[i] += 1);
    if (lastSum == 0) {
        val = new int[val.length+1];
        val[0] = 1;
    }
    return val;
}

// 位运算

/**
 * 返回一个 BigInteger，其值为 {@code (this & val)}。 (此方法返回一个负的 BigInteger 当且仅当 this 和 val 都是负数。)
 *
 * @param val 要与 this 进行 AND 运算的值。
 * @return {@code this & val}
 */
public BigInteger and(BigInteger val) {
    int[] result = new int[Math.max(intLength(), val.intLength())];
    for (int i=0; i < result.length; i++)
        result[i] = (getInt(result.length-i-1)
                     & val.getInt(result.length-i-1));

    return valueOf(result);
}

/**
 * 返回一个 BigInteger，其值为 {@code (this | val)}。 (此方法返回一个负的 BigInteger 当且仅当 this 或 val 是负数。)
 *
 * @param val 要与 this 进行 OR 运算的值。
 * @return {@code this | val}
 */
public BigInteger or(BigInteger val) {
    int[] result = new int[Math.max(intLength(), val.intLength())];
    for (int i=0; i < result.length; i++)
        result[i] = (getInt(result.length-i-1)
                     | val.getInt(result.length-i-1));

    return valueOf(result);
}

/**
 * 返回一个 BigInteger，其值为 {@code (this ^ val)}。 (此方法返回一个负的 BigInteger 当且仅当 this 和 val 中恰好有一个是负数。)
 *
 * @param val 要与 this 进行 XOR 运算的值。
 * @return {@code this ^ val}
 */
public BigInteger xor(BigInteger val) {
    int[] result = new int[Math.max(intLength(), val.intLength())];
    for (int i=0; i < result.length; i++)
        result[i] = (getInt(result.length-i-1)
                     ^ val.getInt(result.length-i-1));

    return valueOf(result);
}

/**
 * 返回一个 BigInteger，其值为 {@code (~this)}。 (此方法返回一个负值当且仅当此 BigInteger 是非负的。)
 *
 * @return {@code ~this}
 */
public BigInteger not() {
    int[] result = new int[intLength()];
    for (int i=0; i < result.length; i++)
        result[i] = ~getInt(result.length-i-1);


                    return valueOf(result);
    }

    /**
     * 返回一个其值为 {@code (this & ~val)} 的 BigInteger。此方法等同于 {@code and(val.not())}，作为掩码操作的便利提供。
     * （当且仅当 {@code this} 为负且 {@code val} 为正时，此方法返回负的 BigInteger。）
     *
     * @param val 要被取反并与此 BigInteger 进行 AND 操作的值。
     * @return {@code this & ~val}
     */
    public BigInteger andNot(BigInteger val) {
        int[] result = new int[Math.max(intLength(), val.intLength())];
        for (int i=0; i < result.length; i++)
            result[i] = (getInt(result.length-i-1)
                         & ~val.getInt(result.length-i-1));

        return valueOf(result);
    }


    // 单位位操作

    /**
     * 如果且仅如果指定的位被设置，则返回 {@code true}。
     * （计算 {@code ((this & (1<<n)) != 0)}。）
     *
     * @param  n 要测试的位的索引。
     * @return 如果且仅如果指定的位被设置，则返回 {@code true}。
     * @throws ArithmeticException {@code n} 为负。
     */
    public boolean testBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");

        return (getInt(n >>> 5) & (1 << (n & 31))) != 0;
    }

    /**
     * 返回一个其值与此 BigInteger 相等且指定位被设置的 BigInteger。
     * （计算 {@code (this | (1<<n))}。）
     *
     * @param  n 要设置的位的索引。
     * @return {@code this | (1<<n)}
     * @throws ArithmeticException {@code n} 为负。
     */
    public BigInteger setBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");

        int intNum = n >>> 5;
        int[] result = new int[Math.max(intLength(), intNum+2)];

        for (int i=0; i < result.length; i++)
            result[result.length-i-1] = getInt(i);

        result[result.length-intNum-1] |= (1 << (n & 31));

        return valueOf(result);
    }

    /**
     * 返回一个其值与此 BigInteger 相等且指定位被清除的 BigInteger。
     * （计算 {@code (this & ~(1<<n))}。）
     *
     * @param  n 要清除的位的索引。
     * @return {@code this & ~(1<<n)}
     * @throws ArithmeticException {@code n} 为负。
     */
    public BigInteger clearBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");

        int intNum = n >>> 5;
        int[] result = new int[Math.max(intLength(), ((n + 1) >>> 5) + 1)];

        for (int i=0; i < result.length; i++)
            result[result.length-i-1] = getInt(i);

        result[result.length-intNum-1] &= ~(1 << (n & 31));

        return valueOf(result);
    }

    /**
     * 返回一个其值与此 BigInteger 相等且指定位被翻转的 BigInteger。
     * （计算 {@code (this ^ (1<<n))}。）
     *
     * @param  n 要翻转的位的索引。
     * @return {@code this ^ (1<<n)}
     * @throws ArithmeticException {@code n} 为负。
     */
    public BigInteger flipBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");

        int intNum = n >>> 5;
        int[] result = new int[Math.max(intLength(), intNum+2)];

        for (int i=0; i < result.length; i++)
            result[result.length-i-1] = getInt(i);

        result[result.length-intNum-1] ^= (1 << (n & 31));

        return valueOf(result);
    }

    /**
     * 返回此 BigInteger 中最低位（最低序）1 位的索引（最低位 1 位右侧的零位数）。如果此 BigInteger 不包含 1 位，则返回 -1。
     * （计算 {@code (this == 0? -1 : log2(this & -this))}。）
     *
     * @return 此 BigInteger 中最低位 1 位的索引。
     */
    public int getLowestSetBit() {
        @SuppressWarnings("deprecation") int lsb = lowestSetBit - 2;
        if (lsb == -2) {  // lowestSetBit 未初始化
            lsb = 0;
            if (signum == 0) {
                lsb -= 1;
            } else {
                // 查找最低序非零 int
                int i,b;
                for (i=0; (b = getInt(i)) == 0; i++)
                    ;
                lsb += (i << 5) + Integer.numberOfTrailingZeros(b);
            }
            lowestSetBit = lsb + 2;
        }
        return lsb;
    }


    // 其他位操作

    /**
     * 返回此 BigInteger 的最小二进制补码表示中的位数，不包括符号位。
     * 对于正的 BigInteger，这等同于普通二进制表示中的位数。
     * （计算 {@code (ceil(log2(this < 0 ? -this : this+1)))}。）
     *
     * @return 此 BigInteger 的最小二进制补码表示中的位数，不包括符号位。
     */
    public int bitLength() {
        @SuppressWarnings("deprecation") int n = bitLength - 1;
        if (n == -1) { // bitLength 未初始化
            int[] m = mag;
            int len = m.length;
            if (len == 0) {
                n = 0; // 偏移 1 以初始化
            }  else {
                // 计算幅度的位数
                int magBitLength = ((len - 1) << 5) + bitLengthForInt(mag[0]);
                 if (signum < 0) {
                     // 检查幅度是否为 2 的幂
                     boolean pow2 = (Integer.bitCount(mag[0]) == 1);
                     for (int i=1; i< len && pow2; i++)
                         pow2 = (mag[i] == 0);

                     n = (pow2 ? magBitLength - 1 : magBitLength);
                 } else {
                     n = magBitLength;
                 }
            }
            bitLength = n + 1;
        }
        return n;
    }

    /**
     * 返回此 BigInteger 的二进制补码表示中与符号位不同的位数。当在 BigInteger 上实现位向量风格的集合时，此方法很有用。
     *
     * @return 此 BigInteger 的二进制补码表示中与符号位不同的位数。
     */
    public int bitCount() {
        @SuppressWarnings("deprecation") int bc = bitCount - 1;
        if (bc == -1) {  // bitCount 未初始化
            bc = 0;      // 偏移 1 以初始化
            // 计算幅度中的位数
            for (int i=0; i < mag.length; i++)
                bc += Integer.bitCount(mag[i]);
            if (signum < 0) {
                // 计算幅度中的尾随零数
                int magTrailingZeroCount = 0, j;
                for (j=mag.length-1; mag[j] == 0; j--)
                    magTrailingZeroCount += 32;
                magTrailingZeroCount += Integer.numberOfTrailingZeros(mag[j]);
                bc += magTrailingZeroCount - 1;
            }
            bitCount = bc + 1;
        }
        return bc;
    }

    // 素性测试

    /**
     * 如果此 BigInteger 可能是素数，则返回 {@code true}，如果它肯定是合数，则返回 {@code false}。如果
     * {@code certainty} 小于等于 0，则返回 {@code true}。
     *
     * @param  certainty 调用者愿意容忍的不确定性度量：如果调用返回 {@code true}
     *         则此 BigInteger 是素数的概率超过
     *         (1 - 1/2<sup>{@code certainty}</sup>)。此方法的执行时间与该参数的值成正比。
     * @return 如果此 BigInteger 可能是素数，则返回 {@code true}，
     *         如果它肯定是合数，则返回 {@code false}。
     */
    public boolean isProbablePrime(int certainty) {
        if (certainty <= 0)
            return true;
        BigInteger w = this.abs();
        if (w.equals(TWO))
            return true;
        if (!w.testBit(0) || w.equals(ONE))
            return false;

        return w.primeToCertainty(certainty, null);
    }

    // 比较操作

    /**
     * 将此 BigInteger 与指定的 BigInteger 进行比较。此方法优先于每个
     * 六个布尔比较运算符（{@literal <}, ==,
     * {@literal >}, {@literal >=}, !=, {@literal <=}）的单独方法。执行这些比较的建议用法是： {@code
     * (x.compareTo(y)} &lt;<i>op</i>&gt; {@code 0)}，其中
     * &lt;<i>op</i>&gt; 是六个比较运算符之一。
     *
     * @param  val 要与之比较的 BigInteger。
     * @return -1, 0 或 1，表示此 BigInteger 数值上小于、等于或大于 {@code val}。
     */
    public int compareTo(BigInteger val) {
        if (signum == val.signum) {
            switch (signum) {
            case 1:
                return compareMagnitude(val);
            case -1:
                return val.compareMagnitude(this);
            default:
                return 0;
            }
        }
        return signum > val.signum ? 1 : -1;
    }

    /**
     * 比较此 BigInteger 的幅度数组与指定的 BigInteger 的幅度数组。这是忽略符号的 compareTo 版本。
     *
     * @param val 要比较其幅度数组的 BigInteger。
     * @return -1, 0 或 1，表示此幅度数组小于、等于或大于指定 BigInteger 的幅度数组。
     */
    final int compareMagnitude(BigInteger val) {
        int[] m1 = mag;
        int len1 = m1.length;
        int[] m2 = val.mag;
        int len2 = m2.length;
        if (len1 < len2)
            return -1;
        if (len1 > len2)
            return 1;
        for (int i = 0; i < len1; i++) {
            int a = m1[i];
            int b = m2[i];
            if (a != b)
                return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
        }
        return 0;
    }

    /**
     * 比较幅度与 long 值的 compareMagnitude 版本。
     * val 不能是 Long.MIN_VALUE。
     */
    final int compareMagnitude(long val) {
        assert val != Long.MIN_VALUE;
        int[] m1 = mag;
        int len = m1.length;
        if (len > 2) {
            return 1;
        }
        if (val < 0) {
            val = -val;
        }
        int highWord = (int)(val >>> 32);
        if (highWord == 0) {
            if (len < 1)
                return -1;
            if (len > 1)
                return 1;
            int a = m1[0];
            int b = (int)val;
            if (a != b) {
                return ((a & LONG_MASK) < (b & LONG_MASK))? -1 : 1;
            }
            return 0;
        } else {
            if (len < 2)
                return -1;
            int a = m1[0];
            int b = highWord;
            if (a != b) {
                return ((a & LONG_MASK) < (b & LONG_MASK))? -1 : 1;
            }
            a = m1[1];
            b = (int)val;
            if (a != b) {
                return ((a & LONG_MASK) < (b & LONG_MASK))? -1 : 1;
            }
            return 0;
        }
    }

    /**
     * 将此 BigInteger 与指定的 Object 进行相等性比较。
     *
     * @param  x 要与之比较的 Object。
     * @return 如果且仅如果指定的 Object 是一个数值上等于此 BigInteger 的 BigInteger，则返回 {@code true}。
     */
    public boolean equals(Object x) {
        // 这个测试只是一个优化，可能或可能不会有所帮助
        if (x == this)
            return true;

        if (!(x instanceof BigInteger))
            return false;

        BigInteger xInt = (BigInteger) x;
        if (xInt.signum != signum)
            return false;

        int[] m = mag;
        int len = m.length;
        int[] xm = xInt.mag;
        if (len != xm.length)
            return false;

        for (int i = 0; i < len; i++)
            if (xm[i] != m[i])
                return false;

        return true;
    }

    /**
     * 返回此 BigInteger 和 {@code val} 中较小的一个。
     *
     * @param  val 要计算最小值的值。
     * @return 数值上较小的 BigInteger。如果它们相等，可以返回任何一个。
     */
    public BigInteger min(BigInteger val) {
        return (compareTo(val) < 0 ? this : val);
    }

    /**
     * 返回此 BigInteger 和 {@code val} 中较大的一个。
     *
     * @param  val 要计算最大值的值。
     * @return 数值上较大的 BigInteger。如果它们相等，可以返回任何一个。
     */
    public BigInteger max(BigInteger val) {
        return (compareTo(val) > 0 ? this : val);
    }


    // 哈希函数

    /**
     * 返回此 BigInteger 的哈希码。
     *
     * @return 此 BigInteger 的哈希码。
     */
    public int hashCode() {
        int hashCode = 0;

        for (int i=0; i < mag.length; i++)
            hashCode = (int)(31*hashCode + (mag[i] & LONG_MASK));

        return hashCode * signum;
    }

    /**
     * 返回此 BigInteger 在给定基数下的字符串表示。如果基数超出从 {@link
     * Character#MIN_RADIX} 到 {@link Character#MAX_RADIX} 的范围（包括两端），则默认为 10（如同
     * {@code Integer.toString} 的情况）。使用 {@code Character.forDigit} 提供的数字到字符的映射，并在适当的情况下前置一个负号。（此表示与
     * {@link #BigInteger(String, int) (String, int)} 构造函数兼容。）
     *
     * @param  radix  字符串表示的基数。
     * @return 此 BigInteger 在给定基数下的字符串表示。
     * @see    Integer#toString
     * @see    Character#forDigit
     * @see    #BigInteger(java.lang.String, int)
     */
    public String toString(int radix) {
        if (signum == 0)
            return "0";
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;

        // 如果足够小，使用 smallToString。
        if (mag.length <= SCHOENHAGE_BASE_CONVERSION_THRESHOLD)
           return smallToString(radix);

        // 否则使用递归 toString，需要正数参数。
        // 结果将被连接到这个 StringBuilder 中
        StringBuilder sb = new StringBuilder();
        if (signum < 0) {
            toString(this.negate(), sb, radix, 0);
            sb.insert(0, '-');
        }
        else
            toString(this, sb, radix, 0);

        return sb.toString();
    }

    /** 用于在参数较小时执行 toString 的方法。 */
    private String smallToString(int radix) {
        if (signum == 0) {
            return "0";
        }

        // 计算数字组的最大数量并分配空间
        int maxNumDigitGroups = (4*mag.length + 6)/7;
        String digitGroup[] = new String[maxNumDigitGroups];

        // 将数字转换为字符串，一次一个数字组
        BigInteger tmp = this.abs();
        int numGroups = 0;
        while (tmp.signum != 0) {
            BigInteger d = longRadix[radix];


                        MutableBigInteger q = new MutableBigInteger(),
                              a = new MutableBigInteger(tmp.mag),
                              b = new MutableBigInteger(d.mag);
            MutableBigInteger r = a.divide(b, q);
            BigInteger q2 = q.toBigInteger(tmp.signum * d.signum);
            BigInteger r2 = r.toBigInteger(tmp.signum * d.signum);

            digitGroup[numGroups++] = Long.toString(r2.longValue(), radix);
            tmp = q2;
        }

        // 将符号（如果有的话）和第一个数字组放入结果缓冲区
        StringBuilder buf = new StringBuilder(numGroups*digitsPerLong[radix]+1);
        if (signum < 0) {
            buf.append('-');
        }
        buf.append(digitGroup[numGroups-1]);

        // 用前导零填充剩余的数字组
        for (int i=numGroups-2; i >= 0; i--) {
            // 为当前数字组添加前导零
            int numLeadingZeros = digitsPerLong[radix]-digitGroup[i].length();
            if (numLeadingZeros != 0) {
                buf.append(zeros[numLeadingZeros]);
            }
            buf.append(digitGroup[i]);
        }
        return buf.toString();
    }

    /**
     * 将指定的 BigInteger 转换为字符串并追加到 {@code sb} 中。这实现了递归的 Schoenhage 算法
     * 用于基数转换。
     * <p/>
     * 参见 Knuth, Donald, 《计算机程序设计艺术》, 第 2 卷,
     * 习题答案 (4.4) 问题 14。
     *
     * @param u      要转换为字符串的数字。
     * @param sb     将在原地追加的 StringBuilder。
     * @param radix  要转换到的基数。
     * @param digits 要填充的最小数字位数。
     */
    private static void toString(BigInteger u, StringBuilder sb, int radix,
                                 int digits) {
        /* 如果小于某个阈值，使用 smallToString 方法，必要时用前导零填充。 */
        if (u.mag.length <= SCHOENHAGE_BASE_CONVERSION_THRESHOLD) {
            String s = u.smallToString(radix);

            // 必要时用内部零填充。
            // 如果在字符串的开头，不填充。
            if ((s.length() < digits) && (sb.length() > 0)) {
                for (int i=s.length(); i < digits; i++) { // 可能有更快的方法
                    sb.append('0');                    // 做这个？
                }
            }

            sb.append(s);
            return;
        }

        int b, n;
        b = u.bitLength();

        // 计算方程 radix^(2^n) = u 中 n 的值
        // 并从该值中减去 1。这用于找到包含最佳值来除以 u 的缓存索引。
        n = (int) Math.round(Math.log(b * LOG_TWO / logCache[radix]) / LOG_TWO - 1.0);
        BigInteger v = getRadixConversionCache(radix, n);
        BigInteger[] results;
        results = u.divideAndRemainder(v);

        int expectedDigits = 1 << n;

        // 现在递归构建每个数字的两个部分。
        toString(results[0], sb, radix, digits-expectedDigits);
        toString(results[1], sb, radix, expectedDigits);
    }

    /**
     * 从缓存中返回值 radix^(2^exponent)。如果该值尚未存在于缓存中，则添加它。
     * <p/>
     * 这可以改为使用 {@code Future} 的更复杂的缓存方法。
     */
    private static BigInteger getRadixConversionCache(int radix, int exponent) {
        BigInteger[] cacheLine = powerCache[radix]; // 挥发性读取
        if (exponent < cacheLine.length) {
            return cacheLine[exponent];
        }

        int oldLength = cacheLine.length;
        cacheLine = Arrays.copyOf(cacheLine, exponent + 1);
        for (int i = oldLength; i <= exponent; i++) {
            cacheLine[i] = cacheLine[i - 1].pow(2);
        }

        BigInteger[][] pc = powerCache; // 再次挥发性读取
        if (exponent >= pc[radix].length) {
            pc = pc.clone();
            pc[radix] = cacheLine;
            powerCache = pc; // 挥发性写入，发布
        }
        return cacheLine[exponent];
    }

    /* zero[i] 是 i 个连续的零。 */
    private static String zeros[] = new String[64];
    static {
        zeros[63] =
            "000000000000000000000000000000000000000000000000000000000000000";
        for (int i=0; i < 63; i++)
            zeros[i] = zeros[63].substring(0, i);
    }

    /**
     * 返回此 BigInteger 的十进制字符串表示形式。
     * 使用 {@code Character.forDigit} 提供的数字到字符的映射，
     * 如果适当，则在前面加上负号。此表示形式与
     * {@link #BigInteger(String) (String)} 构造函数兼容，
     * 并允许使用 Java 的 + 运算符进行字符串连接。
     *
     * @return 此 BigInteger 的十进制字符串表示形式。
     * @see    Character#forDigit
     * @see    #BigInteger(java.lang.String)
     */
    public String toString() {
        return toString(10);
    }

    /**
     * 返回包含此 BigInteger 的二进制补码表示形式的字节数组。字节数组将按
     * <i>大端</i> 字节顺序排列：最高有效字节在零元素中。数组将包含
     * 表示此 BigInteger 所需的最小字节数，包括至少一个符号位，
     * 即 {@code (ceil((this.bitLength() + 1)/8))}。此表示形式与
     * {@link #BigInteger(byte[]) (byte[])} 构造函数兼容。
     *
     * @return 包含此 BigInteger 的二进制补码表示形式的字节数组。
     * @see    #BigInteger(byte[])
     */
    public byte[] toByteArray() {
        int byteLen = bitLength()/8 + 1;
        byte[] byteArray = new byte[byteLen];

        for (int i=byteLen-1, bytesCopied=4, nextInt=0, intIndex=0; i >= 0; i--) {
            if (bytesCopied == 4) {
                nextInt = getInt(intIndex++);
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                bytesCopied++;
            }
            byteArray[i] = (byte)nextInt;
        }
        return byteArray;
    }

    /**
     * 将此 BigInteger 转换为 {@code int}。此转换类似于
     * 从 {@code long} 到 {@code int} 的
     * <i>窄化原始转换</i>，如《Java&trade; 语言规范》第 5.1.3 节所定义：
     * 如果此 BigInteger 太大而无法放入 {@code int} 中，仅返回最低 32 位。
     * 请注意，此转换可能会丢失关于 BigInteger 值总体大小的信息，以及返回带有相反符号的结果。
     *
     * @return 此 BigInteger 转换为的 {@code int}。
     * @see #intValueExact()
     */
    public int intValue() {
        int result = 0;
        result = getInt(0);
        return result;
    }

    /**
     * 将此 BigInteger 转换为 {@code long}。此转换类似于
     * 从 {@code long} 到 {@code int} 的
     * <i>窄化原始转换</i>，如《Java&trade; 语言规范》第 5.1.3 节所定义：
     * 如果此 BigInteger 太大而无法放入 {@code long} 中，仅返回最低 64 位。
     * 请注意，此转换可能会丢失关于 BigInteger 值总体大小的信息，以及返回带有相反符号的结果。
     *
     * @return 此 BigInteger 转换为的 {@code long}。
     * @see #longValueExact()
     */
    public long longValue() {
        long result = 0;

        for (int i=1; i >= 0; i--)
            result = (result << 32) + (getInt(i) & LONG_MASK);
        return result;
    }

    /**
     * 将此 BigInteger 转换为 {@code float}。此转换类似于
     * 从 {@code double} 到 {@code float} 的
     * <i>窄化原始转换</i>，如《Java&trade; 语言规范》第 5.1.3 节所定义：
     * 如果此 BigInteger 的大小太大而无法表示为 {@code float}，它将被转换为
     * {@link Float#NEGATIVE_INFINITY} 或 {@link
     * Float#POSITIVE_INFINITY}。请注意，即使返回值是有限的，此转换也可能丢失
     * 关于 BigInteger 值精度的信息。
     *
     * @return 此 BigInteger 转换为的 {@code float}。
     */
    public float floatValue() {
        if (signum == 0) {
            return 0.0f;
        }

        int exponent = ((mag.length - 1) << 5) + bitLengthForInt(mag[0]) - 1;

        // exponent == floor(log2(abs(this)))
        if (exponent < Long.SIZE - 1) {
            return longValue();
        } else if (exponent > Float.MAX_EXPONENT) {
            return signum > 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        }

        /*
         * 我们需要顶部 SIGNIFICAND_WIDTH 位，包括“隐含”的一位。为了便于舍入，我们提取顶部
         * SIGNIFICAND_WIDTH + 1 位，这样我们有一个位来帮助我们向上或向下舍入。twiceSignifFloor 将包含顶部
         * SIGNIFICAND_WIDTH + 1 位，而 signifFloor 将包含顶部 SIGNIFICAND_WIDTH 位。
         *
         * 考虑实数 signif = abs(this) * 2^(SIGNIFICAND_WIDTH - 1 - exponent) 会有所帮助。
         */
        int shift = exponent - FloatConsts.SIGNIFICAND_WIDTH;

        int twiceSignifFloor;
        // twiceSignifFloor 将等于 abs().shiftRight(shift).intValue()
        // 我们直接将移位操作转换为 int 以提高性能。

        int nBits = shift & 0x1f;
        int nBits2 = 32 - nBits;

        if (nBits == 0) {
            twiceSignifFloor = mag[0];
        } else {
            twiceSignifFloor = mag[0] >>> nBits;
            if (twiceSignifFloor == 0) {
                twiceSignifFloor = (mag[0] << nBits2) | (mag[1] >>> nBits);
            }
        }

        int signifFloor = twiceSignifFloor >> 1;
        signifFloor &= FloatConsts.SIGNIF_BIT_MASK; // 去除隐含位

        /*
         * 如果 signif 的小数部分严格大于 0.5（即 0.5 位设置且任何较低位设置），或者
         * signif 的小数部分 >= 0.5 且 signifFloor 是奇数（即 0.5 位和 1 位都设置），则我们向上舍入。
         * 这相当于所需的 HALF_EVEN 舍入。
         */
        boolean increment = (twiceSignifFloor & 1) != 0
                && ((signifFloor & 1) != 0 || abs().getLowestSetBit() < shift);
        int signifRounded = increment ? signifFloor + 1 : signifFloor;
        int bits = ((exponent + FloatConsts.EXP_BIAS))
                << (FloatConsts.SIGNIFICAND_WIDTH - 1);
        bits += signifRounded;
        /*
         * 如果 signifRounded == 2^24，我们需要将所有有效位设置为零并增加指数。这正是
         * 直接将 signifRounded 添加到 bits 中的行为。如果指数是 Float.MAX_EXPONENT，我们正确地向上舍入到
         * Float.POSITIVE_INFINITY。
         */
        bits |= signum & FloatConsts.SIGN_BIT_MASK;
        return Float.intBitsToFloat(bits);
    }

    /**
     * 将此 BigInteger 转换为 {@code double}。此转换类似于
     * 从 {@code double} 到 {@code float} 的
     * <i>窄化原始转换</i>，如《Java&trade; 语言规范》第 5.1.3 节所定义：
     * 如果此 BigInteger 的大小太大而无法表示为 {@code double}，它将被转换为
     * {@link Double#NEGATIVE_INFINITY} 或 {@link
     * Double#POSITIVE_INFINITY}。请注意，即使返回值是有限的，此转换也可能丢失
     * 关于 BigInteger 值精度的信息。
     *
     * @return 此 BigInteger 转换为的 {@code double}。
     */
    public double doubleValue() {
        if (signum == 0) {
            return 0.0;
        }

        int exponent = ((mag.length - 1) << 5) + bitLengthForInt(mag[0]) - 1;

        // exponent == floor(log2(abs(this))Double)
        if (exponent < Long.SIZE - 1) {
            return longValue();
        } else if (exponent > Double.MAX_EXPONENT) {
            return signum > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }

        /*
         * 我们需要顶部 SIGNIFICAND_WIDTH 位，包括“隐含”的一位。为了便于舍入，我们提取顶部
         * SIGNIFICAND_WIDTH + 1 位，这样我们有一个位来帮助我们向上或向下舍入。twiceSignifFloor 将包含顶部
         * SIGNIFICAND_WIDTH + 1 位，而 signifFloor 将包含顶部 SIGNIFICAND_WIDTH 位。
         *
         * 考虑实数 signif = abs(this) * 2^(SIGNIFICAND_WIDTH - 1 - exponent) 会有所帮助。
         */
        int shift = exponent - DoubleConsts.SIGNIFICAND_WIDTH;

        long twiceSignifFloor;
        // twiceSignifFloor 将等于 abs().shiftRight(shift).longValue()
        // 我们直接将移位操作转换为 long 以提高性能。

        int nBits = shift & 0x1f;
        int nBits2 = 32 - nBits;

        int highBits;
        int lowBits;
        if (nBits == 0) {
            highBits = mag[0];
            lowBits = mag[1];
        } else {
            highBits = mag[0] >>> nBits;
            lowBits = (mag[0] << nBits2) | (mag[1] >>> nBits);
            if (highBits == 0) {
                highBits = lowBits;
                lowBits = (mag[1] << nBits2) | (mag[2] >>> nBits);
            }
        }

        twiceSignifFloor = ((highBits & LONG_MASK) << 32)
                | (lowBits & LONG_MASK);

        long signifFloor = twiceSignifFloor >> 1;
        signifFloor &= DoubleConsts.SIGNIF_BIT_MASK; // 去除隐含位

        /*
         * 如果 signif 的小数部分严格大于 0.5（即 0.5 位设置且任何较低位设置），或者
         * signif 的小数部分 >= 0.5 且 signifFloor 是奇数（即 0.5 位和 1 位都设置），则我们向上舍入。
         * 这相当于所需的 HALF_EVEN 舍入。
         */
        boolean increment = (twiceSignifFloor & 1) != 0
                && ((signifFloor & 1) != 0 || abs().getLowestSetBit() < shift);
        long signifRounded = increment ? signifFloor + 1 : signifFloor;
        long bits = (long) ((exponent + DoubleConsts.EXP_BIAS))
                << (DoubleConsts.SIGNIFICAND_WIDTH - 1);
        bits += signifRounded;
        /*
         * 如果 signifRounded == 2^53，我们需要将所有有效位设置为零并增加指数。这正是
         * 直接将 signifRounded 添加到 bits 中的行为。如果指数是 Double.MAX_EXPONENT，我们正确地向上舍入到
         * Double.POSITIVE_INFINITY。
         */
        bits |= signum & DoubleConsts.SIGN_BIT_MASK;
        return Double.longBitsToDouble(bits);
    }


/**
 * 返回输入数组的副本，移除所有前导零字节。
 */
private static int[] stripLeadingZeroInts(int val[]) {
    int vlen = val.length;
    int keep;

    // 查找第一个非零字节
    for (keep = 0; keep < vlen && val[keep] == 0; keep++)
        ;
    return java.util.Arrays.copyOfRange(val, keep, vlen);
}

/**
 * 返回输入数组，移除所有前导零字节。
 * 由于源是可信的，可以跳过复制。
 */
private static int[] trustedStripLeadingZeroInts(int val[]) {
    int vlen = val.length;
    int keep;

    // 查找第一个非零字节
    for (keep = 0; keep < vlen && val[keep] == 0; keep++)
        ;
    return keep == 0 ? val : java.util.Arrays.copyOfRange(val, keep, vlen);
}

/**
 * 返回输入数组的副本，移除所有前导零字节。
 */
private static int[] stripLeadingZeroBytes(byte a[]) {
    int byteLength = a.length;
    int keep;

    // 查找第一个非零字节
    for (keep = 0; keep < byteLength && a[keep] == 0; keep++)
        ;

    // 分配新数组并复制输入数组的相关部分
    int intLength = ((byteLength - keep) + 3) >>> 2;
    int[] result = new int[intLength];
    int b = byteLength - 1;
    for (int i = intLength-1; i >= 0; i--) {
        result[i] = a[b--] & 0xff;
        int bytesRemaining = b - keep + 1;
        int bytesToTransfer = Math.min(3, bytesRemaining);
        for (int j=8; j <= (bytesToTransfer << 3); j += 8)
            result[i] |= ((a[b--] & 0xff) << j);
    }
    return result;
}

/**
 * 接受表示负二进制补码数的数组 a，并返回最小（无前导零字节）的无符号数，其值为 -a。
 */
private static int[] makePositive(byte a[]) {
    int keep, k;
    int byteLength = a.length;

    // 查找输入的第一个非符号（0xff）字节
    for (keep=0; keep < byteLength && a[keep] == -1; keep++)
        ;


    /* 分配输出数组。如果所有非符号字节都是 0x00，我们必须
     * 为一个额外的输出字节分配空间。 */
    for (k=keep; k < byteLength && a[k] == 0; k++)
        ;

    int extraByte = (k == byteLength) ? 1 : 0;
    int intLength = ((byteLength - keep + extraByte) + 3) >>> 2;
    int result[] = new int[intLength];

    /* 将输入的一补码复制到输出中，如果存在额外的
     * 字节（如果存在）== 0x00 */
    int b = byteLength - 1;
    for (int i = intLength-1; i >= 0; i--) {
        result[i] = a[b--] & 0xff;
        int numBytesToTransfer = Math.min(3, b-keep+1);
        if (numBytesToTransfer < 0)
            numBytesToTransfer = 0;
        for (int j=8; j <= 8*numBytesToTransfer; j += 8)
            result[i] |= ((a[b--] & 0xff) << j);

        // 标记表示必须取反的位
        int mask = -1 >>> (8*(3-numBytesToTransfer));
        result[i] = ~result[i] & mask;
    }

    // 加一到一补码以生成二补码
    for (int i=result.length-1; i >= 0; i--) {
        result[i] = (int)((result[i] & LONG_MASK) + 1);
        if (result[i] != 0)
            break;
    }

    return result;
}

/**
 * 接受表示负二进制补码数的数组 a，并返回最小（无前导零整数）的无符号数，其值为 -a。
 */
private static int[] makePositive(int a[]) {
    int keep, j;

    // 查找输入的第一个非符号（0xffffffff）整数
    for (keep=0; keep < a.length && a[keep] == -1; keep++)
        ;

    /* 分配输出数组。如果所有非符号整数都是 0x00，我们必须
     * 为一个额外的输出整数分配空间。 */
    for (j=keep; j < a.length && a[j] == 0; j++)
        ;
    int extraInt = (j == a.length ? 1 : 0);
    int result[] = new int[a.length - keep + extraInt];

    /* 将输入的一补码复制到输出中，如果存在额外的
     * 整数（如果存在）== 0x00 */
    for (int i = keep; i < a.length; i++)
        result[i - keep + extraInt] = ~a[i];

    // 加一到一补码以生成二补码
    for (int i=result.length-1; ++result[i] == 0; i--)
        ;

    return result;
}

/*
 * 以下两个数组用于快速字符串转换。两者都按基数索引。第一个是给定基数的数字，这些数字可以放入 Java 长整型中而不“变负”，即最高的整数 n 使得 radix**n < 2**63。第二个是“长基数”，它将每个数字拆分为“长数字”，每个长数字由 digitsPerLong 数组中相应元素的数字组成（longRadix[i] = i**digitPerLong[i]）。两个数组的 0 和 1 元素都是无意义的，因为基数 0 和 1 不使用。
 */
private static int digitsPerLong[] = {0, 0,
    62, 39, 31, 27, 24, 22, 20, 19, 18, 18, 17, 17, 16, 16, 15, 15, 15, 14,
    14, 14, 14, 13, 13, 13, 13, 13, 13, 12, 12, 12, 12, 12, 12, 12, 12};

private static BigInteger longRadix[] = {null, null,
    valueOf(0x4000000000000000L), valueOf(0x383d9170b85ff80bL),
    valueOf(0x4000000000000000L), valueOf(0x6765c793fa10079dL),
    valueOf(0x41c21cb8e1000000L), valueOf(0x3642798750226111L),
    valueOf(0x1000000000000000L), valueOf(0x12bf307ae81ffd59L),
    valueOf( 0xde0b6b3a7640000L), valueOf(0x4d28cb56c33fa539L),
    valueOf(0x1eca170c00000000L), valueOf(0x780c7372621bd74dL),
    valueOf(0x1e39a5057d810000L), valueOf(0x5b27ac993df97701L),
    valueOf(0x1000000000000000L), valueOf(0x27b95e997e21d9f1L),
    valueOf(0x5da0e1e53c5c8000L), valueOf( 0xb16a458ef403f19L),
    valueOf(0x16bcc41e90000000L), valueOf(0x2d04b7fdd9c0ef49L),
    valueOf(0x5658597bcaa24000L), valueOf( 0x6feb266931a75b7L),
    valueOf( 0xc29e98000000000L), valueOf(0x14adf4b7320334b9L),
    valueOf(0x226ed36478bfa000L), valueOf(0x383d9170b85ff80bL),
    valueOf(0x5a3c23e39c000000L), valueOf( 0x4e900abb53e6b71L),
    valueOf( 0x7600ec618141000L), valueOf( 0xaee5720ee830681L),
    valueOf(0x1000000000000000L), valueOf(0x172588ad4f5f0981L),
    valueOf(0x211e44f7d02c1000L), valueOf(0x2ee56725f06e5c71L),
    valueOf(0x41c21cb8e1000000L)};

/*
 * 这两个数组是上述数组的整数版本。
 */
private static int digitsPerInt[] = {0, 0, 30, 19, 15, 13, 11,
    11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6,
    6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5};

private static int intRadix[] = {0, 0,
    0x40000000, 0x4546b3db, 0x40000000, 0x48c27395, 0x159fd800,
    0x75db9c97, 0x40000000, 0x17179149, 0x3b9aca00, 0xcc6db61,
    0x19a10000, 0x309f1021, 0x57f6c100, 0xa2f1b6f,  0x10000000,
    0x18754571, 0x247dbc80, 0x3547667b, 0x4c4b4000, 0x6b5a6e1d,
    0x6c20a40,  0x8d2d931,  0xb640000,  0xe8d4a51,  0x1269ae40,
    0x17179149, 0x1cb91000, 0x23744899, 0x2b73a840, 0x34e63b41,
    0x40000000, 0x4cfa3cc1, 0x5c13d840, 0x6d91b519, 0x39aa400
};

/**
 * 这些例程提供了对 BigInteger 二进制补码表示的访问。
 */

/**
 * 返回二进制补码表示的长度（以整数为单位），包括至少一个符号位的空间。
 */
private int intLength() {
    return (bitLength() >>> 5) + 1;
}

/* 返回符号位 */
private int signBit() {
    return signum < 0 ? 1 : 0;
}

/* 返回一个符号位的整数 */
private int signInt() {
    return signum < 0 ? -1 : 0;
}

/**
 * 返回小端二进制补码表示中指定的整数（整数 0 是最低有效位）。整数编号可以任意高（值在逻辑上由无限多的符号整数前缀）。
 */
private int getInt(int n) {
    if (n < 0)
        return 0;
    if (n >= mag.length)
        return signInt();

    int magInt = mag[mag.length-n-1];

    return (signum >= 0 ? magInt :
            (n <= firstNonzeroIntNum() ? -magInt : ~magInt));
}

/**
 * 返回小端二进制表示中包含第一个非零整数的整数索引（整数 0 是最低有效位）。如果幅度为零，返回值未定义。
 */
private int firstNonzeroIntNum() {
    int fn = firstNonzeroIntNum - 2;
    if (fn == -2) { // firstNonzeroIntNum 尚未初始化
        fn = 0;

        // 查找第一个非零整数
        int i;
        int mlen = mag.length;
        for (i = mlen - 1; i >= 0 && mag[i] == 0; i--)
            ;
        fn = mlen - i - 1;
        firstNonzeroIntNum = fn + 2; // 偏移 2 以初始化
    }
    return fn;
}

/** 使用 JDK 1.1 的 serialVersionUID 以确保互操作性 */
private static final long serialVersionUID = -8287574255936472291L;

/**
 * BigInteger 的可序列化字段。
 *
 * @serialField signum  int
 *              此 BigInteger 的符号。
 * @serialField magnitude int[]
 *              此 BigInteger 的幅度数组。
 * @serialField bitCount  int
 *              此 BigInteger 的位数。
 * @serialField bitLength int
 *              此 BigInteger 的最小二进制补码表示的位数。
 * @serialField lowestSetBit int
 *              二进制补码表示中最低设置位。
 */
private static final ObjectStreamField[] serialPersistentFields = {
    new ObjectStreamField("signum", Integer.TYPE),
    new ObjectStreamField("magnitude", byte[].class),
    new ObjectStreamField("bitCount", Integer.TYPE),
    new ObjectStreamField("bitLength", Integer.TYPE),
    new ObjectStreamField("firstNonzeroByteNum", Integer.TYPE),
    new ObjectStreamField("lowestSetBit", Integer.TYPE)
};

/**
 * 从流中重新构建 {@code BigInteger} 实例（即反序列化它）。幅度以字节数组的形式读取，出于历史原因，但会转换为整数数组并丢弃字节数组。
 * 注意：
 * 当前的约定是将缓存字段 bitCount、bitLength 和 lowestSetBit 初始化为 0 而不是其他标记值。因此，不需要在 readObject 中显式设置这些字段，因为这些字段默认值为 0，因为没有使用 defaultReadObject。
 */
private void readObject(java.io.ObjectInputStream s)
    throws java.io.IOException, ClassNotFoundException {
    /*
     * 为了保持与以前的序列化形式的兼容性，BigInteger 的幅度以字节数组的形式序列化。
     * 幅度字段用作临时存储反序列化的字节数组。缓存计算字段应该是瞬态的，但为了兼容性原因而被序列化。
     */

    // 准备读取备用持久字段
    ObjectInputStream.GetField fields = s.readFields();

    // 读取并验证我们关心的备用持久字段，即 signum 和 magnitude

    // 读取并验证 signum
    int sign = fields.get("signum", -2);
    if (sign < -1 || sign > 1) {
        String message = "BigInteger: 无效的 signum 值";
        if (fields.defaulted("signum"))
            message = "BigInteger: 流中没有 signum";
        throw new java.io.StreamCorruptedException(message);
    }

    // 读取并验证 magnitude
    byte[] magnitude = (byte[])fields.get("magnitude", null);
    magnitude = magnitude.clone(); // 防御性复制
    int[] mag = stripLeadingZeroBytes(magnitude);
    if ((mag.length == 0) != (sign == 0)) {
        String message = "BigInteger: signum 和 magnitude 不匹配";
        if (fields.defaulted("magnitude"))
            message = "BigInteger: 流中没有 magnitude";
        throw new java.io.StreamCorruptedException(message);
    }

    // 等效于在 mag 本地变量上执行 checkRange() 而不分配 this.mag 字段
    if (mag.length > MAX_MAG_LENGTH ||
        (mag.length == MAX_MAG_LENGTH && mag[0] < 0)) {
        throw new java.io.StreamCorruptedException("BigInteger: 超出支持范围");
    }

    // 通过 Unsafe 提交最终字段
    UnsafeHolder.putSignAndMag(this, sign, mag);
}

/**
 * 该类不支持没有数据的序列化。
 */
private void readObjectNoData()
    throws ObjectStreamException {
    throw new InvalidObjectException("反序列化的 BigInteger 对象需要数据");
}

// 支持在反序列化时重置最终字段
private static class UnsafeHolder {
    private static final sun.misc.Unsafe unsafe;
    private static final long signumOffset;
    private static final long magOffset;
    static {
        try {
            unsafe = sun.misc.Unsafe.getUnsafe();
            signumOffset = unsafe.objectFieldOffset
                (BigInteger.class.getDeclaredField("signum"));
            magOffset = unsafe.objectFieldOffset
                (BigInteger.class.getDeclaredField("mag"));
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    static void putSignAndMag(BigInteger bi, int sign, int[] magnitude) {
        unsafe.putIntVolatile(bi, signumOffset, sign);
        unsafe.putObjectVolatile(bi, magOffset, magnitude);
    }
}

/**
 * 将 {@code BigInteger} 实例保存到流中。
 * 为了历史原因，BigInteger 的幅度以字节数组的形式序列化。
 *
 * @serialData 除了写入两个必要字段外，还写入旧版本兼容的过时字段。
 */
private void writeObject(ObjectOutputStream s) throws IOException {
    // 设置可序列化字段的值
    ObjectOutputStream.PutField fields = s.putFields();
    fields.put("signum", signum);
    fields.put("magnitude", magSerializedForm());
    // 缓存字段的写入值与旧版本兼容，但在 readObject 中被忽略，因此不重要。
    fields.put("bitCount", -1);
    fields.put("bitLength", -1);
    fields.put("lowestSetBit", -2);
    fields.put("firstNonzeroByteNum", -2);

    // 保存它们
    s.writeFields();
}

/**
 * 返回 mag 数组作为字节数组。
 */
private byte[] magSerializedForm() {
    int len = mag.length;

    int bitLen = (len == 0 ? 0 : ((len - 1) << 5) + bitLengthForInt(mag[0]));
    int byteLen = (bitLen + 7) >>> 3;
    byte[] result = new byte[byteLen];


        for (int i = byteLen - 1, bytesCopied = 4, intIndex = len - 1, nextInt = 0;
             i >= 0; i--) {
            if (bytesCopied == 4) {
                nextInt = mag[intIndex--];
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                bytesCopied++;
            }
            result[i] = (byte)nextInt;
        }
        return result;
    }

    /**
     * 将此 {@code BigInteger} 转换为 {@code long}，并检查是否有信息丢失。如果此 {@code BigInteger}
     * 的值超出了 {@code long} 类型的范围，则抛出 {@code ArithmeticException}。
     *
     * @return 此 {@code BigInteger} 转换为的 {@code long}。
     * @throws ArithmeticException 如果此 {@code BigInteger} 的值无法精确地转换为 {@code long}。
     * @see BigInteger#longValue
     * @since  1.8
     */
    public long longValueExact() {
        if (mag.length <= 2 && bitLength() <= 63)
            return longValue();
        else
            throw new ArithmeticException("BigInteger out of long range");
    }

    /**
     * 将此 {@code BigInteger} 转换为 {@code int}，并检查是否有信息丢失。如果此 {@code BigInteger}
     * 的值超出了 {@code int} 类型的范围，则抛出 {@code ArithmeticException}。
     *
     * @return 此 {@code BigInteger} 转换为的 {@code int}。
     * @throws ArithmeticException 如果此 {@code BigInteger} 的值无法精确地转换为 {@code int}。
     * @see BigInteger#intValue
     * @since  1.8
     */
    public int intValueExact() {
        if (mag.length <= 1 && bitLength() <= 31)
            return intValue();
        else
            throw new ArithmeticException("BigInteger out of int range");
    }

    /**
     * 将此 {@code BigInteger} 转换为 {@code short}，并检查是否有信息丢失。如果此 {@code BigInteger}
     * 的值超出了 {@code short} 类型的范围，则抛出 {@code ArithmeticException}。
     *
     * @return 此 {@code BigInteger} 转换为的 {@code short}。
     * @throws ArithmeticException 如果此 {@code BigInteger} 的值无法精确地转换为 {@code short}。
     * @see BigInteger#shortValue
     * @since  1.8
     */
    public short shortValueExact() {
        if (mag.length <= 1 && bitLength() <= 31) {
            int value = intValue();
            if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
                return shortValue();
        }
        throw new ArithmeticException("BigInteger out of short range");
    }

    /**
     * 将此 {@code BigInteger} 转换为 {@code byte}，并检查是否有信息丢失。如果此 {@code BigInteger}
     * 的值超出了 {@code byte} 类型的范围，则抛出 {@code ArithmeticException}。
     *
     * @return 此 {@code BigInteger} 转换为的 {@code byte}。
     * @throws ArithmeticException 如果此 {@code BigInteger} 的值无法精确地转换为 {@code byte}。
     * @see BigInteger#byteValue
     * @since  1.8
     */
    public byte byteValueExact() {
        if (mag.length <= 1 && bitLength() <= 31) {
            int value = intValue();
            if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
                return byteValue();
        }
        throw new ArithmeticException("BigInteger out of byte range");
    }
}
