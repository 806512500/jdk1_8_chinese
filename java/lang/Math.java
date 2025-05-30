
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
import java.util.Random;

import sun.misc.FloatConsts;
import sun.misc.DoubleConsts;

/**
 * 类 {@code Math} 包含了执行基本数值操作的方法，如基本的指数、对数、平方根和三角函数。
 *
 * <p>与类 {@code StrictMath} 的某些数值方法不同，类 {@code Math} 的所有等效函数的实现不是定义为返回完全相同的位结果。这种放松允许在不需要严格可重复性的情况下提供性能更好的实现。
 *
 * <p>默认情况下，许多 {@code Math} 方法的实现只是调用 {@code StrictMath} 中的等效方法。鼓励代码生成器使用可用的平台特定的本地库或微处理器指令，以提供更高性能的 {@code Math} 方法实现。这些更高性能的实现仍必须符合 {@code Math} 的规范。
 *
 * <p>实现质量规范涉及两个属性：返回结果的准确性以及方法的单调性。{@code Math} 方法的浮点数准确性以 <i>ulps</i>（最后一位单位）来衡量。对于给定的浮点格式，一个特定实数值的 <i>ulp</i> 是包围该数值的两个浮点值之间的距离。当讨论一个方法在所有参数上的整体准确性时，引用的 ulps 数是任何参数上的最坏情况误差。如果一个方法的误差始终小于 0.5 ulps，该方法总是返回最接近精确结果的浮点数；这样的方法是 <i>正确舍入的</i>。正确舍入的方法通常是浮点近似的最佳选择；然而，许多浮点方法实现正确舍入是不切实际的。因此，对于 {@code Math} 类，某些方法允许 1 或 2 ulps 的更大误差。非正式地，对于 1 ulp 的误差界限，当精确结果是可表示的数时，应该返回精确结果作为计算结果；否则，可以返回包围精确结果的两个浮点值中的任何一个。对于大数量级的精确结果，括号的一端可能是无穷大。除了在个别参数上的准确性，保持方法在不同参数上的适当关系也很重要。因此，大多数误差超过 0.5 ulps 的方法要求是 <i>半单调的</i>：每当数学函数是非递减的，浮点近似也是非递减的；同样，每当数学函数是非递增的，浮点近似也是非递增的。并非所有具有 1 ulp 准确性的近似都能自动满足单调性要求。
 *
 * <p>
 * 平台使用带有 int 和 long 原始类型的带符号二进制补码整数算术。开发人员应选择原始类型以确保算术操作始终产生正确的结果，这在某些情况下意味着操作不会超出计算值的范围。最佳做法是选择原始类型和算法以避免溢出。在大小为 {@code int} 或 {@code long} 且需要检测溢出错误的情况下，方法 {@code addExact}、{@code subtractExact}、{@code multiplyExact} 和 {@code toIntExact} 在结果溢出时会抛出 {@code ArithmeticException}。对于其他算术操作，如除法、绝对值、递增、递减和取反，溢出仅在特定的最小值或最大值时发生，应根据需要检查最小值或最大值。
 *
 * @author  未署名
 * @author  Joseph D. Darcy
 * @since   JDK1.0
 */

public final class Math {

    /**
     * 不允许任何人实例化这个类。
     */
    private Math() {}

    /**
     * 最接近自然对数底数 <i>e</i> 的 {@code double} 值。
     */
    public static final double E = 2.7182818284590452354;

    /**
     * 最接近圆周率 <i>pi</i> 的 {@code double} 值，即圆的周长与其直径的比值。
     */
    public static final double PI = 3.14159265358979323846;

    /**
     * 返回一个角度的正弦值。特殊情况：
     * <ul><li>如果参数是 NaN 或无穷大，结果是 NaN。
     * <li>如果参数是零，结果是与参数同符号的零。</ul>
     *
     * <p>计算结果必须在精确结果的 1 ulp 以内。结果必须是半单调的。
     *
     * @param   a   一个角度，以弧度为单位。
     * @return  参数的正弦值。
     */
    public static double sin(double a) {
        return StrictMath.sin(a); // 默认实现委托给 StrictMath
    }

    /**
     * 返回一个角度的余弦值。特殊情况：
     * <ul><li>如果参数是 NaN 或无穷大，结果是 NaN。</ul>
     *
     * <p>计算结果必须在精确结果的 1 ulp 以内。结果必须是半单调的。
     *
     * @param   a   一个角度，以弧度为单位。
     * @return  参数的余弦值。
     */
    public static double cos(double a) {
        return StrictMath.cos(a); // 默认实现委托给 StrictMath
    }

    /**
     * 返回一个角度的正切值。特殊情况：
     * <ul><li>如果参数是 NaN 或无穷大，结果是 NaN。
     * <li>如果参数是零，结果是与参数同符号的零。</ul>
     *
     * <p>计算结果必须在精确结果的 1 ulp 以内。结果必须是半单调的。
     *
     * @param   a   一个角度，以弧度为单位。
     * @return  参数的正切值。
     */
    public static double tan(double a) {
        return StrictMath.tan(a); // 默认实现委托给 StrictMath
    }

    /**
     * 返回一个值的反正弦值；返回的角度范围是 -<i>pi</i>/2 到 <i>pi</i>/2。特殊情况：
     * <ul><li>如果参数是 NaN 或其绝对值大于 1，结果是 NaN。
     * <li>如果参数是零，结果是与参数同符号的零。</ul>
     *
     * <p>计算结果必须在精确结果的 1 ulp 以内。结果必须是半单调的。
     *
     * @param   a   要返回反正弦值的值。
     * @return  参数的反正弦值。
     */
    public static double asin(double a) {
        return StrictMath.asin(a); // 默认实现委托给 StrictMath
    }

    /**
     * 返回一个值的反余弦值；返回的角度范围是 0.0 到 <i>pi</i>。特殊情况：
     * <ul><li>如果参数是 NaN 或其绝对值大于 1，结果是 NaN。</ul>
     *
     * <p>计算结果必须在精确结果的 1 ulp 以内。结果必须是半单调的。
     *
     * @param   a   要返回反余弦值的值。
     * @return  参数的反余弦值。
     */
    public static double acos(double a) {
        return StrictMath.acos(a); // 默认实现委托给 StrictMath
    }

    /**
     * 返回一个值的反正切值；返回的角度范围是 -<i>pi</i>/2 到 <i>pi</i>/2。特殊情况：
     * <ul><li>如果参数是 NaN，结果是 NaN。
     * <li>如果参数是零，结果是与参数同符号的零。</ul>
     *
     * <p>计算结果必须在精确结果的 1 ulp 以内。结果必须是半单调的。
     *
     * @param   a   要返回反正切值的值。
     * @return  参数的反正切值。
     */
    public static double atan(double a) {
        return StrictMath.atan(a); // 默认实现委托给 StrictMath
    }

    /**
     * 将以度为单位的角度转换为以弧度为单位的近似等效角度。从度到弧度的转换通常是不精确的。
     *
     * @param   angdeg   一个角度，以度为单位
     * @return  角度 {@code angdeg} 以弧度为单位的测量值。
     * @since   1.2
     */
    public static double toRadians(double angdeg) {
        return angdeg / 180.0 * PI;
    }

    /**
     * 将以弧度为单位的角度转换为以度为单位的近似等效角度。从弧度到度的转换通常是不精确的；用户不应期望 {@code cos(toRadians(90.0))} 精确等于 {@code 0.0}。
     *
     * @param   angrad   一个角度，以弧度为单位
     * @return  角度 {@code angrad} 以度为单位的测量值。
     * @since   1.2
     */
    public static double toDegrees(double angrad) {
        return angrad * 180.0 / PI;
    }

    /**
     * 返回 Euler 数 <i>e</i> 的 {@code double} 值的幂。特殊情况：
     * <ul><li>如果参数是 NaN，结果是 NaN。
     * <li>如果参数是正无穷大，结果是正无穷大。
     * <li>如果参数是负无穷大，结果是正零。</ul>
     *
     * <p>计算结果必须在精确结果的 1 ulp 以内。结果必须是半单调的。
     *
     * @param   a   要提升 <i>e</i> 的幂的指数。
     * @return  值 <i>e</i><sup>{@code a}</sup>，其中 <i>e</i> 是自然对数的底数。
     */
    public static double exp(double a) {
        return StrictMath.exp(a); // 默认实现委托给 StrictMath
    }

    /**
     * 返回 {@code double} 值的自然对数（以 <i>e</i> 为底）。特殊情况：
     * <ul><li>如果参数是 NaN 或小于零，结果是 NaN。
     * <li>如果参数是正无穷大，结果是正无穷大。
     * <li>如果参数是正零或负零，结果是负无穷大。</ul>
     *
     * <p>计算结果必须在精确结果的 1 ulp 以内。结果必须是半单调的。
     *
     * @param   a   一个值
     * @return  值 ln&nbsp;{@code a}，即 {@code a} 的自然对数。
     */
    public static double log(double a) {
        return StrictMath.log(a); // 默认实现委托给 StrictMath
    }

    /**
     * 返回 {@code double} 值的以 10 为底的对数。特殊情况：
     *
     * <ul><li>如果参数是 NaN 或小于零，结果是 NaN。
     * <li>如果参数是正无穷大，结果是正无穷大。
     * <li>如果参数是正零或负零，结果是负无穷大。
     * <li>如果参数等于 10<sup><i>n</i></sup>，其中 <i>n</i> 是整数，结果是 <i>n</i>。
     * </ul>
     *
     * <p>计算结果必须在精确结果的 1 ulp 以内。结果必须是半单调的。
     *
     * @param   a   一个值
     * @return  值 {@code a} 的以 10 为底的对数。
     * @since 1.5
     */
    public static double log10(double a) {
        return StrictMath.log10(a); // 默认实现委托给 StrictMath
    }

