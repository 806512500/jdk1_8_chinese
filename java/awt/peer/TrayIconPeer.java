/*
 * Copyright (c) 2005, 2007, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.SystemTray;
import java.awt.TrayIcon;

/**
 * {@link TrayIcon} 的对等接口。如果 {@link SystemTray#isSupported()} 返回 false，则不需要实现此接口。
 */
public interface TrayIconPeer {

    /**
     * 释放托盘图标并释放其持有的资源。
     *
     * @see TrayIcon#removeNotify()
     */
    void dispose();

    /**
     * 设置托盘图标的工具提示。
     *
     * @param tooltip 要设置的工具提示
     *
     * @see TrayIcon#setToolTip(String)
     */
    void setToolTip(String tooltip);

    /**
     * 更新图标图像。这应该显示来自 TrayIcon 组件的实际托盘图标中的当前图标。
     *
     * @see TrayIcon#setImage(java.awt.Image)
     * @see TrayIcon#setImageAutoSize(boolean)
     */
    void updateImage();

    /**
     * 在托盘图标上显示消息。
     *
     * @param caption 消息标题
     * @param text 实际消息文本
     * @param messageType 消息类型
     *
     * @see TrayIcon#displayMessage(String, String, java.awt.TrayIcon.MessageType)
     */
    void displayMessage(String caption, String text, String messageType);

    /**
     * 在指定位置显示此托盘图标的弹出菜单。
     *
     * @param x 弹出菜单的 X 位置
     * @param y 弹出菜单的 Y 位置
     */
    void showPopupMenu(int x, int y);
}
