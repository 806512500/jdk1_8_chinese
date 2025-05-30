/*
 * Copyright (c) 1996, 2001, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 定义一个接口，用于知道如何根据布局约束对象布局容器的类。
 *
 * 该接口扩展了 LayoutManager 接口，以处理显式使用约束对象的布局，这些对象指定组件应如何以及在何处添加到布局中。
 * <p>
 * 这个对 LayoutManager 的最小扩展旨在为希望创建基于约束的布局的工具提供商提供支持。
 * 它目前尚未提供对自定义基于约束的布局管理器的全面支持。
 *
 * @see LayoutManager
 * @see Container
 *
 * @author      Jonni Kanerva
 */
public interface LayoutManager2 extends LayoutManager {

    /**
     * 使用指定的约束对象将指定的组件添加到布局中。
     * @param comp 要添加的组件
     * @param constraints  组件添加到布局中的位置和方式。
     */
    void addLayoutComponent(Component comp, Object constraints);

    /**
     * 计算指定容器的最大尺寸，考虑其包含的组件。
     * @see java.awt.Component#getMaximumSize
     * @see LayoutManager
     */
    public Dimension maximumLayoutSize(Container target);

    /**
     * 返回 x 轴上的对齐方式。这指定了组件相对于其他组件的对齐方式。
     * 值应为 0 到 1 之间的数字，其中 0 表示沿原点对齐，1 表示远离原点对齐，0.5 表示居中，等等。
     */
    public float getLayoutAlignmentX(Container target);

    /**
     * 返回 y 轴上的对齐方式。这指定了组件相对于其他组件的对齐方式。
     * 值应为 0 到 1 之间的数字，其中 0 表示沿原点对齐，1 表示远离原点对齐，0.5 表示居中，等等。
     */
    public float getLayoutAlignmentY(Container target);

    /**
     * 使布局无效，指示如果布局管理器有缓存信息，则应丢弃。
     */
    public void invalidateLayout(Container target);

}
