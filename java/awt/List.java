
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

import java.util.Vector;
import java.util.Locale;
import java.util.EventListener;
import java.awt.peer.ListPeer;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import javax.accessibility.*;


/**
 * <code>List</code> 组件向用户呈现一个滚动的文本项列表。该列表可以设置为允许用户选择一个或多个项。
 * <p>
 * 例如，代码&nbsp;.&nbsp;.&nbsp;.
 *
 * <hr><blockquote><pre>
 * List lst = new List(4, false);
 * lst.add("Mercury");
 * lst.add("Venus");
 * lst.add("Earth");
 * lst.add("JavaSoft");
 * lst.add("Mars");
 * lst.add("Jupiter");
 * lst.add("Saturn");
 * lst.add("Uranus");
 * lst.add("Neptune");
 * lst.add("Pluto");
 * cnt.add(lst);
 * </pre></blockquote><hr>
 * <p>
 * 其中 <code>cnt</code> 是一个容器，生成以下滚动列表：
 * <p>
 * <img src="doc-files/List-1.gif"
 * alt="显示一个包含：Venus, Earth, JavaSoft, 和 Mars 的列表。JavaSoft 被选中。" style="float:center; margin: 7px 10px;">
 * <p>
 * 如果列表允许多选，那么点击一个已选中的项将取消其选择。在上述示例中，滚动列表一次只能选择一个项，因为创建新滚动列表时的第二个参数是 <code>false</code>。如果列表不允许多选，选择一个项将导致任何其他已选中的项被取消选择。
 * <p>
 * 注意，示例中的列表创建时有四行可见。一旦列表创建后，可见行数将无法更改。默认的 <code>List</code> 创建时有四行，因此 <code>lst = new List()</code> 等同于 <code>list = new List(4, false)</code>。
 * <p>
 * 从 Java&nbsp;1.1 开始，抽象窗口工具包 (AWT) 会将所有发生在列表上的鼠标、键盘和焦点事件发送给 <code>List</code> 对象。（旧的 AWT 事件模型仅为了向后兼容而保留，其使用不被鼓励。）
 * <p>
 * 当用户选择或取消选择一个项时，AWT 会向列表发送一个 <code>ItemEvent</code> 实例。当用户在滚动列表中双击一个项时，AWT 会在项事件之后向列表发送一个 <code>ActionEvent</code> 实例。当用户在列表中选择一个项时按回车键，AWT 也会生成一个动作事件。
 * <p>
 * 如果应用程序希望根据用户在列表中选择或激活的项执行某些操作，应实现 <code>ItemListener</code> 或 <code>ActionListener</code>，并注册新的监听器以接收来自此列表的事件。
 * <p>
 * 对于多选滚动列表，使用外部手势（如点击按钮）来触发操作被认为是更好的用户界面。
 * @author      Sami Shaio
 * @see         java.awt.event.ItemEvent
 * @see         java.awt.event.ItemListener
 * @see         java.awt.event.ActionEvent
 * @see         java.awt.event.ActionListener
 * @since       JDK1.0
 */
public class List extends Component implements ItemSelectable, Accessible {
    /**
     * 一个用于包含将成为 List 组件一部分的项的向量。
     *
     * @serial
     * @see #addItem(String)
     * @see #getItem(int)
     */
    Vector<String>      items = new Vector<>();

    /**
     * 该字段表示 <code>List</code> 组件中可见的行数。它仅在创建列表组件时指定一次，并且永远不会改变。
     *
     * @serial
     * @see #getRows()
     */
    int         rows = 0;

    /**
     * <code>multipleMode</code> 是一个变量，如果列表组件设置为多选模式（即用户可以一次选择多个项），则该变量将被设置为 <code>true</code>。如果列表组件设置为单选模式（即用户一次只能选择一个项），则该变量将被设置为 <code>false</code>。
     *
     * @serial
     * @see #isMultipleMode()
     * @see #setMultipleMode(boolean)
     */
    boolean     multipleMode = false;

    /**
     * <code>selected</code> 是一个数组，用于包含已选择项的索引。
     *
     * @serial
     * @see #getSelectedIndexes()
     * @see #getSelectedIndex()
     */
    int         selected[] = new int[0];

    /**
     * 该变量包含在尝试使特定列表项可见时使用的值。
     *
     * @serial
     * @see #makeVisible(int)
     */
    int         visibleIndex = -1;

    transient ActionListener actionListener;
    transient ItemListener itemListener;

    private static final String base = "list";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = -3304312411574666869L;

    /**
     * 创建一个新的滚动列表。
     * 默认情况下，有四行可见，不允许多选。注意，这是一个 <code>List(0, false)</code> 的便捷方法。还请注意，列表创建后，可见行数将无法更改。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public List() throws HeadlessException {
        this(0, false);
    }

    /**
     * 创建一个新的滚动列表，并初始化为指定的可见行数。默认情况下，不允许多选。注意，这是一个 <code>List(rows, false)</code> 的便捷方法。还请注意，列表创建后，可见行数将无法更改。
     * @param       rows 要显示的项数。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since       JDK1.1
     */
    public List(int rows) throws HeadlessException {
        this(rows, false);
    }

    /**
     * 默认的可见行数是 4。零行的列表不可用且不美观。
     */
    final static int    DEFAULT_VISIBLE_ROWS = 4;

    /**
     * 创建一个新的滚动列表，并初始化为显示指定的行数。注意，如果指定零行，则列表将创建为默认的四行。还请注意，列表创建后，可见行数将无法更改。
     * 如果 <code>multipleMode</code> 的值为 <code>true</code>，则用户可以从列表中选择多个项。如果为 <code>false</code>，则一次只能选择一个项。
     * @param       rows   要显示的项数。
     * @param       multipleMode   如果为 <code>true</code>，则允许多选；否则，一次只能选择一个项。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public List(int rows, boolean multipleMode) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.rows = (rows != 0) ? rows : DEFAULT_VISIBLE_ROWS;
        this.multipleMode = multipleMode;
    }

    /**
     * 为该组件构建一个名称。当名称为 <code>null</code> 时，由 <code>getName</code> 调用。
     */
    String constructComponentName() {
        synchronized (List.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 为列表创建对等体。对等体允许我们在不改变功能的情况下修改列表的外观。
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createList(this);
            super.addNotify();
        }
    }

