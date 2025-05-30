/*
 * Copyright (c) 1995, 2007, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.peer;

import java.awt.FileDialog;
import java.io.FilenameFilter;

/**
 * {@link FileDialog} 的对等接口。
 *
 * 对等接口仅用于移植 AWT。不建议应用程序开发人员使用这些接口，也不应直接调用对等实例上的任何对等方法。
 */
public interface FileDialogPeer extends DialogPeer {

    /**
     * 为该文件对话框设置选定的文件。
     *
     * @param file 要设置为选定文件的文件，或 {@code null} 表示没有选定文件
     *
     * @see FileDialog#setFile(String)
     */
    void setFile(String file);

    /**
     * 为该文件对话框设置当前目录。
     *
     * @param dir 要设置的目录
     *
     * @see FileDialog#setDirectory(String)
     */
    void setDirectory(String dir);

    /**
     * 为过滤显示的文件设置文件名过滤器。
     *
     * @param filter 要设置的过滤器
     *
     * @see FileDialog#setFilenameFilter(FilenameFilter)
     */
    void setFilenameFilter(FilenameFilter filter);
}
