/*
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
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

package java.rmi.activation;

/**
 * 当激活失败时，RMI 运行时在对可激活对象的远程调用期间抛出此异常。
 *
 * @author      Ann Wollrath
 * @since       1.2
 */
public class ActivateFailedException extends java.rmi.RemoteException {

    /** 表示与 Java 2 SDK v1.2 版本类的兼容性 */
    private static final long serialVersionUID = 4863550261346652506L;

    /**
     * 使用指定的详细消息构造 <code>ActivateFailedException</code>。
     *
     * @param s 详细消息
     * @since 1.2
     */
    public ActivateFailedException(String s) {
        super(s);
    }

    /**
     * 使用指定的详细消息和嵌套异常构造 <code>ActivateFailedException</code>。
     *
     * @param s 详细消息
     * @param ex 嵌套异常
     * @since 1.2
     */
    public ActivateFailedException(String s, Exception ex) {
        super(s, ex);
    }
}
