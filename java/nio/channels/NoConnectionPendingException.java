/*
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 *
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
 *
 */

// -- This file was mechanically generated: Do not edit! -- //

package java.nio.channels;


/**
 * 当调用 {@link SocketChannel#finishConnect finishConnect} 方法时，如果之前没有成功调用 {@link SocketChannel#connect connect} 方法，将抛出此未检查异常。
 *
 * @since 1.4
 */

public class NoConnectionPendingException
    extends IllegalStateException
{

    private static final long serialVersionUID = -8296561183633134743L;

    /**
     * 构造此类的一个实例。
     */
    public NoConnectionPendingException() { }

}
