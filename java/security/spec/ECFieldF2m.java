
/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其附属公司。保留所有权利。
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
package java.security.spec;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * 此不可变类定义了一个椭圆曲线 (EC) 特征 2 有限域。
 *
 * @see ECField
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public class ECFieldF2m implements ECField {

    private int m;
    private int[] ks;
    private BigInteger rp;

    /**
     * 创建一个具有 2^{@code m} 个元素的椭圆曲线特征 2 有限域，使用正规基。
     * @param m 2^{@code m} 为元素的数量。
     * @exception IllegalArgumentException 如果 {@code m} 不是正数。
     */
    public ECFieldF2m(int m) {
        if (m <= 0) {
            throw new IllegalArgumentException("m is not positive");
        }
        this.m = m;
        this.ks = null;
        this.rp = null;
    }

    /**
     * 创建一个具有 2^{@code m} 个元素的椭圆曲线特征 2 有限域，使用多项式基。
     * 该域的约简多项式基于 {@code rp}，其第 i 位对应于约简多项式的第 i 个系数。<p>
     * 注意：有效的约简多项式要么是三项式 (X^{@code m} + X^{@code k} + 1
     * 其中 {@code m} &gt; {@code k} &gt;= 1)，要么是五项式 (X^{@code m} + X^{@code k3}
     * + X^{@code k2} + X^{@code k1} + 1 其中
     * {@code m} &gt; {@code k3} &gt; {@code k2}
     * &gt; {@code k1} &gt;= 1)。
     * @param m 2^{@code m} 为元素的数量。
     * @param rp BigInteger 的第 i 位对应于约简多项式的第 i 个系数。
     * @exception NullPointerException 如果 {@code rp} 为 null。
     * @exception IllegalArgumentException 如果 {@code m} 不是正数，或者 {@code rp} 不表示
     * 有效的约简多项式。
     */
    public ECFieldF2m(int m, BigInteger rp) {
        // 检查 m 和 rp
        this.m = m;
        this.rp = rp;
        if (m <= 0) {
            throw new IllegalArgumentException("m is not positive");
        }
        int bitCount = this.rp.bitCount();
        if (!this.rp.testBit(0) || !this.rp.testBit(m) ||
            ((bitCount != 3) && (bitCount != 5))) {
            throw new IllegalArgumentException
                ("rp does not represent a valid reduction polynomial");
        }
        // 将 rp 转换为 ks
        BigInteger temp = this.rp.clearBit(0).clearBit(m);
        this.ks = new int[bitCount-2];
        for (int i = this.ks.length-1; i >= 0; i--) {
            int index = temp.getLowestSetBit();
            this.ks[i] = index;
            temp = temp.clearBit(index);
        }
    }

    /**
     * 创建一个具有 2^{@code m} 个元素的椭圆曲线特征 2 有限域，使用多项式基。该域的约简多项式基于 {@code ks}，其内容
     * 包含约简多项式的中间项的顺序。
     * 注意：有效的约简多项式要么是三项式 (X^{@code m} + X^{@code k} + 1
     * 其中 {@code m} &gt; {@code k} &gt;= 1)，要么是五项式 (X^{@code m} + X^{@code k3}
     * + X^{@code k2} + X^{@code k1} + 1 其中
     * {@code m} &gt; {@code k3} &gt; {@code k2}
     * &gt; {@code k1} &gt;= 1)，因此 {@code ks} 应该长度为 1 或 3。
     * @param m 2^{@code m} 为元素的数量。
     * @param ks 约简多项式的中间项的顺序。此数组的内容将被复制以防止后续修改。
     * @exception NullPointerException 如果 {@code ks} 为 null。
     * @exception IllegalArgumentException 如果 {@code m} 不是正数，或者 {@code ks} 的长度
     * 不是 1 或 3，或者 {@code ks} 中的值不在 {@code m}-1 和 1（包括）之间且按降序排列。
     */
    public ECFieldF2m(int m, int[] ks) {
        // 检查 m 和 ks
        this.m = m;
        this.ks = ks.clone();
        if (m <= 0) {
            throw new IllegalArgumentException("m is not positive");
        }
        if ((this.ks.length != 1) && (this.ks.length != 3)) {
            throw new IllegalArgumentException
                ("length of ks is neither 1 nor 3");
        }
        for (int i = 0; i < this.ks.length; i++) {
            if ((this.ks[i] < 1) || (this.ks[i] > m-1)) {
                throw new IllegalArgumentException
                    ("ks["+ i + "] is out of range");
            }
            if ((i != 0) && (this.ks[i] >= this.ks[i-1])) {
                throw new IllegalArgumentException
                    ("values in ks are not in descending order");
            }
        }
        // 将 ks 转换为 rp
        this.rp = BigInteger.ONE;
        this.rp = rp.setBit(m);
        for (int j = 0; j < this.ks.length; j++) {
            rp = rp.setBit(this.ks[j]);
        }
    }

    /**
     * 返回该特征 2 有限域的位数，即 {@code m}。
     * @return 位数。
     */
    public int getFieldSize() {
        return m;
    }

    /**
     * 返回此特征 2 有限域的值 {@code m}。
     * @return {@code m}，2^{@code m} 为元素的数量。
     */
    public int getM() {
        return m;
    }

    /**
     * 返回一个 BigInteger，其第 i 位对应于多项式基的约简多项式的第 i 个系数，或对于正规基返回 null。
     * @return 一个 BigInteger，其第 i 位对应于多项式基的约简多项式的第 i 个系数，或对于正规基返回 null。
     */
    public BigInteger getReductionPolynomial() {
        return rp;
    }

    /**
     * 返回一个整数数组，包含多项式基的约简多项式的中间项的顺序，或对于正规基返回 null。
     * @return 一个整数数组，包含多项式基的约简多项式的中间项的顺序，或对于正规基返回 null。每次调用此方法时都会返回一个新的数组。
     */
    public int[] getMidTermsOfReductionPolynomial() {
        if (ks == null) {
            return null;
        } else {
            return ks.clone();
        }
    }

                /**
     * 将此有限域与指定的对象进行等同性比较。
     * @param obj 要比较的对象。
     * @return 如果 {@code obj} 是 ECFieldF2m 的实例，并且 {@code m} 和约简
     * 多项式匹配，则返回 true，否则返回 false。
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof ECFieldF2m) {
            // 无需在此处比较 rp，因为 ks 和 rp
            // 应该是等效的
            return ((m == ((ECFieldF2m)obj).m) &&
                    (Arrays.equals(ks, ((ECFieldF2m) obj).ks)));
        }
        return false;
    }

    /**
     * 返回此特征为 2 的有限域的哈希码值。
     * @return 哈希码值。
     */
    public int hashCode() {
        int value = m << 5;
        value += (rp==null? 0:rp.hashCode());
        // 无需在此处涉及 ks，因为 ks 和 rp
        // 应该是等效的。
        return value;
    }
}
