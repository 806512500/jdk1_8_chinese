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
 * 用于接收鼠标移动事件的抽象适配器类。
 * 该类中的方法都是空的。该类的存在是为了方便创建监听器对象。
 * <P>
 * 鼠标移动事件发生在鼠标被移动或拖动时。
 * （在正常的程序中会生成许多这样的事件。要跟踪点击和其他鼠标事件，请使用 MouseAdapter。）
 * <P>
 * 扩展此类以创建 <code>MouseEvent</code> 监听器，并覆盖感兴趣的事件方法。（如果你实现了
 * <code>MouseMotionListener</code> 接口，你必须定义该接口中的所有方法。这个抽象类为所有方法
 * 定义了空方法，因此你只需要定义你关心的事件的方法。）
 * <P>
 * 使用扩展类创建监听器对象，然后使用组件的 <code>addMouseMotionListener</code>
 * 方法将其注册到组件。当鼠标被移动或拖动时，监听器对象中的相关方法将被调用，并将 <code>MouseEvent</code>
 * 传递给它。
 *
 * @author Amy Fowler
 *
 * @see MouseEvent
 * @see MouseMotionListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/mousemotionlistener.html">教程：编写鼠标移动监听器</a>
 *
 * @since 1.1
 */
public abstract class MouseMotionAdapter implements MouseMotionListener {
    /**
     * 当在组件上按下鼠标按钮并拖动时调用。
     * 鼠标拖动事件将继续传递给最初发生拖动的组件，直到鼠标按钮被释放
     * （无论鼠标位置是否在组件的边界内）。
     */
    public void mouseDragged(MouseEvent e) {}

    /**
     * 当鼠标按钮在组件上移动（没有按钮按下）时调用。
     */
    public void mouseMoved(MouseEvent e) {}
}
