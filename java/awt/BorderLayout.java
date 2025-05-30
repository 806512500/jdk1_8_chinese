
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

/**
 * 边布局管理器将容器布局，将组件排列在五个区域：
 * 北、南、东、西和中心。
 * 每个区域最多只能包含一个组件，并且由相应的常量标识：
 * <code>NORTH</code>、<code>SOUTH</code>、<code>EAST</code>、
 * <code>WEST</code> 和 <code>CENTER</code>。当向具有边布局管理器的容器中添加组件时，使用这五个常量之一，例如：
 * <pre>
 *    Panel p = new Panel();
 *    p.setLayout(new BorderLayout());
 *    p.add(new Button("Okay"), BorderLayout.SOUTH);
 * </pre>
 * 为了方便，<code>BorderLayout</code> 将未指定字符串的情况解释为常量 <code>CENTER</code>：
 * <pre>
 *    Panel p2 = new Panel();
 *    p2.setLayout(new BorderLayout());
 *    p2.add(new TextArea());  // 等同于 p.add(new TextArea(), BorderLayout.CENTER);
 * </pre>
 * <p>
 * 此外，<code>BorderLayout</code> 支持相对定位常量，<code>PAGE_START</code>、<code>PAGE_END</code>、
 * <code>LINE_START</code> 和 <code>LINE_END</code>。
 * 在容器的 <code>ComponentOrientation</code> 设置为 <code>ComponentOrientation.LEFT_TO_RIGHT</code> 时，
 * 这些常量分别映射到 <code>NORTH</code>、<code>SOUTH</code>、<code>WEST</code> 和 <code>EAST</code>。
 * <p>
 * 为了与早期版本兼容，<code>BorderLayout</code> 还包括相对定位常量 <code>BEFORE_FIRST_LINE</code>、
 * <code>AFTER_LAST_LINE</code>、<code>BEFORE_LINE_BEGINS</code> 和 <code>AFTER_LINE_ENDS</code>。
 * 这些常量分别等同于 <code>PAGE_START</code>、<code>PAGE_END</code>、<code>LINE_START</code>
 * 和 <code>LINE_END</code>。为了与其他组件使用的相对定位常量保持一致，建议使用后者。
 * <p>
 * 混合使用绝对和相对定位常量可能导致不可预测的结果。如果同时使用这两种类型，相对常量将优先。
 * 例如，如果在容器的 <code>LEFT_TO_RIGHT</code> 方向上同时使用 <code>NORTH</code> 和 <code>PAGE_START</code> 常量，
 * 只有 <code>PAGE_START</code> 将被布局。
 * <p>
 * 注意：目前（在 Java 2 平台 v1.2 中），<code>BorderLayout</code> 不支持垂直方向。
 * 容器的 <code>ComponentOrientation</code> 的 <code>isVertical</code> 设置不被尊重。
 * <p>
 * 组件根据其首选大小和容器大小的约束进行布局。
 * <code>NORTH</code> 和 <code>SOUTH</code> 组件可以水平拉伸；
 * <code>EAST</code> 和 <code>WEST</code> 组件可以垂直拉伸；
 * <code>CENTER</code> 组件可以水平和垂直拉伸以填充剩余的空间。
 * <p>
 * 以下是一个使用 <code>BorderLayout</code> 布局管理器的五个按钮在小程序中的布局示例：
 * <p>
 * <img src="doc-files/BorderLayout-1.gif"
 * alt="Diagram of an applet demonstrating BorderLayout.
 *      Each section of the BorderLayout contains a Button corresponding to its position in the layout, one of:
 *      North, West, Center, East, or South."
 * style="float:center; margin: 7px 10px;">
 * <p>
 * 该小程序的代码如下：
 *
 * <hr><blockquote><pre>
 * import java.awt.*;
 * import java.applet.Applet;
 *
 * public class buttonDir extends Applet {
 *   public void init() {
 *     setLayout(new BorderLayout());
 *     add(new Button("North"), BorderLayout.NORTH);
 *     add(new Button("South"), BorderLayout.SOUTH);
 *     add(new Button("East"), BorderLayout.EAST);
 *     add(new Button("West"), BorderLayout.WEST);
 *     add(new Button("Center"), BorderLayout.CENTER);
 *   }
 * }
 * </pre></blockquote><hr>
 * <p>
 * @author      Arthur van Hoff
 * @see         java.awt.Container#add(String, Component)
 * @see         java.awt.ComponentOrientation
 * @since       JDK1.0
 */
