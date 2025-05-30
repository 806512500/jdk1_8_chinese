
/*
 * Copyright (c) 1995, 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Vector;
import java.util.Locale;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.awt.peer.ComponentPeer;
import java.awt.peer.ContainerPeer;
import java.awt.peer.LightweightPeer;
import java.awt.image.BufferStrategy;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;
import java.awt.event.*;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.Transient;
import java.awt.im.InputContext;
import java.awt.im.InputMethodRequests;
import java.awt.dnd.DropTarget;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.AccessControlContext;
import javax.accessibility.*;
import java.applet.Applet;

import sun.security.action.GetPropertyAction;
import sun.awt.AppContext;
import sun.awt.AWTAccessor;
import sun.awt.ConstrainableGraphics;
import sun.awt.SubRegionShowable;
import sun.awt.SunToolkit;
import sun.awt.WindowClosingListener;
import sun.awt.CausedFocusEvent;
import sun.awt.EmbeddedFrame;
import sun.awt.dnd.SunDropTargetEvent;
import sun.awt.im.CompositionArea;
import sun.font.FontManager;
import sun.font.FontManagerFactory;
import sun.font.SunFontManager;
import sun.java2d.SunGraphics2D;
import sun.java2d.pipe.Region;
import sun.awt.image.VSyncedBSManager;
import sun.java2d.pipe.hw.ExtendedBufferCapabilities;
import static sun.java2d.pipe.hw.ExtendedBufferCapabilities.VSyncType.*;
import sun.awt.RequestFocusController;
import sun.java2d.SunGraphicsEnvironment;
import sun.util.logging.PlatformLogger;

/**
 * 一个<em>组件</em>是一个具有图形表示的对象，可以显示在屏幕上并与用户交互。组件的示例包括典型图形用户界面中的按钮、复选框和滚动条。<p>
 * <code>Component</code>类是非菜单相关的抽象窗口工具包组件的抽象超类。类<code>Component</code>也可以直接扩展以创建轻量级组件。轻量级组件是没有关联本地窗口的组件。相反，重量级组件是与本地窗口关联的组件。可以使用{@link #isLightweight()}方法来区分这两种组件。
 * <p>
 * 轻量级和重量级组件可以在单个组件层次结构中混合使用。然而，为了使这种混合层次结构正确运行，整个层次结构必须是有效的。当层次结构无效时，例如在更改组件的边界或向容器中添加/删除组件之后，必须通过调用层次结构顶部无效容器的{@link Container#validate()}方法来验证整个层次结构。
 *
 * <h3>序列化</h3>
 * 需要注意的是，只有符合<code>Serializable</code>协议的AWT监听器在对象存储时才会被保存。如果AWT对象有未标记为可序列化的监听器，这些监听器将在<code>writeObject</code>时被丢弃。开发人员需要像往常一样考虑使对象可序列化的后果。需要注意的一种情况是：
 * <pre>
 *    import java.awt.*;
 *    import java.awt.event.*;
 *    import java.io.Serializable;
 *
 *    class MyApp implements ActionListener, Serializable
 *    {
 *        BigObjectThatShouldNotBeSerializedWithAButton bigOne;
 *        Button aButton = new Button();
 *
 *        MyApp()
 *        {
 *            // 哦，现在aButton有一个带有bigOne引用的监听器！
 *            aButton.addActionListener(this);
 *        }
 *
 *        public void actionPerformed(ActionEvent e)
 *        {
 *            System.out.println("Hello There");
 *        }
 *    }
 * </pre>
 * 在这个例子中，单独序列化<code>aButton</code>将导致<code>MyApp</code>及其引用的所有对象也被序列化。问题是监听器是巧合地可序列化的，而不是设计如此。为了分离<code>MyApp</code>和<code>ActionListener</code>是否可序列化的决定，可以使用内部类，如下例所示：
 * <pre>
 *    import java.awt.*;
 *    import java.awt.event.*;
 *    import java.io.Serializable;
 *
 *    class MyApp implements java.io.Serializable
 *    {
 *         BigObjectThatShouldNotBeSerializedWithAButton bigOne;
 *         Button aButton = new Button();
 *
 *         static class MyActionListener implements ActionListener
 *         {
 *             public void actionPerformed(ActionEvent e)
 *             {
 *                 System.out.println("Hello There");
 *             }
 *         }
 *
 *         MyApp()
 *         {
 *             aButton.addActionListener(new MyActionListener());
 *         }
 *    }
 * </pre>
 * <p>
 * <b>注意</b>：有关AWT和Swing使用的绘图机制的更多信息，包括如何编写最高效的绘图代码，请参阅
 * <a href="http://www.oracle.com/technetwork/java/painting-140037.html">AWT和Swing中的绘图</a>。
 * <p>
 * 有关焦点子系统的详细信息，请参阅
 * <a href="https://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html">
 * 如何使用焦点子系统</a>，
 * 《Java教程》中的一个部分，以及
 * <a href="../../java/awt/doc-files/FocusSpec.html">焦点规范</a>
 * 以获取更多信息。
 *
 * @author      Arthur van Hoff
 * @author      Sami Shaio
 */
