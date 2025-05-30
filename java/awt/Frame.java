
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.peer.FramePeer;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.AWTAccessor;
import java.lang.ref.WeakReference;
import javax.accessibility.*;

/**
 * <code>Frame</code> 是一个带有标题和边框的顶级窗口。
 * <p>
 * 窗口的大小包括指定的边框区域。可以通过 <code>getInsets</code> 方法获取边框区域的尺寸，
 * 但是由于这些尺寸是平台相关的，因此在调用 <code>pack</code> 或 <code>show</code> 使窗口可显示之前，
 * 无法获得有效的内边距值。由于边框区域包含在窗口的整体大小中，边框实际上遮挡了窗口的一部分，
 * 使得可用于渲染和/或显示子组件的区域被限制在一个左上角位置为 <code>(insets.left, insets.top)</code>，
 * 大小为 <code>width - (insets.left + insets.right)</code> 乘以
 * <code>height - (insets.top + insets.bottom)</code> 的矩形中。
 * <p>
 * 窗口的默认布局是 <code>BorderLayout</code>。
 * <p>
 * 可以通过 <code>setUndecorated</code> 方法关闭窗口的本地装饰（即 <code>Frame</code>
 * 和 <code>Titlebar</code>）。这只能在窗口不可显示时进行。
 * <p>
 * 在多屏幕环境中，可以通过使用 <code>Frame(GraphicsConfiguration)</code> 或
 * <code>Frame(String title, GraphicsConfiguration)</code> 构造函数在不同的屏幕设备上创建 <code>Frame</code>。
 * <code>GraphicsConfiguration</code> 对象是目标屏幕设备的 <code>GraphicsConfiguration</code> 对象之一。
 * <p>
 * 在虚拟设备多屏幕环境中，桌面区域可能跨越多个物理屏幕设备，所有配置的边界都是相对于虚拟坐标系统的。
 * 虚拟坐标系统的原点位于主物理屏幕的左上角。根据主屏幕在虚拟设备中的位置，可能会出现负坐标，如以下图所示。
 * <p>
 * <img src="doc-files/MultiScreen.gif"
 * alt="虚拟设备包含三个物理屏幕和一个主物理屏幕的图。主物理屏幕显示 (0,0) 坐标，而不同的物理屏幕显示 (-80,-100) 坐标。"
 * style="float:center; margin: 7px 10px;">
 * <p>
 * 在这种环境中，调用 <code>setLocation</code> 时，必须传递虚拟坐标。类似地，
 * 调用 <code>getLocationOnScreen</code> 时，返回的是虚拟设备坐标。可以通过调用
 * <code>GraphicsConfiguration</code> 的 <code>getBounds</code> 方法找到其在虚拟坐标系统中的原点。
 * <p>
 * 以下代码将 <code>Frame</code> 的位置设置为相对于相应 <code>GraphicsConfiguration</code>
 * 的物理屏幕原点的 (10, 10)。如果不考虑 <code>GraphicsConfiguration</code> 的边界，
 * <code>Frame</code> 的位置将被设置为相对于虚拟坐标系统的 (10, 10)，并出现在主物理屏幕上，
 * 这可能与指定的 <code>GraphicsConfiguration</code> 的物理屏幕不同。
 *
 * <pre>
 *      Frame f = new Frame(GraphicsConfiguration gc);
 *      Rectangle bounds = gc.getBounds();
 *      f.setLocation(10 + bounds.x, 10 + bounds.y);
 * </pre>
 *
 * <p>
 * 窗口可以生成以下类型的 <code>WindowEvent</code>：
 * <ul>
 * <li><code>WINDOW_OPENED</code>
 * <li><code>WINDOW_CLOSING</code>：
 *     <br>如果程序在处理此事件时不显式隐藏或销毁窗口，则窗口关闭操作将被取消。
 * <li><code>WINDOW_CLOSED</code>
 * <li><code>WINDOW_ICONIFIED</code>
 * <li><code>WINDOW_DEICONIFIED</code>
 * <li><code>WINDOW_ACTIVATED</code>
 * <li><code>WINDOW_DEACTIVATED</code>
 * <li><code>WINDOW_GAINED_FOCUS</code>
 * <li><code>WINDOW_LOST_FOCUS</code>
 * <li><code>WINDOW_STATE_CHANGED</code>
 * </ul>
 *
 * @author      Sami Shaio
 * @see WindowEvent
 * @see Window#addWindowListener
 * @since       JDK1.0
 */
public class Frame extends Window implements MenuContainer {

    /* 注意：这些正在被淘汰；程序应使用 Cursor 类的变量。参见 Cursor 和 Component.setCursor。 */

