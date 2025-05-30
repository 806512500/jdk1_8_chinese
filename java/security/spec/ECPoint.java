/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.security.spec;

import java.math.BigInteger;

/**
 * 此不可变类表示椭圆曲线（EC）上的一个点，使用仿射坐标。其他坐标系统可以
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
     * 注意：POINT_INFINITY 有一个 null 的仿射 x 坐标。
     * @return 仿射 x 坐标。
     */
    public BigInteger getAffineX() {
        return x;
    }

    /**
     * 返回仿射 y 坐标 {@code y}。
     * 注意：POINT_INFINITY 有一个 null 的仿射 y 坐标。
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
