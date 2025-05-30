/*
 * Copyright (c) 1994, 2012, Oracle and/or its affiliates. All rights reserved.
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
 * 抛出以指示应用程序尝试将字符串转换为数值类型，但该字符串没有适当的格式。
 *
 * @author  未署名
 * @see     java.lang.Integer#parseInt(String)
 * @since   JDK1.0
 */
public
class NumberFormatException extends IllegalArgumentException {
    static final long serialVersionUID = -2848938806368998894L;

    /**
     * 构造一个没有详细消息的 <code>NumberFormatException</code>。
     */
    public NumberFormatException () {
        super();
    }

    /**
     * 构造一个具有指定详细消息的 <code>NumberFormatException</code>。
     *
     * @param   s   详细消息。
     */
    public NumberFormatException (String s) {
        super (s);
    }

    /**
     * 用于创建 <code>NumberFormatException</code> 的工厂方法，给定导致错误的指定输入。
     *
     * @param   s   导致错误的输入
     */
    static NumberFormatException forInputString(String s) {
        return new NumberFormatException("For input string: \"" + s + "\"");
    }
}
