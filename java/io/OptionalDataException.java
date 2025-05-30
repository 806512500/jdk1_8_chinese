/*
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
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
 * 表示由于流中存在未读的原始数据或属于序列化对象的数据结束而导致的对象读取操作失败的异常。此异常可能在以下两种情况下抛出：
 *
 * <ul>
 *   <li>尝试读取对象时，流中的下一个元素是原始数据。在这种情况下，OptionalDataException 的 length 字段设置为可以从流中立即读取的原始数据的字节数，eof 字段设置为 false。
 *
 *   <li>尝试读取超过由类定义的 readObject 或 readExternal 方法可消费的数据的末尾。在这种情况下，OptionalDataException 的 eof 字段设置为 true，length 字段设置为 0。
 * </ul>
 *
 * @author  未署名
 * @since   JDK1.1
 */
public class OptionalDataException extends ObjectStreamException {

    private static final long serialVersionUID = -8011121865681257820L;

    /*
     * 创建一个带有长度的 <code>OptionalDataException</code>。
     */
    OptionalDataException(int len) {
        eof = false;
        length = len;
    }

    /*
     * 创建一个表示没有更多原始数据可用的 <code>OptionalDataException</code>。
     */
    OptionalDataException(boolean end) {
        length = 0;
        eof = end;
    }

    /**
     * 当前缓冲区中可读取的原始数据的字节数。
     *
     * @serial
     */
    public int length;

    /**
     * 如果流的缓冲部分中没有更多数据，则为 true。
     *
     * @serial
     */
    public boolean eof;
}
