
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

import java.util.*;
import java.awt.peer.ChoicePeer;
import java.awt.event.*;
import java.util.EventListener;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import javax.accessibility.*;


/**
 * <code>Choice</code> 类呈现一个选项菜单。
 * 当前选择项显示为菜单的标题。
 * <p>
 * 以下代码示例生成一个下拉菜单：
 *
 * <hr><blockquote><pre>
 * Choice ColorChooser = new Choice();
 * ColorChooser.add("Green");
 * ColorChooser.add("Red");
 * ColorChooser.add("Blue");
 * </pre></blockquote><hr>
 * <p>
 * 将此选择菜单添加到面板后，它在正常状态下的外观如下：
 * <p>
 * <img src="doc-files/Choice-1.gif" alt="The following text describes the graphic"
 * style="float:center; margin: 7px 10px;">
 * <p>
 * 在图片中，<code>"Green"</code> 是当前选择项。
 * 按下鼠标按钮时，会弹出一个带有当前选择项高亮显示的菜单。
 * <p>
 * 一些原生平台不支持任意大小的 <code>Choice</code> 组件，
 * <code>setSize()/getSize()</code> 的行为受这些限制的影响。
 * 原生 GUI <code>Choice</code> 组件的大小通常受字体大小和 <code>Choice</code> 中包含的项目长度等属性的限制。
 * <p>
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 * @since       JDK1.0
 */
public class Choice extends Component implements ItemSelectable, Accessible {
    /**
     * <code>Choice</code> 的项目。
     * 可以是 <code>null</code> 值。
     * @serial
     * @see #add(String)
     * @see #addItem(String)
     * @see #getItem(int)
     * @see #getItemCount()
     * @see #insert(String, int)
     * @see #remove(String)
     */
    Vector<String> pItems;

    /**
     * 当前选择项的索引，如果没有选择任何项则为 -1。
     * @serial
     * @see #getSelectedItem()
     * @see #select(int)
     */
    int selectedIndex = -1;

    transient ItemListener itemListener;

    private static final String base = "choice";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -4075310674757313071L;

    static {
        /* 确保加载必要的原生库 */
        Toolkit.loadLibraries();
        /* 初始化 JNI 字段和方法 ID */
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * 创建一个新的选择菜单。菜单最初没有任何项目。
     * <p>
     * 默认情况下，添加到选择菜单中的第一个项目将成为选定项目，
     * 直到用户通过调用其中一个 <code>select</code> 方法选择不同的项目。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       #select(int)
     * @see       #select(java.lang.String)
     */
    public Choice() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        pItems = new Vector<>();
    }

    /**
     * 为这个组件构造一个名称。当名称为 <code>null</code> 时由
     * <code>getName</code> 调用。
     */
    String constructComponentName() {
        synchronized (Choice.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 创建 <code>Choice</code> 的对等体。此对等体允许我们更改
     * <code>Choice</code> 的外观而不改变其功能。
     * @see     java.awt.Toolkit#createChoice(java.awt.Choice)
     * @see     java.awt.Component#getToolkit()
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createChoice(this);
            super.addNotify();
        }
    }

    /**
     * 返回此 <code>Choice</code> 菜单中的项目数量。
     * @return 此 <code>Choice</code> 菜单中的项目数量
     * @see     #getItem
     * @since   JDK1.1
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
        return pItems.size();
    }

    /**
     * 获取此 <code>Choice</code> 菜单中指定索引处的字符串。
     * @param      index 开始的索引
     * @see        #getItemCount
     */
    public String getItem(int index) {
        return getItemImpl(index);
    }

    /*
     * 由原生代码调用，因此客户端代码不能在工具包线程上调用。
     */
    final String getItemImpl(int index) {
        return pItems.elementAt(index);
    }

    /**
     * 向此 <code>Choice</code> 菜单中添加一个项目。
     * @param      item    要添加的项目
     * @exception  NullPointerException   如果项目的值为
     *                  <code>null</code>
     * @since      JDK1.1
     */
    public void add(String item) {
        addItem(item);
    }

