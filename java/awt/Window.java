
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

import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.im.InputContext;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.peer.ComponentPeer;
import java.awt.peer.WindowPeer;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.accessibility.*;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.CausedFocusEvent;
import sun.awt.SunToolkit;
import sun.awt.util.IdentityArrayList;
import sun.java2d.Disposer;
import sun.java2d.pipe.Region;
import sun.security.action.GetPropertyAction;
import sun.security.util.SecurityConstants;
import sun.util.logging.PlatformLogger;

/**
 * {@code Window} 对象是一个没有边框和菜单栏的顶级窗口。
 * 默认布局为 {@code BorderLayout}。
 * <p>
 * 创建窗口时，必须定义一个框架、对话框或另一个窗口作为其所有者。
 * <p>
 * 在多屏幕环境中，可以通过使用 {@link #Window(Window, GraphicsConfiguration)} 构造函数在不同的屏幕设备上创建 {@code Window}。
 * {@code GraphicsConfiguration} 对象是目标屏幕设备的 {@code GraphicsConfiguration} 对象之一。
 * <p>
 * 在虚拟设备多屏幕环境中，桌面区域可能跨越多个物理屏幕设备，所有配置的边界都相对于虚拟设备坐标系。
 * 虚拟坐标系的原点位于主物理屏幕的左上角。根据主屏幕在虚拟设备中的位置，可能会出现负坐标，如以下图所示。
 * <p>
 * <img src="doc-files/MultiScreen.gif"
 * alt="图示显示虚拟设备包含4个物理屏幕。主物理屏幕显示坐标 (0,0)，其他屏幕显示 (-80,-100)。"
 * style="float:center; margin: 7px 10px;">
 * <p>
 * 在这种环境中，调用 {@code setLocation} 时，必须传递虚拟坐标。同样，调用 {@code getLocationOnScreen} 时，返回的是虚拟设备坐标。
 * 调用 {@code GraphicsConfiguration} 的 {@code getBounds} 方法可以找到其在虚拟坐标系中的原点。
 * <p>
 * 以下代码将 {@code Window} 的位置设置为相对于对应 {@code GraphicsConfiguration} 的物理屏幕原点 (10, 10)。
 * 如果不考虑 {@code GraphicsConfiguration} 的边界，{@code Window} 的位置将被设置为相对于虚拟坐标系的 (10, 10)，并出现在主物理屏幕上，这可能与指定的 {@code GraphicsConfiguration} 的物理屏幕不同。
 *
 * <pre>
 *      Window w = new Window(Window owner, GraphicsConfiguration gc);
 *      Rectangle bounds = gc.getBounds();
 *      w.setLocation(10 + bounds.x, 10 + bounds.y);
 * </pre>
 *
 * <p>
 * 注意：顶级窗口（包括 {@code Window}、{@code Frame} 和 {@code Dialog}）的位置和大小由桌面的窗口管理系统控制。
 * 调用 {@code setLocation}、{@code setSize} 和 {@code setBounds} 是请求（不是指令），这些请求将转发给窗口管理系统。
 * 尽力满足这些请求。但在某些情况下，窗口管理系统可能会忽略这些请求，或修改请求的几何形状，以更符合桌面设置的方式放置和调整 {@code Window} 的大小。
 * <p>
 * 由于原生事件处理的异步性质，调用 {@code getBounds}、{@code getLocation}、{@code getLocationOnScreen} 和 {@code getSize} 返回的结果可能不会立即反映窗口在屏幕上的实际几何形状，直到最后一个请求被处理。
 * 在处理后续请求时，这些值可能会相应地变化，因为窗口管理系统正在满足这些请求。
 * <p>
 * 应用程序可以任意设置不可见 {@code Window} 的大小和位置，但窗口管理系统可能在 {@code Window} 可见时更改其大小和/或位置。会生成一个或多个 {@code ComponentEvent} 以指示新的几何形状。
 * <p>
 * 窗口可以生成以下窗口事件：WindowOpened、WindowClosed、WindowGainedFocus、WindowLostFocus。
 *
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 * @see WindowEvent
 * @see #addWindowListener
 * @see java.awt.BorderLayout
 * @since       JDK1.0
 */
public class Window extends Container implements Accessible {

    /**
     * 可用 <i>窗口类型</i> 的枚举。
     *
     * 窗口类型定义了顶级窗口的通用外观和行为。例如，类型可能会影响装饰的 {@code Frame} 或 {@code Dialog} 实例的装饰类型。
     * <p>
     * 某些平台可能不完全支持某种窗口类型。根据支持程度，窗口类型的一些属性可能不被遵守。
     *
     * @see   #getType
     * @see   #setType
     * @since 1.7
     */
    public static enum Type {
        /**
         * 表示一个 <i>普通</i> 窗口。
         *
         * 这是 {@code Window} 类或其子类对象的默认类型。用于常规顶级窗口。
         */
        NORMAL,

        /**
         * 表示一个 <i>工具</i> 窗口。
         *
         * 工具窗口通常是一个小窗口，如工具栏或调色板。如果窗口是 {@code Frame} 或 {@code Dialog} 对象，并且启用了装饰，原生系统可能会以较小的标题栏渲染该窗口。
         */
        UTILITY,

        /**
         * 表示一个 <i>弹出</i> 窗口。
         *
         * 弹出窗口是一个临时窗口，如下拉菜单或工具提示。在某些平台上，即使窗口是 {@code Frame} 或 {@code Dialog} 类的实例，并且启用了装饰，这些窗口类型也可能被强制设置为无装饰。
         */
        POPUP
    }

    /**
     * 表示在非安全窗口中显示的警告消息。即：
     * 安装了安全管理器且拒绝 {@code AWTPermission("showWindowWithoutWarningBanner")} 的窗口。
     * 该消息可以在窗口的任何位置显示。
     *
     * @serial
     * @see #getWarningString
     */
    String      warningString;

    /**
     * {@code icons} 是表示框架和对话框的图形方式。
     * {@code Window} 不能显示图标，但可以被拥有的 {@code Dialog} 继承。
     *
     * @serial
     * @see #getIconImages
     * @see #setIconImages
     */
    transient java.util.List<Image> icons;

    /**
     * 持有最后一次在该窗口中获得焦点的组件的引用。
     */
    private transient Component temporaryLostComponent;

    static boolean systemSyncLWRequests = false;
    boolean     syncLWRequests = false;
    transient boolean beforeFirstShow = true;
    private transient boolean disposing = false;
    transient WindowDisposerRecord disposerRecord = null;

    static final int OPENED = 0x01;

    /**
     * 表示窗口状态的整数值。
     *
     * @serial
     * @since 1.2
     * @see #show
     */
    int state;

    /**
     * 表示窗口始终置顶状态的布尔值
     * @since 1.5
     * @serial
     * @see #setAlwaysOnTop
     * @see #isAlwaysOnTop
     */
    private boolean alwaysOnTop;

    /**
     * 包含所有具有关联对等对象的窗口，即在 addNotify() 和 removeNotify() 调用之间。可以从 AppContext 对象中获取所有 Window 实例的列表。
     *
     * @since 1.6
     */
    private static final IdentityArrayList<Window> allWindows = new IdentityArrayList<Window>();

    /**
     * 包含此窗口当前拥有的所有窗口的向量。
     * @since 1.2
     * @see #getOwnedWindows
     */
    transient Vector<WeakReference<Window>> ownedWindowList =
                                            new Vector<WeakReference<Window>>();

    /*
     * 我们在 AppContext 中所有窗口的向量中插入一个弱引用，而不是 'this'，以便垃圾收集仍然可以正确进行。
     */
    private transient WeakReference<Window> weakThis;

    transient boolean showWithParent;

    /**
     * 包含阻止此窗口的模态对话框，或如果窗口未被阻止，则为 null。
     *
     * @since 1.6
     */
    transient Dialog modalBlocker;

    /**
     * @serial
     *
     * @see java.awt.Dialog.ModalExclusionType
     * @see #getModalExclusionType
     * @see #setModalExclusionType
     *
     * @since 1.6
     */
    Dialog.ModalExclusionType modalExclusionType;

    transient WindowListener windowListener;
    transient WindowStateListener windowStateListener;
    transient WindowFocusListener windowFocusListener;

    transient InputContext inputContext;
    private transient Object inputContextLock = new Object();

    /**
     * 未使用。为了序列化向后兼容性而保留。
     *
     * @serial
     * @since 1.2
     */
    private FocusManager focusMgr;

    /**
     * 表示此窗口是否可以成为焦点窗口。
     *
     * @serial
     * @see #getFocusableWindowState
     * @see #setFocusableWindowState
     * @since 1.4
     */
    private boolean focusableWindowState = true;

    /**
     * 表示此窗口在随后显示（调用 {@code setVisible(true)}）或移到前面（调用 {@code toFront()}）时是否应获得焦点。
     *
     * @serial
     * @see #setAutoRequestFocus
     * @see #isAutoRequestFocus
     * @since 1.7
     */
    private volatile boolean autoRequestFocus = true;

    /*
     * 表示此窗口正在显示。此标志在 show() 开始时设置为 true，在 show() 结束时设置为 false。
     *
     * @see #show()
     * @see Dialog#shouldBlock
     */
    transient boolean isInShow = false;

    /**
     * 窗口的不透明度级别
     *
     * @serial
     * @see #setOpacity(float)
     * @see #getOpacity()
     * @since 1.7
     */
    private volatile float opacity = 1.0f;

    /**
     * 分配给此窗口的形状。如果未设置形状（矩形窗口），则此字段为 {@code null}。
     *
     * @serial
     * @see #getShape()
     * @see #setShape(Shape)
     * @since 1.7
     */
    private Shape shape = null;

    private static final String base = "win";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 4497834738069338734L;

    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.Window");

    private static final boolean locationByPlatformProp;

    transient boolean isTrayIconWindow = false;

    /**
     * 这些字段在原生对等代码中或通过 AWTAccessor 的 WindowAccessor 初始化。
     */
    private transient volatile int securityWarningWidth = 0;
    private transient volatile int securityWarningHeight = 0;

    /**
     * 这些字段表示如果此窗口不受信任，安全警告的期望位置。
     * 有关更多详细信息，请参阅 com.sun.awt.SecurityWarning。
     */
    private transient double securityWarningPointX = 2.0;
    private transient double securityWarningPointY = 0.0;
    private transient float securityWarningAlignmentX = RIGHT_ALIGNMENT;
    private transient float securityWarningAlignmentY = TOP_ALIGNMENT;

    static {
        /* 确保加载必要的原生库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }

        String s = java.security.AccessController.doPrivileged(
            new GetPropertyAction("java.awt.syncLWRequests"));
        systemSyncLWRequests = (s != null && s.equals("true"));
        s = java.security.AccessController.doPrivileged(
            new GetPropertyAction("java.awt.Window.locationByPlatform"));
        locationByPlatformProp = (s != null && s.equals("true"));
    }

    /**
     * 初始化 JNI 字段和方法 ID，以便从 C 语言访问。
     */
    private static native void initIDs();

    /**
     * 使用指定的 {@code GraphicsConfiguration} 构造一个新的、初始不可见的默认大小窗口。
     * <p>
     * 如果存在安全管理器，则调用它检查 {@code AWTPermission("showWindowWithoutWarningBanner")}
     * 以确定是否必须显示带有警告横幅的窗口。
     *
     * @param gc 目标屏幕设备的 {@code GraphicsConfiguration}。如果 {@code gc} 为 {@code null}，则假定为系统默认的 {@code GraphicsConfiguration}
     * @exception IllegalArgumentException 如果 {@code gc} 不是来自屏幕设备
     * @exception HeadlessException 当 {@code GraphicsEnvironment.isHeadless()} 返回 {@code true} 时
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    Window(GraphicsConfiguration gc) {
        init(gc);
    }


                transient Object anchor = new Object();
    static class WindowDisposerRecord implements sun.java2d.DisposerRecord {
        WeakReference<Window> owner;
        final WeakReference<Window> weakThis;
        final WeakReference<AppContext> context;

        WindowDisposerRecord(AppContext context, Window victim) {
            weakThis = victim.weakThis;
            this.context = new WeakReference<AppContext>(context);
        }

        public void updateOwner() {
            Window victim = weakThis.get();
            owner = (victim == null)
                    ? null
                    : new WeakReference<Window>(victim.getOwner());
        }

        public void dispose() {
            if (owner != null) {
                Window parent = owner.get();
                if (parent != null) {
                    parent.removeOwnedWindow(weakThis);
                }
            }
            AppContext ac = context.get();
            if (null != ac) {
                Window.removeFromWindowList(ac, weakThis);
            }
        }
    }

    private GraphicsConfiguration initGC(GraphicsConfiguration gc) {
        GraphicsEnvironment.checkHeadless();

        if (gc == null) {
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();
        }
        setGraphicsConfiguration(gc);

        return gc;
    }

    private void init(GraphicsConfiguration gc) {
        GraphicsEnvironment.checkHeadless();

        syncLWRequests = systemSyncLWRequests;

        weakThis = new WeakReference<Window>(this);
        addToWindowList();

        setWarningString();
        this.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        this.visible = false;

        gc = initGC(gc);

        if (gc.getDevice().getType() !=
            GraphicsDevice.TYPE_RASTER_SCREEN) {
            throw new IllegalArgumentException("不是屏幕设备");
        }
        setLayout(new BorderLayout());

        /* 用屏幕的原点和任何内边距来偏移初始位置 */
        /* 和任何内边距                                              */
        Rectangle screenBounds = gc.getBounds();
        Insets screenInsets = getToolkit().getScreenInsets(gc);
        int x = getX() + screenBounds.x + screenInsets.left;
        int y = getY() + screenBounds.y + screenInsets.top;
        if (x != this.x || y != this.y) {
            setLocation(x, y);
            /* 在设置位置后重置 */
            setLocationByPlatform(locationByPlatformProp);
        }

        modalExclusionType = Dialog.ModalExclusionType.NO_EXCLUDE;
        disposerRecord = new WindowDisposerRecord(appContext, this);
        sun.java2d.Disposer.addRecord(anchor, disposerRecord);

        SunToolkit.checkAndSetPolicy(this);
    }

    /**
     * 构造一个新的、最初不可见的窗口，默认大小。
     * <p>
     * 如果设置了安全经理，它会被调用以检查
     * {@code AWTPermission("showWindowWithoutWarningBanner")}。
     * 如果该检查以 {@code SecurityException} 失败，则创建警告横幅。
     *
     * @exception HeadlessException 当
     *     {@code GraphicsEnvironment.isHeadless()} 返回 {@code true}
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    Window() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        init((GraphicsConfiguration)null);
    }

    /**
     * 构造一个新的、最初不可见的窗口，指定的
     * {@code Frame} 作为其所有者。除非其所有者在屏幕上显示，
     * 否则该窗口将不可聚焦。
     * <p>
     * 如果设置了安全经理，它会被调用以检查
     * {@code AWTPermission("showWindowWithoutWarningBanner")}。
     * 如果该检查以 {@code SecurityException} 失败，则创建警告横幅。
     *
     * @param owner 作为所有者的 {@code Frame} 或 {@code null}
     *    如果此窗口没有所有者
     * @exception IllegalArgumentException 如果 {@code owner} 的
     *    {@code GraphicsConfiguration} 不是来自屏幕设备
     * @exception HeadlessException 当
     *    {@code GraphicsEnvironment.isHeadless} 返回 {@code true}
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see #isShowing
     */
    public Window(Frame owner) {
        this(owner == null ? (GraphicsConfiguration)null :
            owner.getGraphicsConfiguration());
        ownedInit(owner);
    }

    /**
     * 构造一个新的、最初不可见的窗口，指定的
     * {@code Window} 作为其所有者。除非其最近的拥有
     * {@code Frame} 或 {@code Dialog} 在屏幕上显示，
     * 否则该窗口将不可聚焦。
     * <p>
     * 如果设置了安全经理，它会被调用以检查
     * {@code AWTPermission("showWindowWithoutWarningBanner")}。
     * 如果该检查以 {@code SecurityException} 失败，则创建
     * 警告横幅。
     *
     * @param owner 作为所有者的 {@code Window} 或
     *     {@code null} 如果此窗口没有所有者
     * @exception IllegalArgumentException 如果 {@code owner} 的
     *     {@code GraphicsConfiguration} 不是来自屏幕设备
     * @exception HeadlessException 当
     *     {@code GraphicsEnvironment.isHeadless()} 返回
     *     {@code true}
     *
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       #isShowing
     *
     * @since     1.2
     */
    public Window(Window owner) {
        this(owner == null ? (GraphicsConfiguration)null :
            owner.getGraphicsConfiguration());
        ownedInit(owner);
    }

