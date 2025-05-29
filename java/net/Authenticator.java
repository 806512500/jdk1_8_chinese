
/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.net;

/**
 * Authenticator 类表示一个知道如何为网络连接获取身份验证的对象。通常，它将通过提示用户输入信息来实现这一点。
 * <p>
 * 应用程序通过在子类中覆盖 {@link
 * #getPasswordAuthentication()} 方法来使用此类。此方法通常使用各种 getXXX() 访问器方法来获取请求身份验证的实体的信息。
 * 然后，它必须通过与用户交互或其他非交互方式获取用户名和密码。凭据随后作为 {@link PasswordAuthentication} 返回值返回。
 * <p>
 * 该具体子类的实例通过调用 {@link #setDefault(Authenticator)} 注册到系统中。当需要身份验证时，系统将调用其中一个
 * requestPasswordAuthentication() 方法，这些方法反过来会调用已注册对象的 getPasswordAuthentication() 方法。
 * <p>
 * 所有请求身份验证的方法都有默认实现，这些实现会失败。
 *
 * @see java.net.Authenticator#setDefault(java.net.Authenticator)
 * @see java.net.Authenticator#getPasswordAuthentication()
 *
 * @author  Bill Foote
 * @since   1.2
 */

// 没有抽象方法，但为了有用，用户必须子类化。
public abstract
class Authenticator {

    // 系统范围的身份验证器对象。参见 setDefault()。
    private static Authenticator theAuthenticator;

    private String requestingHost;
    private InetAddress requestingSite;
    private int requestingPort;
    private String requestingProtocol;
    private String requestingPrompt;
    private String requestingScheme;
    private URL requestingURL;
    private RequestorType requestingAuthType;

    /**
     * 请求身份验证的实体类型。
     *
     * @since 1.5
     */
    public enum RequestorType {
        /**
         * 请求身份验证的实体是 HTTP 代理服务器。
         */
        PROXY,
        /**
         * 请求身份验证的实体是 HTTP 原始服务器。
         */
        SERVER
    }

    private void reset() {
        requestingHost = null;
        requestingSite = null;
        requestingPort = -1;
        requestingProtocol = null;
        requestingPrompt = null;
        requestingScheme = null;
        requestingURL = null;
        requestingAuthType = RequestorType.SERVER;
    }


