
/*
 * Copyright (c) 1996, 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.peer.ComponentPeer;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.EmptyStackException;

import sun.awt.*;
import sun.awt.dnd.SunDropTargetEvent;
import sun.util.logging.PlatformLogger;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.atomic.AtomicInteger;

import java.security.AccessControlContext;

import sun.misc.SharedSecrets;
import sun.misc.JavaSecurityAccess;

/**
 * <code>EventQueue</code> 是一个平台无关的类，用于队列化事件，这些事件既来自底层的对等类，也来自受信任的应用程序类。
 * <p>
 * 它封装了异步事件分发机制，该机制从队列中提取事件并通过调用
 * {@link #dispatchEvent(AWTEvent) dispatchEvent(AWTEvent)} 方法
 * 在此 <code>EventQueue</code> 上分发事件。该机制的具体行为是实现依赖的。唯一的要求是：
 * <dl>
 *   <dt> 顺序地。
 *   <dd> 即，不允许同时分发来自此队列的多个事件。
 *   <dt> 按照它们被队列化的顺序。
 *   <dd> 即，如果 <code>AWTEvent</code>&nbsp;A 被队列化到 <code>EventQueue</code> 之前
 *        <code>AWTEvent</code>&nbsp;B，则事件 B 不会在事件 A 之前被分发。
 * </dl>
 * <p>
 * 一些浏览器将不同代码库中的小程序分区到不同的上下文中，并在这些上下文之间建立隔离。在这种情况下，每个上下文将有一个 <code>EventQueue</code>。
 * 其他浏览器将所有小程序放置在同一个上下文中，这意味着所有小程序将只有一个全局的 <code>EventQueue</code>。这种行为是实现依赖的。
 * 请参阅浏览器的文档以获取更多信息。
 * <p>
 * 有关事件分发机制的线程问题，请参阅 <a href="doc-files/AWTThreadIssues.html#Autoshutdown"
 * >AWT 线程问题</a>。
 *
 * @author Thomas Ball
 * @author Fred Ecks
 * @author David Mendenhall
 *
 * @since       1.1
 */
public class EventQueue {
    private static final AtomicInteger threadInitNumber = new AtomicInteger(0);

    private static final int LOW_PRIORITY = 0;
    private static final int NORM_PRIORITY = 1;
    private static final int HIGH_PRIORITY = 2;
    private static final int ULTIMATE_PRIORITY = 3;

    private static final int NUM_PRIORITIES = ULTIMATE_PRIORITY + 1;

    /*
     * 我们为 EventQueue 支持的每个优先级维护一个队列。
     * 即，EventQueue 对象实际上是 NUM_PRIORITIES 个队列的实现，每个内部队列上的所有事件具有相同的优先级。
     * 事件从 EventQueue 开始，从最高优先级的队列开始拉取。我们按优先级递减的顺序遍历所有队列。
     */
    private Queue[] queues = new Queue[NUM_PRIORITIES];

    /*
     * 堆栈上的下一个 EventQueue，如果此 EventQueue 位于堆栈顶部，则为 null。
     * 如果 nextQueue 非空，发布事件的请求将转发到 nextQueue。
     */
    private EventQueue nextQueue;

    /*
     * 堆栈上的前一个 EventQueue，如果这是“基础”EventQueue，则为 null。
     */
    private EventQueue previousQueue;

    /*
     * 一个锁，用于同步 AppContext 中所有 EventQueues 的 push()/pop() 和相关操作。仅同步特定的事件队列是不够的：
     * 我们应该锁定整个堆栈。
     */
    private final Lock pushPopLock;
    private final Condition pushPopCond;

    /*
     * 用于在执行 push/pop 操作后唤醒 EDT 以从 getNextEvent() 中返回的虚拟 Runnable。
     */
    private final static Runnable dummyRunnable = new Runnable() {
        public void run() {
        }
    };

    private EventDispatchThread dispatchThread;

    private final ThreadGroup threadGroup =
        Thread.currentThread().getThreadGroup();
    private final ClassLoader classLoader =
        Thread.currentThread().getContextClassLoader();

    /*
     * 最后分发的 InputEvent 或 ActionEvent 的时间戳。
     */
    private long mostRecentEventTime = System.currentTimeMillis();

    /*
     * 最后分发的 KeyEvent 的时间戳。
     */
    private long mostRecentKeyEventTime = System.currentTimeMillis();

    /**
     * 如果当前事件是 InputEvent 或 ActionEvent，则为当前事件的修饰符字段。
     */
    private WeakReference<AWTEvent> currentEvent;

    /*
     * 如果线程正在 getNextEvent(int) 中等待特定 ID 的事件被发布到队列，则为非零。
     */
    private volatile int waitForID;

    /*
     * 与队列对应的 AppContext。
     */
    private final AppContext appContext;

    private final String name = "AWT-EventQueue-" + threadInitNumber.getAndIncrement();

    private FwDispatcher fwDispatcher;

    private static volatile PlatformLogger eventLog;

    private static final PlatformLogger getEventLog() {
        if(eventLog == null) {
            eventLog = PlatformLogger.getLogger("java.awt.event.EventQueue");
        }
        return eventLog;
    }

