
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

import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.event.*;
import java.util.EventListener;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import javax.accessibility.*;
import sun.awt.AWTAccessor;


/**
 * 该类表示可以包含在菜单中的复选框。
 * 选择菜单中的复选框会将其状态从“开”变为“关”或从“关”变为“开”。
 * <p>
 * 以下图片显示了一个包含 <code>CheckBoxMenuItem</code> 实例的菜单：
 * <p>
 * <img src="doc-files/MenuBar-1.gif"
 * alt="Menu labeled Examples, containing items Basic, Simple, Check, and More Examples. The Check item is a CheckBoxMenuItem instance, in the off state."
 * style="float:center; margin: 7px 10px;">
 * <p>
 * 标签为 <code>Check</code> 的项显示了一个处于“关”状态的复选框菜单项。
 * <p>
 * 当选择复选框菜单项时，AWT 会向该菜单项发送一个项目事件。由于该事件是一个 <code>ItemEvent</code> 实例，
 * <code>processEvent</code> 方法会检查该事件并将其传递给 <code>processItemEvent</code>。后者方法将事件重定向到任何已注册
 * 对此菜单项生成的项目事件感兴趣的 <code>ItemListener</code> 对象。
 *
 * @author      Sami Shaio
 * @see         java.awt.event.ItemEvent
 * @see         java.awt.event.ItemListener
 * @since       JDK1.0
 */