    /**
     * 移除该列表的对等体。对等体允许我们在不改变功能的情况下修改列表的外观。
     */
    public void removeNotify() {
        synchronized (getTreeLock()) {
            ListPeer peer = (ListPeer)this.peer;
            if (peer != null) {
                selected = peer.getSelectedIndexes();
            }
            super.removeNotify();
        }
    }

    /**
     * 获取列表中的项数。
     * @return     列表中的项数
     * @see        #getItem
     * @since      JDK1.1
     */
    public int getItemCount() {
        return countItems();
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>getItemCount()</code>。
     */
    @Deprecated
    public int countItems() {
        return items.size();
    }

    /**
     * 获取与指定索引关联的项。
     * @return       与指定索引关联的项
     * @param        index 项的位置
     * @see          #getItemCount
     */
    public String getItem(int index) {
        return getItemImpl(index);
    }

    // 注意：此方法可能由特权线程调用。
    // 我们在包私有方法中实现此功能，以确保客户端子类无法覆盖。
    // 不要在该线程上调用客户端代码！
    final String getItemImpl(int index) {
        return items.elementAt(index);
    }

    /**
     * 获取列表中的项。
     * @return       包含列表项的字符串数组
     * @see          #select
     * @see          #deselect
     * @see          #isIndexSelected
     * @since        JDK1.1
     */
    public synchronized String[] getItems() {
        String itemCopies[] = new String[items.size()];
        items.copyInto(itemCopies);
        return itemCopies;
    }

    /**
     * 将指定的项添加到滚动列表的末尾。
     * @param item 要添加的项
     * @since JDK1.1
     */
    public void add(String item) {
        addItem(item);
    }

    /**
     * @deprecated 替换为 <code>add(String)</code>。
     */
    @Deprecated
    public void addItem(String item) {
        addItem(item, -1);
    }

    /**
     * 将指定的项添加到滚动列表的指定位置。索引从零开始。如果索引的值小于零，或大于等于列表中的项数，则将项添加到列表的末尾。
     * @param       item   要添加的项；
     *              如果此参数为 <code>null</code>，则项被视为一个空字符串，<code>""</code>
     * @param       index  要添加项的位置
     * @since       JDK1.1
     */
    public void add(String item, int index) {
        addItem(item, index);
    }

    /**
     * @deprecated 替换为 <code>add(String, int)</code>。
     */
    @Deprecated
    public synchronized void addItem(String item, int index) {
        if (index < -1 || index >= items.size()) {
            index = -1;
        }

        if (item == null) {
            item = "";
        }

        if (index == -1) {
            items.addElement(item);
        } else {
            items.insertElementAt(item, index);
        }

        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
            peer.add(item, index);
        }
    }

    /**
     * 用新字符串替换滚动列表中指定索引处的项。
     * @param       newValue   用于替换现有项的新字符串
     * @param       index      要替换的项的位置
     * @exception ArrayIndexOutOfBoundsException 如果 <code>index</code> 超出范围
     */
    public synchronized void replaceItem(String newValue, int index) {
        remove(index);
        add(newValue, index);
    }

    /**
     * 从列表中移除所有项。
     * @see #remove
     * @see #delItems
     * @since JDK1.1
     */
    public void removeAll() {
        clear();
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>removeAll()</code>。
     */
    @Deprecated
    public synchronized void clear() {
        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
            peer.removeAll();
        }
        items = new Vector<>();
        selected = new int[0];
    }

    /**
     * 从列表中移除第一次出现的项。
     * 如果指定的项被选中，并且是列表中唯一被选中的项，则列表将被设置为没有选择。
     * @param        item  要从列表中移除的项
     * @exception    IllegalArgumentException
     *                     如果项不存在于列表中
     * @since        JDK1.1
     */
    public synchronized void remove(String item) {
        int index = items.indexOf(item);
        if (index < 0) {
            throw new IllegalArgumentException("item " + item +
                                               " not found in list");
        } else {
            remove(index);
        }
    }

    /**
     * 从滚动列表中移除指定位置的项。
     * 如果指定位置的项被选中，并且是列表中唯一被选中的项，则列表将被设置为没有选择。
     * @param      position   要删除的项的索引
     * @see        #add(String, int)
     * @since      JDK1.1
     * @exception    ArrayIndexOutOfBoundsException
     *               如果 <code>position</code> 小于 0 或大于 <code>getItemCount()-1</code>
     */
    public void remove(int position) {
        delItem(position);
    }


                /**
     * @deprecated     replaced by <code>remove(String)</code>
     *                         and <code>remove(int)</code>.
     */
    @Deprecated
    public void delItem(int position) {
        delItems(position, position);
    }

    /**
     * 获取列表中选定项目的索引，
     *
     * @return        选定项目的索引；
     *                如果没有项目被选定，或者有多个项目被选定，
     *                则返回 <code>-1</code>。
     * @see           #select
     * @see           #deselect
     * @see           #isIndexSelected
     */
    public synchronized int getSelectedIndex() {
        int sel[] = getSelectedIndexes();
        return (sel.length == 1) ? sel[0] : -1;
    }

