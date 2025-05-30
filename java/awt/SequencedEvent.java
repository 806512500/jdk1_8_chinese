/*
 * Copyright (c) 2000, 2019, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Iterator;
import java.util.LinkedList;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

/**
 * 一种机制，确保一系列 AWTEvents 按精确顺序执行，即使在多个 AppContexts 之间也是如此。嵌套事件将按其包装的 SequencedEvents 构造的顺序分发。唯一的例外是，如果嵌套事件的目标的对等体在包装的 SequencedEvent 能够被分发之前被销毁（通过调用 Component.removeNotify），则嵌套事件将永远不会被分发。
 *
 * @author David Mendenhall
 */
class SequencedEvent extends AWTEvent implements ActiveEvent {
    /*
     * 序列化版本 ID
     */
    private static final long serialVersionUID = 547742659238625067L;

    private static final int ID =
        java.awt.event.FocusEvent.FOCUS_LAST + 1;
    private static final LinkedList<SequencedEvent> list = new LinkedList<>();

    private final AWTEvent nested;
    private AppContext appContext;
    private boolean disposed;
    private final LinkedList<AWTEvent> pendingEvents = new LinkedList<>();

    static {
        AWTAccessor.setSequencedEventAccessor(new AWTAccessor.SequencedEventAccessor() {
            public AWTEvent getNested(AWTEvent sequencedEvent) {
                return ((SequencedEvent)sequencedEvent).nested;
            }
            public boolean isSequencedEvent(AWTEvent event) {
                return event instanceof SequencedEvent;
            }
        });
    }

    private static final class SequencedEventsFilter implements EventFilter {
        private final SequencedEvent currentSequencedEvent;
        private SequencedEventsFilter(SequencedEvent currentSequencedEvent) {
            this.currentSequencedEvent = currentSequencedEvent;
        }
        @Override
        public FilterAction acceptEvent(AWTEvent ev) {
            if (ev.getID() == ID) {
                // 仅在事件位于 SequencedEvent.list 之前时才向前分发。否则，将其保留以供稍后重新发布。
                synchronized (SequencedEvent.class) {
                    Iterator<SequencedEvent> it = list.iterator();
                    while (it.hasNext()) {
                        SequencedEvent iev = it.next();
                        if (iev.equals(currentSequencedEvent)) {
                            break;
                        } else if (iev.equals(ev)) {
                            return FilterAction.ACCEPT;
                        }
                    }
                }
            } else if (ev.getID() == SentEvent.ID) {
                return FilterAction.ACCEPT;
            }
            currentSequencedEvent.pendingEvents.add(ev);
            return FilterAction.REJECT;
        }
    }

    /**
     * 构造一个新的 SequencedEvent，该事件将分发指定的嵌套事件。
     *
     * @param nested 由此 SequencedEvent 的 dispatch() 方法分发的 AWTEvent
     */
    public SequencedEvent(AWTEvent nested) {
        super(nested.getSource(), ID);
        this.nested = nested;
        // 所有包装在 SequencedEvents 中的 AWTEvents（至少目前）都是由系统隐式生成的
        SunToolkit.setSystemGenerated(nested);
        synchronized (SequencedEvent.class) {
            list.add(this);
        }
    }

    /**
     * 在所有先前的嵌套事件被分发或处理后分发嵌套事件。如果在所有先前的嵌套事件被分发之前调用此方法，则此方法将阻塞，直到达到该点。
     * 在等待期间处理已处理的 AppContext 的嵌套事件。
     *
     * 注意：锁定协议。由于 dispose() 可能会获取 EventQueue 锁，因此 dispatch() 在持有列表锁时绝不能调用 dispose()，因为 EventQueue 锁在分发期间持有。锁应按相同顺序获取。
     */
    public final void dispatch() {
        try {
            appContext = AppContext.getAppContext();

            if (getFirst() != this) {
                if (EventQueue.isDispatchThread()) {
                    EventDispatchThread edt = (EventDispatchThread)
                        Thread.currentThread();
                    edt.pumpEventsForFilter(() -> !SequencedEvent.this.isFirstOrDisposed(),
                            new SequencedEventsFilter(this));
                } else {
                    while(!isFirstOrDisposed()) {
                        synchronized (SequencedEvent.class) {
                            try {
                                SequencedEvent.class.wait(1000);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                }
            }

            if (!disposed) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().
                    setCurrentSequencedEvent(this);
                Toolkit.getEventQueue().dispatchEvent(nested);
            }
        } finally {
            dispose();
        }
    }

    /**
     * 仅在事件存在且嵌套源 appContext 已被处理时为 true。
     */
    private final static boolean isOwnerAppContextDisposed(SequencedEvent se) {
        if (se != null) {
            Object target = se.nested.getSource();
            if (target instanceof Component) {
                return ((Component)target).appContext.isDisposed();
            }
        }
        return false;
    }

    /**
     * Sequenced 事件按顺序分发，因此在我们成为队列中的第一个 sequenced 事件（即轮到我们）之前，我们不能分发。但在等待分发时，事件可能因多种原因被处理。
     */
    public final boolean isFirstOrDisposed() {
        if (disposed) {
            return true;
        }
        // getFirstWithContext 可能会处理此事件
        return this == getFirstWithContext() || disposed;
    }

    private final synchronized static SequencedEvent getFirst() {
        return (SequencedEvent)list.getFirst();
    }

    /* 处理来自已处理 AppContext 的所有事件
     * 返回第一个有效事件
     */
    private final static SequencedEvent getFirstWithContext() {
        SequencedEvent first = getFirst();
        while(isOwnerAppContextDisposed(first)) {
            first.dispose();
            first = getFirst();
        }
        return first;
    }

    /**
     * 处理此实例。此方法在嵌套事件被分发和处理后，或在嵌套事件的目标的对等体被销毁（通过调用 Component.removeNotify）时调用。
     *
     * 注意：锁定协议。由于 SunToolkit.postEvent 可能会获取 EventQueue 锁，因此在持有列表锁时绝不能调用它，因为 EventQueue 锁在分发期间持有，而 dispatch() 将获取列表锁。锁应按相同顺序获取。
     */
    final void dispose() {
      synchronized (SequencedEvent.class) {
            if (disposed) {
                return;
            }
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().
                    getCurrentSequencedEvent() == this) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().
                    setCurrentSequencedEvent(null);
            }
            disposed = true;
        }

        SequencedEvent next = null;

        synchronized (SequencedEvent.class) {
          SequencedEvent.class.notifyAll();

          if (list.getFirst() == this) {
              list.removeFirst();

              if (!list.isEmpty()) {
                    next = (SequencedEvent)list.getFirst();
              }
          } else {
              list.remove(this);
          }
      }
        // 唤醒等待的线程
        if (next != null && next.appContext != null) {
            SunToolkit.postEvent(next.appContext, new SentEvent());
        }

        for(AWTEvent e : pendingEvents) {
            SunToolkit.postEvent(appContext, e);
        }
    }
}
