
/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyright IBM Corporation, 2001. All Rights Reserved.
 */
package java.math;

/**
 * 指定一个<i>舍入行为</i>，用于能够丢弃精度的数值操作。每种舍入模式指示如何计算舍入结果的最不重要的返回数字。如果返回的数字比表示精确数值所需的数字少，被丢弃的数字将被称为<i>被丢弃的小数部分</i>，无论这些数字对数值的贡献如何。换句话说，作为数值，被丢弃的小数部分的绝对值可能大于一。
 *
 * <p>每个舍入模式描述包括一个表格，列出了不同的两位十进制值如何在给定的舍入模式下舍入到一位十进制值。结果列可以通过创建一个指定值的{@code BigDecimal}数字，形成一个具有适当设置（{@code precision}设置为{@code 1}，{@code roundingMode}设置为问题中的舍入模式）的{@link MathContext}对象，并调用此数字的{@link BigDecimal#round round}方法来获得。所有舍入模式的舍入操作结果的汇总表如下所示。
 *
 *<table border>
 * <caption><b>不同舍入模式下的舍入操作汇总表</b></caption>
 * <tr><th></th><th colspan=8>使用给定舍入模式将输入舍入到一位数字的结果</th>
 * <tr valign=top>
 * <th>输入数字</th>         <th>{@code UP}</th>
 *                                           <th>{@code DOWN}</th>
 *                                                        <th>{@code CEILING}</th>
 *                                                                       <th>{@code FLOOR}</th>
 *                                                                                    <th>{@code HALF_UP}</th>
 *                                                                                                   <th>{@code HALF_DOWN}</th>
 *                                                                                                                    <th>{@code HALF_EVEN}</th>
 *                                                                                                                                     <th>{@code UNNECESSARY}</th>
 *
 * <tr align=right><td>5.5</td>  <td>6</td>  <td>5</td>    <td>6</td>    <td>5</td>  <td>6</td>      <td>5</td>       <td>6</td>       <td>抛出 {@code ArithmeticException}</td>
 * <tr align=right><td>2.5</td>  <td>3</td>  <td>2</td>    <td>3</td>    <td>2</td>  <td>3</td>      <td>2</td>       <td>2</td>       <td>抛出 {@code ArithmeticException}</td>
 * <tr align=right><td>1.6</td>  <td>2</td>  <td>1</td>    <td>2</td>    <td>1</td>  <td>2</td>      <td>2</td>       <td>2</td>       <td>抛出 {@code ArithmeticException}</td>
 * <tr align=right><td>1.1</td>  <td>2</td>  <td>1</td>    <td>2</td>    <td>1</td>  <td>1</td>      <td>1</td>       <td>1</td>       <td>抛出 {@code ArithmeticException}</td>
 * <tr align=right><td>1.0</td>  <td>1</td>  <td>1</td>    <td>1</td>    <td>1</td>  <td>1</td>      <td>1</td>       <td>1</td>       <td>1</td>
 * <tr align=right><td>-1.0</td> <td>-1</td> <td>-1</td>   <td>-1</td>   <td>-1</td> <td>-1</td>     <td>-1</td>      <td>-1</td>      <td>-1</td>
 * <tr align=right><td>-1.1</td> <td>-2</td> <td>-1</td>   <td>-1</td>   <td>-2</td> <td>-1</td>     <td>-1</td>      <td>-1</td>      <td>抛出 {@code ArithmeticException}</td>
 * <tr align=right><td>-1.6</td> <td>-2</td> <td>-1</td>   <td>-1</td>   <td>-2</td> <td>-2</td>     <td>-2</td>      <td>-2</td>      <td>抛出 {@code ArithmeticException}</td>
 * <tr align=right><td>-2.5</td> <td>-3</td> <td>-2</td>   <td>-2</td>   <td>-3</td> <td>-3</td>     <td>-2</td>      <td>-2</td>      <td>抛出 {@code ArithmeticException}</td>
 * <tr align=right><td>-5.5</td> <td>-6</td> <td>-5</td>   <td>-5</td>   <td>-6</td> <td>-6</td>     <td>-5</td>      <td>-6</td>      <td>抛出 {@code ArithmeticException}</td>
 *</table>
 *
 *
 * <p>此 {@code enum} 旨在替换 {@link BigDecimal} 中基于整数的舍入模式常量枚举（{@link BigDecimal#ROUND_UP}，{@link BigDecimal#ROUND_DOWN} 等）。
 *
 * @see     BigDecimal
 * @see     MathContext
 * @author  Josh Bloch
 * @author  Mike Cowlishaw
 * @author  Joseph D. Darcy
 * @since 1.5
 */
public enum RoundingMode {

        /**
         * 舍入模式，向远离零的方向舍入。始终增加非零被丢弃小数部分之前的数字。请注意，此舍入模式从不减少计算值的大小。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 UP 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>使用 {@code UP} 舍入将输入舍入到一位数字</th>
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>3</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>2</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-2</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-3</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    UP(BigDecimal.ROUND_UP),

        /**
         * 舍入模式，向零的方向舍入。从不增加被丢弃小数部分之前的数字（即截断）。请注意，此舍入模式从不增加计算值的大小。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 DOWN 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>使用 {@code DOWN} 舍入将输入舍入到一位数字</th>
         *<tr align=right><td>5.5</td>  <td>5</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>1</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-1</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-5</td>
         *</table>
         */
    DOWN(BigDecimal.ROUND_DOWN),