   /**
    * @deprecated   被 <code>Cursor.DEFAULT_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     DEFAULT_CURSOR                  = Cursor.DEFAULT_CURSOR;

   /**
    * @deprecated   被 <code>Cursor.CROSSHAIR_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     CROSSHAIR_CURSOR                = Cursor.CROSSHAIR_CURSOR;

   /**
    * @deprecated   被 <code>Cursor.TEXT_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     TEXT_CURSOR                     = Cursor.TEXT_CURSOR;

   /**
    * @deprecated   被 <code>Cursor.WAIT_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     WAIT_CURSOR                     = Cursor.WAIT_CURSOR;

   /**
    * @deprecated   被 <code>Cursor.SW_RESIZE_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     SW_RESIZE_CURSOR                = Cursor.SW_RESIZE_CURSOR;

   /**
    * @deprecated   被 <code>Cursor.SE_RESIZE_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     SE_RESIZE_CURSOR                = Cursor.SE_RESIZE_CURSOR;

   /**
    * @deprecated   被 <code>Cursor.NW_RESIZE_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     NW_RESIZE_CURSOR                = Cursor.NW_RESIZE_CURSOR;

   /**
    * @deprecated   被 <code>Cursor.NE_RESIZE_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     NE_RESIZE_CURSOR                = Cursor.NE_RESIZE_CURSOR;

   /**
    * @deprecated   被 <code>Cursor.N_RESIZE_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     N_RESIZE_CURSOR                 = Cursor.N_RESIZE_CURSOR;

   /**
    * @deprecated   被 <code>Cursor.S_RESIZE_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     S_RESIZE_CURSOR                 = Cursor.S_RESIZE_CURSOR;

   /**
    * @deprecated   被 <code>Cursor.W_RESIZE_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     W_RESIZE_CURSOR                 = Cursor.W_RESIZE_CURSOR;

   /**
    * @deprecated   被 <code>Cursor.E_RESIZE_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     E_RESIZE_CURSOR                 = Cursor.E_RESIZE_CURSOR;

   /**
    * @deprecated   被 <code>Cursor.HAND_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     HAND_CURSOR                     = Cursor.HAND_CURSOR;

   /**
    * @deprecated   被 <code>Cursor.MOVE_CURSOR</code> 替代。
    */
    @Deprecated
    public static final int     MOVE_CURSOR                     = Cursor.MOVE_CURSOR;

    /**
     * 窗口处于“正常”状态。此符号常量命名了一个所有状态位都被清除的窗口状态。
     * @see #setExtendedState(int)
     * @see #getExtendedState
     */
    public static final int NORMAL = 0;

    /**
     * 此状态位表示窗口已最小化。
     * @see #setExtendedState(int)
     * @see #getExtendedState
     */
    public static final int ICONIFIED = 1;

    /**
     * 此状态位表示窗口在水平方向上已最大化。
     * @see #setExtendedState(int)
     * @see #getExtendedState
     * @since 1.4
     */
    public static final int MAXIMIZED_HORIZ = 2;

    /**
     * 此状态位表示窗口在垂直方向上已最大化。
     * @see #setExtendedState(int)
     * @see #getExtendedState
     * @since 1.4
     */
    public static final int MAXIMIZED_VERT = 4;

    /**
     * 此状态位掩码表示窗口已完全最大化（即在水平和垂直方向上都已最大化）。这只是
     * <code>MAXIMIZED_VERT&nbsp;|&nbsp;MAXIMIZED_HORIZ</code> 的一个方便别名。
     *
     * <p>测试窗口是否完全最大化的正确方法是
     * <pre>
     *     (state &amp; Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH
     * </pre>
     *
     * <p>测试窗口是否在某个方向上最大化的方法是
     * <pre>
     *     (state &amp; Frame.MAXIMIZED_BOTH) != 0
     * </pre>
     *
     * @see #setExtendedState(int)
     * @see #getExtendedState
     * @since 1.4
     */
    public static final int MAXIMIZED_BOTH = MAXIMIZED_VERT | MAXIMIZED_HORIZ;

    /**
     * 此窗口的最大化边界。
     * @see     #setMaximizedBounds(Rectangle)
     * @see     #getMaximizedBounds
     * @serial
     * @since 1.4
     */
    Rectangle maximizedBounds;

    /**
     * 这是窗口的标题。可以在任何时候更改。如果 <code>title</code> 为 null，则 <code>title</code> = ""。
     *
     * @serial
     * @see #getTitle
     * @see #setTitle(String)
     */
    String      title = "Untitled";

    /**
     * 窗口的菜单栏。如果 <code>menuBar</code> = null，则窗口将没有菜单栏。
     *
     * @serial
     * @see #getMenuBar
     * @see #setMenuBar(MenuBar)
     */
    MenuBar     menuBar;

    /**
     * 此字段指示窗口是否可调整大小。此属性可以在任何时候更改。
     * 如果窗口可调整大小，则 <code>resizable</code> 为 true，否则为 false。
     *
     * @serial
     * @see #isResizable()
     */
    boolean     resizable = true;

