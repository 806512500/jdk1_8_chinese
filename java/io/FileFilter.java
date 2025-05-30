/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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


/**
 * 抽象路径名的过滤器。
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
