/*
 * Copyright (c) 1996, 1999, Oracle and/or its affiliates. All rights reserved.
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
package java.rmi.dgc;

import java.rmi.*;
import java.rmi.server.ObjID;

/**
 * DGC 抽象用于分布式垃圾收集算法的服务器端。此接口包含两个方法：dirty 和 clean。当远程引用在客户端（通过其 VMID 指示）中解组时，会调用 dirty 方法。当客户端中不再存在对远程引用的引用时，会调用相应的 clean 方法。失败的 dirty 调用必须安排一个强 clean 调用，以便保留调用的序列号，以检测分布式垃圾收集器接收到的顺序错误的调用。
 *
 * 客户端持有的远程对象引用租期由客户端持有。租期从接收到 dirty 调用时开始。客户端有责任在租期到期前通过进行额外的 dirty 调用来续租其持有的远程引用。如果客户端在租期到期前未续租，分布式垃圾收集器将假设该客户端不再引用该远程对象。
 *
 * @author Ann Wollrath
 */
public interface DGC extends Remote {

    /**
     * dirty 调用请求与对象标识符数组 'ids' 中包含的对象引用相关的远程对象引用的租期。'lease' 包含客户端的唯一 VM 标识符 (VMID) 和请求的租期。对于本地 VM 中导出的每个远程对象，垃圾收集器维护一个引用列表——持有引用的客户端列表。如果授予租期，垃圾收集器会将客户端的 VMID 添加到 'ids' 中指示的每个远程对象的引用列表中。'sequenceNum' 参数是一个用于检测和丢弃晚到的垃圾收集器调用的序列号。对于每次后续的垃圾收集器调用，序列号应始终增加。
     *
     * 有些客户端无法生成 VMID，因为 VMID 是一个包含主机地址的通用唯一标识符，而一些客户端由于安全限制无法获取主机地址。在这种情况下，客户端可以使用 null 作为 VMID，分布式垃圾收集器将为客户端分配一个 VMID。
     *
     * dirty 调用返回一个包含使用的 VMID 和授予的远程引用租期的 Lease 对象（服务器可能会决定授予比客户端请求的更短的租期）。客户端必须使用垃圾收集器使用的 VMID，以便在客户端丢弃远程对象引用时进行相应的 clean 调用。
     *
     * 客户端 VM 仅需对 VM 中引用的每个远程引用进行一次初始的 dirty 调用（即使它对同一个远程对象有多个引用）。客户端还必须在租期到期前进行 dirty 调用以续租远程引用。当客户端不再引用特定的远程对象时，必须为与引用关联的对象 ID 安排一个 clean 调用。
     *
     * @param ids 要标记为被调用客户端引用的对象的 ID
     * @param sequenceNum 序列号
     * @param lease 请求的租期
     * @return 授予的租期
     * @throws RemoteException 如果 dirty 调用失败
     */
    Lease dirty(ObjID[] ids, long sequenceNum, Lease lease)
        throws RemoteException;

    /**
     * clean 调用从 'ids' 中指示的每个远程对象的引用列表中移除 'vmid'。序列号用于检测晚到的 clean 调用。如果参数 'strong' 为 true，则 clean 调用是由于失败的 dirty 调用，因此需要记住客户端 'vmid' 的序列号。
     *
     * @param ids 要标记为未被调用客户端引用的对象的 ID
     * @param sequenceNum 序列号
     * @param vmid 客户端 VMID
     * @param strong 是否进行 'strong' clean 调用
     * @throws RemoteException 如果 clean 调用失败
     */
    void clean(ObjID[] ids, long sequenceNum, VMID vmid, boolean strong)
        throws RemoteException;
}
