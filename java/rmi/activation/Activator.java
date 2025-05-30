/*
 * Copyright (c) 1997, 2005, Oracle and/or its affiliates. All rights reserved.
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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.activation.UnknownObjectException;

/**
 * <code>Activator</code> 促进远程对象激活。一个“故障”远程引用调用激活器的
 * <code>activate</code> 方法以获取一个“活动”引用到一个“可激活”远程对象。当接收到激活请求时，
 * 激活器查找激活标识符 <code>id</code> 的激活描述符，确定对象应在哪个组中激活，并通过组的
 * <code>ActivationInstantiator</code>（通过调用 <code>newInstance</code> 方法）启动对象的重新创建。
 * 激活器根据需要启动激活组的执行。例如，如果特定组标识符的激活组尚未执行，激活器将启动该组的 VM。
 * <p>
 *
 * <code>Activator</code> 与 <code>ActivationSystem</code> 密切合作，后者提供了在组内注册组和对象的手段，
 * 以及 <code>ActivationMonitor</code>，后者接收有关活动和非活动对象及非活动组的信息。<p>
 *
 * 激活器负责监控和检测激活组何时失败，以便它可以移除组和组内活动对象的陈旧远程引用。<p>
 *
 * @author      Ann Wollrath
 * @see         ActivationInstantiator
 * @see         ActivationGroupDesc
 * @see         ActivationGroupID
 * @since       1.2
 */
public interface Activator extends Remote {
    /**
     * 激活与激活标识符 <code>id</code> 关联的对象。如果激活器已经知道该对象是活动的，并且 <code>force</code> 为 false，
     * 则立即将带有“活动”引用的存根返回给调用者；否则，如果激活器不知道对应的远程对象是否活动，激活器将使用
     * （先前注册的）激活描述符信息来确定对象应在哪个组（VM）中激活。如果对象组描述符对应的
     * <code>ActivationInstantiator</code> 已经存在，激活器将调用该激活组的 <code>newInstance</code> 方法，
     * 将对象的 id 和描述符传递给它。<p>
     *
     * 如果对象组描述符的激活组尚不存在，激活器将启动一个 <code>ActivationInstantiator</code>（例如通过生成一个子进程）。
     * 当激活器接收到激活组的回调（通过 <code>ActivationSystem</code> 的 <code>activeGroup</code> 方法），
     * 指定激活组的引用时，激活器可以调用该激活实例化器的 <code>newInstance</code> 方法，将每个待处理的激活请求转发给激活组，
     * 并将结果（一个已序列化的远程对象引用，即存根）返回给调用者。<p>
     *
     * 注意，激活器接收的是一个“已序列化”的对象，而不是一个远程对象，这样激活器就不需要加载该对象的代码，
     * 或参与该对象的分布式垃圾回收。如果激活器保留了远程对象的强引用，激活器将阻止该对象在正常的分布式垃圾回收机制下被回收。<p>
     *
     * @param id 要激活的对象的激活标识符
     * @param force 如果为 true，激活器将联系组以获取远程对象的引用；如果为 false，允许返回缓存的值。
     * @return 远程对象（存根）的已序列化形式
     * @exception ActivationException 如果对象激活失败
     * @exception UnknownObjectException 如果对象未知（未注册）
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public MarshalledObject<? extends Remote> activate(ActivationID id,
                                                       boolean force)
        throws ActivationException, UnknownObjectException, RemoteException;

}
