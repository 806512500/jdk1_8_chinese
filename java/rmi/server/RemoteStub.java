/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * {@code RemoteStub} 类是静态生成的客户端
 * 存根的公共超类，提供支持广泛远程引用语义的框架。存根对象是代理，支持
 * 与实际远程对象实现定义的远程接口完全相同的远程接口。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 *
 * @deprecated 静态生成的存根已过时，因为存根是动态生成的。请参阅 {@link UnicastRemoteObject}
 * 以获取有关动态存根生成的信息。
 */
@Deprecated
abstract public class RemoteStub extends RemoteObject {

    /** 表示与 JDK 1.1.x 版本类的兼容性 */
    private static final long serialVersionUID = -1585587260594494182L;

    /**
     * 构造 {@code RemoteStub}。
     */
    protected RemoteStub() {
        super();
    }

    /**
     * 使用指定的远程引用构造 {@code RemoteStub}。
     *
     * @param ref 远程引用
     * @since JDK1.1
     */
    protected RemoteStub(RemoteRef ref) {
        super(ref);
    }

    /**
     * 抛出 {@link UnsupportedOperationException}。
     *
     * @param stub 远程存根
     * @param ref 远程引用
     * @throws UnsupportedOperationException 始终抛出
     * @since JDK1.1
     * @deprecated 没有替代方法。 {@code setRef} 方法
     * 用于设置远程存根的远程引用。这是不必要的，因为可以通过使用
     * {@link #RemoteStub(RemoteRef)} 构造函数创建和初始化带有远程引用的 {@code RemoteStub}。
     */
    @Deprecated
    protected static void setRef(RemoteStub stub, RemoteRef ref) {
        throw new UnsupportedOperationException();
    }
}
