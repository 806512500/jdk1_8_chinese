
/*
 * Copyright (c) 1997, 2004, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Locale;
import java.awt.AWTEvent;
import java.awt.Rectangle;
import java.lang.Character.Subset;


/**
 * 定义支持复杂文本输入的输入方法的接口。
 * 传统上，输入方法支持字符数多于标准键盘所能表示的字符数的语言，如中文、日文和韩文。
 * 然而，它们也可以用于支持英语的音素文本输入或泰语的字符重新排序。
 * <p>
 * InputMethod 的子类可以由输入方法框架加载；然后可以通过 API
 * ({@link java.awt.im.InputContext#selectInputMethod InputContext.selectInputMethod})
 * 或用户界面（输入方法选择菜单）选择它们。
 *
 * @since 1.3
 *
 * @author JavaSoft International
 */

public interface InputMethod {

    /**
     * 设置输入方法上下文，用于将输入方法事件分派给客户端组件并从客户端组件请求信息。
     * <p>
     * 在实例化此输入方法后立即调用此方法。
     *
     * @param context 该输入方法的输入方法上下文
     * @exception NullPointerException 如果 <code>context</code> 为 null
     */
    public void setInputMethodContext(InputMethodContext context);

    /**
     * 尝试设置输入语言环境。如果输入方法支持所需的语言环境，则更改其行为以支持该语言环境的输入并返回 true。
     * 否则，返回 false 并不更改其行为。
     * <p>
     * 此方法在以下情况下调用：
     * <ul>
     * <li>由 {@link java.awt.im.InputContext#selectInputMethod InputContext.selectInputMethod} 调用，
     * <li>通过用户界面切换到此输入方法时，如果用户指定了语言环境或先前选择的输入方法的
     *     {@link java.awt.im.spi.InputMethod#getLocale getLocale} 方法返回非 null 值。
     * </ul>
     *
     * @param locale 要输入的语言环境
     * @return 是否支持指定的语言环境
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     */
    public boolean setLocale(Locale locale);

    /**
     * 返回当前输入语言环境。在特殊情况下可能返回 null。
     * <p>
     * 此方法在以下情况下调用：
     * <ul>
     * <li>由 {@link java.awt.im.InputContext#getLocale InputContext.getLocale} 调用，
     * <li>通过用户界面或
     *     {@link java.awt.im.InputContext#selectInputMethod InputContext.selectInputMethod}
     *     切换到不同的输入方法时。
     * </ul>
     *
     * @return 当前输入语言环境，或 null
     */
    public Locale getLocale();

    /**
     * 设置此输入方法允许输入的 Unicode 字符集的子集。可以传递 null 表示允许所有字符。
     * <p>
     * 此方法在以下情况下调用：
     * <ul>
     * <li>在实例化此输入方法后立即调用，
     * <li>从不同的输入方法切换到此输入方法时，
     * <li>由 {@link java.awt.im.InputContext#setCharacterSubsets InputContext.setCharacterSubsets} 调用。
     * </ul>
     *
     * @param subsets 允许输入的 Unicode 字符集的子集
     */
    public void setCharacterSubsets(Subset[] subsets);

    /**
     * 根据参数 <code>enable</code> 的值启用或禁用此输入方法的组合。
     * <p>
     * 启用组合的输入方法会解释传入的事件以进行组合和控制，而禁用组合的输入方法不会解释事件以进行组合。
     * 但是，无论是否启用，事件都会传递给输入方法，禁用组合的输入方法仍可能解释事件以进行控制，
     * 包括启用或禁用自身的组合。
     * <p>
     * 对于主机操作系统提供的输入方法，有时无法确定此操作是否受支持。例如，输入方法可能仅对某些语言环境启用组合，
     * 而对其他语言环境不执行任何操作。对于这样的输入方法，此方法可能不会抛出
     * {@link java.lang.UnsupportedOperationException UnsupportedOperationException}，
     * 但也不会影响组合是否启用。
     * <p>
     * 此方法在以下情况下调用：
     * <ul>
     * <li>由 {@link java.awt.im.InputContext#setCompositionEnabled InputContext.setCompositionEnabled} 调用，
     * <li>通过用户界面或
     *     {@link java.awt.im.InputContext#selectInputMethod InputContext.selectInputMethod}
     *     从不同的输入方法切换到此输入方法时，如果先前选择的输入方法的
     *     {@link java.awt.im.spi.InputMethod#isCompositionEnabled isCompositionEnabled}
     *     方法没有抛出异常。
     * </ul>
     *
     * @param enable 是否启用输入方法的组合
     * @throws UnsupportedOperationException 如果此输入方法不支持启用/禁用操作
     * @see #isCompositionEnabled
     */
    public void setCompositionEnabled(boolean enable);

