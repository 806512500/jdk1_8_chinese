/*
 * Copyright (c) 2007, 2010, Oracle and/or its affiliates. All rights reserved.
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
 * 定义文件树遍历选项。
 *
 * @since 1.7
 *
 * @see Files#walkFileTree
 */

public enum FileVisitOption {
    /**
     * 跟随符号链接。
     */
    FOLLOW_LINKS;
}
