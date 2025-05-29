/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

/**
 *
 * @since 1.8
 */
class DefaultFileSystem {

    /**
     * 返回适用于 Windows 平台的 FileSystem 对象。
     */
    public static FileSystem getFileSystem() {
        return new WinNTFileSystem();
    }
}
