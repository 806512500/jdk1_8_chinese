
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

import java.awt.peer.DialogPeer;
import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.accessibility.*;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.PeerEvent;
import sun.awt.util.IdentityArrayList;
import sun.awt.util.IdentityLinkedList;
import sun.security.util.SecurityConstants;
import java.security.AccessControlException;

/**
 * 对话框是一个带有标题和边框的顶级窗口，通常用于从用户那里获取某种形式的输入。
 *
 * 对话框的大小包括指定的边框区域。可以使用 <code>getInsets</code> 方法获取边框区域的尺寸，但由于这些尺寸是平台依赖的，因此在对话框通过调用 <code>pack</code> 或 <code>show</code> 变为可显示之前，无法获得有效的内边距值。
 * 由于边框区域包含在对话框的总体尺寸中，边框实际上遮挡了对话框的一部分，将用于渲染和/或显示子组件的区域限制在左上角位置为 <code>(insets.left, insets.top)</code> 的矩形中，其大小为
 * <code>width - (insets.left + insets.right)</code> 乘以 <code>height - (insets.top + insets.bottom)</code>。
 * <p>
 * 对话框的默认布局是 <code>BorderLayout</code>。
 * <p>
 * 可以使用 <code>setUndecorated</code> 方法关闭对话框的本地装饰（即框架和标题栏）。这只能在对话框不可显示时进行。
 * <p>
 * 对话框在构造时可以有另一个窗口作为其所有者。当可见对话框的所有者窗口被最小化时，对话框将自动隐藏。当所有者窗口随后被恢复时，对话框将再次对用户可见。
 * <p>
 * 在多屏幕环境中，可以在与所有者不同的屏幕设备上创建 <code>Dialog</code>。有关更多信息，请参见 {@link java.awt.Frame}。
 * <p>
 * 对话框可以是无模式的（默认）或模式的。模式对话框是阻止应用程序中某些其他顶级窗口输入的对话框，除了以该对话框为所有者的任何窗口。有关详细信息，请参见 <a href="doc-files/Modality.html">AWT 模态性</a> 规范。
 * <p>
 * 对话框能够生成以下 <code>WindowEvents</code>：
 * <code>WindowOpened</code>、<code>WindowClosing</code>、<code>WindowClosed</code>、<code>WindowActivated</code>、
 * <code>WindowDeactivated</code>、<code>WindowGainedFocus</code>、<code>WindowLostFocus</code>。
 *
 * @see WindowEvent
 * @see Window#addWindowListener
 *
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 * @since       JDK1.0
 */
public class Dialog extends Window {

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * 对话框的可调整大小属性。如果对话框可调整大小，则为 true，否则为 false。
     *
     * @serial
     * @see #setResizable(boolean)
     */
    boolean resizable = true;


    /**
     * 此字段指示对话框是否无装饰。此属性只能在对话框不可显示时更改。
     * <code>undecorated</code> 为 true 表示对话框无装饰，否则为 false。
     *
     * @serial
     * @see #setUndecorated(boolean)
     * @see #isUndecorated()
     * @see Component#isDisplayable()
     * @since 1.4
     */
    boolean undecorated = false;

    private transient boolean initialized = false;

    /**
     * 模式对话框阻止某些顶级窗口的输入。特定窗口是否被阻止取决于对话框的模态类型；这称为“阻止范围”。<code>ModalityType</code> 枚举指定了模态类型及其关联的范围。
     *
     * @see Dialog#getModalityType
     * @see Dialog#setModalityType
     * @see Toolkit#isModalityTypeSupported
     *
     * @since 1.6
     */
    public static enum ModalityType {
        /**
         * <code>MODELESS</code> 对话框不阻止任何顶级窗口。
         */
        MODELESS,
        /**
         * <code>DOCUMENT_MODAL</code> 对话框阻止来自同一文档的所有顶级窗口的输入，但不阻止其自身子层次结构中的窗口。
         * 文档是没有所有者的顶级窗口。它可以包含子窗口，这些子窗口与顶级窗口一起被视为一个单一的实体文档。由于每个顶级窗口都必须属于某个文档，因此可以通过查找最近的没有所有者的顶级窗口来找到其根。
         */
        DOCUMENT_MODAL,
        /**
         * <code>APPLICATION_MODAL</code> 对话框阻止来自同一 Java 应用程序的所有顶级窗口的输入，但不阻止其自身子层次结构中的窗口。
         * 如果浏览器中启动了多个 applet，它们可以被视为单独的应用程序或一个应用程序。这种行为是实现依赖的。
         */
        APPLICATION_MODAL,
        /**
         * <code>TOOLKIT_MODAL</code> 对话框阻止来自同一工具包的所有顶级窗口的输入，但不阻止其自身子层次结构中的窗口。如果浏览器中启动了多个 applet，它们都使用相同的工具包；因此，由 applet 显示的工具包模式对话框可能会影响其他 applet 和嵌入此工具包的 Java 运行时环境的浏览器实例中的所有窗口。
         * 使用工具包模式对话框需要特殊的 <code>AWTPermission</code> "toolkitModality"。如果创建 <code>TOOLKIT_MODAL</code> 对话框时未授予此权限，将抛出 <code>SecurityException</code>，并且不会创建对话框。如果将模态类型更改为 <code>TOOLKIT_MODAL</code> 且未授予此权限，将抛出 <code>SecurityException</code>，并且模态类型将保持不变。
         */
        TOOLKIT_MODAL
    };

    /**
     * 模式对话框的默认模态类型。默认模态类型为 <code>APPLICATION_MODAL</code>。调用旧式的 <code>setModal(true)</code> 等同于 <code>setModalityType(DEFAULT_MODALITY_TYPE)</code>。
     *
     * @see java.awt.Dialog.ModalityType
     * @see java.awt.Dialog#setModal
     *
     * @since 1.6
     */
    public final static ModalityType DEFAULT_MODALITY_TYPE = ModalityType.APPLICATION_MODAL;

    /**
     * 如果此对话框是模式的，则为 true，否则为 false。模式对话框阻止用户输入某些应用程序顶级窗口。此字段仅用于向后兼容。请改用 <code>Dialog.ModalityType</code> 枚举。
     *
     * @serial
     *
     * @see #isModal
     * @see #setModal
     * @see #getModalityType
     * @see #setModalityType
     * @see ModalityType
     * @see ModalityType#MODELESS
     * @see #DEFAULT_MODALITY_TYPE
     */
    boolean modal;

    /**
     * 此对话框的模态类型。如果对话框的模态类型不是 {@link Dialog.ModalityType#MODELESS ModalityType.MODELESS}，则它会阻止某些应用程序顶级窗口的所有用户输入。
     *
     * @serial
     *
     * @see ModalityType
     * @see #getModalityType
     * @see #setModalityType
     *
     * @since 1.6
     */
    ModalityType modalityType;

