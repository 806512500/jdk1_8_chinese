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
 * 提供对传统“DOS”文件属性的视图。
 * 这些属性由文件系统（如常用的文件分配表（FAT）格式）支持，通常用于<em>消费设备</em>。
 *
 * <p> {@code DosFileAttributeView} 是一个 {@link BasicFileAttributeView}，它还支持访问一组用于指示文件是否为只读、隐藏、系统文件或已归档的DOS属性标志。
 *
 * <p> 如果需要动态访问文件属性，此属性视图支持的属性由 {@code BasicFileAttributeView} 定义，并且还支持以下属性：
 * <blockquote>
 * <table border="1" cellpadding="8" summary="支持的属性">
 *   <tr>
 *     <th> 名称 </th>
 *     <th> 类型 </th>
 *   </tr>
 *   <tr>
 *     <td> readonly </td>
 *     <td> {@link Boolean} </td>
 *   </tr>
 *   <tr>
 *     <td> hidden </td>
 *     <td> {@link Boolean} </td>
 *   </tr>
 *   <tr>
 *     <td> system </td>
 *     <td> {@link Boolean} </td>
 *   </tr>
 *   <tr>
 *     <td> archive </td>
 *     <td> {@link Boolean} </td>
 *   </tr>
 * </table>
 * </blockquote>
 *
 * <p> 可以使用 {@link java.nio.file.Files#getAttribute getAttribute} 方法读取这些属性中的任何一个，或 {@link BasicFileAttributeView} 定义的任何属性，就像调用 {@link #readAttributes readAttributes()} 方法一样。
 *
 * <p> 可以使用 {@link java.nio.file.Files#setAttribute setAttribute} 方法更新文件的最后修改时间、最后访问时间或创建时间属性，这些属性由 {@link BasicFileAttributeView} 定义。还可以用于更新DOS属性，就像分别调用 {@link #setReadOnly setReadOnly}、{@link #setHidden setHidden}、{@link #setSystem setSystem} 和 {@link #setArchive setArchive} 方法一样。
 *
 * @since 1.7
 */

public interface DosFileAttributeView
    extends BasicFileAttributeView
{
    /**
     * 返回属性视图的名称。此类属性视图的名称为 {@code "dos"}。
     */
    @Override
    String name();

    /**
     * @throws  IOException                             {@inheritDoc}
     * @throws  SecurityException                       {@inheritDoc}
     */
    @Override
    DosFileAttributes readAttributes() throws IOException;

    /**
     * 更新只读属性的值。
     *
     * <p> 是否可以将属性更新为与其他文件系统操作相关的原子操作是实现特定的。例如，实现可能需要读取DOS属性的现有值以更新此属性。
     *
     * @param   value
     *          属性的新值
     *
     * @throws  IOException
     *          如果发生I/O错误
     * @throws  SecurityException
     *          在默认情况下，如果安装了安全经理，其 {@link SecurityManager#checkWrite(String) checkWrite} 方法将被调用以检查对文件的写访问权限
     */
    void setReadOnly(boolean value) throws IOException;

    /**
     * 更新隐藏属性的值。
     *
     * <p> 是否可以将属性更新为与其他文件系统操作相关的原子操作是实现特定的。例如，实现可能需要读取DOS属性的现有值以更新此属性。
     *
     * @param   value
     *          属性的新值
     *
     * @throws  IOException
     *          如果发生I/O错误
     * @throws  SecurityException
     *          在默认情况下，如果安装了安全经理，其 {@link SecurityManager#checkWrite(String) checkWrite} 方法将被调用以检查对文件的写访问权限
     */
    void setHidden(boolean value) throws IOException;

    /**
     * 更新系统属性的值。
     *
     * <p> 是否可以将属性更新为与其他文件系统操作相关的原子操作是实现特定的。例如，实现可能需要读取DOS属性的现有值以更新此属性。
     *
     * @param   value
     *          属性的新值
     *
     * @throws  IOException
     *          如果发生I/O错误
     * @throws  SecurityException
     *          在默认情况下，如果安装了安全经理，其 {@link SecurityManager#checkWrite(String) checkWrite} 方法将被调用以检查对文件的写访问权限
     */
    void setSystem(boolean value) throws IOException;

    /**
     * 更新归档属性的值。
     *
     * <p> 是否可以将属性更新为与其他文件系统操作相关的原子操作是实现特定的。例如，实现可能需要读取DOS属性的现有值以更新此属性。
     *
     * @param   value
     *          属性的新值
     *
     * @throws  IOException
     *          如果发生I/O错误
     * @throws  SecurityException
     *          在默认情况下，如果安装了安全经理，其 {@link SecurityManager#checkWrite(String) checkWrite} 方法将被调用以检查对文件的写访问权限
     */
    void setArchive(boolean value) throws IOException;
}
