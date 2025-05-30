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
 * 提供安全框架的类和接口。
 * 这包括实现易于配置的、细粒度的访问控制安全架构的类。
 * 该包还支持生成和存储加密公钥对，
 * 以及包括消息摘要和签名生成在内的多种可导出的加密操作。最后，
 * 该包提供了支持签名/保护对象和安全随机数生成的类。
 *
 * 该包中提供的许多类（特别是加密和安全随机数生成器类）都是基于提供者的。
 * 类本身定义了应用程序可以编写的编程接口。
 * 实现本身可以由独立的第三方供应商编写，并根据需要无缝插入。
 * 因此，应用程序开发人员可以利用任何数量的基于提供者的实现，
 * 而无需添加或重写代码。
 *
 * <h2>包规范</h2>
 *
 * <ul>
 *   <li><a href="{@docRoot}/../technotes/guides/security/crypto/CryptoSpec.html">
 *     <b>Java&trade;
 *     加密架构 (JCA) 参考指南</b></a></li>
 *
 *   <li>PKCS #8: 私钥信息语法标准，版本 1.2，
 *     1993 年 11 月</li>
 *
 *   <li><a href="{@docRoot}/../technotes/guides/security/StandardNames.html">
 *     <b>Java&trade;
 *     加密架构标准算法名称文档</b></a></li>
 * </ul>
 *
 * <h2>相关文档</h2>
 *
 * 请参阅更多文档：
 * <ul>
 *   <li><a href=
 *     "{@docRoot}/../technotes/guides/security/spec/security-spec.doc.html">
 *     <b>Java&trade;
 *     SE 平台安全架构</b></a></li>
 *
 *   <li><a href=
 *     "{@docRoot}/../technotes/guides/security/crypto/HowToImplAProvider.html">
 *     <b>如何在
 *     Java&trade; 加密架构中实现提供者
 *     </b></a></li>
 *
 *   <li><a href=
 *     "{@docRoot}/../technotes/guides/security/PolicyFiles.html"><b>
 *     默认策略实现和策略文件语法
 *     </b></a></li>
 *
 *   <li><a href=
 *     "{@docRoot}/../technotes/guides/security/permissions.html"><b>
 *     Java&trade; SE 开发工具包 (JDK) 中的权限
 *     </b></a></li>
 *
 *   <li><a href=
 *     "{@docRoot}/../technotes/guides/security/SecurityToolsSummary.html"><b>
 *     Java&trade; 平台安全工具概览
 *     </b></a></li>
 *
 *   <li><b>keytool</b>
 *     (<a href="{@docRoot}/../technotes/tools/unix/keytool.html">
 *       适用于 Solaris/Linux</a>)
 *     (<a href="{@docRoot}/../technotes/tools/windows/keytool.html">
 *       适用于 Windows</a>)
 *     </li>
 *
 *   <li><b>jarsigner</b>
 *     (<a href="{@docRoot}/../technotes/tools/unix/jarsigner.html">
 *       适用于 Solaris/Linux</a>)
 *     (<a href="{@docRoot}/../technotes/tools/windows/jarsigner.html">
 *       适用于 Windows</a>)
 *     </li>
 *
 * </ul>
 *
 * @since 1.1
 */
package java.security;
