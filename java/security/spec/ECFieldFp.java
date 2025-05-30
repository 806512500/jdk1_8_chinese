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
     * 使用指定的素数 {@code p} 创建一个椭圆曲线素数有限域。
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
     * 返回此素数有限域的字段大小（以位为单位），即素数 p 的大小。
     * @return 字段大小（以位为单位）。
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
