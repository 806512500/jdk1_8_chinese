/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.net;

import java.security.cert.Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.Principal;
import java.util.List;

/**
 * 表示通过安全手段（如 TLS）最初检索的缓存响应。
 *
 * @since 1.5
 */
public abstract class SecureCacheResponse extends CacheResponse {
    /**
     * 返回最初连接时用于检索网络资源的加密套件。
     *
     * @return 一个表示加密套件的字符串
     */
    public abstract String getCipherSuite();

    /**
     * 返回最初连接时在握手过程中发送给服务器的证书链。注意：此方法仅在使用基于证书的加密套件时有用。
     *
     * @return 一个不可变的 Certificate 列表，表示发送给服务器的证书链。如果没有发送证书链，则返回 null。
     * @see #getLocalPrincipal()
     */
    public abstract List<Certificate> getLocalCertificateChain();

    /**
     * 从缓存中返回最初连接时在定义会话过程中建立的服务器证书链。注意：此方法仅在使用基于证书的加密套件时可用；
     * 使用非基于证书的加密套件（如 Kerberos）时，将抛出 SSLPeerUnverifiedException。
     *
     * @return 一个不可变的 Certificate 列表，表示服务器的证书链。
     * @throws SSLPeerUnverifiedException 如果对等方未验证。
     * @see #getPeerPrincipal()
     */
    public abstract List<Certificate> getServerCertificateChain()
        throws SSLPeerUnverifiedException;

    /**
     * 返回最初连接时在定义会话过程中建立的服务器的主体。
     *
     * @return 服务器的主体。对于基于 X509 的加密套件，返回 X500Principal 的终端实体证书；对于基于 Kerberos 的加密套件，返回 KerberosPrincipal。
     *
     * @throws SSLPeerUnverifiedException 如果对等方未验证。
     *
     * @see #getServerCertificateChain()
     * @see #getLocalPrincipal()
     */
     public abstract Principal getPeerPrincipal()
             throws SSLPeerUnverifiedException;

    /**
      * 返回最初连接时在握手过程中发送给服务器的主体。
      *
      * @return 发送给服务器的主体。对于基于 X509 的加密套件，返回 X500Principal 的终端实体证书；对于基于 Kerberos 的加密套件，返回 KerberosPrincipal。如果没有发送主体，则返回 null。
      *
      * @see #getLocalCertificateChain()
      * @see #getPeerPrincipal()
      */
     public abstract Principal getLocalPrincipal();
}
