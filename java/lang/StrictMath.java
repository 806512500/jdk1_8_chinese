
/*
 * 版权所有 (c) 1999, 2013, Oracle 和/或其关联公司。保留所有权利。
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
import java.util.Random;
import sun.misc.DoubleConsts;

/**
 * 类 {@code StrictMath} 包含执行基本数值操作的方法，如基本的指数、对数、平方根和三角函数。
 *
 * <p>为了帮助确保 Java 程序的可移植性，此包中某些数值函数的定义要求它们产生与某些已发布的算法相同的结果。这些算法可从知名的网络库
 * {@code netlib} 作为 "Freely Distributable Math
 * Library," <a
 * href="ftp://ftp.netlib.org/fdlibm.tar">{@code fdlibm}</a> 包中获取。这些算法是用 C 编程语言编写的，应理解为所有浮点运算都遵循 Java 浮点算术规则。
 *
 * <p>Java 数学库是针对
 * {@code fdlibm} 5.3 版本定义的。当 {@code fdlibm} 为一个函数提供多个定义（如
 * {@code acos}）时，应使用“IEEE 754 核心函数”版本
 * （位于文件名以字母
 * {@code e} 开头的文件中）。需要 {@code fdlibm} 语义的方法是 {@code sin}, {@code cos}, {@code tan},
 * {@code asin}, {@code acos}, {@code atan},
 * {@code exp}, {@code log}, {@code log10},
 * {@code cbrt}, {@code atan2}, {@code pow},
 * {@code sinh}, {@code cosh}, {@code tanh},
 * {@code hypot}, {@code expm1}, 和 {@code log1p}。
 *
 * <p>
 * 平台使用带符号的二进制补码整数算术，具有 int 和 long 原始类型。开发人员应选择原始类型以确保算术运算始终产生正确的结果，这在某些情况下意味着运算不会超出计算值的范围。
 * 最佳做法是选择原始类型和算法以避免溢出。在大小为 {@code int} 或 {@code long} 且需要检测溢出错误的情况下，方法 {@code addExact},
 * {@code subtractExact}, {@code multiplyExact}, 和 {@code toIntExact}
 * 当结果溢出时会抛出一个 {@code ArithmeticException}。对于其他算术运算，如除法、绝对值、递增、递减和取反，溢出仅在特定的最小值或最大值时发生，应根据情况检查最小值或最大值。
 *
 * @author  未署名
 * @author  Joseph D. Darcy
 * @since   1.3
 */

public final class StrictMath {

    /**
     * 不允许任何人实例化此类。
     */
    private StrictMath() {}

    /**
     * 比任何其他值都更接近自然对数的底数 <i>e</i> 的 {@code double} 值。
     */
    public static final double E = 2.7182818284590452354;

    /**
     * 比任何其他值都更接近圆周率 <i>pi</i> 的 {@code double} 值，即圆的周长与其直径的比值。
     */
    public static final double PI = 3.14159265358979323846;

    /**
     * 返回一个角度的正弦值。特殊情况：
     * <ul><li>如果参数是 NaN 或无穷大，那么结果是 NaN。
     * <li>如果参数是零，那么结果是一个与参数符号相同的零。</ul>
     *
     * @param   a   一个角度，以弧度为单位。
     * @return  参数的正弦值。
     */
    public static native double sin(double a);

    /**
     * 返回一个角度的余弦值。特殊情况：
     * <ul><li>如果参数是 NaN 或无穷大，那么结果是 NaN。</ul>
     *
     * @param   a   一个角度，以弧度为单位。
     * @return  参数的余弦值。
     */
    public static native double cos(double a);

    /**
     * 返回一个角度的正切值。特殊情况：
     * <ul><li>如果参数是 NaN 或无穷大，那么结果是 NaN。
     * <li>如果参数是零，那么结果是一个与参数符号相同的零。</ul>
     *
     * @param   a   一个角度，以弧度为单位。
     * @return  参数的正切值。
     */
    public static native double tan(double a);

    /**
     * 返回一个值的反正弦；返回的角度在 -<i>pi</i>/2 到 <i>pi</i>/2 的范围内。特殊情况：
     * <ul><li>如果参数是 NaN 或其绝对值大于 1，那么结果是 NaN。
     * <li>如果参数是零，那么结果是一个与参数符号相同的零。</ul>
     *
     * @param   a   要返回反正弦的值。
     * @return  参数的反正弦。
     */
    public static native double asin(double a);

    /**
     * 返回一个值的反余弦；返回的角度在 0.0 到 <i>pi</i> 的范围内。特殊情况：
     * <ul><li>如果参数是 NaN 或其绝对值大于 1，那么结果是 NaN。</ul>
     *
     * @param   a   要返回反余弦的值。
     * @return  参数的反余弦。
     */
    public static native double acos(double a);

    /**
     * 返回一个值的反正切；返回的角度在 -<i>pi</i>/2 到 <i>pi</i>/2 的范围内。特殊情况：
     * <ul><li>如果参数是 NaN，那么结果是 NaN。
     * <li>如果参数是零，那么结果是一个与参数符号相同的零。</ul>
     *
     * @param   a   要返回反正切的值。
     * @return  参数的反正切。
     */
    public static native double atan(double a);

    /**
     * 将以度为单位的角度转换为以弧度为单位的大致等效角度。从度到弧度的转换通常是不精确的。
     *
     * @param   angdeg   一个角度，以度为单位
     * @return  角度 {@code angdeg} 以弧度为单位的测量值。
     */
    public static strictfp double toRadians(double angdeg) {
        // 不委托给 Math.toRadians(angdeg)，因为此方法有 strictfp 修饰符。
        return angdeg / 180.0 * PI;
    }

                    /**
     * 将以弧度为单位测量的角度转换为以度为单位测量的近似等效角度。从弧度到度的转换通常是不精确的；用户
     * <i>不应</i>期望 {@code cos(toRadians(90.0))} 精确等于 {@code 0.0}。
     *
     * @param   angrad   以弧度为单位的角度
     * @return  以度为单位测量的角度 {@code angrad}。
     */
    public static strictfp double toDegrees(double angrad) {
        // 不委托给 Math.toDegrees(angrad)，因为此方法具有 strictfp 修饰符。
        return angrad * 180.0 / PI;
    }

