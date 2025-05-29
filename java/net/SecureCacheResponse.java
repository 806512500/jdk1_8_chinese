/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.net;

import java.security.cert.Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.Principal;
import java.util.List;

/**
 * 表示通过安全手段（如 TLS）最初检索到的缓存响应。
 *
 * @since 1.5
 */
public abstract class SecureCacheResponse extends CacheResponse {
    /**
     * 返回最初检索网络资源时使用的连接所使用的加密套件。
     *
     * @return 一个表示加密套件的字符串
     */
    public abstract String getCipherSuite();

    /**
     * 返回在最初检索网络资源时使用的连接握手期间发送给服务器的证书链。注意：此方法仅在使用基于证书的加密套件时有用。
     *
     * @return 一个表示发送给服务器的证书链的不可变 List。如果没有发送证书链，则返回 null。
     * @see #getLocalPrincipal()
     */
    public abstract List<Certificate> getLocalCertificateChain();

    /**
     * 从缓存中返回最初检索网络资源时建立会话的服务器证书链。注意：此方法仅在使用基于证书的加密套件时可用；
     * 使用非基于证书的加密套件（如 Kerberos）将抛出 SSLPeerUnverifiedException。
     *
     * @return 一个表示服务器证书链的不可变 List。
     * @throws SSLPeerUnverifiedException 如果对等方未验证。
     * @see #getPeerPrincipal()
     */
    public abstract List<Certificate> getServerCertificateChain()
        throws SSLPeerUnverifiedException;

    /**
     * 返回最初检索网络资源时在会话定义过程中建立的服务器主体。
     *
     * @return 服务器的主体。对于基于 X509 的加密套件，返回端实体证书的 X500Principal；对于 Kerberos 加密套件，返回 KerberosPrincipal。
     *
     * @throws SSLPeerUnverifiedException 如果对等方未验证。
     *
     * @see #getServerCertificateChain()
     * @see #getLocalPrincipal()
     */
     public abstract Principal getPeerPrincipal()
             throws SSLPeerUnverifiedException;

    /**
      * 返回最初检索网络资源时在连接握手期间发送给服务器的主体。
      *
      * @return 发送给服务器的主体。对于基于 X509 的加密套件，返回端实体证书的 X500Principal；对于 Kerberos 加密套件，返回 KerberosPrincipal。如果没有发送主体，则返回 null。
      *
      * @see #getLocalCertificateChain()
      * @see #getPeerPrincipal()
      */
     public abstract Principal getLocalPrincipal();
}
