
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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.peer.TextAreaPeer;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.accessibility.*;

/**
 * 一个 <code>TextArea</code> 对象是一个多行区域，用于显示文本。它可以设置为允许编辑或只读。
 * <p>
 * 以下图像显示了文本区域的外观：
 * <p>
 * <img src="doc-files/TextArea-1.gif" alt="一个显示 'Hello!' 的文本区域"
 * style="float:center; margin: 7px 10px;">
 * <p>
 * 该文本区域可以通过以下代码行创建：
 *
 * <hr><blockquote><pre>
 * new TextArea("Hello", 5, 40);
 * </pre></blockquote><hr>
 * <p>
 * @author      Sami Shaio
 * @since       JDK1.0
 */
public class TextArea extends TextComponent {

    /**
     * 文本区域中的行数。此参数将决定文本区域的高度。
     * 保证非负。
     *
     * @serial
     * @see #getRows()
     * @see #setRows(int)
     */
    int rows;

    /**
     * 文本区域中的列数。一列是平台依赖的近似平均字符宽度。
     * 此参数将决定文本区域的宽度。
     * 保证非负。
     *
     * @serial
     * @see  #setColumns(int)
     * @see  #getColumns()
     */
    int columns;

    private static final String base = "text";
    private static int nameCounter = 0;

    /**
     * 创建并显示垂直和水平滚动条。
     * @since JDK1.1
     */
    public static final int SCROLLBARS_BOTH = 0;

    /**
     * 仅创建并显示垂直滚动条。
     * @since JDK1.1
     */
    public static final int SCROLLBARS_VERTICAL_ONLY = 1;

    /**
     * 仅创建并显示水平滚动条。
     * @since JDK1.1
     */
    public static final int SCROLLBARS_HORIZONTAL_ONLY = 2;

    /**
     * 不为文本区域创建或显示任何滚动条。
     * @since JDK1.1
     */
    public static final int SCROLLBARS_NONE = 3;

    /**
     * 确定为文本区域创建哪些滚动条。它可以是四个值之一：
     * <code>SCROLLBARS_BOTH</code> = 两个滚动条。<BR>
     * <code>SCROLLBARS_HORIZONTAL_ONLY</code> = 仅水平滚动条。<BR>
     * <code>SCROLLBARS_VERTICAL_ONLY</code> = 仅垂直滚动条。<BR>
     * <code>SCROLLBARS_NONE</code> = 无滚动条。<BR>
     *
     * @serial
     * @see #getScrollbarVisibility()
     */
    private int scrollbarVisibility;

