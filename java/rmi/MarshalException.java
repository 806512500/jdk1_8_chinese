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
 * 当在远程方法调用期间对远程调用头、参数或返回值进行序列化时发生 <code>java.io.IOException</code>，将抛出 <code>MarshalException</code>。
 * 如果接收方不支持发送方的协议版本，也会抛出 <code>MarshalException</code>。
 *
 * <p>如果在远程方法调用期间发生 <code>MarshalException</code>，调用可能或可能没有到达服务器。如果调用确实到达了服务器，参数可能已经被反序列化。
 * 在发生 <code>MarshalException</code> 后，调用不能重新传输并可靠地保持“最多一次”调用语义。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 */
public class MarshalException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本类的兼容性 */
    private static final long serialVersionUID = 6223554758134037936L;

    /**
     * 使用指定的详细消息构造 <code>MarshalException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     */
    public MarshalException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>MarshalException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public MarshalException(String s, Exception ex) {
        super(s, ex);
    }
}
