
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.dnd.peer.DragSourceContextPeer;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.util.TooManyListenersException;

/**
 * <code>DragSourceContext</code> 类负责管理拖放协议的发起方。特别是，它负责管理
 * {@linkplain DragSourceListener DragSourceListeners}
 * 和 {@linkplain DragSourceMotionListener DragSourceMotionListeners} 的拖放事件通知，
 * 以及提供代表拖放操作源数据的 {@link Transferable}。
 * <p>
 * 注意，<code>DragSourceContext</code> 本身
 * 实现了 <code>DragSourceListener</code> 和
 * <code>DragSourceMotionListener</code> 接口。
 * 这是为了允许平台对等体
 * （{@link DragSourceContextPeer} 实例）
 * 由 {@link DragSource} 创建，通知
 * <code>DragSourceContext</code> 操作中的状态变化。这允许
 * <code>DragSourceContext</code> 对象介于平台和
 * 拖放操作发起方提供的监听器之间。
 * <p>
 * <a name="defaultCursor"></a>
 * 默认情况下，{@code DragSourceContext} 会根据拖放操作的当前状态设置合适的光标。例如，如果
 * 用户选择了 {@linkplain DnDConstants#ACTION_MOVE 移动操作}，
 * 并且指针位于接受移动操作的目标上，则显示默认的移动光标。当
 * 指针位于不接受传输的区域时，显示默认的“无法放置”光标。
 * <p>
 * 当通过 {@link #setCursor} 方法设置自定义光标时，此默认处理机制将被禁用。
 * 当默认处理被禁用时，
 * 开发者有责任通过监听 {@code DragSource} 事件并调用 {@code setCursor()} 方法来保持光标的更新。
 * 或者，可以通过提供
 * {@code DragSource} 和 {@code DragSourceContext} 类的自定义实现来提供自定义光标行为。
 *
 * @see DragSourceListener
 * @see DragSourceMotionListener
 * @see DnDConstants
 * @since 1.2
 */

