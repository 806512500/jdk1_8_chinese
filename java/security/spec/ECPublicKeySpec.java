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

/**
 * 这个不可变类指定了一个椭圆曲线公钥及其相关参数。
 *
 * @see KeySpec
 * @see ECPoint
 * @see ECParameterSpec
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public class ECPublicKeySpec implements KeySpec {

    private ECPoint w;
    private ECParameterSpec params;

    /**
     * 使用指定的参数值创建一个新的 ECPublicKeySpec。
     * @param w 公共点。
     * @param params 关联的椭圆曲线域参数。
     * @exception NullPointerException 如果 {@code w}
     * 或 {@code params} 为 null。
     * @exception IllegalArgumentException 如果 {@code w}
     * 是无穷远点，即 ECPoint.POINT_INFINITY
     */
    public ECPublicKeySpec(ECPoint w, ECParameterSpec params) {
        if (w == null) {
            throw new NullPointerException("w is null");
        }
        if (params == null) {
            throw new NullPointerException("params is null");
        }
        if (w == ECPoint.POINT_INFINITY) {
            throw new IllegalArgumentException("w is ECPoint.POINT_INFINITY");
        }
        this.w = w;
        this.params = params;
    }

    /**
     * 返回公共点 W。
     * @return 公共点 W。
     */
    public ECPoint getW() {
        return w;
    }

    /**
     * 返回关联的椭圆曲线域参数。
     * @return 椭圆曲线域参数。
     */
    public ECParameterSpec getParams() {
        return params;
    }
}
