
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.rmi.MarshalledObject;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.activation.UnknownGroupException;
import java.rmi.activation.UnknownObjectException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteServer;
import sun.rmi.server.ActivatableServerRef;

/**
 * <code>Activatable</code> 类为需要持久访问时间的远程对象提供支持，并且这些对象可以由系统激活。
 *
 * <p>对于构造函数和静态 <code>exportObject</code> 方法，远程对象的存根是按照
 * {@link java.rmi.server.UnicastRemoteObject} 中描述的方式获取的。
 *
 * <p>显式序列化此类的实例将会失败。
 *
 * @author      Ann Wollrath
 * @since       1.2
 * @serial      exclude
 */
public abstract class Activatable extends RemoteServer {

    private ActivationID id;
    /** 表示与 Java 2 SDK v1.2 版本的类兼容 */
    private static final long serialVersionUID = -3120617863591563455L;

    /**
     * 通过注册此对象的激活描述符（指定位置、数据和重启模式）并使用指定端口导出此对象来构造可激活的远程对象。
     *
     * <p><strong>注意：</strong> 使用既注册又导出可激活远程对象的 <code>Activatable</code>
     * 构造函数是强烈不建议的，因为注册和导出远程对象的操作不是原子性的。相反，应用程序应分别注册激活描述符和导出远程对象，
     * 以便正确处理异常。
     *
     * <p>此方法调用 {@link
     * #exportObject(Remote,String,MarshalledObject,boolean,int)
     * exportObject} 方法，使用此对象以及指定的位置、数据、重启模式和端口。后续调用 {@link #getID}
     * 将返回从 <code>exportObject</code> 调用返回的激活标识符。
     *
     * @param location 对象的类位置
     * @param data 对象的初始化数据
     * @param port 对象导出的端口（如果 port=0，则使用匿名端口）
     * @param restart 如果为 true，当激活器重启或对象的激活组在意外崩溃后重启时，对象将重启（重新激活）；
     * 如果为 false，对象仅按需激活。指定 <code>restart</code> 为 <code>true</code> 不会强制立即激活新注册的对象；
     * 初始激活是懒惰的。
     * @exception ActivationException 如果对象注册失败。
     * @exception RemoteException 如果以下任一操作失败：
     * a) 在激活系统中注册对象或 b) 将对象导出到 RMI 运行时。
     * @exception UnsupportedOperationException 如果此实现不支持激活。
     * @since 1.2
     **/
    protected Activatable(String location,
                          MarshalledObject<?> data,
                          boolean restart,
                          int port)
        throws ActivationException, RemoteException
    {
        super();
        id = exportObject(this, location, data, restart, port);
    }

    /**
     * 通过注册此对象的激活描述符（指定位置、数据和重启模式）并使用指定端口和指定的客户端和服务器套接字工厂导出此对象来构造可激活的远程对象。
     *
     * <p><strong>注意：</strong> 使用既注册又导出可激活远程对象的 <code>Activatable</code>
     * 构造函数是强烈不建议的，因为注册和导出远程对象的操作不是原子性的。相反，应用程序应分别注册激活描述符和导出远程对象，
     * 以便正确处理异常。
     *
     * <p>此方法调用 {@link
     * #exportObject(Remote,String,MarshalledObject,boolean,int,RMIClientSocketFactory,RMIServerSocketFactory)
     * exportObject} 方法，使用此对象以及指定的位置、数据、重启模式、端口和客户端和服务器套接字工厂。后续调用 {@link #getID}
     * 将返回从 <code>exportObject</code> 调用返回的激活标识符。
     *
     * @param location 对象的类位置
     * @param data 对象的初始化数据
     * @param restart 如果为 true，当激活器重启或对象的激活组在意外崩溃后重启时，对象将重启（重新激活）；
     * 如果为 false，对象仅按需激活。指定 <code>restart</code> 为 <code>true</code> 不会强制立即激活新注册的对象；
     * 初始激活是懒惰的。
     * @param port 对象导出的端口（如果 port=0，则使用匿名端口）
     * @param csf 用于调用远程对象的客户端套接字工厂
     * @param ssf 用于接收远程调用的服务器端套接字工厂
     * @exception ActivationException 如果对象注册失败。
     * @exception RemoteException 如果以下任一操作失败：
     * a) 在激活系统中注册对象或 b) 将对象导出到 RMI 运行时。
     * @exception UnsupportedOperationException 如果此实现不支持激活。
     * @since 1.2
     **/
    protected Activatable(String location,
                          MarshalledObject<?> data,
                          boolean restart,
                          int port,
                          RMIClientSocketFactory csf,
                          RMIServerSocketFactory ssf)
        throws ActivationException, RemoteException
    {
        super();
        id = exportObject(this, location, data, restart, port, csf, ssf);
    }

