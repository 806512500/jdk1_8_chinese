/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.dnd;

/**
 * 此异常由 java.awt.dnd 包中的各种方法抛出。
 * 通常在目标无法执行当前请求的操作时抛出此异常，因为底层的 DnD 系统未处于适当状态。
 *
 * @since 1.2
 */

public class InvalidDnDOperationException extends IllegalStateException {

    private static final long serialVersionUID = -6062568741193956678L;

    static private String dft_msg = "DnD 系统未处于适当状态，因此无法执行请求的操作";

    /**
     * 创建默认异常
     */

    public InvalidDnDOperationException() { super(dft_msg); }

    /**
     * 使用自己的描述性消息创建异常
     * <P>
     * @param msg 详细消息
     */

    public InvalidDnDOperationException(String msg) { super(msg); }

}
