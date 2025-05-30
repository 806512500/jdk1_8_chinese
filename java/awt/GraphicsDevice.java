
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

import java.awt.image.ColorModel;

import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

/**
 * <code>GraphicsDevice</code> 类描述了在特定图形环境中可能存在的图形设备。这些设备包括屏幕和打印机。注意，在一个 {@link GraphicsEnvironment} 实例中可以有多个屏幕和多个打印机。每个图形设备都关联有一个或多个 {@link GraphicsConfiguration} 对象。这些对象指定了 <code>GraphicsDevice</code> 可以使用的不同配置。
 * <p>
 * 在多屏幕环境中，可以使用 <code>GraphicsConfiguration</code> 对象在多个屏幕上渲染组件。以下代码示例演示了如何为 <code>GraphicsEnvironment</code> 中每个屏幕设备上的每个 <code>GraphicsConfiguration</code> 创建一个 <code>JFrame</code> 对象：
 * <pre>{@code
 *   GraphicsEnvironment ge = GraphicsEnvironment.
 *   getLocalGraphicsEnvironment();
 *   GraphicsDevice[] gs = ge.getScreenDevices();
 *   for (int j = 0; j < gs.length; j++) {
 *      GraphicsDevice gd = gs[j];
 *      GraphicsConfiguration[] gc =
 *      gd.getConfigurations();
 *      for (int i=0; i < gc.length; i++) {
 *         JFrame f = new
 *         JFrame(gs[j].getDefaultConfiguration());
 *         Canvas c = new Canvas(gc[i]);
 *         Rectangle gcBounds = gc[i].getBounds();
 *         int xoffs = gcBounds.x;
 *         int yoffs = gcBounds.y;
 *         f.getContentPane().add(c);
 *         f.setLocation((i*50)+xoffs, (i*60)+yoffs);
 *         f.show();
 *      }
 *   }
 * }</pre>
 * <p>
 * 有关全屏独占模式 API 的更多信息，请参阅
 * <a href="https://docs.oracle.com/javase/tutorial/extra/fullscreen/index.html">
 * 全屏独占模式 API 教程</a>。
 *
 * @see GraphicsEnvironment
 * @see GraphicsConfiguration
 */
public abstract class GraphicsDevice {

    private Window fullScreenWindow;
    private AppContext fullScreenAppContext; // 跟踪创建 FS 窗口的 AppContext
    // 此锁用于对 AppContext 的当前全屏窗口进行同步更改
    private final Object fsAppContextLock = new Object();

    private Rectangle windowedModeBounds;

    /**
     * 这是一个抽象类，不能直接实例化。实例必须从合适的工厂或查询方法中获取。
     * @see GraphicsEnvironment#getScreenDevices
     * @see GraphicsEnvironment#getDefaultScreenDevice
     * @see GraphicsConfiguration#getDevice
     */
    protected GraphicsDevice() {
    }

    /**
     * 设备是光栅屏幕。
     */
    public final static int TYPE_RASTER_SCREEN          = 0;

    /**
     * 设备是打印机。
     */
    public final static int TYPE_PRINTER                = 1;

    /**
     * 设备是图像缓冲区。此缓冲区可以驻留在设备或系统内存中，但用户无法物理查看。
     */
    public final static int TYPE_IMAGE_BUFFER           = 2;

    /**
     * 由底层系统支持的透明度类型。
     *
     * @see #isWindowTranslucencySupported
     *
     * @since 1.7
     */
    public static enum WindowTranslucency {
        /**
         * 表示底层系统支持每个像素要么完全不透明（alpha 值为 1.0），要么完全透明（alpha 值为 0.0）的窗口。
         */
        PERPIXEL_TRANSPARENT,
        /**
         * 表示底层系统支持所有像素具有相同 alpha 值（包括 0.0 和 1.0 之间的值）的窗口。
         */
        TRANSLUCENT,
        /**
         * 表示底层系统支持包含或可能包含 alpha 值在 0.0 和 1.0 之间（包括 0.0 和 1.0）的像素的窗口。
         */
        PERPIXEL_TRANSLUCENT;
    }

