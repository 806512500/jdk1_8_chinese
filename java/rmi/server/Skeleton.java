/*
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
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

import java.rmi.Remote;

/**
 * <code>Skeleton</code> 接口仅由 RMI 实现使用。
 *
 * <p> 每个版本 1.1（以及使用 <code>rmic -vcompat</code> 在 1.2 中生成的与版本 1.1 兼容的骨架）由 rmic 存根编译器生成的骨架类都实现了此接口。远程对象的骨架是服务器端实体，用于调度对实际远程对象实现的调用。
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
     * @param opnum 操作编号
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
