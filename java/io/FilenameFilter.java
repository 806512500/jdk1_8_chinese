/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 实现此接口的类的实例用于过滤文件名。这些实例用于在
 * <code>File</code> 类的 <code>list</code> 方法中过滤目录列表，
 * 以及由 Abstract Window Toolkit 的文件对话组件使用。
 *
 * @author  Arthur van Hoff
 * @author  Jonathan Payne
 * @see     java.awt.FileDialog#setFilenameFilter(java.io.FilenameFilter)
 * @see     java.io.File
 * @see     java.io.File#list(java.io.FilenameFilter)
 * @since   JDK1.0
 */
@FunctionalInterface
public interface FilenameFilter {
    /**
     * 测试指定的文件是否应包含在文件列表中。
     *
     * @param   dir    文件所在的目录。
     * @param   name   文件的名称。
     * @return  如果且仅当名称应包含在文件列表中时返回 <code>true</code>；
     * 否则返回 <code>false</code>。
     */
    boolean accept(File dir, String name);
}
