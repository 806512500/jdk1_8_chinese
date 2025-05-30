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
 * <code>RMISecurityException</code> 表示在执行 <code>java.rmi.RMISecurityManager</code>
 * 的方法之一时发生的安全异常。
 *
 * @author  Roger Riggs
 * @since   JDK1.1
 * @deprecated 使用 {@link java.lang.SecurityException} 代替。
 * 应用代码不应直接引用此类，且 <code>RMISecurityManager</code> 不再抛出此 <code>java.lang.SecurityException</code> 的子类。
 */
@Deprecated
public class RMISecurityException extends java.lang.SecurityException {

    /* 表示与 JDK 1.1.x 版本类的兼容性 */
     private static final long serialVersionUID = -8433406075740433514L;

    /**
     * 使用详细消息构造 <code>RMISecurityException</code>。
     * @param name 详细消息
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    public RMISecurityException(String name) {
        super(name);
    }

    /**
     * 使用详细消息构造 <code>RMISecurityException</code>。
     * @param name 详细消息
     * @param arg 被忽略
     * @since JDK1.1
     * @deprecated 没有替代
     */
    @Deprecated
    public RMISecurityException(String name, String arg) {
        this(name);
    }
}