    /**
     * 返回此 <code>GraphicsDevice</code> 的类型。
     * @return 此 <code>GraphicsDevice</code> 的类型，可以是 TYPE_RASTER_SCREEN、TYPE_PRINTER 或 TYPE_IMAGE_BUFFER。
     * @see #TYPE_RASTER_SCREEN
     * @see #TYPE_PRINTER
     * @see #TYPE_IMAGE_BUFFER
     */
    public abstract int getType();

    /**
     * 返回与此 <code>GraphicsDevice</code> 关联的标识字符串。
     * <p>
     * 特定程序可能在 <code>GraphicsEnvironment</code> 中使用多个 <code>GraphicsDevice</code>。此方法返回一个 <code>String</code>，用于标识本地 <code>GraphicsEnvironment</code> 中的特定 <code>GraphicsDevice</code>。虽然没有公共方法可以设置此 <code>String</code>，但程序员可以将其用于调试目的。Java&trade; 运行时环境的供应商可以格式化 <code>String</code> 的返回值。要确定如何解释 <code>String</code> 的值，请联系您的 Java 运行时供应商。要查找供应商是谁，可以从您的程序中调用 System 类的 {@link System#getProperty(String) getProperty} 方法，并传入 "java.vendor"。
     * @return 一个 <code>String</code>，表示此 <code>GraphicsDevice</code> 的标识。
     */
    public abstract String getIDstring();

    /**
     * 返回与此 <code>GraphicsDevice</code> 关联的所有 <code>GraphicsConfiguration</code> 对象。
     * @return 与此 <code>GraphicsDevice</code> 关联的 <code>GraphicsConfiguration</code> 对象数组。
     */
    public abstract GraphicsConfiguration[] getConfigurations();

    /**
     * 返回与此 <code>GraphicsDevice</code> 关联的默认 <code>GraphicsConfiguration</code>。
     * @return 此 <code>GraphicsDevice</code> 的默认 <code>GraphicsConfiguration</code>。
     */
    public abstract GraphicsConfiguration getDefaultConfiguration();

    /**
     * 返回满足 {@link GraphicsConfigTemplate} 中定义的条件的“最佳”配置。
     * @param gct 用于获取有效 <code>GraphicsConfiguration</code> 的 <code>GraphicsConfigTemplate</code> 对象
     * @return 满足指定 <code>GraphicsConfigTemplate</code> 中定义的条件的 <code>GraphicsConfiguration</code>。
     * @see GraphicsConfigTemplate
     */
    public GraphicsConfiguration
           getBestConfiguration(GraphicsConfigTemplate gct) {
        GraphicsConfiguration[] configs = getConfigurations();
        return gct.getBestConfiguration(configs);
    }

    /**
     * 如果此 <code>GraphicsDevice</code> 支持全屏独占模式，则返回 <code>true</code>。
     * 如果安装了 SecurityManager，其 <code>checkPermission</code> 方法将被调用，参数为 <code>AWTPermission("fullScreenExclusive")</code>。
     * <code>isFullScreenSupported</code> 仅在授予该权限时返回 true。
     * @return 此图形设备是否支持全屏独占模式
     * @see java.awt.AWTPermission
     * @since 1.4
     */
    public boolean isFullScreenSupported() {
        return false;
    }

