
/*
 * 版权所有 (c) 2003, 2013, Oracle 及/或其附属公司。保留所有权利。
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

/*
 * 部分版权归 IBM Corporation，2001。保留所有权利。
 */
package java.math;

/**
 * 指定一个<i>舍入行为</i>，用于能够丢弃精度的数值操作。每种舍入模式指示如何
 * 计算舍入结果的最不重要的返回数字。如果返回的数字少于表示精确数值结果所需的数字，
 * 被丢弃的数字将被称为<i>被丢弃的小数部分</i>，无论这些数字对数值的贡献如何。换句话说，
 * 作为数值，被丢弃的小数部分的绝对值可能大于一。
 *
 * <p>每种舍入模式的描述都包括一个表格，列出了在给定舍入模式下，不同的两位十进制值将如何
 * 舍入到一位十进制值。可以通过创建一个具有指定值的{@code BigDecimal}数字，创建一个具有适当设置
 * 的{@link MathContext}对象（{@code precision}设置为{@code 1}，并且
 * {@code roundingMode}设置为所讨论的舍入模式），并调用此数字上的{@link BigDecimal#round round}方法，
 * 传入适当的{@code MathContext}，来获得表格中的结果列。以下是一个总结表，显示了所有舍入模式下
 * 这些舍入操作的结果。
 *
 *<table border>
 * <caption><b>不同舍入模式下舍入操作的总结</b></caption>
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
 * <p>此 {@code enum} 旨在替换 {@link BigDecimal} 中基于整数的舍入模式常量枚举
 * ({@link BigDecimal#ROUND_UP}, {@link BigDecimal#ROUND_DOWN} 等)。
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
         * 舍入模式，向远离零的方向舍入。始终增加非零被丢弃小数部分之前的数字。请注意，此
         * 舍入模式从不减少计算值的大小。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 UP 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>使用 {@code UP} 舍入将输入舍入到一位数字
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
         * 舍入模式，向零的方向舍入。从不增加被丢弃小数部分之前的数字（即，截断）。请注意，此
         * 舍入模式从不增加计算值的大小。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 DOWN 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>使用 {@code DOWN} 舍入将输入舍入到一位数字
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
         * 向正无穷方向舍入的舍入模式。如果结果为正，则行为与 {@code RoundingMode.UP} 相同；
         * 如果结果为负，则行为与 {@code RoundingMode.DOWN} 相同。请注意，这种舍入模式从不减少计算值。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 CEILING 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>输入舍入到一位数字<br> 使用 {@code CEILING} 舍入
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
         * 向负无穷方向舍入的舍入模式。如果结果为正，则行为与 {@code RoundingMode.DOWN} 相同；
         * 如果结果为负，则行为与 {@code RoundingMode.UP} 相同。请注意，这种舍入模式从不增加计算值。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 FLOOR 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>输入舍入到一位数字<br> 使用 {@code FLOOR} 舍入
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
         * 向“最近邻居”舍入的舍入模式，除非两个邻居等距，此时向上舍入。
         * 如果舍弃的分数 ≥ 0.5，则行为与 {@code RoundingMode.UP} 相同；否则，行为与 {@code RoundingMode.DOWN} 相同。
         * 请注意，这是学校里通常教授的舍入模式。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 HALF_UP 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>输入舍入到一位数字<br> 使用 {@code HALF_UP} 舍入
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
         * 向“最近邻居”舍入的舍入模式，除非两个邻居等距，此时向下舍入。
         * 如果舍弃的分数 > 0.5，则行为与 {@code RoundingMode.UP} 相同；否则，行为与 {@code RoundingMode.DOWN} 相同。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 HALF_DOWN 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>输入舍入到一位数字<br> 使用 {@code HALF_DOWN} 舍入
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
         * 向“最近邻居”舍入的舍入模式，除非两个邻居等距，此时向偶数邻居舍入。
         * 如果舍弃的分数左边的数字是奇数，则行为与 {@code RoundingMode.HALF_UP} 相同；
         * 如果是偶数，则行为与 {@code RoundingMode.HALF_DOWN} 相同。请注意，这种舍入模式在统计上可以最小化在一系列计算中反复应用时的累积误差。
         * 它有时被称为“银行家舍入”，主要在美国使用。这种舍入模式类似于 Java 中 {@code float} 和 {@code double} 算术使用的舍入策略。
         *
         *<p>示例：
         *<table border>
         * <caption><b>舍入模式 HALF_EVEN 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>输入舍入到一位数字<br> 使用 {@code HALF_EVEN} 舍入
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
         * 四舍五入模式，用于断言请求的操作具有精确的结果，因此不需要四舍五入。如果在此模式下指定的操作结果不精确，
         * 将抛出 {@code ArithmeticException}。
         *<p>示例：
         *<table border>
         * <caption><b>四舍五入模式 UNNECESSARY 示例</b></caption>
         *<tr valign=top><th>输入数字</th>
         *    <th>使用 {@code UNNECESSARY} 四舍五入模式将输入四舍五入到一位小数
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
     * @param oldMode 与此模式对应的 {@code BigDecimal} 常量
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
