
/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.registry;

import java.rmi.RemoteException;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;
import sun.rmi.registry.RegistryImpl;
import sun.rmi.server.UnicastRef2;
import sun.rmi.server.UnicastRef;
import sun.rmi.server.Util;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;

/**
 * <code>LocateRegistry</code> 用于获取特定主机（包括本地主机）上的引导远程对象注册表的引用，或创建接受特定端口调用的远程对象注册表。
 *
 * <p> 注意，<code>getRegistry</code> 调用实际上不会连接到远程主机。它只是创建一个指向远程注册表的本地引用，即使远程主机上没有运行注册表，该调用也会成功。因此，由于此方法返回的远程注册表的后续方法调用可能会失败。
 *
 * @author  Ann Wollrath
 * @author  Peter Jones
 * @since   JDK1.1
 * @see     java.rmi.registry.Registry
 */
public final class LocateRegistry {

    /**
     * 私有构造函数，禁止公共构造。
     */
    private LocateRegistry() {}

    /**
     * 返回本地主机上默认注册表端口 1099 的远程对象 <code>Registry</code> 的引用。
     *
     * @return 远程对象注册表的引用（存根）
     * @exception RemoteException 如果无法创建引用
     * @since JDK1.1
     */
    public static Registry getRegistry()
        throws RemoteException
    {
        return getRegistry(null, Registry.REGISTRY_PORT);
    }

    /**
     * 返回本地主机上指定 <code>port</code> 的远程对象 <code>Registry</code> 的引用。
     *
     * @param port 注册表接受请求的端口
     * @return 远程对象注册表的引用（存根）
     * @exception RemoteException 如果无法创建引用
     * @since JDK1.1
     */
    public static Registry getRegistry(int port)
        throws RemoteException
    {
        return getRegistry(null, port);
    }

    /**
     * 返回指定 <code>host</code> 上默认注册表端口 1099 的远程对象 <code>Registry</code> 的引用。如果 <code>host</code> 为 <code>null</code>，则使用本地主机。
     *
     * @param host 远程注册表的主机
     * @return 远程对象注册表的引用（存根）
     * @exception RemoteException 如果无法创建引用
     * @since JDK1.1
     */
    public static Registry getRegistry(String host)
        throws RemoteException
    {
        return getRegistry(host, Registry.REGISTRY_PORT);
    }

    /**
     * 返回指定 <code>host</code> 和 <code>port</code> 的远程对象 <code>Registry</code> 的引用。如果 <code>host</code> 为 <code>null</code>，则使用本地主机。
     *
     * @param host 远程注册表的主机
     * @param port 注册表接受请求的端口
     * @return 远程对象注册表的引用（存根）
     * @exception RemoteException 如果无法创建引用
     * @since JDK1.1
     */
    public static Registry getRegistry(String host, int port)
        throws RemoteException
    {
        return getRegistry(host, port, null);
    }

    /**
     * 返回一个本地创建的远程引用，指向指定 <code>host</code> 和 <code>port</code> 的远程对象 <code>Registry</code>。与该远程注册表的通信将使用提供的 <code>RMIClientSocketFactory</code> <code>csf</code> 创建到远程 <code>host</code> 和 <code>port</code> 的 <code>Socket</code> 连接。
     *
     * @param host 远程注册表的主机
     * @param port 注册表接受请求的端口
     * @param csf  用于与注册表建立连接的客户端 <code>Socket</code> 工厂。如果 <code>csf</code> 为 null，则在注册表存根中使用默认的客户端 <code>Socket</code> 工厂。
     * @return 远程注册表的引用（存根）
     * @exception RemoteException 如果无法创建引用
     * @since 1.2
     */
    public static Registry getRegistry(String host, int port,
                                       RMIClientSocketFactory csf)
        throws RemoteException
    {
        Registry registry = null;

        if (port <= 0)
            port = Registry.REGISTRY_PORT;

        if (host == null || host.length() == 0) {
            // 如果主机为空（如 1.0.2 中 "file:" URL 在 java.rmi.Naming 中返回的），尝试转换为真实的本地主机名，以便 RegistryImpl 的 checkAccess 不会失败。
            try {
                host = java.net.InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                // 如果失败，至少尝试使用 ""（本地主机）...
                host = "";
            }
        }

        /*
         * 使用给定的主机、端口和客户端套接字工厂创建注册表的代理。如果提供的客户端套接字工厂为 null，则 ref 类型为 UnicastRef，否则 ref 类型为 UnicastRef2。如果属性
         * java.rmi.server.ignoreStubClasses 为 true，则返回的代理是实现 Registry 接口的动态代理类的实例；否则返回的代理是 RegistryImpl 的预生成存根类的实例。
         **/
        LiveRef liveRef =
            new LiveRef(new ObjID(ObjID.REGISTRY_ID),
                        new TCPEndpoint(host, port, csf, null),
                        false);
        RemoteRef ref =
            (csf == null) ? new UnicastRef(liveRef) : new UnicastRef2(liveRef);


                    return (Registry) Util.createProxy(RegistryImpl.class, ref, false);
    }

    /**
     * 在本地主机上创建并导出一个在指定 <code>port</code> 端口上接受请求的 <code>Registry</code> 实例。
     *
     * <p><code>Registry</code> 实例的导出方式类似于调用静态方法
     * {@link UnicastRemoteObject#exportObject(Remote,int)
     * UnicastRemoteObject.exportObject}，传递 <code>Registry</code> 实例和指定的 <code>port</code> 作为参数，
     * 但 <code>Registry</code> 实例使用一个众所周知的对象标识符导出，即使用值 {@link ObjID#REGISTRY_ID} 构造的 {@link ObjID} 实例。
     *
     * @param port 该注册表接受请求的端口
     * @return 注册表
     * @exception RemoteException 如果注册表无法导出
     * @since JDK1.1
     **/
    public static Registry createRegistry(int port) throws RemoteException {
        return new RegistryImpl(port);
    }

    /**
     * 在本地主机上创建并导出一个使用自定义套接字工厂与该实例通信的 <code>Registry</code> 实例。创建的注册表使用提供的
     * <code>RMIServerSocketFactory</code> 创建的 <code>ServerSocket</code> 监听给定的 <code>port</code> 上的传入请求。
     *
     * <p><code>Registry</code> 实例的导出方式类似于调用静态方法
     * {@link
     * UnicastRemoteObject#exportObject(Remote,int,RMIClientSocketFactory,RMIServerSocketFactory)
     * UnicastRemoteObject.exportObject}，传递 <code>Registry</code> 实例、指定的 <code>port</code>、指定的 <code>RMIClientSocketFactory</code> 和指定的
     * <code>RMIServerSocketFactory</code> 作为参数，但 <code>Registry</code> 实例使用一个众所周知的对象标识符导出，即使用值
     * {@link ObjID#REGISTRY_ID} 构造的 {@link ObjID} 实例。
     *
     * @param port 注册表接受请求的端口
     * @param csf  用于连接到注册表的客户端 <code>Socket</code> 工厂
     * @param ssf  用于接受连接到注册表的服务器端 <code>ServerSocket</code> 工厂
     * @return 注册表
     * @exception RemoteException 如果注册表无法导出
     * @since 1.2
     **/
    public static Registry createRegistry(int port,
                                          RMIClientSocketFactory csf,
                                          RMIServerSocketFactory ssf)
        throws RemoteException
    {
        return new RegistryImpl(port, csf, ssf);
    }
}