    static {
        AWTAccessor.setEventQueueAccessor(
            new AWTAccessor.EventQueueAccessor() {
                public Thread getDispatchThread(EventQueue eventQueue) {
                    return eventQueue.getDispatchThread();
                }
                public boolean isDispatchThreadImpl(EventQueue eventQueue) {
                    return eventQueue.isDispatchThreadImpl();
                }
                public void removeSourceEvents(EventQueue eventQueue,
                                               Object source,
                                               boolean removeAllEvents)
                {
                    eventQueue.removeSourceEvents(source, removeAllEvents);
                }
                public boolean noEvents(EventQueue eventQueue) {
                    return eventQueue.noEvents();
                }
                public void wakeup(EventQueue eventQueue, boolean isShutdown) {
                    eventQueue.wakeup(isShutdown);
                }
                public void invokeAndWait(Object source, Runnable r)
                    throws InterruptedException, InvocationTargetException
                {
                    EventQueue.invokeAndWait(source, r);
                }
                public void setFwDispatcher(EventQueue eventQueue,
                                            FwDispatcher dispatcher) {
                    eventQueue.setFwDispatcher(dispatcher);
                }

                @Override
                public long getMostRecentEventTime(EventQueue eventQueue) {
                    return eventQueue.getMostRecentEventTimeImpl();
                }
            });
    }

    public EventQueue() {
        for (int i = 0; i < NUM_PRIORITIES; i++) {
            queues[i] = new Queue();
        }
        /*
         * 注意：如果您需要在此处启动关联的事件分发线程，请注意以下问题：
         * 如果此 EventQueue 实例是在 SunToolkit.createNewAppContext() 中创建的，
         * 启动的分发线程可能会在 createNewAppContext() 完成之前调用 AppContext.getAppContext()，
         * 从而导致线程组到 AppContext 映射的混乱。
         */

        appContext = AppContext.getAppContext();
        pushPopLock = (Lock)appContext.get(AppContext.EVENT_QUEUE_LOCK_KEY);
        pushPopCond = (Condition)appContext.get(AppContext.EVENT_QUEUE_COND_KEY);
    }

    /**
     * 将 1.1 风格的事件发布到 <code>EventQueue</code>。
     * 如果队列中已经存在具有相同 ID 和事件源的事件，则将调用源 <code>Component</code> 的
     * <code>coalesceEvents</code> 方法。
     *
     * @param theEvent <code>java.awt.AWTEvent</code> 的实例，或其子类的实例
     * @throws NullPointerException 如果 <code>theEvent</code> 为 <code>null</code>
     */
    public void postEvent(AWTEvent theEvent) {
        SunToolkit.flushPendingEvents(appContext);
        postEventPrivate(theEvent);
    }

    /**
     * 将 1.1 风格的事件发布到 <code>EventQueue</code>。
     * 如果队列中已经存在具有相同 ID 和事件源的事件，则将调用源 <code>Component</code> 的
     * <code>coalesceEvents</code> 方法。
     *
     * @param theEvent <code>java.awt.AWTEvent</code> 的实例，或其子类的实例
     */
    private final void postEventPrivate(AWTEvent theEvent) {
        theEvent.isPosted = true;
        pushPopLock.lock();
        try {
            if (nextQueue != null) {
                // 将事件转发到 EventQueue 堆栈的顶部
                nextQueue.postEventPrivate(theEvent);
                return;
            }
            if (dispatchThread == null) {
                if (theEvent.getSource() == AWTAutoShutdown.getInstance()) {
                    return;
                } else {
                    initDispatchThread();
                }
            }
            postEvent(theEvent, getPriority(theEvent));
        } finally {
            pushPopLock.unlock();
        }
    }

    private static int getPriority(AWTEvent theEvent) {
        if (theEvent instanceof PeerEvent) {
            PeerEvent peerEvent = (PeerEvent)theEvent;
            if ((peerEvent.getFlags() & PeerEvent.ULTIMATE_PRIORITY_EVENT) != 0) {
                return ULTIMATE_PRIORITY;
            }
            if ((peerEvent.getFlags() & PeerEvent.PRIORITY_EVENT) != 0) {
                return HIGH_PRIORITY;
            }
            if ((peerEvent.getFlags() & PeerEvent.LOW_PRIORITY_EVENT) != 0) {
                return LOW_PRIORITY;
            }
        }
        int id = theEvent.getID();
        if ((id >= PaintEvent.PAINT_FIRST) && (id <= PaintEvent.PAINT_LAST)) {
            return LOW_PRIORITY;
        }
        return NORM_PRIORITY;
    }

    /**
     * 将事件发布到指定优先级的内部队列，适当合并。
     *
     * @param theEvent <code>java.awt.AWTEvent</code> 的实例，或其子类的实例
     * @param priority 事件的期望优先级
     */
    private void postEvent(AWTEvent theEvent, int priority) {
        if (coalesceEvent(theEvent, priority)) {
            return;
        }

        EventQueueItem newItem = new EventQueueItem(theEvent);

        cacheEQItem(newItem);

        boolean notifyID = (theEvent.getID() == this.waitForID);

        if (queues[priority].head == null) {
            boolean shouldNotify = noEvents();
            queues[priority].head = queues[priority].tail = newItem;

            if (shouldNotify) {
                if (theEvent.getSource() != AWTAutoShutdown.getInstance()) {
                    AWTAutoShutdown.getInstance().notifyThreadBusy(dispatchThread);
                }
                pushPopCond.signalAll();
            } else if (notifyID) {
                pushPopCond.signalAll();
            }
        } else {
            // 事件未合并或具有非 Component 源。
            // 将其插入适当队列的末尾。
            queues[priority].tail.next = newItem;
            queues[priority].tail = newItem;
            if (notifyID) {
                pushPopCond.signalAll();
            }
        }
    }

