/*
 * Copyright (c) 1995, 1998, Oracle and/or its affiliates. All rights reserved.
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
package java.awt.peer;

import java.awt.Dimension;
import java.awt.TextField;

/**
 * {@link TextField} 的对等接口。
 *
 * 对等接口仅用于移植 AWT。不建议应用程序开发人员使用这些接口，也不应直接在对等实例上调用任何对等方法。
 */
public interface TextFieldPeer extends TextComponentPeer {

    /**
     * 设置回显字符。
     *
     * @param echoChar 要设置的回显字符
     *
     * @see TextField#getEchoChar()
     */
    void setEchoChar(char echoChar);

    /**
     * 返回具有指定列数的文本字段的首选大小。
     *
     * @param columns 列数
     *
     * @return 文本字段的首选大小
     *
     * @see TextField#getPreferredSize(int)
     */
    Dimension getPreferredSize(int columns);

    /**
     * 返回具有指定列数的文本字段的最小大小。
     *
     * @param columns 列数
     *
     * @return 文本字段的最小大小
     *
     * @see TextField#getMinimumSize(int)
     */
    Dimension getMinimumSize(int columns);

}
