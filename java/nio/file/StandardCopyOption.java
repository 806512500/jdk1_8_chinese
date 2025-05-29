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

package java.nio.file;

/**
 * 定义标准的复制选项。
 *
 * @since 1.7
 */

public enum StandardCopyOption implements CopyOption {
    /**
     * 如果文件已存在，则替换它。
     */
    REPLACE_EXISTING,
    /**
     * 将属性复制到新文件。
     */
    COPY_ATTRIBUTES,
    /**
     * 将文件作为原子文件系统操作进行移动。
     */
    ATOMIC_MOVE;
}