    /**
     * 自 Java 2 平台 v1.1 起已过时。请改用 <code>add</code> 方法。
     * <p>
     * 向此 <code>Choice</code> 菜单中添加一个项目。
     * @param item 要添加的项目
     * @exception NullPointerException 如果项目的值等于
     *          <code>null</code>
     */
    public void addItem(String item) {
        synchronized (this) {
            insertNoInvalidate(item, pItems.size());
        }

        // 这可能会改变组件的首选大小。
        invalidateIfValid();
    }

    /**
     * 向此 <code>Choice</code> 中插入一个项目，但不使 <code>Choice</code> 无效。
     * 客户端方法在调用此方法之前必须提供自己的同步。
     * @param item 要添加的项目
     * @param index 新项目的索引
     * @exception NullPointerException 如果项目的值等于
     *          <code>null</code>
     */
    private void insertNoInvalidate(String item, int index) {
        if (item == null) {
            throw new
                NullPointerException("不能将 null 项目添加到 Choice");
        }
        pItems.insertElementAt(item, index);
        ChoicePeer peer = (ChoicePeer)this.peer;
        if (peer != null) {
            peer.add(item, index);
        }
        // 没有选择或选择向上移动
        if (selectedIndex < 0 || selectedIndex >= index) {
            select(0);
        }
    }


    /**
     * 在指定位置插入项目。
     * 索引大于或等于 <code>index</code> 的现有项目将向上移动一位以容纳
     * 新项目。如果 <code>index</code> 大于或
     * 等于此选择中的项目数量，则将 <code>item</code> 添加到此选择的末尾。
     * <p>
     * 如果添加的项目是第一个项目，则该项目成为选定项目。否则，如果
     * 选定项目是被移动的项目之一，则选择菜单中的第一个项目成为选定项目。如果
     * 选定项目不在被移动的项目中，则它仍然是选定项目。
     * @param item 要插入的非 <code>null</code> 项目
     * @param index 项目应插入的位置
     * @exception IllegalArgumentException 如果索引小于 0
     */
    public void insert(String item, int index) {
        synchronized (this) {
            if (index < 0) {
                throw new IllegalArgumentException("索引小于零。");
            }
            /* 如果索引大于项目数量，将项目添加到末尾 */
            index = Math.min(index, pItems.size());

            insertNoInvalidate(item, index);
        }

        // 这可能会改变组件的首选大小。
        invalidateIfValid();
    }

    /**
     * 从 <code>Choice</code> 菜单中移除 <code>item</code> 的第一次出现。
     * 如果移除的项目是当前选定项目，则选择菜单中的第一个项目成为
     * 选定项目。否则，当前选定项目保持选定状态（并且选定索引相应更新）。
     * @param      item  要从 <code>Choice</code> 菜单中移除的项目
     * @exception  IllegalArgumentException  如果项目不在选择菜单中
     * @since      JDK1.1
     */
    public void remove(String item) {
        synchronized (this) {
            int index = pItems.indexOf(item);
            if (index < 0) {
                throw new IllegalArgumentException("项目 " + item +
                                                   " 未在选择菜单中找到");
            } else {
                removeNoInvalidate(index);
            }
        }

        // 这可能会改变组件的首选大小。
        invalidateIfValid();
    }

    /**
     * 从选择菜单中移除指定位置的项目。如果移除的项目是当前选定项目，
     * 则选择菜单中的第一个项目成为选定项目。否则，当前选定项目保持选定状态
     * （并且选定索引相应更新）。
     * @param      position 项目的索引
     * @throws IndexOutOfBoundsException 如果指定的索引超出范围
     * @since      JDK1.1
     */
    public void remove(int position) {
        synchronized (this) {
            removeNoInvalidate(position);
        }

        // 这可能会改变组件的首选大小。
        invalidateIfValid();
    }

