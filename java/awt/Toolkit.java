
/*
 * Copyright (c) 1995, 2017, Oracle and/or its affiliates. All rights reserved.
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

import java.beans.PropertyChangeEvent;
import java.awt.event.*;
import java.awt.peer.*;
import java.awt.im.InputMethodHighlight;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.ColorModel;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;

import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import sun.awt.AppContext;

import sun.awt.HeadlessToolkit;
import sun.awt.NullComponentPeer;
import sun.awt.PeerEvent;
import sun.awt.SunToolkit;
import sun.awt.AWTAccessor;
import sun.security.util.SecurityConstants;

import sun.util.CoreResourceBundleControl;

/**
 * 该类是所有实际实现抽象窗口工具包的抽象超类。抽象窗口工具包的子类用于将各种组件绑定到特定的本机工具包实现。
 * <p>
 * 许多 GUI 事件可能异步地传递给用户，除非明确指定为同步。
 * 同样地
 * 许多 GUI 操作可能异步执行。
 * 这意味着如果设置了组件的状态，然后立即查询该状态，返回的值可能尚未反映请求的更改。此行为包括但不限于：
 * <ul>
 * <li>滚动到指定位置。
 * <br>例如，调用 <code>ScrollPane.setScrollPosition</code>
 *     然后调用 <code>getScrollPosition</code> 可能返回不正确的
 *     值，如果原始请求尚未处理。
 *
 * <li>将焦点从一个组件移动到另一个组件。
 * <br>有关更多信息，请参阅
 * <a href="https://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html#transferTiming">焦点转移时机</a>，这是
 * <a href="http://java.sun.com/docs/books/tutorial/uiswing/">Swing 教程</a> 中的一部分。
 *
 * <li>使顶级容器可见。
 * <br>在 <code>Window</code>、<code>Frame</code> 或 <code>Dialog</code> 上调用 <code>setVisible(true)</code> 可能异步发生。
 *
 * <li>设置顶级容器的大小或位置。
 * <br>对 <code>Window</code>、<code>Frame</code> 或 <code>Dialog</code> 的 <code>setSize</code>、<code>setBounds</code> 或
 *     <code>setLocation</code> 的调用将转发给底层窗口管理系统，并可能被忽略或修改。有关更多信息，请参阅 {@link java.awt.Window}。
 * </ul>
 * <p>
 * 大多数应用程序不应直接调用此类中的任何方法。由 <code>Toolkit</code> 定义的方法是将 <code>java.awt</code> 包中的平台无关类与其在
 * <code>java.awt.peer</code> 包中的对应类连接起来的“胶水”。一些由 <code>Toolkit</code> 定义的方法直接查询本机操作系统。
 *
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 * @author      Fred Ecks
 * @since       JDK1.0
 */
public abstract class Toolkit {

    /**
     * 使用指定的对等接口创建此工具包的 <code>Desktop</code> 实现。
     * @param     target 要实现的桌面。
     * @return    此工具包的 <code>Desktop</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.Desktop
     * @see       java.awt.peer.DesktopPeer
     * @since 1.6
     */
    protected abstract DesktopPeer createDesktopPeer(Desktop target)
      throws HeadlessException;