    /**
     * 返回欧拉数 <i>e</i> 的 {@code double} 值次幂。特殊情况：
     * <ul><li>如果参数是 NaN，结果是 NaN。
     * <li>如果参数是正无穷大，那么结果是
     * 正无穷大。
     * <li>如果参数是负无穷大，那么结果是
     * 正零。</ul>
     *
     * @param   a   要提升到的指数。
     * @return  值 <i>e</i><sup>{@code a}</sup>，
     *          其中 <i>e</i> 是自然对数的底数。
     */
    public static native double exp(double a);

    /**
     * 返回 {@code double} 值的自然对数（以 <i>e</i> 为底）。特殊情况：
     * <ul><li>如果参数是 NaN 或小于零，那么结果
     * 是 NaN。
     * <li>如果参数是正无穷大，那么结果是
     * 正无穷大。
     * <li>如果参数是正零或负零，那么结果是
     * 负无穷大。</ul>
     *
     * @param   a   一个值
     * @return  值 ln&nbsp;{@code a}，即
     *          {@code a} 的自然对数。
     */
    public static native double log(double a);


    /**
     * 返回 {@code double} 值的以 10 为底的对数。
     * 特殊情况：
     *
     * <ul><li>如果参数是 NaN 或小于零，那么结果
     * 是 NaN。
     * <li>如果参数是正无穷大，那么结果是
     * 正无穷大。
     * <li>如果参数是正零或负零，那么结果是
     * 负无穷大。
     * <li> 如果参数等于 10<sup><i>n</i></sup>，其中 <i>n</i> 是整数，那么结果是 <i>n</i>。
     * </ul>
     *
     * @param   a   一个值
     * @return  {@code a} 的以 10 为底的对数。
     * @since 1.5
     */
    public static native double log10(double a);

    /**
     * 返回 {@code double} 值的正确舍入的正平方根。
     * 特殊情况：
     * <ul><li>如果参数是 NaN 或小于零，那么结果
     * 是 NaN。
     * <li>如果参数是正无穷大，那么结果是正
     * 无穷大。
     * <li>如果参数是正零或负零，那么结果与参数相同。</ul>
     * 否则，结果是与参数值的真实数学平方根最接近的 {@code double} 值。
     *
     * @param   a   一个值。
     * @return  {@code a} 的正平方根。
     */
    public static native double sqrt(double a);

    /**
     * 返回 {@code double} 值的立方根。对于
     * 正有限的 {@code x}，{@code cbrt(-x) ==
     * -cbrt(x)}；也就是说，负值的立方根
     * 是该值的大小的立方根的负数。
     * 特殊情况：
     *
     * <ul>
     *
     * <li>如果参数是 NaN，那么结果是 NaN。
     *
     * <li>如果参数是无穷大，那么结果是与参数符号相同的无穷大。
     *
     * <li>如果参数是零，那么结果是与参数符号相同的零。
     *
     * </ul>
     *
     * @param   a   一个值。
     * @return  {@code a} 的立方根。
     * @since 1.5
     */
    public static native double cbrt(double a);

    /**
     * 按照 IEEE 754 标准计算两个参数的余数操作。
     * 余数值在数学上等于
     * <code>f1&nbsp;-&nbsp;f2</code>&nbsp;&times;&nbsp;<i>n</i>，
     * 其中 <i>n</i> 是最接近 {@code f1/f2} 的精确数学值的数学整数，如果两个
     * 数学整数与 {@code f1/f2} 同等接近，
     * 那么 <i>n</i> 是偶数的那个整数。如果余数为
     * 零，其符号与第一个参数的符号相同。
     * 特殊情况：
     * <ul><li>如果任一参数是 NaN，或者第一个参数是无穷大，
     * 或者第二个参数是正零或负零，那么
     * 结果是 NaN。
     * <li>如果第一个参数是有限的，而第二个参数是
     * 无穷大，那么结果与第一个参数相同。</ul>
     *
     * @param   f1   被除数。
     * @param   f2   除数。
     * @return  当 {@code f1} 除以
     *          {@code f2} 时的余数。
     */
    public static native double IEEEremainder(double f1, double f2);

