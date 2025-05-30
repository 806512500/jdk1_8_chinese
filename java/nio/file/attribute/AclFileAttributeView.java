/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.file.*;
import java.util.List;
import java.io.IOException;

/**
 * 一个支持读取或更新文件的访问控制列表（ACL）或文件所有者属性的文件属性视图。
 *
 * <p> ACL 用于指定对文件系统对象的访问权限。ACL 是一个有序的 {@link AclEntry 访问控制条目} 列表，每个条目指定一个 {@link UserPrincipal} 和该用户主体的访问级别。此文件属性视图定义了 {@link #getAcl() getAcl} 和 {@link
 * #setAcl(List) setAcl} 方法，以读取和写入基于 <a href="http://www.ietf.org/rfc/rfc3530.txt"><i>RFC&nbsp;3530:
 * 网络文件系统（NFS）版本 4 协议</i></a> 中指定的 ACL 模型的 ACL。此文件属性视图旨在用于支持 NFSv4 ACL 模型或具有与 NFSv4 ACL 模型和文件系统使用的 ACL 模型之间有 <em>明确定义的</em> 映射的文件系统实现。这种映射的具体细节是实现依赖的，因此未指定。
 *
 * <p> 该类还扩展了 {@code FileOwnerAttributeView} 以定义获取和设置文件所有者的方法。
 *
 * <p> 当文件系统提供对一组 {@link FileStore 文件系统} 的访问，而这些文件系统不是同质的时，只有某些文件系统可能支持 ACL。可以使用 {@link FileStore#supportsFileAttributeView
 * supportsFileAttributeView} 方法测试文件系统是否支持 ACL。
 *
 * <h2>互操作性</h2>
 *
 * RFC&nbsp;3530 允许在支持 POSIX 定义的访问权限的平台上使用特殊用户身份。这些特殊用户身份是 "{@code OWNER@}"、"{@code GROUP@}" 和 "{@code EVERYONE@}"。当同时支持 {@code AclFileAttributeView} 和 {@link PosixFileAttributeView}
 * 时，这些特殊用户身份可以包含在读取或写入的 ACL {@link
 * AclEntry 条目} 中。可以使用文件系统的 {@link
 * UserPrincipalLookupService} 通过调用 {@link
 * UserPrincipalLookupService#lookupPrincipalByName lookupPrincipalByName}
 * 方法来获取表示这些特殊身份的 {@link UserPrincipal}。
 *
 * <p> <b>使用示例：</b>
 * 假设我们希望向现有 ACL 添加一个条目以授予 "joe" 访问权限：
 * <pre>
 *     // 查找 "joe"
 *     UserPrincipal joe = file.getFileSystem().getUserPrincipalLookupService()
 *         .lookupPrincipalByName("joe");
 *
 *     // 获取视图
 *     AclFileAttributeView view = Files.getFileAttributeView(file, AclFileAttributeView.class);
 *
 *     // 创建 ACE 以授予 "joe" 读取权限
 *     AclEntry entry = AclEntry.newBuilder()
 *         .setType(AclEntryType.ALLOW)
 *         .setPrincipal(joe)
 *         .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.READ_ATTRIBUTES)
 *         .build();
 *
 *     // 读取 ACL，插入 ACE，重新写入 ACL
 *     List&lt;AclEntry&gt; acl = view.getAcl();
 *     acl.add(0, entry);   // 在任何 DENY 条目前插入
 *     view.setAcl(acl);
 * </pre>
 *
 * <h2> 动态访问 </h2>
 * <p> 当需要动态访问文件属性时，此属性视图支持的属性如下：
 * <blockquote>
 * <table border="1" cellpadding="8" summary="支持的属性">
 *   <tr>
 *     <th> 名称 </th>
 *     <th> 类型 </th>
 *   </tr>
 *   <tr>
 *     <td> "acl" </td>
 *     <td> {@link List}&lt;{@link AclEntry}&gt; </td>
 *   </tr>
 *   <tr>
 *     <td> "owner" </td>
 *     <td> {@link UserPrincipal} </td>
 *   </tr>
 * </table>
 * </blockquote>
 *
 * <p> 可以使用 {@link Files#getAttribute getAttribute} 方法读取 ACL 或所有者属性，就像调用 {@link #getAcl getAcl} 或
 * {@link #getOwner getOwner} 方法一样。
 *
 * <p> 可以使用 {@link Files#setAttribute setAttribute} 方法更新 ACL 或所有者属性，就像调用 {@link #setAcl setAcl}
 * 或 {@link #setOwner setOwner} 方法一样。
 *
 * <h2> 创建文件时设置 ACL </h2>
 *
 * <p> 支持此属性视图的实现可能还支持在创建文件或目录时设置初始 ACL。初始 ACL
 * 可以作为 {@link FileAttribute} 提供给 {@link Files#createFile createFile} 或 {@link
 * Files#createDirectory createDirectory} 等方法，其 {@link FileAttribute#name 名称} 为 {@code "acl:acl"}，其 {@link FileAttribute#value
 * 值} 是 {@code AclEntry} 对象的列表。
 *
 * <p> 如果实现支持与 NFSv4 定义的 ACL 模型不同的 ACL 模型，则在创建文件时设置初始 ACL 必须将 ACL 转换为文件系统支持的模型。创建文件的方法应拒绝（通过抛出 {@link IOException IOException}）
 * 任何因转换而导致文件安全性降低的创建尝试。
 *
 * @since 1.7
 */

