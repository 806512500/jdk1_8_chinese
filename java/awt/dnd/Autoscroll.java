/*
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Insets;
import java.awt.Point;

/**
 * 在拖放操作期间，用户可能希望将操作对象放置在一个可滚动的GUI控件的当前不可见区域。
 * <p>
 * 在这种情况下，希望GUI控件检测到这一点并启动滚动操作，以便使被遮挡的区域对用户可见。这一功能称为自动滚动。
 * <p>
 * 如果一个GUI控件既是一个活动的<code>DropTarget</code>，又是可滚动的，那么它可以通过实现此接口从拖放系统接收用户自动滚动手势的通知。
 * <p>
 * 自动滚动手势由用户通过将拖动光标在<code>Component</code>的边界区域（称为“自动滚动区域”）保持静止一段时间来启动，这将导致对<code>Component</code>的重复滚动请求，直到拖动<code>Cursor</code>恢复其运动。
 *
 * @since 1.2
 */

public interface Autoscroll {

    /**
     * 此方法返回描述自动滚动区域或边界的<code>Insets</code>，相对于实现组件的几何形状。
     * <P>
     * 此值由<code>DropTarget</code>在拖动<code>Cursor</code>进入关联的<code>Component</code>时读取一次。
     * <P>
     * @return the Insets
     */

    public Insets getAutoscrollInsets();

    /**
     * 通知<code>Component</code>自动滚动
     * <P>
     * @param cursorLocn 一个<code>Point</code>，指示触发此操作的光标位置。
     */

    public void autoscroll(Point cursorLocn);

}
