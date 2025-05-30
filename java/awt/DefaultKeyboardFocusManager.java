
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
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.peer.ComponentPeer;
import java.awt.peer.LightweightPeer;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.CausedFocusEvent;
import sun.awt.SunToolkit;
import sun.awt.TimedWindowEvent;
import sun.util.logging.PlatformLogger;

/**
 * AWT 应用程序的默认键盘焦点管理器。焦点遍历是根据组件的焦点遍历键和容器的焦点遍历策略来完成的。
 * <p>
 * 请参阅
 * <a href="https://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html">
 * 如何使用焦点子系统</a>，
 * 《Java 教程》中的一个部分，以及
 * <a href="../../java/awt/doc-files/FocusSpec.html">焦点规范</a>
 * 以获取更多信息。
 *
 * @author David Mendenhall
 *
 * @see FocusTraversalPolicy
 * @see Component#setFocusTraversalKeys
 * @see Component#getFocusTraversalKeys
 * @since 1.4
 */
public class DefaultKeyboardFocusManager extends KeyboardFocusManager {
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("java.awt.focus.DefaultKeyboardFocusManager");

    // null 弱引用以避免创建过多对象
    private static final WeakReference<Window> NULL_WINDOW_WR =
        new WeakReference<Window>(null);
    private static final WeakReference<Component> NULL_COMPONENT_WR =
        new WeakReference<Component>(null);
    private WeakReference<Window> realOppositeWindowWR = NULL_WINDOW_WR;
    private WeakReference<Component> realOppositeComponentWR = NULL_COMPONENT_WR;
    private int inSendMessage;
    private LinkedList<KeyEvent> enqueuedKeyEvents = new LinkedList<KeyEvent>();
    private LinkedList<TypeAheadMarker> typeAheadMarkers = new LinkedList<TypeAheadMarker>();
    private boolean consumeNextKeyTyped;
    private Component restoreFocusTo;

    static {
        AWTAccessor.setDefaultKeyboardFocusManagerAccessor(
            new AWTAccessor.DefaultKeyboardFocusManagerAccessor() {
                public void consumeNextKeyTyped(DefaultKeyboardFocusManager dkfm, KeyEvent e) {
                    dkfm.consumeNextKeyTyped(e);
                }
            });
    }

    private static class TypeAheadMarker {
        long after;
        Component untilFocused;

        TypeAheadMarker(long after, Component untilFocused) {
            this.after = after;
            this.untilFocused = untilFocused;
        }
        /**
         * 返回标记的字符串表示形式
         */
        public String toString() {
            return ">>> Marker after " + after + " on " + untilFocused;
        }
    }

    private Window getOwningFrameDialog(Window window) {
        while (window != null && !(window instanceof Frame ||
                                   window instanceof Dialog)) {
            window = (Window)window.getParent();
        }
        return window;
    }

