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

/**
 * <code>ActivationInstantiator</code> 负责创建“可激活”对象的实例。一个具体的 <code>ActivationGroup</code> 子类实现 <code>newInstance</code>
 * 方法来处理在组内创建对象。
 *
 * @author      Ann Wollrath
 * @see         ActivationGroup
 * @since       1.2
 */
public interface ActivationInstantiator extends Remote {

   /**
    * 激活器调用实例化器的 <code>newInstance</code> 方法，以便在该组中重新创建具有激活标识符 <code>id</code> 和描述符 <code>desc</code> 的对象。
    * 实例化器负责以下任务： <ul>
    *
    * <li> 使用描述符的 <code>getClassName</code> 方法确定对象的类，
    *
    * <li> 从描述符中获取的代码位置（使用 <code>getLocation</code> 方法）加载类，
    *
    * <li> 通过调用对象类的特殊“激活”构造函数来创建类的实例，该构造函数接受两个参数：对象的 <code>ActivationID</code> 和包含对象特定初始化数据的 <code>MarshalledObject</code>，
    *
    * <li> 返回一个包含所创建远程对象存根的 <code>MarshalledObject</code> </ul>
    *
    * @param id 对象的激活标识符
    * @param desc 对象的描述符
    * @return 包含远程对象存根的序列化表示的 <code>MarshalledObject</code>
    * @exception ActivationException 如果对象激活失败
    * @exception RemoteException 如果远程调用失败
    * @since 1.2
    */
    public MarshalledObject<? extends Remote> newInstance(ActivationID id,
                                                          ActivationDesc desc)
        throws ActivationException, RemoteException;
}
