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

package java.awt;

import java.security.BasicPermission;

/**
 * 本类用于 AWT 权限。
 * 一个 <code>AWTPermission</code> 包含一个目标名称，但没有操作列表；你拥有命名的权限或没有。
 *
 * <P>
 * 目标名称是 AWT 权限的名称（见下文）。命名约定遵循层次属性命名约定。
 * 另外，星号可以代表所有 AWT 权限。
 *
 * <P>
 * 下表列出了所有可能的 <code>AWTPermission</code>
 * 目标名称，并为每个名称提供了一个描述，说明该权限允许的内容以及授予代码该权限的风险。
 *
 * <table border=1 cellpadding=5 summary="AWTPermission 目标名称、描述及其相关风险。">
 * <tr>
 * <th>权限目标名称</th>
 * <th>权限允许的内容</th>
 * <th>允许此权限的风险</th>
 * </tr>
 *
 * <tr>
 *   <td>accessClipboard</td>
 *   <td>向 AWT 剪贴板发布和检索信息</td>
 *   <td>这将允许恶意代码共享
 * 潜在敏感或机密的信息。</td>
 * </tr>
 *
 * <tr>
 *   <td>accessEventQueue</td>
 *   <td>访问 AWT 事件队列</td>
 *   <td>检索 AWT 事件队列后，
 * 恶意代码可以查看甚至删除事件队列中的现有事件，
 * 以及发布虚假事件，这些事件可能会故意
 * 导致应用程序或小程序以不安全的方式行为异常。</td>
 * </tr>
 *
 * <tr>
 *   <td>accessSystemTray</td>
 *   <td>访问 AWT SystemTray 实例</td>
 *   <td>这将允许恶意代码向系统托盘添加托盘图标。
 * 首先，这样的图标可能看起来像某些已知应用程序的图标
 * （例如防火墙或防病毒软件），并指示用户执行不安全的操作
 * （借助气球消息）。其次，系统托盘可能会被
 * 托盘图标塞满，以至于没有人能再添加托盘图标。</td>
 * </tr>
 *
 * <tr>
 *   <td>createRobot</td>
 *   <td>创建 java.awt.Robot 对象</td>
 *   <td>java.awt.Robot 对象允许代码生成本机级别的
 * 鼠标和键盘事件以及读取屏幕。它可能允许
 * 恶意代码控制系统，运行其他程序，读取
 * 显示内容，并拒绝用户使用鼠标和键盘。</td>
 * </tr>
 *
 * <tr>
 *   <td>fullScreenExclusive</td>
 *   <td>进入全屏独占模式</td>
 *   <td>进入全屏独占模式允许直接访问
 * 低级显卡内存。这可以用来欺骗
 * 系统，因为程序直接控制渲染。根据
 * 实现，进入全屏独占模式的窗口
 * 可能不会显示安全警告（假设已授予 {@code
 * fullScreenExclusive} 权限）。请注意，
 * 这种行为并不意味着将自动授予
 * 拥有 {@code fullScreenExclusive} 权限的应用程序
 * {@code showWindowWithoutWarningBanner} 权限：
 * 非全屏窗口将继续显示安全警告。</td>
 * </tr>
 *
 * <tr>
 *   <td>listenToAllAWTEvents</td>
 *   <td>监听系统范围内的所有 AWT 事件</td>
 *   <td>添加 AWT 事件监听器后，
 * 恶意代码可以扫描系统中分发的所有 AWT 事件，
 * 允许其读取所有用户输入（如密码）。每个
 * AWT 事件监听器都在该事件队列的 EventDispatchThread
 * 上下文中调用，因此如果启用了 accessEventQueue
 * 权限，恶意代码可以修改
 * 系统范围内的 AWT 事件队列的内容，导致应用程序
 * 或小程序以不安全的方式行为异常。</td>
 * </tr>
 *
 * <tr>
 *   <td>readDisplayPixels</td>
 *   <td>从显示屏幕读取像素</td>
 *   <td>如 java.awt.Composite 接口或
 * java.awt.Robot 类等接口允许任意代码检查显示内容
 * 使恶意代码能够窥探用户的活动。</td>
 * </tr>
 *
 * <tr>
 *   <td>replaceKeyboardFocusManager</td>
 *   <td>为特定线程设置 <code>KeyboardFocusManager</code>
 *   <td>当安装了 <code>SecurityManager</code> 时，调用
 * 线程必须被授予此权限才能替换
 * 当前的 <code>KeyboardFocusManager</code>。如果未授予权限，
 * 将抛出 <code>SecurityException</code>。</td>
 * </tr>
 *
 * <tr>
 *   <td>setAppletStub</td>
 *   <td>设置实现 Applet 容器服务的存根</td>
 *   <td>恶意代码可以设置小程序的存根，导致意外
 * 行为或对小程序的拒绝服务。</td>
 * </tr>
 *
 * <tr>
 *   <td>setWindowAlwaysOnTop</td>
 *   <td>设置窗口的始终置顶属性：{@link Window#setAlwaysOnTop}</td>
 *   <td>恶意窗口可能会使其看起来和行为像一个真实的全桌面，以便
 * 使不知情的用户输入的信息被捕获并随后被滥用。</td>
 * </tr>
 *
 * <tr>
 *   <td>showWindowWithoutWarningBanner</td>
 *   <td>显示窗口时不显示警告横幅
 * 表明该窗口是由小程序创建的</td>
 *   <td>没有此警告，
 * 小程序可以弹出窗口，而用户不知道它们
 * 属于小程序。由于用户可能会根据窗口是否属于小程序
 * 作出安全敏感的决策（例如在对话框中输入用户名和密码），
 * 禁用此警告横幅可能会使小程序欺骗用户
 * 输入此类信息。</td>
 * </tr>
 *
 * <tr>
 *   <td>toolkitModality</td>
 *   <td>创建 {@link Dialog.ModalityType#TOOLKIT_MODAL TOOLKIT_MODAL} 对话框
 *       和设置 {@link Dialog.ModalExclusionType#TOOLKIT_EXCLUDE
 *       TOOLKIT_EXCLUDE} 窗口属性。</td>
 *   <td>当小程序显示工具包模态对话框时，它会阻止浏览器中的所有其他
 * 小程序。从 Java Web Start 启动应用程序时，
 * 其窗口（如安全对话框）也可能被工具包模态
 * 对话框阻止，这些对话框是从这些应用程序中显示的。</td>
 * </tr>
 *
 * <tr>
 *   <td>watchMousePointer</td>
 *   <td>随时获取鼠标指针位置的信息</td>
 *   <td>不断监视鼠标指针，
 * 小程序可以猜测用户正在做什么，例如将鼠标移动到屏幕的左下角
 * 很可能意味着用户即将启动应用程序。如果使用虚拟键盘
 * 使键盘使用鼠标模拟，小程序可以猜测
 * 正在输入的内容。</td>
 * </tr>
 * </table>
 *
 * @see java.security.BasicPermission
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 *
 * @author Marianne Mueller
 * @author Roland Schemers
 */

public final class AWTPermission extends BasicPermission {

    /** 使用 Java 2 平台的 serialVersionUID 以实现互操作性 */
    private static final long serialVersionUID = 8890392402588814465L;

    /**
     * 创建一个具有指定名称的新 <code>AWTPermission</code>。
     * 名称是 <code>AWTPermission</code> 的符号名称，
     * 例如 "topLevelWindow"，"systemClipboard" 等。星号
     * 可以表示所有 AWT 权限。
     *
     * @param name AWTPermission 的名称
     *
     * @throws NullPointerException 如果 <code>name</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果 <code>name</code> 为空。
     */

    public AWTPermission(String name)
    {
        super(name);
    }

    /**
     * 创建一个具有指定名称的新 <code>AWTPermission</code> 对象。
     * 名称是 <code>AWTPermission</code> 的符号名称，操作字符串目前未使用，应为 <code>null</code>。
     *
     * @param name <code>AWTPermission</code> 的名称
     * @param actions 应为 <code>null</code>
     *
     * @throws NullPointerException 如果 <code>name</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果 <code>name</code> 为空。
     */

    public AWTPermission(String name, String actions)
    {
        super(name, actions);
    }
}
