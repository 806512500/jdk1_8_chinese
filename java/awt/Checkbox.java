
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

import java.awt.peer.CheckboxPeer;
import java.awt.event.*;
import java.util.EventListener;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import javax.accessibility.*;


/**
 * 复选框是一个图形组件，可以处于“开”（<code>true</code>）或“关”（<code>false</code>）状态。
 * 单击复选框会将其状态从“开”变为“关”，或从“关”变为“开”。
 * <p>
 * 以下代码示例创建了一个网格布局中的复选框集：
 *
 * <hr><blockquote><pre>
 * setLayout(new GridLayout(3, 1));
 * add(new Checkbox("one", null, true));
 * add(new Checkbox("two"));
 * add(new Checkbox("three"));
 * </pre></blockquote><hr>
 * <p>
 * 该代码示例创建的复选框和网格布局如下图所示：
 * <p>
 * <img src="doc-files/Checkbox-1.gif" alt="以下上下文描述了图形。"
 * style="float:center; margin: 7px 10px;">
 * <p>
 * 标签为<code>one</code>的按钮处于“开”状态，其他两个按钮处于“关”状态。在这个示例中，使用了<code>GridLayout</code>类，三个复选框的状态是独立设置的。
 * <p>
 * 另外，多个复选框可以由一个对象控制，使用<code>CheckboxGroup</code>类。
 * 在复选框组中，任何时候最多只有一个按钮处于“开”状态。单击复选框以将其打开会强制同一组中其他处于“开”状态的复选框变为“关”状态。
 *
 * @author      Sami Shaio
 * @see         java.awt.GridLayout
 * @see         java.awt.CheckboxGroup
 * @since       JDK1.0
 */
public class Checkbox extends Component implements ItemSelectable, Accessible {

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * 复选框的标签。
     * 该字段可以为 null。
     * @serial
     * @see #getLabel()
     * @see #setLabel(String)
     */
    String label;

    /**
     * 复选框的状态。
     * @serial
     * @see #getState()
     * @see #setState(boolean)
     */
    boolean state;

    /**
     * 复选框组。
     * 该字段可以为 null，表示该复选框不是组复选框。
     * @serial
     * @see #getCheckboxGroup()
     * @see #setCheckboxGroup(CheckboxGroup)
     */
    CheckboxGroup group;

    transient ItemListener itemListener;

    private static final String base = "checkbox";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 序列化版本ID
     */
    private static final long serialVersionUID = 7270714317450821763L;

    /**
     * 设置状态和 CheckboxGroup.setSelectedCheckbox 的辅助函数。
     * 应保持包私有。
     */
    void setStateInternal(boolean state) {
        this.state = state;
        CheckboxPeer peer = (CheckboxPeer)this.peer;
        if (peer != null) {
            peer.setState(state);
        }
    }