    private boolean coalescePaintEvent(PaintEvent e) {
        ComponentPeer sourcePeer = ((Component)e.getSource()).peer;
        if (sourcePeer != null) {
            sourcePeer.coalescePaintEvent(e);
        }
        EventQueueItem[] cache = ((Component)e.getSource()).eventCache;
        if (cache == null) {
            return false;
        }
        int index = eventToCacheIndex(e);

        if (index != -1 && cache[index] != null) {
            PaintEvent merged = mergePaintEvents(e, (PaintEvent)cache[index].event);
            if (merged != null) {
                cache[index].event = merged;
                return true;
            }
        }
        return false;
    }

    private PaintEvent mergePaintEvents(PaintEvent a, PaintEvent b) {
        Rectangle aRect = a.getUpdateRect();
        Rectangle bRect = b.getUpdateRect();
        if (bRect.contains(aRect)) {
            return b;
        }
        if (aRect.contains(bRect)) {
            return a;
        }
        return null;
    }

    private boolean coalesceMouseEvent(MouseEvent e) {
        EventQueueItem[] cache = ((Component)e.getSource()).eventCache;
        if (cache == null) {
            return false;
        }
        int index = eventToCacheIndex(e);
        if (index != -1 && cache[index] != null) {
            cache[index].event = e;
            return true;
        }
        return false;
    }

    private boolean coalescePeerEvent(PeerEvent e) {
        EventQueueItem[] cache = ((Component)e.getSource()).eventCache;
        if (cache == null) {
            return false;
        }
        int index = eventToCacheIndex(e);
        if (index != -1 && cache[index] != null) {
            e = e.coalesceEvents((PeerEvent)cache[index].event);
            if (e != null) {
                cache[index].event = e;
                return true;
            } else {
                cache[index] = null;
            }
        }
        return false;
    }

    /*
     * 应尽量避免调用此方法，因为其工作时间取决于 EQ 的长度。
     * 在最坏的情况下，此方法可能会使整个应用程序的事件处理速度降低 10 倍。
     * 仅出于向后兼容的原因而存在。
     */
    private boolean coalesceOtherEvent(AWTEvent e, int priority) {
        int id = e.getID();
        Component source = (Component)e.getSource();
        for (EventQueueItem entry = queues[priority].head;
            entry != null; entry = entry.next)
        {
            // 给 Component.coalesceEvents 一个机会
            if (entry.event.getSource() == source && entry.event.getID() == id) {
                AWTEvent coalescedEvent = source.coalesceEvents(
                    entry.event, e);
                if (coalescedEvent != null) {
                    entry.event = coalescedEvent;
                    return true;
                }
            }
        }
        return false;
    }


                private boolean coalesceEvent(AWTEvent e, int priority) {
        if (!(e.getSource() instanceof Component)) {
            return false;
        }
        if (e instanceof PeerEvent) {
            return coalescePeerEvent((PeerEvent)e);
        }
        // 最坏的情况
        if (((Component)e.getSource()).isCoalescingEnabled()
            && coalesceOtherEvent(e, priority))
        {
            return true;
        }
        if (e instanceof PaintEvent) {
            return coalescePaintEvent((PaintEvent)e);
        }
        if (e instanceof MouseEvent) {
            return coalesceMouseEvent((MouseEvent)e);
        }
        return false;
    }

    private void cacheEQItem(EventQueueItem entry) {
        int index = eventToCacheIndex(entry.event);
        if (index != -1 && entry.event.getSource() instanceof Component) {
            Component source = (Component)entry.event.getSource();
            if (source.eventCache == null) {
                source.eventCache = new EventQueueItem[CACHE_LENGTH];
            }
            source.eventCache[index] = entry;
        }
    }

    private void uncacheEQItem(EventQueueItem entry) {
        int index = eventToCacheIndex(entry.event);
        if (index != -1 && entry.event.getSource() instanceof Component) {
            Component source = (Component)entry.event.getSource();
            if (source.eventCache == null) {
                return;
            }
            source.eventCache[index] = null;
        }
    }

    private static final int PAINT = 0;
    private static final int UPDATE = 1;
    private static final int MOVE = 2;
    private static final int DRAG = 3;
    private static final int PEER = 4;
    private static final int CACHE_LENGTH = 5;

    private static int eventToCacheIndex(AWTEvent e) {
        switch(e.getID()) {
        case PaintEvent.PAINT:
            return PAINT;
        case PaintEvent.UPDATE:
            return UPDATE;
        case MouseEvent.MOUSE_MOVED:
            return MOVE;
        case MouseEvent.MOUSE_DRAGGED:
            // 为 SunDropTargetEvent 返回 -1，因为它们通常是同步的
            // 我们不希望通过合并 MouseEvent 或其他拖动事件而跳过它们
            return e instanceof SunDropTargetEvent ? -1 : DRAG;
        default:
            return e instanceof PeerEvent ? PEER : -1;
        }
    }

    /**
     * 返回任何独立队列中是否有待处理的事件。
     * @return 是否有任何独立队列中有待处理的事件
     */
    private boolean noEvents() {
        for (int i = 0; i < NUM_PRIORITIES; i++) {
            if (queues[i].head != null) {
                return false;
            }
        }

        return true;
    }

    /**
     * 从 <code>EventQueue</code> 中移除一个事件并返回它。此方法将在另一个线程发布事件之前阻塞。
     * @return 下一个 <code>AWTEvent</code>
     * @exception InterruptedException
     *            如果任何线程中断了此线程
     */
    public AWTEvent getNextEvent() throws InterruptedException {
        do {
            /*
             * 必须在同步块之外调用 SunToolkit.flushPendingEvents 以避免在
             * 事件队列嵌套时发生死锁。
             */
            SunToolkit.flushPendingEvents(appContext);
            pushPopLock.lock();
            try {
                AWTEvent event = getNextEventPrivate();
                if (event != null) {
                    return event;
                }
                AWTAutoShutdown.getInstance().notifyThreadFree(dispatchThread);
                pushPopCond.await();
            } finally {
                pushPopLock.unlock();
            }
        } while(true);
    }

