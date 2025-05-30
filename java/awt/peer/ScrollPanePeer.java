/*
 * Copyright (c) 1996, 2007, Oracle and/or its affiliates. All rights reserved.
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
package java.awt.peer;

import java.awt.Adjustable;
import java.awt.ScrollPane;
import java.awt.ScrollPaneAdjustable;

/**
 * {@link ScrollPane} 的对等接口。
 *
 * 对等接口仅用于 AWT 的移植。它们不应用于应用程序开发，
 * 开发者不应实现对等接口，也不应直接调用对等实例上的任何对等方法。
 */
public interface ScrollPanePeer extends ContainerPeer {

    /**
     * 返回水平滚动条的高度。
     *
     * @return 水平滚动条的高度
     *
     * @see ScrollPane#getHScrollbarHeight()
     */
    int getHScrollbarHeight();

    /**
     * 返回垂直滚动条的宽度。
     *
     * @return 垂直滚动条的宽度
     *
     * @see ScrollPane#getVScrollbarWidth()
     */
    int getVScrollbarWidth();

    /**
     * 设置子组件的滚动位置。
     *
     * @param x 滚动位置的 X 坐标
     * @param y 滚动位置的 Y 坐标
     *
     * @see ScrollPane#setScrollPosition(int, int)
     */
    void setScrollPosition(int x, int y);

    /**
     * 当子组件改变其大小时调用。
     *
     * @param w 子组件的新宽度
     * @param h 子组件的新高度
     *
     * @see ScrollPane#layout()
     */
    void childResized(int w, int h);

    /**
     * 设置滚动窗格的一个可调节对象的单位增量。
     *
     * @param adj 滚动窗格的可调节对象
     * @param u 单位增量
     *
     * @see ScrollPaneAdjustable#setUnitIncrement(int)
     */
    void setUnitIncrement(Adjustable adj, int u);

    /**
     * 设置滚动窗格的一个可调节对象的值。
     *
     * @param adj 滚动窗格的可调节对象
     * @param v 要设置的值
     */
    void setValue(Adjustable adj, int v);
}
