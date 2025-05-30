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

package java.rmi.server;

import java.rmi.RemoteException;

/**
 * 当接收到的调用与可用的骨架不匹配时，将抛出此异常。这表明此接口中的远程方法名称或签名已更改，或者用于进行调用的存根类和接收调用的骨架不是由同一版本的存根编译器（<code>rmic</code>）生成的。
 *
 * @author  Roger Riggs
 * @since   JDK1.1
 * @deprecated 没有替代品。自 Java 2 平台 v1.2 及更高版本起，远程方法调用不再需要骨架。
 */
@Deprecated
public class SkeletonMismatchException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本类的兼容性 */
    private static final long serialVersionUID = -7780460454818859281L;

    /**
     * 构造一个带有指定详细消息的新 <code>SkeletonMismatchException</code>。
     *
     * @param s 详细消息
     * @since JDK1.1
     * @deprecated 没有替代品
     */
    @Deprecated
    public SkeletonMismatchException(String s) {
        super(s);
    }

}
