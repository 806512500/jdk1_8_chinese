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

package java.awt.event;

import java.util.EventListener;

/**
 * 用于接收窗口状态事件的监听器接口。
 * <p>
 * 对窗口状态事件感兴趣的类要么实现此接口（及其包含的所有方法），要么扩展抽象的 <code>WindowAdapter</code> 类
 * （仅覆盖感兴趣的方法）。
 * <p>
 * 从该类创建的监听器对象然后使用 <code>Window</code> 的
 * <code>addWindowStateListener</code> 方法注册。当窗口的状态由于最小化、最大化等操作发生变化时，
 * 监听器对象中的 <code>windowStateChanged</code> 方法被调用，并将 <code>WindowEvent</code> 传递给它。
 *
 * @see java.awt.event.WindowAdapter
 * @see java.awt.event.WindowEvent
 *
 * @since 1.4
 */
public interface WindowStateListener extends EventListener {
    /**
     * 当窗口状态发生变化时调用。
     */
    public void windowStateChanged(WindowEvent e);
}