    /**
     * 此字段指示窗口是否无装饰。此属性只能在窗口不可显示时更改。
     * 如果窗口无装饰，则 <code>undecorated</code> 为 true，否则为 false。
     *
     * @serial
     * @see #setUndecorated(boolean)
     * @see #isUndecorated()
     * @see Component#isDisplayable()
     * @since 1.4
     */
    boolean undecorated = false;

    /**
     * <code>mbManagement</code> 仅由 Motif 实现使用。
     *
     * @serial
     */
    boolean     mbManagement = false;   /* 仅由 Motif 实现使用 */

    // XXX: uwe: 暂时滥用旧字段
    // 需要处理序列化
    private int state = NORMAL;

    /*
     * Frame 拥有的窗口。
     * 注意：在 1.2 中已被 Window.ownedWindowList 取代
     *
     * @serial
     * @see java.awt.Window#ownedWindowList
     */
    Vector<Window> ownedWindows;

    private static final String base = "frame";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = 2673458971256075116L;

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * 构造一个新的 <code>Frame</code> 实例，初始状态下不可见。窗口的标题为空。
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时抛出
     * @see java.awt.GraphicsEnvironment#isHeadless()
     * @see Component#setSize
     * @see Component#setVisible(boolean)
     */
    public Frame() throws HeadlessException {
        this("");
    }

    /**
     * 构造一个新的、初始状态下不可见的 <code>Frame</code>，具有指定的 <code>GraphicsConfiguration</code>。
     *
     * @param gc 目标屏幕设备的 <code>GraphicsConfiguration</code>。如果 <code>gc</code> 为 <code>null</code>，
     * 则假设为系统默认的 <code>GraphicsConfiguration</code>。
     * @exception IllegalArgumentException 如果 <code>gc</code> 不来自屏幕设备。
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时抛出
     * @see java.awt.GraphicsEnvironment#isHeadless()
     * @since     1.3
     */
    public Frame(GraphicsConfiguration gc) {
        this("", gc);
    }

    /**
     * 构造一个新的、初始状态下不可见的 <code>Frame</code> 对象，具有指定的标题。
     * @param title 要在窗口边框中显示的标题。如果 <code>title</code> 为 <code>null</code>，
     * 则视为空字符串，""。
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时抛出
     * @see java.awt.GraphicsEnvironment#isHeadless()
     * @see java.awt.Component#setSize
     * @see java.awt.Component#setVisible(boolean)
     * @see java.awt.GraphicsConfiguration#getBounds
     */
    public Frame(String title) throws HeadlessException {
        init(title, null);
    }

    /**
     * 构造一个新的、初始状态下不可见的 <code>Frame</code> 对象，具有指定的标题和
     * <code>GraphicsConfiguration</code>。
     * @param title 要在窗口边框中显示的标题。如果 <code>title</code> 为 <code>null</code>，
     * 则视为空字符串，""。
     * @param gc 目标屏幕设备的 <code>GraphicsConfiguration</code>。如果 <code>gc</code> 为 <code>null</code>，
     * 则假设为系统默认的 <code>GraphicsConfiguration</code>。
     * @exception IllegalArgumentException 如果 <code>gc</code> 不来自屏幕设备。
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时抛出
     * @see java.awt.GraphicsEnvironment#isHeadless()
     * @see java.awt.Component#setSize
     * @see java.awt.Component#setVisible(boolean)
     * @see java.awt.GraphicsConfiguration#getBounds
     * @since 1.3
     */
    public Frame(String title, GraphicsConfiguration gc) {
        super(gc);
        init(title, gc);
    }


                private void init(String title, GraphicsConfiguration gc) {
        this.title = title;
        SunToolkit.checkAndSetPolicy(this);
    }

