/*
 * 版权所有 (c) 2007, 2011, Oracle 和/或其附属公司。保留所有权利。
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

/**
 * 定义用于 {@link PosixFileAttributes#permissions() 权限} 属性的位。
 *
 * <p> {@link PosixFilePermissions} 类定义了用于操作权限集的方法。
 *
 * @since 1.7
 */

public enum PosixFilePermission {

    /**
     * 所有者的读取权限。
     */
    OWNER_READ,

    /**
     * 所有者的写入权限。
     */
    OWNER_WRITE,

    /**
     * 所有者的执行/搜索权限。
     */
    OWNER_EXECUTE,

    /**
     * 组的读取权限。
     */
    GROUP_READ,

    /**
     * 组的写入权限。
     */
    GROUP_WRITE,

    /**
     * 组的执行/搜索权限。
     */
    GROUP_EXECUTE,

    /**
     * 其他人的读取权限。
     */
    OTHERS_READ,

    /**
     * 其他人的写入权限。
     */
    OTHERS_WRITE,

    /**
     * 其他人的执行/搜索权限。
     */
    OTHERS_EXECUTE;
}