    /**
     * 获取列表中选定的索引。
     *
     * @return        一个包含此滚动列表中选定索引的数组；
     *                如果没有项目被选定，返回一个零长度的数组。
     * @see           #select
     * @see           #deselect
     * @see           #isIndexSelected
     */
    public synchronized int[] getSelectedIndexes() {
        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
            selected = peer.getSelectedIndexes();
        }
        return selected.clone();
    }

    /**
     * 获取此滚动列表中选定的项目。
     *
     * @return        列表中选定的项目；
     *                如果没有项目被选定，或者有多个项目被选定，
     *                则返回 <code>null</code>。
     * @see           #select
     * @see           #deselect
     * @see           #isIndexSelected
     */
    public synchronized String getSelectedItem() {
        int index = getSelectedIndex();
        return (index < 0) ? null : getItem(index);
    }

    /**
     * 获取此滚动列表中选定的项目。
     *
     * @return        一个包含此滚动列表中选定项目的数组；
     *                如果没有项目被选定，返回一个零长度的数组。
     * @see           #select
     * @see           #deselect
     * @see           #isIndexSelected
     */
    public synchronized String[] getSelectedItems() {
        int sel[] = getSelectedIndexes();
        String str[] = new String[sel.length];
        for (int i = 0 ; i < sel.length ; i++) {
            str[i] = getItem(sel[i]);
        }
        return str;
    }

    /**
     * 以 Object 数组的形式获取此滚动列表中选定的项目。
     * @return        一个表示此滚动列表中选定项目的 <code>Object</code> 数组；
     *                如果没有项目被选定，返回一个零长度的数组。
     * @see #getSelectedItems
     * @see ItemSelectable
     */
    public Object[] getSelectedObjects() {
        return getSelectedItems();
    }

    /**
     * 在滚动列表中选择指定索引处的项目。
     *<p>
     * 注意，传递超出范围的参数是无效的，
     * 并将导致未指定的行为。
     *
     * <p>注意，此方法主要用于在组件中初始选择一个项目。
     * 程序调用此方法不会触发
     * <code>ItemEvent</code>。触发
     * <code>ItemEvent</code> 的唯一方法是通过用户交互。
     *
     * @param        index 要选择的项目的索引
     * @see          #getSelectedItem
     * @see          #deselect
     * @see          #isIndexSelected
     */
    public void select(int index) {
        // Bug #4059614: select 不能在调用 peer 时同步，
        // 因为它是由窗口线程调用的。同步操作 'selected' 的代码就足够了，
        // 除非 peer 发生变化。为了处理这种情况，我们简单地重复选择过程。

        ListPeer peer;
        do {
            peer = (ListPeer)this.peer;
            if (peer != null) {
                peer.select(index);
                return;
            }

            synchronized(this)
            {
                boolean alreadySelected = false;

                for (int i = 0 ; i < selected.length ; i++) {
                    if (selected[i] == index) {
                        alreadySelected = true;
                        break;
                    }
                }

                if (!alreadySelected) {
                    if (!multipleMode) {
                        selected = new int[1];
                        selected[0] = index;
                    } else {
                        int newsel[] = new int[selected.length + 1];
                        System.arraycopy(selected, 0, newsel, 0,
                                         selected.length);
                        newsel[selected.length] = index;
                        selected = newsel;
                    }
                }
            }
        } while (peer != this.peer);
    }

    /**
     * 取消选择指定索引处的项目。
     * <p>
     * 注意，传递超出范围的参数是无效的，
     * 并将导致未指定的行为。
     * <p>
     * 如果指定索引处的项目未被选定，
     * 则忽略此操作。
     * @param        index 要取消选择的项目的索引
     * @see          #select
     * @see          #getSelectedItem
     * @see          #isIndexSelected
     */
    public synchronized void deselect(int index) {
        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
            if (isMultipleMode() || (getSelectedIndex() == index)) {
                peer.deselect(index);
            }
        }

        for (int i = 0 ; i < selected.length ; i++) {
            if (selected[i] == index) {
                int newsel[] = new int[selected.length - 1];
                System.arraycopy(selected, 0, newsel, 0, i);
                System.arraycopy(selected, i+1, newsel, i, selected.length - (i+1));
                selected = newsel;
                return;
            }
        }
    }

    /**
     * 确定此滚动列表中指定的项目是否被选定。
     * @param      index   要检查的项目
     * @return     <code>true</code> 如果指定的项目已被选定；否则返回 <code>false</code>
     * @see        #select
     * @see        #deselect
     * @since      JDK1.1
     */
    public boolean isIndexSelected(int index) {
        return isSelected(index);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * replaced by <code>isIndexSelected(int)</code>.
     */
    @Deprecated
    public boolean isSelected(int index) {
        int sel[] = getSelectedIndexes();
        for (int i = 0 ; i < sel.length ; i++) {
            if (sel[i] == index) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取此列表中的可见行数。注意，
     * 一旦 <code>List</code> 被创建，这个数字
     * 将不会改变。
     * @return     此滚动列表中的可见行数
     */
    public int getRows() {
        return rows;
    }

    /**
     * 确定此列表是否允许多个选择。
     * @return     <code>true</code> 如果此列表允许多个
     *                 选择；否则返回 <code>false</code>
     * @see        #setMultipleMode
     * @since      JDK1.1
     */
    public boolean isMultipleMode() {
        return allowsMultipleSelections();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * replaced by <code>isMultipleMode()</code>.
     */
    @Deprecated
    public boolean allowsMultipleSelections() {
        return multipleMode;
    }

    /**
     * 设置确定此列表是否允许多个选择的标志。
     * 当选择模式从多选更改为单选时，选定的项目将如下变化：
     * 如果选定的项目中有一个位置光标，只有那个
     * 项目将保持选定。如果没有选定的项目有
     * 位置光标，所有项目都将被取消选择。
     * @param       b   如果 <code>true</code> 则允许多个选择
     *                      否则，列表中只能同时选择一个项目
     * @see         #isMultipleMode
     * @since       JDK1.1
     */
    public void setMultipleMode(boolean b) {
        setMultipleSelections(b);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * replaced by <code>setMultipleMode(boolean)</code>.
     */
    @Deprecated
    public synchronized void setMultipleSelections(boolean b) {
        if (b != multipleMode) {
            multipleMode = b;
            ListPeer peer = (ListPeer)this.peer;
            if (peer != null) {
                peer.setMultipleMode(b);
            }
        }
    }

    /**
     * 获取通过 <code>makeVisible</code> 方法最后变得可见的项目的索引。
     * @return      最后变得可见的项目的索引
     * @see         #makeVisible
     */
    public int getVisibleIndex() {
        return visibleIndex;
    }

    /**
     * 使指定索引处的项目可见。
     * @param       index    项目的索引
     * @see         #getVisibleIndex
     */
    public synchronized void makeVisible(int index) {
        visibleIndex = index;
        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
            peer.makeVisible(index);
        }
    }

    /**
     * 获取具有指定行数的列表的首选尺寸。
     * @param      rows    列表中的行数
     * @return     显示此滚动列表时的首选尺寸，前提是必须可见指定的行数
     * @see        java.awt.Component#getPreferredSize
     * @since      JDK1.1
     */
    public Dimension getPreferredSize(int rows) {
        return preferredSize(rows);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * replaced by <code>getPreferredSize(int)</code>.
     */
    @Deprecated
    public Dimension preferredSize(int rows) {
        synchronized (getTreeLock()) {
            ListPeer peer = (ListPeer)this.peer;
            return (peer != null) ?
                       peer.getPreferredSize(rows) :
                       super.preferredSize();
        }
    }

    /**
     * 获取此滚动列表的首选尺寸。
     * @return     显示此滚动列表时的首选尺寸
     * @see        java.awt.Component#getPreferredSize
     * @since      JDK1.1
     */
    public Dimension getPreferredSize() {
        return preferredSize();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * replaced by <code>getPreferredSize()</code>.
     */
    @Deprecated
    public Dimension preferredSize() {
        synchronized (getTreeLock()) {
            return (rows > 0) ?
                       preferredSize(rows) :
                       super.preferredSize();
        }
    }

    /**
     * 获取具有指定行数的列表的最小尺寸。
     * @param      rows    列表中的行数
     * @return     显示此滚动列表时的最小尺寸，前提是必须可见指定的行数
     * @see        java.awt.Component#getMinimumSize
     * @since      JDK1.1
     */
    public Dimension getMinimumSize(int rows) {
        return minimumSize(rows);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * replaced by <code>getMinimumSize(int)</code>.
     */
    @Deprecated
    public Dimension minimumSize(int rows) {
        synchronized (getTreeLock()) {
            ListPeer peer = (ListPeer)this.peer;
            return (peer != null) ?
                       peer.getMinimumSize(rows) :
                       super.minimumSize();
        }
    }

    /**
     * 确定此滚动列表的最小尺寸。
     * @return       显示此滚动列表所需的最小尺寸
     * @see          java.awt.Component#getMinimumSize()
     * @since        JDK1.1
     */
    public Dimension getMinimumSize() {
        return minimumSize();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * replaced by <code>getMinimumSize()</code>.
     */
    @Deprecated
    public Dimension minimumSize() {
        synchronized (getTreeLock()) {
            return (rows > 0) ? minimumSize(rows) : super.minimumSize();
        }
    }

    /**
     * 添加指定的项目监听器以接收此列表的项目事件。项目事件是在响应用户输入时发送的，而不是在调用 <code>select</code> 或 <code>deselect</code> 时发送的。
     * 如果监听器 <code>l</code> 为 <code>null</code>，
     * 不会抛出异常，也不会执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param         l 项目监听器
     * @see           #removeItemListener
     * @see           #getItemListeners
     * @see           #select
     * @see           #deselect
     * @see           java.awt.event.ItemEvent
     * @see           java.awt.event.ItemListener
     * @since         JDK1.1
     */
    public synchronized void addItemListener(ItemListener l) {
        if (l == null) {
            return;
        }
        itemListener = AWTEventMulticaster.add(itemListener, l);
        newEventsOnly = true;
    }

    /**
     * 移除指定的项目监听器，使其不再接收此列表的项目事件。
     * 如果监听器 <code>l</code> 为 <code>null</code>，
     * 不会抛出异常，也不会执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param           l 项目监听器
     * @see             #addItemListener
     * @see             #getItemListeners
     * @see             java.awt.event.ItemEvent
     * @see             java.awt.event.ItemListener
     * @since           JDK1.1
     */
    public synchronized void removeItemListener(ItemListener l) {
        if (l == null) {
            return;
        }
        itemListener = AWTEventMulticaster.remove(itemListener, l);
    }

    /**
     * 返回注册在此列表上的所有项目监听器的数组。
     *
     * @return 此列表的所有 <code>ItemListener</code>s
     *         或者如果当前没有注册项目监听器，则返回一个空数组
     *
     * @see             #addItemListener
     * @see             #removeItemListener
     * @see             java.awt.event.ItemEvent
     * @see             java.awt.event.ItemListener
     * @since 1.4
     */
    public synchronized ItemListener[] getItemListeners() {
        return getListeners(ItemListener.class);
    }

    /**
     * 添加指定的动作监听器以接收此列表的动作事件。动作事件发生在用户双击
     * 列表项或在列表具有键盘焦点时按下 Enter 键时。
     * <p>
     * 如果监听器 <code>l</code> 为 <code>null</code>，
     * 不会抛出异常，也不会执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param         l 动作监听器
     * @see           #removeActionListener
     * @see           #getActionListeners
     * @see           java.awt.event.ActionEvent
     * @see           java.awt.event.ActionListener
     * @since         JDK1.1
     */
    public synchronized void addActionListener(ActionListener l) {
        if (l == null) {
            return;
        }
        actionListener = AWTEventMulticaster.add(actionListener, l);
        newEventsOnly = true;
    }


                /**
     * 移除指定的动作监听器，使其不再从该列表接收动作事件。动作事件
     * 在用户双击列表项时发生。
     * 如果监听器 <code>l</code> 为 <code>null</code>，
     * 不会抛出异常且不执行任何操作。
     * <p>有关 AWT 的线程模型，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param           l     动作监听器
     * @see             #addActionListener
     * @see             #getActionListeners
     * @see             java.awt.event.ActionEvent
     * @see             java.awt.event.ActionListener
     * @since           JDK1.1
     */
    public synchronized void removeActionListener(ActionListener l) {
        if (l == null) {
            return;
        }
        actionListener = AWTEventMulticaster.remove(actionListener, l);
    }

    /**
     * 返回注册到此列表的所有动作监听器的数组。
     *
     * @return 该列表的所有 <code>ActionListener</code> 的数组
     *         或者如果没有注册动作监听器，则返回空数组
     *
     * @see             #addActionListener
     * @see             #removeActionListener
     * @see             java.awt.event.ActionEvent
     * @see             java.awt.event.ActionListener
     * @since 1.4
     */
    public synchronized ActionListener[] getActionListeners() {
        return getListeners(ActionListener.class);
    }

    /**
     * 返回当前注册为 <code><em>Foo</em>Listener</code> 的所有对象的数组
     * 到此 <code>List</code>。
     * <code><em>Foo</em>Listener</code> 通过 <code>add<em>Foo</em>Listener</code> 方法注册。
     *
     * <p>
     * 可以使用类字面量指定 <code>listenerType</code> 参数，例如
     * <code><em>Foo</em>Listener.class</code>。
     * 例如，可以使用以下代码查询一个
     * <code>List</code> <code>l</code>
     * 的项目监听器：
     *
     * <pre>ItemListener[] ils = (ItemListener[])(l.getListeners(ItemListener.class));</pre>
     *
     * 如果没有此类监听器存在，此方法返回空数组。
     *
     * @param listenerType 请求的监听器类型；此参数
     *          应指定一个继承自
     *          <code>java.util.EventListener</code> 的接口
     * @return 一个包含所有注册为
     *          <code><em>Foo</em>Listener</code> 的对象的数组，
     *          或者如果没有注册此类
     *          监听器，则返回空数组
     * @exception ClassCastException 如果 <code>listenerType</code>
     *          不指定一个实现
     *          <code>java.util.EventListener</code> 的类或接口
     *
     * @see #getItemListeners
     * @since 1.3
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if  (listenerType == ActionListener.class) {
            l = actionListener;
        } else if  (listenerType == ItemListener.class) {
            l = itemListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e) {
        switch(e.id) {
          case ActionEvent.ACTION_PERFORMED:
            if ((eventMask & AWTEvent.ACTION_EVENT_MASK) != 0 ||
                actionListener != null) {
                return true;
            }
            return false;
          case ItemEvent.ITEM_STATE_CHANGED:
            if ((eventMask & AWTEvent.ITEM_EVENT_MASK) != 0 ||
                itemListener != null) {
                return true;
            }
            return false;
          default:
            break;
        }
        return super.eventEnabled(e);
    }

    /**
     * 处理此滚动列表上的事件。如果事件是
     * <code>ItemEvent</code> 的实例，它调用
     * <code>processItemEvent</code> 方法。否则，如果事件是
     * <code>ActionEvent</code> 的实例，它调用 <code>processActionEvent</code>。
     * 如果事件不是项目事件或动作事件，它调用超类的 <code>processEvent</code>。
     * <p>注意，如果事件参数为 <code>null</code>
     * 行为是未指定的，可能会导致异常。
     *
     * @param        e 事件
     * @see          java.awt.event.ActionEvent
     * @see          java.awt.event.ItemEvent
     * @see          #processActionEvent
     * @see          #processItemEvent
     * @since        JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof ItemEvent) {
            processItemEvent((ItemEvent)e);
            return;
        } else if (e instanceof ActionEvent) {
            processActionEvent((ActionEvent)e);
            return;
        }
        super.processEvent(e);
    }

    /**
     * 通过将它们分派给任何注册的
     * <code>ItemListener</code> 对象来处理此列表上发生的项目事件。
     * <p>
     * 除非为该组件启用了项目事件，否则不会调用此方法。项目事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addItemListener</code> 注册了 <code>ItemListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了项目事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>
     * 行为是未指定的，可能会导致异常。
     *
     * @param       e 项目事件
     * @see         java.awt.event.ItemEvent
     * @see         java.awt.event.ItemListener
     * @see         #addItemListener
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected void processItemEvent(ItemEvent e) {
        ItemListener listener = itemListener;
        if (listener != null) {
            listener.itemStateChanged(e);
        }
    }

    /**
     * 通过将它们分派给任何注册的
     * <code>ActionListener</code> 对象来处理此组件上发生的动作事件。
     * <p>
     * 除非为该组件启用了动作事件，否则不会调用此方法。动作事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addActionListener</code> 注册了 <code>ActionListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了动作事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>
     * 行为是未指定的，可能会导致异常。
     *
     * @param       e 动作事件
     * @see         java.awt.event.ActionEvent
     * @see         java.awt.event.ActionListener
     * @see         #addActionListener
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected void processActionEvent(ActionEvent e) {
        ActionListener listener = actionListener;
        if (listener != null) {
            listener.actionPerformed(e);
        }
    }

    /**
     * 返回表示此滚动列表状态的参数字符串。此字符串对调试很有用。
     * @return    此滚动列表的参数字符串
     */
    protected String paramString() {
        return super.paramString() + ",selected=" + getSelectedItem();
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 未来不再供公共使用。
     * 预计仅保留为包私有方法。
     */
    @Deprecated
    public synchronized void delItems(int start, int end) {
        for (int i = end; i >= start; i--) {
            items.removeElementAt(i);
        }
        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
            peer.delItems(start, end);
        }
    }

    /*
     * 序列化支持。由于 selected 字段的值不一定是最新的，因此在序列化之前
     * 与 peer 同步。
     */

    /**
     * <code>List</code> 组件的
     * 序列化数据版本。
     *
     * @serial
     */
    private int listSerializedDataVersion = 1;

    /**
     * 将默认的可序列化字段写入流。写入
     * 一个可选数据的项目监听器和动作监听器的列表。
     * 非可序列化的监听器被检测到，不会尝试序列化它们。
     *
     * @serialData 以 <code>null</code> 结尾的 0
     * 个或更多对的序列；每对由一个 <code>String</code>
     * 和一个 <code>Object</code> 组成；<code>String</code>
     * 表示对象的类型，可以是以下之一：
     *  <code>itemListenerK</code> 表示一个
     *    <code>ItemListener</code> 对象；
     *  <code>actionListenerK</code> 表示一个
     *    <code>ActionListener</code> 对象
     *
     * @param s 要写入的 <code>ObjectOutputStream</code>
     * @see AWTEventMulticaster#save(ObjectOutputStream, String, EventListener)
     * @see java.awt.Component#itemListenerK
     * @see java.awt.Component#actionListenerK
     * @see #readObject(ObjectInputStream)
     */
    private void writeObject(ObjectOutputStream s)
      throws IOException
    {
      synchronized (this) {
        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
          selected = peer.getSelectedIndexes();
        }
      }
      s.defaultWriteObject();

      AWTEventMulticaster.save(s, itemListenerK, itemListener);
      AWTEventMulticaster.save(s, actionListenerK, actionListener);
      s.writeObject(null);
    }

    /**
     * 读取 <code>ObjectInputStream</code> 并如果它
     * 不是 <code>null</code>，则添加一个监听器以接收
     * 由 <code>List</code> 触发的项目事件和动作事件（由
     * 流中存储的键指定）。
     * 未识别的键或值将被忽略。
     *
     * @param s 要读取的 <code>ObjectInputStream</code>
     * @exception HeadlessException 如果
     *   <code>GraphicsEnvironment.isHeadless</code> 返回
     *   <code>true</code>
     * @see #removeItemListener(ItemListener)
     * @see #addItemListener(ItemListener)
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see #writeObject(ObjectOutputStream)
     */
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException
    {
      GraphicsEnvironment.checkHeadless();
      s.defaultReadObject();

      Object keyOrNull;
      while(null != (keyOrNull = s.readObject())) {
        String key = ((String)keyOrNull).intern();

        if (itemListenerK == key)
          addItemListener((ItemListener)(s.readObject()));

        else if (actionListenerK == key)
          addActionListener((ActionListener)(s.readObject()));

        else // 跳过未识别键的值
          s.readObject();
      }
    }


/////////////////
// Accessibility support
////////////////


    /**
     * 获取与此 <code>List</code> 关联的 <code>AccessibleContext</code>。对于列表，
     * <code>AccessibleContext</code> 采用 <code>AccessibleAWTList</code> 的形式。
     * 如果必要，将创建一个新的 <code>AccessibleAWTList</code> 实例。
     *
     * @return 一个 <code>AccessibleAWTList</code>，作为此 <code>List</code> 的
     *         <code>AccessibleContext</code>
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTList();
        }
        return accessibleContext;
    }

    /**
     * 为 <code>List</code> 类实现辅助功能支持。
     * 它为列表用户界面元素提供了适当的 Java 辅助功能 API 实现。
     * @since 1.3
     */
    protected class AccessibleAWTList extends AccessibleAWTComponent
        implements AccessibleSelection, ItemListener, ActionListener
    {
        /*
         * JDK 1.3 serialVersionUID
         */
        private static final long serialVersionUID = 7924617370136012829L;

        public AccessibleAWTList() {
            super();
            List.this.addActionListener(this);
            List.this.addItemListener(this);
        }

        public void actionPerformed(ActionEvent event)  {
        }

        public void itemStateChanged(ItemEvent event)  {
        }

        /**
         * 获取此对象的状态集。
         *
         * @return 一个包含对象当前状态的 AccessibleState 实例
         * @see AccessibleState
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (List.this.isMultipleMode())  {
                states.add(AccessibleState.MULTISELECTABLE);
            }
            return states;
        }

        /**
         * 获取此对象的角色。
         *
         * @return 一个描述对象角色的 AccessibleRole 实例
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.LIST;
        }

        /**
         * 返回对象中包含在本地坐标点的 Accessible 子对象，如果存在。
         *
         * @return 指定位置的 Accessible，如果存在
         */
        public Accessible getAccessibleAt(Point p) {
            return null; // fredxFIXME 尚未实现
        }

        /**
         * 返回对象中的可访问子对象数量。如果此对象的所有子对象都实现了 Accessible，
         * 那么此方法应返回此对象的子对象数量。
         *
         * @return 对象中的可访问子对象数量。
         */
        public int getAccessibleChildrenCount() {
            return List.this.getItemCount();
        }

        /**
         * 返回对象的第 n 个可访问子对象。
         *
         * @param i 从零开始的子对象索引
         * @return 对象的第 n 个可访问子对象
         */
        public Accessible getAccessibleChild(int i) {
            synchronized(List.this)  {
                if (i >= List.this.getItemCount()) {
                    return null;
                } else {
                    return new AccessibleAWTListChild(List.this, i);
                }
            }
        }

        /**
         * 获取与此对象关联的 AccessibleSelection。在此类的 Java 辅助功能 API 实现中，
         * 返回此对象，该对象负责代表自身实现 AccessibleSelection 接口。
         *
         * @return 此对象
         */
        public AccessibleSelection getAccessibleSelection() {
            return this;
        }


                // AccessibleSelection 方法

        /**
         * 返回当前选中的项目数量。
         * 如果没有选中的项目，返回值将为 0。
         *
         * @return 当前选中的项目数量。
         */
         public int getAccessibleSelectionCount() {
             return List.this.getSelectedIndexes().length;
         }

        /**
         * 返回表示对象中指定选中项目的 Accessible。
         * 如果没有选中项目，或者选中的项目数量少于传入的整数，返回值将为 null。
         *
         * @param i 选中项目的零基索引
         * @return 包含选中项目的 Accessible
         */
         public Accessible getAccessibleSelection(int i) {
             synchronized(List.this)  {
                 int len = getAccessibleSelectionCount();
                 if (i < 0 || i >= len) {
                     return null;
                 } else {
                     return getAccessibleChild(List.this.getSelectedIndexes()[i]);
                 }
             }
         }

        /**
         * 如果此对象的当前子对象被选中，则返回 true。
         *
         * @param i 此 Accessible 对象中的子对象的零基索引。
         * @see AccessibleContext#getAccessibleChild
         */
        public boolean isAccessibleChildSelected(int i) {
            return List.this.isIndexSelected(i);
        }

        /**
         * 将对象中的指定选中项目添加到对象的选中项中。如果对象支持多选，
         * 指定的项目将被添加到任何现有的选中项中，否则它将替换对象中的任何现有选中项。如果
         * 指定的项目已经被选中，此方法没有效果。
         *
         * @param i 可选项目的零基索引
         */
         public void addAccessibleSelection(int i) {
             List.this.select(i);
         }

        /**
         * 从对象的选中项中移除指定的选中项目。如果指定的项目当前未被选中，此
         * 方法没有效果。
         *
         * @param i 可选项目的零基索引
         */
         public void removeAccessibleSelection(int i) {
             List.this.deselect(i);
         }

        /**
         * 清除对象中的选中项，使对象中没有任何项目被选中。
         */
         public void clearAccessibleSelection() {
             synchronized(List.this)  {
                 int selectedIndexes[] = List.this.getSelectedIndexes();
                 if (selectedIndexes == null)
                     return;
                 for (int i = selectedIndexes.length - 1; i >= 0; i--) {
                     List.this.deselect(selectedIndexes[i]);
                 }
             }
         }

        /**
         * 如果对象支持多选，使对象中的每个选中项目都被选中。
         */
         public void selectAllAccessibleSelection() {
             synchronized(List.this)  {
                 for (int i = List.this.getItemCount() - 1; i >= 0; i--) {
                     List.this.select(i);
                 }
             }
         }

       /**
        * 此类实现了 List 子对象的可访问性支持。它为列表子对象
        * 用户界面元素提供了 Java 可访问性 API 的实现。
        * @since 1.3
        */
        protected class AccessibleAWTListChild extends AccessibleAWTComponent
            implements Accessible
        {
            /*
             * JDK 1.3 serialVersionUID
             */
            private static final long serialVersionUID = 4412022926028300317L;

        // [[[FIXME]]] 需要完成此实现!!!

            private List parent;
            private int  indexInParent;

            public AccessibleAWTListChild(List parent, int indexInParent)  {
                this.parent = parent;
                this.setAccessibleParent(parent);
                this.indexInParent = indexInParent;
            }

            //
            // 必要的 Accessible 方法
            //
          /**
           * 获取此对象的 AccessibleContext。在此类的 Java 可访问性 API 实现中，
           * 返回此对象，该对象充当自己的 AccessibleContext。
           *
           * @return 此对象
           */
            public AccessibleContext getAccessibleContext() {
                return this;
            }

            //
            // 必要的 AccessibleContext 方法
            //

            /**
             * 获取此对象的角色。
             *
             * @return 一个 AccessibleRole 实例，描述对象的角色
             * @see AccessibleRole
             */
            public AccessibleRole getAccessibleRole() {
                return AccessibleRole.LIST_ITEM;
            }

            /**
             * 获取此对象的状态集。对象的 AccessibleStateSet 由一组唯一的 AccessibleState 组成。对象的
             * AccessibleStateSet 的更改将导致为 ACCESSIBLE_STATE_PROPERTY 属性触发 PropertyChangeEvent。
             *
             * @return 一个 AccessibleStateSet 实例，包含对象的当前状态集
             * @see AccessibleStateSet
             * @see AccessibleState
             * @see #addPropertyChangeListener
             */
            public AccessibleStateSet getAccessibleStateSet() {
                AccessibleStateSet states = super.getAccessibleStateSet();
                if (parent.isIndexSelected(indexInParent)) {
                    states.add(AccessibleState.SELECTED);
                }
                return states;
            }

            /**
             * 获取组件的区域设置。如果组件没有区域设置，则返回其父组件的区域设置。
             *
             * @return 此组件的区域设置。如果此组件没有自己的区域设置，则返回其父组件的区域设置。
             *
             * @exception IllegalComponentStateException
             * 如果组件没有自己的区域设置且尚未被添加到包含层次结构中，使得无法从包含的父组件确定区域设置。
             */
            public Locale getLocale() {
                return parent.getLocale();
            }

            /**
             * 获取此对象在其可访问父对象中的 0 基索引。
             *
             * @return 此对象在其父对象中的 0 基索引；如果此对象没有可访问父对象，则返回 -1。
             *
             * @see #getAccessibleParent
             * @see #getAccessibleChildrenCount
             * @see #getAccessibleChild
             */
            public int getAccessibleIndexInParent() {
                return indexInParent;
            }

            /**
             * 返回对象的可访问子对象数量。
             *
             * @return 对象的可访问子对象数量。
             */
            public int getAccessibleChildrenCount() {
                return 0;       // 列表元素不能有子对象
            }

            /**
             * 返回指定的可访问子对象。对象的可访问子对象从零开始编号，
             * 因此对象的第一个可访问子对象在索引 0，第二个子对象在索引 1，依此类推。
             *
             * @param i 子对象的零基索引
             * @return 对象的可访问子对象
             * @see #getAccessibleChildrenCount
             */
            public Accessible getAccessibleChild(int i) {
                return null;    // 列表元素不能有子对象
            }


            //
            // AccessibleComponent 委托给父 List
            //

            /**
             * 获取此对象的背景颜色。
             *
             * @return 如果支持，则返回对象的背景颜色；否则，返回 null
             * @see #setBackground
             */
            public Color getBackground() {
                return parent.getBackground();
            }

            /**
             * 设置此对象的背景颜色。
             *
             * @param c 背景的新颜色
             * @see #setBackground
             */
            public void setBackground(Color c) {
                parent.setBackground(c);
            }

            /**
             * 获取此对象的前景颜色。
             *
             * @return 如果支持，则返回对象的前景颜色；否则，返回 null
             * @see #setForeground
             */
            public Color getForeground() {
                return parent.getForeground();
            }

            /**
             * 设置此对象的前景颜色。
             *
             * @param c 前景的新颜色
             * @see #getForeground
             */
            public void setForeground(Color c) {
                parent.setForeground(c);
            }

            /**
             * 获取此对象的光标。
             *
             * @return 如果支持，则返回对象的光标；否则，返回 null
             * @see #setCursor
             */
            public Cursor getCursor() {
                return parent.getCursor();
            }

            /**
             * 设置此对象的光标。
             * <p>
             * 如果 Java 平台实现和/或本机系统不支持更改鼠标光标的形状，此方法可能没有视觉效果。
             * @param cursor 对象的新光标
             * @see #getCursor
             */
            public void setCursor(Cursor cursor) {
                parent.setCursor(cursor);
            }

            /**
             * 获取此对象的字体。
             *
             * @return 如果支持，则返回对象的字体；否则，返回 null
             * @see #setFont
             */
            public Font getFont() {
                return parent.getFont();
            }

            /**
             * 设置此对象的字体。
             *
             * @param f 对象的新字体
             * @see #getFont
             */
            public void setFont(Font f) {
                parent.setFont(f);
            }

            /**
             * 获取此对象的 FontMetrics。
             *
             * @param f 字体
             * @return 如果支持，则返回对象的 FontMetrics；否则，返回 null
             * @see #getFont
             */
            public FontMetrics getFontMetrics(Font f) {
                return parent.getFontMetrics(f);
            }

            /**
             * 确定对象是否已启用。已启用的对象在其 AccessibleStateSet 中也会设置 AccessibleState.ENABLED 状态。
             *
             * @return 如果对象已启用，则返回 true；否则，返回 false
             * @see #setEnabled
             * @see AccessibleContext#getAccessibleStateSet
             * @see AccessibleState#ENABLED
             * @see AccessibleStateSet
             */
            public boolean isEnabled() {
                return parent.isEnabled();
            }

            /**
             * 设置对象的启用状态。
             *
             * @param b 如果为 true，则启用此对象；否则，禁用此对象
             * @see #isEnabled
             */
            public void setEnabled(boolean b) {
                parent.setEnabled(b);
            }

            /**
             * 确定对象是否可见。注意：这表示对象意图可见；但是，由于包含此对象的对象当前不可见，对象可能不会显示在屏幕上。要确定对象是否显示在屏幕上，请使用 isShowing()。
             * <p>可见的对象在其 AccessibleStateSet 中也会设置 AccessibleState.VISIBLE 状态。
             *
             * @return 如果对象可见，则返回 true；否则，返回 false
             * @see #setVisible
             * @see AccessibleContext#getAccessibleStateSet
             * @see AccessibleState#VISIBLE
             * @see AccessibleStateSet
             */
            public boolean isVisible() {
                // [[[FIXME]]] 需要像 isShowing() 一样工作
                return false;
                // return parent.isVisible();
            }

            /**
             * 设置对象的可见状态。
             *
             * @param b 如果为 true，则显示此对象；否则，隐藏此对象
             * @see #isVisible
             */
            public void setVisible(boolean b) {
                // [[[FIXME]]] 应该滚动到项目以使其显示！
                parent.setVisible(b);
            }

            /**
             * 确定对象是否显示。这是通过检查对象及其祖先对象的可见性来确定的。
             * 注意：即使对象被其他对象（例如，被拉下的菜单）遮挡，此方法也会返回 true。
             *
             * @return 如果对象显示，则返回 true；否则，返回 false
             */
            public boolean isShowing() {
                // [[[FIXME]]] 仅当对象显示时才返回 true！
                return false;
                // return parent.isShowing();
            }

            /**
             * 检查指定的点是否在对象的边界内，其中点的 x 和 y 坐标相对于对象的坐标系定义。
             *
             * @param p 相对于对象坐标系的点
             * @return 如果对象包含点，则返回 true；否则，返回 false
             * @see #getBounds
             */
            public boolean contains(Point p) {
                // [[[FIXME]]] - 仅当 p 在列表元素内时才返回 true！
                return false;
                // return parent.contains(p);
            }

            /**
             * 返回对象在屏幕上的位置。
             *
             * @return 对象在屏幕上的位置；如果此对象不在屏幕上，则返回 null
             * @see #getBounds
             * @see #getLocation
             */
            public Point getLocationOnScreen() {
                // [[[FIXME]]] 唉
                return null;
            }


                        /**
             * 获取对象相对于其父对象的位置，形式为一个点，表示对象在屏幕坐标空间中的左上角。
             *
             * @return 一个表示对象边界左上角的 Point 实例，在屏幕坐标空间中；如果此对象或其父对象不在屏幕上，则返回 null
             * @see #getBounds
             * @see #getLocationOnScreen
             */
            public Point getLocation() {
                // [[[FIXME]]]
                return null;
            }

            /**
             * 设置对象相对于其父对象的位置。
             * @param p 顶角的新位置
             * @see #getLocation
             */
            public void setLocation(Point p) {
                // [[[FIXME]]] 可能 - 可以简单地返回为空操作
            }

            /**
             * 以 Rectangle 对象的形式获取此对象的边界。边界指定了此对象的宽度、高度和相对于其父对象的位置。
             *
             * @return 一个表示此组件边界的矩形；如果此对象不在屏幕上，则返回 null。
             * @see #contains
             */
            public Rectangle getBounds() {
                // [[[FIXME]]]
                return null;
            }

            /**
             * 以 Rectangle 对象的形式设置此对象的边界。边界指定了此对象的宽度、高度和相对于其父对象的位置。
             *
             * @param r 表示此组件边界的矩形
             * @see #getBounds
             */
            public void setBounds(Rectangle r) {
                // 不支持；空操作
            }

            /**
             * 以 Dimension 对象的形式返回此对象的大小。Dimension 对象的 height 字段包含此对象的高度，width 字段包含此对象的宽度。
             *
             * @return 一个表示此组件大小的 Dimension 对象；如果此对象不在屏幕上，则返回 null
             * @see #setSize
             */
            public Dimension getSize() {
                // [[[FIXME]]]
                return null;
            }

            /**
             * 调整此对象的大小，使其具有指定的宽度和高度。
             *
             * @param d - 指定对象新大小的维度。
             * @see #getSize
             */
            public void setSize(Dimension d) {
                // 不支持；空操作
            }

            /**
             * 返回在本地坐标 <code>Point</code> 处的 <code>Accessible</code> 子对象，如果存在的话。
             *
             * @param p 相对于此对象坐标系的点
             * @return 如果存在，则返回指定位置的 <code>Accessible</code>；否则返回 <code>null</code>
             */
            public Accessible getAccessibleAt(Point p) {
                return null;    // 对象不能有子对象！
            }

            /**
             * 返回此对象是否可以接受焦点。可以接受焦点的对象在其 <code>AccessibleStateSet</code> 中也会设置 <code>AccessibleState.FOCUSABLE</code> 状态。
             *
             * @return 如果对象可以接受焦点，则返回 true；否则返回 false
             * @see AccessibleContext#getAccessibleStateSet
             * @see AccessibleState#FOCUSABLE
             * @see AccessibleState#FOCUSED
             * @see AccessibleStateSet
             */
            public boolean isFocusTraversable() {
                return false;   // 列表元素不能接收焦点！
            }

            /**
             * 请求此对象的焦点。如果此对象不能接受焦点，则不会发生任何事情。否则，对象将尝试获取焦点。
             * @see #isFocusTraversable
             */
            public void requestFocus() {
                // 无事可做；空操作
            }

            /**
             * 添加指定的焦点监听器以接收此组件的焦点事件。
             *
             * @param l 焦点监听器
             * @see #removeFocusListener
             */
            public void addFocusListener(FocusListener l) {
                // 无事可做；空操作
            }

            /**
             * 移除指定的焦点监听器，使其不再接收此组件的焦点事件。
             *
             * @param l 焦点监听器
             * @see #addFocusListener
             */
            public void removeFocusListener(FocusListener l) {
                // 无事可做；空操作
            }



        } // inner class AccessibleAWTListChild

    } // inner class AccessibleAWTList

}
