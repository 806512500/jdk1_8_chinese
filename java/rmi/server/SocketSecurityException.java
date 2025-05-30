/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.server;

/**
 * 一个已废弃的 {@link ExportException} 子类。
 *
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @deprecated 此类已废弃。请使用 {@link ExportException} 代替。
 */
@Deprecated
public class SocketSecurityException extends ExportException {

    /* 表示与 JDK 1.1.x 版本类的兼容性 */
    private static final long serialVersionUID = -7622072999407781979L;

    /**
     * 构造一个带有指定详细消息的 <code>SocketSecurityException</code>。
     *
     * @param s 详细消息。
     * @since JDK1.1
     */
    public SocketSecurityException(String s) {
        super(s);
    }

    /**
     * 构造一个带有指定详细消息和嵌套异常的 <code>SocketSecurityException</code>。
     *
     * @param s 详细消息。
     * @param ex 嵌套异常
     * @since JDK1.1
     */
    public SocketSecurityException(String s, Exception ex) {
        super(s, ex);
    }

}
