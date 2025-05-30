
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

import java.awt.peer.MenuItemPeer;
import java.awt.event.*;
import java.util.EventListener;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import javax.accessibility.*;
import sun.awt.AWTAccessor;

/**
 * 菜单中的所有项目必须属于 <code>MenuItem</code> 类或其子类。
 * <p>
 * 默认的 <code>MenuItem</code> 对象体现了一个简单的带标签的菜单项。
 * <p>
 * 以下菜单栏的图片显示了五个菜单项：
 * <IMG SRC="doc-files/MenuBar-1.gif" alt="The following text describes this graphic."
 * style="float:center; margin: 7px 10px;">
 * <br style="clear:left;">
 * 前两个项目是简单的菜单项，标签为 <code>"Basic"</code> 和 <code>"Simple"</code>。
 * 在这两个项目之后是一个分隔符，它本身是一个菜单项，使用标签 <code>"-"</code> 创建。
 * 接下来是一个 <code>CheckboxMenuItem</code> 的实例，标签为 <code>"Check"</code>。
 * 最后一个菜单项是一个子菜单，标签为 <code>"More&nbsp;Examples"</code>，
 * 这个子菜单是一个 <code>Menu</code> 的实例。
 * <p>
 * 当选择一个菜单项时，AWT 会向该菜单项发送一个动作事件。由于该事件是一个
 * <code>ActionEvent</code> 的实例，<code>processEvent</code>
 * 方法会检查该事件并将其传递给 <code>processActionEvent</code>。
 * 后者方法会将事件重定向到任何已注册对由该菜单项生成的动作事件感兴趣的
 * <code>ActionListener</code> 对象。
 * <P>
 * 注意，子类 <code>Menu</code> 覆盖了这种行为，并且不会在任何子项被选择之前
 * 向框架发送任何事件。
 *
 * @author Sami Shaio
 */
