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
 * 用于接收组件上键盘焦点事件的监听器接口。
 * 对于希望处理焦点事件的类，可以实现此接口（及其所有方法），
 * 或者扩展抽象的 <code>FocusAdapter</code> 类（仅重写感兴趣的方法）。
 * 从该类创建的监听器对象然后使用组件的 <code>addFocusListener</code>
 * 方法注册到组件。当组件获得或失去键盘焦点时，
 * 监听器对象中的相关方法将被调用，并将 <code>FocusEvent</code> 传递给它。
 *
 * @see FocusAdapter
 * @see FocusEvent
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/focuslistener.html">教程：编写焦点监听器</a>
 *
 * @author Carl Quinn
 * @since 1.1
 */
public interface FocusListener extends EventListener {

    /**
     * 当组件获得键盘焦点时调用。
     */
    public void focusGained(FocusEvent e);

    /**
     * 当组件失去键盘焦点时调用。
     */
    public void focusLost(FocusEvent e);
}
