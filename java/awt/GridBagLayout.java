
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

import java.util.Hashtable;
import java.util.Arrays;

/**
 * <code>GridBagLayout</code> 类是一个灵活的布局管理器，可以垂直、水平或沿基线对齐组件，而不要求组件具有相同的大小。
 * 每个 <code>GridBagLayout</code> 对象维护一个动态的矩形网格，每个组件占据一个或多个单元格，称为其 <em>显示区域</em>。
 * <p>
 * 每个由 <code>GridBagLayout</code> 管理的组件都关联一个 {@link GridBagConstraints} 实例。约束对象指定了组件的显示区域在网格中的位置
 * 以及组件在其显示区域中的对齐方式。除了组件的约束对象外，<code>GridBagLayout</code> 还考虑每个组件的最小和首选大小，以确定组件的大小。
 * <p>
 * 网格的整体方向取决于容器的 {@link ComponentOrientation} 属性。对于水平从左到右的布局，网格坐标 (0,0) 位于容器的左上角，x 坐标向右增加，y 坐标向下增加。
 * 对于水平从右到左的布局，网格坐标 (0,0) 位于容器的右上角，x 坐标向左增加，y 坐标向下增加。
 * <p>
 * 要有效使用网格布局，必须自定义一个或多个与其组件关联的 <code>GridBagConstraints</code> 对象。可以通过设置一个或多个其实例变量来自定义 <code>GridBagConstraints</code> 对象：
 *
 * <dl>
 * <dt>{@link GridBagConstraints#gridx},
 * {@link GridBagConstraints#gridy}
 * <dd>指定组件显示区域的起始角所在的单元格，网格原点处的单元格地址为
 * <code>gridx&nbsp;=&nbsp;0</code>，
 * <code>gridy&nbsp;=&nbsp;0</code>。对于水平从左到右的布局，组件的起始角是其左上角。对于水平从右到左的布局，组件的起始角是其右上角。
 * 使用 <code>GridBagConstraints.RELATIVE</code>（默认值）指定组件应放置在
 * （对于 <code>gridx</code> 沿 x 轴或对于 <code>gridy</code> 沿 y 轴）在添加到容器中的前一个组件之后。
 * <dt>{@link GridBagConstraints#gridwidth},
 * {@link GridBagConstraints#gridheight}
 * <dd>指定组件显示区域中的行数（对于 <code>gridwidth</code>）或列数（对于 <code>gridheight</code）。
 * 默认值为 1。
 * 使用 <code>GridBagConstraints.REMAINDER</code> 指定
 * 组件的显示区域将从 <code>gridx</code> 到行的最后一个单元格（对于 <code>gridwidth</code>）
 * 或从 <code>gridy</code> 到列的最后一个单元格（对于 <code>gridheight</code>）。
 *
 * 使用 <code>GridBagConstraints.RELATIVE</code> 指定
 * 组件的显示区域将从 <code>gridx</code> 到行的倒数第二个单元格（对于 <code>gridwidth</code>）
 * 或从 <code>gridy</code> 到列的倒数第二个单元格（对于 <code>gridheight</code>）。
 *
 * <dt>{@link GridBagConstraints#fill}
 * <dd>当组件的显示区域大于组件的请求大小时，用于确定是否（以及如何）调整组件的大小。
 * 可能的值有
 * <code>GridBagConstraints.NONE</code>（默认值），
 * <code>GridBagConstraints.HORIZONTAL</code>
 * （使组件足够宽以填满其显示区域的水平部分，但不改变其高度），
 * <code>GridBagConstraints.VERTICAL</code>
 * （使组件足够高以填满其显示区域的垂直部分，但不改变其宽度），以及
 * <code>GridBagConstraints.BOTH</code>
 * （使组件完全填满其显示区域）。
 * <dt>{@link GridBagConstraints#ipadx},
 * {@link GridBagConstraints#ipady}
 * <dd>指定组件在布局中的内部填充，即添加到组件最小大小的额外空间。
 * 组件的宽度至少为最小宽度加上 <code>ipadx</code> 像素。同样，组件的高度至少为最小高度加上
 * <code>ipady</code> 像素。
 * <dt>{@link GridBagConstraints#insets}
 * <dd>指定组件的外部填充，即组件与其显示区域边缘之间的最小空间。
 * <dt>{@link GridBagConstraints#anchor}
 * <dd>指定组件在其显示区域中的位置。有三种可能的值：绝对值、方向相对值和基线相对值
 * 方向相对值根据容器的 <code>ComponentOrientation</code> 属性进行解释，而绝对值则不进行解释。基线相对值根据基线进行计算。有效值为：
 *
 * <center><table BORDER=0 WIDTH=800
 *        SUMMARY="absolute, relative and baseline values as described above">
 * <tr>
 * <th><P style="text-align:left">绝对值</th>
 * <th><P style="text-align:left">方向相对值</th>
 * <th><P style="text-align:left">基线相对值</th>
 * </tr>
 * <tr>
 * <td>
 * <ul style="list-style-type:none">
 * <li><code>GridBagConstraints.NORTH</code></li>
 * <li><code>GridBagConstraints.SOUTH</code></li>
 * <li><code>GridBagConstraints.WEST</code></li>
 * <li><code>GridBagConstraints.EAST</code></li>
 * <li><code>GridBagConstraints.NORTHWEST</code></li>
 * <li><code>GridBagConstraints.NORTHEAST</code></li>
 * <li><code>GridBagConstraints.SOUTHWEST</code></li>
 * <li><code>GridBagConstraints.SOUTHEAST</code></li>
 * <li><code>GridBagConstraints.CENTER</code>（默认值）</li>
 * </ul>
 * </td>
 * <td>
 * <ul style="list-style-type:none">
 * <li><code>GridBagConstraints.PAGE_START</code></li>
 * <li><code>GridBagConstraints.PAGE_END</code></li>
 * <li><code>GridBagConstraints.LINE_START</code></li>
 * <li><code>GridBagConstraints.LINE_END</code></li>
 * <li><code>GridBagConstraints.FIRST_LINE_START</code></li>
 * <li><code>GridBagConstraints.FIRST_LINE_END</code></li>
 * <li><code>GridBagConstraints.LAST_LINE_START</code></li>
 * <li><code>GridBagConstraints.LAST_LINE_END</code></li>
 * </ul>
 * </td>
 * <td>
 * <ul style="list-style-type:none">
 * <li><code>GridBagConstraints.BASELINE</code></li>
 * <li><code>GridBagConstraints.BASELINE_LEADING</code></li>
 * <li><code>GridBagConstraints.BASELINE_TRAILING</code></li>
 * <li><code>GridBagConstraints.ABOVE_BASELINE</code></li>
 * <li><code>GridBagConstraints.ABOVE_BASELINE_LEADING</code></li>
 * <li><code>GridBagConstraints.ABOVE_BASELINE_TRAILING</code></li>
 * <li><code>GridBagConstraints.BELOW_BASELINE</code></li>
 * <li><code>GridBagConstraints.BELOW_BASELINE_LEADING</code></li>
 * <li><code>GridBagConstraints.BELOW_BASELINE_TRAILING</code></li>
 * </ul>
 * </td>
 * </tr>
 * </table></center>
 * <dt>{@link GridBagConstraints#weightx},
 * {@link GridBagConstraints#weighty}
 * <dd>用于确定如何分配空间，这对于指定调整大小行为非常重要。
 * 除非为至少一个组件指定行（<code>weightx</code>）和列（<code>weighty</code>）的权重，
 * 否则所有组件会聚集在容器的中心。这是因为当权重为零（默认值）时，
 * <code>GridBagLayout</code> 对象会将多余的空隙放在其单元格网格与容器边缘之间。
 * </dl>
 * <p>
 * 每行可能有一个基线；基线由该行中具有有效基线且沿基线对齐（组件的锚定值为 {@code
 * BASELINE}，{@code BASELINE_LEADING} 或 {@code BASELINE_TRAILING}）的组件确定。
 * 如果该行中的组件没有有效基线，则该行没有基线。
 * <p>
 * 如果组件跨越多行，则它将对齐到起始行（如果基线调整大小行为为 {@code
 * CONSTANT_ASCENT}）或结束行（如果基线调整大小行为为 {@code CONSTANT_DESCENT}）。对齐的行称为 <em>主导行</em>。
 * <p>
 * 下图显示了一个基线布局，并包含一个跨越多行的组件：
 * <center><table summary="Baseline Layout">
 * <tr ALIGN=CENTER>
 * <td>
 * <img src="doc-files/GridBagLayout-baseline.png"
 *  alt="The following text describes this graphic (Figure 1)." style="float:center">
 * </td>
 * </table></center>
 * 此布局由三个组件组成：
 * <ul><li>一个面板，从第 0 行开始，到第 1 行结束。面板的基线调整大小行为为 <code>CONSTANT_DESCENT</code>，锚定值为 <code>BASELINE</code>。由于基线调整大小行为为 <code>CONSTANT_DESCENT</code>，面板的主导行为为第 1 行。
 * <li>两个按钮，每个按钮的基线调整大小行为为 <code>CENTER_OFFSET</code>，锚定值为 <code>BASELINE</code>。
 * </ul>
 * 由于第二个按钮和面板共享相同的主导行，它们都沿其基线对齐。
 * <p>
 * 使用基线相对值定位的组件在调整大小时与使用绝对值或方向相对值定位的组件不同。组件如何变化取决于主导行的基线如何变化。基线固定在显示区域的底部，如果同一主导行中的任何组件的基线调整大小行为为 <code>CONSTANT_DESCENT</code>，否则基线固定在显示区域的顶部。以下规则决定了调整大小的行为：
 * <ul>
 * <li>在基线上方的可调整大小的组件只能增长到基线的高度。例如，如果基线位于 100 且固定在顶部，则位于基线上方的可调整大小的组件永远不能增长超过 100 单位。
 * <li>同样，位于基线以下的可调整大小的组件只能增长到显示高度与基线之间的差值。
 * <li>位于基线上且基线调整大小行为为 <code>OTHER</code> 的可调整大小的组件只有在调整大小后的基线适合显示区域时才会调整大小。如果基线不适合显示区域，组件不会调整大小。
 * <li>位于基线上且基线调整大小行为不为 <code>OTHER</code> 的组件只能增长到 {@code 显示高度 - 基线 + 组件的基线}。
 * </ul>
 * 如果将组件定位在基线上，但组件没有有效基线，它将垂直居中在其空间中。同样，如果将组件相对于基线定位，但该行中的组件没有有效基线，组件将垂直居中。
 * <p>
 * 下图显示了由网格布局管理的十个组件（全部为按钮）。图 2 显示了水平从左到右的容器布局，图 3 显示了水平从右到左的容器布局。
 *
 * <center><table WIDTH=600 summary="layout">
 * <tr ALIGN=CENTER>
 * <td>
 * <img src="doc-files/GridBagLayout-1.gif" alt="The preceding text describes this graphic (Figure 1)." style="float:center; margin: 7px 10px;">
 * </td>
 * <td>
 * <img src="doc-files/GridBagLayout-2.gif" alt="The preceding text describes this graphic (Figure 2)." style="float:center; margin: 7px 10px;">
 * </td>
 * <tr ALIGN=CENTER>
 * <td>图 2：水平，从左到右</td>
 * <td>图 3：水平，从右到左</td>
 * </tr>
 * </table></center>
 * <p>
 * 十个组件中的每个组件的关联 <code>GridBagConstraints</code> 对象的 <code>fill</code> 字段
 * 都设置为 <code>GridBagConstraints.BOTH</code>。
 * 此外，组件具有以下非默认约束：
 *
 * <ul>
 * <li>Button1, Button2, Button3: <code>weightx&nbsp;=&nbsp;1.0</code>
 * <li>Button4: <code>weightx&nbsp;=&nbsp;1.0</code>,
 * <code>gridwidth&nbsp;=&nbsp;GridBagConstraints.REMAINDER</code>
 * <li>Button5: <code>gridwidth&nbsp;=&nbsp;GridBagConstraints.REMAINDER</code>
 * <li>Button6: <code>gridwidth&nbsp;=&nbsp;GridBagConstraints.RELATIVE</code>
 * <li>Button7: <code>gridwidth&nbsp;=&nbsp;GridBagConstraints.REMAINDER</code>
 * <li>Button8: <code>gridheight&nbsp;=&nbsp;2</code>,
 * <code>weighty&nbsp;=&nbsp;1.0</code>
 * <li>Button9, Button 10:
 * <code>gridwidth&nbsp;=&nbsp;GridBagConstraints.REMAINDER</code>
 * </ul>
 * <p>
 * 以下是实现上述示例的代码：
 *
 * <hr><blockquote><pre>
 * import java.awt.*;
 * import java.util.*;
 * import java.applet.Applet;
 *
 * public class GridBagEx1 extends Applet {
 *
 *     protected void makebutton(String name,
 *                               GridBagLayout gridbag,
 *                               GridBagConstraints c) {
 *         Button button = new Button(name);
 *         gridbag.setConstraints(button, c);
 *         add(button);
 *     }
 *
 *     public void init() {
 *         GridBagLayout gridbag = new GridBagLayout();
 *         GridBagConstraints c = new GridBagConstraints();
 *
 *         setFont(new Font("SansSerif", Font.PLAIN, 14));
 *         setLayout(gridbag);
 *
 *         c.fill = GridBagConstraints.BOTH;
 *         c.weightx = 1.0;
 *         makebutton("Button1", gridbag, c);
 *         makebutton("Button2", gridbag, c);
 *         makebutton("Button3", gridbag, c);
 *
 *         c.gridwidth = GridBagConstraints.REMAINDER; //end row
 *         makebutton("Button4", gridbag, c);
 *
 *         c.weightx = 0.0;                //reset to the default
 *         makebutton("Button5", gridbag, c); //another row
 *
 *         c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last in row
 *         makebutton("Button6", gridbag, c);
 *
 *         c.gridwidth = GridBagConstraints.REMAINDER; //end row
 *         makebutton("Button7", gridbag, c);
 *
 *         c.gridwidth = 1;                //reset to the default
 *         c.gridheight = 2;
 *         c.weighty = 1.0;
 *         makebutton("Button8", gridbag, c);
 *
 *         c.weighty = 0.0;                //reset to the default
 *         c.gridwidth = GridBagConstraints.REMAINDER; //end row
 *         c.gridheight = 1;               //reset to the default
 *         makebutton("Button9", gridbag, c);
 *         makebutton("Button10", gridbag, c);
 *
 *         setSize(300, 100);
 *     }
 *
 *     public static void main(String args[]) {
 *         Frame f = new Frame("GridBag Layout Example");
 *         GridBagEx1 ex1 = new GridBagEx1();
 *
 *         ex1.init();
 *
 *         f.add("Center", ex1);
 *         f.pack();
 *         f.setSize(f.getPreferredSize());
 *         f.show();
 *     }
 * }
 * </pre></blockquote><hr>
 * <p>
 * @author Doug Stein
 * @author Bill Spitzak（原始 NeWS &amp; OLIT 实现）
 * @see       java.awt.GridBagConstraints
 * @see       java.awt.GridBagLayoutInfo
 * @see       java.awt.ComponentOrientation
 * @since JDK1.0
 */
