/*
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi;

/**
 * <code>Remote</code> 接口用于标识其方法可以从非本地虚拟机调用的接口。任何远程对象都必须直接或间接实现此接口。
 * 只有在“远程接口”中指定的方法（即扩展 <code>java.rmi.Remote</code> 的接口）才能远程调用。
 *
 * <p>实现类可以实现任意数量的远程接口，并且可以扩展其他远程实现类。RMI 提供了一些方便类，远程对象实现可以扩展这些类，以简化远程对象的创建。这些类包括
 * <code>java.rmi.server.UnicastRemoteObject</code> 和
 * <code>java.rmi.activation.Activatable</code>。
 *
 * <p>有关 RMI 的完整详细信息，请参阅 <a
 href=../../../platform/rmi/spec/rmiTOC.html>RMI 规范</a>，该规范描述了 RMI API 和系统。
 *
 * @since   JDK1.1
 * @author  Ann Wollrath
 * @see     java.rmi.server.UnicastRemoteObject
 * @see     java.rmi.activation.Activatable
 */
public interface Remote {}
