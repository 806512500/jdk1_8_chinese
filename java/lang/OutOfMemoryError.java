/*
 * Copyright (c) 1994, 2011, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 当 Java 虚拟机无法分配对象，因为内存不足，且垃圾回收器无法提供更多内存时抛出。
 *
 * {@code OutOfMemoryError} 对象可能由虚拟机构造，就像 {@linkplain Throwable#Throwable(String, Throwable,
 * boolean, boolean) 禁用了抑制和/或堆栈跟踪不可写入} 一样。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public class OutOfMemoryError extends VirtualMachineError {
    private static final long serialVersionUID = 8228564086184010517L;

    /**
     * 构造一个没有详细消息的 {@code OutOfMemoryError}。
     */
    public OutOfMemoryError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 {@code OutOfMemoryError}。
     *
     * @param   s   详细消息。
     */
    public OutOfMemoryError(String s) {
        super(s);
    }
}
