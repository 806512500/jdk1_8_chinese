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
import sun.rmi.server.UnicastServerRef;
import sun.rmi.server.UnicastServerRef2;

/**
 * 用于使用 JRMP 导出远程对象并获取与远程对象通信的存根。存根可以使用动态代理对象在运行时生成，也可以在构建时静态生成，通常使用 {@code rmic} 工具。
 *
 * <p><strong>已弃用：静态存根。</strong> <em>对静态生成存根的支持已弃用。这包括需要使用静态存根的此类中的 API 以及加载静态存根的运行时支持。建议使用以下五种非已弃用的导出对象方法之一动态生成存根。不要运行 {@code rmic} 生成静态存根类。这是不必要的，也是已弃用的。</em>
 *
 * <p>有六种导出远程对象的方法：
 *
 * <ol>
 *
 * <li>继承 {@code UnicastRemoteObject} 并调用 {@link #UnicastRemoteObject()} 构造函数。
 *
 * <li>继承 {@code UnicastRemoteObject} 并调用 {@link #UnicastRemoteObject(int) UnicastRemoteObject(port)} 构造函数。
 *
 * <li>继承 {@code UnicastRemoteObject} 并调用 {@link #UnicastRemoteObject(int, RMIClientSocketFactory, RMIServerSocketFactory)
 * UnicastRemoteObject(port, csf, ssf)} 构造函数。
 *
 * <li>调用 {@link #exportObject(Remote) exportObject(Remote)} 方法。 <strong>已弃用。</strong>
 *
 * <li>调用 {@link #exportObject(Remote, int) exportObject(Remote, port)} 方法。
 *
 * <li>调用 {@link #exportObject(Remote, int, RMIClientSocketFactory, RMIServerSocketFactory)
 * exportObject(Remote, port, csf, ssf)} 方法。
 *
 * </ol>
 *
 * <p>第四种方法 {@link #exportObject(Remote)} 始终使用静态生成的存根，已弃用。
 *
 * <p>其他五种方法都使用以下方法：如果 {@code java.rmi.server.ignoreStubClasses} 属性为 {@code true}（不区分大小写）或找不到静态存根，则使用 {@link java.lang.reflect.Proxy Proxy} 对象动态生成存根。否则，使用静态存根。
 *
 * <p>{@code java.rmi.server.ignoreStubClasses} 属性的默认值为 {@code false}。
 *
 * <p>静态生成的存根通常使用 {@code rmic} 工具从远程对象的类预生成。加载静态存根并构造该存根类的实例，如下所述：
 *
 * <ul>
 *
 * <li>确定“根类”如下：如果远程对象的类直接实现一个扩展 {@link Remote} 的接口，则远程对象的类是根类；否则，根类是远程对象的类的最派生的超类，该超类直接实现一个扩展 {@code Remote} 的接口。
 *
 * <li>要加载的存根类的名称通过将根类的二进制名称与后缀 {@code _Stub} 连接来确定。
 *
 * <li>使用根类的类加载器按名称加载存根类。存根类必须扩展 {@link RemoteStub} 并且必须有一个公共构造函数，该构造函数有一个类型为 {@link RemoteRef} 的参数。
 *
 * <li>最后，使用 {@link RemoteRef} 构造存根类的实例。
 *
 * <li>如果找不到适当的存根类，或者无法加载存根类，或者在创建存根实例时出现问题，则抛出 {@link StubNotFoundException}。
 *
 * </ul>
 *
 * <p>存根通过构造具有以下特征的 {@link java.lang.reflect.Proxy Proxy} 实例动态生成：
 *
 * <ul>
 *
 * <li>代理的类由远程对象类的类加载器定义。
 *
 * <li>代理实现远程对象类实现的所有远程接口。
 *
 * <li>代理的调用处理程序是一个使用 {@link RemoteRef} 构造的 {@link RemoteObjectInvocationHandler} 实例。
 *
 * <li>如果无法创建代理，则抛出 {@link StubNotFoundException}。
 *
 * </ul>
 *
 * @implNote
 * 根据用于导出对象的构造函数或静态方法，{@link RMISocketFactory} 可能用于创建套接字。
 * 默认情况下，由 {@link RMISocketFactory} 创建的服务器套接字监听所有网络接口。请参阅
 * {@link RMISocketFactory} 类和
 * <a href="{@docRoot}/../platform/rmi/spec/rmi-server29.html">RMI Socket Factories</a>
 * 部分
 * <a href="{@docRoot}/../platform/rmi/spec/rmiTOC.html">Java RMI Specification</a>。
 *
 * @author  Ann Wollrath
 * @author  Peter Jones
 * @since   JDK1.1
 **/
