/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file.attribute;

import java.io.IOException;

/**
 * 一个用于通过名称查找用户和组主体的对象。一个 {@link UserPrincipal} 表示一个可用于确定文件系统中对象访问权限的身份。一个 {@link GroupPrincipal} 表示一个 <em>组身份</em>。
 * 一个 {@code UserPrincipalLookupService} 定义了通过名称或组名称（通常是用户或帐户名称）查找身份的方法。名称和组名称是否区分大小写取决于实现。
 * 组的定义是实现特定的，但通常组表示一个为管理目的创建的身份，以确定组成员的访问权限。特别是，名称和组的 <em>命名空间</em> 是否相同或不同取决于实现。
 * 为确保跨平台的一致性和正确行为，建议将此 API 视为命名空间是不同的。换句话说，应使用 {@link #lookupPrincipalByName lookupPrincipalByName} 查找用户，
 * 并使用 {@link #lookupPrincipalByGroupName lookupPrincipalByGroupName} 查找组。
 *
 * @since 1.7
 *
 * @see java.nio.file.FileSystem#getUserPrincipalLookupService
 */

public abstract class UserPrincipalLookupService {

    /**
     * 初始化此类的新实例。
     */
    protected UserPrincipalLookupService() {
    }

    /**
     * 通过名称查找用户主体。
     *
     * @param   name
     *          要查找的用户主体的字符串表示形式
     *
     * @return  一个用户主体
     *
     * @throws  UserPrincipalNotFoundException
     *          主体不存在
     * @throws  IOException
     *          发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全管理器，它会检查 {@link RuntimePermission}<tt>("lookupUserInformation")</tt>
     */
    public abstract UserPrincipal lookupPrincipalByName(String name)
        throws IOException;

    /**
     * 通过组名称查找组主体。
     *
     * <p> 如果实现不支持任何组的概念，则此方法始终抛出 {@link UserPrincipalNotFoundException}。如果用户帐户和组的命名空间相同，则此方法与调用 {@link #lookupPrincipalByName lookupPrincipalByName} 相同。
     *
     * @param   group
     *          要查找的组的字符串表示形式
     *
     * @return  一个组主体
     *
     * @throws  UserPrincipalNotFoundException
     *          主体不存在或不是组
     * @throws  IOException
     *          发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全管理器，它会检查 {@link RuntimePermission}<tt>("lookupUserInformation")</tt>
     */
    public abstract GroupPrincipal lookupPrincipalByGroupName(String group)
        throws IOException;
}
