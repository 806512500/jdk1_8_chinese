/*
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 一个用于接收拖放目标事件的抽象适配器类。此类中的方法都是空的。此类仅作为创建监听器对象的便利而存在。
 * <p>
 * 扩展此类以创建 <code>DropTargetEvent</code> 监听器，并覆盖您感兴趣的事件的方法。（如果您实现了
 * <code>DropTargetListener</code> 接口，则必须定义该接口中的所有方法。此抽象类为除了
 * <code>drop(DropTargetDropEvent)</code> 之外的每个方法定义了一个空实现，因此您只需定义您关心的事件的方法。）
 * 您必须至少为 <code>drop(DropTargetDropEvent)</code> 提供实现。此方法不能有空实现，因为其规范要求您接受或拒绝拖放，
 * 并且如果接受，则指示拖放是否成功。
 * <p>
 * 使用扩展类创建监听器对象，然后将其注册到 <code>DropTarget</code>。当拖动进入、移动到或离开该
 * <code>DropTarget</code> 的可操作部分，当拖动操作改变，以及当拖放发生时，监听器对象中的相关方法将被调用，
 * 并将 <code>DropTargetEvent</code> 传递给它。
 * <p>
 * <code>DropTarget</code> 的可操作部分是与之关联的 <code>Component</code> 的几何形状中未被重叠的顶级窗口或
 * Z 顺序中较高的另一个具有活动 <code>DropTarget</code> 的 <code>Component</code> 遮挡的部分。
 * <p>
 * 在拖动过程中，可以通过调用 <code>getTransferable()</code> 方法从传递给监听器方法的
 * <code>DropTargetDragEvent</code> 实例中检索与当前拖动操作相关联的数据。
 * <p>
 * 请注意，应在相应的监听器方法中调用 <code>DropTargetDragEvent</code> 实例的
 * <code>getTransferable()</code> 方法，并在该方法返回之前从返回的 <code>Transferable</code> 中检索所有必要的数据。
 *
 * @see DropTargetEvent
 * @see DropTargetListener
 *
 * @author David Mendenhall
 * @since 1.4
 */
public abstract class DropTargetAdapter implements DropTargetListener {

    /**
     * 在拖动操作进行中，当鼠标指针进入注册了此监听器的 <code>DropTarget</code> 的可操作部分时调用。
     *
     * @param dtde <code>DropTargetDragEvent</code>
     */
    public void dragEnter(DropTargetDragEvent dtde) {}

    /**
     * 在拖动操作进行中，当鼠标指针仍在注册了此监听器的 <code>DropTarget</code> 的可操作部分上时调用。
     *
     * @param dtde <code>DropTargetDragEvent</code>
     */
    public void dragOver(DropTargetDragEvent dtde) {}

    /**
     * 当用户修改了当前的拖放手势时调用。
     *
     * @param dtde <code>DropTargetDragEvent</code>
     */
    public void dropActionChanged(DropTargetDragEvent dtde) {}

    /**
     * 在拖动操作进行中，当鼠标指针已离开注册了此监听器的 <code>DropTarget</code> 的可操作部分时调用。
     *
     * @param dte <code>DropTargetEvent</code>
     */
    public void dragExit(DropTargetEvent dte) {}
}
