/*
 * 版权所有 (c) 1999, 2007, Oracle 和/或其附属公司。保留所有权利。
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

package java.math;

/**
 * 一个用于表示多精度整数的类，通过允许数字仅占用数组的一部分来高效利用分配的空间，
 * 从而减少数组的重新分配次数。在执行具有许多迭代的操作时，用于保存数字的数组仅在必要时增加，
 * 并且不必与它所表示的数字大小相同。可变数字允许在同一个数字上进行计算，而无需为计算的每一步创建新数字，
 * 如使用 BigInteger 时的情况。
 *
 * 注意，SignedMutableBigIntegers 仅支持带符号的加法和减法。所有其他操作都与 MutableBigIntegers 相同。
 *
 * @see     BigInteger
 * @author  Michael McCloskey
 * @since   1.3
 */

class SignedMutableBigInteger extends MutableBigInteger {

   /**
     * 此 MutableBigInteger 的符号。
     */
    int sign = 1;

    // 构造函数

    /**
     * 默认构造函数。创建一个具有一个字长容量的空 MutableBigInteger。
     */
    SignedMutableBigInteger() {
        super();
    }

    /**
     * 使用由 int val 指定的大小构造一个新的 MutableBigInteger。
     */
    SignedMutableBigInteger(int val) {
        super(val);
    }

    /**
     * 使用与指定的 MutableBigInteger 大小相等的大小构造一个新的 MutableBigInteger。
     */
    SignedMutableBigInteger(MutableBigInteger val) {
        super(val);
    }

   // 算术运算

   /**
     * 基于无符号加法和减法的带符号加法。
     */
    void signedAdd(SignedMutableBigInteger addend) {
        if (sign == addend.sign)
            add(addend);
        else
            sign = sign * subtract(addend);

    }

   /**
     * 基于无符号加法和减法的带符号加法。
     */
    void signedAdd(MutableBigInteger addend) {
        if (sign == 1)
            add(addend);
        else
            sign = sign * subtract(addend);

    }

   /**
     * 基于无符号加法和减法的带符号减法。
     */
    void signedSubtract(SignedMutableBigInteger addend) {
        if (sign == addend.sign)
            sign = sign * subtract(addend);
        else
            add(addend);

    }

   /**
     * 基于无符号加法和减法的带符号减法。
     */
    void signedSubtract(MutableBigInteger addend) {
        if (sign == 1)
            sign = sign * subtract(addend);
        else
            add(addend);
        if (intLen == 0)
             sign = 1;
    }

    /**
     * 从偏移量开始打印此 MutableBigInteger 值数组的前 intLen 个整数。
     */
    public String toString() {
        return this.toBigInteger(sign).toString();
    }

}