    /**
     * 创建一个标签为空字符串的复选框。
     * 该复选框的状态设置为“关”，并且不属于任何复选框组。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public Checkbox() throws HeadlessException {
        this("", false, null);
    }

    /**
     * 创建一个具有指定标签的复选框。该复选框的状态设置为“关”，并且不属于任何复选框组。
     *
     * @param     label   该复选框的字符串标签，或 <code>null</code> 表示无标签。
     * @exception HeadlessException 如果
     *      <code>GraphicsEnvironment.isHeadless</code>
     *      返回 <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public Checkbox(String label) throws HeadlessException {
        this(label, false, null);
    }

    /**
     * 创建一个具有指定标签并设置指定状态的复选框。
     * 该复选框不属于任何复选框组。
     *
     * @param     label   该复选框的字符串标签，或 <code>null</code> 表示无标签
     * @param     state    该复选框的初始状态
     * @exception HeadlessException 如果
     *     <code>GraphicsEnvironment.isHeadless</code>
     *     返回 <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public Checkbox(String label, boolean state) throws HeadlessException {
        this(label, state, null);
    }

    /**
     * 构造一个具有指定标签、设置为指定状态并在指定复选框组中的复选框。
     *
     * @param     label   该复选框的字符串标签，或 <code>null</code> 表示无标签。
     * @param     state   该复选框的初始状态。
     * @param     group   该复选框的复选框组，或 <code>null</code> 表示无组。
     * @exception HeadlessException 如果
     *     <code>GraphicsEnvironment.isHeadless</code>
     *     返回 <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since     JDK1.1
     */
    public Checkbox(String label, boolean state, CheckboxGroup group)
        throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.label = label;
        this.state = state;
        this.group = group;
        if (state && (group != null)) {
            group.setSelectedCheckbox(this);
        }
    }

    /**
     * 创建一个具有指定标签、在指定复选框组中并设置为指定状态的复选框。
     *
     * @param     label   该复选框的字符串标签，或 <code>null</code> 表示无标签。
     * @param     group   该复选框的复选框组，或 <code>null</code> 表示无组。
     * @param     state   该复选框的初始状态。
     * @exception HeadlessException 如果
     *    <code>GraphicsEnvironment.isHeadless</code>
     *    返回 <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since     JDK1.1
     */
    public Checkbox(String label, CheckboxGroup group, boolean state)
        throws HeadlessException {
        this(label, state, group);
    }

    /**
     * 为该组件构造一个名称。当名称为 <code>null</code> 时，由 <code>getName</code> 调用。
     *
     * @return 该组件的名称
     */
    String constructComponentName() {
        synchronized (Checkbox.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 创建复选框的对等体。对等体允许您在不改变复选框功能的情况下改变其外观。
     *
     * @see     java.awt.Toolkit#createCheckbox(java.awt.Checkbox)
     * @see     java.awt.Component#getToolkit()
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createCheckbox(this);
            super.addNotify();
        }
    }

    /**
     * 获取该复选框的标签。
     *
     * @return 该复选框的标签，或 <code>null</code> 表示该复选框没有标签。
     * @see      #setLabel(String)
     */
    public String getLabel() {
        return label;
    }

    /**
     * 将该复选框的标签设置为字符串参数。
     *
     * @param    label   要设置为新标签的字符串，或 <code>null</code> 表示无标签。
     * @see      #getLabel
     */
    public void setLabel(String label) {
        boolean testvalid = false;

        synchronized (this) {
            if (label != this.label && (this.label == null ||
                                        !this.label.equals(label))) {
                this.label = label;
                CheckboxPeer peer = (CheckboxPeer)this.peer;
                if (peer != null) {
                    peer.setLabel(label);
                }
                testvalid = true;
            }
        }

        // 这可能会改变组件的首选大小。
        if (testvalid) {
            invalidateIfValid();
        }
    }

    /**
     * 确定该复选框是处于“开”状态还是“关”状态。
     * 布尔值 <code>true</code> 表示“开”状态，<code>false</code> 表示“关”状态。
     *
     * @return 该复选框的状态，作为布尔值
     * @see       #setState
     */
    public boolean getState() {
        return state;
    }

    /**
     * 将该复选框的状态设置为指定状态。
     * 布尔值 <code>true</code> 表示“开”状态，<code>false</code> 表示“关”状态。
     *
     * <p>注意，此方法主要用于初始化复选框的状态。编程设置复选框的状态不会触发
     * <code>ItemEvent</code>。唯一触发 <code>ItemEvent</code> 的方法是用户交互。
     *
     * @param     state   该复选框的布尔状态
     * @see       #getState
     */
    public void setState(boolean state) {
        /* 在调用 group.setSelectedCheckbox 时不能持有复选框锁。 */
        CheckboxGroup group = this.group;
        if (group != null) {
            if (state) {
                group.setSelectedCheckbox(this);
            } else if (group.getSelectedCheckbox() == this) {
                state = true;
            }
        }
        setStateInternal(state);
    }

    /**
     * 返回一个数组（长度为1），包含复选框的标签或如果复选框未被选中则返回 null。
     * @see ItemSelectable
     */
    public Object[] getSelectedObjects() {
        if (state) {
            Object[] items = new Object[1];
            items[0] = label;
            return items;
        }
        return null;
    }

    /**
     * 确定该复选框的组。
     * @return 该复选框的组，或 <code>null</code> 表示该复选框不属于任何复选框组。
     * @see        #setCheckboxGroup(CheckboxGroup)
     */
    public CheckboxGroup getCheckboxGroup() {
        return group;
    }

    /**
     * 将该复选框的组设置为指定的复选框组。
     * 如果该复选框已经属于不同的复选框组，则首先将其从该组中移除。
     * <p>
     * 如果该复选框的状态为 <code>true</code> 且新组已有一个复选框被选中，则该复选框的状态
     * 会变为 <code>false</code>。如果该复选框的状态为 <code>true</code> 且新组没有复选框
     * 被选中，则该复选框成为新组的选中复选框，其状态为 <code>true</code>。
     *
     * @param     g   新的复选框组，或 <code>null</code> 以将该复选框从任何复选框组中移除
     * @see       #getCheckboxGroup
     */
    public void setCheckboxGroup(CheckboxGroup g) {
        CheckboxGroup oldGroup;
        boolean oldState;

        /* 如果该复选框已经属于复选框组 g，则不执行任何操作。 */
        if (this.group == g) {
            return;
        }

        synchronized (this) {
            oldGroup = this.group;
            oldState = getState();

            this.group = g;
            CheckboxPeer peer = (CheckboxPeer)this.peer;
            if (peer != null) {
                peer.setCheckboxGroup(g);
            }
            if (this.group != null && getState()) {
                if (this.group.getSelectedCheckbox() != null) {
                    setState(false);
                } else {
                    this.group.setSelectedCheckbox(this);
                }
            }
        }

        /* 以下锁定复选框可能会导致与
         * CheckboxGroup 的 setSelectedCheckbox 方法死锁。
         *
         * 4726853 修复 by kdm@sparc.spb.su
         * 这里我们应该检查该复选框是否在前一个组中被选中
         * 如果是，则将该组的选中复选框设置为 null。
         */
        if (oldGroup != null && oldState) {
            oldGroup.setSelectedCheckbox(null);
        }
    }

    /**
     * 添加指定的项监听器以接收来自该复选框的项事件。项事件在响应用户输入时发送给监听器，
     * 但不会在响应 setState() 调用时发送。如果 l 为 null，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param         l    项监听器
     * @see           #removeItemListener
     * @see           #getItemListeners
     * @see           #setState
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
     * 移除指定的项监听器，使项监听器不再接收来自该复选框的项事件。
     * 如果 l 为 null，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
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
     * 返回与此复选框注册的所有项目监听器的数组。
     *
     * @return 此复选框的所有 <code>ItemListener</code>，如果没有注册项目监听器，则返回空数组
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
     * 返回当前注册为 <code><em>Foo</em>Listener</code> 的所有对象的数组。
     * <code><em>Foo</em>Listener</code> 是使用 <code>add<em>Foo</em>Listener</code> 方法注册的。
     *
     * <p>
     * 您可以使用类字面量指定 <code>listenerType</code> 参数，例如
     * <code><em>Foo</em>Listener.class</code>。
     * 例如，您可以使用以下代码查询一个 <code>Checkbox</code> <code>c</code>
     * 的项目监听器：
     *
     * <pre>ItemListener[] ils = (ItemListener[])(c.getListeners(ItemListener.class));</pre>
     *
     * 如果没有这样的监听器存在，此方法返回一个空数组。
     *
     * @param listenerType 请求的监听器类型；此参数应指定一个继承自
     *          <code>java.util.EventListener</code> 的接口
     * @return 作为 <code><em>Foo</em>Listener</code> 注册在该复选框上的所有对象的数组，
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
     * 处理此复选框上的事件。
     * 如果事件是 <code>ItemEvent</code> 的实例，
     * 此方法调用 <code>processItemEvent</code> 方法。
     * 否则，它调用其父类的 <code>processEvent</code> 方法。
     * <p>注意，如果事件参数为 <code>null</code>
     * 行为是未指定的，可能会导致异常。
     *
     * @param         e 事件
     * @see           java.awt.event.ItemEvent
     * @see           #processItemEvent
     * @since         JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof ItemEvent) {
            processItemEvent((ItemEvent)e);
            return;
        }
        super.processEvent(e);
    }

    /**
     * 通过将它们分派给任何注册的
     * <code>ItemListener</code> 对象来处理此复选框上的项目事件。
     * <p>
     * 除非为此组件启用了项目事件，否则不会调用此方法。项目事件在以下情况下启用：
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
     * 返回表示此 <code>Checkbox</code> 状态的字符串。
     * 该方法仅用于调试目的，返回字符串的内容和格式可能因实现而异。返回的字符串可能为空，但不能为
     * <code>null</code>。
     *
     * @return    此复选框的参数字符串
     */
    protected String paramString() {
        String str = super.paramString();
        String label = this.label;
        if (label != null) {
            str += ",label=" + label;
        }
        return str + ",state=" + state;
    }


    /* Serialization support.
     */

    /*
     * 序列化数据版本
     * @serial
     */
    private int checkboxSerializedDataVersion = 1;

    /**
     * 将默认的可序列化字段写入流。写入一个可选数据的
     * <code>ItemListeners</code> 列表。非可序列化的
     * <code>ItemListeners</code> 会被检测到，并不会尝试序列化它们。
     *
     * @param s 要写入的 <code>ObjectOutputStream</code>
     * @serialData 以 <code>null</code> 结尾的 0 或多个对的序列；每个对由一个 <code>String</code>
     *   和一个 <code>Object</code> 组成；<code>String</code> 表示对象的类型，可以是以下之一：
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
     * 读取 <code>ObjectInputStream</code>，如果不是 <code>null</code>，则添加一个监听器以接收
     * 由 <code>Checkbox</code> 触发的项目事件。未识别的键或值将被忽略。
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

        else // 跳过未识别键的值
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
     * 获取与此复选框关联的 AccessibleContext。
     * 对于复选框，AccessibleContext 采用 AccessibleAWTCheckbox 的形式。
     * 如果必要，将创建一个新的 AccessibleAWTCheckbox。
     *
     * @return 一个 AccessibleAWTCheckbox，作为此复选框的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTCheckbox();
        }
        return accessibleContext;
    }

    /**
     * 为 <code>Checkbox</code> 类实现辅助功能支持。
     * 它为复选框用户界面元素提供了 Java 辅助功能 API 的实现。
     * @since 1.3
     */
    protected class AccessibleAWTCheckbox extends AccessibleAWTComponent
        implements ItemListener, AccessibleAction, AccessibleValue
    {
        /*
         * JDK 1.3 序列化版本 ID
         */
        private static final long serialVersionUID = 7881579233144754107L;

        public AccessibleAWTCheckbox() {
            super();
            Checkbox.this.addItemListener(this);
        }

        /**
         * 当切换按钮的状态改变时，触发辅助属性更改事件。
         */
        public void itemStateChanged(ItemEvent e) {
            Checkbox cb = (Checkbox) e.getSource();
            if (Checkbox.this.accessibleContext != null) {
                if (cb.getState()) {
                    Checkbox.this.accessibleContext.firePropertyChange(
                            AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                            null, AccessibleState.CHECKED);
                } else {
                    Checkbox.this.accessibleContext.firePropertyChange(
                            AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                            AccessibleState.CHECKED, null);
                }
            }
        }

        /**
         * 获取与此对象关联的 AccessibleAction。在此类的 Java 辅助功能 API 实现中，
         * 返回此对象，它负责代表自身实现 AccessibleAction 接口。
         *
         * @return 此对象
         */
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        /**
         * 获取与此对象关联的 AccessibleValue。在此类的 Java 辅助功能 API 实现中，
         * 返回此对象，它负责代表自身实现 AccessibleValue 接口。
         *
         * @return 此对象
         */
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        /**
         * 返回此对象中可用的 Action 数量。
         * 如果有多个 Action，第一个是“默认”Action。
         *
         * @return 此对象中的 Action 数量
         */
        public int getAccessibleActionCount() {
            return 0;  //  未来版本中将完全实现
        }

        /**
         * 返回对象的指定 Action 的描述。
         *
         * @param i Action 的零基索引
         */
        public String getAccessibleActionDescription(int i) {
            return null;  //  未来版本中将完全实现
        }

        /**
         * 在对象上执行指定的 Action
         *
         * @param i Action 的零基索引
         * @return 如果执行了 Action，则返回 true；否则返回 false。
         */
        public boolean doAccessibleAction(int i) {
            return false;    //  未来版本中将完全实现
        }

        /**
         * 获取此对象的值作为 Number。如果未设置值，返回值将为 null。
         *
         * @return 对象的值
         * @see #setCurrentAccessibleValue
         */
        public Number getCurrentAccessibleValue() {
            return null;  //  未来版本中将完全实现
        }

        /**
         * 设置此对象的值作为 Number。
         *
         * @return 如果设置了值，则返回 True；否则返回 False
         * @see #getCurrentAccessibleValue
         */
        public boolean setCurrentAccessibleValue(Number n) {
            return false;  //  未来版本中将完全实现
        }

        /**
         * 获取此对象的最小值作为 Number。
         *
         * @return 对象的最小值；如果此对象没有最小值，则返回 null
         * @see #getMaximumAccessibleValue
         */
        public Number getMinimumAccessibleValue() {
            return null;  //  未来版本中将完全实现
        }

        /**
         * 获取此对象的最大值作为 Number。
         *
         * @return 对象的最大值；如果此对象没有最大值，则返回 null
         * @see #getMinimumAccessibleValue
         */
        public Number getMaximumAccessibleValue() {
            return null;  //  未来版本中将完全实现
        }

        /**
         * 获取此对象的角色。
         *
         * @return 一个 AccessibleRole 的实例，描述对象的角色
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.CHECK_BOX;
        }

        /**
         * 获取此对象的状态集。
         *
         * @return 一个 AccessibleState 的实例，包含对象的当前状态
         * @see AccessibleState
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (getState()) {
                states.add(AccessibleState.CHECKED);
            }
            return states;
        }


    } // 内部类 AccessibleAWTCheckbox

}
