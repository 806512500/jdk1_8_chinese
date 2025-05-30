/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import java.util.ArrayList;
import sun.util.logging.PlatformLogger;

import sun.awt.dnd.SunDragSourceContextPeer;
import sun.awt.EventQueueDelegate;

/**
 * EventDispatchThread 是一个包私有的 AWT 类，它从 EventQueue 中取出事件并分发给适当的 AWT 组件。
 *
 * 该线程在其 run() 方法中通过调用 pumpEvents(Conditional) 启动一个“永久”事件泵。事件处理程序可以选择在任何时候阻塞此事件泵，但应通过再次调用 pumpEvents(Conditional) 启动一个新的泵（而不是一个新的 EventDispatchThread）。此二级事件泵将在 Conditional 评估为 false 并且分发了一个额外的事件后自动退出。
 *
 * @author Tom Ball
 * @author Amy Fowler
 * @author Fred Ecks
 * @author David Mendenhall
 *
 * @since 1.1
 */
class EventDispatchThread extends Thread {

    private static final PlatformLogger eventLog = PlatformLogger.getLogger("java.awt.event.EventDispatchThread");

    private EventQueue theQueue;
    private volatile boolean doDispatch = true;

    private static final int ANY_EVENT = -1;

    private ArrayList<EventFilter> eventFilters = new ArrayList<EventFilter>();

    EventDispatchThread(ThreadGroup group, String name, EventQueue queue) {
        super(group, name);
        setEventQueue(queue);
    }

    /*
     * 必须仅在 EDT 上调用，因此不需要同步
     */
    public void stopDispatching() {
        doDispatch = false;
    }

    public void run() {
        try {
            pumpEvents(new Conditional() {
                public boolean evaluate() {
                    return true;
                }
            });
        } finally {
            getEventQueue().detachDispatchThread(this);
        }
    }

    void pumpEvents(Conditional cond) {
        pumpEvents(ANY_EVENT, cond);
    }

    void pumpEventsForHierarchy(Conditional cond, Component modalComponent) {
        pumpEventsForHierarchy(ANY_EVENT, cond, modalComponent);
    }

    void pumpEvents(int id, Conditional cond) {
        pumpEventsForHierarchy(id, cond, null);
    }

    void pumpEventsForHierarchy(int id, Conditional cond, Component modalComponent) {
        pumpEventsForFilter(id, cond, new HierarchyEventFilter(modalComponent));
    }

    void pumpEventsForFilter(Conditional cond, EventFilter filter) {
        pumpEventsForFilter(ANY_EVENT, cond, filter);
    }

    void pumpEventsForFilter(int id, Conditional cond, EventFilter filter) {
        addEventFilter(filter);
        doDispatch = true;
        while (doDispatch && !isInterrupted() && cond.evaluate()) {
            pumpOneEventForFilters(id);
        }
        removeEventFilter(filter);
    }

    void addEventFilter(EventFilter filter) {
        if (eventLog.isLoggable(PlatformLogger.Level.FINEST)) {
            eventLog.finest("添加事件过滤器: " + filter);
        }
        synchronized (eventFilters) {
            if (!eventFilters.contains(filter)) {
                if (filter instanceof ModalEventFilter) {
                    ModalEventFilter newFilter = (ModalEventFilter)filter;
                    int k = 0;
                    for (k = 0; k < eventFilters.size(); k++) {
                        EventFilter f = eventFilters.get(k);
                        if (f instanceof ModalEventFilter) {
                            ModalEventFilter cf = (ModalEventFilter)f;
                            if (cf.compareTo(newFilter) > 0) {
                                break;
                            }
                        }
                    }
                    eventFilters.add(k, filter);
                } else {
                    eventFilters.add(filter);
                }
            }
        }
    }

    void removeEventFilter(EventFilter filter) {
        if (eventLog.isLoggable(PlatformLogger.Level.FINEST)) {
            eventLog.finest("移除事件过滤器: " + filter);
        }
        synchronized (eventFilters) {
            eventFilters.remove(filter);
        }
    }

    boolean filterAndCheckEvent(AWTEvent event) {
        boolean eventOK = true;
        synchronized (eventFilters) {
            for (int i = eventFilters.size() - 1; i >= 0; i--) {
                EventFilter f = eventFilters.get(i);
                EventFilter.FilterAction accept = f.acceptEvent(event);
                if (accept == EventFilter.FilterAction.REJECT) {
                    eventOK = false;
                    break;
                } else if (accept == EventFilter.FilterAction.ACCEPT_IMMEDIATELY) {
                    break;
                }
            }
        }
        return eventOK && SunDragSourceContextPeer.checkEvent(event);
    }