    /**
     * 确定此输入方法是否已启用。
     * 启用组合的输入方法会解释传入的事件以进行组合和控制，而禁用组合的输入方法不会解释事件以进行组合。
     * <p>
     * 此方法在以下情况下调用：
     * <ul>
     * <li>由 {@link java.awt.im.InputContext#isCompositionEnabled InputContext.isCompositionEnabled} 调用，
     * <li>通过用户界面或
     *     {@link java.awt.im.InputContext#selectInputMethod InputContext.selectInputMethod}
     *     从不同的输入方法切换到此输入方法时。
     * </ul>
     *
     * @return 如果此输入方法已启用组合，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @throws UnsupportedOperationException 如果此输入方法不支持检查是否已启用组合
     * @see #setCompositionEnabled
     */
    public boolean isCompositionEnabled();

    /**
     * 开始重新转换操作。输入方法使用
     * {@link java.awt.im.InputMethodRequests#getSelectedText InputMethodRequests.getSelectedText}
     * 方法从当前客户端组件获取要重新转换的文本。它还可以使用其他 <code>InputMethodRequests</code>
     * 方法请求重新转换操作所需的其他信息。操作产生的组合文本和已提交文本
     * 作为一系列 <code>InputMethodEvent</code> 发送到客户端组件。如果给定的文本无法重新转换，
     * 则应将相同的文本作为已提交文本发送到客户端组件。
     * <p>
     * 此方法由
     * {@link java.awt.im.InputContext#reconvert() InputContext.reconvert} 调用。
     *
     * @throws UnsupportedOperationException 如果输入方法不支持重新转换操作。
     */
    public void reconvert();

    /**
     * 将事件分派给输入方法。如果为焦点组件启用了输入方法支持，则某些类型的传入事件
     * 在分派给组件的方法或事件监听器之前，会先分派给该组件的当前输入方法。
     * 输入方法决定是否需要处理该事件。如果需要处理，它还会调用事件的 <code>consume</code> 方法；
     * 这将导致事件不会被分派到组件的事件处理方法或事件监听器。
     * <p>
     * 如果事件是 InputEvent 或其子类的实例，则会分派事件。
     * 这包括 AWT 类 KeyEvent 和 MouseEvent 的实例。
     * <p>
     * 此方法由 {@link java.awt.im.InputContext#dispatchEvent InputContext.dispatchEvent} 调用。
     *
     * @param event 被分派到输入方法的事件
     * @exception NullPointerException 如果 <code>event</code> 为 null
     */
    public void dispatchEvent(AWTEvent event);

    /**
     * 通知此输入方法客户端窗口位置或状态的变化。此方法在以下情况下调用：
     * <ul>
     * <li>
     * 当包含当前客户端组件的窗口在位置、大小、可见性、图标化状态或关闭时发生变化时，
     * <li>
     * 从 <code> enableClientWindowNotification(inputMethod,
     * true)</code> 调用，如果当前客户端组件存在，
     * <li>
     * 在输入方法首次激活后调用
     * <code>enableClientWindowNotification(inputMethod,
     * true)</code> 时，如果调用时没有当前客户端组件，
     * <li>
     * 在输入上下文的 removeNotify 方法被调用后，为新的客户端组件激活输入方法时。
     * </ul>
     * @param bounds 客户端窗口在屏幕上的 {@link
     * java.awt.Component#getBounds bounds}；如果客户端窗口已图标化或不可见，则为 null
     */
    public void notifyClientWindowChange(Rectangle bounds);

    /**
     * 激活输入方法以立即进行输入处理。
     * <p>
     * 如果输入方法提供自己的窗口，应确保此时所有必要的窗口都已打开并可见。
     * <p>
     * 此方法在以下情况下调用：
     * <ul>
     * <li>由 {@link java.awt.im.InputContext#dispatchEvent InputContext.dispatchEvent}
     *     在客户端组件接收到 FOCUS_GAINED 事件时调用，
     * <li>通过用户界面或
     *     {@link java.awt.im.InputContext#selectInputMethod InputContext.selectInputMethod}
     *     从不同的输入方法切换到此输入方法时。
     * </ul>
     * 仅当输入方法处于非活动状态时才调用此方法。
     * 新实例化的输入方法假定为非活动状态。
     */
    public void activate();

