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

/**
 * <code>DragSourceDropEvent</code> 由 <code>DragSourceContextPeer</code>
 * 通过 <code>DragSourceContext</code> 发送到注册在该 <code>DragSourceContext</code>
 * 和其关联的 <code>DragSource</code> 上的 <code>DragSourceListener</code> 的 <code>dragDropEnd</code>
 * 方法。它包含足够的信息，使操作的发起者在操作完成时能够为最终用户提供适当的反馈。
 * <P>
 * @since 1.2
 */

public class DragSourceDropEvent extends DragSourceEvent {

    private static final long serialVersionUID = -5571321229470821891L;

    /**
     * 构造一个用于放置操作的 <code>DragSourceDropEvent</code>，
     * 给定 <code>DragSourceContext</code>、放置操作和一个表示放置是否成功的布尔值。
     * 此 <code>DragSourceDropEvent</code> 的坐标未指定，因此 <code>getLocation</code> 将返回
     * <code>null</code>。
     * <p>
     * 参数 <code>action</code> 应该是表示单个操作的 <code>DnDConstants</code> 之一。
     * 此构造函数不会因无效的 <code>action</code> 而抛出任何异常。
     *
     * @param dsc 与此 <code>DragSourceDropEvent</code> 关联的 <code>DragSourceContext</code>
     * @param action 放置操作
     * @param success 表示放置是否成功的布尔值
     *
     * @throws IllegalArgumentException 如果 <code>dsc</code> 为 <code>null</code>。
     *
     * @see DragSourceEvent#getLocation
     */

    public DragSourceDropEvent(DragSourceContext dsc, int action, boolean success) {
        super(dsc);

        dropSuccess = success;
        dropAction  = action;
    }

    /**
     * 构造一个用于放置操作的 <code>DragSourceDropEvent</code>，给定
     * <code>DragSourceContext</code>、放置操作、一个表示放置是否成功的布尔值和坐标。
     * <p>
     * 参数 <code>action</code> 应该是表示单个操作的 <code>DnDConstants</code> 之一。
     * 此构造函数不会因无效的 <code>action</code> 而抛出任何异常。
     *
     * @param dsc 与此 <code>DragSourceDropEvent</code> 关联的 <code>DragSourceContext</code>
     * @param action 放置操作
     * @param success 表示放置是否成功的布尔值
     * @param x   光标位置的水平坐标
     * @param y   光标位置的垂直坐标
     *
     * @throws IllegalArgumentException 如果 <code>dsc</code> 为 <code>null</code>。
     *
     * @since 1.4
     */
    public DragSourceDropEvent(DragSourceContext dsc, int action,
                               boolean success, int x, int y) {
        super(dsc, x, y);

        dropSuccess = success;
        dropAction  = action;
    }

    /**
     * 构造一个用于未导致放置操作的拖动操作的 <code>DragSourceDropEvent</code>。
     * 此 <code>DragSourceDropEvent</code> 的坐标未指定，因此 <code>getLocation</code> 将返回
     * <code>null</code>。
     *
     * @param dsc 与此 <code>DragSourceDropEvent</code> 关联的 <code>DragSourceContext</code>
     *
     * @throws IllegalArgumentException 如果 <code>dsc</code> 为 <code>null</code>。
     *
     * @see DragSourceEvent#getLocation
     */

    public DragSourceDropEvent(DragSourceContext dsc) {
        super(dsc);

        dropSuccess = false;
    }

    /**
     * 此方法返回一个布尔值，表示放置是否成功。
     *
     * @return 如果放置目标接受放置并成功执行放置操作，则返回 <code>true</code>；
     *         如果放置目标拒绝放置或放置目标接受放置但未能执行放置操作，则返回 <code>false</code>。
     */

    public boolean getDropSuccess() { return dropSuccess; }

    /**
     * 此方法返回一个整数，表示目标对放置对象执行的操作。
     *
     * @return 如果放置目标接受放置且目标放置操作受拖动源支持，则返回目标对放置对象执行的操作；
     *         否则，返回 <code>DnDConstants.ACTION_NONE</code>。
     */

    public int getDropAction() { return dropAction; }

    /*
     * 字段
     */

    /**
     * 如果放置成功，则为 <code>true</code>。
     *
     * @serial
     */
    private boolean dropSuccess;

    /**
     * 放置操作。
     *
     * @serial
     */
    private int     dropAction   = DnDConstants.ACTION_NONE;
}
