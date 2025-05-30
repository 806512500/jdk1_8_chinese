
/*
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.text;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import sun.misc.FloatingDecimal;

/**
 * 数字列表。仅对 DecimalFormat 私有。
 * 处理数值和字符字符串之间的转换。仅处理非负数。DigitList 和
 * DecimalFormat 之间的分工是 DigitList 处理基数 10 的表示问题；
 * DecimalFormat 处理特定于区域性的问题，如正负号、分组、小数点、货币等。
 *
 * DigitList 实际上是一个浮点值的表示。
 * 它可以是一个整数值；我们假设 double 有足够的精度来表示 long 的所有数字。
 *
 * DigitList 的表示由一个字符字符串组成，这些字符是基数 10 的数字，从 '0' 到 '9'。
 * 它还有一个与之关联的基数 10 的指数。DigitList 对象表示的值可以通过将列表中的所有数字
 * 放在小数点右边得到的分数 f（其中 0 <= f < 1）乘以 10^exponent 来计算。
 *
 * @see  Locale
 * @see  Format
 * @see  NumberFormat
 * @see  DecimalFormat
 * @see  ChoiceFormat
 * @see  MessageFormat
 * @author       Mark Davis, Alan Liu
 */
final class DigitList implements Cloneable {
    /**
     * IEEE 754 double（即 Java double）的最大有效数字数。这个值不能增加，否则会产生垃圾数字，
     * 也不应减少，否则会损失精度。
     */
    public static final int MAX_COUNT = 19; // == Long.toString(Long.MAX_VALUE).length()

    /**
     * 这些数据成员是故意公开的，可以直接设置。
     *
     * 代表的值由在 digits[decimalAt] 前放置小数点给出。如果 decimalAt < 0，则在小数点和第一个非零数字之间
     * 暗示有前导零。如果 decimalAt > count，则在 digits[count-1] 和小数点之间暗示有尾随零。
     *
     * 等效地，表示的值由 f * 10^decimalAt 给出。这里 f 是一个 0.1 <= f < 1 的值，通过将 Digits 中的数字
     * 放在小数点右边得到。
     *
     * DigitList 是规范化的，因此如果它非零，则 digits[0] 非零。我们不允许非规范化数字，因为我们的指数实际上是
     * 无限大的。count 值包含 digits[] 中的有效数字数。
     *
     * 零由任何 count == 0 或所有 i <= count 的 digits[i] == '0' 的 DigitList 表示。
     */
    public int decimalAt = 0;
    public int count = 0;
    public char[] digits = new char[MAX_COUNT];

    private char[] data;
    private RoundingMode roundingMode = RoundingMode.HALF_EVEN;
    private boolean isNegative = false;

    /**
     * 如果表示的数字为零，则返回 true。
     */
    boolean isZero() {
        for (int i=0; i < count; ++i) {
            if (digits[i] != '0') {
                return false;
            }
        }
        return true;
    }

    /**
     * 设置舍入模式
     */
    void setRoundingMode(RoundingMode r) {
        roundingMode = r;
    }

    /**
     * 清除数字。
     * 在追加它们之前使用。
     * 通常，您使用 append 设置一系列数字，然后在遇到小数点时设置 myDigitList.decimalAt = myDigitList.count；
     * 然后继续追加数字。
     */
    public void clear () {
        decimalAt = 0;
        count = 0;
    }

    /**
     * 将数字追加到列表中，必要时扩展列表。
     */
    public void append(char digit) {
        if (count == digits.length) {
            char[] data = new char[count + 100];
            System.arraycopy(digits, 0, data, 0, count);
            digits = data;
        }
        digits[count++] = digit;
    }

    /**
     * 获取数字列表的值
     * 如果 (count == 0)，这将抛出 NumberFormatException，这模仿了 Long.parseLong()。
     */
    public final double getDouble() {
        if (count == 0) {
            return 0.0;
        }

        StringBuffer temp = getStringBuffer();
        temp.append('.');
        temp.append(digits, 0, count);
        temp.append('E');
        temp.append(decimalAt);
        return Double.parseDouble(temp.toString());
    }