    /**
     * 从 <code>Choice</code> 中移除指定位置的项目，但不使 <code>Choice</code> 无效。
     * 客户端方法在调用此方法之前必须提供自己的同步。
     * @param      position 项目的索引
     */
    private void removeNoInvalidate(int position) {
        pItems.removeElementAt(position);
        ChoicePeer peer = (ChoicePeer)this.peer;
        if (peer != null) {
            peer.remove(position);
        }
        /* 如果移除的是选定项目，调整 selectedIndex。 */
        if (pItems.size() == 0) {
            selectedIndex = -1;
        } else if (selectedIndex == position) {
            select(0);
        } else if (selectedIndex > position) {
            select(selectedIndex-1);
        }
    }


    /**
     * 从选择菜单中移除所有项目。
     * @see       #remove
     * @since     JDK1.1
     */
    public void removeAll() {
        synchronized (this) {
            if (peer != null) {
                ((ChoicePeer)peer).removeAll();
            }
            pItems.removeAllElements();
            selectedIndex = -1;
        }

        // 这可能会改变组件的首选大小。
        invalidateIfValid();
    }

    /**
     * 获取当前选择项的字符串表示。
     * @return    当前选择项的字符串表示，如果未选择任何项目则返回 <code>null</code>
     * @see       #getSelectedIndex
     */
    public synchronized String getSelectedItem() {
        return (selectedIndex >= 0) ? getItem(selectedIndex) : null;
    }

    /**
     * 返回一个包含当前选定项目的数组（长度为 1）。如果此选择没有项目，则返回 <code>null</code>。
     * @see ItemSelectable
     */
    public synchronized Object[] getSelectedObjects() {
        if (selectedIndex >= 0) {
            Object[] items = new Object[1];
            items[0] = getItem(selectedIndex);
            return items;
        }
        return null;
    }

    /**
     * 返回当前选定项目的索引。如果没有选择任何项目，则返回 -1。
     *
     * @return 当前选定项目的索引，如果没有选择任何项目则返回 -1
     * @see #getSelectedItem
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * 将此 <code>Choice</code> 菜单中的选定项目设置为指定位置的项目。
     *
     * <p>注意，此方法主要用于在组件中初始选择一个项目。
     * 程序调用此方法不会触发 <code>ItemEvent</code>。触发
     * <code>ItemEvent</code> 的唯一方法是用户交互。
     *
     * @param      pos      选定项目的索引
     * @exception  IllegalArgumentException 如果指定的
     *                            索引大于项目数量或小于零
     * @see        #getSelectedItem
     * @see        #getSelectedIndex
     */
    public synchronized void select(int pos) {
        if ((pos >= pItems.size()) || (pos < 0)) {
            throw new IllegalArgumentException("非法 Choice 项目位置: " + pos);
        }
        if (pItems.size() > 0) {
            selectedIndex = pos;
            ChoicePeer peer = (ChoicePeer)this.peer;
            if (peer != null) {
                peer.select(pos);
            }
        }
    }

    /**
     * 将此 <code>Choice</code> 菜单中的选定项目设置为与指定字符串相等的项目。
     * 如果有多个项目与指定字符串匹配（相等），则选择索引最小的项目。
     *
     * <p>注意，此方法主要用于在组件中初始选择一个项目。
     * 程序调用此方法不会触发 <code>ItemEvent</code>。触发
     * <code>ItemEvent</code> 的唯一方法是用户交互。
     *
     * @param       str     指定的字符串
     * @see         #getSelectedItem
     * @see         #getSelectedIndex
     */
    public synchronized void select(String str) {
        int index = pItems.indexOf(str);
        if (index >= 0) {
            select(index);
        }
    }


                /**
     * 将指定的项监听器添加到接收来自此 <code>Choice</code> 菜单的项事件。
     * 项事件是响应用户输入发送的，但不是响应 <code>select</code> 调用发送的。
     * 如果 l 为 <code>null</code>，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     * @param         l    项监听器
     * @see           #removeItemListener
     * @see           #getItemListeners
     * @see           #select
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
     * 移除指定的项监听器，使其不再接收来自此 <code>Choice</code> 菜单的项事件。
     * 如果 l 为 <code>null</code>，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     * @param         l    项监听器
     * @see           #addItemListener
     * @see           #getItemListeners
     * @see           java.awt.event.ItemEvent
     * @see           java.awt.event.ItemListener
     * @since         JDK1.1
     */
    public synchronized void removeItemListener(ItemListener l) {
        if (l == null) {
            return;
        }
        itemListener = AWTEventMulticaster.remove(itemListener, l);
    }

