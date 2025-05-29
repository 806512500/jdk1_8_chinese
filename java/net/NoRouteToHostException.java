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
 * 表示在尝试将套接字连接到远程地址和端口时发生错误。通常，由于中间的防火墙或中间路由器故障，无法到达远程主机。
 *
 * @since   JDK1.1
 */
public class NoRouteToHostException extends SocketException {
    private static final long serialVersionUID = -1897550894873493790L;

    /**
     * 使用指定的详细消息构造一个新的 NoRouteToHostException，说明为什么无法到达远程主机。
     * 详细消息是一个字符串，提供此错误的具体描述。
     * @param msg 详细消息
     */
    public NoRouteToHostException(String msg) {
        super(msg);
    }

    /**
     * 构造一个新的 NoRouteToHostException，不带详细消息。
     */
    public NoRouteToHostException() {}
}
