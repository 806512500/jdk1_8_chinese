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
 * 用于接收容器事件的监听器接口。
 * 对容器事件感兴趣的类要么实现此接口（及其所有方法），
 * 要么扩展抽象的 <code>ContainerAdapter</code> 类（仅重写感兴趣的方法）。
 * 从该类创建的监听器对象然后使用组件的 <code>addContainerListener</code>
 * 方法注册到组件。当容器的内容因组件被添加或移除而改变时，
 * 监听器对象中的相关方法将被调用，并将 <code>ContainerEvent</code> 传递给它。
 * <P>
 * 容器事件仅用于通知目的；
 * AWT 将自动处理添加和移除操作，因此无论程序是否注册了 {@code ContainerListener}，
 * 程序都能正常工作。
 *
 * @see ContainerAdapter
 * @see ContainerEvent
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/containerlistener.html">教程：编写容器监听器</a>
 *
 * @author Tim Prinzing
 * @author Amy Fowler
 * @since 1.1
 */
public interface ContainerListener extends EventListener {
    /**
     * 当组件被添加到容器时调用。
     */
    public void componentAdded(ContainerEvent e);

    /**
     * 当组件从容器中移除时调用。
     */
    public void componentRemoved(ContainerEvent e);

}
