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

import java.awt.Component;
import java.awt.Cursor;

import java.awt.Image;
import java.awt.Point;

import java.awt.event.InputEvent;

import java.awt.datatransfer.Transferable;

import java.io.InvalidObjectException;
import java.util.EventObject;

import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * <code>DragGestureEvent</code> 传递给 <code>DragGestureListener</code> 的
 * dragGestureRecognized() 方法，当特定的 <code>DragGestureRecognizer</code>
 * 检测到在它跟踪的 <code>Component</code> 上发生了平台依赖的拖动初始化手势时。
 *
 * 任何 <code>DragGestureEvent</code> 实例的 {@code action} 字段应取以下值之一：
 * <ul>
 * <li> {@code DnDConstants.ACTION_COPY}
 * <li> {@code DnDConstants.ACTION_MOVE}
 * <li> {@code DnDConstants.ACTION_LINK}
 * </ul>
 * 分配不同于上述值的值将导致未指定的行为。
 *
 * @see java.awt.dnd.DragGestureRecognizer
 * @see java.awt.dnd.DragGestureListener
 * @see java.awt.dnd.DragSource
 * @see java.awt.dnd.DnDConstants
 */

public class DragGestureEvent extends EventObject {

    private static final long serialVersionUID = 9080172649166731306L;

    /**
     * 构造一个 <code>DragGestureEvent</code> 对象，由触发此事件的
     * <code>DragGestureRecognizer</code> 实例、表示用户首选操作的 {@code act} 参数、
     * 表示拖动起源的 {@code ori} 参数以及组成手势的事件列表（{@code evs} 参数）给出。
     * <P>
     * @param dgr 触发此事件的 <code>DragGestureRecognizer</code>
     * @param act 用户的首选操作。
     *            有关允许值的信息，请参见 {@link DragGestureEvent} 的类描述
     * @param ori 拖动的起源
     * @param evs 组成手势的事件列表
     * <P>
     * @throws IllegalArgumentException 如果任何参数等于 {@code null}
     * @throws IllegalArgumentException 如果 act 参数不符合 {@link DragGestureEvent}
     *                                  类描述中给出的值
     * @see java.awt.dnd.DnDConstants
     */

    public DragGestureEvent(DragGestureRecognizer dgr, int act, Point ori,
                            List<? extends InputEvent> evs)
    {
        super(dgr);

        if ((component = dgr.getComponent()) == null)
            throw new IllegalArgumentException("null component");
        if ((dragSource = dgr.getDragSource()) == null)
            throw new IllegalArgumentException("null DragSource");

        if (evs == null || evs.isEmpty())
            throw new IllegalArgumentException("null or empty list of events");

        if (act != DnDConstants.ACTION_COPY &&
            act != DnDConstants.ACTION_MOVE &&
            act != DnDConstants.ACTION_LINK)
            throw new IllegalArgumentException("bad action");

        if (ori == null) throw new IllegalArgumentException("null origin");

        events     = evs;
        action     = act;
        origin     = ori;
    }

    /**
     * 返回作为 <code>DragGestureRecognizer</code> 的源。
     * <P>
     * @return 作为 <code>DragGestureRecognizer</code> 的源
     */

    public DragGestureRecognizer getSourceAsDragGestureRecognizer() {
        return (DragGestureRecognizer)getSource();
    }

    /**
     * 返回与此 <code>DragGestureEvent</code> 关联的 <code>Component</code>。
     * <P>
     * @return Component
     */

    public Component getComponent() { return component; }

    /**
     * 返回 <code>DragSource</code>。
     * <P>
     * @return <code>DragSource</code>
     */

    public DragSource getDragSource() { return dragSource; }

    /**
     * 返回一个 <code>Point</code>，表示拖动起源的 <code>Component</code> 的坐标。
     * <P>
     * @return 拖动起源的 Component 坐标中的 Point。
     */

    public Point getDragOrigin() {
        return origin;
    }