    /**
     * 用于在指定端口上激活/导出对象的构造函数。一个“可激活”的远程对象必须具有一个接受两个参数的构造函数： <ul>
     * <li>对象的激活标识符 (<code>ActivationID</code>)，和
     * <li>对象的初始化数据 (一个 <code>MarshalledObject</code>)。
     * </ul><p>
     *
     * 本类的具体子类必须在通过上述两个参数构造函数激活时调用此构造函数。作为构造的副作用，远程对象将“导出”
     * 到 RMI 运行时（在指定的 <code>port</code> 上），并可以接受来自客户端的传入调用。
     *
     * @param id 对象的激活标识符
     * @param port 对象导出的端口号
     * @exception RemoteException 如果将对象导出到 RMI 运行时失败
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     */
    protected Activatable(ActivationID id, int port)
        throws RemoteException
    {
        super();
        this.id = id;
        exportObject(this, id, port);
    }

    /**
     * 用于在指定端口上激活/导出对象的构造函数。一个“可激活”的远程对象必须具有一个接受两个参数的构造函数： <ul>
     * <li>对象的激活标识符 (<code>ActivationID</code>)，和
     * <li>对象的初始化数据 (一个 <code>MarshalledObject</code>)。
     * </ul><p>
     *
     * 本类的具体子类必须在通过上述两个参数构造函数激活时调用此构造函数。作为构造的副作用，远程对象将“导出”
     * 到 RMI 运行时（在指定的 <code>port</code> 上），并可以接受来自客户端的传入调用。
     *
     * @param id 对象的激活标识符
     * @param port 对象导出的端口号
     * @param csf 用于调用远程对象的客户端套接字工厂
     * @param ssf 用于接收远程调用的服务器端套接字工厂
     * @exception RemoteException 如果将对象导出到 RMI 运行时失败
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     */
    protected Activatable(ActivationID id, int port,
                          RMIClientSocketFactory csf,
                          RMIServerSocketFactory ssf)
        throws RemoteException
    {
        super();
        this.id = id;
        exportObject(this, id, port, csf, ssf);
    }

    /**
     * 返回对象的激活标识符。此方法受保护，因此只有子类可以获取对象的标识符。
     * @return 对象的激活标识符
     * @since 1.2
     */
    protected ActivationID getID() {
        return id;
    }

    /**
     * 为可激活的远程对象注册对象描述符，以便按需激活。
     *
     * @param desc  对象的描述符
     * @return 可激活远程对象的存根
     * @exception UnknownGroupException 如果 <code>desc</code> 中的组 ID 未在激活系统中注册
     * @exception ActivationException 如果激活系统未运行
     * @exception RemoteException 如果远程调用失败
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     */
    public static Remote register(ActivationDesc desc)
        throws UnknownGroupException, ActivationException, RemoteException
    {
        // 在激活器中注册对象。
        ActivationID id =
            ActivationGroup.getSystem().registerObject(desc);
        return sun.rmi.server.ActivatableRef.getStub(desc, id);
    }

    /**
     * 告知系统具有相应激活 <code>id</code> 的对象当前处于非活动状态。如果对象当前处于活动状态，
     * 则对象将从 RMI 运行时“取消导出”（仅当没有待处理或正在进行的调用时），
     * 以便不再接收传入调用。此调用告知此 VM 的 ActivationGroup 对象处于非活动状态，
     * 而 ActivationGroup 又告知其 ActivationMonitor。如果此调用成功完成，后续对激活器的激活请求
     * 将导致对象重新激活。如果对象被认为处于活动状态但已自行取消导出，操作仍可能成功。
     *
     * @param id 对象的激活标识符
     * @return 如果操作成功（对象当前已知处于活动状态且已取消导出或当前已导出且没有待处理/执行的调用），则返回 true；
     * 如果对象有待处理/执行的调用，则返回 false，因为不能取消激活。
     * @exception UnknownObjectException 如果对象未知（可能已经处于非活动状态）
     * @exception ActivationException 如果组未处于活动状态
     * @exception RemoteException 如果通知监视器的调用失败
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     */
    public static boolean inactive(ActivationID id)
        throws UnknownObjectException, ActivationException, RemoteException
    {
        return ActivationGroup.currentGroup().inactiveObject(id);
    }