    /**
     * 进入全屏模式，或返回窗口模式。进入的全屏模式可以是独占模式或模拟模式。独占模式仅在 <code>isFullScreenSupported</code> 返回 <code>true</code> 时可用。
     * <p>
     * 独占模式意味着：
     * <ul>
     * <li>窗口不能重叠全屏窗口。所有其他应用程序窗口在 Z 顺序中始终位于全屏窗口下方。
     * <li>在任何时间，一个设备上只能有一个全屏窗口，因此在现有全屏窗口的情况下调用此方法将导致现有全屏窗口返回窗口模式。
     * <li>输入方法窗口被禁用。建议调用 <code>Component.enableInputMethods(false)</code> 使组件成为输入方法框架的非客户端。
     * </ul>
     * <p>
     * 模拟全屏模式将窗口放置并调整大小到屏幕的最大可见区域。但是，本机窗口系统可能会修改请求的几何相关数据，使得 {@code Window} 对象被放置和调整大小以与桌面设置紧密对应。
     * <p>
     * 进入全屏模式时，如果要使用的全屏窗口不可见，此方法将使其可见。返回窗口模式时，它将保持可见。
     * <p>
     * 进入全屏模式时，窗口的所有透明效果都会重置。其形状被设置为 {@code null}，不透明度值被设置为 1.0f，背景颜色的 alpha 值被设置为 255（完全不透明）。返回窗口模式时，这些值不会恢复。
     * <p>
     * 装饰窗口在全屏模式下的操作方式是未指定且依赖于平台的。因此，建议使用 {@code setUndecorated} 方法关闭 {@code Frame} 或 {@code Dialog} 对象的装饰。
     * <p>
     * 从独占全屏窗口返回窗口模式时，通过调用 {@code setDisplayMode} 进行的任何显示更改将自动恢复到原始状态。
     *
     * @param w 用作全屏窗口的窗口；如果返回窗口模式，则为 {@code null}。某些平台期望全屏窗口是一个顶级组件（即，一个 {@code Frame}）；因此，这里最好使用 {@code Frame} 而不是 {@code Window}。
     *
     * @see #isFullScreenSupported
     * @see #getFullScreenWindow
     * @see #setDisplayMode
     * @see Component#enableInputMethods
     * @see Component#setVisible
     * @see Frame#setUndecorated
     * @see Dialog#setUndecorated
     *
     * @since 1.4
     */
    public void setFullScreenWindow(Window w) {
        if (w != null) {
            if (w.getShape() != null) {
                w.setShape(null);
            }
            if (w.getOpacity() < 1.0f) {
                w.setOpacity(1.0f);
            }
            if (!w.isOpaque()) {
                Color bgColor = w.getBackground();
                bgColor = new Color(bgColor.getRed(), bgColor.getGreen(),
                                    bgColor.getBlue(), 255);
                w.setBackground(bgColor);
            }
            // 检查此窗口是否在另一个设备上处于全屏模式。
            final GraphicsConfiguration gc = w.getGraphicsConfiguration();
            if (gc != null && gc.getDevice() != this
                    && gc.getDevice().getFullScreenWindow() == w) {
                gc.getDevice().setFullScreenWindow(null);
            }
        }
        if (fullScreenWindow != null && windowedModeBounds != null) {
            // 如果窗口在实现之前进入全屏模式，它可能具有 (0,0) 尺寸
            if (windowedModeBounds.width  == 0) windowedModeBounds.width  = 1;
            if (windowedModeBounds.height == 0) windowedModeBounds.height = 1;
            fullScreenWindow.setBounds(windowedModeBounds);
        }
        // 设置全屏窗口
        synchronized (fsAppContextLock) {
            // 将全屏窗口与当前 AppContext 关联
            if (w == null) {
                fullScreenAppContext = null;
            } else {
                fullScreenAppContext = AppContext.getAppContext();
            }
            fullScreenWindow = w;
        }
        if (fullScreenWindow != null) {
            windowedModeBounds = fullScreenWindow.getBounds();
            // 注意，我们使用设备的图形配置，而不是窗口的，因为我们在为这个设备设置全屏窗口。
            final GraphicsConfiguration gc = getDefaultConfiguration();
            final Rectangle screenBounds = gc.getBounds();
            if (SunToolkit.isDispatchThreadForAppContext(fullScreenWindow)) {
                // 直接在此处更新图形配置，不要等待来自对等体的异步通知。注意，如果设置不正确，setBounds() 将重置 GC。
                fullScreenWindow.setGraphicsConfiguration(gc);
            }
            fullScreenWindow.setBounds(screenBounds.x, screenBounds.y,
                                       screenBounds.width, screenBounds.height);
            fullScreenWindow.setVisible(true);
            fullScreenWindow.toFront();
        }
    }

