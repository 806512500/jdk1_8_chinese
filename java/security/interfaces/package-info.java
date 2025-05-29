/*
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
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
 * 提供用于生成 RSA（Rivest, Shamir 和 Adleman 非对称加密算法）密钥的接口，
 * 如 RSA 实验室技术笔记 PKCS#1 中定义，以及 DSA（数字签名算法）密钥，
 * 如 NIST 的 FIPS-186 中定义。
 * <P>
 * 请注意，这些接口仅适用于密钥材料可访问且可用的密钥实现。
 * 这些接口不适用于密钥材料存储在不可访问、受保护的存储中（例如在硬件设备中）的密钥实现。
 * <P>
 * 有关如何使用这些接口的更多信息，包括如何为硬件设备设计 {@code Key} 类的信息，
 * 请参阅这些加密提供程序开发人员指南：
 * <ul>
 *   <li><a href=
 *     "{@docRoot}/../technotes/guides/security/crypto/HowToImplAProvider.html">
 *     <b>如何为 Java&trade; 加密架构实现提供程序
 *     </b></a></li>
 * </ul>
 *
 * <h2>包规范</h2>
 *
 * <ul>
 *   <li>PKCS #1: RSA 加密规范，版本 2.2 (RFC 8017)</li>
 *   <li>联邦信息处理标准出版物 (FIPS PUB) 186:
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
