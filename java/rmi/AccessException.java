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
 * <code>AccessException</code> 由 <code>java.rmi.Naming</code> 类的某些方法（具体来说是 <code>bind</code>、
 * <code>rebind</code> 和 <code>unbind</code>）以及 <code>java.rmi.activation.ActivationSystem</code> 接口的方法抛出，
 * 以指示调用者没有权限执行方法调用请求的操作。如果方法是从非本地主机调用的，则抛出 <code>AccessException</code>。
 *
 * @author  Ann Wollrath
 * @author  Roger Riggs
 * @since   JDK1.1
 * @see     java.rmi.Naming
 * @see     java.rmi.activation.ActivationSystem
 */
public class AccessException extends java.rmi.RemoteException {

    /* 表示与 JDK 1.1.x 版本的类兼容 */
     private static final long serialVersionUID = 6314925228044966088L;

    /**
     * 使用指定的详细消息构造 <code>AccessException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     */
    public AccessException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>AccessException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public AccessException(String s, Exception ex) {
        super(s, ex);
    }
}
