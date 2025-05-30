
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
 * <code>GridLayout</code> 类是一个布局管理器，它将容器的组件以矩形网格的形式排列。
 * 容器被划分为大小相等的矩形，每个矩形中放置一个组件。
 * 例如，以下是一个将六个按钮排列成三行两列的应用程序：
 *
 * <hr><blockquote>
 * <pre>
 * import java.awt.*;
 * import java.applet.Applet;
 * public class ButtonGrid extends Applet {
 *     public void init() {
 *         setLayout(new GridLayout(3,2));
 *         add(new Button("1"));
 *         add(new Button("2"));
 *         add(new Button("3"));
 *         add(new Button("4"));
 *         add(new Button("5"));
 *         add(new Button("6"));
 *     }
 * }
 * </pre></blockquote><hr>
 * <p>
 * 如果容器的 <code>ComponentOrientation</code> 属性是水平且从左到右，上述示例将产生图1所示的输出。
 * 如果容器的 <code>ComponentOrientation</code> 属性是水平且从右到左，示例将产生图2所示的输出。
 *
 * <table style="float:center" WIDTH=600 summary="layout">
 * <tr ALIGN=CENTER>
 * <td><img SRC="doc-files/GridLayout-1.gif"
 *      alt="显示6个按钮，每行2个。第1行显示按钮1然后2。
 * 第2行显示按钮3然后4。第3行显示按钮5然后6。">
 * </td>
 *
 * <td ALIGN=CENTER><img SRC="doc-files/GridLayout-2.gif"
 *                   alt="显示6个按钮，每行2个。第1行显示按钮2然后1。
 * 第2行显示按钮4然后3。第3行显示按钮6然后5。">
 * </td>
 * </tr>
 *
 * <tr ALIGN=CENTER>
 * <td>图1：水平，从左到右</td>
 *
 * <td>图2：水平，从右到左</td>
 * </tr>
 * </table>
 * <p>
 * 当行数和列数都已设置为非零值时，无论是通过构造函数还是通过 <tt>setRows</tt> 和 <tt>setColumns</tt> 方法设置，
 * 指定的列数将被忽略。相反，列数将根据指定的行数和布局中的组件总数来确定。
 * 例如，如果指定了三行两列，并且添加了九个组件到布局中，它们将显示为三行三列。
 * 指定列数仅在行数设置为零时才会影响布局。
 *
 * @author  Arthur van Hoff
 * @since   JDK1.0
 */
public class GridLayout implements LayoutManager, java.io.Serializable {
    /*
     * serialVersionUID
     */
    private static final long serialVersionUID = -7411804673224730901L;

    /**
     * 这是水平间距（以像素为单位），用于指定列之间的空间。它们可以随时更改。
     * 这应该是一个非负整数。
     *
     * @serial
     * @see #getHgap()
     * @see #setHgap(int)
     */
    int hgap;
    /**
     * 这是垂直间距（以像素为单位），用于指定行之间的空间。它们可以随时更改。
     * 这应该是一个非负整数。
     *
     * @serial
     * @see #getVgap()
     * @see #setVgap(int)
     */
    int vgap;
    /**
     * 这是为网格指定的行数。行数可以随时更改。
     * 这应该是一个非负整数，其中 '0' 表示“任意数量”，意味着该维度中的行数取决于另一个维度。
     *
     * @serial
     * @see #getRows()
     * @see #setRows(int)
     */
    int rows;
    /**
     * 这是为网格指定的列数。列数可以随时更改。
     * 这应该是一个非负整数，其中 '0' 表示“任意数量”，意味着该维度中的列数取决于另一个维度。
     *
     * @serial
     * @see #getColumns()
     * @see #setColumns(int)
     */
    int cols;

    /**
     * 创建一个默认的网格布局，每个组件占一列，位于单行中。
     * @since JDK1.1
     */
    public GridLayout() {
        this(1, 0, 0, 0);
    }

    /**
     * 创建一个具有指定行数和列数的网格布局。布局中的所有组件大小相等。
     * <p>
     * <code>rows</code> 和 <code>cols</code> 中的一个可以为零，但不能同时为零，这意味着可以在一行或一列中放置任意数量的对象。
     * @param     rows   行数，值为零表示任意数量的行。
     * @param     cols   列数，值为零表示任意数量的列。
     */
    public GridLayout(int rows, int cols) {
        this(rows, cols, 0, 0);
    }

