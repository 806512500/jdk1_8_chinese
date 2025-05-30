/*
 * Copyright (c) 2003, 2007, Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyright IBM Corporation, 1997, 2001. All Rights Reserved.
 */

package java.math;
import java.io.*;

/**
 * 不可变对象，封装描述某些数值运算规则的上下文设置，例如由 {@link BigDecimal} 类实现的运算。
 *
 * <p>与基数无关的设置包括：
 * <ol>
 * <li>{@code precision}：
 * 运算中使用的数字位数；结果将被舍入到此精度。
 *
 * <li>{@code roundingMode}：
 * 一个 {@link RoundingMode} 对象，指定要使用的舍入算法。
 * </ol>
 *
 * @see     BigDecimal
 * @see     RoundingMode
 * @author  Mike Cowlishaw
 * @author  Joseph D. Darcy
 * @since 1.5
 */

public final class MathContext implements Serializable {

    /* ----- 常量 ----- */

    // 构造函数的默认值
    private static final int DEFAULT_DIGITS = 9;
    private static final RoundingMode DEFAULT_ROUNDINGMODE = RoundingMode.HALF_UP;
    // 数字的最小值（最大值为 Integer.MAX_VALUE）
    private static final int MIN_DIGITS = 0;

    // 序列化版本
    private static final long serialVersionUID = 5579720004786848255L;

    /* ----- 公有属性 ----- */
    /**
     * 一个 {@code MathContext} 对象，其设置值适用于无限精度算术。
     * 设置值为：
     * <code>
     * precision=0 roundingMode=HALF_UP
     * </code>
     */
    public static final MathContext UNLIMITED =
        new MathContext(0, RoundingMode.HALF_UP);

    /**
     * 一个 {@code MathContext} 对象，其精度设置与 IEEE 754R Decimal32 格式匹配，7 位，舍入模式为 {@link RoundingMode#HALF_EVEN HALF_EVEN}，即 IEEE 754R 默认值。
     */
    public static final MathContext DECIMAL32 =
        new MathContext(7, RoundingMode.HALF_EVEN);

    /**
     * 一个 {@code MathContext} 对象，其精度设置与 IEEE 754R Decimal64 格式匹配，16 位，舍入模式为 {@link RoundingMode#HALF_EVEN HALF_EVEN}，即 IEEE 754R 默认值。
     */
    public static final MathContext DECIMAL64 =
        new MathContext(16, RoundingMode.HALF_EVEN);

    /**
     * 一个 {@code MathContext} 对象，其精度设置与 IEEE 754R Decimal128 格式匹配，34 位，舍入模式为 {@link RoundingMode#HALF_EVEN HALF_EVEN}，即 IEEE 754R 默认值。
     */
    public static final MathContext DECIMAL128 =
        new MathContext(34, RoundingMode.HALF_EVEN);

    /* ----- 共享属性 ----- */
    /**
     * 运算中使用的数字位数。值为 0 表示将使用无限精度（尽可能多的位数）。注意，前导零（在数字的系数中）永远不是有效的。
     *
     * <p>{@code precision} 始终为非负数。
     *
     * @serial
     */
    final int precision;

    /**
     * 运算中使用的舍入算法。
     *
     * @see RoundingMode
     * @serial
     */
    final RoundingMode roundingMode;

    /* ----- 构造函数 ----- */

    /**
     * 构造一个具有指定精度和 {@link RoundingMode#HALF_UP HALF_UP} 舍入模式的新 {@code MathContext}。
     *
     * @param setPrecision 非负的 {@code int} 精度设置。
     * @throws IllegalArgumentException 如果 {@code setPrecision} 参数小于零。
     */
    public MathContext(int setPrecision) {
        this(setPrecision, DEFAULT_ROUNDINGMODE);
        return;
    }

    /**
     * 构造一个具有指定精度和舍入模式的新 {@code MathContext}。
     *
     * @param setPrecision 非负的 {@code int} 精度设置。
     * @param setRoundingMode 要使用的舍入模式。
     * @throws IllegalArgumentException 如果 {@code setPrecision} 参数小于零。
     * @throws NullPointerException 如果舍入模式参数为 {@code null}
     */
    public MathContext(int setPrecision,
                       RoundingMode setRoundingMode) {
        if (setPrecision < MIN_DIGITS)
            throw new IllegalArgumentException("Digits < 0");
        if (setRoundingMode == null)
            throw new NullPointerException("null RoundingMode");

        precision = setPrecision;
        roundingMode = setRoundingMode;
        return;
    }