    /**
     * 返回大于或等于参数且等于数学整数的最小（最接近负无穷大）
     * {@code double} 值。特殊情况：
     * <ul><li>如果参数值已经等于
     * 数学整数，那么结果与
     * 参数相同。  <li>如果参数是 NaN 或无穷大或
     * 正零或负零，那么结果与
     * 参数相同。  <li>如果参数值小于零但
     * 大于 -1.0，那么结果是负零。</ul> 注意
     * {@code StrictMath.ceil(x)} 的值正好是
     * {@code -StrictMath.floor(-x)} 的值。
     *
     * @param   a   一个值。
     * @return  大于或等于
     *          参数且等于数学整数的最小（最接近负无穷大）
     *          浮点数。
     */
    public static double ceil(double a) {
        return floorOrCeil(a, -0.0, 1.0, 1.0);
    }


/**
 * 返回小于或等于参数的最大（最接近正无穷大）
 * {@code double} 值，该值等于数学整数。特殊情况：
 * <ul><li>如果参数值已经等于数学整数，则结果与参数相同。
 * <li>如果参数是 NaN 或无穷大或正零或负零，则结果与参数相同。</ul>
 *
 * @param   a   一个值。
 * @return  小于或等于参数的最大（最接近正无穷大）
 *          浮点值，该值等于数学整数。
 */
public static double floor(double a) {
    return floorOrCeil(a, -1.0, 0.0, -1.0);
}

/**
 * 用于 floor 和 ceil 之间共享逻辑的内部方法。
 *
 * @param a 要取地板或天花板的值
 * @param negativeBoundary 负数边界内的结果
 * @param positiveBoundary 正数边界内的结果
 * @param increment 参数非整数时要添加的值
 */
private static double floorOrCeil(double a,
                                  double negativeBoundary,
                                  double positiveBoundary,
                                  double sign) {
    int exponent = Math.getExponent(a);

    if (exponent < 0) {
        /*
         * 参数的绝对值小于 1。
         * floorOrceil(-0.0) => -0.0
         * floorOrceil(+0.0) => +0.0
         */
        return ((a == 0.0) ? a :
                ( (a < 0.0) ?  negativeBoundary : positiveBoundary) );
    } else if (exponent >= 52) {
        /*
         * 无穷大、NaN 或值非常大，必须是整数。
         */
        return a;
    }
    // 否则，参数已经是整数值 XOR 需要四舍五入为一个。
    assert exponent >= 0 && exponent <= 51;

    long doppel = Double.doubleToRawLongBits(a);
    long mask   = DoubleConsts.SIGNIF_BIT_MASK >> exponent;

    if ( (mask & doppel) == 0L )
        return a; // 整数值
    else {
        double result = Double.longBitsToDouble(doppel & (~mask));
        if (sign*a > 0.0)
            result = result + sign;
        return result;
    }
}

/**
 * 返回最接近参数值的 {@code double} 值，该值等于数学整数。如果两个
 * {@code double} 值都是数学整数并且与参数值的距离相等，则结果是
 * 偶数的整数值。特殊情况：
 * <ul><li>如果参数值已经等于数学整数，则结果与参数相同。
 * <li>如果参数是 NaN 或无穷大或正零或负零，则结果与参数相同。</ul>
 *
 * @param   a   一个值。
 * @return  最接近 {@code a} 的浮点值，该值等于数学整数。
 * @author Joseph D. Darcy
 */
public static double rint(double a) {
    /*
     * 如果 a 的绝对值不小于 2^52，则它要么是有限整数（double 格式没有足够的有效位数来表示如此大的数字，因此没有小数部分），要么是无穷大，要么是 NaN。在这些情况下，rint 的结果就是参数本身。
     *
     * 否则，和 (twoToThe52 + a) 将正确地舍入 a 的任何小数部分，因为 ulp(twoToThe52) == 1.0；从这个和中减去 twoToThe52 将是精确的，并且会留下 a 的四舍五入的整数部分。
     *
     * 该方法不需要声明为 strictfp 以获得完全可重复的结果。只有当某些操作在 strictfp 语义下溢出或下溢时，声明一个方法是否为 strictfp 才会有差异。操作 (twoToThe52 + a) 不会溢出，因为 a 的大值已被筛选掉；加法不会下溢，因为 twoToThe52 太大。减法 ((twoToThe52 + a) - twoToThe52) 将如上所述是精确的，因此不会溢出或有意义地下溢。最后，返回语句中的乘法是乘以正负 1.0，这也是精确的。
     */
    double twoToThe52 = (double)(1L << 52); // 2^52
    double sign = Math.copySign(1.0, a); // 保留符号信息
    a = Math.abs(a);

    if (a < twoToThe52) { // E_min <= ilogb(a) <= 51
        a = ((twoToThe52 + a ) - twoToThe52);
    }

    return sign * a; // 恢复原始符号
}

/**
 * 返回从直角坐标 ({@code x},&nbsp;{@code y}) 转换到极坐标 (r,&nbsp;<i>theta</i>) 的角度 <i>theta</i>。
 * 该方法通过计算 {@code y/x} 的反正切值来计算相位 <i>theta</i>，范围为 -<i>pi</i> 到 <i>pi</i>。特殊情况：
 * <ul><li>如果任一参数为 NaN，则结果为 NaN。
 * <li>如果第一个参数为正零且第二个参数为正，或者第一个参数为正且有限且第二个参数为正无穷大，则结果为正零。
 * <li>如果第一个参数为负零且第二个参数为正，或者第一个参数为负且有限且第二个参数为正无穷大，则结果为负零。
 * <li>如果第一个参数为正零且第二个参数为负，或者第一个参数为正且有限且第二个参数为负无穷大，则结果为最接近 <i>pi</i> 的 {@code double} 值。
 * <li>如果第一个参数为负零且第二个参数为负，或者第一个参数为负且有限且第二个参数为负无穷大，则结果为最接近 -<i>pi</i> 的 {@code double} 值。
 * <li>如果第一个参数为正且第二个参数为正零或负零，或者第一个参数为正无穷大且第二个参数为有限值，则结果为最接近 <i>pi</i>/2 的 {@code double} 值。
 * <li>如果第一个参数为负且第二个参数为正零或负零，或者第一个参数为负无穷大且第二个参数为有限值，则结果为最接近 -<i>pi</i>/2 的 {@code double} 值。
 * <li>如果两个参数均为正无穷大，则结果为最接近 <i>pi</i>/4 的 {@code double} 值。
 * <li>如果第一个参数为正无穷大且第二个参数为负无穷大，则结果为最接近 3*<i>pi</i>/4 的 {@code double} 值。
 * <li>如果第一个参数为负无穷大且第二个参数为正无穷大，则结果为最接近 -<i>pi</i>/4 的 {@code double} 值。
 * <li>如果两个参数均为负无穷大，则结果为最接近 -3*<i>pi</i>/4 的 {@code double} 值。</ul>
 *
 * @param   y   纵坐标
 * @param   x   横坐标
 * @return  点
 *          (<i>r</i>,&nbsp;<i>theta</i>)
 *          在极坐标中对应于点
 *          (<i>x</i>,&nbsp;<i>y</i>) 在笛卡尔坐标中的 <i>theta</i> 分量。
 */
public static native double atan2(double y, double x);

    /**
     * 返回第一个参数提高到第二个参数的幂的值。特殊情况：
     *
     * <ul><li>如果第二个参数是正数或负数零，则结果是1.0。
     * <li>如果第二个参数是1.0，则结果与第一个参数相同。
     * <li>如果第二个参数是NaN，则结果是NaN。
     * <li>如果第一个参数是NaN且第二个参数是非零数，则结果是NaN。
     *
     * <li>如果
     * <ul>
     * <li>第一个参数的绝对值大于1且第二个参数是正无穷大，或者
     * <li>第一个参数的绝对值小于1且第二个参数是负无穷大，
     * </ul>
     * 则结果是正无穷大。
     *
     * <li>如果
     * <ul>
     * <li>第一个参数的绝对值大于1且第二个参数是负无穷大，或者
     * <li>第一个参数的绝对值小于1且第二个参数是正无穷大，
     * </ul>
     * 则结果是正零。
     *
     * <li>如果第一个参数的绝对值等于1且第二个参数是无穷大，则结果是NaN。
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
     * <li>第一个参数是负零且第二个参数大于零但不是有限的奇数整数，或者
     * <li>第一个参数是负无穷大且第二个参数小于零但不是有限的奇数整数，
     * </ul>
     * 则结果是正零。
     *
     * <li>如果
     * <ul>
     * <li>第一个参数是负零且第二个参数是正的有限奇数整数，或者
     * <li>第一个参数是负无穷大且第二个参数是负的有限奇数整数，
     * </ul>
     * 则结果是负零。
     *
     * <li>如果
     * <ul>
     * <li>第一个参数是负零且第二个参数小于零但不是有限的奇数整数，或者
     * <li>第一个参数是负无穷大且第二个参数大于零但不是有限的奇数整数，
     * </ul>
     * 则结果是正无穷大。
     *
     * <li>如果
     * <ul>
     * <li>第一个参数是负零且第二个参数是负的有限奇数整数，或者
     * <li>第一个参数是负无穷大且第二个参数是正的有限奇数整数，
     * </ul>
     * 则结果是负无穷大。
     *
     * <li>如果第一个参数是有限且小于零
     * <ul>
     * <li>如果第二个参数是有限的偶数整数，则结果等于将第一个参数的绝对值提高到第二个参数的幂的结果
     *
     * <li>如果第二个参数是有限的奇数整数，则结果等于将第一个参数的绝对值提高到第二个参数的幂的结果的负数
     *
     * <li>如果第二个参数是有限且不是整数，则结果是NaN。
     * </ul>
     *
     * <li>如果两个参数都是整数，那么结果恰好等于将第一个参数提高到第二个参数的幂的数学结果，如果该结果确实可以表示为一个 {@code double} 值。</ul>
     *
     * <p>(在上述描述中，如果一个浮点值是有限且是 {@link #ceil ceil} 方法或等效的 {@link #floor floor} 方法的不动点，则该浮点值被认为是整数。一个值是一个一参数方法的不动点当且仅当将该方法应用于该值的结果等于该值。)
     *
     * @param   a   基数。
     * @param   b   指数。
     * @return  值 {@code a}<sup>{@code b}</sup>。
     */
    public static native double pow(double a, double b);

