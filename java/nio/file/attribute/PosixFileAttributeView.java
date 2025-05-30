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
import java.util.Set;
import java.io.IOException;

/**
 * 提供了一个文件属性视图，该视图通常与实现 Portable Operating System Interface (POSIX) 家族标准的操作系统所使用的文件系统中的文件属性相关联。
 *
 * <p> 实现 POSIX 家族标准的操作系统通常使用具有文件 <em>所有者</em>、<em>组所有者</em> 和相关 <em>访问权限</em> 的文件系统。此文件属性视图提供了对这些属性的读写访问。
 *
 * <p> {@link #readAttributes() readAttributes} 方法用于读取文件的属性。文件的 {@link PosixFileAttributes#owner() 所有者} 由一个 {@link UserPrincipal} 表示，该身份用于访问控制。文件的 {@link PosixFileAttributes#group() 组所有者}，由一个 {@link GroupPrincipal} 表示，是组的身份，该组是为了管理目的创建的，用于确定组成员的访问权限。
 *
 * <p> {@link PosixFileAttributes#permissions() 访问权限} 属性是一组访问权限。此文件属性视图提供了对 {@link PosixFilePermission} 类定义的九个权限的访问。这九个权限位确定了文件所有者、组和其他人（其他人是指除所有者和组成员之外的身份）的 <em>读取</em>、<em>写入</em> 和 <em>执行</em> 访问权限。某些操作系统和文件系统可能提供额外的权限位，但此版本的类中未定义对这些其他位的访问。
 *
 * <p> <b>使用示例：</b>
 * 假设我们需要打印文件的所有者和访问权限：
 * <pre>
 *     Path file = ...
 *     PosixFileAttributes attrs = Files.getFileAttributeView(file, PosixFileAttributeView.class)
 *         .readAttributes();
 *     System.out.format("%s %s%n",
 *         attrs.owner().getName(),
 *         PosixFilePermissions.toString(attrs.permissions()));
 * </pre>
 *
 * <h2> 动态访问 </h2>
 * <p> 当需要动态访问文件属性时，此属性视图支持的属性由 {@link BasicFileAttributeView} 和 {@link FileOwnerAttributeView} 定义，并且还支持以下属性：
 * <blockquote>
 * <table border="1" cellpadding="8" summary="支持的属性">
 *   <tr>
 *     <th> 名称 </th>
 *     <th> 类型 </th>
 *   </tr>
 *  <tr>
 *     <td> "permissions" </td>
 *     <td> {@link Set}&lt;{@link PosixFilePermission}&gt; </td>
 *   </tr>
 *   <tr>
 *     <td> "group" </td>
 *     <td> {@link GroupPrincipal} </td>
 *   </tr>
 * </table>
 * </blockquote>
 *
 * <p> 可以使用 {@link Files#getAttribute getAttribute} 方法读取这些属性中的任何一个，或者读取 {@link BasicFileAttributeView} 定义的任何属性，就像调用 {@link #readAttributes readAttributes()} 方法一样。
 *
 * <p> 可以使用 {@link Files#setAttribute setAttribute} 方法更新文件的最后修改时间、最后访问时间或创建时间属性，这些属性由 {@link BasicFileAttributeView} 定义。还可以用于更新权限、所有者或组所有者，就像调用 {@link #setPermissions setPermissions}、{@link #setOwner setOwner} 和 {@link #setGroup setGroup} 方法一样。
 *
 * <h2> 设置初始权限 </h2>
 * <p> 支持此属性视图的实现可能还支持在创建文件或目录时设置初始权限。初始权限作为 {@link FileAttribute} 提供给 {@link Files#createFile createFile} 或 {@link Files#createDirectory createDirectory} 方法，该属性的 {@link FileAttribute#name 名称} 为 {@code "posix:permissions"}，其 {@link FileAttribute#value 值} 是权限集。以下示例使用 {@link PosixFilePermissions#asFileAttribute asFileAttribute} 方法在创建文件时构建一个 {@code FileAttribute}：
 *
 * <pre>
 *     Path path = ...
 *     Set&lt;PosixFilePermission&gt; perms =
 *         EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ);
 *     Files.createFile(path, PosixFilePermissions.asFileAttribute(perms));
 * </pre>
 *
 * <p> 当在文件创建时设置访问权限时，实际的权限值可能与属性对象的值不同。造成这种情况的原因是实现特定的。例如，在 UNIX 系统上，进程有一个 <em>umask</em>，这会影响新创建文件的权限位。如果实现支持设置访问权限，并且底层文件系统支持访问权限，则要求实际的访问权限值等于或小于提供给 {@link Files#createFile createFile} 或 {@link Files#createDirectory createDirectory} 方法的属性值。换句话说，文件可能比请求的更安全。
 *
 * @since 1.7
 */

public interface PosixFileAttributeView
    extends BasicFileAttributeView, FileOwnerAttributeView
{
    /**
     * 返回属性视图的名称。此类属性视图的名称为 {@code "posix"}。
     */
    @Override
    String name();

    /**
     * @throws  IOException                {@inheritDoc}
     * @throws  SecurityException
     *          在默认提供程序的情况下，安装了安全管理器，并且它拒绝 {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          或其 {@link SecurityManager#checkRead(String) checkRead} 方法拒绝对文件的读取访问。
     */
    @Override
    PosixFileAttributes readAttributes() throws IOException;

    /**
     * 更新文件权限。
     *
     * @param   perms
     *          新的权限集
     *
     * @throws  ClassCastException
     *          如果集合包含不是 {@code PosixFilePermission} 类型的元素
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，安装了安全管理器，并且它拒绝 {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          或其 {@link SecurityManager#checkWrite(String) checkWrite} 方法拒绝对文件的写入访问。
     */
    void setPermissions(Set<PosixFilePermission> perms) throws IOException;

    /**
     * 更新文件的组所有者。
     *
     * @param   group
     *          新的文件组所有者
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，安装了安全管理器，并且它拒绝 {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          或其 {@link SecurityManager#checkWrite(String) checkWrite} 方法拒绝对文件的写入访问。
     */
    void setGroup(GroupPrincipal group) throws IOException;
}
