
/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.InputEvent;
import java.awt.Component;
import java.awt.Point;

import java.io.InvalidObjectException;
import java.util.Collections;
import java.util.TooManyListenersException;
import java.util.ArrayList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * <code>DragGestureRecognizer</code> 是一个抽象基类，用于指定可以与特定
 * <code>Component</code> 关联的平台依赖的监听器，以识别平台依赖的拖动初始化手势。
 * <p>
 * 适当的 <code>DragGestureRecognizer</code> 子类实例可以从与特定 <code>Component</code>
 * 关联的 {@link DragSource} 或通过 <code>Toolkit</code> 对象的
 * {@link java.awt.Toolkit#createDragGestureRecognizer createDragGestureRecognizer()}
 * 方法获得。
 * <p>
 * 一旦 <code>DragGestureRecognizer</code> 与特定的 <code>Component</code> 关联，
 * 它将在该 <code>Component</code> 上注册适当的监听器接口，以跟踪传递给 <code>Component</code> 的输入事件。
 * <p>
 * 一旦 <code>DragGestureRecognizer</code> 识别出 <code>Component</code> 上的一系列事件为拖动初始化手势，
 * 它将通过调用其单播 <code>DragGestureListener</code> 的
 * {@link java.awt.dnd.DragGestureListener#dragGestureRecognized gestureRecognized()}
 * 方法来通知该监听器。
 * <P>
 * 当具体的 <code>DragGestureRecognizer</code> 实例检测到与其关联的 <code>Component</code> 上的拖动初始化手势时，
 * 它会向注册在其单播事件源上的 <code>DragGestureListener</code> 发送一个 {@link DragGestureEvent}。
 * 这个 <code>DragGestureListener</code> 负责在适当的情况下启动与之关联的
 * <code>DragSource</code> 的拖放操作。
 * <P>
 * @author Laurence P. G. Cable
 * @see java.awt.dnd.DragGestureListener
 * @see java.awt.dnd.DragGestureEvent
 * @see java.awt.dnd.DragSource
 */

public abstract class DragGestureRecognizer implements Serializable {

    private static final long serialVersionUID = 8996673345831063337L;

    /**
     * 构造一个新的 <code>DragGestureRecognizer</code>，给定用于此拖放操作的 <code>DragSource</code>，
     * 需要观察拖动初始化手势的 <code>Component</code>，此拖放操作支持的动作，以及检测到拖动初始化手势后要通知的
     * <code>DragGestureListener</code>。
     * <P>
     * @param ds 用于此拖放操作的 <code>DragSource</code>
     *
     * @param c 需要观察事件流以检测拖动初始化手势的 <code>Component</code>。
     * 如果此值为 <code>null</code>，则 <code>DragGestureRecognizer</code>
     * 不与任何 <code>Component</code> 关联。
     *
     * @param sa 此拖放操作将支持的 <code>DnDConstants</code> 集合（逻辑或）
     *
     * @param dgl 检测到拖动手势后要通知的 <code>DragGestureListener</code>
     * <P>
     * @throws IllegalArgumentException 如果 ds 为 <code>null</code>。
     */

    protected DragGestureRecognizer(DragSource ds, Component c, int sa, DragGestureListener dgl) {
        super();

        if (ds == null) throw new IllegalArgumentException("null DragSource");

        dragSource    = ds;
        component     = c;
        sourceActions = sa & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK);

        try {
            if (dgl != null) addDragGestureListener(dgl);
        } catch (TooManyListenersException tmle) {
            // 不会发生 ...
        }
    }

    /**
     * 构造一个新的 <code>DragGestureRecognizer</code>，给定用于此拖放操作的 <code>DragSource</code>，
     * 需要观察拖动初始化手势的 <code>Component</code>，以及此拖放操作支持的动作。
     * <P>
     * @param ds 用于此拖放操作的 <code>DragSource</code>
     *
     * @param c 需要观察事件流以检测拖动初始化手势的 <code>Component</code>。
     * 如果此值为 <code>null</code>，则 <code>DragGestureRecognizer</code>
     * 不与任何 <code>Component</code> 关联。
     *
     * @param sa 此拖放操作将支持的 <code>DnDConstants</code> 集合（逻辑或）
     * <P>
     * @throws IllegalArgumentException 如果 ds 为 <code>null</code>。
     */

    protected DragGestureRecognizer(DragSource ds, Component c, int sa) {
        this(ds, c, sa, null);
    }

    /**
     * 构造一个新的 <code>DragGestureRecognizer</code>，给定用于此拖放操作的 <code>DragSource</code>，
     * 需要观察拖动初始化手势的 <code>Component</code>。
     * <P>
     * @param ds 用于此拖放操作的 <code>DragSource</code>
     *
     * @param c 需要观察事件流以检测拖动初始化手势的 <code>Component</code>。
     * 如果此值为 <code>null</code>，则 <code>DragGestureRecognizer</code>
     * 不与任何 <code>Component</code> 关联。
     * <P>
     * @throws IllegalArgumentException 如果 ds 为 <code>null</code>。
     */

    protected DragGestureRecognizer(DragSource ds, Component c) {
        this(ds, c, DnDConstants.ACTION_NONE);
    }

    /**
     * 构造一个新的 <code>DragGestureRecognizer</code>，给定用于此拖放操作的 <code>DragSource</code>。
     * <P>
     * @param ds 用于此拖放操作的 <code>DragSource</code>
     * <P>
     * @throws IllegalArgumentException 如果 ds 为 <code>null</code>。
     */

    protected DragGestureRecognizer(DragSource ds) {
        this(ds, null);
    }

    /**
     * 在组件上注册此 DragGestureRecognizer 的监听器
     *
     * 子类必须覆盖此方法
     */

    protected abstract void registerListeners();

    /**
     * 在组件上注销此 DragGestureRecognizer 的监听器
     *
     * 子类必须覆盖此方法
     */

    protected abstract void unregisterListeners();

    /**
     * 返回此 <code>DragGestureRecognizer</code> 将用于处理拖放操作的 <code>DragSource</code>。
     * <P>
     * @return DragSource
     */

    public DragSource getDragSource() { return dragSource; }

    /**
     * 返回将由 <code>DragGestureRecognizer</code> 观察拖动初始化手势的 <code>Component</code>。
     * <P>
     * @return 与此 DragGestureRecognizer 关联的 Component
     */

    public synchronized Component getComponent() { return component; }

    /**
     * 设置与 DragGestureRecognizer 关联的 Component
     *
     * 作为副作用，将调用 registerListeners() 和 unregisterListeners()。
     * <P>
     * @param c <code>Component</code> 或 <code>null</code>
     */

    public synchronized void setComponent(Component c) {
        if (component != null && dragGestureListener != null)
            unregisterListeners();

        component = c;

        if (component != null && dragGestureListener != null)
            registerListeners();
    }

    /**
     * 返回此拖放操作将支持的动作类型。
     * <P>
     * @return 当前允许的源动作
     */

    public synchronized int getSourceActions() { return sourceActions; }

    /**
     * 设置此拖放操作允许的源拖动动作。
     * <P>
     * @param actions 允许的源拖动动作
     */

    public synchronized void setSourceActions(int actions) {
        sourceActions = actions & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK);
    }

    /**
     * 返回触发拖放操作的事件系列中的第一个事件。
     * <P>
     * @return 触发拖动手势的初始事件
     */

    public InputEvent getTriggerEvent() { return events.isEmpty() ? null : events.get(0); }

    /**
     * 重置识别器，如果当前正在识别手势，则忽略它。
     */

    public void resetRecognizer() { events.clear(); }

    /**
     * 注册一个新的 <code>DragGestureListener</code>。
     * <P>
     * @param dgl 要注册的 <code>DragGestureListener</code>
     * <P>
     * @throws java.util.TooManyListenersException 如果已经添加了 <code>DragGestureListener</code>。
     */

    public synchronized void addDragGestureListener(DragGestureListener dgl) throws TooManyListenersException {
        if (dragGestureListener != null)
            throw new TooManyListenersException();
        else {
            dragGestureListener = dgl;

            if (component != null) registerListeners();
        }
    }

    /**
     * 注销当前的 DragGestureListener
     * <P>
     * @param dgl 要注销的 <code>DragGestureListener</code>
     * <P>
     * @throws IllegalArgumentException 如果 dgl 不等于当前注册的 <code>DragGestureListener</code>。
     */

    public synchronized void removeDragGestureListener(DragGestureListener dgl) {
        if (dragGestureListener == null || !dragGestureListener.equals(dgl))
            throw new IllegalArgumentException();
        else {
            dragGestureListener = null;

            if (component != null) unregisterListeners();
        }
    }

    /**
     * 通知 DragGestureListener 拖放初始化手势已发生。然后重置识别器的状态。
     * <P>
     * @param dragAction 用户手势最初选择的动作
     * @param p 拖动手势的起始点（以组件坐标表示）
     */
    protected synchronized void fireDragGestureRecognized(int dragAction, Point p) {
        try {
            if (dragGestureListener != null) {
                dragGestureListener.dragGestureRecognized(new DragGestureEvent(this, dragAction, p, events));
            }
        } finally {
            events.clear();
        }
    }

    /**
     * 通过此 API 记录识别为拖放初始化手势的一部分的所有事件。
     * <P>
     * 此方法用于 <code>DragGestureRecognizer</code> 实现，将认为是拖放操作的一部分的
     * <code>InputEvent</code> 子类添加到此 <code>DragGestureRecognizer</code> 内部维护的事件数组中。
     * <P>
     * @param awtie 要添加到此 <code>DragGestureRecognizer</code> 内部事件数组中的 <code>InputEvent</code>。
     * 注意，<code>null</code> 不是有效值，将被忽略。
     */

    protected synchronized void appendEvent(InputEvent awtie) {
        events.add(awtie);
    }

    /**
     * 序列化此 <code>DragGestureRecognizer</code>。此方法首先执行默认序列化。
     * 然后，如果此对象的 <code>DragGestureListener</code> 可以序列化，则将其写入；否则，写入 <code>null</code>。
     *
     * @serialData 默认的可序列化字段，按字母顺序排列，后跟 <code>DragGestureListener</code> 或 <code>null</code>。
     * @since 1.4
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        s.writeObject(SerializationTester.test(dragGestureListener)
                      ? dragGestureListener : null);
    }

    /**
     * 反序列化此 <code>DragGestureRecognizer</code>。此方法首先对所有非 <code>transient</code> 字段执行默认反序列化。
     * 然后，通过使用流中的下一个对象来反序列化此对象的 <code>DragGestureListener</code>。
     *
     * @since 1.4
     */
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException
    {
        ObjectInputStream.GetField f = s.readFields();

        DragSource newDragSource = (DragSource)f.get("dragSource", null);
        if (newDragSource == null) {
            throw new InvalidObjectException("null DragSource");
        }
        dragSource = newDragSource;

        component = (Component)f.get("component", null);
        sourceActions = f.get("sourceActions", 0) & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK);
        events = (ArrayList<InputEvent>)f.get("events", new ArrayList<>(1));


                    dragGestureListener = (DragGestureListener)s.readObject();
    }

    /*
     * 字段
     */

    /**
     * 与此 <code>DragGestureRecognizer</code> 关联的
     * <code>DragSource</code>。
     *
     * @serial
     */
    protected DragSource          dragSource;

    /**
     * 与此 <code>DragGestureRecognizer</code> 关联的
     * <code>Component</code>。
     *
     * @serial
     */
    protected Component           component;

    /**
     * 与此 <code>DragGestureRecognizer</code> 关联的
     * <code>DragGestureListener</code>。
     */
    protected transient DragGestureListener dragGestureListener;

  /**
   * 一个 <code>int</code>，表示
   * 在此拖放操作中使用的动作类型。
   *
   * @serial
   */
  protected int  sourceActions;

   /**
    * 一个事件列表（按顺序），这些事件
    * 被 <code>DragGestureRecognizer</code>
    * 识别为触发拖动的“手势”。
    *
    * @serial
    */
   protected ArrayList<InputEvent> events = new ArrayList<InputEvent>(1);
}