    /*
     * 这一系列的恢复焦点方法用于从拒绝的焦点或激活更改中恢复。拒绝通常发生在用户尝试聚焦不可聚焦的组件或窗口时。
     */
    private void restoreFocus(FocusEvent fe, Window newFocusedWindow) {
        Component realOppositeComponent = this.realOppositeComponentWR.get();
        Component vetoedComponent = fe.getComponent();

        if (newFocusedWindow != null && restoreFocus(newFocusedWindow,
                                                     vetoedComponent, false))
        {
        } else if (realOppositeComponent != null &&
                   doRestoreFocus(realOppositeComponent, vetoedComponent, false)) {
        } else if (fe.getOppositeComponent() != null &&
                   doRestoreFocus(fe.getOppositeComponent(), vetoedComponent, false)) {
        } else {
            clearGlobalFocusOwnerPriv();
        }
    }
    private void restoreFocus(WindowEvent we) {
        Window realOppositeWindow = this.realOppositeWindowWR.get();
        if (realOppositeWindow != null
            && restoreFocus(realOppositeWindow, null, false))
        {
            // 什么都不做，所有操作都在 restoreFocus() 中完成
        } else if (we.getOppositeWindow() != null &&
                   restoreFocus(we.getOppositeWindow(), null, false))
        {
            // 什么都不做，所有操作都在 restoreFocus() 中完成
        } else {
            clearGlobalFocusOwnerPriv();
        }
    }
    private boolean restoreFocus(Window aWindow, Component vetoedComponent,
                                 boolean clearOnFailure) {
        restoreFocusTo = null;
        Component toFocus =
            KeyboardFocusManager.getMostRecentFocusOwner(aWindow);

        if (toFocus != null && toFocus != vetoedComponent) {
            if (getHeavyweight(aWindow) != getNativeFocusOwner()) {
                // 无法同步恢复焦点
                if (!toFocus.isShowing() || !toFocus.canBeFocusOwner()) {
                    toFocus = toFocus.getNextFocusCandidate();
                }
                if (toFocus != null && toFocus != vetoedComponent) {
                    if (!toFocus.requestFocus(false,
                                                   CausedFocusEvent.Cause.ROLLBACK)) {
                        restoreFocusTo = toFocus;
                    }
                    return true;
                }
            } else if (doRestoreFocus(toFocus, vetoedComponent, false)) {
                return true;
            }
        }
        if (clearOnFailure) {
            clearGlobalFocusOwnerPriv();
            return true;
        } else {
            return false;
        }
    }
    private boolean restoreFocus(Component toFocus, boolean clearOnFailure) {
        return doRestoreFocus(toFocus, null, clearOnFailure);
    }
    private boolean doRestoreFocus(Component toFocus, Component vetoedComponent,
                                   boolean clearOnFailure)
    {
        boolean success = true;
        if (toFocus != vetoedComponent && toFocus.isShowing() && toFocus.canBeFocusOwner() &&
            (success = toFocus.requestFocus(false, CausedFocusEvent.Cause.ROLLBACK)))
        {
            return true;
        } else {
            if (!success && getGlobalFocusedWindow() != SunToolkit.getContainingWindow(toFocus)) {
                restoreFocusTo = toFocus;
                return true;
            }
            Component nextFocus = toFocus.getNextFocusCandidate();
            if (nextFocus != null && nextFocus != vetoedComponent &&
                nextFocus.requestFocusInWindow(CausedFocusEvent.Cause.ROLLBACK))
            {
                return true;
            } else if (clearOnFailure) {
                clearGlobalFocusOwnerPriv();
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 一个特殊的 SentEvent 类型，如果目标 KeyboardFocusManager 是 DefaultKeyboardFocusManager 的实例，
     * 则更新计数器。
     */
    private static class DefaultKeyboardFocusManagerSentEvent
        extends SentEvent
    {
        /*
         * 序列化版本 ID
         */
        private static final long serialVersionUID = -2924743257508701758L;

        public DefaultKeyboardFocusManagerSentEvent(AWTEvent nested,
                                                    AppContext toNotify) {
            super(nested, toNotify);
        }
        public final void dispatch() {
            KeyboardFocusManager manager =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
            DefaultKeyboardFocusManager defaultManager =
                (manager instanceof DefaultKeyboardFocusManager)
                ? (DefaultKeyboardFocusManager)manager
                : null;

            if (defaultManager != null) {
                synchronized (defaultManager) {
                    defaultManager.inSendMessage++;
                }
            }

            super.dispatch();

            if (defaultManager != null) {
                synchronized (defaultManager) {
                    defaultManager.inSendMessage--;
                }
            }
        }
    }

    /**
     * 向组件发送一个合成的 AWTEvent。如果组件在当前 AppContext 中，则事件立即分发。
     * 如果组件在不同的 AppContext 中，则事件将被发布到其他 AppContext 的 EventQueue，
     * 并且此方法将阻塞，直到事件被处理或目标 AppContext 被释放。
     * 如果成功分发事件，则返回 true，否则返回 false。
     */
    static boolean sendMessage(Component target, AWTEvent e) {
        e.isPosted = true;
        AppContext myAppContext = AppContext.getAppContext();
        final AppContext targetAppContext = target.appContext;
        final SentEvent se =
            new DefaultKeyboardFocusManagerSentEvent(e, myAppContext);

        if (myAppContext == targetAppContext) {
            se.dispatch();
        } else {
            if (targetAppContext.isDisposed()) {
                return false;
            }
            SunToolkit.postEvent(targetAppContext, se);
            if (EventQueue.isDispatchThread()) {
                EventDispatchThread edt = (EventDispatchThread)
                    Thread.currentThread();
                edt.pumpEvents(SentEvent.ID, new Conditional() {
                        public boolean evaluate() {
                            return !se.dispatched && !targetAppContext.isDisposed();
                        }
                    });
            } else {
                synchronized (se) {
                    while (!se.dispatched && !targetAppContext.isDisposed()) {
                        try {
                            se.wait(1000);
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                }
            }
        }
        return se.dispatched;
    }

    /*
     * 检查焦点窗口事件是否跟随在类型提前队列中等待的键事件（如果有）。这可能发生在用户在窗口中提前输入，
     * 客户端监听器挂起 EDT 一段时间，用户在顶级窗口之间切换的情况下。在这种情况下，焦点窗口事件可能在类型提前事件
     * 被处理之前被分发。这可能导致错误的焦点行为，为了避免这种情况，焦点窗口事件将被重新发布到事件队列的末尾。
     * 参见 6981400。
     */
    private boolean repostIfFollowsKeyEvents(WindowEvent e) {
        if (!(e instanceof TimedWindowEvent)) {
            return false;
        }
        TimedWindowEvent we = (TimedWindowEvent)e;
        long time = we.getWhen();
        synchronized (this) {
            KeyEvent ke = enqueuedKeyEvents.isEmpty() ? null : enqueuedKeyEvents.getFirst();
            if (ke != null && time >= ke.getWhen()) {
                TypeAheadMarker marker = typeAheadMarkers.isEmpty() ? null : typeAheadMarkers.getFirst();
                if (marker != null) {
                    Window toplevel = marker.untilFocused.getContainingWindow();
                    // 检查等待焦点的组件是否属于当前聚焦的窗口。参见 8015454。
                    if (toplevel != null && toplevel.isFocused()) {
                        SunToolkit.postEvent(AppContext.getAppContext(), new SequencedEvent(e));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 当 AWT 事件调度器请求当前 KeyboardFocusManager 代表其分发指定的事件时调用此方法。
     * DefaultKeyboardFocusManagers 分发所有 FocusEvents、所有与焦点相关的 WindowEvents 和所有 KeyEvents。
     * 这些事件基于 KeyboardFocusManager 的焦点所有者和聚焦和活动窗口的概念进行分发，有时会覆盖指定 AWTEvent 的源。
     * 如果此方法返回 <code>false</code>，则 AWT 事件调度器将尝试自行分发事件。
     *
     * @param e 要分发的 AWTEvent
     * @return 如果此方法分发了事件，则返回 <code>true</code>；否则返回 <code>false</code>
     */
    public boolean dispatchEvent(AWTEvent e) {
        if (focusLog.isLoggable(PlatformLogger.Level.FINE) && (e instanceof WindowEvent || e instanceof FocusEvent)) {
            focusLog.fine("" + e);
        }
        switch (e.getID()) {
            case WindowEvent.WINDOW_GAINED_FOCUS: {
                if (repostIfFollowsKeyEvents((WindowEvent)e)) {
                    break;
                }

                WindowEvent we = (WindowEvent)e;
                Window oldFocusedWindow = getGlobalFocusedWindow();
                Window newFocusedWindow = we.getWindow();
                if (newFocusedWindow == oldFocusedWindow) {
                    break;
                }

                if (!(newFocusedWindow.isFocusableWindow()
                      && newFocusedWindow.isVisible()
                      && newFocusedWindow.isDisplayable()))
                {
                    // 无法接受此类窗口的焦点，因此拒绝它。
                    restoreFocus(we);
                    break;
                }
                // 如果存在当前聚焦的窗口，则通知它已失去焦点。
                if (oldFocusedWindow != null) {
                    boolean isEventDispatched =
                        sendMessage(oldFocusedWindow,
                                new WindowEvent(oldFocusedWindow,
                                                WindowEvent.WINDOW_LOST_FOCUS,
                                                newFocusedWindow));
                    // 分发失败，自行清除
                    if (!isEventDispatched) {
                        setGlobalFocusOwner(null);
                        setGlobalFocusedWindow(null);
                    }
                }


                            // 因为原生库不会发布 WINDOW_ACTIVATED
                // 事件，所以我们需要在活动窗口发生变化时合成一个。
                Window newActiveWindow =
                    getOwningFrameDialog(newFocusedWindow);
                Window currentActiveWindow = getGlobalActiveWindow();
                if (newActiveWindow != currentActiveWindow) {
                    sendMessage(newActiveWindow,
                                new WindowEvent(newActiveWindow,
                                                WindowEvent.WINDOW_ACTIVATED,
                                                currentActiveWindow));
                    if (newActiveWindow != getGlobalActiveWindow()) {
                        // 激活变更被拒绝。虽然不太可能，但
                        // 仍有可能。
                        restoreFocus(we);
                        break;
                    }
                }

                setGlobalFocusedWindow(newFocusedWindow);

                if (newFocusedWindow != getGlobalFocusedWindow()) {
                    // 焦点变更被拒绝。如果
                    // newFocusedWindow 不是可聚焦窗口，就会发生这种情况。
                    restoreFocus(we);
                    break;
                }

                // 恢复最后持有焦点的组件的焦点。我们在这里这样做，以便客户端代码可以在
                // WINDOW_GAINED_FOCUS 处理程序中覆盖我们的选择。
                //
                // 确保焦点变更请求不会改变焦点窗口，以防在处理请求时我们不再是焦点窗口。
                if (inSendMessage == 0) {
                    // 确定窗口中最初应获得焦点的组件。
                    //
                    // * 如果我们在 SendMessage 中，那么这是一个由 FOCUS_GAINED 处理程序生成的
                    //   合成的 WINDOW_GAINED_FOCUS 消息。允许 FOCUS_GAINED 消息的目标组件
                    //   获得焦点。
                    // * 否则，在这里查找正确的组件。
                    //   我们不使用 Window.getMostRecentFocusOwner，因为窗口现在是焦点窗口，将返回 'null'。

                    // 最近的焦点所有者和焦点请求的计算应该同步在 KeyboardFocusManager.class 上
                    // 以防止用户在计算和我们的请求之间请求焦点时发生线程竞争。
                    // 但如果焦点转移是同步的，这种同步可能会导致死锁，因此我们不在此块中同步。
                    Component toFocus = KeyboardFocusManager.
                        getMostRecentFocusOwner(newFocusedWindow);
                    boolean isFocusRestore = restoreFocusTo != null &&
                                                      toFocus == restoreFocusTo;
                    if ((toFocus == null) &&
                        newFocusedWindow.isFocusableWindow())
                    {
                        toFocus = newFocusedWindow.getFocusTraversalPolicy().
                            getInitialComponent(newFocusedWindow);
                    }
                    Component tempLost = null;
                    synchronized(KeyboardFocusManager.class) {
                        tempLost = newFocusedWindow.setTemporaryLostComponent(null);
                    }

                    // 当窗口获得焦点时，最后持有焦点的组件应该首先获得焦点
                    if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                        focusLog.finer("tempLost {0}, toFocus {1}",
                                       tempLost, toFocus);
                    }
                    if (tempLost != null) {
                        tempLost.requestFocusInWindow(
                                    isFocusRestore && tempLost == toFocus ?
                                                CausedFocusEvent.Cause.ROLLBACK :
                                                CausedFocusEvent.Cause.ACTIVATION);
                    }

                    if (toFocus != null && toFocus != tempLost) {
                        // 如果在窗口非活动时有组件请求焦点，它期望在激活后获得焦点。
                        toFocus.requestFocusInWindow(CausedFocusEvent.Cause.ACTIVATION);
                    }
                }
                restoreFocusTo = null;

                Window realOppositeWindow = this.realOppositeWindowWR.get();
                if (realOppositeWindow != we.getOppositeWindow()) {
                    we = new WindowEvent(newFocusedWindow,
                                         WindowEvent.WINDOW_GAINED_FOCUS,
                                         realOppositeWindow);
                }
                return typeAheadAssertions(newFocusedWindow, we);
            }

            case WindowEvent.WINDOW_ACTIVATED: {
                WindowEvent we = (WindowEvent)e;
                Window oldActiveWindow = getGlobalActiveWindow();
                Window newActiveWindow = we.getWindow();
                if (oldActiveWindow == newActiveWindow) {
                    break;
                }

                // 如果存在当前活动窗口，则通知它已失去激活。
                if (oldActiveWindow != null) {
                    boolean isEventDispatched =
                        sendMessage(oldActiveWindow,
                                new WindowEvent(oldActiveWindow,
                                                WindowEvent.WINDOW_DEACTIVATED,
                                                newActiveWindow));
                    // 派发失败，自行清除
                    if (!isEventDispatched) {
                        setGlobalActiveWindow(null);
                    }
                    if (getGlobalActiveWindow() != null) {
                        // 激活变更被拒绝。虽然不太可能，但
                        // 仍有可能。
                        break;
                    }
                }

                setGlobalActiveWindow(newActiveWindow);

                if (newActiveWindow != getGlobalActiveWindow()) {
                    // 激活变更被拒绝。虽然不太可能，但
                    // 仍有可能。
                    break;
                }

                return typeAheadAssertions(newActiveWindow, we);
            }

            case FocusEvent.FOCUS_GAINED: {
                restoreFocusTo = null;
                FocusEvent fe = (FocusEvent)e;
                CausedFocusEvent.Cause cause = (fe instanceof CausedFocusEvent) ?
                    ((CausedFocusEvent)fe).getCause() : CausedFocusEvent.Cause.UNKNOWN;
                Component oldFocusOwner = getGlobalFocusOwner();
                Component newFocusOwner = fe.getComponent();
                if (oldFocusOwner == newFocusOwner) {
                    if (focusLog.isLoggable(PlatformLogger.Level.FINE)) {
                        focusLog.fine("Skipping {0} because focus owner is the same", e);
                    }
                    // 我们不能简单地丢弃事件——可能有关联的预输入标记。
                    dequeueKeyEvents(-1, newFocusOwner);
                    break;
                }

                // 如果存在当前焦点所有者，则通知它已失去焦点。
                if (oldFocusOwner != null) {
                    boolean isEventDispatched =
                        sendMessage(oldFocusOwner,
                                    new CausedFocusEvent(oldFocusOwner,
                                                   FocusEvent.FOCUS_LOST,
                                                   fe.isTemporary(),
                                                   newFocusOwner, cause));
                    // 派发失败，自行清除
                    if (!isEventDispatched) {
                        setGlobalFocusOwner(null);
                        if (!fe.isTemporary()) {
                            setGlobalPermanentFocusOwner(null);
                        }
                    }
                }

                // 由于原生窗口系统对当前焦点和激活状态有不同的概念，可能会有
                // 一个不在焦点窗口内的组件接收到 FOCUS_GAINED 事件。在这种情况下，我们合成一个 WINDOW_GAINED_FOCUS
                // 事件。
                final Window newFocusedWindow = SunToolkit.getContainingWindow(newFocusOwner);
                final Window currentFocusedWindow = getGlobalFocusedWindow();
                if (newFocusedWindow != null &&
                    newFocusedWindow != currentFocusedWindow)
                {
                    sendMessage(newFocusedWindow,
                                new WindowEvent(newFocusedWindow,
                                        WindowEvent.WINDOW_GAINED_FOCUS,
                                                currentFocusedWindow));
                    if (newFocusedWindow != getGlobalFocusedWindow()) {
                        // 焦点变更被拒绝。如果
                        // newFocusedWindow 不是可聚焦窗口，就会发生这种情况。

                        // 需要恢复预输入，但不恢复焦点。这已经在
                        // WINDOW_GAINED_FOCUS 处理程序中完成。
                        dequeueKeyEvents(-1, newFocusOwner);
                        break;
                    }
                }

                if (!(newFocusOwner.isFocusable() && newFocusOwner.isShowing() &&
                    // 如果焦点事件不是由于直接请求（而是遍历、激活或系统生成）导致的，拒绝禁用组件的焦点。
                    (newFocusOwner.isEnabled() || cause.equals(CausedFocusEvent.Cause.UNKNOWN))))
                {
                    // 我们不应该接受此类组件的焦点，因此拒绝它。
                    dequeueKeyEvents(-1, newFocusOwner);
                    if (KeyboardFocusManager.isAutoFocusTransferEnabled()) {
                        // 如果 FOCUS_GAINED 是针对已处置的组件（尽管不应发生），其顶级父组件为 null。在这种情况下，我们必须尝试在当前焦点窗口中恢复焦点（详情参见：6607170）。
                        if (newFocusedWindow == null) {
                            restoreFocus(fe, currentFocusedWindow);
                        } else {
                            restoreFocus(fe, newFocusedWindow);
                        }
                        setMostRecentFocusOwner(newFocusedWindow, null); // 参见：8013773
                    }
                    break;
                }

                setGlobalFocusOwner(newFocusOwner);

                if (newFocusOwner != getGlobalFocusOwner()) {
                    // 焦点变更被拒绝。如果
                    // newFocusOwner 不是可遍历焦点的。
                    dequeueKeyEvents(-1, newFocusOwner);
                    if (KeyboardFocusManager.isAutoFocusTransferEnabled()) {
                        restoreFocus(fe, (Window)newFocusedWindow);
                    }
                    break;
                }

                if (!fe.isTemporary()) {
                    setGlobalPermanentFocusOwner(newFocusOwner);

                    if (newFocusOwner != getGlobalPermanentFocusOwner()) {
                        // 焦点变更被拒绝。虽然不太可能，但
                        // 仍有可能。
                        dequeueKeyEvents(-1, newFocusOwner);
                        if (KeyboardFocusManager.isAutoFocusTransferEnabled()) {
                            restoreFocus(fe, (Window)newFocusedWindow);
                        }
                        break;
                    }
                }

                setNativeFocusOwner(getHeavyweight(newFocusOwner));

                Component realOppositeComponent = this.realOppositeComponentWR.get();
                if (realOppositeComponent != null &&
                    realOppositeComponent != fe.getOppositeComponent()) {
                    fe = new CausedFocusEvent(newFocusOwner,
                                        FocusEvent.FOCUS_GAINED,
                                        fe.isTemporary(),
                                        realOppositeComponent, cause);
                    ((AWTEvent) fe).isPosted = true;
                }
                return typeAheadAssertions(newFocusOwner, fe);
            }

            case FocusEvent.FOCUS_LOST: {
                FocusEvent fe = (FocusEvent)e;
                Component currentFocusOwner = getGlobalFocusOwner();
                if (currentFocusOwner == null) {
                    if (focusLog.isLoggable(PlatformLogger.Level.FINE))
                        focusLog.fine("Skipping {0} because focus owner is null", e);
                    break;
                }
                // 忽略组件失去焦点给自己的情况。
                // 如果由于重定向而犯错，FOCUS_GAINED 处理程序将纠正它。
                if (currentFocusOwner == fe.getOppositeComponent()) {
                    if (focusLog.isLoggable(PlatformLogger.Level.FINE))
                        focusLog.fine("Skipping {0} because current focus owner is equal to opposite", e);
                    break;
                }

                setGlobalFocusOwner(null);

                if (getGlobalFocusOwner() != null) {
                    // 焦点变更被拒绝。虽然不太可能，但
                    // 仍有可能。
                    restoreFocus(currentFocusOwner, true);
                    break;
                }

                if (!fe.isTemporary()) {
                    setGlobalPermanentFocusOwner(null);

                    if (getGlobalPermanentFocusOwner() != null) {
                        // 焦点变更被拒绝。虽然不太可能，但
                        // 仍有可能。
                        restoreFocus(currentFocusOwner, true);
                        break;
                    }
                } else {
                    Window owningWindow = currentFocusOwner.getContainingWindow();
                    if (owningWindow != null) {
                        owningWindow.setTemporaryLostComponent(currentFocusOwner);
                    }
                }

                setNativeFocusOwner(null);

                fe.setSource(currentFocusOwner);

                realOppositeComponentWR = (fe.getOppositeComponent() != null)
                    ? new WeakReference<Component>(currentFocusOwner)
                    : NULL_COMPONENT_WR;


                            return typeAheadAssertions(currentFocusOwner, fe);
            }

            case WindowEvent.WINDOW_DEACTIVATED: {
                WindowEvent we = (WindowEvent)e;
                Window currentActiveWindow = getGlobalActiveWindow();
                if (currentActiveWindow == null) {
                    break;
                }

                if (currentActiveWindow != e.getSource()) {
                    // 事件在时间中丢失。
                    // 允许监听器处理事件，但不要更改任何全局状态。
                    break;
                }

                setGlobalActiveWindow(null);
                if (getGlobalActiveWindow() != null) {
                    // 激活更改被拒绝。不太可能，但有可能。
                    break;
                }

                we.setSource(currentActiveWindow);
                return typeAheadAssertions(currentActiveWindow, we);
            }

            case WindowEvent.WINDOW_LOST_FOCUS: {
                if (repostIfFollowsKeyEvents((WindowEvent)e)) {
                    break;
                }

                WindowEvent we = (WindowEvent)e;
                Window currentFocusedWindow = getGlobalFocusedWindow();
                Window losingFocusWindow = we.getWindow();
                Window activeWindow = getGlobalActiveWindow();
                Window oppositeWindow = we.getOppositeWindow();
                if (focusLog.isLoggable(PlatformLogger.Level.FINE))
                    focusLog.fine("激活 {0}, 当前聚焦 {1}, 失去焦点 {2} 对面 {3}",
                                  activeWindow, currentFocusedWindow,
                                  losingFocusWindow, oppositeWindow);
                if (currentFocusedWindow == null) {
                    break;
                }

                // 特殊情况 -- 如果原生窗口系统发布一个事件，声称活动窗口失去了焦点到聚焦窗口，
                // 则丢弃该事件。这是原生窗口系统不知道哪个窗口真正聚焦的结果。
                if (inSendMessage == 0 && losingFocusWindow == activeWindow &&
                    oppositeWindow == currentFocusedWindow)
                {
                    break;
                }

                Component currentFocusOwner = getGlobalFocusOwner();
                if (currentFocusOwner != null) {
                    // 焦点所有者在窗口失去焦点之前应始终收到一个 FOCUS_LOST 事件。
                    Component oppositeComp = null;
                    if (oppositeWindow != null) {
                        oppositeComp = oppositeWindow.getTemporaryLostComponent();
                        if (oppositeComp == null) {
                            oppositeComp = oppositeWindow.getMostRecentFocusOwner();
                        }
                    }
                    if (oppositeComp == null) {
                        oppositeComp = oppositeWindow;
                    }
                    sendMessage(currentFocusOwner,
                                new CausedFocusEvent(currentFocusOwner,
                                               FocusEvent.FOCUS_LOST,
                                               true,
                                               oppositeComp, CausedFocusEvent.Cause.ACTIVATION));
                }

                setGlobalFocusedWindow(null);
                if (getGlobalFocusedWindow() != null) {
                    // 焦点更改被拒绝。不太可能，但有可能。
                    restoreFocus(currentFocusedWindow, null, true);
                    break;
                }

                we.setSource(currentFocusedWindow);
                realOppositeWindowWR = (oppositeWindow != null)
                    ? new WeakReference<Window>(currentFocusedWindow)
                    : NULL_WINDOW_WR;
                typeAheadAssertions(currentFocusedWindow, we);

                if (oppositeWindow == null && activeWindow != null) {
                    // 然后我们还需要取消激活活动窗口。
                    // 在其他情况下不需要合成，因为如果必要，WINDOW_ACTIVATED 会处理它。
                    sendMessage(activeWindow,
                                new WindowEvent(activeWindow,
                                                WindowEvent.WINDOW_DEACTIVATED,
                                                null));
                    if (getGlobalActiveWindow() != null) {
                        // 激活更改被拒绝。不太可能，
                        // 但有可能。
                        restoreFocus(currentFocusedWindow, null, true);
                    }
                }
                break;
            }

            case KeyEvent.KEY_TYPED:
            case KeyEvent.KEY_PRESSED:
            case KeyEvent.KEY_RELEASED:
                return typeAheadAssertions(null, e);

            default:
                return false;
        }

        return true;
    }

    /**
     * 由 <code>dispatchEvent</code> 调用，如果调度链中的其他
     * KeyEventDispatcher 没有调度 KeyEvent，或者没有其他 KeyEventDispatchers 注册。
     * 如果事件未被消耗，其目标已启用，且焦点所有者不为空，
     * 该方法将事件调度到其目标。此方法还将随后将事件调度到所有注册的
     * KeyEventPostProcessors。所有这些操作完成后，
     * 事件将传递给对等对象进行处理。
     * <p>
     * 在所有情况下，此方法返回 <code>true</code>，因为
     * DefaultKeyboardFocusManager 的设计使得无论是
     * <code>dispatchEvent</code> 还是 AWT 事件调度器，在任何情况下都不应进一步处理事件。
     *
     * @param e 要调度的 KeyEvent
     * @return <code>true</code>
     * @see Component#dispatchEvent
     */
    public boolean dispatchKeyEvent(KeyEvent e) {
        Component focusOwner = (((AWTEvent)e).isPosted) ? getFocusOwner() : e.getComponent();

        if (focusOwner != null && focusOwner.isShowing() && focusOwner.canBeFocusOwner()) {
            if (!e.isConsumed()) {
                Component comp = e.getComponent();
                if (comp != null && comp.isEnabled()) {
                    redispatchEvent(comp, e);
                }
            }
        }
        boolean stopPostProcessing = false;
        java.util.List<KeyEventPostProcessor> processors = getKeyEventPostProcessors();
        if (processors != null) {
            for (java.util.Iterator<KeyEventPostProcessor> iter = processors.iterator();
                 !stopPostProcessing && iter.hasNext(); )
            {
                stopPostProcessing = iter.next().
                            postProcessKeyEvent(e);
            }
        }
        if (!stopPostProcessing) {
            postProcessKeyEvent(e);
        }

        // 允许对等对象处理 KeyEvent
        Component source = e.getComponent();
        ComponentPeer peer = source.getPeer();

        if (peer == null || peer instanceof LightweightPeer) {
            // 如果焦点所有者是轻量级的，则其原生容器
            // 处理事件
            Container target = source.getNativeContainer();
            if (target != null) {
                peer = target.getPeer();
            }
        }
        if (peer != null) {
            peer.handleEvent(e);
        }

        return true;
    }

    /**
     * 此方法将由 <code>dispatchKeyEvent</code> 调用。它将处理任何未被消耗且映射到 AWT
     * <code>MenuShortcut</code> 的 KeyEvent，通过消耗事件并激活快捷方式。
     *
     * @param e 要后处理的 KeyEvent
     * @return <code>true</code>
     * @see #dispatchKeyEvent
     * @see MenuShortcut
     */
    public boolean postProcessKeyEvent(KeyEvent e) {
        if (!e.isConsumed()) {
            Component target = e.getComponent();
            Container p = (Container)
                (target instanceof Container ? target : target.getParent());
            if (p != null) {
                p.postProcessKeyEvent(e);
            }
        }
        return true;
    }

    private void pumpApprovedKeyEvents() {
        KeyEvent ke;
        do {
            ke = null;
            synchronized (this) {
                if (enqueuedKeyEvents.size() != 0) {
                    ke = enqueuedKeyEvents.getFirst();
                    if (typeAheadMarkers.size() != 0) {
                        TypeAheadMarker marker = typeAheadMarkers.getFirst();
                        // 修复 5064013: 事件可能具有相同的时间
                        // if (ke.getWhen() >= marker.after) {
                        // 修复已推出。

                        if (ke.getWhen() > marker.after) {
                            ke = null;
                        }
                    }
                    if (ke != null) {
                        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                            focusLog.finer("处理已批准的事件 {0}", ke);
                        }
                        enqueuedKeyEvents.removeFirst();
                    }
                }
            }
            if (ke != null) {
                preDispatchKeyEvent(ke);
            }
        } while (ke != null);
    }

    /**
     * 将类型提前队列标记列表转储到 stderr
     */
    void dumpMarkers() {
        if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
            focusLog.finest(">>> 标记转储，时间: {0}", System.currentTimeMillis());
            synchronized (this) {
                if (typeAheadMarkers.size() != 0) {
                    Iterator<TypeAheadMarker> iter = typeAheadMarkers.iterator();
                    while (iter.hasNext()) {
                        TypeAheadMarker marker = iter.next();
                        focusLog.finest("    {0}", marker);
                    }
                }
            }
        }
    }

    private boolean typeAheadAssertions(Component target, AWTEvent e) {

        // 在 FOCUS_GAINED 处理程序中以及此处清除任何挂起的事件。
        // 我们需要在此处调用此方法，以防在调用 dequeueKeyEvents 时删除了标记。
        pumpApprovedKeyEvents();

        switch (e.getID()) {
            case KeyEvent.KEY_TYPED:
            case KeyEvent.KEY_PRESSED:
            case KeyEvent.KEY_RELEASED: {
                KeyEvent ke = (KeyEvent)e;
                synchronized (this) {
                    if (e.isPosted && typeAheadMarkers.size() != 0) {
                        TypeAheadMarker marker = typeAheadMarkers.getFirst();
                        // 修复 5064013: 事件可能具有相同的时间
                        // if (ke.getWhen() >= marker.after) {
                        // 修复已推出。

                        if (ke.getWhen() > marker.after) {
                            if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                                focusLog.finer("由于标记 {1} 存储事件 {0}", ke, marker);
                            }
                            enqueuedKeyEvents.addLast(ke);
                            return true;
                        }
                    }
                }

                // KeyEvent 在焦点更改请求之前发布
                return preDispatchKeyEvent(ke);
            }

            case FocusEvent.FOCUS_GAINED:
                if (focusLog.isLoggable(PlatformLogger.Level.FINEST)) {
                    focusLog.finest("在 {0} 上 FOCUS_GAINED 之前的标记", target);
                }
                dumpMarkers();
                // 在标记列表中搜索与刚刚获得焦点的组件关联的第一个标记。
                // 然后删除该标记、紧随其后且关联到同一组件的任何标记以及所有前置标记。
                // 这处理了多次为同一组件发出焦点请求的情况，以及我们丢失了一些早期请求的情况。
                // 由于这些额外的请求不会生成 FOCUS_GAINED 事件，因此我们也需要清除这些标记。
                synchronized (this) {
                    boolean found = false;
                    if (hasMarker(target)) {
                        for (Iterator<TypeAheadMarker> iter = typeAheadMarkers.iterator();
                             iter.hasNext(); )
                        {
                            if (iter.next().untilFocused == target) {
                                found = true;
                            } else if (found) {
                                break;
                            }
                            iter.remove();
                        }
                    } else {
                        // 异常条件 - 没有标记的事件
                        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
                            focusLog.finer("没有标记的事件 {0}", e);
                        }
                    }
                }
                focusLog.finest("FOCUS_GAINED 之后的标记");
                dumpMarkers();

                redispatchEvent(target, e);

                // 现在，调度因 FOCUS_GAINED 事件而释放的任何挂起的 KeyEvent，
                // 以便我们不必等待另一个事件被发布到队列。
                pumpApprovedKeyEvents();
                return true;

            default:
                redispatchEvent(target, e);
                return true;
        }
    }

    /**
     * 如果组件 <code>comp</code> 在标记队列中有标记，则返回 true
     * @since 1.5
     */
    private boolean hasMarker(Component comp) {
        for (Iterator<TypeAheadMarker> iter = typeAheadMarkers.iterator(); iter.hasNext(); ) {
            if (iter.next().untilFocused == comp) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清除标记队列
     * @since 1.5
     */
    void clearMarkers() {
        synchronized(this) {
            typeAheadMarkers.clear();
        }
    }

    private boolean preDispatchKeyEvent(KeyEvent ke) {
        if (((AWTEvent) ke).isPosted) {
            Component focusOwner = getFocusOwner();
            ke.setSource(((focusOwner != null) ? focusOwner : getFocusedWindow()));
        }
        if (ke.getSource() == null) {
            return true;
        }

        // 在此处显式设置键事件时间戳（不在 Component.dispatchEventImpl 中）：
        // - 无论如何，键事件都会传递到此方法，该方法开始其实际调度。
        // - 如果键事件被放入类型提前队列，其时间戳应在实际调度开始时（由此方法）注册。
        EventQueue.setCurrentEventAndMostRecentTime(ke);


        }

    }
```

```java
                    /**
         * 4495473 的修复。
         * 此修复允许在本机事件代理机制激活时正确分发事件。
         * 如果激活了代理机制，我们应该在检测到正确的目标后重新分发键事件。
         */
        if (KeyboardFocusManager.isProxyActive(ke)) {
            Component source = (Component)ke.getSource();
            Container target = source.getNativeContainer();
            if (target != null) {
                ComponentPeer peer = target.getPeer();
                if (peer != null) {
                    peer.handleEvent(ke);
                    /**
                     * 4478780 的修复 - 在对等体分发事件后消耗事件。
                     */
                    ke.consume();
                }
            }
            return true;
        }

        java.util.List<KeyEventDispatcher> dispatchers = getKeyEventDispatchers();
        if (dispatchers != null) {
            for (java.util.Iterator<KeyEventDispatcher> iter = dispatchers.iterator();
                 iter.hasNext(); )
             {
                 if (iter.next().
                     dispatchKeyEvent(ke))
                 {
                     return true;
                 }
             }
        }
        return dispatchKeyEvent(ke);
    }

    /*
     * @param e 是一个可以用于跟踪下一个 KEY_TYPED 相关的 KEY_PRESSED 事件。
     */
    private void consumeNextKeyTyped(KeyEvent e) {
        consumeNextKeyTyped = true;
    }

    private void consumeTraversalKey(KeyEvent e) {
        e.consume();
        consumeNextKeyTyped = (e.getID() == KeyEvent.KEY_PRESSED) &&
                              !e.isActionKey();
    }

    /*
     * 如果事件被消耗，返回 true。
     */
    private boolean consumeProcessedKeyEvent(KeyEvent e) {
        if ((e.getID() == KeyEvent.KEY_TYPED) && consumeNextKeyTyped) {
            e.consume();
            consumeNextKeyTyped = false;
            return true;
        }
        return false;
    }

    /**
     * 如果且仅当 KeyEvent 代表指定 focusedComponent 的焦点遍历键时，此方法启动焦点遍历操作。预期 focusedComponent 是当前的焦点所有者，但不一定如此。如果不是，焦点遍历仍将像 focusedComponent 是焦点所有者一样进行。
     *
     * @param focusedComponent 作为焦点遍历操作基础的组件，如果指定的事件代表该组件的焦点遍历键。
     * @param e 可能代表焦点遍历键的事件。
     */
    public void processKeyEvent(Component focusedComponent, KeyEvent e) {
        // 如有必要，消耗已处理的事件
        if (consumeProcessedKeyEvent(e)) {
            return;
        }

        // KEY_TYPED 事件不能是焦点遍历键
        if (e.getID() == KeyEvent.KEY_TYPED) {
            return;
        }

        if (focusedComponent.getFocusTraversalKeysEnabled() &&
            !e.isConsumed())
        {
            AWTKeyStroke stroke = AWTKeyStroke.getAWTKeyStrokeForEvent(e),
                oppStroke = AWTKeyStroke.getAWTKeyStroke(stroke.getKeyCode(),
                                                 stroke.getModifiers(),
                                                 !stroke.isOnKeyRelease());
            Set<AWTKeyStroke> toTest;
            boolean contains, containsOpp;

            toTest = focusedComponent.getFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
            contains = toTest.contains(stroke);
            containsOpp = toTest.contains(oppStroke);

            if (contains || containsOpp) {
                consumeTraversalKey(e);
                if (contains) {
                    focusNextComponent(focusedComponent);
                }
                return;
            } else if (e.getID() == KeyEvent.KEY_PRESSED) {
                // 6637607 的修复：应重置 consumeNextKeyTyped。
                consumeNextKeyTyped = false;
            }

            toTest = focusedComponent.getFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
            contains = toTest.contains(stroke);
            containsOpp = toTest.contains(oppStroke);

            if (contains || containsOpp) {
                consumeTraversalKey(e);
                if (contains) {
                    focusPreviousComponent(focusedComponent);
                }
                return;
            }

            toTest = focusedComponent.getFocusTraversalKeys(
                KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS);
            contains = toTest.contains(stroke);
            containsOpp = toTest.contains(oppStroke);

            if (contains || containsOpp) {
                consumeTraversalKey(e);
                if (contains) {
                    upFocusCycle(focusedComponent);
                }
                return;
            }

            if (!((focusedComponent instanceof Container) &&
                  ((Container)focusedComponent).isFocusCycleRoot())) {
                return;
            }

            toTest = focusedComponent.getFocusTraversalKeys(
                KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS);
            contains = toTest.contains(stroke);
            containsOpp = toTest.contains(oppStroke);

            if (contains || containsOpp) {
                consumeTraversalKey(e);
                if (contains) {
                    downFocusCycle((Container)focusedComponent);
                }
            }
        }
    }

    /**
     * 延迟分发 KeyEvents，直到指定的 Component 成为焦点所有者。具有比指定时间戳更晚的时间戳的 KeyEvents 将被排队，直到指定的 Component 收到 FOCUS_GAINED 事件，或者 AWT 通过调用 <code>dequeueKeyEvents</code> 或 <code>discardKeyEvents</code> 取消延迟请求。
     *
     * @param after 当前事件的时间戳，或者如果当前事件没有时间戳或 AWT 无法确定当前正在处理的事件，则为当前系统时间。
     * @param untilFocused 在任何待处理的 KeyEvents 之前接收 FOCUS_GAINED 事件的 Component。
     * @see #dequeueKeyEvents
     * @see #discardKeyEvents
     */
    protected synchronized void enqueueKeyEvents(long after,
                                                 Component untilFocused) {
        if (untilFocused == null) {
            return;
        }

        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("Enqueue at {0} for {1}",
                       after, untilFocused);
        }

        int insertionIndex = 0,
            i = typeAheadMarkers.size();
        ListIterator<TypeAheadMarker> iter = typeAheadMarkers.listIterator(i);

        for (; i > 0; i--) {
            TypeAheadMarker marker = iter.previous();
            if (marker.after <= after) {
                insertionIndex = i;
                break;
            }
        }

        typeAheadMarkers.add(insertionIndex,
                             new TypeAheadMarker(after, untilFocused));
    }

    /**
     * 释放所有因调用 <code>enqueueKeyEvents</code> 而排队的 KeyEvents，这些 KeyEvents 具有相同的时间戳和 Component。如果给定的时间戳小于零，则应取消给定 Component 的具有<b>最旧</b>时间戳的未完成排队请求（如果有）。
     *
     * @param after 在调用 <code>enqueueKeyEvents</code> 时指定的时间戳，或任何值 &lt; 0。
     * @param untilFocused 在调用 <code>enqueueKeyEvents</code> 时指定的 Component。
     * @see #enqueueKeyEvents
     * @see #discardKeyEvents
     */
    protected synchronized void dequeueKeyEvents(long after,
                                                 Component untilFocused) {
        if (untilFocused == null) {
            return;
        }

        if (focusLog.isLoggable(PlatformLogger.Level.FINER)) {
            focusLog.finer("Dequeue at {0} for {1}",
                       after, untilFocused);
        }

        TypeAheadMarker marker;
        ListIterator<TypeAheadMarker> iter = typeAheadMarkers.listIterator
            ((after >= 0) ? typeAheadMarkers.size() : 0);

        if (after < 0) {
            while (iter.hasNext()) {
                marker = iter.next();
                if (marker.untilFocused == untilFocused)
                {
                    iter.remove();
                    return;
                }
            }
        } else {
            while (iter.hasPrevious()) {
                marker = iter.previous();
                if (marker.untilFocused == untilFocused &&
                    marker.after == after)
                {
                    iter.remove();
                    return;
                }
            }
        }
    }

    /**
     * 丢弃所有因一个或多个调用 <code>enqueueKeyEvents</code> 而排队的 KeyEvents，这些 KeyEvents 具有指定的 Component 或其后代。
     *
     * @param comp 在一个或多个调用 <code>enqueueKeyEvents</code> 时指定的 Component，或此类 Component 的父级。
     * @see #enqueueKeyEvents
     * @see #dequeueKeyEvents
     */
    protected synchronized void discardKeyEvents(Component comp) {
        if (comp == null) {
            return;
        }

        long start = -1;

        for (Iterator<TypeAheadMarker> iter = typeAheadMarkers.iterator(); iter.hasNext(); ) {
            TypeAheadMarker marker = iter.next();
            Component toTest = marker.untilFocused;
            boolean match = (toTest == comp);
            while (!match && toTest != null && !(toTest instanceof Window)) {
                toTest = toTest.getParent();
                match = (toTest == comp);
            }
            if (match) {
                if (start < 0) {
                    start = marker.after;
                }
                iter.remove();
            } else if (start >= 0) {
                purgeStampedEvents(start, marker.after);
                start = -1;
            }
        }

        purgeStampedEvents(start, -1);
    }

    // 注意：
    //   * 必须在同步块内调用
    //   * 如果 'start' < 0，则此函数不执行任何操作
    //   * 如果 'end' < 0，则从 'start' 到队列末尾的所有 KeyEvents 都将被移除
    private void purgeStampedEvents(long start, long end) {
        if (start < 0) {
            return;
        }

        for (Iterator<KeyEvent> iter = enqueuedKeyEvents.iterator(); iter.hasNext(); ) {
            KeyEvent ke = iter.next();
            long time = ke.getWhen();

            if (start < time && (end < 0 || time <= end)) {
                iter.remove();
            }

            if (end >= 0 && time > end) {
                break;
            }
        }
    }

    /**
     * 将焦点设置为 aComponent 之前的 Component，通常基于 FocusTraversalPolicy。
     *
     * @param aComponent 作为焦点遍历操作基础的 Component。
     * @see FocusTraversalPolicy
     * @see Component#transferFocusBackward
     */
    public void focusPreviousComponent(Component aComponent) {
        if (aComponent != null) {
            aComponent.transferFocusBackward();
        }
    }

    /**
     * 将焦点设置为 aComponent 之后的 Component，通常基于 FocusTraversalPolicy。
     *
     * @param aComponent 作为焦点遍历操作基础的 Component。
     * @see FocusTraversalPolicy
     * @see Component#transferFocus
     */
    public void focusNextComponent(Component aComponent) {
        if (aComponent != null) {
            aComponent.transferFocus();
        }
    }

    /**
     * 将焦点上移一个焦点遍历周期。通常，焦点所有者被设置为 aComponent 的焦点周期根，当前焦点周期根被设置为新的焦点所有者的焦点周期根。然而，如果 aComponent 的焦点周期根是一个 Window，则焦点所有者被设置为焦点周期根的默认 Component，当前焦点周期根保持不变。
     *
     * @param aComponent 作为焦点遍历操作基础的 Component。
     * @see Component#transferFocusUpCycle
     */
    public void upFocusCycle(Component aComponent) {
        if (aComponent != null) {
            aComponent.transferFocusUpCycle();
        }
    }

    /**
     * 将焦点下移一个焦点遍历周期。如果 aContainer 是一个焦点周期根，则焦点所有者被设置为 aContainer 的默认 Component，当前焦点周期根被设置为 aContainer。如果 aContainer 不是一个焦点周期根，则不发生焦点遍历操作。
     *
     * @param aContainer 作为焦点遍历操作基础的 Container。
     * @see Container#transferFocusDownCycle
     */
    public void downFocusCycle(Container aContainer) {
        if (aContainer != null && aContainer.isFocusCycleRoot()) {
            aContainer.transferFocusDownCycle();
        }
    }
}
