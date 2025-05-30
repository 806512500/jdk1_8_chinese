
/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import java.awt.peer.KeyboardFocusManagerPeer;
import java.awt.peer.LightweightPeer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import java.lang.ref.WeakReference;

import java.lang.reflect.Field;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import sun.util.logging.PlatformLogger;

import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.CausedFocusEvent;
import sun.awt.KeyboardFocusManagerPeerProvider;
import sun.awt.AWTAccessor;

/**
 * 键盘焦点管理器负责管理活动窗口和焦点窗口，以及当前的焦点所有者。焦点所有者是指在应用程序中通常接收所有用户生成的键事件的组件。焦点窗口是指包含焦点所有者的窗口。只有框架或对话框可以是活动窗口。本地窗口系统可能会用特殊的装饰（如高亮的标题栏）来表示活动窗口。活动窗口总是焦点窗口，或者是焦点窗口的所有者中的第一个框架或对话框。
 * <p>
 * 键盘焦点管理器既是客户端代码查询焦点所有者和发起焦点更改的集中位置，也是所有焦点事件、与焦点相关的窗口事件和键事件的分发器。
 * <p>
 * 一些浏览器将不同代码库中的小程序分隔成不同的上下文，并在这些上下文之间建立隔离。在这种情况下，每个上下文将有一个键盘焦点管理器。其他浏览器将所有小程序放入同一个上下文中，这意味着所有小程序将只有一个全局的键盘焦点管理器。这种行为是实现依赖的。请参阅浏览器的文档以获取更多信息。然而，无论有多少个上下文，每个类加载器都只能有一个焦点所有者、焦点窗口或活动窗口。
 * <p>
 * 请参阅
 * <a href="https://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html">
 * 如何使用焦点子系统</a>，
 * 《Java教程》中的一个部分，以及
 * <a href="../../java/awt/doc-files/FocusSpec.html">焦点规范</a>
 * 以获取更多信息。
 *
 * @author David Mendenhall
 *
 * @see Window
 * @see Frame
 * @see Dialog
 * @see java.awt.event.FocusEvent
 * @see java.awt.event.WindowEvent
 * @see java.awt.event.KeyEvent
 * @since 1.4
 */
