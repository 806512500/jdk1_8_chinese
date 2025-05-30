
/*
 * Copyright (c) 2005, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.*;
import java.awt.peer.TrayIconPeer;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.AWTAccessor;
import sun.awt.HeadlessToolkit;
import java.util.EventObject;
import java.security.AccessControlContext;
import java.security.AccessController;

/**
 * <code>TrayIcon</code> 对象表示可以添加到 {@link SystemTray 系统托盘} 的托盘图标。一个
 * <code>TrayIcon</code> 可以有提示文本（文本）、图像、弹出菜单和一组监听器。
 *
 * <p><code>TrayIcon</code> 可以生成各种 {@link MouseEvent 鼠标事件} 并支持添加相应的监听器以接收
 * 这些事件的通知。 <code>TrayIcon</code> 会自行处理某些事件。例如，默认情况下，当在 <code>TrayIcon</code>
 * 上右击鼠标时，它会显示指定的弹出菜单。当鼠标悬停在 <code>TrayIcon</code> 上时，会显示提示文本。
 *
 * <p><strong>注意：</strong> 当 <code>MouseEvent</code> 被分发给其注册的监听器时，其 <code>component</code>
 * 属性将被设置为 <code>null</code>。 （参见 {@link java.awt.event.ComponentEvent#getComponent}）
 * <code>source</code> 属性将被设置为这个 <code>TrayIcon</code>。 （参见 {@link
 * java.util.EventObject#getSource}）
 *
 * <p><b>注意：</b> 一个行为良好的 {@link TrayIcon} 实现会为显示弹出菜单和选择托盘图标分配不同的手势。
 *
 * <p><code>TrayIcon</code> 可以生成一个 {@link ActionEvent 操作事件}。在某些平台上，当用户使用鼠标或键盘
 * 选择托盘图标时，会发生这种情况。
 *
 * <p>如果安装了 SecurityManager，则必须授予 AWTPermission
 * {@code accessSystemTray} 才能创建一个 {@code TrayIcon}。否则构造函数将抛出一个
 * SecurityException。
 *
 * <p> 请参阅 {@link SystemTray} 类概述，了解如何使用 <code>TrayIcon</code> API 的示例。
 *
 * @since 1.6
 * @see SystemTray#add
 * @see java.awt.event.ComponentEvent#getComponent
 * @see java.util.EventObject#getSource
 *
 * @author Bino George
 * @author Denis Mikhalkin
 * @author Sharon Zakhour
 * @author Anton Tarasov
 */
public class TrayIcon {

    private Image image;
    private String tooltip;
    private PopupMenu popup;
    private boolean autosize;
    private int id;
    private String actionCommand;

    transient private TrayIconPeer peer;

    transient MouseListener mouseListener;
    transient MouseMotionListener mouseMotionListener;
    transient ActionListener actionListener;

    /*
     * 托盘图标的 AccessControlContext。
     *
     * 与 Component 中的 acc 不同，此字段是 final 的，因为 TrayIcon 不是可序列化的。
     */
    private final AccessControlContext acc = AccessController.getContext();