    /**
     * 创建一个具有指定行数和列数的网格布局。布局中的所有组件大小相等。
     * <p>
     * 此外，水平间距和垂直间距被设置为指定的值。水平间距放置在每列之间。垂直间距放置在每行之间。
     * <p>
     * <code>rows</code> 和 <code>cols</code> 中的一个可以为零，但不能同时为零，这意味着可以在一行或一列中放置任意数量的对象。
     * <p>
     * 所有 <code>GridLayout</code> 构造函数都委托给此构造函数。
     * @param     rows   行数，值为零表示任意数量的行
     * @param     cols   列数，值为零表示任意数量的列
     * @param     hgap   水平间距
     * @param     vgap   垂直间距
     * @exception   IllegalArgumentException  如果 <code>rows</code> 和 <code>cols</code> 的值都设置为零
     */
    public GridLayout(int rows, int cols, int hgap, int vgap) {
        if ((rows == 0) && (cols == 0)) {
            throw new IllegalArgumentException("rows and cols cannot both be zero");
        }
        this.rows = rows;
        this.cols = cols;
        this.hgap = hgap;
        this.vgap = vgap;
    }

    /**
     * 获取此布局中的行数。
     * @return    此布局中的行数
     * @since     JDK1.1
     */
    public int getRows() {
        return rows;
    }

    /**
     * 将此布局中的行数设置为指定值。
     * @param        rows   此布局中的行数
     * @exception    IllegalArgumentException  如果 <code>rows</code> 和 <code>cols</code> 的值都设置为零
     * @since        JDK1.1
     */
    public void setRows(int rows) {
        if ((rows == 0) && (this.cols == 0)) {
            throw new IllegalArgumentException("rows and cols cannot both be zero");
        }
        this.rows = rows;
    }

    /**
     * 获取此布局中的列数。
     * @return     此布局中的列数
     * @since      JDK1.1
     */
    public int getColumns() {
        return cols;
    }

    /**
     * 将此布局中的列数设置为指定值。
     * 如果通过构造函数或 <tt>setRows</tt> 方法指定的行数为非零，则设置列数不会影响布局。
     * 在这种情况下，布局中显示的列数由组件总数和指定的行数决定。
     * @param        cols   此布局中的列数
     * @exception    IllegalArgumentException  如果 <code>rows</code> 和 <code>cols</code> 的值都设置为零
     * @since        JDK1.1
     */
    public void setColumns(int cols) {
        if ((cols == 0) && (this.rows == 0)) {
            throw new IllegalArgumentException("rows and cols cannot both be zero");
        }
        this.cols = cols;
    }

    /**
     * 获取组件之间的水平间距。
     * @return       组件之间的水平间距
     * @since        JDK1.1
     */
    public int getHgap() {
        return hgap;
    }

    /**
     * 将组件之间的水平间距设置为指定值。
     * @param        hgap   组件之间的水平间距
     * @since        JDK1.1
     */
    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    /**
     * 获取组件之间的垂直间距。
     * @return       组件之间的垂直间距
     * @since        JDK1.1
     */
    public int getVgap() {
        return vgap;
    }

    /**
     * 将组件之间的垂直间距设置为指定值。
     * @param         vgap  组件之间的垂直间距
     * @since        JDK1.1
     */
    public void setVgap(int vgap) {
        this.vgap = vgap;
    }

    /**
     * 将指定的组件以指定的名称添加到布局中。
     * @param name 组件的名称
     * @param comp 要添加的组件
     */
    public void addLayoutComponent(String name, Component comp) {
    }

    /**
     * 从布局中移除指定的组件。
     * @param comp 要移除的组件
     */
    public void removeLayoutComponent(Component comp) {
    }

    /**
     * 使用此网格布局确定容器参数的首选大小。
     * <p>
     * 网格布局的首选宽度是容器中所有组件的最大首选宽度乘以列数，加上水平填充乘以列数减一，再加上目标容器的左和右内边距。
     * <p>
     * 网格布局的首选高度是容器中所有组件的最大首选高度乘以行数，加上垂直填充乘以行数减一，再加上目标容器的上和下内边距。
     *
     * @param     parent   要进行布局的容器
     * @return    用于布局指定容器的子组件的首选尺寸
     * @see       java.awt.GridLayout#minimumLayoutSize
     * @see       java.awt.Container#getPreferredSize()
     */
    public Dimension preferredLayoutSize(Container parent) {
      synchronized (parent.getTreeLock()) {
        Insets insets = parent.getInsets();
        int ncomponents = parent.getComponentCount();
        int nrows = rows;
        int ncols = cols;

        if (nrows > 0) {
            ncols = (ncomponents + nrows - 1) / nrows;
        } else {
            nrows = (ncomponents + ncols - 1) / ncols;
        }
        int w = 0;
        int h = 0;
        for (int i = 0 ; i < ncomponents ; i++) {
            Component comp = parent.getComponent(i);
            Dimension d = comp.getPreferredSize();
            if (w < d.width) {
                w = d.width;
            }
            if (h < d.height) {
                h = d.height;
            }
        }
        return new Dimension(insets.left + insets.right + ncols*w + (ncols-1)*hgap,
                             insets.top + insets.bottom + nrows*h + (nrows-1)*vgap);
      }
    }