    /**
     * 构造一个新的、最初不可见的窗口，指定的所有者
     * {@code Window} 和屏幕设备的 {@code GraphicsConfiguration}。
     * 除非其最近的拥有 {@code Frame} 或 {@code Dialog}
     * 在屏幕上显示，否则该窗口将不可聚焦。
     * <p>
     * 如果设置了安全经理，它会被调用以检查
     * {@code AWTPermission("showWindowWithoutWarningBanner")}。如果该
     * 检查以 {@code SecurityException} 失败，则创建警告横幅。
     *
     * @param owner 作为所有者的窗口或 {@code null}
     *     如果此窗口没有所有者
     * @param gc 屏幕设备的 {@code GraphicsConfiguration}；如果 {@code gc} 是 {@code null}，
     *     则假定系统默认的 {@code GraphicsConfiguration}
     * @exception IllegalArgumentException 如果 {@code gc}
     *     不是来自屏幕设备
     * @exception HeadlessException 当
     *     {@code GraphicsEnvironment.isHeadless()} 返回
     *     {@code true}
     *
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       GraphicsConfiguration#getBounds
     * @see       #isShowing
     * @since     1.3
     */
    public Window(Window owner, GraphicsConfiguration gc) {
        this(gc);
        ownedInit(owner);
    }

    private void ownedInit(Window owner) {
        this.parent = owner;
        if (owner != null) {
            owner.addOwnedWindow(weakThis);
            if (owner.isAlwaysOnTop()) {
                try {
                    setAlwaysOnTop(true);
                } catch (SecurityException ignore) {
                }
            }
        }

        // WindowDisposerRecord 需要 parent 字段的正确值。
        disposerRecord.updateOwner();
    }

