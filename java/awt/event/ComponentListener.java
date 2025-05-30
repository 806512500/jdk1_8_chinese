/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 用于接收组件事件的监听器接口。
 * 对组件事件感兴趣的类要么实现此接口（及其所有方法），要么扩展抽象的 <code>ComponentAdapter</code> 类
 * （仅重写感兴趣的方法）。然后，从该类创建的监听器对象使用组件的 <code>addComponentListener</code>
 * 方法注册。当组件的大小、位置或可见性发生变化时，监听器对象中的相关方法将被调用，
 * 并将 <code>ComponentEvent</code> 传递给它。
 * <P>
 * 组件事件仅用于通知目的；
 * AWT 将自动处理组件的移动和调整大小，以确保 GUI 布局正常工作，无论程序是否注册了
 * <code>ComponentListener</code>。
 *
 * @see ComponentAdapter
 * @see ComponentEvent
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/componentlistener.html">教程：编写组件监听器</a>
 *
 * @author Carl Quinn
 * @since 1.1
 */
public interface ComponentListener extends EventListener {
    /**
     * 当组件的大小发生变化时调用。
     */
    public void componentResized(ComponentEvent e);

    /**
     * 当组件的位置发生变化时调用。
     */
    public void componentMoved(ComponentEvent e);

    /**
     * 当组件变为可见时调用。
     */
    public void componentShown(ComponentEvent e);

    /**
     * 当组件变为不可见时调用。
     */
    public void componentHidden(ComponentEvent e);
}