    /**
     * 如果设备处于全屏模式，返回表示全屏窗口的 <code>Window</code> 对象。
     *
     * @return 全屏窗口，如果设备不处于全屏模式，则返回 <code>null</code>。
     * @see #setFullScreenWindow(Window)
     * @since 1.4
     */
    public Window getFullScreenWindow() {
        Window returnWindow = null;
        synchronized (fsAppContextLock) {
            // 仅在设置全屏窗口的相同 AppContext 中返回当前全屏窗口的句柄
            if (fullScreenAppContext == AppContext.getAppContext()) {
                returnWindow = fullScreenWindow;
            }
        }
        return returnWindow;
    }

    /**
     * 如果此 <code>GraphicsDevice</code> 支持低级显示更改，则返回 <code>true</code>。
     * 在某些平台上，低级显示更改可能仅在全屏独占模式下允许（即，如果 {@link #isFullScreenSupported()} 返回 {@code true} 并且应用程序已经使用 {@link #setFullScreenWindow} 进入全屏模式）。
     * @return 此图形设备是否支持低级显示更改。
     * @see #isFullScreenSupported
     * @see #setDisplayMode
     * @see #setFullScreenWindow
     * @since 1.4
     */
    public boolean isDisplayChangeSupported() {
        return false;
    }


                /**
     * 设置此图形设备的显示模式。仅当 {@link #isDisplayChangeSupported()} 返回 {@code true} 时才允许此操作，
     * 并且可能需要首先使用 {@link #setFullScreenWindow} 进入全屏独占模式，前提是全屏独占模式得到支持
     * （即 {@link #isFullScreenSupported()} 返回 {@code true}）。
     * <p>
     *
     * 显示模式必须是 {@link #getDisplayModes()} 返回的显示模式之一，有一个例外：传递一个具有
     * {@link DisplayMode#REFRESH_RATE_UNKNOWN} 刷新率的显示模式将从可用显示模式列表中选择一个具有匹配宽度、
     * 高度和位深度的显示模式。但是，传递一个具有 {@link DisplayMode#BIT_DEPTH_MULTI} 位深度的显示模式
     * 仅当该模式存在于 {@link #getDisplayModes()} 返回的列表中时才允许。
     * <p>
     * 示例代码：
     * <pre><code>
     * Frame frame;
     * DisplayMode newDisplayMode;
     * GraphicsDevice gd;
     * // 创建一个 Frame，从 gd.getDisplayModes() 返回的模式列表中选择所需的 DisplayMode ...
     *
     * if (gd.isFullScreenSupported()) {
     *     gd.setFullScreenWindow(frame);
     * } else {
     *    // 以非全屏模式继续
     *    frame.setSize(...);
     *    frame.setLocation(...);
     *    frame.setVisible(true);
     * }
     *
     * if (gd.isDisplayChangeSupported()) {
     *     gd.setDisplayMode(newDisplayMode);
     * }
     * </code></pre>
     *
     * @param dm 此图形设备的新显示模式。
     * @exception IllegalArgumentException 如果提供的 <code>DisplayMode</code> 为 <code>null</code>，
     * 或者不在 <code>getDisplayModes</code> 返回的数组中。
     * @exception UnsupportedOperationException 如果 <code>isDisplayChangeSupported</code> 返回 <code>false</code>
     * @see #getDisplayMode
     * @see #getDisplayModes
     * @see #isDisplayChangeSupported
     * @since 1.4
     */
    public void setDisplayMode(DisplayMode dm) {
        throw new UnsupportedOperationException("Cannot change display mode");
    }

    /**
     * 返回此 <code>GraphicsDevice</code> 的当前显示模式。
     * 返回的显示模式允许具有不确定的刷新率 {@link DisplayMode#REFRESH_RATE_UNKNOWN}。
     * 同样，返回的显示模式允许具有不确定的位深度 {@link DisplayMode#BIT_DEPTH_MULTI} 或支持多种位深度。
     * @return 此图形设备的当前显示模式。
     * @see #setDisplayMode(DisplayMode)
     * @since 1.4
     */
    public DisplayMode getDisplayMode() {
        GraphicsConfiguration gc = getDefaultConfiguration();
        Rectangle r = gc.getBounds();
        ColorModel cm = gc.getColorModel();
        return new DisplayMode(r.width, r.height, cm.getPixelSize(), 0);
    }

