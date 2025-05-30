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
 * 用于接收键盘焦点事件的抽象适配器类。
 * 该类中的方法都是空的。该类的存在是为了方便创建监听器对象。
 * <P>
 * 扩展此类以创建一个 <code>FocusEvent</code> 监听器，并覆盖感兴趣的事件方法。
 * （如果你实现了 <code>FocusListener</code> 接口，你必须定义该接口中的所有方法。
 * 这个抽象类为所有方法定义了空方法，因此你只需定义关心的事件方法。）
 * <P>
 * 使用扩展类创建一个监听器对象，然后使用组件的 <code>addFocusListener</code>
 * 方法将其注册到组件上。当组件获得或失去键盘焦点时，
 * 相关的监听器对象中的方法将被调用，并将 <code>FocusEvent</code> 传递给它。
 *
 * @see FocusEvent
 * @see FocusListener
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/focuslistener.html">教程：编写一个焦点监听器</a>
 *
 * @author Carl Quinn
 * @since 1.1
 */
public abstract class FocusAdapter implements FocusListener {
    /**
     * 当组件获得键盘焦点时调用。
     */
    public void focusGained(FocusEvent e) {}

    /**
     * 当组件失去键盘焦点时调用。
     */
    public void focusLost(FocusEvent e) {}
}
