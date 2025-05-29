/*
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.server;

/**
 * <code>ExportException</code> 是一个 <code>RemoteException</code>，
 * 当尝试导出远程对象失败时抛出。远程对象通过
 * <code>java.rmi.server.UnicastRemoteObject</code> 和
 * <code>java.rmi.activation.Activatable</code> 的构造函数和 <code>exportObject</code> 方法导出。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @see java.rmi.server.UnicastRemoteObject
 * @see java.rmi.activation.Activatable
 */
public class ExportException extends java.rmi.RemoteException {

    /* 表示与 JDK 1.1.x 版本类的兼容性 */
    private static final long serialVersionUID = -9155485338494060170L;

    /**
     * 使用指定的详细消息构造 <code>ExportException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     */
    public ExportException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>ExportException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public ExportException(String s, Exception ex) {
        super(s, ex);
    }

}
