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
 * 定义了用于 ACL {@link AclEntry 条目} 的标志组件的标志。
 *
 * <p> 在此版本中，此类不定义与 {@link AclEntryType#AUDIT} 和 {@link AclEntryType#ALARM} 条目类型相关的标志。
 *
 * @since 1.7
 */

public enum AclEntryFlag {

    /**
     * 可以放置在目录上，表示应将 ACL 条目添加到每个新创建的非目录文件中。
     */
    FILE_INHERIT,

    /**
     * 可以放置在目录上，表示应将 ACL 条目添加到每个新创建的目录中。
     */
    DIRECTORY_INHERIT,

    /**
     * 可以放置在目录上，表示不应将 ACL 条目放置在新创建的目录上，该目录可由创建的目录的子目录继承。
     */
    NO_PROPAGATE_INHERIT,

    /**
     * 可以放置在目录上，但不适用于目录本身，仅适用于由 {@link #FILE_INHERIT} 和 {@link #DIRECTORY_INHERIT} 标志指定的新创建的文件/目录。
     */
    INHERIT_ONLY;
}
