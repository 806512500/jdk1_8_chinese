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

/**
 * {@link Window} 的对等接口。
 *
 * 对等接口仅用于移植 AWT。不建议应用程序开发人员使用这些接口，也不应直接在对等实例上调用任何对等方法。
 */
public interface WindowPeer extends ContainerPeer {

    /**
     * 使此窗口成为桌面上的最上层窗口。
     *
     * @see Window#toFront()
     */
    void toFront();

    /**
     * 使此窗口成为桌面上的最底层窗口。
     *
     * @see Window#toBack()
     */
    void toBack();

    /**
     * 更新窗口的始终置顶状态。
     * 设置窗口是否应始终位于所有其他窗口之上。
     *
     * @see Window#getAlwaysOnTop()
     * @see Window#setAlwaysOnTop(boolean)
     */
    void updateAlwaysOnTopState();

    /**
     * 更新窗口的可聚焦状态。
     *
     * @see Window#setFocusableWindowState(boolean)
     */
    void updateFocusableWindowState();

    /**
     * 设置此窗口是否被模态对话框阻塞。
     *
     * @param blocker 阻塞的模态对话框
     * @param blocked {@code true} 阻塞窗口，{@code false} 解除阻塞
     */
    void setModalBlocked(Dialog blocker, boolean blocked);

    /**
     * 更新对等窗口的最小尺寸。
     *
     * @see Window#setMinimumSize(Dimension)
     */
    void updateMinimumSize();

    /**
     * 更新窗口的图标。
     *
     * @see Window#setIconImages(java.util.List)
     */
    void updateIconImages();

    /**
     * 设置窗口的不透明度。
     *
     * @see Window#setOpacity(float)
     */
    void setOpacity(float opacity);

    /**
     * 为窗口启用每个像素的 alpha 支持。
     *
     * @see Window#setBackground(Color)
     */
    void setOpaque(boolean isOpaque);

    /**
     * 更新非不透明窗口的本机部分。
     *
     * @see Window#setBackground(Color)
     */
    void updateWindow();

    /**
     * 指示对等组件更新安全警告的位置。
     */
    void repositionSecurityWarning();
}
