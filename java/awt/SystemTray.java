
/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Vector;
import java.awt.peer.SystemTrayPeer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.HeadlessToolkit;
import sun.security.util.SecurityConstants;
import sun.awt.AWTAccessor;

/**
 * <code>SystemTray</code> 类表示桌面的系统托盘。在 Microsoft Windows 上，它被称为“任务栏状态区域”，在 Gnome 上被称为“通知区域”，在 KDE 上被称为“系统托盘”。系统托盘由桌面上运行的所有应用程序共享。
 *
 * <p> 在某些平台上，系统托盘可能不存在或不受支持，在这种情况下，{@link SystemTray#getSystemTray()} 抛出 {@link UnsupportedOperationException}。要检测系统托盘是否受支持，使用 {@link SystemTray#isSupported}。
 *
 * <p><code>SystemTray</code> 可以包含一个或多个 {@link TrayIcon TrayIcons}，这些图标通过 {@link #add} 方法添加到托盘中，不再需要时通过 {@link #remove} 方法移除。<code>TrayIcon</code> 包括一个图像、一个弹出菜单和一组关联的监听器。请参阅 {@link TrayIcon} 类以获取详细信息。
 *
 * <p>每个 Java 应用程序都有一个 <code>SystemTray</code> 实例，允许应用程序在运行时与桌面的系统托盘进行交互。可以通过 {@link #getSystemTray} 方法获取 <code>SystemTray</code> 实例。应用程序不能创建自己的 <code>SystemTray</code> 实例。
 *
 * <p>以下代码片段演示了如何访问和自定义系统托盘：
 * <pre>
 * <code>
 *     {@link TrayIcon} trayIcon = null;
 *     if (SystemTray.isSupported()) {
 *         // 获取 SystemTray 实例
 *         SystemTray tray = SystemTray.{@link #getSystemTray};
 *         // 加载图像
 *         {@link java.awt.Image} image = {@link java.awt.Toolkit#getImage(String) Toolkit.getDefaultToolkit().getImage}(...);
 *         // 创建一个动作监听器，用于监听托盘图标上的默认动作
 *         {@link java.awt.event.ActionListener} listener = new {@link java.awt.event.ActionListener ActionListener}() {
 *             public void {@link java.awt.event.ActionListener#actionPerformed actionPerformed}({@link java.awt.event.ActionEvent} e) {
 *                 // 执行应用程序的默认动作
 *                 // ...
 *             }
 *         };
 *         // 创建一个弹出菜单
 *         {@link java.awt.PopupMenu} popup = new {@link java.awt.PopupMenu#PopupMenu PopupMenu}();
 *         // 创建默认动作的菜单项
 *         MenuItem defaultItem = new MenuItem(...);
 *         defaultItem.addActionListener(listener);
 *         popup.add(defaultItem);
 *         /// ... 添加其他项
 *         // 构建一个 TrayIcon
 *         trayIcon = new {@link TrayIcon#TrayIcon(java.awt.Image, String, java.awt.PopupMenu) TrayIcon}(image, "托盘演示", popup);
 *         // 设置 TrayIcon 属性
 *         trayIcon.{@link TrayIcon#addActionListener(java.awt.event.ActionListener) addActionListener}(listener);
 *         // ...
 *         // 添加托盘图像
 *         try {
 *             tray.{@link SystemTray#add(TrayIcon) add}(trayIcon);
 *         } catch (AWTException e) {
 *             System.err.println(e);
 *         }
 *         // ...
 *     } else {
 *         // 禁用应用程序中的托盘选项或
 *         // 执行其他操作
 *         ...
 *     }
 *     // ...
 *     // 一段时间后
 *     // 应用程序状态已更改 - 更新图像
 *     if (trayIcon != null) {
 *         trayIcon.{@link TrayIcon#setImage(java.awt.Image) setImage}(updatedImage);
 *     }
 *     // ...
 * </code>
 * </pre>
 *
 * @since 1.6
 * @see TrayIcon
 *
 * @author Bino George
 * @author Denis Mikhalkin
 * @author Sharon Zakhour
 * @author Anton Tarasov
 */
public class SystemTray {
    private static SystemTray systemTray;
    private int currentIconID = 0; // 每个添加的 TrayIcon 获得一个唯一的 ID

    transient private SystemTrayPeer peer;

