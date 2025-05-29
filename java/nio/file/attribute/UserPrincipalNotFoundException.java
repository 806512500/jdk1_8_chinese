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
 * 当 {@link UserPrincipal} 查找失败，因为该主体不存在时抛出的检查异常。
 *
 * @since 1.7
 */

public class UserPrincipalNotFoundException
    extends IOException
{
    static final long serialVersionUID = -5369283889045833024L;

    private final String name;

    /**
     * 构造此类的一个实例。
     *
     * @param   name
     *          主体名称；可能是 {@code null}
     */
    public UserPrincipalNotFoundException(String name) {
        super();
        this.name = name;
    }

    /**
     * 如果此异常是使用未找到的用户主体名称创建的，则返回用户主体名称，否则返回 <tt>null</tt>。
     *
     * @return  用户主体名称或 {@code null}
     */
    public String getName() {
        return name;
    }
}
