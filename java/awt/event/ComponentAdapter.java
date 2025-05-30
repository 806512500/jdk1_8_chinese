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
 * 一个用于接收组件事件的抽象适配器类。
 * 该类中的方法都是空的。该类的存在是为了方便创建监听器对象。
 * <P>
 * 继承此类以创建 <code>ComponentEvent</code> 监听器，并覆盖感兴趣的事件方法。
 * （如果你实现了 <code>ComponentListener</code> 接口，你必须定义该接口中的所有方法。
 * 这个抽象类为所有方法定义了空方法，因此你只需要定义你关心的事件方法。）
 * <P>
 * 使用你的类创建一个监听器对象，然后使用组件的 <code>addComponentListener</code>
 * 方法将其注册到组件。当组件的大小、位置或可见性发生变化时，监听器对象中的相关方法将被调用，
 * 并将 <code>ComponentEvent</code> 传递给它。
 *
 * @see ComponentEvent
 * @see ComponentListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/componentlistener.html">教程：编写组件监听器</a>
 *
 * @author Carl Quinn
 * @since 1.1
 */
public abstract class ComponentAdapter implements ComponentListener {
    /**
     * 当组件的大小发生变化时调用。
     */
    public void componentResized(ComponentEvent e) {}

    /**
     * 当组件的位置发生变化时调用。
     */
    public void componentMoved(ComponentEvent e) {}

    /**
     * 当组件被显示时调用。
     */
    public void componentShown(ComponentEvent e) {}

    /**
     * 当组件被隐藏时调用。
     */
    public void componentHidden(ComponentEvent e) {}
}