public class BorderLayout implements LayoutManager2,
                                     java.io.Serializable {
    /**
     * 构造一个具有水平间距的边布局。
     * 水平间距由 <code>hgap</code> 指定。
     *
     * @see #getHgap()
     * @see #setHgap(int)
     *
     * @serial
     */
        int hgap;

    /**
     * 构造一个具有垂直间距的边布局。
     * 垂直间距由 <code>vgap</code> 指定。
     *
     * @see #getVgap()
     * @see #setVgap(int)
     * @serial
     */
        int vgap;

    /**
     * 指定组件位置为边布局的北部分。
     * @serial
     * @see #getChild(String, boolean)
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
        Component north;
     /**
     * 指定组件位置为边布局的西部分。
     * @serial
     * @see #getChild(String, boolean)
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
        Component west;
    /**
     * 指定组件位置为边布局的东部分。
     * @serial
     * @see #getChild(String, boolean)
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
        Component east;
    /**
     * 指定组件位置为边布局的南部分。
     * @serial
     * @see #getChild(String, boolean)
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
    Component south;
    /**
     * 指定组件位置为边布局的中心部分。
     * @serial
     * @see #getChild(String, boolean)
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
        Component center;

    /**
     * 一个相对定位常量，可以替代北、南、东、西或中心。
     * 混合使用这两种类型的常量可能导致不可预测的结果。如果同时使用这两种类型，相对常量将优先。
     * 例如，如果在容器的 <code>LEFT_TO_RIGHT</code> 方向上同时使用 <code>NORTH</code> 和 <code>BEFORE_FIRST_LINE</code> 常量，
 * 只有 <code>BEFORE_FIRST_LINE</code> 将被布局。
 * 这同样适用于 lastLine, firstItem, lastItem。
 * @serial
     */
    Component firstLine;
     /**
     * 一个相对定位常量，可以替代北、南、东、西或中心。
     * 请参阅 firstLine 的描述。
     * @serial
     */
        Component lastLine;
     /**
     * 一个相对定位常量，可以替代北、南、东、西或中心。
     * 请参阅 firstLine 的描述。
     * @serial
     */
        Component firstItem;
    /**
     * 一个相对定位常量，可以替代北、南、东、西或中心。
     * 请参阅 firstLine 的描述。
     * @serial
     */
        Component lastItem;

    /**
     * 北布局约束（容器顶部）。
     */
    public static final String NORTH  = "North";

    /**
     * 南布局约束（容器底部）。
     */
    public static final String SOUTH  = "South";

    /**
     * 东布局约束（容器右侧）。
     */
    public static final String EAST   = "East";

    /**
     * 西布局约束（容器左侧）。
     */
    public static final String WEST   = "West";

    /**
     * 中心布局约束（容器中间）。
     */
    public static final String CENTER = "Center";

    /**
     * <code>PAGE_START</code> 的同义词。为了与早期版本兼容而存在。建议使用 <code>PAGE_START</code>。
     *
     * @see #PAGE_START
     * @since 1.2
     */
    public static final String BEFORE_FIRST_LINE = "First";

    /**
     * <code>PAGE_END</code> 的同义词。为了与早期版本兼容而存在。建议使用 <code>PAGE_END</code>。
     *
     * @see #PAGE_END
     * @since 1.2
     */
    public static final String AFTER_LAST_LINE = "Last";

    /**
     * <code>LINE_START</code> 的同义词。为了与早期版本兼容而存在。建议使用 <code>LINE_START</code>。
     *
     * @see #LINE_START
     * @since 1.2
     */
    public static final String BEFORE_LINE_BEGINS = "Before";

    /**
     * <code>LINE_END</code> 的同义词。为了与早期版本兼容而存在。建议使用 <code>LINE_END</code>。
     *
     * @see #LINE_END
     * @since 1.2
     */
    public static final String AFTER_LINE_ENDS = "After";

    /**
     * 组件位于布局内容的第一行之前。
     * 对于西方的从左到右和从上到下的方向，这等同于 <code>NORTH</code>。
     *
     * @see java.awt.Component#getComponentOrientation
     * @since 1.4
     */
    public static final String PAGE_START = BEFORE_FIRST_LINE;

    /**
     * 组件位于布局内容的最后一行之后。
     * 对于西方的从左到右和从上到下的方向，这等同于 <code>SOUTH</code>。
     *
     * @see java.awt.Component#getComponentOrientation
     * @since 1.4
     */
    public static final String PAGE_END = AFTER_LAST_LINE;

    /**
     * 组件位于布局的行方向的开始位置。
     * 对于西方的从左到右和从上到下的方向，这等同于 <code>WEST</code>。
     *
     * @see java.awt.Component#getComponentOrientation
     * @since 1.4
     */
    public static final String LINE_START = BEFORE_LINE_BEGINS;

    /**
     * 组件位于布局的行方向的结束位置。
     * 对于西方的从左到右和从上到下的方向，这等同于 <code>EAST</code>。
     *
     * @see java.awt.Component#getComponentOrientation
     * @since 1.4
     */
    public static final String LINE_END = AFTER_LINE_ENDS;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = -8658291919501921765L;

    /**
     * 构造一个新的边布局，组件之间没有间距。
     */
    public BorderLayout() {
        this(0, 0);
    }

    /**
     * 构造一个具有指定间距的边布局。
     * 水平间距由 <code>hgap</code> 指定，垂直间距由 <code>vgap</code> 指定。
     * @param   hgap   水平间距。
     * @param   vgap   垂直间距。
     */
    public BorderLayout(int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
    }

    /**
     * 返回组件之间的水平间距。
     * @since   JDK1.1
     */
    public int getHgap() {
        return hgap;
    }

    /**
     * 设置组件之间的水平间距。
     * @param hgap 组件之间的水平间距
     * @since   JDK1.1
     */
    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    /**
     * 返回组件之间的垂直间距。
     * @since   JDK1.1
     */
    public int getVgap() {
        return vgap;
    }

    /**
     * 设置组件之间的垂直间距。
     * @param vgap 组件之间的垂直间距
     * @since   JDK1.1
     */
    public void setVgap(int vgap) {
        this.vgap = vgap;
    }

    /**
     * 使用指定的约束对象将指定的组件添加到布局中。对于边布局，约束必须是以下常量之一：
     * <code>NORTH</code>、<code>SOUTH</code>、<code>EAST</code>、
     * <code>WEST</code> 或 <code>CENTER</code>。
     * <p>
     * 大多数应用程序不直接调用此方法。此方法在使用相同参数类型调用 <code>Container.add</code> 方法将组件添加到容器时被调用。
     * @param   comp         要添加的组件。
     * @param   constraints  指定如何和在何处将组件添加到布局的对象。
     * @see     java.awt.Container#add(java.awt.Component, java.lang.Object)
     * @exception   IllegalArgumentException  如果约束对象不是字符串，或者不是五个指定的常量之一。
     * @since   JDK1.1
     */
    public void addLayoutComponent(Component comp, Object constraints) {
      synchronized (comp.getTreeLock()) {
        if ((constraints == null) || (constraints instanceof String)) {
            addLayoutComponent((String)constraints, comp);
        } else {
            throw new IllegalArgumentException("cannot add to layout: constraint must be a string (or null)");
        }
      }
    }

    /**
     * @deprecated  替换为 <code>addLayoutComponent(Component, Object)</code>。
     */
    @Deprecated
    public void addLayoutComponent(String name, Component comp) {
      synchronized (comp.getTreeLock()) {
        /* 特殊情况：将 null 视同 "Center"。 */
        if (name == null) {
            name = "Center";
        }

        /* 将组件分配到布局的已知区域之一。
         */
        if ("Center".equals(name)) {
            center = comp;
        } else if ("North".equals(name)) {
            north = comp;
        } else if ("South".equals(name)) {
            south = comp;
        } else if ("East".equals(name)) {
            east = comp;
        } else if ("West".equals(name)) {
            west = comp;
        } else if (BEFORE_FIRST_LINE.equals(name)) {
            firstLine = comp;
        } else if (AFTER_LAST_LINE.equals(name)) {
            lastLine = comp;
        } else if (BEFORE_LINE_BEGINS.equals(name)) {
            firstItem = comp;
        } else if (AFTER_LINE_ENDS.equals(name)) {
            lastItem = comp;
        } else {
            throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + name);
        }
      }
    }


