/*
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
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

import java.rmi.RemoteException;

/**
 * 当接收到的调用与可用的骨架不匹配时，将抛出此异常。这表明此接口中的远程方法名称或签名已更改，
 * 或者用于发起调用的存根类和接收调用的骨架不是由同一版本的存根编译器（<code>rmic</code>）生成的。
 *
 * @author  Roger Riggs
 * @since   JDK1.1
 * @deprecated 没有替代方案。从 Java 2 平台 v1.2 及更高版本开始，远程方法调用不再需要骨架。
 */
@Deprecated
public class SkeletonMismatchException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = -7780460454818859281L;

    /**
     * 使用指定的详细消息构造新的 <code>SkeletonMismatchException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     * @deprecated 没有替代方案
     */
    @Deprecated
    public SkeletonMismatchException(String s) {
        super(s);
    }

}
