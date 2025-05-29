/*
 * 版权所有 (c) 2007, 2015, Oracle 和/或其附属公司。保留所有权利。
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

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * 该类定义了一个用于创建 DatagramSocketImpl 的工厂。默认情况下，它创建普通的 DatagramSocketImpl，但可以通过设置 impl.prefix 系统属性来创建其他 DatagramSocketImpl。
 *
 * 对于低于 Windows Vista 的 Windows 版本，总是创建 TwoStacksPlainDatagramSocketImpl。此实现支持这些平台上可用的 IPv6。
 *
 * 在支持双层 TCP/IP 栈的 Windows Vista 以上版本的平台上，为 DatagramSockets 创建 DualStackPlainDatagramSocketImpl。对于 MulticastSockets，总是创建 TwoStacksPlainDatagramSocketImpl。这是为了克服 RFC 对双层套接字上的多播行为定义的不足。
 *
 * @author Chris Hegarty
 */

class DefaultDatagramSocketImplFactory
{
    private final static Class<?> prefixImplClass;

    /* Windows 版本。 */
    private static float version;

    /* java.net.preferIPv4Stack */
    private static boolean preferIPv4Stack = false;

    /* 如果版本支持双栈 TCP 实现 */
    private final static boolean useDualStackImpl;

    /* sun.net.useExclusiveBind */
    private static String exclBindProp;

    /* 如果 Windows 上启用了独占绑定，则为真 */
    private final static boolean exclusiveBind;

    static {
        Class<?> prefixImplClassLocal = null;
        boolean useDualStackImplLocal = false;
        boolean exclusiveBindLocal = true;

        // 确定 Windows 版本。
        java.security.AccessController.doPrivileged(
                new PrivilegedAction<Object>() {
                    public Object run() {
                        version = 0;
                        try {
                            version = Float.parseFloat(System.getProperties()
                                    .getProperty("os.version"));
                            preferIPv4Stack = Boolean.parseBoolean(
                                              System.getProperties()
                                              .getProperty(
                                                   "java.net.preferIPv4Stack"));
                            exclBindProp = System.getProperty(
                                    "sun.net.useExclusiveBind");
                        } catch (NumberFormatException e) {
                            assert false : e;
                        }
                        return null; // 没有返回值
                    }
                });

        // (version >= 6.0) 意味着 Vista 或更高版本。
        if (version >= 6.0 && !preferIPv4Stack) {
            useDualStackImplLocal = true;
        }
        if (exclBindProp != null) {
            // sun.net.useExclusiveBind 为真
            exclusiveBindLocal = exclBindProp.length() == 0 ? true
                    : Boolean.parseBoolean(exclBindProp);
        } else if (version < 6.0) {
            exclusiveBindLocal = false;
        }

        // impl.prefix
        String prefix = null;
        try {
            prefix = AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("impl.prefix", null));
            if (prefix != null)
                prefixImplClassLocal = Class.forName("java.net."+prefix+"DatagramSocketImpl");
        } catch (Exception e) {
            System.err.println("找不到类: java.net." +
                                prefix +
                                "DatagramSocketImpl: 检查 impl.prefix 属性");
        }

        prefixImplClass = prefixImplClassLocal;
        useDualStackImpl = useDualStackImplLocal;
        exclusiveBind = exclusiveBindLocal;
    }

    /**
     * 创建一个新的 <code>DatagramSocketImpl</code> 实例。
     *
     * @param   isMulticast 如果此实现将用于 MutlicastSocket，则为 true
     * @return  一个新的 <code>PlainDatagramSocketImpl</code> 实例。
     */
    static DatagramSocketImpl createDatagramSocketImpl(boolean isMulticast)
        throws SocketException {
        if (prefixImplClass != null) {
            try {
                return (DatagramSocketImpl) prefixImplClass.newInstance();
            } catch (Exception e) {
                throw new SocketException("无法实例化 DatagramSocketImpl");
            }
        } else {
            if (useDualStackImpl && !isMulticast)
                return new DualStackPlainDatagramSocketImpl(exclusiveBind);
            else
                return new TwoStacksPlainDatagramSocketImpl(exclusiveBind && !isMulticast);
        }
    }
}