    /*
     * 必须在锁下调用。不调用 flushPendingEvents()
     */
    AWTEvent getNextEventPrivate() throws InterruptedException {
        for (int i = NUM_PRIORITIES - 1; i >= 0; i--) {
            if (queues[i].head != null) {
                EventQueueItem entry = queues[i].head;
                queues[i].head = entry.next;
                if (entry.next == null) {
                    queues[i].tail = null;
                }
                uncacheEQItem(entry);
                return entry.event;
            }
        }
        return null;
    }

    AWTEvent getNextEvent(int id) throws InterruptedException {
        do {
            /*
             * 必须在同步块之外调用 SunToolkit.flushPendingEvents 以避免在
             * 事件队列嵌套时发生死锁。
             */
            SunToolkit.flushPendingEvents(appContext);
            pushPopLock.lock();
            try {
                for (int i = 0; i < NUM_PRIORITIES; i++) {
                    for (EventQueueItem entry = queues[i].head, prev = null;
                         entry != null; prev = entry, entry = entry.next)
                    {
                        if (entry.event.getID() == id) {
                            if (prev == null) {
                                queues[i].head = entry.next;
                            } else {
                                prev.next = entry.next;
                            }
                            if (queues[i].tail == entry) {
                                queues[i].tail = prev;
                            }
                            uncacheEQItem(entry);
                            return entry.event;
                        }
                    }
                }
                waitForID = id;
                pushPopCond.await();
                waitForID = 0;
            } finally {
                pushPopLock.unlock();
            }
        } while(true);
    }

    /**
     * 返回 <code>EventQueue</code> 上的第一个事件，但不移除它。
     * @return 第一个事件
     */
    public AWTEvent peekEvent() {
        pushPopLock.lock();
        try {
            for (int i = NUM_PRIORITIES - 1; i >= 0; i--) {
                if (queues[i].head != null) {
                    return queues[i].head.event;
                }
            }
        } finally {
            pushPopLock.unlock();
        }

        return null;
    }

    /**
     * 返回指定 id 的第一个事件，如果有。
     * @param id 所需事件类型的 id
     * @return 指定 id 的第一个事件，如果没有这样的事件则返回 <code>null</code>
     */
    public AWTEvent peekEvent(int id) {
        pushPopLock.lock();
        try {
            for (int i = NUM_PRIORITIES - 1; i >= 0; i--) {
                EventQueueItem q = queues[i].head;
                for (; q != null; q = q.next) {
                    if (q.event.getID() == id) {
                        return q.event;
                    }
                }
            }
        } finally {
            pushPopLock.unlock();
        }

        return null;
    }

    private static final JavaSecurityAccess javaSecurityAccess =
        SharedSecrets.getJavaSecurityAccess();

    /**
     * 分发一个事件。事件的分发方式取决于事件的类型和事件源对象的类型：
     *
     * <table border=1 summary="事件类型、源类型和分发方法">
     * <tr>
     *     <th>事件类型</th>
     *     <th>源类型</th>
     *     <th>分发到</th>
     * </tr>
     * <tr>
     *     <td>ActiveEvent</td>
     *     <td>任何</td>
     *     <td>event.dispatch()</td>
     * </tr>
     * <tr>
     *     <td>其他</td>
     *     <td>Component</td>
     *     <td>source.dispatchEvent(AWTEvent)</td>
     * </tr>
     * <tr>
     *     <td>其他</td>
     *     <td>MenuComponent</td>
     *     <td>source.dispatchEvent(AWTEvent)</td>
     * </tr>
     * <tr>
     *     <td>其他</td>
     *     <td>其他</td>
     *     <td>无操作（忽略）</td>
     * </tr>
     * </table>
     * <p>
     * @param event <code>java.awt.AWTEvent</code> 的一个实例，或其子类
     * @throws NullPointerException 如果 <code>event</code> 为 <code>null</code>
     * @since           1.2
     */
    protected void dispatchEvent(final AWTEvent event) {
        final Object src = event.getSource();
        final PrivilegedAction<Void> action = new PrivilegedAction<Void>() {
            public Void run() {
                // 如果 fwDispatcher 已安装且我们已经在分发线程上（例如执行 DefaultKeyboardFocusManager.sendMessage），
                // 则直接分发事件。
                if (fwDispatcher == null || isDispatchThreadImpl()) {
                    dispatchEventImpl(event, src);
                } else {
                    fwDispatcher.scheduleDispatch(new Runnable() {
                        @Override
                        public void run() {
                            if (dispatchThread.filterAndCheckEvent(event)) {
                                dispatchEventImpl(event, src);
                            }
                        }
                    });
                }
                return null;
            }
        };

        final AccessControlContext stack = AccessController.getContext();
        final AccessControlContext srcAcc = getAccessControlContextFrom(src);
        final AccessControlContext eventAcc = event.getAccessControlContext();
        if (srcAcc == null) {
            javaSecurityAccess.doIntersectionPrivilege(action, stack, eventAcc);
        } else {
            javaSecurityAccess.doIntersectionPrivilege(
                new PrivilegedAction<Void>() {
                    public Void run() {
                        javaSecurityAccess.doIntersectionPrivilege(action, eventAcc);
                        return null;
                    }
                }, stack, srcAcc);
        }
    }

