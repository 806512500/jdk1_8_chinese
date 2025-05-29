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

import java.rmi.Remote;

/**
 * <code>Skeleton</code> 接口仅由 RMI 实现使用。
 *
 * <p> 每个版本 1.1（以及使用 <code>rmic -vcompat</code> 在 1.2 中生成的与版本 1.1 兼容的骨架）由 rmic 存根编译器生成的骨架类都实现了此接口。远程对象的骨架是服务器端实体，用于调度调用到实际的远程对象实现。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @deprecated 没有替代品。从 Java 2 平台 v1.2 及更高版本开始，远程方法调用不再需要骨架。
 */
@Deprecated
public interface Skeleton {
    /**
     * 反序列化参数，调用实际的远程对象实现，并序列化返回值或任何异常。
     *
     * @param obj 要调度调用的远程实现
     * @param theCall 代表远程调用的对象
     * @param opnum 操作号
     * @param hash 存根/骨架接口哈希
     * @exception java.lang.Exception 如果发生一般异常。
     * @since JDK1.1
     * @deprecated 没有替代品
     */
    @Deprecated
    void dispatch(Remote obj, RemoteCall theCall, int opnum, long hash)
        throws Exception;

    /**
     * 返回骨架支持的操作。
     * @return 骨架支持的操作
     * @since JDK1.1
     * @deprecated 没有替代品
     */
    @Deprecated
    Operation[] getOperations();
}