    /**
     * 缓存前向和后向遍历键的集合，以便我们不必每次查找它们。
     */
    private static Set<AWTKeyStroke> forwardTraversalKeys, backwardTraversalKeys;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = 3692302836626095722L;

    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
        forwardTraversalKeys = KeyboardFocusManager.initFocusTraversalKeysSet(
            "ctrl TAB",
            new HashSet<AWTKeyStroke>());
        backwardTraversalKeys = KeyboardFocusManager.initFocusTraversalKeysSet(
            "ctrl shift TAB",
            new HashSet<AWTKeyStroke>());
    }

    /**
     * 构造一个新的文本区域，以空字符串作为文本。
     * 该文本区域的滚动条可见性设置为 {@link #SCROLLBARS_BOTH}，因此该文本区域将显示垂直和水平滚动条。
     * @exception HeadlessException 如果 <code>GraphicsEnvironment.isHeadless</code> 返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless()
     */
    public TextArea() throws HeadlessException {
        this("", 0, 0, SCROLLBARS_BOTH);
    }

    /**
     * 构造一个新的文本区域，以指定的文本。
     * 该文本区域的滚动条可见性设置为 {@link #SCROLLBARS_BOTH}，因此该文本区域将显示垂直和水平滚动条。
     * @param      text       要显示的文本；如果 <code>text</code> 为 <code>null</code>，则显示空字符串 <code>""</code>
     * @exception HeadlessException 如果 <code>GraphicsEnvironment.isHeadless</code> 返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless()
     */
    public TextArea(String text) throws HeadlessException {
        this(text, 0, 0, SCROLLBARS_BOTH);
    }

    /**
     * 构造一个新的文本区域，以指定的行数和列数，以空字符串作为文本。
     * 一列是平台依赖的近似平均字符宽度。该文本区域的滚动条可见性设置为 {@link #SCROLLBARS_BOTH}，因此该文本区域将显示垂直和水平滚动条。
     * @param rows 行数
     * @param columns 列数
     * @exception HeadlessException 如果 <code>GraphicsEnvironment.isHeadless</code> 返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless()
     */
    public TextArea(int rows, int columns) throws HeadlessException {
        this("", rows, columns, SCROLLBARS_BOTH);
    }

    /**
     * 构造一个新的文本区域，以指定的文本，以及指定的行数和列数。
     * 一列是平台依赖的近似平均字符宽度。该文本区域的滚动条可见性设置为 {@link #SCROLLBARS_BOTH}，因此该文本区域将显示垂直和水平滚动条。
     * @param      text       要显示的文本；如果 <code>text</code> 为 <code>null</code>，则显示空字符串 <code>""</code>
     * @param     rows      行数
     * @param     columns   列数
     * @exception HeadlessException 如果 <code>GraphicsEnvironment.isHeadless</code> 返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless()
     */
    public TextArea(String text, int rows, int columns)
        throws HeadlessException {
        this(text, rows, columns, SCROLLBARS_BOTH);
    }

    /**
     * 构造一个新的文本区域，以指定的文本，以及指定的行数、列数和滚动条可见性。
     * 所有 <code>TextArea</code> 构造函数都委托给此构造函数。
     * <p>
     * <code>TextArea</code> 类定义了几个可以作为 <code>scrollbars</code> 参数值的常量：
     * <ul>
     * <li><code>SCROLLBARS_BOTH</code>，
     * <li><code>SCROLLBARS_VERTICAL_ONLY</code>，
     * <li><code>SCROLLBARS_HORIZONTAL_ONLY</code>，
     * <li><code>SCROLLBARS_NONE</code>。
     * </ul>
     * <code>scrollbars</code> 参数的任何其他值都是无效的，将导致此文本区域的滚动条可见性设置为默认值 {@link #SCROLLBARS_BOTH}。
     * @param      text       要显示的文本；如果 <code>text</code> 为 <code>null</code>，则显示空字符串 <code>""</code>
     * @param      rows       行数；如果 <code>rows</code> 小于 <code>0</code>，则设置为 <code>0</code>
     * @param      columns    列数；如果 <code>columns</code> 小于 <code>0</code>，则设置为 <code>0</code>
     * @param      scrollbars 一个常量，确定为查看文本区域创建哪些滚动条
     * @since      JDK1.1
     * @exception HeadlessException 如果 <code>GraphicsEnvironment.isHeadless</code> 返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless()
     */
    public TextArea(String text, int rows, int columns, int scrollbars)
        throws HeadlessException {
        super(text);

        this.rows = (rows >= 0) ? rows : 0;
        this.columns = (columns >= 0) ? columns : 0;

        if (scrollbars >= SCROLLBARS_BOTH && scrollbars <= SCROLLBARS_NONE) {
            this.scrollbarVisibility = scrollbars;
        } else {
            this.scrollbarVisibility = SCROLLBARS_BOTH;
        }

        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                              forwardTraversalKeys);
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                              backwardTraversalKeys);
    }

    /**
     * 为该组件构建一个名称。当名称为 <code>null</code> 时，由 <code>getName</code> 调用。
     */
    String constructComponentName() {
        synchronized (TextArea.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 创建 <code>TextArea</code> 的对等体。对等体允许我们在不改变其任何功能的情况下修改 <code>TextArea</code> 的外观。
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createTextArea(this);
            super.addNotify();
        }
    }

    /**
     * 在此文本区域的指定位置插入指定的文本。
     * <p>注意，传递 <code>null</code> 或不一致的参数是无效的，将导致未指定的行为。
     *
     * @param      str 要插入的非 <code>null</code> 文本
     * @param      pos 要插入的位置
     * @see        java.awt.TextComponent#setText
     * @see        java.awt.TextArea#replaceRange
     * @see        java.awt.TextArea#append
     * @since      JDK1.1
     */
    public void insert(String str, int pos) {
        insertText(str, pos);
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>insert(String, int)</code>。
     */
    @Deprecated
    public synchronized void insertText(String str, int pos) {
        TextAreaPeer peer = (TextAreaPeer)this.peer;
        if (peer != null) {
            peer.insert(str, pos);
        }
        text = text.substring(0, pos) + str + text.substring(pos);
    }

    /**
     * 将给定的文本追加到文本区域的当前文本中。
     * <p>注意，传递 <code>null</code> 或不一致的参数是无效的，将导致未指定的行为。
     *
     * @param     str 要追加的非 <code>null</code> 文本
     * @see       java.awt.TextArea#insert
     * @since     JDK1.1
     */
    public void append(String str) {
        appendText(str);
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>append(String)</code>。
     */
    @Deprecated
    public synchronized void appendText(String str) {
            insertText(str, getText().length());
    }

    /**
     * 用指定的替换文本替换指定起始和结束位置之间的文本。结束位置的文本不会被替换。起始位置的文本将被替换（除非起始位置与结束位置相同）。
     * 文本位置从零开始。插入的子字符串可以与它替换的文本长度不同。
     * <p>注意，传递 <code>null</code> 或不一致的参数是无效的，将导致未指定的行为。
     *
     * @param     str      要用作替换的非 <code>null</code> 文本
     * @param     start    起始位置
     * @param     end      结束位置
     * @see       java.awt.TextArea#insert
     * @since     JDK1.1
     */
    public void replaceRange(String str, int start, int end) {
        replaceText(str, start, end);
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>replaceRange(String, int, int)</code>。
     */
    @Deprecated
    public synchronized void replaceText(String str, int start, int end) {
        TextAreaPeer peer = (TextAreaPeer)this.peer;
        if (peer != null) {
            peer.replaceRange(str, start, end);
        }
        text = text.substring(0, start) + str + text.substring(end);
    }

    /**
     * 返回文本区域中的行数。
     * @return    文本区域中的行数
     * @see       #setRows(int)
     * @see       #getColumns()
     * @since     JDK1
     */
    public int getRows() {
        return rows;
    }

    /**
     * 设置此文本区域的行数。
     * @param       rows   行数
     * @see         #getRows()
     * @see         #setColumns(int)
     * @exception   IllegalArgumentException   如果提供的 <code>rows</code> 值小于 <code>0</code>
     * @since       JDK1.1
     */
    public void setRows(int rows) {
        int oldVal = this.rows;
        if (rows < 0) {
            throw new IllegalArgumentException("rows less than zero.");
        }
        if (rows != oldVal) {
            this.rows = rows;
            invalidate();
        }
    }

    /**
     * 返回此文本区域中的列数。
     * @return    文本区域中的列数
     * @see       #setColumns(int)
     * @see       #getRows()
     */
    public int getColumns() {
        return columns;
    }

    /**
     * 设置此文本区域的列数。
     * @param       columns   列数
     * @see         #getColumns()
     * @see         #setRows(int)
     * @exception   IllegalArgumentException   如果提供的 <code>columns</code> 值小于 <code>0</code>
     * @since       JDK1.1
     */
    public void setColumns(int columns) {
        int oldVal = this.columns;
        if (columns < 0) {
            throw new IllegalArgumentException("columns less than zero.");
        }
        if (columns != oldVal) {
            this.columns = columns;
            invalidate();
        }
    }


                /**
     * 返回一个枚举值，指示文本区域使用的滚动条。
     * <p>
     * <code>TextArea</code> 类定义了四个整数常量，用于指定可用的滚动条。
     * <code>TextArea</code> 有一个构造函数，允许应用程序决定滚动条的使用。
     *
     * @return     指示使用哪些滚动条的整数
     * @see        java.awt.TextArea#SCROLLBARS_BOTH
     * @see        java.awt.TextArea#SCROLLBARS_VERTICAL_ONLY
     * @see        java.awt.TextArea#SCROLLBARS_HORIZONTAL_ONLY
     * @see        java.awt.TextArea#SCROLLBARS_NONE
     * @see        java.awt.TextArea#TextArea(java.lang.String, int, int, int)
     * @since      JDK1.1
     */
    public int getScrollbarVisibility() {
        return scrollbarVisibility;
    }


    /**
     * 确定具有指定行数和列数的文本区域的首选大小。
     * @param     rows   行数
     * @param     columns   列数
     * @return    显示具有指定行数和列数的文本区域所需的首选尺寸
     * @see       java.awt.Component#getPreferredSize
     * @since     JDK1.1
     */
    public Dimension getPreferredSize(int rows, int columns) {
        return preferredSize(rows, columns);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getPreferredSize(int, int)</code>。
     */
    @Deprecated
    public Dimension preferredSize(int rows, int columns) {
        synchronized (getTreeLock()) {
            TextAreaPeer peer = (TextAreaPeer)this.peer;
            return (peer != null) ?
                       peer.getPreferredSize(rows, columns) :
                       super.preferredSize();
        }
    }

    /**
     * 确定此文本区域的首选大小。
     * @return    此文本区域所需的首选尺寸
     * @see       java.awt.Component#getPreferredSize
     * @since     JDK1.1
     */
    public Dimension getPreferredSize() {
        return preferredSize();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getPreferredSize()</code>。
     */
    @Deprecated
    public Dimension preferredSize() {
        synchronized (getTreeLock()) {
            return ((rows > 0) && (columns > 0)) ?
                        preferredSize(rows, columns) :
                        super.preferredSize();
        }
    }

    /**
     * 确定具有指定行数和列数的文本区域的最小大小。
     * @param     rows   行数
     * @param     columns   列数
     * @return    显示具有指定行数和列数的文本区域所需的最小尺寸
     * @see       java.awt.Component#getMinimumSize
     * @since     JDK1.1
     */
    public Dimension getMinimumSize(int rows, int columns) {
        return minimumSize(rows, columns);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getMinimumSize(int, int)</code>。
     */
    @Deprecated
    public Dimension minimumSize(int rows, int columns) {
        synchronized (getTreeLock()) {
            TextAreaPeer peer = (TextAreaPeer)this.peer;
            return (peer != null) ?
                       peer.getMinimumSize(rows, columns) :
                       super.minimumSize();
        }
    }

    /**
     * 确定此文本区域的最小大小。
     * @return    此文本区域所需的最小尺寸
     * @see       java.awt.Component#getPreferredSize
     * @since     JDK1.1
     */
    public Dimension getMinimumSize() {
        return minimumSize();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getMinimumSize()</code>。
     */
    @Deprecated
    public Dimension minimumSize() {
        synchronized (getTreeLock()) {
            return ((rows > 0) && (columns > 0)) ?
                        minimumSize(rows, columns) :
                        super.minimumSize();
        }
    }

    /**
     * 返回表示此 <code>TextArea</code> 状态的字符串。
     * 此方法仅用于调试目的，返回字符串的内容和格式可能因实现而异。返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return      此文本区域的参数字符串
     */
    protected String paramString() {
        String sbVisStr;
        switch (scrollbarVisibility) {
            case SCROLLBARS_BOTH:
                sbVisStr = "both";
                break;
            case SCROLLBARS_VERTICAL_ONLY:
                sbVisStr = "vertical-only";
                break;
            case SCROLLBARS_HORIZONTAL_ONLY:
                sbVisStr = "horizontal-only";
                break;
            case SCROLLBARS_NONE:
                sbVisStr = "none";
                break;
            default:
                sbVisStr = "invalid display policy";
        }

        return super.paramString() + ",rows=" + rows +
            ",columns=" + columns +
          ",scrollbarVisibility=" + sbVisStr;
    }


    /*
     * 序列化支持。
     */
    /**
     * 文本区域序列化数据版本。
     *
     * @serial
     */
    private int textAreaSerializedDataVersion = 2;

    /**
     * 读取 ObjectInputStream。
     * @exception HeadlessException 如果
     * <code>GraphicsEnvironment.isHeadless()</code> 返回
     * <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException
    {
        // HeadlessException 将由 TextComponent 的 readObject 抛出
        s.defaultReadObject();

        // 确保我们刚刚读取的 columns、rows 和 scrollbarVisibility 的状态具有合法值
        if (columns < 0) {
            columns = 0;
        }
        if (rows < 0) {
            rows = 0;
        }

        if ((scrollbarVisibility < SCROLLBARS_BOTH) ||
            (scrollbarVisibility > SCROLLBARS_NONE)) {
            this.scrollbarVisibility = SCROLLBARS_BOTH;
        }

        if (textAreaSerializedDataVersion < 2) {
            setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                                  forwardTraversalKeys);
            setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                                  backwardTraversalKeys);
        }
    }


/////////////////
// 可访问性支持
////////////////


    /**
     * 返回与此 <code>TextArea</code> 关联的 <code>AccessibleContext</code>。对于文本区域，
     * <code>AccessibleContext</code> 采用 <code>AccessibleAWTTextArea</code> 的形式。
     * 如果必要，将创建一个新的 <code>AccessibleAWTTextArea</code> 实例。
     *
     * @return 一个 <code>AccessibleAWTTextArea</code>，作为此 <code>TextArea</code> 的 <code>AccessibleContext</code>
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTTextArea();
        }
        return accessibleContext;
    }

    /**
     * 该类为 <code>TextArea</code> 类实现了可访问性支持。它提供了适用于文本区域用户界面元素的 Java 可访问性 API 的实现。
     * @since 1.3
     */
    protected class AccessibleAWTTextArea extends AccessibleAWTTextComponent
    {
        /*
         * JDK 1.3 序列化版本 ID
         */
        private static final long serialVersionUID = 3472827823632144419L;

        /**
         * 获取此对象的状态集。
         *
         * @return 一个 AccessibleStateSet 实例，描述对象的状态
         * @see AccessibleStateSet
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.MULTI_LINE);
            return states;
        }
    }


}
