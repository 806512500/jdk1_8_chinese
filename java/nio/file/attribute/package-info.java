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

/**
 * 接口和类提供对文件和文件系统属性的访问。
 *
 * <blockquote><table cellspacing=1 cellpadding=0 summary="属性视图">
 * <tr><th align="left">属性视图</th><th align="left">描述</th></tr>
 * <tr><td valign=top><tt><i>{@link java.nio.file.attribute.AttributeView}</i></tt></td>
 *     <td>可以读取或更新文件系统中对象的非不透明值</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;<i>{@link java.nio.file.attribute.FileAttributeView}</i></tt></td>
 *     <td>可以读取或更新文件属性</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.file.attribute.BasicFileAttributeView}&nbsp;&nbsp;</i></tt></td>
 *     <td>可以读取或更新一组基本文件属性</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.file.attribute.PosixFileAttributeView}&nbsp;&nbsp;</i></tt></td>
 *     <td>可以读取或更新POSIX定义的文件属性</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.file.attribute.DosFileAttributeView}&nbsp;&nbsp;</i></tt></td>
 *     <td>可以读取或更新FAT文件属性</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.file.attribute.FileOwnerAttributeView}&nbsp;&nbsp;</i></tt></td>
 *     <td>可以读取或更新文件的所有者</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.file.attribute.AclFileAttributeView}&nbsp;&nbsp;</i></tt></td>
 *     <td>可以读取或更新访问控制列表</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.file.attribute.UserDefinedFileAttributeView}&nbsp;&nbsp;</i></tt></td>
 *     <td>可以读取或更新用户定义的文件属性</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.file.attribute.FileStoreAttributeView}</i></tt></td>
 *     <td>可以读取或更新文件系统属性</td></tr>
 * </table></blockquote>
 *
 * <p> 属性视图提供了文件系统中对象的非不透明值，或称为<em>元数据</em>的只读或可更新视图。
 * {@link java.nio.file.attribute.FileAttributeView}接口被其他几个接口扩展，这些接口提供了对特定文件属性集的视图。
 * 通过调用{@link java.nio.file.Files#getFileAttributeView}方法并使用<em>类型标记</em>来标识所需的视图，可以选择这些视图。
 * 视图也可以通过名称来标识。{@link java.nio.file.attribute.FileStoreAttributeView}接口提供了对文件存储属性的访问。
 * 通过调用{@link java.nio.file.FileStore#getFileStoreAttributeView}方法，可以获得给定类型的{@code FileStoreAttributeView}。
 *
 * <p> {@link java.nio.file.attribute.BasicFileAttributeView}类定义了方法，用于读取和更新许多文件系统中常见的<em>基本</em>文件属性集。
 *
 * <p> {@link java.nio.file.attribute.PosixFileAttributeView}接口扩展了{@code BasicFileAttributeView}，定义了方法来访问实现可移植操作系统接口（POSIX）标准系列的文件系统和操作系统中常用的文件属性。
 *
 * <p> {@link java.nio.file.attribute.DosFileAttributeView}类扩展了{@code BasicFileAttributeView}，定义了方法来访问文件分配表（FAT）等文件系统中支持的遗留“DOS”文件属性，这些文件系统通常用于消费设备。
 *
 * <p> {@link java.nio.file.attribute.AclFileAttributeView}类定义了方法来读取和写入访问控制列表（ACL）文件属性。此文件属性视图使用的ACL模型基于<a href="http://www.ietf.org/rfc/rfc3530.txt">
 * <i>RFC&nbsp;3530: 网络文件系统（NFS）版本4协议</i></a>中定义的模型。
 *
 * <p> 除了属性视图，此包还定义了在访问属性时使用的类和接口：
 *
 * <ul>
 *
 *   <li> {@link java.nio.file.attribute.UserPrincipal}和
 *   {@link java.nio.file.attribute.GroupPrincipal}接口表示身份或组身份。</li>
 *
 *   <li> {@link java.nio.file.attribute.UserPrincipalLookupService}
 *   接口定义了查找用户或组身份的方法。</li>
 *
 *   <li> {@link java.nio.file.attribute.FileAttribute}接口
 *   表示属性值，用于在创建文件系统中的对象时需要原子设置属性值的情况。</li>
 *
 * </ul>
 *
 *
 * <p> 除非另有说明，否则将<tt>null</tt>参数传递给此包中任何类或接口的构造函数或方法将导致抛出{@link
 * java.lang.NullPointerException NullPointerException}。
 *
 * @since 1.7
 */

package java.nio.file.attribute;