    /**
     * 撤销与 <code>id</code> 关联的激活描述符的先前注册。对象不能再通过该 <code>id</code> 激活。
     *
     * @param id 对象的激活标识符
     * @exception UnknownObjectException 如果对象 (<code>id</code>) 未知
     * @exception ActivationException 如果激活系统未运行
     * @exception RemoteException 如果对激活系统的远程调用失败
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     */
    public static void unregister(ActivationID id)
        throws UnknownObjectException, ActivationException, RemoteException
    {
        ActivationGroup.getSystem().unregisterObject(id);
    }

    /**
     * 为指定对象注册激活描述符（指定位置、数据和重启模式），并使用指定端口导出该对象。
     *
     * <p><strong>注意：</strong> 使用此方法（以及既注册又导出可激活远程对象的 <code>Activatable</code> 构造函数）
     * 是强烈不建议的，因为注册和导出远程对象的操作不是原子性的。相反，应用程序应分别注册激活描述符和导出远程对象，
     * 以便正确处理异常。
     *
     * <p>此方法调用 {@link
     * #exportObject(Remote,String,MarshalledObject,boolean,int,RMIClientSocketFactory,RMIServerSocketFactory)
     * exportObject} 方法，使用指定的对象、位置、数据、重启模式和端口，以及 <code>null</code> 作为客户端和服务器套接字工厂，
     * 然后返回生成的激活标识符。
     *
     * @param obj 被导出的对象
     * @param location 对象的代码位置
     * @param data 对象的引导数据
     * @param restart 如果为 true，当激活器重启或对象的激活组在意外崩溃后重启时，对象将重启（重新激活）；
     * 如果为 false，对象仅按需激活。指定 <code>restart</code> 为 <code>true</code> 不会强制立即激活新注册的对象；
     * 初始激活是懒惰的。
     * @param port 对象导出的端口（如果 port=0，则使用匿名端口）
     * @return 从激活系统注册描述符 <code>desc</code> 获得的激活标识符
     * @exception ActivationException 如果激活组未处于活动状态
     * @exception RemoteException 如果对象注册或导出失败
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     **/
    public static ActivationID exportObject(Remote obj,
                                            String location,
                                            MarshalledObject<?> data,
                                            boolean restart,
                                            int port)
        throws ActivationException, RemoteException
    {
        return exportObject(obj, location, data, restart, port, null, null);
    }


                /**
     * 为指定对象注册一个激活描述符（具有指定的位置、数据和重启模式），并使用指定的端口和客户端、服务器套接字工厂导出该对象。
     *
     * <p><strong>注意：</strong>使用此方法（以及同时注册和导出可激活远程对象的<code>Activatable</code>构造函数）强烈不建议，因为注册和导出远程对象的动作不能保证是原子的。相反，应用程序应分别注册激活描述符和导出远程对象，以便正确处理异常。
     *
     * <p>此方法首先通过以下方式为指定对象注册激活描述符。它通过调用方法{@link ActivationGroup#getSystem ActivationGroup.getSystem}获取激活系统。然后，此方法通过调用激活系统的{@link ActivationSystem#registerObject registerObject}方法，使用指定对象的类名、指定的位置、数据和重启模式构造的{@link ActivationDesc}来获取对象的{@link ActivationID}。如果在获取激活系统或注册激活描述符时发生异常，该异常将抛给调用者。
     *
     * <p>接下来，此方法通过调用{@link #exportObject(Remote,ActivationID,int,RMIClientSocketFactory,RMIServerSocketFactory) exportObject}方法，使用指定的远程对象、从注册中获得的激活标识符、指定的端口和客户端、服务器套接字工厂来导出对象。如果在导出对象时发生异常，此方法将尝试通过调用激活系统的{@link ActivationSystem#unregisterObject unregisterObject}方法，使用从注册中获得的激活标识符来注销激活标识符。如果在注销标识符时发生异常，该异常将被忽略，而最初导出对象时发生的异常将抛给调用者。
     *
     * <p>最后，此方法在当前VM中的激活组上调用{@link ActivationGroup#activeObject activeObject}方法，使用激活标识符和指定的远程对象，并将激活标识符返回给调用者。
     *
     * @param obj 被导出的对象
     * @param location 对象的代码位置
     * @param data 对象的引导数据
     * @param restart 如果为true，对象在激活器重启或对象的激活组在意外崩溃后重启时重新启动（重新激活）；如果为false，对象仅按需激活。指定<code>restart</code>为<code>true</code>不会强制立即激活新注册的对象；初始激活是懒惰的。
     * @param port 对象导出的端口（如果端口=0，则使用匿名端口）
     * @param csf 用于调用远程对象的客户端套接字工厂
     * @param ssf 用于接收远程调用的服务器端套接字工厂
     * @return 从激活系统注册描述符时获得的激活标识符
     * @exception ActivationException 如果激活组未激活
     * @exception RemoteException 如果对象注册或导出失败
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     **/
    public static ActivationID exportObject(Remote obj,
                                            String location,
                                            MarshalledObject<?> data,
                                            boolean restart,
                                            int port,
                                            RMIClientSocketFactory csf,
                                            RMIServerSocketFactory ssf)
        throws ActivationException, RemoteException
    {
        ActivationDesc desc = new ActivationDesc(obj.getClass().getName(),
                                                 location, data, restart);
        /*
         * 注册描述符。
         */
        ActivationSystem system =  ActivationGroup.getSystem();
        ActivationID id = system.registerObject(desc);

        /*
         * 导出对象。
         */
        try {
            exportObject(obj, id, port, csf, ssf);
        } catch (RemoteException e) {
            /*
             * 尝试注销激活描述符，因为导出失败，注册/导出应该是原子的（参见4323621）。
             */
            try {
                system.unregisterObject(id);
            } catch (Exception ex) {
            }
            /*
             * 报告原始异常。
             */
            throw e;
        }

        /*
         * 此调用不会失败（这是一个本地调用，唯一可能的异常，即如果组未激活时抛出的异常，不会被抛出，因为组不是未激活的）。
         */
        ActivationGroup.currentGroup().activeObject(id, obj);

        return id;
    }

