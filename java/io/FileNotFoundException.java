/*
 * Copyright (c) 1994, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * 表示尝试打开指定路径名的文件失败。
 *
 * <p> 当文件不存在时，此异常将由 {@link FileInputStream}、{@link
 * FileOutputStream} 和 {@link RandomAccessFile} 构造函数抛出。当文件存在但出于某种原因无法访问时，例如尝试打开只读文件进行写入时，这些构造函数也会抛出此异常。
 *
 * @author  未署名
 * @since   JDK1.0
 */

public class FileNotFoundException extends IOException {
    private static final long serialVersionUID = -897856973823710492L;

    /**
     * 构造一个 <code>FileNotFoundException</code>，其错误详细信息消息为
     * <code>null</code>。
     */
    public FileNotFoundException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 <code>FileNotFoundException</code>。字符串 <code>s</code> 可以通过
     * <code>{@link java.lang.Throwable#getMessage}</code>
     * 方法从类 <code>java.lang.Throwable</code> 中稍后检索。
     *
     * @param   s   详细信息消息。
     */
    public FileNotFoundException(String s) {
        super(s);
    }

    /**
     * 使用给定的路径名字符串和原因字符串构造一个 <code>FileNotFoundException</code>，详细信息消息由这两部分组成。如果 <code>reason</code> 参数为 <code>null</code>，则将省略该部分。此私有构造函数仅由本机 I/O 方法调用。
     *
     * @since 1.2
     */
    private FileNotFoundException(String path, String reason) {
        super(path + ((reason == null)
                      ? ""
                      : " (" + reason + ")"));
    }

}
