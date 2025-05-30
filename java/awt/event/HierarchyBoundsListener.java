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
 * 用于接收祖先移动和调整大小事件的监听器接口。
 * 对这些事件感兴趣的类要么实现此接口（及其所有方法），要么扩展抽象
 * <code>HierarchyBoundsAdapter</code> 类（仅重写感兴趣的方法）。
 * 从该类创建的监听器对象然后使用 Component 的 <code>addHierarchyBoundsListener</code>
 * 方法注册。当 Component 所属的层次结构因祖先的调整大小或移动而改变时，监听器对象中的相关方法
 * 将被调用，并将 <code>HierarchyEvent</code> 传递给它。
 * <p>
 * 层次结构事件仅用于通知目的；
 * AWT 将自动处理层次结构的内部变化，以确保 GUI 布局正常工作，无论程序是否注册了
 * <code>HierarchyBoundsListener</code>。
 *
 * @author      David Mendenhall
 * @see         HierarchyBoundsAdapter
 * @see         HierarchyEvent
 * @since       1.3
 */
public interface HierarchyBoundsListener extends EventListener {
    /**
     * 当源的祖先移动时调用。
     */
    public void ancestorMoved(HierarchyEvent e);

    /**
     * 当源的祖先调整大小时调用。
     */
    public void ancestorResized(HierarchyEvent e);
}
