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
import java.rmi.activation.UnknownGroupException;
import java.rmi.activation.UnknownObjectException;

/**
 * <code>ActivationMonitor</code> 特定于一个 <code>ActivationGroup</code>，并在组通过调用
 * <code>ActivationSystem.activeGroup</code> 被报告为活动时获得（这是内部完成的）。激活组负责通知其
 * <code>ActivationMonitor</code>，当其对象变为活动或非活动状态，或者组整体变为非活动状态时。
 *
 * @author      Ann Wollrath
 * @see         Activator
 * @see         ActivationSystem
 * @see         ActivationGroup
 * @since       1.2
 */
public interface ActivationMonitor extends Remote {

   /**
     * 激活组在其组中的对象变为非活动（停用）时调用其监视器的
     * <code>inactiveObject</code> 方法。激活组通过调用激活组的
     * <code>inactiveObject</code> 方法发现其 VM 中的某个对象（它参与激活的对象）
     * 不再活动。 <p>
     *
     * <code>inactiveObject</code> 调用通知 <code>ActivationMonitor</code>，它持有的具有激活标识符
     * <code>id</code> 的远程对象引用不再有效。监视器认为与 <code>id</code> 关联的引用是陈旧的引用。
     * 由于引用被认为是陈旧的，后续对相同激活标识符的 <code>activate</code> 调用将导致重新激活远程对象。<p>
     *
     * @param id 对象的激活标识符
     * @exception UnknownObjectException 如果对象未知
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public void inactiveObject(ActivationID id)
        throws UnknownObjectException, RemoteException;

    /**
     * 通知对象现在处于活动状态。 <code>ActivationGroup</code>
     * 通过其他方式（即，对象注册并“激活”自身）使其组中的对象变为活动状态时，会通知其监视器。
     *
     * @param id 活动对象的 id
     * @param obj 对象存根的序列化形式
     * @exception UnknownObjectException 如果对象未知
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public void activeObject(ActivationID id,
                             MarshalledObject<? extends Remote> obj)
        throws UnknownObjectException, RemoteException;

    /**
     * 通知组现在处于非活动状态。组将在后续请求激活组内的对象时被重新创建。当组中的所有对象报告它们处于非活动状态时，组变为非活动状态。
     *
     * @param id 组的 id
     * @param incarnation 组的化身编号
     * @exception UnknownGroupException 如果组未知
     * @exception RemoteException 如果远程调用失败
     * @since 1.2
     */
    public void inactiveGroup(ActivationGroupID id,
                              long incarnation)
        throws UnknownGroupException, RemoteException;

}
