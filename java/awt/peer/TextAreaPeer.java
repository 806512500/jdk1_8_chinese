/*
 * Copyright (c) 1995, 2014, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.TextArea;

/**
 * {@link TexTArea} 的对等接口。
 *
 * 对等接口仅用于移植 AWT。不建议应用程序开发人员使用这些接口，也不建议直接在对等实例上调用任何对等方法。
 */
public interface TextAreaPeer extends TextComponentPeer {

    /**
     * 在文档的指定位置插入指定的文本。
     *
     * @param text 要插入的文本
     * @param pos 插入的位置
     *
     * @see TextArea#insert(String, int)
     */
    void insert(String text, int pos);

    /**
     * 用指定的字符串替换一段文本。
     *
     * @param text 替换字符串
     * @param start 要替换的范围的开始位置
     * @param end 要替换的范围的结束位置
     *
     * @see TextArea#replaceRange(String, int, int)
     */
    void replaceRange(String text, int start, int end);

    /**
     * 返回具有指定列数和行数的文本区域的首选大小。
     *
     * @param rows 行数
     * @param columns 列数
     *
     * @return 文本区域的首选大小
     *
     * @see TextArea#getPreferredSize(int, int)
     */
    Dimension getPreferredSize(int rows, int columns);

    /**
     * 返回具有指定列数和行数的文本区域的最小大小。
     *
     * @param rows 行数
     * @param columns 列数
     *
     * @return 文本区域的最小大小
     *
     * @see TextArea#getMinimumSize(int, int)
     */
    Dimension getMinimumSize(int rows, int columns);

}
