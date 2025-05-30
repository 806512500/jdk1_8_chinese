/*
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
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

package java.math;

/**
 * 一个用于表示多精度整数的类，通过允许一个数只占用数组的一部分来高效利用分配的空间，因此数组不需要经常重新分配。
 * 当执行多次迭代的操作时，用于保存数字的数组只有在必要时才会增加，而不需要与它表示的数字大小相同。
 * 可变数字允许在同一个数字上进行计算，而不需要在计算的每一步都创建一个新数字，就像使用BigInteger时那样。
 *
 * 注意，SignedMutableBigIntegers只支持带符号的加法和减法。所有其他操作都与MutableBigIntegers相同。
 *
 * @see     BigInteger
 * @author  Michael McCloskey
 * @since   1.3
 */

class SignedMutableBigInteger extends MutableBigInteger {

   /**
     * 此MutableBigInteger的符号。
     */
    int sign = 1;

    // 构造函数

    /**
     * 默认构造函数。创建一个具有一个单词容量的空MutableBigInteger。
     */
    SignedMutableBigInteger() {
        super();
    }

    /**
     * 使用指定的int值构造一个新的MutableBigInteger。
     */
    SignedMutableBigInteger(int val) {
        super(val);
    }

    /**
     * 使用与指定的MutableBigInteger相同的值构造一个新的MutableBigInteger。
     */
    SignedMutableBigInteger(MutableBigInteger val) {
        super(val);
    }

   // 算术运算

   /**
     * 基于无符号加法和减法的带符号加法。
     */
    void signedAdd(SignedMutableBigInteger addend) {
        if (sign == addend.sign)
            add(addend);
        else
            sign = sign * subtract(addend);

    }

   /**
     * 基于无符号加法和减法的带符号加法。
     */
    void signedAdd(MutableBigInteger addend) {
        if (sign == 1)
            add(addend);
        else
            sign = sign * subtract(addend);

    }

   /**
     * 基于无符号加法和减法的带符号减法。
     */
    void signedSubtract(SignedMutableBigInteger addend) {
        if (sign == addend.sign)
            sign = sign * subtract(addend);
        else
            add(addend);

    }

   /**
     * 基于无符号加法和减法的带符号减法。
     */
    void signedSubtract(MutableBigInteger addend) {
        if (sign == 1)
            sign = sign * subtract(addend);
        else
            add(addend);
        if (intLen == 0)
             sign = 1;
    }

    /**
     * 从偏移量开始打印此MutableBigInteger的值数组的前intLen个整数。
     */
    public String toString() {
        return this.toBigInteger(sign).toString();
    }

}
