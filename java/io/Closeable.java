/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

import java.io.IOException;

/**
 * 一个 {@code Closeable} 是一个可以关闭的数据源或目标。关闭方法用于释放对象持有的资源（如打开的文件）。
 *
 * @since 1.5
 */
public interface Closeable extends AutoCloseable {

    /**
     * 关闭此流并释放与此流关联的任何系统资源。如果流已关闭，则调用此方法不会产生任何效果。
     *
     * <p> 如 {@link AutoCloseable#close()} 中所述，关闭可能失败的情况需要特别注意。强烈建议在抛出
     * {@code IOException} 之前释放底层资源并内部 <em>标记</em> {@code Closeable} 为已关闭。
     *
     * @throws IOException 如果发生 I/O 错误
     */
    public void close() throws IOException;
}
