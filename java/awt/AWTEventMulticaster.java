
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.*;
import java.lang.reflect.Array;
import java.util.EventListener;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.EventListener;


/**
 * {@code AWTEventMulticaster} 实现了高效且线程安全的多播事件分发，适用于 {@code java.awt.event}
 * 包中定义的 AWT 事件。
 * <p>
 * 以下示例说明了如何使用此类：
 *
 * <pre><code>
 * public myComponent extends Component {
 *     ActionListener actionListener = null;
 *
 *     public synchronized void addActionListener(ActionListener l) {
 *         actionListener = AWTEventMulticaster.add(actionListener, l);
 *     }
 *     public synchronized void removeActionListener(ActionListener l) {
 *         actionListener = AWTEventMulticaster.remove(actionListener, l);
 *     }
 *     public void processEvent(AWTEvent e) {
 *         // 当发生导致“action”语义的事件时
 *         ActionListener listener = actionListener;
 *         if (listener != null) {
 *             listener.actionPerformed(new ActionEvent());
 *         }
 *     }
 * }
 * </code></pre>
 * 需要注意的重要一点是，传递给 {@code add} 和 {@code remove} 方法的第一个参数是维护监听器的字段。
 * 此外，必须将 {@code add} 和 {@code remove} 方法的结果赋值给维护监听器的字段。
 * <p>
 * {@code AWTEventMulticaster} 实现为在构造时设置的一对 {@code EventListeners}。{@code AWTEventMulticaster} 是不可变的。
 * {@code add} 和 {@code remove} 方法不会以任何方式修改 {@code AWTEventMulticaster}。如果需要，将创建一个新的 {@code AWTEventMulticaster}。
 * 这样在事件分发过程中添加和删除监听器是安全的。然而，在事件分发过程中添加的事件监听器不会被通知当前正在分发的事件。
 * <p>
 * 所有 {@code add} 方法都允许 {@code null} 参数。如果第一个参数是 {@code null}，则返回第二个参数。如果第一个参数不是 {@code null}，
 * 而第二个参数是 {@code null}，则返回第一个参数。如果两个参数都不是 {@code null}，则使用这两个参数创建一个新的 {@code AWTEventMulticaster} 并返回。
 * <p>
 * 对于接受两个参数的 {@code remove} 方法，返回以下内容：
 * <ul>
 *   <li>{@code null}，如果第一个参数是 {@code null}，或者两个参数相等，通过 {@code ==} 判断。
 *   <li>第一个参数，如果第一个参数不是 {@code AWTEventMulticaster} 的实例。
 *   <li>调用 {@code remove(EventListener)} 方法的结果，将第二个参数传递给该方法。
 * </ul>
 * <p>Swing 使用 {@link javax.swing.event.EventListenerList EventListenerList} 实现类似的逻辑。请参阅它以获取详细信息。
 *
 * @see javax.swing.event.EventListenerList
 *
 * @author      John Rose
 * @author      Amy Fowler
 * @since       1.1
 */

