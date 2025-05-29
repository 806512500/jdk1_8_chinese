
/*
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.activation;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.activation.UnknownGroupException;
import java.rmi.activation.UnknownObjectException;

/**
 * <code>ActivationSystem</code> 提供了一种注册组和“可激活”对象的方法，这些对象可以在这些组内被激活。
 * <code>ActivationSystem</code> 与 <code>Activator</code> 紧密合作，<code>Activator</code> 通过 <code>ActivationSystem</code> 注册的对象进行激活，
 * 以及 <code>ActivationMonitor</code>，它获取关于活动和非活动对象以及非活动组的信息。
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
     * <code>registerObject</code> 方法用于注册一个激活描述符 <code>desc</code>，并为可激活的远程对象获取一个激活标识符。
     * <code>ActivationSystem</code> 为描述符 <code>desc</code> 指定的对象创建一个 <code>ActivationID</code>（激活标识符），
     * 并在稳定存储中记录激活描述符及其关联的标识符，以便以后使用。当 <code>Activator</code> 收到特定标识符的 <code>activate</code> 请求时，
     * 它查找先前注册的指定标识符的激活描述符，并使用该信息激活对象。 <p>
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
     * 从 <code>ActivationSystem</code> 中移除先前注册的激活标识符及其关联的描述符；对象将不能再通过对象的激活标识符被激活。
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
     * 注册激活组。必须先将激活组注册到 <code>ActivationSystem</code>，然后才能在该组内注册对象。
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
     * 回调通知激活系统组现在处于活动状态。此调用由 <code>ActivationGroup.createGroup</code> 方法内部进行，
     * 以通知 <code>ActivationSystem</code> 组现在处于活动状态。
     *
     * @param id 激活组的标识符
     * @param group 组的实例化器
     * @param incarnation 组的化身编号
     * @return 激活组的监视器
     * @exception UnknownGroupException 如果组未注册
     * @exception ActivationException 如果指定 <code>id</code> 的组已处于活动状态且该组不等于指定的 <code>group</code> 或该组具有与指定 <code>group</code> 不同的 <code>incarnation</code>
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public ActivationMonitor activeGroup(ActivationGroupID id,
                                         ActivationInstantiator group,
                                         long incarnation)
        throws UnknownGroupException, ActivationException, RemoteException;

    /**
     * 移除激活组。激活组调用此回调通知激活器应移除（销毁）该组。如果此调用成功完成，则不能再在组内注册或激活对象。
     * 组及其关联对象的所有信息将从系统中移除。
     *
     * @param id 激活组的标识符
     * @exception ActivationException 如果取消注册失败（例如，数据库更新失败等）。
     * @exception UnknownGroupException 如果组未注册
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public void unregisterGroup(ActivationGroupID id)
        throws ActivationException, UnknownGroupException, RemoteException;

    /**
     * 关闭激活系统。销毁激活守护程序生成的所有组并退出激活守护程序。
     * @exception RemoteException 如果无法联系/关闭激活守护程序
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
     * @exception ActivationException 一般失败（例如，无法更新日志）
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
     * 设置具有激活组标识符 <code>id</code> 的对象的激活组描述符 <code>desc</code>。更改将在组的后续激活时生效。
     *
     * @param id 激活组的激活组标识符
     * @param desc 激活组的激活组描述符
     * @exception UnknownGroupException 与 <code>id</code> 关联的组不是已注册的组
     * @exception ActivationException 一般失败（例如，无法更新日志）
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
     * @exception ActivationException 一般失败
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
     * @exception ActivationException 一般失败
     * @exception RemoteException 如果远程调用失败
     * @return 激活组描述符
     * @see #setActivationGroupDesc
     * @since 1.2
     */
    public ActivationGroupDesc getActivationGroupDesc(ActivationGroupID id)
       throws ActivationException, UnknownGroupException, RemoteException;
}
