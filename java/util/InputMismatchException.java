/*
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

/**
 * 由 <code>Scanner</code> 抛出，表示检索到的令牌与预期类型的模式不匹配，或者令牌超出预期类型的范围。
 *
 * @author  未署名
 * @see     java.util.Scanner
 * @since   1.5
 */
public
class InputMismatchException extends NoSuchElementException {
    private static final long serialVersionUID = 8811230760997066428L;

    /**
     * 构造一个 <code>InputMismatchException</code>，其错误消息字符串为 <tt>null</tt>。
     */
    public InputMismatchException() {
        super();
    }

    /**
     * 构造一个 <code>InputMismatchException</code>，保存对错误消息字符串 <tt>s</tt> 的引用，以便稍后通过 <tt>getMessage</tt> 方法检索。
     *
     * @param   s   详细消息。
     */
    public InputMismatchException(String s) {
        super(s);
    }
}
