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
 * 提供用于密钥规范和算法参数规范的类和接口。
 *
 * <p>密钥规范是密钥材料的透明表示。密钥可以以算法特定的方式指定，也可以以算法独立的编码格式（如 ASN.1）指定。
 * 本包包含 DSA 公钥和私钥、RSA 公钥和私钥、DER 编码格式的 PKCS #8 私钥以及 DER 编码格式的 X.509 公钥和私钥的密钥规范。
 *
 * <p>算法参数规范是算法使用的参数集的透明表示。本包包含用于 DSA 算法的算法参数规范。
 *
 * <h2>包规范</h2>
 *
 * <ul>
 *   <li>PKCS #1: RSA 密码学规范，版本 2.2 (RFC 8017)</li>
 *   <li>PKCS #8: 私钥信息语法标准，
 *     版本 1.2，1993 年 11 月</li>
 *   <li>联邦信息处理标准出版物 (FIPS PUB) 186：
 *     数字签名标准 (DSS)</li>
 * </ul>
 *
 * <h2>相关文档</h2>
 *
 * 有关包含算法参数和密钥规范信息的文档，请参阅：
 * <ul>
 *   <li>
 *     <a href=
 *       "{@docRoot}/../technotes/guides/security/crypto/CryptoSpec.html">
 *       <b>Java&trade;
 *       密码学架构 API 规范和参考
 *       </b></a></li>
 *   <li>
 *     <a href=
 *       "{@docRoot}/../technotes/guides/security/crypto/HowToImplAProvider.html">
 *       <b>如何为
 *       Java&trade; 密码学架构实现提供者
 *       </b></a></li>
 * </ul>
 *
 * @since 1.2
 */
package java.security.spec;