public class DragSourceContext
    implements DragSourceListener, DragSourceMotionListener, Serializable {

    private static final long serialVersionUID = -115407898692194719L;

    // 用于 updateCurrentCursor

    /**
     * 一个 <code>int</code>，用于 updateCurrentCursor()
     * 表示光标应更改为默认（无法放置）光标。
     */
    protected static final int DEFAULT = 0;

    /**
     * 一个 <code>int</code>，用于 updateCurrentCursor()
     * 表示光标已进入 <code>DropTarget</code>。
     */
    protected static final int ENTER   = 1;

    /**
     * 一个 <code>int</code>，用于 updateCurrentCursor()
     * 表示光标位于 <code>DropTarget</code> 上。
     */
    protected static final int OVER    = 2;

    /**
     * 一个 <code>int</code>，用于 updateCurrentCursor()
     * 表示用户操作已更改。
     */

    protected static final int CHANGED = 3;

    /**
     * 由 <code>DragSource</code> 调用，此构造函数根据
     * 此拖放操作的 <code>DragSourceContextPeer</code>、
     * 触发拖放的 <code>DragGestureEvent</code>、
     * 拖放操作的初始 <code>Cursor</code>、
     * 拖放过程中显示的（可选）<code>Image</code>、
     * 触发事件时热点与图像原点的偏移量、
     * 拖放操作的主题数据 <code>Transferable</code> 以及
     * 拖放操作期间使用的 <code>DragSourceListener</code> 创建一个新的
     * <code>DragSourceContext</code>。
     * <br>
     * 如果 <code>DragSourceContextPeer</code> 为 <code>null</code>，则抛出 <code>NullPointerException</code>。
     * <br>
     * 如果 <code>DragGestureEvent</code> 为 <code>null</code>，则抛出 <code>NullPointerException</code>。
     * <br>
     * 如果 <code>Cursor</code> 为 <code>null</code>，则不抛出异常，并激活此拖放操作的默认拖放光标行为。
     * <br>
     * 如果 <code>Image</code> 为 <code>null</code>，则不抛出异常。
     * <br>
     * 如果 <code>Image</code> 不为 <code>null</code> 且偏移量为 <code>null</code>，则抛出 <code>NullPointerException</code>。
     * <br>
     * 如果 <code>Transferable</code> 为 <code>null</code>，则抛出 <code>NullPointerException</code>。
     * <br>
     * 如果 <code>DragSourceListener</code> 为 <code>null</code>，则不抛出异常。
     *
     * @param dscp       此拖放操作的 <code>DragSourceContextPeer</code>
     * @param trigger    触发事件
     * @param dragCursor     此拖放操作的初始 {@code Cursor} 或 {@code null} 以启用默认光标处理；
     *                       有关拖放期间光标处理机制的更多详细信息，请参阅 <a href="DragSourceContext.html#defaultCursor">类级别文档</a>
     * @param dragImage  要拖动的 <code>Image</code>（或 <code>null</code>）
     * @param offset     触发事件时热点与图像原点的偏移量
     * @param t          <code>Transferable</code>
     * @param dsl        <code>DragSourceListener</code>
     *
     * @throws IllegalArgumentException 如果触发事件关联的 <code>Component</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果触发事件的 <code>DragSource</code> 为 <code>null</code>。
     * @throws IllegalArgumentException 如果触发事件的拖放操作为 <code>DnDConstants.ACTION_NONE</code>。
     * @throws IllegalArgumentException 如果与触发事件关联的 <code>DragGestureRecognizer</code> 的源操作等于 <code>DnDConstants.ACTION_NONE</code>。
     * @throws NullPointerException 如果 dscp、trigger 或 t 为 null，或
     *         如果 dragImage 不为 null 且 offset 为 null
     */
    public DragSourceContext(DragSourceContextPeer dscp,
                             DragGestureEvent trigger, Cursor dragCursor,
                             Image dragImage, Point offset, Transferable t,
                             DragSourceListener dsl) {
        if (dscp == null) {
            throw new NullPointerException("DragSourceContextPeer");
        }

        if (trigger == null) {
            throw new NullPointerException("Trigger");
        }

        if (trigger.getDragSource() == null) {
            throw new IllegalArgumentException("DragSource");
        }

        if (trigger.getComponent() == null) {
            throw new IllegalArgumentException("Component");
        }

        if (trigger.getSourceAsDragGestureRecognizer().getSourceActions() ==
                 DnDConstants.ACTION_NONE) {
            throw new IllegalArgumentException("source actions");
        }

        if (trigger.getDragAction() == DnDConstants.ACTION_NONE) {
            throw new IllegalArgumentException("no drag action");
        }

        if (t == null) {
            throw new NullPointerException("Transferable");
        }

        if (dragImage != null && offset == null) {
            throw new NullPointerException("offset");
        }

        peer         = dscp;
        this.trigger = trigger;
        cursor       = dragCursor;
        transferable = t;
        listener     = dsl;
        sourceActions =
            trigger.getSourceAsDragGestureRecognizer().getSourceActions();

        useCustomCursor = (dragCursor != null);

        updateCurrentCursor(trigger.getDragAction(), getSourceActions(), DEFAULT);
    }

    /**
     * 返回实例化此 <code>DragSourceContext</code> 的 <code>DragSource</code>。
     *
     * @return 实例化此 <code>DragSourceContext</code> 的 <code>DragSource</code>
     */

    public DragSource   getDragSource() { return trigger.getDragSource(); }

    /**
     * 返回与此 <code>DragSourceContext</code> 关联的 <code>Component</code>。
     *
     * @return 开始拖放操作的 <code>Component</code>
     */

    public Component    getComponent() { return trigger.getComponent(); }

    /**
     * 返回最初触发拖放操作的 <code>DragGestureEvent</code>。
     *
     * @return 触发拖放操作的事件
     */

    public DragGestureEvent getTrigger() { return trigger; }

    /**
     * 返回一个 <code>DnDConstants</code> 的位掩码，表示
     * 与此 <code>DragSourceContext</code> 关联的拖放操作的源支持的放置操作集。
     *
     * @return 拖放源支持的放置操作
     */
    public int  getSourceActions() {
        return sourceActions;
    }

    /**
     * 将此拖放操作的光标设置为指定的 <code>Cursor</code>。如果指定的 <code>Cursor</code>
     * 为 <code>null</code>，则为此次拖放操作激活默认拖放光标行为，否则禁用默认行为。
     *
     * @param c     此拖放操作的初始 {@code Cursor}，
     *                       或 {@code null} 以启用默认光标处理；
     *                       有关拖放期间光标处理的更多详细信息，请参阅 {@linkplain Cursor 类级别文档}
     *
     */

    public synchronized void setCursor(Cursor c) {
        useCustomCursor = (c != null);
        setCursorImpl(c);
    }

    /**
     * 返回当前拖放的 <code>Cursor</code>。
     * <P>
     * @return 当前拖放的 <code>Cursor</code>
     */

    public Cursor getCursor() { return cursor; }

    /**
     * 如果尚未添加 <code>DragSourceListener</code>，则将其添加到此
     * <code>DragSourceContext</code>。如果已添加 <code>DragSourceListener</code>，
     * 则此方法抛出 <code>TooManyListenersException</code>。
     * <P>
     * @param dsl 要添加的 <code>DragSourceListener</code>。
     * 注意，虽然 <code>null</code> 不被禁止，
     * 但作为参数是不可接受的。
     * <P>
     * @throws TooManyListenersException 如果
     * 已添加 <code>DragSourceListener</code>
     */

    public synchronized void addDragSourceListener(DragSourceListener dsl) throws TooManyListenersException {
        if (dsl == null) return;

        if (equals(dsl)) throw new IllegalArgumentException("DragSourceContext may not be its own listener");

        if (listener != null)
            throw new TooManyListenersException();
        else
            listener = dsl;
    }

    /**
     * 从此 <code>DragSourceContext</code> 中移除指定的 <code>DragSourceListener</code>。
     *
     * @param dsl 要移除的 <code>DragSourceListener</code>；
     *     注意，虽然 <code>null</code> 不被禁止，
     *     但作为参数是不可接受的
     */

    public synchronized void removeDragSourceListener(DragSourceListener dsl) {
        if (listener != null && listener.equals(dsl)) {
            listener = null;
        } else
            throw new IllegalArgumentException();
    }

    /**
     * 通知对等体 <code>Transferable</code> 的 <code>DataFlavor</code> 已更改。
     */

    public void transferablesFlavorsChanged() {
        if (peer != null) peer.transferablesFlavorsChanged();
    }

    /**
     * 调用注册到此 <code>DragSourceContext</code> 和关联的
     * <code>DragSource</code> 的 <code>DragSourceListener</code> 的 <code>dragEnter</code> 方法，
     * 并将指定的 <code>DragSourceDragEvent</code> 传递给它们。
     *
     * @param dsde 指定的 <code>DragSourceDragEvent</code>
     */
    public void dragEnter(DragSourceDragEvent dsde) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dragEnter(dsde);
        }
        getDragSource().processDragEnter(dsde);

        updateCurrentCursor(getSourceActions(), dsde.getTargetActions(), ENTER);
    }

    /**
     * 调用注册到此 <code>DragSourceContext</code> 和关联的
     * <code>DragSource</code> 的 <code>DragSourceListener</code> 的 <code>dragOver</code> 方法，
     * 并将指定的 <code>DragSourceDragEvent</code> 传递给它们。
     *
     * @param dsde 指定的 <code>DragSourceDragEvent</code>
     */
    public void dragOver(DragSourceDragEvent dsde) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dragOver(dsde);
        }
        getDragSource().processDragOver(dsde);

        updateCurrentCursor(getSourceActions(), dsde.getTargetActions(), OVER);
    }

    /**
     * 调用注册到此 <code>DragSourceContext</code> 和关联的
     * <code>DragSource</code> 的 <code>DragSourceListener</code> 的 <code>dragExit</code> 方法，
     * 并将指定的 <code>DragSourceEvent</code> 传递给它们。
     *
     * @param dse 指定的 <code>DragSourceEvent</code>
     */
    public void dragExit(DragSourceEvent dse) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dragExit(dse);
        }
        getDragSource().processDragExit(dse);


                    updateCurrentCursor(DnDConstants.ACTION_NONE, DnDConstants.ACTION_NONE, DEFAULT);
    }

    /**
     * 调用与此 <code>DragSourceContext</code> 和关联的 <code>DragSource</code>
     * 注册的 <code>DragSourceListener</code> 的 <code>dropActionChanged</code> 方法，
     * 并将指定的 <code>DragSourceDragEvent</code> 传递给它们。
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dropActionChanged(DragSourceDragEvent dsde) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dropActionChanged(dsde);
        }
        getDragSource().processDropActionChanged(dsde);

        updateCurrentCursor(getSourceActions(), dsde.getTargetActions(), CHANGED);
    }

    /**
     * 调用与此 <code>DragSourceContext</code> 和关联的 <code>DragSource</code>
     * 注册的 <code>DragSourceListener</code> 的 <code>dragDropEnd</code> 方法，
     * 并将指定的 <code>DragSourceDropEvent</code> 传递给它们。
     *
     * @param dsde the <code>DragSourceDropEvent</code>
     */
    public void dragDropEnd(DragSourceDropEvent dsde) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dragDropEnd(dsde);
        }
        getDragSource().processDragDropEnd(dsde);
    }

    /**
     * 调用与此 <code>DragSourceContext</code> 关联的 <code>DragSource</code>
     * 注册的 <code>DragSourceMotionListener</code> 的 <code>dragMouseMoved</code> 方法，
     * 并将指定的 <code>DragSourceDragEvent</code> 传递给它们。
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     * @since 1.4
     */
    public void dragMouseMoved(DragSourceDragEvent dsde) {
        getDragSource().processDragMouseMoved(dsde);
    }

    /**
     * 返回与此 <code>DragSourceContext</code> 关联的 <code>Transferable</code>。
     *
     * @return the <code>Transferable</code>
     */
    public Transferable getTransferable() { return transferable; }

    /**
     * 如果默认的拖动光标行为处于活动状态，此方法将为拖动源支持的操作、
     * 放置目标操作和状态设置默认的拖动光标，否则此方法不执行任何操作。
     *
     * @param sourceAct 拖动源支持的操作
     * @param targetAct 放置目标操作
     * @param status 以下字段之一： <code>DEFAULT</code>、
     *               <code>ENTER</code>、 <code>OVER</code>、
     *               <code>CHANGED</code>
     */

    protected synchronized void updateCurrentCursor(int sourceAct, int targetAct, int status) {

        // 如果光标已先前设置，则不执行任何默认处理。

        if (useCustomCursor) {
            return;
        }

        // 执行默认处理

        Cursor c = null;

        switch (status) {
            default:
                targetAct = DnDConstants.ACTION_NONE;
            case ENTER:
            case OVER:
            case CHANGED:
                int    ra = sourceAct & targetAct;

                if (ra == DnDConstants.ACTION_NONE) { // 无法放置
                    if ((sourceAct & DnDConstants.ACTION_LINK) == DnDConstants.ACTION_LINK)
                        c = DragSource.DefaultLinkNoDrop;
                    else if ((sourceAct & DnDConstants.ACTION_MOVE) == DnDConstants.ACTION_MOVE)
                        c = DragSource.DefaultMoveNoDrop;
                    else
                        c = DragSource.DefaultCopyNoDrop;
                } else { // 可以放置
                    if ((ra & DnDConstants.ACTION_LINK) == DnDConstants.ACTION_LINK)
                        c = DragSource.DefaultLinkDrop;
                    else if ((ra & DnDConstants.ACTION_MOVE) == DnDConstants.ACTION_MOVE)
                        c = DragSource.DefaultMoveDrop;
                    else
                        c = DragSource.DefaultCopyDrop;
                }
        }

        setCursorImpl(c);
    }

    private void setCursorImpl(Cursor c) {
        if (cursor == null || !cursor.equals(c)) {
            cursor = c;
            if (peer != null) peer.setCursor(cursor);
        }
    }

    /**
     * 序列化此 <code>DragSourceContext</code>。此方法首先执行默认序列化。
     * 接下来，如果此对象的 <code>Transferable</code> 可以被序列化，则将其写入，
     * 否则写入 <code>null</code>。在这种情况下，从结果反序列化流创建的
     * <code>DragSourceContext</code> 将包含一个不支持任何 <code>DataFlavor</code> 的
     * 哑 <code>Transferable</code>。最后，如果此对象的 <code>DragSourceListener</code>
     * 可以被序列化，则将其写入，否则写入 <code>null</code>。
     *
     * @serialData 默认的可序列化字段，按字母顺序排列，后跟一个 <code>Transferable</code> 实例或
     *             <code>null</code>，再后跟一个 <code>DragSourceListener</code> 实例或
     *             <code>null</code>。
     * @since 1.4
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        s.writeObject(SerializationTester.test(transferable)
                      ? transferable : null);
        s.writeObject(SerializationTester.test(listener)
                      ? listener : null);
    }

    /**
     * 反序列化此 <code>DragSourceContext</code>。此方法首先对所有非 <code>transient</code>
     * 字段执行默认反序列化。然后通过使用流中的下一个两个对象来反序列化此对象的
     * <code>Transferable</code> 和 <code>DragSourceListener</code>。如果结果
     * <code>Transferable</code> 为 <code>null</code>，则将此对象的 <code>Transferable</code>
     * 设置为一个不支持任何 <code>DataFlavor</code> 的哑 <code>Transferable</code>。
     *
     * @since 1.4
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException
    {
        ObjectInputStream.GetField f = s.readFields();

        DragGestureEvent newTrigger = (DragGestureEvent)f.get("trigger", null);
        if (newTrigger == null) {
            throw new InvalidObjectException("Null trigger");
        }
        if (newTrigger.getDragSource() == null) {
            throw new InvalidObjectException("Null DragSource");
        }
        if (newTrigger.getComponent() == null) {
            throw new InvalidObjectException("Null trigger component");
        }

        int newSourceActions = f.get("sourceActions", 0)
                & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK);
        if (newSourceActions == DnDConstants.ACTION_NONE) {
            throw new InvalidObjectException("Invalid source actions");
        }
        int triggerActions = newTrigger.getDragAction();
        if (triggerActions != DnDConstants.ACTION_COPY &&
                triggerActions != DnDConstants.ACTION_MOVE &&
                triggerActions != DnDConstants.ACTION_LINK) {
            throw new InvalidObjectException("No drag action");
        }
        trigger = newTrigger;

        cursor = (Cursor)f.get("cursor", null);
        useCustomCursor = f.get("useCustomCursor", false);
        sourceActions = newSourceActions;

        transferable = (Transferable)s.readObject();
        listener = (DragSourceListener)s.readObject();

        // 实现假设 'transferable' 从不为 null。
        if (transferable == null) {
            if (emptyTransferable == null) {
                emptyTransferable = new Transferable() {
                        public DataFlavor[] getTransferDataFlavors() {
                            return new DataFlavor[0];
                        }
                        public boolean isDataFlavorSupported(DataFlavor flavor)
                        {
                            return false;
                        }
                        public Object getTransferData(DataFlavor flavor)
                            throws UnsupportedFlavorException
                        {
                            throw new UnsupportedFlavorException(flavor);
                        }
                    };
            }
            transferable = emptyTransferable;
        }
    }

    private static Transferable emptyTransferable;

    /*
     * fields
     */

    private transient DragSourceContextPeer peer;

    /**
     * 触发拖动开始的事件。
     *
     * @serial
     */
    private DragGestureEvent    trigger;

    /**
     * 当前的拖动光标。
     *
     * @serial
     */
    private Cursor              cursor;

    private transient Transferable      transferable;

    private transient DragSourceListener    listener;

    /**
     * 如果使用自定义拖动光标而不是默认光标，则为 <code>true</code>。
     *
     * @serial
     */
    private boolean useCustomCursor;

    /**
     * 一个 <code>DnDConstants</code> 的位掩码，表示与此 <code>DragSourceContext</code>
     * 关联的拖动操作支持的放置操作集。
     *
     * @serial
     */
    private int sourceActions;
}
