/*
 * Copyright (c) 1998, 2003, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.AWTEvent;

/**
 * 用于接收 Component 或 MenuComponent 及其子类对象事件通知的监听器接口。与本包中的其他 EventListeners 不同，
 * AWTEventListeners 会被动地观察 AWT 系统范围内的事件分发。大多数应用程序不应使用此类；可能使用 AWTEventListeners 的应用程序
 * 包括用于自动化测试的事件记录器，以及类似 Java Accessibility 包的设施。
 * <p>
 * 对于希望监控 AWT 事件的类，该类应实现此接口，并使用 Toolkit 的 <code>addAWTEventListener</code> 方法将创建的对象注册到 Toolkit。
 * 当 AWT 中任何地方分发事件时，该对象的 <code>eventDispatched</code> 方法将被调用。
 *
 * @see java.awt.AWTEvent
 * @see java.awt.Toolkit#addAWTEventListener
 * @see java.awt.Toolkit#removeAWTEventListener
 *
 * @author Fred Ecks
 * @since 1.2
 */
public interface AWTEventListener extends EventListener {

    /**
     * 当 AWT 中分发事件时被调用。
     */
    public void eventDispatched(AWTEvent event);

}
