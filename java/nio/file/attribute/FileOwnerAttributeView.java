/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * 一个支持读取或更新文件所有者的文件属性视图。此文件属性视图旨在用于支持表示文件所有者的文件属性的文件系统实现。通常，文件的所有者是创建文件的实体的身份。
 *
 * <p> 可以使用 {@link #getOwner getOwner} 或 {@link #setOwner setOwner} 方法来读取或更新文件的所有者。
 *
 * <p> 也可以使用 {@link java.nio.file.Files#getAttribute getAttribute} 和
 * {@link java.nio.file.Files#setAttribute setAttribute} 方法来读取或更新所有者。在这种情况下，所有者属性的名称为 {@code "owner"}，属性的值为
 * {@link UserPrincipal}。
 *
 * @since 1.7
 */

public interface FileOwnerAttributeView
    extends FileAttributeView
{
    /**
     * 返回属性视图的名称。此类属性视图的名称为 {@code "owner"}。
     */
    @Override
    String name();

    /**
     * 读取文件所有者。
     *
     * <p> 文件所有者是否可以是 {@link
     * GroupPrincipal 组} 是实现特定的。
     *
     * @return  文件所有者
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，并且它拒绝 {@link
     *          RuntimePermission}<tt>("accessUserInformation")</tt> 或其
     *          {@link SecurityManager#checkRead(String) checkRead} 方法拒绝读取文件的访问权限。
     */
    UserPrincipal getOwner() throws IOException;

    /**
     * 更新文件所有者。
     *
     * <p> 文件所有者是否可以是 {@link
     * GroupPrincipal 组} 是实现特定的。为了确保跨平台的一致和正确行为，建议仅将文件所有者设置为不是组的用户主体。
     *
     * @param   owner
     *          新的文件所有者
     *
     * @throws  IOException
     *          如果发生 I/O 错误，或者 {@code owner} 参数是组且此实现不支持将所有者设置为组
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，并且它拒绝 {@link
     *          RuntimePermission}<tt>("accessUserInformation")</tt> 或其
     *          {@link SecurityManager#checkWrite(String) checkWrite} 方法拒绝写入文件的访问权限。
     */
    void setOwner(UserPrincipal owner) throws IOException;
}
