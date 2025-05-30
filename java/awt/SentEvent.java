/*
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
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

import sun.awt.AppContext;
import sun.awt.SunToolkit;

/**
 * 一个嵌套 AWTEvent 的包装标签，表示该事件是从另一个 AppContext 发送的。目标 AppContext 应该处理该事件，
 * 即使它当前正在等待处理 SequencedEvent 或另一个 SentEvent。
 *
 * @author David Mendenhall
 */
class SentEvent extends AWTEvent implements ActiveEvent {
    /*
     * 序列化版本 UID
     */
    private static final long serialVersionUID = -383615247028828931L;

    static final int ID =
        java.awt.event.FocusEvent.FOCUS_LAST + 2;

    boolean dispatched;
    private AWTEvent nested;
    private AppContext toNotify;

    SentEvent() {
        this(null);
    }
    SentEvent(AWTEvent nested) {
        this(nested, null);
    }
    SentEvent(AWTEvent nested, AppContext toNotify) {
        super((nested != null)
                  ? nested.getSource()
                  : Toolkit.getDefaultToolkit(),
              ID);
        this.nested = nested;
        this.toNotify = toNotify;
    }

    public void dispatch() {
        try {
            if (nested != null) {
                Toolkit.getEventQueue().dispatchEvent(nested);
            }
        } finally {
            dispatched = true;
            if (toNotify != null) {
                SunToolkit.postEvent(toNotify, new SentEvent());
            }
            synchronized (this) {
                notifyAll();
            }
        }
    }
    final void dispose() {
        dispatched = true;
        if (toNotify != null) {
            SunToolkit.postEvent(toNotify, new SentEvent());
        }
        synchronized (this) {
            notifyAll();
        }
    }
}