    /**
     * 设置当代理或 HTTP 服务器请求身份验证时网络代码将使用的身份验证器。
     * <p>
     * 首先，如果有安全经理，其 {@code checkPermission}
     * 方法将被调用，权限为
     * {@code NetPermission("setDefaultAuthenticator")}。
     * 这可能导致 java.lang.SecurityException。
     *
     * @param   a       要设置的身份验证器。如果 a 是 {@code null}，则移除任何先前设置的身份验证器。
     *
     * @throws SecurityException
     *        如果存在安全经理且其
     *        {@code checkPermission} 方法不允许
     *        设置默认身份验证器。
     *
     * @see SecurityManager#checkPermission
     * @see java.net.NetPermission
     */
    public synchronized static void setDefault(Authenticator a) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            NetPermission setDefaultPermission
                = new NetPermission("setDefaultAuthenticator");
            sm.checkPermission(setDefaultPermission);
        }

        theAuthenticator = a;
    }

    /**
     * 请求系统中已注册的身份验证器提供密码。
     * <p>
     * 首先，如果有安全经理，其 {@code checkPermission}
     * 方法将被调用，权限为
     * {@code NetPermission("requestPasswordAuthentication")}。
     * 这可能导致 java.lang.SecurityException。
     *
     * @param addr 请求授权的站点的 InetAddress，如果未知则为 null。
     * @param port 请求连接的端口。
     * @param protocol 请求连接的协议
     *          ({@link java.net.Authenticator#getRequestingProtocol()})
     * @param prompt 用户的提示字符串。
     * @param scheme 身份验证方案。
     *
     * @return 用户名/密码，如果无法获取则为 null。
     *
     * @throws SecurityException
     *        如果存在安全经理且其
     *        {@code checkPermission} 方法不允许
     *        请求密码身份验证。
     *
     * @see SecurityManager#checkPermission
     * @see java.net.NetPermission
     */
    public static PasswordAuthentication requestPasswordAuthentication(
                                            InetAddress addr,
                                            int port,
                                            String protocol,
                                            String prompt,
                                            String scheme) {

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            NetPermission requestPermission
                = new NetPermission("requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }

        Authenticator a = theAuthenticator;
        if (a == null) {
            return null;
        } else {
            synchronized(a) {
                a.reset();
                a.requestingSite = addr;
                a.requestingPort = port;
                a.requestingProtocol = protocol;
                a.requestingPrompt = prompt;
                a.requestingScheme = scheme;
                return a.getPasswordAuthentication();
            }
        }
    }

    /**
     * 请求系统中已注册的身份验证器提供密码。这是请求密码的首选方法，因为在无法提供 InetAddress 的情况下可以提供主机名。
     * <p>
     * 首先，如果有安全经理，其 {@code checkPermission}
     * 方法将被调用，权限为
     * {@code NetPermission("requestPasswordAuthentication")}。
     * 这可能导致 java.lang.SecurityException。
     *
     * @param host 请求身份验证的站点的主机名。
     * @param addr 请求身份验证的站点的 InetAddress，如果未知则为 null。
     * @param port 请求连接的端口。
     * @param protocol 请求连接的协议
     *          ({@link java.net.Authenticator#getRequestingProtocol()})
     * @param prompt 用户的提示字符串，用于标识身份验证域。
     * @param scheme 身份验证方案。
     *
     * @return 用户名/密码，如果无法获取则为 null。
     *
     * @throws SecurityException
     *        如果存在安全经理且其
     *        {@code checkPermission} 方法不允许
     *        请求密码身份验证。
     *
     * @see SecurityManager#checkPermission
     * @see java.net.NetPermission
     * @since 1.4
     */
    public static PasswordAuthentication requestPasswordAuthentication(
                                            String host,
                                            InetAddress addr,
                                            int port,
                                            String protocol,
                                            String prompt,
                                            String scheme) {


                    SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            NetPermission requestPermission
                = new NetPermission("requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }

        Authenticator a = theAuthenticator;
        if (a == null) {
            return null;
        } else {
            synchronized(a) {
                a.reset();
                a.requestingHost = host;
                a.requestingSite = addr;
                a.requestingPort = port;
                a.requestingProtocol = protocol;
                a.requestingPrompt = prompt;
                a.requestingScheme = scheme;
                return a.getPasswordAuthentication();
            }
        }
    }

    /**
     * 向系统注册的身份验证器请求密码。
     * <p>
     * 首先，如果有安全经理，其 {@code checkPermission}
     * 方法将被调用，权限为
     * {@code NetPermission("requestPasswordAuthentication")}。
     * 这可能会导致 java.lang.SecurityException。
     *
     * @param host 请求身份验证的站点的主机名。
     * @param addr 请求授权的站点的 InetAddress，
     *             如果未知则为 null。
     * @param port 请求连接的端口
     * @param protocol 请求连接的协议
     *          ({@link java.net.Authenticator#getRequestingProtocol()})
     * @param prompt 用户的提示字符串
     * @param scheme 身份验证方案
     * @param url 导致身份验证的请求 URL
     * @param reqType 请求身份验证的实体类型（服务器或代理）。
     *
     * @return 用户名/密码，如果无法获取则返回 null。
     *
     * @throws SecurityException
     *        如果存在安全经理且其
     *        {@code checkPermission} 方法不允许
     *        密码身份验证请求。
     *
     * @see SecurityManager#checkPermission
     * @see java.net.NetPermission
     *
     * @since 1.5
     */
    public static PasswordAuthentication requestPasswordAuthentication(
                                    String host,
                                    InetAddress addr,
                                    int port,
                                    String protocol,
                                    String prompt,
                                    String scheme,
                                    URL url,
                                    RequestorType reqType) {

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            NetPermission requestPermission
                = new NetPermission("requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }

        Authenticator a = theAuthenticator;
        if (a == null) {
            return null;
        } else {
            synchronized(a) {
                a.reset();
                a.requestingHost = host;
                a.requestingSite = addr;
                a.requestingPort = port;
                a.requestingProtocol = protocol;
                a.requestingPrompt = prompt;
                a.requestingScheme = scheme;
                a.requestingURL = url;
                a.requestingAuthType = reqType;
                return a.getPasswordAuthentication();
            }
        }
    }

    /**
     * 获取请求身份验证的站点或代理的 {@code 主机名}，如果不可用则返回 {@code null}。
     *
     * @return 需要身份验证的连接的主机名，如果不可用则返回 null。
     * @since 1.4
     */
    protected final String getRequestingHost() {
        return requestingHost;
    }

    /**
     * 获取请求授权的站点的 {@code InetAddress}，如果不可用则返回 {@code null}。
     *
     * @return 请求授权的站点的 InetAddress，如果不可用则返回 null。
     */
    protected final InetAddress getRequestingSite() {
        return requestingSite;
    }

    /**
     * 获取请求连接的端口号。
     * @return 一个 {@code int}，表示请求连接的端口。
     */
    protected final int getRequestingPort() {
        return requestingPort;
    }

    /**
     * 获取请求连接的协议。通常这将基于 URL，但在未来的 JDK 中，例如，可能是受密码保护的 SOCKS5 防火墙的 "SOCKS"。
     *
     * @return 协议，可选地后跟 "/version"，其中 version 是版本号。
     *
     * @see java.net.URL#getProtocol()
     */
    protected final String getRequestingProtocol() {
        return requestingProtocol;
    }

    /**
     * 获取请求者的提示字符串。
     *
     * @return 请求者提供的提示字符串（对于 HTTP 请求为 realm）。
     */
    protected final String getRequestingPrompt() {
        return requestingPrompt;
    }

    /**
     * 获取请求者的方案（例如，HTTP 防火墙的 HTTP 方案）。
     *
     * @return 请求者的方案。
     *
     */
    protected final String getRequestingScheme() {
        return requestingScheme;
    }

    /**
     * 当需要密码授权时调用。子类应覆盖默认实现，该实现返回 null。
     * @return 从用户收集的 PasswordAuthentication，如果未提供则返回 null。
     */
    protected PasswordAuthentication getPasswordAuthentication() {
        return null;
    }

    /**
     * 返回导致此身份验证请求的 URL。
     *
     * @since 1.5
     *
     * @return 请求的 URL。
     *
     */
    protected URL getRequestingURL () {
        return requestingURL;
    }

    /**
     * 返回请求者是代理还是服务器。
     *
     * @since 1.5
     *
     * @return 请求者的身份验证类型。
     *
     */
    protected RequestorType getRequestorType () {
        return requestingAuthType;
    }
}