    /**
     * 任何顶级窗口都可以标记为不被模式对话框阻止。这称为“模式排除”。此枚举指定了可能的模式排除类型。
     *
     * @see Window#getModalExclusionType
     * @see Window#setModalExclusionType
     * @see Toolkit#isModalExclusionTypeSupported
     *
     * @since 1.6
     */
    public static enum ModalExclusionType {
        /**
         * 无模式排除。
         */
        NO_EXCLUDE,
        /**
         * <code>APPLICATION_EXCLUDE</code> 表示顶级窗口不会被任何应用程序模式对话框阻止。此外，它也不会被其子层次结构之外的文档模式对话框阻止。
         */
        APPLICATION_EXCLUDE,
        /**
         * <code>TOOLKIT_EXCLUDE</code> 表示顶级窗口不会被应用程序模式或工具包模式对话框阻止。此外，它也不会被其子层次结构之外的文档模式对话框阻止。
         * 需要授予“toolkitModality” <code>AWTPermission</code>。如果将排除属性更改为 <code>TOOLKIT_EXCLUDE</code> 且未授予此权限，将抛出 <code>SecurityException</code>，并且排除属性将保持不变。
         */
        TOOLKIT_EXCLUDE
    };

    /* 对此列表的操作应同步在树锁上 */
    transient static IdentityArrayList<Dialog> modalDialogs = new IdentityArrayList<Dialog>();

    transient IdentityArrayList<Window> blockedWindows = new IdentityArrayList<Window>();

    /**
     * 指定对话框的标题。此字段可以为 null。
     *
     * @serial
     * @see #getTitle()
     * @see #setTitle(String)
     */
    String title;

    private transient ModalEventFilter modalFilter;
    private transient volatile SecondaryLoop secondaryLoop;

    /*
     * 指示此对话框正在被隐藏。此标志在 hide() 开始时设置为 true，在 hide() 结束时设置为 false。
     *
     * @see #hide()
     * @see #hideAndDisposePreHandler()
     * @see #hideAndDisposeHandler()
     * @see #shouldBlock()
     */
    transient volatile boolean isInHide = false;

    /*
     * 指示此对话框正在被销毁。此标志在 doDispose() 开始时设置为 true，在 doDispose() 结束时设置为 false。
     *
     * @see #hide()
     * @see #hideAndDisposePreHandler()
     * @see #hideAndDisposeHandler()
     * @see #doDispose()
     */
    transient volatile boolean isInDispose = false;

    private static final String base = "dialog";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 序列化版本 ID
     */
    private static final long serialVersionUID = 5920926903803293709L;

    /**
     * 构造一个初始不可见、无模式的 <code>Dialog</code>，指定所有者 <code>Frame</code> 和空标题。
     *
     * @param owner 对话框的所有者，或 <code>null</code> 表示此对话框没有所有者
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 的 <code>GraphicsConfiguration</code> 不来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see Component#setSize
     * @see Component#setVisible
     */
     public Dialog(Frame owner) {
         this(owner, "", false);
     }

    /**
     * 构造一个初始不可见的 <code>Dialog</code>，指定所有者 <code>Frame</code> 和模态性，以及空标题。
     *
     * @param owner 对话框的所有者，或 <code>null</code> 表示此对话框没有所有者
     * @param modal 指定对话框在显示时是否阻止用户输入其他顶级窗口。如果为 <code>false</code>，则对话框为 <code>MODELESS</code>；如果为 <code>true</code>，则模态类型属性设置为 <code>DEFAULT_MODALITY_TYPE</code>
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 的 <code>GraphicsConfiguration</code> 不来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     *
     * @see java.awt.Dialog.ModalityType
     * @see java.awt.Dialog.ModalityType#MODELESS
     * @see java.awt.Dialog#DEFAULT_MODALITY_TYPE
     * @see java.awt.Dialog#setModal
     * @see java.awt.Dialog#setModalityType
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
     public Dialog(Frame owner, boolean modal) {
         this(owner, "", modal);
     }

    /**
     * 构造一个初始不可见、无模式的 <code>Dialog</code>，指定所有者 <code>Frame</code> 和标题。
     *
     * @param owner 对话框的所有者，或 <code>null</code> 表示此对话框没有所有者
     * @param title 对话框的标题，或 <code>null</code> 表示此对话框没有标题
     * @exception IllegalArgumentException 如果 <code>owner</code> 的 <code>GraphicsConfiguration</code> 不来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see Component#setSize
     * @see Component#setVisible
     */
     public Dialog(Frame owner, String title) {
         this(owner, title, false);
     }

