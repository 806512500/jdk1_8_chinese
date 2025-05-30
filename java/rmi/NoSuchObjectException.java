/*
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
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
 * 当尝试调用已不再存在于远程虚拟机中的对象的方法时，将抛出 <code>NoSuchObjectException</code>。如果在尝试调用远程对象的方法时发生 <code>NoSuchObjectException</code>，调用可以重新传输并仍然保持 RMI 的“最多一次”调用语义。
 *
 * <code>NoSuchObjectException</code> 也由 <code>java.rmi.server.RemoteObject.toStub</code> 方法和 <code>java.rmi.server.UnicastRemoteObject</code> 以及 <code>java.rmi.activation.Activatable</code> 的 <code>unexportObject</code> 方法抛出。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @see     java.rmi.server.RemoteObject#toStub(Remote)
 * @see     java.rmi.server.UnicastRemoteObject#unexportObject(Remote,boolean)
 * @see     java.rmi.activation.Activatable#unexportObject(Remote,boolean)
 */
public class NoSuchObjectException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本类的兼容性 */
    private static final long serialVersionUID = 6619395951570472985L;

    /**
     * 使用指定的详细消息构造 <code>NoSuchObjectException</code>。
     *
     * @param s 详细消息
     * @since   JDK1.1
     */
    public NoSuchObjectException(String s) {
        super(s);
    }
}