public class CheckboxMenuItem extends MenuItem implements ItemSelectable, Accessible {

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }

        AWTAccessor.setCheckboxMenuItemAccessor(
            new AWTAccessor.CheckboxMenuItemAccessor() {
                public boolean getState(CheckboxMenuItem cmi) {
                    return cmi.state;
                }
            });
    }

   /**
    * 复选框菜单项的状态
    * @serial
    * @see #getState()
    * @see #setState(boolean)
    */
    boolean state = false;

    transient ItemListener itemListener;

    private static final String base = "chkmenuitem";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 序列化版本号
     */
     private static final long serialVersionUID = 6190621106981774043L;

    /**
     * 创建一个标签为空的复选框菜单项。
     * 项的初始状态设置为“关”。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since   JDK1.1
     */
    public CheckboxMenuItem() throws HeadlessException {
        this("", false);
    }

    /**
     * 创建一个具有指定标签的复选框菜单项。
     * 项的初始状态设置为“关”。

     * @param     label   复选框菜单项的字符串标签，或 <code>null</code> 表示无标签的菜单项。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public CheckboxMenuItem(String label) throws HeadlessException {
        this(label, false);
    }

    /**
     * 创建一个具有指定标签和状态的复选框菜单项。
     * @param      label   复选框菜单项的字符串标签，或 <code>null</code> 表示无标签的菜单项。
     * @param      state   菜单项的初始状态，其中 <code>true</code> 表示“开”，<code>false</code> 表示“关”。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since      JDK1.1
     */
    public CheckboxMenuItem(String label, boolean state)
        throws HeadlessException {
        super(label);
        this.state = state;
    }

    /**
     * 为这个 MenuComponent 构造一个名称。当名称为 null 时由 getName() 调用。
     */
    String constructComponentName() {
        synchronized (CheckboxMenuItem.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 创建复选框项的对等体。此对等体允许我们在不改变复选框项功能的情况下改变其外观。
     * 大多数应用程序不会直接调用此方法。
     * @see     java.awt.Toolkit#createCheckboxMenuItem(java.awt.CheckboxMenuItem)
     * @see     java.awt.Component#getToolkit()
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = Toolkit.getDefaultToolkit().createCheckboxMenuItem(this);
            super.addNotify();
        }
    }

    /**
     * 确定此复选框菜单项的状态是“开”还是“关”。
     *
     * @return      此复选框菜单项的状态，其中 <code>true</code> 表示“开”，<code>false</code> 表示“关”
     * @see        #setState
     */
    public boolean getState() {
        return state;
    }

    /**
     * 将此复选框菜单项设置为指定状态。
     * 布尔值 <code>true</code> 表示“开”，而 <code>false</code> 表示“关”。
     *
     * <p>注意，此方法主要用于初始化复选框菜单项的状态。
     * 程序性地设置复选框菜单项的状态将 <i>不会</i> 触发 <code>ItemEvent</code>。触发 <code>ItemEvent</code> 的唯一方法是用户交互。
     *
     * @param      b   如果复选框菜单项处于“开”状态，则为 <code>true</code>，否则为 <code>false</code>
     * @see        #getState
     */
    public synchronized void setState(boolean b) {
        state = b;
        CheckboxMenuItemPeer peer = (CheckboxMenuItemPeer)this.peer;
        if (peer != null) {
            peer.setState(b);
        }
    }

    /**
     * 返回一个数组（长度为 1），其中包含复选框菜单项的标签，如果复选框未被选中，则返回 null。
     * @see ItemSelectable
     */
    public synchronized Object[] getSelectedObjects() {
        if (state) {
            Object[] items = new Object[1];
            items[0] = label;
            return items;
        }
        return null;
    }

    /**
     * 添加指定的项目监听器以接收来自此复选框菜单项的项目事件。项目事件是在用户操作时发送的，而不是在调用 setState() 时发送。
     * 如果 l 为 null，则不会抛出异常且不执行任何操作。
     * <p>请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a> 以了解 AWT 的线程模型。
     *
     * @param         l 项目监听器
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
     * 移除指定的项目监听器，使其不再接收来自此复选框菜单项的项目事件。
     * 如果 l 为 null，则不会抛出异常且不执行任何操作。
     * <p>请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a> 以了解 AWT 的线程模型。
     *
     * @param         l 项目监听器
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
     * 返回在此复选框菜单项上注册的所有项目监听器的数组。
     *
     * @return 此复选框菜单项的所有 <code>ItemListener</code>，如果没有注册项目监听器，则返回一个空数组
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
     * 返回在此 <code>CheckboxMenuItem</code> 上注册的所有 <code><em>Foo</em>Listener</code> 的数组。
     * <code><em>Foo</em>Listener</code> 是使用 <code>add<em>Foo</em>Listener</code> 方法注册的。
     *
     * <p>
     * 您可以使用类字面量指定 <code>listenerType</code> 参数，例如
     * <code><em>Foo</em>Listener.class</code>。例如，您可以使用以下代码查询
     * <code>CheckboxMenuItem</code> <code>c</code> 的项目监听器：
     *
     * <pre>ItemListener[] ils = (ItemListener[])(c.getListeners(ItemListener.class));</pre>
     *
     * 如果没有注册此类监听器，此方法返回一个空数组。
     *
     * @param listenerType 请求的监听器类型；此参数应指定一个实现
     *          <code>java.util.EventListener</code> 的类或接口
     * @return 在此复选框菜单项上注册的所有
     *          <code><em>Foo</em>Listener</code> 的数组，如果没有注册此类
     *          监听器，则返回一个空数组
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
     * 处理此复选框菜单项上的事件。
     * 如果事件是 <code>ItemEvent</code> 的实例，此方法调用 <code>processItemEvent</code> 方法。
     * 如果事件不是项目事件，则调用超类的 <code>processEvent</code> 方法。
     * <p>
     * 复选框菜单项目前仅支持项目事件。
     * <p>注意，如果事件参数为 <code>null</code>，则行为是未指定的，可能会导致异常。
     *
     * @param        e 事件
     * @see          java.awt.event.ItemEvent
     * @see          #processItemEvent
     * @since        JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof ItemEvent) {
            processItemEvent((ItemEvent)e);
            return;
        }
        super.processEvent(e);
    }

    /**
     * 通过将它们分发给任何注册的 <code>ItemListener</code> 对象来处理此复选框菜单项上的项目事件。
     * <p>
     * 只有在项目事件对此菜单项启用时，此方法才会被调用。项目事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addItemListener</code> 注册了 <code>ItemListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了项目事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>，则行为是未指定的，可能会导致异常。
     *
     * @param       e 项目事件
     * @see         java.awt.event.ItemEvent
     * @see         java.awt.event.ItemListener
     * @see         #addItemListener
     * @see         java.awt.MenuItem#enableEvents
     * @since       JDK1.1
     */
    protected void processItemEvent(ItemEvent e) {
        ItemListener listener = itemListener;
        if (listener != null) {
            listener.itemStateChanged(e);
        }
    }

    /*
     * 发布 ItemEvent 并切换状态。
     */
    void doMenuEvent(long when, int modifiers) {
        setState(!state);
        Toolkit.getEventQueue().postEvent(
            new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
                          getLabel(),
                          state ? ItemEvent.SELECTED :
                                  ItemEvent.DESELECTED));
    }

    /**
     * 返回一个表示此 <code>CheckBoxMenuItem</code> 状态的字符串。此
     * 方法仅用于调试目的，返回字符串的内容和格式可能在实现之间有所不同。返回的字符串可以为空，但不能为
     * <code>null</code>。
     *
     * @return     此复选框菜单项的参数字符串
     */
    public String paramString() {
        return super.paramString() + ",state=" + state;
    }

    /* 序列化支持。
     */

    /*
     * 序列化数据版本
     * @serial
     */
    private int checkboxMenuItemSerializedDataVersion = 1;


                /**
     * 将默认的可序列化字段写入流中。写入
     * 一个可序列化的 <code>ItemListeners</code> 列表
     * 作为可选数据。非可序列化的
     * <code>ItemListeners</code> 会被检测到，
     * 并不会尝试序列化它们。
     *
     * @param s 要写入的 <code>ObjectOutputStream</code>
     * @serialData 以 <code>null</code> 结尾的
     *  0 个或多个对的序列；每个对由一个 <code>String</code>
     *  和一个 <code>Object</code> 组成；<code>String</code> 表示
     *  对象的类型，可以是以下之一：
     *  <code>itemListenerK</code> 表示一个
     *    <code>ItemListener</code> 对象
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

    /*
     * 读取 <code>ObjectInputStream</code>，如果它
     * 不是 <code>null</code>，则添加一个监听器以接收
     * 由 <code>Checkbox</code> 菜单项触发的项目事件。
     * 未识别的键或值将被忽略。
     *
     * @param s 要读取的 <code>ObjectInputStream</code>
     * @serial
     * @see removeActionListener()
     * @see addActionListener()
     * @see #writeObject
     */
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException
    {
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
     * 获取与此 CheckboxMenuItem 关联的 AccessibleContext。
     * 对于复选菜单项，AccessibleContext 的形式为 AccessibleAWTCheckboxMenuItem。
     * 如果需要，将创建一个新的 AccessibleAWTCheckboxMenuItem。
     *
     * @return 一个 AccessibleAWTCheckboxMenuItem，作为此 CheckboxMenuItem 的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTCheckboxMenuItem();
        }
        return accessibleContext;
    }

    /**
     * CheckboxMenuItem 的内部类，用于提供对可访问性的默认支持。
     * 该类不建议由应用程序开发人员直接使用，而是仅用于
     * 菜单组件开发人员的子类。
     * <p>
     * 该类实现了适用于复选菜单项用户界面元素的
     * Java 可访问性 API。
     * @since 1.3
     */
    protected class AccessibleAWTCheckboxMenuItem extends AccessibleAWTMenuItem
        implements AccessibleAction, AccessibleValue
    {
        /*
         * JDK 1.3 serialVersionUID
         */
        private static final long serialVersionUID = -1122642964303476L;

        /**
         * 获取与此对象关联的 AccessibleAction。在
         * 本类的 Java 可访问性 API 实现中，返回此对象，
         * 该对象负责代表自身实现 AccessibleAction 接口。
         *
         * @return 此对象
         */
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        /**
         * 获取与此对象关联的 AccessibleValue。在
         * 本类的 Java 可访问性 API 实现中，返回此对象，
         * 该对象负责代表自身实现 AccessibleValue 接口。
         *
         * @return 此对象
         */
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        /**
         * 返回此对象中可用的 Actions 数量。
         * 如果有多个，第一个是“默认”操作。
         *
         * @return 此对象中的 Actions 数量
         */
        public int getAccessibleActionCount() {
            return 0;  //  将在未来的版本中完全实现
        }

        /**
         * 返回对象指定操作的描述。
         *
         * @param i 操作的零基索引
         */
        public String getAccessibleActionDescription(int i) {
            return null;  //  将在未来的版本中完全实现
        }

        /**
         * 在对象上执行指定的操作
         *
         * @param i 操作的零基索引
         * @return 如果操作执行成功，则返回 true；否则返回 false。
         */
        public boolean doAccessibleAction(int i) {
            return false;    //  将在未来的版本中完全实现
        }

        /**
         * 获取此对象的值作为 Number。如果未设置值，
         * 返回值将为 null。
         *
         * @return 对象的值
         * @see #setCurrentAccessibleValue
         */
        public Number getCurrentAccessibleValue() {
            return null;  //  将在未来的版本中完全实现
        }

        /**
         * 将此对象的值设置为 Number。
         *
         * @return 如果值设置成功，则返回 true；否则返回 false
         * @see #getCurrentAccessibleValue
         */
        public boolean setCurrentAccessibleValue(Number n) {
            return false;  //  将在未来的版本中完全实现
        }

        /**
         * 获取此对象的最小值作为 Number。
         *
         * @return 对象的最小值；如果此对象没有最小值，则返回 null
         * @see #getMaximumAccessibleValue
         */
        public Number getMinimumAccessibleValue() {
            return null;  //  将在未来的版本中完全实现
        }

        /**
         * 获取此对象的最大值作为 Number。
         *
         * @return 对象的最大值；如果此对象没有最大值，则返回 null
         * @see #getMinimumAccessibleValue
         */
        public Number getMaximumAccessibleValue() {
            return null;  //  将在未来的版本中完全实现
        }

        /**
         * 获取此对象的角色。
         *
         * @return 描述对象角色的 AccessibleRole 实例
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.CHECK_BOX;
        }

    } // class AccessibleAWTMenuItem

}