    void pumpOneEventForFilters(int id) {
        AWTEvent event = null;
        boolean eventOK = false;
        try {
            EventQueue eq = null;
            EventQueueDelegate.Delegate delegate = null;
            do {
                // 在分发过程中，EventQueue 可能会改变
                eq = getEventQueue();
                delegate = EventQueueDelegate.getDelegate();

                if (delegate != null && id == ANY_EVENT) {
                    event = delegate.getNextEvent(eq);
                } else {
                    event = (id == ANY_EVENT) ? eq.getNextEvent() : eq.getNextEvent(id);
                }

                eventOK = filterAndCheckEvent(event);
                if (!eventOK) {
                    event.consume();
                }
            }
            while (eventOK == false);

            if (eventLog.isLoggable(PlatformLogger.Level.FINEST)) {
                eventLog.finest("分发: " + event);
            }

            Object handle = null;
            if (delegate != null) {
                handle = delegate.beforeDispatch(event);
            }
            eq.dispatchEvent(event);
            if (delegate != null) {
                delegate.afterDispatch(event, handle);
            }
        }
        catch (ThreadDeath death) {
            doDispatch = false;
            throw death;
        }
        catch (InterruptedException interruptedException) {
            doDispatch = false; // AppContext.dispose() 会中断 AppContext 中的所有线程
        }
        catch (Throwable e) {
            processException(e);
        }
    }

    private void processException(Throwable e) {
        if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
            eventLog.fine("处理异常: " + e);
        }
        getUncaughtExceptionHandler().uncaughtException(this, e);
    }

    public synchronized EventQueue getEventQueue() {
        return theQueue;
    }
    public synchronized void setEventQueue(EventQueue eq) {
        theQueue = eq;
    }

    private static class HierarchyEventFilter implements EventFilter {
        private Component modalComponent;
        public HierarchyEventFilter(Component modalComponent) {
            this.modalComponent = modalComponent;
        }
        public FilterAction acceptEvent(AWTEvent event) {
            if (modalComponent != null) {
                int eventID = event.getID();
                boolean mouseEvent = (eventID >= MouseEvent.MOUSE_FIRST) &&
                                     (eventID <= MouseEvent.MOUSE_LAST);
                boolean actionEvent = (eventID >= ActionEvent.ACTION_FIRST) &&
                                      (eventID <= ActionEvent.ACTION_LAST);
                boolean windowClosingEvent = (eventID == WindowEvent.WINDOW_CLOSING);
                /*
                 * 过滤掉 modalComponent 层次结构之外的 MouseEvent 和 ActionEvent。
                 * KeyEvent 通过在 Dialog.show 中使用 enqueueKeyEvent 处理。
                 */
                if (Component.isInstanceOf(modalComponent, "javax.swing.JInternalFrame")) {
                    /*
                     * 模态内部框架单独处理。如果事件是来自与 modalComp 不同的重量级组件，
                     * 则接受该事件。如果重量级组件相同 - 我们仍然接受事件，并在 LightweightDispatcher 中进行进一步过滤
                     */
                    return windowClosingEvent ? FilterAction.REJECT : FilterAction.ACCEPT;
                }
                if (mouseEvent || actionEvent || windowClosingEvent) {
                    Object o = event.getSource();
                    if (o instanceof sun.awt.ModalExclude) {
                        // 从模态中排除此对象并继续分发其事件。
                        return FilterAction.ACCEPT;
                    } else if (o instanceof Component) {
                        Component c = (Component) o;
                        // 5.0u3 模态排除
                        boolean modalExcluded = false;
                        if (modalComponent instanceof Container) {
                            while (c != modalComponent && c != null) {
                                if ((c instanceof Window) &&
                                    (sun.awt.SunToolkit.isModalExcluded((Window)c))) {
                                    // 排除此窗口及其所有子组件的模态，并继续分发其事件。
                                    modalExcluded = true;
                                    break;
                                }
                                c = c.getParent();
                            }
                        }
                        if (!modalExcluded && (c != modalComponent)) {
                            return FilterAction.REJECT;
                        }
                    }
                }
            }
            return FilterAction.ACCEPT;
        }
    }
}