    /**
     * 返回 {@code double} 值的正确舍入的正平方根。
     * 特殊情况：
     * <ul><li>如果参数是 NaN 或小于零，结果是 NaN。
     * <li>如果参数是正无穷大，结果是正无穷大。
     * <li>如果参数是正零或负零，结果与参数相同。</ul>
     * 否则，结果是与参数值的真实数学平方根最接近的 {@code double} 值。
     *
     * @param   a   一个值。
     * @return  值 {@code a} 的正平方根。
     *          如果参数是 NaN 或小于零，结果是 NaN。
     */
    public static double sqrt(double a) {
        return StrictMath.sqrt(a); // 默认实现委托给 StrictMath
                                   // 注意 JIT 可以直接使用硬件平方根指令
                                   // 应该比在软件中调用 Math.sqrt 快得多。
    }


    /**
     * 返回 {@code double} 值的立方根。对于正的有限 {@code x}，{@code cbrt(-x) == -cbrt(x)}；即，负值的立方根是该值的绝对值的立方根的负值。
     *
     * 特殊情况：
     *
     * <ul>
     *
     * <li>如果参数是 NaN，结果是 NaN。
     *
     * <li>如果参数是无穷大，结果是与参数同符号的无穷大。
     *
     * <li>如果参数是零，结果是与参数同符号的零。
     *
     * </ul>
     *
     * <p>计算结果必须在精确结果的 1 ulp 以内。
     *
     * @param   a   一个值。
     * @return  值 {@code a} 的立方根。
     * @since 1.5
     */
    public static double cbrt(double a) {
        return StrictMath.cbrt(a);
    }

    /**
     * 按照 IEEE 754 标准计算两个参数的余数操作。
     * 余数值在数学上等于
     * <code>f1&nbsp;-&nbsp;f2</code>&nbsp;&times;&nbsp;<i>n</i>，
     * 其中 <i>n</i> 是最接近 {@code f1/f2} 的精确数学值的数学整数，如果两个数学整数与 {@code f1/f2} 同等接近，则 <i>n</i> 是偶数的整数。如果余数是零，其符号与第一个参数的符号相同。特殊情况：
     * <ul><li>如果任一参数是 NaN，或第一个参数是无穷大，或第二个参数是正零或负零，结果是 NaN。
     * <li>如果第一个参数是有限的，第二个参数是无穷大，结果与第一个参数相同。</ul>
     *
     * @param   f1   被除数。
     * @param   f2   除数。
     * @return  当 {@code f1} 被 {@code f2} 除时的余数。
     */
    public static double IEEEremainder(double f1, double f2) {
        return StrictMath.IEEEremainder(f1, f2); // 委托给 StrictMath
    }


                /**
     * 返回大于或等于参数且等于数学整数的最小（最接近负无穷大）
     * {@code double} 值。特殊情况：
     * <ul><li>如果参数值已经是数学整数，则结果与参数相同。
     * <li>如果参数是 NaN 或无穷大或正零或负零，则结果与参数相同。
     * <li>如果参数值小于零但大于 -1.0，则结果是负零。</ul> 注意
     * {@code Math.ceil(x)} 的值正好等于 {@code -Math.floor(-x)}。
     *
     *
     * @param   a   一个值。
     * @return  大于或等于参数且等于数学整数的最小（最接近负无穷大）
     *          浮点值。
     */
    public static double ceil(double a) {
        return StrictMath.ceil(a); // 默认实现委托给 StrictMath
    }

    /**
     * 返回小于或等于参数且等于数学整数的最大（最接近正无穷大）
     * {@code double} 值。特殊情况：
     * <ul><li>如果参数值已经是数学整数，则结果与参数相同。
     * <li>如果参数是 NaN 或无穷大或正零或负零，则结果与参数相同。</ul>
     *
     * @param   a   一个值。
     * @return  小于或等于参数且等于数学整数的最大（最接近正无穷大）
     *          浮点值。
     */
    public static double floor(double a) {
        return StrictMath.floor(a); // 默认实现委托给 StrictMath
    }

    /**
     * 返回最接近参数值且等于数学整数的 {@code double} 值。如果两个
     * {@code double} 值都是数学整数且距离相等，结果是偶数的整数值。特殊情况：
     * <ul><li>如果参数值已经是数学整数，则结果与参数相同。
     * <li>如果参数是 NaN 或无穷大或正零或负零，则结果与参数相同。</ul>
     *
     * @param   a   一个 {@code double} 值。
     * @return  最接近 {@code a} 且等于数学整数的浮点值。
     */
    public static double rint(double a) {
        return StrictMath.rint(a); // 默认实现委托给 StrictMath
    }

    /**
     * 返回从直角坐标 ({@code x},&nbsp;{@code y}) 转换到极坐标
     * (r,&nbsp;<i>theta</i>) 的角度 <i>theta</i>。
     * 该方法通过计算 {@code y/x} 的反正切值来计算相位 <i>theta</i>，范围为 -<i>pi</i> 到 <i>pi</i>。特殊情况：
     * <ul><li>如果任一参数是 NaN，则结果是 NaN。
     * <li>如果第一个参数是正零且第二个参数是正数，或者第一个参数是正数且有限且第二个参数是正无穷大，则结果是正零。
     * <li>如果第一个参数是负零且第二个参数是正数，或者第一个参数是负数且有限且第二个参数是正无穷大，则结果是负零。
     * <li>如果第一个参数是正零且第二个参数是负数，或者第一个参数是正数且有限且第二个参数是负无穷大，则结果是
     * {@code double} 值中最接近 <i>pi</i> 的值。
     * <li>如果第一个参数是负零且第二个参数是负数，或者第一个参数是负数且有限且第二个参数是负无穷大，则结果是
     * {@code double} 值中最接近 -<i>pi</i> 的值。
     * <li>如果第一个参数是正数且第二个参数是正零或负零，或者第一个参数是正无穷大且第二个参数是有限数，则结果是
     * {@code double} 值中最接近 <i>pi</i>/2 的值。
     * <li>如果第一个参数是负数且第二个参数是正零或负零，或者第一个参数是负无穷大且第二个参数是有限数，则结果是
     * {@code double} 值中最接近 -<i>pi</i>/2 的值。
     * <li>如果两个参数都是正无穷大，则结果是
     * {@code double} 值中最接近 <i>pi</i>/4 的值。
     * <li>如果第一个参数是正无穷大且第二个参数是负无穷大，则结果是
     * {@code double} 值中最接近 3*<i>pi</i>/4 的值。
     * <li>如果第一个参数是负无穷大且第二个参数是正无穷大，则结果是
     * {@code double} 值中最接近 -<i>pi</i>/4 的值。
     * <li>如果两个参数都是负无穷大，则结果是
     * {@code double} 值中最接近 -3*<i>pi</i>/4 的值。</ul>
     *
     * <p>计算结果必须在精确结果的 2 ulps 以内。
     * 结果必须是半单调的。
     *
     * @param   y   纵坐标
     * @param   x   横坐标
     * @return  点
     *          (<i>r</i>,&nbsp;<i>theta</i>)
     *          在极坐标系中对应于点
     *          (<i>x</i>,&nbsp;<i>y</i>) 的 <i>theta</i> 分量。
     */
    public static double atan2(double y, double x) {
        return StrictMath.atan2(y, x); // 默认实现委托给 StrictMath
    }

    /**
     * 返回第一个参数的值的第二个参数次幂。特殊情况：
     *
     * <ul><li>如果第二个参数是正零或负零，则结果是 1.0。
     * <li>如果第二个参数是 1.0，则结果与第一个参数相同。
     * <li>如果第二个参数是 NaN，则结果是 NaN。
     * <li>如果第一个参数是 NaN 且第二个参数是非零值，则结果是 NaN。
     *
     * <li>如果
     * <ul>
     * <li>第一个参数的绝对值大于 1 且第二个参数是正无穷大，或者
     * <li>第一个参数的绝对值小于 1 且第二个参数是负无穷大，
     * </ul>
     * 则结果是正无穷大。
     *
     * <li>如果
     * <ul>
     * <li>第一个参数的绝对值大于 1 且第二个参数是负无穷大，或者
     * <li>第一个参数的绝对值小于 1 且第二个参数是正无穷大，
     * </ul>
     * 则结果是正零。
     *
     * <li>如果第一个参数的绝对值等于 1 且第二个参数是无穷大，则结果是 NaN。
     *
     * <li>如果
     * <ul>
     * <li>第一个参数是正零且第二个参数大于零，或者
     * <li>第一个参数是正无穷大且第二个参数小于零，
     * </ul>
     * 则结果是正零。
     *
     * <li>如果
     * <ul>
     * <li>第一个参数是正零且第二个参数小于零，或者
     * <li>第一个参数是正无穷大且第二个参数大于零，
     * </ul>
     * 则结果是正无穷大。
     *
     * <li>如果
     * <ul>
     * <li>第一个参数是负零且第二个参数大于零但不是有限奇数，或者
     * <li>第一个参数是负无穷大且第二个参数小于零但不是有限奇数，
     * </ul>
     * 则结果是正零。
     *
     * <li>如果
     * <ul>
     * <li>第一个参数是负零且第二个参数是正有限奇数，或者
     * <li>第一个参数是负无穷大且第二个参数是负有限奇数，
     * </ul>
     * 则结果是负零。
     *
     * <li>如果
     * <ul>
     * <li>第一个参数是负零且第二个参数小于零但不是有限奇数，或者
     * <li>第一个参数是负无穷大且第二个参数大于零但不是有限奇数，
     * </ul>
     * 则结果是正无穷大。
     *
     * <li>如果
     * <ul>
     * <li>第一个参数是负零且第二个参数是负有限奇数，或者
     * <li>第一个参数是负无穷大且第二个参数是正有限奇数，
     * </ul>
     * 则结果是负无穷大。
     *
     * <li>如果第一个参数是有限数且小于零
     * <ul>
     * <li>如果第二个参数是有限偶数，则结果等于将第一个参数的绝对值的第二个参数次幂的结果
     *
     * <li>如果第二个参数是有限奇数，则结果等于将第一个参数的绝对值的第二个参数次幂的结果的负数
     *
     * <li>如果第二个参数是有限数且不是整数，则结果是 NaN。
     * </ul>
     *
     * <li>如果两个参数都是整数，则结果精确等于将第一个参数的第二个参数次幂的数学结果，前提是该结果可以精确表示为
     * {@code double} 值。</ul>
     *
     * <p>（在上述描述中，如果一个浮点值是有限数且是 {@link #ceil ceil} 方法或等效的
     * {@link #floor floor} 方法的不动点，则该值被认为是整数。一个值是单参数方法的不动点当且仅当将该方法应用于该值的结果等于该值。）
     *
     * <p>计算结果必须在精确结果的 1 ulp 以内。
     * 结果必须是半单调的。
     *
     * @param   a   底数。
     * @param   b   指数。
     * @return  值 {@code a}<sup>{@code b}</sup>。
     */
    public static double pow(double a, double b) {
        return StrictMath.pow(a, b); // 默认实现委托给 StrictMath
    }

