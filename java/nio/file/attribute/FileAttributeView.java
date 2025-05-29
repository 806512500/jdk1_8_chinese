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
 * 一个文件系统中与文件关联的非不透明值的只读或可更新视图。此接口由特定的文件属性视图扩展或实现，
 * 定义了用于读取和/或更新文件属性的方法。
 *
 * @since 1.7
 *
 * @see java.nio.file.Files#getFileAttributeView(Path,Class,java.nio.file.LinkOption[])
 */

public interface FileAttributeView
    extends AttributeView
{
}
