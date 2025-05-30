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

/**
 * 一个用于接收祖先移动和调整大小事件的抽象适配器类。
 * 该类中的方法是空的。这个类的存在是为了方便创建监听器对象。
 * <p>
 * 扩展这个类并覆盖您感兴趣的事件的方法。（如果您实现了 <code>HierarchyBoundsListener</code> 接口，
 * 您必须定义该接口中的两个方法。这个抽象类为这两个方法都定义了空方法，因此您只需要定义您关心的事件的方法。）
 * <p>
 * 使用您的类创建一个监听器对象，然后使用组件的 <code>addHierarchyBoundsListener</code> 方法将其注册到组件。
 * 当组件所属的层次结构因祖先的调整大小或移动而改变时，监听器对象中的相关方法将被调用，并将 <code>HierarchyEvent</code> 传递给它。
 *
 * @author      David Mendenhall
 * @see         HierarchyBoundsListener
 * @see         HierarchyEvent
 * @since       1.3
 */
public abstract class HierarchyBoundsAdapter implements HierarchyBoundsListener
{
    /**
     * 当源的祖先被移动时调用。
     */
    public void ancestorMoved(HierarchyEvent e) {}

    /**
     * 当源的祖先被调整大小时调用。
     */
    public void ancestorResized(HierarchyEvent e) {}
}
