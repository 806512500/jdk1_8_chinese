/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * ServerRef 表示远程对象实现的服务器端句柄。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @deprecated 没有替代品。此接口未使用且已过时。
 */
@Deprecated
public interface ServerRef extends RemoteRef {

    /** 表示与 JDK 1.1.x 版本类的兼容性。 */
    static final long serialVersionUID = -4557750989390278438L;

    /**
     * 为提供的远程对象创建客户端存根对象。
     * 如果调用成功完成，远程对象应能够接受来自客户端的传入调用。
     * @param obj 远程对象实现
     * @param data 导出对象所需的信息
     * @return 远程对象的存根
     * @exception RemoteException 如果在尝试导出对象时发生异常（例如，找不到存根类）
     * @since JDK1.1
     */
    RemoteStub exportObject(Remote obj, Object data)
        throws RemoteException;

    /**
     * 返回当前客户端的主机名。当从正在处理远程方法调用的线程调用时，
     * 返回客户端的主机名。
     * @return 客户端的主机名
     * @exception ServerNotActiveException 如果在处理远程方法调用之外调用
     * @since JDK1.1
     */
    String getClientHost() throws ServerNotActiveException;
}
