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
 * 一个用于接收拖放源事件的抽象适配器类。此类中的方法为空。此类仅作为创建监听器对象的便利而存在。
 * <p>
 * 扩展此类以创建 <code>DragSourceEvent</code> 监听器，并覆盖感兴趣的事件方法。（如果你实现 <code>DragSourceListener</code> 接口，
 * 则必须定义该接口中的所有方法。此抽象类为所有方法定义了空方法，因此你只需定义感兴趣的事件方法。）
 * <p>
 * 使用扩展类创建监听器对象，然后将其注册到 <code>DragSource</code>。当拖动进入、移动到、或离开一个放置站点，
 * 当放置操作改变，以及当拖动结束时，相关的监听器对象方法将被调用，并传递 <code>DragSourceEvent</code>。
 * <p>
 * 放置站点与 <i>前一个 <code>dragEnter()</code> 调用相关联</i>，如果此适配器上的最新 <code>dragEnter()</code> 调用
 * 对应于该放置站点，并且没有被此适配器上的 <code>dragExit()</code> 调用所跟随。
 *
 * @see DragSourceEvent
 * @see DragSourceListener
 * @see DragSourceMotionListener
 *
 * @author David Mendenhall
 * @since 1.4
 */
public abstract class DragSourceAdapter
    implements DragSourceListener, DragSourceMotionListener {

    /**
     * 当光标的热点进入平台依赖的放置站点时调用。当以下所有条件为真时调用此方法：
     * <UL>
     * <LI>光标的热点进入平台依赖的放置站点的操作部分。
     * <LI>放置站点处于活动状态。
     * <LI>放置站点接受拖动。
     * </UL>
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dragEnter(DragSourceDragEvent dsde) {}

    /**
     * 当光标的热点在平台依赖的放置站点上移动时调用。当以下所有条件为真时调用此方法：
     * <UL>
     * <LI>光标的热点已移动，但仍然与前一个 <code>dragEnter()</code> 调用相关联的放置站点的操作部分相交。
     * <LI>放置站点仍然处于活动状态。
     * <LI>放置站点接受拖动。
     * </UL>
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dragOver(DragSourceDragEvent dsde) {}

    /**
     * 在拖动操作期间每当鼠标移动时调用。
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dragMouseMoved(DragSourceDragEvent dsde) {}

    /**
     * 当用户修改了放置手势时调用。当用户交互的输入设备（通常是鼠标按钮或键盘修饰键）的状态改变时调用此方法。
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dropActionChanged(DragSourceDragEvent dsde) {}

    /**
     * 当光标的热点离开平台依赖的放置站点时调用。当以下任一条件为真时调用此方法：
     * <UL>
     * <LI>光标的热点不再与前一个 <code>dragEnter()</code> 调用相关联的放置站点的操作部分相交。
     * </UL>
     * 或
     * <UL>
     * <LI>前一个 <code>dragEnter()</code> 调用相关联的放置站点不再处于活动状态。
     * </UL>
     * 或
     * <UL>
     * <LI>前一个 <code>dragEnter()</code> 调用相关联的放置站点拒绝了拖动。
     * </UL>
     *
     * @param dse the <code>DragSourceEvent</code>
     */
    public void dragExit(DragSourceEvent dse) {}

    /**
     * 调用此方法表示拖放操作已完成。<code>DragSourceDropEvent</code> 的 getDropSuccess() 方法可以用来
     * 确定终止状态。getDropAction() 方法返回放置站点选择应用于放置操作的操作。一旦此方法完成，
     * 当前的 <code>DragSourceContext</code> 和关联的资源将变得无效。
     *
     * @param dsde the <code>DragSourceDropEvent</code>
     */
    public void dragDropEnd(DragSourceDropEvent dsde) {}
}