public class UnicastRemoteObject extends RemoteServer {

    /**
     * @serial 用于导出对象的端口号
     */
    private int port = 0;

    /**
     * @serial 客户端套接字工厂（如果有）
     */
    private RMIClientSocketFactory csf = null;

    /**
     * @serial 服务器端套接字工厂（如果有）用于导出对象
     */
    private RMIServerSocketFactory ssf = null;

    /* 表示与 JDK 1.1.x 版本的类兼容 */
    private static final long serialVersionUID = 4974527148936298033L;

    /**
     * 使用匿名端口创建并导出新的 UnicastRemoteObject 对象。
     *
     * <p>对象使用 {@link RMISocketFactory} 类创建的服务器套接字导出。
     *
     * @throws RemoteException 如果导出对象失败
     * @since JDK1.1
     */
    protected UnicastRemoteObject() throws RemoteException
    {
        this(0);
    }

    /**
     * 使用提供的特定端口创建并导出新的 UnicastRemoteObject 对象。
     *
     * <p>对象使用 {@link RMISocketFactory} 类创建的服务器套接字导出。
     *
     * @param port 远程对象接收调用的端口号（如果 <code>port</code> 为零，则选择匿名端口）
     * @throws RemoteException 如果导出对象失败
     * @since 1.2
     */
    protected UnicastRemoteObject(int port) throws RemoteException
    {
        this.port = port;
        exportObject((Remote) this, port);
    }

    /**
     * 使用提供的特定端口和套接字工厂创建并导出新的 UnicastRemoteObject 对象。
     *
     * <p>任一端口工厂可以为 {@code null}，在这种情况下，使用 {@link RMISocketFactory} 的相应客户端或服务器套接字创建方法。
     *
     * @param port 远程对象接收调用的端口号（如果 <code>port</code> 为零，则选择匿名端口）
     * @param csf 用于调用远程对象的客户端套接字工厂
     * @param ssf 用于接收远程调用的服务器端套接字工厂
     * @throws RemoteException 如果导出对象失败
     * @since 1.2
     */
    protected UnicastRemoteObject(int port,
                                  RMIClientSocketFactory csf,
                                  RMIServerSocketFactory ssf)
        throws RemoteException
    {
        this.port = port;
        this.csf = csf;
        this.ssf = ssf;
        exportObject((Remote) this, port, csf, ssf);
    }

    /**
     * 在反序列化时重新导出远程对象。
     */
    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, java.lang.ClassNotFoundException
    {
        in.defaultReadObject();
        reexport();
    }

    /**
     * 返回与原始对象不同的远程对象的克隆。
     *
     * @exception CloneNotSupportedException 如果克隆因 RemoteException 失败。
     * @return 新的远程对象
     * @since JDK1.1
     */
    public Object clone() throws CloneNotSupportedException
    {
        try {
            UnicastRemoteObject cloned = (UnicastRemoteObject) super.clone();
            cloned.reexport();
            return cloned;
        } catch (RemoteException e) {
            throw new ServerCloneException("Clone failed", e);
        }
    }

    /*
     * 使用其初始化的字段重新导出此 UnicastRemoteObject，因为其创建绕过了运行其构造函数（例如通过反序列化或克隆）。
     */
    private void reexport() throws RemoteException
    {
        if (csf == null && ssf == null) {
            exportObject((Remote) this, port);
        } else {
            exportObject((Remote) this, port, csf, ssf);
        }
    }