/**
 * 从这个边框布局中移除指定的组件。当容器调用其 <code>remove</code> 或
 * <code>removeAll</code> 方法时，会调用此方法。大多数应用程序不会直接调用此方法。
 * @param   comp   要移除的组件。
 * @see     java.awt.Container#remove(java.awt.Component)
 * @see     java.awt.Container#removeAll()
 */
public void removeLayoutComponent(Component comp) {
  synchronized (comp.getTreeLock()) {
    if (comp == center) {
        center = null;
    } else if (comp == north) {
        north = null;
    } else if (comp == south) {
        south = null;
    } else if (comp == east) {
        east = null;
    } else if (comp == west) {
        west = null;
    }
    if (comp == firstLine) {
        firstLine = null;
    } else if (comp == lastLine) {
        lastLine = null;
    } else if (comp == firstItem) {
        firstItem = null;
    } else if (comp == lastItem) {
        lastItem = null;
    }
  }
}

/**
 * 获取使用给定约束添加的组件
 *
 * @param   constraints  所需的约束，可以是 <code>CENTER</code>,
 *                       <code>NORTH</code>, <code>SOUTH</code>,
 *                       <code>WEST</code>, <code>EAST</code>,
 *                       <code>PAGE_START</code>, <code>PAGE_END</code>,
 *                       <code>LINE_START</code>, <code>LINE_END</code>
 * @return  给定位置的组件，如果位置为空则返回 <code>null</code>
 * @exception   IllegalArgumentException  如果约束对象不是九个指定的常量之一
 * @see     #addLayoutComponent(java.awt.Component, java.lang.Object)
 * @since 1.5
 */
