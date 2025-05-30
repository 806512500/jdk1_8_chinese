/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

/**
 * <p> 当 AccessController 检测到请求访问（例如文件系统或网络等关键系统资源）被拒绝时，抛出此异常。
 *
 * <p> 拒绝访问的原因可能各不相同。例如，请求的权限可能类型不正确，包含无效值，或者根据安全策略不允许访问。在抛出异常时，应尽可能提供此类信息。
 *
 * @author Li Gong
 * @author Roland Schemers
 */

public class AccessControlException extends SecurityException {

    private static final long serialVersionUID = 5138225684096988535L;

    // 导致异常被抛出的权限。
    private Permission perm;

    /**
     * 使用指定的详细消息构造一个 {@code AccessControlException}。
     *
     * @param   s   详细消息。
     */
    public AccessControlException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和导致异常的请求权限构造一个 {@code AccessControlException}。
     *
     * @param   s   详细消息。
     * @param   p   导致异常的权限。
     */
    public AccessControlException(String s, Permission p) {
        super(s);
        perm = p;
    }

    /**
     * 获取与此异常关联的 Permission 对象，如果没有相应的 Permission 对象，则返回 null。
     *
     * @return Permission 对象。
     */
    public Permission getPermission() {
        return perm;
    }
}
