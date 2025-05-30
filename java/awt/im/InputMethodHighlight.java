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

package java.awt.im;

import java.awt.font.TextAttribute;
import java.util.Map;

/**
* 一个 InputMethodHighlight 用于描述正在组合的文本的高亮属性。
* 描述可以分为两个层次：
* 在抽象层次上，它指定了转换状态和文本是否被选中；在具体层次上，它指定了用于渲染高亮的样式属性。
* 一个 InputMethodHighlight 必须提供抽象层次的描述；它可以提供或不提供具体层次的描述。
* 如果没有提供具体样式，渲染器应使用 {@link java.awt.Toolkit#mapInputMethodHighlight} 映射到具体样式。
* <p>
* 抽象描述由三个字段组成： <code>selected</code>、<code>state</code> 和 <code>variation</code>。
* <code>selected</code> 表示文本范围是否是输入方法当前正在处理的范围，例如，当前在菜单中显示转换候选的段落。
* <code>state</code> 表示转换状态。状态值由输入方法框架定义，并应在所有从抽象到具体样式的映射中区分开来。目前定义的状态值有原始（未转换）和已转换。
* 这些状态值建议在文本组合的主要转换步骤之前和之后使用，例如，在 kana->kanji 或 pinyin->hanzi 转换之前和之后。
* <code>variation</code> 字段允许输入方法表达关于转换结果的附加信息。
* <p>
*
* InputMethodHighlight 实例通常作为 AttributedCharacterIterator 为 INPUT_METHOD_HIGHLIGHT 属性返回的属性值使用。它们可以被包装到 {@link java.text.Annotation Annotation} 实例中，以指示单独的文本段落。
*
* @see java.text.AttributedCharacterIterator
* @since 1.2
*/

public class InputMethodHighlight {

    /**
     * 原始文本状态的常量。
     */
    public final static int RAW_TEXT = 0;

    /**
     * 已转换文本状态的常量。
     */
    public final static int CONVERTED_TEXT = 1;


    /**
     * 未选中原始文本的默认高亮。
     */
    public final static InputMethodHighlight UNSELECTED_RAW_TEXT_HIGHLIGHT =
        new InputMethodHighlight(false, RAW_TEXT);

    /**
     * 选中原始文本的默认高亮。
     */
    public final static InputMethodHighlight SELECTED_RAW_TEXT_HIGHLIGHT =
        new InputMethodHighlight(true, RAW_TEXT);

    /**
     * 未选中已转换文本的默认高亮。
     */
    public final static InputMethodHighlight UNSELECTED_CONVERTED_TEXT_HIGHLIGHT =
        new InputMethodHighlight(false, CONVERTED_TEXT);

    /**
     * 选中已转换文本的默认高亮。
     */
    public final static InputMethodHighlight SELECTED_CONVERTED_TEXT_HIGHLIGHT =
        new InputMethodHighlight(true, CONVERTED_TEXT);


    /**
     * 构造一个输入方法高亮记录。
     * 变体设置为 0，样式设置为 null。
     * @param selected 文本范围是否被选中
     * @param state 文本范围的转换状态 - RAW_TEXT 或 CONVERTED_TEXT
     * @see InputMethodHighlight#RAW_TEXT
     * @see InputMethodHighlight#CONVERTED_TEXT
     * @exception IllegalArgumentException 如果给定的状态不是 RAW_TEXT 或 CONVERTED_TEXT
     */
    public InputMethodHighlight(boolean selected, int state) {
        this(selected, state, 0, null);
    }

    /**
     * 构造一个输入方法高亮记录。
     * 样式设置为 null。
     * @param selected 文本范围是否被选中
     * @param state 文本范围的转换状态 - RAW_TEXT 或 CONVERTED_TEXT
     * @param variation 文本范围的样式变体
     * @see InputMethodHighlight#RAW_TEXT
     * @see InputMethodHighlight#CONVERTED_TEXT
     * @exception IllegalArgumentException 如果给定的状态不是 RAW_TEXT 或 CONVERTED_TEXT
     */
    public InputMethodHighlight(boolean selected, int state, int variation) {
        this(selected, state, variation, null);
    }

    /**
     * 构造一个输入方法高亮记录。
     * 提供的样式属性映射必须是不可修改的。
     * @param selected 文本范围是否被选中
     * @param state 文本范围的转换状态 - RAW_TEXT 或 CONVERTED_TEXT
     * @param variation 文本范围的变体
     * @param style 文本范围的渲染样式属性，或 null
     * @see InputMethodHighlight#RAW_TEXT
     * @see InputMethodHighlight#CONVERTED_TEXT
     * @exception IllegalArgumentException 如果给定的状态不是 RAW_TEXT 或 CONVERTED_TEXT
     * @since 1.3
     */
    public InputMethodHighlight(boolean selected, int state, int variation,
                                Map<TextAttribute,?> style)
    {
        this.selected = selected;
        if (!(state == RAW_TEXT || state == CONVERTED_TEXT)) {
            throw new IllegalArgumentException("未知的输入方法高亮状态");
        }
        this.state = state;
        this.variation = variation;
        this.style = style;
    }

    /**
     * 返回文本范围是否被选中。
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * 返回文本范围的转换状态。
     * @return 文本范围的转换状态 - RAW_TEXT 或 CONVERTED_TEXT。
     * @see InputMethodHighlight#RAW_TEXT
     * @see InputMethodHighlight#CONVERTED_TEXT
     */
    public int getState() {
        return state;
    }

    /**
     * 返回文本范围的变体。
     */
    public int getVariation() {
        return variation;
    }

    /**
     * 返回文本范围的渲染样式属性，或 null。
     * @since 1.3
     */
    public Map<TextAttribute,?> getStyle() {
        return style;
    }

    private boolean selected;
    private int state;
    private int variation;
    private Map<TextAttribute, ?> style;

};
