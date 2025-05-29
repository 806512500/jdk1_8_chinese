/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 与套接字关联的套接字选项。
 *
 * <p> 在 {@link java.nio.channels channels} 包中，{@link
 * java.nio.channels.NetworkChannel} 接口定义了 {@link
 * java.nio.channels.NetworkChannel#setOption(SocketOption,Object) setOption}
 * 和 {@link java.nio.channels.NetworkChannel#getOption(SocketOption) getOption}
 * 方法来设置和查询通道的套接字选项。
 *
 * @param   <T>     套接字选项值的类型。
 *
 * @since 1.7
 *
 * @see StandardSocketOptions
 */

public interface SocketOption<T> {

    /**
     * 返回套接字选项的名称。
     *
     * @return 套接字选项的名称
     */
    String name();

    /**
     * 返回套接字选项值的类型。
     *
     * @return 套接字选项值的类型
     */
    Class<T> type();
}
