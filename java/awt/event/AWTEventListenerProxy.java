/*
 * Copyright (c) 2001, 2007, Oracle and/or its affiliates. All rights reserved.
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

import java.util.EventListenerProxy;
import java.awt.AWTEvent;

/**
 * 一个扩展 {@code EventListenerProxy} 的类，
 * 专门用于为特定事件掩码添加 {@code AWTEventListener}。
 * 该类的实例可以作为 {@code AWTEventListener} 添加到 {@code Toolkit} 对象中。
 * <p>
 * {@code Toolkit} 的 {@code getAWTEventListeners} 方法
 * 可以返回 {@code AWTEventListener} 和 {@code AWTEventListenerProxy} 对象的混合。
 *
 * @see java.awt.Toolkit
 * @see java.util.EventListenerProxy
 * @since 1.4
 */
public class AWTEventListenerProxy
        extends EventListenerProxy<AWTEventListener>
        implements AWTEventListener {

    private final long eventMask;

    /**
     * 构造函数，将 {@code AWTEventListener} 绑定到特定的事件掩码。
     *
     * @param eventMask  要接收的事件类型的位图
     * @param listener   监听器对象
     */
    public AWTEventListenerProxy (long eventMask, AWTEventListener listener) {
        super(listener);
        this.eventMask = eventMask;
    }

    /**
     * 将 AWT 事件转发给监听器代理。
     *
     * @param event  AWT 事件
     */
    public void eventDispatched(AWTEvent event) {
        getListener().eventDispatched(event);
    }

    /**
     * 返回与监听器关联的事件掩码。
     *
     * @return 与监听器关联的事件掩码
     */
    public long getEventMask() {
        return this.eventMask;
    }
}
