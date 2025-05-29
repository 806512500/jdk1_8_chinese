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
 * 此接口定义了 {@code URL} 流协议处理程序的工厂。
 * <p>
 * 它由 {@code URL} 类用于为特定协议创建一个
 * {@code URLStreamHandler}。
 *
 * @author  Arthur van Hoff
 * @see     java.net.URL
 * @see     java.net.URLStreamHandler
 * @since   JDK1.0
 */
public interface URLStreamHandlerFactory {
    /**
     * 使用指定的协议创建一个新的 {@code URLStreamHandler} 实例。
     *
     * @param   protocol   协议 ("{@code ftp}",
     *                     "{@code http}", "{@code nntp}", 等)。
     * @return  为特定协议创建的 {@code URLStreamHandler}。
     * @see     java.net.URLStreamHandler
     */
    URLStreamHandler createURLStreamHandler(String protocol);
}