    /**
     * 构造一个初始不可见的 <code>Dialog</code>，指定所有者 <code>Frame</code>、标题和模态性。
     *
     * @param owner 对话框的所有者，或 <code>null</code> 表示此对话框没有所有者
     * @param title 对话框的标题，或 <code>null</code> 表示此对话框没有标题
     * @param modal 指定对话框在显示时是否阻止用户输入其他顶级窗口。如果为 <code>false</code>，则对话框为 <code>MODELESS</code>；如果为 <code>true</code>，则模态类型属性设置为 <code>DEFAULT_MODALITY_TYPE</code>
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 的 <code>GraphicsConfiguration</code> 不来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     *
     * @see java.awt.Dialog.ModalityType
     * @see java.awt.Dialog.ModalityType#MODELESS
     * @see java.awt.Dialog#DEFAULT_MODALITY_TYPE
     * @see java.awt.Dialog#setModal
     * @see java.awt.Dialog#setModalityType
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see Component#setSize
     * @see Component#setVisible
     */
     public Dialog(Frame owner, String title, boolean modal) {
         this(owner, title, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
     }


                /**
     * 构造一个初始不可见的 <code>Dialog</code>，具有指定的所有者 <code>Frame</code>、标题、模态性和 <code>GraphicsConfiguration</code>。
     * @param owner 对话框的所有者，或如果此对话框没有所有者则为 <code>null</code>
     * @param title 对话框的标题，或如果此对话框没有标题则为 <code>null</code>
     * @param modal 指定对话框显示时是否阻止用户输入到其他顶级窗口。如果为 <code>false</code>，对话框为 <code>MODELESS</code>；
     *     如果为 <code>true</code>，模态类型属性设置为 <code>DEFAULT_MODALITY_TYPE</code>
     * @param gc 目标屏幕设备的 <code>GraphicsConfiguration</code>；如果为 <code>null</code>，则假定为默认系统 <code>GraphicsConfiguration</code>
     * @exception java.lang.IllegalArgumentException 如果 <code>gc</code> 不是来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     *
     * @see java.awt.Dialog.ModalityType
     * @see java.awt.Dialog.ModalityType#MODELESS
     * @see java.awt.Dialog#DEFAULT_MODALITY_TYPE
     * @see java.awt.Dialog#setModal
     * @see java.awt.Dialog#setModalityType
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see Component#setSize
     * @see Component#setVisible
     * @since 1.4
     */
     public Dialog(Frame owner, String title, boolean modal,
                   GraphicsConfiguration gc) {
         this(owner, title, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS, gc);
     }

    /**
     * 构造一个初始不可见的无模态 <code>Dialog</code>，具有指定的所有者 <code>Dialog</code> 和空标题。
     *
     * @param owner 对话框的所有者，或如果此对话框没有所有者则为 <code>null</code>
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 的 <code>GraphicsConfiguration</code> 不是来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since 1.2
     */
     public Dialog(Dialog owner) {
         this(owner, "", false);
     }

    /**
     * 构造一个初始不可见的无模态 <code>Dialog</code>，具有指定的所有者 <code>Dialog</code> 和标题。
     *
     * @param owner 对话框的所有者，或如果此对话框没有所有者则为 <code>null</code>
     * @param title 对话框的标题，或如果此对话框没有标题则为 <code>null</code>
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 的 <code>GraphicsConfiguration</code> 不是来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since 1.2
     */
     public Dialog(Dialog owner, String title) {
         this(owner, title, false);
     }

    /**
     * 构造一个初始不可见的 <code>Dialog</code>，具有指定的所有者 <code>Dialog</code>、标题和模态性。
     *
     * @param owner 对话框的所有者，或如果此对话框没有所有者则为 <code>null</code>
     * @param title 对话框的标题，或如果此对话框没有标题则为 <code>null</code>
     * @param modal 指定对话框显示时是否阻止用户输入到其他顶级窗口。如果为 <code>false</code>，对话框为 <code>MODELESS</code>；
     *     如果为 <code>true</code>，模态类型属性设置为 <code>DEFAULT_MODALITY_TYPE</code>
     * @exception IllegalArgumentException 如果 <code>owner</code> 的 <code>GraphicsConfiguration</code> 不是来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     *
     * @see java.awt.Dialog.ModalityType
     * @see java.awt.Dialog.ModalityType#MODELESS
     * @see java.awt.Dialog#DEFAULT_MODALITY_TYPE
     * @see java.awt.Dialog#setModal
     * @see java.awt.Dialog#setModalityType
     * @see java.awt.GraphicsEnvironment#isHeadless
     *
     * @since 1.2
     */
     public Dialog(Dialog owner, String title, boolean modal) {
         this(owner, title, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
     }

    /**
     * 构造一个初始不可见的 <code>Dialog</code>，具有指定的所有者 <code>Dialog</code>、标题、模态性和 <code>GraphicsConfiguration</code>。
     *
     * @param owner 对话框的所有者，或如果此对话框没有所有者则为 <code>null</code>
     * @param title 对话框的标题，或如果此对话框没有标题则为 <code>null</code>
     * @param modal 指定对话框显示时是否阻止用户输入到其他顶级窗口。如果为 <code>false</code>，对话框为 <code>MODELESS</code>；
     *     如果为 <code>true</code>，模态类型属性设置为 <code>DEFAULT_MODALITY_TYPE</code>
     * @param gc 目标屏幕设备的 <code>GraphicsConfiguration</code>；如果为 <code>null</code>，则假定为默认系统 <code>GraphicsConfiguration</code>
     * @exception java.lang.IllegalArgumentException 如果 <code>gc</code> 不是来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     *
     * @see java.awt.Dialog.ModalityType
     * @see java.awt.Dialog.ModalityType#MODELESS
     * @see java.awt.Dialog#DEFAULT_MODALITY_TYPE
     * @see java.awt.Dialog#setModal
     * @see java.awt.Dialog#setModalityType
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see Component#setSize
     * @see Component#setVisible
     *
     * @since 1.4
     */
     public Dialog(Dialog owner, String title, boolean modal,
                   GraphicsConfiguration gc) {
         this(owner, title, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS, gc);
     }

    /**
     * 构造一个初始不可见的无模态 <code>Dialog</code>，具有指定的所有者 <code>Window</code> 和空标题。
     *
     * @param owner 对话框的所有者。所有者必须是 {@link java.awt.Dialog Dialog}、{@link java.awt.Frame Frame} 的实例，
     *     或其任何后代，或 <code>null</code>
     *
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 不是 {@link java.awt.Dialog Dialog} 或 {@link
     *     java.awt.Frame Frame} 的实例
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 的 <code>GraphicsConfiguration</code> 不是来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     *
     * @since 1.6
     */
    public Dialog(Window owner) {
        this(owner, "", ModalityType.MODELESS);
    }

    /**
     * 构造一个初始不可见的无模态 <code>Dialog</code>，具有指定的所有者 <code>Window</code> 和标题。
     *
     * @param owner 对话框的所有者。所有者必须是 {@link java.awt.Dialog Dialog}、{@link java.awt.Frame Frame} 的实例，
     *    或其任何后代，或 <code>null</code>
     * @param title 对话框的标题，或如果此对话框没有标题则为 <code>null</code>
     *
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 不是 {@link java.awt.Dialog Dialog} 或 {@link
     *    java.awt.Frame Frame} 的实例
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 的 <code>GraphicsConfiguration</code> 不是来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     *
     * @see java.awt.GraphicsEnvironment#isHeadless
     *
     * @since 1.6
     */
    public Dialog(Window owner, String title) {
        this(owner, title, ModalityType.MODELESS);
    }

    /**
     * 构造一个初始不可见的 <code>Dialog</code>，具有指定的所有者 <code>Window</code> 和模态性，以及空标题。
     *
     * @param owner 对话框的所有者。所有者必须是 {@link java.awt.Dialog Dialog}、{@link java.awt.Frame Frame} 的实例，
     *    或其任何后代，或 <code>null</code>
     * @param modalityType 指定对话框显示时是否阻止输入到其他窗口。<code>null</code> 值和不受支持的模态类型等同于 <code>MODELESS</code>
     *
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 不是 {@link java.awt.Dialog Dialog} 或 {@link
     *    java.awt.Frame Frame} 的实例
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 的 <code>GraphicsConfiguration</code> 不是来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     * @exception SecurityException 如果调用线程没有权限使用给定的 <code>modalityType</code> 创建模态对话框
     *
     * @see java.awt.Dialog.ModalityType
     * @see java.awt.Dialog#setModal
     * @see java.awt.Dialog#setModalityType
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see java.awt.Toolkit#isModalityTypeSupported
     *
     * @since 1.6
     */
    public Dialog(Window owner, ModalityType modalityType) {
        this(owner, "", modalityType);
    }

    /**
     * 构造一个初始不可见的 <code>Dialog</code>，具有指定的所有者 <code>Window</code>、标题和模态性。
     *
     * @param owner 对话框的所有者。所有者必须是 {@link java.awt.Dialog Dialog}、{@link java.awt.Frame Frame} 的实例，
     *     或其任何后代，或 <code>null</code>
     * @param title 对话框的标题，或如果此对话框没有标题则为 <code>null</code>
     * @param modalityType 指定对话框显示时是否阻止输入到其他窗口。<code>null</code> 值和不受支持的模态类型等同于 <code>MODELESS</code>
     *
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 不是 {@link java.awt.Dialog Dialog} 或 {@link
     *     java.awt.Frame Frame} 的实例
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 的 <code>GraphicsConfiguration</code> 不是来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     * @exception SecurityException 如果调用线程没有权限使用给定的 <code>modalityType</code> 创建模态对话框
     *
     * @see java.awt.Dialog.ModalityType
     * @see java.awt.Dialog#setModal
     * @see java.awt.Dialog#setModalityType
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see java.awt.Toolkit#isModalityTypeSupported
     *
     * @since 1.6
     */
    public Dialog(Window owner, String title, ModalityType modalityType) {
        super(owner);

        if ((owner != null) &&
            !(owner instanceof Frame) &&
            !(owner instanceof Dialog))
        {
            throw new IllegalArgumentException("错误的父窗口");
        }

        this.title = title;
        setModalityType(modalityType);
        SunToolkit.checkAndSetPolicy(this);
        initialized = true;
    }

    /**
     * 构造一个初始不可见的 <code>Dialog</code>，具有指定的所有者 <code>Window</code>、标题、模态性和 <code>GraphicsConfiguration</code>。
     *
     * @param owner 对话框的所有者。所有者必须是 {@link java.awt.Dialog Dialog}、{@link java.awt.Frame Frame} 的实例，
     *     或其任何后代，或 <code>null</code>
     * @param title 对话框的标题，或如果此对话框没有标题则为 <code>null</code>
     * @param modalityType 指定对话框显示时是否阻止输入到其他窗口。<code>null</code> 值和不受支持的模态类型等同于 <code>MODELESS</code>
     * @param gc 目标屏幕设备的 <code>GraphicsConfiguration</code>；如果为 <code>null</code>，则假定为默认系统 <code>GraphicsConfiguration</code>
     *
     * @exception java.lang.IllegalArgumentException 如果 <code>owner</code> 不是 {@link java.awt.Dialog Dialog} 或 {@link
     *     java.awt.Frame Frame} 的实例
     * @exception java.lang.IllegalArgumentException 如果 <code>gc</code> 不是来自屏幕设备
     * @exception HeadlessException 当 <code>GraphicsEnvironment.isHeadless()</code> 返回 <code>true</code> 时
     * @exception SecurityException 如果调用线程没有权限使用给定的 <code>modalityType</code> 创建模态对话框
     *
     * @see java.awt.Dialog.ModalityType
     * @see java.awt.Dialog#setModal
     * @see java.awt.Dialog#setModalityType
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see java.awt.Toolkit#isModalityTypeSupported
     *
     * @since 1.6
     */
    public Dialog(Window owner, String title, ModalityType modalityType,
                  GraphicsConfiguration gc) {
        super(owner, gc);

        if ((owner != null) &&
            !(owner instanceof Frame) &&
            !(owner instanceof Dialog))
        {
            throw new IllegalArgumentException("错误的父窗口");
        }

        this.title = title;
        setModalityType(modalityType);
        SunToolkit.checkAndSetPolicy(this);
        initialized = true;
    }

    /**
     * 为该组件构造一个名称。当名称为 null 时，由 getName() 调用。
     */
    String constructComponentName() {
        synchronized (Dialog.class) {
            return base + nameCounter++;
        }
    }

    /**
     * 通过连接到本机屏幕资源使此对话框可显示。使对话框可显示将导致其任何子组件也被设置为可显示。
     * 此方法由工具包内部调用，不应由程序直接调用。
     * @see Component#isDisplayable
     * @see #removeNotify
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (parent != null && parent.getPeer() == null) {
                parent.addNotify();
            }

            if (peer == null) {
                peer = getToolkit().createDialog(this);
            }
            super.addNotify();
        }
    }

    /**
     * 指示对话框是否为模态。
     * <p>
     * 此方法已过时，仅为了向后兼容而保留。使用 {@link #getModalityType getModalityType()} 代替。
     *
     * @return    如果此对话框窗口为模态，则返回 <code>true</code>；否则返回 <code>false</code>
     *
     * @see       java.awt.Dialog#DEFAULT_MODALITY_TYPE
     * @see       java.awt.Dialog.ModalityType#MODELESS
     * @see       java.awt.Dialog#setModal
     * @see       java.awt.Dialog#getModalityType
     * @see       java.awt.Dialog#setModalityType
     */
    public boolean isModal() {
        return isModal_NoClientCode();
    }
    final boolean isModal_NoClientCode() {
        return modalityType != ModalityType.MODELESS;
    }


                /**
     * 指定此对话框是否为模态。
     * <p>
     * 此方法已过时，仅保留以保持向后兼容性。
     * 请改用 {@link #setModalityType setModalityType()}。
     * <p>
     * 注意：更改可见对话框的模态性可能不会产生效果，直到它被隐藏并再次显示。
     *
     * @param modal 指定对话框显示时是否阻止输入到其他窗口；
     *     调用 <code>setModal(true)</code> 等同于
     *     <code>setModalityType(Dialog.DEFAULT_MODALITY_TYPE)</code>，而
     *     调用 <code>setModal(false)</code> 等同于
     *     <code>setModalityType(Dialog.ModalityType.MODELESS)</code>
     *
     * @see       java.awt.Dialog#DEFAULT_MODALITY_TYPE
     * @see       java.awt.Dialog.ModalityType#MODELESS
     * @see       java.awt.Dialog#isModal
     * @see       java.awt.Dialog#getModalityType
     * @see       java.awt.Dialog#setModalityType
     *
     * @since     1.1
     */
    public void setModal(boolean modal) {
        this.modal = modal;
        setModalityType(modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
    }

    /**
     * 返回此对话框的模态类型。
     *
     * @return 此对话框的模态类型
     *
     * @see java.awt.Dialog#setModalityType
     *
     * @since 1.6
     */
    public ModalityType getModalityType() {
        return modalityType;
    }

    /**
     * 设置此对话框的模态类型。有关可能的模态类型，请参见 {@link
     * java.awt.Dialog.ModalityType ModalityType}。
     * <p>
     * 如果给定的模态类型不受支持，则使用 <code>MODELESS</code>。
     * 您可能希望在调用此方法后调用 <code>getModalityType()</code> 以确保模态类型已设置。
     * <p>
     * 注意：更改可见对话框的模态性可能不会产生效果，直到它被隐藏并再次显示。
     *
     * @param type 指定对话框显示时是否阻止输入到其他
     *     窗口。<code>null</code> 值和不受支持的模态类型等同于 <code>MODELESS</code>
     * @exception SecurityException 如果调用线程没有权限
     *     创建具有给定 <code>modalityType</code> 的模态对话框
     *
     * @see       java.awt.Dialog#getModalityType
     * @see       java.awt.Toolkit#isModalityTypeSupported
     *
     * @since     1.6
     */
    public void setModalityType(ModalityType type) {
        if (type == null) {
            type = Dialog.ModalityType.MODELESS;
        }
        if (!Toolkit.getDefaultToolkit().isModalityTypeSupported(type)) {
            type = Dialog.ModalityType.MODELESS;
        }
        if (modalityType == type) {
            return;
        }

        checkModalityPermission(type);

        modalityType = type;
        modal = (modalityType != ModalityType.MODELESS);
    }

    /**
     * 获取对话框的标题。标题显示在对话框的边框中。
     * @return    此对话框窗口的标题。标题可以为
     *            <code>null</code>。
     * @see       java.awt.Dialog#setTitle
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置对话框的标题。
     * @param title 显示在对话框边框中的标题；
     * a null value results in an empty title
     * @see #getTitle
     */
    public void setTitle(String title) {
        String oldTitle = this.title;

        synchronized(this) {
            this.title = title;
            DialogPeer peer = (DialogPeer)this.peer;
            if (peer != null) {
                peer.setTitle(title);
            }
        }
        firePropertyChange("title", oldTitle, title);
    }

    /**
     * @return true if we actually showed, false if we just called toFront()
     */
    private boolean conditionalShow(Component toFocus, AtomicLong time) {
        boolean retval;

        closeSplashScreen();

        synchronized (getTreeLock()) {
            if (peer == null) {
                addNotify();
            }
            validateUnconditionally();
            if (visible) {
                toFront();
                retval = false;
            } else {
                visible = retval = true;

                // check if this dialog should be modal blocked BEFORE calling peer.show(),
                // otherwise, a pair of FOCUS_GAINED and FOCUS_LOST may be mistakenly
                // generated for the dialog
                if (!isModal()) {
                    checkShouldBeBlocked(this);
                } else {
                    modalDialogs.add(this);
                    modalShow();
                }

                if (toFocus != null && time != null && isFocusable() &&
                    isEnabled() && !isModalBlocked()) {
                    // keep the KeyEvents from being dispatched
                    // until the focus has been transfered
                    time.set(Toolkit.getEventQueue().getMostRecentKeyEventTime());
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().
                        enqueueKeyEvents(time.get(), toFocus);
                }

                // This call is required as the show() method of the Dialog class
                // does not invoke the super.show(). So wried... :(
                mixOnShowing();

                peer.setVisible(true); // now guaranteed never to block
                if (isModalBlocked()) {
                    modalBlocker.toFront();
                }

                setLocationByPlatform(false);
                for (int i = 0; i < ownedWindowList.size(); i++) {
                    Window child = ownedWindowList.elementAt(i).get();
                    if ((child != null) && child.showWithParent) {
                        child.show();
                        child.showWithParent = false;
                    }       // endif
                }   // endfor
                Window.updateChildFocusableWindowState(this);

                createHierarchyEvents(HierarchyEvent.HIERARCHY_CHANGED,
                                      this, parent,
                                      HierarchyEvent.SHOWING_CHANGED,
                                      Toolkit.enabledOnToolkit(AWTEvent.HIERARCHY_EVENT_MASK));
                if (componentListener != null ||
                        (eventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0 ||
                        Toolkit.enabledOnToolkit(AWTEvent.COMPONENT_EVENT_MASK)) {
                    ComponentEvent e =
                        new ComponentEvent(this, ComponentEvent.COMPONENT_SHOWN);
                    Toolkit.getEventQueue().postEvent(e);
                }
            }
        }

        if (retval && (state & OPENED) == 0) {
            postWindowEvent(WindowEvent.WINDOW_OPENED);
            state |= OPENED;
        }

        return retval;
    }

    /**
     * 根据参数 {@code b} 显示或隐藏此 {@code Dialog}。
     * @param b 如果 {@code true}，使 {@code Dialog} 可见，
     * 否则隐藏 {@code Dialog}。
     * 如果对话框和/或其所有者
     * 尚未可显示，则两者都将被设为可显示。对话框将在被设为可见之前进行验证。
     * 如果 {@code false}，隐藏 {@code Dialog} 并在当前被阻塞时使 {@code setVisible(true)}
     * 返回。
     * <p>
     * <b>模态对话框的注意事项</b>。
     * <ul>
     * <li>{@code setVisible(true)}:  如果对话框尚未可见，此调用将不会返回，直到对话框被
     * 调用 {@code setVisible(false)} 或 {@code dispose} 隐藏。
     * <li>{@code setVisible(false)}:  隐藏对话框并在当前被阻塞时返回 {@code setVisible(true)}。
     * <li>从事件调度线程调用此方法是安全的，因为工具包确保其他事件在该方法被阻塞时不会被阻塞。
     * </ul>
     * @see java.awt.Window#setVisible
     * @see java.awt.Window#dispose
     * @see java.awt.Component#isDisplayable
     * @see java.awt.Component#validate
     * @see java.awt.Dialog#isModal
     */
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

   /**
     * 使 {@code Dialog} 可见。如果对话框和/或其所有者
     * 尚未可显示，则两者都将被设为可显示。对话框将在被设为可见之前进行验证。
     * 如果对话框已可见，这将使对话框移到前面。
     * <p>
     * 如果对话框为模态且尚未可见，此调用将不会返回，直到对话框被调用 hide 或
     * dispose 隐藏。从事件调度线程显示模态对话框是允许的，因为工具包将确保另一个
     * 事件泵在调用此方法的事件泵被阻塞时运行。
     * @see Component#hide
     * @see Component#isDisplayable
     * @see Component#validate
     * @see #isModal
     * @see Window#setVisible(boolean)
     * @deprecated 自 JDK 1.5 版起，被
     * {@link #setVisible(boolean) setVisible(boolean)} 取代。
     */
    @Deprecated
    public void show() {
        if (!initialized) {
            throw new IllegalStateException("The dialog component " +
                "has not been initialized properly");
        }

        beforeFirstShow = false;
        if (!isModal()) {
            conditionalShow(null, null);
        } else {
            AppContext showAppContext = AppContext.getAppContext();

            AtomicLong time = new AtomicLong();
            Component predictedFocusOwner = null;
            try {
                predictedFocusOwner = getMostRecentFocusOwner();
                if (conditionalShow(predictedFocusOwner, time)) {
                    modalFilter = ModalEventFilter.createFilterForDialog(this);
                    final Conditional cond = new Conditional() {
                        @Override
                        public boolean evaluate() {
                            return windowClosingException == null;
                        }
                    };

                    // if this dialog is toolkit-modal, the filter should be added
                    // to all EDTs (for all AppContexts)
                    if (modalityType == ModalityType.TOOLKIT_MODAL) {
                        Iterator<AppContext> it = AppContext.getAppContexts().iterator();
                        while (it.hasNext()) {
                            AppContext appContext = it.next();
                            if (appContext == showAppContext) {
                                continue;
                            }
                            EventQueue eventQueue = (EventQueue)appContext.get(AppContext.EVENT_QUEUE_KEY);
                            // it may occur that EDT for appContext hasn't been started yet, so
                            // we post an empty invocation event to trigger EDT initialization
                            Runnable createEDT = new Runnable() {
                                public void run() {};
                            };
                            eventQueue.postEvent(new InvocationEvent(this, createEDT));
                            EventDispatchThread edt = eventQueue.getDispatchThread();
                            edt.addEventFilter(modalFilter);
                        }
                    }

                    modalityPushed();
                    try {
                        final EventQueue eventQueue = AccessController.doPrivileged(
                            new PrivilegedAction<EventQueue>() {
                                public EventQueue run() {
                                    return Toolkit.getDefaultToolkit().getSystemEventQueue();
                                }
                        });
                        secondaryLoop = eventQueue.createSecondaryLoop(cond, modalFilter, 0);
                        if (!secondaryLoop.enter()) {
                            secondaryLoop = null;
                        }
                    } finally {
                        modalityPopped();
                    }

                    // if this dialog is toolkit-modal, its filter must be removed
                    // from all EDTs (for all AppContexts)
                    if (modalityType == ModalityType.TOOLKIT_MODAL) {
                        Iterator<AppContext> it = AppContext.getAppContexts().iterator();
                        while (it.hasNext()) {
                            AppContext appContext = it.next();
                            if (appContext == showAppContext) {
                                continue;
                            }
                            EventQueue eventQueue = (EventQueue)appContext.get(AppContext.EVENT_QUEUE_KEY);
                            EventDispatchThread edt = eventQueue.getDispatchThread();
                            edt.removeEventFilter(modalFilter);
                        }
                    }

                    if (windowClosingException != null) {
                        windowClosingException.fillInStackTrace();
                        throw windowClosingException;
                    }
                }
            } finally {
                if (predictedFocusOwner != null) {
                    // Restore normal key event dispatching
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().
                        dequeueKeyEvents(time.get(), predictedFocusOwner);
                }
            }
        }
    }

    final void modalityPushed() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk instanceof SunToolkit) {
            SunToolkit stk = (SunToolkit)tk;
            stk.notifyModalityPushed(this);
        }
    }

    final void modalityPopped() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk instanceof SunToolkit) {
            SunToolkit stk = (SunToolkit)tk;
            stk.notifyModalityPopped(this);
        }
    }