    /**
     * 取消激活输入方法。
     * isTemporary 参数与
     * {@link java.awt.event.FocusEvent#isTemporary FocusEvent.isTemporary} 中的含义相同。
     * <p>
     * 如果输入方法提供自己的窗口，此时应仅关闭与当前组合相关的窗口（如查找选择窗口）。
     * 可能会立即为不同的客户端组件重新激活输入方法，关闭和重新打开更持久的窗口（如控制面板）
     * 会造成不必要的屏幕闪烁。
     * 在激活不同输入方法类的实例之前，会调用当前输入方法的 {@link #hideWindows}。
     * <p>
     * 此方法在以下情况下调用：
     * <ul>
     * <li>由 {@link java.awt.im.InputContext#dispatchEvent InputContext.dispatchEvent}
     *     在客户端组件接收到 FOCUS_LOST 事件时调用，
     * <li>通过用户界面或
     *     {@link java.awt.im.InputContext#selectInputMethod InputContext.selectInputMethod}
     *     从不同的输入方法切换到此输入方法时，
     * <li>在当前客户端组件被移除前调用 {@link #removeNotify removeNotify} 时。
     * </ul>
     * 仅当输入方法处于活动状态时才调用此方法。
     *
     * @param isTemporary 焦点变化是否为临时的
     */
    public void deactivate(boolean isTemporary);

    /**
     * 关闭或隐藏此输入方法实例或其类打开的所有窗口。
     * <p>
     * 此方法在以下情况下调用：
     * <ul>
     * <li>在调用不同输入方法类的实例的 {@link #activate activate} 之前，
     * <li>在调用此输入方法的 {@link #dispose dispose} 之前。
     * </ul>
     * 仅当输入方法处于非活动状态时才调用此方法。
     */
    public void hideWindows();

    /**
     * 通知输入方法客户端组件已从其包含层次结构中移除，或已禁用输入方法支持。
     * <p>
     * 此方法由 {@link java.awt.im.InputContext#removeNotify InputContext.removeNotify} 调用。
     * <p>
     * 仅当输入方法处于非活动状态时才调用此方法。
     */
    public void removeNotify();

    /**
     * 结束此上下文中可能正在进行的任何输入组合。根据平台和可能的用户偏好，
     * 这可能会提交或删除未提交的文本。任何文本的更改都会通过输入方法事件通知活动组件。
     *
     * <p>
     * 文本编辑组件可能在各种情况下调用此方法，例如，当用户在文本中移动插入点（但不在组合文本内）时，
     * 或当组件的文本保存到文件或复制到剪贴板时。
     * <p>
     * 此方法在以下情况下调用：
     * <ul>
     * <li>由 {@link java.awt.im.InputContext#endComposition InputContext.endComposition} 调用，
     * <li>由 {@link java.awt.im.InputContext#dispatchEvent InputContext.dispatchEvent}
     *     在切换到不同的客户端组件时调用，
     * <li>通过用户界面或
     *     {@link java.awt.im.InputContext#selectInputMethod InputContext.selectInputMethod}
     *     从不同的输入方法切换到此输入方法时。
     * </ul>
     */
    public void endComposition();


                /**
     * 释放此输入方法使用的资源。
     * 特别是，输入方法应该释放不再需要的窗口和关闭文件。
     * <p>
     * 此方法由 {@link java.awt.im.InputContext#dispose InputContext.dispose} 调用。
     * <p>
     * 仅当输入方法处于非活动状态时，此方法才会被调用。
     * 在调用 dispose 之后，不会在此实例上调用此接口的任何方法。
     */
    public void dispose();

    /**
     * 从此输入方法返回一个控制对象，或返回 null。控制对象提供控制输入方法行为或从输入方法获取信息的方法。
     * 对象的类型是特定于输入方法的类。客户端必须将结果与已知的输入方法控制对象类进行比较，并转换为适当的类以调用提供的方法。
     * <p>
     * 此方法由
     * {@link java.awt.im.InputContext#getInputMethodControlObject InputContext.getInputMethodControlObject} 调用。
     *
     * @return 从此输入方法返回的控制对象，或 null
     */
    public Object getControlObject();

}
