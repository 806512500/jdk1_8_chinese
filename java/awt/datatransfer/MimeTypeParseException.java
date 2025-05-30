/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.datatransfer;


/**
 * 一个用于封装 MimeType 解析相关异常的类
 *
 * @serial exclude
 * @since 1.3
 */
public class MimeTypeParseException extends Exception {

    // 使用 JDK 1.2.2 的 serialVersionUID 以确保互操作性
    private static final long serialVersionUID = -5604407764691570741L;

    /**
     * 构造一个没有指定详细消息的 MimeTypeParseException。
     */
    public MimeTypeParseException() {
        super();
    }

    /**
     * 构造一个具有指定详细消息的 MimeTypeParseException。
     *
     * @param   s   详细消息。
     */
    public MimeTypeParseException(String s) {
        super(s);
    }
} // class MimeTypeParseException
