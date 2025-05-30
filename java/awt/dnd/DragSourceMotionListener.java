/*
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
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

import java.util.EventListener;

/**
 * 用于在拖动操作期间接收鼠标移动事件的监听器接口。
 * <p>
 * 对于在拖动操作期间处理鼠标移动事件感兴趣的类，要么实现此接口，要么扩展抽象
 * <code>DragSourceAdapter</code> 类（仅重写感兴趣的方法）。
 * <p>
 * 使用该类创建一个监听器对象，然后将其注册到
 * <code>DragSource</code>。每当使用此 <code>DragSource</code> 初始化的拖动操作期间鼠标移动时，
 * 该对象的 <code>dragMouseMoved</code> 方法将被调用，并传递
 * <code>DragSourceDragEvent</code>。
 *
 * @see DragSourceDragEvent
 * @see DragSource
 * @see DragSourceListener
 * @see DragSourceAdapter
 *
 * @since 1.4
 */

public interface DragSourceMotionListener extends EventListener {

    /**
     * 在拖动操作期间每当鼠标移动时调用。
     *
     * @param dsde <code>DragSourceDragEvent</code>
     */
    void dragMouseMoved(DragSourceDragEvent dsde);
}
