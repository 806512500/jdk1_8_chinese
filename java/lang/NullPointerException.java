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
 * 当应用程序尝试在需要对象的情况下使用 {@code null} 时抛出。这些情况包括：
 * <ul>
 * <li>调用 {@code null} 对象的实例方法。
 * <li>访问或修改 {@code null} 对象的字段。
 * <li>将 {@code null} 作为数组获取其长度。
 * <li>将 {@code null} 作为数组访问或修改其槽位。
 * <li>将 {@code null} 作为 {@code Throwable} 值抛出。
 * </ul>
 * <p>
 * 应用程序应抛出此类的实例以指示 {@code null} 对象的其他非法使用。
 *
 * 虚拟机可能会构造 {@code NullPointerException} 对象，就像 {@linkplain Throwable#Throwable(String,
 * Throwable, boolean, boolean) 抑制被禁用和/或堆栈跟踪不可写} 一样。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class NullPointerException extends RuntimeException {
    private static final long serialVersionUID = 5162710183389028792L;

    /**
     * 构造一个没有详细消息的 {@code NullPointerException}。
     */
    public NullPointerException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 {@code NullPointerException}。
     *
     * @param   s   详细消息。
     */
    public NullPointerException(String s) {
        super(s);
    }
}
