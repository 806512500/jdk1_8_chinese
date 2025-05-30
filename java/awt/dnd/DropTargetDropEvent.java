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
 * <code>DropTargetDropEvent</code> 通过 <code>DropTargetListener</code> 的 drop() 方法传递。
 * <p>
 * <code>DropTargetDropEvent</code> 报告 <i>源拖放操作</i> 和 <i>用户拖放操作</i>，反映了当前拖放操作的状态。
 * <p>
 * <i>源拖放操作</i> 是 <code>DnDConstants</code> 的位掩码，表示此拖放操作中拖放源支持的拖放操作集。
 * <p>
 * <i>用户拖放操作</i> 取决于拖放源支持的拖放操作和用户选择的拖放操作。用户可以通过在拖放操作期间按修饰键来选择拖放操作：
 * <pre>
 *   Ctrl + Shift -&gt; ACTION_LINK
 *   Ctrl         -&gt; ACTION_COPY
 *   Shift        -&gt; ACTION_MOVE
 * </pre>
 * 如果用户选择了拖放操作，则 <i>用户拖放操作</i> 是 <code>DnDConstants</code> 中表示所选拖放操作的常量，如果此拖放操作不受拖放源支持，则为 <code>DnDConstants.ACTION_NONE</code>。
 * <p>
 * 如果用户没有选择拖放操作，则搜索 <code>DnDConstants</code> 中表示拖放源支持的拖放操作集的常量，按以下顺序查找：先查找 <code>DnDConstants.ACTION_MOVE</code>，然后查找 <code>DnDConstants.ACTION_COPY</code>，最后查找 <code>DnDConstants.ACTION_LINK</code>，<i>用户拖放操作</i> 是找到的第一个常量。如果没有找到常量，则 <i>用户拖放操作</i> 为 <code>DnDConstants.ACTION_NONE</code>。
 *
 * @since 1.2
 */

public class DropTargetDropEvent extends DropTargetEvent {

    private static final long serialVersionUID = -1721911170440459322L;

    /**
     * 构造一个 <code>DropTargetDropEvent</code>，给定此操作的 <code>DropTargetContext</code>、
     * 拖放 <code>Cursor</code> 的热点在 <code>Component</code> 坐标中的位置、
     * 当前选择的用户拖放操作和源支持的当前操作集。
     * 默认情况下，此构造函数假设目标不在与源相同的虚拟机中；即，{@link #isLocalTransfer()} 将返回 <code>false</code>。
     * <P>
     * @param dtc        此操作的 <code>DropTargetContext</code>
     * @param cursorLocn 拖放光标的热点在 <code>Component</code> 坐标中的位置
     * @param dropAction 用户拖放操作。
     * @param srcActions 源拖放操作。
     *
     * @throws NullPointerException
     * 如果 cursorLocn 为 <code>null</code>
     * @throws IllegalArgumentException
     *         如果 dropAction 不是 <code>DnDConstants</code> 中的一个。
     * @throws IllegalArgumentException
     *         如果 srcActions 不是 <code>DnDConstants</code> 的位掩码。
     * @throws IllegalArgumentException 如果 dtc 为 <code>null</code>。
     */

    public DropTargetDropEvent(DropTargetContext dtc, Point cursorLocn, int dropAction, int srcActions)  {
        super(dtc);

        if (cursorLocn == null) throw new NullPointerException("cursorLocn");

        if (dropAction != DnDConstants.ACTION_NONE &&
            dropAction != DnDConstants.ACTION_COPY &&
            dropAction != DnDConstants.ACTION_MOVE &&
            dropAction != DnDConstants.ACTION_LINK
        ) throw new IllegalArgumentException("dropAction = " + dropAction);

        if ((srcActions & ~(DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK)) != 0) throw new IllegalArgumentException("srcActions");

        location        = cursorLocn;
        actions         = srcActions;
        this.dropAction = dropAction;
    }

