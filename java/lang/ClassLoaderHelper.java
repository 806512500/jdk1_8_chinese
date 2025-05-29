/*
 * Copyright (c) 2012, 2021, Oracle and/or its affiliates. All rights reserved.
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
package java.lang;

import java.io.File;

class ClassLoaderHelper {

    private ClassLoaderHelper() {}

    /**
     * 如果仅当本地库存在于文件系统中时才加载该库，则返回 true。
     */
    static boolean loadLibraryOnlyIfPresent() {
        return true;
    }

    /**
     * 返回给定文件的备用路径名，使得如果原始路径名不存在，则文件可能位于备用位置。
     * 对于大多数平台，此行为不受支持，返回 null。
     */
    static File mapAlternativeName(File lib) {
        return null;
    }
}