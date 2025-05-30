/*
 * Copyright (c) 1996, 2000, Oracle and/or its affiliates. All rights reserved.
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

package java.awt;

import java.awt.event.*;

/**
 * 包含可选择项的对象的接口。
 *
 * @author Amy Fowler
 */

public interface ItemSelectable {

    /**
     * 返回已选择的项或 <code>null</code> 如果没有项被选择。
     */
    public Object[] getSelectedObjects();

    /**
     * 添加一个监听器，当用户更改项的状态时接收项事件。项事件不会在项的状态被程序设置时发送。
     * 如果 <code>l</code> 为 <code>null</code>，则不抛出异常且不执行任何操作。
     *
     * @param    l 接收事件的监听器
     * @see ItemEvent
     */
    public void addItemListener(ItemListener l);

    /**
     * 移除一个项监听器。
     * 如果 <code>l</code> 为 <code>null</code>，
     * 则不抛出异常且不执行任何操作。
     *
     * @param   l 被移除的监听器
     * @see ItemEvent
     */
    public void removeItemListener(ItemListener l);
}
