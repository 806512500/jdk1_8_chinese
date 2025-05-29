
/*
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.server;

import java.rmi.*;

/**
 * <code>RemoteRef</code> 表示远程对象的句柄。一个 <code>RemoteStub</code> 使用远程引用来进行
 * 对远程对象的远程方法调用。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @see     java.rmi.server.RemoteStub
 */
public interface RemoteRef extends java.io.Externalizable {

    /** 表示与 JDK 1.1.x 版本的类兼容。 */
    static final long serialVersionUID = 3632638527362204081L;

    /**
     * 初始化服务器包前缀：假设服务器引用类的实现（例如，UnicastRef，UnicastServerRef）位于
     * 由前缀定义的包中。
     */
    final static String packagePrefix = "sun.rmi.server";

    /**
     * 调用一个方法。这种委托方法调用的形式允许引用处理设置与远程主机的连接，序列化方法和参数的某种表示，
     * 然后将方法调用通信到远程主机。此方法返回位于远程主机上的远程对象的方法调用的结果，或者如果调用失败则抛出
     * RemoteException，如果远程调用抛出异常则抛出应用程序级别的异常。
     *
     * @param obj 包含 RemoteRef 的对象（例如，对象的 RemoteStub）。
     * @param method 要调用的方法
     * @param params 参数列表
     * @param opnum  可用于表示方法的哈希值
     * @return 远程方法调用的结果
     * @exception Exception 如果在远程方法调用期间发生任何异常
     * @since 1.2
     */
    Object invoke(Remote obj,
                  java.lang.reflect.Method method,
                  Object[] params,
                  long opnum)
        throws Exception;

    /**
     * 为这个对象的新远程方法调用创建一个适当的调用对象。传递操作数组和索引，允许存根生成器分配操作索引并解释它们。
     * 远程引用可能需要将操作编码在调用中。
     *
     * @since JDK1.1
     * @deprecated 1.2 风格的存根不再使用此方法。存根不再使用对远程引用的一系列方法调用（<code>newCall</code>、
     * <code>invoke</code> 和 <code>done</code>），而是使用单个方法 <code>invoke(Remote, Method, Object[], int)</code>
     * 来执行参数的序列化、远程方法执行和返回值的反序列化。
     *
     * @param obj 通过其进行调用的远程存根
     * @param op 存根操作数组
     * @param opnum 操作编号
     * @param hash 存根/骨架接口哈希
     * @return 表示远程调用的调用对象
     * @throws RemoteException 如果未能启动新的远程调用
     * @see #invoke(Remote,java.lang.reflect.Method,Object[],long)
     */
    @Deprecated
    RemoteCall newCall(RemoteObject obj, Operation[] op, int opnum, long hash)
        throws RemoteException;

    /**
     * 执行远程调用。
     *
     * 调用将引发任何应通过存根传递而不会被捕获的“用户”异常。如果在远程调用期间发生任何异常，
     * 调用应负责在引发“用户”或远程异常之前清理连接。
     *
     * @since JDK1.1
     * @deprecated 1.2 风格的存根不再使用此方法。存根不再使用对远程引用的一系列方法调用（<code>newCall</code>、
     * <code>invoke</code> 和 <code>done</code>），而是使用单个方法 <code>invoke(Remote, Method, Object[], int)</code>
     * 来执行参数的序列化、远程方法执行和返回值的反序列化。
     *
     * @param call 表示远程调用的对象
     * @throws Exception 如果在远程方法调用期间发生任何异常
     * @see #invoke(Remote,java.lang.reflect.Method,Object[],long)
     */
    @Deprecated
    void invoke(RemoteCall call) throws Exception;

    /**
     * 允许远程引用清理（或重用）连接。只有在调用成功返回存根（非异常）时才应调用 Done。
     *
     * @since JDK1.1
     * @deprecated 1.2 风格的存根不再使用此方法。存根不再使用对远程引用的一系列方法调用（<code>newCall</code>、
     * <code>invoke</code> 和 <code>done</code>），而是使用单个方法 <code>invoke(Remote, Method, Object[], int)</code>
     * 来执行参数的序列化、远程方法执行和返回值的反序列化。
     *
     * @param call 表示远程调用的对象
     * @throws RemoteException 如果在调用清理期间发生远程错误
     * @see #invoke(Remote,java.lang.reflect.Method,Object[],long)
     */
    @Deprecated
    void done(RemoteCall call) throws RemoteException;

    /**
     * 返回要序列化到流 'out' 的引用类型的类名。
     * @param out 引用将被序列化的输出流
     * @return 引用类型的类名（不带包限定）
     * @since JDK1.1
     */
    String getRefClass(java.io.ObjectOutput out);

    /**
     * 返回远程对象的哈希码。引用同一远程对象的两个远程对象存根将具有相同的哈希码
     * （为了支持哈希表中的远程对象作为键）。
     *
     * @return 远程对象哈希码
     * @see             java.util.Hashtable
     * @since JDK1.1
     */
    int remoteHashCode();

                /**
     * 比较两个远程对象是否相等。
     * 返回一个布尔值，指示此远程对象是否与指定的对象等效。当远程对象存储在哈希表中时，使用此方法。
     * @param   obj     要比较的对象
     * @return  如果这些对象相等，则返回 true；否则返回 false。
     * @see             java.util.Hashtable
     * @since JDK1.1
     */
    boolean remoteEquals(RemoteRef obj);

    /**
     * 返回表示此远程对象引用的字符串。
     * @return 表示远程对象引用的字符串
     * @since JDK1.1
     */
    String remoteToString();

}
