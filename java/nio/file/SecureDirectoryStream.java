
/*
 * 版权所有 (c) 2007, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

import java.nio.file.attribute.*;
import java.nio.channels.SeekableByteChannel;
import java.util.Set;
import java.io.IOException;

/**
 * 一个 {@code DirectoryStream}，定义了对相对于打开目录的位置的文件的操作。一个 {@code SecureDirectoryStream} 旨在
 * 供复杂的或对安全敏感的应用程序使用，这些应用程序需要以无竞争的方式遍历文件树或以其他方式操作目录。
 * 当一系列文件操作不能独立执行时，可能会出现竞争条件。此接口定义的每个文件操作都指定一个相对路径。无论目录在打开期间被移动或被攻击者替换，
 * 所有对文件的访问都是相对于打开的目录的。一个 {@code SecureDirectoryStream} 也可以用作虚拟 <em>工作目录</em>。
 *
 * <p> 一个 {@code SecureDirectoryStream} 需要底层操作系统的相应支持。如果实现支持此功能，则由 {@link Files#newDirectoryStream
 * newDirectoryStream} 方法返回的 {@code DirectoryStream} 将是一个 {@code SecureDirectoryStream}，必须将其转换为此类型
 * 以调用此接口定义的方法。
 *
 * <p> 在默认 {@link java.nio.file.spi.FileSystemProvider
 * 提供者} 的情况下，如果设置了安全管理者，那么权限检查将使用通过将给定的相对路径解析为目录的 <i>原始路径</i> 获得的路径进行（无论目录自打开以来是否被移动）。
 *
 * @since   1.7
 */