    /**
     * 返回最接近参数的 {@code int} 值，如果结果在两个整数之间，则四舍五入到正无穷大。
     *
     * <p>
     * 特殊情况：
     * <ul><li>如果参数是 NaN，结果是 0。
     * <li>如果参数是负无穷大或任何小于或等于
     * {@code Integer.MIN_VALUE} 的值，结果等于
     * {@code Integer.MIN_VALUE} 的值。
     * <li>如果参数是正无穷大或任何大于或等于
     * {@code Integer.MAX_VALUE} 的值，结果等于
     * {@code Integer.MAX_VALUE} 的值。</ul>
     *
     * @param   a   要四舍五入为整数的浮点值。
     * @return  参数四舍五入到最接近的
     *          {@code int} 值。
     * @see     java.lang.Integer#MAX_VALUE
     * @see     java.lang.Integer#MIN_VALUE
     */
    public static int round(float a) {
        int intBits = Float.floatToRawIntBits(a);
        int biasedExp = (intBits & FloatConsts.EXP_BIT_MASK)
                >> (FloatConsts.SIGNIFICAND_WIDTH - 1);
        int shift = (FloatConsts.SIGNIFICAND_WIDTH - 2
                + FloatConsts.EXP_BIAS) - biasedExp;
        if ((shift & -32) == 0) { // shift >= 0 && shift < 32
            // a 是一个有限数，使得 pow(2,-32) <= ulp(a) < 1
            int r = ((intBits & FloatConsts.SIGNIF_BIT_MASK)
                    | (FloatConsts.SIGNIF_BIT_MASK + 1));
            if (intBits < 0) {
                r = -r;
            }
            // 下面的注释中每个 Java 表达式评估为对应的数学表达式的值：
            // (r) 评估为 a / ulp(a)
            // (r >> shift) 评估为 floor(a * 2)
            // ((r >> shift) + 1) 评估为 floor((a + 1/2) * 2)
            // (((r >> shift) + 1) >> 1) 评估为 floor(a + 1/2)
            return ((r >> shift) + 1) >> 1;
        } else {
            // a 要么是
            // - 一个有限数，其 abs(a) < exp(2,FloatConsts.SIGNIFICAND_WIDTH-32) < 1/2
            // - 一个有限数，其 ulp(a) >= 1 且因此 a 是一个数学整数
            // - 无穷大或 NaN
            return (int) a;
        }
    }

    /**
     * 返回最接近参数的 {@code long} 值，如果结果在两个整数之间，则四舍五入到正无穷大。
     *
     * <p>特殊情况：
     * <ul><li>如果参数是 NaN，结果是 0。
     * <li>如果参数是负无穷大或任何小于或等于
     * {@code Long.MIN_VALUE} 的值，结果等于
     * {@code Long.MIN_VALUE} 的值。
     * <li>如果参数是正无穷大或任何大于或等于
     * {@code Long.MAX_VALUE} 的值，结果等于
     * {@code Long.MAX_VALUE} 的值。</ul>
     *
     * @param   a   要四舍五入为
     *          {@code long} 的浮点值。
     * @return  参数四舍五入到最接近的
     *          {@code long} 值。
     * @see     java.lang.Long#MAX_VALUE
     * @see     java.lang.Long#MIN_VALUE
     */
    public static long round(double a) {
        long longBits = Double.doubleToRawLongBits(a);
        long biasedExp = (longBits & DoubleConsts.EXP_BIT_MASK)
                >> (DoubleConsts.SIGNIFICAND_WIDTH - 1);
        long shift = (DoubleConsts.SIGNIFICAND_WIDTH - 2
                + DoubleConsts.EXP_BIAS) - biasedExp;
        if ((shift & -64) == 0) { // shift >= 0 && shift < 64
            // a 是一个有限数，使得 pow(2,-64) <= ulp(a) < 1
            long r = ((longBits & DoubleConsts.SIGNIF_BIT_MASK)
                    | (DoubleConsts.SIGNIF_BIT_MASK + 1));
            if (longBits < 0) {
                r = -r;
            }
            // 下面的注释中每个 Java 表达式评估为对应的数学表达式的值：
            // (r) 评估为 a / ulp(a)
            // (r >> shift) 评估为 floor(a * 2)
            // ((r >> shift) + 1) 评估为 floor((a + 1/2) * 2)
            // (((r >> shift) + 1) >> 1) 评估为 floor(a + 1/2)
            return ((r >> shift) + 1) >> 1;
        } else {
            // a 要么是
            // - 一个有限数，其 abs(a) < exp(2,DoubleConsts.SIGNIFICAND_WIDTH-64) < 1/2
            // - 一个有限数，其 ulp(a) >= 1 且因此 a 是一个数学整数
            // - 无穷大或 NaN
            return (long) a;
        }
    }


                private static final class RandomNumberGeneratorHolder {
        static final Random randomNumberGenerator = new Random();
    }

    /**
     * 返回一个正数的 {@code double} 值，大于等于 {@code 0.0} 且小于 {@code 1.0}。
     * 返回的值是从该范围中伪随机选择的，（大约）均匀分布。
     *
     * <p>当此方法首次被调用时，它会创建一个新的伪随机数生成器，就像通过以下表达式创建的一样：
     *
     * <blockquote>{@code new java.util.Random()}</blockquote>
     *
     * 从此以后，此新的伪随机数生成器将用于此方法的所有调用，并且不会在其他地方使用。
     *
     * <p>此方法已正确同步，允许多个线程正确使用。
     * 但是，如果许多线程需要以很高的速率生成伪随机数，每个线程拥有自己的伪随机数生成器可能会减少争用。
     *
     * @return 一个伪随机的 {@code double}，大于等于 {@code 0.0} 且小于 {@code 1.0}。
     * @see Random#nextDouble()
     */
    public static double random() {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextDouble();
    }

    /**
     * 返回其参数的和，
     * 如果结果溢出 {@code int}，则抛出异常。
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return 结果
     * @throws ArithmeticException 如果结果溢出 int
     * @since 1.8
     */
    public static int addExact(int x, int y) {
        int r = x + y;
        // HD 2-12 溢出当且仅当两个参数的符号与结果的符号相反
        if (((x ^ r) & (y ^ r)) < 0) {
            throw new ArithmeticException("integer overflow");
        }
        return r;
    }

    /**
     * 返回其参数的和，
     * 如果结果溢出 {@code long}，则抛出异常。
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return 结果
     * @throws ArithmeticException 如果结果溢出 long
     * @since 1.8
     */
    public static long addExact(long x, long y) {
        long r = x + y;
        // HD 2-12 溢出当且仅当两个参数的符号与结果的符号相反
        if (((x ^ r) & (y ^ r)) < 0) {
            throw new ArithmeticException("long overflow");
        }
        return r;
    }

    /**
     * 返回其参数的差，
     * 如果结果溢出 {@code int}，则抛出异常。
     *
     * @param x 第一个值
     * @param y 从第一个值中减去的第二个值
     * @return 结果
     * @throws ArithmeticException 如果结果溢出 int
     * @since 1.8
     */
    public static int subtractExact(int x, int y) {
        int r = x - y;
        // HD 2-12 溢出当且仅当参数符号不同且结果的符号与 x 的符号不同
        if (((x ^ y) & (x ^ r)) < 0) {
            throw new ArithmeticException("integer overflow");
        }
        return r;
    }

    /**
     * 返回其参数的差，
     * 如果结果溢出 {@code long}，则抛出异常。
     *
     * @param x 第一个值
     * @param y 从第一个值中减去的第二个值
     * @return 结果
     * @throws ArithmeticException 如果结果溢出 long
     * @since 1.8
     */
    public static long subtractExact(long x, long y) {
        long r = x - y;
        // HD 2-12 溢出当且仅当参数符号不同且结果的符号与 x 的符号不同
        if (((x ^ y) & (x ^ r)) < 0) {
            throw new ArithmeticException("long overflow");
        }
        return r;
    }

    /**
     * 返回其参数的乘积，
     * 如果结果溢出 {@code int}，则抛出异常。
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return 结果
     * @throws ArithmeticException 如果结果溢出 int
     * @since 1.8
     */
    public static int multiplyExact(int x, int y) {
        long r = (long)x * (long)y;
        if ((int)r != r) {
            throw new ArithmeticException("integer overflow");
        }
        return (int)r;
    }

