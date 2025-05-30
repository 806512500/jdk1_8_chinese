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

package java.rmi.server;

import java.rmi.RemoteException;

/**
 * 当导出的远程对象对应的 <code>Skeleton</code> 未找到时，将抛出 <code>SkeletonNotFoundException</code>。
 * 自 Java 2 平台 v1.2 及更高版本起，骨架已不再需要，因此此异常永远不会抛出。
 *
 * @since   JDK1.1
 * @deprecated 没有替代方案。自 Java 2 平台 v1.2 及更高版本起，远程方法调用已不再需要骨架。
 */
@Deprecated
public class SkeletonNotFoundException extends RemoteException {

    /* 表示与 JDK 1.1.x 版本类的兼容性 */
    private static final long serialVersionUID = -7860299673822761231L;

    /**
     * 使用指定的详细消息构造 <code>SkeletonNotFoundException</code>。
     *
     * @param s 详细消息。
     * @since JDK1.1
     */
    public SkeletonNotFoundException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>SkeletonNotFoundException</code>。
     *
     * @param s 详细消息。
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public SkeletonNotFoundException(String s, Exception ex) {
        super(s, ex);
    }
}
