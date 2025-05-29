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
 * 此不可变类定义了一个椭圆曲线（EC）素数有限域。
 *
 * @see ECField
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public class ECFieldFp implements ECField {

    private BigInteger p;

    /**
     * 创建一个具有指定素数 {@code p} 的椭圆曲线素数有限域。
     * @param p 素数。
     * @exception NullPointerException 如果 {@code p} 为 null。
     * @exception IllegalArgumentException 如果 {@code p}
     * 不是正数。
     */
    public ECFieldFp(BigInteger p) {
        if (p.signum() != 1) {
            throw new IllegalArgumentException("p is not positive");
        }
        this.p = p;
    }

    /**
     * 返回此素数有限域的位大小，即素数 p 的大小。
     * @return 位大小。
     */
    public int getFieldSize() {
        return p.bitLength();
    };

    /**
     * 返回此素数有限域的素数 {@code p}。
     * @return 素数。
     */
    public BigInteger getP() {
        return p;
    }

    /**
     * 比较此素数有限域与指定对象是否相等。
     * @param obj 要比较的对象。
     * @return 如果 {@code obj} 是 ECFieldFp 的实例且素数值匹配，则返回 true，否则返回 false。
     */
    public boolean equals(Object obj) {
        if (this == obj)  return true;
        if (obj instanceof ECFieldFp) {
            return (p.equals(((ECFieldFp)obj).p));
        }
        return false;
    }

    /**
     * 返回此素数有限域的哈希码值。
     * @return 哈希码值。
     */
    public int hashCode() {
        return p.hashCode();
    }
}
