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
 * 如果应用程序尝试调用类的指定方法（无论是静态的还是实例的），而该类不再有该方法的定义，则抛出此异常。
 * <p>
 * 通常，此错误会被编译器捕获；只有在类的定义发生了不兼容的更改时，此错误才会在运行时发生。
 *
 * @author  未署名
 * @since   JDK1.0
 */
public
class NoSuchMethodError extends IncompatibleClassChangeError {
    private static final long serialVersionUID = -3765521442372831335L;

    /**
     * 构造一个没有详细信息的 <code>NoSuchMethodError</code>。
     */
    public NoSuchMethodError() {
        super();
    }

    /**
     * 使用指定的详细信息消息构造一个 <code>NoSuchMethodError</code>。
     *
     * @param   s   详细信息消息。
     */
    public NoSuchMethodError(String s) {
        super(s);
    }
}