    /**
     * 获取数字列表的值。
     * 如果 (count == 0)，这返回 0，与 Long.parseLong() 不同。
     */
    public final long getLong() {
        // 目前，简单的实现；以后，做适当的 IEEE 本机处理

        if (count == 0) {
            return 0;
        }

        // 我们必须检查这一点，因为这是唯一一个我们表示的负值。如果我们将数字传递给 parseLong，
        // 我们会得到解析失败。
        if (isLongMIN_VALUE()) {
            return Long.MIN_VALUE;
        }

        StringBuffer temp = getStringBuffer();
        temp.append(digits, 0, count);
        for (int i = count; i < decimalAt; ++i) {
            temp.append('0');
        }
        return Long.parseLong(temp.toString());
    }

    public final BigDecimal getBigDecimal() {
        if (count == 0) {
            if (decimalAt == 0) {
                return BigDecimal.ZERO;
            } else {
                return new BigDecimal("0E" + decimalAt);
            }
        }

       if (decimalAt == count) {
           return new BigDecimal(digits, 0, count);
       } else {
           return new BigDecimal(digits, 0, count).scaleByPowerOfTen(decimalAt - count);
       }
    }

    /**
     * 如果此对象表示的数字可以放入 long 中，则返回 true。
     * @param isPositive 如果此数字应被视为正数，则为 true
     * @param ignoreNegativeZero 如果 -0 应被视为与 +0 相同，则为 true；否则它们被视为不同
     * @return 如果此数字可以放入 Java long 中，则为 true
     */
    boolean fitsIntoLong(boolean isPositive, boolean ignoreNegativeZero) {
        // 确定结果是否可以放入 long 中。我们首先查找小数点后的非零数字；
        // 然后检查大小。如果数字计数为 18 或更少，则值肯定可以表示为 long。如果为 19
        // 则可能太大。

        // 去除尾随零。这不会改变表示的值。
        while (count > 0 && digits[count - 1] == '0') {
            --count;
        }

        if (count == 0) {
            // 正零可以放入 long 中，但负零只能表示为 double。- bug 4162852
            return isPositive || ignoreNegativeZero;
        }

        if (decimalAt < count || decimalAt > MAX_COUNT) {
            return false;
        }

        if (decimalAt < MAX_COUNT) return true;

        // 此时我们有 decimalAt == count，且 count == MAX_COUNT。
        // 如果数字大于 9223372036854775807 或小于 -9223372036854775808，则会溢出。
        for (int i=0; i<count; ++i) {
            char dig = digits[i], max = LONG_MIN_REP[i];
            if (dig > max) return false;
            if (dig < max) return true;
        }

        // 此时前 count 位匹配。如果 decimalAt 小于 count，则剩余的数字为零，我们返回 true。
        if (count < decimalAt) return true;

        // 现在我们有一个 Long.MIN_VALUE 的表示，但没有前导负号。如果这表示一个正数，则它
        // 不适合；否则它适合。
        return !isPositive;
    }

    /**
     * 将数字列表设置为给定 double 值的表示。
     * 此方法支持定点表示。
     * @param isNegative 表示数字是否为负的布尔值。
     * @param source 要转换的值；必须不是 Inf, -Inf, Nan，或 <= 0 的值。
     * @param maximumFractionDigits 应转换的最大小数位数。
     */
    final void set(boolean isNegative, double source, int maximumFractionDigits) {
        set(isNegative, source, maximumFractionDigits, true);
    }

    /**
     * 将数字列表设置为给定 double 值的表示。
     * 此方法支持定点和指数表示。
     * @param isNegative 表示数字是否为负的布尔值。
     * @param source 要转换的值；必须不是 Inf, -Inf, Nan，或 <= 0 的值。
     * @param maximumDigits 应转换的最大小数位数或总位数。
     * @param fixedPoint 如果为 true，则 maximumDigits 是要转换的最大小数位数。如果为 false，则为总位数。
     */
    final void set(boolean isNegative, double source, int maximumDigits, boolean fixedPoint) {

        FloatingDecimal.BinaryToASCIIConverter fdConverter  = FloatingDecimal.getBinaryToASCIIConverter(source);
        boolean hasBeenRoundedUp = fdConverter.digitsRoundedUp();
        boolean valueExactAsDecimal = fdConverter.decimalDigitsExact();
        assert !fdConverter.isExceptional();
        String digitsString = fdConverter.toJavaFormatString();

        set(isNegative, digitsString,
            hasBeenRoundedUp, valueExactAsDecimal,
            maximumDigits, fixedPoint);
    }

