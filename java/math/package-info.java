/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

/**
 * 提供用于执行任意精度整数算术（{@code BigInteger}）和任意精度十进制算术（{@code BigDecimal}）的类。
 * {@code BigInteger} 类似于基本整数类型，但提供了任意精度，因此对 {@code BigInteger} 的操作不会溢出或丢失精度。
 * 除了标准的算术运算外，{@code BigInteger} 还提供了模运算、GCD 计算、素性测试、素数生成、位操作以及一些其他杂项操作。
 *
 * {@code BigDecimal} 提供了适合货币计算等用途的任意精度带符号十进制数。{@code BigDecimal} 使用户可以完全控制舍入行为，
 * 允许用户从全面的八种舍入模式中选择。
 *
 * @since JDK1.1
 */
package java.math;
