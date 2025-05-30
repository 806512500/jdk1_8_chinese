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

package java.awt.dnd.peer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.InvalidDnDOperationException;

/**
 * <p>
 * 该接口由底层窗口系统平台提供，以启用对平台拖放操作的控制
 * </p>
 *
 * @since 1.2
 *
 */

public interface DropTargetContextPeer {

    /**
     * 更新对端目标操作的概念
     */

    void setTargetActions(int actions);

    /**
     * 获取当前的目标操作
     */

    int getTargetActions();

    /**
     * 获取与此对端关联的DropTarget
     */

    DropTarget getDropTarget();

    /**
     * 从对端获取（远程）数据类型
     */

    DataFlavor[] getTransferDataFlavors();

    /**
     * 获取远程数据的输入流
     */

    Transferable getTransferable() throws InvalidDnDOperationException;

    /**
     * @return 如果拖放源Transferable与目标在同一个JVM中
     */

    boolean isTransferableJVMLocal();

    /**
     * 接受拖动
     */

    void acceptDrag(int dragAction);

    /**
     * 拒绝拖动
     */

    void rejectDrag();

    /**
     * 接受放置
     */

    void acceptDrop(int dropAction);

    /**
     * 拒绝放置
     */

    void rejectDrop();

    /**
     * 信号完成
     */

    void dropComplete(boolean success);

}