    /**
     * 生成 DDDDD, DDDDD.DDDDD, 或 DDDDDE+/-DDDDD 形式的表示。
     * @param roundedUp 是否已经发生了向上舍入。
     * @param valueExactAsDecimal 收集的数字是否提供了值的精确十进制表示。
     */
    private void set(boolean isNegative, String s,
                     boolean roundedUp, boolean valueExactAsDecimal,
                     int maximumDigits, boolean fixedPoint) {

        this.isNegative = isNegative;
        int len = s.length();
        char[] source = getDataChars(len);
        s.getChars(0, len, source, 0);

        decimalAt = -1;
        count = 0;
        int exponent = 0;
        // 小于 1 的数字在小数点后第一个非零数字前的零的数量。
        int leadingZerosAfterDecimal = 0;
        boolean nonZeroDigitSeen = false;

        for (int i = 0; i < len; ) {
            char c = source[i++];
            if (c == '.') {
                decimalAt = count;
            } else if (c == 'e' || c == 'E') {
                exponent = parseInt(source, i, len);
                break;
            } else {
                if (!nonZeroDigitSeen) {
                    nonZeroDigitSeen = (c != '0');
                    if (!nonZeroDigitSeen && decimalAt != -1)
                        ++leadingZerosAfterDecimal;
                }
                if (nonZeroDigitSeen) {
                    digits[count++] = c;
                }
            }
        }
        if (decimalAt == -1) {
            decimalAt = count;
        }
        if (nonZeroDigitSeen) {
            decimalAt += exponent - leadingZerosAfterDecimal;
        }

        if (fixedPoint) {
            // 负指数表示小数点和第一个非零数字之间的零的数量，对于值 < 0.1（例如，对于 0.00123，-decimalAt == 2）。
            // 如果这大于最大小数位数，则打印表示将发生下溢。
            if (-decimalAt > maximumDigits) {
                // 当我们将类似 0.0009 的值舍入到 2 位小数时，处理下溢到零。
                count = 0;
                return;
            } else if (-decimalAt == maximumDigits) {
                // 如果我们将 0.0009 舍入到 3 位小数，则我们必须在最低有效位置创建一个新的数字。
                if (shouldRoundUp(0, roundedUp, valueExactAsDecimal)) {
                    count = 1;
                    ++decimalAt;
                    digits[0] = '1';
                } else {
                    count = 0;
                }
                return;
            }
            // 否则继续
        }

        // 去除尾随零。
        while (count > 1 && digits[count - 1] == '0') {
            --count;
        }

        // 去除要显示的最大位数之外的数字。
        // 如果适当，向上舍入。
        round(fixedPoint ? (maximumDigits + decimalAt) : maximumDigits,
              roundedUp, valueExactAsDecimal);

     }

    /**
     * 将表示舍入到给定位数。
     * @param maximumDigits 要显示的最大位数。
     * @param alreadyRounded 是否已经发生了向上舍入。
     * @param valueExactAsDecimal 收集的数字是否提供了值的精确十进制表示。
     *
     * 返回时，count 将小于或等于 maximumDigits。
     */
    private final void round(int maximumDigits,
                             boolean alreadyRounded,
                             boolean valueExactAsDecimal) {
        // 去除要显示的最大位数之外的数字。
        // 如果适当，向上舍入。
        if (maximumDigits >= 0 && maximumDigits < count) {
            if (shouldRoundUp(maximumDigits, alreadyRounded, valueExactAsDecimal)) {
                // 向上舍入涉及从最低有效位到最高有效位递增数字。
                // 在大多数情况下这是简单的，但在最坏情况下（9999..99）我们必须调整 decimalAt 值。
                for (;;) {
                    --maximumDigits;
                    if (maximumDigits < 0) {
                        // 我们有全 9，所以我们递增到一个单个的 1 位数并调整指数。
                        digits[0] = '1';
                        ++decimalAt;
                        maximumDigits = 0; // 调整 count
                        break;
                    }


                                ++digits[maximumDigits];
                    if (digits[maximumDigits] <= '9') break;
                    // digits[maximumDigits] = '0'; // 不必要，因为我们将会截断这个
                }
                ++maximumDigits; // 递增用于计数
            }
            count = maximumDigits;

            // 消除尾随零。
            while (count > 1 && digits[count-1] == '0') {
                --count;
            }
        }
    }


