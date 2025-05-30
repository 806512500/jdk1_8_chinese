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
 * 用于接收组件上“有趣的”鼠标事件（按下、释放、点击、进入和退出）的监听器接口。
 * （要跟踪鼠标移动和鼠标拖动，请使用 <code>MouseMotionListener</code>。）
 * <P>
 * 对鼠标事件感兴趣的类要么实现此接口（及其包含的所有方法），要么扩展抽象 <code>MouseAdapter</code> 类
 * （仅覆盖感兴趣的方法）。
 * <P>
 * 从该类创建的监听器对象然后使用组件的 <code>addMouseListener</code>
 * 方法注册到组件。当鼠标被按下、释放或点击（按下并释放）时，会生成鼠标事件。当鼠标指针进入或离开组件时，也会生成鼠标事件。当鼠标事件发生时，监听器对象中的相关方法将被调用，并将 <code>MouseEvent</code> 传递给它。
 *
 * @author Carl Quinn
 *
 * @see MouseAdapter
 * @see MouseEvent
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/mouselistener.html">教程：编写鼠标监听器</a>
 *
 * @since 1.1
 */
public interface MouseListener extends EventListener {

    /**
     * 当在组件上点击（按下并释放）鼠标按钮时调用。
     */
    public void mouseClicked(MouseEvent e);

    /**
     * 当在组件上按下鼠标按钮时调用。
     */
    public void mousePressed(MouseEvent e);

    /**
     * 当在组件上释放鼠标按钮时调用。
     */
    public void mouseReleased(MouseEvent e);

    /**
     * 当鼠标进入组件时调用。
     */
    public void mouseEntered(MouseEvent e);

    /**
     * 当鼠标离开组件时调用。
     */
    public void mouseExited(MouseEvent e);
}
