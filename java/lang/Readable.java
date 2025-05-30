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

package java.lang;

import java.io.IOException;

/**
 * 一个 <tt>Readable</tt> 是字符的来源。从 <tt>Readable</tt> 读取的字符通过
 * {@link java.nio.CharBuffer CharBuffer} 提供给调用者。
 *
 * @since 1.5
 */
public interface Readable {

    /**
     * 尝试将字符读入指定的字符缓冲区。缓冲区作为字符的存储库使用：唯一的变化是 put 操作的结果。
     * 不会对缓冲区进行翻转或重绕。
     *
     * @param cb 要读取字符到的缓冲区
     * @return 添加到缓冲区的 {@code char} 值的数量，或者 -1 表示此字符来源已结束
     * @throws IOException 如果发生 I/O 错误
     * @throws NullPointerException 如果 cb 为 null
     * @throws java.nio.ReadOnlyBufferException 如果 cb 是只读缓冲区
     */
    public int read(java.nio.CharBuffer cb) throws IOException;
}
