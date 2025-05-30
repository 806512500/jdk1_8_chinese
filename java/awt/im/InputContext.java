/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.im;

import java.awt.Component;
import java.util.Locale;
import java.awt.AWTEvent;
import java.beans.Transient;
import java.lang.Character.Subset;
import sun.awt.im.InputMethodContext;

/**
 * 提供控制文本输入设施（如输入法和键盘布局）的方法。
 * 两个方法同时处理输入法和键盘布局：selectInputMethod
 * 让客户端组件根据区域设置选择输入法或键盘布局，
 * getLocale 让客户端组件获取当前输入法或键盘布局的区域设置。
 * 其他方法更具体地支持与输入法的交互：
 * 它们让客户端组件控制输入法的行为，并将事件从客户端组件分派到输入法。
 *
 * <p>
 * 默认情况下，每个 Window 实例创建一个 InputContext 实例，
 * 并且该输入上下文由窗口容器层次结构中的所有组件共享。但是，这意味着在一个窗口内一次只能进行一个文本输入操作，
 * 并且当焦点从一个文本组件移动到另一个文本组件时，需要提交文本。如果这不是所希望的，
 * 文本组件可以创建自己的输入上下文实例。
 *
 * <p>
 * Java 平台支持使用 {@link java.awt.im.spi} 包中的接口开发并安装为 Java SE 运行时环境扩展的输入法。
 * 实现也可能支持使用其运行平台上提供的原生输入法；
 * 但是，并非所有平台和区域设置都提供输入法。键盘布局由主机平台提供。
 *
 * <p>
 * 如果 (a) 没有安装用 Java 编程语言编写的输入法，且 (b) Java 平台实现或底层平台不支持原生输入法，
 * 则输入法不可用。在这种情况下，仍然可以创建和使用输入上下文；
 * 其行为在下面的各个方法中指定。
 *
 * @see java.awt.Component#getInputContext
 * @see java.awt.Component#enableInputMethods
 * @author JavaSoft Asia/Pacific
 * @since 1.2
 */

public class InputContext {

    /**
     * 构造 InputContext。
     * 此方法受保护，因此客户端不能直接实例化 InputContext。
     * 通过调用 {@link #getInstance} 获取输入上下文。
     */
    protected InputContext() {
        // 实际实现位于 sun.awt.im.InputContext 中
    }

    /**
     * 返回一个新的 InputContext 实例。
     */
    public static InputContext getInstance() {
        return new sun.awt.im.InputMethodContext();
    }

    /**
     * 尝试选择支持给定区域设置的输入法或键盘布局，并返回一个值，指示是否成功选择了这样的输入法或键盘布局。
     * 以下步骤将依次执行，直到选择了一个输入法：
     *
     * <ul>
     * <li>
     * 如果当前选择的输入法或键盘布局支持请求的区域设置，则保持选择状态。</li>
     *
     * <li>
     * 如果没有可用的输入法或键盘布局支持请求的区域设置，则保持当前输入法或键盘布局的选择状态。</li>
     *
     * <li>
     * 如果用户之前从用户界面为请求的区域设置选择了输入法或键盘布局，则重新选择最近选择的输入法或键盘布局。</li>
     *
     * <li>
     * 否则，以实现依赖的方式选择支持请求区域设置的输入法或键盘布局。</li>
     *
     * </ul>
     * 在切换输入法之前，任何未提交的文本都会被提交。如果没有任何支持请求区域设置的输入法或键盘布局可用，
     * 则返回 false。
     *
     * <p>
     * 并非所有主机操作系统都提供 API 来确定当前选择的原生输入法或键盘布局的区域设置，以及按区域设置选择原生输入法或键盘布局。
     * 对于不提供此类 API 的主机操作系统，<code>selectInputMethod</code> 假设主机操作系统提供的原生输入法或键盘布局仅支持系统的默认区域设置。
     *
     * <p>
     * 例如，当用户更改插入点时，文本编辑组件可以调用此方法，以便用户可以立即继续输入周围文本的语言。
     *
     * @param locale 所需的新区域设置。
     * @return 如果调用后活动的输入法或键盘布局支持所需的区域设置，则返回 true。
     * @exception NullPointerException 如果 <code>locale</code> 为 null
     */
    public boolean selectInputMethod(Locale locale) {
        // 实际实现位于 sun.awt.im.InputContext 中
        return false;
    }

    /**
     * 返回当前输入法或键盘布局的当前区域设置。
     * 如果输入上下文没有当前输入法或键盘布局，或者当前输入法的
     * {@link java.awt.im.spi.InputMethod#getLocale()} 方法返回 null，则返回 null。
     *
     * <p>
     * 并非所有主机操作系统都提供 API 来确定当前选择的原生输入法或键盘布局的区域设置。
     * 对于不提供此类 API 的主机操作系统，<code>getLocale</code> 假设主机操作系统提供的所有原生输入法或键盘布局的当前区域设置是系统的默认区域设置。
     *
     * @return 当前输入法或键盘布局的当前区域设置
     * @since 1.3
     */
    public Locale getLocale() {
        // 实际实现位于 sun.awt.im.InputContext 中
        return null;
    }

