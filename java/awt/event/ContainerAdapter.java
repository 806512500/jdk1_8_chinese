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

/**
 * 一个用于接收容器事件的抽象适配器类。
 * 该类中的方法都是空的。这个类的存在是为了方便创建监听器对象。
 * <P>
 * 扩展这个类以创建一个 <code>ContainerEvent</code> 监听器，并覆盖感兴趣的事件的方法。（如果你实现了
 * <code>ContainerListener</code> 接口，你必须定义该接口中的所有方法。这个抽象类为所有方法定义了空方法，
 * 因此你只需定义感兴趣的事件的方法。）
 * <P>
 * 使用扩展的类创建一个监听器对象，然后使用组件的 <code>addContainerListener</code>
 * 方法注册它。当容器的内容因组件被添加或移除而改变时，监听器对象中的相关方法将被调用，
 * 并将 <code>ContainerEvent</code> 传递给它。
 *
 * @see ContainerEvent
 * @see ContainerListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/containerlistener.html">教程：编写容器监听器</a>
 *
 * @author Amy Fowler
 * @since 1.1
 */
public abstract class ContainerAdapter implements ContainerListener {
    /**
     * 当组件被添加到容器时调用。
     */
    public void componentAdded(ContainerEvent e) {}

    /**
     * 当组件从容器中移除时调用。
     */
    public void componentRemoved(ContainerEvent e) {}
}
