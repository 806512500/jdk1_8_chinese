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
 * 这个不可变类表示椭圆曲线（EC）上的一个点，使用仿射坐标。其他坐标系可以
 * 扩展此类以表示其他坐标系中的点。
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public class ECPoint {

    private final BigInteger x;
    private final BigInteger y;

    /**
     * 定义无穷远点。
     */
    public static final ECPoint POINT_INFINITY = new ECPoint();

    // 私有构造函数，用于构造无穷远点
    private ECPoint() {
        this.x = null;
        this.y = null;
    }

    /**
     * 从指定的仿射 x 坐标 {@code x} 和仿射 y 坐标 {@code y} 创建一个 ECPoint。
     * @param x 仿射 x 坐标。
     * @param y 仿射 y 坐标。
     * @exception NullPointerException 如果 {@code x} 或
     * {@code y} 为 null。
     */
    public ECPoint(BigInteger x, BigInteger y) {
        if ((x==null) || (y==null)) {
            throw new NullPointerException("仿射坐标 x 或 y 为 null");
        }
        this.x = x;
        this.y = y;
    }

    /**
     * 返回仿射 x 坐标 {@code x}。
     * 注意：POINT_INFINITY 有一个 null 仿射 x 坐标。
     * @return 仿射 x 坐标。
     */
    public BigInteger getAffineX() {
        return x;
    }

    /**
     * 返回仿射 y 坐标 {@code y}。
     * 注意：POINT_INFINITY 有一个 null 仿射 y 坐标。
     * @return 仿射 y 坐标。
     */
    public BigInteger getAffineY() {
        return y;
    }

    /**
     * 比较此椭圆曲线点与指定对象是否相等。
     * @param obj 要比较的对象。
     * @return 如果 {@code obj} 是 ECPoint 的实例且仿射坐标匹配，则返回 true，否则返回 false。
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (this == POINT_INFINITY) return false;
        if (obj instanceof ECPoint) {
            return ((x.equals(((ECPoint)obj).x)) &&
                    (y.equals(((ECPoint)obj).y)));
        }
        return false;
    }

    /**
     * 返回此椭圆曲线点的哈希码值。
     * @return 哈希码值。
     */
    public int hashCode() {
        if (this == POINT_INFINITY) return 0;
        return x.hashCode() << 5 + y.hashCode();
    }
}
