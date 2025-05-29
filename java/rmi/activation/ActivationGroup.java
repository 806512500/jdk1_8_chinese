
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.MarshalledObject;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.activation.UnknownGroupException;
import java.rmi.activation.UnknownObjectException;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessController;
import sun.security.action.GetIntegerAction;

/**
 * 一个 <code>ActivationGroup</code> 负责在其组中创建新的“可激活”对象实例，并在其对象变为活动或非活动状态，或整个组变为非活动状态时通知其 <code>ActivationMonitor</code>。 <p>
 *
 * 一个 <code>ActivationGroup</code> <i>最初</i> 可以通过以下几种方式之一创建： <ul>
 * <li>作为创建没有显式 <code>ActivationGroupID</code> 的 <code>ActivationDesc</code> 的副作用，用于组中的第一个可激活对象，或
 * <li>通过 <code>ActivationGroup.createGroup</code> 方法
 * <li>作为激活组中第一个对象的副作用，该组的 <code>ActivationGroupDesc</code> 仅注册。</ul><p>
 *
 * 只有激活器可以 <i>重新创建</i> 一个 <code>ActivationGroup</code>。激活器根据需要生成一个单独的 VM（例如，作为子进程）为每个注册的激活组，并将激活请求定向到适当的组。VM 的生成方式是实现特定的。通过 <code>ActivationGroup.createGroup</code> 静态方法创建激活组。<code>createGroup</code> 方法对要创建的组有两个要求：1) 组必须是 <code>ActivationGroup</code> 的具体子类，2) 组必须有一个接受两个参数的构造函数：
 *
 * <ul>
 * <li> 组的 <code>ActivationGroupID</code>，和
 * <li> 组的初始化数据（在 <code>java.rmi.MarshalledObject</code> 中）</ul><p>
 *
 * 创建时，默认的 <code>ActivationGroup</code> 实现将使用创建 <code>ActivationGroupDesc</code> 时请求的属性覆盖系统属性，并将 {@link SecurityManager} 设置为默认的系统安全管理器。如果您的应用程序在组中激活对象时需要设置特定的属性，应用程序应创建一个包含这些属性的特殊 <code>Properties</code> 对象，然后使用该 <code>Properties</code> 对象创建一个 <code>ActivationGroupDesc</code>，并在创建任何 <code>ActivationDesc</code>s 之前（在创建默认的 <code>ActivationGroupDesc</code> 之前）使用 <code>ActivationGroup.createGroup</code>。如果您的应用程序需要使用不同于 {@link SecurityManager} 的安全管理器，可以在 ActivativationGroupDescriptor 属性列表中设置 <code>java.security.manager</code> 属性为要安装的安全管理器的名称。
 *
 * @author      Ann Wollrath
 * @see         ActivationInstantiator
 * @see         ActivationGroupDesc
 * @see         ActivationGroupID
 * @since       1.2
 */
