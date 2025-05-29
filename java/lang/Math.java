/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. 使用受许可条款限制。
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
 import java.util.Random;
 
 import sun.misc.FloatConsts;
 import sun.misc.DoubleConsts;
 
 /**
  * {@code Math} 类包含用于执行基本数值运算的方法，例如初等指数、对数、平方根和三角函数。
  *
  * <p>与 {@code StrictMath} 类的一些数值方法不同，{@code Math} 类的等效函数的实现不要求返回位对位相同的结果。这种宽松允许在不需要严格可重现性的情况下使用性能更高的实现。
  *
  * <p>默认情况下，许多 {@code Math} 方法只是调用 {@code StrictMath} 中的等效方法进行实现。鼓励代码生成器在可用时使用平台特定的本地库或微处理器指令，以提供 {@code Math} 方法的更高性能实现。此类高性能实现仍必须符合 {@code Math} 的规范。
  *
  * <p>实现规范的质量涉及两个属性：返回结果的精度和方法的单调性。浮点数 {@code Math} 方法的精度以 <i>ulps</i>（最后一位单位）来衡量。对于给定的浮点格式，某个具体实数值的一个 {@linkplain #ulp(double) ulp} 是围绕该数值值的两个浮点值之间的距离。当讨论方法的整体精度而不是特定参数时，引用的 ulp 数是任何参数下的最坏情况误差。如果一个方法的误差始终小于 0.5 ulp，则该方法始终返回最接近精确结果的浮点数；这样的方法是<i>正确四舍五入</i>的。正确四舍五入的方法通常是浮点近似的最佳选择；然而，对于许多浮点方法来说，正确四舍五入是不切实际的。因此，对于 {@code Math} 类，某些方法的较大误差界限允许为 1 或 2 ulp。非正式地，对于 1 ulp 的误差界限，当精确结果是可表示的数字时，应返回精确结果作为计算结果；否则，返回围绕精确结果的两个浮点值之一。对于幅度较大的精确结果，边界点之一可能是无穷大。除了单个参数的精度外，保持方法在不同参数之间的适当关系也很重要。因此，具有超过 0.5 ulp 误差的大多数方法要求是<i>半单调的</i>：当数学函数是非递减的时，浮点近似也是非递减的；同样，当数学函数是非递增的时，浮点近似也是非递增的。并非所有具有 1 ulp 精度的近似都会自动满足单调性要求。
  *
  * <p>
  * 该平台使用带符号的二进制补码整数运算，涉及 int 和 long 基本类型。开发者应选择适当的基本类型以确保算术运算始终产生正确结果，在某些情况下，这意味着运算不会溢出计算值的范围。最佳实践是选择基本类型和算法以避免溢出。在大小为 {@code int} 或 {@code long} 且需要检测溢出错误的情况下，方法 {@code addExact}、{@code subtractExact}、{@code multiplyExact} 和 {@code toIntExact} 会在结果溢出时抛出 {@code ArithmeticException}。对于其他算术运算，如除法、绝对值、递增、递减和取反，仅在特定最小或最大值时会发生溢出，应根据需要检查最小或最大值。
  *
  * @author  未署名
  * @author  Joseph D. Darcy
  * @since   JDK1.0
  */
 
 public final class Math {
 
     /**
      * 禁止任何人实例化此类。
      */
     private Math() {}
 
     /**
      * 比任何其他值更接近自然对数的基数 <i>e</i> 的 {@code double} 值。
      */
     public static final double E = 2.7182818284590452354;
 
     /**
      * 比任何其他值更接近圆周率 <i>pi</i>（圆周长与直径的比率）的 {@code double} 值。
      */
     public static final double PI = 3.14159265358979323846;
 
     /**
      * 返回一个角度的三角正弦值。特殊情况：
      * <ul><li>如果参数是 NaN 或无穷大，则结果是 NaN。
      * <li>如果参数是零，则结果是与参数同符号的零。</ul>
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。结果必须是半单调的。
      *
      * @param   a   以弧度为单位的角度。
      * @return  参数的正弦值。
      */
     public static double sin(double a) {
         return StrictMath.sin(a); // 默认实现委托给 StrictMath
     }
 
     /**
      * 返回一个角度的三角余弦值。特殊情况：
      * <ul><li>如果参数是 NaN 或无穷大，则结果是 NaN。</ul>
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。结果必须是半单调的。
      *
      * @param   a   以弧度为单位的角度。
      * @return  参数的余弦值。
      */
     public static double cos(double a) {
         return StrictMath.cos(a); // 默认实现委托给 StrictMath
     }
 
     /**
      * 返回一个角度的三角正切值。特殊情况：
      * <ul><li>如果参数是 NaN 或无穷大，则结果是 NaN。
      * <li>如果参数是零，则结果是与参数同符号的零。</ul>
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。结果必须是半单调的。
      *
      * @param   a   以弧度为单位的角度。
      * @return  参数的正切值。
      */
     public static double tan(double a) {
         return StrictMath.tan(a); // 默认实现委托给 StrictMath
     }
 
     /**
      * 返回一个值的反正弦；返回的角度在 -<i>pi</i>/2 到 <i>pi</i>/2 范围内。特殊情况：
      * <ul><li>如果参数是 NaN 或其绝对值大于 1，则结果是 NaN。
      * <li>如果参数是零，则结果是与参数同符号的零。</ul>
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。结果必须是半单调的。
      *
      * @param   a   要返回其反正弦的值。
      * @return  参数的反正弦值。
      */
     public static double asin(double a) {
         return StrictMath.asin(a); // 默认实现委托给 StrictMath
     }
 
     /**
      * 返回一个值的反余弦；返回的角度在 0.0 到 <i>pi</i> 范围内。特殊情况：
      * <ul><li>如果参数是 NaN 或其绝对值大于 1，则结果是 NaN。</ul>
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。结果必须是半单调的。
      *
      * @param   a   要返回其反余弦的值。
      * @return  参数的反余弦值。
      */
     public static double acos(double a) {
         return StrictMath.acos(a); // 默认实现委托给 StrictMath
     }
 
     /**
      * 返回一个值的反正切；返回的角度在 -<i>pi</i>/2 到 <i>pi</i>/2 范围内。特殊情况：
      * <ul><li>如果参数是 NaN，则结果是 NaN。
      * <li>如果参数是零，则结果是与参数同符号的零。</ul>
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。结果必须是半单调的。
      *
      * @param   a   要返回其反正切的值。
      * @return  参数的反正切值。
      */
     public static double atan(double a) {
         return StrictMath.atan(a); // 默认实现委托给 StrictMath
     }
 
     /**
      * 将以度为单位测量的角度转换为近似等效的以弧度为单位的角度。从度到弧度的转换通常是不精确的。
      *
      * @param   angdeg   以度为单位的角度
      * @return  以弧度为单位的 {@code angdeg} 角度的测量值。
      * @since   1.2
      */
     public static double toRadians(double angdeg) {
         return angdeg / 180.0 * PI;
     }
 
     /**
      * 将以弧度为单位测量的角度转换为近似等效的以度为单位的角度。从弧度到度的转换通常是不精确的；用户<i>不应</i>期望 {@code cos(toRadians(90.0))} 精确等于 {@code 0.0}。
      *
      * @param   angrad   以弧度为单位的角度
      * @return  以度为单位的 {@code angrad} 角度的测量值。
      * @since   1.2
      */
     public static double toDegrees(double angrad) {
         return angrad * 180.0 / PI;
     }
 
     /**
      * 返回欧拉数 <i>e</i> 的 {@code double} 值次幂。特殊情况：
      * <ul><li>如果参数是 NaN，则结果是 NaN。
      * <li>如果参数是正无穷大，则结果是正无穷大。
      * <li>如果参数是负无穷大，则结果是正零。</ul>
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。结果必须是半单调的。
      *
      * @param   a   要将 <i>e</i> 提升到的指数。
      * @return  <i>e</i><sup>{@code a}</sup> 的值，其中 <i>e</i> 是自然对数的基数。
      */
     public static double exp(double a) {
         return StrictMath.exp(a); // 默认实现委托给 StrictMath
     }
 
     /**
      * 返回一个 {@code double} 值的自然对数（以 <i>e</i> 为底）。特殊情况：
      * <ul><li>如果参数是 NaN 或小于零，则结果是 NaN。
      * <li>如果参数是正无穷大，则结果是正无穷大。
      * <li>如果参数是正零或负零，则结果是负无穷大。</ul>
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。结果必须是半单调的。
      *
      * @param   a   一个值
      * @return  ln {@code a} 的值，即 {@code a} 的自然对数。
      */
     public static double log(double a) {
         return StrictMath.log(a); // 默认实现委托给 StrictMath
     }
 
     /**
      * 返回一个 {@code double} 值的以 10 为底的对数。特殊情况：
      *
      * <ul><li>如果参数是 NaN 或小于零，则结果是 NaN。
      * <li>如果参数是正无穷大，则结果是正无穷大。
      * <li>如果参数是正零或负零，则结果是负无穷大。
      * <li>如果参数等于 10<sup><i>n</i></sup>（其中 <i>n</i> 是整数），则结果是 <i>n</i>。
      * </ul>
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。结果必须是半单调的。
      *
      * @param   a   一个值
      * @return  {@code a} 的以 10 为底的对数。
      * @since 1.5
      */
     public static double log10(double a) {
         return StrictMath.log10(a); // 默认实现委托给 StrictMath
     }
 
     /**
      * 返回一个 {@code double} 值的正确四舍五入的正平方根。特殊情况：
      * <ul><li>如果参数是 NaN 或小于零，则结果是 NaN。
      * <li>如果参数是正无穷大，则结果是正无穷大。
      * <li>如果参数是正零或负零，则结果与参数相同。</ul>
      * 否则，结果是最接近参数值真实数学平方根的 {@code double} 值。
      *
      * @param   a   一个值。
      * @return  {@code a} 的正平方根。如果参数是 NaN 或小于零，则结果是 NaN。
      */
     public static double sqrt(double a) {
         return StrictMath.sqrt(a); // 默认实现委托给 StrictMath
                                    // 注意，硬件 sqrt 指令通常可被 JIT 直接使用，
                                    // 应比在软件中执行 Math.sqrt 快得多。
     }
 
     /**
      * 返回一个 {@code double} 值的立方根。对于正有限值 {@code x}，{@code cbrt(-x) == -cbrt(x)}；
      * 即负值的立方根是该值绝对值的立方根的负数。
      *
      * 特殊情况：
      *
      * <ul>
      *
      * <li>如果参数是 NaN，则结果是 NaN。
      *
      * <li>如果参数是无穷大，则结果是与参数同符号的无穷大。
      *
      * <li>如果参数是零，则结果是与参数同符号的零。
      *
      * </ul>
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。
      *
      * @param   a   一个值。
      * @return  {@code a} 的立方根。
      * @since 1.5
      */
     public static double cbrt(double a) {
         return StrictMath.cbrt(a);
     }
 
     /**
      * 根据 IEEE 754 标准对两个参数执行余数运算。余数值在数学上等于
      * <code>f1 - f2</code> × <i>n</i>，
      * 其中 <i>n</i> 是最接近 {@code f1/f2} 精确数学值的数学整数，如果两个数学整数与 {@code f1/f2} 的距离相等，
      * 则 <i>n</i> 是偶数整数。如果余数为零，其符号与第一个参数的符号相同。
      * 特殊情况：
      * <ul><li>如果任一参数是 NaN，或者第一个参数是无穷大，或者第二个参数是正零或负零，则结果是 NaN。
      * <li>如果第一个参数是有限的且第二个参数是无穷大，则结果与第一个参数相同。</ul>
      *
      * @param   f1   被除数。
      * @param   f2   除数。
      * @return  {@code f1} 除以 {@code f2} 的余数。
      */
     public static double IEEEremainder(double f1, double f2) {
         return StrictMath.IEEEremainder(f1, f2); // 委托给 StrictMath
     }
 
     /**
      * 返回最接近负无穷大的 {@code double} 值，该值大于或等于参数且等于一个数学整数。特殊情况：
      * <ul><li>如果参数值已经等于一个数学整数，则结果与参数相同。
      * <li>如果参数是 NaN 或无穷大或正零或负零，则结果与参数相同。
      * <li>如果参数值小于零但大于 -1.0，则结果是负零。</ul>
      * 请注意，{@code Math.ceil(x)} 的值正好等于 {@code -Math.floor(-x)}。
      *
      * @param   a   一个值。
      * @return  最接近负无穷大的浮点值，大于或等于参数且等于一个数学整数。
      */
     public static double ceil(double a) {
         return StrictMath.ceil(a); // 默认实现委托给 StrictMath
     }
 
     /**
      * 返回最接近正无穷大的 {@code double} 值，该值小于或等于参数且等于一个数学整数。特殊情况：
      * <ul><li>如果参数值已经等于一个数学整数，则结果与参数相同。
      * <li>如果参数是 NaN 或无穷大或正零或负零，则结果与参数相同。</ul>
      *
      * @param   a   一个值。
      * @return  最接近正无穷大的浮点值，小于或等于参数且等于一个数学整数。
      */
     public static double floor(double a) {
         return StrictMath.floor(a); // 默认实现委托给 StrictMath
     }
 
     /**
      * 返回最接近参数且等于数学整数的 {@code double} 值。如果两个 {@code double} 值都是数学整数且距离相等，则结果是偶数的整数值。特殊情况：
      * <ul><li>如果参数值已经等于一个数学整数，则结果与参数相同。
      * <li>如果参数是 NaN 或无穷大或正零或负零，则结果与参数相同。</ul>
      *
      * @param   a   一个 {@code double} 值。
      * @return  最接近 {@code a} 且等于数学整数的浮点值。
      */
     public static double rint(double a) {
         return StrictMath.rint(a); // 默认实现委托给 StrictMath
     }
 
     /**
      * 通过将直角坐标 ({@code x}, {@code y}) 转换为极坐标 (r, <i>theta</i>)，返回角度 <i>theta</i>。
      * 该方法通过计算 {@code y/x} 的反正切来计算相位 <i>theta</i>，范围在 -<i>pi</i> 到 <i>pi</i> 之间。特殊情况：
      * <ul><li>如果任一参数是 NaN，则结果是 NaN。
      * <li>如果第一个参数是正零且第二个参数是正数，或者第一个参数是有限正数且第二个参数是正无穷大，则结果是正零。
      * <li>如果第一个参数是负零且第二个参数是正数，或者第一个参数是有限负数且第二个参数是正无穷大，则结果是负零。
      * <li>如果第一个参数是正零且第二个参数是负数，或者第一个参数是有限正数且第二个参数是负无穷大，则结果是最接近 <i>pi</i> 的 {@code double} 值。
      * <li>如果第一个参数是负零且第二个参数是负数，或者第一个参数是有限负数且第二个参数是负无穷大，则结果是最接近 -<i>pi</i> 的 {@code double} 值。
      * <li>如果第一个参数是正数且第二个参数是正零或负零，或者第一个参数是正无穷大且第二个参数是有限的，则结果是最接近 <i>pi</i>/2 的 {@code double} 值。
      * <li>如果第一个参数是负数且第二个参数是正零或负零，或者第一个参数是负无穷大且第二个参数是有限的，则结果是最接近 -<i>pi</i>/2 的 {@code double} 值。
      * <li>如果两个参数都是正无穷大，则结果是最接近 <i>pi</i>/4 的 {@code double} 值。
      * <li>如果第一个参数是正无穷大且第二个参数是负无穷大，则结果是最接近 3*<i>pi</i>/4 的 {@code double} 值。
      * <li>如果第一个参数是负无穷大且第二个参数是正无穷大，则结果是最接近 -<i>pi</i>/4 的 {@code double} 值。
      * <li>如果两个参数都是负无穷大，则结果是最接近 -3*<i>pi</i>/4 的 {@code double} 值。</ul>
      *
      * <p>计算结果必须在精确结果的 2 ulp 范围内。结果必须是半单调的。
      *
      * @param   y   纵坐标
      * @param   x   横坐标
      * @return  极坐标中的 <i>theta</i> 分量，对应于直角坐标中的点 (<i>x</i>, <i>y</i>)。
      */
     public static double atan2(double y, double x) {
         return StrictMath.atan2(y, x); // 默认实现委托给 StrictMath
     }
 
     /**
      * 返回第一个参数的第二个参数次幂的值。特殊情况：
      *
      * <ul><li>如果第二个参数是正零或负零，则结果是 1.0。
      * <li>如果第二个参数是 1.0，则结果与第一个参数相同。
      * <li>如果第二个参数是 NaN，则结果是 NaN。
      * <li>如果第一个参数是 NaN 且第二个参数非零，则结果是 NaN。
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
      * <li>第一个参数是负零且第二个参数大于零但不是有限奇整数，或者
      * <li>第一个参数是负无穷大且第二个参数小于零但不是有限奇整数，
      * </ul>
      * 则结果是正零。
      *
      * <li>如果
      * <ul>
      * <li>第一个参数是负零且第二个参数是正有限奇整数，或者
      * <li>第一个参数是负无穷大且第二个参数是负有限奇整数，
      * </ul>
      * 则结果是负零。
      *
      * <li>如果
      * <ul>
      * <li>第一个参数是负零且第二个参数小于零但不是有限奇整数，或者
      * <li>第一个参数是负无穷大且第二个参数大于零但不是有限奇整数，
      * </ul>
      * 则结果是正无穷大。
      *
      * <li>如果
      * <ul>
      * <li>第一个参数是负零且第二个参数是负有限奇整数，或者
      * <li>第一个参数是负无穷大且第二个参数是正有限奇整数，
      * </ul>
      * 则结果是负无穷大。
      *
      * <li>如果第一个参数是有限且小于零
      * <ul>
      * <li>如果第二个参数是有限偶整数，结果等于将第一个参数的绝对值提升到第二个参数次幂的结果。
      * <li>如果第二个参数是有限奇整数，结果等于将第一个参数的绝对值提升到第二个参数次幂的结果的负数。
      * <li>如果第二个参数是有限的但不是整数，则结果是 NaN。
      * </ul>
      *
      * <li>如果两个参数都是整数，则结果精确等于将第一个参数提升到第二个参数次幂的数学结果（如果该结果确实可以表示为 {@code double} 值）。</ul>
      *
      * <p>（在上述描述中，如果浮点值是有限的且是 {@link #ceil ceil} 方法的固定点，或者等价地，是 {@link #floor floor} 方法的固定点，则认为它是整数。如果对该值应用单参数方法的结果等于该值，则该值是该方法的固定点。）
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。结果必须是半单调的。
      *
      * @param   a   底数。
      * @param   b   指数。
      * @return  {@code a}<sup>{@code b}</sup> 的值。
      */
     public static double pow(double a, double b) {
         return StrictMath.pow(a, b); // 默认实现委托给 StrictMath
     }
 
     /**
      * 返回最接近参数的 {@code int} 值，对于相等的情况向正无穷大四舍五入。
      *
      * <p>
      * 特殊情况：
      * <ul><li>如果参数是 NaN，则结果是 0。
      * <li>如果参数是负无穷大或任何小于或等于 {@code Integer.MIN_VALUE} 的值，则结果等于 {@code Integer.MIN_VALUE} 的值。
      * <li>如果参数是正无穷大或任何大于或等于 {@code Integer.MAX_VALUE} 的值，则结果等于 {@code Integer.MAX_VALUE} 的值。</ul>
      *
      * @param   a   要四舍五入为整数的浮点值。
      * @return  参数四舍五入到最接近的 {@code int} 值。
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
             // a 是有限数，满足 pow(2,-32) <= ulp(a) < 1
             int r = ((intBits & FloatConsts.SIGNIF_BIT_MASK)
                     | (FloatConsts.SIGNIF_BIT_MASK + 1));
             if (intBits < 0) {
                 r = -r;
             }
             // 以下注释中的每个 Java 表达式对应于数学表达式的值：
             // (r) 对应于 a / ulp(a)
             // (r >> shift) 对应于 floor(a * 2)
             // ((r >> shift) + 1) 对应于 floor((a + 1/2) * 2)
             // (((r >> shift) + 1) >> 1) 对应于 floor(a + 1/2)
             return ((r >> shift) + 1) >> 1;
         } else {
             // a 是以下之一：
             // - 绝对值小于 exp(2,FloatConsts.SIGNIFICAND_WIDTH-32) < 1/2 的有限数
             // - ulp(a) >= 1 的有限数，因此 a 是数学整数
             // - 无穷大或 NaN
             return (int) a;
         }
     }
 
     /**
      * 返回最接近参数的 {@code long} 值，对于相等的情况向正无穷大四舍五入。
      *
      * <p>特殊情况：
      * <ul><li>如果参数是 NaN，则结果是 0。
      * <li>如果参数是负无穷大或任何小于或等于 {@code Long.MIN_VALUE} 的值，则结果等于 {@code Long.MIN_VALUE} 的值。
      * <li>如果参数是正无穷大或任何大于或等于 {@code Long.MAX_VALUE} 的值，则结果等于 {@code Long.MAX_VALUE} 的值。</ul>
      *
      * @param   a   要四舍五入为 {@code long} 的浮点值。
      * @return  参数四舍五入到最接近的 {@code long} 值。
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
             // a 是有限数，满足 pow(2,-64) <= ulp(a) < 1
             long r = ((longBits & DoubleConsts.SIGNIF_BIT_MASK)
                     | (DoubleConsts.SIGNIF_BIT_MASK + 1));
             if (longBits < 0) {
                 r = -r;
             }
             // 以下注释中的每个 Java 表达式对应于数学表达式的值：
             // (r) 对应于 a / ulp(a)
             // (r >> shift) 对应于 floor(a * 2)
             // ((r >> shift) + 1) 对应于 floor((a + 1/2) * 2)
             // (((r >> shift) + 1) >> 1) 对应于 floor(a + 1/2)
             return ((r >> shift) + 1) >> 1;
         } else {
             // a 是以下之一：
             // - 绝对值小于 exp(2,DoubleConsts.SIGNIFICAND_WIDTH-64) < 1/2 的有限数
             // - ulp(a) >= 1 的有限数，因此 a 是数学整数
             // - 无穷大或 NaN
             return (long) a;
         }
     }
 
     private static final class RandomNumberGeneratorHolder {
         static final Random randomNumberGenerator = new Random();
     }
 
     /**
      * 返回一个正号的 {@code double} 值，大于或等于 {@code 0.0} 且小于 {@code 1.0}。
      * 返回值以（近似）均匀分布的方式从该范围内伪随机选择。
      *
      * <p>首次调用此方法时，它会创建一个新的伪随机数生成器，等价于以下表达式：
      *
      * <blockquote>{@code new java.util.Random()}</blockquote>
      *
      * 此新伪随机数生成器随后用于此方法的所有调用，且不在其他地方使用。
      *
      * <p>此方法已正确同步，允许多个线程正确使用。然而，如果多个线程需要以高频率生成伪随机数，每个线程拥有自己的伪随机数生成器可能会减少竞争。
      *
      * @return  一个伪随机的 {@code double} 值，大于或等于 {@code 0.0} 且小于 {@code 1.0}。
      * @see Random#nextDouble()
      */
     public static double random() {
         return RandomNumberGeneratorHolder.randomNumberGenerator.nextDouble();
     }
 
     /**
      * 返回其参数之和，如果结果溢出 {@code int}，则抛出异常。
      *
      * @param x 第一个值
      * @param y 第二个值
      * @return 结果
      * @throws ArithmeticException 如果结果溢出 int
      * @since 1.8
      */
     public static int addExact(int x, int y) {
         int r = x + y;
         // HD 2-12 如果两个参数的符号与结果相反，则发生溢出
         if (((x ^ r) & (y ^ r)) < 0) {
             throw new ArithmeticException("整数溢出");
         }
         return r;
     }
 
     /**
      * 返回其参数之和，如果结果溢出 {@code long}，则抛出异常。
      *
      * @param x 第一个值
      * @param y 第二个值
      * @return 结果
      * @throws ArithmeticException 如果结果溢出 long
      * @since 1.8
      */
     public static long addExact(long x, long y) {
         long r = x + y;
         // HD 2-12 如果两个参数的符号与结果相反，则发生溢出
         if (((x ^ r) & (y ^ r)) < 0) {
             throw new ArithmeticException("长整数溢出");
         }
         return r;
     }
 
     /**
      * 返回参数之差，如果结果溢出 {@code int}，则抛出异常。
      *
      * @param x 第一个值
      * @param y 从第一个值减去的第二个值
      * @return 结果
      * @throws ArithmeticException 如果结果溢出 int
      * @since 1.8
      */
     public static int subtractExact(int x, int y) {
         int r = x - y;
         // HD 2-12 如果参数符号不同且结果的符号与 x 的符号不同，则发生溢出
         if (((x ^ y) & (x ^ r)) < 0) {
             throw new ArithmeticException("整数溢出");
         }
         return r;
     }
 
     /**
      * 返回参数之差，如果结果溢出 {@code long}，则抛出异常。
      *
      * @param x 第一个值
      * @param y 从第一个值减去的第二个值
      * @return 结果
      * @throws ArithmeticException 如果结果溢出 long
      * @since 1.8
      */
     public static long subtractExact(long x, long y) {
         long r = x - y;
         // HD 2-12 如果参数符号不同且结果的符号与 x 的符号不同，则发生溢出
         if (((x ^ y) & (x ^ r)) < 0) {
             throw new ArithmeticException("长整数溢出");
         }
         return r;
     }
 
     /**
      * 返回参数的乘积，如果结果溢出 {@code int}，则抛出异常。
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
             throw new ArithmeticException("整数溢出");
         }
         return (int)r;
     }
 
     /**
      * 返回参数的乘积，如果结果溢出 {@code long}，则抛出异常。
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
             // 存在可能导致溢出的高于 2^31 的位
             // 使用除法运算符检查结果
             // 并检查 Long.MIN_VALUE * -1 的特殊情况
            if (((y != 0) && (r / y != x)) ||
                (x == Long.MIN_VALUE && y == -1)) {
                 throw new ArithmeticException("长整数溢出");
             }
         }
         return r;
     }
 
     /**
      * 返回参数加一，如果结果溢出 {@code int}，则抛出异常。
      *
      * @param a 要递增的值
      * @return 结果
      * @throws ArithmeticException 如果结果溢出 int
      * @since 1.8
      */
     public static int incrementExact(int a) {
         if (a == Integer.MAX_VALUE) {
             throw new ArithmeticException("整数溢出");
         }
 
         return a + 1;
     }
 
     /**
      * 返回参数加一，如果结果溢出 {@code long}，则抛出异常。
      *
      * @param a 要递增的值
      * @return 结果
      * @throws ArithmeticException 如果结果溢出 long
      * @since 1.8
      */
     public static long incrementExact(long a) {
         if (a == Long.MAX_VALUE) {
             throw new ArithmeticException("长整数溢出");
         }
 
         return a + 1L;
     }
 
     /**
      * 返回参数减一，如果结果溢出 {@code int}，则抛出异常。
      *
      * @param a 要递减的值
      * @return 结果
      * @throws ArithmeticException 如果结果溢出 int
      * @since 1.8
      */
     public static int decrementExact(int a) {
         if (a == Integer.MIN_VALUE) {
             throw new ArithmeticException("整数溢出");
         }
 
         return a - 1;
     }
 
     /**
      * 返回参数减一，如果结果溢出 {@code long}，则抛出异常。
      *
      * @param a 要递减的值
      * @return 结果
      * @throws ArithmeticException 如果结果溢出 long
      * @since 1.8
      */
     public static long decrementExact(long a) {
         if (a == Long.MIN_VALUE) {
             throw new ArithmeticException("长整数溢出");
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
             throw new ArithmeticException("整数溢出");
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
             throw new ArithmeticException("长整数溢出");
         }
 
         return -a;
     }
 
     /**
      * 返回 {@code long} 参数的值；如果值溢出 {@code int}，则抛出异常。
      *
      * @param value long 值
      * @return 参数转换为 int
      * @throws ArithmeticException 如果 {@code argument} 溢出 int
      * @since 1.8
      */
     public static int toIntExact(long value) {
         if ((int)value != value) {
             throw new ArithmeticException("整数溢出");
         }
         return (int)value;
     }
 
     /**
      * 返回最接近正无穷大的 {@code int} 值，该值小于或等于代数商。特殊情况：如果被除数是
      * {@linkplain Integer#MIN_VALUE Integer.MIN_VALUE} 且除数是 {@code -1}，
      * 则发生整数溢出，结果等于 {@code Integer.MIN_VALUE}。
      * <p>
      * 普通整数除法在向零四舍五入模式（截断）下运行。此操作在向负无穷大（向下取整）四舍五入模式下运行。
      * 当精确结果为负时，向下取整模式与截断模式的结果不同。
      * <ul>
      *   <li>如果参数的符号相同，{@code floorDiv} 和 {@code /} 运算符的结果相同。<br>
      *       例如，{@code floorDiv(4, 3) == 1} 和 {@code (4 / 3) == 1}。</li>
      *   <li>如果参数的符号不同，商为负，{@code floorDiv} 返回小于或等于商的整数，
      *       而 {@code /} 运算符返回最接近零的整数。<br>
      *       例如，{@code floorDiv(-4, 3) == -2}，而 {@code (-4 / 3) == -1}。
      *   </li>
      * </ul>
      * <p>
      *
      * @param x 被除数
      * @param y 除数
      * @return 最接近正无穷大的 {@code int} 值，小于或等于代数商。
      * @throws ArithmeticException 如果除数 {@code y} 为零
      * @see #floorMod(int, int)
      * @see #floor(double)
      * @since 1.8
      */
     public static int floorDiv(int x, int y) {
         int r = x / y;
         // 如果符号不同且模不为零，向下取整
         if ((x ^ y) < 0 && (r * y != x)) {
             r--;
         }
         return r;
     }
 
     /**
      * 返回最接近正无穷大的 {@code long} 值，该值小于或等于代数商。特殊情况：如果被除数是
      * {@linkplain Long#MIN_VALUE Long.MIN_VALUE} 且除数是 {@code -1}，
      * 则发生整数溢出，结果等于 {@code Long.MIN_VALUE}。
      * <p>
      * 普通整数除法在向零四舍五入模式（截断）下运行。此操作在向负无穷大（向下取整）四舍五入模式下运行。
      * 当精确结果为负时，向下取整模式与截断模式的结果不同。
      * <p>
      * 示例见 {@link #floorDiv(int, int)}。
      *
      * @param x 被除数
      * @param y 除数
      * @return 最接近正无穷大的 {@code long} 值，小于或等于代数商。
      * @throws ArithmeticException 如果除数 {@code y} 为零
      * @see #floorMod(long, long)
      * @see #floor(double)
      * @since 1.8
      */
     public static long floorDiv(long x, long y) {
         long r = x / y;
         // 如果符号不同且模不为零，向下取整
         if ((x ^ y) < 0 && (r * y != x)) {
             r--;
         }
         return r;
     }
 
     /**
      * 返回 {@code int} 参数的向下取整模。
      * <p>
      * 向下取整模是 {@code x - (floorDiv(x, y) * y)}，
      * 其符号与除数 {@code y} 相同，且在 {@code -abs(y) < r < +abs(y)} 范围内。
      *
      * <p>
      * {@code floorDiv} 和 {@code floorMod} 的关系满足：
      * <ul>
      *   <li>{@code floorDiv(x, y) * y + floorMod(x, y) == x}
      * </ul>
      * <p>
      * {@code floorMod} 与 {@code %} 运算符的值差异源于 {@code floorDiv} 返回小于或等于商的整数，
      * 而 {@code /} 运算符返回最接近零的整数。
      * <p>
      * 示例：
      * <ul>
      *   <li>如果参数的符号相同，{@code floorMod} 和 {@code %} 运算符的结果相同。<br>
      *       <ul>
      *       <li>{@code floorMod(4, 3) == 1}；和 {@code (4 % 3) == 1}</li>
      *       </ul>
      *   <li>如果参数的符号不同，结果与 {@code %} 运算符不同。<br>
      *      <ul>
      *      <li>{@code floorMod(+4, -3) == -2}；和 {@code (+4 % -3) == +1}</li>
      *      <li>{@code floorMod(-4, +3) == +2}；和 {@code (-4 % +3) == -1}</li>
      *      <li>{@code floorMod(-4, -3) == -1}；和 {@code (-4 % -3) == -1}</li>
      *      </ul>
      *   </li>
      * </ul>
      * <p>
      * 如果参数的符号未知且需要正模，可以计算为 {@code (floorMod(x, y) + abs(y)) % abs(y)}。
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
      * 向下取整模是 {@code x - (floorDiv(x, y) * y)}，
      * 其符号与除数 {@code y} 相同，且在 {@code -abs(y) < r < +abs(y)} 范围内。
      *
      * <p>
      * {@code floorDiv} 和 {@code floorMod} 的关系满足：
      * <ul>
      *   <li>{@code floorDiv(x, y) * y + floorMod(x, y) == x}
      * </ul>
      * <p>
      * 示例见 {@link #floorMod(int, int)}。
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
      * 返回一个 {@code int} 值的绝对值。如果参数非负，则返回参数。如果参数为负，则返回参数的相反数。
      *
      * <p>请注意，如果参数等于 {@link Integer#MIN_VALUE}，最负的可表示 {@code int} 值，则结果是该值，仍然为负。
      *
      * @param   a   要确定绝对值的参数
      * @return  参数的绝对值。
      */
     public static int abs(int a) {
         return (a < 0) ? -a : a;
     }
 
     /**
      * 返回一个 {@code long} 值的绝对值。如果参数非负，则返回参数。如果参数为负，则返回参数的相反数。
      *
      * <p>请注意，如果参数等于 {@link Long#MIN_VALUE}，最负的可表示 {@code long} 值，则结果是该值，仍然为负。
      *
      * @param   a   要确定绝对值的参数
      * @return  参数的绝对值。
      */
     public static long abs(long a) {
         return (a < 0) ? -a : a;
     }
 
     /**
      * 返回一个 {@code float} 值的绝对值。如果参数非负，则返回参数。如果参数为负，则返回参数的相反数。特殊情况：
      * <ul><li>如果参数是正零或负零，则结果是正零。
      * <li>如果参数是无穷大，则结果是正无穷大。
      * <li>如果参数是 NaN，则结果是 NaN。</ul>
      * 换句话说，结果与以下表达式的值相同：
      * <p>{@code Float.intBitsToFloat(0x7fffffff & Float.floatToIntBits(a))}
      *
      * @param   a   要确定绝对值的参数
      * @return  参数的绝对值。
      */
     public static float abs(float a) {
         return (a <= 0.0F) ? 0.0F - a : a;
     }
 
     /**
      * 返回一个 {@code double} 值的绝对值。如果参数非负，则返回参数。如果参数为负，则返回参数的相反数。特殊情况：
      * <ul><li>如果参数是正零或负零，则结果是正零。
      * <li>如果参数是无穷大，则结果是正无穷大。
      * <li>如果参数是 NaN，则结果是 NaN。</ul>
      * 换句话说，结果与以下表达式的值相同：
      * <p>{@code Double.longBitsToDouble((Double.doubleToLongBits(a)<<1)>>>1)}
      *
      * @param   a   要确定绝对值的参数
      * @return  参数的绝对值。
      */
     public static double abs(double a) {
         return (a <= 0.0D) ? 0.0D - a : a;
     }
 
     /**
      * 返回两个 {@code int} 值中的较大值。即结果是最接近 {@link Integer#MAX_VALUE} 的参数值。如果两个参数值相同，则结果是该值。
      *
      * @param   a   一个参数。
      * @param   b   另一个参数。
      * @return  {@code a} 和 {@code b} 中较大的值。
      */
     public static int max(int a, int b) {
         return (a >= b) ? a : b;
     }
 
     /**
      * 返回两个 {@code long} 值中的较大值。即结果是最接近 {@link Long#MAX_VALUE} 的参数值。如果两个参数值相同，则结果是该值。
      *
      * @param   a   一个参数。
      * @param   b   另一个参数。
      * @return  {@code a} 和 {@code b} 中较大的值。
      */
     public static long max(long a, long b) {
         return (a >= b) ? a : b;
     }
 
     // 使用原始位操作转换，确保参数非 NaN。
     private static long negativeZeroFloatBits  = Float.floatToRawIntBits(-0.0f);
     private static long negativeZeroDoubleBits = Double.doubleToRawLongBits(-0.0d);
 
     /**
      * 返回两个 {@code float} 值中的较大值。即结果是最接近正无穷大的参数值。如果两个参数值相同，则结果是该值。如果任一值为 NaN，则结果是 NaN。与数值比较运算符不同，此方法认为负零严格小于正零。如果一个参数是正零，另一个是负零，则结果是正零。
      *
      * @param   a   一个参数。
      * @param   b   另一个参数。
      * @return  {@code a} 和 {@code b} 中较大的值。
      */
     public static float max(float a, float b) {
         if (a != a)
             return a;   // a 是 NaN
         if ((a == 0.0f) &&
             (b == 0.0f) &&
             (Float.floatToRawIntBits(a) == negativeZeroFloatBits)) {
             // 原始转换可以，因为 NaN 不会映射到 -0.0。
             return b;
         }
         return (a >= b) ? a : b;
     }
 
     /**
      * 返回两个 {@code double} 值中的较大值。即结果是最接近正无穷大的参数值。如果两个参数值相同，则结果是该值。如果任一值为 NaN，则结果是 NaN。与数值比较运算符不同，此方法认为负零严格小于正零。如果一个参数是正零，另一个是负零，则结果是正零。
      *
      * @param   a   一个参数。
      * @param   b   另一个参数。
      * @return  {@code a} 和 {@code b} 中较大的值。
      */
     public static double max(double a, double b) {
         if (a != a)
             return a;   // a 是 NaN
         if ((a == 0.0d) &&
             (b == 0.0d) &&
             (Double.doubleToRawLongBits(a) == negativeZeroDoubleBits)) {
             // 原始转换可以，因为 NaN 不会映射到 -0.0。
             return b;
         }
         return (a >= b) ? a : b;
     }
 
     /**
      * 返回两个 {@code int} 值中的较小值。即结果是最接近 {@link Integer#MIN_VALUE} 的参数值。如果两个参数值相同，则结果是该值。
      *
      * @param   a   一个参数。
      * @param   b   另一个参数。
      * @return  {@code a} 和 {@code b} 中较小的值。
      */
     public static int min(int a, int b) {
         return (a <= b) ? a : b;
     }
 
     /**
      * 返回两个 {@code long} 值中的较小值。即结果是最接近 {@link Long#MIN_VALUE} 的参数值。如果两个参数值相同，则结果是该值。
      *
      * @param   a   一个参数。
      * @param   b   另一个参数。
      * @return  {@code a} 和 {@code b} 中较小的值。
      */
     public static long min(long a, long b) {
         return (a <= b) ? a : b;
     }
 
     /**
      * 返回两个 {@code float} 值中的较小值。即结果是最接近负无穷大的值。如果两个参数值相同，则结果是该值。如果任一值为 NaN，则结果是 NaN。与数值比较运算符不同，此方法认为负零严格小于正零。如果一个参数是正零，另一个是负零，则结果是负零。
      *
      * @param   a   一个参数。
      * @param   b   另一个参数。
      * @return  {@code a} 和 {@code b} 中较小的值。
      */
     public static float min(float a, float b) {
         if (a != a)
             return a;   // a 是 NaN
         if ((a == 0.0f) &&
             (b == 0.0f) &&
             (Float.floatToRawIntBits(b) == negativeZeroFloatBits)) {
             // 原始转换可以，因为 NaN 不会映射到 -0.0。
             return b;
         }
         return (a <= b) ? a : b;
     }
 
     /**
      * 返回两个 {@code double} 值中的较小值。即结果是最接近负无穷大的值。如果两个参数值相同，则结果是该值。如果任一值为 NaN，则结果是 NaN。与数值比较运算符不同，此方法认为负零严格小于正零。如果一个参数是正零，另一个是负零，则结果是负零。
      *
      * @param   a   一个参数。
      * @param   b   另一个参数。
      * @return  {@code a} 和 {@code b} 中较小的值。
      */
     public static double min(double a, double b) {
         if (a != a)
             return a;   // a 是 NaN
         if ((a == 0.0d) &&
             (b == 0.0d) &&
             (Double.doubleToRawLongBits(b) == negativeZeroDoubleBits)) {
             // 原始转换可以，因为 NaN 不会映射到 -0.0。
             return b;
         }
         return (a <= b) ? a : b;
     }
 
     /**
      * 返回参数的 ulp（最后一位单位）大小。{@code double} 值的 ulp 是该浮点值与幅度上紧邻的较大 {@code double} 值之间的正距离。请注意，对于非 NaN 的 <i>x</i>，<code>ulp(-<i>x</i>) == ulp(<i>x</i>)</code>。
      *
      * <p>特殊情况：
      * <ul>
      * <li> 如果参数是 NaN，则结果是 NaN。
      * <li> 如果参数是正或负无穷大，则结果是正无穷大。
      * <li> 如果参数是正或负零，则结果是 {@code Double.MIN_VALUE}。
      * <li> 如果参数是 ±{@code Double.MAX_VALUE}，则结果等于 2<sup>971</sup>。
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
 
         case DoubleConsts.MIN_EXPONENT-1:       // 零或次正规数
             return Double.MIN_VALUE;
 
         default:
             assert exp <= DoubleConsts.MAX_EXPONENT && exp >= DoubleConsts.MIN_EXPONENT;
 
             // ulp(x) 通常是 2^(SIGNIFICAND_WIDTH-1)*(2^ilogb(x))
             exp = exp - (DoubleConsts.SIGNIFICAND_WIDTH-1);
             if (exp >= DoubleConsts.MIN_EXPONENT) {
                 return powerOfTwoD(exp);
             }
             else {
                 // 返回次正规结果；将 Double.MIN_VALUE 的整数表示左移适当位数
                 return Double.longBitsToDouble(1L <<
                 (exp - (DoubleConsts.MIN_EXPONENT - (DoubleConsts.SIGNIFICAND_WIDTH-1)) ));
             }
         }
     }
 
     /**
      * 返回参数的 ulp（最后一位单位）大小。{@code float} 值的 ulp 是该浮点值与幅度上紧邻的较大 {@code float} 值之间的正距离。请注意，对于非 NaN 的 <i>x</i>，<code>ulp(-<i>x</i>) == ulp(<i>x</i>)</code>。
      *
      * <p>特殊情况：
      * <ul>
      * <li> 如果参数是 NaN，则结果是 NaN。
      * <li> 如果参数是正或负无穷大，则结果是正无穷大。
      * <li> 如果参数是正或负零，则结果是 {@code Float.MIN_VALUE}。
      * <li> 如果参数是 ±{@code Float.MAX_VALUE}，则结果等于 2<sup>104</sup>。
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
 
         case FloatConsts.MIN_EXPONENT-1:        // 零或次正规数
             return FloatConsts.MIN_VALUE;
 
         default:
             assert exp <= FloatConsts.MAX_EXPONENT && exp >= FloatConsts.MIN_EXPONENT;
 
             // ulp(x) 通常是 2^(SIGNIFICAND_WIDTH-1)*(2^ilogb(x))
             exp = exp - (FloatConsts.SIGNIFICAND_WIDTH-1);
             if (exp >= FloatConsts.MIN_EXPONENT) {
                 return powerOfTwoF(exp);
             }
             else {
                 // 返回次正规结果；将 FloatConsts.MIN_VALUE 的整数表示左移适当位数
                 return Float.intBitsToFloat(1 <<
                 (exp - (FloatConsts.MIN_EXPONENT - (FloatConsts.SIGNIFICAND_WIDTH-1)) ));
             }
         }
     }
 
     /**
      * 返回参数的符号函数；如果参数为零，则返回零；如果参数大于零，则返回 1.0；如果参数小于零，则返回 -1.0。
      *
      * <p>特殊情况：
      * <ul>
      * <li> 如果参数是 NaN，则结果是 NaN。
      * <li> 如果参数是正零或负零，则结果与参数相同。
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
      * 返回参数的符号函数；如果参数为零，则返回零；如果参数大于零，则返回 1.0f；如果参数小于零，则返回 -1.0f。
      *
      * <p>特殊情况：
      * <ul>
      * <li> 如果参数是 NaN，则结果是 NaN。
      * <li> 如果参数是正零或负零，则结果与参数相同。
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
      * 返回一个 {@code double} 值的双曲正弦。双曲正弦 <i>x</i> 定义为
      * (<i>e<sup>x</sup> - e<sup>-x</sup></i>)/2，其中 <i>e</i> 是 {@linkplain Math#E 欧拉数}。
      *
      * <p>特殊情况：
      * <ul>
      *
      * <li>如果参数是 NaN，则结果是 NaN。
      *
      * <li>如果参数是无穷大，则结果是与参数同符号的无穷大。
      *
      * <li>如果参数是零，则结果是与参数同符号的零。
      *
      * </ul>
      *
      * <p>计算结果必须在精确结果的 2.5 ulp 范围内。
      *
      * @param   x 要返回其双曲正弦的数值。
      * @return  {@code x} 的双曲正弦。
      * @since 1.5
      */
     public static double sinh(double x) {
         return StrictMath.sinh(x);
     }
 
     /**
      * 返回一个 {@code double} 值的双曲余弦。双曲余弦 <i>x</i> 定义为
      * (<i>e<sup>x</sup> + e<sup>-x</sup></i>)/2，其中 <i>e</i> 是 {@linkplain Math#E 欧拉数}。
      *
      * <p>特殊情况：
      * <ul>
      *
      * <li>如果参数是 NaN，则结果是 NaN。
      *
      * <li>如果参数是无穷大，则结果是正无穷大。
      *
      * <li>如果参数是零，则结果是 {@code 1.0}。
      *
      * </ul>
      *
      * <p>计算结果必须在精确结果的 2.5 ulp 范围内。
      *
      * @param   x 要返回其双曲余弦的数值。
      * @return  {@code x} 的双曲余弦。
      * @since 1.5
      */
     public static double cosh(double x) {
         return StrictMath.cosh(x);
     }
 
     /**
      * 返回一个 {@code double} 值的双曲正切。双曲正切 <i>x</i> 定义为
      * (<i>e<sup>x</sup> - e<sup>-x</sup></i>)/(<i>e<sup>x</sup> + e<sup>-x</sup></i>)，
      * 换句话说，即 {@linkplain Math#sinh sinh(<i>x</i>)}/{@linkplain Math#cosh cosh(<i>x</i>)}。请注意，精确双曲正切的绝对值始终小于 1。
      *
      * <p>特殊情况：
      * <ul>
      *
      * <li>如果参数是 NaN，则结果是 NaN。
      *
      * <li>如果参数是零，则结果是与参数同符号的零。
      *
      * <li>如果参数是正无穷大，则结果是 {@code +1.0}。
      *
      * <li>如果参数是负无穷大，则结果是 {@code -1.0}。
      *
      * </ul>
      *
      * <p>计算结果必须在精确结果的 2.5 ulp 范围内。对于任何有限输入，{@code tanh} 的结果绝对值必须小于或等于 1。请注意，一旦 tanh 的精确结果在极限值 ±1 的 1/2 ulp 范围内，应返回正确符号的 ±{@code 1.0}。
      *
      * @param   x 要返回其双曲正切的数值。
      * @return  {@code x} 的双曲正切。
      * @since 1.5
      */
     public static double tanh(double x) {
         return StrictMath.tanh(x);
     }
 
     /**
      * 返回 sqrt(<i>x</i><sup>2</sup> + <i>y</i><sup>2</sup>)，不会出现中间溢出或下溢。
      *
      * <p>特殊情况：
      * <ul>
      *
      * <li> 如果任一参数是无穷大，则结果是正无穷大。
      *
      * <li> 如果任一参数是 NaN 且没有参数是无穷大，则结果是 NaN。
      *
      * </ul>
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。如果一个参数保持不变，结果在另一个参数上必须是半单调的。
      *
      * @param x 一个值
      * @param y 一个值
      * @return sqrt(<i>x</i><sup>2</sup> + <i>y</i><sup>2</sup>)，不会出现中间溢出或下溢
      * @since 1.5
      */
     public static double hypot(double x, double y) {
         return StrictMath.hypot(x, y);
     }
 
     /**
      * 返回 <i>e</i><sup>x</sup> - 1。请注意，对于接近 0 的 <i>x</i> 值，{@code expm1(x)} + 1 的精确和比 {@code exp(x)} 更接近 <i>e</i><sup>x</sup> 的真实结果。
      *
      * <p>特殊情况：
      * <ul>
      * <li>如果参数是 NaN，则结果是 NaN。
      *
      * <li>如果参数是正无穷大，则结果是正无穷大。
      *
      * <li>如果参数是负无穷大，则结果是 -1.0。
      *
      * <li>如果参数是零，则结果是与参数同符号的零。
      *
      * </ul>
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。结果必须是半单调的。对于任何有限输入，{@code expm1} 的结果必须大于或等于 {@code -1.0}。请注意，一旦 <i>e</i><sup>{@code x}</sup> - 1 的精确结果在极限值 -1 的 1/2 ulp 范围内，应返回 {@code -1.0}。
      *
      * @param   x   用于计算 <i>e</i><sup>{@ gentleness
      * @return  <i>e</i><sup>{@code x}</sup> - 1 的值。
      * @since 1.5
      */
     public static double expm1(double x) {
         return StrictMath.expm1(x);
     }
 
     /**
      * 返回参数加 1 的自然对数。请注意，对于小的值 {@code x}，{@code log1p(x)} 的结果比浮点评估 {@code log(1.0+x)} 更接近 ln(1 + {@code x}) 的真实结果。
      *
      * <p>特殊情况：
      *
      * <ul>
      *
      * <li>如果参数是 NaN 或小于 -1，则结果是 NaN。
      *
      * <li>如果参数是正无穷大，则结果是正无穷大。
      *
      * <li>如果参数是负一，则结果是负无穷大。
      *
      * <li>如果参数是零，则结果是与参数同符号的零。
      *
      * </ul>
      *
      * <p>计算结果必须在精确结果的 1 ulp 范围内。结果必须是半单调的。
      *
      * @param   x   一个值
      * @return ln({@code x} + 1) 的值，即 {@code x} + 1 的自然对数
      * @since 1.5
      */
     public static double log1p(double x) {
         return StrictMath.log1p(x);
     }
 
     /**
      * 返回第一个浮点参数，带有第二个浮点参数的符号。请注意，与 {@link StrictMath#copySign(double, double) StrictMath.copySign} 方法不同，此方法不要求将 NaN {@code sign} 参数视为正值；实现可以允许将某些 NaN 参数视为正值，某些视为负值，以提高性能。
      *
      * @param magnitude 提供结果绝对值的参数
      * @param sign   提供结果符号的参数
      * @return 具有 {@code magnitude} 的绝对值和 {@code sign} 的符号的值。
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
      * 返回第一个浮点参数，带有第二个浮点参数的符号。请注意，与 {@link StrictMath#copySign(float, float) StrictMath.copySign} 方法不同，此方法不要求将 NaN {@code sign} 参数视为正值；实现可以允许将某些 NaN 参数视为正值，某些视为负值，以提高性能。
      *
      * @param magnitude 提供结果绝对值的参数
      * @param sign   提供结果符号的参数
      * @return 具有 {@code magnitude} 的绝对值和 {@code sign} 的符号的值。
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
      * <li>如果参数是 NaN 或无穷大，则结果是 {@link Float#MAX_EXPONENT} + 1。
      * <li>如果参数是零或次正规数，则结果是 {@link Float#MIN_EXPONENT} - 1。
      * </ul>
      * @param f 一个 {@code float} 值
      * @return 参数的无偏指数
      * @since 1.6
      */
     public static int getExponent(float f) {
         /*
          * 将 f 按位转换为整数，屏蔽指数位，向右移位，然后减去 float 的偏置以获得真正的指数值
          */
         return ((Float.floatToRawIntBits(f) & FloatConsts.EXP_BIT_MASK) >>
                 (FloatConsts.SIGNIFICAND_WIDTH - 1)) - FloatConsts.EXP_BIAS;
     }
 
     /**
      * 返回用于表示 {@code double} 的无偏指数。特殊情况：
      *
      * <ul>
      * <li>如果参数是 NaN 或无穷大，则结果是 {@link Double#MAX_EXPONENT} + 1。
      * <li>如果参数是零或次正规数，则结果是 {@link Double#MIN_EXPONENT} - 1。
      * </ul>
      * @param d 一个 {@code double} 值
      * @return 参数的无偏指数
      * @since 1.6
      */
     public static int getExponent(double d) {
         /*
          * 将 d 按位转换为 long，屏蔽指数位，向右移位，然后减去 double 的偏置以获得真正的指数值。
          */
         return (int)(((Double.doubleToRawLongBits(d) & DoubleConsts.EXP_BIT_MASK) >>
                       (DoubleConsts.SIGNIFICAND_WIDTH - 1)) - DoubleConsts.EXP_BIAS);
     }
 
     /**
      * 返回朝向第二个参数方向的第一个浮点数的相邻浮点数。如果两个参数比较为相等，则返回第二个参数。
      *
      * <p>
      * 特殊情况：
      * <ul>
      * <li> 如果任一参数是 NaN，则返回 NaN。
      *
      * <li> 如果两个参数都是带符号的零，则返回未更改的 {@code direction}（根据参数比较为相等时返回第二个参数的要求）。
      *
      * <li> 如果 {@code start} 是 ±{@link Double#MIN_VALUE} 且 {@code direction} 的值使得结果应具有更小的幅度，则返回与 {@code start} 同符号的零。
      *
      * <li> 如果 {@code start} 是无穷大且 {@code direction} 的值使得结果应具有更小的幅度，则返回与 {@code start} 同符号的 {@link Double#MAX_VALUE}。
      *
      * <li> 如果 {@code start} 等于 ±{@link Double#MAX_VALUE} 且 {@code direction} 的值使得结果应具有更大的幅度，则返回与 {@code start} 同符号的无穷大。
      * </ul>
      *
      * @param start  起始浮点值
      * @param direction 指示应返回 {@code start} 的哪个邻居或 {@code start} 的值
      * @return 朝向 {@code direction} 方向的 {@code start} 的相邻浮点数。
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
          * 这些情况无需额外测试即可自然处理
          */
     
         // 首先检查 NaN 值
         if (Double.isNaN(start) || Double.isNaN(direction)) {
             // 返回从输入 NaN(s) 派生的 NaN
             return start + direction;
         } else if (start == direction) {
             return direction;
         } else {        // start > direction 或 start < direction
             // 添加 +0.0 以消除 -0.0 (+0.0 + -0.0 => +0.0)
             // 然后将 start 按位转换为整数。
             long transducer = Double.doubleToRawLongBits(start + 0.0d);
     
             /*
              * IEEE 754 浮点数如果被视为带符号幅度整数，则按字典序排序。
              * 由于 Java 的整数是二进制补码，
              * 对逻辑上负的浮点值的二进制补码表示进行“递增”会使带符号幅度表示“递减”。
              * 因此，当浮点值的整数表示小于零时，
              * 对表示的调整方向与最初预期相反。
              */
             if (direction > start) { // 计算下一个更大值
                 transducer = transducer + (transducer >= 0L ? 1L:-1L);
             } else  { // 计算下一个较小值
                 assert direction < start;
                 if (transducer > 0L)
                     --transducer;
                 else
                     if (transducer < 0L )
                         ++transducer;
                     /*
                      * transducer==0，结果是 -MIN_VALUE
                      *
                      * 从零（隐式正）到最小的负带符号幅度值的转换
                      * 必须显式完成。
                      */
                     else
                         transducer = DoubleConsts.SIGN_BIT_MASK | 1L;
             }
     
             return Double.longBitsToDouble(transducer);
         }
     }
     
     /**
      * 返回朝向第二个参数方向的第一个浮点数的相邻浮点数。如果两个参数比较为相等，则返回与第二个参数等价的值。
      *
      * <p>
      * 特殊情况：
      * <ul>
      * <li> 如果任一参数是 NaN，则返回 NaN。
      *
      * <li> 如果两个参数都是带符号的零，则返回与 {@code direction} 等价的值。
      *
      * <li> 如果 {@code start} 是 ±{@link Float#MIN_VALUE} 且 {@code direction} 的值使得结果应具有更小的幅度，则返回与 {@code start} 同符号的零。
      *
      * <li> 如果 {@code start} 是无穷大且 {@code direction} 的值使得结果应具有更小的幅度，则返回与 {@code start} 同符号的 {@link Float#MAX_VALUE}。
      *
      * <li> 如果 {@code start} 等于 ±{@link Float#MAX_VALUE} 且 {@code direction} 的值使得结果应具有更大的幅度，则返回与 {@code start} 同符号的无穷大。
      * </ul>
      *
      * @param start  起始浮点值
      * @param direction 指示应返回 {@code start} 的哪个邻居或 {@code start} 的值
      * @return 朝向 {@code direction} 方向的 {@code start} 的相邻浮点数。
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
          * 这些情况无需额外测试即可自然处理
          */
     
         // 首先检查 NaN 值
         if (Float.isNaN(start) || Double.isNaN(direction)) {
             // 返回从输入 NaN(s) 派生的 NaN
             return start + (float)direction;
         } else if (start == direction) {
             return (float)direction;
         } else {        // start > direction 或 start < direction
             // 添加 +0.0 以消除 -0.0 (+0.0 + -0.0 => +0.0)
             // 然后将 start 按位转换为整数。
             int transducer = Float.floatToRawIntBits(start + 0.0f);
     
             /*
              * IEEE 754 浮点数如果被视为带符号幅度整数，则按字典序排序。
              * 由于 Java 的整数是二进制补码，
              * 对逻辑上负的浮点值的二进制补码表示进行“递增”会使带符号幅度表示“递减”。
              * 因此，当浮点值的整数表示小于零时，
              * 对表示的调整方向与最初预期相反。
              */
             if (direction > start) {// 计算下一个更大值
                 transducer = transducer + (transducer >= 0 ? 1:-1);
             } else  { // 计算下一个较小值
                 assert direction < start;
                 if (transducer > 0)
                     --transducer;
                 else
                     if (transducer < 0 )
                         ++transducer;
                     /*
                      * transducer==0，结果是 -MIN_VALUE
                      *
                      * 从零（隐式正）到最小的负带符号幅度值的转换
                      * 必须显式完成。
                      */
                     else
                         transducer = FloatConsts.SIGN_BIT_MASK | 1;
             }
     
             return Float.intBitsToFloat(transducer);
         }
     }
     
     /**
      * 返回朝向正无穷大的 {@code d} 的相邻浮点值。该方法在语义上等价于 {@code nextAfter(d, Double.POSITIVE_INFINITY)}；
      * 然而，{@code nextUp} 实现可能比其等价的 {@code nextAfter} 调用运行得更快。
      *
      * <p>特殊情况：
      * <ul>
      * <li> 如果参数是 NaN，则结果是 NaN。
      *
      * <li> 如果参数是正无穷大，则结果是正无穷大。
      *
      * <li> 如果参数是零，则结果是 {@link Double#MIN_VALUE}
      *
      * </ul>
      *
      * @param d 起始浮点值
      * @return 更接近正无穷大的相邻浮点值。
      * @since 1.6
      */
     public static double nextUp(double d) {
         if( Double.isNaN(d) || d == Double.POSITIVE_INFINITY)
             return d;
         else {
             d += 0.0d;
             return Double.longBitsToDouble(Double.doubleToRawLongBits(d) +
                                            ((d >= 0.0d)?+1L:-1L));
         }
     }
     
     /**
      * 返回朝向正无穷大的 {@code f} 的相邻浮点值。该方法在语义上等价于 {@code nextAfter(f, Float.POSITIVE_INFINITY)}；
      * 然而，{@code nextUp} 实现可能比其等价的 {@code nextAfter} 调用运行得更快。
      *
      * <p>特殊情况：
      * <ul>
      * <li> 如果参数是 NaN，则结果是 NaN。
      *
      * <li> 如果参数是正无穷大，则结果是正无穷大。
      *
      * <li> 如果参数是零，则结果是 {@link Float#MIN_VALUE}
      *
      * </ul>
      *
      * @param f 起始浮点值
      * @return 更接近正无穷大的相邻浮点值。
      * @since 1.6
      */
     public static float nextUp(float f) {
         if( Float.isNaN(f) || f == FloatConsts.POSITIVE_INFINITY)
             return f;
         else {
             f += 0.0f;
             return Float.intBitsToFloat(Float.floatToRawIntBits(f) +
                                         ((f >= 0.0f)?+1:-1));
         }
     }
     
     /**
      * 返回朝向负无穷大的 {@code d} 的相邻浮点值。该方法在语义上等价于 {@code nextAfter(d, Double.NEGATIVE_INFINITY)}；
      * 然而，{@code nextDown} 实现可能比其等价的 {@code nextAfter} 调用运行得更快。
      *
      * <p>特殊情况：
      * <ul>
      * <li> 如果参数是 NaN，则结果是 NaN。
      *
      * <li> 如果参数是负无穷大，则结果是负无穷大。
      *
      * <li> 如果参数是零，则结果是 {@code -Double.MIN_VALUE}
      *
      * </ul>
      *
      * @param d  起始浮点值
      * @return 更接近负无穷大的相邻浮点值。
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
                                                ((d > 0.0d)?-1L:+1L));
         }
     }
     
     /**
      * 返回朝向负无穷大的 {@code f} 的相邻浮点值。该方法在语义上等价于 {@code nextAfter(f, Float.NEGATIVE_INFINITY)}；
      * 然而，{@code nextDown} 实现可能比其等价的 {@code nextAfter} 调用运行得更快。
      *
      * <p>特殊情况：
      * <ul>
      * <li> 如果参数是 NaN，则结果是 NaN。
      *
      * <li> 如果参数是负无穷大，则结果是负无穷大。
      *
      * <li> 如果参数是零，则结果是 {@code -Float.MIN_VALUE}
      *
      * </ul>
      *
      * @param f  起始浮点值
      * @return 更接近负无穷大的相邻浮点值。
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
                                             ((f > 0.0f)?-1:+1));
         }
     }
     
     /**
      * 返回 {@code d} × 2<sup>{@code scaleFactor}</sup>，如同通过单一正确四舍五入的浮点乘法到双精度值集合的成员一样进行四舍五入。
      * 有关浮点值集合的讨论，请参见 Java 语言规范。如果结果的指数在 {@link Double#MIN_EXPONENT} 和 {@link Double#MAX_EXPONENT} 之间，
      * 答案将精确计算。如果结果的指数大于 {@code Double.MAX_EXPONENT}，则返回无穷大。
      * 请注意，如果结果是次正规数，可能会丢失精度；也就是说，当 {@code scalb(x, n)} 是次正规数时，
      * {@code scalb(scalb(x, n), -n)} 可能不等于 <i>x</i>。当结果不是 NaN 时，结果与 {@code d} 的符号相同。
      *
      * <p>特殊情况：
      * <ul>
      * <li> 如果第一个参数是 NaN，则返回 NaN。
      * <li> 如果第一个参数是无穷大，则返回与该参数同符号的无穷大。
      * <li> 如果第一个参数是零，则返回与该参数同符号的零。
      * </ul>
      *
      * @param d 要按二的幂缩放的数字。
      * @param scaleFactor 用于缩放 {@code d} 的二的幂。
      * @return {@code d} × 2<sup>{@code scaleFactor}</sup>
      * @since 1.6
      */
     public static double scalb(double d, int scaleFactor) {
         /*
          * 该方法无需声明为 strictfp 即可在所有平台上计算相同的正确结果。
          * 放大时，乘法存储操作的顺序无关紧要；无论操作顺序如何，结果将是有限的或溢出。
          * 然而，为了在缩小规模时获得正确结果，必须使用特定的顺序。
          *
          * 缩小规模时，乘法存储操作按顺序进行，以确保不会连续两次返回次正规结果。
          * 如果一个乘法存储结果是次正规的，下一次乘法会将其四舍五入为零。
          * 这是通过首先乘以 2 ^ (scaleFactor % n)，然后根据需要多次乘以 2^n 来完成的，
          * 其中 n 是方便的二的幂的指数。这样，最多只会发生一次真正的舍入误差。
          * 如果仅使用双精度值集合，舍入将在乘法时发生。
          * 如果使用双精度扩展指数值集合，乘积可能会是精确的，但存储到 d 时保证会四舍五入到双精度值集合。
          *
          * 首先将 d 乘以 2^MIN_EXPONENT 然后再乘以 2 ^ (scaleFactor % MIN_EXPONENT) 的实现是不可行的，
          * 因为即使在 strictfp 程序中，也可能在下溢时发生双重舍入；
          * 例如，如果 scaleFactor 参数是 (MIN_EXPONENT - n) 并且 d 的指数略小于 -(MIN_EXPONENT - n)，
          * 意味着最终结果将是次正规的。
          *
          * 由于无需过多的性能负担即可实现此方法的精确可重现性，
          * 没有令人信服的理由允许在 scalb 中下溢时进行双重舍入。
          */
     
         // 二的幂的幅度如此之大，以至于将其缩放有限非零值将保证溢出或下溢；
         // 由于舍入，缩小规模需要额外的二的幂，这在此处反映出来
         final int MAX_SCALE = DoubleConsts.MAX_EXPONENT + -DoubleConsts.MIN_EXPONENT +
                               DoubleConsts.SIGNIFICAND_WIDTH + 1;
         int exp_adjust = 0;
         int scale_increment = 0;
         double exp_delta = Double.NaN;
     
         // 确保缩放因子在合理范围内
         if(scaleFactor < 0) {
             scaleFactor = Math.max(scaleFactor, -MAX_SCALE);
             scale_increment = -512;
             exp_delta = twoToTheDoubleScaleDown;
         }
         else {
             scaleFactor = Math.min(scaleFactor, MAX_SCALE);
             scale_increment = 512;
             exp_delta = twoToTheDoubleScaleUp;
         }
     
         // 计算 (scaleFactor % +/-512)，512 = 2^9，使用
         // “Hacker's Delight” 第 10-2 节中的技术。
         int t = (scaleFactor >> 9-1) >>> 32 - 9;
         exp_adjust = ((scaleFactor + t) & (512 -1)) - t;
     
         d *= powerOfTwoD(exp_adjust);
         scaleFactor -= exp_adjust;
     
         while(scaleFactor != 0) {
             d *= exp_delta;
             scaleFactor -= scale_increment;
         }
         return d;
     }
     
     /**
      * 返回 {@code f} × 2<sup>{@code scaleFactor}</sup>，如同通过单一正确四舍五入的浮点乘法到单精度值集合的成员一样进行四舍五入。
      * 有关浮点值集合的讨论，请参见 Java 语言规范。如果结果的指数在 {@link Float#MIN_EXPONENT} 和 {@link Float#MAX_EXPONENT} 之间，
      * 答案将精确计算。如果结果的指数大于 {@code Float.MAX_EXPONENT}，则返回无穷大。
      * 请注意，如果结果是次正规数，可能会丢失精度；也就是说，当 {@code scalb(x, n)} 是次正规数时，
      * {@code scalb(scalb(x, n), -n)} 可能不等于 <i>x</i>。当结果不是 NaN 时，结果与 {@code f} 的符号相同。
      *
      * <p>特殊情况：
      * <ul>
      * <li> 如果第一个参数是 NaN，则返回 NaN。
      * <li> 如果第一个参数是无穷大，则返回与该参数同符号的无穷大。
      * <li> 如果第一个参数是零，则返回与该参数同符号的零。
      * </ul>
      *
      * @param f 要按二的幂缩放的数字。
      * @param scaleFactor 用于缩放 {@code f} 的二的幂。
      * @return {@code f} × 2<sup>{@code scaleFactor}</sup>
      * @since 1.6
      */
     public static float scalb(float f, int scaleFactor) {
         // 二的幂的幅度如此之大，以至于将其缩放有限非零值将保证溢出或下溢；
         // 由于舍入，缩小规模需要额外的二的幂，这在此处反映出来
         final int MAX_SCALE = FloatConsts.MAX_EXPONENT + -FloatConsts.MIN_EXPONENT +
                               FloatConsts.SIGNIFICAND_WIDTH + 1;
     
         // 确保缩放因子在合理范围内
         scaleFactor = Math.max(Math.min(scaleFactor, MAX_SCALE), -MAX_SCALE);
     
         /*
          * 由于浮点的 + MAX_SCALE 很好地适应双精度指数范围，
          * 并且从浮点到双精度的转换是精确的，
          * 因此下面的乘法将是精确的。因此，当双精度乘积被转换为浮点时发生的舍入
          * 将是正确四舍五入的浮点结果。由于除了最终乘法之外的所有操作都是精确的，
          * 因此没有必要声明此方法为 strictfp。
          */
         return (float)((double)f*powerOfTwoD(scaleFactor));
     }
     
     // scalb 中使用的常量
     static double twoToTheDoubleScaleUp = powerOfTwoD(512);
     static double twoToTheDoubleScaleDown = powerOfTwoD(-512);
     
     /**
      * 返回正常范围内的二的浮点幂。
      */
     static double powerOfTwoD(int n) {
         assert(n >= DoubleConsts.MIN_EXPONENT && n <= DoubleConsts.MAX_EXPONENT);
         return Double.longBitsToDouble((((long)n + (long)DoubleConsts.EXP_BIAS) <<
                                         (DoubleConsts.SIGNIFICAND_WIDTH-1))
                                        & DoubleConsts.EXP_BIT_MASK);
     }
     
     /**
      * 返回正常范围内的二的浮点幂。
      */
     static float powerOfTwoF(int n) {
         assert(n >= FloatConsts.MIN_EXPONENT && n <= FloatConsts.MAX_EXPONENT);
         return Float.intBitsToFloat(((n + FloatConsts.EXP_BIAS) <<
                                      (FloatConsts.SIGNIFICAND_WIDTH-1))
                                     & FloatConsts.EXP_BIT_MASK);
     }
}