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
 * 用于接收组件上的鼠标移动事件的监听器接口。
 * （对于点击和其他鼠标事件，请使用 <code>MouseListener</code>。）
 * <P>
 * 对于感兴趣处理鼠标移动事件的类，可以实现此接口（及其所有方法）或扩展抽象类 <code>MouseMotionAdapter</code>
 * （仅重写感兴趣的那些方法）。
 * <P>
 * 从该类创建的监听器对象然后使用组件的 <code>addMouseMotionListener</code>
 * 方法注册到组件。当鼠标移动或拖动时，会生成鼠标移动事件（会生成许多这样的事件）。当发生鼠标移动事件时，
 * 监听器对象中的相关方法将被调用，并将 <code>MouseEvent</code> 传递给它。
 *
 * @author Amy Fowler
 *
 * @see MouseMotionAdapter
 * @see MouseEvent
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/mousemotionlistener.html">教程：编写鼠标移动监听器</a>
 *
 * @since 1.1
 */
public interface MouseMotionListener extends EventListener {

    /**
     * 当在组件上按下鼠标按钮并拖动时调用。 <code>MOUSE_DRAGGED</code> 事件将继续传递给拖动开始的组件，
     * 直到释放鼠标按钮（无论鼠标位置是否在组件边界内）。
     * <p>
     * 由于平台依赖的拖放实现，<code>MOUSE_DRAGGED</code> 事件可能在本机拖放操作期间不会传递。
     */
    public void mouseDragged(MouseEvent e);

    /**
     * 当鼠标光标移动到组件上但未按下任何按钮时调用。
     */
    public void mouseMoved(MouseEvent e);

}
