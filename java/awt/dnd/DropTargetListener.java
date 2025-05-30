/*
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
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

import java.util.EventListener;

import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 * <code>DropTargetListener</code> 接口
 * 是由 <code>DropTarget</code> 类使用的回调接口，用于提供
 * 涉及到该 <code>DropTarget</code> 的 DnD 操作的通知。可以实现该接口的方法以在整个
 * 拖放操作过程中为用户提供“拖动下方”的视觉反馈。
 * <p>
 * 通过实现该接口创建一个监听器对象，然后将其注册到 <code>DropTarget</code>。当拖动进入、移动到或离开
 * 该 <code>DropTarget</code> 的可操作部分，当拖动操作改变，以及当拖放发生时，监听器对象中的相关方法将被调用，
 * 并将 <code>DropTargetEvent</code> 传递给它。
 * <p>
 * <code>DropTarget</code> 的可操作部分是与之关联的 <code>Component</code> 的几何部分，该部分未被重叠的顶层窗口或
 * Z 顺序中较高的另一个具有活动 <code>DropTarget</code> 的 <code>Component</code> 遮挡。
 * <p>
 * 在拖动过程中，可以通过调用 <code>DropTargetDragEvent</code> 实例上的 <code>getTransferable()</code>
 * 方法来检索与当前拖动操作相关联的数据。
 * <p>
 * 注意，<code>DropTargetDragEvent</code> 实例上的 <code>getTransferable()</code> 方法只能在
 * 相应的监听器方法中调用，并且应在该方法返回之前从返回的 <code>Transferable</code> 中检索所有必要的数据。
 *
 * @since 1.2
 */

public interface DropTargetListener extends EventListener {

    /**
     * 当拖动操作正在进行且鼠标指针进入
     * 该监听器注册的 <code>DropTarget</code> 的可操作部分时调用。
     *
     * @param dtde <code>DropTargetDragEvent</code>
     */

    void dragEnter(DropTargetDragEvent dtde);

    /**
     * 当拖动操作正在进行且鼠标指针仍然位于
     * 该监听器注册的 <code>DropTarget</code> 的可操作部分时调用。
     *
     * @param dtde <code>DropTargetDragEvent</code>
     */

    void dragOver(DropTargetDragEvent dtde);

    /**
     * 如果用户已修改
     * 当前的拖放手势，则调用此方法。
     * <P>
     * @param dtde <code>DropTargetDragEvent</code>
     */

    void dropActionChanged(DropTargetDragEvent dtde);

    /**
     * 当拖动操作正在进行且鼠标指针已离开
     * 该监听器注册的 <code>DropTarget</code> 的可操作部分时调用。
     *
     * @param dte <code>DropTargetEvent</code>
     */

    void dragExit(DropTargetEvent dte);

    /**
     * 当拖动操作以拖放在
     * 该监听器注册的 <code>DropTarget</code> 的可操作部分上终止时调用。
     * <p>
     * 该方法负责执行与手势相关联的数据传输。<code>DropTargetDropEvent</code>
     * 提供了一种获取表示要传输的数据对象的 <code>Transferable</code> 对象的手段。<P>
     * 从该方法中，<code>DropTargetListener</code>
     * 应通过 <code>DropTargetDropEvent</code> 参数的
     * acceptDrop(int dropAction) 或 rejectDrop() 方法接受或拒绝拖放。
     * <P>
     * 在调用 acceptDrop() 之后，但不是之前，
     * 可以调用 <code>DropTargetDropEvent</code> 的 getTransferable()
     * 方法，并可以通过返回的 <code>Transferable</code> 的
     * getTransferData() 方法执行数据传输。
     * <P>
     * 在拖放完成时，该方法的实现必须通过将适当的
     * <code>boolean</code> 传递给 <code>DropTargetDropEvent</code> 的
     * dropComplete(boolean success) 方法来指示拖放的成功/失败。
     * <P>
     * 注意：应在调用 <code>DropTargetDropEvent</code> 的 dropComplete(boolean success) 方法之前完成数据传输。
     * 之后，调用 <code>Transferable</code> 的 getTransferData() 方法（该 <code>Transferable</code> 由
     * <code>DropTargetDropEvent.getTransferable()</code> 返回）仅在数据传输是本地的时才保证成功；即，
     * 仅当 <code>DropTargetDropEvent.isLocalTransfer()</code> 返回 <code>true</code> 时。否则，调用的行为是
     * 实现依赖的。
     * <P>
     * @param dtde <code>DropTargetDropEvent</code>
     */

    void drop(DropTargetDropEvent dtde);
}
