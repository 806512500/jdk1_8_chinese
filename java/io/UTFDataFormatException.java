/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * 表示在数据输入流或实现数据输入接口的任何类中读取到格式不正确的
 * <a href="DataInput.html#modified-utf-8">修改后的 UTF-8</a>
 * 字符串时发出的信号。
 * 有关修改后的 UTF-8 字符串的读写格式，请参见
 * <a href="DataInput.html#modified-utf-8"><code>DataInput</code></a>
 * 类的描述。
 *
 * @author  Frank Yellin
 * @see     java.io.DataInput
 * @see     java.io.DataInputStream#readUTF(java.io.DataInput)
 * @see     java.io.IOException
 * @since   JDK1.0
 */
public
class UTFDataFormatException extends IOException {
    private static final long serialVersionUID = 420743449228280612L;

    /**
     * 构造一个 <code>UTFDataFormatException</code>，其错误详细信息消息为
     * <code>null</code>。
     */
    public UTFDataFormatException() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 <code>UTFDataFormatException</code>。
     * 字符串 <code>s</code> 可以通过
     * <code>{@link java.lang.Throwable#getMessage}</code>
     * 方法从类 <code>java.lang.Throwable</code> 中稍后检索。
     *
     * @param   s   详细信息消息。
     */
    public UTFDataFormatException(String s) {
        super(s);
    }
}
