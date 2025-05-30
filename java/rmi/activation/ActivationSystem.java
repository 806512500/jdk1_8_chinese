/*
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.activation;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.activation.UnknownGroupException;
import java.rmi.activation.UnknownObjectException;

/**
 * <code>ActivationSystem</code> 提供了一种注册组和“可激活”对象的方法，这些对象将在这些组中被激活。
 * <code>ActivationSystem</code> 与 <code>Activator</code> 密切合作，后者激活通过 <code>ActivationSystem</code> 注册的对象，
 * 以及 <code>ActivationMonitor</code>，后者获取活动和非活动对象以及非活动组的信息。
 *
 * @author      Ann Wollrath
 * @see         Activator
 * @see         ActivationMonitor
 * @since       1.2
 */
public interface ActivationSystem extends Remote {

    /** 用于查找激活系统的端口。 */
    public static final int SYSTEM_PORT = 1098;

    /**
     * <code>registerObject</code> 方法用于注册一个激活描述符 <code>desc</code>，并获取一个激活标识符，
     * 用于激活一个可激活的远程对象。<code>ActivationSystem</code> 为描述符 <code>desc</code> 指定的对象创建一个
     * <code>ActivationID</code>（激活标识符），并在稳定存储中记录该激活描述符及其关联的标识符，以便后续使用。
     * 当 <code>Activator</code> 收到特定标识符的 <code>activate</code> 请求时，它会查找之前注册的激活描述符，
     * 并使用该信息激活对象。 <p>
     *
     * @param desc 对象的激活描述符
     * @return 可用于激活对象的激活标识符
     * @exception ActivationException 如果注册失败（例如，数据库更新失败等）。
     * @exception UnknownGroupException 如果 <code>desc</code> 中引用的组未在此系统中注册
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public ActivationID registerObject(ActivationDesc desc)
        throws ActivationException, UnknownGroupException, RemoteException;

    /**
     * 从 <code>ActivationSystem</code> 中移除之前注册的激活标识符及其关联的描述符；对象将无法再通过其激活标识符被激活。
     *
     * @param id 对象的激活标识符（来自之前的注册）
     * @exception ActivationException 如果取消注册失败（例如，数据库更新失败等）。
     * @exception UnknownObjectException 如果对象未知（未注册）
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public void unregisterObject(ActivationID id)
        throws ActivationException, UnknownObjectException, RemoteException;

    /**
     * 注册激活组。激活组必须先在 <code>ActivationSystem</code> 中注册，然后才能在此组中注册对象。
     *
     * @param desc 组的描述符
     * @return 组的标识符
     * @exception ActivationException 如果组注册失败
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public ActivationGroupID registerGroup(ActivationGroupDesc desc)
        throws ActivationException, RemoteException;

    /**
     * 回调以通知激活系统组现在处于活动状态。此调用由 <code>ActivationGroup.createGroup</code> 方法内部调用，
     * 以通知 <code>ActivationSystem</code> 组现在处于活动状态。
     *
     * @param id 组的激活标识符
     * @param group 组的实例化器
     * @param incarnation 组的化身编号
     * @return 组的激活监控器
     * @exception UnknownGroupException 如果组未注册
     * @exception ActivationException 如果指定的 <code>id</code> 组已经处于活动状态且该组不等于指定的 <code>group</code>
     * 或该组具有不同的 <code>incarnation</code> 编号
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public ActivationMonitor activeGroup(ActivationGroupID id,
                                         ActivationInstantiator group,
                                         long incarnation)
        throws UnknownGroupException, ActivationException, RemoteException;

    /**
     * 移除激活组。激活组调用此回调以通知激活器组应被移除（销毁）。如果此调用成功完成，将无法再在此组中注册或激活对象。
     * 系统中将移除组及其关联对象的所有信息。
     *
     * @param id 组的激活标识符
     * @exception ActivationException 如果取消注册失败（例如，数据库更新失败等）。
     * @exception UnknownGroupException 如果组未注册
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public void unregisterGroup(ActivationGroupID id)
        throws ActivationException, UnknownGroupException, RemoteException;

    /**
     * 关闭激活系统。销毁由激活守护进程生成的所有组，并退出激活守护进程。
     * @exception RemoteException 如果无法联系/关闭激活守护进程
     * @since 1.2
     */
    public void shutdown() throws RemoteException;

    /**
     * 为具有激活标识符 <code>id</code> 的对象设置激活描述符 <code>desc</code>。更改将在对象的后续激活中生效。
     *
     * @param id 可激活对象的激活标识符
     * @param desc 可激活对象的激活描述符
     * @exception UnknownGroupException 与 <code>desc</code> 关联的组不是已注册的组
     * @exception UnknownObjectException 激活 <code>id</code> 未注册
     * @exception ActivationException 一般性失败（例如，无法更新日志）
     * @exception RemoteException 如果远程调用失败
     * @return 激活描述符的先前值
     * @see #getActivationDesc
     * @since 1.2
     */
    public ActivationDesc setActivationDesc(ActivationID id,
                                            ActivationDesc desc)
        throws ActivationException, UnknownObjectException,
            UnknownGroupException, RemoteException;

    /**
     * 为具有激活组标识符 <code>id</code> 的对象设置激活组描述符 <code>desc</code>。更改将在组的后续激活中生效。
     *
     * @param id 激活组的激活组标识符
     * @param desc 激活组的激活组描述符
     * @exception UnknownGroupException 与 <code>id</code> 关联的组不是已注册的组
     * @exception ActivationException 一般性失败（例如，无法更新日志）
     * @exception RemoteException 如果远程调用失败
     * @return 激活组描述符的先前值
     * @see #getActivationGroupDesc
     * @since 1.2
     */
    public ActivationGroupDesc setActivationGroupDesc(ActivationGroupID id,
                                                      ActivationGroupDesc desc)
       throws ActivationException, UnknownGroupException, RemoteException;

    /**
     * 返回具有激活标识符 <code>id</code> 的对象的激活描述符。
     *
     * @param id 可激活对象的激活标识符
     * @exception UnknownObjectException 如果 <code>id</code> 未注册
     * @exception ActivationException 一般性失败
     * @exception RemoteException 如果远程调用失败
     * @return 激活描述符
     * @see #setActivationDesc
     * @since 1.2
     */
    public ActivationDesc getActivationDesc(ActivationID id)
       throws ActivationException, UnknownObjectException, RemoteException;

    /**
     * 返回具有激活组标识符 <code>id</code> 的组的激活组描述符。
     *
     * @param id 组的激活组标识符
     * @exception UnknownGroupException 如果 <code>id</code> 未注册
     * @exception ActivationException 一般性失败
     * @exception RemoteException 如果远程调用失败
     * @return 激活组描述符
     * @see #setActivationGroupDesc
     * @since 1.2
     */
    public ActivationGroupDesc getActivationGroupDesc(ActivationGroupID id)
       throws ActivationException, UnknownGroupException, RemoteException;
}
