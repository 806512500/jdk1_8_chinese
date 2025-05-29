/*
 * Copyright (c) 1997, 2004, Oracle and/or its affiliates. All rights reserved.
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
import java.rmi.UnknownHostException;

/**
 * <code>RegistryHandler</code> 是一个接口，用于 RMI 运行时在早期实现版本中的内部使用。应用程序代码不应访问它。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @deprecated 没有替代品
 */
@Deprecated
public interface RegistryHandler {

    /**
     * 返回一个“存根”，用于在指定的主机和端口上联系远程注册表。
     *
     * @deprecated 没有替代品。从 Java 2 平台 v1.2 开始，RMI 不再使用 <code>RegistryHandler</code> 来获取注册表的存根。
     * @param host 远程注册表主机名
     * @param port 远程注册表端口
     * @return 远程注册表存根
     * @throws RemoteException 如果发生远程错误
     * @throws UnknownHostException 如果无法解析给定的主机名
     */
    @Deprecated
    Registry registryStub(String host, int port)
        throws RemoteException, UnknownHostException;

    /**
     * 在指定的端口上构建并导出一个注册表。
     * 端口必须是非零的。
     *
     * @deprecated 没有替代品。从 Java 2 平台 v1.2 开始，RMI 不再使用 <code>RegistryHandler</code> 来获取注册表的实现。
     * @param port 导出注册表的端口
     * @return 注册表存根
     * @throws RemoteException 如果发生远程错误
     */
    @Deprecated
    Registry registryImpl(int port) throws RemoteException;
}