    /**
     * 将可激活的远程对象导出到RMI运行时，使对象能够接收传入的调用。如果<code>port</code>为零，对象将在匿名端口上导出。 <p>
     *
     * 在激活期间，此<code>exportObject</code>方法应由不扩展<code>Activatable</code>类的“可激活”对象显式调用。对于扩展<code>Activatable</code>类的对象，无需直接调用此方法，因为对象在构造时导出。
     *
     * @return 可激活远程对象的存根
     * @param obj 远程对象实现
     * @param id 对象的激活标识符
     * @param port 对象导出的端口（如果端口=0，则使用匿名端口）
     * @exception RemoteException 如果对象导出失败
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     */
    public static Remote exportObject(Remote obj,
                                      ActivationID id,
                                      int port)
        throws RemoteException
    {
        return exportObject(obj, new ActivatableServerRef(id, port));
    }

    /**
     * 将可激活的远程对象导出到RMI运行时，使对象能够接收传入的调用。如果<code>port</code>为零，对象将在匿名端口上导出。 <p>
     *
     * 在激活期间，此<code>exportObject</code>方法应由不扩展<code>Activatable</code>类的“可激活”对象显式调用。对于扩展<code>Activatable</code>类的对象，无需直接调用此方法，因为对象在构造时导出。
     *
     * @return 可激活远程对象的存根
     * @param obj 远程对象实现
     * @param id 对象的激活标识符
     * @param port 对象导出的端口（如果端口=0，则使用匿名端口）
     * @param csf 用于调用远程对象的客户端套接字工厂
     * @param ssf 用于接收远程调用的服务器端套接字工厂
     * @exception RemoteException 如果对象导出失败
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     */
    public static Remote exportObject(Remote obj,
                                      ActivationID id,
                                      int port,
                                      RMIClientSocketFactory csf,
                                      RMIServerSocketFactory ssf)
        throws RemoteException
    {
        return exportObject(obj, new ActivatableServerRef(id, port, csf, ssf));
    }

    /**
     * 从RMI运行时中移除远程对象，obj。如果成功，对象将不再接受传入的RMI调用。如果force参数为true，即使有对远程对象的挂起调用或远程对象仍有正在进行的调用，对象也会被强制取消导出。如果force参数为false，只有在没有挂起或正在进行的调用时，对象才会被取消导出。
     *
     * @param obj 要取消导出的远程对象
     * @param force 如果为true，即使有挂起或正在进行的调用，也会取消导出对象；如果为false，只有在没有挂起或正在进行的调用时，才会取消导出对象
     * @return 如果操作成功返回true，否则返回false
     * @exception NoSuchObjectException 如果远程对象当前未导出
     * @exception UnsupportedOperationException 如果此实现不支持激活
     * @since 1.2
     */
    public static boolean unexportObject(Remote obj, boolean force)
        throws NoSuchObjectException
    {
        return sun.rmi.transport.ObjectTable.unexportObject(obj, force);
    }

    /**
     * 使用指定的服务器引用导出指定的对象。
     */
    private static Remote exportObject(Remote obj, ActivatableServerRef sref)
        throws RemoteException
    {
        // 如果obj扩展了Activatable，设置其引用。
        if (obj instanceof Activatable) {
            ((Activatable) obj).ref = sref;

        }
        return sref.exportObject(obj, null, false);
    }
}
