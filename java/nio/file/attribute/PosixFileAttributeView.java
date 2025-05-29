
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

package java.nio.file.attribute;

import java.nio.file.*;
import java.util.Set;
import java.io.IOException;

/**
 * 提供一个视图，用于查看通常与实现 Portable Operating System Interface (POSIX) 标准系列的操作系统所使用的文件系统中的文件相关联的文件属性。
 *
 * <p> 实现 <a href="http://www.opengroup.org">POSIX</a> 标准系列的操作系统通常使用具有文件 <em>所有者</em>、<em>组所有者</em> 和相关 <em>访问权限</em> 的文件系统。此文件属性视图提供了对这些属性的读写访问。
 *
 * <p> {@link #readAttributes() readAttributes} 方法用于读取文件的属性。文件 {@link PosixFileAttributes#owner() 所有者} 由一个 {@link UserPrincipal} 表示，该身份是用于访问控制的文件所有者的身份。文件的 {@link PosixFileAttributes#group() 组所有者} 由一个 {@link GroupPrincipal} 表示，该身份是为管理目的创建的，用于确定组成员的访问权限。
 *
 * <p> {@link PosixFileAttributes#permissions() 权限} 属性是一组访问权限。此文件属性视图提供了对 {@link PosixFilePermission} 类定义的九个权限的访问。这九个权限位决定了文件所有者、组和其他人（其他人指除所有者和组成员之外的身份）的 <em>读取</em>、<em>写入</em> 和 <em>执行</em> 访问权限。某些操作系统和文件系统可能提供额外的权限位，但此版本的类中未定义对这些其他位的访问。
 *
 * <p> <b>使用示例：</b>
 * 假设我们需要打印出文件的所有者和访问权限：
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
 * <p> 可以使用 {@link Files#getAttribute getAttribute} 方法读取这些属性中的任何一个，或 {@link BasicFileAttributeView} 定义的任何属性，就像调用 {@link #readAttributes readAttributes()} 方法一样。
 *
 * <p> 可以使用 {@link Files#setAttribute setAttribute} 方法更新文件的最后修改时间、最后访问时间或创建时间属性，这些属性由 {@link BasicFileAttributeView} 定义。也可以用于更新权限、所有者或组所有者，就像分别调用 {@link #setPermissions setPermissions}、{@link #setOwner setOwner} 和 {@link #setGroup setGroup} 方法一样。
 *
 * <h2> 设置初始权限 </h2>
 * <p> 支持此属性视图的实现也可能支持在创建文件或目录时设置初始权限。初始权限作为 {@link FileAttribute} 提供给 {@link Files#createFile createFile} 或 {@link Files#createDirectory createDirectory} 方法，其 {@link FileAttribute#name 名称} 为 {@code "posix:permissions"}，其 {@link FileAttribute#value 值} 为权限集。以下示例使用 {@link PosixFilePermissions#asFileAttribute asFileAttribute} 方法在创建文件时构造一个 {@code FileAttribute}：
 *
 * <pre>
 *     Path path = ...
 *     Set&lt;PosixFilePermission&gt; perms =
 *         EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ);
 *     Files.createFile(path, PosixFilePermissions.asFileAttribute(perms));
 * </pre>
 *
 * <p> 当在文件创建时设置访问权限时，实际的权限值可能与属性对象的值不同。这是实现特定的。例如，在 UNIX 系统上，进程有一个 <em>umask</em>，它会影响新创建文件的权限位。如果实现支持设置访问权限，并且底层文件系统支持访问权限，那么要求实际访问权限的值等于或小于提供给 {@link Files#createFile createFile} 或 {@link Files#createDirectory createDirectory} 方法的属性的值。换句话说，文件可能比请求的更安全。
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
     *          如果使用默认提供程序，安装了安全管理器，并且它拒绝 {@link RuntimePermission}<tt>("accessUserInformation")</tt>
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
     *          如果集合包含的元素不是 {@code
     *          PosixFilePermission} 类型
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供者的情况下，如果安装了安全管理者，并且它拒绝 {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          或其 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法拒绝写入文件的权限。
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
     *          在默认提供者的情况下，如果安装了安全管理者，并且它拒绝 {@link RuntimePermission}<tt>("accessUserInformation")</tt>
     *          或其 {@link SecurityManager#checkWrite(String) checkWrite}
     *          方法拒绝写入文件的权限。
     */
    void setGroup(GroupPrincipal group) throws IOException;
}
