/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 一个用于抽象路径名的过滤器。
 *
 * <p> 该接口的实例可以传递给 <code>{@link
 * File#listFiles(java.io.FileFilter) listFiles(FileFilter)}</code> 方法
 * 的 <code>{@link java.io.File}</code> 类。
 *
 * @since 1.2
 */
@FunctionalInterface
public interface FileFilter {

    /**
     * 测试指定的抽象路径名是否应包含在路径名列表中。
     *
     * @param  pathname  要测试的抽象路径名
     * @return  <code>true</code> 如果且仅当 <code>pathname</code>
     *          应被包含
     */
    boolean accept(File pathname);
}
