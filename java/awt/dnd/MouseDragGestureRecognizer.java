/*
 * Copyright (c) 1998, 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.dnd;

import java.awt.Component;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * 此抽象子类定义了基于鼠标的手势的 <code>DragGestureRecognizer</code>。
 *
 * 每个平台实现自己的此类的具体子类，通过 Toolkit.createDragGestureRecognizer() 方法提供，
 * 以封装平台依赖的鼠标手势，这些手势会触发拖放操作。
 * <p>
 * 鼠标拖动手势识别器应遵守通过 {@link DragSource#getDragThreshold} 获取的拖动手势移动阈值。
 * 只有当最新鼠标拖动事件的位置与相应鼠标按钮按下事件的位置之间的水平或垂直距离大于拖动手势移动阈值时，
 * 才应识别拖动手势。
 * <p>
 * 使用 {@link DragSource#createDefaultDragGestureRecognizer} 创建的拖动手势识别器遵循此约定。
 *
 * @author Laurence P. G. Cable
 *
 * @see java.awt.dnd.DragGestureListener
 * @see java.awt.dnd.DragGestureEvent
 * @see java.awt.dnd.DragSource
 */

public abstract class MouseDragGestureRecognizer extends DragGestureRecognizer implements MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 6220099344182281120L;

    /**
     * 给定 <code>DragSource</code>、要观察的 <code>Component</code>、此拖放操作允许的操作以及
     * 当检测到拖动手势时要通知的 <code>DragGestureListener</code>，构造一个新的 <code>MouseDragGestureRecognizer</code>。
     * <P>
     * @param ds  组件 c 的 DragSource
     * @param c   要观察的组件
     * @param act 此拖放操作允许的操作
     * @param dgl 检测到手势时要通知的 DragGestureListener
     *
     */

    protected MouseDragGestureRecognizer(DragSource ds, Component c, int act, DragGestureListener dgl) {
        super(ds, c, act, dgl);
    }

    /**
     * 给定 <code>DragSource</code>、要观察的 <code>Component</code> 以及此拖放操作允许的操作，
     * 构造一个新的 <code>MouseDragGestureRecognizer</code>。
     * <P>
     * @param ds  组件 c 的 DragSource
     * @param c   要观察的组件
     * @param act 此拖放操作允许的操作
     */

    protected MouseDragGestureRecognizer(DragSource ds, Component c, int act) {
        this(ds, c, act, null);
    }

    /**
     * 给定 <code>DragSource</code> 和要观察的 <code>Component</code>，
     * 构造一个新的 <code>MouseDragGestureRecognizer</code>。
     * <P>
     * @param ds  组件 c 的 DragSource
     * @param c   要观察的组件
     */

    protected MouseDragGestureRecognizer(DragSource ds, Component c) {
        this(ds, c, DnDConstants.ACTION_NONE);
    }

    /**
     * 给定 <code>DragSource</code>，构造一个新的 <code>MouseDragGestureRecognizer</code>。
     * <P>
     * @param ds  组件的 DragSource
     */

    protected MouseDragGestureRecognizer(DragSource ds) {
        this(ds, null);
    }

    /**
     * 在组件上注册此 DragGestureRecognizer 的监听器
     */

    protected void registerListeners() {
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
    }

    /**
     * 在组件上注销此 DragGestureRecognizer 的监听器
     *
     * 子类必须覆盖此方法
     */


    protected void unregisterListeners() {
        component.removeMouseListener(this);
        component.removeMouseMotionListener(this);
    }

    /**
     * 当鼠标在组件上被点击时调用。
     * <P>
     * @param e MouseEvent
     */

    public void mouseClicked(MouseEvent e) { }

    /**
     * 当鼠标按钮在 <code>Component</code> 上被按下时调用。
     * <P>
     * @param e MouseEvent
     */

    public void mousePressed(MouseEvent e) { }

    /**
     * 当鼠标按钮在组件上被释放时调用。
     * <P>
     * @param e MouseEvent
     */

    public void mouseReleased(MouseEvent e) { }

    /**
     * 当鼠标进入组件时调用。
     * <P>
     * @param e MouseEvent
     */

    public void mouseEntered(MouseEvent e) { }

    /**
     * 当鼠标离开组件时调用。
     * <P>
     * @param e MouseEvent
     */

    public void mouseExited(MouseEvent e) { }

    /**
     * 当鼠标按钮在组件上被按下时调用。
     * <P>
     * @param e MouseEvent
     */

    public void mouseDragged(MouseEvent e) { }

    /**
     * 当鼠标在组件上移动（没有按钮按下）时调用。
     * <P>
     * @param e MouseEvent
     */

    public void mouseMoved(MouseEvent e) { }
}
