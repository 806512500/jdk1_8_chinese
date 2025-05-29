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
 * 提供密钥规范和算法参数规范的类和接口。
 *
 * <p>密钥规范是密钥材料的透明表示。密钥可以以算法特定的方式指定，也可以以算法无关的编码格式（如ASN.1）指定。
 * 本包包含DSA公钥和私钥、RSA公钥和私钥、DER编码格式的PKCS #8私钥以及DER编码格式的X.509公钥和私钥的密钥规范。
 *
 * <p>算法参数规范是算法使用参数集的透明表示。本包包含用于DSA算法的参数的算法参数规范。
 *
 * <h2>包规范</h2>
 *
 * <ul>
 *   <li>PKCS #1: RSA Cryptography Specifications, Version 2.2 (RFC 8017)</li>
 *   <li>PKCS #8: Private-Key Information Syntax Standard,
 *     Version 1.2, November 1993</li>
 *   <li>Federal Information Processing Standards Publication (FIPS PUB) 186:
 *     Digital Signature Standard (DSS)</li>
 * </ul>
 *
 * <h2>相关文档</h2>
 *
 * 有关包含算法参数和密钥规范信息的文档，请参见：
 * <ul>
 *   <li>
 *     <a href=
 *       "{@docRoot}/../technotes/guides/security/crypto/CryptoSpec.html">
 *       <b>Java&trade;
 *       Cryptography Architecture API Specification and Reference
 *       </b></a></li>
 *   <li>
 *     <a href=
 *       "{@docRoot}/../technotes/guides/security/crypto/HowToImplAProvider.html">
 *       <b>如何为
 *       Java&trade; Cryptography Architecture 实现提供者
 *       </b></a></li>
 * </ul>
 *
 * @since 1.2
 */
package java.security.spec;