    /**
     * 返回最接近参数的 {@code int} 值，平局时向正无穷大舍入。
     *
     * <p>特殊情况：
     * <ul><li>如果参数是NaN，结果是0。
     * <li>如果参数是负无穷大或任何小于或等于 {@code Integer.MIN_VALUE} 的值，结果等于 {@code Integer.MIN_VALUE} 的值。
     * <li>如果参数是正无穷大或任何大于或等于 {@code Integer.MAX_VALUE} 的值，结果等于 {@code Integer.MAX_VALUE} 的值。</ul>
     *
     * @param   a   要四舍五入为整数的浮点值。
     * @return  参数四舍五入到最接近的 {@code int} 值。
     * @see     java.lang.Integer#MAX_VALUE
     * @see     java.lang.Integer#MIN_VALUE
     */
    public static int round(float a) {
        return Math.round(a);
    }

    /**
     * 返回最接近参数的 {@code long} 值，平局时向正无穷大舍入。
     *
     * <p>特殊情况：
     * <ul><li>如果参数是NaN，结果是0。
     * <li>如果参数是负无穷大或任何小于或等于 {@code Long.MIN_VALUE} 的值，结果等于 {@code Long.MIN_VALUE} 的值。
     * <li>如果参数是正无穷大或任何大于或等于 {@code Long.MAX_VALUE} 的值，结果等于 {@code Long.MAX_VALUE} 的值。</ul>
     *
     * @param   a  要四舍五入为 {@code long} 的浮点值。
     * @return  参数四舍五入到最接近的 {@code long} 值。
     * @see     java.lang.Long#MAX_VALUE
     * @see     java.lang.Long#MIN_VALUE
     */
    public static long round(double a) {
        return Math.round(a);
    }


                    private static final class RandomNumberGeneratorHolder {
        static final Random randomNumberGenerator = new Random();
    }

    /**
     * 返回一个带有正号的 {@code double} 值，大于等于 {@code 0.0} 且小于 {@code 1.0}。
     * 返回的值是从该范围内伪随机选择的，分布（大约）均匀。
     *
     * <p>当首次调用此方法时，它会创建一个新的伪随机数生成器，就像通过以下表达式创建的一样
     *
     * <blockquote>{@code new java.util.Random()}</blockquote>
     *
     * 此新的伪随机数生成器此后用于所有对该方法的调用，并且不会在其他地方使用。
     *
     * <p>此方法已正确同步，允许多个线程正确使用。
     * 但是，如果许多线程需要以高速率生成伪随机数，为每个线程拥有自己的伪随机数生成器可能会减少争用。
     *
     * @return 一个伪随机的 {@code double}，大于等于 {@code 0.0} 且小于 {@code 1.0}。
     * @see Random#nextDouble()
     */
    public static double random() {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextDouble();
    }

    /**
     * 返回其参数的和，
     * 如果结果超出 {@code int} 的范围，则抛出异常。
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return 结果
     * @throws ArithmeticException 如果结果超出 int 范围
     * @see Math#addExact(int,int)
     * @since 1.8
     */
    public static int addExact(int x, int y) {
        return Math.addExact(x, y);
    }

    /**
     * 返回其参数的和，
     * 如果结果超出 {@code long} 的范围，则抛出异常。
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return 结果
     * @throws ArithmeticException 如果结果超出 long 范围
     * @see Math#addExact(long,long)
     * @since 1.8
     */
    public static long addExact(long x, long y) {
        return Math.addExact(x, y);
    }

    /**
     * 返回其参数的差，
     * 如果结果超出 {@code int} 的范围，则抛出异常。
     *
     * @param x 第一个值
     * @param y 从第一个值中减去的第二个值
     * @return 结果
     * @throws ArithmeticException 如果结果超出 int 范围
     * @see Math#subtractExact(int,int)
     * @since 1.8
     */
    public static int subtractExact(int x, int y) {
        return Math.subtractExact(x, y);
    }

    /**
     * 返回其参数的差，
     * 如果结果超出 {@code long} 的范围，则抛出异常。
     *
     * @param x 第一个值
     * @param y 从第一个值中减去的第二个值
     * @return 结果
     * @throws ArithmeticException 如果结果超出 long 范围
     * @see Math#subtractExact(long,long)
     * @since 1.8
     */
    public static long subtractExact(long x, long y) {
        return Math.subtractExact(x, y);
    }

    /**
     * 返回其参数的乘积，
     * 如果结果超出 {@code int} 的范围，则抛出异常。
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return 结果
     * @throws ArithmeticException 如果结果超出 int 范围
     * @see Math#multiplyExact(int,int)
     * @since 1.8
     */
    public static int multiplyExact(int x, int y) {
        return Math.multiplyExact(x, y);
    }

    /**
     * 返回其参数的乘积，
     * 如果结果超出 {@code long} 的范围，则抛出异常。
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return 结果
     * @throws ArithmeticException 如果结果超出 long 范围
     * @see Math#multiplyExact(long,long)
     * @since 1.8
     */
    public static long multiplyExact(long x, long y) {
        return Math.multiplyExact(x, y);
    }