    private static AccessControlContext getAccessControlContextFrom(Object src) {
        return src instanceof Component ?
            ((Component)src).getAccessControlContext() :
            src instanceof MenuComponent ?
                ((MenuComponent)src).getAccessControlContext() :
                src instanceof TrayIcon ?
                    ((TrayIcon)src).getAccessControlContext() :
                    null;
    }

    /**
     * 从正确的 AccessControlContext 下由 dispatchEvent() 调用
     */
    private void dispatchEventImpl(final AWTEvent event, final Object src) {
        event.isPosted = true;
        if (event instanceof ActiveEvent) {
            // 这可能会成为唯一的分发方法。
            setCurrentEventAndMostRecentTimeImpl(event);
            ((ActiveEvent)event).dispatch();
        } else if (src instanceof Component) {
            ((Component)src).dispatchEvent(event);
            event.dispatched();
        } else if (src instanceof MenuComponent) {
            ((MenuComponent)src).dispatchEvent(event);
        } else if (src instanceof TrayIcon) {
            ((TrayIcon)src).dispatchEvent(event);
        } else if (src instanceof AWTAutoShutdown) {
            if (noEvents()) {
                dispatchThread.stopDispatching();
            }
        } else {
            if (getEventLog().isLoggable(PlatformLogger.Level.FINE)) {
                getEventLog().fine("无法分发事件: " + event);
            }
        }
    }

    /**
     * 返回最近具有时间戳且从与调用线程关联的 <code>EventQueue</code> 分发的事件的时间戳。如果当前正在分发具有时间戳的事件，
     * 则返回其时间戳。如果尚未分发任何事件，则返回 EventQueue 的初始化时间。在当前版本的 JDK 中，只有 <code>InputEvent</code>s、
     * <code>ActionEvent</code>s 和 <code>InvocationEvent</code>s 具有时间戳；但是，未来版本的 JDK 可能会为其他事件类型添加时间戳。
     * 注意，此方法应仅从应用程序的 {@link #isDispatchThread 事件分发线程} 调用。如果从其他线程调用此方法，将返回当前系统时间（由
     * <code>System.currentTimeMillis()</code> 报告）。
     *
     * @return 最后分发的 <code>InputEvent</code>、<code>ActionEvent</code> 或 <code>InvocationEvent</code> 的时间戳，
     *         或者如果此方法在事件分发线程之外的线程上调用，则返回 <code>System.currentTimeMillis()</code>
     * @see java.awt.event.InputEvent#getWhen
     * @see java.awt.event.ActionEvent#getWhen
     * @see java.awt.event.InvocationEvent#getWhen
     * @see #isDispatchThread
     *
     * @since 1.4
     */
    public static long getMostRecentEventTime() {
        return Toolkit.getEventQueue().getMostRecentEventTimeImpl();
    }
    private long getMostRecentEventTimeImpl() {
        pushPopLock.lock();
        try {
            return (Thread.currentThread() == dispatchThread)
                ? mostRecentEventTime
                : System.currentTimeMillis();
        } finally {
            pushPopLock.unlock();
        }
    }

    /**
     * @return 所有线程上的最近事件时间。
     */
    long getMostRecentEventTimeEx() {
        pushPopLock.lock();
        try {
            return mostRecentEventTime;
        } finally {
            pushPopLock.unlock();
        }
    }

    /**
     * 返回与调用线程关联的 <code>EventQueue</code> 当前正在分发的事件。如果方法需要访问事件，但设计时未接收事件的引用作为参数，
     * 这将非常有用。注意，此方法应仅从应用程序的事件分发线程调用。如果从其他线程调用此方法，将返回 null。
     *
     * @return 当前正在分发的事件，或者如果此方法在事件分发线程之外的线程上调用，则返回 null
     * @since 1.4
     */
    public static AWTEvent getCurrentEvent() {
        return Toolkit.getEventQueue().getCurrentEventImpl();
    }
    private AWTEvent getCurrentEventImpl() {
        pushPopLock.lock();
        try {
                return (Thread.currentThread() == dispatchThread)
                ? currentEvent.get()
                : null;
        } finally {
            pushPopLock.unlock();
        }
    }

    /**
     * 用指定的 <code>EventQueue</code> 替换现有的 <code>EventQueue</code>。任何待处理的事件将被转移到新的 <code>EventQueue</code>
     * 以供其处理。
     *
     * @param newEventQueue 要使用的 <code>EventQueue</code>（或其子类）实例
     * @see      java.awt.EventQueue#pop
     * @throws NullPointerException 如果 <code>newEventQueue</code> 为 <code>null</code>
     * @since           1.2
     */
    public void push(EventQueue newEventQueue) {
        if (getEventLog().isLoggable(PlatformLogger.Level.FINE)) {
            getEventLog().fine("EventQueue.push(" + newEventQueue + ")");
        }


                    pushPopLock.lock();
        try {
            EventQueue topQueue = this;
            while (topQueue.nextQueue != null) {
                topQueue = topQueue.nextQueue;
            }
            if (topQueue.fwDispatcher != null) {
                throw new RuntimeException("向具有 fwDispatcher 的队列推送");
            }
            if ((topQueue.dispatchThread != null) &&
                (topQueue.dispatchThread.getEventQueue() == this))
            {
                newEventQueue.dispatchThread = topQueue.dispatchThread;
                topQueue.dispatchThread.setEventQueue(newEventQueue);
            }

            // 将所有事件向前传递到新的 EventQueue。
            while (topQueue.peekEvent() != null) {
                try {
                    // 使用 getNextEventPrivate()，因为它不会调用 flushPendingEvents()
                    newEventQueue.postEventPrivate(topQueue.getNextEventPrivate());
                } catch (InterruptedException ie) {
                    if (getEventLog().isLoggable(PlatformLogger.Level.FINE)) {
                        getEventLog().fine("中断推送", ie);
                    }
                }
            }

            if (topQueue.dispatchThread != null) {
                // 唤醒在 getNextEvent() 中等待的 EDT，以便它可以
                // 捕获新的 EventQueue。在分配 topQueue.nextQueue 之前发布唤醒事件，否则事件将
                // 转到 newEventQueue
                topQueue.postEventPrivate(new InvocationEvent(topQueue, dummyRunnable));
            }

            newEventQueue.previousQueue = topQueue;
            topQueue.nextQueue = newEventQueue;

            if (appContext.get(AppContext.EVENT_QUEUE_KEY) == topQueue) {
                appContext.put(AppContext.EVENT_QUEUE_KEY, newEventQueue);
            }

            pushPopCond.signalAll();
        } finally {
            pushPopLock.unlock();
        }
    }

