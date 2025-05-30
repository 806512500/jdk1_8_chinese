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

import java.io.IOException;

/**
 * 提供对许多文件系统常见的 <em>基本集</em> 文件属性的视图。基本集文件属性由 {@link BasicFileAttributes} 接口定义，包括 <em>强制性</em> 和 <em>可选</em> 文件属性。

 * <p> 通过调用 {@link #readAttributes() readAttributes} 方法，可以作为 <em>批量操作</em> 从文件系统中检索文件属性。
 * 本类还定义了 {@link #setTimes setTimes} 方法来更新文件的时间属性。
 *
 * <p> 如果需要动态访问文件属性，此属性视图支持的属性名称和类型如下：
 * <blockquote>
 *  <table border="1" cellpadding="8" summary="支持的属性">
 *   <tr>
 *     <th> 名称 </th>
 *     <th> 类型 </th>
 *   </tr>
 *  <tr>
 *     <td> "lastModifiedTime" </td>
 *     <td> {@link FileTime} </td>
 *   </tr>
 *   <tr>
 *     <td> "lastAccessTime" </td>
 *     <td> {@link FileTime} </td>
 *   </tr>
 *   <tr>
 *     <td> "creationTime" </td>
 *     <td> {@link FileTime} </td>
 *   </tr>
 *   <tr>
 *     <td> "size" </td>
 *     <td> {@link Long} </td>
 *   </tr>
 *   <tr>
 *     <td> "isRegularFile" </td>
 *     <td> {@link Boolean} </td>
 *   </tr>
 *   <tr>
 *     <td> "isDirectory" </td>
 *     <td> {@link Boolean} </td>
 *   </tr>
 *   <tr>
 *     <td> "isSymbolicLink" </td>
 *     <td> {@link Boolean} </td>
 *   </tr>
 *   <tr>
 *     <td> "isOther" </td>
 *     <td> {@link Boolean} </td>
 *   </tr>
 *   <tr>
 *     <td> "fileKey" </td>
 *     <td> {@link Object} </td>
 *   </tr>
 * </table>
 * </blockquote>
 *
 * <p> 可以使用 {@link java.nio.file.Files#getAttribute getAttribute} 方法读取这些属性，就像调用 {@link
 * #readAttributes() readAttributes()} 方法一样。
 *
 * <p> 可以使用 {@link java.nio.file.Files#setAttribute setAttribute} 方法更新文件的最后修改时间、最后访问时间或创建时间属性，就像调用 {@link #setTimes setTimes} 方法一样。
 *
 * @since 1.7
 */

public interface BasicFileAttributeView
    extends FileAttributeView
{
    /**
     * 返回属性视图的名称。此类属性视图的名称为 {@code "basic"}。
     */
    @Override
    String name();

    /**
     * 作为批量操作读取基本文件属性。
     *
     * <p> 是否所有文件属性都作为原子操作读取，具体实现可能有所不同。
     *
     * @return  文件属性
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          如果默认提供程序安装了安全管理器，其 {@link SecurityManager#checkRead(String) checkRead}
     *          方法将被调用以检查文件的读取权限
     */
    BasicFileAttributes readAttributes() throws IOException;

    /**
     * 更新文件的最后修改时间、最后访问时间或创建时间属性中的一个或多个。
     *
     * <p> 此方法更新文件的时间戳属性。值将转换为文件系统支持的纪元和精度。
     * 从更细粒度转换为更粗粒度会导致精度损失。当尝试设置不受支持的时间戳或设置超出底层文件存储支持范围的值时，此方法的行为未定义。可能会或不会通过抛出 {@code IOException} 而失败。
     *
     * <p> 如果 {@code lastModifiedTime}、{@code lastAccessTime} 或 {@code createTime} 参数中的任何一个为 {@code null}，则相应的时间戳不会改变。
     * 如果仅更新部分时间戳属性，实现可能需要读取现有文件属性值。因此，此方法可能不是相对于其他文件系统操作的原子操作。读取和重写现有值也可能导致精度损失。
     * 如果 {@code lastModifiedTime}、{@code lastAccessTime} 和 {@code createTime} 参数全部为 {@code null}，则此方法没有效果。
     *
     * <p> <b>使用示例：</b>
     * 假设我们想更改文件的最后访问时间。
     * <pre>
     *    Path path = ...
     *    FileTime time = ...
     *    Files.getFileAttributeView(path, BasicFileAttributeView.class).setTimes(null, time, null);
     * </pre>
     *
     * @param   lastModifiedTime
     *          新的最后修改时间，或 {@code null} 表示不更改值
     * @param   lastAccessTime
     *          最后访问时间，或 {@code null} 表示不更改值
     * @param   createTime
     *          文件的创建时间，或 {@code null} 表示不更改值
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          如果默认提供程序安装了安全管理器，其 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法将被调用以检查文件的写入权限
     *
     * @see java.nio.file.Files#setLastModifiedTime
     */
    void setTimes(FileTime lastModifiedTime,
                  FileTime lastAccessTime,
                  FileTime createTime) throws IOException;
}
