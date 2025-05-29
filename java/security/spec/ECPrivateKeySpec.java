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
 * 该不可变类指定了一个椭圆曲线私钥及其关联参数。
 *
 * @see KeySpec
 * @see ECParameterSpec
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public class ECPrivateKeySpec implements KeySpec {

    private BigInteger s;
    private ECParameterSpec params;

    /**
     * 使用指定的参数值创建一个新的 ECPrivateKeySpec。
     * @param s 私有值。
     * @param params 关联的椭圆曲线域参数。
     * @exception NullPointerException 如果 {@code s}
     * 或 {@code params} 为 null。
     */
    public ECPrivateKeySpec(BigInteger s, ECParameterSpec params) {
        if (s == null) {
            throw new NullPointerException("s is null");
        }
        if (params == null) {
            throw new NullPointerException("params is null");
        }
        this.s = s;
        this.params = params;
    }

    /**
     * 返回私有值 S。
     * @return 私有值 S。
     */
    public BigInteger getS() {
        return s;
    }

    /**
     * 返回关联的椭圆曲线域参数。
     * @return 椭圆曲线域参数。
     */
    public ECParameterSpec getParams() {
        return params;
    }
}
