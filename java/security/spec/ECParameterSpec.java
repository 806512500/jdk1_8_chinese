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
 * 这个不可变类指定了用于椭圆曲线密码学（ECC）的一组域参数。
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
     * 返回该参数定义的椭圆曲线。
     * @return 该参数定义的椭圆曲线。
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
