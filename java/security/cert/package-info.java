/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 提供用于解析和管理证书、证书吊销列表（CRLs）和认证路径的类和接口。它支持X.509 v3证书和X.509 v2 CRLs。
 *
 * <h2>包规范</h2>
 *
 * <ul>
 *   <li><a href="{@docRoot}/../technotes/guides/security/crypto/CryptoSpec.html">
 *     <b>Java&trade;
 *     加密架构（JCA）参考指南</b></a>
 *   <li>RFC 5280: Internet X.509 公钥基础设施证书和证书吊销列表（CRL）配置文件
 *   <li>RFC 2560: X.509 Internet 公钥基础设施在线证书状态协议 - OCSP
 *   <li><a href="{@docRoot}/../technotes/guides/security/StandardNames.html">
 *     <b>Java&trade;
 *     加密架构标准算法名称文档</b></a></li>
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
 *     <b>X.509 证书和证书吊销列表（CRLs）</b></a>
 * </ul>
 *
 * @since 1.2
 */
package java.security.cert;
