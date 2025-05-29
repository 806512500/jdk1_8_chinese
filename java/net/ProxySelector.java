/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.IOException;
import java.util.List;
import sun.security.util.SecurityConstants;

/**
 * 选择连接到由 URL 引用的网络资源时要使用的代理服务器（如果有的话）。代理选择器是此类的具体子类，并通过调用
 * {@link java.net.ProxySelector#setDefault setDefault} 方法注册。通过调用
 * {@link java.net.ProxySelector#getDefault getDefault} 方法可以检索当前注册的代理选择器。
 *
 * <p> 当注册了代理选择器时，例如，URLConnection 类的子类应在每个 URL 请求时调用 {@link #select select}
 * 方法，以便代理选择器可以决定是使用直接连接还是代理连接。{@link #select select} 方法返回一个迭代器，遍历包含首选连接方式的集合。
 *
 * <p> 如果无法建立与代理（PROXY 或 SOCKS）服务器的连接，则调用者应调用代理选择器的
 * {@link #connectFailed connectFailed} 方法，通知代理选择器该代理服务器不可用。 </p>
 *
 * <P>默认的代理选择器确实强制执行与代理设置相关的
 * <a href="doc-files/net-properties.html#Proxies">一组系统属性</a>。</P>
 *
 * @author Yingxian Wang
 * @author Jean-Christophe Collet
 * @since 1.5
 */
public abstract class ProxySelector {
    /**
     * 选择连接到由 URL 引用的远程对象时要使用的代理服务器（如果有的话）的系统范围的代理选择器。
     *
     * @see #setDefault(ProxySelector)
     */
    private static ProxySelector theProxySelector;

    static {
        try {
            Class<?> c = Class.forName("sun.net.spi.DefaultProxySelector");
            if (c != null && ProxySelector.class.isAssignableFrom(c)) {
                theProxySelector = (ProxySelector) c.newInstance();
            }
        } catch (Exception e) {
            theProxySelector = null;
        }
    }

    /**
     * 获取系统范围的代理选择器。
     *
     * @throws  SecurityException
     *          如果已安装了安全经理，并且它拒绝
     * {@link NetPermission}{@code ("getProxySelector")}
     * @see #setDefault(ProxySelector)
     * @return 系统范围的 {@code ProxySelector}
     * @since 1.5
     */
    public static ProxySelector getDefault() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_PROXYSELECTOR_PERMISSION);
        }
        return theProxySelector;
    }

    /**
     * 设置（或取消设置）系统范围的代理选择器。
     *
     * 注意：非标准协议处理程序可能会忽略此设置。
     *
     * @param ps HTTP 代理选择器，或
     *          {@code null} 以取消设置代理选择器。
     *
     * @throws  SecurityException
     *          如果已安装了安全经理，并且它拒绝
     * {@link NetPermission}{@code ("setProxySelector")}
     *
     * @see #getDefault()
     * @since 1.5
     */
    public static void setDefault(ProxySelector ps) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.SET_PROXYSELECTOR_PERMISSION);
        }
        theProxySelector = ps;
    }

    /**
     * 根据访问资源的协议和访问资源的目标地址选择所有适用的代理。
     * URI 的格式定义如下：
     * <UL>
     * <LI>http URI 用于 http 连接</LI>
     * <LI>https URI 用于 https 连接</LI>
     * <LI>{@code socket://host:port}<br>
     *     用于 tcp 客户端套接字连接</LI>
     * </UL>
     *
     * @param   uri
     *          需要连接的 URI
     *
     * @return  代理的列表。列表中的每个元素都是
     *          {@link java.net.Proxy Proxy} 类型；
     *          当没有可用代理时，列表将包含一个
     *          {@link java.net.Proxy Proxy} 类型的元素，
     *          表示直接连接。
     * @throws IllegalArgumentException 如果参数为 null
     */
    public abstract List<Proxy> select(URI uri);

    /**
     * 调用以指示无法建立与代理/socks 服务器的连接。此方法的实现可以
     * 临时移除代理或重新排序由 {@link #select(URI)} 返回的代理序列，
     * 使用尝试连接时捕获的地址和 IOException。
     *
     * @param   uri
     *          代理在 sa 处无法服务的 URI。
     * @param   sa
     *          代理/SOCKS 服务器的套接字地址
     *
     * @param   ioe
     *          连接失败时抛出的 I/O 异常。
     * @throws IllegalArgumentException 如果任一参数为 null
     */
    public abstract void connectFailed(URI uri, SocketAddress sa, IOException ioe);
}
