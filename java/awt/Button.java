
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

import java.awt.peer.ButtonPeer;
import java.util.EventListener;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import javax.accessibility.*;

/**
 * 该类创建一个带有标签的按钮。应用程序可以触发一些动作，当按钮被按下时。此图像描绘了三个视图，显示了在 Solaris 操作系统下 "<code>Quit</code>" 按钮的外观：
 * <p>
 * <img src="doc-files/Button-1.gif" alt="以下上下文描述了图形"
 * style="float:center; margin: 7px 10px;">
 * <p>
 * 第一个视图显示按钮正常状态下的外观。
 * 第二个视图显示按钮获得输入焦点时的状态。其轮廓变暗，以告知用户这是一个活动对象。第三个视图显示用户点击按钮时的状态，从而请求执行某个动作。
 * <p>
 * 用鼠标点击按钮的动作与一个 <code>ActionEvent</code> 实例相关联，该实例在鼠标在按钮上按下并释放时发送。如果应用程序对按钮被按下但未释放的情况感兴趣，可以专门化 <code>processMouseEvent</code>，
 * 或者可以通过调用 <code>addMouseListener</code> 注册自己为鼠标事件的监听器。这两种方法都是由 <code>Component</code> 定义的，它是所有组件的抽象基类。
 * <p>
 * 当按钮被按下并释放时，AWT 会通过调用按钮上的 <code>processEvent</code> 方法向按钮发送一个 <code>ActionEvent</code> 实例。按钮的 <code>processEvent</code> 方法接收所有按钮事件；
 * 它通过调用其自身的 <code>processActionEvent</code> 方法传递动作事件。后者方法将动作事件传递给对由该按钮生成的动作事件感兴趣的任何动作监听器。
 * <p>
 * 如果应用程序希望基于按钮被按下并释放来执行某些操作，它应该实现 <code>ActionListener</code> 并通过调用按钮的 <code>addActionListener</code> 方法注册新的监听器以接收此按钮的事件。
 * 应用程序可以利用按钮的动作命令作为消息传递协议。
 *
 * @author      Sami Shaio
 * @see         java.awt.event.ActionEvent
 * @see         java.awt.event.ActionListener
 * @see         java.awt.Component#processMouseEvent
 * @see         java.awt.Component#addMouseListener
 * @since       JDK1.0
 */
public class Button extends Component implements Accessible {

    /**
     * 按钮的标签。此值可以为 null。
     * @serial
     * @see #getLabel()
     * @see #setLabel(String)
     */
    String label;

    /**
     * 按钮被按下后要执行的动作。此值可以为 null。
     * @serial
     * @see #getActionCommand()
     * @see #setActionCommand(String)
     */
    String actionCommand;

    transient ActionListener actionListener;

    private static final String base = "button";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -8774683716313001058L;


    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * 初始化可以从 C 访问的字段的 JNI 字段和方法 ID。
     */
    private static native void initIDs();