    /**
     * 为这个组件构建一个名称。当名称为 null 时由 getName() 调用。
     */
    String constructComponentName() {
        synchronized (Window.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 返回作为此窗口图标显示的图像序列。
     * <p>
     * 此方法返回内部存储列表的副本，因此对返回对象的所有操作
     * 都不会影响窗口的行为。
     *
     * @return    此窗口的图标图像列表的副本，或
     *            如果此窗口没有图标图像，则返回空列表。
     * @see       #setIconImages
     * @see       #setIconImage(Image)
     * @since     1.6
     */
    public java.util.List<Image> getIconImages() {
        java.util.List<Image> icons = this.icons;
        if (icons == null || icons.size() == 0) {
            return new ArrayList<Image>();
        }
        return new ArrayList<Image>(icons);
    }

    /**
     * 设置作为此窗口图标显示的图像序列。后续调用 {@code getIconImages} 将
     * 始终返回 {@code icons} 列表的副本。
     * <p>
     * 根据平台功能，一个或多个不同尺寸的图像将用作窗口的图标。
     * <p>
     * 从列表的开头扫描最合适的尺寸图像。如果列表包含
     * 多个相同大小的图像，则使用第一个。
     * <p>
     * 没有指定图标的无所有者窗口使用平台默认图标。
     * 拥有窗口的图标可以从所有者继承，除非显式覆盖。
     * 将图标设置为 {@code null} 或空列表将恢复默认行为。
     * <p>
     * 注意：本机窗口系统可能使用不同尺寸的图像来表示窗口，
     * 具体取决于上下文（例如窗口装饰、窗口列表、任务栏等）。
     * 他们也可以为所有上下文使用单个图像或根本不使用图像。
     *
     * @param     icons 要显示的图标图像列表。
     * @see       #getIconImages()
     * @see       #setIconImage(Image)
     * @since     1.6
     */
    public synchronized void setIconImages(java.util.List<? extends Image> icons) {
        this.icons = (icons == null) ? new ArrayList<Image>() :
            new ArrayList<Image>(icons);
        WindowPeer peer = (WindowPeer)this.peer;
        if (peer != null) {
            peer.updateIconImages();
        }
        // 始终发送属性更改事件
        firePropertyChange("iconImage", null, null);
    }

    /**
     * 设置作为此窗口图标显示的图像。
     * <p>
     * 此方法可以用作 {@link #setIconImages setIconImages()}
     * 的替代方法，以指定单个图像作为窗口的图标。
     * <p>
     * 以下语句：
     * <pre>
     *     setIconImage(image);
     * </pre>
     * 等效于：
     * <pre>
     *     ArrayList&lt;Image&gt; imageList = new ArrayList&lt;Image&gt;();
     *     imageList.add(image);
     *     setIconImages(imageList);
     * </pre>
     * <p>
     * 注意：本机窗口系统可能使用不同尺寸的图像来表示窗口，
     * 具体取决于上下文（例如窗口装饰、窗口列表、任务栏等）。
     * 他们也可以为所有上下文使用单个图像或根本不使用图像。
     *
     * @param     image 要显示的图标图像。
     * @see       #setIconImages
     * @see       #getIconImages()
     * @since     1.6
     */
    public void setIconImage(Image image) {
        ArrayList<Image> imageList = new ArrayList<Image>();
        if (image != null) {
            imageList.add(image);
        }
        setIconImages(imageList);
    }

    /**
     * 通过创建与本机屏幕资源的连接使此窗口可显示。
     * 此方法由工具包内部调用，不应由程序直接调用。
     * @see Component#isDisplayable
     * @see Container#removeNotify
     * @since JDK1.0
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            Container parent = this.parent;
            if (parent != null && parent.getPeer() == null) {
                parent.addNotify();
            }
            if (peer == null) {
                peer = getToolkit().createWindow(this);
            }
            synchronized (allWindows) {
                allWindows.add(this);
            }
            super.addNotify();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeNotify() {
        synchronized (getTreeLock()) {
            synchronized (allWindows) {
                allWindows.remove(this);
            }
            super.removeNotify();
        }
    }

    /**
     * 使此窗口的大小适合其子组件的首选大小和布局。窗口的宽度和
     * 高度将自动增大，如果任一维度小于通过
     * 之前的 {@code setMinimumSize} 方法指定的最小尺寸。
     * <p>
     * 如果窗口和/或其所有者尚未可显示，
     * 在计算首选大小之前，它们将被设置为可显示。计算
     * 完成后，窗口将被验证。
     *
     * @see Component#isDisplayable
     * @see #setMinimumSize
     */
    public void pack() {
        Container parent = this.parent;
        if (parent != null && parent.getPeer() == null) {
            parent.addNotify();
        }
        if (peer == null) {
            addNotify();
        }
        Dimension newSize = getPreferredSize();
        if (peer != null) {
            setClientSize(newSize.width, newSize.height);
        }

        if(beforeFirstShow) {
            isPacked = true;
        }

        validateUnconditionally();
    }

    /**
     * 将此窗口的最小尺寸设置为常量
     * 值。后续调用 {@code getMinimumSize}
     * 将始终返回此值。如果当前窗口的
     * 尺寸小于 {@code minimumSize}，窗口的大小将自动增大
     * 以满足最小尺寸要求。
     * <p>
     * 如果之后调用 {@code setSize} 或 {@code setBounds} 方法
     * 指定的宽度或高度小于
     * 通过 {@code setMinimumSize} 方法指定的值
     * 窗口将自动增大以满足
     * {@code minimumSize} 值。{@code minimumSize}
     * 值也会影响 {@code pack} 方法的行为。
     * <p>
     * 通过将最小尺寸参数设置为 {@code null} 值来恢复默认行为。
     * <p>
     * 如果用户尝试将窗口调整到小于 {@code minimumSize} 的大小，
     * 可能会限制调整大小操作。此行为取决于平台。
     *
     * @param minimumSize 此窗口的新最小尺寸
     * @see Component#setMinimumSize
     * @see #getMinimumSize
     * @see #isMinimumSizeSet
     * @see #setSize(Dimension)
     * @see #pack
     * @since 1.6
     */
    public void setMinimumSize(Dimension minimumSize) {
        synchronized (getTreeLock()) {
            super.setMinimumSize(minimumSize);
            Dimension size = getSize();
            if (isMinimumSizeSet()) {
                if (size.width < minimumSize.width || size.height < minimumSize.height) {
                    int nw = Math.max(width, minimumSize.width);
                    int nh = Math.max(height, minimumSize.height);
                    setSize(nw, nh);
                }
            }
            if (peer != null) {
                ((WindowPeer)peer).updateMinimumSize();
            }
        }
    }


                /**
     * {@inheritDoc}
     * <p>
     * {@code d.width} 和 {@code d.height} 值
     * 如果任何一个小于
     * 通过先前调用 {@code setMinimumSize} 指定的最小尺寸，
     * 则会自动增大。
     * <p>
     * 该方法更改与几何相关的数据。因此，
     * 本机窗口系统可能会忽略此类请求，或者修改
     * 请求的数据，以便 {@code Window} 对象以与桌面设置密切对应的方式放置和调整大小。
     *
     * @see #getSize
     * @see #setBounds
     * @see #setMinimumSize
     * @since 1.6
     */
    public void setSize(Dimension d) {
        super.setSize(d);
    }

    /**
     * {@inheritDoc}
     * <p>
     * {@code width} 和 {@code height} 值
     * 如果任何一个小于
     * 通过先前调用 {@code setMinimumSize} 指定的最小尺寸，
     * 则会自动增大。
     * <p>
     * 该方法更改与几何相关的数据。因此，
     * 本机窗口系统可能会忽略此类请求，或者修改
     * 请求的数据，以便 {@code Window} 对象以与桌面设置密切对应的方式放置和调整大小。
     *
     * @see #getSize
     * @see #setBounds
     * @see #setMinimumSize
     * @since 1.6
     */
    public void setSize(int width, int height) {
        super.setSize(width, height);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 该方法更改与几何相关的数据。因此，
     * 本机窗口系统可能会忽略此类请求，或者修改
     * 请求的数据，以便 {@code Window} 对象以与桌面设置密切对应的方式放置和调整大小。
     */
    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 该方法更改与几何相关的数据。因此，
     * 本机窗口系统可能会忽略此类请求，或者修改
     * 请求的数据，以便 {@code Window} 对象以与桌面设置密切对应的方式放置和调整大小。
     */
    @Override
    public void setLocation(Point p) {
        super.setLocation(p);
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 {@code setBounds(int, int, int, int)}。
     */
    @Deprecated
    public void reshape(int x, int y, int width, int height) {
        if (isMinimumSizeSet()) {
            Dimension minSize = getMinimumSize();
            if (width < minSize.width) {
                width = minSize.width;
            }
            if (height < minSize.height) {
                height = minSize.height;
            }
        }
        super.reshape(x, y, width, height);
    }

    void setClientSize(int w, int h) {
        synchronized (getTreeLock()) {
            setBoundsOp(ComponentPeer.SET_CLIENT_SIZE);
            setBounds(x, y, w, h);
        }
    }

    static private final AtomicBoolean
        beforeFirstWindowShown = new AtomicBoolean(true);

    final void closeSplashScreen() {
        if (isTrayIconWindow) {
            return;
        }
        if (beforeFirstWindowShown.getAndSet(false)) {
            // 我们不使用 SplashScreen.getSplashScreen() 以避免在用户代码未显式请求时实例化
            // 该对象
            SunToolkit.closeSplashScreen();
            SplashScreen.markClosed();
        }
    }

    /**
     * 根据参数 {@code b} 显示或隐藏此 {@code Window}。
     * <p>
     * 如果该方法显示窗口，则在以下条件下窗口也会获得焦点：
     * <ul>
     * <li> {@code Window} 满足 {@link #isFocusableWindow} 方法中概述的要求。
     * <li> {@code Window} 的 {@code autoRequestFocus} 属性值为 {@code true}。
     * <li> 本机窗口系统允许 {@code Window} 获得焦点。
     * </ul>
     * 第二个条件（{@code autoRequestFocus} 属性的值）有一个例外。如果窗口是一个阻止当前焦点窗口的模态对话框，则不会考虑该属性。
     * <p>
     * 开发者在窗口接收到 WINDOW_GAINED_FOCUS 或 WINDOW_ACTIVATED 事件之前，永远不应假设窗口是焦点窗口或活动窗口。
     * @param b  如果为 {@code true}，则使 {@code Window} 可见，
     * 否则隐藏 {@code Window}。
     * 如果 {@code Window} 和/或其所有者
     * 尚未可显示，则两者都会被设为可显示。 该
     * {@code Window} 在显示之前将被验证。
     * 如果 {@code Window} 已经可见，这将使
     * {@code Window} 置于最前面。<p>
     * 如果为 {@code false}，则隐藏此 {@code Window}，其子组件及其所有
     * 拥有的子窗口。
     * 该 {@code Window} 及其子组件可以通过调用 {@code #setVisible(true)} 再次显示。
     * @see java.awt.Component#isDisplayable
     * @see java.awt.Component#setVisible
     * @see java.awt.Window#toFront
     * @see java.awt.Window#dispose
     * @see java.awt.Window#setAutoRequestFocus
     * @see java.awt.Window#isFocusableWindow
     */
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    /**
     * 使窗口可见。如果窗口及其所有者
     * 尚未可显示，则两者都会被设为可显示。 该
     * 窗口在显示之前将被验证。
     * 如果窗口已经可见，这将使窗口
     * 置于最前面。
     * @see       Component#isDisplayable
     * @see       #toFront
     * @deprecated 自 JDK 1.5 版本起，替换为
     * {@link #setVisible(boolean)}。
     */
    @Deprecated
    public void show() {
        if (peer == null) {
            addNotify();
        }
        validateUnconditionally();

        isInShow = true;
        if (visible) {
            toFront();
        } else {
            beforeFirstShow = false;
            closeSplashScreen();
            Dialog.checkShouldBeBlocked(this);
            super.show();
            locationByPlatform = false;
            for (int i = 0; i < ownedWindowList.size(); i++) {
                Window child = ownedWindowList.elementAt(i).get();
                if ((child != null) && child.showWithParent) {
                    child.show();
                    child.showWithParent = false;
                }       // endif
            }   // endfor
            if (!isModalBlocked()) {
                updateChildrenBlocking();
            } else {
                // 修复 6532736：显示此窗口后，其阻塞器
                // 应被置于最前面
                modalBlocker.toFront_NoClientCode();
            }
            if (this instanceof Frame || this instanceof Dialog) {
                updateChildFocusableWindowState(this);
            }
        }
        isInShow = false;

        // 如果首次显示，生成 WindowOpened 事件
        if ((state & OPENED) == 0) {
            postWindowEvent(WindowEvent.WINDOW_OPENED);
            state |= OPENED;
        }
    }

    static void updateChildFocusableWindowState(Window w) {
        if (w.getPeer() != null && w.isShowing()) {
            ((WindowPeer)w.getPeer()).updateFocusableWindowState();
        }
        for (int i = 0; i < w.ownedWindowList.size(); i++) {
            Window child = w.ownedWindowList.elementAt(i).get();
            if (child != null) {
                updateChildFocusableWindowState(child);
            }
        }
    }

    synchronized void postWindowEvent(int id) {
        if (windowListener != null
            || (eventMask & AWTEvent.WINDOW_EVENT_MASK) != 0
            ||  Toolkit.enabledOnToolkit(AWTEvent.WINDOW_EVENT_MASK)) {
            WindowEvent e = new WindowEvent(this, id);
            Toolkit.getEventQueue().postEvent(e);
        }
    }

    /**
     * 隐藏此窗口、其子组件及其所有
     * 拥有的子窗口。
     * 该窗口及其子组件可以通过调用 {@code show} 再次显示。
     * @see #show
     * @see #dispose
     * @deprecated 自 JDK 1.5 版本起，替换为
     * {@link #setVisible(boolean)}。
     */
    @Deprecated
    public void hide() {
        synchronized(ownedWindowList) {
            for (int i = 0; i < ownedWindowList.size(); i++) {
                Window child = ownedWindowList.elementAt(i).get();
                if ((child != null) && child.visible) {
                    child.hide();
                    child.showWithParent = true;
                }
            }
        }
        if (isModalBlocked()) {
            modalBlocker.unblockWindow(this);
        }
        super.hide();
        locationByPlatform = false;
    }

    final void clearMostRecentFocusOwnerOnHide() {
        /* do nothing */
    }

    /**
     * 释放此 {@code Window}、其子组件及其所有
     * 拥有的子窗口使用的本机屏幕资源。也就是说，这些 {@code Component} 的资源
     * 将被销毁，它们消耗的内存将返还给操作系统，它们将被标记为不可显示。
     * <p>
     * 该 {@code Window} 及其子组件可以通过后续调用
     * {@code pack} 或 {@code show} 重新创建本机资源并再次设为可显示。重新创建的
     * {@code Window} 及其子组件的状态将与这些对象在窗口被销毁时的状态相同（不考虑
     * 之间的其他修改）。
     * <p>
     * <b>注意</b>：当 Java 虚拟机 (VM) 中的最后一个可显示窗口
     * 被销毁时，VM 可能会终止。有关更多信息，请参阅 <a href="doc-files/AWTThreadIssues.html#Autoshutdown">
     * AWT 线程问题</a>。
     * @see Component#isDisplayable
     * @see #pack
     * @see #show
     */
    public void dispose() {
        doDispose();
    }

    /*
     * 修复 4872170。
     * 如果在父窗口上调用 dispose()，则其子窗口也必须被销毁
     * 如 javadoc 中所述。因此，即使子窗口覆盖了 dispose() 并未调用 super.dispose()，
     * 我们也需要实现此功能。
     */
    void disposeImpl() {
        dispose();
        if (getPeer() != null) {
            doDispose();
        }
    }

    void doDispose() {
    class DisposeAction implements Runnable {
        public void run() {
            disposing = true;
            try {
                // 检查此窗口是否为设备的全屏窗口。如果是，
                // 在销毁窗口之前退出全屏模式。
                GraphicsDevice gd = getGraphicsConfiguration().getDevice();
                if (gd.getFullScreenWindow() == Window.this) {
                    gd.setFullScreenWindow(null);
                }

                Object[] ownedWindowArray;
                synchronized(ownedWindowList) {
                    ownedWindowArray = new Object[ownedWindowList.size()];
                    ownedWindowList.copyInto(ownedWindowArray);
                }
                for (int i = 0; i < ownedWindowArray.length; i++) {
                    Window child = (Window) (((WeakReference)
                                   (ownedWindowArray[i])).get());
                    if (child != null) {
                        child.disposeImpl();
                    }
                }
                hide();
                beforeFirstShow = true;
                removeNotify();
                synchronized (inputContextLock) {
                    if (inputContext != null) {
                        inputContext.dispose();
                        inputContext = null;
                    }
                }
                clearCurrentFocusCycleRootOnHide();
            } finally {
                disposing = false;
            }
        }
    }
        boolean fireWindowClosedEvent = isDisplayable();
        DisposeAction action = new DisposeAction();
        if (EventQueue.isDispatchThread()) {
            action.run();
        }
        else {
            try {
                EventQueue.invokeAndWait(this, action);
            }
            catch (InterruptedException e) {
                System.err.println("销毁被中断：");
                e.printStackTrace();
            }
            catch (InvocationTargetException e) {
                System.err.println("销毁期间发生异常：");
                e.printStackTrace();
            }
        }
        // 在 Runnable 外部执行，因为 postWindowEvent 是
        // 同步的 (this)。我们不需要在 EventQueue 上同步调用。
        if (fireWindowClosedEvent) {
            postWindowEvent(WindowEvent.WINDOW_CLOSED);
        }
    }

    /*
     * 应在持有树锁时调用。
     * 在 Window 中重写是因为 parent == owner，
     * 我们不应该在 owner 上调整计数器
     */
    void adjustListeningChildrenOnParent(long mask, int num) {
    }

    // 应在持有树锁时调用
    void adjustDecendantsOnParent(int num) {
        // 由于 parent == owner，我们不应该
        // 在 owner 上调整计数器，因此这里不做任何操作
    }

    /**
     * 如果此窗口可见，则将此窗口置于最前面，并可能使其成为焦点窗口。
     * <p>
     * 将此窗口置于堆叠顺序的最上方，并显示在本 VM 中的其他窗口前面。如果此窗口不可见，则不会采取任何操作。某些平台不允许拥有其他窗口的窗口
     * 出现在其拥有的窗口之上。某些平台可能不允许此 VM 将其窗口置于本机应用程序窗口或
     * 其他 VM 的窗口之上。此权限可能取决于
     * 是否有窗口在本 VM 中已经获得焦点。将尽一切努力将此窗口移至堆叠顺序的最上方；
     * 然而，开发者不应假设此方法在每种情况下都会将此窗口置于所有其他窗口之上。
     * <p>
     * 开发者在窗口接收到 WINDOW_GAINED_FOCUS 或 WINDOW_ACTIVATED 事件之前，永远不应假设窗口是焦点窗口或活动窗口。
     * 在那些顶部窗口是焦点窗口的平台上，此方法将 <b>可能</b> 在以下条件下使此窗口获得焦点（如果它尚未获得焦点）：
     * <ul>
     * <li> 窗口满足 {@link #isFocusableWindow} 方法中概述的要求。
     * <li> 窗口的属性 {@code autoRequestFocus} 值为 {@code true}。
     * <li> 本机窗口系统允许窗口获得焦点。
     * </ul>
     * 在那些堆叠顺序通常不影响焦点窗口的平台上，此方法将 <b>可能</b> 使焦点和活动窗口保持不变。
     * <p>
     * 如果此方法使此窗口获得焦点，并且此窗口是 Frame 或 Dialog，则它也会被激活。如果此窗口获得焦点，但不是 Frame 或 Dialog，则第一个 Frame 或
     * Dialog（作为此窗口的所有者）将被激活。
     * <p>
     * 如果此窗口被模态对话框阻塞，则阻塞对话框将被置于最前面并保持在被阻塞窗口之上。
     *
     * @see       #toBack
     * @see       #setAutoRequestFocus
     * @see       #isFocusableWindow
     */
    public void toFront() {
        toFront_NoClientCode();
    }


                // 此功能实现在一个最终的包私有方法中
    // 以确保它不能被客户端子类覆盖。
    final void toFront_NoClientCode() {
        if (visible) {
            WindowPeer peer = (WindowPeer)this.peer;
            if (peer != null) {
                peer.toFront();
            }
            if (isModalBlocked()) {
                modalBlocker.toFront_NoClientCode();
            }
        }
    }

    /**
     * 如果此窗口可见，则将此窗口发送到后方，并可能导致
     * 它失去焦点或激活，如果它是焦点或活动窗口。
     * <p>
     * 将此窗口置于堆叠顺序的底部，并显示在
     * 本虚拟机中的其他窗口之后。如果此窗口不可见，则不会采取任何行动。
     * 一些平台不允许由其他窗口拥有的窗口出现在其所有者之下。
     * 尽管会尽一切努力将此窗口尽可能低地移动到堆叠顺序中；
     * 然而，开发人员不应假设此方法会在所有情况下
     * 将此窗口移动到所有其他窗口之下。
     * <p>
     * 由于原生窗口系统的差异，无法保证
     * 焦点和活动窗口的变化。开发人员必须
     * 在此窗口接收到 WINDOW_LOST_FOCUS 或 WINDOW_DEACTIVATED
     * 事件之前，不要假设此窗口不再是焦点或活动窗口。
     * 在某些平台上，最顶层的窗口是焦点窗口，
     * 这种情况下，此方法可能会导致此窗口失去焦点。
     * 在这种情况下，本虚拟机中下一个最高、可聚焦的窗口将获得焦点。
     * 在堆叠顺序通常不影响焦点窗口的平台上，
     * 此方法可能会使焦点和活动窗口保持不变。
     *
     * @see       #toFront
     */
    public void toBack() {
        toBack_NoClientCode();
    }

    // 此功能实现在一个最终的包私有方法中
    // 以确保它不能被客户端子类覆盖。
    final void toBack_NoClientCode() {
        if(isAlwaysOnTop()) {
            try {
                setAlwaysOnTop(false);
            }catch(SecurityException e) {
            }
        }
        if (visible) {
            WindowPeer peer = (WindowPeer)this.peer;
            if (peer != null) {
                peer.toBack();
            }
        }
    }

    /**
     * 返回此框架的工具包。
     * @return    此窗口的工具包。
     * @see       Toolkit
     * @see       Toolkit#getDefaultToolkit
     * @see       Component#getToolkit
     */
    public Toolkit getToolkit() {
        return Toolkit.getDefaultToolkit();
    }

    /**
     * 获取与此窗口一起显示的警告字符串。
     * 如果此窗口不安全，警告字符串将显示在
     * 窗口的可见区域中。如果存在安全经理并且安全经理拒绝
     * {@code AWTPermission("showWindowWithoutWarningBanner")}，
     * 则窗口不安全。
     * <p>
     * 如果窗口安全，则 {@code getWarningString}
     * 返回 {@code null}。如果窗口不安全，此
     * 方法会检查系统属性
     * {@code awt.appletWarning}
     * 并返回该属性的字符串值。
     * @return    此窗口的警告字符串。
     */
    public final String getWarningString() {
        return warningString;
    }

    private void setWarningString() {
        warningString = null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                sm.checkPermission(SecurityConstants.AWT.TOPLEVEL_WINDOW_PERMISSION);
            } catch (SecurityException se) {
                // 确保特权操作仅用于获取属性！
                // 我们不希望上面的 checkPermission 调用总是成功！
                warningString = AccessController.doPrivileged(
                      new GetPropertyAction("awt.appletWarning",
                                            "Java Applet Window"));
            }
        }
    }

    /**
     * 获取与此窗口关联的 {@code Locale} 对象，如果已设置。
     * 如果未设置任何区域设置，则返回默认区域设置。
     * @return    为此窗口设置的区域设置。
     * @see       java.util.Locale
     * @since     JDK1.1
     */
    public Locale getLocale() {
      if (this.locale == null) {
        return Locale.getDefault();
      }
      return this.locale;
    }

    /**
     * 获取此窗口的输入上下文。窗口始终有一个输入上下文，
     * 该上下文由子组件共享，除非子组件创建并设置了自己的上下文。
     * @see Component#getInputContext
     * @since 1.2
     */
    public InputContext getInputContext() {
        synchronized (inputContextLock) {
            if (inputContext == null) {
                inputContext = InputContext.getInstance();
            }
        }
        return inputContext;
    }

    /**
     * 将光标图像设置为指定的光标。
     * <p>
     * 如果 Java 平台实现和/或原生系统不支持
     * 更改鼠标光标的形状，此方法可能没有视觉效果。
     * @param     cursor 由 {@code Cursor} 类定义的常量之一。
     *            如果此参数为 null，则此窗口的光标将设置为
     *            Cursor.DEFAULT_CURSOR 类型。
     * @see       Component#getCursor
     * @see       Cursor
     * @since     JDK1.1
     */
    public void setCursor(Cursor cursor) {
        if (cursor == null) {
            cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        }
        super.setCursor(cursor);
    }

    /**
     * 返回此窗口的所有者。
     * @since 1.2
     */
    public Window getOwner() {
        return getOwner_NoClientCode();
    }
    final Window getOwner_NoClientCode() {
        return (Window)parent;
    }

    /**
     * 返回此窗口当前拥有的所有窗口的数组。
     * @since 1.2
     */
    public Window[] getOwnedWindows() {
        return getOwnedWindows_NoClientCode();
    }
    final Window[] getOwnedWindows_NoClientCode() {
        Window realCopy[];

        synchronized(ownedWindowList) {
            // 回想一下，ownedWindowList 实际上是一个
            // WeakReferences 的 Vector，调用其中一个引用的 get()
            // 可能会返回 null。创建两个数组——一个与
            // Vector 的大小相同（fullCopy 大小为 fullSize），另一个与
            // 所有非 null get() 的大小相同（realCopy 大小为 realSize）。
            int fullSize = ownedWindowList.size();
            int realSize = 0;
            Window fullCopy[] = new Window[fullSize];

            for (int i = 0; i < fullSize; i++) {
                fullCopy[realSize] = ownedWindowList.elementAt(i).get();

                if (fullCopy[realSize] != null) {
                    realSize++;
                }
            }

            if (fullSize != realSize) {
                realCopy = Arrays.copyOf(fullCopy, realSize);
            } else {
                realCopy = fullCopy;
            }
        }

        return realCopy;
    }

    boolean isModalBlocked() {
        return modalBlocker != null;
    }

    void setModalBlocked(Dialog blocker, boolean blocked, boolean peerCall) {
        this.modalBlocker = blocked ? blocker : null;
        if (peerCall) {
            WindowPeer peer = (WindowPeer)this.peer;
            if (peer != null) {
                peer.setModalBlocked(blocker, blocked);
            }
        }
    }

    Dialog getModalBlocker() {
        return modalBlocker;
    }

    /*
     * 返回所有可显示的窗口列表，即所有
     * peer 不为 null 的窗口。
     *
     * @see #addNotify
     * @see #removeNotify
     */
    static IdentityArrayList<Window> getAllWindows() {
        synchronized (allWindows) {
            IdentityArrayList<Window> v = new IdentityArrayList<Window>();
            v.addAll(allWindows);
            return v;
        }
    }

    static IdentityArrayList<Window> getAllUnblockedWindows() {
        synchronized (allWindows) {
            IdentityArrayList<Window> unblocked = new IdentityArrayList<Window>();
            for (int i = 0; i < allWindows.size(); i++) {
                Window w = allWindows.get(i);
                if (!w.isModalBlocked()) {
                    unblocked.add(w);
                }
            }
            return unblocked;
        }
    }

    private static Window[] getWindows(AppContext appContext) {
        synchronized (Window.class) {
            Window realCopy[];
            @SuppressWarnings("unchecked")
            Vector<WeakReference<Window>> windowList =
                (Vector<WeakReference<Window>>)appContext.get(Window.class);
            if (windowList != null) {
                int fullSize = windowList.size();
                int realSize = 0;
                Window fullCopy[] = new Window[fullSize];
                for (int i = 0; i < fullSize; i++) {
                    Window w = windowList.get(i).get();
                    if (w != null) {
                        fullCopy[realSize++] = w;
                    }
                }
                if (fullSize != realSize) {
                    realCopy = Arrays.copyOf(fullCopy, realSize);
                } else {
                    realCopy = fullCopy;
                }
            } else {
                realCopy = new Window[0];
            }
            return realCopy;
        }
    }

    /**
     * 返回由该应用程序创建的所有 {@code Window} 的数组，包括拥有者和无拥有者的窗口。
     * 如果从 applet 调用，数组仅包括该 applet 可访问的 {@code Window}。
     * <p>
     * <b>警告：</b>此方法可能返回系统创建的窗口，例如打印对话框。
     * 应用程序不应假设这些对话框的存在，也不应假设这些对话框的任何属性，
     * 如组件位置、{@code LayoutManager} 或序列化。
     *
     * @see Frame#getFrames
     * @see Window#getOwnerlessWindows
     *
     * @since 1.6
     */
    public static Window[] getWindows() {
        return getWindows(AppContext.getAppContext());
    }

    /**
     * 返回由该应用程序创建的所有无拥有者的 {@code Window} 的数组。
     * 它们包括 {@code Frame} 和无拥有者的 {@code Dialog} 和 {@code Window}。
     * 如果从 applet 调用，数组仅包括该 applet 可访问的 {@code Window}。
     * <p>
     * <b>警告：</b>此方法可能返回系统创建的窗口，例如打印对话框。
     * 应用程序不应假设这些对话框的存在，也不应假设这些对话框的任何属性，
     * 如组件位置、{@code LayoutManager} 或序列化。
     *
     * @see Frame#getFrames
     * @see Window#getWindows()
     *
     * @since 1.6
     */
    public static Window[] getOwnerlessWindows() {
        Window[] allWindows = Window.getWindows();

        int ownerlessCount = 0;
        for (Window w : allWindows) {
            if (w.getOwner() == null) {
                ownerlessCount++;
            }
        }

        Window[] ownerless = new Window[ownerlessCount];
        int c = 0;
        for (Window w : allWindows) {
            if (w.getOwner() == null) {
                ownerless[c++] = w;
            }
        }

        return ownerless;
    }

    Window getDocumentRoot() {
        synchronized (getTreeLock()) {
            Window w = this;
            while (w.getOwner() != null) {
                w = w.getOwner();
            }
            return w;
        }
    }

    /**
     * 指定此窗口的模态排除类型。如果窗口被模态排除，
     * 它将不会被某些模态对话框阻塞。有关可能的模态排除类型，
     * 请参见 {@link java.awt.Dialog.ModalExclusionType Dialog.ModalExclusionType}。
     * <p>
     * 如果给定的类型不受支持，则使用 {@code NO_EXCLUDE}。
     * <p>
     * 注意：更改可见窗口的模态排除类型可能不会立即生效，
     * 直到窗口被隐藏然后再显示。
     *
     * @param exclusionType 此窗口的模态排除类型；{@code null}
     *     值等同于 {@link Dialog.ModalExclusionType#NO_EXCLUDE
     *     NO_EXCLUDE}
     * @throws SecurityException 如果调用线程没有权限
     *     将模态排除属性设置为给定的 {@code exclusionType}
     * @see java.awt.Dialog.ModalExclusionType
     * @see java.awt.Window#getModalExclusionType
     * @see java.awt.Toolkit#isModalExclusionTypeSupported
     *
     * @since 1.6
     */
    public void setModalExclusionType(Dialog.ModalExclusionType exclusionType) {
        if (exclusionType == null) {
            exclusionType = Dialog.ModalExclusionType.NO_EXCLUDE;
        }
        if (!Toolkit.getDefaultToolkit().isModalExclusionTypeSupported(exclusionType)) {
            exclusionType = Dialog.ModalExclusionType.NO_EXCLUDE;
        }
        if (modalExclusionType == exclusionType) {
            return;
        }
        if (exclusionType == Dialog.ModalExclusionType.TOOLKIT_EXCLUDE) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SecurityConstants.AWT.TOOLKIT_MODALITY_PERMISSION);
            }
        }
        modalExclusionType = exclusionType;

