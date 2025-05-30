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

package java.rmi;

/**
 * 从在 JDK 1.1 上运行的服务器执行时，当在服务器上处理调用时抛出 <code>RuntimeException</code>，
 * 无论是反序列化参数、执行远程方法本身还是序列化返回值时，都会抛出 <code>ServerRuntimeException</code>。
 *
 * <code>ServerRuntimeException</code> 实例包含作为其原因的原始 <code>RuntimeException</code>。
 *
 * <p>从在 Java 2 平台 v1.2 或更高版本上执行的服务器不会抛出 <code>ServerRuntimeException</code>。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @deprecated 没有替代
 */
@Deprecated
public class ServerRuntimeException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本类的兼容性 */
    private static final long serialVersionUID = 7054464920481467219L;

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>ServerRuntimeException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @deprecated 没有替代
     * @since JDK1.1
     */
    @Deprecated
    public ServerRuntimeException(String s, Exception ex) {
        super(s, ex);
    }
}