    /**
     * 从字符串构造一个新的 {@code MathContext}。
     *
     * 字符串必须与 {@link #toString} 方法生成的格式相同。
     *
     * <p>如果字符串中的精度部分超出范围（{@code < 0}）或字符串格式不正确，则抛出 {@code IllegalArgumentException}。
     *
     * @param val 要解析的字符串
     * @throws IllegalArgumentException 如果精度部分超出范围或格式不正确
     * @throws NullPointerException 如果参数为 {@code null}
     */
    public MathContext(String val) {
        boolean bad = false;
        int setPrecision;
        if (val == null)
            throw new NullPointerException("null String");
        try { // 任何错误都表示字符串格式问题
            if (!val.startsWith("precision=")) throw new RuntimeException();
            int fence = val.indexOf(' ');    // 可能为 -1
            int off = 10;                     // 值开始的位置
            setPrecision = Integer.parseInt(val.substring(10, fence));

            if (!val.startsWith("roundingMode=", fence+1))
                throw new RuntimeException();
            off = fence + 1 + 13;
            String str = val.substring(off, val.length());
            roundingMode = RoundingMode.valueOf(str);
        } catch (RuntimeException re) {
            throw new IllegalArgumentException("bad string format");
        }

        if (setPrecision < MIN_DIGITS)
            throw new IllegalArgumentException("Digits < 0");
        // 如果我们到达这里，其他参数不可能无效
        precision = setPrecision;
    }

    /**
     * 返回 {@code precision} 设置。
     * 此值始终为非负数。
     *
     * @return 一个 {@code int}，表示 {@code precision} 设置的值
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * 返回 {@code roundingMode} 设置。
     * 这将是以下之一：
     * {@link  RoundingMode#CEILING},
     * {@link  RoundingMode#DOWN},
     * {@link  RoundingMode#FLOOR},
     * {@link  RoundingMode#HALF_DOWN},
     * {@link  RoundingMode#HALF_EVEN},
     * {@link  RoundingMode#HALF_UP},
     * {@link  RoundingMode#UNNECESSARY}, 或
     * {@link  RoundingMode#UP}.
     *
     * @return 一个 {@code RoundingMode} 对象，表示 {@code roundingMode} 设置的值
     */

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    /**
     * 比较此 {@code MathContext} 与指定的 {@code Object} 是否相等。
     *
     * @param  x 要与之比较的 {@code Object}
     * @return 如果且仅当指定的 {@code Object} 是一个具有与该对象完全相同设置的 {@code MathContext} 对象，则返回 {@code true}
     */
    public boolean equals(Object x){
        MathContext mc;
        if (!(x instanceof MathContext))
            return false;
        mc = (MathContext) x;
        return mc.precision == this.precision
            && mc.roundingMode == this.roundingMode; // 无需 .equals()
    }

    /**
     * 返回此 {@code MathContext} 的哈希码。
     *
     * @return 此 {@code MathContext} 的哈希码
     */
    public int hashCode() {
        return this.precision + roundingMode.hashCode() * 59;
    }

    /**
     * 返回此 {@code MathContext} 的字符串表示形式。
     * 返回的字符串表示 {@code MathContext} 对象的设置，格式为两个由空格分隔的单词（以单个空格字符 <tt>'&#92;u0020'</tt> 分隔，且没有前导或尾随空格），如下：
     * <ol>
     * <li>
     * 字符串 {@code "precision="}，紧接着是精度设置的值，形式为数字字符串，如同由 {@link Integer#toString(int) Integer.toString} 方法生成。
     *
     * <li>
     * 字符串 {@code "roundingMode="}，紧接着是 {@code roundingMode} 设置的值，形式为单词。此单词将与 {@link RoundingMode} 枚举中相应的公共常量名称相同。
     * </ol>
     * <p>
     * 例如：
     * <pre>
     * precision=9 roundingMode=HALF_UP
     * </pre>
     *
     * 如果将来为该类添加更多属性，可能会在 {@code toString} 的结果中附加更多单词。
     *
     * @return 一个表示上下文设置的 {@code String}
     */
    public java.lang.String toString() {
        return "precision=" +           precision + " " +
               "roundingMode=" +        roundingMode.toString();
    }

    // 私有方法

    /**
     * 从流中重新构建 {@code MathContext} 实例（即，反序列化它）。
     *
     * @param s 正在读取的流。
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();     // 读取所有字段
        // 验证可能的错误字段
        if (precision < MIN_DIGITS) {
            String message = "MathContext: invalid digits in stream";
            throw new java.io.StreamCorruptedException(message);
        }
        if (roundingMode == null) {
            String message = "MathContext: null roundingMode in stream";
            throw new java.io.StreamCorruptedException(message);
        }
    }

}
