/*
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.awt.dnd;

import java.awt.AWTEventMulticaster;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.EventListener;


/**
 * 一个扩展 <code>AWTEventMulticaster</code> 的类，用于实现 java.awt.dnd 包中定义的拖放事件的高效且线程安全的多播事件分发。
 *
 * @since       1.4
 * @see AWTEventMulticaster
 */

class DnDEventMulticaster extends AWTEventMulticaster
    implements DragSourceListener, DragSourceMotionListener {

    /**
     * 创建一个事件多播实例，将 listener-a 与 listener-b 链接。输入参数 <code>a</code> 和 <code>b</code>
     * 不应为 <code>null</code>，但实现可能有所不同，是否在该情况下抛出 <code>NullPointerException</code>。
     *
     * @param a listener-a
     * @param b listener-b
     */
    protected DnDEventMulticaster(EventListener a, EventListener b) {
        super(a,b);
    }

    /**
     * 通过在 listener-a 和 listener-b 上调用 <code>dragEnter</code> 来处理 <code>DragSourceDragEvent</code>。
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dragEnter(DragSourceDragEvent dsde) {
        ((DragSourceListener)a).dragEnter(dsde);
        ((DragSourceListener)b).dragEnter(dsde);
    }

    /**
     * 通过在 listener-a 和 listener-b 上调用 <code>dragOver</code> 来处理 <code>DragSourceDragEvent</code>。
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dragOver(DragSourceDragEvent dsde) {
        ((DragSourceListener)a).dragOver(dsde);
        ((DragSourceListener)b).dragOver(dsde);
    }

    /**
     * 通过在 listener-a 和 listener-b 上调用 <code>dropActionChanged</code> 来处理 <code>DragSourceDragEvent</code>。
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dropActionChanged(DragSourceDragEvent dsde) {
        ((DragSourceListener)a).dropActionChanged(dsde);
        ((DragSourceListener)b).dropActionChanged(dsde);
    }

    /**
     * 通过在 listener-a 和 listener-b 上调用 <code>dragExit</code> 来处理 <code>DragSourceEvent</code>。
     *
     * @param dse the <code>DragSourceEvent</code>
     */
    public void dragExit(DragSourceEvent dse) {
        ((DragSourceListener)a).dragExit(dse);
        ((DragSourceListener)b).dragExit(dse);
    }

    /**
     * 通过在 listener-a 和 listener-b 上调用 <code>dragDropEnd</code> 来处理 <code>DragSourceDropEvent</code>。
     *
     * @param dsde the <code>DragSourceDropEvent</code>
     */
    public void dragDropEnd(DragSourceDropEvent dsde) {
        ((DragSourceListener)a).dragDropEnd(dsde);
        ((DragSourceListener)b).dragDropEnd(dsde);
    }

    /**
     * 通过在 listener-a 和 listener-b 上调用 <code>dragMouseMoved</code> 来处理 <code>DragSourceDragEvent</code>。
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dragMouseMoved(DragSourceDragEvent dsde) {
        ((DragSourceMotionListener)a).dragMouseMoved(dsde);
        ((DragSourceMotionListener)b).dragMouseMoved(dsde);
    }

    /**
     * 将 drag-source-listener-a 与 drag-source-listener-b 相加，并返回结果的多播监听器。
     *
     * @param a drag-source-listener-a
     * @param b drag-source-listener-b
     */
    public static DragSourceListener add(DragSourceListener a,
                                         DragSourceListener b) {
        return (DragSourceListener)addInternal(a, b);
    }

    /**
     * 将 drag-source-motion-listener-a 与 drag-source-motion-listener-b 相加，并返回结果的多播监听器。
     *
     * @param a drag-source-motion-listener-a
     * @param b drag-source-motion-listener-b
     */
    public static DragSourceMotionListener add(DragSourceMotionListener a,
                                               DragSourceMotionListener b) {
        return (DragSourceMotionListener)addInternal(a, b);
    }

    /**
     * 从 drag-source-listener-l 中移除旧的 drag-source-listener，并返回结果的多播监听器。
     *
     * @param l drag-source-listener-l
     * @param oldl 被移除的 drag-source-listener
     */
    public static DragSourceListener remove(DragSourceListener l,
                                            DragSourceListener oldl) {
        return (DragSourceListener)removeInternal(l, oldl);
    }

    /**
     * 从 drag-source-motion-listener-l 中移除旧的 drag-source-motion-listener，并返回结果的多播监听器。
     *
     * @param l drag-source-motion-listener-l
     * @param ol 被移除的 drag-source-motion-listener
     */
    public static DragSourceMotionListener remove(DragSourceMotionListener l,
                                                  DragSourceMotionListener ol) {
        return (DragSourceMotionListener)removeInternal(l, ol);
    }

    /**
     * 返回将 listener-a 和 listener-b 相加后的结果多播监听器。
     * 如果 listener-a 为 null，返回 listener-b；
     * 如果 listener-b 为 null，返回 listener-a；
     * 如果两者都不为 null，则创建并返回一个新的 AWTEventMulticaster 实例，将 a 与 b 链接。
     * @param a event listener-a
     * @param b event listener-b
     */
    protected static EventListener addInternal(EventListener a, EventListener b) {
        if (a == null)  return b;
        if (b == null)  return a;
        return new DnDEventMulticaster(a, b);
    }

    /**
     * 从这个多播监听器中移除一个监听器，并返回结果的多播监听器。
     * @param oldl 被移除的监听器
     */
    protected EventListener remove(EventListener oldl) {
        if (oldl == a)  return b;
        if (oldl == b)  return a;
        EventListener a2 = removeInternal(a, oldl);
        EventListener b2 = removeInternal(b, oldl);
        if (a2 == a && b2 == b) {
            return this;        // 它不在这里
        }
        return addInternal(a2, b2);
    }

    /**
     * 从 listener-l 中移除旧的监听器后，返回结果的多播监听器。
     * 如果 listener-l 等于旧的监听器 OR listener-l 为 null，返回 null。
     * 否则，如果 listener-l 是 AWTEventMulticaster 的实例，则从它中移除旧的监听器。
     * 否则，返回 listener l。
     * @param l 被移除的监听器
     * @param oldl 被移除的监听器
     */
    protected static EventListener removeInternal(EventListener l, EventListener oldl) {
        if (l == oldl || l == null) {
            return null;
        } else if (l instanceof DnDEventMulticaster) {
            return ((DnDEventMulticaster)l).remove(oldl);
        } else {
            return l;           // 它不在这里
        }
    }

    protected static void save(ObjectOutputStream s, String k, EventListener l)
      throws IOException {
        AWTEventMulticaster.save(s, k, l);
    }
}
