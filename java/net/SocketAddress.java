/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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
 *
 * 此类表示没有协议关联的套接字地址。
 * 作为一个抽象类，它旨在被特定的、依赖于协议的实现子类化。
 * <p>
 * 它提供了一个不可变的对象，用于套接字的绑定、连接或作为返回值。
 *
 * @see java.net.Socket
 * @see java.net.ServerSocket
 * @since 1.4
 */
public abstract class SocketAddress implements java.io.Serializable {

    static final long serialVersionUID = 5215720748342549866L;

}