    void interruptBlocking() {
        if (isModal()) {
            disposeImpl();
        } else if (windowClosingException != null) {
            windowClosingException.fillInStackTrace();
            windowClosingException.printStackTrace();
            windowClosingException = null;
        }
    }

    private void hideAndDisposePreHandler() {
        isInHide = true;
        synchronized (getTreeLock()) {
            if (secondaryLoop != null) {
                modalHide();
                // dialog can be shown and then disposed before its
                // modal filter is created
                if (modalFilter != null) {
                    modalFilter.disable();
                }
                modalDialogs.remove(this);
            }
        }
    }
    private void hideAndDisposeHandler() {
        if (secondaryLoop != null) {
            secondaryLoop.exit();
            secondaryLoop = null;
        }
        isInHide = false;
    }

    /**
     * 隐藏对话框并在当前被阻塞时使 {@code show} 返回。
     * @see Window#show
     * @see Window#dispose
     * @see Window#setVisible(boolean)
     * @deprecated 自 JDK 1.5 版起，被
     * {@link #setVisible(boolean) setVisible(boolean)} 取代。
     */
    @Deprecated
    public void hide() {
        hideAndDisposePreHandler();
        super.hide();
        // fix for 5048370: if hide() is called from super.doDispose(), then
        // hideAndDisposeHandler() should not be called here as it will be called
        // at the end of doDispose()
        if (!isInDispose) {
            hideAndDisposeHandler();
        }
    }