public Component getLayoutComponent(Object constraints) {
    if (CENTER.equals(constraints)) {
        return center;
    } else if (NORTH.equals(constraints)) {
        return north;
    } else if (SOUTH.equals(constraints)) {
        return south;
    } else if (WEST.equals(constraints)) {
        return west;
    } else if (EAST.equals(constraints)) {
        return east;
    } else if (PAGE_START.equals(constraints)) {
        return firstLine;
    } else if (PAGE_END.equals(constraints)) {
        return lastLine;
    } else if (LINE_START.equals(constraints)) {
        return firstItem;
    } else if (LINE_END.equals(constraints)) {
        return lastItem;
    } else {
        throw new IllegalArgumentException("cannot get component: unknown constraint: " + constraints);
    }
}

/**
 * 根据目标 <code>Container</code> 的组件方向，返回与给定约束位置对应的组件。
 * 使用相对约束 <code>PAGE_START</code>, <code>PAGE_END</code>, <code>LINE_START</code>,
 * 和 <code>LINE_END</code> 添加的组件优先于使用显式约束 <code>NORTH</code>, <code>SOUTH</code>,
 * <code>WEST</code>, 和 <code>EAST</code> 添加的组件。组件方向用于确定使用 <code>LINE_START</code>
 * 和 <code>LINE_END</code> 添加的组件的位置。
 *
 * @param   constraints     所需的绝对位置，可以是 <code>CENTER</code>,
 *                          <code>NORTH</code>, <code>SOUTH</code>,
 *                          <code>EAST</code>, <code>WEST</code>
 * @param   target     用于根据目标 <code>Container</code> 的组件方向获取约束位置的容器。
 * @return  给定位置的组件，如果位置为空则返回 <code>null</code>
 * @exception   IllegalArgumentException  如果约束对象不是五个指定的常量之一
 * @exception   NullPointerException  如果目标参数为 null
 * @see     #addLayoutComponent(java.awt.Component, java.lang.Object)
 * @since 1.5
 */
public Component getLayoutComponent(Container target, Object constraints) {
    boolean ltr = target.getComponentOrientation().isLeftToRight();
    Component result = null;

    if (NORTH.equals(constraints)) {
        result = (firstLine != null) ? firstLine : north;
    } else if (SOUTH.equals(constraints)) {
        result = (lastLine != null) ? lastLine : south;
    } else if (WEST.equals(constraints)) {
        result = ltr ? firstItem : lastItem;
        if (result == null) {
            result = west;
        }
    } else if (EAST.equals(constraints)) {
        result = ltr ? lastItem : firstItem;
        if (result == null) {
            result = east;
        }
    } else if (CENTER.equals(constraints)) {
        result = center;
    } else {
        throw new IllegalArgumentException("cannot get component: invalid constraint: " + constraints);
    }

    return result;
}

