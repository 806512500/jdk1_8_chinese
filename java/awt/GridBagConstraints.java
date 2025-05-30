
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
package java.awt;

/**
 * <code>GridBagConstraints</code> 类指定使用 <code>GridBagLayout</code> 类布局的组件的约束。
 *
 * @author Doug Stein
 * @author Bill Spitzak（原始 NeWS &amp; OLIT 实现）
 * @see java.awt.GridBagLayout
 * @since JDK1.0
 */
public class GridBagConstraints implements Cloneable, java.io.Serializable {

    /**
     * 指定该组件是其列或行中的倒数第二个组件（<code>gridwidth</code>，<code>gridheight</code>），
     * 或者该组件应放置在上一个添加的组件旁边（<code>gridx</code>，<code>gridy</code>）。
     * @see      java.awt.GridBagConstraints#gridwidth
     * @see      java.awt.GridBagConstraints#gridheight
     * @see      java.awt.GridBagConstraints#gridx
     * @see      java.awt.GridBagConstraints#gridy
     */
    public static final int RELATIVE = -1;

    /**
     * 指定该组件是其列或行中的最后一个组件。
     */
    public static final int REMAINDER = 0;

    /**
     * 不调整组件的大小。
     */
    public static final int NONE = 0;

    /**
     * 水平和垂直调整组件的大小。
     */
    public static final int BOTH = 1;

    /**
     * 水平调整组件的大小但不垂直调整。
     */
    public static final int HORIZONTAL = 2;

    /**
     * 垂直调整组件的大小但不水平调整。
     */
    public static final int VERTICAL = 3;

    /**
     * 将组件放在其显示区域的中心。
     */
    public static final int CENTER = 10;

    /**
     * 将组件放在其显示区域的顶部，水平居中。
     */
    public static final int NORTH = 11;

    /**
     * 将组件放在其显示区域的右上角。
     */
    public static final int NORTHEAST = 12;

    /**
     * 将组件放在其显示区域的右侧，垂直居中。
     */
    public static final int EAST = 13;

    /**
     * 将组件放在其显示区域的右下角。
     */
    public static final int SOUTHEAST = 14;

    /**
     * 将组件放在其显示区域的底部，水平居中。
     */
    public static final int SOUTH = 15;

    /**
     * 将组件放在其显示区域的左下角。
     */
    public static final int SOUTHWEST = 16;

    /**
     * 将组件放在其显示区域的左侧，垂直居中。
     */
    public static final int WEST = 17;

    /**
     * 将组件放在其显示区域的左上角。
     */
    public static final int NORTHWEST = 18;

    /**
     * 将组件放在其显示区域的当前 <code>ComponentOrientation</code> 关联的页面起始边缘的中心。
     * 对于水平方向，等于 NORTH。
     */
    public static final int PAGE_START = 19;

    /**
     * 将组件放在其显示区域的当前 <code>ComponentOrientation</code> 关联的页面结束边缘的中心。
     * 对于水平方向，等于 SOUTH。
     */
    public static final int PAGE_END = 20;

    /**
     * 将组件放在其显示区域的当前 <code>ComponentOrientation</code> 关联的文本行通常开始的位置的边缘中心。
     * 对于水平、从左到右的方向，等于 WEST；对于水平、从右到左的方向，等于 EAST。
     */
    public static final int LINE_START = 21;

    /**
     * 将组件放在其显示区域的当前 <code>ComponentOrientation</code> 关联的文本行通常结束的位置的边缘中心。
     * 对于水平、从左到右的方向，等于 EAST；对于水平、从右到左的方向，等于 WEST。
     */
    public static final int LINE_END = 22;

    /**
     * 将组件放在其显示区域的当前 <code>ComponentOrientation</code> 关联的页面上第一行文本通常开始的位置的角落。
     * 对于水平、从左到右的方向，等于 NORTHWEST；对于水平、从右到左的方向，等于 NORTHEAST。
     */
    public static final int FIRST_LINE_START = 23;

    /**
     * 将组件放在其显示区域的当前 <code>ComponentOrientation</code> 关联的页面上第一行文本通常结束的位置的角落。
     * 对于水平、从左到右的方向，等于 NORTHEAST；对于水平、从右到左的方向，等于 NORTHWEST。
     */
    public static final int FIRST_LINE_END = 24;

    /**
     * 将组件放在其显示区域的当前 <code>ComponentOrientation</code> 关联的页面上最后一行文本通常开始的位置的角落。
     * 对于水平、从左到右的方向，等于 SOUTHWEST；对于水平、从右到左的方向，等于 SOUTHEAST。
     */
    public static final int LAST_LINE_START = 25;