    /**
     * 返回 {@code long} 参数的值；
     * 如果值超出 {@code int} 的范围，则抛出异常。
     *
     * @param value long 值
     * @return 参数作为 int
     * @throws ArithmeticException 如果 {@code 参数} 超出 int 范围
     * @see Math#toIntExact(long)
     * @since 1.8
     */
    public static int toIntExact(long value) {
        return Math.toIntExact(value);
    }

    /**
     * 返回小于或等于代数商的最大（最接近正无穷大）的
     * {@code int} 值。有一个特殊情况，如果被除数是
     * {@linkplain Integer#MIN_VALUE Integer.MIN_VALUE} 且除数是 {@code -1}，
     * 则发生整数溢出，
     * 结果等于 {@code Integer.MIN_VALUE}。
     * <p>
     * 有关示例和与整数除法 {@code /} 运算符的比较，请参见 {@link Math#floorDiv(int, int) Math.floorDiv}。
     *
     * @param x 被除数
     * @param y 除数
     * @return 小于或等于代数商的最大（最接近正无穷大）的
     * {@code int} 值。
     * @throws ArithmeticException 如果除数 {@code y} 为零
     * @see Math#floorDiv(int, int)
     * @see Math#floor(double)
     * @since 1.8
     */
    public static int floorDiv(int x, int y) {
        return Math.floorDiv(x, y);
    }

    /**
     * 返回小于或等于代数商的最大（最接近正无穷大）的
     * {@code long} 值。有一个特殊情况，如果被除数是
     * {@linkplain Long#MIN_VALUE Long.MIN_VALUE} 且除数是 {@code -1}，
     * 则发生整数溢出，
     * 结果等于 {@code Long.MIN_VALUE}。
     * <p>
     * 有关示例和与整数除法 {@code /} 运算符的比较，请参见 {@link Math#floorDiv(int, int) Math.floorDiv}。
     *
     * @param x 被除数
     * @param y 除数
     * @return 小于或等于代数商的最大（最接近正无穷大）的
     * {@code long} 值。
     * @throws ArithmeticException 如果除数 {@code y} 为零
     * @see Math#floorDiv(long, long)
     * @see Math#floor(double)
     * @since 1.8
     */
    public static long floorDiv(long x, long y) {
        return Math.floorDiv(x, y);
    }

                    /**
     * 返回两个 {@code int} 参数的地板模数。
     * <p>
     * 地板模数是 {@code x - (floorDiv(x, y) * y)}，
     * 与除数 {@code y} 具有相同的符号，
     * 并且在 {@code -abs(y) < r < +abs(y)} 的范围内。
     * <p>
     * {@code floorDiv} 和 {@code floorMod} 之间的关系如下：
     * <ul>
     *   <li>{@code floorDiv(x, y) * y + floorMod(x, y) == x}
     * </ul>
     * <p>
     * 有关示例和与 {@code %} 运算符的比较，请参阅 {@link Math#floorMod(int, int) Math.floorMod}。
     *
     * @param x 被除数
     * @param y 除数
     * @return 地板模数 {@code x - (floorDiv(x, y) * y)}
     * @throws ArithmeticException 如果除数 {@code y} 为零
     * @see Math#floorMod(int, int)
     * @see StrictMath#floorDiv(int, int)
     * @since 1.8
     */
    public static int floorMod(int x, int y) {
        return Math.floorMod(x , y);
    }
    /**
     * 返回两个 {@code long} 参数的地板模数。
     * <p>
     * 地板模数是 {@code x - (floorDiv(x, y) * y)}，
     * 与除数 {@code y} 具有相同的符号，
     * 并且在 {@code -abs(y) < r < +abs(y)} 的范围内。
     * <p>
     * {@code floorDiv} 和 {@code floorMod} 之间的关系如下：
     * <ul>
     *   <li>{@code floorDiv(x, y) * y + floorMod(x, y) == x}
     * </ul>
     * <p>
     * 有关示例和与 {@code %} 运算符的比较，请参阅 {@link Math#floorMod(int, int) Math.floorMod}。
     *
     * @param x 被除数
     * @param y 除数
     * @return 地板模数 {@code x - (floorDiv(x, y) * y)}
     * @throws ArithmeticException 如果除数 {@code y} 为零
     * @see Math#floorMod(long, long)
     * @see StrictMath#floorDiv(long, long)
     * @since 1.8
     */
    public static long floorMod(long x, long y) {
        return Math.floorMod(x, y);
    }

    /**
     * 返回一个 {@code int} 值的绝对值。
     * 如果参数不是负数，则返回该参数。
     * 如果参数是负数，则返回该参数的相反数。
     *
     * <p>注意，如果参数等于 {@link Integer#MIN_VALUE}，
     * 即可表示的最小 {@code int} 值，结果是该相同的值，即负数。
     *
     * @param   a   要确定其绝对值的参数。
     * @return  参数的绝对值。
     */
    public static int abs(int a) {
        return Math.abs(a);
    }

    /**
     * 返回一个 {@code long} 值的绝对值。
     * 如果参数不是负数，则返回该参数。
     * 如果参数是负数，则返回该参数的相反数。
     *
     * <p>注意，如果参数等于 {@link Long#MIN_VALUE}，
     * 即可表示的最小 {@code long} 值，结果是该相同的值，即负数。
     *
     * @param   a   要确定其绝对值的参数。
     * @return  参数的绝对值。
     */
    public static long abs(long a) {
        return Math.abs(a);
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
        return Math.abs(a);
    }

    /**
     * 返回一个 {@code double} 值的绝对值。
     * 如果参数不是负数，则返回该参数。
     * 如果参数是负数，则返回该参数的相反数。
     * 特殊情况：
     * <ul><li>如果参数是正零或负零，结果是正零。
     * <li>如果参数是无穷大，结果是正无穷大。
     * <li>如果参数是 NaN，结果是 NaN。</ul>
     * 换句话说，结果与以下表达式的值相同：
     * <p>{@code Double.longBitsToDouble((Double.doubleToLongBits(a)<<1)>>>1)}
     *
     * @param   a   要确定其绝对值的参数
     * @return  参数的绝对值。
     */
    public static double abs(double a) {
        return Math.abs(a);
    }

    /**
     * 返回两个 {@code int} 值中较大的一个。也就是说，
     * 结果是更接近 {@link Integer#MAX_VALUE} 值的参数。如果两个参数的值相同，
     * 结果是相同的值。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较大的 {@code a} 和 {@code b}。
     */
    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    /**
     * 返回两个 {@code long} 值中较大的一个。也就是说，
     * 结果是更接近 {@link Long#MAX_VALUE} 值的参数。如果两个参数的值相同，
     * 结果是相同的值。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较大的 {@code a} 和 {@code b}。
        */
    public static long max(long a, long b) {
        return Math.max(a, b);
    }

