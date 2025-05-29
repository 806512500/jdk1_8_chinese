/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

package java.security.cert;

/**
 * 用于 LDAP {@code CertStore} 算法的参数。
 * <p>
 * 本类用于向 LDAP {@code CertStore} 算法的实现提供必要的配置参数（服务器名称和端口号）。
 * <p>
 * <b>并发访问</b>
 * <p>
 * 除非另有说明，本类中定义的方法不是线程安全的。需要并发访问单个对象的多个线程应自行同步并提供必要的锁定。每个操作不同对象的多个线程不需要同步。
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
     * @return 名称（非 {@code null}）
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
     * 返回此对象的副本。对副本的更改不会影响原始对象，反之亦然。
     * <p>
     * 注意：此方法目前执行对象的浅拷贝（简单调用 {@code Object.clone()}）。如果添加了不应共享的新参数，未来版本可能会更改此方法以执行深拷贝。
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
