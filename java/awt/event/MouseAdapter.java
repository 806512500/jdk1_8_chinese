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
 * 一个用于接收鼠标事件的抽象适配器类。
 * 该类中的方法都是空的。该类的存在是为了方便创建监听器对象。
 * <P>
 * 鼠标事件可以让你跟踪鼠标按键的按下、释放、点击、移动、拖动、进入组件、离开组件以及鼠标滚轮的滚动。
 * <P>
 * 继承此类以创建 {@code MouseEvent}（包括拖动和移动事件）或/和 {@code MouseWheelEvent}
 * 监听器，并覆盖感兴趣的事件的方法。（如果你实现了
 * {@code MouseListener}，
 * {@code MouseMotionListener}
 * 接口，你必须定义接口中的所有方法。这个抽象类为所有这些方法定义了空方法，
 * 因此你只需要定义你关心的事件的方法。）
 * <P>
 * 使用扩展的类创建一个监听器对象，然后使用组件的 {@code addMouseListener}
 * {@code addMouseMotionListener}，{@code addMouseWheelListener}
 * 方法注册它。
 * 在以下情况下，监听器对象中的相关方法将被调用，并将 {@code MouseEvent}
 * 或 {@code MouseWheelEvent} 传递给它：
 * <ul>
 * <li>当鼠标按键被按下、释放或点击（按下并释放）时
 * <li>当鼠标指针进入或离开组件时
 * <li>当鼠标滚轮旋转，或鼠标移动或拖动时
 * </ul>
 *
 * @author Carl Quinn
 * @author Andrei Dmitriev
 *
 * @see MouseEvent
 * @see MouseWheelEvent
 * @see MouseListener
 * @see MouseMotionListener
 * @see MouseWheelListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/mouselistener.html">教程：编写鼠标监听器</a>
 *
 * @since 1.1
 */
public abstract class MouseAdapter implements MouseListener, MouseWheelListener, MouseMotionListener {
    /**
     * {@inheritDoc}
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * {@inheritDoc}
     */
    public void mousePressed(MouseEvent e) {}

    /**
     * {@inheritDoc}
     */
    public void mouseReleased(MouseEvent e) {}

    /**
     * {@inheritDoc}
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * {@inheritDoc}
     */
    public void mouseExited(MouseEvent e) {}

    /**
     * {@inheritDoc}
     * @since 1.6
     */
    public void mouseWheelMoved(MouseWheelEvent e){}

    /**
     * {@inheritDoc}
     * @since 1.6
     */
    public void mouseDragged(MouseEvent e){}

    /**
     * {@inheritDoc}
     * @since 1.6
     */
    public void mouseMoved(MouseEvent e){}
}
