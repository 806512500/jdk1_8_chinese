/*
 * Copyright (c) 1994, 2008, Oracle and/or its affiliates. All rights reserved.
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
 * 当 Java 虚拟机或 <code>ClassLoader</code> 实例尝试加载类的定义（作为正常方法调用的一部分或使用 <code>new</code> 表达式创建新实例的一部分）时，如果找不到类的定义，则抛出此异常。
 * <p>
 * 在编译当前执行的类时，所查找的类定义存在，但定义已无法找到。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class NoClassDefFoundError extends LinkageError {
    private static final long serialVersionUID = 9095859863287012458L;

    /**
     * 构造一个没有详细消息的 <code>NoClassDefFoundError</code>。
     */
    public NoClassDefFoundError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 <code>NoClassDefFoundError</code>。
     *
     * @param   s   详细消息。
     */
    public NoClassDefFoundError(String s) {
        super(s);
    }
}
