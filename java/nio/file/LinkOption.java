/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * 定义了如何处理符号链接的选项。
 *
 * @since 1.7
 */

public enum LinkOption implements OpenOption, CopyOption {
    /**
     * 不跟随符号链接。
     *
     * @see Files#getFileAttributeView(Path,Class,LinkOption[])
     * @see Files#copy
     * @see SecureDirectoryStream#newByteChannel
     */
    NOFOLLOW_LINKS;
}
