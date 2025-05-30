/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.TextComponent;
import java.awt.im.InputMethodRequests;

/**
 * {@link TextComponent} 的对等接口。
 *
 * 对等接口仅用于移植 AWT。不建议应用程序开发人员使用这些接口，也不应直接调用对等实例上的任何对等方法。
 */
public interface TextComponentPeer extends ComponentPeer {

    /**
     * 设置文本组件是否可编辑。
     *
     * @param editable {@code true} 表示可编辑的文本组件，{@code false} 表示不可编辑的文本组件
     *
     * @see TextComponent#setEditable(boolean)
     */
    void setEditable(boolean editable);

    /**
     * 返回文本组件的当前内容。
     *
     * @return 文本组件的当前内容
     *
     * @see TextComponent#getText()
     */
    String getText();

    /**
     * 设置文本组件的内容。
     *
     * @param text 要设置的内容
     *
     * @see TextComponent#setText(String)
     */
    void setText(String text);

    /**
     * 返回当前选择的起始索引。
     *
     * @return 当前选择的起始索引
     *
     * @see TextComponent#getSelectionStart()
     */
    int getSelectionStart();

    /**
     * 返回当前选择的结束索引。
     *
     * @return 当前选择的结束索引
     *
     * @see TextComponent#getSelectionEnd()
     */
    int getSelectionEnd();

    /**
     * 选择文本组件的一个区域。
     *
     * @param selStart 新选择的起始索引
     * @param selEnd 新选择的结束索引
     *
     * @see TextComponent#select(int, int)
     */
    void select(int selStart, int selEnd);

    /**
     * 设置文本组件的光标位置。
     *
     * @param pos 要设置的光标位置
     *
     * @see TextComponent#setCaretPosition(int)
     */
    void setCaretPosition(int pos);

    /**
     * 返回当前的光标位置。
     *
     * @return 当前的光标位置
     *
     * @see TextComponent#getCaretPosition()
     */
    int getCaretPosition();

    /**
     * 返回输入方法请求。
     *
     * @return 输入方法请求
     */
    InputMethodRequests getInputMethodRequests();
}
