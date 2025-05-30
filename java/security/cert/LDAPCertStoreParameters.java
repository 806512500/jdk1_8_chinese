/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security.cert;

/**
 * 用于 LDAP {@code CertStore} 算法的参数。
 * <p>
 * 此类用于向 LDAP {@code CertStore} 算法的实现提供必要的配置参数（服务器名称和端口号）。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，此类定义的方法不是线程安全的。多个线程需要并发访问单个对象时，应同步并提供必要的锁定。多个线程各自操作不同的对象时，无需同步。
 *
 * @since       1.4
 * @author      Steve Hanna
 * @see         CertStore
 */
public class LDAPCertStoreParameters implements CertStoreParameters {

    private static final int LDAP_DEFAULT_PORT = 389;

    /**
     * LDAP 服务器的端口号
     */
    private int port;

    /**
     * LDAP 服务器的 DNS 名称
     */
    private String serverName;

    /**
     * 使用指定的参数值创建 {@code LDAPCertStoreParameters} 的实例。
     *
     * @param serverName LDAP 服务器的 DNS 名称
     * @param port LDAP 服务器的端口号
     * @exception NullPointerException 如果 {@code serverName} 为
     * {@code null}
     */
    public LDAPCertStoreParameters(String serverName, int port) {
        if (serverName == null)
            throw new NullPointerException();
        this.serverName = serverName;
        this.port = port;
    }

    /**
     * 使用指定的服务器名称和默认端口 389 创建 {@code LDAPCertStoreParameters} 的实例。
     *
     * @param serverName LDAP 服务器的 DNS 名称
     * @exception NullPointerException 如果 {@code serverName} 为
     * {@code null}
     */
    public LDAPCertStoreParameters(String serverName) {
        this(serverName, LDAP_DEFAULT_PORT);
    }

    /**
     * 使用默认参数值（服务器名称 "localhost"，端口 389）创建 {@code LDAPCertStoreParameters} 的实例。
     */
    public LDAPCertStoreParameters() {
        this("localhost", LDAP_DEFAULT_PORT);
    }

    /**
     * 返回 LDAP 服务器的 DNS 名称。
     *
     * @return 名称（不为 {@code null}）
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * 返回 LDAP 服务器的端口号。
     *
     * @return 端口号
     */
    public int getPort() {
        return port;
    }

    /**
     * 返回此对象的副本。对副本的修改不会影响原始对象，反之亦然。
     * <p>
     * 注意：此方法目前执行的是浅拷贝（简单调用 {@code Object.clone()}）。将来可能会更改以执行深拷贝，如果添加了不应共享的新参数。
     *
     * @return 副本
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            /* 不可能发生 */
            throw new InternalError(e.toString(), e);
        }
    }

    /**
     * 返回描述参数的格式化字符串。
     *
     * @return 描述参数的格式化字符串
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LDAPCertStoreParameters: [\n");

        sb.append("  serverName: " + serverName + "\n");
        sb.append("  port: " + port + "\n");
        sb.append("]");
        return sb.toString();
    }
}
