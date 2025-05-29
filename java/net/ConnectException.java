/*
 * Copyright (c) 1996, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * 表示在尝试将套接字连接到远程地址和端口时发生错误。通常，连接被远程拒绝（例如，没有进程在远程地址/端口上监听）。
 *
 * @since   JDK1.1
 */
public class ConnectException extends SocketException {
    private static final long serialVersionUID = 3831404271622369215L;

    /**
     * 使用指定的详细消息构造一个新的 ConnectException，说明连接错误发生的原因。
     * 详细消息是一个描述此错误的具体原因的字符串。
     * @param msg 详细消息
     */
    public ConnectException(String msg) {
        super(msg);
    }

    /**
     * 构造一个没有详细消息的新 ConnectException。
     */
    public ConnectException() {}
}