    /**
     * 如果将表示截断到给定的数字数量会导致最后一个数字的递增，则返回 true。此方法实现了
     * java.math.RoundingMode 类中定义的舍入模式。
     * [bnf]
     * @param maximumDigits 要保留的数字数量，从 0 到
     * <code>count-1</code>。如果为 0，则所有数字都被舍入，如果应该生成一个 1（例如，格式化
     * 0.09 为 "#.#"），则此方法返回 true。
     * @param alreadyRounded 是否已经发生了向上舍入。
     * @param valueExactAsDecimal 收集的数字是否提供了值的精确十进制表示。
     * @exception ArithmeticException 如果需要舍入而舍入模式设置为 RoundingMode.UNNECESSARY
     * @return 如果数字 <code>maximumDigits-1</code> 应该递增，则返回 true
     */
    private boolean shouldRoundUp(int maximumDigits,
                                  boolean alreadyRounded,
                                  boolean valueExactAsDecimal) {
        if (maximumDigits < count) {
            /*
             * 为了避免在将二进制 double 值转换为文本时出现错误的双重舍入或截断，需要
             * 了解 FloatingDecimal 中转换结果的精确性以及任何已进行的舍入。
             *
             * - 对于以下的 HALF_DOWN, HALF_EVEN, HALF_UP 舍入规则：
             *   在格式化 float 或 double 时，我们必须考虑 FloatingDecimal 在二进制到十进制
             *   转换中所做的处理。
             *
             *   考虑到平局情况，FloatingDecimal 可能会向上舍入值（返回低于平局时的十进制数字），
             *   或将值“截断”到平局而值高于它，或者在给定 FloatingDecimal 的格式化规则下，
             *   当二进制值可以精确转换为其十进制表示时提供精确的十进制数字（因此我们有一个
             *   二进制值的精确十进制表示）。
             *
             *   - 如果二进制值被精确地转换为十进制值，则 DigitList 代码必须应用预期的舍入规则。
             *
             *   - 如果 FloatingDecimal 已经向上舍入了十进制值，则 DigitList 不应在上述三种
             *     舍入模式中的任何一种再次向上舍入值。
             *
             *   - 如果 FloatingDecimal 将十进制值截断到以 '5' 结尾的数字，DigitList 应在上述
             *     三种舍入模式中的所有情况下向上舍入值。
             *
             *
             *   只有当最大数字索引处的数字恰好是数字集中的最后一个时，才需要考虑这一点，否则该位置
             *   之后还有剩余的数字，我们不需要考虑 FloatingDecimal 做了什么。
             *
             * - 其他舍入模式不受这些平局情况的影响。
             *
             * - 对于总是转换为精确数字的其他数字（如 BigInteger, Long, ...），传递的 alreadyRounded
             *   布尔值必须设置为 false，而 valueExactAsDecimal 必须在上层 DigitList 调用堆栈中设置为
             *   true，为这些情况提供正确的状态。
             */

            switch(roundingMode) {
            case UP:
                for (int i=maximumDigits; i<count; ++i) {
                    if (digits[i] != '0') {
                        return true;
                    }
                }
                break;
            case DOWN:
                break;
            case CEILING:
                for (int i=maximumDigits; i<count; ++i) {
                    if (digits[i] != '0') {
                        return !isNegative;
                    }
                }
                break;
            case FLOOR:
                for (int i=maximumDigits; i<count; ++i) {
                    if (digits[i] != '0') {
                        return isNegative;
                    }
                }
                break;
            case HALF_UP:
            case HALF_DOWN:
                if (digits[maximumDigits] > '5') {
                    // 值高于平局 ==> 必须向上舍入
                    return true;
                } else if (digits[maximumDigits] == '5') {
                    // 舍入位置的数字是 '5'。平局情况。
                    if (maximumDigits != (count - 1)) {
                        // 还有剩余的数字。高于平局 => 必须向上舍入
                        return true;
                    } else {
                        // 舍入位置的数字是最后一个！
                        if (valueExactAsDecimal) {
                            // 精确的二进制表示。处于平局。
                            // 根据舍入模式应用舍入。
                            return roundingMode == RoundingMode.HALF_UP;
                        } else {
                            // 不是精确的二进制表示。
                            // 数字序列要么向上舍入，要么被截断。
                            // 只有在被截断的情况下才向上舍入。
                            return !alreadyRounded;
                        }
                    }
                }
                // 舍入位置的数字 < '5' ==> 不向上舍入。
                // 只需执行默认操作，即不向上舍入（因此 break）。
                break;
            case HALF_EVEN:
                // 实现 IEEE 半偶舍入
                if (digits[maximumDigits] > '5') {
                    return true;
                } else if (digits[maximumDigits] == '5' ) {
                    if (maximumDigits == (count - 1)) {
                        // 舍入位置恰好是最后一个索引：
                        if (alreadyRounded)
                            // 如果 FloatingDecimal 向上舍入（值低于平局），
                            // 则我们不应再次向上舍入。
                            return false;

                        if (!valueExactAsDecimal)
                            // 否则，如果数字不表示精确值，
                            // 值高于平局，FloatingDecimal 截断了数字到平局。我们必须向上舍入。
                            return true;
                        else {
                            // 这是一个精确的平局值，FloatingDecimal
                            // 提供了所有精确的数字。因此我们应用
                            // 半偶舍入规则。
                            return ((maximumDigits > 0) &&
                                    (digits[maximumDigits-1] % 2 != 0));
                        }
                    } else {
                        // 如果在 '5' 之后有非零数字则向上舍入
                        for (int i=maximumDigits+1; i<count; ++i) {
                            if (digits[i] != '0')
                                return true;
                        }
                    }
                }
                break;
            case UNNECESSARY:
                for (int i=maximumDigits; i<count; ++i) {
                    if (digits[i] != '0') {
                        throw new ArithmeticException(
                            "舍入模式设置为 RoundingMode.UNNECESSARY 时需要舍入");
                    }
                }
                break;
            default:
                assert false;
            }
        }
        return false;
    }

