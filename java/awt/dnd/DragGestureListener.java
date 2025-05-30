/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 用于接收拖动手势事件的监听器接口。
 * 该接口旨在用于拖动手势识别实现。有关如何注册监听器接口的详细信息，请参阅 {@code DragGestureRecognizer}
 * 的规范。当识别到拖动手势时，{@code DragGestureRecognizer} 会调用此接口的
 * {@link #dragGestureRecognized dragGestureRecognized()}
 * 方法，并传递一个 {@code DragGestureEvent}。

 *
 * @see java.awt.dnd.DragGestureRecognizer
 * @see java.awt.dnd.DragGestureEvent
 * @see java.awt.dnd.DragSource
 */

 public interface DragGestureListener extends EventListener {

    /**
     * 当 {@code DragGestureRecognizer} 检测到平台依赖的拖动启动手势时，此方法由 {@code DragGestureRecognizer}
     * 调用。为了启动拖放操作，如果合适，需要调用 {@code DragGestureEvent} 上的
     * {@link DragGestureEvent#startDrag startDrag()} 方法。
     * <P>
     * @see java.awt.dnd.DragGestureRecognizer
     * @see java.awt.dnd.DragGestureEvent
     * @param dge 描述刚刚发生的手势的 <code>DragGestureEvent</code>
     */

     void dragGestureRecognized(DragGestureEvent dge);
}
