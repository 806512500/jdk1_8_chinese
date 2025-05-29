/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其子公司。保留所有权利。
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
 * 这个不可变类持有表示椭圆曲线所需的所有值。
 *
 * @see ECField
 * @see ECFieldFp
 * @see ECFieldF2m
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public class EllipticCurve {

    private final ECField field;
    private final BigInteger a;
    private final BigInteger b;
    private final byte[] seed;

    // 检查系数 c 是否是 ECField field 中的有效元素。
    private static void checkValidity(ECField field, BigInteger c,
        String cName) {
        // 只有当 field 是 ECFieldFp 或 ECFieldF2m 时才能执行检查。
        if (field instanceof ECFieldFp) {
            BigInteger p = ((ECFieldFp)field).getP();
            if (p.compareTo(c) != 1) {
                throw new IllegalArgumentException(cName + " 太大");
            } else if (c.signum() < 0) {
                throw new IllegalArgumentException(cName + " 是负数");
            }
        } else if (field instanceof ECFieldF2m) {
            int m = ((ECFieldF2m)field).getM();
            if (c.bitLength() > m) {
                throw new IllegalArgumentException(cName + " 太大");
            }
        }
    }

    /**
     * 使用指定的椭圆域 {@code field} 和系数 {@code a} 和
     * {@code b} 创建椭圆曲线。
     * @param field 这条椭圆曲线所在的有限域。
     * @param a 这条椭圆曲线的第一个系数。
     * @param b 这条椭圆曲线的第二个系数。
     * @exception NullPointerException 如果 {@code field}，
     * {@code a}，或 {@code b} 为 null。
     * @exception IllegalArgumentException 如果 {@code a}
     * 或 {@code b} 不为 null 且不在 {@code field} 中。
     */
    public EllipticCurve(ECField field, BigInteger a,
                         BigInteger b) {
        this(field, a, b, null);
    }

    /**
     * 使用指定的椭圆域 {@code field}，系数 {@code a} 和
     * {@code b}，以及用于曲线生成的 {@code seed} 创建椭圆曲线。
     * @param field 这条椭圆曲线所在的有限域。
     * @param a 这条椭圆曲线的第一个系数。
     * @param b 这条椭圆曲线的第二个系数。
     * @param seed 用于曲线生成的字节，以便后续验证。此数组的内容将被复制以防止后续修改。
     * @exception NullPointerException 如果 {@code field}，
     * {@code a}，或 {@code b} 为 null。
     * @exception IllegalArgumentException 如果 {@code a}
     * 或 {@code b} 不为 null 且不在 {@code field} 中。
     */
    public EllipticCurve(ECField field, BigInteger a,
                         BigInteger b, byte[] seed) {
        if (field == null) {
            throw new NullPointerException("field 为 null");
        }
        if (a == null) {
            throw new NullPointerException("第一个系数为 null");
        }
        if (b == null) {
            throw new NullPointerException("第二个系数为 null");
        }
        checkValidity(field, a, "第一个系数");
        checkValidity(field, b, "第二个系数");
        this.field = field;
        this.a = a;
        this.b = b;
        if (seed != null) {
            this.seed = seed.clone();
        } else {
            this.seed = null;
        }
    }

    /**
     * 返回这条椭圆曲线所在的有限域 {@code field}。
     * @return 这条曲线所在的域 {@code field}。
     */
    public ECField getField() {
        return field;
    }

    /**
     * 返回椭圆曲线的第一个系数 {@code a}。
     * @return 第一个系数 {@code a}。
     */
    public BigInteger getA() {
        return a;
    }

    /**
     * 返回椭圆曲线的第二个系数 {@code b}。
     * @return 第二个系数 {@code b}。
     */
    public BigInteger getB() {
        return b;
    }

    /**
     * 返回用于曲线生成的种子字节 {@code seed}。如果未指定则为 null。
     * @return 种子字节 {@code seed}。每次调用此方法时都会返回一个新的数组。
     */
    public byte[] getSeed() {
        if (seed == null) return null;
        else return seed.clone();
    }

    /**
     * 比较此椭圆曲线与指定对象是否相等。
     * @param obj 要比较的对象。
     * @return 如果 {@code obj} 是 EllipticCurve 的实例且域、A 和 B 匹配，则返回 true，否则返回 false。
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof EllipticCurve) {
            EllipticCurve curve = (EllipticCurve) obj;
            if ((field.equals(curve.field)) &&
                (a.equals(curve.a)) &&
                (b.equals(curve.b))) {
                    return true;
            }
        }
        return false;
    }

    /**
     * 返回此椭圆曲线的哈希码值。
     * @return 从域、A 和 B 的哈希码计算出的哈希码值，如下所示：
     * <pre>{@code
     *     (field.hashCode() << 6) + (a.hashCode() << 4) + (b.hashCode() << 2)
     * }</pre>
     */
    public int hashCode() {
        return (field.hashCode() << 6 +
            (a.hashCode() << 4) +
            (b.hashCode() << 2));
    }
}