    /**
     * 用于从 long 设置数字列表值的实用程序例程
     */
    final void set(boolean isNegative, long source) {
        set(isNegative, source, 0);
    }

    /**
     * 将数字列表设置为给定 long 值的表示。
     * @param isNegative 布尔值，指示数字是否为负。
     * @param source 要转换的值；必须 >= 0 或 ==
     * Long.MIN_VALUE。
     * @param maximumDigits 应转换的最大数字。如果 maximumDigits 低于 source 中的
     * 显著数字数量，表示将被舍入。如果 <= 0，则忽略。
     */
    final void set(boolean isNegative, long source, int maximumDigits) {
        this.isNegative = isNegative;

        // 此方法不期望负数。但是，
        // "source" 可以是 Long.MIN_VALUE (-9223372036854775808)，
        // 如果被格式化的数字是 Long.MIN_VALUE。在这种情况下，它将被格式化为 -Long.MIN_VALUE，
        // 这是一个超出 long 合法范围的数字，但可以由 DigitList 表示。
        if (source <= 0) {
            if (source == Long.MIN_VALUE) {
                decimalAt = count = MAX_COUNT;
                System.arraycopy(LONG_MIN_REP, 0, digits, 0, count);
            } else {
                decimalAt = count = 0; // 值 <= 0 格式化为零
            }
        } else {
            // 重写以提高性能。我以前调用
            // Long.toString()，这比这段代码慢约 4 倍。
            int left = MAX_COUNT;
            int right;
            while (source > 0) {
                digits[--left] = (char)('0' + (source % 10));
                source /= 10;
            }
            decimalAt = MAX_COUNT - left;
            // 不复制尾随零。我们保证至少有一个非零数字，因此不需要检查下限。
            for (right = MAX_COUNT - 1; digits[right] == '0'; --right)
                ;
            count = right - left + 1;
            System.arraycopy(digits, left, digits, 0, count);
        }
        if (maximumDigits > 0) round(maximumDigits, false, true);
    }

