/*
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
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
 * 用于接收键盘事件（按键）的监听器接口。
 * 对键盘事件感兴趣的类要么实现此接口（及其所有方法），
 * 要么扩展抽象 <code>KeyAdapter</code> 类（仅覆盖感兴趣的方法）。
 * <P>
 * 从该类创建的监听器对象然后使用组件的 <code>addKeyListener</code>
 * 方法注册到组件。当按键被按下、释放或输入时，会生成键盘事件。
 * 相关方法在监听器对象中被调用，并将 <code>KeyEvent</code> 传递给它。
 *
 * @author Carl Quinn
 *
 * @see KeyAdapter
 * @see KeyEvent
 * @see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/keylistener.html">教程：编写键监听器</a>
 *
 * @since 1.1
 */
public interface KeyListener extends EventListener {

    /**
     * 当按键被输入时调用。
     * 有关按键输入事件的定义，请参阅 {@link KeyEvent} 的类描述。
     */
    public void keyTyped(KeyEvent e);

    /**
     * 当按键被按下时调用。
     * 有关按键按下事件的定义，请参阅 {@link KeyEvent} 的类描述。
     */
    public void keyPressed(KeyEvent e);

    /**
     * 当按键被释放时调用。
     * 有关按键释放事件的定义，请参阅 {@link KeyEvent} 的类描述。
     */
    public void keyReleased(KeyEvent e);
}