    /**
     * 设置此输入上下文的输入法可以输入的 Unicode 字符集的子集。可以传递 null 以表示允许所有字符。
     * 初始值为 null。此设置适用于当前输入法以及在此调用后选择的输入法。
     * 但是，应用程序不能依赖此调用具有预期效果，因为此设置不能传递给所有主机输入法——
     * 应用程序仍需要应用自己的字符验证。如果没有可用的输入法，则此方法没有效果。
     *
     * @param subsets 可以输入的 Unicode 字符集的子集
     */
    public void setCharacterSubsets(Subset[] subsets) {
        // 实际实现位于 sun.awt.im.InputContext 中
    }

    /**
     * 根据参数 <code>enable</code> 的值启用或禁用当前输入法的组合。
     * <p>
     * 启用组合的输入法将传入的事件解释为组合和控制目的，而禁用组合的输入法则不解释事件以进行组合。
     * 但是，无论输入法是否启用，事件都会传递给输入法，而禁用组合的输入法仍可能解释事件以进行控制目的，
     * 包括启用或禁用自身以进行组合。
     * <p>
     * 对于主机操作系统提供的输入法，有时无法确定此操作是否受支持。例如，输入法可能仅对某些区域设置启用组合，
     * 而对其他区域设置则不执行任何操作。对于此类输入法，此方法可能不会抛出
     * {@link java.lang.UnsupportedOperationException UnsupportedOperationException}，
     * 但也不会影响组合是否启用。
     *
     * @param enable 是否启用当前输入法的组合
     * @throws UnsupportedOperationException 如果没有当前输入法可用或当前输入法不支持启用/禁用操作
     * @see #isCompositionEnabled
     * @since 1.3
     */
    public void setCompositionEnabled(boolean enable) {
        // 实际实现位于 sun.awt.im.InputContext 中
    }

    /**
     * 确定当前输入法是否已启用组合。
     * 启用组合的输入法将传入的事件解释为组合和控制目的，而禁用组合的输入法则不解释事件以进行组合。
     *
     * @return 如果当前输入法已启用组合，则返回 <code>true</code>；否则返回 <code>false</code>
     * @throws UnsupportedOperationException 如果没有当前输入法可用或当前输入法不支持检查是否启用组合
     * @see #setCompositionEnabled
     * @since 1.3
     */
    @Transient
    public boolean isCompositionEnabled() {
        // 实际实现位于 sun.awt.im.InputContext 中
        return false;
    }

    /**
     * 要求当前输入法从当前客户端组件重新转换文本。输入法使用
     * {@link InputMethodRequests#getSelectedText InputMethodRequests.getSelectedText}
     * 方法从客户端组件获取要重新转换的文本。其他 <code>InputMethodRequests</code> 方法
     * 必须准备处理输入法的进一步信息请求。组合和/或提交的文本将作为一系列
     * <code>InputMethodEvent</code> 发送到客户端组件。如果输入法无法重新转换给定的文本，
     * 则文本将以提交文本的形式在 <code>InputMethodEvent</code> 中返回。
     *
     * @throws UnsupportedOperationException 如果没有当前输入法可用或当前输入法不支持重新转换操作。
     *
     * @since 1.3
     */
    public void reconvert() {
        // 实际实现位于 sun.awt.im.InputContext 中
    }

    /**
     * 将事件分派给活动的输入法。由 AWT 调用。
     * 如果没有可用的输入法，则事件将永远不会被消费。
     *
     * @param event 事件
     * @exception NullPointerException 如果 <code>event</code> 为 null
     */
    public void dispatchEvent(AWTEvent event) {
        // 实际实现位于 sun.awt.im.InputContext 中
    }

    /**
     * 通知输入上下文客户端组件已从其包含层次结构中移除，或已禁用输入法支持。
     * 通常从客户端组件的
     * {@link java.awt.Component#removeNotify() Component.removeNotify}
     * 方法调用此方法。此组件的输入法可能挂起的输入将被丢弃。
     * 如果没有可用的输入法，则此方法没有效果。
     *
     * @param client 客户端组件
     * @exception NullPointerException 如果 <code>client</code> 为 null
     */
    public void removeNotify(Component client) {
        // 实际实现位于 sun.awt.im.InputContext 中
    }

    /**
     * 结束此上下文中可能正在进行的任何输入组合。根据平台和可能的用户偏好，
     * 这可能会提交或删除未提交的文本。任何文本的更改都将通过输入法事件通知活动组件。
     * 如果没有可用的输入法，则此方法没有效果。
     *
     * <p>
     * 文本编辑组件可能在各种情况下调用此方法，例如，当用户在文本中移动插入点（但不在组合文本之外）时，
     * 或者当组件的文本保存到文件或复制到剪贴板时。
     *
     */
    public void endComposition() {
        // 实际实现位于 sun.awt.im.InputContext 中
    }

    /**
     * 释放此输入上下文使用的资源。
     * 由 AWT 为每个 Window 的默认输入上下文调用。
     * 如果没有可用的输入法，则此方法没有效果。
     */
    public void dispose() {
        // 实际实现位于 sun.awt.im.InputContext 中
    }

    /**
     * 从当前输入法返回一个控制对象，或返回 null。控制对象提供控制输入法行为或从输入法获取信息的方法。
     * 对象的类型是输入法特定的类。客户端必须将结果与已知的输入法控制对象类进行比较，并转换为适当的类以调用提供的方法。
     * <p>
     * 如果没有可用的输入法或当前输入法不提供输入法控制对象，则返回 null。
     *
     * @return 当前输入法的控制对象，或 null。
     */
    public Object getInputMethodControlObject() {
        // 实际实现位于 sun.awt.im.InputContext 中
        return null;
    }

}
