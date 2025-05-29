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

package java.rmi.server;

/**
 * 远程对象实现应实现 <code>Unreferenced</code> 接口以在没有更多客户端引用该远程对象时接收通知。
 *
 * @author  Ann Wollrath
 * @author  Roger Riggs
 * @since   JDK1.1
 */
public interface Unreferenced {
    /**
     * 当 RMI 运行时确定客户端引用远程对象的引用列表变为空后，RMI 运行时会调用此方法。
     * @since JDK1.1
     */
    public void unreferenced();
}