/**
 * 获取指定组件的约束
 *
 * @param   comp 要查询的组件
 * @return  指定组件的约束，如果组件为 null 或不在此布局中则返回 null
 * @see #addLayoutComponent(java.awt.Component, java.lang.Object)
 * @since 1.5
 */
public Object getConstraints(Component comp) {
    // 修复 6242148：API 方法 java.awt.BorderLayout.getConstraints(null) 应返回 null
    if (comp == null){
        return null;
    }
    if (comp == center) {
        return CENTER;
    } else if (comp == north) {
        return NORTH;
    } else if (comp == south) {
        return SOUTH;
    } else if (comp == west) {
        return WEST;
    } else if (comp == east) {
        return EAST;
    } else if (comp == firstLine) {
        return PAGE_START;
    } else if (comp == lastLine) {
        return PAGE_END;
    } else if (comp == firstItem) {
        return LINE_START;
    } else if (comp == lastItem) {
        return LINE_END;
    }
    return null;
}

/**
 * 确定使用此布局管理器的 <code>target</code> 容器的最小尺寸。
 * <p>
 * 当容器调用其 <code>getMinimumSize</code> 方法时，会调用此方法。大多数应用程序不会直接调用此方法。
 * @param   target   要进行布局的容器。
 * @return  布局指定容器的子组件所需的最小尺寸。
 * @see     java.awt.Container
 * @see     java.awt.BorderLayout#preferredLayoutSize
 * @see     java.awt.Container#getMinimumSize()
 */
public Dimension minimumLayoutSize(Container target) {
  synchronized (target.getTreeLock()) {
    Dimension dim = new Dimension(0, 0);

    boolean ltr = target.getComponentOrientation().isLeftToRight();
    Component c = null;

    if ((c=getChild(EAST,ltr)) != null) {
        Dimension d = c.getMinimumSize();
        dim.width += d.width + hgap;
        dim.height = Math.max(d.height, dim.height);
    }
    if ((c=getChild(WEST,ltr)) != null) {
        Dimension d = c.getMinimumSize();
        dim.width += d.width + hgap;
        dim.height = Math.max(d.height, dim.height);
    }
    if ((c=getChild(CENTER,ltr)) != null) {
        Dimension d = c.getMinimumSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
    }
    if ((c=getChild(NORTH,ltr)) != null) {
        Dimension d = c.getMinimumSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height + vgap;
    }
    if ((c=getChild(SOUTH,ltr)) != null) {
        Dimension d = c.getMinimumSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height + vgap;
    }

    Insets insets = target.getInsets();
    dim.width += insets.left + insets.right;
    dim.height += insets.top + insets.bottom;

    return dim;
  }
}

/**
 * 确定使用此布局管理器的 <code>target</code> 容器的首选尺寸，基于容器中的组件。
 * <p>
 * 大多数应用程序不会直接调用此方法。当容器调用其 <code>getPreferredSize</code>
 * 方法时，会调用此方法。
 * @param   target   要进行布局的容器。
 * @return  布局指定容器的子组件的首选尺寸。
 * @see     java.awt.Container
 * @see     java.awt.BorderLayout#minimumLayoutSize
 * @see     java.awt.Container#getPreferredSize()
 */
public Dimension preferredLayoutSize(Container target) {
  synchronized (target.getTreeLock()) {
    Dimension dim = new Dimension(0, 0);

    boolean ltr = target.getComponentOrientation().isLeftToRight();
    Component c = null;

    if ((c=getChild(EAST,ltr)) != null) {
        Dimension d = c.getPreferredSize();
        dim.width += d.width + hgap;
        dim.height = Math.max(d.height, dim.height);
    }
    if ((c=getChild(WEST,ltr)) != null) {
        Dimension d = c.getPreferredSize();
        dim.width += d.width + hgap;
        dim.height = Math.max(d.height, dim.height);
    }
    if ((c=getChild(CENTER,ltr)) != null) {
        Dimension d = c.getPreferredSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
    }
    if ((c=getChild(NORTH,ltr)) != null) {
        Dimension d = c.getPreferredSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height + vgap;
    }
    if ((c=getChild(SOUTH,ltr)) != null) {
        Dimension d = c.getPreferredSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height + vgap;
    }

    Insets insets = target.getInsets();
    dim.width += insets.left + insets.right;
    dim.height += insets.top + insets.bottom;

    return dim;
  }
}