    /**
     * 将组件放在其显示区域的当前 <code>ComponentOrientation</code> 关联的页面上最后一行文本通常结束的位置的角落。
     * 对于水平、从左到右的方向，等于 SOUTHEAST；对于水平、从右到左的方向，等于 SOUTHWEST。
     */
    public static final int LAST_LINE_END = 26;

    /**
     * <code>anchor</code> 字段的可能值。指定组件应水平居中并垂直对齐到当前行的基线。
     * 如果组件没有基线，它将垂直居中。
     *
     * @since 1.6
     */
    public static final int BASELINE = 0x100;

    /**
     * <code>anchor</code> 字段的可能值。指定组件应水平放置在前缘。对于从左到右方向的组件，前缘是左缘。
     * 垂直上，组件对齐到当前行的基线。如果组件没有基线，它将垂直居中。
     *
     * @since 1.6
     */
    public static final int BASELINE_LEADING = 0x200;

    /**
     * <code>anchor</code> 字段的可能值。指定组件应水平放置在后缘。对于从左到右方向的组件，后缘是右缘。
     * 垂直上，组件对齐到当前行的基线。如果组件没有基线，它将垂直居中。
     *
     * @since 1.6
     */
    public static final int BASELINE_TRAILING = 0x300;

    /**
     * <code>anchor</code> 字段的可能值。指定组件应水平居中。垂直上，组件的位置使其底边接触起始行的基线。
     * 如果起始行没有基线，它将垂直居中。
     *
     * @since 1.6
     */
    public static final int ABOVE_BASELINE = 0x400;

    /**
     * <code>anchor</code> 字段的可能值。指定组件应水平放置在前缘。对于从左到右方向的组件，前缘是左缘。
     * 垂直上，组件的位置使其底边接触起始行的基线。如果起始行没有基线，它将垂直居中。
     *
     * @since 1.6
     */
    public static final int ABOVE_BASELINE_LEADING = 0x500;

    /**
     * <code>anchor</code> 字段的可能值。指定组件应水平放置在后缘。对于从左到右方向的组件，后缘是右缘。
     * 垂直上，组件的位置使其底边接触起始行的基线。如果起始行没有基线，它将垂直居中。
     *
     * @since 1.6
     */
    public static final int ABOVE_BASELINE_TRAILING = 0x600;

    /**
     * <code>anchor</code> 字段的可能值。指定组件应水平居中。垂直上，组件的位置使其顶边接触起始行的基线。
     * 如果起始行没有基线，它将垂直居中。
     *
     * @since 1.6
     */
    public static final int BELOW_BASELINE = 0x700;

    /**
     * <code>anchor</code> 字段的可能值。指定组件应水平放置在前缘。对于从左到右方向的组件，前缘是左缘。
     * 垂直上，组件的位置使其顶边接触起始行的基线。如果起始行没有基线，它将垂直居中。
     *
     * @since 1.6
     */
    public static final int BELOW_BASELINE_LEADING = 0x800;

    /**
     * <code>anchor</code> 字段的可能值。指定组件应水平放置在后缘。对于从左到右方向的组件，后缘是右缘。
     * 垂直上，组件的位置使其顶边接触起始行的基线。如果起始行没有基线，它将垂直居中。
     *
     * @since 1.6
     */
    public static final int BELOW_BASELINE_TRAILING = 0x900;

    /**
     * 指定组件显示区域的前缘所在的单元格，其中行中的第一个单元格 <code>gridx=0</code>。
     * 组件显示区域的前缘是其在水平、从左到右的容器中的左缘，或在水平、从右到左的容器中的右缘。
     * 值 <code>RELATIVE</code> 指定组件应放置在添加到容器中的前一个组件的紧邻位置。
     * <p>
     * 默认值是 <code>RELATIVE</code>。
     * <code>gridx</code> 应为非负值。
     * @serial
     * @see #clone()
     * @see java.awt.GridBagConstraints#gridy
     * @see java.awt.ComponentOrientation
     */
    public int gridx;

    /**
     * 指定组件显示区域的顶部所在的单元格，其中最顶部的单元格 <code>gridy=0</code>。值
     * <code>RELATIVE</code> 指定组件应放置在添加到容器中的前一个组件的正下方。
     * <p>
     * 默认值是 <code>RELATIVE</code>。
     * <code>gridy</code> 应为非负值。
     * @serial
     * @see #clone()
     * @see java.awt.GridBagConstraints#gridx
     */
    public int gridy;

    /**
     * 指定组件显示区域在行中的单元格数。
     * <p>
     * 使用 <code>REMAINDER</code> 指定组件的显示区域从 <code>gridx</code> 到行中的最后一个单元格。
     * 使用 <code>RELATIVE</code> 指定组件的显示区域从 <code>gridx</code> 到行中的倒数第二个单元格。
     * <p>
     * <code>gridwidth</code> 应为非负值，默认值为 1。
     * @serial
     * @see #clone()
     * @see java.awt.GridBagConstraints#gridheight
     */
    public int gridwidth;

