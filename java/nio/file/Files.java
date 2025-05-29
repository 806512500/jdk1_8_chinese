
/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.nio.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;   // javadoc
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.spi.FileSystemProvider;
import java.nio.file.spi.FileTypeDetector;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 该类仅包含用于操作文件、目录或其他类型文件的静态方法。
 *
 * <p> 在大多数情况下，这里定义的方法将委托给关联的文件系统提供程序来执行文件操作。
 *
 * @since 1.7
 */

public final class Files {
    private Files() { }

    /**
     * 返回要委托的 {@code FileSystemProvider}。
     */
    private static FileSystemProvider provider(Path path) {
        return path.getFileSystem().provider();
    }

    /**
     * 通过将检查的 IOException 转换为 UncheckedIOException，将 Closeable 转换为 Runnable。
     */
    private static Runnable asUncheckedRunnable(Closeable c) {
        return () -> {
            try {
                c.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    // -- 文件内容 --

    /**
     * 打开一个文件，返回一个用于从文件读取的输入流。该流不会被缓冲，并且不需要支持 {@link
     * InputStream#mark mark} 或 {@link InputStream#reset reset} 方法。该流可以安全地由多个并发线程访问。读取从文件的开头开始。返回的流是否
     * <i>异步关闭</i>和/或<i>可中断</i>高度依赖于文件系统提供程序，因此未指定。
     *
     * <p> {@code options} 参数确定如何打开文件。如果没有选项，则等同于使用 {@link StandardOpenOption#READ READ} 选项打开文件。除了 {@code
     * READ} 选项外，实现还可能支持其他特定于实现的选项。
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
     *          对于默认提供程序，如果安装了安全管理器，则调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对文件的读取权限。
     */
    public static InputStream newInputStream(Path path, OpenOption... options)
        throws IOException
    {
        return provider(path).newInputStream(path, options);
    }

    /**
     * 打开或创建一个文件，返回一个可以用于向文件写入字节的输出流。结果流不会被缓冲。该流可以安全地由多个并发线程访问。返回的流是否
     * <i>异步关闭</i>和/或<i>可中断</i>高度依赖于文件系统提供程序，因此未指定。
     *
     * <p> 该方法以 {@link #newByteChannel(Path,Set,FileAttribute[]) newByteChannel}
     * 方法指定的方式打开或创建文件，但 {@link StandardOpenOption#READ READ} 选项不能出现在选项数组中。如果没有选项，则此方法的行为就像
     * {@link StandardOpenOption#CREATE CREATE}、{@link StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING} 和
     * {@link StandardOpenOption#WRITE WRITE} 选项都存在一样。换句话说，它打开文件进行写入，如果文件不存在则创建文件，或如果文件存在则将其截断为
     * {@code 0} 的大小。
     *
     * <p> <b>使用示例：</b>
     * <pre>
     *     Path path = ...
     *
     *     // 截断并覆盖现有文件，或如果文件最初不存在则创建文件
     *     OutputStream out = Files.newOutputStream(path);
     *
     *     // 追加到现有文件，如果文件不存在则失败
     *     out = Files.newOutputStream(path, APPEND);
     *
     *     // 追加到现有文件，如果文件最初不存在则创建文件
     *     out = Files.newOutputStream(path, CREATE, APPEND);
     *
     *     // 始终创建新文件，如果文件已存在则失败
     *     out = Files.newOutputStream(path, CREATE_NEW);
     * </pre>
     *
     * @param   path
     *          要打开或创建的文件的路径
     * @param   options
     *          指定如何打开文件的选项
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
     *          对于默认提供程序，如果安装了安全管理器，则调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法检查对文件的写入权限。如果文件以 {@code DELETE_ON_CLOSE} 选项打开，则调用 {@link
     *          SecurityManager#checkDelete(String) checkDelete} 方法检查删除权限。
     */
    public static OutputStream newOutputStream(Path path, OpenOption... options)
        throws IOException
    {
        return provider(path).newOutputStream(path, options);
    }


                /**
     * 打开或创建一个文件，返回一个可寻址的字节通道以访问该文件。
     *
     * <p> {@code options} 参数确定文件如何被打开。
     * {@link StandardOpenOption#READ READ} 和 {@link
     * StandardOpenOption#WRITE WRITE} 选项确定文件是否应被打开以进行读取和/或写入。如果未指定这两个选项（或 {@link
     * StandardOpenOption#APPEND APPEND} 选项），则文件将被打开以进行读取。默认情况下，读取或写入从文件的开头开始。
     *
     * <p> 除了 {@code READ} 和 {@code WRITE} 之外，还可以包含以下选项：
     *
     * <table border=1 cellpadding=5 summary="Options">
     * <tr> <th>选项</th> <th>描述</th> </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#APPEND APPEND} </td>
     *   <td> 如果此选项存在，则文件将被打开以进行写入，并且每次调用通道的 {@code write} 方法时，首先将位置移动到文件的末尾，然后写入请求的数据。位置的移动和数据的写入是否在一个原子操作中完成取决于系统，因此未指定。此选项不能与 {@code READ} 或 {@code TRUNCATE_EXISTING} 选项一起使用。 </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING} </td>
     *   <td> 如果此选项存在，则现有文件将被截断为 0 字节。如果文件仅被打开以进行读取，则此选项将被忽略。 </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#CREATE_NEW CREATE_NEW} </td>
     *   <td> 如果此选项存在，则将创建一个新文件，如果文件已存在或是一个符号链接，则会失败。在创建文件时，检查文件是否存在以及如果不存在则创建文件的操作是相对于其他文件系统操作的原子操作。如果文件仅被打开以进行读取，则此选项将被忽略。 </td>
     * </tr>
     * <tr>
     *   <td > {@link StandardOpenOption#CREATE CREATE} </td>
     *   <td> 如果此选项存在，则如果文件存在则打开现有文件，否则创建一个新文件。如果 {@code CREATE_NEW} 选项也存在或文件仅被打开以进行读取，则此选项将被忽略。 </td>
     * </tr>
     * <tr>
     *   <td > {@link StandardOpenOption#DELETE_ON_CLOSE DELETE_ON_CLOSE} </td>
     *   <td> 当此选项存在时，实现将尽力在通过 {@link SeekableByteChannel#close close} 方法关闭文件时删除文件。如果未调用 {@code close} 方法，则在 Java 虚拟机终止时将尽力删除文件。 </td>
     * </tr>
     * <tr>
     *   <td>{@link StandardOpenOption#SPARSE SPARSE} </td>
     *   <td> 在创建新文件时，此选项是一个提示，表示新文件将是稀疏文件。如果未创建新文件，则此选项将被忽略。 </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#SYNC SYNC} </td>
     *   <td> 要求文件内容或元数据的每次更新都同步写入底层存储设备。（参见 <a
     *   href="package-summary.html#integrity"> 同步 I/O 文件完整性</a>）。 </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardOpenOption#DSYNC DSYNC} </td>
     *   <td> 要求文件内容的每次更新都同步写入底层存储设备。（参见 <a
     *   href="package-summary.html#integrity"> 同步 I/O 文件完整性</a>）。 </td>
     * </tr>
     * </table>
     *
     * <p> 实现还可能支持其他特定于实现的选项。
     *
     * <p> {@code attrs} 参数是在创建新文件时原子设置的可选 {@link FileAttribute
     * 文件属性}。
     *
     * <p> 在默认提供者的情况下，返回的可寻址字节通道是一个 {@link java.nio.channels.FileChannel}。
     *
     * <p> <b>使用示例：</b>
     * <pre>
     *     Path path = ...
     *
     *     // 打开文件以进行读取
     *     ReadableByteChannel rbc = Files.newByteChannel(path, EnumSet.of(READ)));
     *
     *     // 打开文件以在现有文件的末尾写入，如果文件不存在则创建
     *     WritableByteChannel wbc = Files.newByteChannel(path, EnumSet.of(CREATE,APPEND));
     *
     *     // 以初始权限创建文件，打开文件以进行读取和写入
     *     {@code FileAttribute<Set<PosixFilePermission>> perms = ...}
     *     SeekableByteChannel sbc = Files.newByteChannel(path, EnumSet.of(CREATE_NEW,READ,WRITE), perms);
     * </pre>
     *
     * @param   path
     *          要打开或创建的文件的路径
     * @param   options
     *          指定文件如何被打开的选项
     * @param   attrs
     *          在创建文件时原子设置的可选文件属性列表
     *
     * @return  一个新的可寻址字节通道
     *
     * @throws  IllegalArgumentException
     *          如果选项集中包含无效的选项组合
     * @throws  UnsupportedOperationException
     *          如果指定了不支持的打开选项，或者数组中包含在创建文件时不能原子设置的属性
     * @throws  FileAlreadyExistsException
     *          如果同名文件已存在且指定了 {@link
     *          StandardOpenOption#CREATE_NEW CREATE_NEW} 选项
     *          <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理器，将调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对路径的读取权限，如果文件被打开以进行读取。将调用 {@link SecurityManager#checkWrite(String)
     *          checkWrite} 方法检查对路径的写入权限，如果文件被打开以进行写入。如果文件被打开时指定了 {@code DELETE_ON_CLOSE} 选项，将调用 {@link
     *          SecurityManager#checkDelete(String) checkDelete} 方法检查删除权限。
     *
     * @see java.nio.channels.FileChannel#open(Path,Set,FileAttribute[])
     */
    public static SeekableByteChannel newByteChannel(Path path,
                                                     Set<? extends OpenOption> options,
                                                     FileAttribute<?>... attrs)
        throws IOException
    {
        return provider(path).newByteChannel(path, options, attrs);
    }

                /**
     * 打开或创建一个文件，返回一个可寻址的字节通道以访问该文件。
     *
     * <p> 此方法以 {@link #newByteChannel(Path,Set,FileAttribute[]) newByteChannel}
     * 方法指定的确切方式打开或创建文件。
     *
     * @param   path
     *          要打开或创建的文件的路径
     * @param   options
     *          指定文件如何打开的选项
     *
     * @return  一个新的可寻址字节通道
     *
     * @throws  IllegalArgumentException
     *          如果选项集中包含无效的选项组合
     * @throws  UnsupportedOperationException
     *          如果指定了不支持的打开选项
     * @throws  FileAlreadyExistsException
     *          如果具有该名称的文件已存在，并且指定了 {@link
     *          StandardOpenOption#CREATE_NEW CREATE_NEW} 选项
     *          <i>(可选的具体异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全管理器，则会调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对路径的读取权限。如果文件以写入方式打开，则调用 {@link SecurityManager#checkWrite(String)
     *          checkWrite} 方法检查对路径的写入权限。如果文件以 {@code DELETE_ON_CLOSE} 选项打开，则调用 {@link
     *          SecurityManager#checkDelete(String) checkDelete} 方法检查删除权限。
     *
     * @see java.nio.channels.FileChannel#open(Path,OpenOption[])
     */
    public static SeekableByteChannel newByteChannel(Path path, OpenOption... options)
        throws IOException
    {
        Set<OpenOption> set = new HashSet<OpenOption>(options.length);
        Collections.addAll(set, options);
        return newByteChannel(path, set);
    }

    // -- 目录 --

    private static class AcceptAllFilter
        implements DirectoryStream.Filter<Path>
    {
        private AcceptAllFilter() { }

        @Override
        public boolean accept(Path entry) { return true; }

        static final AcceptAllFilter FILTER = new AcceptAllFilter();
    }

    /**
     * 打开一个目录，返回一个 {@link DirectoryStream} 以迭代目录中的所有条目。目录流的 {@link DirectoryStream#iterator iterator}
     * 返回的元素类型为 {@code Path}，每个元素代表目录中的一个条目。这些 {@code Path} 对象是通过将目录条目的名称解析到 {@code dir} 获得的。
     *
     * <p> 如果不使用 try-with-resources 构造，则应在迭代完成后调用目录流的 {@code close} 方法以释放为打开目录持有的任何资源。
     *
     * <p> 如果实现支持以无竞争的方式执行目录条目的操作，则返回的目录流是一个 {@link SecureDirectoryStream}。
     *
     * @param   dir
     *          目录的路径
     *
     * @return  一个新的且已打开的 {@code DirectoryStream} 对象
     *
     * @throws  NotDirectoryException
     *          如果文件不能以其他方式打开，因为它不是一个目录 <i>(可选的具体异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全管理器，则会调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对目录的读取权限。
     */
    public static DirectoryStream<Path> newDirectoryStream(Path dir)
        throws IOException
    {
        return provider(dir).newDirectoryStream(dir, AcceptAllFilter.FILTER);
    }

    /**
     * 打开一个目录，返回一个 {@link DirectoryStream} 以迭代目录中的条目。目录流的 {@link DirectoryStream#iterator iterator}
     * 返回的元素类型为 {@code Path}，每个元素代表目录中的一个条目。这些 {@code Path} 对象是通过将目录条目的名称解析到 {@code dir} 获得的。
     * 迭代器返回的条目通过匹配其文件名的 {@code String} 表示与给定的 <em>通配符</em> 模式进行过滤。
     *
     * <p> 例如，假设我们想迭代目录中以 ".java" 结尾的文件：
     * <pre>
     *     Path dir = ...
     *     try (DirectoryStream&lt;Path&gt; stream = Files.newDirectoryStream(dir, "*.java")) {
     *         :
     *     }
     * </pre>
     *
     * <p> 通配符模式由 {@link
     * FileSystem#getPathMatcher getPathMatcher} 方法指定。
     *
     * <p> 如果不使用 try-with-resources 构造，则应在迭代完成后调用目录流的 {@code close} 方法以释放为打开目录持有的任何资源。
     *
     * <p> 如果实现支持以无竞争的方式执行目录条目的操作，则返回的目录流是一个 {@link SecureDirectoryStream}。
     *
     * @param   dir
     *          目录的路径
     * @param   glob
     *          通配符模式
     *
     * @return  一个新的且已打开的 {@code DirectoryStream} 对象
     *
     * @throws  java.util.regex.PatternSyntaxException
     *          如果模式无效
     * @throws  NotDirectoryException
     *          如果文件不能以其他方式打开，因为它不是一个目录 <i>(可选的具体异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全管理器，则会调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对目录的读取权限。
     */
    public static DirectoryStream<Path> newDirectoryStream(Path dir, String glob)
        throws IOException
    {
        // 如果需要所有条目，则避免创建匹配器。
        if (glob.equals("*"))
            return newDirectoryStream(dir);


                    // 创建一个匹配器并返回一个使用该匹配器的过滤器。
        FileSystem fs = dir.getFileSystem();
        final PathMatcher matcher = fs.getPathMatcher("glob:" + glob);
        DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry)  {
                return matcher.matches(entry.getFileName());
            }
        };
        return fs.provider().newDirectoryStream(dir, filter);
    }

    /**
     * 打开一个目录，返回一个 {@link DirectoryStream} 以迭代目录中的条目。目录流的 {@link DirectoryStream#iterator iterator}
     * 返回的元素类型为 {@code Path}，每个元素代表目录中的一个条目。这些 {@code Path} 对象是通过 {@link Path#resolve(Path) 解析}
     * 目录条目的名称与 {@code dir} 对比获得的。迭代器返回的条目通过给定的 {@link DirectoryStream.Filter 过滤器} 进行过滤。
     *
     * <p> 如果不使用 try-with-resources 构造，则应在迭代完成后调用目录流的 {@code close} 方法，以释放打开目录所持有的任何资源。
     *
     * <p> 如果过滤器因未捕获的错误或运行时异常而终止，则该异常将传播到 {@link Iterator#hasNext() hasNext} 或 {@link Iterator#next() next} 方法。
     * 如果抛出 {@code IOException}，则会导致 {@code hasNext} 或 {@code next} 方法抛出一个带有 {@code IOException} 作为原因的 {@link DirectoryIteratorException}。
     *
     * <p> 如果实现支持以无竞争的方式执行目录条目的操作，则返回的目录流是一个 {@link SecureDirectoryStream}。
     *
     * <p> <b>使用示例：</b>
     * 假设我们想要迭代目录中大于 8K 的文件。
     * <pre>
     *     DirectoryStream.Filter&lt;Path&gt; filter = new DirectoryStream.Filter&lt;Path&gt;() {
     *         public boolean accept(Path file) throws IOException {
     *             return (Files.size(file) &gt; 8192L);
     *         }
     *     };
     *     Path dir = ...
     *     try (DirectoryStream&lt;Path&gt; stream = Files.newDirectoryStream(dir, filter)) {
     *         :
     *     }
     * </pre>
     *
     * @param   dir
     *          目录的路径
     * @param   filter
     *          目录流过滤器
     *
     * @return  一个新的且已打开的 {@code DirectoryStream} 对象
     *
     * @throws  NotDirectoryException
     *          如果文件无法打开，因为它不是一个目录 <i>(可选的具体异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkRead(String) checkRead} 方法检查对目录的读取权限。
     */
    public static DirectoryStream<Path> newDirectoryStream(Path dir,
                                                           DirectoryStream.Filter<? super Path> filter)
        throws IOException
    {
        return provider(dir).newDirectoryStream(dir, filter);
    }

    // -- 创建和删除 --

    /**
     * 创建一个新的空文件，如果文件已存在则失败。检查文件是否存在以及如果不存在则创建新文件是单个操作，相对于所有可能影响目录的其他文件系统活动是原子的。
     *
     * <p> {@code attrs} 参数是创建文件时原子设置的可选 {@link FileAttribute 文件属性}。每个属性由其 {@link FileAttribute#name 名称} 标识。
     * 如果数组中包含多个同名属性，则除最后一个出现的属性外，其余均被忽略。
     *
     * @param   path
     *          要创建的文件的路径
     * @param   attrs
     *          创建文件时原子设置的可选文件属性列表
     *
     * @return  文件
     *
     * @throws  UnsupportedOperationException
     *          如果数组包含在创建文件时无法原子设置的属性
     * @throws  FileAlreadyExistsException
     *          如果同名文件已存在
     *          <i>(可选的具体异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误或父目录不存在
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkWrite(String) checkWrite} 方法检查对新文件的写入权限。
     */
    public static Path createFile(Path path, FileAttribute<?>... attrs)
        throws IOException
    {
        EnumSet<StandardOpenOption> options =
            EnumSet.<StandardOpenOption>of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        newByteChannel(path, options, attrs).close();
        return path;
    }

    /**
     * 创建一个新的目录。检查文件是否存在以及如果不存在则创建目录是单个操作，相对于所有可能影响目录的其他文件系统活动是原子的。
     * 如果需要首先创建所有不存在的父目录，应使用 {@link #createDirectories createDirectories} 方法。
     *
     * <p> {@code attrs} 参数是创建目录时原子设置的可选 {@link FileAttribute 文件属性}。每个属性由其 {@link FileAttribute#name 名称} 标识。
     * 如果数组中包含多个同名属性，则除最后一个出现的属性外，其余均被忽略。
     *
     * @param   dir
     *          要创建的目录
     * @param   attrs
     *          创建目录时原子设置的可选文件属性列表
     *
     * @return  目录
     *
     * @throws  UnsupportedOperationException
     *          如果数组包含在创建目录时无法原子设置的属性
     * @throws  FileAlreadyExistsException
     *          如果同名文件已存在，导致无法创建目录 <i>(可选的具体异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误或父目录不存在
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkWrite(String) checkWrite} 方法检查对新目录的写入权限。
     */
    public static Path createDirectory(Path dir, FileAttribute<?>... attrs)
        throws IOException
    {
        provider(dir).createDirectory(dir, attrs);
        return dir;
    }


                /**
     * 通过首先创建所有不存在的父目录来创建一个目录。
     * 与 {@link #createDirectory createDirectory} 方法不同，如果目录已经存在而无法创建时，不会抛出异常。
     *
     * <p> {@code attrs} 参数是可选的 {@link FileAttribute
     * 文件属性}，在创建不存在的目录时原子地设置。每个文件属性由其 {@link
     * FileAttribute#name 名称} 标识。如果数组中包含多个同名属性，则除了最后一次出现的属性外，其余的都被忽略。
     *
     * <p> 如果此方法失败，那么它可能在创建了一些但不是全部的父目录后失败。
     *
     * @param   dir
     *          要创建的目录
     *
     * @param   attrs
     *          在创建目录时原子地设置的可选文件属性列表
     *
     * @return  目录
     *
     * @throws  UnsupportedOperationException
     *          如果数组包含在创建目录时无法原子设置的属性
     * @throws  FileAlreadyExistsException
     *          如果 {@code dir} 存在但不是目录 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全管理器，则在尝试创建目录之前会调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法，并且对于每个检查的父目录会调用其 {@link SecurityManager#checkRead(String) checkRead} 方法。如果 {@code
     *          dir} 不是绝对路径，则可能需要调用其 {@link Path#toAbsolutePath
     *          toAbsolutePath} 方法以获取其绝对路径。这可能会调用安全管理器的 {@link
     *          SecurityManager#checkPropertyAccess(String) checkPropertyAccess}
     *          方法来检查对系统属性 {@code user.dir} 的访问权限
     */
    public static Path createDirectories(Path dir, FileAttribute<?>... attrs)
        throws IOException
    {
        // 尝试创建目录
        try {
            createAndCheckIsDirectory(dir, attrs);
            return dir;
        } catch (FileAlreadyExistsException x) {
            // 文件存在且不是目录
            throw x;
        } catch (IOException x) {
            // 父目录可能不存在或其他原因
        }
        SecurityException se = null;
        try {
            dir = dir.toAbsolutePath();
        } catch (SecurityException x) {
            // 没有权限获取绝对路径
            se = x;
        }
        // 查找存在的后代
        Path parent = dir.getParent();
        while (parent != null) {
            try {
                provider(parent).checkAccess(parent);
                break;
            } catch (NoSuchFileException x) {
                // 不存在
            }
            parent = parent.getParent();
        }
        if (parent == null) {
            // 无法找到存在的父目录
            if (se == null) {
                throw new FileSystemException(dir.toString(), null,
                    "无法确定根目录是否存在");
            } else {
                throw se;
            }
        }

        // 创建目录
        Path child = parent;
        for (Path name: parent.relativize(dir)) {
            child = child.resolve(name);
            createAndCheckIsDirectory(child, attrs);
        }
        return dir;
    }

    /**
     * 用于 createDirectories 尝试创建目录。如果目录已存在，则不执行任何操作。
     */
    private static void createAndCheckIsDirectory(Path dir,
                                                  FileAttribute<?>... attrs)
        throws IOException
    {
        try {
            createDirectory(dir, attrs);
        } catch (FileAlreadyExistsException x) {
            if (!isDirectory(dir, LinkOption.NOFOLLOW_LINKS))
                throw x;
        }
    }

    /**
     * 在指定目录中使用给定的前缀和后缀字符串生成名称来创建一个新空文件。生成的
     * {@code Path} 与给定目录关联的相同 {@code FileSystem}。
     *
     * <p> 文件名称的构造细节取决于实现，因此未指定。如果可能，{@code prefix} 和 {@code suffix} 用于以与 {@link
     * java.io.File#createTempFile(String,String,File)} 方法相同的方式构造候选名称。
     *
     * <p> 与 {@code File.createTempFile} 方法一样，此方法仅是临时文件设施的一部分。作为 <em>工作文件</em> 使用时，
     * 可以使用 {@link StandardOpenOption#DELETE_ON_CLOSE DELETE_ON_CLOSE} 选项打开文件，以便在调用适当的 {@code close} 方法时删除文件。
     * 或者，可以使用 {@link Runtime#addShutdownHook 关机钩子} 或 {@link java.io.File#deleteOnExit} 机制自动删除文件。
     *
     * <p> {@code attrs} 参数是可选的 {@link FileAttribute
     * 文件属性}，在创建文件时原子地设置。每个属性由其 {@link FileAttribute#name 名称} 标识。如果数组中包含多个同名属性，则除了最后一次出现的属性外，其余的都被忽略。当没有指定文件属性时，生成的文件可能具有比通过 {@link java.io.File#createTempFile(String,String,File)}
     * 方法创建的文件更严格的访问权限。
     *
     * @param   dir
     *          要在其中创建文件的目录路径
     * @param   prefix
     *          用于生成文件名称的前缀字符串；可以为 {@code null}
     * @param   suffix
     *          用于生成文件名称的后缀字符串；可以为 {@code null}，在这种情况下使用 "{@code .tmp}"
     * @param   attrs
     *          在创建文件时原子地设置的可选文件属性列表
     *
     * @return  在调用此方法之前不存在的新创建文件的路径
     *
     * @throws  IllegalArgumentException
     *          如果前缀或后缀参数无法用于生成候选文件名
     * @throws  UnsupportedOperationException
     *          如果数组包含在创建目录时无法原子设置的属性
     * @throws  IOException
     *          如果发生 I/O 错误或 {@code dir} 不存在
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全管理器，则会调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法来检查对文件的写入权限。
     */
    public static Path createTempFile(Path dir,
                                      String prefix,
                                      String suffix,
                                      FileAttribute<?>... attrs)
        throws IOException
    {
        return TempFileHelper.createTempFile(Objects.requireNonNull(dir),
                                             prefix, suffix, attrs);
    }

                /**
     * 在默认的临时文件目录中创建一个空文件，使用给定的前缀和后缀生成其名称。生成的 {@code
     * Path} 与默认的 {@code FileSystem} 关联。
     *
     * <p> 此方法的工作方式与 {@link #createTempFile(Path,String,String,FileAttribute[])} 方法完全相同，
     * 但 {@code dir} 参数为临时文件目录。
     *
     * @param   prefix
     *          用于生成文件名称的前缀字符串；可以为 {@code null}
     * @param   suffix
     *          用于生成文件名称的后缀字符串；可以为 {@code null}，在这种情况下使用 "{@code .tmp}"
     * @param   attrs
     *          一个可选的文件属性列表，用于在创建文件时原子性地设置
     *
     * @return  该方法调用前不存在的新创建文件的路径
     *
     * @throws  IllegalArgumentException
     *          如果前缀或后缀参数不能用于生成候选文件名
     * @throws  UnsupportedOperationException
     *          如果数组中包含一个在创建目录时不能原子性设置的属性
     * @throws  IOException
     *          如果发生 I/O 错误或临时文件目录不存在
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理器，则调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法检查文件的写权限。
     */
    public static Path createTempFile(String prefix,
                                      String suffix,
                                      FileAttribute<?>... attrs)
        throws IOException
    {
        return TempFileHelper.createTempFile(null, prefix, suffix, attrs);
    }

    /**
     * 在指定的目录中使用给定的前缀生成其名称创建一个新的目录。生成的 {@code Path} 与给定目录的
     * {@code FileSystem} 关联。
     *
     * <p> 目录名称的构造方式取决于实现，因此未指定。尽可能使用 {@code prefix} 构建候选名称。
     *
     * <p> 与 {@code createTempFile} 方法一样，此方法仅是临时文件设施的一部分。可以使用 {@link Runtime#addShutdownHook
     * shutdown-hook} 或 {@link java.io.File#deleteOnExit} 机制自动删除目录。
     *
     * <p> {@code attrs} 参数是创建目录时原子性设置的可选 {@link FileAttribute
     * 文件属性}。每个属性由其 {@link FileAttribute#name 名称} 标识。如果数组中包含同名的多个属性，则除最后一个外，其余均被忽略。
     *
     * @param   dir
     *          要在其中创建目录的目录路径
     * @param   prefix
     *          用于生成目录名称的前缀字符串；可以为 {@code null}
     * @param   attrs
     *          一个可选的文件属性列表，用于在创建目录时原子性地设置
     *
     * @return  该方法调用前不存在的新创建目录的路径
     *
     * @throws  IllegalArgumentException
     *          如果前缀不能用于生成候选目录名称
     * @throws  UnsupportedOperationException
     *          如果数组中包含一个在创建目录时不能原子性设置的属性
     * @throws  IOException
     *          如果发生 I/O 错误或 {@code dir} 不存在
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理器，则调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法检查创建目录时的写权限。
     */
    public static Path createTempDirectory(Path dir,
                                           String prefix,
                                           FileAttribute<?>... attrs)
        throws IOException
    {
        return TempFileHelper.createTempDirectory(Objects.requireNonNull(dir),
                                                  prefix, attrs);
    }

    /**
     * 在默认的临时文件目录中使用给定的前缀生成其名称创建一个新的目录。生成的 {@code Path} 与默认的
     * {@code FileSystem} 关联。
     *
     * <p> 此方法的工作方式与 {@link
     * #createTempDirectory(Path,String,FileAttribute[])} 方法完全相同，但 {@code dir} 参数为临时文件目录。
     *
     * @param   prefix
     *          用于生成目录名称的前缀字符串；可以为 {@code null}
     * @param   attrs
     *          一个可选的文件属性列表，用于在创建目录时原子性地设置
     *
     * @return  该方法调用前不存在的新创建目录的路径
     *
     * @throws  IllegalArgumentException
     *          如果前缀不能用于生成候选目录名称
     * @throws  UnsupportedOperationException
     *          如果数组中包含一个在创建目录时不能原子性设置的属性
     * @throws  IOException
     *          如果发生 I/O 错误或临时文件目录不存在
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理器，则调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法检查创建目录时的写权限。
     */
    public static Path createTempDirectory(String prefix,
                                           FileAttribute<?>... attrs)
        throws IOException
    {
        return TempFileHelper.createTempDirectory(null, prefix, attrs);
    }

                /**
     * 创建指向目标的符号链接 <i>(可选操作)</i>。
     *
     * <p> {@code target} 参数是链接的目标。它可以是 {@link Path#isAbsolute 绝对} 路径或相对路径，并且可能不存在。当目标是相对路径时，对结果链接的文件系统操作相对于链接的路径。
     *
     * <p> {@code attrs} 参数是在创建链接时原子设置的可选 {@link FileAttribute 属性}。每个属性由其 {@link FileAttribute#name 名称} 标识。如果数组中包含多个同名属性，则除了最后一次出现外，其他所有出现都将被忽略。
     *
     * <p> 如果支持符号链接，但底层 {@link FileStore} 不支持符号链接，则可能会因 {@link IOException} 而失败。此外，某些操作系统可能需要 Java 虚拟机以特定于实现的权限启动，以创建符号链接，在这种情况下，此方法可能会抛出 {@code IOException}。
     *
     * @param   link
     *          要创建的符号链接的路径
     * @param   target
     *          符号链接的目标
     * @param   attrs
     *          在创建符号链接时原子设置的属性数组
     *
     * @return  符号链接的路径
     *
     * @throws  UnsupportedOperationException
     *          如果实现不支持符号链接或数组包含在创建符号链接时不能原子设置的属性
     * @throws  FileAlreadyExistsException
     *          如果已经存在同名文件 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，它拒绝 {@link LinkPermission}<tt>("symbolic")</tt>
     *          或其 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法拒绝对符号链接路径的写入访问。
     */
    public static Path createSymbolicLink(Path link, Path target,
                                          FileAttribute<?>... attrs)
        throws IOException
    {
        provider(link).createSymbolicLink(link, target, attrs);
        return link;
    }

    /**
     * 为现有文件创建新的链接（目录项）<i>(可选操作)</i>。
     *
     * <p> {@code link} 参数定位要创建的目录项。{@code existing} 参数是现有文件的路径。此方法为文件创建新的目录项，以便可以使用 {@code link} 作为路径访问文件。在某些文件系统中，这被称为创建“硬链接”。文件属性是为文件还是为每个目录项维护的，具体取决于文件系统，因此未指定。通常，文件系统要求文件的所有链接（目录项）位于同一文件系统上。此外，在某些平台上，Java 虚拟机可能需要以特定于实现的权限启动，以创建硬链接或创建指向目录的链接。
     *
     * @param   link
     *          要创建的链接（目录项）
     * @param   existing
     *          现有文件的路径
     *
     * @return  链接（目录项）的路径
     *
     * @throws  UnsupportedOperationException
     *          如果实现不支持将现有文件添加到目录
     * @throws  FileAlreadyExistsException
     *          如果由于同名文件已存在而无法创建条目 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，它拒绝 {@link LinkPermission}<tt>("hard")</tt>
     *          或其 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法拒绝对链接或现有文件的写入访问。
     */
    public static Path createLink(Path link, Path existing) throws IOException {
        provider(link).createLink(link, existing);
        return link;
    }

    /**
     * 删除文件。
     *
     * <p> 实现可能需要检查文件以确定文件是否为目录。因此，此方法可能与其他文件系统操作不具有原子性。如果文件是符号链接，则删除符号链接本身，而不是链接的最终目标。
     *
     * <p> 如果文件是目录，则目录必须为空。在某些实现中，目录在创建时会创建特殊文件或链接的条目。在这种实现中，当仅存在特殊条目时，目录被视为为空。此方法可以与 {@link #walkFileTree walkFileTree} 方法一起使用，以删除目录及其所有条目，或在需要时删除整个 <i>文件树</i>。
     *
     * <p> 在某些操作系统上，当文件被此 Java 虚拟机或其他程序打开并使用时，可能无法删除文件。
     *
     * @param   path
     *          要删除的文件的路径
     *
     * @throws  NoSuchFileException
     *          如果文件不存在 <i>(可选特定异常)</i>
     * @throws  DirectoryNotEmptyException
     *          如果文件是目录且由于目录不为空而无法删除 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，将调用 {@link SecurityManager#checkDelete(String)} 方法检查文件的删除权限
     */
    public static void delete(Path path) throws IOException {
        provider(path).delete(path);
    }

                /**
     * 如果文件存在，则删除该文件。
     *
     * <p> 与 {@link #delete(Path) delete(Path)} 方法一样，实现可能需要检查文件以确定文件是否为目录。因此，此方法可能与其他文件系统操作不具有原子性。如果文件是符号链接，则删除符号链接本身，而不是链接的最终目标。
     *
     * <p> 如果文件是目录，则目录必须为空。在某些实现中，当目录创建时会创建特殊文件或链接的条目。在这种实现中，当只有特殊条目存在时，目录被认为是空的。
     *
     * <p> 在某些操作系统上，当文件被此 Java 虚拟机或其他程序打开并使用时，可能无法删除文件。
     *
     * @param   path
     *          要删除的文件的路径
     *
     * @return  如果文件由该方法删除，则返回 {@code true}；如果文件不存在而无法删除，则返回 {@code
     *          false}
     *
     * @throws  DirectoryNotEmptyException
     *          如果文件是目录且无法删除，因为目录不为空 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理器，则调用 {@link SecurityManager#checkDelete(String)} 方法来检查文件的删除权限。
     */
    public static boolean deleteIfExists(Path path) throws IOException {
        return provider(path).deleteIfExists(path);
    }

    // -- 文件复制和移动 --

    /**
     * 将文件复制到目标文件。
     *
     * <p> 此方法使用 {@code options} 参数指定的方式将文件复制到目标文件。默认情况下，如果目标文件已存在或为符号链接，则复制失败，除非源文件和目标文件是 {@link #isSameFile 同一个} 文件，在这种情况下，方法在不复制文件的情况下完成。不要求将文件属性复制到目标文件。如果支持符号链接，且文件为符号链接，则复制链接的最终目标。如果文件是目录，则在目标位置创建一个空目录（目录中的条目不会被复制）。此方法可以与 {@link #walkFileTree walkFileTree} 方法一起使用，以复制目录及其所有条目，或根据需要复制整个 <i>文件树</i>。
     *
     * <p> {@code options} 参数可以包括以下任何一个：
     *
     * <table border=1 cellpadding=5 summary="">
     * <tr> <th>选项</th> <th>描述</th> </tr>
     * <tr>
     *   <td> {@link StandardCopyOption#REPLACE_EXISTING REPLACE_EXISTING} </td>
     *   <td> 如果目标文件存在，则替换目标文件，除非它是非空目录。如果目标文件存在且为符号链接，则替换符号链接本身，而不是链接的目标。 </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardCopyOption#COPY_ATTRIBUTES COPY_ATTRIBUTES} </td>
     *   <td> 尝试将与此文件关联的文件属性复制到目标文件。确切复制的文件属性取决于平台和文件系统，因此未指定。至少，如果源文件存储和目标文件存储都支持，则 {@link BasicFileAttributes#lastModifiedTime 最后修改时间} 会被复制到目标文件。文件时间戳的复制可能导致精度损失。 </td>
     * </tr>
     * <tr>
     *   <td> {@link LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} </td>
     *   <td> 不跟随符号链接。如果文件是符号链接，则复制符号链接本身，而不是链接的目标。是否可以将文件属性复制到新链接是实现特定的。换句话说，当复制符号链接时，可能会忽略 {@code COPY_ATTRIBUTES} 选项。 </td>
     * </tr>
     * </table>
     *
     * <p> 此接口的实现可能支持其他实现特定的选项。
     *
     * <p> 复制文件不是原子操作。如果抛出 {@link IOException}，则目标文件可能是不完整的，或者其某些文件属性尚未从源文件复制。当指定 {@code REPLACE_EXISTING} 选项且目标文件存在时，则替换目标文件。检查文件是否存在以及创建新文件可能与其他文件系统活动不具有原子性。
     *
     * <p> <b>使用示例：</b>
     * 假设我们想将文件复制到目录中，并给它与源文件相同的文件名：
     * <pre>
     *     Path source = ...
     *     Path newdir = ...
     *     Files.copy(source, newdir.resolve(source.getFileName());
     * </pre>
     *
     * @param   source
     *          要复制的文件的路径
     * @param   target
     *          目标文件的路径（可能与源路径关联不同的提供者）
     * @param   options
     *          指定如何进行复制的选项
     *
     * @return  目标文件的路径
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
     *          在默认提供者的情况下，如果安装了安全管理器，则调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法来检查源文件的读取权限，调用 {@link SecurityManager#checkWrite(String) checkWrite} 方法来检查目标文件的写入权限。如果复制了符号链接，则调用安全管理器来检查 {@link
     *          LinkPermission}{@code ("symbolic")}。
     */
    public static Path copy(Path source, Path target, CopyOption... options)
        throws IOException
    {
        FileSystemProvider provider = provider(source);
        if (provider(target) == provider) {
            // 同一提供者
            provider.copy(source, target, options);
        } else {
            // 不同提供者
            CopyMoveHelper.copyToForeignTarget(source, target, options);
        }
        return target;
    }

                /**
     * 将文件移动或重命名到目标文件。
     *
     * <p> 默认情况下，此方法尝试将文件移动到目标文件，如果目标文件已存在则失败，除非源文件和目标文件是 {@link #isSameFile 同一个} 文件，在这种情况下，此方法没有效果。如果文件是符号链接，则移动符号链接本身，而不是链接的目标。此方法可以被调用来移动一个空目录。在某些实现中，目录在创建时会包含特殊文件或链接的条目。在这些实现中，当只有特殊条目存在时，目录被认为是空的。当调用此方法来移动一个非空目录时，如果不需要移动目录中的条目，则目录将被移动。例如，在同一个 {@link FileStore} 上重命名目录通常不需要移动目录中的条目。当移动目录需要移动其条目时，此方法会失败（通过抛出一个 {@code IOException}）。要移动一个 <i>文件树</i> 可能涉及复制而不是移动目录，这可以使用 {@link #copy copy} 方法结合 {@link #walkFileTree Files.walkFileTree} 工具方法来完成。
     *
     * <p> {@code options} 参数可以包括以下任何一个选项：
     *
     * <table border=1 cellpadding=5 summary="">
     * <tr> <th>选项</th> <th>描述</th> </tr>
     * <tr>
     *   <td> {@link StandardCopyOption#REPLACE_EXISTING REPLACE_EXISTING} </td>
     *   <td> 如果目标文件已存在，则替换目标文件，除非它是一个非空目录。如果目标文件已存在并且是一个符号链接，则替换符号链接本身，而不是链接的目标。 </td>
     * </tr>
     * <tr>
     *   <td> {@link StandardCopyOption#ATOMIC_MOVE ATOMIC_MOVE} </td>
     *   <td> 移动操作作为一个原子文件系统操作执行，所有其他选项将被忽略。如果目标文件已存在，则具体实现决定是替换现有文件还是通过抛出一个 {@link IOException} 使此方法失败。如果移动操作不能作为一个原子文件系统操作执行，则抛出 {@link AtomicMoveNotSupportedException}。例如，当目标位置位于不同的 {@code FileStore} 上，需要复制文件，或目标位置与该对象关联的提供者不同时，可能会出现这种情况。 </td>
     * </table>
     *
     * <p> 此接口的实现可能支持额外的特定实现选项。
     *
     * <p> 移动文件时，如果源文件存储和目标文件存储都支持，将复制 {@link BasicFileAttributes#lastModifiedTime 最后修改时间} 到目标文件。复制文件时间戳可能导致精度损失。实现也可能尝试复制其他文件属性，但不要求在无法复制文件属性时失败。当移动操作作为一个非原子操作执行，并且抛出 {@code IOException} 时，文件的状态是未定义的。原始文件和目标文件都可能存在，目标文件可能不完整，或者其某些文件属性可能未从原始文件复制。
     *
     * <p> <b>使用示例：</b>
     * 假设我们想将一个文件重命名为 "newname"，保持文件在同一目录中：
     * <pre>
     *     Path source = ...
     *     Files.move(source, source.resolveSibling("newname"));
     * </pre>
     * 或者，假设我们想将一个文件移动到新目录中，保持相同的文件名，并替换目标目录中同名的任何现有文件：
     * <pre>
     *     Path source = ...
     *     Path newdir = ...
     *     Files.move(source, newdir.resolve(source.getFileName()), REPLACE_EXISTING);
     * </pre>
     *
     * @param   source
     *          要移动的文件的路径
     * @param   target
     *          目标文件的路径（可能与源路径关联的提供者不同）
     * @param   options
     *          指定如何执行移动的选项
     *
     * @return  目标文件的路径
     *
     * @throws  UnsupportedOperationException
     *          如果数组包含不支持的复制选项
     * @throws  FileAlreadyExistsException
     *          如果目标文件已存在但不能被替换，因为未指定 {@code REPLACE_EXISTING} 选项 <i>(可选的具体异常)</i>
     * @throws  DirectoryNotEmptyException
     *          如果指定了 {@code REPLACE_EXISTING} 选项，但文件不能被替换，因为它是一个非空目录 <i>(可选的具体异常)</i>
     * @throws  AtomicMoveNotSupportedException
     *          如果选项数组包含 {@code ATOMIC_MOVE} 选项，但文件不能作为一个原子文件系统操作移动。
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理器，则调用 {@link SecurityManager#checkWrite(String) checkWrite} 方法来检查对源文件和目标文件的写访问权限。
     */
    public static Path move(Path source, Path target, CopyOption... options)
        throws IOException
    {
        FileSystemProvider provider = provider(source);
        if (provider(target) == provider) {
            // 同一提供者
            provider.move(source, target, options);
        } else {
            // 不同提供者
            CopyMoveHelper.moveToForeignTarget(source, target, options);
        }
        return target;
    }

                // -- 杂项 --

    /**
     * 读取符号链接的目标 <i>(可选操作)</i>。
     *
     * <p> 如果文件系统支持 <a href="package-summary.html#links">符号链接</a>，则此方法用于读取链接的目标，如果文件不是符号链接则会失败。链接的目标不必存在。
     * 返回的 {@code Path} 对象将与 {@code link} 相同的文件系统相关联。
     *
     * @param   link
     *          符号链接的路径
     *
     * @return  一个表示链接目标的 {@code Path} 对象
     *
     * @throws  UnsupportedOperationException
     *          如果实现不支持符号链接
     * @throws  NotLinkException
     *          如果由于文件不是符号链接而无法读取目标 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，它会检查是否授予了带有 "{@code readlink}" 操作的 {@code FilePermission} 以读取链接。
     */
    public static Path readSymbolicLink(Path link) throws IOException {
        return provider(link).readSymbolicLink(link);
    }

    /**
     * 返回表示文件所在文件存储的 {@link FileStore}。
     *
     * <p> 一旦获得了 {@code FileStore} 的引用，具体实现将决定对返回的 {@code FileStore} 或从其获取的 {@link FileStoreAttributeView} 对象的操作是否继续依赖于文件的存在。
     * 特别是，如果文件被删除或移动到不同的文件存储，行为是未定义的。
     *
     * @param   path
     *          文件的路径
     *
     * @return  存储文件的文件存储
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，将调用 {@link SecurityManager#checkRead(String) checkRead} 方法检查文件的读取权限，并且还会检查 {@link RuntimePermission}<tt>
     *          ("getFileStoreAttributes")</tt>
     */
    public static FileStore getFileStore(Path path) throws IOException {
        return provider(path).getFileStore(path);
    }

    /**
     * 测试两个路径是否指向同一个文件。
     *
     * <p> 如果两个 {@code Path} 对象 {@link Path#equals(Object) 相等}，则此方法返回 {@code true} 而不检查文件是否存在。
     * 如果两个 {@code Path} 对象与不同的提供者相关联，则此方法返回 {@code false}。否则，此方法检查两个 {@code Path} 对象是否指向同一个文件，具体实现可能需要打开或访问两个文件。
     *
     * <p> 如果文件系统和文件保持静态，那么此方法为非空 {@code Paths} 实现等价关系。
     * <ul>
     * <li>它是 <i>自反的</i>：对于 {@code Path} {@code f}，
     *     {@code isSameFile(f,f)} 应返回 {@code true}。
     * <li>它是 <i>对称的</i>：对于两个 {@code Paths} {@code f} 和 {@code g}，
     *     {@code isSameFile(f,g)} 将等于 {@code isSameFile(g,f)}。
     * <li>它是 <i>传递的</i>：对于三个 {@code Paths}
     *     {@code f}, {@code g}, 和 {@code h}，如果 {@code isSameFile(f,g)} 返回
     *     {@code true} 且 {@code isSameFile(g,h)} 返回 {@code true}，那么
     *     {@code isSameFile(f,h)} 将返回 {@code true}。
     * </ul>
     *
     * @param   path
     *          一个文件的路径
     * @param   path2
     *          另一个路径
     *
     * @return  如果且仅当两个路径指向同一个文件时返回 {@code true}
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，将调用 {@link SecurityManager#checkRead(String) checkRead} 方法检查两个文件的读取权限。
     *
     * @see java.nio.file.attribute.BasicFileAttributes#fileKey
     */
    public static boolean isSameFile(Path path, Path path2) throws IOException {
        return provider(path).isSameFile(path, path2);
    }

    /**
     * 告知文件是否被认为是 <em>隐藏的</em>。隐藏的确切定义取决于平台或提供者。例如，在 UNIX 上，如果文件名以句点字符 ('.') 开头，则认为文件是隐藏的。
     * 在 Windows 上，如果文件不是目录且 DOS {@link DosFileAttributes#isHidden 隐藏} 属性已设置，则认为文件是隐藏的。
     *
     * <p> 根据具体实现，此方法可能需要访问文件系统以确定文件是否被认为是隐藏的。
     *
     * @param   path
     *          要测试的文件的路径
     *
     * @return  如果文件被认为是隐藏的则返回 {@code true}
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，将调用 {@link SecurityManager#checkRead(String) checkRead} 方法检查文件的读取权限。
     */
    public static boolean isHidden(Path path) throws IOException {
        return provider(path).isHidden(path);
    }

    // 懒加载默认和已安装的文件类型检测器
    private static class FileTypeDetectors{
        static final FileTypeDetector defaultFileTypeDetector =
            createDefaultFileTypeDetector();
        static final List<FileTypeDetector> installeDetectors =
            loadInstalledDetectors();


                    // 创建默认的文件类型检测器
        private static FileTypeDetector createDefaultFileTypeDetector() {
            return AccessController
                .doPrivileged(new PrivilegedAction<FileTypeDetector>() {
                    @Override public FileTypeDetector run() {
                        return sun.nio.fs.DefaultFileTypeDetector.create();
                }});
        }

        // 加载所有已安装的文件类型检测器
        private static List<FileTypeDetector> loadInstalledDetectors() {
            return AccessController
                .doPrivileged(new PrivilegedAction<List<FileTypeDetector>>() {
                    @Override public List<FileTypeDetector> run() {
                        List<FileTypeDetector> list = new ArrayList<>();
                        ServiceLoader<FileTypeDetector> loader = ServiceLoader
                            .load(FileTypeDetector.class, ClassLoader.getSystemClassLoader());
                        for (FileTypeDetector detector: loader) {
                            list.add(detector);
                        }
                        return list;
                }});
        }
    }

    /**
     * 探测文件的内容类型。
     *
     * <p> 该方法使用已安装的 {@link FileTypeDetector} 实现来探测给定文件以确定其内容类型。每个文件类型检测器的
     * {@link FileTypeDetector#probeContentType probeContentType} 方法依次被调用以探测文件类型。如果文件被识别，
     * 则返回内容类型。如果文件未被任何已安装的文件类型检测器识别，则调用系统默认的文件类型检测器来猜测内容类型。
     *
     * <p> 给定的 Java 虚拟机调用维护一个系统范围的文件类型检测器列表。已安装的文件类型检测器使用 {@link ServiceLoader}
     * 类定义的服务提供者加载设施加载。已安装的文件类型检测器使用系统类加载器加载。如果找不到系统类加载器，则使用扩展类加载器；
     * 如果找不到扩展类加载器，则使用引导类加载器。文件类型检测器通常通过将它们放在应用程序类路径或扩展目录中的 JAR 文件中安装，
     * JAR 文件包含资源目录 {@code META-INF/services} 中名为 {@code java.nio.file.spi.FileTypeDetector} 的提供者配置文件，
     * 该文件列出了一个或多个具有零参数构造函数的 {@code FileTypeDetector} 的具体子类的完全限定名称。如果定位或实例化已安装的文件类型检测器的过程失败，
     * 则抛出未指定的错误。已安装提供者的定位顺序是实现特定的。
     *
     * <p> 该方法的返回值是多用途互联网邮件扩展（MIME）内容类型的值的字符串形式，如
     * <a href="http://www.ietf.org/rfc/rfc2045.txt"><i>RFC&nbsp;2045: 多用途互联网邮件扩展（MIME）第一部分：互联网消息体格式</i></a> 中定义。
     * 该字符串保证可以按照 RFC 中的语法解析。
     *
     * @param   path
     *          要探测的文件的路径
     *
     * @return  文件的内容类型，如果无法确定内容类型，则返回 {@code null}
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          如果安装了安全管理器并且它拒绝了文件类型检测器实现所需的未指定权限。
     */
    public static String probeContentType(Path path)
        throws IOException
    {
        // 尝试已安装的文件类型检测器
        for (FileTypeDetector detector: FileTypeDetectors.installeDetectors) {
            String result = detector.probeContentType(path);
            if (result != null)
                return result;
        }

        // 回退到默认值
        return FileTypeDetectors.defaultFileTypeDetector.probeContentType(path);
    }

    // -- 文件属性 --

    /**
     * 返回给定类型的文件属性视图。
     *
     * <p> 文件属性视图提供了一组文件属性的只读或可更新视图。此方法旨在用于文件属性视图定义类型安全的方法来读取或更新文件属性的情况。
     * {@code type} 参数是所需的属性视图类型，如果支持该类型，则该方法返回该类型的实例。{@link BasicFileAttributeView} 类型支持访问文件的基本属性。
     * 调用此方法选择该类型的文件属性视图将始终返回该类的实例。
     *
     * <p> {@code options} 数组可用于指示如果文件是符号链接，则结果文件属性视图如何处理符号链接。默认情况下，符号链接会被跟随。
     * 如果存在选项 {@link LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS}，则不会跟随符号链接。不支持符号链接的实现将忽略此选项。
     *
     * <p> <b>使用示例：</b>
     * 假设我们要读取或设置文件的 ACL（如果支持）：
     * <pre>
     *     Path path = ...
     *     AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);
     *     if (view != null) {
     *         List&lt;AclEntry&gt; acl = view.getAcl();
     *         :
     *     }
     * </pre>
     *
     * @param   <V>
     *          {@code FileAttributeView} 类型
     * @param   path
     *          文件的路径
     * @param   type
     *          对应于文件属性视图的 {@code Class} 对象
     * @param   options
     *          指示如何处理符号链接的选项
     *
     * @return  指定类型的文件属性视图，如果属性视图类型不可用，则返回 {@code null}
     */
    public static <V extends FileAttributeView> V getFileAttributeView(Path path,
                                                                       Class<V> type,
                                                                       LinkOption... options)
    {
        return provider(path).getFileAttributeView(path, type, options);
    }

                /**
     * 以批量操作方式读取文件的属性。
     *
     * <p> 参数 {@code type} 是所需的属性类型，如果支持该类型，此方法将返回该类型的实例。所有实现都支持一组基本的文件属性，因此使用 {@code
     * BasicFileAttributes.class} 作为 {@code type} 参数调用此方法不会抛出 {@code
     * UnsupportedOperationException}。
     *
     * <p> 如果文件是一个符号链接，可以使用 {@code options} 数组来指示如何处理符号链接。默认情况下，符号链接会被跟随，并读取链接最终目标的文件属性。如果存在选项 {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS}，则不会跟随符号链接。
     *
     * <p> 是否所有文件属性都作为原子操作读取，具体取决于实现，相对于其他文件系统操作。
     *
     * <p> <b>使用示例：</b>
     * 假设我们想要批量读取文件的属性：
     * <pre>
     *    Path path = ...
     *    BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
     * </pre>
     * 或者，假设我们想要在不跟随符号链接的情况下读取文件的POSIX属性：
     * <pre>
     *    PosixFileAttributes attrs = Files.readAttributes(path, PosixFileAttributes.class, NOFOLLOW_LINKS);
     * </pre>
     *
     * @param   <A>
     *          {@code BasicFileAttributes} 类型
     * @param   path
     *          文件的路径
     * @param   type
     *          需要读取的文件属性的 {@code Class}
     * @param   options
     *          指示如何处理符号链接的选项
     *
     * @return  文件属性
     *
     * @throws  UnsupportedOperationException
     *          如果不支持给定类型的属性
     * @throws  IOException
     *          如果发生I/O错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，其 {@link SecurityManager#checkRead(String) checkRead}
     *          方法将被调用来检查文件的读取权限。如果此方法用于读取安全敏感属性，安全经理可能会被调用来检查额外的权限。
     */
    public static <A extends BasicFileAttributes> A readAttributes(Path path,
                                                                   Class<A> type,
                                                                   LinkOption... options)
        throws IOException
    {
        return provider(path).readAttributes(path, type, options);
    }

    /**
     * 设置文件属性的值。
     *
     * <p> 参数 {@code attribute} 用于标识要设置的属性，其形式为：
     * <blockquote>
     * [<i>view-name</i><b>:</b>]<i>attribute-name</i>
     * </blockquote>
     * 其中方括号 [...] 表示可选组件，字符 {@code ':'} 表示自身。
     *
     * <p> <i>view-name</i> 是 {@link FileAttributeView} 的 {@link FileAttributeView#name 名称}，用于标识一组文件属性。如果未指定，则默认为 {@code "basic"}，这是标识许多文件系统中常见的基本文件属性集的文件属性视图的名称。<i>attribute-name</i> 是该集合中的属性名称。
     *
     * <p> 如果文件是一个符号链接，可以使用 {@code options} 数组来指示如何处理符号链接。默认情况下，符号链接会被跟随，并设置链接最终目标的文件属性。如果存在选项 {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS}，则不会跟随符号链接。
     *
     * <p> <b>使用示例：</b>
     * 假设我们想要设置DOS "hidden" 属性：
     * <pre>
     *    Path path = ...
     *    Files.setAttribute(path, "dos:hidden", true);
     * </pre>
     *
     * @param   path
     *          文件的路径
     * @param   attribute
     *          要设置的属性
     * @param   value
     *          属性值
     * @param   options
     *          指示如何处理符号链接的选项
     *
     * @return  {@code path} 参数
     *
     * @throws  UnsupportedOperationException
     *          如果属性视图不可用
     * @throws  IllegalArgumentException
     *          如果未指定属性名称，或者未被识别，或者属性值类型正确但值不合适
     * @throws  ClassCastException
     *          如果属性值不是预期类型，或者是一个包含非预期类型元素的集合
     * @throws  IOException
     *          如果发生I/O错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，其 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法拒绝文件的写入权限。如果此方法用于设置安全敏感属性，安全经理可能会被调用来检查额外的权限。
     */
    public static Path setAttribute(Path path, String attribute, Object value,
                                    LinkOption... options)
        throws IOException
    {
        provider(path).setAttribute(path, attribute, value, options);
        return path;
    }

    /**
     * 读取文件属性的值。
     *
     * <p> 参数 {@code attribute} 用于标识要读取的属性，其形式为：
     * <blockquote>
     * [<i>view-name</i><b>:</b>]<i>attribute-name</i>
     * </blockquote>
     * 其中方括号 [...] 表示可选组件，字符 {@code ':'} 表示自身。
     *
     * <p> <i>view-name</i> 是 {@link FileAttributeView} 的 {@link FileAttributeView#name 名称}，用于标识一组文件属性。如果未指定，则默认为 {@code "basic"}，这是标识许多文件系统中常见的基本文件属性集的文件属性视图的名称。<i>attribute-name</i> 是属性的名称。
     *
     * <p> 如果文件是一个符号链接，可以使用 {@code options} 数组来指示如何处理符号链接。默认情况下，符号链接会被跟随，并读取链接最终目标的文件属性。如果存在选项 {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS}，则不会跟随符号链接。
     *
     * <p> <b>使用示例：</b>
     * 假设我们需要在支持 "{@code unix}" 视图的系统上获取文件所有者的用户ID：
     * <pre>
     *    Path path = ...
     *    int uid = (Integer)Files.getAttribute(path, "unix:uid");
     * </pre>
     *
     * @param   path
     *          文件的路径
     * @param   attribute
     *          要读取的属性
     * @param   options
     *          指示如何处理符号链接的选项
     *
     * @return  属性值
     *
     * @throws  UnsupportedOperationException
     *          如果属性视图不可用
     * @throws  IllegalArgumentException
     *          如果未指定属性名称或未被识别
     * @throws  IOException
     *          如果发生I/O错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，其 {@link SecurityManager#checkRead(String) checkRead}
     *          方法拒绝文件的读取权限。如果此方法用于读取安全敏感属性，安全经理可能会被调用来检查额外的权限。
     */
    public static Object getAttribute(Path path, String attribute,
                                      LinkOption... options)
        throws IOException
    {
        // 只应读取一个属性
        if (attribute.indexOf('*') >= 0 || attribute.indexOf(',') >= 0)
            throw new IllegalArgumentException(attribute);
        Map<String,Object> map = readAttributes(path, attribute, options);
        assert map.size() == 1;
        String name;
        int pos = attribute.indexOf(':');
        if (pos == -1) {
            name = attribute;
        } else {
            name = (pos == attribute.length()) ? "" : attribute.substring(pos+1);
        }
        return map.get(name);
    }


                /**
     * 以批量操作的形式读取一组文件属性。
     *
     * <p> 参数 {@code attributes} 用于标识要读取的属性，其形式为：
     * <blockquote>
     * [<i>view-name</i><b>:</b>]<i>attribute-list</i>
     * </blockquote>
     * 其中方括号 [...] 表示可选部分，字符 {@code ':'} 表示自身。
     *
     * <p> <i>view-name</i> 是 {@link FileAttributeView} 的 {@link FileAttributeView#name 名称}，用于标识一组文件属性。如果未指定，则默认为 {@code "basic"}，这是标识许多文件系统中常见的基本文件属性集的文件属性视图的名称。
     *
     * <p> <i>attribute-list</i> 组件是一个以逗号分隔的零个或多个属性名称的列表。如果列表包含值 {@code "*"}，则读取所有属性。不支持的属性将被忽略，并且不会出现在返回的映射中。是否所有属性都作为原子操作读取，取决于实现，相对于其他文件系统操作。
     *
     * <p> 以下示例展示了 {@code attributes} 参数的可能值：
     *
     * <blockquote>
     * <table border="0" summary="Possible values">
     * <tr>
     *   <td> {@code "*"} </td>
     *   <td> 读取所有 {@link BasicFileAttributes 基本文件属性}。 </td>
     * </tr>
     * <tr>
     *   <td> {@code "size,lastModifiedTime,lastAccessTime"} </td>
     *   <td> 读取文件大小、最后修改时间和最后访问时间属性。 </td>
     * </tr>
     * <tr>
     *   <td> {@code "posix:*"} </td>
     *   <td> 读取所有 {@link PosixFileAttributes POSIX 文件属性}。 </td>
     * </tr>
     * <tr>
     *   <td> {@code "posix:permissions,owner,size"} </td>
     *   <td> 读取 POSIX 文件权限、所有者和文件大小。 </td>
     * </tr>
     * </table>
     * </blockquote>
     *
     * <p> {@code options} 数组可用于指示如何处理符号链接的情况，即文件是符号链接。默认情况下，符号链接会被跟随，并读取链接最终目标的文件属性。如果存在选项 {@link LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS}，则不会跟随符号链接。
     *
     * @param   path
     *          文件的路径
     * @param   attributes
     *          要读取的属性
     * @param   options
     *          指示如何处理符号链接的选项
     *
     * @return  返回的属性映射；映射的键是属性名称，值是属性值
     *
     * @throws  UnsupportedOperationException
     *          如果属性视图不可用
     * @throws  IllegalArgumentException
     *          如果未指定任何属性或指定了未识别的属性
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，其 {@link SecurityManager#checkRead(String) checkRead}
     *          方法拒绝文件的读取访问。如果此方法用于读取安全敏感属性，安全经理可能会检查额外的权限。
     */
    public static Map<String,Object> readAttributes(Path path, String attributes,
                                                    LinkOption... options)
        throws IOException
    {
        return provider(path).readAttributes(path, attributes, options);
    }

    /**
     * 返回文件的 POSIX 文件权限。
     *
     * <p> 参数 {@code path} 与支持 {@link PosixFileAttributeView} 的 {@code FileSystem} 关联。此属性视图提供了访问通常与实现 Portable Operating
     * System Interface (POSIX) 家族标准的操作系统文件系统中文件相关联的文件属性。
     *
     * <p> {@code options} 数组可用于指示如何处理符号链接的情况，即文件是符号链接。默认情况下，符号链接会被跟随，并读取链接最终目标的文件属性。如果存在选项 {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS}，则不会跟随符号链接。
     *
     * @param   path
     *          文件的路径
     * @param   options
     *          指示如何处理符号链接的选项
     *
     * @return  文件权限
     *
     * @throws  UnsupportedOperationException
     *          如果关联的文件系统不支持 {@code PosixFileAttributeView}
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，它拒绝 {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          或其 {@link SecurityManager#checkRead(String) checkRead} 方法拒绝文件的读取访问。
     */
    public static Set<PosixFilePermission> getPosixFilePermissions(Path path,
                                                                   LinkOption... options)
        throws IOException
    {
        return readAttributes(path, PosixFileAttributes.class, options).permissions();
    }

    /**
     * 设置文件的 POSIX 权限。
     *
     * <p> 参数 {@code path} 与支持 {@link PosixFileAttributeView} 的 {@code FileSystem} 关联。此属性视图提供了访问通常与实现 Portable Operating
     * System Interface (POSIX) 家族标准的操作系统文件系统中文件相关联的文件属性。
     *
     * @param   path
     *          文件的路径
     * @param   perms
     *          新的权限集
     *
     * @return  文件路径
     *
     * @throws  UnsupportedOperationException
     *          如果关联的文件系统不支持 {@code PosixFileAttributeView}
     * @throws  ClassCastException
     *          如果集合包含不是 {@code PosixFilePermission} 类型的元素
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，它拒绝 {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          或其 {@link SecurityManager#checkWrite(String) checkWrite} 方法拒绝文件的写入访问。
     */
    public static Path setPosixFilePermissions(Path path,
                                               Set<PosixFilePermission> perms)
        throws IOException
    {
        PosixFileAttributeView view =
            getFileAttributeView(path, PosixFileAttributeView.class);
        if (view == null)
            throw new UnsupportedOperationException();
        view.setPermissions(perms);
        return path;
    }

                /**
     * 返回文件的所有者。
     *
     * <p> 参数 {@code path} 与支持 {@link FileOwnerAttributeView} 的文件系统相关联。此文件属性视图提供了
     * 访问文件所有者属性的接口。
     *
     * @param   path
     *          文件的路径
     * @param   options
     *          指示如何处理符号链接的选项
     *
     * @return  代表文件所有者的用户主体
     *
     * @throws  UnsupportedOperationException
     *          如果关联的文件系统不支持 {@code FileOwnerAttributeView}
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，它拒绝 {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          或其 {@link SecurityManager#checkRead(String) checkRead} 方法拒绝读取文件的访问。
     */
    public static UserPrincipal getOwner(Path path, LinkOption... options) throws IOException {
        FileOwnerAttributeView view =
            getFileAttributeView(path, FileOwnerAttributeView.class, options);
        if (view == null)
            throw new UnsupportedOperationException();
        return view.getOwner();
    }

    /**
     * 更新文件所有者。
     *
     * <p> 参数 {@code path} 与支持 {@link FileOwnerAttributeView} 的文件系统相关联。此文件属性视图提供了
     * 访问文件所有者属性的接口。
     *
     * <p> <b>使用示例：</b>
     * 假设我们想让 "joe" 成为文件的所有者：
     * <pre>
     *     Path path = ...
     *     UserPrincipalLookupService lookupService =
     *         provider(path).getUserPrincipalLookupService();
     *     UserPrincipal joe = lookupService.lookupPrincipalByName("joe");
     *     Files.setOwner(path, joe);
     * </pre>
     *
     * @param   path
     *          文件的路径
     * @param   owner
     *          新的文件所有者
     *
     * @return  路径
     *
     * @throws  UnsupportedOperationException
     *          如果关联的文件系统不支持 {@code FileOwnerAttributeView}
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，它拒绝 {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          或其 {@link SecurityManager#checkWrite(String) checkWrite} 方法拒绝写入文件的访问。
     *
     * @see FileSystem#getUserPrincipalLookupService
     * @see java.nio.file.attribute.UserPrincipalLookupService
     */
    public static Path setOwner(Path path, UserPrincipal owner)
        throws IOException
    {
        FileOwnerAttributeView view =
            getFileAttributeView(path, FileOwnerAttributeView.class);
        if (view == null)
            throw new UnsupportedOperationException();
        view.setOwner(owner);
        return path;
    }

    /**
     * 测试文件是否为符号链接。
     *
     * <p> 如果需要区分 I/O 异常和文件不是符号链接的情况，则可以使用 {@link #readAttributes(Path,Class,LinkOption[])
     * readAttributes} 方法读取文件属性，并使用 {@link
     * BasicFileAttributes#isSymbolicLink} 方法测试文件类型。
     *
     * @param   path  文件的路径
     *
     * @return  如果文件是符号链接，则返回 {@code true}；如果文件不存在、不是符号链接或无法确定文件是否为符号链接，则返回 {@code false}。
     *
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，其 {@link SecurityManager#checkRead(String) checkRead}
     *          方法拒绝读取文件的访问。
     */
    public static boolean isSymbolicLink(Path path) {
        try {
            return readAttributes(path,
                                  BasicFileAttributes.class,
                                  LinkOption.NOFOLLOW_LINKS).isSymbolicLink();
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * 测试文件是否为目录。
     *
     * <p> 可以使用 {@code options} 数组来指示如何处理文件为符号链接的情况。默认情况下，符号链接会被跟随，并读取链接最终目标的文件属性。
     * 如果存在选项 {@link LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS}，则不会跟随符号链接。
     *
     * <p> 如果需要区分 I/O 异常和文件不是目录的情况，则可以使用 {@link #readAttributes(Path,Class,LinkOption[])
     * readAttributes} 方法读取文件属性，并使用 {@link
     * BasicFileAttributes#isDirectory} 方法测试文件类型。
     *
     * @param   path
     *          要测试的文件的路径
     * @param   options
     *          指示如何处理符号链接的选项
     *
     * @return  如果文件是目录，则返回 {@code true}；如果文件不存在、不是目录或无法确定文件是否为目录，则返回 {@code false}。
     *
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，其 {@link SecurityManager#checkRead(String) checkRead}
     *          方法拒绝读取文件的访问。
     */
    public static boolean isDirectory(Path path, LinkOption... options) {
        try {
            return readAttributes(path, BasicFileAttributes.class, options).isDirectory();
        } catch (IOException ioe) {
            return false;
        }
    }

                /**
     * 测试文件是否为具有不透明内容的普通文件。
     *
     * <p> {@code options} 数组可用于指示如何处理符号链接的情况，即文件是符号链接。默认情况下，
     * 符号链接会被跟随，链接的最终目标的文件属性会被读取。如果存在选项 {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS}，则不会跟随符号链接。
     *
     * <p> 如果需要区分 I/O 异常和文件不是普通文件的情况，可以使用 {@link #readAttributes(Path,Class,LinkOption[])
     * readAttributes} 方法读取文件属性，并使用 {@link
     * BasicFileAttributes#isRegularFile} 方法测试文件类型。
     *
     * @param   path
     *          文件的路径
     * @param   options
     *          指示如何处理符号链接的选项
     *
     * @return  如果文件是普通文件，则返回 {@code true}；如果文件不存在、不是普通文件，或者无法确定文件是否为普通文件，则返回 {@code false}。
     *
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，其 {@link SecurityManager#checkRead(String) checkRead}
     *          方法拒绝读取文件的权限。
     */
    public static boolean isRegularFile(Path path, LinkOption... options) {
        try {
            return readAttributes(path, BasicFileAttributes.class, options).isRegularFile();
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * 返回文件的最后修改时间。
     *
     * <p> {@code options} 数组可用于指示如何处理符号链接的情况，即文件是符号链接。默认情况下，
     * 符号链接会被跟随，链接的最终目标的文件属性会被读取。如果存在选项 {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS}，则不会跟随符号链接。
     *
     * @param   path
     *          文件的路径
     * @param   options
     *          指示如何处理符号链接的选项
     *
     * @return  一个 {@code FileTime}，表示文件最后修改的时间，或者当文件系统不支持表示最后修改时间的时间戳时，返回特定实现的默认值
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，其 {@link SecurityManager#checkRead(String) checkRead}
     *          方法拒绝读取文件的权限。
     *
     * @see BasicFileAttributes#lastModifiedTime
     */
    public static FileTime getLastModifiedTime(Path path, LinkOption... options)
        throws IOException
    {
        return readAttributes(path, BasicFileAttributes.class, options).lastModifiedTime();
    }

    /**
     * 更新文件的最后修改时间属性。文件时间将转换为文件系统支持的纪元和精度。从更细粒度转换为更粗粒度会导致精度损失。当尝试设置的最后修改时间不被文件系统支持或超出底层文件存储支持的范围时，此方法的行为未定义。它可能会或不会通过抛出一个 {@code IOException} 来失败。
     *
     * <p> <b>使用示例：</b>
     * 假设我们想将最后修改时间设置为当前时间：
     * <pre>
     *    Path path = ...
     *    FileTime now = FileTime.fromMillis(System.currentTimeMillis());
     *    Files.setLastModifiedTime(path, now);
     * </pre>
     *
     * @param   path
     *          文件的路径
     * @param   time
     *          新的最后修改时间
     *
     * @return  文件的路径
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，安全管理者的 {@link
     *          SecurityManager#checkWrite(String) checkWrite} 方法被调用以检查文件的写权限
     *
     * @see BasicFileAttributeView#setTimes
     */
    public static Path setLastModifiedTime(Path path, FileTime time)
        throws IOException
    {
        getFileAttributeView(path, BasicFileAttributeView.class)
            .setTimes(time, null, null);
        return path;
    }

    /**
     * 返回文件的大小（以字节为单位）。由于压缩、支持稀疏文件或其他原因，实际大小可能与文件系统上的大小不同。对于不是 {@link
     * #isRegularFile 普通} 文件的文件的大小是实现特定的，因此未指定。
     *
     * @param   path
     *          文件的路径
     *
     * @return  文件的大小，以字节为单位
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，其 {@link SecurityManager#checkRead(String) checkRead}
     *          方法拒绝读取文件的权限。
     *
     * @see BasicFileAttributes#size
     */
    public static long size(Path path) throws IOException {
        return readAttributes(path, BasicFileAttributes.class).size();
    }

    // -- 访问性 --

    /**
     * 如果存在 NOFOLLOW_LINKS，则返回 {@code false}。
     */
    private static boolean followLinks(LinkOption... options) {
        boolean followLinks = true;
        for (LinkOption opt: options) {
            if (opt == LinkOption.NOFOLLOW_LINKS) {
                followLinks = false;
                continue;
            }
            if (opt == null)
                throw new NullPointerException();
            throw new AssertionError("不应该到达这里");
        }
        return followLinks;
    }


                /**
     * 测试文件是否存在。
     *
     * <p> 参数 {@code options} 可用于指示如何处理符号链接的情况，即文件为符号链接时。默认情况下，
     * 符号链接会被跟随。如果存在选项 {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS}，则不会跟随符号链接。
     *
     * <p> 注意，此方法的结果会立即过时。如果此方法指示文件存在，则无法保证后续的访问会成功。在安全敏感的应用程序中使用此方法时应谨慎。
     *
     * @param   path
     *          要测试的文件的路径
     * @param   options
     *          指示如何处理符号链接的选项
     * .
     * @return  如果文件存在，则返回 {@code true}；如果文件不存在或无法确定其存在，则返回 {@code false}。
     *
     * @throws  SecurityException
     *          对于默认提供者，将调用 {@link
     *          SecurityManager#checkRead(String)} 以检查对文件的读取权限。
     *
     * @see #notExists
     */
    public static boolean exists(Path path, LinkOption... options) {
        try {
            if (followLinks(options)) {
                provider(path).checkAccess(path);
            } else {
                // 尝试不跟随链接读取属性
                readAttributes(path, BasicFileAttributes.class,
                               LinkOption.NOFOLLOW_LINKS);
            }
            // 文件存在
            return true;
        } catch (IOException x) {
            // 不存在或无法确定文件是否存在
            return false;
        }

    }

    /**
     * 测试由该路径定位的文件是否不存在。此方法适用于需要在确认文件不存在时采取行动的情况。
     *
     * <p> 参数 {@code options} 可用于指示如何处理符号链接的情况，即文件为符号链接时。默认情况下，
     * 符号链接会被跟随。如果存在选项 {@link LinkOption#NOFOLLOW_LINKS
     * NOFOLLOW_LINKS}，则不会跟随符号链接。
     *
     * <p> 注意，此方法不是 {@link #exists
     * exists} 方法的补集。如果无法确定文件是否存在，则两个方法都返回 {@code false}。与 {@code exists}
     * 方法一样，此方法的结果会立即过时。如果此方法指示文件存在，则无法保证后续尝试创建文件会成功。在安全敏感的应用程序中使用此方法时应谨慎。
     *
     * @param   path
     *          要测试的文件的路径
     * @param   options
     *          指示如何处理符号链接的选项
     *
     * @return  如果文件不存在，则返回 {@code true}；如果文件存在或无法确定其存在，则返回 {@code false}。
     *
     * @throws  SecurityException
     *          对于默认提供者，将调用 {@link
     *          SecurityManager#checkRead(String)} 以检查对文件的读取权限。
     */
    public static boolean notExists(Path path, LinkOption... options) {
        try {
            if (followLinks(options)) {
                provider(path).checkAccess(path);
            } else {
                // 尝试不跟随链接读取属性
                readAttributes(path, BasicFileAttributes.class,
                               LinkOption.NOFOLLOW_LINKS);
            }
            // 文件存在
            return false;
        } catch (NoSuchFileException x) {
            // 确认文件不存在
            return true;
        } catch (IOException x) {
            return false;
        }
    }

    /**
     * 用于 isReadbale, isWritable, isExecutable 来测试文件的访问权限。
     */
    private static boolean isAccessible(Path path, AccessMode... modes) {
        try {
            provider(path).checkAccess(path, modes);
            return true;
        } catch (IOException x) {
            return false;
        }
    }

    /**
     * 测试文件是否可读。此方法检查文件是否存在以及此 Java 虚拟机是否有适当的权限允许其打开文件进行读取。根据实现的不同，
     * 此方法可能需要读取文件权限、访问控制列表或其他文件属性以检查对文件的有效访问。因此，此方法可能与其他文件系统操作不具有原子性。
     *
     * <p> 注意，此方法的结果会立即过时，无法保证后续尝试打开文件进行读取会成功（甚至无法保证访问的是同一个文件）。在安全敏感的应用程序中使用此方法时应谨慎。
     *
     * @param   path
     *          要检查的文件的路径
     *
     * @return  如果文件存在且可读，则返回 {@code true}；如果文件不存在、读取访问权限因 Java 虚拟机权限不足而被拒绝，或无法确定访问权限，则返回 {@code false}。
     *
     * @throws  SecurityException
     *          对于默认提供者，如果安装了安全管理器，则调用 {@link SecurityManager#checkRead(String) checkRead}
     *          以检查对文件的读取权限。
     */
    public static boolean isReadable(Path path) {
        return isAccessible(path, AccessMode.READ);
    }

    /**
     * 测试文件是否可写。此方法检查文件是否存在以及此 Java 虚拟机是否有适当的权限允许其打开文件进行写入。根据实现的不同，
     * 此方法可能需要读取文件权限、访问控制列表或其他文件属性以检查对文件的有效访问。因此，此方法可能与其他文件系统操作不具有原子性。
     *
     * <p> 注意，此方法的结果会立即过时，无法保证后续尝试打开文件进行写入会成功（甚至无法保证访问的是同一个文件）。在安全敏感的应用程序中使用此方法时应谨慎。
     *
     * @param   path
     *          要检查的文件的路径
     *
     * @return  如果文件存在且可写，则返回 {@code true}；如果文件不存在、写入访问权限因 Java 虚拟机权限不足而被拒绝，或无法确定访问权限，则返回 {@code false}。
     *
     * @throws  SecurityException
     *          对于默认提供者，如果安装了安全管理器，则调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          以检查对文件的写入权限。
     */
    public static boolean isWritable(Path path) {
        return isAccessible(path, AccessMode.WRITE);
    }

                /**
     * 测试文件是否可执行。此方法检查文件是否存在，并且此 Java 虚拟机是否有适当的权限来 {@link
     * Runtime#exec 执行} 该文件。当检查目录的访问权限时，语义可能会有所不同。例如，在 UNIX 系统上，检查执行访问权限会检查 Java 虚拟机是否有权限搜索目录以访问文件或子目录。
     *
     * <p> 根据实现，此方法可能需要读取文件权限、访问控制列表或其他文件属性，以检查对文件的有效访问。因此，此方法可能不会与其他文件系统操作保持原子性。
     *
     * <p> 请注意，此方法的结果立即过时，无法保证后续尝试执行文件会成功（甚至无法保证访问的是同一个文件）。在安全敏感的应用程序中使用此方法时应谨慎。
     *
     * @param   path
     *          要检查的文件的路径
     *
     * @return  {@code true} 如果文件存在且可执行；{@code false}
     *          如果文件不存在，执行访问将被拒绝，因为 Java 虚拟机权限不足，或无法确定访问权限
     *
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkExec(String)
     *          checkExec} 方法来检查文件的执行访问权限。
     */
    public static boolean isExecutable(Path path) {
        return isAccessible(path, AccessMode.EXECUTE);
    }

    // -- 递归操作 --

    /**
     * 遍历文件树。
     *
     * <p> 此方法从给定的起始文件开始遍历文件树。文件树遍历采用 <em>深度优先</em> 的方式，对于遇到的每个文件，给定的 {@link
     * FileVisitor} 都会被调用。当文件树中的所有可访问文件都被访问过，或者访问方法返回 {@link FileVisitResult#TERMINATE
     * TERMINATE} 结果时，文件树遍历完成。如果访问方法由于 {@code IOException}、未捕获的错误或运行时异常而终止，则遍历终止，错误或异常将传递给此方法的调用者。
     *
     * <p> 对于遇到的每个文件，此方法尝试读取其 {@link
     * java.nio.file.attribute.BasicFileAttributes}。如果文件不是目录，则调用带有文件属性的 {@link FileVisitor#visitFile visitFile} 方法。如果由于 I/O 异常无法读取文件属性，则调用带有 I/O 异常的 {@link FileVisitor#visitFileFailed
     * visitFileFailed} 方法。
     *
     * <p> 如果文件是目录，并且无法打开该目录，则调用带有 I/O 异常的 {@code visitFileFailed} 方法，之后，默认情况下，文件树遍历将继续到目录的下一个 <em>同级</em>。
     *
     * <p> 如果目录成功打开，则访问目录中的条目及其 <em>后代</em>。当所有条目都被访问过，或者在迭代目录时发生 I/O 错误，则关闭目录，并调用访问者的 {@link
     * FileVisitor#postVisitDirectory postVisitDirectory} 方法。然后，默认情况下，文件树遍历将继续到目录的下一个 <em>同级</em>。
     *
     * <p> 默认情况下，此方法不会自动跟随符号链接。如果 {@code options} 参数包含 {@link
     * FileVisitOption#FOLLOW_LINKS FOLLOW_LINKS} 选项，则会跟随符号链接。当跟随链接时，如果无法读取目标的属性，则此方法尝试获取链接的 {@code BasicFileAttributes}。如果可以读取，则调用带有链接属性的 {@code visitFile} 方法（否则，如上所述，调用 {@code visitFileFailed} 方法）。
     *
     * <p> 如果 {@code options} 参数包含 {@link
     * FileVisitOption#FOLLOW_LINKS FOLLOW_LINKS} 选项，则此方法会跟踪访问的目录，以便检测循环。当目录中的条目是该目录的祖先时，就会出现循环。循环检测是通过记录目录的 {@link
     * java.nio.file.attribute.BasicFileAttributes#fileKey file-key}，或者如果文件键不可用，则通过调用 {@link #isSameFile
     * isSameFile} 方法来测试目录是否与祖先相同。当检测到循环时，它被视为 I/O 错误，并调用带有 {@link FileSystemLoopException} 实例的 {@link FileVisitor#visitFileFailed visitFileFailed} 方法。
     *
     * <p> {@code maxDepth} 参数是访问的目录层级的最大数量。值为 {@code 0} 表示仅访问起始文件，除非被安全经理拒绝。值为 {@link Integer#MAX_VALUE MAX_VALUE} 可用于表示应访问所有层级。对于在 {@code maxDepth} 处遇到的所有文件，包括目录，都会调用 {@code visitFile} 方法，除非无法读取基本文件属性，在这种情况下，调用 {@code
     * visitFileFailed} 方法。
     *
     * <p> 如果访问者返回的结果为 {@code null}，则抛出 {@code
     * NullPointerException}。
     *
     * <p> 当安装了安全经理并且它拒绝访问文件（或目录）时，则忽略该文件（或目录），不会为该文件（或目录）调用访问者。
     *
     * @param   start
     *          起始文件
     * @param   options
     *          配置遍历的选项
     * @param   maxDepth
     *          要访问的目录层级的最大数量
     * @param   visitor
     *          要为每个文件调用的文件访问者
     *
     * @return  起始文件
     *
     * @throws  IllegalArgumentException
     *          如果 {@code maxDepth} 参数为负数
     * @throws  SecurityException
     *          如果安全经理拒绝访问起始文件。在默认提供者的情况下，调用 {@link
     *          SecurityManager#checkRead(String) checkRead} 方法来检查对目录的读取访问权限。
     * @throws  IOException
     *          如果访问者方法抛出 I/O 错误
     */
    public static Path walkFileTree(Path start,
                                    Set<FileVisitOption> options,
                                    int maxDepth,
                                    FileVisitor<? super Path> visitor)
        throws IOException
    {
        /**
         * 创建一个 FileTreeWalker 来遍历文件树，并为每个事件调用访问者。
         */
        try (FileTreeWalker walker = new FileTreeWalker(options, maxDepth)) {
            FileTreeWalker.Event ev = walker.walk(start);
            do {
                FileVisitResult result;
                switch (ev.type()) {
                    case ENTRY :
                        IOException ioe = ev.ioeException();
                        if (ioe == null) {
                            assert ev.attributes() != null;
                            result = visitor.visitFile(ev.file(), ev.attributes());
                        } else {
                            result = visitor.visitFileFailed(ev.file(), ioe);
                        }
                        break;


                                case START_DIRECTORY :
                        result = visitor.preVisitDirectory(ev.file(), ev.attributes());

                        // 如果返回了 SKIP_SIBLINGS 和 SKIP_SUBTREE，则
                        // 当前目录不应该再有更多事件。
                        if (result == FileVisitResult.SKIP_SUBTREE ||
                            result == FileVisitResult.SKIP_SIBLINGS)
                            walker.pop();
                        break;

                    case END_DIRECTORY :
                        result = visitor.postVisitDirectory(ev.file(), ev.ioeException());

                        // 对于 postVisitDirectory，SKIP_SIBLINGS 是无操作的
                        if (result == FileVisitResult.SKIP_SIBLINGS)
                            result = FileVisitResult.CONTINUE;
                        break;

                    default :
                        throw new AssertionError("不应该到达这里");
                }

                if (Objects.requireNonNull(result) != FileVisitResult.CONTINUE) {
                    if (result == FileVisitResult.TERMINATE) {
                        break;
                    } else if (result == FileVisitResult.SKIP_SIBLINGS) {
                        walker.skipRemainingSiblings();
                    }
                }
                ev = walker.next();
            } while (ev != null);
        }

        return start;
    }

    /**
     * 遍历文件树。
     *
     * <p> 调用此方法等同于评估以下表达式：
     * <blockquote><pre>
     * walkFileTree(start, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, visitor)
     * </pre></blockquote>
     * 换句话说，它不会跟随符号链接，并遍历文件树的所有层级。
     *
     * @param   start
     *          起始文件
     * @param   visitor
     *          要调用的文件访问者
     *
     * @return  起始文件
     *
     * @throws  SecurityException
     *          如果安全管理器拒绝访问起始文件。对于默认提供者，将调用 {@link
     *          SecurityManager#checkRead(String) checkRead} 方法来检查对目录的读取权限。
     * @throws  IOException
     *          如果访问者方法抛出 I/O 错误
     */
    public static Path walkFileTree(Path start, FileVisitor<? super Path> visitor)
        throws IOException
    {
        return walkFileTree(start,
                            EnumSet.noneOf(FileVisitOption.class),
                            Integer.MAX_VALUE,
                            visitor);
    }


    // -- 用于简单用法的实用方法 --

    // 用于读取和写入的缓冲区大小
    private static final int BUFFER_SIZE = 8192;

    /**
     * 打开文件以进行读取，返回一个 {@code BufferedReader}，可用于高效地从文件中读取文本。文件中的字节使用指定的字符集解码为字符。读取从文件的开头开始。
     *
     * <p> 从文件读取的 {@code Reader} 方法如果读取到格式错误或无法映射的字节序列，则会抛出 {@code
     * IOException}。
     *
     * @param   path
     *          文件的路径
     * @param   cs
     *          用于解码的字符集
     *
     * @return  一个新的缓冲读取器，具有默认缓冲区大小，用于从文件中读取文本
     *
     * @throws  IOException
     *          如果打开文件时发生 I/O 错误
     * @throws  SecurityException
     *          对于默认提供者，如果安装了安全管理器，将调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法来检查对文件的读取权限。
     *
     * @see #readAllLines
     */
    public static BufferedReader newBufferedReader(Path path, Charset cs)
        throws IOException
    {
        CharsetDecoder decoder = cs.newDecoder();
        Reader reader = new InputStreamReader(newInputStream(path), decoder);
        return new BufferedReader(reader);
    }

    /**
     * 打开文件以进行读取，返回一个 {@code BufferedReader}，可用于高效地从文件中读取文本。文件中的字节使用 {@link StandardCharsets#UTF_8 UTF-8} {@link Charset
     * 字符集} 解码为字符。
     *
     * <p> 调用此方法等同于评估以下表达式：
     * <pre>{@code
     * Files.newBufferedReader(path, StandardCharsets.UTF_8)
     * }</pre>
     *
     * @param   path
     *          文件的路径
     *
     * @return  一个新的缓冲读取器，具有默认缓冲区大小，用于从文件中读取文本
     *
     * @throws  IOException
     *          如果打开文件时发生 I/O 错误
     * @throws  SecurityException
     *          对于默认提供者，如果安装了安全管理器，将调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法来检查对文件的读取权限。
     *
     * @since 1.8
     */
    public static BufferedReader newBufferedReader(Path path) throws IOException {
        return newBufferedReader(path, StandardCharsets.UTF_8);
    }

    /**
     * 打开或创建一个文件以进行写入，返回一个 {@code BufferedWriter}，可用于高效地将文本写入文件。
     * {@code options} 参数指定文件的创建或打开方式。如果没有指定选项，则此方法等同于指定了 {@link
     * StandardOpenOption#CREATE CREATE}、{@link
     * StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING} 和 {@link
     * StandardOpenOption#WRITE WRITE} 选项。换句话说，它打开文件以进行写入，如果文件不存在则创建文件，如果文件存在则将其截断为大小为 {@code 0}。
     *
     * <p> 用于写入文本的 {@code Writer} 方法如果无法使用指定的字符集编码文本，则会抛出 {@code IOException}。
     *
     * @param   path
     *          文件的路径
     * @param   cs
     *          用于编码的字符集
     * @param   options
     *          指定文件打开方式的选项
     *
     * @return  一个新的缓冲写入器，具有默认缓冲区大小，用于将文本写入文件
     *
     * @throws  IOException
     *          如果打开或创建文件时发生 I/O 错误
     * @throws  UnsupportedOperationException
     *          如果指定了不支持的选项
     * @throws  SecurityException
     *          对于默认提供者，如果安装了安全管理器，将调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法来检查对文件的写入权限。
     *
     * @see #write(Path,Iterable,Charset,OpenOption[])
     */
    public static BufferedWriter newBufferedWriter(Path path, Charset cs,
                                                   OpenOption... options)
        throws IOException
    {
        CharsetEncoder encoder = cs.newEncoder();
        Writer writer = new OutputStreamWriter(newOutputStream(path, options), encoder);
        return new BufferedWriter(writer);
    }

                /**
     * 打开或创建一个文件用于写入，返回一个 {@code BufferedWriter}
     * 以高效的方式将文本写入文件。文本使用 {@link StandardCharsets#UTF_8 UTF-8}
     * {@link Charset 字符集} 编码为字节进行写入。
     *
     * <p> 该方法的工作方式类似于调用以下表达式：
     * <pre>{@code
     * Files.newBufferedWriter(path, StandardCharsets.UTF_8, options)
     * }</pre>
     *
     * @param   path
     *          文件的路径
     * @param   options
     *          指定文件如何打开的选项
     *
     * @return  一个新的缓冲写入器，具有默认缓冲区大小，用于将文本写入文件
     *
     * @throws  IOException
     *          打开或创建文件时发生 I/O 错误
     * @throws  UnsupportedOperationException
     *          指定了不支持的选项
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，将调用
     *          {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法检查对文件的写权限。
     *
     * @since 1.8
     */
    public static BufferedWriter newBufferedWriter(Path path, OpenOption... options) throws IOException {
        return newBufferedWriter(path, StandardCharsets.UTF_8, options);
    }

    /**
     * 从输入流读取所有字节并写入输出流。
     */
    private static long copy(InputStream source, OutputStream sink)
        throws IOException
    {
        long nread = 0L;
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }

    /**
     * 将输入流中的所有字节复制到文件中。返回时，输入
     * 流将处于流的末尾。
     *
     * <p> 默认情况下，如果目标文件已存在或是一个符号链接，则复制失败。如果指定了
     * {@link StandardCopyOption#REPLACE_EXISTING REPLACE_EXISTING} 选项，并且目标文件已存在，
     * 则如果它不是一个非空目录，将被替换。如果目标文件存在且是一个符号链接，则符号链接将被替换。
     * 在此版本中，{@code REPLACE_EXISTING} 选项是此方法唯一要求支持的选项。未来版本可能会支持更多选项。
     *
     * <p> 从输入流读取或写入文件时，如果发生 I/O 错误，可能会在目标文件已创建且已读取或写入一些字节后发生。
     * 因此，输入流可能不会处于流的末尾，且可能处于不一致状态。强烈建议在发生 I/O 错误时立即关闭输入流。
     *
     * <p> 该方法可能会无限期地从输入流读取（或写入文件）。如果输入流
     * <i>异步关闭</i> 或在复制过程中线程被中断，其行为高度依赖于输入流和文件系统提供者，因此未指定。
     *
     * <p> <b>使用示例</b>：假设我们想要捕获一个网页并将其保存到文件中：
     * <pre>
     *     Path path = ...
     *     URI u = URI.create("http://java.sun.com/");
     *     try (InputStream in = u.toURL().openStream()) {
     *         Files.copy(in, path);
     *     }
     * </pre>
     *
     * @param   in
     *          要读取的输入流
     * @param   target
     *          文件的路径
     * @param   options
     *          指定如何进行复制的选项
     *
     * @return  读取或写入的字节数
     *
     * @throws  IOException
     *          读取或写入时发生 I/O 错误
     * @throws  FileAlreadyExistsException
     *          目标文件已存在但无法被替换，因为未指定
     *          {@code REPLACE_EXISTING} 选项 <i>(可选特定异常)</i>
     * @throws  DirectoryNotEmptyException
     *          指定了 {@code REPLACE_EXISTING} 选项，但文件无法被替换，因为它是一个非空目录
     *          <i>(可选特定异常)</i>     *
     * @throws  UnsupportedOperationException
     *          如果 {@code options} 包含不支持的复制选项
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，将调用
     *          {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法检查对文件的写权限。如果指定了 {@code REPLACE_EXISTING} 选项，安全经理的
     *          {@link SecurityManager#checkDelete(String) checkDelete}
     *          方法将被调用以检查是否可以删除现有文件。
     */
    public static long copy(InputStream in, Path target, CopyOption... options)
        throws IOException
    {
        // 确保在打开文件之前不为空
        Objects.requireNonNull(in);

        // 检查是否指定了 REPLACE_EXISTING
        boolean replaceExisting = false;
        for (CopyOption opt: options) {
            if (opt == StandardCopyOption.REPLACE_EXISTING) {
                replaceExisting = true;
            } else {
                if (opt == null) {
                    throw new NullPointerException("options contains 'null'");
                }  else {
                    throw new UnsupportedOperationException(opt + " not supported");
                }
            }
        }

        // 尝试删除现有文件
        SecurityException se = null;
        if (replaceExisting) {
            try {
                deleteIfExists(target);
            } catch (SecurityException x) {
                se = x;
            }
        }


                    // 尝试创建目标文件。如果失败，且失败原因是
        // FileAlreadyExistsException，则可能是由于安全
        // 管理器阻止了我们删除文件，此时我们只需
        // 抛出SecurityException。
        OutputStream ostream;
        try {
            ostream = newOutputStream(target, StandardOpenOption.CREATE_NEW,
                                              StandardOpenOption.WRITE);
        } catch (FileAlreadyExistsException x) {
            if (se != null)
                throw se;
            // 其他人赢得了竞争并创建了文件
            throw x;
        }

        // 执行复制
        try (OutputStream out = ostream) {
            return copy(in, out);
        }
    }

    /**
     * 将文件中的所有字节复制到输出流中。
     *
     * <p> 如果在从文件读取或向输出流写入时发生I/O错误，则可能在读取或写入了一些字节后发生。
     * 因此，输出流可能处于不一致状态。强烈建议在发生I/O错误时立即关闭输出流。
     *
     * <p> 此方法可能无限期地阻塞向输出流写入（或从文件读取）。如果输出流
     * 被<i>异步关闭</i>或在复制过程中线程被中断，其行为高度依赖于输出流和文件系统提供者，因此未指定。
     *
     * <p> 注意，如果给定的输出流是{@link java.io.Flushable}，则在本方法完成后可能需要调用其
     * {@link java.io.Flushable#flush flush}方法以刷新任何缓冲的输出。
     *
     * @param   source
     *          文件的路径
     * @param   out
     *          要写入的输出流
     *
     * @return  读取或写入的字节数
     *
     * @throws  IOException
     *          读取或写入时发生I/O错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理器，则调用
     *          {@link SecurityManager#checkRead(String) checkRead}方法检查对文件的读取权限。
     */
    public static long copy(Path source, OutputStream out) throws IOException {
        // 在打开文件前确保非空
        Objects.requireNonNull(out);

        try (InputStream in = newInputStream(source)) {
            return copy(in, out);
        }
    }

    /**
     * 可分配的最大数组大小。
     * 一些虚拟机在数组中预留了一些头字。
     * 尝试分配更大的数组可能会导致
     * OutOfMemoryError: 请求的数组大小超过VM限制
     */
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 从输入流中读取所有字节。使用{@code initialSize}作为提示
     * 关于流将有多少字节。
     *
     * @param   source
     *          要从中读取的输入流
     * @param   initialSize
     *          分配的字节数组的初始大小
     *
     * @return  包含从文件读取的字节的字节数组
     *
     * @throws  IOException
     *          从流读取时发生I/O错误
     * @throws  OutOfMemoryError
     *          无法分配所需大小的数组
     */
    private static byte[] read(InputStream source, int initialSize) throws IOException {
        int capacity = initialSize;
        byte[] buf = new byte[capacity];
        int nread = 0;
        int n;
        for (;;) {
            // 读取到EOF，可能读取的字节数多于或少于initialSize（例如：在读取过程中文件被截断）
            while ((n = source.read(buf, nread, capacity - nread)) > 0)
                nread += n;

            // 如果最后一次调用source.read()返回-1，表示读取完成
            // 否则，尝试再读取一个字节；如果失败，也表示读取完成
            if (n < 0 || (n = source.read()) < 0)
                break;

            // 读取了一个额外的字节；需要分配更大的缓冲区
            if (capacity <= MAX_BUFFER_SIZE - capacity) {
                capacity = Math.max(capacity << 1, BUFFER_SIZE);
            } else {
                if (capacity == MAX_BUFFER_SIZE)
                    throw new OutOfMemoryError("所需数组大小过大");
                capacity = MAX_BUFFER_SIZE;
            }
            buf = Arrays.copyOf(buf, capacity);
            buf[nread++] = (byte)n;
        }
        return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
    }

    /**
     * 从文件中读取所有字节。此方法确保在读取所有字节或发生I/O错误，或其他运行时
     * 异常时关闭文件。
     *
     * <p> 注意，此方法适用于简单情况，方便将所有字节读取到字节数组中。不适用于
     * 读取大文件。
     *
     * @param   path
     *          文件的路径
     *
     * @return  包含从文件读取的字节的字节数组
     *
     * @throws  IOException
     *          从流读取时发生I/O错误
     * @throws  OutOfMemoryError
     *          无法分配所需大小的数组，例如文件大于{@code 2GB}
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理器，则调用
     *          {@link SecurityManager#checkRead(String) checkRead}方法检查对文件的读取权限。
     */
    public static byte[] readAllBytes(Path path) throws IOException {
        try (SeekableByteChannel sbc = Files.newByteChannel(path);
             InputStream in = Channels.newInputStream(sbc)) {
            long size = sbc.size();
            if (size > (long)MAX_BUFFER_SIZE)
                throw new OutOfMemoryError("所需数组大小过大");


                        return read(in, (int)size);
        }
    }

    /**
     * 从文件中读取所有行。此方法确保在读取所有字节或发生 I/O 错误或其他运行时异常时关闭文件。使用指定的字符集将文件中的字节解码为字符。
     *
     * <p> 该方法识别以下行终止符：
     * <ul>
     *   <li> <code>&#92;u000D</code> 后跟 <code>&#92;u000A</code>，即回车后跟换行 </li>
     *   <li> <code>&#92;u000A</code>，换行 </li>
     *   <li> <code>&#92;u000D</code>，回车 </li>
     * </ul>
     * <p> 未来版本可能会识别其他 Unicode 行终止符。
     *
     * <p> 注意，此方法适用于简单情况，即方便一次性读取所有行。它不适用于读取大文件。
     *
     * @param   path
     *          文件的路径
     * @param   cs
     *          用于解码的字符集
     *
     * @return  文件中的行作为 {@code List}；该 {@code List} 是否可修改取决于实现，因此未指定
     *
     * @throws  IOException
     *          如果在从文件读取时发生 I/O 错误或读取到畸形或无法映射的字节序列
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对文件的读取权限。
     *
     * @see #newBufferedReader
     */
    public static List<String> readAllLines(Path path, Charset cs) throws IOException {
        try (BufferedReader reader = newBufferedReader(path, cs)) {
            List<String> result = new ArrayList<>();
            for (;;) {
                String line = reader.readLine();
                if (line == null)
                    break;
                result.add(line);
            }
            return result;
        }
    }

    /**
     * 从文件中读取所有行。使用 {@link StandardCharsets#UTF_8 UTF-8} {@link Charset 字符集} 将文件中的字节解码为字符。
     *
     * <p> 调用此方法的效果等同于评估以下表达式：
     * <pre>{@code
     * Files.readAllLines(path, StandardCharsets.UTF_8)
     * }</pre>
     *
     * @param   path
     *          文件的路径
     *
     * @return  文件中的行作为 {@code List}；该 {@code List} 是否可修改取决于实现，因此未指定
     *
     * @throws  IOException
     *          如果在从文件读取时发生 I/O 错误或读取到畸形或无法映射的字节序列
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对文件的读取权限。
     *
     * @since 1.8
     */
    public static List<String> readAllLines(Path path) throws IOException {
        return readAllLines(path, StandardCharsets.UTF_8);
    }

    /**
     * 将字节写入文件。{@code options} 参数指定文件的创建或打开方式。如果没有指定选项，则此方法的行为类似于指定了 {@link StandardOpenOption#CREATE CREATE}、
     * {@link StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING} 和 {@link StandardOpenOption#WRITE WRITE} 选项。换句话说，它以写入模式打开文件，
     * 如果文件不存在则创建文件，或者将现有的 {@link #isRegularFile 普通文件} 截断为大小为 {@code 0}。字节数组中的所有字节都将写入文件。
     * 该方法确保在写入所有字节（或发生 I/O 错误或其他运行时异常）后关闭文件。如果发生 I/O 错误，可能在文件创建或截断后，或在写入部分字节后发生。
     *
     * <p> <b>使用示例</b>：默认情况下，该方法创建新文件或覆盖现有文件。假设您希望向现有文件追加字节：
     * <pre>
     *     Path path = ...
     *     byte[] bytes = ...
     *     Files.write(path, bytes, StandardOpenOption.APPEND);
     * </pre>
     *
     * @param   path
     *          文件的路径
     * @param   bytes
     *          要写入的字节数组
     * @param   options
     *          指定文件打开方式的选项
     *
     * @return  路径
     *
     * @throws  IOException
     *          如果在写入或创建文件时发生 I/O 错误
     * @throws  UnsupportedOperationException
     *          如果指定了不支持的选项
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法检查对文件的写入权限。
     */
    public static Path write(Path path, byte[] bytes, OpenOption... options)
        throws IOException
    {
        // 在打开文件前确保 bytes 不为 null
        Objects.requireNonNull(bytes);

        try (OutputStream out = Files.newOutputStream(path, options)) {
            int len = bytes.length;
            int rem = len;
            while (rem > 0) {
                int n = Math.min(rem, BUFFER_SIZE);
                out.write(bytes, (len-rem), n);
                rem -= n;
            }
        }
        return path;
    }

    /**
     * 将文本行写入文件。每一行是一个字符序列，按顺序写入文件，每行以平台的行分隔符终止，该分隔符由系统属性 {@code line.separator} 定义。
     * 使用指定的字符集将字符编码为字节。
     *
     * <p> {@code options} 参数指定文件的创建或打开方式。如果没有指定选项，则此方法的行为类似于指定了 {@link StandardOpenOption#CREATE CREATE}、
     * {@link StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING} 和 {@link StandardOpenOption#WRITE WRITE} 选项。换句话说，它以写入模式打开文件，
     * 如果文件不存在则创建文件，或者将现有的 {@link #isRegularFile 普通文件} 截断为大小为 {@code 0}。该方法确保在写入所有行（或发生 I/O 错误或其他运行时异常）后关闭文件。
     * 如果发生 I/O 错误，可能在文件创建或截断后，或在写入部分字节后发生。
     *
     * @param   path
     *          文件的路径
     * @param   lines
     *          用于迭代字符序列的对象
     * @param   cs
     *          用于编码的字符集
     * @param   options
     *          指定文件打开方式的选项
     *
     * @return  路径
     *
     * @throws  IOException
     *          如果在写入或创建文件时发生 I/O 错误，或无法使用指定的字符集编码文本
     * @throws  UnsupportedOperationException
     *          如果指定了不支持的选项
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法检查对文件的写入权限。
     */
    public static Path write(Path path, Iterable<? extends CharSequence> lines,
                             Charset cs, OpenOption... options)
        throws IOException
    {
        // 在打开文件前确保 lines 不为 null
        Objects.requireNonNull(lines);
        CharsetEncoder encoder = cs.newEncoder();
        try (OutputStream out = newOutputStream(path, options);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, encoder))) {
            for (CharSequence line: lines) {
                writer.append(line);
                writer.newLine();
            }
        }
        return path;
    }


/**
 * 将文本行写入文件。字符使用 {@link StandardCharsets#UTF_8 UTF-8} {@link Charset 字符集} 编码为字节。
 *
 * <p> 该方法的行为类似于调用以下表达式：
 * <pre>{@code
 * Files.write(path, lines, StandardCharsets.UTF_8, options);
 * }</pre>
 *
 * @param   path
 *          文件的路径
 * @param   lines
 *          一个可迭代的字符序列对象
 * @param   options
 *          指定文件打开方式的选项
 *
 * @return  路径
 *
 * @throws  IOException
 *          如果在写入或创建文件时发生 I/O 错误，或者文本无法编码为 {@code UTF-8}
 * @throws  UnsupportedOperationException
 *          如果指定了不支持的选项
 * @throws  SecurityException
 *          如果安装了安全经理，默认提供者会调用 {@link SecurityManager#checkWrite(String) checkWrite} 方法检查文件的写权限。
 *
 * @since 1.8
 */
public static Path write(Path path,
                         Iterable<? extends CharSequence> lines,
                         OpenOption... options)
    throws IOException
{
    return write(path, lines, StandardCharsets.UTF_8, options);
}

// -- 流 API --

/**
 * 返回一个惰性填充的 {@code Stream}，其元素是目录中的条目。列表不是递归的。
 *
 * <p> 流的元素是 {@link Path} 对象，这些对象是通过将目录条目的名称解析为 {@code dir} 获得的。某些文件系统维护指向目录本身和父目录的特殊链接。表示这些链接的条目不包括在内。
 *
 * <p> 该流是 <i>弱一致的</i>。它是线程安全的，但在迭代时不会冻结目录，因此可能会（或可能不会）反映在调用此方法后对目录的更新。
 *
 * <p> 返回的流封装了一个 {@link DirectoryStream}。如果需要及时释放文件系统资源，应使用 {@code try}-with-resources 构造来确保在流操作完成后调用流的 {@link Stream#close close} 方法。
 *
 * <p> 在流关闭后进行操作的行为就像流已到达末尾。由于预读取，流关闭后可能会返回一个或多个元素。
 *
 * <p> 如果在调用此方法后访问目录时抛出 {@link IOException}，则会将其包装在 {@link UncheckedIOException} 中，并从导致访问的该方法中抛出。
 *
 * @param   dir  目录的路径
 *
 * @return  描述目录内容的 {@code Stream}
 *
 * @throws  NotDirectoryException
 *          如果文件无法打开，因为它不是目录 <i>(可选的具体异常)</i>
 * @throws  IOException
 *          如果在打开目录时发生 I/O 错误
 * @throws  SecurityException
 *          如果安装了安全经理，默认提供者会调用 {@link SecurityManager#checkRead(String) checkRead} 方法检查目录的读权限。
 *
 * @see     #newDirectoryStream(Path)
 * @since   1.8
 */
public static Stream<Path> list(Path dir) throws IOException {
    DirectoryStream<Path> ds = Files.newDirectoryStream(dir);
    try {
        final Iterator<Path> delegate = ds.iterator();

        // 重新包装 DirectoryIteratorException 为 UncheckedIOException
        Iterator<Path> it = new Iterator<Path>() {
            @Override
            public boolean hasNext() {
                try {
                    return delegate.hasNext();
                } catch (DirectoryIteratorException e) {
                    throw new UncheckedIOException(e.getCause());
                }
            }
            @Override
            public Path next() {
                try {
                    return delegate.next();
                } catch (DirectoryIteratorException e) {
                    throw new UncheckedIOException(e.getCause());
                }
            }
        };

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.DISTINCT), false)
                            .onClose(asUncheckedRunnable(ds));
    } catch (Error|RuntimeException e) {
        try {
            ds.close();
        } catch (IOException ex) {
            try {
                e.addSuppressed(ex);
            } catch (Throwable ignore) {}
        }
        throw e;
    }
}

/**
 * 返回一个惰性填充的 {@code Stream}，通过遍历以给定起始文件为根的文件树来填充 {@code Path}。文件树以 <em>深度优先</em> 的方式遍历，流中的元素是通过将相对路径解析为 {@code start} 获得的 {@link Path} 对象。
 *
 * <p> 流在元素被消费时遍历文件树。返回的流保证至少包含一个元素，即起始文件本身。对于每个访问的文件，流尝试读取其 {@link BasicFileAttributes}。如果文件是一个目录并且可以成功打开，则目录中的条目及其 <em>后代</em> 将在流中跟随该目录，当所有条目都被访问后，目录将被关闭。然后文件树遍历继续到该目录的下一个 <em>同级</em>。
 *
 * <p> 该流是 <i>弱一致的</i>。它在迭代时不会冻结文件树，因此可能会（或可能不会）反映在调用此方法后对文件树的更新。
 *
 * <p> 默认情况下，此方法不会自动跟随符号链接。如果 {@code options} 参数包含 {@link FileVisitOption#FOLLOW_LINKS FOLLOW_LINKS} 选项，则会跟随符号链接。当跟随链接时，如果目标的属性无法读取，则此方法尝试获取链接的 {@code BasicFileAttributes}。
 *
 * <p> 如果 {@code options} 参数包含 {@link FileVisitOption#FOLLOW_LINKS FOLLOW_LINKS} 选项，则流会跟踪访问的目录，以便检测循环。循环出现在目录中有一个条目是该目录的祖先时。循环检测通过记录目录的 {@link java.nio.file.attribute.BasicFileAttributes#fileKey file-key} 来完成，或者如果文件键不可用，则通过调用 {@link #isSameFile isSameFile} 方法测试目录是否与祖先相同。当检测到循环时，它被视为 I/O 错误，抛出 {@link FileSystemLoopException}。
 *
 * <p> {@code maxDepth} 参数是访问的目录层级的最大数量。值为 {@code 0} 表示仅访问起始文件，除非被安全经理拒绝。值为 {@link Integer#MAX_VALUE MAX_VALUE} 表示应访问所有层级。
 *
 * <p> 当安装了安全经理并且它拒绝访问文件（或目录）时，该文件将被忽略，不包含在流中。
 *
 * <p> 返回的流封装了一个或多个 {@link DirectoryStream}。如果需要及时释放文件系统资源，应使用 {@code try}-with-resources 构造来确保在流操作完成后调用流的 {@link Stream#close close} 方法。在流关闭后进行操作将导致 {@link java.lang.IllegalStateException}。
 *
 * <p> 如果在调用此方法后访问目录时抛出 {@link IOException}，则会将其包装在 {@link UncheckedIOException} 中，并从导致访问的该方法中抛出。
 *
 * @param   start
 *          起始文件
 * @param   maxDepth
 *          要访问的目录层级的最大数量
 * @param   options
 *          配置遍历的选项
 *
 * @return  {@link Path} 的 {@link Stream}
 *
 * @throws  IllegalArgumentException
 *          如果 {@code maxDepth} 参数为负数
 * @throws  SecurityException
 *          如果安全经理拒绝访问起始文件。在默认提供者的情况下，会调用 {@link SecurityManager#checkRead(String) checkRead} 方法检查目录的读权限。
 * @throws  IOException
 *          如果在访问起始文件时发生 I/O 错误。
 * @since   1.8
 */
public static Stream<Path> walk(Path start,
                                int maxDepth,
                                FileVisitOption... options)
    throws IOException
{
    FileTreeIterator iterator = new FileTreeIterator(start, maxDepth, options);
    try {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.DISTINCT), false)
                            .onClose(iterator::close)
                            .map(entry -> entry.file());
    } catch (Error|RuntimeException e) {
        iterator.close();
        throw e;
    }
}


                /**
     * 返回一个 {@code Stream}，该 {@code Stream} 通过遍历以给定起始文件为根的文件树来懒加载 {@code
     * Path}。文件树以 <em>深度优先</em> 的方式遍历，流中的元素是通过将相对路径 {@link
     * Path#resolve(Path) 解析} 到 {@code start} 获得的 {@link Path} 对象。
     *
     * <p> 该方法的工作方式相当于调用以下表达式：
     * <blockquote><pre>
     * walk(start, Integer.MAX_VALUE, options)
     * </pre></blockquote>
     * 换句话说，它访问文件树的所有层级。
     *
     * <p> 返回的流封装了一个或多个 {@link DirectoryStream}。如果需要及时释放文件系统资源，应使用
     * {@code try}-with-resources 构造来确保在流操作完成后调用流的 {@link Stream#close close} 方法。对已关闭的流进行操作将导致
     * {@link java.lang.IllegalStateException}。
     *
     * @param   start
     *          起始文件
     * @param   options
     *          配置遍历的选项
     *
     * @return  {@link Stream} of {@link Path}
     *
     * @throws  SecurityException
     *          如果安全管理器拒绝访问起始文件。对于默认提供者，将调用 {@link
     *          SecurityManager#checkRead(String) checkRead} 方法检查对目录的读取权限。
     * @throws  IOException
     *          如果在访问起始文件时发生 I/O 错误。
     *
     * @see     #walk(Path, int, FileVisitOption...)
     * @since   1.8
     */
    public static Stream<Path> walk(Path start, FileVisitOption... options) throws IOException {
        return walk(start, Integer.MAX_VALUE, options);
    }

    /**
     * 返回一个 {@code Stream}，该 {@code Stream} 通过在以给定起始文件为根的文件树中搜索文件来懒加载 {@code
     * Path}。
     *
     * <p> 该方法以与 {@link #walk walk} 方法完全相同的方式遍历文件树。对于遇到的每个文件，将调用给定的
     * {@link BiPredicate}，传入其 {@link Path} 和 {@link
     * BasicFileAttributes}。{@code Path} 对象是通过将相对路径 {@link
     * Path#resolve(Path) 解析} 到 {@code start} 获得的，只有当 {@code BiPredicate} 返回 true 时，才会包含在返回的 {@link Stream} 中。与调用
     * {@code walk} 方法返回的 {@code Stream} 上的 {@link
     * java.util.stream.Stream#filter filter} 方法相比，此方法可能更高效，因为它避免了重复检索 {@code BasicFileAttributes}。
     *
     * <p> 返回的流封装了一个或多个 {@link DirectoryStream}。如果需要及时释放文件系统资源，应使用
     * {@code try}-with-resources 构造来确保在流操作完成后调用流的 {@link Stream#close close} 方法。对已关闭的流进行操作将导致
     * {@link java.lang.IllegalStateException}。
     *
     * <p> 如果在从该方法返回后访问目录时发生 {@link IOException}，它将被包装在 {@link
     * UncheckedIOException} 中，该异常将从导致访问发生的 {@code Stream} 方法中抛出。
     *
     * @param   start
     *          起始文件
     * @param   maxDepth
     *          要搜索的最大目录层级数
     * @param   matcher
     *          用于决定文件是否应包含在返回的流中的函数
     * @param   options
     *          配置遍历的选项
     *
     * @return  {@link Stream} of {@link Path}
     *
     * @throws  IllegalArgumentException
     *          如果 {@code maxDepth} 参数为负数
     * @throws  SecurityException
     *          如果安全管理器拒绝访问起始文件。对于默认提供者，将调用 {@link
     *          SecurityManager#checkRead(String) checkRead} 方法检查对目录的读取权限。
     * @throws  IOException
     *          如果在访问起始文件时发生 I/O 错误。
     *
     * @see     #walk(Path, int, FileVisitOption...)
     * @since   1.8
     */
    public static Stream<Path> find(Path start,
                                    int maxDepth,
                                    BiPredicate<Path, BasicFileAttributes> matcher,
                                    FileVisitOption... options)
        throws IOException
    {
        FileTreeIterator iterator = new FileTreeIterator(start, maxDepth, options);
        try {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.DISTINCT), false)
                                .onClose(iterator::close)
                                .filter(entry -> matcher.test(entry.file(), entry.attributes()))
                                .map(entry -> entry.file());
        } catch (Error|RuntimeException e) {
            iterator.close();
            throw e;
        }
    }