public interface AclFileAttributeView
    extends FileOwnerAttributeView
{
    /**
     * 返回属性视图的名称。此类属性视图的名称为 {@code "acl"}。
     */
    @Override
    String name();

    /**
     * 读取访问控制列表。
     *
     * <p> 当文件系统使用的 ACL 模型与 NFSv4 定义的 ACL 模型不同时，此方法返回一个转换为 NFSv4 ACL 模型的 ACL。
     *
     * <p> 返回的列表是可修改的，以便于对现有 ACL 进行更改。使用 {@link #setAcl setAcl} 方法更新文件的 ACL 属性。
     *
     * @return  一个有序的 {@link AclEntry 条目} 列表，表示 ACL
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，安装了安全管理器，并且它拒绝 {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          或其 {@link SecurityManager#checkRead(String) checkRead} 方法拒绝读取文件的访问权限。
     */
    List<AclEntry> getAcl() throws IOException;

    /**
     * 更新（替换）访问控制列表。
     *
     * <p> 如果文件系统支持访问控制列表，并且它使用的 ACL 模型与 NFSv4 定义的 ACL 模型不同，那么此方法必须将 ACL 转换为文件系统支持的模型。此方法应拒绝（通过抛出 {@link IOException IOException}）
     * 任何尝试写入一个看似比实际更新后的 ACL 更安全的 ACL。如果实现不支持 {@link AclEntryType#AUDIT} 或 {@link
     * AclEntryType#ALARM} 条目的映射，那么此方法在写入 ACL 时将忽略这些条目。
     *
     * <p> 如果 ACL 条目包含一个与该属性视图的提供程序不同的 {@link AclEntry#principal 用户主体}，则抛出 {@link ProviderMismatchException}。其他验证（如果有）是实现依赖的。
     *
     * <p> 如果文件系统支持其他与安全相关的文件属性（例如文件的 {@link PosixFileAttributes#permissions
     * 访问权限}），那么更新访问控制列表也可能导致这些与安全相关的属性被更新。
     *
     * @param   acl
     *          新的访问控制列表
     *
     * @throws  IOException
     *          如果发生 I/O 错误或 ACL 无效
     * @throws  SecurityException
     *          在默认提供程序的情况下，安装了安全管理器，并且它拒绝 {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          或其 {@link SecurityManager#checkWrite(String) checkWrite} 方法拒绝写入文件的访问权限。
     */
    void setAcl(List<AclEntry> acl) throws IOException;
}
