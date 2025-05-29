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
 * 一个配置如何复制或移动文件的对象。
 *
 * <p> 这种类型的对象可以与 {@link
 * Files#copy(Path,Path,CopyOption[]) Files.copy(Path,Path,CopyOption...)}，
 * {@link Files#copy(java.io.InputStream,Path,CopyOption[])
 * Files.copy(InputStream,Path,CopyOption...)} 和 {@link Files#move
 * Files.move(Path,Path,CopyOption...)} 方法一起使用，以配置文件的复制或移动方式。
 *
 * <p> {@link StandardCopyOption} 枚举类型定义了
 * <i>标准</i> 选项。
 *
 * @since 1.7
 */

public interface CopyOption {
}
