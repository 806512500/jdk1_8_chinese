/*
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
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
 * 提供了根据 RSA（Rivest, Shamir 和 Adleman 非对称加密算法）在 RSA 实验室技术说明
 * PKCS#1 中定义的接口，以及根据 NIST 的 FIPS-186 定义的 DSA（数字签名
 * 算法）的接口。
 * <P>
 * 请注意，这些接口仅适用于密钥材料可访问和可用的密钥
 * 实现。这些接口不适用于密钥材料存储在
 * 不可访问、受保护的存储（如在
 * 硬件设备中）中的密钥
 * 实现。
 * <P>
 * 有关如何使用这些
 * 接口的更多信息，包括如何为硬件设备设计
 * {@code Key} 类的信息，请参阅
 * 这些加密提供程序开发人员指南：
 * <ul>
 *   <li><a href=
 *     "{@docRoot}/../technotes/guides/security/crypto/HowToImplAProvider.html">
 *     <b>如何为
 *     Java&trade; 加密架构实现提供程序
 *     </b></a></li>
 * </ul>
 *
 * <h2>包规范</h2>
 *
 * <ul>
 *   <li>PKCS #1: RSA 加密规范，版本 2.2 (RFC 8017)</li>
 *   <li>联邦信息处理标准出版物 (FIPS PUB) 186：
 *     数字签名标准 (DSS) </li>
 * </ul>
 *
 * <h2>相关文档</h2>
 *
 * 有关进一步的文档，请参阅：
 * <ul>
 *   <li>
 *     <a href=
 *       "{@docRoot}/../technotes/guides/security/crypto/CryptoSpec.html">
 *       <b>Java&trade;
 *       加密架构 API 规范和参考
 *       </b></a></li>
 * </ul>
 *
 * @since JDK1.1
 */
package java.security.interfaces;
