
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

package java.nio.file.spi;

import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.channels.*;
import java.net.URI;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * 文件系统的服务提供者类。由 {@link java.nio.file.Files} 类定义的方法通常会委托给此类的实例。
 *
 * <p> 文件系统提供者是此类的具体实现，它实现了此类定义的抽象方法。提供者通过一个 {@code URI} {@link #getScheme() 方案} 来标识。默认提供者通过 URI 方案 "file" 来标识。它创建了 {@link FileSystem}，该文件系统提供了对 Java 虚拟机可访问的文件系统的访问。{@link FileSystems} 类定义了如何定位和加载文件系统提供者。默认提供者通常是系统默认提供者，但如果系统属性 {@code
 * java.nio.file.spi.DefaultFileSystemProvider} 被设置，则可以覆盖默认提供者。在这种情况下，提供者有一个单参数构造函数，其形式参数类型为 {@code
 * FileSystemProvider}。所有其他提供者都有一个无参数构造函数，用于初始化提供者。
 *
 * <p> 提供者是多个 {@link FileSystem} 实例的工厂。每个文件系统通过一个 {@code URI} 来标识，其中 URI 的方案与提供者的 {@link #getScheme 方案} 匹配。例如，默认文件系统通过 URI {@code "file:///"} 来标识。基于内存的文件系统可能通过一个如 {@code "memory:///?name=logfs"} 的 URI 来标识。{@link #newFileSystem newFileSystem} 方法可用于创建文件系统，而 {@link #getFileSystem getFileSystem} 方法可用于获取由提供者创建的现有文件系统的引用。如果提供者是单个文件系统的工厂，则文件系统是在提供者初始化时创建的，还是在调用 {@code newFileSystem} 方法时创建的，取决于提供者。对于默认提供者，文件系统在提供者初始化时创建。
 *
 * <p> 本类中的所有方法都适合多线程并发使用。
 *
 * @since 1.7
 */

public abstract class FileSystemProvider {
    // 加载提供者时使用的锁
    private static final Object lock = new Object();

    // 已安装的提供者
    private static volatile List<FileSystemProvider> installedProviders;

    // 用于避免递归加载已安装的提供者
    private static boolean loadingProviders  = false;

    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("fileSystemProvider"));
        return null;
    }
    private FileSystemProvider(Void ignore) { }

    /**
     * 初始化此类的新实例。
     *
     * <p> 在构造过程中，提供者可以安全地访问与默认提供者关联的文件，但需要注意避免循环加载其他已安装的提供者。如果检测到已安装提供者的循环加载，则会抛出未指定的错误。
     *
     * @throws  SecurityException
     *          如果已安装了安全经理，并且它拒绝了
     *          {@link RuntimePermission}<tt>("fileSystemProvider")</tt>
     */
    protected FileSystemProvider() {
        this(checkPermission());
    }

    // 加载所有已安装的提供者
    private static List<FileSystemProvider> loadInstalledProviders() {
        List<FileSystemProvider> list = new ArrayList<FileSystemProvider>();

        ServiceLoader<FileSystemProvider> sl = ServiceLoader
            .load(FileSystemProvider.class, ClassLoader.getSystemClassLoader());

        // ServiceConfigurationError 可能会在这里抛出
        for (FileSystemProvider provider: sl) {
            String scheme = provider.getScheme();

            // 如果提供者不是 "file" 且不是重复的，则添加到列表中
            if (!scheme.equalsIgnoreCase("file")) {
                boolean found = false;
                for (FileSystemProvider p: list) {
                    if (p.getScheme().equalsIgnoreCase(scheme)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    list.add(provider);
                }
            }
        }
        return list;
    }

    /**
     * 返回已安装的文件系统提供者的列表。
     *
     * <p> 该方法的首次调用会导致默认提供者初始化（如果尚未初始化），并加载 {@link FileSystems} 类描述的其他已安装提供者。
     *
     * @return  已安装的文件系统提供者的不可修改列表。列表至少包含一个元素，即默认文件系统提供者
     *
     * @throws  ServiceConfigurationError
     *          当加载服务提供者时发生错误
     */
    public static List<FileSystemProvider> installedProviders() {
        if (installedProviders == null) {
            // 确保默认提供者已初始化
            FileSystemProvider defaultProvider = FileSystems.getDefault().provider();

            synchronized (lock) {
                if (installedProviders == null) {
                    if (loadingProviders) {
                        throw new Error("Circular loading of installed providers detected");
                    }
                    loadingProviders = true;

                    List<FileSystemProvider> list = AccessController
                        .doPrivileged(new PrivilegedAction<List<FileSystemProvider>>() {
                            @Override
                            public List<FileSystemProvider> run() {
                                return loadInstalledProviders();
                        }});

                    // 将默认提供者插入列表的开头
                    list.add(0, defaultProvider);

                    installedProviders = Collections.unmodifiableList(list);
                }
            }
        }
        return installedProviders;
    }

    /**
     * 返回标识此提供者的 URI 方案。
     *
     * @return  URI 方案
     */
    public abstract String getScheme();

    /**
     * 构造一个由 URI 标识的新 {@code FileSystem} 对象。此方法由 {@link FileSystems#newFileSystem(URI,Map)}
     * 方法调用，以打开由 URI 标识的新文件系统。
     *
     * <p> {@code uri} 参数是一个绝对的、分层的 URI，其方案（不区分大小写）与该提供者支持的方案相同。URI 的确切形式高度依赖于提供者。{@code env} 参数是一个提供者特定的属性映射，用于配置文件系统。
     *
     * <p> 如果文件系统已经存在（因为之前调用了此方法），则此方法会抛出 {@link FileSystemAlreadyExistsException}。一旦文件系统被 {@link
     * java.nio.file.FileSystem#close 关闭}，提供者是否允许使用与之前创建的文件系统相同的 URI 创建新的文件系统，这取决于提供者。
     *
     * @param   uri
     *          URI 引用
     * @param   env
     *          一个提供者特定的属性映射，用于配置文件系统；可以为空
     *
     * @return  一个新的文件系统
     *
     * @throws  IllegalArgumentException
     *          如果 {@code uri} 参数的预条件不满足，或者 {@code env} 参数不包含提供者所需的属性，或者属性值无效
     * @throws  IOException
     *          创建文件系统时发生 I/O 错误
     * @throws  SecurityException
     *          如果已安装了安全经理，并且它拒绝了文件系统提供者实现所需的未指定权限
     * @throws  FileSystemAlreadyExistsException
     *          如果文件系统已经存在
     */
    public abstract FileSystem newFileSystem(URI uri, Map<String,?> env)
        throws IOException;

    /**
     * 返回由此提供者创建的现有 {@code FileSystem}。
     *
     * <p> 此方法返回通过调用 {@link #newFileSystem(URI,Map) newFileSystem(URI,Map)}
     * 方法创建的 {@code FileSystem} 的引用。通过 {@link #newFileSystem(Path,Map)
     * newFileSystem(Path,Map)} 方法创建的文件系统不会被此方法返回。文件系统通过其 {@code URI} 来标识。其确切形式高度依赖于提供者。对于默认提供者，URI 的路径组件为 {@code "/"}，而权威、查询和片段组件未定义（未定义的组件由 {@code null} 表示）。
     *
     * <p> 一旦由提供者创建的文件系统被 {@link
     * java.nio.file.FileSystem#close 关闭}，提供者是否返回已关闭的文件系统的引用或抛出 {@link
     * FileSystemNotFoundException} 取决于提供者。如果提供者允许使用与之前创建的文件系统相同的 URI 创建新的文件系统，则在文件系统关闭后（在新的实例通过 {@link #newFileSystem
     * newFileSystem} 方法创建之前）调用此方法会抛出异常。
     *
     * <p> 如果已安装了安全经理，则提供者实现可能需要在返回现有文件系统的引用之前检查权限。对于 {@link FileSystems#getDefault
     * 默认} 文件系统，不需要权限检查。
     *
     * @param   uri
     *          URI 引用
     *
     * @return  文件系统
     *
     * @throws  IllegalArgumentException
     *          如果 {@code uri} 参数的预条件不满足
     * @throws  FileSystemNotFoundException
     *          如果文件系统不存在
     * @throws  SecurityException
     *          如果已安装了安全经理，并且它拒绝了未指定的权限。
     */
    public abstract FileSystem getFileSystem(URI uri);

    /**
     * 通过转换给定的 {@link URI} 返回一个 {@code Path} 对象。结果的 {@code Path} 与已存在的文件系统或自动构造的文件系统相关联。
     *
     * <p> URI 的确切形式取决于文件系统提供者。对于默认提供者，URI 方案为 {@code "file"}，给定的 URI 具有非空的路径组件，未定义的查询和片段组件。结果的 {@code Path} 与默认的 {@link FileSystems#getDefault 默认} {@code FileSystem} 相关联。
     *
     * <p> 如果已安装了安全经理，则提供者实现可能需要在返回 {@code Path} 之前检查权限。对于 {@link
     * FileSystems#getDefault 默认} 文件系统，不需要权限检查。
     *
     * @param   uri
     *          要转换的 URI
     *
     * @return  结果的 {@code Path}
     *
     * @throws  IllegalArgumentException
     *          如果 URI 方案不标识此提供者或 {@code uri} 参数的其他预条件不满足
     * @throws  FileSystemNotFoundException
     *          如果文件系统不存在且无法自动创建
     * @throws  SecurityException
     *          如果已安装了安全经理，并且它拒绝了未指定的权限。
     */
    public abstract Path getPath(URI uri);

    /**
     * 构造一个新的 {@code FileSystem} 以将文件的内容作为文件系统访问。
     *
     * <p> 此方法适用于伪文件系统的专业提供者，其中将一个或多个文件的内容视为文件系统。{@code env} 参数是一个提供者特定的属性映射，用于配置文件系统。
     *
     * <p> 如果此提供者不支持创建此类文件系统或不识别给定文件的文件类型，则会抛出 {@code UnsupportedOperationException}。此方法的默认实现会抛出 {@code UnsupportedOperationException}。
     *
     * @param   path
     *          文件的路径
     * @param   env
     *          一个提供者特定的属性映射，用于配置文件系统；可以为空
     *
     * @return  一个新的文件系统
     *
     * @throws  UnsupportedOperationException
     *          如果此提供者不支持将内容作为文件系统访问或不识别给定文件的文件类型
     * @throws  IllegalArgumentException
     *          如果 {@code env} 参数不包含提供者所需的属性，或者属性值无效
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          如果已安装了安全经理，并且它拒绝了未指定的权限。
     */
    public FileSystem newFileSystem(Path path, Map<String,?> env)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 打开一个文件，返回一个输入流以从文件中读取数据。此方法的工作方式与 {@link
     * Files#newInputStream} 方法指定的方式完全相同。
     *
     * <p> 此方法的默认实现通过调用 {@link #newByteChannel} 方法打开文件通道，并构造一个从通道读取字节的流。如果适当，应覆盖此方法。
     *
     * @param   path
     *          要打开的文件的路径
     * @param   options
     *          指定如何打开文件的选项
     *
     * @return  一个新的输入流
     *
     * @throws  IllegalArgumentException
     *          如果指定了无效的选项组合
     * @throws  UnsupportedOperationException
     *          如果指定了不支持的选项
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          对于默认提供者，如果已安装了安全经理，将调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法以检查对文件的读取权限。
     */
    public InputStream newInputStream(Path path, OpenOption... options)
        throws IOException
    {
        if (options.length > 0) {
            for (OpenOption opt: options) {
                // 除 APPEND 和 WRITE 外的所有 OpenOption 值都是允许的
                if (opt == StandardOpenOption.APPEND ||
                    opt == StandardOpenOption.WRITE)
                    throw new UnsupportedOperationException("'" + opt + "' not allowed");
            }
        }
        return Channels.newInputStream(Files.newByteChannel(path, options));
    }


                /**
     * 打开或创建一个文件，返回一个输出流，可以用于向文件写入字节。此方法的工作方式与 {@link Files#newOutputStream} 方法完全相同。
     *
     * <p> 此方法的默认实现通过调用 {@link #newByteChannel} 方法打开一个到文件的通道，并构建一个向通道写入字节的流。如果适当，此方法应被重写。
     *
     * @param   path
     *          要打开或创建的文件的路径
     * @param   options
     *          指定文件打开方式的选项
     *
     * @return  一个新的输出流
     *
     * @throws  IllegalArgumentException
     *          如果 {@code options} 包含无效的选项组合
     * @throws  UnsupportedOperationException
     *          如果指定了不支持的选项
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，将调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法检查对文件的写权限。如果文件以 {@code DELETE_ON_CLOSE} 选项打开，将调用 {@link
     *          SecurityManager#checkDelete(String) checkDelete} 方法检查删除权限。
     */
    public OutputStream newOutputStream(Path path, OpenOption... options)
        throws IOException
    {
        int len = options.length;
        Set<OpenOption> opts = new HashSet<OpenOption>(len + 3);
        if (len == 0) {
            opts.add(StandardOpenOption.CREATE);
            opts.add(StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            for (OpenOption opt: options) {
                if (opt == StandardOpenOption.READ)
                    throw new IllegalArgumentException("READ not allowed");
                opts.add(opt);
            }
        }
        opts.add(StandardOpenOption.WRITE);
        return Channels.newOutputStream(newByteChannel(path, opts));
    }

    /**
     * 打开或创建一个文件以进行读取和/或写入，返回一个文件通道以访问文件。此方法的工作方式与 {@link FileChannel#open(Path,Set,FileAttribute[])
     * FileChannel.open} 方法完全相同。不支持创建文件通道的提供者将抛出 {@code
     * UnsupportedOperationException}。默认提供者必须支持文件通道的创建。如果未重写，默认实现将抛出 {@code UnsupportedOperationException}。
     *
     * @param   path
     *          要打开或创建的文件的路径
     * @param   options
     *          指定文件打开方式的选项
     * @param   attrs
     *          创建文件时原子设置的可选文件属性列表
     *
     * @return  一个新的文件通道
     *
     * @throws  IllegalArgumentException
     *          如果集合包含无效的选项组合
     * @throws  UnsupportedOperationException
     *          如果此提供者不支持创建文件通道，或指定了不支持的打开选项或文件属性
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认文件系统的情况下，如果文件以读取方式打开，将调用 {@link
     *          SecurityManager#checkRead(String)} 方法检查读权限。如果文件以写入方式打开，将调用 {@link
     *          SecurityManager#checkWrite(String)} 方法检查写权限
     */
    public FileChannel newFileChannel(Path path,
                                      Set<? extends OpenOption> options,
                                      FileAttribute<?>... attrs)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 打开或创建一个文件以进行读取和/或写入，返回一个异步文件通道以访问文件。此方法的工作方式与 {@link
     * AsynchronousFileChannel#open(Path,Set,ExecutorService,FileAttribute[])
     * AsynchronousFileChannel.open} 方法完全相同。不支持创建异步文件通道的提供者将抛出 {@code UnsupportedOperationException}。
     * 默认提供者必须支持异步文件通道的创建。如果未重写，默认实现将抛出 {@code UnsupportedOperationException}。
     *
     * @param   path
     *          要打开或创建的文件的路径
     * @param   options
     *          指定文件打开方式的选项
     * @param   executor
     *          要关联的线程池，或 {@code null} 以使用默认线程池
     * @param   attrs
     *          创建文件时原子设置的可选文件属性列表
     *
     * @return  一个新的异步文件通道
     *
     * @throws  IllegalArgumentException
     *          如果集合包含无效的选项组合
     * @throws  UnsupportedOperationException
     *          如果此提供者不支持创建异步文件通道，或指定了不支持的打开选项或文件属性
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认文件系统的情况下，如果文件以读取方式打开，将调用 {@link
     *          SecurityManager#checkRead(String)} 方法检查读权限。如果文件以写入方式打开，将调用 {@link
     *          SecurityManager#checkWrite(String)} 方法检查写权限
     */
    public AsynchronousFileChannel newAsynchronousFileChannel(Path path,
                                                              Set<? extends OpenOption> options,
                                                              ExecutorService executor,
                                                              FileAttribute<?>... attrs)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 打开或创建一个文件，返回一个可寻址的字节通道以访问文件。此方法的工作方式与 {@link
     * Files#newByteChannel(Path,Set,FileAttribute[])} 方法完全相同。
     *
     * @param   path
     *          要打开或创建的文件的路径
     * @param   options
     *          指定文件打开方式的选项
     * @param   attrs
     *          创建文件时原子设置的可选文件属性列表
     *
     * @return  一个新的可寻址字节通道
     *
     * @throws  IllegalArgumentException
     *          如果集合包含无效的选项组合
     * @throws  UnsupportedOperationException
     *          如果指定了不支持的打开选项，或数组包含在创建文件时无法原子设置的属性
     * @throws  FileAlreadyExistsException
     *          如果同名文件已存在且指定了 {@link
     *          StandardOpenOption#CREATE_NEW CREATE_NEW} 选项 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，将调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对路径的读权限，如果文件以读取方式打开。将调用 {@link SecurityManager#checkWrite(String)
     *          checkWrite} 方法检查对路径的写权限，如果文件以写入方式打开。如果文件以 {@code DELETE_ON_CLOSE} 选项打开，将调用 {@link
     *          SecurityManager#checkDelete(String) checkDelete} 方法检查删除权限。
     */
    public abstract SeekableByteChannel newByteChannel(Path path,
        Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException;

    /**
     * 打开一个目录，返回一个 {@code DirectoryStream} 以迭代目录中的条目。此方法的工作方式与 {@link
     * Files#newDirectoryStream(java.nio.file.Path, java.nio.file.DirectoryStream.Filter)}
     * 方法完全相同。
     *
     * @param   dir
     *          目录的路径
     * @param   filter
     *          目录流过滤器
     *
     * @return  一个新的且已打开的 {@code DirectoryStream} 对象
     *
     * @throws  NotDirectoryException
     *          如果文件不能以其他方式打开，因为它不是目录 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，将调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对目录的读权限。
     */
    public abstract DirectoryStream<Path> newDirectoryStream(Path dir,
         DirectoryStream.Filter<? super Path> filter) throws IOException;

    /**
     * 创建一个新目录。此方法的工作方式与 {@link Files#createDirectory} 方法完全相同。
     *
     * @param   dir
     *          要创建的目录
     * @param   attrs
     *          创建目录时原子设置的可选文件属性列表
     *
     * @throws  UnsupportedOperationException
     *          如果数组包含在创建目录时无法原子设置的属性
     * @throws  FileAlreadyExistsException
     *          如果同名目录已存在且不能以其他方式创建 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误或父目录不存在
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，将调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法检查对新目录的写权限。
     */
    public abstract void createDirectory(Path dir, FileAttribute<?>... attrs)
        throws IOException;

    /**
     * 创建一个指向目标的符号链接。此方法的工作方式与 {@link Files#createSymbolicLink} 方法完全相同。
     *
     * <p> 此方法的默认实现将抛出 {@code
     * UnsupportedOperationException}。
     *
     * @param   link
     *          要创建的符号链接的路径
     * @param   target
     *          符号链接的目标
     * @param   attrs
     *          创建符号链接时原子设置的属性数组
     *
     * @throws  UnsupportedOperationException
     *          如果实现不支持符号链接，或数组包含在创建符号链接时无法原子设置的属性
     * @throws  FileAlreadyExistsException
     *          如果同名文件已存在 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，将拒绝 {@link LinkPermission}<tt>("symbolic")</tt>
     *          或其 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法拒绝对符号链接路径的写权限。
     */
    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 为现有文件创建一个新的链接（目录条目）。此方法的工作方式与 {@link Files#createLink}
     * 方法完全相同。
     *
     * <p> 此方法的默认实现将抛出 {@code
     * UnsupportedOperationException}。
     *
     * @param   link
     *          要创建的链接（目录条目）
     * @param   existing
     *          现有文件的路径
     *
     * @throws  UnsupportedOperationException
     *          如果实现不支持将现有文件添加到目录
     * @throws  FileAlreadyExistsException
     *          如果同名文件已存在且不能以其他方式创建 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，将拒绝 {@link LinkPermission}<tt>("hard")</tt>
     *          或其 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法拒绝对链接或现有文件的写权限。
     */
    public void createLink(Path link, Path existing) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * 删除一个文件。此方法的工作方式与 {@link Files#delete} 方法完全相同。
     *
     * @param   path
     *          要删除的文件的路径
     *
     * @throws  NoSuchFileException
     *          如果文件不存在 <i>(可选特定异常)</i>
     * @throws  DirectoryNotEmptyException
     *          如果文件是目录且不能以其他方式删除，因为目录不为空 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，将调用 {@link SecurityManager#checkDelete(String)} 方法
     *          检查对文件的删除权限
     */
    public abstract void delete(Path path) throws IOException;

    /**
     * 如果文件存在则删除。此方法的工作方式与 {@link Files#deleteIfExists} 方法完全相同。
     *
     * <p> 此方法的默认实现简单地调用 {@link
     * #delete} 忽略文件不存在时的 {@code NoSuchFileException}。如果适当，可以重写此方法。
     *
     * @param   path
     *          要删除的文件的路径
     *
     * @return  如果文件由此方法删除，则返回 {@code true}；如果文件不存在且无法删除，则返回 {@code
     *          false}
     *
     * @throws  DirectoryNotEmptyException
     *          如果文件是目录且不能以其他方式删除，因为目录不为空 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，将调用 {@link SecurityManager#checkDelete(String)} 方法
     *          检查对文件的删除权限
     */
    public boolean deleteIfExists(Path path) throws IOException {
        try {
            delete(path);
            return true;
        } catch (NoSuchFileException ignore) {
            return false;
        }
    }


                /**
     * 读取符号链接的目标。此方法的工作方式与 {@link Files#readSymbolicLink} 方法完全相同。
     *
     * <p> 该方法的默认实现抛出 {@code
     * UnsupportedOperationException}。
     *
     * @param   link
     *          符号链接的路径
     *
     * @return  符号链接的目标
     *
     * @throws  UnsupportedOperationException
     *          如果实现不支持符号链接
     * @throws  NotLinkException
     *          如果文件不是符号链接，因此无法读取目标 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，它会检查是否授予了带有 "{@code readlink}" 操作的 {@code FilePermission} 以读取链接。
     */
    public Path readSymbolicLink(Path link) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * 将文件复制到目标文件。此方法的工作方式与 {@link Files#copy(Path,Path,CopyOption[])} 方法完全相同，
     * 但源文件和目标文件的路径必须与此提供者关联。
     *
     * @param   source
     *          要复制的文件的路径
     * @param   target
     *          目标文件的路径
     * @param   options
     *          指定如何复制的选项
     *
     * @throws  UnsupportedOperationException
     *          如果数组包含不支持的复制选项
     * @throws  FileAlreadyExistsException
     *          如果目标文件存在但无法替换，因为未指定 {@code REPLACE_EXISTING} 选项 <i>(可选特定异常)</i>
     * @throws  DirectoryNotEmptyException
     *          指定了 {@code REPLACE_EXISTING} 选项，但文件无法替换，因为它是一个非空目录
     *          <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，会调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对源文件的读取权限，调用 {@link SecurityManager#checkWrite(String) checkWrite} 方法检查对目标文件的写入权限。如果复制了符号链接，安全经理会被调用以检查 {@link
     *          LinkPermission}{@code ("symbolic")}。
     */
    public abstract void copy(Path source, Path target, CopyOption... options)
        throws IOException;

    /**
     * 将文件移动或重命名到目标文件。此方法的工作方式与 {@link Files#move} 方法完全相同，
     * 但源文件和目标文件的路径必须与此提供者关联。
     *
     * @param   source
     *          要移动的文件的路径
     * @param   target
     *          目标文件的路径
     * @param   options
     *          指定如何移动的选项
     *
     * @throws  UnsupportedOperationException
     *          如果数组包含不支持的复制选项
     * @throws  FileAlreadyExistsException
     *          如果目标文件存在但无法替换，因为未指定 {@code REPLACE_EXISTING} 选项 <i>(可选特定异常)</i>
     * @throws  DirectoryNotEmptyException
     *          指定了 {@code REPLACE_EXISTING} 选项，但文件无法替换，因为它是一个非空目录
     *          <i>(可选特定异常)</i>
     * @throws  AtomicMoveNotSupportedException
     *          如果数组包含 {@code ATOMIC_MOVE} 选项，但文件无法作为原子文件系统操作移动。
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，会调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法检查对源文件和目标文件的写入权限。
     */
    public abstract void move(Path source, Path target, CopyOption... options)
        throws IOException;

    /**
     * 测试两个路径是否指向同一个文件。此方法的工作方式与 {@link Files#isSameFile} 方法完全相同。
     *
     * @param   path
     *          一个文件的路径
     * @param   path2
     *          另一个路径
     *
     * @return  {@code true} 如果且仅当两个路径指向同一个文件
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，会调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对两个文件的读取权限。
     */
    public abstract boolean isSameFile(Path path, Path path2)
        throws IOException;

    /**
     * 判断文件是否被认为是 <em>隐藏</em> 的。此方法的工作方式与 {@link Files#isHidden} 方法完全相同。
     *
     * <p> 此方法由 {@link Files#isHidden isHidden} 方法调用。
     *
     * @param   path
     *          要测试的文件的路径
     *
     * @return  {@code true} 如果文件被认为是隐藏的
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，会调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对文件的读取权限。
     */
    public abstract boolean isHidden(Path path) throws IOException;

    /**
     * 返回表示文件所在文件存储的 {@link FileStore}。此方法的工作方式与 {@link Files#getFileStore} 方法完全相同。
     *
     * @param   path
     *          文件的路径
     *
     * @return  文件存储，文件存储在其中
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，会调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对文件的读取权限，并且还会检查 {@link RuntimePermission}<tt>
     *          ("getFileStoreAttributes")</tt>
     */
    public abstract FileStore getFileStore(Path path) throws IOException;

    /**
     * 检查文件的存在性，以及可选地检查文件的可访问性。
     *
     * <p> 此方法可能由 {@link Files#isReadable isReadable}、
     * {@link Files#isWritable isWritable} 和 {@link Files#isExecutable
     * isExecutable} 方法用于检查文件的可访问性。
     *
     * <p> 此方法检查文件的存在性，并且检查此 Java 虚拟机是否具有根据 {@code modes} 参数中指定的所有访问模式访问文件的适当权限，如下所示：
     *
     * <table border=1 cellpadding=5 summary="">
     * <tr> <th>值</th> <th>描述</th> </tr>
     * <tr>
     *   <td> {@link AccessMode#READ READ} </td>
     *   <td> 检查文件是否存在以及 Java 虚拟机是否有权限读取文件。 </td>
     * </tr>
     * <tr>
     *   <td> {@link AccessMode#WRITE WRITE} </td>
     *   <td> 检查文件是否存在以及 Java 虚拟机是否有权限写入文件。 </td>
     * </tr>
     * <tr>
     *   <td> {@link AccessMode#EXECUTE EXECUTE} </td>
     *   <td> 检查文件是否存在以及 Java 虚拟机是否有权限 {@link Runtime#exec 执行} 文件。检查目录的访问权限时，语义可能不同。例如，在 UNIX 系统上，检查 {@code EXECUTE} 访问权限会检查 Java 虚拟机是否有权限搜索目录以访问文件或子目录。 </td>
     * </tr>
     * </table>
     *
     * <p> 如果 {@code modes} 参数的长度为零，则仅检查文件的存在性。
     *
     * <p> 如果此对象引用的文件是一个符号链接，此方法会跟随符号链接。根据实现，此方法可能需要读取文件权限、访问控制列表或其他文件属性以检查对文件的有效访问。确定文件的有效访问可能需要访问多个属性，因此在某些实现中，此方法可能不是与其他文件系统操作原子的。
     *
     * @param   path
     *          要检查的文件的路径
     * @param   modes
     *          要检查的访问模式；可以有零个元素
     *
     * @throws  UnsupportedOperationException
     *          实现必须支持检查 {@code READ}、{@code WRITE} 和 {@code EXECUTE} 访问。此异常是为了允许在未来的版本中扩展 {@code Access} 枚举。
     * @throws  NoSuchFileException
     *          如果文件不存在 <i>(可选特定异常)</i>
     * @throws  AccessDeniedException
     *          请求的访问被拒绝或无法确定访问权限，因为 Java 虚拟机的权限不足或其他原因。 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，当检查文件的读取权限或仅检查文件的存在性时，会调用 {@link SecurityManager#checkRead(String) checkRead}，
     *          当检查文件的写入权限时，会调用 {@link SecurityManager#checkWrite(String) checkWrite}，
     *          当检查执行权限时，会调用 {@link SecurityManager#checkExec(String) checkExec}。
     */
    public abstract void checkAccess(Path path, AccessMode... modes)
        throws IOException;

    /**
     * 返回给定类型的文件属性视图。此方法的工作方式与 {@link Files#getFileAttributeView} 方法完全相同。
     *
     * @param   <V>
     *          {@code FileAttributeView} 类型
     * @param   path
     *          文件的路径
     * @param   type
     *          文件属性视图对应的 {@code Class} 对象
     * @param   options
     *          指定如何处理符号链接的选项
     *
     * @return  指定类型的文件属性视图，如果属性视图类型不可用，则返回 {@code null}
     */
    public abstract <V extends FileAttributeView> V
        getFileAttributeView(Path path, Class<V> type, LinkOption... options);

    /**
     * 以批量操作方式读取文件的属性。此方法的工作方式与 {@link
     * Files#readAttributes(Path,Class,LinkOption[])} 方法完全相同。
     *
     * @param   <A>
     *          {@code BasicFileAttributes} 类型
     * @param   path
     *          文件的路径
     * @param   type
     *          要读取的文件属性的 {@code Class}
     * @param   options
     *          指定如何处理符号链接的选项
     *
     * @return  文件属性
     *
     * @throws  UnsupportedOperationException
     *          如果不支持给定类型的属性
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，会调用其 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对文件的读取权限
     */
    public abstract <A extends BasicFileAttributes> A
        readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException;

    /**
     * 以批量操作方式读取一组文件属性。此方法的工作方式与 {@link
     * Files#readAttributes(Path,String,LinkOption[])} 方法完全相同。
     *
     * @param   path
     *          文件的路径
     * @param   attributes
     *          要读取的属性
     * @param   options
     *          指定如何处理符号链接的选项
     *
     * @return  返回的属性的映射；可以为空。映射的键是属性名称，值是属性值
     *
     * @throws  UnsupportedOperationException
     *          如果属性视图不可用
     * @throws  IllegalArgumentException
     *          如果未指定属性或指定了不识别的属性
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，其 {@link SecurityManager#checkRead(String) checkRead}
     *          方法拒绝对文件的读取权限。如果此方法用于读取安全敏感属性，安全经理可能会被调用以检查其他权限。
     */
    public abstract Map<String,Object> readAttributes(Path path, String attributes,
                                                      LinkOption... options)
        throws IOException;

    /**
     * 设置文件属性的值。此方法的工作方式与 {@link Files#setAttribute} 方法完全相同。
     *
     * @param   path
     *          文件的路径
     * @param   attribute
     *          要设置的属性
     * @param   value
     *          属性值
     * @param   options
     *          指定如何处理符号链接的选项
     *
     * @throws  UnsupportedOperationException
     *          如果属性视图不可用
     * @throws  IllegalArgumentException
     *          如果未指定属性名称，或不识别属性名称，或属性值类型正确但值不合适
     * @throws  ClassCastException
     *          如果属性值不是预期类型，或者是一个包含非预期类型元素的集合
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，其 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法拒绝对文件的写入权限。如果此方法用于设置安全敏感属性，安全经理可能会被调用以检查其他权限。
     */
    public abstract void setAttribute(Path path, String attribute,
                                      Object value, LinkOption... options)
        throws IOException;
}