    /**
     * 返回一个 <code>Iterator</code>，用于组成手势的事件。
     * <P>
     * @return 用于组成手势的事件的 Iterator
     */
    @SuppressWarnings("unchecked")
    public Iterator<InputEvent> iterator() { return events.iterator(); }

    /**
     * 返回一个 <code>Object</code> 数组，包含组成拖动手势的事件。
     * <P>
     * @return 组成手势的事件数组
     */

    public Object[] toArray() { return events.toArray(); }

    /**
     * 返回一个 <code>EventObject</code> 子类型数组，包含组成拖动手势的事件。
     * <P>
     * @param array <code>EventObject</code> 子类型的数组
     * <P>
     * @return 组成手势的事件数组
     */
    @SuppressWarnings("unchecked")
    public Object[] toArray(Object[] array) { return events.toArray(array); }

    /**
     * 返回一个 <code>int</code>，表示用户选择的操作。
     * <P>
     * @return 用户选择的操作
     */

    public int getDragAction() { return action; }

    /**
     * 返回触发手势的初始事件。
     * <P>
     * @return 手势序列中的第一个“触发”事件
     */

    public InputEvent getTriggerEvent() {
        return getSourceAsDragGestureRecognizer().getTriggerEvent();
    }

    /**
     * 给定此拖动操作的 <code>Cursor</code> 和表示此拖动操作的源数据的 <code>Transferable</code>，
     * 开始拖动操作。
     * <br>
     * 如果指定了 <code>null</code> <code>Cursor</code>，则不会抛出异常，而是使用默认的拖动光标。
     * <br>
     * 如果指定了 <code>null</code> <code>Transferable</code>，则会抛出 <code>NullPointerException</code>。
     * @param dragCursor     此拖动操作的初始 {@code Cursor}，或 {@code null} 以使用默认光标处理；
     *                       有关拖放过程中光标处理机制的更多详细信息，请参见
     *                       <a href="DragSourceContext.html#defaultCursor">DragSourceContext</a>
     * @param transferable 表示此拖动操作的源数据的 <code>Transferable</code>。
     *
     * @throws InvalidDnDOperationException 如果拖放系统无法启动拖动操作，或者用户尝试在现有拖动操作仍在执行时启动拖动操作。
     * @throws NullPointerException 如果 {@code Transferable} 为 {@code null}
     * @since 1.4
     */
    public void startDrag(Cursor dragCursor, Transferable transferable)
      throws InvalidDnDOperationException {
        dragSource.startDrag(this, dragCursor, transferable, null);
    }

    /**
     * 给定要显示的初始 <code>Cursor</code>、<code>Transferable</code> 对象和要使用的 <code>DragSourceListener</code>，
     * 开始拖动。
     * <P>
     * @param dragCursor     此拖动操作的初始 {@code Cursor}，或 {@code null} 以使用默认光标处理；
     *                       有关拖放过程中光标处理机制的更多详细信息，请参见
     *                       <a href="DragSourceContext.html#defaultCursor">DragSourceContext</a>
     * @param transferable 源的 Transferable
     * @param dsl          源的 DragSourceListener
     * <P>
     * @throws InvalidDnDOperationException 如果
     * 拖放系统无法启动拖动操作，或者用户尝试在现有拖动操作仍在执行时启动拖动操作。
     */

    public void startDrag(Cursor dragCursor, Transferable transferable, DragSourceListener dsl) throws InvalidDnDOperationException {
        dragSource.startDrag(this, dragCursor, transferable, dsl);
    }

    /**
     * 给定要显示的初始 <code>Cursor</code>、拖动 <code>Image</code>、<code>Image</code> 的偏移量、
     * <code>Transferable</code> 对象和要使用的 <code>DragSourceListener</code>，开始拖动。
     * <P>
     * @param dragCursor     此拖动操作的初始 {@code Cursor}，或 {@code null} 以使用默认光标处理；
     *                       有关拖放过程中光标处理机制的更多详细信息，请参见
     *                       <a href="DragSourceContext.html#defaultCursor">DragSourceContext</a>
     * @param dragImage    源的拖动图像
     * @param imageOffset  拖动图像的偏移量
     * @param transferable 源的 Transferable
     * @param dsl          源的 DragSourceListener
     * <P>
     * @throws InvalidDnDOperationException 如果
     * 拖放系统无法启动拖动操作，或者用户尝试在现有拖动操作仍在执行时启动拖动操作。
     */

