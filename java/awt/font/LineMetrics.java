/*
 * Copyright (c) 1998, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.font;

/**
* <code>LineMetrics</code> 类允许访问布局字符在行上和多行布局所需的度量信息。
* <code>LineMetrics</code> 对象封装了与文本段落相关的测量信息。
* <p>
* 字体可以对不同范围的字符有不同的度量。 {@link java.awt.Font Font} 类的
* <code>getLineMetrics</code> 方法接受一些文本作为参数，并返回一个描述该文本中初始字符数的
* <code>LineMetrics</code> 对象，如 {@link #getNumChars} 所返回。
*/


public abstract class LineMetrics {


    /**
     * 返回此 <code>LineMetrics</code> 对象封装的文本中的字符数（<code>char</code> 值）。
     * @return 与此 <code>LineMetrics</code> 创建时关联的文本中的字符数（<code>char</code> 值）。
     */
    public abstract int getNumChars();

    /**
     * 返回文本的上升高度。上升高度是从基线到上升线的距离。
     * 上升高度通常表示文本中大写字母的高度。某些字符可能超出上升线。
     * @return 文本的上升高度。
     */
    public abstract float getAscent();

    /**
     * 返回文本的下降高度。下降高度是从基线到下降线的距离。
     * 下降高度通常表示小写字母（如 'p'）的底部距离。某些字符可能低于下降线。
     * @return 文本的下降高度。
     */
    public abstract float getDescent();

    /**
     * 返回文本的行间距。行间距是从下降线底部到下一行顶部的推荐距离。
     * @return 文本的行间距。
     */
    public abstract float getLeading();

    /**
     * 返回文本的高度。高度等于上升高度、下降高度和行间距的总和。
     * @return 文本的高度。
     */
    public abstract float getHeight();

    /**
     * 返回文本的基线索引。索引可以是
     * {@link java.awt.Font#ROMAN_BASELINE ROMAN_BASELINE}、
     * {@link java.awt.Font#CENTER_BASELINE CENTER_BASELINE}、
     * {@link java.awt.Font#HANGING_BASELINE HANGING_BASELINE}。
     * @return 文本的基线。
     */
    public abstract int getBaselineIndex();

    /**
     * 返回文本的基线偏移量，相对于文本的基线。偏移量由基线索引索引。
     * 例如，如果基线索引是 <code>CENTER_BASELINE</code>，则
     * <code>offsets[HANGING_BASELINE]</code> 通常是负数，<code>offsets[CENTER_BASELINE]</code>
     * 是零，<code>offsets[ROMAN_BASELINE]</code> 通常是正数。
     * @return 文本的基线偏移量。
     */
    public abstract float[] getBaselineOffsets();

    /**
     * 返回删除线相对于基线的位置。
     * @return 删除线的位置。
     */
    public abstract float getStrikethroughOffset();

    /**
     * 返回删除线的厚度。
     * @return 删除线的厚度。
     */
    public abstract float getStrikethroughThickness();

    /**
     * 返回下划线相对于基线的位置。
     * @return 下划线的位置。
     */
    public abstract float getUnderlineOffset();

    /**
     * 返回下划线的厚度。
     * @return 下划线的厚度。
     */
    public abstract float getUnderlineThickness();
}
