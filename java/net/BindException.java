/*
 * Copyright (c) 1996, 2008, Oracle and/or its affiliates. All rights reserved.
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

package java.net;

/**
 * 表示在尝试将套接字绑定到本地地址和端口时发生错误。通常，端口已被使用，或请求的本地地址无法分配。
 *
 * @since   JDK1.1
 */

public class BindException extends SocketException {
    private static final long serialVersionUID = -5945005768251722951L;

    /**
     * 构造一个新的 BindException，其中包含指定的详细信息，说明绑定错误的原因。
     * 详细信息是一个描述此错误的具体原因的字符串。
     * @param msg 详细信息
     */
    public BindException(String msg) {
        super(msg);
    }

    /**
     * 构造一个新的 BindException，不包含详细信息。
     */
    public BindException() {}
}