/**
 * 返回给定目标容器中组件的最大尺寸。
 * @param target 需要布局的组件
 * @see Container
 * @see #minimumLayoutSize
 * @see #preferredLayoutSize
 */
public Dimension maximumLayoutSize(Container target) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
}

/**
 * 返回 x 轴上的对齐方式。这指定了组件相对于其他组件的对齐方式。值应该在 0 和 1 之间，
 * 其中 0 表示对齐在原点，1 表示对齐在远离原点的最远位置，0.5 表示居中，等等。
 */
public float getLayoutAlignmentX(Container parent) {
    return 0.5f;
}

/**
 * 返回 y 轴上的对齐方式。这指定了组件相对于其他组件的对齐方式。值应该在 0 和 1 之间，
 * 其中 0 表示对齐在原点，1 表示对齐在远离原点的最远位置，0.5 表示居中，等等。
 */
public float getLayoutAlignmentY(Container parent) {
    return 0.5f;
}

/**
 * 使布局失效，指示如果布局管理器有缓存信息，则应丢弃。
 */
public void invalidateLayout(Container target) {
}

/**
 * 使用此边框布局对容器参数进行布局。
 * <p>
 * 此方法实际上会调整指定容器中组件的形状，以满足此 <code>BorderLayout</code> 对象的约束。
 * <code>NORTH</code> 和 <code>SOUTH</code> 组件（如果有）分别放置在容器的顶部和底部。
 * <code>WEST</code> 和 <code>EAST</code> 组件分别放置在左侧和右侧。最后，<code>CENTER</code>
 * 组件放置在中间的剩余空间中。
 * <p>
 * 大多数应用程序不会直接调用此方法。当容器调用其 <code>doLayout</code> 方法时，会调用此方法。
 * @param   target   要进行布局的容器。
 * @see     java.awt.Container
 * @see     java.awt.Container#doLayout()
 */
public void layoutContainer(Container target) {
  synchronized (target.getTreeLock()) {
    Insets insets = target.getInsets();
    int top = insets.top;
    int bottom = target.height - insets.bottom;
    int left = insets.left;
    int right = target.width - insets.right;

    boolean ltr = target.getComponentOrientation().isLeftToRight();
    Component c = null;

    if ((c=getChild(NORTH,ltr)) != null) {
        c.setSize(right - left, c.height);
        Dimension d = c.getPreferredSize();
        c.setBounds(left, top, right - left, d.height);
        top += d.height + vgap;
    }
    if ((c=getChild(SOUTH,ltr)) != null) {
        c.setSize(right - left, c.height);
        Dimension d = c.getPreferredSize();
        c.setBounds(left, bottom - d.height, right - left, d.height);
        bottom -= d.height + vgap;
    }
    if ((c=getChild(EAST,ltr)) != null) {
        c.setSize(c.width, bottom - top);
        Dimension d = c.getPreferredSize();
        c.setBounds(right - d.width, top, d.width, bottom - top);
        right -= d.width + hgap;
    }
    if ((c=getChild(WEST,ltr)) != null) {
        c.setSize(c.width, bottom - top);
        Dimension d = c.getPreferredSize();
        c.setBounds(left, top, d.width, bottom - top);
        left += d.width + hgap;
    }
    if ((c=getChild(CENTER,ltr)) != null) {
        c.setBounds(left, top, right - left, bottom - top);
    }
  }
}

/**
 * 获取与给定约束位置对应的组件
 *
 * @param   key     所需的绝对位置，可以是 NORTH, SOUTH, EAST, 或 WEST。
 * @param   ltr     组件的行方向是否为从左到右？
 */
private Component getChild(String key, boolean ltr) {
    Component result = null;

    if (key == NORTH) {
        result = (firstLine != null) ? firstLine : north;
    }
    else if (key == SOUTH) {
        result = (lastLine != null) ? lastLine : south;
    }
    else if (key == WEST) {
        result = ltr ? firstItem : lastItem;
        if (result == null) {
            result = west;
        }
    }
    else if (key == EAST) {
        result = ltr ? lastItem : firstItem;
        if (result == null) {
            result = east;
        }
    }
    else if (key == CENTER) {
        result = center;
    }
    if (result != null && !result.visible) {
        result = null;
    }
    return result;
}

/**
 * 返回此边框布局状态的字符串表示。
 * @return    此边框布局的字符串表示。
 */
public String toString() {
    return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + "]";
}
