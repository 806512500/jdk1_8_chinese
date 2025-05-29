/*
 * 版权所有 (c) 2007, 2009, Oracle 和/或其附属公司。保留所有权利。
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
 * 一个根据名称查找用户和组主体的对象。{@link UserPrincipal} 表示一个可用于确定文件系统中对象访问权限的身份。
 * {@link GroupPrincipal} 表示一个 <em>组身份</em>。{@code UserPrincipalLookupService} 定义了通过名称或组名称（通常是用户或账户名称）
 * 查找身份的方法。名称和组名称是否区分大小写取决于实现。组的确切定义是实现特定的，但通常组表示为了管理目的而创建的身份，以确定组成员的访问权限。
 * 特别是，名称和组的 <em>命名空间</em> 是否相同或不同是实现特定的。为了确保跨平台的一致和正确行为，建议将此 API 用作命名空间是不同的。
 * 换句话说，应该使用 {@link #lookupPrincipalByName lookupPrincipalByName} 查找用户，而应该使用 {@link
 * #lookupPrincipalByGroupName lookupPrincipalByGroupName} 查找组。
 *
 * @since 1.7
 *
 * @see java.nio.file.FileSystem#getUserPrincipalLookupService
 */

public abstract class UserPrincipalLookupService {

    /**
     * 初始化此类的新实例。
     */
    protected UserPrincipalLookupService() {
    }

    /**
     * 通过名称查找用户主体。
     *
     * @param   name
     *          要查找的用户主体的字符串表示形式
     *
     * @return  一个用户主体
     *
     * @throws  UserPrincipalNotFoundException
     *          主体不存在
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全经理，它会检查 {@link RuntimePermission}<tt>("lookupUserInformation")</tt>
     */
    public abstract UserPrincipal lookupPrincipalByName(String name)
        throws IOException;

    /**
     * 通过组名称查找组主体。
     *
     * <p> 如果实现不支持任何组的概念，则此方法总是抛出 {@link UserPrincipalNotFoundException}。
     * 如果用户账户和组的命名空间相同，那么此方法与调用 {@link #lookupPrincipalByName
     * lookupPrincipalByName} 相同。
     *
     * @param   group
     *          要查找的组的字符串表示形式
     *
     * @return  一个组主体
     *
     * @throws  UserPrincipalNotFoundException
     *          主体不存在或不是组
     * @throws  IOException
     *          如果发生 I/O 错误
     * @throws  SecurityException
     *          在默认提供程序的情况下，如果安装了安全经理，它会检查 {@link RuntimePermission}<tt>("lookupUserInformation")</tt>
     */
    public abstract GroupPrincipal lookupPrincipalByGroupName(String group)
        throws IOException;
}