    /**
     * 返回两个 {@code float} 值中较大的一个。也就是说，
     * 结果是更接近正无穷大的参数。如果两个参数的值相同，
     * 结果是相同的值。如果任一值为 NaN，则结果为 NaN。与数值比较运算符不同，
     * 此方法认为负零严格小于正零。如果一个参数为正零，另一个为负零，
     * 结果为正零。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较大的 {@code a} 和 {@code b}。
     */
    public static float max(float a, float b) {
        return Math.max(a, b);
    }

                    /**
     * 返回两个 {@code double} 值中较大的一个。即，
     * 结果是更接近正无穷大的参数。如果
     * 两个参数值相同，结果是相同的值。如果任一值为 NaN，则结果为 NaN。与
     * 数值比较运算符不同，此方法认为负零严格小于正零。如果一个
     * 参数为正零，另一个为负零，结果为正零。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较大的 {@code a} 和 {@code b}。
     */
    public static double max(double a, double b) {
        return Math.max(a, b);
    }

    /**
     * 返回两个 {@code int} 值中较小的一个。即，
     * 结果是更接近 {@link Integer#MIN_VALUE} 的参数。如果
     * 两个参数值相同，结果是相同的值。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较小的 {@code a} 和 {@code b}。
     */
    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    /**
     * 返回两个 {@code long} 值中较小的一个。即，
     * 结果是更接近 {@link Long#MIN_VALUE} 的参数。如果
     * 两个参数值相同，结果是相同的值。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较小的 {@code a} 和 {@code b}。
     */
    public static long min(long a, long b) {
        return Math.min(a, b);
    }

    /**
     * 返回两个 {@code float} 值中较小的一个。即，
     * 结果是更接近负无穷大的值。如果
     * 两个参数值相同，结果是相同的值。如果任一值为 NaN，则结果为 NaN。与
     * 数值比较运算符不同，此方法认为负零严格小于正零。如果
     * 一个参数为正零，另一个为负零，结果为负零。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较小的 {@code a} 和 {@code b.}
     */
    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    /**
     * 返回两个 {@code double} 值中较小的一个。即，
     * 结果是更接近负无穷大的值。如果
     * 两个参数值相同，结果是相同的值。如果任一值为 NaN，则结果为 NaN。与
     * 数值比较运算符不同，此方法认为负零严格小于正零。如果一个
     * 参数为正零，另一个为负零，结果为负零。
     *
     * @param   a   一个参数。
     * @param   b   另一个参数。
     * @return  较小的 {@code a} 和 {@code b}。
     */
    public static double min(double a, double b) {
        return Math.min(a, b);
    }

    /**
     * 返回参数的 ulp 大小。一个 ulp，即最后一个位置的单位，对于一个 {@code double} 值来说，是这个浮点值与下一个更大绝对值的 {@code
     * double} 值之间的正距离。注意对于非 NaN
     * <i>x</i>，<code>ulp(-<i>x</i>) == ulp(<i>x</i>)</code>。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，则结果是 NaN。
     * <li> 如果参数是正无穷大或负无穷大，则结果是正无穷大。
     * <li> 如果参数是正零或负零，则结果是
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
        return Math.ulp(d);
    }

    /**
     * 返回参数的 ulp 大小。一个 ulp，即最后一个位置的单位，对于一个 {@code float} 值来说，是这个浮点值与下一个更大绝对值的 {@code
     * float} 值之间的正距离。注意对于非 NaN
     * <i>x</i>，<code>ulp(-<i>x</i>) == ulp(<i>x</i>)</code>。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，则结果是 NaN。
     * <li> 如果参数是正无穷大或负无穷大，则结果是正无穷大。
     * <li> 如果参数是正零或负零，则结果是
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
        return Math.ulp(f);
    }

    /**
     * 返回参数的符号函数；如果参数为零，则为零，如果参数大于零，则为 1.0，如果参数小于零，则为 -1.0。
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
        return Math.signum(d);
    }

    /**
     * 返回参数的符号函数；如果参数为零，则为零，如果参数大于零，则为 1.0f，如果参数小于零，则为 -1.0f。
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
        return Math.signum(f);
    }

                    /**
     * 返回一个 {@code double} 值的双曲正弦。
     * 双曲正弦的定义为
     * (<i>e<sup>x</sup>&nbsp;-&nbsp;e<sup>-x</sup></i>)/2
     * 其中 <i>e</i> 是 {@linkplain Math#E 欧拉数}。
     *
     * <p>特殊情况：
     * <ul>
     *
     * <li>如果参数是 NaN，则结果是 NaN。
     *
     * <li>如果参数是无穷大，则结果是一个与参数同符号的无穷大。
     *
     * <li>如果参数是零，则结果是一个与参数同符号的零。
     *
     * </ul>
     *
     * @param   x 要返回其双曲正弦的数字。
     * @return  {@code x} 的双曲正弦。
     * @since 1.5
     */
    public static native double sinh(double x);

    /**
     * 返回一个 {@code double} 值的双曲余弦。
     * 双曲余弦的定义为
     * (<i>e<sup>x</sup>&nbsp;+&nbsp;e<sup>-x</sup></i>)/2
     * 其中 <i>e</i> 是 {@linkplain Math#E 欧拉数}。
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
     * @param   x 要返回其双曲余弦的数字。
     * @return  {@code x} 的双曲余弦。
     * @since 1.5
     */
    public static native double cosh(double x);

    /**
     * 返回一个 {@code double} 值的双曲正切。
     * 双曲正切的定义为
     * (<i>e<sup>x</sup>&nbsp;-&nbsp;e<sup>-x</sup></i>)/(<i>e<sup>x</sup>&nbsp;+&nbsp;e<sup>-x</sup></i>)，
     * 换句话说，{@linkplain Math#sinh
     * sinh(<i>x</i>)}/{@linkplain Math#cosh cosh(<i>x</i>)}。 注意
     * 精确的双曲正切的绝对值总是小于
     * 1。
     *
     * <p>特殊情况：
     * <ul>
     *
     * <li>如果参数是 NaN，则结果是 NaN。
     *
     * <li>如果参数是零，则结果是一个与参数同符号的零。
     *
     * <li>如果参数是正无穷大，则结果是
     * {@code +1.0}。
     *
     * <li>如果参数是负无穷大，则结果是
     * {@code -1.0}。
     *
     * </ul>
     *
     * @param   x 要返回其双曲正切的数字。
     * @return  {@code x} 的双曲正切。
     * @since 1.5
     */
    public static native double tanh(double x);