    /**
     * 指定组件显示区域在列中的单元格数。
     * <p>
     * 使用 <code>REMAINDER</code> 指定组件的显示区域从 <code>gridy</code> 到列中的最后一个单元格。
     * 使用 <code>RELATIVE</code> 指定组件的显示区域从 <code>gridy</code> 到列中的倒数第二个单元格。
     * <p>
     * <code>gridheight</code> 应为非负值，默认值为 1。
     * @serial
     * @see #clone()
     * @see java.awt.GridBagConstraints#gridwidth
     */
    public int gridheight;

    /**
     * 指定如何分配额外的水平空间。
     * <p>
     * 网格袋布局管理器计算列的权重为该列中所有组件的最大 <code>weightx</code>。如果计算出的布局在水平方向上小于需要填充的区域，
     * 额外的空间将按比例分配给每个列。权重为零的列不会获得额外的空间。
     * <p>
     * 如果所有权重都为零，所有额外的空间将出现在单元格网格与左、右边缘之间。
     * <p>
     * 该字段的默认值为 <code>0</code>。
     * <code>weightx</code> 应为非负值。
     * @serial
     * @see #clone()
     * @see java.awt.GridBagConstraints#weighty
     */
    public double weightx;

    /**
     * 指定如何分配额外的垂直空间。
     * <p>
     * 网格袋布局管理器计算行的权重为该行中所有组件的最大 <code>weighty</code>。如果计算出的布局在垂直方向上小于需要填充的区域，
     * 额外的空间将按比例分配给每个行。权重为零的行不会获得额外的空间。
     * <p>
     * 如果所有权重都为零，所有额外的空间将出现在单元格网格与上、下边缘之间。
     * <p>
     * 该字段的默认值为 <code>0</code>。
     * <code>weighty</code> 应为非负值。
     * @serial
     * @see #clone()
     * @see java.awt.GridBagConstraints#weightx
     */
    public double weighty;


                /**
     * 当组件小于其显示区域时，此字段用于确定在显示区域内放置组件的位置。
     * <p> 有三种可能的值：方向相关、基线相关和绝对值。方向相关值根据容器的组件方向属性解释，基线相关值根据基线解释，而绝对值则不解释。绝对值为：
     * <code>CENTER</code>, <code>NORTH</code>, <code>NORTHEAST</code>,
     * <code>EAST</code>, <code>SOUTHEAST</code>, <code>SOUTH</code>,
     * <code>SOUTHWEST</code>, <code>WEST</code>, 和 <code>NORTHWEST</code>。
     * 方向相关值为： <code>PAGE_START</code>,
     * <code>PAGE_END</code>,
     * <code>LINE_START</code>, <code>LINE_END</code>,
     * <code>FIRST_LINE_START</code>, <code>FIRST_LINE_END</code>,
     * <code>LAST_LINE_START</code> 和 <code>LAST_LINE_END</code>。基线相关值为：
     * <code>BASELINE</code>, <code>BASELINE_LEADING</code>,
     * <code>BASELINE_TRAILING</code>,
     * <code>ABOVE_BASELINE</code>, <code>ABOVE_BASELINE_LEADING</code>,
     * <code>ABOVE_BASELINE_TRAILING</code>,
     * <code>BELOW_BASELINE</code>, <code>BELOW_BASELINE_LEADING</code>,
     * 和 <code>BELOW_BASELINE_TRAILING</code>。
     * 默认值为 <code>CENTER</code>。
     * @serial
     * @see #clone()
     * @see java.awt.ComponentOrientation
     */
    public int anchor;

    /**
     * 当组件的显示区域大于组件的请求大小时，此字段用于确定是否调整组件大小，以及如何调整。
     * <p>
     * <code>fill</code> 的有效值如下：
     *
     * <ul>
     * <li>
     * <code>NONE</code>: 不调整组件大小。
     * <li>
     * <code>HORIZONTAL</code>: 使组件在水平方向上填充其显示区域，但不改变其高度。
     * <li>
     * <code>VERTICAL</code>: 使组件在垂直方向上填充其显示区域，但不改变其宽度。
     * <li>
     * <code>BOTH</code>: 使组件完全填充其显示区域。
     * </ul>
     * <p>
     * 默认值为 <code>NONE</code>。
     * @serial
     * @see #clone()
     */
    public int fill;

    /**
     * 此字段指定组件的外部填充，即组件与显示区域边缘之间的最小间距。
     * <p>
     * 默认值为 <code>new Insets(0, 0, 0, 0)</code>。
     * @serial
     * @see #clone()
     */
    public Insets insets;

