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
package java.nio.file;

import java.nio.file.attribute.*;
import java.nio.channels.SeekableByteChannel;
import java.util.Set;
import java.io.IOException;

/**
 * 一个 {@code DirectoryStream}，定义了对相对于打开目录的文件的操作。{@code SecureDirectoryStream} 旨在供需要以无竞争方式遍历文件树或以其他方式操作目录的高级或安全敏感应用程序使用。当一系列文件操作不能独立进行时，可能会出现竞争条件。此接口定义的每个文件操作都指定一个相对路径。无论目录在打开后是否被移动或被攻击者替换，所有对文件的访问都是相对于打开的目录。{@code SecureDirectoryStream} 也可以用作虚拟 <em>工作目录</em>。
 *
 * <p> 一个 {@code SecureDirectoryStream} 需要底层操作系统的相应支持。如果实现支持此功能，则 {@link Files#newDirectoryStream newDirectoryStream} 方法返回的 {@code DirectoryStream} 将是一个 {@code SecureDirectoryStream}，必须将其转换为该类型才能调用此接口定义的方法。
 *
 * <p> 在默认 {@link java.nio.file.spi.FileSystemProvider 提供程序} 的情况下，如果设置了安全经理，则权限检查将使用通过将给定的相对路径解析为目录的 <i>原始路径</i> 获得的路径进行（无论目录自打开以来是否已移动）。
 *
 * @since   1.7
 */

