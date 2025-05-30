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

package java.lang.reflect;

/**
 * 反射操作的权限类。
 * <P>
 * 下表提供了该权限允许的操作的简要描述，以及授予代码此权限的风险。
 *
 * <table border=1 cellpadding=5 summary="表显示权限目标名称、允许的操作和相关风险">
 * <tr>
 * <th>权限目标名称</th>
 * <th>允许的操作</th>
 * <th>允许此权限的风险</th>
 * </tr>
 *
 * <tr>
 *   <td>suppressAccessChecks</td>
 *   <td>能够抑制类中字段和方法的标准 Java 语言访问检查；不仅允许访问公共成员，还允许访问默认（包）访问、受保护和私有成员。</td>
 *   <td>这很危险，因为通常不可用的信息（可能是机密的）和方法将对恶意代码开放。</td>
 * </tr>
 * <tr>
 *   <td>newProxyInPackage.{包名}</td>
 *   <td>能够在指定包中创建代理实例，该代理类实现的非公共接口。</td>
 *   <td>这使代码能够访问通常没有访问权限的包中的类，并且动态代理类位于系统保护域中。恶意代码可能会使用这些类来尝试破坏系统中的安全性。</td>
 * </tr>
 *
 * </table>
 *
 * @see java.security.Permission
 * @see java.security.BasicPermission
 * @see AccessibleObject
 * @see Field#get
 * @see Field#set
 * @see Method#invoke
 * @see Constructor#newInstance
 * @see Proxy#newProxyInstance
 *
 * @since 1.2
 */
public final
class ReflectPermission extends java.security.BasicPermission {

    private static final long serialVersionUID = 7412737110241507485L;

    /**
     * 使用指定的名称构造 ReflectPermission。
     *
     * @param name ReflectPermission 的名称
     *
     * @throws NullPointerException 如果 {@code name} 为 {@code null}。
     * @throws IllegalArgumentException 如果 {@code name} 为空。
     */
    public ReflectPermission(String name) {
        super(name);
    }

    /**
     * 使用指定的名称和操作构造 ReflectPermission。
     * 操作应为 null；它们被忽略。
     *
     * @param name ReflectPermission 的名称
     *
     * @param actions 应为 null
     *
     * @throws NullPointerException 如果 {@code name} 为 {@code null}。
     * @throws IllegalArgumentException 如果 {@code name} 为空。
     */
    public ReflectPermission(String name, String actions) {
        super(name, actions);
    }

}