    /**
     * 使用指定的对等接口创建此工具包的 <code>Button</code> 实现。
     * @param     target 要实现的按钮。
     * @return    此工具包的 <code>Button</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.Button
     * @see       java.awt.peer.ButtonPeer
     */
    protected abstract ButtonPeer createButton(Button target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>TextField</code> 实现。
     * @param     target 要实现的文本字段。
     * @return    此工具包的 <code>TextField</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.TextField
     * @see       java.awt.peer.TextFieldPeer
     */
    protected abstract TextFieldPeer createTextField(TextField target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>Label</code> 实现。
     * @param     target 要实现的标签。
     * @return    此工具包的 <code>Label</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.Label
     * @see       java.awt.peer.LabelPeer
     */
    protected abstract LabelPeer createLabel(Label target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>List</code> 实现。
     * @param     target 要实现的列表。
     * @return    此工具包的 <code>List</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.List
     * @see       java.awt.peer.ListPeer
     */
    protected abstract ListPeer createList(java.awt.List target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>Checkbox</code> 实现。
     * @param     target 要实现的复选框。
     * @return    此工具包的 <code>Checkbox</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.Checkbox
     * @see       java.awt.peer.CheckboxPeer
     */
    protected abstract CheckboxPeer createCheckbox(Checkbox target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>Scrollbar</code> 实现。
     * @param     target 要实现的滚动条。
     * @return    此工具包的 <code>Scrollbar</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.Scrollbar
     * @see       java.awt.peer.ScrollbarPeer
     */
    protected abstract ScrollbarPeer createScrollbar(Scrollbar target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>ScrollPane</code> 实现。
     * @param     target 要实现的滚动窗格。
     * @return    此工具包的 <code>ScrollPane</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.ScrollPane
     * @see       java.awt.peer.ScrollPanePeer
     * @since     JDK1.1
     */
    protected abstract ScrollPanePeer createScrollPane(ScrollPane target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>TextArea</code> 实现。
     * @param     target 要实现的文本区域。
     * @return    此工具包的 <code>TextArea</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.TextArea
     * @see       java.awt.peer.TextAreaPeer
     */
    protected abstract TextAreaPeer createTextArea(TextArea target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>Choice</code> 实现。
     * @param     target 要实现的选择框。
     * @return    此工具包的 <code>Choice</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.Choice
     * @see       java.awt.peer.ChoicePeer
     */
    protected abstract ChoicePeer createChoice(Choice target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>Frame</code> 实现。
     * @param     target 要实现的框架。
     * @return    此工具包的 <code>Frame</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.Frame
     * @see       java.awt.peer.FramePeer
     */
    protected abstract FramePeer createFrame(Frame target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>Canvas</code> 实现。
     * @param     target 要实现的画布。
     * @return    此工具包的 <code>Canvas</code> 实现。
     * @see       java.awt.Canvas
     * @see       java.awt.peer.CanvasPeer
     */
    protected abstract CanvasPeer       createCanvas(Canvas target);

    /**
     * 使用指定的对等接口创建此工具包的 <code>Panel</code> 实现。
     * @param     target 要实现的面板。
     * @return    此工具包的 <code>Panel</code> 实现。
     * @see       java.awt.Panel
     * @see       java.awt.peer.PanelPeer
     */
    protected abstract PanelPeer        createPanel(Panel target);

    /**
     * 使用指定的对等接口创建此工具包的 <code>Window</code> 实现。
     * @param     target 要实现的窗口。
     * @return    此工具包的 <code>Window</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.Window
     * @see       java.awt.peer.WindowPeer
     */
    protected abstract WindowPeer createWindow(Window target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>Dialog</code> 实现。
     * @param     target 要实现的对话框。
     * @return    此工具包的 <code>Dialog</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.Dialog
     * @see       java.awt.peer.DialogPeer
     */
    protected abstract DialogPeer createDialog(Dialog target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>MenuBar</code> 实现。
     * @param     target 要实现的菜单栏。
     * @return    此工具包的 <code>MenuBar</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.MenuBar
     * @see       java.awt.peer.MenuBarPeer
     */
    protected abstract MenuBarPeer createMenuBar(MenuBar target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>Menu</code> 实现。
     * @param     target 要实现的菜单。
     * @return    此工具包的 <code>Menu</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.Menu
     * @see       java.awt.peer.MenuPeer
     */
    protected abstract MenuPeer createMenu(Menu target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>PopupMenu</code> 实现。
     * @param     target 要实现的弹出菜单。
     * @return    此工具包的 <code>PopupMenu</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.PopupMenu
     * @see       java.awt.peer.PopupMenuPeer
     * @since     JDK1.1
     */
    protected abstract PopupMenuPeer createPopupMenu(PopupMenu target)
        throws HeadlessException;

    /**
     * 使用指定的对等接口创建此工具包的 <code>MenuItem</code> 实现。
     * @param     target 要实现的菜单项。
     * @return    此工具包的 <code>MenuItem</code> 实现。
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
     * 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.MenuItem
     * @see       java.awt.peer.MenuItemPeer
     */
    protected abstract MenuItemPeer createMenuItem(MenuItem target)
        throws HeadlessException;


                /**
                 * 创建此工具包的 <code>FileDialog</code> 实现，使用
                 * 指定的对等接口。
                 * @param     target 要实现的文件对话框。
                 * @return    此工具包的 <code>FileDialog</code> 实现。
                 * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
                 * 返回 true
                 * @see       java.awt.GraphicsEnvironment#isHeadless
                 * @see       java.awt.FileDialog
                 * @see       java.awt.peer.FileDialogPeer
                 */
                protected abstract FileDialogPeer createFileDialog(FileDialog target)
                    throws HeadlessException;

                /**
                 * 创建此工具包的 <code>CheckboxMenuItem</code> 实现，使用
                 * 指定的对等接口。
                 * @param     target 要实现的复选菜单项。
                 * @return    此工具包的 <code>CheckboxMenuItem</code> 实现。
                 * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
                 * 返回 true
                 * @see       java.awt.GraphicsEnvironment#isHeadless
                 * @see       java.awt.CheckboxMenuItem
                 * @see       java.awt.peer.CheckboxMenuItemPeer
                 */
                protected abstract CheckboxMenuItemPeer createCheckboxMenuItem(
                    CheckboxMenuItem target) throws HeadlessException;

                /**
                 * 获取此工具包的 <code>MouseInfo</code> 操作的辅助类实现。
                 * @return    此工具包的 <code>MouseInfo</code> 辅助类实现
                 * @throws    UnsupportedOperationException 如果此操作未实现
                 * @see       java.awt.peer.MouseInfoPeer
                 * @see       java.awt.MouseInfo
                 * @since 1.5
                 */
                protected MouseInfoPeer getMouseInfoPeer() {
                    throw new UnsupportedOperationException("未实现");
                }

                private static LightweightPeer lightweightMarker;

                /**
                 * 为组件或容器创建一个对等对象。此对等对象是无窗口的，
                 * 允许 Component 和 Container 类直接扩展以创建完全用 Java 定义的无窗口组件。
                 *
                 * @param target 要创建的组件。
                 */
                protected LightweightPeer createComponent(Component target) {
                    if (lightweightMarker == null) {
                        lightweightMarker = new NullComponentPeer();
                    }
                    return lightweightMarker;
                }

                /**
                 * 使用指定的对等接口创建此工具包的 <code>Font</code> 实现。
                 * @param     name 要实现的字体
                 * @param     style 字体的样式，如 <code>PLAIN</code>、
                 *            <code>BOLD</code>、<code>ITALIC</code> 或其组合
                 * @return    此工具包的 <code>Font</code> 实现
                 * @see       java.awt.Font
                 * @see       java.awt.peer.FontPeer
                 * @see       java.awt.GraphicsEnvironment#getAllFonts
                 * @deprecated 请参见 java.awt.GraphicsEnvironment#getAllFonts
                 */
                @Deprecated
                protected abstract FontPeer getFontPeer(String name, int style);

                // 下列方法由 <code>SystemColor</code> 中的私有方法
                // <code>updateSystemColors</code> 调用。

                /**
                 * 用当前系统颜色值填充作为参数提供的整数数组。
                 *
                 * @param     systemColors 一个整数数组。
                 * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
                 * 返回 true
                 * @see       java.awt.GraphicsEnvironment#isHeadless
                 * @since     JDK1.1
                 */
                protected void loadSystemColors(int[] systemColors)
                    throws HeadlessException {
                    GraphicsEnvironment.checkHeadless();
                }

                /**
                 * 控制容器在调整大小期间的布局是动态验证还是在调整大小完成后静态验证。
                 * 使用 {@code isDynamicLayoutActive()} 检查此功能是否在程序中启用
                 * 并且是否受此操作系统和/或窗口管理器支持。
                 * 请注意，此功能并非所有平台都支持，而且有些平台
                 * 无法关闭此功能。在这些不支持动态布局的平台（或总是支持动态布局的平台）上，
                 * 设置此属性没有效果。
                 * 请注意，此功能可以在某些平台上作为操作系统的属性或窗口管理器的属性设置或取消设置。
                 * 在这些平台上，必须在操作系统或窗口管理器级别设置动态调整大小属性，
                 * 然后此方法才能生效。
                 * 此方法不会更改底层操作系统或
                 * 窗口管理器的支持或设置。可以使用 getDesktopProperty("awt.dynamicLayoutSupported") 方法查询 OS/WM 支持。
                 *
                 * @param     dynamic 如果为 true，则在调整容器大小时应重新布局其组件。如果为 false，
                 *            则在调整大小完成后验证布局。
                 * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
                 *            返回 true
                 * @see       #isDynamicLayoutSet()
                 * @see       #isDynamicLayoutActive()
                 * @see       #getDesktopProperty(String propertyName)
                 * @see       java.awt.GraphicsEnvironment#isHeadless
                 * @since     1.4
                 */
                public void setDynamicLayout(final boolean dynamic)
                    throws HeadlessException {
                    GraphicsEnvironment.checkHeadless();
                    if (this != getDefaultToolkit()) {
                        getDefaultToolkit().setDynamicLayout(dynamic);
                    }
                }

                /**
                 * 返回容器的布局是在调整大小期间动态验证还是在调整大小完成后静态验证。
                 * 注意：此方法返回的是程序中设置的值；
                 * 它不反映操作系统或窗口管理器对调整大小时动态布局的支持，或当前
                 * 操作系统或窗口管理器的设置。可以使用 getDesktopProperty("awt.dynamicLayoutSupported") 方法查询 OS/WM 支持。
                 *
                 * @return    如果容器的验证是在调整大小时动态进行的，则返回 true，
                 *            如果验证是在调整大小完成后进行的，则返回 false。
                 * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
                 *            返回 true
                 * @see       #setDynamicLayout(boolean dynamic)
                 * @see       #isDynamicLayoutActive()
                 * @see       #getDesktopProperty(String propertyName)
                 * @see       java.awt.GraphicsEnvironment#isHeadless
                 * @since     1.4
                 */
                protected boolean isDynamicLayoutSet()
                    throws HeadlessException {
                    GraphicsEnvironment.checkHeadless();

                    if (this != Toolkit.getDefaultToolkit()) {
                        return Toolkit.getDefaultToolkit().isDynamicLayoutSet();
                    } else {
                        return false;
                    }
                }

                /**
                 * 返回容器在调整大小时的动态布局是否
                 * 当前处于活动状态（既在程序中设置
                 * ({@code isDynamicLayoutSet()} )，
                 * 也受底层操作系统和/或窗口管理器支持）。
                 * 如果动态布局当前未激活，则容器
                 * 在调整大小完成后重新布局其组件。结果
                 * {@code Component.validate()} 方法将仅在每次调整大小时调用一次。
                 * 如果动态布局当前处于活动状态，则容器
                 * 在每次本机调整大小事件时重新布局其组件，
                 * 并且每次都会调用 {@code validate()} 方法。
                 * 可以使用
                 * getDesktopProperty("awt.dynamicLayoutSupported") 方法查询 OS/WM 支持。
                 *
                 * @return    如果容器在调整大小时的动态布局当前处于活动状态，则返回 true，否则返回 false。
                 * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
                 *            方法返回 true
                 * @see       #setDynamicLayout(boolean dynamic)
                 * @see       #isDynamicLayoutSet()
                 * @see       #getDesktopProperty(String propertyName)
                 * @see       java.awt.GraphicsEnvironment#isHeadless
                 * @since     1.4
                 */
                public boolean isDynamicLayoutActive()
                    throws HeadlessException {
                    GraphicsEnvironment.checkHeadless();

                    if (this != Toolkit.getDefaultToolkit()) {
                        return Toolkit.getDefaultToolkit().isDynamicLayoutActive();
                    } else {
                        return false;
                    }
                }

                /**
                 * 获取屏幕的大小。在具有多个显示器的系统上，使用主显示器。多屏幕感知的显示尺寸
                 * 可从 <code>GraphicsConfiguration</code> 和
                 * <code>GraphicsDevice</code> 获得。
                 * @return    此工具包的屏幕大小，以像素为单位。
                 * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
                 * 返回 true
                 * @see       java.awt.GraphicsConfiguration#getBounds
                 * @see       java.awt.GraphicsDevice#getDisplayMode
                 * @see       java.awt.GraphicsEnvironment#isHeadless
                 */
                public abstract Dimension getScreenSize()
                    throws HeadlessException;

                /**
                 * 返回屏幕分辨率，以每英寸点数为单位。
                 * @return    此工具包的屏幕分辨率，以每英寸点数为单位。
                 * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
                 * 返回 true
                 * @see       java.awt.GraphicsEnvironment#isHeadless
                 */
                public abstract int getScreenResolution()
                    throws HeadlessException;

                /**
                 * 获取屏幕的边距。
                 * @param     gc 一个 <code>GraphicsConfiguration</code>
                 * @return    此工具包的屏幕边距，以像素为单位。
                 * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
                 * 返回 true
                 * @see       java.awt.GraphicsEnvironment#isHeadless
                 * @since     1.4
                 */
                public Insets getScreenInsets(GraphicsConfiguration gc)
                    throws HeadlessException {
                    GraphicsEnvironment.checkHeadless();
                    if (this != Toolkit.getDefaultToolkit()) {
                        return Toolkit.getDefaultToolkit().getScreenInsets(gc);
                    } else {
                        return new Insets(0, 0, 0, 0);
                    }
                }

                /**
                 * 确定此工具包屏幕的颜色模型。
                 * <p>
                 * <code>ColorModel</code> 是一个抽象类，封装了将
                 * 图像的像素值转换为其红色、绿色、蓝色和 alpha 分量的能力。
                 * <p>
                 * 此工具包方法由
                 * <code>Component</code> 类的 <code>getColorModel</code> 方法调用。
                 * @return    此工具包屏幕的颜色模型。
                 * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless()
                 * 返回 true
                 * @see       java.awt.GraphicsEnvironment#isHeadless
                 * @see       java.awt.image.ColorModel
                 * @see       java.awt.Component#getColorModel
                 */
                public abstract ColorModel getColorModel()
                    throws HeadlessException;

                /**
                 * 返回此工具包中可用字体的名称。<p>
                 * 对于 1.1，以下字体名称已弃用（替换名称如下）：
                 * <ul>
                 * <li>TimesRoman（使用 Serif）
                 * <li>Helvetica（使用 SansSerif）
                 * <li>Courier（使用 Monospaced）
                 * </ul><p>
                 * ZapfDingbats 字体名称在 1.1 中也已弃用，但字符
                 * 从 0x2700 开始定义在 Unicode 中，从 1.1 开始 Java 支持这些字符。
                 * @return    此工具包中可用字体的名称。
                 * @deprecated 请参见 {@link java.awt.GraphicsEnvironment#getAvailableFontFamilyNames()}
                 * @see java.awt.GraphicsEnvironment#getAvailableFontFamilyNames()
                 */
                @Deprecated
                public abstract String[] getFontList();

                /**
                 * 获取字体的屏幕设备度量。
                 * @param     font   一个字体
                 * @return    指定字体在此工具包中的屏幕度量
                 * @deprecated 从 JDK 1.2 版本开始，被 <code>Font</code>
                 *          方法 <code>getLineMetrics</code> 替换。
                 * @see java.awt.font.LineMetrics
                 * @see java.awt.Font#getLineMetrics
                 * @see java.awt.GraphicsEnvironment#getScreenDevices
                 */
                @Deprecated
                public abstract FontMetrics getFontMetrics(Font font);

                /**
                 * 同步此工具包的图形状态。某些窗口系统
                 * 可能会缓冲图形事件。
                 * <p>
                 * 此方法确保显示是最新的。对于动画很有用。
                 */
                public abstract void sync();

                /**
                 * 默认工具包。
                 */
                private static Toolkit toolkit;

                /**
                 * 由辅助技术功能内部使用；在初始化时设置并在加载时使用
                 */
                private static String atNames;

                /**
                 * 初始化与辅助技术相关的属性。
                 * 这些属性在 loadAssistiveProperties()
                 * 函数中以及依赖这些属性的其他 jdk 类中使用
                 * （例如，在 Java2D 硬件加速初始化中使用 screen_magnifier_present
                 * 属性）。必须在实例化平台特定的 Toolkit 类之前
                 * 初始化这些属性，以便在依赖这些属性的任何类初始化之前
                 * 正确设置所有必要的属性。
                 */
                private static void initAssistiveTechnologies() {

                    // 获取辅助功能属性
                    final String sep = File.separator;
                    final Properties properties = new Properties();


                    atNames = java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedAction<String>() {
                        public String run() {

                            // 尝试加载每个用户的辅助功能属性文件。
                            try {
                                File propsFile = new File(
                                  System.getProperty("user.home") +
                                  sep + ".accessibility.properties");
                                FileInputStream in =
                                    new FileInputStream(propsFile);

                                // 输入流已在 Properties 类中缓冲
                                properties.load(in);
                                in.close();
                            } catch (Exception e) {
                                // 每个用户的辅助功能属性文件不存在
                            }

                            // 如果每个用户的辅助功能属性文件不存在或为空，
                            // 则尝试加载系统范围的辅助功能属性文件。
                            if (properties.size() == 0) {
                                try {
                                    File propsFile = new File(
                                        System.getProperty("java.home") + sep + "lib" +
                                        sep + "accessibility.properties");
                                    FileInputStream in =
                                        new FileInputStream(propsFile);

                                    // 输入流已在 Properties 类中缓冲
                                    properties.load(in);
                                    in.close();
                                } catch (Exception e) {
                                    // 系统范围的辅助功能属性文件不存在；
                                }
                            }


                            // 获取是否存在屏幕放大器。首先检查
                // 系统属性，然后检查属性文件。
                String magPresent = System.getProperty("javax.accessibility.screen_magnifier_present");
                if (magPresent == null) {
                    magPresent = properties.getProperty("screen_magnifier_present", null);
                    if (magPresent != null) {
                        System.setProperty("javax.accessibility.screen_magnifier_present", magPresent);
                    }
                }

                // 获取要加载的辅助技术的名称。首先
                // 检查系统属性，然后检查属性
                // 文件。
                String classNames = System.getProperty("javax.accessibility.assistive_technologies");
                if (classNames == null) {
                    classNames = properties.getProperty("assistive_technologies", null);
                    if (classNames != null) {
                        System.setProperty("javax.accessibility.assistive_technologies", classNames);
                    }
                }
                return classNames;
            }
        });
    }

    /**
     * 加载额外的类到虚拟机中，使用 Sun 参考实现中
     * 'accessibility.properties' 文件中指定的 'assistive_technologies'
     * 属性。形式为 "assistive_technologies=..."，其中
     * "..." 是一个以逗号分隔的辅助技术类列表。每个类按给定的顺序加载
     * 并使用 Class.forName(class).newInstance() 创建一个实例。所有错误都通过
     * AWTError 异常处理。
     *
     * <p>假设辅助技术类是作为 INSTALLED（而不是 BUNDLED）扩展的一部分提供的，或者在类路径上指定
     * （因此可以使用调用 <code>ClassLoader.getSystemClassLoader</code> 返回的类加载器加载，
     * 其委托父类是已安装扩展的扩展类加载器）。
     */
    private static void loadAssistiveTechnologies() {
        // 加载任何辅助技术
        if (atNames != null) {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            StringTokenizer parser = new StringTokenizer(atNames," ,");
            String atName;
            while (parser.hasMoreTokens()) {
                atName = parser.nextToken();
                try {
                    Class<?> clazz;
                    if (cl != null) {
                        clazz = cl.loadClass(atName);
                    } else {
                        clazz = Class.forName(atName);
                    }
                    clazz.newInstance();
                } catch (ClassNotFoundException e) {
                    throw new AWTError("未找到辅助技术: "
                            + atName);
                } catch (InstantiationException e) {
                    throw new AWTError("无法实例化辅助技术: " + atName);
                } catch (IllegalAccessException e) {
                    throw new AWTError("无法访问辅助技术: " + atName);
                } catch (Exception e) {
                    throw new AWTError("尝试安装辅助技术时出错: " + atName + " " + e);
                }
            }
        }
    }

    /**
     * 获取默认的工具包。
     * <p>
     * 如果系统属性 <code>"java.awt.headless"</code> 设置为
     * <code>true</code>，则使用无头实现的 <code>Toolkit</code>。
     * <p>
     * 如果没有 <code>"java.awt.headless"</code> 或其值为
     * <code>false</code> 且存在系统属性 <code>"awt.toolkit"</code>，
     * 则该属性被视为 <code>Toolkit</code> 的子类的名称；
     * 否则使用默认的平台特定实现的 <code>Toolkit</code>。
     * <p>
     * 还会加载额外的类到虚拟机中，使用 Sun 参考实现中
     * 'accessibility.properties' 文件中指定的 'assistive_technologies'
     * 属性。形式为 "assistive_technologies=..."，其中
     * "..." 是一个以逗号分隔的辅助技术类列表。每个类按给定的顺序加载
     * 并使用 Class.forName(class).newInstance() 创建一个实例。这是在
     * AWT 工具包创建后立即完成的。所有错误都通过 AWTError 异常处理。
     * @return    默认的工具包。
     * @exception  AWTError  如果找不到工具包，或者
     *                 无法访问或实例化工具包。
     */
    public static synchronized Toolkit getDefaultToolkit() {
        if (toolkit == null) {
            java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    Class<?> cls = null;
                    String nm = System.getProperty("awt.toolkit");
                    try {
                        cls = Class.forName(nm);
                    } catch (ClassNotFoundException e) {
                        ClassLoader cl = ClassLoader.getSystemClassLoader();
                        if (cl != null) {
                            try {
                                cls = cl.loadClass(nm);
                            } catch (final ClassNotFoundException ignored) {
                                throw new AWTError("未找到工具包: " + nm);
                            }
                        }
                    }
                    try {
                        if (cls != null) {
                            toolkit = (Toolkit)cls.newInstance();
                            if (GraphicsEnvironment.isHeadless()) {
                                toolkit = new HeadlessToolkit(toolkit);
                            }
                        }
                    } catch (final InstantiationException ignored) {
                        throw new AWTError("无法实例化工具包: " + nm);
                    } catch (final IllegalAccessException ignored) {
                        throw new AWTError("无法访问工具包: " + nm);
                    }
                    return null;
                }
            });
            loadAssistiveTechnologies();
        }
        return toolkit;
    }

    /**
     * 返回一个从指定文件获取像素数据的图像，
     * 文件格式可以是 GIF、JPEG 或 PNG。
     * 底层工具包尝试将具有相同文件名的多个请求解析为相同的返回图像。
     * <p>
     * 由于实现此共享 <code>Image</code> 对象所需的机制可能会无限期地继续持有不再使用的图像，
     * 开发者被鼓励通过使用 {@link #createImage(java.lang.String) createImage}
     * 变体在任何可用的地方实现自己的图像缓存。
     * 如果指定文件中的图像数据发生变化，
     * 从该方法返回的 <code>Image</code> 对象可能仍包含从先前调用后加载的旧信息。
     * 可以通过调用返回的 <code>Image</code> 上的 {@link Image#flush flush} 方法手动丢弃先前加载的图像数据。
     * <p>
     * 该方法首先检查是否安装了安全管理器。
     * 如果是这样，该方法会调用安全管理器的
     * <code>checkRead</code> 方法，以确保允许访问图像。
     * @param     filename   包含像素数据的文件名，格式为已识别的文件格式。
     * @return    从指定文件获取像素数据的图像。
     * @throws SecurityException  如果存在安全管理器且其
     *                            checkRead 方法不允许该操作。
     * @see #createImage(java.lang.String)
     */
    public abstract Image getImage(String filename);

    /**
     * 返回一个从指定 URL 获取像素数据的图像。
     * 指定 URL 引用的像素数据必须是以下格式之一：GIF、JPEG 或 PNG。
     * 底层工具包尝试将具有相同 URL 的多个请求解析为相同的返回图像。
     * <p>
     * 由于实现此共享 <code>Image</code> 对象所需的机制可能会无限期地继续持有不再使用的图像，
     * 开发者被鼓励通过使用 {@link #createImage(java.net.URL) createImage}
     * 变体在任何可用的地方实现自己的图像缓存。
     * 如果指定 URL 存储的图像数据发生变化，
     * 从该方法返回的 <code>Image</code> 对象可能仍包含从先前调用后获取的旧信息。
     * 可以通过调用返回的 <code>Image</code> 上的 {@link Image#flush flush} 方法手动丢弃先前加载的图像数据。
     * <p>
     * 该方法首先检查是否安装了安全管理器。
     * 如果是这样，该方法会调用安全管理器的
     * <code>checkPermission</code> 方法，以确保允许访问图像。为了与 1.2 之前的
     * 安全管理器兼容，如果访问被 <code>FilePermission</code> 或 <code>SocketPermission</code>
     * 拒绝，该方法会抛出 <code>SecurityException</code>，如果相应的 1.1 风格的
     * SecurityManager.checkXXX 方法也拒绝权限。
     * @param     url   用于获取像素数据的 URL。
     * @return    从指定 URL 获取像素数据的图像。
     * @throws SecurityException  如果存在安全管理器且其
     *                            checkPermission 方法不允许该操作。
     * @see #createImage(java.net.URL)
     */
    public abstract Image getImage(URL url);

    /**
     * 返回一个从指定文件获取像素数据的图像。
     * 返回的 <code>Image</code> 是一个新对象，不会与该方法或其 getImage 变体的任何其他调用者共享。
     * <p>
     * 该方法首先检查是否安装了安全管理器。
     * 如果是这样，该方法会调用安全管理器的
     * <code>checkRead</code> 方法，以确保允许创建图像。
     * @param     filename   包含像素数据的文件名，格式为已识别的文件格式。
     * @return    从指定文件获取像素数据的图像。
     * @throws SecurityException  如果存在安全管理器且其
     *                            checkRead 方法不允许该操作。
     * @see #getImage(java.lang.String)
     */
    public abstract Image createImage(String filename);

    /**
     * 返回一个从指定 URL 获取像素数据的图像。
     * 返回的 <code>Image</code> 是一个新对象，不会与该方法或其 getImage 变体的任何其他调用者共享。
     * <p>
     * 该方法首先检查是否安装了安全管理器。
     * 如果是这样，该方法会调用安全管理器的
     * <code>checkPermission</code> 方法，以确保允许创建图像。为了与 1.2 之前的
     * 安全管理器兼容，如果访问被 <code>FilePermission</code> 或 <code>SocketPermission</code>
     * 拒绝，该方法会抛出 <code>SecurityException</code>，如果相应的 1.1 风格的
     * SecurityManager.checkXXX 方法也拒绝权限。
     * @param     url   用于获取像素数据的 URL。
     * @return    从指定 URL 获取像素数据的图像。
     * @throws SecurityException  如果存在安全管理器且其
     *                            checkPermission 方法不允许该操作。
     * @see #getImage(java.net.URL)
     */
    public abstract Image createImage(URL url);

    /**
     * 准备图像以进行渲染。
     * <p>
     * 如果宽度和高度参数的值都为 <code>-1</code>，此方法将图像准备在默认屏幕上渲染；
     * 否则，此方法将图像准备在默认屏幕上以指定的宽度和高度渲染。
     * <p>
     * 图像数据在另一个线程中异步下载，并生成适当缩放的屏幕表示。
     * <p>
     * 该方法由组件的 <code>prepareImage</code> 方法调用。
     * <p>
     * 有关此方法返回的标志的信息可以在 <code>ImageObserver</code> 接口的定义中找到。

     * @param     image      要准备屏幕表示的图像。
     * @param     width      所需屏幕表示的宽度，或 <code>-1</code>。
     * @param     height     所需屏幕表示的高度，或 <code>-1</code>。
     * @param     observer   <code>ImageObserver</code>
     *                           对象，将在图像准备时通知。
     * @return    如果图像已完全准备，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @see       java.awt.Component#prepareImage(java.awt.Image,
     *                 java.awt.image.ImageObserver)
     * @see       java.awt.Component#prepareImage(java.awt.Image,
     *                 int, int, java.awt.image.ImageObserver)
     * @see       java.awt.image.ImageObserver
     */
    public abstract boolean prepareImage(Image image, int width, int height,
                                         ImageObserver observer);

    /**
     * 指示正在准备显示的指定图像的构造状态。
     * <p>
     * 如果宽度和高度参数的值都为 <code>-1</code>，此方法返回指定图像在本工具包中的屏幕表示的构造状态。
     * 否则，此方法返回指定宽度和高度的图像缩放表示的构造状态。
     * <p>
     * 该方法不会导致图像开始加载。
     * 应用程序必须调用 <code>prepareImage</code> 以强制加载图像。
     * <p>
     * 该方法由组件的 <code>checkImage</code> 方法调用。
     * <p>
     * 有关此方法返回的标志的信息可以在 <code>ImageObserver</code> 接口的定义中找到。
     * @param     image   要检查状态的图像。
     * @param     width   要检查状态的缩放版本的宽度，或 <code>-1</code>。
     * @param     height  要检查状态的缩放版本的高度，或 <code>-1</code>。
     * @param     observer   <code>ImageObserver</code> 对象，将在图像准备时通知。
     * @return    当前可用的图像数据的 <code>ImageObserver</code> 标志的按位或。
     * @see       java.awt.Toolkit#prepareImage(java.awt.Image,
     *                 int, int, java.awt.image.ImageObserver)
     * @see       java.awt.Component#checkImage(java.awt.Image,
     *                 java.awt.image.ImageObserver)
     * @see       java.awt.Component#checkImage(java.awt.Image,
     *                 int, int, java.awt.image.ImageObserver)
     * @see       java.awt.image.ImageObserver
     */
    public abstract int checkImage(Image image, int width, int height,
                                   ImageObserver observer);


                /**
     * 使用指定的图像生成器创建图像。
     * @param     producer 要使用的图像生成器。
     * @return    使用指定的图像生成器创建的图像。
     * @see       java.awt.Image
     * @see       java.awt.image.ImageProducer
     * @see       java.awt.Component#createImage(java.awt.image.ImageProducer)
     */
    public abstract Image createImage(ImageProducer producer);

    /**
     * 创建一个解码指定字节数组中存储的图像。
     * <p>
     * 数据必须是某种图像格式，如GIF或JPEG，这些格式由此工具包支持。
     * @param     imagedata 一个字节数组，表示支持的图像格式的图像数据。
     * @return    一个图像。
     * @since     JDK1.1
     */
    public Image createImage(byte[] imagedata) {
        return createImage(imagedata, 0, imagedata.length);
    }

    /**
     * 创建一个解码指定字节数组中存储的图像，并指定偏移量和长度。
     * 数据必须是某种图像格式，如GIF或JPEG，这些格式由此工具包支持。
     * @param     imagedata 一个字节数组，表示支持的图像格式的图像数据。
     * @param     imageoffset 数据在数组中的起始偏移量。
     * @param     imagelength 数据在数组中的长度。
     * @return    一个图像。
     * @since     JDK1.1
     */
    public abstract Image createImage(byte[] imagedata,
                                      int imageoffset,
                                      int imagelength);

    /**
     * 获取一个<code>PrintJob</code>对象，该对象是启动工具包平台上的打印操作的结果。
     * <p>
     * 每个实际实现此方法的类应首先检查是否有安全经理安装。如果有，该方法应调用安全经理的<code>checkPrintJobAccess</code>方法，
     * 以确保允许启动打印操作。如果使用的是<code>checkPrintJobAccess</code>的默认实现（即该方法未被重写），
     * 则这将导致调用安全经理的<code>checkPermission</code>方法，使用<code>RuntimePermission("queuePrintJob")</code>权限。
     *
     * @param   frame 打印对话框的父窗口。不能为空。
     * @param   jobtitle 打印作业的标题。空标题等同于""。
     * @param   props 包含零个或多个属性的属性对象。属性未标准化且在实现之间不一致。
     *          因此，需要作业和页面控制的打印作业应使用接受JobAttributes和PageAttributes对象的版本。此对象可能在退出时更新以反映用户的作业选择。可以为空。
     * @return  一个<code>PrintJob</code>对象，或如果用户取消了打印作业则返回<code>null</code>。
     * @throws  NullPointerException 如果frame为空
     * @throws  SecurityException 如果此线程不允许启动打印作业请求
     * @see     java.awt.GraphicsEnvironment#isHeadless
     * @see     java.awt.PrintJob
     * @see     java.lang.RuntimePermission
     * @since   JDK1.1
     */
    public abstract PrintJob getPrintJob(Frame frame, String jobtitle,
                                         Properties props);

    /**
     * 获取一个<code>PrintJob</code>对象，该对象是启动工具包平台上的打印操作的结果。
     * <p>
     * 每个实际实现此方法的类应首先检查是否有安全经理安装。如果有，该方法应调用安全经理的<code>checkPrintJobAccess</code>方法，
     * 以确保允许启动打印操作。如果使用的是<code>checkPrintJobAccess</code>的默认实现（即该方法未被重写），
     * 则这将导致调用安全经理的<code>checkPermission</code>方法，使用<code>RuntimePermission("queuePrintJob")</code>权限。
     *
     * @param   frame 打印对话框的父窗口。不能为空。
     * @param   jobtitle 打印作业的标题。空标题等同于""。
     * @param   jobAttributes 一组将控制打印作业的作业属性。属性将更新以反映用户的选择，如JobAttributes文档中所述。可以为空。
     * @param   pageAttributes 一组将控制打印作业的页面属性。属性将应用于作业中的每一页。属性将更新以反映用户的选择，如PageAttributes文档中所述。可以为空。
     * @return  一个<code>PrintJob</code>对象，或如果用户取消了打印作业则返回<code>null</code>。
     * @throws  NullPointerException 如果frame为空
     * @throws  IllegalArgumentException 如果pageAttributes指定了不同的横向和纵向分辨率。如果此线程可以访问文件系统且jobAttributes指定了打印到文件，
     *          并且指定的目标文件存在但是一个目录而不是普通文件，或者不存在但无法创建，或者由于其他原因无法打开。
     *          但是，如果请求显示对话框，则用户将有机会选择文件并继续打印。对话框将在返回此方法之前确保选定的输出文件有效。
     * @throws  SecurityException 如果此线程不允许启动打印作业请求，或者jobAttributes指定了打印到文件，且此线程不允许访问文件系统
     * @see     java.awt.PrintJob
     * @see     java.awt.GraphicsEnvironment#isHeadless
     * @see     java.lang.RuntimePermission
     * @see     java.awt.JobAttributes
     * @see     java.awt.PageAttributes
     * @since   1.3
     */
    public PrintJob getPrintJob(Frame frame, String jobtitle,
                                JobAttributes jobAttributes,
                                PageAttributes pageAttributes) {
        // 重写以添加使用新的作业/页面控制类的打印支持

        if (this != Toolkit.getDefaultToolkit()) {
            return Toolkit.getDefaultToolkit().getPrintJob(frame, jobtitle,
                                                           jobAttributes,
                                                           pageAttributes);
        } else {
            return getPrintJob(frame, jobtitle, null);
        }
    }

    /**
     * 根据本机系统设置和硬件功能发出音频嘟嘟声。
     * @since     JDK1.1
     */
    public abstract void beep();

    /**
     * 获取系统剪贴板的单例实例，该实例与本机平台提供的剪贴板功能接口。此剪贴板允许Java程序与使用本机剪贴板功能的本机应用程序之间进行数据传输。
     * <p>
     * 除了flavormap.properties文件或由<code>AWT.DnD.flavorMapFileURL</code> Toolkit属性指定的其他文件中指定的任何和所有格式外，
     * 系统剪贴板的<code>getTransferData()</code>方法返回的文本还可用以下格式：
     * <ul>
     * <li>DataFlavor.stringFlavor</li>
     * <li>DataFlavor.plainTextFlavor (<b>已弃用</b>)</li>
     * </ul>
     * 与<code>java.awt.datatransfer.StringSelection</code>一样，如果请求的格式是<code>DataFlavor.plainTextFlavor</code>或等效格式，
     * 则返回一个Reader。<b>注意：</b>系统剪贴板的<code>getTransferData()</code>方法对<code>DataFlavor.plainTextFlavor</code>及其等效格式的行为
     * 与<code>DataFlavor.plainTextFlavor</code>的定义不一致。因此，对<code>DataFlavor.plainTextFlavor</code>及其等效格式的支持<b>已弃用</b>。
     * <p>
     * 每个实际实现此方法的类应首先检查是否有安全经理安装。如果有，该方法应调用安全经理的{@link SecurityManager#checkPermission checkPermission}方法，
     * 以检查{@code AWTPermission("accessClipboard")}。
     *
     * @return    系统剪贴板
     * @exception HeadlessException 如果GraphicsEnvironment.isHeadless()返回true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.datatransfer.Clipboard
     * @see       java.awt.datatransfer.StringSelection
     * @see       java.awt.datatransfer.DataFlavor#stringFlavor
     * @see       java.awt.datatransfer.DataFlavor#plainTextFlavor
     * @see       java.io.Reader
     * @see       java.awt.AWTPermission
     * @since     JDK1.1
     */
    public abstract Clipboard getSystemClipboard()
        throws HeadlessException;

    /**
     * 获取系统选择的单例实例作为<code>Clipboard</code>对象。这允许应用程序读取和修改当前的系统范围选择。
     * <p>
     * 应用程序负责在用户使用鼠标或键盘选择文本时更新系统选择。通常，这是通过在支持文本选择的所有<code>Component</code>上安装
     * <code>FocusListener</code>来实现的，并且在<code>FOCUS_GAINED</code>和<code>FOCUS_LOST</code>事件传递给该<code>Component</code>之间，
     * 在<code>Component</code>内的选择发生变化时更新系统选择<code>Clipboard</code>。正确更新系统选择确保Java应用程序能够与同时运行在系统上的本机应用程序和其他Java应用程序正确交互。
     * 请注意，<code>java.awt.TextComponent</code>和<code>javax.swing.text.JTextComponent</code>已经遵循此策略。使用这些类及其子类时，开发人员无需编写任何额外的代码。
     * <p>
     * 一些平台不支持系统选择<code>Clipboard</code>。在这些平台上，此方法将返回<code>null</code>。在这种情况下，应用程序无需如上所述更新系统选择<code>Clipboard</code>。
     * <p>
     * 每个实际实现此方法的类应首先检查是否有安全经理安装。如果有，该方法应调用安全经理的{@link SecurityManager#checkPermission checkPermission}方法，
     * 以检查{@code AWTPermission("accessClipboard")}。
     *
     * @return 系统选择作为<code>Clipboard</code>，或如果本机平台不支持系统选择<code>Clipboard</code>则返回<code>null</code>
     * @exception HeadlessException 如果GraphicsEnvironment.isHeadless()返回true
     *
     * @see java.awt.datatransfer.Clipboard
     * @see java.awt.event.FocusListener
     * @see java.awt.event.FocusEvent#FOCUS_GAINED
     * @see java.awt.event.FocusEvent#FOCUS_LOST
     * @see TextComponent
     * @see javax.swing.text.JTextComponent
     * @see AWTPermission
     * @see GraphicsEnvironment#isHeadless
     * @since 1.4
     */
    public Clipboard getSystemSelection() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();

        if (this != Toolkit.getDefaultToolkit()) {
            return Toolkit.getDefaultToolkit().getSystemSelection();
        } else {
            GraphicsEnvironment.checkHeadless();
            return null;
        }
    }

    /**
     * 确定适用于菜单快捷键的适当修饰键。
     * <p>
     * 菜单快捷键由<code>MenuShortcut</code>类体现，由<code>MenuBar</code>类处理。
     * <p>
     * 默认情况下，此方法返回<code>Event.CTRL_MASK</code>。如果<b>Control</b>键不是快捷键的正确键，工具包实现应重写此方法。
     * @return    用于此工具包菜单快捷键的<code>Event</code>类的修饰符掩码。
     * @exception HeadlessException 如果GraphicsEnvironment.isHeadless()返回true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.MenuBar
     * @see       java.awt.MenuShortcut
     * @since     JDK1.1
     */
    public int getMenuShortcutKeyMask() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();

        return Event.CTRL_MASK;
    }

    /**
     * 返回键盘上给定的锁定键是否当前处于“开启”状态。
     * 有效的键码是
     * {@link java.awt.event.KeyEvent#VK_CAPS_LOCK VK_CAPS_LOCK}，
     * {@link java.awt.event.KeyEvent#VK_NUM_LOCK VK_NUM_LOCK}，
     * {@link java.awt.event.KeyEvent#VK_SCROLL_LOCK VK_SCROLL_LOCK}，和
     * {@link java.awt.event.KeyEvent#VK_KANA_LOCK VK_KANA_LOCK}。
     *
     * @exception java.lang.IllegalArgumentException 如果<code>keyCode</code>
     * 是无效的键码
     * @exception java.lang.UnsupportedOperationException 如果主机系统不允许通过编程方式获取此键的状态，或键盘上没有此键
     * @exception HeadlessException 如果GraphicsEnvironment.isHeadless()
     * 返回true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @since 1.3
     */
    public boolean getLockingKeyState(int keyCode)
        throws UnsupportedOperationException
    {
        GraphicsEnvironment.checkHeadless();

        if (! (keyCode == KeyEvent.VK_CAPS_LOCK || keyCode == KeyEvent.VK_NUM_LOCK ||
               keyCode == KeyEvent.VK_SCROLL_LOCK || keyCode == KeyEvent.VK_KANA_LOCK)) {
            throw new IllegalArgumentException("invalid key for Toolkit.getLockingKeyState");
        }
        throw new UnsupportedOperationException("Toolkit.getLockingKeyState");
    }

    /**
     * 设置键盘上给定的锁定键的状态。
     * 有效的键码是
     * {@link java.awt.event.KeyEvent#VK_CAPS_LOCK VK_CAPS_LOCK}，
     * {@link java.awt.event.KeyEvent#VK_NUM_LOCK VK_NUM_LOCK}，
     * {@link java.awt.event.KeyEvent#VK_SCROLL_LOCK VK_SCROLL_LOCK}，和
     * {@link java.awt.event.KeyEvent#VK_KANA_LOCK VK_KANA_LOCK}。
     * <p>
     * 根据平台，设置锁定键的状态可能涉及事件处理，因此可能不会立即通过getLockingKeyState观察到。
     *
     * @exception java.lang.IllegalArgumentException 如果<code>keyCode</code>
     * 是无效的键码
     * @exception java.lang.UnsupportedOperationException 如果主机系统不允许通过编程方式设置此键的状态，或键盘上没有此键
     * @exception HeadlessException 如果GraphicsEnvironment.isHeadless()
     * 返回true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @since 1.3
     */
    public void setLockingKeyState(int keyCode, boolean on)
        throws UnsupportedOperationException
    {
        GraphicsEnvironment.checkHeadless();


    /**
     * 刷新流。
     */
    public void flush() { }

    /**
     * 关闭流。
     */
    public void close() { }

}

/**
 * 如果键码不是以下之一，则抛出 IllegalArgumentException。
 */
if (! (keyCode == KeyEvent.VK_CAPS_LOCK || keyCode == KeyEvent.VK_NUM_LOCK ||
       keyCode == KeyEvent.VK_SCROLL_LOCK || keyCode == KeyEvent.VK_KANA_LOCK)) {
    throw new IllegalArgumentException("invalid key for Toolkit.setLockingKeyState");
}
throw new UnsupportedOperationException("Toolkit.setLockingKeyState");
}

/**
 * 给本地对等体提供查询本地容器的能力，给定一个本地组件（例如直接父组件可能是轻量级的）。
 */
protected static Container getNativeContainer(Component c) {
    return c.getNativeContainer();
}

/**
 * 创建一个新的自定义光标对象。
 * 如果要显示的图像无效，光标将被隐藏（完全透明），并且热点将被设置为 (0, 0)。
 *
 * <p>注意：多帧图像无效，可能会导致此方法挂起。
 *
 * @param cursor 当光标激活时要显示的图像
 * @param hotSpot 大光标的热点 X 和 Y 坐标；热点值必须小于由 <code>getBestCursorSize</code> 返回的尺寸
 * @param name 光标的本地化描述，用于 Java 可访问性
 * @exception IndexOutOfBoundsException 如果热点值超出光标的边界
 * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
 * @see java.awt.GraphicsEnvironment#isHeadless
 * @since 1.2
 */
public Cursor createCustomCursor(Image cursor, Point hotSpot, String name)
    throws IndexOutOfBoundsException, HeadlessException
{
    // 覆盖以实现自定义光标支持。
    if (this != Toolkit.getDefaultToolkit()) {
        return Toolkit.getDefaultToolkit().
            createCustomCursor(cursor, hotSpot, name);
    } else {
        return new Cursor(Cursor.DEFAULT_CURSOR);
    }
}

/**
 * 返回最接近所需尺寸的支持光标尺寸。仅支持单个光标尺寸的系统将返回该尺寸，而不考虑所需尺寸。不支持自定义光标的系统将返回 0, 0 尺寸。<p>
 * 注意：如果使用了尺寸不匹配支持尺寸的图像（由本方法返回），工具包实现将尝试将图像调整为支持的尺寸。
 * 由于转换低分辨率图像很困难，因此不会保证尺寸不匹配支持尺寸的光标图像的质量。因此建议调用此方法并使用适当的图像，以避免图像转换。
 *
 * @param preferredWidth 组件希望使用的首选光标宽度
 * @param preferredHeight 组件希望使用的首选光标高度
 * @return 最接近匹配的支持光标尺寸，或 0,0 尺寸，如果工具包实现不支持自定义光标
 * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
 * @see java.awt.GraphicsEnvironment#isHeadless
 * @since 1.2
 */
public Dimension getBestCursorSize(int preferredWidth,
    int preferredHeight) throws HeadlessException {
    GraphicsEnvironment.checkHeadless();

    // 覆盖以实现自定义光标支持。
    if (this != Toolkit.getDefaultToolkit()) {
        return Toolkit.getDefaultToolkit().
            getBestCursorSize(preferredWidth, preferredHeight);
    } else {
        return new Dimension(0, 0);
    }
}

/**
 * 返回工具包在自定义光标调色板中支持的最大颜色数。<p>
 * 注意：如果使用了调色板颜色数超过支持最大值的图像，工具包实现将尝试将调色板压平到最大值。由于转换低分辨率图像很困难，因此不会保证颜色数超过系统支持的光标图像的质量。因此建议调用此方法并使用适当的图像，以避免图像转换。
 *
 * @return 最大颜色数，或零，如果此工具包实现不支持自定义光标
 * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
 * @see java.awt.GraphicsEnvironment#isHeadless
 * @since 1.2
 */
public int getMaximumCursorColors() throws HeadlessException {
    GraphicsEnvironment.checkHeadless();

    // 覆盖以实现自定义光标支持。
    if (this != Toolkit.getDefaultToolkit()) {
        return Toolkit.getDefaultToolkit().getMaximumCursorColors();
    } else {
        return 0;
    }
}

/**
 * 返回工具包是否支持给定的 <code>Frame</code> 状态。此方法指示是否支持最大化或图标化等 UI 概念。对于像 <code>Frame.ICONIFIED|Frame.MAXIMIZED_VERT</code> 这样的“复合”状态，此方法将始终返回 false。
 * 换句话说，只有使用单个帧状态常量作为参数的查询才有意义。
 * <p>注意：支持给定概念是一个平台依赖的特性。由于本机限制，工具包对象可能会报告某个状态受支持，但同时无法将该状态应用于给定的帧。这有两个后果：
 * <ul>
 * <li>只有当本方法返回 {@code false} 时，才实际表示给定状态不受支持。如果方法返回 {@code true}，给定状态仍可能不受支持和/或不可用于特定的帧。
 * <li>开发者应考虑检查通过 {@link java.awt.event.WindowStateListener} 接收到的 {@code WindowEvent} 的 {@link java.awt.event.WindowEvent#getNewState} 方法的值，而不是假设给定的状态将被 {@code setExtendedState()} 方法确定地应用。有关更多信息，请参见 {@link Frame#setExtendedState} 方法的文档。
 * </ul>
 *
 * @param state 命名的帧状态常量之一。
 * @return 如果此帧状态受此工具包实现支持，则返回 <code>true</code>，否则返回 <code>false</code>。
 * @exception HeadlessException
 *     如果 <code>GraphicsEnvironment.isHeadless()</code>
 *     返回 <code>true</code>。
 * @see java.awt.Window#addWindowStateListener
 * @since   1.4
 */
public boolean isFrameStateSupported(int state)
    throws HeadlessException
{
    GraphicsEnvironment.checkHeadless();

    if (this != Toolkit.getDefaultToolkit()) {
        return Toolkit.getDefaultToolkit().
            isFrameStateSupported(state);
    } else {
        return (state == Frame.NORMAL); // 其他状态不保证
    }
}

/**
 * I18N 支持：任何可见的字符串应存储在 sun.awt.resources.awt.properties 中。资源包存储在此处，以便只维护一个副本。
 */
private static ResourceBundle resources;
private static ResourceBundle platformResources;

// 由平台工具包调用
private static void setPlatformResources(ResourceBundle bundle) {
    platformResources = bundle;
}

/**
 * 初始化 JNI 字段和方法 ID
 */
private static native void initIDs();

/**
 * 警告：这是 AWT 加载本机库问题的临时解决方法。AWT 包中的许多类都有一个本机方法 initIDs()，该方法初始化其实现的本机部分使用的 JNI 字段和方法 ID。
 *
 * 由于这些 ID 的使用和存储是由实现库完成的，因此这些方法的实现由特定的 AWT 实现（例如，“Toolkit”/Peer），如 Motif、Microsoft Windows 或 Tiny 提供。
 * 问题是这意味着本机库必须由 java.* 类加载，而这些类不一定知道要加载的库的名称。更好的方法是提供一个定义 java.awt.* initIDs 的单独库，并将相关符号导出到实现库。
 *
 * 目前，我们知道这是由实现完成的，并假设库的名称是“awt”。 -br.
 *
 * 如果更改 loadLibraries()，请将更改添加到 java.awt.image.ColorModel.loadLibraries()。不幸的是，java.awt.image 中可以加载依赖于 libawt 的类，而没有直接调用 Toolkit.loadLibraries() 的方法。 -hung
 */
private static boolean loaded = false;
static void loadLibraries() {
    if (!loaded) {
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    System.loadLibrary("awt");
                    return null;
                }
            });
        loaded = true;
    }
}

static {
    AWTAccessor.setToolkitAccessor(
            new AWTAccessor.ToolkitAccessor() {
                @Override
                public void setPlatformResources(ResourceBundle bundle) {
                    Toolkit.setPlatformResources(bundle);
                }
            });

    java.security.AccessController.doPrivileged(
                             new java.security.PrivilegedAction<Void>() {
        public Void run() {
            try {
                resources =
                    ResourceBundle.getBundle("sun.awt.resources.awt",
                            Locale.getDefault(),
                            ClassLoader.getSystemClassLoader(),
                            CoreResourceBundleControl.getRBControlInstance());
            } catch (MissingResourceException e) {
                // 没有资源文件；将使用默认值。
            }
            return null;
        }
    });

    // 确保加载了正确的库
    loadLibraries();
    initAssistiveTechnologies();
    if (!GraphicsEnvironment.isHeadless()) {
        initIDs();
    }
}

/**
 * 获取具有指定键和默认值的属性。
 * 如果未找到属性，则返回 defaultValue。
 */
public static String getProperty(String key, String defaultValue) {
    // 首先尝试平台特定的资源包
    if (platformResources != null) {
        try {
            return platformResources.getString(key);
        }
        catch (MissingResourceException e) {}
    }

    // 然后共享的资源包
    if (resources != null) {
        try {
            return resources.getString(key);
        }
        catch (MissingResourceException e) {}
    }

    return defaultValue;
}

/**
 * 获取应用程序或 applet 的 EventQueue 实例。
 * 根据工具包实现，不同的 applet 可能会返回不同的 EventQueue。因此，applet 不应假设通过此方法返回的 EventQueue 实例将与其他 applet 或系统共享。
 *
 * <p> 如果设置了安全管理器，则其
 * {@link SecurityManager#checkPermission checkPermission} 方法
 * 会检查 {@code AWTPermission("accessEventQueue")}。
 *
 * @return <code>EventQueue</code> 对象
 * @throws  SecurityException
 *          如果设置了安全管理器并且它拒绝访问
 *          {@code EventQueue}
 * @see     java.awt.AWTPermission
*/
public final EventQueue getSystemEventQueue() {
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
        security.checkPermission(SecurityConstants.AWT.CHECK_AWT_EVENTQUEUE_PERMISSION);
    }
    return getSystemEventQueueImpl();
}

/**
 * 获取应用程序或 applet 的 <code>EventQueue</code> 实例，不检查访问权限。出于安全原因，这只能从 <code>Toolkit</code> 子类调用。
 * @return <code>EventQueue</code> 对象
 */
protected abstract EventQueue getSystemEventQueueImpl();

/* 供 AWT 包例程使用的访问器方法。 */
static EventQueue getEventQueue() {
    return getDefaultToolkit().getSystemEventQueueImpl();
}

/**
 * 为 DragSourceContext 创建对等体。
 * 如果 GraphicsEnvironment.isHeadless() 返回 true，则始终抛出 InvalidDndOperationException。
 * @see java.awt.GraphicsEnvironment#isHeadless
 */
public abstract DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException;

/**
 * 创建请求的抽象 DragGestureRecognizer 类的平台依赖的具体子类，并将其与指定的 DragSource、Component 和 DragGestureListener 关联。
 *
 * 子类应覆盖此方法以提供自己的实现
 *
 * @param abstractRecognizerClass 所需识别器的抽象类
 * @param ds                      DragSource
 * @param c                       DragGestureRecognizer 的目标 Component
 * @param srcActions              允许的手势操作
 * @param dgl                     DragGestureListener
 *
 * @return 新对象或 null。如果 GraphicsEnvironment.isHeadless() 返回 true，则始终返回 null。
 * @see java.awt.GraphicsEnvironment#isHeadless
 */
public <T extends DragGestureRecognizer> T
    createDragGestureRecognizer(Class<T> abstractRecognizerClass,
                                DragSource ds, Component c, int srcActions,
                                DragGestureListener dgl)
{
    return null;
}

/**
 * 获取指定桌面属性的值。
 *
 * 桌面属性是 Toolkit 全局性质的唯一命名值。通常它也是底层平台依赖的桌面设置的抽象表示。有关 AWT 支持的桌面属性的更多信息，请参见
 * <a href="doc-files/DesktopProperties.html">AWT Desktop Properties</a>。
 */
public final synchronized Object getDesktopProperty(String propertyName) {
    // 这是无头工具包的解决方法。更好的方法是覆盖此方法，但它被声明为 final。
    // "this instanceof" 语法会破坏多态性。
    // --mm, 03/03/00
    if (this instanceof HeadlessToolkit) {
        return ((HeadlessToolkit)this).getUnderlyingToolkit()
            .getDesktopProperty(propertyName);
    }


    /**
     * 刷新流。
     */
    public void flush() { }

    /**
     * 关闭流。
     */
    public void close() { }

}

/**
 * 设置命名的桌面属性为指定的值，并触发一个属性更改事件，通知任何监听者该值已更改。
 */
protected final void setDesktopProperty(String name, Object newValue) {
    // 这是无头工具包的变通方法。最好是重写此方法，但它是最终的。
    // "this instanceof" 语法破坏了多态性。
    // --mm, 03/03/00
    if (this instanceof HeadlessToolkit) {
        ((HeadlessToolkit)this).getUnderlyingToolkit()
            .setDesktopProperty(name, newValue);
        return;
    }
    Object oldValue;

    synchronized (this) {
        oldValue = desktopProperties.get(name);
        desktopProperties.put(name, newValue);
    }

    // 如果旧值和新值都为 null，则不触发更改事件。
    // 这有助于避免 WM_THEMECHANGED 的递归重发
    if (oldValue != null || newValue != null) {
        desktopPropsSupport.firePropertyChange(name, oldValue, newValue);
    }
}

/**
 * 提供一个机会来惰性地评估桌面属性值。
 */
protected Object lazilyLoadDesktopProperty(String name) {
    return null;
}

/**
 * 初始化桌面属性。
 */
protected void initializeDesktopProperties() {
}

/**
 * 为命名的桌面属性添加指定的属性更改监听器。当添加 {@link java.beans.PropertyChangeListenerProxy} 对象时，
 * 其属性名称将被忽略，包装的监听器将被添加。
 * 如果 {@code name} 为 {@code null} 或 {@code pcl} 为 {@code null}，
 * 不会抛出异常且不会执行任何操作。
 *
 * @param   name 要监听的属性名称
 * @param   pcl 属性更改监听器
 * @see PropertyChangeSupport#addPropertyChangeListener(String,
            PropertyChangeListener)
 * @since   1.2
 */
public void addPropertyChangeListener(String name, PropertyChangeListener pcl) {
    desktopPropsSupport.addPropertyChangeListener(name, pcl);
}

/**
 * 为命名的桌面属性移除指定的属性更改监听器。当移除 {@link java.beans.PropertyChangeListenerProxy} 对象时，
 * 其属性名称将被忽略，包装的监听器将被移除。
 * 如果 {@code name} 为 {@code null} 或 {@code pcl} 为 {@code null}，
 * 不会抛出异常且不会执行任何操作。
 *
 * @param   name 要移除的属性名称
 * @param   pcl 属性更改监听器
 * @see PropertyChangeSupport#removePropertyChangeListener(String,
            PropertyChangeListener)
 * @since   1.2
 */
public void removePropertyChangeListener(String name, PropertyChangeListener pcl) {
    desktopPropsSupport.removePropertyChangeListener(name, pcl);
}

/**
 * 返回注册在此工具包上的所有属性更改监听器的数组。返回的数组
 * 包含 {@link java.beans.PropertyChangeListenerProxy} 对象，
 * 这些对象将监听器与桌面属性的名称关联起来。
 *
 * @return 所有此工具包的 {@link PropertyChangeListener} 对象，
 *         包装在 {@code java.beans.PropertyChangeListenerProxy} 对象中，
 *         如果没有添加监听器，则返回空数组
 *
 * @see PropertyChangeSupport#getPropertyChangeListeners()
 * @since 1.4
 */
public PropertyChangeListener[] getPropertyChangeListeners() {
    return desktopPropsSupport.getPropertyChangeListeners();
}

/**
 * 返回与指定的桌面属性名称关联的所有属性更改监听器的数组。
 *
 * @param  propertyName 命名的属性
 * @return 与指定的桌面属性名称关联的所有 {@code PropertyChangeListener} 对象，
 *         如果没有添加此类监听器，则返回空数组
 *
 * @see PropertyChangeSupport#getPropertyChangeListeners(String)
 * @since 1.4
 */
public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
    return desktopPropsSupport.getPropertyChangeListeners(propertyName);
}

protected final Map<String,Object> desktopProperties =
        new HashMap<String,Object>();
protected final PropertyChangeSupport desktopPropsSupport =
        Toolkit.createPropertyChangeSupport(this);

/**
 * 返回此工具包是否支持始终置顶模式。要检测特定窗口是否支持始终置顶模式，
 * 请使用 {@link Window#isAlwaysOnTopSupported}。
 * @return 如果当前工具包支持始终置顶模式，则返回 <code>true</code>，否则返回 <code>false</code>
 * @see Window#isAlwaysOnTopSupported
 * @see Window#setAlwaysOnTop(boolean)
 * @since 1.6
 */
public boolean isAlwaysOnTopSupported() {
    return true;
}

/**
 * 返回此工具包是否支持给定的模态类型。如果创建了不支持的模态类型的对话框，
 * 则使用 <code>Dialog.ModalityType.MODELESS</code>。
 *
 * @param modalityType 要检查的模态类型
 *
 * @return 如果当前工具包支持给定的模态类型，则返回 <code>true</code>，否则返回 <code>false</code>
 *
 * @see java.awt.Dialog.ModalityType
 * @see java.awt.Dialog#getModalityType
 * @see java.awt.Dialog#setModalityType
 *
 * @since 1.6
 */
public abstract boolean isModalityTypeSupported(Dialog.ModalityType modalityType);

/**
 * 返回此工具包是否支持给定的模态排除类型。如果在窗口上设置了不支持的模态排除类型属性，
 * 则使用 <code>Dialog.ModalExclusionType.NO_EXCLUDE</code>。
 *
 * @param modalExclusionType 要检查的模态排除类型
 *
 * @return 如果当前工具包支持给定的模态排除类型，则返回 <code>true</code>，否则返回 <code>false</code>
 *
 * @see java.awt.Dialog.ModalExclusionType
 * @see java.awt.Window#getModalExclusionType
 * @see java.awt.Window#setModalExclusionType
 *
 * @since 1.6
 */
public abstract boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType modalExclusionType);

// 8014718: 日志记录已从 SunToolkit 中移除

private static final int LONG_BITS = 64;
private int[] calls = new int[LONG_BITS];
private static volatile long enabledOnToolkitMask;
private AWTEventListener eventListener = null;
private WeakHashMap<AWTEventListener, SelectiveAWTEventListener> listener2SelectiveListener = new WeakHashMap<>();

/*
 * 从 AWTEventListenerProxy 中提取“纯”AWTEventListener，
 * 如果监听器被代理。
 */
static private AWTEventListener deProxyAWTEventListener(AWTEventListener l)
{
    AWTEventListener localL = l;

    if (localL == null) {
        return null;
    }
    // 如果用户传递了 AWTEventListenerProxy 对象，提取监听器
    if (l instanceof AWTEventListenerProxy) {
        localL = ((AWTEventListenerProxy)l).getListener();
    }
    return localL;
}

/**
 * 添加一个 AWTEventListener 以接收符合给定 <code>eventMask</code> 的所有系统范围的 AWT 事件。
 * <p>
 * 首先，如果有安全经理，其 <code>checkPermission</code>
 * 方法将被调用，使用 <code>AWTPermission("listenToAllAWTEvents")</code> 权限。
 * 这可能导致 SecurityException。
 * <p>
 * <code>eventMask</code> 是要接收的事件类型的位掩码。
 * 它是通过将 <code>AWTEvent</code> 中定义的事件掩码按位或运算构造的。
 * <p>
 * 注意：事件监听器的使用不建议用于正常应用程序，而是专门用于支持特殊用途设施，
 * 包括辅助功能支持、事件记录/回放和诊断跟踪。
 *
 * 如果监听器为 null，不会抛出异常且不会执行任何操作。
 *
 * @param    listener   事件监听器。
 * @param    eventMask  要接收的事件类型的位掩码
 * @throws SecurityException
 *        如果存在安全经理且其 <code>checkPermission</code> 方法不允许操作。
 * @see      #removeAWTEventListener
 * @see      #getAWTEventListeners
 * @see      SecurityManager#checkPermission
 * @see      java.awt.AWTEvent
 * @see      java.awt.AWTPermission
 * @see      java.awt.event.AWTEventListener
 * @see      java.awt.event.AWTEventListenerProxy
 * @since    1.2
 */
public void addAWTEventListener(AWTEventListener listener, long eventMask) {
    AWTEventListener localL = deProxyAWTEventListener(listener);

    if (localL == null) {
        return;
    }
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
      security.checkPermission(SecurityConstants.AWT.ALL_AWT_EVENTS_PERMISSION);
    }
    synchronized (this) {
        SelectiveAWTEventListener selectiveListener =
            listener2SelectiveListener.get(localL);

        if (selectiveListener == null) {
            // 创建一个新的 selectiveListener。
            selectiveListener = new SelectiveAWTEventListener(localL,
                                                             eventMask);
            listener2SelectiveListener.put(localL, selectiveListener);
            eventListener = ToolkitEventMulticaster.add(eventListener,
                                                        selectiveListener);
        }
        // 将 eventMask 按位或运算到 selectiveListener 的事件掩码中。
        selectiveListener.orEventMasks(eventMask);

        enabledOnToolkitMask |= eventMask;

        long mask = eventMask;
        for (int i=0; i<LONG_BITS; i++) {
            // 如果没有设置位，跳出循环。
            if (mask == 0) {
                break;
            }
            if ((mask & 1L) != 0) {  // 始终测试位 0。
                calls[i]++;
            }
            mask >>>= 1;  // 右移，左边填充零。
        }
    }
}

/**
 * 从接收分发的 AWT 事件中移除 AWTEventListener。
 * <p>
 * 首先，如果有安全经理，其 <code>checkPermission</code>
 * 方法将被调用，使用 <code>AWTPermission("listenToAllAWTEvents")</code> 权限。
 * 这可能导致 SecurityException。
 * <p>
 * 注意：事件监听器的使用不建议用于正常应用程序，而是专门用于支持特殊用途设施，
 * 包括辅助功能支持、事件记录/回放和诊断跟踪。
 *
 * 如果监听器为 null，不会抛出异常且不会执行任何操作。
 *
 * @param    listener   事件监听器。
 * @throws SecurityException
 *        如果存在安全经理且其 <code>checkPermission</code> 方法不允许操作。
 * @see      #addAWTEventListener
 * @see      #getAWTEventListeners
 * @see      SecurityManager#checkPermission
 * @see      java.awt.AWTEvent
 * @see      java.awt.AWTPermission
 * @see      java.awt.event.AWTEventListener
 * @see      java.awt.event.AWTEventListenerProxy
 * @since    1.2
 */
public void removeAWTEventListener(AWTEventListener listener) {
    AWTEventListener localL = deProxyAWTEventListener(listener);

    if (listener == null) {
        return;
    }
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
        security.checkPermission(SecurityConstants.AWT.ALL_AWT_EVENTS_PERMISSION);
    }

