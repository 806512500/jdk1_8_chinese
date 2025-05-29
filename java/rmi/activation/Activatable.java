
/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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
 * <code>Activatable</code> 类为需要持久访问且可以由系统激活的远程对象提供支持。
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
     * 通过注册此对象的激活描述符（指定位置、数据和重启模式）并使用指定的端口导出该对象来构造可激活的远程对象。
     *
     * <p><strong>注意：</strong>不建议使用同时注册和导出可激活远程对象的 <code>Activatable</code>
     * 构造函数，因为注册和导出远程对象的操作<i>不</i>保证是原子的。相反，应用程序应分别注册激活描述符和导出远程对象，
     * 以便正确处理异常。
     *
     * <p>此方法调用 {@link
     * #exportObject(Remote,String,MarshalledObject,boolean,int)
     * exportObject} 方法，使用此对象以及指定的位置、数据、重启模式和端口。后续调用 {@link #getID}
     * 将返回从 <code>exportObject</code> 调用返回的激活标识符。
     *
     * @param location 此对象的类的位置
     * @param data 对象的初始化数据
     * @param port 对象导出的端口（如果端口=0，则使用匿名端口）
     * @param restart 如果为 true，则当激活器重启或对象的激活组在意外崩溃后重启时，对象将重启（重新激活）；
     * 如果为 false，则对象仅按需激活。指定 <code>restart</code> 为 <code>true</code> 不会强制新注册的对象立即激活；
     * 初始激活是懒惰的。
     * @exception ActivationException 如果对象注册失败。
     * @exception RemoteException 如果以下任何一项失败：
     * a) 将对象注册到激活系统或 b) 将对象导出到 RMI 运行时。
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
     * 通过注册此对象的激活描述符（指定位置、数据和重启模式）并使用指定的端口、客户端和服务器套接字工厂导出该对象来构造可激活的远程对象。
     *
     * <p><strong>注意：</strong>不建议使用同时注册和导出可激活远程对象的 <code>Activatable</code>
     * 构造函数，因为注册和导出远程对象的操作<i>不</i>保证是原子的。相反，应用程序应分别注册激活描述符和导出远程对象，
     * 以便正确处理异常。
     *
     * <p>此方法调用 {@link
     * #exportObject(Remote,String,MarshalledObject,boolean,int,RMIClientSocketFactory,RMIServerSocketFactory)
     * exportObject} 方法，使用此对象以及指定的位置、数据、重启模式、端口和客户端及服务器套接字工厂。后续调用 {@link #getID}
     * 将返回从 <code>exportObject</code> 调用返回的激活标识符。
     *
     * @param location 此对象的类的位置
     * @param data 对象的初始化数据
     * @param restart 如果为 true，则当激活器重启或对象的激活组在意外崩溃后重启时，对象将重启（重新激活）；
     * 如果为 false，则对象仅按需激活。指定 <code>restart</code> 为 <code>true</code> 不会强制新注册的对象立即激活；
     * 初始激活是懒惰的。
     * @param port 对象导出的端口（如果端口=0，则使用匿名端口）
     * @param csf 用于调用远程对象的客户端套接字工厂
     * @param ssf 用于接收远程调用的服务器端套接字工厂
     * @exception ActivationException 如果对象注册失败。
     * @exception RemoteException 如果以下任何一项失败：
     * a) 将对象注册到激活系统或 b) 将对象导出到 RMI 运行时。
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
     * 构造函数用于在指定端口激活/导出对象。一个“可激活”的远程对象必须有一个构造函数，
     * 该构造函数接受两个参数：<ul>
     * <li>对象的激活标识符（<code>ActivationID</code>），以及
     * <li>对象的初始化数据（一个<code>MarshalledObject</code>）。
     * </ul><p>
     *
     * 本类的具体子类必须在通过上述两个参数构造函数激活时调用此构造函数。作为构造的副作用，
     * 远程对象被“导出”到RMI运行时（在指定的<code>port</code>上），并且可以接受来自客户端的调用。
     *
     * @param id 对象的激活标识符
     * @param port 对象导出的端口号
     * @exception RemoteException 如果将对象导出到RMI运行时失败
     * @exception UnsupportedOperationException 如果且仅当此实现不支持激活时
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
     * 构造函数用于在指定端口激活/导出对象。一个“可激活”的远程对象必须有一个构造函数，
     * 该构造函数接受两个参数：<ul>
     * <li>对象的激活标识符（<code>ActivationID</code>），以及
     * <li>对象的初始化数据（一个<code>MarshalledObject</code>）。
     * </ul><p>
     *
     * 本类的具体子类必须在通过上述两个参数构造函数激活时调用此构造函数。作为构造的副作用，
     * 远程对象被“导出”到RMI运行时（在指定的<code>port</code>上），并且可以接受来自客户端的调用。
     *
     * @param id 对象的激活标识符
     * @param port 对象导出的端口号
     * @param csf 用于调用远程对象的客户端套接字工厂
     * @param ssf 用于接收远程调用的服务器端套接字工厂
     * @exception RemoteException 如果将对象导出到RMI运行时失败
     * @exception UnsupportedOperationException 如果且仅当此实现不支持激活时
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
     * 返回对象的激活标识符。该方法受保护，以便只有子类可以获取对象的标识符。
     * @return 对象的激活标识符
     * @since 1.2
     */
    protected ActivationID getID() {
        return id;
    }

    /**
     * 为可激活远程对象注册一个对象描述符，以便按需激活。
     *
     * @param desc 对象的描述符
     * @return 可激活远程对象的存根
     * @exception UnknownGroupException 如果<code>desc</code>中的组ID未在激活系统中注册
     * @exception ActivationException 如果激活系统未运行
     * @exception RemoteException 如果远程调用失败
     * @exception UnsupportedOperationException 如果且仅当此实现不支持激活时
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
     * 告知系统具有相应激活<code>id</code>的对象当前处于非活动状态。如果对象当前处于活动状态，
     * 则对象将从RMI运行时“取消导出”（仅当没有待处理或正在进行的调用时），
     * 以便它不再接收传入的调用。此调用告知此VM的ActivationGroup该对象处于非活动状态，
     * 进而告知其ActivationMonitor。如果此调用成功完成，随后对激活器的激活请求将导致对象重新激活。
     * 即使对象被认为处于活动状态但已自行取消导出，操作仍可能成功。
     *
     * @param id 对象的激活标识符
     * @return 如果操作成功（如果对象当前已知处于活动状态并且要么已取消导出，要么当前已导出且没有待处理/执行的调用），则返回true；
     * 如果对象有待处理/执行的调用，则返回false，因为在这种情况下它不能被停用
     * @exception UnknownObjectException 如果对象未知（它可能已经处于非活动状态）
     * @exception ActivationException 如果组未处于活动状态
     * @exception RemoteException 如果通知监视器的调用失败
     * @exception UnsupportedOperationException 如果且仅当此实现不支持激活时
     * @since 1.2
     */
    public static boolean inactive(ActivationID id)
        throws UnknownObjectException, ActivationException, RemoteException
    {
        return ActivationGroup.currentGroup().inactiveObject(id);
    }

    /**
     * 撤销与<code>id</code>关联的激活描述符的先前注册。对象不能再通过该<code>id</code>被激活。
     *
     * @param id 对象的激活标识符
     * @exception UnknownObjectException 如果对象（<code>id</code>）未知
     * @exception ActivationException 如果激活系统未运行
     * @exception RemoteException 如果对激活系统的远程调用失败
     * @exception UnsupportedOperationException 如果且仅当此实现不支持激活时
     * @since 1.2
     */
    public static void unregister(ActivationID id)
        throws UnknownObjectException, ActivationException, RemoteException
    {
        ActivationGroup.getSystem().unregisterObject(id);
    }

                /**
     * 为指定对象注册一个激活描述符（指定位置、数据和重启模式），并使用指定端口导出该对象。
     *
     * <p><strong>注意：</strong> 使用此方法（以及同时注册和导出可激活远程对象的 <code>Activatable</code> 构造函数）是强烈不建议的，因为注册和导出远程对象的操作 <i>不是</i> 原子性的。 相反，应用程序应该分别注册激活描述符和导出远程对象，以便正确处理异常。
     *
     * <p>此方法调用带有指定对象、位置、数据、重启模式和端口的 {@link
     * #exportObject(Remote,String,MarshalledObject,boolean,int,RMIClientSocketFactory,RMIServerSocketFactory)
     * exportObject} 方法，并将客户端和服务器套接字工厂设置为 <code>null</code>，然后返回生成的激活标识符。
     *
     * @param obj 被导出的对象
     * @param location 对象的代码位置
     * @param data 对象的引导数据
     * @param restart 如果为 true，则当激活器重启或对象的激活组在意外崩溃后重启时，对象将被重启（重新激活）；如果为 false，则对象仅按需激活。 指定 <code>restart</code> 为 <code>true</code> 并不会强制立即激活新注册的对象；初始激活是惰性的。
     * @param port 对象导出的端口（如果 port=0，则使用匿名端口）
     * @return 从激活系统注册描述符 <code>desc</code> 获得的激活标识符
     * the wrong group
     * @exception ActivationException 如果激活组不活跃
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
     * 为指定对象注册一个激活描述符（指定位置、数据和重启模式），并使用指定端口和指定的客户端及服务器套接字工厂导出该对象。
     *
     * <p><strong>注意：</strong> 使用此方法（以及同时注册和导出可激活远程对象的 <code>Activatable</code> 构造函数）是强烈不建议的，因为注册和导出远程对象的操作 <i>不是</i> 原子性的。 相反，应用程序应该分别注册激活描述符和导出远程对象，以便正确处理异常。
     *
     * <p>此方法首先按照以下方式为指定对象注册一个激活描述符。它通过调用方法 {@link ActivationGroup#getSystem
     * ActivationGroup.getSystem} 获取激活系统。然后，通过调用激活系统的 {@link
     * ActivationSystem#registerObject registerObject} 方法，使用指定对象的类名以及指定的位置、数据和重启模式构造的 {@link ActivationDesc} 来获取对象的 {@link
     * ActivationID}。如果在获取激活系统或注册激活描述符时发生异常，该异常将被抛给调用者。
     *
     * <p>接下来，此方法通过调用带有指定远程对象、从注册中获得的激活标识符、指定端口以及指定的客户端和服务器套接字工厂的 {@link
     * #exportObject(Remote,ActivationID,int,RMIClientSocketFactory,RMIServerSocketFactory)
     * exportObject} 方法来导出对象。如果在导出对象时发生异常，此方法将尝试通过调用激活系统的 {@link
     * ActivationSystem#unregisterObject unregisterObject} 方法，使用从注册中获得的激活标识符来注销激活标识符。如果在注销标识符时发生异常，该异常将被忽略，而最初导出对象时发生的异常将被抛给调用者。
     *
     * <p>最后，此方法在本虚拟机中的激活组上调用带有激活标识符和指定远程对象的 {@link
     * ActivationGroup#activeObject activeObject} 方法，并将激活标识符返回给调用者。
     *
     * @param obj 被导出的对象
     * @param location 对象的代码位置
     * @param data 对象的引导数据
     * @param restart 如果为 true，则当激活器重启或对象的激活组在意外崩溃后重启时，对象将被重启（重新激活）；如果为 false，则对象仅按需激活。 指定 <code>restart</code> 为 <code>true</code> 并不会强制立即激活新注册的对象；初始激活是惰性的。
     * @param port 对象导出的端口（如果 port=0，则使用匿名端口）
     * @param csf 用于调用远程对象的客户端套接字工厂
     * @param ssf 用于接收远程调用的服务器端套接字工厂
     * @return 从激活系统注册描述符获得的激活标识符
     * @exception ActivationException 如果激活组不活跃
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
             * 尝试注销激活描述符，因为导出失败，注册/导出应该是原子的（参见 4323621）。
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
         * 此调用不会失败（这是一个本地调用，唯一可能的异常是在组不活动时抛出，但这种情况不会发生
         * 因为组是活动的）。
         */
        ActivationGroup.currentGroup().activeObject(id, obj);

        return id;
    }

    /**
     * 将可激活的远程对象导出到 RMI 运行时，使对象能够接收传入的调用。如果 <code>port</code> 为零，则对象将在匿名端口上导出。 <p>
     *
     * 在激活期间，此 <code>exportObject</code> 方法应由不扩展 <code>Activatable</code> 类的“可激活”对象显式调用。对于扩展 <code>Activatable</code> 类的对象，无需直接调用此方法，因为对象在构造时已导出。
     *
     * @return 可激活远程对象的存根
     * @param obj 远程对象实现
     * @param id 对象的激活标识符
     * @param port 对象导出的端口（如果 port=0，则使用匿名端口）
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
     * 将可激活的远程对象导出到 RMI 运行时，使对象能够接收传入的调用。如果 <code>port</code> 为零，则对象将在匿名端口上导出。 <p>
     *
     * 在激活期间，此 <code>exportObject</code> 方法应由不扩展 <code>Activatable</code> 类的“可激活”对象显式调用。对于扩展 <code>Activatable</code> 类的对象，无需直接调用此方法，因为对象在构造时已导出。
     *
     * @return 可激活远程对象的存根
     * @param obj 远程对象实现
     * @param id 对象的激活标识符
     * @param port 对象导出的端口（如果 port=0，则使用匿名端口）
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
     * 从 RMI 运行时中移除远程对象 obj。如果成功，对象将不再接受传入的 RMI 调用。
     * 如果 force 参数为 true，即使有对远程对象的待处理调用或远程对象仍有正在进行的调用，对象也会被强制取消导出。如果 force 参数为 false，只有在没有待处理或正在进行的调用时，对象才会被取消导出。
     *
     * @param obj 要取消导出的远程对象
     * @param force 如果为 true，即使有待处理或正在进行的调用，也会取消导出对象；如果为 false，只有在没有待处理或正在进行的调用时才会取消导出对象
     * @return 如果操作成功返回 true，否则返回 false
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
        // 如果 obj 扩展了 Activatable，设置其引用。
        if (obj instanceof Activatable) {
            ((Activatable) obj).ref = sref;

        }
        return sref.exportObject(obj, null, false);
    }
}
