/*
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi;

/**
 * 从在 JDK 1.1 上运行的服务器执行时，当在服务器上处理调用时抛出 <code>RuntimeException</code>，
 * 无论是解组参数、执行远程方法本身还是封送返回值时，都会抛出 <code>ServerRuntimeException</code>。
 *
 * <code>ServerRuntimeException</code> 实例包含其原因的原始 <code>RuntimeException</code>。
 *
 * <p>从 Java 2 平台 v1.2 或更高版本执行的服务器不会抛出 <code>ServerRuntimeException</code>。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @deprecated 没有替代
 */
@Deprecated
public class ServerRuntimeException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本类的兼容性 */
    private static final long serialVersionUID = 7054464920481467219L;

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>ServerRuntimeException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @deprecated 没有替代
     * @since JDK1.1
     */
    @Deprecated
    public ServerRuntimeException(String s, Exception ex) {
        super(s, ex);
    }
}
