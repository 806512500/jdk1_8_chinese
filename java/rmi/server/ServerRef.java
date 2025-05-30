/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.rmi.*;

/**
 * ServerRef 表示远程对象实现的服务器端句柄。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @deprecated 没有替代品。此接口未使用且已过时。
 */
@Deprecated
public interface ServerRef extends RemoteRef {

    /** 表示与 JDK 1.1.x 版本类的兼容性。 */
    static final long serialVersionUID = -4557750989390278438L;

    /**
     * 为提供的远程对象创建客户端存根对象。
     * 如果调用成功完成，远程对象应能够接受来自客户端的传入调用。
     * @param obj 远程对象实现
     * @param data 导出对象所需的信息
     * @return 远程对象的存根
     * @exception RemoteException 如果尝试导出对象时发生异常（例如，找不到存根类）
     * @since JDK1.1
     */
    RemoteStub exportObject(Remote obj, Object data)
        throws RemoteException;

    /**
     * 返回当前客户端的主机名。当从一个正在处理远程方法调用的线程调用时，
     * 返回客户端的主机名。
     * @return 客户端的主机名
     * @exception ServerNotActiveException 如果在服务远程方法调用之外调用
     * @since JDK1.1
     */
    String getClientHost() throws ServerNotActiveException;
}