    /**
     * 将数字列表设置为给定 BigDecimal 值的表示。
     * 此方法支持定点和科学记数法。
     * @param isNegative 布尔值，指示数字是否为负。
     * @param source 要转换的值；必须不是 <= 0 的值。
     * @param maximumDigits 应转换的最大小数或总数字。
     * @param fixedPoint 如果为 true，则 maximumDigits 是要转换的最大小数位数。如果为 false，则为总位数。
     */
    final void set(boolean isNegative, BigDecimal source, int maximumDigits, boolean fixedPoint) {
        String s = source.toString();
        extendDigits(s.length());

        set(isNegative, s,
            false, true,
            maximumDigits, fixedPoint);
    }

    /**
     * 将数字列表设置为给定 BigInteger 值的表示。
     * @param isNegative 布尔值，指示数字是否为负。
     * @param source 要转换的值；必须 >= 0。
     * @param maximumDigits 应转换的最大数字。如果 maximumDigits 低于 source 中的
     * 显著数字数量，表示将被舍入。如果 <= 0，则忽略。
     */
    final void set(boolean isNegative, BigInteger source, int maximumDigits) {
        this.isNegative = isNegative;
        String s = source.toString();
        int len = s.length();
        extendDigits(len);
        s.getChars(0, len, digits, 0);

        decimalAt = len;
        int right;
        for (right = len - 1; right >= 0 && digits[right] == '0'; --right)
            ;
        count = right + 1;

        if (maximumDigits > 0) {
            round(maximumDigits, false, true);
        }
    }

    /**
     * 两个数字列表之间的相等性测试。
     */
    public boolean equals(Object obj) {
        if (this == obj)                      // 快速检查
            return true;
        if (!(obj instanceof DigitList))         // (1) 同一对象？
            return false;
        DigitList other = (DigitList) obj;
        if (count != other.count ||
        decimalAt != other.decimalAt)
            return false;
        for (int i = 0; i < count; i++)
            if (digits[i] != other.digits[i])
                return false;
        return true;
    }

    /**
     * 为数字列表生成哈希码。
     */
    public int hashCode() {
        int hashcode = decimalAt;

        for (int i = 0; i < count; i++) {
            hashcode = hashcode * 37 + digits[i];
        }

        return hashcode;
    }

    /**
     * 创建此对象的副本。
     * @return 此实例的克隆。
     */
    public Object clone() {
        try {
            DigitList other = (DigitList) super.clone();
            char[] newDigits = new char[digits.length];
            System.arraycopy(digits, 0, newDigits, 0, digits.length);
            other.digits = newDigits;
            other.tempBuffer = null;
            return other;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 如果此 DigitList 表示 Long.MIN_VALUE，则返回 true；
     * 否则返回 false。这是必需的，以便 getLong() 能够工作。
     */
    private boolean isLongMIN_VALUE() {
        if (decimalAt != count || count != MAX_COUNT) {
            return false;
        }

        for (int i = 0; i < count; ++i) {
            if (digits[i] != LONG_MIN_REP[i]) return false;
        }

        return true;
    }

    private static final int parseInt(char[] str, int offset, int strLen) {
        char c;
        boolean positive = true;
        if ((c = str[offset]) == '-') {
            positive = false;
            offset++;
        } else if (c == '+') {
            offset++;
        }

        int value = 0;
        while (offset < strLen) {
            c = str[offset++];
            if (c >= '0' && c <= '9') {
                value = value * 10 + (c - '0');
            } else {
                break;
            }
        }
        return positive ? value : -value;
    }

    // -9223372036854775808L 的数字部分
    private static final char[] LONG_MIN_REP = "9223372036854775808".toCharArray();

    public String toString() {
        if (isZero()) {
            return "0";
        }
        StringBuffer buf = getStringBuffer();
        buf.append("0.");
        buf.append(digits, 0, count);
        buf.append("x10^");
        buf.append(decimalAt);
        return buf.toString();
    }

    private StringBuffer tempBuffer;


                private StringBuffer getStringBuffer() {
        if (tempBuffer == null) {
            tempBuffer = new StringBuffer(MAX_COUNT);
        } else {
            tempBuffer.setLength(0);
        }
        return tempBuffer;
    }

    private void extendDigits(int len) {
        if (len > digits.length) {
            digits = new char[len];
        }
    }

    private final char[] getDataChars(int length) {
        if (data == null || data.length < length) {
            data = new char[length];
        }
        return data;
    }
}