public abstract class Component implements ImageObserver, MenuContainer,
                                           Serializable
{

    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.Component");
    private static final PlatformLogger eventLog = PlatformLogger.getLogger("java.awt.event.Component");
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("java.awt.focus.Component");
    private static final PlatformLogger mixingLog = PlatformLogger.getLogger("java.awt.mixing.Component");

    /**
     * 组件的对等体。对等体实现了组件的行为。当<code>Component</code>被添加到也是一个对等体的容器中时，对等体被设置。
     * @see #addNotify
     * @see #removeNotify
     */
    transient ComponentPeer peer;

    /**
     * 对象的父级。对于顶级组件，它可以是<code>null</code>。
     * @see #getParent
     */
    transient Container parent;

    /**
     * 组件在父级坐标系统中的x位置。
     *
     * @serial
     * @see #getLocation
     */
    int x;

    /**
     * 组件在父级坐标系统中的y位置。
     *
     * @serial
     * @see #getLocation
     */
    int y;

    /**
     * 组件的宽度。
     *
     * @serial
     * @see #getSize
     */
    int width;

    /**
     * 组件的高度。
     *
     * @serial
     * @see #getSize
     */
    int height;

    /**
     * 此组件的前景色。
     * <code>foreground</code>可以是<code>null</code>。
     *
     * @serial
     * @see #getForeground
     * @see #setForeground
     */
    Color       foreground;

    /**
     * 此组件的背景色。
     * <code>background</code>可以是<code>null</code>。
     *
     * @serial
     * @see #getBackground
     * @see #setBackground
     */
    Color       background;

    /**
     * 此组件使用的字体。
     * <code>font</code>可以是<code>null</code>。
     *
     * @serial
     * @see #getFont
     * @see #setFont
     */
    volatile Font font;

    /**
     * 对等体当前使用的字体。
     * （如果不存在对等体，则为<code>null</code>。）
     */
    Font        peerFont;

    /**
     * 当指针悬停在此组件上时显示的光标。
     * 此值可以是<code>null</code>。
     *
     * @serial
     * @see #getCursor
     * @see #setCursor
     */
    Cursor      cursor;

    /**
     * 组件的区域设置。
     *
     * @serial
     * @see #getLocale
     * @see #setLocale
     */
    Locale      locale;

    /**
     * 一个引用<code>GraphicsConfiguration</code>对象的引用，用于描述图形目的地的特性。
     * 此值可以是<code>null</code>。
     *
     * @since 1.3
     * @serial
     * @see GraphicsConfiguration
     * @see #getGraphicsConfiguration
     */
    private transient volatile GraphicsConfiguration graphicsConfig;

    /**
     * 一个引用<code>BufferStrategy</code>对象的引用，用于操作此组件上的缓冲区。
     *
     * @since 1.4
     * @see java.awt.image.BufferStrategy
     * @see #getBufferStrategy()
     */
    transient BufferStrategy bufferStrategy = null;

    /**
     * 当对象应忽略所有重绘事件时为真。
     *
     * @since 1.4
     * @serial
     * @see #setIgnoreRepaint
     * @see #getIgnoreRepaint
     */
    boolean ignoreRepaint = false;

    /**
     * 当对象可见时为真。不可见的对象不会在屏幕上绘制。
     *
     * @serial
     * @see #isVisible
     * @see #setVisible
     */
    boolean visible = true;

    /**
     * 当对象启用时为真。未启用的对象不会与用户交互。
     *
     * @serial
     * @see #isEnabled
     * @see #setEnabled
     */
    boolean enabled = true;

    /**
     * 当对象有效时为真。无效的对象需要布局。当对象大小改变时，此标志将被设置为假。
     *
     * @serial
     * @see #isValid
     * @see #validate
     * @see #invalidate
     */
    private volatile boolean valid = false;

    /**
     * 与此组件关联的<code>DropTarget</code>。
     *
     * @since 1.2
     * @serial
     * @see #setDropTarget
     * @see #getDropTarget
     */
    DropTarget dropTarget;

    /**
     * @serial
     * @see #add
     */
    Vector<PopupMenu> popups;

    /**
     * 组件的名称。
     * 此字段可以是<code>null</code>。
     *
     * @serial
     * @see #getName
     * @see #setName(String)
     */
    private String name;

    /**
     * 一个布尔值，用于确定名称是否已显式设置。<code>nameExplicitlySet</code>如果名称未设置，则为假，如果已设置，则为真。
     *
     * @serial
     * @see #getName
     * @see #setName(String)
     */
    private boolean nameExplicitlySet = false;

    /**
     * 指示此组件是否可以获取焦点。
     *
     * @serial
     * @see #setFocusable
     * @see #isFocusable
     * @since 1.4
     */
    private boolean focusable = true;

    private static final int FOCUS_TRAVERSABLE_UNKNOWN = 0;
    private static final int FOCUS_TRAVERSABLE_DEFAULT = 1;
    private static final int FOCUS_TRAVERSABLE_SET = 2;

    /**
     * 跟踪此组件是否依赖于默认的焦点遍历性。
     *
     * @serial
     * @since 1.4
     */
    private int isFocusTraversableOverridden = FOCUS_TRAVERSABLE_UNKNOWN;

    /**
     * 焦点遍历键。这些键将为启用焦点遍历键的组件生成焦点遍历行为。如果为遍历键指定了<code>null</code>值，此组件将从其父级继承该遍历键。如果此组件的所有祖先都为该遍历键指定了<code>null</code>，则使用当前KeyboardFocusManager的默认遍历键。
     *
     * @serial
     * @see #setFocusTraversalKeys
     * @see #getFocusTraversalKeys
     * @since 1.4
     */
    Set<AWTKeyStroke>[] focusTraversalKeys;

    private static final String[] focusTraversalKeyPropertyNames = {
        "forwardFocusTraversalKeys",
        "backwardFocusTraversalKeys",
        "upCycleFocusTraversalKeys",
        "downCycleFocusTraversalKeys"
    };

    /**
     * 指示是否为此组件启用了焦点遍历键。对于禁用了焦点遍历键的组件，将接收焦点遍历键的键事件。对于启用了焦点遍历键的组件，这些事件将不会被看到；相反，这些事件将自动转换为遍历操作。
     *
     * @serial
     * @see #setFocusTraversalKeysEnabled
     * @see #getFocusTraversalKeysEnabled
     * @since 1.4
     */
    private boolean focusTraversalKeysEnabled = true;

    /**
     * AWT组件树和布局操作的锁定对象。
     *
     * @see #getTreeLock
     */
    static final Object LOCK = new AWTTreeLock();
    static class AWTTreeLock {}

    /*
     * 组件的AccessControlContext。
     */
    private transient volatile AccessControlContext acc =
        AccessController.getContext();

    /**
     * 最小尺寸。
     * （此字段或许应该是瞬态的）。
     *
     * @serial
     */
    Dimension minSize;

    /**
     * 是否调用了setMinimumSize且传入了非null值。
     */
    boolean minSizeSet;

    /**
     * 偏好尺寸。
     * （此字段或许应该是瞬态的）。
     *
     * @serial
     */
    Dimension prefSize;

    /**
     * 是否调用了setPreferredSize且传入了非null值。
     */
    boolean prefSizeSet;


                /**
     * 最大尺寸
     *
     * @serial
     */
    Dimension maxSize;

    /**
     * 是否调用了 setMaximumSize 并传入了非 null 值。
     */
    boolean maxSizeSet;

    /**
     * 该组件的方向。
     * @see #getComponentOrientation
     * @see #setComponentOrientation
     */
    transient ComponentOrientation componentOrientation
    = ComponentOrientation.UNKNOWN;

    /**
     * <code>newEventsOnly</code> 为 true 表示事件是
     * 为组件启用的事件类型之一。
     * 它将允许继续正常处理。
     * 如果为 false，则事件将传递给
     * 组件的父级并沿祖先树向上传递
     * 直到事件被消耗。
     *
     * @serial
     * @see #dispatchEvent
     */
    boolean newEventsOnly = false;
    transient ComponentListener componentListener;
    transient FocusListener focusListener;
    transient HierarchyListener hierarchyListener;
    transient HierarchyBoundsListener hierarchyBoundsListener;
    transient KeyListener keyListener;
    transient MouseListener mouseListener;
    transient MouseMotionListener mouseMotionListener;
    transient MouseWheelListener mouseWheelListener;
    transient InputMethodListener inputMethodListener;

    transient RuntimeException windowClosingException = null;

    /** 内部，用于序列化的常量 */
    final static String actionListenerK = "actionL";
    final static String adjustmentListenerK = "adjustmentL";
    final static String componentListenerK = "componentL";
    final static String containerListenerK = "containerL";
    final static String focusListenerK = "focusL";
    final static String itemListenerK = "itemL";
    final static String keyListenerK = "keyL";
    final static String mouseListenerK = "mouseL";
    final static String mouseMotionListenerK = "mouseMotionL";
    final static String mouseWheelListenerK = "mouseWheelL";
    final static String textListenerK = "textL";
    final static String ownedWindowK = "ownedL";
    final static String windowListenerK = "windowL";
    final static String inputMethodListenerK = "inputMethodL";
    final static String hierarchyListenerK = "hierarchyL";
    final static String hierarchyBoundsListenerK = "hierarchyBoundsL";
    final static String windowStateListenerK = "windowStateL";
    final static String windowFocusListenerK = "windowFocusL";

    /**
     * <code>eventMask</code> 仅由子类通过
     * <code>enableEvents</code> 设置。
     * 当注册监听器时不应设置掩码
     * 以便我们可以区分监听器请求事件和子类请求事件之间的区别。
     * 一个位用于指示是否启用了输入方法；此位由 <code>enableInputMethods</code> 设置，默认为开启。
     *
     * @serial
     * @see #enableInputMethods
     * @see AWTEvent
     */
    long eventMask = AWTEvent.INPUT_METHODS_ENABLED_MASK;

    /**
     * 用于增量绘制的静态属性。
     * @see #imageUpdate
     */
    static boolean isInc;
    static int incRate;
    static {
        /* 确保加载了必要的本机库 */
        Toolkit.loadLibraries();
        /* 初始化 JNI 字段和方法 ID */
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }

        String s = java.security.AccessController.doPrivileged(
                                                               new GetPropertyAction("awt.image.incrementaldraw"));
        isInc = (s == null || s.equals("true"));

        s = java.security.AccessController.doPrivileged(
                                                        new GetPropertyAction("awt.image.redrawrate"));
        incRate = (s != null) ? Integer.parseInt(s) : 100;
    }

    /**
     * 用于 <code>getAlignmentY()</code> 的便捷常量。
     * 指定组件顶部对齐。
     * @see     #getAlignmentY
     */
    public static final float TOP_ALIGNMENT = 0.0f;

    /**
     * 用于 <code>getAlignmentY</code> 和
     * <code>getAlignmentX</code> 的便捷常量。指定组件中心对齐。
     * @see     #getAlignmentX
     * @see     #getAlignmentY
     */
    public static final float CENTER_ALIGNMENT = 0.5f;

    /**
     * 用于 <code>getAlignmentY</code> 的便捷常量。
     * 指定组件底部对齐。
     * @see     #getAlignmentY
     */
    public static final float BOTTOM_ALIGNMENT = 1.0f;

    /**
     * 用于 <code>getAlignmentX</code> 的便捷常量。
     * 指定组件左侧对齐。
     * @see     #getAlignmentX
     */
    public static final float LEFT_ALIGNMENT = 0.0f;

    /**
     * 用于 <code>getAlignmentX</code> 的便捷常量。
     * 指定组件右侧对齐。
     * @see     #getAlignmentX
     */
    public static final float RIGHT_ALIGNMENT = 1.0f;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -7644114512714619750L;

    /**
     * 如果已注册任何 <code>PropertyChangeListeners</code>，
     * 则 <code>changeSupport</code> 字段描述它们。
     *
     * @serial
     * @since 1.2
     * @see #addPropertyChangeListener
     * @see #removePropertyChangeListener
     * @see #firePropertyChange
     */
    private PropertyChangeSupport changeSupport;

    /*
     * 在某些情况下，使用 "this" 作为同步对象
     * 可能会导致死锁，如果客户端代码也使用组件对象进行同步。
     * 对于每个此类情况，我们应该考虑用下面引入的包私有对象
     * objectLock 替换 "this"。到目前为止，已知有 3 个问题：
     * - CR 6708322 (getName/setName 方法);
     * - CR 6608764 (PropertyChangeListener 机制);
     * - CR 7108598 (Container.paint/KeyboardFocusManager.clearMostRecentFocusOwner 方法)。
     *
     * 注意：此字段被认为是 final 的，尽管 readObject() 禁止
     * 初始化 final 字段。
     */
    private transient Object objectLock = new Object();
    Object getObjectLock() {
        return objectLock;
    }

    /*
     * 返回构造此组件时使用的 acc。
     */
    final AccessControlContext getAccessControlContext() {
        if (acc == null) {
            throw new SecurityException("Component is missing AccessControlContext");
        }
        return acc;
    }

    boolean isPacked = false;

    /**
     * 用于直接 Geometry API (setLocation, setBounds, setSize)
     * 的伪参数，用于向 setBounds 信号传递正在更改的内容。应在 TreeLock 下使用。
     * 由于无法更改公共方法和已弃用方法之间的交叉调用顺序，因此需要此参数。
     */
    private int boundsOp = ComponentPeer.DEFAULT_OPERATION;

    /**
     * 枚举了组件大小变化时基线变化的常见方式。
     * 基线调整行为主要用于布局管理器，这些管理器需要知道
     * 组件大小变化时基线位置的变化。
     * 通常，基线调整行为对于大于或等于最小尺寸（实际最小尺寸；而非开发人员指定的最小尺寸）的尺寸是有效的。
     * 对于小于最小尺寸的尺寸，基线可能会以与基线调整行为指示的方式不同的方式变化。
     * 同样，当尺寸接近 <code>Integer.MAX_VALUE</code> 和/或
     * <code>Short.MAX_VALUE</code> 时，基线可能会以与基线调整行为指示的方式不同的方式变化。
     *
     * @see #getBaselineResizeBehavior
     * @see #getBaseline(int,int)
     * @since 1.6
     */
    public enum BaselineResizeBehavior {
        /**
         * 表示基线相对于 y 轴原点保持固定。
         * 即，<code>getBaseline</code> 返回的值与高度或宽度无关。例如，
         * 包含非空文本且垂直对齐方式为 <code>TOP</code> 的 <code>JLabel</code>
         * 应具有 <code>CONSTANT_ASCENT</code> 基线类型。
         */
        CONSTANT_ASCENT,

        /**
         * 表示基线相对于高度保持固定，并且宽度变化时不会改变。
         * 即，对于任何高度 H，H 与 <code>getBaseline(w, H)</code> 之间的差值相同。
         * 例如，包含非空文本且垂直对齐方式为 <code>BOTTOM</code> 的 <code>JLabel</code>
         * 应具有 <code>CONSTANT_DESCENT</code> 基线类型。
         */
        CONSTANT_DESCENT,

        /**
         * 表示基线与组件中心保持固定距离。
         * 即，对于任何高度 H，<code>getBaseline(w, H)</code> 与 <code>H / 2</code> 之间的差值相同（加减 1，取决于舍入误差）。
         * <p>
         * 由于可能的舍入误差，建议使用两个连续的高度来询问基线，并使用返回值来确定是否需要在计算中增加 1。
         * 以下显示了如何计算任何高度的基线：
         * <pre>
         *   Dimension preferredSize = component.getPreferredSize();
         *   int baseline = getBaseline(preferredSize.width,
         *                              preferredSize.height);
         *   int nextBaseline = getBaseline(preferredSize.width,
         *                                  preferredSize.height + 1);
         *   // 计算特定高度时基线的位置需要增加的高度：
         *   int padding = 0;
         *   // 基线相对于中点的位置
         *   int baselineOffset = baseline - height / 2;
         *   if (preferredSize.height % 2 == 0 &amp;&amp;
         *       baseline != nextBaseline) {
         *       padding = 1;
         *   }
         *   else if (preferredSize.height % 2 == 1 &amp;&amp;
         *            baseline == nextBaseline) {
         *       baselineOffset--;
         *       padding = 1;
         *   }
         *   // 以下计算高度 z 时基线的位置：
         *   int calculatedBaseline = (z + padding) / 2 + baselineOffset;
         * </pre>
         */
        CENTER_OFFSET,

        /**
         * 表示基线调整行为无法用其他常量表示。
         * 这也可能表示基线随组件宽度变化。这也是没有基线的组件返回的值。
         */
        OTHER
    }

    /*
     * 通过 applyCompoundShape() 方法设置的形状。它包括与 HW/LW 混合相关的形状计算结果。
     * 它也可能包括用户指定的组件形状。
     * 'null' 值表示组件具有正常形状（或根本没有形状），applyCompoundShape() 将跳过与正常形状相同的形状。
     */
    private transient Region compoundShape = null;

    /*
     * 表示应从重叠的重量级组件中裁剪出的轻量级组件的形状。可能的值：
     *    1. null - 考虑形状为矩形
     *    2. EMPTY_REGION - 没有裁剪（子组件仍然会被裁剪）
     *    3. 非空 - 裁剪此形状。
     */
    private transient Region mixingCutoutRegion = null;

    /*
     * 表示 addNotify() 是否已完成
     * （即，peer 是否已创建）。
     */
    private transient boolean isAddNotifyComplete = false;

    /**
     * 仅应在子类的 getBounds 中使用，以检查 bounds 的部分是否实际更改。
     */
    int getBoundsOp() {
        assert Thread.holdsLock(getTreeLock());
        return boundsOp;
    }

    void setBoundsOp(int op) {
        assert Thread.holdsLock(getTreeLock());
        if (op == ComponentPeer.RESET_OPERATION) {
            boundsOp = ComponentPeer.DEFAULT_OPERATION;
        } else
            if (boundsOp == ComponentPeer.DEFAULT_OPERATION) {
                boundsOp = op;
            }
    }

    // 表示此组件是否已通过 SunToolkit.disableBackgroundErase() 指定了背景擦除标志。
    // 这是为了使该功能在 X11 平台上工作，目前在创建 peer 时没有机会进行干预，
    // 因此无法调用 XSetBackground。
    transient boolean backgroundEraseDisabled;

    static {
        AWTAccessor.setComponentAccessor(new AWTAccessor.ComponentAccessor() {
            public void setBackgroundEraseDisabled(Component comp, boolean disabled) {
                comp.backgroundEraseDisabled = disabled;
            }
            public boolean getBackgroundEraseDisabled(Component comp) {
                return comp.backgroundEraseDisabled;
            }
            public Rectangle getBounds(Component comp) {
                return new Rectangle(comp.x, comp.y, comp.width, comp.height);
            }
            public void setMixingCutoutShape(Component comp, Shape shape) {
                Region region = shape == null ?  null :
                    Region.getInstance(shape, null);

                synchronized (comp.getTreeLock()) {
                    boolean needShowing = false;
                    boolean needHiding = false;

                    if (!comp.isNonOpaqueForMixing()) {
                        needHiding = true;
                    }

                    comp.mixingCutoutRegion = region;

                    if (!comp.isNonOpaqueForMixing()) {
                        needShowing = true;
                    }

                    if (comp.isMixingNeeded()) {
                        if (needHiding) {
                            comp.mixOnHiding(comp.isLightweight());
                        }
                        if (needShowing) {
                            comp.mixOnShowing();
                        }
                    }
                }
            }

            public void setGraphicsConfiguration(Component comp,
                    GraphicsConfiguration gc)
            {
                comp.setGraphicsConfiguration(gc);
            }
            public boolean requestFocus(Component comp, CausedFocusEvent.Cause cause) {
                return comp.requestFocus(cause);
            }
            public boolean canBeFocusOwner(Component comp) {
                return comp.canBeFocusOwner();
            }

            public boolean isVisible(Component comp) {
                return comp.isVisible_NoClientCode();
            }
            public void setRequestFocusController
                (RequestFocusController requestController)
            {
                 Component.setRequestFocusController(requestController);
            }
            public AppContext getAppContext(Component comp) {
                 return comp.appContext;
            }
            public void setAppContext(Component comp, AppContext appContext) {
                 comp.appContext = appContext;
            }
            public Container getParent(Component comp) {
                return comp.getParent_NoClientCode();
            }
            public void setParent(Component comp, Container parent) {
                comp.parent = parent;
            }
            public void setSize(Component comp, int width, int height) {
                comp.width = width;
                comp.height = height;
            }
            public Point getLocation(Component comp) {
                return comp.location_NoClientCode();
            }
            public void setLocation(Component comp, int x, int y) {
                comp.x = x;
                comp.y = y;
            }
            public boolean isEnabled(Component comp) {
                return comp.isEnabledImpl();
            }
            public boolean isDisplayable(Component comp) {
                return comp.peer != null;
            }
            public Cursor getCursor(Component comp) {
                return comp.getCursor_NoClientCode();
            }
            public ComponentPeer getPeer(Component comp) {
                return comp.peer;
            }
            public void setPeer(Component comp, ComponentPeer peer) {
                comp.peer = peer;
            }
            public boolean isLightweight(Component comp) {
                return (comp.peer instanceof LightweightPeer);
            }
            public boolean getIgnoreRepaint(Component comp) {
                return comp.ignoreRepaint;
            }
            public int getWidth(Component comp) {
                return comp.width;
            }
            public int getHeight(Component comp) {
                return comp.height;
            }
            public int getX(Component comp) {
                return comp.x;
            }
            public int getY(Component comp) {
                return comp.y;
            }
            public Color getForeground(Component comp) {
                return comp.foreground;
            }
            public Color getBackground(Component comp) {
                return comp.background;
            }
            public void setBackground(Component comp, Color background) {
                comp.background = background;
            }
            public Font getFont(Component comp) {
                return comp.getFont_NoClientCode();
            }
            public void processEvent(Component comp, AWTEvent e) {
                comp.processEvent(e);
            }


                        public AccessControlContext getAccessControlContext(Component comp) {
                return comp.getAccessControlContext();
            }

            public void revalidateSynchronously(Component comp) {
                comp.revalidateSynchronously();
            }
        });

    /**
     * 构造一个新的组件。类 <code>Component</code> 可以直接扩展以创建一个不使用不透明本机窗口的轻量级组件。轻量级组件必须由更高层次的组件树中的本机容器托管（例如，由 <code>Frame</code> 对象托管）。
     */
    protected Component() {
        appContext = AppContext.getAppContext();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    void initializeFocusTraversalKeys() {
        focusTraversalKeys = new Set[3];
    }

    /**
     * 为这个组件构造一个名称。当名称为 <code>null</code> 时，由 <code>getName</code> 调用。
     */
    String constructComponentName() {
        return null; // 为了严格遵守早期平台版本，一个未设置名称的 Component
                     // 从 getName() 返回 null
    }

    /**
     * 获取组件的名称。
     * @return 此组件的名称
     * @see    #setName
     * @since JDK1.1
     */
    public String getName() {
        if (name == null && !nameExplicitlySet) {
            synchronized(getObjectLock()) {
                if (name == null && !nameExplicitlySet)
                    name = constructComponentName();
            }
        }
        return name;
    }

    /**
     * 将组件的名称设置为指定的字符串。
     * @param name  要成为此组件名称的字符串
     * @see #getName
     * @since JDK1.1
     */
    public void setName(String name) {
        String oldName;
        synchronized(getObjectLock()) {
            oldName = this.name;
            this.name = name;
            nameExplicitlySet = true;
        }
        firePropertyChange("name", oldName, name);
    }

    /**
     * 获取此组件的父容器。
     * @return 此组件的父容器
     * @since JDK1.0
     */
    public Container getParent() {
        return getParent_NoClientCode();
    }

    // 注意：此方法可能由特权线程调用。
    // 此功能实现在一个包私有方法中
    // 以确保它不能被客户端子类覆盖。
    // 不要在该线程上调用客户端代码！
    final Container getParent_NoClientCode() {
        return parent;
    }

    // 该方法在 Window 类中被覆盖以返回 null，
    // 因为 Window 对象的 parent 字段包含
    // 窗口的所有者，而不是其父容器。
    Container getContainer() {
        return getParent_NoClientCode();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 程序不应直接操作对等体；
     * 由 <code>boolean isDisplayable()</code> 替代。
     */
    @Deprecated
    public ComponentPeer getPeer() {
        return peer;
    }

    /**
     * 为该组件关联一个 <code>DropTarget</code>。
     * 仅当组件启用时，它才会接收拖放。
     *
     * @see #isEnabled
     * @param dt The DropTarget
     */

    public synchronized void setDropTarget(DropTarget dt) {
        if (dt == dropTarget || (dropTarget != null && dropTarget.equals(dt)))
            return;

        DropTarget old;

        if ((old = dropTarget) != null) {
            if (peer != null) dropTarget.removeNotify(peer);

            DropTarget t = dropTarget;

            dropTarget = null;

            try {
                t.setComponent(null);
            } catch (IllegalArgumentException iae) {
                // 忽略它。
            }
        }

        // 如果我们有一个新的 DropTarget，并且我们有一个对等体，添加它！

        if ((dropTarget = dt) != null) {
            try {
                dropTarget.setComponent(this);
                if (peer != null) dropTarget.addNotify(peer);
            } catch (IllegalArgumentException iae) {
                if (old != null) {
                    try {
                        old.setComponent(this);
                        if (peer != null) dropTarget.addNotify(peer);
                    } catch (IllegalArgumentException iae1) {
                        // 忽略它！
                    }
                }
            }
        }
    }

    /**
     * 获取与此 <code>Component</code> 关联的 <code>DropTarget</code>。
     */

    public synchronized DropTarget getDropTarget() { return dropTarget; }

    /**
     * 获取与此 <code>Component</code> 关联的 <code>GraphicsConfiguration</code>。
     * 如果 <code>Component</code> 没有被分配一个特定的 <code>GraphicsConfiguration</code>，
     * 则返回 <code>Component</code> 对象的顶级容器的 <code>GraphicsConfiguration</code>。
     * 如果 <code>Component</code> 已经被创建，但尚未添加到 <code>Container</code> 中，此方法返回 <code>null</code>。
     *
     * @return 用于此 <code>Component</code> 的 <code>GraphicsConfiguration</code> 或 <code>null</code>
     * @since 1.3
     */
    public GraphicsConfiguration getGraphicsConfiguration() {
        return getGraphicsConfiguration_NoClientCode();
    }

    final GraphicsConfiguration getGraphicsConfiguration_NoClientCode() {
        return graphicsConfig;
    }

    void setGraphicsConfiguration(GraphicsConfiguration gc) {
        synchronized(getTreeLock()) {
            if (updateGraphicsData(gc)) {
                removeNotify();
                addNotify();
            }
        }
    }

    boolean updateGraphicsData(GraphicsConfiguration gc) {
        checkTreeLock();

        if (graphicsConfig == gc) {
            return false;
        }

        graphicsConfig = gc;

        ComponentPeer peer = getPeer();
        if (peer != null) {
            return peer.updateGraphicsData(gc);
        }
        return false;
    }

    /**
     * 检查此组件的 <code>GraphicsDevice</code>
     * <code>idString</code> 是否与字符串参数匹配。
     */
    void checkGD(String stringID) {
        if (graphicsConfig != null) {
            if (!graphicsConfig.getDevice().getIDstring().equals(stringID)) {
                throw new IllegalArgumentException(
                                                   "将容器添加到不同 GraphicsDevice 上的容器中");
            }
        }
    }

    /**
     * 获取 AWT 组件树和布局操作的锁定对象（拥有线程同步监视器的对象）。
     * @return 此组件的锁定对象
     */
    public final Object getTreeLock() {
        return LOCK;
    }

    final void checkTreeLock() {
        if (!Thread.holdsLock(getTreeLock())) {
            throw new IllegalStateException("调用此函数时应持有 treeLock");
        }
    }

    /**
     * 获取此组件的工具包。注意
     * 包含组件的框架控制该组件使用的工具包。因此，如果组件
     * 从一个框架移动到另一个框架，它使用的工具包可能会改变。
     * @return 此组件的工具包
     * @since JDK1.0
     */
    public Toolkit getToolkit() {
        return getToolkitImpl();
    }

    /*
     * 由本机代码调用，因此客户端代码不能
     * 在工具包线程上调用。
     */
    final Toolkit getToolkitImpl() {
        Container parent = this.parent;
        if (parent != null) {
            return parent.getToolkitImpl();
        }
        return Toolkit.getDefaultToolkit();
    }

    /**
     * 确定此组件是否有效。当组件在父容器中正确地调整大小和定位，并且其所有子组件也有效时，组件是有效的。
     * 为了考虑对等体的大小要求，组件在首次显示在屏幕上之前会被无效化。当父容器完全实现时，所有组件都将是有效的。
     * @return 如果组件有效，则返回 <code>true</code>，否则返回 <code>false</code>
     * @see #validate
     * @see #invalidate
     * @since JDK1.0
     */
    public boolean isValid() {
        return (peer != null) && valid;
    }

    /**
     * 确定此组件是否可显示。当组件连接到本机屏幕资源时，它是可显示的。
     * <p>
     * 当组件被添加到
     * 可显示的包含层次结构中，或者其包含层次结构变为可显示时，组件变为可显示。
     * 当其祖先窗口被打包或显示时，包含层次结构变为可显示。
     * <p>
     * 当组件从
     * 可显示的包含层次结构中移除，或者其包含层次结构变为不可显示时，组件变为不可显示。包含层次结构
     * 当其祖先窗口被销毁时变为不可显示。
     *
     * @return 如果组件可显示，则返回 <code>true</code>，否则返回 <code>false</code>
     * @see Container#add(Component)
     * @see Window#pack
     * @see Window#show
     * @see Container#remove(Component)
     * @see Window#dispose
     * @since 1.2
     */
    public boolean isDisplayable() {
        return getPeer() != null;
    }

    /**
     * 确定当其父组件可见时，此组件是否应可见。组件
     * 初始时是可见的，除了顶级组件如
     * <code>Frame</code> 对象。
     * @return 如果组件可见，则返回 <code>true</code>，否则返回 <code>false</code>
     * @see #setVisible
     * @since JDK1.0
     */
    @Transient
    public boolean isVisible() {
        return isVisible_NoClientCode();
    }
    final boolean isVisible_NoClientCode() {
        return visible;
    }

    /**
     * 确定此组件是否会在屏幕上显示。
     * @return 如果组件及其所有祖先直到顶级窗口或 null 父组件都可见，则返回 <code>true</code>，否则返回 <code>false</code>
     */
    boolean isRecursivelyVisible() {
        return visible && (parent == null || parent.isRecursivelyVisible());
    }

    /**
     * 确定组件相对于其父组件的可见部分的边界。
     *
     * @return 可见部分的边界
     */
    private Rectangle getRecursivelyVisibleBounds() {
        final Component container = getContainer();
        final Rectangle bounds = getBounds();
        if (container == null) {
            // 我们是顶级窗口或没有容器，返回我们的边界
            return bounds;
        }
        // 将容器的边界转换到我们的坐标空间
        final Rectangle parentsBounds = container.getRecursivelyVisibleBounds();
        parentsBounds.setLocation(0, 0);
        return parentsBounds.intersection(bounds);
    }

    /**
     * 将绝对坐标转换为该组件坐标空间中的坐标。
     */
    Point pointRelativeToComponent(Point absolute) {
        Point compCoords = getLocationOnScreen();
        return new Point(absolute.x - compCoords.x,
                         absolute.y - compCoords.y);
    }

    /**
     * 假设鼠标位置存储在传递给此方法的 PointerInfo 中，它会找到与该组件在同一
     * 窗口中且位于鼠标指针下的组件。如果不存在这样的组件，则返回 null。
     * 注意：此方法应在树锁的保护下调用，如 Component.getMousePosition() 和
     * Container.getMousePosition(boolean) 中所做的那样。
     */
    Component findUnderMouseInWindow(PointerInfo pi) {
        if (!isShowing()) {
            return null;
        }
        Window win = getContainingWindow();
        if (!Toolkit.getDefaultToolkit().getMouseInfoPeer().isWindowUnderMouse(win)) {
            return null;
        }
        final boolean INCLUDE_DISABLED = true;
        Point relativeToWindow = win.pointRelativeToComponent(pi.getLocation());
        Component inTheSameWindow = win.findComponentAt(relativeToWindow.x,
                                                        relativeToWindow.y,
                                                        INCLUDE_DISABLED);
        return inTheSameWindow;
    }

    /**
     * 如果组件直接位于鼠标指针下，则返回鼠标指针在该 <code>Component</code> 的坐标空间中的位置，否则返回 <code>null</code>。
     * 如果组件未在屏幕上显示，即使鼠标指针位于组件将显示的区域上方，此方法也返回 <code>null</code>。
     * 如果组件被其他组件或本机窗口部分或完全遮挡，此方法仅在鼠标指针位于组件未被遮挡的部分上方时返回非 null 值。
     * <p>
     * 对于 <code>Container</code>，如果鼠标位于 <code>Container</code> 本身或其任何子组件上方，则返回非 null 值。
     * 如果需要排除子组件，请使用 {@link Container#getMousePosition(boolean)}。
     * <p>
     * 有时确切的鼠标坐标并不重要，唯一重要的是特定的 <code>Component</code> 是否位于鼠标指针下。如果此方法的返回值为 <code>null</code>，则鼠标指针不在组件正上方。
     *
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
     * @see       #isShowing
     * @see       Container#getMousePosition
     * @return    相对于此 <code>Component</code> 的鼠标坐标，或 null
     * @since     1.5
     */
    public Point getMousePosition() throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }

        PointerInfo pi = java.security.AccessController.doPrivileged(
                                                                     new java.security.PrivilegedAction<PointerInfo>() {
                                                                         public PointerInfo run() {
                                                                             return MouseInfo.getPointerInfo();
                                                                         }
                                                                     }
                                                                     );

        synchronized (getTreeLock()) {
            Component inTheSameWindow = findUnderMouseInWindow(pi);
            if (!isSameOrAncestorOf(inTheSameWindow, true)) {
                return null;
            }
            return pointRelativeToComponent(pi.getLocation());
        }
    }


                /**
     * 在 Container 中重写。必须在 TreeLock 下调用。
     */
    boolean isSameOrAncestorOf(Component comp, boolean allowChildren) {
        return comp == this;
    }

    /**
     * 确定此组件是否在屏幕上显示。这意味着
     * 组件必须是可见的，并且必须位于可见且显示的容器中。
     * <p>
     * <strong>注意：</strong> 有时无法检测组件是否实际对用户可见。这可能发生在：
     * <ul>
     * <li>组件已添加到可见的 {@code ScrollPane}，但组件当前不在滚动窗格的视口中。
     * <li>组件被另一个组件或容器遮挡。
     * </ul>
     * @return <code>true</code> 如果组件正在显示，
     *          <code>false</code> 否则
     * @see #setVisible
     * @since JDK1.0
     */
    public boolean isShowing() {
        if (visible && (peer != null)) {
            Container parent = this.parent;
            return (parent == null) || parent.isShowing();
        }
        return false;
    }

    /**
     * 确定此组件是否已启用。已启用的组件可以响应用户输入并生成事件。组件默认情况下是启用的。可以通过调用其 <code>setEnabled</code> 方法来启用或禁用组件。
     * @return <code>true</code> 如果组件已启用，
     *          <code>false</code> 否则
     * @see #setEnabled
     * @since JDK1.0
     */
    public boolean isEnabled() {
        return isEnabledImpl();
    }

    /*
     * 由本机代码调用，因此客户端代码不能在工具包线程上调用。
     */
    final boolean isEnabledImpl() {
        return enabled;
    }

    /**
     * 根据参数 <code>b</code> 启用或禁用此组件。已启用的组件可以响应用户输入并生成事件。组件默认情况下是启用的。
     *
     * <p>注意：禁用轻量级组件不会阻止它接收 MouseEvents。
     * <p>注意：禁用重量级容器会阻止该容器中的所有组件接收任何输入事件。但禁用轻量级容器仅影响该容器。
     *
     * @param     b   如果 <code>true</code>，此组件将被启用；否则此组件将被禁用
     * @see #isEnabled
     * @see #isLightweight
     * @since JDK1.1
     */
    public void setEnabled(boolean b) {
        enable(b);
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>setEnabled(boolean)</code>。
     */
    @Deprecated
    public void enable() {
        if (!enabled) {
            synchronized (getTreeLock()) {
                enabled = true;
                ComponentPeer peer = this.peer;
                if (peer != null) {
                    peer.setEnabled(true);
                    if (visible && !getRecursivelyVisibleBounds().isEmpty()) {
                        updateCursorImmediately();
                    }
                }
            }
            if (accessibleContext != null) {
                accessibleContext.firePropertyChange(
                                                     AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                                     null, AccessibleState.ENABLED);
            }
        }
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>setEnabled(boolean)</code>。
     */
    @Deprecated
    public void enable(boolean b) {
        if (b) {
            enable();
        } else {
            disable();
        }
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>setEnabled(boolean)</code>。
     */
    @Deprecated
    public void disable() {
        if (enabled) {
            KeyboardFocusManager.clearMostRecentFocusOwner(this);
            synchronized (getTreeLock()) {
                enabled = false;
                // 禁用的轻量级容器允许包含焦点所有者。
                if ((isFocusOwner() || (containsFocus() && !isLightweight())) &&
                    KeyboardFocusManager.isAutoFocusTransferEnabled())
                {
                    // 不要清除全局焦点所有者。如果 transferFocus
                    // 失败，我们希望焦点保留在禁用的
                    // 组件上，以便键盘导航等对用户仍有意义。
                    transferFocus(false);
                }
                ComponentPeer peer = this.peer;
                if (peer != null) {
                    peer.setEnabled(false);
                    if (visible && !getRecursivelyVisibleBounds().isEmpty()) {
                        updateCursorImmediately();
                    }
                }
            }
            if (accessibleContext != null) {
                accessibleContext.firePropertyChange(
                                                     AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                                     null, AccessibleState.ENABLED);
            }
        }
    }

    /**
     * 如果此组件绘制到稍后复制到屏幕的离屏图像（“缓冲区”）中，则返回 true。支持双缓冲的组件子类应覆盖此
     * 方法以在启用双缓冲时返回 true。
     *
     * @return 默认情况下返回 false
     */
    public boolean isDoubleBuffered() {
        return false;
    }

    /**
     * 为该组件启用或禁用输入方法支持。如果启用了输入方法支持并且组件还处理键事件，
     * 则传入的事件将提供给当前的输入方法，并且仅在输入方法未消耗它们时才由组件处理或分发给其监听器。
     * 默认情况下，输入方法支持是启用的。
     *
     * @param enable true 以启用，false 以禁用
     * @see #processKeyEvent
     * @since 1.2
     */
    public void enableInputMethods(boolean enable) {
        if (enable) {
            if ((eventMask & AWTEvent.INPUT_METHODS_ENABLED_MASK) != 0)
                return;

            // 如果此组件已获得焦点，则通过分发合成的焦点获得事件来激活
            // 输入方法。
            if (isFocusOwner()) {
                InputContext inputContext = getInputContext();
                if (inputContext != null) {
                    FocusEvent focusGainedEvent =
                        new FocusEvent(this, FocusEvent.FOCUS_GAINED);
                    inputContext.dispatchEvent(focusGainedEvent);
                }
            }

            eventMask |= AWTEvent.INPUT_METHODS_ENABLED_MASK;
        } else {
            if ((eventMask & AWTEvent.INPUT_METHODS_ENABLED_MASK) != 0) {
                InputContext inputContext = getInputContext();
                if (inputContext != null) {
                    inputContext.endComposition();
                    inputContext.removeNotify(this);
                }
            }
            eventMask &= ~AWTEvent.INPUT_METHODS_ENABLED_MASK;
        }
    }

    /**
     * 根据参数 <code>b</code> 的值显示或隐藏此组件。
     * <p>
     * 此方法更改与布局相关的信息，因此会
     * 使组件层次结构失效。
     *
     * @param b  如果 <code>true</code>，显示此组件；
     * 否则，隐藏此组件
     * @see #isVisible
     * @see #invalidate
     * @since JDK1.1
     */
    public void setVisible(boolean b) {
        show(b);
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>setVisible(boolean)</code>。
     */
    @Deprecated
    public void show() {
        if (!visible) {
            synchronized (getTreeLock()) {
                visible = true;
                mixOnShowing();
                ComponentPeer peer = this.peer;
                if (peer != null) {
                    peer.setVisible(true);
                    createHierarchyEvents(HierarchyEvent.HIERARCHY_CHANGED,
                                          this, parent,
                                          HierarchyEvent.SHOWING_CHANGED,
                                          Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_EVENT_MASK));
                    if (peer instanceof LightweightPeer) {
                        repaint();
                    }
                    updateCursorImmediately();
                }

                if (componentListener != null ||
                    (eventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0 ||
                    Toolkit.enabledOnToolkit(AWTEvent.COMPONENT_EVENT_MASK)) {
                    ComponentEvent e = new ComponentEvent(this,
                                                          ComponentEvent.COMPONENT_SHOWN);
                    Toolkit.getEventQueue().postEvent(e);
                }
            }
            Container parent = this.parent;
            if (parent != null) {
                parent.invalidate();
            }
        }
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>setVisible(boolean)</code>。
     */
    @Deprecated
    public void show(boolean b) {
        if (b) {
            show();
        } else {
            hide();
        }
    }

    boolean containsFocus() {
        return isFocusOwner();
    }

    void clearMostRecentFocusOwnerOnHide() {
        KeyboardFocusManager.clearMostRecentFocusOwner(this);
    }

    void clearCurrentFocusCycleRootOnHide() {
        /* do nothing */
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 <code>setVisible(boolean)</code>。
     */
    @Deprecated
    public void hide() {
        isPacked = false;

        if (visible) {
            clearCurrentFocusCycleRootOnHide();
            clearMostRecentFocusOwnerOnHide();
            synchronized (getTreeLock()) {
                visible = false;
                mixOnHiding(isLightweight());
                if (containsFocus() && KeyboardFocusManager.isAutoFocusTransferEnabled()) {
                    transferFocus(true);
                }
                ComponentPeer peer = this.peer;
                if (peer != null) {
                    peer.setVisible(false);
                    createHierarchyEvents(HierarchyEvent.HIERARCHY_CHANGED,
                                          this, parent,
                                          HierarchyEvent.SHOWING_CHANGED,
                                          Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_EVENT_MASK));
                    if (peer instanceof LightweightPeer) {
                        repaint();
                    }
                    updateCursorImmediately();
                }
                if (componentListener != null ||
                    (eventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0 ||
                    Toolkit.enabledOnToolkit(AWTEvent.COMPONENT_EVENT_MASK)) {
                    ComponentEvent e = new ComponentEvent(this,
                                                          ComponentEvent.COMPONENT_HIDDEN);
                    Toolkit.getEventQueue().postEvent(e);
                }
            }
            Container parent = this.parent;
            if (parent != null) {
                parent.invalidate();
            }
        }
    }

    /**
     * 获取此组件的前景色。
     * @return 此组件的前景色；如果此组件没有前景色，则返回其父组件的前景色
     * @see #setForeground
     * @since JDK1.0
     * @beaninfo
     *       bound: true
     */
    @Transient
    public Color getForeground() {
        Color foreground = this.foreground;
        if (foreground != null) {
            return foreground;
        }
        Container parent = this.parent;
        return (parent != null) ? parent.getForeground() : null;
    }

    /**
     * 设置此组件的前景色。
     * @param c 要成为此组件前景色的颜色；如果此参数为 <code>null</code>
     *          则此组件将继承其父组件的前景色
     * @see #getForeground
     * @since JDK1.0
     */
    public void setForeground(Color c) {
        Color oldColor = foreground;
        ComponentPeer peer = this.peer;
        foreground = c;
        if (peer != null) {
            c = getForeground();
            if (c != null) {
                peer.setForeground(c);
            }
        }
        // 这是一个绑定属性，因此向
        // 任何已注册的监听器报告更改。（如果没有监听器则廉价。）
        firePropertyChange("foreground", oldColor, c);
    }

    /**
     * 返回是否为该 Component 显式设置了前景色。如果此方法返回 <code>false</code>，则该 Component 从其祖先继承前景色。
     *
     * @return <code>true</code> 如果为该 Component 显式设置了前景色；<code>false</code> 否则。
     * @since 1.4
     */
    public boolean isForegroundSet() {
        return (foreground != null);
    }

    /**
     * 获取此组件的背景色。
     * @return 此组件的背景色；如果此组件没有背景色，
     *          则返回其父组件的背景色
     * @see #setBackground
     * @since JDK1.0
     */
    @Transient
    public Color getBackground() {
        Color background = this.background;
        if (background != null) {
            return background;
        }
        Container parent = this.parent;
        return (parent != null) ? parent.getBackground() : null;
    }

    /**
     * 设置此组件的背景色。
     * <p>
     * 背景色对每个组件的影响不同，背景色影响的组件部分
     * 可能在不同的操作系统中有所不同。
     *
     * @param c 要成为此组件颜色的颜色；
     *          如果此参数为 <code>null</code>，则此
     *          组件将继承其父组件的背景色
     * @see #getBackground
     * @since JDK1.0
     * @beaninfo
     *       bound: true
     */
    public void setBackground(Color c) {
        Color oldColor = background;
        ComponentPeer peer = this.peer;
        background = c;
        if (peer != null) {
            c = getBackground();
            if (c != null) {
                peer.setBackground(c);
            }
        }
        // 这是一个绑定属性，因此向
        // 任何已注册的监听器报告更改。（如果没有监听器则廉价。）
        firePropertyChange("background", oldColor, c);
    }

    /**
     * 返回是否为该 Component 显式设置了背景色。如果此方法返回 <code>false</code>，则该 Component 从其祖先继承背景色。
     *
     * @return <code>true</code> 如果为该 Component 显式设置了背景色；<code>false</code> 否则。
     * @since 1.4
     */
    public boolean isBackgroundSet() {
        return (background != null);
    }


                /**
     * 获取此组件的字体。
     * @return 此组件的字体；如果未为此组件设置字体，则返回其父组件的字体
     * @see #setFont
     * @since JDK1.0
     */
    @Transient
    public Font getFont() {
        return getFont_NoClientCode();
    }

    // 注意：此方法可能被特权线程调用。
    //      此功能实现在一个包私有方法中
    //      以确保它不能被客户端子类覆盖。
    //      不要在该线程上调用客户端代码！
    final Font getFont_NoClientCode() {
        Font font = this.font;
        if (font != null) {
            return font;
        }
        Container parent = this.parent;
        return (parent != null) ? parent.getFont_NoClientCode() : null;
    }

    /**
     * 设置此组件的字体。
     * <p>
     * 此方法更改与布局相关的信息，因此，
     * 使组件层次结构无效。
     *
     * @param f 要成为此组件字体的字体；
     *          如果此参数为 <code>null</code> 则此
     *          组件将继承其父组件的字体
     * @see #getFont
     * @see #invalidate
     * @since JDK1.0
     * @beaninfo
     *       bound: true
     */
    public void setFont(Font f) {
        Font oldFont, newFont;
        synchronized(getTreeLock()) {
            oldFont = font;
            newFont = font = f;
            ComponentPeer peer = this.peer;
            if (peer != null) {
                f = getFont();
                if (f != null) {
                    peer.setFont(f);
                    peerFont = f;
                }
            }
        }
        // 这是一个绑定属性，因此向
        // 任何已注册的监听器报告更改。  （如果没有监听器则很便宜。）
        firePropertyChange("font", oldFont, newFont);

        // 这可能会改变组件的首选大小。
        // 修复 6213660。 应该比较旧的和新的字体，如果它们相等，则不要
        // 调用 invalidate()。
        if (f != oldFont && (oldFont == null ||
                                      !oldFont.equals(f))) {
            invalidateIfValid();
        }
    }

    /**
     * 返回是否已为此组件显式设置了字体。如果
     * 此方法返回 <code>false</code>，则此组件从祖先继承其
     * 字体。
     *
     * @return <code>true</code> 如果已为此组件显式设置字体； <code>false</code> 否则。
     * @since 1.4
     */
    public boolean isFontSet() {
        return (font != null);
    }

    /**
     * 获取此组件的区域设置。
     * @return 此组件的区域设置；如果此组件没有
     *          区域设置，则返回其父组件的区域设置
     * @see #setLocale
     * @exception IllegalComponentStateException 如果 <code>Component</code>
     *          没有其自己的区域设置并且尚未添加到
     *          区域设置可以从包含的父组件确定的包含层次结构中
     * @since  JDK1.1
     */
    public Locale getLocale() {
        Locale locale = this.locale;
        if (locale != null) {
            return locale;
        }
        Container parent = this.parent;

        if (parent == null) {
            throw new IllegalComponentStateException("此组件必须有一个父组件才能确定其区域设置");
        } else {
            return parent.getLocale();
        }
    }

    /**
     * 设置此组件的区域设置。这是一个绑定属性。
     * <p>
     * 此方法更改与布局相关的信息，因此，
     * 使组件层次结构无效。
     *
     * @param l 要成为此组件区域设置的区域设置
     * @see #getLocale
     * @see #invalidate
     * @since JDK1.1
     */
    public void setLocale(Locale l) {
        Locale oldValue = locale;
        locale = l;

        // 这是一个绑定属性，因此向
        // 任何已注册的监听器报告更改。  （如果没有监听器则很便宜。）
        firePropertyChange("locale", oldValue, l);

        // 这可能会改变组件的首选大小。
        invalidateIfValid();
    }

    /**
     * 获取用于在输出设备上显示
     * 组件的 <code>ColorModel</code> 实例。
     * @return 由此组件使用的颜色模型
     * @see java.awt.image.ColorModel
     * @see java.awt.peer.ComponentPeer#getColorModel()
     * @see Toolkit#getColorModel()
     * @since JDK1.0
     */
    public ColorModel getColorModel() {
        ComponentPeer peer = this.peer;
        if ((peer != null) && ! (peer instanceof LightweightPeer)) {
            return peer.getColorModel();
        } else if (GraphicsEnvironment.isHeadless()) {
            return ColorModel.getRGBdefault();
        } // else
        return getToolkit().getColorModel();
    }

    /**
     * 以一个点的形式获取此组件的位置，
     * 该点指定组件的左上角。
     * 该位置将相对于父组件的坐标空间。
     * <p>
     * 由于本机事件处理的异步性质，此
     * 方法可以返回过时的值（例如，在多次调用
     * <code>setLocation()</code> 之后）。 因此，获取组件位置的推荐方法是
     * 在 <code>java.awt.event.ComponentListener.componentMoved()</code> 中，
     * 该方法在操作系统完成移动组件后调用。
     * </p>
     * @return 一个 <code>Point</code> 实例，表示
     *          组件边界左上角在
     *          组件父级坐标空间中的位置
     * @see #setLocation
     * @see #getLocationOnScreen
     * @since JDK1.1
     */
    public Point getLocation() {
        return location();
    }

    /**
     * 以一个点的形式获取此组件的位置，
     * 该点指定组件在屏幕坐标空间中的左上角。
     * @return 一个 <code>Point</code> 实例，表示
     *          组件边界左上角在屏幕
     *          坐标空间中的位置
     * @throws IllegalComponentStateException 如果组件未显示在屏幕上
     * @see #setLocation
     * @see #getLocation
     */
    public Point getLocationOnScreen() {
        synchronized (getTreeLock()) {
            return getLocationOnScreen_NoTreeLock();
        }
    }

    /*
     * 一个包私有的 getLocationOnScreen 版本
     * 由 GlobalCursormanager 用于更新光标
     */
    final Point getLocationOnScreen_NoTreeLock() {

        if (peer != null && isShowing()) {
            if (peer instanceof LightweightPeer) {
                // 轻量级组件的位置需要相对于本机组件进行转换。
                Container host = getNativeContainer();
                Point pt = host.peer.getLocationOnScreen();
                for(Component c = this; c != host; c = c.getParent()) {
                    pt.x += c.x;
                    pt.y += c.y;
                }
                return pt;
            } else {
                Point pt = peer.getLocationOnScreen();
                return pt;
            }
        } else {
            throw new IllegalComponentStateException("组件必须显示在屏幕上才能确定其位置");
        }
    }


    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getLocation()</code>。
     */
    @Deprecated
    public Point location() {
        return location_NoClientCode();
    }

    private Point location_NoClientCode() {
        return new Point(x, y);
    }

    /**
     * 将此组件移动到新位置。新位置的左上角
     * 由 <code>x</code> 和 <code>y</code> 参数指定，这些参数在
     * 此组件父级的坐标空间中。
     * <p>
     * 此方法更改与布局相关的信息，因此，
     * 使组件层次结构无效。
     *
     * @param x 新位置左上角的 <i>x</i>-坐标
     *          在父级坐标空间中
     * @param y 新位置左上角的 <i>y</i>-坐标
     *          在父级坐标空间中
     * @see #getLocation
     * @see #setBounds
     * @see #invalidate
     * @since JDK1.1
     */
    public void setLocation(int x, int y) {
        move(x, y);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>setLocation(int, int)</code>。
     */
    @Deprecated
    public void move(int x, int y) {
        synchronized(getTreeLock()) {
            setBoundsOp(ComponentPeer.SET_LOCATION);
            setBounds(x, y, width, height);
        }
    }

    /**
     * 将此组件移动到新位置。新位置的左上角
     * 由点 <code>p</code> 指定。点
     * <code>p</code> 在父级的坐标空间中给出。
     * <p>
     * 此方法更改与布局相关的信息，因此，
     * 使组件层次结构无效。
     *
     * @param p 定义新位置左上角的点
     *          在此组件父级的坐标空间中给出
     * @see #getLocation
     * @see #setBounds
     * @see #invalidate
     * @since JDK1.1
     */
    public void setLocation(Point p) {
        setLocation(p.x, p.y);
    }

    /**
     * 以 <code>Dimension</code> 对象的形式返回此组件的大小。
     * <code>Dimension</code> 对象的 <code>height</code>
     * 字段包含此组件的高度，<code>width</code>
     * 字段包含此组件的宽度。
     * @return 一个 <code>Dimension</code> 对象，指示
     *          此组件的大小
     * @see #setSize
     * @since JDK1.1
     */
    public Dimension getSize() {
        return size();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getSize()</code>。
     */
    @Deprecated
    public Dimension size() {
        return new Dimension(width, height);
    }

    /**
     * 调整此组件的大小，使其具有宽度 <code>width</code>
     * 和高度 <code>height</code>。
     * <p>
     * 此方法更改与布局相关的信息，因此，
     * 使组件层次结构无效。
     *
     * @param width 此组件的新宽度（以像素为单位）
     * @param height 此组件的新高度（以像素为单位）
     * @see #getSize
     * @see #setBounds
     * @see #invalidate
     * @since JDK1.1
     */
    public void setSize(int width, int height) {
        resize(width, height);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>setSize(int, int)</code>。
     */
    @Deprecated
    public void resize(int width, int height) {
        synchronized(getTreeLock()) {
            setBoundsOp(ComponentPeer.SET_SIZE);
            setBounds(x, y, width, height);
        }
    }

    /**
     * 调整此组件的大小，使其具有宽度 <code>d.width</code>
     * 和高度 <code>d.height</code>。
     * <p>
     * 此方法更改与布局相关的信息，因此，
     * 使组件层次结构无效。
     *
     * @param d 指定新大小的维度
     * @throws NullPointerException 如果 {@code d} 为 {@code null}
     * @see #setSize
     * @see #setBounds
     * @see #invalidate
     * @since JDK1.1
     */
    public void setSize(Dimension d) {
        resize(d);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>setSize(Dimension)</code>。
     */
    @Deprecated
    public void resize(Dimension d) {
        setSize(d.width, d.height);
    }

    /**
     * 以 <code>Rectangle</code> 对象的形式获取此组件的边界。边界指定此
     * 组件的宽度、高度和相对于其父级的位置。
     * @return 表示此组件边界的矩形
     * @see #setBounds
     * @see #getLocation
     * @see #getSize
     */
    public Rectangle getBounds() {
        return bounds();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getBounds()</code>。
     */
    @Deprecated
    public Rectangle bounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * 移动并调整此组件的大小。新位置的左上角
     * 由 <code>x</code> 和 <code>y</code> 指定，新大小
     * 由 <code>width</code> 和 <code>height</code> 指定。
     * <p>
     * 此方法更改与布局相关的信息，因此，
     * 使组件层次结构无效。
     *
     * @param x 此组件的新 <i>x</i>-坐标
     * @param y 此组件的新 <i>y</i>-坐标
     * @param width 此组件的新 <code>width</code>
     * @param height 此组件的新 <code>height</code>
     * @see #getBounds
     * @see #setLocation(int, int)
     * @see #setLocation(Point)
     * @see #setSize(int, int)
     * @see #setSize(Dimension)
     * @see #invalidate
     * @since JDK1.1
     */
    public void setBounds(int x, int y, int width, int height) {
        reshape(x, y, width, height);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>setBounds(int, int, int, int)</code>。
     */
    @Deprecated
    public void reshape(int x, int y, int width, int height) {
        synchronized (getTreeLock()) {
            try {
                setBoundsOp(ComponentPeer.SET_BOUNDS);
                boolean resized = (this.width != width) || (this.height != height);
                boolean moved = (this.x != x) || (this.y != y);
                if (!resized && !moved) {
                    return;
                }
                int oldX = this.x;
                int oldY = this.y;
                int oldWidth = this.width;
                int oldHeight = this.height;
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;

                if (resized) {
                    isPacked = false;
                }

                boolean needNotify = true;
                mixOnReshaping();
                if (peer != null) {
                    // LightweightPeer 是一个空的存根，因此可以跳过 peer.reshape
                    if (!(peer instanceof LightweightPeer)) {
                        reshapeNativePeer(x, y, width, height, getBoundsOp());
                        // 检查 peer 实际上是否更改了坐标
                        resized = (oldWidth != this.width) || (oldHeight != this.height);
                        moved = (oldX != this.x) || (oldY != this.y);
                        // 修复 5025858：不要在此处发送顶级
                        // 窗口的 ComponentEvents，因为它们是从 peer 或本机代码发送的
                        // 当窗口真正调整大小或移动时，否则可能会发送两次
                        // 一些事件
                        if (this instanceof Window) {
                            needNotify = false;
                        }
                    }
                    if (resized) {
                        invalidate();
                    }
                    if (parent != null) {
                        parent.invalidateIfValid();
                    }
                }
                if (needNotify) {
                    notifyNewBounds(resized, moved);
                }
                repaintParentIfNeeded(oldX, oldY, oldWidth, oldHeight);
            } finally {
                setBoundsOp(ComponentPeer.RESET_OPERATION);
            }
        }
    }


                private void repaintParentIfNeeded(int oldX, int oldY, int oldWidth,
                                       int oldHeight)
    {
        if (parent != null && peer instanceof LightweightPeer && isShowing()) {
            // 使父组件重绘此组件占据的区域。
            parent.repaint(oldX, oldY, oldWidth, oldHeight);
            // 使父组件重绘此组件现在占据的区域。
            repaint();
        }
    }

    private void reshapeNativePeer(int x, int y, int width, int height, int op) {
        // 本地对等体可能由于父组件是轻量级组件而偏移更多。
        int nativeX = x;
        int nativeY = y;
        for (Component c = parent;
             (c != null) && (c.peer instanceof LightweightPeer);
             c = c.parent)
        {
            nativeX += c.x;
            nativeY += c.y;
        }
        peer.setBounds(nativeX, nativeY, width, height, op);
    }

    @SuppressWarnings("deprecation")
    private void notifyNewBounds(boolean resized, boolean moved) {
        if (componentListener != null
            || (eventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0
            || Toolkit.enabledOnToolkit(AWTEvent.COMPONENT_EVENT_MASK))
            {
                if (resized) {
                    ComponentEvent e = new ComponentEvent(this,
                                                          ComponentEvent.COMPONENT_RESIZED);
                    Toolkit.getEventQueue().postEvent(e);
                }
                if (moved) {
                    ComponentEvent e = new ComponentEvent(this,
                                                          ComponentEvent.COMPONENT_MOVED);
                    Toolkit.getEventQueue().postEvent(e);
                }
            } else {
                if (this instanceof Container && ((Container)this).countComponents() > 0) {
                    boolean enabledOnToolkit =
                        Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK);
                    if (resized) {

                        ((Container)this).createChildHierarchyEvents(
                                                                     HierarchyEvent.ANCESTOR_RESIZED, 0, enabledOnToolkit);
                    }
                    if (moved) {
                        ((Container)this).createChildHierarchyEvents(
                                                                     HierarchyEvent.ANCESTOR_MOVED, 0, enabledOnToolkit);
                    }
                }
                }
    }

    /**
     * 将此组件移动并调整大小以符合新的边界矩形 <code>r</code>。此组件的新位置由 <code>r.x</code> 和 <code>r.y</code> 指定，
     * 新的大小由 <code>r.width</code> 和 <code>r.height</code> 指定。
     * <p>
     * 此方法更改与布局相关的信息，因此会使组件层次结构失效。
     *
     * @param r 此组件的新边界矩形
     * @throws NullPointerException 如果 {@code r} 为 {@code null}
     * @see       #getBounds
     * @see       #setLocation(int, int)
     * @see       #setLocation(Point)
     * @see       #setSize(int, int)
     * @see       #setSize(Dimension)
     * @see #invalidate
     * @since     JDK1.1
     */
    public void setBounds(Rectangle r) {
        setBounds(r.x, r.y, r.width, r.height);
    }


    /**
     * 返回组件原点的当前 x 坐标。
     * 该方法优于编写 <code>component.getBounds().x</code> 或 <code>component.getLocation().x</code>，
     * 因为它不会导致堆分配。
     *
     * @return 组件原点的当前 x 坐标
     * @since 1.2
     */
    public int getX() {
        return x;
    }


    /**
     * 返回组件原点的当前 y 坐标。
     * 该方法优于编写 <code>component.getBounds().y</code> 或 <code>component.getLocation().y</code>，
     * 因为它不会导致堆分配。
     *
     * @return 组件原点的当前 y 坐标
     * @since 1.2
     */
    public int getY() {
        return y;
    }


    /**
     * 返回此组件的当前宽度。
     * 该方法优于编写 <code>component.getBounds().width</code> 或 <code>component.getSize().width</code>，
     * 因为它不会导致堆分配。
     *
     * @return 此组件的当前宽度
     * @since 1.2
     */
    public int getWidth() {
        return width;
    }


    /**
     * 返回此组件的当前高度。
     * 该方法优于编写 <code>component.getBounds().height</code> 或 <code>component.getSize().height</code>，
     * 因为它不会导致堆分配。
     *
     * @return 此组件的当前高度
     * @since 1.2
     */
    public int getHeight() {
        return height;
    }

    /**
     * 将此组件的边界存储到“返回值” <b>rv</b> 中并返回 <b>rv</b>。如果 rv 为 <code>null</code>，则分配新的 <code>Rectangle</code>。
     * 此版本的 <code>getBounds</code> 对于希望避免在堆上分配新的 <code>Rectangle</code> 对象的调用者非常有用。
     *
     * @param rv 返回值，修改为组件的边界
     * @return rv
     */
    public Rectangle getBounds(Rectangle rv) {
        if (rv == null) {
            return new Rectangle(getX(), getY(), getWidth(), getHeight());
        }
        else {
            rv.setBounds(getX(), getY(), getWidth(), getHeight());
            return rv;
        }
    }

    /**
     * 将此组件的宽度/高度存储到“返回值” <b>rv</b> 中并返回 <b>rv</b>。如果 rv 为 <code>null</code>，则分配新的 <code>Dimension</code> 对象。
     * 此版本的 <code>getSize</code> 对于希望避免在堆上分配新的 <code>Dimension</code> 对象的调用者非常有用。
     *
     * @param rv 返回值，修改为组件的大小
     * @return rv
     */
    public Dimension getSize(Dimension rv) {
        if (rv == null) {
            return new Dimension(getWidth(), getHeight());
        }
        else {
            rv.setSize(getWidth(), getHeight());
            return rv;
        }
    }

    /**
     * 将此组件的 x,y 原点存储到“返回值” <b>rv</b> 中并返回 <b>rv</b>。如果 rv 为 <code>null</code>，则分配新的 <code>Point</code>。
     * 此版本的 <code>getLocation</code> 对于希望避免在堆上分配新的 <code>Point</code> 对象的调用者非常有用。
     *
     * @param rv 返回值，修改为组件的位置
     * @return rv
     */
    public Point getLocation(Point rv) {
        if (rv == null) {
            return new Point(getX(), getY());
        }
        else {
            rv.setLocation(getX(), getY());
            return rv;
        }
    }

    /**
     * 如果此组件完全不透明，则返回 true，默认返回 false。
     * <p>
     * 完全不透明的组件会绘制其矩形区域内的每个像素。非完全不透明的组件仅绘制部分像素，允许其下方的像素“显示”。
     * 因此，不完全绘制其像素的组件提供一定程度的透明度。
     * <p>
     * 保证始终完全绘制其内容的子类应重写此方法并返回 true。
     *
     * @return 如果此组件完全不透明，则返回 true
     * @see #isLightweight
     * @since 1.2
     */
    public boolean isOpaque() {
        if (getPeer() == null) {
            return false;
        }
        else {
            return !isLightweight();
        }
    }


    /**
     * 轻量级组件没有本地工具包对等体。
     * <code>Component</code> 和 <code>Container</code> 的子类，除了本包中定义的 <code>Button</code> 或 <code>Scrollbar</code>，
     * 都是轻量级组件。所有 Swing 组件都是轻量级组件。
     * <p>
     * 如果此组件不可显示，则此方法将始终返回 <code>false</code>，因为无法确定不可显示组件的权重。
     *
     * @return 如果此组件具有轻量级对等体，则返回 true；如果具有本地对等体或没有对等体，则返回 false
     * @see #isDisplayable
     * @since 1.2
     */
    public boolean isLightweight() {
        return getPeer() instanceof LightweightPeer;
    }


    /**
     * 将此组件的首选大小设置为常量值。后续调用 <code>getPreferredSize</code> 将始终返回此值。
     * 将首选大小设置为 <code>null</code> 会恢复默认行为。
     *
     * @param preferredSize 新的首选大小，或 null
     * @see #getPreferredSize
     * @see #isPreferredSizeSet
     * @since 1.5
     */
    public void setPreferredSize(Dimension preferredSize) {
        Dimension old;
        // 如果设置了首选大小，则使用它作为旧值，否则使用 null 表示我们之前没有设置过首选大小。
        if (prefSizeSet) {
            old = this.prefSize;
        }
        else {
            old = null;
        }
        this.prefSize = preferredSize;
        prefSizeSet = (preferredSize != null);
        firePropertyChange("preferredSize", old, preferredSize);
    }


    /**
     * 如果已将首选大小设置为非 <code>null</code> 值，则返回 true，否则返回 false。
     *
     * @return 如果已调用 <code>setPreferredSize</code> 并传递了非 null 值，则返回 true。
     * @since 1.5
     */
    public boolean isPreferredSizeSet() {
        return prefSizeSet;
    }


    /**
     * 获取此组件的首选大小。
     * @return 表示此组件首选大小的维度对象
     * @see #getMinimumSize
     * @see LayoutManager
     */
    public Dimension getPreferredSize() {
        return preferredSize();
    }


    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getPreferredSize()</code>。
     */
    @Deprecated
    public Dimension preferredSize() {
        /* 如果有合理的缓存大小值可用，则避免获取锁。 */
        Dimension dim = prefSize;
        if (dim == null || !(isPreferredSizeSet() || isValid())) {
            synchronized (getTreeLock()) {
                prefSize = (peer != null) ?
                    peer.getPreferredSize() :
                    getMinimumSize();
                dim = prefSize;
            }
        }
        return new Dimension(dim);
    }

    /**
     * 将此组件的最小大小设置为常量值。后续调用 <code>getMinimumSize</code> 将始终返回此值。
     * 将最小大小设置为 <code>null</code> 会恢复默认行为。
     *
     * @param minimumSize 此组件的新最小大小
     * @see #getMinimumSize
     * @see #isMinimumSizeSet
     * @since 1.5
     */
    public void setMinimumSize(Dimension minimumSize) {
        Dimension old;
        // 如果设置了最小大小，则使用它作为旧值，否则使用 null 表示我们之前没有设置过最小大小。
        if (minSizeSet) {
            old = this.minSize;
        }
        else {
            old = null;
        }
        this.minSize = minimumSize;
        minSizeSet = (minimumSize != null);
        firePropertyChange("minimumSize", old, minimumSize);
    }

    /**
     * 返回是否已调用 <code>setMinimumSize</code> 并传递了非 null 值。
     *
     * @return 如果已调用 <code>setMinimumSize</code> 并传递了非 null 值，则返回 true。
     * @since 1.5
     */
    public boolean isMinimumSizeSet() {
        return minSizeSet;
    }

    /**
     * 获取此组件的最小大小。
     * @return 表示此组件最小大小的维度对象
     * @see #getPreferredSize
     * @see LayoutManager
     */
    public Dimension getMinimumSize() {
        return minimumSize();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>getMinimumSize()</code>。
     */
    @Deprecated
    public Dimension minimumSize() {
        /* 如果有合理的缓存大小值可用，则避免获取锁。 */
        Dimension dim = minSize;
        if (dim == null || !(isMinimumSizeSet() || isValid())) {
            synchronized (getTreeLock()) {
                minSize = (peer != null) ?
                    peer.getMinimumSize() :
                    size();
                dim = minSize;
            }
        }
        return new Dimension(dim);
    }

    /**
     * 将此组件的最大大小设置为常量值。后续调用 <code>getMaximumSize</code> 将始终返回此值。
     * 将最大大小设置为 <code>null</code> 会恢复默认行为。
     *
     * @param maximumSize 包含所需最大允许大小的 <code>Dimension</code>
     * @see #getMaximumSize
     * @see #isMaximumSizeSet
     * @since 1.5
     */
    public void setMaximumSize(Dimension maximumSize) {
        // 如果设置了最大大小，则使用它作为旧值，否则使用 null 表示我们之前没有设置过最大大小。
        Dimension old;
        if (maxSizeSet) {
            old = this.maxSize;
        }
        else {
            old = null;
        }
        this.maxSize = maximumSize;
        maxSizeSet = (maximumSize != null);
        firePropertyChange("maximumSize", old, maximumSize);
    }

    /**
     * 如果最大大小已设置为非 <code>null</code> 值，则返回 true，否则返回 false。
     *
     * @return 如果 <code>maximumSize</code> 为非 <code>null</code>，则返回 true，否则返回 false
     * @since 1.5
     */
    public boolean isMaximumSizeSet() {
        return maxSizeSet;
    }

    /**
     * 获取此组件的最大大小。
     * @return 表示此组件最大大小的维度对象
     * @see #getMinimumSize
     * @see #getPreferredSize
     * @see LayoutManager
     */
    public Dimension getMaximumSize() {
        if (isMaximumSizeSet()) {
            return new Dimension(maxSize);
        }
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }

    /**
     * 返回 x 轴上的对齐方式。这指定了组件相对于其他组件的对齐方式。值应在 0 到 1 之间，
     * 其中 0 表示沿原点对齐，1 表示远离原点对齐，0.5 表示居中，等等。
     */
    public float getAlignmentX() {
        return CENTER_ALIGNMENT;
    }

    /**
     * 返回 y 轴上的对齐方式。这指定了组件相对于其他组件的对齐方式。值应在 0 到 1 之间，
     * 其中 0 表示沿原点对齐，1 表示远离原点对齐，0.5 表示居中，等等。
     */
    public float getAlignmentY() {
        return CENTER_ALIGNMENT;
    }


                /**
     * 返回基线。基线从组件的顶部测量。此方法主要用于
     * <code>LayoutManager</code> 沿基线对齐组件。返回值小于 0 表示此组件
     * 没有合理的基线，<code>LayoutManager</code> 不应沿其基线对齐此组件。
     * <p>
     * 默认实现返回 -1。支持基线的子类应适当重写。如果返回值 &gt;= 0，
     * 则该组件在任何大小 &gt;= 最小大小时都有有效的基线，并且可以使用
     * <code>getBaselineResizeBehavior</code> 确定基线如何随大小变化。
     *
     * @param width 要获取基线的宽度
     * @param height 要获取基线的高度
     * @return 基线或 &lt; 0 表示没有合理的基线
     * @throws IllegalArgumentException 如果宽度或高度 &lt; 0
     * @see #getBaselineResizeBehavior
     * @see java.awt.FontMetrics
     * @since 1.6
     */
    public int getBaseline(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException(
                    "Width and height must be >= 0");
        }
        return -1;
    }

    /**
     * 返回一个枚举，指示组件的基线如何随大小变化。此方法主要用于
     * 布局管理器和 GUI 构建器。
     * <p>
     * 默认实现返回
     * <code>BaselineResizeBehavior.OTHER</code>。支持基线的子类应适当重写。子类
     * 不应返回 <code>null</code>；如果无法计算基线，应返回
     * <code>BaselineResizeBehavior.OTHER</code>。调用者应首先使用
     * <code>getBaseline</code> 获取基线，如果返回值 &gt;= 0，则使用此方法。即使
     * <code>getBaseline</code> 返回值小于 0，此方法返回一个值不同于
     * <code>BaselineResizeBehavior.OTHER</code> 也是可以接受的。
     *
     * @return 一个枚举，指示组件大小变化时基线如何变化
     * @see #getBaseline(int, int)
     * @since 1.6
     */
    public BaselineResizeBehavior getBaselineResizeBehavior() {
        return BaselineResizeBehavior.OTHER;
    }

    /**
     * 提示布局管理器布局此组件。这通常在组件（更具体地说，容器）
     * 被验证时调用。
     * @see #validate
     * @see LayoutManager
     */
    public void doLayout() {
        layout();
    }

    /**
     * @deprecated 自 JDK 1.1 起，
     * 被 <code>doLayout()</code> 替代。
     */
    @Deprecated
    public void layout() {
    }

    /**
     * 验证此组件。
     * <p>
     * “验证”的含义由该类的祖先定义。请参阅 {@link Container#validate} 了解更多信息。
     *
     * @see       #invalidate
     * @see       #doLayout()
     * @see       LayoutManager
     * @see       Container#validate
     * @since     JDK1.0
     */
    public void validate() {
        synchronized (getTreeLock()) {
            ComponentPeer peer = this.peer;
            boolean wasValid = isValid();
            if (!wasValid && peer != null) {
                Font newfont = getFont();
                Font oldfont = peerFont;
                if (newfont != oldfont && (oldfont == null
                                           || !oldfont.equals(newfont))) {
                    peer.setFont(newfont);
                    peerFont = newfont;
                }
                peer.layout();
            }
            valid = true;
            if (!wasValid) {
                mixOnValidating();
            }
        }
    }

    /**
     * 使此组件及其祖先无效。
     * <p>
     * 默认情况下，组件层次结构中的所有祖先都会被标记为无效，直到层次结构的最顶层容器。如果
     * {@code java.awt.smartInvalidate} 系统属性设置为 {@code true}，
     * 无效化将在该组件的最近验证根处停止。将容器标记为 <i>无效</i> 表示该容器需要重新布局。
     * <p>
     * 当任何布局相关的信息发生变化时（例如，设置组件的边界，或将组件添加到容器中），此方法会被自动调用。
     * <p>
     * 此方法可能会被频繁调用，因此应快速执行。
     *
     * @see       #validate
     * @see       #doLayout
     * @see       LayoutManager
     * @see       java.awt.Container#isValidateRoot
     * @since     JDK1.0
     */
    public void invalidate() {
        synchronized (getTreeLock()) {
            /* 清除缓存的布局和大小信息。
             * 为了提高效率，仅在其他组件尚未首先执行此操作时才向上传播 invalidate()。
             */
            valid = false;
            if (!isPreferredSizeSet()) {
                prefSize = null;
            }
            if (!isMinimumSizeSet()) {
                minSize = null;
            }
            if (!isMaximumSizeSet()) {
                maxSize = null;
            }
            invalidateParent();
        }
    }

    /**
     * 使此组件的父组件无效（如果有的话）。
     *
     * 此方法必须在 TreeLock 下调用。
     */
    void invalidateParent() {
        if (parent != null) {
            parent.invalidateIfValid();
        }
    }

    /** 使组件无效，除非它已经是无效的。
     */
    final void invalidateIfValid() {
        if (isValid()) {
            invalidate();
        }
    }

    /**
     * 重新验证组件层次结构，直到最近的验证根。
     * <p>
     * 此方法首先从该组件开始，使组件层次结构无效，直到最近的验证根。之后，从最近的验证根开始验证组件层次结构。
     * <p>
     * 这是一个方便方法，旨在帮助应用程序开发人员避免手动查找验证根。基本上，这相当于首先调用此组件上的
     * {@link #invalidate()} 方法，然后调用最近验证根上的 {@link #validate()} 方法。
     *
     * @see Container#isValidateRoot
     * @since 1.7
     */
    public void revalidate() {
        revalidateSynchronously();
    }

    /**
     * 同步重新验证组件。
     */
    final void revalidateSynchronously() {
        synchronized (getTreeLock()) {
            invalidate();

            Container root = getContainer();
            if (root == null) {
                // 没有父组件。仅验证自身。
                validate();
            } else {
                while (!root.isValidateRoot()) {
                    if (root.getContainer() == null) {
                        // 如果没有验证根，我们将验证最顶层的容器
                        break;
                    }

                    root = root.getContainer();
                }

                root.validate();
            }
        }
    }

    /**
     * 为该组件创建图形上下文。如果该组件当前不可显示，此方法将返回 <code>null</code>。
     * @return 该组件的图形上下文，或 <code>null</code>（如果它没有图形上下文）
     * @see       #paint
     * @since     JDK1.0
     */
    public Graphics getGraphics() {
        if (peer instanceof LightweightPeer) {
            // 这是一个轻量级组件，需要
            // 转换坐标空间并相对于父组件裁剪。
            if (parent == null) return null;
            Graphics g = parent.getGraphics();
            if (g == null) return null;
            if (g instanceof ConstrainableGraphics) {
                ((ConstrainableGraphics) g).constrain(x, y, width, height);
            } else {
                g.translate(x,y);
                g.setClip(0, 0, width, height);
            }
            g.setFont(getFont());
            return g;
        } else {
            ComponentPeer peer = this.peer;
            return (peer != null) ? peer.getGraphics() : null;
        }
    }

    final Graphics getGraphics_NoClientCode() {
        ComponentPeer peer = this.peer;
        if (peer instanceof LightweightPeer) {
            // 这是一个轻量级组件，需要
            // 转换坐标空间并相对于父组件裁剪。
            Container parent = this.parent;
            if (parent == null) return null;
            Graphics g = parent.getGraphics_NoClientCode();
            if (g == null) return null;
            if (g instanceof ConstrainableGraphics) {
                ((ConstrainableGraphics) g).constrain(x, y, width, height);
            } else {
                g.translate(x,y);
                g.setClip(0, 0, width, height);
            }
            g.setFont(getFont_NoClientCode());
            return g;
        } else {
            return (peer != null) ? peer.getGraphics() : null;
        }
    }

    /**
     * 获取指定字体的字体度量。
     * 警告：由于字体度量受
     * {@link java.awt.font.FontRenderContext FontRenderContext} 的影响，
     * 而此方法不提供一个，因此它只能返回默认渲染上下文的度量，这可能与使用
     * {@link Graphics2D} 功能时在组件上渲染时使用的度量不匹配。相反，可以在渲染时通过调用
     * {@link Graphics#getFontMetrics()} 或 {@link Font Font} 类的文本测量 API 获取度量。
     * @param font 要获取字体度量的字体
     * @return <code>font</code> 的字体度量
     * @see       #getFont
     * @see       #getPeer
     * @see       java.awt.peer.ComponentPeer#getFontMetrics(Font)
     * @see       Toolkit#getFontMetrics(Font)
     * @since     JDK1.0
     */
    public FontMetrics getFontMetrics(Font font) {
        // 这是一个不受支持的 hack，但为了客户保留。不要删除。
        FontManager fm = FontManagerFactory.getInstance();
        if (fm instanceof SunFontManager
            && ((SunFontManager) fm).usePlatformFontMetrics()) {

            if (peer != null &&
                !(peer instanceof LightweightPeer)) {
                return peer.getFontMetrics(font);
            }
        }
        return sun.font.FontDesignMetrics.getMetrics(font);
    }

    /**
     * 设置指定的光标图像。当此组件的 <code>contains</code> 方法返回 true 时，
     * 且此组件可见、可显示且启用时，将显示此光标图像。设置容器的光标将导致该光标在容器的所有子组件中显示，
     * 除非这些子组件设置了非 <code>null</code> 的光标。
     * <p>
     * 如果 Java 平台实现和/或本机系统不支持更改鼠标光标的形状，此方法可能没有视觉效果。
     * @param cursor 由 <code>Cursor</code> 类定义的常量之一；
     *          如果此参数为 <code>null</code>，则此组件将继承其父组件的光标
     * @see       #isEnabled
     * @see       #isShowing
     * @see       #getCursor
     * @see       #contains
     * @see       Toolkit#createCustomCursor
     * @see       Cursor
     * @since     JDK1.1
     */
    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        updateCursorImmediately();
    }

    /**
     * 更新光标。不得从本机消息泵中调用此方法。
     */
    final void updateCursorImmediately() {
        if (peer instanceof LightweightPeer) {
            Container nativeContainer = getNativeContainer();

            if (nativeContainer == null) return;

            ComponentPeer cPeer = nativeContainer.getPeer();

            if (cPeer != null) {
                cPeer.updateCursorImmediately();
            }
        } else if (peer != null) {
            peer.updateCursorImmediately();
        }
    }

    /**
     * 获取组件中设置的光标。如果组件未设置光标，则返回其父组件的光标。
     * 如果整个层次结构中未设置光标，则返回 <code>Cursor.DEFAULT_CURSOR</code>。
     * @see #setCursor
     * @since      JDK1.1
     */
    public Cursor getCursor() {
        return getCursor_NoClientCode();
    }

    final Cursor getCursor_NoClientCode() {
        Cursor cursor = this.cursor;
        if (cursor != null) {
            return cursor;
        }
        Container parent = this.parent;
        if (parent != null) {
            return parent.getCursor_NoClientCode();
        } else {
            return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        }
    }

    /**
     * 返回是否为该组件显式设置了光标。如果此方法返回 <code>false</code>，则该组件从其祖先继承了光标。
     *
     * @return <code>true</code> 如果为该组件显式设置了光标；否则为 <code>false</code>。
     * @since 1.4
     */
    public boolean isCursorSet() {
        return (cursor != null);
    }

    /**
     * 绘制此组件。
     * <p>
     * 当组件的内容需要绘制时，例如当组件首次显示或损坏需要修复时，调用此方法。
     * <code>Graphics</code> 参数中的剪切矩形设置为需要绘制的区域。
     * <code>Component</code> 的子类重写此方法时不需要调用 <code>super.paint(g)</code>。
     * <p>
     * 为了提高性能，宽度或高度为零的 <code>Component</code> 在首次显示时不会被认为需要绘制，
     * 也不会被认为需要修复。
     * <p>
     * <b>注意</b>：有关 AWT 和 Swing 使用的绘制机制的更多信息，包括如何编写最高效的绘制代码，请参阅
     * <a href="http://www.oracle.com/technetwork/java/painting-140037.html">Painting in AWT and Swing</a>。
     *
     * @param g 要用于绘制的图形上下文
     * @see       #update
     * @since     JDK1.0
     */
    public void paint(Graphics g) {
    }

    /**
     * 更新此组件。
     * <p>
     * 如果此组件不是轻量级组件，AWT 会在调用 <code>repaint</code> 时调用
     * <code>update</code> 方法。可以假设背景未被清除。
     * <p>
     * <code>Component</code> 的 <code>update</code> 方法调用此组件的
     * <code>paint</code> 方法以重新绘制此组件。子类通常会重写此方法以响应
     * <code>repaint</code> 调用。重写此方法的 <code>Component</code> 的子类应调用
     * <code>super.update(g)</code>，或直接从其 <code>update</code> 方法中调用
     * <code>paint(g)</code>。
     * <p>
     * 图形上下文的原点，其 (<code>0</code>,&nbsp;<code>0</code>) 坐标点，是此组件的左上角。
     * 图形上下文的剪切区域是此组件的边界矩形。
     *
     * <p>
     * <b>注意</b>：有关 AWT 和 Swing 使用的绘制机制的更多信息，包括如何编写最高效的绘制代码，请参阅
     * <a href="http://www.oracle.com/technetwork/java/painting-140037.html">Painting in AWT and Swing</a>。
     *
     * @param g 要用于更新的指定上下文
     * @see       #paint
     * @see       #repaint()
     * @since     JDK1.0
     */
    public void update(Graphics g) {
        paint(g);
    }


                /**
     * 绘制此组件及其所有子组件。
     * <p>
     * 图形上下文的原点，即其
     * (<code>0</code>,&nbsp;<code>0</code>) 坐标点，是此组件的左上角。图形上下文的
     * 剪辑区域是此组件的边界矩形。
     *
     * @param     g   用于绘制的图形上下文
     * @see       #paint
     * @since     JDK1.0
     */
    public void paintAll(Graphics g) {
        if (isShowing()) {
            GraphicsCallback.PeerPaintCallback.getInstance().
                runOneComponent(this, new Rectangle(0, 0, width, height),
                                g, g.getClip(),
                                GraphicsCallback.LIGHTWEIGHTS |
                                GraphicsCallback.HEAVYWEIGHTS);
        }
    }

    /**
     * 模拟轻量级组件的 java.awt 绘制回调。
     * @param     g   用于绘制的图形上下文
     * @see       #paintAll
     */
    void lightweightPaint(Graphics g) {
        paint(g);
    }

    /**
     * 绘制所有重量级子组件。
     */
    void paintHeavyweightComponents(Graphics g) {
    }

    /**
     * 重新绘制此组件。
     * <p>
     * 如果此组件是轻量级组件，此方法将尽快调用此组件的 <code>paint</code>
     * 方法。否则，此方法将尽快调用此组件的 <code>update</code> 方法。
     * <p>
     * <b>注意</b>: 有关 AWT 和 Swing 使用的绘制机制的更多信息，包括如何编写最高效的绘制代码的信息，请参见
     * <a href="http://www.oracle.com/technetwork/java/painting-140037.html">AWT 和 Swing 中的绘制</a>。

     *
     * @see       #update(Graphics)
     * @since     JDK1.0
     */
    public void repaint() {
        repaint(0, 0, 0, width, height);
    }

    /**
     * 重新绘制此组件。如果此组件是轻量级组件，此方法将在 <code>tm</code> 毫秒内
     * 调用 <code>paint</code> 方法。
     * <p>
     * <b>注意</b>: 有关 AWT 和 Swing 使用的绘制机制的更多信息，包括如何编写最高效的绘制代码的信息，请参见
     * <a href="http://www.oracle.com/technetwork/java/painting-140037.html">AWT 和 Swing 中的绘制</a>。
     *
     * @param tm 最大更新时间（毫秒）
     * @see #paint
     * @see #update(Graphics)
     * @since JDK1.0
     */
    public void repaint(long tm) {
        repaint(tm, 0, 0, width, height);
    }

    /**
     * 重新绘制此组件的指定矩形。
     * <p>
     * 如果此组件是轻量级组件，此方法将尽快调用此组件的 <code>paint</code> 方法。
     * 否则，此方法将尽快调用此组件的 <code>update</code> 方法。
     * <p>
     * <b>注意</b>: 有关 AWT 和 Swing 使用的绘制机制的更多信息，包括如何编写最高效的绘制代码的信息，请参见
     * <a href="http://www.oracle.com/technetwork/java/painting-140037.html">AWT 和 Swing 中的绘制</a>。
     *
     * @param     x   坐标 <i>x</i>
     * @param     y   坐标 <i>y</i>
     * @param     width   宽度
     * @param     height  高度
     * @see       #update(Graphics)
     * @since     JDK1.0
     */
    public void repaint(int x, int y, int width, int height) {
        repaint(0, x, y, width, height);
    }

    /**
     * 在 <code>tm</code> 毫秒内重新绘制此组件的指定矩形。
     * <p>
     * 如果此组件是轻量级组件，此方法将调用此组件的 <code>paint</code> 方法。
     * 否则，此方法将调用此组件的 <code>update</code> 方法。
     * <p>
     * <b>注意</b>: 有关 AWT 和 Swing 使用的绘制机制的更多信息，包括如何编写最高效的绘制代码的信息，请参见
     * <a href="http://www.oracle.com/technetwork/java/painting-140037.html">AWT 和 Swing 中的绘制</a>。
     *
     * @param     tm   最大更新时间（毫秒）
     * @param     x    坐标 <i>x</i>
     * @param     y    坐标 <i>y</i>
     * @param     width    宽度
     * @param     height   高度
     * @see       #update(Graphics)
     * @since     JDK1.0
     */
    public void repaint(long tm, int x, int y, int width, int height) {
        if (this.peer instanceof LightweightPeer) {
            // 需要转换为父组件的坐标，因为实际的重绘服务由父本机容器提供。
            // 此外，请求被限制在组件的边界内。
            if (parent != null) {
                if (x < 0) {
                    width += x;
                    x = 0;
                }
                if (y < 0) {
                    height += y;
                    y = 0;
                }

                int pwidth = (width > this.width) ? this.width : width;
                int pheight = (height > this.height) ? this.height : height;

                if (pwidth <= 0 || pheight <= 0) {
                    return;
                }

                int px = this.x + x;
                int py = this.y + y;
                parent.repaint(tm, px, py, pwidth, pheight);
            }
        } else {
            if (isVisible() && (this.peer != null) &&
                (width > 0) && (height > 0)) {
                PaintEvent e = new PaintEvent(this, PaintEvent.UPDATE,
                                              new Rectangle(x, y, width, height));
                SunToolkit.postEvent(SunToolkit.targetToAppContext(this), e);
            }
        }
    }

    /**
     * 打印此组件。应用程序应覆盖此方法，以在打印前进行特殊处理或以不同于绘制的方式打印。
     * <p>
     * 此方法的默认实现调用 <code>paint</code> 方法。
     * <p>
     * 图形上下文的原点，即其
     * (<code>0</code>,&nbsp;<code>0</code>) 坐标点，是此组件的左上角。图形上下文的
     * 剪辑区域是此组件的边界矩形。
     * @param     g   用于打印的图形上下文
     * @see       #paint(Graphics)
     * @since     JDK1.0
     */
    public void print(Graphics g) {
        paint(g);
    }

    /**
     * 打印此组件及其所有子组件。
     * <p>
     * 图形上下文的原点，即其
     * (<code>0</code>,&nbsp;<code>0</code>) 坐标点，是此组件的左上角。图形上下文的
     * 剪辑区域是此组件的边界矩形。
     * @param     g   用于打印的图形上下文
     * @see       #print(Graphics)
     * @since     JDK1.0
     */
    public void printAll(Graphics g) {
        if (isShowing()) {
            GraphicsCallback.PeerPrintCallback.getInstance().
                runOneComponent(this, new Rectangle(0, 0, width, height),
                                g, g.getClip(),
                                GraphicsCallback.LIGHTWEIGHTS |
                                GraphicsCallback.HEAVYWEIGHTS);
        }
    }

    /**
     * 模拟轻量级组件的 java.awt 打印回调。
     * @param     g   用于打印的图形上下文
     * @see       #printAll
     */
    void lightweightPrint(Graphics g) {
        print(g);
    }

    /**
     * 打印所有重量级子组件。
     */
    void printHeavyweightComponents(Graphics g) {
    }

    private Insets getInsets_NoClientCode() {
        ComponentPeer peer = this.peer;
        if (peer instanceof ContainerPeer) {
            return (Insets)((ContainerPeer)peer).getInsets().clone();
        }
        return new Insets(0, 0, 0, 0);
    }

    /**
     * 当图像发生变化时重新绘制组件。
     * 此 <code>imageUpdate</code> 方法是一个 <code>ImageObserver</code>
     * 的方法，当使用 <code>Graphics</code> 的异步方法（如 <code>drawImage</code>）
     * 请求的图像有更多信息可用时，此方法将被调用。
     * 有关此方法及其参数的更多信息，请参见 <code>imageUpdate</code> 的定义。
     * <p>
     * <code>Component</code> 的 <code>imageUpdate</code> 方法会随着图像的更多位信息可用
     * 而逐步在组件上绘制图像。
     * <p>
     * 如果系统属性 <code>awt.image.incrementaldraw</code>
     * 不存在或值为 <code>true</code>，则图像将逐步绘制。如果系统属性有其他值，
     * 则图像将在完全加载后绘制。
     * <p>
     * 此外，如果逐步绘制生效，系统属性 <code>awt.image.redrawrate</code>
     * 的值将被解释为整数，以给出最大重绘率，单位为毫秒。如果系统属性不存在或无法解释为整数，
     * 则重绘率为每 100 毫秒一次。
     * <p>
     * <code>x</code>、<code>y</code>、<code>width</code> 和 <code>height</code>
     * 参数的解释取决于 <code>infoflags</code> 参数的值。
     *
     * @param     img   被观察的图像
     * @param     infoflags   有关 <code>imageUpdate</code> 的更多信息
     * @param     x   坐标 <i>x</i>
     * @param     y   坐标 <i>y</i>
     * @param     w   宽度
     * @param     h   高度
     * @return    如果 infoflags 表示图像已完全加载，则返回 <code>false</code>；否则返回 <code>true</code>。
     *
     * @see     java.awt.image.ImageObserver
     * @see     Graphics#drawImage(Image, int, int, Color, java.awt.image.ImageObserver)
     * @see     Graphics#drawImage(Image, int, int, java.awt.image.ImageObserver)
     * @see     Graphics#drawImage(Image, int, int, int, int, Color, java.awt.image.ImageObserver)
     * @see     Graphics#drawImage(Image, int, int, int, int, java.awt.image.ImageObserver)
     * @see     java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     * @since   JDK1.0
     */
    public boolean imageUpdate(Image img, int infoflags,
                               int x, int y, int w, int h) {
        int rate = -1;
        if ((infoflags & (FRAMEBITS|ALLBITS)) != 0) {
            rate = 0;
        } else if ((infoflags & SOMEBITS) != 0) {
            if (isInc) {
                rate = incRate;
                if (rate < 0) {
                    rate = 0;
                }
            }
        }
        if (rate >= 0) {
            repaint(rate, 0, 0, width, height);
        }
        return (infoflags & (ALLBITS|ABORT)) == 0;
    }

    /**
     * 从指定的图像生成器创建图像。
     * @param     producer  图像生成器
     * @return    生成的图像
     * @since     JDK1.0
     */
    public Image createImage(ImageProducer producer) {
        ComponentPeer peer = this.peer;
        if ((peer != null) && ! (peer instanceof LightweightPeer)) {
            return peer.createImage(producer);
        }
        return getToolkit().createImage(producer);
    }

    /**
     * 创建一个用于双缓冲的离屏可绘制图像。
     * @param     width 指定的宽度
     * @param     height 指定的高度
     * @return    一个用于双缓冲的离屏可绘制图像。如果组件不可显示，则返回值可能为 <code>null</code>。
     *    如果 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code>，则总是会发生这种情况。
     * @see #isDisplayable
     * @see GraphicsEnvironment#isHeadless
     * @since     JDK1.0
     */
    public Image createImage(int width, int height) {
        ComponentPeer peer = this.peer;
        if (peer instanceof LightweightPeer) {
            if (parent != null) { return parent.createImage(width, height); }
            else { return null;}
        } else {
            return (peer != null) ? peer.createImage(width, height) : null;
        }
    }

    /**
     * 创建一个用于双缓冲的易失性离屏可绘制图像。
     * @param     width 指定的宽度。
     * @param     height 指定的高度。
     * @return    一个用于双缓冲的离屏可绘制图像。如果组件不可显示，则返回值可能为 <code>null</code>。
     *    如果 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code>，则总是会发生这种情况。
     * @see java.awt.image.VolatileImage
     * @see #isDisplayable
     * @see GraphicsEnvironment#isHeadless
     * @since     1.4
     */
    public VolatileImage createVolatileImage(int width, int height) {
        ComponentPeer peer = this.peer;
        if (peer instanceof LightweightPeer) {
            if (parent != null) {
                return parent.createVolatileImage(width, height);
            }
            else { return null;}
        } else {
            return (peer != null) ?
                peer.createVolatileImage(width, height) : null;
        }
    }

    /**
     * 创建一个具有指定功能的易失性离屏可绘制图像。
     * 该图像的内容可能随时因操作系统问题而丢失，因此必须通过
     * <code>VolatileImage</code> 接口进行管理。
     * @param width 指定的宽度。
     * @param height 指定的高度。
     * @param caps 图像功能
     * @exception AWTException 如果无法创建具有指定功能的图像
     * @return 一个 VolatileImage 对象，可用于管理表面内容丢失和功能。
     * @see java.awt.image.VolatileImage
     * @since 1.4
     */
    public VolatileImage createVolatileImage(int width, int height,
                                             ImageCapabilities caps) throws AWTException {
        // REMIND : 检查 caps
        return createVolatileImage(width, height);
    }

    /**
     * 为在此组件上渲染准备图像。图像数据在另一个线程中异步下载，
     * 并生成图像的适当屏幕表示。
     * @param     image   要准备屏幕表示的 <code>Image</code>
     * @param     observer   作为图像准备过程中的通知对象的 <code>ImageObserver</code>
     * @return    如果图像已完全准备，则返回 <code>true</code>；否则返回 <code>false</code>
     * @since     JDK1.0
     */
    public boolean prepareImage(Image image, ImageObserver observer) {
        return prepareImage(image, -1, -1, observer);
    }


                /**
                 * 为在此组件上以指定的宽度和高度渲染准备图像。
                 * <p>
                 * 图像数据在另一个线程中异步下载，
                 * 并生成适当缩放的屏幕图像表示。
                 * @param     image    要准备屏幕表示的 <code>Image</code> 实例
                 * @param     width    所需屏幕表示的宽度
                 * @param     height   所需屏幕表示的高度
                 * @param     observer   作为图像准备过程中的通知对象的 <code>ImageObserver</code>
                 * @return    如果图像已完全准备，则返回 <code>true</code>；否则返回 <code>false</code>
                 * @see       java.awt.image.ImageObserver
                 * @since     JDK1.0
                 */
                public boolean prepareImage(Image image, int width, int height,
                                            ImageObserver observer) {
                    ComponentPeer peer = this.peer;
                    if (peer instanceof LightweightPeer) {
                        return (parent != null)
                            ? parent.prepareImage(image, width, height, observer)
                            : getToolkit().prepareImage(image, width, height, observer);
                    } else {
                        return (peer != null)
                            ? peer.prepareImage(image, width, height, observer)
                            : getToolkit().prepareImage(image, width, height, observer);
                    }
                }

                /**
                 * 返回指定图像的屏幕表示构建状态。
                 * <p>
                 * 此方法不会导致图像开始加载。应用程序必须使用 <code>prepareImage</code> 方法
                 * 强制加载图像。
                 * <p>
                 * 有关此方法返回的标志的信息可以在 <code>ImageObserver</code> 接口的讨论中找到。
                 * @param     image   正在检查状态的 <code>Image</code> 对象
                 * @param     observer   作为图像准备过程中的通知对象的 <code>ImageObserver</code>
                 * @return  表示当前可用的图像信息的 <code>ImageObserver</code> 标志的按位或
                 * @see      #prepareImage(Image, int, int, java.awt.image.ImageObserver)
                 * @see      Toolkit#checkImage(Image, int, int, java.awt.image.ImageObserver)
                 * @see      java.awt.image.ImageObserver
                 * @since    JDK1.0
                 */
                public int checkImage(Image image, ImageObserver observer) {
                    return checkImage(image, -1, -1, observer);
                }

                /**
                 * 返回指定图像的屏幕表示构建状态。
                 * <p>
                 * 此方法不会导致图像开始加载。应用程序必须使用 <code>prepareImage</code> 方法
                 * 强制加载图像。
                 * <p>
                 * <code>Component</code> 的 <code>checkImage</code> 方法调用其对等对象的 <code>checkImage</code> 方法来计算标志。
                 * 如果此组件尚未有对等对象，则调用组件的工具包的 <code>checkImage</code> 方法。
                 * <p>
                 * 有关此方法返回的标志的信息可以在 <code>ImageObserver</code> 接口的讨论中找到。
                 * @param     image   正在检查状态的 <code>Image</code> 对象
                 * @param     width   要检查状态的缩放版本的宽度
                 * @param     height  要检查状态的缩放版本的高度
                 * @param     observer   作为图像准备过程中的通知对象的 <code>ImageObserver</code>
                 * @return    表示当前可用的图像信息的 <code>ImageObserver</code> 标志的按位或
                 * @see      #prepareImage(Image, int, int, java.awt.image.ImageObserver)
                 * @see      Toolkit#checkImage(Image, int, int, java.awt.image.ImageObserver)
                 * @see      java.awt.image.ImageObserver
                 * @since    JDK1.0
                 */
                public int checkImage(Image image, int width, int height,
                                      ImageObserver observer) {
                    ComponentPeer peer = this.peer;
                    if (peer instanceof LightweightPeer) {
                        return (parent != null)
                            ? parent.checkImage(image, width, height, observer)
                            : getToolkit().checkImage(image, width, height, observer);
                    } else {
                        return (peer != null)
                            ? peer.checkImage(image, width, height, observer)
                            : getToolkit().checkImage(image, width, height, observer);
                    }
                }

                /**
                 * 在此组件上创建新的多缓冲策略。
                 * 多缓冲对于渲染性能非常有用。此方法尝试使用提供的缓冲区数量创建最佳策略。
                 * 它将始终创建具有该数量缓冲区的 <code>BufferStrategy</code>。
                 * 首先尝试创建页面翻转策略，然后尝试使用加速缓冲区的光栅化策略。
                 * 最后，使用非加速光栅化策略。
                 * <p>
                 * 每次调用此方法时，
                 * 都会丢弃此组件的现有缓冲策略。
                 * @param numBuffers 要创建的缓冲区数量，包括前台缓冲区
                 * @exception IllegalArgumentException 如果 numBuffers 小于 1。
                 * @exception IllegalStateException 如果组件不可显示
                 * @see #isDisplayable
                 * @see Window#getBufferStrategy()
                 * @see Canvas#getBufferStrategy()
                 * @since 1.4
                 */
                void createBufferStrategy(int numBuffers) {
                    BufferCapabilities bufferCaps;
                    if (numBuffers > 1) {
                        // 尝试创建页面翻转策略
                        bufferCaps = new BufferCapabilities(new ImageCapabilities(true),
                                                            new ImageCapabilities(true),
                                                            BufferCapabilities.FlipContents.UNDEFINED);
                        try {
                            createBufferStrategy(numBuffers, bufferCaps);
                            return; // 成功
                        } catch (AWTException e) {
                            // 失败
                        }
                    }
                    // 尝试创建（但仍然加速的）光栅化策略
                    bufferCaps = new BufferCapabilities(new ImageCapabilities(true),
                                                        new ImageCapabilities(true),
                                                        null);
                    try {
                        createBufferStrategy(numBuffers, bufferCaps);
                        return; // 成功
                    } catch (AWTException e) {
                        // 失败
                    }
                    // 尝试创建非加速光栅化策略
                    bufferCaps = new BufferCapabilities(new ImageCapabilities(false),
                                                        new ImageCapabilities(false),
                                                        null);
                    try {
                        createBufferStrategy(numBuffers, bufferCaps);
                        return; // 成功
                    } catch (AWTException e) {
                        // 代码不应该到达这里（非加速光栅化策略应该总是有效）
                        throw new InternalError("无法创建缓冲策略", e);
                    }
                }

                /**
                 * 为具有所需缓冲区功能的此组件创建新的多缓冲策略。
                 * 例如，如果仅需要加速内存或页面翻转（如缓冲区功能所指定）。
                 * <p>
                 * 每次调用此方法时，
                 * 都会调用现有 <code>BufferStrategy</code> 的 <code>dispose</code> 方法。
                 * @param numBuffers 要创建的缓冲区数量
                 * @param caps 创建缓冲策略所需的缓冲区功能；不能为 <code>null</code>
                 * @exception AWTException 如果提供的功能无法支持或满足；例如，如果当前没有足够的加速内存，或者指定了页面翻转但不可能。
                 * @exception IllegalArgumentException 如果 numBuffers 小于 1，或 caps 为 <code>null</code>
                 * @see Window#getBufferStrategy()
                 * @see Canvas#getBufferStrategy()
                 * @since 1.4
                 */
                void createBufferStrategy(int numBuffers,
                                          BufferCapabilities caps) throws AWTException {
                    // 检查参数
                    if (numBuffers < 1) {
                        throw new IllegalArgumentException(
                            "缓冲区数量必须至少为 1");
                    }
                    if (caps == null) {
                        throw new IllegalArgumentException("未指定功能");
                    }
                    // 销毁旧缓冲区
                    if (bufferStrategy != null) {
                        bufferStrategy.dispose();
                    }
                    if (numBuffers == 1) {
                        bufferStrategy = new SingleBufferStrategy(caps);
                    } else {
                        SunGraphicsEnvironment sge = (SunGraphicsEnvironment)
                            GraphicsEnvironment.getLocalGraphicsEnvironment();
                        if (!caps.isPageFlipping() && sge.isFlipStrategyPreferred(peer)) {
                            caps = new ProxyCapabilities(caps);
                        }
                        // 断言 numBuffers > 1;
                        if (caps.isPageFlipping()) {
                            bufferStrategy = new FlipSubRegionBufferStrategy(numBuffers, caps);
                        } else {
                            bufferStrategy = new BltSubRegionBufferStrategy(numBuffers, caps);
                        }
                    }
                }

                /**
                 * 当创建 FlipBufferStrategy 而不是请求的 Blit 策略时使用的代理功能类。
                 *
                 * @see sun.java2d.SunGraphicsEnvironment#isFlipStrategyPreferred(ComponentPeer)
                 */
                private class ProxyCapabilities extends ExtendedBufferCapabilities {
                    private BufferCapabilities orig;
                    private ProxyCapabilities(BufferCapabilities orig) {
                        super(orig.getFrontBufferCapabilities(),
                              orig.getBackBufferCapabilities(),
                              orig.getFlipContents() ==
                                  BufferCapabilities.FlipContents.BACKGROUND ?
                                  BufferCapabilities.FlipContents.BACKGROUND :
                                  BufferCapabilities.FlipContents.COPIED);
                        this.orig = orig;
                    }
                }

                /**
                 * @return 此组件使用的缓冲策略
                 * @see Window#createBufferStrategy
                 * @see Canvas#createBufferStrategy
                 * @since 1.4
                 */
                BufferStrategy getBufferStrategy() {
                    return bufferStrategy;
                }

                /**
                 * @return 此组件的 BufferStrategy 当前使用的后缓冲区。如果没有 BufferStrategy 或没有
                 * 后缓冲区，此方法返回 null。
                 */
                Image getBackBuffer() {
                    if (bufferStrategy != null) {
                        if (bufferStrategy instanceof BltBufferStrategy) {
                            BltBufferStrategy bltBS = (BltBufferStrategy)bufferStrategy;
                            return bltBS.getBackBuffer();
                        } else if (bufferStrategy instanceof FlipBufferStrategy) {
                            FlipBufferStrategy flipBS = (FlipBufferStrategy)bufferStrategy;
                            return flipBS.getBackBuffer();
                        }
                    }
                    return null;
                }

                /**
                 * 用于在组件上翻转缓冲区的内部类。该组件必须是 <code>Canvas</code> 或 <code>Window</code>。
                 * @see Canvas
                 * @see Window
                 * @see java.awt.image.BufferStrategy
                 * @author Michael Martak
                 * @since 1.4
                 */
                protected class FlipBufferStrategy extends BufferStrategy {
                    /**
                     * 缓冲区的数量
                     */
                    protected int numBuffers; // = 0
                    /**
                     * 缓冲区的功能
                     */
                    protected BufferCapabilities caps; // = null
                    /**
                     * 绘制缓冲区
                     */
                    protected Image drawBuffer; // = null
                    /**
                     * 绘制缓冲区作为易失图像
                     */
                    protected VolatileImage drawVBuffer; // = null
                    /**
                     * 绘制缓冲区是否已从丢失状态恢复。
                     */
                    protected boolean validatedContents; // = false
                    /**
                     * 后缓冲区的大小。 （注意：这些字段在 6.0 中添加，但保持包私有以避免在规范中暴露。
                     * 这些字段/方法在 1.4 中引入时本不应该标记为受保护，但现在我们必须接受这一决定。）
                     */
                    int width;
                    int height;

                    /**
                     * 为此组件创建新的翻转缓冲策略。
                     * 该组件必须是 <code>Canvas</code> 或 <code>Window</code>。
                     * @see Canvas
                     * @see Window
                     * @param numBuffers 缓冲区的数量
                     * @param caps 缓冲区的功能
                     * @exception AWTException 如果提供的功能无法支持或满足
                     * @exception ClassCastException 如果组件不是 canvas 或 window。
                     * @exception IllegalStateException 如果组件没有对等对象
                     * @exception IllegalArgumentException 如果 {@code numBuffers} 小于两个，
                     * 或如果 {@code BufferCapabilities.isPageFlipping} 不为
                     * {@code true}。
                     * @see #createBuffers(int, BufferCapabilities)
                     */
                    protected FlipBufferStrategy(int numBuffers, BufferCapabilities caps)
                        throws AWTException
                    {
                        if (!(Component.this instanceof Window) &&
                            !(Component.this instanceof Canvas))
                        {
                            throw new ClassCastException(
                                "组件必须是 Canvas 或 Window");
                        }
                        this.numBuffers = numBuffers;
                        this.caps = caps;
                        createBuffers(numBuffers, caps);
                    }

                    /**
                     * 使用给定的功能创建一个或多个复杂的翻转缓冲区。
                     * @param numBuffers 要创建的缓冲区数量；必须大于一个
                     * @param caps 缓冲区的功能。
                     * <code>BufferCapabilities.isPageFlipping</code> 必须为
                     * <code>true</code>。
                     * @exception AWTException 如果提供的功能无法支持或满足
                     * @exception IllegalStateException 如果组件没有对等对象
                     * @exception IllegalArgumentException 如果 numBuffers 小于两个，
                     * 或如果 <code>BufferCapabilities.isPageFlipping</code> 不为
                     * <code>true</code>。
                     * @see java.awt.BufferCapabilities#isPageFlipping()
                     */
                    protected void createBuffers(int numBuffers, BufferCapabilities caps)
                        throws AWTException
                    {
                        if (numBuffers < 2) {
                            throw new IllegalArgumentException(
                                "缓冲区数量不能小于两个");
                        } else if (peer == null) {
                            throw new IllegalStateException(
                                "组件必须有有效的对等对象");
                        } else if (caps == null || !caps.isPageFlipping()) {
                            throw new IllegalArgumentException(
                                "必须指定页面翻转功能");
                        }


                        // 保存当前边界
            width = getWidth();
            height = getHeight();

            if (drawBuffer != null) {
                // 释放现有的后缓冲区
                drawBuffer = null;
                drawVBuffer = null;
                destroyBuffers();
                // ... 然后重新创建后缓冲区
            }

            if (caps instanceof ExtendedBufferCapabilities) {
                ExtendedBufferCapabilities ebc =
                    (ExtendedBufferCapabilities)caps;
                if (ebc.getVSync() == VSYNC_ON) {
                    // 如果此缓冲策略不允许同步，
                    // 更改传递给对等对象的 caps，但继续尝试创建同步缓冲区；
                    // 如果不允许，不要在这里抛出 IAE，详见
                    // ExtendedBufferCapabilities 的更多信息
                    if (!VSyncedBSManager.vsyncAllowed(this)) {
                        caps = ebc.derive(VSYNC_DEFAULT);
                    }
                }
            }

            peer.createBuffers(numBuffers, caps);
            updateInternalBuffers();
        }

        /**
         * 更新内部缓冲区（包括易失性和非易失性缓冲区）
         * 通过从对等对象请求后缓冲区。
         */
        private void updateInternalBuffers() {
            // 获取与绘图缓冲区关联的图像
            drawBuffer = getBackBuffer();
            if (drawBuffer instanceof VolatileImage) {
                drawVBuffer = (VolatileImage)drawBuffer;
            } else {
                drawVBuffer = null;
            }
        }

        /**
         * @return 作为图像的后缓冲区的直接访问。
         * @exception IllegalStateException 如果缓冲区尚未创建
         */
        protected Image getBackBuffer() {
            if (peer != null) {
                return peer.getBackBuffer();
            } else {
                throw new IllegalStateException(
                    "组件必须有一个有效的对等对象");
            }
        }

        /**
         * 翻转将后缓冲区的内容移动到前缓冲区，
         * 通过复制或移动视频指针。
         * @param flipAction 描述后缓冲区内容翻转操作的整数值。
         * 这应该是 <code>BufferCapabilities.FlipContents</code>
         * 属性的值之一。
         * @exception IllegalStateException 如果缓冲区尚未创建
         * @see java.awt.BufferCapabilities#getFlipContents()
         */
        protected void flip(BufferCapabilities.FlipContents flipAction) {
            if (peer != null) {
                Image backBuffer = getBackBuffer();
                if (backBuffer != null) {
                    peer.flip(0, 0,
                              backBuffer.getWidth(null),
                              backBuffer.getHeight(null), flipAction);
                }
            } else {
                throw new IllegalStateException(
                    "组件必须有一个有效的对等对象");
            }
        }

        void flipSubRegion(int x1, int y1, int x2, int y2,
                      BufferCapabilities.FlipContents flipAction)
        {
            if (peer != null) {
                peer.flip(x1, y1, x2, y2, flipAction);
            } else {
                throw new IllegalStateException(
                    "组件必须有一个有效的对等对象");
            }
        }

        /**
         * 销毁通过此对象创建的缓冲区
         */
        protected void destroyBuffers() {
            VSyncedBSManager.releaseVsync(this);
            if (peer != null) {
                peer.destroyBuffers();
            } else {
                throw new IllegalStateException(
                    "组件必须有一个有效的对等对象");
            }
        }

        /**
         * @return 此策略的缓冲能力
         */
        public BufferCapabilities getCapabilities() {
            if (caps instanceof ProxyCapabilities) {
                return ((ProxyCapabilities)caps).orig;
            } else {
                return caps;
            }
        }

        /**
         * @return 绘图缓冲区上的图形。此方法可能未同步以提高性能；
         * 多线程使用此方法应由应用程序级别处理。图形对象的释放
         * 必须由应用程序处理。
         */
        public Graphics getDrawGraphics() {
            revalidate();
            return drawBuffer.getGraphics();
        }

        /**
         * 如果绘图缓冲区已丢失，则恢复绘图缓冲区
         */
        protected void revalidate() {
            revalidate(true);
        }

        void revalidate(boolean checkSize) {
            validatedContents = false;

            if (checkSize && (getWidth() != width || getHeight() != height)) {
                // 组件已调整大小；重新创建后缓冲区
                try {
                    createBuffers(numBuffers, caps);
                } catch (AWTException e) {
                    // 不应发生
                }
                validatedContents = true;
            }

            // 每次从对等对象获取缓冲区，因为它们
            // 可能在显示更改事件响应中被替换
            updateInternalBuffers();

            // 现在验证后缓冲区
            if (drawVBuffer != null) {
                GraphicsConfiguration gc =
                        getGraphicsConfiguration_NoClientCode();
                int returnCode = drawVBuffer.validate(gc);
                if (returnCode == VolatileImage.IMAGE_INCOMPATIBLE) {
                    try {
                        createBuffers(numBuffers, caps);
                    } catch (AWTException e) {
                        // 不应发生
                    }
                    if (drawVBuffer != null) {
                        // 后缓冲区已重新创建，因此再次验证
                        drawVBuffer.validate(gc);
                    }
                    validatedContents = true;
                } else if (returnCode == VolatileImage.IMAGE_RESTORED) {
                    validatedContents = true;
                }
            }
        }

        /**
         * @return 自上次调用 <code>getDrawGraphics</code> 以来
         * 绘图缓冲区是否已丢失
         */
        public boolean contentsLost() {
            if (drawVBuffer == null) {
                return false;
            }
            return drawVBuffer.contentsLost();
        }

        /**
         * @return 绘图缓冲区是否最近从丢失状态恢复
         * 并重新初始化为默认背景色（白色）
         */
        public boolean contentsRestored() {
            return validatedContents;
        }

        /**
         * 通过 blitting 或翻转使下一个可用缓冲区可见。
         */
        public void show() {
            flip(caps.getFlipContents());
        }

        /**
         * 通过 blitting 或翻转使下一个可用缓冲区的指定区域可见。
         */
        void showSubRegion(int x1, int y1, int x2, int y2) {
            flipSubRegion(x1, y1, x2, y2, caps.getFlipContents());
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public void dispose() {
            if (Component.this.bufferStrategy == this) {
                Component.this.bufferStrategy = null;
                if (peer != null) {
                    destroyBuffers();
                }
            }
        }

    } // 内部类 FlipBufferStrategy

    /**
     * 用于将离屏表面 blitting 到组件的内部类。
     *
     * @author Michael Martak
     * @since 1.4
     */
    protected class BltBufferStrategy extends BufferStrategy {

        /**
         * 缓冲能力
         */
        protected BufferCapabilities caps; // = null
        /**
         * 后缓冲区
         */
        protected VolatileImage[] backBuffers; // = null
        /**
         * 绘图缓冲区是否最近从丢失状态恢复
         */
        protected boolean validatedContents; // = false
        /**
         * 后缓冲区的大小
         */
        protected int width;
        protected int height;

        /**
         * 承载组件的内边距。后缓冲区的大小
         * 受这些内边距的限制。
         */
        private Insets insets;

        /**
         * 创建围绕组件的新 blt 缓冲策略
         * @param numBuffers 要创建的缓冲区数量，包括前缓冲区
         * @param caps 缓冲区的能力
         */
        protected BltBufferStrategy(int numBuffers, BufferCapabilities caps) {
            this.caps = caps;
            createBackBuffers(numBuffers - 1);
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        public void dispose() {
            if (backBuffers != null) {
                for (int counter = backBuffers.length - 1; counter >= 0;
                     counter--) {
                    if (backBuffers[counter] != null) {
                        backBuffers[counter].flush();
                        backBuffers[counter] = null;
                    }
                }
            }
            if (Component.this.bufferStrategy == this) {
                Component.this.bufferStrategy = null;
            }
        }

        /**
         * 创建后缓冲区
         */
        protected void createBackBuffers(int numBuffers) {
            if (numBuffers == 0) {
                backBuffers = null;
            } else {
                // 保存当前边界
                width = getWidth();
                height = getHeight();
                insets = getInsets_NoClientCode();
                int iWidth = width - insets.left - insets.right;
                int iHeight = height - insets.top - insets.bottom;

                // 组件的宽度和/或高度可能为 0。强制后缓冲区的大小
                // 大于 0，以防止创建图像时失败。
                iWidth = Math.max(1, iWidth);
                iHeight = Math.max(1, iHeight);
                if (backBuffers == null) {
                    backBuffers = new VolatileImage[numBuffers];
                } else {
                    // 刷新任何现有的后缓冲区
                    for (int i = 0; i < numBuffers; i++) {
                        if (backBuffers[i] != null) {
                            backBuffers[i].flush();
                            backBuffers[i] = null;
                        }
                    }
                }

                // 创建后缓冲区
                for (int i = 0; i < numBuffers; i++) {
                    backBuffers[i] = createVolatileImage(iWidth, iHeight);
                }
            }
        }

        /**
         * @return 此策略的缓冲能力
         */
        public BufferCapabilities getCapabilities() {
            return caps;
        }

        /**
         * @return 绘图图形
         */
        public Graphics getDrawGraphics() {
            revalidate();
            Image backBuffer = getBackBuffer();
            if (backBuffer == null) {
                return getGraphics();
            }
            SunGraphics2D g = (SunGraphics2D)backBuffer.getGraphics();
            g.constrain(-insets.left, -insets.top,
                        backBuffer.getWidth(null) + insets.left,
                        backBuffer.getHeight(null) + insets.top);
            return g;
        }

        /**
         * @return 作为图像的后缓冲区的直接访问。
         * 如果没有后缓冲区，返回 null。
         */
        Image getBackBuffer() {
            if (backBuffers != null) {
                return backBuffers[backBuffers.length - 1];
            } else {
                return null;
            }
        }

        /**
         * 使下一个可用缓冲区可见。
         */
        public void show() {
            showSubRegion(insets.left, insets.top,
                          width - insets.right,
                          height - insets.bottom);
        }

        /**
         * 包级私有方法，用于呈现此缓冲区的特定矩形区域。
         * 此类目前仅显示整个缓冲区，通过调用 showSubRegion()
         * 传递缓冲区的完整尺寸。子类（例如，BltSubRegionBufferStrategy
         * 和 FlipSubRegionBufferStrategy）可能有特定区域的显示
         * 方法，这些方法调用此方法时传递缓冲区的实际子区域。
         */
        void showSubRegion(int x1, int y1, int x2, int y2) {
            if (backBuffers == null) {
                return;
            }
            // 调整位置以相对于客户区。
            x1 -= insets.left;
            x2 -= insets.left;
            y1 -= insets.top;
            y2 -= insets.top;
            Graphics g = getGraphics_NoClientCode();
            if (g == null) {
                // 不显示，退出
                return;
            }
            try {
                // 第一个图像复制是相对于 Frame 的坐标，需要
                // 转换为客户区。
                g.translate(insets.left, insets.top);
                for (int i = 0; i < backBuffers.length; i++) {
                    g.drawImage(backBuffers[i],
                                x1, y1, x2, y2,
                                x1, y1, x2, y2,
                                null);
                    g.dispose();
                    g = null;
                    g = backBuffers[i].getGraphics();
                }
            } finally {
                if (g != null) {
                    g.dispose();
                }
            }
        }

        /**
         * 如果绘图缓冲区已丢失，则恢复绘图缓冲区
         */
        protected void revalidate() {
            revalidate(true);
        }

        void revalidate(boolean checkSize) {
            validatedContents = false;

            if (backBuffers == null) {
                return;
            }

            if (checkSize) {
                Insets insets = getInsets_NoClientCode();
                if (getWidth() != width || getHeight() != height ||
                    !insets.equals(this.insets)) {
                    // 组件已调整大小；重新创建后缓冲区
                    createBackBuffers(backBuffers.length);
                    validatedContents = true;
                }
            }

            // 现在验证后缓冲区
            GraphicsConfiguration gc = getGraphicsConfiguration_NoClientCode();
            int returnCode =
                backBuffers[backBuffers.length - 1].validate(gc);
            if (returnCode == VolatileImage.IMAGE_INCOMPATIBLE) {
                if (checkSize) {
                    createBackBuffers(backBuffers.length);
                    // 后缓冲区已重新创建，因此再次验证
                    backBuffers[backBuffers.length - 1].validate(gc);
                }
                // 否则意味着我们从 Swing 在工具包线程上调用，
                // 不要重新创建缓冲区，因为这将导致死锁
                // （创建 VolatileImages 会调用获取 GraphicsConfig
                // 从而获取 treelock）。
                validatedContents = true;
            } else if (returnCode == VolatileImage.IMAGE_RESTORED) {
                validatedContents = true;
            }
        }


                    /**
         * @return 是否自上次调用 <code>getDrawGraphics</code> 以来绘制缓冲区已丢失。
         */
        public boolean contentsLost() {
            if (backBuffers == null) {
                return false;
            } else {
                return backBuffers[backBuffers.length - 1].contentsLost();
            }
        }

        /**
         * @return 绘制缓冲区是否最近从丢失状态恢复并重新初始化为默认背景色（白色）。
         */
        public boolean contentsRestored() {
            return validatedContents;
        }
    } // 内部类 BltBufferStrategy

    /**
     * 私有类，用于执行子区域翻转。
     */
    private class FlipSubRegionBufferStrategy extends FlipBufferStrategy
        implements SubRegionShowable
    {

        protected FlipSubRegionBufferStrategy(int numBuffers,
                                              BufferCapabilities caps)
            throws AWTException
        {
            super(numBuffers, caps);
        }

        public void show(int x1, int y1, int x2, int y2) {
            showSubRegion(x1, y1, x2, y2);
        }

        // 由 Swing 在工具包线程上调用。
        public boolean showIfNotLost(int x1, int y1, int x2, int y2) {
            if (!contentsLost()) {
                showSubRegion(x1, y1, x2, y2);
                return !contentsLost();
            }
            return false;
        }
    }

    /**
     * 私有类，用于执行子区域光栅化。Swing 将通过 SubRegionShowable 接口使用此子类，以便仅复制在重绘期间更改的区域。
     * 参见 javax.swing.BufferStrategyPaintManager。
     */
    private class BltSubRegionBufferStrategy extends BltBufferStrategy
        implements SubRegionShowable
    {

        protected BltSubRegionBufferStrategy(int numBuffers,
                                             BufferCapabilities caps)
        {
            super(numBuffers, caps);
        }

        public void show(int x1, int y1, int x2, int y2) {
            showSubRegion(x1, y1, x2, y2);
        }

        // 由 Swing 在工具包线程上调用。
        public boolean showIfNotLost(int x1, int y1, int x2, int y2) {
            if (!contentsLost()) {
                showSubRegion(x1, y1, x2, y2);
                return !contentsLost();
            }
            return false;
        }
    }

    /**
     * 用于在组件上翻转缓冲区的内部类。该组件必须是 <code>Canvas</code> 或 <code>Window</code>。
     * @see Canvas
     * @see Window
     * @see java.awt.image.BufferStrategy
     * @author Michael Martak
     * @since 1.4
     */
    private class SingleBufferStrategy extends BufferStrategy {

        private BufferCapabilities caps;

        public SingleBufferStrategy(BufferCapabilities caps) {
            this.caps = caps;
        }
        public BufferCapabilities getCapabilities() {
            return caps;
        }
        public Graphics getDrawGraphics() {
            return getGraphics();
        }
        public boolean contentsLost() {
            return false;
        }
        public boolean contentsRestored() {
            return false;
        }
        public void show() {
            // 什么都不做
        }
    } // 内部类 SingleBufferStrategy

    /**
     * 设置是否忽略从操作系统接收的绘图消息。这不会影响由 AWT 在软件中生成的绘图事件，除非它们是操作系统级绘图消息的即时响应。
     * <p>
     * 例如，在全屏模式下运行时，如果需要更好的性能，或者使用页面翻转作为缓冲策略时，这很有用。
     *
     * @since 1.4
     * @see #getIgnoreRepaint
     * @see Canvas#createBufferStrategy
     * @see Window#createBufferStrategy
     * @see java.awt.image.BufferStrategy
     * @see GraphicsDevice#setFullScreenWindow
     */
    public void setIgnoreRepaint(boolean ignoreRepaint) {
        this.ignoreRepaint = ignoreRepaint;
    }

    /**
     * @return 是否忽略从操作系统接收的绘图消息。
     *
     * @since 1.4
     * @see #setIgnoreRepaint
     */
    public boolean getIgnoreRepaint() {
        return ignoreRepaint;
    }

    /**
     * 检查此组件是否包含指定的点，其中 <code>x</code> 和 <code>y</code> 被定义为相对于此组件的坐标系。
     * @param     x   点的 <i>x</i> 坐标
     * @param     y   点的 <i>y</i> 坐标
     * @see       #getComponentAt(int, int)
     * @since     JDK1.1
     */
    public boolean contains(int x, int y) {
        return inside(x, y);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 contains(int, int)。
     */
    @Deprecated
    public boolean inside(int x, int y) {
        return (x >= 0) && (x < width) && (y >= 0) && (y < height);
    }

    /**
     * 检查此组件是否包含指定的点，其中点的 <i>x</i> 和 <i>y</i> 坐标被定义为相对于此组件的坐标系。
     * @param     p     点
     * @throws    NullPointerException 如果 {@code p} 为 {@code null}
     * @see       #getComponentAt(Point)
     * @since     JDK1.1
     */
    public boolean contains(Point p) {
        return contains(p.x, p.y);
    }

    /**
     * 确定此组件或其直接子组件是否包含 (<i>x</i>,&nbsp;<i>y</i>) 位置，如果是，则返回包含的组件。此方法只查找一层深。如果点 (<i>x</i>,&nbsp;<i>y</i>) 位于一个有子组件的子组件内，它不会继续查找子组件树。
     * <p>
     * <code>Component</code> 的 <code>locate</code> 方法仅在其边界框内返回组件本身，否则返回 <code>null</code>。
     * @param     x   <i>x</i> 坐标
     * @param     y   <i>y</i> 坐标
     * @return    包含 (<i>x</i>,&nbsp;<i>y</i>) 位置的组件或子组件；
     *                如果位置在此组件之外，则返回 <code>null</code>
     * @see       #contains(int, int)
     * @since     JDK1.0
     */
    public Component getComponentAt(int x, int y) {
        return locate(x, y);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 getComponentAt(int, int)。
     */
    @Deprecated
    public Component locate(int x, int y) {
        return contains(x, y) ? this : null;
    }

    /**
     * 返回包含指定点的组件或子组件。
     * @param     p   点
     * @see       java.awt.Component#contains
     * @since     JDK1.1
     */
    public Component getComponentAt(Point p) {
        return getComponentAt(p.x, p.y);
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 <code>dispatchEvent(AWTEvent e)</code>。
     */
    @Deprecated
    public void deliverEvent(Event e) {
        postEvent(e);
    }

    /**
     * 将事件分派给此组件或其子组件。在返回之前，对于已为 <code>Component</code> 启用的 1.1 风格事件，调用 <code>processEvent</code>。
     * @param e 事件
     */
    public final void dispatchEvent(AWTEvent e) {
        dispatchEventImpl(e);
    }

    @SuppressWarnings("deprecation")
    void dispatchEventImpl(AWTEvent e) {
        int id = e.getID();

        // 检查此组件是否属于此应用上下文
        AppContext compContext = appContext;
        if (compContext != null && !compContext.equals(AppContext.getAppContext())) {
            if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
                eventLog.fine("Event " + e + " is being dispatched on the wrong AppContext");
            }
        }

        if (eventLog.isLoggable(PlatformLogger.Level.FINEST)) {
            eventLog.finest("{0}", e);
        }

        /*
         * 0. 设置当前事件的时间戳和修饰符。
         */
        if (!(e instanceof KeyEvent)) {
            // 键盘事件的时间戳在 DKFM.preDispatchKeyEvent(KeyEvent) 中设置。
            EventQueue.setCurrentEventAndMostRecentTime(e);
        }

        /*
         * 1. 预分派器。在通知 AWTEventListeners 之前，进行任何必要的重定向/重新排序。
         */

        if (e instanceof SunDropTargetEvent) {
            ((SunDropTargetEvent)e).dispatch();
            return;
        }

        if (!e.focusManagerIsDispatching) {
            // 调用私有的焦点重定向方法，提供轻量级组件支持
            if (e.isPosted) {
                e = KeyboardFocusManager.retargetFocusEvent(e);
                e.isPosted = true;
            }

            // 现在，如果必要，事件已正确定向到轻量级后代，调用公共焦点重定向和分派函数
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().
                dispatchEvent(e))
            {
                return;
            }
        }
        if ((e instanceof FocusEvent) && focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
            focusLog.finest("" + e);
        }
        // 鼠标滚轮事件可能需要在此处重定向，以便 AWTEventListener 看到事件发送到正确的组件。如果 MouseWheelEvent 需要发送到祖先，事件将发送到祖先，此处的分派将停止。
        if (id == MouseEvent.MOUSE_WHEEL &&
            (!eventTypeEnabled(id)) &&
            (peer != null && !peer.handlesWheelScrolling()) &&
            (dispatchMouseWheelToAncestor((MouseWheelEvent)e)))
        {
            return;
        }

        /*
         * 2. 允许工具包将此事件传递给 AWTEventListeners。
         */
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.notifyAWTEventListeners(e);


        /*
         * 3. 如果没有消费键盘事件，允许 KeyboardFocusManager 处理它。
         */
        if (!e.isConsumed()) {
            if (e instanceof java.awt.event.KeyEvent) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().
                    processKeyEvent(this, (KeyEvent)e);
                if (e.isConsumed()) {
                    return;
                }
            }
        }

        /*
         * 4. 允许输入方法处理事件
         */
        if (areInputMethodsEnabled()) {
            // 我们需要传递 InputMethodEvents，因为某些主机输入方法适配器通过 Java 事件队列而不是直接传递给组件，输入上下文还处理 Java 组合窗口
            if(((e instanceof InputMethodEvent) && !(this instanceof CompositionArea))
               ||
               // 否则，我们只传递输入和焦点事件，因为
               // a) 输入方法不应该知道语义或组件级事件
               // b) 传递事件需要时间
               // c) isConsumed() 对于语义事件始终为 true。
               (e instanceof InputEvent) || (e instanceof FocusEvent)) {
                InputContext inputContext = getInputContext();


                if (inputContext != null) {
                    inputContext.dispatchEvent(e);
                    if (e.isConsumed()) {
                        if ((e instanceof FocusEvent) && focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                            focusLog.finest("3579: Skipping " + e);
                        }
                        return;
                    }
                }
            }
        } else {
            // 当非客户端获得焦点时，需要显式禁用本机输入方法。当活动/被动/配对的客户端失去焦点时，本机输入方法实际上不会被禁用。
            if (id == FocusEvent.FOCUS_GAINED) {
                InputContext inputContext = getInputContext();
                if (inputContext != null && inputContext instanceof sun.awt.im.InputContext) {
                    ((sun.awt.im.InputContext)inputContext).disableNativeIM();
                }
            }
        }


        /*
         * 5. 在传递前预处理任何特殊事件
         */
        switch(id) {
            // PAINT 和 UPDATE 事件的处理现在在 peer 的 handleEvent() 方法中完成，以便在 Windows 上仅选择性地清除非本机组件的背景。
            // - Fred.Ecks@Eng.sun.com, 5-8-98

          case KeyEvent.KEY_PRESSED:
          case KeyEvent.KEY_RELEASED:
              Container p = (Container)((this instanceof Container) ? this : parent);
              if (p != null) {
                  p.preProcessKeyEvent((KeyEvent)e);
                  if (e.isConsumed()) {
                        if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                            focusLog.finest("Pre-process consumed event");
                        }
                      return;
                  }
              }
              break;

          case WindowEvent.WINDOW_CLOSING:
              if (toolkit instanceof WindowClosingListener) {
                  windowClosingException = ((WindowClosingListener)
                                            toolkit).windowClosingNotify((WindowEvent)e);
                  if (checkWindowClosingException()) {
                      return;
                  }
              }
              break;

          default:
              break;
        }

        /*
         * 6. 传递事件以进行正常处理
         */
        if (newEventsOnly) {
            // 过滤需要真正移到较低级别以获得最大性能增益；暂时在此处确保 API 规范得到遵守。
            //
            if (eventEnabled(e)) {
                processEvent(e);
            }
        } else if (id == MouseEvent.MOUSE_WHEEL) {
            // 对于没有监听器的 ScrollPane，newEventsOnly 将为 false，但仍然需要分派 MouseWheelEvents 以便进行滚动。
            autoProcessMouseWheel((MouseWheelEvent)e);
        } else if (!(e instanceof MouseEvent && !postsOldMouseEvents())) {
            //
            // 向后兼容
            //
            Event olde = e.convertToOld();
            if (olde != null) {
                int key = olde.key;
                int modifiers = olde.modifiers;

                postEvent(olde);
                if (olde.isConsumed()) {
                    e.consume();
                }
                // 如果目标更改了键或修饰符值，将它们复制回原始事件
                //
                switch(olde.id) {
                  case Event.KEY_PRESS:
                  case Event.KEY_RELEASE:
                  case Event.KEY_ACTION:
                  case Event.KEY_ACTION_RELEASE:
                      if (olde.key != key) {
                          ((KeyEvent)e).setKeyChar(olde.getKeyEventChar());
                      }
                      if (olde.modifiers != modifiers) {
                          ((KeyEvent)e).setModifiers(olde.modifiers);
                      }
                      break;
                  default:
                      break;
                }
            }
        }


                    /*
         * 8. 特殊处理 4061116：为浏览器关闭模态对话框提供钩子。
         */
        if (id == WindowEvent.WINDOW_CLOSING && !e.isConsumed()) {
            if (toolkit instanceof WindowClosingListener) {
                windowClosingException =
                    ((WindowClosingListener)toolkit).
                    windowClosingDelivered((WindowEvent)e);
                if (checkWindowClosingException()) {
                    return;
                }
            }
        }

        /*
         * 9. 允许对等体处理事件。
         * 除了 KeyEvent，它们将在所有 KeyEventPostProcessors 之后由对等体处理
         * (参见 DefaultKeyboardFocusManager.dispatchKeyEvent())
         */
        if (!(e instanceof KeyEvent)) {
            ComponentPeer tpeer = peer;
            if (e instanceof FocusEvent && (tpeer == null || tpeer instanceof LightweightPeer)) {
                // 如果焦点所有者是轻量级组件，则其本机容器处理事件
                Component source = (Component)e.getSource();
                if (source != null) {
                    Container target = source.getNativeContainer();
                    if (target != null) {
                        tpeer = target.getPeer();
                    }
                }
            }
            if (tpeer != null) {
                tpeer.handleEvent(e);
            }
        }

        if (SunToolkit.isTouchKeyboardAutoShowEnabled() &&
            (toolkit instanceof SunToolkit) &&
            ((e instanceof MouseEvent) || (e instanceof FocusEvent))) {
            ((SunToolkit)toolkit).showOrHideTouchKeyboard(this, e);
        }
    } // dispatchEventImpl()

    /*
     * 如果 newEventsOnly 为 false，方法被调用以便 ScrollPane 可以
     * 覆盖它并处理常见的鼠标滚轮滚动。 对于 Component，此方法为空操作。
     */
    void autoProcessMouseWheel(MouseWheelEvent e) {}

    /*
     * 将给定的 MouseWheelEvent 派发给第一个启用 MouseWheelEvents 的祖先。
     *
     * 返回事件是否已派发给祖先
     */
    boolean dispatchMouseWheelToAncestor(MouseWheelEvent e) {
        int newX, newY;
        newX = e.getX() + getX(); // 坐标考虑至少
        newY = e.getY() + getY(); // 光标的相对位置（e.getX()），以及此
                                  // 组件相对于其父组件的位置。
        MouseWheelEvent newMWE;

        if (eventLog.isLoggable(PlatformLogger.Level.FINEST)) {
            eventLog.finest("dispatchMouseWheelToAncestor");
            eventLog.finest("orig event src is of " + e.getSource().getClass());
        }

        /* 窗口的 parent 字段指的是拥有窗口。
         * MouseWheelEvents 不应传播到拥有窗口
         */
        synchronized (getTreeLock()) {
            Container anc = getParent();
            while (anc != null && !anc.eventEnabled(e)) {
                // 修正新事件源的相对坐标
                newX += anc.getX();
                newY += anc.getY();

                if (!(anc instanceof Window)) {
                    anc = anc.getParent();
                }
                else {
                    break;
                }
            }

            if (eventLog.isLoggable(PlatformLogger.Level.FINEST)) {
                eventLog.finest("new event src is " + anc.getClass());
            }

            if (anc != null && anc.eventEnabled(e)) {
                // 更改事件以从新源发出，带有新的 x, y
                // 暂时，只是创建一个新事件 - 很糟糕

                newMWE = new MouseWheelEvent(anc, // 新源
                                             e.getID(),
                                             e.getWhen(),
                                             e.getModifiers(),
                                             newX, // 相对于新源的 x
                                             newY, // 相对于新源的 y
                                             e.getXOnScreen(),
                                             e.getYOnScreen(),
                                             e.getClickCount(),
                                             e.isPopupTrigger(),
                                             e.getScrollType(),
                                             e.getScrollAmount(),
                                             e.getWheelRotation(),
                                             e.getPreciseWheelRotation());
                ((AWTEvent)e).copyPrivateDataInto(newMWE);
                // 当将滚轮事件派发给祖先时，
                // 不需要尝试找到要派发事件的后代轻量级组件。
                // 如果我们派发事件到顶级祖先，
                // 这可能会形成循环：6480024。
                anc.dispatchEventToSelf(newMWE);
                if (newMWE.isConsumed()) {
                    e.consume();
                }
                return true;
            }
        }
        return false;
    }

    boolean checkWindowClosingException() {
        if (windowClosingException != null) {
            if (this instanceof Dialog) {
                ((Dialog)this).interruptBlocking();
            } else {
                windowClosingException.fillInStackTrace();
                windowClosingException.printStackTrace();
                windowClosingException = null;
            }
            return true;
        }
        return false;
    }

    boolean areInputMethodsEnabled() {
        // 在 1.2 中，我们假设所有处理键事件的组件都需要输入方法支持，但组件可以通过调用 enableInputMethods(false) 关闭输入方法。
        return ((eventMask & AWTEvent.INPUT_METHODS_ENABLED_MASK) != 0) &&
            ((eventMask & AWTEvent.KEY_EVENT_MASK) != 0 || keyListener != null);
    }

    // 提醒：在较低级别处理过滤时删除
    boolean eventEnabled(AWTEvent e) {
        return eventTypeEnabled(e.id);
    }

    boolean eventTypeEnabled(int type) {
        switch(type) {
          case ComponentEvent.COMPONENT_MOVED:
          case ComponentEvent.COMPONENT_RESIZED:
          case ComponentEvent.COMPONENT_SHOWN:
          case ComponentEvent.COMPONENT_HIDDEN:
              if ((eventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0 ||
                  componentListener != null) {
                  return true;
              }
              break;
          case FocusEvent.FOCUS_GAINED:
          case FocusEvent.FOCUS_LOST:
              if ((eventMask & AWTEvent.FOCUS_EVENT_MASK) != 0 ||
                  focusListener != null) {
                  return true;
              }
              break;
          case KeyEvent.KEY_PRESSED:
          case KeyEvent.KEY_RELEASED:
          case KeyEvent.KEY_TYPED:
              if ((eventMask & AWTEvent.KEY_EVENT_MASK) != 0 ||
                  keyListener != null) {
                  return true;
              }
              break;
          case MouseEvent.MOUSE_PRESSED:
          case MouseEvent.MOUSE_RELEASED:
          case MouseEvent.MOUSE_ENTERED:
          case MouseEvent.MOUSE_EXITED:
          case MouseEvent.MOUSE_CLICKED:
              if ((eventMask & AWTEvent.MOUSE_EVENT_MASK) != 0 ||
                  mouseListener != null) {
                  return true;
              }
              break;
          case MouseEvent.MOUSE_MOVED:
          case MouseEvent.MOUSE_DRAGGED:
              if ((eventMask & AWTEvent.MOUSE_MOTION_EVENT_MASK) != 0 ||
                  mouseMotionListener != null) {
                  return true;
              }
              break;
          case MouseEvent.MOUSE_WHEEL:
              if ((eventMask & AWTEvent.MOUSE_WHEEL_EVENT_MASK) != 0 ||
                  mouseWheelListener != null) {
                  return true;
              }
              break;
          case InputMethodEvent.INPUT_METHOD_TEXT_CHANGED:
          case InputMethodEvent.CARET_POSITION_CHANGED:
              if ((eventMask & AWTEvent.INPUT_METHOD_EVENT_MASK) != 0 ||
                  inputMethodListener != null) {
                  return true;
              }
              break;
          case HierarchyEvent.HIERARCHY_CHANGED:
              if ((eventMask & AWTEvent.HIERARCHY_EVENT_MASK) != 0 ||
                  hierarchyListener != null) {
                  return true;
              }
              break;
          case HierarchyEvent.ANCESTOR_MOVED:
          case HierarchyEvent.ANCESTOR_RESIZED:
              if ((eventMask & AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK) != 0 ||
                  hierarchyBoundsListener != null) {
                  return true;
              }
              break;
          case ActionEvent.ACTION_PERFORMED:
              if ((eventMask & AWTEvent.ACTION_EVENT_MASK) != 0) {
                  return true;
              }
              break;
          case TextEvent.TEXT_VALUE_CHANGED:
              if ((eventMask & AWTEvent.TEXT_EVENT_MASK) != 0) {
                  return true;
              }
              break;
          case ItemEvent.ITEM_STATE_CHANGED:
              if ((eventMask & AWTEvent.ITEM_EVENT_MASK) != 0) {
                  return true;
              }
              break;
          case AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED:
              if ((eventMask & AWTEvent.ADJUSTMENT_EVENT_MASK) != 0) {
                  return true;
              }
              break;
          default:
              break;
        }
        //
        // 始终传递由外部程序定义的事件。
        //
        if (type > AWTEvent.RESERVED_ID_MAX) {
            return true;
        }
        return false;
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 由 dispatchEvent(AWTEvent) 替代。
     */
    @Deprecated
    public boolean postEvent(Event e) {
        ComponentPeer peer = this.peer;

        if (handleEvent(e)) {
            e.consume();
            return true;
        }

        Component parent = this.parent;
        int eventx = e.x;
        int eventy = e.y;
        if (parent != null) {
            e.translate(x, y);
            if (parent.postEvent(e)) {
                e.consume();
                return true;
            }
            // 恢复坐标
            e.x = eventx;
            e.y = eventy;
        }
        return false;
    }

    // 事件源接口

    /**
     * 添加指定的组件监听器以接收此组件的组件事件。
     * 如果监听器 <code>l</code> 为 <code>null</code>，
     * 不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   组件监听器
     * @see      java.awt.event.ComponentEvent
     * @see      java.awt.event.ComponentListener
     * @see      #removeComponentListener
     * @see      #getComponentListeners
     * @since    JDK1.1
     */
    public synchronized void addComponentListener(ComponentListener l) {
        if (l == null) {
            return;
        }
        componentListener = AWTEventMulticaster.add(componentListener, l);
        newEventsOnly = true;
    }

    /**
     * 移除指定的组件监听器，使其不再接收此组件的组件事件。如果指定的监听器
     * 未先前添加到此组件，此方法不执行任何操作，也不抛出异常。
     * 如果监听器 <code>l</code> 为 <code>null</code>，
     * 不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     * @param    l   组件监听器
     * @see      java.awt.event.ComponentEvent
     * @see      java.awt.event.ComponentListener
     * @see      #addComponentListener
     * @see      #getComponentListeners
     * @since    JDK1.1
     */
    public synchronized void removeComponentListener(ComponentListener l) {
        if (l == null) {
            return;
        }
        componentListener = AWTEventMulticaster.remove(componentListener, l);
    }

    /**
     * 返回注册在此组件上的所有组件监听器的数组。
     *
     * @return 此组件的所有 <code>ComponentListener</code>，如果没有注册组件
     *         监听器，则返回空数组
     *
     * @see #addComponentListener
     * @see #removeComponentListener
     * @since 1.4
     */
    public synchronized ComponentListener[] getComponentListeners() {
        return getListeners(ComponentListener.class);
    }

    /**
     * 添加指定的焦点监听器以接收此组件获得输入焦点时的焦点事件。
     * 如果监听器 <code>l</code> 为 <code>null</code>，
     * 不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   焦点监听器
     * @see      java.awt.event.FocusEvent
     * @see      java.awt.event.FocusListener
     * @see      #removeFocusListener
     * @see      #getFocusListeners
     * @since    JDK1.1
     */
    public synchronized void addFocusListener(FocusListener l) {
        if (l == null) {
            return;
        }
        focusListener = AWTEventMulticaster.add(focusListener, l);
        newEventsOnly = true;

        // 如果这是轻量级组件，则在本机容器中启用焦点事件。
        if (peer instanceof LightweightPeer) {
            parent.proxyEnableEvents(AWTEvent.FOCUS_EVENT_MASK);
        }
    }

    /**
     * 移除指定的焦点监听器，使其不再接收此组件的焦点事件。如果指定的监听器
     * 未先前添加到此组件，此方法不执行任何操作，也不抛出异常。
     * 如果监听器 <code>l</code> 为 <code>null</code>，
     * 不抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   焦点监听器
     * @see      java.awt.event.FocusEvent
     * @see      java.awt.event.FocusListener
     * @see      #addFocusListener
     * @see      #getFocusListeners
     * @since    JDK1.1
     */
    public synchronized void removeFocusListener(FocusListener l) {
        if (l == null) {
            return;
        }
        focusListener = AWTEventMulticaster.remove(focusListener, l);
    }

    /**
     * 返回注册在此组件上的所有焦点监听器的数组。
     *
     * @return 此组件的所有 <code>FocusListener</code>，如果没有注册组件
     *         监听器，则返回空数组
     *
     * @see #addFocusListener
     * @see #removeFocusListener
     * @since 1.4
     */
    public synchronized FocusListener[] getFocusListeners() {
        return getListeners(FocusListener.class);
    }


                /**
     * 将指定的层次结构监听器添加到此组件，以便在该容器所属的层次结构发生变化时接收层次结构更改事件。
     * 如果监听器 <code>l</code> 为 <code>null</code>，则不会抛出异常，也不会执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   层次结构监听器
     * @see      java.awt.event.HierarchyEvent
     * @see      java.awt.event.HierarchyListener
     * @see      #removeHierarchyListener
     * @see      #getHierarchyListeners
     * @since    1.3
     */
    public void addHierarchyListener(HierarchyListener l) {
        if (l == null) {
            return;
        }
        boolean notifyAncestors;
        synchronized (this) {
            notifyAncestors =
                (hierarchyListener == null &&
                 (eventMask & AWTEvent.HIERARCHY_EVENT_MASK) == 0);
            hierarchyListener = AWTEventMulticaster.add(hierarchyListener, l);
            notifyAncestors = (notifyAncestors && hierarchyListener != null);
            newEventsOnly = true;
        }
        if (notifyAncestors) {
            synchronized (getTreeLock()) {
                adjustListeningChildrenOnParent(AWTEvent.HIERARCHY_EVENT_MASK,
                                                1);
            }
        }
    }

    /**
     * 移除指定的层次结构监听器，使其不再从该组件接收层次结构更改事件。如果指定的监听器未先前添加到此组件，此方法不会执行任何功能，也不会抛出异常。
     * 如果监听器 <code>l</code> 为 <code>null</code>，则不会抛出异常，也不会执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   层次结构监听器
     * @see      java.awt.event.HierarchyEvent
     * @see      java.awt.event.HierarchyListener
     * @see      #addHierarchyListener
     * @see      #getHierarchyListeners
     * @since    1.3
     */
    public void removeHierarchyListener(HierarchyListener l) {
        if (l == null) {
            return;
        }
        boolean notifyAncestors;
        synchronized (this) {
            notifyAncestors =
                (hierarchyListener != null &&
                 (eventMask & AWTEvent.HIERARCHY_EVENT_MASK) == 0);
            hierarchyListener =
                AWTEventMulticaster.remove(hierarchyListener, l);
            notifyAncestors = (notifyAncestors && hierarchyListener == null);
        }
        if (notifyAncestors) {
            synchronized (getTreeLock()) {
                adjustListeningChildrenOnParent(AWTEvent.HIERARCHY_EVENT_MASK,
                                                -1);
            }
        }
    }

    /**
     * 返回在此组件上注册的所有层次结构监听器的数组。
     *
     * @return 此组件的所有 <code>HierarchyListener</code>，如果当前未注册任何层次结构监听器，则返回空数组
     *
     * @see      #addHierarchyListener
     * @see      #removeHierarchyListener
     * @since    1.4
     */
    public synchronized HierarchyListener[] getHierarchyListeners() {
        return getListeners(HierarchyListener.class);
    }

    /**
     * 将指定的层次结构边界监听器添加到此组件，以便在该容器所属的层次结构发生变化时接收层次结构边界事件。
     * 如果监听器 <code>l</code> 为 <code>null</code>，则不会抛出异常，也不会执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   层次结构边界监听器
     * @see      java.awt.event.HierarchyEvent
     * @see      java.awt.event.HierarchyBoundsListener
     * @see      #removeHierarchyBoundsListener
     * @see      #getHierarchyBoundsListeners
     * @since    1.3
     */
    public void addHierarchyBoundsListener(HierarchyBoundsListener l) {
        if (l == null) {
            return;
        }
        boolean notifyAncestors;
        synchronized (this) {
            notifyAncestors =
                (hierarchyBoundsListener == null &&
                 (eventMask & AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK) == 0);
            hierarchyBoundsListener =
                AWTEventMulticaster.add(hierarchyBoundsListener, l);
            notifyAncestors = (notifyAncestors &&
                               hierarchyBoundsListener != null);
            newEventsOnly = true;
        }
        if (notifyAncestors) {
            synchronized (getTreeLock()) {
                adjustListeningChildrenOnParent(
                                                AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK, 1);
            }
        }
    }

    /**
     * 移除指定的层次结构边界监听器，使其不再从该组件接收层次结构边界事件。如果指定的监听器未先前添加到此组件，此方法不会执行任何功能，也不会抛出异常。
     * 如果监听器 <code>l</code> 为 <code>null</code>，则不会抛出异常，也不会执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   层次结构边界监听器
     * @see      java.awt.event.HierarchyEvent
     * @see      java.awt.event.HierarchyBoundsListener
     * @see      #addHierarchyBoundsListener
     * @see      #getHierarchyBoundsListeners
     * @since    1.3
     */
    public void removeHierarchyBoundsListener(HierarchyBoundsListener l) {
        if (l == null) {
            return;
        }
        boolean notifyAncestors;
        synchronized (this) {
            notifyAncestors =
                (hierarchyBoundsListener != null &&
                 (eventMask & AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK) == 0);
            hierarchyBoundsListener =
                AWTEventMulticaster.remove(hierarchyBoundsListener, l);
            notifyAncestors = (notifyAncestors &&
                               hierarchyBoundsListener == null);
        }
        if (notifyAncestors) {
            synchronized (getTreeLock()) {
                adjustListeningChildrenOnParent(
                                                AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK, -1);
            }
        }
    }

    // 仅在持有树锁时调用
    int numListening(long mask) {
        // 一个掩码或另一个，但不能同时为两者或两者都不是。
        if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
            if ((mask != AWTEvent.HIERARCHY_EVENT_MASK) &&
                (mask != AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK))
            {
                eventLog.fine("断言失败");
            }
        }
        if ((mask == AWTEvent.HIERARCHY_EVENT_MASK &&
             (hierarchyListener != null ||
              (eventMask & AWTEvent.HIERARCHY_EVENT_MASK) != 0)) ||
            (mask == AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK &&
             (hierarchyBoundsListener != null ||
              (eventMask & AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK) != 0))) {
            return 1;
        } else {
            return 0;
        }
    }

    // 仅在持有树锁时调用
    int countHierarchyMembers() {
        return 1;
    }
    // 仅在持有树锁时调用
    int createHierarchyEvents(int id, Component changed,
                              Container changedParent, long changeFlags,
                              boolean enabledOnToolkit) {
        switch (id) {
          case HierarchyEvent.HIERARCHY_CHANGED:
              if (hierarchyListener != null ||
                  (eventMask & AWTEvent.HIERARCHY_EVENT_MASK) != 0 ||
                  enabledOnToolkit) {
                  HierarchyEvent e = new HierarchyEvent(this, id, changed,
                                                        changedParent,
                                                        changeFlags);
                  dispatchEvent(e);
                  return 1;
              }
              break;
          case HierarchyEvent.ANCESTOR_MOVED:
          case HierarchyEvent.ANCESTOR_RESIZED:
              if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
                  if (changeFlags != 0) {
                      eventLog.fine("断言 (changeFlags == 0) 失败");
                  }
              }
              if (hierarchyBoundsListener != null ||
                  (eventMask & AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK) != 0 ||
                  enabledOnToolkit) {
                  HierarchyEvent e = new HierarchyEvent(this, id, changed,
                                                        changedParent);
                  dispatchEvent(e);
                  return 1;
              }
              break;
          default:
              // 断言 false
              if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
                  eventLog.fine("此代码绝不能到达");
              }
              break;
        }
        return 0;
    }

    /**
     * 返回在此组件上注册的所有层次结构边界监听器的数组。
     *
     * @return 此组件的所有 <code>HierarchyBoundsListener</code>，如果当前未注册任何层次结构边界监听器，则返回空数组
     *
     * @see      #addHierarchyBoundsListener
     * @see      #removeHierarchyBoundsListener
     * @since    1.4
     */
    public synchronized HierarchyBoundsListener[] getHierarchyBoundsListeners() {
        return getListeners(HierarchyBoundsListener.class);
    }

    /*
     * 仅在持有树锁时调用。
     * 仅为了在 java.awt.Window 中重写而添加，因为 Window 的父级是所有者。
     */
    void adjustListeningChildrenOnParent(long mask, int num) {
        if (parent != null) {
            parent.adjustListeningChildren(mask, num);
        }
    }

    /**
     * 将指定的键盘监听器添加到此组件，以便接收键盘事件。
     * 如果 l 为 null，不会抛出异常，也不会执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   键盘监听器
     * @see      java.awt.event.KeyEvent
     * @see      java.awt.event.KeyListener
     * @see      #removeKeyListener
     * @see      #getKeyListeners
     * @since    JDK1.1
     */
    public synchronized void addKeyListener(KeyListener l) {
        if (l == null) {
            return;
        }
        keyListener = AWTEventMulticaster.add(keyListener, l);
        newEventsOnly = true;

        // 如果这是轻量级组件，则在本机容器中启用键盘事件。
        if (peer instanceof LightweightPeer) {
            parent.proxyEnableEvents(AWTEvent.KEY_EVENT_MASK);
        }
    }

    /**
     * 移除指定的键盘监听器，使其不再从该组件接收键盘事件。如果指定的监听器未先前添加到此组件，此方法不会执行任何功能，也不会抛出异常。
     * 如果监听器 <code>l</code> 为 <code>null</code>，则不会抛出异常，也不会执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   键盘监听器
     * @see      java.awt.event.KeyEvent
     * @see      java.awt.event.KeyListener
     * @see      #addKeyListener
     * @see      #getKeyListeners
     * @since    JDK1.1
     */
    public synchronized void removeKeyListener(KeyListener l) {
        if (l == null) {
            return;
        }
        keyListener = AWTEventMulticaster.remove(keyListener, l);
    }

    /**
     * 返回在此组件上注册的所有键盘监听器的数组。
     *
     * @return 此组件的所有 <code>KeyListener</code>，如果当前未注册任何键盘监听器，则返回空数组
     *
     * @see      #addKeyListener
     * @see      #removeKeyListener
     * @since    1.4
     */
    public synchronized KeyListener[] getKeyListeners() {
        return getListeners(KeyListener.class);
    }

    /**
     * 将指定的鼠标监听器添加到此组件，以便接收鼠标事件。
     * 如果监听器 <code>l</code> 为 <code>null</code>，则不会抛出异常，也不会执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   鼠标监听器
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseListener
     * @see      #removeMouseListener
     * @see      #getMouseListeners
     * @since    JDK1.1
     */
    public synchronized void addMouseListener(MouseListener l) {
        if (l == null) {
            return;
        }
        mouseListener = AWTEventMulticaster.add(mouseListener,l);
        newEventsOnly = true;

        // 如果这是轻量级组件，则在本机容器中启用鼠标事件。
        if (peer instanceof LightweightPeer) {
            parent.proxyEnableEvents(AWTEvent.MOUSE_EVENT_MASK);
        }
    }

    /**
     * 移除指定的鼠标监听器，使其不再从该组件接收鼠标事件。如果指定的监听器未先前添加到此组件，此方法不会执行任何功能，也不会抛出异常。
     * 如果监听器 <code>l</code> 为 <code>null</code>，则不会抛出异常，也不会执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   鼠标监听器
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseListener
     * @see      #addMouseListener
     * @see      #getMouseListeners
     * @since    JDK1.1
     */
    public synchronized void removeMouseListener(MouseListener l) {
        if (l == null) {
            return;
        }
        mouseListener = AWTEventMulticaster.remove(mouseListener, l);
    }

    /**
     * 返回在此组件上注册的所有鼠标监听器的数组。
     *
     * @return 此组件的所有 <code>MouseListener</code>，如果当前未注册任何鼠标监听器，则返回空数组
     *
     * @see      #addMouseListener
     * @see      #removeMouseListener
     * @since    1.4
     */
    public synchronized MouseListener[] getMouseListeners() {
        return getListeners(MouseListener.class);
    }


                /**
     * 添加指定的鼠标移动监听器以接收来自此组件的鼠标移动事件。
     * 如果监听器 <code>l</code> 为 <code>null</code>，
     * 不会抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   鼠标移动监听器
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseMotionListener
     * @see      #removeMouseMotionListener
     * @see      #getMouseMotionListeners
     * @since    JDK1.1
     */
    public synchronized void addMouseMotionListener(MouseMotionListener l) {
        if (l == null) {
            return;
        }
        mouseMotionListener = AWTEventMulticaster.add(mouseMotionListener,l);
        newEventsOnly = true;

        // 如果这是一个轻量级组件，则在本机容器中启用鼠标事件。
        if (peer instanceof LightweightPeer) {
            parent.proxyEnableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
        }
    }

    /**
     * 移除指定的鼠标移动监听器，使其不再接收来自此组件的鼠标移动事件。如果指定的监听器
     * 从未添加到此组件，则此方法不执行任何操作，也不抛出异常。
     * 如果监听器 <code>l</code> 为 <code>null</code>，
     * 不会抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   鼠标移动监听器
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseMotionListener
     * @see      #addMouseMotionListener
     * @see      #getMouseMotionListeners
     * @since    JDK1.1
     */
    public synchronized void removeMouseMotionListener(MouseMotionListener l) {
        if (l == null) {
            return;
        }
        mouseMotionListener = AWTEventMulticaster.remove(mouseMotionListener, l);
    }

    /**
     * 返回注册在此组件上的所有鼠标移动监听器的数组。
     *
     * @return 此组件的所有 <code>MouseMotionListener</code>，如果没有注册鼠标移动监听器，则返回空数组。
     *
     * @see      #addMouseMotionListener
     * @see      #removeMouseMotionListener
     * @since    1.4
     */
    public synchronized MouseMotionListener[] getMouseMotionListeners() {
        return getListeners(MouseMotionListener.class);
    }

    /**
     * 添加指定的鼠标滚轮监听器以接收来自此组件的鼠标滚轮事件。容器还会接收子组件的鼠标滚轮事件。
     * <p>
     * 有关鼠标滚轮事件的分发方式，请参阅 {@link MouseWheelEvent} 的类描述。
     * <p>
     * 如果 <code>l</code> 为 <code>null</code>，不会抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   鼠标滚轮监听器
     * @see      java.awt.event.MouseWheelEvent
     * @see      java.awt.event.MouseWheelListener
     * @see      #removeMouseWheelListener
     * @see      #getMouseWheelListeners
     * @since    1.4
     */
    public synchronized void addMouseWheelListener(MouseWheelListener l) {
        if (l == null) {
            return;
        }
        mouseWheelListener = AWTEventMulticaster.add(mouseWheelListener,l);
        newEventsOnly = true;

        // 如果这是一个轻量级组件，则在本机容器中启用鼠标事件。
        if (peer instanceof LightweightPeer) {
            parent.proxyEnableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        }
    }

    /**
     * 移除指定的鼠标滚轮监听器，使其不再接收来自此组件的鼠标滚轮事件。如果指定的监听器
     * 从未添加到此组件，则此方法不执行任何操作，也不抛出异常。
     * 如果 <code>l</code> 为 <code>null</code>，不会抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   鼠标滚轮监听器。
     * @see      java.awt.event.MouseWheelEvent
     * @see      java.awt.event.MouseWheelListener
     * @see      #addMouseWheelListener
     * @see      #getMouseWheelListeners
     * @since    1.4
     */
    public synchronized void removeMouseWheelListener(MouseWheelListener l) {
        if (l == null) {
            return;
        }
        mouseWheelListener = AWTEventMulticaster.remove(mouseWheelListener, l);
    }

    /**
     * 返回注册在此组件上的所有鼠标滚轮监听器的数组。
     *
     * @return 此组件的所有 <code>MouseWheelListener</code>，如果没有注册鼠标滚轮监听器，则返回空数组。
     *
     * @see      #addMouseWheelListener
     * @see      #removeMouseWheelListener
     * @since    1.4
     */
    public synchronized MouseWheelListener[] getMouseWheelListeners() {
        return getListeners(MouseWheelListener.class);
    }

    /**
     * 添加指定的输入方法监听器以接收来自此组件的输入方法事件。组件只有在重写 <code>getInputMethodRequests</code> 以返回
     * <code>InputMethodRequests</code> 实例时才会从输入方法接收输入方法事件。
     * 如果监听器 <code>l</code> 为 <code>null</code>，
     * 不会抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="{@docRoot}/java/awt/doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   输入方法监听器
     * @see      java.awt.event.InputMethodEvent
     * @see      java.awt.event.InputMethodListener
     * @see      #removeInputMethodListener
     * @see      #getInputMethodListeners
     * @see      #getInputMethodRequests
     * @since    1.2
     */
    public synchronized void addInputMethodListener(InputMethodListener l) {
        if (l == null) {
            return;
        }
        inputMethodListener = AWTEventMulticaster.add(inputMethodListener, l);
        newEventsOnly = true;
    }

    /**
     * 移除指定的输入方法监听器，使其不再接收来自此组件的输入方法事件。如果指定的监听器
     * 从未添加到此组件，则此方法不执行任何操作，也不抛出异常。
     * 如果监听器 <code>l</code> 为 <code>null</code>，
     * 不会抛出异常且不执行任何操作。
     * <p>有关 AWT 线程模型的详细信息，请参阅 <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param    l   输入方法监听器
     * @see      java.awt.event.InputMethodEvent
     * @see      java.awt.event.InputMethodListener
     * @see      #addInputMethodListener
     * @see      #getInputMethodListeners
     * @since    1.2
     */
    public synchronized void removeInputMethodListener(InputMethodListener l) {
        if (l == null) {
            return;
        }
        inputMethodListener = AWTEventMulticaster.remove(inputMethodListener, l);
    }

    /**
     * 返回注册在此组件上的所有输入方法监听器的数组。
     *
     * @return 此组件的所有 <code>InputMethodListener</code>，如果没有注册输入方法监听器，则返回空数组。
     *
     * @see      #addInputMethodListener
     * @see      #removeInputMethodListener
     * @since    1.4
     */
    public synchronized InputMethodListener[] getInputMethodListeners() {
        return getListeners(InputMethodListener.class);
    }

    /**
     * 返回当前注册在此 <code>Component</code> 上的所有 <code><em>Foo</em>Listener</code> 的数组。
     * <code><em>Foo</em>Listener</code> 通过 <code>add<em>Foo</em>Listener</code> 方法注册。
     *
     * <p>
     * 可以使用类字面量指定 <code>listenerType</code> 参数，例如
     * <code><em>Foo</em>Listener.class</code>。例如，可以使用以下代码查询
     * <code>Component</code> <code>c</code> 的鼠标监听器：
     *
     * <pre>MouseListener[] mls = (MouseListener[])(c.getListeners(MouseListener.class));</pre>
     *
     * 如果没有这样的监听器，此方法返回空数组。
     *
     * @param listenerType 请求的监听器类型；此参数应指定一个实现
     *          <code>java.util.EventListener</code> 的类或接口
     * @return 此组件上注册的所有 <code><em>Foo</em>Listener</code> 的数组，
     *          如果没有添加这样的监听器，则返回空数组
     * @exception ClassCastException 如果 <code>listenerType</code>
     *          不指定实现 <code>java.util.EventListener</code> 的类或接口
     * @throws NullPointerException 如果 {@code listenerType} 为 {@code null}
     * @see #getComponentListeners
     * @see #getFocusListeners
     * @see #getHierarchyListeners
     * @see #getHierarchyBoundsListeners
     * @see #getKeyListeners
     * @see #getMouseListeners
     * @see #getMouseMotionListeners
     * @see #getMouseWheelListeners
     * @see #getInputMethodListeners
     * @see #getPropertyChangeListeners
     *
     * @since 1.3
     */
    @SuppressWarnings("unchecked")
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if  (listenerType == ComponentListener.class) {
            l = componentListener;
        } else if (listenerType == FocusListener.class) {
            l = focusListener;
        } else if (listenerType == HierarchyListener.class) {
            l = hierarchyListener;
        } else if (listenerType == HierarchyBoundsListener.class) {
            l = hierarchyBoundsListener;
        } else if (listenerType == KeyListener.class) {
            l = keyListener;
        } else if (listenerType == MouseListener.class) {
            l = mouseListener;
        } else if (listenerType == MouseMotionListener.class) {
            l = mouseMotionListener;
        } else if (listenerType == MouseWheelListener.class) {
            l = mouseWheelListener;
        } else if (listenerType == InputMethodListener.class) {
            l = inputMethodListener;
        } else if (listenerType == PropertyChangeListener.class) {
            return (T[])getPropertyChangeListeners();
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }

    /**
     * 获取支持此组件的输入方法请求的输入方法请求处理程序。支持原位文本输入的组件
     * 必须重写此方法以返回 <code>InputMethodRequests</code> 实例。同时，它还必须处理输入方法事件。
     *
     * @return 此组件的输入方法请求处理程序，默认为 <code>null</code>
     * @see #addInputMethodListener
     * @since 1.2
     */
    public InputMethodRequests getInputMethodRequests() {
        return null;
    }

    /**
     * 获取此组件用于处理在此组件中输入文本时与输入方法通信的输入上下文。默认情况下，返回父组件使用的输入上下文。组件可以
     * 重写此方法以返回私有的输入上下文。
     *
     * @return 此组件使用的输入上下文；如果无法确定上下文，则返回 <code>null</code>
     * @since 1.2
     */
    public InputContext getInputContext() {
        Container parent = this.parent;
        if (parent == null) {
            return null;
        } else {
            return parent.getInputContext();
        }
    }

    /**
     * 启用由指定事件掩码参数定义的事件被传递到此组件。
     * <p>
     * 当为特定事件类型添加监听器时，事件类型会自动启用。
     * <p>
     * 只有当 <code>Component</code> 的子类希望在没有注册监听器的情况下将指定的事件类型传递到 <code>processEvent</code> 时，
     * 才需要调用此方法。
     * @param      eventsToEnable   定义事件类型的事件掩码
     * @see        #processEvent
     * @see        #disableEvents
     * @see        AWTEvent
     * @since      JDK1.1
     */
    protected final void enableEvents(long eventsToEnable) {
        long notifyAncestors = 0;
        synchronized (this) {
            if ((eventsToEnable & AWTEvent.HIERARCHY_EVENT_MASK) != 0 &&
                hierarchyListener == null &&
                (eventMask & AWTEvent.HIERARCHY_EVENT_MASK) == 0) {
                notifyAncestors |= AWTEvent.HIERARCHY_EVENT_MASK;
            }
            if ((eventsToEnable & AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK) != 0 &&
                hierarchyBoundsListener == null &&
                (eventMask & AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK) == 0) {
                notifyAncestors |= AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK;
            }
            eventMask |= eventsToEnable;
            newEventsOnly = true;
        }

        // 如果这是一个轻量级组件，则在本机容器中启用鼠标事件。
        if (peer instanceof LightweightPeer) {
            parent.proxyEnableEvents(eventMask);
        }
        if (notifyAncestors != 0) {
            synchronized (getTreeLock()) {
                adjustListeningChildrenOnParent(notifyAncestors, 1);
            }
        }
    }

    /**
     * 禁用由指定事件掩码参数定义的事件被传递到此组件。
     * @param      eventsToDisable   定义事件类型的事件掩码
     * @see        #enableEvents
     * @since      JDK1.1
     */
    protected final void disableEvents(long eventsToDisable) {
        long notifyAncestors = 0;
        synchronized (this) {
            if ((eventsToDisable & AWTEvent.HIERARCHY_EVENT_MASK) != 0 &&
                hierarchyListener == null &&
                (eventMask & AWTEvent.HIERARCHY_EVENT_MASK) != 0) {
                notifyAncestors |= AWTEvent.HIERARCHY_EVENT_MASK;
            }
            if ((eventsToDisable & AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK)!=0 &&
                hierarchyBoundsListener == null &&
                (eventMask & AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK) != 0) {
                notifyAncestors |= AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK;
            }
            eventMask &= ~eventsToDisable;
        }
        if (notifyAncestors != 0) {
            synchronized (getTreeLock()) {
                adjustListeningChildrenOnParent(notifyAncestors, -1);
            }
        }
    }


                transient sun.awt.EventQueueItem[] eventCache;

    /**
     * @see #isCoalescingEnabled
     * @see #checkCoalescing
     */
    transient private boolean coalescingEnabled = checkCoalescing();

    /**
     * 已知的 coalesceEvent 覆盖者的弱映射。
     * 值表示是否已覆盖。
     * 引导类不包括在内。
     */
    private static final Map<Class<?>, Boolean> coalesceMap =
        new java.util.WeakHashMap<Class<?>, Boolean>();

    /**
     * 指示此类是否覆盖了 coalesceEvents。
     * 假设所有从引导加载器加载的类都没有覆盖。
     * 假设引导类加载器由 null 表示。
     * 我们不检查该方法是否真正覆盖
     *   （它可能是静态的、私有的或包私有的）。
     */
     private boolean checkCoalescing() {
         if (getClass().getClassLoader()==null) {
             return false;
         }
         final Class<? extends Component> clazz = getClass();
         synchronized (coalesceMap) {
             // 检查缓存。
             Boolean value = coalesceMap.get(clazz);
             if (value != null) {
                 return value;
             }

             // 需要检查非引导类。
             Boolean enabled = java.security.AccessController.doPrivileged(
                 new java.security.PrivilegedAction<Boolean>() {
                     public Boolean run() {
                         return isCoalesceEventsOverriden(clazz);
                     }
                 }
                 );
             coalesceMap.put(clazz, enabled);
             return enabled;
         }
     }

    /**
     * coalesceEvents(AWTEvent,AWTEVent) 的参数类型。
     */
    private static final Class[] coalesceEventsParams = {
        AWTEvent.class, AWTEvent.class
    };

    /**
     * 指示一个类或其超类是否覆盖了 coalesceEvents。
     * 必须在 coalesceMap 上加锁并具有特权时调用。
     * @see checkCoalescing
     */
    private static boolean isCoalesceEventsOverriden(Class<?> clazz) {
        assert Thread.holdsLock(coalesceMap);

        // 首先检查超类 - 我们可能不需要麻烦自己。
        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null) {
            // 仅在实现中发生
            //   不使用 null 表示引导类加载器。
            return false;
        }
        if (superclass.getClassLoader() != null) {
            Boolean value = coalesceMap.get(superclass);
            if (value == null) {
                // 尚未完成 - 递归。
                if (isCoalesceEventsOverriden(superclass)) {
                    coalesceMap.put(superclass, true);
                    return true;
                }
            } else if (value) {
                return true;
            }
        }

        try {
            // 如果未覆盖，则抛出异常。
            clazz.getDeclaredMethod(
                "coalesceEvents", coalesceEventsParams
                );
            return true;
        } catch (NoSuchMethodException e) {
            // 在此类中不存在。
            return false;
        }
    }

    /**
     * 指示 coalesceEvents 是否可能执行某些操作。
     */
    final boolean isCoalescingEnabled() {
        return coalescingEnabled;
     }


    /**
     * 潜在地将一个正在发布的事件与一个已存在的事件合并。
     * 如果事件队列中已存在一个与要发布的事件具有相同 ID 的事件（两个事件的源都是此组件），
     * 则调用此方法。
     * 此方法返回一个合并后的事件，该事件将替换已存在的事件（新事件将被丢弃），
     * 或返回 <code>null</code> 表示不进行合并（将第二个事件添加到队列末尾）。
     * 任一事件参数都可以被修改并返回，因为另一个事件将被丢弃
     * 除非返回 <code>null</code>。
     * <p>
     * 此实现的 <code>coalesceEvents</code> 合并
     * 两种事件类型：鼠标移动（和拖动）事件，
     * 以及绘制（和更新）事件。
     * 对于鼠标移动事件，总是返回最后一个事件，导致
     * 中间移动被丢弃。对于绘制事件，新事件被合并到对等体中的复杂 <code>RepaintArea</code> 中。
     * 总是返回新的 <code>AWTEvent</code>。
     *
     * @param  existingEvent  已在 <code>EventQueue</code> 上的事件
     * @param  newEvent       要发布到 <code>EventQueue</code> 的事件
     * @return 合并后的事件，或 <code>null</code> 表示未进行合并
     */
    protected AWTEvent coalesceEvents(AWTEvent existingEvent,
                                      AWTEvent newEvent) {
        return null;
    }

    /**
     * 处理此组件上发生的事件。默认情况下，此方法调用
     * 给定事件类的适当
     * <code>process&lt;event&nbsp;type&gt;Event</code>
     * 方法。
     * <p>注意，如果事件参数为 <code>null</code>
     * 行为是未指定的，可能会导致异常。
     *
     * @param     e 事件
     * @see       #processComponentEvent
     * @see       #processFocusEvent
     * @see       #processKeyEvent
     * @see       #processMouseEvent
     * @see       #processMouseMotionEvent
     * @see       #processInputMethodEvent
     * @see       #processHierarchyEvent
     * @see       #processMouseWheelEvent
     * @since     JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof FocusEvent) {
            processFocusEvent((FocusEvent)e);

        } else if (e instanceof MouseEvent) {
            switch(e.getID()) {
              case MouseEvent.MOUSE_PRESSED:
              case MouseEvent.MOUSE_RELEASED:
              case MouseEvent.MOUSE_CLICKED:
              case MouseEvent.MOUSE_ENTERED:
              case MouseEvent.MOUSE_EXITED:
                  processMouseEvent((MouseEvent)e);
                  break;
              case MouseEvent.MOUSE_MOVED:
              case MouseEvent.MOUSE_DRAGGED:
                  processMouseMotionEvent((MouseEvent)e);
                  break;
              case MouseEvent.MOUSE_WHEEL:
                  processMouseWheelEvent((MouseWheelEvent)e);
                  break;
            }

        } else if (e instanceof KeyEvent) {
            processKeyEvent((KeyEvent)e);

        } else if (e instanceof ComponentEvent) {
            processComponentEvent((ComponentEvent)e);
        } else if (e instanceof InputMethodEvent) {
            processInputMethodEvent((InputMethodEvent)e);
        } else if (e instanceof HierarchyEvent) {
            switch (e.getID()) {
              case HierarchyEvent.HIERARCHY_CHANGED:
                  processHierarchyEvent((HierarchyEvent)e);
                  break;
              case HierarchyEvent.ANCESTOR_MOVED:
              case HierarchyEvent.ANCESTOR_RESIZED:
                  processHierarchyBoundsEvent((HierarchyEvent)e);
                  break;
            }
        }
    }

    /**
     * 通过将它们分发给任何已注册的
     * <code>ComponentListener</code> 对象来处理此组件上发生的组件事件。
     * <p>
     * 除非为此组件启用了组件事件，否则不会调用此方法。组件事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addComponentListener</code> 注册了 <code>ComponentListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了组件事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>
     * 行为是未指定的，可能会导致异常。
     *
     * @param       e 组件事件
     * @see         java.awt.event.ComponentEvent
     * @see         java.awt.event.ComponentListener
     * @see         #addComponentListener
     * @see         #enableEvents
     * @since       JDK1.1
     */
    protected void processComponentEvent(ComponentEvent e) {
        ComponentListener listener = componentListener;
        if (listener != null) {
            int id = e.getID();
            switch(id) {
              case ComponentEvent.COMPONENT_RESIZED:
                  listener.componentResized(e);
                  break;
              case ComponentEvent.COMPONENT_MOVED:
                  listener.componentMoved(e);
                  break;
              case ComponentEvent.COMPONENT_SHOWN:
                  listener.componentShown(e);
                  break;
              case ComponentEvent.COMPONENT_HIDDEN:
                  listener.componentHidden(e);
                  break;
            }
        }
    }

    /**
     * 通过将它们分发给任何已注册的
     * <code>FocusListener</code> 对象来处理此组件上发生的焦点事件。
     * <p>
     * 除非为此组件启用了焦点事件，否则不会调用此方法。焦点事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addFocusListener</code> 注册了 <code>FocusListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了焦点事件。
     * </ul>
     * <p>
     * 如果为 <code>Component</code> 启用了焦点事件，当前的 <code>KeyboardFocusManager</code> 确定
     * 是否应将焦点事件分发给已注册的 <code>FocusListener</code> 对象。如果事件应被分发，
     * <code>KeyboardFocusManager</code> 将调用 <code>Component</code> 的 <code>dispatchEvent</code>
     * 方法，这将导致调用 <code>Component</code> 的 <code>processFocusEvent</code> 方法。
     * <p>
     * 如果为 <code>Component</code> 启用了焦点事件，使用 <code>FocusEvent</code> 作为参数调用
     * <code>Component</code> 的 <code>dispatchEvent</code> 方法将导致调用
     * <code>Component</code> 的 <code>processFocusEvent</code> 方法，无论当前的
     * <code>KeyboardFocusManager</code> 如何。
     *
     * <p>注意，如果事件参数为 <code>null</code>
     * 行为是未指定的，可能会导致异常。
     *
     * @param       e 焦点事件
     * @see         java.awt.event.FocusEvent
     * @see         java.awt.event.FocusListener
     * @see         java.awt.KeyboardFocusManager
     * @see         #addFocusListener
     * @see         #enableEvents
     * @see         #dispatchEvent
     * @since       JDK1.1
     */
    protected void processFocusEvent(FocusEvent e) {
        FocusListener listener = focusListener;
        if (listener != null) {
            int id = e.getID();
            switch(id) {
              case FocusEvent.FOCUS_GAINED:
                  listener.focusGained(e);
                  break;
              case FocusEvent.FOCUS_LOST:
                  listener.focusLost(e);
                  break;
            }
        }
    }

    /**
     * 通过将它们分发给任何已注册的
     * <code>KeyListener</code> 对象来处理此组件上发生的键盘事件。
     * <p>
     * 除非为此组件启用了键盘事件，否则不会调用此方法。键盘事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addKeyListener</code> 注册了 <code>KeyListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了键盘事件。
     * </ul>
     *
     * <p>
     * 如果为 <code>Component</code> 启用了键盘事件，当前的 <code>KeyboardFocusManager</code> 确定
     * 是否应将键盘事件分发给已注册的 <code>KeyListener</code> 对象。 <code>DefaultKeyboardFocusManager</code>
     * 不会将键盘事件分发给不是焦点所有者或未显示的 <code>Component</code>。
     * <p>
     * 从 J2SE 1.4 开始，<code>KeyEvent</code>s 被重定向到焦点所有者。请参阅
     * <a href="doc-files/FocusSpec.html">焦点规范</a>
     * 以获取更多信息。
     * <p>
     * 只要组件显示、聚焦且启用，并且在其上启用了键盘事件，
     * 使用 <code>KeyEvent</code> 作为参数调用 <code>Component</code> 的 <code>dispatchEvent</code>
     * 方法将导致调用 <code>Component</code> 的 <code>processKeyEvent</code> 方法，无论当前的
     * <code>KeyboardFocusManager</code> 如何。
     * <p>如果事件参数为 <code>null</code>
     * 行为是未指定的，可能会导致异常。
     *
     * @param       e 键盘事件
     * @see         java.awt.event.KeyEvent
     * @see         java.awt.event.KeyListener
     * @see         java.awt.KeyboardFocusManager
     * @see         java.awt.DefaultKeyboardFocusManager
     * @see         #processEvent
     * @see         #dispatchEvent
     * @see         #addKeyListener
     * @see         #enableEvents
     * @see         #isShowing
     * @since       JDK1.1
     */
    protected void processKeyEvent(KeyEvent e) {
        KeyListener listener = keyListener;
        if (listener != null) {
            int id = e.getID();
            switch(id) {
              case KeyEvent.KEY_TYPED:
                  listener.keyTyped(e);
                  break;
              case KeyEvent.KEY_PRESSED:
                  listener.keyPressed(e);
                  break;
              case KeyEvent.KEY_RELEASED:
                  listener.keyReleased(e);
                  break;
            }
        }
    }

    /**
     * 通过将它们分发给任何已注册的
     * <code>MouseListener</code> 对象来处理此组件上发生的鼠标事件。
     * <p>
     * 除非为此组件启用了鼠标事件，否则不会调用此方法。鼠标事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addMouseListener</code> 注册了 <code>MouseListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了鼠标事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>
     * 行为是未指定的，可能会导致异常。
     *
     * @param       e 鼠标事件
     * @see         java.awt.event.MouseEvent
     * @see         java.awt.event.MouseListener
     * @see         #addMouseListener
     * @see         #enableEvents
     * @since       JDK1.1
     */
    protected void processMouseEvent(MouseEvent e) {
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
              case MouseEvent.MOUSE_EXITED:
                  listener.mouseExited(e);
                  break;
              case MouseEvent.MOUSE_ENTERED:
                  listener.mouseEntered(e);
                  break;
            }
        }
    }


                /**
     * 处理在此组件上发生的鼠标移动事件，通过将它们分发给任何已注册的
     * <code>MouseMotionListener</code> 对象。
     * <p>
     * 除非为该组件启用了鼠标移动事件，否则不会调用此方法。鼠标移动事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addMouseMotionListener</code> 注册了 <code>MouseMotionListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了鼠标移动事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>
     * 则行为未指定，可能会导致异常。
     *
     * @param       e 鼠标移动事件
     * @see         java.awt.event.MouseEvent
     * @see         java.awt.event.MouseMotionListener
     * @see         #addMouseMotionListener
     * @see         #enableEvents
     * @since       JDK1.1
     */
    protected void processMouseMotionEvent(MouseEvent e) {
        MouseMotionListener listener = mouseMotionListener;
        if (listener != null) {
            int id = e.getID();
            switch(id) {
              case MouseEvent.MOUSE_MOVED:
                  listener.mouseMoved(e);
                  break;
              case MouseEvent.MOUSE_DRAGGED:
                  listener.mouseDragged(e);
                  break;
            }
        }
    }

    /**
     * 处理在此组件上发生的鼠标滚轮事件，通过将它们分发给任何已注册的
     * <code>MouseWheelListener</code> 对象。
     * <p>
     * 除非为该组件启用了鼠标滚轮事件，否则不会调用此方法。鼠标滚轮事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addMouseWheelListener</code> 注册了 <code>MouseWheelListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了鼠标滚轮事件。
     * </ul>
     * <p>
     * 有关鼠标滚轮事件如何分发的信息，请参见
     * {@link MouseWheelEvent} 的类描述。
     * <p>
     * 注意，如果事件参数为 <code>null</code>
     * 则行为未指定，可能会导致异常。
     *
     * @param       e 鼠标滚轮事件
     * @see         java.awt.event.MouseWheelEvent
     * @see         java.awt.event.MouseWheelListener
     * @see         #addMouseWheelListener
     * @see         #enableEvents
     * @since       1.4
     */
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        MouseWheelListener listener = mouseWheelListener;
        if (listener != null) {
            int id = e.getID();
            switch(id) {
              case MouseEvent.MOUSE_WHEEL:
                  listener.mouseWheelMoved(e);
                  break;
            }
        }
    }

    boolean postsOldMouseEvents() {
        return false;
    }

    /**
     * 处理在此组件上发生的输入方法事件，通过将它们分发给任何已注册的
     * <code>InputMethodListener</code> 对象。
     * <p>
     * 除非为该组件启用了输入方法事件，否则不会调用此方法。输入方法事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addInputMethodListener</code> 注册了 <code>InputMethodListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了输入方法事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>
     * 则行为未指定，可能会导致异常。
     *
     * @param       e 输入方法事件
     * @see         java.awt.event.InputMethodEvent
     * @see         java.awt.event.InputMethodListener
     * @see         #addInputMethodListener
     * @see         #enableEvents
     * @since       1.2
     */
    protected void processInputMethodEvent(InputMethodEvent e) {
        InputMethodListener listener = inputMethodListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
              case InputMethodEvent.INPUT_METHOD_TEXT_CHANGED:
                  listener.inputMethodTextChanged(e);
                  break;
              case InputMethodEvent.CARET_POSITION_CHANGED:
                  listener.caretPositionChanged(e);
                  break;
            }
        }
    }

    /**
     * 处理在此组件上发生的层次结构事件，通过将它们分发给任何已注册的
     * <code>HierarchyListener</code> 对象。
     * <p>
     * 除非为该组件启用了层次结构事件，否则不会调用此方法。层次结构事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addHierarchyListener</code> 注册了 <code>HierarchyListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了层次结构事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>
     * 则行为未指定，可能会导致异常。
     *
     * @param       e 层次结构事件
     * @see         java.awt.event.HierarchyEvent
     * @see         java.awt.event.HierarchyListener
     * @see         #addHierarchyListener
     * @see         #enableEvents
     * @since       1.3
     */
    protected void processHierarchyEvent(HierarchyEvent e) {
        HierarchyListener listener = hierarchyListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
              case HierarchyEvent.HIERARCHY_CHANGED:
                  listener.hierarchyChanged(e);
                  break;
            }
        }
    }

    /**
     * 处理在此组件上发生的层次结构边界事件，通过将它们分发给任何已注册的
     * <code>HierarchyBoundsListener</code> 对象。
     * <p>
     * 除非为该组件启用了层次结构边界事件，否则不会调用此方法。层次结构边界事件在以下情况下启用：
     * <ul>
     * <li>通过 <code>addHierarchyBoundsListener</code> 注册了 <code>HierarchyBoundsListener</code> 对象。
     * <li>通过 <code>enableEvents</code> 启用了层次结构边界事件。
     * </ul>
     * <p>注意，如果事件参数为 <code>null</code>
     * 则行为未指定，可能会导致异常。
     *
     * @param       e 层次结构事件
     * @see         java.awt.event.HierarchyEvent
     * @see         java.awt.event.HierarchyBoundsListener
     * @see         #addHierarchyBoundsListener
     * @see         #enableEvents
     * @since       1.3
     */
    protected void processHierarchyBoundsEvent(HierarchyEvent e) {
        HierarchyBoundsListener listener = hierarchyBoundsListener;
        if (listener != null) {
            int id = e.getID();
            switch (id) {
              case HierarchyEvent.ANCESTOR_MOVED:
                  listener.ancestorMoved(e);
                  break;
              case HierarchyEvent.ANCESTOR_RESIZED:
                  listener.ancestorResized(e);
                  break;
            }
        }
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起
     * 被 processEvent(AWTEvent) 取代。
     */
    @Deprecated
    public boolean handleEvent(Event evt) {
        switch (evt.id) {
          case Event.MOUSE_ENTER:
              return mouseEnter(evt, evt.x, evt.y);

          case Event.MOUSE_EXIT:
              return mouseExit(evt, evt.x, evt.y);

          case Event.MOUSE_MOVE:
              return mouseMove(evt, evt.x, evt.y);

          case Event.MOUSE_DOWN:
              return mouseDown(evt, evt.x, evt.y);

          case Event.MOUSE_DRAG:
              return mouseDrag(evt, evt.x, evt.y);

          case Event.MOUSE_UP:
              return mouseUp(evt, evt.x, evt.y);

          case Event.KEY_PRESS:
          case Event.KEY_ACTION:
              return keyDown(evt, evt.key);

          case Event.KEY_RELEASE:
          case Event.KEY_ACTION_RELEASE:
              return keyUp(evt, evt.key);

          case Event.ACTION_EVENT:
              return action(evt, evt.arg);
          case Event.GOT_FOCUS:
              return gotFocus(evt, evt.arg);
          case Event.LOST_FOCUS:
              return lostFocus(evt, evt.arg);
        }
        return false;
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起
     * 被 processMouseEvent(MouseEvent) 取代。
     */
    @Deprecated
    public boolean mouseDown(Event evt, int x, int y) {
        return false;
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起
     * 被 processMouseMotionEvent(MouseEvent) 取代。
     */
    @Deprecated
    public boolean mouseDrag(Event evt, int x, int y) {
        return false;
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起
     * 被 processMouseEvent(MouseEvent) 取代。
     */
    @Deprecated
    public boolean mouseUp(Event evt, int x, int y) {
        return false;
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起
     * 被 processMouseMotionEvent(MouseEvent) 取代。
     */
    @Deprecated
    public boolean mouseMove(Event evt, int x, int y) {
        return false;
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起
     * 被 processMouseEvent(MouseEvent) 取代。
     */
    @Deprecated
    public boolean mouseEnter(Event evt, int x, int y) {
        return false;
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起
     * 被 processMouseEvent(MouseEvent) 取代。
     */
    @Deprecated
    public boolean mouseExit(Event evt, int x, int y) {
        return false;
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起
     * 被 processKeyEvent(KeyEvent) 取代。
     */
    @Deprecated
    public boolean keyDown(Event evt, int key) {
        return false;
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起
     * 被 processKeyEvent(KeyEvent) 取代。
     */
    @Deprecated
    public boolean keyUp(Event evt, int key) {
        return false;
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起
     * 应将此组件注册为组件的 ActionListener，该组件触发动作事件。
     */
    @Deprecated
    public boolean action(Event evt, Object what) {
        return false;
    }

    /**
     * 通过将此 <code>Component</code> 连接到本机屏幕资源，使其可显示。
     * 此方法由工具包内部调用，不应由程序直接调用。
     * <p>
     * 此方法更改了与布局相关的信息，因此会使组件层次结构失效。
     *
     * @see       #isDisplayable
     * @see       #removeNotify
     * @see #invalidate
     * @since JDK1.0
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            ComponentPeer peer = this.peer;
            if (peer == null || peer instanceof LightweightPeer){
                if (peer == null) {
                    // 更新 Component 的 peer 变量和用于线程安全的局部变量。
                    this.peer = peer = getToolkit().createComponent(this);
                }

                // 这是一个轻量级组件，意味着它本身无法获取窗口相关事件。如果已启用任何此类事件，
                // 则最近的本机容器必须启用这些事件。
                if (parent != null) {
                    long mask = 0;
                    if ((mouseListener != null) || ((eventMask & AWTEvent.MOUSE_EVENT_MASK) != 0)) {
                        mask |= AWTEvent.MOUSE_EVENT_MASK;
                    }
                    if ((mouseMotionListener != null) ||
                        ((eventMask & AWTEvent.MOUSE_MOTION_EVENT_MASK) != 0)) {
                        mask |= AWTEvent.MOUSE_MOTION_EVENT_MASK;
                    }
                    if ((mouseWheelListener != null ) ||
                        ((eventMask & AWTEvent.MOUSE_WHEEL_EVENT_MASK) != 0)) {
                        mask |= AWTEvent.MOUSE_WHEEL_EVENT_MASK;
                    }
                    if (focusListener != null || (eventMask & AWTEvent.FOCUS_EVENT_MASK) != 0) {
                        mask |= AWTEvent.FOCUS_EVENT_MASK;
                    }
                    if (keyListener != null || (eventMask & AWTEvent.KEY_EVENT_MASK) != 0) {
                        mask |= AWTEvent.KEY_EVENT_MASK;
                    }
                    if (mask != 0) {
                        parent.proxyEnableEvents(mask);
                    }
                }
            } else {
                // 它是本机的。如果父组件是轻量级的，则需要一些帮助。
                Container parent = getContainer();
                if (parent != null && parent.isLightweight()) {
                    relocateComponent();
                    if (!parent.isRecursivelyVisibleUpToHeavyweightContainer())
                    {
                        peer.setVisible(false);
                    }
                }
            }
            invalidate();

            int npopups = (popups != null? popups.size() : 0);
            for (int i = 0 ; i < npopups ; i++) {
                PopupMenu popup = popups.elementAt(i);
                popup.addNotify();
            }

            if (dropTarget != null) dropTarget.addNotify(peer);

            peerFont = getFont();

            if (getContainer() != null && !isAddNotifyComplete) {
                getContainer().increaseComponentCount(this);
            }


            // 更新堆叠顺序
            updateZOrder();

            if (!isAddNotifyComplete) {
                mixOnShowing();
            }

            isAddNotifyComplete = true;

            if (hierarchyListener != null ||
                (eventMask & AWTEvent.HIERARCHY_EVENT_MASK) != 0 ||
                Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_EVENT_MASK)) {
                HierarchyEvent e =
                    new HierarchyEvent(this, HierarchyEvent.HIERARCHY_CHANGED,
                                       this, parent,
                                       HierarchyEvent.DISPLAYABILITY_CHANGED |
                                       ((isRecursivelyVisible())
                                        ? HierarchyEvent.SHOWING_CHANGED
                                        : 0));
                dispatchEvent(e);
            }
        }
    }

    /**
     * 通过销毁其本机屏幕资源，使此 <code>Component</code> 不可显示。
     * <p>
     * 此方法由工具包内部调用，不应由程序直接调用。重写此方法的代码应以
     * <code>super.removeNotify</code> 作为重写方法的第一行。
     *
     * @see       #isDisplayable
     * @see       #addNotify
     * @since JDK1.0
     */
    public void removeNotify() {
        KeyboardFocusManager.clearMostRecentFocusOwner(this);
        if (KeyboardFocusManager.getCurrentKeyboardFocusManager().
            getPermanentFocusOwner() == this)
        {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().
                setGlobalPermanentFocusOwner(null);
        }


                    synchronized (getTreeLock()) {
            if (isFocusOwner() && KeyboardFocusManager.isAutoFocusTransferEnabledFor(this)) {
                transferFocus(true);
            }

            if (getContainer() != null && isAddNotifyComplete) {
                getContainer().decreaseComponentCount(this);
            }

            int npopups = (popups != null? popups.size() : 0);
            for (int i = 0 ; i < npopups ; i++) {
                PopupMenu popup = popups.elementAt(i);
                popup.removeNotify();
            }
            // 如果此组件有任何输入上下文，通知
            // 该组件正在被移除。（这必须在隐藏对等组件之前完成。）
            if ((eventMask & AWTEvent.INPUT_METHODS_ENABLED_MASK) != 0) {
                InputContext inputContext = getInputContext();
                if (inputContext != null) {
                    inputContext.removeNotify(this);
                }
            }

            ComponentPeer p = peer;
            if (p != null) {
                boolean isLightweight = isLightweight();

                if (bufferStrategy instanceof FlipBufferStrategy) {
                    ((FlipBufferStrategy)bufferStrategy).destroyBuffers();
                }

                if (dropTarget != null) dropTarget.removeNotify(peer);

                // 首先隐藏对等组件以停止系统事件，例如光标移动。
                if (visible) {
                    p.setVisible(false);
                }

                peer = null; // 停止对等组件更新。
                peerFont = null;

                Toolkit.getEventQueue().removeSourceEvents(this, false);
                KeyboardFocusManager.getCurrentKeyboardFocusManager().
                    discardKeyEvents(this);

                p.dispose();

                mixOnHiding(isLightweight);

                isAddNotifyComplete = false;
                // 将 compoundShape 置为 null 表示该组件具有正常形状
                // （或根本没有形状）。
                this.compoundShape = null;
            }

            if (hierarchyListener != null ||
                (eventMask & AWTEvent.HIERARCHY_EVENT_MASK) != 0 ||
                Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_EVENT_MASK)) {
                HierarchyEvent e =
                    new HierarchyEvent(this, HierarchyEvent.HIERARCHY_CHANGED,
                                       this, parent,
                                       HierarchyEvent.DISPLAYABILITY_CHANGED |
                                       ((isRecursivelyVisible())
                                        ? HierarchyEvent.SHOWING_CHANGED
                                        : 0));
                dispatchEvent(e);
            }
        }
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 processFocusEvent(FocusEvent)。
     */
    @Deprecated
    public boolean gotFocus(Event evt, Object what) {
        return false;
    }

    /**
     * @deprecated 自 JDK 1.1 版本起，
     * 替换为 processFocusEvent(FocusEvent)。
     */
    @Deprecated
    public boolean lostFocus(Event evt, Object what) {
        return false;
    }

    /**
     * 返回此 <code>Component</code> 是否可以成为焦点所有者。
     *
     * @return 如果此 <code>Component</code> 是可聚焦的，则返回 <code>true</code>；否则返回 <code>false</code>
     * @see #setFocusable
     * @since JDK1.1
     * @deprecated 自 1.4 起，替换为 <code>isFocusable()</code>。
     */
    @Deprecated
    public boolean isFocusTraversable() {
        if (isFocusTraversableOverridden == FOCUS_TRAVERSABLE_UNKNOWN) {
            isFocusTraversableOverridden = FOCUS_TRAVERSABLE_DEFAULT;
        }
        return focusable;
    }

    /**
     * 返回此组件是否可以聚焦。
     *
     * @return 如果此组件是可聚焦的，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @see #setFocusable
     * @since 1.4
     */
    public boolean isFocusable() {
        return isFocusTraversable();
    }

    /**
     * 将此组件的可聚焦状态设置为指定值。此值覆盖组件的默认可聚焦性。
     *
     * @param focusable 表示此组件是否可聚焦
     * @see #isFocusable
     * @since 1.4
     * @beaninfo
     *       bound: true
     */
    public void setFocusable(boolean focusable) {
        boolean oldFocusable;
        synchronized (this) {
            oldFocusable = this.focusable;
            this.focusable = focusable;
        }
        isFocusTraversableOverridden = FOCUS_TRAVERSABLE_SET;

        firePropertyChange("focusable", oldFocusable, focusable);
        if (oldFocusable && !focusable) {
            if (isFocusOwner() && KeyboardFocusManager.isAutoFocusTransferEnabled()) {
                transferFocus(true);
            }
            KeyboardFocusManager.clearMostRecentFocusOwner(this);
        }
    }

    final boolean isFocusTraversableOverridden() {
        return (isFocusTraversableOverridden != FOCUS_TRAVERSABLE_DEFAULT);
    }

    /**
     * 为给定的焦点遍历操作设置此组件的焦点遍历键。
     * <p>
     * 组件的焦点遍历键的默认值取决于实现。Sun 建议所有特定本地平台的实现使用相同的默认值。Windows 和 Unix 的推荐值如下。这些推荐值在 Sun AWT 实现中使用。
     *
     * <table border=1 summary="Recommended default values for a Component's focus traversal keys">
     * <tr>
     *    <th>标识符</th>
     *    <th>含义</th>
     *    <th>默认值</th>
     * </tr>
     * <tr>
     *    <td>KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS</td>
     *    <td>正常的前向键盘遍历</td>
     *    <td>TAB on KEY_PRESSED, CTRL-TAB on KEY_PRESSED</td>
     * </tr>
     * <tr>
     *    <td>KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS</td>
     *    <td>正常的反向键盘遍历</td>
     *    <td>SHIFT-TAB on KEY_PRESSED, CTRL-SHIFT-TAB on KEY_PRESSED</td>
     * </tr>
     * <tr>
     *    <td>KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS</td>
     *    <td>上移一个焦点遍历周期</td>
     *    <td>无</td>
     * </tr>
     * </table>
     *
     * 要禁用遍历键，可以使用空集；建议使用 Collections.EMPTY_SET。
     * <p>
     * 使用 AWTKeyStroke API，客户端代码可以指定在两个特定的 KeyEvent 之一，KEY_PRESSED 或 KEY_RELEASED，上发生焦点遍历操作。然而，无论指定了哪个 KeyEvent，与焦点遍历键相关的所有 KeyEvent，包括关联的 KEY_TYPED 事件，都将被消耗，不会被分派给任何组件。将 KEY_TYPED 事件指定为映射到焦点遍历操作，或为同一个事件映射多个默认焦点遍历操作，都是运行时错误。
     * <p>
     * 如果为 Set 指定了 null 值，此组件将从其父组件继承该 Set。如果此组件的所有祖先都为该 Set 指定了 null，则使用当前 KeyboardFocusManager 的默认 Set。
     * <p>
     * 如果 keystrokes 中的任何 Object 不是 AWTKeyStroke，此方法可能会抛出 {@code ClassCastException}。
     *
     * @param id 以下之一：KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS，
     *        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS，或
     *        KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS
     * @param keystrokes 指定操作的 AWTKeyStroke 集合
     * @see #getFocusTraversalKeys
     * @see KeyboardFocusManager#FORWARD_TRAVERSAL_KEYS
     * @see KeyboardFocusManager#BACKWARD_TRAVERSAL_KEYS
     * @see KeyboardFocusManager#UP_CYCLE_TRAVERSAL_KEYS
     * @throws IllegalArgumentException 如果 id 不是
     *         KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS，
     *         KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS，或
     *         KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS 之一，或者 keystrokes
     *         包含 null，或者任何 keystroke 代表 KEY_TYPED 事件，或者任何 keystroke
     *         已经映射到此组件的另一个默认焦点遍历操作
     * @since 1.4
     * @beaninfo
     *       bound: true
     */
    public void setFocusTraversalKeys(int id,
                                      Set<? extends AWTKeyStroke> keystrokes)
    {
        if (id < 0 || id >= KeyboardFocusManager.TRAVERSAL_KEY_LENGTH - 1) {
            throw new IllegalArgumentException("无效的焦点遍历键标识符");
        }

        setFocusTraversalKeys_NoIDCheck(id, keystrokes);
    }

    /**
     * 返回此组件给定焦点遍历操作的焦点遍历键集。（请参阅
     * <code>setFocusTraversalKeys</code> 以获取每个键的完整描述。）
     * <p>
     * 如果未为此组件显式定义遍历键集，则返回此组件的父组件的集。如果此组件的任何祖先都未显式定义集，则返回当前 KeyboardFocusManager 的默认集。
     *
     * @param id 以下之一：KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS，
     *        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS，或
     *        KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS
     * @return 指定操作的 AWTKeyStrokes 集。该集将不可修改，且可能为空。永远不会返回 null。
     * @see #setFocusTraversalKeys
     * @see KeyboardFocusManager#FORWARD_TRAVERSAL_KEYS
     * @see KeyboardFocusManager#BACKWARD_TRAVERSAL_KEYS
     * @see KeyboardFocusManager#UP_CYCLE_TRAVERSAL_KEYS
     * @throws IllegalArgumentException 如果 id 不是
     *         KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS，
     *         KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS，或
     *         KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS 之一
     * @since 1.4
     */
    public Set<AWTKeyStroke> getFocusTraversalKeys(int id) {
        if (id < 0 || id >= KeyboardFocusManager.TRAVERSAL_KEY_LENGTH - 1) {
            throw new IllegalArgumentException("无效的焦点遍历键标识符");
        }

        return getFocusTraversalKeys_NoIDCheck(id);
    }

    // 我们定义这些方法，以便 Container 不需要重复此代码。Container 不能调用 super.<method>，因为 Container 允许
    // DOWN_CYCLE_TRAVERSAL_KEY 而 Component 不允许。Component 方法会错误地为
    // DOWN_CYCLE_TRAVERSAL_KEY 生成 IllegalArgumentException。
    final void setFocusTraversalKeys_NoIDCheck(int id, Set<? extends AWTKeyStroke> keystrokes) {
        Set<AWTKeyStroke> oldKeys;

        synchronized (this) {
            if (focusTraversalKeys == null) {
                initializeFocusTraversalKeys();
            }

            if (keystrokes != null) {
                for (AWTKeyStroke keystroke : keystrokes ) {

                    if (keystroke == null) {
                        throw new IllegalArgumentException("不能设置 null 焦点遍历键");
                    }

                    if (keystroke.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                        throw new IllegalArgumentException("焦点遍历键不能映射到 KEY_TYPED 事件");
                    }

                    for (int i = 0; i < focusTraversalKeys.length; i++) {
                        if (i == id) {
                            continue;
                        }

                        if (getFocusTraversalKeys_NoIDCheck(i).contains(keystroke))
                        {
                            throw new IllegalArgumentException("焦点遍历键对于组件必须是唯一的");
                        }
                    }
                }
            }

            oldKeys = focusTraversalKeys[id];
            focusTraversalKeys[id] = (keystrokes != null)
                ? Collections.unmodifiableSet(new HashSet<AWTKeyStroke>(keystrokes))
                : null;
        }

        firePropertyChange(focusTraversalKeyPropertyNames[id], oldKeys,
                           keystrokes);
    }
    final Set<AWTKeyStroke> getFocusTraversalKeys_NoIDCheck(int id) {
        // 可以直接返回 Set，因为它是一个不可修改的视图
        @SuppressWarnings("unchecked")
        Set<AWTKeyStroke> keystrokes = (focusTraversalKeys != null)
            ? focusTraversalKeys[id]
            : null;

        if (keystrokes != null) {
            return keystrokes;
        } else {
            Container parent = this.parent;
            if (parent != null) {
                return parent.getFocusTraversalKeys(id);
            } else {
                return KeyboardFocusManager.getCurrentKeyboardFocusManager().
                    getDefaultFocusTraversalKeys(id);
            }
        }
    }

    /**
     * 返回给定焦点遍历操作的焦点遍历键集是否已为此组件显式定义。如果此方法返回 <code>false</code>，此组件是从祖先或当前 KeyboardFocusManager 继承该集。
     *
     * @param id 以下之一：KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS，
     *        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS，或
     *        KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS
     * @return 如果给定焦点遍历操作的焦点遍历键集已为此组件显式定义，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @throws IllegalArgumentException 如果 id 不是
     *         KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS，
     *         KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS，或
     *         KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS 之一
     * @since 1.4
     */
    public boolean areFocusTraversalKeysSet(int id) {
        if (id < 0 || id >= KeyboardFocusManager.TRAVERSAL_KEY_LENGTH - 1) {
            throw new IllegalArgumentException("无效的焦点遍历键标识符");
        }

        return (focusTraversalKeys != null && focusTraversalKeys[id] != null);
    }

    /**
     * 设置此组件是否启用焦点遍历键。对于禁用了焦点遍历键的组件，会接收焦点遍历键的键事件。对于启用了焦点遍历键的组件，不会看到这些事件；相反，这些事件会自动转换为遍历操作。
     *
     * @param focusTraversalKeysEnabled 是否为此组件启用焦点遍历键
     * @see #getFocusTraversalKeysEnabled
     * @see #setFocusTraversalKeys
     * @see #getFocusTraversalKeys
     * @since 1.4
     * @beaninfo
     *       bound: true
     */
    public void setFocusTraversalKeysEnabled(boolean
                                             focusTraversalKeysEnabled) {
        boolean oldFocusTraversalKeysEnabled;
        synchronized (this) {
            oldFocusTraversalKeysEnabled = this.focusTraversalKeysEnabled;
            this.focusTraversalKeysEnabled = focusTraversalKeysEnabled;
        }
        firePropertyChange("focusTraversalKeysEnabled",
                           oldFocusTraversalKeysEnabled,
                           focusTraversalKeysEnabled);
    }


                /**
     * 返回此 Component 是否启用了焦点遍历键。
     * 对于禁用了焦点遍历键的组件，会接收到焦点遍历键的按键事件。对于启用了焦点遍历键的组件，这些事件不会被看到；相反，这些事件会被自动转换为遍历操作。
     *
     * @return 此 Component 是否启用了焦点遍历键
     * @see #setFocusTraversalKeysEnabled
     * @see #setFocusTraversalKeys
     * @see #getFocusTraversalKeys
     * @since 1.4
     */
    public boolean getFocusTraversalKeysEnabled() {
        return focusTraversalKeysEnabled;
    }

    /**
     * 请求此 Component 获取输入焦点，并且此 Component 的顶级祖先成为焦点 Window。此组件必须是可显示的、可聚焦的、可见的，并且其所有祖先（顶级 Window 除外）必须是可见的才能授予该请求。将尽一切努力满足该请求；然而，在某些情况下，可能无法做到这一点。开发人员在组件接收到 FOCUS_GAINED 事件之前，永远不要假设此组件是焦点所有者。如果由于此组件的顶级 Window 无法成为焦点 Window 而拒绝了此请求，请求将被记住，并在用户稍后聚焦 Window 时被授予。
     * <p>
     * 该方法不能用于将焦点所有者设置为没有任何组件。请改用 <code>KeyboardFocusManager.clearGlobalFocusOwner()</code>。
     * <p>
     * 由于此方法的焦点行为是平台依赖的，强烈建议开发人员在可能的情况下使用 <code>requestFocusInWindow</code>。
     *
     * <p>注意：并非所有的焦点转移都源自调用此方法。因此，组件可能会在没有调用此方法或 Component 的其他 <code>requestFocus</code> 方法的情况下接收到焦点。
     *
     * @see #requestFocusInWindow
     * @see java.awt.event.FocusEvent
     * @see #addFocusListener
     * @see #isFocusable
     * @see #isDisplayable
     * @see KeyboardFocusManager#clearGlobalFocusOwner
     * @since JDK1.0
     */
    public void requestFocus() {
        requestFocusHelper(false, true);
    }

    boolean requestFocus(CausedFocusEvent.Cause cause) {
        return requestFocusHelper(false, true, cause);
    }

    /**
     * 请求此 <code>Component</code> 获取输入焦点，并且此 <code>Component</code> 的顶级祖先成为焦点 <code>Window</code>。此组件必须是可显示的、可聚焦的、可见的，并且其所有祖先（顶级 Window 除外）必须是可见的才能授予该请求。将尽一切努力满足该请求；然而，在某些情况下，可能无法做到这一点。开发人员在组件接收到 FOCUS_GAINED 事件之前，永远不要假设此组件是焦点所有者。如果由于此组件的顶级 Window 无法成为焦点 Window 而拒绝了此请求，请求将被记住，并在用户稍后聚焦 Window 时被授予。
     * <p>
     * 该方法返回一个布尔值。如果返回 <code>false</code>，则请求 <b>肯定失败</b>。如果返回 <code>true</code>，则请求 <b>除非</b> 被否决，或者在请求被原生窗口系统授予之前发生了异常事件（如组件的对等体被销毁），否则将成功。同样，虽然返回值为 <code>true</code> 表示请求可能会成功，但开发人员在组件接收到 FOCUS_GAINED 事件之前，永远不要假设此组件是焦点所有者。
     * <p>
     * 该方法不能用于将焦点所有者设置为没有任何组件。请改用 <code>KeyboardFocusManager.clearGlobalFocusOwner</code>。
     * <p>
     * 由于此方法的焦点行为是平台依赖的，强烈建议开发人员在可能的情况下使用 <code>requestFocusInWindow</code>。
     * <p>
     * 将尽一切努力确保由此请求生成的 <code>FocusEvent</code> 具有指定的临时值。然而，由于在所有原生窗口系统上指定任意临时状态可能不可实现，因此可以保证此方法的正确行为仅适用于轻量级组件。此方法不打算用于一般用途，而是作为轻量级组件库（如 Swing）的钩子存在。
     *
     * <p>注意：并非所有的焦点转移都源自调用此方法。因此，组件可能会在没有调用此方法或 Component 的其他 <code>requestFocus</code> 方法的情况下接收到焦点。
     *
     * @param temporary 如果焦点更改是临时的（例如，当窗口失去焦点时），则为 true；有关临时焦点更改的更多信息，请参阅
     *<a href="../../java/awt/doc-files/FocusSpec.html">焦点规范</a>
     * @return 如果焦点更改请求肯定失败，则返回 <code>false</code>；如果可能成功，则返回 <code>true</code>
     * @see java.awt.event.FocusEvent
     * @see #addFocusListener
     * @see #isFocusable
     * @see #isDisplayable
     * @see KeyboardFocusManager#clearGlobalFocusOwner
     * @since 1.4
     */
    protected boolean requestFocus(boolean temporary) {
        return requestFocusHelper(temporary, true);
    }

    boolean requestFocus(boolean temporary, CausedFocusEvent.Cause cause) {
        return requestFocusHelper(temporary, true, cause);
    }
    /**
     * 请求此 Component 获取输入焦点，前提是此 Component 的顶级祖先已经是焦点 Window。此组件必须是可显示的、可聚焦的、可见的，并且其所有祖先（顶级 Window 除外）必须是可见的才能授予该请求。将尽一切努力满足该请求；然而，在某些情况下，可能无法做到这一点。开发人员在组件接收到 FOCUS_GAINED 事件之前，永远不要假设此组件是焦点所有者。
     * <p>
     * 该方法返回一个布尔值。如果返回 <code>false</code>，则请求 <b>肯定失败</b>。如果返回 <code>true</code>，则请求 <b>除非</b> 被否决，或者在请求被原生窗口系统授予之前发生了异常事件（如组件的对等体被销毁），否则将成功。同样，虽然返回值为 <code>true</code> 表示请求可能会成功，但开发人员在组件接收到 FOCUS_GAINED 事件之前，永远不要假设此组件是焦点所有者。
     * <p>
     * 该方法不能用于将焦点所有者设置为没有任何组件。请改用 <code>KeyboardFocusManager.clearGlobalFocusOwner()</code>。
     * <p>
     * 该方法的焦点行为可以在所有平台上统一实现，因此强烈建议开发人员在可能的情况下使用此方法而不是 <code>requestFocus</code>。依赖于 <code>requestFocus</code> 的代码在不同平台上可能会表现出不同的焦点行为。
     *
     * <p>注意：并非所有的焦点转移都源自调用此方法。因此，组件可能会在没有调用此方法或 Component 的其他 <code>requestFocus</code> 方法的情况下接收到焦点。
     *
     * @return 如果焦点更改请求肯定失败，则返回 <code>false</code>；如果可能成功，则返回 <code>true</code>
     * @see #requestFocus
     * @see java.awt.event.FocusEvent
     * @see #addFocusListener
     * @see #isFocusable
     * @see #isDisplayable
     * @see KeyboardFocusManager#clearGlobalFocusOwner
     * @since 1.4
     */
    public boolean requestFocusInWindow() {
        return requestFocusHelper(false, false);
    }

    boolean requestFocusInWindow(CausedFocusEvent.Cause cause) {
        return requestFocusHelper(false, false, cause);
    }

    /**
     * 请求此 <code>Component</code> 获取输入焦点，前提是此 <code>Component</code> 的顶级祖先已经是焦点 <code>Window</code>。此组件必须是可显示的、可聚焦的、可见的，并且其所有祖先（顶级 Window 除外）必须是可见的才能授予该请求。将尽一切努力满足该请求；然而，在某些情况下，可能无法做到这一点。开发人员在组件接收到 FOCUS_GAINED 事件之前，永远不要假设此组件是焦点所有者。
     * <p>
     * 该方法返回一个布尔值。如果返回 <code>false</code>，则请求 <b>肯定失败</b>。如果返回 <code>true</code>，则请求 <b>除非</b> 被否决，或者在请求被原生窗口系统授予之前发生了异常事件（如组件的对等体被销毁），否则将成功。同样，虽然返回值为 <code>true</code> 表示请求可能会成功，但开发人员在组件接收到 FOCUS_GAINED 事件之前，永远不要假设此组件是焦点所有者。
     * <p>
     * 该方法不能用于将焦点所有者设置为没有任何组件。请改用 <code>KeyboardFocusManager.clearGlobalFocusOwner</code>。
     * <p>
     * 该方法的焦点行为可以在所有平台上统一实现，因此强烈建议开发人员在可能的情况下使用此方法而不是 <code>requestFocus</code>。依赖于 <code>requestFocus</code> 的代码在不同平台上可能会表现出不同的焦点行为。
     * <p>
     * 将尽一切努力确保由此请求生成的 <code>FocusEvent</code> 具有指定的临时值。然而，由于在所有原生窗口系统上指定任意临时状态可能不可实现，因此可以保证此方法的正确行为仅适用于轻量级组件。此方法不打算用于一般用途，而是作为轻量级组件库（如 Swing）的钩子存在。
     *
     * <p>注意：并非所有的焦点转移都源自调用此方法。因此，组件可能会在没有调用此方法或 Component 的其他 <code>requestFocus</code> 方法的情况下接收到焦点。
     *
     * @param temporary 如果焦点更改是临时的（例如，当窗口失去焦点时），则为 true；有关临时焦点更改的更多信息，请参阅
     *<a href="../../java/awt/doc-files/FocusSpec.html">焦点规范</a>
     * @return 如果焦点更改请求肯定失败，则返回 <code>false</code>；如果可能成功，则返回 <code>true</code>
     * @see #requestFocus
     * @see java.awt.event.FocusEvent
     * @see #addFocusListener
     * @see #isFocusable
     * @see #isDisplayable
     * @see KeyboardFocusManager#clearGlobalFocusOwner
     * @since 1.4
     */
    protected boolean requestFocusInWindow(boolean temporary) {
        return requestFocusHelper(temporary, false);
    }

    boolean requestFocusInWindow(boolean temporary, CausedFocusEvent.Cause cause) {
        return requestFocusHelper(temporary, false, cause);
    }

    final boolean requestFocusHelper(boolean temporary,
                                     boolean focusedWindowChangeAllowed) {
        return requestFocusHelper(temporary, focusedWindowChangeAllowed, CausedFocusEvent.Cause.UNKNOWN);
    }

    final boolean requestFocusHelper(boolean temporary,
                                     boolean focusedWindowChangeAllowed,
                                     CausedFocusEvent.Cause cause)
    {
        // 1) 检查正在分发的事件是否是系统生成的鼠标事件。
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        if (currentEvent instanceof MouseEvent &&
            SunToolkit.isSystemGenerated(currentEvent))
        {
            // 2) 桑性检查：如果鼠标事件组件源属于同一个包含窗口。
            Component source = ((MouseEvent)currentEvent).getComponent();
            if (source == null || source.getContainingWindow() == getContainingWindow()) {
                focusLog.finest("requesting focus by mouse event \"in window\"");

                // 如果两个条件都满足，则焦点请求应严格限制在顶级窗口内。假设鼠标事件激活了窗口（如果它未激活），这使得具有强窗口内要求的焦点请求在顶级窗口内更改焦点成为可能。如果由于事件分发机制的异步性，当此焦点请求最终被处理时，窗口碰巧在原生上未激活，则不应重新激活顶级窗口。否则结果可能不符合用户期望。参见 6981400。
                focusedWindowChangeAllowed = false;
            }
        }
        if (!isRequestFocusAccepted(temporary, focusedWindowChangeAllowed, cause)) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("requestFocus is not accepted");
            }
            return false;
        }
        // 更新最近的映射
        KeyboardFocusManager.setMostRecentFocusOwner(this);

        Component window = this;
        while ( (window != null) && !(window instanceof Window)) {
            if (!window.isVisible()) {
                if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                    focusLog.finest("component is recurively invisible");
                }
                return false;
            }
            window = window.parent;
        }

        ComponentPeer peer = this.peer;
        Component heavyweight = (peer instanceof LightweightPeer)
            ? getNativeContainer() : this;
        if (heavyweight == null || !heavyweight.isVisible()) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("Component is not a part of visible hierarchy");
            }
            return false;
        }
        peer = heavyweight.peer;
        if (peer == null) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("Peer is null");
            }
            return false;
        }


                    // 聚焦此组件
        long time = 0;
        if (EventQueue.isDispatchThread()) {
            time = Toolkit.getEventQueue().getMostRecentKeyEventTime();
        } else {
            // 从 EDT 外部发出的聚焦请求不应与任何事件相关联，
            // 因此其时间戳应简单地设置为当前时间。
            time = System.currentTimeMillis();
        }

        boolean success = peer.requestFocus
            (this, temporary, focusedWindowChangeAllowed, time, cause);
        if (!success) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager
                (appContext).dequeueKeyEvents(time, this);
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("Peer 请求失败");
            }
        } else {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("通过 " + this);
            }
        }
        return success;
    }

    private boolean isRequestFocusAccepted(boolean temporary,
                                           boolean focusedWindowChangeAllowed,
                                           CausedFocusEvent.Cause cause)
    {
        if (!isFocusable() || !isVisible()) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("不可聚焦或不可见");
            }
            return false;
        }

        ComponentPeer peer = this.peer;
        if (peer == null) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("peer 为 null");
            }
            return false;
        }

        Window window = getContainingWindow();
        if (window == null || !window.isFocusableWindow()) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("组件没有顶级窗口");
            }
            return false;
        }

        // 我们已经通过了所有常规的聚焦请求检查，
        // 现在让我们调用 RequestFocusController 并看看它的意见。
        Component focusOwner = KeyboardFocusManager.getMostRecentFocusOwner(window);
        if (focusOwner == null) {
            // 有时最近的聚焦所有者可能为 null，但聚焦所有者不是
            // 例如，如果用户移除聚焦所有者，我们会重置最近的聚焦所有者
            focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focusOwner != null && focusOwner.getContainingWindow() != window) {
                focusOwner = null;
            }
        }

        if (focusOwner == this || focusOwner == null) {
            // 控制器应验证聚焦转移，为此它
            // 应该知道从和到的组件。而且它不应该验证
            // 当这些组件相等时的转移。
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("聚焦所有者为 null 或此组件");
            }
            return true;
        }

        if (CausedFocusEvent.Cause.ACTIVATION == cause) {
            // 如果我们在激活过程中，不应调用 RequestFocusController。
            // 我们会在获得临时聚焦丢失的组件上请求聚焦，
            // 然后在最近的聚焦所有者上请求聚焦。但最近的聚焦所有者只能
            // 通过 requestFocsuXXX() 调用更改，因此此转移已获批准。
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("原因是激活");
            }
            return true;
        }

        boolean ret = Component.requestFocusController.acceptRequestFocus(focusOwner,
                                                                          this,
                                                                          temporary,
                                                                          focusedWindowChangeAllowed,
                                                                          cause);
        if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
            focusLog.finest("RequestFocusController 返回 {0}", ret);
        }

        return ret;
    }

    private static RequestFocusController requestFocusController = new DummyRequestFocusController();

    // Swing 通过反射访问此方法以实现 InputVerifier 的功能。
    // 也许，我们应该将此方法公开（稍后 ;)
    private static class DummyRequestFocusController implements RequestFocusController {
        public boolean acceptRequestFocus(Component from, Component to,
                                          boolean temporary, boolean focusedWindowChangeAllowed,
                                          CausedFocusEvent.Cause cause)
        {
            return true;
        }
    };

    synchronized static void setRequestFocusController(RequestFocusController requestController)
    {
        if (requestController == null) {
            requestFocusController = new DummyRequestFocusController();
        } else {
            requestFocusController = requestController;
        }
    }

    /**
     * 返回此组件的聚焦遍历周期的聚焦周期根容器。
     * 每个聚焦遍历周期只有一个聚焦周期根，每个不是容器的组件只属于一个聚焦遍历周期。
     * 作为聚焦周期根的容器属于两个周期：一个以该容器本身为根，另一个以该容器最近的聚焦周期根祖先为根。
     * 对于这样的容器，此方法将返回该容器最近的聚焦周期根祖先。
     *
     * @return 此组件最近的聚焦周期根祖先
     * @see Container#isFocusCycleRoot()
     * @since 1.4
     */
    public Container getFocusCycleRootAncestor() {
        Container rootAncestor = this.parent;
        while (rootAncestor != null && !rootAncestor.isFocusCycleRoot()) {
            rootAncestor = rootAncestor.parent;
        }
        return rootAncestor;
    }

    /**
     * 返回指定的容器是否是此组件的聚焦遍历周期的聚焦周期根。
     * 每个聚焦遍历周期只有一个聚焦周期根，每个不是容器的组件只属于一个聚焦遍历周期。
     *
     * @param container 要测试的容器
     * @return 如果指定的容器是此组件的聚焦周期根，则返回 <code>true</code>；否则返回 <code>false</code>
     * @see Container#isFocusCycleRoot()
     * @since 1.4
     */
    public boolean isFocusCycleRoot(Container container) {
        Container rootAncestor = getFocusCycleRootAncestor();
        return (rootAncestor == container);
    }

    Container getTraversalRoot() {
        return getFocusCycleRootAncestor();
    }

    /**
     * 将聚焦转移到下一个组件，就像此组件是聚焦所有者一样。
     * @see       #requestFocus()
     * @since     JDK1.1
     */
    public void transferFocus() {
        nextFocus();
    }

    /**
     * @deprecated 自 JDK 版本 1.1 起，
     * 替换为 transferFocus()。
     */
    @Deprecated
    public void nextFocus() {
        transferFocus(false);
    }

    boolean transferFocus(boolean clearOnFailure) {
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("clearOnFailure = " + clearOnFailure);
        }
        Component toFocus = getNextFocusCandidate();
        boolean res = false;
        if (toFocus != null && !toFocus.isFocusOwner() && toFocus != this) {
            res = toFocus.requestFocusInWindow(CausedFocusEvent.Cause.TRAVERSAL_FORWARD);
        }
        if (clearOnFailure && !res) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                focusLog.finer("清除全局聚焦所有者");
            }
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwnerPriv();
        }
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("返回结果: " + res);
        }
        return res;
    }

    final Component getNextFocusCandidate() {
        Container rootAncestor = getTraversalRoot();
        Component comp = this;
        while (rootAncestor != null &&
               !(rootAncestor.isShowing() && rootAncestor.canBeFocusOwner()))
        {
            comp = rootAncestor;
            rootAncestor = comp.getFocusCycleRootAncestor();
        }
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("comp = " + comp + ", root = " + rootAncestor);
        }
        Component candidate = null;
        if (rootAncestor != null) {
            FocusTraversalPolicy policy = rootAncestor.getFocusTraversalPolicy();
            Component toFocus = policy.getComponentAfter(rootAncestor, comp);
            if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                focusLog.finer("之后的组件是 " + toFocus);
            }
            if (toFocus == null) {
                toFocus = policy.getDefaultComponent(rootAncestor);
                if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                    focusLog.finer("默认组件是 " + toFocus);
                }
            }
            if (toFocus == null) {
                Applet applet = EmbeddedFrame.getAppletIfAncestorOf(this);
                if (applet != null) {
                    toFocus = applet;
                }
            }
            candidate = toFocus;
        }
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("聚焦转移候选者: " + candidate);
        }
        return candidate;
    }

    /**
     * 将聚焦转移到前一个组件，就像此组件是聚焦所有者一样。
     * @see       #requestFocus()
     * @since     1.4
     */
    public void transferFocusBackward() {
        transferFocusBackward(false);
    }

    boolean transferFocusBackward(boolean clearOnFailure) {
        Container rootAncestor = getTraversalRoot();
        Component comp = this;
        while (rootAncestor != null &&
               !(rootAncestor.isShowing() && rootAncestor.canBeFocusOwner()))
        {
            comp = rootAncestor;
            rootAncestor = comp.getFocusCycleRootAncestor();
        }
        boolean res = false;
        if (rootAncestor != null) {
            FocusTraversalPolicy policy = rootAncestor.getFocusTraversalPolicy();
            Component toFocus = policy.getComponentBefore(rootAncestor, comp);
            if (toFocus == null) {
                toFocus = policy.getDefaultComponent(rootAncestor);
            }
            if (toFocus != null) {
                res = toFocus.requestFocusInWindow(CausedFocusEvent.Cause.TRAVERSAL_BACKWARD);
            }
        }
        if (clearOnFailure && !res) {
            if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                focusLog.finer("清除全局聚焦所有者");
            }
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwnerPriv();
        }
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("返回结果: " + res);
        }
        return res;
    }

    /**
     * 将聚焦向上转移一个聚焦遍历周期。通常，聚焦所有者被设置为该组件的聚焦周期根，
     * 当前的聚焦周期根被设置为新的聚焦所有者的聚焦周期根。但是，如果该组件的聚焦周期根是一个窗口，
     * 则聚焦所有者被设置为聚焦周期根的默认聚焦组件，当前的聚焦周期根保持不变。
     *
     * @see       #requestFocus()
     * @see       Container#isFocusCycleRoot()
     * @see       Container#setFocusCycleRoot(boolean)
     * @since     1.4
     */
    public void transferFocusUpCycle() {
        Container rootAncestor;
        for (rootAncestor = getFocusCycleRootAncestor();
             rootAncestor != null && !(rootAncestor.isShowing() &&
                                       rootAncestor.isFocusable() &&
                                       rootAncestor.isEnabled());
             rootAncestor = rootAncestor.getFocusCycleRootAncestor()) {
        }

        if (rootAncestor != null) {
            Container rootAncestorRootAncestor =
                rootAncestor.getFocusCycleRootAncestor();
            Container fcr = (rootAncestorRootAncestor != null) ?
                rootAncestorRootAncestor : rootAncestor;

            KeyboardFocusManager.getCurrentKeyboardFocusManager().
                setGlobalCurrentFocusCycleRootPriv(fcr);
            rootAncestor.requestFocus(CausedFocusEvent.Cause.TRAVERSAL_UP);
        } else {
            Window window = getContainingWindow();

            if (window != null) {
                Component toFocus = window.getFocusTraversalPolicy().
                    getDefaultComponent(window);
                if (toFocus != null) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().
                        setGlobalCurrentFocusCycleRootPriv(window);
                    toFocus.requestFocus(CausedFocusEvent.Cause.TRAVERSAL_UP);
                }
            }
        }
    }

    /**
     * 如果此组件是聚焦所有者，则返回 <code>true</code>。此方法已过时，已被
     * <code>isFocusOwner()</code> 替换。
     *
     * @return 如果此组件是聚焦所有者，则返回 <code>true</code>；否则返回 <code>false</code>
     * @since 1.2
     */
    public boolean hasFocus() {
        return (KeyboardFocusManager.getCurrentKeyboardFocusManager().
                getFocusOwner() == this);
    }

    /**
     * 如果此组件是聚焦所有者，则返回 <code>true</code>。
     *
     * @return 如果此组件是聚焦所有者，则返回 <code>true</code>；否则返回 <code>false</code>
     * @since 1.4
     */
    public boolean isFocusOwner() {
        return hasFocus();
    }

    /*
     * 用于在处置聚焦所有者的过程中禁止自动聚焦转移
     * 以处置其父容器。
     */
    private boolean autoFocusTransferOnDisposal = true;

    void setAutoFocusTransferOnDisposal(boolean value) {
        autoFocusTransferOnDisposal = value;
    }

    boolean isAutoFocusTransferOnDisposal() {
        return autoFocusTransferOnDisposal;
    }

    /**
     * 将指定的弹出菜单添加到组件。
     * @param     popup 要添加到组件的弹出菜单。
     * @see       #remove(MenuComponent)
     * @exception NullPointerException 如果 {@code popup} 为 {@code null}
     * @since     JDK1.1
     */
    public void add(PopupMenu popup) {
        synchronized (getTreeLock()) {
            if (popup.parent != null) {
                popup.parent.remove(popup);
            }
            if (popups == null) {
                popups = new Vector<PopupMenu>();
            }
            popups.addElement(popup);
            popup.parent = this;


                        if (peer != null) {
                if (popup.peer == null) {
                    popup.addNotify();
                }
            }
        }
    }

    /**
     * 从组件中移除指定的弹出菜单。
     * @param     popup 要移除的弹出菜单
     * @see       #add(PopupMenu)
     * @since     JDK1.1
     */
    @SuppressWarnings("unchecked")
    public void remove(MenuComponent popup) {
        synchronized (getTreeLock()) {
            if (popups == null) {
                return;
            }
            int index = popups.indexOf(popup);
            if (index >= 0) {
                PopupMenu pmenu = (PopupMenu)popup;
                if (pmenu.peer != null) {
                    pmenu.removeNotify();
                }
                pmenu.parent = null;
                popups.removeElementAt(index);
                if (popups.size() == 0) {
                    popups = null;
                }
            }
        }
    }

    /**
     * 返回表示此组件状态的字符串。此方法仅用于调试目的，返回的字符串的内容和格式可能在不同实现之间有所不同。返回的字符串可以为空，但不能为 <code>null</code>。
     *
     * @return  表示此组件状态的字符串
     * @since     JDK1.0
     */
    protected String paramString() {
        final String thisName = Objects.toString(getName(), "");
        final String invalid = isValid() ? "" : ",invalid";
        final String hidden = visible ? "" : ",hidden";
        final String disabled = enabled ? "" : ",disabled";
        return thisName + ',' + x + ',' + y + ',' + width + 'x' + height
                + invalid + hidden + disabled;
    }

    /**
     * 返回此组件及其值的字符串表示形式。
     * @return    此组件的字符串表示形式
     * @since     JDK1.0
     */
    public String toString() {
        return getClass().getName() + '[' + paramString() + ']';
    }

    /**
     * 将此组件的列表打印到标准系统输出流 <code>System.out</code>。
     * @see       java.lang.System#out
     * @since     JDK1.0
     */
    public void list() {
        list(System.out, 0);
    }

    /**
     * 将此组件的列表打印到指定的输出流。
     * @param    out   打印流
     * @throws   NullPointerException 如果 {@code out} 为 {@code null}
     * @since    JDK1.0
     */
    public void list(PrintStream out) {
        list(out, 0);
    }

    /**
     * 从指定的缩进开始，将列表打印到指定的打印流。
     * @param     out      打印流
     * @param     indent   缩进的空格数
     * @see       java.io.PrintStream#println(java.lang.Object)
     * @throws    NullPointerException 如果 {@code out} 为 {@code null}
     * @since     JDK1.0
     */
    public void list(PrintStream out, int indent) {
        for (int i = 0 ; i < indent ; i++) {
            out.print(" ");
        }
        out.println(this);
    }

    /**
     * 将列表打印到指定的打印写入器。
     * @param  out  要打印到的打印写入器
     * @throws NullPointerException 如果 {@code out} 为 {@code null}
     * @since JDK1.1
     */
    public void list(PrintWriter out) {
        list(out, 0);
    }

    /**
     * 从指定的缩进开始，将列表打印到指定的打印写入器。
     * @param out 要打印到的打印写入器
     * @param indent 缩进的空格数
     * @throws NullPointerException 如果 {@code out} 为 {@code null}
     * @see       java.io.PrintStream#println(java.lang.Object)
     * @since JDK1.1
     */
    public void list(PrintWriter out, int indent) {
        for (int i = 0 ; i < indent ; i++) {
            out.print(" ");
        }
        out.println(this);
    }

    /*
     * 从组件树中获取包含此组件的原生容器。
     */
    final Container getNativeContainer() {
        Container p = getContainer();
        while (p != null && p.peer instanceof LightweightPeer) {
            p = p.getContainer();
        }
        return p;
    }

    /**
     * 将 PropertyChangeListener 添加到监听器列表中。监听器将为此类的所有绑定属性注册，包括以下内容：
     * <ul>
     *    <li>此组件的字体 ("font")</li>
     *    <li>此组件的背景色 ("background")</li>
     *    <li>此组件的前景色 ("foreground")</li>
     *    <li>此组件的可聚焦性 ("focusable")</li>
     *    <li>此组件的焦点遍历键启用状态 ("focusTraversalKeysEnabled")</li>
     *    <li>此组件的 FORWARD_TRAVERSAL_KEYS 集 ("forwardFocusTraversalKeys")</li>
     *    <li>此组件的 BACKWARD_TRAVERSAL_KEYS 集 ("backwardFocusTraversalKeys")</li>
     *    <li>此组件的 UP_CYCLE_TRAVERSAL_KEYS 集 ("upCycleFocusTraversalKeys")</li>
     *    <li>此组件的首选大小 ("preferredSize")</li>
     *    <li>此组件的最小大小 ("minimumSize")</li>
     *    <li>此组件的最大大小 ("maximumSize")</li>
     *    <li>此组件的名称 ("name")</li>
     * </ul>
     * 注意，如果此 <code>Component</code> 继承了绑定属性，则不会因继承属性的更改而触发事件。
     * <p>
     * 如果 <code>listener</code> 为 <code>null</code>，则不会抛出异常，也不会执行任何操作。
     *
     * @param    listener  要添加的属性更改监听器
     *
     * @see #removePropertyChangeListener
     * @see #getPropertyChangeListeners
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(
                                                       PropertyChangeListener listener) {
        synchronized (getObjectLock()) {
            if (listener == null) {
                return;
            }
            if (changeSupport == null) {
                changeSupport = new PropertyChangeSupport(this);
            }
            changeSupport.addPropertyChangeListener(listener);
        }
    }

    /**
     * 从监听器列表中移除 PropertyChangeListener。此方法应用于移除为此类所有绑定属性注册的 PropertyChangeListeners。
     * <p>
     * 如果监听器为 null，则不会抛出异常，也不会执行任何操作。
     *
     * @param listener 要移除的 PropertyChangeListener
     *
     * @see #addPropertyChangeListener
     * @see #getPropertyChangeListeners
     * @see #removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(
                                                          PropertyChangeListener listener) {
        synchronized (getObjectLock()) {
            if (listener == null || changeSupport == null) {
                return;
            }
            changeSupport.removePropertyChangeListener(listener);
        }
    }

    /**
     * 返回在此组件上注册的所有属性更改监听器的数组。
     *
     * @return 此组件的所有 <code>PropertyChangeListener</code>，如果当前没有注册任何属性更改监听器，则返回空数组
     *
     * @see      #addPropertyChangeListener
     * @see      #removePropertyChangeListener
     * @see      #getPropertyChangeListeners(java.lang.String)
     * @see      java.beans.PropertyChangeSupport#getPropertyChangeListeners
     * @since    1.4
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        synchronized (getObjectLock()) {
            if (changeSupport == null) {
                return new PropertyChangeListener[0];
            }
            return changeSupport.getPropertyChangeListeners();
        }
    }

    /**
     * 将 PropertyChangeListener 添加到特定属性的监听器列表中。指定的属性可以是用户定义的，也可以是以下之一：
     * <ul>
     *    <li>此组件的字体 ("font")</li>
     *    <li>此组件的背景色 ("background")</li>
     *    <li>此组件的前景色 ("foreground")</li>
     *    <li>此组件的可聚焦性 ("focusable")</li>
     *    <li>此组件的焦点遍历键启用状态 ("focusTraversalKeysEnabled")</li>
     *    <li>此组件的 FORWARD_TRAVERSAL_KEYS 集 ("forwardFocusTraversalKeys")</li>
     *    <li>此组件的 BACKWARD_TRAVERSAL_KEYS 集 ("backwardFocusTraversalKeys")</li>
     *    <li>此组件的 UP_CYCLE_TRAVERSAL_KEYS 集 ("upCycleFocusTraversalKeys")</li>
     * </ul>
     * 注意，如果此 <code>Component</code> 继承了绑定属性，则不会因继承属性的更改而触发事件。
     * <p>
     * 如果 <code>propertyName</code> 或 <code>listener</code> 为 <code>null</code>，则不会抛出异常，也不会执行任何操作。
     *
     * @param propertyName 以上列出的属性名称之一
     * @param listener 要添加的属性更改监听器
     *
     * @see #removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     * @see #getPropertyChangeListeners(java.lang.String)
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(
                                                       String propertyName,
                                                       PropertyChangeListener listener) {
        synchronized (getObjectLock()) {
            if (listener == null) {
                return;
            }
            if (changeSupport == null) {
                changeSupport = new PropertyChangeSupport(this);
            }
            changeSupport.addPropertyChangeListener(propertyName, listener);
        }
    }

    /**
     * 从特定属性的监听器列表中移除 PropertyChangeListener。此方法应用于移除为此类特定绑定属性注册的 PropertyChangeListeners。
     * <p>
     * 如果 <code>propertyName</code> 或 <code>listener</code> 为 <code>null</code>，则不会抛出异常，也不会执行任何操作。
     *
     * @param propertyName 有效的属性名称
     * @param listener 要移除的 PropertyChangeListener
     *
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     * @see #getPropertyChangeListeners(java.lang.String)
     * @see #removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(
                                                          String propertyName,
                                                          PropertyChangeListener listener) {
        synchronized (getObjectLock()) {
            if (listener == null || changeSupport == null) {
                return;
            }
            changeSupport.removePropertyChangeListener(propertyName, listener);
        }
    }

    /**
     * 返回与命名属性关联的所有监听器的数组。
     *
     * @return 与命名属性关联的所有 <code>PropertyChangeListener</code>；如果没有添加此类监听器或 <code>propertyName</code> 为 <code>null</code>，则返回空数组
     *
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     * @see #removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     * @see #getPropertyChangeListeners
     * @since 1.4
     */
    public PropertyChangeListener[] getPropertyChangeListeners(
                                                                            String propertyName) {
        synchronized (getObjectLock()) {
            if (changeSupport == null) {
                return new PropertyChangeListener[0];
            }
            return changeSupport.getPropertyChangeListeners(propertyName);
        }
    }

    /**
     * 用于报告对象属性的绑定属性更改。当绑定属性更改时可以调用此方法，它将向任何注册的 PropertyChangeListeners 发送适当的 PropertyChangeEvent。
     *
     * @param propertyName 更改的属性的名称
     * @param oldValue 属性的旧值
     * @param newValue 属性的新值
     */
    protected void firePropertyChange(String propertyName,
                                      Object oldValue, Object newValue) {
        PropertyChangeSupport changeSupport;
        synchronized (getObjectLock()) {
            changeSupport = this.changeSupport;
        }
        if (changeSupport == null ||
            (oldValue != null && newValue != null && oldValue.equals(newValue))) {
            return;
        }
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * 用于报告布尔属性的绑定属性更改。当绑定属性更改时可以调用此方法，它将向任何注册的 PropertyChangeListeners 发送适当的 PropertyChangeEvent。
     *
     * @param propertyName 更改的属性的名称
     * @param oldValue 属性的旧值
     * @param newValue 属性的新值
     * @since 1.4
     */
    protected void firePropertyChange(String propertyName,
                                      boolean oldValue, boolean newValue) {
        PropertyChangeSupport changeSupport = this.changeSupport;
        if (changeSupport == null || oldValue == newValue) {
            return;
        }
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * 用于报告整数属性的绑定属性更改。当绑定属性更改时可以调用此方法，它将向任何注册的 PropertyChangeListeners 发送适当的 PropertyChangeEvent。
     *
     * @param propertyName 更改的属性的名称
     * @param oldValue 属性的旧值
     * @param newValue 属性的新值
     * @since 1.4
     */
    protected void firePropertyChange(String propertyName,
                                      int oldValue, int newValue) {
        PropertyChangeSupport changeSupport = this.changeSupport;
        if (changeSupport == null || oldValue == newValue) {
            return;
        }
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * 报告绑定属性的更改。
     *
     * @param propertyName 更改的属性的程序名称
     * @param oldValue 属性的旧值（作为字节）
     * @param newValue 属性的新值（作为字节）
     * @see #firePropertyChange(java.lang.String, java.lang.Object,
     *          java.lang.Object)
     * @since 1.5
     */
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
        if (changeSupport == null || oldValue == newValue) {
            return;
        }
        firePropertyChange(propertyName, Byte.valueOf(oldValue), Byte.valueOf(newValue));
    }


                /**
     * 报告绑定属性的更改。
     *
     * @param propertyName 被更改的属性的程序名称
     *          被更改的属性的旧值（作为 char）
     * @param newValue 被更改的属性的新值（作为 char）
     * @see #firePropertyChange(java.lang.String, java.lang.Object,
     *          java.lang.Object)
     * @since 1.5
     */
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
        if (changeSupport == null || oldValue == newValue) {
            return;
        }
        firePropertyChange(propertyName, new Character(oldValue), new Character(newValue));
    }

    /**
     * 报告绑定属性的更改。
     *
     * @param propertyName 被更改的属性的程序名称
     *          被更改的属性的旧值（作为 short）
     * @param newValue 被更改的属性的新值（作为 short）
     * @see #firePropertyChange(java.lang.String, java.lang.Object,
     *          java.lang.Object)
     * @since 1.5
     */
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {
        if (changeSupport == null || oldValue == newValue) {
            return;
        }
        firePropertyChange(propertyName, Short.valueOf(oldValue), Short.valueOf(newValue));
    }


    /**
     * 报告绑定属性的更改。
     *
     * @param propertyName 被更改的属性的程序名称
     *          被更改的属性的旧值（作为 long）
     * @param newValue 被更改的属性的新值（作为 long）
     * @see #firePropertyChange(java.lang.String, java.lang.Object,
     *          java.lang.Object)
     * @since 1.5
     */
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {
        if (changeSupport == null || oldValue == newValue) {
            return;
        }
        firePropertyChange(propertyName, Long.valueOf(oldValue), Long.valueOf(newValue));
    }

    /**
     * 报告绑定属性的更改。
     *
     * @param propertyName 被更改的属性的程序名称
     *          被更改的属性的旧值（作为 float）
     * @param newValue 被更改的属性的新值（作为 float）
     * @see #firePropertyChange(java.lang.String, java.lang.Object,
     *          java.lang.Object)
     * @since 1.5
     */
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {
        if (changeSupport == null || oldValue == newValue) {
            return;
        }
        firePropertyChange(propertyName, Float.valueOf(oldValue), Float.valueOf(newValue));
    }

    /**
     * 报告绑定属性的更改。
     *
     * @param propertyName 被更改的属性的程序名称
     *          被更改的属性的旧值（作为 double）
     * @param newValue 被更改的属性的新值（作为 double）
     * @see #firePropertyChange(java.lang.String, java.lang.Object,
     *          java.lang.Object)
     * @since 1.5
     */
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {
        if (changeSupport == null || oldValue == newValue) {
            return;
        }
        firePropertyChange(propertyName, Double.valueOf(oldValue), Double.valueOf(newValue));
    }


    // 序列化支持。

    /**
     * 组件序列化数据版本。
     *
     * @serial
     */
    private int componentSerializedDataVersion = 4;

    /**
     * 用于 Swing 序列化的 hack。它将调用
     * Swing 包私有方法 <code>compWriteObjectNotify</code>。
     */
    private void doSwingSerialization() {
        Package swingPackage = Package.getPackage("javax.swing");
        // 为了使 Swing 序列化正确工作，Swing 需要在 Component 进行序列化之前
        // 被通知。这个 hack 适应了这一点。
        //
        // Swing 类必须由引导类加载器加载，
        // 否则我们不考虑它们。
        for (Class<?> klass = Component.this.getClass(); klass != null;
                   klass = klass.getSuperclass()) {
            if (klass.getPackage() == swingPackage &&
                      klass.getClassLoader() == null) {
                final Class<?> swingClass = klass;
                // 查找 compWriteObjectNotify 方法的第一个重写
                Method[] methods = AccessController.doPrivileged(
                                                                 new PrivilegedAction<Method[]>() {
                                                                     public Method[] run() {
                                                                         return swingClass.getDeclaredMethods();
                                                                     }
                                                                 });
                for (int counter = methods.length - 1; counter >= 0;
                     counter--) {
                    final Method method = methods[counter];
                    if (method.getName().equals("compWriteObjectNotify")){
                        // 我们找到了，使用 doPrivileged 使其可访问
                        // 以便使用。
                        AccessController.doPrivileged(new PrivilegedAction<Void>() {
                                public Void run() {
                                    method.setAccessible(true);
                                    return null;
                                }
                            });
                        // 调用该方法
                        try {
                            method.invoke(this, (Object[]) null);
                        } catch (IllegalAccessException iae) {
                        } catch (InvocationTargetException ite) {
                        }
                        // 完成，退出。
                        return;
                    }
                }
            }
        }
    }

    /**
     * 将默认的可序列化字段写入流。写入
     * 0 个或多个可序列化监听器作为可选数据。
     * 非可序列化监听器被检测到，
     * 不会尝试序列化它们。
     *
     * @param s 要写入的 <code>ObjectOutputStream</code>
     * @serialData 以 <code>null</code> 结尾的
     *   0 个或多个对的序列；对由一个 <code>String</code>
     *   和一个 <code>Object</code> 组成；<code>String</code> 表示
     *   对象的类型，是以下之一（截至 1.4）：
     *   <code>componentListenerK</code> 表示一个
     *     <code>ComponentListener</code> 对象；
     *   <code>focusListenerK</code> 表示一个
     *     <code>FocusListener</code> 对象；
     *   <code>keyListenerK</code> 表示一个
     *     <code>KeyListener</code> 对象；
     *   <code>mouseListenerK</code> 表示一个
     *     <code>MouseListener</code> 对象；
     *   <code>mouseMotionListenerK</code> 表示一个
     *     <code>MouseMotionListener</code> 对象；
     *   <code>inputMethodListenerK</code> 表示一个
     *     <code>InputMethodListener</code> 对象；
     *   <code>hierarchyListenerK</code> 表示一个
     *     <code>HierarchyListener</code> 对象；
     *   <code>hierarchyBoundsListenerK</code> 表示一个
     *     <code>HierarchyBoundsListener</code> 对象；
     *   <code>mouseWheelListenerK</code> 表示一个
     *     <code>MouseWheelListener</code> 对象
     * @serialData 一个可选的 <code>ComponentOrientation</code>
     *    （在 <code>inputMethodListener</code> 之后，自 1.2 起）
     *
     * @see AWTEventMulticaster#save(java.io.ObjectOutputStream, java.lang.String, java.util.EventListener)
     * @see #componentListenerK
     * @see #focusListenerK
     * @see #keyListenerK
     * @see #mouseListenerK
     * @see #mouseMotionListenerK
     * @see #inputMethodListenerK
     * @see #hierarchyListenerK
     * @see #hierarchyBoundsListenerK
     * @see #mouseWheelListenerK
     * @see #readObject(ObjectInputStream)
     */
    private void writeObject(ObjectOutputStream s)
      throws IOException
    {
        doSwingSerialization();

        s.defaultWriteObject();

        AWTEventMulticaster.save(s, componentListenerK, componentListener);
        AWTEventMulticaster.save(s, focusListenerK, focusListener);
        AWTEventMulticaster.save(s, keyListenerK, keyListener);
        AWTEventMulticaster.save(s, mouseListenerK, mouseListener);
        AWTEventMulticaster.save(s, mouseMotionListenerK, mouseMotionListener);
        AWTEventMulticaster.save(s, inputMethodListenerK, inputMethodListener);

        s.writeObject(null);
        s.writeObject(componentOrientation);

        AWTEventMulticaster.save(s, hierarchyListenerK, hierarchyListener);
        AWTEventMulticaster.save(s, hierarchyBoundsListenerK,
                                 hierarchyBoundsListener);
        s.writeObject(null);

        AWTEventMulticaster.save(s, mouseWheelListenerK, mouseWheelListener);
        s.writeObject(null);

    }

    /**
     * 读取 <code>ObjectInputStream</code> 并如果不是
     * <code>null</code>，则添加一个监听器以接收
     * 由组件触发的各种事件。
     * 未识别的键或值将被忽略。
     *
     * @param s 要读取的 <code>ObjectInputStream</code>
     * @see #writeObject(ObjectOutputStream)
     */
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException
    {
        objectLock = new Object();

        acc = AccessController.getContext();

        s.defaultReadObject();

        appContext = AppContext.getAppContext();
        coalescingEnabled = checkCoalescing();
        if (componentSerializedDataVersion < 4) {
            // 这些字段是非瞬态的，依赖于默认
            // 序列化。但是，默认值是不充分的，
            // 因此我们需要为 1.4 之前的数据流显式设置它们。
            focusable = true;
            isFocusTraversableOverridden = FOCUS_TRAVERSABLE_UNKNOWN;
            initializeFocusTraversalKeys();
            focusTraversalKeysEnabled = true;
        }

        Object keyOrNull;
        while(null != (keyOrNull = s.readObject())) {
            String key = ((String)keyOrNull).intern();

            if (componentListenerK == key)
                addComponentListener((ComponentListener)(s.readObject()));

            else if (focusListenerK == key)
                addFocusListener((FocusListener)(s.readObject()));

            else if (keyListenerK == key)
                addKeyListener((KeyListener)(s.readObject()));

            else if (mouseListenerK == key)
                addMouseListener((MouseListener)(s.readObject()));

            else if (mouseMotionListenerK == key)
                addMouseMotionListener((MouseMotionListener)(s.readObject()));

            else if (inputMethodListenerK == key)
                addInputMethodListener((InputMethodListener)(s.readObject()));

            else // 跳过未识别键的值
                s.readObject();

        }

        // 如果存在，读取组件的方向
        Object orient = null;

        try {
            orient = s.readObject();
        } catch (java.io.OptionalDataException e) {
            // JDK 1.1 实例将没有此可选数据。
            // e.eof 将为 true，表示该对象没有更多数据。
            // 如果 e.eof 不为 true，则抛出异常，因为它
            // 可能是由与 componentOrientation 无关的原因引起的。

            if (!e.eof)  {
                throw (e);
            }
        }

        if (orient != null) {
            componentOrientation = (ComponentOrientation)orient;
        } else {
            componentOrientation = ComponentOrientation.UNKNOWN;
        }

        try {
            while(null != (keyOrNull = s.readObject())) {
                String key = ((String)keyOrNull).intern();

                if (hierarchyListenerK == key) {
                    addHierarchyListener((HierarchyListener)(s.readObject()));
                }
                else if (hierarchyBoundsListenerK == key) {
                    addHierarchyBoundsListener((HierarchyBoundsListener)
                                               (s.readObject()));
                }
                else {
                    // 跳过未识别键的值
                    s.readObject();
                }
            }
        } catch (java.io.OptionalDataException e) {
            // JDK 1.1/1.2 实例将没有此可选数据。
            // e.eof 将为 true，表示该对象没有更多数据。
            // 如果 e.eof 不为 true，则抛出异常，因为它
            // 可能是由与 hierarchy 和 hierarchyBounds 监听器无关的原因引起的。

            if (!e.eof)  {
                throw (e);
            }
        }

        try {
            while (null != (keyOrNull = s.readObject())) {
                String key = ((String)keyOrNull).intern();

                if (mouseWheelListenerK == key) {
                    addMouseWheelListener((MouseWheelListener)(s.readObject()));
                }
                else {
                    // 跳过未识别键的值
                    s.readObject();
                }
            }
        } catch (java.io.OptionalDataException e) {
            // pre-1.3 实例将没有此可选数据。
            // e.eof 将为 true，表示该对象没有更多数据。
            // 如果 e.eof 不为 true，则抛出异常，因为它
            // 可能是由与鼠标滚轮监听器无关的原因引起的。

            if (!e.eof)  {
                throw (e);
            }
        }

        if (popups != null) {
            int npopups = popups.size();
            for (int i = 0 ; i < npopups ; i++) {
                PopupMenu popup = popups.elementAt(i);
                popup.parent = this;
            }
        }
    }

    /**
     * 设置用于确定此组件内元素或文本顺序的语言敏感方向。
     * 语言敏感的 <code>LayoutManager</code> 和 <code>Component</code>
     * 子类将使用此属性来
     * 确定如何布局和绘制组件。
     * <p>
     * 在构造时，组件的方向设置为
     * <code>ComponentOrientation.UNKNOWN</code>，
     * 表示它没有被显式指定。
     * UNKNOWN 方向的行为与
     * <code>ComponentOrientation.LEFT_TO_RIGHT</code> 相同。
     * <p>
     * 要设置单个组件的方向，使用此方法。
     * 要设置整个组件
     * 层次结构的方向，使用
     * {@link #applyComponentOrientation applyComponentOrientation}。
     * <p>
     * 此方法更改与布局相关的信息，因此，
     * 使组件层次结构无效。
     *
     *
     * @see ComponentOrientation
     * @see #invalidate
     *
     * @author Laura Werner, IBM
     * @beaninfo
     *       bound: true
     */
    public void setComponentOrientation(ComponentOrientation o) {
        ComponentOrientation oldValue = componentOrientation;
        componentOrientation = o;


                    // 这是一个绑定属性，因此向
        // 任何已注册的监听器报告更改。（如果没有监听器，成本很低。）
        firePropertyChange("componentOrientation", oldValue, o);

        // 这可能会改变组件的首选大小。
        invalidateIfValid();
    }

    /**
     * 检索用于排序
     * 此组件内元素或文本的语言敏感方向。希望尊重方向的
     * <code>LayoutManager</code> 和 <code>Component</code>
     * 子类应在执行布局或绘制之前调用此方法以获取组件的方向。
     *
     * @see ComponentOrientation
     *
     * @author Laura Werner, IBM
     */
    public ComponentOrientation getComponentOrientation() {
        return componentOrientation;
    }

    /**
     * 设置此组件及其包含的所有组件的 <code>ComponentOrientation</code> 属性。
     * <p>
     * 此方法会更改布局相关的信息，因此，
     * 使组件层次结构无效。
     *
     *
     * @param orientation 此组件及其包含的组件的新组件方向。
     * @exception NullPointerException 如果 <code>orientation</code> 为 null。
     * @see #setComponentOrientation
     * @see #getComponentOrientation
     * @see #invalidate
     * @since 1.4
     */
    public void applyComponentOrientation(ComponentOrientation orientation) {
        if (orientation == null) {
            throw new NullPointerException();
        }
        setComponentOrientation(orientation);
    }

    final boolean canBeFocusOwner() {
        // 它已启用、可见且可聚焦。
        if (isEnabled() && isDisplayable() && isVisible() && isFocusable()) {
            return true;
        }
        return false;
    }

    /**
     * 检查此组件是否满足成为焦点所有者的前提条件：
     * - 它已启用、可见且可聚焦
     * - 它的父组件都已启用并显示
     * - 顶级窗口可聚焦
     * - 如果焦点循环根具有 DefaultFocusTraversalPolicy，则还检查此策略是否接受
     * 此组件作为焦点所有者
     * @since 1.5
     */
    final boolean canBeFocusOwnerRecursively() {
        // - 它已启用、可见且可聚焦
        if (!canBeFocusOwner()) {
            return false;
        }

        // - 它的父组件都已启用并显示
        synchronized(getTreeLock()) {
            if (parent != null) {
                return parent.canContainFocusOwner(this);
            }
        }
        return true;
    }

    /**
     * 在 LW 容器层次结构中修复 HW 组件的位置。
     */
    final void relocateComponent() {
        synchronized (getTreeLock()) {
            if (peer == null) {
                return;
            }
            int nativeX = x;
            int nativeY = y;
            for (Component cont = getContainer();
                    cont != null && cont.isLightweight();
                    cont = cont.getContainer())
            {
                nativeX += cont.x;
                nativeY += cont.y;
            }
            peer.setBounds(nativeX, nativeY, width, height,
                    ComponentPeer.SET_LOCATION);
        }
    }

    /**
     * 返回组件的 <code>Window</code> 祖先。
     * @return 组件的 Window 祖先或组件本身（如果它是 Window）；
     *         如果组件不是窗口层次结构的一部分，则返回 null
     */
    Window getContainingWindow() {
        return SunToolkit.getContainingWindow(this);
    }

    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();

    /*
     * --- Accessibility Support ---
     *
     *  Component 将包含接口 Accessible 中的所有方法，
     *  虽然它实际上不会实现该接口 - 这将由
     *  扩展 Component 的各个对象来实现。
     */

    /**
     * 与此 <code>Component</code> 关联的 {@code AccessibleContext}。
     */
    protected AccessibleContext accessibleContext = null;

    /**
     * 获取与此 <code>Component</code> 关联的 <code>AccessibleContext</code>。
     * 该基类实现的方法返回 null。扩展 <code>Component</code>
     * 的类应实现此方法以返回与子类关联的
     * <code>AccessibleContext</code>。
     *
     *
     * @return 与此 <code>Component</code> 关联的
     *    <code>AccessibleContext</code>
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        return accessibleContext;
    }

    /**
     * Component 的内部类，用于提供对可访问性的默认支持。
     * 此类不应用于直接由应用程序开发人员使用，而是仅用于
     * 组件开发人员的子类。
     * <p>
     * 用于获取此对象的可访问角色的类。
     * @since 1.3
     */
    protected abstract class AccessibleAWTComponent extends AccessibleContext
        implements Serializable, AccessibleComponent {

        private static final long serialVersionUID = 642321655757800191L;

        /**
         * 虽然该类是抽象的，但所有子类都应调用此方法。
         */
        protected AccessibleAWTComponent() {
        }

        /**
         * 注册的 PropertyChangeListener 对象数量。用于
         * 添加/移除 ComponentListener 和 FocusListener 以跟踪
         * 目标组件的状态。
         */
        private volatile transient int propertyListenersCount = 0;

        protected ComponentListener accessibleAWTComponentHandler = null;
        protected FocusListener accessibleAWTFocusHandler = null;

        /**
         * 如果注册了 PropertyChange 监听器，
         * 则在显示/隐藏时触发 PropertyChange 监听器。
         * @since 1.3
         */
        protected class AccessibleAWTComponentHandler implements ComponentListener {
            public void componentHidden(ComponentEvent e)  {
                if (accessibleContext != null) {
                    accessibleContext.firePropertyChange(
                                                         AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                                         AccessibleState.VISIBLE, null);
                }
            }

            public void componentShown(ComponentEvent e)  {
                if (accessibleContext != null) {
                    accessibleContext.firePropertyChange(
                                                         AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                                         null, AccessibleState.VISIBLE);
                }
            }

            public void componentMoved(ComponentEvent e)  {
            }

            public void componentResized(ComponentEvent e)  {
            }
        } // inner class AccessibleAWTComponentHandler


        /**
         * 如果注册了 PropertyChange 监听器，
         * 则在焦点事件发生时触发 PropertyChange 监听器。
         * @since 1.3
         */
        protected class AccessibleAWTFocusHandler implements FocusListener {
            public void focusGained(FocusEvent event) {
                if (accessibleContext != null) {
                    accessibleContext.firePropertyChange(
                                                         AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                                         null, AccessibleState.FOCUSED);
                }
            }
            public void focusLost(FocusEvent event) {
                if (accessibleContext != null) {
                    accessibleContext.firePropertyChange(
                                                         AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                                         AccessibleState.FOCUSED, null);
                }
            }
        }  // inner class AccessibleAWTFocusHandler


        /**
         * 将 <code>PropertyChangeListener</code> 添加到监听器列表中。
         *
         * @param listener  要添加的属性更改监听器
         */
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            if (accessibleAWTComponentHandler == null) {
                accessibleAWTComponentHandler = new AccessibleAWTComponentHandler();
            }
            if (accessibleAWTFocusHandler == null) {
                accessibleAWTFocusHandler = new AccessibleAWTFocusHandler();
            }
            if (propertyListenersCount++ == 0) {
                Component.this.addComponentListener(accessibleAWTComponentHandler);
                Component.this.addFocusListener(accessibleAWTFocusHandler);
            }
            super.addPropertyChangeListener(listener);
        }

        /**
         * 从监听器列表中移除 PropertyChangeListener。
         * 这会移除为所有属性注册的 PropertyChangeListener。
         *
         * @param listener  要移除的属性更改监听器
         */
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            if (--propertyListenersCount == 0) {
                Component.this.removeComponentListener(accessibleAWTComponentHandler);
                Component.this.removeFocusListener(accessibleAWTFocusHandler);
            }
            super.removePropertyChangeListener(listener);
        }

        // AccessibleContext 方法
        //
        /**
         * 获取此对象的可访问名称。这几乎不应该
         * 返回 <code>java.awt.Component.getName()</code>，
         * 因为这通常不是一个本地化的名称，
         * 并且对用户没有意义。如果
         * 对象本质上是一个文本对象（例如菜单项），则
         * 可访问名称应该是对象的文本（例如 "save"）。
         * 如果对象有工具提示，工具提示文本也可能是
         * 一个合适的字符串。
         *
         * @return 对象的本地化名称 -- 如果此
         *         对象没有名称，则可以为
         *         <code>null</code>
         * @see javax.accessibility.AccessibleContext#setAccessibleName
         */
        public String getAccessibleName() {
            return accessibleName;
        }

        /**
         * 获取此对象的可访问描述。这应该是
         * 一个简洁的、本地化的描述，说明这个对象是什么 - 对
         * 用户来说有什么意义。如果对象有工具提示，工具提示文本可能是一个合适的字符串，
         * 前提是它包含对象的简洁描述（而不是仅仅对象的名称 - 例如，工具栏上的 "Save" 图标
         * 如果工具提示文本是 "save"，则不应返回工具提示文本作为描述，而应返回类似 "Saves the current
         * text document" 的内容）。
         *
         * @return 对象的本地化描述 -- 如果此对象没有描述，则可以为
         *        <code>null</code>
         * @see javax.accessibility.AccessibleContext#setAccessibleDescription
         */
        public String getAccessibleDescription() {
            return accessibleDescription;
        }

        /**
         * 获取此对象的角色。
         *
         * @return 一个 <code>AccessibleRole</code> 实例
         *      描述对象的角色
         * @see javax.accessibility.AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.AWT_COMPONENT;
        }

        /**
         * 获取此对象的状态。
         *
         * @return 一个 <code>AccessibleStateSet</code> 实例
         *       包含对象的当前状态集
         * @see javax.accessibility.AccessibleState
         */
        public AccessibleStateSet getAccessibleStateSet() {
            return Component.this.getAccessibleStateSet();
        }

        /**
         * 获取此对象的可访问父对象。
         * 如果此对象的父对象实现了 <code>Accessible</code>，
         * 则此方法应简单地返回 <code>getParent</code>。
         *
         * @return 此对象的 <code>Accessible</code> 父对象 -- 如果此
         *      对象没有 <code>Accessible</code> 父对象，则可以为
         *      <code>null</code>
         */
        public Accessible getAccessibleParent() {
            if (accessibleParent != null) {
                return accessibleParent;
            } else {
                Container parent = getParent();
                if (parent instanceof Accessible) {
                    return (Accessible) parent;
                }
            }
            return null;
        }

        /**
         * 获取此对象在其可访问父对象中的索引。
         *
         * @return 此对象在其父对象中的索引；如果此
         *    对象没有可访问父对象，则返回 -1
         * @see #getAccessibleParent
         */
        public int getAccessibleIndexInParent() {
            return Component.this.getAccessibleIndexInParent();
        }

        /**
         * 返回此对象中的可访问子对象数量。如果
         * 此对象的所有子对象都实现了 <code>Accessible</code>，
         * 则此方法应返回此对象的子对象数量。
         *
         * @return 此对象中的可访问子对象数量
         */
        public int getAccessibleChildrenCount() {
            return 0; // Components don't have children
        }

        /**
         * 返回此对象的第 n 个 <code>Accessible</code> 子对象。
         *
         * @param i 从零开始的子对象索引
         * @return 此对象的第 n 个 <code>Accessible</code> 子对象
         */
        public Accessible getAccessibleChild(int i) {
            return null; // Components don't have children
        }

        /**
         * 返回此对象的区域设置。
         *
         * @return 此对象的区域设置
         */
        public Locale getLocale() {
            return Component.this.getLocale();
        }

        /**
         * 获取与此对象关联的 <code>AccessibleComponent</code>（如果存在）。
         * 否则返回 <code>null</code>。
         *
         * @return 组件
         */
        public AccessibleComponent getAccessibleComponent() {
            return this;
        }


        // AccessibleComponent 方法
        //
        /**
         * 获取此对象的背景颜色。
         *
         * @return 如果支持，则返回对象的背景颜色；
         *      否则，返回 <code>null</code>
         */
        public Color getBackground() {
            return Component.this.getBackground();
        }

        /**
         * 设置此对象的背景颜色。
         * （有关透明度，请参见 <code>isOpaque</code>。）
         *
         * @param c 背景的新 <code>Color</code>
         * @see Component#isOpaque
         */
        public void setBackground(Color c) {
            Component.this.setBackground(c);
        }


        /**
         * 获取此对象的前景色。
         *
         * @return 如果支持，则返回对象的前景色；否则返回 <code>null</code>
         */
        public Color getForeground() {
            return Component.this.getForeground();
        }

        /**
         * 设置此对象的前景色。
         *
         * @param c 前景的新 <code>Color</code>
         */
        public void setForeground(Color c) {
            Component.this.setForeground(c);
        }

        /**
         * 获取此对象的 <code>Cursor</code>。
         *
         * @return 如果支持，则返回对象的 <code>Cursor</code>；否则返回 <code>null</code>
         */
        public Cursor getCursor() {
            return Component.this.getCursor();
        }

        /**
         * 设置此对象的 <code>Cursor</code>。
         * <p>
         * 如果 Java 平台实现和/或本机系统不支持更改鼠标指针形状，此方法可能没有视觉效果。
         * @param cursor 对象的新 <code>Cursor</code>
         */
        public void setCursor(Cursor cursor) {
            Component.this.setCursor(cursor);
        }

        /**
         * 获取此对象的 <code>Font</code>。
         *
         * @return 如果支持，则返回对象的 <code>Font</code>；否则返回 <code>null</code>
         */
        public Font getFont() {
            return Component.this.getFont();
        }

        /**
         * 设置此对象的 <code>Font</code>。
         *
         * @param f 对象的新 <code>Font</code>
         */
        public void setFont(Font f) {
            Component.this.setFont(f);
        }

        /**
         * 获取此对象的 <code>FontMetrics</code>。
         *
         * @param f <code>Font</code>
         * @return 如果支持，则返回对象的 <code>FontMetrics</code>；否则返回 <code>null</code>
         * @see #getFont
         */
        public FontMetrics getFontMetrics(Font f) {
            if (f == null) {
                return null;
            } else {
                return Component.this.getFontMetrics(f);
            }
        }

        /**
         * 确定对象是否已启用。
         *
         * @return 如果对象已启用，则返回 true；否则返回 false
         */
        public boolean isEnabled() {
            return Component.this.isEnabled();
        }

        /**
         * 设置对象的启用状态。
         *
         * @param b 如果为 true，则启用此对象；否则禁用它
         */
        public void setEnabled(boolean b) {
            boolean old = Component.this.isEnabled();
            Component.this.setEnabled(b);
            if (b != old) {
                if (accessibleContext != null) {
                    if (b) {
                        accessibleContext.firePropertyChange(
                                                             AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                                             null, AccessibleState.ENABLED);
                    } else {
                        accessibleContext.firePropertyChange(
                                                             AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                                             AccessibleState.ENABLED, null);
                    }
                }
            }
        }

        /**
         * 确定对象是否可见。注意：这表示对象打算可见；但是，由于包含此对象的某个对象不可见，它可能实际上并未显示在屏幕上。要确定对象是否显示在屏幕上，请使用 <code>isShowing</code>。
         *
         * @return 如果对象可见，则返回 true；否则返回 false
         */
        public boolean isVisible() {
            return Component.this.isVisible();
        }

        /**
         * 设置对象的可见状态。
         *
         * @param b 如果为 true，则显示此对象；否则隐藏它
         */
        public void setVisible(boolean b) {
            boolean old = Component.this.isVisible();
            Component.this.setVisible(b);
            if (b != old) {
                if (accessibleContext != null) {
                    if (b) {
                        accessibleContext.firePropertyChange(
                                                             AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                                             null, AccessibleState.VISIBLE);
                    } else {
                        accessibleContext.firePropertyChange(
                                                             AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                                             AccessibleState.VISIBLE, null);
                    }
                }
            }
        }

        /**
         * 确定对象是否显示。这是通过检查对象及其祖先的可见性来确定的。注意：即使对象被其他对象遮挡（例如，它恰好在下拉菜单下方），此方法也会返回 true。
         *
         * @return 如果对象显示，则返回 true；否则返回 false
         */
        public boolean isShowing() {
            return Component.this.isShowing();
        }

        /**
         * 检查指定的点是否在对象的边界内，其中点的 x 和 y 坐标相对于对象的坐标系定义。
         *
         * @param p 相对于对象坐标系的 <code>Point</code>
         * @return 如果对象包含 <code>Point</code>，则返回 true；否则返回 false
         */
        public boolean contains(Point p) {
            return Component.this.contains(p);
        }

        /**
         * 返回对象在屏幕上的位置。
         *
         * @return 对象在屏幕上的位置 -- 如果此对象不在屏幕上，则返回 <code>null</code>
         */
        public Point getLocationOnScreen() {
            synchronized (Component.this.getTreeLock()) {
                if (Component.this.isShowing()) {
                    return Component.this.getLocationOnScreen();
                } else {
                    return null;
                }
            }
        }

        /**
         * 获取对象相对于父对象的位置，形式为一个点，表示对象的左上角在屏幕坐标空间中的位置。
         *
         * @return 一个表示对象边界左上角的 Point 实例，在屏幕坐标空间中；如果此对象或其父对象不在屏幕上，则返回 <code>null</code>
         */
        public Point getLocation() {
            return Component.this.getLocation();
        }

        /**
         * 设置对象相对于父对象的位置。
         * @param p  对象的坐标
         */
        public void setLocation(Point p) {
            Component.this.setLocation(p);
        }

        /**
         * 以 Rectangle 对象的形式获取此对象的边界。边界指定了此对象的宽度、高度和相对于其父对象的位置。
         *
         * @return 一个表示此组件边界的矩形；如果此对象不在屏幕上，则返回 <code>null</code>
         */
        public Rectangle getBounds() {
            return Component.this.getBounds();
        }

        /**
         * 以 <code>Rectangle</code> 对象的形式设置此对象的边界。边界指定了此对象的宽度、高度和相对于其父对象的位置。
         *
         * @param r 一个表示此组件边界的矩形
         */
        public void setBounds(Rectangle r) {
            Component.this.setBounds(r);
        }

        /**
         * 以 <code>Dimension</code> 对象的形式返回此对象的大小。<code>Dimension</code> 对象的 height 字段包含此对象的高度，width 字段包含此对象的宽度。
         *
         * @return 一个表示此组件大小的 <code>Dimension</code> 对象；如果此对象不在屏幕上，则返回 <code>null</code>
         */
        public Dimension getSize() {
            return Component.this.getSize();
        }

        /**
         * 调整此对象的大小，使其具有指定的宽度和高度。
         *
         * @param d 指定对象新大小的维度
         */
        public void setSize(Dimension d) {
            Component.this.setSize(d);
        }

        /**
         * 返回位于本地坐标 <code>Point</code> 处的 <code>Accessible</code> 子对象。如果不存在，则返回 <code>null</code>。
         *
         * @param p 定义 <code>Accessible</code> 左上角的点，以对象父对象的坐标空间给出
         * @return 如果存在，则返回指定位置的 <code>Accessible</code>；否则返回 <code>null</code>
         */
        public Accessible getAccessibleAt(Point p) {
            return null; // Components don't have children
        }

        /**
         * 返回此对象是否可以接受焦点。
         *
         * @return 如果对象可以接受焦点，则返回 true；否则返回 false
         */
        public boolean isFocusTraversable() {
            return Component.this.isFocusTraversable();
        }

        /**
         * 请求此对象获得焦点。
         */
        public void requestFocus() {
            Component.this.requestFocus();
        }

        /**
         * 添加指定的焦点监听器以接收此组件的焦点事件。
         *
         * @param l 焦点监听器
         */
        public void addFocusListener(FocusListener l) {
            Component.this.addFocusListener(l);
        }

        /**
         * 移除指定的焦点监听器，使其不再接收此组件的焦点事件。
         *
         * @param l 焦点监听器
         */
        public void removeFocusListener(FocusListener l) {
            Component.this.removeFocusListener(l);
        }

    } // inner class AccessibleAWTComponent


    /**
     * 获取此对象在其可访问父对象中的索引。如果此对象没有可访问父对象，则返回 -1。
     *
     * @return 此对象在其可访问父对象中的索引
     */
    int getAccessibleIndexInParent() {
        synchronized (getTreeLock()) {
            int index = -1;
            Container parent = this.getParent();
            if (parent != null && parent instanceof Accessible) {
                Component ca[] = parent.getComponents();
                for (int i = 0; i < ca.length; i++) {
                    if (ca[i] instanceof Accessible) {
                        index++;
                    }
                    if (this.equals(ca[i])) {
                        return index;
                    }
                }
            }
            return -1;
        }
    }

    /**
     * 获取此对象的当前状态集。
     *
     * @return 包含对象当前状态集的 <code>AccessibleStateSet</code> 实例
     * @see AccessibleState
     */
    AccessibleStateSet getAccessibleStateSet() {
        synchronized (getTreeLock()) {
            AccessibleStateSet states = new AccessibleStateSet();
            if (this.isEnabled()) {
                states.add(AccessibleState.ENABLED);
            }
            if (this.isFocusTraversable()) {
                states.add(AccessibleState.FOCUSABLE);
            }
            if (this.isVisible()) {
                states.add(AccessibleState.VISIBLE);
            }
            if (this.isShowing()) {
                states.add(AccessibleState.SHOWING);
            }
            if (this.isFocusOwner()) {
                states.add(AccessibleState.FOCUSED);
            }
            if (this instanceof Accessible) {
                AccessibleContext ac = ((Accessible) this).getAccessibleContext();
                if (ac != null) {
                    Accessible ap = ac.getAccessibleParent();
                    if (ap != null) {
                        AccessibleContext pac = ap.getAccessibleContext();
                        if (pac != null) {
                            AccessibleSelection as = pac.getAccessibleSelection();
                            if (as != null) {
                                states.add(AccessibleState.SELECTABLE);
                                int i = ac.getAccessibleIndexInParent();
                                if (i >= 0) {
                                    if (as.isAccessibleChildSelected(i)) {
                                        states.add(AccessibleState.SELECTED);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (Component.isInstanceOf(this, "javax.swing.JComponent")) {
                if (((javax.swing.JComponent) this).isOpaque()) {
                    states.add(AccessibleState.OPAQUE);
                }
            }
            return states;
        }
    }

    /**
     * 检查给定对象是否是指定类的实例。
     * @param obj 要检查的对象
     * @param className 类的名称。必须是完全限定的类名。
     * @return 如果此对象是指定类的实例，则返回 true；否则返回 false，或者如果 obj 或 className 为 null 也返回 false
     */
    static boolean isInstanceOf(Object obj, String className) {
        if (obj == null) return false;
        if (className == null) return false;

        Class<?> cls = obj.getClass();
        while (cls != null) {
            if (cls.getName().equals(className)) {
                return true;
            }
            cls = cls.getSuperclass();
        }
        return false;
    }


    // ************************** MIXING CODE *******************************

    /**
     * 检查是否可以信任组件的当前边界。返回值为 false 表示组件的容器无效，因此需要布局，这可能会改变其子组件的边界。容器的空布局或容器不存在表示组件的边界是最终的，可以信任。
     */
    final boolean areBoundsValid() {
        Container cont = getContainer();
        return cont == null || cont.isValid() || cont.getLayout() == null;
    }


                /**
     * 将形状应用于组件
     * @param shape 要应用于组件的形状
     */
    void applyCompoundShape(Region shape) {
        checkTreeLock();

        if (!areBoundsValid()) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + this + "; areBoundsValid = " + areBoundsValid());
            }
            return;
        }

        if (!isLightweight()) {
            ComponentPeer peer = getPeer();
            if (peer != null) {
                // Region 类有一些优化。因此，我们应该手动检查它是否为空，
                // 并自己替换对象。否则，我们可能会得到一个不正确的 Region 对象，例如 loX 大于 hiX。
                if (shape.isEmpty()) {
                    shape = Region.EMPTY_REGION;
                }


                // 注意：形状实际上没有被复制/克隆。我们自己创建 Region 对象，因此没有可能在混合代码之外修改该对象。
                // 将 compoundShape 置为 null 表示组件具有正常形状（或根本没有形状）。
                if (shape.equals(getNormalShape())) {
                    if (this.compoundShape == null) {
                        return;
                    }
                    this.compoundShape = null;
                    peer.applyShape(null);
                } else {
                    if (shape.equals(getAppliedShape())) {
                        return;
                    }
                    this.compoundShape = shape;
                    Point compAbsolute = getLocationOnWindow();
                    if (mixingLog.isLoggable(PlatformLogger.Level.FINER)) {
                        mixingLog.fine("this = " + this +
                                "; compAbsolute=" + compAbsolute + "; shape=" + shape);
                    }
                    peer.applyShape(shape.getTranslatedRegion(-compAbsolute.x, -compAbsolute.y));
                }
            }
        }
    }

    /**
     * 返回之前使用 applyCompoundShape() 设置的形状。
     * 如果组件是轻量级组件或尚未应用任何形状，
     * 该方法返回正常形状。
     */
    private Region getAppliedShape() {
        checkTreeLock();
        //XXX: 如果允许轻量级组件具有形状，这必须更改
        return (this.compoundShape == null || isLightweight()) ? getNormalShape() : this.compoundShape;
    }

    Point getLocationOnWindow() {
        checkTreeLock();
        Point curLocation = getLocation();

        for (Container parent = getContainer();
                parent != null && !(parent instanceof Window);
                parent = parent.getContainer())
        {
            curLocation.x += parent.getX();
            curLocation.y += parent.getY();
        }

        return curLocation;
    }

    /**
     * 返回位于窗口坐标中的组件的完整形状
     */
    final Region getNormalShape() {
        checkTreeLock();
        //XXX: 我们可以考虑为该组件指定用户定义的形状
        Point compAbsolute = getLocationOnWindow();
        return
            Region.getInstanceXYWH(
                    compAbsolute.x,
                    compAbsolute.y,
                    getWidth(),
                    getHeight()
            );
    }

    /**
     * 返回组件的“不透明形状”。
     *
     * 轻量级组件的不透明形状是需要从重量级组件中裁剪的实际形状，以便正确地将此轻量级组件与它们混合。
     *
     * 该方法在 java.awt.Container 中被重写，以处理包含不透明子组件的非不透明容器。
     *
     * 详情见 6637655。
     */
    Region getOpaqueShape() {
        checkTreeLock();
        if (mixingCutoutRegion != null) {
            return mixingCutoutRegion;
        } else {
            return getNormalShape();
        }
    }

    final int getSiblingIndexAbove() {
        checkTreeLock();
        Container parent = getContainer();
        if (parent == null) {
            return -1;
        }

        int nextAbove = parent.getComponentZOrder(this) - 1;

        return nextAbove < 0 ? -1 : nextAbove;
    }

    final ComponentPeer getHWPeerAboveMe() {
        checkTreeLock();

        Container cont = getContainer();
        int indexAbove = getSiblingIndexAbove();

        while (cont != null) {
            for (int i = indexAbove; i > -1; i--) {
                Component comp = cont.getComponent(i);
                if (comp != null && comp.isDisplayable() && !comp.isLightweight()) {
                    return comp.getPeer();
                }
            }
            // 遍历层次结构，直到最近的重量级容器；
            // 进一步遍历可能会返回一个实际上不是此组件的本地兄弟组件的组件，这种 z-order 请求可能不被底层系统允许（6852051）。
            if (!cont.isLightweight()) {
                break;
            }

            indexAbove = cont.getSiblingIndexAbove();
            cont = cont.getContainer();
        }

        return null;
    }

    final int getSiblingIndexBelow() {
        checkTreeLock();
        Container parent = getContainer();
        if (parent == null) {
            return -1;
        }

        int nextBelow = parent.getComponentZOrder(this) + 1;

        return nextBelow >= parent.getComponentCount() ? -1 : nextBelow;
    }

    final boolean isNonOpaqueForMixing() {
        return mixingCutoutRegion != null &&
            mixingCutoutRegion.isEmpty();
    }

    private Region calculateCurrentShape() {
        checkTreeLock();
        Region s = getNormalShape();

        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("this = " + this + "; normalShape=" + s);
        }

        if (getContainer() != null) {
            Component comp = this;
            Container cont = comp.getContainer();

            while (cont != null) {
                for (int index = comp.getSiblingIndexAbove(); index != -1; --index) {
                    /* 假设：
                     *
                     *    getComponent(getContainer().getComponentZOrder(comp)) == comp
                     *
                     * 该假设是根据当前 Container 类的实现做出的。
                     */
                    Component c = cont.getComponent(index);
                    if (c.isLightweight() && c.isShowing()) {
                        s = s.getDifference(c.getOpaqueShape());
                    }
                }

                if (cont.isLightweight()) {
                    s = s.getIntersection(cont.getNormalShape());
                } else {
                    break;
                }

                comp = cont;
                cont = cont.getContainer();
            }
        }

        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("currentShape=" + s);
        }

        return s;
    }

    void applyCurrentShape() {
        checkTreeLock();
        if (!areBoundsValid()) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + this + "; areBoundsValid = " + areBoundsValid());
            }
            return; // 因为 applyCompoundShape() 会忽略这些组件
        }
        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("this = " + this);
        }
        applyCompoundShape(calculateCurrentShape());
    }

    final void subtractAndApplyShape(Region s) {
        checkTreeLock();

        if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
            mixingLog.fine("this = " + this + "; s=" + s);
        }

        applyCompoundShape(getAppliedShape().getDifference(s));
    }

    private final void applyCurrentShapeBelowMe() {
        checkTreeLock();
        Container parent = getContainer();
        if (parent != null && parent.isShowing()) {
            // 首先，重新应用我的兄弟组件的形状
            parent.recursiveApplyCurrentShape(getSiblingIndexBelow());

            // 其次，如果我的容器是非不透明的，重新应用我的容器的兄弟组件的形状
            Container parent2 = parent.getContainer();
            while (!parent.isOpaque() && parent2 != null) {
                parent2.recursiveApplyCurrentShape(parent.getSiblingIndexBelow());

                parent = parent2;
                parent2 = parent.getContainer();
            }
        }
    }

    final void subtractAndApplyShapeBelowMe() {
        checkTreeLock();
        Container parent = getContainer();
        if (parent != null && isShowing()) {
            Region opaqueShape = getOpaqueShape();

            // 首先，裁剪我的兄弟组件
            parent.recursiveSubtractAndApplyShape(opaqueShape, getSiblingIndexBelow());

            // 其次，如果我的容器是非不透明的，裁剪我的容器的兄弟组件
            Container parent2 = parent.getContainer();
            while (!parent.isOpaque() && parent2 != null) {
                parent2.recursiveSubtractAndApplyShape(opaqueShape, parent.getSiblingIndexBelow());

                parent = parent2;
                parent2 = parent.getContainer();
            }
        }
    }

    void mixOnShowing() {
        synchronized (getTreeLock()) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + this);
            }
            if (!isMixingNeeded()) {
                return;
            }
            if (isLightweight()) {
                subtractAndApplyShapeBelowMe();
            } else {
                applyCurrentShape();
            }
        }
    }

    void mixOnHiding(boolean isLightweight) {
        // 我们不能确定此时对等体是否存在，因此需要参数来确定隐藏的组件是（实际上是）轻量级还是重量级。
        synchronized (getTreeLock()) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + this + "; isLightweight = " + isLightweight);
            }
            if (!isMixingNeeded()) {
                return;
            }
            if (isLightweight) {
                applyCurrentShapeBelowMe();
            }
        }
    }

    void mixOnReshaping() {
        synchronized (getTreeLock()) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + this);
            }
            if (!isMixingNeeded()) {
                return;
            }
            if (isLightweight()) {
                applyCurrentShapeBelowMe();
            } else {
                applyCurrentShape();
            }
        }
    }

    void mixOnZOrderChanging(int oldZorder, int newZorder) {
        synchronized (getTreeLock()) {
            boolean becameHigher = newZorder < oldZorder;
            Container parent = getContainer();

            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + this +
                    "; oldZorder=" + oldZorder + "; newZorder=" + newZorder + "; parent=" + parent);
            }
            if (!isMixingNeeded()) {
                return;
            }
            if (isLightweight()) {
                if (becameHigher) {
                    if (parent != null && isShowing()) {
                        parent.recursiveSubtractAndApplyShape(getOpaqueShape(), getSiblingIndexBelow(), oldZorder);
                    }
                } else {
                    if (parent != null) {
                        parent.recursiveApplyCurrentShape(oldZorder, newZorder);
                    }
                }
            } else {
                if (becameHigher) {
                    applyCurrentShape();
                } else {
                    if (parent != null) {
                        Region shape = getAppliedShape();

                        for (int index = oldZorder; index < newZorder; index++) {
                            Component c = parent.getComponent(index);
                            if (c.isLightweight() && c.isShowing()) {
                                shape = shape.getDifference(c.getOpaqueShape());
                            }
                        }
                        applyCompoundShape(shape);
                    }
                }
            }
        }
    }

    void mixOnValidating() {
        // 该方法在 Container 中被重写。显然，非容器组件不需要处理验证。
    }

    final boolean isMixingNeeded() {
        if (SunToolkit.getSunAwtDisableMixing()) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINEST)) {
                mixingLog.finest("this = " + this + "; Mixing disabled via sun.awt.disableMixing");
            }
            return false;
        }
        if (!areBoundsValid()) {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + this + "; areBoundsValid = " + areBoundsValid());
            }
            return false;
        }
        Window window = getContainingWindow();
        if (window != null) {
            if (!window.hasHeavyweightDescendants() || !window.hasLightweightDescendants() || window.isDisposing()) {
                if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                    mixingLog.fine("containing window = " + window +
                            "; has h/w descendants = " + window.hasHeavyweightDescendants() +
                            "; has l/w descendants = " + window.hasLightweightDescendants() +
                            "; disposing = " + window.isDisposing());
                }
                return false;
            }
        } else {
            if (mixingLog.isLoggable(PlatformLogger.Level.FINE)) {
                mixingLog.fine("this = " + this + "; containing window is null");
            }
            return false;
        }
        return true;
    }

    // ****************** MIXING CODE END ********************************

    // 注意该方法在 Window 类中被重写，
    // 窗口不需要在 Z-order 中更新。
    void updateZOrder() {
        peer.setZOrder(getHWPeerAboveMe());
    }

}
