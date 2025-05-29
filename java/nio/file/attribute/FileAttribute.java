/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 一个封装文件属性值的对象，该属性值可以在创建新文件或目录时通过调用 {@link
 * java.nio.file.Files#createFile createFile} 或 {@link
 * java.nio.file.Files#createDirectory createDirectory} 方法原子地设置。
 *
 * @param <T> 文件属性值的类型
 *
 * @since 1.7
 * @see PosixFilePermissions#asFileAttribute
 */

public interface FileAttribute<T> {
    /**
     * 返回属性名称。
     *
     * @return 属性名称
     */
    String name();

    /**
     * 返回属性值。
     *
     * @return 属性值
     */
    T value();
}
