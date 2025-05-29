/*
 * 版权所有 (c) 2007, 2011, Oracle 和/或其关联公司。保留所有权利。
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

import java.io.IOException;

/**
 * 支持读取或更新文件所有者的文件属性视图。
 * 此文件属性视图适用于支持表示文件所有者的身份的文件属性的文件系统实现。通常，文件的所有者是创建文件的实体的身份。
 *
 * <p> 可以使用 {@link #getOwner getOwner} 或 {@link #setOwner setOwner} 方法来读取或更新文件的所有者。
 *
 * <p> 也可以使用 {@link java.nio.file.Files#getAttribute getAttribute} 和
 * {@link java.nio.file.Files#setAttribute setAttribute} 方法来读取或更新所有者。在这种情况下，所有者属性的名称为 {@code "owner"}，属性的值为
 * {@link UserPrincipal}。
 *
 * @since 1.7
 */

public interface FileOwnerAttributeView
    extends FileAttributeView
{
    /**
     * 返回属性视图的名称。此类属性视图的名称为 {@code "owner"}。
     */
    @Override
    String name();

    /**
     * 读取文件所有者。
     *
     * <p> 文件所有者是否可以是 {@link
     * GroupPrincipal 组} 是特定于实现的。
     *
     * @return  文件所有者
     *
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，安装了安全管理器，并且它拒绝 {@link
     *          RuntimePermission}<tt>("accessUserInformation")</tt> 或其
     *          {@link SecurityManager#checkRead(String) checkRead} 方法拒绝读取文件的访问权限。
     */
    UserPrincipal getOwner() throws IOException;

    /**
     * 更新文件所有者。
     *
     * <p> 文件所有者是否可以是 {@link
     * GroupPrincipal 组} 是特定于实现的。为了确保跨平台的一致和正确行为，建议仅使用此方法将文件所有者设置为不是组的用户主体。
     *
     * @param   owner
     *          新的文件所有者
     *
     * @throws  IOException
     *          如果发生 I/O 错误，或者 {@code owner} 参数是组且此实现不支持将所有者设置为组
     * @throws  SecurityException
     *          在默认提供程序的情况下，安装了安全管理器，并且它拒绝 {@link
     *          RuntimePermission}<tt>("accessUserInformation")</tt> 或其
     *          {@link SecurityManager#checkWrite(String) checkWrite} 方法拒绝写入文件的访问权限。
     */
    void setOwner(UserPrincipal owner) throws IOException;
}