    /**
     * 返回注册在此选择框上的所有项监听器的数组。
     *
     * @return 此选择框的所有 <code>ItemListener</code>，如果没有注册项监听器，则返回空数组。
     *
     * @see           #addItemListener
     * @see           #removeItemListener
     * @see           java.awt.event.ItemEvent
     * @see           java.awt.event.ItemListener
     * @since 1.4
     */
    public synchronized ItemListener[] getItemListeners() {
        return getListeners(ItemListener.class);
    }

    /**
     * 返回注册在此 <code>Choice</code> 上的所有 <code><em>Foo</em>Listener</code> 的数组。
     * <code><em>Foo</em>Listener</code> 是使用 <code>add<em>Foo</em>Listener</code> 方法注册的。
     *
     * <p>
     * 您可以使用类字面量指定 <code>listenerType</code> 参数，例如
     * <code><em>Foo</em>Listener.class</code>。
     * 例如，您可以使用以下代码查询一个 <code>Choice</code> <code>c</code>
     * 的项监听器：
     *
     * <pre>ItemListener[] ils = (ItemListener[])(c.getListeners(ItemListener.class));</pre>
     *
     * 如果没有这样的监听器存在，此方法返回一个空数组。
     *
     * @param listenerType 请求的监听器类型；此参数应指定一个实现
     *          <code>java.util.EventListener</code> 的类或接口
     * @return 注册在此选择框上的所有 <code><em>Foo</em>Listener</code> 的数组，
     *          如果没有添加这样的监听器，则返回空数组
     * @exception ClassCastException 如果 <code>listenerType</code>
     *          不指定一个实现 <code>java.util.EventListener</code> 的类或接口
     *
     * @see #getItemListeners
     * @since 1.3
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if  (listenerType == ItemListener.class) {
            l = itemListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e) {
        if (e.id == ItemEvent.ITEM_STATE_CHANGED) {
            if ((eventMask & AWTEvent.ITEM_EVENT_MASK) != 0 ||
                itemListener != null) {
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }

    /**
     * 处理此选择框上的事件。如果事件是 <code>ItemEvent</code> 的实例，则调用
     * <code>processItemEvent</code> 方法。否则，调用其父类的 <code>processEvent</code> 方法。
     * <p>注意，如果事件参数为 <code>null</code>，则行为是未指定的，可能会导致异常。
     *
     * @param      e 事件
     * @see        java.awt.event.ItemEvent
     * @see        #processItemEvent
     * @since      JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof ItemEvent) {
            processItemEvent((ItemEvent)e);
            return;
        }
        super.processEvent(e);
    }

    /**
     * 通过将它们分派给任何注册的 <code>ItemListener</code> 对象来处理在此 <code>Choice</code>
     * 菜单上发生的项事件。
     * <p>
     * 除非为该组件启用了项事件，否则不会调用此方法。项事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addItemListener</code> 注册了 <code>ItemListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了项事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>，则行为是未指定的，可能会导致异常。
     *
     * @param       e 项事件
     * @see         java.awt.event.ItemEvent
     * @see         java.awt.event.ItemListener
     * @see         #addItemListener(ItemListener)
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
     * 返回表示此 <code>Choice</code> 菜单状态的字符串。此方法仅用于调试目的，
     * 返回的字符串的内容和格式可能因实现而异。返回的字符串可以为空，但不能为
     * <code>null</code>。
     *
     * @return    此 <code>Choice</code> 菜单的参数字符串
     */
    protected String paramString() {
        return super.paramString() + ",current=" + getSelectedItem();
    }


    /* Serialization support.
     */

    /*
     * Choice Serial Data Version.
     * @serial
     */
    private int choiceSerializedDataVersion = 1;

