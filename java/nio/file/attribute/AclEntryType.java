/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file.attribute;

/**
 * 访问控制条目类型的类型安全枚举。
 *
 * @since 1.7
 */

public enum AclEntryType {
    /**
     * 明确授予对文件或目录的访问权限。
     */
    ALLOW,

    /**
     * 明确拒绝访问文件或目录。
     */
    DENY,

    /**
     * 以系统依赖的方式记录 ACL 条目权限组件中指定的访问。
     */
    AUDIT,

    /**
     * 以系统依赖的方式生成报警，针对 ACL 条目权限组件中指定的访问。
     */
    ALARM
}
