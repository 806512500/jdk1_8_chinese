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
 * 此不可变类定义了一个椭圆曲线（EC）特征2有限域。
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
     * 创建一个具有2^{@code m}个元素的椭圆曲线特征2有限域，使用正规基。
     * @param m 2^{@code m}是元素的数量。
     * @exception IllegalArgumentException 如果 {@code m} 不为正数。
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
     * 创建一个具有2^{@code m}个元素的椭圆曲线特征2有限域，使用多项式基。
     * 该域的约简多项式基于 {@code rp}，其第i位对应于约简多项式的第i个系数。<p>
     * 注意：有效的约简多项式要么是一个三项式（X^{@code m} + X^{@code k} + 1
     * 其中 {@code m} > {@code k} >= 1），要么是一个五项式（X^{@code m} + X^{@code k3}
     * + X^{@code k2} + X^{@code k1} + 1 其中 {@code m} > {@code k3} > {@code k2}
     * > {@code k1} >= 1）。
     * @param m 2^{@code m}是元素的数量。
     * @param rp BigInteger，其第i位对应于约简多项式的第i个系数。
     * @exception NullPointerException 如果 {@code rp} 为 null。
     * @exception IllegalArgumentException 如果 {@code m} 不为正数，或者 {@code rp} 不表示
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
     * 创建一个具有2^{@code m}个元素的椭圆曲线特征2有限域，使用多项式基。该域的约简多项式基于 {@code ks}，其内容
     * 包含约简多项式的中间项的顺序。
     * 注意：有效的约简多项式要么是一个三项式（X^{@code m} + X^{@code k} + 1
     * 其中 {@code m} > {@code k} >= 1），要么是一个五项式（X^{@code m} + X^{@code k3}
     * + X^{@code k2} + X^{@code k1} + 1 其中 {@code m} > {@code k3} > {@code k2}
     * > {@code k1} >= 1），因此 {@code ks} 应该有长度1或3。
     * @param m 2^{@code m}是元素的数量。
     * @param ks 约简多项式的中间项的顺序。此数组的内容被复制以防止后续修改。
     * @exception NullPointerException 如果 {@code ks} 为 null。
     * @exception IllegalArgumentException 如果 {@code m} 不为正数，或者 {@code ks} 的长度
     * 既不是1也不是3，或者 {@code ks} 中的值不在 {@code m}-1 和 1（包括）之间且按降序排列。
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
     * 返回以位表示的域大小，对于这个特征2有限域，即为 {@code m}。
     * @return 以位表示的域大小。
     */
    public int getFieldSize() {
        return m;
    }

    /**
     * 返回此特征2有限域的值 {@code m}。
     * @return {@code m}，2^{@code m}是元素的数量。
     */
    public int getM() {
        return m;
    }

    /**
     * 返回一个 BigInteger，其第i位对应于多项式基的约简多项式的第i个系数，或者对于正规基返回 null。
     * @return 一个 BigInteger，其第i位对应于多项式基的约简多项式的第i个系数，或者对于正规基返回 null。
     */
    public BigInteger getReductionPolynomial() {
        return rp;
    }

    /**
     * 返回一个整数数组，包含多项式基的约简多项式的中间项的顺序，或者对于正规基返回 null。
     * @return 一个整数数组，包含多项式基的约简多项式的中间项的顺序，或者对于正规基返回 null。每次调用此方法时都会返回一个新的数组。
     */
    public int[] getMidTermsOfReductionPolynomial() {
        if (ks == null) {
            return null;
        } else {
            return ks.clone();
        }
    }

    /**
     * 比较此有限域与指定对象是否相等。
     * @param obj 要比较的对象。
     * @return 如果 {@code obj} 是 ECFieldF2m 的实例且 {@code m} 和约简多项式匹配，则返回 true，否则返回 false。
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof ECFieldF2m) {
            // 无需在这里比较 rp，因为 ks 和 rp 应该是等价的
            return ((m == ((ECFieldF2m)obj).m) &&
                    (Arrays.equals(ks, ((ECFieldF2m) obj).ks)));
        }
        return false;
    }

    /**
     * 返回此特征2有限域的哈希码值。
     * @return 哈希码值。
     */
    public int hashCode() {
        int value = m << 5;
        value += (rp==null? 0:rp.hashCode());
        // 无需涉及 ks，因为 ks 和 rp 应该是等价的。
        return value;
    }
}