    synchronized (this) {
        SelectiveAWTEventListener selectiveListener =
            listener2SelectiveListener.get(localL);

        if (selectiveListener != null) {
            listener2SelectiveListener.remove(localL);
            int[] listenerCalls = selectiveListener.getCalls();
            for (int i=0; i<LONG_BITS; i++) {
                calls[i] -= listenerCalls[i];
                assert calls[i] >= 0: "Negative Listeners count";

                if (calls[i] == 0) {
                    enabledOnToolkitMask &= ~(1L<<i);
                }
            }
        }
        eventListener = ToolkitEventMulticaster.remove(eventListener,
        (selectiveListener == null) ? localL : selectiveListener);
    }
}

static boolean enabledOnToolkit(long eventMask) {
    return (enabledOnToolkitMask & eventMask) != 0;
}

synchronized int countAWTEventListeners(long eventMask) {
    int ci = 0;
    for (; eventMask != 0; eventMask >>>= 1, ci++) {
    }
    ci--;
    return calls[ci];
}

/**
 * 返回在此工具包上注册的所有 <code>AWTEventListener</code> 的数组。
 * 如果存在安全经理，其 {@code checkPermission}
 * 方法将被调用，使用 {@code AWTPermission("listenToAllAWTEvents")} 权限。
 * 这可能导致 SecurityException。
 * 监听器可以返回
 * 在 <code>AWTEventListenerProxy</code> 对象中，这些对象还包含给定监听器的事件掩码。
 * 注意，多次添加的监听器对象在返回的数组中只出现一次。
 *
 * @return 所有的 <code>AWTEventListener</code> 或者如果没有注册监听器则返回空数组
 * @throws SecurityException
 *        如果存在安全经理且其 <code>checkPermission</code> 方法不允许操作。
 * @see      #addAWTEventListener
 * @see      #removeAWTEventListener
 * @see      SecurityManager#checkPermission
 * @see      java.awt.AWTEvent
 * @see      java.awt.AWTPermission
 * @see      java.awt.event.AWTEventListener
 * @see      java.awt.event.AWTEventListenerProxy
 * @since 1.4
 */
