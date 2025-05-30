
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

import java.awt.peer.TextFieldPeer;
import java.awt.event.*;
import java.util.EventListener;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import javax.accessibility.*;


/**
 * <code>TextField</code> 对象是一个文本组件，允许编辑单行文本。
 * <p>
 * 例如，以下图像显示了一个包含四个不同宽度的文本字段的框架。其中两个文本字段
 * 显示预定义的文本 <code>"Hello"</code>。
 * <p>
 * <img src="doc-files/TextField-1.gif" alt="上述文本描述了此图像。"
 * style="float:center; margin: 7px 10px;">
 * <p>
 * 以下是生成这四个文本字段的代码：
 *
 * <hr><blockquote><pre>
 * TextField tf1, tf2, tf3, tf4;
 * // 一个空白的文本字段
 * tf1 = new TextField();
 * // 20 列的空白字段
 * tf2 = new TextField("", 20);
 * // 显示预定义文本
 * tf3 = new TextField("Hello!");
 * // 30 列的预定义文本
 * tf4 = new TextField("Hello", 30);
 * </pre></blockquote><hr>
 * <p>
 * 每当用户在文本字段中键入一个键时，会向文本字段发送一个或多个键事件。一个 <code>KeyEvent</code>
 * 可能是三种类型之一：keyPressed、keyReleased 或 keyTyped。
 * 事件的属性指示了它是哪种类型，以及关于事件的其他信息，例如应用了哪些修饰符以及事件发生的时间。
 * <p>
 * 键事件会传递给每个注册以接收此类事件的 <code>KeyListener</code>
 * 或 <code>KeyAdapter</code> 对象（<code>KeyAdapter</code> 对象实现了
 * <code>KeyListener</code> 接口）。
 * <p>
 * 也可以触发一个 <code>ActionEvent</code>。如果为文本字段启用了动作事件，可以通过按
 * <code>Return</code> 键来触发它们。
 * <p>
 * <code>TextField</code> 类的 <code>processEvent</code>
 * 方法会检查动作事件并将其传递给 <code>processActionEvent</code>。后者方法将事件重定向到
 * 任何已注册以接收此文本字段生成的动作事件的 <code>ActionListener</code> 对象。
 *
 * @author      Sami Shaio
 * @see         java.awt.event.KeyEvent
 * @see         java.awt.event.KeyAdapter
 * @see         java.awt.event.KeyListener
 * @see         java.awt.event.ActionEvent
 * @see         java.awt.Component#addKeyListener
 * @see         java.awt.TextField#processEvent
 * @see         java.awt.TextField#processActionEvent
 * @see         java.awt.TextField#addActionListener
 * @since       JDK1.0
 */
public class TextField extends TextComponent {

    /**
     * 文本字段中的列数。
     * 一列是平台依赖的近似平均字符宽度。
     * 保证非负。
     *
     * @serial
     * @see #setColumns(int)
     * @see #getColumns()
     */
    int columns;

    /**
     * 回显字符，用于在用户希望隐藏输入到文本字段中的字符时使用。
     * 如果 echoChar = <code>0</code>，则不隐藏字符。
     *
     * @serial
     * @see #getEchoChar()
     * @see #setEchoChar(char)
     * @see #echoCharIsSet()
     */
    char echoChar;

    transient ActionListener actionListener;

    private static final String base = "textfield";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -2966288784432217853L;

    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();

