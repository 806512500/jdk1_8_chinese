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

package java.awt.im.spi;

import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodRequests;
import java.text.AttributedCharacterIterator;
import javax.swing.JFrame;

/**
 * 提供输入方法可以用来与客户端组件通信或请求其他服务的方法。此接口由输入方法框架实现，
 * 输入方法在其通过 {@link java.awt.im.spi.InputMethod#setInputMethodContext} 接收到的实例上调用这些方法。
 * 不应有其他实现者或调用者。
 *
 * @since 1.3
 *
 * @author JavaSoft International
 */

public interface InputMethodContext extends InputMethodRequests {

    /**
     * 从给定的参数创建一个输入方法事件并将其分派给客户端组件。有关参数，请参见
     * {@link java.awt.event.InputMethodEvent#InputMethodEvent}。
     */
    public void dispatchInputMethodEvent(int id,
                AttributedCharacterIterator text, int committedCharacterCount,
                TextHitInfo caret, TextHitInfo visiblePosition);

    /**
     * 为输入方法创建一个顶级窗口。此窗口的预期行为是：
     * <ul>
     * <li>它浮在所有文档窗口和对话框之上
     * <li>它及其包含的所有组件不会接收焦点
     * <li>它具有轻量级的装饰，例如没有标题的减少的拖动区域
     * </ul>
     * 然而，这三项的实际行为取决于平台。
     * <p>
     * 标题可能会或可能不会显示，具体取决于创建的实际窗口类型。
     * <p>
     * 如果 attachToInputContext 为 true，则新窗口将共享与此输入方法上下文相对应的输入上下文，
     * 以便窗口中的组件事件自动分派给输入方法。此外，当使用 setVisible(true) 打开窗口时，
     * 输入上下文将防止可能引起的输入方法的停用和激活调用。
     * <p>
     * 输入方法必须在不再需要返回的输入方法窗口时调用 {@link java.awt.Window#dispose() Window.dispose}。
     * <p>
     * @param title 要在窗口标题栏中显示的标题，如果有这样的标题栏。
     *              如果为 <code>null</code>，则视为一个空字符串，""。
     * @param attachToInputContext 此窗口是否应共享与此输入方法上下文相对应的输入上下文
     * @return 一个具有特殊特性的窗口，供输入方法使用
     * @exception HeadlessException 如果 <code>GraphicsEnvironment.isHeadless
     *              </code> 返回 <code>true</code>
     */
    public Window createInputMethodWindow(String title, boolean attachToInputContext);

    /**
     * 为输入方法创建一个顶级 Swing JFrame。此窗口的预期行为是：
     * <ul>
     * <li>它浮在所有文档窗口和对话框之上
     * <li>它及其包含的所有组件不会接收焦点
     * <li>它具有轻量级的装饰，例如没有标题的减少的拖动区域
     * </ul>
     * 然而，这三项的实际行为取决于平台。
     * <p>
     * 标题可能会或可能不会显示，具体取决于创建的实际窗口类型。
     * <p>
     * 如果 attachToInputContext 为 true，则新窗口将共享与此输入方法上下文相对应的输入上下文，
     * 以便窗口中的组件事件自动分派给输入方法。此外，当使用 setVisible(true) 打开窗口时，
     * 输入上下文将防止可能引起的输入方法的停用和激活调用。
     * <p>
     * 输入方法必须在不再需要返回的输入方法窗口时调用 {@link java.awt.Window#dispose() Window.dispose}。
     * <p>
     * @param title 要在窗口标题栏中显示的标题，如果有这样的标题栏。
     *              如果为 <code>null</code>，则视为一个空字符串，""。
     * @param attachToInputContext 此窗口是否应共享与此输入方法上下文相对应的输入上下文
     * @return 一个具有特殊特性的 JFrame，供输入方法使用
     * @exception HeadlessException 如果 <code>GraphicsEnvironment.isHeadless
     *              </code> 返回 <code>true</code>
     *
     * @since 1.4
     */
    public JFrame createInputMethodJFrame(String title, boolean attachToInputContext);

    /**
     * 为指定的输入方法启用或禁用当前客户端窗口的位置和状态通知。当启用通知时，
     * 输入方法的 {@link java.awt.im.spi.InputMethod#notifyClientWindowChange
     * notifyClientWindowChange} 方法将按照该方法的规范进行调用。当输入方法被释放时，
     * 通知将自动禁用。
     *
     * @param inputMethod 要启用或禁用通知的输入方法
     * @param enable true 表示启用，false 表示禁用
     */
    public void enableClientWindowNotification(InputMethod inputMethod, boolean enable);
}
