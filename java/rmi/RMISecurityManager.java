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

package java.rmi;

import java.security.*;

/**
 * {@code RMISecurityManager} 实现了一个与 {@link SecurityManager} 实现的策略相同的策略。RMI 应用程序
 * 应该使用 {@code SecurityManager} 类或另一个合适的
 * {@code SecurityManager} 实现，而不是这个类。RMI 的类加载器只有在设置了安全经理的情况下才会从远程位置下载类。
 *
 * @implNote
 * <p>通常，小程序在一个已经具有安全经理的容器中运行，因此通常不需要小程序设置安全经理。如果你有一个独立的应用程序，你可能需要设置一个
 * {@code SecurityManager} 以启用类下载。这可以通过在代码中添加以下内容来完成。（它需要在 RMI 从远程主机下载代码之前执行，因此它最有可能需要出现在应用程序的
 * {@code main} 方法中。）
 *
 * <pre>{@code
 *    if (System.getSecurityManager() == null) {
 *        System.setSecurityManager(new SecurityManager());
 *    }
 * }</pre>
 *
 * @author  Roger Riggs
 * @author  Peter Jones
 * @since JDK1.1
 * @deprecated 使用 {@link SecurityManager} 代替。
 */
@Deprecated
public class RMISecurityManager extends SecurityManager {

    /**
     * 构造一个新的 {@code RMISecurityManager}。
     * @since JDK1.1
     */
    public RMISecurityManager() {
    }
}