    /**
     * 停止使用此 <code>EventQueue</code> 分发事件。
     * 任何待处理的事件将被转移到前一个
     * <code>EventQueue</code> 以进行处理。
     * <p>
     * 警告：为了避免死锁，不要在子类中声明此方法
     * 同步。
     *
     * @exception EmptyStackException 如果在此 <code>EventQueue</code> 上没有进行前一个推送
     * @see      java.awt.EventQueue#push
     * @since           1.2
     */
    protected void pop() throws EmptyStackException {
        if (getEventLog().isLoggable(PlatformLogger.Level.FINE)) {
            getEventLog().fine("EventQueue.pop(" + this + ")");
        }

        pushPopLock.lock();
        try {
            EventQueue topQueue = this;
            while (topQueue.nextQueue != null) {
                topQueue = topQueue.nextQueue;
            }
            EventQueue prevQueue = topQueue.previousQueue;
            if (prevQueue == null) {
                throw new EmptyStackException();
            }

            topQueue.previousQueue = null;
            prevQueue.nextQueue = null;

            // 将所有事件回传到前一个 EventQueue。
            while (topQueue.peekEvent() != null) {
                try {
                    prevQueue.postEventPrivate(topQueue.getNextEventPrivate());
                } catch (InterruptedException ie) {
                    if (getEventLog().isLoggable(PlatformLogger.Level.FINE)) {
                        getEventLog().fine("中断弹出", ie);
                    }
                }
            }

            if ((topQueue.dispatchThread != null) &&
                (topQueue.dispatchThread.getEventQueue() == this))
            {
                prevQueue.dispatchThread = topQueue.dispatchThread;
                topQueue.dispatchThread.setEventQueue(prevQueue);
            }

            if (appContext.get(AppContext.EVENT_QUEUE_KEY) == this) {
                appContext.put(AppContext.EVENT_QUEUE_KEY, prevQueue);
            }

            // 唤醒在 getNextEvent() 中等待的 EDT，以便它可以
            // 捕获新的 EventQueue
            topQueue.postEventPrivate(new InvocationEvent(topQueue, dummyRunnable));

            pushPopCond.signalAll();
        } finally {
            pushPopLock.unlock();
        }
    }

    /**
     * 创建与此
     * 事件队列关联的新 {@code secondary loop}。使用 {@link SecondaryLoop#enter} 和
     * {@link SecondaryLoop#exit} 方法启动和停止
     * 事件循环并从该队列分发事件。
     *
     * @return secondaryLoop 一个新的辅助循环对象，可以
     *                       用于启动新的嵌套事件循环并从该队列分发事件
     *
     * @see SecondaryLoop#enter
     * @see SecondaryLoop#exit
     *
     * @since 1.7
     */
    public SecondaryLoop createSecondaryLoop() {
        return createSecondaryLoop(null, null, 0);
    }

    private class FwSecondaryLoopWrapper implements SecondaryLoop {
        final private SecondaryLoop loop;
        final private EventFilter filter;

        public FwSecondaryLoopWrapper(SecondaryLoop loop, EventFilter filter) {
            this.loop = loop;
            this.filter = filter;
        }

        @Override
        public boolean enter() {
            if (filter != null) {
                dispatchThread.addEventFilter(filter);
            }
            return loop.enter();
        }

        @Override
        public boolean exit() {
            if (filter != null) {
                dispatchThread.removeEventFilter(filter);
            }
            return loop.exit();
        }
    }

    SecondaryLoop createSecondaryLoop(Conditional cond, EventFilter filter, long interval) {
        pushPopLock.lock();
        try {
            if (nextQueue != null) {
                // 将请求转发到事件队列堆栈的顶部
                return nextQueue.createSecondaryLoop(cond, filter, interval);
            }
            if (fwDispatcher != null) {
                return new FwSecondaryLoopWrapper(fwDispatcher.createSecondaryLoop(), filter);
            }
            if (dispatchThread == null) {
                initDispatchThread();
            }
            return new WaitDispatchSupport(dispatchThread, cond, filter, interval);
        } finally {
            pushPopLock.unlock();
        }
    }

    /**
     * 如果调用线程是
     * {@link Toolkit#getSystemEventQueue 当前 AWT 事件队列}的
     * 分发线程，则返回 true。使用此方法确保特定的
     * 任务正在（或不在）该线程上执行。
     * <p>
     * 注意：使用 {@link #invokeLater} 或 {@link #invokeAndWait}
     * 方法在 {@link Toolkit#getSystemEventQueue 当前 AWT 事件队列}的
     * 分发线程上执行任务。
     * <p>
     *
     * @return 如果在 {@link Toolkit#getSystemEventQueue 当前 AWT 事件队列}的
     * 分发线程上运行，则返回 true
     * @see             #invokeLater
     * @see             #invokeAndWait
     * @see             Toolkit#getSystemEventQueue
     * @since           1.2
     */
    public static boolean isDispatchThread() {
        EventQueue eq = Toolkit.getEventQueue();
        return eq.isDispatchThreadImpl();
    }

