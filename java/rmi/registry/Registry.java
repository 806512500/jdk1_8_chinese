/*
 * Copyright (c) 1996, 2001, Oracle and/or its affiliates. All rights reserved.
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
package java.rmi.registry;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * <code>Registry</code> 是一个远程接口，提供了一种简单的方法来存储和检索与任意字符串名称绑定的远程对象引用。方法 <code>bind</code>、<code>unbind</code> 和 <code>rebind</code>
 * 用于更改注册表中的名称绑定，而方法 <code>lookup</code> 和 <code>list</code> 用于查询当前的名称绑定。
 *
 * <p>在典型使用中，<code>Registry</code> 使 RMI 客户端能够启动：它提供了一种简单的方法让客户端获取初始的远程对象引用。因此，注册表的远程对象实现通常使用一个众所周知的地址导出，例如使用一个众所周知的 {@link
 * java.rmi.server.ObjID#REGISTRY_ID ObjID} 和 TCP 端口号（默认是 {@link #REGISTRY_PORT 1099}）。
 *
 * <p>{@link LocateRegistry} 类提供了一个编程 API，用于在远程地址构建一个 <code>Registry</code> 的启动引用（参见静态 <code>getRegistry</code> 方法）和在当前 VM 的特定本地地址创建和导出一个 <code>Registry</code>（参见静态
 * <code>createRegistry</code> 方法）。
 *
 * <p><code>Registry</code> 实现可以选择限制对某些或所有方法的访问（例如，更改注册表绑定的方法可能限制来自本地主机之外的调用）。如果 <code>Registry</code> 方法选择拒绝给定调用的访问，其实现可能会抛出 {@link java.rmi.AccessException}，这（因为它扩展了 {@link java.rmi.RemoteException}）将被远程客户端捕获时包装在 {@link java.rmi.ServerException} 中。
 *
 * <p>在 <code>Registry</code> 中用于绑定的名称是纯字符串，不解析。存储其远程引用的服务可能希望在名称绑定中使用包名作为前缀，以减少注册表中名称冲突的可能性。
 *
 * @author      Ann Wollrath
 * @author      Peter Jones
 * @since       JDK1.1
 * @see         LocateRegistry
 */
public interface Registry extends Remote {

    /** 注册表的知名端口。 */
    public static final int REGISTRY_PORT = 1099;

    /**
     * 返回在此注册表中绑定到指定 <code>name</code> 的远程引用。
     *
     * @param   name 要查找的远程引用的名称
     *
     * @return  一个远程对象的引用
     *
     * @throws  NotBoundException 如果 <code>name</code> 当前未绑定
     *
     * @throws  RemoteException 如果与注册表的远程通信失败；如果异常是一个 <code>ServerException</code>
     * 包含一个 <code>AccessException</code>，则注册表拒绝调用者执行此操作
     *
     * @throws  AccessException 如果此注册表是本地的并且拒绝调用者执行此操作
     *
     * @throws  NullPointerException 如果 <code>name</code> 为 <code>null</code>
     */
    public Remote lookup(String name)
        throws RemoteException, NotBoundException, AccessException;

    /**
     * 在此注册表中将远程引用绑定到指定的 <code>name</code>。
     *
     * @param   name 要与远程引用关联的名称
     * @param   obj 远程对象的引用（通常是存根）
     *
     * @throws  AlreadyBoundException 如果 <code>name</code> 已经绑定
     *
     * @throws  RemoteException 如果与注册表的远程通信失败；如果异常是一个 <code>ServerException</code>
     * 包含一个 <code>AccessException</code>，则注册表拒绝调用者执行此操作（如果调用来自非本地主机，例如）
     *
     * @throws  AccessException 如果此注册表是本地的并且拒绝调用者执行此操作
     *
     * @throws  NullPointerException 如果 <code>name</code> 为 <code>null</code>，或者 <code>obj</code> 为 <code>null</code>
     */
    public void bind(String name, Remote obj)
        throws RemoteException, AlreadyBoundException, AccessException;

    /**
     * 在此注册表中移除指定 <code>name</code> 的绑定。
     *
     * @param   name 要移除的绑定的名称
     *
     * @throws  NotBoundException 如果 <code>name</code> 当前未绑定
     *
     * @throws  RemoteException 如果与注册表的远程通信失败；如果异常是一个 <code>ServerException</code>
     * 包含一个 <code>AccessException</code>，则注册表拒绝调用者执行此操作（如果调用来自非本地主机，例如）
     *
     * @throws  AccessException 如果此注册表是本地的并且拒绝调用者执行此操作
     *
     * @throws  NullPointerException 如果 <code>name</code> 为 <code>null</code>
     */
    public void unbind(String name)
        throws RemoteException, NotBoundException, AccessException;

    /**
     * 在此注册表中用提供的远程引用替换指定 <code>name</code> 的绑定。如果指定 <code>name</code> 已存在绑定，则该绑定将被丢弃。
     *
     * @param   name 要与远程引用关联的名称
     * @param   obj 远程对象的引用（通常是存根）
     *
     * @throws  RemoteException 如果与注册表的远程通信失败；如果异常是一个 <code>ServerException</code>
     * 包含一个 <code>AccessException</code>，则注册表拒绝调用者执行此操作（如果调用来自非本地主机，例如）
     *
     * @throws  AccessException 如果此注册表是本地的并且拒绝调用者执行此操作
     *
     * @throws  NullPointerException 如果 <code>name</code> 为 <code>null</code>，或者 <code>obj</code> 为 <code>null</code>
     */
    public void rebind(String name, Remote obj)
        throws RemoteException, AccessException;

    /**
     * 返回在此注册表中绑定的名称数组。该数组将包含在调用此方法时此注册表中绑定的名称的快照。
     *
     * @return  在此注册表中绑定的名称数组
     *
     * @throws  RemoteException 如果与注册表的远程通信失败；如果异常是一个 <code>ServerException</code>
     * 包含一个 <code>AccessException</code>，则注册表拒绝调用者执行此操作
     *
     * @throws  AccessException 如果此注册表是本地的并且拒绝调用者执行此操作
     */
    public String[] list() throws RemoteException, AccessException;
}
