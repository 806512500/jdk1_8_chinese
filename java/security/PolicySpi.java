/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 此类定义了 {@code Policy} 类的 <i>服务提供程序接口</i> (<b>SPI</b>)。
 * 所有希望提供 Policy 实现的服务提供程序都必须实现此类中的所有抽象方法。
 *
 * <p> 此抽象类的子类实现必须提供一个公共构造函数，该构造函数接受一个 {@code Policy.Parameters}
 * 对象作为输入参数。如果此构造函数不理解 {@code Policy.Parameters} 输入，则必须抛出
 * IllegalArgumentException。
 *
 *
 * @since 1.6
 */

public abstract class PolicySpi {

    /**
     * 检查策略是否已授予 ProtectionDomain 权限。
     *
     * @param domain 要检查的 ProtectionDomain。
     *
     * @param permission 检查此权限是否已授予指定的域。
     *
     * @return 如果权限已授予域，则返回 true。
     */
    protected abstract boolean engineImplies
        (ProtectionDomain domain, Permission permission);

    /**
     * 刷新/重新加载策略配置。此方法的行为取决于实现。例如，调用基于文件的策略的 {@code refresh}
     * 将导致文件被重新读取。
     *
     * <p> 此方法的默认实现不执行任何操作。如果策略实现支持刷新操作，
     * 则应覆盖此方法。
     */
    protected void engineRefresh() { }

    /**
     * 返回包含授予指定 CodeSource 的权限集的 PermissionCollection 对象。
     *
     * <p> 此方法的默认实现返回 Policy.UNSUPPORTED_EMPTY_COLLECTION 对象。如果策略实现可以返回
     * 授予 CodeSource 的权限集，则可以覆盖此方法。
     *
     * @param codesource 返回的 PermissionCollection 已授予的 CodeSource。
     *
     * @return 授予指定 CodeSource 的权限集。如果支持此操作，返回的权限集必须是一个新的可变实例，
     *          并且必须支持异构权限类型。如果不支持此操作，则返回 Policy.UNSUPPORTED_EMPTY_COLLECTION。
     */
    protected PermissionCollection engineGetPermissions
                                        (CodeSource codesource) {
        return Policy.UNSUPPORTED_EMPTY_COLLECTION;
    }

    /**
     * 返回包含授予指定 ProtectionDomain 的权限集的 PermissionCollection 对象。
     *
     * <p> 此方法的默认实现返回 Policy.UNSUPPORTED_EMPTY_COLLECTION 对象。如果策略实现可以返回
     * 授予 ProtectionDomain 的权限集，则可以覆盖此方法。
     *
     * @param domain 返回的 PermissionCollection 已授予的 ProtectionDomain。
     *
     * @return 授予指定 ProtectionDomain 的权限集。如果支持此操作，返回的权限集必须是一个新的可变实例，
     *          并且必须支持异构权限类型。如果不支持此操作，则返回 Policy.UNSUPPORTED_EMPTY_COLLECTION。
     */
    protected PermissionCollection engineGetPermissions
                                        (ProtectionDomain domain) {
        return Policy.UNSUPPORTED_EMPTY_COLLECTION;
    }
}