    /**
     * 返回 sqrt(<i>x</i><sup>2</sup>&nbsp;+<i>y</i><sup>2</sup>)
     * 而不发生中间溢出或下溢。
     *
     * <p>特殊情况：
     * <ul>
     *
     * <li> 如果任一参数是无穷大，则结果
     * 是正无穷大。
     *
     * <li> 如果任一参数是 NaN 且没有参数是无穷大，
     * 则结果是 NaN。
     *
     * </ul>
     *
     * @param x 一个值
     * @param y 一个值
     * @return sqrt(<i>x</i><sup>2</sup>&nbsp;+<i>y</i><sup>2</sup>)
     * 而不发生中间溢出或下溢
     * @since 1.5
     */
    public static native double hypot(double x, double y);

    /**
     * 返回 <i>e</i><sup>x</sup>&nbsp;-1。 注意对于接近 0 的
     * <i>x</i> 值，{@code expm1(x)}&nbsp;+&nbsp;1 的精确和比
     * {@code exp(x)} 更接近 <i>e</i><sup>x</sup> 的真实结果。
     *
     * <p>特殊情况：
     * <ul>
     * <li>如果参数是 NaN，则结果是 NaN。
     *
     * <li>如果参数是正无穷大，则结果是
     * 正无穷大。
     *
     * <li>如果参数是负无穷大，则结果是
     * -1.0。
     *
     * <li>如果参数是零，则结果是一个与参数同符号的零。
     *
     * </ul>
     *
     * @param   x   计算 <i>e</i><sup>{@code x}</sup>&nbsp;-1 时的指数。
     * @return  <i>e</i><sup>{@code x}</sup>&nbsp;-&nbsp;1 的值。
     * @since 1.5
     */
    public static native double expm1(double x);

    /**
     * 返回参数加 1 的自然对数。
     * 注意对于小值 {@code x}，{@code log1p(x)} 的结果比
     * {@code log(1.0+x)} 的浮点计算更接近 ln(1
     * + {@code x}) 的真实结果。
     *
     * <p>特殊情况：
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
     * <li>如果参数是零，则结果是一个与参数同符号的零。
     *
     * </ul>
     *
     * @param   x   一个值
     * @return ln({@code x}&nbsp;+&nbsp;1)，即 {@code x}&nbsp;+&nbsp;1 的自然对数
     * @since 1.5
     */
    public static native double log1p(double x);

    /**
     * 返回第一个浮点数参数的值，但带有第二个浮点数参数的符号。 对于此方法，NaN
     * {@code sign} 参数始终被视为正数。
     *
     * @param magnitude  提供结果大小的参数
     * @param sign   提供结果符号的参数
     * @return 一个具有 {@code magnitude} 的大小
     * 和 {@code sign} 的符号的值。
     * @since 1.6
     */
    public static double copySign(double magnitude, double sign) {
        return Math.copySign(magnitude, (Double.isNaN(sign)?1.0d:sign));
    }

    /**
     * 返回第一个浮点数参数的值，但带有第二个浮点数参数的符号。 对于此方法，NaN
     * {@code sign} 参数始终被视为正数。
     *
     * @param magnitude  提供结果大小的参数
     * @param sign   提供结果符号的参数
     * @return 一个具有 {@code magnitude} 的大小
     * 和 {@code sign} 的符号的值。
     * @since 1.6
     */
    public static float copySign(float magnitude, float sign) {
        return Math.copySign(magnitude, (Float.isNaN(sign)?1.0f:sign));
    }
    /**
     * 返回表示 {@code float} 的无偏指数。 特殊情况：
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
        return Math.getExponent(f);
    }

                    /**
     * 返回用于表示 {@code double} 的无偏指数。特殊情况：
     *
     * <ul>
     * <li>如果参数是 NaN 或无限，则结果是
     * {@link Double#MAX_EXPONENT} + 1。
     * <li>如果参数是零或次正规数，则结果是
     * {@link Double#MIN_EXPONENT} -1。
     * </ul>
     * @param d 一个 {@code double} 值
     * @return 参数的无偏指数
     * @since 1.6
     */
    public static int getExponent(double d) {
        return Math.getExponent(d);
    }

    /**
     * 返回第一个参数在朝向第二个参数方向上的相邻浮点数。如果两个参数比较相等，则返回第二个参数。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果任一参数是 NaN，则返回 NaN。
     *
     * <li> 如果两个参数都是带符号的零，则返回 {@code direction} 不变（如要求所暗示的，如果参数比较相等，则返回第二个参数）。
     *
     * <li> 如果 {@code start} 是
     * &plusmn;{@link Double#MIN_VALUE} 并且 {@code direction}
     * 的值使得结果应该具有更小的幅度，则返回与 {@code start} 符号相同的零。
     *
     * <li> 如果 {@code start} 是无限大并且
     * {@code direction} 的值使得结果应该具有更小的幅度，则返回与 {@code start} 符号相同的 {@link Double#MAX_VALUE}。
     *
     * <li> 如果 {@code start} 等于 &plusmn;
     * {@link Double#MAX_VALUE} 并且 {@code direction} 的值使得结果应该具有更大的幅度，则返回与 {@code start} 符号相同的无限大。
     * </ul>
     *
     * @param start 起始浮点值
     * @param direction 表示应返回 {@code start} 的邻居之一或 {@code start} 的值
     * @return 朝向 {@code direction} 的与 {@code start} 相邻的浮点数。
     * @since 1.6
     */
    public static double nextAfter(double start, double direction) {
        return Math.nextAfter(start, direction);
    }

    /**
     * 返回第一个参数在朝向第二个参数方向上的相邻浮点数。如果两个参数比较相等，则返回与第二个参数等效的值。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果任一参数是 NaN，则返回 NaN。
     *
     * <li> 如果两个参数都是带符号的零，则返回与 {@code direction} 等效的值。
     *
     * <li> 如果 {@code start} 是
     * &plusmn;{@link Float#MIN_VALUE} 并且 {@code direction}
     * 的值使得结果应该具有更小的幅度，则返回与 {@code start} 符号相同的零。
     *
     * <li> 如果 {@code start} 是无限大并且
     * {@code direction} 的值使得结果应该具有更小的幅度，则返回与 {@code start} 符号相同的 {@link Float#MAX_VALUE}。
     *
     * <li> 如果 {@code start} 等于 &plusmn;
     * {@link Float#MAX_VALUE} 并且 {@code direction} 的值使得结果应该具有更大的幅度，则返回与 {@code start} 符号相同的无限大。
     * </ul>
     *
     * @param start 起始浮点值
     * @param direction 表示应返回 {@code start} 的邻居之一或 {@code start} 的值
     * @return 朝向 {@code direction} 的与 {@code start} 相邻的浮点数。
     * @since 1.6
     */
    public static float nextAfter(float start, double direction) {
        return Math.nextAfter(start, direction);
    }