                /**
     * 释放对话框，然后如果 show() 当前被阻塞，则使其返回。
     */
    void doDispose() {
        // 修复 5048370：将 isInDispose 标志设置为 true 以防止从 hide() 调用
        // hideAndDisposeHandler()
        isInDispose = true;
        super.doDispose();
        hideAndDisposeHandler();
        isInDispose = false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * 如果此对话框是模态的并且阻止了一些窗口，那么所有这些窗口
     * 也会被发送到后面，以保持它们在阻止对话框之下。
     *
     * @see java.awt.Window#toBack
     */
    public void toBack() {
        super.toBack();
        if (visible) {
            synchronized (getTreeLock()) {
                for (Window w : blockedWindows) {
                    w.toBack_NoClientCode();
                }
            }
        }
    }

    /**
     * 指示用户是否可以调整此对话框的大小。
     * 默认情况下，所有对话框最初都是可调整大小的。
     * @return    <code>true</code> 如果用户可以调整对话框的大小；
     *            <code>false</code> 否则。
     * @see       java.awt.Dialog#setResizable
     */
    public boolean isResizable() {
        return resizable;
    }

    /**
     * 设置用户是否可以调整此对话框的大小。
     * @param     resizable <code>true</code> 如果用户可以
     *                 调整此对话框的大小；<code>false</code> 否则。
     * @see       java.awt.Dialog#isResizable
     */
    public void setResizable(boolean resizable) {
        boolean testvalid = false;

        synchronized (this) {
            this.resizable = resizable;
            DialogPeer peer = (DialogPeer)this.peer;
            if (peer != null) {
                peer.setResizable(resizable);
                testvalid = true;
            }
        }

        // 在某些平台上，更改可调整大小状态会影响
        // 对话框的内边距。如果可以，我们会在对等体中调用 invalidate()
        // 但我们需要保证在调用 invalidate() 时不持有
        // 对话框锁。
        if (testvalid) {
            invalidateIfValid();
        }
    }


    /**
     * 为该对话框启用或禁用装饰。
     * <p>
     * 只能在对话框不可显示时调用此方法。要使此对话框具有装饰，它必须是不透明的并且具有默认形状，
     * 否则将抛出 {@code IllegalComponentStateException}。
     * 请参阅 {@link Window#setShape}，{@link Window#setOpacity} 和 {@link
     * Window#setBackground} 以获取详细信息
     *
     * @param  undecorated {@code true} 如果不启用对话框装饰； {@code false} 如果启用对话框装饰
     *
     * @throws IllegalComponentStateException 如果对话框可显示
     * @throws IllegalComponentStateException 如果 {@code undecorated} 为
     *      {@code false}，且此对话框没有默认形状
     * @throws IllegalComponentStateException 如果 {@code undecorated} 为
     *      {@code false}，且此对话框的不透明度小于 {@code 1.0f}
     * @throws IllegalComponentStateException 如果 {@code undecorated} 为
     *      {@code false}，且此对话框背景颜色的 alpha 值小于 {@code 1.0f}
     *
     * @see    #isUndecorated
     * @see    Component#isDisplayable
     * @see    Window#getShape
     * @see    Window#getOpacity
     * @see    Window#getBackground
     *
     * @since 1.4
     */
    public void setUndecorated(boolean undecorated) {
        /* 确保不会在对等体创建过程中运行。*/
        synchronized (getTreeLock()) {
            if (isDisplayable()) {
                throw new IllegalComponentStateException("对话框可显示。");
            }
            if (!undecorated) {
                if (getOpacity() < 1.0f) {
                    throw new IllegalComponentStateException("对话框不是不透明的");
                }
                if (getShape() != null) {
                    throw new IllegalComponentStateException("对话框没有默认形状");
                }
                Color bg = getBackground();
                if ((bg != null) && (bg.getAlpha() < 255)) {
                    throw new IllegalComponentStateException("对话框背景颜色不是不透明的");
                }
            }
            this.undecorated = undecorated;
        }
    }

    /**
     * 指示此对话框是否无装饰。
     * 默认情况下，所有对话框最初都是有装饰的。
     * @return    <code>true</code> 如果对话框无装饰；
     *                        <code>false</code> 否则。
     * @see       java.awt.Dialog#setUndecorated
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
                throw new IllegalComponentStateException("对话框有装饰");
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
                throw new IllegalComponentStateException("对话框有装饰");
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
                throw new IllegalComponentStateException("对话框有装饰");
            }
            super.setBackground(bgColor);
        }
    }

    /**
     * 返回表示此对话框状态的字符串。此方法仅用于调试目的，返回的字符串的内容和格式可能因实现而异。返回的字符串可以为空，但不能为
     * <code>null</code>。
     *
     * @return    此对话框窗口的参数字符串。
     */
    protected String paramString() {
        String str = super.paramString() + "," + modalityType;
        if (title != null) {
            str += ",title=" + title;
        }
        return str;
    }

    /**
     * 初始化 JNI 字段和方法 ID
     */
    private static native void initIDs();

    /*
     * --- 模态支持 ---
     *
     */

    /*
     * 仅用于模态对话框。
     *
     * 遍历所有可见顶级窗口的列表，并
     * 将它们分为三个不同的组：阻止此对话框的窗口，
     * 被此对话框阻止的窗口和所有其他窗口。然后通过第一个组中的第一个对话框（如果有）阻止此对话框
     * 并阻止第二组中的所有窗口。
     */
    void modalShow() {
        // 找出所有阻止此对话框的对话框
        IdentityArrayList<Dialog> blockers = new IdentityArrayList<Dialog>();
        for (Dialog d : modalDialogs) {
            if (d.shouldBlock(this)) {
                Window w = d;
                while ((w != null) && (w != this)) {
                    w = w.getOwner_NoClientCode();
                }
                if ((w == this) || !shouldBlock(d) || (modalityType.compareTo(d.getModalityType()) < 0)) {
                    blockers.add(d);
                }
            }
        }

        // 将所有阻止者的阻止者添加到阻止者中 :)
        for (int i = 0; i < blockers.size(); i++) {
            Dialog blocker = blockers.get(i);
            if (blocker.isModalBlocked()) {
                Dialog blockerBlocker = blocker.getModalBlocker();
                if (!blockers.contains(blockerBlocker)) {
                    blockers.add(i + 1, blockerBlocker);
                }
            }
        }

        if (blockers.size() > 0) {
            blockers.get(0).blockWindow(this);
        }

        // 找出所有阻止者的层次结构
        IdentityArrayList<Window> blockersHierarchies = new IdentityArrayList<Window>(blockers);
        int k = 0;
        while (k < blockersHierarchies.size()) {
            Window w = blockersHierarchies.get(k);
            Window[] ownedWindows = w.getOwnedWindows_NoClientCode();
            for (Window win : ownedWindows) {
                blockersHierarchies.add(win);
            }
            k++;
        }

        java.util.List<Window> toBlock = new IdentityLinkedList<Window>();
        // 阻止所有应被阻止的窗口，但不包括阻止者的层次结构
        IdentityArrayList<Window> unblockedWindows = Window.getAllUnblockedWindows();
        for (Window w : unblockedWindows) {
            if (shouldBlock(w) && !blockersHierarchies.contains(w)) {
                if ((w instanceof Dialog) && ((Dialog)w).isModal_NoClientCode()) {
                    Dialog wd = (Dialog)w;
                    if (wd.shouldBlock(this) && (modalDialogs.indexOf(wd) > modalDialogs.indexOf(this))) {
                        continue;
                    }
                }
                toBlock.add(w);
            }
        }
        blockWindows(toBlock);

        if (!isModalBlocked()) {
            updateChildrenBlocking();
        }
    }

    /*
     * 仅用于模态对话框。
     *
     * 解除所有被此模态对话框阻止的窗口的阻止。解除每个窗口的阻止后，
     * 检查它们是否应被其他模态对话框阻止。
     */
    void modalHide() {
        // 我们应该先解除所有窗口的阻止...
        IdentityArrayList<Window> save = new IdentityArrayList<Window>();
        int blockedWindowsCount = blockedWindows.size();
        for (int i = 0; i < blockedWindowsCount; i++) {
            Window w = blockedWindows.get(0);
            save.add(w);
            unblockWindow(w); // 同时从 blockedWindows 中移除 w
        }
        // ... 然后检查它们是否应被其他对话框阻止
        for (int i = 0; i < blockedWindowsCount; i++) {
            Window w = save.get(i);
            if ((w instanceof Dialog) && ((Dialog)w).isModal_NoClientCode()) {
                Dialog d = (Dialog)w;
                d.modalShow();
            } else {
                checkShouldBeBlocked(w);
            }
        }
    }

    /*
     * 返回给定顶级窗口是否应被此对话框阻止。
     * 注意，给定的窗口也可以是模态对话框
     * 并且它应该阻止此对话框，但此方法不考虑这种情况（此类检查在
     * modalShow() 和 modalHide() 方法中执行）。
     *
     * 此方法应在 getTreeLock() 锁上调用。
     */
    boolean shouldBlock(Window w) {
        if (!isVisible_NoClientCode() ||
            (!w.isVisible_NoClientCode() && !w.isInShow) ||
            isInHide ||
            (w == this) ||
            !isModal_NoClientCode())
        {
            return false;
        }
        if ((w instanceof Dialog) && ((Dialog)w).isInHide) {
            return false;
        }
        // 检查 w 是否属于子层次结构
        // 修复 6271546：我们还应考虑此对话框阻止者的子层次结构
        Window blockerToCheck = this;
        while (blockerToCheck != null) {
            Component c = w;
            while ((c != null) && (c != blockerToCheck)) {
                c = c.getParent_NoClientCode();
            }
            if (c == blockerToCheck) {
                return false;
            }
            blockerToCheck = blockerToCheck.getModalBlocker();
        }
        switch (modalityType) {
            case MODELESS:
                return false;
            case DOCUMENT_MODAL:
                if (w.isModalExcluded(ModalExclusionType.APPLICATION_EXCLUDE)) {
                    // 应用程序-和工具包-排除的窗口不会被
                    // 文档模态对话框从其子层次结构之外阻止
                    Component c = this;
                    while ((c != null) && (c != w)) {
                        c = c.getParent_NoClientCode();
                    }
                    return c == w;
                } else {
                    return getDocumentRoot() == w.getDocumentRoot();
                }
            case APPLICATION_MODAL:
                return !w.isModalExcluded(ModalExclusionType.APPLICATION_EXCLUDE) &&
                    (appContext == w.appContext);
            case TOOLKIT_MODAL:
                return !w.isModalExcluded(ModalExclusionType.TOOLKIT_EXCLUDE);
        }

        return false;
    }

    /*
     * 将给定的顶级窗口添加到此对话框的被阻止窗口列表中，并将其标记为模态阻止。
     * 如果窗口已被某些模态对话框阻止，
     * 则不执行任何操作。
     */
    void blockWindow(Window w) {
        if (!w.isModalBlocked()) {
            w.setModalBlocked(this, true, true);
            blockedWindows.add(w);
        }
    }

    void blockWindows(java.util.List<Window> toBlock) {
        DialogPeer dpeer = (DialogPeer)peer;
        if (dpeer == null) {
            return;
        }
        Iterator<Window> it = toBlock.iterator();
        while (it.hasNext()) {
            Window w = it.next();
            if (!w.isModalBlocked()) {
                w.setModalBlocked(this, true, false);
            } else {
                it.remove();
            }
        }
        dpeer.blockWindows(toBlock);
        blockedWindows.addAll(toBlock);
    }

    /*
     * 从此对话框的被阻止窗口列表中移除给定的顶级窗口，并将其标记为未被阻止。如果窗口
     * 未被模态阻止，则不执行任何操作。
     */
    void unblockWindow(Window w) {
        if (w.isModalBlocked() && blockedWindows.contains(w)) {
            blockedWindows.remove(w);
            w.setModalBlocked(this, false, true);
        }
    }

    /*
     * 检查是否有其他模态对话框 D 阻止给定的窗口。
     * 如果存在这样的 D，则将窗口标记为被 D 阻止。
     */
    static void checkShouldBeBlocked(Window w) {
        synchronized (w.getTreeLock()) {
            for (int i = 0; i < modalDialogs.size(); i++) {
                Dialog modalDialog = modalDialogs.get(i);
                if (modalDialog.shouldBlock(w)) {
                    modalDialog.blockWindow(w);
                    break;
                }
            }
        }
    }

    private void checkModalityPermission(ModalityType mt) {
        if (mt == ModalityType.TOOLKIT_MODAL) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(
                    SecurityConstants.AWT.TOOLKIT_MODALITY_PERMISSION
                );
            }
        }
    }

    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException, HeadlessException
    {
        GraphicsEnvironment.checkHeadless();

        java.io.ObjectInputStream.GetField fields =
            s.readFields();


                    ModalityType localModalityType = (ModalityType)fields.get("modalityType", null);

        try {
            checkModalityPermission(localModalityType);
        } catch (AccessControlException ace) {
            localModalityType = DEFAULT_MODALITY_TYPE;
        }

        // 在 1.5 或更早版本中，modalityType 不存在，因此使用 "modal" 代替
        if (localModalityType == null) {
            this.modal = fields.get("modal", false);
            setModal(modal);
        } else {
            this.modalityType = localModalityType;
        }

        this.resizable = fields.get("resizable", true);
        this.undecorated = fields.get("undecorated", false);
        this.title = (String)fields.get("title", "");

        blockedWindows = new IdentityArrayList<>();

        SunToolkit.checkAndSetPolicy(this);

        initialized = true;

    }

    /*
     * --- 可访问性支持 ---
     *
     */

    /**
     * 获取与此 Dialog 关联的 AccessibleContext。
     * 对于对话框，AccessibleContext 的形式为 AccessibleAWTDialog。
     * 如果必要，将创建一个新的 AccessibleAWTDialog 实例。
     *
     * @return 一个 AccessibleAWTDialog，作为此 Dialog 的 AccessibleContext
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTDialog();
        }
        return accessibleContext;
    }

    /**
     * 该类为 <code>Dialog</code> 类实现可访问性支持。
     * 它为对话框用户界面元素提供了 Java 可访问性 API 的适当实现。
     * @since 1.3
     */
    protected class AccessibleAWTDialog extends AccessibleAWTWindow
    {
        /*
         * JDK 1.3 serialVersionUID
         */
        private static final long serialVersionUID = 4837230331833941201L;

        /**
         * 获取此对象的角色。
         *
         * @return 一个 AccessibleRole 实例，描述对象的角色
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.DIALOG;
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
            if (isModal()) {
                states.add(AccessibleState.MODAL);
            }
            if (isResizable()) {
                states.add(AccessibleState.RESIZABLE);
            }
            return states;
        }

    } // 内部类 AccessibleAWTDialog
}