    /*
     * 返回此托盘图标创建时的 acc。
     */
    final AccessControlContext getAccessControlContext() {
        if (acc == null) {
            throw new SecurityException("TrayIcon 缺少 AccessControlContext");
        }
        return acc;
    }

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }

        AWTAccessor.setTrayIconAccessor(
            new AWTAccessor.TrayIconAccessor() {
                public void addNotify(TrayIcon trayIcon) throws AWTException {
                    trayIcon.addNotify();
                }
                public void removeNotify(TrayIcon trayIcon) {
                    trayIcon.removeNotify();
                }
            });
    }

    private TrayIcon()
      throws UnsupportedOperationException, HeadlessException, SecurityException
    {
        SystemTray.checkSystemTrayAllowed();
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        if (!SystemTray.isSupported()) {
            throw new UnsupportedOperationException();
        }
        SunToolkit.insertTargetMapping(this, AppContext.getAppContext());
    }

    /**
     * 使用指定的图像创建一个 <code>TrayIcon</code>。
     *
     * @param image 要使用的 <code>Image</code>
     * @throws IllegalArgumentException 如果 <code>image</code> 为
     * <code>null</code>
     * @throws UnsupportedOperationException 如果当前平台不支持系统托盘
     * @throws HeadlessException 如果
     * {@code GraphicsEnvironment.isHeadless()} 返回 {@code true}
     * @throws SecurityException 如果未授予 {@code accessSystemTray} 权限
     * @see SystemTray#add(TrayIcon)
     * @see TrayIcon#TrayIcon(Image, String, PopupMenu)
     * @see TrayIcon#TrayIcon(Image, String)
     * @see SecurityManager#checkPermission
     * @see AWTPermission
     */
    public TrayIcon(Image image) {
        this();
        if (image == null) {
            throw new IllegalArgumentException("创建 TrayIcon 时使用了 null 图像");
        }
        setImage(image);
    }

    /**
     * 使用指定的图像和提示文本创建一个 <code>TrayIcon</code>。
     *
     * @param image 要使用的 <code>Image</code>
     * @param tooltip 要用作提示文本的字符串；如果值为 <code>null</code> 则不显示提示
     * @throws IllegalArgumentException 如果 <code>image</code> 为
     * <code>null</code>
     * @throws UnsupportedOperationException 如果当前平台不支持系统托盘
     * @throws HeadlessException 如果
     * {@code GraphicsEnvironment.isHeadless()} 返回 {@code true}
     * @throws SecurityException 如果未授予 {@code accessSystemTray} 权限
     * @see SystemTray#add(TrayIcon)
     * @see TrayIcon#TrayIcon(Image)
     * @see TrayIcon#TrayIcon(Image, String, PopupMenu)
     * @see SecurityManager#checkPermission
     * @see AWTPermission
     */
    public TrayIcon(Image image, String tooltip) {
        this(image);
        setToolTip(tooltip);
    }

    /**
     * 使用指定的图像、提示文本和弹出菜单创建一个 <code>TrayIcon</code>。
     *
     * @param image 要使用的 <code>Image</code>
     * @param tooltip 要用作提示文本的字符串；如果值为 <code>null</code> 则不显示提示
     * @param popup 要用作托盘图标弹出菜单的菜单；如果值为 <code>null</code> 则不显示弹出菜单
     * @throws IllegalArgumentException 如果 <code>image</code> 为 <code>null</code>
     * @throws UnsupportedOperationException 如果当前平台不支持系统托盘
     * @throws HeadlessException 如果
     * {@code GraphicsEnvironment.isHeadless()} 返回 {@code true}
     * @throws SecurityException 如果未授予 {@code accessSystemTray} 权限
     * @see SystemTray#add(TrayIcon)
     * @see TrayIcon#TrayIcon(Image, String)
     * @see TrayIcon#TrayIcon(Image)
     * @see PopupMenu
     * @see MouseListener
     * @see #addMouseListener(MouseListener)
     * @see SecurityManager#checkPermission
     * @see AWTPermission
     */
    public TrayIcon(Image image, String tooltip, PopupMenu popup) {
        this(image, tooltip);
        setPopupMenu(popup);
    }

    /**
     * 为这个 <code>TrayIcon</code> 设置图像。之前的托盘图标图像将被丢弃，而不会调用 {@link
     * java.awt.Image#flush} 方法 — 您需要手动调用它。
     *
     * <p> 如果图像是动画图像，它将自动播放。
     *
     * <p> 有关显示图像大小的详细信息，请参阅 {@link #setImageAutoSize(boolean)} 属性。
     *
     * <p> 使用当前正在使用的相同图像调用此方法将不会产生任何效果。
     *
     * @throws NullPointerException 如果 <code>image</code> 为 <code>null</code>
     * @param image 要使用的非空 <code>Image</code>
     * @see #getImage
     * @see Image
     * @see SystemTray#add(TrayIcon)
     * @see TrayIcon#TrayIcon(Image, String)
     */
    public void setImage(Image image) {
        if (image == null) {
            throw new NullPointerException("设置 null 图像");
        }
        this.image = image;

        TrayIconPeer peer = this.peer;
        if (peer != null) {
            peer.updateImage();
        }
    }

    /**
     * 返回此 <code>TrayIcon</code> 当前使用的图像。
     *
     * @return 图像
     * @see #setImage(Image)
     * @see Image
     */
    public Image getImage() {
        return image;
    }

    /**
     * 为这个 <code>TrayIcon</code> 设置弹出菜单。如果 <code>popup</code> 为 <code>null</code>，则不会
     * 为此 <code>TrayIcon</code> 关联任何弹出菜单。
     *
     * <p>注意，此 <code>popup</code> 在设置到托盘图标之前或之后都不能添加到任何父级。如果您将其添加到某些父级，
     * <code>popup</code> 可能会被从该父级中移除。
     *
     * <p>此 <code>popup</code> 只能设置在一个 <code>TrayIcon</code> 上。将相同的弹出菜单设置到多个
     * <code>TrayIcon</code> 上将导致 <code>IllegalArgumentException</code>。
     *
     * <p><strong>注意：</strong> 某些平台可能不支持在用户右击托盘图标时显示用户指定的弹出菜单组件。在这种情况下，
     * 要么不会显示任何菜单，要么在某些系统上会显示一个原生版本的菜单。
     *
     * @throws IllegalArgumentException 如果此 <code>popup</code> 已经设置为另一个 <code>TrayIcon</code>
     * @param popup 一个 <code>PopupMenu</code> 或 <code>null</code> 以移除任何弹出菜单
     * @see #getPopupMenu
     */
    public void setPopupMenu(PopupMenu popup) {
        if (popup == this.popup) {
            return;
        }
        synchronized (TrayIcon.class) {
            if (popup != null) {
                if (popup.isTrayIconPopup) {
                    throw new IllegalArgumentException("此 PopupMenu 已经设置为另一个 TrayIcon");
                }
                popup.isTrayIconPopup = true;
            }
            if (this.popup != null) {
                this.popup.isTrayIconPopup = false;
            }
            this.popup = popup;
        }
    }

    /**
     * 返回与此 <code>TrayIcon</code> 关联的弹出菜单。
     *
     * @return 弹出菜单或 <code>null</code> 如果不存在
     * @see #setPopupMenu(PopupMenu)
     */
    public PopupMenu getPopupMenu() {
        return popup;
    }

    /**
     * 为这个 <code>TrayIcon</code> 设置提示字符串。当鼠标悬停在图标上时，提示会自动显示。将提示设置为
     * <code>null</code> 会移除任何提示文本。
     *
     * 当显示时，提示字符串在某些平台上可能会被截断；可显示的字符数取决于平台。
     *
     * @param tooltip 要用作提示的字符串；如果值为 <code>null</code> 则不显示提示
     * @see #getToolTip
     */
    public void setToolTip(String tooltip) {
        this.tooltip = tooltip;

        TrayIconPeer peer = this.peer;
        if (peer != null) {
            peer.setToolTip(tooltip);
        }
    }

    /**
     * 返回与此 <code>TrayIcon</code> 关联的提示字符串。
     *
     * @return 提示字符串或 <code>null</code> 如果不存在
     * @see #setToolTip(String)
     */
    public String getToolTip() {
        return tooltip;
    }

    /**
     * 设置自动调整大小属性。自动调整大小确定托盘图像是否自动调整大小以适应托盘上分配给图像的空间。默认情况下，
     * 自动调整大小属性设置为 <code>false</code>。
     *
     * <p> 如果自动调整大小为 <code>false</code>，且图像大小与托盘图标空间不匹配，图像将按原样绘制在该空间内 —
     * 如果大于分配的空间，它将被裁剪。
     *
     * <p> 如果自动调整大小为 <code>true</code>，图像将被拉伸或缩小以适应托盘图标空间。
     *
     * @param autosize <code>true</code> 以自动调整图像大小，<code>false</code> 否则
     * @see #isImageAutoSize
     */
    public void setImageAutoSize(boolean autosize) {
        this.autosize = autosize;

        TrayIconPeer peer = this.peer;
        if (peer != null) {
            peer.updateImage();
        }
    }

    /**
     * 返回自动调整大小属性的值。
     *
     * @return <code>true</code> 如果图像将自动调整大小，<code>false</code> 否则
     * @see #setImageAutoSize(boolean)
     */
    public boolean isImageAutoSize() {
        return autosize;
    }

    /**
     * 为这个 <code>TrayIcon</code> 添加指定的鼠标监听器以接收鼠标事件。使用 <code>null</code> 值调用此方法将不会产生任何效果。
     *
     * <p><b>注意</b>: 从 <code>TrayIcon</code> 接收到的 {@code MouseEvent} 的坐标相对于屏幕，而不是
     * <code>TrayIcon</code>。
     *
     * <p> <b>注意: </b> 不支持 <code>MOUSE_ENTERED</code> 和 <code>MOUSE_EXITED</code> 鼠标事件。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    listener 鼠标监听器
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseListener
     * @see      #removeMouseListener(MouseListener)
     * @see      #getMouseListeners
     */
    public synchronized void addMouseListener(MouseListener listener) {
        if (listener == null) {
            return;
        }
        mouseListener = AWTEventMulticaster.add(mouseListener, listener);
    }

    /**
     * 移除指定的鼠标监听器。使用 <code>null</code> 或无效值调用此方法将不会产生任何效果。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    listener   鼠标监听器
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseListener
     * @see      #addMouseListener(MouseListener)
     * @see      #getMouseListeners
     */
    public synchronized void removeMouseListener(MouseListener listener) {
        if (listener == null) {
            return;
        }
        mouseListener = AWTEventMulticaster.remove(mouseListener, listener);
    }


                /**
     * 返回注册到此 <code>TrayIcon</code> 的所有鼠标监听器的数组。
     *
     * @return 注册到此 <code>TrayIcon</code> 的所有 <code>MouseListeners</code>，如果当前没有注册鼠标监听器，则返回空数组
     *
     * @see      #addMouseListener(MouseListener)
     * @see      #removeMouseListener(MouseListener)
     * @see      java.awt.event.MouseListener
     */
    public synchronized MouseListener[] getMouseListeners() {
        return AWTEventMulticaster.getListeners(mouseListener, MouseListener.class);
    }

    /**
     * 添加指定的鼠标监听器以接收来自此 <code>TrayIcon</code> 的鼠标移动事件。使用 <code>null</code> 值调用此方法没有效果。
     *
     * <p><b>注意</b>: {@code MouseEvent} 的坐标（从 {@code TrayIcon} 接收）相对于屏幕，而不是 {@code TrayIcon}。
     *
     * <p> <b>注意: </b><code>MOUSE_DRAGGED</code> 鼠标事件不受支持。
     * <p>有关 AWT 的线程模型，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    listener   鼠标监听器
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseMotionListener
     * @see      #removeMouseMotionListener(MouseMotionListener)
     * @see      #getMouseMotionListeners
     */
    public synchronized void addMouseMotionListener(MouseMotionListener listener) {
        if (listener == null) {
            return;
        }
        mouseMotionListener = AWTEventMulticaster.add(mouseMotionListener, listener);
    }

    /**
     * 移除指定的鼠标移动监听器。使用 <code>null</code> 或无效值调用此方法没有效果。
     * <p>有关 AWT 的线程模型，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    listener   鼠标监听器
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseMotionListener
     * @see      #addMouseMotionListener(MouseMotionListener)
     * @see      #getMouseMotionListeners
     */
    public synchronized void removeMouseMotionListener(MouseMotionListener listener) {
        if (listener == null) {
            return;
        }
        mouseMotionListener = AWTEventMulticaster.remove(mouseMotionListener, listener);
    }

    /**
     * 返回注册到此 <code>TrayIcon</code> 的所有鼠标移动监听器的数组。
     *
     * @return 注册到此 <code>TrayIcon</code> 的所有 <code>MouseInputListeners</code>，如果当前没有注册鼠标监听器，则返回空数组
     *
     * @see      #addMouseMotionListener(MouseMotionListener)
     * @see      #removeMouseMotionListener(MouseMotionListener)
     * @see      java.awt.event.MouseMotionListener
     */
    public synchronized MouseMotionListener[] getMouseMotionListeners() {
        return AWTEventMulticaster.getListeners(mouseMotionListener, MouseMotionListener.class);
    }

    /**
     * 返回此托盘图标触发的动作事件的命令名称。
     *
     * @return 动作命令名称，如果不存在则返回 <code>null</code>
     * @see #addActionListener(ActionListener)
     * @see #setActionCommand(String)
     */
    public String getActionCommand() {
        return actionCommand;
    }

    /**
     * 设置此托盘图标触发的动作事件的命令名称。默认情况下，此动作命令设置为 <code>null</code>。
     *
     * @param command  用于设置托盘图标动作命令的字符串。
     * @see java.awt.event.ActionEvent
     * @see #addActionListener(ActionListener)
     * @see #getActionCommand
     */
    public void setActionCommand(String command) {
        actionCommand = command;
    }

    /**
     * 添加指定的动作监听器以接收来自此 <code>TrayIcon</code> 的 <code>ActionEvent</code>。
     * 动作事件通常在用户使用鼠标或键盘选择托盘图标时发生。生成动作事件的条件是平台相关的。
     *
     * <p>使用 <code>null</code> 值调用此方法没有效果。
     * <p>有关 AWT 的线程模型，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param         listener 动作监听器
     * @see           #removeActionListener
     * @see           #getActionListeners
     * @see           java.awt.event.ActionListener
     * @see #setActionCommand(String)
     */
    public synchronized void addActionListener(ActionListener listener) {
        if (listener == null) {
            return;
        }
        actionListener = AWTEventMulticaster.add(actionListener, listener);
    }

    /**
     * 移除指定的动作监听器。使用 <code>null</code> 或无效值调用此方法没有效果。
     * <p>有关 AWT 的线程模型，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    listener   动作监听器
     * @see      java.awt.event.ActionEvent
     * @see      java.awt.event.ActionListener
     * @see      #addActionListener(ActionListener)
     * @see      #getActionListeners
     * @see #setActionCommand(String)
     */
    public synchronized void removeActionListener(ActionListener listener) {
        if (listener == null) {
            return;
        }
        actionListener = AWTEventMulticaster.remove(actionListener, listener);
    }

    /**
     * 返回注册到此 <code>TrayIcon</code> 的所有动作监听器的数组。
     *
     * @return 注册到此 <code>TrayIcon</code> 的所有 <code>ActionListeners</code>，如果当前没有注册动作监听器，则返回空数组
     *
     * @see      #addActionListener(ActionListener)
     * @see      #removeActionListener(ActionListener)
     * @see      java.awt.event.ActionListener
     */
    public synchronized ActionListener[] getActionListeners() {
        return AWTEventMulticaster.getListeners(actionListener, ActionListener.class);
    }

    /**
     * 消息类型确定在消息标题中显示的图标，以及消息显示时可能生成的系统声音。
     *
     * @see TrayIcon
     * @see TrayIcon#displayMessage(String, String, MessageType)
     * @since 1.6
     */
    public enum MessageType {
        /** 错误消息 */
        ERROR,
        /** 警告消息 */
        WARNING,
        /** 信息消息 */
        INFO,
        /** 简单消息 */
        NONE
    };

    /**
     * 在托盘图标附近显示一个弹出消息。消息将在一段时间后消失，或者在用户点击它时消失。点击消息可能会触发一个 {@code ActionEvent}。
     *
     * <p>标题或文本可以是 <code>null</code>，但如果两者都是 <code>null</code>，则抛出 <code>NullPointerException</code>。
     *
     * 当显示时，某些平台上标题或文本字符串可能会被截断；可显示的字符数是平台相关的。
     *
     * <p><strong>注意:</strong> 某些平台可能不支持显示消息。
     *
     * @param caption 显示在文本上方的标题，通常为粗体；可以是 <code>null</code>
     * @param text 用于特定消息的文本；可以是 <code>null</code>
     * @param messageType 枚举，指示消息类型
     * @throws NullPointerException 如果 <code>caption</code> 和 <code>text</code> 都是 <code>null</code>
     */
    public void displayMessage(String caption, String text, MessageType messageType) {
        if (caption == null && text == null) {
            throw new NullPointerException("displaying the message with both caption and text being null");
        }

        TrayIconPeer peer = this.peer;
        if (peer != null) {
            peer.displayMessage(caption, text, messageType.name());
        }
    }

    /**
     * 返回托盘图标在系统托盘中占用的空间的大小（以像素为单位）。对于尚未添加到系统托盘的托盘图标，返回的大小等于 {@link SystemTray#getTrayIconSize} 的结果。
     *
     * @return 托盘图标的大小，以像素为单位
     * @see TrayIcon#setImageAutoSize(boolean)
     * @see java.awt.Image
     * @see TrayIcon#getSize()
     */
    public Dimension getSize() {
        return SystemTray.getSystemTray().getTrayIconSize();
    }

    // ****************************************************************
    // ****************************************************************

    void addNotify()
      throws AWTException
    {
        synchronized (this) {
            if (peer == null) {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                if (toolkit instanceof SunToolkit) {
                    peer = ((SunToolkit)Toolkit.getDefaultToolkit()).createTrayIcon(this);
                } else if (toolkit instanceof HeadlessToolkit) {
                    peer = ((HeadlessToolkit)Toolkit.getDefaultToolkit()).createTrayIcon(this);
                }
            }
        }
        peer.setToolTip(tooltip);
    }

    void removeNotify() {
        TrayIconPeer p = null;
        synchronized (this) {
            p = peer;
            peer = null;
        }
        if (p != null) {
            p.dispose();
        }
    }

    void setID(int id) {
        this.id = id;
    }

    int getID(){
        return id;
    }

    void dispatchEvent(AWTEvent e) {
        EventQueue.setCurrentEventAndMostRecentTime(e);
        Toolkit.getDefaultToolkit().notifyAWTEventListeners(e);
        processEvent(e);
    }

    void processEvent(AWTEvent e) {
        if (e instanceof MouseEvent) {
            switch(e.getID()) {
            case MouseEvent.MOUSE_PRESSED:
            case MouseEvent.MOUSE_RELEASED:
            case MouseEvent.MOUSE_CLICKED:
                processMouseEvent((MouseEvent)e);
                break;
            case MouseEvent.MOUSE_MOVED:
                processMouseMotionEvent((MouseEvent)e);
                break;
            default:
                return;
            }
        } else if (e instanceof ActionEvent) {
            processActionEvent((ActionEvent)e);
        }
    }

    void processMouseEvent(MouseEvent e) {
        MouseListener listener = mouseListener;

        if (listener != null) {
            int id = e.getID();
            switch(id) {
            case MouseEvent.MOUSE_PRESSED:
                listener.mousePressed(e);
                break;
            case MouseEvent.MOUSE_RELEASED:
                listener.mouseReleased(e);
                break;
            case MouseEvent.MOUSE_CLICKED:
                listener.mouseClicked(e);
                break;
            default:
                return;
            }
        }
    }

    void processMouseMotionEvent(MouseEvent e) {
        MouseMotionListener listener = mouseMotionListener;
        if (listener != null &&
            e.getID() == MouseEvent.MOUSE_MOVED)
        {
            listener.mouseMoved(e);
        }
    }

    void processActionEvent(ActionEvent e) {
        ActionListener listener = actionListener;
        if (listener != null) {
            listener.actionPerformed(e);
        }
    }

    private static native void initIDs();
}