    /**
     * 为该组件构建一个名称。当名称为 null 时，由 getName() 调用。
     */
    String constructComponentName() {
        synchronized (Frame.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 通过连接到本地屏幕资源使此 Frame 可显示。使 Frame 可显示将导致其任何子组件也变为可显示。
     * 该方法由工具包内部调用，不应由程序直接调用。
     * @see Component#isDisplayable
     * @see #removeNotify
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null) {
                peer = getToolkit().createFrame(this);
            }
            FramePeer p = (FramePeer)peer;
            MenuBar menuBar = this.menuBar;
            if (menuBar != null) {
                mbManagement = true;
                menuBar.addNotify();
                p.setMenuBar(menuBar);
            }
            p.setMaximizedBounds(maximizedBounds);
            super.addNotify();
        }
    }

    /**
     * 获取此 Frame 的标题。标题显示在 Frame 的边框中。
     * @return 该 Frame 的标题，或空字符串 ("")，如果此 Frame 没有标题。
     * @see #setTitle(String)
     */
    public String getTitle() {
        return title;
    }

    /**
     * 将此 Frame 的标题设置为指定的字符串。
     * @param title 要在 Frame 边框中显示的标题。
     *              如果值为 <code>null</code>，则视为空字符串，""。
     * @see #getTitle
     */
    public void setTitle(String title) {
        String oldTitle = this.title;
        if (title == null) {
            title = "";
        }


        synchronized(this) {
            this.title = title;
            FramePeer peer = (FramePeer)this.peer;
            if (peer != null) {
                peer.setTitle(title);
            }
        }
        firePropertyChange("title", oldTitle, title);
    }

    /**
     * 返回作为此 Frame 图标的图像。
     * <p>
     * 此方法已过时，仅保留向后兼容。请使用 {@link Window#getIconImages Window.getIconImages()} 代替。
     * <p>
     * 如果为 Window 指定了多个图像作为图标，此方法将返回列表中的第一个图像。
     *
     * @return 该 Frame 的图标图像，或 <code>null</code>，如果此 Frame 没有图标图像。
     * @see #setIconImage(Image)
     * @see Window#getIconImages()
     * @see Window#setIconImages
     */
    public Image getIconImage() {
        java.util.List<Image> icons = this.icons;
        if (icons != null) {
            if (icons.size() > 0) {
                return icons.get(0);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setIconImage(Image image) {
        super.setIconImage(image);
    }

    /**
     * 获取此 Frame 的菜单栏。
     * @return 该 Frame 的菜单栏，或 <code>null</code>，如果此 Frame 没有菜单栏。
     * @see #setMenuBar(MenuBar)
     */
    public MenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * 将此 Frame 的菜单栏设置为指定的菜单栏。
     * @param mb 要设置的菜单栏。
     *            如果此参数为 <code>null</code>，则移除此 Frame 上的任何现有菜单栏。
     * @see #getMenuBar
     */
    public void setMenuBar(MenuBar mb) {
        synchronized (getTreeLock()) {
            if (menuBar == mb) {
                return;
            }
            if ((mb != null) && (mb.parent != null)) {
                mb.parent.remove(mb);
            }
            if (menuBar != null) {
                remove(menuBar);
            }
            menuBar = mb;
            if (menuBar != null) {
                menuBar.parent = this;

                FramePeer peer = (FramePeer)this.peer;
                if (peer != null) {
                    mbManagement = true;
                    menuBar.addNotify();
                    invalidateIfValid();
                    peer.setMenuBar(menuBar);
                }
            }
        }
    }

    /**
     * 指示此 Frame 是否可由用户调整大小。
     * 默认情况下，所有 Frame 都是可调整大小的。
     * @return 如果用户可以调整此 Frame 的大小，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @see java.awt.Frame#setResizable(boolean)
     */
    public boolean isResizable() {
        return resizable;
    }

    /**
     * 设置此 Frame 是否可由用户调整大小。
     * @param resizable 如果此 Frame 可调整大小，则为 <code>true</code>；否则为 <code>false</code>。
     * @see java.awt.Frame#isResizable
     */
    public void setResizable(boolean resizable) {
        boolean oldResizable = this.resizable;
        boolean testvalid = false;

        synchronized (this) {
            this.resizable = resizable;
            FramePeer peer = (FramePeer)this.peer;
            if (peer != null) {
                peer.setResizable(resizable);
                testvalid = true;
            }
        }

        // 在某些平台上，更改可调整大小状态会影响 Frame 的内边距。如果可以，我们会在 peer 中调用 invalidate()，
        // 但我们需要保证在调用 invalidate() 时不持有 Frame 锁。
        if (testvalid) {
            invalidateIfValid();
        }
        firePropertyChange("resizable", oldResizable, resizable);
    }


    /**
     * 设置此 Frame 的状态（已过时）。
     * <p>
     * 在 JDK 的旧版本中，Frame 的状态只能是 NORMAL 或 ICONIFIED。自 JDK 1.4 起，支持的 Frame 状态集已扩展，
     * Frame 状态表示为按位掩码。
     * <p>
     * 为了与早期开发的应用程序兼容，此方法仍然只接受
     * {@code Frame.NORMAL} 和
     * {@code Frame.ICONIFIED}。此方法仅更改 Frame 的图标状态，不影响 Frame 状态的其他方面。如果
     * 传递给此方法的状态既不是 {@code Frame.NORMAL} 也不是 {@code Frame.ICONIFIED}，则此方法不执行任何操作。
     * <p>注意，如果给定平台不支持状态，则既不会更改状态，也不会更改 {@link #getState} 方法的返回值。
     * 应用程序可以通过 {@link java.awt.Toolkit#isFrameStateSupported} 方法确定是否支持特定状态。
     * <p><b>如果 Frame 当前在屏幕上可见</b>（{@link #isShowing} 方法返回
     * {@code true}），开发人员应检查通过 {@link java.awt.event.WindowStateListener} 收到的
     * {@code WindowEvent} 的 {@link java.awt.event.WindowEvent#getNewState} 方法的返回值，以确定状态是否实际更改。
     * <p><b>如果 Frame 当前不在屏幕上可见</b>，则可能不会生成事件。在这种情况下，开发人员可以假设状态在该方法返回后立即更改。
     * 后来，当调用 {@code setVisible(true)} 方法时，Frame 将尝试应用此状态。在这种情况下，接收任何
     * {@link java.awt.event.WindowEvent#WINDOW_STATE_CHANGED} 事件也不保证。
     *
     * @param state 要设置的状态，可以是 <code>Frame.NORMAL</code> 或 <code>Frame.ICONIFIED</code>。
     * @see #setExtendedState(int)
     * @see java.awt.Window#addWindowStateListener
     */
    public synchronized void setState(int state) {
        int current = getExtendedState();
        if (state == ICONIFIED && (current & ICONIFIED) == 0) {
            setExtendedState(current | ICONIFIED);
        }
        else if (state == NORMAL && (current & ICONIFIED) != 0) {
            setExtendedState(current & ~ICONIFIED);
        }
    }

    /**
     * 设置此 Frame 的状态。状态表示为按位掩码。
     * <ul>
     * <li><code>NORMAL</code>
     * <br>表示没有设置状态位。
     * <li><code>ICONIFIED</code>
     * <li><code>MAXIMIZED_HORIZ</code>
     * <li><code>MAXIMIZED_VERT</code>
     * <li><code>MAXIMIZED_BOTH</code>
     * <br>组合 <code>MAXIMIZED_HORIZ</code> 和 <code>MAXIMIZED_VERT</code>。
     * </ul>
     * <p>注意，如果给定平台不支持状态，则既不会更改状态，也不会更改 {@link #getExtendedState} 方法的返回值。
     * 应用程序可以通过 {@link java.awt.Toolkit#isFrameStateSupported} 方法确定是否支持特定状态。
     * <p><b>如果 Frame 当前在屏幕上可见</b>（{@link #isShowing} 方法返回
     * {@code true}），开发人员应检查通过 {@link java.awt.event.WindowStateListener} 收到的
     * {@code WindowEvent} 的 {@link java.awt.event.WindowEvent#getNewState} 方法的返回值，以确定状态是否实际更改。
     * <p><b>如果 Frame 当前不在屏幕上可见</b>，则可能不会生成事件。在这种情况下，开发人员可以假设状态在该方法返回后立即更改。
     * 后来，当调用 {@code setVisible(true)} 方法时，Frame 将尝试应用此状态。在这种情况下，接收任何
     * {@link java.awt.event.WindowEvent#WINDOW_STATE_CHANGED} 事件也不保证。
     *
     * @param state Frame 状态常量的按位掩码
     * @since   1.4
     * @see java.awt.Window#addWindowStateListener
     */
    public void setExtendedState(int state) {
        if ( !isFrameStateSupported( state ) ) {
            return;
        }
        synchronized (getObjectLock()) {
            this.state = state;
        }
        // peer.setState 必须在对象锁同步块之外调用，以避免可能的死锁
        FramePeer peer = (FramePeer)this.peer;
        if (peer != null) {
            peer.setState(state);
        }
    }
    private boolean isFrameStateSupported(int state) {
        if( !getToolkit().isFrameStateSupported( state ) ) {
            // * Toolkit.isFrameStateSupported 始终返回 false
            // 即使所有部分都支持的复合状态；
            // * 如果状态的一部分不支持，则状态不支持；
            // * MAXIMIZED_BOTH 不是复合状态。
            if( ((state & ICONIFIED) != 0) &&
                !getToolkit().isFrameStateSupported( ICONIFIED )) {
                return false;
            }else {
                state &= ~ICONIFIED;
            }
            return getToolkit().isFrameStateSupported( state );
        }
        return true;
    }

    /**
     * 获取此 Frame 的状态（已过时）。
     * <p>
     * 在 JDK 的旧版本中，Frame 的状态只能是 NORMAL 或 ICONIFIED。自 JDK 1.4 起，支持的 Frame 状态集已扩展，
     * Frame 状态表示为按位掩码。
     * <p>
     * 为了与旧程序兼容，此方法仍然返回 <code>Frame.NORMAL</code> 和 <code>Frame.ICONIFIED</code>，但仅报告 Frame 的图标状态，
     * Frame 状态的其他方面不由此方法报告。
     *
     * @return <code>Frame.NORMAL</code> 或 <code>Frame.ICONIFIED</code>。
     * @see #setState(int)
     * @see #getExtendedState
     */
    public synchronized int getState() {
        return (getExtendedState() & ICONIFIED) != 0 ? ICONIFIED : NORMAL;
    }


    /**
     * 获取此 Frame 的状态。状态表示为按位掩码。
     * <ul>
     * <li><code>NORMAL</code>
     * <br>表示没有设置状态位。
     * <li><code>ICONIFIED</code>
     * <li><code>MAXIMIZED_HORIZ</code>
     * <li><code>MAXIMIZED_VERT</code>
     * <li><code>MAXIMIZED_BOTH</code>
     * <br>组合 <code>MAXIMIZED_HORIZ</code> 和 <code>MAXIMIZED_VERT</code>。
     * </ul>
     *
     * @return Frame 状态常量的按位掩码
     * @see #setExtendedState(int)
     * @since 1.4
     */
    public int getExtendedState() {
        synchronized (getObjectLock()) {
            return state;
        }
    }

    static {
        AWTAccessor.setFrameAccessor(
            new AWTAccessor.FrameAccessor() {
                public void setExtendedState(Frame frame, int state) {
                    synchronized(frame.getObjectLock()) {
                        frame.state = state;
                    }
                }
                public int getExtendedState(Frame frame) {
                    synchronized(frame.getObjectLock()) {
                        return frame.state;
                    }
                }
                public Rectangle getMaximizedBounds(Frame frame) {
                    synchronized(frame.getObjectLock()) {
                        return frame.maximizedBounds;
                    }
                }
            }
        );
    }

    /**
     * 设置此 Frame 的最大化边界。
     * <p>
     * 当 Frame 处于最大化状态时，系统会提供一些默认边界。此方法允许覆盖这些系统提供的值。
     * <p>
     * 如果 <code>bounds</code> 为 <code>null</code>，则接受系统提供的边界。如果非 <code>null</code>，可以通过将希望从系统接受的字段设置为
     * <code>Integer.MAX_VALUE</code> 来覆盖系统提供的某些值。
     * <p>
     * 注意，给定的最大化边界是提供给本地系统的提示，因为底层平台可能不支持设置最大化窗口的位置和/或大小。如果情况如此，提供的值不会影响 Frame 在最大化状态下的外观。
     *
     * @param bounds 最大化状态的边界
     * @see #getMaximizedBounds()
     * @since 1.4
     */
    public void setMaximizedBounds(Rectangle bounds) {
        synchronized(getObjectLock()) {
            this.maximizedBounds = bounds;
        }
        FramePeer peer = (FramePeer)this.peer;
        if (peer != null) {
            peer.setMaximizedBounds(bounds);
        }
    }

    /**
     * 获取此 Frame 的最大化边界。
     * 某些字段可能包含 <code>Integer.MAX_VALUE</code>，以指示必须使用系统提供的值。
     *
     * @return 该 Frame 的最大化边界；可能为 <code>null</code>
     * @see #setMaximizedBounds(Rectangle)
     * @since 1.4
     */
    public Rectangle getMaximizedBounds() {
        synchronized(getObjectLock()) {
            return maximizedBounds;
        }
    }


    /**
     * 禁用或启用此框架的装饰。
     * <p>
     * 该方法只能在框架不可显示时调用。要使此框架具有装饰，它必须是不透明的并且具有默认形状，否则将抛出
     * {@code IllegalComponentStateException}。请参阅 {@link Window#setShape}、
     * {@link Window#setOpacity} 和 {@link Window#setBackground} 以获取详细信息。
     *
     * @param  undecorated {@code true} 表示不启用框架装饰；{@code false} 表示启用框架装饰。
     *
     * @throws IllegalComponentStateException 如果框架是可显示的
     * @throws IllegalComponentStateException 如果 {@code undecorated} 为
     *      {@code false}，且此框架不具有默认形状
     * @throws IllegalComponentStateException 如果 {@code undecorated} 为
     *      {@code false}，且此框架的不透明度小于 {@code 1.0f}
     * @throws IllegalComponentStateException 如果 {@code undecorated} 为
     *      {@code false}，且此框架背景颜色的 alpha 值小于 {@code 1.0f}
     *
     * @see    #isUndecorated
     * @see    Component#isDisplayable
     * @see    Window#getShape
     * @see    Window#getOpacity
     * @see    Window#getBackground
     * @see    javax.swing.JFrame#setDefaultLookAndFeelDecorated(boolean)
     *
     * @since 1.4
     */
    public void setUndecorated(boolean undecorated) {
        /* 确保我们不会在对等创建过程中运行。*/
        synchronized (getTreeLock()) {
            if (isDisplayable()) {
                throw new IllegalComponentStateException("The frame is displayable.");
            }
            if (!undecorated) {
                if (getOpacity() < 1.0f) {
                    throw new IllegalComponentStateException("The frame is not opaque");
                }
                if (getShape() != null) {
                    throw new IllegalComponentStateException("The frame does not have a default shape");
                }
                Color bg = getBackground();
                if ((bg != null) && (bg.getAlpha() < 255)) {
                    throw new IllegalComponentStateException("The frame background color is not opaque");
                }
            }
            this.undecorated = undecorated;
        }
    }

    /**
     * 指示此框架是否未装饰。
     * 默认情况下，所有框架最初都是装饰的。
     * @return    <code>true</code> 如果框架未装饰；
     *                        <code>false</code> 否则。
     * @see       java.awt.Frame#setUndecorated(boolean)
     * @since 1.4
     */
    public boolean isUndecorated() {
        return undecorated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOpacity(float opacity) {
        synchronized (getTreeLock()) {
            if ((opacity < 1.0f) && !isUndecorated()) {
                throw new IllegalComponentStateException("The frame is decorated");
            }
            super.setOpacity(opacity);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setShape(Shape shape) {
        synchronized (getTreeLock()) {
            if ((shape != null) && !isUndecorated()) {
                throw new IllegalComponentStateException("The frame is decorated");
            }
            super.setShape(shape);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBackground(Color bgColor) {
        synchronized (getTreeLock()) {
            if ((bgColor != null) && (bgColor.getAlpha() < 255) && !isUndecorated()) {
                throw new IllegalComponentStateException("The frame is decorated");
            }
            super.setBackground(bgColor);
        }
    }

    /**
     * 从此框架中移除指定的菜单栏。
     * @param    m   要移除的菜单组件。
     *           如果 <code>m</code> 为 <code>null</code>，则
     *           不采取任何操作
     */
    public void remove(MenuComponent m) {
        if (m == null) {
            return;
        }
        synchronized (getTreeLock()) {
            if (m == menuBar) {
                menuBar = null;
                FramePeer peer = (FramePeer)this.peer;
                if (peer != null) {
                    mbManagement = true;
                    invalidateIfValid();
                    peer.setMenuBar(null);
                    m.removeNotify();
                }
                m.parent = null;
            } else {
                super.remove(m);
            }
        }
    }

    /**
     * 通过移除与本机屏幕资源的连接使此 Frame 不可显示。
     * 使 Frame 不可显示将导致其所有子组件也不可显示。
     * 该方法由工具包内部调用，程序不应直接调用。
     * @see Component#isDisplayable
     * @see #addNotify
     */
    public void removeNotify() {
        synchronized (getTreeLock()) {
            FramePeer peer = (FramePeer)this.peer;
            if (peer != null) {
                // 在释放之前获取最新的 Frame 状态
                getState();

                if (menuBar != null) {
                    mbManagement = true;
                    peer.setMenuBar(null);
                    menuBar.removeNotify();
                }
            }
            super.removeNotify();
        }
    }

    void postProcessKeyEvent(KeyEvent e) {
        if (menuBar != null && menuBar.handleShortcut(e)) {
            e.consume();
            return;
        }
        super.postProcessKeyEvent(e);
    }

    /**
     * 返回表示此 <code>Frame</code> 状态的字符串。
     * 此方法仅用于调试目的，返回字符串的内容和格式可能因实现而异。
     * 返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return 此框架的参数字符串
     */
    protected String paramString() {
        String str = super.paramString();
        if (title != null) {
            str += ",title=" + title;
        }
        if (resizable) {
            str += ",resizable";
        }
        int state = getExtendedState();
        if (state == NORMAL) {
            str += ",normal";
        }
        else {
            if ((state & ICONIFIED) != 0) {
                str += ",iconified";
            }
            if ((state & MAXIMIZED_BOTH) == MAXIMIZED_BOTH) {
                str += ",maximized";
            }
            else if ((state & MAXIMIZED_HORIZ) != 0) {
                str += ",maximized_horiz";
            }
            else if ((state & MAXIMIZED_VERT) != 0) {
                str += ",maximized_vert";
            }
        }
        return str;
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>Component.setCursor(Cursor)</code>。
     */
    @Deprecated
    public void setCursor(int cursorType) {
        if (cursorType < DEFAULT_CURSOR || cursorType > MOVE_CURSOR) {
            throw new IllegalArgumentException("illegal cursor type");
        }
        setCursor(Cursor.getPredefinedCursor(cursorType));
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>Component.getCursor()</code>。
     */
    @Deprecated
    public int getCursorType() {
        return (getCursor().getType());
    }

    /**
     * 返回由此应用程序创建的所有 {@code Frame} 的数组。
     * 如果从 applet 调用，数组仅包括该 applet 可访问的 {@code Frame}。
     * <p>
     * <b>警告：</b>此方法可能返回系统创建的框架，例如 Swing 使用的共享、隐藏框架。
     * 应用程序不应假设这些框架的存在，也不应假设这些框架的任何属性，如组件位置、
     * <code>LayoutManager</code> 或序列化。
     * <p>
     * <b>注意：</b>要获取所有无所有者的窗口列表，包括 1.6 版本引入的无所有者 {@code Dialog}，
     * 请使用 {@link Window#getOwnerlessWindows Window.getOwnerlessWindows}。
     *
     * @see Window#getWindows()
     * @see Window#getOwnerlessWindows
     *
     * @since 1.2
     */
    public static Frame[] getFrames() {
        Window[] allWindows = Window.getWindows();

        int frameCount = 0;
        for (Window w : allWindows) {
            if (w instanceof Frame) {
                frameCount++;
            }
        }

        Frame[] frames = new Frame[frameCount];
        int c = 0;
        for (Window w : allWindows) {
            if (w instanceof Frame) {
                frames[c++] = (Frame)w;
            }
        }

        return frames;
    }

    /* 序列化支持。如果有 MenuBar，我们在此恢复其（瞬态）父字段。
     * 同样适用于由此框架“拥有”的顶级窗口。
     */

    /**
     * <code>Frame</code> 的序列化数据版本。
     *
     * @serial
     */
    private int frameSerializedDataVersion = 1;

    /**
     * 将默认的可序列化字段写入流。写入一个可选的可序列化图标 <code>Image</code>，
     * 该图标自 1.4 版本起可用。
     *
     * @param s 要写入的 <code>ObjectOutputStream</code>
     * @serialData 一个可选的图标 <code>Image</code>
     * @see java.awt.Image
     * @see #getIconImage
     * @see #setIconImage(Image)
     * @see #readObject(ObjectInputStream)
     */
    private void writeObject(ObjectOutputStream s)
      throws IOException
    {
        s.defaultWriteObject();
        if (icons != null && icons.size() > 0) {
            Image icon1 = icons.get(0);
            if (icon1 instanceof Serializable) {
                s.writeObject(icon1);
                return;
            }
        }
        s.writeObject(null);
    }

    /**
     * 读取 <code>ObjectInputStream</code>。尝试读取一个可选的图标 <code>Image</code>，
     * 该图标自 1.4 版本起可用。如果图标 <code>Image</code> 不可用，但检测到除 EOF 以外的任何内容，
     * 将抛出 <code>OptionalDataException</code>。未识别的键或值将被忽略。
     *
     * @param s 要读取的 <code>ObjectInputStream</code>
     * @exception java.io.OptionalDataException 如果图标 <code>Image</code>
     *   不可用，但检测到除 EOF 以外的任何内容
     * @exception HeadlessException 如果
     *   <code>GraphicsEnvironment.isHeadless</code> 返回
     *   <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless()
     * @see java.awt.Image
     * @see #getIconImage
     * @see #setIconImage(Image)
     * @see #writeObject(ObjectOutputStream)
     */
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException
    {
      // HeadlessException 由 Window 的 readObject 抛出
      s.defaultReadObject();
      try {
          Image icon = (Image) s.readObject();
          if (icons == null) {
              icons = new ArrayList<Image>();
              icons.add(icon);
          }
      } catch (java.io.OptionalDataException e) {
          // 1.4 之前的实例将没有此可选数据。
          // 1.6 及更高版本的实例在 Window 类中序列化图标
          // e.eof 为 true 表示没有更多此对象的数据可用。

          // 如果 e.eof 不为 true，抛出异常，因为它可能是由其他原因引起的。
          if (!e.eof) {
              throw (e);
          }
      }

      if (menuBar != null)
        menuBar.parent = this;

      // 确保 1.1 序列化的 Frame 可以正确读取并连接
      // 拥有的窗口
      //
      if (ownedWindows != null) {
          for (int i = 0; i < ownedWindows.size(); i++) {
              connectOwnedWindow(ownedWindows.elementAt(i));
          }
          ownedWindows = null;
      }
    }

    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();

    /*
     * --- 可访问性支持 ---
     *
     */

    /**
     * 获取与此 Frame 关联的 AccessibleContext。
     * 对于框架，AccessibleContext 的形式为 AccessibleAWTFrame。
     * 如果需要，将创建一个新的 AccessibleAWTFrame 实例。
     *
     * @return 一个 AccessibleAWTFrame，作为此 Frame 的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTFrame();
        }
        return accessibleContext;
    }

    /**
     * 为 <code>Frame</code> 类实现可访问性支持。
     * 它提供了适合框架用户界面元素的 Java 可访问性 API 的实现。
     * @since 1.3
     */
    protected class AccessibleAWTFrame extends AccessibleAWTWindow
    {
        /*
         * JDK 1.3 序列化版本 ID
         */
        private static final long serialVersionUID = -6172960752956030250L;

        /**
         * 获取此对象的角色。
         *
         * @return 一个 AccessibleRole 实例，描述对象的角色
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.FRAME;
        }

        /**
         * 获取此对象的状态。
         *
         * @return 一个 AccessibleStateSet 实例，包含对象的当前状态集
         * @see AccessibleState
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (getFocusOwner() != null) {
                states.add(AccessibleState.ACTIVE);
            }
            if (isResizable()) {
                states.add(AccessibleState.RESIZABLE);
            }
            return states;
        }


    } // 内部类 AccessibleAWTFrame

}