    /**
     * 此字段指定组件的内部填充，即添加到组件最小宽度的空间量。组件的宽度至少为其最小宽度加上
     * <code>ipadx</code> 像素。
     * <p>
     * 默认值为 <code>0</code>。
     * @serial
     * @see #clone()
     * @see java.awt.GridBagConstraints#ipady
     */
    public int ipadx;

    /**
     * 此字段指定组件的内部填充，即添加到组件最小高度的空间量。组件的高度至少为其最小高度加上
     * <code>ipady</code> 像素。
     * <p>
     * 默认值为 0。
     * @serial
     * @see #clone()
     * @see java.awt.GridBagConstraints#ipadx
     */
    public int ipady;

    /**
     * 临时占位符，用于组件的 x 坐标。
     * @serial
     */
    int tempX;
    /**
     * 临时占位符，用于组件的 y 坐标。
     * @serial
     */
    int tempY;
    /**
     * 临时占位符，用于组件的宽度。
     * @serial
     */
    int tempWidth;
    /**
     * 临时占位符，用于组件的高度。
     * @serial
     */
    int tempHeight;
    /**
     * 组件的最小宽度。用于计算 <code>ipady</code>，默认值为 0。
     * @serial
     * @see #ipady
     */
    int minWidth;
    /**
     * 组件的最小高度。用于计算 <code>ipadx</code>，默认值为 0。
     * @serial
     * @see #ipadx
     */
    int minHeight;

    // 以下字段仅在 anchor 为
    // BASELINE, BASELINE_LEADING 或 BASELINE_TRAILING 时使用。
    // ascent 和 descent 包括 insets 和 ipady 值。
    transient int ascent;
    transient int descent;
    transient Component.BaselineResizeBehavior baselineResizeBehavior;
    // 以下两个字段仅在 baseline 类型为
    // CENTER_OFFSET 时使用。
    // centerPadding 为 0 或 1，表示在计算基线位置时高度是否需要增加 1。
    transient int centerPadding;
    // 基线相对于组件中心的位置。
    transient int centerOffset;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -1000070633030801713L;

    /**
     * 创建一个 <code>GridBagConstraint</code> 对象，其所有字段均设置为其默认值。
     */
    public GridBagConstraints () {
        gridx = RELATIVE;
        gridy = RELATIVE;
        gridwidth = 1;
        gridheight = 1;

        weightx = 0;
        weighty = 0;
        anchor = CENTER;
        fill = NONE;

        insets = new Insets(0, 0, 0, 0);
        ipadx = 0;
        ipady = 0;
    }

    /**
     * 创建一个 <code>GridBagConstraints</code> 对象，其所有字段均设置为传入的参数值。
     *
     * 注意：由于使用此构造函数会降低源代码的可读性，因此此构造函数应仅由自动源代码生成工具使用。
     *
     * @param gridx     初始 gridx 值。
     * @param gridy     初始 gridy 值。
     * @param gridwidth 初始 gridwidth 值。
     * @param gridheight        初始 gridheight 值。
     * @param weightx   初始 weightx 值。
     * @param weighty   初始 weighty 值。
     * @param anchor    初始 anchor 值。
     * @param fill      初始 fill 值。
     * @param insets    初始 insets 值。
     * @param ipadx     初始 ipadx 值。
     * @param ipady     初始 ipady 值。
     *
     * @see java.awt.GridBagConstraints#gridx
     * @see java.awt.GridBagConstraints#gridy
     * @see java.awt.GridBagConstraints#gridwidth
     * @see java.awt.GridBagConstraints#gridheight
     * @see java.awt.GridBagConstraints#weightx
     * @see java.awt.GridBagConstraints#weighty
     * @see java.awt.GridBagConstraints#anchor
     * @see java.awt.GridBagConstraints#fill
     * @see java.awt.GridBagConstraints#insets
     * @see java.awt.GridBagConstraints#ipadx
     * @see java.awt.GridBagConstraints#ipady
     *
     * @since 1.2
     */
    public GridBagConstraints(int gridx, int gridy,
                              int gridwidth, int gridheight,
                              double weightx, double weighty,
                              int anchor, int fill,
                              Insets insets, int ipadx, int ipady) {
        this.gridx = gridx;
        this.gridy = gridy;
        this.gridwidth = gridwidth;
        this.gridheight = gridheight;
        this.fill = fill;
        this.ipadx = ipadx;
        this.ipady = ipady;
        this.insets = insets;
        this.anchor  = anchor;
        this.weightx = weightx;
        this.weighty = weighty;
    }

    /**
     * 创建此网格袋约束的副本。
     * @return     此网格袋约束的副本
     */
    public Object clone () {
        try {
            GridBagConstraints c = (GridBagConstraints)super.clone();
            c.insets = (Insets)insets.clone();
            return c;
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们实现了 Cloneable
            throw new InternalError(e);
        }
    }

    boolean isVerticallyResizable() {
        return (fill == BOTH || fill == VERTICAL);
    }
}
