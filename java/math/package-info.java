/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 提供用于执行任意精度整数算术（{@code BigInteger}）和任意精度十进制算术（{@code BigDecimal}）的类。{@code BigInteger} 类似于原始整数类型，但提供了任意精度，因此对 {@code BigInteger} 的操作不会溢出或丢失精度。除了标准的算术运算外，{@code BigInteger} 还提供了模运算、GCD 计算、素性测试、素数生成、位操作和一些其他杂项操作。
 *
 * {@code BigDecimal} 提供适合货币计算等用途的任意精度带符号十进制数。{@code BigDecimal} 使用户能够完全控制舍入行为，允许用户从八种舍入模式中选择。
 *
 * @since JDK1.1
 */
package java.math;