    /**
     * 返回其参数的乘积，
     * 如果结果溢出 {@code long}，则抛出异常。
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return 结果
     * @throws ArithmeticException 如果结果溢出 long
     * @since 1.8
     */
    public static long multiplyExact(long x, long y) {
        long r = x * y;
        long ax = Math.abs(x);
        long ay = Math.abs(y);
        if (((ax | ay) >>> 31 != 0)) {
            // 一些大于 2^31 的位可能会导致溢出
            // 通过除法操作检查结果
            // 并检查 Long.MIN_VALUE * -1 的特殊情况
           if (((y != 0) && (r / y != x)) ||
               (x == Long.MIN_VALUE && y == -1)) {
                throw new ArithmeticException("long overflow");
            }
        }
        return r;
    }

    /**
     * 返回参数加一的结果，如果结果溢出 {@code int}，则抛出异常。
     *
     * @param a 要增加的值
     * @return 结果
     * @throws ArithmeticException 如果结果溢出 int
     * @since 1.8
     */
    public static int incrementExact(int a) {
        if (a == Integer.MAX_VALUE) {
            throw new ArithmeticException("integer overflow");
        }

        return a + 1;
    }

    /**
     * 返回参数加一的结果，如果结果溢出 {@code long}，则抛出异常。
     *
     * @param a 要增加的值
     * @return 结果
     * @throws ArithmeticException 如果结果溢出 long
     * @since 1.8
     */
    public static long incrementExact(long a) {
        if (a == Long.MAX_VALUE) {
            throw new ArithmeticException("long overflow");
        }

        return a + 1L;
    }

    /**
     * 返回参数减一的结果，如果结果溢出 {@code int}，则抛出异常。
     *
     * @param a 要减少的值
     * @return 结果
     * @throws ArithmeticException 如果结果溢出 int
     * @since 1.8
     */
    public static int decrementExact(int a) {
        if (a == Integer.MIN_VALUE) {
            throw new ArithmeticException("integer overflow");
        }

        return a - 1;
    }

    /**
     * 返回参数减一的结果，如果结果溢出 {@code long}，则抛出异常。
     *
     * @param a 要减少的值
     * @return 结果
     * @throws ArithmeticException 如果结果溢出 long
     * @since 1.8
     */
    public static long decrementExact(long a) {
        if (a == Long.MIN_VALUE) {
            throw new ArithmeticException("long overflow");
        }

        return a - 1L;
    }

    /**
     * 返回参数的相反数，如果结果溢出 {@code int}，则抛出异常。
     *
     * @param a 要取反的值
     * @return 结果
     * @throws ArithmeticException 如果结果溢出 int
     * @since 1.8
     */
    public static int negateExact(int a) {
        if (a == Integer.MIN_VALUE) {
            throw new ArithmeticException("integer overflow");
        }

        return -a;
    }

    /**
     * 返回参数的相反数，如果结果溢出 {@code long}，则抛出异常。
     *
     * @param a 要取反的值
     * @return 结果
     * @throws ArithmeticException 如果结果溢出 long
     * @since 1.8
     */
    public static long negateExact(long a) {
        if (a == Long.MIN_VALUE) {
            throw new ArithmeticException("long overflow");
        }

        return -a;
    }

    /**
     * 返回 {@code long} 参数的值；
     * 如果值溢出 {@code int}，则抛出异常。
     *
     * @param value long 值
     * @return 参数作为 int
     * @throws ArithmeticException 如果参数溢出 int
     * @since 1.8
     */
    public static int toIntExact(long value) {
        if ((int)value != value) {
            throw new ArithmeticException("integer overflow");
        }
        return (int)value;
    }