    /**
     * 构造一个 <code>DropTargetEvent</code>，给定此操作的 <code>DropTargetContext</code>、
     * 拖放 <code>Cursor</code> 的热点在 <code>Component</code> 坐标中的位置、
     * 当前选择的用户拖放操作、源支持的当前操作集，
     * 以及一个 <code>boolean</code> 值，指示源是否与目标在同一个 JVM 中。
     * <P>
     * @param dtc        此操作的 <code>DropTargetContext</code>
     * @param cursorLocn 拖放光标的热点在 <code>Component</code> 坐标中的位置
     * @param dropAction 用户拖放操作。
     * @param srcActions 源拖放操作。
     * @param isLocal  如果源与目标在同一个 JVM 中，则为真。
     *
     * @throws NullPointerException
     *         如果 cursorLocn 为 <code>null</code>
     * @throws IllegalArgumentException
     *         如果 dropAction 不是 <code>DnDConstants</code> 中的一个。
     * @throws IllegalArgumentException 如果 srcActions 不是 <code>DnDConstants</code> 的位掩码。
     * @throws IllegalArgumentException  如果 dtc 为 <code>null</code>。
     */

    public DropTargetDropEvent(DropTargetContext dtc, Point cursorLocn, int dropAction, int srcActions, boolean isLocal)  {
        this(dtc, cursorLocn, dropAction, srcActions);

        isLocalTx = isLocal;
    }

    /**
     * 此方法返回一个 <code>Point</code>，指示 <code>Cursor</code> 在 <code>Component</code> 坐标中的当前位置。
     * <P>
     * @return 当前的 <code>Cursor</code> 位置，以 Component 坐标表示。
     */

    public Point getLocation() {
        return location;
    }


    /**
     * 此方法返回当前的数据格式。
     * <P>
     * @return 当前的数据格式
     */

    public DataFlavor[] getCurrentDataFlavors() {
        return getDropTargetContext().getCurrentDataFlavors();
    }

    /**
     * 此方法返回当前可用的 <code>DataFlavor</code> 作为 <code>java.util.List</code>。
     * <P>
     * @return 当前可用的数据格式，作为 java.util.List
     */

    public List<DataFlavor> getCurrentDataFlavorsAsList() {
        return getDropTargetContext().getCurrentDataFlavorsAsList();
    }

    /**
     * 此方法返回一个 <code>boolean</code>，指示指定的 <code>DataFlavor</code> 是否从源可用。
     * <P>
     * @param df 要测试的 <code>DataFlavor</code>
     * <P>
     * @return 如果指定的数据格式从源可用，则返回 true
     */

    public boolean isDataFlavorSupported(DataFlavor df) {
        return getDropTargetContext().isDataFlavorSupported(df);
    }

    /**
     * 此方法返回源拖放操作。
     *
     * @return 源拖放操作。
     */
    public int getSourceActions() { return actions; }

    /**
     * 此方法返回用户拖放操作。
     *
     * @return 用户拖放操作。
     */
    public int getDropAction() { return dropAction; }

    /**
     * 此方法返回与拖放关联的 <code>Transferable</code> 对象。
     * <P>
     * @return 与拖放关联的 <code>Transferable</code>
     */

    public Transferable getTransferable() {
        return getDropTargetContext().getTransferable();
    }

    /**
     * 使用指定的操作接受拖放。
     * <P>
     * @param dropAction 指定的操作
     */

    public void acceptDrop(int dropAction) {
        getDropTargetContext().acceptDrop(dropAction);
    }

    /**
     * 拒绝拖放。
     */

    public void rejectDrop() {
        getDropTargetContext().rejectDrop();
    }

    /**
     * 此方法通知 <code>DragSource</code> 拖放传输已完成。
     * <P>
     * @param success 一个 <code>boolean</code>，指示拖放传输已完成。
     */

    public void dropComplete(boolean success) {
        getDropTargetContext().dropComplete(success);
    }

    /**
     * 此方法返回一个 <code>int</code>，指示源是否与目标在同一个 JVM 中。
     * <P>
     * @return 如果源与目标在同一个 JVM 中，则返回 true
     */

    public boolean isLocalTransfer() {
        return isLocalTx;
    }

    /*
     * 字段
     */

    static final private Point  zero     = new Point(0,0);

    /**
     * 拖放光标的热点在 Component 坐标中的位置。
     *
     * @serial
     */
    private Point               location   = zero;

    /**
     * 源拖放操作。
     *
     * @serial
     */
    private int                 actions    = DnDConstants.ACTION_NONE;

    /**
     * 用户拖放操作。
     *
     * @serial
     */
    private int                 dropAction = DnDConstants.ACTION_NONE;

    /**
     * 如果源与目标在同一个 JVM 中，则为 <code>true</code>。
     *
     * @serial
     */
    private boolean             isLocalTx = false;
}
