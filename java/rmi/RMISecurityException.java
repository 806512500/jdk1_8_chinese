/*
 * 版权所有 (c) 1996, 2004, Oracle 和/或其附属公司。保留所有权利。
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
package java.rmi;

/**
 * <code>RMISecurityException</code> 表示在执行 <code>java.rmi.RMISecurityManager</code> 的方法之一时发生了安全异常。
 *
 * @author  Roger Riggs
 * @since   JDK1.1
 * @deprecated 使用 {@link java.lang.SecurityException} 代替。
 * 应用程序代码不应直接引用此类，<code>RMISecurityManager</code> 不再抛出这个 <code>java.lang.SecurityException</code> 的子类。
 */
@Deprecated
public class RMISecurityException extends java.lang.SecurityException {

    /* 表示与 JDK 1.1.x 版本类的兼容性 */
     private static final long serialVersionUID = -8433406075740433514L;

    /**
     * 使用详细消息构造 <code>RMISecurityException</code>。
     * @param name 详细消息
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    public RMISecurityException(String name) {
        super(name);
    }

    /**
     * 使用详细消息构造 <code>RMISecurityException</code>。
     * @param name 详细消息
     * @param arg 被忽略
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    public RMISecurityException(String name, String arg) {
        this(name);
    }
}