        // 如果需要即时更改，需要取消下面的注释
        //   并在 Dialog 中重写该方法以使用 modalShow() 而不是 updateChildrenBlocking()
 /*
        if (isModalBlocked()) {
            modalBlocker.unblockWindow(this);
        }
        Dialog.checkShouldBeBlocked(this);
        updateChildrenBlocking();
 */
    }

    /**
     * 返回此窗口的模态排除类型。
     *
     * @return 此窗口的模态排除类型
     *
     * @see java.awt.Dialog.ModalExclusionType
     * @see java.awt.Window#setModalExclusionType
     *
     * @since 1.6
     */
    public Dialog.ModalExclusionType getModalExclusionType() {
        return modalExclusionType;
    }

    boolean isModalExcluded(Dialog.ModalExclusionType exclusionType) {
        if ((modalExclusionType != null) &&
            modalExclusionType.compareTo(exclusionType) >= 0)
        {
            return true;
        }
        Window owner = getOwner_NoClientCode();
        return (owner != null) && owner.isModalExcluded(exclusionType);
    }

    void updateChildrenBlocking() {
        Vector<Window> childHierarchy = new Vector<Window>();
        Window[] ownedWindows = getOwnedWindows();
        for (int i = 0; i < ownedWindows.length; i++) {
            childHierarchy.add(ownedWindows[i]);
        }
        int k = 0;
        while (k < childHierarchy.size()) {
            Window w = childHierarchy.get(k);
            if (w.isVisible()) {
                if (w.isModalBlocked()) {
                    Dialog blocker = w.getModalBlocker();
                    blocker.unblockWindow(w);
                }
                Dialog.checkShouldBeBlocked(w);
                Window[] wOwned = w.getOwnedWindows();
                for (int j = 0; j < wOwned.length; j++) {
                    childHierarchy.add(wOwned[j]);
                }
            }
            k++;
        }
    }


                /**
     * 将指定的窗口监听器添加到此窗口，以接收窗口事件。
     * 如果 l 为 null，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 的线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param   l 窗口监听器
     * @see #removeWindowListener
     * @see #getWindowListeners
     */
    public synchronized void addWindowListener(WindowListener l) {
        if (l == null) {
            return;
        }
        newEventsOnly = true;
        windowListener = AWTEventMulticaster.add(windowListener, l);
    }

    /**
     * 将指定的窗口状态监听器添加到此窗口，以接收窗口事件。如果 {@code l} 为 {@code null}，
     * 则不抛出异常且不执行任何操作。
     * <p>有关 AWT 的线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param   l 窗口状态监听器
     * @see #removeWindowStateListener
     * @see #getWindowStateListeners
     * @since 1.4
     */
    public synchronized void addWindowStateListener(WindowStateListener l) {
        if (l == null) {
            return;
        }
        windowStateListener = AWTEventMulticaster.add(windowStateListener, l);
        newEventsOnly = true;
    }

    /**
     * 将指定的窗口焦点监听器添加到此窗口，以接收窗口事件。
     * 如果 l 为 null，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 的线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param   l 窗口焦点监听器
     * @see #removeWindowFocusListener
     * @see #getWindowFocusListeners
     * @since 1.4
     */
    public synchronized void addWindowFocusListener(WindowFocusListener l) {
        if (l == null) {
            return;
        }
        windowFocusListener = AWTEventMulticaster.add(windowFocusListener, l);
        newEventsOnly = true;
    }

    /**
     * 移除指定的窗口监听器，使其不再接收此窗口的窗口事件。
     * 如果 l 为 null，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 的线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param   l 窗口监听器
     * @see #addWindowListener
     * @see #getWindowListeners
     */
    public synchronized void removeWindowListener(WindowListener l) {
        if (l == null) {
            return;
        }
        windowListener = AWTEventMulticaster.remove(windowListener, l);
    }

    /**
     * 移除指定的窗口状态监听器，使其不再接收此窗口的窗口事件。如果
     * {@code l} 为 {@code null}，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 的线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param   l 窗口状态监听器
     * @see #addWindowStateListener
     * @see #getWindowStateListeners
     * @since 1.4
     */
    public synchronized void removeWindowStateListener(WindowStateListener l) {
        if (l == null) {
            return;
        }
        windowStateListener = AWTEventMulticaster.remove(windowStateListener, l);
    }

    /**
     * 移除指定的窗口焦点监听器，使其不再接收此窗口的窗口事件。
     * 如果 l 为 null，则不抛出异常且不执行任何操作。
     * <p>有关 AWT 的线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param   l 窗口焦点监听器
     * @see #addWindowFocusListener
     * @see #getWindowFocusListeners
     * @since 1.4
     */
    public synchronized void removeWindowFocusListener(WindowFocusListener l) {
        if (l == null) {
            return;
        }
        windowFocusListener = AWTEventMulticaster.remove(windowFocusListener, l);
    }

    /**
     * 返回在此窗口上注册的所有窗口监听器的数组。
     *
     * @return 此窗口的所有 {@code WindowListener}，如果没有注册窗口
     *         监听器，则返回一个空数组
     *
     * @see #addWindowListener
     * @see #removeWindowListener
     * @since 1.4
     */
    public synchronized WindowListener[] getWindowListeners() {
        return getListeners(WindowListener.class);
    }

    /**
     * 返回在此窗口上注册的所有窗口焦点监听器的数组。
     *
     * @return 此窗口的所有 {@code WindowFocusListener}，如果没有注册窗口焦点
     *         监听器，则返回一个空数组
     *
     * @see #addWindowFocusListener
     * @see #removeWindowFocusListener
     * @since 1.4
     */
    public synchronized WindowFocusListener[] getWindowFocusListeners() {
        return getListeners(WindowFocusListener.class);
    }

    /**
     * 返回在此窗口上注册的所有窗口状态监听器的数组。
     *
     * @return 此窗口的所有 {@code WindowStateListener}，如果没有注册窗口状态
     *         监听器，则返回一个空数组
     *
     * @see #addWindowStateListener
     * @see #removeWindowStateListener
     * @since 1.4
     */
    public synchronized WindowStateListener[] getWindowStateListeners() {
        return getListeners(WindowStateListener.class);
    }


    /**
     * 返回在此 {@code Window} 上注册的所有 <code><em>Foo</em>Listener</code> 的数组。
     * <code><em>Foo</em>Listener</code> 是使用 <code>add<em>Foo</em>Listener</code> 方法注册的。
     *
     * <p>
     *
     * 可以使用类字面量指定 {@code listenerType} 参数，例如
     * <code><em>Foo</em>Listener.class</code>。例如，可以使用以下代码查询
     * {@code Window} {@code w} 的窗口监听器：
     *
     * <pre>WindowListener[] wls = (WindowListener[])(w.getListeners(WindowListener.class));</pre>
     *
     * 如果没有注册此类监听器，此方法将返回一个空数组。
     *
     * @param listenerType 请求的监听器类型；此参数应指定一个实现
     *          {@code java.util.EventListener} 的类或接口
     * @return 在此窗口上注册的所有 <code><em>Foo</em>Listener</code> 的数组，
     *          如果没有注册此类监听器，则返回一个空数组
     * @exception ClassCastException 如果 {@code listenerType}
     *          不指定一个实现 {@code java.util.EventListener} 的类或接口
     * @exception NullPointerException 如果 {@code listenerType} 为 {@code null}
     *
     * @see #getWindowListeners
     * @since 1.3
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if (listenerType == WindowFocusListener.class) {
            l = windowFocusListener;
        } else if (listenerType == WindowStateListener.class) {
            l = windowStateListener;
        } else if (listenerType == WindowListener.class) {
            l = windowListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }

    // REMIND: remove when filtering is handled at lower level
    boolean eventEnabled(AWTEvent e) {
        switch(e.id) {
          case WindowEvent.WINDOW_OPENED:
          case WindowEvent.WINDOW_CLOSING:
          case WindowEvent.WINDOW_CLOSED:
          case WindowEvent.WINDOW_ICONIFIED:
          case WindowEvent.WINDOW_DEICONIFIED:
          case WindowEvent.WINDOW_ACTIVATED:
          case WindowEvent.WINDOW_DEACTIVATED:
            if ((eventMask & AWTEvent.WINDOW_EVENT_MASK) != 0 ||
                windowListener != null) {
                return true;
            }
            return false;
          case WindowEvent.WINDOW_GAINED_FOCUS:
          case WindowEvent.WINDOW_LOST_FOCUS:
            if ((eventMask & AWTEvent.WINDOW_FOCUS_EVENT_MASK) != 0 ||
                windowFocusListener != null) {
                return true;
            }
            return false;
          case WindowEvent.WINDOW_STATE_CHANGED:
            if ((eventMask & AWTEvent.WINDOW_STATE_EVENT_MASK) != 0 ||
                windowStateListener != null) {
                return true;
            }
            return false;
          default:
            break;
        }
        return super.eventEnabled(e);
    }

    /**
     * 处理此窗口上的事件。如果事件是 {@code WindowEvent}，则调用
     * {@code processWindowEvent} 方法，否则调用其父类的 {@code processEvent} 方法。
     * <p>注意，如果事件参数为 {@code null}，则行为未指定，可能会导致异常。
     *
     * @param e 事件
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof WindowEvent) {
            switch (e.getID()) {
                case WindowEvent.WINDOW_OPENED:
                case WindowEvent.WINDOW_CLOSING:
                case WindowEvent.WINDOW_CLOSED:
                case WindowEvent.WINDOW_ICONIFIED:
                case WindowEvent.WINDOW_DEICONIFIED:
                case WindowEvent.WINDOW_ACTIVATED:
                case WindowEvent.WINDOW_DEACTIVATED:
                    processWindowEvent((WindowEvent)e);
                    break;
                case WindowEvent.WINDOW_GAINED_FOCUS:
                case WindowEvent.WINDOW_LOST_FOCUS:
                    processWindowFocusEvent((WindowEvent)e);
                    break;
                case WindowEvent.WINDOW_STATE_CHANGED:
                    processWindowStateEvent((WindowEvent)e);
                    break;
            }
            return;
        }
        super.processEvent(e);
    }

    /**
     * 通过分派给任何已注册的 WindowListener 对象来处理此窗口上的窗口事件。
     * 注意：除非为该组件启用了窗口事件，否则不会调用此方法；这发生在以下情况之一：
     * <ul>
     * <li>通过 {@code addWindowListener} 注册了 WindowListener 对象
     * <li>通过 {@code enableEvents} 启用了窗口事件
     * </ul>
     * <p>注意，如果事件参数为 {@code null}，则行为未指定，可能会导致异常。
     *
     * @param e 窗口事件
     * @see Component#enableEvents
     */
    protected void processWindowEvent(WindowEvent e) {
        WindowListener listener = windowListener;
        if (listener != null) {
            switch(e.getID()) {
                case WindowEvent.WINDOW_OPENED:
                    listener.windowOpened(e);
                    break;
                case WindowEvent.WINDOW_CLOSING:
                    listener.windowClosing(e);
                    break;
                case WindowEvent.WINDOW_CLOSED:
                    listener.windowClosed(e);
                    break;
                case WindowEvent.WINDOW_ICONIFIED:
                    listener.windowIconified(e);
                    break;
                case WindowEvent.WINDOW_DEICONIFIED:
                    listener.windowDeiconified(e);
                    break;
                case WindowEvent.WINDOW_ACTIVATED:
                    listener.windowActivated(e);
                    break;
                case WindowEvent.WINDOW_DEACTIVATED:
                    listener.windowDeactivated(e);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 通过分派给任何已注册的 WindowFocusListener 对象来处理此窗口上的窗口焦点事件。
     * 注意：除非为该窗口启用了窗口焦点事件，否则不会调用此方法。这发生在以下情况之一：
     * <ul>
     * <li>通过 {@code addWindowFocusListener} 注册了 WindowFocusListener 对象
     * <li>通过 {@code enableEvents} 启用了窗口焦点事件
     * </ul>
     * <p>注意，如果事件参数为 {@code null}，则行为未指定，可能会导致异常。
     *
     * @param e 窗口焦点事件
     * @see Component#enableEvents
     * @since 1.4
     */
    protected void processWindowFocusEvent(WindowEvent e) {
        WindowFocusListener listener = windowFocusListener;
        if (listener != null) {
            switch (e.getID()) {
                case WindowEvent.WINDOW_GAINED_FOCUS:
                    listener.windowGainedFocus(e);
                    break;
                case WindowEvent.WINDOW_LOST_FOCUS:
                    listener.windowLostFocus(e);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 通过分派给任何已注册的 {@code WindowStateListener} 对象来处理此窗口上的窗口状态事件。
     * 注意：除非为该窗口启用了窗口状态事件，否则不会调用此方法。这发生在以下情况之一：
     * <ul>
     * <li>通过 {@code addWindowStateListener} 注册了 {@code WindowStateListener} 对象
     * <li>通过 {@code enableEvents} 启用了窗口状态事件
     * </ul>
     * <p>注意，如果事件参数为 {@code null}，则行为未指定，可能会导致异常。
     *
     * @param e 窗口状态事件
     * @see java.awt.Component#enableEvents
     * @since 1.4
     */
    protected void processWindowStateEvent(WindowEvent e) {
        WindowStateListener listener = windowStateListener;
        if (listener != null) {
            switch (e.getID()) {
                case WindowEvent.WINDOW_STATE_CHANGED:
                    listener.windowStateChanged(e);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 实现一个调试钩子——检查用户是否按下了 <i>控制-Shift-F1</i>。如果是，则将子窗口列表输出到 {@code System.out}。
     * @param e 键盘事件
     */
    void preProcessKeyEvent(KeyEvent e) {
        // 将子窗口列表输出到 System.out。
        if (e.isActionKey() && e.getKeyCode() == KeyEvent.VK_F1 &&
            e.isControlDown() && e.isShiftDown() &&
            e.getID() == KeyEvent.KEY_PRESSED) {
            list(System.out, 0);
        }
    }

    void postProcessKeyEvent(KeyEvent e) {
        // 不做任何操作
    }


    /**
     * 设置此窗口是否应始终位于其他窗口之上。如果有多个始终位于顶部的窗口，它们的相对顺序
     * 未指定且依赖于平台。
     * <p>
     * 如果其他窗口已经是始终位于顶部的窗口，则这些窗口之间的相对顺序未指定（取决于
     * 平台）。没有窗口可以被置于始终位于顶部的窗口之上，除非可能是另一个始终位于顶部的窗口。
     * <p>
     * 所有由始终位于顶部的窗口拥有的窗口将继承此状态并自动成为始终位于顶部的窗口。如果窗口不再
     * 始终位于顶部，其拥有的窗口将不再始终位于顶部。当始终位于顶部的窗口被发送 {@link #toBack
     * toBack} 时，其始终位于顶部的状态将被设置为 {@code false}。
     *
     * <p> 当此方法被调用且参数值为 {@code true} 时，如果窗口可见且平台支持此窗口的始终位于顶部，
     * 窗口将立即被带到前面，“粘”在最顶部。如果窗口当前不可见，此方法将始终位于顶部的状态设置为
     * {@code true} 但不会将窗口带到前面。当窗口稍后显示时，它将始终位于顶部。
     *
     * <p> 当此方法被调用且参数值为 {@code false} 时，始终位于顶部的状态将被设置为正常。它还可能导致
     * 顶层窗口的 z 顺序发生未指定的、平台依赖的变化，但其他始终位于顶部的窗口将保持在最顶部位置。调用此方法
     * 并将参数值设置为 {@code false} 的窗口如果已经处于正常状态，将不会产生任何效果。
     *
     * <p><b>注意</b>：某些平台可能不支持始终位于顶部的窗口。要检测当前平台是否支持始终位于顶部的窗口，
     * 请使用 {@link Toolkit#isAlwaysOnTopSupported()} 和 {@link Window#isAlwaysOnTopSupported()}。如果始终位于顶部模式
     * 不支持此窗口或此窗口的工具包不支持始终位于顶部的窗口，调用此方法将不会产生任何效果。
     * <p>
     * 如果安装了 SecurityManager，调用线程必须被授予 AWTPermission "setWindowAlwaysOnTop" 才能
     * 设置此属性的值。如果未授予此权限，此方法将抛出 SecurityException，且属性的当前值将保持不变。
     *
     * @param alwaysOnTop 如果窗口应始终位于其他窗口之上，则为 true
     * @throws SecurityException 如果调用线程没有权限设置始终位于顶部属性的值
     *
     * @see #isAlwaysOnTop
     * @see #toFront
     * @see #toBack
     * @see AWTPermission
     * @see #isAlwaysOnTopSupported
     * @see #getToolkit
     * @see Toolkit#isAlwaysOnTopSupported
     * @since 1.5
     */
    public final void setAlwaysOnTop(boolean alwaysOnTop) throws SecurityException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(SecurityConstants.AWT.SET_WINDOW_ALWAYS_ON_TOP_PERMISSION);
        }


                    boolean oldAlwaysOnTop;
        synchronized(this) {
            oldAlwaysOnTop = this.alwaysOnTop;
            this.alwaysOnTop = alwaysOnTop;
        }
        if (oldAlwaysOnTop != alwaysOnTop ) {
            if (isAlwaysOnTopSupported()) {
                WindowPeer peer = (WindowPeer)this.peer;
                synchronized(getTreeLock()) {
                    if (peer != null) {
                        peer.updateAlwaysOnTopState();
                    }
                }
            }
            firePropertyChange("alwaysOnTop", oldAlwaysOnTop, alwaysOnTop);
        }
        setOwnedWindowsAlwaysOnTop(alwaysOnTop);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setOwnedWindowsAlwaysOnTop(boolean alwaysOnTop) {
        WeakReference<Window>[] ownedWindowArray;
        synchronized (ownedWindowList) {
            ownedWindowArray = new WeakReference[ownedWindowList.size()];
            ownedWindowList.copyInto(ownedWindowArray);
        }

        for (WeakReference<Window> ref : ownedWindowArray) {
            Window window = ref.get();
            if (window != null) {
                try {
                    window.setAlwaysOnTop(alwaysOnTop);
                } catch (SecurityException ignore) {
                }
            }
        }
    }

    /**
     * 返回此窗口是否支持始终置顶模式。某些平台可能不支持始终置顶窗口，某些平台可能只支持某些类型的顶级窗口；例如，平台可能不支持始终置顶的模态对话框。
     *
     * @return 如果此窗口支持始终置顶模式且此窗口的工具包支持始终置顶窗口，则返回 {@code true}，否则返回 {@code false}
     *
     * @see #setAlwaysOnTop(boolean)
     * @see #getToolkit
     * @see Toolkit#isAlwaysOnTopSupported
     * @since 1.6
     */
    public boolean isAlwaysOnTopSupported() {
        return Toolkit.getDefaultToolkit().isAlwaysOnTopSupported();
    }


    /**
     * 返回此窗口是否为始终置顶窗口。
     * @return 如果窗口处于始终置顶状态，则返回 {@code true}，否则返回 {@code false}
     * @see #setAlwaysOnTop
     * @since 1.5
     */
    public final boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }


    /**
     * 如果此窗口处于焦点状态，则返回此窗口的子组件中具有焦点的组件；否则返回 null。
     *
     * @return 如果此窗口处于焦点状态，则返回具有焦点的子组件，否则返回 null
     * @see #getMostRecentFocusOwner
     * @see #isFocused
     */
    public Component getFocusOwner() {
        return (isFocused())
            ? KeyboardFocusManager.getCurrentKeyboardFocusManager().
                  getFocusOwner()
            : null;
    }

    /**
     * 返回当此窗口处于焦点状态时将接收焦点的子组件。如果此窗口当前处于焦点状态，此方法返回与 {@code getFocusOwner()} 相同的组件。如果此窗口未处于焦点状态，则返回最近请求焦点的子组件。如果从未有子组件请求过焦点，且此窗口是可焦点窗口，则返回此窗口的初始可焦点组件。如果从未有子组件请求过焦点，且此窗口是非可焦点窗口，则返回 null。
     *
     * @return 当此窗口处于焦点状态时将接收焦点的子组件
     * @see #getFocusOwner
     * @see #isFocused
     * @see #isFocusableWindow
     * @since 1.4
     */
    public Component getMostRecentFocusOwner() {
        if (isFocused()) {
            return getFocusOwner();
        } else {
            Component mostRecent =
                KeyboardFocusManager.getMostRecentFocusOwner(this);
            if (mostRecent != null) {
                return mostRecent;
            } else {
                return (isFocusableWindow())
                    ? getFocusTraversalPolicy().getInitialComponent(this)
                    : null;
            }
        }
    }

    /**
     * 返回此窗口是否处于激活状态。只有 Frame 或 Dialog 可能处于激活状态。本机窗口系统可能会用特殊的装饰（如高亮的标题栏）来表示激活窗口或其子组件。激活窗口总是处于焦点状态的窗口，或者是焦点窗口的所有者中的第一个 Frame 或 Dialog。
     *
     * @return 此窗口是否为激活窗口。
     * @see #isFocused
     * @since 1.4
     */
    public boolean isActive() {
        return (KeyboardFocusManager.getCurrentKeyboardFocusManager().
                getActiveWindow() == this);
    }

    /**
     * 返回此窗口是否处于焦点状态。如果存在焦点所有者，焦点窗口是包含该焦点所有者的窗口。如果没有焦点所有者，则没有窗口处于焦点状态。
     * <p>
     * 如果焦点窗口是 Frame 或 Dialog，则它也是激活窗口。否则，激活窗口是焦点窗口的所有者中的第一个 Frame 或 Dialog。
     *
     * @return 此窗口是否为焦点窗口。
     * @see #isActive
     * @since 1.4
     */
    public boolean isFocused() {
        return (KeyboardFocusManager.getCurrentKeyboardFocusManager().
                getGlobalFocusedWindow() == this);
    }

    /**
     * 获取此窗口的焦点遍历键。（有关每个键的完整描述，请参见 {@code setFocusTraversalKeys}。）
     * <p>
     * 如果未为此窗口显式设置遍历键，则返回此窗口的父窗口的遍历键。如果未为此窗口的任何祖先显式设置遍历键，则返回当前 KeyboardFocusManager 的默认遍历键。
     *
     * @param id 一个 KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS、KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS、KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS 或 KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS
     * @return 指定键的 AWTKeyStroke
     * @see Container#setFocusTraversalKeys
     * @see KeyboardFocusManager#FORWARD_TRAVERSAL_KEYS
     * @see KeyboardFocusManager#BACKWARD_TRAVERSAL_KEYS
     * @see KeyboardFocusManager#UP_CYCLE_TRAVERSAL_KEYS
     * @see KeyboardFocusManager#DOWN_CYCLE_TRAVERSAL_KEYS
     * @throws IllegalArgumentException 如果 id 不是 KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS、KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS、KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS 或 KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS
     * @since 1.4
     */
    @SuppressWarnings("unchecked")
    public Set<AWTKeyStroke> getFocusTraversalKeys(int id) {
        if (id < 0 || id >= KeyboardFocusManager.TRAVERSAL_KEY_LENGTH) {
            throw new IllegalArgumentException("无效的焦点遍历键标识符");
        }

        // 可以直接返回 Set，因为它是一个不可修改的视图
        @SuppressWarnings("rawtypes")
        Set keystrokes = (focusTraversalKeys != null)
            ? focusTraversalKeys[id]
            : null;

        if (keystrokes != null) {
            return keystrokes;
        } else {
            return KeyboardFocusManager.getCurrentKeyboardFocusManager().
                getDefaultFocusTraversalKeys(id);
        }
    }

    /**
     * 什么都不做，因为窗口必须总是焦点遍历周期的根。传递的值被忽略。
     *
     * @param focusCycleRoot 此值被忽略
     * @see #isFocusCycleRoot
     * @see Container#setFocusTraversalPolicy
     * @see Container#getFocusTraversalPolicy
     * @since 1.4
     */
    public final void setFocusCycleRoot(boolean focusCycleRoot) {
    }

    /**
     * 始终返回 {@code true}，因为所有窗口都必须是焦点遍历周期的根。
     *
     * @return {@code true}
     * @see #setFocusCycleRoot
     * @see Container#setFocusTraversalPolicy
     * @see Container#getFocusTraversalPolicy
     * @since 1.4
     */
    public final boolean isFocusCycleRoot() {
        return true;
    }

    /**
     * 始终返回 {@code null}，因为窗口没有祖先；它们代表组件层次结构的顶部。
     *
     * @return {@code null}
     * @see Container#isFocusCycleRoot()
     * @since 1.4
     */
    public final Container getFocusCycleRootAncestor() {
        return null;
    }

    /**
     * 返回此窗口是否可以成为焦点窗口，即此窗口或其任何子组件是否可以成为焦点所有者。对于 Frame 或 Dialog 要成为可焦点窗口，其可焦点窗口状态必须设置为 {@code true}。对于非 Frame 或 Dialog 的窗口要成为可焦点窗口，其可焦点窗口状态必须设置为 {@code true}，其最近的所有者 Frame 或 Dialog 必须在屏幕上显示，并且它必须包含至少一个在其焦点遍历周期中的组件。如果这些条件中的任何一个不满足，则此窗口或其任何子组件都不能成为焦点所有者。
     *
     * @return 如果此窗口可以成为焦点窗口，则返回 {@code true}；否则返回 {@code false}
     * @see #getFocusableWindowState
     * @see #setFocusableWindowState
     * @see #isShowing
     * @see Component#isFocusable
     * @since 1.4
     */
    public final boolean isFocusableWindow() {
        // 如果窗口/Frame/Dialog 被设置为不可焦点，则它始终是不可焦点的。
        if (!getFocusableWindowState()) {
            return false;
        }

        // 其他测试仅适用于窗口。
        if (this instanceof Frame || this instanceof Dialog) {
            return true;
        }

        // 窗口必须在其根焦点遍历周期中至少包含一个组件才能成为可焦点窗口。
        if (getFocusTraversalPolicy().getDefaultComponent(this) == null) {
            return false;
        }

        // 窗口的最近所有者 Frame 或 Dialog 必须在屏幕上显示。
        for (Window owner = getOwner(); owner != null;
             owner = owner.getOwner())
        {
            if (owner instanceof Frame || owner instanceof Dialog) {
                return owner.isShowing();
            }
        }

        return false;
    }

    /**
     * 返回此窗口是否可以在满足 {@code isFocusableWindow} 中概述的其他要求的情况下成为焦点窗口。如果此方法返回 {@code false}，则 {@code isFocusableWindow} 也将返回 {@code false}。如果此方法返回 {@code true}，则 {@code isFocusableWindow} 可能返回 {@code true} 或 {@code false}，具体取决于窗口成为可焦点窗口所需满足的其他要求。
     * <p>
     * 默认情况下，所有窗口的可焦点窗口状态为 {@code true}。
     *
     * @return 此窗口是否可以成为焦点窗口
     * @see #isFocusableWindow
     * @see #setFocusableWindowState
     * @see #isShowing
     * @see Component#setFocusable
     * @since 1.4
     */
    public boolean getFocusableWindowState() {
        return focusableWindowState;
    }

    /**
     * 设置此窗口是否可以在满足 {@code isFocusableWindow} 中概述的其他要求的情况下成为焦点窗口。如果此窗口的可焦点窗口状态设置为 {@code false}，则 {@code isFocusableWindow} 将返回 {@code false}。如果此窗口的可焦点窗口状态设置为 {@code true}，则 {@code isFocusableWindow} 可能返回 {@code true} 或 {@code false}，具体取决于窗口成为可焦点窗口所需满足的其他要求。
     * <p>
     * 将窗口的可焦点状态设置为 {@code false} 是应用程序向 AWT 标识将用作浮动调色板或工具栏的窗口的标准机制，因此应为非可焦点窗口。
     *
     * 在某些平台上，设置可见窗口的可焦点状态可能会有延迟效果——实际的更改可能只有在窗口被隐藏然后再次显示时才会发生。为了确保跨平台的一致行为，当窗口不可见时设置窗口的可焦点状态，然后显示它。
     *
     * @param focusableWindowState 此窗口是否可以成为焦点窗口
     * @see #isFocusableWindow
     * @see #getFocusableWindowState
     * @see #isShowing
     * @see Component#setFocusable
     * @since 1.4
     */
    public void setFocusableWindowState(boolean focusableWindowState) {
        boolean oldFocusableWindowState;
        synchronized (this) {
            oldFocusableWindowState = this.focusableWindowState;
            this.focusableWindowState = focusableWindowState;
        }
        WindowPeer peer = (WindowPeer)this.peer;
        if (peer != null) {
            peer.updateFocusableWindowState();
        }
        firePropertyChange("focusableWindowState", oldFocusableWindowState,
                           focusableWindowState);
        if (oldFocusableWindowState && !focusableWindowState && isFocused()) {
            for (Window owner = getOwner();
                 owner != null;
                 owner = owner.getOwner())
                {
                    Component toFocus =
                        KeyboardFocusManager.getMostRecentFocusOwner(owner);
                    if (toFocus != null && toFocus.requestFocus(false, CausedFocusEvent.Cause.ACTIVATION)) {
                        return;
                    }
                }
            KeyboardFocusManager.getCurrentKeyboardFocusManager().
                clearGlobalFocusOwnerPriv();
        }
    }

    /**
     * 设置此窗口是否应在随后显示（通过调用 {@link #setVisible setVisible(true)}）或移到前面（通过调用 {@link #toFront}）时接收焦点。
     * <p>
     * 注意，{@link #setVisible setVisible(true)} 可能被间接调用（例如，当显示窗口的所有者时，窗口将被显示）。{@link #toFront} 也可能被间接调用（例如，当对已显示的窗口调用 {@link #setVisible setVisible(true)} 时）。在所有这些情况下，此属性同样生效。
     * <p>
     * 该属性的值不会被子窗口继承。
     *
     * @param autoRequestFocus 此窗口是否应在随后显示或移到前面时接收焦点
     * @see #isAutoRequestFocus
     * @see #isFocusableWindow
     * @see #setVisible
     * @see #toFront
     * @since 1.7
     */
    public void setAutoRequestFocus(boolean autoRequestFocus) {
        this.autoRequestFocus = autoRequestFocus;
    }

    /**
     * 返回此窗口是否应在随后显示（通过调用 {@link #setVisible setVisible(true)}）或移到前面（通过调用 {@link #toFront}）时接收焦点。
     * <p>
     * 默认情况下，窗口的 {@code autoRequestFocus} 值为 {@code true}。
     *
     * @return {@code autoRequestFocus} 值
     * @see #setAutoRequestFocus
     * @since 1.7
     */
    public boolean isAutoRequestFocus() {
        return autoRequestFocus;
    }


                /**
     * 将属性更改侦听器添加到侦听器列表中。该侦听器
     * 会注册此类的所有绑定属性，包括以下内容：
     * <ul>
     *    <li>此窗口的字体（"font"）</li>
     *    <li>此窗口的背景颜色（"background"）</li>
     *    <li>此窗口的前景颜色（"foreground"）</li>
     *    <li>此窗口的可聚焦性（"focusable"）</li>
     *    <li>此窗口的焦点遍历键启用状态
     *        （"focusTraversalKeysEnabled"）</li>
     *    <li>此窗口的 FORWARD_TRAVERSAL_KEYS 集
     *        （"forwardFocusTraversalKeys"）</li>
     *    <li>此窗口的 BACKWARD_TRAVERSAL_KEYS 集
     *        （"backwardFocusTraversalKeys"）</li>
     *    <li>此窗口的 UP_CYCLE_TRAVERSAL_KEYS 集
     *        （"upCycleFocusTraversalKeys"）</li>
     *    <li>此窗口的 DOWN_CYCLE_TRAVERSAL_KEYS 集
     *        （"downCycleFocusTraversalKeys"）</li>
     *    <li>此窗口的焦点遍历策略（"focusTraversalPolicy"）
     *        </li>
     *    <li>此窗口的可聚焦窗口状态（"focusableWindowState"）
     *        </li>
     *    <li>此窗口的始终置顶状态（"alwaysOnTop"）</li>
     * </ul>
     * 注意，如果此窗口继承了一个绑定属性，则不会
     * 对继承属性的更改触发事件。
     * <p>
     * 如果侦听器为 null，则不会抛出异常，也不会执行任何操作。
     *
     * @param    listener  要添加的属性更改侦听器
     *
     * @see Component#removePropertyChangeListener
     * @see #addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
    }

    /**
     * 为特定属性添加属性更改侦听器。指定的属性可以是用户定义的，也可以是以下之一：
     * <ul>
     *    <li>此窗口的字体（"font"）</li>
     *    <li>此窗口的背景颜色（"background"）</li>
     *    <li>此窗口的前景颜色（"foreground"）</li>
     *    <li>此窗口的可聚焦性（"focusable"）</li>
     *    <li>此窗口的焦点遍历键启用状态
     *        （"focusTraversalKeysEnabled"）</li>
     *    <li>此窗口的 FORWARD_TRAVERSAL_KEYS 集
     *        （"forwardFocusTraversalKeys"）</li>
     *    <li>此窗口的 BACKWARD_TRAVERSAL_KEYS 集
     *        （"backwardFocusTraversalKeys"）</li>
     *    <li>此窗口的 UP_CYCLE_TRAVERSAL_KEYS 集
     *        （"upCycleFocusTraversalKeys"）</li>
     *    <li>此窗口的 DOWN_CYCLE_TRAVERSAL_KEYS 集
     *        （"downCycleFocusTraversalKeys"）</li>
     *    <li>此窗口的焦点遍历策略（"focusTraversalPolicy"）
     *        </li>
     *    <li>此窗口的可聚焦窗口状态（"focusableWindowState"）
     *        </li>
     *    <li>此窗口的始终置顶状态（"alwaysOnTop"）</li>
     * </ul>
     * 注意，如果此窗口继承了一个绑定属性，则不会
     * 对继承属性的更改触发事件。
     * <p>
     * 如果侦听器为 null，则不会抛出异常，也不会执行任何操作。
     *
     * @param propertyName 上述之一的属性名称
     * @param listener 要添加的属性更改侦听器
     *
     * @see #addPropertyChangeListener(java.beans.PropertyChangeListener)
     * @see Component#removePropertyChangeListener
     */
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener listener) {
        super.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * 指示此容器是否为验证根。
     * <p>
     * {@code Window} 对象是验证根，因此，它们
     * 重写此方法以返回 {@code true}。
     *
     * @return {@code true}
     * @since 1.7
     * @see java.awt.Container#isValidateRoot
     */
    @Override
    public boolean isValidateRoot() {
        return true;
    }

    /**
     * 将事件分派给此窗口或其子组件之一。
     * @param e 事件
     */
    void dispatchEventImpl(AWTEvent e) {
        if (e.getID() == ComponentEvent.COMPONENT_RESIZED) {
            invalidate();
            validate();
        }
        super.dispatchEventImpl(e);
    }

    /**
     * @deprecated 自 JDK 1.1 版本起
     * 替换为 {@code dispatchEvent(AWTEvent)}。
     */
    @Deprecated
    public boolean postEvent(Event e) {
        if (handleEvent(e)) {
            e.consume();
            return true;
        }
        return false;
    }

    /**
     * 检查此窗口是否显示在屏幕上。
     * @see Component#setVisible
    */
    public boolean isShowing() {
        return visible;
    }

    boolean isDisposing() {
        return disposing;
    }

    /**
     * @deprecated 自 J2SE 1.4 版本起，替换为
     * {@link Component#applyComponentOrientation Component.applyComponentOrientation}。
     */
    @Deprecated
    public void applyResourceBundle(ResourceBundle rb) {
        applyComponentOrientation(ComponentOrientation.getOrientation(rb));
    }

    /**
     * @deprecated 自 J2SE 1.4 版本起，替换为
     * {@link Component#applyComponentOrientation Component.applyComponentOrientation}。
     */
    @Deprecated
    public void applyResourceBundle(String rbName) {
        applyResourceBundle(ResourceBundle.getBundle(rbName,
                                Locale.getDefault(),
                                ClassLoader.getSystemClassLoader()));
    }

   /*
    * 支持跟踪此窗口拥有的所有窗口
    */
    void addOwnedWindow(WeakReference<Window> weakWindow) {
        if (weakWindow != null) {
            synchronized(ownedWindowList) {
                // 此 if 语句实际上应该是断言，但我们没有断言...
                if (!ownedWindowList.contains(weakWindow)) {
                    ownedWindowList.addElement(weakWindow);
                }
            }
        }
    }

    void removeOwnedWindow(WeakReference<Window> weakWindow) {
        if (weakWindow != null) {
            // 同步块不是必需的，因为 removeElement 已经是同步的
            ownedWindowList.removeElement(weakWindow);
        }
    }

    void connectOwnedWindow(Window child) {
        child.parent = this;
        addOwnedWindow(child.weakThis);
        child.disposerRecord.updateOwner();
    }

    private void addToWindowList() {
        synchronized (Window.class) {
            @SuppressWarnings("unchecked")
            Vector<WeakReference<Window>> windowList = (Vector<WeakReference<Window>>)appContext.get(Window.class);
            if (windowList == null) {
                windowList = new Vector<WeakReference<Window>>();
                appContext.put(Window.class, windowList);
            }
            windowList.add(weakThis);
        }
    }

    private static void removeFromWindowList(AppContext context, WeakReference<Window> weakThis) {
        synchronized (Window.class) {
            @SuppressWarnings("unchecked")
            Vector<WeakReference<Window>> windowList = (Vector<WeakReference<Window>>)context.get(Window.class);
            if (windowList != null) {
                windowList.remove(weakThis);
            }
        }
    }

    private void removeFromWindowList() {
        removeFromWindowList(appContext, weakThis);
    }

    /**
     * 窗口类型。
     *
     * 同步：ObjectLock
     */
    private Type type = Type.NORMAL;

    /**
     * 设置窗口的类型。
     *
     * 仅当窗口不可显示时，才能调用此方法。
     *
     * @throws IllegalComponentStateException 如果窗口
     *         可显示。
     * @throws IllegalArgumentException 如果类型为 {@code null}
     * @see    Component#isDisplayable
     * @see    #getType
     * @since 1.7
     */
    public void setType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type should not be null.");
        }
        synchronized (getTreeLock()) {
            if (isDisplayable()) {
                throw new IllegalComponentStateException(
                        "The window is displayable.");
            }
            synchronized (getObjectLock()) {
                this.type = type;
            }
        }
    }

    /**
     * 返回窗口的类型。
     *
     * @see   #setType
     * @since 1.7
     */
    public Type getType() {
        synchronized (getObjectLock()) {
            return type;
        }
    }

    /**
     * 窗口序列化数据版本。
     *
     * @serial
     */
    private int windowSerializedDataVersion = 2;

    /**
     * 将默认的可序列化字段写入流。写入
     * 可选数据的 {@code WindowListener} 和
     * {@code WindowFocusListener} 列表。
     * 写入子窗口列表作为可选数据。
     * 写入图标图像列表作为可选数据
     *
     * @param s 要写入的 {@code ObjectOutputStream}
     * @serialData 以 {@code null} 结尾的
     *    0 个或更多对的序列；每对由一个 {@code String}
     *    和一个 {@code Object} 组成；该 {@code String}
     *    表示对象的类型，可以是以下之一：
     *    {@code windowListenerK} 表示一个
     *      {@code WindowListener} 对象；
     *    {@code windowFocusWindowK} 表示一个
     *      {@code WindowFocusListener} 对象；
     *    {@code ownedWindowK} 表示一个子
     *      {@code Window} 对象
     *
     * @see AWTEventMulticaster#save(java.io.ObjectOutputStream, java.lang.String, java.util.EventListener)
     * @see Component#windowListenerK
     * @see Component#windowFocusListenerK
     * @see Component#ownedWindowK
     * @see #readObject(ObjectInputStream)
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        synchronized (this) {
            // 更新旧的 focusMgr 字段，以便我们的对象流可以被早期版本读取
            focusMgr = new FocusManager();
            focusMgr.focusRoot = this;
            focusMgr.focusOwner = getMostRecentFocusOwner();

            s.defaultWriteObject();

            // 清除字段，以便我们不会保留额外的引用
            focusMgr = null;

            AWTEventMulticaster.save(s, windowListenerK, windowListener);
            AWTEventMulticaster.save(s, windowFocusListenerK, windowFocusListener);
            AWTEventMulticaster.save(s, windowStateListenerK, windowStateListener);
        }

        s.writeObject(null);

        synchronized (ownedWindowList) {
            for (int i = 0; i < ownedWindowList.size(); i++) {
                Window child = ownedWindowList.elementAt(i).get();
                if (child != null) {
                    s.writeObject(ownedWindowK);
                    s.writeObject(child);
                }
            }
        }
        s.writeObject(null);

        // 写入图标数组
        if (icons != null) {
            for (Image i : icons) {
                if (i instanceof Serializable) {
                    s.writeObject(i);
                }
            }
        }
        s.writeObject(null);
    }

    //
    // 反序列化过程的一部分，应在
    // 用户代码之前调用。
    //
    private void initDeserializedWindow() {
        setWarningString();
        inputContextLock = new Object();

        // 反序列化的窗口尚未可见。
        visible = false;

        weakThis = new WeakReference<>(this);

        anchor = new Object();
        disposerRecord = new WindowDisposerRecord(appContext, this);
        sun.java2d.Disposer.addRecord(anchor, disposerRecord);

        addToWindowList();
        initGC(null);
        ownedWindowList = new Vector<>();
    }

    private void deserializeResources(ObjectInputStream s)
        throws ClassNotFoundException, IOException, HeadlessException {

            if (windowSerializedDataVersion < 2) {
                // 将旧的焦点跟踪转换为新模型。对于 1.4 及更高版本，
                // 我们将依赖于窗口的初始可聚焦组件。
                if (focusMgr != null) {
                    if (focusMgr.focusOwner != null) {
                        KeyboardFocusManager.
                            setMostRecentFocusOwner(this, focusMgr.focusOwner);
                    }
                }

                // 该字段是非瞬态的，依赖于默认序列化。
                // 但是，默认值是不够的，因此我们需要为 1.4 之前的对象数据流显式设置它。
                focusableWindowState = true;


            }

        Object keyOrNull;
        while(null != (keyOrNull = s.readObject())) {
            String key = ((String)keyOrNull).intern();

            if (windowListenerK == key) {
                addWindowListener((WindowListener)(s.readObject()));
            } else if (windowFocusListenerK == key) {
                addWindowFocusListener((WindowFocusListener)(s.readObject()));
            } else if (windowStateListenerK == key) {
                addWindowStateListener((WindowStateListener)(s.readObject()));
            } else // 跳过未识别键的值
                s.readObject();
        }

        try {
            while (null != (keyOrNull = s.readObject())) {
                String key = ((String)keyOrNull).intern();

                if (ownedWindowK == key)
                    connectOwnedWindow((Window) s.readObject());

                else // 跳过未识别键的值
                    s.readObject();
            }

            // 读取图标
            Object obj = s.readObject(); // 抛出 OptionalDataException
                                         // 对于 1.6 之前的对象。
            icons = new ArrayList<Image>(); // Frame.readObject() 假设
                                            // 如果 icons 为 null，则为 1.6 之前的版本。
            while (obj != null) {
                if (obj instanceof Image) {
                    icons.add((Image)obj);
                }
                obj = s.readObject();
            }
        }
        catch (OptionalDataException e) {
            // 1.1 序列化形式
            // ownedWindowList 将由 Frame.readObject 更新
        }

    }

    /**
     * 读取 {@code ObjectInputStream} 和一个可选的
     * 监听器列表以接收组件触发的各种事件；还读取一个
     * （可能为 {@code null}）子窗口列表。
     * 未识别的键或值将被忽略。
     *
     * @param s 要读取的 {@code ObjectInputStream}
     * @exception HeadlessException 如果
     *   {@code GraphicsEnvironment.isHeadless} 返回
     *   {@code true}
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see #writeObject
     */
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException
    {
         GraphicsEnvironment.checkHeadless();
         initDeserializedWindow();
         ObjectInputStream.GetField f = s.readFields();


                     syncLWRequests = f.get("syncLWRequests", systemSyncLWRequests);
         state = f.get("state", 0);
         focusableWindowState = f.get("focusableWindowState", true);
         windowSerializedDataVersion = f.get("windowSerializedDataVersion", 1);
         locationByPlatform = f.get("locationByPlatform", locationByPlatformProp);
         // 注意：1.4（或更高版本）不使用 focusMgr
         focusMgr = (FocusManager)f.get("focusMgr", null);
         Dialog.ModalExclusionType et = (Dialog.ModalExclusionType)
             f.get("modalExclusionType", Dialog.ModalExclusionType.NO_EXCLUDE);
         setModalExclusionType(et); // 自 6.0 起
         boolean aot = f.get("alwaysOnTop", false);
         if(aot) {
             setAlwaysOnTop(aot); // 自 1.5 起；受权限检查影响
         }
         shape = (Shape)f.get("shape", null);
         opacity = (Float)f.get("opacity", 1.0f);

         this.securityWarningWidth = 0;
         this.securityWarningHeight = 0;
         this.securityWarningPointX = 2.0;
         this.securityWarningPointY = 0.0;
         this.securityWarningAlignmentX = RIGHT_ALIGNMENT;
         this.securityWarningAlignmentY = TOP_ALIGNMENT;

         deserializeResources(s);
    }

    /*
     * --- Accessibility Support ---
     *
     */

    /**
     * 获取与此 Window 关联的 AccessibleContext。
     * 对于窗口，AccessibleContext 的形式为 AccessibleAWTWindow。
     * 如果必要，会创建一个新的 AccessibleAWTWindow 实例。
     *
     * @return 一个 AccessibleAWTWindow，作为此窗口的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTWindow();
        }
        return accessibleContext;
    }

    /**
     * 为 {@code Window} 类实现辅助功能支持。
     * 它提供了适用于窗口用户界面元素的 Java 辅助功能 API 的实现。
     * @since 1.3
     */
    protected class AccessibleAWTWindow extends AccessibleAWTContainer
    {
        /*
         * JDK 1.3 serialVersionUID
         */
        private static final long serialVersionUID = 4215068635060671780L;

        /**
         * 获取此对象的角色。
         *
         * @return 一个 AccessibleRole 实例，描述对象的角色
         * @see javax.accessibility.AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.WINDOW;
        }

        /**
         * 获取此对象的状态。
         *
         * @return 一个 AccessibleStateSet 实例，包含对象的当前状态集
         * @see javax.accessibility.AccessibleState
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (getFocusOwner() != null) {
                states.add(AccessibleState.ACTIVE);
            }
            return states;
        }

    } // inner class AccessibleAWTWindow

    @Override
    void setGraphicsConfiguration(GraphicsConfiguration gc) {
        if (gc == null) {
            gc = GraphicsEnvironment.
                    getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().
                    getDefaultConfiguration();
        }
        synchronized (getTreeLock()) {
            super.setGraphicsConfiguration(gc);
            if (log.isLoggable(PlatformLogger.Level.FINER)) {
                log.finer("+ Window.setGraphicsConfiguration(): new GC is \n+ " + getGraphicsConfiguration_NoClientCode() + "\n+ this is " + this);
            }
        }
    }

    /**
     * 根据以下场景设置窗口相对于指定组件的位置。
     * <p>
     * 下面提到的目标屏幕是指在调用 setLocationRelativeTo 方法后窗口应放置的屏幕。
     * <ul>
     * <li>如果组件为 {@code null}，或与此组件关联的 {@code
     * GraphicsConfiguration} 为 {@code null}，则窗口将放置在屏幕中心。可以通过 {@link
     * GraphicsEnvironment#getCenterPoint
     * GraphicsEnvironment.getCenterPoint} 方法获取中心点。
     * <li>如果组件不为 {@code null}，但当前未显示，则窗口将放置在由与此组件关联的 {@code
     * GraphicsConfiguration} 定义的目标屏幕的中心。
     * <li>如果组件不为 {@code null} 且显示在屏幕上，则窗口将定位在组件的中心。
     * </ul>
     * <p>
     * 如果屏幕配置不允许将窗口从一个屏幕移动到另一个屏幕，则窗口仅根据上述条件放置，其 {@code GraphicsConfiguration} 不会改变。
     * <p>
     * <b>注意</b>：如果窗口的下边缘超出屏幕，则窗口将放置在 {@code Component}
     * 的靠近屏幕中心的一侧。因此，如果组件位于屏幕的右侧，窗口将放置在其左侧，反之亦然。
     * <p>
     * 如果在计算窗口位置后，窗口的上、左或右边缘超出屏幕，则窗口将定位在屏幕的相应边缘。如果窗口的左、右边缘都超出屏幕，则窗口将放置在屏幕的左侧。如果顶部和底部边缘都超出屏幕，则窗口将放置在屏幕的顶部。
     * <p>
     * 该方法更改了几何相关数据。因此，本机窗口系统可能会忽略此类请求，或修改请求的数据，使 {@code Window} 对象的放置和大小与桌面设置接近。
     *
     * @param c 用于确定窗口位置的组件
     * @see java.awt.GraphicsEnvironment#getCenterPoint
     * @since 1.4
     */
    public void setLocationRelativeTo(Component c) {
        // 目标位置
        int dx = 0, dy = 0;
        // 目标 GC
        GraphicsConfiguration gc = getGraphicsConfiguration_NoClientCode();
        Rectangle gcBounds = gc.getBounds();

        Dimension windowSize = getSize();

        // 查找 c 的顶级窗口
        Window componentWindow = SunToolkit.getContainingWindow(c);
        if ((c == null) || (componentWindow == null)) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
            gcBounds = gc.getBounds();
            Point centerPoint = ge.getCenterPoint();
            dx = centerPoint.x - windowSize.width / 2;
            dy = centerPoint.y - windowSize.height / 2;
        } else if (!c.isShowing()) {
            gc = componentWindow.getGraphicsConfiguration();
            gcBounds = gc.getBounds();
            dx = gcBounds.x + (gcBounds.width - windowSize.width) / 2;
            dy = gcBounds.y + (gcBounds.height - windowSize.height) / 2;
        } else {
            gc = componentWindow.getGraphicsConfiguration();
            gcBounds = gc.getBounds();
            Dimension compSize = c.getSize();
            Point compLocation = c.getLocationOnScreen();
            dx = compLocation.x + ((compSize.width - windowSize.width) / 2);
            dy = compLocation.y + ((compSize.height - windowSize.height) / 2);

            // 调整下边缘超出屏幕的情况
            if (dy + windowSize.height > gcBounds.y + gcBounds.height) {
                dy = gcBounds.y + gcBounds.height - windowSize.height;
                if (compLocation.x - gcBounds.x + compSize.width / 2 < gcBounds.width / 2) {
                    dx = compLocation.x + compSize.width;
                } else {
                    dx = compLocation.x - windowSize.width;
                }
            }
        }

        // 避免放置在屏幕边缘之外：
        // 底部
        if (dy + windowSize.height > gcBounds.y + gcBounds.height) {
            dy = gcBounds.y + gcBounds.height - windowSize.height;
        }
        // 顶部
        if (dy < gcBounds.y) {
            dy = gcBounds.y;
        }
        // 右侧
        if (dx + windowSize.width > gcBounds.x + gcBounds.width) {
            dx = gcBounds.x + gcBounds.width - windowSize.width;
        }
        // 左侧
        if (dx < gcBounds.x) {
            dx = gcBounds.x;
        }

        setLocation(dx, dy);
    }

    /**
     * 重写自 Component。顶级窗口不应将 MouseWheelEvent 传播到其拥有窗口。
     */
    void deliverMouseWheelToAncestor(MouseWheelEvent e) {}

    /**
     * 重写自 Component。顶级窗口不向祖先分派事件
     */
    boolean dispatchMouseWheelToAncestor(MouseWheelEvent e) {return false;}

    /**
     * 为此组件创建多缓冲策略。多缓冲对渲染性能很有用。此方法尝试使用提供的缓冲区数量创建最佳策略。它将始终创建具有该数量缓冲区的 {@code BufferStrategy}。
     * 首先尝试创建页面翻转策略，然后尝试使用加速缓冲区的混合策略。最后，使用非加速混合策略。
     * <p>
     * 每次调用此方法时，都会丢弃此组件的现有缓冲策略。
     * @param numBuffers 要创建的缓冲区数量
     * @exception IllegalArgumentException 如果 numBuffers 小于 1。
     * @exception IllegalStateException 如果组件不可显示
     * @see #isDisplayable
     * @see #getBufferStrategy
     * @since 1.4
     */
    public void createBufferStrategy(int numBuffers) {
        super.createBufferStrategy(numBuffers);
    }

    /**
     * 为此组件创建具有所需缓冲区功能的多缓冲策略。例如，如果仅需要加速内存或页面翻转（由缓冲区功能指定）。
     * <p>
     * 每次调用此方法时，都会丢弃此组件的现有缓冲策略。
     * @param numBuffers 要创建的缓冲区数量，包括前台缓冲区
     * @param caps 创建缓冲策略所需的缓冲区功能；不能为 {@code null}
     * @exception AWTException 如果提供的功能无法支持或满足；例如，如果当前没有足够的加速内存，或者指定了页面翻转但不可行。
     * @exception IllegalArgumentException 如果 numBuffers 小于 1，或 caps 为 {@code null}
     * @see #getBufferStrategy
     * @since 1.4
     */
    public void createBufferStrategy(int numBuffers,
        BufferCapabilities caps) throws AWTException {
        super.createBufferStrategy(numBuffers, caps);
    }

    /**
     * 返回此组件使用的 {@code BufferStrategy}。如果尚未创建或已释放 {@code BufferStrategy}，此方法将返回 null。
     *
     * @return 此组件使用的缓冲策略
     * @see #createBufferStrategy
     * @since 1.4
     */
    public BufferStrategy getBufferStrategy() {
        return super.getBufferStrategy();
    }

    Component getTemporaryLostComponent() {
        return temporaryLostComponent;
    }
    Component setTemporaryLostComponent(Component component) {
        Component previousComp = temporaryLostComponent;
        // 检查 "component" 是否是可接受的焦点所有者，如果不是则不存储
        // - 否则在处理 WINDOW_GAINED_FOCUS 时会遇到问题
        if (component == null || component.canBeFocusOwner()) {
            temporaryLostComponent = component;
        } else {
            temporaryLostComponent = null;
        }
        return previousComp;
    }

    /**
     * 检查此窗口是否可以包含焦点所有者。
     * 验证它是否可聚焦，并且作为容器是否可以包含焦点所有者。
     * @since 1.5
     */
    boolean canContainFocusOwner(Component focusOwnerCandidate) {
        return super.canContainFocusOwner(focusOwnerCandidate) && isFocusableWindow();
    }

    private volatile boolean locationByPlatform = locationByPlatformProp;


    /**
     * 设置此窗口是否应在下一次显示时出现在本机窗口系统的默认位置或当前位置（由
     * {@code getLocation} 返回）。此行为类似于未通过编程方式设置位置的本机窗口。大多数窗口系统会级联未显式设置位置的窗口。实际位置在窗口显示在屏幕上时确定。
     * <p>
     * 也可以通过将系统属性 "java.awt.Window.locationByPlatform" 设置为 "true" 来启用此行为，但对此方法的调用优先。
     * <p>
     * 在调用 {@code setLocationByPlatform} 之后调用 {@code setVisible}、{@code setLocation} 和
     * {@code setBounds} 会清除此窗口的此属性。
     * <p>
     * 例如，执行以下代码后：
     * <pre>
     * setLocationByPlatform(true);
     * setVisible(true);
     * boolean flag = isLocationByPlatform();
     * </pre>
     * 窗口将显示在平台的默认位置，且 {@code flag} 为 {@code false}。
     * <p>
     * 在以下示例中：
     * <pre>
     * setLocationByPlatform(true);
     * setLocation(10, 10);
     * boolean flag = isLocationByPlatform();
     * setVisible(true);
     * </pre>
     * 窗口将显示在 (10, 10)，且 {@code flag} 为 {@code false}。
     *
     * @param locationByPlatform 如果此窗口应出现在默认位置，则为 {@code true}，否则为 {@code false}
     * @throws IllegalComponentStateException 如果窗口正在屏幕上显示且 locationByPlatform 为 {@code true}。
     * @see #setLocation
     * @see #isShowing
     * @see #setVisible
     * @see #isLocationByPlatform
     * @see java.lang.System#getProperty(String)
     * @since 1.5
     */
    public void setLocationByPlatform(boolean locationByPlatform) {
        synchronized (getTreeLock()) {
            if (locationByPlatform && isShowing()) {
                throw new IllegalComponentStateException("The window is showing on screen.");
            }
            this.locationByPlatform = locationByPlatform;
        }
    }


                /**
     * 如果此窗口下次可见时将出现在本机窗口系统的默认位置，则返回 {@code true}。
     * 如果窗口当前显示在屏幕上，则此方法始终返回 {@code false}。
     *
     * @return 此窗口是否将出现在默认位置
     * @see #setLocationByPlatform
     * @see #isShowing
     * @since 1.5
     */
    public boolean isLocationByPlatform() {
        return locationByPlatform;
    }

    /**
     * {@inheritDoc}
     * <p>
     * 如果 {@code width} 或 {@code height} 的值小于通过调用 {@code setMinimumSize} 设置的最小尺寸，
     * 则会自动增大这些值。
     * <p>
     * 该方法更改与几何相关的数据。因此，本机窗口系统可能会忽略此类请求，或者修改请求的数据，
     * 使 {@code Window} 对象的放置和大小与桌面设置紧密对应。
     *
     * @see #getBounds
     * @see #setLocation(int, int)
     * @see #setLocation(Point)
     * @see #setSize(int, int)
     * @see #setSize(Dimension)
     * @see #setMinimumSize
     * @see #setLocationByPlatform
     * @see #isLocationByPlatform
     * @since 1.6
     */
    public void setBounds(int x, int y, int width, int height) {
        synchronized (getTreeLock()) {
            if (getBoundsOp() == ComponentPeer.SET_LOCATION ||
                getBoundsOp() == ComponentPeer.SET_BOUNDS)
            {
                locationByPlatform = false;
            }
            super.setBounds(x, y, width, height);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * 如果 {@code r.width} 或 {@code r.height} 的值小于通过调用 {@code setMinimumSize} 设置的最小尺寸，
     * 则会自动增大这些值。
     * <p>
     * 该方法更改与几何相关的数据。因此，本机窗口系统可能会忽略此类请求，或者修改请求的数据，
     * 使 {@code Window} 对象的放置和大小与桌面设置紧密对应。
     *
     * @see #getBounds
     * @see #setLocation(int, int)
     * @see #setLocation(Point)
     * @see #setSize(int, int)
     * @see #setSize(Dimension)
     * @see #setMinimumSize
     * @see #setLocationByPlatform
     * @see #isLocationByPlatform
     * @since 1.6
     */
    public void setBounds(Rectangle r) {
        setBounds(r.x, r.y, r.width, r.height);
    }

    /**
     * 确定此组件是否将显示在屏幕上。
     * @return 如果组件及其所有祖先直到顶级窗口都是可见的，则返回 {@code true}，否则返回 {@code false}
     */
    boolean isRecursivelyVisible() {
        // 5079694 fix: 顶级窗口显示时，其父级不必可见。
        // 我们重写 isRecursivelyVisible 以实现此策略。
        return visible;
    }


    // ******************** SHAPES & TRANSPARENCY CODE ********************

    /**
     * 返回窗口的不透明度。
     *
     * @return 窗口的不透明度
     *
     * @see Window#setOpacity(float)
     * @see GraphicsDevice.WindowTranslucency
     *
     * @since 1.7
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * 设置窗口的不透明度。
     * <p>
     * 不透明度值在 [0..1] 范围内。请注意，将不透明度设置为 0 可能会禁用此窗口上的鼠标事件处理。这是平台依赖的行为。
     * <p>
     * 为了将不透明度值设置为小于 {@code 1.0f}，必须满足以下条件：
     * <ul>
     * <li>底层系统必须支持 {@link GraphicsDevice.WindowTranslucency#TRANSLUCENT TRANSLUCENT} 透明度
     * <li>窗口必须是无边框的（参见 {@link Frame#setUndecorated} 和 {@link Dialog#setUndecorated}）
     * <li>窗口不得处于全屏模式（参见 {@link GraphicsDevice#setFullScreenWindow(Window)}）
     * </ul>
     * <p>
     * 如果请求的不透明度值小于 {@code 1.0f}，且上述任何条件未满足，窗口的不透明度将不会改变，
     * 并将抛出 {@code IllegalComponentStateException}。
     * <p>
     * 个别像素的透明度级别也可能受其颜色的 alpha 组件（参见 {@link Window#setBackground(Color)}) 和
     * 此窗口的当前形状（参见 {@link #setShape(Shape)}）的影响。
     *
     * @param opacity 要设置给窗口的不透明度级别
     *
     * @throws IllegalArgumentException 如果不透明度超出 [0..1] 范围
     * @throws IllegalComponentStateException 如果窗口已装饰且不透明度小于 {@code 1.0f}
     * @throws IllegalComponentStateException 如果窗口处于全屏模式且不透明度小于 {@code 1.0f}
     * @throws UnsupportedOperationException 如果 {@code
     *     GraphicsDevice.WindowTranslucency#TRANSLUCENT TRANSLUCENT}
     *     透明度不受支持且不透明度小于 {@code 1.0f}
     *
     * @see Window#getOpacity
     * @see Window#setBackground(Color)
     * @see Window#setShape(Shape)
     * @see Frame#isUndecorated
     * @see Dialog#isUndecorated
     * @see GraphicsDevice.WindowTranslucency
     * @see GraphicsDevice#isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency)
     *
     * @since 1.7
     */
    public void setOpacity(float opacity) {
        synchronized (getTreeLock()) {
            if (opacity < 0.0f || opacity > 1.0f) {
                throw new IllegalArgumentException(
                    "不透明度的值应在 [0.0f .. 1.0f] 范围内。");
            }
            if (opacity < 1.0f) {
                GraphicsConfiguration gc = getGraphicsConfiguration();
                GraphicsDevice gd = gc.getDevice();
                if (gc.getDevice().getFullScreenWindow() == this) {
                    throw new IllegalComponentStateException(
                        "为全屏窗口设置不透明度不受支持。");
                }
                if (!gd.isWindowTranslucencySupported(
                    GraphicsDevice.WindowTranslucency.TRANSLUCENT))
                {
                    throw new UnsupportedOperationException(
                        "TRANSLUCENT 透明度不受支持。");
                }
            }
            this.opacity = opacity;
            WindowPeer peer = (WindowPeer)getPeer();
            if (peer != null) {
                peer.setOpacity(opacity);
            }
        }
    }

    /**
     * 返回窗口的形状。
     *
     * 该方法返回的值可能与使用 {@code setShape(shape)} 设置的值不同，但保证表示相同的形状。
     *
     * @return 窗口的形状，如果未为窗口指定形状，则返回 {@code null}
     *
     * @see Window#setShape(Shape)
     * @see GraphicsDevice.WindowTranslucency
     *
     * @since 1.7
     */
    public Shape getShape() {
        synchronized (getTreeLock()) {
            return shape == null ? null : new Path2D.Float(shape);
        }
    }

    /**
     * 设置窗口的形状。
     * <p>
     * 设置形状会裁剪窗口的某些部分。只有属于给定 {@link Shape} 的部分仍然可见和可点击。如果
     * 形状参数为 {@code null}，此方法将恢复默认形状，使窗口在大多数平台上呈矩形。
     * <p>
     * 为了设置非空形状，必须满足以下条件：
     * <ul>
     * <li>底层系统必须支持 {@link GraphicsDevice.WindowTranslucency#PERPIXEL_TRANSPARENT
     * PERPIXEL_TRANSPARENT} 透明度
     * <li>窗口必须是无边框的（参见 {@link Frame#setUndecorated}
     * 和 {@link Dialog#setUndecorated}）
     * <li>窗口不得处于全屏模式（参见 {@link
     * GraphicsDevice#setFullScreenWindow(Window)}）
     * </ul>
     * <p>
     * 如果请求的形状不是 {@code null}，且上述任何条件未满足，此窗口的形状将不会改变，
     * 并将抛出 {@code UnsupportedOperationException} 或 {@code
     * IllegalComponentStateException}。
     * <p>
     * 个别像素的透明度级别也可能受其颜色的 alpha 组件（参见 {@link Window#setBackground(Color)}) 和
     * 不透明度值（参见 {@link #setOpacity(float)}）的影响。请参阅 {@link
     * GraphicsDevice.WindowTranslucency} 以获取更多详细信息。
     *
     * @param shape 要设置给窗口的形状
     *
     * @throws IllegalComponentStateException 如果形状不是 {@code
     *     null} 且窗口已装饰
     * @throws IllegalComponentStateException 如果形状不是 {@code
     *     null} 且窗口处于全屏模式
     * @throws UnsupportedOperationException 如果形状不是 {@code
     *     null} 且 {@link GraphicsDevice.WindowTranslucency#PERPIXEL_TRANSPARENT
     *     PERPIXEL_TRANSPARENT} 透明度不受支持
     *
     * @see Window#getShape()
     * @see Window#setBackground(Color)
     * @see Window#setOpacity(float)
     * @see Frame#isUndecorated
     * @see Dialog#isUndecorated
     * @see GraphicsDevice.WindowTranslucency
     * @see GraphicsDevice#isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency)
     *
     * @since 1.7
     */
    public void setShape(Shape shape) {
        synchronized (getTreeLock()) {
            if (shape != null) {
                GraphicsConfiguration gc = getGraphicsConfiguration();
                GraphicsDevice gd = gc.getDevice();
                if (gc.getDevice().getFullScreenWindow() == this) {
                    throw new IllegalComponentStateException(
                        "为全屏窗口设置形状不受支持。");
                }
                if (!gd.isWindowTranslucencySupported(
                        GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT))
                {
                    throw new UnsupportedOperationException(
                        "PERPIXEL_TRANSPARENT 透明度不受支持。");
                }
            }
            this.shape = (shape == null) ? null : new Path2D.Float(shape);
            WindowPeer peer = (WindowPeer)getPeer();
            if (peer != null) {
                peer.applyShape(shape == null ? null : Region.getInstance(shape, null));
            }
        }
    }

    /**
     * 获取此窗口的背景颜色。
     * <p>
     * 请注意，返回颜色的 alpha 组件指示窗口是否处于非不透明（每像素透明）模式。
     *
     * @return 此组件的背景颜色
     *
     * @see Window#setBackground(Color)
     * @see Window#isOpaque
     * @see GraphicsDevice.WindowTranslucency
     */
    @Override
    public Color getBackground() {
        return super.getBackground();
    }

    /**
     * 设置此窗口的背景颜色。
     * <p>
     * 如果窗口系统支持 {@link
     * GraphicsDevice.WindowTranslucency#PERPIXEL_TRANSLUCENT PERPIXEL_TRANSLUCENT}
     * 透明度，给定背景颜色的 alpha 组件可能会影响此窗口的操作模式：它指示此窗口是否应为不透明（alpha 等于 {@code 1.0f}）或每像素透明
     * （alpha 小于 {@code 1.0f}）。如果给定的背景颜色为
     * {@code null}，则认为窗口完全不透明。
     * <p>
     * 为了启用此窗口的每像素透明模式，必须满足以下所有条件：
     * <ul>
     * <li>此窗口所在的图形设备必须支持 {@link GraphicsDevice.WindowTranslucency#PERPIXEL_TRANSLUCENT
     * PERPIXEL_TRANSLUCENT} 透明度
     * <li>窗口必须是无边框的（参见 {@link Frame#setUndecorated}
     * 和 {@link Dialog#setUndecorated}）
     * <li>窗口不得处于全屏模式（参见 {@link
     * GraphicsDevice#setFullScreenWindow(Window)}）
     * </ul>
     * <p>
     * 如果请求的背景颜色的 alpha 组件小于
     * {@code 1.0f}，且上述任何条件未满足，此窗口的背景颜色将不会改变，给定背景颜色的 alpha 组件不会影响此窗口的操作模式，
     * 并将抛出 {@code UnsupportedOperationException} 或 {@code
     * IllegalComponentStateException}。
     * <p>
     * 当窗口每像素透明时，绘图子系统会尊重每个像素的 alpha 值。如果像素被绘制为 alpha 颜色组件等于零，则该像素将变得视觉上透明。如果像素的 alpha 值等于 1.0f，则该像素完全不透明。alpha 颜色组件的中间值使像素半透明。在这种模式下，窗口的背景将使用给定背景颜色的 alpha 值进行绘制。如果此方法的参数的 alpha 值等于 {@code 0}，则背景将完全不绘制。
     * <p>
     * 给定像素的实际透明度级别还取决于窗口的不透明度（参见 {@link #setOpacity(float)}），以及此窗口的当前形状（参见 {@link #setShape(Shape)}）。
     * <p>
     * 请注意，绘制 alpha 值为 {@code 0} 的像素可能会或可能不会禁用该像素上的鼠标事件处理。这是平台依赖的行为。要确保鼠标事件不会分发到特定像素，必须将该像素排除在窗口的形状之外。
     * <p>
     * 启用每像素透明模式可能会由于本机平台的要求而更改此窗口的图形配置。
     *
     * @param bgColor 要成为此窗口背景颜色的颜色。
     *
     * @throws IllegalComponentStateException 如果给定背景颜色的 alpha 值小于 {@code 1.0f} 且窗口已装饰
     * @throws IllegalComponentStateException 如果给定背景颜色的 alpha 值小于 {@code 1.0f} 且窗口处于
     *     全屏模式
     * @throws UnsupportedOperationException 如果给定背景颜色的 alpha 值小于 {@code 1.0f} 且 {@link
     *     GraphicsDevice.WindowTranslucency#PERPIXEL_TRANSLUCENT
     *     PERPIXEL_TRANSLUCENT} 透明度不受支持
     *
     * @see Window#getBackground
     * @see Window#isOpaque
     * @see Window#setOpacity(float)
     * @see Window#setShape(Shape)
     * @see Frame#isUndecorated
     * @see Dialog#isUndecorated
     * @see GraphicsDevice.WindowTranslucency
     * @see GraphicsDevice#isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency)
     * @see GraphicsConfiguration#isTranslucencyCapable()
     */
    @Override
    public void setBackground(Color bgColor) {
        Color oldBg = getBackground();
        super.setBackground(bgColor);
        if (oldBg != null && oldBg.equals(bgColor)) {
            return;
        }
        int oldAlpha = oldBg != null ? oldBg.getAlpha() : 255;
        int alpha = bgColor != null ? bgColor.getAlpha() : 255;
        if ((oldAlpha == 255) && (alpha < 255)) { // 非不透明窗口
            GraphicsConfiguration gc = getGraphicsConfiguration();
            GraphicsDevice gd = gc.getDevice();
            if (gc.getDevice().getFullScreenWindow() == this) {
                throw new IllegalComponentStateException(
                    "将全屏窗口设置为非不透明不受支持。");
            }
            if (!gc.isTranslucencyCapable()) {
                GraphicsConfiguration capableGC = gd.getTranslucencyCapableGC();
                if (capableGC == null) {
                    throw new UnsupportedOperationException(
                        "PERPIXEL_TRANSLUCENT 透明度不受支持");
                }
                setGraphicsConfiguration(capableGC);
            }
            setLayersOpaque(this, false);
        } else if ((oldAlpha < 255) && (alpha == 255)) {
            setLayersOpaque(this, true);
        }
        WindowPeer peer = (WindowPeer)getPeer();
        if (peer != null) {
            peer.setOpaque(alpha == 255);
        }
    }


                /**
     * 表示窗口当前是否不透明。
     * <p>
     * 如果窗口的背景色不为 {@code null} 且颜色的 alpha 分量小于 {@code 1.0f}，则该方法返回 {@code false}。
     * 否则，该方法返回 {@code true}。
     *
     * @return 如果窗口不透明则返回 {@code true}，否则返回 {@code false}。
     *
     * @see Window#getBackground
     * @see Window#setBackground(Color)
     * @since 1.7
     */
    @Override
    public boolean isOpaque() {
        Color bg = getBackground();
        return bg != null ? bg.getAlpha() == 255 : true;
    }

    private void updateWindow() {
        synchronized (getTreeLock()) {
            WindowPeer peer = (WindowPeer)getPeer();
            if (peer != null) {
                peer.updateWindow();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.7
     */
    @Override
    public void paint(Graphics g) {
        if (!isOpaque()) {
            Graphics gg = g.create();
            try {
                if (gg instanceof Graphics2D) {
                    gg.setColor(getBackground());
                    ((Graphics2D)gg).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
                    gg.fillRect(0, 0, getWidth(), getHeight());
                }
            } finally {
                gg.dispose();
            }
        }
        super.paint(g);
    }

    private static void setLayersOpaque(Component component, boolean isOpaque) {
        // 不应使用 instanceof 以避免在纯 AWT 应用程序中加载 Swing 类。
        if (SunToolkit.isInstanceOf(component, "javax.swing.RootPaneContainer")) {
            javax.swing.RootPaneContainer rpc = (javax.swing.RootPaneContainer)component;
            javax.swing.JRootPane root = rpc.getRootPane();
            javax.swing.JLayeredPane lp = root.getLayeredPane();
            Container c = root.getContentPane();
            javax.swing.JComponent content =
                (c instanceof javax.swing.JComponent) ? (javax.swing.JComponent)c : null;
            lp.setOpaque(isOpaque);
            root.setOpaque(isOpaque);
            if (content != null) {
                content.setOpaque(isOpaque);

                // 向下迭代一级，以查看是否有需要处理的 JApplet（也是一个 RootPaneContainer）
                int numChildren = content.getComponentCount();
                if (numChildren > 0) {
                    Component child = content.getComponent(0);
                    // 现在可以使用 instanceof，因为我们已经加载了 RootPaneContainer 类
                    if (child instanceof javax.swing.RootPaneContainer) {
                        setLayersOpaque(child, isOpaque);
                    }
                }
            }
        }
    }


    // ************************** MIXING CODE *******************************

    // 窗口有一个所有者，但没有容器
    @Override
    final Container getContainer() {
        return null;
    }

    /**
     * 将形状应用到组件
     * @param shape 要应用到组件的形状
     */
    @Override
    final void applyCompoundShape(Region shape) {
        // 通过混合代码计算的形状不应用于窗口或框架
    }

    @Override
    final void applyCurrentShape() {
        // 通过混合代码计算的形状不应用于窗口或框架
    }

    @Override
    final void mixOnReshaping() {
        // 通过混合代码计算的形状不应用于窗口或框架
    }

    @Override
    final Point getLocationOnWindow() {
        return new Point(0, 0);
    }

    // ****************** END OF MIXING CODE ********************************

    /**
     * 限制给定的 double 值在给定范围内。
     */
    private static double limit(double value, double min, double max) {
        value = Math.max(value, min);
        value = Math.min(value, max);
        return value;
    }

    /**
     * 计算安全警告的位置。
     *
     * 该方法获取由本地系统报告的窗口位置/大小，因为本地缓存的值可能表示过时的数据。
     *
     * 该方法由本地代码或通过 AWTAccessor 调用。
     *
     * 注意：此方法在工具包线程上被调用，因此不应公开/用户可覆盖。
     */
    private Point2D calculateSecurityWarningPosition(double x, double y,
            double w, double h)
    {
        // 根据 SecurityWarning.setPosition() 的规范计算位置
        double wx = x + w * securityWarningAlignmentX + securityWarningPointX;
        double wy = y + h * securityWarningAlignmentY + securityWarningPointY;

        // 首先，确保警告不会离窗口边界太远
        wx = Window.limit(wx,
                x - securityWarningWidth - 2,
                x + w + 2);
        wy = Window.limit(wy,
                y - securityWarningHeight - 2,
                y + h + 2);

        // 现在确保警告窗口在屏幕上可见
        GraphicsConfiguration graphicsConfig =
            getGraphicsConfiguration_NoClientCode();
        Rectangle screenBounds = graphicsConfig.getBounds();
        Insets screenInsets =
            Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfig);

        wx = Window.limit(wx,
                screenBounds.x + screenInsets.left,
                screenBounds.x + screenBounds.width - screenInsets.right
                - securityWarningWidth);
        wy = Window.limit(wy,
                screenBounds.y + screenInsets.top,
                screenBounds.y + screenBounds.height - screenInsets.bottom
                - securityWarningHeight);

        return new Point2D.Double(wx, wy);
    }

    static {
        AWTAccessor.setWindowAccessor(new AWTAccessor.WindowAccessor() {
            public float getOpacity(Window window) {
                return window.opacity;
            }
            public void setOpacity(Window window, float opacity) {
                window.setOpacity(opacity);
            }
            public Shape getShape(Window window) {
                return window.getShape();
            }
            public void setShape(Window window, Shape shape) {
                window.setShape(shape);
            }
            public void setOpaque(Window window, boolean opaque) {
                Color bg = window.getBackground();
                if (bg == null) {
                    bg = new Color(0, 0, 0, 0);
                }
                window.setBackground(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(),
                                               opaque ? 255 : 0));
            }
            public void updateWindow(Window window) {
                window.updateWindow();
            }

            public Dimension getSecurityWarningSize(Window window) {
                return new Dimension(window.securityWarningWidth,
                        window.securityWarningHeight);
            }

            public void setSecurityWarningSize(Window window, int width, int height)
            {
                window.securityWarningWidth = width;
                window.securityWarningHeight = height;
            }

            public void setSecurityWarningPosition(Window window,
                    Point2D point, float alignmentX, float alignmentY)
            {
                window.securityWarningPointX = point.getX();
                window.securityWarningPointY = point.getY();
                window.securityWarningAlignmentX = alignmentX;
                window.securityWarningAlignmentY = alignmentY;

                synchronized (window.getTreeLock()) {
                    WindowPeer peer = (WindowPeer)window.getPeer();
                    if (peer != null) {
                        peer.repositionSecurityWarning();
                    }
                }
            }

            public Point2D calculateSecurityWarningPosition(Window window,
                    double x, double y, double w, double h)
            {
                return window.calculateSecurityWarningPosition(x, y, w, h);
            }

            public void setLWRequestStatus(Window changed, boolean status) {
                changed.syncLWRequests = status;
            }

            public boolean isAutoRequestFocus(Window w) {
                return w.autoRequestFocus;
            }

            public boolean isTrayIconWindow(Window w) {
                return w.isTrayIconWindow;
            }

            public void setTrayIconWindow(Window w, boolean isTrayIconWindow) {
                w.isTrayIconWindow = isTrayIconWindow;
            }

            public Window[] getOwnedWindows(Window w) {
                return w.getOwnedWindows_NoClientCode();
            }
        }); // WindowAccessor
    } // static

    // 窗口不需要在 Z 顺序中更新。
    @Override
    void updateZOrder() {}

} // class Window


/**
 * 该类不再使用，但为了序列化向后兼容性而保留。
 */
class FocusManager implements java.io.Serializable {
    Container focusRoot;
    Component focusOwner;

    /*
     * JDK 1.1 序列化版本 ID
     */
    static final long serialVersionUID = 2491878825643557906L;
}