    /**
     * 返回小于或等于代数商的最大（最接近正无穷）的 {@code int} 值。
     * 有一个特殊情况，如果被除数是 {@linkplain Integer#MIN_VALUE Integer.MIN_VALUE} 且除数是 {@code -1}，
     * 则发生整数溢出，结果等于 {@code Integer.MIN_VALUE}。
     * <p>
     * 普通整数除法在舍入到零的舍入模式下（截断）操作。此操作则在舍入到负无穷（向下取整）的舍入模式下进行。
     * 向下取整的舍入模式在精确结果为负时给出与截断不同的结果。
     * <ul>
     *   <li>如果参数的符号相同，则 {@code floorDiv} 和 {@code /} 操作符的结果相同。 <br>
     *       例如，{@code floorDiv(4, 3) == 1} 和 {@code (4 / 3) == 1}。</li>
     *   <li>如果参数的符号不同，商为负数，
     *       {@code floorDiv} 返回小于或等于商的整数，而 {@code /} 操作符返回最接近零的整数。<br>
     *       例如，{@code floorDiv(-4, 3) == -2}，
     *       而 {@code (-4 / 3) == -1}。
     *   </li>
     * </ul>
     * <p>
     *
     * @param x 被除数
     * @param y 除数
     * @return 小于或等于代数商的最大（最接近正无穷）的 {@code int} 值。
     * @throws ArithmeticException 如果除数 {@code y} 为零
     * @see #floorMod(int, int)
     * @see #floor(double)
     * @since 1.8
     */
    public static int floorDiv(int x, int y) {
        int r = x / y;
        // 如果符号不同且模不为零，则向下取整
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

    /**
     * 返回小于或等于代数商的最大（最接近正无穷）的 {@code long} 值。
     * 有一个特殊情况，如果被除数是 {@linkplain Long#MIN_VALUE Long.MIN_VALUE} 且除数是 {@code -1}，
     * 则发生整数溢出，结果等于 {@code Long.MIN_VALUE}。
     * <p>
     * 普通整数除法在舍入到零的舍入模式下（截断）操作。此操作则在舍入到负无穷（向下取整）的舍入模式下进行。
     * 向下取整的舍入模式在精确结果为负时给出与截断不同的结果。
     * <p>
     * 有关示例，请参见 {@link #floorDiv(int, int)}。
     *
     * @param x 被除数
     * @param y 除数
     * @return 小于或等于代数商的最大（最接近正无穷）的 {@code long} 值。
     * @throws ArithmeticException 如果除数 {@code y} 为零
     * @see #floorMod(long, long)
     * @see #floor(double)
     * @since 1.8
     */
    public static long floorDiv(long x, long y) {
        long r = x / y;
        // 如果符号不同且模不为零，则向下取整
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

    /**
     * 返回 {@code int} 参数的向下取整模。
     * <p>
     * 向下取整模是 {@code x - (floorDiv(x, y) * y)}，与除数 {@code y} 的符号相同，
     * 且在 {@code -abs(y) < r < +abs(y)} 的范围内。
     *
     * <p>
     * {@code floorDiv} 和 {@code floorMod} 之间的关系如下：
     * <ul>
     *   <li>{@code floorDiv(x, y) * y + floorMod(x, y) == x}
     * </ul>
     * <p>
     * {@code floorMod} 和 {@code %} 操作符之间的值差异是由于
     * {@code floorDiv} 返回小于或等于商的整数，而 {@code /} 操作符返回最接近零的整数。
     * <p>
     * 示例：
     * <ul>
     *   <li>如果参数的符号相同，则 {@code floorMod} 和 {@code %} 操作符的结果相同。  <br>
     *       <ul>
     *       <li>{@code floorMod(4, 3) == 1}; &nbsp; 而 {@code (4 % 3) == 1}</li>
     *       </ul>
     *   <li>如果参数的符号不同，则结果与 {@code %} 操作符不同。<br>
     *      <ul>
     *      <li>{@code floorMod(+4, -3) == -2}; &nbsp; 而 {@code (+4 % -3) == +1} </li>
     *      <li>{@code floorMod(-4, +3) == +2}; &nbsp; 而 {@code (-4 % +3) == -1} </li>
     *      <li>{@code floorMod(-4, -3) == -1}; &nbsp; 而 {@code (-4 % -3) == -1 } </li>
     *      </ul>
     *   </li>
     * </ul>
     * <p>
     * 如果参数的符号未知且需要正的模，则可以计算为 {@code (floorMod(x, y) + abs(y)) % abs(y)}。
     *
     * @param x 被除数
     * @param y 除数
     * @return 向下取整模 {@code x - (floorDiv(x, y) * y)}
     * @throws ArithmeticException 如果除数 {@code y} 为零
     * @see #floorDiv(int, int)
     * @since 1.8
     */
    public static int floorMod(int x, int y) {
        int r = x - floorDiv(x, y) * y;
        return r;
    }

    /**
     * 返回 {@code long} 参数的向下取整模。
     * <p>
     * 向下取整模是 {@code x - (floorDiv(x, y) * y)}，与除数 {@code y} 的符号相同，
     * 且在 {@code -abs(y) < r < +abs(y)} 的范围内。
     *
     * <p>
     * {@code floorDiv} 和 {@code floorMod} 之间的关系如下：
     * <ul>
     *   <li>{@code floorDiv(x, y) * y + floorMod(x, y) == x}
     * </ul>
     * <p>
     * 有关示例，请参见 {@link #floorMod(int, int)}。
     *
     * @param x 被除数
     * @param y 除数
     * @return 向下取整模 {@code x - (floorDiv(x, y) * y)}
     * @throws ArithmeticException 如果除数 {@code y} 为零
     * @see #floorDiv(long, long)
     * @since 1.8
     */
    public static long floorMod(long x, long y) {
        return x - floorDiv(x, y) * y;
    }

    /**
     * 返回一个 {@code int} 值的绝对值。
     * 如果参数不是负数，则返回参数本身。
     * 如果参数是负数，则返回参数的相反数。
     *
     * <p>注意，如果参数等于 {@link Integer#MIN_VALUE}，即最小的可表示的 {@code int} 值，结果是该相同的值，即负数。
     *
     * @param   a   要确定其绝对值的参数
     * @return  参数的绝对值。
     */
    public static int abs(int a) {
        return (a < 0) ? -a : a;
    }


                /**
     * 返回一个 {@code long} 值的绝对值。
     * 如果参数不是负数，则返回该参数。
     * 如果参数是负数，则返回该参数的相反数。
     *
     * <p>注意，如果参数等于
     * {@link Long#MIN_VALUE}，即最小的可表示的
     * {@code long} 值，结果是相同的值，即
     * 是负数。
     *
     * @param   a   要确定其绝对值的参数
     * @return  参数的绝对值。
     */
    public static long abs(long a) {
        return (a < 0) ? -a : a;
    }

    /**
     * 返回一个 {@code float} 值的绝对值。
     * 如果参数不是负数，则返回该参数。
     * 如果参数是负数，则返回该参数的相反数。
     * 特殊情况：
     * <ul><li>如果参数是正零或负零，结果是正零。
     * <li>如果参数是无穷大，结果是正无穷大。
     * <li>如果参数是 NaN，结果是 NaN。</ul>
     * 换句话说，结果与以下表达式的值相同：
     * <p>{@code Float.intBitsToFloat(0x7fffffff & Float.floatToIntBits(a))}
     *
     * @param   a   要确定其绝对值的参数
     * @return  参数的绝对值。
     */
    public static float abs(float a) {
        return (a <= 0.0F) ? 0.0F - a : a;
    }

    /**
     * 返回一个 {@code double} 值的绝对值。
     * 如果参数不是负数，则返回该参数。
     * 如果参数是负数，则返回该参数的相反数。
     * 特殊情况：
     * <ul><li>如果参数是正零或负零，结果
     * 是正零。
     * <li>如果参数是无穷大，结果是正无穷大。
     * <li>如果参数是 NaN，结果是 NaN。</ul>
     * 换句话说，结果与以下表达式的值相同：
     * <p>{@code Double.longBitsToDouble((Double.doubleToLongBits(a)<<1)>>>1)}
     *
     * @param   a   要确定其绝对值的参数
     * @return  参数的绝对值。
     */
    public static double abs(double a) {
        return (a <= 0.0D) ? 0.0D - a : a;
    }

    /**
     * 返回两个 {@code int} 值中较大的一个。也就是说，
     * 结果是更接近 {@link Integer#MAX_VALUE} 值的参数。如果参数值相同，
     * 结果是相同的值。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较大的 {@code a} 和 {@code b}。
     */
    public static int max(int a, int b) {
        return (a >= b) ? a : b;
    }

    /**
     * 返回两个 {@code long} 值中较大的一个。也就是说，
     * 结果是更接近 {@link Long#MAX_VALUE} 值的参数。如果参数值相同，
     * 结果是相同的值。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较大的 {@code a} 和 {@code b}。
     */
    public static long max(long a, long b) {
        return (a >= b) ? a : b;
    }

    // 使用保证非 NaN 参数的原始位转换。
    private static long negativeZeroFloatBits  = Float.floatToRawIntBits(-0.0f);
    private static long negativeZeroDoubleBits = Double.doubleToRawLongBits(-0.0d);

    /**
     * 返回两个 {@code float} 值中较大的一个。也就是说，
     * 结果是更接近正无穷大的参数。如果参数值相同，结果是相同的值。
     * 如果任一值是 NaN，则结果是 NaN。与数值比较运算符不同，此方法认为
     * 负零严格小于正零。如果一个参数是正零，另一个是负零，结果是正零。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较大的 {@code a} 和 {@code b}。
     */
    public static float max(float a, float b) {
        if (a != a)
            return a;   // a 是 NaN
        if ((a == 0.0f) &&
            (b == 0.0f) &&
            (Float.floatToRawIntBits(a) == negativeZeroFloatBits)) {
            // 原始转换可以，因为 NaN 不能映射到 -0.0。
            return b;
        }
        return (a >= b) ? a : b;
    }

    /**
     * 返回两个 {@code double} 值中较大的一个。也就是说，
     * 结果是更接近正无穷大的参数。如果参数值相同，结果是相同的值。
     * 如果任一值是 NaN，则结果是 NaN。与数值比较运算符不同，此方法认为
     * 负零严格小于正零。如果一个参数是正零，另一个是负零，结果是正零。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较大的 {@code a} 和 {@code b}。
     */
    public static double max(double a, double b) {
        if (a != a)
            return a;   // a 是 NaN
        if ((a == 0.0d) &&
            (b == 0.0d) &&
            (Double.doubleToRawLongBits(a) == negativeZeroDoubleBits)) {
            // 原始转换可以，因为 NaN 不能映射到 -0.0。
            return b;
        }
        return (a >= b) ? a : b;
    }

    /**
     * 返回两个 {@code int} 值中较小的一个。也就是说，
     * 结果是更接近 {@link Integer#MIN_VALUE} 值的参数。如果参数值相同，
     * 结果是相同的值。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较小的 {@code a} 和 {@code b}。
     */
    public static int min(int a, int b) {
        return (a <= b) ? a : b;
    }

    /**
     * 返回两个 {@code long} 值中较小的一个。也就是说，
     * 结果是更接近 {@link Long#MIN_VALUE} 值的参数。如果参数值相同，
     * 结果是相同的值。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较小的 {@code a} 和 {@code b}。
     */
    public static long min(long a, long b) {
        return (a <= b) ? a : b;
    }

    /**
     * 返回两个 {@code float} 值中较小的一个。也就是说，
     * 结果是更接近负无穷大的值。如果参数值相同，结果是相同的值。
     * 如果任一值是 NaN，则结果是 NaN。与数值比较运算符不同，此方法认为
     * 负零严格小于正零。如果一个参数是正零，另一个是负零，结果是负零。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较小的 {@code a} 和 {@code b}。
     */
    public static float min(float a, float b) {
        if (a != a)
            return a;   // a 是 NaN
        if ((a == 0.0f) &&
            (b == 0.0f) &&
            (Float.floatToRawIntBits(b) == negativeZeroFloatBits)) {
            // 原始转换可以，因为 NaN 不能映射到 -0.0。
            return b;
        }
        return (a <= b) ? a : b;
    }

    /**
     * 返回两个 {@code double} 值中较小的一个。也就是说，
     * 结果是更接近负无穷大的值。如果参数值相同，结果是相同的值。
     * 如果任一值是 NaN，则结果是 NaN。与数值比较运算符不同，此方法认为
     * 负零严格小于正零。如果一个参数是正零，另一个是负零，结果是负零。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较小的 {@code a} 和 {@code b}。
     */
    public static double min(double a, double b) {
        if (a != a)
            return a;   // a 是 NaN
        if ((a == 0.0d) &&
            (b == 0.0d) &&
            (Double.doubleToRawLongBits(b) == negativeZeroDoubleBits)) {
            // 原始转换可以，因为 NaN 不能映射到 -0.0。
            return b;
        }
        return (a <= b) ? a : b;
    }

    /**
     * 返回参数的 ulp 大小。一个 ulp，即最后一位的单位，是一个 {@code double} 值
     * 与其下一个更大的值之间的正距离。注意，对于非 NaN 的
     * <i>x</i>，<code>ulp(-<i>x</i>) == ulp(<i>x</i>)</code>。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，结果是 NaN。
     * <li> 如果参数是正无穷大或负无穷大，结果是正无穷大。
     * <li> 如果参数是正零或负零，结果是
     * {@code Double.MIN_VALUE}。
     * <li> 如果参数是 &plusmn;{@code Double.MAX_VALUE}，则
     * 结果等于 2<sup>971</sup>。
     * </ul>
     *
     * @param d 要返回其 ulp 的浮点值
     * @return 参数的 ulp 大小
     * @author Joseph D. Darcy
     * @since 1.5
     */
    public static double ulp(double d) {
        int exp = getExponent(d);

        switch(exp) {
        case DoubleConsts.MAX_EXPONENT+1:       // NaN 或无穷大
            return Math.abs(d);

        case DoubleConsts.MIN_EXPONENT-1:       // 零或次正规
            return Double.MIN_VALUE;

        default:
            assert exp <= DoubleConsts.MAX_EXPONENT && exp >= DoubleConsts.MIN_EXPONENT;

            // ulp(x) 通常是 2^(SIGNIFICAND_WIDTH-1)*(2^ilogb(x))
            exp = exp - (DoubleConsts.SIGNIFICAND_WIDTH-1);
            if (exp >= DoubleConsts.MIN_EXPONENT) {
                return powerOfTwoD(exp);
            }
            else {
                // 返回次正规结果；将 Double.MIN_VALUE 的整数表示
                // 适当左移
                return Double.longBitsToDouble(1L <<
                (exp - (DoubleConsts.MIN_EXPONENT - (DoubleConsts.SIGNIFICAND_WIDTH-1)) ));
            }
        }
    }

    /**
     * 返回参数的 ulp 大小。一个 ulp，即最后一位的单位，是一个 {@code float} 值
     * 与其下一个更大的值之间的正距离。注意，对于非 NaN 的
     * <i>x</i>，<code>ulp(-<i>x</i>) == ulp(<i>x</i>)</code>。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，结果是 NaN。
     * <li> 如果参数是正无穷大或负无穷大，结果是正无穷大。
     * <li> 如果参数是正零或负零，结果是
     * {@code Float.MIN_VALUE}。
     * <li> 如果参数是 &plusmn;{@code Float.MAX_VALUE}，则
     * 结果等于 2<sup>104</sup>。
     * </ul>
     *
     * @param f 要返回其 ulp 的浮点值
     * @return 参数的 ulp 大小
     * @author Joseph D. Darcy
     * @since 1.5
     */
    public static float ulp(float f) {
        int exp = getExponent(f);

        switch(exp) {
        case FloatConsts.MAX_EXPONENT+1:        // NaN 或无穷大
            return Math.abs(f);

        case FloatConsts.MIN_EXPONENT-1:        // 零或次正规
            return FloatConsts.MIN_VALUE;

        default:
            assert exp <= FloatConsts.MAX_EXPONENT && exp >= FloatConsts.MIN_EXPONENT;

            // ulp(x) 通常是 2^(SIGNIFICAND_WIDTH-1)*(2^ilogb(x))
            exp = exp - (FloatConsts.SIGNIFICAND_WIDTH-1);
            if (exp >= FloatConsts.MIN_EXPONENT) {
                return powerOfTwoF(exp);
            }
            else {
                // 返回次正规结果；将 FloatConsts.MIN_VALUE 的整数表示
                // 适当左移
                return Float.intBitsToFloat(1 <<
                (exp - (FloatConsts.MIN_EXPONENT - (FloatConsts.SIGNIFICAND_WIDTH-1)) ));
            }
        }
    }

    /**
     * 返回参数的符号函数；如果参数为零，返回零；如果参数大于零，返回 1.0；如果参数小于零，返回 -1.0。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，结果是 NaN。
     * <li> 如果参数是正零或负零，结果与参数相同。
     * </ul>
     *
     * @param d 要返回其符号函数的浮点值
     * @return 参数的符号函数
     * @author Joseph D. Darcy
     * @since 1.5
     */
    public static double signum(double d) {
        return (d == 0.0 || Double.isNaN(d))?d:copySign(1.0, d);
    }

    /**
     * 返回参数的符号函数；如果参数为零，返回零；如果参数大于零，返回 1.0f；如果参数小于零，返回 -1.0f。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，结果是 NaN。
     * <li> 如果参数是正零或负零，结果与参数相同。
     * </ul>
     *
     * @param f 要返回其符号函数的浮点值
     * @return 参数的符号函数
     * @author Joseph D. Darcy
     * @since 1.5
     */
    public static float signum(float f) {
        return (f == 0.0f || Float.isNaN(f))?f:copySign(1.0f, f);
    }

    /**
     * 返回一个 {@code double} 值的双曲正弦。
     * <i>x</i> 的双曲正弦定义为
     * (<i>e<sup>x</sup>&nbsp;-&nbsp;e<sup>-x</sup></i>)/2
     * 其中 <i>e</i> 是 {@linkplain Math#E 欧拉数}。
     *
     * <p>特殊情况：
     * <ul>
     *
     * <li>如果参数是 NaN，结果是 NaN。
     *
     * <li>如果参数是无穷大，结果是与参数同符号的无穷大。
     *
     * <li>如果参数是零，结果是与参数同符号的零。
     *
     * </ul>
     *
     * <p>计算结果必须在精确结果的 2.5 ulps 之内。
     *
     * @param   x 要返回其双曲正弦的数。
     * @return  {@code x} 的双曲正弦。
     * @since 1.5
     */
    public static double sinh(double x) {
        return StrictMath.sinh(x);
    }

    /**
     * 返回一个 {@code double} 值的双曲余弦。
     * <i>x</i> 的双曲余弦定义为
     * (<i>e<sup>x</sup>&nbsp;+&nbsp;e<sup>-x</sup></i>)/2
     * 其中 <i>e</i> 是 {@linkplain Math#E 欧拉数}。
     *
     * <p>特殊情况：
     * <ul>
     *
     * <li>如果参数是 NaN，结果是 NaN。
     *
     * <li>如果参数是无穷大，结果是正无穷大。
     *
     * <li>如果参数是零，结果是 {@code 1.0}。
     *
     * </ul>
     *
     * <p>计算结果必须在精确结果的 2.5 ulps 之内。
     *
     * @param   x 要返回其双曲余弦的数。
     * @return  {@code x} 的双曲余弦。
     * @since 1.5
     */
    public static double cosh(double x) {
        return StrictMath.cosh(x);
    }


                /**
     * 返回一个 {@code double} 值的双曲正切值。
     * x 的双曲正切定义为
     * (e^x - e^-x) / (e^x + e^-x)，
     * 换句话说，即 {@linkplain Math#sinh
     * sinh(x)}/{@linkplain Math#cosh cosh(x)}。注意
     * 精确 tanh 的绝对值总是小于
     * 1。
     *
     * <p>特殊情况：
     * <ul>
     *
     * <li>如果参数是 NaN，则结果是 NaN。
     *
     * <li>如果参数是零，则结果是一个与参数符号相同的零。
     *
     * <li>如果参数是正无穷大，则结果是
     * {@code +1.0}。
     *
     * <li>如果参数是负无穷大，则结果是
     * {@code -1.0}。
     *
     * </ul>
     *
     * <p>计算结果必须在精确结果的 2.5 ulps 之内。
     * 对于任何有限输入，{@code tanh} 的结果
     * 的绝对值必须小于或等于 1。注意一旦
     * tanh 的精确结果在 &plusmn;1 的 1/2 ulp 之内，
     * 应该返回正确符号的 &plusmn;{@code 1.0}。
     *
     * @param   x 要返回其双曲正切的数字。
     * @return  x 的双曲正切。
     * @since 1.5
     */
    public static double tanh(double x) {
        return StrictMath.tanh(x);
    }

    /**
     * 返回 sqrt(x^2 + y^2) 而不发生中间溢出或下溢。
     *
     * <p>特殊情况：
     * <ul>
     *
     * <li> 如果任一参数是无穷大，则结果
     * 是正无穷大。
     *
     * <li> 如果任一参数是 NaN 且任一参数不是无穷大，
     * 则结果是 NaN。
     *
     * </ul>
     *
     * <p>计算结果必须在精确结果的 1 ulp 之内。
     * 如果一个参数保持不变，结果必须在另一个参数中
     * 半单调。
     *
     * @param x 一个值
     * @param y 一个值
     * @return sqrt(x^2 + y^2) 而不发生中间溢出或下溢
     * @since 1.5
     */
    public static double hypot(double x, double y) {
        return StrictMath.hypot(x, y);
    }

    /**
     * 返回 e^x - 1。注意对于接近 0 的 x 值，
     * {@code expm1(x)} + 1 的精确和比
     * {@code exp(x)} 更接近 e^x 的真实结果。
     *
     * <p>特殊情况：
     * <ul>
     * <li>如果参数是 NaN，结果是 NaN。
     *
     * <li>如果参数是正无穷大，则结果是
     * 正无穷大。
     *
     * <li>如果参数是负无穷大，则结果是
     * -1.0。
     *
     * <li>如果参数是零，则结果是一个与参数符号相同的零。
     *
     * </ul>
     *
     * <p>计算结果必须在精确结果的 1 ulp 之内。
     * 结果必须是半单调的。对于任何有限输入，
     * {@code expm1} 的结果必须大于或
     * 等于 {@code -1.0}。注意一旦 e^x - 1 的精确结果
     * 在 -1 的 1/2 ulp 之内，应该返回
     * {@code -1.0}。
     *
     * @param   x   用于计算 e^x - 1 的指数。
     * @return  e^x - 1 的值。
     * @since 1.5
     */
    public static double expm1(double x) {
        return StrictMath.expm1(x);
    }

    /**
     * 返回参数与 1 之和的自然对数。
     * 注意对于小值 {@code x}，{@code log1p(x)} 的结果
     * 比浮点计算 {@code log(1.0 + x)} 更接近 ln(1 + x) 的真实结果。
     *
     * <p>特殊情况：
     *
     * <ul>
     *
     * <li>如果参数是 NaN 或小于 -1，则结果是
     * NaN。
     *
     * <li>如果参数是正无穷大，则结果是
     * 正无穷大。
     *
     * <li>如果参数是负一，则结果是
     * 负无穷大。
     *
     * <li>如果参数是零，则结果是一个与参数符号相同的零。
     *
     * </ul>
     *
     * <p>计算结果必须在精确结果的 1 ulp 之内。
     * 结果必须是半单调的。
     *
     * @param   x   一个值
     * @return ln(x + 1)，即 x + 1 的自然对数
     * @since 1.5
     */
    public static double log1p(double x) {
        return StrictMath.log1p(x);
    }

    /**
     * 返回具有第二个浮点参数符号的第一个浮点参数。注意与 {@link
     * StrictMath#copySign(double, double) StrictMath.copySign}
     * 方法不同，此方法不要求 NaN {@code sign}
     * 参数被视为正数；允许实现将某些 NaN 参数视为正数，将其他 NaN 参数视为负数以提高性能。
     *
     * @param magnitude  提供结果大小的参数
     * @param sign   提供结果符号的参数
     * @return 一个具有 {@code magnitude} 的大小和 {@code sign} 的符号的值。
     * @since 1.6
     */
    public static double copySign(double magnitude, double sign) {
        return Double.longBitsToDouble((Double.doubleToRawLongBits(sign) &
                                        (DoubleConsts.SIGN_BIT_MASK)) |
                                       (Double.doubleToRawLongBits(magnitude) &
                                        (DoubleConsts.EXP_BIT_MASK |
                                         DoubleConsts.SIGNIF_BIT_MASK)));
    }

    /**
     * 返回具有第二个浮点参数符号的第一个浮点参数。注意与 {@link
     * StrictMath#copySign(float, float) StrictMath.copySign}
     * 方法不同，此方法不要求 NaN {@code sign}
     * 参数被视为正数；允许实现将某些 NaN 参数视为正数，将其他 NaN 参数视为负数以提高性能。
     *
     * @param magnitude  提供结果大小的参数
     * @param sign   提供结果符号的参数
     * @return 一个具有 {@code magnitude} 的大小和 {@code sign} 的符号的值。
     * @since 1.6
     */
    public static float copySign(float magnitude, float sign) {
        return Float.intBitsToFloat((Float.floatToRawIntBits(sign) &
                                     (FloatConsts.SIGN_BIT_MASK)) |
                                    (Float.floatToRawIntBits(magnitude) &
                                     (FloatConsts.EXP_BIT_MASK |
                                      FloatConsts.SIGNIF_BIT_MASK)));
    }

    /**
     * 返回用于表示 {@code float} 的无偏指数。特殊情况：
     *
     * <ul>
     * <li>如果参数是 NaN 或无穷大，则结果是
     * {@link Float#MAX_EXPONENT} + 1。
     * <li>如果参数是零或次正规数，则结果是
     * {@link Float#MIN_EXPONENT} -1。
     * </ul>
     * @param f 一个 {@code float} 值
     * @return 参数的无偏指数
     * @since 1.6
     */
    public static int getExponent(float f) {
        /*
         * 将 f 位转换为整数，屏蔽出指数位，向右移位，然后减去
         * 浮点数的偏移调整以获得真实的指数值
         */
        return ((Float.floatToRawIntBits(f) & FloatConsts.EXP_BIT_MASK) >>
                (FloatConsts.SIGNIFICAND_WIDTH - 1)) - FloatConsts.EXP_BIAS;
    }

    /**
     * 返回用于表示 {@code double} 的无偏指数。特殊情况：
     *
     * <ul>
     * <li>如果参数是 NaN 或无穷大，则结果是
     * {@link Double#MAX_EXPONENT} + 1。
     * <li>如果参数是零或次正规数，则结果是
     * {@link Double#MIN_EXPONENT} -1。
     * </ul>
     * @param d 一个 {@code double} 值
     * @return 参数的无偏指数
     * @since 1.6
     */
    public static int getExponent(double d) {
        /*
         * 将 d 位转换为长整数，屏蔽出指数位，向右移位，然后减去
         * 双精度数的偏移调整以获得真实的指数值。
         */
        return (int)(((Double.doubleToRawLongBits(d) & DoubleConsts.EXP_BIT_MASK) >>
                      (DoubleConsts.SIGNIFICAND_WIDTH - 1)) - DoubleConsts.EXP_BIAS);
    }

    /**
     * 返回第一个参数在第二个参数方向上的相邻浮点数。如果两个参数相等，则返回第二个参数。
     *
     * <p>
     * 特殊情况：
     * <ul>
     * <li> 如果任一参数是 NaN，则返回 NaN。
     *
     * <li> 如果两个参数都是带符号的零，返回 {@code direction}
     * 不变（根据在参数相等时返回第二个参数的要求）。
     *
     * <li> 如果 {@code start} 是
     * &plusmn;{@link Double#MIN_VALUE} 且 {@code direction}
     * 有值使得结果应具有更小的大小，则返回与 {@code start}
     * 符号相同的零。
     *
     * <li> 如果 {@code start} 是无穷大且
     * {@code direction} 有值使得结果应具有更小的大小，则返回与 {@code start}
     * 符号相同的 {@link Double#MAX_VALUE}。
     *
     * <li> 如果 {@code start} 等于 &plusmn;
     * {@link Double#MAX_VALUE} 且 {@code direction} 有
     * 值使得结果应具有更大的大小，则返回与 {@code start}
     * 符号相同的无穷大。
     * </ul>
     *
     * @param start  起始浮点值
     * @param direction 指示应返回 {@code start} 的哪个邻居或 {@code start} 本身的值
     * @return 与 {@code start} 相邻且在 {@code direction} 方向上的浮点数。
     * @since 1.6
     */
    public static double nextAfter(double start, double direction) {
        /*
         * 情况：
         *
         * nextAfter(+infinity, 0)  == MAX_VALUE
         * nextAfter(+infinity, +infinity)  == +infinity
         * nextAfter(-infinity, 0)  == -MAX_VALUE
         * nextAfter(-infinity, -infinity)  == -infinity
         *
         * 这些情况可以自然处理，无需额外测试
         */

        // 首先检查 NaN 值
        if (Double.isNaN(start) || Double.isNaN(direction)) {
            // 返回由输入 NaN(s) 导出的 NaN
            return start + direction;
        } else if (start == direction) {
            return direction;
        } else {        // start > direction 或 start < direction
            // 添加 +0.0 以消除 -0.0 (+0.0 + -0.0 => +0.0)
            // 然后将 start 位转换为整数。
            long transducer = Double.doubleToRawLongBits(start + 0.0d);

            /*
             * IEEE 754 浮点数如果被视为带符号的整数，则是字典序排列的。
             * 由于 Java 的整数是二进制补码，
             * 对逻辑上为负的浮点值的表示“递增”实际上会
             * *递减* 带符号的大小表示。因此，当浮点值的整数表示
             * 小于零时，对表示的调整方向与最初预期的相反。
             */
            if (direction > start) { // 计算下一个更大的值
                transducer = transducer + (transducer >= 0L ? 1L : -1L);
            } else  { // 计算下一个更小的值
                assert direction < start;
                if (transducer > 0L)
                    --transducer;
                else
                    if (transducer < 0L )
                        ++transducer;
                    /*
                     * transducer==0，结果是 -MIN_VALUE
                     *
                     * 从零（隐式正数）到最小负
                     * 带符号大小值的转换必须显式完成。
                     */
                    else
                        transducer = DoubleConsts.SIGN_BIT_MASK | 1L;
            }

            return Double.longBitsToDouble(transducer);
        }
    }

    /**
     * 返回第一个参数在第二个参数方向上的相邻浮点数。如果两个参数相等，则返回与第二个参数等价的值。
     *
     * <p>
     * 特殊情况：
     * <ul>
     * <li> 如果任一参数是 NaN，则返回 NaN。
     *
     * <li> 如果两个参数都是带符号的零，返回与 {@code direction} 等价的值。
     *
     * <li> 如果 {@code start} 是
     * &plusmn;{@link Float#MIN_VALUE} 且 {@code direction}
     * 有值使得结果应具有更小的大小，则返回与 {@code start}
     * 符号相同的零。
     *
     * <li> 如果 {@code start} 是无穷大且
     * {@code direction} 有值使得结果应具有更小的大小，则返回与 {@code start}
     * 符号相同的 {@link Float#MAX_VALUE}。
     *
     * <li> 如果 {@code start} 等于 &plusmn;
     * {@link Float#MAX_VALUE} 且 {@code direction} 有
     * 值使得结果应具有更大的大小，则返回与 {@code start}
     * 符号相同的无穷大。
     * </ul>
     *
     * @param start  起始浮点值
     * @param direction 指示应返回 {@code start} 的哪个邻居或 {@code start} 本身的值
     * @return 与 {@code start} 相邻且在 {@code direction} 方向上的浮点数。
     * @since 1.6
     */
    public static float nextAfter(float start, double direction) {
        /*
         * 情况：
         *
         * nextAfter(+infinity, 0)  == MAX_VALUE
         * nextAfter(+infinity, +infinity)  == +infinity
         * nextAfter(-infinity, 0)  == -MAX_VALUE
         * nextAfter(-infinity, -infinity)  == -infinity
         *
         * 这些情况可以自然处理，无需额外测试
         */

        // 首先检查 NaN 值
        if (Float.isNaN(start) || Double.isNaN(direction)) {
            // 返回由输入 NaN(s) 导出的 NaN
            return start + (float)direction;
        } else if (start == direction) {
            return (float)direction;
        } else {        // start > direction 或 start < direction
            // 添加 +0.0 以消除 -0.0 (+0.0 + -0.0 => +0.0)
            // 然后将 start 位转换为整数。
            int transducer = Float.floatToRawIntBits(start + 0.0f);


                        /*
             * IEEE 754 浮点数如果被视为带符号的整数，则是字典序排列的。
             * 由于 Java 的整数是二进制补码形式，
             * 对逻辑上为负的浮点值的二进制补码表示进行“递增”操作 *会递减*
             * 带符号的整数表示。因此，当浮点值的整数表示小于零时，
             * 对表示的调整方向与最初预期的相反。
             */
            if (direction > start) { // 计算下一个更大的值
                transducer = transducer + (transducer >= 0 ? 1 : -1);
            } else { // 计算下一个更小的值
                assert direction < start;
                if (transducer > 0)
                    --transducer;
                else
                    if (transducer < 0)
                        ++transducer;
                    /*
                     * transducer == 0，结果是 -MIN_VALUE
                     *
                     * 从零（隐式正数）到最小负数
                     * 带符号的整数值的转换必须显式完成。
                     */
                    else
                        transducer = FloatConsts.SIGN_BIT_MASK | 1;
            }

            return Float.intBitsToFloat(transducer);
        }
    }

    /**
     * 返回接近 {@code d} 且方向为正无穷的浮点值。此方法在语义上等同于 {@code nextAfter(d,
     * Double.POSITIVE_INFINITY)}；然而，{@code nextUp} 实现可能比其等效的
     * {@code nextAfter} 调用运行得更快。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，结果是 NaN。
     *
     * <li> 如果参数是正无穷，结果是正无穷。
     *
     * <li> 如果参数是零，结果是
     * {@link Double#MIN_VALUE}
     *
     * </ul>
     *
     * @param d 起始浮点值
     * @return 更接近正无穷的相邻浮点值。
     * @since 1.6
     */
    public static double nextUp(double d) {
        if (Double.isNaN(d) || d == Double.POSITIVE_INFINITY)
            return d;
        else {
            d += 0.0d;
            return Double.longBitsToDouble(Double.doubleToRawLongBits(d) +
                                           ((d >= 0.0d) ? +1L : -1L));
        }
    }

    /**
     * 返回接近 {@code f} 且方向为正无穷的浮点值。此方法在语义上等同于 {@code nextAfter(f,
     * Float.POSITIVE_INFINITY)}；然而，{@code nextUp} 实现可能比其等效的
     * {@code nextAfter} 调用运行得更快。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，结果是 NaN。
     *
     * <li> 如果参数是正无穷，结果是正无穷。
     *
     * <li> 如果参数是零，结果是
     * {@link Float#MIN_VALUE}
     *
     * </ul>
     *
     * @param f 起始浮点值
     * @return 更接近正无穷的相邻浮点值。
     * @since 1.6
     */
    public static float nextUp(float f) {
        if (Float.isNaN(f) || f == FloatConsts.POSITIVE_INFINITY)
            return f;
        else {
            f += 0.0f;
            return Float.intBitsToFloat(Float.floatToRawIntBits(f) +
                                        ((f >= 0.0f) ? +1 : -1));
        }
    }

    /**
     * 返回接近 {@code d} 且方向为负无穷的浮点值。此方法在语义上等同于 {@code nextAfter(d,
     * Double.NEGATIVE_INFINITY)}；然而，{@code nextDown} 实现可能比其等效的
     * {@code nextAfter} 调用运行得更快。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，结果是 NaN。
     *
     * <li> 如果参数是负无穷，结果是负无穷。
     *
     * <li> 如果参数是零，结果是
     * {@code -Double.MIN_VALUE}
     *
     * </ul>
     *
     * @param d 起始浮点值
     * @return 更接近负无穷的相邻浮点值。
     * @since 1.8
     */
    public static double nextDown(double d) {
        if (Double.isNaN(d) || d == Double.NEGATIVE_INFINITY)
            return d;
        else {
            if (d == 0.0)
                return -Double.MIN_VALUE;
            else
                return Double.longBitsToDouble(Double.doubleToRawLongBits(d) +
                                               ((d > 0.0d) ? -1L : +1L));
        }
    }

    /**
     * 返回接近 {@code f} 且方向为负无穷的浮点值。此方法在语义上等同于 {@code nextAfter(f,
     * Float.NEGATIVE_INFINITY)}；然而，{@code nextDown} 实现可能比其等效的
     * {@code nextAfter} 调用运行得更快。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，结果是 NaN。
     *
     * <li> 如果参数是负无穷，结果是负无穷。
     *
     * <li> 如果参数是零，结果是
     * {@code -Float.MIN_VALUE}
     *
     * </ul>
     *
     * @param f 起始浮点值
     * @return 更接近负无穷的相邻浮点值。
     * @since 1.8
     */
    public static float nextDown(float f) {
        if (Float.isNaN(f) || f == Float.NEGATIVE_INFINITY)
            return f;
        else {
            if (f == 0.0f)
                return -Float.MIN_VALUE;
            else
                return Float.intBitsToFloat(Float.floatToRawIntBits(f) +
                                            ((f > 0.0f) ? -1 : +1));
        }
    }

    /**
     * 返回 {@code d} &times;
     * 2<sup>{@code scaleFactor}</sup>，结果四舍五入，如同通过一次正确四舍五入的浮点乘法计算得到。
     * 有关浮点值集的讨论，请参阅 Java 语言规范。如果结果的指数在 {@link
     * Double#MIN_EXPONENT} 和 {@link Double#MAX_EXPONENT} 之间，则结果是精确的。
     * 如果结果的指数大于 {@code Double.MAX_EXPONENT}，则返回无穷大。
     * 注意，如果结果是次正规数，精度可能会丢失；也就是说，当 {@code scalb(x, n)}
     * 是次正规数时，{@code scalb(scalb(x, n), -n)} 可能不等于
     * <i>x</i>。当结果不是 NaN 时，结果与 {@code d} 有相同的符号。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果第一个参数是 NaN，返回 NaN。
     * <li> 如果第一个参数是无穷大，则返回相同符号的无穷大。
     * <li> 如果第一个参数是零，则返回相同符号的零。
     * </ul>
     *
     * @param d 要按 2 的幂缩放的数字。
     * @param scaleFactor 用于缩放 {@code d} 的 2 的幂。
     * @return {@code d} &times; 2<sup>{@code scaleFactor}</sup>
     * @since 1.6
     */
    public static double scalb(double d, int scaleFactor) {
        /*
         * 该方法不需要声明为 strictfp 以在所有平台上计算相同的结果。当放大时，乘法-存储操作的顺序无关紧要；
         * 结果将是有限的或溢出，无论操作顺序如何。然而，为了在缩小时获得正确结果，必须使用特定的顺序。
         *
         * 当缩小时，乘法-存储操作的顺序确保不会出现两次连续的乘法-存储操作返回次正规结果。如果一次乘法-存储结果是次正规的，
         * 下一次乘法将舍入为零。这是通过首先乘以 2 ^ (scaleFactor % n)，然后根据需要多次乘以 2^n 来完成的，
         * 其中 n 是方便的 2 的幂的指数。这样，最多只会发生一次真正的舍入误差。如果仅使用双精度值集，
         * 舍入将在乘法时发生。如果使用双精度-扩展指数值集，乘积可能是精确的，但存储到 d 时会保证舍入到双精度值集。
         *
         * 不能首先将 d 乘以 2^MIN_EXPONENT，然后乘以 2 ^ (scaleFactor %
         * MIN_EXPONENT)，因为在 strictfp 程序中，下溢时可能会发生两次舍入；例如，如果 scaleFactor
         * 参数是 (MIN_EXPONENT - n) 且 d 的指数略小于 -(MIN_EXPONENT - n)，这意味着最终结果将是次正规的。
         *
         * 由于可以实现完全可重现的此方法而不会带来任何不必要的性能负担，因此没有充分的理由允许在 scalb 中下溢时发生两次舍入。
         */

        // 一个 2 的幂的大小，使得将有限的非零值按其缩放肯定会溢出或下溢；由于舍入，缩小时会额外取一个 2 的幂，这在这里反映出来
        final int MAX_SCALE = DoubleConsts.MAX_EXPONENT + -DoubleConsts.MIN_EXPONENT +
                              DoubleConsts.SIGNIFICAND_WIDTH + 1;
        int exp_adjust = 0;
        int scale_increment = 0;
        double exp_delta = Double.NaN;

        // 确保缩放因子在合理的范围内

        if (scaleFactor < 0) {
            scaleFactor = Math.max(scaleFactor, -MAX_SCALE);
            scale_increment = -512;
            exp_delta = twoToTheDoubleScaleDown;
        } else {
            scaleFactor = Math.min(scaleFactor, MAX_SCALE);
            scale_increment = 512;
            exp_delta = twoToTheDoubleScaleUp;
        }

        // 计算 (scaleFactor % +/-512)，512 = 2^9，使用 "Hacker's Delight" 第 10-2 节的技术。
        int t = (scaleFactor >> 9 - 1) >>> 32 - 9;
        exp_adjust = ((scaleFactor + t) & (512 - 1)) - t;

        d *= powerOfTwoD(exp_adjust);
        scaleFactor -= exp_adjust;

        while (scaleFactor != 0) {
            d *= exp_delta;
            scaleFactor -= scale_increment;
        }
        return d;
    }

    /**
     * 返回 {@code f} &times;
     * 2<sup>{@code scaleFactor}</sup>，结果四舍五入，如同通过一次正确四舍五入的浮点乘法计算得到。
     * 有关浮点值集的讨论，请参阅 Java 语言规范。如果结果的指数在 {@link
     * Float#MIN_EXPONENT} 和 {@link Float#MAX_EXPONENT} 之间，则结果是精确的。
     * 如果结果的指数大于 {@code Float.MAX_EXPONENT}，则返回无穷大。
     * 注意，如果结果是次正规数，精度可能会丢失；也就是说，当 {@code scalb(x, n)}
     * 是次正规数时，{@code scalb(scalb(x, n), -n)} 可能不等于
     * <i>x</i>。当结果不是 NaN 时，结果与 {@code f} 有相同的符号。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果第一个参数是 NaN，返回 NaN。
     * <li> 如果第一个参数是无穷大，则返回相同符号的无穷大。
     * <li> 如果第一个参数是零，则返回相同符号的零。
     * </ul>
     *
     * @param f 要按 2 的幂缩放的数字。
     * @param scaleFactor 用于缩放 {@code f} 的 2 的幂。
     * @return {@code f} &times; 2<sup>{@code scaleFactor}</sup>
     * @since 1.6
     */
    public static float scalb(float f, int scaleFactor) {
        // 一个 2 的幂的大小，使得将有限的非零值按其缩放肯定会溢出或下溢；由于舍入，缩小时会额外取一个 2 的幂，这在这里反映出来
        final int MAX_SCALE = FloatConsts.MAX_EXPONENT + -FloatConsts.MIN_EXPONENT +
                              FloatConsts.SIGNIFICAND_WIDTH + 1;

        // 确保缩放因子在合理的范围内
        scaleFactor = Math.max(Math.min(scaleFactor, MAX_SCALE), -MAX_SCALE);

        /*
         * 由于 + MAX_SCALE 对于 float 能很好地适应双精度指数范围，并且 + float -> double 转换是精确的，
         * 下面的乘法将是精确的。因此，当双精度乘积转换为 float 时发生的舍入将是正确的 float 结果。
         * 由于所有其他操作都是精确的，因此没有必要将此方法声明为 strictfp。
         */
        return (float) ((double) f * powerOfTwoD(scaleFactor));
    }

    // 用于 scalb 的常量
    static double twoToTheDoubleScaleUp = powerOfTwoD(512);
    static double twoToTheDoubleScaleDown = powerOfTwoD(-512);

    /**
     * 返回正常范围内的浮点 2 的幂。
     */
    static double powerOfTwoD(int n) {
        assert (n >= DoubleConsts.MIN_EXPONENT && n <= DoubleConsts.MAX_EXPONENT);
        return Double.longBitsToDouble((((long) n + (long) DoubleConsts.EXP_BIAS) <<
                                        (DoubleConsts.SIGNIFICAND_WIDTH - 1))
                                       & DoubleConsts.EXP_BIT_MASK);
    }

    /**
     * 返回正常范围内的浮点 2 的幂。
     */
    static float powerOfTwoF(int n) {
        assert (n >= FloatConsts.MIN_EXPONENT && n <= FloatConsts.MAX_EXPONENT);
        return Float.intBitsToFloat(((n + FloatConsts.EXP_BIAS) <<
                                     (FloatConsts.SIGNIFICAND_WIDTH - 1))
                                    & FloatConsts.EXP_BIT_MASK);
    }
}