public class MenuItem extends MenuComponent implements Accessible {

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }

        AWTAccessor.setMenuItemAccessor(
            new AWTAccessor.MenuItemAccessor() {
                public boolean isEnabled(MenuItem item) {
                    return item.enabled;
                }

                public String getLabel(MenuItem item) {
                    return item.label;
                }

                public MenuShortcut getShortcut(MenuItem item) {
                    return item.shortcut;
                }

                public String getActionCommandImpl(MenuItem item) {
                    return item.getActionCommandImpl();
                }

                public boolean isItemEnabled(MenuItem item) {
                    return item.isItemEnabled();
                }
            });
    }

    /**
     * 一个值，用于指示菜单项是否启用。
     * 如果启用，<code>enabled</code> 将被设置为 true。
     * 否则，<code>enabled</code> 将被设置为 false。
     *
     * @serial
     * @see #isEnabled()
     * @see #setEnabled(boolean)
     */
    boolean enabled = true;

    /**
     * <code>label</code> 是菜单项的标签。
     * 它可以是任何字符串。
     *
     * @serial
     * @see #getLabel()
     * @see #setLabel(String)
     */
    String label;

    /**
     * 此字段指示由特定菜单项发出的命令。
     * 默认情况下，<code>actionCommand</code>
     * 是菜单项的标签，除非使用 setActionCommand 设置。
     *
     * @serial
     * @see #setActionCommand(String)
     * @see #getActionCommand()
     */
    String actionCommand;

    /**
     * 事件掩码仅由子类通过 enableEvents 设置。
     * 注册监听器时不应设置掩码，
     * 以便我们可以区分监听器请求事件和子类请求事件的情况。
     *
     * @serial
     */
    long eventMask;

    transient ActionListener actionListener;

    /**
     * 与菜单项关联的一系列按键。
     * 注意：在 1.1.2 版本中，必须使用 setActionCommand()
     * 为菜单项设置快捷键，以便其快捷键生效。
     *
     * @serial
     * @see #getShortcut()
     * @see #setShortcut(MenuShortcut)
     * @see #deleteShortcut()
     */
    private MenuShortcut shortcut = null;

    private static final String base = "menuitem";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 序列化版本 ID
     */
    private static final long serialVersionUID = -21757335363267194L;

    /**
     * 构造一个带有空标签和无键盘快捷键的新菜单项。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since    JDK1.1
     */
    public MenuItem() throws HeadlessException {
        this("", null);
    }

    /**
     * 构造一个带有指定标签和无键盘快捷键的新菜单项。
     * 注意，标签中使用 "-" 保留用于表示菜单项之间的分隔符。
     * 默认情况下，所有菜单项（分隔符除外）都是启用的。
     * @param       label 该菜单项的标签。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since       JDK1.0
     */
    public MenuItem(String label) throws HeadlessException {
        this(label, null);
    }

    /**
     * 创建一个带有关联键盘快捷键的菜单项。
     * 注意，标签中使用 "-" 保留用于表示菜单项之间的分隔符。
     * 默认情况下，所有菜单项（分隔符除外）都是启用的。
     * @param       label 该菜单项的标签。
     * @param       s 与该菜单项关联的 <code>MenuShortcut</code> 实例。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since       JDK1.1
     */
    public MenuItem(String label, MenuShortcut s) throws HeadlessException {
        this.label = label;
        this.shortcut = s;
    }

    /**
     * 为该 MenuComponent 构造一个名称。当名称为 null 时，由 getName() 调用。
     */
    String constructComponentName() {
        synchronized (MenuItem.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 创建菜单项的对等体。对等体允许我们在不改变其功能的情况下修改菜单项的外观。
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = Toolkit.getDefaultToolkit().createMenuItem(this);
        }
    }

    /**
     * 获取该菜单项的标签。
     * @return 该菜单项的标签，或 <code>null</code>
     * 如果该菜单项没有标签。
     * @see     java.awt.MenuItem#setLabel
     * @since   JDK1.0
     */
    public String getLabel() {
        return label;
    }

    /**
     * 将该菜单项的标签设置为指定的标签。
     * @param     label   新标签，或 <code>null</code> 表示无标签。
     * @see       java.awt.MenuItem#getLabel
     * @since     JDK1.0
     */
    public synchronized void setLabel(String label) {
        this.label = label;
        MenuItemPeer peer = (MenuItemPeer)this.peer;
        if (peer != null) {
            peer.setLabel(label);
        }
    }

    /**
     * 检查该菜单项是否启用。
     * @see        java.awt.MenuItem#setEnabled
     * @since      JDK1.0
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置该菜单项是否可以选择。
     * @param      b  如果为 <code>true</code>，启用该菜单项；
     *                       如果为 <code>false</code>，禁用该菜单项。
     * @see        java.awt.MenuItem#isEnabled
     * @since      JDK1.1
     */
    public synchronized void setEnabled(boolean b) {
        enable(b);
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>setEnabled(boolean)</code>。
     */
    @Deprecated
    public synchronized void enable() {
        enabled = true;
        MenuItemPeer peer = (MenuItemPeer)this.peer;
        if (peer != null) {
            peer.setEnabled(true);
        }
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>setEnabled(boolean)</code>。
     */
    @Deprecated
    public void enable(boolean b) {
        if (b) {
            enable();
        } else {
            disable();
        }
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>setEnabled(boolean)</code>。
     */
    @Deprecated
    public synchronized void disable() {
        enabled = false;
        MenuItemPeer peer = (MenuItemPeer)this.peer;
        if (peer != null) {
            peer.setEnabled(false);
        }
    }

    /**
     * 获取与此菜单项关联的 <code>MenuShortcut</code> 对象，
     * @return 与此菜单项关联的菜单快捷键，
     *                   或 <code>null</code> 表示未指定。
     * @see         java.awt.MenuItem#setShortcut
     * @since       JDK1.1
     */
    public MenuShortcut getShortcut() {
        return shortcut;
    }

    /**
     * 设置与此菜单项关联的 <code>MenuShortcut</code> 对象。
     * 如果已有一个菜单快捷键与此菜单项关联，则替换它。
     * @param       s 与此菜单项关联的菜单快捷键。
     * @see         java.awt.MenuItem#getShortcut
     * @since       JDK1.1
     */
    public void setShortcut(MenuShortcut s) {
        shortcut = s;
        MenuItemPeer peer = (MenuItemPeer)this.peer;
        if (peer != null) {
            peer.setLabel(label);
        }
    }

    /**
     * 删除与此菜单项关联的任何 <code>MenuShortcut</code> 对象。
     * @since      JDK1.1
     */
    public void deleteShortcut() {
        shortcut = null;
        MenuItemPeer peer = (MenuItemPeer)this.peer;
        if (peer != null) {
            peer.setLabel(label);
        }
    }

    /*
     * 删除与此 MenuItem 关联的匹配 MenuShortcut。
     * 用于遍历菜单时。
     */
    void deleteShortcut(MenuShortcut s) {
        if (s.equals(shortcut)) {
            shortcut = null;
            MenuItemPeer peer = (MenuItemPeer)this.peer;
            if (peer != null) {
                peer.setLabel(label);
            }
        }
    }

    /*
     * 该方法的主要目标是在按下菜单快捷键时向事件队列发布适当的事件。
     * 然而，在子类中，该方法可能不仅仅发布事件。
     */
    void doMenuEvent(long when, int modifiers) {
        Toolkit.getEventQueue().postEvent(
            new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                            getActionCommand(), when, modifiers));
    }

    /*
     * 如果项目及其所有祖先都启用，则返回 true，否则返回 false
     */
    private final boolean isItemEnabled() {
        // 修复 6185151：当菜单本身禁用时，菜单内的所有菜单项的快捷键也应禁用
        if (!isEnabled()) {
            return false;
        }
        MenuContainer container = getParent_NoClientCode();
        do {
            if (!(container instanceof Menu)) {
                return true;
            }
            Menu menu = (Menu)container;
            if (!menu.isEnabled()) {
                return false;
            }
            container = menu.getParent_NoClientCode();
        } while (container != null);
        return true;
    }

    /*
     * 在按下键时向目标发布 ActionEvent，并且项目已启用。
     * 如果有相关快捷键，则返回 true。
     */
    boolean handleShortcut(KeyEvent e) {
        MenuShortcut s = new MenuShortcut(e.getKeyCode(),
                             (e.getModifiers() & InputEvent.SHIFT_MASK) > 0);
        MenuShortcut sE = new MenuShortcut(e.getExtendedKeyCode(),
                             (e.getModifiers() & InputEvent.SHIFT_MASK) > 0);
        // 修复 6185151：当菜单本身禁用时，菜单内的所有菜单项的快捷键也应禁用
        if ((s.equals(shortcut) || sE.equals(shortcut)) && isItemEnabled()) {
            // MenuShortcut 匹配 -- 在按下键时发出事件。
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                doMenuEvent(e.getWhen(), e.getModifiers());
            } else {
                // 静默处理键释放。
            }
            return true;
        }
        return false;
    }

    MenuItem getShortcutMenuItem(MenuShortcut s) {
        return (s.equals(shortcut)) ? this : null;
    }

    /**
     * 启用事件传递到该菜单项，事件类型由指定的事件掩码参数定义
     * <p>
     * 由于在菜单项上添加了特定类型的监听器时，事件类型会自动启用，
     * 因此只有 <code>MenuItem</code> 的子类需要调用此方法，以便在没有注册监听器的情况下，
     * 将指定的事件类型传递到 <code>processEvent</code>。
     *
     * @param       eventsToEnable 定义事件类型的事件掩码
     * @see         java.awt.MenuItem#processEvent
     * @see         java.awt.MenuItem#disableEvents
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected final void enableEvents(long eventsToEnable) {
        eventMask |= eventsToEnable;
        newEventsOnly = true;
    }

    /**
     * 禁用事件传递到该菜单项，事件类型由指定的事件掩码参数定义。
     *
     * @param       eventsToDisable 定义事件类型的事件掩码
     * @see         java.awt.MenuItem#processEvent
     * @see         java.awt.MenuItem#enableEvents
     * @see         java.awt.Component#disableEvents
     * @since       JDK1.1
     */
    protected final void disableEvents(long eventsToDisable) {
        eventMask &= ~eventsToDisable;
    }


                /**
     * 设置此菜单项触发的动作事件的命令名称。
     * <p>
     * 默认情况下，动作命令设置为菜单项的标签。
     * @param       command   要为此菜单项设置的动作命令。
     * @see         java.awt.MenuItem#getActionCommand
     * @since       JDK1.1
     */
    public void setActionCommand(String command) {
        actionCommand = command;
    }

    /**
     * 获取此菜单项触发的动作事件的命令名称。
     * @see         java.awt.MenuItem#setActionCommand
     * @since       JDK1.1
     */
    public String getActionCommand() {
        return getActionCommandImpl();
    }

    // 这是最终的，因此可以在 Toolkit 线程上调用。
    final String getActionCommandImpl() {
        return (actionCommand == null? label : actionCommand);
    }

    /**
     * 添加指定的动作监听器以接收此菜单项的动作事件。
     * 如果 l 为 null，则不抛出异常且不执行任何操作。
     * <p>请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a> 以了解 AWT 的线程模型。
     *
     * @param      l 动作监听器。
     * @see        #removeActionListener
     * @see        #getActionListeners
     * @see        java.awt.event.ActionEvent
     * @see        java.awt.event.ActionListener
     * @since      JDK1.1
     */
    public synchronized void addActionListener(ActionListener l) {
        if (l == null) {
            return;
        }
        actionListener = AWTEventMulticaster.add(actionListener, l);
        newEventsOnly = true;
    }

    /**
     * 移除指定的动作监听器，使其不再接收此菜单项的动作事件。
     * 如果 l 为 null，则不抛出异常且不执行任何操作。
     * <p>请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a> 以了解 AWT 的线程模型。
     *
     * @param      l 动作监听器。
     * @see        #addActionListener
     * @see        #getActionListeners
     * @see        java.awt.event.ActionEvent
     * @see        java.awt.event.ActionListener
     * @since      JDK1.1
     */
    public synchronized void removeActionListener(ActionListener l) {
        if (l == null) {
            return;
        }
        actionListener = AWTEventMulticaster.remove(actionListener, l);
    }

    /**
     * 返回注册在此菜单项上的所有动作监听器的数组。
     *
     * @return 此菜单项的所有 <code>ActionListener</code>，如果未注册任何动作监听器，则返回空数组。
     *
     * @see        #addActionListener
     * @see        #removeActionListener
     * @see        java.awt.event.ActionEvent
     * @see        java.awt.event.ActionListener
     * @since 1.4
     */
    public synchronized ActionListener[] getActionListeners() {
        return getListeners(ActionListener.class);
    }

    /**
     * 返回注册在此 <code>MenuItem</code> 上的所有 <code><em>Foo</em>Listener</code> 的数组。
     * <code><em>Foo</em>Listener</code> 通过 <code>add<em>Foo</em>Listener</code> 方法注册。
     *
     * <p>
     * 您可以使用类字面量指定 <code>listenerType</code> 参数，例如
     * <code><em>Foo</em>Listener.class</code>。例如，您可以查询一个
     * <code>MenuItem</code> <code>m</code>
     * 的动作监听器，使用以下代码：
     *
     * <pre>ActionListener[] als = (ActionListener[])(m.getListeners(ActionListener.class));</pre>
     *
     * 如果没有注册此类监听器，此方法返回空数组。
     *
     * @param listenerType 请求的监听器类型；此参数应指定一个继承自
     *          <code>java.util.EventListener</code> 的类或接口。
     * @return 注册在此菜单项上的所有 <code><em>Foo</em>Listener</code> 的数组，
     *         如果未添加此类监听器，则返回空数组。
     * @exception ClassCastException 如果 <code>listenerType</code>
     *          不指定一个继承自 <code>java.util.EventListener</code> 的类或接口。
     *
     * @see #getActionListeners
     * @since 1.3
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if  (listenerType == ActionListener.class) {
            l = actionListener;
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }

    /**
     * 处理此菜单项上的事件。如果事件是 <code>ActionEvent</code> 的实例，
     * 则调用 <code>processActionEvent</code>，这是 <code>MenuItem</code> 定义的另一个方法。
     * <p>
     * 当前，菜单项仅支持动作事件。
     * <p>注意，如果事件参数为 <code>null</code>，则行为未指定，可能会导致异常。
     *
     * @param       e 事件
     * @see         java.awt.MenuItem#processActionEvent
     * @since       JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof ActionEvent) {
            processActionEvent((ActionEvent)e);
        }
    }

    // 提醒：在较低级别完成过滤时删除
    boolean eventEnabled(AWTEvent e) {
        if (e.id == ActionEvent.ACTION_PERFORMED) {
            if ((eventMask & AWTEvent.ACTION_EVENT_MASK) != 0 ||
                actionListener != null) {
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }

    /**
     * 处理此菜单项上的动作事件，通过将它们分派给任何注册的
     * <code>ActionListener</code> 对象。
     * 仅当为此组件启用了动作事件时，才会调用此方法。动作事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addActionListener</code> 注册了 <code>ActionListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了动作事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>，则行为未指定，可能会导致异常。
     *
     * @param       e 动作事件
     * @see         java.awt.event.ActionEvent
     * @see         java.awt.event.ActionListener
     * @see         java.awt.MenuItem#enableEvents
     * @since       JDK1.1
     */
    protected void processActionEvent(ActionEvent e) {
        ActionListener listener = actionListener;
        if (listener != null) {
            listener.actionPerformed(e);
        }
    }

    /**
     * 返回表示此 <code>MenuItem</code> 状态的字符串。
     * 此方法仅用于调试目的，返回字符串的内容和格式可能因实现而异。返回的字符串可以为空，但不能为
     * <code>null</code>。
     *
     * @return 此菜单项的参数字符串
     */
    public String paramString() {
        String str = ",label=" + label;
        if (shortcut != null) {
            str += ",shortcut=" + shortcut;
        }
        return super.paramString() + str;
    }


    /* 序列化支持。
     */

    /**
     * 菜单项序列化数据版本。
     *
     * @serial
     */
    private int menuItemSerializedDataVersion = 1;

    /**
     * 将默认的可序列化字段写入流中。写入一个可序列化的 <code>ActionListeners</code> 列表作为可选数据。
     * 非可序列化的监听器将被检测到，不会尝试序列化它们。
     *
     * @param s 要写入的 <code>ObjectOutputStream</code>
     * @serialData 以 <code>null</code> 结尾的 0 个或多个对序列；每个对由一个 <code>String</code>
     *   和一个 <code>Object</code> 组成；<code>String</code> 表示对象的类型，可以是以下之一：
     *   <code>actionListenerK</code> 表示一个 <code>ActionListener</code> 对象
     *
     * @see AWTEventMulticaster#save(ObjectOutputStream, String, EventListener)
     * @see #readObject(ObjectInputStream)
     */
    private void writeObject(ObjectOutputStream s)
      throws IOException
    {
      s.defaultWriteObject();

      AWTEventMulticaster.save(s, actionListenerK, actionListener);
      s.writeObject(null);
    }

    /**
     * 从 <code>ObjectInputStream</code> 读取，并在不为 <code>null</code> 时添加一个监听器以接收
     * <code>Menu</code> 项触发的动作事件。未识别的键或值将被忽略。
     *
     * @param s 要读取的 <code>ObjectInputStream</code>
     * @exception HeadlessException 如果
     *   <code>GraphicsEnvironment.isHeadless</code> 返回
     *   <code>true</code>
     * @see #removeActionListener(ActionListener)
     * @see #addActionListener(ActionListener)
     * @see #writeObject(ObjectOutputStream)
     */
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException
    {
      // HeadlessException 将从 MenuComponent 的 readObject 抛出
      s.defaultReadObject();

      Object keyOrNull;
      while(null != (keyOrNull = s.readObject())) {
        String key = ((String)keyOrNull).intern();

        if (actionListenerK == key)
          addActionListener((ActionListener)(s.readObject()));

        else // 跳过未识别键的值
          s.readObject();
      }
    }

    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();


/////////////////
// 可访问性支持
////////////////

    /**
     * 获取与此 MenuItem 关联的 AccessibleContext。
     * 对于菜单项，AccessibleContext 采用 AccessibleAWTMenuItem 的形式。
     * 如果必要，将创建一个新的 AccessibleAWTMenuItem 实例。
     *
     * @return 一个 AccessibleAWTMenuItem，作为此 MenuItem 的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTMenuItem();
        }
        return accessibleContext;
    }

    /**
     * MenuItem 的内部类，用于提供可访问性的默认支持。
     * 此类不建议由应用程序开发人员直接使用，而是仅供菜单组件开发人员子类化。
     * <p>
     * 此类为 <code>MenuItem</code> 类实现了可访问性支持。它为菜单项用户界面元素提供了
     * Java 可访问性 API 的实现。
     * @since 1.3
     */
    protected class AccessibleAWTMenuItem extends AccessibleAWTMenuComponent
        implements AccessibleAction, AccessibleValue
    {
        /*
         * JDK 1.3 序列化版本 ID
         */
        private static final long serialVersionUID = -217847831945965825L;

        /**
         * 获取此对象的可访问名称。
         *
         * @return 对象的本地化名称——如果此对象没有名称，则可以为 null
         */
        public String getAccessibleName() {
            if (accessibleName != null) {
                return accessibleName;
            } else {
                if (getLabel() == null) {
                    return super.getAccessibleName();
                } else {
                    return getLabel();
                }
            }
        }

        /**
         * 获取此对象的角色。
         *
         * @return 一个 AccessibleRole 实例，描述对象的角色
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.MENU_ITEM;
        }

        /**
         * 获取与此对象关联的 AccessibleAction。在此类的 Java 可访问性 API 实现中，
         * 返回此对象，该对象负责代表自身实现 AccessibleAction 接口。
         *
         * @return 此对象
         */
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        /**
         * 获取与此对象关联的 AccessibleValue。在此类的 Java 可访问性 API 实现中，
         * 返回此对象，该对象负责代表自身实现 AccessibleValue 接口。
         *
         * @return 此对象
         */
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        /**
         * 返回此对象中可用的 Actions 数量。菜单项的默认行为是有一个动作。
         *
         * @return 1，此对象中的 Actions 数量
         */
        public int getAccessibleActionCount() {
            return 1;
        }

        /**
         * 返回对象的指定动作的描述。
         *
         * @param i 动作的零基索引
         */
        public String getAccessibleActionDescription(int i) {
            if (i == 0) {
                // [[[待定：WDW -- 需要提供一个本地化字符串]]]
                return "click";
            } else {
                return null;
            }
        }

        /**
         * 在对象上执行指定的动作
         *
         * @param i 动作的零基索引
         * @return 如果动作执行成功，则返回 true；否则返回 false。
         */
        public boolean doAccessibleAction(int i) {
            if (i == 0) {
                // 模拟按钮点击
                Toolkit.getEventQueue().postEvent(
                        new ActionEvent(MenuItem.this,
                                        ActionEvent.ACTION_PERFORMED,
                                        MenuItem.this.getActionCommand(),
                                        EventQueue.getMostRecentEventTime(),
                                        0));
                return true;
            } else {
                return false;
            }
        }

        /**
         * 获取此对象的值作为 Number。
         *
         * @return 如果未选中则返回 0 的 Integer，如果选中则返回 1 的 Integer。
         * @see javax.swing.AbstractButton#isSelected()
         */
        public Number getCurrentAccessibleValue() {
            return Integer.valueOf(0);
        }

        /**
         * 设置此对象的值作为 Number。
         *
         * @return 如果值被设置，则返回 true。
         */
        public boolean setCurrentAccessibleValue(Number n) {
            return false;
        }

        /**
         * 获取此对象的最小值作为 Number。
         *
         * @return 0 的 Integer。
         */
        public Number getMinimumAccessibleValue() {
            return Integer.valueOf(0);
        }


                    /**
         * 获取此对象的最大值作为 Number。
         *
         * @return 0 的 Integer。
         */
        public Number getMaximumAccessibleValue() {
            return Integer.valueOf(0);
        }

    } // class AccessibleAWTMenuItem

}