    /**
     * 使用匿名端口导出远程对象，使其能够接收传入的调用。此方法将始终返回静态生成的存根。
     *
     * <p>对象使用 {@link RMISocketFactory} 类创建的服务器套接字导出。
     *
     * @param obj 要导出的远程对象
     * @return 远程对象存根
     * @exception RemoteException 如果导出失败
     * @since JDK1.1
     * @deprecated 此方法已弃用，因为它仅支持静态存根。
     * 使用 {@link #exportObject(Remote, int) exportObject(Remote, port)} 或
     * {@link #exportObject(Remote, int, RMIClientSocketFactory, RMIServerSocketFactory)
     * exportObject(Remote, port, csf, ssf)}
     * 代替。
     */
    @Deprecated
    public static RemoteStub exportObject(Remote obj)
        throws RemoteException
    {
        /*
         * 使用 UnicastServerRef 构造函数传递布尔值 true
         * 表示仅使用生成的存根类。必须使用生成的存根类而不是动态代理
         * 因为该方法的返回值是 RemoteStub，而动态代理类不能扩展。
         */
        return (RemoteStub) exportObject(obj, new UnicastServerRef(true));
    }

    /**
     * 使用提供的特定端口导出远程对象，使其能够接收传入的调用。
     *
     * <p>对象使用 {@link RMISocketFactory} 类创建的服务器套接字导出。
     *
     * @param obj 要导出的远程对象
     * @param port 要导出对象的端口
     * @return 远程对象存根
     * @exception RemoteException 如果导出失败
     * @since 1.2
     */
    public static Remote exportObject(Remote obj, int port)
        throws RemoteException
    {
        return exportObject(obj, new UnicastServerRef(port));
    }

    /**
     * 使用给定套接字工厂指定的传输导出远程对象，使其能够接收传入的调用。
     *
     * <p>任一端口工厂可以为 {@code null}，在这种情况下，使用 {@link RMISocketFactory} 的相应客户端或服务器套接字创建方法。
     *
     * @param obj 要导出的远程对象
     * @param port 要导出对象的端口
     * @param csf 用于调用远程对象的客户端套接字工厂
     * @param ssf 用于接收远程调用的服务器端套接字工厂
     * @return 远程对象存根
     * @exception RemoteException 如果导出失败
     * @since 1.2
     */
    public static Remote exportObject(Remote obj, int port,
                                      RMIClientSocketFactory csf,
                                      RMIServerSocketFactory ssf)
        throws RemoteException
    {

        return exportObject(obj, new UnicastServerRef2(port, csf, ssf));
    }

    /**
     * 从 RMI 运行时中移除远程对象 obj。如果成功，对象将不再能够接受传入的 RMI 调用。
     * 如果 force 参数为 true，则即使有对远程对象的待处理调用或远程对象仍有正在进行的调用，对象也会被强制移除。
     * 如果 force 参数为 false，则只有在对象没有待处理或正在进行的调用时才会被移除。
     *
     * @param obj 要移除的远程对象
     * @param force 如果为 true，则即使有待处理或正在进行的调用，也移除对象；如果为 false，则只有在没有待处理或正在进行的调用时才移除对象
     * @return 如果操作成功返回 true，否则返回 false
     * @exception NoSuchObjectException 如果远程对象当前未导出
     * @since 1.2
     */
    public static boolean unexportObject(Remote obj, boolean force)
        throws java.rmi.NoSuchObjectException
    {
        return sun.rmi.transport.ObjectTable.unexportObject(obj, force);
    }

    /**
     * 使用指定的服务器引用导出指定的对象。
     */
    private static Remote exportObject(Remote obj, UnicastServerRef sref)
        throws RemoteException
    {
        // 如果 obj 继承自 UnicastRemoteObject，则设置其引用。
        if (obj instanceof UnicastRemoteObject) {
            ((UnicastRemoteObject) obj).ref = sref;
        }
        return sref.exportObject(obj, null, false);
    }
}
