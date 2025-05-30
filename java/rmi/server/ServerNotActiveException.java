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
 * <code>ServerNotActiveException</code> 是一个在调用 <code>RemoteServer.getClientHost</code> 时抛出的 <code>Exception</code>，
 * 如果 <code>getClientHost</code> 方法在服务远程方法调用之外被调用，则会抛出此异常。
 *
 * @author  Roger Riggs
 * @since   JDK1.1
 * @see java.rmi.server.RemoteServer#getClientHost()
 */
public class ServerNotActiveException extends java.lang.Exception {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = 4687940720827538231L;

    /**
     * 构造一个没有指定详细消息的 <code>ServerNotActiveException</code>。
     * @since JDK1.1
     */
    public ServerNotActiveException() {}

    /**
     * 构造一个具有指定详细消息的 <code>ServerNotActiveException</code>。
     *
     * @param s 详细消息。
     * @since JDK1.1
     */
    public ServerNotActiveException(String s)
    {
        super(s);
    }
}
