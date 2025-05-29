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
 * 一个 <code>RMIFailureHandler</code> 可以通过调用
 * <code>RMISocketFactory.setFailureHandler</code> 注册。当 RMI
 * 运行时无法创建一个 <code>ServerSocket</code> 以监听传入的调用时，
 * 处理程序的 <code>failure</code> 方法将被调用。<code>failure</code> 方法返回一个布尔值，
 * 表示运行时是否应尝试重新创建 <code>ServerSocket</code>。
 *
 * @author      Ann Wollrath
 * @since       JDK1.1
 */
public interface RMIFailureHandler {

    /**
     * 当 RMI 运行时无法通过 <code>RMISocketFactory</code> 创建一个 <code>ServerSocket</code> 时，
     * <code>failure</code> 回调将被调用。通过调用
     * <code>RMISocketFacotry.setFailureHandler</code> 注册一个 <code>RMIFailureHandler</code>。
     * 如果没有安装故障处理程序，默认行为是尝试重新创建 ServerSocket。
     *
     * @param ex 在 <code>ServerSocket</code> 创建期间发生的异常
     * @return 如果为 true，RMI 运行时将尝试重新创建 <code>ServerSocket</code>
     * @see java.rmi.server.RMISocketFactory#setFailureHandler(RMIFailureHandler)
     * @since JDK1.1
     */
    public boolean failure(Exception ex);

}