    static {
        /* 确保加载了必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * 构造一个新的文本字段。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public TextField() throws HeadlessException {
        this("", 0);
    }

    /**
     * 构造一个初始化为指定文本的新文本字段。
     * @param      text       要显示的文本。如果
     *             <code>text</code> 为 <code>null</code>，则显示空字符串 <code>""</code>。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public TextField(String text) throws HeadlessException {
        this(text, (text != null) ? text.length() : 0);
    }

    /**
     * 构造一个指定列数的新空文本字段。一列是平台依赖的近似平均字符宽度。
     * @param      columns     列数。如果
     *             <code>columns</code> 小于 <code>0</code>，
     *             <code>columns</code> 设置为 <code>0</code>。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public TextField(int columns) throws HeadlessException {
        this("", columns);
    }

    /**
     * 构造一个初始化为指定文本的新文本字段，并且足够宽以容纳指定的列数。一列是平台依赖的近似平均字符宽度。
     * @param      text       要显示的文本。如果
     *             <code>text</code> 为 <code>null</code>，则显示空字符串 <code>""</code>。
     * @param      columns     列数。如果
     *             <code>columns</code> 小于 <code>0</code>，
     *             <code>columns</code> 设置为 <code>0</code>。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true。
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public TextField(String text, int columns) throws HeadlessException {
        super(text);
        this.columns = (columns >= 0) ? columns : 0;
    }

    /**
     * 为该组件构造一个名称。当名称为 null 时，由 getName() 调用。
     */
    String constructComponentName() {
        synchronized (TextField.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 创建文本字段的对等体。对等体允许我们修改文本字段的外观而不改变其功能。
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createTextField(this);
            super.addNotify();
        }
    }

    /**
     * 获取用于回显的字符。
     * <p>
     * 回显字符对于用户输入不应显示在屏幕上的文本字段非常有用，例如输入密码的文本字段。
     * 如果 <code>echoChar</code> = <code>0</code>，用户输入将不加改变地显示在屏幕上。
     * <p>
     * Java 平台实现可能只支持有限的、非空的回显字符集。此函数返回通过 setEchoChar() 原始请求的回显字符。
     * 文本字段实现实际使用的回显字符可能不同。
     * @return      该文本字段的回显字符。
     * @see         java.awt.TextField#echoCharIsSet
     * @see         java.awt.TextField#setEchoChar
     */
    public char getEchoChar() {
        return echoChar;
    }

    /**
     * 设置该文本字段的回显字符。
     * <p>
     * 回显字符对于用户输入不应显示在屏幕上的文本字段非常有用，例如输入密码的文本字段。
     * 设置 <code>echoChar</code> = <code>0</code> 允许用户输入再次显示在屏幕上。
     * <p>
     * Java 平台实现可能只支持有限的、非空的回显字符集。尝试设置不受支持的回显字符将导致使用默认回显字符。
     * 后续调用 getEchoChar() 将返回原始请求的回显字符。这可能与文本字段实现实际使用的回显字符相同或不同。
     * @param       c   该文本字段的回显字符。
     * @see         java.awt.TextField#echoCharIsSet
     * @see         java.awt.TextField#getEchoChar
     * @since       JDK1.1
     */
    public void setEchoChar(char c) {
        setEchoCharacter(c);
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>setEchoChar(char)</code>。
     */
    @Deprecated
    public synchronized void setEchoCharacter(char c) {
        if (echoChar != c) {
            echoChar = c;
            TextFieldPeer peer = (TextFieldPeer)this.peer;
            if (peer != null) {
                peer.setEchoChar(c);
            }
        }
    }

    /**
     * 设置该文本组件显示的文本为指定文本。
     * @param       t   新的文本。
     * @see         java.awt.TextComponent#getText
     */
    public void setText(String t) {
        super.setText(t);

        // 这可能会改变组件的首选大小。
        invalidateIfValid();
    }

    /**
     * 指示该文本字段是否设置了回显字符。
     * <p>
     * 回显字符对于用户输入不应显示在屏幕上的文本字段非常有用，例如输入密码的文本字段。
     * @return     <code>true</code> 如果该文本字段设置了回显字符；
     *                 <code>false</code> 否则。
     * @see        java.awt.TextField#setEchoChar
     * @see        java.awt.TextField#getEchoChar
     */
    public boolean echoCharIsSet() {
        return echoChar != 0;
    }

    /**
     * 获取该文本字段的列数。一列是平台依赖的近似平均字符宽度。
     * @return     列数。
     * @see        java.awt.TextField#setColumns
     * @since      JDK1.1
     */
    public int getColumns() {
        return columns;
    }

    /**
     * 设置该文本字段的列数。一列是平台依赖的近似平均字符宽度。
     * @param      columns   列数。
     * @see        java.awt.TextField#getColumns
     * @exception  IllegalArgumentException   如果提供的 <code>columns</code>
     *                 小于 <code>0</code>。
     * @since      JDK1.1
     */
    public void setColumns(int columns) {
        int oldVal;
        synchronized (this) {
            oldVal = this.columns;
            if (columns < 0) {
                throw new IllegalArgumentException("columns less than zero.");
            }
            if (columns != oldVal) {
                this.columns = columns;
            }
        }

        if (columns != oldVal) {
            invalidate();
        }
    }

    /**
     * 获取具有指定列数的文本字段的首选大小。
     * @param     columns 该文本字段的列数。
     * @return    显示该文本字段的首选尺寸。
     * @since     JDK1.1
     */
    public Dimension getPreferredSize(int columns) {
        return preferredSize(columns);
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>getPreferredSize(int)</code>。
     */
    @Deprecated
    public Dimension preferredSize(int columns) {
        synchronized (getTreeLock()) {
            TextFieldPeer peer = (TextFieldPeer)this.peer;
            return (peer != null) ?
                       peer.getPreferredSize(columns) :
                       super.preferredSize();
        }
    }

    /**
     * 获取该文本字段的首选大小。
     * @return     显示该文本字段的首选尺寸。
     * @since      JDK1.1
     */
    public Dimension getPreferredSize() {
        return preferredSize();
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>getPreferredSize()</code>。
     */
    @Deprecated
    public Dimension preferredSize() {
        synchronized (getTreeLock()) {
            return (columns > 0) ?
                       preferredSize(columns) :
                       super.preferredSize();
        }
    }

    /**
     * 获取具有指定列数的文本字段的最小尺寸。
     * @param    columns   该文本字段的列数。
     * @since    JDK1.1
     */
    public Dimension getMinimumSize(int columns) {
        return minimumSize(columns);
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>getMinimumSize(int)</code>。
     */
    @Deprecated
    public Dimension minimumSize(int columns) {
        synchronized (getTreeLock()) {
            TextFieldPeer peer = (TextFieldPeer)this.peer;
            return (peer != null) ?
                       peer.getMinimumSize(columns) :
                       super.minimumSize();
        }
    }

    /**
     * 获取该文本字段的最小尺寸。
     * @return     显示该文本字段的最小尺寸。
     * @since      JDK1.1
     */
    public Dimension getMinimumSize() {
        return minimumSize();
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>getMinimumSize()</code>。
     */
    @Deprecated
    public Dimension minimumSize() {
        synchronized (getTreeLock()) {
            return (columns > 0) ?
                       minimumSize(columns) :
                       super.minimumSize();
        }
    }


                /**
     * 添加指定的动作监听器以接收来自此文本字段的动作事件。
     * 如果 l 为 null，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 的线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param      l 动作监听器。
     * @see        #removeActionListener
     * @see        #getActionListeners
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
     * 移除指定的动作监听器，使其不再接收来自此文本字段的动作事件。
     * 如果 l 为 null，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 的线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param           l 动作监听器。
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
     * 返回注册在此文本字段上的所有动作监听器的数组。
     *
     * @return 此文本字段的所有 <code>ActionListener</code>，如果没有注册动作监听器，则返回空数组。
     *
     * @see #addActionListener
     * @see #removeActionListener
     * @see java.awt.event.ActionListener
     * @since 1.4
     */
    public synchronized ActionListener[] getActionListeners() {
        return getListeners(ActionListener.class);
    }

    /**
     * 返回注册在此 <code>TextField</code> 上的所有 <code><em>Foo</em>Listener</code> 的数组。
     * <code><em>Foo</em>Listener</code> 通过 <code>add<em>Foo</em>Listener</code> 方法注册。
     *
     * <p>
     * 您可以使用类字面量指定 <code>listenerType</code> 参数，例如 <code><em>Foo</em>Listener.class</code>。
     * 例如，您可以使用以下代码查询 <code>TextField</code> <code>t</code> 的动作监听器：
     *
     * <pre>ActionListener[] als = (ActionListener[])(t.getListeners(ActionListener.class));</pre>
     *
     * 如果没有这样的监听器存在，此方法返回空数组。
     *
     * @param listenerType 请求的监听器类型；此参数应指定一个实现 <code>java.util.EventListener</code> 的类或接口。
     * @return 注册在此文本字段上的所有 <code><em>Foo</em>Listener</code> 的数组，如果没有添加这样的监听器，则返回空数组。
     * @exception ClassCastException 如果 <code>listenerType</code> 没有指定实现 <code>java.util.EventListener</code> 的类或接口。
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
     * 处理此文本字段上的事件。如果事件是 <code>ActionEvent</code> 的实例，
     * 则调用 <code>processActionEvent</code> 方法。否则，调用超类的 <code>processEvent</code> 方法。
     * <p>注意，如果事件参数为 <code>null</code>，则行为是未指定的，可能会导致异常。
     *
     * @param      e 事件
     * @see        java.awt.event.ActionEvent
     * @see        java.awt.TextField#processActionEvent
     * @since      JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof ActionEvent) {
            processActionEvent((ActionEvent)e);
            return;
        }
        super.processEvent(e);
    }

    /**
     * 通过分派给任何已注册的 <code>ActionListener</code> 对象来处理此文本字段上的动作事件。
     * <p>
     * 除非为该组件启用了动作事件，否则不会调用此方法。动作事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addActionListener</code> 注册了 <code>ActionListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了动作事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>，则行为是未指定的，可能会导致异常。
     *
     * @param       e 动作事件
     * @see         java.awt.event.ActionListener
     * @see         java.awt.TextField#addActionListener
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
     * 返回表示此 <code>TextField</code> 状态的字符串。
     * 此方法仅用于调试目的，返回字符串的内容和格式可能会因实现而异。返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return      此文本字段的参数字符串
     */
    protected String paramString() {
        String str = super.paramString();
        if (echoChar != 0) {
            str += ",echo=" + echoChar;
        }
        return str;
    }


    /*
     * 序列化支持。
     */
    /**
     * 文本字段的序列化数据版本。
     *
     * @serial
     */
    private int textFieldSerializedDataVersion = 1;

    /**
     * 将默认的可序列化字段写入流中。写入一个可选的可序列化 <code>ActionListener</code> 列表。
     * 非可序列化的 <code>ActionListener</code> 会被检测到，并不会尝试序列化它们。
     *
     * @serialData 以 null 结尾的零个或多个对的序列。一个对由一个字符串和一个对象组成。
     *             字符串表示对象的类型，可以是以下之一：
     *             ActionListenerK 表示一个 <code>ActionListener</code> 对象。
     *
     * @see AWTEventMulticaster#save(ObjectOutputStream, String, EventListener)
     * @see java.awt.Component#actionListenerK
     */
    private void writeObject(ObjectOutputStream s)
      throws IOException
    {
        s.defaultWriteObject();

        AWTEventMulticaster.save(s, actionListenerK, actionListener);
        s.writeObject(null);
    }

    /**
     * 读取 ObjectInputStream 并如果它不为 null，则添加一个监听器以接收由 TextField 触发的动作事件。
     * 无法识别的键或值将被忽略。
     *
     * @exception HeadlessException 如果 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code>
     * @see #removeActionListener(ActionListener)
     * @see #addActionListener(ActionListener)
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException
    {
        // HeadlessException 将由 TextComponent 的 readObject 抛出
        s.defaultReadObject();

        // 确保我们刚刚读取的列状态具有合法值
        if (columns < 0) {
            columns = 0;
        }

        // 读取监听器，如果有
        Object keyOrNull;
        while(null != (keyOrNull = s.readObject())) {
            String key = ((String)keyOrNull).intern();

            if (actionListenerK == key) {
                addActionListener((ActionListener)(s.readObject()));
            } else {
                // 跳过无法识别的键的值
                s.readObject();
            }
        }
    }


/////////////////
// 可访问性支持
////////////////


    /**
     * 获取与此 TextField 关联的 AccessibleContext。
     * 对于文本字段，AccessibleContext 采用 AccessibleAWTTextField 的形式。
     * 如果必要，将创建一个新的 AccessibleAWTTextField 实例。
     *
     * @return 一个 AccessibleAWTTextField，作为此 TextField 的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTTextField();
        }
        return accessibleContext;
    }

    /**
     * 为 <code>TextField</code> 类实现可访问性支持。
     * 它为文本字段用户界面元素提供了 Java 可访问性 API 的实现。
     * @since 1.3
     */
    protected class AccessibleAWTTextField extends AccessibleAWTTextComponent
    {
        /*
         * JDK 1.3 序列化版本号
         */
        private static final long serialVersionUID = 6219164359235943158L;

        /**
         * 获取此对象的状态集。
         *
         * @return 一个 AccessibleStateSet 实例，描述对象的状态
         * @see AccessibleState
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.SINGLE_LINE);
            return states;
        }
    }

}
