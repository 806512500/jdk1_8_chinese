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

package java.awt.event;

/**
 * 一个用于接收窗口事件的抽象适配器类。
 * 该类中的方法都是空的。该类的存在是为了方便创建监听器对象。
 * <P>
 * 扩展此类以创建一个 <code>WindowEvent</code> 监听器，并覆盖感兴趣的事件的方法。
 * （如果你实现 <code>WindowListener</code> 接口，你必须定义该接口中的所有方法。
 * 这个抽象类为所有方法定义了空方法，因此你只需定义你关心的事件的方法。）
 * <P>
 * 使用扩展类创建一个监听器对象，然后使用窗口的 <code>addWindowListener</code>
 * 方法将其注册到窗口。当窗口的状态由于打开、关闭、激活或去激活、最小化或恢复时发生变化，
 * 相关的监听器方法将被调用，并将 <code>WindowEvent</code> 传递给它。
 *
 * @see WindowEvent
 * @see WindowListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/windowlistener.html">教程：编写窗口监听器</a>
 *
 * @author Carl Quinn
 * @author Amy Fowler
 * @author David Mendenhall
 * @since 1.1
 */
public abstract class WindowAdapter
    implements WindowListener, WindowStateListener, WindowFocusListener
{
    /**
     * 当窗口被打开时调用。
     */
    public void windowOpened(WindowEvent e) {}

    /**
     * 当窗口正在被关闭时调用。
     * 可以在此处覆盖关闭操作。
     */
    public void windowClosing(WindowEvent e) {}

    /**
     * 当窗口已被关闭时调用。
     */
    public void windowClosed(WindowEvent e) {}

    /**
     * 当窗口被最小化时调用。
     */
    public void windowIconified(WindowEvent e) {}

    /**
     * 当窗口被恢复时调用。
     */
    public void windowDeiconified(WindowEvent e) {}

    /**
     * 当窗口被激活时调用。
     */
    public void windowActivated(WindowEvent e) {}

    /**
     * 当窗口被去激活时调用。
     */
    public void windowDeactivated(WindowEvent e) {}

    /**
     * 当窗口状态改变时调用。
     * @since 1.4
     */
    public void windowStateChanged(WindowEvent e) {}

    /**
     * 当窗口被设置为焦点窗口时调用，这意味着窗口或其一个子组件将接收键盘事件。
     *
     * @since 1.4
     */
    public void windowGainedFocus(WindowEvent e) {}

    /**
     * 当窗口不再是焦点窗口时调用，这意味着键盘事件将不再传递给窗口或其任何子组件。
     *
     * @since 1.4
     */
    public void windowLostFocus(WindowEvent e) {}
}
