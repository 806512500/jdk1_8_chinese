/*
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
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

/**
 * <code>DragSourceListener</code> 定义了拖放操作的发起者用于跟踪用户手势状态的事件接口，
 * 并在整个拖放操作过程中为用户提供适当的“拖动”反馈。
 * <p>
 * 下拉站点 <i>与之前的 <code>dragEnter()</code> 调用相关联</i>，如果最新的 <code>dragEnter()</code> 调用：
 * <ul>
 * <li>对应于该下拉站点，并且
 * <li>未被此监听器的 <code>dragExit()</code> 调用所跟随。
 * </ul>
 *
 * @since 1.2
 */

public interface DragSourceListener extends EventListener {

    /**
     * 当光标的热点进入平台依赖的下拉站点时调用。
     * 当以下所有条件为真时调用此方法：
     * <UL>
     * <LI>光标的热点进入平台依赖的下拉站点的操作部分。
     * <LI>下拉站点处于活动状态。
     * <LI>下拉站点接受拖动。
     * </UL>
     *
     * @param dsde <code>DragSourceDragEvent</code>
     */
    void dragEnter(DragSourceDragEvent dsde);

    /**
     * 当光标的热点在平台依赖的下拉站点上移动时调用。
     * 当以下所有条件为真时调用此方法：
     * <UL>
     * <LI>光标的热点已移动，但仍然与之前 <code>dragEnter()</code> 调用相关联的下拉站点的操作部分相交。
     * <LI>下拉站点仍然处于活动状态。
     * <LI>下拉站点接受拖动。
     * </UL>
     *
     * @param dsde <code>DragSourceDragEvent</code>
     */
    void dragOver(DragSourceDragEvent dsde);

    /**
     * 当用户修改了下拉手势时调用。
     * 当用户正在交互的输入设备的状态发生变化时调用此方法。
     * 这些设备通常是用户正在交互的鼠标按钮或键盘修饰键。
     *
     * @param dsde <code>DragSourceDragEvent</code>
     */
    void dropActionChanged(DragSourceDragEvent dsde);

    /**
     * 当光标的热点离开平台依赖的下拉站点时调用。
     * 当以下任何条件为真时调用此方法：
     * <UL>
     * <LI>光标的热点不再与之前 <code>dragEnter()</code> 调用相关联的下拉站点的操作部分相交。
     * </UL>
     * 或
     * <UL>
     * <LI>与之前 <code>dragEnter()</code> 调用相关联的下拉站点不再处于活动状态。
     * </UL>
     * 或
     * <UL>
     * <LI>与之前 <code>dragEnter()</code> 调用相关联的下拉站点拒绝了拖动。
     * </UL>
     *
     * @param dse <code>DragSourceEvent</code>
     */
    void dragExit(DragSourceEvent dse);

    /**
     * 调用此方法表示拖放操作已完成。
     * 可以使用 <code>DragSourceDropEvent</code> 的 getDropSuccess() 方法来确定终止状态。
     * getDropAction() 方法返回下拉站点选择应用于下拉操作的操作。
     * 一旦此方法完成，当前的 <code>DragSourceContext</code> 和相关资源将变得无效。
     *
     * @param dsde <code>DragSourceDropEvent</code>
     */
    void dragDropEnd(DragSourceDropEvent dsde);
}