    /**
     * 将默认的可序列化字段写入流中。将可序列化的 <code>ItemListeners</code>
     * 列表作为可选数据写入。非可序列化的 <code>ItemListeners</code> 会被检测到，
     * 并不会尝试序列化它们。
     *
     * @param s 要写入的 <code>ObjectOutputStream</code>
     * @serialData 以 <code>null</code> 结尾的 0 个或多个对序列；
     *   每个对由一个 <code>String</code> 和一个 <code>Object</code> 组成；
     *   <code>String</code> 表示对象的类型，可以是以下之一：
     *   <code>itemListenerK</code> 表示一个 <code>ItemListener</code> 对象
     *
     * @see AWTEventMulticaster#save(ObjectOutputStream, String, EventListener)
     * @see java.awt.Component#itemListenerK
     * @see #readObject(ObjectInputStream)
     */
    private void writeObject(ObjectOutputStream s)
      throws java.io.IOException
    {
      s.defaultWriteObject();

      AWTEventMulticaster.save(s, itemListenerK, itemListener);
      s.writeObject(null);
    }

    /**
     * 读取 <code>ObjectInputStream</code>，如果它不是 <code>null</code>，
     * 则添加一个监听器以接收 <code>Choice</code> 项触发的项事件。
     * 未识别的键或值将被忽略。
     *
     * @param s 要读取的 <code>ObjectInputStream</code>
     * @exception HeadlessException 如果
     *   <code>GraphicsEnvironment.isHeadless</code> 返回
     *   <code>true</code>
     * @serial
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

        else // skip value for unrecognized key
          s.readObject();
      }
    }

    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();

/////////////////
// Accessibility support
////////////////


    /**
     * 获取与此 <code>Choice</code> 关联的 <code>AccessibleContext</code>。
     * 对于 <code>Choice</code> 组件，<code>AccessibleContext</code> 的形式为
     * <code>AccessibleAWTChoice</code>。如果必要，将创建一个新的 <code>AccessibleAWTChoice</code> 实例。
     *
     * @return 一个 <code>AccessibleAWTChoice</code>，作为此 <code>Choice</code> 的
     *         <code>AccessibleContext</code>
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTChoice();
        }
        return accessibleContext;
    }

    /**
     * 为 <code>Choice</code> 类实现辅助功能支持。它为选择用户界面元素提供了
     * Java 辅助功能 API 的实现。
     * @since 1.3
     */
    protected class AccessibleAWTChoice extends AccessibleAWTComponent
        implements AccessibleAction
    {
        /*
         * JDK 1.3 serialVersionUID
         */
        private static final long serialVersionUID = 7175603582428509322L;

        public AccessibleAWTChoice() {
            super();
        }

        /**
         * 获取与此对象关联的 <code>AccessibleAction</code>。在此类的 Java 辅助功能 API 实现中，
         * 返回此对象，它负责代表自身实现 <code>AccessibleAction</code> 接口。
         *
         * @return 此对象
         * @see AccessibleAction
         */
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        /**
         * 获取此对象的角色。
         *
         * @return 一个 <code>AccessibleRole</code> 实例，描述对象的角色
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.COMBO_BOX;
        }

        /**
         * 返回此对象中可用的辅助操作的数量。如果有多个操作，第一个操作被视为对象的“默认”操作。
         *
         * @return 此对象中的操作数量，从零开始计数
         */
        public int getAccessibleActionCount() {
            return 0;  //  未来版本中将完全实现
        }

        /**
         * 返回指定操作的描述。
         *
         * @param i 操作的零基索引
         * @return 操作的字符串描述
         * @see #getAccessibleActionCount
         */
        public String getAccessibleActionDescription(int i) {
            return null;  //  未来版本中将完全实现
        }

        /**
         * 在对象上执行指定的操作
         *
         * @param i 操作的零基索引
         * @return 如果操作成功执行，则返回 true；否则返回 false。
         * @see #getAccessibleActionCount
         */
        public boolean doAccessibleAction(int i) {
            return false;  //  未来版本中将完全实现
        }

    } // inner class AccessibleAWTChoice

}