    /**
     * 返回朝向正无穷方向上的与 {@code d} 相邻的浮点值。此方法在语义上等同于 {@code nextAfter(d,
     * Double.POSITIVE_INFINITY)}；但是，{@code nextUp}
     * 实现可能比其等效的 {@code nextAfter} 调用运行得更快。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，则结果是 NaN。
     *
     * <li> 如果参数是正无穷大，则结果是
     * 正无穷大。
     *
     * <li> 如果参数是零，则结果是
     * {@link Double#MIN_VALUE}
     *
     * </ul>
     *
     * @param d 起始浮点值
     * @return 更接近正无穷的相邻浮点值。
     * @since 1.6
     */
    public static double nextUp(double d) {
        return Math.nextUp(d);
    }

    /**
     * 返回朝向正无穷方向上的与 {@code f} 相邻的浮点值。此方法在语义上等同于 {@code nextAfter(f,
     * Float.POSITIVE_INFINITY)}；但是，{@code nextUp}
     * 实现可能比其等效的 {@code nextAfter} 调用运行得更快。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，则结果是 NaN。
     *
     * <li> 如果参数是正无穷大，则结果是
     * 正无穷大。
     *
     * <li> 如果参数是零，则结果是
     * {@link Float#MIN_VALUE}
     *
     * </ul>
     *
     * @param f 起始浮点值
     * @return 更接近正无穷的相邻浮点值。
     * @since 1.6
     */
    public static float nextUp(float f) {
        return Math.nextUp(f);
    }

    /**
     * 返回朝向负无穷方向上的与 {@code d} 相邻的浮点值。此方法在语义上等同于 {@code nextAfter(d,
     * Double.NEGATIVE_INFINITY)}；但是，{@code nextDown}
     * 实现可能比其等效的 {@code nextAfter} 调用运行得更快。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，则结果是 NaN。
     *
     * <li> 如果参数是负无穷大，则结果是
     * 负无穷大。
     *
     * <li> 如果参数是零，则结果是
     * {@code -Double.MIN_VALUE}
     *
     * </ul>
     *
     * @param d 起始浮点值
     * @return 更接近负无穷的相邻浮点值。
     * @since 1.8
     */
    public static double nextDown(double d) {
        return Math.nextDown(d);
    }

                    /**
     * 返回接近 {@code f} 且方向为负无穷大的浮点值。此方法在语义上等同于 {@code nextAfter(f,
     * Float.NEGATIVE_INFINITY)}；然而，{@code nextDown} 的实现可能比其等效的 {@code nextAfter} 调用运行得更快。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果参数是 NaN，结果是 NaN。
     *
     * <li> 如果参数是负无穷大，结果是负无穷大。
     *
     * <li> 如果参数是零，结果是
     * {@code -Float.MIN_VALUE}
     *
     * </ul>
     *
     * @param f  起始浮点值
     * @return 更接近负无穷大的相邻浮点值。
     * @since 1.8
     */
    public static float nextDown(float f) {
        return Math.nextDown(f);
    }

    /**
     * 返回 {@code d} &times;
     * 2<sup>{@code scaleFactor}</sup>，结果四舍五入，如同通过单次正确四舍五入的浮点乘法计算为 double 值集的成员。请参阅 Java
     * 语言规范，了解浮点值集的讨论。如果结果的指数在 {@link
     * Double#MIN_EXPONENT} 和 {@link Double#MAX_EXPONENT} 之间，结果会被精确计算。如果结果的指数
     * 大于 {@code Double.MAX_EXPONENT}，则返回无穷大。请注意，如果结果是次正规数，
     * 精度可能会丢失；也就是说，当 {@code scalb(x, n)}
     * 是次正规数时，{@code scalb(scalb(x, n), -n)} 可能不等于
     * <i>x</i>。当结果非 NaN 时，结果的符号与 {@code d} 相同。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果第一个参数是 NaN，返回 NaN。
     * <li> 如果第一个参数是无穷大，则返回相同符号的无穷大。
     * <li> 如果第一个参数是零，则返回相同符号的零。
     * </ul>
     *
     * @param d 要按 2 的幂缩放的数字。
     * @param scaleFactor 用于缩放 {@code d} 的 2 的幂
     * @return {@code d} &times; 2<sup>{@code scaleFactor}</sup>
     * @since 1.6
     */
    public static double scalb(double d, int scaleFactor) {
        return Math.scalb(d, scaleFactor);
    }

    /**
     * 返回 {@code f} &times;
     * 2<sup>{@code scaleFactor}</sup>，结果四舍五入，如同通过单次正确四舍五入的浮点乘法计算为 float 值集的成员。请参阅 Java
     * 语言规范，了解浮点值集的讨论。如果结果的指数在 {@link
     * Float#MIN_EXPONENT} 和 {@link Float#MAX_EXPONENT} 之间，结果会被精确计算。如果结果的指数
     * 大于 {@code Float.MAX_EXPONENT}，则返回无穷大。请注意，如果结果是次正规数，
     * 精度可能会丢失；也就是说，当 {@code scalb(x, n)}
     * 是次正规数时，{@code scalb(scalb(x, n), -n)} 可能不等于
     * <i>x</i>。当结果非 NaN 时，结果的符号与 {@code f} 相同。
     *
     * <p>特殊情况：
     * <ul>
     * <li> 如果第一个参数是 NaN，返回 NaN。
     * <li> 如果第一个参数是无穷大，则返回相同符号的无穷大。
     * <li> 如果第一个参数是零，则返回相同符号的零。
     * </ul>
     *
     * @param f 要按 2 的幂缩放的数字。
     * @param scaleFactor 用于缩放 {@code f} 的 2 的幂
     * @return {@code f} &times; 2<sup>{@code scaleFactor}</sup>
     * @since 1.6
     */
    public static float scalb(float f, int scaleFactor) {
        return Math.scalb(f, scaleFactor);
    }
}