    final boolean isDispatchThreadImpl() {
        EventQueue eq = this;
        pushPopLock.lock();
        try {
            EventQueue next = eq.nextQueue;
            while (next != null) {
                eq = next;
                next = eq.nextQueue;
            }
            if (eq.fwDispatcher != null) {
                return eq.fwDispatcher.isDispatchThread();
            }
            return (Thread.currentThread() == eq.dispatchThread);
        } finally {
            pushPopLock.unlock();
        }
    }

    final void initDispatchThread() {
        pushPopLock.lock();
        try {
            if (dispatchThread == null && !threadGroup.isDestroyed() && !appContext.isDisposed()) {
                dispatchThread = AccessController.doPrivileged(
                    new PrivilegedAction<EventDispatchThread>() {
                        public EventDispatchThread run() {
                            EventDispatchThread t =
                                new EventDispatchThread(threadGroup,
                                                        name,
                                                        EventQueue.this);
                            t.setContextClassLoader(classLoader);
                            t.setPriority(Thread.NORM_PRIORITY + 1);
                            t.setDaemon(false);
                            AWTAutoShutdown.getInstance().notifyThreadBusy(t);
                            return t;
                        }
                    }
                );
                dispatchThread.start();
            }
        } finally {
            pushPopLock.unlock();
        }
    }

    final void detachDispatchThread(EventDispatchThread edt) {
        /*
         * 最小化未发布事件的丢弃可能性
         */
        SunToolkit.flushPendingEvents(appContext);
        /*
         * 此同步块是为了确保事件分发线程不会在向关联事件队列发布新事件时死亡。
         * 这很重要，因为我们在向其队列发布新事件后通知事件分发线程忙碌，
         * 因此在该点上 EventQueue.dispatchThread 引用必须有效。
         */
        pushPopLock.lock();
        try {
            if (edt == dispatchThread) {
                dispatchThread = null;
            }
            AWTAutoShutdown.getInstance().notifyThreadFree(edt);
            /*
             * 事件在 EDT 事件泵停止后发布，因此启动另一个 EDT 来处理此事件
             */
            if (peekEvent() != null) {
                initDispatchThread();
            }
        } finally {
            pushPopLock.unlock();
        }
    }

    /*
     * 获取此 <code>EventQueue</code> 的 <code>EventDispatchThread</code>。
     * @return 与此事件队列关联的事件分发线程，如果此事件队列没有关联的工作线程，则返回 <code>null</code>
     * @see    java.awt.EventQueue#initDispatchThread
     * @see    java.awt.EventQueue#detachDispatchThread
     */
    final EventDispatchThread getDispatchThread() {
        pushPopLock.lock();
        try {
            return dispatchThread;
        } finally {
            pushPopLock.unlock();
        }
    }

    /*
     * 移除指定源对象的任何待处理事件。
     * 如果 removeAllEvents 参数为 <code>true</code>，则移除指定源对象的所有事件；
     * 如果为 <code>false</code>，则保留 <code>SequencedEvent</code>、<code>SentEvent</code>、
     * <code>FocusEvent</code>、<code>WindowEvent</code>、<code>KeyEvent</code> 和
     * <code>InputMethodEvent</code>，但移除所有其他事件。
     *
     * 通常由源的 <code>removeNotify</code> 方法调用此方法。
     */
    final void removeSourceEvents(Object source, boolean removeAllEvents) {
        SunToolkit.flushPendingEvents(appContext);
        pushPopLock.lock();
        try {
            for (int i = 0; i < NUM_PRIORITIES; i++) {
                EventQueueItem entry = queues[i].head;
                EventQueueItem prev = null;
                while (entry != null) {
                    if ((entry.event.getSource() == source)
                        && (removeAllEvents
                            || ! (entry.event instanceof SequencedEvent
                                  || entry.event instanceof SentEvent
                                  || entry.event instanceof FocusEvent
                                  || entry.event instanceof WindowEvent
                                  || entry.event instanceof KeyEvent
                                  || entry.event instanceof InputMethodEvent)))
                    {
                        if (entry.event instanceof SequencedEvent) {
                            ((SequencedEvent)entry.event).dispose();
                        }
                        if (entry.event instanceof SentEvent) {
                            ((SentEvent)entry.event).dispose();
                        }
                        if (entry.event instanceof InvocationEvent) {
                            AWTAccessor.getInvocationEventAccessor()
                                    .dispose((InvocationEvent)entry.event);
                        }
                        if (prev == null) {
                            queues[i].head = entry.next;
                        } else {
                            prev.next = entry.next;
                        }
                        uncacheEQItem(entry);
                    } else {
                        prev = entry;
                    }
                    entry = entry.next;
                }
                queues[i].tail = prev;
            }
        } finally {
            pushPopLock.unlock();
        }
    }

    synchronized long getMostRecentKeyEventTime() {
        pushPopLock.lock();
        try {
            return mostRecentKeyEventTime;
        } finally {
            pushPopLock.unlock();
        }
    }