    public void startDrag(Cursor dragCursor, Image dragImage, Point imageOffset, Transferable transferable, DragSourceListener dsl) throws InvalidDnDOperationException {
        dragSource.startDrag(this,  dragCursor, dragImage, imageOffset, transferable, dsl);
    }

    /**
     * 序列化此 <code>DragGestureEvent</code>。执行默认序列化，然后仅当 <code>List</code> 可以序列化时，
     * 写出此对象的 <code>List</code> 手势事件。如果不能序列化，则写入 <code>null</code>。
     * 在这种情况下，从结果反序列化流创建的 <code>DragGestureEvent</code> 将包含一个空的 <code>List</code> 手势事件。
     *
     * @serialData 按字母顺序排列的默认可序列化字段，后跟 <code>List</code> 实例或 <code>null</code>。
     * @since 1.4
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        s.writeObject(SerializationTester.test(events) ? events : null);
    }

    /**
     * 反序列化此 <code>DragGestureEvent</code>。此方法首先对所有非 <code>transient</code> 字段执行默认反序列化。
     * 然后尝试反序列化此对象的 <code>List</code> 手势事件。首先尝试通过反序列化 <code>events</code> 字段来实现，
     * 因为在 1.4 之前的版本中，一个名为 <code>events</code> 的非 <code>transient</code> 字段存储了手势事件的 <code>List</code>。
     * 如果失败，则使用流中的下一个对象。如果结果 <code>List</code> 为 <code>null</code>，则将此对象的手势事件 <code>List</code>
     * 设置为一个空的 <code>List</code>。
     *
     * @since 1.4
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException
    {
        ObjectInputStream.GetField f = s.readFields();

        DragSource newDragSource = (DragSource)f.get("dragSource", null);
        if (newDragSource == null) {
            throw new InvalidObjectException("null DragSource");
        }
        dragSource = newDragSource;

        Component newComponent = (Component)f.get("component", null);
        if (newComponent == null) {
            throw new InvalidObjectException("null component");
        }
        component = newComponent;

        Point newOrigin = (Point)f.get("origin", null);
        if (newOrigin == null) {
            throw new InvalidObjectException("null origin");
        }
        origin = newOrigin;

        int newAction = f.get("action", 0);
        if (newAction != DnDConstants.ACTION_COPY &&
                newAction != DnDConstants.ACTION_MOVE &&
                newAction != DnDConstants.ACTION_LINK) {
            throw new InvalidObjectException("bad action");
        }
        action = newAction;

        // Pre-1.4 support. 'events' was previously non-transient
        List newEvents;
        try {
            newEvents = (List)f.get("events", null);
        } catch (IllegalArgumentException e) {
            // 1.4-compatible byte stream. 'events' was written explicitly
            newEvents = (List)s.readObject();
        }

        // Implementation assumes 'events' is never null.
        if (newEvents != null && newEvents.isEmpty()) {
            // Constructor treats empty events list as invalid value
            // Throw exception if serialized list is empty
            throw new InvalidObjectException("empty list of events");
        } else if (newEvents == null) {
            newEvents = Collections.emptyList();
        }
        events = newEvents;
    }

    /*
     * fields
     */
    @SuppressWarnings("rawtypes")
    private transient List events;

    /**
     * 与此 DragGestureEvent 关联的 DragSource。
     *
     * @serial
     */
    private DragSource dragSource;

    /**
     * 与此 DragGestureEvent 关联的 Component。
     *
     * @serial
     */
    private Component  component;

    /**
     * 拖动的起源。
     *
     * @serial
     */
    private Point      origin;

    /**
     * 用户的首选操作。
     *
     * @serial
     */
    private int        action;
}
