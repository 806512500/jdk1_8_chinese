/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
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
 * 等方法，当打开或创建文件时。
 *
 * <p> {@link StandardOpenOption} 枚举类型定义了
 * <i>标准</i> 选项。
 *
 * @since 1.7
 */

public interface OpenOption {
}
