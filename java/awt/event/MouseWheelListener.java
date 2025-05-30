/*
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
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
 * 用于接收组件上的鼠标滚轮事件的监听器接口。
 * （对于点击和其他鼠标事件，请使用 <code>MouseListener</code>。
 * 对于鼠标移动和拖动，请使用 <code>MouseMotionListener</code>。）
 * <P>
 * 对鼠标滚轮事件感兴趣的类实现此接口（及其包含的所有方法）。
 * <P>
 * 从该类创建的监听器对象然后使用组件的 <code>addMouseWheelListener</code>
 * 方法注册到组件。当鼠标滚轮被旋转时，会生成鼠标滚轮事件。
 * 当鼠标滚轮事件发生时，该对象的 <code>mouseWheelMoved</code>
 * 方法将被调用。
 * <p>
 * 有关鼠标滚轮事件如何分派的信息，请参阅
 * {@link MouseWheelEvent} 的类描述。
 *
 * @author Brent Christian
 * @see MouseWheelEvent
 * @since 1.4
 */
public interface MouseWheelListener extends EventListener {

    /**
     * 当鼠标滚轮被旋转时调用。
     * @see MouseWheelEvent
     */
    public void mouseWheelMoved(MouseWheelEvent e);
}
