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

import java.awt.Point;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.util.List;

/**
 * <code>DropTargetDragEvent</code> 事件传递给 <code>DropTargetListener</code> 的
 * dragEnter() 和 dragOver() 方法。
 * <p>
 * <code>DropTargetDragEvent</code> 报告 <i>源拖放操作</i> 和 <i>用户拖放操作</i>，
 * 这些反映了当前拖放操作的状态。
 * <p>
 * <i>源拖放操作</i> 是一个 <code>DnDConstants</code> 的位掩码，表示此拖放操作中拖放源支持的拖放操作集。
 * <p>
 * <i>用户拖放操作</i> 取决于拖放源支持的拖放操作和用户选择的拖放操作。用户可以通过在拖放操作期间按修饰键来选择拖放操作：
 * <pre>
 *   Ctrl + Shift -&gt; ACTION_LINK
 *   Ctrl         -&gt; ACTION_COPY
 *   Shift        -&gt; ACTION_MOVE
 * </pre>
 * 如果用户选择了拖放操作，则 <i>用户拖放操作</i> 是 <code>DnDConstants</code> 中表示所选拖放操作的常量，如果此拖放操作不受拖放源支持，则为 <code>DnDConstants.ACTION_NONE</code>。
 * <p>
 * 如果用户没有选择拖放操作，则搜索 <code>DnDConstants</code> 中表示拖放源支持的拖放操作集的常量，首先查找 <code>DnDConstants.ACTION_MOVE</code>，然后查找 <code>DnDConstants.ACTION_COPY</code>，最后查找 <code>DnDConstants.ACTION_LINK</code>，<i>用户拖放操作</i> 是找到的第一个常量。如果没有找到常量，则 <i>用户拖放操作</i> 为 <code>DnDConstants.ACTION_NONE</code>。
 *
 * @since 1.2
 */

public class DropTargetDragEvent extends DropTargetEvent {

    private static final long serialVersionUID = -8422265619058953682L;

    /**
     * 构造一个 <code>DropTargetDragEvent</code>，给定此操作的 <code>DropTargetContext</code>，
     * “拖放” <code>Cursor</code> 热点在 <code>Component</code> 坐标中的位置，
     * 用户拖放操作和源拖放操作。
     * <P>
     * @param dtc        此操作的 DropTargetContext
     * @param cursorLocn “拖放” Cursor 的热点在 Component 坐标中的位置
     * @param dropAction 用户拖放操作
     * @param srcActions 源拖放操作
     *
     * @throws NullPointerException 如果 cursorLocn 为 null
     * @throws IllegalArgumentException 如果 dropAction 不是 <code>DnDConstants</code> 中的一个
     * @throws IllegalArgumentException 如果 srcActions 不是 <code>DnDConstants</code> 的位掩码
     * @throws IllegalArgumentException 如果 dtc 为 <code>null</code>
     */

    public DropTargetDragEvent(DropTargetContext dtc, Point cursorLocn, int dropAction, int srcActions)  {
        super(dtc);

        if (cursorLocn == null) throw new NullPointerException("cursorLocn");

        if (dropAction != DnDConstants.ACTION_NONE &&
            dropAction != DnDConstants.ACTION_COPY &&
            dropAction != DnDConstants.ACTION_MOVE &&
            dropAction != DnDConstants.ACTION_LINK
        ) throw new IllegalArgumentException("dropAction" + dropAction);

        if ((srcActions & ~(DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK)) != 0) throw new IllegalArgumentException("srcActions");

        location        = cursorLocn;
        actions         = srcActions;
        this.dropAction = dropAction;
    }

    /**
     * 此方法返回一个 <code>Point</code>，表示 <code>Cursor</code> 在 <code>Component</code> 坐标中的当前位置。
     * <P>
     * @return 当前光标位置，以 Component 坐标表示
     */

    public Point getLocation() {
        return location;
    }


    /**
     * 此方法从 <code>DropTargetContext</code> 返回当前的 <code>DataFlavor</code>。
     * <P>
     * @return 从 DropTargetContext 获取的当前 DataFlavors
     */

    public DataFlavor[] getCurrentDataFlavors() {
        return getDropTargetContext().getCurrentDataFlavors();
    }

    /**
     * 此方法将当前的 <code>DataFlavor</code> 作为 <code>java.util.List</code> 返回。
     * <P>
     * @return 当前 DataFlavor 的 <code>java.util.List</code>
     */

    public List<DataFlavor> getCurrentDataFlavorsAsList() {
        return getDropTargetContext().getCurrentDataFlavorsAsList();
    }

    /**
     * 此方法返回一个 <code>boolean</code>，表示指定的 <code>DataFlavor</code> 是否受支持。
     * <P>
     * @param df 要测试的 <code>DataFlavor</code>
     * <P>
     * @return 指定的 DataFlavor 是否受支持
     */

    public boolean isDataFlavorSupported(DataFlavor df) {
        return getDropTargetContext().isDataFlavorSupported(df);
    }

    /**
     * 此方法返回源拖放操作。
     *
     * @return 源拖放操作
     */
    public int getSourceActions() { return actions; }

    /**
     * 此方法返回用户拖放操作。
     *
     * @return 用户拖放操作
     */
    public int getDropAction() { return dropAction; }

    /**
     * 此方法返回表示当前拖放操作关联的数据的 Transferable 对象。
     *
     * @return 与拖放操作关联的 Transferable
     * @throws InvalidDnDOperationException 如果拖放操作关联的数据不可用
     *
     * @since 1.5
     */
    public Transferable getTransferable() {
        return getDropTargetContext().getTransferable();
    }

    /**
     * 接受拖放。
     *
     * 如果实现希望接受 srcActions 中除用户选择的 <code>dropAction</code> 之外的操作，
     * 则应在 <code>DropTargetListeners</code> 的 <code>dragEnter</code>、
     * <code>dragOver</code> 和 <code>dropActionChanged</code> 方法中调用此方法。
     *
     * @param dragOperation 目标接受的操作
     */
    public void acceptDrag(int dragOperation) {
        getDropTargetContext().acceptDrag(dragOperation);
    }

    /**
     * 拒绝拖放，这是由于检查 <code>dropAction</code> 或可用的 <code>DataFlavor</code> 类型的结果。
     */
    public void rejectDrag() {
        getDropTargetContext().rejectDrag();
    }

    /*
     * 字段
     */

    /**
     * 拖放光标的热点在 Component 坐标中的位置。
     *
     * @serial
     */
    private Point               location;

    /**
     * 源拖放操作。
     *
     * @serial
     */
    private int                 actions;

    /**
     * 用户拖放操作。
     *
     * @serial
     */
    private int                 dropAction;
}
