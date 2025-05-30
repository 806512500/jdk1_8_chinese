/*
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Component;
import java.awt.Window;

/**
 * {@link KeyboardFocusManager} 的本机对等接口。
 */
public interface KeyboardFocusManagerPeer {

    /**
     * 设置应成为焦点窗口的窗口。
     *
     * @param win 应成为焦点窗口的窗口
     *
     */
    void setCurrentFocusedWindow(Window win);

    /**
     * 返回当前焦点窗口。
     *
     * @return 当前焦点窗口
     *
     * @see KeyboardFocusManager#getNativeFocusedWindow()
     */
    Window getCurrentFocusedWindow();

    /**
     * 设置应成为焦点所有者的组件。
     *
     * @param comp 应成为焦点所有者的组件
     *
     * @see KeyboardFocusManager#setNativeFocusOwner(Component)
     */
    void setCurrentFocusOwner(Component comp);

    /**
     * 返回当前拥有输入焦点的组件。
     *
     * @return 当前拥有输入焦点的组件
     *
     * @see KeyboardFocusManager#getNativeFocusOwner()
     */
    Component getCurrentFocusOwner();

    /**
     * 清除当前的全局焦点所有者。
     *
     * @param activeWindow
     *
     * @see KeyboardFocusManager#clearGlobalFocusOwner()
     */
    void clearGlobalFocusOwner(Window activeWindow);

}
