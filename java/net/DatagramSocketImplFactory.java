/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 该接口定义了数据报套接字实现的工厂。它被 {@code DatagramSocket} 类用于创建实际的套接字实现。
 *
 * @author  Yingxian Wang
 * @see     java.net.DatagramSocket
 * @since   1.3
 */
public
interface DatagramSocketImplFactory {
    /**
     * 创建一个新的 {@code DatagramSocketImpl} 实例。
     *
     * @return  一个新的 {@code DatagramSocketImpl} 实例。
     * @see     java.net.DatagramSocketImpl
     */
    DatagramSocketImpl createDatagramSocketImpl();
}