public AWTEventListener[] getAWTEventListeners() {
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
        security.checkPermission(SecurityConstants.AWT.ALL_AWT_EVENTS_PERMISSION);
    }
    synchronized (this) {
        EventListener[] la = ToolkitEventMulticaster.getListeners(eventListener,AWTEventListener.class);


                        AWTEventListener[] ret = new AWTEventListener[la.length];
            for (int i = 0; i < la.length; i++) {
                SelectiveAWTEventListener sael = (SelectiveAWTEventListener)la[i];
                AWTEventListener tempL = sael.getListener();
                // 断言 tempL 不是 AWTEventListenerProxy - 我们应该
                // 已经将它们全部排除
                // 不希望在代理内部再包装一个代理
                ret[i] = new AWTEventListenerProxy(sael.getEventMask(), tempL);
            }
            return ret;
        }
    }

    /**
     * 返回注册到此工具包的所有 <code>AWTEventListener</code>
     * 并监听 {@code eventMask} 参数中指定的所有事件类型的数组。
     * 如果存在安全经理，其 {@code checkPermission}
     * 方法将被调用，使用
     * {@code AWTPermission("listenToAllAWTEvents")} 权限。
     * 这可能导致 SecurityException。
     * 监听器可以返回
     * 在 <code>AWTEventListenerProxy</code> 对象中，这些对象还包含
     * 给定监听器的事件掩码。
     * 注意，多次添加的监听器对象
     * 在返回的数组中仅出现一次。
     *
     * @param  eventMask 要监听的事件类型的位掩码
     * @return 注册到此工具包的所有 <code>AWTEventListener</code>
     *         对于指定的事件类型，如果没有注册此类监听器
     *         则返回一个空数组
     * @throws SecurityException
     *        如果存在安全经理且其
     *        <code>checkPermission</code> 方法不允许此操作。
     * @see      #addAWTEventListener
     * @see      #removeAWTEventListener
     * @see      SecurityManager#checkPermission
     * @see      java.awt.AWTEvent
     * @see      java.awt.AWTPermission
     * @see      java.awt.event.AWTEventListener
     * @see      java.awt.event.AWTEventListenerProxy
     * @since 1.4
     */
    public AWTEventListener[] getAWTEventListeners(long eventMask) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(SecurityConstants.AWT.ALL_AWT_EVENTS_PERMISSION);
        }
        synchronized (this) {
            EventListener[] la = ToolkitEventMulticaster.getListeners(eventListener,AWTEventListener.class);

            java.util.List<AWTEventListenerProxy> list = new ArrayList<>(la.length);

            for (int i = 0; i < la.length; i++) {
                SelectiveAWTEventListener sael = (SelectiveAWTEventListener)la[i];
                if ((sael.getEventMask() & eventMask) == eventMask) {
                    //AWTEventListener tempL = sael.getListener();
                    list.add(new AWTEventListenerProxy(sael.getEventMask(),
                                                       sael.getListener()));
                }
            }
            return list.toArray(new AWTEventListener[0]);
        }
    }

    /*
     * 通知任何 AWTEventListeners 事件即将被分发。
     *
     * @param theEvent 即将被分发的事件。
     */
    void notifyAWTEventListeners(AWTEvent theEvent) {
        // 这是无头工具包的变通方法。最好覆盖此方法，但它是包私有的。
        // "this instanceof" 语法会破坏多态性。
        // --mm, 03/03/00
        if (this instanceof HeadlessToolkit) {
            ((HeadlessToolkit)this).getUnderlyingToolkit()
                .notifyAWTEventListeners(theEvent);
            return;
        }

        AWTEventListener eventListener = this.eventListener;
        if (eventListener != null) {
            eventListener.eventDispatched(theEvent);
        }
    }

    static private class ToolkitEventMulticaster extends AWTEventMulticaster
        implements AWTEventListener {
        // 实现克隆自 AWTEventMulticaster。

        ToolkitEventMulticaster(AWTEventListener a, AWTEventListener b) {
            super(a, b);
        }

        static AWTEventListener add(AWTEventListener a,
                                    AWTEventListener b) {
            if (a == null)  return b;
            if (b == null)  return a;
            return new ToolkitEventMulticaster(a, b);
        }

        static AWTEventListener remove(AWTEventListener l,
                                       AWTEventListener oldl) {
            return (AWTEventListener) removeInternal(l, oldl);
        }

        // #4178589: 必须重载 remove(EventListener) 以调用我们的 add()
        // 而不是静态的 addInternal()，这样我们分配的是
        // ToolkitEventMulticaster 而不是 AWTEventMulticaster。
        // 注意：此方法由 AWTEventListener.removeInternal() 调用，
        // 因此其方法签名必须与 AWTEventListener.remove() 匹配。
        protected EventListener remove(EventListener oldl) {
            if (oldl == a)  return b;
            if (oldl == b)  return a;
            AWTEventListener a2 = (AWTEventListener)removeInternal(a, oldl);
            AWTEventListener b2 = (AWTEventListener)removeInternal(b, oldl);
            if (a2 == a && b2 == b) {
                return this;    // 它不在这里
            }
            return add(a2, b2);
        }

        public void eventDispatched(AWTEvent event) {
            ((AWTEventListener)a).eventDispatched(event);
            ((AWTEventListener)b).eventDispatched(event);
        }
    }

    private class SelectiveAWTEventListener implements AWTEventListener {
        AWTEventListener listener;
        private long eventMask;
        // 此数组包含每次调用 eventlistener 的次数
        // 对于每种事件类型。
        int[] calls = new int[Toolkit.LONG_BITS];

        public AWTEventListener getListener() {return listener;}
        public long getEventMask() {return eventMask;}
        public int[] getCalls() {return calls;}

        public void orEventMasks(long mask) {
            eventMask |= mask;
            // 对于 mask 中设置的每个事件位，增加其调用计数。
            for (int i=0; i<Toolkit.LONG_BITS; i++) {
                // 如果没有位被设置，跳出循环。
                if (mask == 0) {
                    break;
                }
                if ((mask & 1L) != 0) {  // 始终测试位 0。
                    calls[i]++;
                }
                mask >>>= 1;  // 右移，左侧填充零。
            }
        }

        SelectiveAWTEventListener(AWTEventListener l, long mask) {
            listener = l;
            eventMask = mask;
        }

        public void eventDispatched(AWTEvent event) {
            long eventBit = 0; // 用于保存事件类型的位。
            if (((eventBit = eventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0 &&
                 event.id >= ComponentEvent.COMPONENT_FIRST &&
                 event.id <= ComponentEvent.COMPONENT_LAST)
             || ((eventBit = eventMask & AWTEvent.CONTAINER_EVENT_MASK) != 0 &&
                 event.id >= ContainerEvent.CONTAINER_FIRST &&
                 event.id <= ContainerEvent.CONTAINER_LAST)
             || ((eventBit = eventMask & AWTEvent.FOCUS_EVENT_MASK) != 0 &&
                 event.id >= FocusEvent.FOCUS_FIRST &&
                 event.id <= FocusEvent.FOCUS_LAST)
             || ((eventBit = eventMask & AWTEvent.KEY_EVENT_MASK) != 0 &&
                 event.id >= KeyEvent.KEY_FIRST &&
                 event.id <= KeyEvent.KEY_LAST)
             || ((eventBit = eventMask & AWTEvent.MOUSE_WHEEL_EVENT_MASK) != 0 &&
                 event.id == MouseEvent.MOUSE_WHEEL)
             || ((eventBit = eventMask & AWTEvent.MOUSE_MOTION_EVENT_MASK) != 0 &&
                 (event.id == MouseEvent.MOUSE_MOVED ||
                  event.id == MouseEvent.MOUSE_DRAGGED))
             || ((eventBit = eventMask & AWTEvent.MOUSE_EVENT_MASK) != 0 &&
                 event.id != MouseEvent.MOUSE_MOVED &&
                 event.id != MouseEvent.MOUSE_DRAGGED &&
                 event.id != MouseEvent.MOUSE_WHEEL &&
                 event.id >= MouseEvent.MOUSE_FIRST &&
                 event.id <= MouseEvent.MOUSE_LAST)
             || ((eventBit = eventMask & AWTEvent.WINDOW_EVENT_MASK) != 0 &&
                 (event.id >= WindowEvent.WINDOW_FIRST &&
                 event.id <= WindowEvent.WINDOW_LAST))
             || ((eventBit = eventMask & AWTEvent.ACTION_EVENT_MASK) != 0 &&
                 event.id >= ActionEvent.ACTION_FIRST &&
                 event.id <= ActionEvent.ACTION_LAST)
             || ((eventBit = eventMask & AWTEvent.ADJUSTMENT_EVENT_MASK) != 0 &&
                 event.id >= AdjustmentEvent.ADJUSTMENT_FIRST &&
                 event.id <= AdjustmentEvent.ADJUSTMENT_LAST)
             || ((eventBit = eventMask & AWTEvent.ITEM_EVENT_MASK) != 0 &&
                 event.id >= ItemEvent.ITEM_FIRST &&
                 event.id <= ItemEvent.ITEM_LAST)
             || ((eventBit = eventMask & AWTEvent.TEXT_EVENT_MASK) != 0 &&
                 event.id >= TextEvent.TEXT_FIRST &&
                 event.id <= TextEvent.TEXT_LAST)
             || ((eventBit = eventMask & AWTEvent.INPUT_METHOD_EVENT_MASK) != 0 &&
                 event.id >= InputMethodEvent.INPUT_METHOD_FIRST &&
                 event.id <= InputMethodEvent.INPUT_METHOD_LAST)
             || ((eventBit = eventMask & AWTEvent.PAINT_EVENT_MASK) != 0 &&
                 event.id >= PaintEvent.PAINT_FIRST &&
                 event.id <= PaintEvent.PAINT_LAST)
             || ((eventBit = eventMask & AWTEvent.INVOCATION_EVENT_MASK) != 0 &&
                 event.id >= InvocationEvent.INVOCATION_FIRST &&
                 event.id <= InvocationEvent.INVOCATION_LAST)
             || ((eventBit = eventMask & AWTEvent.HIERARCHY_EVENT_MASK) != 0 &&
                 event.id == HierarchyEvent.HIERARCHY_CHANGED)
             || ((eventBit = eventMask & AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK) != 0 &&
                 (event.id == HierarchyEvent.ANCESTOR_MOVED ||
                  event.id == HierarchyEvent.ANCESTOR_RESIZED))
             || ((eventBit = eventMask & AWTEvent.WINDOW_STATE_EVENT_MASK) != 0 &&
                 event.id == WindowEvent.WINDOW_STATE_CHANGED)
             || ((eventBit = eventMask & AWTEvent.WINDOW_FOCUS_EVENT_MASK) != 0 &&
                 (event.id == WindowEvent.WINDOW_GAINED_FOCUS ||
                  event.id == WindowEvent.WINDOW_LOST_FOCUS))
                || ((eventBit = eventMask & sun.awt.SunToolkit.GRAB_EVENT_MASK) != 0 &&
                    (event instanceof sun.awt.UngrabEvent))) {
                // 获取此事件类型的调用计数索引。
                // 代替使用 Math.log(...)，我们将使用位移计算它。之前的实现看起来像这样：
                //
                // int ci = (int) (Math.log(eventBit)/Math.log(2));
                int ci = 0;
                for (long eMask = eventBit; eMask != 0; eMask >>>= 1, ci++) {
                }
                ci--;
                // 对于此事件类型，调用监听器的次数。
                for (int i=0; i<calls[ci]; i++) {
                    listener.eventDispatched(event);
                }
            }
        }
    }

    /**
     * 返回给定输入方法高亮的抽象级别描述的视觉属性映射，如果没有找到映射则返回 null。
     * 输入方法高亮的样式字段被忽略。返回的映射是不可修改的。
     * @param highlight 输入方法高亮
     * @return 样式属性映射，或 <code>null</code>
     * @exception HeadlessException 如果
     *     <code>GraphicsEnvironment.isHeadless</code> 返回 true
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @since 1.3
     */
    public abstract Map<java.awt.font.TextAttribute,?>
        mapInputMethodHighlight(InputMethodHighlight highlight)
        throws HeadlessException;

    private static PropertyChangeSupport createPropertyChangeSupport(Toolkit toolkit) {
        if (toolkit instanceof SunToolkit || toolkit instanceof HeadlessToolkit) {
            return new DesktopPropertyChangeSupport(toolkit);
        } else {
            return new PropertyChangeSupport(toolkit);
        }
    }

    @SuppressWarnings("serial")
    private static class DesktopPropertyChangeSupport extends PropertyChangeSupport {

        private static final StringBuilder PROP_CHANGE_SUPPORT_KEY =
                new StringBuilder("桌面属性更改支持键");
        private final Object source;

        public DesktopPropertyChangeSupport(Object sourceBean) {
            super(sourceBean);
            source = sourceBean;
        }

        @Override
        public synchronized void addPropertyChangeListener(
                String propertyName,
                PropertyChangeListener listener)
        {
            PropertyChangeSupport pcs = (PropertyChangeSupport)
                    AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
            if (null == pcs) {
                pcs = new PropertyChangeSupport(source);
                AppContext.getAppContext().put(PROP_CHANGE_SUPPORT_KEY, pcs);
            }
            pcs.addPropertyChangeListener(propertyName, listener);
        }

        @Override
        public synchronized void removePropertyChangeListener(
                String propertyName,
                PropertyChangeListener listener)
        {
            PropertyChangeSupport pcs = (PropertyChangeSupport)
                    AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
            if (null != pcs) {
                pcs.removePropertyChangeListener(propertyName, listener);
            }
        }

        @Override
        public synchronized PropertyChangeListener[] getPropertyChangeListeners()
        {
            PropertyChangeSupport pcs = (PropertyChangeSupport)
                    AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
            if (null != pcs) {
                return pcs.getPropertyChangeListeners();
            } else {
                return new PropertyChangeListener[0];
            }
        }

        @Override
        public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName)
        {
            PropertyChangeSupport pcs = (PropertyChangeSupport)
                    AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
            if (null != pcs) {
                return pcs.getPropertyChangeListeners(propertyName);
            } else {
                return new PropertyChangeListener[0];
            }
        }

        @Override
        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            PropertyChangeSupport pcs = (PropertyChangeSupport)
                    AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
            if (null == pcs) {
                pcs = new PropertyChangeSupport(source);
                AppContext.getAppContext().put(PROP_CHANGE_SUPPORT_KEY, pcs);
            }
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            PropertyChangeSupport pcs = (PropertyChangeSupport)
                    AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
            if (null != pcs) {
                pcs.removePropertyChangeListener(listener);
            }
        }


                    /*
         * 我们期望 java.beans.PropertyChangeSupport 的所有其他 fireXXX() 方法都使用此方法。如果这种情况发生变化，我们将需要更改此类。
         */
        @Override
        public void firePropertyChange(final PropertyChangeEvent evt) {
            Object oldValue = evt.getOldValue();
            Object newValue = evt.getNewValue();
            String propertyName = evt.getPropertyName();
            if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
                return;
            }
            Runnable updater = new Runnable() {
                public void run() {
                    PropertyChangeSupport pcs = (PropertyChangeSupport)
                            AppContext.getAppContext().get(PROP_CHANGE_SUPPORT_KEY);
                    if (null != pcs) {
                        pcs.firePropertyChange(evt);
                    }
                }
            };
            final AppContext currentAppContext = AppContext.getAppContext();
            for (AppContext appContext : AppContext.getAppContexts()) {
                if (null == appContext || appContext.isDisposed()) {
                    continue;
                }
                if (currentAppContext == appContext) {
                    updater.run();
                } else {
                    final PeerEvent e = new PeerEvent(source, updater, PeerEvent.ULTIMATE_PRIORITY_EVENT);
                    SunToolkit.postEvent(appContext, e);
                }
            }
        }
    }

    /**
    * 报告是否允许处理和发布来自额外鼠标按钮的事件到 {@code EventQueue}。
    * <br>
    * 要更改返回值，需要在 {@code Toolkit} 类初始化之前设置 {@code sun.awt.enableExtraMouseButtons} 属性。可以通过以下命令在应用程序启动时设置此属性：
    * <pre>
    * java -Dsun.awt.enableExtraMouseButtons=false Application
    * </pre>
    * 或者，可以在应用程序中使用以下代码设置此属性：
    * <pre>
    * System.setProperty("sun.awt.enableExtraMouseButtons", "true");
    * </pre>
    * 在 {@code Toolkit} 类初始化之前。
    * 如果在 {@code Toolkit} 类初始化时未设置此属性，该属性将被初始化为 {@code true}。
    * 在 {@code Toolkit} 类初始化后更改此值将不会产生任何效果。
    * <p>
    * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
    * @return {@code true} 如果允许处理和发布来自额外鼠标按钮的事件；否则返回 {@code false}
    * @see System#getProperty(String propertyName)
    * @see System#setProperty(String propertyName, String value)
    * @see java.awt.EventQueue
    * @since 1.7
     */
    public boolean areExtraMouseButtonsEnabled() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();

        return Toolkit.getDefaultToolkit().areExtraMouseButtonsEnabled();
    }
}
