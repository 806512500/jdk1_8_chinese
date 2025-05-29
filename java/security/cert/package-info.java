/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 提供用于解析和管理证书、证书吊销列表（CRLs）和认证路径的类和接口。它支持X.509 v3
 * 证书和X.509 v2 CRLs。
 *
 * <h2>包规范</h2>
 *
 * <ul>
 *   <li><a href="{@docRoot}/../technotes/guides/security/crypto/CryptoSpec.html">
 *     <b>Java&trade;
 *     加密架构 (JCA) 参考指南</b></a>
 *   <li>RFC 5280: Internet X.509 公钥基础设施证书和
 *     证书吊销列表 (CRL) 配置文件
 *   <li>RFC 2560: X.509 Internet 公钥基础设施在线证书
 *     状态协议 - OCSP
 *   <li><a href="{@docRoot}/../technotes/guides/security/StandardNames.html">
 *     <b>Java&trade;
 *     加密架构标准算法名称
 *     文档</b></a></li>
 * </ul>
 *
 * <h2>相关文档</h2>
 *
 * 有关X.509证书和CRLs的信息，请参见：
 * <ul>
 *   <li><a href="http://www.ietf.org/rfc/rfc5280.txt">
 *     http://www.ietf.org/rfc/rfc5280.txt</a>
 *   <li><a href=
 *     "{@docRoot}/../technotes/guides/security/certpath/CertPathProgGuide.html">
 *     <b>Java&trade;
 *     PKI 编程指南</b></a>
 *   <li><a href="{@docRoot}/../technotes/guides/security/cert3.html">
 *     <b>X.509 证书和证书吊销列表 (CRLs)</b></a>
 * </ul>
 *
 * @since 1.2
 */
package java.security.cert;