    /**
     * 构造一个带有空字符串标签的按钮。
     *
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public Button() throws HeadlessException {
        this("");
    }

    /**
     * 构造一个带有指定标签的按钮。
     *
     * @param label  按钮的标签，或 <code>null</code> 表示无标签
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public Button(String label) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.label = label;
    }

    /**
     * 为该组件构造一个名称。当名称为 null 时，由 getName() 调用。
     */
    String constructComponentName() {
        synchronized (Button.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 创建按钮的对等体。按钮的对等体允许应用程序在不改变按钮功能的情况下改变其外观。
     *
     * @see     java.awt.Toolkit#createButton(java.awt.Button)
     * @see     java.awt.Component#getToolkit()
     */
    public void addNotify() {
        synchronized(getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createButton(this);
            super.addNotify();
        }
    }

    /**
     * 获取此按钮的标签。
     *
     * @return    按钮的标签，或 <code>null</code> 表示按钮无标签。
     * @see       java.awt.Button#setLabel
     */
    public String getLabel() {
        return label;
    }

    /**
     * 将按钮的标签设置为指定的字符串。
     *
     * @param     label   新的标签，或 <code>null</code> 表示按钮无标签。
     * @see       java.awt.Button#getLabel
     */
    public void setLabel(String label) {
        boolean testvalid = false;

        synchronized (this) {
            if (label != this.label && (this.label == null ||
                                        !this.label.equals(label))) {
                this.label = label;
                ButtonPeer peer = (ButtonPeer)this.peer;
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
     * 设置此按钮触发的动作事件的命令名称。默认情况下，此动作命令设置为与按钮的标签匹配。
     *
     * @param     command  用于设置按钮的动作命令的字符串。
     *            如果字符串为 <code>null</code>，则动作命令设置为与按钮的标签匹配。
     * @see       java.awt.event.ActionEvent
     * @since     JDK1.1
     */
    public void setActionCommand(String command) {
        actionCommand = command;
    }

    /**
     * 返回此按钮触发的动作事件的命令名称。如果命令名称为 <code>null</code>（默认值），则此方法返回按钮的标签。
     */
    public String getActionCommand() {
        return (actionCommand == null? label : actionCommand);
    }

    /**
     * 添加指定的动作监听器以接收此按钮的动作事件。动作事件在用户按下或释放鼠标时发生。
     * 如果 l 为 null，不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param         l 动作监听器
     * @see           #removeActionListener
     * @see           #getActionListeners
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
     * 移除指定的动作监听器，使其不再接收此按钮的动作事件。动作事件在用户按下或释放鼠标时发生。
     * 如果 l 为 null，不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param           l     动作监听器
     * @see             #addActionListener
     * @see             #getActionListeners
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
     * 返回注册在此按钮上的所有动作监听器的数组。
     *
     * @return 此按钮的所有 <code>ActionListener</code>，如果没有注册动作监听器，则返回空数组
     *
     * @see             #addActionListener
     * @see             #removeActionListener
     * @see             java.awt.event.ActionListener
     * @since 1.4
     */
    public synchronized ActionListener[] getActionListeners() {
        return getListeners(ActionListener.class);
    }

    /**
     * 返回注册在此 <code>Button</code> 上的所有 <code><em>Foo</em>Listener</code> 的数组。
     * <code><em>Foo</em>Listener</code> 是通过 <code>add<em>Foo</em>Listener</code> 方法注册的。
     *
     * <p>
     * 可以使用类字面量指定 <code>listenerType</code> 参数，例如
     * <code><em>Foo</em>Listener.class</code>。例如，可以使用以下代码查询
     * <code>Button</code> <code>b</code> 的动作监听器：
     *
     * <pre>ActionListener[] als = (ActionListener[])(b.getListeners(ActionListener.class));</pre>
     *
     * 如果没有注册此类监听器，此方法返回空数组。
     *
     * @param listenerType 请求的监听器类型；此参数应指定一个实现
     *          <code>java.util.EventListener</code> 的类或接口
     * @return 注册在此按钮上的所有 <code><em>Foo</em>Listener</code> 的数组，
     *          如果没有注册此类监听器，则返回空数组
     * @exception ClassCastException 如果 <code>listenerType</code>
     *          不指定一个实现 <code>java.util.EventListener</code> 的类或接口
     *
     * @see #getActionListeners
     * @since 1.3
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if  (listenerType == ActionListener.class) {
            l = actionListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }

    // REMIND: remove when filtering is done at lower level
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
     * 处理此按钮上的事件。如果事件是 <code>ActionEvent</code> 的实例，此方法调用
     * <code>processActionEvent</code> 方法。否则，调用超类的 <code>processEvent</code> 方法。
     * <p>注意，如果事件参数为 <code>null</code>，行为是未指定的，可能会导致异常。
     *
     * @param        e 事件
     * @see          java.awt.event.ActionEvent
     * @see          java.awt.Button#processActionEvent
     * @since        JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof ActionEvent) {
            processActionEvent((ActionEvent)e);
            return;
        }
        super.processEvent(e);
    }

    /**
     * 处理此按钮上的动作事件，通过将它们分发给所有已注册的
     * <code>ActionListener</code> 对象。
     * <p>
     * 只有在为该按钮启用了动作事件时，此方法才会被调用。动作事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addActionListener</code> 注册了 <code>ActionListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了动作事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>，行为是未指定的，可能会导致异常。
     *
     * @param       e 动作事件
     * @see         java.awt.event.ActionListener
     * @see         java.awt.Button#addActionListener
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
     * 返回表示此 <code>Button</code> 状态的字符串。此方法仅用于调试目的，返回字符串的内容和格式可能因实现而异。返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return     此按钮的参数字符串
     */
    protected String paramString() {
        return super.paramString() + ",label=" + label;
    }


    /* Serialization support.
     */

    /*
     * Button Serial Data Version.
     * @serial
     */
    private int buttonSerializedDataVersion = 1;


                /**
     * 将默认可序列化字段写入流。写入
     * 一个可序列化的 <code>ActionListeners</code>
     * 列表作为可选数据。非可序列化的
     * <code>ActionListeners</code> 会被检测到，
     * 并且不会尝试序列化它们。
     *
     * @serialData 以 <code>null</code> 结尾的 0 个或多个对的序列：
     *   每个对由一个 <code>String</code> 和一个 <code>Object</code> 组成；
     *   <code>String</code> 表示对象的类型，可以是以下之一：
     *   <code>actionListenerK</code> 表示一个
     *     <code>ActionListener</code> 对象
     *
     * @param s 要写入的 <code>ObjectOutputStream</code>
     * @see AWTEventMulticaster#save(ObjectOutputStream, String, EventListener)
     * @see java.awt.Component#actionListenerK
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
     * 读取 <code>ObjectInputStream</code> 并且
     * 如果它不是 <code>null</code>，则向按钮添加一个监听器
     * 以接收按钮触发的动作事件。未识别的键或值将被忽略。
     *
     * @param s 要读取的 <code>ObjectInputStream</code>
     * @exception HeadlessException 如果
     *   <code>GraphicsEnvironment.isHeadless</code> 返回
     *   <code>true</code>
     * @serial
     * @see #removeActionListener(ActionListener)
     * @see #addActionListener(ActionListener)
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

        if (actionListenerK == key)
          addActionListener((ActionListener)(s.readObject()));

        else // 跳过未识别键的值
          s.readObject();
      }
    }


/////////////////
// 可访问性支持
////////////////

    /**
     * 获取与此 <code>Button</code> 关联的 <code>AccessibleContext</code>。
     * 对于按钮，<code>AccessibleContext</code> 采用
     * <code>AccessibleAWTButton</code> 的形式。如果必要，将创建一个新的
     * <code>AccessibleAWTButton</code> 实例。
     *
     * @return 一个 <code>AccessibleAWTButton</code>，作为此 <code>Button</code> 的
     *         <code>AccessibleContext</code>
     * @beaninfo
     *       expert: true
     *  description: 与此按钮关联的 AccessibleContext。
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTButton();
        }
        return accessibleContext;
    }

    /**
     * 为 <code>Button</code> 类实现可访问性支持。
     * 它为按钮用户界面元素提供了 Java 可访问性 API 的实现。
     * @since 1.3
     */
    protected class AccessibleAWTButton extends AccessibleAWTComponent
        implements AccessibleAction, AccessibleValue
    {
        /*
         * JDK 1.3 序列化版本 ID
         */
        private static final long serialVersionUID = -5932203980244017102L;

        /**
         * 获取此对象的可访问名称。
         *
         * @return 对象的本地化名称 -- 如果此对象没有名称，则可以为 null
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
         * 获取与此对象关联的 AccessibleAction。在
         * 为该类实现 Java 可访问性 API 时，返回此对象，该对象负责
         * 代表自身实现 AccessibleAction 接口。
         *
         * @return 此对象
         */
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        /**
         * 获取与此对象关联的 AccessibleValue。在
         * 为该类实现 Java 可访问性 API 时，返回此对象，该对象负责
         * 代表自身实现 AccessibleValue 接口。
         *
         * @return 此对象
         */
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        /**
         * 返回此对象中可用的 Actions 数量。按钮的默认行为是有一个动作 -- 切换按钮。
         *
         * @return 1，此对象中的 Actions 数量
         */
        public int getAccessibleActionCount() {
            return 1;
        }

        /**
         * 返回对象指定动作的描述。
         *
         * @param i 动作的零基索引
         */
        public String getAccessibleActionDescription(int i) {
            if (i == 0) {
                // [[[待处理：WDW -- 需要提供一个本地化字符串]]]
                return "click";
            } else {
                return null;
            }
        }

        /**
         * 在对象上执行指定的动作
         *
         * @param i 动作的零基索引
         * @return 如果执行了动作，则返回 true；否则返回 false。
         */
        public boolean doAccessibleAction(int i) {
            if (i == 0) {
                // 模拟按钮点击
                Toolkit.getEventQueue().postEvent(
                        new ActionEvent(Button.this,
                                        ActionEvent.ACTION_PERFORMED,
                                        Button.this.getActionCommand()));
                return true;
            } else {
                return false;
            }
        }

        /**
         * 获取此对象的值作为 Number。
         *
         * @return 如果未选中，则返回 0 的 Integer；如果选中，则返回 1 的 Integer。
         * @see javax.swing.AbstractButton#isSelected()
         */
        public Number getCurrentAccessibleValue() {
            return Integer.valueOf(0);
        }

        /**
         * 将此对象的值设置为 Number。
         *
         * @return 如果值已设置，则返回 True。
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

        /**
         * 获取此对象的角色。
         *
         * @return 一个 AccessibleRole 实例，描述对象的角色
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PUSH_BUTTON;
        }
    } // 内部类 AccessibleAWTButton

}
