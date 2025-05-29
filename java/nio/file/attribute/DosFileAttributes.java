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

package java.nio.file.attribute;

/**
 * 与支持传统 "DOS" 属性的文件系统中的文件关联的文件属性。
 *
 * <p> <b>使用示例：</b>
 * <pre>
 *    Path file = ...
 *    DosFileAttributes attrs = Files.readAttributes(file, DosFileAttributes.class);
 * </pre>
 *
 * @since 1.7
 */

public interface DosFileAttributes
    extends BasicFileAttributes
{
    /**
     * 返回只读属性的值。
     *
     * <p> 该属性通常用作简单的访问控制机制，以防止文件被删除或更新。文件系统或平台是否对 <em>只读</em> 文件进行任何强制以防止其被更新是实现特定的。
     *
     * @return  只读属性的值
     */
    boolean isReadOnly();

    /**
     * 返回隐藏属性的值。
     *
     * <p> 该属性通常用于指示文件是否对用户可见。
     *
     * @return  隐藏属性的值
     */
    boolean isHidden();

    /**
     * 返回存档属性的值。
     *
     * <p> 该属性通常由备份程序使用。
     *
     * @return  存档属性的值
     */
    boolean isArchive();

    /**
     * 返回系统属性的值。
     *
     * <p> 该属性通常用于指示文件是操作系统的一部分。
     *
     * @return  系统属性的值
     */
    boolean isSystem();
}