public abstract class ActivationGroup
        extends UnicastRemoteObject
        implements ActivationInstantiator
{
    /**
     * @serial 组的标识符
     */
    private ActivationGroupID groupID;

    /**
     * @serial 组的监视器
     */
    private ActivationMonitor monitor;

    /**
     * @serial 组的化身编号
     */
    private long incarnation;

    /** 当前 VM 的激活组 */
    private static ActivationGroup currGroup;
    /** 当前组的标识符 */
    private static ActivationGroupID currGroupID;
    /** 当前组的激活系统 */
    private static ActivationSystem currSystem;
    /** 用于控制组仅创建一次 */
    private static boolean canCreate = true;

    /** 表示与 Java 2 SDK v1.2 版本的类兼容 */
    private static final long serialVersionUID = -7696947875314805420L;

    /**
     * 使用给定的激活组标识符构造一个激活组。该组作为 <code>java.rmi.server.UnicastRemoteObject</code> 导出。
     *
     * @param   groupID 组的标识符
     * @throws  RemoteException 如果此组无法导出
     * @throws  UnsupportedOperationException 如果此实现不支持激活
     * @since   1.2
     */
    protected ActivationGroup(ActivationGroupID groupID)
        throws RemoteException
    {
        // 调用超类构造函数以导出对象
        super();
        this.groupID = groupID;
    }

    /**
     * 组的 <code>inactiveObject</code> 方法通过调用 <code>Activatable.inactive</code> 方法间接调用。远程对象实现必须在对象停用时（对象认为自己不再活动时）调用 <code>Activatable</code> 的 <code>inactive</code> 方法。如果对象在停用时不调用 <code>Activatable.inactive</code>，对象将永远不会被垃圾回收，因为组对其创建的对象保持强引用。
     *
     * <p>组的 <code>inactiveObject</code> 方法从 RMI 运行时中取消导出远程对象，以便对象不再接收传入的 RMI 调用。只有当对象没有挂起或正在执行的调用时，对象才会被取消导出。<code>ActivationGroup</code> 的子类必须覆盖此方法并取消导出对象。
     *
     * <p>在从 RMI 运行时中移除对象后，组必须通过调用监视器的 <code>inactiveObject</code> 方法通知其 <code>ActivationMonitor</code>，告知远程对象当前不活动，以便在随后的激活请求中由激活器重新激活远程对象。
     *
     * <p>此方法仅通知组的监视器对象处于非活动状态。由 <code>ActivationGroup</code> 的具体子类来满足取消导出对象的额外要求。 <p>
     *
     * @param id 对象的激活标识符
     * @return 如果对象成功停用则返回 true；否则返回 false。
     * @exception UnknownObjectException 如果对象未知（可能已经处于非活动状态）
     * @exception RemoteException 如果通知监视器的调用失败
     * @exception ActivationException 如果组处于非活动状态
     * @since 1.2
     */
    public boolean inactiveObject(ActivationID id)
        throws ActivationException, UnknownObjectException, RemoteException
    {
        getMonitor().inactiveObject(id);
        return true;
    }


/**
 * 当对象被导出时（无论是通过 <code>Activatable</code> 对象构造还是显式调用
 * <code>Activatable.exportObject</code>），将调用组的 <code>activeObject</code> 方法。
 * 如果组尚未执行此操作，则组必须通过其 <code>ActivationMonitor</code> 的 <code>activeObject</code>
 * 方法通知监视器该对象处于活动状态。
 *
 * @param id 对象的标识符
 * @param obj 远程对象实现
 * @exception UnknownObjectException 如果对象未注册
 * @exception RemoteException 如果通知监视器的调用失败
 * @exception ActivationException 如果组处于非活动状态
 * @since 1.2
 */
public abstract void activeObject(ActivationID id, Remote obj)
    throws ActivationException, UnknownObjectException, RemoteException;

/**
 * 为当前 VM 创建并设置激活组。只有在当前未设置激活组时，才能设置激活组。
 * 当 <code>Activator</code> 初始化重新创建激活组以处理传入的 <code>activate</code> 请求时，
 * 会使用 <code>createGroup</code> 方法设置激活组。在通过此方法创建组之前，必须先将组注册到
 * <code>ActivationSystem</code>。
 *
 * <p><code>ActivationGroupDesc</code> 指定的组类必须是 <code>ActivationGroup</code> 的具体子类，
 * 并且必须具有一个公共构造函数，该构造函数接受两个参数：组的 <code>ActivationGroupID</code>
 * 和包含组初始化数据的 <code>MarshalledObject</code>（从 <code>ActivationGroupDesc</code> 获取）。
 *
 * <p>如果 <code>ActivationGroupDesc</code> 中指定的组类名称为 <code>null</code>，则此方法将
 * 行为如同组描述符包含默认激活组实现类的名称。
 *
 * <p>请注意，如果您的应用程序创建了自己的自定义激活组，则必须为此组设置安全经理。
 * 否则，无法在组中激活对象。默认情况下会设置 <code>SecurityManager</code>。
 *
 * <p>如果组 VM 中已经设置了安全经理，此方法首先调用安全经理的
 * <code>checkSetFactory</code> 方法。这可能会导致 <code>SecurityException</code>。
 * 如果您的应用程序需要设置不同的安全经理，您必须确保组的 <code>ActivationGroupDesc</code>
 * 指定的策略文件授予组设置新安全经理所需的权限。（注意：如果您的组下载并设置安全经理，这将是必要的）。
 *
 * <p>组创建后，通过调用 <code>activeGroup</code> 方法通知 <code>ActivationSystem</code>
 * 组处于活动状态，该方法返回组的 <code>ActivationMonitor</code>。应用程序无需独立调用
 * <code>activeGroup</code>，因为此方法会处理这一点。
 *
 * <p>组创建后，后续对 <code>currentGroupID</code> 方法的调用将返回此组的标识符，直到组变为非活动状态。
 *
 * @param id 激活组的标识符
 * @param desc 激活组的描述符
 * @param incarnation 组的实例化编号（在组的初始创建时为零）
 * @return VM 的激活组
 * @exception ActivationException 如果组已存在或在组创建过程中发生错误
 * @exception SecurityException 如果创建组的权限被拒绝。
 * （注意：安全经理 <code>checkSetFactory</code> 方法的默认实现需要
 * <code>RuntimePermission "setFactory"</code> 权限）
 * @exception UnsupportedOperationException 如果此实现不支持激活
 * @see SecurityManager#checkSetFactory
 * @since 1.2
 */
public static synchronized
    ActivationGroup createGroup(ActivationGroupID id,
                                final ActivationGroupDesc desc,
                                long incarnation)
    throws ActivationException
{
    SecurityManager security = System.getSecurityManager();
    if (security != null)
        security.checkSetFactory();

    if (currGroup != null)
        throw new ActivationException("group already exists");

    if (canCreate == false)
        throw new ActivationException("group deactivated and " +
                                      "cannot be recreated");

    try {
        // 加载组的类
        String groupClassName = desc.getClassName();
        Class<? extends ActivationGroup> cl;
        Class<? extends ActivationGroup> defaultGroupClass =
            sun.rmi.server.ActivationGroupImpl.class;
        if (groupClassName == null ||       // see 4252236
            groupClassName.equals(defaultGroupClass.getName()))
        {
            cl = defaultGroupClass;
        } else {
            Class<?> cl0;
            try {
                cl0 = RMIClassLoader.loadClass(desc.getLocation(),
                                               groupClassName);
            } catch (Exception ex) {
                throw new ActivationException(
                    "Could not load group implementation class", ex);
            }
            if (ActivationGroup.class.isAssignableFrom(cl0)) {
                cl = cl0.asSubclass(ActivationGroup.class);
            } else {
                throw new ActivationException("group not correct class: " +
                                              cl0.getName());
            }
        }

        // 创建组
        Constructor<? extends ActivationGroup> constructor =
            cl.getConstructor(ActivationGroupID.class,
                              MarshalledObject.class);
        ActivationGroup newGroup =
            constructor.newInstance(id, desc.getData());
        currSystem = id.getSystem();
        newGroup.incarnation = incarnation;
        newGroup.monitor =
            currSystem.activeGroup(id, newGroup, incarnation);
        currGroup = newGroup;
        currGroupID = id;
        canCreate = false;
    } catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
            throw new ActivationException("exception in group constructor",
                                          e.getTargetException());


                    } catch (ActivationException e) {
            throw e;

        } catch (Exception e) {
            throw new ActivationException("exception creating group", e);
        }

        return currGroup;
    }

    /**
     * 返回当前激活组的标识符。如果没有组在当前虚拟机中激活，则返回 null。
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @return 激活组的标识符
     * @since 1.2
     */
    public static synchronized ActivationGroupID currentGroupID() {
        return currGroupID;
    }

    /**
     * 返回虚拟机的激活组标识符。如果此虚拟机中不存在激活组，则创建一个默认的激活组。
     * 组只能创建一次，因此如果组已经激活并被停用，则不会再次创建。
     *
     * @return 激活组的标识符
     * @exception ActivationException 如果在组创建过程中发生错误，安全管理者未设置，或组已创建并被停用。
     */
    static synchronized ActivationGroupID internalCurrentGroupID()
        throws ActivationException
    {
        if (currGroupID == null)
            throw new ActivationException("nonexistent group");

        return currGroupID;
    }

    /**
     * 为虚拟机设置激活系统。只有在当前没有激活组时才能设置激活系统。
     * 如果未通过此调用设置激活系统，则 <code>getSystem</code> 方法会尝试通过在激活器的注册表中查找名称
     * "java.rmi.activation.ActivationSystem" 来获取 <code>ActivationSystem</code> 的引用。
     * 默认情况下，用于查找激活系统的端口号由 <code>ActivationSystem.SYSTEM_PORT</code> 定义。
     * 可以通过设置属性 <code>java.rmi.activation.port</code> 来覆盖此端口。
     *
     * <p>如果有安全管理者，此方法首先调用安全管理者的 <code>checkSetFactory</code> 方法。
     * 这可能会导致 SecurityException。
     *
     * @param system 远程引用到 <code>ActivationSystem</code>
     * @exception ActivationException 如果激活系统已设置
     * @exception SecurityException 如果设置激活系统的权限被拒绝。
     * （注意：安全管理者的 <code>checkSetFactory</code> 方法的默认实现需要 RuntimePermission "setFactory"）
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @see #getSystem
     * @see SecurityManager#checkSetFactory
     * @since 1.2
     */
    public static synchronized void setSystem(ActivationSystem system)
        throws ActivationException
    {
        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkSetFactory();

        if (currSystem != null)
            throw new ActivationException("activation system already set");

        currSystem = system;
    }

    /**
     * 返回虚拟机的激活系统。激活系统可以通过 <code>setSystem</code> 方法设置。
     * 如果未通过 <code>setSystem</code> 方法设置激活系统，则 <code>getSystem</code> 方法会尝试通过在激活器的注册表中查找名称
     * "java.rmi.activation.ActivationSystem" 来获取 <code>ActivationSystem</code> 的引用。
     * 默认情况下，用于查找激活系统的端口号由 <code>ActivationSystem.SYSTEM_PORT</code> 定义。
     * 可以通过设置属性 <code>java.rmi.activation.port</code> 来覆盖此端口。
     *
     * @return 虚拟机/组的激活系统
     * @exception ActivationException 如果无法获取激活系统或未绑定（意味着它未运行）
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @see #setSystem
     * @since 1.2
     */
    public static synchronized ActivationSystem getSystem()
        throws ActivationException
    {
        if (currSystem == null) {
            try {
                int port = AccessController.doPrivileged(
                    new GetIntegerAction("java.rmi.activation.port",
                                         ActivationSystem.SYSTEM_PORT));
                currSystem = (ActivationSystem)
                    Naming.lookup("//:" + port +
                                  "/java.rmi.activation.ActivationSystem");
            } catch (Exception e) {
                throw new ActivationException(
                    "unable to obtain ActivationSystem", e);
            }
        }
        return currSystem;
    }

    /**
     * 此受保护的方法对于子类向组的监视器发出 <code>activeObject</code> 回调是必要的。
     * 调用将直接转发给组的 <code>ActivationMonitor</code>。
     *
     * @param id 对象的标识符
     * @param mobj 包含远程对象存根的序列化对象
     * @exception UnknownObjectException 如果对象未注册
     * @exception RemoteException 如果通知监视器的调用失败
     * @exception ActivationException 如果发生激活错误
     * @since 1.2
     */
    protected void activeObject(ActivationID id,
                                MarshalledObject<? extends Remote> mobj)
        throws ActivationException, UnknownObjectException, RemoteException
    {
        getMonitor().activeObject(id, mobj);
    }

    /**
     * 此受保护的方法对于子类向组的监视器发出 <code>inactiveGroup</code> 回调是必要的。
     * 调用将直接转发给组的 <code>ActivationMonitor</code>。此外，虚拟机的当前组将被设置为 null。
     *
     * @exception UnknownGroupException 如果组未注册
     * @exception RemoteException 如果通知监视器的调用失败
     * @since 1.2
     */
    protected void inactiveGroup()
        throws UnknownGroupException, RemoteException
    {
        try {
            getMonitor().inactiveGroup(groupID, incarnation);
        } finally {
            destroyGroup();
        }
    }

                /**
     * 返回激活组的监视器。
     */
    private ActivationMonitor getMonitor() throws RemoteException {
        synchronized (ActivationGroup.class) {
            if (monitor != null) {
                return monitor;
            }
        }
        throw new RemoteException("monitor not received");
    }

    /**
     * 销毁当前组。
     */
    private static synchronized void destroyGroup() {
        currGroup = null;
        currGroupID = null;
        // 注意：不要将 currSystem 设置为 null，因为它可能需要被使用
    }

    /**
     * 返回 VM 的当前组。
     * @exception ActivationException 如果当前组为 null（未激活）
     */
    static synchronized ActivationGroup currentGroup()
        throws ActivationException
    {
        if (currGroup == null) {
            throw new ActivationException("group is not active");
        }
        return currGroup;
    }

}