    private static final TrayIcon[] EMPTY_TRAY_ARRAY = new TrayIcon[0];

    static {
        AWTAccessor.setSystemTrayAccessor(
            new AWTAccessor.SystemTrayAccessor() {
                public void firePropertyChange(SystemTray tray,
                                               String propertyName,
                                               Object oldValue,
                                               Object newValue) {
                    tray.firePropertyChange(propertyName, oldValue, newValue);
                }
            });
    }

    /**
     * 私有的 <code>SystemTray</code> 构造函数。
     *
     */
    private SystemTray() {
        addNotify();
    }

    /**
     * 获取表示桌面托盘区域的 <code>SystemTray</code> 实例。每个应用程序始终返回相同的实例。在某些平台上，系统托盘可能不受支持。可以使用 {@link #isSupported} 方法检查系统托盘是否受支持。
     *
     * <p>如果安装了 SecurityManager，则必须授予 AWTPermission {@code accessSystemTray} 以获取 <code>SystemTray</code> 实例。否则，此方法将抛出 SecurityException。
     *
     * @return 表示桌面托盘区域的 <code>SystemTray</code> 实例
     * @throws UnsupportedOperationException 如果当前平台不支持系统托盘
     * @throws HeadlessException 如果 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code>
     * @throws SecurityException 如果未授予 {@code accessSystemTray} 权限
     * @see #add(TrayIcon)
     * @see TrayIcon
     * @see #isSupported
     * @see SecurityManager#checkPermission
     * @see AWTPermission
     */
    public static SystemTray getSystemTray() {
        checkSystemTrayAllowed();
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }

        initializeSystemTrayIfNeeded();

        if (!isSupported()) {
            throw new UnsupportedOperationException(
                "当前平台不支持系统托盘。");
        }