public class GridBagLayout implements LayoutManager2,
java.io.Serializable {


                static final int EMPIRICMULTIPLIER = 2;
    /**
     * 该字段不再用于保留数组，而是为了向后兼容而保留。以前，这是
     * 由网格布局可以布局的最大网格位置数（水平和垂直）。
     * 当前实现不对网格的大小施加任何限制。
     */
    protected static final int MAXGRIDSIZE = 512;

    /**
     * 网格布局可以布局的最小网格。
     */
    protected static final int MINSIZE = 1;
    /**
     * 网格布局可以布局的首选网格大小。
     */
    protected static final int PREFERREDSIZE = 2;

    /**
     * 此哈希表维护组件与其网格袋约束之间的关联。
     * <code>comptable</code> 中的键是组件，值是 <code>GridBagConstraints</code> 的实例。
     *
     * @serial
     * @see java.awt.GridBagConstraints
     */
    protected Hashtable<Component,GridBagConstraints> comptable;

    /**
     * 此字段保存包含默认值的网格袋约束实例，因此如果组件
     * 没有关联的网格袋约束，则该组件将被分配一个
     * <code>defaultConstraints</code> 的副本。
     *
     * @serial
     * @see #getConstraints(Component)
     * @see #setConstraints(Component, GridBagConstraints)
     * @see #lookupConstraints(Component)
     */
    protected GridBagConstraints defaultConstraints;

    /**
     * 此字段保存网格袋的布局信息。此字段中的信息
     * 基于网格袋的最近一次验证。
     * 如果 <code>layoutInfo</code> 为 <code>null</code>
     * 这表示网格袋中没有组件，或者如果有组件，它们尚未
     * 被验证。
     *
     * @serial
     * @see #getLayoutInfo(Container, int)
     */
    protected GridBagLayoutInfo layoutInfo;

    /**
     * 此字段保存列最小宽度的覆盖。如果此字段非 <code>null</code>，则这些值
     * 在计算所有最小列宽后应用于网格袋。
     * 如果 <code>columnWidths</code> 的元素数多于列数，则会向网格袋中添加列以匹配
     * <code>columnWidth</code> 中的元素数。
     *
     * @serial
     * @see #getLayoutDimensions()
     */
    public int columnWidths[];

    /**
     * 此字段保存行最小高度的覆盖。如果此字段非 <code>null</code>，则这些值
     * 在计算所有最小行高后应用于网格袋。
     * 如果 <code>rowHeights</code> 的元素数多于行数，则会向网格袋中添加行以匹配
     * <code>rowHeights</code> 中的元素数。
     *
     * @serial
     * @see #getLayoutDimensions()
     */
    public int rowHeights[];

    /**
     * 此字段保存列权重的覆盖。如果此字段非 <code>null</code>，则这些值
     * 在计算所有列权重后应用于网格袋。
     * 如果 <code>columnWeights[i]</code> > 列 i 的权重，则
     * 列 i 被分配 <code>columnWeights[i]</code> 中的权重。
     * 如果 <code>columnWeights</code> 的元素数多于列数，则多余的元素被忽略 - 它们不会
     * 导致创建更多的列。
     *
     * @serial
     */
    public double columnWeights[];

    /**
     * 此字段保存行权重的覆盖。如果此字段非 <code>null</code>，则这些值
     * 在计算所有行权重后应用于网格袋。
     * 如果 <code>rowWeights[i]</code> > 行 i 的权重，则
     * 行 i 被分配 <code>rowWeights[i]</code> 中的权重。
     * 如果 <code>rowWeights</code> 的元素数多于行数，则多余的元素被忽略 - 它们不会
     * 导致创建更多的行。
     *
     * @serial
     */
    public double rowWeights[];

    /**
     * 正在定位的组件。在调用 <code>adjustForGravity</code> 之前设置。
     */
    private Component componentAdjusting;

    /**
     * 创建一个网格袋布局管理器。
     */
    public GridBagLayout () {
        comptable = new Hashtable<Component,GridBagConstraints>();
        defaultConstraints = new GridBagConstraints();
    }

    /**
     * 设置此布局中指定组件的约束。
     * @param       comp 要修改的组件
     * @param       constraints 要应用的约束
     */
    public void setConstraints(Component comp, GridBagConstraints constraints) {
        comptable.put(comp, (GridBagConstraints)constraints.clone());
    }

    /**
     * 获取指定组件的约束。返回实际的 <code>GridBagConstraints</code> 对象的副本。
     * @param       comp 要查询的组件
     * @return      指定组件在此网格袋布局中的约束；返回实际约束对象的副本
     */
    public GridBagConstraints getConstraints(Component comp) {
        GridBagConstraints constraints = comptable.get(comp);
        if (constraints == null) {
            setConstraints(comp, defaultConstraints);
            constraints = comptable.get(comp);
        }
        return (GridBagConstraints)constraints.clone();
    }

    /**
     * 检索指定组件的约束。
     * 返回值不是副本，而是布局机制使用的实际
     * <code>GridBagConstraints</code> 对象。
     * <p>
     * 如果 <code>comp</code> 不在 <code>GridBagLayout</code> 中，
     * 则返回一组默认的 <code>GridBagConstraints</code>。
     * <code>comp</code> 值为 <code>null</code> 无效
     * 并返回 <code>null</code>。
     *
     * @param       comp 要查询的组件
     * @return      指定组件的约束
     */
    protected GridBagConstraints lookupConstraints(Component comp) {
        GridBagConstraints constraints = comptable.get(comp);
        if (constraints == null) {
            setConstraints(comp, defaultConstraints);
            constraints = comptable.get(comp);
        }
        return constraints;
    }

    /**
     * 从此布局中移除指定组件的约束
     * @param       comp 要修改的组件
     */
    private void removeConstraints(Component comp) {
        comptable.remove(comp);
    }

    /**
     * 确定布局区域的原点，位于目标容器的图形坐标空间中。此值表示布局区域左上角的像素坐标，无论容器的 <code>ComponentOrientation</code> 值如何。这与由单元格坐标 (0,0) 给出的网格原点不同。
     * 大多数应用程序不直接调用此方法。
     * @return     布局网格左上角单元格的图形原点
     * @see        java.awt.ComponentOrientation
     * @since      JDK1.1
     */
    public Point getLayoutOrigin () {
        Point origin = new Point(0,0);
        if (layoutInfo != null) {
            origin.x = layoutInfo.startx;
            origin.y = layoutInfo.starty;
        }
        return origin;
    }

    /**
     * 确定布局网格的列宽和行高。
     * <p>
     * 大多数应用程序不直接调用此方法。
     * @return     一个包含两个数组的数组，分别包含布局列的宽度和布局行的高度
     * @since      JDK1.1
     */
    public int [][] getLayoutDimensions () {
        if (layoutInfo == null)
            return new int[2][0];

        int dim[][] = new int [2][];
        dim[0] = new int[layoutInfo.width];
        dim[1] = new int[layoutInfo.height];

        System.arraycopy(layoutInfo.minWidth, 0, dim[0], 0, layoutInfo.width);
        System.arraycopy(layoutInfo.minHeight, 0, dim[1], 0, layoutInfo.height);

        return dim;
    }

    /**
     * 确定布局网格的列和行的权重。权重用于计算给定列或行在布局有多余空间时超出其首选大小的程度。
     * <p>
     * 大多数应用程序不直接调用此方法。
     * @return      一个包含两个数组的数组，分别表示布局列的水平权重和布局行的垂直权重
     * @since       JDK1.1
     */
    public double [][] getLayoutWeights () {
        if (layoutInfo == null)
            return new double[2][0];

        double weights[][] = new double [2][];
        weights[0] = new double[layoutInfo.width];
        weights[1] = new double[layoutInfo.height];

        System.arraycopy(layoutInfo.weightX, 0, weights[0], 0, layoutInfo.width);
        System.arraycopy(layoutInfo.weightY, 0, weights[1], 0, layoutInfo.height);

        return weights;
    }

    /**
     * 确定布局网格中包含点 <code>(x,&nbsp;y)</code> 的单元格。每个单元格由其列索引（从 0 到列数减 1）和行索引（从 0 到行数减 1）标识。
     * <p>
     * 如果点 <code>(x,&nbsp;y)</code> 位于网格之外，则使用以下规则。
     * 如果 <code>x</code> 位于从左到右容器的布局左侧或从右到左容器的布局右侧，则返回列索引为零。如果 <code>x</code> 位于从左到右容器的布局右侧或从右到左容器的布局左侧，则返回列索引为列数。如果 <code>y</code> 位于布局上方，则返回行索引为零；如果 <code>y</code> 位于布局下方，则返回行索引为行数。容器的方向由其 <code>ComponentOrientation</code> 属性确定。
     * @param      x    点的 <i>x</i> 坐标
     * @param      y    点的 <i>y</i> 坐标
     * @return     一个有序的索引对，指示布局网格中包含点
     *             (<i>x</i>,&nbsp;<i>y</i>) 的单元格。
     * @see        java.awt.ComponentOrientation
     * @since      JDK1.1
     */
    public Point location(int x, int y) {
        Point loc = new Point(0,0);
        int i, d;

        if (layoutInfo == null)
            return loc;

        d = layoutInfo.startx;
        if (!rightToLeft) {
            for (i=0; i<layoutInfo.width; i++) {
                d += layoutInfo.minWidth[i];
                if (d > x)
                    break;
            }
        } else {
            for (i=layoutInfo.width-1; i>=0; i--) {
                if (d > x)
                    break;
                d += layoutInfo.minWidth[i];
            }
            i++;
        }
        loc.x = i;

        d = layoutInfo.starty;
        for (i=0; i<layoutInfo.height; i++) {
            d += layoutInfo.minHeight[i];
            if (d > y)
                break;
        }
        loc.y = i;

        return loc;
    }

    /**
     * 无效，因为此布局管理器不使用每个组件的字符串。
     */
    public void addLayoutComponent(String name, Component comp) {
    }

    /**
     * 使用指定的 <code>constraints</code> 对象将指定的组件添加到布局中。注意，约束是可变的，因此在缓存时会被克隆。
     *
     * @param      comp         要添加的组件
     * @param      constraints  确定组件如何添加到布局中的对象
     * @exception IllegalArgumentException 如果 <code>constraints</code>
     *            不是 <code>GridBagConstraint</code>
     */
    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints instanceof GridBagConstraints) {
            setConstraints(comp, (GridBagConstraints)constraints);
        } else if (constraints != null) {
            throw new IllegalArgumentException("cannot add to layout: constraints must be a GridBagConstraint");
        }
    }

    /**
     * 从此布局中移除指定的组件。
     * <p>
     * 大多数应用程序不直接调用此方法。
     * @param    comp   要移除的组件。
     * @see      java.awt.Container#remove(java.awt.Component)
     * @see      java.awt.Container#removeAll()
     */
    public void removeLayoutComponent(Component comp) {
        removeConstraints(comp);
    }

    /**
     * 确定使用此网格袋布局的 <code>parent</code> 容器的首选大小。
     * <p>
     * 大多数应用程序不直接调用此方法。
     *
     * @param     parent   要进行布局的容器
     * @see       java.awt.Container#getPreferredSize
     * @return the preferred size of the <code>parent</code>
     *  container
     */
    public Dimension preferredLayoutSize(Container parent) {
        GridBagLayoutInfo info = getLayoutInfo(parent, PREFERREDSIZE);
        return getMinSize(parent, info);
    }

    /**
     * 确定使用此网格袋布局的 <code>parent</code> 容器的最小大小。
     * <p>
     * 大多数应用程序不直接调用此方法。
     * @param     parent   要进行布局的容器
     * @see       java.awt.Container#doLayout
     * @return the minimum size of the <code>parent</code> container
     */
    public Dimension minimumLayoutSize(Container parent) {
        GridBagLayoutInfo info = getLayoutInfo(parent, MINSIZE);
        return getMinSize(parent, info);
    }

    /**
     * 返回给定目标容器中组件的最大尺寸。
     * @param target 需要布局的容器
     * @see Container
     * @see #minimumLayoutSize(Container)
     * @see #preferredLayoutSize(Container)
     * @return this layout's maximum dimensions
     */
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * 返回沿 x 轴的对齐方式。这指定了组件相对于其他组件的对齐方式。值应在 0 到 1 之间
     * 其中 0 表示沿原点对齐，1 表示远离原点对齐，0.5 表示居中，等等。
     * <p>
     * @return the value <code>0.5f</code> to indicate centered
     */
    public float getLayoutAlignmentX(Container parent) {
        return 0.5f;
    }


                /**
                 * 返回沿 y 轴的对齐方式。这指定了组件相对于其他组件的对齐方式。值应该在 0 和 1 之间，
                 * 其中 0 表示沿原点对齐，1 表示远离原点对齐，0.5 表示居中对齐等。
                 * <p>
                 * @return 值 <code>0.5f</code> 表示居中对齐。
                 */
                public float getLayoutAlignmentY(Container parent) {
                    return 0.5f;
                }

                /**
                 * 使布局失效，指示布局管理器应丢弃缓存的信息。
                 */
                public void invalidateLayout(Container target) {
                }

                /**
                 * 使用此网格布局对指定的容器进行布局。此方法将重新调整指定容器中组件的形状，
                 * 以满足此 <code>GridBagLayout</code> 对象的约束。
                 * <p>
                 * 大多数应用程序不会直接调用此方法。
                 * @param parent 要进行布局的容器
                 * @see java.awt.Container
                 * @see java.awt.Container#doLayout
                 */
                public void layoutContainer(Container parent) {
                    arrangeGrid(parent);
                }

                /**
                 * 返回此网格布局值的字符串表示形式。
                 * @return 此网格布局的字符串表示形式。
                 */
                public String toString() {
                    return getClass().getName();
                }

                /**
                 * 打印布局信息。对调试很有用。
                 */

                /* DEBUG
                 *
                 *  protected void dumpLayoutInfo(GridBagLayoutInfo s) {
                 *    int x;
                 *
                 *    System.out.println("Col\tWidth\tWeight");
                 *    for (x=0; x<s.width; x++) {
                 *      System.out.println(x + "\t" +
                 *                   s.minWidth[x] + "\t" +
                 *                   s.weightX[x]);
                 *    }
                 *    System.out.println("Row\tHeight\tWeight");
                 *    for (x=0; x<s.height; x++) {
                 *      System.out.println(x + "\t" +
                 *                   s.minHeight[x] + "\t" +
                 *                   s.weightY[x]);
                 *    }
                 *  }
                 */

                /**
                 * 打印布局约束。对调试很有用。
                 */

                /* DEBUG
                 *
                 *  protected void dumpConstraints(GridBagConstraints constraints) {
                 *    System.out.println(
                 *                 "wt " +
                 *                 constraints.weightx +
                 *                 " " +
                 *                 constraints.weighty +
                 *                 ", " +
                 *
                 *                 "box " +
                 *                 constraints.gridx +
                 *                 " " +
                 *                 constraints.gridy +
                 *                 " " +
                 *                 constraints.gridwidth +
                 *                 " " +
                 *                 constraints.gridheight +
                 *                 ", " +
                 *
                 *                 "min " +
                 *                 constraints.minWidth +
                 *                 " " +
                 *                 constraints.minHeight +
                 *                 ", " +
                 *
                 *                 "pad " +
                 *                 constraints.insets.bottom +
                 *                 " " +
                 *                 constraints.insets.left +
                 *                 " " +
                 *                 constraints.insets.right +
                 *                 " " +
                 *                 constraints.insets.top +
                 *                 " " +
                 *                 constraints.ipadx +
                 *                 " " +
                 *                 constraints.ipady);
                 *  }
                 */

                /**
                 * 为当前管理的子组件集填充 <code>GridBagLayoutInfo</code> 的实例。这需要遍历子组件集三次：
                 *
                 * <ol>
                 * <li>确定布局网格的尺寸。
                 * <li>确定组件占据的单元格。
                 * <li>在行和列之间分配权重和最小尺寸。
                 * </ol>
                 *
                 * 这还会在首次遇到子组件时缓存它们的最小尺寸（以便后续循环不需要再次询问）。
                 * <p>
                 * 此方法仅供 <code>GridBagLayout</code> 内部使用。
                 *
                 * @param parent  布局容器
                 * @param sizeflag 为 <code>PREFERREDSIZE</code> 或 <code>MINSIZE</code>
                 * @return 子组件集的 <code>GridBagLayoutInfo</code>
                 * @since 1.4
                 */
                protected GridBagLayoutInfo getLayoutInfo(Container parent, int sizeflag) {
                    return GetLayoutInfo(parent, sizeflag);
                }

                /*
                 * 计算最大数组大小，以在不使用 ensureCapacity 的情况下分配数组。
                 * 我们可以在整个类中使用预计算的大小，因为对 maximumArrayXIndex 和 maximumArrayYIndex 的上界估计。
                 */

                private long[]  preInitMaximumArraySizes(Container parent){
                    Component components[] = parent.getComponents();
                    Component comp;
                    GridBagConstraints constraints;
                    int curX, curY;
                    int curWidth, curHeight;
                    int preMaximumArrayXIndex = 0;
                    int preMaximumArrayYIndex = 0;
                    long [] returnArray = new long[2];

                    for (int compId = 0 ; compId < components.length ; compId++) {
                        comp = components[compId];
                        if (!comp.isVisible()) {
                            continue;
                        }

                        constraints = lookupConstraints(comp);
                        curX = constraints.gridx;
                        curY = constraints.gridy;
                        curWidth = constraints.gridwidth;
                        curHeight = constraints.gridheight;

                        // -1==RELATIVE，表示列或行等于先前添加的组件，
                        // 由于每个下一个 gridx 或 gridy == RELATIVE 的组件从
                        // 前一个位置开始，所以我们应该从已经用于 maximumArray[X|Y]Index 计算的前一个组件开始。
                        // 我们可以通过增加 1 来处理添加 gridx=-1 的组件的情况。
                        if (curX < 0){
                            curX = ++preMaximumArrayYIndex;
                        }
                        if (curY < 0){
                            curY = ++preMaximumArrayXIndex;
                        }
                        // gridwidth 或 gridheight 可能等于 RELATIVE (-1) 或 REMAINDER (0)
                        // 任何情况下使用 1 代替 0 或 -1 应该足以正确计算 maximumArraySizes
                        if (curWidth <= 0){
                            curWidth = 1;
                        }
                        if (curHeight <= 0){
                            curHeight = 1;
                        }

                        preMaximumArrayXIndex = Math.max(curY + curHeight, preMaximumArrayXIndex);
                        preMaximumArrayYIndex = Math.max(curX + curWidth, preMaximumArrayYIndex);
                    } //for (components) loop
                    // 必须指定 index++ 以分配正常工作的数组。
                    /* 修复 4623196。
                     * 现在返回 long 数组而不是 Point
                     */
                    returnArray[0] = preMaximumArrayXIndex;
                    returnArray[1] = preMaximumArrayYIndex;
                    return returnArray;
                } //PreInitMaximumSizes

                /**
                 * 此方法已过时，仅为了向后兼容而提供；新代码应调用 {@link
                 * #getLayoutInfo(java.awt.Container, int) getLayoutInfo}。
                 * 此方法与 <code>getLayoutInfo</code> 相同；
                 * 有关参数和返回值的详细信息，请参阅 <code>getLayoutInfo</code>。
                 */
                protected GridBagLayoutInfo GetLayoutInfo(Container parent, int sizeflag) {
                    synchronized (parent.getTreeLock()) {
                        GridBagLayoutInfo r;
                        Component comp;
                        GridBagConstraints constraints;
                        Dimension d;
                        Component components[] = parent.getComponents();
                        // 以下代码将在 yMaxArray 和 weightY 中使用索引 curX+curWidth
                        // （分别为 xMaxArray 和 weightX 中的 curY+curHeight），其中
                        //  curX 在 0 到 preInitMaximumArraySizes.y 之间
                        // 因此，以下代码中可以计算的最大索引是 curX+curX。
                        // EmpericMultier 等于 2，因为这个原因。

                        int layoutWidth, layoutHeight;
                        int []xMaxArray;
                        int []yMaxArray;
                        int compindex, i, k, px, py, pixels_diff, nextSize;
                        int curX = 0; // constraints.gridx
                        int curY = 0; // constraints.gridy
                        int curWidth = 1;  // constraints.gridwidth
                        int curHeight = 1;  // constraints.gridheight
                        int curRow, curCol;
                        double weight_diff, weight;
                        int maximumArrayXIndex = 0;
                        int maximumArrayYIndex = 0;
                        int anchor;

                        /*
                         * 第一次遍历
                         *
                         * 确定布局网格的尺寸（对于零或负宽度和高度使用 1）。
                         */

                        layoutWidth = layoutHeight = 0;
                        curRow = curCol = -1;
                        long [] arraySizes = preInitMaximumArraySizes(parent);

                        /* 修复 4623196。
                         * 如果用户尝试创建一个非常大的网格，我们可能会
                         * 因为整数值溢出（EMPIRICMULTIPLIER*gridSize 可能大于 Integer.MAX_VALUE）而
                         * 遇到 NegativeArraySizeException。
                         * 我们需要检测这种情况，并尝试创建一个
                         * 大小为 Integer.MAX_VALUE 的网格。
                         */
                        maximumArrayXIndex = (EMPIRICMULTIPLIER * arraySizes[0] > Integer.MAX_VALUE )? Integer.MAX_VALUE : EMPIRICMULTIPLIER*(int)arraySizes[0];
                        maximumArrayYIndex = (EMPIRICMULTIPLIER * arraySizes[1] > Integer.MAX_VALUE )? Integer.MAX_VALUE : EMPIRICMULTIPLIER*(int)arraySizes[1];

                        if (rowHeights != null){
                            maximumArrayXIndex = Math.max(maximumArrayXIndex, rowHeights.length);
                        }
                        if (columnWidths != null){
                            maximumArrayYIndex = Math.max(maximumArrayYIndex, columnWidths.length);
                        }

                        xMaxArray = new int[maximumArrayXIndex];
                        yMaxArray = new int[maximumArrayYIndex];

                        boolean hasBaseline = false;
                        for (compindex = 0 ; compindex < components.length ; compindex++) {
                            comp = components[compindex];
                            if (!comp.isVisible())
                                continue;
                            constraints = lookupConstraints(comp);

                            curX = constraints.gridx;
                            curY = constraints.gridy;
                            curWidth = constraints.gridwidth;
                            if (curWidth <= 0)
                                curWidth = 1;
                            curHeight = constraints.gridheight;
                            if (curHeight <= 0)
                                curHeight = 1;

                            /* 如果 x 或 y 为负，则使用相对定位： */
                            if (curX < 0 && curY < 0) {
                                if (curRow >= 0)
                                    curY = curRow;
                                else if (curCol >= 0)
                                    curX = curCol;
                                else
                                    curY = 0;
                            }
                            if (curX < 0) {
                                px = 0;
                                for (i = curY; i < (curY + curHeight); i++) {
                                    px = Math.max(px, xMaxArray[i]);
                                }

                                curX = px - curX - 1;
                                if(curX < 0)
                                    curX = 0;
                            }
                            else if (curY < 0) {
                                py = 0;
                                for (i = curX; i < (curX + curWidth); i++) {
                                    py = Math.max(py, yMaxArray[i]);
                                }
                                curY = py - curY - 1;
                                if(curY < 0)
                                    curY = 0;
                            }

                            /* 调整网格宽度和高度
                             * 修复 5005945：移除了不必要的循环
                             */
                            px = curX + curWidth;
                            if (layoutWidth < px) {
                                layoutWidth = px;
                            }
                            py = curY + curHeight;
                            if (layoutHeight < py) {
                                layoutHeight = py;
                            }

                            /* 调整 xMaxArray 和 yMaxArray */
                            for (i = curX; i < (curX + curWidth); i++) {
                                yMaxArray[i] =py;
                            }
                            for (i = curY; i < (curY + curHeight); i++) {
                                xMaxArray[i] = px;
                            }


                            /* 缓存当前子组件的大小。 */
                            if (sizeflag == PREFERREDSIZE)
                                d = comp.getPreferredSize();
                            else
                                d = comp.getMinimumSize();
                            constraints.minWidth = d.width;
                            constraints.minHeight = d.height;
                            if (calculateBaseline(comp, constraints, d)) {
                                hasBaseline = true;
                            }

                            /* 零宽度和高度必须表示这是最后一个项目（否则有问题）。 */
                            if (constraints.gridheight == 0 && constraints.gridwidth == 0)
                                curRow = curCol = -1;

                            /* 零宽度开始新的一行 */
                            if (constraints.gridheight == 0 && curRow < 0)
                                curCol = curX + curWidth;

                            /* 零高度开始新的一列 */
                            else if (constraints.gridwidth == 0 && curCol < 0)
                                curRow = curY + curHeight;
                        } //for (components) loop


                        /*
                         * 应用最小行/列尺寸
                         */
                        if (columnWidths != null && layoutWidth < columnWidths.length)
                            layoutWidth = columnWidths.length;
                        if (rowHeights != null && layoutHeight < rowHeights.length)
                            layoutHeight = rowHeights.length;

                        r = new GridBagLayoutInfo(layoutWidth, layoutHeight);

                        /*
                         * 第二次遍历
                         *
                         * 负值的 gridX 用当前 x 值填充。
                         * 负值的 gridY 用当前 y 值填充。
                         * 负值或零值的 gridWidth 和 gridHeight 分别结束当前行或列。
                         */

                        curRow = curCol = -1;

                        Arrays.fill(xMaxArray, 0);
                        Arrays.fill(yMaxArray, 0);

                        int[] maxAscent = null;
                        int[] maxDescent = null;
                        short[] baselineType = null;

                        if (hasBaseline) {
                            r.maxAscent = maxAscent = new int[layoutHeight];
                            r.maxDescent = maxDescent = new int[layoutHeight];
                            r.baselineType = baselineType = new short[layoutHeight];
                            r.hasBaseline = true;
                        }


                        for (compindex = 0 ; compindex < components.length ; compindex++) {
                            comp = components[compindex];
                            if (!comp.isVisible())
                                continue;
                            constraints = lookupConstraints(comp);

                            curX = constraints.gridx;
                            curY = constraints.gridy;
                            curWidth = constraints.gridwidth;
                            curHeight = constraints.gridheight;

                            /* 如果 x 或 y 为负，则使用相对定位： */
                            if (curX < 0 && curY < 0) {
                                if(curRow >= 0)
                                    curY = curRow;
                                else if(curCol >= 0)
                                    curX = curCol;
                                else
                                    curY = 0;
                            }


                            if (curX < 0) {
                    if (curHeight <= 0) {
                        curHeight += r.height - curY;
                        if (curHeight < 1)
                            curHeight = 1;
                    }

                    px = 0;
                    for (i = curY; i < (curY + curHeight); i++)
                        px = Math.max(px, xMaxArray[i]);

                    curX = px - curX - 1;
                    if(curX < 0)
                        curX = 0;
                }
                else if (curY < 0) {
                    if (curWidth <= 0) {
                        curWidth += r.width - curX;
                        if (curWidth < 1)
                            curWidth = 1;
                    }

                    py = 0;
                    for (i = curX; i < (curX + curWidth); i++){
                        py = Math.max(py, yMaxArray[i]);
                    }

                    curY = py - curY - 1;
                    if(curY < 0)
                        curY = 0;
                }

                if (curWidth <= 0) {
                    curWidth += r.width - curX;
                    if (curWidth < 1)
                        curWidth = 1;
                }

                if (curHeight <= 0) {
                    curHeight += r.height - curY;
                    if (curHeight < 1)
                        curHeight = 1;
                }

                px = curX + curWidth;
                py = curY + curHeight;

                for (i = curX; i < (curX + curWidth); i++) { yMaxArray[i] = py; }
                for (i = curY; i < (curY + curHeight); i++) { xMaxArray[i] = px; }

                /* 使负尺寸开始新的一行/列 */
                if (constraints.gridheight == 0 && constraints.gridwidth == 0)
                    curRow = curCol = -1;
                if (constraints.gridheight == 0 && curRow < 0)
                    curCol = curX + curWidth;
                else if (constraints.gridwidth == 0 && curCol < 0)
                    curRow = curY + curHeight;

                /* 为 gridbag 从属分配新值 */
                constraints.tempX = curX;
                constraints.tempY = curY;
                constraints.tempWidth = curWidth;
                constraints.tempHeight = curHeight;

                anchor = constraints.anchor;
                if (hasBaseline) {
                    switch(anchor) {
                    case GridBagConstraints.BASELINE:
                    case GridBagConstraints.BASELINE_LEADING:
                    case GridBagConstraints.BASELINE_TRAILING:
                        if (constraints.ascent >= 0) {
                            if (curHeight == 1) {
                                maxAscent[curY] =
                                        Math.max(maxAscent[curY],
                                                 constraints.ascent);
                                maxDescent[curY] =
                                        Math.max(maxDescent[curY],
                                                 constraints.descent);
                            }
                            else {
                                if (constraints.baselineResizeBehavior ==
                                        Component.BaselineResizeBehavior.
                                        CONSTANT_DESCENT) {
                                    maxDescent[curY + curHeight - 1] =
                                        Math.max(maxDescent[curY + curHeight
                                                            - 1],
                                                 constraints.descent);
                                }
                                else {
                                    maxAscent[curY] = Math.max(maxAscent[curY],
                                                           constraints.ascent);
                                }
                            }
                            if (constraints.baselineResizeBehavior ==
                                    Component.BaselineResizeBehavior.CONSTANT_DESCENT) {
                                baselineType[curY + curHeight - 1] |=
                                        (1 << constraints.
                                         baselineResizeBehavior.ordinal());
                            }
                            else {
                                baselineType[curY] |= (1 << constraints.
                                             baselineResizeBehavior.ordinal());
                            }
                        }
                        break;
                    case GridBagConstraints.ABOVE_BASELINE:
                    case GridBagConstraints.ABOVE_BASELINE_LEADING:
                    case GridBagConstraints.ABOVE_BASELINE_TRAILING:
                        // 组件位于基线之上。
                        // 为了使组件的底部边缘与基线对齐，
                        // 底部内边距被添加到下降距离，其余部分添加到上升距离。
                        pixels_diff = constraints.minHeight +
                                constraints.insets.top +
                                constraints.ipady;
                        maxAscent[curY] = Math.max(maxAscent[curY],
                                                   pixels_diff);
                        maxDescent[curY] = Math.max(maxDescent[curY],
                                                    constraints.insets.bottom);
                        break;
                    case GridBagConstraints.BELOW_BASELINE:
                    case GridBagConstraints.BELOW_BASELINE_LEADING:
                    case GridBagConstraints.BELOW_BASELINE_TRAILING:
                        // 组件位于基线之下。
                        // 为了使组件的顶部边缘与基线对齐，
                        // 顶部内边距被添加到上升距离，其余部分添加到下降距离。
                        pixels_diff = constraints.minHeight +
                                constraints.insets.bottom + constraints.ipady;
                        maxDescent[curY] = Math.max(maxDescent[curY],
                                                    pixels_diff);
                        maxAscent[curY] = Math.max(maxAscent[curY],
                                                   constraints.insets.top);
                        break;
                    }
                }
            }

            r.weightX = new double[maximumArrayYIndex];
            r.weightY = new double[maximumArrayXIndex];
            r.minWidth = new int[maximumArrayYIndex];
            r.minHeight = new int[maximumArrayXIndex];


            /*
             * 应用最小行/列尺寸和权重
             */
            if (columnWidths != null)
                System.arraycopy(columnWidths, 0, r.minWidth, 0, columnWidths.length);
            if (rowHeights != null)
                System.arraycopy(rowHeights, 0, r.minHeight, 0, rowHeights.length);
            if (columnWeights != null)
                System.arraycopy(columnWeights, 0, r.weightX, 0,  Math.min(r.weightX.length, columnWeights.length));
            if (rowWeights != null)
                System.arraycopy(rowWeights, 0, r.weightY, 0,  Math.min(r.weightY.length, rowWeights.length));

            /*
             * 第三遍
             *
             * 分配最小宽度和权重：
             */

            nextSize = Integer.MAX_VALUE;

            for (i = 1;
                 i != Integer.MAX_VALUE;
                 i = nextSize, nextSize = Integer.MAX_VALUE) {
                for (compindex = 0 ; compindex < components.length ; compindex++) {
                    comp = components[compindex];
                    if (!comp.isVisible())
                        continue;
                    constraints = lookupConstraints(comp);

                    if (constraints.tempWidth == i) {
                        px = constraints.tempX + constraints.tempWidth; /* 右列 */

                        /*
                         * 确定是否应使用此从属的权重。如果权重小于由单元格宽度跨越的总权重，
                         * 则丢弃该权重。否则根据现有权重分配差异。
                         */

                        weight_diff = constraints.weightx;
                        for (k = constraints.tempX; k < px; k++)
                            weight_diff -= r.weightX[k];
                        if (weight_diff > 0.0) {
                            weight = 0.0;
                            for (k = constraints.tempX; k < px; k++)
                                weight += r.weightX[k];
                            for (k = constraints.tempX; weight > 0.0 && k < px; k++) {
                                double wt = r.weightX[k];
                                double dx = (wt * weight_diff) / weight;
                                r.weightX[k] += dx;
                                weight_diff -= dx;
                                weight -= wt;
                            }
                            /* 将剩余部分分配给最右列 */
                            r.weightX[px-1] += weight_diff;
                        }

                        /*
                         * 计算 minWidth 数组值。
                         * 首先，确定当前从属需要多宽。
                         * 然后，查看它是否能适应当前的 minWidth 值。
                         * 如果不能适应，根据 weightX 数组添加差异。
                         */

                        pixels_diff =
                            constraints.minWidth + constraints.ipadx +
                            constraints.insets.left + constraints.insets.right;

                        for (k = constraints.tempX; k < px; k++)
                            pixels_diff -= r.minWidth[k];
                        if (pixels_diff > 0) {
                            weight = 0.0;
                            for (k = constraints.tempX; k < px; k++)
                                weight += r.weightX[k];
                            for (k = constraints.tempX; weight > 0.0 && k < px; k++) {
                                double wt = r.weightX[k];
                                int dx = (int)((wt * ((double)pixels_diff)) / weight);
                                r.minWidth[k] += dx;
                                pixels_diff -= dx;
                                weight -= wt;
                            }
                            /* 任何剩余部分进入最右列 */
                            r.minWidth[px-1] += pixels_diff;
                        }
                    }
                    else if (constraints.tempWidth > i && constraints.tempWidth < nextSize)
                        nextSize = constraints.tempWidth;


                    if (constraints.tempHeight == i) {
                        py = constraints.tempY + constraints.tempHeight; /* 底行 */

                        /*
                         * 确定是否应使用此从属的权重。如果权重小于由单元格高度跨越的总权重，
                         * 则丢弃该权重。否则根据现有权重分配差异。
                         */

                        weight_diff = constraints.weighty;
                        for (k = constraints.tempY; k < py; k++)
                            weight_diff -= r.weightY[k];
                        if (weight_diff > 0.0) {
                            weight = 0.0;
                            for (k = constraints.tempY; k < py; k++)
                                weight += r.weightY[k];
                            for (k = constraints.tempY; weight > 0.0 && k < py; k++) {
                                double wt = r.weightY[k];
                                double dy = (wt * weight_diff) / weight;
                                r.weightY[k] += dy;
                                weight_diff -= dy;
                                weight -= wt;
                            }
                            /* 将剩余部分分配给最底行 */
                            r.weightY[py-1] += weight_diff;
                        }

                        /*
                         * 计算 minHeight 数组值。
                         * 首先，确定当前从属需要多高。
                         * 然后，查看它是否能适应当前的 minHeight 值。
                         * 如果不能适应，根据 weightY 数组添加差异。
                         */

                        pixels_diff = -1;
                        if (hasBaseline) {
                            switch(constraints.anchor) {
                            case GridBagConstraints.BASELINE:
                            case GridBagConstraints.BASELINE_LEADING:
                            case GridBagConstraints.BASELINE_TRAILING:
                                if (constraints.ascent >= 0) {
                                    if (constraints.tempHeight == 1) {
                                        pixels_diff =
                                            maxAscent[constraints.tempY] +
                                            maxDescent[constraints.tempY];
                                    }
                                    else if (constraints.baselineResizeBehavior !=
                                             Component.BaselineResizeBehavior.
                                             CONSTANT_DESCENT) {
                                        pixels_diff =
                                                maxAscent[constraints.tempY] +
                                                constraints.descent;
                                    }
                                    else {
                                        pixels_diff = constraints.ascent +
                                                maxDescent[constraints.tempY +
                                                   constraints.tempHeight - 1];
                                    }
                                }
                                break;
                            case GridBagConstraints.ABOVE_BASELINE:
                            case GridBagConstraints.ABOVE_BASELINE_LEADING:
                            case GridBagConstraints.ABOVE_BASELINE_TRAILING:
                                pixels_diff = constraints.insets.top +
                                        constraints.minHeight +
                                        constraints.ipady +
                                        maxDescent[constraints.tempY];
                                break;
                            case GridBagConstraints.BELOW_BASELINE:
                            case GridBagConstraints.BELOW_BASELINE_LEADING:
                            case GridBagConstraints.BELOW_BASELINE_TRAILING:
                                pixels_diff = maxAscent[constraints.tempY] +
                                        constraints.minHeight +
                                        constraints.insets.bottom +
                                        constraints.ipady;
                                break;
                            }
                        }
                        if (pixels_diff == -1) {
                            pixels_diff =
                                constraints.minHeight + constraints.ipady +
                                constraints.insets.top +
                                constraints.insets.bottom;
                        }
                        for (k = constraints.tempY; k < py; k++)
                            pixels_diff -= r.minHeight[k];
                        if (pixels_diff > 0) {
                            weight = 0.0;
                            for (k = constraints.tempY; k < py; k++)
                                weight += r.weightY[k];
                            for (k = constraints.tempY; weight > 0.0 && k < py; k++) {
                                double wt = r.weightY[k];
                                int dy = (int)((wt * ((double)pixels_diff)) / weight);
                                r.minHeight[k] += dy;
                                pixels_diff -= dy;
                                weight -= wt;
                            }
                            /* 任何剩余部分进入最底行 */
                            r.minHeight[py-1] += pixels_diff;
                        }
                    }
                    else if (constraints.tempHeight > i &&
                             constraints.tempHeight < nextSize)
                        nextSize = constraints.tempHeight;
                }
            }
            return r;
        }
    } //getLayoutInfo()


                /**
     * 计算指定组件的基线。
     * 如果组件 {@code c} 沿其基线定位，则获取基线并将 {@code constraints} 的上升、下降和基线调整行为从组件中设置，并返回 true。否则返回 false。
     */
    private boolean calculateBaseline(Component c,
                                      GridBagConstraints constraints,
                                      Dimension size) {
        int anchor = constraints.anchor;
        if (anchor == GridBagConstraints.BASELINE ||
                anchor == GridBagConstraints.BASELINE_LEADING ||
                anchor == GridBagConstraints.BASELINE_TRAILING) {
            // 应用内边距到组件，然后请求基线。
            int w = size.width + constraints.ipadx;
            int h = size.height + constraints.ipady;
            constraints.ascent = c.getBaseline(w, h);
            if (constraints.ascent >= 0) {
                // 组件有基线
                int baseline = constraints.ascent;
                // 调整上升和下降以包括内边距。
                constraints.descent = h - constraints.ascent +
                            constraints.insets.bottom;
                constraints.ascent += constraints.insets.top;
                constraints.baselineResizeBehavior =
                        c.getBaselineResizeBehavior();
                constraints.centerPadding = 0;
                if (constraints.baselineResizeBehavior == Component.
                        BaselineResizeBehavior.CENTER_OFFSET) {
                    // 组件的基线调整行为为 CENTER_OFFSET，计算 centerPadding 和 centerOffset
                    // （请参阅枚举中的 CENTER_OFFSET 描述以了解此算法的详细信息）。
                    int nextBaseline = c.getBaseline(w, h + 1);
                    constraints.centerOffset = baseline - h / 2;
                    if (h % 2 == 0) {
                        if (baseline != nextBaseline) {
                            constraints.centerPadding = 1;
                        }
                    }
                    else if (baseline == nextBaseline){
                        constraints.centerOffset--;
                        constraints.centerPadding = 1;
                    }
                }
            }
            return true;
        }
        else {
            constraints.ascent = -1;
            return false;
        }
    }

    /**
     * 根据约束几何和填充调整 x、y、width 和 height 字段到正确的值。
     * 该方法仅供 <code>GridBagLayout</code> 内部使用。
     *
     * @param constraints 要应用的约束
     * @param r 要调整的 <code>Rectangle</code>
     * @since 1.4
     */
    protected void adjustForGravity(GridBagConstraints constraints,
                                    Rectangle r) {
        AdjustForGravity(constraints, r);
    }

    /**
     * 该方法已过时，仅为了向后兼容而提供；新代码应调用 {@link
     * #adjustForGravity(java.awt.GridBagConstraints, java.awt.Rectangle)
     * adjustForGravity} 代替。
     * 该方法与 <code>adjustForGravity</code> 相同；
     * 有关参数的详细信息，请参阅 <code>adjustForGravity</code>。
     */
    protected void AdjustForGravity(GridBagConstraints constraints,
                                    Rectangle r) {
        int diffx, diffy;
        int cellY = r.y;
        int cellHeight = r.height;

        if (!rightToLeft) {
            r.x += constraints.insets.left;
        } else {
            r.x -= r.width - constraints.insets.right;
        }
        r.width -= (constraints.insets.left + constraints.insets.right);
        r.y += constraints.insets.top;
        r.height -= (constraints.insets.top + constraints.insets.bottom);

        diffx = 0;
        if ((constraints.fill != GridBagConstraints.HORIZONTAL &&
             constraints.fill != GridBagConstraints.BOTH)
            && (r.width > (constraints.minWidth + constraints.ipadx))) {
            diffx = r.width - (constraints.minWidth + constraints.ipadx);
            r.width = constraints.minWidth + constraints.ipadx;
        }

        diffy = 0;
        if ((constraints.fill != GridBagConstraints.VERTICAL &&
             constraints.fill != GridBagConstraints.BOTH)
            && (r.height > (constraints.minHeight + constraints.ipady))) {
            diffy = r.height - (constraints.minHeight + constraints.ipady);
            r.height = constraints.minHeight + constraints.ipady;
        }

        switch (constraints.anchor) {
          case GridBagConstraints.BASELINE:
              r.x += diffx/2;
              alignOnBaseline(constraints, r, cellY, cellHeight);
              break;
          case GridBagConstraints.BASELINE_LEADING:
              if (rightToLeft) {
                  r.x += diffx;
              }
              alignOnBaseline(constraints, r, cellY, cellHeight);
              break;
          case GridBagConstraints.BASELINE_TRAILING:
              if (!rightToLeft) {
                  r.x += diffx;
              }
              alignOnBaseline(constraints, r, cellY, cellHeight);
              break;
          case GridBagConstraints.ABOVE_BASELINE:
              r.x += diffx/2;
              alignAboveBaseline(constraints, r, cellY, cellHeight);
              break;
          case GridBagConstraints.ABOVE_BASELINE_LEADING:
              if (rightToLeft) {
                  r.x += diffx;
              }
              alignAboveBaseline(constraints, r, cellY, cellHeight);
              break;
          case GridBagConstraints.ABOVE_BASELINE_TRAILING:
              if (!rightToLeft) {
                  r.x += diffx;
              }
              alignAboveBaseline(constraints, r, cellY, cellHeight);
              break;
          case GridBagConstraints.BELOW_BASELINE:
              r.x += diffx/2;
              alignBelowBaseline(constraints, r, cellY, cellHeight);
              break;
          case GridBagConstraints.BELOW_BASELINE_LEADING:
              if (rightToLeft) {
                  r.x += diffx;
              }
              alignBelowBaseline(constraints, r, cellY, cellHeight);
              break;
          case GridBagConstraints.BELOW_BASELINE_TRAILING:
              if (!rightToLeft) {
                  r.x += diffx;
              }
              alignBelowBaseline(constraints, r, cellY, cellHeight);
              break;
          case GridBagConstraints.CENTER:
              r.x += diffx/2;
              r.y += diffy/2;
              break;
          case GridBagConstraints.PAGE_START:
          case GridBagConstraints.NORTH:
              r.x += diffx/2;
              break;
          case GridBagConstraints.NORTHEAST:
              r.x += diffx;
              break;
          case GridBagConstraints.EAST:
              r.x += diffx;
              r.y += diffy/2;
              break;
          case GridBagConstraints.SOUTHEAST:
              r.x += diffx;
              r.y += diffy;
              break;
          case GridBagConstraints.PAGE_END:
          case GridBagConstraints.SOUTH:
              r.x += diffx/2;
              r.y += diffy;
              break;
          case GridBagConstraints.SOUTHWEST:
              r.y += diffy;
              break;
          case GridBagConstraints.WEST:
              r.y += diffy/2;
              break;
          case GridBagConstraints.NORTHWEST:
              break;
          case GridBagConstraints.LINE_START:
              if (rightToLeft) {
                  r.x += diffx;
              }
              r.y += diffy/2;
              break;
          case GridBagConstraints.LINE_END:
              if (!rightToLeft) {
                  r.x += diffx;
              }
              r.y += diffy/2;
              break;
          case GridBagConstraints.FIRST_LINE_START:
              if (rightToLeft) {
                  r.x += diffx;
              }
              break;
          case GridBagConstraints.FIRST_LINE_END:
              if (!rightToLeft) {
                  r.x += diffx;
              }
              break;
          case GridBagConstraints.LAST_LINE_START:
              if (rightToLeft) {
                  r.x += diffx;
              }
              r.y += diffy;
              break;
          case GridBagConstraints.LAST_LINE_END:
              if (!rightToLeft) {
                  r.x += diffx;
              }
              r.y += diffy;
              break;
          default:
              throw new IllegalArgumentException("非法的 anchor 值");
        }
    }

    /**
     * 定位在基线上。
     *
     * @param cellY 行的位置，不包括内边距
     * @param cellHeight 行的高度，不考虑内边距
     * @param r 组件的可用边界，由内边距和 ipady 填充
     */
    private void alignOnBaseline(GridBagConstraints cons, Rectangle r,
                                 int cellY, int cellHeight) {
        if (cons.ascent >= 0) {
            if (cons.baselineResizeBehavior == Component.
                    BaselineResizeBehavior.CONSTANT_DESCENT) {
                // 锚定到底部。
                // 基线位于 (cellY + cellHeight - maxDescent)。
                // 组件的底部 (maxY) 位于基线 + 组件的下降。这里需要减去底部内边距
                // 因为 constraints 对象中的下降包括底部内边距。
                int maxY = cellY + cellHeight -
                      layoutInfo.maxDescent[cons.tempY + cons.tempHeight - 1] +
                      cons.descent - cons.insets.bottom;
                if (!cons.isVerticallyResizable()) {
                    // 组件不可调整大小，计算从 maxY - height 的 y 位置。
                    r.y = maxY - cons.minHeight;
                    r.height = cons.minHeight;
                } else {
                    // 组件可调整大小。由于 brb 是 constant descent，
                    // 可以扩展组件以填充基线以上的区域。
                    // 减去顶部内边距以确保组件的内边距得到尊重。
                    r.height = maxY - cellY - cons.insets.top;
                }
            }
            else {
                // BRB 不是 constant_descent
                int baseline; // 行的基线，相对于 cellY
                // 组件的基线，包括顶部内边距
                int ascent = cons.ascent;
                if (layoutInfo.hasConstantDescent(cons.tempY)) {
                    // 同一行中有混合的上升/下降，根据 maxDescent 计算位置
                    baseline = cellHeight - layoutInfo.maxDescent[cons.tempY];
                }
                else {
                    // 本行中只有上升/未知，锚定到顶部
                    baseline = layoutInfo.maxAscent[cons.tempY];
                }
                if (cons.baselineResizeBehavior == Component.
                        BaselineResizeBehavior.OTHER) {
                    // BRB 是 other，这意味着我们只能通过再次请求基线来确定基线
                    // 给定我们计划用于组件的大小。
                    boolean fits = false;
                    ascent = componentAdjusting.getBaseline(r.width, r.height);
                    if (ascent >= 0) {
                        // 组件有基线，加上顶部内边距
                        // （这遵循 calculateBaseline 中的相同操作）。
                        ascent += cons.insets.top;
                    }
                    if (ascent >= 0 && ascent <= baseline) {
                        // 组件的基线适合行的基线。
                        // 确保下降也适合空间。
                        if (baseline + (r.height - ascent - cons.insets.top) <=
                                cellHeight - cons.insets.bottom) {
                            // 它适合，我们很好。
                            fits = true;
                        }
                        else if (cons.isVerticallyResizable()) {
                            // 不适合，但它是可调整大小的。假设我们会再次得到上升。
                            int ascent2 = componentAdjusting.getBaseline(
                                    r.width, cellHeight - cons.insets.bottom -
                                    baseline + ascent);
                            if (ascent2 >= 0) {
                                ascent2 += cons.insets.top;
                            }
                            if (ascent2 >= 0 && ascent2 <= ascent) {
                                // 它会适合
                                r.height = cellHeight - cons.insets.bottom -
                                        baseline + ascent;
                                ascent = ascent2;
                                fits = true;
                            }
                        }
                    }
                    if (!fits) {
                        // 不适合，使用最小大小和原始上升
                        ascent = cons.ascent;
                        r.width = cons.minWidth;
                        r.height = cons.minHeight;
                    }
                }
                // 根据组件的上升和行的基线重置组件的 y 位置。因为上升包括基线
                r.y = cellY + baseline - ascent + cons.insets.top;
                if (cons.isVerticallyResizable()) {
                    switch(cons.baselineResizeBehavior) {
                    case CONSTANT_ASCENT:
                        r.height = Math.max(cons.minHeight,cellY + cellHeight -
                                            r.y - cons.insets.bottom);
                        break;
                    case CENTER_OFFSET:
                        {
                            int upper = r.y - cellY - cons.insets.top;
                            int lower = cellY + cellHeight - r.y -
                                cons.minHeight - cons.insets.bottom;
                            int delta = Math.min(upper, lower);
                            delta += delta;
                            if (delta > 0 &&
                                (cons.minHeight + cons.centerPadding +
                                 delta) / 2 + cons.centerOffset != baseline) {
                                // 偏差 1
                                delta--;
                            }
                            r.height = cons.minHeight + delta;
                            r.y = cellY + baseline -
                                (r.height + cons.centerPadding) / 2 -
                                cons.centerOffset;
                        }
                        break;
                    case OTHER:
                        // 已在上面处理
                        break;
                    default:
                        break;
                    }
                }
            }
        }
        else {
            centerVertically(cons, r, cellHeight);
        }
    }


                /**
     * 将指定的组件定位在基线之上。也就是说，
     * 组件的底部边缘将与基线对齐。
     * 如果行没有基线，则此方法将组件居中。
     */
    private void alignAboveBaseline(GridBagConstraints cons, Rectangle r,
                                    int cellY, int cellHeight) {
        if (layoutInfo.hasBaseline(cons.tempY)) {
            int maxY; // 行的基线
            if (layoutInfo.hasConstantDescent(cons.tempY)) {
                // 优先使用下降
                maxY = cellY + cellHeight - layoutInfo.maxDescent[cons.tempY];
            }
            else {
                // 优先使用上升
                maxY = cellY + layoutInfo.maxAscent[cons.tempY];
            }
            if (cons.isVerticallyResizable()) {
                // 组件可调整大小。顶部边缘偏移顶部
                // 内边距，底部边缘在基线上。
                r.y = cellY + cons.insets.top;
                r.height = maxY - r.y;
            }
            else {
                // 不可调整大小。
                r.height = cons.minHeight + cons.ipady;
                r.y = maxY - r.height;
            }
        }
        else {
            centerVertically(cons, r, cellHeight);
        }
    }

    /**
     * 定位在基线下方。
     */
    private void alignBelowBaseline(GridBagConstraints cons, Rectangle r,
                                    int cellY, int cellHeight) {
        if (layoutInfo.hasBaseline(cons.tempY)) {
            if (layoutInfo.hasConstantDescent(cons.tempY)) {
                // 优先使用下降
                r.y = cellY + cellHeight - layoutInfo.maxDescent[cons.tempY];
            }
            else {
                // 优先使用上升
                r.y = cellY + layoutInfo.maxAscent[cons.tempY];
            }
            if (cons.isVerticallyResizable()) {
                r.height = cellY + cellHeight - r.y - cons.insets.bottom;
            }
        }
        else {
            centerVertically(cons, r, cellHeight);
        }
    }

    private void centerVertically(GridBagConstraints cons, Rectangle r,
                                  int cellHeight) {
        if (!cons.isVerticallyResizable()) {
            r.y += Math.max(0, (cellHeight - cons.insets.top -
                                cons.insets.bottom - cons.minHeight -
                                cons.ipady) / 2);
        }
    }

    /**
     * 根据 <code>getLayoutInfo</code> 提供的信息计算主容器的最小尺寸。
     * 此方法仅供 <code>GridBagLayout</code> 内部使用。
     *
     * @param parent 布局容器
     * @param info 该父容器的布局信息
     * @return 包含最小尺寸的 <code>Dimension</code> 对象
     * @since 1.4
     */
    protected Dimension getMinSize(Container parent, GridBagLayoutInfo info) {
        return GetMinSize(parent, info);
    }

    /**
     * 此方法已过时，仅为了向后兼容而提供；新代码应调用 {@link
     * #getMinSize(java.awt.Container, GridBagLayoutInfo) getMinSize}。
     * 此方法与 <code>getMinSize</code> 相同；
     * 有关参数和返回值的详细信息，请参阅 <code>getMinSize</code>。
     */
    protected Dimension GetMinSize(Container parent, GridBagLayoutInfo info) {
        Dimension d = new Dimension();
        int i, t;
        Insets insets = parent.getInsets();

        t = 0;
        for(i = 0; i < info.width; i++)
            t += info.minWidth[i];
        d.width = t + insets.left + insets.right;

        t = 0;
        for(i = 0; i < info.height; i++)
            t += info.minHeight[i];
        d.height = t + insets.top + insets.bottom;

        return d;
    }

    transient boolean rightToLeft = false;

    /**
     * 布局网格。
     * 此方法仅供 <code>GridBagLayout</code> 内部使用。
     *
     * @param parent 布局容器
     * @since 1.4
     */
    protected void arrangeGrid(Container parent) {
        ArrangeGrid(parent);
    }

    /**
     * 此方法已过时，仅为了向后兼容而提供；新代码应调用 {@link
     * #arrangeGrid(Container) arrangeGrid}。
     * 此方法与 <code>arrangeGrid</code> 相同；
     * 有关参数的详细信息，请参阅 <code>arrangeGrid</code>。
     */
    protected void ArrangeGrid(Container parent) {
        Component comp;
        int compindex;
        GridBagConstraints constraints;
        Insets insets = parent.getInsets();
        Component components[] = parent.getComponents();
        Dimension d;
        Rectangle r = new Rectangle();
        int i, diffw, diffh;
        double weight;
        GridBagLayoutInfo info;

        rightToLeft = !parent.getComponentOrientation().isLeftToRight();

        /*
         * 如果父容器没有任何子组件，则不执行任何操作：
         * 只保留父容器的当前尺寸。
         */
        if (components.length == 0 &&
            (columnWidths == null || columnWidths.length == 0) &&
            (rowHeights == null || rowHeights.length == 0)) {
            return;
        }

        /*
         * 第一次遍历：扫描所有子组件以确定所需的空间总量。
         */

        info = getLayoutInfo(parent, PREFERREDSIZE);
        d = getMinSize(parent, info);

        if (parent.width < d.width || parent.height < d.height) {
            info = getLayoutInfo(parent, MINSIZE);
            d = getMinSize(parent, info);
        }

        layoutInfo = info;
        r.width = d.width;
        r.height = d.height;

        /*
         * 调试
         *
         * DumpLayoutInfo(info);
         * for (compindex = 0 ; compindex < components.length ; compindex++) {
         * comp = components[compindex];
         * if (!comp.isVisible())
         *      continue;
         * constraints = lookupConstraints(comp);
         * DumpConstraints(constraints);
         * }
         * System.out.println("minSize " + r.width + " " + r.height);
         */

        /*
         * 如果当前窗口尺寸与所需尺寸不匹配，则根据权重调整 minWidth 和 minHeight 数组。
         */

        diffw = parent.width - r.width;
        if (diffw != 0) {
            weight = 0.0;
            for (i = 0; i < info.width; i++)
                weight += info.weightX[i];
            if (weight > 0.0) {
                for (i = 0; i < info.width; i++) {
                    int dx = (int)(( ((double)diffw) * info.weightX[i]) / weight);
                    info.minWidth[i] += dx;
                    r.width += dx;
                    if (info.minWidth[i] < 0) {
                        r.width -= info.minWidth[i];
                        info.minWidth[i] = 0;
                    }
                }
            }
            diffw = parent.width - r.width;
        }

        else {
            diffw = 0;
        }

        diffh = parent.height - r.height;
        if (diffh != 0) {
            weight = 0.0;
            for (i = 0; i < info.height; i++)
                weight += info.weightY[i];
            if (weight > 0.0) {
                for (i = 0; i < info.height; i++) {
                    int dy = (int)(( ((double)diffh) * info.weightY[i]) / weight);
                    info.minHeight[i] += dy;
                    r.height += dy;
                    if (info.minHeight[i] < 0) {
                        r.height -= info.minHeight[i];
                        info.minHeight[i] = 0;
                    }
                }
            }
            diffh = parent.height - r.height;
        }

        else {
            diffh = 0;
        }

        /*
         * 调试
         *
         * System.out.println("Re-adjusted:");
         * DumpLayoutInfo(info);
         */

        /*
         * 现在使用收集的布局信息实际布局子组件。
         */

        info.startx = diffw/2 + insets.left;
        info.starty = diffh/2 + insets.top;

        for (compindex = 0 ; compindex < components.length ; compindex++) {
            comp = components[compindex];
            if (!comp.isVisible()){
                continue;
            }
            constraints = lookupConstraints(comp);

            if (!rightToLeft) {
                r.x = info.startx;
                for(i = 0; i < constraints.tempX; i++)
                    r.x += info.minWidth[i];
            } else {
                r.x = parent.width - (diffw/2 + insets.right);
                for(i = 0; i < constraints.tempX; i++)
                    r.x -= info.minWidth[i];
            }

            r.y = info.starty;
            for(i = 0; i < constraints.tempY; i++)
                r.y += info.minHeight[i];

            r.width = 0;
            for(i = constraints.tempX;
                i < (constraints.tempX + constraints.tempWidth);
                i++) {
                r.width += info.minWidth[i];
            }

            r.height = 0;
            for(i = constraints.tempY;
                i < (constraints.tempY + constraints.tempHeight);
                i++) {
                r.height += info.minHeight[i];
            }

            componentAdjusting = comp;
            adjustForGravity(constraints, r);

            /* 修复 4408108 - 组件在容器外部创建 */
            /* 修复 4969409 "-" 替换为 "+"  */
            if (r.x < 0) {
                r.width += r.x;
                r.x = 0;
            }

            if (r.y < 0) {
                r.height += r.y;
                r.y = 0;
            }

            /*
             * 如果窗口太小而无法显示内容，则取消映射。
             * 否则配置并确保映射。
             */

            if ((r.width <= 0) || (r.height <= 0)) {
                comp.setBounds(0, 0, 0, 0);
            }
            else {
                if (comp.x != r.x || comp.y != r.y ||
                    comp.width != r.width || comp.height != r.height) {
                    comp.setBounds(r.x, r.y, r.width, r.height);
                }
            }
        }
    }

    // 为序列化向后兼容添加（4348425）
    static final long serialVersionUID = 8838754796412211005L;
}
