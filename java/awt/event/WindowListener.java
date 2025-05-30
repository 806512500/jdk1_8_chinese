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

import java.util.EventListener;

/**
 * 用于接收窗口事件的监听器接口。
 * 对窗口事件感兴趣的类要么实现此接口（及其包含的所有方法），
 * 要么扩展抽象的 <code>WindowAdapter</code> 类（仅重写感兴趣的方法）。
 * 从该类创建的监听器对象然后使用窗口的 <code>addWindowListener</code>
 * 方法注册。当窗口的状态因打开、关闭、激活或取消激活、最小化或恢复而改变时，
 * 相关的监听器对象中的方法将被调用，并将 <code>WindowEvent</code> 传递给它。
 *
 * @author Carl Quinn
 *
 * @see WindowAdapter
 * @see WindowEvent
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/windowlistener.html">教程：如何编写窗口监听器</a>
 *
 * @since 1.1
 */
public interface WindowListener extends EventListener {
    /**
     * 当窗口首次变为可见时调用。
     */
    public void windowOpened(WindowEvent e);

    /**
     * 当用户尝试从窗口的系统菜单关闭窗口时调用。
     */
    public void windowClosing(WindowEvent e);

    /**
     * 当窗口由于调用窗口的 dispose 方法而关闭时调用。
     */
    public void windowClosed(WindowEvent e);

    /**
     * 当窗口从正常状态更改为最小化状态时调用。对于许多平台，最小化窗口
     * 会显示为窗口的 iconImage 属性指定的图标。
     * @see java.awt.Frame#setIconImage
     */
    public void windowIconified(WindowEvent e);

    /**
     * 当窗口从最小化状态更改为正常状态时调用。
     */
    public void windowDeiconified(WindowEvent e);

    /**
     * 当窗口被设置为活动窗口时调用。只有 Frame 或 Dialog 可以是活动窗口。
     * 本机窗口系统可能会用特殊的装饰（如高亮的标题栏）表示活动窗口或其子窗口。
     * 活动窗口总是焦点窗口，或者是焦点窗口的第一个 Frame 或 Dialog 所有者。
     */
    public void windowActivated(WindowEvent e);

    /**
     * 当窗口不再是活动窗口时调用。只有 Frame 或 Dialog 可以是活动窗口。
     * 本机窗口系统可能会用特殊的装饰（如高亮的标题栏）表示活动窗口或其子窗口。
     * 活动窗口总是焦点窗口，或者是焦点窗口的第一个 Frame 或 Dialog 所有者。
     */
    public void windowDeactivated(WindowEvent e);
}