public abstract class KeyboardFocusManager
    implements KeyEventDispatcher, KeyEventPostProcessor
{

    // 共享的焦点引擎日志记录器
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("java.awt.focus.KeyboardFocusManager");

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
        AWTAccessor.setKeyboardFocusManagerAccessor(
            new AWTAccessor.KeyboardFocusManagerAccessor() {
                public int shouldNativelyFocusHeavyweight(Component heavyweight,
                                                   Component descendant,
                                                   boolean temporary,
                                                   boolean focusedWindowChangeAllowed,
                                                   long time,
                                                   CausedFocusEvent.Cause cause)
                {
                    return KeyboardFocusManager.shouldNativelyFocusHeavyweight(
                        heavyweight, descendant, temporary, focusedWindowChangeAllowed, time, cause);
                }
                public boolean processSynchronousLightweightTransfer(Component heavyweight,
                                                              Component descendant,
                                                              boolean temporary,
                                                              boolean focusedWindowChangeAllowed,
                                                              long time)
                {
                    return KeyboardFocusManager.processSynchronousLightweightTransfer(
                        heavyweight, descendant, temporary, focusedWindowChangeAllowed, time);
                }
                public void removeLastFocusRequest(Component heavyweight) {
                    KeyboardFocusManager.removeLastFocusRequest(heavyweight);
                }
                public void setMostRecentFocusOwner(Window window, Component component) {
                    KeyboardFocusManager.setMostRecentFocusOwner(window, component);
                }
                public KeyboardFocusManager getCurrentKeyboardFocusManager(AppContext ctx) {
                    return KeyboardFocusManager.getCurrentKeyboardFocusManager(ctx);
                }
                public Container getCurrentFocusCycleRoot() {
                    return KeyboardFocusManager.currentFocusCycleRoot;
                }
            }
        );
    }

    transient KeyboardFocusManagerPeer peer;

    /**
     * 初始化JNI字段和方法ID
     */
    private static native void initIDs();

    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.KeyboardFocusManager");

    /**
     * 前向焦点遍历键的标识符。
     *
     * @see #setDefaultFocusTraversalKeys
     * @see #getDefaultFocusTraversalKeys
     * @see Component#setFocusTraversalKeys
     * @see Component#getFocusTraversalKeys
     */
    public static final int FORWARD_TRAVERSAL_KEYS = 0;

    /**
     * 后向焦点遍历键的标识符。
     *
     * @see #setDefaultFocusTraversalKeys
     * @see #getDefaultFocusTraversalKeys
     * @see Component#setFocusTraversalKeys
     * @see Component#getFocusTraversalKeys
     */
    public static final int BACKWARD_TRAVERSAL_KEYS = 1;

    /**
     * 上循环焦点遍历键的标识符。
     *
     * @see #setDefaultFocusTraversalKeys
     * @see #getDefaultFocusTraversalKeys
     * @see Component#setFocusTraversalKeys
     * @see Component#getFocusTraversalKeys
     */
    public static final int UP_CYCLE_TRAVERSAL_KEYS = 2;

    /**
     * 下循环焦点遍历键的标识符。
     *
     * @see #setDefaultFocusTraversalKeys
     * @see #getDefaultFocusTraversalKeys
     * @see Component#setFocusTraversalKeys
     * @see Component#getFocusTraversalKeys
     */
    public static final int DOWN_CYCLE_TRAVERSAL_KEYS = 3;

    static final int TRAVERSAL_KEY_LENGTH = DOWN_CYCLE_TRAVERSAL_KEYS + 1;

    /**
     * 返回调用线程上下文的当前KeyboardFocusManager实例。
     *
     * @return 该线程上下文的KeyboardFocusManager
     * @see #setCurrentKeyboardFocusManager
     */
    public static KeyboardFocusManager getCurrentKeyboardFocusManager() {
        return getCurrentKeyboardFocusManager(AppContext.getAppContext());
    }

    synchronized static KeyboardFocusManager
        getCurrentKeyboardFocusManager(AppContext appcontext)
    {
        KeyboardFocusManager manager = (KeyboardFocusManager)
            appcontext.get(KeyboardFocusManager.class);
        if (manager == null) {
            manager = new DefaultKeyboardFocusManager();
            appcontext.put(KeyboardFocusManager.class, manager);
        }
        return manager;
    }

    /**
     * 设置调用线程上下文的当前KeyboardFocusManager实例。如果指定为null，则当前的KeyboardFocusManager将被新的DefaultKeyboardFocusManager实例替换。
     * <p>
     * 如果安装了SecurityManager，调用线程必须被授予AWTPermission "replaceKeyboardFocusManager" 才能替换当前的KeyboardFocusManager。如果没有授予此权限，此方法将抛出SecurityException，当前的KeyboardFocusManager将保持不变。
     *
     * @param newManager 该线程上下文的新KeyboardFocusManager
     * @see #getCurrentKeyboardFocusManager
     * @see DefaultKeyboardFocusManager
     * @throws SecurityException 如果调用线程没有权限替换当前的KeyboardFocusManager
     */
    public static void setCurrentKeyboardFocusManager(
        KeyboardFocusManager newManager) throws SecurityException
    {
        checkReplaceKFMPermission();

        KeyboardFocusManager oldManager = null;

        synchronized (KeyboardFocusManager.class) {
            AppContext appcontext = AppContext.getAppContext();

            if (newManager != null) {
                oldManager = getCurrentKeyboardFocusManager(appcontext);

                appcontext.put(KeyboardFocusManager.class, newManager);
            } else {
                oldManager = getCurrentKeyboardFocusManager(appcontext);
                appcontext.remove(KeyboardFocusManager.class);
            }
        }

        if (oldManager != null) {
            oldManager.firePropertyChange("managingFocus",
                                          Boolean.TRUE,
                                          Boolean.FALSE);
        }
        if (newManager != null) {
            newManager.firePropertyChange("managingFocus",
                                          Boolean.FALSE,
                                          Boolean.TRUE);
        }
    }

    /**
     * 应用程序中通常接收所有用户生成的键事件的组件。
     */
    private static Component focusOwner;

    /**
     * 当前的临时焦点传输完成时将重新获得焦点的组件，或者如果不存在未完成的临时传输，则为焦点所有者。
     */
    private static Component permanentFocusOwner;

    /**
     * 包含焦点所有者的窗口。
     */
    private static Window focusedWindow;

    /**
     * 只有框架或对话框可以是活动窗口。本地窗口系统可能会用特殊的装饰（如高亮的标题栏）来表示活动窗口。活动窗口总是焦点窗口，或者是焦点窗口的所有者中的第一个框架或对话框。
     */
    private static Window activeWindow;

    /**
     * 所有没有设置自己策略的窗口的默认FocusTraversalPolicy。如果这些窗口有焦点循环根子组件，而这些子组件没有自己的键盘遍历策略，那么这些子组件（递归地，它们的焦点循环根子组件）也将继承此策略。
     */
    private FocusTraversalPolicy defaultPolicy =
        new DefaultFocusTraversalPolicy();

    /**
     * 每个焦点遍历键的绑定属性名称。
     */
    private static final String[] defaultFocusTraversalKeyPropertyNames = {
        "forwardDefaultFocusTraversalKeys",
        "backwardDefaultFocusTraversalKeys",
        "upCycleDefaultFocusTraversalKeys",
        "downCycleDefaultFocusTraversalKeys"
    };

    /**
     * 默认的焦点遍历键。每个遍历键数组将在所有没有显式设置此类数组的窗口中生效。每个数组也将被递归地继承，由这些窗口的任何没有显式设置此类数组的子组件继承。
     */
    private Set<AWTKeyStroke>[] defaultFocusTraversalKeys = new Set[4];

    /**
     * 当前的焦点循环根。如果焦点所有者本身是一个焦点循环根，那么在正常焦点遍历时，哪些组件是下一个和上一个组件可能会有歧义。在这种情况下，当前的焦点循环根用于区分这些可能性。
     */
    private static Container currentFocusCycleRoot;

    /**
     * 注册的任何VetoableChangeListeners的描述。
     */
    private VetoableChangeSupport vetoableSupport;

    /**
     * 注册的任何PropertyChangeListeners的描述。
     */
    private PropertyChangeSupport changeSupport;

    /**
     * 此KeyboardFocusManager的KeyEventDispatcher链。列表不包括此KeyboardFocusManager，除非它通过调用<code>addKeyEventDispatcher</code>显式重新注册。如果没有其他KeyEventDispatchers注册，此字段可能为null或引用长度为0的列表。
     */
    private java.util.LinkedList<KeyEventDispatcher> keyEventDispatchers;

    /**
     * 此KeyboardFocusManager的KeyEventPostProcessor链。列表不包括此KeyboardFocusManager，除非它通过调用<code>addKeyEventPostProcessor</code>显式重新注册。如果没有其他KeyEventPostProcessors注册，此字段可能为null或引用长度为0的列表。
     */
    private java.util.LinkedList<KeyEventPostProcessor> keyEventPostProcessors;

    /**
     * 将窗口映射到这些窗口的最近焦点所有者。
     */
    private static java.util.Map<Window, WeakReference<Component>> mostRecentFocusOwners = new WeakHashMap<>();

    /**
     * 我们缓存用于验证调用线程是否有权限访问全局焦点状态的权限。
     */
    private static AWTPermission replaceKeyboardFocusManagerPermission;


                /*
     * 当前在 AppContext 中分派的 SequencedEvent。
     */
    transient SequencedEvent currentSequencedEvent = null;

    final void setCurrentSequencedEvent(SequencedEvent current) {
        synchronized (SequencedEvent.class) {
            assert(current == null || currentSequencedEvent == null);
            currentSequencedEvent = current;
        }
    }

    final SequencedEvent getCurrentSequencedEvent() {
        synchronized (SequencedEvent.class) {
            return currentSequencedEvent;
        }
    }

    static Set<AWTKeyStroke> initFocusTraversalKeysSet(String value, Set<AWTKeyStroke> targetSet) {
        StringTokenizer tokens = new StringTokenizer(value, ",");
        while (tokens.hasMoreTokens()) {
            targetSet.add(AWTKeyStroke.getAWTKeyStroke(tokens.nextToken()));
        }
        return (targetSet.isEmpty())
            ? Collections.EMPTY_SET
            : Collections.unmodifiableSet(targetSet);
    }

    /**
     * 初始化一个 KeyboardFocusManager。
     */
    public KeyboardFocusManager() {
        AWTKeyStroke[][] defaultFocusTraversalKeyStrokes = {
                {
                        AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0, false),
                        AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,
                                InputEvent.CTRL_DOWN_MASK |
                                        InputEvent.CTRL_MASK, false),
                },
                {
                        AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,
                                InputEvent.SHIFT_DOWN_MASK |
                                        InputEvent.SHIFT_MASK, false),
                        AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,
                                InputEvent.SHIFT_DOWN_MASK |
                                        InputEvent.SHIFT_MASK |
                                        InputEvent.CTRL_DOWN_MASK |
                                        InputEvent.CTRL_MASK,
                                false),
                },
                {},
                {},
        };
        for (int i = 0; i < TRAVERSAL_KEY_LENGTH; i++) {
            Set<AWTKeyStroke> work_set = new HashSet<>();
            for (int j = 0; j < defaultFocusTraversalKeyStrokes[i].length; j++) {
                work_set.add(defaultFocusTraversalKeyStrokes[i][j]);
            }
            defaultFocusTraversalKeys[i] = (work_set.isEmpty())
                ? Collections.EMPTY_SET
                : Collections.unmodifiableSet(work_set);
        }
        initPeer();
    }

    private void initPeer() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        KeyboardFocusManagerPeerProvider peerProvider = (KeyboardFocusManagerPeerProvider)tk;
        peer = peerProvider.getKeyboardFocusManagerPeer();
    }

    /**
     * 如果调用线程与焦点所有者在同一个上下文中，则返回焦点所有者。焦点所有者定义为应用程序中通常接收所有用户生成的 KeyEvent 的组件。如果启用了焦点遍历键，映射到焦点所有者焦点遍历键的 KeyEvent 将不会被传递。此外，KeyEventDispatchers 可能会在 KeyEvent 到达焦点所有者之前重新定向或消耗这些事件。
     *
     * @return 焦点所有者，如果焦点所有者不是调用线程上下文的成员，则返回 null
     * @see #getGlobalFocusOwner
     * @see #setGlobalFocusOwner
     */
    public Component getFocusOwner() {
        synchronized (KeyboardFocusManager.class) {
            if (focusOwner == null) {
                return null;
            }

            return (focusOwner.appContext == AppContext.getAppContext())
                ? focusOwner
                : null;
        }
    }

    /**
     * 即使调用线程与焦点所有者在不同的上下文中，也返回焦点所有者。焦点所有者定义为应用程序中通常接收所有用户生成的 KeyEvent 的组件。如果启用了焦点遍历键，映射到焦点所有者焦点遍历键的 KeyEvent 将不会被传递。此外，KeyEventDispatchers 可能会在 KeyEvent 到达焦点所有者之前重新定向或消耗这些事件。
     * <p>
     * 如果此 KeyboardFocusManager 不是调用线程上下文的当前 KeyboardFocusManager，此方法将抛出 SecurityException。
     *
     * @return 焦点所有者
     * @see #getFocusOwner
     * @see #setGlobalFocusOwner
     * @throws SecurityException 如果此 KeyboardFocusManager 不是调用线程上下文的当前 KeyboardFocusManager，且调用线程没有 "replaceKeyboardFocusManager" 权限
     */
    protected Component getGlobalFocusOwner() throws SecurityException {
        synchronized (KeyboardFocusManager.class) {
            checkKFMSecurity();
            return focusOwner;
        }
    }

    /**
     * 设置焦点所有者。如果组件不可聚焦，操作将被取消。焦点所有者定义为应用程序中通常接收所有用户生成的 KeyEvent 的组件。如果启用了焦点遍历键，映射到焦点所有者焦点遍历键的 KeyEvent 将不会被传递。此外，KeyEventDispatchers 可能会在 KeyEvent 到达焦点所有者之前重新定向或消耗这些事件。
     * <p>
     * 此方法实际上并不会将焦点设置到指定的组件。它只是存储值，以便随后由 <code>getFocusOwner()</code> 返回。使用 <code>Component.requestFocus()</code> 或 <code>Component.requestFocusInWindow()</code> 来更改焦点所有者，受平台限制。
     *
     * @param focusOwner 焦点所有者
     * @see #getFocusOwner
     * @see #getGlobalFocusOwner
     * @see Component#requestFocus()
     * @see Component#requestFocusInWindow()
     * @see Component#isFocusable
     * @throws SecurityException 如果此 KeyboardFocusManager 不是调用线程上下文的当前 KeyboardFocusManager，且调用线程没有 "replaceKeyboardFocusManager" 权限
     * @beaninfo
     *       bound: true
     */
    protected void setGlobalFocusOwner(Component focusOwner)
        throws SecurityException
    {
        Component oldFocusOwner = null;
        boolean shouldFire = false;

        if (focusOwner == null || focusOwner.isFocusable()) {
            synchronized (KeyboardFocusManager.class) {
                checkKFMSecurity();

                oldFocusOwner = getFocusOwner();

                try {
                    fireVetoableChange("focusOwner", oldFocusOwner,
                                       focusOwner);
                } catch (PropertyVetoException e) {
                    // 被拒绝
                    return;
                }

                KeyboardFocusManager.focusOwner = focusOwner;

                if (focusOwner != null &&
                    (getCurrentFocusCycleRoot() == null ||
                     !focusOwner.isFocusCycleRoot(getCurrentFocusCycleRoot())))
                {
                    Container rootAncestor =
                        focusOwner.getFocusCycleRootAncestor();
                    if (rootAncestor == null && (focusOwner instanceof Window))
                    {
                        rootAncestor = (Container)focusOwner;
                    }
                    if (rootAncestor != null) {
                        setGlobalCurrentFocusCycleRootPriv(rootAncestor);
                    }
                }

                shouldFire = true;
            }
        }

        if (shouldFire) {
            firePropertyChange("focusOwner", oldFocusOwner, focusOwner);
        }
    }

    /**
     * 如果焦点所有者存在且位于与调用线程相同的上下文中，则在 Java 和本机级别清除焦点所有者，否则方法将静默返回。
     * <p>
     * 焦点所有者组件将接收一个永久的 FOCUS_LOST 事件。此操作完成后，本机窗口系统将丢弃所有用户生成的 KeyEvent，直到用户选择一个新的组件来接收焦点，或者通过调用 <code>requestFocus()</code> 显式地给组件焦点。此操作不会更改聚焦或活动的窗口。
     *
     * @see Component#requestFocus()
     * @see java.awt.event.FocusEvent#FOCUS_LOST
     * @since 1.8
     */
    public void clearFocusOwner() {
        if (getFocusOwner() != null) {
            clearGlobalFocusOwner();
        }
    }

    /**
     * 在 Java 和本机级别清除全局焦点所有者。如果存在焦点所有者，该组件将接收一个永久的 FOCUS_LOST 事件。此操作完成后，本机窗口系统将丢弃所有用户生成的 KeyEvent，直到用户选择一个新的组件来接收焦点，或者通过调用 <code>requestFocus()</code> 显式地给组件焦点。此操作不会更改聚焦或活动的窗口。
     * <p>
     * 如果安装了 SecurityManager，调用线程必须被授予 "replaceKeyboardFocusManager" AWTPermission。如果未授予此权限，此方法将抛出 SecurityException，当前焦点所有者将不会被清除。
     * <p>
     * 此方法仅用于设置为调用线程上下文当前 KeyboardFocusManager 的 KeyboardFocusManager。不用于一般客户端使用。
     *
     * @see KeyboardFocusManager#clearFocusOwner
     * @see Component#requestFocus()
     * @see java.awt.event.FocusEvent#FOCUS_LOST
     * @throws SecurityException 如果调用线程没有 "replaceKeyboardFocusManager" 权限
     */
    public void clearGlobalFocusOwner()
        throws SecurityException
    {
        checkReplaceKFMPermission();
        if (!GraphicsEnvironment.isHeadless()) {
            // 工具包必须完全初始化，否则 _clearGlobalFocusOwner 将崩溃或抛出异常
            Toolkit.getDefaultToolkit();

            _clearGlobalFocusOwner();
        }
    }
    private void _clearGlobalFocusOwner() {
        Window activeWindow = markClearGlobalFocusOwner();
        peer.clearGlobalFocusOwner(activeWindow);
    }

    void clearGlobalFocusOwnerPriv() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                clearGlobalFocusOwner();
                return null;
            }
        });
    }

    Component getNativeFocusOwner() {
        return peer.getCurrentFocusOwner();
    }

    void setNativeFocusOwner(Component comp) {
        if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
            focusLog.finest("Calling peer {0} setCurrentFocusOwner for {1}",
                            String.valueOf(peer), String.valueOf(comp));
        }
        peer.setCurrentFocusOwner(comp);
    }

    Window getNativeFocusedWindow() {
        return peer.getCurrentFocusedWindow();
    }

    /**
     * 如果永久焦点所有者位于与调用线程相同的上下文中，则返回永久焦点所有者。永久焦点所有者定义为应用程序中最后一个接收永久 FOCUS_GAINED 事件的组件。除非当前正在进行临时焦点更改，否则焦点所有者和永久焦点所有者是等价的。在这种情况下，当临时焦点更改结束时，永久焦点所有者将再次成为焦点所有者。
     *
     * @return 永久焦点所有者，如果永久焦点所有者不是调用线程上下文的成员，则返回 null
     * @see #getGlobalPermanentFocusOwner
     * @see #setGlobalPermanentFocusOwner
     */
    public Component getPermanentFocusOwner() {
        synchronized (KeyboardFocusManager.class) {
            if (permanentFocusOwner == null) {
                return null;
            }

            return (permanentFocusOwner.appContext ==
                    AppContext.getAppContext())
                ? permanentFocusOwner
                : null;
        }
    }

    /**
     * 即使调用线程与永久焦点所有者在不同的上下文中，也返回永久焦点所有者。永久焦点所有者定义为应用程序中最后一个接收永久 FOCUS_GAINED 事件的组件。除非当前正在进行临时焦点更改，否则焦点所有者和永久焦点所有者是等价的。在这种情况下，当临时焦点更改结束时，永久焦点所有者将再次成为焦点所有者。
     *
     * @return 永久焦点所有者
     * @see #getPermanentFocusOwner
     * @see #setGlobalPermanentFocusOwner
     * @throws SecurityException 如果此 KeyboardFocusManager 不是调用线程上下文的当前 KeyboardFocusManager，且调用线程没有 "replaceKeyboardFocusManager" 权限
     */
    protected Component getGlobalPermanentFocusOwner()
        throws SecurityException
    {
        synchronized (KeyboardFocusManager.class) {
            checkKFMSecurity();
            return permanentFocusOwner;
        }
    }

    /**
     * 设置永久焦点所有者。如果组件不可聚焦，操作将被取消。永久焦点所有者定义为应用程序中最后一个接收永久 FOCUS_GAINED 事件的组件。除非当前正在进行临时焦点更改，否则焦点所有者和永久焦点所有者是等价的。在这种情况下，当临时焦点更改结束时，永久焦点所有者将再次成为焦点所有者。
     * <p>
     * 此方法实际上并不会将焦点设置到指定的组件。它只是存储值，以便随后由 <code>getPermanentFocusOwner()</code> 返回。使用 <code>Component.requestFocus()</code> 或 <code>Component.requestFocusInWindow()</code> 来更改焦点所有者，受平台限制。
     *
     * @param permanentFocusOwner 永久焦点所有者
     * @see #getPermanentFocusOwner
     * @see #getGlobalPermanentFocusOwner
     * @see Component#requestFocus()
     * @see Component#requestFocusInWindow()
     * @see Component#isFocusable
     * @throws SecurityException 如果此 KeyboardFocusManager 不是调用线程上下文的当前 KeyboardFocusManager，且调用线程没有 "replaceKeyboardFocusManager" 权限
     * @beaninfo
     *       bound: true
     */
    protected void setGlobalPermanentFocusOwner(Component permanentFocusOwner)
        throws SecurityException
    {
        Component oldPermanentFocusOwner = null;
        boolean shouldFire = false;


                    if (permanentFocusOwner == null || permanentFocusOwner.isFocusable()) {
            synchronized (KeyboardFocusManager.class) {
                checkKFMSecurity();

                oldPermanentFocusOwner = getPermanentFocusOwner();

                try {
                    fireVetoableChange("permanentFocusOwner",
                                       oldPermanentFocusOwner,
                                       permanentFocusOwner);
                } catch (PropertyVetoException e) {
                    // 被拒绝
                    return;
                }

                KeyboardFocusManager.permanentFocusOwner = permanentFocusOwner;

                KeyboardFocusManager.
                    setMostRecentFocusOwner(permanentFocusOwner);

                shouldFire = true;
            }
        }

        if (shouldFire) {
            firePropertyChange("permanentFocusOwner", oldPermanentFocusOwner,
                               permanentFocusOwner);
        }
    }

    /**
     * 如果调用线程的上下文与焦点窗口相同，则返回焦点窗口。焦点窗口是包含焦点所有者的窗口。
     *
     * @return 焦点窗口，如果焦点窗口不是调用线程上下文的成员，则返回 null
     * @see #getGlobalFocusedWindow
     * @see #setGlobalFocusedWindow
     */
    public Window getFocusedWindow() {
        synchronized (KeyboardFocusManager.class) {
            if (focusedWindow == null) {
                return null;
            }

            return (focusedWindow.appContext == AppContext.getAppContext())
                ? focusedWindow
                : null;
        }
    }

    /**
     * 即使调用线程的上下文与焦点窗口不同，也返回焦点窗口。焦点窗口是包含焦点所有者的窗口。
     *
     * @return 焦点窗口
     * @see #getFocusedWindow
     * @see #setGlobalFocusedWindow
     * @throws SecurityException 如果此 KeyboardFocusManager 不是调用线程上下文的当前 KeyboardFocusManager，
     *         并且调用线程没有 "replaceKeyboardFocusManager" 权限
     */
    protected Window getGlobalFocusedWindow() throws SecurityException {
        synchronized (KeyboardFocusManager.class) {
            checkKFMSecurity();
            return focusedWindow;
        }
    }

    /**
     * 设置焦点窗口。焦点窗口是包含焦点所有者的窗口。如果指定的窗口不是可聚焦窗口，则操作将被取消。
     * <p>
     * 此方法不会实际更改本机窗口系统中的焦点窗口。它只是存储一个值，以便后续由 <code>getFocusedWindow()</code> 返回。
     * 使用 <code>Component.requestFocus()</code> 或 <code>Component.requestFocusInWindow()</code>
     * 更改焦点窗口，但受平台限制。
     *
     * @param focusedWindow 焦点窗口
     * @see #getFocusedWindow
     * @see #getGlobalFocusedWindow
     * @see Component#requestFocus()
     * @see Component#requestFocusInWindow()
     * @see Window#isFocusableWindow
     * @throws SecurityException 如果此 KeyboardFocusManager 不是调用线程上下文的当前 KeyboardFocusManager，
     *         并且调用线程没有 "replaceKeyboardFocusManager" 权限
     * @beaninfo
     *       bound: true
     */
    protected void setGlobalFocusedWindow(Window focusedWindow)
        throws SecurityException
    {
        Window oldFocusedWindow = null;
        boolean shouldFire = false;

        if (focusedWindow == null || focusedWindow.isFocusableWindow()) {
            synchronized (KeyboardFocusManager.class) {
                checkKFMSecurity();

                oldFocusedWindow = getFocusedWindow();

                try {
                    fireVetoableChange("focusedWindow", oldFocusedWindow,
                                       focusedWindow);
                } catch (PropertyVetoException e) {
                    // 被拒绝
                    return;
                }

                KeyboardFocusManager.focusedWindow = focusedWindow;
                shouldFire = true;
            }
        }

        if (shouldFire) {
            firePropertyChange("focusedWindow", oldFocusedWindow,
                               focusedWindow);
        }
    }

    /**
     * 如果活动窗口与调用线程的上下文相同，则返回活动窗口。只有 Frame 或 Dialog 可以是活动窗口。
     * 本机窗口系统可能会用特殊的装饰（如高亮的标题栏）表示活动窗口或其子窗口。
     * 活动窗口始终是焦点窗口，或者是焦点窗口的所有者中的第一个 Frame 或 Dialog。
     *
     * @return 活动窗口，如果活动窗口不是调用线程上下文的成员，则返回 null
     * @see #getGlobalActiveWindow
     * @see #setGlobalActiveWindow
     */
    public Window getActiveWindow() {
        synchronized (KeyboardFocusManager.class) {
            if (activeWindow == null) {
                return null;
            }

            return (activeWindow.appContext == AppContext.getAppContext())
                ? activeWindow
                : null;
        }
    }

    /**
     * 即使调用线程的上下文与活动窗口不同，也返回活动窗口。只有 Frame 或 Dialog 可以是活动窗口。
     * 本机窗口系统可能会用特殊的装饰（如高亮的标题栏）表示活动窗口或其子窗口。
     * 活动窗口始终是焦点窗口，或者是焦点窗口的所有者中的第一个 Frame 或 Dialog。
     *
     * @return 活动窗口
     * @see #getActiveWindow
     * @see #setGlobalActiveWindow
     * @throws SecurityException 如果此 KeyboardFocusManager 不是调用线程上下文的当前 KeyboardFocusManager，
     *         并且调用线程没有 "replaceKeyboardFocusManager" 权限
     */
    protected Window getGlobalActiveWindow() throws SecurityException {
        synchronized (KeyboardFocusManager.class) {
            checkKFMSecurity();
            return activeWindow;
        }
    }

    /**
     * 设置活动窗口。只有 Frame 或 Dialog 可以是活动窗口。本机窗口系统可能会用特殊的装饰（如高亮的标题栏）表示活动窗口或其子窗口。
     * 活动窗口始终是焦点窗口，或者是焦点窗口的所有者中的第一个 Frame 或 Dialog。
     * <p>
     * 此方法不会实际更改本机窗口系统中的活动窗口。它只是存储一个值，以便后续由 <code>getActiveWindow()</code> 返回。
     * 使用 <code>Component.requestFocus()</code> 或 <code>Component.requestFocusInWindow()</code>
     * 更改活动窗口，但受平台限制。
     *
     * @param activeWindow 活动窗口
     * @see #getActiveWindow
     * @see #getGlobalActiveWindow
     * @see Component#requestFocus()
     * @see Component#requestFocusInWindow()
     * @throws SecurityException 如果此 KeyboardFocusManager 不是调用线程上下文的当前 KeyboardFocusManager，
     *         并且调用线程没有 "replaceKeyboardFocusManager" 权限
     * @beaninfo
     *       bound: true
     */
    protected void setGlobalActiveWindow(Window activeWindow)
        throws SecurityException
    {
        Window oldActiveWindow;
        synchronized (KeyboardFocusManager.class) {
            checkKFMSecurity();

            oldActiveWindow = getActiveWindow();
            if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                focusLog.finer("Setting global active window to " + activeWindow + ", old active " + oldActiveWindow);
            }

            try {
                fireVetoableChange("activeWindow", oldActiveWindow,
                                   activeWindow);
            } catch (PropertyVetoException e) {
                // 被拒绝
                return;
            }

            KeyboardFocusManager.activeWindow = activeWindow;
        }

        firePropertyChange("activeWindow", oldActiveWindow, activeWindow);
    }

    /**
     * 返回默认的焦点遍历策略。顶级组件在创建时使用此值通过显式调用 Container.setFocusTraversalPolicy 来初始化它们自己的焦点遍历策略。
     *
     * @return 默认的焦点遍历策略。永远不会返回 null。
     * @see #setDefaultFocusTraversalPolicy
     * @see Container#setFocusTraversalPolicy
     * @see Container#getFocusTraversalPolicy
     */
    public synchronized FocusTraversalPolicy getDefaultFocusTraversalPolicy() {
        return defaultPolicy;
    }

    /**
     * 设置默认的焦点遍历策略。顶级组件在创建时使用此值通过显式调用 Container.setFocusTraversalPolicy 来初始化它们自己的焦点遍历策略。
     * 注意：此调用不会影响已创建的组件，因为它们的策略已经初始化。只有新组件会使用此策略作为其默认策略。
     *
     * @param defaultPolicy 新的默认焦点遍历策略
     * @see #getDefaultFocusTraversalPolicy
     * @see Container#setFocusTraversalPolicy
     * @see Container#getFocusTraversalPolicy
     * @throws IllegalArgumentException 如果 defaultPolicy 为 null
     * @beaninfo
     *       bound: true
     */
    public void setDefaultFocusTraversalPolicy(FocusTraversalPolicy
                                               defaultPolicy) {
        if (defaultPolicy == null) {
            throw new IllegalArgumentException("默认焦点遍历策略不能为 null");
        }

        FocusTraversalPolicy oldPolicy;

        synchronized (this) {
            oldPolicy = this.defaultPolicy;
            this.defaultPolicy = defaultPolicy;
        }

        firePropertyChange("defaultFocusTraversalPolicy", oldPolicy,
                           defaultPolicy);
    }

    /**
     * 为给定的遍历操作设置默认的焦点遍历键。此遍历键 {@code Set} 将在所有没有显式定义此类 {@code Set} 的 {@code Window} 上生效。
     * 此 {@code Set} 还将递归地继承给这些 {@code Windows} 的任何没有显式定义此类 {@code Set} 的子 {@code Component}。
     * <p>
     * 默认焦点遍历键的默认值取决于实现。Sun 建议特定本机平台的所有实现使用相同的默认值。Windows 和 Unix 的推荐值如下表所示。
     * 这些推荐值在 Sun AWT 实现中使用。
     *
     * <table border=1 summary="推荐的默认焦点遍历键值">
     * <tr>
     *    <th>标识符</th>
     *    <th>含义</th>
     *    <th>默认值</th>
     * </tr>
     * <tr>
     *    <td>{@code KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS}</td>
     *    <td>正常的前向键盘遍历</td>
     *    <td>{@code TAB} 在 {@code KEY_PRESSED} 时，
     *        {@code CTRL-TAB} 在 {@code KEY_PRESSED} 时</td>
     * </tr>
     * <tr>
     *    <td>{@code KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS}</td>
     *    <td>正常的后向键盘遍历</td>
     *    <td>{@code SHIFT-TAB} 在 {@code KEY_PRESSED} 时，
     *        {@code CTRL-SHIFT-TAB} 在 {@code KEY_PRESSED} 时</td>
     * </tr>
     * <tr>
     *    <td>{@code KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS}</td>
     *    <td>上移一个焦点遍历周期</td>
     *    <td>无</td>
     * </tr>
     * <tr>
     *    <td>{@code KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS}</td>
     *    <td>下移一个焦点遍历周期</td>
     *    <td>无</td>
     * </tr>
     * </table>
     *
     * 要禁用遍历键，可以使用空的 {@code Set}；推荐使用 {@code Collections.EMPTY_SET}。
     * <p>
     * 使用 {@code AWTKeyStroke} API，客户端代码可以指定在两个特定的 {@code KeyEvent} 中的哪一个，
     * {@code KEY_PRESSED} 或 {@code KEY_RELEASED}，焦点遍历操作将发生。然而，无论指定哪个 {@code KeyEvent}，
     * 与焦点遍历键相关的所有 {@code KeyEvent}，包括关联的 {@code KEY_TYPED} 事件，都将被消耗，不会被分发给任何 {@code Component}。
     * 运行时错误是在指定 {@code KEY_TYPED} 事件映射到焦点遍历操作，或映射同一事件到多个默认焦点遍历操作时。
     * <p>
     * 如果 {@code keystrokes} 中的任何 {@code Object} 不是 {@code AWTKeyStroke}，此方法可能会抛出 {@code ClassCastException}。
     *
     * @param id 以下之一：
     *        {@code KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS}，
     *        {@code KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS}，
     *        {@code KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS}，或
     *        {@code KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS}
     * @param keystrokes 为指定操作设置的 {@code AWTKeyStroke} 集合
     * @see #getDefaultFocusTraversalKeys
     * @see Component#setFocusTraversalKeys
     * @see Component#getFocusTraversalKeys
     * @throws IllegalArgumentException 如果 id 不是以下之一：
     *         {@code KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS}，
     *         {@code KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS}，
     *         {@code KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS}，或
     *         {@code KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS}，
     *         或者 keystrokes 为 {@code null}，
     *         或者 keystrokes 包含 {@code null}，
     *         或者任何 keystroke
     *         代表 {@code KEY_TYPED} 事件，
     *         或者任何 keystroke 已经映射到另一个默认焦点遍历操作
     * @beaninfo
     *       bound: true
     */
    public void
        setDefaultFocusTraversalKeys(int id,
                                     Set<? extends AWTKeyStroke> keystrokes)
    {
        if (id < 0 || id >= TRAVERSAL_KEY_LENGTH) {
            throw new IllegalArgumentException("无效的焦点遍历键标识符");
        }
        if (keystrokes == null) {
            throw new IllegalArgumentException("不能设置默认焦点遍历键的 null 集合");
        }

        Set<AWTKeyStroke> oldKeys;

        synchronized (this) {
            for (AWTKeyStroke keystroke : keystrokes) {


                            if (keystroke == null) {
                    throw new IllegalArgumentException("不能设置 null 作为焦点遍历键");
                }

                if (keystroke.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                    throw new IllegalArgumentException("焦点遍历键不能映射到 KEY_TYPED 事件");
                }

                // 检查键是否已映射到其他遍历操作
                for (int i = 0; i < TRAVERSAL_KEY_LENGTH; i++) {
                    if (i == id) {
                        continue;
                    }

                    if (defaultFocusTraversalKeys[i].contains(keystroke)) {
                        throw new IllegalArgumentException("焦点遍历键在组件中必须是唯一的");
                    }
                }
            }

            oldKeys = defaultFocusTraversalKeys[id];
            defaultFocusTraversalKeys[id] =
                Collections.unmodifiableSet(new HashSet<>(keystrokes));
        }

        firePropertyChange(defaultFocusTraversalKeyPropertyNames[id],
                           oldKeys, keystrokes);
    }

    /**
     * 返回给定遍历操作的默认焦点遍历键集。此遍历键集将在没有显式定义此类键集的所有窗口中生效。此键集还将递归地继承给这些窗口的任何子组件，前提是这些子组件没有显式定义此类键集。（请参阅
     * <code>setDefaultFocusTraversalKeys</code> 以获取每个操作的完整描述。）
     *
     * @param id 以下之一：KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS，
     *        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS，
     *        KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS，或
     *        KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS
     * @return 指定操作的 <code>AWTKeyStroke</code> 集；该集将不可修改，且可能为空；永远不会返回 <code>null</code>
     * @see #setDefaultFocusTraversalKeys
     * @see Component#setFocusTraversalKeys
     * @see Component#getFocusTraversalKeys
     * @throws IllegalArgumentException 如果 id 不是以下之一：
     *         KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS，
     *         KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS，
     *         KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS，或
     *         KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS
     */
    public Set<AWTKeyStroke> getDefaultFocusTraversalKeys(int id) {
        if (id < 0 || id >= TRAVERSAL_KEY_LENGTH) {
            throw new IllegalArgumentException("无效的焦点遍历键标识符");
        }

        // 可以直接返回集，因为它是一个不可修改的视图
        return defaultFocusTraversalKeys[id];
    }

    /**
     * 返回当前焦点循环根，如果当前焦点循环根在调用线程的上下文中。如果焦点所有者本身是一个焦点循环根，则在正常焦点遍历期间，哪些组件代表下一个和上一个组件可能是模糊的。在这种情况下，当前焦点循环根用于区分各种可能性。
     * <p>
     * 此方法仅用于 KeyboardFocusManagers 和焦点实现。不供一般客户端使用。
     *
     * @return 当前焦点循环根，如果当前焦点循环根不是调用线程上下文的成员，则返回 null
     * @see #getGlobalCurrentFocusCycleRoot
     * @see #setGlobalCurrentFocusCycleRoot
     */
    public Container getCurrentFocusCycleRoot() {
        synchronized (KeyboardFocusManager.class) {
            if (currentFocusCycleRoot == null) {
                return null;
            }

            return (currentFocusCycleRoot.appContext ==
                    AppContext.getAppContext())
                ? currentFocusCycleRoot
                : null;
        }
    }

    /**
     * 返回当前焦点循环根，即使调用线程与当前焦点循环根位于不同的上下文。如果焦点所有者本身是一个焦点循环根，则在正常焦点遍历期间，哪些组件代表下一个和上一个组件可能是模糊的。在这种情况下，当前焦点循环根用于区分各种可能性。
     *
     * @return 当前焦点循环根，如果当前焦点循环根不是调用线程上下文的成员，则返回 null
     * @see #getCurrentFocusCycleRoot
     * @see #setGlobalCurrentFocusCycleRoot
     * @throws SecurityException 如果此 KeyboardFocusManager 不是调用线程上下文的当前 KeyboardFocusManager
     *         且调用线程没有 "replaceKeyboardFocusManager" 权限
     */
    protected Container getGlobalCurrentFocusCycleRoot()
        throws SecurityException
    {
        synchronized (KeyboardFocusManager.class) {
            checkKFMSecurity();
            return currentFocusCycleRoot;
        }
    }

    /**
     * 设置当前焦点循环根。如果焦点所有者本身是一个焦点循环根，则在正常焦点遍历期间，哪些组件代表下一个和上一个组件可能是模糊的。在这种情况下，当前焦点循环根用于区分各种可能性。
     * <p>
     * 如果安装了 SecurityManager，调用线程必须被授予 "replaceKeyboardFocusManager" AWTPermission。如果未授予此权限，此方法将抛出 SecurityException，且当前焦点循环根不会改变。
     * <p>
     * 此方法仅用于 KeyboardFocusManagers 和焦点实现。不供一般客户端使用。
     *
     * @param newFocusCycleRoot 新的焦点循环根
     * @see #getCurrentFocusCycleRoot
     * @see #getGlobalCurrentFocusCycleRoot
     * @throws SecurityException 如果调用线程没有
     *         "replaceKeyboardFocusManager" 权限
     * @beaninfo
     *       bound: true
     */
    public void setGlobalCurrentFocusCycleRoot(Container newFocusCycleRoot)
        throws SecurityException
    {
        checkReplaceKFMPermission();

        Container oldFocusCycleRoot;

        synchronized (KeyboardFocusManager.class) {
            oldFocusCycleRoot  = getCurrentFocusCycleRoot();
            currentFocusCycleRoot = newFocusCycleRoot;
        }

        firePropertyChange("currentFocusCycleRoot", oldFocusCycleRoot,
                           newFocusCycleRoot);
    }

    void setGlobalCurrentFocusCycleRootPriv(final Container newFocusCycleRoot) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                setGlobalCurrentFocusCycleRoot(newFocusCycleRoot);
                return null;
            }
        });
    }

    /**
     * 将 PropertyChangeListener 添加到监听器列表。监听器将注册此类的所有绑定属性，包括以下内容：
     * <ul>
     *    <li>KeyboardFocusManager 当前是否正在管理此应用程序或 applet 浏览器上下文的焦点
     *        ("managingFocus")</li>
     *    <li>焦点所有者 ("focusOwner")</li>
     *    <li>永久焦点所有者 ("permanentFocusOwner")</li>
     *    <li>焦点窗口 ("focusedWindow")</li>
     *    <li>活动窗口 ("activeWindow")</li>
     *    <li>默认焦点遍历策略
     *        ("defaultFocusTraversalPolicy")</li>
     *    <li>默认 FORWARD_TRAVERSAL_KEYS 集
     *        ("forwardDefaultFocusTraversalKeys")</li>
     *    <li>默认 BACKWARD_TRAVERSAL_KEYS 集
     *        ("backwardDefaultFocusTraversalKeys")</li>
     *    <li>默认 UP_CYCLE_TRAVERSAL_KEYS 集
     *        ("upCycleDefaultFocusTraversalKeys")</li>
     *    <li>默认 DOWN_CYCLE_TRAVERSAL_KEYS 集
     *        ("downCycleDefaultFocusTraversalKeys")</li>
     *    <li>当前焦点循环根 ("currentFocusCycleRoot")</li>
     * </ul>
     * 如果监听器为 null，则不会抛出异常且不会执行任何操作。
     *
     * @param listener 要添加的 PropertyChangeListener
     * @see #removePropertyChangeListener
     * @see #getPropertyChangeListeners
     * @see #addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (changeSupport == null) {
                    changeSupport = new PropertyChangeSupport(this);
                }
                changeSupport.addPropertyChangeListener(listener);
            }
        }
    }

    /**
     * 从监听器列表中移除 PropertyChangeListener。此方法应用于移除为此类所有绑定属性注册的 PropertyChangeListeners。
     * <p>
     * 如果监听器为 null，则不会抛出异常且不会执行任何操作。
     *
     * @param listener 要移除的 PropertyChangeListener
     * @see #addPropertyChangeListener
     * @see #getPropertyChangeListeners
     * @see #removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (changeSupport != null) {
                    changeSupport.removePropertyChangeListener(listener);
                }
            }
        }
    }

    /**
     * 返回在此键盘焦点管理器上注册的所有属性更改监听器的数组。
     *
     * @return 此键盘焦点管理器的所有
     *         <code>PropertyChangeListener</code>s
     *         或者如果当前没有注册属性更改监听器，则返回一个空数组
     *
     * @see #addPropertyChangeListener
     * @see #removePropertyChangeListener
     * @see #getPropertyChangeListeners(java.lang.String)
     * @since 1.4
     */
    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport(this);
        }
        return changeSupport.getPropertyChangeListeners();
    }

    /**
     * 将 PropertyChangeListener 添加到特定属性的监听器列表。指定的属性可以是用户定义的，也可以是以下之一：
     * <ul>
     *    <li>KeyboardFocusManager 当前是否正在管理此应用程序或 applet 浏览器上下文的焦点
     *        ("managingFocus")</li>
     *    <li>焦点所有者 ("focusOwner")</li>
     *    <li>永久焦点所有者 ("permanentFocusOwner")</li>
     *    <li>焦点窗口 ("focusedWindow")</li>
     *    <li>活动窗口 ("activeWindow")</li>
     *    <li>默认焦点遍历策略
     *        ("defaultFocusTraversalPolicy")</li>
     *    <li>默认 FORWARD_TRAVERSAL_KEYS 集
     *        ("forwardDefaultFocusTraversalKeys")</li>
     *    <li>默认 BACKWARD_TRAVERSAL_KEYS 集
     *        ("backwardDefaultFocusTraversalKeys")</li>
     *    <li>默认 UP_CYCLE_TRAVERSAL_KEYS 集
     *        ("upCycleDefaultFocusTraversalKeys")</li>
     *    <li>默认 DOWN_CYCLE_TRAVERSAL_KEYS 集
     *        ("downCycleDefaultFocusTraversalKeys")</li>
     *    <li>当前焦点循环根 ("currentFocusCycleRoot")</li>
     * </ul>
     * 如果监听器为 null，则不会抛出异常且不会执行任何操作。
     *
     * @param propertyName 以上列出的属性名称之一
     * @param listener 要添加的 PropertyChangeListener
     * @see #addPropertyChangeListener(java.beans.PropertyChangeListener)
     * @see #removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
     * @see #getPropertyChangeListeners(java.lang.String)
     */
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (changeSupport == null) {
                    changeSupport = new PropertyChangeSupport(this);
                }
                changeSupport.addPropertyChangeListener(propertyName,
                                                        listener);
            }
        }
    }

    /**
     * 从特定属性的监听器列表中移除 PropertyChangeListener。此方法应用于移除为此特定绑定属性注册的 PropertyChangeListeners。
     * <p>
     * 如果监听器为 null，则不会抛出异常且不会执行任何操作。
     *
     * @param propertyName 有效的属性名称
     * @param listener 要移除的 PropertyChangeListener
     * @see #addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
     * @see #getPropertyChangeListeners(java.lang.String)
     * @see #removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (changeSupport != null) {
                    changeSupport.removePropertyChangeListener(propertyName,
                                                               listener);
                }
            }
        }
    }

    /**
     * 返回与命名属性关联的所有 <code>PropertyChangeListener</code>s 的数组。
     *
     * @return 与命名属性关联的所有 <code>PropertyChangeListener</code>s
     *         或者如果未添加此类监听器，则返回一个空数组。
     *
     * @see #addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
     * @see #removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
     * @since 1.4
     */
    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport(this);
        }
        return changeSupport.getPropertyChangeListeners(propertyName);
    }

    /**
     * 在绑定属性发生变化时触发 PropertyChangeEvent。事件将传递给所有注册的 PropertyChangeListeners。如果 oldValue 和 newValue 相同，则不会传递事件。
     *
     * @param propertyName 已更改的属性名称
     * @param oldValue 属性的先前值
     * @param newValue 属性的新值
     */
    protected void firePropertyChange(String propertyName, Object oldValue,
                                      Object newValue)
    {
        if (oldValue == newValue) {
            return;
        }
        PropertyChangeSupport changeSupport = this.changeSupport;
        if (changeSupport != null) {
            changeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }


                /**
     * 将 VetoableChangeListener 添加到监听器列表中。监听器将为本类的所有可否决属性注册，包括以下内容：
     * <ul>
     *    <li>焦点所有者 ("focusOwner")</li>
     *    <li>永久焦点所有者 ("permanentFocusOwner")</li>
     *    <li>聚焦的窗口 ("focusedWindow")</li>
     *    <li>活动窗口 ("activeWindow")</li>
     * </ul>
     * 如果监听器为 null，不会抛出异常且不执行任何操作。
     *
     * @param listener 要添加的 VetoableChangeListener
     * @see #removeVetoableChangeListener
     * @see #getVetoableChangeListeners
     * @see #addVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
     */
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (vetoableSupport == null) {
                    vetoableSupport =
                        new VetoableChangeSupport(this);
                }
                vetoableSupport.addVetoableChangeListener(listener);
            }
        }
    }

    /**
     * 从监听器列表中移除 VetoableChangeListener。此方法应用于移除为本类所有可否决属性注册的 VetoableChangeListeners。
     * <p>
     * 如果监听器为 null，不会抛出异常且不执行任何操作。
     *
     * @param listener 要移除的 VetoableChangeListener
     * @see #addVetoableChangeListener
     * @see #getVetoableChangeListeners
     * @see #removeVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
     */
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (vetoableSupport != null) {
                    vetoableSupport.removeVetoableChangeListener(listener);
                }
            }
        }
    }

    /**
     * 返回在此键盘焦点管理器上注册的所有可否决更改监听器的数组。
     *
     * @return 此键盘焦点管理器的所有 <code>VetoableChangeListener</code>，或如果当前未注册任何可否决更改监听器，则返回一个空数组。
     *
     * @see #addVetoableChangeListener
     * @see #removeVetoableChangeListener
     * @see #getVetoableChangeListeners(java.lang.String)
     * @since 1.4
     */
    public synchronized VetoableChangeListener[] getVetoableChangeListeners() {
        if (vetoableSupport == null) {
            vetoableSupport = new VetoableChangeSupport(this);
        }
        return vetoableSupport.getVetoableChangeListeners();
    }

    /**
     * 将 VetoableChangeListener 添加到特定属性的监听器列表中。指定的属性可以是用户定义的，也可以是以下之一：
     * <ul>
     *    <li>焦点所有者 ("focusOwner")</li>
     *    <li>永久焦点所有者 ("permanentFocusOwner")</li>
     *    <li>聚焦的窗口 ("focusedWindow")</li>
     *    <li>活动窗口 ("activeWindow")</li>
     * </ul>
     * 如果监听器为 null，不会抛出异常且不执行任何操作。
     *
     * @param propertyName 以上列出的属性名称之一
     * @param listener 要添加的 VetoableChangeListener
     * @see #addVetoableChangeListener(java.beans.VetoableChangeListener)
     * @see #removeVetoableChangeListener
     * @see #getVetoableChangeListeners
     */
    public void addVetoableChangeListener(String propertyName,
                                          VetoableChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (vetoableSupport == null) {
                    vetoableSupport =
                        new VetoableChangeSupport(this);
                }
                vetoableSupport.addVetoableChangeListener(propertyName,
                                                          listener);
            }
        }
    }

    /**
     * 从特定属性的监听器列表中移除 VetoableChangeListener。此方法应用于移除为特定绑定属性注册的 VetoableChangeListeners。
     * <p>
     * 如果监听器为 null，不会抛出异常且不执行任何操作。
     *
     * @param propertyName 有效的属性名称
     * @param listener 要移除的 VetoableChangeListener
     * @see #addVetoableChangeListener
     * @see #getVetoableChangeListeners
     * @see #removeVetoableChangeListener(java.beans.VetoableChangeListener)
     */
    public void removeVetoableChangeListener(String propertyName,
                                             VetoableChangeListener listener) {
        if (listener != null) {
            synchronized (this) {
                if (vetoableSupport != null) {
                    vetoableSupport.removeVetoableChangeListener(propertyName,
                                                                 listener);
                }
            }
        }
    }

    /**
     * 返回与命名属性关联的所有 <code>VetoableChangeListener</code> 的数组。
     *
     * @return 与命名属性关联的所有 <code>VetoableChangeListener</code>，或如果未添加任何此类监听器，则返回一个空数组。
     *
     * @see #addVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
     * @see #removeVetoableChangeListener(java.lang.String,java.beans.VetoableChangeListener)
     * @see #getVetoableChangeListeners
     * @since 1.4
     */
    public synchronized VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        if (vetoableSupport == null) {
            vetoableSupport = new VetoableChangeSupport(this);
        }
        return vetoableSupport.getVetoableChangeListeners(propertyName);
    }

    /**
     * 在可否决属性更改时触发 PropertyChangeEvent。事件将传递给所有注册的 VetoableChangeListeners。如果 VetoableChangeListener 抛出 PropertyVetoException，
     * 将触发一个新事件，将所有 VetoableChangeListeners 恢复到旧值，然后重新抛出异常。如果 oldValue 和 newValue 相同，则不会传递任何事件。
     *
     * @param propertyName 更改的属性名称
     * @param oldValue 属性的旧值
     * @param newValue 属性的新值
     * @throws java.beans.PropertyVetoException 如果 <code>VetoableChangeListener</code> 抛出 <code>PropertyVetoException</code>
     */
    protected void fireVetoableChange(String propertyName, Object oldValue,
                                      Object newValue)
        throws PropertyVetoException
    {
        if (oldValue == newValue) {
            return;
        }
        VetoableChangeSupport vetoableSupport =
            this.vetoableSupport;
        if (vetoableSupport != null) {
            vetoableSupport.fireVetoableChange(propertyName, oldValue,
                                               newValue);
        }
    }

    /**
     * 将 KeyEventDispatcher 添加到此 KeyboardFocusManager 的分发器链中。此 KeyboardFocusManager 将请求每个 KeyEventDispatcher
     * 在最终分发 KeyEvent 之前分发用户生成的 KeyEvent。KeyEventDispatchers 将按添加顺序通知。一旦某个 KeyEventDispatcher
     * 从其 <code>dispatchKeyEvent</code> 方法返回 <code>true</code>，通知将停止。可以添加任意数量的 KeyEventDispatchers，
     * 也可以多次添加特定的 KeyEventDispatcher 实例。
     * <p>
     * 如果指定了 null 分发器，不会执行任何操作且不会抛出异常。
     * <p>
     * 在多线程应用程序中，{@link KeyEventDispatcher} 的行为与其他 AWT 监听器相同。有关详细信息，请参阅
     * <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param dispatcher 要添加到分发器链中的 KeyEventDispatcher
     * @see #removeKeyEventDispatcher
     */
    public void addKeyEventDispatcher(KeyEventDispatcher dispatcher) {
        if (dispatcher != null) {
            synchronized (this) {
                if (keyEventDispatchers == null) {
                    keyEventDispatchers = new java.util.LinkedList<>();
                }
                keyEventDispatchers.add(dispatcher);
            }
        }
    }

    /**
     * 从此 KeyboardFocusManager 的分发器链中移除之前添加的 KeyEventDispatcher。除非通过调用 <code>addKeyEventDispatcher</code>
     * 显式重新注册，否则此 KeyboardFocusManager 本身不能被移除。
     * <p>
     * 如果指定了 null 分发器，如果指定的分发器不在分发器链中，或如果指定了此 KeyboardFocusManager 而未显式重新注册，
     * 不会执行任何操作且不会抛出异常。
     * <p>
     * 在多线程应用程序中，{@link KeyEventDispatcher} 的行为与其他 AWT 监听器相同。有关详细信息，请参阅
     * <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param dispatcher 要从分发器链中移除的 KeyEventDispatcher
     * @see #addKeyEventDispatcher
     */
    public void removeKeyEventDispatcher(KeyEventDispatcher dispatcher) {
        if (dispatcher != null) {
            synchronized (this) {
                if (keyEventDispatchers != null) {
                    keyEventDispatchers.remove(dispatchor);
                }
            }
        }
    }

    /**
     * 返回此 KeyboardFocusManager 的 KeyEventDispatcher 链作为 List。除非通过调用
     * <code>addKeyEventDispatcher</code> 显式重新注册，否则 List 不会包含此 KeyboardFocusManager。
     * 如果没有其他 KeyEventDispatchers 注册，实现可以返回 null 或长度为 0 的 List。客户端代码不应假设一种行为优于另一种，
     * 也不应假设一旦建立的行为不会改变。
     *
     * @return 可能为 null 或空的 KeyEventDispatchers 列表
     * @see #addKeyEventDispatcher
     * @see #removeKeyEventDispatcher
     */
    protected synchronized java.util.List<KeyEventDispatcher>
        getKeyEventDispatchers()
    {
        return (keyEventDispatchers != null)
            ? (java.util.List)keyEventDispatchers.clone()
            : null;
    }

    /**
     * 将 KeyEventPostProcessor 添加到此 KeyboardFocusManager 的后处理器链中。在 KeyEvent 被分发并处理后，
     * KeyboardFocusManager 将请求每个 KeyEventPostProcessor 执行任何必要的后处理，作为 KeyEvent 最终解决的一部分。
     * KeyEventPostProcessors 将按添加顺序通知；当前 KeyboardFocusManager 将最后通知。一旦某个 KeyEventPostProcessor
     * 从其 <code>postProcessKeyEvent</code> 方法返回 <code>true</code>，通知将停止。可以添加任意数量的 KeyEventPostProcessors，
     * 也可以多次添加特定的 KeyEventPostProcessor 实例。
     * <p>
     * 如果指定了 null 后处理器，不会执行任何操作且不会抛出异常。
     * <p>
     * 在多线程应用程序中，{@link KeyEventPostProcessor} 的行为与其他 AWT 监听器相同。有关详细信息，请参阅
     * <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param processor 要添加到后处理器链中的 KeyEventPostProcessor
     * @see #removeKeyEventPostProcessor
     */
    public void addKeyEventPostProcessor(KeyEventPostProcessor processor) {
        if (processor != null) {
            synchronized (this) {
                if (keyEventPostProcessors == null) {
                    keyEventPostProcessors = new java.util.LinkedList<>();
                }
                keyEventPostProcessors.add(processor);
            }
        }
    }


    /**
     * 从此 KeyboardFocusManager 的后处理器链中移除之前添加的 KeyEventPostProcessor。此 KeyboardFocusManager 本身不能从链中完全移除。
     * 只能移除通过 <code>addKeyEventPostProcessor</code> 添加的额外引用。
     * <p>
     * 如果指定了 null 后处理器，如果指定的后处理器不在后处理器链中，或如果指定了此 KeyboardFocusManager 而未显式添加，
     * 不会执行任何操作且不会抛出异常。
     * <p>
     * 在多线程应用程序中，{@link KeyEventPostProcessor} 的行为与其他 AWT 监听器相同。有关详细信息，请参阅
     * <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT 线程问题</a>。
     *
     * @param processor 要从后处理器链中移除的 KeyEventPostProcessor
     * @see #addKeyEventPostProcessor
     */
    public void removeKeyEventPostProcessor(KeyEventPostProcessor processor) {
        if (processor != null) {
            synchronized (this) {
                if (keyEventPostProcessors != null) {
                    keyEventPostProcessors.remove(processor);
                }
            }
        }
    }


    /**
     * 返回此 KeyboardFocusManager 的 KeyEventPostProcessor 链作为 List。除非通过调用 <code>addKeyEventPostProcessor</code>
     * 显式添加，否则 List 不会包含此 KeyboardFocusManager。如果没有 KeyEventPostProcessors 注册，实现可以返回 null 或长度为 0 的 List。
     * 客户端代码不应假设一种行为优于另一种，也不应假设一旦建立的行为不会改变。
     *
     * @return 可能为 null 或空的 KeyEventPostProcessors 列表
     * @see #addKeyEventPostProcessor
     * @see #removeKeyEventPostProcessor
     */
    protected java.util.List<KeyEventPostProcessor>
        getKeyEventPostProcessors()
    {
        return (keyEventPostProcessors != null)
            ? (java.util.List)keyEventPostProcessors.clone()
            : null;
    }



    static void setMostRecentFocusOwner(Component component) {
        Component window = component;
        while (window != null && !(window instanceof Window)) {
            window = window.parent;
        }
        if (window != null) {
            setMostRecentFocusOwner((Window)window, component);
        }
    }
    static synchronized void setMostRecentFocusOwner(Window window,
                                                     Component component) {
        // 注意：component 通过 Component.parent 字段链对 window 有强引用。由于 WeakHashMap 对其值有强引用，
        // 我们需要断开值（component）对键（window）的强引用。
        WeakReference<Component> weakValue = null;
        if (component != null) {
            weakValue = new WeakReference<>(component);
        }
        mostRecentFocusOwners.put(window, weakValue);
    }
    static void clearMostRecentFocusOwner(Component comp) {
        Container window;


/*
 * Copyright (c) 1996, 1999, ...
 */

                    if (comp == null) {
            return;
        }

        synchronized (comp.getTreeLock()) {
            window = comp.getParent();
            while (window != null && !(window instanceof Window)) {
                window = window.getParent();
            }
        }

        synchronized (KeyboardFocusManager.class) {
            if ((window != null)
                && (getMostRecentFocusOwner((Window)window) == comp))
            {
                setMostRecentFocusOwner((Window)window, null);
            }
            // 也清除存储在 Window 中的临时丢失组件
            if (window != null) {
                Window realWindow = (Window)window;
                if (realWindow.getTemporaryLostComponent() == comp) {
                    realWindow.setTemporaryLostComponent(null);
                }
            }
        }
    }

    /*
     * 请小心更改此方法！它通过反射从 javax.swing.JComponent.runInputVerifier() 调用。
     */
    static synchronized Component getMostRecentFocusOwner(Window window) {
        WeakReference<Component> weakValue =
            (WeakReference)mostRecentFocusOwners.get(window);
        return weakValue == null ? null : (Component)weakValue.get();
    }

    /**
     * 此方法由 AWT 事件调度器调用，请求当前 KeyboardFocusManager 代表其调度指定的事件。
     * 预期所有 KeyboardFocusManagers 将调度所有 FocusEvents、所有与焦点相关的 WindowEvents 和所有 KeyEvents。
     * 这些事件应基于 KeyboardFocusManager 对焦点所有者和焦点和活动窗口的概念进行调度，有时会覆盖指定 AWTEvent 的源。
     * 必须使用 <code>redispatchEvent</code> 进行调度，以防止 AWT 事件调度器递归请求 KeyboardFocusManager
     * 再次调度事件。如果此方法返回 <code>false</code>，则 AWT 事件调度器将尝试自行调度事件。
     *
     * @param e 要调度的 AWTEvent
     * @return <code>true</code> 如果此方法调度了事件；<code>false</code> 否则
     * @see #redispatchEvent
     * @see #dispatchKeyEvent
     */
    public abstract boolean dispatchEvent(AWTEvent e);

    /**
     * 以一种方式重新调度 AWTEvent，使得 AWT 事件调度器不会递归请求 KeyboardFocusManager 或任何
     * 安装的 KeyEventDispatchers 再次调度事件。客户端实现的 <code>dispatchEvent</code> 和客户端定义的
     * KeyEventDispatchers 必须调用 <code>redispatchEvent(target, e)</code> 而不是
     * <code>target.dispatchEvent(e)</code> 来调度事件。
     * <p>
     * 此方法仅供 KeyboardFocusManagers 和 KeyEventDispatchers 使用。不供一般客户端使用。
     *
     * @param target 要调度事件的 Component
     * @param e 要调度的事件
     * @see #dispatchEvent
     * @see KeyEventDispatcher
     */
    public final void redispatchEvent(Component target, AWTEvent e) {
        e.focusManagerIsDispatching = true;
        target.dispatchEvent(e);
        e.focusManagerIsDispatching = false;
    }

    /**
     * 通常此方法将由 <code>dispatchEvent</code> 调用，如果调度器链中的其他 KeyEventDispatcher
     * 没有调度 KeyEvent，或者没有其他 KeyEventDispatchers 注册。如果此方法的实现返回 <code>false</code>，
     * <code>dispatchEvent</code> 可能会尝试自行调度 KeyEvent，或者简单地返回 <code>false</code>。
     * 如果返回 <code>true</code>，<code>dispatchEvent</code> 也应该返回 <code>true</code>。
     *
     * @param e 当前 KeyboardFocusManager 请求此 KeyEventDispatcher 调度的 KeyEvent
     * @return <code>true</code> 如果 KeyEvent 被调度；<code>false</code> 否则
     * @see #dispatchEvent
     */
    public abstract boolean dispatchKeyEvent(KeyEvent e);

    /**
     * 此方法将由 <code>dispatchKeyEvent</code> 调用。默认情况下，此方法将处理任何未被消费的 KeyEvent，
     * 如果这些 KeyEvent 映射到 AWT <code>MenuShortcut</code>，则消费该事件并激活快捷方式。
     *
     * @param e 要进行后处理的 KeyEvent
     * @return <code>true</code> 表示不会通知其他 KeyEventPostProcessor 该 KeyEvent。
     * @see #dispatchKeyEvent
     * @see MenuShortcut
     */
    public abstract boolean postProcessKeyEvent(KeyEvent e);

    /**
     * 仅当 KeyEvent 代表指定 focusedComponent 的焦点遍历键时，此方法才会发起焦点遍历操作。
     * 预期 focusedComponent 是当前焦点所有者，但不一定如此。如果不是，焦点遍历仍将像 focusedComponent
     * 是当前焦点所有者一样进行。
     *
     * @param focusedComponent 如果指定事件代表焦点遍历键，将作为焦点遍历操作的基础的 Component
     * @param e 可能代表焦点遍历键的事件
     */
    public abstract void processKeyEvent(Component focusedComponent,
                                         KeyEvent e);

    /**
     * 由 AWT 调用，通知 KeyboardFocusManager 应延迟调度 KeyEvents，直到指定 Component 成为焦点所有者。
     * 如果客户端代码请求焦点更改，且 AWT 确定此请求可能由本机窗口系统授予，则 AWT 将调用此方法。
     * 由 KeyboardFocusManager 负责延迟调度具有指定时间戳的 KeyEvents，直到指定 Component 收到 FOCUS_GAINED 事件，
     * 或 AWT 通过调用 <code>dequeueKeyEvents</code> 或 <code>discardKeyEvents</code> 取消延迟请求。
     *
     * @param after 当前事件的时间戳，或者如果当前事件没有时间戳或 AWT 无法确定正在处理的事件，则为当前系统时间
     * @param untilFocused 在任何挂起的 KeyEvents 之前应接收 FOCUS_GAINED 事件的 Component
     * @see #dequeueKeyEvents
     * @see #discardKeyEvents
     */
    protected abstract void enqueueKeyEvents(long after,
                                             Component untilFocused);

    /**
     * 由 AWT 调用，通知 KeyboardFocusManager 应取消延迟调度 KeyEvents。所有由于调用 <code>enqueueKeyEvents</code>
     * 而排队的 KeyEvents 应释放给当前焦点所有者进行正常调度。如果给定的时间戳小于零，则应取消具有 <b>最旧</b>
     * 时间戳的挂起的排队请求（如果有）。
     *
     * @param after 在调用 <code>enqueueKeyEvents</code> 时指定的时间戳，或任何值 &lt; 0
     * @param untilFocused 在调用 <code>enqueueKeyEvents</code> 时指定的 Component
     * @see #enqueueKeyEvents
     * @see #discardKeyEvents
     */
    protected abstract void dequeueKeyEvents(long after,
                                             Component untilFocused);

    /**
     * 由 AWT 调用，通知 KeyboardFocusManager 应取消延迟调度 KeyEvents。所有由于一个或多个调用
     * <code>enqueueKeyEvents</code> 而排队的 KeyEvents 应被丢弃。
     *
     * @param comp 在一个或多个调用 <code>enqueueKeyEvents</code> 时指定的 Component
     * @see #enqueueKeyEvents
     * @see #dequeueKeyEvents
     */
    protected abstract void discardKeyEvents(Component comp);

    /**
     * 通常基于 FocusTraversalPolicy 将焦点设置到 aComponent 之后的 Component。
     *
     * @param aComponent 作为焦点遍历操作基础的 Component
     * @see FocusTraversalPolicy
     */
    public abstract void focusNextComponent(Component aComponent);

    /**
     * 通常基于 FocusTraversalPolicy 将焦点设置到 aComponent 之前的 Component。
     *
     * @param aComponent 作为焦点遍历操作基础的 Component
     * @see FocusTraversalPolicy
     */
    public abstract void focusPreviousComponent(Component aComponent);

    /**
     * 将焦点上移一个焦点遍历周期。通常，焦点所有者被设置为 aComponent 的焦点周期根，当前焦点周期根被设置为
     * 新焦点所有者的焦点周期根。然而，如果 aComponent 的焦点周期根是 Window，则通常焦点所有者被设置为
     * Window 的默认 Component 要聚焦，当前焦点周期根保持不变。
     *
     * @param aComponent 作为焦点遍历操作基础的 Component
     */
    public abstract void upFocusCycle(Component aComponent);

    /**
     * 将焦点下移一个焦点遍历周期。通常，如果 aContainer 是焦点周期根，则焦点所有者被设置为 aContainer 的
     * 默认 Component 要聚焦，当前焦点周期根被设置为 aContainer。如果 aContainer 不是焦点周期根，则不进行焦点遍历操作。
     *
     * @param aContainer 作为焦点遍历操作基础的 Container
     */
    public abstract void downFocusCycle(Container aContainer);

    /**
     * 将焦点设置到当前焦点所有者之后的 Component。
     */
    public final void focusNextComponent() {
        Component focusOwner = getFocusOwner();
        if (focusOwner != null) {
            focusNextComponent(focusOwner);
        }
    }

    /**
     * 将焦点设置到当前焦点所有者之前的 Component。
     */
    public final void focusPreviousComponent() {
        Component focusOwner = getFocusOwner();
        if (focusOwner != null) {
            focusPreviousComponent(focusOwner);
        }
    }

    /**
     * 从当前焦点所有者上移一个焦点遍历周期。通常，新的焦点所有者被设置为当前焦点所有者的焦点周期根，当前焦点周期根被设置为
     * 新焦点所有者的焦点周期根。然而，如果当前焦点所有者的焦点周期根是 Window，则通常焦点所有者被设置为焦点周期根的
     * 默认 Component 要聚焦，当前焦点周期根保持不变。
     */
    public final void upFocusCycle() {
        Component focusOwner = getFocusOwner();
        if (focusOwner != null) {
            upFocusCycle(focusOwner);
        }
    }

    /**
     * 仅当当前焦点所有者是 Container 且是焦点周期根时，才从当前焦点所有者下移一个焦点遍历周期。通常，焦点所有者被设置为
     * 当前焦点所有者的默认 Component 要聚焦，当前焦点周期根被设置为当前焦点所有者。如果当前焦点所有者不是 Container
     * 且不是焦点周期根，则不进行焦点遍历操作。
     */
    public final void downFocusCycle() {
        Component focusOwner = getFocusOwner();
        if (focusOwner instanceof Container) {
            downFocusCycle((Container)focusOwner);
        }
    }

    /**
     * 将焦点请求列表转储到 stderr
     */
    void dumpRequests() {
        System.err.println(">>> Requests dump, time: " + System.currentTimeMillis());
        synchronized (heavyweightRequests) {
            for (HeavyweightFocusRequest req : heavyweightRequests) {
                System.err.println(">>> Req: " + req);
            }
        }
        System.err.println("");
    }

    private static final class LightweightFocusRequest {
        final Component component;
        final boolean temporary;
        final CausedFocusEvent.Cause cause;

        LightweightFocusRequest(Component component, boolean temporary, CausedFocusEvent.Cause cause) {
            this.component = component;
            this.temporary = temporary;
            this.cause = cause;
        }
        public String toString() {
            return "LightweightFocusRequest[component=" + component +
                ",temporary=" + temporary + ", cause=" + cause + "]";
        }
    }

    private static final class HeavyweightFocusRequest {
        final Component heavyweight;
        final LinkedList<LightweightFocusRequest> lightweightRequests;

        static final HeavyweightFocusRequest CLEAR_GLOBAL_FOCUS_OWNER =
            new HeavyweightFocusRequest();

        private HeavyweightFocusRequest() {
            heavyweight = null;
            lightweightRequests = null;
        }

        HeavyweightFocusRequest(Component heavyweight, Component descendant,
                                boolean temporary, CausedFocusEvent.Cause cause) {
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                if (heavyweight == null) {
                    log.fine("Assertion (heavyweight != null) failed");
                }
            }

            this.heavyweight = heavyweight;
            this.lightweightRequests = new LinkedList<LightweightFocusRequest>();
            addLightweightRequest(descendant, temporary, cause);
        }
        boolean addLightweightRequest(Component descendant,
                                      boolean temporary, CausedFocusEvent.Cause cause) {
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                if (this == HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER) {
                    log.fine("Assertion (this != HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER) failed");
                }
                if (descendant == null) {
                    log.fine("Assertion (descendant != null) failed");
                }
            }

            Component lastDescendant = ((lightweightRequests.size() > 0)
                ? lightweightRequests.getLast().component
                : null);

            if (descendant != lastDescendant) {
                // 不是重复请求
                lightweightRequests.add
                    (new LightweightFocusRequest(descendant, temporary, cause));
                return true;
            } else {
                return false;
            }
        }

        LightweightFocusRequest getFirstLightweightRequest() {
            if (this == CLEAR_GLOBAL_FOCUS_OWNER) {
                return null;
            }
            return lightweightRequests.getFirst();
        }
        public String toString() {
            boolean first = true;
            String str = "HeavyweightFocusRequest[heavweight=" + heavyweight +
                ",lightweightRequests=";
            if (lightweightRequests == null) {
                str += null;
            } else {
                str += "[";


    /*
     * heavyweightRequests 用作 currentLightweightRequests、clearingCurrentLightweightRequests 和
     * newFocusOwner 的同步更改的监视器。
     */
    private static LinkedList<HeavyweightFocusRequest> heavyweightRequests =
        new LinkedList<HeavyweightFocusRequest>();
    private static LinkedList<LightweightFocusRequest> currentLightweightRequests;
    private static boolean clearingCurrentLightweightRequests;
    private static boolean allowSyncFocusRequests = true;
    private static Component newFocusOwner = null;
    private static volatile boolean disableRestoreFocus;

    static final int SNFH_FAILURE = 0;
    static final int SNFH_SUCCESS_HANDLED = 1;
    static final int SNFH_SUCCESS_PROCEED = 2;

    static boolean processSynchronousLightweightTransfer(Component heavyweight, Component descendant,
                                                  boolean temporary, boolean focusedWindowChangeAllowed,
                                                  long time)
    {
        Window parentWindow = SunToolkit.getContainingWindow(heavyweight);
        if (parentWindow == null || !parentWindow.syncLWRequests) {
            return false;
        }
        if (descendant == null) {
            // 从轻量级子组件返回到重载容器的焦点转移应被视为轻量级焦点转移。
            descendant = heavyweight;
        }

        KeyboardFocusManager manager = getCurrentKeyboardFocusManager(SunToolkit.targetToAppContext(descendant));

        FocusEvent currentFocusOwnerEvent = null;
        FocusEvent newFocusOwnerEvent = null;
        Component currentFocusOwner = manager.getGlobalFocusOwner();

        synchronized (heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getLastHWRequest();
            if (hwFocusRequest == null &&
                heavyweight == manager.getNativeFocusOwner() &&
                allowSyncFocusRequests)
            {

                if (descendant == currentFocusOwner) {
                    // 重复请求。
                    return true;
                }

                // 'heavyweight' 拥有本机焦点且没有待处理的请求。'heavyweight' 必须是一个容器，且
                // 'descendant' 不能是焦点所有者。否则，我们不会到达这里。
                manager.enqueueKeyEvents(time, descendant);

                hwFocusRequest =
                    new HeavyweightFocusRequest(heavyweight, descendant,
                                                temporary, CausedFocusEvent.Cause.UNKNOWN);
                heavyweightRequests.add(hwFocusRequest);

                if (currentFocusOwner != null) {
                    currentFocusOwnerEvent =
                        new FocusEvent(currentFocusOwner,
                                       FocusEvent.FOCUS_LOST,
                                       temporary, descendant);
                }
                newFocusOwnerEvent =
                    new FocusEvent(descendant, FocusEvent.FOCUS_GAINED,
                                   temporary, currentFocusOwner);
            }
        }
        boolean result = false;
        final boolean clearing = clearingCurrentLightweightRequests;

        Throwable caughtEx = null;
        try {
            clearingCurrentLightweightRequests = false;
            synchronized(Component.LOCK) {

                if (currentFocusOwnerEvent != null && currentFocusOwner != null) {
                    ((AWTEvent) currentFocusOwnerEvent).isPosted = true;
                    caughtEx = dispatchAndCatchException(caughtEx, currentFocusOwner, currentFocusOwnerEvent);
                    result = true;
                }

                if (newFocusOwnerEvent != null && descendant != null) {
                    ((AWTEvent) newFocusOwnerEvent).isPosted = true;
                    caughtEx = dispatchAndCatchException(caughtEx, descendant, newFocusOwnerEvent);
                    result = true;
                }
            }
        } finally {
            clearingCurrentLightweightRequests = clearing;
        }
        if (caughtEx instanceof RuntimeException) {
            throw (RuntimeException)caughtEx;
        } else if (caughtEx instanceof Error) {
            throw (Error)caughtEx;
        }
        return result;
    }

    /**
     * 指示本机实现是否应继续处理挂起的本机焦点请求。在本机级别更改焦点之前，AWT 实现应始终调用此函数以获取许可。
     * 如果重复请求或指定的重载组件已经拥有焦点且没有挂起的本机焦点更改请求，此函数将拒绝请求。
     * 否则，请求将被批准，并更新焦点请求列表，以便在接收到重载组件的 FOCUS_GAINED 事件时，如果需要，将正确聚焦子组件。
     *
     * 实现必须确保对此方法的调用和本机焦点更改是原子的。如果不能保证这一点，则焦点请求列表的顺序可能不正确，从而导致类型提前机制中的错误。
     * 通常通过仅从本机事件泵送线程调用此函数，或在调用期间持有全局本机锁来实现这一点。
     */
    static int shouldNativelyFocusHeavyweight
        (Component heavyweight, Component descendant, boolean temporary,
         boolean focusedWindowChangeAllowed, long time, CausedFocusEvent.Cause cause)
    {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            if (heavyweight == null) {
                log.fine("断言 (heavyweight != null) 失败");
            }
            if (time == 0) {
                log.fine("断言 (time != 0) 失败");
            }
        }

        if (descendant == null) {
            // 从轻量级子组件返回到重载容器的焦点转移应被视为轻量级焦点转移。
            descendant = heavyweight;
        }

        KeyboardFocusManager manager =
            getCurrentKeyboardFocusManager(SunToolkit.targetToAppContext(descendant));
        KeyboardFocusManager thisManager = getCurrentKeyboardFocusManager();
        Component currentFocusOwner = thisManager.getGlobalFocusOwner();
        Component nativeFocusOwner = thisManager.getNativeFocusOwner();
        Window nativeFocusedWindow = thisManager.getNativeFocusedWindow();
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("SNFH for {0} in {1}",
                       String.valueOf(descendant), String.valueOf(heavyweight));
        }
        if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
            focusLog.finest("0. 当前焦点所有者 {0}",
                            String.valueOf(currentFocusOwner));
            focusLog.finest("0. 本机焦点所有者 {0}",
                            String.valueOf(nativeFocusOwner));
            focusLog.finest("0. 本机聚焦窗口 {0}",
                            String.valueOf(nativeFocusedWindow));
        }
        synchronized (heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getLastHWRequest();
            if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                focusLog.finest("请求 {0}", String.valueOf(hwFocusRequest));
            }
            if (hwFocusRequest == null &&
                heavyweight == nativeFocusOwner &&
                heavyweight.getContainingWindow() == nativeFocusedWindow)
            {
                if (descendant == currentFocusOwner) {
                    // 重复请求。
                    if (focusLog.isLoggable(PlatformLogger.Level.FINEST))
                        focusLog.finest("1. SNFH_FAILURE for {0}",
                                        String.valueOf(descendant));
                    return SNFH_FAILURE;
                }

                // 'heavyweight' 拥有本机焦点且没有待处理的请求。'heavyweight' 必须是一个容器，且
                // 'descendant' 不能是焦点所有者。否则，我们不会到达这里。
                manager.enqueueKeyEvents(time, descendant);

                hwFocusRequest =
                    new HeavyweightFocusRequest(heavyweight, descendant,
                                                temporary, cause);
                heavyweightRequests.add(hwFocusRequest);

                if (currentFocusOwner != null) {
                    FocusEvent currentFocusOwnerEvent =
                        new CausedFocusEvent(currentFocusOwner,
                                       FocusEvent.FOCUS_LOST,
                                       temporary, descendant, cause);
                    // 修复 5028014。已回滚。
                    // SunToolkit.postPriorityEvent(currentFocusOwnerEvent);
                    SunToolkit.postEvent(currentFocusOwner.appContext,
                                         currentFocusOwnerEvent);
                }
                FocusEvent newFocusOwnerEvent =
                    new CausedFocusEvent(descendant, FocusEvent.FOCUS_GAINED,
                                   temporary, currentFocusOwner, cause);
                // 修复 5028014。已回滚。
                // SunToolkit.postPriorityEvent(newFocusOwnerEvent);
                SunToolkit.postEvent(descendant.appContext, newFocusOwnerEvent);

                if (focusLog.isLoggable(PlatformLogger.Level.FINEST))
                    focusLog.finest("2. SNFH_HANDLED for {0}", String.valueOf(descendant));
                return SNFH_SUCCESS_HANDLED;
            } else if (hwFocusRequest != null &&
                       hwFocusRequest.heavyweight == heavyweight) {
                // 'heavyweight' 当前没有本机焦点，但如果所有待处理请求都完成，它将拥有焦点。将
                // 子组件添加到重载组件的待处理轻量级焦点转移列表中。
                if (hwFocusRequest.addLightweightRequest(descendant,
                                                         temporary, cause)) {
                    manager.enqueueKeyEvents(time, descendant);
                }

                if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                    focusLog.finest("3. SNFH_HANDLED for lightweight" +
                                    descendant + " in " + heavyweight);
                }
                return SNFH_SUCCESS_HANDLED;
            } else {
                if (!focusedWindowChangeAllowed) {
                    // 为了计算 oldFocusedWindow，我们应该查看队列中倒数第二个 HeavyweightFocusRequest，前提是
                    // 队列中的最后一个 HeavyweightFocusRequest 是 CLEAR_GLOBAL_FOCUS_OWNER。如果没有倒数第二个
                    // HeavyweightFocusRequest，null 是一个可接受的值。
                    if (hwFocusRequest ==
                        HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER)
                    {
                        int size = heavyweightRequests.size();
                        hwFocusRequest = (HeavyweightFocusRequest)((size >= 2)
                            ? heavyweightRequests.get(size - 2)
                            : null);
                    }
                    if (focusedWindowChanged(heavyweight,
                                             (hwFocusRequest != null)
                                             ? hwFocusRequest.heavyweight
                                             : nativeFocusedWindow)) {
                        if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                            focusLog.finest("4. SNFH_FAILURE for " + descendant);
                        }
                        return SNFH_FAILURE;
                    }
                }

                manager.enqueueKeyEvents(time, descendant);
                heavyweightRequests.add
                    (new HeavyweightFocusRequest(heavyweight, descendant,
                                                 temporary, cause));
                if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                    focusLog.finest("5. SNFH_PROCEED for " + descendant);
                }
                return SNFH_SUCCESS_PROCEED;
            }
        }
    }

    /**
     * 返回处理此请求后将处于活动状态的窗口，如果这是重复请求，则返回 null。活动窗口很有用，
     * 因为某些本机平台不支持将本机焦点所有者设置为 null。在这些平台上，显而易见的选择是将焦点所有者设置为活动窗口的焦点代理。
     */
    static Window markClearGlobalFocusOwner() {
        // 需要在同步块之外调用此方法以避免可能的死锁
        // 参见 6454631。
        final Component nativeFocusedWindow =
                getCurrentKeyboardFocusManager().getNativeFocusedWindow();

        synchronized (heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getLastHWRequest();
            if (hwFocusRequest ==
                HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER)
            {
                // 重复请求
                return null;
            }

            heavyweightRequests.add
                (HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER);

            Component activeWindow = ((hwFocusRequest != null)
                ? SunToolkit.getContainingWindow(hwFocusRequest.heavyweight)
                : nativeFocusedWindow);
            while (activeWindow != null &&
                   !((activeWindow instanceof Frame) ||
                     (activeWindow instanceof Dialog)))
            {
                activeWindow = activeWindow.getParent_NoClientCode();
            }

            return (Window) activeWindow;
        }
    }
    Component getCurrentWaitingRequest(Component parent) {
        synchronized (heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getFirstHWRequest();
            if (hwFocusRequest != null) {
                if (hwFocusRequest.heavyweight == parent) {
                    LightweightFocusRequest lwFocusRequest =
                        hwFocusRequest.lightweightRequests.getFirst();
                    if (lwFocusRequest != null) {
                        return lwFocusRequest.component;
                    }
                }
            }
        }
        return null;
    }

    static boolean isAutoFocusTransferEnabled() {
        synchronized (heavyweightRequests) {
            return (heavyweightRequests.size() == 0)
                    && !disableRestoreFocus
                    && (null == currentLightweightRequests);
        }
    }


                static boolean isAutoFocusTransferEnabledFor(Component comp) {
        return isAutoFocusTransferEnabled() && comp.isAutoFocusTransferOnDisposal();
    }

    /*
     * 用于处理在分发焦点事件（在 focusLost/focusGained 回调中）时发生的异常。
     * @param ex 之前捕获的异常，可能在此处处理，或为 null
     * @param comp 要分发事件的组件
     * @param event 要分发给组件的事件
     */
    static private Throwable dispatchAndCatchException(Throwable ex, Component comp, FocusEvent event) {
        Throwable retEx = null;
        try {
            comp.dispatchEvent(event);
        } catch (RuntimeException re) {
            retEx = re;
        } catch (Error er) {
            retEx = er;
        }
        if (retEx != null) {
            if (ex != null) {
                handleException(ex);
            }
            return retEx;
        }
        return ex;
    }

    static private void handleException(Throwable ex) {
        ex.printStackTrace();
    }

    static void processCurrentLightweightRequests() {
        KeyboardFocusManager manager = getCurrentKeyboardFocusManager();
        LinkedList<LightweightFocusRequest> localLightweightRequests = null;

        Component globalFocusOwner = manager.getGlobalFocusOwner();
        if ((globalFocusOwner != null) &&
            (globalFocusOwner.appContext != AppContext.getAppContext()))
        {
            // 当前的应用上下文与焦点所有者（以及所有待处理的轻量级请求）的应用上下文不同，
            // 因此我们现在什么都不做，等待下一个事件。
            return;
        }

        synchronized(heavyweightRequests) {
            if (currentLightweightRequests != null) {
                clearingCurrentLightweightRequests = true;
                disableRestoreFocus = true;
                localLightweightRequests = currentLightweightRequests;
                allowSyncFocusRequests = (localLightweightRequests.size() < 2);
                currentLightweightRequests = null;
            } else {
                // 什么都不做
                return;
            }
        }

        Throwable caughtEx = null;
        try {
            if (localLightweightRequests != null) {
                Component lastFocusOwner = null;
                Component currentFocusOwner = null;

                for (Iterator<KeyboardFocusManager.LightweightFocusRequest> iter = localLightweightRequests.iterator(); iter.hasNext(); ) {

                    currentFocusOwner = manager.getGlobalFocusOwner();
                    LightweightFocusRequest lwFocusRequest =
                        iter.next();

                    /*
                     * 警告：这仅基于 DKFM 的逻辑！
                     *
                     * 我们只在分发最后一个请求时允许触发 restoreFocus()。如果最后一个请求失败，
                     * 焦点将恢复到最后一个成功请求的组件，或恢复到此清除过程之前的焦点所有者。
                     */
                    if (!iter.hasNext()) {
                        disableRestoreFocus = false;
                    }

                    FocusEvent currentFocusOwnerEvent = null;
                    /*
                     * 当前焦点所有者为 null 时，我们不派发 FOCUS_LOST。
                     * 但无论它是否为 null，我们都会清除所有本地的轻量级请求。
                     */
                    if (currentFocusOwner != null) {
                        currentFocusOwnerEvent = new CausedFocusEvent(currentFocusOwner,
                                       FocusEvent.FOCUS_LOST,
                                       lwFocusRequest.temporary,
                                       lwFocusRequest.component, lwFocusRequest.cause);
                    }
                    FocusEvent newFocusOwnerEvent =
                        new CausedFocusEvent(lwFocusRequest.component,
                                       FocusEvent.FOCUS_GAINED,
                                       lwFocusRequest.temporary,
                                       currentFocusOwner == null ? lastFocusOwner : currentFocusOwner,
                                       lwFocusRequest.cause);

                    if (currentFocusOwner != null) {
                        ((AWTEvent) currentFocusOwnerEvent).isPosted = true;
                        caughtEx = dispatchAndCatchException(caughtEx, currentFocusOwner, currentFocusOwnerEvent);
                    }

                    ((AWTEvent) newFocusOwnerEvent).isPosted = true;
                    caughtEx = dispatchAndCatchException(caughtEx, lwFocusRequest.component, newFocusOwnerEvent);

                    if (manager.getGlobalFocusOwner() == lwFocusRequest.component) {
                        lastFocusOwner = lwFocusRequest.component;
                    }
                }
            }
        } finally {
            clearingCurrentLightweightRequests = false;
            disableRestoreFocus = false;
            localLightweightRequests = null;
            allowSyncFocusRequests = true;
        }
        if (caughtEx instanceof RuntimeException) {
            throw (RuntimeException)caughtEx;
        } else if (caughtEx instanceof Error) {
            throw (Error)caughtEx;
        }
    }

    static FocusEvent retargetUnexpectedFocusEvent(FocusEvent fe) {
        synchronized (heavyweightRequests) {
            // 任何其他情况都表示我们未预期的失败条件。我们需要清除 focusRequestList 并尽可能地修补事件。

            if (removeFirstRequest()) {
                return (FocusEvent)retargetFocusEvent(fe);
            }

            Component source = fe.getComponent();
            Component opposite = fe.getOppositeComponent();
            boolean temporary = false;
            if (fe.getID() == FocusEvent.FOCUS_LOST &&
                (opposite == null || isTemporary(opposite, source)))
            {
                temporary = true;
            }
            return new CausedFocusEvent(source, fe.getID(), temporary, opposite,
                                        CausedFocusEvent.Cause.NATIVE_SYSTEM);
        }
    }

    static FocusEvent retargetFocusGained(FocusEvent fe) {
        assert (fe.getID() == FocusEvent.FOCUS_GAINED);

        Component currentFocusOwner = getCurrentKeyboardFocusManager().
            getGlobalFocusOwner();
        Component source = fe.getComponent();
        Component opposite = fe.getOppositeComponent();
        Component nativeSource = getHeavyweight(source);

        synchronized (heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getFirstHWRequest();

            if (hwFocusRequest == HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER)
            {
                return retargetUnexpectedFocusEvent(fe);
            }

            if (source != null && nativeSource == null && hwFocusRequest != null) {
                // 如果源没有对等组件，并且
                // 源等于第一个轻量级组件
                // 那么我们应该修正源和 nativeSource
                if (source == hwFocusRequest.getFirstLightweightRequest().component)
                {
                    source = hwFocusRequest.heavyweight;
                    nativeSource = source; // 源本身就是重量级组件
                }
            }
            if (hwFocusRequest != null &&
                nativeSource == hwFocusRequest.heavyweight)
            {
                // 由于已知的 requestFocus() 调用，或已知的点击对等焦点可聚焦重量级组件导致的焦点变化。

                heavyweightRequests.removeFirst();

                LightweightFocusRequest lwFocusRequest =
                    hwFocusRequest.lightweightRequests.removeFirst();

                Component newSource = lwFocusRequest.component;
                if (currentFocusOwner != null) {
                    /*
                     * 由于当前焦点所有者不为 null 时我们接收到 FOCUS_GAINED，对应的 FOCUS_LOST 应该已经丢失。
                     * 因此，我们保留新的焦点所有者，以确定由 KeyboardFocusManager 为此次 FOCUS_GAINED 生成的合成 FOCUS_LOST 事件。
                     *
                     * 此代码基于 DefaultKeyboardFocusManager 的实现知识，可能不适用于其他 KeyboardFocusManager。
                     */
                    newFocusOwner = newSource;
                }

                boolean temporary = (opposite == null ||
                                     isTemporary(newSource, opposite))
                        ? false
                        : lwFocusRequest.temporary;

                if (hwFocusRequest.lightweightRequests.size() > 0) {
                    currentLightweightRequests =
                        hwFocusRequest.lightweightRequests;
                    EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                processCurrentLightweightRequests();
                            }
                        });
                }

                // 'opposite' 将由 DefaultKeyboardFocusManager.realOppositeComponent 修正
                return new CausedFocusEvent(newSource,
                                      FocusEvent.FOCUS_GAINED, temporary,
                                      opposite, lwFocusRequest.cause);
            }

            if (currentFocusOwner != null
                && currentFocusOwner.getContainingWindow() == source
                && (hwFocusRequest == null || source != hwFocusRequest.heavyweight))
            {
                // 顶级组件中 FOCUS_GAINED 的特殊情况
                // 如果它是由于激活而到达的，我们应该跳过它
                // 此事件将没有适当的请求记录，并且到达时已经设置了一些焦点所有者。
                return new CausedFocusEvent(currentFocusOwner, FocusEvent.FOCUS_GAINED, false,
                                            null, CausedFocusEvent.Cause.ACTIVATION);
            }

            return retargetUnexpectedFocusEvent(fe);
        } // end synchronized(heavyweightRequests)
    }

    static FocusEvent retargetFocusLost(FocusEvent fe) {
        assert (fe.getID() == FocusEvent.FOCUS_LOST);

        Component currentFocusOwner = getCurrentKeyboardFocusManager().
            getGlobalFocusOwner();
        Component opposite = fe.getOppositeComponent();
        Component nativeOpposite = getHeavyweight(opposite);

        synchronized (heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getFirstHWRequest();

            if (hwFocusRequest == HeavyweightFocusRequest.CLEAR_GLOBAL_FOCUS_OWNER)
            {
                if (currentFocusOwner != null) {
                    // 调用 KeyboardFocusManager.clearGlobalFocusOwner()
                    heavyweightRequests.removeFirst();
                    return new CausedFocusEvent(currentFocusOwner,
                                                FocusEvent.FOCUS_LOST, false, null,
                                                CausedFocusEvent.Cause.CLEAR_GLOBAL_FOCUS_OWNER);
                }

                // 否则，跳转到下面的失败情况

            } else if (opposite == null)
            {
                // 焦点离开应用程序
                if (currentFocusOwner != null) {
                    return new CausedFocusEvent(currentFocusOwner,
                                                FocusEvent.FOCUS_LOST,
                                                true, null, CausedFocusEvent.Cause.ACTIVATION);
                } else {
                    return fe;
                }
            } else if (hwFocusRequest != null &&
                       (nativeOpposite == hwFocusRequest.heavyweight ||
                        nativeOpposite == null &&
                        opposite == hwFocusRequest.getFirstLightweightRequest().component))
            {
                if (currentFocusOwner == null) {
                    return fe;
                }
                // 由于已知的 requestFocus() 调用，或点击对等焦点可聚焦重量级组件导致的焦点变化。
                // 如果焦点转移跨越顶级组件，则 FOCUS_LOST 事件总是临时的，而 FOCUS_GAINED 事件总是永久的。否则，存储的临时值将被保留。

                LightweightFocusRequest lwFocusRequest =
                    hwFocusRequest.lightweightRequests.getFirst();

                boolean temporary = isTemporary(opposite, currentFocusOwner)
                    ? true
                    : lwFocusRequest.temporary;

                return new CausedFocusEvent(currentFocusOwner, FocusEvent.FOCUS_LOST,
                                            temporary, lwFocusRequest.component, lwFocusRequest.cause);
            } else if (focusedWindowChanged(opposite, currentFocusOwner)) {
                // 如果顶级组件发生变化，列表中可能没有焦点请求
                // 但我们知道相反的组件，我们知道它是临时的 - 派发事件。
                if (!fe.isTemporary() && currentFocusOwner != null) {
                    // 创建一个事件的副本，仅临时参数不同。
                    fe = new CausedFocusEvent(currentFocusOwner, FocusEvent.FOCUS_LOST,
                                              true, opposite, CausedFocusEvent.Cause.ACTIVATION);
                }
                return fe;
            }

            return retargetUnexpectedFocusEvent(fe);
        }  // end synchronized(heavyweightRequests)
    }

    static AWTEvent retargetFocusEvent(AWTEvent event) {
        if (clearingCurrentLightweightRequests) {
            return event;
        }

        KeyboardFocusManager manager = getCurrentKeyboardFocusManager();
        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            if (event instanceof FocusEvent || event instanceof WindowEvent) {
                focusLog.finer(">>> {0}", String.valueOf(event));
            }
            if (focusLog.isLoggable(PlatformLogger.Level.FINER) && event instanceof KeyEvent) {
                focusLog.finer("    focus owner is {0}",
                               String.valueOf(manager.getGlobalFocusOwner()));
                focusLog.finer(">>> {0}", String.valueOf(event));
            }
        }

        synchronized(heavyweightRequests) {
            /*
             * 此代码处理由 DefaultKeyboardFocusManager 为 FOCUS_GAINED 生成的 FOCUS_LOST 事件。
             *
             * 此代码基于 DefaultKeyboardFocusManager 的实现知识，可能不适用于其他 KeyboardFocusManager。
             *
             * 4472032 的修复
             */
            if (newFocusOwner != null &&
                event.getID() == FocusEvent.FOCUS_LOST)
            {
                FocusEvent fe = (FocusEvent)event;


                            if (manager.getGlobalFocusOwner() == fe.getComponent() &&
                    fe.getOppositeComponent() == newFocusOwner)
                {
                    newFocusOwner = null;
                    return event;
                }
            }
        }

        processCurrentLightweightRequests();

        switch (event.getID()) {
            case FocusEvent.FOCUS_GAINED: {
                event = retargetFocusGained((FocusEvent)event);
                break;
            }
            case FocusEvent.FOCUS_LOST: {
                event = retargetFocusLost((FocusEvent)event);
                break;
            }
            default:
                /* do nothing */
        }
        return event;
    }

    /**
     * 清除标记队列
     * 此方法不打算被 KFM 覆盖。
     * 只有 DefaultKeyboardFocusManager 可以实现它。
     * @since 1.5
     */
    void clearMarkers() {
    }

    static boolean removeFirstRequest() {
        KeyboardFocusManager manager =
            KeyboardFocusManager.getCurrentKeyboardFocusManager();

        synchronized(heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getFirstHWRequest();

            if (hwFocusRequest != null) {
                heavyweightRequests.removeFirst();
                if (hwFocusRequest.lightweightRequests != null) {
                    for (Iterator<KeyboardFocusManager.LightweightFocusRequest> lwIter = hwFocusRequest.lightweightRequests.
                             iterator();
                         lwIter.hasNext(); )
                    {
                        manager.dequeueKeyEvents
                            (-1, lwIter.next().
                             component);
                    }
                }
            }
            // 修复 4799136 - 如果请求队列为空，则清除预输入标记
            // 我们在这里这样做是因为只有在出现问题时才会调用此方法
            if (heavyweightRequests.size() == 0) {
                manager.clearMarkers();
            }
            return (heavyweightRequests.size() > 0);
        }
    }
    static void removeLastFocusRequest(Component heavyweight) {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            if (heavyweight == null) {
                log.fine("断言 (heavyweight != null) 失败");
            }
        }

        KeyboardFocusManager manager =
            KeyboardFocusManager.getCurrentKeyboardFocusManager();
        synchronized(heavyweightRequests) {
            HeavyweightFocusRequest hwFocusRequest = getLastHWRequest();
            if (hwFocusRequest != null &&
                hwFocusRequest.heavyweight == heavyweight) {
                heavyweightRequests.removeLast();
            }
            // 修复 4799136 - 如果请求队列为空，则清除预输入标记
            // 我们在这里这样做是因为只有在出现问题时才会调用此方法
            if (heavyweightRequests.size() == 0) {
                manager.clearMarkers();
            }
        }
    }

    private static boolean focusedWindowChanged(Component to, Component from) {
        Window wto = SunToolkit.getContainingWindow(to);
        Window wfrom = SunToolkit.getContainingWindow(from);
        if (wto == null && wfrom == null) {
            return true;
        }
        if (wto == null) {
            return true;
        }
        if (wfrom == null) {
            return true;
        }
        return (wto != wfrom);
    }

    private static boolean isTemporary(Component to, Component from) {
        Window wto = SunToolkit.getContainingWindow(to);
        Window wfrom = SunToolkit.getContainingWindow(from);
        if (wto == null && wfrom == null) {
            return false;
        }
        if (wto == null) {
            return true;
        }
        if (wfrom == null) {
            return false;
        }
        return (wto != wfrom);
    }

    static Component getHeavyweight(Component comp) {
        if (comp == null || comp.getPeer() == null) {
            return null;
        } else if (comp.getPeer() instanceof LightweightPeer) {
            return comp.getNativeContainer();
        } else {
            return comp;
        }
    }

    static Field proxyActive;
    // 访问 KeyEvent 的私有字段 isProxyActive
    private static boolean isProxyActiveImpl(KeyEvent e) {
        if (proxyActive == null) {
            proxyActive =  AccessController.doPrivileged(new PrivilegedAction<Field>() {
                    public Field run() {
                        Field field = null;
                        try {
                            field = KeyEvent.class.getDeclaredField("isProxyActive");
                            if (field != null) {
                                field.setAccessible(true);
                            }
                        } catch (NoSuchFieldException nsf) {
                            assert(false);
                        }
                        return field;
                    }
                });
        }

        try {
            return proxyActive.getBoolean(e);
        } catch (IllegalAccessException iae) {
            assert(false);
        }
        return false;
    }

    // 返回此 KeyEvent 的字段 isProxyActive 的值
    static boolean isProxyActive(KeyEvent e) {
        if (!GraphicsEnvironment.isHeadless()) {
            return isProxyActiveImpl(e);
        } else {
            return false;
        }
    }

    private static HeavyweightFocusRequest getLastHWRequest() {
        synchronized(heavyweightRequests) {
            return (heavyweightRequests.size() > 0)
                ? heavyweightRequests.getLast()
                : null;
        }
    }

    private static HeavyweightFocusRequest getFirstHWRequest() {
        synchronized(heavyweightRequests) {
            return (heavyweightRequests.size() > 0)
                ? heavyweightRequests.getFirst()
                : null;
        }
    }

    private static void checkReplaceKFMPermission()
        throws SecurityException
    {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            if (replaceKeyboardFocusManagerPermission == null) {
                replaceKeyboardFocusManagerPermission =
                    new AWTPermission("replaceKeyboardFocusManager");
            }
            security.
                checkPermission(replaceKeyboardFocusManagerPermission);
        }
    }

    // 检查此 KeyboardFocusManager 实例是否是当前的 KFM，
    // 或者检查调用线程是否有 "replaceKeyboardFocusManager" 权限。这样做的理由是：
    //
    // 系统 KFM 实例（默认情况下是当前 KFM）可能在客户端代码在调用堆栈下时没有
    // "replaceKFM" 权限，但由于系统 KFM 是可信的（并且像“特权”一样），它仍然应该能够
    // 执行受此检查保护的方法。
    //
    // 如果此 KFM 实例不是当前 KFM 但客户端代码具有所有权限，我们不能抛出 SecurityException，
    // 因为这会与安全概念相矛盾。在这种情况下，可信的客户端代码负责从不是当前的 KFM 实例调用安全方法。
    private void checkKFMSecurity()
        throws SecurityException
    {
        if (this != getCurrentKeyboardFocusManager()) {
            checkReplaceKFMPermission();
        }
    }
}