        /**
         * 舍入模式，向正无穷方向舍入。如果结果为正，则行为与 {@code RoundingMode.UP} 相同；如果为负，则行为与 {@code RoundingMode.DOWN} 相同。请注意，此舍入模式从不减少计算值。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 CEILING 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>使用 {@code CEILING} 舍入将输入舍入到一位数字</th>
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>3</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>2</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-1</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-5</td>
         *</table>
         */
    CEILING(BigDecimal.ROUND_CEILING),

        /**
         * 舍入模式，向负无穷方向舍入。如果结果为正，则行为与 {@code RoundingMode.DOWN} 相同；如果为负，则行为与 {@code RoundingMode.UP} 相同。请注意，此舍入模式从不增加计算值。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 FLOOR 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>使用 {@code FLOOR} 舍入将输入舍入到一位数字</th>
         *<tr align=right><td>5.5</td>  <td>5</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>1</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-2</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-3</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    FLOOR(BigDecimal.ROUND_FLOOR),

        /**
         * 舍入模式，向“最近邻居”舍入，除非两个邻居等距，此时向上舍入。如果被丢弃的小数部分 ≥ 0.5，则行为与 {@code RoundingMode.UP} 相同；否则，行为与 {@code RoundingMode.DOWN} 相同。请注意，这是学校中常见的舍入模式。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 HALF_UP 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>使用 {@code HALF_UP} 舍入将输入舍入到一位数字</th>
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>3</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-3</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    HALF_UP(BigDecimal.ROUND_HALF_UP),

        /**
         * 舍入模式，向“最近邻居”舍入，除非两个邻居等距，此时向下舍入。如果被丢弃的小数部分 > 0.5，则行为与 {@code RoundingMode.UP} 相同；否则，行为与 {@code RoundingMode.DOWN} 相同。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 HALF_DOWN 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>使用 {@code HALF_DOWN} 舍入将输入舍入到一位数字</th>
         *<tr align=right><td>5.5</td>  <td>5</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-5</td>
         *</table>
         */
    HALF_DOWN(BigDecimal.ROUND_HALF_DOWN),

        /**
         * 舍入模式，向“最近邻居”舍入，除非两个邻居等距，此时向偶数邻居舍入。如果被丢弃小数部分左边的数字是奇数，则行为与 {@code RoundingMode.HALF_UP} 相同；如果它是偶数，则行为与 {@code RoundingMode.HALF_DOWN} 相同。请注意，这是统计上最小化一系列计算中累积误差的舍入模式。它有时被称为“银行家舍入”，主要在美国使用。此舍入模式类似于 Java 中 {@code float} 和 {@code double} 算术使用的舍入策略。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 HALF_EVEN 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>使用 {@code HALF_EVEN} 舍入将输入舍入到一位数字</th>
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    HALF_EVEN(BigDecimal.ROUND_HALF_EVEN),

        /**
         * 舍入模式，断言请求的操作具有精确结果，因此不需要舍入。如果在此操作中指定了此舍入模式并且结果不精确，则抛出 {@code ArithmeticException}。
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 UNNECESSARY 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>使用 {@code UNNECESSARY} 舍入将输入舍入到一位数字</th>
         *<tr align=right><td>5.5</td>  <td>抛出 {@code ArithmeticException}</td>
         *<tr align=right><td>2.5</td>  <td>抛出 {@code ArithmeticException}</td>
         *<tr align=right><td>1.6</td>  <td>抛出 {@code ArithmeticException}</td>
         *<tr align=right><td>1.1</td>  <td>抛出 {@code ArithmeticException}</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>抛出 {@code ArithmeticException}</td>
         *<tr align=right><td>-1.6</td> <td>抛出 {@code ArithmeticException}</td>
         *<tr align=right><td>-2.5</td> <td>抛出 {@code ArithmeticException}</td>
         *<tr align=right><td>-5.5</td> <td>抛出 {@code ArithmeticException}</td>
         *</table>
         */
    UNNECESSARY(BigDecimal.ROUND_UNNECESSARY);


                // 对应的 BigDecimal 四舍五入常量
    final int oldMode;

    /**
     * 构造函数
     *
     * @param oldMode 与该模式对应的 {@code BigDecimal} 常量
     */
    private RoundingMode(int oldMode) {
        this.oldMode = oldMode;
    }

    /**
     * 返回与 {@link BigDecimal} 中的旧整数四舍五入模式常量对应的 {@code RoundingMode} 对象。
     *
     * @param  rm 要转换的旧整数四舍五入模式
     * @return 与给定整数对应的 {@code RoundingMode}。
     * @throws IllegalArgumentException 整数超出范围
     */
    public static RoundingMode valueOf(int rm) {
        switch(rm) {

        case BigDecimal.ROUND_UP:
            return UP;

        case BigDecimal.ROUND_DOWN:
            return DOWN;

        case BigDecimal.ROUND_CEILING:
            return CEILING;

        case BigDecimal.ROUND_FLOOR:
            return FLOOR;

        case BigDecimal.ROUND_HALF_UP:
            return HALF_UP;

        case BigDecimal.ROUND_HALF_DOWN:
            return HALF_DOWN;

        case BigDecimal.ROUND_HALF_EVEN:
            return HALF_EVEN;

        case BigDecimal.ROUND_UNNECESSARY:
            return UNNECESSARY;

        default:
            throw new IllegalArgumentException("argument out of range");
        }
    }
}
