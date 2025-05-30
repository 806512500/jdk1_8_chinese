
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

import java.awt.peer.TextComponentPeer;
import java.awt.event.*;
import java.util.EventListener;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import sun.awt.InputMethodSupport;
import java.text.BreakIterator;
import javax.swing.text.AttributeSet;
import javax.accessibility.*;
import java.awt.im.InputMethodRequests;
import sun.security.util.SecurityConstants;

/**
 * <code>TextComponent</code> 类是任何允许编辑文本的组件的超类。
 * <p>
 * 文本组件体现了一段文本。<code>TextComponent</code> 类定义了一组方法，
 * 用于确定此文本是否可编辑。如果组件是可编辑的，它定义了另一组方法，
 * 用于支持文本插入光标。
 * <p>
 * 此外，该类定义了用于维护当前 <em>选择</em> 的方法。
 * 选择是从组件文本中选择的子字符串，也是编辑操作的目标。
 * 它也被称为 <em>选定文本</em>。
 *
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 * @since       JDK1.0
 */
public class TextComponent extends Component implements Accessible {

    /**
     * 文本的值。
     * <code>null</code> 值等同于 ""。
     *
     * @serial
     * @see #setText(String)
     * @see #getText()
     */
    String text;

    /**
     * 一个布尔值，表示此 <code>TextComponent</code> 是否可编辑。
     * 如果文本组件可编辑，则为 <code>true</code>，否则为 <code>false</code>。
     *
     * @serial
     * @see #isEditable()
     */
    boolean editable = true;

    /**
     * 选择指的是选定的文本，<code>selectionStart</code> 是选定文本的起始位置。
     *
     * @serial
     * @see #getSelectionStart()
     * @see #setSelectionStart(int)
     */
    int selectionStart;

    /**
     * 选择指的是选定的文本，<code>selectionEnd</code> 是选定文本的结束位置。
     *
     * @serial
     * @see #getSelectionEnd()
     * @see #setSelectionEnd(int)
     */
    int selectionEnd;

    // 一个标志，用于指示背景是否由开发人员代码（而不是 AWT 代码）设置。
    // 用于确定非可编辑文本组件的背景颜色。
    boolean backgroundSetByClientCode = false;

    transient protected TextListener textListener;

    /*
     * JDK 1.1 序列化版本 ID
     */
    private static final long serialVersionUID = -2214773872412987419L;

    /**
     * 构造一个新的文本组件，并用指定的文本初始化。将光标的值设置为
     * <code>Cursor.TEXT_CURSOR</code>。
     * @param      text       要显示的文本；如果
     *             <code>text</code> 为 <code>null</code>，则显示空字符串 <code>""</code>
     * @exception  HeadlessException 如果
     *             <code>GraphicsEnvironment.isHeadless</code>
     *             返回 true
     * @see        java.awt.GraphicsEnvironment#isHeadless
     * @see        java.awt.Cursor
     */
    TextComponent(String text) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.text = (text != null) ? text : "";
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    private void enableInputMethodsIfNecessary() {
        if (checkForEnableIM) {
            checkForEnableIM = false;
            try {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                boolean shouldEnable = false;
                if (toolkit instanceof InputMethodSupport) {
                    shouldEnable = ((InputMethodSupport)toolkit)
                      .enableInputMethodsForTextComponent();
                }
                enableInputMethods(shouldEnable);
            } catch (Exception e) {
                // 如果发生错误，就不启用输入方法
            }
        }
    }

    /**
     * 为这个文本组件启用或禁用输入方法支持。如果输入方法支持已启用且文本组件也处理键事件，
     * 则将传入的事件提供给当前输入方法，只有当输入方法不消耗这些事件时，才会由组件处理或分发给其监听器。
     * 是否以及如何为这个文本组件启用或禁用输入方法支持取决于实现。
     *
     * @param enable true 表示启用，false 表示禁用
     * @see #processKeyEvent
     * @since 1.2
     */
    public void enableInputMethods(boolean enable) {
        checkForEnableIM = false;
        super.enableInputMethods(enable);
    }

