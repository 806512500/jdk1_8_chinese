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
 * 一个配置如何打开或创建文件的对象。
 *
 * <p> 这种类型的对象用于诸如 {@link
 * Files#newOutputStream(Path,OpenOption[]) newOutputStream}，{@link
 * Files#newByteChannel newByteChannel}，{@link
 * java.nio.channels.FileChannel#open FileChannel.open} 和 {@link
 * java.nio.channels.AsynchronousFileChannel#open AsynchronousFileChannel.open}
 * 等方法，在打开或创建文件时使用。
 *
 * <p> {@link StandardOpenOption} 枚举类型定义了
 * <i>标准</i> 选项。
 *
 * @since 1.7
 */

public interface OpenOption {
}
