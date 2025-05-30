/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 表示在输入过程中意外地遇到了文件结束或流结束。
 * <p>
 * 此异常主要由数据输入流用于信号流结束。请注意，许多其他输入操作在流结束时返回一个特殊值，而不是抛出异常。
 *
 * @author  Frank Yellin
 * @see     java.io.DataInputStream
 * @see     java.io.IOException
 * @since   JDK1.0
 */
public
class EOFException extends IOException {
    private static final long serialVersionUID = 6433858223774886977L;

    /**
     * 构造一个 <code>EOFException</code>，其错误详细信息消息为 <code>null</code>。
     */
    public EOFException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 <code>EOFException</code>。字符串 <code>s</code> 可以通过
     * <code>{@link java.lang.Throwable#getMessage}</code> 方法从类
     * <code>java.lang.Throwable</code> 中检索。
     *
     * @param   s   详细信息消息。
     */
    public EOFException(String s) {
        super(s);
    }
}
