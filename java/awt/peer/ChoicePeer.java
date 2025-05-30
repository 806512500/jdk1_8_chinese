/*
 * Copyright (c) 1995, 2007, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Choice;

/**
 * {@link Choice} 的对等接口。
 *
 * 对等接口仅用于移植 AWT。不建议应用程序开发人员使用这些接口，也不应直接调用对等实例上的任何对等方法。
 */
public interface ChoicePeer extends ComponentPeer {

    /**
     * 在索引 {@code index} 处将带有字符串 {@code item} 的项添加到组合框列表中。
     *
     * @param item 要添加到列表中的标签
     * @param index 要添加项的索引
     *
     * @see Choice#add(String)
     */
    void add(String item, int index);

    /**
     * 从组合框列表中移除索引 {@code index} 处的项。
     *
     * @param index 要移除项的索引
     *
     * @see Choice#remove(int)
     */
    void remove(int index);

    /**
     * 从组合框列表中移除所有项。
     *
     * @see Choice#removeAll()
     */
    void removeAll();

    /**
     * 选择索引 {@code index} 处的项。
     *
     * @param index 应该被选中的索引
     *
     * @see Choice#select(int)
     */
    void select(int index);

}
