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

import java.awt.Rectangle;
import java.awt.font.TextHitInfo;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;

/**
 * InputMethodRequests 定义了文本编辑组件必须处理的请求，以便与输入法一起工作。组件
 * 可以自己实现这个接口，或者使用一个单独的对象来实现。实现该接口的对象必须从
 * 组件的 getInputMethodRequests 方法返回。
 *
 * <p>
 * 文本编辑组件还必须提供一个输入法事件监听器。
 *
 * <p>
 * 该接口设计支持两种输入用户界面之一：
 * <ul>
 * <li><em>就地输入</em>，其中组合文本作为文本组件文本体的一部分显示。
 * <li><em>下方输入</em>，其中组合文本显示在插入点下方的单独组合窗口中，当文本提交时，
 *     该文本将被插入。注意，如果文本组件的文本体中选定了文本，这些文本将在提交时被替换；
 *     因此，它们不被视为输入文本的上下文的一部分。
 * </ul>
 *
 * @see java.awt.Component#getInputMethodRequests
 * @see java.awt.event.InputMethodListener
 *
 * @author JavaSoft Asia/Pacific
 * @since 1.2
 */

public interface InputMethodRequests {

    /**
     * 获取当前组合文本中指定偏移量的位置，或已提交文本中的选区位置。
     * 例如，此信息用于将候选窗口定位在组合文本附近，或将组合窗口定位在将要插入已提交文本的位置附近。
     *
     * <p>
     * 如果组件有组合文本（因为最近发送给它的 InputMethodEvent 包含组合文本），则偏移量相对于组合文本 -
     * 偏移量 0 表示组合文本中的第一个字符。返回的位置应为此字符的位置。
     *
     * <p>
     * 如果组件没有组合文本，应忽略偏移量，返回的位置应反映最后一行中选定文本的开头（按行方向）。
     * 例如，对于水平从左到右的文本（如英语），返回的是最后一行中选定文本最左边字符左边的位置。
     * 对于垂直从上到下的文本，行从右到左排列，返回的是最后一行中选定文本最左边行的顶部位置。
     *
     * <p>
     * 位置表示为 0 厚度的光标，即如果文本水平绘制，则宽度为 0；如果文本垂直绘制，则高度为 0。
     * 其他文本方向需要映射到水平或垂直方向。矩形使用绝对屏幕坐标。
     *
     * @param offset 如果有组合文本，则为组合文本中的偏移量；否则为 null
     * @return 一个表示偏移量屏幕位置的矩形
     */
    Rectangle getTextLocation(TextHitInfo offset);

    /**
     * 获取指定屏幕绝对 x 和 y 坐标在组合文本中的偏移量。例如，此信息用于处理鼠标点击和鼠标光标。
     * 偏移量相对于组合文本，因此偏移量 0 表示组合文本的开头。
     *
     * <p>
     * 如果位置在组合文本占用的区域之外，返回 null。
     *
     * @param x 屏幕上的绝对 x 坐标
     * @param y 屏幕上的绝对 y 坐标
     * @return 一个描述组合文本中偏移量的文本命中信息。
     */
    TextHitInfo getLocationOffset(int x, int y);

    /**
     * 获取文本编辑组件中包含的已提交文本中插入位置的偏移量。这是通过输入法输入的字符插入的位置。
     * 输入法使用此信息，例如，检查插入位置周围的文本。
     *
     * @return 插入位置的偏移量
     */
    int getInsertPositionOffset();

    /**
     * 获取一个迭代器，提供对文本编辑组件中包含的整个文本和属性的访问，但不包括未提交的文本。
     * 未提交（组合）的文本应忽略索引计算，并且不应通过迭代器访问。
     *
     * <p>
     * 输入法可能提供一个它感兴趣的属性列表。在这种情况下，实现者可能不需要通过迭代器提供其他属性的信息。
     * 如果列表为 null，则应通过迭代器提供所有可用的属性信息。
     *
     * @param beginIndex 第一个字符的索引
     * @param endIndex 最后一个字符之后的字符的索引
     * @param attributes 输入法感兴趣的属性列表
     * @return 提供对文本及其属性访问的迭代器
     */
    AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex,
                                                 Attribute[] attributes);

    /**
     * 获取文本编辑组件中包含的整个文本的长度，但不包括未提交（组合）的文本。
     *
     * @return 除未提交文本外的文本长度
     */
    int getCommittedTextLength();

    /**
     * 从文本编辑组件中获取最新的已提交文本并将其从组件的文本体中移除。
     * 这用于某些输入法中的“撤销提交”功能，其中已提交的文本恢复到其先前的组合状态。
     * 组合文本将通过 InputMethodEvent 发送到组件。
     *
     * <p>
     * 通常，此功能仅在文本提交后立即支持，而不是在用户对文本执行其他操作后支持。
     * 当不支持此功能时，返回 null。
     *
     * <p>
     * 输入法可能提供一个它感兴趣的属性列表。在这种情况下，实现者可能不需要通过迭代器提供其他属性的信息。
     * 如果列表为 null，则应通过迭代器提供所有可用的属性信息。
     *
     * @param attributes 输入法感兴趣的属性列表
     * @return 最新的已提交文本，或当不支持“撤销提交”功能时返回 null
     */
    AttributedCharacterIterator cancelLatestCommittedText(Attribute[] attributes);

    /**
     * 从文本编辑组件中获取当前选中的文本。这可用于各种目的。
     * 其中之一是某些输入法中的“重新转换”功能。在这种情况下，输入法通常会发送一个输入法事件
     * 以用组合文本替换选中的文本。根据输入法的能力，这可能是选中文本的原始组合文本、
     * 任何地方输入的最新组合文本，或者是从选中文本转换回的文本版本。
     *
     * <p>
     * 输入法可能提供一个它感兴趣的属性列表。在这种情况下，实现者可能不需要通过迭代器提供其他属性的信息。
     * 如果列表为 null，则应通过迭代器提供所有可用的属性信息。
     *
     * @param attributes 输入法感兴趣的属性列表
     * @return 当前选中的文本
     */
    AttributedCharacterIterator getSelectedText(Attribute[] attributes);
}
