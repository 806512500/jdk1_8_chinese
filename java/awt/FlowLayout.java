
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

import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * 流布局将组件按方向排列，类似于段落中的文本行。流方向由容器的 <code>componentOrientation</code>
 * 属性确定，可以是以下两个值之一：
 * <ul>
 * <li><code>ComponentOrientation.LEFT_TO_RIGHT</code>
 * <li><code>ComponentOrientation.RIGHT_TO_LEFT</code>
 * </ul>
 * 流布局通常用于在面板中排列按钮。它将按钮水平排列，直到同一行上无法再容纳更多按钮。
 * 行对齐方式由 <code>align</code> 属性确定。可能的值包括：
 * <ul>
 * <li>{@link #LEFT LEFT}
 * <li>{@link #RIGHT RIGHT}
 * <li>{@link #CENTER CENTER}
 * <li>{@link #LEADING LEADING}
 * <li>{@link #TRAILING TRAILING}
 * </ul>
 * <p>
 * 例如，以下图片显示了一个使用流布局管理器（其默认布局管理器）来定位三个按钮的 applet：
 * <p>
 * <img src="doc-files/FlowLayout-1.gif"
 * ALT="Graphic of Layout for Three Buttons"
 * style="float:center; margin: 7px 10px;">
 * <p>
 * 以下是此 applet 的代码：
 *
 * <hr><blockquote><pre>
 * import java.awt.*;
 * import java.applet.Applet;
 *
 * public class myButtons extends Applet {
 *     Button button1, button2, button3;
 *     public void init() {
 *         button1 = new Button("Ok");
 *         button2 = new Button("Open");
 *         button3 = new Button("Close");
 *         add(button1);
 *         add(button2);
 *         add(button3);
 *     }
 * }
 * </pre></blockquote><hr>
 * <p>
 * 流布局允许每个组件采用其自然（首选）大小。
 *
 * @author      Arthur van Hoff
 * @author      Sami Shaio
 * @since       JDK1.0
 * @see ComponentOrientation
 */
public class FlowLayout implements LayoutManager, java.io.Serializable {

    /**
     * 此值表示每行组件应左对齐。
     */
    public static final int LEFT        = 0;

    /**
     * 此值表示每行组件应居中对齐。
     */
    public static final int CENTER      = 1;

    /**
     * 此值表示每行组件应右对齐。
     */
    public static final int RIGHT       = 2;

    /**
     * 此值表示每行组件应根据容器的方向对齐到前缘，例如，在从左到右的方向中对齐到左边。
     *
     * @see     java.awt.Component#getComponentOrientation
     * @see     java.awt.ComponentOrientation
     * @since   1.2
     */
    public static final int LEADING     = 3;

    /**
     * 此值表示每行组件应根据容器的方向对齐到后缘，例如，在从左到右的方向中对齐到右边。
     *
     * @see     java.awt.Component#getComponentOrientation
     * @see     java.awt.ComponentOrientation
     * @since   1.2
     */
    public static final int TRAILING = 4;

    /**
     * <code>align</code> 属性确定每行如何分布空格。
     * 它可以是以下值之一：
     * <ul>
     * <li><code>LEFT</code>
     * <li><code>RIGHT</code>
     * <li><code>CENTER</code>
     * </ul>
     *
     * @serial
     * @see #getAlignment
     * @see #setAlignment
     */
    int align;          // 这是为了 1.1 序列化兼容性

    /**
     * <code>newAlign</code> 属性确定 Java 2 平台 v1.2 及更高版本中每行如何分布空格。
     * 它可以是以下五个值之一：
     * <ul>
     * <li><code>LEFT</code>
     * <li><code>RIGHT</code>
     * <li><code>CENTER</code>
     * <li><code>LEADING</code>
     * <li><code>TRAILING</code>
     * </ul>
     *
     * @serial
     * @since 1.2
     * @see #getAlignment
     * @see #setAlignment
     */
    int newAlign;       // 这是我们实际使用的属性

    /**
     * 流布局管理器允许组件之间有间距。水平间距将指定组件之间以及组件与 <code>Container</code>
     * 边框之间的空间。
     *
     * @serial
     * @see #getHgap()
     * @see #setHgap(int)
     */
    int hgap;

    /**
     * 流布局管理器允许组件之间有间距。垂直间距将指定行之间以及行与 <code>Container</code>
     * 边框之间的空间。
     *
     * @serial
     * @see #getHgap()
     * @see #setHgap(int)
     */
    int vgap;

    /**
     * 如果为 true，则组件将对齐到基线。
     */
    private boolean alignOnBaseline;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = -7262534875583282631L;

    /**
     * 构造一个新的 <code>FlowLayout</code>，对齐方式为居中，默认水平和垂直间距为 5 个单位。
     */
    public FlowLayout() {
        this(CENTER, 5, 5);
    }

    /**
     * 构造一个新的 <code>FlowLayout</code>，指定对齐方式，默认水平和垂直间距为 5 个单位。
     * 对齐参数的值必须是 <code>FlowLayout.LEFT</code>、<code>FlowLayout.RIGHT</code>、
     * <code>FlowLayout.CENTER</code>、<code>FlowLayout.LEADING</code> 或
     * <code>FlowLayout.TRAILING</code> 之一。
     * @param align 对齐值
     */
    public FlowLayout(int align) {
        this(align, 5, 5);
    }

    /**
     * 创建一个新的流布局管理器，指定对齐方式和水平及垂直间距。
     * <p>
     * 对齐参数的值必须是 <code>FlowLayout.LEFT</code>、<code>FlowLayout.RIGHT</code>、
     * <code>FlowLayout.CENTER</code>、<code>FlowLayout.LEADING</code> 或
     * <code>FlowLayout.TRAILING</code> 之一。
     * @param      align   对齐值
     * @param      hgap    组件之间的水平间距以及组件与 <code>Container</code> 边框之间的水平间距
     * @param      vgap    组件之间的垂直间距以及组件与 <code>Container</code> 边框之间的垂直间距
     */
    public FlowLayout(int align, int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
        setAlignment(align);
    }

    /**
     * 获取此布局的对齐方式。
     * 可能的值是 <code>FlowLayout.LEFT</code>、<code>FlowLayout.RIGHT</code>、
     * <code>FlowLayout.CENTER</code>、<code>FlowLayout.LEADING</code> 或
     * <code>FlowLayout.TRAILING</code>。
     * @return     此布局的对齐值
     * @see        java.awt.FlowLayout#setAlignment
     * @since      JDK1.1
     */
    public int getAlignment() {
        return newAlign;
    }

    /**
     * 设置此布局的对齐方式。
     * 可能的值是
     * <ul>
     * <li><code>FlowLayout.LEFT</code>
     * <li><code>FlowLayout.RIGHT</code>
     * <li><code>FlowLayout.CENTER</code>
     * <li><code>FlowLayout.LEADING</code>
     * <li><code>FlowLayout.TRAILING</code>
     * </ul>
     * @param      align 以上所示的对齐值之一
     * @see        #getAlignment()
     * @since      JDK1.1
     */
    public void setAlignment(int align) {
        this.newAlign = align;

        // this.align 仅用于序列化兼容性，
        // 因此设置为与 1.1 版本类兼容的值

        switch (align) {
        case LEADING:
            this.align = LEFT;
            break;
        case TRAILING:
            this.align = RIGHT;
            break;
        default:
            this.align = align;
            break;
        }
    }

    /**
     * 获取组件之间以及组件与 <code>Container</code> 边框之间的水平间距。
     *
     * @return     组件之间以及组件与 <code>Container</code> 边框之间的水平间距
     * @see        java.awt.FlowLayout#setHgap
     * @since      JDK1.1
     */
    public int getHgap() {
        return hgap;
    }

    /**
     * 设置组件之间以及组件与 <code>Container</code> 边框之间的水平间距。
     *
     * @param hgap 组件之间以及组件与 <code>Container</code> 边框之间的水平间距
     * @see        java.awt.FlowLayout#getHgap
     * @since      JDK1.1
     */
    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    /**
     * 获取组件之间以及组件与 <code>Container</code> 边框之间的垂直间距。
     *
     * @return     组件之间以及组件与 <code>Container</code> 边框之间的垂直间距
     * @see        java.awt.FlowLayout#setVgap
     * @since      JDK1.1
     */
    public int getVgap() {
        return vgap;
    }

    /**
     * 设置组件之间以及组件与 <code>Container</code> 边框之间的垂直间距。
     *
     * @param vgap 组件之间以及组件与 <code>Container</code> 边框之间的垂直间距
     * @see        java.awt.FlowLayout#getVgap
     * @since      JDK1.1
     */
    public void setVgap(int vgap) {
        this.vgap = vgap;
    }

    /**
     * 设置组件是否应垂直对齐到基线。没有基线的组件将居中对齐。
     * 默认值为 false。
     *
     * @param alignOnBaseline 组件是否应垂直对齐到基线
     * @since 1.6
     */
    public void setAlignOnBaseline(boolean alignOnBaseline) {
        this.alignOnBaseline = alignOnBaseline;
    }

    /**
     * 返回组件是否应垂直对齐到基线。默认值为 false。
     *
     * @return 组件是否应垂直对齐到基线
     * @since 1.6
     */
    public boolean getAlignOnBaseline() {
        return alignOnBaseline;
    }

    /**
     * 将指定的组件添加到布局中。
     * 本类不使用此方法。
     * @param name 组件的名称
     * @param comp 要添加的组件
     */
    public void addLayoutComponent(String name, Component comp) {
    }

    /**
     * 从布局中移除指定的组件。
     * 本类不使用此方法。
     * @param comp 要移除的组件
     * @see       java.awt.Container#removeAll
     */
    public void removeLayoutComponent(Component comp) {
    }

    /**
     * 返回指定目标容器中 <i>可见</i> 组件的首选布局尺寸。
     *
     * @param target 需要布局的容器
     * @return    布局指定容器的子组件的首选尺寸
     * @see Container
     * @see #minimumLayoutSize
     * @see       java.awt.Container#getPreferredSize
     */
    public Dimension preferredLayoutSize(Container target) {
      synchronized (target.getTreeLock()) {
        Dimension dim = new Dimension(0, 0);
        int nmembers = target.getComponentCount();
        boolean firstVisibleComponent = true;
        boolean useBaseline = getAlignOnBaseline();
        int maxAscent = 0;
        int maxDescent = 0;

        for (int i = 0 ; i < nmembers ; i++) {
            Component m = target.getComponent(i);
            if (m.isVisible()) {
                Dimension d = m.getPreferredSize();
                dim.height = Math.max(dim.height, d.height);
                if (firstVisibleComponent) {
                    firstVisibleComponent = false;
                } else {
                    dim.width += hgap;
                }
                dim.width += d.width;
                if (useBaseline) {
                    int baseline = m.getBaseline(d.width, d.height);
                    if (baseline >= 0) {
                        maxAscent = Math.max(maxAscent, baseline);
                        maxDescent = Math.max(maxDescent, d.height - baseline);
                    }
                }
            }
        }
        if (useBaseline) {
            dim.height = Math.max(maxAscent + maxDescent, dim.height);
        }
        Insets insets = target.getInsets();
        dim.width += insets.left + insets.right + hgap*2;
        dim.height += insets.top + insets.bottom + vgap*2;
        return dim;
      }
    }

    /**
     * 返回指定目标容器中 <i>可见</i> 组件的最小布局尺寸。
     * @param target 需要布局的容器
     * @return    布局指定容器的子组件的最小尺寸
     * @see #preferredLayoutSize
     * @see       java.awt.Container
     * @see       java.awt.Container#doLayout
     */
    public Dimension minimumLayoutSize(Container target) {
      synchronized (target.getTreeLock()) {
        boolean useBaseline = getAlignOnBaseline();
        Dimension dim = new Dimension(0, 0);
        int nmembers = target.getComponentCount();
        int maxAscent = 0;
        int maxDescent = 0;
        boolean firstVisibleComponent = true;

        for (int i = 0 ; i < nmembers ; i++) {
            Component m = target.getComponent(i);
            if (m.visible) {
                Dimension d = m.getMinimumSize();
                dim.height = Math.max(dim.height, d.height);
                if (firstVisibleComponent) {
                    firstVisibleComponent = false;
                } else {
                    dim.width += hgap;
                }
                dim.width += d.width;
                if (useBaseline) {
                    int baseline = m.getBaseline(d.width, d.height);
                    if (baseline >= 0) {
                        maxAscent = Math.max(maxAscent, baseline);
                        maxDescent = Math.max(maxDescent,
                                              dim.height - baseline);
                    }
                }
}
}


                    if (useBaseline) {
            dim.height = Math.max(maxAscent + maxDescent, dim.height);
        }

        Insets insets = target.getInsets();
        dim.width += insets.left + insets.right + hgap * 2;
        dim.height += insets.top + insets.bottom + vgap * 2;
        return dim;





      }
    }

    /**
     * 如果有余地，将指定行中的元素居中。
     * @param target 需要移动的组件
     * @param x x 坐标
     * @param y y 坐标
     * @param width 宽度尺寸
     * @param height 高度尺寸
     * @param rowStart 行的开始
     * @param rowEnd 行的结束
     * @param useBaseline 是否对齐基线。
     * @param ascent 组件的上升高度。仅在 useBaseline 为 true 时有效。
     * @param descent 组件的下降高度。仅在 useBaseline 为 true 时有效。
     * @return 实际行高
     */
    private int moveComponents(Container target, int x, int y, int width, int height,
                                int rowStart, int rowEnd, boolean ltr,
                                boolean useBaseline, int[] ascent,
                                int[] descent) {
        switch (newAlign) {
        case LEFT:
            x += ltr ? 0 : width;
            break;
        case CENTER:
            x += width / 2;
            break;
        case RIGHT:
            x += ltr ? width : 0;
            break;
        case LEADING:
            break;
        case TRAILING:
            x += width;
            break;
        }
        int maxAscent = 0;
        int nonbaselineHeight = 0;
        int baselineOffset = 0;
        if (useBaseline) {
            int maxDescent = 0;
            for (int i = rowStart ; i < rowEnd ; i++) {
                Component m = target.getComponent(i);
                if (m.visible) {
                    if (ascent[i] >= 0) {
                        maxAscent = Math.max(maxAscent, ascent[i]);
                        maxDescent = Math.max(maxDescent, descent[i]);
                    }
                    else {
                        nonbaselineHeight = Math.max(m.getHeight(),
                                                     nonbaselineHeight);
                    }
                }
            }
            height = Math.max(maxAscent + maxDescent, nonbaselineHeight);
            baselineOffset = (height - maxAscent - maxDescent) / 2;
        }
        for (int i = rowStart ; i < rowEnd ; i++) {
            Component m = target.getComponent(i);
            if (m.isVisible()) {
                int cy;
                if (useBaseline && ascent[i] >= 0) {
                    cy = y + baselineOffset + maxAscent - ascent[i];
                }
                else {
                    cy = y + (height - m.height) / 2;
                }
                if (ltr) {
                    m.setLocation(x, cy);
                } else {
                    m.setLocation(target.width - x - m.width, cy);
                }
                x += m.width + hgap;
            }
        }
        return height;
    }

    /**
     * 布局容器。此方法让每个
     * <i>可见的</i>组件采用
     * 其首选大小，通过重塑目标容器中的组件以满足此
     * <code>FlowLayout</code> 对象的对齐方式。
     *
     * @param target 被布局的指定组件
     * @see Container
     * @see       java.awt.Container#doLayout
     */
    public void layoutContainer(Container target) {
      synchronized (target.getTreeLock()) {
        Insets insets = target.getInsets();
        int maxwidth = target.width - (insets.left + insets.right + hgap * 2);
        int nmembers = target.getComponentCount();
        int x = 0, y = insets.top + vgap;
        int rowh = 0, start = 0;

        boolean ltr = target.getComponentOrientation().isLeftToRight();

        boolean useBaseline = getAlignOnBaseline();
        int[] ascent = null;
        int[] descent = null;

        if (useBaseline) {
            ascent = new int[nmembers];
            descent = new int[nmembers];
        }

        for (int i = 0 ; i < nmembers ; i++) {
            Component m = target.getComponent(i);
            if (m.isVisible()) {
                Dimension d = m.getPreferredSize();
                m.setSize(d.width, d.height);

                if (useBaseline) {
                    int baseline = m.getBaseline(d.width, d.height);
                    if (baseline >= 0) {
                        ascent[i] = baseline;
                        descent[i] = d.height - baseline;
                    }
                    else {
                        ascent[i] = -1;
                    }
                }
                if ((x == 0) || ((x + d.width) <= maxwidth)) {
                    if (x > 0) {
                        x += hgap;
                    }
                    x += d.width;
                    rowh = Math.max(rowh, d.height);
                } else {
                    rowh = moveComponents(target, insets.left + hgap, y,
                                   maxwidth - x, rowh, start, i, ltr,
                                   useBaseline, ascent, descent);
                    x = d.width;
                    y += vgap + rowh;
                    rowh = d.height;
                    start = i;
                }
            }
        }
        moveComponents(target, insets.left + hgap, y, maxwidth - x, rowh,
                       start, nmembers, ltr, useBaseline, ascent, descent);
      }
    }

    //
    // 内部序列化版本，表示写入的版本
    // - 0 (默认) 用于 Java 2 平台 v1.2 之前的版本
    // - 1 用于 Java 2 平台 v1.2 及更高版本，包含 "newAlign" 字段
    //
    private static final int currentSerialVersion = 1;
    /**
     * 表示当前使用的 <code>currentSerialVersion</code>。
     * 它将是以下两个值之一：
     * <code>0</code> Java 2 平台 v1.2 之前的版本。
     * <code>1</code> Java 2 平台 v1.2 之后的版本。
     *
     * @serial
     * @since 1.2
     */
    private int serialVersionOnStream = currentSerialVersion;

    /**
     * 从序列化流中读取此对象，处理由旧版本类写入的对象，这些对象不包含我们当前使用的所有字段。
     */
    private void readObject(ObjectInputStream stream)
         throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();

        if (serialVersionOnStream < 1) {
            // "newAlign" 字段不存在，因此使用旧的 "align" 字段。
            setAlignment(this.align);
        }
        serialVersionOnStream = currentSerialVersion;
    }

    /**
     * 返回此 <code>FlowLayout</code> 对象及其值的字符串表示形式。
     * @return 此布局的字符串表示形式
     */
    public String toString() {
        String str = "";
        switch (align) {
          case LEFT:        str = ",align=left"; break;
          case CENTER:      str = ",align=center"; break;
          case RIGHT:       str = ",align=right"; break;
          case LEADING:     str = ",align=leading"; break;
          case TRAILING:    str = ",align=trailing"; break;
        }
        return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + str + "]";
    }


}