    boolean areInputMethodsEnabled() {
        // 从上面的构造函数移到这里和 addNotify 下面，
        // 这个调用将在工具包未初始化时初始化工具包。
        if (checkForEnableIM) {
            enableInputMethodsIfNecessary();
        }

        // TextComponent 在不修改事件掩码或没有键监听器的情况下处理键事件，
        // 因此只需检查标志是否已设置
        return (eventMask & AWTEvent.INPUT_METHODS_ENABLED_MASK) != 0;
    }

    public InputMethodRequests getInputMethodRequests() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) return peer.getInputMethodRequests();
        else return null;
    }



    /**
     * 通过连接到本机屏幕资源使此组件可显示。
     * 此方法由工具包内部调用，不应由程序直接调用。
     * @see       java.awt.TextComponent#removeNotify
     */
    public void addNotify() {
        super.addNotify();
        enableInputMethodsIfNecessary();
    }

    /**
     * 移除 <code>TextComponent</code> 的对等体。
     * 对等体允许我们在不改变其功能的情况下修改 <code>TextComponent</code> 的外观。
     */
    public void removeNotify() {
        synchronized (getTreeLock()) {
            TextComponentPeer peer = (TextComponentPeer)this.peer;
            if (peer != null) {
                text = peer.getText();
                selectionStart = peer.getSelectionStart();
                selectionEnd = peer.getSelectionEnd();
            }
            super.removeNotify();
        }
    }

    /**
     * 将此文本组件显示的文本设置为指定的文本。
     * @param       t   新的文本；
     *                  如果此参数为 <code>null</code>，则文本设置为空字符串 ""
     * @see         java.awt.TextComponent#getText
     */
    public synchronized void setText(String t) {
        boolean skipTextEvent = (text == null || text.isEmpty())
                && (t == null || t.isEmpty());
        text = (t != null) ? t : "";
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        // 请注意，我们不希望在 TextArea.setText() 或 TextField.setText() 用空文本
        // 替换空文本时发布事件，即，如果组件的文本保持不变。
        if (peer != null && !skipTextEvent) {
            peer.setText(text);
        }
    }

    /**
     * 返回此文本组件显示的文本。
     * 默认情况下，这是一个空字符串。
     *
     * @return 此 <code>TextComponent</code> 的值
     * @see     java.awt.TextComponent#setText
     */
    public synchronized String getText() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            text = peer.getText();
        }
        return text;
    }

    /**
     * 返回此文本组件显示的选定文本。
     * @return      此文本组件的选定文本
     * @see         java.awt.TextComponent#select
     */
    public synchronized String getSelectedText() {
        return getText().substring(getSelectionStart(), getSelectionEnd());
    }

    /**
     * 指示此文本组件是否可编辑。
     * @return     如果此文本组件可编辑，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @see        java.awt.TextComponent#setEditable
     * @since      JDK1.0
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * 设置确定此文本组件是否可编辑的标志。
     * <p>
     * 如果标志设置为 <code>true</code>，此文本组件将变为用户可编辑。如果标志设置为 <code>false</code>，
     * 用户将无法更改此文本组件的文本。默认情况下，非可编辑文本组件的背景颜色为 SystemColor.control。
     * 可以通过调用 setBackground 覆盖此默认值。
     *
     * @param     b   一个标志，表示此文本组件是否用户可编辑。
     * @see       java.awt.TextComponent#isEditable
     * @since     JDK1.0
     */
    public synchronized void setEditable(boolean b) {
        if (editable == b) {
            return;
        }

        editable = b;
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            peer.setEditable(b);
        }
    }

    /**
     * 获取此文本组件的背景颜色。
     *
     * 默认情况下，非可编辑文本组件的背景颜色为 SystemColor.control。
     * 可以通过调用 setBackground 覆盖此默认值。
     *
     * @return 此文本组件的背景颜色。
     *         如果此文本组件没有背景颜色，则返回其父组件的背景颜色。
     * @see #setBackground(Color)
     * @since JDK1.0
     */
    public Color getBackground() {
        if (!editable && !backgroundSetByClientCode) {
            return SystemColor.control;
        }

        return super.getBackground();
    }

    /**
     * 设置此文本组件的背景颜色。
     *
     * @param c 要成为此文本组件颜色的颜色。
     *        如果此参数为 null，则此文本组件将继承其父组件的背景颜色。
     * @see #getBackground()
     * @since JDK1.0
     */
    public void setBackground(Color c) {
        backgroundSetByClientCode = true;
        super.setBackground(c);
    }

    /**
     * 获取此文本组件中选定文本的起始位置。
     * @return      选定文本的起始位置
     * @see         java.awt.TextComponent#setSelectionStart
     * @see         java.awt.TextComponent#getSelectionEnd
     */
    public synchronized int getSelectionStart() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            selectionStart = peer.getSelectionStart();
        }
        return selectionStart;
    }

    /**
     * 将此文本组件的选定起始位置设置为指定的位置。新的起始点受当前选定结束位置的约束。
     * 它也不能设置为小于零，即组件文本的开头。
     * 如果调用者提供的 <code>selectionStart</code> 值超出范围，该方法将静默地强制执行这些约束，
     * 而不会失败。
     * @param       selectionStart   选定文本的起始位置
     * @see         java.awt.TextComponent#getSelectionStart
     * @see         java.awt.TextComponent#setSelectionEnd
     * @since       JDK1.1
     */
    public synchronized void setSelectionStart(int selectionStart) {
        /* 通过 select 方法来强制执行 selectionStart 和 selectionEnd 之间的一致策略
         */
        select(selectionStart, getSelectionEnd());
    }

    /**
     * 获取此文本组件中选定文本的结束位置。
     * @return      选定文本的结束位置
     * @see         java.awt.TextComponent#setSelectionEnd
     * @see         java.awt.TextComponent#getSelectionStart
     */
    public synchronized int getSelectionEnd() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            selectionEnd = peer.getSelectionEnd();
        }
        return selectionEnd;
    }

    /**
     * 将此文本组件的选定结束位置设置为指定的位置。新的结束点受当前选定起始位置的约束。
     * 它也不能设置为超出组件文本的结尾。
     * 如果调用者提供的 <code>selectionEnd</code> 值超出范围，该方法将静默地强制执行这些约束，
     * 而不会失败。
     * @param       selectionEnd   选定文本的结束位置
     * @see         java.awt.TextComponent#getSelectionEnd
     * @see         java.awt.TextComponent#setSelectionStart
     * @since       JDK1.1
     */
    public synchronized void setSelectionEnd(int selectionEnd) {
        /* 通过 select 方法来强制执行 selectionStart 和 selectionEnd 之间的一致策略
         */
        select(getSelectionStart(), selectionEnd);
    }

    /**
     * 选择指定起始和结束位置之间的文本。
     * <p>
     * 此方法设置选定文本的起始和结束位置，强制执行起始位置必须大于或等于零的限制。
     * 结束位置必须大于或等于起始位置，并且小于或等于文本组件文本的长度。
     * 字符位置从零开始索引。选择的长度为
     * <code>endPosition</code> - <code>startPosition</code>，因此
     * <code>endPosition</code> 位置的字符未被选中。
     * 如果选定文本的起始和结束位置相等，则取消选择所有文本。
     * <p>
     * 如果调用者提供的值不一致或超出范围，该方法将静默地强制执行这些约束，
     * 而不会失败。具体来说，如果起始位置或结束位置大于文本长度，则重置为等于文本长度。
     * 如果起始位置小于零，则重置为零，如果结束位置小于起始位置，则重置为起始位置。
     *
     * @param        selectionStart 选定的第一个字符（<code>char</code> 值）的零基索引
     * @param        selectionEnd 选定文本的零基结束位置；<code>selectionEnd</code> 位置的字符（<code>char</code> 值）未被选中
     * @see          java.awt.TextComponent#setSelectionStart
     * @see          java.awt.TextComponent#setSelectionEnd
     * @see          java.awt.TextComponent#selectAll
     */
    public synchronized void select(int selectionStart, int selectionEnd) {
        String text = getText();
        if (selectionStart < 0) {
            selectionStart = 0;
        }
        if (selectionStart > text.length()) {
            selectionStart = text.length();
        }
        if (selectionEnd > text.length()) {
            selectionEnd = text.length();
        }
        if (selectionEnd < selectionStart) {
            selectionEnd = selectionStart;
        }


                    this.selectionStart = selectionStart;
        this.selectionEnd = selectionEnd;

        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            peer.select(selectionStart, selectionEnd);
        }
    }

    /**
     * 选择此文本组件中的所有文本。
     * @see        java.awt.TextComponent#select
     */
    public synchronized void selectAll() {
        this.selectionStart = 0;
        this.selectionEnd = getText().length();

        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            peer.select(selectionStart, selectionEnd);
        }
    }

    /**
     * 设置文本插入光标的位置。
     * 光标位置被限制在 0 和文本的最后一个字符之间（包括最后一个字符）。
     * 如果传入的值大于此范围，则值被设置为最后一个字符（如果 <code>TextComponent</code> 不包含文本，则为 0），并且不会返回错误。
     * 如果传入的值小于 0，则抛出 <code>IllegalArgumentException</code>。
     *
     * @param        position 文本插入光标的位置
     * @exception    IllegalArgumentException 如果 <code>position</code>
     *               小于零
     * @since        JDK1.1
     */
    public synchronized void setCaretPosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("position less than zero.");
        }

        int maxposition = getText().length();
        if (position > maxposition) {
            position = maxposition;
        }

        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            peer.setCaretPosition(position);
        } else {
            select(position, position);
        }
    }

    /**
     * 返回文本插入光标的位置。
     * 光标位置被限制在 0 和文本的最后一个字符之间（包括最后一个字符）。
     * 如果文本或光标未设置，默认的光标位置为 0。
     *
     * @return       文本插入光标的位置
     * @see #setCaretPosition(int)
     * @since        JDK1.1
     */
    public synchronized int getCaretPosition() {
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        int position = 0;

        if (peer != null) {
            position = peer.getCaretPosition();
        } else {
            position = selectionStart;
        }
        int maxposition = getText().length();
        if (position > maxposition) {
            position = maxposition;
        }
        return position;
    }

    /**
     * 添加指定的文本事件监听器以接收此文本组件的文本事件。
     * 如果 <code>l</code> 为 <code>null</code>，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param l 文本事件监听器
     * @see             #removeTextListener
     * @see             #getTextListeners
     * @see             java.awt.event.TextListener
     */
    public synchronized void addTextListener(TextListener l) {
        if (l == null) {
            return;
        }
        textListener = AWTEventMulticaster.add(textListener, l);
        newEventsOnly = true;
    }

    /**
     * 移除指定的文本事件监听器，使其不再接收此文本组件的文本事件。
     * 如果 <code>l</code> 为 <code>null</code>，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param           l     文本监听器
     * @see             #addTextListener
     * @see             #getTextListeners
     * @see             java.awt.event.TextListener
     * @since           JDK1.1
     */
    public synchronized void removeTextListener(TextListener l) {
        if (l == null) {
            return;
        }
        textListener = AWTEventMulticaster.remove(textListener, l);
    }

    /**
     * 返回注册在此文本组件上的所有文本监听器的数组。
     *
     * @return 此文本组件的所有 <code>TextListener</code>，如果未注册任何文本监听器，则返回空数组
     *
     *
     * @see #addTextListener
     * @see #removeTextListener
     * @since 1.4
     */
    public synchronized TextListener[] getTextListeners() {
        return getListeners(TextListener.class);
    }

    /**
     * 返回注册在此 <code>TextComponent</code> 上的所有 <code><em>Foo</em>Listener</code> 的数组。
     * <code><em>Foo</em>Listener</code> 通过 <code>add<em>Foo</em>Listener</code> 方法注册。
     *
     * <p>
     * 您可以使用类字面量指定 <code>listenerType</code> 参数，例如
     * <code><em>Foo</em>Listener.class</code>。
     * 例如，您可以查询 <code>TextComponent</code> <code>t</code>
     * 的文本监听器，使用以下代码：
     *
     * <pre>TextListener[] tls = (TextListener[])(t.getListeners(TextListener.class));</pre>
     *
     * 如果没有注册此类监听器，此方法返回空数组。
     *
     * @param listenerType 请求的监听器类型；此参数应指定一个继承自
     *          <code>java.util.EventListener</code> 的类或接口
     * @return 注册在此文本组件上的所有
     *          <code><em>Foo</em>Listener</code> 的数组，
     *          如果未添加此类监听器，则返回空数组
     * @exception ClassCastException 如果 <code>listenerType</code>
     *          不指定一个实现 <code>java.util.EventListener</code> 的类或接口
     *
     * @see #getTextListeners
     * @since 1.3
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if  (listenerType == TextListener.class) {
            l = textListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e) {
        if (e.id == TextEvent.TEXT_VALUE_CHANGED) {
            if ((eventMask & AWTEvent.TEXT_EVENT_MASK) != 0 ||
                textListener != null) {
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }

    /**
     * 处理此文本组件上的事件。如果事件是 <code>TextEvent</code>，则调用 <code>processTextEvent</code>
     * 方法，否则调用其父类的 <code>processEvent</code> 方法。
     * <p>注意，如果事件参数为 <code>null</code>，则行为未定义，可能会导致异常。
     *
     * @param e 事件
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof TextEvent) {
            processTextEvent((TextEvent)e);
            return;
        }
        super.processEvent(e);
    }

    /**
     * 通过分派给任何注册的 <code>TextListener</code> 对象来处理此文本组件上的文本事件。
     * <p>
     * 注意：除非为该组件启用了文本事件，否则此方法不会被调用。这发生在以下情况之一：
     * <ul>
     * <li>通过 <code>addTextListener</code> 注册了 <code>TextListener</code> 对象
     * <li>通过 <code>enableEvents</code> 启用了文本事件
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>，则行为未定义，可能会导致异常。
     *
     * @param e 文本事件
     * @see Component#enableEvents
     */
    protected void processTextEvent(TextEvent e) {
        TextListener listener = textListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
            case TextEvent.TEXT_VALUE_CHANGED:
                listener.textValueChanged(e);
                break;
            }
        }
    }

    /**
     * 返回表示此 <code>TextComponent</code> 状态的字符串。此方法仅用于调试目的，返回字符串的内容和格式可能因实现而异。
     * 返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return 此文本组件的参数字符串
     */
    protected String paramString() {
        String str = super.paramString() + ",text=" + getText();
        if (editable) {
            str += ",editable";
        }
        return str + ",selection=" + getSelectionStart() + "-" + getSelectionEnd();
    }

    /**
     * 为 canAccessClipboard 实例变量分配一个有效值。
     */
    private boolean canAccessClipboard() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) return true;
        try {
            sm.checkPermission(SecurityConstants.AWT.ACCESS_CLIPBOARD_PERMISSION);
            return true;
        } catch (SecurityException e) {}
        return false;
    }

    /*
     * 序列化支持。
     */
    /**
     * textComponent 序列化数据版本。
     *
     * @serial
     */
    private int textComponentSerializedDataVersion = 1;

    /**
     * 将默认的可序列化字段写入流。写入一个可选的可序列化 TextListener 列表。
     * 非可序列化的 TextListener 被检测到，不会尝试序列化它们。
     *
     * @serialData 以 null 结尾的零个或多个键值对的序列。
     *             每个键值对由一个字符串和一个对象组成。
     *             字符串表示对象的类型，可以是以下之一：
     *             textListenerK 表示一个 TextListener 对象。
     *
     * @see AWTEventMulticaster#save(ObjectOutputStream, String, EventListener)
     * @see java.awt.Component#textListenerK
     */
    private void writeObject(java.io.ObjectOutputStream s)
      throws IOException
    {
        // 序列化支持。由于 selectionStart、selectionEnd 和 text 字段的值不一定是最新的，
        // 在序列化之前，我们同步这些值。
        TextComponentPeer peer = (TextComponentPeer)this.peer;
        if (peer != null) {
            text = peer.getText();
            selectionStart = peer.getSelectionStart();
            selectionEnd = peer.getSelectionEnd();
        }

        s.defaultWriteObject();

        AWTEventMulticaster.save(s, textListenerK, textListener);
        s.writeObject(null);
    }

    /**
     * 读取 ObjectInputStream，如果它不为 null，则添加一个监听器以接收
     * TextComponent 触发的文本事件。未识别的键或值将被忽略。
     *
     * @exception HeadlessException 如果
     * <code>GraphicsEnvironment.isHeadless()</code> 返回
     * <code>true</code>
     * @see #removeTextListener
     * @see #addTextListener
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException, HeadlessException
    {
        GraphicsEnvironment.checkHeadless();
        s.defaultReadObject();

        // 确保刚读取的 text、selectionStart 和 selectionEnd 的状态值是合法的
        this.text = (text != null) ? text : "";
        select(selectionStart, selectionEnd);

        Object keyOrNull;
        while(null != (keyOrNull = s.readObject())) {
            String key = ((String)keyOrNull).intern();

            if (textListenerK == key) {
                addTextListener((TextListener)(s.readObject()));
            } else {
                // 跳过未识别键的值
                s.readObject();
            }
        }
        enableInputMethodsIfNecessary();
    }


/////////////////
// Accessibility support
////////////////

    /**
     * 获取与此 TextComponent 关联的 AccessibleContext。
     * 对于文本组件，AccessibleContext 采用 AccessibleAWTTextComponent 的形式。
     * 如果必要，将创建一个新的 AccessibleAWTTextComponent 实例。
     *
     * @return 一个 AccessibleAWTTextComponent，作为此 TextComponent 的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTTextComponent();
        }
        return accessibleContext;
    }

    /**
     * 为 <code>TextComponent</code> 类实现辅助功能支持。
     * 它为文本组件用户界面元素提供了 Java 辅助功能 API 的实现。
     * @since 1.3
     */
    protected class AccessibleAWTTextComponent extends AccessibleAWTComponent
        implements AccessibleText, TextListener
    {
        /*
         * JDK 1.3 序列化版本号
         */
        private static final long serialVersionUID = 3631432373506317811L;

        /**
         * 构造 AccessibleAWTTextComponent。添加一个监听器以跟踪光标变化。
         */
        public AccessibleAWTTextComponent() {
            TextComponent.this.addTextListener(this);
        }

        /**
         * 文本值变化时的 TextListener 通知。
         */
        public void textValueChanged(TextEvent textEvent)  {
            Integer cpos = Integer.valueOf(TextComponent.this.getCaretPosition());
            firePropertyChange(ACCESSIBLE_TEXT_PROPERTY, null, cpos);
        }

        /**
         * 获取此对象的状态集。
         * 对象的 AccessibleStateSet 由一组唯一的 AccessibleStates 组成。
         * 对象的 AccessibleStateSet 发生变化时，将触发一个 PropertyChangeEvent，
         * 属性名为 AccessibleContext.ACCESSIBLE_STATE_PROPERTY。
         *
         * @return 一个 AccessibleStateSet 实例，包含对象的当前状态集
         * @see AccessibleStateSet
         * @see AccessibleState
         * @see #addPropertyChangeListener
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (TextComponent.this.isEditable()) {
                states.add(AccessibleState.EDITABLE);
            }
            return states;
        }


        /**
         * 获取此对象的角色。
         *
         * @return 一个 AccessibleRole 实例，描述对象的角色（AccessibleRole.TEXT）
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TEXT;
        }


                    /**
         * 获取与此对象关联的 AccessibleText。在实现 Java Accessibility API 时，
         * 返回此对象，该对象负责代表自身实现 AccessibleText 接口。
         *
         * @return 此对象
         */
        public AccessibleText getAccessibleText() {
            return this;
        }


        // --- AccessibleText 接口方法 ------------------------

        /**
         * 这些方法中的许多都是方便方法；它们只是调用父类的相应方法。
         */

        /**
         * 给定一个本地坐标系中的点，返回该点下的字符的零基索引。如果点无效，
         * 此方法返回 -1。
         *
         * @param p 本地坐标系中的点
         * @return 点 p 下的字符的零基索引。
         */
        public int getIndexAtPoint(Point p) {
            return -1;
        }

        /**
         * 确定给定索引处字符的边界框。边界框以本地坐标系返回。如果索引无效，
         * 则返回 null 矩形。
         *
         * @param i 字符串中的索引 &gt;= 0
         * @return 字符边界框的屏幕坐标
         */
        public Rectangle getCharacterBounds(int i) {
            return null;
        }

        /**
         * 返回字符数（有效索引）
         *
         * @return 字符数 &gt;= 0
         */
        public int getCharCount() {
            return TextComponent.this.getText().length();
        }

        /**
         * 返回光标的零基偏移量。
         *
         * 注意：光标右侧的字符将具有与偏移量相同的索引值（光标位于两个字符之间）。
         *
         * @return 光标的零基偏移量。
         */
        public int getCaretPosition() {
            return TextComponent.this.getCaretPosition();
        }

        /**
         * 返回给定字符（给定索引处）的 AttributeSet。
         *
         * @param i 文本中的零基索引
         * @return 字符的 AttributeSet
         */
        public AttributeSet getCharacterAttribute(int i) {
            return null; // TextComponent 中没有属性
        }

        /**
         * 返回选定文本的起始偏移量。
         * 如果没有选择，但有光标，起始和结束偏移量将相同。
         * 如果文本为空，返回 0，或如果没有选择，返回光标位置。
         *
         * @return 选定文本起始处的索引 &gt;= 0
         */
        public int getSelectionStart() {
            return TextComponent.this.getSelectionStart();
        }

        /**
         * 返回选定文本的结束偏移量。
         * 如果没有选择，但有光标，起始和结束偏移量将相同。
         * 如果文本为空，返回 0，或如果没有选择，返回光标位置。
         *
         * @return 选定文本结束处的索引 &gt;= 0
         */
        public int getSelectionEnd() {
            return TextComponent.this.getSelectionEnd();
        }

        /**
         * 返回选定的文本部分。
         *
         * @return 选定的文本，如果没有选择则返回 null
         */
        public String getSelectedText() {
            String selText = TextComponent.this.getSelectedText();
            // 修复 4256662
            if (selText == null || selText.equals("")) {
                return null;
            }
            return selText;
        }

        /**
         * 返回给定索引处的字符串。
         *
         * @param part 要检索的 AccessibleText.CHARACTER, AccessibleText.WORD,
         * 或 AccessibleText.SENTENCE
         * @param index 文本中的索引 &gt;= 0
         * @return 字母、单词或句子，如果索引或部分无效则返回 null
         */
        public String getAtIndex(int part, int index) {
            if (index < 0 || index >= TextComponent.this.getText().length()) {
                return null;
            }
            switch (part) {
            case AccessibleText.CHARACTER:
                return TextComponent.this.getText().substring(index, index+1);
            case AccessibleText.WORD:  {
                    String s = TextComponent.this.getText();
                    BreakIterator words = BreakIterator.getWordInstance();
                    words.setText(s);
                    int end = words.following(index);
                    return s.substring(words.previous(), end);
                }
            case AccessibleText.SENTENCE:  {
                    String s = TextComponent.this.getText();
                    BreakIterator sentence = BreakIterator.getSentenceInstance();
                    sentence.setText(s);
                    int end = sentence.following(index);
                    return s.substring(sentence.previous(), end);
                }
            default:
                return null;
            }
        }

        private static final boolean NEXT = true;
        private static final boolean PREVIOUS = false;

        /**
         * 用于统一前向和后向搜索。
         * 该方法假设 s 是分配给 words 的文本。
         */
        private int findWordLimit(int index, BreakIterator words, boolean direction,
                                         String s) {
            // 修复 4256660 和 4256661。
            // 单词迭代器与字符和句子迭代器不同，一个单词的结束不一定是另一个单词的开始。
            // 请参阅 java.text.BreakIterator JavaDoc。下面的代码基于 BreakIterator.java 中的 nextWordStartAfter 示例。
            int last = (direction == NEXT) ? words.following(index)
                                           : words.preceding(index);
            int current = (direction == NEXT) ? words.next()
                                              : words.previous();
            while (current != BreakIterator.DONE) {
                for (int p = Math.min(last, current); p < Math.max(last, current); p++) {
                    if (Character.isLetter(s.charAt(p))) {
                        return last;
                    }
                }
                last = current;
                current = (direction == NEXT) ? words.next()
                                              : words.previous();
            }
            return BreakIterator.DONE;
        }

        /**
         * 返回给定索引后的字符串。
         *
         * @param part 要检索的 AccessibleText.CHARACTER, AccessibleText.WORD,
         * 或 AccessibleText.SENTENCE
         * @param index 文本中的索引 &gt;= 0
         * @return 字母、单词或句子，如果索引或部分无效则返回 null
         */
        public String getAfterIndex(int part, int index) {
            if (index < 0 || index >= TextComponent.this.getText().length()) {
                return null;
            }
            switch (part) {
            case AccessibleText.CHARACTER:
                if (index+1 >= TextComponent.this.getText().length()) {
                   return null;
                }
                return TextComponent.this.getText().substring(index+1, index+2);
            case AccessibleText.WORD:  {
                    String s = TextComponent.this.getText();
                    BreakIterator words = BreakIterator.getWordInstance();
                    words.setText(s);
                    int start = findWordLimit(index, words, NEXT, s);
                    if (start == BreakIterator.DONE || start >= s.length()) {
                        return null;
                    }
                    int end = words.following(start);
                    if (end == BreakIterator.DONE || end >= s.length()) {
                        return null;
                    }
                    return s.substring(start, end);
                }
            case AccessibleText.SENTENCE:  {
                    String s = TextComponent.this.getText();
                    BreakIterator sentence = BreakIterator.getSentenceInstance();
                    sentence.setText(s);
                    int start = sentence.following(index);
                    if (start == BreakIterator.DONE || start >= s.length()) {
                        return null;
                    }
                    int end = sentence.following(start);
                    if (end == BreakIterator.DONE || end >= s.length()) {
                        return null;
                    }
                    return s.substring(start, end);
                }
            default:
                return null;
            }
        }


        /**
         * 返回给定索引前的字符串。
         *
         * @param part 要检索的 AccessibleText.CHARACTER, AccessibleText.WORD,
         *   或 AccessibleText.SENTENCE
         * @param index 文本中的索引 &gt;= 0
         * @return 字母、单词或句子，如果索引或部分无效则返回 null
         */
        public String getBeforeIndex(int part, int index) {
            if (index < 0 || index > TextComponent.this.getText().length()-1) {
                return null;
            }
            switch (part) {
            case AccessibleText.CHARACTER:
                if (index == 0) {
                    return null;
                }
                return TextComponent.this.getText().substring(index-1, index);
            case AccessibleText.WORD:  {
                    String s = TextComponent.this.getText();
                    BreakIterator words = BreakIterator.getWordInstance();
                    words.setText(s);
                    int end = findWordLimit(index, words, PREVIOUS, s);
                    if (end == BreakIterator.DONE) {
                        return null;
                    }
                    int start = words.preceding(end);
                    if (start == BreakIterator.DONE) {
                        return null;
                    }
                    return s.substring(start, end);
                }
            case AccessibleText.SENTENCE:  {
                    String s = TextComponent.this.getText();
                    BreakIterator sentence = BreakIterator.getSentenceInstance();
                    sentence.setText(s);
                    int end = sentence.following(index);
                    end = sentence.previous();
                    int start = sentence.previous();
                    if (start == BreakIterator.DONE) {
                        return null;
                    }
                    return s.substring(start, end);
                }
            default:
                return null;
            }
        }
    }  // end of AccessibleAWTTextComponent

    private boolean checkForEnableIM = true;
}