public interface SecureDirectoryStream<T>
    extends DirectoryStream<T>
{
    /**
     * 打开由给定路径标识的目录，返回一个 {@code
     * SecureDirectoryStream} 以迭代目录中的条目。
     *
     * <p> 此方法的工作方式与 {@link
     * Files#newDirectoryStream(Path) newDirectoryStream} 方法完全相同，适用于
     * {@code path} 参数是 {@link Path#isAbsolute 绝对} 路径的情况。
     * 当参数是相对路径时，要打开的目录相对于此打开的目录。可以使用 {@link
     * LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} 选项确保此方法在文件是符号链接时失败。
     *
     * <p> 一旦创建，新的目录流就不依赖于用于创建它的目录流。关闭此目录流对新创建的目录流没有影响。
     *
     * @param   path
     *          要打开的目录的路径
     * @param   options
     *          指定如何处理符号链接的选项
     *
     * @return  一个新的且打开的 {@code SecureDirectoryStream} 对象
     *
     * @throws  ClosedDirectoryStreamException
     *          如果目录流已关闭
     * @throws  NotDirectoryException
     *          如果文件无法打开，因为它不是目录 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，将调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法以检查对目录的读取权限。
     */
    SecureDirectoryStream<T> newDirectoryStream(T path, LinkOption... options)
        throws IOException;

    /**
     * 在此目录中打开或创建一个文件，返回一个可寻址的字节通道以访问文件。
     *
     * <p> 此方法的工作方式与 {@link
     * Files#newByteChannel Files.newByteChannel} 方法完全相同，适用于
     * {@code path} 参数是 {@link Path#isAbsolute 绝对} 路径的情况。
     * 当参数是相对路径时，要打开或创建的文件相对于此打开的目录。除了 {@code Files.newByteChannel} 方法定义的选项外，
     * 还可以使用 {@link
     * LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} 选项确保此方法在文件是符号链接时失败。
     *
     * <p> 一旦创建，通道就不依赖于用于创建它的目录流。关闭此目录流对通道没有影响。
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
     *          如果集合包含无效的选项组合
     * @throws  UnsupportedOperationException
     *          如果指定了不支持的打开选项，或者数组包含在创建文件时无法原子地设置的属性
     * @throws  FileAlreadyExistsException
     *          如果同名文件已存在且指定了 {@link
     *          StandardOpenOption#CREATE_NEW CREATE_NEW} 选项
     *          <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，将调用 {@link SecurityManager#checkRead(String) checkRead}
     *          方法以检查对路径的读取权限，如果文件以读取方式打开。将调用 {@link SecurityManager#checkWrite(String)
     *          checkWrite} 方法以检查对路径的写入权限，如果文件以写入方式打开。
     */
    SeekableByteChannel newByteChannel(T path,
                                       Set<? extends OpenOption> options,
                                       FileAttribute<?>... attrs)
        throws IOException;

                /**
     * 删除一个文件。
     *
     * <p> 与 {@link Files#delete delete()} 方法不同，此方法不会首先检查文件是否为目录。
     * 通过此方法删除目录是否成功取决于系统，因此未作具体说明。如果文件是符号链接，则删除链接本身，而不是链接的最终目标。
     * 当参数为相对路径时，要删除的文件相对于此打开的目录。
     *
     * @param   path
     *          要删除的文件的路径
     *
     * @throws  ClosedDirectoryStreamException
     *          如果目录流已关闭
     * @throws  NoSuchFileException
     *          如果文件不存在 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkDelete(String) checkDelete}
     *          方法检查文件的删除权限
     */
    void deleteFile(T path) throws IOException;

    /**
     * 删除一个目录。
     *
     * <p> 与 {@link Files#delete delete()} 方法不同，此方法不会首先检查文件是否为目录。
     * 通过此方法删除非目录文件是否成功取决于系统，因此未作具体说明。当参数为相对路径时，要删除的目录相对于此打开的目录。
     *
     * @param   path
     *          要删除的目录的路径
     *
     * @throws  ClosedDirectoryStreamException
     *          如果目录流已关闭
     * @throws  NoSuchFileException
     *          如果目录不存在 <i>(可选特定异常)</i>
     * @throws  DirectoryNotEmptyException
     *          如果目录不为空，无法删除 <i>(可选特定异常)</i>
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkDelete(String) checkDelete}
     *          方法检查目录的删除权限
     */
    void deleteDirectory(T path) throws IOException;

    /**
     * 将文件从一个目录移动到另一个目录。
     *
     * <p> 此方法的工作方式类似于指定 {@link StandardCopyOption#ATOMIC_MOVE ATOMIC_MOVE} 选项时的
     * {@link Files#move move} 方法。也就是说，此方法以原子文件系统操作的方式移动文件。如果 {@code srcpath} 参数是
     * {@link Path#isAbsolute 绝对} 路径，则定位源文件。如果参数是相对路径，则相对于此打开的目录定位。如果
     * {@code targetpath} 参数是绝对路径，则定位目标文件（忽略 {@code targetdir} 参数）。如果参数是相对路径，
     * 则相对于由 {@code targetdir} 参数标识的打开目录定位。在所有情况下，如果目标文件存在，则是否替换或此方法失败
     * 取决于具体实现。
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
     *          如果目标目录中文件已存在且无法替换 <i>(可选特定异常)</i>
     * @throws  AtomicMoveNotSupportedException
     *          如果文件无法作为原子文件系统操作移动
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全经理，则调用 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法检查源文件和目标文件的写权限。
     */
    void move(T srcpath, SecureDirectoryStream<T> targetdir, T targetpath)
        throws IOException;

    /**
     * 返回一个新的文件属性视图以访问此目录的文件属性。
     *
     * <p> 所得的文件属性视图可用于读取或更新此（打开的）目录的属性。{@code type} 参数指定属性视图的类型，
     * 如果支持该类型，方法将返回该类型的实例。调用此方法获取 {@link BasicFileAttributeView} 始终返回该类的实例，
     * 该实例绑定到此打开的目录。
     *
     * <p> 所得文件属性视图的状态与该目录流紧密相关。一旦目录流 {@link #close 关闭}，所有读取或更新属性的方法
     * 都将抛出 {@link ClosedDirectoryStreamException ClosedDirectoryStreamException}。
     *
     * @param   <V>
     *          {@code FileAttributeView} 类型
     * @param   type
     *          对应于文件属性视图的 {@code Class} 对象
     *
     * @return  指定类型的新的文件属性视图，绑定到此目录流，如果属性视图类型不可用则返回 {@code null}
     */
    <V extends FileAttributeView> V getFileAttributeView(Class<V> type);

    /**
     * 返回一个新的文件属性视图以访问此目录中文件的文件属性。
     *
     * <p> 所得的文件属性视图可用于读取或更新此目录中文件的属性。{@code type} 参数指定属性视图的类型，
     * 如果支持该类型，方法将返回该类型的实例。调用此方法获取 {@link BasicFileAttributeView} 始终返回该类的实例，
     * 该实例绑定到目录中的文件。
     *
     * <p> 所得文件属性视图的状态与该目录流紧密相关。一旦目录流 {@link #close 关闭}，所有读取或更新属性的方法
     * 都将抛出 {@link ClosedDirectoryStreamException ClosedDirectoryStreamException}。文件在创建文件属性视图时
     * 不需要存在，但如果文件不存在，调用读取或更新文件属性的方法时将失败。
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
     * @return  指定类型的新的文件属性视图，绑定到此目录流，如果属性视图类型不可用则返回 {@code null}
     *
     */
    <V extends FileAttributeView> V getFileAttributeView(T path,
                                                         Class<V> type,
                                                         LinkOption... options);
}