    /**
     * 从文件中读取所有行并作为 {@code Stream} 返回。与 {@link
     * #readAllLines(Path, Charset) readAllLines} 不同，此方法不会将所有行读入 {@code List}，而是随着流的消耗懒加载。
     *
     * <p> 文件中的字节使用指定的字符集解码为字符，并支持与 {@code
     * readAllLines} 相同的行终止符。
     *
     * <p> 该方法返回后，如果在读取文件或读取到畸形或无法映射的字节序列时发生 I/O 错误，将被包装在 {@link UncheckedIOException} 中，并从导致读取发生的
     * {@link java.util.stream.Stream} 方法中抛出。如果在关闭文件时发生 {@code IOException}，它也将被包装为 {@code UncheckedIOException}。
     *
     * <p> 返回的流封装了一个 {@link Reader}。如果需要及时释放文件系统资源，应使用 try-with-resources 构造来确保在流操作完成后调用流的
     * {@link Stream#close close} 方法。
     *
     *
     * @param   path
     *          文件的路径
     * @param   cs
     *          用于解码的字符集
     *
     * @return  文件中的行作为 {@code Stream}
     *
     * @throws  IOException
     *          如果在打开文件时发生 I/O 错误
     * @throws  SecurityException
     *          对于默认提供者，如果安装了安全管理器，将调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对文件的读取权限。
     *
     * @see     #readAllLines(Path, Charset)
     * @see     #newBufferedReader(Path, Charset)
     * @see     java.io.BufferedReader#lines()
     * @since   1.8
     */
    public static Stream<String> lines(Path path, Charset cs) throws IOException {
        BufferedReader br = Files.newBufferedReader(path, cs);
        try {
            return br.lines().onClose(asUncheckedRunnable(br));
        } catch (Error|RuntimeException e) {
            try {
                br.close();
            } catch (IOException ex) {
                try {
                    e.addSuppressed(ex);
                } catch (Throwable ignore) {}
            }
            throw e;
        }
    }

                /**
     * 从文件中读取所有行作为 {@code Stream}。使用 {@link StandardCharsets#UTF_8 UTF-8}
     * {@link Charset 字符集} 将文件中的字节解码为字符。
     *
     * <p> 该方法的调用等效于评估以下表达式：
     * <pre>{@code
     * Files.lines(path, StandardCharsets.UTF_8)
     * }</pre>
     *
     * @param   path
     *          文件的路径
     *
     * @return  文件中的行作为 {@code Stream}
     *
     * @throws  IOException
     *          如果在打开文件时发生 I/O 错误
     * @throws  SecurityException
     *          如果使用默认提供程序，并且安装了安全管理者，则调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法检查对文件的读取权限。
     *
     * @since 1.8
     */
    public static Stream<String> lines(Path path) throws IOException {
        return lines(path, StandardCharsets.UTF_8);
    }
}
