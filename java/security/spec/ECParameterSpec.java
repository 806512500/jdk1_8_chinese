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

/**
 * 此不可变类指定了用于椭圆曲线密码学 (ECC) 的域参数集。
 *
 * @see AlgorithmParameterSpec
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public class ECParameterSpec implements AlgorithmParameterSpec {

    private final EllipticCurve curve;
    private final ECPoint g;
    private final BigInteger n;
    private final int h;

    /**
     * 基于指定的值创建椭圆曲线域参数。
     * @param curve 该参数定义的椭圆曲线。
     * @param g 生成器，也称为基点。
     * @param n 生成器 {@code g} 的阶。
     * @param h 共因子。
     * @exception NullPointerException 如果 {@code curve}、
     * {@code g} 或 {@code n} 为 null。
     * @exception IllegalArgumentException 如果 {@code n}
     * 或 {@code h} 不为正数。
     */
    public ECParameterSpec(EllipticCurve curve, ECPoint g,
                           BigInteger n, int h) {
        if (curve == null) {
            throw new NullPointerException("curve is null");
        }
        if (g == null) {
            throw new NullPointerException("g is null");
        }
        if (n == null) {
            throw new NullPointerException("n is null");
        }
        if (n.signum() != 1) {
            throw new IllegalArgumentException("n is not positive");
        }
        if (h <= 0) {
            throw new IllegalArgumentException("h is not positive");
        }
        this.curve = curve;
        this.g = g;
        this.n = n;
        this.h = h;
    }

    /**
     * 返回此参数定义的椭圆曲线。
     * @return 此参数定义的椭圆曲线。
     */
    public EllipticCurve getCurve() {
        return curve;
    }

    /**
     * 返回生成器，也称为基点。
     * @return 生成器，也称为基点。
     */
    public ECPoint getGenerator() {
        return g;
    }

    /**
     * 返回生成器的阶。
     * @return 生成器的阶。
     */
    public BigInteger getOrder() {
        return n;
    }

    /**
     * 返回共因子。
     * @return 共因子。
     */
    public int getCofactor() {
        return h;
    }
}
