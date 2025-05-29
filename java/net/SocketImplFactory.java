/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.net;

/**
 * 该接口定义了套接字实现的工厂。它
 * 由类 {@code Socket} 和
 * {@code ServerSocket} 用于创建实际的套接字
 * 实现。
 *
 * @author  Arthur van Hoff
 * @see     java.net.Socket
 * @see     java.net.ServerSocket
 * @since   JDK1.0
 */
public
interface SocketImplFactory {
    /**
     * 创建一个新的 {@code SocketImpl} 实例。
     *
     * @return  一个新的 {@code SocketImpl} 实例。
     * @see     java.net.SocketImpl
     */
    SocketImpl createSocketImpl();
}
