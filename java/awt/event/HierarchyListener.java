/*
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
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
 * 用于接收层次结构更改事件的监听器接口。
 * 对层次结构更改事件感兴趣的类应实现此接口。
 * 从该类创建的监听器对象然后使用 Component 的 <code>addHierarchyListener</code>
 * 方法注册。当 Component 所属的层次结构发生变化时，监听器对象中的
 * <code>hierarchyChanged</code> 方法将被调用，并将 <code>HierarchyEvent</code>
 * 传递给它。
 * <p>
 * 层次结构事件仅用于通知；
 * AWT 将自动处理层次结构的内部更改，以确保 GUI 布局、可显示性和可见性正常工作，
 * 无论程序是否注册了 <code>HierarchyListener</code>。
 *
 * @author      David Mendenhall
 * @see         HierarchyEvent
 * @since       1.3
 */
public interface HierarchyListener extends EventListener {
    /**
     * 当层次结构发生变化时调用。要确定实际的更改类型，请调用
     * <code>HierarchyEvent.getChangeFlags()</code>。
     *
     * @see HierarchyEvent#getChangeFlags()
     */
    public void hierarchyChanged(HierarchyEvent e);
}