    static void setCurrentEventAndMostRecentTime(AWTEvent e) {
        Toolkit.getEventQueue().setCurrentEventAndMostRecentTimeImpl(e);
    }
    private void setCurrentEventAndMostRecentTimeImpl(AWTEvent e) {
        pushPopLock.lock();
        try {
            if (Thread.currentThread() != dispatchThread) {
                return;
            }

            currentEvent = new WeakReference<>(e);

            // 这一系列的 'instanceof' 检查应被替换为多态类型（例如，声明 getWhen() 方法的接口）。
            // 但是，这需要我们将此类公开，或将其放置在 sun.awt 中。这两种方法都不被推荐。
            // 因此，目前我们采用这种变通方法。
            //
            // 在 tiger 中，我们将可能为所有事件提供时间戳，因此这将不再是一个问题。
            long mostRecentEventTime2 = Long.MIN_VALUE;
            if (e instanceof InputEvent) {
                InputEvent ie = (InputEvent)e;
                mostRecentEventTime2 = ie.getWhen();
                if (e instanceof KeyEvent) {
                    mostRecentKeyEventTime = ie.getWhen();
                }
            } else if (e instanceof InputMethodEvent) {
                InputMethodEvent ime = (InputMethodEvent)e;
                mostRecentEventTime2 = ime.getWhen();
            } else if (e instanceof ActionEvent) {
                ActionEvent ae = (ActionEvent)e;
                mostRecentEventTime2 = ae.getWhen();
            } else if (e instanceof InvocationEvent) {
                InvocationEvent ie = (InvocationEvent)e;
                mostRecentEventTime2 = ie.getWhen();
            }
            mostRecentEventTime = Math.max(mostRecentEventTime, mostRecentEventTime2);
        } finally {
            pushPopLock.unlock();
        }
    }


                /**
     * 使 <code>runnable</code> 的 <code>run</code>
     * 方法在 {@link #isDispatchThread 事件分发线程} 中
     * {@link Toolkit#getSystemEventQueue 系统事件队列} 被调用。
     * 这将在所有待处理事件处理完毕后发生。
     *
     * @param runnable  <code>Runnable</code> 对象，其 <code>run</code>
     *                  方法应异步地在
     *                  {@link #isDispatchThread 事件分发线程} 中
     *                  {@link Toolkit#getSystemEventQueue 系统事件队列} 被执行
     * @see             #invokeAndWait
     * @see             Toolkit#getSystemEventQueue
     * @see             #isDispatchThread
     * @since           1.2
     */
    public static void invokeLater(Runnable runnable) {
        Toolkit.getEventQueue().postEvent(
            new InvocationEvent(Toolkit.getDefaultToolkit(), runnable));
    }

    /**
     * 使 <code>runnable</code> 的 <code>run</code>
     * 方法在 {@link #isDispatchThread 事件分发线程} 中
     * {@link Toolkit#getSystemEventQueue 系统事件队列} 被调用。
     * 这将在所有待处理事件处理完毕后发生。
     * 调用将阻塞直到这发生。如果从
     * {@link #isDispatchThread 事件分发线程} 调用此方法，
     * 将抛出一个错误。
     *
     * @param runnable  <code>Runnable</code> 对象，其 <code>run</code>
     *                  方法应同步地在
     *                  {@link #isDispatchThread 事件分发线程} 中
     *                  {@link Toolkit#getSystemEventQueue 系统事件队列} 被执行
     * @exception       InterruptedException  如果任何线程中断了此线程
     * @exception       InvocationTargetException  如果运行 <code>runnable</code> 时抛出异常
     * @see             #invokeLater
     * @see             Toolkit#getSystemEventQueue
     * @see             #isDispatchThread
     * @since           1.2
     */
    public static void invokeAndWait(Runnable runnable)
        throws InterruptedException, InvocationTargetException
    {
        invokeAndWait(Toolkit.getDefaultToolkit(), runnable);
    }

    static void invokeAndWait(Object source, Runnable runnable)
        throws InterruptedException, InvocationTargetException
    {
        if (EventQueue.isDispatchThread()) {
            throw new Error("Cannot call invokeAndWait from the event dispatcher thread");
        }

        class AWTInvocationLock {}
        Object lock = new AWTInvocationLock();

        InvocationEvent event =
            new InvocationEvent(source, runnable, lock, true);

        synchronized (lock) {
            Toolkit.getEventQueue().postEvent(event);
            while (!event.isDispatched()) {
                lock.wait();
            }
        }

        Throwable eventThrowable = event.getThrowable();
        if (eventThrowable != null) {
            throw new InvocationTargetException(eventThrowable);
        }
    }

    /*
     * 从 PostEventQueue.postEvent 调用，以通知出现新事件。
     * 首先继续到事件队列堆栈顶部的 EventQueue，
     * 然后如果存在关联的分发线程则通知该线程，
     * 否则启动一个新的线程。
     */
    private void wakeup(boolean isShutdown) {
        pushPopLock.lock();
        try {
            if (nextQueue != null) {
                // 将调用转发到事件队列堆栈的顶部。
                nextQueue.wakeup(isShutdown);
            } else if (dispatchThread != null) {
                pushPopCond.signalAll();
            } else if (!isShutdown) {
                initDispatchThread();
            }
        } finally {
            pushPopLock.unlock();
        }
    }

    // 该方法由 AWTAccessor 用于 javafx/AWT 单线程模式。
    private void setFwDispatcher(FwDispatcher dispatcher) {
        if (nextQueue != null) {
            nextQueue.setFwDispatcher(dispatcher);
        } else {
            fwDispatcher = dispatcher;
        }
    }
}

/**
 * Queue 对象持有指向一个内部队列的开头和结尾的指针。
 * EventQueue 对象由多个内部队列组成，每个队列对应 EventQueue 支持的一个优先级。
 * 特定内部队列上的所有事件具有相同的优先级。
 */
class Queue {
    EventQueueItem head;
    EventQueueItem tail;
}