    /**
     * 返回此 <code>GraphicsDevice</code> 可用的所有显示模式。
     * 返回的显示模式允许具有不确定的刷新率 {@link DisplayMode#REFRESH_RATE_UNKNOWN}。
     * 同样，返回的显示模式允许具有不确定的位深度 {@link DisplayMode#BIT_DEPTH_MULTI} 或支持多种位深度。
     * @return 此图形设备可用的所有显示模式。
     * @since 1.4
     */
    public DisplayMode[] getDisplayModes() {
        return new DisplayMode[] { getDisplayMode() };
    }

    /**
     * 返回此设备加速内存中可用的字节数。
     * 一些图像在加速内存中创建或缓存，遵循先到先得的原则。在某些操作系统上，
     * 这种内存是一种有限的资源。调用此方法并仔细安排图像的创建和刷新，可以使应用程序
     * 更高效地利用这种有限的资源。
     * <br>
     * 注意，返回的数字是一个快照，表示当前可用的内存数量；某些图像可能仍然无法分配到该内存中。
     * 例如，根据操作系统、驱动程序、内存配置和线程情况，报告的大小可能无法完全用于某个图像。
     * 可以使用与 VolatileImage 关联的 {@link ImageCapabilities} 对象上的进一步查询方法来确定
     * 某个特定的 VolatileImage 是否已创建在加速内存中。
     * @return 加速内存中可用的字节数。
     * 负返回值表示此 GraphicsDevice 上的加速内存量不确定。
     * @see java.awt.image.VolatileImage#flush
     * @see ImageCapabilities#isAccelerated
     * @since 1.4
     */
    public int getAvailableAcceleratedMemory() {
        return -1;
    }

    /**
     * 返回此图形设备是否支持给定的透明度级别。
     *
     * @param translucencyKind 透明度支持类型
     * @return 是否支持给定的透明度类型
     *
     * @since 1.7
     */
    public boolean isWindowTranslucencySupported(WindowTranslucency translucencyKind) {
        switch (translucencyKind) {
            case PERPIXEL_TRANSPARENT:
                return isWindowShapingSupported();
            case TRANSLUCENT:
                return isWindowOpacitySupported();
            case PERPIXEL_TRANSLUCENT:
                return isWindowPerpixelTranslucencySupported();
        }
        return false;
    }

    /**
     * 返回窗口系统是否支持更改顶级窗口的形状。
     * 注意，此方法有时可能返回 true，但原生窗口系统可能仍然不支持形状概念
     * （由于窗口系统中的错误）。
     */
    static boolean isWindowShapingSupported() {
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        return ((SunToolkit)curToolkit).isWindowShapingSupported();
    }

    /**
     * 返回窗口系统是否支持更改顶级窗口的不透明度值。
     * 注意，此方法有时可能返回 true，但原生窗口系统可能仍然不支持透明度概念
     * （由于窗口系统中的错误）。
     */
    static boolean isWindowOpacitySupported() {
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        return ((SunToolkit)curToolkit).isWindowOpacitySupported();
    }

    boolean isWindowPerpixelTranslucencySupported() {
        /*
         * 每像素 alpha 透明度支持的条件是：
         *    1. 工具包是一种 SunToolkit
         *    2. 工具包通常支持透明度（isWindowTranslucencySupported()）
         *    3. 至少有一个支持透明度的 GraphicsConfiguration
         */
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        if (!((SunToolkit)curToolkit).isWindowTranslucencySupported()) {
            return false;
        }

        // TODO: 缓存支持透明度的 GC
        return getTranslucencyCapableGC() != null;
    }

    GraphicsConfiguration getTranslucencyCapableGC() {
        // 如果默认的 GC 支持透明度则返回 true。
        // 以这种方式优化验证非常重要，
        // 请参阅 CR 6661196 了解详细信息。
        GraphicsConfiguration defaultGC = getDefaultConfiguration();
        if (defaultGC.isTranslucencyCapable()) {
            return defaultGC;
        }

        // ... 否则遍历所有 GC。
        GraphicsConfiguration[] configs = getConfigurations();
        for (int j = 0; j < configs.length; j++) {
            if (configs[j].isTranslucencyCapable()) {
                return configs[j];
            }
        }

        return null;
    }
}