        return systemTray;
    }

    /**
     * 返回当前平台是否支持系统托盘。除了显示托盘图标外，最小的系统托盘支持包括弹出菜单（参见 {@link TrayIcon#setPopupMenu(PopupMenu)}) 或动作事件（参见 {@link TrayIcon#addActionListener(ActionListener)}）。
     *
     * <p>开发人员不应假设所有系统托盘功能都受支持。为了确保托盘图标的默认动作始终可访问，应将默认动作同时添加到动作监听器和弹出菜单中。请参阅 {@link SystemTray 示例} 以了解如何做到这一点。
     *
     * <p><b>注意</b>：在实现 <code>SystemTray</code> 和 <code>TrayIcon</code> 时，<em>强烈建议</em> 为弹出菜单和动作事件分配不同的手势。为两个目的重载手势会导致混淆，并可能阻止用户访问其中一个或另一个。
     *
     * @see #getSystemTray
     * @return <code>false</code> 如果不支持系统托盘访问；如果支持最小的系统托盘访问但不保证当前平台支持所有系统托盘功能，则返回 <code>true</code>
     */
    public static boolean isSupported() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (toolkit instanceof SunToolkit) {
            // 将托盘连接到本机资源
            initializeSystemTrayIfNeeded();
            return ((SunToolkit)toolkit).isTraySupported();
        } else if (toolkit instanceof HeadlessToolkit) {
            // 跳过初始化，因为初始化例程会抛出 HeadlessException
            return ((HeadlessToolkit)toolkit).isTraySupported();
        } else {
            return false;
        }
    }

    /**
     * 将 <code>TrayIcon</code> 添加到 <code>SystemTray</code>。托盘图标一旦添加，就会在系统托盘中可见。图标在托盘中的显示顺序未指定，具体取决于平台和实现。
     *
     * <p> 应用程序添加的所有图标在应用程序退出时以及桌面系统托盘不可用时，都会自动从 <code>SystemTray</code> 中移除。
     *
     * @param trayIcon 要添加的 <code>TrayIcon</code>
     * @throws NullPointerException 如果 <code>trayIcon</code> 为 <code>null</code>
     * @throws IllegalArgumentException 如果添加了相同的 <code>TrayIcon</code> 实例多次
     * @throws AWTException 如果桌面系统托盘缺失
     * @see #remove(TrayIcon)
     * @see #getSystemTray
     * @see TrayIcon
     * @see java.awt.Image
     */
    public void add(TrayIcon trayIcon) throws AWTException {
        if (trayIcon == null) {
            throw new NullPointerException("添加了 null 的 TrayIcon");
        }
        TrayIcon[] oldArray = null, newArray = null;
        Vector<TrayIcon> icons = null;
        synchronized (this) {
            oldArray = systemTray.getTrayIcons();
            icons = (Vector<TrayIcon>)AppContext.getAppContext().get(TrayIcon.class);
            if (icons == null) {
                icons = new Vector<TrayIcon>(3);
                AppContext.getAppContext().put(TrayIcon.class, icons);

            } else if (icons.contains(trayIcon)) {
                throw new IllegalArgumentException("添加了已添加的 TrayIcon");
            }
            icons.add(trayIcon);
            newArray = systemTray.getTrayIcons();

            trayIcon.setID(++currentIconID);
        }
        try {
            trayIcon.addNotify();
        } catch (AWTException e) {
            icons.remove(trayIcon);
            throw e;
        }
        firePropertyChange("trayIcons", oldArray, newArray);
    }

    /**
     * 从 <code>SystemTray</code> 中移除指定的 <code>TrayIcon</code>。
     *
     * <p> 应用程序添加的所有图标在应用程序退出时以及桌面系统托盘不可用时，都会自动从 <code>SystemTray</code> 中移除。
     *
     * <p> 如果 <code>trayIcon</code> 为 <code>null</code> 或未添加到系统托盘，则不抛出异常，也不执行任何操作。
     *
     * @param trayIcon 要移除的 <code>TrayIcon</code>
     * @see #add(TrayIcon)
     * @see TrayIcon
     */
    public void remove(TrayIcon trayIcon) {
        if (trayIcon == null) {
            return;
        }
        TrayIcon[] oldArray = null, newArray = null;
        synchronized (this) {
            oldArray = systemTray.getTrayIcons();
            Vector<TrayIcon> icons = (Vector<TrayIcon>)AppContext.getAppContext().get(TrayIcon.class);
            // 没有对等体的 TrayIcon 不包含在数组中。
            if (icons == null || !icons.remove(trayIcon)) {
                return;
            }
            trayIcon.removeNotify();
            newArray = systemTray.getTrayIcons();
        }
        firePropertyChange("trayIcons", oldArray, newArray);
    }

    /**
     * 返回此应用程序添加到托盘的所有图标。不能访问其他应用程序添加的图标。某些浏览器将不同代码库中的小程序分区为不同的上下文，并在这些上下文之间建立隔离。在这种情况下，仅返回从此上下文添加的托盘图标。
     *
     * <p> 返回的数组是实际数组的副本，可以以任何方式修改，而不会影响系统托盘。要从 <code>SystemTray</code> 中移除 <code>TrayIcon</code>，请使用 {@link #remove(TrayIcon)} 方法。
     *
     * @return 添加到此托盘的所有托盘图标数组，如果没有添加任何图标，则返回空数组
     * @see #add(TrayIcon)
     * @see TrayIcon
     */
    public TrayIcon[] getTrayIcons() {
        Vector<TrayIcon> icons = (Vector<TrayIcon>)AppContext.getAppContext().get(TrayIcon.class);
        if (icons != null) {
            return (TrayIcon[])icons.toArray(new TrayIcon[icons.size()]);
        }
        return EMPTY_TRAY_ARRAY;
    }

    /**
     * 返回托盘图标在系统托盘中占用的空间大小（以像素为单位）。开发人员可以使用此方法在创建托盘图标之前获取图像属性的推荐大小。为了方便，<code>TrayIcon</code> 类中也有一个类似的方法 {@link TrayIcon#getSize}。
     *
     * @return 托盘图标的默认大小，以像素为单位
     * @see TrayIcon#setImageAutoSize(boolean)
     * @see java.awt.Image
     * @see TrayIcon#getSize()
     */
    public Dimension getTrayIconSize() {
        return peer.getTrayIconSize();
    }

    /**
     * 将 {@code PropertyChangeListener} 添加到特定属性的监听器列表中。目前支持以下属性：
     *
     * <table border=1 summary="SystemTray 属性">
     * <tr>
     *    <th>属性</th>
     *    <th>描述</th>
     * </tr>
     * <tr>
     *    <td>{@code trayIcons}</td>
     *    <td><code>SystemTray</code> 的 <code>TrayIcon</code> 对象数组。<br>
     *        该数组通过 {@link #getTrayIcons} 方法访问。<br>
     *        当托盘图标添加到（或从）系统托盘中移除时，此属性会更改。<br>
     *        例如，当桌面系统托盘不可用时，托盘图标会自动移除，此时此属性会更改。</td>
     * </tr>
     * <tr>
     *    <td>{@code systemTray}</td>
     *    <td>当系统托盘可用时，此属性包含 <code>SystemTray</code> 实例，否则为 <code>null</code>。<br>
     *        当桌面系统托盘可用或不可用时，此属性会更改。<br>
     *        该属性通过 {@link #getSystemTray} 方法访问。</td>
     * </tr>
     * </table>
     * <p>
     * {@code listener} 仅监听此上下文中的属性更改。
     * <p>
     * 如果 {@code listener} 为 {@code null}，则不抛出异常，也不执行任何操作。
     *
     * @param propertyName 指定的属性
     * @param listener 要添加的属性更改监听器
     *
     * @see #removePropertyChangeListener
     * @see #getPropertyChangeListeners
     */
    public synchronized void addPropertyChangeListener(String propertyName,
                                                       PropertyChangeListener listener)
    {
        if (listener == null) {
            return;
        }
        getCurrentChangeSupport().addPropertyChangeListener(propertyName, listener);
    }


                /**
     * 从监听器列表中移除一个 {@code PropertyChangeListener}，针对特定属性。
     * <p>
     * 该 {@code PropertyChangeListener} 必须来自此上下文。
     * <p>
     * 如果 {@code propertyName} 或 {@code listener} 为 {@code null} 或无效，
     * 不会抛出异常，也不会采取任何行动。
     *
     * @param propertyName 指定的属性
     * @param listener 要移除的 PropertyChangeListener
     *
     * @see #addPropertyChangeListener
     * @see #getPropertyChangeListeners
     */
    public synchronized void removePropertyChangeListener(String propertyName,
                                                          PropertyChangeListener listener)
    {
        if (listener == null) {
            return;
        }
        getCurrentChangeSupport().removePropertyChangeListener(propertyName, listener);
    }

    /**
     * 返回与命名属性关联的所有监听器的数组。
     * <p>
     * 仅返回此上下文中的监听器。
     *
     * @param propertyName 指定的属性
     * @return 与命名属性关联的所有 {@code PropertyChangeListener}；如果未添加此类监听器或
     *         {@code propertyName} 为 {@code null} 或无效，则返回空数组
     *
     * @see #addPropertyChangeListener
     * @see #removePropertyChangeListener
     */
    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return getCurrentChangeSupport().getPropertyChangeListeners(propertyName);
    }


    // ***************************************************************
    // ***************************************************************


    /**
     * 用于报告对象属性的绑定属性更改。
     * 当绑定属性更改时可以调用此方法，它将向任何已注册的
     * PropertyChangeListeners 发送适当的 PropertyChangeEvent。
     *
     * @param propertyName 更改的属性
     * @param oldValue 属性的旧值
     * @param newValue 属性的新值
     */
    private void firePropertyChange(String propertyName,
                                    Object oldValue, Object newValue)
    {
        if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
            return;
        }
        getCurrentChangeSupport().firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * 返回调用线程上下文的当前 PropertyChangeSupport 实例。
     *
     * @return 当前线程上下文的 PropertyChangeSupport
     */
    private synchronized PropertyChangeSupport getCurrentChangeSupport() {
        PropertyChangeSupport changeSupport =
            (PropertyChangeSupport)AppContext.getAppContext().get(SystemTray.class);

        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport(this);
            AppContext.getAppContext().put(SystemTray.class, changeSupport);
        }
        return changeSupport;
    }

    synchronized void addNotify() {
        if (peer == null) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            if (toolkit instanceof SunToolkit) {
                peer = ((SunToolkit)Toolkit.getDefaultToolkit()).createSystemTray(this);
            } else if (toolkit instanceof HeadlessToolkit) {
                peer = ((HeadlessToolkit)Toolkit.getDefaultToolkit()).createSystemTray(this);
            }
        }
    }

    static void checkSystemTrayAllowed() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(SecurityConstants.AWT.ACCESS_SYSTEM_TRAY_PERMISSION);
        }
    }

    private static void initializeSystemTrayIfNeeded() {
        synchronized (SystemTray.class) {
            if (systemTray == null) {
                systemTray = new SystemTray();
            }
        }
    }
}
