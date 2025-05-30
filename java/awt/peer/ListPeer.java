/*
 * Copyright (c) 1995, 1998, Oracle and/or its affiliates. All rights reserved.
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
package java.awt.peer;

import java.awt.Dimension;
import java.awt.List;

/**
 * {@link List} 的对等接口。
 *
 * 对等接口仅用于移植 AWT。不建议应用程序开发人员使用这些接口，也不应直接调用对等实例上的任何对等方法。
 */
public interface ListPeer extends ComponentPeer {

    /**
     * 返回当前选中的列表项的索引。返回的数组不需要是副本，调用此方法的调用者已经确保不会修改它。
     *
     * @return 当前选中的列表项的索引
     *
     * @see List#getSelectedIndexes()
     */
    int[] getSelectedIndexes();

    /**
     * 在指定索引处向列表中添加一个项。
     *
     * @param item 要添加到列表中的项
     * @param index 要将项添加到列表中的索引
     *
     * @see List#add(String, int)
     */
    void add(String item, int index);

    /**
     * 从列表中删除项。从 start 到 end 的所有项都将被删除，包括 start 和 end 索引处的项。
     *
     * @param start 要删除的第一个项
     * @param end 要删除的最后一个项
     */
    void delItems(int start, int end);

    /**
     * 从列表中删除所有项。
     *
     * @see List#removeAll()
     */
    void removeAll();

    /**
     * 选择指定索引处的项。
     *
     * @param index 要选择的项的索引
     *
     * @see List#select(int)
     */
    void select(int index);

    /**
     * 取消选择指定索引处的项。
     *
     * @param index 要取消选择的项的索引
     *
     * @see List#deselect(int)
     */
    void deselect(int index);

    /**
     * 确保指定索引处的项可见，通过滚动列表或类似操作。
     *
     * @param index 要使其可见的项的索引
     *
     * @see List#makeVisible(int)
     */
    void makeVisible(int index);

    /**
     * 切换多选模式的开启或关闭。
     *
     * @param m {@code true} 表示多选模式，{@code false} 表示单选模式
     *
     * @see List#setMultipleMode(boolean)
     */
    void setMultipleMode(boolean m);

    /**
     * 返回具有指定行数的列表的首选大小。
     *
     * @param rows 行数
     *
     * @return 列表的首选大小
     *
     * @see List#getPreferredSize(int)
     */
    Dimension getPreferredSize(int rows);

    /**
     * 返回具有指定行数的列表的最小大小。
     *
     * @param rows 行数
     *
     * @return 列表的最小大小
     *
     * @see List#getMinimumSize(int)
     */
    Dimension getMinimumSize(int rows);

}