    /**
     * 使用此网格布局确定容器参数的最小大小。
     * <p>
     * 网格布局的最小宽度是容器中所有组件的最大最小宽度乘以列数，加上水平填充乘以列数减一，再加上目标容器的左和右内边距。
     * <p>
     * 网格布局的最小高度是容器中所有组件的最大最小高度乘以行数，加上垂直填充乘以行数减一，再加上目标容器的上和下内边距。
     *
     * @param       parent   要进行布局的容器
     * @return      用于布局指定容器的子组件的最小尺寸
     * @see         java.awt.GridLayout#preferredLayoutSize
     * @see         java.awt.Container#doLayout
     */
    public Dimension minimumLayoutSize(Container parent) {
      synchronized (parent.getTreeLock()) {
        Insets insets = parent.getInsets();
        int ncomponents = parent.getComponentCount();
        int nrows = rows;
        int ncols = cols;

        if (nrows > 0) {
            ncols = (ncomponents + nrows - 1) / nrows;
        } else {
            nrows = (ncomponents + ncols - 1) / ncols;
        }
        int w = 0;
        int h = 0;
        for (int i = 0 ; i < ncomponents ; i++) {
            Component comp = parent.getComponent(i);
            Dimension d = comp.getMinimumSize();
            if (w < d.width) {
                w = d.width;
            }
            if (h < d.height) {
                h = d.height;
            }
        }
        return new Dimension(insets.left + insets.right + ncols*w + (ncols-1)*hgap,
                             insets.top + insets.bottom + nrows*h + (nrows-1)*vgap);
      }
    }

    /**
     * 使用此布局对指定的容器进行布局。
     * <p>
     * 此方法重新调整指定目标容器中的组件大小，以满足 <code>GridLayout</code> 对象的约束。
     * <p>
     * 网格布局管理器通过将容器中的自由空间划分为根据布局中的行数和列数确定的等大小部分来确定单个组件的大小。
     * 容器的自由空间等于容器的大小减去任何内边距和指定的水平或垂直间距。网格布局中的所有组件大小相同。
     *
     * @param      parent   要进行布局的容器
     * @see        java.awt.Container
     * @see        java.awt.Container#doLayout
     */
    public void layoutContainer(Container parent) {
      synchronized (parent.getTreeLock()) {
        Insets insets = parent.getInsets();
        int ncomponents = parent.getComponentCount();
        int nrows = rows;
        int ncols = cols;
        boolean ltr = parent.getComponentOrientation().isLeftToRight();


                    if (ncomponents == 0) {
            return;
        }
        if (nrows > 0) {
            ncols = (ncomponents + nrows - 1) / nrows;
        } else {
            nrows = (ncomponents + ncols - 1) / ncols;
        }
        // 4370316. 为了将组件居中，我们应该：
        // 1. 获取容器内的额外空间量
        // 2. 将该值的一半纳入左/顶位置
        // 注意我们对 widthOnComponent 使用截断除法
        // 剩余部分归于 extraWidthAvailable
        int totalGapsWidth = (ncols - 1) * hgap;
        int widthWOInsets = parent.width - (insets.left + insets.right);
        int widthOnComponent = (widthWOInsets - totalGapsWidth) / ncols;
        int extraWidthAvailable = (widthWOInsets - (widthOnComponent * ncols + totalGapsWidth)) / 2;

        int totalGapsHeight = (nrows - 1) * vgap;
        int heightWOInsets = parent.height - (insets.top + insets.bottom);
        int heightOnComponent = (heightWOInsets - totalGapsHeight) / nrows;
        int extraHeightAvailable = (heightWOInsets - (heightOnComponent * nrows + totalGapsHeight)) / 2;
        if (ltr) {
            for (int c = 0, x = insets.left + extraWidthAvailable; c < ncols ; c++, x += widthOnComponent + hgap) {
                for (int r = 0, y = insets.top + extraHeightAvailable; r < nrows ; r++, y += heightOnComponent + vgap) {
                    int i = r * ncols + c;
                    if (i < ncomponents) {
                        parent.getComponent(i).setBounds(x, y, widthOnComponent, heightOnComponent);
                    }
                }
            }
        } else {
            for (int c = 0, x = (parent.width - insets.right - widthOnComponent) - extraWidthAvailable; c < ncols ; c++, x -= widthOnComponent + hgap) {
                for (int r = 0, y = insets.top + extraHeightAvailable; r < nrows ; r++, y += heightOnComponent + vgap) {
                    int i = r * ncols + c;
                    if (i < ncomponents) {
                        parent.getComponent(i).setBounds(x, y, widthOnComponent, heightOnComponent);
                    }
                }
            }
        }
      }
    }

    /**
     * 返回此网格布局值的字符串表示形式。
     * @return     此网格布局的字符串表示形式
     */
    public String toString() {
        return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap +
                                       ",rows=" + rows + ",cols=" + cols + "]";
    }
}