public interface SecureDirectoryStream<T>
    extends DirectoryStream<T>
{
    /**
     * 打开由给定路径标识的目录，返回一个 {@code SecureDirectoryStream} 以遍历目录中的条目。
     *
     * <p> 此方法的工作方式与 {@link Files#newDirectoryStream(Path) newDirectoryStream} 方法完全相同，但 {@code path} 参数是一个 {@link Path#isAbsolute 绝对} 路径。当参数是相对路径时，要打开的目录相对于此打开的目录。可以使用 {@link LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} 选项确保如果文件是符号链接，则此方法失败。
     *
     * <p> 一旦创建了新的目录流，它就不依赖于用于创建它的目录流。关闭此目录流不会影响新创建的目录流。
     *
     * @param   path
     *          要打开的目录的路径
     * @param   options
     *          指定如何处理符号链接的选项
     *
     * @return  一个新的且已打开的 {@code SecureDirectoryStream} 对象
     *
     * @throws  ClosedDirectoryStreamException
     *          如果目录流已关闭
     * @throws  NotDirectoryException
     *          如果文件无法打开，因为它不是目录 <i>(可选的具体异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkRead(String) checkRead} 方法检查对目录的读取权限。
     */
    SecureDirectoryStream<T> newDirectoryStream(T path, LinkOption... options)
        throws IOException;

    /**
     * 在此目录中打开或创建一个文件，返回一个可寻址的字节通道以访问文件。
     *
     * <p> 此方法的工作方式与 {@link Files#newByteChannel Files.newByteChannel} 方法完全相同，但 {@code path} 参数是一个 {@link Path#isAbsolute 绝对} 路径。当参数是相对路径时，要打开或创建的文件相对于此打开的目录。除了 {@code Files.newByteChannel} 方法定义的选项外，还可以使用 {@link LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} 选项确保如果文件是符号链接，则此方法失败。
     *
     * <p> 一旦创建了通道，它就不依赖于用于创建它的目录流。关闭此目录流不会影响通道。
     *
     * @param   path
     *          要打开或创建的文件的路径
     * @param   options
     *          指定如何打开文件的选项
     * @param   attrs
     *          一个可选的属性列表，用于在创建文件时原子地设置
     *
     * @return  可寻址的字节通道
     *
     * @throws  ClosedDirectoryStreamException
     *          如果目录流已关闭
     * @throws  IllegalArgumentException
     *          如果集合中包含无效的选项组合
     * @throws  UnsupportedOperationException
     *          如果指定了不支持的打开选项，或者数组中包含在创建文件时无法原子地设置的属性
     * @throws  FileAlreadyExistsException
     *          如果同名文件已存在且指定了 {@link StandardOpenOption#CREATE_NEW CREATE_NEW} 选项 <i>(可选的具体异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkRead(String) checkRead} 方法检查对路径的读取权限（如果文件以读取方式打开）。调用 {@link SecurityManager#checkWrite(String) checkWrite} 方法检查对路径的写入权限（如果文件以写入方式打开）。
     */
    SeekableByteChannel newByteChannel(T path,
                                       Set<? extends OpenOption> options,
                                       FileAttribute<?>... attrs)
        throws IOException;

    /**
     * 删除一个文件。
     *
     * <p> 与 {@link Files#delete delete()} 方法不同，此方法不会首先检查文件以确定文件是否为目录。是否可以删除目录取决于系统，因此未指定。如果文件是符号链接，则删除链接本身，而不是链接的最终目标。当参数是相对路径时，要删除的文件相对于此打开的目录。
     *
     * @param   path
     *          要删除的文件的路径
     *
     * @throws  ClosedDirectoryStreamException
     *          如果目录流已关闭
     * @throws  NoSuchFileException
     *          如果文件不存在 <i>(可选的具体异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkDelete(String) checkDelete} 方法检查对文件的删除权限。
     */
    void deleteFile(T path) throws IOException;

    /**
     * 删除一个目录。
     *
     * <p> 与 {@link Files#delete delete()} 方法不同，此方法不会首先检查文件以确定文件是否为目录。是否可以删除非目录文件取决于系统，因此未指定。当参数是相对路径时，要删除的目录相对于此打开的目录。
     *
     * @param   path
     *          要删除的目录的路径
     *
     * @throws  ClosedDirectoryStreamException
     *          如果目录流已关闭
     * @throws  NoSuchFileException
     *          如果目录不存在 <i>(可选的具体异常)</i>
     * @throws  DirectoryNotEmptyException
     *          如果目录无法删除，因为它不为空 <i>(可选的具体异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkDelete(String) checkDelete} 方法检查对目录的删除权限。
     */
    void deleteDirectory(T path) throws IOException;

    /**
     * 将一个文件从一个目录移动到另一个目录。
     *
     * <p> 此方法的工作方式类似于 {@link Files#move move} 方法，当指定了 {@link StandardCopyOption#ATOMIC_MOVE ATOMIC_MOVE} 选项时。也就是说，此方法将文件作为一个原子文件系统操作进行移动。如果 {@code srcpath} 参数是一个 {@link Path#isAbsolute 绝对} 路径，则它定位源文件。如果参数是相对路径，则它相对于此打开的目录定位。如果 {@code targetpath} 参数是绝对路径，则它定位目标文件（忽略 {@code targetdir} 参数）。如果参数是相对路径，则它相对于由 {@code targetdir} 参数标识的打开目录定位。在所有情况下，如果目标文件存在，则具体实现决定是否替换目标文件或使此方法失败。
     *
     * @param   srcpath
     *          要移动的文件的名称
     * @param   targetdir
     *          目标目录
     * @param   targetpath
     *          目标目录中文件的名称
     *
     * @throws  ClosedDirectoryStreamException
     *          如果此目录流或目标目录流已关闭
     * @throws  FileAlreadyExistsException
     *          如果文件已存在于目标目录中且无法替换 <i>(可选的具体异常)</i>
     * @throws  AtomicMoveNotSupportedException
     *          如果文件无法作为一个原子文件系统操作进行移动
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkWrite(String) checkWrite} 方法检查对源文件和目标文件的写入权限。
     */
    void move(T srcpath, SecureDirectoryStream<T> targetdir, T targetpath)
        throws IOException;

    /**
     * 返回一个新的文件属性视图，以访问此目录的文件属性。
     *
     * <p> 结果文件属性视图可用于读取或更新此（打开的）目录的属性。{@code type} 参数指定属性视图的类型，如果支持，方法将返回该类型的实例。调用此方法以获取 {@link BasicFileAttributeView} 总是返回一个绑定到此打开目录的该类的实例。
     *
     * <p> 结果文件属性视图的状态与此目录流紧密相关。一旦目录流 {@link #close 关闭}，则所有读取或更新属性的方法将抛出 {@link ClosedDirectoryStreamException ClosedDirectoryStreamException}。
     *
     * @param   <V>
     *          {@code FileAttributeView} 类型
     * @param   type
     *          对应于文件属性视图的 {@code Class} 对象
     *
     * @return  一个绑定到此目录流的指定类型的文件属性视图的新实例，如果属性视图类型不可用，则返回 {@code null}
     */
    <V extends FileAttributeView> V getFileAttributeView(Class<V> type);

    /**
     * 返回一个新的文件属性视图，以访问此目录中文件的文件属性。
     *
     * <p> 结果文件属性视图可用于读取或更新此目录中文件的属性。{@code type} 参数指定属性视图的类型，如果支持，方法将返回该类型的实例。调用此方法以获取 {@link BasicFileAttributeView} 总是返回一个绑定到目录中文件的该类的实例。
     *
     * <p> 结果文件属性视图的状态与此目录流紧密相关。一旦目录流 {@link #close 关闭}，则所有读取或更新属性的方法将抛出 {@link ClosedDirectoryStreamException ClosedDirectoryStreamException}。文件在创建文件属性视图时不需要存在，但如果文件不存在，调用读取或更新文件属性的方法时将失败。
     *
     * @param   <V>
     *          {@code FileAttributeView} 类型
     * @param   path
     *          文件的路径
     * @param   type
     *          对应于文件属性视图的 {@code Class} 对象
     * @param   options
     *          指定如何处理符号链接的选项
     *
     * @return  一个绑定到此目录流的指定类型的文件属性视图的新实例，如果属性视图类型不可用，则返回 {@code null}
     *
     */
    <V extends FileAttributeView> V getFileAttributeView(T path,
                                                         Class<V> type,
                                                         LinkOption... options);
}
