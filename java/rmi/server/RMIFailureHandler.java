/*
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.rmi.server;

/**
 * 一个 <code>RMIFailureHandler</code> 可以通过调用
 * <code>RMISocketFactory.setFailureHandler</code> 注册。当 RMI
 * 运行时无法创建一个 <code>ServerSocket</code> 以监听
 * 进来的调用时，处理程序的 <code>failure</code> 方法将被调用。
 * <code>failure</code> 方法返回一个布尔值，指示运行时是否应尝试重新创建
 * <code>ServerSocket</code>。
 *
 * @author      Ann Wollrath
 * @since       JDK1.1
 */
public interface RMIFailureHandler {

    /**
     * 当 RMI 运行时无法通过 <code>RMISocketFactory</code> 创建一个 <code>ServerSocket</code>
     * 时，<code>failure</code> 回调将被调用。通过调用
     * <code>RMISocketFacotry.setFailureHandler</code> 注册一个 <code>RMIFailureHandler</code>。
     * 如果没有安装失败处理程序，默认行为是尝试重新创建 ServerSocket。
     *
     * @param ex 在 <code>ServerSocket</code> 创建期间发生的异常
     * @return 如果为 true，RMI 运行时将尝试重新创建 <code>ServerSocket</code>
     * @see java.rmi.server.RMISocketFactory#setFailureHandler(RMIFailureHandler)
     * @since JDK1.1
     */
    public boolean failure(Exception ex);

}
