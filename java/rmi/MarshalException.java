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

package java.rmi;

/**
 * 如果在远程方法调用时发生 <code>java.io.IOException</code>，则抛出 <code>MarshalException</code>，
 * 该异常发生在序列化远程调用头、参数或返回值时。如果接收方不支持发送方的协议版本，也会抛出 <code>MarshalException</code>。
 *
 * <p>如果在远程方法调用期间发生 <code>MarshalException</code>，调用可能已到达服务器，也可能未到达。
 * 如果调用已到达服务器，参数可能已被反序列化。在发生 <code>MarshalException</code> 后，不能重新传输调用并可靠地保持“最多一次”调用语义。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 */
public class MarshalException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = 6223554758134037936L;

    /**
     * 使用指定的详细消息构造 <code>MarshalException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     */
    public MarshalException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>MarshalException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public MarshalException(String s, Exception ex) {
        super(s, ex);
    }
}