public class AWTEventMulticaster implements
    ComponentListener, ContainerListener, FocusListener, KeyListener,
    MouseListener, MouseMotionListener, WindowListener, WindowFocusListener,
    WindowStateListener, ActionListener, ItemListener, AdjustmentListener,
    TextListener, InputMethodListener, HierarchyListener,
    HierarchyBoundsListener, MouseWheelListener {

    protected final EventListener a, b;

    /**
     * 创建一个事件多播实例，将监听器-a 与监听器-b 链接在一起。输入参数 <code>a</code> 和 <code>b</code>
     * 不应为 <code>null</code>，尽管实现可能有所不同，是否在该情况下抛出 <code>NullPointerException</code>。
     * @param a 监听器-a
     * @param b 监听器-b
     */
    protected AWTEventMulticaster(EventListener a, EventListener b) {
        this.a = a; this.b = b;
    }

    /**
     * 从多播器中移除一个监听器。
     * <p>
     * 返回的多播器包含此多播器中的所有监听器，但不包括所有出现的 {@code oldl}。
     * 如果结果多播器仅包含一个普通监听器，则可以返回该普通监听器。如果结果多播器为空，则可以返回 {@code null}。
     * <p>
     * 如果 {@code oldl} 是 {@code null}，则不会抛出异常。
     *
     * @param oldl 要移除的监听器
     * @return 结果监听器
     */
    protected EventListener remove(EventListener oldl) {
        if (oldl == a)  return b;
        if (oldl == b)  return a;
        EventListener a2 = removeInternal(a, oldl);
        EventListener b2 = removeInternal(b, oldl);
        if (a2 == a && b2 == b) {
            return this;        // 它不在这里
        }
        return addInternal(a2, b2);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 componentResized 方法处理 componentResized 事件。
     * @param e 组件事件
     */
    public void componentResized(ComponentEvent e) {
        ((ComponentListener)a).componentResized(e);
        ((ComponentListener)b).componentResized(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 componentMoved 方法处理 componentMoved 事件。
     * @param e 组件事件
     */
    public void componentMoved(ComponentEvent e) {
        ((ComponentListener)a).componentMoved(e);
        ((ComponentListener)b).componentMoved(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 componentShown 方法处理 componentShown 事件。
     * @param e 组件事件
     */
    public void componentShown(ComponentEvent e) {
        ((ComponentListener)a).componentShown(e);
        ((ComponentListener)b).componentShown(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 componentHidden 方法处理 componentHidden 事件。
     * @param e 组件事件
     */
    public void componentHidden(ComponentEvent e) {
        ((ComponentListener)a).componentHidden(e);
        ((ComponentListener)b).componentHidden(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 componentAdded 方法处理 componentAdded 容器事件。
     * @param e 组件事件
     */
    public void componentAdded(ContainerEvent e) {
        ((ContainerListener)a).componentAdded(e);
        ((ContainerListener)b).componentAdded(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 componentRemoved 方法处理 componentRemoved 容器事件。
     * @param e 组件事件
     */
    public void componentRemoved(ContainerEvent e) {
        ((ContainerListener)a).componentRemoved(e);
        ((ContainerListener)b).componentRemoved(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 focusGained 方法处理 focusGained 事件。
     * @param e 焦点事件
     */
    public void focusGained(FocusEvent e) {
        ((FocusListener)a).focusGained(e);
        ((FocusListener)b).focusGained(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 focusLost 方法处理 focusLost 事件。
     * @param e 焦点事件
     */
    public void focusLost(FocusEvent e) {
        ((FocusListener)a).focusLost(e);
        ((FocusListener)b).focusLost(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 keyTyped 方法处理 keyTyped 事件。
     * @param e 键盘事件
     */
    public void keyTyped(KeyEvent e) {
        ((KeyListener)a).keyTyped(e);
        ((KeyListener)b).keyTyped(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 keyPressed 方法处理 keyPressed 事件。
     * @param e 键盘事件
     */
    public void keyPressed(KeyEvent e) {
        ((KeyListener)a).keyPressed(e);
        ((KeyListener)b).keyPressed(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 keyReleased 方法处理 keyReleased 事件。
     * @param e 键盘事件
     */
    public void keyReleased(KeyEvent e) {
        ((KeyListener)a).keyReleased(e);
        ((KeyListener)b).keyReleased(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 mouseClicked 方法处理 mouseClicked 事件。
     * @param e 鼠标事件
     */
    public void mouseClicked(MouseEvent e) {
        ((MouseListener)a).mouseClicked(e);
        ((MouseListener)b).mouseClicked(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 mousePressed 方法处理 mousePressed 事件。
     * @param e 鼠标事件
     */
    public void mousePressed(MouseEvent e) {
        ((MouseListener)a).mousePressed(e);
        ((MouseListener)b).mousePressed(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 mouseReleased 方法处理 mouseReleased 事件。
     * @param e 鼠标事件
     */
    public void mouseReleased(MouseEvent e) {
        ((MouseListener)a).mouseReleased(e);
        ((MouseListener)b).mouseReleased(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 mouseEntered 方法处理 mouseEntered 事件。
     * @param e 鼠标事件
     */
    public void mouseEntered(MouseEvent e) {
        ((MouseListener)a).mouseEntered(e);
        ((MouseListener)b).mouseEntered(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 mouseExited 方法处理 mouseExited 事件。
     * @param e 鼠标事件
     */
    public void mouseExited(MouseEvent e) {
        ((MouseListener)a).mouseExited(e);
        ((MouseListener)b).mouseExited(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 mouseDragged 方法处理 mouseDragged 事件。
     * @param e 鼠标事件
     */
    public void mouseDragged(MouseEvent e) {
        ((MouseMotionListener)a).mouseDragged(e);
        ((MouseMotionListener)b).mouseDragged(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 mouseMoved 方法处理 mouseMoved 事件。
     * @param e 鼠标事件
     */
    public void mouseMoved(MouseEvent e) {
        ((MouseMotionListener)a).mouseMoved(e);
        ((MouseMotionListener)b).mouseMoved(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 windowOpened 方法处理 windowOpened 事件。
     * @param e 窗口事件
     */
    public void windowOpened(WindowEvent e) {
        ((WindowListener)a).windowOpened(e);
        ((WindowListener)b).windowOpened(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 windowClosing 方法处理 windowClosing 事件。
     * @param e 窗口事件
     */
    public void windowClosing(WindowEvent e) {
        ((WindowListener)a).windowClosing(e);
        ((WindowListener)b).windowClosing(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 windowClosed 方法处理 windowClosed 事件。
     * @param e 窗口事件
     */
    public void windowClosed(WindowEvent e) {
        ((WindowListener)a).windowClosed(e);
        ((WindowListener)b).windowClosed(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 windowIconified 方法处理 windowIconified 事件。
     * @param e 窗口事件
     */
    public void windowIconified(WindowEvent e) {
        ((WindowListener)a).windowIconified(e);
        ((WindowListener)b).windowIconified(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 windowDeiconified 方法处理 windowDeiconified 事件。
     * @param e 窗口事件
     */
    public void windowDeiconified(WindowEvent e) {
        ((WindowListener)a).windowDeiconified(e);
        ((WindowListener)b).windowDeiconified(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 windowActivated 方法处理 windowActivated 事件。
     * @param e 窗口事件
     */
    public void windowActivated(WindowEvent e) {
        ((WindowListener)a).windowActivated(e);
        ((WindowListener)b).windowActivated(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 windowDeactivated 方法处理 windowDeactivated 事件。
     * @param e 窗口事件
     */
    public void windowDeactivated(WindowEvent e) {
        ((WindowListener)a).windowDeactivated(e);
        ((WindowListener)b).windowDeactivated(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 windowStateChanged 方法处理 windowStateChanged 事件。
     * @param e 窗口事件
     * @since 1.4
     */
    public void windowStateChanged(WindowEvent e) {
        ((WindowStateListener)a).windowStateChanged(e);
        ((WindowStateListener)b).windowStateChanged(e);
    }


    /**
     * 通过调用监听器-a 和监听器-b 的 windowGainedFocus 方法处理 windowGainedFocus 事件。
     * @param e 窗口事件
     * @since 1.4
     */
    public void windowGainedFocus(WindowEvent e) {
        ((WindowFocusListener)a).windowGainedFocus(e);
        ((WindowFocusListener)b).windowGainedFocus(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 windowLostFocus 方法处理 windowLostFocus 事件。
     * @param e 窗口事件
     * @since 1.4
     */
    public void windowLostFocus(WindowEvent e) {
        ((WindowFocusListener)a).windowLostFocus(e);
        ((WindowFocusListener)b).windowLostFocus(e);
    }

    /**
     * 通过调用监听器-a 和监听器-b 的 actionPerformed 方法处理 actionPerformed 事件。
     * @param e 动作事件
     */
    public void actionPerformed(ActionEvent e) {
        ((ActionListener)a).actionPerformed(e);
        ((ActionListener)b).actionPerformed(e);
    }


                /**
     * 处理 itemStateChanged 事件，通过调用 listener-a 和 listener-b 的
     * itemStateChanged 方法。
     * @param e 项目事件
     */
    public void itemStateChanged(ItemEvent e) {
        ((ItemListener)a).itemStateChanged(e);
        ((ItemListener)b).itemStateChanged(e);
    }

    /**
     * 处理 adjustmentValueChanged 事件，通过调用 listener-a 和 listener-b 的
     * adjustmentValueChanged 方法。
     * @param e 调整事件
     */
    public void adjustmentValueChanged(AdjustmentEvent e) {
        ((AdjustmentListener)a).adjustmentValueChanged(e);
        ((AdjustmentListener)b).adjustmentValueChanged(e);
    }
    public void textValueChanged(TextEvent e) {
        ((TextListener)a).textValueChanged(e);
        ((TextListener)b).textValueChanged(e);
    }

    /**
     * 处理 inputMethodTextChanged 事件，通过调用 listener-a 和 listener-b 的
     * inputMethodTextChanged 方法。
     * @param e 输入方法事件
     */
    public void inputMethodTextChanged(InputMethodEvent e) {
       ((InputMethodListener)a).inputMethodTextChanged(e);
       ((InputMethodListener)b).inputMethodTextChanged(e);
    }

    /**
     * 处理 caretPositionChanged 事件，通过调用 listener-a 和 listener-b 的
     * caretPositionChanged 方法。
     * @param e 输入方法事件
     */
    public void caretPositionChanged(InputMethodEvent e) {
       ((InputMethodListener)a).caretPositionChanged(e);
       ((InputMethodListener)b).caretPositionChanged(e);
    }

    /**
     * 处理 hierarchyChanged 事件，通过调用 listener-a 和 listener-b 的
     * hierarchyChanged 方法。
     * @param e 层次结构事件
     * @since 1.3
     */
    public void hierarchyChanged(HierarchyEvent e) {
        ((HierarchyListener)a).hierarchyChanged(e);
        ((HierarchyListener)b).hierarchyChanged(e);
    }

    /**
     * 处理 ancestorMoved 事件，通过调用 listener-a 和 listener-b 的
     * ancestorMoved 方法。
     * @param e 层次结构事件
     * @since 1.3
     */
    public void ancestorMoved(HierarchyEvent e) {
        ((HierarchyBoundsListener)a).ancestorMoved(e);
        ((HierarchyBoundsListener)b).ancestorMoved(e);
    }

    /**
     * 处理 ancestorResized 事件，通过调用 listener-a 和 listener-b 的
     * ancestorResized 方法。
     * @param e 层次结构事件
     * @since 1.3
     */
    public void ancestorResized(HierarchyEvent e) {
        ((HierarchyBoundsListener)a).ancestorResized(e);
        ((HierarchyBoundsListener)b).ancestorResized(e);
    }

    /**
     * 处理 mouseWheelMoved 事件，通过调用 listener-a 和 listener-b 的
     * mouseWheelMoved 方法。
     * @param e 鼠标事件
     * @since 1.4
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        ((MouseWheelListener)a).mouseWheelMoved(e);
        ((MouseWheelListener)b).mouseWheelMoved(e);
    }

    /**
     * 将 component-listener-a 与 component-listener-b 组合，并返回组合后的多播监听器。
     * @param a component-listener-a
     * @param b component-listener-b
     */
    public static ComponentListener add(ComponentListener a, ComponentListener b) {
        return (ComponentListener)addInternal(a, b);
    }

    /**
     * 将 container-listener-a 与 container-listener-b 组合，并返回组合后的多播监听器。
     * @param a container-listener-a
     * @param b container-listener-b
     */
    public static ContainerListener add(ContainerListener a, ContainerListener b) {
        return (ContainerListener)addInternal(a, b);
    }

    /**
     * 将 focus-listener-a 与 focus-listener-b 组合，并返回组合后的多播监听器。
     * @param a focus-listener-a
     * @param b focus-listener-b
     */
    public static FocusListener add(FocusListener a, FocusListener b) {
        return (FocusListener)addInternal(a, b);
    }

    /**
     * 将 key-listener-a 与 key-listener-b 组合，并返回组合后的多播监听器。
     * @param a key-listener-a
     * @param b key-listener-b
     */
    public static KeyListener add(KeyListener a, KeyListener b) {
        return (KeyListener)addInternal(a, b);
    }

    /**
     * 将 mouse-listener-a 与 mouse-listener-b 组合，并返回组合后的多播监听器。
     * @param a mouse-listener-a
     * @param b mouse-listener-b
     */
    public static MouseListener add(MouseListener a, MouseListener b) {
        return (MouseListener)addInternal(a, b);
    }

    /**
     * 将 mouse-motion-listener-a 与 mouse-motion-listener-b 组合，并返回组合后的多播监听器。
     * @param a mouse-motion-listener-a
     * @param b mouse-motion-listener-b
     */
    public static MouseMotionListener add(MouseMotionListener a, MouseMotionListener b) {
        return (MouseMotionListener)addInternal(a, b);
    }

    /**
     * 将 window-listener-a 与 window-listener-b 组合，并返回组合后的多播监听器。
     * @param a window-listener-a
     * @param b window-listener-b
     */
    public static WindowListener add(WindowListener a, WindowListener b) {
        return (WindowListener)addInternal(a, b);
    }

    /**
     * 将 window-state-listener-a 与 window-state-listener-b 组合，并返回组合后的多播监听器。
     * @param a window-state-listener-a
     * @param b window-state-listener-b
     * @since 1.4
     */
    public static WindowStateListener add(WindowStateListener a,
                                          WindowStateListener b) {
        return (WindowStateListener)addInternal(a, b);
    }

    /**
     * 将 window-focus-listener-a 与 window-focus-listener-b 组合，并返回组合后的多播监听器。
     * @param a window-focus-listener-a
     * @param b window-focus-listener-b
     * @since 1.4
     */
    public static WindowFocusListener add(WindowFocusListener a,
                                          WindowFocusListener b) {
        return (WindowFocusListener)addInternal(a, b);
    }

    /**
     * 将 action-listener-a 与 action-listener-b 组合，并返回组合后的多播监听器。
     * @param a action-listener-a
     * @param b action-listener-b
     */
    public static ActionListener add(ActionListener a, ActionListener b) {
        return (ActionListener)addInternal(a, b);
    }

    /**
     * 将 item-listener-a 与 item-listener-b 组合，并返回组合后的多播监听器。
     * @param a item-listener-a
     * @param b item-listener-b
     */
    public static ItemListener add(ItemListener a, ItemListener b) {
        return (ItemListener)addInternal(a, b);
    }

    /**
     * 将 adjustment-listener-a 与 adjustment-listener-b 组合，并返回组合后的多播监听器。
     * @param a adjustment-listener-a
     * @param b adjustment-listener-b
     */
    public static AdjustmentListener add(AdjustmentListener a, AdjustmentListener b) {
        return (AdjustmentListener)addInternal(a, b);
    }
    public static TextListener add(TextListener a, TextListener b) {
        return (TextListener)addInternal(a, b);
    }

    /**
     * 将 input-method-listener-a 与 input-method-listener-b 组合，并返回组合后的多播监听器。
     * @param a input-method-listener-a
     * @param b input-method-listener-b
     */
     public static InputMethodListener add(InputMethodListener a, InputMethodListener b) {
        return (InputMethodListener)addInternal(a, b);
     }

    /**
     * 将 hierarchy-listener-a 与 hierarchy-listener-b 组合，并返回组合后的多播监听器。
     * @param a hierarchy-listener-a
     * @param b hierarchy-listener-b
     * @since 1.3
     */
     public static HierarchyListener add(HierarchyListener a, HierarchyListener b) {
        return (HierarchyListener)addInternal(a, b);
     }

    /**
     * 将 hierarchy-bounds-listener-a 与 hierarchy-bounds-listener-b 组合，并返回组合后的多播监听器。
     * @param a hierarchy-bounds-listener-a
     * @param b hierarchy-bounds-listener-b
     * @since 1.3
     */
     public static HierarchyBoundsListener add(HierarchyBoundsListener a, HierarchyBoundsListener b) {
        return (HierarchyBoundsListener)addInternal(a, b);
     }

    /**
     * 将 mouse-wheel-listener-a 与 mouse-wheel-listener-b 组合，并返回组合后的多播监听器。
     * @param a mouse-wheel-listener-a
     * @param b mouse-wheel-listener-b
     * @since 1.4
     */
    public static MouseWheelListener add(MouseWheelListener a,
                                         MouseWheelListener b) {
        return (MouseWheelListener)addInternal(a, b);
    }

    /**
     * 从 component-listener-l 中移除旧的 component-listener，并返回组合后的多播监听器。
     * @param l component-listener-l
     * @param oldl 被移除的 component-listener
     */
    public static ComponentListener remove(ComponentListener l, ComponentListener oldl) {
        return (ComponentListener) removeInternal(l, oldl);
    }

    /**
     * 从 container-listener-l 中移除旧的 container-listener，并返回组合后的多播监听器。
     * @param l container-listener-l
     * @param oldl 被移除的 container-listener
     */
    public static ContainerListener remove(ContainerListener l, ContainerListener oldl) {
        return (ContainerListener) removeInternal(l, oldl);
    }

    /**
     * 从 focus-listener-l 中移除旧的 focus-listener，并返回组合后的多播监听器。
     * @param l focus-listener-l
     * @param oldl 被移除的 focus-listener
     */
    public static FocusListener remove(FocusListener l, FocusListener oldl) {
        return (FocusListener) removeInternal(l, oldl);
    }

    /**
     * 从 key-listener-l 中移除旧的 key-listener，并返回组合后的多播监听器。
     * @param l key-listener-l
     * @param oldl 被移除的 key-listener
     */
    public static KeyListener remove(KeyListener l, KeyListener oldl) {
        return (KeyListener) removeInternal(l, oldl);
    }

    /**
     * 从 mouse-listener-l 中移除旧的 mouse-listener，并返回组合后的多播监听器。
     * @param l mouse-listener-l
     * @param oldl 被移除的 mouse-listener
     */
    public static MouseListener remove(MouseListener l, MouseListener oldl) {
        return (MouseListener) removeInternal(l, oldl);
    }

    /**
     * 从 mouse-motion-listener-l 中移除旧的 mouse-motion-listener，并返回组合后的多播监听器。
     * @param l mouse-motion-listener-l
     * @param oldl 被移除的 mouse-motion-listener
     */
    public static MouseMotionListener remove(MouseMotionListener l, MouseMotionListener oldl) {
        return (MouseMotionListener) removeInternal(l, oldl);
    }

    /**
     * 从 window-listener-l 中移除旧的 window-listener，并返回组合后的多播监听器。
     * @param l window-listener-l
     * @param oldl 被移除的 window-listener
     */
    public static WindowListener remove(WindowListener l, WindowListener oldl) {
        return (WindowListener) removeInternal(l, oldl);
    }

    /**
     * 从 window-state-listener-l 中移除旧的 window-state-listener，并返回组合后的多播监听器。
     * @param l window-state-listener-l
     * @param oldl 被移除的 window-state-listener
     * @since 1.4
     */
    public static WindowStateListener remove(WindowStateListener l,
                                             WindowStateListener oldl) {
        return (WindowStateListener) removeInternal(l, oldl);
    }

    /**
     * 从 window-focus-listener-l 中移除旧的 window-focus-listener，并返回组合后的多播监听器。
     * @param l window-focus-listener-l
     * @param oldl 被移除的 window-focus-listener
     * @since 1.4
     */
    public static WindowFocusListener remove(WindowFocusListener l,
                                             WindowFocusListener oldl) {
        return (WindowFocusListener) removeInternal(l, oldl);
    }

    /**
     * 从 action-listener-l 中移除旧的 action-listener，并返回组合后的多播监听器。
     * @param l action-listener-l
     * @param oldl 被移除的 action-listener
     */
    public static ActionListener remove(ActionListener l, ActionListener oldl) {
        return (ActionListener) removeInternal(l, oldl);
    }

    /**
     * 从 item-listener-l 中移除旧的 item-listener，并返回组合后的多播监听器。
     * @param l item-listener-l
     * @param oldl 被移除的 item-listener
     */
    public static ItemListener remove(ItemListener l, ItemListener oldl) {
        return (ItemListener) removeInternal(l, oldl);
    }

    /**
     * 从 adjustment-listener-l 中移除旧的 adjustment-listener，并返回组合后的多播监听器。
     * @param l adjustment-listener-l
     * @param oldl 被移除的 adjustment-listener
     */
    public static AdjustmentListener remove(AdjustmentListener l, AdjustmentListener oldl) {
        return (AdjustmentListener) removeInternal(l, oldl);
    }
    public static TextListener remove(TextListener l, TextListener oldl) {
        return (TextListener) removeInternal(l, oldl);
    }

    /**
     * 从 input-method-listener-l 中移除旧的 input-method-listener，并返回组合后的多播监听器。
     * @param l input-method-listener-l
     * @param oldl 被移除的 input-method-listener
     */
    public static InputMethodListener remove(InputMethodListener l, InputMethodListener oldl) {
        return (InputMethodListener) removeInternal(l, oldl);
    }

    /**
     * 从 hierarchy-listener-l 中移除旧的 hierarchy-listener，并返回组合后的多播监听器。
     * @param l hierarchy-listener-l
     * @param oldl 被移除的 hierarchy-listener
     * @since 1.3
     */
    public static HierarchyListener remove(HierarchyListener l, HierarchyListener oldl) {
        return (HierarchyListener) removeInternal(l, oldl);
    }

    /**
     * 从 hierarchy-bounds-listener-l 中移除旧的 hierarchy-bounds-listener，并返回组合后的多播监听器。
     * @param l hierarchy-bounds-listener-l
     * @param oldl 被移除的 hierarchy-bounds-listener
     * @since 1.3
     */
    public static HierarchyBoundsListener remove(HierarchyBoundsListener l, HierarchyBoundsListener oldl) {
        return (HierarchyBoundsListener) removeInternal(l, oldl);
    }

    /**
     * 从 mouse-wheel-listener-l 中移除旧的 mouse-wheel-listener，并返回组合后的多播监听器。
     * @param l mouse-wheel-listener-l
     * @param oldl 被移除的 mouse-wheel-listener
     * @since 1.4
     */
    public static MouseWheelListener remove(MouseWheelListener l,
                                            MouseWheelListener oldl) {
      return (MouseWheelListener) removeInternal(l, oldl);
    }

    /**
     * 将 listener-a 和 listener-b 组合在一起，返回组合后的多播监听器。
     * 如果 listener-a 为 null，则返回 listener-b；
     * 如果 listener-b 为 null，则返回 listener-a；
     * 如果两者都不为 null，则创建并返回一个新的 AWTEventMulticaster 实例，将 a 与 b 链接。
     * @param a 事件监听器-a
     * @param b 事件监听器-b
     */
    protected static EventListener addInternal(EventListener a, EventListener b) {
        if (a == null)  return b;
        if (b == null)  return a;
        return new AWTEventMulticaster(a, b);
    }


                /**
     * 返回从 listener-l 中移除旧监听器后的多播监听器。
     * 如果 listener-l 等于旧监听器或 listener-l 为 null，
     * 返回 null。
     * 否则，如果 listener-l 是 AWTEventMulticaster 的实例，
     * 则从其中移除旧监听器。
     * 否则，返回 listener l。
     * @param l 被移除监听器的监听器
     * @param oldl 被移除的监听器
     */
    protected static EventListener removeInternal(EventListener l, EventListener oldl) {
        if (l == oldl || l == null) {
            return null;
        } else if (l instanceof AWTEventMulticaster) {
            return ((AWTEventMulticaster)l).remove(oldl);
        } else {
            return l;           // it's not here
        }
    }


    /* 序列化支持。
     */

    protected void saveInternal(ObjectOutputStream s, String k) throws IOException {
        if (a instanceof AWTEventMulticaster) {
            ((AWTEventMulticaster)a).saveInternal(s, k);
        }
        else if (a instanceof Serializable) {
            s.writeObject(k);
            s.writeObject(a);
        }

        if (b instanceof AWTEventMulticaster) {
            ((AWTEventMulticaster)b).saveInternal(s, k);
        }
        else if (b instanceof Serializable) {
            s.writeObject(k);
            s.writeObject(b);
        }
    }

    protected static void save(ObjectOutputStream s, String k, EventListener l) throws IOException {
      if (l == null) {
          return;
      }
      else if (l instanceof AWTEventMulticaster) {
          ((AWTEventMulticaster)l).saveInternal(s, k);
      }
      else if (l instanceof Serializable) {
           s.writeObject(k);
           s.writeObject(l);
      }
    }

    /*
     * 递归方法，返回 EventListener 中的监听器数量，处理 l 实际上是
     * AWTEventMulticaster 的常见情况。此外，仅计算 listenerType 类型的监听器。
     * 修改此方法以修复 bug 4513402。 -bchristi
     */
    private static int getListenerCount(EventListener l, Class<?> listenerType) {
        if (l instanceof AWTEventMulticaster) {
            AWTEventMulticaster mc = (AWTEventMulticaster)l;
            return getListenerCount(mc.a, listenerType) +
             getListenerCount(mc.b, listenerType);
        }
        else {
            // 仅计算正确类型的监听器
            return listenerType.isInstance(l) ? 1 : 0;
        }
    }

    /*
     * 递归方法，使用 l 中的 EventListeners 填充 EventListener 数组 a。
     * l 通常是 AWTEventMulticaster。Bug 4513402 显示，如果 l 与 a 的元素类型不同，
     * 将发生 ArrayStoreException。现在只有当 l 是适当类型时，才会将其插入 a 中。 -bchristi
     */
    private static int populateListenerArray(EventListener[] a, EventListener l, int index) {
        if (l instanceof AWTEventMulticaster) {
            AWTEventMulticaster mc = (AWTEventMulticaster)l;
            int lhs = populateListenerArray(a, mc.a, index);
            return populateListenerArray(a, mc.b, lhs);
        }
        else if (a.getClass().getComponentType().isInstance(l)) {
            a[index] = l;
            return index + 1;
        }
        // 跳过 null 和错误类的实例
        else {
            return index;
        }
    }

    /**
     * 返回由指定的 <code>java.util.EventListener</code> 链接的所有
     * <code><em>Foo</em>Listener</code> 对象的数组。
     * <code><em>Foo</em>Listener</code> 通过 <code>AWTEventMulticaster</code>
     * 使用 <code>add<em>Foo</em>Listener</code> 方法链接。
     * 如果指定了 null 监听器，此方法返回一个空数组。如果指定的监听器不是
     * <code>AWTEventMulticaster</code> 的实例，此方法返回一个仅包含指定监听器的数组。
     * 如果没有链接此类监听器，此方法返回一个空数组。
     *
     * @param l 指定的 <code>java.util.EventListener</code>
     * @param listenerType 请求的监听器类型；此参数应指定一个继承自
     *          <code>java.util.EventListener</code> 的接口
     * @return 由指定多播监听器链接的所有
     *          <code><em>Foo</em>Listener</code> 对象的数组，或如果指定的多播监听器
     *          没有链接此类监听器，则返回一个空数组
     * @exception NullPointerException 如果指定的
     *             {@code listenertype} 参数为 {@code null}
     * @exception ClassCastException 如果 <code>listenerType</code>
     *          没有指定实现 <code>java.util.EventListener</code> 的类或接口
     *
     * @since 1.4
     */
    @SuppressWarnings("unchecked")
    public static <T extends EventListener> T[]
        getListeners(EventListener l, Class<T> listenerType)
    {
        if (listenerType == null) {
            throw new NullPointerException ("Listener type should not be null");
        }

        int n = getListenerCount(l, listenerType);
        T[] result = (T[])Array.newInstance(listenerType, n);
        populateListenerArray(result, l, 0);
        return result;
    }
}
