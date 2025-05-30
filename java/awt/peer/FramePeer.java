/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.*;

import sun.awt.EmbeddedFrame;

/**
 * {@link Frame} 的对等接口。这在 {@link WindowPeer} 接口上添加了一些特定于框架的方法。
 *
 * 对等接口仅用于移植 AWT。它们不应用于应用程序开发人员，开发人员不应实现对等接口
 * 也不应直接在对等实例上调用任何对等方法。
 */
public interface FramePeer extends WindowPeer {

    /**
     * 设置框架的标题。
     *
     * @param title 要设置的标题
     *
     * @see Frame#setTitle(String)
     */
    void setTitle(String title);

    /**
     * 设置框架的菜单栏。
     *
     * @param mb 要设置的菜单栏
     *
     * @see Frame#setMenuBar(MenuBar)
     */
    void setMenuBar(MenuBar mb);

    /**
     * 设置框架是否可调整大小。
     *
     * @param resizeable 当框架应可调整大小时为 {@code true}，否则为 {@code false}
     *
     * @see Frame#setResizable(boolean)
     */
    void setResizable(boolean resizeable);

    /**
     * 更改框架的状态。
     *
     * @param state 新的状态
     *
     * @see Frame#setExtendedState(int)
     */
    void setState(int state);

    /**
     * 返回框架的当前状态。
     *
     * @return 框架的当前状态
     *
     * @see Frame#getExtendedState()
     */
    int getState();

    /**
     * 设置框架最大化时的边界。
     *
     * @param bounds 框架最大化的边界
     *
     * @see Frame#setMaximizedBounds(Rectangle)
     */
    void setMaximizedBounds(Rectangle bounds);

    /**
     * 为嵌入式框架设置大小和位置。（在嵌入式框架上，setLocation() 和 setBounds() 始终将框架设置为 (0,0) 以保持向后兼容性。
     *
     * @param x X 位置
     * @param y Y 位置
     * @param width 框架的宽度
     * @param height 框架的高度
     *
     * @see EmbeddedFrame#setBoundsPrivate(int, int, int, int)
     */
    // TODO: 这仅在 EmbeddedFrame 中使用，应该移动到扩展 FramePeer 的 EmbeddedFramePeer 中
    void setBoundsPrivate(int x, int y, int width, int height);

    /**
     * 返回嵌入式框架的大小和位置。（在嵌入式框架上，setLocation() 和 setBounds() 始终将框架设置为 (0,0) 以保持向后兼容性。
     *
     * @return 嵌入式框架的边界
     *
     * @see EmbeddedFrame#getBoundsPrivate()
     */
    // TODO: 这仅在 EmbeddedFrame 中使用，应该移动到扩展 FramePeer 的 EmbeddedFramePeer 中
    Rectangle getBoundsPrivate();

    /**
     * 请求对等对象模拟窗口激活。
     *
     * @param activate 激活或取消激活窗口
     */
    void emulateActivation(boolean activate);
}
