/*
 * 版权所有 (c) 2002, 2013, Oracle 和/或其附属公司。保留所有权利。
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

/*
 * 包私有实现的 InetAddressImpl 用于双栈
 * IPv4/IPv6。
 * <p>
 * 如果 InetAddress.preferIPv6Address 为 true，则 anyLocalAddress()、
 * loopbackAddress() 和 localHost() 将返回 IPv6 地址，否则返回 IPv4 地址。
 *
 * @since 1.4
 */

class Inet6AddressImpl implements InetAddressImpl {
    public native String getLocalHostName() throws UnknownHostException;
    public native InetAddress[]
        lookupAllHostAddr(String hostname) throws UnknownHostException;
    public native String getHostByAddr(byte[] addr) throws UnknownHostException;
    private native boolean isReachable0(byte[] addr, int scope, int timeout, byte[] inf, int ttl, int if_scope) throws IOException;

    public boolean isReachable(InetAddress addr, int timeout, NetworkInterface netif, int ttl) throws IOException {
        byte[] ifaddr = null;
        int scope = -1;
        int netif_scope = -1;
        if (netif != null) {
            /*
             * 确保我们绑定到适当地址族的地址。
             * 这意味着与 addr 相同的地址族，因为在这一点上它可能是
             * IPv6 地址或 IPv4 地址（双栈系统的情况）。
             */
            java.util.Enumeration<InetAddress> it = netif.getInetAddresses();
            InetAddress inetaddr = null;
            while (it.hasMoreElements()) {
                inetaddr = it.nextElement();
                if (inetaddr.getClass().isInstance(addr)) {
                    ifaddr = inetaddr.getAddress();
                    if (inetaddr instanceof Inet6Address) {
                        netif_scope = ((Inet6Address) inetaddr).getScopeId();
                    }
                    break;
                }
            }
            if (ifaddr == null) {
                // 接口不支持目标地址族
                return false;
            }
        }
        if (addr instanceof Inet6Address)
            scope = ((Inet6Address) addr).getScopeId();
        return isReachable0(addr.getAddress(), scope, timeout, ifaddr, ttl, netif_scope);
    }

    public synchronized InetAddress anyLocalAddress() {
        if (anyLocalAddress == null) {
            if (InetAddress.preferIPv6Address) {
                anyLocalAddress = new Inet6Address();
                anyLocalAddress.holder().hostName = "::";
            } else {
                anyLocalAddress = (new Inet4AddressImpl()).anyLocalAddress();
            }
        }
        return anyLocalAddress;
    }

    public synchronized InetAddress loopbackAddress() {
        if (loopbackAddress == null) {
             if (InetAddress.preferIPv6Address) {
                 byte[] loopback =
                        {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                         0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
                 loopbackAddress = new Inet6Address("localhost", loopback);
             } else {
                loopbackAddress = (new Inet4AddressImpl()).loopbackAddress();
             }
        }
        return loopbackAddress;
    }

    private InetAddress      anyLocalAddress;
    private InetAddress      loopbackAddress;
}
