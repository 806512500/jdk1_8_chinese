/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Point;

import java.util.EventObject;

/**
 * 该类是 <code>DragSourceDragEvent</code> 和 <code>DragSourceDropEvent</code> 的基类。
 * <p>
 * <code>DragSourceEvent</code> 在拖动进入、移动或离开一个放置站点时生成，当拖动操作改变时生成，以及当拖动结束时生成。
 * 生成的 <code>DragSourceEvent</code> 的位置指定了该事件发生时鼠标光标在屏幕坐标中的位置。
 * <p>
 * 在没有虚拟设备的多屏幕环境中，光标位置是在 <i>发起者</i> <code>GraphicsConfiguration</code> 的坐标系统中指定的。
 * <i>发起者</i> <code>GraphicsConfiguration</code> 是当前拖动操作中识别出拖动手势的 <code>Component</code> 的 <code>GraphicsConfiguration</code>。
 * 如果光标位置超出发起者 <code>GraphicsConfiguration</code> 的边界，报告的坐标将被裁剪以适应该 <code>GraphicsConfiguration</code> 的边界。
 * <p>
 * 在有虚拟设备的多屏幕环境中，位置是在相应的虚拟坐标系统中指定的。如果光标位置超出虚拟设备的边界，报告的坐标将被裁剪以适应虚拟设备的边界。
 *
 * @since 1.2
 */

public class DragSourceEvent extends EventObject {

    private static final long serialVersionUID = -763287114604032641L;

    /**
     * 表示此事件是否指定了光标位置的 <code>boolean</code>。
     *
     * @serial
     */
    private final boolean locationSpecified;

    /**
     * 如果此事件指定了光标位置，则为该事件发生时光标位置的水平坐标；否则为零。
     *
     * @serial
     */
    private final int x;

    /**
     * 如果此事件指定了光标位置，则为该事件发生时光标位置的垂直坐标；否则为零。
     *
     * @serial
     */
    private final int y;

    /**
     * 给定一个指定的 <code>DragSourceContext</code> 构造一个 <code>DragSourceEvent</code>。
     * 该 <code>DragSourceEvent</code> 的坐标未指定，因此 <code>getLocation</code> 将返回
     * <code>null</code>。
     *
     * @param dsc the <code>DragSourceContext</code>
     *
     * @throws IllegalArgumentException 如果 <code>dsc</code> 为 <code>null</code>。
     *
     * @see #getLocation
     */

    public DragSourceEvent(DragSourceContext dsc) {
        super(dsc);
        locationSpecified = false;
        this.x = 0;
        this.y = 0;
    }

    /**
     * 给定一个指定的 <code>DragSourceContext</code> 和光标位置的坐标，构造一个 <code>DragSourceEvent</code>。
     *
     * @param dsc the <code>DragSourceContext</code>
     * @param x   该事件发生时光标位置的水平坐标
     * @param y   该事件发生时光标位置的垂直坐标
     *
     * @throws IllegalArgumentException 如果 <code>dsc</code> 为 <code>null</code>。
     *
     * @since 1.4
     */
    public DragSourceEvent(DragSourceContext dsc, int x, int y) {
        super(dsc);
        locationSpecified = true;
        this.x = x;
        this.y = y;
    }

    /**
     * 返回引发此事件的 <code>DragSourceContext</code>。
     * <P>
     * @return 引发此事件的 <code>DragSourceContext</code>
     */

    public DragSourceContext getDragSourceContext() {
        return (DragSourceContext)getSource();
    }

    /**
     * 返回一个 <code>Point</code>，表示该事件发生时光标在屏幕坐标中的位置，或如果此事件未指定光标位置则返回 <code>null</code>。
     *
     * @return 表示光标位置的 <code>Point</code>，或如果未指定光标位置则返回 <code>null</code>
     * @since 1.4
     */
    public Point getLocation() {
        if (locationSpecified) {
            return new Point(x, y);
        } else {
            return null;
        }
    }

    /**
     * 返回该事件发生时光标在屏幕坐标中的水平坐标，或如果此事件未指定光标位置则返回零。
     *
     * @return 表示光标水平坐标的整数，或如果未指定光标位置则返回零
     * @since 1.4
     */
    public int getX() {
        return x;
    }

    /**
     * 返回该事件发生时光标在屏幕坐标中的垂直坐标，或如果此事件未指定光标位置则返回零。
     *
     * @return 表示光标垂直坐标的整数，或如果未指定光标位置则返回零
     * @since 1.4
     */
    public int getY() {
        return y;
    }
}
