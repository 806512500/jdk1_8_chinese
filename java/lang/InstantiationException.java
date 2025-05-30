/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * 当应用程序尝试使用 {@code Class} 类中的 {@code newInstance} 方法创建一个类的实例，
 * 但指定的类对象无法实例化时抛出。实例化可能因多种原因失败，包括但不限于：
 *
 * <ul>
 * <li> 类对象表示抽象类、接口、数组类、基本类型或 {@code void}
 * <li> 类没有无参构造函数
 *</ul>
 *
 * @author  未署名
 * @see     java.lang.Class#newInstance()
 * @since   JDK1.0
 */
public
class InstantiationException extends ReflectiveOperationException {
    private static final long serialVersionUID = -8441929162975509110L;

    /**
     * 构造一个没有详细消息的 {@code InstantiationException}。
     */
    public InstantiationException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 {@code InstantiationException}。
     *
     * @param   s   详细消息。
     */
    public InstantiationException(String s) {
        super(s);
    }
}
